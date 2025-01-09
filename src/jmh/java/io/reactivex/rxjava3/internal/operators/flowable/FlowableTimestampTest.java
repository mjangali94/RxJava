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
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableTimestampTest extends RxJavaTest {

    Subscriber<Object> subscriber;

    @Before
    public void before() {
        subscriber = TestHelper.mockSubscriber();
    }

    @Test
    public void timestampWithScheduler() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<Timed<Integer>> m = source.timestamp(scheduler);
        m.subscribe(subscriber);
        source.onNext(1);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        source.onNext(2);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        source.onNext(3);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(new Timed<>(1, 0, TimeUnit.MILLISECONDS));
        inOrder.verify(subscriber, times(1)).onNext(new Timed<>(2, 100, TimeUnit.MILLISECONDS));
        inOrder.verify(subscriber, times(1)).onNext(new Timed<>(3, 200, TimeUnit.MILLISECONDS));
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void timestampWithScheduler2() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<Timed<Integer>> m = source.timestamp(scheduler);
        m.subscribe(subscriber);
        source.onNext(1);
        source.onNext(2);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        source.onNext(3);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(new Timed<>(1, 0, TimeUnit.MILLISECONDS));
        inOrder.verify(subscriber, times(1)).onNext(new Timed<>(2, 0, TimeUnit.MILLISECONDS));
        inOrder.verify(subscriber, times(1)).onNext(new Timed<>(3, 200, TimeUnit.MILLISECONDS));
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
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
            Flowable.range(1, 5).timestamp().map(new Function<Timed<Integer>, Long>() {

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
            Flowable.range(1, 5).timestamp(TimeUnit.SECONDS).map(new Function<Timed<Integer>, Long>() {

                @Override
                public Long apply(Timed<Integer> v) throws Exception {
                    return v.time();
                }
            }).test().assertResult(0L, 0L, 0L, 0L, 0L);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableTimestampTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timestampWithScheduler() throws java.lang.Throwable {
            this.payloads.timestampWithScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timestampWithScheduler2() throws java.lang.Throwable {
            this.payloads.timestampWithScheduler2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeIntervalDefault() throws java.lang.Throwable {
            this.payloads.timeIntervalDefault.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeIntervalDefaultSchedulerCustomUnit() throws java.lang.Throwable {
            this.payloads.timeIntervalDefaultSchedulerCustomUnit.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimestampTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimestampTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.before();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimestampTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimestampTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableTimestampTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimestampTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableTimestampTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableTimestampTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement timestampWithScheduler;

            public org.junit.runners.model.Statement timestampWithScheduler2;

            public org.junit.runners.model.Statement timeIntervalDefault;

            public org.junit.runners.model.Statement timeIntervalDefaultSchedulerCustomUnit;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.timestampWithScheduler = _ClassStatement.forPayload(FlowableTimestampTest::timestampWithScheduler, "timestampWithScheduler", this);
            this.payloads.timestampWithScheduler2 = _ClassStatement.forPayload(FlowableTimestampTest::timestampWithScheduler2, "timestampWithScheduler2", this);
            this.payloads.timeIntervalDefault = _ClassStatement.forPayload(FlowableTimestampTest::timeIntervalDefault, "timeIntervalDefault", this);
            this.payloads.timeIntervalDefaultSchedulerCustomUnit = _ClassStatement.forPayload(FlowableTimestampTest::timeIntervalDefaultSchedulerCustomUnit, "timeIntervalDefaultSchedulerCustomUnit", this);
        }
    }
}
