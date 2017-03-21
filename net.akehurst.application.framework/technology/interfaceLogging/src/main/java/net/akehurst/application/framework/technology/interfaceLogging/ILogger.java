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

public interface ILogger {

	/**
	 * send the message to the logger at the given log level
	 *
	 * @param level
	 * @param message
	 */
	void log(LogLevel level, String message);

	/**
	 * send the message to the logger at the given log level
	 *
	 * @param level
	 * @param message
	 * @param t
	 */
	void log(LogLevel level, String message, Throwable t);

	/**
	 * send a formatted message to the logger at the given log level
	 *
	 * format is put the String.format with the object arguments
	 *
	 * @param level
	 * @param format
	 * @param objects
	 */
	void log(LogLevel level, String format, Object... objects);

}
