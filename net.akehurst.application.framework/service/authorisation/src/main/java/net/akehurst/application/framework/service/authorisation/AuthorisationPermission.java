package net.akehurst.application.framework.service.authorisation;

import net.akehurst.application.framework.common.AbstractDataType;
import net.akehurst.application.framework.common.annotations.declaration.DataType;
import net.akehurst.application.framework.service.interfaceAccessControl.AuthorisationActivity;
import net.akehurst.application.framework.service.interfaceAccessControl.AuthorisationSubject;
import net.akehurst.application.framework.service.interfaceAccessControl.AuthorisationTarget;

@DataType
public class AuthorisationPermission extends AbstractDataType {

	public AuthorisationPermission(final AuthorisationSubject subject, final AuthorisationActivity activity, final AuthorisationTarget target) {
		super(subject, activity, target);
	}

	public AuthorisationSubject getSubject() {
		return (AuthorisationSubject) super.getIdentityValues().get(0);
	}

	public AuthorisationActivity getActivity() {
		return (AuthorisationActivity) super.getIdentityValues().get(1);
	}

	public AuthorisationTarget getTarget() {
		return (AuthorisationTarget) super.getIdentityValues().get(2);
	}
}
