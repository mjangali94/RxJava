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

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleDoAfterSuccessTest extends RxJavaTest {

    final List<Integer> values = new ArrayList<>();

    final Consumer<Integer> afterSuccess = new Consumer<Integer>() {

        @Override
        public void accept(Integer e) throws Exception {
            values.add(-e);
        }
    };

    final TestObserver<Integer> to = new TestObserver<Integer>() {

        @Override
        public void onNext(Integer t) {
            super.onNext(t);
            SingleDoAfterSuccessTest.this.values.add(t);
        }
    };

    @Test
    public void just() {
        Single.just(1).doAfterSuccess(afterSuccess).subscribeWith(to).assertResult(1);
        assertEquals(Arrays.asList(1, -1), values);
    }

    @Test
    public void error() {
        Single.<Integer>error(new TestException()).doAfterSuccess(afterSuccess).subscribeWith(to).assertFailure(TestException.class);
        assertTrue(values.isEmpty());
    }

    @Test
    public void justConditional() {
        Single.just(1).doAfterSuccess(afterSuccess).filter(Functions.alwaysTrue()).subscribeWith(to).assertResult(1);
        assertEquals(Arrays.asList(1, -1), values);
    }

    @Test
    public void errorConditional() {
        Single.<Integer>error(new TestException()).doAfterSuccess(afterSuccess).filter(Functions.alwaysTrue()).subscribeWith(to).assertFailure(TestException.class);
        assertTrue(values.isEmpty());
    }

    @Test
    public void consumerThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Single.just(1).doAfterSuccess(new Consumer<Integer>() {

                @Override
                public void accept(Integer e) throws Exception {
                    throw new TestException();
                }
            }).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.<Integer>create().singleOrError().doAfterSuccess(afterSuccess));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingle(new Function<Single<Integer>, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Single<Integer> m) throws Exception {
                return m.doAfterSuccess(afterSuccess);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleDoAfterSuccessTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.payloads.just.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justConditional() throws java.lang.Throwable {
            this.payloads.justConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorConditional() throws java.lang.Throwable {
            this.payloads.errorConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_consumerThrows() throws java.lang.Throwable {
            this.payloads.consumerThrows.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoAfterSuccessTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoAfterSuccessTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoAfterSuccessTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoAfterSuccessTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleDoAfterSuccessTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoAfterSuccessTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleDoAfterSuccessTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleDoAfterSuccessTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement just;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement justConditional;

            public org.junit.runners.model.Statement errorConditional;

            public org.junit.runners.model.Statement consumerThrows;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.just = _ClassStatement.forPayload(SingleDoAfterSuccessTest::just, "just", this);
            this.payloads.error = _ClassStatement.forPayload(SingleDoAfterSuccessTest::error, "error", this);
            this.payloads.justConditional = _ClassStatement.forPayload(SingleDoAfterSuccessTest::justConditional, "justConditional", this);
            this.payloads.errorConditional = _ClassStatement.forPayload(SingleDoAfterSuccessTest::errorConditional, "errorConditional", this);
            this.payloads.consumerThrows = _ClassStatement.forPayload(SingleDoAfterSuccessTest::consumerThrows, "consumerThrows", this);
            this.payloads.dispose = _ClassStatement.forPayload(SingleDoAfterSuccessTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(SingleDoAfterSuccessTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
