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

public class SingleConcatMapTest extends RxJavaTest {

    @Test
    public void concatMapValue() {
        Single.just(1).concatMap(new Function<Integer, SingleSource<Integer>>() {

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
    public void concatMapValueDifferentType() {
        Single.just(1).concatMap(new Function<Integer, SingleSource<String>>() {

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
    public void concatMapValueNull() {
        Single.just(1).concatMap(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                return null;
            }
        }).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertError(NullPointerException.class).assertErrorMessage("The single returned by the mapper is null");
    }

    @Test
    public void concatMapValueErrorThrown() {
        Single.just(1).concatMap(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                throw new RuntimeException("something went terribly wrong!");
            }
        }).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertError(RuntimeException.class).assertErrorMessage("something went terribly wrong!");
    }

    @Test
    public void concatMapError() {
        RuntimeException exception = new RuntimeException("test");
        Single.error(exception).concatMap(new Function<Object, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(final Object integer) throws Exception {
                return Single.just(new Object());
            }
        }).test().assertError(exception);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Single.just(1).concatMap(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(2);
            }
        }));
    }

    @Test
    public void mappedSingleOnError() {
        Single.just(1).concatMap(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingle(new Function<Single<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Single<Object> s) throws Exception {
                return s.concatMap(new Function<Object, SingleSource<? extends Object>>() {

                    @Override
                    public SingleSource<? extends Object> apply(Object v) throws Exception {
                        return Single.just(v);
                    }
                });
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleConcatMapTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapValue() throws java.lang.Throwable {
            this.payloads.concatMapValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapValueDifferentType() throws java.lang.Throwable {
            this.payloads.concatMapValueDifferentType.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapValueNull() throws java.lang.Throwable {
            this.payloads.concatMapValueNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapValueErrorThrown() throws java.lang.Throwable {
            this.payloads.concatMapValueErrorThrown.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapError() throws java.lang.Throwable {
            this.payloads.concatMapError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mappedSingleOnError() throws java.lang.Throwable {
            this.payloads.mappedSingleOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatMapTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatMapTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatMapTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatMapTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleConcatMapTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatMapTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleConcatMapTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleConcatMapTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement concatMapValue;

            public org.junit.runners.model.Statement concatMapValueDifferentType;

            public org.junit.runners.model.Statement concatMapValueNull;

            public org.junit.runners.model.Statement concatMapValueErrorThrown;

            public org.junit.runners.model.Statement concatMapError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement mappedSingleOnError;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.concatMapValue = _ClassStatement.forPayload(SingleConcatMapTest::concatMapValue, "concatMapValue", this);
            this.payloads.concatMapValueDifferentType = _ClassStatement.forPayload(SingleConcatMapTest::concatMapValueDifferentType, "concatMapValueDifferentType", this);
            this.payloads.concatMapValueNull = _ClassStatement.forPayload(SingleConcatMapTest::concatMapValueNull, "concatMapValueNull", this);
            this.payloads.concatMapValueErrorThrown = _ClassStatement.forPayload(SingleConcatMapTest::concatMapValueErrorThrown, "concatMapValueErrorThrown", this);
            this.payloads.concatMapError = _ClassStatement.forPayload(SingleConcatMapTest::concatMapError, "concatMapError", this);
            this.payloads.dispose = _ClassStatement.forPayload(SingleConcatMapTest::dispose, "dispose", this);
            this.payloads.mappedSingleOnError = _ClassStatement.forPayload(SingleConcatMapTest::mappedSingleOnError, "mappedSingleOnError", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(SingleConcatMapTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
