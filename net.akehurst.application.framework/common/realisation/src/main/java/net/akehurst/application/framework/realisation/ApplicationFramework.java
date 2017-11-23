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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IActiveObject;
import net.akehurst.application.framework.common.IApplication;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.IComponent;
import net.akehurst.application.framework.common.IConfiguration;
import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.IService;
import net.akehurst.application.framework.common.annotations.declaration.ProvidesInterfaceForPort;
import net.akehurst.application.framework.common.annotations.instance.CommandLineArgument;
import net.akehurst.application.framework.common.annotations.instance.CommandLineGroup;
import net.akehurst.application.framework.common.annotations.instance.CommandLineGroupContainer;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.application.framework.technology.interfacePersistence.PersistentStoreException;
import net.akehurst.application.framework.technology.logging.console.ConsoleLogger;
import net.akehurst.holser.reflect.BetterMethodFinder;

public class ApplicationFramework implements IApplicationFramework, IService {

	public static <T extends IApplication> T start(final Class<T> applicationClass, final String[] arguments) {
		try {
			final IApplicationFramework af = new ApplicationFramework("af", "af");
			final T app = af.createApplication(applicationClass, "application", arguments);
			af.activate(app);
			return app;
		} catch (final Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	public static final String DEFAULT_CONFIGURATION_SERVICE = "configuration";

	private final String afId;
	protected ICommandLineHandler commandLineHandler;

	/**
	 * Other parts of the FW expect the ApplicationFramework to have a serviceName == 'af. e.g. ...persistence.filesystem.HJsonFile
	 *
	 * @param id
	 * @param serviceName
	 */
	public ApplicationFramework(final String afId, final String serviceName) {
		this.afId = afId;
		this.services = new HashMap<>();
		this.services.put(serviceName, this);
		this.commandLineHandler = new CommandLineHandler("[a-zA-Z0-9_-]+", "--");
		// this.commandLineOptionGroups = new HashMap<>();
	}

	// --------- IIdentifiable ---------

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
			throw new ApplicationFrameworkException("Failed to create Active Object", ex);
		}
	}

	public static Class<?> getClass(final Type type) {
		if (type instanceof Class) {
			return (Class) type;
		} else if (type instanceof ParameterizedType) {
			return ApplicationFramework.getClass(((ParameterizedType) type).getRawType());
		} else if (type instanceof GenericArrayType) {
			final Type componentType = ((GenericArrayType) type).getGenericComponentType();
			final Class<?> componentClass = ApplicationFramework.getClass(componentType);
			if (componentClass != null) {
				return Array.newInstance(componentClass, 0).getClass();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public <T> T createDatatype(final Type type, final Object... constructorArgs) throws ApplicationFrameworkException {
		try {
			final Class<?> class_ = ApplicationFramework.getClass(type);
			if (class_.isPrimitive()) {
				final Object value = constructorArgs[0];
				if (Boolean.class == class_ || class_ == Boolean.TYPE) {
					if (value instanceof String) {
						return (T) Boolean.valueOf((String) value);
					} else if (value instanceof Boolean) {
						return (T) (Boolean) value;
					} else {
						throw new ApplicationFrameworkException("Failed to create Datatype, cannot convert value to Boolean " + value, null);
					}
				} else if (Byte.class == class_ || class_ == Byte.TYPE) {
					if (value instanceof String) {
						return (T) Byte.valueOf((String) value);
					} else if (value instanceof Byte) {
						return (T) (Byte) value;
					} else {
						throw new ApplicationFrameworkException("Failed to create Datatype, cannot convert value to Byte " + value, null);
					}
				} else if (Short.class == class_ || class_ == Short.TYPE) {
					if (value instanceof String) {
						return (T) Short.valueOf((String) value);
					} else if (value instanceof Short) {
						return (T) (Short) value;
					} else {
						throw new ApplicationFrameworkException("Failed to create Datatype, cannot convert value to Short " + value, null);
					}
				} else if (Integer.class == class_ || class_ == Integer.TYPE) {
					if (value instanceof String) {
						return (T) Integer.valueOf((String) value);
					} else if (value instanceof Integer) {
						return (T) (Integer) value;
					} else {
						throw new ApplicationFrameworkException("Failed to create Datatype, cannot convert value to Integer " + value, null);
					}
				} else if (Long.class == class_ || class_ == Long.TYPE) {
					if (value instanceof String) {
						return (T) Long.valueOf((String) value);
					} else if (value instanceof Long) {
						return (T) (Long) value;
					} else {
						throw new ApplicationFrameworkException("Failed to create Datatype, cannot convert value to Long " + value, null);
					}
				} else if (Float.class == class_ || class_ == Float.TYPE) {
					if (value instanceof String) {
						return (T) Float.valueOf((String) value);
					} else if (value instanceof Float) {
						return (T) (Float) value;
					} else {
						throw new ApplicationFrameworkException("Failed to create Datatype, cannot convert value to Float " + value, null);
					}
				} else if (Double.class == class_ || class_ == Double.TYPE) {
					if (value instanceof String) {
						return (T) Double.valueOf((String) value);
					} else if (value instanceof Double) {
						return (T) (Double) value;
					} else {
						throw new ApplicationFrameworkException("Failed to create Datatype, cannot convert value to Double " + value, null);
					}
				} else {
					throw new ApplicationFrameworkException("Failed to create Datatype, unknown primitive type " + class_, null);
				}
			} else if (class_.isEnum()) {
				final Object value = constructorArgs[0];
				if (value instanceof String) {
					return (T) Enum.valueOf((Class<? extends Enum>) class_, (String) value);
				} else if (class_.isInstance(value)) {
					return (T) value;
				} else {
					throw new ApplicationFrameworkException("Failed to create Datatype, cannot convert value to Enum " + value, null);
				}
			} else if (List.class.isAssignableFrom(class_)) {
				if (1 == constructorArgs.length) {
					final Object value = constructorArgs[0];
					if (value instanceof String) {
						final String strValue = (String) value;
						if (strValue.isEmpty()) {
							return (T) new ArrayList<>();
						} else {
							// TODO parse the string
							throw new UnsupportedOperationException("TODO");
						}
					} else {
						return (T) Arrays.asList(constructorArgs);
					}
				} else {
					return (T) Arrays.asList(constructorArgs);
				}
			} else {

				final BetterMethodFinder bmf = new BetterMethodFinder(class_);
				final Constructor<T> cons = bmf.findConstructor(constructorArgs);
				final T obj = cons.newInstance(constructorArgs);
				return obj;
			}
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Datatype", ex);
		}
	}

	@Override
	public <T extends IService> T injectIntoService(final T obj) throws ApplicationFrameworkException {
		try {
			this.injectServiceReferences(obj.getClass(), obj);
			this.injectParts(obj);
			this.injectCommandLineArgs(obj.getClass(), obj);
			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Service", ex);
		}
	}

	@Override
	public <T extends IIdentifiableObject> T injectIntoSimpleObject(final T object) throws ApplicationFrameworkException {
		try {
			this.injectServiceReferences(object.getClass(), object);
			this.injectParts(object);
			return object;
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

	protected void defineCommandLine(final IApplication applicationObject) {
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

	@Override
	public void outputCommandLineHelp() {
		final String help = this.commandLineHandler.getHelp();
		// HelpFormatter formatter = new HelpFormatter();
		// for (Options opts : this.commandLineHandler.getGoups()) {
		// formatter.printHelp(100, "<application>", "", opts, "", true);
		// }
		this.logger().log(LogLevel.INFO, System.lineSeparator() + help);
	}

	protected ILogger logger() {
		if (null == this.fetchService("logger")) {
			this.services.put("logger", new ConsoleLogger("logger"));
		}
		final ILogger logger = this.createServiceReference("logger", "af.logger", ILogger.class);
		return logger;
	}

	protected void logError(final String message, final Throwable t) {
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

	protected Map<String, IService> services;

	protected IService fetchService(final String name) {
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
	protected <T extends IApplication> void injectServiceInstances(final Class<?> class_, final T obj) throws IllegalArgumentException, IllegalAccessException {
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
						final Class<IService> fieldType = (Class<IService>) f.getType();
						final IService service = this.createServiceInstance(serviceId, fieldType, obj.afId() + "." + serviceId);
						f.set(obj, service);
					}
				} catch (final Exception ex) {
					this.logError(ex.getMessage(), ex);
				}
			}
		}
	}

	protected void injectServiceReferences(final Class<?> class_, final IIdentifiableObject obj) throws IllegalArgumentException, IllegalAccessException {
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
			throws IllegalArgumentException, IllegalAccessException, ApplicationFrameworkException {
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

					final IConfiguration config = (IConfiguration) this.services.get(serviceName);
					if (null == config) {
						this.logError("no configuration service found, using default values", null);

						final Class<? extends Object> itemType = f.getType();
						Object value = null;
						if (itemType.isAssignableFrom(ArrayList.class)) {
							value = new ArrayList<>();
						} else if (itemType.isAssignableFrom(HashMap.class)) {
							value = new HashMap<>();
						} else {
							value = this.createDatatype(f.getType(), ann.defaultValue());
						}
						f.set(obj, value);
					} else {
						final Type itemType = f.getGenericType();
						String idPath = obj.afId() + "." + itemId;
						// TODO: get rid of 'application' start over all
						// remove the 'application.' first bit of the path
						idPath = idPath.substring(idPath.indexOf('.') + 1);
						final Object value = config.fetchValue(itemType, idPath, ann.defaultValue());
						// if (null == value) {
						// if (itemType.isAssignableFrom(ArrayList.class)) {
						// value = new ArrayList<>();
						// } else if (itemType.isAssignableFrom(HashMap.class)) {
						// value = new HashMap<>();
						// } else {
						// value = this.createDatatype(f.getType(), ann.defaultValue());
						// }
						// }
						f.set(obj, value);
					}
				}
			}
		}
	}

	protected void injectConfigurationValues(final IApplication applicationObject)
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

	protected void injectCommandLineArgs(final Class<?> class_, final IIdentifiableObject obj) {
		final String objId = obj.afId();
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
	}

	protected void injectCommandLineArgs(final IApplication applicationObject)
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

	protected void injectParts(final IApplication appObj) {

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

	private void injectParts(final IService service) {

		final ApplicationCompositionTreeWalker walker = new ApplicationCompositionTreeWalker(this.logger());

		walker.build(service, (partKind, partClass, partId) -> {
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

	protected void injectParts(final IComponent compObj) {

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
			return null;
		});

	}

	protected void injectParts(final IIdentifiableObject obj) {

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

	protected void injectPorts(final Class<?> class_, final IComponent obj) throws IllegalArgumentException, IllegalAccessException, InstantiationException,
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

					final PortContract contracts[] = f.getAnnotationsByType(PortContract.class);
					// Class<? extends Port> fType = (Class<? extends Port>) f.getType();
					final Object value = this.createPort(Port.class, obj.afId() + "." + portId, obj, contracts);
					f.set(obj, value);
				}

			}
		}
	}

	IPort createPort(final Class<? extends IPort> class_, final String id, final IComponent component, final PortContract[] contracts) // final Class<?>[]
																																		// provides, final
																																		// Class<?>[] requires)
			throws ApplicationFrameworkException {
		try {
			final Port prt = new Port(id, component);
			this.injectServiceReferences(class_, prt);

			for (final PortContract contract : contracts) {
				final Class<?> interfaceType = contract.provides();
				if (Void.class != interfaceType) {
					final List<Object> providers = (List<Object>) this.findInternalProviderForPort(component, interfaceType, id);
					for (final Object provider : providers) {
						if (provider instanceof Class) {
							prt.provideProvided((Class<Object>) interfaceType, null);
						} else {
							prt.provideProvided((Class<Object>) interfaceType, provider);
						}
					}
				}
			}

			for (final PortContract contract : contracts) {
				final Class<?> interfaceType = contract.requires();
				if (Void.class != interfaceType) {
					prt.requires(interfaceType);
				}
			}

			return prt;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Port " + id, ex);
		}
	}

	<T> List<?> findInternalProviderForPort(final IComponent component, final Class<T> interfaceType, final String portId)
			throws IllegalArgumentException, IllegalAccessException, ApplicationFrameworkException {
		final String shortPortId = portId.substring(portId.lastIndexOf('.') + 1);
		final List<Object> providers = new ArrayList<>();
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
				providers.add(component);
			} else {
				// hope it gets connected later
				providers.add(interfaceType);
				// throw new ApplicationFrameworkException(
				// "Failed find internal provider of " + interfaceType.getSimpleName() + " for component " + component.afId(), null);
			}
		}
		return providers;
	}

	@Override
	public <T extends IApplication> void activate(final T object) {
		object.afStart();
		// final ApplicationCompositionTreeWalker walker = new ApplicationCompositionTreeWalker(this.logger());
		//
		// walker.build(object, (partKind, partClass, partId) -> {
		// switch (partKind) {
		// case COMPONENT: {
		// return this.createComponent((Class<? extends IComponent>) partClass, partId);
		// }
		// case ACTIVE_OBJECT: {
		// return this.createActiveObject((Class<? extends IActiveObject>) partClass, partId);
		// }
		// case PASSIVE_OBJECT:
		// return this.createObject((Class<? extends IActiveObject>) partClass, partId);
		// default:
		// break;
		// }
		// return null; // should never happen
		// });
	}

}
