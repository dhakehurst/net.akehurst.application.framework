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

import java.lang.reflect.Field;
import java.util.List;

import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.Stringify;

import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IActiveObject;
import net.akehurst.application.framework.common.IApplication;
import net.akehurst.application.framework.common.annotations.instance.ActiveObjectInstance;
import net.akehurst.application.framework.common.annotations.instance.CommandLineArgument;
import net.akehurst.application.framework.common.annotations.instance.ComponentInstance;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract public class AbstractApplication extends AbstractActiveObject implements IApplication {

	public AbstractApplication(final String id) {
		super(id);
	}

	@ServiceReference
	ILogger logger;

	@CommandLineArgument(group = "", name = "help", hasValue = false, description = "Display the command line options for this application.")
	Boolean displayHelp;

	@CommandLineArgument(group = "", name = "configuration", hasValue = false, description = "Display the current configuration of this application.")
	Boolean displayConfig;

	public void afOutputCommandLineHelp() {
		this.af.outputCommandLineHelp();
	}

	private JsonValue fetchJsonValue(final Object value) throws ApplicationFrameworkException {
		if (value instanceof List<?>) {
			final JsonArray arr = new JsonArray();
			for (final Object o : (List<?>) value) {
				final JsonValue jo = this.fetchJsonValue(o);
				arr.add(jo);
			}
			return arr;
		} else if (value instanceof Enum) {
			return JsonValue.valueOf(value.toString());
		} else if (value instanceof String) {
			return JsonValue.valueOf((String) value);
		} else if (value instanceof Boolean || value.getClass() == Boolean.TYPE) {
			return JsonValue.valueOf((Boolean) value);
		} else if (value instanceof Byte || value.getClass() == Byte.TYPE) {
			return JsonValue.valueOf((Byte) value);
		} else if (value instanceof Integer || value.getClass() == Integer.TYPE) {
			return JsonValue.valueOf((Integer) value);
		} else if (value instanceof Short || value.getClass() == Short.TYPE) {
			return JsonValue.valueOf((Short) value);
		} else if (value instanceof Long || value.getClass() == Long.TYPE) {
			return JsonValue.valueOf((Long) value);
		} else if (value instanceof Float || value.getClass() == Float.TYPE) {
			return JsonValue.valueOf((Float) value);
		} else if (value instanceof Double || value.getClass() == Double.TYPE) {
			return JsonValue.valueOf((Double) value);
		} else if (value instanceof Character || value.getClass() == Character.TYPE) {
			return JsonValue.valueOf((Character) value);
		} else {
			throw new ApplicationFrameworkException("Cannot create Json representation for " + value, null);
		}

	}

	private JsonObject fetchConfigFor(final Object obj) {
		final JsonObject json = new JsonObject();

		for (final Field f : obj.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			try {
				final ConfiguredValue annCv = f.getAnnotation(ConfiguredValue.class);
				if (null == annCv) {
					// donothing
				} else {
					String itemId = annCv.id();
					if (itemId.isEmpty()) {
						itemId = f.getName();
					} else {
						// do nothing
					}
					final Object value = f.get(obj);
					json.add(itemId, this.fetchJsonValue(value));
				}

				final ComponentInstance annCI = f.getAnnotation(ComponentInstance.class);
				if (null == annCI) {
					// do nothing
				} else {
					String id = annCI.id();
					if (id.isEmpty()) {
						id = f.getName();
					} else {
						// do nothing
					}
					final Object part = f.get(obj);
					json.add(id, this.fetchConfigFor(part));
				}

				final ActiveObjectInstance annAI = f.getAnnotation(ActiveObjectInstance.class);
				if (null == annAI) {
					// do nothing
				} else {
					String id = annAI.id();
					if (id.isEmpty()) {
						id = f.getName();
					} else {
						// do nothing
					}
					final Object part = f.get(obj);
					json.add(id, this.fetchConfigFor(part));
				}

			} catch (final Exception ex) {
				this.logger.log(LogLevel.ERROR, "Failed to fetchConfg", ex);
			}
		}

		return json;
	}

	public void afOutputConfiguration() {
		final JsonObject config = this.fetchConfigFor(this);
		System.out.println(config.toString(Stringify.HJSON));
	}

	@Override
	public void afRun() {
		try {
			if (this.displayHelp) {
				this.afOutputCommandLineHelp();
				System.exit(0);
			}

			if (this.displayConfig) {
				this.afOutputConfiguration();
				System.exit(0);
			}

			final List<IActiveObject> objects = super.afActiveParts();
			this.logger.log(LogLevel.TRACE, "Active Parts: %s", objects);

			for (final IActiveObject ao : objects) {
				ao.afStart();
			}

			for (final IActiveObject ao : objects) {
				ao.afJoin();
			}

		} catch (final Exception ex) {
			this.logger.log(LogLevel.ERROR, "Failed to run application " + this.afId(), ex);
		}
	}

	@Override
	public void afInterrupt() {
		try {
			final List<IActiveObject> objects = super.afActiveParts();
			for (final IActiveObject ao : objects) {
				ao.afInterrupt();
			}
		} catch (final Exception ex) {
			this.logger.log(LogLevel.ERROR, "Error during Interrupt application " + this.afId(), ex);
		}
	}

	@Override
	public void afTerminate() {
		try {
			final List<IActiveObject> objects = super.afActiveParts();
			for (final IActiveObject ao : objects) {
				ao.afTerminate();
			}
		} catch (final Exception ex) {
			this.logger.log(LogLevel.ERROR, "Error during Terminate application " + this.afId(), ex);
		}
	}

}
