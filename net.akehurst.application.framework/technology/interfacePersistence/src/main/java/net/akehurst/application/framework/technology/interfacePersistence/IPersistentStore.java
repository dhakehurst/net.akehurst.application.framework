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

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.akehurst.application.framework.common.IIdentifiableObject;

public interface IPersistentStore extends IIdentifiableObject {

	void connect(Map<String, Object> properties);

	void disconnect();

	IPersistenceTransaction startTransaction();

	void commitTransaction(IPersistenceTransaction transaction);

	void rollbackTransaction(IPersistenceTransaction transaction);

	/**
	 *
	 * @param transaction
	 * @param item
	 *            the item to store, must be a DataType
	 * @param itemType
	 *            (optional) class that defines the datatype of the item
	 */
	<T> void store(IPersistenceTransaction transaction, T item, Class<T> itemType) throws PersistentStoreException;

	<T> void remove(IPersistenceTransaction transaction, Class<T> itemType, Map<String, Object> filter) throws PersistentStoreException;

	/**
	 * @param T
	 *            type of the result objects
	 * @param query
	 *            defines the items to retrieve
	 * @return
	 * @throws PersistentStoreException
	 */
	<T> List<T> retrieve(IPersistenceTransaction transaction, PersistentItemQuery query, Map<String, Object> params) throws PersistentStoreException;

	/**
	 *
	 * @param transaction
	 * @param itemType
	 *            class that defines the datatype of the item
	 * @param filter
	 *            filters out objects with the given property-value pairs
	 * @return
	 */
	<T> Set<T> retrieve(IPersistenceTransaction transaction, Class<T> itemType, Map<String, Object> filter) throws PersistentStoreException;

	<T> Set<T> retrieve(IPersistenceTransaction transaction, Class<T> itemType, Map<String, Object> filter, long rangeFrom, long rangeTo)
			throws PersistentStoreException;

	// <T> Set<T> retrieveAll(IPersistenceTransaction transaction, Class<T> itemType, Map<String, Object> filter);

}
