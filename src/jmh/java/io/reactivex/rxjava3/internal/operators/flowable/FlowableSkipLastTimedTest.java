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
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableSkipLastTimedTest extends RxJavaTest {

    @Test
    public void skipLastTimed() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> source = PublishProcessor.create();
        // FIXME the timeunit now matters due to rounding
        Flowable<Integer> result = source.skipLast(1000, TimeUnit.MILLISECONDS, scheduler);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onNext(4);
        source.onNext(5);
        source.onNext(6);
        scheduler.advanceTimeBy(950, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onNext(1);
        inOrder.verify(subscriber).onNext(2);
        inOrder.verify(subscriber).onNext(3);
        inOrder.verify(subscriber, never()).onNext(4);
        inOrder.verify(subscriber, never()).onNext(5);
        inOrder.verify(subscriber, never()).onNext(6);
        inOrder.verify(subscriber).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipLastTimedErrorBeforeTime() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<Integer> result = source.skipLast(1, TimeUnit.SECONDS, scheduler);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onError(new TestException());
        scheduler.advanceTimeBy(1050, TimeUnit.MILLISECONDS);
        verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void skipLastTimedCompleteBeforeTime() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<Integer> result = source.skipLast(1, TimeUnit.SECONDS, scheduler);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipLastTimedWhenAllElementsAreValid() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<Integer> result = source.skipLast(1, TimeUnit.MILLISECONDS, scheduler);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onNext(1);
        inOrder.verify(subscriber).onNext(2);
        inOrder.verify(subscriber).onNext(3);
        inOrder.verify(subscriber).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void skipLastTimedDefaultScheduler() {
        Flowable.just(1).concatWith(Flowable.just(2).delay(500, TimeUnit.MILLISECONDS)).skipLast(300, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void skipLastTimedDefaultSchedulerDelayError() {
        Flowable.just(1).concatWith(Flowable.just(2).delay(500, TimeUnit.MILLISECONDS)).skipLast(300, TimeUnit.MILLISECONDS, true).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void skipLastTimedCustomSchedulerDelayError() {
        Flowable.just(1).concatWith(Flowable.just(2).delay(500, TimeUnit.MILLISECONDS)).skipLast(300, TimeUnit.MILLISECONDS, Schedulers.io(), true).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().skipLast(1, TimeUnit.DAYS));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.skipLast(1, TimeUnit.DAYS);
            }
        });
    }

    @Test
    public void onNextDisposeRace() {
        TestScheduler scheduler = new TestScheduler();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final TestSubscriber<Integer> ts = pp.skipLast(1, TimeUnit.DAYS, scheduler).test();
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
    public void errorDelayed() {
        Flowable.error(new TestException()).skipLast(1, TimeUnit.DAYS, new TestScheduler(), true).test().assertFailure(TestException.class);
    }

    @Test
    public void take() {
        Flowable.just(1).skipLast(0, TimeUnit.SECONDS).take(1).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void observeOn() {
        Flowable.range(1, 1000).skipLast(0, TimeUnit.SECONDS).observeOn(Schedulers.single(), false, 16).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(1000).assertComplete().assertNoErrors();
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().skipLast(1, TimeUnit.MINUTES));
    }

    @Test
    public void delayErrorMoreWork() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.skipLast(0, TimeUnit.MILLISECONDS, true).doOnNext(v -> {
            if (v == 1) {
                pp.onNext(1);
                pp.onComplete();
            }
        }).test();
        pp.onNext(1);
        ts.assertComplete();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableSkipLastTimedTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimed() throws java.lang.Throwable {
            this.payloads.skipLastTimed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedErrorBeforeTime() throws java.lang.Throwable {
            this.payloads.skipLastTimedErrorBeforeTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedCompleteBeforeTime() throws java.lang.Throwable {
            this.payloads.skipLastTimedCompleteBeforeTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedWhenAllElementsAreValid() throws java.lang.Throwable {
            this.payloads.skipLastTimedWhenAllElementsAreValid.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedDefaultScheduler() throws java.lang.Throwable {
            this.payloads.skipLastTimedDefaultScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedDefaultSchedulerDelayError() throws java.lang.Throwable {
            this.payloads.skipLastTimedDefaultSchedulerDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedCustomSchedulerDelayError() throws java.lang.Throwable {
            this.payloads.skipLastTimedCustomSchedulerDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextDisposeRace() throws java.lang.Throwable {
            this.payloads.onNextDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDelayed() throws java.lang.Throwable {
            this.payloads.errorDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOn() throws java.lang.Throwable {
            this.payloads.observeOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorMoreWork() throws java.lang.Throwable {
            this.payloads.delayErrorMoreWork.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipLastTimedTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipLastTimedTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipLastTimedTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipLastTimedTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableSkipLastTimedTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipLastTimedTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableSkipLastTimedTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableSkipLastTimedTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement skipLastTimed;

            public org.junit.runners.model.Statement skipLastTimedErrorBeforeTime;

            public org.junit.runners.model.Statement skipLastTimedCompleteBeforeTime;

            public org.junit.runners.model.Statement skipLastTimedWhenAllElementsAreValid;

            public org.junit.runners.model.Statement skipLastTimedDefaultScheduler;

            public org.junit.runners.model.Statement skipLastTimedDefaultSchedulerDelayError;

            public org.junit.runners.model.Statement skipLastTimedCustomSchedulerDelayError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement onNextDisposeRace;

            public org.junit.runners.model.Statement errorDelayed;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement observeOn;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement delayErrorMoreWork;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.skipLastTimed = _ClassStatement.forPayload(FlowableSkipLastTimedTest::skipLastTimed, "skipLastTimed", this);
            this.payloads.skipLastTimedErrorBeforeTime = _ClassStatement.forPayload(FlowableSkipLastTimedTest::skipLastTimedErrorBeforeTime, "skipLastTimedErrorBeforeTime", this);
            this.payloads.skipLastTimedCompleteBeforeTime = _ClassStatement.forPayload(FlowableSkipLastTimedTest::skipLastTimedCompleteBeforeTime, "skipLastTimedCompleteBeforeTime", this);
            this.payloads.skipLastTimedWhenAllElementsAreValid = _ClassStatement.forPayload(FlowableSkipLastTimedTest::skipLastTimedWhenAllElementsAreValid, "skipLastTimedWhenAllElementsAreValid", this);
            this.payloads.skipLastTimedDefaultScheduler = _ClassStatement.forPayload(FlowableSkipLastTimedTest::skipLastTimedDefaultScheduler, "skipLastTimedDefaultScheduler", this);
            this.payloads.skipLastTimedDefaultSchedulerDelayError = _ClassStatement.forPayload(FlowableSkipLastTimedTest::skipLastTimedDefaultSchedulerDelayError, "skipLastTimedDefaultSchedulerDelayError", this);
            this.payloads.skipLastTimedCustomSchedulerDelayError = _ClassStatement.forPayload(FlowableSkipLastTimedTest::skipLastTimedCustomSchedulerDelayError, "skipLastTimedCustomSchedulerDelayError", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableSkipLastTimedTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableSkipLastTimedTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.onNextDisposeRace = _ClassStatement.forPayload(FlowableSkipLastTimedTest::onNextDisposeRace, "onNextDisposeRace", this);
            this.payloads.errorDelayed = _ClassStatement.forPayload(FlowableSkipLastTimedTest::errorDelayed, "errorDelayed", this);
            this.payloads.take = _ClassStatement.forPayload(FlowableSkipLastTimedTest::take, "take", this);
            this.payloads.observeOn = _ClassStatement.forPayload(FlowableSkipLastTimedTest::observeOn, "observeOn", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableSkipLastTimedTest::badRequest, "badRequest", this);
            this.payloads.delayErrorMoreWork = _ClassStatement.forPayload(FlowableSkipLastTimedTest::delayErrorMoreWork, "delayErrorMoreWork", this);
        }
    }
}
