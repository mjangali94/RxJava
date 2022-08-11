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
package io.reactivex.rxjava3.internal.operators.single;

import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleOfTypeTest extends RxJavaTest {

    @Test
    public void normal() {
        Single.just(1).ofType(Integer.class).test().assertResult(1);
    }

    @Test
    public void normalDowncast() {
        TestObserver<Number> to = Single.just(1).ofType(Number.class).test();
        // don't make this fluent, target type required!
        to.assertResult((Number) 1);
    }

    @Test
    public void notInstance() {
        TestObserver<String> to = Single.just(1).ofType(String.class).test();
        // don't make this fluent, target type required!
        to.assertResult();
    }

    @Test
    public void error() {
        TestObserver<Number> to = Single.<Integer>error(new TestException()).ofType(Number.class).test();
        // don't make this fluent, target type required!
        to.assertFailure(TestException.class);
    }

    @Test
    public void errorNotInstance() {
        TestObserver<String> to = Single.<Integer>error(new TestException()).ofType(String.class).test();
        // don't make this fluent, target type required!
        to.assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposedSingleToMaybe(new Function<Single<Object>, Maybe<Object>>() {

            @Override
            public Maybe<Object> apply(Single<Object> m) throws Exception {
                return m.ofType(Object.class);
            }
        });
    }

    @Test
    public void isDisposed() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestHelper.checkDisposed(pp.singleElement().ofType(Object.class));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingleToMaybe(new Function<Single<Object>, Maybe<Object>>() {

            @Override
            public Maybe<Object> apply(Single<Object> f) throws Exception {
                return f.ofType(Object.class);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleOfTypeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDowncast() throws java.lang.Throwable {
            this.payloads.normalDowncast.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notInstance() throws java.lang.Throwable {
            this.payloads.notInstance.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNotInstance() throws java.lang.Throwable {
            this.payloads.errorNotInstance.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposed() throws java.lang.Throwable {
            this.payloads.isDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleOfTypeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleOfTypeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleOfTypeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleOfTypeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleOfTypeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleOfTypeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleOfTypeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleOfTypeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement normalDowncast;

            public org.junit.runners.model.Statement notInstance;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement errorNotInstance;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement isDisposed;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(SingleOfTypeTest::normal, "normal", this);
            this.payloads.normalDowncast = _ClassStatement.forPayload(SingleOfTypeTest::normalDowncast, "normalDowncast", this);
            this.payloads.notInstance = _ClassStatement.forPayload(SingleOfTypeTest::notInstance, "notInstance", this);
            this.payloads.error = _ClassStatement.forPayload(SingleOfTypeTest::error, "error", this);
            this.payloads.errorNotInstance = _ClassStatement.forPayload(SingleOfTypeTest::errorNotInstance, "errorNotInstance", this);
            this.payloads.dispose = _ClassStatement.forPayload(SingleOfTypeTest::dispose, "dispose", this);
            this.payloads.isDisposed = _ClassStatement.forPayload(SingleOfTypeTest::isDisposed, "isDisposed", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(SingleOfTypeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
