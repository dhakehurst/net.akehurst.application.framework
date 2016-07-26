package net.akehurst.application.framework.realisation;

public interface ICommandLineHandler {

	void defineGroup(String groupName);
	
	void defineArgument(String group, String name, boolean required, boolean hasValue, Object defaultValue);
	
	void parse(String[] args);

	String getArgumentValue(String group, String name);

	boolean hasArgument(String group, String name);
}
