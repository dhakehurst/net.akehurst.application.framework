package net.akehurst.application.framework.realisation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

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

public class MethodUtils {

	public static <T> T uncheckedFutureGet(final Future<T> future) {
		try {
			return future.get();
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static <C> Method getMethodLiteral(final Class<C> class_, final Consumer1<C> lit) {
		final CompletableFuture<Method> res = new CompletableFuture<>();
		final InvocationHandler handler = (proxy, method, args) -> res.complete(method);
		final C c = (C) Proxy.newProxyInstance(class_.getClassLoader(), new Class[] { class_ }, handler);
		lit.accept(c);
		return MethodUtils.uncheckedFutureGet(res);
	}

	public static <C, R> Method getMethodLiteral(final Class<C> class_, final Function1<C, R> lit) {
		final CompletableFuture<Method> res = new CompletableFuture<>();
		final InvocationHandler handler = (proxy, method, args) -> {
			res.complete(method);
			return null;
		};
		final C c = (C) Proxy.newProxyInstance(class_.getClassLoader(), new Class[] { class_ }, handler);
		lit.apply(c);
		return MethodUtils.uncheckedFutureGet(res);
	}

	public static <C, P1> Method getMethodLiteral(final Class<C> class_, final Consumer2<C, P1> lit) {
		final CompletableFuture<Method> res = new CompletableFuture<>();
		final InvocationHandler handler = (proxy, method, args) -> res.complete(method);
		final C c = (C) Proxy.newProxyInstance(class_.getClassLoader(), new Class[] { class_ }, handler);
		lit.accept(c, null);
		return MethodUtils.uncheckedFutureGet(res);
	}

	public static <C, P1, R> Method getMethodLiteral(final Class<C> class_, final Function2<C, P1, R> lit) {
		final CompletableFuture<Method> res = new CompletableFuture<>();
		final InvocationHandler handler = (proxy, method, args) -> {
			res.complete(method);
			return null;
		};
		final C c = (C) Proxy.newProxyInstance(class_.getClassLoader(), new Class[] { class_ }, handler);
		lit.apply(c, null);
		return MethodUtils.uncheckedFutureGet(res);
	}

	public static <C, P1, P2> Method getMethodLiteral(final Class<C> class_, final Consumer3<C, P1, P2> lit) {
		final CompletableFuture<Method> res = new CompletableFuture<>();
		final InvocationHandler handler = (proxy, method, args) -> res.complete(method);
		final C c = (C) Proxy.newProxyInstance(class_.getClassLoader(), new Class[] { class_ }, handler);
		lit.accept(c, null, null);
		return MethodUtils.uncheckedFutureGet(res);
	}

	public static <C, P1, P2, R> Method getMethodLiteral(final Class<C> class_, final Function3<C, P1, P2, R> lit) {
		final CompletableFuture<Method> res = new CompletableFuture<>();
		final InvocationHandler handler = (proxy, method, args) -> {
			res.complete(method);
			return null;
		};
		final C c = (C) Proxy.newProxyInstance(class_.getClassLoader(), new Class[] { class_ }, handler);
		lit.apply(c, null, null);
		return MethodUtils.uncheckedFutureGet(res);
	}

	public static <C, P1, P2, P3> Method getMethodLiteral(final Class<C> class_, final Consumer4<C, P1, P2, P3> lit) {
		final CompletableFuture<Method> res = new CompletableFuture<>();
		final InvocationHandler handler = (proxy, method, args) -> res.complete(method);
		final C c = (C) Proxy.newProxyInstance(class_.getClassLoader(), new Class[] { class_ }, handler);
		lit.accept(c, null, null, null);
		return MethodUtils.uncheckedFutureGet(res);
	}

	public static <C, P1, P2, P3, R> Method getMethodLiteral(final Class<C> class_, final Function4<C, P1, P2, P3, R> lit) {
		final CompletableFuture<Method> res = new CompletableFuture<>();
		final InvocationHandler handler = (proxy, method, args) -> {
			res.complete(method);
			return null;
		};
		final C c = (C) Proxy.newProxyInstance(class_.getClassLoader(), new Class[] { class_ }, handler);
		lit.apply(c, null, null, null);
		return MethodUtils.uncheckedFutureGet(res);
	}

	public static <C, P1, P2, P3, P4> Method getMethodLiteral(final Class<C> class_, final Consumer5<C, P1, P2, P3, P4> lit) {
		final CompletableFuture<Method> res = new CompletableFuture<>();
		final InvocationHandler handler = (proxy, method, args) -> res.complete(method);
		final C c = (C) Proxy.newProxyInstance(class_.getClassLoader(), new Class[] { class_ }, handler);
		lit.accept(c, null, null, null, null);
		return MethodUtils.uncheckedFutureGet(res);
	}

	public static <C, P1, P2, P3, P4, R> Method getMethodLiteral(final Class<C> class_, final Function5<C, P1, P2, P3, P4, R> lit) {
		final CompletableFuture<Method> res = new CompletableFuture<>();
		final InvocationHandler handler = (proxy, method, args) -> {
			res.complete(method);
			return null;
		};
		final C c = (C) Proxy.newProxyInstance(class_.getClassLoader(), new Class[] { class_ }, handler);
		lit.apply(c, null, null, null, null);
		return MethodUtils.uncheckedFutureGet(res);
	}

	public static <C, P1, P2, P3, P4, P5> Method getMethodLiteral(final Class<C> class_, final Consumer6<C, P1, P2, P3, P4, P5> lit) {
		final CompletableFuture<Method> res = new CompletableFuture<>();
		final InvocationHandler handler = (proxy, method, args) -> res.complete(method);
		final C c = (C) Proxy.newProxyInstance(class_.getClassLoader(), new Class[] { class_ }, handler);
		lit.accept(c, null, null, null, null, null);
		return MethodUtils.uncheckedFutureGet(res);
	}

	public static <C, P1, P2, P3, P4, P5, R> Method getMethodLiteral(final Class<C> class_, final Function6<C, P1, P2, P3, P4, P5, R> lit) {
		final CompletableFuture<Method> res = new CompletableFuture<>();
		final InvocationHandler handler = (proxy, method, args) -> {
			res.complete(method);
			return null;
		};
		final C c = (C) Proxy.newProxyInstance(class_.getClassLoader(), new Class[] { class_ }, handler);
		lit.apply(c, null, null, null, null, null);
		return MethodUtils.uncheckedFutureGet(res);
	}

	public static <C, P1, P2, P3, P4, P5, P6> Method getMethodLiteral(final Class<C> class_, final Consumer7<C, P1, P2, P3, P4, P5, P6> lit) {
		final CompletableFuture<Method> res = new CompletableFuture<>();
		final InvocationHandler handler = (proxy, method, args) -> res.complete(method);
		final C c = (C) Proxy.newProxyInstance(class_.getClassLoader(), new Class[] { class_ }, handler);
		lit.accept(c, null, null, null, null, null, null);
		return MethodUtils.uncheckedFutureGet(res);
	}

	public static <C, P1, P2, P3, P4, P5, P6, R> Method getMethodLiteral(final Class<C> class_, final Function7<C, P1, P2, P3, P4, P5, P6, R> lit) {
		final CompletableFuture<Method> res = new CompletableFuture<>();
		final InvocationHandler handler = (proxy, method, args) -> {
			res.complete(method);
			return null;
		};
		final C c = (C) Proxy.newProxyInstance(class_.getClassLoader(), new Class[] { class_ }, handler);
		lit.apply(c, null, null, null, null, null, null);
		return MethodUtils.uncheckedFutureGet(res);
	}
}
