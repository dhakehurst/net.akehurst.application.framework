package net.akehurst.application.framework.common;

import org.jooq.lambda.function.Consumer0;
import org.jooq.lambda.function.Consumer1;
import org.jooq.lambda.function.Consumer2;
import org.jooq.lambda.function.Consumer3;

public interface ActiveProcessor {

	<T, P1> MessageBuilder send(Class<T> contractInterface, Consumer2<? super T, P1> message, P1 p1);

	<T, P1, P2> void send(Class<T> contractInterface, Consumer3<? super T, P1, P2> message, P1 p1, P2 p2);

	<T> void receive(Class<T> contractInterface, Consumer0 message);

	<T> void receive0(Class<T> contractInterface, Consumer1<? super T> message, Consumer0 task);

	<T, P1> void receive1(Class<T> contractInterface, Consumer2<? super T, P1> message, Consumer1<P1> task);

}
