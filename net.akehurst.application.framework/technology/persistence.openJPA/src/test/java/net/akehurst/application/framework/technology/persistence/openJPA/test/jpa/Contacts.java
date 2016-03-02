package net.akehurst.application.framework.technology.persistence.openJPA.test.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Contacts {
	public Contacts() {
		this.people = new ArrayList<>();
	}

	@Id
	public String id;

	@OneToOne(cascade=CascadeType.ALL)
	public Person owner;
	
	@OneToMany(cascade=CascadeType.ALL)
	public List<Person> people;
}
