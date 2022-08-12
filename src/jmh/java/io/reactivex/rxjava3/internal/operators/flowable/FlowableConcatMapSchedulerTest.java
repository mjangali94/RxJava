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
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.schedulers.ImmediateThinScheduler;
import io.reactivex.rxjava3.internal.subscriptions.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableConcatMapSchedulerTest extends RxJavaTest {

    @Test
    public void boundaryFusion() {
        Flowable.range(1, 10000).observeOn(Schedulers.single()).map(new Function<Integer, String>() {

            @Override
            public String apply(Integer t) throws Exception {
                String name = Thread.currentThread().getName();
                if (name.contains("RxSingleScheduler")) {
                    return "RxSingleScheduler";
                }
                return name;
            }
        }).concatMap(new Function<String, Publisher<? extends Object>>() {

            @Override
            public Publisher<? extends Object> apply(String v) throws Exception {
                return Flowable.just(v);
            }
        }, 2, ImmediateThinScheduler.INSTANCE).observeOn(Schedulers.computation()).distinct().test().awaitDone(5, TimeUnit.SECONDS).assertResult("RxSingleScheduler");
    }

    @Test
    public void innerScalarRequestRace() {
        Flowable<Integer> just = Flowable.just(1);
        int n = 1000;
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishProcessor<Flowable<Integer>> source = PublishProcessor.create();
            TestSubscriber<Integer> ts = source.concatMap(v -> v, n + 1, ImmediateThinScheduler.INSTANCE).test(1L);
            TestHelper.race(() -> {
                for (int j = 0; j < n; j++) {
                    source.onNext(just);
                }
            }, () -> {
                for (int j = 0; j < n; j++) {
                    ts.request(1);
                }
            });
            ts.assertValueCount(n);
        }
    }

    @Test
    public void innerScalarRequestRaceDelayError() {
        Flowable<Integer> just = Flowable.just(1);
        int n = 1000;
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishProcessor<Flowable<Integer>> source = PublishProcessor.create();
            TestSubscriber<Integer> ts = source.concatMapDelayError(v -> v, true, n + 1, ImmediateThinScheduler.INSTANCE).test(1L);
            TestHelper.race(() -> {
                for (int j = 0; j < n; j++) {
                    source.onNext(just);
                }
            }, () -> {
                for (int j = 0; j < n; j++) {
                    ts.request(1);
                }
            });
            ts.assertValueCount(n);
        }
    }

    @Test
    public void boundaryFusionDelayError() {
        Flowable.range(1, 10000).observeOn(Schedulers.single()).map(new Function<Integer, String>() {

            @Override
            public String apply(Integer t) throws Exception {
                String name = Thread.currentThread().getName();
                if (name.contains("RxSingleScheduler")) {
                    return "RxSingleScheduler";
                }
                return name;
            }
        }).concatMapDelayError(new Function<String, Publisher<? extends Object>>() {

            @Override
            public Publisher<? extends Object> apply(String v) throws Exception {
                return Flowable.just(v);
            }
        }, true, 2, ImmediateThinScheduler.INSTANCE).observeOn(Schedulers.computation()).distinct().test().awaitDone(5, TimeUnit.SECONDS).assertResult("RxSingleScheduler");
    }

    @Test
    public void pollThrows() {
        Flowable.just(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).compose(TestHelper.<Integer>flowableStripBoundary()).concatMap(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.just(v);
            }
        }, 2, ImmediateThinScheduler.INSTANCE).test().assertFailure(TestException.class);
    }

    @Test
    public void pollThrowsDelayError() {
        Flowable.just(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).compose(TestHelper.<Integer>flowableStripBoundary()).concatMapDelayError(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.just(v);
            }
        }, true, 2, ImmediateThinScheduler.INSTANCE).test().assertFailure(TestException.class);
    }

    @Test
    public void noCancelPrevious() {
        final AtomicInteger counter = new AtomicInteger();
        Flowable.range(1, 5).concatMap(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) throws Exception {
                return Flowable.just(v).doOnCancel(new Action() {

                    @Override
                    public void run() throws Exception {
                        counter.getAndIncrement();
                    }
                });
            }
        }, 2, ImmediateThinScheduler.INSTANCE).test().assertResult(1, 2, 3, 4, 5);
        assertEquals(0, counter.get());
    }

    @Test
    public void delayErrorCallableTillTheEnd() {
        Flowable.just(1, 2, 3, 101, 102, 23, 890, 120, 32).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(final Integer integer) throws Exception {
                return Flowable.fromCallable(new Callable<Integer>() {

                    @Override
                    public Integer call() throws Exception {
                        if (integer >= 100) {
                            throw new NullPointerException("test null exp");
                        }
                        return integer;
                    }
                });
            }
        }, true, 2, ImmediateThinScheduler.INSTANCE).test().assertFailure(CompositeException.class, 1, 2, 3, 23, 32);
    }

    @Test
    public void delayErrorCallableEager() {
        Flowable.just(1, 2, 3, 101, 102, 23, 890, 120, 32).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(final Integer integer) throws Exception {
                return Flowable.fromCallable(new Callable<Integer>() {

                    @Override
                    public Integer call() throws Exception {
                        if (integer >= 100) {
                            throw new NullPointerException("test null exp");
                        }
                        return integer;
                    }
                });
            }
        }, false, 2, ImmediateThinScheduler.INSTANCE).test().assertFailure(NullPointerException.class, 1, 2, 3);
    }

    @Test
    public void mapperScheduled() {
        TestSubscriber<String> ts = Flowable.just(1).concatMap(new Function<Integer, Flowable<String>>() {

            @Override
            public Flowable<String> apply(Integer t) throws Throwable {
                return Flowable.just(Thread.currentThread().getName());
            }
        }, 2, Schedulers.single()).test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(1).assertNoErrors().assertComplete();
        assertTrue(ts.values().toString(), ts.values().get(0).startsWith("RxSingleScheduler-"));
    }

    @Test
    public void mapperScheduledHidden() {
        TestSubscriber<String> ts = Flowable.just(1).concatMap(new Function<Integer, Flowable<String>>() {

            @Override
            public Flowable<String> apply(Integer t) throws Throwable {
                return Flowable.just(Thread.currentThread().getName()).hide();
            }
        }, 2, Schedulers.single()).test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(1).assertNoErrors().assertComplete();
        assertTrue(ts.values().toString(), ts.values().get(0).startsWith("RxSingleScheduler-"));
    }

    @Test
    public void mapperDelayErrorScheduled() {
        TestSubscriber<String> ts = Flowable.just(1).concatMapDelayError(new Function<Integer, Flowable<String>>() {

            @Override
            public Flowable<String> apply(Integer t) throws Throwable {
                return Flowable.just(Thread.currentThread().getName());
            }
        }, false, 2, Schedulers.single()).test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(1).assertNoErrors().assertComplete();
        assertTrue(ts.values().toString(), ts.values().get(0).startsWith("RxSingleScheduler-"));
    }

    @Test
    public void mapperDelayErrorScheduledHidden() {
        TestSubscriber<String> ts = Flowable.just(1).concatMapDelayError(new Function<Integer, Flowable<String>>() {

            @Override
            public Flowable<String> apply(Integer t) throws Throwable {
                return Flowable.just(Thread.currentThread().getName()).hide();
            }
        }, false, 2, Schedulers.single()).test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(1).assertNoErrors().assertComplete();
        assertTrue(ts.values().toString(), ts.values().get(0).startsWith("RxSingleScheduler-"));
    }

    @Test
    public void mapperDelayError2Scheduled() {
        TestSubscriber<String> ts = Flowable.just(1).concatMapDelayError(new Function<Integer, Flowable<String>>() {

            @Override
            public Flowable<String> apply(Integer t) throws Throwable {
                return Flowable.just(Thread.currentThread().getName());
            }
        }, true, 2, Schedulers.single()).test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(1).assertNoErrors().assertComplete();
        assertTrue(ts.values().toString(), ts.values().get(0).startsWith("RxSingleScheduler-"));
    }

    @Test
    public void mapperDelayError2ScheduledHidden() {
        TestSubscriber<String> ts = Flowable.just(1).concatMapDelayError(new Function<Integer, Flowable<String>>() {

            @Override
            public Flowable<String> apply(Integer t) throws Throwable {
                return Flowable.just(Thread.currentThread().getName()).hide();
            }
        }, true, 2, Schedulers.single()).test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(1).assertNoErrors().assertComplete();
        assertTrue(ts.values().toString(), ts.values().get(0).startsWith("RxSingleScheduler-"));
    }

    @Test
    public void issue2890NoStackoverflow() throws InterruptedException, TimeoutException {
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final Scheduler sch = Schedulers.from(executor);
        Function<Integer, Flowable<Integer>> func = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t) {
                Flowable<Integer> flowable = Flowable.just(t).subscribeOn(sch);
                FlowableProcessor<Integer> processor = UnicastProcessor.create();
                flowable.subscribe(processor);
                return processor;
            }
        };
        int n = 5000;
        final AtomicInteger counter = new AtomicInteger();
        Flowable.range(1, n).concatMap(func, 2, ImmediateThinScheduler.INSTANCE).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                // Consume after sleep for 1 ms
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                // ignored
                }
                if (counter.getAndIncrement() % 100 == 0) {
                    // System.out.print("testIssue2890NoStackoverflow -> ");
                    // System.out.println(counter.get());
                }
                ;
            }

            @Override
            public void onComplete() {
                executor.shutdown();
            }

            @Override
            public void onError(Throwable e) {
                executor.shutdown();
            }
        });
        long awaitTerminationTimeoutMillis = 100_000;
        if (!executor.awaitTermination(awaitTerminationTimeoutMillis, TimeUnit.MILLISECONDS)) {
            throw new TimeoutException("Completed " + counter.get() + "/" + n + " before timed out after " + awaitTerminationTimeoutMillis + " milliseconds.");
        }
        assertEquals(n, counter.get());
    }

    @Test
    public void concatMapRangeAsyncLoopIssue2876() {
        final long durationSeconds = 2;
        final long startTime = System.currentTimeMillis();
        for (int i = 0; ; i++) {
            // only run this for a max of ten seconds
            if (System.currentTimeMillis() - startTime > TimeUnit.SECONDS.toMillis(durationSeconds)) {
                return;
            }
            if (i % 1000 == 0) {
                // System.out.println("concatMapRangeAsyncLoop > " + i);
            }
            TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
            Flowable.range(0, 1000).concatMap(new Function<Integer, Flowable<Integer>>() {

                @Override
                public Flowable<Integer> apply(Integer t) {
                    return Flowable.fromIterable(Arrays.asList(t));
                }
            }, 2, ImmediateThinScheduler.INSTANCE).observeOn(Schedulers.computation()).subscribe(ts);
            ts.awaitDone(2500, TimeUnit.MILLISECONDS);
            ts.assertTerminated();
            ts.assertNoErrors();
            assertEquals(1000, ts.values().size());
            assertEquals((Integer) 999, ts.values().get(999));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void concatArray() throws Exception {
        for (int i = 2; i < 10; i++) {
            Flowable<Integer>[] obs = new Flowable[i];
            Arrays.fill(obs, Flowable.just(1));
            Integer[] expected = new Integer[i];
            Arrays.fill(expected, 1);
            Method m = Flowable.class.getMethod("concatArray", Publisher[].class);
            TestSubscriber<Integer> ts = TestSubscriber.create();
            ((Flowable<Integer>) m.invoke(null, new Object[] { obs })).subscribe(ts);
            ts.assertValues(expected);
            ts.assertNoErrors();
            ts.assertComplete();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void concatMapJustJust() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(Flowable.just(1)).concatMap((Function) Functions.identity(), 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void concatMapJustRange() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(Flowable.range(1, 5)).concatMap((Function) Functions.identity(), 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        ts.assertValues(1, 2, 3, 4, 5);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void concatMapDelayErrorJustJust() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(Flowable.just(1)).concatMapDelayError((Function) Functions.identity(), true, 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void concatMapDelayErrorJustRange() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(Flowable.range(1, 5)).concatMapDelayError((Function) Functions.identity(), true, 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        ts.assertValues(1, 2, 3, 4, 5);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void startWithArray() throws Exception {
        for (int i = 2; i < 10; i++) {
            Object[] obs = new Object[i];
            Arrays.fill(obs, 1);
            Integer[] expected = new Integer[i];
            Arrays.fill(expected, 1);
            Method m = Flowable.class.getMethod("startWithArray", Object[].class);
            TestSubscriber<Integer> ts = TestSubscriber.create();
            ((Flowable<Integer>) m.invoke(Flowable.empty(), new Object[] { obs })).subscribe(ts);
            ts.assertValues(expected);
            ts.assertNoErrors();
            ts.assertComplete();
        }
    }

    @Test
    public void concatMapDelayError() {
        Flowable.just(Flowable.just(1), Flowable.just(2)).concatMapDelayError(Functions.<Flowable<Integer>>identity(), true, 2, ImmediateThinScheduler.INSTANCE).test().assertResult(1, 2);
    }

    @Test
    public void concatMapDelayErrorJustSource() {
        Flowable.just(0).concatMapDelayError(new Function<Object, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Object v) throws Exception {
                return Flowable.just(1);
            }
        }, true, 16, ImmediateThinScheduler.INSTANCE).test().assertResult(1);
    }

    @Test
    public void concatMapJustSource() {
        Flowable.just(0).hide().concatMap(new Function<Object, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Object v) throws Exception {
                return Flowable.just(1);
            }
        }, 16, ImmediateThinScheduler.INSTANCE).test().assertResult(1);
    }

    @Test
    public void concatMapJustSourceDelayError() {
        Flowable.just(0).hide().concatMapDelayError(new Function<Object, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Object v) throws Exception {
                return Flowable.just(1);
            }
        }, false, 16, ImmediateThinScheduler.INSTANCE).test().assertResult(1);
    }

    @Test
    public void concatMapScalarBackpressured() {
        Flowable.just(1).hide().concatMap(Functions.justFunction(Flowable.just(2)), 2, ImmediateThinScheduler.INSTANCE).test(1L).assertResult(2);
    }

    @Test
    public void concatMapScalarBackpressuredDelayError() {
        Flowable.just(1).hide().concatMapDelayError(Functions.justFunction(Flowable.just(2)), true, 2, ImmediateThinScheduler.INSTANCE).test(1L).assertResult(2);
    }

    @Test
    public void concatMapEmpty() {
        Flowable.just(1).hide().concatMap(Functions.justFunction(Flowable.empty()), 2, ImmediateThinScheduler.INSTANCE).test().assertResult();
    }

    @Test
    public void concatMapEmptyDelayError() {
        Flowable.just(1).hide().concatMapDelayError(Functions.justFunction(Flowable.empty()), true, 2, ImmediateThinScheduler.INSTANCE).test().assertResult();
    }

    @Test
    public void ignoreBackpressure() {
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                for (int i = 0; i < 10; i++) {
                    s.onNext(i);
                }
            }
        }.concatMap(Functions.justFunction(Flowable.just(2)), 8, ImmediateThinScheduler.INSTANCE).test(0L).assertFailure(IllegalStateException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Object> f) throws Exception {
                return f.concatMap(Functions.justFunction(Flowable.just(2)), 2, ImmediateThinScheduler.INSTANCE);
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Object> f) throws Exception {
                return f.concatMapDelayError(Functions.justFunction(Flowable.just(2)), true, 2, ImmediateThinScheduler.INSTANCE);
            }
        });
    }

    @Test
    public void immediateInnerNextOuterError() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    pp.onError(new TestException("First"));
                }
            }
        };
        pp.concatMap(Functions.justFunction(Flowable.just(1)), 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        pp.onNext(1);
        assertFalse(pp.hasSubscribers());
        ts.assertFailureAndMessage(TestException.class, "First", 1);
    }

    @Test
    public void immediateInnerNextOuterError2() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    pp.onError(new TestException("First"));
                }
            }
        };
        pp.concatMap(Functions.justFunction(Flowable.just(1).hide()), 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        pp.onNext(1);
        assertFalse(pp.hasSubscribers());
        ts.assertFailureAndMessage(TestException.class, "First", 1);
    }

    @Test
    public void concatMapInnerError() {
        Flowable.just(1).hide().concatMap(Functions.justFunction(Flowable.error(new TestException())), 2, ImmediateThinScheduler.INSTANCE).test().assertFailure(TestException.class);
    }

    @Test
    public void concatMapInnerErrorDelayError() {
        Flowable.just(1).hide().concatMapDelayError(Functions.justFunction(Flowable.error(new TestException())), true, 2, ImmediateThinScheduler.INSTANCE).test().assertFailure(TestException.class);
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.concatMap(Functions.justFunction(Flowable.just(1).hide()), 2, ImmediateThinScheduler.INSTANCE);
            }
        }, true, 1, 1, 1);
    }

    @Test
    public void badInnerSource() {
        @SuppressWarnings("rawtypes")
        final Subscriber[] ts0 = { null };
        TestSubscriberEx<Integer> ts = Flowable.just(1).hide().concatMap(Functions.justFunction(new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                ts0[0] = s;
                s.onSubscribe(new BooleanSubscription());
                s.onError(new TestException("First"));
            }
        }), 2, ImmediateThinScheduler.INSTANCE).to(TestHelper.<Integer>testConsumer());
        ts.assertFailureAndMessage(TestException.class, "First");
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            ts0[0].onError(new TestException("Second"));
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badInnerSourceDelayError() {
        @SuppressWarnings("rawtypes")
        final Subscriber[] ts0 = { null };
        TestSubscriberEx<Integer> ts = Flowable.just(1).hide().concatMapDelayError(Functions.justFunction(new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                ts0[0] = s;
                s.onSubscribe(new BooleanSubscription());
                s.onError(new TestException("First"));
            }
        }), true, 2, ImmediateThinScheduler.INSTANCE).to(TestHelper.<Integer>testConsumer());
        ts.assertFailureAndMessage(TestException.class, "First");
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            ts0[0].onError(new TestException("Second"));
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSourceDelayError() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.concatMapDelayError(Functions.justFunction(Flowable.just(1).hide()), true, 2, ImmediateThinScheduler.INSTANCE);
            }
        }, true, 1, 1, 1);
    }

    @Test
    public void fusedCrash() {
        Flowable.range(1, 2).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).concatMap(Functions.justFunction(Flowable.just(1)), 2, ImmediateThinScheduler.INSTANCE).test().assertFailure(TestException.class);
    }

    @Test
    public void fusedCrashDelayError() {
        Flowable.range(1, 2).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).concatMapDelayError(Functions.justFunction(Flowable.just(1)), true, 2, ImmediateThinScheduler.INSTANCE).test().assertFailure(TestException.class);
    }

    @Test
    public void callableCrash() {
        Flowable.just(1).hide().concatMap(Functions.justFunction(Flowable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw new TestException();
            }
        })), 2, ImmediateThinScheduler.INSTANCE).test().assertFailure(TestException.class);
    }

    @Test
    public void callableCrashDelayError() {
        Flowable.just(1).hide().concatMapDelayError(Functions.justFunction(Flowable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw new TestException();
            }
        })), true, 2, ImmediateThinScheduler.INSTANCE).test().assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.range(1, 2).concatMap(Functions.justFunction(Flowable.just(1)), 2, ImmediateThinScheduler.INSTANCE));
        TestHelper.checkDisposed(Flowable.range(1, 2).concatMapDelayError(Functions.justFunction(Flowable.just(1)), true, 2, ImmediateThinScheduler.INSTANCE));
    }

    @Test
    public void notVeryEnd() {
        Flowable.range(1, 2).concatMapDelayError(Functions.justFunction(Flowable.error(new TestException())), false, 16, ImmediateThinScheduler.INSTANCE).test().assertFailure(TestException.class);
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).concatMapDelayError(Functions.justFunction(Flowable.just(2)), false, 16, ImmediateThinScheduler.INSTANCE).test().assertFailure(TestException.class);
    }

    @Test
    public void mapperThrows() {
        Flowable.range(1, 2).concatMap(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }, 2, ImmediateThinScheduler.INSTANCE).test().assertFailure(TestException.class);
    }

    @Test
    public void mainErrors() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestSubscriber<Integer> ts = TestSubscriber.create();
        source.concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return Flowable.range(v, 2);
            }
        }, true, 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        ts.assertValues(1, 2, 2, 3);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void innerErrors() {
        final Flowable<Integer> inner = Flowable.range(1, 2).concatWith(Flowable.<Integer>error(new TestException()));
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(1, 3).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return inner;
            }
        }, true, 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        ts.assertValues(1, 2, 1, 2, 1, 2);
        ts.assertError(CompositeException.class);
        ts.assertNotComplete();
    }

    @Test
    public void singleInnerErrors() {
        final Flowable<Integer> inner = Flowable.range(1, 2).concatWith(Flowable.<Integer>error(new TestException()));
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(1).hide().concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return inner;
            }
        }, true, 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void innerNull() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(1).hide().concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return null;
            }
        }, true, 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(NullPointerException.class);
        ts.assertNotComplete();
    }

    @Test
    public void innerThrows() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(1).hide().concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                throw new TestException();
            }
        }, true, 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void innerWithEmpty() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(1, 3).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return v == 2 ? Flowable.<Integer>empty() : Flowable.range(1, 2);
            }
        }, true, 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        ts.assertValues(1, 2, 1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void innerWithScalar() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(1, 3).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return v == 2 ? Flowable.just(3) : Flowable.range(1, 2);
            }
        }, true, 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        ts.assertValues(1, 2, 3, 1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        Flowable.range(1, 3).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return Flowable.range(v, 2);
            }
        }, true, 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(1);
        ts.assertValues(1);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(3);
        ts.assertValues(1, 2, 2, 3);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(2);
        ts.assertValues(1, 2, 2, 3, 3, 4);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void mapperScheduledLong() {
        TestSubscriber<String> ts = Flowable.range(1, 1000).hide().observeOn(Schedulers.computation()).concatMap(new Function<Integer, Flowable<String>>() {

            @Override
            public Flowable<String> apply(Integer t) throws Throwable {
                return Flowable.just(Thread.currentThread().getName()).repeat(1000).observeOn(Schedulers.io());
            }
        }, 2, Schedulers.single()).distinct().test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(1).assertNoErrors().assertComplete();
        assertTrue(ts.values().toString(), ts.values().get(0).startsWith("RxSingleScheduler-"));
    }

    @Test
    public void mapperDelayErrorScheduledLong() {
        TestSubscriber<String> ts = Flowable.range(1, 1000).hide().observeOn(Schedulers.computation()).concatMapDelayError(new Function<Integer, Flowable<String>>() {

            @Override
            public Flowable<String> apply(Integer t) throws Throwable {
                return Flowable.just(Thread.currentThread().getName()).repeat(1000).observeOn(Schedulers.io());
            }
        }, false, 2, Schedulers.single()).distinct().test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(1).assertNoErrors().assertComplete();
        assertTrue(ts.values().toString(), ts.values().get(0).startsWith("RxSingleScheduler-"));
    }

    @Test
    public void mapperDelayError2ScheduledLong() {
        TestSubscriber<String> ts = Flowable.range(1, 1000).hide().observeOn(Schedulers.computation()).concatMapDelayError(new Function<Integer, Flowable<String>>() {

            @Override
            public Flowable<String> apply(Integer t) throws Throwable {
                return Flowable.just(Thread.currentThread().getName()).repeat(1000).observeOn(Schedulers.io());
            }
        }, true, 2, Schedulers.single()).distinct().test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(1).assertNoErrors().assertComplete();
        assertTrue(ts.values().toString(), ts.values().get(0).startsWith("RxSingleScheduler-"));
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.concatMap(new Function<Integer, Publisher<Integer>>() {

                    @Override
                    public Publisher<Integer> apply(Integer v) throws Throwable {
                        return Flowable.just(v).hide();
                    }
                }, 2, ImmediateThinScheduler.INSTANCE);
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayError() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.concatMapDelayError(new Function<Integer, Publisher<Integer>>() {

                    @Override
                    public Publisher<Integer> apply(Integer v) throws Throwable {
                        return Flowable.just(v).hide();
                    }
                }, false, 2, ImmediateThinScheduler.INSTANCE);
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayErrorTillEnd() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.concatMapDelayError(new Function<Integer, Publisher<Integer>>() {

                    @Override
                    public Publisher<Integer> apply(Integer v) throws Throwable {
                        return Flowable.just(v).hide();
                    }
                }, true, 2, ImmediateThinScheduler.INSTANCE);
            }
        });
    }

    @Test
    public void fusionRejected() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        TestHelper.rejectFlowableFusion().concatMap(v -> Flowable.never(), 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
    }

    @Test
    public void fusionRejectedDelayErrorr() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        TestHelper.rejectFlowableFusion().concatMapDelayError(v -> Flowable.never(), true, 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
    }

    @Test
    public void scalarInnerJustDispose() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.just(1).hide().concatMap(v -> Flowable.fromCallable(() -> {
            ts.cancel();
            return 1;
        }), 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        ts.assertEmpty();
    }

    @Test
    public void scalarInnerJustDisposeDelayError() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.just(1).hide().concatMapDelayError(v -> Flowable.fromCallable(() -> {
            ts.cancel();
            return 1;
        }), true, 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        ts.assertEmpty();
    }

    static final class EmptyDisposingFlowable extends Flowable<Object> implements Supplier<Object> {

        final TestSubscriber<Object> ts;

        EmptyDisposingFlowable(TestSubscriber<Object> ts) {
            this.ts = ts;
        }

        @Override
        protected void subscribeActual(@NonNull Subscriber<? super @NonNull Object> subscriber) {
            EmptySubscription.complete(subscriber);
        }

        @Override
        @NonNull
        public Object get() throws Throwable {
            ts.cancel();
            return null;
        }
    }

    @Test
    public void scalarInnerEmptyDisposeDelayError() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.just(1).hide().concatMapDelayError(v -> new EmptyDisposingFlowable(ts), true, 2, ImmediateThinScheduler.INSTANCE).subscribe(ts);
        ts.assertEmpty();
    }

    @Test
    public void mainErrorInnerNextIgnoreCancel() {
        AtomicReference<Subscriber<? super Integer>> ref = new AtomicReference<>();
        Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).concatMap(v -> Flowable.<Integer>fromPublisher(ref::set), 2, ImmediateThinScheduler.INSTANCE).doOnError(e -> {
            ref.get().onSubscribe(new BooleanSubscription());
            ref.get().onNext(1);
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void scalarSupplierMainError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.concatMap(v -> Flowable.fromCallable(() -> {
            pp.onError(new TestException());
            return 2;
        }), 2, ImmediateThinScheduler.INSTANCE).test();
        pp.onNext(1);
        ts.assertFailure(TestException.class);
    }

    @Test
    public void mainErrorInnerErrorRace() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            TestException ex1 = new TestException();
            TestException ex2 = new TestException();
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                AtomicReference<Subscriber<? super Integer>> ref1 = new AtomicReference<>();
                AtomicReference<Subscriber<? super Integer>> ref2 = new AtomicReference<>();
                TestSubscriber<Integer> ts = Flowable.<Integer>fromPublisher(ref1::set).concatMap(v -> Flowable.<Integer>fromPublisher(ref2::set), 2, ImmediateThinScheduler.INSTANCE).test();
                ref1.get().onSubscribe(new BooleanSubscription());
                ref1.get().onNext(1);
                ref2.get().onSubscribe(new BooleanSubscription());
                TestHelper.race(() -> ref1.get().onError(ex1), () -> ref2.get().onError(ex2));
                ts.assertError(RuntimeException.class);
                errors.clear();
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableConcatMapSchedulerTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusion() throws java.lang.Throwable {
            this.payloads.boundaryFusion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerScalarRequestRace() throws java.lang.Throwable {
            this.payloads.innerScalarRequestRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerScalarRequestRaceDelayError() throws java.lang.Throwable {
            this.payloads.innerScalarRequestRaceDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusionDelayError() throws java.lang.Throwable {
            this.payloads.boundaryFusionDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_pollThrows() throws java.lang.Throwable {
            this.payloads.pollThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_pollThrowsDelayError() throws java.lang.Throwable {
            this.payloads.pollThrowsDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPrevious() throws java.lang.Throwable {
            this.payloads.noCancelPrevious.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorCallableTillTheEnd() throws java.lang.Throwable {
            this.payloads.delayErrorCallableTillTheEnd.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorCallableEager() throws java.lang.Throwable {
            this.payloads.delayErrorCallableEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperScheduled() throws java.lang.Throwable {
            this.payloads.mapperScheduled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperScheduledHidden() throws java.lang.Throwable {
            this.payloads.mapperScheduledHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperDelayErrorScheduled() throws java.lang.Throwable {
            this.payloads.mapperDelayErrorScheduled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperDelayErrorScheduledHidden() throws java.lang.Throwable {
            this.payloads.mapperDelayErrorScheduledHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperDelayError2Scheduled() throws java.lang.Throwable {
            this.payloads.mapperDelayError2Scheduled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperDelayError2ScheduledHidden() throws java.lang.Throwable {
            this.payloads.mapperDelayError2ScheduledHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue2890NoStackoverflow() throws java.lang.Throwable {
            this.payloads.issue2890NoStackoverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapRangeAsyncLoopIssue2876() throws java.lang.Throwable {
            this.payloads.concatMapRangeAsyncLoopIssue2876.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArray() throws java.lang.Throwable {
            this.payloads.concatArray.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapJustJust() throws java.lang.Throwable {
            this.payloads.concatMapJustJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapJustRange() throws java.lang.Throwable {
            this.payloads.concatMapJustRange.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapDelayErrorJustJust() throws java.lang.Throwable {
            this.payloads.concatMapDelayErrorJustJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapDelayErrorJustRange() throws java.lang.Throwable {
            this.payloads.concatMapDelayErrorJustRange.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithArray() throws java.lang.Throwable {
            this.payloads.startWithArray.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapDelayError() throws java.lang.Throwable {
            this.payloads.concatMapDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapDelayErrorJustSource() throws java.lang.Throwable {
            this.payloads.concatMapDelayErrorJustSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapJustSource() throws java.lang.Throwable {
            this.payloads.concatMapJustSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapJustSourceDelayError() throws java.lang.Throwable {
            this.payloads.concatMapJustSourceDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapScalarBackpressured() throws java.lang.Throwable {
            this.payloads.concatMapScalarBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapScalarBackpressuredDelayError() throws java.lang.Throwable {
            this.payloads.concatMapScalarBackpressuredDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapEmpty() throws java.lang.Throwable {
            this.payloads.concatMapEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapEmptyDelayError() throws java.lang.Throwable {
            this.payloads.concatMapEmptyDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreBackpressure() throws java.lang.Throwable {
            this.payloads.ignoreBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_immediateInnerNextOuterError() throws java.lang.Throwable {
            this.payloads.immediateInnerNextOuterError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_immediateInnerNextOuterError2() throws java.lang.Throwable {
            this.payloads.immediateInnerNextOuterError2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapInnerError() throws java.lang.Throwable {
            this.payloads.concatMapInnerError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapInnerErrorDelayError() throws java.lang.Throwable {
            this.payloads.concatMapInnerErrorDelayError.evaluate();
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
        public void benchmark_badInnerSourceDelayError() throws java.lang.Throwable {
            this.payloads.badInnerSourceDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceDelayError() throws java.lang.Throwable {
            this.payloads.badSourceDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedCrash() throws java.lang.Throwable {
            this.payloads.fusedCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedCrashDelayError() throws java.lang.Throwable {
            this.payloads.fusedCrashDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_callableCrash() throws java.lang.Throwable {
            this.payloads.callableCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_callableCrashDelayError() throws java.lang.Throwable {
            this.payloads.callableCrashDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notVeryEnd() throws java.lang.Throwable {
            this.payloads.notVeryEnd.evaluate();
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
        public void benchmark_mainErrors() throws java.lang.Throwable {
            this.payloads.mainErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrors() throws java.lang.Throwable {
            this.payloads.innerErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleInnerErrors() throws java.lang.Throwable {
            this.payloads.singleInnerErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerNull() throws java.lang.Throwable {
            this.payloads.innerNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerThrows() throws java.lang.Throwable {
            this.payloads.innerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerWithEmpty() throws java.lang.Throwable {
            this.payloads.innerWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerWithScalar() throws java.lang.Throwable {
            this.payloads.innerWithScalar.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.payloads.backpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperScheduledLong() throws java.lang.Throwable {
            this.payloads.mapperScheduledLong.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperDelayErrorScheduledLong() throws java.lang.Throwable {
            this.payloads.mapperDelayErrorScheduledLong.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperDelayError2ScheduledLong() throws java.lang.Throwable {
            this.payloads.mapperDelayError2ScheduledLong.evaluate();
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
        public void benchmark_undeliverableUponCancelDelayErrorTillEnd() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancelDelayErrorTillEnd.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejected() throws java.lang.Throwable {
            this.payloads.fusionRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejectedDelayErrorr() throws java.lang.Throwable {
            this.payloads.fusionRejectedDelayErrorr.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarInnerJustDispose() throws java.lang.Throwable {
            this.payloads.scalarInnerJustDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarInnerJustDisposeDelayError() throws java.lang.Throwable {
            this.payloads.scalarInnerJustDisposeDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarInnerEmptyDisposeDelayError() throws java.lang.Throwable {
            this.payloads.scalarInnerEmptyDisposeDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorInnerNextIgnoreCancel() throws java.lang.Throwable {
            this.payloads.mainErrorInnerNextIgnoreCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarSupplierMainError() throws java.lang.Throwable {
            this.payloads.scalarSupplierMainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorInnerErrorRace() throws java.lang.Throwable {
            this.payloads.mainErrorInnerErrorRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapSchedulerTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapSchedulerTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapSchedulerTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapSchedulerTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableConcatMapSchedulerTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapSchedulerTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableConcatMapSchedulerTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableConcatMapSchedulerTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement boundaryFusion;

            public org.junit.runners.model.Statement innerScalarRequestRace;

            public org.junit.runners.model.Statement innerScalarRequestRaceDelayError;

            public org.junit.runners.model.Statement boundaryFusionDelayError;

            public org.junit.runners.model.Statement pollThrows;

            public org.junit.runners.model.Statement pollThrowsDelayError;

            public org.junit.runners.model.Statement noCancelPrevious;

            public org.junit.runners.model.Statement delayErrorCallableTillTheEnd;

            public org.junit.runners.model.Statement delayErrorCallableEager;

            public org.junit.runners.model.Statement mapperScheduled;

            public org.junit.runners.model.Statement mapperScheduledHidden;

            public org.junit.runners.model.Statement mapperDelayErrorScheduled;

            public org.junit.runners.model.Statement mapperDelayErrorScheduledHidden;

            public org.junit.runners.model.Statement mapperDelayError2Scheduled;

            public org.junit.runners.model.Statement mapperDelayError2ScheduledHidden;

            public org.junit.runners.model.Statement issue2890NoStackoverflow;

            public org.junit.runners.model.Statement concatMapRangeAsyncLoopIssue2876;

            public org.junit.runners.model.Statement concatArray;

            public org.junit.runners.model.Statement concatMapJustJust;

            public org.junit.runners.model.Statement concatMapJustRange;

            public org.junit.runners.model.Statement concatMapDelayErrorJustJust;

            public org.junit.runners.model.Statement concatMapDelayErrorJustRange;

            public org.junit.runners.model.Statement startWithArray;

            public org.junit.runners.model.Statement concatMapDelayError;

            public org.junit.runners.model.Statement concatMapDelayErrorJustSource;

            public org.junit.runners.model.Statement concatMapJustSource;

            public org.junit.runners.model.Statement concatMapJustSourceDelayError;

            public org.junit.runners.model.Statement concatMapScalarBackpressured;

            public org.junit.runners.model.Statement concatMapScalarBackpressuredDelayError;

            public org.junit.runners.model.Statement concatMapEmpty;

            public org.junit.runners.model.Statement concatMapEmptyDelayError;

            public org.junit.runners.model.Statement ignoreBackpressure;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement immediateInnerNextOuterError;

            public org.junit.runners.model.Statement immediateInnerNextOuterError2;

            public org.junit.runners.model.Statement concatMapInnerError;

            public org.junit.runners.model.Statement concatMapInnerErrorDelayError;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement badInnerSource;

            public org.junit.runners.model.Statement badInnerSourceDelayError;

            public org.junit.runners.model.Statement badSourceDelayError;

            public org.junit.runners.model.Statement fusedCrash;

            public org.junit.runners.model.Statement fusedCrashDelayError;

            public org.junit.runners.model.Statement callableCrash;

            public org.junit.runners.model.Statement callableCrashDelayError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement notVeryEnd;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement mapperThrows;

            public org.junit.runners.model.Statement mainErrors;

            public org.junit.runners.model.Statement innerErrors;

            public org.junit.runners.model.Statement singleInnerErrors;

            public org.junit.runners.model.Statement innerNull;

            public org.junit.runners.model.Statement innerThrows;

            public org.junit.runners.model.Statement innerWithEmpty;

            public org.junit.runners.model.Statement innerWithScalar;

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement mapperScheduledLong;

            public org.junit.runners.model.Statement mapperDelayErrorScheduledLong;

            public org.junit.runners.model.Statement mapperDelayError2ScheduledLong;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayErrorTillEnd;

            public org.junit.runners.model.Statement fusionRejected;

            public org.junit.runners.model.Statement fusionRejectedDelayErrorr;

            public org.junit.runners.model.Statement scalarInnerJustDispose;

            public org.junit.runners.model.Statement scalarInnerJustDisposeDelayError;

            public org.junit.runners.model.Statement scalarInnerEmptyDisposeDelayError;

            public org.junit.runners.model.Statement mainErrorInnerNextIgnoreCancel;

            public org.junit.runners.model.Statement scalarSupplierMainError;

            public org.junit.runners.model.Statement mainErrorInnerErrorRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.boundaryFusion = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::boundaryFusion, "boundaryFusion", this);
            this.payloads.innerScalarRequestRace = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::innerScalarRequestRace, "innerScalarRequestRace", this);
            this.payloads.innerScalarRequestRaceDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::innerScalarRequestRaceDelayError, "innerScalarRequestRaceDelayError", this);
            this.payloads.boundaryFusionDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::boundaryFusionDelayError, "boundaryFusionDelayError", this);
            this.payloads.pollThrows = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::pollThrows, "pollThrows", this);
            this.payloads.pollThrowsDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::pollThrowsDelayError, "pollThrowsDelayError", this);
            this.payloads.noCancelPrevious = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::noCancelPrevious, "noCancelPrevious", this);
            this.payloads.delayErrorCallableTillTheEnd = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::delayErrorCallableTillTheEnd, "delayErrorCallableTillTheEnd", this);
            this.payloads.delayErrorCallableEager = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::delayErrorCallableEager, "delayErrorCallableEager", this);
            this.payloads.mapperScheduled = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::mapperScheduled, "mapperScheduled", this);
            this.payloads.mapperScheduledHidden = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::mapperScheduledHidden, "mapperScheduledHidden", this);
            this.payloads.mapperDelayErrorScheduled = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::mapperDelayErrorScheduled, "mapperDelayErrorScheduled", this);
            this.payloads.mapperDelayErrorScheduledHidden = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::mapperDelayErrorScheduledHidden, "mapperDelayErrorScheduledHidden", this);
            this.payloads.mapperDelayError2Scheduled = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::mapperDelayError2Scheduled, "mapperDelayError2Scheduled", this);
            this.payloads.mapperDelayError2ScheduledHidden = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::mapperDelayError2ScheduledHidden, "mapperDelayError2ScheduledHidden", this);
            this.payloads.issue2890NoStackoverflow = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::issue2890NoStackoverflow, "issue2890NoStackoverflow", this);
            this.payloads.concatMapRangeAsyncLoopIssue2876 = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapRangeAsyncLoopIssue2876, "concatMapRangeAsyncLoopIssue2876", this);
            this.payloads.concatArray = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatArray, "concatArray", this);
            this.payloads.concatMapJustJust = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapJustJust, "concatMapJustJust", this);
            this.payloads.concatMapJustRange = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapJustRange, "concatMapJustRange", this);
            this.payloads.concatMapDelayErrorJustJust = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapDelayErrorJustJust, "concatMapDelayErrorJustJust", this);
            this.payloads.concatMapDelayErrorJustRange = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapDelayErrorJustRange, "concatMapDelayErrorJustRange", this);
            this.payloads.startWithArray = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::startWithArray, "startWithArray", this);
            this.payloads.concatMapDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapDelayError, "concatMapDelayError", this);
            this.payloads.concatMapDelayErrorJustSource = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapDelayErrorJustSource, "concatMapDelayErrorJustSource", this);
            this.payloads.concatMapJustSource = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapJustSource, "concatMapJustSource", this);
            this.payloads.concatMapJustSourceDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapJustSourceDelayError, "concatMapJustSourceDelayError", this);
            this.payloads.concatMapScalarBackpressured = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapScalarBackpressured, "concatMapScalarBackpressured", this);
            this.payloads.concatMapScalarBackpressuredDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapScalarBackpressuredDelayError, "concatMapScalarBackpressuredDelayError", this);
            this.payloads.concatMapEmpty = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapEmpty, "concatMapEmpty", this);
            this.payloads.concatMapEmptyDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapEmptyDelayError, "concatMapEmptyDelayError", this);
            this.payloads.ignoreBackpressure = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::ignoreBackpressure, "ignoreBackpressure", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.immediateInnerNextOuterError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::immediateInnerNextOuterError, "immediateInnerNextOuterError", this);
            this.payloads.immediateInnerNextOuterError2 = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::immediateInnerNextOuterError2, "immediateInnerNextOuterError2", this);
            this.payloads.concatMapInnerError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapInnerError, "concatMapInnerError", this);
            this.payloads.concatMapInnerErrorDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::concatMapInnerErrorDelayError, "concatMapInnerErrorDelayError", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::badSource, "badSource", this);
            this.payloads.badInnerSource = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::badInnerSource, "badInnerSource", this);
            this.payloads.badInnerSourceDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::badInnerSourceDelayError, "badInnerSourceDelayError", this);
            this.payloads.badSourceDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::badSourceDelayError, "badSourceDelayError", this);
            this.payloads.fusedCrash = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::fusedCrash, "fusedCrash", this);
            this.payloads.fusedCrashDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::fusedCrashDelayError, "fusedCrashDelayError", this);
            this.payloads.callableCrash = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::callableCrash, "callableCrash", this);
            this.payloads.callableCrashDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::callableCrashDelayError, "callableCrashDelayError", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::dispose, "dispose", this);
            this.payloads.notVeryEnd = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::notVeryEnd, "notVeryEnd", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::error, "error", this);
            this.payloads.mapperThrows = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::mapperThrows, "mapperThrows", this);
            this.payloads.mainErrors = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::mainErrors, "mainErrors", this);
            this.payloads.innerErrors = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::innerErrors, "innerErrors", this);
            this.payloads.singleInnerErrors = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::singleInnerErrors, "singleInnerErrors", this);
            this.payloads.innerNull = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::innerNull, "innerNull", this);
            this.payloads.innerThrows = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::innerThrows, "innerThrows", this);
            this.payloads.innerWithEmpty = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::innerWithEmpty, "innerWithEmpty", this);
            this.payloads.innerWithScalar = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::innerWithScalar, "innerWithScalar", this);
            this.payloads.backpressure = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::backpressure, "backpressure", this);
            this.payloads.mapperScheduledLong = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::mapperScheduledLong, "mapperScheduledLong", this);
            this.payloads.mapperDelayErrorScheduledLong = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::mapperDelayErrorScheduledLong, "mapperDelayErrorScheduledLong", this);
            this.payloads.mapperDelayError2ScheduledLong = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::mapperDelayError2ScheduledLong, "mapperDelayError2ScheduledLong", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.undeliverableUponCancelDelayErrorTillEnd = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::undeliverableUponCancelDelayErrorTillEnd, "undeliverableUponCancelDelayErrorTillEnd", this);
            this.payloads.fusionRejected = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::fusionRejected, "fusionRejected", this);
            this.payloads.fusionRejectedDelayErrorr = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::fusionRejectedDelayErrorr, "fusionRejectedDelayErrorr", this);
            this.payloads.scalarInnerJustDispose = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::scalarInnerJustDispose, "scalarInnerJustDispose", this);
            this.payloads.scalarInnerJustDisposeDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::scalarInnerJustDisposeDelayError, "scalarInnerJustDisposeDelayError", this);
            this.payloads.scalarInnerEmptyDisposeDelayError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::scalarInnerEmptyDisposeDelayError, "scalarInnerEmptyDisposeDelayError", this);
            this.payloads.mainErrorInnerNextIgnoreCancel = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::mainErrorInnerNextIgnoreCancel, "mainErrorInnerNextIgnoreCancel", this);
            this.payloads.scalarSupplierMainError = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::scalarSupplierMainError, "scalarSupplierMainError", this);
            this.payloads.mainErrorInnerErrorRace = _ClassStatement.forPayload(FlowableConcatMapSchedulerTest::mainErrorInnerErrorRace, "mainErrorInnerErrorRace", this);
        }
    }
}
