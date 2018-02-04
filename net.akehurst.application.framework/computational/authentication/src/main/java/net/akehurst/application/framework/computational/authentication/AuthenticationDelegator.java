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
    public void requestLogin(final UserSession session, final String username, final String password) {
        this.portProvider().out(ICAuthenticatorRequest.class).requestLogin(session, username, password);
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
