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

import net.akehurst.application.framework.common.IService;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class Log4JLogger implements ILogger, IService {

    public Log4JLogger(final String id) {
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

    // --------- IService ---------
    @Override
    public Object createReference(final String locationId) {
        return new Log4JLogger(locationId);
    }

    // --------- ILogger ---------
    @Override
    public void log(final LogLevel level, final String message) {
        final Level l4jlevel = Level.getLevel(level.getName());
        this.log4j.log(l4jlevel, message);
    }

    @Override
    public void log(final LogLevel level, final String message, final Throwable t) {
        final Level l4jlevel = Level.getLevel(level.getName());
        this.log4j.log(l4jlevel, message, t);
    }

    @Override
    public void log(final LogLevel level, final String format, final Object... objects) {
        final String message = String.format(format, objects);
        final Level l4jlevel = Level.getLevel(level.getName());
        this.log4j.log(l4jlevel, message);
    }
}
