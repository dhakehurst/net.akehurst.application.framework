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
import java.util.Map;
import java.util.Set;

import org.hjson.JsonObject;
import org.hjson.JsonValue;

import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.IService;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.technology.interfaceFilesystem.FilesystemException;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFile;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFilesystem;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemLocation;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;

public class HJsonFile implements IService, IIdentifiableObject, IPersistentStore {

	@Override
	public Object createReference(String locationId) {
		return this;
	}
	
	public HJsonFile(String id) {
		this.id = id;
	}

	String id;

	public String afId() {
		return id;
	}

	@ServiceReference
	IApplicationFramework os;

	@ServiceReference
	IFilesystem fs;

	JsonValue json_cache;

	JsonValue getJson() throws IOException, FilesystemException {
		if (null == this.json_cache) {
			IFile file = fs.file(this.afId() + ".hjson");
			if (file.exists()) {
				this.json_cache = JsonValue.readHjson(file.reader());
			} else {

			}
		}
		return this.json_cache;
	}

	JsonValue fetchJson(JsonValue json, String path) {
		if (json instanceof JsonObject) {
			JsonObject jo = (JsonObject) json;
			int ix = path.indexOf(".");
			if (-1 == ix) {
				return jo.get(path);
			} else {
				String hd = path.substring(0, ix);
				String tl = path.substring(ix + 1);
				return this.fetchJson(jo.get(hd), tl);
			}

		} else {
			return null;
		}
	}

	// --------- IPersistentStore ---------
	@Override
	public void connect(Map<String,Object> properties) {
		
	};
	
	
	@Override
	public <T> void store(IPersistenceTransaction transaction,PersistentItemLocation location, T item, Class<T> itemType) throws PersistentStoreException {

	}

	@Override
	public <T> T retrieve(IPersistenceTransaction transaction,PersistentItemLocation location, Class<T> itemType) throws PersistentStoreException {
		try {
			JsonValue value = this.fetchJson(this.getJson(), location.asPrimitive());
			if (null == value) {
				return null;
			} else if (value.isString()){
				T t = os.createDatatype(itemType, value.asString());
				return t;
			} else if (value.isNumber()) {
				T t = os.createDatatype(itemType, value.asDouble());
				return t;
			} else if (value.isBoolean()) {
				T t = os.createDatatype(itemType, value.asBoolean());
				return t;
			} else {
				throw new PersistentStoreException("Unknown JSON type at " + location, null);
			}
		} catch (Exception ex) {
			throw new PersistentStoreException("Failed to retrieve item from location " + location, ex);
		}
	}

	@Override
	public <T> Set<T> retrieve(IPersistenceTransaction transaction,PersistentItemLocation location, Class<T> itemType, Map<String, Object> filter) throws PersistentStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Set<T> retrieveAll(IPersistenceTransaction transaction,Class<T> itemType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPersistenceTransaction startTransaction() {
		return null;
	}

	@Override
	public void commitTransaction(IPersistenceTransaction transaction) {
		// TODO Auto-generated method stub
		
	}
}
