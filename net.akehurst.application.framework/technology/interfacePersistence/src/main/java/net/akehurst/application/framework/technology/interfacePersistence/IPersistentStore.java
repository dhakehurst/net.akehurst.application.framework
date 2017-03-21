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

import net.akehurst.application.framework.common.IIdentifiableObject;

public interface IPersistentStore extends IIdentifiableObject {

	void connect(Map<String, Object> properties);

	IPersistenceTransaction startTransaction();

	void commitTransaction(IPersistenceTransaction transaction);

	void rollbackTransaction(IPersistenceTransaction transaction);

	/**
	 *
	 * @param query
	 *            location of the item to store
	 * @param item
	 *            the item to store, must be a DataType
	 * @param itemType
	 *            (optional) class that defines the datatype of the item
	 */
	<T> void store(IPersistenceTransaction transaction, PersistentItemQuery query, T item, Class<T> itemType) throws PersistentStoreException;

	<T> void remove(IPersistenceTransaction transaction, PersistentItemQuery query, Class<T> itemType) throws PersistentStoreException;

	/**
	 *
	 * @param query
	 *            location of the item to retrieve
	 * @param itemType
	 *            (optional) class that defines the datatype of the item
	 * @return
	 * @throws PersistentStoreException
	 */
	<T> T retrieve(IPersistenceTransaction transaction, PersistentItemQuery query, Class<T> itemType) throws PersistentStoreException;

	/**
	 *
	 * @param query
	 *            location of the items to retrieve
	 * @param itemType
	 *            (optional) class that defines the datatype of the item
	 * @param filter
	 *            filters out objects with the give property-value pairs
	 * @return
	 */
	<T> Set<T> retrieve(IPersistenceTransaction transaction, PersistentItemQuery query, Class<T> itemType, Map<String, Object> filter)
			throws PersistentStoreException;

	<T> Set<T> retrieveAll(IPersistenceTransaction transaction, Class<T> itemType);

}
