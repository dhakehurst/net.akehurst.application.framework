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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import org.easymock.EasyMock;

import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IActiveObject;
import net.akehurst.application.framework.common.IComponent;
import net.akehurst.application.framework.common.IIdentifiableObject;
import net.akehurst.application.framework.common.IService;
import net.akehurst.application.framework.common.test.annotation.MockServiceInstance;
import net.akehurst.application.framework.realisation.ApplicationFramework;
import net.akehurst.application.framework.test.ITestEnvironment;
import net.akehurst.holser.reflect.BetterMethodFinder;

public class TestFramework extends ApplicationFramework {

	public static <T extends ITestEnvironment> T test(final Class<T> applicationClass, final Map<String, IService> services, final String[] arguments) {
		try {
			final TestFramework tf = new TestFramework("af", "af");
			final T tEnv = tf.createTestEnvironment(applicationClass, services, arguments);
			tEnv.afStart();
			return tEnv;
		} catch (final Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	public TestFramework(final String id, final String serviceName) {
		super(id, serviceName);
	}

	public <T extends ITestEnvironment> T createTestEnvironment(final Class<T> class_, final Map<String, IService> services, final String[] arguments)
			throws ApplicationFrameworkException {
		try {
			final BetterMethodFinder bmf = new BetterMethodFinder(class_);
			final Constructor<T> cons = bmf.findConstructor();
			cons.setAccessible(true);
			final T testEnv = cons.newInstance(new Object[] {});

			this.injectServiceInstances(class_, testEnv);
			this.injectMockServiceInstances(class_, testEnv);
			super.services.putAll(services);

			this.injectServiceReferences(testEnv.getClass(), testEnv);
			this.injectParts(testEnv);
			this.injectMockParts(testEnv);

			this.defineCommandLine(testEnv);

			// appObj.defineArguments();
			this.commandLineHandler.parse(arguments);
			// appObj.parseArguments();
			this.injectConfigurationValues(testEnv);
			this.injectCommandLineArgs(testEnv);

			testEnv.afConnectParts();

			return testEnv;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Application", ex);
		}
	}

	public <T extends IService> T createMockServiceInstance(final String serviceName, final Class<T> class_, final String id)
			throws ApplicationFrameworkException {
		try {
			final T obj = EasyMock.niceMock(class_);

			this.services.put(serviceName, obj);
			// this.injectIntoService(obj);
			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Service", ex);
		}
	}

	public <T extends IComponent> T createTestComponent(final Class<T> class_, final String id) throws ApplicationFrameworkException {
		try {
			final BetterMethodFinder bmf = new BetterMethodFinder(class_);
			final Constructor<T> cons = bmf.findConstructor(String.class);
			final T obj = cons.newInstance(id);
			this.injectServiceReferences(obj.getClass(), obj);
			this.injectParts(obj);
			this.injectMockParts(obj);
			this.injectPorts(obj.getClass(), obj);
			// this.injectConfigurationValues(obj);
			// this.injectCommandLineArgs(obj);
			obj.afConnectParts();
			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Component " + id, ex);
		}
	}

	public <T extends IComponent> T createMockComponent(final Class<T> class_, final String id) throws ApplicationFrameworkException {
		try {
			final T obj = EasyMock.mock(class_);

			// this.injectServiceReferences(obj.getClass(), obj);
			// this.injectParts(obj);
			// this.injectPorts(obj.getClass(), obj);
			// // this.injectConfigurationValues(obj);
			// // this.injectCommandLineArgs(obj);
			// obj.afConnectParts();

			// TODO: handle mock ports?
			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Component " + id, ex);
		}
	}

	public <T extends IActiveObject> T createTestActiveObject(final Class<T> class_, final String id) throws ApplicationFrameworkException {
		try {
			final BetterMethodFinder bmf = new BetterMethodFinder(class_);
			final Constructor<T> cons = bmf.findConstructor(String.class);
			final T obj = cons.newInstance(id);
			this.injectServiceReferences(obj.getClass(), obj);
			this.injectParts(obj);
			this.injectMockParts(obj);
			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Active Object", ex);
		}
	}

	public <T extends IActiveObject> T createMockActiveObject(final Class<T> class_, final String id) throws ApplicationFrameworkException {
		try {
			final T obj = EasyMock.mock(class_);

			// this.injectIntoActiveObject(obj);
			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("Failed to create Basic Object", ex);
		}
	}

	public <T extends IIdentifiableObject> T createTestObject(final Class<T> class_, final Object... constructorArgs) throws ApplicationFrameworkException {
		try {
			final BetterMethodFinder bmf = new BetterMethodFinder(class_);
			final Constructor<T> cons = bmf.findConstructor(constructorArgs);
			cons.setAccessible(true);
			final T obj = cons.newInstance(constructorArgs);

			this.injectServiceReferences(obj.getClass(), obj);
			this.injectParts(obj);
			this.injectMockParts(obj);

			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("cannot create object with class " + class_, ex);
		}
	}

	public <T extends IIdentifiableObject> T createMockObject(final Class<T> class_, final Object... constructorArgs) throws ApplicationFrameworkException {
		try {
			final T obj = EasyMock.mock(class_);

			// this.injectServiceReferences(obj.getClass(), obj);
			// this.injectParts(obj);

			return obj;
		} catch (final Exception ex) {
			throw new ApplicationFrameworkException("cannot create object with class " + class_, ex);
		}
	}

	private <T extends ITestEnvironment> void injectMockServiceInstances(final Class<?> class_, final T obj)
			throws IllegalArgumentException, IllegalAccessException {
		if (null == class_.getSuperclass()) {
			return; // Object.class will have a null superclass, no need to inject anything for Object.class
		} else {
			this.injectMockServiceInstances(class_.getSuperclass(), obj);
			for (final Field f : class_.getDeclaredFields()) {
				try {
					f.setAccessible(true);
					final MockServiceInstance ann = f.getAnnotation(MockServiceInstance.class);
					if (null == ann) {
						// do nothing
					} else {
						String serviceId = ann.id();
						if (serviceId.isEmpty()) {
							serviceId = f.getName();
						} else {
							// do nothing
						}
						final IService service = this.createMockServiceInstance(serviceId, (Class<IService>) f.getType(), obj.afId() + "." + serviceId);
						f.set(obj, service);
					}
				} catch (final Exception ex) {
					this.logError(ex.getMessage(), ex);
				}
			}
		}
	}

	protected void injectMockParts(final ITestEnvironment testEnv) {

		final TestCompositionTreeWalker walker = new TestCompositionTreeWalker(this.logger());

		walker.build(testEnv, (partKind, partClass, partId) -> {
			switch (partKind) {
				case MOCK_COMPONENT: {
					return this.createMockComponent((Class<? extends IComponent>) partClass, partId);
				}
				case MOCK_ACTIVE_OBJECT: {
					return this.createMockActiveObject((Class<? extends IActiveObject>) partClass, partId);
				}
				case MOCK_PASSIVE_OBJECT:
					return this.createMockObject((Class<? extends IActiveObject>) partClass, partId);
				case TEST_ACTIVE_OBJECT: {
					return this.createTestActiveObject((Class<? extends IActiveObject>) partClass, partId);
				}
				case TEST_COMPONENT: {
					return this.createTestComponent((Class<? extends IComponent>) partClass, partId);
				}
				case TEST_PASSIVE_OBJECT: {
					return this.createTestObject((Class<? extends IActiveObject>) partClass, partId);

				}
				default:
				break;
			}
			return null; // should never happen
		});

	}

	protected void injectMockParts(final IComponent compObj) {

		final TestCompositionTreeWalker walker = new TestCompositionTreeWalker(this.logger());

		walker.build(compObj, (partKind, partClass, partId) -> {
			switch (partKind) {
				case MOCK_COMPONENT: {
					return this.createMockComponent((Class<? extends IComponent>) partClass, partId);
				}
				case MOCK_ACTIVE_OBJECT: {
					return this.createMockActiveObject((Class<? extends IActiveObject>) partClass, partId);
				}
				case MOCK_PASSIVE_OBJECT:
					return this.createMockObject((Class<? extends IActiveObject>) partClass, partId);
				case TEST_ACTIVE_OBJECT: {
					return this.createTestActiveObject((Class<? extends IActiveObject>) partClass, partId);
				}
				case TEST_COMPONENT: {
					return this.createTestComponent((Class<? extends IComponent>) partClass, partId);
				}
				case TEST_PASSIVE_OBJECT: {
					return this.createTestObject((Class<? extends IActiveObject>) partClass, partId);

				}
				default:
				break;
			}
			return null; // should never happen
		});

	}

	protected void injectMockParts(final IActiveObject obj) {
		final TestCompositionTreeWalker walker = new TestCompositionTreeWalker(this.logger());
		walker.build(obj, (partKind, partClass, partId) -> {
			switch (partKind) {
				case MOCK_COMPONENT: {
					this.logError("A component may not be a part of an Active Object", null);
				}
				break;
				case MOCK_ACTIVE_OBJECT: {
					return this.createMockActiveObject((Class<? extends IActiveObject>) partClass, partId);
				}
				case MOCK_PASSIVE_OBJECT:
					return this.createMockObject((Class<? extends IActiveObject>) partClass, partId);
				case TEST_ACTIVE_OBJECT: {
					return this.createTestActiveObject((Class<? extends IActiveObject>) partClass, partId);
				}
				case TEST_COMPONENT: {
					this.logError("A component may not be a part of an Active Object", null);
				}
				break;
				case TEST_PASSIVE_OBJECT: {
					return this.createTestObject((Class<? extends IActiveObject>) partClass, partId);

				}
				default:
				break;
			}
			return null;
		});
	}

	protected void injectMockParts(final IIdentifiableObject obj) {
		final TestCompositionTreeWalker walker = new TestCompositionTreeWalker(this.logger());
		walker.build(obj, (partKind, partClass, partId) -> {
			switch (partKind) {
				case MOCK_COMPONENT: {
					this.logError("A component may not be a part of an Simple Object", null);
				}
				break;
				case MOCK_ACTIVE_OBJECT: {
					this.logError("An Active Object may not be a part of an Simple Object", null);
				}
				break;
				case MOCK_PASSIVE_OBJECT:
					return this.createMockObject((Class<? extends IActiveObject>) partClass, partId);
				case TEST_ACTIVE_OBJECT: {
					return this.createTestActiveObject((Class<? extends IActiveObject>) partClass, partId);
				}
				case TEST_COMPONENT: {
					this.logError("A component may not be a part of an Active Object", null);
				}
				break;
				case TEST_PASSIVE_OBJECT: {
					return this.createTestObject((Class<? extends IActiveObject>) partClass, partId);

				}
				default:
				break;
			}
			return null;
		});
	}
}
