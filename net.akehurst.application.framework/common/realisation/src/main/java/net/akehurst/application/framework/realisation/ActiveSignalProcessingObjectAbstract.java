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
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.jooq.lambda.function.Consumer0;
import org.jooq.lambda.function.Consumer1;
import org.jooq.lambda.function.Consumer2;
import org.jooq.lambda.function.Consumer3;
import org.jooq.lambda.function.Consumer4;
import org.jooq.lambda.function.Consumer5;
import org.jooq.lambda.function.Consumer6;
import org.jooq.lambda.function.Consumer7;
import org.jooq.lambda.function.Function1;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Function4;
import org.jooq.lambda.function.Function5;
import org.jooq.lambda.function.Function6;
import org.jooq.lambda.function.Function7;
import org.jooq.lambda.tuple.Tuple2;

import net.akehurst.application.framework.common.ActiveObject;
import net.akehurst.application.framework.common.ActiveSignalProcessingObject;
import net.akehurst.application.framework.common.AsyncCall;
import net.akehurst.application.framework.common.Context;
import net.akehurst.application.framework.common.annotations.instance.ConfiguredValue;
import net.akehurst.application.framework.technology.interfaceLogging.ILogger;
import net.akehurst.application.framework.technology.interfaceLogging.LogLevel;

abstract public class ActiveSignalProcessingObjectAbstract extends ActiveObjectAbstract implements ActiveSignalProcessingObject {

	@ConfiguredValue(defaultValue = "1")
	Integer numThreads;

	private boolean terminateRequested;
	private ExecutorService executor;
	private final BlockingQueue<NamedSignal<?>> signals;
	private final Map<SignalKey, Object> waiting;

	public ActiveSignalProcessingObjectAbstract(final String afId) {
		super(afId);
		this.terminateRequested = false;
		this.signals = new LinkedBlockingQueue<>();
		this.waiting = new HashMap<>();
	}

	private class SignalKey extends Tuple2<Method, Context> {
		public SignalKey(final Method signature, final Context ctx) {
			super(signature, ctx);
		}
	}

	private class NamedSignal<R> {
		public NamedSignal(final Method signalSignature, final Context ctx, final Consumer1<SignalKey> body) {
			this.signalSignature = signalSignature;
			this.body = body;
			this.future = new FutureTask<>(() -> {
				try {
					final SignalKey key = new SignalKey(signalSignature, ctx);
					this.body.accept(key);
				} catch (final Throwable t) {
					ActiveSignalProcessingObjectAbstract.this.logger.log(LogLevel.ERROR, t.getMessage(), t);
				}
				return null;
			});
		}

		Method signalSignature;
		Consumer1<SignalKey> body;
		FutureTask<Void> future;

		String getSignature() {
			return this.signalSignature.getDeclaringClass().getSimpleName() + "::" + this.signalSignature.getName();
		}
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
			final List<ActiveObject> objects = super.afActiveParts();

			for (final ActiveObject ao : objects) {
				ao.afStart();
			}

			this.executor = Executors.newFixedThreadPool(this.numThreads, new NamedThreadFactory(this.afId(), this.logger));
			while (!this.terminateRequested) {
				try {
					final NamedSignal<?> ns = this.signals.take();
					if (this.executor.isShutdown()) {
						this.logger.log(LogLevel.TRACE, "Shutdown, not executing task %s", ns.getSignature());
					} else {
						this.executor.submit(() -> {
							try {
								this.logger.log(LogLevel.TRACE, ns.getSignature());
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

			for (final ActiveObject ao : objects) {
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
		this.receive(ActiveObject.class, ActiveObject::afInterrupt, null, () -> {
		});
		this.executor.shutdownNow();
	}

	@Override
	public void afTerminate() {
		this.terminateRequested = true;
		this.receive(ActiveObject.class, ActiveObject::afTerminate, null, () -> {
		});
		this.executor.shutdown();
	}

	//	protected AsyncCall execute(final Method signalSignature, final Context ctx, final Consumer0 body) {
	//		final NamedSignal<Void> ns = new NamedSignal<>(signalSignature, ctx, (key) -> {
	//			body.accept();
	//		});
	//		this.signals.add(ns);
	//		return new AsyncCallDefault(this);
	//	}
	//
	//	protected <C, R> AsyncCall execute(final Class<C> class_, final Function1<C, R> signalSignature, final Context ctx, final Consumer0 body) {
	//		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
	//		return this.execute(m, ctx, body);
	//	}

	protected AsyncCall send(final Runnable toSend) {
		return new AsyncCallDefault(this, toSend);
	}

	protected <I, R> void whenReceivedThenExecute(final Method signalSignature, final Context ctx, final long timeout, final Object body) {
		final SignalKey key = new SignalKey(signalSignature, ctx);
		this.waiting.put(key, body);
	}

	@Override
	public <I, R> void whenReceivedThenExecute0(final Class<I> class_, final Function1<I, R> signalSignature, final Context ctx, final long timeout,
			final Consumer0 body) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.whenReceivedThenExecute(m, ctx, timeout, body);
	}

	@Override
	public <I> void whenReceivedThenExecute0(final Class<I> class_, final Consumer1<I> signalSignature, final Context ctx, final long timeout,
			final Consumer0 body) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.whenReceivedThenExecute(m, ctx, timeout, body);
	}

	@Override
	public <I, P1> void whenReceivedThenExecute1(final Class<I> class_, final Consumer2<I, P1> signalSignature, final Context ctx, final long timeout,
			final Consumer1<P1> body) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.whenReceivedThenExecute(m, ctx, timeout, body);
	}

	@Override
	public <I, P1, R> void whenReceivedThenExecute1(final Class<I> class_, final Function2<I, P1, R> signalSignature, final Context ctx, final long timeout,
			final Consumer1<P1> body) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.whenReceivedThenExecute(m, ctx, timeout, body);
	}

	@Override
	public <I, P1, P2> void whenReceivedThenExecute2(final Class<I> class_, final Consumer3<I, P1, P2> signalSignature, final Context ctx, final long timeout,
			final Consumer2<P1, P2> body) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.whenReceivedThenExecute(m, ctx, timeout, body);
	}

	@Override
	public <I, P1, P2, R> void whenReceivedThenExecute2(final Class<I> class_, final Function3<I, P1, P2, R> signalSignature, final Context ctx,
			final long timeout, final Consumer2<P1, P2> body) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.whenReceivedThenExecute(m, ctx, timeout, body);
	}

	@Override
	public <I, P1, P2, P3> void whenReceivedThenExecute3(final Class<I> class_, final Consumer4<I, P1, P2, P3> signalSignature, final Context ctx,
			final long timeout, final Consumer3<P1, P2, P3> body) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.whenReceivedThenExecute(m, ctx, timeout, body);
	}

	@Override
	public <I, P1, P2, P3, R> void whenReceivedThenExecute3(final Class<I> class_, final Function4<I, P1, P2, P3, R> signalSignature, final Context ctx,
			final long timeout, final Consumer3<P1, P2, P3> body) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.whenReceivedThenExecute(m, ctx, timeout, body);
	}

	@Override
	public <I, P1, P2, P3, P4> void whenReceivedThenExecute4(final Class<I> class_, final Consumer5<I, P1, P2, P3, P4> signalSignature, final Context ctx,
			final long timeout, final Consumer4<P1, P2, P3, P4> body) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.whenReceivedThenExecute(m, ctx, timeout, body);
	}

	@Override
	public <I, P1, P2, P3, P4, R> void whenReceivedThenExecute4(final Class<I> class_, final Function5<I, P1, P2, P3, P4, R> signalSignature, final Context ctx,
			final long timeout, final Consumer4<P1, P2, P3, P4> body) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.whenReceivedThenExecute(m, ctx, timeout, body);
	}

	@Override
	public <I, P1, P2, P3, P4, P5> void whenReceivedThenExecute5(final Class<I> class_, final Consumer6<I, P1, P2, P3, P4, P5> signalSignature,
			final Context ctx, final long timeout, final Consumer5<P1, P2, P3, P4, P5> body) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.whenReceivedThenExecute(m, ctx, timeout, body);
	}

	@Override
	public <I, P1, P2, P3, P4, P5, R> void whenReceivedThenExecute5(final Class<I> class_, final Function6<I, P1, P2, P3, P4, P5, R> signalSignature,
			final Context ctx, final long timeout, final Consumer5<P1, P2, P3, P4, P5> body) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.whenReceivedThenExecute(m, ctx, timeout, body);
	}

	@Override
	public <I, P1, P2, P3, P4, P5, P6> void whenReceivedThenExecute6(final Class<I> class_, final Consumer7<I, P1, P2, P3, P4, P5, P6> signalSignature,
			final Context ctx, final long timeout, final Consumer6<P1, P2, P3, P4, P5, P6> body) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.whenReceivedThenExecute(m, ctx, timeout, body);
	}

	@Override
	public <I, P1, P2, P3, P4, P5, P6, R> void whenReceivedThenExecute6(final Class<I> class_, final Function7<I, P1, P2, P3, P4, P5, P6, R> signalSignature,
			final Context ctx, final long timeout, final Consumer6<P1, P2, P3, P4, P5, P6> body) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.whenReceivedThenExecute(m, ctx, timeout, body);
	}

	protected void receive(final Method signalSignature, final Context ctx, final Consumer1<SignalKey> body) {
		final NamedSignal<Void> ns = new NamedSignal<>(signalSignature, ctx, body);
		this.signals.add(ns);
	}

	protected <C, R> void receive(final Class<C> class_, final Consumer1<C> signalSignature, final Context ctx, final Consumer0 defaultBody) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.receive(m, ctx, (key) -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer0 body = (Consumer0) value;
				body.accept();
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
		});
	}

	protected <C, R> void receive(final Class<C> class_, final Function1<C, R> signalSignature, final Context ctx, final Consumer0 defaultBody) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.receive(m, ctx, (key) -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer0 body = (Consumer0) value;
				body.accept();
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
		});
	}

	protected <C, P1> void receive(final Class<C> class_, final Consumer2<C, P1> signalSignature, final Context ctx, final P1 p1, final Consumer0 defaultBody) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.receive(m, ctx, (key) -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer1<P1> body = (Consumer1<P1>) value;
				body.accept(p1);
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
		});
	}

	protected <C, P1, R> void receive(final Class<C> class_, final Function2<C, P1, R> signalSignature, final Context ctx, final P1 p1,
			final Consumer0 defaultBody) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.receive(m, ctx, (key) -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer1<P1> body = (Consumer1<P1>) value;
				body.accept(p1);
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
		});
	}

	protected <C, P1, P2> void receive(final Class<C> class_, final Consumer3<C, P1, P2> signalSignature, final Context ctx, final P1 p1, final P2 p2,
			final Consumer0 defaultBody) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.receive(m, ctx, (key) -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer2<P1, P2> body = (Consumer2<P1, P2>) value;
				body.accept(p1, p2);
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
		});
	}

	protected <C, P1, P2, R> void receive(final Class<C> class_, final Function3<C, P1, P2, R> signalSignature, final Context ctx, final P1 p1, final P2 p2,
			final Consumer0 defaultBody) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.receive(m, ctx, (key) -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer2<P1, P2> body = (Consumer2<P1, P2>) value;
				body.accept(p1, p2);
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
		});
	}

	protected <C, P1, P2, P3> void receive(final Class<C> class_, final Consumer4<C, P1, P2, P3> signalSignature, final Context ctx, final P1 p1, final P2 p2,
			final P3 p3, final Consumer0 defaultBody) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.receive(m, ctx, (key) -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer3<P1, P2, P3> body = (Consumer3<P1, P2, P3>) value;
				body.accept(p1, p2, p3);
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
		});
	}

	protected <C, P1, P2, P3, R> void receive(final Class<C> class_, final Function4<C, P1, P2, P3, R> signalSignature, final Context ctx, final P1 p1,
			final P2 p2, final P3 p3, final Consumer0 defaultBody) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.receive(m, ctx, (key) -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer3<P1, P2, P3> body = (Consumer3<P1, P2, P3>) value;
				body.accept(p1, p2, p3);
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
		});
	}

	protected <C, P1, P2, P3, P4> void receive(final Class<C> class_, final Consumer5<C, P1, P2, P3, P4> signalSignature, final Context ctx, final P1 p1,
			final P2 p2, final P3 p3, final P4 p4, final Consumer0 defaultBody) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.receive(m, ctx, (key) -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer4<P1, P2, P3, P4> body = (Consumer4<P1, P2, P3, P4>) value;
				body.accept(p1, p2, p3, p4);
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
		});
	}

	protected <C, P1, P2, P3, P4, R> void receive(final Class<C> class_, final Function5<C, P1, P2, P3, P4, R> signalSignature, final Context ctx, final P1 p1,
			final P2 p2, final P3 p3, final P4 p4, final Consumer0 defaultBody) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.receive(m, ctx, (key) -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer4<P1, P2, P3, P4> body = (Consumer4<P1, P2, P3, P4>) value;
				body.accept(p1, p2, p3, p4);
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
		});
	}

	protected <C, P1, P2, P3, P4, P5> void receive(final Class<C> class_, final Consumer6<C, P1, P2, P3, P4, P5> signalSignature, final Context ctx,
			final P1 p1, final P2 p2, final P3 p3, final P4 p4, final P5 p5, final Consumer0 defaultBody) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.receive(m, ctx, (key) -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer5<P1, P2, P3, P4, P5> body = (Consumer5<P1, P2, P3, P4, P5>) value;
				body.accept(p1, p2, p3, p4, p5);
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
		});
	}

	protected <C, P1, P2, P3, P4, P5, R> void receive(final Class<C> class_, final Function6<C, P1, P2, P3, P4, P5, R> signalSignature, final Context ctx,
			final P1 p1, final P2 p2, final P3 p3, final P4 p4, final P5 p5, final Consumer0 defaultBody) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.receive(m, ctx, (key) -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer5<P1, P2, P3, P4, P5> body = (Consumer5<P1, P2, P3, P4, P5>) value;
				body.accept(p1, p2, p3, p4, p5);
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
		});
	}

	protected <C, P1, P2, P3, P4, P5, P6> void receive(final Class<C> class_, final Consumer7<C, P1, P2, P3, P4, P5, P6> signalSignature, final Context ctx,
			final P1 p1, final P2 p2, final P3 p3, final P4 p4, final P5 p5, final P6 p6, final Consumer0 defaultBody) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.receive(m, ctx, (key) -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer6<P1, P2, P3, P4, P5, P6> body = (Consumer6<P1, P2, P3, P4, P5, P6>) value;
				body.accept(p1, p2, p3, p4, p5, p6);
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
		});
	}

	protected <C, P1, P2, P3, P4, P5, P6, R> void receive(final Class<C> class_, final Function7<C, P1, P2, P3, P4, P5, P6, R> signalSignature,
			final Context ctx, final P1 p1, final P2 p2, final P3 p3, final P4 p4, final P5 p5, final P6 p6, final Consumer0 defaultBody) {
		final Method m = MethodUtils.getMethodLiteral(class_, signalSignature);
		this.receive(m, ctx, (key) -> {
			final Object value = this.waiting.get(key); //TODO: what if we process the receive before the whenRecieved...can this happen? and handle timeout!
			if (null != value) {
				final Consumer6<P1, P2, P3, P4, P5, P6> body = (Consumer6<P1, P2, P3, P4, P5, P6>) value;
				body.accept(p1, p2, p3, p4, p5, p6);
			} else {
				if (null != defaultBody) {
					defaultBody.accept();
				}
			}
		});
	}

	//	/**
	//	 * deprecated, use 'receive' instead.
	//	 */
	//	@Deprecated()
	//	protected Future<Void> submit(final String name, final ISignal signal) {
	//		final NamedSignal<Void> ns = new NamedSignal<>(name, () -> {
	//			signal.execute();
	//			return null;
	//		});
	//		this.signals.add(ns);
	//		return ns.future;
	//	}
	//
	//	protected <T> Future<T> submit(final String name, final Class<T> returnType, final ISignalR<T> signal) {
	//		final NamedSignal<T> ns = new NamedSignal<>(name, signal);
	//		this.signals.add(ns);
	//		return ns.future;
	//	}
}
