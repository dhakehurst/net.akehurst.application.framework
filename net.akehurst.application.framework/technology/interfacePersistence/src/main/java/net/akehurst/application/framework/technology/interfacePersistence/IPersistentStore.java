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
package net.akehurst.application.framework.technology.interfacePersistence;

import java.util.Map;
import java.util.Set;

public interface IPersistentStore {

	void connect(Map<String,Object> properties);
	
	/**
	 * 
	 * @param location  location of the item to store
	 * @param item  the item to store, must be a DataType
	 * @param itemType (optional) class that defines the datatype of the item 
	 */
	<T> void store(PersistentItemLocation location, T item, Class<T> itemType) throws PersistentStoreException;

	
	/**
	 * 
	 * @param location  location of the item to retrieve
	 * @param itemType  (optional) class that defines the datatype of the item 
	 * @return
	 * @throws PersistentStoreException 
	 */
	<T> T retrieve(PersistentItemLocation location, Class<T> itemType) throws PersistentStoreException;
	
	/**
	 * 
	 * @param location  location of the items to retrieve
	 * @param itemType  (optional) class that defines the datatype of the item 
	 * @param filter filters out objects with the give property-value pairs
	 * @return
	 */
	<T> Set<T> retrieve(PersistentItemLocation location, Class<T> itemType, Map<String, Object> filter) throws PersistentStoreException;
	
	
	<T> Set<T> retrieveAll(Class<T> itemType);
}
