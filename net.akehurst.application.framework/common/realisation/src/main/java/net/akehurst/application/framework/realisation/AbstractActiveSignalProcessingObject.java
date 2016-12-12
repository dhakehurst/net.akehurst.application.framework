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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import net.akehurst.application.framework.common.ISignal;
import net.akehurst.application.framework.common.ISignalR;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract public class AbstractActiveSignalProcessingObject extends AbstractActiveObject {

	public AbstractActiveSignalProcessingObject(final String afId) {
		super(afId);
		this.signals = new LinkedBlockingQueue<>();
	}

	@ConfiguredValue(defaultValue = "1")
	Integer numThreads;

	ExecutorService executor;

	class NamedSignal<R> {
		public NamedSignal(final String name, final ISignalR<R> signal) {
			this.name = name;
			this.signal = signal;
			this.future = new FutureTask<>(() -> {
				try {
					return signal.execute();
				} catch (final Throwable t) {
					AbstractActiveSignalProcessingObject.this.logger.log(LogLevel.ERROR, t.getMessage(), t);
				}
				return null;
			});
		}

		String name;
		ISignalR<R> signal;
		FutureTask<R> future;
	}

	BlockingQueue<NamedSignal<?>> signals;

	static class NamedThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		NamedThreadFactory(final String baseName) {
			final SecurityManager s = System.getSecurityManager();
			this.group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			this.namePrefix = baseName + "-pool-" + NamedThreadFactory.poolNumber.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(final Runnable r) {
			final Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0);
			if (t.isDaemon()) {
				t.setDaemon(false);
			}
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}
	}

	@Override
	public void afRun() {
		this.logger.log(LogLevel.TRACE, "AbstractActiveSignalProcessingObject.afRun");
		this.executor = Executors.newFixedThreadPool(this.numThreads, new NamedThreadFactory(this.afId()));
		while (true) {
			try {
				final NamedSignal<?> ns = this.signals.take();
				this.executor.submit(() -> {
					this.logger.log(LogLevel.TRACE, ns.name);
					ns.future.run();
				});

			} catch (final Throwable ex) {
				this.logger.log(LogLevel.ERROR, ex.getMessage(), ex);
			}
		}

	}

	protected Future<Void> submit(final String name, final ISignal signal) {
		final NamedSignal<Void> ns = new NamedSignal<>(name, () -> {
			signal.execute();
			return null;
		});
		this.signals.add(ns);
		return ns.future;
	}

	protected <T> Future<T> submit(final String name, final Class<T> returnType, final ISignalR<T> signal) {
		final NamedSignal<T> ns = new NamedSignal<>(name, signal);
		this.signals.add(ns);
		return ns.future;
	}
}
