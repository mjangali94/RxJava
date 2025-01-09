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
package io.reactivex.rxjava3.internal.operators.observable;

import static org.junit.Assert.*;
import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.ScalarSupplier;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableFromTest extends RxJavaTest {

    @Test
    public void fromFutureTimeout() throws Exception {
        Observable.fromFuture(Observable.never().toFuture(), 100, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io()).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TimeoutException.class);
    }

    @Test
    public void fromPublisher() {
        Observable.fromPublisher(Flowable.just(1)).test().assertResult(1);
    }

    @Test
    public void just10() {
        Observable.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void fromArrayEmpty() {
        assertSame(Observable.empty(), Observable.fromArray());
    }

    @Test
    public void fromArraySingle() {
        assertTrue(Observable.fromArray(1) instanceof ScalarSupplier);
    }

    @Test
    public void fromPublisherDispose() {
        TestHelper.checkDisposed(Flowable.just(1).toObservable());
    }

    @Test
    public void fromPublisherDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToObservable(new Function<Flowable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Flowable<Object> f) throws Exception {
                return f.toObservable();
            }
        });
    }

    @Test
    public void fusionRejected() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ASYNC);
        Observable.fromArray(1, 2, 3).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableFromTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFutureTimeout() throws java.lang.Throwable {
            this.payloads.fromFutureTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromPublisher() throws java.lang.Throwable {
            this.payloads.fromPublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just10() throws java.lang.Throwable {
            this.payloads.just10.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromArrayEmpty() throws java.lang.Throwable {
            this.payloads.fromArrayEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromArraySingle() throws java.lang.Throwable {
            this.payloads.fromArraySingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromPublisherDispose() throws java.lang.Throwable {
            this.payloads.fromPublisherDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromPublisherDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.fromPublisherDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejected() throws java.lang.Throwable {
            this.payloads.fusionRejected.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableFromTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableFromTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableFromTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement fromFutureTimeout;

            public org.junit.runners.model.Statement fromPublisher;

            public org.junit.runners.model.Statement just10;

            public org.junit.runners.model.Statement fromArrayEmpty;

            public org.junit.runners.model.Statement fromArraySingle;

            public org.junit.runners.model.Statement fromPublisherDispose;

            public org.junit.runners.model.Statement fromPublisherDoubleOnSubscribe;

            public org.junit.runners.model.Statement fusionRejected;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.fromFutureTimeout = _ClassStatement.forPayload(ObservableFromTest::fromFutureTimeout, "fromFutureTimeout", this);
            this.payloads.fromPublisher = _ClassStatement.forPayload(ObservableFromTest::fromPublisher, "fromPublisher", this);
            this.payloads.just10 = _ClassStatement.forPayload(ObservableFromTest::just10, "just10", this);
            this.payloads.fromArrayEmpty = _ClassStatement.forPayload(ObservableFromTest::fromArrayEmpty, "fromArrayEmpty", this);
            this.payloads.fromArraySingle = _ClassStatement.forPayload(ObservableFromTest::fromArraySingle, "fromArraySingle", this);
            this.payloads.fromPublisherDispose = _ClassStatement.forPayload(ObservableFromTest::fromPublisherDispose, "fromPublisherDispose", this);
            this.payloads.fromPublisherDoubleOnSubscribe = _ClassStatement.forPayload(ObservableFromTest::fromPublisherDoubleOnSubscribe, "fromPublisherDoubleOnSubscribe", this);
            this.payloads.fusionRejected = _ClassStatement.forPayload(ObservableFromTest::fusionRejected, "fusionRejected", this);
        }
    }
}
