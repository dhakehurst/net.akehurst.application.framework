/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.akehurst.application.framework.computational.interfaceAccessControl;

import net.akehurst.application.framework.common.interfaceUser.UserDetails;

public interface IAuthorisationRequest {

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
