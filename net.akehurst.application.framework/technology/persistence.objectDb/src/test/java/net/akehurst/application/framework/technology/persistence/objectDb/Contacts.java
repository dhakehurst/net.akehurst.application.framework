package net.akehurst.application.framework.technology.persistence.objectDb;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Contacts {
	public Contacts() {
		this.people = new ArrayList<>();
	}

	@Id
	public String id;

	public Person owner;
	

	public List<Person> people;
}
