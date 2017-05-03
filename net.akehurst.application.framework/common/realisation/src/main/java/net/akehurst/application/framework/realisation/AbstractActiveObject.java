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
import java.util.ArrayList;
import java.util.List;

import net.akehurst.application.framework.common.IActiveObject;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.common.annotations.instance.ActiveObjectInstance;
import net.akehurst.application.framework.common.annotations.instance.ComponentInstance;
import net.akehurst.application.framework.common.annotations.instance.ServiceReference;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract public class AbstractActiveObject implements IActiveObject {

	@ServiceReference
	protected IApplicationFramework af;

	@ServiceReference
	protected ILogger logger;

	public AbstractActiveObject(final String afId) {
		this.afId = afId;
	}

	String afId;

	@Override
	public String afId() {
		return this.afId;
	}

	Thread thread;

	@Override
	public void afStart() {
		this.logger.log(LogLevel.TRACE, "AbstractActiveObject.afStart");
		this.thread = new Thread(() -> {
			this.afRun();
			this.logger.log(LogLevel.TRACE, "Finished");
		}, this.afId());
		this.thread.start();
	}

	@Override
	public void afJoin() throws InterruptedException {
		this.logger.log(LogLevel.TRACE, "Waiting for %s to Finish", this.afId());
		this.thread.join();
	}

	@Override
	public void afInterrupt() {
		this.thread.interrupt();
	}

	protected List<IActiveObject> afActiveParts() throws IllegalArgumentException, IllegalAccessException {
		final List<IActiveObject> objects = this.afActiveParts(this.getClass());

		return objects;
	}

	private List<IActiveObject> afActiveParts(final Class<?> class_) throws IllegalArgumentException, IllegalAccessException {
		final List<IActiveObject> objects = new ArrayList<>();

		if (null == class_.getSuperclass()) {
		} else {
			final List<IActiveObject> superObjs = this.afActiveParts(class_.getSuperclass());
			objects.addAll(superObjs);
		}

		for (final Field f : class_.getDeclaredFields()) {
			f.setAccessible(true);
			final ComponentInstance annC = f.getAnnotation(ComponentInstance.class);
			final ActiveObjectInstance annAO = f.getAnnotation(ActiveObjectInstance.class);
			if (null == annC && null == annAO) {
				// do nothing
			} else {
				final IActiveObject ao = (IActiveObject) f.get(this);
				// TODO: support ordering of objects
				objects.add(ao);
			}
		}
		return objects;
	}

	@Override
	public int hashCode() {
		return this.afId().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof IActiveObject) {
			final IActiveObject other = (IActiveObject) obj;
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
