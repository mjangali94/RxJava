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
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleConcatMapMaybeTest extends RxJavaTest {

    @Test
    public void concatMapMaybeValue() {
        Single.just(1).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(final Integer integer) throws Exception {
                if (integer == 1) {
                    return Maybe.just(2);
                }
                return Maybe.just(1);
            }
        }).test().assertResult(2);
    }

    @Test
    public void concatMapMaybeValueDifferentType() {
        Single.just(1).concatMapMaybe(new Function<Integer, MaybeSource<String>>() {

            @Override
            public MaybeSource<String> apply(final Integer integer) throws Exception {
                if (integer == 1) {
                    return Maybe.just("2");
                }
                return Maybe.just("1");
            }
        }).test().assertResult("2");
    }

    @Test
    public void concatMapMaybeValueNull() {
        Single.just(1).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(final Integer integer) throws Exception {
                return null;
            }
        }).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertError(NullPointerException.class).assertErrorMessage("The mapper returned a null MaybeSource");
    }

    @Test
    public void concatMapMaybeValueErrorThrown() {
        Single.just(1).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(final Integer integer) throws Exception {
                throw new RuntimeException("something went terribly wrong!");
            }
        }).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertError(RuntimeException.class).assertErrorMessage("something went terribly wrong!");
    }

    @Test
    public void concatMapMaybeError() {
        RuntimeException exception = new RuntimeException("test");
        Single.error(exception).concatMapMaybe(new Function<Object, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(final Object integer) throws Exception {
                return Maybe.just(new Object());
            }
        }).test().assertError(exception);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Single.just(1).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(1);
            }
        }));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingleToMaybe(new Function<Single<Integer>, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Single<Integer> v) throws Exception {
                return v.concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

                    @Override
                    public MaybeSource<Integer> apply(Integer v) throws Exception {
                        return Maybe.just(1);
                    }
                });
            }
        });
    }

    @Test
    public void mapsToError() {
        Single.just(1).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void mapsToEmpty() {
        Single.just(1).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.empty();
            }
        }).test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public SingleConcatMapMaybeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapMaybeValue() throws java.lang.Throwable {
            this.payloads.concatMapMaybeValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapMaybeValueDifferentType() throws java.lang.Throwable {
            this.payloads.concatMapMaybeValueDifferentType.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapMaybeValueNull() throws java.lang.Throwable {
            this.payloads.concatMapMaybeValueNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapMaybeValueErrorThrown() throws java.lang.Throwable {
            this.payloads.concatMapMaybeValueErrorThrown.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapMaybeError() throws java.lang.Throwable {
            this.payloads.concatMapMaybeError.evaluate();
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
        public void benchmark_mapsToError() throws java.lang.Throwable {
            this.payloads.mapsToError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapsToEmpty() throws java.lang.Throwable {
            this.payloads.mapsToEmpty.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatMapMaybeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatMapMaybeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatMapMaybeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatMapMaybeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleConcatMapMaybeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatMapMaybeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleConcatMapMaybeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleConcatMapMaybeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement concatMapMaybeValue;

            public org.junit.runners.model.Statement concatMapMaybeValueDifferentType;

            public org.junit.runners.model.Statement concatMapMaybeValueNull;

            public org.junit.runners.model.Statement concatMapMaybeValueErrorThrown;

            public org.junit.runners.model.Statement concatMapMaybeError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement mapsToError;

            public org.junit.runners.model.Statement mapsToEmpty;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.concatMapMaybeValue = _ClassStatement.forPayload(SingleConcatMapMaybeTest::concatMapMaybeValue, "concatMapMaybeValue", this);
            this.payloads.concatMapMaybeValueDifferentType = _ClassStatement.forPayload(SingleConcatMapMaybeTest::concatMapMaybeValueDifferentType, "concatMapMaybeValueDifferentType", this);
            this.payloads.concatMapMaybeValueNull = _ClassStatement.forPayload(SingleConcatMapMaybeTest::concatMapMaybeValueNull, "concatMapMaybeValueNull", this);
            this.payloads.concatMapMaybeValueErrorThrown = _ClassStatement.forPayload(SingleConcatMapMaybeTest::concatMapMaybeValueErrorThrown, "concatMapMaybeValueErrorThrown", this);
            this.payloads.concatMapMaybeError = _ClassStatement.forPayload(SingleConcatMapMaybeTest::concatMapMaybeError, "concatMapMaybeError", this);
            this.payloads.dispose = _ClassStatement.forPayload(SingleConcatMapMaybeTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(SingleConcatMapMaybeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.mapsToError = _ClassStatement.forPayload(SingleConcatMapMaybeTest::mapsToError, "mapsToError", this);
            this.payloads.mapsToEmpty = _ClassStatement.forPayload(SingleConcatMapMaybeTest::mapsToEmpty, "mapsToEmpty", this);
        }
    }
}
