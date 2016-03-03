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
import net.akehurst.application.framework.os.IOperatingSystem;
import net.akehurst.application.framework.os.annotations.ServiceReference;
import net.akehurst.application.framework.technology.interfaceFilesystem.FilesystemException;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFile;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFilesystem;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemLocation;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;

public class HJsonFile implements IIdentifiableObject, IPersistentStore {

	public HJsonFile(String id) {
		this.id = id;
	}

	String id;

	public String afId() {
		return id;
	}

	@ServiceReference
	IOperatingSystem os;

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
	public <T> void store(PersistentItemLocation location, T item, Class<T> itemType) throws PersistentStoreException {

	}

	@Override
	public <T> T retrieve(PersistentItemLocation location, Class<T> itemType) throws PersistentStoreException {
		try {
			JsonValue value = this.fetchJson(this.getJson(), location.asPrimitive());
			if (null == value) {
				return null;
			} else {
				T t = os.createDatatype(itemType, value);
				return t;
			}
		} catch (Exception ex) {
			throw new PersistentStoreException("Failed to retrieve item from location " + location, ex);
		}
	}

	@Override
	public <T> Set<T> retrieve(PersistentItemLocation location, Class<T> itemType, Map<String, Object> filter) throws PersistentStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Set<T> retrieveAll(Class<T> itemType) {
		// TODO Auto-generated method stub
		return null;
	}
}
