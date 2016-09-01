package net.akehurst.application.framework.service.interfaceAccessControl;

import net.akehurst.application.framework.common.interfaceUser.UserDetails;

public interface IAuthorisationService {

	/**
	 * Does 'user' have 'permission' to do something with 'target'
	 *
	 * @param user
	 * @param permission
	 * @param subject
	 * @throws AuthorisationException
	 */
	boolean hasAuthorisation(UserDetails user, AuthorisationActivity activity, AuthorisationTarget target) throws AuthorisationException;

	/**
	 * grant to the 'subject' the 'permission' to do something to 'target'
	 *
	 * @param subject
	 */
	void grantAuthorisation(AuthorisationSubject subject, AuthorisationActivity activity, AuthorisationTarget target);

}
