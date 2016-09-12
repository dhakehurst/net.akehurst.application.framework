package net.akehurst.application.framework.service.interfaceAccessControl;

public class AuthorisationException extends Exception {
	public AuthorisationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}