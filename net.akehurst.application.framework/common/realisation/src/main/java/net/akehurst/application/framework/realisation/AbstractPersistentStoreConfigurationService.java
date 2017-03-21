package net.akehurst.application.framework.realisation;

import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemQuery;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;

abstract public class AbstractPersistentStoreConfigurationService extends AbstractConfigurationService {

	public AbstractPersistentStoreConfigurationService(final String afId) {
		super(afId);
	}

	abstract protected IPersistentStore getStore();

	@Override
	public Object createReference(final String locationId) {
		return this;
	}

	@Override
	public <T> T fetchValue(final Class<T> itemType, final String idPath, final String defaultValueString) {
		try {
			final IPersistenceTransaction trans = this.getStore().startTransaction();
			final PersistentItemQuery pid = new PersistentItemQuery(idPath);
			final T value = this.getStore().retrieve(trans, pid, itemType);
			this.getStore().commitTransaction(trans);
			if (null != value) {
				this.logger.log(LogLevel.TRACE,
						String.format("%s.fetchValue(%s,%s) = %s", this.afId(), itemType.getName(), idPath, null == value ? "null" : value.toString()));

				return value;
			} else {
				// do nothing, use default from below
			}
		} catch (final PersistentStoreException e) {
			this.logger.log(LogLevel.DEBUG, e.getMessage());
		}

		final T defaultValue = super.createValueFromDefault(itemType, defaultValueString);
		this.logger.log(LogLevel.TRACE, String.format("%s.fetchValue(%s,%s) = default %s", this.afId(), itemType.getName(), idPath,
				null == defaultValue ? "null" : defaultValue.toString()));

		return defaultValue;
	}

}
