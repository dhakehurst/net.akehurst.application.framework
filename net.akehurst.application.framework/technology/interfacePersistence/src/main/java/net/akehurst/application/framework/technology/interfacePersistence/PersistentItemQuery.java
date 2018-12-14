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
package net.akehurst.application.framework.technology.interfacePersistence;

import net.akehurst.datatype.common.DatatypeAbstract;

public class PersistentItemQuery extends DatatypeAbstract {

    public PersistentItemQuery(final String language, final String value) {
        super(language, value);
        this.value = value;
        this.language = language;
    }

    private final String language;
    private final String value;

    public String getLanguage() {
        return this.language;
    }

    public String getValue() {
        return this.value;
    }
}
