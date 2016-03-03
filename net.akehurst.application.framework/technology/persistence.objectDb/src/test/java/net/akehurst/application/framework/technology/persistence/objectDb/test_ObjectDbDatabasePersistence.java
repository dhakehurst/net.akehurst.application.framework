package net.akehurst.application.framework.technology.persistence.objectDb;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import net.akehurst.application.framework.os.IOperatingSystem;
import net.akehurst.application.framework.os.OperatingSystem;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemLocation;

public class test_ObjectDbDatabasePersistence {
	@Test
	public void connect() {
		try {
			IOperatingSystem os = new OperatingSystem("os");
			ObjectDbDatabasePersistence sut = os.createComponent(ObjectDbDatabasePersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void store_Person() {
		try {
			IOperatingSystem os = new OperatingSystem("os");
			ObjectDbDatabasePersistence sut = os.createComponent(ObjectDbDatabasePersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
			sut.portPersist().getProvided(IPersistentStore.class).connect(props);
			
			PersistentItemLocation location = new PersistentItemLocation("");
			Person item = new Person();
			item.name = "Test";

			sut.portPersist().getProvided(IPersistentStore.class).store(location, item, Person.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void store_Contacts() {
		try {
			IOperatingSystem os = new OperatingSystem("os");
			ObjectDbDatabasePersistence sut = os.createComponent(ObjectDbDatabasePersistence.class, "test");

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
			ObjectDbDatabasePersistence sut = os.createComponent(ObjectDbDatabasePersistence.class, "test");

			Map<String, Object> props = new HashMap<>();
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
			ObjectDbDatabasePersistence sut = os.createComponent(ObjectDbDatabasePersistence.class, "test");

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
			ObjectDbDatabasePersistence sut = os.createComponent(ObjectDbDatabasePersistence.class, "test");

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
