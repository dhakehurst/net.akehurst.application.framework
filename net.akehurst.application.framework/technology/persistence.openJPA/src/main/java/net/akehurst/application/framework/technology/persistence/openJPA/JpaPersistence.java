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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.realisation.ComponentAbstract;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemQuery;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;

/**
 *
 * connection properties requires persistenceUnitName = ?
 *
 */
public class JpaPersistence extends ComponentAbstract implements IPersistentStore {

    public JpaPersistence(final String id) {
        super(id);
    }

    EntityManagerFactory emf;

    @Override
    public void afRun() {

    }

    // --------- IPersistentStore ---------

    @Override
    public void connect(final Map<String, Object> properties) {
        try {

            this.emf = Persistence.createEntityManagerFactory((String) properties.get("persistenceUnitName"), properties);

        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    };

    @Override
    public void disconnect() {
        this.emf.close();
    }

    @Override
    public IPersistenceTransaction startTransaction() {
        final EntityManager em = this.emf.createEntityManager();
        final EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        return new JpaPersistenceTransaction(em);
    }

    @Override
    public void commitTransaction(final IPersistenceTransaction transaction) {
        final JpaPersistenceTransaction trans = (JpaPersistenceTransaction) transaction;
        trans.em.getTransaction().commit();
        trans.em.close();
        trans.em = null;
    }

    @Override
    public void rollbackTransaction(final IPersistenceTransaction transaction) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> void store(final IPersistenceTransaction transaction, final T item, final Class<T> itemType) throws PersistentStoreException {
        try {
            final JpaPersistenceTransaction trans = (JpaPersistenceTransaction) transaction;
            trans.em.persist(item);
        } catch (final Exception ex) {
            throw new PersistentStoreException("Failed to store item", ex);
        }
    }

    @Override
    public <T> List<T> retrieve(final IPersistenceTransaction transaction, final PersistentItemQuery query, final Map<String, Object> params) {
        try {
            final JpaPersistenceTransaction trans = (JpaPersistenceTransaction) transaction;
            final Query q = trans.em.createQuery(query.getValue());
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
        String qs = "SELECT item FROM " + itemType.getSimpleName() + " item";
        if (filter.isEmpty()) {
            // do nothing, return all
        } else {
            qs += " WHERE ";
            final ArrayList<Map.Entry<String, Object>> list = new ArrayList<>(filter.entrySet());
            qs += "item." + list.get(0).getKey() + " = " + this.convert(list.get(0).getValue());
            for (int i = 1; i < list.size(); ++i) {
                final Map.Entry<String, Object> me = list.get(i);
                qs += " AND " + "item." + me.getKey() + " = " + this.convert(me.getValue());
            }
        }
        final JpaPersistenceTransaction trans = (JpaPersistenceTransaction) transaction;
        final TypedQuery<T> q = trans.em.createQuery(qs, itemType);
        final List<T> res = q.getResultList();
        return new HashSet<>(res);
    }

    String convert(final Object value) {
        if (value instanceof String) {
            return "'" + value + "'";
        } else {
            return value.toString();
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
    // final String qs = "SELECT x FROM " + itemType.getSimpleName() + " x";
    // final JpaPersistenceTransaction trans = (JpaPersistenceTransaction) transaction;
    // final TypedQuery<T> q = trans.em.createQuery(qs, itemType);
    // final List<T> res = q.getResultList();
    // return new HashSet<>(res);
    // }

    @Override
    public <T> void remove(final IPersistenceTransaction transaction, final Class<T> itemType, final Map<String, Object> filter)
            throws PersistentStoreException {
        try {
            String qs = "SELECT item FROM " + itemType.getSimpleName() + " item";
            if (filter.isEmpty()) {
                // do nothing, return all
            } else {
                qs += " WHERE ";
                final ArrayList<Map.Entry<String, Object>> list = new ArrayList<>(filter.entrySet());
                qs += "item." + list.get(0).getKey() + " = " + this.convert(list.get(0).getValue());
                for (int i = 1; i < list.size(); ++i) {
                    final Map.Entry<String, Object> me = list.get(i);
                    qs += " AND " + "item." + me.getKey() + " = " + this.convert(me.getValue());
                }
            }

            final JpaPersistenceTransaction trans = (JpaPersistenceTransaction) transaction;
            final T obj = trans.em.find(itemType, qs);
            trans.em.remove(obj);
        } catch (final Exception ex) {
            throw new PersistentStoreException("Failed to remove item", ex);
        }
    }

    // ---------- Ports ---------
    @PortInstance
    @PortContract(provides = IPersistentStore.class)
    IPort portPersist;

    public IPort portPersist() {
        return this.portPersist;
    }
}
