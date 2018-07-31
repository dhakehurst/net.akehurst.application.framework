package net.akehurst.application.framework.common;

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

public interface ActiveSignalProcessingObject {

	<I> void whenReceivedThenExecute0(Class<I> class_, final Consumer1<I> signalSignature, Context ctx, long timeout, Consumer0 body);

	<I, R> void whenReceivedThenExecute0(Class<I> class_, final Function1<I, R> signalSignature, Context ctx, long timeout, Consumer0 body);

	<I, P1> void whenReceivedThenExecute1(Class<I> class_, final Consumer2<I, P1> signalSignature, Context ctx, long timeout, Consumer1<P1> body);

	<I, P1, R> void whenReceivedThenExecute1(Class<I> class_, final Function2<I, P1, R> signalSignature, Context ctx, long timeout, Consumer1<P1> body);

	<I, P1, P2> void whenReceivedThenExecute2(Class<I> class_, final Consumer3<I, P1, P2> signalSignature, Context ctx, long timeout, Consumer2<P1, P2> body);

	<I, P1, P2, R> void whenReceivedThenExecute2(Class<I> class_, final Function3<I, P1, P2, R> signalSignature, Context ctx, long timeout,
			Consumer2<P1, P2> body);

	<I, P1, P2, P3> void whenReceivedThenExecute3(Class<I> class_, final Consumer4<I, P1, P2, P3> signalSignature, Context ctx, long timeout,
			Consumer3<P1, P2, P3> body);

	<I, P1, P2, P3, R> void whenReceivedThenExecute3(Class<I> class_, final Function4<I, P1, P2, P3, R> signalSignature, Context ctx, long timeout,
			Consumer3<P1, P2, P3> body);

	<I, P1, P2, P3, P4> void whenReceivedThenExecute4(Class<I> class_, final Consumer5<I, P1, P2, P3, P4> signalSignature, Context ctx, long timeout,
			Consumer4<P1, P2, P3, P4> body);

	<I, P1, P2, P3, P4, R> void whenReceivedThenExecute4(Class<I> class_, final Function5<I, P1, P2, P3, P4, R> signalSignature, Context ctx, long timeout,
			Consumer4<P1, P2, P3, P4> body);

	<I, P1, P2, P3, P4, P5> void whenReceivedThenExecute5(Class<I> class_, final Consumer6<I, P1, P2, P3, P4, P5> signalSignature, Context ctx, long timeout,
			Consumer5<P1, P2, P3, P4, P5> body);

	<I, P1, P2, P3, P4, P5, R> void whenReceivedThenExecute5(Class<I> class_, final Function6<I, P1, P2, P3, P4, P5, R> signalSignature, Context ctx,
			long timeout, Consumer5<P1, P2, P3, P4, P5> body);

	<I, P1, P2, P3, P4, P5, P6> void whenReceivedThenExecute6(Class<I> class_, final Consumer7<I, P1, P2, P3, P4, P5, P6> signalSignature, Context ctx,
			long timeout, Consumer6<P1, P2, P3, P4, P5, P6> body);

	<I, P1, P2, P3, P4, P5, P6, R> void whenReceivedThenExecute6(Class<I> class_, final Function7<I, P1, P2, P3, P4, P5, P6, R> signalSignature, Context ctx,
			long timeout, Consumer6<P1, P2, P3, P4, P5, P6> body);
}
