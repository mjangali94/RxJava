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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableTakeLastTimedTest extends RxJavaTest {

    @Test(expected = IllegalArgumentException.class)
    public void takeLastTimedWithNegativeCount() {
        Flowable.just("one").takeLast(-1, 1, TimeUnit.SECONDS);
    }

    @Test
    public void takeLastTimed() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Object> source = PublishProcessor.create();
        // FIXME time unit now matters!
        Flowable<Object> result = source.takeLast(1000, TimeUnit.MILLISECONDS, scheduler);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        result.subscribe(subscriber);
        // T: 0ms
        source.onNext(1);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 250ms
        source.onNext(2);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 500ms
        source.onNext(3);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 750ms
        source.onNext(4);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1000ms
        source.onNext(5);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1250ms
        source.onComplete();
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onNext(3);
        inOrder.verify(subscriber, times(1)).onNext(4);
        inOrder.verify(subscriber, times(1)).onNext(5);
        inOrder.verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void takeLastTimedDelayCompletion() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Object> source = PublishProcessor.create();
        // FIXME time unit now matters
        Flowable<Object> result = source.takeLast(1000, TimeUnit.MILLISECONDS, scheduler);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        result.subscribe(subscriber);
        // T: 0ms
        source.onNext(1);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 250ms
        source.onNext(2);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 500ms
        source.onNext(3);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 750ms
        source.onNext(4);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1000ms
        source.onNext(5);
        scheduler.advanceTimeBy(1250, TimeUnit.MILLISECONDS);
        // T: 2250ms
        source.onComplete();
        inOrder.verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void takeLastTimedWithCapacity() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Object> source = PublishProcessor.create();
        // FIXME time unit now matters!
        Flowable<Object> result = source.takeLast(2, 1000, TimeUnit.MILLISECONDS, scheduler);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        result.subscribe(subscriber);
        // T: 0ms
        source.onNext(1);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 250ms
        source.onNext(2);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 500ms
        source.onNext(3);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 750ms
        source.onNext(4);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1000ms
        source.onNext(5);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1250ms
        source.onComplete();
        inOrder.verify(subscriber, times(1)).onNext(4);
        inOrder.verify(subscriber, times(1)).onNext(5);
        inOrder.verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void takeLastTimedThrowingSource() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Object> source = PublishProcessor.create();
        Flowable<Object> result = source.takeLast(1, TimeUnit.SECONDS, scheduler);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        result.subscribe(subscriber);
        // T: 0ms
        source.onNext(1);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 250ms
        source.onNext(2);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 500ms
        source.onNext(3);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 750ms
        source.onNext(4);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1000ms
        source.onNext(5);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1250ms
        source.onError(new TestException());
        inOrder.verify(subscriber, times(1)).onError(any(TestException.class));
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void takeLastTimedWithZeroCapacity() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Object> source = PublishProcessor.create();
        Flowable<Object> result = source.takeLast(0, 1, TimeUnit.SECONDS, scheduler);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        result.subscribe(subscriber);
        // T: 0ms
        source.onNext(1);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 250ms
        source.onNext(2);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 500ms
        source.onNext(3);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 750ms
        source.onNext(4);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1000ms
        source.onNext(5);
        scheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS);
        // T: 1250ms
        source.onComplete();
        inOrder.verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void continuousDelivery() {
        TestScheduler scheduler = new TestScheduler();
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.takeLast(1000, TimeUnit.MILLISECONDS, scheduler).subscribe(ts);
        pp.onNext(1);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        pp.onNext(2);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        pp.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        pp.onNext(4);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        pp.onComplete();
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        ts.assertNoValues();
        ts.request(1);
        ts.assertValue(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        ts.request(1);
        ts.assertValues(3, 4);
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void takeLastTimeAndSize() {
        Flowable.just(1, 2).takeLast(1, 1, TimeUnit.MINUTES).test().assertResult(2);
    }

    @Test
    public void takeLastTime() {
        Flowable.just(1, 2).takeLast(1, TimeUnit.MINUTES).test().assertResult(1, 2);
    }

    @Test
    public void takeLastTimeDelayError() {
        Flowable.just(1, 2).concatWith(Flowable.<Integer>error(new TestException())).takeLast(1, TimeUnit.MINUTES, true).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void takeLastTimeDelayErrorCustomScheduler() {
        Flowable.just(1, 2).concatWith(Flowable.<Integer>error(new TestException())).takeLast(1, TimeUnit.MINUTES, Schedulers.io(), true).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishProcessor.create().takeLast(1, TimeUnit.MINUTES));
    }

    @Test
    public void observeOn() {
        Observable.range(1, 1000).takeLast(1, TimeUnit.DAYS).take(500).observeOn(Schedulers.single(), true, 1).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(500).assertNoErrors().assertComplete();
    }

    @Test
    public void cancelCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final TestSubscriber<Integer> ts = pp.takeLast(1, TimeUnit.DAYS).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void emptyDelayError() {
        Flowable.empty().takeLast(1, TimeUnit.DAYS, true).test().assertResult();
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.takeLast(1, TimeUnit.SECONDS);
            }
        });
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(PublishProcessor.create().takeLast(1, TimeUnit.SECONDS));
    }

    @Test
    public void lastWindowIsFixedInTime() {
        TimesteppingScheduler scheduler = new TimesteppingScheduler();
        scheduler.stepEnabled = false;
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.takeLast(2, TimeUnit.SECONDS, scheduler).test();
        pp.onNext(1);
        pp.onNext(2);
        pp.onNext(3);
        pp.onNext(4);
        scheduler.stepEnabled = true;
        pp.onComplete();
        ts.assertResult(1, 2, 3, 4);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableTakeLastTimedTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimedWithNegativeCount() throws java.lang.Throwable {
            this.payloads.takeLastTimedWithNegativeCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimed() throws java.lang.Throwable {
            this.payloads.takeLastTimed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimedDelayCompletion() throws java.lang.Throwable {
            this.payloads.takeLastTimedDelayCompletion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimedWithCapacity() throws java.lang.Throwable {
            this.payloads.takeLastTimedWithCapacity.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimedThrowingSource() throws java.lang.Throwable {
            this.payloads.takeLastTimedThrowingSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimedWithZeroCapacity() throws java.lang.Throwable {
            this.payloads.takeLastTimedWithZeroCapacity.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_continuousDelivery() throws java.lang.Throwable {
            this.payloads.continuousDelivery.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimeAndSize() throws java.lang.Throwable {
            this.payloads.takeLastTimeAndSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTime() throws java.lang.Throwable {
            this.payloads.takeLastTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimeDelayError() throws java.lang.Throwable {
            this.payloads.takeLastTimeDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTimeDelayErrorCustomScheduler() throws java.lang.Throwable {
            this.payloads.takeLastTimeDelayErrorCustomScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOn() throws java.lang.Throwable {
            this.payloads.observeOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelCompleteRace() throws java.lang.Throwable {
            this.payloads.cancelCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyDelayError() throws java.lang.Throwable {
            this.payloads.emptyDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWindowIsFixedInTime() throws java.lang.Throwable {
            this.payloads.lastWindowIsFixedInTime.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastTimedTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastTimedTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastTimedTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastTimedTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableTakeLastTimedTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastTimedTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableTakeLastTimedTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableTakeLastTimedTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement takeLastTimedWithNegativeCount;

            public org.junit.runners.model.Statement takeLastTimed;

            public org.junit.runners.model.Statement takeLastTimedDelayCompletion;

            public org.junit.runners.model.Statement takeLastTimedWithCapacity;

            public org.junit.runners.model.Statement takeLastTimedThrowingSource;

            public org.junit.runners.model.Statement takeLastTimedWithZeroCapacity;

            public org.junit.runners.model.Statement continuousDelivery;

            public org.junit.runners.model.Statement takeLastTimeAndSize;

            public org.junit.runners.model.Statement takeLastTime;

            public org.junit.runners.model.Statement takeLastTimeDelayError;

            public org.junit.runners.model.Statement takeLastTimeDelayErrorCustomScheduler;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement observeOn;

            public org.junit.runners.model.Statement cancelCompleteRace;

            public org.junit.runners.model.Statement emptyDelayError;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement lastWindowIsFixedInTime;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.takeLastTimedWithNegativeCount = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableTakeLastTimedTest::takeLastTimedWithNegativeCount, java.lang.IllegalArgumentException.class), "takeLastTimedWithNegativeCount", this);
            this.payloads.takeLastTimed = _ClassStatement.forPayload(FlowableTakeLastTimedTest::takeLastTimed, "takeLastTimed", this);
            this.payloads.takeLastTimedDelayCompletion = _ClassStatement.forPayload(FlowableTakeLastTimedTest::takeLastTimedDelayCompletion, "takeLastTimedDelayCompletion", this);
            this.payloads.takeLastTimedWithCapacity = _ClassStatement.forPayload(FlowableTakeLastTimedTest::takeLastTimedWithCapacity, "takeLastTimedWithCapacity", this);
            this.payloads.takeLastTimedThrowingSource = _ClassStatement.forPayload(FlowableTakeLastTimedTest::takeLastTimedThrowingSource, "takeLastTimedThrowingSource", this);
            this.payloads.takeLastTimedWithZeroCapacity = _ClassStatement.forPayload(FlowableTakeLastTimedTest::takeLastTimedWithZeroCapacity, "takeLastTimedWithZeroCapacity", this);
            this.payloads.continuousDelivery = _ClassStatement.forPayload(FlowableTakeLastTimedTest::continuousDelivery, "continuousDelivery", this);
            this.payloads.takeLastTimeAndSize = _ClassStatement.forPayload(FlowableTakeLastTimedTest::takeLastTimeAndSize, "takeLastTimeAndSize", this);
            this.payloads.takeLastTime = _ClassStatement.forPayload(FlowableTakeLastTimedTest::takeLastTime, "takeLastTime", this);
            this.payloads.takeLastTimeDelayError = _ClassStatement.forPayload(FlowableTakeLastTimedTest::takeLastTimeDelayError, "takeLastTimeDelayError", this);
            this.payloads.takeLastTimeDelayErrorCustomScheduler = _ClassStatement.forPayload(FlowableTakeLastTimedTest::takeLastTimeDelayErrorCustomScheduler, "takeLastTimeDelayErrorCustomScheduler", this);
            this.payloads.disposed = _ClassStatement.forPayload(FlowableTakeLastTimedTest::disposed, "disposed", this);
            this.payloads.observeOn = _ClassStatement.forPayload(FlowableTakeLastTimedTest::observeOn, "observeOn", this);
            this.payloads.cancelCompleteRace = _ClassStatement.forPayload(FlowableTakeLastTimedTest::cancelCompleteRace, "cancelCompleteRace", this);
            this.payloads.emptyDelayError = _ClassStatement.forPayload(FlowableTakeLastTimedTest::emptyDelayError, "emptyDelayError", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableTakeLastTimedTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableTakeLastTimedTest::badRequest, "badRequest", this);
            this.payloads.lastWindowIsFixedInTime = _ClassStatement.forPayload(FlowableTakeLastTimedTest::lastWindowIsFixedInTime, "lastWindowIsFixedInTime", this);
        }
    }
}
