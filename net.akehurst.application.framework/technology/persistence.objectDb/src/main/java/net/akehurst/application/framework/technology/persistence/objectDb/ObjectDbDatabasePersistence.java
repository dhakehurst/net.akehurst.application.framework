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
package net.akehurst.application.framework.technology.persistence.objectDb;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.declaration.Component;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemQuery;

@Component
public class ObjectDbDatabasePersistence extends AbstractComponent implements IPersistentStore {

	public ObjectDbDatabasePersistence(String id) {
		super(id);
	}

	@ConfiguredValue(defaultValue = "db/database.objectdb")
	String databaseFilePath;

	EntityManager entityManager;

	@Override
	public void afRun() {

	}

	// --------- IPersistentStore ---------

	@Override
	public void connect(Map<String, Object> properties) {
		try {
			System.setProperty("objectdb.conf", databaseFilePath+".conf");
			
			EntityManagerFactory emf = Persistence.createEntityManagerFactory(this.databaseFilePath);
			this.entityManager = emf.createEntityManager();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	};

	@Override
	public <T> void store(IPersistenceTransaction transaction,PersistentItemQuery location, T item, Class<T> itemType) {
		try {
			this.entityManager.getTransaction().begin();
			this.entityManager.persist(item);
			this.entityManager.getTransaction().commit();
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}

	@Override
	public <T> T retrieve(IPersistenceTransaction transaction,PersistentItemQuery location, Class<T> itemType) {
		try {
			T t = this.entityManager.find(itemType, location.asPrimitive());
			return t;
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
		return null;
	}

	@Override
	public <T> Set<T> retrieve(IPersistenceTransaction transaction,PersistentItemQuery location, Class<T> itemType, Map<String, Object> filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Set<T> retrieveAll(IPersistenceTransaction transaction,Class<T> itemType) {
		String qs = "SELECT x FROM " + itemType.getSimpleName()+" x";
		TypedQuery<T> q = this.entityManager.createQuery(qs, itemType);
		List<T> res = q.getResultList();
		return new HashSet<>((Collection<T>) res);
	}

	// ---------- Ports ---------
	@PortInstance(provides = { IPersistentStore.class }, requires = {})
	IPort portPersist;

	public IPort portPersist() {
		return this.portPersist;
	}

	@Override
	public IPersistenceTransaction  startTransaction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void commitTransaction(IPersistenceTransaction transaction) {
		// TODO Auto-generated method stub
		
	}
}
