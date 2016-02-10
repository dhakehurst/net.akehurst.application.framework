package net.akehurst.application.framework.technology.authentication;

public interface IAuthenticatorRequest {

	void requestLogin(ISession session, String username, String password);
	
	void requestLogout(ISession session);
	

}
