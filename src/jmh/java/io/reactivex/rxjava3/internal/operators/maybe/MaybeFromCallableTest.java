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
package io.reactivex.rxjava3.internal.operators.maybe;

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

public class MaybeFromCallableTest extends RxJavaTest {

    @Test
    public void fromCallable() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Maybe.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                atomicInteger.incrementAndGet();
                return null;
            }
        }).test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromCallableTwice() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Callable<Object> callable = new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                atomicInteger.incrementAndGet();
                return null;
            }
        };
        Maybe.fromCallable(callable).test().assertResult();
        assertEquals(1, atomicInteger.get());
        Maybe.fromCallable(callable).test().assertResult();
        assertEquals(2, atomicInteger.get());
    }

    @Test
    public void fromCallableInvokesLazy() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Maybe<Object> completable = Maybe.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                atomicInteger.incrementAndGet();
                return null;
            }
        });
        assertEquals(0, atomicInteger.get());
        completable.test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromCallableThrows() {
        Maybe.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw new UnsupportedOperationException();
            }
        }).test().assertFailure(UnsupportedOperationException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void callable() throws Throwable {
        final int[] counter = { 0 };
        Maybe<Integer> m = Maybe.fromCallable(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                counter[0]++;
                return 0;
            }
        });
        assertTrue(m.getClass().toString(), m instanceof Supplier);
        assertEquals(0, ((Supplier<Void>) m).get());
        assertEquals(1, counter[0]);
    }

    @Test
    public void noErrorLoss() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final CountDownLatch cdl1 = new CountDownLatch(1);
            final CountDownLatch cdl2 = new CountDownLatch(1);
            TestObserver<Integer> to = Maybe.fromCallable(new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
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
        Maybe<String> fromCallableObservable = Maybe.fromCallable(func);
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
    public void disposeUpfront() {
        Maybe.fromCallable(() -> 1).test(true).assertEmpty();
    }

    @Test
    public void success() {
        Maybe.fromCallable(() -> 1).test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeFromCallableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallable() throws java.lang.Throwable {
            this.payloads.fromCallable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallableTwice() throws java.lang.Throwable {
            this.payloads.fromCallableTwice.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallableInvokesLazy() throws java.lang.Throwable {
            this.payloads.fromCallableInvokesLazy.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallableThrows() throws java.lang.Throwable {
            this.payloads.fromCallableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_callable() throws java.lang.Throwable {
            this.payloads.callable.evaluate();
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
        public void benchmark_disposeUpfront() throws java.lang.Throwable {
            this.payloads.disposeUpfront.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_success() throws java.lang.Throwable {
            this.payloads.success.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFromCallableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFromCallableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFromCallableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFromCallableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeFromCallableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFromCallableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeFromCallableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeFromCallableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement fromCallable;

            public org.junit.runners.model.Statement fromCallableTwice;

            public org.junit.runners.model.Statement fromCallableInvokesLazy;

            public org.junit.runners.model.Statement fromCallableThrows;

            public org.junit.runners.model.Statement callable;

            public org.junit.runners.model.Statement noErrorLoss;

            public org.junit.runners.model.Statement shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission;

            public org.junit.runners.model.Statement disposeUpfront;

            public org.junit.runners.model.Statement success;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.fromCallable = _ClassStatement.forPayload(MaybeFromCallableTest::fromCallable, "fromCallable", this);
            this.payloads.fromCallableTwice = _ClassStatement.forPayload(MaybeFromCallableTest::fromCallableTwice, "fromCallableTwice", this);
            this.payloads.fromCallableInvokesLazy = _ClassStatement.forPayload(MaybeFromCallableTest::fromCallableInvokesLazy, "fromCallableInvokesLazy", this);
            this.payloads.fromCallableThrows = _ClassStatement.forPayload(MaybeFromCallableTest::fromCallableThrows, "fromCallableThrows", this);
            this.payloads.callable = _ClassStatement.forPayload(MaybeFromCallableTest::callable, "callable", this);
            this.payloads.noErrorLoss = _ClassStatement.forPayload(MaybeFromCallableTest::noErrorLoss, "noErrorLoss", this);
            this.payloads.shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission = _ClassStatement.forPayload(MaybeFromCallableTest::shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission, "shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission", this);
            this.payloads.disposeUpfront = _ClassStatement.forPayload(MaybeFromCallableTest::disposeUpfront, "disposeUpfront", this);
            this.payloads.success = _ClassStatement.forPayload(MaybeFromCallableTest::success, "success", this);
        }
    }
}
