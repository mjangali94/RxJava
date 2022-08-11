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
package io.reactivex.rxjava3.internal.operators.maybe;

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeDoFinallyTest extends RxJavaTest implements Action {

    int calls;

    @Override
    public void run() throws Exception {
        calls++;
    }

    @Test
    public void normalJust() {
        Maybe.just(1).doFinally(this).test().assertResult(1);
        assertEquals(1, calls);
    }

    @Test
    public void normalEmpty() {
        Maybe.empty().doFinally(this).test().assertResult();
        assertEquals(1, calls);
    }

    @Test
    public void normalError() {
        Maybe.error(new TestException()).doFinally(this).test().assertFailure(TestException.class);
        assertEquals(1, calls);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, Maybe<Object>>() {

            @Override
            public Maybe<Object> apply(Maybe<Object> f) throws Exception {
                return f.doFinally(MaybeDoFinallyTest.this);
            }
        });
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, Maybe<Object>>() {

            @Override
            public Maybe<Object> apply(Maybe<Object> f) throws Exception {
                return f.doFinally(MaybeDoFinallyTest.this).filter(Functions.alwaysTrue());
            }
        });
    }

    @Test
    public void normalJustConditional() {
        Maybe.just(1).doFinally(this).filter(Functions.alwaysTrue()).test().assertResult(1);
        assertEquals(1, calls);
    }

    @Test
    public void normalEmptyConditional() {
        Maybe.empty().doFinally(this).filter(Functions.alwaysTrue()).test().assertResult();
        assertEquals(1, calls);
    }

    @Test
    public void normalErrorConditional() {
        Maybe.error(new TestException()).doFinally(this).filter(Functions.alwaysTrue()).test().assertFailure(TestException.class);
        assertEquals(1, calls);
    }

    @Test
    public void actionThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Maybe.just(1).doFinally(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).test().assertResult(1).dispose();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void actionThrowsConditional() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Maybe.just(1).doFinally(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).filter(Functions.alwaysTrue()).test().assertResult(1).dispose();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishSubject.create().singleElement().doFinally(this));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeDoFinallyTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalJust() throws java.lang.Throwable {
            this.payloads.normalJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmpty() throws java.lang.Throwable {
            this.payloads.normalEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalError() throws java.lang.Throwable {
            this.payloads.normalError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalJustConditional() throws java.lang.Throwable {
            this.payloads.normalJustConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmptyConditional() throws java.lang.Throwable {
            this.payloads.normalEmptyConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalErrorConditional() throws java.lang.Throwable {
            this.payloads.normalErrorConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_actionThrows() throws java.lang.Throwable {
            this.payloads.actionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_actionThrowsConditional() throws java.lang.Throwable {
            this.payloads.actionThrowsConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDoFinallyTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDoFinallyTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDoFinallyTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDoFinallyTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeDoFinallyTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDoFinallyTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeDoFinallyTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeDoFinallyTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normalJust;

            public org.junit.runners.model.Statement normalEmpty;

            public org.junit.runners.model.Statement normalError;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement normalJustConditional;

            public org.junit.runners.model.Statement normalEmptyConditional;

            public org.junit.runners.model.Statement normalErrorConditional;

            public org.junit.runners.model.Statement actionThrows;

            public org.junit.runners.model.Statement actionThrowsConditional;

            public org.junit.runners.model.Statement disposed;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normalJust = _ClassStatement.forPayload(MaybeDoFinallyTest::normalJust, "normalJust", this);
            this.payloads.normalEmpty = _ClassStatement.forPayload(MaybeDoFinallyTest::normalEmpty, "normalEmpty", this);
            this.payloads.normalError = _ClassStatement.forPayload(MaybeDoFinallyTest::normalError, "normalError", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(MaybeDoFinallyTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.normalJustConditional = _ClassStatement.forPayload(MaybeDoFinallyTest::normalJustConditional, "normalJustConditional", this);
            this.payloads.normalEmptyConditional = _ClassStatement.forPayload(MaybeDoFinallyTest::normalEmptyConditional, "normalEmptyConditional", this);
            this.payloads.normalErrorConditional = _ClassStatement.forPayload(MaybeDoFinallyTest::normalErrorConditional, "normalErrorConditional", this);
            this.payloads.actionThrows = _ClassStatement.forPayload(MaybeDoFinallyTest::actionThrows, "actionThrows", this);
            this.payloads.actionThrowsConditional = _ClassStatement.forPayload(MaybeDoFinallyTest::actionThrowsConditional, "actionThrowsConditional", this);
            this.payloads.disposed = _ClassStatement.forPayload(MaybeDoFinallyTest::disposed, "disposed", this);
        }
    }
}
