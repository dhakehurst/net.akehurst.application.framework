package net.akehurst.application.framework.computational.interfaceUser.authentication;

import net.akehurst.application.framework.common.AbstractDataType;

public class UserDetails extends AbstractDataType {

	public UserDetails(String name) {
		super(name);
		this.name = name;
	}
	
	String name;
	public String getName() {
		return this.name;
	}
}
