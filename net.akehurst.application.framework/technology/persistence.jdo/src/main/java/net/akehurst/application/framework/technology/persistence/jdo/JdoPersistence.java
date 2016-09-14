/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.akehurst.application.framework.technology.persistence.jdo;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.ProtectionDomain;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.apache.commons.io.IOUtils;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.enhancer.DataNucleusClassFileTransformer;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemQuery;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;
//import sun.misc.IOUtils;

/**
 *
 * Must ensure that compatible versions are in use for javax.jdo, datanucleus-api-jdo, datanucleus-core, and the datanucleus-* make sure to use
 * org.datanucleus:javax.jdo, rather than javax.jdo:jdo-api
 *
 * @author akehurst
 *
 */
public class JdoPersistence extends AbstractComponent implements IPersistentStore {

	static class DynClassloader extends ClassLoader {
		public DynClassloader(final ClassLoader parent) {
			super(parent);
		}

		public synchronized Class<?> defineClass(final String fullClassName, final byte[] bytes) {
			try {
				return this.defineClass(fullClassName, bytes, 0, bytes.length);
			} finally {
			}
		}

		@Override
		public Class<?> loadClass(final String name) throws ClassNotFoundException {
			return super.loadClass(name, true);
		}

		@Override
		protected Class<?> findClass(final String name) throws ClassNotFoundException {
			// TODO Auto-generated method stub
			return super.findClass(name);
		}
	}

	public JdoPersistence(final String id) {
		super(id);
		this.cl = new DynClassloader(this.getClass().getClassLoader());
	}

	@ServiceReference
	ILogger logger;

	PersistenceManager manager;

	@Override
	public void afRun() {

	}

	DynClassloader cl;

	<T> Class<? extends Persistable> fetchEnhanced(final Class<T> class_) {
		final String enhancedName = class_.getName(); // has to match for transform to work
		try {
			Class<?> enhCls = null;
			try {
				enhCls = new DynClassloader(this.cl).loadClass(enhancedName);
			} catch (final ClassNotFoundException e) {
			}
			if (null != enhCls && Persistable.class.isAssignableFrom(enhCls)) {
				return (Class<? extends Persistable>) enhCls;
			} else {

				final DataNucleusClassFileTransformer t = new DataNucleusClassFileTransformer("-api=JDO", null);
				final ProtectionDomain protectionDomain = class_.getProtectionDomain();
				final InputStream is = class_.getClassLoader().getResourceAsStream(class_.getName().replace(".", "/") + ".class");
				final byte[] classfileBuffer = IOUtils.toByteArray(is);

				final byte[] bytes = t.transform(new DynClassloader(this.cl), class_.getName(), null, protectionDomain, classfileBuffer);
				final Class<? extends Persistable> res = (Class<? extends Persistable>) this.cl.defineClass(enhancedName, bytes);

				for (final Field f : class_.getFields()) {
					final Class<?> fieldType = f.getType();
					if (null != f.getAnnotation(Persistent.class)) {
						if (null != fieldType.getAnnotation(PersistenceCapable.class)) {
							this.fetchEnhanced(fieldType);
						} else if (List.class.isAssignableFrom(fieldType)) {
							final Class<?> listElementType = (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
							this.fetchEnhanced(listElementType);
						}
					}
				}

				for (final Method a : class_.getMethods()) {
					if (null != a.getAnnotation(Persistent.class)) {
						if (null != a.getReturnType().getAnnotation(PersistenceCapable.class)) {
							this.fetchEnhanced(a.getReturnType());
						}
					}
				}

				return (Class<? extends Persistable>) new DynClassloader(this.cl).loadClass(enhancedName);
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 *
	 * @param srcType
	 * @param src
	 * @param tgtType
	 *            enhancedType
	 * @param tgt
	 *            enhanced object
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	void convertToEnhanced(final Class<?> srcType, final Object src, final Class<?> tgtType, final Object tgt) throws IllegalArgumentException,
			IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException, NoSuchMethodException, InvocationTargetException {
		for (final Field tgtField : tgtType.getFields()) {
			if (null != tgtField.getAnnotation(Persistent.class)) {
				final Field srcField = srcType.getField(tgtField.getName());
				if (Persistable.class.isAssignableFrom(tgtField.getType())) {
					final Object tgtFieldObj = tgtField.getType().newInstance();
					final Object srcFieldObj = srcField.get(src);
					if (null != srcFieldObj) {
						this.convertToEnhanced(srcField.getType(), srcFieldObj, tgtField.getType(), tgtFieldObj);
						tgtField.set(tgt, tgtFieldObj);
					}
				} else if (List.class.isAssignableFrom(tgtField.getType()) && this.isPersistentCollection(tgtField.getGenericType())) {
					final Class<?> srcCollObjType = (Class<?>) ((ParameterizedType) srcField.getGenericType()).getActualTypeArguments()[0];
					final Class<?> tgtCollObjType = (Class<?>) ((ParameterizedType) tgtField.getGenericType()).getActualTypeArguments()[0];
					final List<Object> srcColl = (List<Object>) srcField.get(src);
					// TODO: not sure why srccoll is null here ? something to do with the jdo mapping
					final List<Object> tgtColl = null == srcColl ? null : new AbstractList<Object>() {
						@Override
						public Object get(final int index) {
							try {
								final Object srcCollObj = srcColl.get(index);
								final Object tgtCollObj = tgtCollObjType.newInstance();
								JdoPersistence.this.convertToEnhanced(srcCollObj.getClass(), srcCollObj, tgtCollObjType, tgtCollObj);
								return tgtCollObj;
							} catch (final Exception ex) {
								ex.printStackTrace();
								return null;
							}
						}

						@Override
						public int size() {
							return srcColl.size();
						}

						@Override
						public void add(final int index, final Object element) {
							try {
								final Object srcCollObj = srcCollObjType.newInstance();
								JdoPersistence.this.convertFromEnhanced(element.getClass(), element, srcCollObjType, srcCollObj);
								srcColl.add(srcCollObj);
							} catch (final Exception ex) {
								ex.printStackTrace();
							}
						}
					};
					tgtField.set(tgt, tgtColl);
				} else {
					tgtField.set(tgt, srcField.get(src));
				}
			} else {
			}
		}
	}

	/**
	 *
	 * @param srcType
	 *            enhanced type
	 * @param src
	 *            enhanced object
	 * @param tgtType
	 * @param tgt
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	void convertFromEnhanced(final Class<?> srcType, final Object src, final Class<?> tgtType, final Object tgt) throws IllegalArgumentException,
			IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException, NoSuchMethodException, InvocationTargetException {
		for (final Field srcField : srcType.getFields()) {
			if (null != srcField.getAnnotation(Persistent.class)) {
				final Field tgtField = tgtType.getField(srcField.getName());
				if (Persistable.class.isAssignableFrom(srcField.getType())) {
					final Object tgtFieldObj = tgtField.getType().newInstance();
					final Object srcFieldObj = srcField.get(src);
					if (null != srcFieldObj) {
						this.convertFromEnhanced(srcField.getType(), srcFieldObj, tgtField.getType(), tgtFieldObj);
						tgtField.set(tgt, tgtFieldObj);
					}
				} else if (List.class.isAssignableFrom(srcField.getType()) && this.isPersistentCollection(srcField.getGenericType())) {
					final Class<?> srcCollObjType = (Class<?>) ((ParameterizedType) srcField.getGenericType()).getActualTypeArguments()[0];
					final Class<?> tgtCollObjType = (Class<?>) ((ParameterizedType) tgtField.getGenericType()).getActualTypeArguments()[0];
					final List<Object> srcColl = (List<Object>) srcField.get(src);
					// TODO: not sure why srccoll is null here ? something to do with the jdo mapping
					final List<Object> tgtColl = null == srcColl ? null : new AbstractList<Object>() {
						@Override
						public Object get(final int index) {
							try {
								final Object srcCollObj = srcColl.get(index);
								final Object tgtCollObj = tgtCollObjType.newInstance();
								JdoPersistence.this.convertFromEnhanced(srcCollObj.getClass(), srcCollObj, tgtCollObjType, tgtCollObj);
								return tgtCollObj;
							} catch (final Exception ex) {
								ex.printStackTrace();
								return null;
							}
						}

						@Override
						public int size() {
							return srcColl.size();
						}

						@Override
						public void add(final int index, final Object element) {
							try {
								final Object srcCollObj = srcCollObjType.newInstance();
								JdoPersistence.this.convertToEnhanced(element.getClass(), element, srcCollObjType, srcCollObj);
								srcColl.add(srcCollObj);
							} catch (final Exception ex) {
								ex.printStackTrace();
							}
						}
					};
					tgtField.set(tgt, tgtColl);
				} else {
					tgtField.set(tgt, srcField.get(src));
				}
			} else {
			}
		}
	}

	boolean isPersistentCollection(final Type type) {
		final ParameterizedType pt = (ParameterizedType) type;
		for (final Type t : pt.getActualTypeArguments()) {
			final boolean b = Persistable.class.isAssignableFrom((Class<?>) t);
			if (b) {
				// iterate
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public void connect(final Map<String, Object> properties) {
		final PersistenceManagerFactory factory = JDOHelper.getPersistenceManagerFactory(properties);
		this.manager = factory.getPersistenceManager();
	}

	@Override
	public <T> void store(final IPersistenceTransaction transaction, final PersistentItemQuery query, final T item, final Class<T> itemType)
			throws PersistentStoreException {
		final Transaction tx = this.manager.currentTransaction();
		try {
			Object toPersist = null;
			if (item instanceof Persistable) {
				toPersist = item;
			} else {
				final Class<? extends Persistable> enhancedType = this.fetchEnhanced(itemType);
				final Persistable enhancedObj = enhancedType.newInstance();
				this.convertToEnhanced(itemType, item, enhancedType, enhancedObj);
				toPersist = enhancedObj;
			}

			tx.begin();

			this.manager.makePersistent(toPersist);

			tx.commit();
		} catch (final Throwable ex) {
			this.logger.log(LogLevel.ERROR, "Failed to store persistent item", ex);
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
		}
	}

	@Override
	public <T> void remove(final IPersistenceTransaction transaction, final PersistentItemQuery query, final Class<T> itemType)
			throws PersistentStoreException {
		final Transaction tx = this.manager.currentTransaction();
		try {
			final Class<? extends Persistable> enhancedType = this.fetchEnhanced(itemType);

			tx.begin();

			final Query<? extends Persistable> jdoQuery = this.manager.newQuery(enhancedType, query.asPrimitive());
			jdoQuery.compile();
			final Collection<? extends Persistable> res = (Collection<? extends Persistable>) jdoQuery.execute();
			for (final Persistable jdoObj : res) {
				this.manager.deletePersistent(jdoObj);
			}

			tx.commit();

		} catch (final Throwable ex) {
			this.logger.log(LogLevel.ERROR, "Failed to remove persistent item", ex);
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
		}
	}

	@Override
	public <T> T retrieve(final IPersistenceTransaction transaction, final PersistentItemQuery query, final Class<T> itemType) throws PersistentStoreException {
		try {
			final Class<? extends Persistable> enhancedType = this.fetchEnhanced(itemType);
			final Query<? extends Persistable> jdoquery = this.manager.newQuery(enhancedType, query.asPrimitive());
			final Collection<T> res = (Collection<T>) jdoquery.execute();
			if (res.isEmpty()) {
				return null;
			} else {
				final Object o = res.iterator().next();
				if (itemType.isInstance(o)) {
					return (T) o;
				} else {
					final T item = itemType.newInstance();
					this.convertFromEnhanced(enhancedType, o, itemType, item);
					return item;
				}
			}
		} catch (final Exception ex) {
			throw new PersistentStoreException("", ex);
		}
	}

	@Override
	public <T> Set<T> retrieve(final IPersistenceTransaction transaction, final PersistentItemQuery query, final Class<T> itemType,
			final Map<String, Object> filter) throws PersistentStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Set<T> retrieveAll(final IPersistenceTransaction transaction, final Class<T> itemType) {
		final Class<? extends Persistable> enhancedType = this.fetchEnhanced(itemType);
		final Query<? extends Persistable> query = this.manager.newQuery(enhancedType);
		final Collection<T> res = (Collection<T>) query.execute();
		final Set<T> result = res.stream().map((el) -> {
			try {
				final T item = itemType.newInstance();
				this.convertFromEnhanced(enhancedType, el, itemType, item);
				return item;
			} catch (final Exception ex) {
				throw new RuntimeException("Error mapping JDO enhanced object to un-enhanced", ex);
			}
		}).collect(Collectors.toSet());
		return result;
	}

	// ---------- Ports ---------
	@PortInstance
	@PortContract(provides = IPersistentStore.class)
	IPort portPersist;

	public IPort portPersist() {
		return this.portPersist;
	}

	@Override
	public IPersistenceTransaction startTransaction() {
		return null;
	}

	@Override
	public void commitTransaction(final IPersistenceTransaction transaction) {
		// TODO Auto-generated method stub

	}

}
