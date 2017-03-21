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
package net.akehurst.application.framework.technology.logging.console;

import net.akehurst.application.framework.common.IService;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class ConsoleLogger implements ILogger, IService {

	public ConsoleLogger(final String id) {
		this.id = id;
	}

	String id;

	@Override
	public String afId() {
		return this.id;
	}

	// --------- IService ---------
	@Override
	public Object createReference(final String locationId) {
		return new ConsoleLogger(locationId);
	}

	// --------- ILogger ---------
	@Override
	public void log(final LogLevel level, final String message) {
		final String prefix = level.getName() + ": " + "[" + this.afId() + "] ";
		if (level.getValue() > LogLevel.INFO.getValue()) {
			System.err.println(prefix + message);
		} else {
			System.out.println(prefix + message);
		}
	}

	@Override
	public void log(final LogLevel level, final String message, final Throwable t) {
		if (null == t) {
			this.log(level, message);
		} else {
			final String prefix = level.getName() + ": " + "[" + this.afId() + "] ";
			if (level.getValue() > LogLevel.INFO.getValue()) {
				System.err.println(prefix + message);
				t.printStackTrace(System.err);
			} else {
				System.out.println(prefix + message);
				t.printStackTrace(System.out);
			}
		}
	}

	@Override
	public void log(final LogLevel level, final String format, final Object... objects) {
		final String prefix = level.getName() + ": " + "[" + this.afId() + "] ";
		final String message = String.format(format, objects);
		if (level.getValue() > LogLevel.INFO.getValue()) {
			System.err.println(prefix + message);
		} else {
			System.out.println(prefix + message);
		}
	}
}
