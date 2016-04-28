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
import net.akehurst.application.framework.common.UserSession;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.computational.interfaceAuthenticator.AuthenticatorSession;
import net.akehurst.application.framework.computational.interfaceAuthenticator.AuthenticatorUserDetails;
import net.akehurst.application.framework.computational.interfaceAuthenticator.ICAuthenticatorNotification;
import net.akehurst.application.framework.computational.interfaceAuthenticator.ICAuthenticatorRequest;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.authentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.authentication.IAuthenticatorRequest;


public class AuthenticatorToGui extends AbstractComponent implements ICAuthenticatorRequest, IAuthenticatorNotification {

	public AuthenticatorToGui(String id) {
		super(id);
	}
	
	@Override
	public void afConnectParts() {
	}
	
	@Override
	public void afRun() {
	}
	
	// --------- ICAuthenticatorRequest ---------
	@Override
	public void requestLogin(AuthenticatorSession session, String username, String password) {
		UserSession ts = new UserSession(session.getId(), null);
		portGui().out(IAuthenticatorRequest.class).requestLogin(ts, username, password);
	}
	
	@Override
	public void requestLogout(AuthenticatorSession session) {
		UserSession ts = new UserSession(session.getId(), null);
		portGui().out(IAuthenticatorRequest.class).requestLogout(ts);
	}
	
	// --------- IAuthenticatorNotification ---------
	@Override
	public void notifyAuthenticationFailure(UserSession session, String message) {
		AuthenticatorSession as = new AuthenticatorSession(session.getId(), null);
		portAuth().out(ICAuthenticatorNotification.class).notifyAuthenticationFailure(as, message);
	}
	
	@Override
	public void notifyAuthenticationSuccess(UserSession session) {
		AuthenticatorSession as = new AuthenticatorSession(session.getId(), new AuthenticatorUserDetails(session.getUser().getName()));
		portAuth().out(ICAuthenticatorNotification.class).notifyAuthenticationSuccess(as);
	}
	
	@Override
	public void notifyAuthenticationCleared(UserSession session) {
		AuthenticatorSession as = new AuthenticatorSession(session.getId(), null);
		portAuth().out(ICAuthenticatorNotification.class).notifyAuthenticationCleared(as);
	}
	
	// --------- Ports ---------
	@PortInstance(provides={ICAuthenticatorRequest.class},requires={ICAuthenticatorNotification.class})
	IPort portAuth;
	public IPort portAuth() {
		return this.portAuth;
	}
	
	@PortInstance(provides={IAuthenticatorNotification.class},requires={IAuthenticatorRequest.class})
	IPort portGui;
	public IPort portGui() {
		return this.portGui;
	}
}
