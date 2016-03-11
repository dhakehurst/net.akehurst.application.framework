package net.akehurst.application.framework.technology.persistence.openJPA.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import net.akehurst.application.framework.technology.persistence.openJPA.test.jpa.Person;

public class SimpleMain {

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("openjpa");
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		Person p = new Person();
		p.setName("t1");
		p.setHairColour("black");
		em.persist(p);
		em.getTransaction().commit();
		em.close();
		
		em = emf.createEntityManager();
		em.getTransaction().begin();
		Person p2 = em.find(Person.class, "t1");
		p2.setHairColour("brown");
		em.getTransaction().commit();
		em.close();
		emf.close();
		
		emf = Persistence.createEntityManagerFactory("openjpa");
		em = emf.createEntityManager();
		em.getTransaction().begin();
		Person p3 = em.find(Person.class, "t1");

		em.getTransaction().commit();
		em.close();	
		
	}
	
}
