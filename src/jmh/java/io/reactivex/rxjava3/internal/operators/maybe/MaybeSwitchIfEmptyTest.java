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

import static org.junit.Assert.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeSwitchIfEmptyTest extends RxJavaTest {

    @Test
    public void nonEmpty() {
        Maybe.just(1).switchIfEmpty(Maybe.just(2)).test().assertResult(1);
    }

    @Test
    public void empty() {
        Maybe.<Integer>empty().switchIfEmpty(Maybe.just(2)).test().assertResult(2);
    }

    @Test
    public void defaultIfEmptyNonEmpty() {
        Maybe.just(1).defaultIfEmpty(2).test().assertResult(1);
    }

    @Test
    public void defaultIfEmptyEmpty() {
        Maybe.<Integer>empty().defaultIfEmpty(2).test().assertResult(2);
    }

    @Test
    public void error() {
        Maybe.<Integer>error(new TestException()).switchIfEmpty(Maybe.just(2)).test().assertFailure(TestException.class);
    }

    @Test
    public void errorOther() {
        Maybe.empty().switchIfEmpty(Maybe.<Integer>error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void emptyOtherToo() {
        Maybe.empty().switchIfEmpty(Maybe.empty()).test().assertResult();
    }

    @Test
    public void dispose() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Integer> to = pp.singleElement().switchIfEmpty(Maybe.just(2)).test();
        assertTrue(pp.hasSubscribers());
        to.dispose();
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void isDisposed() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestHelper.checkDisposed(pp.singleElement().switchIfEmpty(Maybe.just(2)));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Integer>, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Maybe<Integer> f) throws Exception {
                return f.switchIfEmpty(Maybe.just(2));
            }
        });
    }

    @Test
    public void emptyCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final TestObserver<Integer> to = pp.singleElement().switchIfEmpty(Maybe.just(2)).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeSwitchIfEmptyTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonEmpty() throws java.lang.Throwable {
            this.payloads.nonEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_defaultIfEmptyNonEmpty() throws java.lang.Throwable {
            this.payloads.defaultIfEmptyNonEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_defaultIfEmptyEmpty() throws java.lang.Throwable {
            this.payloads.defaultIfEmptyEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorOther() throws java.lang.Throwable {
            this.payloads.errorOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyOtherToo() throws java.lang.Throwable {
            this.payloads.emptyOtherToo.evaluate();
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

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyCancelRace() throws java.lang.Throwable {
            this.payloads.emptyCancelRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeSwitchIfEmptyTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeSwitchIfEmptyTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeSwitchIfEmptyTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeSwitchIfEmptyTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeSwitchIfEmptyTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeSwitchIfEmptyTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeSwitchIfEmptyTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeSwitchIfEmptyTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement nonEmpty;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement defaultIfEmptyNonEmpty;

            public org.junit.runners.model.Statement defaultIfEmptyEmpty;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement errorOther;

            public org.junit.runners.model.Statement emptyOtherToo;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement isDisposed;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement emptyCancelRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.nonEmpty = _ClassStatement.forPayload(MaybeSwitchIfEmptyTest::nonEmpty, "nonEmpty", this);
            this.payloads.empty = _ClassStatement.forPayload(MaybeSwitchIfEmptyTest::empty, "empty", this);
            this.payloads.defaultIfEmptyNonEmpty = _ClassStatement.forPayload(MaybeSwitchIfEmptyTest::defaultIfEmptyNonEmpty, "defaultIfEmptyNonEmpty", this);
            this.payloads.defaultIfEmptyEmpty = _ClassStatement.forPayload(MaybeSwitchIfEmptyTest::defaultIfEmptyEmpty, "defaultIfEmptyEmpty", this);
            this.payloads.error = _ClassStatement.forPayload(MaybeSwitchIfEmptyTest::error, "error", this);
            this.payloads.errorOther = _ClassStatement.forPayload(MaybeSwitchIfEmptyTest::errorOther, "errorOther", this);
            this.payloads.emptyOtherToo = _ClassStatement.forPayload(MaybeSwitchIfEmptyTest::emptyOtherToo, "emptyOtherToo", this);
            this.payloads.dispose = _ClassStatement.forPayload(MaybeSwitchIfEmptyTest::dispose, "dispose", this);
            this.payloads.isDisposed = _ClassStatement.forPayload(MaybeSwitchIfEmptyTest::isDisposed, "isDisposed", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(MaybeSwitchIfEmptyTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.emptyCancelRace = _ClassStatement.forPayload(MaybeSwitchIfEmptyTest::emptyCancelRace, "emptyCancelRace", this);
        }
    }
}
