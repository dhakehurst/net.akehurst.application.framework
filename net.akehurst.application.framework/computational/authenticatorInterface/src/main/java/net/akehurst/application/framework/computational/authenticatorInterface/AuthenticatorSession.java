package net.akehurst.application.framework.computational.authenticatorInterface;

import net.akehurst.application.framework.common.AbstractDataType;

public class AuthenticatorSession extends AbstractDataType {

	public AuthenticatorSession(String sessionId, AuthenticatorUserDetails user) {
		this.sessionId = sessionId;
		this.user = user;
	}
	String sessionId;
	public String getId() {
		return this.sessionId;
	}
	
	AuthenticatorUserDetails user;
	
	public AuthenticatorUserDetails getUser() {
		return this.user;
	}
}