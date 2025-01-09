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
import java.util.List;
import java.util.concurrent.*;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableFlatMapSingleTest extends RxJavaTest {

    @Test
    public void normal() {
        Flowable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalDelayError() {
        Flowable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }, true, Integer.MAX_VALUE).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalAsync() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v).subscribeOn(Schedulers.computation());
            }
        }).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(10).assertNoErrors().assertComplete();
        TestHelper.assertValueSet(ts, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalAsyncMaxConcurrency() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v).subscribeOn(Schedulers.computation());
            }
        }, false, 3).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertNoErrors().assertComplete();
        TestHelper.assertValueSet(ts, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalAsyncMaxConcurrency1() {
        Flowable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v).subscribeOn(Schedulers.computation());
            }
        }, false, 1).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void mapperThrowsFlowable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertFailure(TestException.class);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void mapperReturnsNullFlowable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return null;
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertFailure(NullPointerException.class);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void normalDelayErrorAll() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(ts.errors().get(0));
        for (int i = 0; i < 11; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalBackpressured() {
        Flowable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }).rebatchRequests(1).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalMaxConcurrent1Backpressured() {
        Flowable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }, false, 1).rebatchRequests(1).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalMaxConcurrent2Backpressured() {
        Flowable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }, false, 2).rebatchRequests(1).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void takeAsync() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v).subscribeOn(Schedulers.computation());
            }
        }).take(2).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(2).assertNoErrors().assertComplete();
        TestHelper.assertValueSet(ts, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void take() {
        Flowable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }).take(2).test().assertResult(1, 2);
    }

    @Test
    public void middleError() {
        Flowable.fromArray(new String[] { "1", "a", "2" }).flatMapSingle(new Function<String, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final String s) throws NumberFormatException {
                // return Single.just(Integer.valueOf(s)); //This works
                return Single.fromCallable(new Callable<Integer>() {

                    @Override
                    public Integer call() throws NumberFormatException {
                        return Integer.valueOf(s);
                    }
                });
            }
        }).test().assertFailure(NumberFormatException.class, 1);
    }

    @Test
    public void asyncFlatten() {
        Flowable.range(1, 1000).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(1).subscribeOn(Schedulers.computation());
            }
        }).take(500).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(500).assertNoErrors().assertComplete();
    }

    @Test
    public void successError() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = Flowable.range(1, 2).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                if (v == 2) {
                    return pp.singleOrError();
                }
                return Single.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).test();
        pp.onNext(1);
        pp.onComplete();
        ts.assertFailure(TestException.class, 1);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishProcessor.<Integer>create().flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.<Integer>just(1);
            }
        }));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Object> f) throws Exception {
                return f.flatMapSingle(Functions.justFunction(Single.just(2)));
            }
        });
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onError(new TestException("First"));
                    subscriber.onError(new TestException("Second"));
                }
            }.flatMapSingle(Functions.justFunction(Single.just(2))).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badInnerSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.just(1).flatMapSingle(Functions.justFunction(new Single<Integer>() {

                @Override
                protected void subscribeActual(SingleObserver<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onError(new TestException("First"));
                    observer.onError(new TestException("Second"));
                }
            })).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void emissionQueueTrigger() {
        final PublishProcessor<Integer> pp1 = PublishProcessor.create();
        final PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    pp2.onNext(2);
                    pp2.onComplete();
                }
            }
        };
        Flowable.just(pp1, pp2).flatMapSingle(new Function<PublishProcessor<Integer>, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(PublishProcessor<Integer> v) throws Exception {
                return v.singleOrError();
            }
        }).subscribe(ts);
        pp1.onNext(1);
        pp1.onComplete();
        ts.assertResult(1, 2);
    }

    @Test
    public void disposeInner() {
        final TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.just(1).flatMapSingle(new Function<Integer, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Integer v) throws Exception {
                return new Single<Object>() {

                    @Override
                    protected void subscribeActual(SingleObserver<? super Object> observer) {
                        observer.onSubscribe(Disposable.empty());
                        assertFalse(((Disposable) observer).isDisposed());
                        ts.cancel();
                        assertTrue(((Disposable) observer).isDisposed());
                    }
                };
            }
        }).subscribe(ts);
        ts.assertEmpty();
    }

    @Test
    public void innerSuccessCompletesAfterMain() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = Flowable.just(1).flatMapSingle(Functions.justFunction(pp.singleOrError())).test();
        pp.onNext(2);
        pp.onComplete();
        ts.assertResult(2);
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = Flowable.just(1).flatMapSingle(Functions.justFunction(Single.just(2))).test(0L).assertEmpty();
        ts.request(1);
        ts.assertResult(2);
    }

    @Test
    public void error() {
        Flowable.just(1).flatMapSingle(Functions.justFunction(Single.<Integer>error(new TestException()))).test(0L).assertFailure(TestException.class);
    }

    @Test
    public void errorDelayed() {
        Flowable.just(1).flatMapSingle(Functions.justFunction(Single.<Integer>error(new TestException())), true, 16).test(0L).assertFailure(TestException.class);
    }

    @Test
    public void requestCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = Flowable.just(1).concatWith(Flowable.<Integer>never()).flatMapSingle(Functions.justFunction(Single.just(2))).test(0);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts.request(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void asyncFlattenErrorMaxConcurrency() {
        Flowable.range(1, 1000).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.<Integer>error(new TestException()).subscribeOn(Schedulers.computation());
            }
        }, true, 128).take(500).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(CompositeException.class);
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.flatMapSingle(new Function<Integer, Single<Integer>>() {

                    @Override
                    public Single<Integer> apply(Integer v) throws Throwable {
                        return Single.just(v).hide();
                    }
                });
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayError() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.flatMapSingle(new Function<Integer, Single<Integer>>() {

                    @Override
                    public Single<Integer> apply(Integer v) throws Throwable {
                        return Single.just(v).hide();
                    }
                }, true, 2);
            }
        });
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().flatMapSingle(v -> Single.never()));
    }

    @Test
    public void successRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            SingleSubject<Integer> ss1 = SingleSubject.create();
            SingleSubject<Integer> ss2 = SingleSubject.create();
            TestSubscriber<Integer> ts = Flowable.just(ss1, ss2).flatMapSingle(v -> v).test();
            TestHelper.race(() -> ss1.onSuccess(1), () -> ss2.onSuccess(1));
            ts.assertResult(1, 1);
        }
    }

    @Test
    public void successShortcut() {
        SingleSubject<Integer> ss1 = SingleSubject.create();
        TestSubscriber<Integer> ts = Flowable.just(ss1).hide().flatMapSingle(v -> v).test();
        ss1.onSuccess(1);
        ts.assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableFlatMapSingleTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayError() throws java.lang.Throwable {
            this.payloads.normalDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsync() throws java.lang.Throwable {
            this.payloads.normalAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsyncMaxConcurrency() throws java.lang.Throwable {
            this.payloads.normalAsyncMaxConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsyncMaxConcurrency1() throws java.lang.Throwable {
            this.payloads.normalAsyncMaxConcurrency1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrowsFlowable() throws java.lang.Throwable {
            this.payloads.mapperThrowsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperReturnsNullFlowable() throws java.lang.Throwable {
            this.payloads.mapperReturnsNullFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayErrorAll() throws java.lang.Throwable {
            this.payloads.normalDelayErrorAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalBackpressured() throws java.lang.Throwable {
            this.payloads.normalBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalMaxConcurrent1Backpressured() throws java.lang.Throwable {
            this.payloads.normalMaxConcurrent1Backpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalMaxConcurrent2Backpressured() throws java.lang.Throwable {
            this.payloads.normalMaxConcurrent2Backpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeAsync() throws java.lang.Throwable {
            this.payloads.takeAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_middleError() throws java.lang.Throwable {
            this.payloads.middleError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFlatten() throws java.lang.Throwable {
            this.payloads.asyncFlatten.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successError() throws java.lang.Throwable {
            this.payloads.successError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badInnerSource() throws java.lang.Throwable {
            this.payloads.badInnerSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emissionQueueTrigger() throws java.lang.Throwable {
            this.payloads.emissionQueueTrigger.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeInner() throws java.lang.Throwable {
            this.payloads.disposeInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerSuccessCompletesAfterMain() throws java.lang.Throwable {
            this.payloads.innerSuccessCompletesAfterMain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.payloads.backpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDelayed() throws java.lang.Throwable {
            this.payloads.errorDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestCancelRace() throws java.lang.Throwable {
            this.payloads.requestCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFlattenErrorMaxConcurrency() throws java.lang.Throwable {
            this.payloads.asyncFlattenErrorMaxConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelDelayError() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancelDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successRace() throws java.lang.Throwable {
            this.payloads.successRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successShortcut() throws java.lang.Throwable {
            this.payloads.successShortcut.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapSingleTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapSingleTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapSingleTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapSingleTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableFlatMapSingleTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapSingleTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableFlatMapSingleTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableFlatMapSingleTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement normalDelayError;

            public org.junit.runners.model.Statement normalAsync;

            public org.junit.runners.model.Statement normalAsyncMaxConcurrency;

            public org.junit.runners.model.Statement normalAsyncMaxConcurrency1;

            public org.junit.runners.model.Statement mapperThrowsFlowable;

            public org.junit.runners.model.Statement mapperReturnsNullFlowable;

            public org.junit.runners.model.Statement normalDelayErrorAll;

            public org.junit.runners.model.Statement normalBackpressured;

            public org.junit.runners.model.Statement normalMaxConcurrent1Backpressured;

            public org.junit.runners.model.Statement normalMaxConcurrent2Backpressured;

            public org.junit.runners.model.Statement takeAsync;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement middleError;

            public org.junit.runners.model.Statement asyncFlatten;

            public org.junit.runners.model.Statement successError;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement badInnerSource;

            public org.junit.runners.model.Statement emissionQueueTrigger;

            public org.junit.runners.model.Statement disposeInner;

            public org.junit.runners.model.Statement innerSuccessCompletesAfterMain;

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement errorDelayed;

            public org.junit.runners.model.Statement requestCancelRace;

            public org.junit.runners.model.Statement asyncFlattenErrorMaxConcurrency;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement successRace;

            public org.junit.runners.model.Statement successShortcut;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(FlowableFlatMapSingleTest::normal, "normal", this);
            this.payloads.normalDelayError = _ClassStatement.forPayload(FlowableFlatMapSingleTest::normalDelayError, "normalDelayError", this);
            this.payloads.normalAsync = _ClassStatement.forPayload(FlowableFlatMapSingleTest::normalAsync, "normalAsync", this);
            this.payloads.normalAsyncMaxConcurrency = _ClassStatement.forPayload(FlowableFlatMapSingleTest::normalAsyncMaxConcurrency, "normalAsyncMaxConcurrency", this);
            this.payloads.normalAsyncMaxConcurrency1 = _ClassStatement.forPayload(FlowableFlatMapSingleTest::normalAsyncMaxConcurrency1, "normalAsyncMaxConcurrency1", this);
            this.payloads.mapperThrowsFlowable = _ClassStatement.forPayload(FlowableFlatMapSingleTest::mapperThrowsFlowable, "mapperThrowsFlowable", this);
            this.payloads.mapperReturnsNullFlowable = _ClassStatement.forPayload(FlowableFlatMapSingleTest::mapperReturnsNullFlowable, "mapperReturnsNullFlowable", this);
            this.payloads.normalDelayErrorAll = _ClassStatement.forPayload(FlowableFlatMapSingleTest::normalDelayErrorAll, "normalDelayErrorAll", this);
            this.payloads.normalBackpressured = _ClassStatement.forPayload(FlowableFlatMapSingleTest::normalBackpressured, "normalBackpressured", this);
            this.payloads.normalMaxConcurrent1Backpressured = _ClassStatement.forPayload(FlowableFlatMapSingleTest::normalMaxConcurrent1Backpressured, "normalMaxConcurrent1Backpressured", this);
            this.payloads.normalMaxConcurrent2Backpressured = _ClassStatement.forPayload(FlowableFlatMapSingleTest::normalMaxConcurrent2Backpressured, "normalMaxConcurrent2Backpressured", this);
            this.payloads.takeAsync = _ClassStatement.forPayload(FlowableFlatMapSingleTest::takeAsync, "takeAsync", this);
            this.payloads.take = _ClassStatement.forPayload(FlowableFlatMapSingleTest::take, "take", this);
            this.payloads.middleError = _ClassStatement.forPayload(FlowableFlatMapSingleTest::middleError, "middleError", this);
            this.payloads.asyncFlatten = _ClassStatement.forPayload(FlowableFlatMapSingleTest::asyncFlatten, "asyncFlatten", this);
            this.payloads.successError = _ClassStatement.forPayload(FlowableFlatMapSingleTest::successError, "successError", this);
            this.payloads.disposed = _ClassStatement.forPayload(FlowableFlatMapSingleTest::disposed, "disposed", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableFlatMapSingleTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableFlatMapSingleTest::badSource, "badSource", this);
            this.payloads.badInnerSource = _ClassStatement.forPayload(FlowableFlatMapSingleTest::badInnerSource, "badInnerSource", this);
            this.payloads.emissionQueueTrigger = _ClassStatement.forPayload(FlowableFlatMapSingleTest::emissionQueueTrigger, "emissionQueueTrigger", this);
            this.payloads.disposeInner = _ClassStatement.forPayload(FlowableFlatMapSingleTest::disposeInner, "disposeInner", this);
            this.payloads.innerSuccessCompletesAfterMain = _ClassStatement.forPayload(FlowableFlatMapSingleTest::innerSuccessCompletesAfterMain, "innerSuccessCompletesAfterMain", this);
            this.payloads.backpressure = _ClassStatement.forPayload(FlowableFlatMapSingleTest::backpressure, "backpressure", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableFlatMapSingleTest::error, "error", this);
            this.payloads.errorDelayed = _ClassStatement.forPayload(FlowableFlatMapSingleTest::errorDelayed, "errorDelayed", this);
            this.payloads.requestCancelRace = _ClassStatement.forPayload(FlowableFlatMapSingleTest::requestCancelRace, "requestCancelRace", this);
            this.payloads.asyncFlattenErrorMaxConcurrency = _ClassStatement.forPayload(FlowableFlatMapSingleTest::asyncFlattenErrorMaxConcurrency, "asyncFlattenErrorMaxConcurrency", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(FlowableFlatMapSingleTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(FlowableFlatMapSingleTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableFlatMapSingleTest::badRequest, "badRequest", this);
            this.payloads.successRace = _ClassStatement.forPayload(FlowableFlatMapSingleTest::successRace, "successRace", this);
            this.payloads.successShortcut = _ClassStatement.forPayload(FlowableFlatMapSingleTest::successShortcut, "successShortcut", this);
        }
    }
}
