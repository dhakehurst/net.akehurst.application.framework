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

import net.akehurst.application.framework.components.IComponent;
import net.akehurst.application.framework.os.annotations.ComponentInstance;
import net.akehurst.application.framework.os.annotations.ServiceReference;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract public class AbstractApplication extends AbstractActiveObject implements IApplication {

	public AbstractApplication(String id, String[] args) {
		super(id);
		this.args = args;
		this.options = new Options();

		this.defineArguments();
	}

	@ServiceReference
	ILogger logger;
	
	String[] args;
	Options options;

	public void defineArgument(boolean required, String argumentName, boolean hasValue, String description) {
		Option opt = Option.builder().longOpt(argumentName).desc(description).required(required).hasArg(hasValue).build();
		this.options.addOption(opt);

	}

	public void parseArguments() {
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine commandLine = parser.parse(options, args);
			((OperatingSystem) this.os).setCommandLine(commandLine);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void afOutputCommandLineHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("<app>", options);
	}

	public void defineArguments() {
		this.defineArgument(false, "help", false, "Display command line argument descriptions for this application");
	}

	public void instantiateServices() {
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
			// TODO handle inheritance ?
			List<IActiveObject> objects = new ArrayList<>();
			for (Field f : this.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				ComponentInstance ann = f.getAnnotation(ComponentInstance.class);
				if (null == ann) {
					// do nothing
				} else {
					IActiveObject ao = (IActiveObject) f.get(this);
					//TODO: support ordering of objects
					objects.add(ao);
				}
			}
			
			for(IActiveObject ao: objects) {
				ao.afStart();
			}
			
			for(IActiveObject ao: objects) {
				ao.afJoin();
			}
			
		} catch (Exception ex) {
			logger.log(LogLevel.ERROR, "Failed to run application "+this.afId(), ex);
		}
	}

}
