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

import java.lang.reflect.Method;

import org.hjson.JsonObject;
import org.hjson.JsonValue;

import net.akehurst.application.framework.common.annotations.datatype.DataType;
import net.akehurst.application.framework.common.annotations.datatype.Query;
import net.akehurst.application.framework.common.annotations.datatype.Reference;
import net.akehurst.transform.binary.api.BinaryRule;
import net.akehurst.transform.binary.api.BinaryTransformer;

public class Datatype2HJsonObject implements BinaryRule<Object, JsonObject> {

	private Object getValue(final Method m, final Object obj) {
		try {
			final Object value = m.invoke(obj);
			return value;
		} catch (final Throwable t) {
			// TODO: log
			t.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean isValidForLeft2Right(final Object left) {
		return left.getClass().isAnnotationPresent(DataType.class);
	}

	@Override
	public boolean isValidForRight2Left(final JsonObject right) {
		// TODO any constraints here ?
		return true;
	}

	@Override
	public boolean isAMatch(final Object left, final JsonObject right, final BinaryTransformer transformer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JsonObject constructLeft2Right(final Object left, final BinaryTransformer transformer) {
		final JsonObject right = new JsonObject();
		return right;
	}

	@Override
	public Object constructRight2Left(final JsonObject right, final BinaryTransformer transformer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateLeft2Right(final Object left, final JsonObject right, final BinaryTransformer transformer) {
		for (final Method m : left.getClass().getMethods()) {
			if (m.getDeclaringClass() != Object.class && m.getName().startsWith("get")) {
				if (m.getAnnotation(Query.class) != null) {
					// this is a query method, don't map it
				} else if (m.getAnnotation(Reference.class) != null) {
					// this is a reference property, so find and map the reference value

				} else {
					// normal composit property
					final String propName = m.getName().substring(3, 4).toLowerCase() + m.getName().substring(4);
					final Object lv = this.getValue(m, left);
					final JsonValue rv = transformer
							.transformLeft2Right((Class<? extends BinaryRule<Object, JsonValue>>) (Object) Object2HJsonValueAbstract.class, lv);
					right.add(propName, rv);
				}
			}
		}
	}

	@Override
	public void updateRight2Left(final Object left, final JsonObject right, final BinaryTransformer transformer) {
		// TODO Auto-generated method stub

	}

}
