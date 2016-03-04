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
package net.akehurst.application.framework.components;

import java.util.HashSet;
import java.util.Set;

import net.akehurst.application.framework.os.AbstractActiveObject;
import net.akehurst.application.framework.os.annotations.ServiceReference;

abstract
public class AbstractComponent extends AbstractActiveObject implements IComponent {

	public AbstractComponent(String id) {
		super(id);
		this.ports = new HashSet<>();
	}

	Set<Port> ports;
	@Override
	public void afAddPort(Port value) {
		this.ports.add(value);
	}
	
	@Override
	public void afConnectParts() {
	}
	
	@Override
	public void afStart() {
		for(Port p: this.ports) {
			for(Class<?>interfaceType : p.getRequired()) {
				if (null==p.out(interfaceType)) {
					System.out.println("Warn: Port "+p+" has not been provided with "+interfaceType);
				} else {
					// ok
				}
			}
		}
		
		super.afStart();
	}
}
