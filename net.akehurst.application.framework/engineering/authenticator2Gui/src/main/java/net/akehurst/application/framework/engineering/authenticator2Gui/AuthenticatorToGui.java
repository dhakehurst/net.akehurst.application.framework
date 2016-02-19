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

import net.akehurst.application.framework.components.AbstractComponent;
import net.akehurst.application.framework.components.Port;
import net.akehurst.application.framework.computational.authenticatorInterface.AuthenticatorSession;
import net.akehurst.application.framework.computational.authenticatorInterface.AuthenticatorUserDetails;
import net.akehurst.application.framework.computational.authenticatorInterface.ICAuthenticatorNotification;
import net.akehurst.application.framework.computational.authenticatorInterface.ICAuthenticatorRequest;
import net.akehurst.application.framework.technology.authentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.authentication.IAuthenticatorRequest;
import net.akehurst.application.framework.technology.authentication.TechSession;
import net.akehurst.application.framework.technology.authentication.TechUserDetails;

public class AuthenticatorToGui extends AbstractComponent implements ICAuthenticatorRequest, IAuthenticatorNotification {

	public AuthenticatorToGui(String id) {
		super(id);
	}
	
	@Override
	public void afRun() {
		// TODO Auto-generated method stub
		
	}
	
	// --------- ICAuthenticatorRequest ---------
	@Override
	public void requestLogin(AuthenticatorSession session, String username, String password) {
		TechSession ts = new TechSession(session.getId(), null);
		portGui().out(IAuthenticatorRequest.class).requestLogin(ts, username, password);
	}
	
	@Override
	public void requestLogout(AuthenticatorSession session) {
		TechSession ts = new TechSession(session.getId(), null);
		portGui().out(IAuthenticatorRequest.class).requestLogout(ts);
	}
	
	// --------- IAuthenticatorNotification ---------
	@Override
	public void notifyAuthenticationFailure(TechSession session, String message) {
		AuthenticatorSession as = new AuthenticatorSession(session.getId(), null);
		portAuth().out(ICAuthenticatorNotification.class).notifyAuthenticationFailure(as, message);
	}
	
	@Override
	public void notifyAuthenticationSuccess(TechSession session) {
		AuthenticatorSession as = new AuthenticatorSession(session.getId(), new AuthenticatorUserDetails(session.getUser().getName()));
		portAuth().out(ICAuthenticatorNotification.class).notifyAuthenticationSuccess(as);
	}
	
	@Override
	public void notifyAuthenticationCleared(TechSession session) {
		AuthenticatorSession as = new AuthenticatorSession(session.getId(), null);
		portAuth().out(ICAuthenticatorNotification.class).notifyAuthenticationCleared(as);
	}
	
	// --------- Ports ---------
	Port portAuth;
	public Port portAuth() {
		if (null==this.portAuth) {
			this.portAuth = new Port("portAuth", this)
					.provides(ICAuthenticatorRequest.class, AuthenticatorToGui.this)
					.requires(ICAuthenticatorNotification.class);
		}
		return this.portAuth;
	}
	
	Port portGui;
	public Port portGui() {
		if (null==this.portGui) {
			this.portGui = new Port("portGui", this)
					.provides(IAuthenticatorNotification.class, AuthenticatorToGui.this)
					.requires(IAuthenticatorRequest.class);
		}
		return this.portGui;
	}
}
