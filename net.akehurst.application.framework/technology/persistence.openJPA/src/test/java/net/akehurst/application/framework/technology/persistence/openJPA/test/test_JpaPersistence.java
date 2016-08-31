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
package net.akehurst.application.framework.technology.persistence.openJPA.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.realisation.ApplicationFramework;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemQuery;
import net.akehurst.application.framework.technology.persistence.openJPA.JpaPersistence;
import net.akehurst.application.framework.technology.persistence.openJPA.test.jpa.Contacts;
import net.akehurst.application.framework.technology.persistence.openJPA.test.jpa.Person;

public class test_JpaPersistence {
	@Test
	public void connect() {
		try {
			IApplicationFramework os = new ApplicationFramework("os","os");
			JpaPersistence sut = os.createComponent(JpaPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
//			props.put("javax.jdo.PersistenceManagerFactoryClass", "org.apache.openjpa.persistence.PersistenceProviderImpl ");
			props.put("openjpa.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver");
			props.put("openjpa.ConnectionURL", "jdbc:derby:db/Test;create=true");
			props.put("openjpa.jdbc.SynchronizeMappings","buildSchema(ForeignKeys=true)");
			//sut.portPersist().getProvided(IPersistentStore.class).connect(props);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void store_Person() {
		try {
			IApplicationFramework os = new ApplicationFramework("os","os");
			JpaPersistence sut = os.createComponent(JpaPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			props.put("persistenceUnitName", "openjpa");
//			props.put("javax.jdo.PersistenceManagerFactoryClass", "org.apache.openjpa.persistence.PersistenceProviderImpl ");
//			props.put("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
//			props.put("javax.persistence.jdbc.url", "jdbc:derby:db/Test;create=true");
//			props.put("openjpa.jdbc.SynchronizeMappings","buildSchema(ForeignKeys=true)");
//			props.put("openjpa.Log","DefaultLevel=TRACE,SQL=TRACE");
//			props.put("javax.persistence.schema-generation.database.action","create");
//			props.put("openjpa.DynamicEnhancementAgent", true);
			//sut.portPersist().getProvided(IPersistentStore.class).connect(props);
			
			PersistentItemQuery location = new PersistentItemQuery("");
			Person item = new Person();
			item.setName("Test");

//			Class<?> c = Class.forName("net.akehurst.application.framework.technology.persistence.openJPA.test.Person");
//			Object o = c.newInstance();
//			c.getField("name").set(o, "Test");
			//IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();

			//sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, item, Person.class);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	@Test
	public void store_Contacts() {
		try {
			IApplicationFramework os = new ApplicationFramework("os","os");
			JpaPersistence sut = os.createComponent(JpaPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			//sut.portPersist().getProvided(IPersistentStore.class).connect(props);
			
			PersistentItemQuery location = new PersistentItemQuery("");
			
			Person p = new Person();
			p.setName("Owner");
			
			Person p1 = new Person();
			p1.setName( "Test2");

			Contacts contacts = new Contacts();
			contacts.id = "myContacts";
			contacts.owner = p;
			contacts.people.add(p1);
			//IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();

			//sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, contacts, Contacts.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void retrieve_Person() {
		try {
			IApplicationFramework os = new ApplicationFramework("os","os");
			JpaPersistence sut = os.createComponent(JpaPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			props.put("persistenceUnitName", "openjpa");
			//sut.portPersist().getProvided(IPersistentStore.class).connect(props);
			
			PersistentItemQuery location = new PersistentItemQuery("Test");
			Person item = new Person();
			item.setName("Test");
			//IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();

			//sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, item, Person.class);

			//Object o = sut.portPersist().getProvided(IPersistentStore.class).retrieve(transaction,location, Person.class);
			//sut.portPersist().getProvided(IPersistentStore.class).commitTransaction(transaction);
			
//			Person p = (Person)o;
//			Assert.assertNotNull(p);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void modify_Person() {
		try {
			IApplicationFramework os = new ApplicationFramework("os","os");
			JpaPersistence sut = os.createComponent(JpaPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			props.put("persistenceUnitName", "openjpa");
			//sut.portPersist().getProvided(IPersistentStore.class).connect(props);
			
			PersistentItemQuery location = new PersistentItemQuery("Test");
			Person item = new Person();
			item.setName("Test");
			item.setHairColour("black");
			//IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();

			//sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, item, Person.class);

			//Person p = sut.portPersist().getProvided(IPersistentStore.class).retrieve(transaction,location, Person.class);
			//Assert.assertNotNull(p);
			//p.setHairColour("brown");
			//sut.portPersist().getProvided(IPersistentStore.class).commitTransaction(transaction);

//			IPersistenceTransaction transaction2 = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();
//
//			Person p2 = sut.portPersist().getProvided(IPersistentStore.class).retrieve(transaction2,location, Person.class);
//			Assert.assertNotNull(p2);
//			Assert.assertTrue(p2.getHairColour().equals("brown"));
//			sut.portPersist().getProvided(IPersistentStore.class).commitTransaction(transaction2);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void retrieveAll_Person() {
		try {
			IApplicationFramework os = new ApplicationFramework("os","os");
			JpaPersistence sut = os.createComponent(JpaPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
//			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
//			
//			PersistentItemLocation location = new PersistentItemLocation("Test");
//			Person item1 = new Person();
//			item1.setName("Test1");
//			Person item2 = new Person();
//			item2.setName("Test2");
//			IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();
//
//			sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, item1, Person.class);
//			sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, item2, Person.class);
//
//			Set<Person> set = sut.portPersist().getProvided(IPersistentStore.class).retrieveAll(transaction,Person.class);
//			
//			Assert.assertTrue(set.size() > 0);
//			for(Person p: set) {
//				System.out.println(p.getName());
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	@Test
	public void retrieve_Contacts() {
		try {
			IApplicationFramework os = new ApplicationFramework("os","os");
			JpaPersistence sut = os.createComponent(JpaPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
//			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
//			
//			PersistentItemLocation location = new PersistentItemLocation("myContacts");
//			
//			Person p = new Person();
//			p.setName("Owner");
//			
//			Person p1 = new Person();
//			p1.setName("Test2");
//
//			Contacts contacts = new Contacts();
//			contacts.id = "myContacts";
//			contacts.owner = p;
//			contacts.people.add(p1);
//			IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();
//
//			sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, contacts, Contacts.class);
//			
//			
//			Object o = sut.portPersist().getProvided(IPersistentStore.class).retrieve(transaction,location, Contacts.class);
//			
//			Contacts c = (Contacts)o;
//			Assert.assertNotNull(c);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
