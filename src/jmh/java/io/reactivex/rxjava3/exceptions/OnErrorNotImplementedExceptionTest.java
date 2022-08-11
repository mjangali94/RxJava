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
package io.reactivex.rxjava3.exceptions;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class OnErrorNotImplementedExceptionTest extends RxJavaTest {

    List<Throwable> errors;

    @Before
    public void before() {
        errors = TestHelper.trackPluginErrors();
    }

    @After
    public void after() {
        RxJavaPlugins.reset();
        assertFalse("" + errors, errors.isEmpty());
        TestHelper.assertError(errors, 0, OnErrorNotImplementedException.class);
        Throwable c = errors.get(0).getCause();
        assertTrue("" + c, c instanceof TestException);
    }

    @Test
    public void flowableSubscribe0() {
        Flowable.error(new TestException()).subscribe();
    }

    @Test
    public void flowableSubscribe1() {
        Flowable.error(new TestException()).subscribe(Functions.emptyConsumer());
    }

    @Test
    public void flowableForEachWhile() {
        Flowable.error(new TestException()).forEachWhile(Functions.alwaysTrue());
    }

    @Test
    public void flowableBlockingSubscribe1() {
        Flowable.error(new TestException()).blockingSubscribe(Functions.emptyConsumer());
    }

    @Test
    public void flowableBoundedBlockingSubscribe1() {
        Flowable.error(new TestException()).blockingSubscribe(Functions.emptyConsumer(), 128);
    }

    @Test
    public void observableSubscribe0() {
        Observable.error(new TestException()).subscribe();
    }

    @Test
    public void observableSubscribe1() {
        Observable.error(new TestException()).subscribe(Functions.emptyConsumer());
    }

    @Test
    public void observableForEachWhile() {
        Observable.error(new TestException()).forEachWhile(Functions.alwaysTrue());
    }

    @Test
    public void observableBlockingSubscribe1() {
        Observable.error(new TestException()).blockingSubscribe(Functions.emptyConsumer());
    }

    @Test
    public void singleSubscribe0() {
        Single.error(new TestException()).subscribe();
    }

    @Test
    public void singleSubscribe1() {
        Single.error(new TestException()).subscribe(Functions.emptyConsumer());
    }

    @Test
    public void maybeSubscribe0() {
        Maybe.error(new TestException()).subscribe();
    }

    @Test
    public void maybeSubscribe1() {
        Maybe.error(new TestException()).subscribe(Functions.emptyConsumer());
    }

    @Test
    public void completableSubscribe0() {
        Completable.error(new TestException()).subscribe();
    }

    @Test
    public void completableSubscribe1() {
        Completable.error(new TestException()).subscribe(Functions.EMPTY_ACTION);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private OnErrorNotImplementedExceptionTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableSubscribe0() throws java.lang.Throwable {
            this.payloads.flowableSubscribe0.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableSubscribe1() throws java.lang.Throwable {
            this.payloads.flowableSubscribe1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableForEachWhile() throws java.lang.Throwable {
            this.payloads.flowableForEachWhile.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableBlockingSubscribe1() throws java.lang.Throwable {
            this.payloads.flowableBlockingSubscribe1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableBoundedBlockingSubscribe1() throws java.lang.Throwable {
            this.payloads.flowableBoundedBlockingSubscribe1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableSubscribe0() throws java.lang.Throwable {
            this.payloads.observableSubscribe0.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableSubscribe1() throws java.lang.Throwable {
            this.payloads.observableSubscribe1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableForEachWhile() throws java.lang.Throwable {
            this.payloads.observableForEachWhile.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableBlockingSubscribe1() throws java.lang.Throwable {
            this.payloads.observableBlockingSubscribe1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSubscribe0() throws java.lang.Throwable {
            this.payloads.singleSubscribe0.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSubscribe1() throws java.lang.Throwable {
            this.payloads.singleSubscribe1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeSubscribe0() throws java.lang.Throwable {
            this.payloads.maybeSubscribe0.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeSubscribe1() throws java.lang.Throwable {
            this.payloads.maybeSubscribe1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableSubscribe0() throws java.lang.Throwable {
            this.payloads.completableSubscribe0.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableSubscribe1() throws java.lang.Throwable {
            this.payloads.completableSubscribe1.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<OnErrorNotImplementedExceptionTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<OnErrorNotImplementedExceptionTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.before();
                try {
                    this.payload.accept(this.benchmark.instance);
                } finally {
                    this.benchmark.instance.after();
                }
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<OnErrorNotImplementedExceptionTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<OnErrorNotImplementedExceptionTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new OnErrorNotImplementedExceptionTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<OnErrorNotImplementedExceptionTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(OnErrorNotImplementedExceptionTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(OnErrorNotImplementedExceptionTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement flowableSubscribe0;

            public org.junit.runners.model.Statement flowableSubscribe1;

            public org.junit.runners.model.Statement flowableForEachWhile;

            public org.junit.runners.model.Statement flowableBlockingSubscribe1;

            public org.junit.runners.model.Statement flowableBoundedBlockingSubscribe1;

            public org.junit.runners.model.Statement observableSubscribe0;

            public org.junit.runners.model.Statement observableSubscribe1;

            public org.junit.runners.model.Statement observableForEachWhile;

            public org.junit.runners.model.Statement observableBlockingSubscribe1;

            public org.junit.runners.model.Statement singleSubscribe0;

            public org.junit.runners.model.Statement singleSubscribe1;

            public org.junit.runners.model.Statement maybeSubscribe0;

            public org.junit.runners.model.Statement maybeSubscribe1;

            public org.junit.runners.model.Statement completableSubscribe0;

            public org.junit.runners.model.Statement completableSubscribe1;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.flowableSubscribe0 = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::flowableSubscribe0, "flowableSubscribe0", this);
            this.payloads.flowableSubscribe1 = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::flowableSubscribe1, "flowableSubscribe1", this);
            this.payloads.flowableForEachWhile = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::flowableForEachWhile, "flowableForEachWhile", this);
            this.payloads.flowableBlockingSubscribe1 = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::flowableBlockingSubscribe1, "flowableBlockingSubscribe1", this);
            this.payloads.flowableBoundedBlockingSubscribe1 = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::flowableBoundedBlockingSubscribe1, "flowableBoundedBlockingSubscribe1", this);
            this.payloads.observableSubscribe0 = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::observableSubscribe0, "observableSubscribe0", this);
            this.payloads.observableSubscribe1 = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::observableSubscribe1, "observableSubscribe1", this);
            this.payloads.observableForEachWhile = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::observableForEachWhile, "observableForEachWhile", this);
            this.payloads.observableBlockingSubscribe1 = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::observableBlockingSubscribe1, "observableBlockingSubscribe1", this);
            this.payloads.singleSubscribe0 = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::singleSubscribe0, "singleSubscribe0", this);
            this.payloads.singleSubscribe1 = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::singleSubscribe1, "singleSubscribe1", this);
            this.payloads.maybeSubscribe0 = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::maybeSubscribe0, "maybeSubscribe0", this);
            this.payloads.maybeSubscribe1 = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::maybeSubscribe1, "maybeSubscribe1", this);
            this.payloads.completableSubscribe0 = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::completableSubscribe0, "completableSubscribe0", this);
            this.payloads.completableSubscribe1 = _ClassStatement.forPayload(OnErrorNotImplementedExceptionTest::completableSubscribe1, "completableSubscribe1", this);
        }
    }
}
