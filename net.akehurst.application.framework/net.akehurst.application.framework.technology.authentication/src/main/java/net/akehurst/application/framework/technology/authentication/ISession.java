package net.akehurst.application.framework.technology.authentication;

public interface ISession {

	IUser getUser();
	
	<T> T get(String key);
	<T> T remove(String key);
	ISession put(String key, Object value);
}
