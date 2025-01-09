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
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeFlatMapBiSelectorTest extends RxJavaTest {

    BiFunction<Integer, Integer, String> stringCombine() {
        return new BiFunction<Integer, Integer, String>() {

            @Override
            public String apply(Integer a, Integer b) throws Exception {
                return a + ":" + b;
            }
        };
    }

    @Test
    public void normal() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(2);
            }
        }, stringCombine()).test().assertResult("1:2");
    }

    @Test
    public void normalWithEmpty() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.empty();
            }
        }, stringCombine()).test().assertResult();
    }

    @Test
    public void emptyWithJust() {
        final int[] call = { 0 };
        Maybe.<Integer>empty().flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                call[0]++;
                return Maybe.just(1);
            }
        }, stringCombine()).test().assertResult();
        assertEquals(0, call[0]);
    }

    @Test
    public void errorWithJust() {
        final int[] call = { 0 };
        Maybe.<Integer>error(new TestException()).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                call[0]++;
                return Maybe.just(1);
            }
        }, stringCombine()).test().assertFailure(TestException.class);
        assertEquals(0, call[0]);
    }

    @Test
    public void justWithError() {
        final int[] call = { 0 };
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                call[0]++;
                return Maybe.<Integer>error(new TestException());
            }
        }, stringCombine()).test().assertFailure(TestException.class);
        assertEquals(1, call[0]);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().singleElement().flatMap(new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.just(1);
            }
        }, new BiFunction<Object, Integer, Object>() {

            @Override
            public Object apply(Object a, Integer b) throws Exception {
                return b;
            }
        }));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Maybe<Object> v) throws Exception {
                return v.flatMap(new Function<Object, MaybeSource<Integer>>() {

                    @Override
                    public MaybeSource<Integer> apply(Object v) throws Exception {
                        return Maybe.just(1);
                    }
                }, new BiFunction<Object, Integer, Object>() {

                    @Override
                    public Object apply(Object a, Integer b) throws Exception {
                        return b;
                    }
                });
            }
        });
    }

    @Test
    public void mapperThrows() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }, stringCombine()).test().assertFailure(TestException.class);
    }

    @Test
    public void mapperReturnsNull() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return null;
            }
        }, stringCombine()).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void resultSelectorThrows() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(2);
            }
        }, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void resultSelectorReturnsNull() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(2);
            }
        }, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void mapperCancels() {
        final TestObserver<Integer> to = new TestObserver<>();
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                to.dispose();
                return Maybe.just(2);
            }
        }, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                throw new IllegalStateException();
            }
        }).subscribeWith(to).assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeFlatMapBiSelectorTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalWithEmpty() throws java.lang.Throwable {
            this.payloads.normalWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyWithJust() throws java.lang.Throwable {
            this.payloads.emptyWithJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorWithJust() throws java.lang.Throwable {
            this.payloads.errorWithJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justWithError() throws java.lang.Throwable {
            this.payloads.justWithError.evaluate();
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
        public void benchmark_mapperThrows() throws java.lang.Throwable {
            this.payloads.mapperThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperReturnsNull() throws java.lang.Throwable {
            this.payloads.mapperReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resultSelectorThrows() throws java.lang.Throwable {
            this.payloads.resultSelectorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resultSelectorReturnsNull() throws java.lang.Throwable {
            this.payloads.resultSelectorReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperCancels() throws java.lang.Throwable {
            this.payloads.mapperCancels.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapBiSelectorTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapBiSelectorTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapBiSelectorTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapBiSelectorTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeFlatMapBiSelectorTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapBiSelectorTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeFlatMapBiSelectorTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeFlatMapBiSelectorTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement normalWithEmpty;

            public org.junit.runners.model.Statement emptyWithJust;

            public org.junit.runners.model.Statement errorWithJust;

            public org.junit.runners.model.Statement justWithError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement mapperThrows;

            public org.junit.runners.model.Statement mapperReturnsNull;

            public org.junit.runners.model.Statement resultSelectorThrows;

            public org.junit.runners.model.Statement resultSelectorReturnsNull;

            public org.junit.runners.model.Statement mapperCancels;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(MaybeFlatMapBiSelectorTest::normal, "normal", this);
            this.payloads.normalWithEmpty = _ClassStatement.forPayload(MaybeFlatMapBiSelectorTest::normalWithEmpty, "normalWithEmpty", this);
            this.payloads.emptyWithJust = _ClassStatement.forPayload(MaybeFlatMapBiSelectorTest::emptyWithJust, "emptyWithJust", this);
            this.payloads.errorWithJust = _ClassStatement.forPayload(MaybeFlatMapBiSelectorTest::errorWithJust, "errorWithJust", this);
            this.payloads.justWithError = _ClassStatement.forPayload(MaybeFlatMapBiSelectorTest::justWithError, "justWithError", this);
            this.payloads.dispose = _ClassStatement.forPayload(MaybeFlatMapBiSelectorTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(MaybeFlatMapBiSelectorTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.mapperThrows = _ClassStatement.forPayload(MaybeFlatMapBiSelectorTest::mapperThrows, "mapperThrows", this);
            this.payloads.mapperReturnsNull = _ClassStatement.forPayload(MaybeFlatMapBiSelectorTest::mapperReturnsNull, "mapperReturnsNull", this);
            this.payloads.resultSelectorThrows = _ClassStatement.forPayload(MaybeFlatMapBiSelectorTest::resultSelectorThrows, "resultSelectorThrows", this);
            this.payloads.resultSelectorReturnsNull = _ClassStatement.forPayload(MaybeFlatMapBiSelectorTest::resultSelectorReturnsNull, "resultSelectorReturnsNull", this);
            this.payloads.mapperCancels = _ClassStatement.forPayload(MaybeFlatMapBiSelectorTest::mapperCancels, "mapperCancels", this);
        }
    }
}
