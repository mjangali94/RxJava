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

import static io.reactivex.rxjava3.core.BackpressureOverflowStrategy.*;
import static io.reactivex.rxjava3.internal.functions.Functions.EMPTY_ACTION;
import static org.junit.Assert.assertEquals;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableOnBackpressureBufferStrategyTest extends RxJavaTest {

    @Test
    public void backpressureWithBufferDropOldest() throws InterruptedException {
        int bufferSize = 3;
        final AtomicInteger droppedCount = new AtomicInteger(0);
        Action incrementOnDrop = new Action() {

            @Override
            public void run() throws Exception {
                droppedCount.incrementAndGet();
            }
        };
        TestSubscriber<Long> ts = createTestSubscriber();
        Flowable.fromPublisher(send500ValuesAndComplete.onBackpressureBuffer(bufferSize, incrementOnDrop, DROP_OLDEST)).subscribe(ts);
        // we request 10 but only 3 should come from the buffer
        ts.request(10);
        ts.awaitDone(5, TimeUnit.SECONDS);
        assertEquals(bufferSize, ts.values().size());
        ts.assertNoErrors();
        assertEquals(497, ts.values().get(0).intValue());
        assertEquals(498, ts.values().get(1).intValue());
        assertEquals(499, ts.values().get(2).intValue());
        assertEquals(droppedCount.get(), 500 - bufferSize);
    }

    private TestSubscriber<Long> createTestSubscriber() {
        return new TestSubscriber<>(new DefaultSubscriber<Long>() {

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
            }
        }, 0L);
    }

    @Test
    public void backpressureWithBufferDropLatest() throws InterruptedException {
        int bufferSize = 3;
        final AtomicInteger droppedCount = new AtomicInteger(0);
        Action incrementOnDrop = new Action() {

            @Override
            public void run() throws Exception {
                droppedCount.incrementAndGet();
            }
        };
        TestSubscriber<Long> ts = createTestSubscriber();
        Flowable.fromPublisher(send500ValuesAndComplete.onBackpressureBuffer(bufferSize, incrementOnDrop, DROP_LATEST)).subscribe(ts);
        // we request 10 but only 3 should come from the buffer
        ts.request(10);
        ts.awaitDone(5, TimeUnit.SECONDS);
        assertEquals(bufferSize, ts.values().size());
        ts.assertNoErrors();
        assertEquals(0, ts.values().get(0).intValue());
        assertEquals(1, ts.values().get(1).intValue());
        assertEquals(499, ts.values().get(2).intValue());
        assertEquals(droppedCount.get(), 500 - bufferSize);
    }

    private static final Flowable<Long> send500ValuesAndComplete = Flowable.unsafeCreate(new Publisher<Long>() {

        @Override
        public void subscribe(Subscriber<? super Long> s) {
            BooleanSubscription bs = new BooleanSubscription();
            s.onSubscribe(bs);
            long i = 0;
            while (!bs.isCancelled() && i < 500) {
                s.onNext(i++);
            }
            if (!bs.isCancelled()) {
                s.onComplete();
            }
        }
    });

    @Test(expected = IllegalArgumentException.class)
    public void backpressureBufferNegativeCapacity() throws InterruptedException {
        Flowable.empty().onBackpressureBuffer(-1, EMPTY_ACTION, DROP_OLDEST);
    }

    @Test(expected = IllegalArgumentException.class)
    public void backpressureBufferZeroCapacity() throws InterruptedException {
        Flowable.empty().onBackpressureBuffer(0, EMPTY_ACTION, DROP_OLDEST);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).onBackpressureBuffer(16, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR));
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).onBackpressureBuffer(16, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR).test().assertFailure(TestException.class);
    }

    @Test
    public void overflowError() {
        Flowable.range(1, 20).onBackpressureBuffer(8, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR).test(0L).assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Object>, Object>() {

            @Override
            public Object apply(Flowable<Object> f) throws Exception {
                return f.onBackpressureBuffer(8, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR);
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.onBackpressureBuffer(8, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR);
            }
        });
    }

    @Test
    public void overflowCrashes() {
        Flowable.range(1, 20).onBackpressureBuffer(8, new Action() {

            @Override
            public void run() throws Exception {
                throw new TestException();
            }
        }, BackpressureOverflowStrategy.DROP_OLDEST).test(0L).assertFailure(TestException.class);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.just(1).onBackpressureBuffer(16, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR));
    }

    @Test
    public void empty() {
        Flowable.empty().onBackpressureBuffer(16, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR).test(0L).assertResult();
    }

    @Test
    public void justTake() {
        Flowable.just(1).onBackpressureBuffer(16, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR).take(1).test().assertResult(1);
    }

    @Test
    public void overflowNullAction() {
        Flowable.range(1, 5).onBackpressureBuffer(1, null, BackpressureOverflowStrategy.DROP_OLDEST).test(0L).assertEmpty();
    }

    @Test
    public void cancelOnDrain() {
        Flowable.range(1, 5).onBackpressureBuffer(10, null, BackpressureOverflowStrategy.DROP_OLDEST).takeUntil(v -> true).test(0L).assertEmpty().requestMore(10).assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableOnBackpressureBufferStrategyTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithBufferDropOldest() throws java.lang.Throwable {
            this.payloads.backpressureWithBufferDropOldest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithBufferDropLatest() throws java.lang.Throwable {
            this.payloads.backpressureWithBufferDropLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureBufferNegativeCapacity() throws java.lang.Throwable {
            this.payloads.backpressureBufferNegativeCapacity.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureBufferZeroCapacity() throws java.lang.Throwable {
            this.payloads.backpressureBufferZeroCapacity.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overflowError() throws java.lang.Throwable {
            this.payloads.overflowError.evaluate();
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
        public void benchmark_overflowCrashes() throws java.lang.Throwable {
            this.payloads.overflowCrashes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justTake() throws java.lang.Throwable {
            this.payloads.justTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overflowNullAction() throws java.lang.Throwable {
            this.payloads.overflowNullAction.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOnDrain() throws java.lang.Throwable {
            this.payloads.cancelOnDrain.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureBufferStrategyTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureBufferStrategyTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureBufferStrategyTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureBufferStrategyTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableOnBackpressureBufferStrategyTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureBufferStrategyTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableOnBackpressureBufferStrategyTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableOnBackpressureBufferStrategyTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement backpressureWithBufferDropOldest;

            public org.junit.runners.model.Statement backpressureWithBufferDropLatest;

            public org.junit.runners.model.Statement backpressureBufferNegativeCapacity;

            public org.junit.runners.model.Statement backpressureBufferZeroCapacity;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement overflowError;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement overflowCrashes;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement justTake;

            public org.junit.runners.model.Statement overflowNullAction;

            public org.junit.runners.model.Statement cancelOnDrain;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.backpressureWithBufferDropOldest = _ClassStatement.forPayload(FlowableOnBackpressureBufferStrategyTest::backpressureWithBufferDropOldest, "backpressureWithBufferDropOldest", this);
            this.payloads.backpressureWithBufferDropLatest = _ClassStatement.forPayload(FlowableOnBackpressureBufferStrategyTest::backpressureWithBufferDropLatest, "backpressureWithBufferDropLatest", this);
            this.payloads.backpressureBufferNegativeCapacity = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableOnBackpressureBufferStrategyTest::backpressureBufferNegativeCapacity, java.lang.IllegalArgumentException.class), "backpressureBufferNegativeCapacity", this);
            this.payloads.backpressureBufferZeroCapacity = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableOnBackpressureBufferStrategyTest::backpressureBufferZeroCapacity, java.lang.IllegalArgumentException.class), "backpressureBufferZeroCapacity", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableOnBackpressureBufferStrategyTest::dispose, "dispose", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableOnBackpressureBufferStrategyTest::error, "error", this);
            this.payloads.overflowError = _ClassStatement.forPayload(FlowableOnBackpressureBufferStrategyTest::overflowError, "overflowError", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableOnBackpressureBufferStrategyTest::badSource, "badSource", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableOnBackpressureBufferStrategyTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.overflowCrashes = _ClassStatement.forPayload(FlowableOnBackpressureBufferStrategyTest::overflowCrashes, "overflowCrashes", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableOnBackpressureBufferStrategyTest::badRequest, "badRequest", this);
            this.payloads.empty = _ClassStatement.forPayload(FlowableOnBackpressureBufferStrategyTest::empty, "empty", this);
            this.payloads.justTake = _ClassStatement.forPayload(FlowableOnBackpressureBufferStrategyTest::justTake, "justTake", this);
            this.payloads.overflowNullAction = _ClassStatement.forPayload(FlowableOnBackpressureBufferStrategyTest::overflowNullAction, "overflowNullAction", this);
            this.payloads.cancelOnDrain = _ClassStatement.forPayload(FlowableOnBackpressureBufferStrategyTest::cancelOnDrain, "cancelOnDrain", this);
        }
    }
}
