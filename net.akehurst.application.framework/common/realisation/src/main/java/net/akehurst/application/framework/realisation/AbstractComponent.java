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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.akehurst.application.framework.common.IActiveObject;
import net.akehurst.application.framework.common.IComponent;
import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract public class AbstractComponent extends AbstractActiveObject implements IComponent {

	public AbstractComponent(final String afId) {
		super(afId);
		this.ports = new HashSet<>();
	}

	Set<IPort> ports;

	@Override
	public void afAddPort(final IPort value) {
		this.ports.add(value);
	}

	@Override
	public void afConnectParts() {}

	@Override
	public void afStart() {
		this.logger.log(LogLevel.TRACE, "AbstractComponent.afStart");
		for (final IPort p : this.ports) {
			for (final Class<?> interfaceType : p.getRequired()) {
				if (null == p.out(interfaceType)) {
					System.out.println("Warn: Port " + p + " has not been provided with " + interfaceType);
				} else {
					// ok
				}
			}
		}

		super.afStart();
	}

	@Override
	public void afRun() {
		this.logger.log(LogLevel.TRACE, "AbstractComponent.afRun");
		try {
			final List<IActiveObject> objects = super.afActiveParts();

			for (final IActiveObject ao : objects) {
				ao.afStart();
			}

			for (final IActiveObject ao : objects) {
				ao.afJoin();
			}

		} catch (final Exception ex) {
			this.logger.log(LogLevel.ERROR, "Failed to run component " + this.afId(), ex);
		}
	}
}
