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
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableDefaultIfEmptyTest extends RxJavaTest {

    @Test
    public void defaultIfEmpty() {
        Flowable<Integer> source = Flowable.just(1, 2, 3);
        Flowable<Integer> flowable = source.defaultIfEmpty(10);
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(10);
        verify(subscriber).onNext(1);
        verify(subscriber).onNext(2);
        verify(subscriber).onNext(3);
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void defaultIfEmptyWithEmpty() {
        Flowable<Integer> source = Flowable.empty();
        Flowable<Integer> flowable = source.defaultIfEmpty(10);
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber).onNext(10);
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void backpressureEmpty() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0L);
        Flowable.<Integer>empty().defaultIfEmpty(1).subscribe(ts);
        ts.assertNoValues();
        ts.assertNotTerminated();
        ts.request(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void backpressureNonEmpty() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0L);
        Flowable.just(1, 2, 3).defaultIfEmpty(1).subscribe(ts);
        ts.assertNoValues();
        ts.assertNotTerminated();
        ts.request(2);
        ts.assertValues(1, 2);
        ts.request(1);
        ts.assertValues(1, 2, 3);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableDefaultIfEmptyTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_defaultIfEmpty() throws java.lang.Throwable {
            this.payloads.defaultIfEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_defaultIfEmptyWithEmpty() throws java.lang.Throwable {
            this.payloads.defaultIfEmptyWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureEmpty() throws java.lang.Throwable {
            this.payloads.backpressureEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNonEmpty() throws java.lang.Throwable {
            this.payloads.backpressureNonEmpty.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDefaultIfEmptyTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDefaultIfEmptyTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDefaultIfEmptyTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDefaultIfEmptyTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableDefaultIfEmptyTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDefaultIfEmptyTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableDefaultIfEmptyTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableDefaultIfEmptyTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement defaultIfEmpty;

            public org.junit.runners.model.Statement defaultIfEmptyWithEmpty;

            public org.junit.runners.model.Statement backpressureEmpty;

            public org.junit.runners.model.Statement backpressureNonEmpty;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.defaultIfEmpty = _ClassStatement.forPayload(FlowableDefaultIfEmptyTest::defaultIfEmpty, "defaultIfEmpty", this);
            this.payloads.defaultIfEmptyWithEmpty = _ClassStatement.forPayload(FlowableDefaultIfEmptyTest::defaultIfEmptyWithEmpty, "defaultIfEmptyWithEmpty", this);
            this.payloads.backpressureEmpty = _ClassStatement.forPayload(FlowableDefaultIfEmptyTest::backpressureEmpty, "backpressureEmpty", this);
            this.payloads.backpressureNonEmpty = _ClassStatement.forPayload(FlowableDefaultIfEmptyTest::backpressureNonEmpty, "backpressureNonEmpty", this);
        }
    }
}
