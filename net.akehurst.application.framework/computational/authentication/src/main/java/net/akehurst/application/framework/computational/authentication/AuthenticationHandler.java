package net.akehurst.application.framework.computational.authentication;

import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.computational.interfaceAuthenticator.ICAuthenticatorNotification;
import net.akehurst.application.framework.computational.interfaceAuthenticator.ICAuthenticatorRequest;
import net.akehurst.application.framework.computational.interfaceUser.authentication.IUserAuthenticationNotification;
import net.akehurst.application.framework.computational.interfaceUser.authentication.IUserAuthenticationRequest;
import net.akehurst.application.framework.realisation.AbstractIdentifiableObject;

public class AuthenticationHandler extends AbstractIdentifiableObject implements IUserAuthenticationRequest, ICAuthenticatorNotification {

	public AuthenticationHandler(final String afId) {
		super(afId);
	}

	public ICAuthenticatorRequest authRequest;
	public IUserAuthenticationNotification userAuthNotification;

	public void setAuthRequest(final ICAuthenticatorRequest value) {
		this.authRequest = value;
	}

	public void setUserAuthNotification(final IUserAuthenticationNotification value) {
		this.userAuthNotification = value;
	}

	// --- IUserAuthenticationRequest ---
	@Override
	public void requestLogin(final UserSession session, final String username, final String password) {
		this.authRequest.requestLogin(session, username, password);
	}

	@Override
	public void requestLogout(final UserSession session) {
		this.authRequest.requestLogout(session);
	}

	// --- ICAuthenticatorNotification ---
	@Override
	public void notifyAuthenticationSuccess(final UserSession session) {
		this.userAuthNotification.notifyAuthenticationSuccess(session);
	};

	@Override
	public void notifyAuthenticationFailure(final UserSession session, final String message) {
		this.userAuthNotification.notifyAuthenticationFailure(session, message);
	}

	@Override
	public void notifyAuthenticationCleared(final UserSession session) {
		this.userAuthNotification.notifyAuthenticationCleared(session);
	}

}
