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
package net.akehurst.application.framework.technology.gui.vertx;

import net.akehurst.application.framework.common.DataTypeAbstract;

public class IpPort extends DataTypeAbstract {

	public IpPort(final String value) {
		this(Integer.parseInt(value));
	}

	public IpPort(final Integer value) {
		super(value);
		this.value = value;
	}

	Integer value;

	public Integer asPrimitive() {
		return this.value;
	}

}