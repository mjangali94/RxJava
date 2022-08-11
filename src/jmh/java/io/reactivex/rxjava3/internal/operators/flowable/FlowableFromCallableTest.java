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
package io.reactivex.rxjava3.internal.operators.flowable;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableFromCallableTest extends RxJavaTest {

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotInvokeFuncUntilSubscription() throws Exception {
        Callable<Object> func = mock(Callable.class);
        when(func.call()).thenReturn(new Object());
        Flowable<Object> fromCallableFlowable = Flowable.fromCallable(func);
        verifyNoInteractions(func);
        fromCallableFlowable.subscribe();
        verify(func).call();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCallOnNextAndOnCompleted() throws Exception {
        Callable<String> func = mock(Callable.class);
        when(func.call()).thenReturn("test_value");
        Flowable<String> fromCallableFlowable = Flowable.fromCallable(func);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        fromCallableFlowable.subscribe(subscriber);
        verify(subscriber).onNext("test_value");
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCallOnError() throws Exception {
        Callable<Object> func = mock(Callable.class);
        Throwable throwable = new IllegalStateException("Test exception");
        when(func.call()).thenThrow(throwable);
        Flowable<Object> fromCallableFlowable = Flowable.fromCallable(func);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        fromCallableFlowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
        verify(subscriber).onError(throwable);
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
        Flowable<String> fromCallableFlowable = Flowable.fromCallable(func);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<String> outer = new TestSubscriber<>(subscriber);
        fromCallableFlowable.subscribeOn(Schedulers.computation()).subscribe(outer);
        // Wait until func will be invoked
        observerLatch.await();
        // Unsubscribing before emission
        outer.cancel();
        // Emitting result
        funcLatch.countDown();
        // func must be invoked
        verify(func).call();
        // Observer must not be notified at all
        verify(subscriber).onSubscribe(any(Subscription.class));
        verifyNoMoreInteractions(subscriber);
    }

    @Test
    public void shouldAllowToThrowCheckedException() {
        final Exception checkedException = new Exception("test exception");
        Flowable<Object> fromCallableFlowable = Flowable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw checkedException;
            }
        });
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        fromCallableFlowable.subscribe(subscriber);
        verify(subscriber).onSubscribe(any(Subscription.class));
        verify(subscriber).onError(checkedException);
        verifyNoMoreInteractions(subscriber);
    }

    @Test
    public void fusedFlatMapExecution() {
        final int[] calls = { 0 };
        Flowable.just(1).flatMap(new Function<Integer, Publisher<? extends Object>>() {

            @Override
            public Publisher<? extends Object> apply(Integer v) throws Exception {
                return Flowable.fromCallable(new Callable<Object>() {

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
        Flowable.just(1).hide().flatMap(new Function<Integer, Publisher<? extends Object>>() {

            @Override
            public Publisher<? extends Object> apply(Integer v) throws Exception {
                return Flowable.fromCallable(new Callable<Object>() {

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
        Flowable.just(1).flatMap(new Function<Integer, Publisher<? extends Object>>() {

            @Override
            public Publisher<? extends Object> apply(Integer v) throws Exception {
                return Flowable.fromCallable(new Callable<Object>() {

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
        Flowable.just(1).hide().flatMap(new Function<Integer, Publisher<? extends Object>>() {

            @Override
            public Publisher<? extends Object> apply(Integer v) throws Exception {
                return Flowable.fromCallable(new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {
                        return null;
                    }
                });
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void undeliverableUponCancellation() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final TestSubscriber<Integer> ts = new TestSubscriber<>();
            Flowable.fromCallable(new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
                    ts.cancel();
                    throw new TestException();
                }
            }).subscribe(ts);
            ts.assertEmpty();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableFromCallableTest instance;

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
        public void benchmark_undeliverableUponCancellation() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancellation.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromCallableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromCallableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromCallableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromCallableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableFromCallableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromCallableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableFromCallableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableFromCallableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement shouldNotInvokeFuncUntilSubscription;

            public org.junit.runners.model.Statement shouldCallOnNextAndOnCompleted;

            public org.junit.runners.model.Statement shouldCallOnError;

            public org.junit.runners.model.Statement shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission;

            public org.junit.runners.model.Statement shouldAllowToThrowCheckedException;

            public org.junit.runners.model.Statement fusedFlatMapExecution;

            public org.junit.runners.model.Statement fusedFlatMapExecutionHidden;

            public org.junit.runners.model.Statement fusedFlatMapNull;

            public org.junit.runners.model.Statement fusedFlatMapNullHidden;

            public org.junit.runners.model.Statement undeliverableUponCancellation;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.shouldNotInvokeFuncUntilSubscription = _ClassStatement.forPayload(FlowableFromCallableTest::shouldNotInvokeFuncUntilSubscription, "shouldNotInvokeFuncUntilSubscription", this);
            this.payloads.shouldCallOnNextAndOnCompleted = _ClassStatement.forPayload(FlowableFromCallableTest::shouldCallOnNextAndOnCompleted, "shouldCallOnNextAndOnCompleted", this);
            this.payloads.shouldCallOnError = _ClassStatement.forPayload(FlowableFromCallableTest::shouldCallOnError, "shouldCallOnError", this);
            this.payloads.shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission = _ClassStatement.forPayload(FlowableFromCallableTest::shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission, "shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission", this);
            this.payloads.shouldAllowToThrowCheckedException = _ClassStatement.forPayload(FlowableFromCallableTest::shouldAllowToThrowCheckedException, "shouldAllowToThrowCheckedException", this);
            this.payloads.fusedFlatMapExecution = _ClassStatement.forPayload(FlowableFromCallableTest::fusedFlatMapExecution, "fusedFlatMapExecution", this);
            this.payloads.fusedFlatMapExecutionHidden = _ClassStatement.forPayload(FlowableFromCallableTest::fusedFlatMapExecutionHidden, "fusedFlatMapExecutionHidden", this);
            this.payloads.fusedFlatMapNull = _ClassStatement.forPayload(FlowableFromCallableTest::fusedFlatMapNull, "fusedFlatMapNull", this);
            this.payloads.fusedFlatMapNullHidden = _ClassStatement.forPayload(FlowableFromCallableTest::fusedFlatMapNullHidden, "fusedFlatMapNullHidden", this);
            this.payloads.undeliverableUponCancellation = _ClassStatement.forPayload(FlowableFromCallableTest::undeliverableUponCancellation, "undeliverableUponCancellation", this);
        }
    }
}
