package net.akehurst.application.framework.realisation;

public class ArgumentDefinition {

	public ArgumentDefinition(String group, String name, Class<?> type, boolean required, boolean hasValue, Object defaultValue, String description) {
		super();
		this.group = group;
		this.name = name;
		this.type = type;
		this.required = required;
		this.hasValue = hasValue;
		this.defaultValue = defaultValue;
		this.description = description;
	}
	
	String group;
	public String getGroup() {
		return group;
	}
	
	String name;
	public String getName() {
		return name;
	}
	
	Class<?> type;
	public Class<?> getType() {
		return this.type;
	}
	
	boolean required;
	public boolean getRequired() {
		return required;
	}
	
	boolean hasValue;
	public boolean getHasValue() {
		return hasValue;
	}
	
	Object defaultValue;
	public Object getDefaultValue() {
		return defaultValue;
	}

	String description;
	public String getDescription() {
		return this.description;
	}
}
