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

import java.util.List;

import net.akehurst.application.framework.common.IActiveObject;
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
			final List<IActiveObject> objects = super.afActiveParts();

			for (final IActiveObject ao : objects) {
				ao.afStart();
			}

			for (final IActiveObject ao : objects) {
				ao.afJoin();
			}

		} catch (final Exception ex) {
			this.logger.log(LogLevel.ERROR, "Failed to run testContext " + this.afId(), ex);
		}
	}

	@Override
	public void afInterrupt() {
		try {
			final List<IActiveObject> objects = super.afActiveParts();
			for (final IActiveObject ao : objects) {
				ao.afInterrupt();
			}
		} catch (final Exception ex) {
			this.logger.log(LogLevel.ERROR, "Error during Interrupt testContext " + this.afId(), ex);
		}
	}

	@Override
	public void afTerminate() {
		try {
			final List<IActiveObject> objects = super.afActiveParts();
			for (final IActiveObject ao : objects) {
				ao.afTerminate();
			}
		} catch (final Exception ex) {
			this.logger.log(LogLevel.ERROR, "Error during Terminate testContext " + this.afId(), ex);
		}
	}

}
