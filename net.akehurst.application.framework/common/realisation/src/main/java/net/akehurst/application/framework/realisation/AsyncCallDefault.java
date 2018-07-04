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

import net.akehurst.application.framework.common.ActiveSignalProcessingObject;
import net.akehurst.application.framework.common.AsyncCall;
import net.akehurst.application.framework.common.Context;

public class AsyncCallDefault implements AsyncCall {

	private final ActiveSignalProcessingObject activeSignalProcessingObject;
	private final boolean completed;

	public AsyncCallDefault(final ActiveSignalProcessingObject activeSignalProcessingObject) {
		this.activeSignalProcessingObject = activeSignalProcessingObject;
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
	public void andWhen(final Context ctx, final Class<?> class_, final String signalName, final Consumer0 func) {
		this.activeSignalProcessingObject.whenReceivedThenExecute0(class_, signalName, ctx, -1, func);
	}

	@Override
	public <T1> void andWhen(final Context ctx, final Class<?> class_, final String signalName, final Consumer1<T1> func) {
		this.activeSignalProcessingObject.whenReceivedThenExecute1(class_, signalName, ctx, -1, func);
	}

	@Override
	public <T1, T2> void andWhen(final Context ctx, final Class<?> class_, final String signalName, final Consumer2<T1, T2> func) {
		this.activeSignalProcessingObject.whenReceivedThenExecute2(class_, signalName, ctx, -1, func);
	}

}
