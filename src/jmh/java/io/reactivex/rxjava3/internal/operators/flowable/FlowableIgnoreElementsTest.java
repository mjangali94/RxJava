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

import static org.junit.Assert.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.QueueSubscription;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableIgnoreElementsTest extends RxJavaTest {

    @Test
    public void withEmptyFlowable() {
        assertTrue(Flowable.empty().ignoreElements().toFlowable().isEmpty().blockingGet());
    }

    @Test
    public void withNonEmptyFlowable() {
        assertTrue(Flowable.just(1, 2, 3).ignoreElements().toFlowable().isEmpty().blockingGet());
    }

    @Test
    public void upstreamIsProcessedButIgnoredFlowable() {
        final int num = 10;
        final AtomicInteger upstreamCount = new AtomicInteger();
        long count = Flowable.range(1, num).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).ignoreElements().toFlowable().count().blockingGet();
        assertEquals(num, upstreamCount.get());
        assertEquals(0, count);
    }

    @Test
    public void completedOkFlowable() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        Flowable.range(1, 10).ignoreElements().toFlowable().subscribe(ts);
        ts.assertNoErrors();
        ts.assertNoValues();
        ts.assertTerminated();
    }

    @Test
    public void errorReceivedFlowable() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        TestException ex = new TestException("boo");
        Flowable.error(ex).ignoreElements().toFlowable().subscribe(ts);
        ts.assertNoValues();
        ts.assertTerminated();
        ts.assertError(TestException.class);
        ts.assertErrorMessage("boo");
    }

    @Test
    public void unsubscribesFromUpstreamFlowable() {
        final AtomicBoolean unsub = new AtomicBoolean();
        Flowable.range(1, 10).concatWith(Flowable.<Integer>never()).doOnCancel(new Action() {

            @Override
            public void run() {
                unsub.set(true);
            }
        }).ignoreElements().toFlowable().subscribe().dispose();
        assertTrue(unsub.get());
    }

    @Test
    public void doesNotHangAndProcessesAllUsingBackpressureFlowable() {
        final AtomicInteger upstreamCount = new AtomicInteger();
        final AtomicInteger count = new AtomicInteger(0);
        int num = 10;
        Flowable.range(1, num).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).ignoreElements().<Integer>toFlowable().doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
                count.incrementAndGet();
            }
        });
        assertEquals(num, upstreamCount.get());
        assertEquals(0, count.get());
    }

    @Test
    public void withEmpty() {
        Flowable.empty().ignoreElements().blockingAwait();
    }

    @Test
    public void withNonEmpty() {
        Flowable.just(1, 2, 3).ignoreElements().blockingAwait();
    }

    @Test
    public void upstreamIsProcessedButIgnored() {
        final int num = 10;
        final AtomicInteger upstreamCount = new AtomicInteger();
        Flowable.range(1, num).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).ignoreElements().blockingAwait();
        assertEquals(num, upstreamCount.get());
    }

    @Test
    public void completedOk() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        Flowable.range(1, 10).ignoreElements().subscribe(to);
        to.assertNoErrors();
        to.assertNoValues();
        to.assertTerminated();
    }

    @Test
    public void errorReceived() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        TestException ex = new TestException("boo");
        Flowable.error(ex).ignoreElements().subscribe(to);
        to.assertNoValues();
        to.assertTerminated();
        to.assertError(TestException.class);
        to.assertErrorMessage("boo");
    }

    @Test
    public void unsubscribesFromUpstream() {
        final AtomicBoolean unsub = new AtomicBoolean();
        Flowable.range(1, 10).concatWith(Flowable.<Integer>never()).doOnCancel(new Action() {

            @Override
            public void run() {
                unsub.set(true);
            }
        }).ignoreElements().subscribe().dispose();
        assertTrue(unsub.get());
    }

    @Test
    public void doesNotHangAndProcessesAllUsingBackpressure() {
        final AtomicInteger upstreamCount = new AtomicInteger();
        final AtomicInteger count = new AtomicInteger(0);
        int num = 10;
        Flowable.range(1, num).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).ignoreElements().subscribe(new DisposableCompletableObserver() {

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }
        });
        assertEquals(num, upstreamCount.get());
        assertEquals(0, count.get());
    }

    @Test
    public void cancel() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.ignoreElements().<Integer>toFlowable().test();
        assertTrue(pp.hasSubscribers());
        ts.cancel();
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void fused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.just(1).hide().ignoreElements().<Integer>toFlowable().subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void fusedAPICalls() {
        Flowable.just(1).hide().ignoreElements().<Integer>toFlowable().subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                @SuppressWarnings("unchecked")
                QueueSubscription<Integer> qs = (QueueSubscription<Integer>) s;
                try {
                    assertNull(qs.poll());
                } catch (Throwable ex) {
                    throw new AssertionError(ex);
                }
                assertTrue(qs.isEmpty());
                qs.clear();
                assertTrue(qs.isEmpty());
                try {
                    assertNull(qs.poll());
                } catch (Throwable ex) {
                    throw new AssertionError(ex);
                }
                try {
                    qs.offer(1);
                    fail("Should have thrown!");
                } catch (UnsupportedOperationException ex) {
                // expected
                }
                try {
                    qs.offer(1, 2);
                    fail("Should have thrown!");
                } catch (UnsupportedOperationException ex) {
                // expected
                }
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).ignoreElements());
        TestHelper.checkDisposed(Flowable.just(1).ignoreElements().toFlowable());
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.ignoreElements().toFlowable();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToCompletable(new Function<Flowable<Object>, Completable>() {

            @Override
            public Completable apply(Flowable<Object> f) throws Exception {
                return f.ignoreElements();
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableIgnoreElementsTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmptyFlowable() throws java.lang.Throwable {
            this.payloads.withEmptyFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withNonEmptyFlowable() throws java.lang.Throwable {
            this.payloads.withNonEmptyFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamIsProcessedButIgnoredFlowable() throws java.lang.Throwable {
            this.payloads.upstreamIsProcessedButIgnoredFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completedOkFlowable() throws java.lang.Throwable {
            this.payloads.completedOkFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorReceivedFlowable() throws java.lang.Throwable {
            this.payloads.errorReceivedFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribesFromUpstreamFlowable() throws java.lang.Throwable {
            this.payloads.unsubscribesFromUpstreamFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesNotHangAndProcessesAllUsingBackpressureFlowable() throws java.lang.Throwable {
            this.payloads.doesNotHangAndProcessesAllUsingBackpressureFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty() throws java.lang.Throwable {
            this.payloads.withEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withNonEmpty() throws java.lang.Throwable {
            this.payloads.withNonEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamIsProcessedButIgnored() throws java.lang.Throwable {
            this.payloads.upstreamIsProcessedButIgnored.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completedOk() throws java.lang.Throwable {
            this.payloads.completedOk.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorReceived() throws java.lang.Throwable {
            this.payloads.errorReceived.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribesFromUpstream() throws java.lang.Throwable {
            this.payloads.unsubscribesFromUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesNotHangAndProcessesAllUsingBackpressure() throws java.lang.Throwable {
            this.payloads.doesNotHangAndProcessesAllUsingBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.payloads.fused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedAPICalls() throws java.lang.Throwable {
            this.payloads.fusedAPICalls.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableIgnoreElementsTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableIgnoreElementsTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableIgnoreElementsTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableIgnoreElementsTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableIgnoreElementsTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableIgnoreElementsTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableIgnoreElementsTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableIgnoreElementsTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement withEmptyFlowable;

            public org.junit.runners.model.Statement withNonEmptyFlowable;

            public org.junit.runners.model.Statement upstreamIsProcessedButIgnoredFlowable;

            public org.junit.runners.model.Statement completedOkFlowable;

            public org.junit.runners.model.Statement errorReceivedFlowable;

            public org.junit.runners.model.Statement unsubscribesFromUpstreamFlowable;

            public org.junit.runners.model.Statement doesNotHangAndProcessesAllUsingBackpressureFlowable;

            public org.junit.runners.model.Statement withEmpty;

            public org.junit.runners.model.Statement withNonEmpty;

            public org.junit.runners.model.Statement upstreamIsProcessedButIgnored;

            public org.junit.runners.model.Statement completedOk;

            public org.junit.runners.model.Statement errorReceived;

            public org.junit.runners.model.Statement unsubscribesFromUpstream;

            public org.junit.runners.model.Statement doesNotHangAndProcessesAllUsingBackpressure;

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement fused;

            public org.junit.runners.model.Statement fusedAPICalls;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.withEmptyFlowable = _ClassStatement.forPayload(FlowableIgnoreElementsTest::withEmptyFlowable, "withEmptyFlowable", this);
            this.payloads.withNonEmptyFlowable = _ClassStatement.forPayload(FlowableIgnoreElementsTest::withNonEmptyFlowable, "withNonEmptyFlowable", this);
            this.payloads.upstreamIsProcessedButIgnoredFlowable = _ClassStatement.forPayload(FlowableIgnoreElementsTest::upstreamIsProcessedButIgnoredFlowable, "upstreamIsProcessedButIgnoredFlowable", this);
            this.payloads.completedOkFlowable = _ClassStatement.forPayload(FlowableIgnoreElementsTest::completedOkFlowable, "completedOkFlowable", this);
            this.payloads.errorReceivedFlowable = _ClassStatement.forPayload(FlowableIgnoreElementsTest::errorReceivedFlowable, "errorReceivedFlowable", this);
            this.payloads.unsubscribesFromUpstreamFlowable = _ClassStatement.forPayload(FlowableIgnoreElementsTest::unsubscribesFromUpstreamFlowable, "unsubscribesFromUpstreamFlowable", this);
            this.payloads.doesNotHangAndProcessesAllUsingBackpressureFlowable = _ClassStatement.forPayload(FlowableIgnoreElementsTest::doesNotHangAndProcessesAllUsingBackpressureFlowable, "doesNotHangAndProcessesAllUsingBackpressureFlowable", this);
            this.payloads.withEmpty = _ClassStatement.forPayload(FlowableIgnoreElementsTest::withEmpty, "withEmpty", this);
            this.payloads.withNonEmpty = _ClassStatement.forPayload(FlowableIgnoreElementsTest::withNonEmpty, "withNonEmpty", this);
            this.payloads.upstreamIsProcessedButIgnored = _ClassStatement.forPayload(FlowableIgnoreElementsTest::upstreamIsProcessedButIgnored, "upstreamIsProcessedButIgnored", this);
            this.payloads.completedOk = _ClassStatement.forPayload(FlowableIgnoreElementsTest::completedOk, "completedOk", this);
            this.payloads.errorReceived = _ClassStatement.forPayload(FlowableIgnoreElementsTest::errorReceived, "errorReceived", this);
            this.payloads.unsubscribesFromUpstream = _ClassStatement.forPayload(FlowableIgnoreElementsTest::unsubscribesFromUpstream, "unsubscribesFromUpstream", this);
            this.payloads.doesNotHangAndProcessesAllUsingBackpressure = _ClassStatement.forPayload(FlowableIgnoreElementsTest::doesNotHangAndProcessesAllUsingBackpressure, "doesNotHangAndProcessesAllUsingBackpressure", this);
            this.payloads.cancel = _ClassStatement.forPayload(FlowableIgnoreElementsTest::cancel, "cancel", this);
            this.payloads.fused = _ClassStatement.forPayload(FlowableIgnoreElementsTest::fused, "fused", this);
            this.payloads.fusedAPICalls = _ClassStatement.forPayload(FlowableIgnoreElementsTest::fusedAPICalls, "fusedAPICalls", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableIgnoreElementsTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableIgnoreElementsTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
