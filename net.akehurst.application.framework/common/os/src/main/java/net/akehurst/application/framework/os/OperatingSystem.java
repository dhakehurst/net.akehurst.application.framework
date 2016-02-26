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
package net.akehurst.application.framework.os;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.os.annotations.BasicObjectInstance;
import net.akehurst.application.framework.os.annotations.ComponentInstance;
import net.akehurst.application.framework.os.annotations.ServiceReference;
import net.akehurst.holser.reflect.BetterMethodFinder;

public class OperatingSystem implements IOperatingSystem {

	public OperatingSystem(String serviceName) {
		this.services = new HashMap<>();
		this.services.put(serviceName, this);
	}

	Map<String, Object> services;

	public <T> T fetchService(String name) {
		return (T) this.services.get(name);
	}

	@Override
	public <T> T createService(String serviceName, Class<T> class_, String id) throws OperatingSystemExcpetion {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(class_);
			Constructor<T> cons = bmf.findConstructor(String.class);
			T obj = cons.newInstance(id);
			this.services.put(serviceName, obj);
			this.injectServices(obj, id);
			return obj;
		} catch (Exception ex) {
			throw new OperatingSystemExcpetion("Failed to create Service", ex);
		}
	}

	@Override
	public <T extends IApplication> T createApplication(Class<T> class_, String id, String[] arguments) throws OperatingSystemExcpetion {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(class_);
			Constructor<T> cons = bmf.findConstructor(String.class, String[].class);
			T obj = cons.newInstance(new Object[] { id, arguments });
			this.injectServices(obj, id);
			this.injectParts(obj, id);
			
			obj.instantiateServices();
			obj.instantiateComputational();
			obj.instantiateEngineering();
			obj.instantiateTechnology();
			obj.connectComputationalToEngineering();
			obj.connectEngineeringToTechnology();
			
			return obj;
		} catch (Exception ex) {
			throw new OperatingSystemExcpetion("Failed to create Application", ex);
		}
	}

	@Override
	public <T> T createComponent(Class<T> class_, String id) throws OperatingSystemExcpetion {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(class_);
			Constructor<T> cons = bmf.findConstructor(String.class);
			T obj = cons.newInstance(id);
			this.injectServices(obj, id);
			this.injectParts(obj, id);
			return obj;
		} catch (Exception ex) {
			throw new OperatingSystemExcpetion("Failed to create Basic Object", ex);
		}
	}

	@Override
	public <T> T createBasicObject(Class<T> class_, String id) throws OperatingSystemExcpetion {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(class_);
			Constructor<T> cons = bmf.findConstructor(String.class);
			T obj = cons.newInstance(id);
			this.injectServices(obj, id);
			this.injectParts(obj, id);
			return obj;
		} catch (Exception ex) {
			throw new OperatingSystemExcpetion("Failed to create Basic Object", ex);
		}
	}

	@Override
	public <T> T createDataType(Class<T> class_, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	private void injectServices(Object obj, String id) throws IllegalArgumentException, IllegalAccessException {
		for (Field f : obj.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			ServiceReference ann = f.getAnnotation(ServiceReference.class);
			if (null == ann) {
				// do nothing
			} else {
				String serviceName = ann.name();
				if (serviceName.isEmpty()) {
					serviceName = f.getName();
				} else {
					//do nothing
				}
				Object value = this.services.get(serviceName);
				f.set(obj, value);
			}
		}
	}
	
	private void injectParts(Object obj, String id) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, OperatingSystemExcpetion {
		for (Field f : obj.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			
			ComponentInstance ann = f.getAnnotation(ComponentInstance.class);
			if (null == ann) {
				// do nothing
			} else {
				String compId = ann.id();
				if (compId.isEmpty()) {
					compId = f.getName();
				} else {
					//do nothing
				}
				Object value = this.createComponent(f.getType(), id+"."+compId);
				f.set(obj, value);
			}
			
			BasicObjectInstance ann2 = f.getAnnotation(BasicObjectInstance.class);
			if (null == ann2) {
				// do nothing
			} else {
				String compId = ann2.id();
				if (compId.isEmpty()) {
					compId = f.getName();
				} else {
					//do nothing
				}
				Object value = this.createBasicObject(f.getType(), id+"."+compId);
				f.set(obj, value);
			}
			
		}
	}
	
	
}
