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
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOEnhancer;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.metadata.JDOMetadata;

import org.datanucleus.enhancement.Persistable;
import org.datanucleus.enhancer.DataNucleusClassFileTransformer;
import org.datanucleus.store.types.converters.ByteArrayByteBufferConverter;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.realisation.Port;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemLocation;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;
//import sun.misc.IOUtils;

public class JdoPersistence extends AbstractComponent implements IPersistentStore {

	static class DynClassloader extends ClassLoader {
		public DynClassloader(ClassLoader parent) {
			super(parent);
		}

		public synchronized Class<?> defineClass(String fullClassName, byte[] bytes) {
			try {
				return defineClass(fullClassName, bytes, 0, bytes.length);
			} finally {
			}
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return super.loadClass(name, true);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			// TODO Auto-generated method stub
			return super.findClass(name);
		}
	}

	public JdoPersistence(String id) {
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

	<T> Class<? extends Persistable> fetchEnhanced(Class<T> class_) {
//		String enhancedName = class_.getName(); // has to match for transform to work
//		try {
//			Class<?> enhCls = null;
//			try {
//				enhCls = new DynClassloader(cl).loadClass(enhancedName);
//			} catch (ClassNotFoundException e) {
//			}
//			if (null != enhCls && Persistable.class.isAssignableFrom(enhCls)) {
//				return (Class<? extends Persistable>) enhCls;
//			} else {
//
//				DataNucleusClassFileTransformer t = new DataNucleusClassFileTransformer("-api=JDO", null);
//				ProtectionDomain protectionDomain = class_.getProtectionDomain();
//				InputStream is = class_.getClassLoader().getResourceAsStream(class_.getName().replace(".", "/") + ".class");
//				byte[] classfileBuffer = IOUtils.readFully(is, -1, true);
//
//				byte[] bytes = t.transform(new DynClassloader(cl), class_.getName(), null, protectionDomain, classfileBuffer);
//				Class<? extends Persistable> res = (Class<? extends Persistable>) cl.defineClass(enhancedName, bytes);
//
//				for (Field f : class_.getFields()) {
//					if (null != f.getAnnotation(Persistent.class)) {
//						if (null != f.getType().getAnnotation(PersistenceCapable.class)) {
//							fetchEnhanced(f.getType());
//						}
//					} else if (null != f.getAnnotation(Join.class)) {
//
//					}
//				}
//
//				return (Class<? extends Persistable>) new DynClassloader(cl).loadClass(enhancedName);
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
	void copyOutOf(Class<?> srcType, Object src, Class<?> tgtType, Object tgt)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException {
		for (Field f : srcType.getFields()) {
			if (null != f.getAnnotation(Persistent.class)) {
				Field tgtField = tgtType.getField(f.getName());
				if (Persistable.class.isAssignableFrom(f.getType())) {
					Object v = tgtField.getType().newInstance();
					copyOutOf(f.getType(), f.get(src), tgtField.getType(), v);
					tgtField.set(tgt, v);
				} else if (List.class.isAssignableFrom(f.getType()) && this.isPersistentCollection(f.getGenericType())) {
					Class<?> tgtCollObjType = (Class<?>)((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];
					List<?> srcColl = (List<?>) f.get(src);
					List<?> tgtColl = new AbstractList<Object>() {
						@Override
						public Object get(int index) {
							try {
								Object srcCollObj = srcColl.get(index);
								Object tgtCollObj = tgtCollObjType.newInstance();
								copyInto(srcCollObj.getClass(), srcCollObj, tgtCollObjType, tgtCollObj);
								return tgtCollObj;
							} catch (Exception ex) {
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
	void copyInto(Class<?> srcType, Object src, Class<?> tgtType, Object tgt)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException {
		for (Field f : srcType.getFields()) {
			if (null != f.getAnnotation(Persistent.class)) {
				Field tgtField = tgtType.getField(f.getName());
				if (Persistable.class.isAssignableFrom(tgtField.getType())) {
					Object v = tgtField.getType().newInstance();
					copyInto(f.getType(), f.get(src), tgtField.getType(), v);
					tgtField.set(tgt, v);
				} else if (List.class.isAssignableFrom(tgtField.getType()) && this.isPersistentCollection(tgtField.getGenericType())) {
					Class<?> tgtCollObjType = (Class<?>)((ParameterizedType)tgtField.getGenericType()).getActualTypeArguments()[0];
					List<?> srcColl = (List<?>) f.get(src);
					List<?> tgtColl = new AbstractList<Object>() {
						@Override
						public Object get(int index) {
							try {
								Object srcCollObj = srcColl.get(index);
								Object tgtCollObj = tgtCollObjType.newInstance();
								copyInto(srcCollObj.getClass(), srcCollObj, tgtCollObjType, tgtCollObj);
								return tgtCollObj;
							} catch (Exception ex) {
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
				//don't store
			}
		}
	}

	boolean isPersistentCollection(Type type) {
		ParameterizedType pt = (ParameterizedType) type;
		for (Type t : pt.getActualTypeArguments()) {
			boolean b = Persistable.class.isAssignableFrom((Class<?>) t);
			if (b) {
				// iterate
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public void connect(Map<String, Object> properties) {
		PersistenceManagerFactory factory = JDOHelper.getPersistenceManagerFactory(properties);
		this.manager = factory.getPersistenceManager();
	}

	@Override
	public <T> void store(IPersistenceTransaction transaction,PersistentItemLocation location, T item, Class<T> itemType) throws PersistentStoreException {
		Transaction tx = this.manager.currentTransaction();
		try {
			Object toPersist = null;
			if (item instanceof Persistable) {
				toPersist = item;
			} else {
				Class<? extends Persistable> enhancedType = fetchEnhanced(itemType);
				Persistable enhancedObj = enhancedType.newInstance();
				copyInto(itemType, item, enhancedType, enhancedObj);
				toPersist = enhancedObj;
			}

			tx.begin();

			this.manager.makePersistent(toPersist);

			tx.commit();
		} catch (Throwable ex) {
			logger.log(LogLevel.ERROR, "Failed to store persistent item", ex);
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
		}
	}

	@Override
	public <T> T retrieve(IPersistenceTransaction transaction,PersistentItemLocation location, Class<T> itemType) throws PersistentStoreException {
		try {
			Class<? extends Persistable> enhancedType = fetchEnhanced(itemType);
			Query<? extends Persistable> query = this.manager.newQuery(enhancedType, location.asPrimitive());
			Collection<T> res = (Collection<T>) query.execute();
			if (res.isEmpty()) {
				return null;
			} else {
				Object o = res.iterator().next();
				if (itemType.isInstance(o)) {
					return (T) o;
				} else {
					T item = itemType.newInstance();
					copyOutOf(enhancedType, o, itemType, item);
					return item;
				}
			}
		} catch (Exception ex) {
			throw new PersistentStoreException("", ex);
		}
	}

	@Override
	public <T> Set<T> retrieve(IPersistenceTransaction transaction,PersistentItemLocation location, Class<T> itemType, Map<String, Object> filter) throws PersistentStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Set<T> retrieveAll(IPersistenceTransaction transaction,Class<T> itemType) {
		Query query = this.manager.newQuery(itemType);
		Collection<T> res = (Collection<T>) query.execute();
		return new HashSet<>(res);
	}

	// ---------- Ports ---------
	@PortInstance(provides = { IPersistentStore.class }, requires = {})
	IPort portPersist;

	public IPort portPersist() {
		return this.portPersist;
	}

	@Override
	public IPersistenceTransaction  startTransaction() {
		return null;
	}

	@Override
	public void commitTransaction(IPersistenceTransaction transaction) {
		// TODO Auto-generated method stub
		
	}

}
