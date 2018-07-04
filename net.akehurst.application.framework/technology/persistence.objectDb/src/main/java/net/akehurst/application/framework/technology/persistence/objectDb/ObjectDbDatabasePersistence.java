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

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.declaration.Component;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.realisation.ComponentAbstract;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemQuery;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;

@Component
public class ObjectDbDatabasePersistence extends ComponentAbstract implements IPersistentStore {

    public ObjectDbDatabasePersistence(final String id) {
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
    public void connect(final Map<String, Object> properties) {
        try {
            System.setProperty("objectdb.conf", this.databaseFilePath + ".conf");

            final EntityManagerFactory emf = Persistence.createEntityManagerFactory(this.databaseFilePath);
            this.entityManager = emf.createEntityManager();

        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    };

    @Override
    public void disconnect() {
        this.entityManager.close();
    }

    @Override
    public IPersistenceTransaction startTransaction() {
        // TODO Auto-generated method stub
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
    public <T> void store(final IPersistenceTransaction transaction, final T item, final Class<T> itemType) {
        try {
            this.entityManager.getTransaction().begin();
            this.entityManager.persist(item);
            this.entityManager.getTransaction().commit();
        } catch (final Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public <T> List<T> retrieve(final IPersistenceTransaction transaction, final PersistentItemQuery query, final Map<String, Object> params) {
        try {
            final Query q = this.entityManager.createQuery(query.getValue());
            final List<?> res = q.getResultList();
            return (List<T>) res;
        } catch (final Exception ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }

    @Override
    public <T> Set<T> retrieve(final IPersistenceTransaction transaction, final Type itemType, final Map<String, Object> filter)
            throws PersistentStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Set<T> retrieve(final IPersistenceTransaction transaction, final Class<T> itemType, final Map<String, Object> filter) {
        final String qs = "SELECT x FROM " + itemType.getSimpleName() + " x";
        final TypedQuery<T> q = this.entityManager.createQuery(qs, itemType);

        final List<T> res = q.getResultList();
        return new HashSet<>(res);
    }

    @Override
    public <T> Set<T> retrieve(final IPersistenceTransaction transaction, final Class<T> itemType, final Map<String, Object> filter, final long rangeFrom,
            final long rangeTo) throws PersistentStoreException {
        final String qs = "SELECT x FROM " + itemType.getSimpleName() + " x";
        final TypedQuery<T> q = this.entityManager.createQuery(qs, itemType);
        q.setFirstResult((int) rangeFrom);
        q.setMaxResults((int) (rangeTo - rangeFrom));
        final List<T> res = q.getResultList();
        return new HashSet<>(res);
    }

    // @Override
    // public <T> Set<T> retrieveAll(final IPersistenceTransaction transaction, final Class<T> itemType, final Map<String, Object> filter) {
    // final String qs = "SELECT x FROM " + itemType.getSimpleName() + " x";
    // final TypedQuery<T> q = this.entityManager.createQuery(qs, itemType);
    // final List<T> res = q.getResultList();
    // return new HashSet<>(res);
    // }

    @Override
    public <T> void remove(final IPersistenceTransaction transaction, final Class<T> itemType, final Map<String, Object> filter)
            throws PersistentStoreException {
        // TODO Auto-generated method stub

    }

    // ---------- Ports ---------
    @PortInstance
    @PortContract(provides = IPersistentStore.class)
    IPort portPersist;

    public IPort portPersist() {
        return this.portPersist;
    }

}
