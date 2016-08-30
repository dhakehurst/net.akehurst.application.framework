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
package net.akehurst.application.framework.engineering.authenticator2Gui;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.interfaceUser.UserSession;
import net.akehurst.application.framework.computational.interfaceAuthenticator.ICAuthenticatorNotification;
import net.akehurst.application.framework.computational.interfaceAuthenticator.ICAuthenticatorRequest;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorRequest;

public class AuthenticatorToGui extends AbstractComponent implements ICAuthenticatorRequest, IAuthenticatorNotification {

	public AuthenticatorToGui(final String id) {
		super(id);
	}

	// --------- ICAuthenticatorRequest ---------
	@Override
	public void requestLogin(final UserSession session, final String username, final String password) {
		this.portGui().out(IAuthenticatorRequest.class).requestLogin(session, username, password);
	}

	@Override
	public void requestLogout(final UserSession session) {
		this.portGui().out(IAuthenticatorRequest.class).requestLogout(session);
	}

	// --------- IAuthenticatorNotification ---------
	@Override
	public void notifyAuthenticationFailure(final UserSession session, final String message) {
		this.portAuth().out(ICAuthenticatorNotification.class).notifyAuthenticationFailure(session, message);
	}

	@Override
	public void notifyAuthenticationSuccess(final UserSession session) {
		this.portAuth().out(ICAuthenticatorNotification.class).notifyAuthenticationSuccess(session);
	}

	@Override
	public void notifyAuthenticationCleared(final UserSession session) {
		this.portAuth().out(ICAuthenticatorNotification.class).notifyAuthenticationCleared(session);
	}

	// --------- Ports ---------
	@PortInstance(provides = { ICAuthenticatorRequest.class }, requires = { ICAuthenticatorNotification.class })
	IPort portAuth;

	public IPort portAuth() {
		return this.portAuth;
	}

	@PortInstance(provides = { IAuthenticatorNotification.class }, requires = { IAuthenticatorRequest.class })
	IPort portGui;

	public IPort portGui() {
		return this.portGui;
	}
}
