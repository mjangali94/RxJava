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

import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeFlatMapSingleElementTest extends RxJavaTest {

    @Test
    public void flatMapSingleValue() {
        Maybe.just(1).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                if (integer == 1) {
                    return Single.just(2);
                }
                return Single.just(1);
            }
        }).test().assertResult(2);
    }

    @Test
    public void flatMapSingleValueDifferentType() {
        Maybe.just(1).flatMapSingle(new Function<Integer, SingleSource<String>>() {

            @Override
            public SingleSource<String> apply(final Integer integer) throws Exception {
                if (integer == 1) {
                    return Single.just("2");
                }
                return Single.just("1");
            }
        }).test().assertResult("2");
    }

    @Test
    public void flatMapSingleValueNull() {
        Maybe.just(1).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                return null;
            }
        }).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertError(NullPointerException.class).assertErrorMessage("The mapper returned a null SingleSource");
    }

    @Test
    public void flatMapSingleValueErrorThrown() {
        Maybe.just(1).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                throw new RuntimeException("something went terribly wrong!");
            }
        }).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertError(RuntimeException.class).assertErrorMessage("something went terribly wrong!");
    }

    @Test
    public void flatMapSingleError() {
        RuntimeException exception = new RuntimeException("test");
        Maybe.error(exception).flatMapSingle(new Function<Object, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(final Object integer) throws Exception {
                return Single.just(new Object());
            }
        }).test().assertError(exception);
    }

    @Test
    public void flatMapSingleEmpty() {
        Maybe.<Integer>empty().flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                return Single.just(2);
            }
        }).test().assertNoValues().assertResult();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Maybe.just(1).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                return Single.just(2);
            }
        }));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Integer>, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Maybe<Integer> m) throws Exception {
                return m.flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

                    @Override
                    public SingleSource<Integer> apply(final Integer integer) throws Exception {
                        return Single.just(2);
                    }
                });
            }
        });
    }

    @Test
    public void singleErrors() {
        Maybe.just(1).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                return Single.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeFlatMapSingleElementTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapSingleValue() throws java.lang.Throwable {
            this.payloads.flatMapSingleValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapSingleValueDifferentType() throws java.lang.Throwable {
            this.payloads.flatMapSingleValueDifferentType.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapSingleValueNull() throws java.lang.Throwable {
            this.payloads.flatMapSingleValueNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapSingleValueErrorThrown() throws java.lang.Throwable {
            this.payloads.flatMapSingleValueErrorThrown.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapSingleError() throws java.lang.Throwable {
            this.payloads.flatMapSingleError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapSingleEmpty() throws java.lang.Throwable {
            this.payloads.flatMapSingleEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleErrors() throws java.lang.Throwable {
            this.payloads.singleErrors.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapSingleElementTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapSingleElementTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapSingleElementTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapSingleElementTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeFlatMapSingleElementTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapSingleElementTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeFlatMapSingleElementTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeFlatMapSingleElementTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement flatMapSingleValue;

            public org.junit.runners.model.Statement flatMapSingleValueDifferentType;

            public org.junit.runners.model.Statement flatMapSingleValueNull;

            public org.junit.runners.model.Statement flatMapSingleValueErrorThrown;

            public org.junit.runners.model.Statement flatMapSingleError;

            public org.junit.runners.model.Statement flatMapSingleEmpty;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement singleErrors;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.flatMapSingleValue = _ClassStatement.forPayload(MaybeFlatMapSingleElementTest::flatMapSingleValue, "flatMapSingleValue", this);
            this.payloads.flatMapSingleValueDifferentType = _ClassStatement.forPayload(MaybeFlatMapSingleElementTest::flatMapSingleValueDifferentType, "flatMapSingleValueDifferentType", this);
            this.payloads.flatMapSingleValueNull = _ClassStatement.forPayload(MaybeFlatMapSingleElementTest::flatMapSingleValueNull, "flatMapSingleValueNull", this);
            this.payloads.flatMapSingleValueErrorThrown = _ClassStatement.forPayload(MaybeFlatMapSingleElementTest::flatMapSingleValueErrorThrown, "flatMapSingleValueErrorThrown", this);
            this.payloads.flatMapSingleError = _ClassStatement.forPayload(MaybeFlatMapSingleElementTest::flatMapSingleError, "flatMapSingleError", this);
            this.payloads.flatMapSingleEmpty = _ClassStatement.forPayload(MaybeFlatMapSingleElementTest::flatMapSingleEmpty, "flatMapSingleEmpty", this);
            this.payloads.dispose = _ClassStatement.forPayload(MaybeFlatMapSingleElementTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(MaybeFlatMapSingleElementTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.singleErrors = _ClassStatement.forPayload(MaybeFlatMapSingleElementTest::singleErrors, "singleErrors", this);
        }
    }
}
