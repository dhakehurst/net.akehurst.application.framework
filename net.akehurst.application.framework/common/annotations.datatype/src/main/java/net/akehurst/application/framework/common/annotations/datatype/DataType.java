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
package net.akehurst.application.framework.common.annotations.datatype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type (class/interface) as representing a datatype. A datatype is similar to a 'C' language concept of a struct.
 *
 * A datatype is expected to conform to the following constraints:
 * <li>accessors/mutators should only be for primitive types, collections, or other datatypes</li>
 * <li>accessors/mutators should create a tree structure, i.e. they are assumed to define a composition relationship unless otherwise defined</li>
 * <li>accessors/mutators should be marked with the annotations Reference if they reference something in another part of the tree</li>
 * <li>accessors should be marked with the annotations Query if it computes a value rather than containing it</li>
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataType {

}
