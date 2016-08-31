package net.akehurst.application.framework.service.interfaceCapability;

import net.akehurst.application.framework.common.interfaceUser.UserDetails;

public interface ICapabilityService {

	/**
	 * Does the user have permission to perform the given action on the asset
	 *
	 * @param action
	 * @param user
	 * @return
	 */
	boolean hasFor(UserDetails user, CapabilityAction action, IProtectedAsset asset);

}
