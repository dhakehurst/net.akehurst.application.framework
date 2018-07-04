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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.jooq.lambda.function.Consumer0;
import org.jooq.lambda.function.Consumer1;
import org.jooq.lambda.function.Consumer2;
import org.jooq.lambda.tuple.Tuple3;

import net.akehurst.application.framework.common.ActiveSignalProcessingObject;
import net.akehurst.application.framework.common.AsyncCall;
import net.akehurst.application.framework.common.Context;
import net.akehurst.application.framework.common.IActiveObject;
import net.akehurst.application.framework.common.ISignal;
import net.akehurst.application.framework.common.ISignalR;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract public class ActiveSignalProcessingObjectAbstract extends ActiveObjectAbstract implements ActiveSignalProcessingObject {

	@ConfiguredValue(defaultValue = "1")
	Integer numThreads;

	private boolean terminateRequested;
	private ExecutorService executor;
	private final BlockingQueue<NamedSignal<?>> signals;
	private final Map<Tuple3<Class<?>, String, Context>, Object> waiting;

	public ActiveSignalProcessingObjectAbstract(final String afId) {
		super(afId);
		this.terminateRequested = false;
		this.signals = new LinkedBlockingQueue<>();
		this.waiting = new HashMap<>();
	}

	private class NamedSignal<R> {
		public NamedSignal(final String name, final ISignalR<R> signal) {
			this.name = name;
			this.signal = signal;
			this.future = new FutureTask<>(() -> {
				try {
					return signal.execute();
				} catch (final Throwable t) {
					ActiveSignalProcessingObjectAbstract.this.logger.log(LogLevel.ERROR, t.getMessage(), t);
				}
				return null;
			});
		}

		String name;
		ISignalR<R> signal;
		FutureTask<R> future;
	}

	static class NamedThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final ILogger logger;

		NamedThreadFactory(final String baseName, final ILogger logger) {
			this.logger = logger;
			final SecurityManager s = System.getSecurityManager();
			this.group = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			this.namePrefix = baseName + "-pool-" + NamedThreadFactory.poolNumber.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(final Runnable r) {
			final String threadName = this.namePrefix + this.threadNumber.getAndIncrement();
			final Thread t = new Thread(this.group, r, threadName, 0);
			if (t.isDaemon()) {
				t.setDaemon(false);
			}
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			t.setUncaughtExceptionHandler((th, e) -> {
				this.logger.log(LogLevel.ERROR, "Thread %s terminated with uncaught exception %s", threadName, e);
			});
			return t;
		}
	}

	private void checkMethodExists(final Class<?> class_, final String methodName, final int numArgs) {
		for (final Method m : class_.getMethods()) {
			if (Objects.equals(m.getName(), methodName) && (-1 == numArgs || m.getParameterTypes().length == numArgs)) {
				return;
			}
		}
		throw new RuntimeException(String.format("Method '%s' with %d arguments not found on type '%s'", methodName, numArgs, class_.getSimpleName()));
	}

	@Override
	public void afRun() {
		this.logger.log(LogLevel.TRACE, "AbstractActiveSignalProcessingObject.afRun");

		try {
			final List<IActiveObject> objects = super.afActiveParts();

			for (final IActiveObject ao : objects) {
				ao.afStart();
			}

			this.executor = Executors.newFixedThreadPool(this.numThreads, new NamedThreadFactory(this.afId(), this.logger));
			while (!this.terminateRequested) {
				try {
					final NamedSignal<?> ns = this.signals.take();
					if (this.executor.isShutdown()) {
						this.logger.log(LogLevel.TRACE, "Shutdown, not executing task %s", ns.name);
					} else {
						this.executor.submit(() -> {
							try {
								this.logger.log(LogLevel.TRACE, ns.name);
								ns.future.run();
							} catch (final Throwable ex) {
								this.logger.log(LogLevel.ERROR, ex.getMessage(), ex);
							}
						});
					}
				} catch (final InterruptedException ex) {
					break;
				} catch (final Throwable ex) {
					this.logger.log(LogLevel.ERROR, ex.getMessage(), ex);
				}
			}

			for (final IActiveObject ao : objects) {
				ao.afJoin();
			}

		} catch (final IllegalArgumentException | IllegalAccessException ex) {
			this.logger.log(LogLevel.ERROR, "Failed get active parts " + this.afId(), ex);
		} catch (final InterruptedException ex) {
			this.logger.log(LogLevel.INFO, "Interrupted " + this.afId(), ex);
		}

	}

	@Override
	public void afInterrupt() {
		this.terminateRequested = true;
		this.submit("afInterrupt", () -> {
		});
		this.executor.shutdownNow();
	}

	@Override
	public void afTerminate() {
		this.terminateRequested = true;
		this.submit("afTerminate", () -> {
		});
		this.executor.shutdown();
	}

	protected AsyncCall execute(final Class<?> class_, final String signalName, final Context ctx, final Consumer0 body) {
		this.checkMethodExists(class_, signalName, -1);

		final Tuple3<Class<?>, String, Context> key = new Tuple3<>(class_, signalName, ctx);

		final NamedSignal<Void> ns = new NamedSignal<>(signalName, () -> {
			body.accept();
			return null;
		});
		this.signals.add(ns);
		return new AsyncCallDefault(this);
	}

	@Override
	public void whenReceivedThenExecute0(final Class<?> class_, final String signalName, final Context ctx, final long timeout, final Consumer0 func) {
		this.checkMethodExists(class_, signalName, 0);
		final Tuple3<Class<?>, String, Context> key = new Tuple3<>(class_, signalName, ctx);
		this.waiting.put(key, func);
	}

	@Override
	public <T1> void whenReceivedThenExecute1(final Class<?> class_, final String signalName, final Context ctx, final long timeout, final Consumer1<T1> func) {
		this.checkMethodExists(class_, signalName, 1);
		final Tuple3<Class<?>, String, Context> key = new Tuple3<>(class_, signalName, ctx);
		this.waiting.put(key, func);
	}

	@Override
	public <T1, T2> void whenReceivedThenExecute2(final Class<?> class_, final String signalName, final Context ctx, final long timeout,
			final Consumer2<T1, T2> func) {
		this.checkMethodExists(class_, signalName, 2);
		final Tuple3<Class<?>, String, Context> key = new Tuple3<>(class_, signalName, ctx);
		this.waiting.put(key, func);
	}

	protected void receive0(final Class<?> class_, final String signalName, final Context ctx, final Consumer0 defaultBody) {
		this.checkMethodExists(class_, signalName, 0);
		final Tuple3<Class<?>, String, Context> key = new Tuple3<>(class_, signalName, ctx);
		final NamedSignal<Void> ns = new NamedSignal<>(signalName, () -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer0 body = (Consumer0) value;
				body.accept();
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
			return null;
		});
		this.signals.add(ns);
	}

	protected <T1> void receive1(final Class<?> class_, final String signalName, final Context ctx, final T1 p1, final Consumer0 defaultBody) {
		this.checkMethodExists(class_, signalName, 1);
		final Tuple3<Class<?>, String, Context> key = new Tuple3<>(class_, signalName, ctx);
		final NamedSignal<Void> ns = new NamedSignal<>(signalName, () -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen?  and handle timeout!
			if (null != value) {
				final Consumer1<T1> body = (Consumer1<T1>) value;
				body.accept(p1);
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
			return null;
		});
		this.signals.add(ns);
	}

	protected <T1, T2> void receive2(final Class<?> class_, final String signalName, final Context ctx, final T1 p1, final T2 p2, final Consumer0 defaultBody) {
		this.checkMethodExists(class_, signalName, 2);
		final Tuple3<Class<?>, String, Context> key = new Tuple3<>(class_, signalName, ctx);
		final NamedSignal<Void> ns = new NamedSignal<>(signalName, () -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen?  and handle timeout!
			if (null != value) {
				final Consumer2<T1, T2> body = (Consumer2<T1, T2>) value;
				body.accept(p1, p2);
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
			return null;
		});
		this.signals.add(ns);
	}

	/**
	 * deprecated, use 'execute' instead.
	 */
	@Deprecated()
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
