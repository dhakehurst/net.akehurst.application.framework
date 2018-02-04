package net.akehurst.application.framework.technology.authentication.ldap;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.declaration.Component;
import net.akehurst.application.framework.common.annotations.instance.ActiveObjectInstance;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorRequest;

@Component
public class LdapAuthenticator extends AbstractComponent {

    public LdapAuthenticator(final String afId) {
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
