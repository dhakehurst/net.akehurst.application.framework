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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.akehurst.application.framework.common.IComponent;
import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class Port implements IPort {

	public Port(String id, IComponent owner) {
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


	public String afId() {
		return this.owner.afId()+"."+this.id;
	}
	
	
	Map<Class<?>, Set<Object>> provided;
	
	public <T> void provideProvided(Class<T> interfaceType, T provider) {
		Set<Object> set = this.provided.get(interfaceType);
		if (null==set) {
			set = new HashSet<>();
			this.provided.put(interfaceType, set);
		}
		set.add(provider);
	}
	public <T> Set<T> getProvided(Class<T> interfaceType) {
		Set<T> res = (Set<T>)this.provided.get(interfaceType);
		if (null==res) {
			return new HashSet<>();
		} else {
			return res;
		}
	}
	
	
	Map<Class<?>, Set<Object>> required;
	
	public <T> Port requires(Class<T> interfaceType) {
		this.required.put(interfaceType, null);
		return this;
	}
	
	public Set<Class<?>> getRequired() {
		return this.required.keySet();
	}
	
	public <T> void provideRequired(Class<T> interfaceType, T provider) {
		Set<Object> set = this.required.get(interfaceType);
		if (null==set) {
			set = new HashSet<>();
			this.required.put(interfaceType, set);
		}
		set.add(provider);
	}
	
	public <T> T out(Class<T> interfaceType) {
		
		InvocationHandler h = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				try {
					logger.log(LogLevel.TRACE, method.getName());
					Object result = null;
					 //get these here to delay resolving the object until moment of call
					Set<Object> set = required.get(interfaceType);
					for(Object provider: set) {
						result = method.invoke(provider, args);
					}
					return result;
				} catch (InvocationTargetException ex) {
					throw ex.getCause();
				}
			}
		};
		Object proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{interfaceType}, h);
		return (T)proxy;
	}
	
	public void connect(IPort other) {
		
		for(Class<?> req: this.getRequired()) {
			Class<Object> t = (Class<Object>) req;
			Set<Object> objs = (Set<Object>)other.getProvided(req);
			for(Object o: objs) {
				this.provideRequired(t, o);
			}
		}
		
		for(Class<?> req: other.getRequired()) {
			Class<Object> t = (Class<Object>) req;
			Set<Object> objs = (Set<Object>)this.getProvided(req);
			for(Object o: objs) {
				other.provideRequired(t, o);
			}
		}
		
	}
	
	@Override
	public int hashCode() {
		return this.afId().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Port) {
			Port other = (Port)obj;
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
