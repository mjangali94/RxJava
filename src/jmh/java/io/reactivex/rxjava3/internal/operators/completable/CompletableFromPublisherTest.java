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
package io.reactivex.rxjava3.internal.operators.completable;

import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableFromPublisherTest extends RxJavaTest {

    @Test
    public void fromPublisher() {
        Completable.fromPublisher(Flowable.just(1)).test().assertResult();
    }

    @Test
    public void fromPublisherEmpty() {
        Completable.fromPublisher(Flowable.empty()).test().assertResult();
    }

    @Test
    public void fromPublisherThrows() {
        Completable.fromPublisher(Flowable.error(new UnsupportedOperationException())).test().assertFailure(UnsupportedOperationException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Completable.fromPublisher(Flowable.just(1)));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToCompletable(new Function<Flowable<Object>, Completable>() {

            @Override
            public Completable apply(Flowable<Object> f) throws Exception {
                return Completable.fromPublisher(f);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private CompletableFromPublisherTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromPublisher() throws java.lang.Throwable {
            this.payloads.fromPublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromPublisherEmpty() throws java.lang.Throwable {
            this.payloads.fromPublisherEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromPublisherThrows() throws java.lang.Throwable {
            this.payloads.fromPublisherThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromPublisherTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromPublisherTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromPublisherTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromPublisherTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableFromPublisherTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromPublisherTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableFromPublisherTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableFromPublisherTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement fromPublisher;

            public org.junit.runners.model.Statement fromPublisherEmpty;

            public org.junit.runners.model.Statement fromPublisherThrows;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.fromPublisher = _ClassStatement.forPayload(CompletableFromPublisherTest::fromPublisher, "fromPublisher", this);
            this.payloads.fromPublisherEmpty = _ClassStatement.forPayload(CompletableFromPublisherTest::fromPublisherEmpty, "fromPublisherEmpty", this);
            this.payloads.fromPublisherThrows = _ClassStatement.forPayload(CompletableFromPublisherTest::fromPublisherThrows, "fromPublisherThrows", this);
            this.payloads.dispose = _ClassStatement.forPayload(CompletableFromPublisherTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(CompletableFromPublisherTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
