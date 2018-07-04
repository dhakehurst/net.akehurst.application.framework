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
package net.akehurst.application.framework.engineering.datatype.transform.hjson.rules;

import java.util.ArrayList;
import java.util.List;

import org.hjson.JsonArray;
import org.hjson.JsonValue;

import net.akehurst.transform.binary.api.BinaryRule;
import net.akehurst.transform.binary.api.BinaryTransformer;

public class List2HJsonArray extends Object2HJsonValueAbstract<List<Object>, JsonArray> implements BinaryRule<List<Object>, JsonArray> {

	@Override
	public boolean isValidForLeft2Right(final List<Object> left) {
		return true;
	}

	@Override
	public boolean isValidForRight2Left(final JsonArray right) {
		return true;
	}

	@Override
	public boolean isAMatch(final List<Object> left, final JsonArray right, final BinaryTransformer transformer) {
		return left.size() == right.size()
				&& transformer.isAllAMatch((Class<? extends BinaryRule<Object, JsonValue>>) (Object) Object2HJsonValueAbstract.class, left, right.values());
	}

	@Override
	public JsonArray constructLeft2Right(final List<Object> left, final BinaryTransformer transformer) {
		final JsonArray right = new JsonArray();
		return right;
	}

	@Override
	public List<Object> constructRight2Left(final JsonArray right, final BinaryTransformer transformer) {
		final List<Object> left = new ArrayList<>();
		return left;
	}

	@Override
	public void updateLeft2Right(final List<Object> left, final JsonArray right, final BinaryTransformer transformer) {
		for (final Object l : left) {
			final JsonValue r = transformer.transformLeft2Right((Class<? extends BinaryRule<Object, JsonValue>>) (Object) Object2HJsonValueAbstract.class, l);
			right.add(r);
		}
	}

	@Override
	public void updateRight2Left(final List<Object> left, final JsonArray right, final BinaryTransformer transformer) {
		for (final JsonValue r : right) {
			final Object l = transformer.transformRight2Left((Class<? extends BinaryRule<Object, JsonValue>>) (Object) Object2HJsonValueAbstract.class, r);
			left.add(l);
		}
	}

}
