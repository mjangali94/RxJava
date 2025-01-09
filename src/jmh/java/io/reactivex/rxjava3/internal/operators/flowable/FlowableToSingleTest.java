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

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

public class FlowableToSingleTest extends RxJavaTest {

    @Test
    public void justSingleItemObservable() {
        TestSubscriber<String> subscriber = TestSubscriber.create();
        Single<String> single = Flowable.just("Hello World!").single("");
        single.toFlowable().subscribe(subscriber);
        subscriber.assertResult("Hello World!");
    }

    @Test
    public void errorObservable() {
        TestSubscriber<String> subscriber = TestSubscriber.create();
        IllegalArgumentException error = new IllegalArgumentException("Error");
        Single<String> single = Flowable.<String>error(error).single("");
        single.toFlowable().subscribe(subscriber);
        subscriber.assertError(error);
    }

    @Test
    public void justTwoEmissionsObservableThrowsError() {
        TestSubscriber<String> subscriber = TestSubscriber.create();
        Single<String> single = Flowable.just("First", "Second").single("");
        single.toFlowable().subscribe(subscriber);
        subscriber.assertError(IllegalArgumentException.class);
    }

    @Test
    public void emptyObservable() {
        TestSubscriber<String> subscriber = TestSubscriber.create();
        Single<String> single = Flowable.<String>empty().single("");
        single.toFlowable().subscribe(subscriber);
        subscriber.assertResult("");
    }

    @Test
    public void repeatObservableThrowsError() {
        TestSubscriber<String> subscriber = TestSubscriber.create();
        Single<String> single = Flowable.just("First", "Second").repeat().single("");
        single.toFlowable().subscribe(subscriber);
        subscriber.assertError(IllegalArgumentException.class);
    }

    @Test
    public void shouldUseUnsafeSubscribeInternallyNotSubscribe() {
        TestSubscriber<String> subscriber = TestSubscriber.create();
        final AtomicBoolean unsubscribed = new AtomicBoolean(false);
        Single<String> single = Flowable.just("Hello World!").doOnCancel(new Action() {

            @Override
            public void run() {
                unsubscribed.set(true);
            }
        }).single("");
        single.toFlowable().subscribe(subscriber);
        subscriber.assertComplete();
        Assert.assertFalse(unsubscribed.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableToSingleTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justSingleItemObservable() throws java.lang.Throwable {
            this.payloads.justSingleItemObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorObservable() throws java.lang.Throwable {
            this.payloads.errorObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justTwoEmissionsObservableThrowsError() throws java.lang.Throwable {
            this.payloads.justTwoEmissionsObservableThrowsError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyObservable() throws java.lang.Throwable {
            this.payloads.emptyObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatObservableThrowsError() throws java.lang.Throwable {
            this.payloads.repeatObservableThrowsError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldUseUnsafeSubscribeInternallyNotSubscribe() throws java.lang.Throwable {
            this.payloads.shouldUseUnsafeSubscribeInternallyNotSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToSingleTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToSingleTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToSingleTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToSingleTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableToSingleTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToSingleTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableToSingleTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableToSingleTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement justSingleItemObservable;

            public org.junit.runners.model.Statement errorObservable;

            public org.junit.runners.model.Statement justTwoEmissionsObservableThrowsError;

            public org.junit.runners.model.Statement emptyObservable;

            public org.junit.runners.model.Statement repeatObservableThrowsError;

            public org.junit.runners.model.Statement shouldUseUnsafeSubscribeInternallyNotSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.justSingleItemObservable = _ClassStatement.forPayload(FlowableToSingleTest::justSingleItemObservable, "justSingleItemObservable", this);
            this.payloads.errorObservable = _ClassStatement.forPayload(FlowableToSingleTest::errorObservable, "errorObservable", this);
            this.payloads.justTwoEmissionsObservableThrowsError = _ClassStatement.forPayload(FlowableToSingleTest::justTwoEmissionsObservableThrowsError, "justTwoEmissionsObservableThrowsError", this);
            this.payloads.emptyObservable = _ClassStatement.forPayload(FlowableToSingleTest::emptyObservable, "emptyObservable", this);
            this.payloads.repeatObservableThrowsError = _ClassStatement.forPayload(FlowableToSingleTest::repeatObservableThrowsError, "repeatObservableThrowsError", this);
            this.payloads.shouldUseUnsafeSubscribeInternallyNotSubscribe = _ClassStatement.forPayload(FlowableToSingleTest::shouldUseUnsafeSubscribeInternallyNotSubscribe, "shouldUseUnsafeSubscribeInternallyNotSubscribe", this);
        }
    }
}
