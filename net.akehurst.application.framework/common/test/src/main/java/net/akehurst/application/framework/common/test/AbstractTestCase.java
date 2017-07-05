/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
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
package net.akehurst.application.framework.common.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IExpectationSetters;

public class AbstractTestCase {

	public AbstractTestCase() {
		this.reset();
	}

	Set<Object> mocks;
	List<FutureTask<?>> steps;

	public void reset() {
		this.mocks = new HashSet<>();
		this.steps = new ArrayList<>();
	}

	public interface IMockExpectation0<R> {
		interface Check {
			void execute();
		}

		IMockExpectation0<R> andCheck(Check check);

		IMockExpectation0<R> andReturn(R returnValue);
	}

	public interface IMockExpectation1<R, P1> {
		interface Check<P1> {
			void execute(P1 p1);
		}

		IMockExpectation1<R, P1> andCheck(Check<P1> check);

		IMockExpectation1<R, P1> andReturn(R returnValue);
	}

	public interface IMockExpectation2<R, P1, P2> {
		interface Check<P1, P2> {
			void execute(P1 p1, P2 p2);
		}

		IMockExpectation2<R, P1, P2> andCheck(Check<P1, P2> check);

		IMockExpectation2<R, P1, P2> andReturn(R returnValue);
	}

	public interface IMockExpectation3<R, P1, P2, P3> {
		interface Check<P1, P2, P3> {
			void execute(P1 p1, P2 p2, P3 p3);
		}

		IMockExpectation3<R, P1, P2, P3> andCheck(Check<P1, P2, P3> check);

		IMockExpectation3<R, P1, P2, P3> andReturn(R returnValue);
	}

	public interface IMockExpectation4<R, P1, P2, P3, P4> {
		interface Check<P1, P2, P3, P4> {
			void execute(P1 p1, P2 p2, P3 p3, P4 p4);
		}

		IMockExpectation4<R, P1, P2, P3, P4> andCheck(Check<P1, P2, P3, P4> check);

		IMockExpectation4<R, P1, P2, P3, P4> andReturn(R returnValue);
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

	public <TO extends T, T> IMockExpectation0<Void> expect(final TO toObj, final Proc0<T> proc) {
		this.mocks.add(toObj);
		EasyMock.expectLastCall().once();
		return new IMockExpectation0<Void>() {
			@Override
			public IMockExpectation0<Void> andCheck(final IMockExpectation0.Check check) {
				final FutureTask<Void> result = new FutureTask<>(() -> check.execute(), null);
				AbstractTestCase.this.steps.add(result);
				return this;
			}

			@Override
			public IMockExpectation0<Void> andReturn(final Void returnValue) {
				throw new UnsupportedOperationException();
			}
		};
	}

	public <TO extends T, T, P1> IMockExpectation1<Void, P1> expect(final TO toObj, final Proc1<T, P1> proc, final P1 p1) {
		this.mocks.add(toObj);
		final Capture<P1> real_p1 = EasyMock.newCapture();
		proc.call(toObj, EasyMock.capture(real_p1));
		EasyMock.expectLastCall().once();
		return new IMockExpectation1<Void, P1>() {
			@Override
			public IMockExpectation1<Void, P1> andCheck(final IMockExpectation1.Check<P1> check) {
				final FutureTask<Void> result = new FutureTask<>(() -> check.execute(real_p1.getValue()), null);
				AbstractTestCase.this.steps.add(result);
				return this;
			}

			@Override
			public IMockExpectation1<Void, P1> andReturn(final Void returnValue) {
				throw new UnsupportedOperationException();
			}
		};
	}

	public <TO extends T, T, P1, P2> IMockExpectation2<Void, P1, P2> expect(final TO toObj, final Proc2<T, P1, P2> proc, final P1 p1, final P2 p2) {
		this.mocks.add(toObj);
		final Capture<P1> real_p1 = EasyMock.newCapture();
		final Capture<P2> real_p2 = EasyMock.newCapture();
		proc.call(toObj, EasyMock.capture(real_p1), EasyMock.capture(real_p2));
		EasyMock.expectLastCall().once();
		return new IMockExpectation2<Void, P1, P2>() {
			@Override
			public IMockExpectation2<Void, P1, P2> andCheck(final IMockExpectation2.Check<P1, P2> check) {
				final FutureTask<Void> result = new FutureTask<>(() -> check.execute(real_p1.getValue(), real_p2.getValue()), null);
				AbstractTestCase.this.steps.add(result);
				return this;
			}

			@Override
			public IMockExpectation2<Void, P1, P2> andReturn(final Void returnValue) {
				throw new UnsupportedOperationException();
			}
		};
	}

	public <TO extends T, T, P1, P2, P3> IMockExpectation3<Void, P1, P2, P3> expect(final TO toObj, final Proc3<T, P1, P2, P3> proc, final P1 p1, final P2 p2,
			final P3 p3) {
		this.mocks.add(toObj);
		final Capture<P1> real_p1 = EasyMock.newCapture();
		final Capture<P2> real_p2 = EasyMock.newCapture();
		final Capture<P3> real_p3 = EasyMock.newCapture();
		proc.call(toObj, EasyMock.capture(real_p1), EasyMock.capture(real_p2), EasyMock.capture(real_p3));
		EasyMock.expectLastCall().once();
		return new IMockExpectation3<Void, P1, P2, P3>() {
			@Override
			public IMockExpectation3<Void, P1, P2, P3> andCheck(final IMockExpectation3.Check<P1, P2, P3> check) {
				final FutureTask<Void> result = new FutureTask<>(() -> check.execute(real_p1.getValue(), real_p2.getValue(), real_p3.getValue()), null);
				AbstractTestCase.this.steps.add(result);
				return this;
			}

			@Override
			public IMockExpectation3<Void, P1, P2, P3> andReturn(final Void returnValue) {
				throw new UnsupportedOperationException();
			}
		};
	}

	public <TO extends T, T, P1, P2, P3, P4> IMockExpectation4<Void, P1, P2, P3, P4> expect(final TO toObj, final Proc4<T, P1, P2, P3, P4> proc, final P1 p1,
			final P2 p2, final P3 p3, final P4 p4) {
		this.mocks.add(toObj);
		final Capture<P1> real_p1 = EasyMock.newCapture();
		final Capture<P2> real_p2 = EasyMock.newCapture();
		final Capture<P3> real_p3 = EasyMock.newCapture();
		final Capture<P4> real_p4 = EasyMock.newCapture();
		proc.call(toObj, EasyMock.capture(real_p1), EasyMock.capture(real_p2), EasyMock.capture(real_p3), EasyMock.capture(real_p4));
		EasyMock.expectLastCall().once();
		return new IMockExpectation4<Void, P1, P2, P3, P4>() {
			@Override
			public IMockExpectation4<Void, P1, P2, P3, P4> andCheck(final IMockExpectation4.Check<P1, P2, P3, P4> check) {
				final FutureTask<Void> result = new FutureTask<>(
						() -> check.execute(real_p1.getValue(), real_p2.getValue(), real_p3.getValue(), real_p4.getValue()), null);
				AbstractTestCase.this.steps.add(result);
				return this;
			}

			@Override
			public IMockExpectation4<Void, P1, P2, P3, P4> andReturn(final Void returnValue) {
				throw new UnsupportedOperationException();
			}
		};
	}

	public <R, TO extends T, T> IMockExpectation0<R> expect(final TO toObj, final Func0<T, R> func) {
		this.mocks.add(toObj);
		final IExpectationSetters<R> easy = EasyMock.expectLastCall();
		return new IMockExpectation0<R>() {
			@Override
			public IMockExpectation0<R> andCheck(final IMockExpectation0.Check check) {
				final FutureTask<Void> result = new FutureTask<>(() -> check.execute(), null);
				AbstractTestCase.this.steps.add(result);
				return this;
			}

			@Override
			public IMockExpectation0<R> andReturn(final R returnValue) {
				easy.andReturn(returnValue);
				return this;
			}
		};
	}

	public <R, TO extends T, T, P1> IMockExpectation1<R, P1> expect(final TO toObj, final Func1<T, R, P1> func, final P1 p1) {
		this.mocks.add(toObj);
		final Capture<P1> real_p1 = EasyMock.newCapture();
		func.apply(toObj, EasyMock.capture(real_p1));
		final IExpectationSetters<R> easy = EasyMock.expectLastCall();
		return new IMockExpectation1<R, P1>() {
			@Override
			public IMockExpectation1<R, P1> andCheck(final net.akehurst.application.framework.common.test.AbstractTestCase.IMockExpectation1.Check<P1> check) {
				final FutureTask<Void> result = new FutureTask<>(() -> check.execute(real_p1.getValue()), null);
				AbstractTestCase.this.steps.add(result);
				return this;
			}

			@Override
			public IMockExpectation1<R, P1> andReturn(final R returnValue) {
				easy.andReturn(returnValue);
				return this;
			}
		};
	}

	public <R, TO extends T, T, P1, P2> IMockExpectation2<R, P1, P2> expect(final TO toObj, final Func2<T, R, P1, P2> func, final P1 p1, final P2 p2) {
		this.mocks.add(toObj);
		final Capture<P1> real_p1 = EasyMock.newCapture();
		final Capture<P2> real_p2 = EasyMock.newCapture();
		func.apply(toObj, EasyMock.capture(real_p1), EasyMock.capture(real_p2));
		final IExpectationSetters<R> easy = EasyMock.expectLastCall();
		return new IMockExpectation2<R, P1, P2>() {
			@Override
			public IMockExpectation2<R, P1, P2> andCheck(final IMockExpectation2.Check<P1, P2> check) {
				final FutureTask<Void> result = new FutureTask<>(() -> check.execute(real_p1.getValue(), real_p2.getValue()), null);
				AbstractTestCase.this.steps.add(result);
				return this;
			}

			@Override
			public IMockExpectation2<R, P1, P2> andReturn(final R returnValue) {
				easy.andReturn(returnValue);
				return this;
			}
		};
	}

	public <R, TO extends T, T, P1, P2, P3> IMockExpectation3<R, P1, P2, P3> expect(final TO toObj, final Func3<T, R, P1, P2, P3> func, final P1 p1,
			final P2 p2, final P3 p3) {
		this.mocks.add(toObj);
		final Capture<P1> real_p1 = EasyMock.newCapture();
		final Capture<P2> real_p2 = EasyMock.newCapture();
		final Capture<P3> real_p3 = EasyMock.newCapture();
		func.apply(toObj, EasyMock.capture(real_p1), EasyMock.capture(real_p2), EasyMock.capture(real_p3));
		final IExpectationSetters<R> easy = EasyMock.expectLastCall();
		return new IMockExpectation3<R, P1, P2, P3>() {
			@Override
			public IMockExpectation3<R, P1, P2, P3> andCheck(final IMockExpectation3.Check<P1, P2, P3> check) {
				final FutureTask<Void> result = new FutureTask<>(() -> check.execute(real_p1.getValue(), real_p2.getValue(), real_p3.getValue()), null);
				AbstractTestCase.this.steps.add(result);
				return this;
			}

			@Override
			public IMockExpectation3<R, P1, P2, P3> andReturn(final R returnValue) {
				easy.andReturn(returnValue);
				return this;
			}
		};
	}

	public <R, TO extends T, T, P1, P2, P3, P4> IMockExpectation4<R, P1, P2, P3, P4> expect(final TO toObj, final Func4<T, R, P1, P2, P3, P4> func, final P1 p1,
			final P2 p2, final P3 p3, final P4 p4) {
		this.mocks.add(toObj);
		final Capture<P1> real_p1 = EasyMock.newCapture();
		final Capture<P2> real_p2 = EasyMock.newCapture();
		final Capture<P3> real_p3 = EasyMock.newCapture();
		final Capture<P4> real_p4 = EasyMock.newCapture();
		func.apply(toObj, EasyMock.capture(real_p1), EasyMock.capture(real_p2), EasyMock.capture(real_p3), EasyMock.capture(real_p4));
		final IExpectationSetters<R> easy = EasyMock.expectLastCall();
		return new IMockExpectation4<R, P1, P2, P3, P4>() {
			@Override
			public IMockExpectation4<R, P1, P2, P3, P4> andCheck(final IMockExpectation4.Check<P1, P2, P3, P4> check) {
				final FutureTask<Void> result = new FutureTask<>(
						() -> check.execute(real_p1.getValue(), real_p2.getValue(), real_p3.getValue(), real_p4.getValue()), null);
				AbstractTestCase.this.steps.add(result);
				return this;
			}

			@Override
			public IMockExpectation4<R, P1, P2, P3, P4> andReturn(final R returnValue) {
				easy.andReturn(returnValue);
				return this;
			}
		};
	}

	public void perform(final Runnable task) {
		final FutureTask<Void> result = new FutureTask<>(task, null);
		this.steps.add(result);
	}

	public <TO extends T, T, P1> void perform(final TO toObj, final Proc1<T, P1> func, final P1 p1) {
		final FutureTask<Void> result = new FutureTask<>(() -> func.call(toObj, p1), null);
		this.steps.add(result);
	}

	public <TO extends T, T, P1, P2> void perform(final TO toObj, final Proc2<T, P1, P2> func, final P1 p1, final P2 p2) {
		final FutureTask<Void> result = new FutureTask<>(() -> func.call(toObj, p1, p2), null);
		this.steps.add(result);
	}

	public <TO extends T, T, P1, P2, P3> void perform(final TO toObj, final Proc3<T, P1, P2, P3> func, final P1 p1, final P2 p2, final P3 p3) {
		final FutureTask<Void> result = new FutureTask<>(() -> func.call(toObj, p1, p2, p3), null);
		this.steps.add(result);
	}

	public <TO extends T, T, P1, P2, P3, P4> void perform(final TO toObj, final Proc4<T, P1, P2, P3, P4> func, final P1 p1, final P2 p2, final P3 p3,
			final P4 p4) {
		final FutureTask<Void> result = new FutureTask<>(() -> func.call(toObj, p1, p2, p3, p4), null);
		this.steps.add(result);
	}

	public <R, TO extends T, T, P1> Future<R> perform(final TO toObj, final Func1<T, R, P1> func, final P1 p1) {
		final FutureTask<R> result = new FutureTask<>(() -> func.apply(toObj, p1));
		this.steps.add(result);
		return result;
	}

	public <R, TO extends T, T, P1, P2> Future<R> perform(final TO toObj, final Func2<T, R, P1, P2> func, final P1 p1, final P2 p2) {
		final FutureTask<R> result = new FutureTask<>(() -> func.apply(toObj, p1, p2));
		this.steps.add(result);
		return result;
	}

	/**
	 * cause a delay in the scenario playback for the given number of milliseconds
	 *
	 * @param milliseconds
	 */
	public void delay(final int milliseconds) {
		final FutureTask<Void> result = new FutureTask<>(() -> {
			try {
				Thread.sleep(milliseconds);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}, null);
		this.steps.add(result);
	}

	/**
	 * @throws ExecutionException
	 * @throws InterruptedException
	 *
	 */
	public void play() {
		EasyMock.replay(this.mocks.toArray());
		for (final FutureTask<?> t : this.steps) {
			try {
				t.run();
				t.get();
			} catch (final ExecutionException e) {
				if (e.getCause() instanceof AssertionError) {
					throw (AssertionError) e.getCause();
				} else {
					throw new AssertionError(e);
				}
			} catch (final InterruptedException e) {
				throw new AssertionError(e);
			}
		}
	}

	/**
	 * put the current thread to sleep for the given number of milliseconds
	 *
	 * @param milliseconds
	 */
	public void sleep(final long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void verify() {
		EasyMock.verify(this.mocks.toArray());
	}
}
