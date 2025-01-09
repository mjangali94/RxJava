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
package io.reactivex.rxjava3.internal;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

public class SubscribeWithTest extends RxJavaTest {

    @Test
    public void withFlowable() {
        Flowable.range(1, 10).subscribeWith(new TestSubscriber<>()).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void withObservable() {
        Observable.range(1, 10).subscribeWith(new TestObserver<>()).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    class ObserverImpl implements SingleObserver<Object>, CompletableObserver {

        Object value;

        @Override
        public void onSubscribe(Disposable d) {
        }

        @Override
        public void onComplete() {
            this.value = 100;
        }

        @Override
        public void onSuccess(Object value) {
            this.value = value;
        }

        @Override
        public void onError(Throwable e) {
            this.value = e;
        }
    }

    @Test
    public void withSingle() {
        assertEquals(1, Single.just(1).subscribeWith(new ObserverImpl()).value);
    }

    @Test
    public void withCompletable() {
        assertEquals(100, Completable.complete().subscribeWith(new ObserverImpl()).value);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public SubscribeWithTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFlowable() throws java.lang.Throwable {
            this.payloads.withFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withObservable() throws java.lang.Throwable {
            this.payloads.withObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withSingle() throws java.lang.Throwable {
            this.payloads.withSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withCompletable() throws java.lang.Throwable {
            this.payloads.withCompletable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SubscribeWithTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SubscribeWithTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SubscribeWithTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SubscribeWithTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SubscribeWithTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SubscribeWithTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SubscribeWithTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SubscribeWithTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement withFlowable;

            public org.junit.runners.model.Statement withObservable;

            public org.junit.runners.model.Statement withSingle;

            public org.junit.runners.model.Statement withCompletable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.withFlowable = _ClassStatement.forPayload(SubscribeWithTest::withFlowable, "withFlowable", this);
            this.payloads.withObservable = _ClassStatement.forPayload(SubscribeWithTest::withObservable, "withObservable", this);
            this.payloads.withSingle = _ClassStatement.forPayload(SubscribeWithTest::withSingle, "withSingle", this);
            this.payloads.withCompletable = _ClassStatement.forPayload(SubscribeWithTest::withCompletable, "withCompletable", this);
        }
    }
}
