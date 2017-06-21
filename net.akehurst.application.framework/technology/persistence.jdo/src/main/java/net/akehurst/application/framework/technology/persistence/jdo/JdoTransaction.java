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
package net.akehurst.application.framework.technology.persistence.jdo;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.datanucleus.enhancement.Persistable;

import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;

public class JdoTransaction implements IPersistenceTransaction {

	public JdoTransaction(final PersistenceManager manager) {
		this.manager = manager;
		this.tx = manager.currentTransaction();
	}

	PersistenceManager manager;
	Transaction tx;

	public void begin() {
		this.tx.begin();
	}

	public void commit() {
		this.tx.commit();
	}

	public void rollback() {
		this.tx.rollback();
	}

	public void makePersistent(final Persistable pc) {
		this.manager.makePersistent(pc);
	}

	public Query<? extends Persistable> newQuery(final Class<? extends Persistable> enhancedType) {
		return this.manager.newQuery(enhancedType);
	}

	public Query<? extends Persistable> newQuery(final Class<? extends Persistable> enhancedType, final String language, final String queryString) {
		final Query<? extends Persistable> query = this.manager.newQuery(language, queryString);
		return query;
	}

	public void deletePersistent(final Persistable pc) {
		this.manager.deletePersistent(pc);
	}

}
