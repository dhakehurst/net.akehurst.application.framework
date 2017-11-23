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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
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
	public <T> T fetchValue(final Type itemType, final String idPath, final String defaultValueString) {
		try {
			final Class<?> class_ = ApplicationFramework.getClass(itemType);
			final IPersistenceTransaction trans = this.getStore().startTransaction();
			final Map<String, Object> filter = new HashMap<>();
			filter.put("path", idPath);
			final Set<T> values = (Set<T>) this.getStore().retrieve(trans, itemType, filter);
			final T value = values.iterator().next();
			this.getStore().commitTransaction(trans);
			if (null != value) {
				this.logger.log(LogLevel.TRACE,
						String.format("%s.fetchValue(%s,%s) = %s", this.afId(), itemType.getTypeName(), idPath, null == value ? "null" : value.toString()));

				return value;
			} else {
				// do nothing, use default from below
			}
		} catch (final PersistentStoreException e) {
			this.logger.log(LogLevel.DEBUG, "Failed to fetch value", e);
		}

		final T defaultValue = super.createValueFromDefault(itemType, defaultValueString);
		this.logger.log(LogLevel.TRACE, String.format("%s.fetchValue(%s,%s) = default %s", this.afId(), itemType.getTypeName(), idPath,
				null == defaultValue ? "null" : defaultValue.toString()));

		return defaultValue;
	}

}
