package net.akehurst.application.framework.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Port {

	public Port(String id, IComponent owner) {
		this.id = id;
		this.owner = owner;
		this.owner.afAddPort(this);
		this.provided = new HashMap<>();
		this.required = new HashMap<>();
	}
	String id;
	IComponent owner;

	public String afId() {
		return this.owner.afId()+"."+this.id;
	}
	
	
	Map<Class<?>, Object> provided;
	
	public <T> Port provides(Class<T> interfaceType, T provider) {
		this.provided.put(interfaceType, provider);
		return this;
	}
	public <T> T getProvided(Class<T> interfaceType) {
		return (T)this.provided.get(interfaceType);
	}
	
	
	Map<Class<?>, Object> required;
	
	public <T> Port requires(Class<T> interfaceType) {
		this.required.put(interfaceType, null);
		return this;
	}
	
	public Set<Class<?>> getRequired() {
		return this.required.keySet();
	}
	
	public <T> void setRequired(Class<T> interfaceType, T provider) {
		this.required.put(interfaceType, provider);
	}
	
	public <T> T out(Class<T> interfaceType) {
		return (T)this.required.get(interfaceType);
	}
	
	public void connect(Port other) {
		
		for(Class<?> req: this.getRequired()) {
			Class<Object> t = (Class<Object>) req;
			Object o = other.getProvided(req);
			if (null==o) {
				//
			} else {
				this.setRequired(t, o);
			}
		}
		
		for(Class<?> req: other.getRequired()) {
			Class<Object> t = (Class<Object>) req;
			Object o = this.getProvided(req);
			if (null==o) {
				//
			} else {
				other.setRequired(t, o);
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
