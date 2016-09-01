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

import java.lang.reflect.Field;

public class AnnotationDetails<AT> {

	AnnotationDetails(final Object object, final Field field, final Class<?> fieldType, final AT annotation) {
		this.object = object;
		this.field = field;
		this.annotation = annotation;
	}

	Object object;
	Field field;
	AT annotation;

	public AT getAnnotation() {
		return this.annotation;
	}

	public Field getField() {
		return this.field;
	}

	public Object getValue() {
		try {
			this.field.get(this.object);
		} catch (final IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
