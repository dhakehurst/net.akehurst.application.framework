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
package net.akehurst.application.framework.technology.interfaceGui;

import java.util.regex.Pattern;

import net.akehurst.application.framework.common.AbstractDataType;

public class DialogIdentity extends AbstractDataType {

	private static Pattern valid = Pattern.compile("[a-zA-z][a-zA-Z0-9]*");

	public DialogIdentity(final String value) throws GuiException {
		super(value);
		if (DialogIdentity.valid.matcher(value).matches()) {
			// ok
		} else {
			throw new GuiException("Invalid DialogIdentity value - " + value, null);
		}
	}

	public String asPrimitive() {
		return (String) super.getIdentityValues().get(0);
	}
}
