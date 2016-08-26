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

public class ArgumentDefinition {

	public ArgumentDefinition(final String group, final String name, final Class<?> type, final boolean required, final boolean hasValue,
			final Object defaultValue, final String description) {
		super();
		this.group = group;
		this.name = name;
		this.type = type;
		this.required = required;
		this.hasValue = hasValue;
		this.defaultValue = defaultValue;
		this.description = description;
	}

	String group;

	public String getGroup() {
		return this.group;
	}

	String name;

	public String getName() {
		return this.name;
	}

	Class<?> type;

	public Class<?> getType() {
		return this.type;
	}

	boolean required;

	public boolean getRequired() {
		return this.required;
	}

	boolean hasValue;

	public boolean getHasValue() {
		return this.hasValue;
	}

	Object defaultValue;

	public Object getDefaultValue() {
		return this.defaultValue;
	}

	String description;

	public String getDescription() {
		return this.description;
	}
}
