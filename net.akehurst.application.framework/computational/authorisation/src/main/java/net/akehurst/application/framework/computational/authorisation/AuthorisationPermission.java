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
package net.akehurst.application.framework.computational.authorisation;

import net.akehurst.application.framework.common.AbstractDataType;
import net.akehurst.application.framework.common.annotations.declaration.DataType;
import net.akehurst.application.framework.computational.interfaceAccessControl.AuthorisationActivity;
import net.akehurst.application.framework.computational.interfaceAccessControl.AuthorisationSubject;
import net.akehurst.application.framework.computational.interfaceAccessControl.AuthorisationTarget;

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
