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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.akehurst.application.framework.os.AbstractActiveObject;
import net.akehurst.application.framework.os.IActiveObject;
import net.akehurst.application.framework.os.annotations.ActiveObjectInstance;
import net.akehurst.application.framework.os.annotations.ComponentInstance;
import net.akehurst.application.framework.os.annotations.ServiceReference;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract
public class AbstractComponent extends AbstractActiveObject implements IComponent {

	public AbstractComponent(String id) {
		super(id);
		this.ports = new HashSet<>();
	}

	@ServiceReference
	ILogger logger;
	
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
	
	@Override
	public void afRun() {
		try {
			// TODO handle inheritance ?
			List<IActiveObject> objects = new ArrayList<>();
			for (Field f : this.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				ComponentInstance annC = f.getAnnotation(ComponentInstance.class);
				ActiveObjectInstance annAO = f.getAnnotation(ActiveObjectInstance.class);
				if (null == annC && null==annAO) {
					// do nothing
				} else {
					IActiveObject ao = (IActiveObject) f.get(this);
					//TODO: support ordering of objects
					objects.add(ao);
				}
			}
			
			for(IActiveObject ao: objects) {
				ao.afStart();
			}
			
			for(IActiveObject ao: objects) {
				ao.afJoin();
			}
			
		} catch (Exception ex) {
			logger.log(LogLevel.ERROR, "Failed to run application "+this.afId(), ex);
		}
	}
}
