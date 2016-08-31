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

import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IActiveObject;
import net.akehurst.application.framework.common.IApplication;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.IComponent;
import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.IService;
import net.akehurst.application.framework.common.annotations.declaration.ProvidesInterfaceForPort;
import net.akehurst.application.framework.common.annotations.instance.CommandLineArgument;
import net.akehurst.application.framework.common.annotations.instance.CommandLineGroup;
import net.akehurst.application.framework.common.annotations.instance.CommandLineGroupContainer;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistenceTransaction;
import net.akehurst.application.framework.technology.interfacePersistence.IPersistentStore;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentItemQuery;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;
import net.akehurst.application.framework.technology.logging.console.ConsoleLogger;
import net.akehurst.holser.reflect.BetterMethodFinder;

public class ApplicationFramework implements IApplicationFramework, IService {

	public static <T extends IApplication> T start(final Class<T> applicationClass, final String[] arguments) {
		try {

			final IApplicationFramework af = new ApplicationFramework("af", "af");
			final T app = af.createApplication(applicationClass, "application", arguments);
			app.afStart();
			return app;
		} catch (final Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	static final String DEFAULT_CONFIGURATION_SERVICE = "configuration";

	/**
	 * Other parts of the FW expect the ApplicationFramework to have a serviceName == 'af. e.g. ...persistence.filesystem.HJsonFile
	 *
	 * @param id
	 * @param serviceName
	 */
	public ApplicationFramework(final String id, final String serviceName) {
		this.afId = id;
		this.services = new HashMap<>();
		this.services.put(serviceName, this);
		this.commandLineHandler = new CommandLineHandler("[a-zA-Z0-9_-]+", "--");
		// this.commandLineOptionGroups = new HashMap<>();
	}

	ICommandLineHandler commandLineHandler;

	// --------- IIdentifiable ---------
	String afId;

	@Override
	public String afId() {
		return this.afId;
	}

	// --------- IService ---------
	@Override
	public Object createReference(final String locationId) {
		return this;
	}

	// --------- IApplicationFramework ---------

	@Override
	public <T extends IIdentifiableObject> T createObject(final Class<T> class_, final Object... constructorArgs) throws ApplicationFrameworkException {
		try {
			final BetterMethodFinder bmf = new BetterMethodFinder(class_);
			final Constructor<T> cons = bmf.findConstructor(constructorArgs);
			cons.setAccessible(true);
			final T obj = cons.newInstance(constructorArgs);

			this.injectServiceReferences(obj.getClass(), obj);
			this.injectParts(obj);

			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("cannot create object with class " + class_, ex);
		}
	}

	@Override
	public <T extends IApplication> T createApplication(final Class<T> class_, final String id, final String[] arguments) throws ApplicationFrameworkException {
		try {
			final BetterMethodFinder bmf = new BetterMethodFinder(class_);
			final Constructor<T> cons = bmf.findConstructor(String.class);
			cons.setAccessible(true);
			final T appObj = cons.newInstance(new Object[] { id });

			this.injectServiceInstances(class_, appObj);
			this.injectServiceReferences(appObj.getClass(), appObj);
			this.injectParts(appObj);

			this.defineCommandLine(appObj);

			// appObj.defineArguments();
			this.commandLineHandler.parse(arguments);
			// appObj.parseArguments();
			this.injectConfigurationValues(appObj);
			this.injectCommandLineArgs(appObj);

			appObj.afConnectParts();

			return appObj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Application", ex);
		}
	}

	@Override
	public <T extends IService> T createServiceInstance(final String serviceName, final Class<T> class_, final String id) throws ApplicationFrameworkException {
		try {
			final BetterMethodFinder bmf = new BetterMethodFinder(class_);
			final Constructor<T> cons = bmf.findConstructor(String.class);
			final T obj = cons.newInstance(id);
			this.services.put(serviceName, obj);
			this.injectIntoService(obj);
			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Service", ex);
		}
	}

	@Override
	public <T extends IComponent> T createComponent(final Class<T> class_, final String id) throws ApplicationFrameworkException {
		try {
			final BetterMethodFinder bmf = new BetterMethodFinder(class_);
			final Constructor<T> cons = bmf.findConstructor(String.class);
			final T obj = cons.newInstance(id);
			this.injectServiceReferences(obj.getClass(), obj);
			this.injectParts(obj);
			this.injectPorts(obj.getClass(), obj);
			// this.injectConfigurationValues(obj);
			// this.injectCommandLineArgs(obj);
			obj.afConnectParts();
			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Component " + id, ex);
		}
	}

	@Override
	public <T extends IActiveObject> T createActiveObject(final Class<T> class_, final String id) throws ApplicationFrameworkException {
		try {
			final BetterMethodFinder bmf = new BetterMethodFinder(class_);
			final Constructor<T> cons = bmf.findConstructor(String.class);
			final T obj = cons.newInstance(id);
			this.injectIntoActiveObject(obj);
			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Basic Object", ex);
		}
	}

	@Override
	public <T> T createDatatype(final Class<T> class_, final Object... constructorArgs) throws ApplicationFrameworkException {
		try {
			final BetterMethodFinder bmf = new BetterMethodFinder(class_);
			final Constructor<T> cons = bmf.findConstructor(constructorArgs);
			final T obj = cons.newInstance(constructorArgs);
			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Datatype", ex);
		}
	}

	@Override
	public <T extends IService> T injectIntoService(final T obj) throws ApplicationFrameworkException {
		try {
			this.injectServiceReferences(obj.getClass(), obj);
			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Service", ex);
		}
	}

	@Override
	public <T extends IActiveObject> T injectIntoActiveObject(final T obj) throws ApplicationFrameworkException {
		try {
			this.injectServiceReferences(obj.getClass(), obj);
			this.injectParts(obj);
			// this.injectConfigurationValues(obj.getClass(), obj);
			// this.injectCommandLineArgs(obj.getClass(), obj);

			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Service", ex);
		}
	}

	private void defineCommandLine(final IApplication applicationObject) {
		final ApplicationCompositionTreeWalker walker = new ApplicationCompositionTreeWalker(this.logger());

		// Define groups
		walker.walkAllAndApply(applicationObject, (obj, objId) -> {
			try {
				final AnnotationNavigator an = new AnnotationNavigator(obj);
				for (final AnnotationDetailsList<CommandLineGroup> ad : an.getList(CommandLineGroupContainer.class, CommandLineGroup.class)) {
					for (final CommandLineGroup a : ad.getAnnotations()) {
						this.commandLineHandler.defineGroup(a.name());
					}
				}
			} catch (final Throwable t) {
				this.logError("Failed to check for command line groups in " + objId, t);
			}
		});

		// Define arguments
		walker.walkAllAndApply(applicationObject, (obj, objId) -> {
			try {
				final AnnotationNavigator an = new AnnotationNavigator(obj);
				for (final AnnotationDetails<CommandLineArgument> ad : an.get(CommandLineArgument.class)) {
					final String group = ad.getAnnotation().group();
					String name = ad.getAnnotation().name();
					if (name.isEmpty()) {
						name = ad.getField().getName();
					} else {
						// use value from annotation
					}
					final boolean required = ad.getAnnotation().required();
					final boolean hasValue = ad.getAnnotation().hasValue();
					final Object defaultValue = ad.getValue();
					final String description = ad.getAnnotation().description();
					final Class<?> type = ad.getField().getType();
					this.commandLineHandler.defineArgument(group, name, type, required, hasValue, defaultValue, description);
				}
			} catch (final Throwable t) {
				this.logError("Failed to check for command line groups in " + objId, t);
			}
		});

	}

	// @Override
	// public void defineCommandLineArgument(String[] groupNames, boolean required, String argumentName, boolean hasValue, String description) {
	// // remove applicationName from start
	// int i = argumentName.indexOf('.');
	// if (i > 0) {
	// argumentName = argumentName.substring(i + 1);
	// }
	// Options group = this.commandLineOptionGroups.get(groupNames);
	// if (null == group) {
	// group = new Options();
	// this.commandLineOptionGroups.put(groupNames, group);
	// }
	//
	// Option opt = Option.builder().longOpt(argumentName).desc(description).required(required).hasArg(hasValue).build();
	// group.addOption(opt);
	// }

	@Override
	public void outputCommandLineHelp() {
		final String help = this.commandLineHandler.getHelp();
		// HelpFormatter formatter = new HelpFormatter();
		// for (Options opts : this.commandLineHandler.getGoups()) {
		// formatter.printHelp(100, "<application>", "", opts, "", true);
		// }
		this.logger().log(LogLevel.INFO, System.lineSeparator() + help);
	}

	ILogger logger() {
		if (null == this.fetchService("logger")) {
			this.services.put("logger", new ConsoleLogger("logger"));
		}
		final ILogger logger = this.createServiceReference("logger", "af.logger", ILogger.class);
		return logger;
	}

	void logError(final String message, final Throwable t) {
		final ILogger logger = this.logger();
		if (null == logger) {
			System.err.println(message);
			if (null != t) {
				t.printStackTrace();
			}
		} else {
			logger.log(LogLevel.ERROR, message, t);
		}
	}

	// Map<String, Options> commandLineOptionGroups;

	// CommandLine commandLine;

	// public void setCommandLine(String[] args) {
	// try {
	// CommandLineParser parser = new DefaultParser();
	// if (args.length < 1) {
	// this.commandLine = parser.parse(new Options(), args, true);
	// return; // nothing to parse
	// } else {
	// for (Options opts : this.commandLineOptionGroups.values()) {
	//
	// this.commandLine = parser.parse(opts, args, true);
	// if (this.commandLine.getArgList().isEmpty()) {
	// // parse has succeeded
	// return;
	// } else {
	// // try next OptionGroup
	// }
	// }
	// // all OptionGroups failed to parse
	// this.outputCommandLineHelp();
	// System.exit(1);
	// }
	// } catch (MissingOptionException ex) {
	// this.outputCommandLineHelp();
	// System.exit(1);
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// }

	// Object getOptionValue(String argumentName) {
	//// int i = argumentName.indexOf('.');
	//// if (i > 0) {
	//// argumentName = argumentName.substring(i + 1);
	//// }
	// return this.commandLine.getOptionValue(argumentName);
	// }
	//
	// boolean hasOption(String argumentName) {
	//// int i = argumentName.indexOf('.');
	//// if (i > 0) {
	//// argumentName = argumentName.substring(i + 1);
	//// }
	// return this.commandLine.hasOption(argumentName);
	// }

	Map<String, IService> services;

	IService fetchService(final String name) {
		return this.services.get(name);
	}

	<T> T createServiceReference(final String serviceName, final String locationId, final Class<T> serviceReferenceType) {

		final InvocationHandler h = new InvocationHandler() {
			T serviceReference;

			T getServiceReference() {
				if (null == this.serviceReference) {
					final IService service = ApplicationFramework.this.fetchService(serviceName);
					if (null == service) {
						ApplicationFramework.this.logError("Cannot find service with name " + serviceName, null);
					} else {
						this.serviceReference = (T) service.createReference(locationId);
					}
				}
				return this.serviceReference;
			}

			@Override
			public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
				try {
					final T sr = this.getServiceReference();
					if (null == sr) {
						ApplicationFramework.this.logError("Cannot find service named " + serviceName, null);
					} else {
						final Object result = method.invoke(sr, args);
						return result;
					}
				} catch (final InvocationTargetException ex) {
					throw ex.getCause();
				}
				return null;
			}
		};
		final Object proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { serviceReferenceType }, h);
		return (T) proxy;
	}

	/**
	 * Inject service instances, which should only exist in an 'Application' class
	 *
	 */
	private <T extends IApplication> void injectServiceInstances(final Class<?> class_, final T obj) throws IllegalArgumentException, IllegalAccessException {
		if (null == class_.getSuperclass()) {
			return; // Object.class will have a null superclass, no need to inject anything for Object.class
		} else {
			this.injectServiceInstances(class_.getSuperclass(), obj);
			for (final Field f : class_.getDeclaredFields()) {
				try {
					f.setAccessible(true);
					final ServiceInstance ann = f.getAnnotation(ServiceInstance.class);
					if (null == ann) {
						// do nothing
					} else {
						String serviceId = ann.id();
						if (serviceId.isEmpty()) {
							serviceId = f.getName();
						} else {
							// do nothing
						}
						final IService service = this.createServiceInstance(serviceId, (Class<IService>) f.getType(), obj.afId() + "." + serviceId);
						f.set(obj, service);
					}
				} catch (final Exception ex) {
					this.logError(ex.getMessage(), ex);
				}
			}
		}
	}

	// private void injectServiceReferences(IIdentifiableObject obj) throws IllegalArgumentException, IllegalAccessException {
	//
	// ApplicationCompositionTreeWalker walker = new ApplicationCompositionTreeWalker(this.logger());
	//
	// walker.walkOneAndApply(obj, (tObj, tObjId) -> {
	// try {
	// this.injectServiceReferences(tObj.getClass(), tObj, tObjId);
	// } catch (IllegalArgumentException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IllegalAccessException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// });
	//
	// }

	private void injectServiceReferences(final Class<?> class_, final IIdentifiableObject obj) throws IllegalArgumentException, IllegalAccessException {
		if (null == class_.getSuperclass()) {
			return; // Object.class will have a null superclass, no need to inject anything for Object.class
		} else {
			this.injectServiceReferences(class_.getSuperclass(), obj);
			for (final Field f : class_.getDeclaredFields()) {
				f.setAccessible(true);
				final ServiceReference ann = f.getAnnotation(ServiceReference.class);
				if (null == ann) {
					// do nothing
				} else {
					String serviceName = ann.name();
					if (serviceName.isEmpty()) {
						serviceName = f.getName();
					} else {
						// do nothing
					}

					final Object value = this.createServiceReference(serviceName, obj.afId(), f.getType());
					f.set(obj, value);
				}
			}
		}
	}

	private void injectConfigurationValues(final Class<?> class_, final IIdentifiableObject obj)
			throws IllegalArgumentException, IllegalAccessException, ApplicationFrameworkException, PersistentStoreException {
		if (null == class_.getSuperclass()) {
			return; // Object.class will have a null superclass, no need to inject anything for Object.class
		} else {
			this.injectConfigurationValues(class_.getSuperclass(), obj);
			for (final Field f : class_.getDeclaredFields()) {
				f.setAccessible(true);
				final ConfiguredValue ann = f.getAnnotation(ConfiguredValue.class);
				if (null == ann) {
					// do nothing
				} else {
					String itemId = ann.id();
					if (itemId.isEmpty()) {
						itemId = f.getName();
					} else {
						// do nothing
					}

					String serviceName = ann.service();
					if (serviceName.isEmpty()) {
						serviceName = ApplicationFramework.DEFAULT_CONFIGURATION_SERVICE;
					} else {
						// do nothing
					}

					final IPersistentStore config = (IPersistentStore) this.services.get(serviceName);
					if (null == config) {
						this.logError("no configuration service found", null);
						final Object value = this.createDatatype(f.getType(), ann.defaultValue());
						f.set(obj, value);
					} else {
						final IPersistenceTransaction trans = config.startTransaction();
						final Class<? extends Object> itemType = f.getType();
						String idPath = obj.afId() + "." + itemId;
						// remove the 'application.' first bit of the path
						idPath = idPath.substring(idPath.indexOf('.') + 1);
						final PersistentItemQuery pid = new PersistentItemQuery(idPath);
						Object value = config.retrieve(trans, pid, itemType);
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

	private void injectConfigurationValues(final IApplication applicationObject)
			throws IllegalArgumentException, IllegalAccessException, ApplicationFrameworkException, PersistentStoreException {

		final ApplicationCompositionTreeWalker walker = new ApplicationCompositionTreeWalker(this.logger());
		walker.walkAllAndApply(applicationObject, (obj, objId) -> {
			try {
				this.injectConfigurationValues(obj.getClass(), obj);
			} catch (final Throwable t) {
				this.logError("Failed to inject Configuratioon Values into " + objId, t);
			}
		});

	}

	// private void injectCommandLineArgs(Class<?> class_, IIdentifiableObject obj)
	// throws IllegalArgumentException, IllegalAccessException, ApplicationFrameworkException {
	// if (null == class_.getSuperclass()) {
	// return; // Object.class will have a null superclass, no need to inject anything for Object.class
	// } else {
	// this.injectCommandLineArgs(class_.getSuperclass(), obj);
	// for (Field f : class_.getDeclaredFields()) {
	// f.setAccessible(true);
	// CommandLineArgument ann = f.getAnnotation(CommandLineArgument.class);
	// if (null == ann) {
	// // do nothing
	// } else {
	// String name = ann.name();
	// if (name.isEmpty()) {
	// name = f.getName();
	// } else {
	// // do nothing
	// }
	//
	// String idPath = obj.afId() + "." +name;
	// //remove the 'application.' first bit of the path
	// idPath = idPath.substring(idPath.indexOf('.')+1);
	//
	// Object argValue = this.commandLineHandler.getArgumentValue(ann.group(), idPath); //
	// if (null == argValue && Boolean.class.isAssignableFrom(f.getType())) {
	// argValue = this.commandLineHandler.hasArgument(ann.group(),name);
	// }
	// if (null != argValue) {
	// Object value = this.createDatatype(f.getType(), argValue);
	//
	// f.set(obj, value);
	// }
	// }
	// }
	// }
	// }

	private void injectCommandLineArgs(final IApplication applicationObject)
			throws IllegalArgumentException, IllegalAccessException, ApplicationFrameworkException, PersistentStoreException {

		final ApplicationCompositionTreeWalker walker = new ApplicationCompositionTreeWalker(this.logger());
		walker.walkAllAndApply(applicationObject, (obj, objId) -> {
			try {
				final AnnotationNavigator an = new AnnotationNavigator(obj);

				for (final AnnotationDetailsList<CommandLineGroup> ad : an.getList(CommandLineGroupContainer.class, CommandLineGroup.class)) {
					if (ad.getField().getType().isAssignableFrom(List.class)) {
						final List<String> list = new ArrayList<>();
						for (final CommandLineGroup clg : ad.getAnnotations()) {
							if (this.commandLineHandler.hasGroup(clg.name())) {
								list.add(clg.name());
							}
						}
						ad.getField().set(obj, list);
					}
				}

				for (final AnnotationDetails<CommandLineArgument> ad : an.get(CommandLineArgument.class)) {
					String argName = obj.afId() + ".";
					// remove the 'application.' first bit of the path
					argName = argName.substring(argName.indexOf('.') + 1);

					String name = ad.getAnnotation().name();
					if (name.isEmpty()) {
						name = ad.getField().getName();
						argName += name;
					} else {
						argName = name;
					}

					Object argValue = this.commandLineHandler.getArgumentValue(ad.getAnnotation().group(), argName); //
					if (null == argValue && Boolean.class.isAssignableFrom(ad.getField().getType())) {
						argValue = this.commandLineHandler.hasArgument(ad.getAnnotation().group(), name);
					}
					if (null != argValue) {
						final Object value = this.createDatatype(ad.getField().getType(), argValue);
						ad.getField().set(obj, value);
					}
				}

				// this.injectCommandLineArgs(obj.getClass(), obj);
			} catch (final Throwable t) {
				this.logError("Failed to inject Configuratioon Values into " + objId, t);
			}
		});

	}

	private void injectParts(final IApplication appObj) {

		final ApplicationCompositionTreeWalker walker = new ApplicationCompositionTreeWalker(this.logger());

		walker.build(appObj, (partKind, partClass, partId) -> {
			switch (partKind) {
				case COMPONENT: {
					return this.createComponent((Class<? extends IComponent>) partClass, partId);
				}
				case ACTIVE_OBJECT: {
					return this.createActiveObject((Class<? extends IActiveObject>) partClass, partId);
				}
				case PASSIVE_OBJECT:
					return this.createObject((Class<? extends IActiveObject>) partClass, partId);
				default:
				break;
			}
			return null; // should never happen
		});

	}

	private void injectParts(final IComponent compObj) {

		final ApplicationCompositionTreeWalker walker = new ApplicationCompositionTreeWalker(this.logger());

		walker.build(compObj, (partKind, partClass, partId) -> {
			switch (partKind) {
				case COMPONENT: {
					return this.createComponent((Class<? extends IComponent>) partClass, partId);
				}
				case ACTIVE_OBJECT: {
					return this.createActiveObject((Class<? extends IActiveObject>) partClass, partId);
				}
				case PASSIVE_OBJECT:
					return this.createObject((Class<? extends IActiveObject>) partClass, partId);
				default:
				break;
			}
			return null; // should never happen
		});

	}

	private void injectParts(final IActiveObject obj) {

		final ApplicationCompositionTreeWalker walker = new ApplicationCompositionTreeWalker(this.logger());

		walker.build(obj, (partKind, partClass, partId) -> {
			switch (partKind) {
				case COMPONENT: {
					this.logError("A component may not be a part of an Active Object", null);
				}
				break;
				case ACTIVE_OBJECT: {
					return this.createActiveObject((Class<? extends IActiveObject>) partClass, partId);
				}
				case PASSIVE_OBJECT:
					return this.createObject((Class<? extends IActiveObject>) partClass, partId);
				default:
				break;
			}
			return null; // should never happen
		});

	}

	private void injectParts(final IIdentifiableObject obj) {

		final ApplicationCompositionTreeWalker walker = new ApplicationCompositionTreeWalker(this.logger());

		walker.build(obj, (partKind, partClass, partId) -> {
			switch (partKind) {
				case COMPONENT: {
					this.logError("A component may not be a part of an Simple Object", null);
				}
				break;
				case ACTIVE_OBJECT: {
					this.logError("An Active Object may not be a part of an Simple Object", null);
				}
				break;
				case PASSIVE_OBJECT:
					return this.createObject((Class<? extends IIdentifiableObject>) partClass, partId);
				default:
				break;
			}
			return null; // should never happen
		});

	}

	private void injectPorts(final Class<?> class_, final IComponent obj) throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			InvocationTargetException, NoSuchMethodException, ApplicationFrameworkException {
		if (null == class_.getSuperclass()) {
			return; // Object.class will have a null superclass, no need to inject anything for Object.class
		} else {
			this.injectPorts(class_.getSuperclass(), obj);
			for (final Field f : class_.getDeclaredFields()) {
				f.setAccessible(true);

				final PortInstance ann = f.getAnnotation(PortInstance.class);
				if (null == ann) {
					// do nothing
				} else {
					String portId = ann.id();
					if (portId.isEmpty()) {
						portId = f.getName();
					} else {
						// do nothing
					}

					// Class<? extends Port> fType = (Class<? extends Port>) f.getType();
					final Object value = this.createPort(Port.class, obj.afId() + "." + portId, obj, ann.provides(), ann.requires());
					f.set(obj, value);
				}

			}
		}
	}

	IPort createPort(final Class<? extends IPort> class_, final String id, final IComponent component, final Class<?>[] provides, final Class<?>[] requires)
			throws ApplicationFrameworkException {
		try {
			final Port prt = new Port(id, component);
			this.injectServiceReferences(class_, prt);

			for (final Class<?> interfaceType : provides) {
				final List<Object> providers = (List<Object>) this.findInternalProviderForPort(component, interfaceType, id);
				for (final Object provider : providers) {
					prt.provideProvided((Class<Object>) interfaceType, provider);
				}

			}

			for (final Class<?> interfaceType : requires) {
				prt.requires(interfaceType);
			}

			return prt;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Port " + id, ex);
		}
	}

	<T> List<T> findInternalProviderForPort(final IComponent component, final Class<T> interfaceType, final String portId)
			throws IllegalArgumentException, IllegalAccessException, ApplicationFrameworkException {
		final String shortPortId = portId.substring(portId.lastIndexOf('.') + 1);
		final List<T> providers = new ArrayList<>();
		for (final Field f : component.getClass().getDeclaredFields()) {
			final ProvidesInterfaceForPort[] anns = f.getAnnotationsByType(ProvidesInterfaceForPort.class);
			if (null != anns && anns.length > 0) {
				for (final ProvidesInterfaceForPort ann : anns) {
					if (interfaceType == ann.provides() && shortPortId.equals(ann.portId())) {
						f.setAccessible(true);
						final T comp = (T) f.get(component);
						if (null == comp) {
							throw new ApplicationFrameworkException(
									component.afId() + "." + f.getName() + " cannot be internal provider of " + interfaceType.getSimpleName() + " it is null",
									null);
						} else {
							providers.add(comp);
						}
					}
				}
			}
		}
		if (providers.isEmpty()) {
			// try the component itself
			if (interfaceType.isInstance(component)) {
				providers.add((T) component);
			} else {
				throw new ApplicationFrameworkException(
						"Failed find internal provider of " + interfaceType.getSimpleName() + " for component " + component.afId(), null);
			}
		}
		return providers;
	}
}
