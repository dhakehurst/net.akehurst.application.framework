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
package net.akehurst.application.framework.common.test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import net.akehurst.application.framework.realisation.ConfigurationServiceAbstract;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

public class TestConfigurationService extends ConfigurationServiceAbstract {

    public TestConfigurationService(final String afId) {
        super(afId);
        this.testValues = new HashMap<>();
    }

    Map<String, Object> testValues;

    public void set(final String idPath, final Object value) {
        this.testValues.put(idPath, value);
    }

    @Override
    public <T> T fetchValue(final Type itemType, final String idPath, final String defaultValueString) {
        T value = (T) this.testValues.get(idPath);
        if (null == value) {
            value = super.createValueFromDefault(itemType, defaultValueString);
        }
        this.logger.log(LogLevel.TRACE,
                String.format("%s.fetchValue(%s,%s) = %s", this.afId(), itemType.getTypeName(), idPath, null == value ? "null" : value.toString()));
        return value;
    }

}
