package net.akehurst.application.framework.technology.authentication;

import net.akehurst.application.framework.common.AbstractDataType;

public class TechUserDetails extends AbstractDataType {
	
	public TechUserDetails(String name) {
		super(name);
		this.name = name;
	}

	String name;
	public String getName() {
		return this.name;
	}
}
