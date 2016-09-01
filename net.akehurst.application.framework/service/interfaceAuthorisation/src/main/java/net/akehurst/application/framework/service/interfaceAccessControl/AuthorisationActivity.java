package net.akehurst.application.framework.service.interfaceAccessControl;

import net.akehurst.application.framework.common.AbstractDataType;
import net.akehurst.application.framework.common.annotations.declaration.DataType;

@DataType
public class AuthorisationActivity extends AbstractDataType {

	public AuthorisationActivity(final String identity) {
		super(identity);
	}

	public String getIdentity() {
		return (String) super.getIdentityValues().get(0);
	}

}
