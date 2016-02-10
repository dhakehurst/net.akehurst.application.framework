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
package net.akehurst.application.framework.common;

import java.util.Arrays;
import java.util.Objects;

abstract public class AbstractDataType {

	public AbstractDataType(Object... objects) {
		this.objects = objects;
		this.hashCode_cache = Objects.hash(this.objects);
	}

	Object[] objects;
	
	int hashCode_cache;
	@Override
	public int hashCode() {
		return this.hashCode_cache;
	}

	@Override
	public boolean equals(Object obj) {
		if (this.getClass().isInstance(obj)) {
			AbstractDataType other = (AbstractDataType)obj;
			return Arrays.equals(this.objects, other.objects);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + Arrays.asList(this.objects);
	}

}
