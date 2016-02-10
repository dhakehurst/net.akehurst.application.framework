package net.akehurst.application.framework.technology.authentication;

public interface IAuthenticatorNotification {

	void notifyAuthenticationSuccess(ISession session);
	
	void notifyAuthenticationFailure(ISession session, String message);
	
	void notifyAuthenticationCleared(ISession session);
}
