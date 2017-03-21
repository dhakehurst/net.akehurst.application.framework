package net.akehurst.application.framework.computational.authentication;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.declaration.Component;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.computational.interfaceAuthenticator.ICAuthenticatorNotification;
import net.akehurst.application.framework.computational.interfaceAuthenticator.ICAuthenticatorRequest;
import net.akehurst.application.framework.computational.interfaceUser.authentication.IUserAuthenticationNotification;
import net.akehurst.application.framework.computational.interfaceUser.authentication.IUserAuthenticationRequest;
import net.akehurst.application.framework.realisation.AbstractComponent;

@Component
public class AuthenticationDelegator extends AbstractComponent implements IUserAuthenticationRequest, ICAuthenticatorNotification {

	public AuthenticationDelegator(final String afId) {
		super(afId);
	}

	// public ICAuthenticatorRequest authRequest;
	// public IUserAuthenticationNotification userAuthNotification;

	// public void setAuthRequest(final ICAuthenticatorRequest value) {
	// this.port() = value;
	// }
	//
	// public void setUserAuthNotification(final IUserAuthenticationNotification value) {
	// this.userAuthNotification = value;
	// }

	// --- IUserAuthenticationRequest ---
	@Override
	public void requestLogin(final UserSession session, final String username, final String password, final String encoding) {
		this.portProvider().out(ICAuthenticatorRequest.class).requestLogin(session, username, password, encoding);
	}

	@Override
	public void requestLogout(final UserSession session) {
		this.portProvider().out(ICAuthenticatorRequest.class).requestLogout(session);
	}

	// --- ICAuthenticatorNotification ---
	@Override
	public void notifyAuthenticationSuccess(final UserSession session) {
		this.portUser().out(IUserAuthenticationNotification.class).notifyAuthenticationSuccess(session);
	};

	@Override
	public void notifyAuthenticationFailure(final UserSession session, final String message) {
		this.portUser().out(IUserAuthenticationNotification.class).notifyAuthenticationFailure(session, message);
	}

	@Override
	public void notifyAuthenticationCleared(final UserSession session) {
		this.portUser().out(IUserAuthenticationNotification.class).notifyAuthenticationCleared(session);
	}

	// --- Ports ---

	@PortInstance
	@PortContract(provides = IUserAuthenticationRequest.class, requires = IUserAuthenticationNotification.class)
	IPort portUser;

	public IPort portUser() {
		return this.portUser;
	}

	@PortInstance
	@PortContract(provides = ICAuthenticatorNotification.class, requires = ICAuthenticatorRequest.class)
	IPort portProvider;

	public IPort portProvider() {
		return this.portProvider;
	}
}
