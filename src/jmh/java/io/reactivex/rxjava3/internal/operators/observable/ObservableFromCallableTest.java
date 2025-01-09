/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package io.reactivex.rxjava3.internal.operators.observable;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableFromCallableTest extends RxJavaTest {

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotInvokeFuncUntilSubscription() throws Exception {
        Callable<Object> func = mock(Callable.class);
        when(func.call()).thenReturn(new Object());
        Observable<Object> fromCallableObservable = Observable.fromCallable(func);
        verifyNoInteractions(func);
        fromCallableObservable.subscribe();
        verify(func).call();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCallOnNextAndOnCompleted() throws Exception {
        Callable<String> func = mock(Callable.class);
        when(func.call()).thenReturn("test_value");
        Observable<String> fromCallableObservable = Observable.fromCallable(func);
        Observer<Object> observer = TestHelper.mockObserver();
        fromCallableObservable.subscribe(observer);
        verify(observer).onNext("test_value");
        verify(observer).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCallOnError() throws Exception {
        Callable<Object> func = mock(Callable.class);
        Throwable throwable = new IllegalStateException("Test exception");
        when(func.call()).thenThrow(throwable);
        Observable<Object> fromCallableObservable = Observable.fromCallable(func);
        Observer<Object> observer = TestHelper.mockObserver();
        fromCallableObservable.subscribe(observer);
        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
        verify(observer).onError(throwable);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission() throws Exception {
        Callable<String> func = mock(Callable.class);
        final CountDownLatch funcLatch = new CountDownLatch(1);
        final CountDownLatch observerLatch = new CountDownLatch(1);
        when(func.call()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                observerLatch.countDown();
                try {
                    funcLatch.await();
                } catch (InterruptedException e) {
                    // It's okay, unsubscription causes Thread interruption
                    // Restoring interruption status of the Thread
                    Thread.currentThread().interrupt();
                }
                return "should_not_be_delivered";
            }
        });
        Observable<String> fromCallableObservable = Observable.fromCallable(func);
        Observer<Object> observer = TestHelper.mockObserver();
        TestObserver<String> outer = new TestObserver<>(observer);
        fromCallableObservable.subscribeOn(Schedulers.computation()).subscribe(outer);
        // Wait until func will be invoked
        observerLatch.await();
        // Unsubscribing before emission
        outer.dispose();
        // Emitting result
        funcLatch.countDown();
        // func must be invoked
        verify(func).call();
        // Observer must not be notified at all
        verify(observer).onSubscribe(any(Disposable.class));
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void shouldAllowToThrowCheckedException() {
        final Exception checkedException = new Exception("test exception");
        Observable<Object> fromCallableObservable = Observable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw checkedException;
            }
        });
        Observer<Object> observer = TestHelper.mockObserver();
        fromCallableObservable.subscribe(observer);
        verify(observer).onSubscribe(any(Disposable.class));
        verify(observer).onError(checkedException);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void fusedFlatMapExecution() {
        final int[] calls = { 0 };
        Observable.just(1).flatMap(new Function<Integer, ObservableSource<? extends Object>>() {

            @Override
            public ObservableSource<? extends Object> apply(Integer v) throws Exception {
                return Observable.fromCallable(new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {
                        return ++calls[0];
                    }
                });
            }
        }).test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @Test
    public void fusedFlatMapExecutionHidden() {
        final int[] calls = { 0 };
        Observable.just(1).hide().flatMap(new Function<Integer, ObservableSource<? extends Object>>() {

            @Override
            public ObservableSource<? extends Object> apply(Integer v) throws Exception {
                return Observable.fromCallable(new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {
                        return ++calls[0];
                    }
                });
            }
        }).test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @Test
    public void fusedFlatMapNull() {
        Observable.just(1).flatMap(new Function<Integer, ObservableSource<? extends Object>>() {

            @Override
            public ObservableSource<? extends Object> apply(Integer v) throws Exception {
                return Observable.fromCallable(new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {
                        return null;
                    }
                });
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void fusedFlatMapNullHidden() {
        Observable.just(1).hide().flatMap(new Function<Integer, ObservableSource<? extends Object>>() {

            @Override
            public ObservableSource<? extends Object> apply(Integer v) throws Exception {
                return Observable.fromCallable(new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {
                        return null;
                    }
                });
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void disposedOnArrival() {
        final int[] count = { 0 };
        Observable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                count[0]++;
                return 1;
            }
        }).test(true).assertEmpty();
        assertEquals(0, count[0]);
    }

    @Test
    public void disposedOnCall() {
        final TestObserver<Integer> to = new TestObserver<>();
        Observable.fromCallable(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                to.dispose();
                return 1;
            }
        }).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void disposedOnCallThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final TestObserver<Integer> to = new TestObserver<>();
            Observable.fromCallable(new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
                    to.dispose();
                    throw new TestException();
                }
            }).subscribe(to);
            to.assertEmpty();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void take() {
        Observable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return 1;
            }
        }).take(1).test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableFromCallableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotInvokeFuncUntilSubscription() throws java.lang.Throwable {
            this.payloads.shouldNotInvokeFuncUntilSubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldCallOnNextAndOnCompleted() throws java.lang.Throwable {
            this.payloads.shouldCallOnNextAndOnCompleted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldCallOnError() throws java.lang.Throwable {
            this.payloads.shouldCallOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission() throws java.lang.Throwable {
            this.payloads.shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldAllowToThrowCheckedException() throws java.lang.Throwable {
            this.payloads.shouldAllowToThrowCheckedException.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedFlatMapExecution() throws java.lang.Throwable {
            this.payloads.fusedFlatMapExecution.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedFlatMapExecutionHidden() throws java.lang.Throwable {
            this.payloads.fusedFlatMapExecutionHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedFlatMapNull() throws java.lang.Throwable {
            this.payloads.fusedFlatMapNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedFlatMapNullHidden() throws java.lang.Throwable {
            this.payloads.fusedFlatMapNullHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedOnArrival() throws java.lang.Throwable {
            this.payloads.disposedOnArrival.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedOnCall() throws java.lang.Throwable {
            this.payloads.disposedOnCall.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedOnCallThrows() throws java.lang.Throwable {
            this.payloads.disposedOnCallThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromCallableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromCallableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromCallableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromCallableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableFromCallableTest();
                org.junit.runners.model.Statement statement = new _InstanceStatement(this.payload, this.benchmark);
                statement = this.applyRule(this.benchmark.instance.globalTimeout, statement);
                statement = this.applyRule(this.benchmark.instance.suppressUndeliverableRule, statement);
                statement.evaluate();
            }

            private org.junit.runners.model.Statement applyRule(org.junit.rules.TestRule rule, org.junit.runners.model.Statement statement) {
                return se.chalmers.ju2jmh.api.Rules.apply(rule, statement, this.description);
            }

            private org.junit.runners.model.Statement applyRule(org.junit.rules.MethodRule rule, org.junit.runners.model.Statement statement) {
                return se.chalmers.ju2jmh.api.Rules.apply(rule, statement, this.frameworkMethod, this.benchmark.instance);
            }

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromCallableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableFromCallableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableFromCallableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement shouldNotInvokeFuncUntilSubscription;

            public org.junit.runners.model.Statement shouldCallOnNextAndOnCompleted;

            public org.junit.runners.model.Statement shouldCallOnError;

            public org.junit.runners.model.Statement shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission;

            public org.junit.runners.model.Statement shouldAllowToThrowCheckedException;

            public org.junit.runners.model.Statement fusedFlatMapExecution;

            public org.junit.runners.model.Statement fusedFlatMapExecutionHidden;

            public org.junit.runners.model.Statement fusedFlatMapNull;

            public org.junit.runners.model.Statement fusedFlatMapNullHidden;

            public org.junit.runners.model.Statement disposedOnArrival;

            public org.junit.runners.model.Statement disposedOnCall;

            public org.junit.runners.model.Statement disposedOnCallThrows;

            public org.junit.runners.model.Statement take;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.shouldNotInvokeFuncUntilSubscription = _ClassStatement.forPayload(ObservableFromCallableTest::shouldNotInvokeFuncUntilSubscription, "shouldNotInvokeFuncUntilSubscription", this);
            this.payloads.shouldCallOnNextAndOnCompleted = _ClassStatement.forPayload(ObservableFromCallableTest::shouldCallOnNextAndOnCompleted, "shouldCallOnNextAndOnCompleted", this);
            this.payloads.shouldCallOnError = _ClassStatement.forPayload(ObservableFromCallableTest::shouldCallOnError, "shouldCallOnError", this);
            this.payloads.shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission = _ClassStatement.forPayload(ObservableFromCallableTest::shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission, "shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission", this);
            this.payloads.shouldAllowToThrowCheckedException = _ClassStatement.forPayload(ObservableFromCallableTest::shouldAllowToThrowCheckedException, "shouldAllowToThrowCheckedException", this);
            this.payloads.fusedFlatMapExecution = _ClassStatement.forPayload(ObservableFromCallableTest::fusedFlatMapExecution, "fusedFlatMapExecution", this);
            this.payloads.fusedFlatMapExecutionHidden = _ClassStatement.forPayload(ObservableFromCallableTest::fusedFlatMapExecutionHidden, "fusedFlatMapExecutionHidden", this);
            this.payloads.fusedFlatMapNull = _ClassStatement.forPayload(ObservableFromCallableTest::fusedFlatMapNull, "fusedFlatMapNull", this);
            this.payloads.fusedFlatMapNullHidden = _ClassStatement.forPayload(ObservableFromCallableTest::fusedFlatMapNullHidden, "fusedFlatMapNullHidden", this);
            this.payloads.disposedOnArrival = _ClassStatement.forPayload(ObservableFromCallableTest::disposedOnArrival, "disposedOnArrival", this);
            this.payloads.disposedOnCall = _ClassStatement.forPayload(ObservableFromCallableTest::disposedOnCall, "disposedOnCall", this);
            this.payloads.disposedOnCallThrows = _ClassStatement.forPayload(ObservableFromCallableTest::disposedOnCallThrows, "disposedOnCallThrows", this);
            this.payloads.take = _ClassStatement.forPayload(ObservableFromCallableTest::take, "take", this);
        }
    }
}
