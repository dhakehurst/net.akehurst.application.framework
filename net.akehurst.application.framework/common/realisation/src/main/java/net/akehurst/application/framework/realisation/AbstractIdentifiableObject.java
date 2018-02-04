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
package net.akehurst.application.framework.realisation;

import net.akehurst.application.framework.common.IIdentifiableObject;

public class AbstractIdentifiableObject implements IIdentifiableObject {

    public AbstractIdentifiableObject(final String afId) {
        this.afId = afId;
    }

    String afId;

    @Override
    public String afId() {
        return this.afId;
    }

    @Override
    public int hashCode() {
        return this.afId().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof IIdentifiableObject) {
            final IIdentifiableObject other = (IIdentifiableObject) obj;
            return this.afId().equals(other.afId());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.afId();
    }

}
