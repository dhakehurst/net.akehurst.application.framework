package net.akehurst.application.framework.technology.authentication.ldap;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.common.test.annotation.MockPassiveObjectInstance;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorRequest;

public class testComponent_LdapAuthenticationStimulator extends AbstractComponent {

	public testComponent_LdapAuthenticationStimulator(final String afId) {
		super(afId);
	}

	@MockPassiveObjectInstance
	testHandler handler;

	@Override
	public void afConnectParts() {
		this.portAuth().connectInternal(this.handler);
	}

	@PortInstance
	@PortContract(provides = IAuthenticatorNotification.class, requires = IAuthenticatorRequest.class)
	private IPort portAuth;

	public IPort portAuth() {
		return this.portAuth;
	}

}
