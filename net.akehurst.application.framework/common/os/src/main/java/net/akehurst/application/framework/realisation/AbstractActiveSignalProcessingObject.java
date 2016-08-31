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
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

import net.akehurst.application.framework.common.ISignal;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract public class AbstractActiveSignalProcessingObject extends AbstractActiveObject {

	public AbstractActiveSignalProcessingObject(final String afId) {
		super(afId);
		this.signals = new LinkedBlockingQueue<>();
	}

	static class NamedSignal {
		public NamedSignal(final String name, final ISignal signal) {
			this.name = name;
			this.signal = signal;
			this.future = new FutureTask<>(() -> {
				try {
					signal.execute();
				} catch (final Throwable t) {
					t.printStackTrace();
				}
			}, null);
		}

		String name;
		ISignal signal;
		FutureTask<Void> future;
	}

	BlockingQueue<NamedSignal> signals;

	@Override
	public void afRun() {
		this.logger.log(LogLevel.TRACE, "AbstractActiveSignalProcessingObject.afRun");
		while (true) {
			try {
				final NamedSignal ns = this.signals.take();
				this.logger.log(LogLevel.TRACE, ns.name);
				ns.future.run();

			} catch (final Throwable ex) {
				ex.printStackTrace(); // TODO: make this log
			}
		}

	}

	protected Future<Void> submit(final String name, final ISignal signal) {
		final NamedSignal ns = new NamedSignal(name, signal);
		this.signals.add(ns);
		return ns.future;
	}

}
