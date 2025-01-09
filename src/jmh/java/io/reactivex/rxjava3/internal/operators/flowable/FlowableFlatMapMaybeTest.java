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
import io.reactivex.rxjava3.subjects.MaybeSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableFlatMapMaybeTest extends RxJavaTest {

    @Test
    public void normal() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalEmpty() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.empty();
            }
        }).test().assertResult();
    }

    @Test
    public void normalDelayError() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }, true, Integer.MAX_VALUE).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalAsync() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v).subscribeOn(Schedulers.computation());
            }
        }).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(10).assertNoErrors().assertComplete();
        TestHelper.assertValueSet(ts, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalAsyncMaxConcurrency() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v).subscribeOn(Schedulers.computation());
            }
        }, false, 3).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(10).assertNoErrors().assertComplete();
        TestHelper.assertValueSet(ts, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalAsyncMaxConcurrency1() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v).subscribeOn(Schedulers.computation());
            }
        }, false, 1).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void mapperThrowsFlowable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
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
        TestSubscriber<Integer> ts = pp.flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
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
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(ts.errors().get(0));
        for (int i = 0; i < 11; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalBackpressured() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }).rebatchRequests(1).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalMaxConcurrent1Backpressured() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }, false, 1).rebatchRequests(1).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalMaxConcurrent2Backpressured() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }, false, 2).rebatchRequests(1).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void takeAsync() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v).subscribeOn(Schedulers.computation());
            }
        }).take(2).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(2).assertNoErrors().assertComplete();
        TestHelper.assertValueSet(ts, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void take() {
        Flowable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }).take(2).test().assertResult(1, 2);
    }

    @Test
    public void middleError() {
        Flowable.fromArray(new String[] { "1", "a", "2" }).flatMapMaybe(new Function<String, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(final String s) throws NumberFormatException {
                // return Maybe.just(Integer.valueOf(s)); //This works
                return Maybe.fromCallable(new Callable<Integer>() {

                    @Override
                    public Integer call() throws NumberFormatException {
                        return Integer.valueOf(s);
                    }
                });
            }
        }).test().assertFailure(NumberFormatException.class, 1);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishProcessor.<Integer>create().flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.<Integer>empty();
            }
        }));
    }

    @Test
    public void asyncFlatten() {
        Flowable.range(1, 1000).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(1).subscribeOn(Schedulers.computation());
            }
        }).take(500).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(500).assertNoErrors().assertComplete();
    }

    @Test
    public void asyncFlattenNone() {
        Flowable.range(1, 1000).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.<Integer>empty().subscribeOn(Schedulers.computation());
            }
        }).take(500).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void asyncFlattenNoneMaxConcurrency() {
        Flowable.range(1, 1000).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.<Integer>empty().subscribeOn(Schedulers.computation());
            }
        }, false, 128).take(500).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
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
    public void successError() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = Flowable.range(1, 2).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                if (v == 2) {
                    return pp.singleElement();
                }
                return Maybe.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).test();
        pp.onNext(1);
        pp.onComplete();
        ts.assertFailure(TestException.class, 1);
    }

    @Test
    public void completeError() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = Flowable.range(1, 2).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                if (v == 2) {
                    return pp.singleElement();
                }
                return Maybe.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).test();
        pp.onComplete();
        ts.assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Object> f) throws Exception {
                return f.flatMapMaybe(Functions.justFunction(Maybe.just(2)));
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
            }.flatMapMaybe(Functions.justFunction(Maybe.just(2))).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badInnerSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.just(1).flatMapMaybe(Functions.justFunction(new Maybe<Integer>() {

                @Override
                protected void subscribeActual(MaybeObserver<? super Integer> observer) {
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
        Flowable.just(pp1, pp2).flatMapMaybe(new Function<PublishProcessor<Integer>, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(PublishProcessor<Integer> v) throws Exception {
                return v.singleElement();
            }
        }).subscribe(ts);
        pp1.onNext(1);
        pp1.onComplete();
        ts.assertResult(1, 2);
    }

    @Test
    public void emissionQueueTrigger2() {
        final PublishProcessor<Integer> pp1 = PublishProcessor.create();
        final PublishProcessor<Integer> pp2 = PublishProcessor.create();
        final PublishProcessor<Integer> pp3 = PublishProcessor.create();
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
        Flowable.just(pp1, pp2, pp3).flatMapMaybe(new Function<PublishProcessor<Integer>, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(PublishProcessor<Integer> v) throws Exception {
                return v.singleElement();
            }
        }).subscribe(ts);
        pp1.onNext(1);
        pp1.onComplete();
        pp3.onComplete();
        ts.assertResult(1, 2);
    }

    @Test
    public void disposeInner() {
        final TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.just(1).flatMapMaybe(new Function<Integer, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Integer v) throws Exception {
                return new Maybe<Object>() {

                    @Override
                    protected void subscribeActual(MaybeObserver<? super Object> observer) {
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
        TestSubscriber<Integer> ts = Flowable.just(1).flatMapMaybe(Functions.justFunction(pp.singleElement())).test();
        pp.onNext(2);
        pp.onComplete();
        ts.assertResult(2);
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = Flowable.just(1).flatMapMaybe(Functions.justFunction(Maybe.just(2))).test(0L).assertEmpty();
        ts.request(1);
        ts.assertResult(2);
    }

    @Test
    public void error() {
        Flowable.just(1).flatMapMaybe(Functions.justFunction(Maybe.<Integer>error(new TestException()))).test(0L).assertFailure(TestException.class);
    }

    @Test
    public void errorDelayed() {
        Flowable.just(1).flatMapMaybe(Functions.justFunction(Maybe.<Integer>error(new TestException())), true, 16).test(0L).assertFailure(TestException.class);
    }

    @Test
    public void requestCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = Flowable.just(1).concatWith(Flowable.<Integer>never()).flatMapMaybe(Functions.justFunction(Maybe.just(2))).test(0);
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
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.flatMapMaybe(new Function<Integer, Maybe<Integer>>() {

                    @Override
                    public Maybe<Integer> apply(Integer v) throws Throwable {
                        return Maybe.just(v).hide();
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
                return upstream.flatMapMaybe(new Function<Integer, Maybe<Integer>>() {

                    @Override
                    public Maybe<Integer> apply(Integer v) throws Throwable {
                        return Maybe.just(v).hide();
                    }
                }, true, 2);
            }
        });
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().flatMapMaybe(v -> Maybe.never()));
    }

    @Test
    public void successRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            MaybeSubject<Integer> ss1 = MaybeSubject.create();
            MaybeSubject<Integer> ss2 = MaybeSubject.create();
            TestSubscriber<Integer> ts = Flowable.just(ss1, ss2).flatMapMaybe(v -> v).test();
            TestHelper.race(() -> ss1.onSuccess(1), () -> ss2.onSuccess(1));
            ts.assertResult(1, 1);
        }
    }

    @Test
    public void successCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            MaybeSubject<Integer> ss1 = MaybeSubject.create();
            MaybeSubject<Integer> ss2 = MaybeSubject.create();
            TestSubscriber<Integer> ts = Flowable.just(ss1, ss2).flatMapMaybe(v -> v).test();
            TestHelper.race(() -> ss1.onSuccess(1), () -> ss2.onComplete());
            ts.assertResult(1);
        }
    }

    @Test
    public void successShortcut() {
        MaybeSubject<Integer> ss1 = MaybeSubject.create();
        TestSubscriber<Integer> ts = Flowable.just(ss1).hide().flatMapMaybe(v -> v).test();
        ss1.onSuccess(1);
        ts.assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableFlatMapMaybeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmpty() throws java.lang.Throwable {
            this.payloads.normalEmpty.evaluate();
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
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFlatten() throws java.lang.Throwable {
            this.payloads.asyncFlatten.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFlattenNone() throws java.lang.Throwable {
            this.payloads.asyncFlattenNone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFlattenNoneMaxConcurrency() throws java.lang.Throwable {
            this.payloads.asyncFlattenNoneMaxConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFlattenErrorMaxConcurrency() throws java.lang.Throwable {
            this.payloads.asyncFlattenErrorMaxConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successError() throws java.lang.Throwable {
            this.payloads.successError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeError() throws java.lang.Throwable {
            this.payloads.completeError.evaluate();
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
        public void benchmark_emissionQueueTrigger2() throws java.lang.Throwable {
            this.payloads.emissionQueueTrigger2.evaluate();
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
        public void benchmark_successCompleteRace() throws java.lang.Throwable {
            this.payloads.successCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successShortcut() throws java.lang.Throwable {
            this.payloads.successShortcut.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapMaybeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapMaybeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapMaybeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapMaybeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableFlatMapMaybeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapMaybeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableFlatMapMaybeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableFlatMapMaybeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement normalEmpty;

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

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement asyncFlatten;

            public org.junit.runners.model.Statement asyncFlattenNone;

            public org.junit.runners.model.Statement asyncFlattenNoneMaxConcurrency;

            public org.junit.runners.model.Statement asyncFlattenErrorMaxConcurrency;

            public org.junit.runners.model.Statement successError;

            public org.junit.runners.model.Statement completeError;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement badInnerSource;

            public org.junit.runners.model.Statement emissionQueueTrigger;

            public org.junit.runners.model.Statement emissionQueueTrigger2;

            public org.junit.runners.model.Statement disposeInner;

            public org.junit.runners.model.Statement innerSuccessCompletesAfterMain;

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement errorDelayed;

            public org.junit.runners.model.Statement requestCancelRace;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement successRace;

            public org.junit.runners.model.Statement successCompleteRace;

            public org.junit.runners.model.Statement successShortcut;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::normal, "normal", this);
            this.payloads.normalEmpty = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::normalEmpty, "normalEmpty", this);
            this.payloads.normalDelayError = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::normalDelayError, "normalDelayError", this);
            this.payloads.normalAsync = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::normalAsync, "normalAsync", this);
            this.payloads.normalAsyncMaxConcurrency = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::normalAsyncMaxConcurrency, "normalAsyncMaxConcurrency", this);
            this.payloads.normalAsyncMaxConcurrency1 = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::normalAsyncMaxConcurrency1, "normalAsyncMaxConcurrency1", this);
            this.payloads.mapperThrowsFlowable = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::mapperThrowsFlowable, "mapperThrowsFlowable", this);
            this.payloads.mapperReturnsNullFlowable = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::mapperReturnsNullFlowable, "mapperReturnsNullFlowable", this);
            this.payloads.normalDelayErrorAll = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::normalDelayErrorAll, "normalDelayErrorAll", this);
            this.payloads.normalBackpressured = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::normalBackpressured, "normalBackpressured", this);
            this.payloads.normalMaxConcurrent1Backpressured = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::normalMaxConcurrent1Backpressured, "normalMaxConcurrent1Backpressured", this);
            this.payloads.normalMaxConcurrent2Backpressured = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::normalMaxConcurrent2Backpressured, "normalMaxConcurrent2Backpressured", this);
            this.payloads.takeAsync = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::takeAsync, "takeAsync", this);
            this.payloads.take = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::take, "take", this);
            this.payloads.middleError = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::middleError, "middleError", this);
            this.payloads.disposed = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::disposed, "disposed", this);
            this.payloads.asyncFlatten = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::asyncFlatten, "asyncFlatten", this);
            this.payloads.asyncFlattenNone = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::asyncFlattenNone, "asyncFlattenNone", this);
            this.payloads.asyncFlattenNoneMaxConcurrency = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::asyncFlattenNoneMaxConcurrency, "asyncFlattenNoneMaxConcurrency", this);
            this.payloads.asyncFlattenErrorMaxConcurrency = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::asyncFlattenErrorMaxConcurrency, "asyncFlattenErrorMaxConcurrency", this);
            this.payloads.successError = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::successError, "successError", this);
            this.payloads.completeError = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::completeError, "completeError", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::badSource, "badSource", this);
            this.payloads.badInnerSource = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::badInnerSource, "badInnerSource", this);
            this.payloads.emissionQueueTrigger = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::emissionQueueTrigger, "emissionQueueTrigger", this);
            this.payloads.emissionQueueTrigger2 = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::emissionQueueTrigger2, "emissionQueueTrigger2", this);
            this.payloads.disposeInner = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::disposeInner, "disposeInner", this);
            this.payloads.innerSuccessCompletesAfterMain = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::innerSuccessCompletesAfterMain, "innerSuccessCompletesAfterMain", this);
            this.payloads.backpressure = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::backpressure, "backpressure", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::error, "error", this);
            this.payloads.errorDelayed = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::errorDelayed, "errorDelayed", this);
            this.payloads.requestCancelRace = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::requestCancelRace, "requestCancelRace", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::badRequest, "badRequest", this);
            this.payloads.successRace = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::successRace, "successRace", this);
            this.payloads.successCompleteRace = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::successCompleteRace, "successCompleteRace", this);
            this.payloads.successShortcut = _ClassStatement.forPayload(FlowableFlatMapMaybeTest::successShortcut, "successShortcut", this);
        }
    }
}
