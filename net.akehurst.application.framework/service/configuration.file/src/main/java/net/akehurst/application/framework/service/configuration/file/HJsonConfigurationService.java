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
