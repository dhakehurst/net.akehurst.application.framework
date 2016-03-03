package net.akehurst.application.framework.technology.persistence.jdo;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class Person {

	@Persistent
	public String name;
	
}
