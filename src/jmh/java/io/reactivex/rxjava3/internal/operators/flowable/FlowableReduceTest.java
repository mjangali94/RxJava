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
package io.reactivex.rxjava3.internal.operators.flowable;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.fuseable.HasUpstreamPublisher;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableReduceTest extends RxJavaTest {

    Subscriber<Object> subscriber;

    SingleObserver<Object> singleObserver;

    @Before
    public void before() {
        subscriber = TestHelper.mockSubscriber();
        singleObserver = TestHelper.mockSingleObserver();
    }

    BiFunction<Integer, Integer, Integer> sum = new BiFunction<Integer, Integer, Integer>() {

        @Override
        public Integer apply(Integer t1, Integer t2) {
            return t1 + t2;
        }
    };

    @Test
    public void aggregateAsIntSumFlowable() {
        Flowable<Integer> result = Flowable.just(1, 2, 3, 4, 5).reduce(0, sum).toFlowable().map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        });
        result.subscribe(subscriber);
        verify(subscriber).onNext(1 + 2 + 3 + 4 + 5);
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void aggregateAsIntSumSourceThrowsFlowable() {
        Flowable<Integer> result = Flowable.concat(Flowable.just(1, 2, 3, 4, 5), Flowable.<Integer>error(new TestException())).reduce(0, sum).toFlowable().map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        });
        result.subscribe(subscriber);
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
        verify(subscriber, times(1)).onError(any(TestException.class));
    }

    @Test
    public void aggregateAsIntSumAccumulatorThrowsFlowable() {
        BiFunction<Integer, Integer, Integer> sumErr = new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                throw new TestException();
            }
        };
        Flowable<Integer> result = Flowable.just(1, 2, 3, 4, 5).reduce(0, sumErr).toFlowable().map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        });
        result.subscribe(subscriber);
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
        verify(subscriber, times(1)).onError(any(TestException.class));
    }

    @Test
    public void aggregateAsIntSumResultSelectorThrowsFlowable() {
        Function<Integer, Integer> error = new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                throw new TestException();
            }
        };
        Flowable<Integer> result = Flowable.just(1, 2, 3, 4, 5).reduce(0, sum).toFlowable().map(error);
        result.subscribe(subscriber);
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
        verify(subscriber, times(1)).onError(any(TestException.class));
    }

    @Test
    public void backpressureWithInitialValueFlowable() throws InterruptedException {
        Flowable<Integer> source = Flowable.just(1, 2, 3, 4, 5, 6);
        Flowable<Integer> reduced = source.reduce(0, sum).toFlowable();
        Integer r = reduced.blockingFirst();
        assertEquals(21, r.intValue());
    }

    @Test
    public void aggregateAsIntSum() {
        Single<Integer> result = Flowable.just(1, 2, 3, 4, 5).reduce(0, sum).map(new Function<Integer, Integer>() {

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
        Single<Integer> result = Flowable.concat(Flowable.just(1, 2, 3, 4, 5), Flowable.<Integer>error(new TestException())).reduce(0, sum).map(new Function<Integer, Integer>() {

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
        Single<Integer> result = Flowable.just(1, 2, 3, 4, 5).reduce(0, sumErr).map(new Function<Integer, Integer>() {

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
        Single<Integer> result = Flowable.just(1, 2, 3, 4, 5).reduce(0, sum).map(error);
        result.subscribe(singleObserver);
        verify(singleObserver, never()).onSuccess(any());
        verify(singleObserver, times(1)).onError(any(TestException.class));
    }

    @Test
    public void backpressureWithNoInitialValue() throws InterruptedException {
        Flowable<Integer> source = Flowable.just(1, 2, 3, 4, 5, 6);
        Maybe<Integer> reduced = source.reduce(sum);
        Integer r = reduced.blockingGet();
        assertEquals(21, r.intValue());
    }

    @Test
    public void backpressureWithInitialValue() throws InterruptedException {
        Flowable<Integer> source = Flowable.just(1, 2, 3, 4, 5, 6);
        Single<Integer> reduced = source.reduce(0, sum);
        Integer r = reduced.blockingGet();
        assertEquals(21, r.intValue());
    }

    @Test
    public void reducerCrashSuppressOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.<Integer>fromPublisher(new Publisher<Integer>() {

                @Override
                public void subscribe(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onNext(1);
                    s.onError(new TestException("Source"));
                    s.onComplete();
                }
            }).reduce(new BiFunction<Integer, Integer, Integer>() {

                @Override
                public Integer apply(Integer a, Integer b) throws Exception {
                    throw new TestException("Reducer");
                }
            }).toFlowable().to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "Reducer");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Source");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancel() {
        TestSubscriber<Integer> ts = Flowable.just(1).concatWith(Flowable.<Integer>never()).reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).toFlowable().test();
        ts.assertEmpty();
        ts.cancel();
        ts.assertEmpty();
    }

    @Test
    public void backpressureWithNoInitialValueObservable() throws InterruptedException {
        Flowable<Integer> source = Flowable.just(1, 2, 3, 4, 5, 6);
        Flowable<Integer> reduced = source.reduce(sum).toFlowable();
        Integer r = reduced.blockingFirst();
        assertEquals(21, r.intValue());
    }

    @Test
    public void source() {
        Flowable<Integer> source = Flowable.just(1);
        assertSame(source, (((HasUpstreamPublisher<?>) source.reduce(sum))).source());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.range(1, 2).reduce(sum));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToMaybe(new Function<Flowable<Integer>, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Flowable<Integer> f) throws Exception {
                return f.reduce(sum);
            }
        });
    }

    @Test
    public void error() {
        Flowable.<Integer>error(new TestException()).reduce(sum).test().assertFailure(TestException.class);
    }

    @Test
    public void errorFlowable() {
        Flowable.<Integer>error(new TestException()).reduce(sum).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void empty() {
        Flowable.<Integer>empty().reduce(sum).test().assertResult();
    }

    @Test
    public void emptyFlowable() {
        Flowable.<Integer>empty().reduce(sum).toFlowable().test().assertResult();
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.reduce(sum);
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void badSourceFlowable() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.reduce(sum).toFlowable();
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void reducerThrows() {
        Flowable.just(1, 2).reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    /**
     * Make sure an asynchronous reduce with flatMap works.
     * Original Reactor-Core test case: https://gist.github.com/jurna/353a2bd8ff83f0b24f0b5bc772077d61
     */
    @Test
    public void shouldReduceTo10Events() {
        final AtomicInteger count = new AtomicInteger();
        Flowable.range(0, 10).flatMap(new Function<Integer, Publisher<String>>() {

            @Override
            public Publisher<String> apply(final Integer x) throws Exception {
                return Flowable.range(0, 2).map(new Function<Integer, String>() {

                    @Override
                    public String apply(Integer y) throws Exception {
                        return blockingOp(x, y);
                    }
                }).subscribeOn(Schedulers.io()).reduce(new BiFunction<String, String, String>() {

                    @Override
                    public String apply(String l, String r) throws Exception {
                        return l + "_" + r;
                    }
                }).doOnSuccess(new Consumer<String>() {

                    @Override
                    public void accept(String s) throws Exception {
                        count.incrementAndGet();
                        // System.out.println("Completed with " + s);
                    }
                }).toFlowable();
            }
        }).blockingLast();
        assertEquals(10, count.get());
    }

    /**
     * Make sure an asynchronous reduce with flatMap works.
     * Original Reactor-Core test case: https://gist.github.com/jurna/353a2bd8ff83f0b24f0b5bc772077d61
     */
    @Test
    public void shouldReduceTo10EventsFlowable() {
        final AtomicInteger count = new AtomicInteger();
        Flowable.range(0, 10).flatMap(new Function<Integer, Publisher<String>>() {

            @Override
            public Publisher<String> apply(final Integer x) throws Exception {
                return Flowable.range(0, 2).map(new Function<Integer, String>() {

                    @Override
                    public String apply(Integer y) throws Exception {
                        return blockingOp(x, y);
                    }
                }).subscribeOn(Schedulers.io()).reduce(new BiFunction<String, String, String>() {

                    @Override
                    public String apply(String l, String r) throws Exception {
                        return l + "_" + r;
                    }
                }).toFlowable().doOnNext(new Consumer<String>() {

                    @Override
                    public void accept(String s) throws Exception {
                        count.incrementAndGet();
                        // System.out.println("Completed with " + s);
                    }
                });
            }
        }).blockingLast();
        assertEquals(10, count.get());
    }

    static String blockingOp(Integer x, Integer y) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "x" + x + "y" + y;
    }

    @Test
    public void seedDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Integer>, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Flowable<Integer> f) throws Exception {
                return f.reduce(0, new BiFunction<Integer, Integer, Integer>() {

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
        TestHelper.checkDisposed(PublishProcessor.<Integer>create().reduce(0, new BiFunction<Integer, Integer, Integer>() {

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
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onComplete();
                    subscriber.onNext(1);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
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

    @Test
    public void doubleOnSubscribeFlowable() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.reduce((a, b) -> a).toFlowable());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableReduceTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumFlowable() throws java.lang.Throwable {
            this.payloads.aggregateAsIntSumFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumSourceThrowsFlowable() throws java.lang.Throwable {
            this.payloads.aggregateAsIntSumSourceThrowsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumAccumulatorThrowsFlowable() throws java.lang.Throwable {
            this.payloads.aggregateAsIntSumAccumulatorThrowsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_aggregateAsIntSumResultSelectorThrowsFlowable() throws java.lang.Throwable {
            this.payloads.aggregateAsIntSumResultSelectorThrowsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithInitialValueFlowable() throws java.lang.Throwable {
            this.payloads.backpressureWithInitialValueFlowable.evaluate();
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
        public void benchmark_reducerCrashSuppressOnError() throws java.lang.Throwable {
            this.payloads.reducerCrashSuppressOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithNoInitialValueObservable() throws java.lang.Throwable {
            this.payloads.backpressureWithNoInitialValueObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_source() throws java.lang.Throwable {
            this.payloads.source.evaluate();
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
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorFlowable() throws java.lang.Throwable {
            this.payloads.errorFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyFlowable() throws java.lang.Throwable {
            this.payloads.emptyFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceFlowable() throws java.lang.Throwable {
            this.payloads.badSourceFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reducerThrows() throws java.lang.Throwable {
            this.payloads.reducerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReduceTo10Events() throws java.lang.Throwable {
            this.payloads.shouldReduceTo10Events.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReduceTo10EventsFlowable() throws java.lang.Throwable {
            this.payloads.shouldReduceTo10EventsFlowable.evaluate();
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

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeFlowable() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribeFlowable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReduceTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReduceTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReduceTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReduceTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableReduceTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReduceTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableReduceTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableReduceTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement aggregateAsIntSumFlowable;

            public org.junit.runners.model.Statement aggregateAsIntSumSourceThrowsFlowable;

            public org.junit.runners.model.Statement aggregateAsIntSumAccumulatorThrowsFlowable;

            public org.junit.runners.model.Statement aggregateAsIntSumResultSelectorThrowsFlowable;

            public org.junit.runners.model.Statement backpressureWithInitialValueFlowable;

            public org.junit.runners.model.Statement aggregateAsIntSum;

            public org.junit.runners.model.Statement aggregateAsIntSumSourceThrows;

            public org.junit.runners.model.Statement aggregateAsIntSumAccumulatorThrows;

            public org.junit.runners.model.Statement aggregateAsIntSumResultSelectorThrows;

            public org.junit.runners.model.Statement backpressureWithNoInitialValue;

            public org.junit.runners.model.Statement backpressureWithInitialValue;

            public org.junit.runners.model.Statement reducerCrashSuppressOnError;

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement backpressureWithNoInitialValueObservable;

            public org.junit.runners.model.Statement source;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement errorFlowable;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement emptyFlowable;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement badSourceFlowable;

            public org.junit.runners.model.Statement reducerThrows;

            public org.junit.runners.model.Statement shouldReduceTo10Events;

            public org.junit.runners.model.Statement shouldReduceTo10EventsFlowable;

            public org.junit.runners.model.Statement seedDoubleOnSubscribe;

            public org.junit.runners.model.Statement seedDisposed;

            public org.junit.runners.model.Statement seedBadSource;

            public org.junit.runners.model.Statement doubleOnSubscribeFlowable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.aggregateAsIntSumFlowable = _ClassStatement.forPayload(FlowableReduceTest::aggregateAsIntSumFlowable, "aggregateAsIntSumFlowable", this);
            this.payloads.aggregateAsIntSumSourceThrowsFlowable = _ClassStatement.forPayload(FlowableReduceTest::aggregateAsIntSumSourceThrowsFlowable, "aggregateAsIntSumSourceThrowsFlowable", this);
            this.payloads.aggregateAsIntSumAccumulatorThrowsFlowable = _ClassStatement.forPayload(FlowableReduceTest::aggregateAsIntSumAccumulatorThrowsFlowable, "aggregateAsIntSumAccumulatorThrowsFlowable", this);
            this.payloads.aggregateAsIntSumResultSelectorThrowsFlowable = _ClassStatement.forPayload(FlowableReduceTest::aggregateAsIntSumResultSelectorThrowsFlowable, "aggregateAsIntSumResultSelectorThrowsFlowable", this);
            this.payloads.backpressureWithInitialValueFlowable = _ClassStatement.forPayload(FlowableReduceTest::backpressureWithInitialValueFlowable, "backpressureWithInitialValueFlowable", this);
            this.payloads.aggregateAsIntSum = _ClassStatement.forPayload(FlowableReduceTest::aggregateAsIntSum, "aggregateAsIntSum", this);
            this.payloads.aggregateAsIntSumSourceThrows = _ClassStatement.forPayload(FlowableReduceTest::aggregateAsIntSumSourceThrows, "aggregateAsIntSumSourceThrows", this);
            this.payloads.aggregateAsIntSumAccumulatorThrows = _ClassStatement.forPayload(FlowableReduceTest::aggregateAsIntSumAccumulatorThrows, "aggregateAsIntSumAccumulatorThrows", this);
            this.payloads.aggregateAsIntSumResultSelectorThrows = _ClassStatement.forPayload(FlowableReduceTest::aggregateAsIntSumResultSelectorThrows, "aggregateAsIntSumResultSelectorThrows", this);
            this.payloads.backpressureWithNoInitialValue = _ClassStatement.forPayload(FlowableReduceTest::backpressureWithNoInitialValue, "backpressureWithNoInitialValue", this);
            this.payloads.backpressureWithInitialValue = _ClassStatement.forPayload(FlowableReduceTest::backpressureWithInitialValue, "backpressureWithInitialValue", this);
            this.payloads.reducerCrashSuppressOnError = _ClassStatement.forPayload(FlowableReduceTest::reducerCrashSuppressOnError, "reducerCrashSuppressOnError", this);
            this.payloads.cancel = _ClassStatement.forPayload(FlowableReduceTest::cancel, "cancel", this);
            this.payloads.backpressureWithNoInitialValueObservable = _ClassStatement.forPayload(FlowableReduceTest::backpressureWithNoInitialValueObservable, "backpressureWithNoInitialValueObservable", this);
            this.payloads.source = _ClassStatement.forPayload(FlowableReduceTest::source, "source", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableReduceTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableReduceTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableReduceTest::error, "error", this);
            this.payloads.errorFlowable = _ClassStatement.forPayload(FlowableReduceTest::errorFlowable, "errorFlowable", this);
            this.payloads.empty = _ClassStatement.forPayload(FlowableReduceTest::empty, "empty", this);
            this.payloads.emptyFlowable = _ClassStatement.forPayload(FlowableReduceTest::emptyFlowable, "emptyFlowable", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableReduceTest::badSource, "badSource", this);
            this.payloads.badSourceFlowable = _ClassStatement.forPayload(FlowableReduceTest::badSourceFlowable, "badSourceFlowable", this);
            this.payloads.reducerThrows = _ClassStatement.forPayload(FlowableReduceTest::reducerThrows, "reducerThrows", this);
            this.payloads.shouldReduceTo10Events = _ClassStatement.forPayload(FlowableReduceTest::shouldReduceTo10Events, "shouldReduceTo10Events", this);
            this.payloads.shouldReduceTo10EventsFlowable = _ClassStatement.forPayload(FlowableReduceTest::shouldReduceTo10EventsFlowable, "shouldReduceTo10EventsFlowable", this);
            this.payloads.seedDoubleOnSubscribe = _ClassStatement.forPayload(FlowableReduceTest::seedDoubleOnSubscribe, "seedDoubleOnSubscribe", this);
            this.payloads.seedDisposed = _ClassStatement.forPayload(FlowableReduceTest::seedDisposed, "seedDisposed", this);
            this.payloads.seedBadSource = _ClassStatement.forPayload(FlowableReduceTest::seedBadSource, "seedBadSource", this);
            this.payloads.doubleOnSubscribeFlowable = _ClassStatement.forPayload(FlowableReduceTest::doubleOnSubscribeFlowable, "doubleOnSubscribeFlowable", this);
        }
    }
}
