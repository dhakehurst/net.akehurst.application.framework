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

import net.akehurst.datatype.common.DatatypeAbstract;

public class LogLevel extends DatatypeAbstract {

    public static LogLevel OFF = new LogLevel("OFF", 0);
    public static LogLevel FATAL = new LogLevel("FATAL", 100);
    public static LogLevel ERROR = new LogLevel("ERROR", 200);
    public static LogLevel INFO = new LogLevel("INFO", 300);
    public static LogLevel WARN = new LogLevel("WARN", 400);
    public static LogLevel DEBUG = new LogLevel("DEBUG", 500);
    public static LogLevel TRACE = new LogLevel("TRACE", 600);
    public static LogLevel ALL = new LogLevel("ALL", 1000);

    public LogLevel(final String name, final int value) {
        super(name);
        this.name = name;
        this.value = value;
    }

    String name;

    public String getName() {
        return this.name;
    }

    int value;

    public int getValue() {
        return this.value;
    }
}
