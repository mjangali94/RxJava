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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableDefaultIfEmptyTest extends RxJavaTest {

    @Test
    public void defaultIfEmpty() {
        Observable<Integer> source = Observable.just(1, 2, 3);
        Observable<Integer> observable = source.defaultIfEmpty(10);
        Observer<Integer> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, never()).onNext(10);
        verify(observer).onNext(1);
        verify(observer).onNext(2);
        verify(observer).onNext(3);
        verify(observer).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void defaultIfEmptyWithEmpty() {
        Observable<Integer> source = Observable.empty();
        Observable<Integer> observable = source.defaultIfEmpty(10);
        Observer<Integer> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer).onNext(10);
        verify(observer).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableDefaultIfEmptyTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_defaultIfEmpty() throws java.lang.Throwable {
            this.payloads.defaultIfEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_defaultIfEmptyWithEmpty() throws java.lang.Throwable {
            this.payloads.defaultIfEmptyWithEmpty.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDefaultIfEmptyTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDefaultIfEmptyTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDefaultIfEmptyTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDefaultIfEmptyTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableDefaultIfEmptyTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDefaultIfEmptyTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableDefaultIfEmptyTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableDefaultIfEmptyTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement defaultIfEmpty;

            public org.junit.runners.model.Statement defaultIfEmptyWithEmpty;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.defaultIfEmpty = _ClassStatement.forPayload(ObservableDefaultIfEmptyTest::defaultIfEmpty, "defaultIfEmpty", this);
            this.payloads.defaultIfEmptyWithEmpty = _ClassStatement.forPayload(ObservableDefaultIfEmptyTest::defaultIfEmptyWithEmpty, "defaultIfEmptyWithEmpty", this);
        }
    }
}
