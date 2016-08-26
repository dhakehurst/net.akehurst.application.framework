/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.akehurst.application.framework.technology.persistence.openJPA.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import net.akehurst.application.framework.technology.persistence.openJPA.test.jpa.Person;

public class SimpleMain {

	public static void main(final String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("openjpa");
		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();
		final Person p = new Person();
		p.setName("t1");
		p.setHairColour("black");
		em.persist(p);
		em.getTransaction().commit();
		em.close();

		em = emf.createEntityManager();
		em.getTransaction().begin();
		final Person p2 = em.find(Person.class, "t1");
		p2.setHairColour("brown");
		em.getTransaction().commit();
		em.close();
		emf.close();

		emf = Persistence.createEntityManagerFactory("openjpa");
		em = emf.createEntityManager();
		em.getTransaction().begin();
		final Person p3 = em.find(Person.class, "t1");

		em.getTransaction().commit();
		em.close();

	}

}
