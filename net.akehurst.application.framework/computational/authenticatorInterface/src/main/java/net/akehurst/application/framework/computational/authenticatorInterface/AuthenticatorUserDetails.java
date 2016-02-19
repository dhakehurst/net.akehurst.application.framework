package net.akehurst.application.framework.computational.authenticatorInterface;

import net.akehurst.application.framework.common.AbstractDataType;

public class AuthenticatorUserDetails extends AbstractDataType {
	
	public AuthenticatorUserDetails(String name) {
		super(name);
		this.name = name;
	}

	String name;
	public String getName() {
		return this.name;
	}
}