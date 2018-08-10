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

import java.util.Set;

public interface IPort extends IIdentifiableObject {

    /**
     * Set of objects internal to the component that provide the interface
     *
     * @param interfaceType
     * @return
     */
    <T> Set<T> getProvided(Class<T> interfaceType);

    Set<Class<?>> getRequired();

    <T> void provideRequired(Class<T> interfaceType, T provider);

    /**
     * returns a proxy object that implements the providedInterfaceType by calling methods on all the internal connections to the port that 'provide' the
     * required interface
     *
     * @param interfaceType
     * @return
     */
    <T> T in(final Class<T> providedInterfaceType);

    /**
     * returns a proxy object that implements the requiredInterfaceType by calling methods on all the external connections to the port that 'provide' the
     * required interface
     *
     * @param requiredInterfaceType
     * @return
     */
    <T> T out(Class<T> requiredInterfaceType);

    /**
     * connect one port to another at the same level, i.e. provides is matched to requires in each direction
     */
    void connect(IPort other);

    /**
     * connect one port to another at the same level, i.e. provides is matched to to @ExternalConnection fields and requires matched to implemented interfaces
     */
    void connect(IIdentifiableObject other);

    /**
     * connect one port to another internal port, i.e. provides is matched to provides and requires matched to requires
     */
    void connectInternal(IPort other);

    /**
     * connect one port to an internal object, i.e. provides is matched to implemented interface and requires matched to @ExternalConnection fields
     */
    void connectInternal(IIdentifiableObject other);

}
