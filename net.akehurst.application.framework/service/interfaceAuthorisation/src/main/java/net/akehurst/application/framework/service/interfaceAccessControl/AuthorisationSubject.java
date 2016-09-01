package net.akehurst.application.framework.service.interfaceAccessControl;

import net.akehurst.application.framework.common.AbstractDataType;
import net.akehurst.application.framework.common.annotations.declaration.DataType;

@DataType
public class AuthorisationSubject extends AbstractDataType {

	public AuthorisationSubject(final String identity) {
		super(identity);
	}

	public String getIdentity() {
		return (String) super.getIdentityValues().get(0);
	}
}