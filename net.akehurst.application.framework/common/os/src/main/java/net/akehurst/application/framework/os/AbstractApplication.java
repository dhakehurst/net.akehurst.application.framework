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
package net.akehurst.application.framework.os;

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
import net.akehurst.application.framework.common.IOperatingSystem;
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

	@CommandLineArgument(name = "help")
	Boolean displayHelp;

	@CommandLineArgument(name = "configuration")
	Boolean displayConfig;

	public void defineArgument(boolean required, String argumentName, boolean hasValue, String description) {
		this.os.defineCommandLineArgument(required, argumentName, hasValue, description);
	}

	public void parseArguments() {
		this.os.setCommandLine(args);
	}

	public void afOutputCommandLineHelp() {
		this.os.outputCommandLineHelp();
	}

	private JsonValue fetchJsonValue() {
		return null;
	}
	
	private JsonObject fetchConfigFor(Object obj) {
		JsonObject json = new JsonObject();

		for (Field f : obj.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			try {
				ConfiguredValue annCL = f.getAnnotation(ConfiguredValue.class);
				if (null == annCL) {
					// donothing
				} else {
					String itemId = annCL.id();
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

	public void afOutputConfiguration() {
		JsonObject app = this.fetchConfigFor(this);
		JsonObject config = new JsonObject();
		config.add(this.afId(), app);
		System.out.println(config.toString(Stringify.HJSON));
	}

	public void defineArguments() {
		this.os.defineCommandLineArgument(false, "help", false, "Display command line argument descriptions for this application");
		this.os.defineCommandLineArgument(false, "configuration", false, "Display the configuration of this application");
	}

	public void instantiateComputational() {
	}

	public void instantiateEngineering() {
	}

	public void instantiateTechnology() {
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
