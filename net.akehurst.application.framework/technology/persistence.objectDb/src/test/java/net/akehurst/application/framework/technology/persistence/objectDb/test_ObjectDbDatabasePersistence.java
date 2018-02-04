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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.annotations.instance.ServiceInstance;
import net.akehurst.application.framework.realisation.ApplicationFramework;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemQuery;

public class test_ObjectDbDatabasePersistence {
    IApplicationFramework os() {
        return new ApplicationFramework("os", "os");
    }

    @Test
    public void connect() {
        try {
            IApplicationFramework os = os();
            ObjectDbDatabasePersistence sut = os.createComponent(ObjectDbDatabasePersistence.class, "test");

            Map<String, Object> props = new HashMap<>();
            // sut.portPersist().getProvided(IPersistentStore.class).connect(props);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void store_Person() {
        try {
            IApplicationFramework os = os();
            ObjectDbDatabasePersistence sut = os.createComponent(ObjectDbDatabasePersistence.class, "test");

            Map<String, Object> props = new HashMap<>();
            // sut.portPersist().getProvided(IPersistentStore.class).connect(props);
            //
            // PersistentItemLocation location = new PersistentItemLocation("");
            // Person item = new Person();
            // item.name = "Test";
            // IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();
            // sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, item, Person.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void store_Contacts() {
        try {
            IApplicationFramework os = os();
            ObjectDbDatabasePersistence sut = os.createComponent(ObjectDbDatabasePersistence.class, "test");

            Map<String, Object> props = new HashMap<>();
            // sut.portPersist().getProvided(IPersistentStore.class).connect(props);
            //
            // PersistentItemLocation location = new PersistentItemLocation("");
            //
            // Person p = new Person();
            // p.name = "Owner";
            //
            // Person p1 = new Person();
            // p1.name = "Test2";
            //
            // Contacts contacts = new Contacts();
            // contacts.id = "myContacts";
            // contacts.owner = p;
            // contacts.people.add(p1);
            // IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();
            //
            // sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, contacts, Contacts.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void retrieve_Person() {
        try {
            IApplicationFramework os = os();
            ObjectDbDatabasePersistence sut = os.createComponent(ObjectDbDatabasePersistence.class, "test");

            Map<String, Object> props = new HashMap<>();
            // sut.portPersist().getProvided(IPersistentStore.class).connect(props);
            //
            // PersistentItemLocation location = new PersistentItemLocation("Test");
            // Person item = new Person();
            // item.name = "Test";
            // IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();
            //
            // sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, item, Person.class);
            //
            // Object o = sut.portPersist().getProvided(IPersistentStore.class).retrieve(transaction,location, Person.class);
            //
            // Person p = (Person)o;
            // Assert.assertNotNull(p);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void retrieveAll_Person() {
        try {
            IApplicationFramework os = os();
            ObjectDbDatabasePersistence sut = os.createComponent(ObjectDbDatabasePersistence.class, "test");

            Map<String, Object> props = new HashMap<>();
            // sut.portPersist().getProvided(IPersistentStore.class).connect(props);
            //
            // PersistentItemLocation location = new PersistentItemLocation("Test");
            // Person item1 = new Person();
            // item1.name = "Test1";
            // Person item2 = new Person();
            // item2.name = "Test2";
            // IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();
            //
            // sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, item1, Person.class);
            // sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, item2, Person.class);
            //
            // Set<Person> set = sut.portPersist().getProvided(IPersistentStore.class).retrieveAll(transaction,Person.class);
            //
            // Assert.assertTrue(set.size() > 0);
            // for(Person p: set) {
            // System.out.println(p.name);
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void retrieve_Contacts() {
        try {
            IApplicationFramework os = os();
            ObjectDbDatabasePersistence sut = os.createComponent(ObjectDbDatabasePersistence.class, "test");

            Map<String, Object> props = new HashMap<>();
            // sut.portPersist().getProvided(IPersistentStore.class).connect(props);
            //
            // PersistentItemLocation location = new PersistentItemLocation("myContacts");
            //
            // Person p = new Person();
            // p.name = "Owner";
            //
            // Person p1 = new Person();
            // p1.name = "Test2";
            //
            // Contacts contacts = new Contacts();
            // contacts.id = "myContacts";
            // contacts.owner = p;
            // contacts.people.add(p1);
            // IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();
            //
            // sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, contacts, Contacts.class);
            //
            //
            // Object o = sut.portPersist().getProvided(IPersistentStore.class).retrieve(transaction,location, Contacts.class);
            //
            // Contacts c = (Contacts)o;
            // Assert.assertNotNull(c);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
