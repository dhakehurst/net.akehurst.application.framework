package net.akehurst.application.framework.common.realisation;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import net.akehurst.application.framework.realisation.MethodUtils;

public class test_MethodUtils {

	interface Signature {
		void signal0();

		Object signalR0();

		void signal1(Object arg1);

		Object signalR1(Object arg1);

		void signal2(Object arg1, Object arg2);

		Object signalR2(Object arg1, Object arg2);
	}

	@Test
	public void getMethodLiteral() {

		final Method m0 = MethodUtils.getMethodLiteral(Signature.class, Signature::signal0);
		final Method mR0 = MethodUtils.getMethodLiteral(Signature.class, Signature::signalR0);

		Assert.assertEquals("signal0", m0.getName());
		Assert.assertEquals("signalR0", mR0.getName());

	}

}
