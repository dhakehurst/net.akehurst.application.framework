package net.akehurst.application.framework.common.test;

import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;

import net.akehurst.application.framework.common.IService;
import net.akehurst.application.framework.realisation.AbstractIdentifiableObject;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemQuery;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;

public class AbstractMockConfigurationService extends AbstractIdentifiableObject implements IService, IPersistentStore {

	public AbstractMockConfigurationService(final String afId) {
		super(afId);
		this.mock = EasyMock.mock(IPersistentStore.class);
	}

	IPersistentStore mock;

	@Override
	public void connect(final Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public IPersistenceTransaction startTransaction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void commitTransaction(final IPersistenceTransaction transaction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rollbackTransaction(final IPersistenceTransaction transaction) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void store(final IPersistenceTransaction transaction, final PersistentItemQuery query, final T item, final Class<T> itemType)
			throws PersistentStoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void remove(final IPersistenceTransaction transaction, final PersistentItemQuery query, final Class<T> itemType)
			throws PersistentStoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T retrieve(final IPersistenceTransaction transaction, final PersistentItemQuery query, final Class<T> itemType) throws PersistentStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Set<T> retrieve(final IPersistenceTransaction transaction, final PersistentItemQuery query, final Class<T> itemType,
			final Map<String, Object> filter) throws PersistentStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Set<T> retrieveAll(final IPersistenceTransaction transaction, final Class<T> itemType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object createReference(final String locationId) {
		// TODO Auto-generated method stub
		return null;
	}

}
