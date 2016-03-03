package net.akehurst.application.framework.technology.persistence.jdo;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class Contacts {
	public Contacts() {
		this.people = new ArrayList<>();
	}
	
	@Persistent
	public String id;
	
	@Persistent
	public Person owner;
	
	@Persistent
	@Element(column="id")
	public List<Person> people;
}
