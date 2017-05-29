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
	public void play() throws InterruptedException, ExecutionException {
		EasyMock.replay(this.mocks.toArray());
		for (final FutureTask<?> t : this.steps) {
			t.run();
			t.get();
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
