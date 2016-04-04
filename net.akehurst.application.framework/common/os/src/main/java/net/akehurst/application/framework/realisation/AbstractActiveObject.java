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

import net.akehurst.application.framework.common.IActiveObject;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract
public class AbstractActiveObject implements IActiveObject {
	
	@ServiceReference
	protected IApplicationFramework os;
	
	@ServiceReference
	protected ILogger logger;
	
	public AbstractActiveObject(String id) {
		this.id = id;
	}
	
	String id;
	public String afId() {
		return this.id;
	}
	
	Thread thread;

	@Override
	public void afStart() {
		logger.log(LogLevel.TRACE, "afStart");
		this.thread = new Thread((() -> this.afRun()), this.getClass().getSimpleName());
		this.thread.setName(this.afId());
		this.thread.start();
	}
	
	@Override
	public void afJoin() throws InterruptedException {
		this.thread.join();
	}
	
	
	@Override
	public int hashCode() {
		return this.afId().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IActiveObject) {
			IActiveObject other = (IActiveObject)obj;
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
