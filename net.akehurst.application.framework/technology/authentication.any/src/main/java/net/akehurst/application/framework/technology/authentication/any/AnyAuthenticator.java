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

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.declaration.Component;
import net.akehurst.application.framework.common.annotations.instance.ActiveObjectInstance;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.realisation.ComponentAbstract;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorRequest;

@Component
public class AnyAuthenticator extends ComponentAbstract {
    public AnyAuthenticator(final String afId) {
        super(afId);
    }

    @ActiveObjectInstance
    AuthenticationHandler handler;

    @Override
    public void afConnectParts() {
        this.portAuth().connectInternal(this.handler);
    }

    @PortInstance
    @PortContract(provides = IAuthenticatorRequest.class, requires = IAuthenticatorNotification.class)
    private IPort portAuth;

    public IPort portAuth() {
        return this.portAuth;
    }
}
