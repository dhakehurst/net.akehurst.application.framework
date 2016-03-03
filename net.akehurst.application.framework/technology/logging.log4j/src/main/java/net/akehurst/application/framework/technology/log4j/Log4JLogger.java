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
package net.akehurst.application.framework.technology.log4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class Log4JLogger implements IIdentifiableObject, ILogger {

	public Log4JLogger(String id) {
		this.log4j = LogManager.getLogger(id);
		this.id = id;
	}
	String id;
	@Override
	public String afId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	Logger log4j;
	
	@Override
	public void log(LogLevel level, String message) {
		Level l4jlevel = Level.getLevel(level.getName());
		this.log4j.log(l4jlevel , message);
	}
	
	@Override
	public void log(LogLevel level, String message, Throwable t) {
		Level l4jlevel = Level.getLevel(level.getName());
		this.log4j.log(l4jlevel , message,t);
	}
}
