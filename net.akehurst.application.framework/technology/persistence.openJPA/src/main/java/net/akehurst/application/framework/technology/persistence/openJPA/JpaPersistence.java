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
package net.akehurst.application.framework.technology.persistence.openJPA;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.akehurst.application.framework.components.AbstractComponent;
import net.akehurst.application.framework.components.Port;
import net.akehurst.application.framework.os.annotations.ConfiguredValue;
import net.akehurst.application.framework.os.annotations.PortInstance;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemLocation;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;
/**
 * 
 * connection properties requires
 *   persistenceUnitName = ?
 *
 */
public class JpaPersistence extends AbstractComponent implements IPersistentStore {

	public JpaPersistence(String id) {
		super(id);
	}


	EntityManager entityManager;

	@Override
	public void afRun() {

	}

	// --------- IPersistentStore ---------

	@Override
	public void connect(Map<String, Object> properties) {
		try {
			
			EntityManagerFactory emf = Persistence.createEntityManagerFactory((String)properties.get("persistenceUnitName"), properties);
			this.entityManager = emf.createEntityManager();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	};

	@Override
	public <T> void store(PersistentItemLocation location, T item, Class<T> itemType) throws PersistentStoreException {
		try {
			this.entityManager.getTransaction().begin();
			this.entityManager.persist(item);
			this.entityManager.getTransaction().commit();
		} catch (Exception ex) {
			throw new PersistentStoreException("Failed to store item",ex);
		}
	}

	@Override
	public <T> T retrieve(PersistentItemLocation location, Class<T> itemType) {
		try {
			T t = this.entityManager.find(itemType, location.asPrimitive());
			return t;
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
		return null;
	}

	@Override
	public <T> Set<T> retrieve(PersistentItemLocation location, Class<T> itemType, Map<String, Object> filter) {
		String qs = "SELECT item FROM " + itemType.getSimpleName()+" item";
		if (filter.isEmpty()) {
			//do nothing, return all
		} else {
			qs +=" WHERE ";
			ArrayList<Map.Entry<String, Object>> list = new ArrayList<>(filter.entrySet());
			qs += "item."+list.get(0).getKey() + " = " + this.convert(list.get(0).getValue());
			for(int i=1; i<list.size(); ++i) {
				Map.Entry<String, Object> me = list.get(i);
				qs += " AND " + "item."+me.getKey() + " = " + this.convert(me.getValue());
			}
		}
		TypedQuery<T> q = this.entityManager.createQuery(qs, itemType);
		List<T> res = q.getResultList();
		return new HashSet<>((Collection<T>) res);
	}

	String convert(Object value) {
		if (value instanceof String) {
			return "'"+value+"'";
		} else {
			return value.toString();
		}
	}
	
	@Override
	public <T> Set<T> retrieveAll(Class<T> itemType) {
		String qs = "SELECT x FROM " + itemType.getSimpleName()+" x";
		TypedQuery<T> q = this.entityManager.createQuery(qs, itemType);
		List<T> res = q.getResultList();
		return new HashSet<>((Collection<T>) res);
	}

	// ---------- Ports ---------
	@PortInstance(provides = { IPersistentStore.class }, requires = {})
	Port portPersist;

	public Port portPersist() {
		return this.portPersist;
	}
}
