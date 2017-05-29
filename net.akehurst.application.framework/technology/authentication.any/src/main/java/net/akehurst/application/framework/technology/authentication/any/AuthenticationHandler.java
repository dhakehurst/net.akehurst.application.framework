/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

			final UserSession authenticatedSession = new UserSession(session.getId(), new UserDetails(username), session.getData());

			this.authenticationNotification.notifyAuthenticationSuccess(authenticatedSession);

		});
	}

	@Override
	public void requestLogout(final UserSession session) {
		super.submit("requestLogout", () -> {
			final UserSession clearedSession = new UserSession(session.getId(), null, null);
			this.authenticationNotification.notifyAuthenticationCleared(clearedSession);
		});
	}

}
