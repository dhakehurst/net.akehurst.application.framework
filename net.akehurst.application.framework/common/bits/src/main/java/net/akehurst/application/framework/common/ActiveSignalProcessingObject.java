package net.akehurst.application.framework.common;

import org.jooq.lambda.function.Consumer0;
import org.jooq.lambda.function.Consumer1;
import org.jooq.lambda.function.Consumer2;

public interface ActiveSignalProcessingObject {

	void whenReceivedThenExecute0(Class<?> class_, String signalName, Context ctx, long timeout, Consumer0 body);

	<T1> void whenReceivedThenExecute1(Class<?> class_, String signalName, Context ctx, long timeout, Consumer1<T1> body);

	<T1, T2> void whenReceivedThenExecute2(Class<?> class_, String signalName, Context ctx, long timeout, Consumer2<T1, T2> body);

}
