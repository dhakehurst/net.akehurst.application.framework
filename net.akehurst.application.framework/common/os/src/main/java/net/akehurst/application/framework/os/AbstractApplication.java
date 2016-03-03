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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

abstract public class AbstractApplication extends AbstractActiveObject implements IApplication {

	public AbstractApplication(String id, String[] args) {
		super(id);
		this.args = args;
		this.options = new Options();
		
		this.defineArguments();
	}

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
		formatter.printHelp( "<app>", options );
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

}
