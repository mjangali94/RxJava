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
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.testsupport.*;

public class SingleFlatMapTest extends RxJavaTest {

    @Test
    public void normal() {
        final boolean[] b = { false };
        Single.just(1).flatMapCompletable(new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer t) throws Exception {
                return Completable.complete().doOnComplete(new Action() {

                    @Override
                    public void run() throws Exception {
                        b[0] = true;
                    }
                });
            }
        }).test().assertResult();
        assertTrue(b[0]);
    }

    @Test
    public void error() {
        final boolean[] b = { false };
        Single.<Integer>error(new TestException()).flatMapCompletable(new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer t) throws Exception {
                return Completable.complete().doOnComplete(new Action() {

                    @Override
                    public void run() throws Exception {
                        b[0] = true;
                    }
                });
            }
        }).test().assertFailure(TestException.class);
        assertFalse(b[0]);
    }

    @Test
    public void mapperThrows() {
        final boolean[] b = { false };
        Single.just(1).flatMapCompletable(new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer t) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
        assertFalse(b[0]);
    }

    @Test
    public void mapperReturnsNull() {
        final boolean[] b = { false };
        Single.just(1).flatMapCompletable(new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer t) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
        assertFalse(b[0]);
    }

    @Test
    public void flatMapObservable() {
        Single.just(1).flatMapObservable(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 5);
            }
        }).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void flatMapPublisher() {
        Single.just(1).flatMapPublisher(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.range(v, 5);
            }
        }).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void flatMapPublisherMapperThrows() {
        final TestException ex = new TestException();
        Single.just(1).flatMapPublisher(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                throw ex;
            }
        }).test().assertNoValues().assertError(ex);
    }

    @Test
    public void flatMapPublisherSingleError() {
        final TestException ex = new TestException();
        Single.<Integer>error(ex).flatMapPublisher(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.just(1);
            }
        }).test().assertNoValues().assertError(ex);
    }

    @Test
    public void flatMapPublisherCancelDuringSingle() {
        final AtomicBoolean disposed = new AtomicBoolean();
        TestSubscriberEx<Integer> ts = Single.<Integer>never().doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                disposed.set(true);
            }
        }).flatMapPublisher(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.range(v, 5);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertNotTerminated();
        assertFalse(disposed.get());
        ts.cancel();
        assertTrue(disposed.get());
        ts.assertNotTerminated();
    }

    @Test
    public void flatMapPublisherCancelDuringFlowable() {
        final AtomicBoolean disposed = new AtomicBoolean();
        TestSubscriberEx<Integer> ts = Single.just(1).flatMapPublisher(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.<Integer>never().doOnCancel(new Action() {

                    @Override
                    public void run() throws Exception {
                        disposed.set(true);
                    }
                });
            }
        }).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertNotTerminated();
        assertFalse(disposed.get());
        ts.cancel();
        assertTrue(disposed.get());
        ts.assertNotTerminated();
    }

    @Test
    public void flatMapValue() {
        Single.just(1).flatMap(new Function<Integer, SingleSource<Integer>>() {

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
    public void flatMapValueDifferentType() {
        Single.just(1).flatMap(new Function<Integer, SingleSource<String>>() {

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
    public void flatMapValueNull() {
        Single.just(1).flatMap(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                return null;
            }
        }).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertError(NullPointerException.class).assertErrorMessage("The single returned by the mapper is null");
    }

    @Test
    public void flatMapValueErrorThrown() {
        Single.just(1).flatMap(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final Integer integer) throws Exception {
                throw new RuntimeException("something went terribly wrong!");
            }
        }).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertError(RuntimeException.class).assertErrorMessage("something went terribly wrong!");
    }

    @Test
    public void flatMapError() {
        RuntimeException exception = new RuntimeException("test");
        Single.error(exception).flatMap(new Function<Object, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(final Object integer) throws Exception {
                return Single.just(new Object());
            }
        }).test().assertError(exception);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Single.just(1).flatMap(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(2);
            }
        }));
    }

    @Test
    public void mappedSingleOnError() {
        Single.just(1).flatMap(new Function<Integer, SingleSource<Integer>>() {

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
                return s.flatMap(new Function<Object, SingleSource<? extends Object>>() {

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

public _Payloads payloads;

        public SingleFlatMapTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
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
        public void benchmark_flatMapObservable() throws java.lang.Throwable {
            this.payloads.flatMapObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapPublisher() throws java.lang.Throwable {
            this.payloads.flatMapPublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapPublisherMapperThrows() throws java.lang.Throwable {
            this.payloads.flatMapPublisherMapperThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapPublisherSingleError() throws java.lang.Throwable {
            this.payloads.flatMapPublisherSingleError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapPublisherCancelDuringSingle() throws java.lang.Throwable {
            this.payloads.flatMapPublisherCancelDuringSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapPublisherCancelDuringFlowable() throws java.lang.Throwable {
            this.payloads.flatMapPublisherCancelDuringFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapValue() throws java.lang.Throwable {
            this.payloads.flatMapValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapValueDifferentType() throws java.lang.Throwable {
            this.payloads.flatMapValueDifferentType.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapValueNull() throws java.lang.Throwable {
            this.payloads.flatMapValueNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapValueErrorThrown() throws java.lang.Throwable {
            this.payloads.flatMapValueErrorThrown.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapError() throws java.lang.Throwable {
            this.payloads.flatMapError.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleFlatMapTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleFlatMapTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleFlatMapTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement mapperThrows;

            public org.junit.runners.model.Statement mapperReturnsNull;

            public org.junit.runners.model.Statement flatMapObservable;

            public org.junit.runners.model.Statement flatMapPublisher;

            public org.junit.runners.model.Statement flatMapPublisherMapperThrows;

            public org.junit.runners.model.Statement flatMapPublisherSingleError;

            public org.junit.runners.model.Statement flatMapPublisherCancelDuringSingle;

            public org.junit.runners.model.Statement flatMapPublisherCancelDuringFlowable;

            public org.junit.runners.model.Statement flatMapValue;

            public org.junit.runners.model.Statement flatMapValueDifferentType;

            public org.junit.runners.model.Statement flatMapValueNull;

            public org.junit.runners.model.Statement flatMapValueErrorThrown;

            public org.junit.runners.model.Statement flatMapError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement mappedSingleOnError;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(SingleFlatMapTest::normal, "normal", this);
            this.payloads.error = _ClassStatement.forPayload(SingleFlatMapTest::error, "error", this);
            this.payloads.mapperThrows = _ClassStatement.forPayload(SingleFlatMapTest::mapperThrows, "mapperThrows", this);
            this.payloads.mapperReturnsNull = _ClassStatement.forPayload(SingleFlatMapTest::mapperReturnsNull, "mapperReturnsNull", this);
            this.payloads.flatMapObservable = _ClassStatement.forPayload(SingleFlatMapTest::flatMapObservable, "flatMapObservable", this);
            this.payloads.flatMapPublisher = _ClassStatement.forPayload(SingleFlatMapTest::flatMapPublisher, "flatMapPublisher", this);
            this.payloads.flatMapPublisherMapperThrows = _ClassStatement.forPayload(SingleFlatMapTest::flatMapPublisherMapperThrows, "flatMapPublisherMapperThrows", this);
            this.payloads.flatMapPublisherSingleError = _ClassStatement.forPayload(SingleFlatMapTest::flatMapPublisherSingleError, "flatMapPublisherSingleError", this);
            this.payloads.flatMapPublisherCancelDuringSingle = _ClassStatement.forPayload(SingleFlatMapTest::flatMapPublisherCancelDuringSingle, "flatMapPublisherCancelDuringSingle", this);
            this.payloads.flatMapPublisherCancelDuringFlowable = _ClassStatement.forPayload(SingleFlatMapTest::flatMapPublisherCancelDuringFlowable, "flatMapPublisherCancelDuringFlowable", this);
            this.payloads.flatMapValue = _ClassStatement.forPayload(SingleFlatMapTest::flatMapValue, "flatMapValue", this);
            this.payloads.flatMapValueDifferentType = _ClassStatement.forPayload(SingleFlatMapTest::flatMapValueDifferentType, "flatMapValueDifferentType", this);
            this.payloads.flatMapValueNull = _ClassStatement.forPayload(SingleFlatMapTest::flatMapValueNull, "flatMapValueNull", this);
            this.payloads.flatMapValueErrorThrown = _ClassStatement.forPayload(SingleFlatMapTest::flatMapValueErrorThrown, "flatMapValueErrorThrown", this);
            this.payloads.flatMapError = _ClassStatement.forPayload(SingleFlatMapTest::flatMapError, "flatMapError", this);
            this.payloads.dispose = _ClassStatement.forPayload(SingleFlatMapTest::dispose, "dispose", this);
            this.payloads.mappedSingleOnError = _ClassStatement.forPayload(SingleFlatMapTest::mappedSingleOnError, "mappedSingleOnError", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(SingleFlatMapTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
