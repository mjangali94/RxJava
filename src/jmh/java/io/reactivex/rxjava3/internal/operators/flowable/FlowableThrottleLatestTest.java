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

import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableThrottleLatestTest extends RxJavaTest {

    @Test
    public void just() {
        Flowable.just(1).throttleLatest(1, TimeUnit.MINUTES).test().assertResult(1);
    }

    @Test
    public void range() {
        Flowable.range(1, 5).throttleLatest(1, TimeUnit.MINUTES).test().assertResult(1);
    }

    @Test
    public void rangeEmitLatest() {
        Flowable.range(1, 5).throttleLatest(1, TimeUnit.MINUTES, true).test().assertResult(1, 5);
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).throttleLatest(1, TimeUnit.MINUTES).test().assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.throttleLatest(1, TimeUnit.MINUTES);
            }
        });
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().throttleLatest(1, TimeUnit.MINUTES));
    }

    @Test
    public void normal() {
        TestScheduler sch = new TestScheduler();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.throttleLatest(1, TimeUnit.SECONDS, sch).test();
        pp.onNext(1);
        ts.assertValuesOnly(1);
        pp.onNext(2);
        ts.assertValuesOnly(1);
        pp.onNext(3);
        ts.assertValuesOnly(1);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertValuesOnly(1, 3);
        pp.onNext(4);
        ts.assertValuesOnly(1, 3);
        pp.onNext(5);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertValuesOnly(1, 3, 5);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertValuesOnly(1, 3, 5);
        pp.onNext(6);
        ts.assertValuesOnly(1, 3, 5, 6);
        pp.onNext(7);
        pp.onComplete();
        ts.assertResult(1, 3, 5, 6);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertResult(1, 3, 5, 6);
    }

    @Test
    public void normalEmitLast() {
        TestScheduler sch = new TestScheduler();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.throttleLatest(1, TimeUnit.SECONDS, sch, true).test();
        pp.onNext(1);
        ts.assertValuesOnly(1);
        pp.onNext(2);
        ts.assertValuesOnly(1);
        pp.onNext(3);
        ts.assertValuesOnly(1);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertValuesOnly(1, 3);
        pp.onNext(4);
        ts.assertValuesOnly(1, 3);
        pp.onNext(5);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertValuesOnly(1, 3, 5);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertValuesOnly(1, 3, 5);
        pp.onNext(6);
        ts.assertValuesOnly(1, 3, 5, 6);
        pp.onNext(7);
        pp.onComplete();
        ts.assertResult(1, 3, 5, 6, 7);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertResult(1, 3, 5, 6, 7);
    }

    @Test
    public void missingBackpressureExceptionFirst() throws Throwable {
        TestScheduler sch = new TestScheduler();
        Action onCancel = mock(Action.class);
        Flowable.just(1, 2).doOnCancel(onCancel).throttleLatest(1, TimeUnit.MINUTES, sch).test(0).assertFailure(MissingBackpressureException.class);
        verify(onCancel).run();
    }

    @Test
    public void missingBackpressureExceptionLatest() throws Throwable {
        TestScheduler sch = new TestScheduler();
        Action onCancel = mock(Action.class);
        TestSubscriber<Integer> ts = Flowable.just(1, 2).concatWith(Flowable.<Integer>never()).doOnCancel(onCancel).throttleLatest(1, TimeUnit.SECONDS, sch, true).test(1);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertFailure(MissingBackpressureException.class, 1);
        verify(onCancel).run();
    }

    @Test
    public void missingBackpressureExceptionLatestComplete() throws Throwable {
        TestScheduler sch = new TestScheduler();
        Action onCancel = mock(Action.class);
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.doOnCancel(onCancel).throttleLatest(1, TimeUnit.SECONDS, sch, true).test(1);
        pp.onNext(1);
        pp.onNext(2);
        ts.assertValuesOnly(1);
        pp.onComplete();
        ts.assertFailure(MissingBackpressureException.class, 1);
        verify(onCancel, never()).run();
    }

    @Test
    public void take() throws Throwable {
        Action onCancel = mock(Action.class);
        Flowable.range(1, 5).doOnCancel(onCancel).throttleLatest(1, TimeUnit.MINUTES).take(1).test().assertResult(1);
        verify(onCancel).run();
    }

    @Test
    public void reentrantComplete() {
        TestScheduler sch = new TestScheduler();
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    pp.onNext(2);
                }
                if (t == 2) {
                    pp.onComplete();
                }
            }
        };
        pp.throttleLatest(1, TimeUnit.SECONDS, sch).subscribe(ts);
        pp.onNext(1);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertResult(1, 2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableThrottleLatestTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.payloads.just.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_range() throws java.lang.Throwable {
            this.payloads.range.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeEmitLatest() throws java.lang.Throwable {
            this.payloads.rangeEmitLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
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
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmitLast() throws java.lang.Throwable {
            this.payloads.normalEmitLast.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_missingBackpressureExceptionFirst() throws java.lang.Throwable {
            this.payloads.missingBackpressureExceptionFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_missingBackpressureExceptionLatest() throws java.lang.Throwable {
            this.payloads.missingBackpressureExceptionLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_missingBackpressureExceptionLatestComplete() throws java.lang.Throwable {
            this.payloads.missingBackpressureExceptionLatestComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantComplete() throws java.lang.Throwable {
            this.payloads.reentrantComplete.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableThrottleLatestTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableThrottleLatestTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableThrottleLatestTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableThrottleLatestTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableThrottleLatestTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableThrottleLatestTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableThrottleLatestTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableThrottleLatestTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement just;

            public org.junit.runners.model.Statement range;

            public org.junit.runners.model.Statement rangeEmitLatest;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement normalEmitLast;

            public org.junit.runners.model.Statement missingBackpressureExceptionFirst;

            public org.junit.runners.model.Statement missingBackpressureExceptionLatest;

            public org.junit.runners.model.Statement missingBackpressureExceptionLatestComplete;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement reentrantComplete;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.just = _ClassStatement.forPayload(FlowableThrottleLatestTest::just, "just", this);
            this.payloads.range = _ClassStatement.forPayload(FlowableThrottleLatestTest::range, "range", this);
            this.payloads.rangeEmitLatest = _ClassStatement.forPayload(FlowableThrottleLatestTest::rangeEmitLatest, "rangeEmitLatest", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableThrottleLatestTest::error, "error", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableThrottleLatestTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableThrottleLatestTest::badRequest, "badRequest", this);
            this.payloads.normal = _ClassStatement.forPayload(FlowableThrottleLatestTest::normal, "normal", this);
            this.payloads.normalEmitLast = _ClassStatement.forPayload(FlowableThrottleLatestTest::normalEmitLast, "normalEmitLast", this);
            this.payloads.missingBackpressureExceptionFirst = _ClassStatement.forPayload(FlowableThrottleLatestTest::missingBackpressureExceptionFirst, "missingBackpressureExceptionFirst", this);
            this.payloads.missingBackpressureExceptionLatest = _ClassStatement.forPayload(FlowableThrottleLatestTest::missingBackpressureExceptionLatest, "missingBackpressureExceptionLatest", this);
            this.payloads.missingBackpressureExceptionLatestComplete = _ClassStatement.forPayload(FlowableThrottleLatestTest::missingBackpressureExceptionLatestComplete, "missingBackpressureExceptionLatestComplete", this);
            this.payloads.take = _ClassStatement.forPayload(FlowableThrottleLatestTest::take, "take", this);
            this.payloads.reentrantComplete = _ClassStatement.forPayload(FlowableThrottleLatestTest::reentrantComplete, "reentrantComplete", this);
        }
    }
}
