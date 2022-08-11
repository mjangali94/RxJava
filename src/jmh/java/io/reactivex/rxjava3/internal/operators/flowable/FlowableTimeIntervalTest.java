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
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableTimeIntervalTest extends RxJavaTest {

    private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;

    private Subscriber<Timed<Integer>> subscriber;

    private TestScheduler testScheduler;

    private PublishProcessor<Integer> processor;

    private Flowable<Timed<Integer>> flowable;

    @Before
    public void setUp() {
        subscriber = TestHelper.mockSubscriber();
        testScheduler = new TestScheduler();
        processor = PublishProcessor.create();
        flowable = processor.timeInterval(testScheduler);
    }

    @Test
    public void timeInterval() {
        InOrder inOrder = inOrder(subscriber);
        flowable.subscribe(subscriber);
        testScheduler.advanceTimeBy(1000, TIME_UNIT);
        processor.onNext(1);
        testScheduler.advanceTimeBy(2000, TIME_UNIT);
        processor.onNext(2);
        testScheduler.advanceTimeBy(3000, TIME_UNIT);
        processor.onNext(3);
        processor.onComplete();
        inOrder.verify(subscriber, times(1)).onNext(new Timed<>(1, 1000, TIME_UNIT));
        inOrder.verify(subscriber, times(1)).onNext(new Timed<>(2, 2000, TIME_UNIT));
        inOrder.verify(subscriber, times(1)).onNext(new Timed<>(3, 3000, TIME_UNIT));
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void timeIntervalDefault() {
        final TestScheduler scheduler = new TestScheduler();
        RxJavaPlugins.setComputationSchedulerHandler(new Function<Scheduler, Scheduler>() {

            @Override
            public Scheduler apply(Scheduler v) throws Exception {
                return scheduler;
            }
        });
        try {
            Flowable.range(1, 5).timeInterval().map(new Function<Timed<Integer>, Long>() {

                @Override
                public Long apply(Timed<Integer> v) throws Exception {
                    return v.time();
                }
            }).test().assertResult(0L, 0L, 0L, 0L, 0L);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void timeIntervalDefaultSchedulerCustomUnit() {
        final TestScheduler scheduler = new TestScheduler();
        RxJavaPlugins.setComputationSchedulerHandler(new Function<Scheduler, Scheduler>() {

            @Override
            public Scheduler apply(Scheduler v) throws Exception {
                return scheduler;
            }
        });
        try {
            Flowable.range(1, 5).timeInterval(TimeUnit.SECONDS).map(new Function<Timed<Integer>, Long>() {

                @Override
                public Long apply(Timed<Integer> v) throws Exception {
                    return v.time();
                }
            }).test().assertResult(0L, 0L, 0L, 0L, 0L);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).timeInterval());
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).timeInterval().test().assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Timed<Object>>>() {

            @Override
            public Publisher<Timed<Object>> apply(Flowable<Object> f) throws Exception {
                return f.timeInterval();
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableTimeIntervalTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeInterval() throws java.lang.Throwable {
            this.payloads.timeInterval.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeIntervalDefault() throws java.lang.Throwable {
            this.payloads.timeIntervalDefault.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeIntervalDefaultSchedulerCustomUnit() throws java.lang.Throwable {
            this.payloads.timeIntervalDefaultSchedulerCustomUnit.evaluate();
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
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeIntervalTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeIntervalTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.setUp();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeIntervalTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeIntervalTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableTimeIntervalTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeIntervalTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableTimeIntervalTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableTimeIntervalTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement timeInterval;

            public org.junit.runners.model.Statement timeIntervalDefault;

            public org.junit.runners.model.Statement timeIntervalDefaultSchedulerCustomUnit;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.timeInterval = _ClassStatement.forPayload(FlowableTimeIntervalTest::timeInterval, "timeInterval", this);
            this.payloads.timeIntervalDefault = _ClassStatement.forPayload(FlowableTimeIntervalTest::timeIntervalDefault, "timeIntervalDefault", this);
            this.payloads.timeIntervalDefaultSchedulerCustomUnit = _ClassStatement.forPayload(FlowableTimeIntervalTest::timeIntervalDefaultSchedulerCustomUnit, "timeIntervalDefaultSchedulerCustomUnit", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableTimeIntervalTest::dispose, "dispose", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableTimeIntervalTest::error, "error", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableTimeIntervalTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
