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
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableOnBackpressureDropTest extends RxJavaTest {

    @Test
    public void noBackpressureSupport() {
        TestSubscriber<Long> ts = new TestSubscriber<>(0L);
        // this will be ignored
        ts.request(100);
        // we take 500 so it unsubscribes
        infinite.take(500).subscribe(ts);
        // it completely ignores the `request(100)` and we get 500
        assertEquals(500, ts.values().size());
        ts.assertNoErrors();
    }

    @Test
    public void withObserveOn() throws InterruptedException {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(0, Flowable.bufferSize() * 10).onBackpressureDrop().observeOn(Schedulers.io()).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
    }

    @Test
    public void fixBackpressureWithBuffer() throws InterruptedException {
        final CountDownLatch l1 = new CountDownLatch(100);
        final CountDownLatch l2 = new CountDownLatch(150);
        TestSubscriber<Long> ts = new TestSubscriber<>(new DefaultSubscriber<Long>() {

            @Override
            protected void onStart() {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Long t) {
                l1.countDown();
                l2.countDown();
            }
        }, 0L);
        // this will be ignored
        ts.request(100);
        // we take 500 so it unsubscribes
        infinite.subscribeOn(Schedulers.computation()).onBackpressureDrop().take(500).subscribe(ts);
        // it completely ignores the `request(100)` and we get 500
        l1.await();
        assertEquals(100, ts.values().size());
        ts.request(50);
        l2.await();
        assertEquals(150, ts.values().size());
        ts.request(350);
        ts.awaitDone(5, TimeUnit.SECONDS);
        assertEquals(500, ts.values().size());
        ts.assertNoErrors();
        assertEquals(0, ts.values().get(0).intValue());
    }

    @Test
    public void requestOverflow() throws InterruptedException {
        final AtomicInteger count = new AtomicInteger();
        int n = 10;
        range(n).onBackpressureDrop().subscribe(new DefaultSubscriber<Long>() {

            @Override
            public void onStart() {
                request(10);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
                throw new RuntimeException(e);
            }

            @Override
            public void onNext(Long t) {
                count.incrementAndGet();
                // cause overflow of requested if not handled properly in onBackpressureDrop operator
                request(Long.MAX_VALUE - 1);
            }
        });
        assertEquals(n, count.get());
    }

    static final Flowable<Long> infinite = Flowable.unsafeCreate(new Publisher<Long>() {

        @Override
        public void subscribe(Subscriber<? super Long> s) {
            BooleanSubscription bs = new BooleanSubscription();
            s.onSubscribe(bs);
            long i = 0;
            while (!bs.isCancelled()) {
                s.onNext(i++);
            }
        }
    });

    private static Flowable<Long> range(final long n) {
        return Flowable.unsafeCreate(new Publisher<Long>() {

            @Override
            public void subscribe(Subscriber<? super Long> s) {
                BooleanSubscription bs = new BooleanSubscription();
                s.onSubscribe(bs);
                for (long i = 0; i < n; i++) {
                    if (bs.isCancelled()) {
                        break;
                    }
                    s.onNext(i);
                }
                s.onComplete();
            }
        });
    }

    private static final Consumer<Long> THROW_NON_FATAL = new Consumer<Long>() {

        @Override
        public void accept(Long n) {
            throw new RuntimeException();
        }
    };

    @Test
    public void nonFatalExceptionFromOverflowActionIsNotReportedFromUpstreamOperator() {
        final AtomicBoolean errorOccurred = new AtomicBoolean(false);
        // request 0
        TestSubscriber<Long> ts = TestSubscriber.create(0);
        // range method emits regardless of requests so should trigger onBackpressureDrop action
        range(2).doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable t) {
                errorOccurred.set(true);
            }
        }).onBackpressureDrop(THROW_NON_FATAL).subscribe(ts);
        assertFalse(errorOccurred.get());
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.onBackpressureDrop();
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.onBackpressureDrop();
            }
        });
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.just(1).onBackpressureDrop());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableOnBackpressureDropTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noBackpressureSupport() throws java.lang.Throwable {
            this.payloads.noBackpressureSupport.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withObserveOn() throws java.lang.Throwable {
            this.payloads.withObserveOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fixBackpressureWithBuffer() throws java.lang.Throwable {
            this.payloads.fixBackpressureWithBuffer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestOverflow() throws java.lang.Throwable {
            this.payloads.requestOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonFatalExceptionFromOverflowActionIsNotReportedFromUpstreamOperator() throws java.lang.Throwable {
            this.payloads.nonFatalExceptionFromOverflowActionIsNotReportedFromUpstreamOperator.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureDropTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureDropTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureDropTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureDropTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableOnBackpressureDropTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureDropTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableOnBackpressureDropTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableOnBackpressureDropTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement noBackpressureSupport;

            public org.junit.runners.model.Statement withObserveOn;

            public org.junit.runners.model.Statement fixBackpressureWithBuffer;

            public org.junit.runners.model.Statement requestOverflow;

            public org.junit.runners.model.Statement nonFatalExceptionFromOverflowActionIsNotReportedFromUpstreamOperator;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badRequest;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.noBackpressureSupport = _ClassStatement.forPayload(FlowableOnBackpressureDropTest::noBackpressureSupport, "noBackpressureSupport", this);
            this.payloads.withObserveOn = _ClassStatement.forPayload(FlowableOnBackpressureDropTest::withObserveOn, "withObserveOn", this);
            this.payloads.fixBackpressureWithBuffer = _ClassStatement.forPayload(FlowableOnBackpressureDropTest::fixBackpressureWithBuffer, "fixBackpressureWithBuffer", this);
            this.payloads.requestOverflow = _ClassStatement.forPayload(FlowableOnBackpressureDropTest::requestOverflow, "requestOverflow", this);
            this.payloads.nonFatalExceptionFromOverflowActionIsNotReportedFromUpstreamOperator = _ClassStatement.forPayload(FlowableOnBackpressureDropTest::nonFatalExceptionFromOverflowActionIsNotReportedFromUpstreamOperator, "nonFatalExceptionFromOverflowActionIsNotReportedFromUpstreamOperator", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableOnBackpressureDropTest::badSource, "badSource", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableOnBackpressureDropTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableOnBackpressureDropTest::badRequest, "badRequest", this);
        }
    }
}
