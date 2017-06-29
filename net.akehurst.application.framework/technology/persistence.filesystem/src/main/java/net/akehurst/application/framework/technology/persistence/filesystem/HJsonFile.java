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
import net.akehurst.application.framework.technology.interfaceFilesystem.FilesystemException;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFile;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFilesystem;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemQuery;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;

@SimpleObject
public class HJsonFile implements IService, IIdentifiableObject, IPersistentStore {

	@ServiceReference
	IApplicationFramework af;

	@ServiceReference
	IFilesystem fs;

	@Override
	public Object createReference(final String locationId) {
		return this;
	}

	public HJsonFile(final String afId) {
		this.afId = afId;
	}

	String afId;

	@Override
	public String afId() {
		return this.afId;
	}

	JsonValue json_cache;

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

	private <T> T convertJsonTo(final JsonValue value, final Class<T> itemType) throws PersistentStoreException {
		try {
			if (null == value) {
				return null;
			} else if (value.isString()) {
				final T t = this.af.createDatatype(itemType, value.asString());
				return t;
			} else if (value.isNumber()) {
				final T t = this.af.createDatatype(itemType, value.asDouble());
				return t;
			} else if (value.isBoolean()) {
				final T t = this.af.createDatatype(itemType, value.asBoolean());
				return t;
			} else if (value.isArray()) {
				if (List.class.isAssignableFrom(itemType)) {
					final List list = new ArrayList<>();
					for (final JsonValue av : value.asArray()) {
						final Object o = this.convertJsonToJava(av);
						list.add(o);
					}
					return (T) list;
				} else {
					throw new PersistentStoreException("Cannot convert JSON Array to List.", null);
				}
			} else if (value.isObject()) {
				if (Map.class.isAssignableFrom(itemType)) {
					final Map<String, Object> map = new HashMap<>();
					for (final String k : value.asObject().names()) {
						final JsonValue jv = value.asObject().get(k);
						final Object v = this.convertJsonToJava(jv);
						map.put(k, v);
					}
					return (T) map;
				} else {
					throw new PersistentStoreException("Cannot convert JSON Object to Map.", null);
				}
			} else {
				throw new PersistentStoreException("Unknown JSON type.", null);
			}
		} catch (final ApplicationFrameworkException e) {
			throw new PersistentStoreException(e.getMessage(), e);
		}
	}

	private Object convertJsonToJava(final JsonValue value) throws PersistentStoreException {
		if (null == value) {
			return null;
		} else if (value.isString()) {
			return value.asString();
		} else if (value.isNumber()) {
			return value.asDouble();
		} else if (value.isBoolean()) {
			return value.asBoolean();
		} else if (value.isArray()) {
			final List<Object> list = new ArrayList<>();
			for (final JsonValue av : value.asArray()) {
				final Object o = this.convertJsonToJava(av);
				list.add(o);
			}
			return list;
		} else if (value.isObject()) {
			final Map<String, Object> map = new HashMap<>();
			for (final String k : value.asObject().names()) {
				final JsonValue jv = value.asObject().get(k);
				final Object v = this.convertJsonToJava(jv);
				map.put(k, v);
			}
			return map;
		} else {
			throw new PersistentStoreException("Unknown JSON type.", null);
		}
	}

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
