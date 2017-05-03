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
package net.akehurst.application.framework.common.test;

import net.akehurst.application.framework.common.IConfiguration;
import net.akehurst.application.framework.realisation.AbstractIdentifiableObject;

public class AbstractMockConfigurationService extends AbstractIdentifiableObject implements IConfiguration {

	public AbstractMockConfigurationService(final String afId) {
		super(afId);
	}

	@Override
	public <T> T fetchValue(final Class<T> itemType, final String idPath, final String defaultValueString) {
		// TODO Auto-generated method stub
		return null;
	}

}
