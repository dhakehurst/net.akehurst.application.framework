package net.akehurst.application.framework.common.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.easymock.EasyMock;
import org.easymock.IExpectationSetters;

public class AbstractTestCase {

	public AbstractTestCase() {
		this.mocks = new HashSet<>();
		this.steps = new ArrayList<>();
	}

	Set<Object> mocks;
	List<Runnable> steps;

	public interface IMockExpectation<R> {
		IMockExpectation<R> andReturn(R returnValue);
	}

	public interface Func0<T, R> {
		R apply(T t);
	}

	public interface Func1<T, R, P1> {
		R apply(T t, P1 p1);
	}

	public interface Func2<T, R, P1, P2> {
		R apply(T t, P1 p1, P2 p2);
	}

	public interface Func3<T, R, P1, P2, P3> {
		R apply(T t, P1 p1, P2 p2, P3 p3);
	}

	public interface Func4<T, R, P1, P2, P3, P4> {
		R apply(T t, P1 p1, P2 p2, P3 p3, P4 p4);
	}

	public interface Proc0<T> {
		void call(T t);
	}

	public interface Proc1<T, P1> {
		void call(T t, P1 p1);
	}

	public interface Proc2<T, P1, P2> {
		void call(T t, P1 p1, P2 p2);
	}

	public interface Proc3<T, P1, P2, P3> {
		void call(T t, P1 p1, P2 p2, P3 p3);
	}

	public interface Proc4<T, P1, P2, P3, P4> {
		void call(T t, P1 p1, P2 p2, P3 p3, P4 p4);
	}

	public <TO extends T, T, P1> void expect(final TO toObj, final Proc1<T, P1> proc, final P1 p1) {
		this.mocks.add(toObj);
		proc.call(toObj, p1);
		EasyMock.expectLastCall().once();
	}

	public <TO extends T, T, P1, P2> void expect(final TO toObj, final Proc2<T, P1, P2> proc, final P1 p1, final P2 p2) {
		this.mocks.add(toObj);
		proc.call(toObj, p1, p2);
		EasyMock.expectLastCall().once();
	}

	public <TO extends T, T, P1, P2, P3> void expect(final TO toObj, final Proc3<T, P1, P2, P3> func, final P1 p1, final P2 p2, final P3 p3) {
		this.mocks.add(toObj);
		func.call(toObj, p1, p2, p3);
		EasyMock.expectLastCall().once();
	}

	public <TO extends T, T, P1, P2, P3, P4> void expect(final TO toObj, final Proc4<T, P1, P2, P3, P4> func, final P1 p1, final P2 p2, final P3 p3,
			final P4 p4) {
		this.mocks.add(toObj);
		func.call(toObj, p1, p2, p3, p4);
		EasyMock.expectLastCall().once();
	}

	public <R, TO extends T, T, P1> IMockExpectation<R> expect(final TO toObj, final Func1<T, R, P1> func, final P1 p1) {
		this.mocks.add(toObj);
		func.apply(toObj, p1);
		final IExpectationSetters<R> easy = EasyMock.expectLastCall();
		return new IMockExpectation<R>() {
			@Override
			public IMockExpectation<R> andReturn(final R returnValue) {
				easy.andReturn(returnValue);
				return this;
			}
		};

	}

	public <R, TO extends T, T, P1, P2> IMockExpectation<R> expect(final TO toObj, final Func2<T, R, P1, P2> func, final P1 p1, final P2 p2) {
		this.mocks.add(toObj);
		func.apply(toObj, p1, p2);
		final IExpectationSetters<R> easy = EasyMock.expectLastCall();
		return new IMockExpectation<R>() {
			@Override
			public IMockExpectation<R> andReturn(final R returnValue) {
				easy.andReturn(returnValue);
				return this;
			}
		};
	}

	public <R, TO extends T, T, P1, P2, P3> IMockExpectation<R> expect(final TO toObj, final Func3<T, R, P1, P2, P3> func, final P1 p1, final P2 p2,
			final P3 p3) {
		this.mocks.add(toObj);
		func.apply(toObj, p1, p2, p3);
		final IExpectationSetters<R> easy = EasyMock.expectLastCall();
		return new IMockExpectation<R>() {
			@Override
			public IMockExpectation<R> andReturn(final R returnValue) {
				easy.andReturn(returnValue);
				return this;
			}
		};
	}

	public <R, TO extends T, T, P1, P2, P3, P4> IMockExpectation<R> expect(final TO toObj, final Func4<T, R, P1, P2, P3, P4> func, final P1 p1, final P2 p2,
			final P3 p3, final P4 p4) {
		this.mocks.add(toObj);
		func.apply(toObj, p1, p2, p3, p4);
		final IExpectationSetters<R> easy = EasyMock.expectLastCall();
		return new IMockExpectation<R>() {
			@Override
			public IMockExpectation<R> andReturn(final R returnValue) {
				easy.andReturn(returnValue);
				return this;
			}
		};
	}

	public <TO extends T, T, P1, P2> void perform(final TO toObj, final Proc2<T, P1, P2> func, final P1 p1, final P2 p2) {
		this.steps.add(() -> func.call(toObj, p1, p2));
	}

	public <TO extends T, T, P1, P2, P3> void perform(final TO toObj, final Proc3<T, P1, P2, P3> func, final P1 p1, final P2 p2, final P3 p3) {
		this.steps.add(() -> func.call(toObj, p1, p2, p3));
	}

	public <TO extends T, T, P1, P2, P3, P4> void perform(final TO toObj, final Proc4<T, P1, P2, P3, P4> func, final P1 p1, final P2 p2, final P3 p3,
			final P4 p4) {
		this.steps.add(() -> func.call(toObj, p1, p2, p3, p4));
	}

	public <TO extends T, T, P1> void perform(final TO toObj, final Proc1<T, P1> func, final P1 p1) {
		this.steps.add(() -> func.call(toObj, p1));
	}

	public <R, TO extends T, T, P1> Future<R> perform(final TO toObj, final Func1<T, R, P1> func, final P1 p1) {
		final FutureTask<R> result = new FutureTask<>(() -> func.apply(toObj, p1));
		this.steps.add(() -> result.run());
		return result;
	}

	public <R, TO extends T, T, P1, P2> Future<R> perform(final TO toObj, final Func2<T, R, P1, P2> func, final P1 p1, final P2 p2) {
		final FutureTask<R> result = new FutureTask<>(() -> func.apply(toObj, p1, p2));
		this.steps.add(() -> result.run());
		return result;
	}

	/**
	 * cause a delay in the scenario playback for the given number of milliseconds
	 *
	 * @param milliseconds
	 */
	public void delay(final int milliseconds) {
		this.steps.add(() -> {
			try {
				Thread.sleep(milliseconds);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		});
	}

	public void play() {
		EasyMock.replay(this.mocks.toArray());
		for (final Runnable r : this.steps) {
			r.run();
		}

	}

	/**
	 * put the current thread to sleep for the given number of milliseconds
	 *
	 * @param milliseconds
	 */
	public void sleep(final long milliseconds) {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void verify() {
		EasyMock.verify(this.mocks.toArray());
	}
}
