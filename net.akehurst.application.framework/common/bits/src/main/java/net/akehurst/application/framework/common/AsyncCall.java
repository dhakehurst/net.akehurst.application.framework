package net.akehurst.application.framework.common;

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

public interface AsyncCall {

	enum WaitResult {
		complete, timeout, interrupted
	}

	/**
	 * do the call now
	 */
	void go();

	/**
	 *
	 * @param timeout
	 * @param unit
	 * @return result of waiting
	 */
	WaitResult waitUntilFinished(long timeout, TimeUnit unit);

	<I> AsyncCall andWhen(Context ctx, Class<I> class_, Consumer1<I> signalSignature, long timeout, TimeUnit unit, Consumer0 body);

	<I, R> AsyncCall andWhen(Context ctx, Class<I> class_, Function1<I, R> signalSignature, long timeout, TimeUnit unit, Consumer0 body);

	<I, P1> AsyncCall andWhen(Context ctx, Class<I> class_, Consumer2<I, P1> signalSignature, long timeout, TimeUnit unit, Consumer1<P1> body);

	<I, P1, R> AsyncCall andWhen(Context ctx, Class<I> class_, Function2<I, P1, R> signalSignature, long timeout, TimeUnit unit, Consumer1<P1> body);

	<I, P1, P2> AsyncCall andWhen(Context ctx, Class<I> class_, Consumer3<I, P1, P2> signalSignature, long timeout, TimeUnit unit, Consumer2<P1, P2> body);

	<I, P1, P2, R> AsyncCall andWhen(Context ctx, Class<I> class_, Function3<I, P1, P2, R> signalSignature, long timeout, TimeUnit unit,
			Consumer2<P1, P2> body);

	<I, P1, P2, P3> AsyncCall andWhen(Context ctx, Class<I> class_, Consumer4<I, P1, P2, P3> signalSignature, long timeout, TimeUnit unit,
			Consumer3<P1, P2, P3> body);

	<I, P1, P2, P3, R> AsyncCall andWhen(Context ctx, Class<I> class_, Function4<I, P1, P2, P3, R> signalSignature, long timeout, TimeUnit unit,
			Consumer3<P1, P2, P3> body);

}
