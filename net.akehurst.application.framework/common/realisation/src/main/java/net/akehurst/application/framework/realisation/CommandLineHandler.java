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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandLineHandler implements ICommandLineHandler {

    static class CommandLineArgumentValue {
        CommandLineArgumentValue(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        String name;
        String value;
    }

    public CommandLineHandler(final String groupRegEx, final String argumentStart) {
        this.groupRegEx = groupRegEx;
        this.argumentStart = argumentStart;
        this.definedArgs = new HashMap<>();
        this.defineGroup("");
        this.groupArgs = new HashMap<>();
        // add default group
        this.groupArgs.put("", new HashMap<>());
    }

    String groupRegEx;
    String argumentStart;
    Map<String, List<ArgumentDefinition>> definedArgs;

    Map<String, Map<String, String>> groupArgs;

    @Override
    public void defineGroup(final String groupName) {
        this.definedArgs.put(groupName, new ArrayList<>());
    }

    @Override
    public void defineArgument(final String group, final String name, final Class<?> type, final boolean required, final boolean hasValue,
            final Object defaultValue, final String description) {
        final List<ArgumentDefinition> args = this.definedArgs.get(group);
        args.add(new ArgumentDefinition(group, name, type, required, hasValue, defaultValue, description));
    }

    @Override
    public String getHelp() {
        String result = "";
        for (final String grp : this.definedArgs.keySet()) {
            result += (grp.isEmpty() ? "<default>" : grp) + System.lineSeparator();
            final List<ArgumentDefinition> da = this.definedArgs.get(grp);
            for (final ArgumentDefinition ad : da) {
                result += "  " + ad.getName() + " : " + ad.getType().getSimpleName() + (ad.getRequired() ? " [1] " : " [?] ") + "("
                        + (null == ad.getDefaultValue() ? "" : ad.getDefaultValue()) + ") - " + ad.getDescription();
                result += System.lineSeparator();
            }
            result += System.lineSeparator();
        }
        return result;
    }

    @Override
    public void parse(final String[] args) {
        // default group if nothing else parsed
        String currentGroup = "";
        for (final String arg : args) {
            if (arg.startsWith(this.argumentStart)) {
                this.parseArgument(currentGroup, arg.substring(this.argumentStart.length()));
            } else if (arg.matches(this.groupRegEx)) {
                currentGroup = arg;
                this.groupArgs.put(currentGroup, new HashMap<>());
            } else {
                throw new RuntimeException("Command line argument doesn't match expected pattern " + arg);
            }
        }
    }

    private void parseArgument(final String currentGroup, final String arg) {
        final String[] split = arg.split("=");
        final Map<String, String> groupArgs = this.groupArgs.get(currentGroup);
        if (null == groupArgs) {
            // internal error group must have been parsed before an argument
        } else {
            if (split.length > 1) {
                // has a value
                groupArgs.put(split[0], split[1]);
            } else {
                // null indicates presence
                groupArgs.put(split[0], null);
            }
        }
    }

    @Override
    public boolean hasGroup(final String group) {
        return this.groupArgs.containsKey(group);
    }

    @Override
    public String getArgumentValue(final String group, final String name) {
        final Map<String, String> groupArgs = this.groupArgs.get(group);
        if (null == groupArgs) {
            return null;
        } else {
            return groupArgs.get(name);
        }
    }

    @Override
    public boolean hasArgument(final String group, final String name) {
        final Map<String, String> groupArgs = this.groupArgs.get(group);
        if (null == groupArgs) {
            return false;
        } else {
            return groupArgs.containsKey(name);
        }
    }

}
