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
import net.akehurst.application.framework.common.annotations.instance.CommandLineArgument;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.realisation.PersistentStoreConfigurationServiceAbstract;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.persistence.filesystem.HJsonFile;

public class HJsonConfigurationService extends PersistentStoreConfigurationServiceAbstract {

    @CommandLineArgument(description = "name of a directory in which the configuration file can be found")
    protected String directory = "configuration";

    @ServiceReference
    private ILogger logger;

    public HJsonConfigurationService(final String afId) {
        super(afId);

    }

    private IPersistentStore store;

    @Override
    protected IPersistentStore getStore() {
        if (null == this.store) {
            try {
                // re inject stuff into this service, because otherwise we don't get the commandline args injected
                // services are injected before connadline args are parsed.
                super.af.injectIntoService(this);

                final String path = this.directory.isEmpty() ? "" : this.directory + "/" + this.afId();
                this.store = new HJsonFile(path);
                super.af.injectIntoSimpleObject(this.store);

                this.logger.log(LogLevel.DEBUG, "Using configuration file " + ((HJsonFile) this.store).getFile().getFullName());

            } catch (final ApplicationFrameworkException e) {
                super.logger.log(LogLevel.ERROR, e.getMessage());
                super.logger.log(LogLevel.DEBUG, e.getMessage(), e);
            }
        }
        return this.store;
    }

}
