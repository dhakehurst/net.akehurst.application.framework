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
package net.akehurst.application.framework.common;

import java.util.Set;

public interface IPort extends IIdentifiableObject {

	/**
	 * Set of objects internal to the component that provide the interface
	 * @param interfaceType
	 * @return
	 */
	<T> Set<T> getProvided(Class<T> interfaceType);

	Set<Class<?>> getRequired();

	<T> void provideRequired(Class<T> interfaceType, T provider);

	/**
	 * returns an object that implements the required interfaceType normally a 'connection' to something outside the component probably something 'provided' by
	 * a different Component.Port
	 * 
	 * @param interfaceType
	 * @return
	 */
	<T> T out(Class<T> interfaceType);

	void connect(IPort other);
}
