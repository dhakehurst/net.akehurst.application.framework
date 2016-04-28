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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.Stringify;

import net.akehurst.application.framework.common.IActiveObject;
import net.akehurst.application.framework.common.IApplication;
import net.akehurst.application.framework.common.IComponent;
import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.annotations.instance.ActiveObjectInstance;
import net.akehurst.application.framework.common.annotations.instance.CommandLineArgument;
import net.akehurst.application.framework.common.annotations.instance.ComponentInstance;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract public class AbstractApplication extends AbstractActiveObject implements IApplication {

	public AbstractApplication(String id, String[] args) {
		super(id);
		this.args = args;
	}

	@ServiceReference
	ILogger logger;

	String[] args;

	@CommandLineArgument(group="help", name = "help", hasValue=false, description="Display the command line options for this application.")
	Boolean displayHelp;

	@CommandLineArgument(group="help", name = "configuration", hasValue=false, description="Display the current configuration of this application.")
	Boolean displayConfig;

	public void parseArguments() {
		this.af.setCommandLine(args);
	}

	public void afOutputCommandLineHelp() {
		this.af.outputCommandLineHelp();
	}

	private JsonValue fetchJsonValue() {
		return null;
	}

	private JsonObject fetchConfigFor(Object obj) {
		JsonObject json = new JsonObject();

		for (Field f : obj.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			try {
				ConfiguredValue annCv = f.getAnnotation(ConfiguredValue.class);
				if (null == annCv) {
					// donothing
				} else {
					String itemId = annCv.id();
					if (itemId.isEmpty()) {
						itemId = f.getName();
					} else {
						// do nothing
					}
					// TODO: handle types other than String
					json.add(itemId, f.get(obj).toString());
				}

				ComponentInstance annCI = f.getAnnotation(ComponentInstance.class);
				if (null == annCI) {
					// do nothing
				} else {
					String id = annCI.id();
					if (id.isEmpty()) {
						id = f.getName();
					} else {
						// do nothing
					}
					Object part = f.get(obj);
					json.add(id, fetchConfigFor(part));
				}

				ActiveObjectInstance annAI = f.getAnnotation(ActiveObjectInstance.class);
				if (null == annAI) {
					// do nothing
				} else {
					String id = annAI.id();
					if (id.isEmpty()) {
						id = f.getName();
					} else {
						// do nothing
					}
					Object part = f.get(obj);
					json.add(id, fetchConfigFor(part));
				}

			} catch (Exception ex) {
				logger.log(LogLevel.ERROR, "Failed to fetchConfg", ex);
			}
		}

		return json;
	}

	private void defineCommandLineArgumentsFor(IIdentifiableObject obj) {
		this.defineCommandLineArgumentsFor(obj.getClass(), obj);
	}

	private void defineCommandLineArgumentsFor(Class<?> class_, IIdentifiableObject obj) {
		if (null == class_.getSuperclass()) {
			return; // Object.class will have a null superclass, no need to inject anything for Object.class
		} else {
			this.defineCommandLineArgumentsFor(class_.getSuperclass(), obj);
			for (Field f : class_.getDeclaredFields()) {
				f.setAccessible(true);
				try {
					CommandLineArgument annCL = f.getAnnotation(CommandLineArgument.class);
					if (null == annCL) {
						// donothing
					} else {
						String argumentName = annCL.name();
						if (argumentName.isEmpty()) {
							argumentName = obj.afId() + '.' + f.getName();
						} else {
							// do nothing
						}
						String group = annCL.group();
						boolean required = annCL.required();
						boolean hasValue = annCL.hasValue();
						String description = annCL.description();
						this.af.defineCommandLineArgument(group, required, argumentName, hasValue, description);
					}

					ComponentInstance annCI = f.getAnnotation(ComponentInstance.class);
					if (null == annCI) {
						// do nothing
					} else {
						Object part = f.get(obj);
						if (part instanceof IIdentifiableObject) {
							this.defineCommandLineArgumentsFor((IIdentifiableObject) part);
						}
					}

					ActiveObjectInstance annAI = f.getAnnotation(ActiveObjectInstance.class);
					if (null == annAI) {
						// do nothing
					} else {
						Object part = f.get(obj);
						if (part instanceof IIdentifiableObject) {
							this.defineCommandLineArgumentsFor((IIdentifiableObject) part);
						}
					}

				} catch (Exception ex) {
					logger.log(LogLevel.ERROR, "Failed in defineCommandLineArgumentsFor", ex);
				}
			}
		}
	}

	public void afOutputConfiguration() {
		JsonObject app = this.fetchConfigFor(this);
		JsonObject config = new JsonObject();
		config.add(this.afId(), app);
		System.out.println(config.toString(Stringify.HJSON));
	}

	public void defineArguments() {
		this.defineCommandLineArgumentsFor(this);
	}

	public void connectComputationalToEngineering() {
	}

	public void connectEngineeringToTechnology() {
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

			// TODO handle inheritance ?
			List<IActiveObject> objects = new ArrayList<>();
			for (Field f : this.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				ComponentInstance ann = f.getAnnotation(ComponentInstance.class);
				if (null == ann) {
					// do nothing
				} else {
					IActiveObject ao = (IActiveObject) f.get(this);
					// TODO: support ordering of objects
					objects.add(ao);
				}
				
				ActiveObjectInstance ann2 = f.getAnnotation(ActiveObjectInstance.class);
				if (null == ann2) {
					// do nothing
				} else {
					IActiveObject ao = (IActiveObject) f.get(this);
					// TODO: support ordering of objects
					objects.add(ao);
				}
			}

			for (IActiveObject ao : objects) {
				ao.afStart();
			}

			for (IActiveObject ao : objects) {
				ao.afJoin();
			}

		} catch (Exception ex) {
			logger.log(LogLevel.ERROR, "Failed to run application " + this.afId(), ex);
		}
	}

}
