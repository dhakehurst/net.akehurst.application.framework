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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import net.akehurst.application.framework.common.ISignal;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract public class AbstractActiveSignalProcessingObject extends AbstractActiveObject {

	public AbstractActiveSignalProcessingObject(String id) {
		super(id);
		this.signals = new LinkedBlockingQueue<>();
	}

	static class NamedSignal {
		public NamedSignal(String name, ISignal signal) {
			this.name = name;
			this.signal = signal;
		}
		String name;
		ISignal signal;
	}
	
	BlockingQueue<NamedSignal> signals;

	@Override
	public void afRun() {
		logger.log(LogLevel.TRACE, "afRun");
		while (true) {
			try {
				NamedSignal ns = this.signals.take();
				logger.log(LogLevel.TRACE, ns.name);
				ns.signal.execute();
			} catch (Exception ex) {
				ex.printStackTrace(); //TODO: make this log
			}
		}

	}

	protected void addToQueue(String name, ISignal signal) {
		this.signals.add(new NamedSignal(name, signal));
	}

}
