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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.akehurst.application.framework.common.IActiveObject;
import net.akehurst.application.framework.common.annotations.instance.ActiveObjectInstance;
import net.akehurst.application.framework.common.annotations.instance.ComponentInstance;
import net.akehurst.application.framework.realisation.AbstractActiveObject;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;
import net.akehurst.application.framework.test.ITestEnvironment;

abstract public class AbstractTestContext extends AbstractActiveObject implements ITestEnvironment {

	public AbstractTestContext() {
		super("testEnvironment");
	}

	@Override
	public void afRun() {
		try {
			// TODO handle inheritance ?
			final List<IActiveObject> objects = new ArrayList<>();
			for (final Field f : this.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				final ComponentInstance ann = f.getAnnotation(ComponentInstance.class);
				if (null == ann) {
					// do nothing
				} else {
					final IActiveObject ao = (IActiveObject) f.get(this);
					// TODO: support ordering of objects
					objects.add(ao);
				}

				final ActiveObjectInstance ann2 = f.getAnnotation(ActiveObjectInstance.class);
				if (null == ann2) {
					// do nothing
				} else {
					final IActiveObject ao = (IActiveObject) f.get(this);
					// TODO: support ordering of objects
					objects.add(ao);
				}
			}

			for (final IActiveObject ao : objects) {
				ao.afStart();
			}

			for (final IActiveObject ao : objects) {
				ao.afJoin();
			}

		} catch (final Exception ex) {
			this.logger.log(LogLevel.ERROR, "Failed to run application " + this.afId(), ex);
		}
	}

	@Override
	public void afTerminate() {
		// no need to do anything, default run behaviour will terminate on its own
	}

}
