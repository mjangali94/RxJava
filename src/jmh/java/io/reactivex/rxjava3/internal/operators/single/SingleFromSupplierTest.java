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
package io.reactivex.rxjava3.internal.operators.single;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleFromSupplierTest extends RxJavaTest {

    @Test
    public void fromSupplierValue() {
        Single.fromSupplier(new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                return 5;
            }
        }).test().assertResult(5);
    }

    @Test
    public void fromSupplierError() {
        Single.fromSupplier(new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                throw new UnsupportedOperationException();
            }
        }).test().assertFailure(UnsupportedOperationException.class);
    }

    @Test
    public void fromSupplierNull() {
        Single.fromSupplier(new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                return null;
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(NullPointerException.class, "The supplier returned a null value");
    }

    @Test
    public void fromSupplierTwice() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Supplier<Integer> supplier = new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                return atomicInteger.incrementAndGet();
            }
        };
        Single.fromSupplier(supplier).test().assertResult(1);
        assertEquals(1, atomicInteger.get());
        Single.fromSupplier(supplier).test().assertResult(2);
        assertEquals(2, atomicInteger.get());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotInvokeFuncUntilSubscription() throws Throwable {
        Supplier<Object> func = mock(Supplier.class);
        when(func.get()).thenReturn(new Object());
        Single<Object> fromSupplierSingle = Single.fromSupplier(func);
        verifyNoInteractions(func);
        fromSupplierSingle.subscribe();
        verify(func).get();
    }

    @Test
    public void noErrorLoss() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final CountDownLatch cdl1 = new CountDownLatch(1);
            final CountDownLatch cdl2 = new CountDownLatch(1);
            TestObserver<Integer> to = Single.fromSupplier(new Supplier<Integer>() {

                @Override
                public Integer get() throws Exception {
                    cdl1.countDown();
                    cdl2.await(5, TimeUnit.SECONDS);
                    return 1;
                }
            }).subscribeOn(Schedulers.single()).test();
            assertTrue(cdl1.await(5, TimeUnit.SECONDS));
            to.dispose();
            int timeout = 10;
            while (timeout-- > 0 && errors.isEmpty()) {
                Thread.sleep(100);
            }
            TestHelper.assertUndeliverable(errors, 0, InterruptedException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission() throws Throwable {
        Supplier<String> func = mock(Supplier.class);
        final CountDownLatch funcLatch = new CountDownLatch(1);
        final CountDownLatch observerLatch = new CountDownLatch(1);
        when(func.get()).thenAnswer(new Answer<String>() {

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
        Single<String> fromSupplierObservable = Single.fromSupplier(func);
        Observer<Object> observer = TestHelper.mockObserver();
        TestObserver<String> outer = new TestObserver<>(observer);
        fromSupplierObservable.subscribeOn(Schedulers.computation()).subscribe(outer);
        // Wait until func will be invoked
        observerLatch.await();
        // Unsubscribing before emission
        outer.dispose();
        // Emitting result
        funcLatch.countDown();
        // func must be invoked
        verify(func).get();
        // Observer must not be notified at all
        verify(observer).onSubscribe(any(Disposable.class));
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void shouldAllowToThrowCheckedException() {
        final Exception checkedException = new Exception("test exception");
        Single<Object> fromSupplierObservable = Single.fromSupplier(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                throw checkedException;
            }
        });
        SingleObserver<Object> observer = TestHelper.mockSingleObserver();
        fromSupplierObservable.subscribe(observer);
        verify(observer).onSubscribe(any(Disposable.class));
        verify(observer).onError(checkedException);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void disposedOnArrival() {
        final int[] count = { 0 };
        Single.fromSupplier(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                count[0]++;
                return 1;
            }
        }).test(true).assertEmpty();
        assertEquals(0, count[0]);
    }

    @Test
    public void disposedOnCall() {
        final TestObserver<Integer> to = new TestObserver<>();
        Single.fromSupplier(new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                to.dispose();
                return 1;
            }
        }).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void toObservableTake() {
        Single.fromSupplier(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }).toObservable().take(1).test().assertResult(1);
    }

    @Test
    public void toObservableAndBack() {
        Single.fromSupplier(new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                return 1;
            }
        }).toObservable().singleOrError().test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public SingleFromSupplierTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromSupplierValue() throws java.lang.Throwable {
            this.payloads.fromSupplierValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromSupplierError() throws java.lang.Throwable {
            this.payloads.fromSupplierError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromSupplierNull() throws java.lang.Throwable {
            this.payloads.fromSupplierNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromSupplierTwice() throws java.lang.Throwable {
            this.payloads.fromSupplierTwice.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotInvokeFuncUntilSubscription() throws java.lang.Throwable {
            this.payloads.shouldNotInvokeFuncUntilSubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noErrorLoss() throws java.lang.Throwable {
            this.payloads.noErrorLoss.evaluate();
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
        public void benchmark_disposedOnArrival() throws java.lang.Throwable {
            this.payloads.disposedOnArrival.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedOnCall() throws java.lang.Throwable {
            this.payloads.disposedOnCall.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservableTake() throws java.lang.Throwable {
            this.payloads.toObservableTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservableAndBack() throws java.lang.Throwable {
            this.payloads.toObservableAndBack.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFromSupplierTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFromSupplierTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFromSupplierTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFromSupplierTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleFromSupplierTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFromSupplierTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleFromSupplierTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleFromSupplierTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement fromSupplierValue;

            public org.junit.runners.model.Statement fromSupplierError;

            public org.junit.runners.model.Statement fromSupplierNull;

            public org.junit.runners.model.Statement fromSupplierTwice;

            public org.junit.runners.model.Statement shouldNotInvokeFuncUntilSubscription;

            public org.junit.runners.model.Statement noErrorLoss;

            public org.junit.runners.model.Statement shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission;

            public org.junit.runners.model.Statement shouldAllowToThrowCheckedException;

            public org.junit.runners.model.Statement disposedOnArrival;

            public org.junit.runners.model.Statement disposedOnCall;

            public org.junit.runners.model.Statement toObservableTake;

            public org.junit.runners.model.Statement toObservableAndBack;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.fromSupplierValue = _ClassStatement.forPayload(SingleFromSupplierTest::fromSupplierValue, "fromSupplierValue", this);
            this.payloads.fromSupplierError = _ClassStatement.forPayload(SingleFromSupplierTest::fromSupplierError, "fromSupplierError", this);
            this.payloads.fromSupplierNull = _ClassStatement.forPayload(SingleFromSupplierTest::fromSupplierNull, "fromSupplierNull", this);
            this.payloads.fromSupplierTwice = _ClassStatement.forPayload(SingleFromSupplierTest::fromSupplierTwice, "fromSupplierTwice", this);
            this.payloads.shouldNotInvokeFuncUntilSubscription = _ClassStatement.forPayload(SingleFromSupplierTest::shouldNotInvokeFuncUntilSubscription, "shouldNotInvokeFuncUntilSubscription", this);
            this.payloads.noErrorLoss = _ClassStatement.forPayload(SingleFromSupplierTest::noErrorLoss, "noErrorLoss", this);
            this.payloads.shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission = _ClassStatement.forPayload(SingleFromSupplierTest::shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission, "shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission", this);
            this.payloads.shouldAllowToThrowCheckedException = _ClassStatement.forPayload(SingleFromSupplierTest::shouldAllowToThrowCheckedException, "shouldAllowToThrowCheckedException", this);
            this.payloads.disposedOnArrival = _ClassStatement.forPayload(SingleFromSupplierTest::disposedOnArrival, "disposedOnArrival", this);
            this.payloads.disposedOnCall = _ClassStatement.forPayload(SingleFromSupplierTest::disposedOnCall, "disposedOnCall", this);
            this.payloads.toObservableTake = _ClassStatement.forPayload(SingleFromSupplierTest::toObservableTake, "toObservableTake", this);
            this.payloads.toObservableAndBack = _ClassStatement.forPayload(SingleFromSupplierTest::toObservableAndBack, "toObservableAndBack", this);
        }
    }
}
