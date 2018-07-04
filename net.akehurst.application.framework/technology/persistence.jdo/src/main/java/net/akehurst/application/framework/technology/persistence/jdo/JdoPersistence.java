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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.commons.io.IOUtils;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.enhancer.DataNucleusClassFileTransformer;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.realisation.ComponentAbstract;
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
 * It seems that using the @Persistent annotation on fields does not result in a working situation use getters and setters for your @PersistenceCapable classes
 *
 * @author akehurst
 *
 */
public class JdoPersistence extends ComponentAbstract implements IPersistentStore {

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

    @ServiceReference
    private ILogger logger;

    public JdoPersistence(final String id) {
        super(id);
        this.cl = new DynClassloader(this.getClass().getClassLoader());
    }

    private PersistenceManagerFactory factory;

    private final DynClassloader cl;

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
                this.logger.log(LogLevel.WARN, "Runtime Enhancement of JDO Class: " + enhancedName);

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

    private Field getIdentityField(final Class<?> class_) throws PersistentStoreException {
        for (final Field f : class_.getFields()) {
            final PrimaryKey pk = f.getAnnotation(PrimaryKey.class);
            if (null != pk) {
                return f;
            } else {
                // iterate
            }
        }
        return null;// throw new PersistentStoreException("all classes must have a PrimaryKey field defined", null);
    }

    private Method getIdentityMethod(final Class<?> class_) throws PersistentStoreException {
        for (final Method m : class_.getMethods()) {
            final PrimaryKey pk = m.getAnnotation(PrimaryKey.class);
            if (null != pk) {
                return m;
            } else {
                // iterate
            }
        }
        return null;// throw new PersistentStoreException("all classes must have a PrimaryKey field defined", null);
    }

    Persistable fetch(final JdoTransaction tx, final String filter, final Class<? extends Persistable> type) {
        final Query<? extends Persistable> q = tx.manager.newQuery(type, filter);
        q.compile();
        final List<? extends Persistable> res = q.executeList();
        if (res.isEmpty()) {
            return null;
        } else {
            // TODO: should also check for more than 1
            return res.get(0);
        }
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
     * @throws PersistentStoreException
     */
    Persistable convertToEnhanced(final JdoTransaction tx, final Class<?> srcType, final Object src, final Class<? extends Persistable> tgtType)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException, NoSuchMethodException,
            InvocationTargetException, PersistentStoreException {
        Persistable tgt = null;
        if (src instanceof Persistable) {
            tgt = (Persistable) src;
        } else {
            // try and find already persisted object with the same id
            Object id = null;
            final Field idField = this.getIdentityField(srcType);
            String idName = null;
            if (null != idField) {
                id = idField.get(src);
                idName = idField.getName();
            } else {
                final Method idMethod = this.getIdentityMethod(srcType);
                if (null == idMethod) {
                    throw new PersistentStoreException("all classes must have a PrimaryKey field or method defined to use the runtime enahncement", null);
                }
                id = idMethod.invoke(src);
                idName = idMethod.getName().substring(3, 4).toLowerCase() + idMethod.getName().substring(4);
            }
            final String filter = idName + " == '" + id + "'";
            final Persistable p = this.fetch(tx, filter, tgtType);
            if (null != p) {
                tgt = p;
            } else {
                // create new instance and copy
                tgt = tx.manager.newInstance(tgtType);
                // copy fields
                for (final Field tgtField : tgtType.getFields()) {
                    if (null != tgtField.getAnnotation(Persistent.class)) {
                        final Field srcField = srcType.getField(tgtField.getName());
                        if (Persistable.class.isAssignableFrom(tgtField.getType())) {
                            final Object srcFieldObj = srcField.get(src);
                            if (null != srcFieldObj) {
                                final Persistable tgtFieldObj = this.convertToEnhanced(tx, srcField.getType(), srcFieldObj,
                                        (Class<? extends Persistable>) tgtField.getType());
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
                                        final Object tgtCollObj = JdoPersistence.this.convertToEnhanced(tx, srcCollObj.getClass(), srcCollObj,
                                                (Class<? extends Persistable>) tgtCollObjType);
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
                                        final Object srcCollObj = JdoPersistence.this.convertFromEnhanced(tx, element.getClass(), element, srcCollObjType);
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
                // copy methods
                for (final Method tgtMethod : tgtType.getMethods()) {
                    if (null != tgtMethod.getAnnotation(Persistent.class)) {
                        final Method srcMethod = srcType.getMethod(tgtMethod.getName());
                        final Method tgtSetMethod = tgtType.getMethod(tgtMethod.getName().replaceFirst("get", "set"), tgtMethod.getReturnType());
                        if (Persistable.class.isAssignableFrom(tgtMethod.getReturnType())) {
                            final Object srcMethodObj = srcMethod.invoke(src);
                            if (null != srcMethodObj) {
                                final Persistable tgtMethodObj = this.convertToEnhanced(tx, srcMethod.getReturnType(), srcMethodObj,
                                        (Class<? extends Persistable>) tgtMethod.getReturnType());

                                tgtSetMethod.invoke(tgt, tgtMethodObj);
                            }
                        } else if (List.class.isAssignableFrom(tgtMethod.getReturnType()) && this.isPersistentCollection(tgtMethod.getGenericReturnType())) {
                            final Class<?> srcCollObjType = (Class<?>) ((ParameterizedType) srcMethod.getGenericReturnType()).getActualTypeArguments()[0];
                            final Class<?> tgtCollObjType = (Class<?>) ((ParameterizedType) tgtMethod.getGenericReturnType()).getActualTypeArguments()[0];
                            final List<Object> srcColl = (List<Object>) srcMethod.invoke(src);
                            // TODO: not sure why srccoll is null here ? something to do with the jdo mapping
                            final List<Object> tgtColl = null == srcColl ? null : new AbstractList<Object>() {
                                @Override
                                public Object get(final int index) {
                                    try {
                                        final Object srcCollObj = srcColl.get(index);
                                        final Object tgtCollObj = JdoPersistence.this.convertToEnhanced(tx, srcCollObj.getClass(), srcCollObj,
                                                (Class<? extends Persistable>) tgtCollObjType);
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
                                        final Object srcCollObj = JdoPersistence.this.convertFromEnhanced(tx, element.getClass(), element, srcCollObjType);
                                        srcColl.add(srcCollObj);
                                    } catch (final Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            };
                            tgtSetMethod.invoke(tgt, tgtColl);
                        } else {
                            tgtSetMethod.invoke(tgt, srcMethod.invoke(src));
                        }
                    } else {
                    }
                }
            }
        }
        return tgt;
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
    Object convertFromEnhanced(final JdoTransaction tx, final Class<?> srcType, final Object src, final Class<?> tgtType) throws IllegalArgumentException,
            IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Object tgt;
        if (Persistable.class.isAssignableFrom(tgtType)) {
            // don't bother converting..tgt is also enhanced
            tgt = src;
        } else {

            tgt = tgtType.newInstance();
            // TODO: methods also!

            for (final Field srcField : srcType.getFields()) {
                if (null != srcField.getAnnotation(Persistent.class)) {
                    final Field tgtField = tgtType.getField(srcField.getName());
                    if (Persistable.class.isAssignableFrom(srcField.getType())) {
                        final Object srcFieldObj = srcField.get(src);
                        if (null != srcFieldObj) {
                            final Object tgtFieldObj = this.convertFromEnhanced(tx, srcField.getType(), srcFieldObj, tgtField.getType());
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
                                    final Object tgtCollObj = JdoPersistence.this.convertFromEnhanced(tx, srcCollObj.getClass(), srcCollObj, tgtCollObjType);
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
                                    final Persistable srcCollObj = JdoPersistence.this.convertToEnhanced(tx, element.getClass(), element,
                                            (Class<? extends Persistable>) srcCollObjType);
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
            // copy methods
            for (final Method srcMethod : srcType.getMethods()) {
                if (null != srcMethod.getAnnotation(Persistent.class)) {
                    final Method tgtMethod = tgtType.getMethod(srcMethod.getName());
                    final Method tgtSetMethod = tgtType.getMethod(tgtMethod.getName().replaceFirst("get", "set"), tgtMethod.getReturnType());
                    if (Persistable.class.isAssignableFrom(srcMethod.getReturnType())) {
                        final Object srcMethodObj = srcMethod.invoke(src);
                        if (null != srcMethodObj) {
                            final Object tgtMethodObj = this.convertFromEnhanced(tx, srcMethod.getReturnType(), srcMethodObj, tgtMethod.getReturnType());
                            tgtSetMethod.invoke(tgt, tgtMethodObj);
                        }
                    } else if (List.class.isAssignableFrom(srcMethod.getReturnType()) && this.isPersistentCollection(srcMethod.getGenericReturnType())) {
                        final Class<?> srcCollObjType = (Class<?>) ((ParameterizedType) srcMethod.getGenericReturnType()).getActualTypeArguments()[0];
                        final Class<?> tgtCollObjType = (Class<?>) ((ParameterizedType) tgtMethod.getGenericReturnType()).getActualTypeArguments()[0];
                        final List<Object> srcColl = (List<Object>) srcMethod.invoke(src);
                        // TODO: not sure why srccoll is null here ? something to do with the jdo mapping
                        final List<Object> tgtColl = null == srcColl ? null : new AbstractList<Object>() {
                            @Override
                            public Object get(final int index) {
                                try {
                                    final Object srcCollObj = srcColl.get(index);
                                    final Object tgtCollObj = JdoPersistence.this.convertFromEnhanced(tx, srcCollObj.getClass(), srcCollObj, tgtCollObjType);
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
                                    final Persistable srcCollObj = JdoPersistence.this.convertToEnhanced(tx, element.getClass(), element,
                                            (Class<? extends Persistable>) srcCollObjType);
                                    srcColl.add(srcCollObj);
                                } catch (final Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        };
                        tgtSetMethod.invoke(tgt, tgtColl);
                    } else {
                        tgtSetMethod.invoke(tgt, srcMethod.invoke(src));
                    }
                } else {
                }
            }
        }
        return tgt;
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

    private <T> List<? extends Persistable> execute(final IPersistenceTransaction transaction, final Class<? extends Persistable> enhancedType,
            final Map<String, Object> filter, final long rangeFrom, final long rangeTo) {
        final Map<String, Object> params = new HashMap<>();
        final List<String> filterValues = new ArrayList<>();
        for (final Map.Entry<String, Object> me : filter.entrySet()) {
            final String n = me.getKey();
            final String pn = n.replace('.', '_');
            filterValues.add(n + " == :" + pn);
            params.put(pn, me.getValue());
        }
        String filterString = "";
        if (filterValues.isEmpty()) {
        } else {
            filterString = filterValues.get(0);
        }
        for (int i = 1; i < filterValues.size(); ++i) {
            filterString += " && " + filterValues.get(i);
        }
        String queryString = "SELECT FROM " + enhancedType.getName();
        if (filterString.isEmpty()) {
            // do nothing
        } else {
            queryString += " WHERE " + filterString;
        }
        final Query<? extends Persistable> jdoquery = (Query<? extends Persistable>) ((JdoTransaction) transaction).newQuery("javax.jdo.query.JDOQL",
                queryString);
        jdoquery.setRange(rangeFrom, rangeTo);

        final List<? extends Persistable> res = (List<? extends Persistable>) jdoquery.executeWithMap(params);

        this.logger.log(LogLevel.DEBUG, "Query %s returned %d items ", queryString, res.size());

        return res;
    }

    @Override
    public void connect(final Map<String, Object> properties) {
        this.factory = JDOHelper.getPersistenceManagerFactory(properties);
    }

    @Override
    public void disconnect() {
        this.factory.close();
    }

    @Override
    public IPersistenceTransaction startTransaction() {
        final PersistenceManager manager = this.factory.getPersistenceManager();
        final JdoTransaction tx = new JdoTransaction(manager);
        tx.begin();
        return tx;
    }

    @Override
    public void commitTransaction(final IPersistenceTransaction transaction) {
        final JdoTransaction tx = (JdoTransaction) transaction;
        tx.commit();
        tx.manager.close();
    }

    @Override
    public void rollbackTransaction(final IPersistenceTransaction transaction) {
        final JdoTransaction tx = (JdoTransaction) transaction;
        tx.rollback();
        tx.manager.close();
    }

    @Override
    public <T> void store(final IPersistenceTransaction transaction, final T item, final Class<T> itemType) throws PersistentStoreException {
        try {
            final JdoTransaction tx = (JdoTransaction) transaction;
            Object toPersist = null;
            if (item instanceof Persistable) {
                toPersist = item;
            } else {
                final Class<? extends Persistable> enhancedType = this.fetchEnhanced(itemType);
                final Persistable enhancedObj = this.convertToEnhanced(tx, itemType, item, enhancedType);
                toPersist = enhancedObj;
            }

            ((JdoTransaction) transaction).makePersistent((Persistable) toPersist);
            this.logger.log(LogLevel.DEBUG, "Item has been made persistent %s", item.toString());

        } catch (final Throwable ex) {
            this.logger.log(LogLevel.ERROR, "Failed to store persistent item", ex);
        }
    }

    @Override
    public <T> void remove(final IPersistenceTransaction transaction, final Class<T> itemType, final Map<String, Object> filter)
            throws PersistentStoreException {
        try {
            final Class<? extends Persistable> enhancedType = this.fetchEnhanced(itemType);
            final List<? extends Persistable> res = this.execute(transaction, enhancedType, filter, 0, Integer.MAX_VALUE);
            if (res.isEmpty()) {
                this.logger.log(LogLevel.WARN, "Nothing to remove for type %s with filter %s", itemType.getSimpleName(), filter);
            } else {
                for (final Persistable jdoObj : res) {
                    ((JdoTransaction) transaction).deletePersistent(jdoObj);
                }
            }
        } catch (final Throwable ex) {
            this.logger.log(LogLevel.ERROR, "Failed to remove persistent item", ex);
        }
    }

    @Override
    public <T> List<T> retrieve(final IPersistenceTransaction transaction, final PersistentItemQuery query, final Map<String, Object> params)
            throws PersistentStoreException {
        try {
            final JdoTransaction tx = (JdoTransaction) transaction;
            final Query<?> jdoquery = ((JdoTransaction) transaction).newQuery(query.getLanguage(), query.getValue());
            final Object res = jdoquery.executeWithMap(params);
            if (null == res) {
                return null;
            } else if (res instanceof List) {
                final List<T> list = (List<T>) res;
                if (list.isEmpty()) {
                    return list;
                } else {
                    final List<T> result = new ArrayList<>();
                    for (final Object o : list) {
                        if (o instanceof Object[]) {
                            result.add((T) Arrays.asList((Object[]) o));
                        } else {
                            result.add((T) o);
                        }

                    }
                    return result;
                }
            } else {
                return Arrays.asList((T) res);
            }

        } catch (final Exception ex) {
            throw new PersistentStoreException(ex.getMessage(), ex);
        }
    }

    @Override
    public <T> Set<T> retrieve(final IPersistenceTransaction transaction, final Type itemType, final Map<String, Object> filter)
            throws PersistentStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Set<T> retrieve(final IPersistenceTransaction transaction, final Class<T> itemType, final Map<String, Object> filter)
            throws PersistentStoreException {
        try {
            final Class<? extends Persistable> enhancedType = this.fetchEnhanced(itemType);
            // Postgres doen't support long values for the limit!
            final List<? extends Persistable> res = this.execute(transaction, enhancedType, filter, 0, Integer.MAX_VALUE);
            if (res.isEmpty()) {
                return new HashSet<>();
            } else {
                final Set<T> result = res.stream().map((el) -> {
                    try {
                        final T item = (T) this.convertFromEnhanced((JdoTransaction) transaction, enhancedType, el, itemType);
                        return item;
                    } catch (final Exception ex) {
                        throw new RuntimeException("Error mapping JDO enhanced object to un-enhanced", ex);
                    }
                }).collect(Collectors.toSet());
                return result;
            }
        } catch (final Exception ex) {
            throw new PersistentStoreException("", ex);
        }
    }

    @Override
    public <T> Set<T> retrieve(final IPersistenceTransaction transaction, final Class<T> itemType, final Map<String, Object> filter, final long rangeFrom,
            final long rangeTo) throws PersistentStoreException {
        try {
            final JdoTransaction tx = (JdoTransaction) transaction;
            final Class<? extends Persistable> enhancedType = this.fetchEnhanced(itemType);
            final List<? extends Persistable> res = this.execute(transaction, enhancedType, filter, rangeFrom, rangeTo);
            if (res.isEmpty()) {
                return new HashSet<>();
            } else {
                final Set<T> result = res.stream().map((el) -> {
                    try {
                        final T item = (T) this.convertFromEnhanced(tx, enhancedType, el, itemType);
                        return item;
                    } catch (final Exception ex) {
                        throw new RuntimeException("Error mapping JDO enhanced object to un-enhanced", ex);
                    }
                }).collect(Collectors.toSet());
                return result;
            }
        } catch (final Exception ex) {
            throw new PersistentStoreException("", ex);
        }
    }

    // ---------- Ports ---------
    @PortInstance
    @PortContract(provides = IPersistentStore.class)
    private IPort portPersist;

    public IPort portPersist() {
        return this.portPersist;
    }

}
