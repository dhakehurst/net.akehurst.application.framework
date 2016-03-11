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

import org.apache.commons.cli.CommandLine;

import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.components.IComponent;
import net.akehurst.application.framework.components.Port;
import net.akehurst.application.framework.os.annotations.ActiveObjectInstance;
import net.akehurst.application.framework.os.annotations.CommandLineArgument;
import net.akehurst.application.framework.os.annotations.ComponentInstance;
import net.akehurst.application.framework.os.annotations.ConfiguredValue;
import net.akehurst.application.framework.os.annotations.PortInstance;
import net.akehurst.application.framework.os.annotations.ProvidesInterfaceForPort;
import net.akehurst.application.framework.os.annotations.ServiceReference;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemLocation;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;
import net.akehurst.holser.reflect.BetterMethodFinder;

public class OperatingSystem implements IOperatingSystem {

	static final String DEFAULT_CONFIGURATION_SERVICE = "configuration";

	public OperatingSystem(String serviceName) {
		this.services = new HashMap<>();
		this.services.put(serviceName, this);
	}

	void logError(String message) {
		ILogger logger = (ILogger)this.services.get("logger");
		if (null==logger) {
			System.err.println(message);
		} else {
			logger.log(LogLevel.ERROR, message);
		}
	}
	
	CommandLine commandLine;

	public void setCommandLine(CommandLine value) {
		this.commandLine = value;
	}

	Map<String, Object> services;

	public <T> T fetchService(String name) {
		return (T) this.services.get(name);
	}


	@Override
	public <T extends IIdentifiableObject> T createService(String serviceName, Class<T> class_, String id) throws OperatingSystemExcpetion {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(class_);
			Constructor<T> cons = bmf.findConstructor(String.class);
			T obj = cons.newInstance(id);
			this.services.put(serviceName, obj);
			this.injectIntoService(obj);
			return obj;
		} catch (Exception ex) {
			throw new OperatingSystemExcpetion("Failed to create Service", ex);
		}
	}

	public <T extends IIdentifiableObject> T injectIntoService(T obj) throws OperatingSystemExcpetion {
		try {
			this.injectServices(obj, obj.afId());
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
			cons.setAccessible(true);
			T obj = cons.newInstance(new Object[] { id, arguments });
			obj.instantiateServices(this);

			this.injectServices(obj, id);
			obj.parseArguments();
			if (obj instanceof IIdentifiableObject) {
				this.injectConfigurationValues((IIdentifiableObject) obj, id);
				this.injectCommandLineArgs((IIdentifiableObject) obj, id);
			}
			this.injectParts(obj, id);

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
	public <T extends IComponent> T createComponent(Class<T> class_, String id) throws OperatingSystemExcpetion {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(class_);
			Constructor<T> cons = bmf.findConstructor(String.class);
			T obj = cons.newInstance(id);
			this.injectServices(obj, id);
			if (obj instanceof IIdentifiableObject) {
				this.injectConfigurationValues((IIdentifiableObject) obj, id);
				this.injectCommandLineArgs((IIdentifiableObject) obj, id);
			}
			this.injectParts(obj, id);
			this.injectPorts(obj, id);
			obj.afConnectParts();
			return obj;
		} catch (Exception ex) {
			throw new OperatingSystemExcpetion("Failed to create Basic Object", ex);
		}
	}

	@Override
	public <T extends IIdentifiableObject> T createActiveObject(Class<T> class_, String id) throws OperatingSystemExcpetion {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(class_);
			Constructor<T> cons = bmf.findConstructor(String.class);
			T obj = cons.newInstance(id);
			this.injectIntoActiveObject(obj);
			return obj;
		} catch (Exception ex) {
			throw new OperatingSystemExcpetion("Failed to create Basic Object", ex);
		}
	}

	public <T extends IIdentifiableObject> T injectIntoActiveObject(T obj) throws OperatingSystemExcpetion {
		try {
			this.injectServices(obj, obj.afId());
			if (obj instanceof IIdentifiableObject) {
				this.injectConfigurationValues((IIdentifiableObject) obj, obj.afId());
				this.injectCommandLineArgs((IIdentifiableObject) obj, obj.afId());
			}
			this.injectParts(obj, obj.afId());
			return obj;
		} catch (Exception ex) {
			throw new OperatingSystemExcpetion("Failed to create Service", ex);
		}
	}

	@Override
	public <T> T createDatatype(Class<T> class_, Object... constructorArgs) throws OperatingSystemExcpetion {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(class_);
			Constructor<T> cons = bmf.findConstructor(constructorArgs);
			T obj = cons.newInstance(constructorArgs);
			return obj;
		} catch (Exception ex) {
			throw new OperatingSystemExcpetion("Failed to create Datatype", ex);
		}
	}

	private void injectServices(Object obj, String id) throws IllegalArgumentException, IllegalAccessException {
		this.injectServices(obj.getClass(), obj, id);
	}

	private void injectServices(Class<?> class_, Object obj, String id) throws IllegalArgumentException, IllegalAccessException {
		if (null == class_.getSuperclass()) {
			return; // Object.class will have a null superclass, no need to inject anything for Object.class
		} else {
			this.injectServices(class_.getSuperclass(), obj, id);
			for (Field f : class_.getDeclaredFields()) {
				f.setAccessible(true);
				ServiceReference ann = f.getAnnotation(ServiceReference.class);
				if (null == ann) {
					// do nothing
				} else {
					String serviceName = ann.name();
					if (serviceName.isEmpty()) {
						serviceName = f.getName();
					} else {
						// do nothing
					}
					Object value = this.services.get(serviceName);
					f.set(obj, value);
				}
			}
		}
	}

	private void injectConfigurationValues(IIdentifiableObject obj, String id)
			throws IllegalArgumentException, IllegalAccessException, OperatingSystemExcpetion, PersistentStoreException {
		this.injectConfigurationValues(obj.getClass(), obj, id);
	}

	private void injectConfigurationValues(Class<?> class_, IIdentifiableObject obj, String id)
			throws IllegalArgumentException, IllegalAccessException, OperatingSystemExcpetion, PersistentStoreException {
		if (null == class_.getSuperclass()) {
			return; // Object.class will have a null superclass, no need to inject anything for Object.class
		} else {
			this.injectCommandLineArgs(class_.getSuperclass(), obj, id);
			for (Field f : class_.getDeclaredFields()) {
				f.setAccessible(true);
				ConfiguredValue ann = f.getAnnotation(ConfiguredValue.class);
				if (null == ann) {
					// do nothing
				} else {
					String itemId = ann.id();
					if (itemId.isEmpty()) {
						itemId = obj.afId() + "." + f.getName();
					} else {
						// do nothing
					}

					String serviceName = ann.service();
					if (serviceName.isEmpty()) {
						serviceName = DEFAULT_CONFIGURATION_SERVICE;
					} else {
						// do nothing
					}

					IPersistentStore config = (IPersistentStore) this.services.get(serviceName);
					if (null == config) {
						logError("no configuration service found");
						Object value = this.createDatatype(f.getType(), ann.defaultValue());
						f.set(obj, value);
					} else {
						IPersistenceTransaction trans = config.startTransaction();
						Class<? extends Object> itemType = (Class<? extends Object>) f.getType();
						PersistentItemLocation pid = new PersistentItemLocation(itemId);
						Object value = config.retrieve(trans,pid, itemType);
						if (null == value) {
							value = this.createDatatype(f.getType(), ann.defaultValue());
						}
						f.set(obj, value);
						config.commitTransaction(trans);
					}
					
				}
			}
		}
	}

	private void injectCommandLineArgs(IIdentifiableObject obj, String id) throws IllegalArgumentException, IllegalAccessException, OperatingSystemExcpetion {
		this.injectCommandLineArgs(obj.getClass(), obj, id);
	}

	private void injectCommandLineArgs(Class<?> class_, IIdentifiableObject obj, String id)
			throws IllegalArgumentException, IllegalAccessException, OperatingSystemExcpetion {
		if (null == class_.getSuperclass()) {
			return; // Object.class will have a null superclass, no need to inject anything for Object.class
		} else {
			this.injectCommandLineArgs(class_.getSuperclass(), obj, id);
			for (Field f : class_.getDeclaredFields()) {
				f.setAccessible(true);
				CommandLineArgument ann = f.getAnnotation(CommandLineArgument.class);
				if (null == ann) {
					// do nothing
				} else {
					String name = ann.name();
					if (name.isEmpty()) {
						name = f.getName();
					} else {
						// do nothing
					}
					Object argValue = this.commandLine.getOptionValue(name);
					if (null == argValue && Boolean.class.isAssignableFrom(f.getType())) {
						argValue = this.commandLine.hasOption(name);
					}
					Object value = this.createDatatype(f.getType(), argValue);
					f.set(obj, value);
				}
			}
		}
	}

	private void injectParts(Object obj, String id) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException,
			NoSuchMethodException, OperatingSystemExcpetion {
		this.injectParts(obj.getClass(), obj, id);
	}

	private void injectParts(Class<?> class_, Object obj, String id) throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			InvocationTargetException, NoSuchMethodException, OperatingSystemExcpetion {
		if (null == class_.getSuperclass()) {
			return; // Object.class will have a null superclass, no need to inject anything for Object.class
		} else {
			this.injectParts(class_.getSuperclass(), obj, id);
			for (Field f : class_.getDeclaredFields()) {
				f.setAccessible(true);

				ComponentInstance ann = f.getAnnotation(ComponentInstance.class);
				if (null == ann) {
					// do nothing
				} else {
					String compId = ann.id();
					if (compId.isEmpty()) {
						compId = f.getName();
					} else {
						// do nothing
					}
					Class<? extends IComponent> fType = (Class<? extends IComponent>) f.getType();
					Object value = this.createComponent(fType, id + "." + compId);
					f.set(obj, value);
				}

				ActiveObjectInstance ann2 = f.getAnnotation(ActiveObjectInstance.class);
				if (null == ann2) {
					// do nothing
				} else {
					String compId = ann2.id();
					if (compId.isEmpty()) {
						compId = f.getName();
					} else {
						// do nothing
					}
					Class<? extends IIdentifiableObject> fType = (Class<? extends IIdentifiableObject>) f.getType();
					Object value = this.createActiveObject(fType, id + "." + compId);
					f.set(obj, value);
				}

			}
		}
	}

	private void injectPorts(IComponent obj, String id) throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			InvocationTargetException, NoSuchMethodException, OperatingSystemExcpetion {
		this.injectPorts(obj.getClass(), obj, id);
	}

	private void injectPorts(Class<?> class_, IComponent obj, String id) throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			InvocationTargetException, NoSuchMethodException, OperatingSystemExcpetion {
		if (null == class_.getSuperclass()) {
			return; // Object.class will have a null superclass, no need to inject anything for Object.class
		} else {
			this.injectPorts(class_.getSuperclass(), obj, id);
			for (Field f : class_.getDeclaredFields()) {
				f.setAccessible(true);

				PortInstance ann = f.getAnnotation(PortInstance.class);
				if (null == ann) {
					// do nothing
				} else {
					String compId = ann.id();
					if (compId.isEmpty()) {
						compId = f.getName();
					} else {
						// do nothing
					}

					Class<? extends Port> fType = (Class<? extends Port>) f.getType();
					Object value = this.createPort(fType, id + "." + compId, obj, ann.provides(), ann.requires());
					f.set(obj, value);
				}

			}
		}
	}

	Port createPort(Class<? extends Port> class_, String id, IComponent component, Class<?>[] provides, Class<?>[] requires) throws OperatingSystemExcpetion {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(class_);
			Constructor<Port> cons = bmf.findConstructor(id, component);
			Port obj = cons.newInstance(id, component);

			for (Class<?> interfaceType : provides) {
				Object provider = findInternalProviderForPort(component, interfaceType, id);
				obj.provides((Class<Object>) interfaceType, provider);
			}

			for (Class<?> interfaceType : requires) {
				obj.requires(interfaceType);
			}

			return obj;
		} catch (Exception ex) {
			throw new OperatingSystemExcpetion("Failed to create Port " + id, ex);
		}
	}
	
	<T> T findInternalProviderForPort(IComponent component, Class<T> interfaceType, String portId) throws IllegalArgumentException, IllegalAccessException, OperatingSystemExcpetion {
		ProvidesInterfaceForPort ann = null;
		String shortPortId = portId.substring(portId.lastIndexOf('.')+1);
		for (Field f : component.getClass().getDeclaredFields()) {
			ann = f.getAnnotation(ProvidesInterfaceForPort.class);
			if (null!=ann && interfaceType == ann.provides() && shortPortId.equals(ann.portId())) {
				f.setAccessible(true);
				return (T)f.get(component);
			}
		}
		if (interfaceType.isInstance(component)) {
			return (T)component;
		} else {
			throw new OperatingSystemExcpetion("Failed find internal provider of "+interfaceType.getSimpleName()+" for component "+component.afId(), null);
		}
	}
}
