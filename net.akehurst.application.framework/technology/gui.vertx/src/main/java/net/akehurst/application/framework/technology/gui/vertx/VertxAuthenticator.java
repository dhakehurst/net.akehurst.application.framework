package net.akehurst.application.framework.technology.gui.vertx;

import net.akehurst.application.framework.common.IPort;
import net.akehurst.application.framework.common.annotations.instance.ActiveObjectInstance;
import net.akehurst.application.framework.common.annotations.instance.PortContract;
import net.akehurst.application.framework.common.annotations.instance.PortInstance;
import net.akehurst.application.framework.realisation.AbstractComponent;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorNotification;
import net.akehurst.application.framework.technology.interfaceAuthentication.IAuthenticatorRequest;

public class VertxAuthenticator extends AbstractComponent {

	public VertxAuthenticator(final String afId) {
		super(afId);
	}

	@ActiveObjectInstance
	VertxAuthenticationHandler handler;

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
