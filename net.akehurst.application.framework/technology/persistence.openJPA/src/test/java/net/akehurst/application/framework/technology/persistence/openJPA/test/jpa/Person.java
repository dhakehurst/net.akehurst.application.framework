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
package net.akehurst.application.framework.technology.persistence.openJPA.test.jpa;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Person {

	String name;
	
	@Id
	public String getName() {
		return this.name;
	};
	
	public void setName(String value) {
		this.name = value;
	}
	
	String hairColour;
	
	@Basic
	public String getHairColour() {
		return this.hairColour;
	}
	
	public void setHairColour(String value) {
		this.hairColour = value;
	}
}
