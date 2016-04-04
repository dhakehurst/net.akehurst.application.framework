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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import net.akehurst.application.framework.common.IApplication;
import net.akehurst.application.framework.common.IComponent;
import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.IService;
import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.annotations.declaration.ProvidesInterfaceForPort;
import net.akehurst.application.framework.common.annotations.instance.ActiveObjectInstance;
import net.akehurst.application.framework.common.annotations.instance.CommandLineArgument;
import net.akehurst.application.framework.common.annotations.instance.ComponentInstance;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemLocation;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;
import net.akehurst.holser.reflect.BetterMethodFinder;

public class ApplicationFramework implements IApplicationFramework, IService {

	static final String DEFAULT_CONFIGURATION_SERVICE = "configuration";

	public ApplicationFramework(String id, String serviceName) {
		this.afId = id;
		this.services = new HashMap<>();
		this.services.put(serviceName, this);
		this.commandLineOptionGroups = new HashMap<>();
	}

	String afId;

	@Override
	public String afId() {
		return this.afId;
	}

	@Override
	public Object createReference(String locationId) {
		return this;
	}

	ILogger logger() {
		ILogger logger = this.createServiceReference("logger", "os.logger", ILogger.class);
		return logger;
	}

	void logError(String message, Throwable t) {
		ILogger logger = logger();
		if (null == logger) {
			System.err.println(message);
			if (null != t) {
				t.printStackTrace();
			}
		} else {
			logger.log(LogLevel.ERROR, message, t);
		}
	}

	Map<String, Options> commandLineOptionGroups;

	@Override
	public void defineCommandLineArgument(String groupName, boolean required, String argumentName, boolean hasValue, String description) {
		// remove applicationName from start
		int i = argumentName.indexOf('.');
		if (i > 0) {
			argumentName = argumentName.substring(i + 1);
		}
		Options group = this.commandLineOptionGroups.get(groupName);
		if (null == group) {
			group = new Options();
			this.commandLineOptionGroups.put(groupName, group);
		}

		Option opt = Option.builder().longOpt(argumentName).desc(description).required(required).hasArg(hasValue).build();
		group.addOption(opt);
	}

	CommandLine commandLine;

	public void setCommandLine(String[] args) {
		try {
			CommandLineParser parser = new DefaultParser();
			if (args.length < 1) {
				this.commandLine = parser.parse(new Options(), args, true);
				return; // nothing to parse
			} else {
				for (Options opts : this.commandLineOptionGroups.values()) {
					
					this.commandLine = parser.parse(opts, args, true);
					if (this.commandLine.getArgList().isEmpty()) {
						// parse has succeeded
						return;
					} else {
						// try next OptionGroup
					}
				}
				// all OptionGroups failed to parse
				this.outputCommandLineHelp();
				System.exit(1);
			}
		} catch (MissingOptionException ex) {
			this.outputCommandLineHelp();
			System.exit(1);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	Object getOptionValue(String argumentName) {
		int i = argumentName.indexOf('.');
		if (i > 0) {
			argumentName = argumentName.substring(i + 1);
		}
		return this.commandLine.getOptionValue(argumentName);
	}

	boolean hasOption(String argumentName) {
		int i = argumentName.indexOf('.');
		if (i > 0) {
			argumentName = argumentName.substring(i + 1);
		}
		return this.commandLine.hasOption(argumentName);
	}

	public void outputCommandLineHelp() {
		HelpFormatter formatter = new HelpFormatter();
		for (Options opts : this.commandLineOptionGroups.values()) {
			formatter.printHelp(100, "<application>", "", opts, "", true);
		}
	}

	Map<String, IService> services;

	public IService fetchService(String name) {
		return this.services.get(name);
	}

	@Override
	public <T extends IService> T createServiceInstance(String serviceName, Class<T> class_, String id) throws ApplicationFrameworkException {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(class_);
			Constructor<T> cons = bmf.findConstructor(String.class);
			T obj = cons.newInstance(id);
			this.services.put(serviceName, obj);
			this.injectIntoService(obj);
			return obj;
		} catch (Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Service", ex);
		}
	}

	<T> T createServiceReference(String serviceName, String locationId, Class<T> serviceReferenceType) {

		InvocationHandler h = new InvocationHandler() {
			T serviceReference;

			T getServiceReference() {
				if (null == this.serviceReference) {
					IService service = fetchService(serviceName);
					this.serviceReference = (T) service.createReference(locationId);
				}
				return this.serviceReference;
			}

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				try {
					T sr = this.getServiceReference();
					if (null == sr) {
						logError("Cannot find service named " + serviceName, null);
					} else {
						Object result = method.invoke(sr, args);
						return result;
					}
				} catch (InvocationTargetException ex) {
					throw ex.getCause();
				}
				return null;
			}
		};
		Object proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { serviceReferenceType }, h);
		return (T) proxy;
	}

	public <T extends IIdentifiableObject> T injectIntoService(T obj) throws ApplicationFrameworkException {
		try {
			this.injectServiceReferences(obj, obj.afId());
			return obj;
		} catch (Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Service", ex);
		}
	}

	@Override
	public <T extends IApplication> T createApplication(Class<T> class_, String id, String[] arguments) throws ApplicationFrameworkException {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(class_);
			Constructor<T> cons = bmf.findConstructor(String.class, String[].class);
			cons.setAccessible(true);
			T obj = cons.newInstance(new Object[] { id, arguments });
			this.injectServiceInstances(obj, id);

			this.injectServiceReferences(obj, id);
			this.injectParts(obj, id);

			obj.defineArguments();
			obj.parseArguments();
			this.injectConfigurationValues((IIdentifiableObject) obj, id);
			this.injectCommandLineArgs((IIdentifiableObject) obj, id);

			obj.connectComputationalToEngineering();
			obj.connectEngineeringToTechnology();

			return obj;
		} catch (Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Application", ex);
		}
	}

	@Override
	public <T extends IComponent> T createComponent(Class<T> class_, String id) throws ApplicationFrameworkException {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(class_);
			Constructor<T> cons = bmf.findConstructor(String.class);
			T obj = cons.newInstance(id);
			this.injectServiceReferences(obj, id);
			this.injectParts(obj, id);
			this.injectPorts(obj, id);
			// if (obj instanceof IIdentifiableObject) {
			// this.injectConfigurationValues((IIdentifiableObject) obj, id);
			// this.injectCommandLineArgs((IIdentifiableObject) obj, id);
			// }
			obj.afConnectParts();
			return obj;
		} catch (Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Basic Object", ex);
		}
	}

	public <T extends IIdentifiableObject> T createActiveObject(Class<T> class_, String id) throws ApplicationFrameworkException {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(class_);
			Constructor<T> cons = bmf.findConstructor(String.class);
			T obj = cons.newInstance(id);
			this.injectIntoActiveObject(obj);
			return obj;
		} catch (Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Basic Object", ex);
		}
	}

	public <T extends IIdentifiableObject> T injectIntoActiveObject(T obj) throws ApplicationFrameworkException {
		try {
			this.injectServiceReferences(obj, obj.afId());
			this.injectParts(obj, obj.afId());
			// if (obj instanceof IIdentifiableObject) {
			// this.injectConfigurationValues((IIdentifiableObject) obj, obj.afId());
			// this.injectCommandLineArgs((IIdentifiableObject) obj, obj.afId());
			// }
			return obj;
		} catch (Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Service", ex);
		}
	}

	@Override
	public <T> T createDatatype(Class<T> class_, Object... constructorArgs) throws ApplicationFrameworkException {
		try {
			BetterMethodFinder bmf = new BetterMethodFinder(class_);
			Constructor<T> cons = bmf.findConstructor(constructorArgs);
			T obj = cons.newInstance(constructorArgs);
			return obj;
		} catch (Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Datatype", ex);
		}
	}

	private void injectServiceInstances(Object obj, String id) throws IllegalArgumentException, IllegalAccessException {
		this.injectServiceInstances(obj.getClass(), obj, id);
	}

	private void injectServiceInstances(Class<?> class_, Object obj, String id) throws IllegalArgumentException, IllegalAccessException {
		if (null == class_.getSuperclass()) {
			return; // Object.class will have a null superclass, no need to inject anything for Object.class
		} else {
			this.injectServiceInstances(class_.getSuperclass(), obj, id);
			for (Field f : class_.getDeclaredFields()) {
				try {
					f.setAccessible(true);
					ServiceInstance ann = f.getAnnotation(ServiceInstance.class);
					if (null == ann) {
						// do nothing
					} else {
						String serviceId = ann.id();
						if (serviceId.isEmpty()) {
							serviceId = f.getName();
						} else {
							// do nothing
						}
						IService service = this.createServiceInstance(serviceId, (Class<IService>) f.getType(), id + "." + serviceId);
						f.set(obj, service);
					}
				} catch (Exception ex) {
					logError(ex.getMessage(), ex);
				}
			}
		}
	}

	private void injectServiceReferences(Object obj, String id) throws IllegalArgumentException, IllegalAccessException {
		this.injectServiceReferences(obj.getClass(), obj, id);
	}

	private void injectServiceReferences(Class<?> class_, Object obj, String id) throws IllegalArgumentException, IllegalAccessException {
		if (null == class_.getSuperclass()) {
			return; // Object.class will have a null superclass, no need to inject anything for Object.class
		} else {
			this.injectServiceReferences(class_.getSuperclass(), obj, id);
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

					Object value = this.createServiceReference(serviceName, id, f.getType());
					f.set(obj, value);
				}
			}
		}
	}

	private void injectConfigurationValues(IIdentifiableObject obj, String id)
			throws IllegalArgumentException, IllegalAccessException, ApplicationFrameworkException, PersistentStoreException {
		this.injectConfigurationValues(obj.getClass(), obj, id);
	}

	private void injectConfigurationValues(Class<?> class_, IIdentifiableObject obj, String id)
			throws IllegalArgumentException, IllegalAccessException, ApplicationFrameworkException, PersistentStoreException {
		if (null == class_.getSuperclass()) {
			return; // Object.class will have a null superclass, no need to inject anything for Object.class
		} else {
			this.injectConfigurationValues(class_.getSuperclass(), obj, id);
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
						logError("no configuration service found", null);
						Object value = this.createDatatype(f.getType(), ann.defaultValue());
						f.set(obj, value);
					} else {
						IPersistenceTransaction trans = config.startTransaction();
						Class<? extends Object> itemType = (Class<? extends Object>) f.getType();
						PersistentItemLocation pid = new PersistentItemLocation(itemId);
						Object value = config.retrieve(trans, pid, itemType);
						if (null == value) {
							value = this.createDatatype(f.getType(), ann.defaultValue());
						}
						f.set(obj, value);
						config.commitTransaction(trans);
					}
				}

				ComponentInstance annCI = f.getAnnotation(ComponentInstance.class);
				if (null == annCI) {
					// do nothing
				} else {
					String partId = annCI.id();
					if (partId.isEmpty()) {
						partId = f.getName();
					} else {
						// do nothing
					}
					Object part = f.get(obj);
					this.injectConfigurationValues((IIdentifiableObject) part, id + "." + partId);
				}

				ActiveObjectInstance annAI = f.getAnnotation(ActiveObjectInstance.class);
				if (null == annAI) {
					// do nothing
				} else {
					String partId = annAI.id();
					if (partId.isEmpty()) {
						partId = f.getName();
					} else {
						// do nothing
					}
					Object part = f.get(obj);
					this.injectConfigurationValues((IIdentifiableObject) part, id + "." + partId);
				}

			}
		}
	}

	private void injectCommandLineArgs(IIdentifiableObject obj, String id) throws IllegalArgumentException, IllegalAccessException, ApplicationFrameworkException {
		this.injectCommandLineArgs(obj.getClass(), obj, id);
	}

	private void injectCommandLineArgs(Class<?> class_, IIdentifiableObject obj, String id)
			throws IllegalArgumentException, IllegalAccessException, ApplicationFrameworkException {
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
						name = id + "." + f.getName();
					} else {
						// do nothing
					}
					Object argValue = this.getOptionValue(name); //
					if (null == argValue && Boolean.class.isAssignableFrom(f.getType())) {
						argValue = this.hasOption(name);
					}
					if (null != argValue) {
						Object value = this.createDatatype(f.getType(), argValue);

						f.set(obj, value);
					}
				}
				ComponentInstance annCI = f.getAnnotation(ComponentInstance.class);
				if (null == annCI) {
					// do nothing
				} else {
					String partId = annCI.id();
					if (partId.isEmpty()) {
						partId = f.getName();
					} else {
						// do nothing
					}
					Object part = f.get(obj);
					this.injectCommandLineArgs((IIdentifiableObject) part, id + "." + partId);
				}

				ActiveObjectInstance annAI = f.getAnnotation(ActiveObjectInstance.class);
				if (null == annAI) {
					// do nothing
				} else {
					String partId = annAI.id();
					if (partId.isEmpty()) {
						partId = f.getName();
					} else {
						// do nothing
					}
					Object part = f.get(obj);
					this.injectCommandLineArgs((IIdentifiableObject) part, id + "." + partId);
				}
			}
		}
	}

	private void injectParts(Object obj, String id) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException,
			NoSuchMethodException, ApplicationFrameworkException {
		this.injectParts(obj.getClass(), obj, id);
	}

	private void injectParts(Class<?> class_, Object obj, String id) throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			InvocationTargetException, NoSuchMethodException, ApplicationFrameworkException {
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
			InvocationTargetException, NoSuchMethodException, ApplicationFrameworkException {
		this.injectPorts(obj.getClass(), obj, id);
	}

	private void injectPorts(Class<?> class_, IComponent obj, String id) throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			InvocationTargetException, NoSuchMethodException, ApplicationFrameworkException {
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

					// Class<? extends Port> fType = (Class<? extends Port>) f.getType();
					Object value = this.createPort(Port.class, id + "." + compId, obj, ann.provides(), ann.requires());
					f.set(obj, value);
				}

			}
		}
	}

	IPort createPort(Class<? extends IPort> class_, String id, IComponent component, Class<?>[] provides, Class<?>[] requires) throws ApplicationFrameworkException {
		try {
			Port obj = new Port(id, component);
			this.injectServiceReferences(obj, id);

			for (Class<?> interfaceType : provides) {
				Object provider = findInternalProviderForPort(component, interfaceType, id);
				obj.provides((Class<Object>) interfaceType, provider);
			}

			for (Class<?> interfaceType : requires) {
				obj.requires(interfaceType);
			}

			return obj;
		} catch (Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Port " + id, ex);
		}
	}

	<T> T findInternalProviderForPort(IComponent component, Class<T> interfaceType, String portId)
			throws IllegalArgumentException, IllegalAccessException, ApplicationFrameworkException {
		ProvidesInterfaceForPort ann = null;
		String shortPortId = portId.substring(portId.lastIndexOf('.') + 1);
		for (Field f : component.getClass().getDeclaredFields()) {
			ann = f.getAnnotation(ProvidesInterfaceForPort.class);
			if (null != ann && interfaceType == ann.provides() && shortPortId.equals(ann.portId())) {
				f.setAccessible(true);
				return (T) f.get(component);
			}
		}
		if (interfaceType.isInstance(component)) {
			return (T) component;
		} else {
			throw new ApplicationFrameworkException("Failed find internal provider of " + interfaceType.getSimpleName() + " for component " + component.afId(),
					null);
		}
	}
}
