package net.akehurst.application.framework.service.interfaceAccessControl;

import net.akehurst.application.framework.common.AbstractDataType;

public class AuthorisationTarget extends AbstractDataType {

	public AuthorisationTarget(final String identity) {
		super(identity);
	}

	public String getIdentity() {
		return (String) super.getIdentityValues().get(0);
	}
}