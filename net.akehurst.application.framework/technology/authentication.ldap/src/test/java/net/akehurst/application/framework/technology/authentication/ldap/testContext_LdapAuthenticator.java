package net.akehurst.application.framework.technology.authentication.ldap;

import net.akehurst.application.framework.common.annotations.instance.ComponentInstance;
import net.akehurst.application.framework.common.test.AbstractTestContext;
import net.akehurst.application.framework.common.test.annotation.TestComponentInstance;
import net.akehurst.application.framework.common.test.annotation.TestContext;

@TestContext
public class testContext_LdapAuthenticator extends AbstractTestContext {

	@ComponentInstance
	public LdapAuthenticator sut;

	@TestComponentInstance
	public testComponent_LdapAuthenticationStimulator tc1;

	@Override
	public void afConnectParts() {
		this.tc1.portAuth().connect(this.sut.portAuth());
	}

}
