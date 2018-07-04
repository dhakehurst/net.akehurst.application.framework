package net.akehurst.application.framework.common;

import java.util.concurrent.TimeUnit;

import org.jooq.lambda.function.Consumer0;
import org.jooq.lambda.function.Consumer1;
import org.jooq.lambda.function.Consumer2;

public interface AsyncCall {

	enum WaitResult {
		complete, timeout, interrupted
	}

	/**
	 *
	 * @param timeout
	 * @param unit
	 * @return result of waiting
	 */
	WaitResult waitUntilFinished(long timeout, TimeUnit unit);

	void andWhen(Context ctx, Class<?> class_, String signalName, Consumer0 func);

	<T1> void andWhen(Context ctx, Class<?> class_, String signalName, Consumer1<T1> func);

	<T1, T2> void andWhen(Context ctx, Class<?> class_, String signalName, Consumer2<T1, T2> func);

}
