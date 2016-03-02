package net.akehurst.application.framework.technology.persistence.openJPA.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import net.akehurst.application.framework.os.IOperatingSystem;
import net.akehurst.application.framework.os.OperatingSystem;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemLocation;
import net.akehurst.application.framework.technology.persistence.openJPA.JpaPersistence;
import net.akehurst.application.framework.technology.persistence.openJPA.test.jpa.Contacts;
import net.akehurst.application.framework.technology.persistence.openJPA.test.jpa.Person;

public class test_JpaPersistence {
	@Test
	public void connect() {
		try {
			IOperatingSystem os = new OperatingSystem("os");
			JpaPersistence sut = os.createComponent(JpaPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
//			props.put("javax.jdo.PersistenceManagerFactoryClass", "org.apache.openjpa.persistence.PersistenceProviderImpl ");
			props.put("openjpa.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver");
			props.put("openjpa.ConnectionURL", "jdbc:derby:db/Test;create=true");
			props.put("openjpa.jdbc.SynchronizeMappings","buildSchema(ForeignKeys=true)");
			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void store_Person() {
		try {
			IOperatingSystem os = new OperatingSystem("os");
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
			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
			
			PersistentItemLocation location = new PersistentItemLocation("");
			Person item = new Person();
			item.name = "Test";

//			Class<?> c = Class.forName("net.akehurst.application.framework.technology.persistence.openJPA.test.Person");
//			Object o = c.newInstance();
//			c.getField("name").set(o, "Test");
			
			sut.portPersist().getProvided(IPersistentStore.class).store(location, item, Person.class);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	@Test
	public void store_Contacts() {
		try {
			IOperatingSystem os = new OperatingSystem("os");
			JpaPersistence sut = os.createComponent(JpaPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
			
			PersistentItemLocation location = new PersistentItemLocation("");
			
			Person p = new Person();
			p.name = "Owner";
			
			Person p1 = new Person();
			p1.name = "Test2";

			Contacts contacts = new Contacts();
			contacts.id = "myContacts";
			contacts.owner = p;
			contacts.people.add(p1);
			
			sut.portPersist().getProvided(IPersistentStore.class).store(location, contacts, Contacts.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void retrieve_Person() {
		try {
			IOperatingSystem os = new OperatingSystem("os");
			JpaPersistence sut = os.createComponent(JpaPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			props.put("persistenceUnitName", "openjpa");
			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
			
			PersistentItemLocation location = new PersistentItemLocation("Test");
			Person item = new Person();
			item.name = "Test";

			sut.portPersist().getProvided(IPersistentStore.class).store(location, item, Person.class);

			Object o = sut.portPersist().getProvided(IPersistentStore.class).retrieve(location, Person.class);
			
			Person p = (Person)o;
			Assert.assertNotNull(p);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void retrieveAll_Person() {
		try {
			IOperatingSystem os = new OperatingSystem("os");
			JpaPersistence sut = os.createComponent(JpaPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
			
			PersistentItemLocation location = new PersistentItemLocation("Test");
			Person item1 = new Person();
			item1.name = "Test1";
			Person item2 = new Person();
			item2.name = "Test2";
			
			sut.portPersist().getProvided(IPersistentStore.class).store(location, item1, Person.class);
			sut.portPersist().getProvided(IPersistentStore.class).store(location, item2, Person.class);

			Set<Person> set = sut.portPersist().getProvided(IPersistentStore.class).retrieveAll(Person.class);
			
			Assert.assertTrue(set.size() > 0);
			for(Person p: set) {
				System.out.println(p.name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	@Test
	public void retrieve_Contacts() {
		try {
			IOperatingSystem os = new OperatingSystem("os");
			JpaPersistence sut = os.createComponent(JpaPersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
			
			PersistentItemLocation location = new PersistentItemLocation("myContacts");
			
			Person p = new Person();
			p.name = "Owner";
			
			Person p1 = new Person();
			p1.name = "Test2";

			Contacts contacts = new Contacts();
			contacts.id = "myContacts";
			contacts.owner = p;
			contacts.people.add(p1);
			
			sut.portPersist().getProvided(IPersistentStore.class).store(location, contacts, Contacts.class);
			
			
			Object o = sut.portPersist().getProvided(IPersistentStore.class).retrieve(location, Contacts.class);
			
			Contacts c = (Contacts)o;
			Assert.assertNotNull(c);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
