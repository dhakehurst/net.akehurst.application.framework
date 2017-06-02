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

	public Query<? extends Persistable> newQuery(final Class<? extends Persistable> enhancedType, final String language, final String filterString) {
		final String queryString = "SELECT FROM " + enhancedType.getName() + " WHERE " + filterString;
		return this.manager.newQuery(language, queryString);
	}

	public void deletePersistent(final Persistable pc) {
		this.manager.deletePersistent(pc);
	}

}
