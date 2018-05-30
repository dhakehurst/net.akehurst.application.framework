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
package net.akehurst.application.framework.technology.persistence.filesystem;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hjson.JsonObject;
import org.hjson.JsonValue;

import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.common.IService;
import net.akehurst.application.framework.common.annotations.declaration.SimpleObject;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.realisation.ApplicationFramework;
import net.akehurst.application.framework.technology.interfaceFilesystem.FilesystemException;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFile;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFilesystem;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemQuery;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;
import net.akehurst.holser.reflect.BetterMethodFinder;

@SimpleObject
public class HJsonFile implements IService, IIdentifiableObject, IPersistentStore {

    @ServiceReference
    IApplicationFramework af;

    @ServiceReference
    IFilesystem fs;

    public HJsonFile(final String afId) {
        this.afId = afId;
    }

    private final String afId;
    private JsonValue json_cache;

    @Override
    public String afId() {
        return this.afId;
    }

    @Override
    public Object createReference(final String locationId) {
        return this;
    }

    public IFile getFile() {
        final String fileName = this.afId() + ".hjson";
        final IFile file = this.fs.file(fileName);
        return file;
    }

    JsonValue getJson() throws IOException, FilesystemException {
        if (null == this.json_cache) {
            final IFile file = this.getFile();
            if (file.exists()) {
                this.json_cache = JsonValue.readHjson(file.reader());
            } else {

            }
        }
        return this.json_cache;
    }

    JsonValue fetchJson(final JsonValue json, final String path) {
        if (json instanceof JsonObject) {
            final JsonObject jo = (JsonObject) json;
            final int ix = path.indexOf(".");
            if (-1 == ix) {
                return jo.get(path);
            } else {
                final String hd = path.substring(0, ix);
                final String tl = path.substring(ix + 1);
                return this.fetchJson(jo.get(hd), tl);
            }

        } else {
            return null;
        }
    }

    private <T> T convertJsonTo(final JsonValue value, final Type itemType) throws PersistentStoreException {
        try {
            final Class<?> class_ = ApplicationFramework.getClass(itemType);
            if (null == value) {
                return null;
            } else if (value.isString()) {
                final T t = this.af.createDatatype(itemType, value.asString());
                return t;
            } else if (value.isNumber()) {
                if (itemType == Double.TYPE || itemType == Double.class) {
                    return this.af.createDatatype(itemType, value.asDouble());
                } else if (itemType == Float.TYPE || itemType == Float.class) {
                    return this.af.createDatatype(itemType, value.asFloat());
                } else if (itemType == Integer.TYPE || itemType == Integer.class) {
                    return this.af.createDatatype(itemType, value.asInt());
                } else if (itemType == Long.TYPE || itemType == Long.class) {
                    return this.af.createDatatype(itemType, value.asLong());
                } else if (itemType == Short.TYPE || itemType == Short.class) {
                    return this.af.createDatatype(itemType, (short) value.asInt());
                } else {
                    throw new PersistentStoreException("Cannot convert JSON value to " + itemType, null);
                }
            } else if (value.isBoolean()) {
                final T t = this.af.createDatatype(itemType, value.asBoolean());
                return t;
            } else if (value.isArray()) {
                if (List.class.isAssignableFrom(class_)) {
                    final Type elType = ((ParameterizedType) itemType).getActualTypeArguments()[0];
                    final List list = new ArrayList<>();
                    for (final JsonValue av : value.asArray()) {
                        final Object o = this.convertJsonTo(av, elType);
                        list.add(o);
                    }
                    return (T) list;
                } else {
                    throw new PersistentStoreException("Cannot convert JSON Array to List.", null);
                }
            } else if (value.isObject()) {
                if (Map.class.isAssignableFrom(class_)) {
                    // TODO: support Map as a supertype, i.e. get type arguments from superclass ?
                    final Type keyType = ((ParameterizedType) itemType).getActualTypeArguments()[0];
                    final Type valueType = ((ParameterizedType) itemType).getActualTypeArguments()[1];
                    final Map<Object, Object> map = new HashMap<>();
                    for (final String k : value.asObject().names()) {
                        final Object kv = this.convertJsonTo(JsonValue.valueOf(k), keyType);
                        final JsonValue jv = value.asObject().get(k);
                        final Object vv = this.convertJsonTo(jv, valueType);
                        map.put(kv, vv);
                    }
                    return (T) map;
                } else {
                    try {
                        final Object object = class_.newInstance();
                        value.asObject().names().forEach(propName -> {
                            try {
                                final String getterName = this.getterName(propName);
                                final String setterName = this.setterName(propName);
                                final BetterMethodFinder bmf = new BetterMethodFinder(class_);
                                final Method getter = bmf.findMethod(getterName);
                                final Type genPropType = getter.getGenericReturnType();
                                final Class<?> propType = getter.getReturnType();
                                final Method setter = bmf.findMethod(setterName, propType);
                                final JsonValue jpv = value.asObject().get(propName);
                                final Object propValue = this.convertJsonTo(jpv, genPropType);
                                setter.invoke(object, propValue);
                            } catch (final Exception e) {
                                throw new PersistentStoreException("Cannot find getter/setter for " + propName, e);
                            }
                        });
                        return (T) object;
                    } catch (final Exception e) {
                        throw new PersistentStoreException("Cannot convert JSON Object to " + class_.getSimpleName(), e);
                    }
                }
            } else {
                throw new PersistentStoreException("Unknown JSON type.", null);
            }
        } catch (final ApplicationFrameworkException e) {
            throw new PersistentStoreException(e.getMessage(), e);
        }
    }

    String getterName(final String propName) {
        return "get" + propName.substring(0, 1).toUpperCase() + propName.substring(1);
    }

    String setterName(final String propName) {
        return "set" + propName.substring(0, 1).toUpperCase() + propName.substring(1);
    }

    // private <T> T convertJsonToJava(final JsonValue value, final Type itemType) throws PersistentStoreException {
    // final Class<?> class_ = ApplicationFramework.getClass(itemType);
    // if (null == value) {
    // return null;
    // } else if (value.isString()) {
    // if (itemType == String.class) {
    // return (T) value.asString();
    // } else if (class_.isEnum()) {
    // return (T) Enum.valueOf((Class<? extends Enum>) itemType, value.asString());
    // } else {
    // throw new PersistentStoreException("Cannot convert JSON value to " + itemType, null);
    // }
    // } else if (value.isNumber()) {
    // if (itemType == Double.TYPE || itemType == Double.class) {
    // return (T) Double.valueOf(value.asDouble());
    // } else if (itemType == Float.TYPE || itemType == Float.class) {
    // return (T) Float.valueOf(value.asFloat());
    // } else if (itemType == Integer.TYPE || itemType == Integer.class) {
    // return (T) Integer.valueOf(value.asInt());
    // } else if (itemType == Long.TYPE || itemType == Long.class) {
    // return (T) Long.valueOf(value.asLong());
    // } else if (itemType == Short.TYPE || itemType == Short.class) {
    // return (T) Short.valueOf((short) value.asInt());
    // } else {
    // throw new PersistentStoreException("Cannot convert JSON value to " + itemType, null);
    // }
    // } else if (value.isBoolean()) {
    // return (T) Boolean.valueOf(value.asBoolean());
    // } else if (value.isArray()) {
    // final Type elType = ((ParameterizedType) itemType).getActualTypeArguments()[0];
    // final List<Object> list = new ArrayList<>();
    // for (final JsonValue av : value.asArray()) {
    // final Object o = this.convertJsonToJava(av, elType);
    // list.add(o);
    // }
    // return (T) list;
    // } else if (value.isObject()) {
    // if (Map.class.isAssignableFrom(class_)) {
    // final Type elType = ((ParameterizedType) itemType).getActualTypeArguments()[1];
    // final Map<String, Object> map = new HashMap<>();
    // for (final String k : value.asObject().names()) {
    // final JsonValue jv = value.asObject().get(k);
    // final Object v = this.convertJsonToJava(jv, elType);
    // map.put(k, v);
    // }
    // return (T) map;
    // } else {
    // throw new PersistentStoreException("Cannot convert JSON Object to Map.", null);
    // }
    // } else {
    // throw new PersistentStoreException("Unknown JSON type.", null);
    // }
    // }

    // --------- IPersistentStore ---------
    @Override
    public void connect(final Map<String, Object> properties) {

    };

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    @Override
    public IPersistenceTransaction startTransaction() {
        return null;
    }

    @Override
    public void commitTransaction(final IPersistenceTransaction transaction) {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollbackTransaction(final IPersistenceTransaction transaction) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void store(final IPersistenceTransaction transaction, final T item, final Class<T> itemType) throws PersistentStoreException {

    }

    @Override
    public <T> List<T> retrieve(final IPersistenceTransaction transaction, final PersistentItemQuery query, final Map<String, Object> params)
            throws PersistentStoreException {
        return null;
    }

    @Override
    public <T> void remove(final IPersistenceTransaction transaction, final Class<T> itemType, final Map<String, Object> filter)
            throws PersistentStoreException {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> Set<T> retrieve(final IPersistenceTransaction transaction, final Class<T> itemType, final Map<String, Object> filter)
            throws PersistentStoreException {
        return this.retrieve(transaction, (Type) itemType, filter);
    }

    @Override
    public <T> Set<T> retrieve(final IPersistenceTransaction transaction, final Type itemType, final Map<String, Object> filter)
            throws PersistentStoreException {

        final String path = (String) filter.get("path");
        try {

            final JsonValue value = this.fetchJson(this.getJson(), path);
            final T t = this.convertJsonTo(value, itemType);
            return new HashSet<>(Arrays.asList(t));
        } catch (final Exception ex) {
            throw new PersistentStoreException("Failed to retrieve item from location " + path, ex);
        }
    }

    @Override
    public <T> Set<T> retrieve(final IPersistenceTransaction transaction, final Class<T> itemType, final Map<String, Object> filter, final long rangeFrom,
            final long rangeTo) throws PersistentStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    // @Override
    // public <T> Set<T> retrieveAll(final IPersistenceTransaction transaction, final Class<T> itemType, final Map<String, Object> filter) {
    // // TODO Auto-generated method stub
    // return null;
    // }

}
