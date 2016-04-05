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
package net.akehurst.application.framework.technology.persistence.jdo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.datanucleus.enhancement.Persistable;
import org.junit.Test;

import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.annotations.instance.ServiceInstance;
import net.akehurst.application.framework.realisation.ApplicationFramework;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemLocation;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;
import net.akehurst.application.framework.technology.log4j.Log4JLogger;

public class test_JdoPersistence {

	IApplicationFramework os() {
		return new ApplicationFramework("os", "os");
	}
	
	@ServiceInstance
	Log4JLogger logger;
	
	@Test
	public void connect() {
		try {

			JdoPersistence sut = os().createComponent(JdoPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			props.put("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
			props.put("javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver");
			props.put("javax.jdo.option.ConnectionURL", "jdbc:derby:db/Test;create=true");
//			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void store_Person() {
		try {
			JdoPersistence sut = os().createComponent(JdoPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			props.put("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
			props.put("javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver");
			props.put("javax.jdo.option.ConnectionURL", "jdbc:derby:db/Test;create=true");
			props.put("datanucleus.schema.autoCreateAll", true);
//			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
//			
//			PersistentItemLocation location = new PersistentItemLocation("");
//			Person item = new Person();
//			item.name = "Test";
//			IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();
//
//			sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, item, Person.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void store_Contacts() {
		try {
			JdoPersistence sut = os().createComponent(JdoPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			props.put("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
			props.put("javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver");
			props.put("javax.jdo.option.ConnectionURL", "jdbc:derby:db/Test;create=true");
			props.put("datanucleus.schema.autoCreateAll", true);
//			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
//			
//			PersistentItemLocation location = new PersistentItemLocation("");
//			
//			Person p = new Person();
//			p.name = "Owner";
//			
//			Person p1 = new Person();
//			p1.name = "Test2";
//
//			Contacts contacts = new Contacts();
//			contacts.owner = p;
//			contacts.people.add(p1);
//			IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();
//
//			sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, contacts, Contacts.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void store_Contacts2() {
		try {
			IApplicationFramework os = os();
			JdoPersistence sut = os.createComponent(JdoPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			props.put("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
			props.put("javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver");
			props.put("javax.jdo.option.ConnectionURL", "jdbc:derby:db/Test;create=true");
			props.put("datanucleus.schema.autoCreateAll", true);
//			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
//			
//			PersistentItemLocation location = new PersistentItemLocation("");
//
//			Class<?> e = sut.fetchEnhanced(Person.class);
//			Object o = os.createDatatype(e);
//			
//			Person p = new Person();
//			p.name = "Test2";
//
//			Contacts contacts = new Contacts();
//			contacts.people.add(p);
//			IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();
//
//			sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, contacts, Contacts.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	@Test
	public void retrieve_Person() {
		try {
			IApplicationFramework os = os();
			JdoPersistence sut = os.createComponent(JdoPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			props.put("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
			props.put("javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver");
			props.put("javax.jdo.option.ConnectionURL", "jdbc:derby:db/Test;create=true");
			props.put("datanucleus.schema.autoCreateAll", true);
//			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
//			
//			PersistentItemLocation location = new PersistentItemLocation("");
//			Person item = new Person();
//			item.name = "Test";
//			IPersistenceTransaction transaction = sut.portPersist().getProvided(IPersistentStore.class).startTransaction();
//
//			sut.portPersist().getProvided(IPersistentStore.class).store(transaction,location, item, Person.class);
//
//			Object o = sut.portPersist().getProvided(IPersistentStore.class).retrieve(transaction,location, Person.class);
			
//			Person p = (Person)o;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void retrieve_Contacts() {
		try {
			IApplicationFramework os = os();
			JdoPersistence sut = os.createComponent(JdoPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			props.put("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
			props.put("javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver");
			props.put("javax.jdo.option.ConnectionURL", "jdbc:derby:db/Test;create=true");
			props.put("datanucleus.schema.autoCreateAll", true);
//			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
//			
//			PersistentItemLocation location = new PersistentItemLocation("");
//			
//			Person p = new Person();
//			p.name = "Owner";
//			
//			Person p1 = new Person();
//			p1.name = "Test2";
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
			
//			Contacts c = (Contacts)o;
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
