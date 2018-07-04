/**
 * Copyright (C) 2018 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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
package net.akehurst.application.framework.engineering.datatype.transform.hjson;

import net.akehurst.application.framework.engineering.datatype.transform.hjson.rules.Datatype2HJsonObject;
import net.akehurst.application.framework.engineering.datatype.transform.hjson.rules.List2HJsonArray;
import net.akehurst.application.framework.engineering.datatype.transform.hjson.rules.Object2HJsonValueAbstract;
import net.akehurst.application.framework.engineering.datatype.transform.hjson.rules.String2HJsonValue;
import net.akehurst.transform.binary.api.BinaryRule;
import net.akehurst.transform.binary.basic.BinaryTransformerBasic;

public class Transformer extends BinaryTransformerBasic {

	public Transformer() {
		super.registerRule((Class<? extends BinaryRule<?, ?>>) (Object) Object2HJsonValueAbstract.class);
		super.registerRule(String2HJsonValue.class);
		super.registerRule(Datatype2HJsonObject.class);
		super.registerRule(List2HJsonArray.class);
	}

}
