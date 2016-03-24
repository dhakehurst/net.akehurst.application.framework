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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.os.AbstractComponent;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
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

	EntityManagerFactory emf;

	@Override
	public void afRun() {

	}

	// --------- IPersistentStore ---------

	@Override
	public void connect(Map<String, Object> properties) {
		try {
			
			this.emf = Persistence.createEntityManagerFactory((String)properties.get("persistenceUnitName"), properties);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	};

	@Override
	public <T> void store(IPersistenceTransaction transaction, PersistentItemLocation location, T item, Class<T> itemType) throws PersistentStoreException {
		try {
			JpaPersistenceTransaction trans = (JpaPersistenceTransaction)transaction;
			trans.em.persist(item);
		} catch (Exception ex) {
			throw new PersistentStoreException("Failed to store item",ex);
		}
	}
	
	@Override
	public IPersistenceTransaction startTransaction() {
		EntityManager em = emf.createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		return new JpaPersistenceTransaction(em);
	}
	
	@Override
	public void commitTransaction(IPersistenceTransaction transaction) {
		JpaPersistenceTransaction trans = (JpaPersistenceTransaction)transaction;
		trans.em.getTransaction().commit();
		trans.em.close();
		trans.em = null;
	}
	
	@Override
	public <T> T retrieve(IPersistenceTransaction transaction, PersistentItemLocation location, Class<T> itemType) {
		try {
			JpaPersistenceTransaction trans = (JpaPersistenceTransaction)transaction;
			T t = trans.em.find(itemType, location.asPrimitive());
			return t;
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
		return null;
	}

	@Override
	public <T> Set<T> retrieve(IPersistenceTransaction transaction, PersistentItemLocation location, Class<T> itemType, Map<String, Object> filter) {
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
		JpaPersistenceTransaction trans = (JpaPersistenceTransaction)transaction;
		TypedQuery<T> q = trans.em.createQuery(qs, itemType);
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
	public <T> Set<T> retrieveAll(IPersistenceTransaction transaction, Class<T> itemType) {
		String qs = "SELECT x FROM " + itemType.getSimpleName()+" x";
		JpaPersistenceTransaction trans = (JpaPersistenceTransaction)transaction;
		TypedQuery<T> q = trans.em.createQuery(qs, itemType);
		List<T> res = q.getResultList();
		return new HashSet<>((Collection<T>) res);
	}

	// ---------- Ports ---------
	@PortInstance(provides = { IPersistentStore.class }, requires = {})
	IPort portPersist;

	public IPort portPersist() {
		return this.portPersist;
	}
}
