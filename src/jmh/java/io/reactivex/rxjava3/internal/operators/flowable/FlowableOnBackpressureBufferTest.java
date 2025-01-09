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
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableOnBackpressureBufferTest extends RxJavaTest {

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
        infinite.subscribeOn(Schedulers.computation()).onBackpressureBuffer().take(500).subscribe(ts);
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
        assertEquals(499, ts.values().get(499).intValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fixBackpressureBufferNegativeCapacity() throws InterruptedException {
        Flowable.empty().onBackpressureBuffer(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fixBackpressureBufferZeroCapacity() throws InterruptedException {
        Flowable.empty().onBackpressureBuffer(0);
    }

    @Test
    public void fixBackpressureBoundedBuffer() throws InterruptedException {
        final CountDownLatch l1 = new CountDownLatch(100);
        final CountDownLatch backpressureCallback = new CountDownLatch(1);
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
            }
        }, 0L);
        ts.request(100);
        infinite.subscribeOn(Schedulers.computation()).onBackpressureBuffer(500, new Action() {

            @Override
            public void run() {
                backpressureCallback.countDown();
            }
        }).subscribe(ts);
        l1.await();
        ts.request(50);
        assertTrue(backpressureCallback.await(500, TimeUnit.MILLISECONDS));
        ts.awaitDone(1, TimeUnit.SECONDS);
        ts.assertError(MissingBackpressureException.class);
        int size = ts.values().size();
        // will get up to 50 more
        assertTrue(size <= 150);
        assertEquals((long) ts.values().get(size - 1), size - 1);
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

    private static final Action THROWS_NON_FATAL = new Action() {

        @Override
        public void run() {
            throw new RuntimeException();
        }
    };

    @Test
    public void nonFatalExceptionThrownByOnOverflowIsNotReportedByUpstream() {
        final AtomicBoolean errorOccurred = new AtomicBoolean(false);
        TestSubscriber<Long> ts = TestSubscriber.create(0);
        infinite.subscribeOn(Schedulers.computation()).doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable t) {
                errorOccurred.set(true);
            }
        }).onBackpressureBuffer(1, THROWS_NON_FATAL).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        assertFalse(errorOccurred.get());
    }

    @Test
    public void maxSize() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        Flowable.range(1, 10).onBackpressureBuffer(1).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(MissingBackpressureException.class);
        ts.assertNotComplete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void fixBackpressureBufferNegativeCapacity2() throws InterruptedException {
        Flowable.empty().onBackpressureBuffer(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fixBackpressureBufferZeroCapacity2() throws InterruptedException {
        Flowable.empty().onBackpressureBuffer(0);
    }

    @Test
    public void noDelayError() {
        Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).onBackpressureBuffer(false).test(0L).assertFailure(TestException.class);
    }

    @Test
    public void delayError() {
        TestSubscriber<Integer> ts = Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).onBackpressureBuffer(true).test(0L).assertEmpty();
        ts.request(1);
        ts.assertFailure(TestException.class, 1);
    }

    @Test
    public void delayErrorBuffer() {
        TestSubscriber<Integer> ts = Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).onBackpressureBuffer(16, true).test(0L).assertEmpty();
        ts.request(1);
        ts.assertFailure(TestException.class, 1);
    }

    @Test
    public void fusedNormal() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.range(1, 10).onBackpressureBuffer().subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void fusedError() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.<Integer>error(new TestException()).onBackpressureBuffer().subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertFailure(TestException.class);
    }

    @Test
    public void fusedPreconsume() throws Exception {
        TestSubscriber<Integer> ts = Flowable.range(1, 1000 * 1000).onBackpressureBuffer().observeOn(Schedulers.single()).test(0L);
        ts.assertEmpty();
        Thread.sleep(100);
        ts.request(1000 * 1000);
        ts.awaitDone(5, TimeUnit.SECONDS).assertValueCount(1000 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void emptyDelayError() {
        Flowable.empty().onBackpressureBuffer(true).test().assertResult();
    }

    @Test
    public void fusionRejected() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.SYNC);
        Flowable.<Integer>never().onBackpressureBuffer().subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertEmpty();
    }

    @Test
    public void fusedNoConcurrentCleanDueToCancel() {
        for (int j = 0; j < TestHelper.RACE_LONG_LOOPS; j++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp = PublishProcessor.create();
                TestObserver<Integer> to = pp.onBackpressureBuffer(4, false, true).observeOn(Schedulers.io()).map(Functions.<Integer>identity()).observeOn(Schedulers.single()).firstOrError().test();
                for (int i = 0; pp.hasSubscribers(); i++) {
                    pp.onNext(i);
                }
                to.awaitDone(5, TimeUnit.SECONDS);
                if (!errors.isEmpty()) {
                    throw new CompositeException(errors);
                }
                to.assertResult(0);
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.onBackpressureBuffer());
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().onBackpressureBuffer());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableOnBackpressureBufferTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noBackpressureSupport() throws java.lang.Throwable {
            this.payloads.noBackpressureSupport.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fixBackpressureWithBuffer() throws java.lang.Throwable {
            this.payloads.fixBackpressureWithBuffer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fixBackpressureBufferNegativeCapacity() throws java.lang.Throwable {
            this.payloads.fixBackpressureBufferNegativeCapacity.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fixBackpressureBufferZeroCapacity() throws java.lang.Throwable {
            this.payloads.fixBackpressureBufferZeroCapacity.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fixBackpressureBoundedBuffer() throws java.lang.Throwable {
            this.payloads.fixBackpressureBoundedBuffer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonFatalExceptionThrownByOnOverflowIsNotReportedByUpstream() throws java.lang.Throwable {
            this.payloads.nonFatalExceptionThrownByOnOverflowIsNotReportedByUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maxSize() throws java.lang.Throwable {
            this.payloads.maxSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fixBackpressureBufferNegativeCapacity2() throws java.lang.Throwable {
            this.payloads.fixBackpressureBufferNegativeCapacity2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fixBackpressureBufferZeroCapacity2() throws java.lang.Throwable {
            this.payloads.fixBackpressureBufferZeroCapacity2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noDelayError() throws java.lang.Throwable {
            this.payloads.noDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayError() throws java.lang.Throwable {
            this.payloads.delayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorBuffer() throws java.lang.Throwable {
            this.payloads.delayErrorBuffer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedNormal() throws java.lang.Throwable {
            this.payloads.fusedNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedError() throws java.lang.Throwable {
            this.payloads.fusedError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPreconsume() throws java.lang.Throwable {
            this.payloads.fusedPreconsume.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyDelayError() throws java.lang.Throwable {
            this.payloads.emptyDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejected() throws java.lang.Throwable {
            this.payloads.fusionRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedNoConcurrentCleanDueToCancel() throws java.lang.Throwable {
            this.payloads.fusedNoConcurrentCleanDueToCancel.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureBufferTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureBufferTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureBufferTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureBufferTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableOnBackpressureBufferTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureBufferTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableOnBackpressureBufferTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableOnBackpressureBufferTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement noBackpressureSupport;

            public org.junit.runners.model.Statement fixBackpressureWithBuffer;

            public org.junit.runners.model.Statement fixBackpressureBufferNegativeCapacity;

            public org.junit.runners.model.Statement fixBackpressureBufferZeroCapacity;

            public org.junit.runners.model.Statement fixBackpressureBoundedBuffer;

            public org.junit.runners.model.Statement nonFatalExceptionThrownByOnOverflowIsNotReportedByUpstream;

            public org.junit.runners.model.Statement maxSize;

            public org.junit.runners.model.Statement fixBackpressureBufferNegativeCapacity2;

            public org.junit.runners.model.Statement fixBackpressureBufferZeroCapacity2;

            public org.junit.runners.model.Statement noDelayError;

            public org.junit.runners.model.Statement delayError;

            public org.junit.runners.model.Statement delayErrorBuffer;

            public org.junit.runners.model.Statement fusedNormal;

            public org.junit.runners.model.Statement fusedError;

            public org.junit.runners.model.Statement fusedPreconsume;

            public org.junit.runners.model.Statement emptyDelayError;

            public org.junit.runners.model.Statement fusionRejected;

            public org.junit.runners.model.Statement fusedNoConcurrentCleanDueToCancel;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badRequest;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.noBackpressureSupport = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::noBackpressureSupport, "noBackpressureSupport", this);
            this.payloads.fixBackpressureWithBuffer = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::fixBackpressureWithBuffer, "fixBackpressureWithBuffer", this);
            this.payloads.fixBackpressureBufferNegativeCapacity = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableOnBackpressureBufferTest::fixBackpressureBufferNegativeCapacity, java.lang.IllegalArgumentException.class), "fixBackpressureBufferNegativeCapacity", this);
            this.payloads.fixBackpressureBufferZeroCapacity = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableOnBackpressureBufferTest::fixBackpressureBufferZeroCapacity, java.lang.IllegalArgumentException.class), "fixBackpressureBufferZeroCapacity", this);
            this.payloads.fixBackpressureBoundedBuffer = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::fixBackpressureBoundedBuffer, "fixBackpressureBoundedBuffer", this);
            this.payloads.nonFatalExceptionThrownByOnOverflowIsNotReportedByUpstream = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::nonFatalExceptionThrownByOnOverflowIsNotReportedByUpstream, "nonFatalExceptionThrownByOnOverflowIsNotReportedByUpstream", this);
            this.payloads.maxSize = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::maxSize, "maxSize", this);
            this.payloads.fixBackpressureBufferNegativeCapacity2 = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableOnBackpressureBufferTest::fixBackpressureBufferNegativeCapacity2, java.lang.IllegalArgumentException.class), "fixBackpressureBufferNegativeCapacity2", this);
            this.payloads.fixBackpressureBufferZeroCapacity2 = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableOnBackpressureBufferTest::fixBackpressureBufferZeroCapacity2, java.lang.IllegalArgumentException.class), "fixBackpressureBufferZeroCapacity2", this);
            this.payloads.noDelayError = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::noDelayError, "noDelayError", this);
            this.payloads.delayError = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::delayError, "delayError", this);
            this.payloads.delayErrorBuffer = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::delayErrorBuffer, "delayErrorBuffer", this);
            this.payloads.fusedNormal = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::fusedNormal, "fusedNormal", this);
            this.payloads.fusedError = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::fusedError, "fusedError", this);
            this.payloads.fusedPreconsume = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::fusedPreconsume, "fusedPreconsume", this);
            this.payloads.emptyDelayError = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::emptyDelayError, "emptyDelayError", this);
            this.payloads.fusionRejected = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::fusionRejected, "fusionRejected", this);
            this.payloads.fusedNoConcurrentCleanDueToCancel = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::fusedNoConcurrentCleanDueToCancel, "fusedNoConcurrentCleanDueToCancel", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableOnBackpressureBufferTest::badRequest, "badRequest", this);
        }
    }
}
