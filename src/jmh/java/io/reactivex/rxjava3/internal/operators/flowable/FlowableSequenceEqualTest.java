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

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.InOrder;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableSequenceEqualTest extends RxJavaTest {

    @Test
    public void flowable1() {
        Flowable<Boolean> flowable = Flowable.sequenceEqual(Flowable.just("one", "two", "three"), Flowable.just("one", "two", "three")).toFlowable();
        verifyResult(flowable, true);
    }

    @Test
    public void flowable2() {
        Flowable<Boolean> flowable = Flowable.sequenceEqual(Flowable.just("one", "two", "three"), Flowable.just("one", "two", "three", "four")).toFlowable();
        verifyResult(flowable, false);
    }

    @Test
    public void flowable3() {
        Flowable<Boolean> flowable = Flowable.sequenceEqual(Flowable.just("one", "two", "three", "four"), Flowable.just("one", "two", "three")).toFlowable();
        verifyResult(flowable, false);
    }

    @Test
    public void withError1Flowable() {
        Flowable<Boolean> flowable = Flowable.sequenceEqual(Flowable.concat(Flowable.just("one"), Flowable.<String>error(new TestException())), Flowable.just("one", "two", "three")).toFlowable();
        verifyError(flowable);
    }

    @Test
    public void withError2Flowable() {
        Flowable<Boolean> flowable = Flowable.sequenceEqual(Flowable.just("one", "two", "three"), Flowable.concat(Flowable.just("one"), Flowable.<String>error(new TestException()))).toFlowable();
        verifyError(flowable);
    }

    @Test
    public void withError3Flowable() {
        Flowable<Boolean> flowable = Flowable.sequenceEqual(Flowable.concat(Flowable.just("one"), Flowable.<String>error(new TestException())), Flowable.concat(Flowable.just("one"), Flowable.<String>error(new TestException()))).toFlowable();
        verifyError(flowable);
    }

    @Test
    public void withEmpty1Flowable() {
        Flowable<Boolean> flowable = Flowable.sequenceEqual(Flowable.<String>empty(), Flowable.just("one", "two", "three")).toFlowable();
        verifyResult(flowable, false);
    }

    @Test
    public void withEmpty2Flowable() {
        Flowable<Boolean> flowable = Flowable.sequenceEqual(Flowable.just("one", "two", "three"), Flowable.<String>empty()).toFlowable();
        verifyResult(flowable, false);
    }

    @Test
    public void withEmpty3Flowable() {
        Flowable<Boolean> flowable = Flowable.sequenceEqual(Flowable.<String>empty(), Flowable.<String>empty()).toFlowable();
        verifyResult(flowable, true);
    }

    @Test
    public void withEqualityErrorFlowable() {
        Flowable<Boolean> flowable = Flowable.sequenceEqual(Flowable.just("one"), Flowable.just("one"), new BiPredicate<String, String>() {

            @Override
            public boolean test(String t1, String t2) {
                throw new TestException();
            }
        }).toFlowable();
        verifyError(flowable);
    }

    @Test
    public void one() {
        Single<Boolean> single = Flowable.sequenceEqual(Flowable.just("one", "two", "three"), Flowable.just("one", "two", "three"));
        verifyResult(single, true);
    }

    @Test
    public void two() {
        Single<Boolean> single = Flowable.sequenceEqual(Flowable.just("one", "two", "three"), Flowable.just("one", "two", "three", "four"));
        verifyResult(single, false);
    }

    @Test
    public void three() {
        Single<Boolean> single = Flowable.sequenceEqual(Flowable.just("one", "two", "three", "four"), Flowable.just("one", "two", "three"));
        verifyResult(single, false);
    }

    @Test
    public void withError1() {
        Single<Boolean> single = Flowable.sequenceEqual(Flowable.concat(Flowable.just("one"), Flowable.<String>error(new TestException())), Flowable.just("one", "two", "three"));
        verifyError(single);
    }

    @Test
    public void withError2() {
        Single<Boolean> single = Flowable.sequenceEqual(Flowable.just("one", "two", "three"), Flowable.concat(Flowable.just("one"), Flowable.<String>error(new TestException())));
        verifyError(single);
    }

    @Test
    public void withError3() {
        Single<Boolean> single = Flowable.sequenceEqual(Flowable.concat(Flowable.just("one"), Flowable.<String>error(new TestException())), Flowable.concat(Flowable.just("one"), Flowable.<String>error(new TestException())));
        verifyError(single);
    }

    @Test
    public void withEmpty1() {
        Single<Boolean> single = Flowable.sequenceEqual(Flowable.<String>empty(), Flowable.just("one", "two", "three"));
        verifyResult(single, false);
    }

    @Test
    public void withEmpty2() {
        Single<Boolean> single = Flowable.sequenceEqual(Flowable.just("one", "two", "three"), Flowable.<String>empty());
        verifyResult(single, false);
    }

    @Test
    public void withEmpty3() {
        Single<Boolean> single = Flowable.sequenceEqual(Flowable.<String>empty(), Flowable.<String>empty());
        verifyResult(single, true);
    }

    @Test
    public void withEqualityError() {
        Single<Boolean> single = Flowable.sequenceEqual(Flowable.just("one"), Flowable.just("one"), new BiPredicate<String, String>() {

            @Override
            public boolean test(String t1, String t2) {
                throw new TestException();
            }
        });
        verifyError(single);
    }

    private void verifyResult(Flowable<Boolean> flowable, boolean result) {
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(result);
        inOrder.verify(subscriber).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    private void verifyResult(Single<Boolean> single, boolean result) {
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(result);
        inOrder.verifyNoMoreInteractions();
    }

    private void verifyError(Flowable<Boolean> flowable) {
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onError(isA(TestException.class));
        inOrder.verifyNoMoreInteractions();
    }

    private void verifyError(Single<Boolean> single) {
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(TestException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void prefetch() {
        Flowable.sequenceEqual(Flowable.range(1, 20), Flowable.range(1, 20), 2).test().assertResult(true);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Flowable.sequenceEqual(Flowable.just(1), Flowable.just(2)));
    }

    @Test
    public void simpleInequal() {
        Flowable.sequenceEqual(Flowable.just(1), Flowable.just(2)).test().assertResult(false);
    }

    @Test
    public void simpleInequalObservable() {
        Flowable.sequenceEqual(Flowable.just(1), Flowable.just(2)).toFlowable().test().assertResult(false);
    }

    @Test
    public void onNextCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final TestObserver<Boolean> to = Flowable.sequenceEqual(Flowable.never(), pp).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            TestHelper.race(r1, r2);
            to.assertEmpty();
        }
    }

    @Test
    public void onNextCancelRaceObservable() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final TestSubscriber<Boolean> ts = Flowable.sequenceEqual(Flowable.never(), pp).toFlowable().test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            TestHelper.race(r1, r2);
            ts.assertEmpty();
        }
    }

    @Test
    public void disposedFlowable() {
        TestHelper.checkDisposed(Flowable.sequenceEqual(Flowable.just(1), Flowable.just(2)).toFlowable());
    }

    @Test
    public void prefetchFlowable() {
        Flowable.sequenceEqual(Flowable.range(1, 20), Flowable.range(1, 20), 2).toFlowable().test().assertResult(true);
    }

    @Test
    public void longSequenceEqualsFlowable() {
        Flowable<Integer> source = Flowable.range(1, Flowable.bufferSize() * 4).subscribeOn(Schedulers.computation());
        Flowable.sequenceEqual(source, source).toFlowable().test().awaitDone(5, TimeUnit.SECONDS).assertResult(true);
    }

    @Test
    public void syncFusedCrashFlowable() {
        Flowable<Integer> source = Flowable.range(1, 10).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                throw new TestException();
            }
        });
        Flowable.sequenceEqual(source, Flowable.range(1, 10).hide()).toFlowable().test().assertFailure(TestException.class);
        Flowable.sequenceEqual(Flowable.range(1, 10).hide(), source).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void cancelAndDrainRaceFlowable() {
        Flowable<Object> neverNever = new Flowable<Object>() {

            @Override
            protected void subscribeActual(Subscriber<? super Object> s) {
            }
        };
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Boolean> ts = new TestSubscriber<>();
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            boolean swap = (i & 1) == 0;
            Flowable.sequenceEqual(swap ? pp : neverNever, swap ? neverNever : pp).toFlowable().subscribe(ts);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            TestHelper.race(r1, r2);
            ts.assertEmpty();
        }
    }

    @Test
    public void sourceOverflowsFlowable() {
        Flowable.sequenceEqual(Flowable.never(), new Flowable<Object>() {

            @Override
            protected void subscribeActual(Subscriber<? super Object> s) {
                s.onSubscribe(new BooleanSubscription());
                for (int i = 0; i < 10; i++) {
                    s.onNext(i);
                }
            }
        }, 8).toFlowable().test().assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void doubleErrorFlowable() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.sequenceEqual(Flowable.never(), new Flowable<Object>() {

                @Override
                protected void subscribeActual(Subscriber<? super Object> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onError(new TestException("First"));
                    s.onError(new TestException("Second"));
                }
            }, 8).toFlowable().to(TestHelper.<Boolean>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void longSequenceEquals() {
        Flowable<Integer> source = Flowable.range(1, Flowable.bufferSize() * 4).subscribeOn(Schedulers.computation());
        Flowable.sequenceEqual(source, source).test().awaitDone(5, TimeUnit.SECONDS).assertResult(true);
    }

    @Test
    public void syncFusedCrash() {
        Flowable<Integer> source = Flowable.range(1, 10).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                throw new TestException();
            }
        });
        Flowable.sequenceEqual(source, Flowable.range(1, 10).hide()).test().assertFailure(TestException.class);
        Flowable.sequenceEqual(Flowable.range(1, 10).hide(), source).test().assertFailure(TestException.class);
    }

    @Test
    public void cancelAndDrainRace() {
        Flowable<Object> neverNever = new Flowable<Object>() {

            @Override
            protected void subscribeActual(Subscriber<? super Object> s) {
            }
        };
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestObserver<Boolean> to = new TestObserver<>();
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            boolean swap = (i & 1) == 0;
            Flowable.sequenceEqual(swap ? pp : neverNever, swap ? neverNever : pp).subscribe(to);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
            to.assertEmpty();
        }
    }

    @Test
    public void sourceOverflows() {
        Flowable.sequenceEqual(Flowable.never(), new Flowable<Object>() {

            @Override
            protected void subscribeActual(Subscriber<? super Object> s) {
                s.onSubscribe(new BooleanSubscription());
                for (int i = 0; i < 10; i++) {
                    s.onNext(i);
                }
            }
        }, 8).test().assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void doubleError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.sequenceEqual(Flowable.never(), new Flowable<Object>() {

                @Override
                protected void subscribeActual(Subscriber<? super Object> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onError(new TestException("First"));
                    s.onError(new TestException("Second"));
                }
            }, 8).to(TestHelper.<Boolean>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Single<Boolean>>() {

            @Override
            public Single<Boolean> apply(Flowable<Integer> upstream) {
                return Flowable.sequenceEqual(Flowable.just(1).hide(), upstream);
            }
        });
    }

    @Test
    public void undeliverableUponCancelAsFlowable() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Boolean>>() {

            @Override
            public Flowable<Boolean> apply(Flowable<Integer> upstream) {
                return Flowable.sequenceEqual(Flowable.just(1).hide(), upstream).toFlowable();
            }
        });
    }

    @Test
    public void undeliverableUponCancel2() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Single<Boolean>>() {

            @Override
            public Single<Boolean> apply(Flowable<Integer> upstream) {
                return Flowable.sequenceEqual(upstream, Flowable.just(1).hide());
            }
        });
    }

    @Test
    public void undeliverableUponCancelAsFlowable2() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Boolean>>() {

            @Override
            public Flowable<Boolean> apply(Flowable<Integer> upstream) {
                return Flowable.sequenceEqual(upstream, Flowable.just(1).hide()).toFlowable();
            }
        });
    }

    @Test
    public void fusionRejected() {
        Flowable.sequenceEqual(TestHelper.rejectFlowableFusion(), Flowable.never()).test().assertEmpty();
    }

    @Test
    public void fusionRejectedFlowable() {
        Flowable.sequenceEqual(TestHelper.rejectFlowableFusion(), Flowable.never()).toFlowable().test().assertEmpty();
    }

    @Test
    public void asyncSourceCompare() {
        Flowable.sequenceEqual(Flowable.fromCallable(() -> 1), Flowable.just(1)).test().assertResult(true);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableSequenceEqualTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowable1() throws java.lang.Throwable {
            this.payloads.flowable1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowable2() throws java.lang.Throwable {
            this.payloads.flowable2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowable3() throws java.lang.Throwable {
            this.payloads.flowable3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError1Flowable() throws java.lang.Throwable {
            this.payloads.withError1Flowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError2Flowable() throws java.lang.Throwable {
            this.payloads.withError2Flowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError3Flowable() throws java.lang.Throwable {
            this.payloads.withError3Flowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty1Flowable() throws java.lang.Throwable {
            this.payloads.withEmpty1Flowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty2Flowable() throws java.lang.Throwable {
            this.payloads.withEmpty2Flowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty3Flowable() throws java.lang.Throwable {
            this.payloads.withEmpty3Flowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEqualityErrorFlowable() throws java.lang.Throwable {
            this.payloads.withEqualityErrorFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_one() throws java.lang.Throwable {
            this.payloads.one.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_two() throws java.lang.Throwable {
            this.payloads.two.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_three() throws java.lang.Throwable {
            this.payloads.three.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError1() throws java.lang.Throwable {
            this.payloads.withError1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError2() throws java.lang.Throwable {
            this.payloads.withError2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError3() throws java.lang.Throwable {
            this.payloads.withError3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty1() throws java.lang.Throwable {
            this.payloads.withEmpty1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty2() throws java.lang.Throwable {
            this.payloads.withEmpty2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty3() throws java.lang.Throwable {
            this.payloads.withEmpty3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEqualityError() throws java.lang.Throwable {
            this.payloads.withEqualityError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_prefetch() throws java.lang.Throwable {
            this.payloads.prefetch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleInequal() throws java.lang.Throwable {
            this.payloads.simpleInequal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleInequalObservable() throws java.lang.Throwable {
            this.payloads.simpleInequalObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextCancelRace() throws java.lang.Throwable {
            this.payloads.onNextCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextCancelRaceObservable() throws java.lang.Throwable {
            this.payloads.onNextCancelRaceObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedFlowable() throws java.lang.Throwable {
            this.payloads.disposedFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_prefetchFlowable() throws java.lang.Throwable {
            this.payloads.prefetchFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_longSequenceEqualsFlowable() throws java.lang.Throwable {
            this.payloads.longSequenceEqualsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedCrashFlowable() throws java.lang.Throwable {
            this.payloads.syncFusedCrashFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAndDrainRaceFlowable() throws java.lang.Throwable {
            this.payloads.cancelAndDrainRaceFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceOverflowsFlowable() throws java.lang.Throwable {
            this.payloads.sourceOverflowsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleErrorFlowable() throws java.lang.Throwable {
            this.payloads.doubleErrorFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_longSequenceEquals() throws java.lang.Throwable {
            this.payloads.longSequenceEquals.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedCrash() throws java.lang.Throwable {
            this.payloads.syncFusedCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAndDrainRace() throws java.lang.Throwable {
            this.payloads.cancelAndDrainRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceOverflows() throws java.lang.Throwable {
            this.payloads.sourceOverflows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleError() throws java.lang.Throwable {
            this.payloads.doubleError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelAsFlowable() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancelAsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel2() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancel2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelAsFlowable2() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancelAsFlowable2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejected() throws java.lang.Throwable {
            this.payloads.fusionRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejectedFlowable() throws java.lang.Throwable {
            this.payloads.fusionRejectedFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncSourceCompare() throws java.lang.Throwable {
            this.payloads.asyncSourceCompare.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSequenceEqualTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSequenceEqualTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSequenceEqualTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSequenceEqualTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableSequenceEqualTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSequenceEqualTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableSequenceEqualTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableSequenceEqualTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement flowable1;

            public org.junit.runners.model.Statement flowable2;

            public org.junit.runners.model.Statement flowable3;

            public org.junit.runners.model.Statement withError1Flowable;

            public org.junit.runners.model.Statement withError2Flowable;

            public org.junit.runners.model.Statement withError3Flowable;

            public org.junit.runners.model.Statement withEmpty1Flowable;

            public org.junit.runners.model.Statement withEmpty2Flowable;

            public org.junit.runners.model.Statement withEmpty3Flowable;

            public org.junit.runners.model.Statement withEqualityErrorFlowable;

            public org.junit.runners.model.Statement one;

            public org.junit.runners.model.Statement two;

            public org.junit.runners.model.Statement three;

            public org.junit.runners.model.Statement withError1;

            public org.junit.runners.model.Statement withError2;

            public org.junit.runners.model.Statement withError3;

            public org.junit.runners.model.Statement withEmpty1;

            public org.junit.runners.model.Statement withEmpty2;

            public org.junit.runners.model.Statement withEmpty3;

            public org.junit.runners.model.Statement withEqualityError;

            public org.junit.runners.model.Statement prefetch;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement simpleInequal;

            public org.junit.runners.model.Statement simpleInequalObservable;

            public org.junit.runners.model.Statement onNextCancelRace;

            public org.junit.runners.model.Statement onNextCancelRaceObservable;

            public org.junit.runners.model.Statement disposedFlowable;

            public org.junit.runners.model.Statement prefetchFlowable;

            public org.junit.runners.model.Statement longSequenceEqualsFlowable;

            public org.junit.runners.model.Statement syncFusedCrashFlowable;

            public org.junit.runners.model.Statement cancelAndDrainRaceFlowable;

            public org.junit.runners.model.Statement sourceOverflowsFlowable;

            public org.junit.runners.model.Statement doubleErrorFlowable;

            public org.junit.runners.model.Statement longSequenceEquals;

            public org.junit.runners.model.Statement syncFusedCrash;

            public org.junit.runners.model.Statement cancelAndDrainRace;

            public org.junit.runners.model.Statement sourceOverflows;

            public org.junit.runners.model.Statement doubleError;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelAsFlowable;

            public org.junit.runners.model.Statement undeliverableUponCancel2;

            public org.junit.runners.model.Statement undeliverableUponCancelAsFlowable2;

            public org.junit.runners.model.Statement fusionRejected;

            public org.junit.runners.model.Statement fusionRejectedFlowable;

            public org.junit.runners.model.Statement asyncSourceCompare;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.flowable1 = _ClassStatement.forPayload(FlowableSequenceEqualTest::flowable1, "flowable1", this);
            this.payloads.flowable2 = _ClassStatement.forPayload(FlowableSequenceEqualTest::flowable2, "flowable2", this);
            this.payloads.flowable3 = _ClassStatement.forPayload(FlowableSequenceEqualTest::flowable3, "flowable3", this);
            this.payloads.withError1Flowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::withError1Flowable, "withError1Flowable", this);
            this.payloads.withError2Flowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::withError2Flowable, "withError2Flowable", this);
            this.payloads.withError3Flowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::withError3Flowable, "withError3Flowable", this);
            this.payloads.withEmpty1Flowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::withEmpty1Flowable, "withEmpty1Flowable", this);
            this.payloads.withEmpty2Flowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::withEmpty2Flowable, "withEmpty2Flowable", this);
            this.payloads.withEmpty3Flowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::withEmpty3Flowable, "withEmpty3Flowable", this);
            this.payloads.withEqualityErrorFlowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::withEqualityErrorFlowable, "withEqualityErrorFlowable", this);
            this.payloads.one = _ClassStatement.forPayload(FlowableSequenceEqualTest::one, "one", this);
            this.payloads.two = _ClassStatement.forPayload(FlowableSequenceEqualTest::two, "two", this);
            this.payloads.three = _ClassStatement.forPayload(FlowableSequenceEqualTest::three, "three", this);
            this.payloads.withError1 = _ClassStatement.forPayload(FlowableSequenceEqualTest::withError1, "withError1", this);
            this.payloads.withError2 = _ClassStatement.forPayload(FlowableSequenceEqualTest::withError2, "withError2", this);
            this.payloads.withError3 = _ClassStatement.forPayload(FlowableSequenceEqualTest::withError3, "withError3", this);
            this.payloads.withEmpty1 = _ClassStatement.forPayload(FlowableSequenceEqualTest::withEmpty1, "withEmpty1", this);
            this.payloads.withEmpty2 = _ClassStatement.forPayload(FlowableSequenceEqualTest::withEmpty2, "withEmpty2", this);
            this.payloads.withEmpty3 = _ClassStatement.forPayload(FlowableSequenceEqualTest::withEmpty3, "withEmpty3", this);
            this.payloads.withEqualityError = _ClassStatement.forPayload(FlowableSequenceEqualTest::withEqualityError, "withEqualityError", this);
            this.payloads.prefetch = _ClassStatement.forPayload(FlowableSequenceEqualTest::prefetch, "prefetch", this);
            this.payloads.disposed = _ClassStatement.forPayload(FlowableSequenceEqualTest::disposed, "disposed", this);
            this.payloads.simpleInequal = _ClassStatement.forPayload(FlowableSequenceEqualTest::simpleInequal, "simpleInequal", this);
            this.payloads.simpleInequalObservable = _ClassStatement.forPayload(FlowableSequenceEqualTest::simpleInequalObservable, "simpleInequalObservable", this);
            this.payloads.onNextCancelRace = _ClassStatement.forPayload(FlowableSequenceEqualTest::onNextCancelRace, "onNextCancelRace", this);
            this.payloads.onNextCancelRaceObservable = _ClassStatement.forPayload(FlowableSequenceEqualTest::onNextCancelRaceObservable, "onNextCancelRaceObservable", this);
            this.payloads.disposedFlowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::disposedFlowable, "disposedFlowable", this);
            this.payloads.prefetchFlowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::prefetchFlowable, "prefetchFlowable", this);
            this.payloads.longSequenceEqualsFlowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::longSequenceEqualsFlowable, "longSequenceEqualsFlowable", this);
            this.payloads.syncFusedCrashFlowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::syncFusedCrashFlowable, "syncFusedCrashFlowable", this);
            this.payloads.cancelAndDrainRaceFlowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::cancelAndDrainRaceFlowable, "cancelAndDrainRaceFlowable", this);
            this.payloads.sourceOverflowsFlowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::sourceOverflowsFlowable, "sourceOverflowsFlowable", this);
            this.payloads.doubleErrorFlowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::doubleErrorFlowable, "doubleErrorFlowable", this);
            this.payloads.longSequenceEquals = _ClassStatement.forPayload(FlowableSequenceEqualTest::longSequenceEquals, "longSequenceEquals", this);
            this.payloads.syncFusedCrash = _ClassStatement.forPayload(FlowableSequenceEqualTest::syncFusedCrash, "syncFusedCrash", this);
            this.payloads.cancelAndDrainRace = _ClassStatement.forPayload(FlowableSequenceEqualTest::cancelAndDrainRace, "cancelAndDrainRace", this);
            this.payloads.sourceOverflows = _ClassStatement.forPayload(FlowableSequenceEqualTest::sourceOverflows, "sourceOverflows", this);
            this.payloads.doubleError = _ClassStatement.forPayload(FlowableSequenceEqualTest::doubleError, "doubleError", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(FlowableSequenceEqualTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelAsFlowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::undeliverableUponCancelAsFlowable, "undeliverableUponCancelAsFlowable", this);
            this.payloads.undeliverableUponCancel2 = _ClassStatement.forPayload(FlowableSequenceEqualTest::undeliverableUponCancel2, "undeliverableUponCancel2", this);
            this.payloads.undeliverableUponCancelAsFlowable2 = _ClassStatement.forPayload(FlowableSequenceEqualTest::undeliverableUponCancelAsFlowable2, "undeliverableUponCancelAsFlowable2", this);
            this.payloads.fusionRejected = _ClassStatement.forPayload(FlowableSequenceEqualTest::fusionRejected, "fusionRejected", this);
            this.payloads.fusionRejectedFlowable = _ClassStatement.forPayload(FlowableSequenceEqualTest::fusionRejectedFlowable, "fusionRejectedFlowable", this);
            this.payloads.asyncSourceCompare = _ClassStatement.forPayload(FlowableSequenceEqualTest::asyncSourceCompare, "asyncSourceCompare", this);
        }
    }
}
