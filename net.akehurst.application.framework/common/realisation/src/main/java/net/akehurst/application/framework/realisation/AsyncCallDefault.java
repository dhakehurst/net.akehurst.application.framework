/**
 * Copyright (C) 2018 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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

import java.util.concurrent.TimeUnit;

import org.jooq.lambda.function.Consumer0;
import org.jooq.lambda.function.Consumer1;
import org.jooq.lambda.function.Consumer2;
import org.jooq.lambda.function.Consumer3;
import org.jooq.lambda.function.Consumer4;
import org.jooq.lambda.function.Function1;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Function4;

import net.akehurst.application.framework.common.ActiveSignalProcessingObject;
import net.akehurst.application.framework.common.AsyncCall;
import net.akehurst.application.framework.common.Context;

public class AsyncCallDefault implements AsyncCall {

	private final ActiveSignalProcessingObject activeSignalProcessingObject;
	private final boolean completed;
	private final Runnable toSend;

	public AsyncCallDefault(final ActiveSignalProcessingObject activeSignalProcessingObject, final Runnable toSend) {
		this.activeSignalProcessingObject = activeSignalProcessingObject;
		this.toSend = toSend;
		this.completed = false;
	}

	@Override
	public AsyncCall.WaitResult waitUntilFinished(final long timeout, final TimeUnit unit) {
		try {
			final long msecs = unit.toMillis(timeout);
			final long startTime = msecs <= 0 ? 0 : System.currentTimeMillis();
			long waitTime = msecs;
			if (this.completed) {
				return AsyncCall.WaitResult.complete;
			} else if (waitTime <= 0) {
				return AsyncCall.WaitResult.timeout;
			} else {
				for (;;) {
					this.wait(waitTime);
					if (this.completed) {
						return AsyncCall.WaitResult.complete;
					} else {
						waitTime = msecs - (System.currentTimeMillis() - startTime);
						if (waitTime <= 0) {
							return AsyncCall.WaitResult.timeout;
						}
					}
				}
			}
		} catch (final InterruptedException e) {
			return AsyncCall.WaitResult.interrupted;
		}
	}

	@Override
	public <I> AsyncCall andWhen(final Context ctx, final Class<I> class_, final Consumer1<I> signalSignature, final long timeout, final TimeUnit unit,
			final Consumer0 body) {
		this.activeSignalProcessingObject.whenReceivedThenExecute0(class_, signalSignature, ctx, timeout, body);
		return this;
	}

	@Override
	public <I, R> AsyncCall andWhen(final Context ctx, final Class<I> class_, final Function1<I, R> signalSignature, final long timeout, final TimeUnit unit,
			final Consumer0 body) {
		this.activeSignalProcessingObject.whenReceivedThenExecute0(class_, signalSignature, ctx, timeout, body);
		return this;
	}

	@Override
	public <I, P1> AsyncCall andWhen(final Context ctx, final Class<I> class_, final Consumer2<I, P1> signalSignature, final long timeout, final TimeUnit unit,
			final Consumer1<P1> body) {
		this.activeSignalProcessingObject.whenReceivedThenExecute1(class_, signalSignature, ctx, timeout, body);
		return this;
	}

	@Override
	public <I, P1, R> AsyncCall andWhen(final Context ctx, final Class<I> class_, final Function2<I, P1, R> signalSignature, final long timeout,
			final TimeUnit unit, final Consumer1<P1> body) {
		this.activeSignalProcessingObject.whenReceivedThenExecute1(class_, signalSignature, ctx, timeout, body);
		return this;
	}

	@Override
	public <I, P1, P2> AsyncCall andWhen(final Context ctx, final Class<I> class_, final Consumer3<I, P1, P2> signalSignature, final long timeout,
			final TimeUnit unit, final Consumer2<P1, P2> body) {
		this.activeSignalProcessingObject.whenReceivedThenExecute2(class_, signalSignature, ctx, timeout, body);
		return this;
	}

	@Override
	public <I, P1, P2, R> AsyncCall andWhen(final Context ctx, final Class<I> class_, final Function3<I, P1, P2, R> signalSignature, final long timeout,
			final TimeUnit unit, final Consumer2<P1, P2> body) {
		this.activeSignalProcessingObject.whenReceivedThenExecute2(class_, signalSignature, ctx, timeout, body);
		return this;
	}

	@Override
	public <I, P1, P2, P3> AsyncCall andWhen(final Context ctx, final Class<I> class_, final Consumer4<I, P1, P2, P3> signalSignature, final long timeout,
			final TimeUnit unit, final Consumer3<P1, P2, P3> body) {
		this.activeSignalProcessingObject.whenReceivedThenExecute3(class_, signalSignature, ctx, timeout, body);
		return this;
	}

	@Override
	public <I, P1, P2, P3, R> AsyncCall andWhen(final Context ctx, final Class<I> class_, final Function4<I, P1, P2, P3, R> signalSignature, final long timeout,
			final TimeUnit unit, final Consumer3<P1, P2, P3> body) {
		this.activeSignalProcessingObject.whenReceivedThenExecute3(class_, signalSignature, ctx, timeout, body);
		return this;
	}

	@Override
	public void go() {
		this.toSend.run();
	}

}
