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
package net.akehurst.application.framework.realisation;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.IConfiguration;
import net.akehurst.application.framework.common.IService;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract public class AbstractConfigurationService extends AbstractIdentifiableObject implements IConfiguration, IService {

	@ServiceReference
	protected IApplicationFramework af;

	@ServiceReference
	protected ILogger logger;

	public AbstractConfigurationService(final String afId) {
		super(afId);
	}

	@Override
	public Object createReference(final String locationId) {
		return this;
	}

	protected <T> T createValueFromDefault(final Type itemType, final String defaultValueString) {
		try {
			Object value;
			final Class<?> class_ = ApplicationFramework.getClass(itemType);
			// TODO: implement default values for list and map

			if (class_.isAssignableFrom(ArrayList.class)) {
				value = new ArrayList<>();
			} else if (class_.isAssignableFrom(HashMap.class)) {
				value = new HashMap<>();
			} else {
				value = this.af.createDatatype(itemType, defaultValueString);
			}
			return (T) value;
		} catch (final ApplicationFrameworkException e) {
			this.logger.log(LogLevel.ERROR, e.getMessage());
			this.logger.log(LogLevel.DEBUG, e.getMessage(), e);
		}
		return null;
	}

}
