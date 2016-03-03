package net.akehurst.application.framework.technology.persistence.objectDb;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Person {

	@Id
	public String name;
	
}
