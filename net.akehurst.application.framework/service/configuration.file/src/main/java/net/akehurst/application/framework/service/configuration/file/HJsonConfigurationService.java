package net.akehurst.application.framework.service.configuration.file;

import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.realisation.AbstractPersistentStoreConfigurationService;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.persistence.filesystem.HJsonFile;

public class HJsonConfigurationService extends AbstractPersistentStoreConfigurationService {

	@ServiceReference
	IApplicationFramework af;

	@ServiceReference
	ILogger logger;

	public HJsonConfigurationService(final String afId) {
		super(afId);

	}

	IPersistentStore store;

	@Override
	protected IPersistentStore getStore() {
		if (null == this.store) {
			try {
				this.store = new HJsonFile(this.afId());
				this.af.injectIntoSimpleObject(this.store);
			} catch (final ApplicationFrameworkException e) {
				this.logger.log(LogLevel.ERROR, e.getMessage());
				this.logger.log(LogLevel.DEBUG, e.getMessage(), e);
			}
		}
		return this.store;
	}

}
