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

import java.lang.reflect.Type;

public interface IApplicationFramework {

    <T extends IApplication> T createApplication(Class<T> class_, String id, String[] arguments) throws ApplicationFrameworkException;

    void outputCommandLineHelp();

    <T extends IService> T createServiceInstance(String serviceName, Class<T> class_, String id) throws ApplicationFrameworkException;

    <T extends IComponent> T createComponent(Class<T> class_, String id) throws ApplicationFrameworkException;

    <T extends ActiveObject> T createActiveObject(Class<T> class_, String id) throws ApplicationFrameworkException;

    <T> T createDatatype(Type class_, Object... constructorArgs) throws ApplicationFrameworkException;

    <T extends IIdentifiableObject> T createObject(Class<T> class_, Object... constructorArgs) throws ApplicationFrameworkException;

    <T extends IService> T injectIntoService(T object) throws ApplicationFrameworkException;

    <T extends ActiveObject> T injectIntoActiveObject(T object) throws ApplicationFrameworkException;

    <T extends IIdentifiableObject> T injectIntoSimpleObject(T object) throws ApplicationFrameworkException;

    <T extends IApplication> void activate(final T object);
}
