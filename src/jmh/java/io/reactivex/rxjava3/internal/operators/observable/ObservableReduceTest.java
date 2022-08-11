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
package io.reactivex.rxjava3.internal.operators.observable;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableReduceTest extends RxJavaTest {

    Observer<Object> observer;

    SingleObserver<Object> singleObserver;

    @Before
    public void before() {
        observer = TestHelper.mockObserver();
        singleObserver = TestHelper.mockSingleObserver();
    }

    BiFunction<Integer, Integer, Integer> sum = new BiFunction<Integer, Integer, Integer>() {

        @Override
        public Integer apply(Integer t1, Integer t2) {
            return t1 + t2;
        }
    };

    @Test
    public void aggregateAsIntSumObservable() {
        Observable<Integer> result = Observable.just(1, 2, 3, 4, 5).reduce(0, sum).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        }).toObservable();
        result.subscribe(observer);
        verify(observer).onNext(1 + 2 + 3 + 4 + 5);
        verify(observer).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void aggregateAsIntSumSourceThrowsObservable() {
        Observable<Integer> result = Observable.concat(Observable.just(1, 2, 3, 4, 5), Observable.<Integer>error(new TestException())).reduce(0, sum).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        }).toObservable();
        result.subscribe(observer);
        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void aggregateAsIntSumAccumulatorThrowsObservable() {
        BiFunction<Integer, Integer, Integer> sumErr = new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                throw new TestException();
            }
        };
        Observable<Integer> result = Observable.just(1, 2, 3, 4, 5).reduce(0, sumErr).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        }).toObservable();
        result.subscribe(observer);
        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void aggregateAsIntSumResultSelectorThrowsObservable() {
        Function<Integer, Integer> error = new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                throw new TestException();
            }
        };
        Observable<Integer> result = Observable.just(1, 2, 3, 4, 5).reduce(0, sum).toObservable().map(error);
        result.subscribe(observer);
        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void backpressureWithNoInitialValueObservable() throws InterruptedException {
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Observable<Integer> reduced = source.reduce(sum).toObservable();
        Integer r = reduced.blockingFirst();
        assertEquals(21, r.intValue());
    }

    @Test
    public void backpressureWithInitialValueObservable() throws InterruptedException {
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Observable<Integer> reduced = source.reduce(0, sum).toObservable();
        Integer r = reduced.blockingFirst();
        assertEquals(21, r.intValue());
    }

    @Test
    public void aggregateAsIntSum() {
        Single<Integer> result = Observable.just(1, 2, 3, 4, 5).reduce(0, sum).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        });
        result.subscribe(singleObserver);
        verify(singleObserver).onSuccess(1 + 2 + 3 + 4 + 5);
        verify(singleObserver, never()).onError(any(Throwable.class));
    }

    @Test
    public void aggregateAsIntSumSourceThrows() {
        Single<Integer> result = Observable.concat(Observable.just(1, 2, 3, 4, 5), Observable.<Integer>error(new TestException())).reduce(0, sum).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        });
        result.subscribe(singleObserver);
        verify(singleObserver, never()).onSuccess(any());
        verify(singleObserver, times(1)).onError(any(TestException.class));
    }

    @Test
    public void aggregateAsIntSumAccumulatorThrows() {
        BiFunction<Integer, Integer, Integer> sumErr = new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                throw new TestException();
            }
        };
        Single<Integer> result = Observable.just(1, 2, 3, 4, 5).reduce(0, sumErr).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        });
        result.subscribe(singleObserver);
        verify(singleObserver, never()).onSuccess(any());
        verify(singleObserver, times(1)).onError(any(TestException.class));
    }

    @Test
    public void aggregateAsIntSumResultSelectorThrows() {
        Function<Integer, Integer> error = new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                throw new TestException();
            }
        };
        Single<Integer> result = Observable.just(1, 2, 3, 4, 5).reduce(0, sum).map(error);
        result.subscribe(singleObserver);
        verify(singleObserver, never()).onSuccess(any());
        verify(singleObserver, times(1)).onError(any(TestException.class));
    }

    @Test
    public void backpressureWithNoInitialValue() throws InterruptedException {
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Maybe<Integer> reduced = source.reduce(sum);
        Integer r = reduced.blockingGet();
        assertEquals(21, r.intValue());
    }

    @Test
    public void backpressureWithInitialValue() throws InterruptedException {
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Single<Integer> reduced = source.reduce(0, sum);
        Integer r = reduced.blockingGet();
        assertEquals(21, r.intValue());
    }

    @Test
    public void reduceWithSingle() {
        Observable.range(1, 5).reduceWith(new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                return 0;
            }
        }, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).test().assertResult(15);
    }

    @Test
    public void reduceMaybeDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToMaybe(new Function<Observable<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Observable<Object> o) throws Exception {
                return o.reduce(new BiFunction<Object, Object, Object>() {

                    @Override
                    public Object apply(Object a, Object b) throws Exception {
                        return a;
                    }
                });
            }
        });
    }

    @Test
    public void reduceMaybeCheckDisposed() {
        TestHelper.checkDisposed(Observable.just(new Object()).reduce(new BiFunction<Object, Object, Object>() {

            @Override
            public Object apply(Object a, Object b) throws Exception {
                return a;
            }
        }));
    }

    @Test
    public void reduceMaybeBadSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Object>() {

                @Override
                protected void subscribeActual(Observer<? super Object> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onComplete();
                    observer.onNext(1);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.reduce(new BiFunction<Object, Object, Object>() {

                @Override
                public Object apply(Object a, Object b) throws Exception {
                    return a;
                }
            }).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void seedDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToSingle(new Function<Observable<Integer>, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Observable<Integer> o) throws Exception {
                return o.reduce(0, new BiFunction<Integer, Integer, Integer>() {

                    @Override
                    public Integer apply(Integer a, Integer b) throws Exception {
                        return a;
                    }
                });
            }
        });
    }

    @Test
    public void seedDisposed() {
        TestHelper.checkDisposed(PublishSubject.<Integer>create().reduce(0, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a;
            }
        }));
    }

    @Test
    public void seedBadSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onComplete();
                    observer.onNext(1);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.reduce(0, new BiFunction<Integer, Integer, Integer>() {

                @Override
                public Integer apply(Integer a, Integer b) throws Exception {
                    return a;
                }
            }).test().assertResult(0);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableReduceTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumObservable() throws java.lang.Throwable {
            this.payloads.aggregateAsIntSumObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumSourceThrowsObservable() throws java.lang.Throwable {
            this.payloads.aggregateAsIntSumSourceThrowsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumAccumulatorThrowsObservable() throws java.lang.Throwable {
            this.payloads.aggregateAsIntSumAccumulatorThrowsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumResultSelectorThrowsObservable() throws java.lang.Throwable {
            this.payloads.aggregateAsIntSumResultSelectorThrowsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithNoInitialValueObservable() throws java.lang.Throwable {
            this.payloads.backpressureWithNoInitialValueObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithInitialValueObservable() throws java.lang.Throwable {
            this.payloads.backpressureWithInitialValueObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSum() throws java.lang.Throwable {
            this.payloads.aggregateAsIntSum.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumSourceThrows() throws java.lang.Throwable {
            this.payloads.aggregateAsIntSumSourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumAccumulatorThrows() throws java.lang.Throwable {
            this.payloads.aggregateAsIntSumAccumulatorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumResultSelectorThrows() throws java.lang.Throwable {
            this.payloads.aggregateAsIntSumResultSelectorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithNoInitialValue() throws java.lang.Throwable {
            this.payloads.backpressureWithNoInitialValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithInitialValue() throws java.lang.Throwable {
            this.payloads.backpressureWithInitialValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithSingle() throws java.lang.Throwable {
            this.payloads.reduceWithSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceMaybeDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.reduceMaybeDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceMaybeCheckDisposed() throws java.lang.Throwable {
            this.payloads.reduceMaybeCheckDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceMaybeBadSource() throws java.lang.Throwable {
            this.payloads.reduceMaybeBadSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_seedDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.seedDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_seedDisposed() throws java.lang.Throwable {
            this.payloads.seedDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_seedBadSource() throws java.lang.Throwable {
            this.payloads.seedBadSource.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableReduceTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableReduceTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.before();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableReduceTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableReduceTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableReduceTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableReduceTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableReduceTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableReduceTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement aggregateAsIntSumObservable;

            public org.junit.runners.model.Statement aggregateAsIntSumSourceThrowsObservable;

            public org.junit.runners.model.Statement aggregateAsIntSumAccumulatorThrowsObservable;

            public org.junit.runners.model.Statement aggregateAsIntSumResultSelectorThrowsObservable;

            public org.junit.runners.model.Statement backpressureWithNoInitialValueObservable;

            public org.junit.runners.model.Statement backpressureWithInitialValueObservable;

            public org.junit.runners.model.Statement aggregateAsIntSum;

            public org.junit.runners.model.Statement aggregateAsIntSumSourceThrows;

            public org.junit.runners.model.Statement aggregateAsIntSumAccumulatorThrows;

            public org.junit.runners.model.Statement aggregateAsIntSumResultSelectorThrows;

            public org.junit.runners.model.Statement backpressureWithNoInitialValue;

            public org.junit.runners.model.Statement backpressureWithInitialValue;

            public org.junit.runners.model.Statement reduceWithSingle;

            public org.junit.runners.model.Statement reduceMaybeDoubleOnSubscribe;

            public org.junit.runners.model.Statement reduceMaybeCheckDisposed;

            public org.junit.runners.model.Statement reduceMaybeBadSource;

            public org.junit.runners.model.Statement seedDoubleOnSubscribe;

            public org.junit.runners.model.Statement seedDisposed;

            public org.junit.runners.model.Statement seedBadSource;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.aggregateAsIntSumObservable = _ClassStatement.forPayload(ObservableReduceTest::aggregateAsIntSumObservable, "aggregateAsIntSumObservable", this);
            this.payloads.aggregateAsIntSumSourceThrowsObservable = _ClassStatement.forPayload(ObservableReduceTest::aggregateAsIntSumSourceThrowsObservable, "aggregateAsIntSumSourceThrowsObservable", this);
            this.payloads.aggregateAsIntSumAccumulatorThrowsObservable = _ClassStatement.forPayload(ObservableReduceTest::aggregateAsIntSumAccumulatorThrowsObservable, "aggregateAsIntSumAccumulatorThrowsObservable", this);
            this.payloads.aggregateAsIntSumResultSelectorThrowsObservable = _ClassStatement.forPayload(ObservableReduceTest::aggregateAsIntSumResultSelectorThrowsObservable, "aggregateAsIntSumResultSelectorThrowsObservable", this);
            this.payloads.backpressureWithNoInitialValueObservable = _ClassStatement.forPayload(ObservableReduceTest::backpressureWithNoInitialValueObservable, "backpressureWithNoInitialValueObservable", this);
            this.payloads.backpressureWithInitialValueObservable = _ClassStatement.forPayload(ObservableReduceTest::backpressureWithInitialValueObservable, "backpressureWithInitialValueObservable", this);
            this.payloads.aggregateAsIntSum = _ClassStatement.forPayload(ObservableReduceTest::aggregateAsIntSum, "aggregateAsIntSum", this);
            this.payloads.aggregateAsIntSumSourceThrows = _ClassStatement.forPayload(ObservableReduceTest::aggregateAsIntSumSourceThrows, "aggregateAsIntSumSourceThrows", this);
            this.payloads.aggregateAsIntSumAccumulatorThrows = _ClassStatement.forPayload(ObservableReduceTest::aggregateAsIntSumAccumulatorThrows, "aggregateAsIntSumAccumulatorThrows", this);
            this.payloads.aggregateAsIntSumResultSelectorThrows = _ClassStatement.forPayload(ObservableReduceTest::aggregateAsIntSumResultSelectorThrows, "aggregateAsIntSumResultSelectorThrows", this);
            this.payloads.backpressureWithNoInitialValue = _ClassStatement.forPayload(ObservableReduceTest::backpressureWithNoInitialValue, "backpressureWithNoInitialValue", this);
            this.payloads.backpressureWithInitialValue = _ClassStatement.forPayload(ObservableReduceTest::backpressureWithInitialValue, "backpressureWithInitialValue", this);
            this.payloads.reduceWithSingle = _ClassStatement.forPayload(ObservableReduceTest::reduceWithSingle, "reduceWithSingle", this);
            this.payloads.reduceMaybeDoubleOnSubscribe = _ClassStatement.forPayload(ObservableReduceTest::reduceMaybeDoubleOnSubscribe, "reduceMaybeDoubleOnSubscribe", this);
            this.payloads.reduceMaybeCheckDisposed = _ClassStatement.forPayload(ObservableReduceTest::reduceMaybeCheckDisposed, "reduceMaybeCheckDisposed", this);
            this.payloads.reduceMaybeBadSource = _ClassStatement.forPayload(ObservableReduceTest::reduceMaybeBadSource, "reduceMaybeBadSource", this);
            this.payloads.seedDoubleOnSubscribe = _ClassStatement.forPayload(ObservableReduceTest::seedDoubleOnSubscribe, "seedDoubleOnSubscribe", this);
            this.payloads.seedDisposed = _ClassStatement.forPayload(ObservableReduceTest::seedDisposed, "seedDisposed", this);
            this.payloads.seedBadSource = _ClassStatement.forPayload(ObservableReduceTest::seedBadSource, "seedBadSource", this);
        }
    }
}
