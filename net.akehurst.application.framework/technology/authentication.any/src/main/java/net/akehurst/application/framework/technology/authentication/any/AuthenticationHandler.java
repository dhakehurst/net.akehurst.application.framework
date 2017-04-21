package net.akehurst.application.framework.technology.authentication.any;

import net.akehurst.application.framework.common.annotations.declaration.ExternalConnection;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.common.interfaceUser.UserDetails;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.realisation.AbstractActiveSignalProcessingObject;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorRequest;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;

public class AuthenticationHandler extends AbstractActiveSignalProcessingObject implements IAuthenticatorRequest {

	@ServiceReference
	ILogger logger;

	public AuthenticationHandler(final String afId) {
		super(afId);
	}

	@ExternalConnection
	public IAuthenticatorNotification authenticationNotification;

	@Override
	public void requestLogin(final UserSession session, final String username, final String password) {
		super.submit("requestLogin", () -> {

			final UserSession authenticatedSession = new UserSession(session.getId(), new UserDetails(username));

			this.authenticationNotification.notifyAuthenticationSuccess(authenticatedSession);

		});
	}

	@Override
	public void requestLogout(final UserSession session) {
		super.submit("requestLogout", () -> {
			final UserSession clearedSession = new UserSession(session.getId(), null);
			this.authenticationNotification.notifyAuthenticationCleared(clearedSession);
		});
	}

}
