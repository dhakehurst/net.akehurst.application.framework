package net.akehurst.application.framework.common;

import java.util.Set;

public interface IPort extends IIdentifiableObject {

	/**
	 * Set of objects internal to the component that provide the interface
	 * @param interfaceType
	 * @return
	 */
	<T> Set<T> getProvided(Class<T> interfaceType);

	Set<Class<?>> getRequired();

	<T> void provideRequired(Class<T> interfaceType, T provider);

	/**
	 * returns an object that implements the required interfaceType normally a 'connection' to something outside the component probably something 'provided' by
	 * a different Component.Port
	 * 
	 * @param interfaceType
	 * @return
	 */
	<T> T out(Class<T> interfaceType);

	void connect(IPort other);
}
