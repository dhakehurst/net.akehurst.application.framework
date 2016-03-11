package net.akehurst.application.framework.technology.persistence.openJPA;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;

public class JpaPersistenceTransaction implements IPersistenceTransaction {

	public JpaPersistenceTransaction(EntityManager em) {
		this.em = em;
	}
	
	EntityManager em;
}
