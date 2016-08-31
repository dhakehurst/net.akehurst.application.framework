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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IComponent;
import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class Port implements IPort {

	public Port(final String id, final IComponent owner) {
		this.id = id;
		this.owner = owner;

		this.owner.afAddPort(this);
		this.provided = new HashMap<>();
		this.required = new HashMap<>();
	}

	String id;
	IComponent owner;

	@ServiceReference
	ILogger logger;

	@Override
	public String afId() {
		return this.id;
	}

	Map<Class<?>, Set<Object>> provided;

	public <T> void provideProvided(final Class<T> interfaceType, final T provider) {
		Set<Object> set = this.provided.get(interfaceType);
		if (null == set) {
			set = new HashSet<>();
			this.provided.put(interfaceType, set);
		}
		set.add(provider);
	}

	@Override
	public <T> Set<T> getProvided(final Class<T> interfaceType) {
		final Set<T> res = (Set<T>) this.provided.get(interfaceType);
		if (null == res) {
			return new HashSet<>();
		} else {
			return res;
		}
	}

	Map<Class<?>, Set<Object>> required;

	public <T> Port requires(final Class<T> interfaceType) {
		this.required.put(interfaceType, null);
		return this;
	}

	@Override
	public Set<Class<?>> getRequired() {
		return this.required.keySet();
	}

	@Override
	public <T> void provideRequired(final Class<T> interfaceType, final T provider) {
		Set<Object> set = this.required.get(interfaceType);
		if (null == set) {
			set = new HashSet<>();
			this.required.put(interfaceType, set);
		}
		set.add(provider);
	}

	@Override
	public <T> T out(final Class<T> interfaceType) {

		final InvocationHandler h = (proxy, method, args) -> {
			try {
				Port.this.logger.log(LogLevel.TRACE, method.getName());
				Object result = null;
				// get these here to delay resolving the object until moment of call
				final Set<Object> set = Port.this.required.get(interfaceType);
				if (null == set) {
					throw new ApplicationFrameworkException(
							"Port " + Port.this.afId() + " requires interface " + interfaceType + " and it has not been provided", null);
				}
				for (final Object provider : set) {
					if (null == provider) {
						throw new ApplicationFrameworkException(
								"Port " + Port.this.afId() + " requires interface " + interfaceType + " and it has not been provided", null);
					} else {
						result = method.invoke(provider, args);
					}
				}
				return result;
			} catch (final InvocationTargetException ex) {
				throw ex.getCause();
			}
		};
		final Object proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { interfaceType }, h);
		return (T) proxy;
	}

	@Override
	public void connect(final IPort other) {

		for (final Class<?> req : this.getRequired()) {
			final Class<Object> t = (Class<Object>) req;
			final Set<Object> objs = (Set<Object>) other.getProvided(req);
			for (final Object o : objs) {
				this.provideRequired(t, o);
			}
		}

		for (final Class<?> req : other.getRequired()) {
			final Class<Object> t = (Class<Object>) req;
			final Set<Object> objs = (Set<Object>) this.getProvided(req);
			for (final Object o : objs) {
				other.provideRequired(t, o);
			}
		}

	}

	@Override
	public int hashCode() {
		return this.afId().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Port) {
			final Port other = (Port) obj;
			return this.afId().equals(other.afId());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return this.afId();
	}
}
