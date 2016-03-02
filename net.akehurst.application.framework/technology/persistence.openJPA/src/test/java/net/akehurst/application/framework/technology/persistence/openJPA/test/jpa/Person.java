package net.akehurst.application.framework.technology.persistence.openJPA.test.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Person {

	@Id
	public String name;
	
}
