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
package net.akehurst.application.framework.technology.interfaceLogging;

import net.akehurst.application.framework.common.AbstractDataType;

public class LogLevel extends AbstractDataType {

	public static LogLevel OFF = new LogLevel("OFF");
	public static LogLevel FATAL = new LogLevel("FATAL");
	public static LogLevel ERROR = new LogLevel("ERROR");
	public static LogLevel INFO = new LogLevel("INFO");
	public static LogLevel WARN = new LogLevel("WARN");
	public static LogLevel DEBUG = new LogLevel("DEBUG");
	public static LogLevel TRACE = new LogLevel("TRACE");
	public static LogLevel ALL = new LogLevel("ALL");
	
	public LogLevel(String name) {
		super(name);
		this.name = name;
	}
	
	String name;
	public String getName() {
		return this.name;
	}
}
