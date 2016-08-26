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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.ProtectionDomain;
import java.util.AbstractList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.apache.commons.io.IOUtils;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.enhancer.DataNucleusClassFileTransformer;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemLocation;
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
					if (null != f.getAnnotation(Persistent.class)) {
						if (null != f.getType().getAnnotation(PersistenceCapable.class)) {
							this.fetchEnhanced(f.getType());
						}
					} else if (null != f.getAnnotation(Join.class)) {

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
	 *            the Persistable type
	 * @param src
	 *            the Persistable Object
	 * @param tgtType
	 * @param tgt
	 */
	void copyOutOf(final Class<?> srcType, final Object src, final Class<?> tgtType, final Object tgt)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException {
		for (final Field f : srcType.getFields()) {
			if (null != f.getAnnotation(Persistent.class)) {
				final Field tgtField = tgtType.getField(f.getName());
				if (Persistable.class.isAssignableFrom(f.getType())) {
					final Object v = tgtField.getType().newInstance();
					this.copyOutOf(f.getType(), f.get(src), tgtField.getType(), v);
					tgtField.set(tgt, v);
				} else if (List.class.isAssignableFrom(f.getType()) && this.isPersistentCollection(f.getGenericType())) {
					final Class<?> tgtCollObjType = (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
					final List<?> srcColl = (List<?>) f.get(src);
					final List<?> tgtColl = new AbstractList<Object>() {
						@Override
						public Object get(final int index) {
							try {
								final Object srcCollObj = srcColl.get(index);
								final Object tgtCollObj = tgtCollObjType.newInstance();
								JdoPersistence.this.copyInto(srcCollObj.getClass(), srcCollObj, tgtCollObjType, tgtCollObj);
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
					};
					tgtField.set(tgt, tgtColl);
				} else {
					tgtField.set(tgt, f.get(src));
				}
			} else {
			}
		}
	}

	/**
	 *
	 * @param srcType
	 * @param src
	 * @param tgtType
	 *            the Persistable type
	 * @param tgt
	 *            the Persistable object
	 */
	void copyInto(final Class<?> srcType, final Object src, final Class<?> tgtType, final Object tgt)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException {
		for (final Field f : srcType.getFields()) {
			if (null != f.getAnnotation(Persistent.class)) {
				final Field tgtField = tgtType.getField(f.getName());
				if (Persistable.class.isAssignableFrom(tgtField.getType())) {
					final Object v = tgtField.getType().newInstance();
					this.copyInto(f.getType(), f.get(src), tgtField.getType(), v);
					tgtField.set(tgt, v);
				} else if (List.class.isAssignableFrom(tgtField.getType()) && this.isPersistentCollection(tgtField.getGenericType())) {
					final Class<?> tgtCollObjType = (Class<?>) ((ParameterizedType) tgtField.getGenericType()).getActualTypeArguments()[0];
					final List<?> srcColl = (List<?>) f.get(src);
					final List<?> tgtColl = new AbstractList<Object>() {
						@Override
						public Object get(final int index) {
							try {
								final Object srcCollObj = srcColl.get(index);
								final Object tgtCollObj = tgtCollObjType.newInstance();
								JdoPersistence.this.copyInto(srcCollObj.getClass(), srcCollObj, tgtCollObjType, tgtCollObj);
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
					};
					tgtField.set(tgt, tgtColl);
				} else {
					tgtField.set(tgt, f.get(src));
				}
			} else {
				// don't store
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
	public <T> void store(final IPersistenceTransaction transaction, final PersistentItemLocation location, final T item, final Class<T> itemType)
			throws PersistentStoreException {
		final Transaction tx = this.manager.currentTransaction();
		try {
			Object toPersist = null;
			if (item instanceof Persistable) {
				toPersist = item;
			} else {
				final Class<? extends Persistable> enhancedType = this.fetchEnhanced(itemType);
				final Persistable enhancedObj = enhancedType.newInstance();
				this.copyInto(itemType, item, enhancedType, enhancedObj);
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
	public <T> T retrieve(final IPersistenceTransaction transaction, final PersistentItemLocation location, final Class<T> itemType)
			throws PersistentStoreException {
		try {
			final Class<? extends Persistable> enhancedType = this.fetchEnhanced(itemType);
			final Query<? extends Persistable> query = this.manager.newQuery(enhancedType, location.asPrimitive());
			final Collection<T> res = (Collection<T>) query.execute();
			if (res.isEmpty()) {
				return null;
			} else {
				final Object o = res.iterator().next();
				if (itemType.isInstance(o)) {
					return (T) o;
				} else {
					final T item = itemType.newInstance();
					this.copyOutOf(enhancedType, o, itemType, item);
					return item;
				}
			}
		} catch (final Exception ex) {
			throw new PersistentStoreException("", ex);
		}
	}

	@Override
	public <T> Set<T> retrieve(final IPersistenceTransaction transaction, final PersistentItemLocation location, final Class<T> itemType,
			final Map<String, Object> filter) throws PersistentStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Set<T> retrieveAll(final IPersistenceTransaction transaction, final Class<T> itemType) {
		final Class<? extends Persistable> enhancedType = this.fetchEnhanced(itemType);
		final Query<? extends Persistable> query = this.manager.newQuery(enhancedType);
		final Collection<T> res = (Collection<T>) query.execute();
		return new HashSet<>(res);
	}

	// ---------- Ports ---------
	@PortInstance(provides = { IPersistentStore.class }, requires = {})
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
