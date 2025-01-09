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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableWindowWithTimeTest extends RxJavaTest {

    private TestScheduler scheduler;

    private Scheduler.Worker innerScheduler;

    @Before
    public void before() {
        scheduler = new TestScheduler();
        innerScheduler = scheduler.createWorker();
    }

    @Test
    public void timedAndCount() {
        final List<String> list = new ArrayList<>();
        final List<List<String>> lists = new ArrayList<>();
        Flowable<String> source = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                push(subscriber, "one", 10);
                push(subscriber, "two", 90);
                push(subscriber, "three", 110);
                push(subscriber, "four", 190);
                push(subscriber, "five", 210);
                complete(subscriber, 250);
            }
        });
        Flowable<Flowable<String>> windowed = source.window(100, TimeUnit.MILLISECONDS, scheduler, 2);
        windowed.subscribe(observeWindow(list, lists));
        scheduler.advanceTimeTo(95, TimeUnit.MILLISECONDS);
        assertEquals(1, lists.size());
        assertEquals(lists.get(0), list("one", "two"));
        scheduler.advanceTimeTo(195, TimeUnit.MILLISECONDS);
        assertEquals(3, lists.size());
        assertTrue(lists.get(1).isEmpty());
        assertEquals(lists.get(2), list("three", "four"));
        scheduler.advanceTimeTo(300, TimeUnit.MILLISECONDS);
        assertEquals(5, lists.size());
        assertTrue(lists.get(3).isEmpty());
        assertEquals(lists.get(4), list("five"));
    }

    @Test
    public void timed() {
        final List<String> list = new ArrayList<>();
        final List<List<String>> lists = new ArrayList<>();
        Flowable<String> source = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                push(subscriber, "one", 98);
                push(subscriber, "two", 99);
                // FIXME happens after the window is open
                push(subscriber, "three", 99);
                push(subscriber, "four", 101);
                push(subscriber, "five", 102);
                complete(subscriber, 150);
            }
        });
        Flowable<Flowable<String>> windowed = source.window(100, TimeUnit.MILLISECONDS, scheduler);
        windowed.subscribe(observeWindow(list, lists));
        scheduler.advanceTimeTo(101, TimeUnit.MILLISECONDS);
        assertEquals(1, lists.size());
        assertEquals(lists.get(0), list("one", "two", "three"));
        scheduler.advanceTimeTo(201, TimeUnit.MILLISECONDS);
        assertEquals(2, lists.size());
        assertEquals(lists.get(1), list("four", "five"));
    }

    private List<String> list(String... args) {
        List<String> list = new ArrayList<>();
        for (String arg : args) {
            list.add(arg);
        }
        return list;
    }

    private <T> void push(final Subscriber<T> subscriber, final T value, int delay) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                subscriber.onNext(value);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void complete(final Subscriber<?> subscriber, int delay) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                subscriber.onComplete();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private <T> Consumer<Flowable<T>> observeWindow(final List<T> list, final List<List<T>> lists) {
        return new Consumer<Flowable<T>>() {

            @Override
            public void accept(Flowable<T> stringFlowable) {
                stringFlowable.subscribe(new DefaultSubscriber<T>() {

                    @Override
                    public void onComplete() {
                        lists.add(new ArrayList<>(list));
                        list.clear();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Assert.fail(e.getMessage());
                    }

                    @Override
                    public void onNext(T args) {
                        list.add(args);
                    }
                });
            }
        };
    }

    @Test
    public void exactWindowSize() {
        Flowable<Flowable<Integer>> source = Flowable.range(1, 10).window(1, TimeUnit.MINUTES, scheduler, 3);
        final List<Integer> list = new ArrayList<>();
        final List<List<Integer>> lists = new ArrayList<>();
        source.subscribe(observeWindow(list, lists));
        assertEquals(4, lists.size());
        assertEquals(3, lists.get(0).size());
        assertEquals(Arrays.asList(1, 2, 3), lists.get(0));
        assertEquals(3, lists.get(1).size());
        assertEquals(Arrays.asList(4, 5, 6), lists.get(1));
        assertEquals(3, lists.get(2).size());
        assertEquals(Arrays.asList(7, 8, 9), lists.get(2));
        assertEquals(1, lists.get(3).size());
        assertEquals(Arrays.asList(10), lists.get(3));
    }

    @Test
    public void takeFlatMapCompletes() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final AtomicInteger wip = new AtomicInteger();
        final int indicator = 999999999;
        FlowableWindowWithSizeTest.hotStream().window(300, TimeUnit.MILLISECONDS).take(10).doOnComplete(new Action() {

            @Override
            public void run() {
                // System.out.println("Main done!");
            }
        }).flatMap(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> w) {
                return w.startWithItem(indicator).doOnComplete(new Action() {

                    @Override
                    public void run() {
                        // System.out.println("inner done: " + wip.incrementAndGet());
                    }
                });
            }
        }).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer pv) {
                // System.out.println(pv);
            }
        }).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertComplete();
        Assert.assertTrue(ts.values().size() != 0);
    }

    @Test
    public void timespanTimeskipCustomSchedulerBufferSize() {
        Flowable.range(1, 10).window(1, 1, TimeUnit.MINUTES, Schedulers.io(), 2).flatMap(Functions.<Flowable<Integer>>identity()).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void timespanDefaultSchedulerSize() {
        Flowable.range(1, 10).window(1, TimeUnit.MINUTES, 20).flatMap(Functions.<Flowable<Integer>>identity()).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void timespanDefaultSchedulerSizeRestart() {
        Flowable.range(1, 10).window(1, TimeUnit.MINUTES, 20, true).flatMap(Functions.<Flowable<Integer>>identity(), true).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void invalidSpan() {
        try {
            Flowable.just(1).window(-99, 1, TimeUnit.SECONDS);
            fail("Should have thrown!");
        } catch (IllegalArgumentException ex) {
            assertEquals("timespan > 0 required but it was -99", ex.getMessage());
        }
    }

    @Test
    public void timespanTimeskipDefaultScheduler() {
        Flowable.just(1).window(1, 1, TimeUnit.MINUTES).flatMap(Functions.<Flowable<Integer>>identity()).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void timespanTimeskipCustomScheduler() {
        Flowable.just(1).window(1, 1, TimeUnit.MINUTES, Schedulers.io()).flatMap(Functions.<Flowable<Integer>>identity()).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void timeskipJustOverlap() {
        Flowable.just(1).window(2, 1, TimeUnit.MINUTES, Schedulers.single()).flatMap(Functions.<Flowable<Integer>>identity()).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void timeskipJustSkip() {
        Flowable.just(1).window(1, 2, TimeUnit.MINUTES, Schedulers.single()).flatMap(Functions.<Flowable<Integer>>identity()).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void timeskipSkipping() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.window(1, 2, TimeUnit.SECONDS, scheduler).flatMap(Functions.<Flowable<Integer>>identity()).test();
        pp.onNext(1);
        pp.onNext(2);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onNext(3);
        pp.onNext(4);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onNext(5);
        pp.onNext(6);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onNext(7);
        pp.onComplete();
        ts.assertResult(1, 2, 5, 6);
    }

    @Test
    public void timeskipOverlapping() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.window(2, 1, TimeUnit.SECONDS, scheduler).flatMap(Functions.<Flowable<Integer>>identity()).test();
        pp.onNext(1);
        pp.onNext(2);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onNext(3);
        pp.onNext(4);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onNext(5);
        pp.onNext(6);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onNext(7);
        pp.onComplete();
        ts.assertResult(1, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7);
    }

    @Test
    public void exactOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestScheduler scheduler = new TestScheduler();
            PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriber<Integer> ts = pp.window(1, 1, TimeUnit.SECONDS, scheduler).flatMap(Functions.<Flowable<Integer>>identity()).test();
            pp.onError(new TestException());
            ts.assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void overlappingOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestScheduler scheduler = new TestScheduler();
            PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriber<Integer> ts = pp.window(2, 1, TimeUnit.SECONDS, scheduler).flatMap(Functions.<Flowable<Integer>>identity()).test();
            pp.onError(new TestException());
            ts.assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void skipOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestScheduler scheduler = new TestScheduler();
            PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriber<Integer> ts = pp.window(1, 2, TimeUnit.SECONDS, scheduler).flatMap(Functions.<Flowable<Integer>>identity()).test();
            pp.onError(new TestException());
            ts.assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void exactBackpressure() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Flowable<Integer>> ts = pp.window(1, 1, TimeUnit.SECONDS, scheduler).test(0L);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void skipBackpressure() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Flowable<Integer>> ts = pp.window(1, 2, TimeUnit.SECONDS, scheduler).test(0L);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void overlapBackpressure() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Flowable<Integer>> ts = pp.window(2, 1, TimeUnit.SECONDS, scheduler).test(0L);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void exactBackpressure2() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Flowable<Integer>> ts = pp.window(1, 1, TimeUnit.SECONDS, scheduler).test(1L);
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        ts.assertError(MissingBackpressureException.class);
    }

    @Test
    public void skipBackpressure2() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Flowable<Integer>> ts = pp.window(1, 2, TimeUnit.SECONDS, scheduler).test(1L);
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        ts.assertError(MissingBackpressureException.class);
    }

    @Test
    public void overlapBackpressure2() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestScheduler scheduler = new TestScheduler();
            PublishProcessor<Integer> pp = PublishProcessor.create();
            final TestSubscriber<Integer> tsInner = new TestSubscriber<>();
            TestSubscriber<Flowable<Integer>> ts = pp.window(2, 1, TimeUnit.SECONDS, scheduler).doOnNext(new Consumer<Flowable<Integer>>() {

                @Override
                public void accept(Flowable<Integer> w) throws Throwable {
                    w.subscribe(tsInner);
                }
            }).test(1L);
            scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
            ts.assertError(MissingBackpressureException.class);
            tsInner.assertError(MissingBackpressureException.class);
            assertTrue(errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.range(1, 5).window(1, TimeUnit.DAYS, Schedulers.single()).onBackpressureDrop());
        TestHelper.checkDisposed(Flowable.range(1, 5).window(2, 1, TimeUnit.DAYS, Schedulers.single()).onBackpressureDrop());
        TestHelper.checkDisposed(Flowable.range(1, 5).window(1, 2, TimeUnit.DAYS, Schedulers.single()).onBackpressureDrop());
        TestHelper.checkDisposed(Flowable.never().window(1, TimeUnit.DAYS, Schedulers.single(), 2, true).onBackpressureDrop());
    }

    @Test
    public void restartTimer() {
        Flowable.range(1, 5).window(1, TimeUnit.DAYS, Schedulers.single(), 2, true).flatMap(Functions.<Flowable<Integer>>identity()).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    @SuppressUndeliverable
    public void exactBoundaryError() {
        Flowable.error(new TestException()).window(1, TimeUnit.DAYS, Schedulers.single(), 2, true).to(TestHelper.<Flowable<Object>>testConsumer()).assertSubscribed().assertError(TestException.class).assertNotComplete();
    }

    @Test
    public void restartTimerMany() throws Exception {
        final AtomicBoolean cancel1 = new AtomicBoolean();
        Flowable.intervalRange(1, 1000, 1, 1, TimeUnit.MILLISECONDS).doOnCancel(new Action() {

            @Override
            public void run() throws Exception {
                cancel1.set(true);
            }
        }).window(1, TimeUnit.MILLISECONDS, Schedulers.single(), 2, true).flatMap(Functions.<Flowable<Long>>identity()).take(500).to(TestHelper.<Long>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(500).assertNoErrors().assertComplete();
        int timeout = 20;
        while (timeout-- > 0 && !cancel1.get()) {
            Thread.sleep(100);
        }
        assertTrue("intervalRange was not cancelled!", cancel1.get());
    }

    @Test
    public void exactUnboundedReentrant() {
        TestScheduler scheduler = new TestScheduler();
        final FlowableProcessor<Integer> ps = PublishProcessor.<Integer>create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    ps.onNext(2);
                    ps.onComplete();
                }
            }
        };
        ps.window(1, TimeUnit.MILLISECONDS, scheduler).flatMap(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> v) throws Exception {
                return v;
            }
        }).subscribe(ts);
        ps.onNext(1);
        ts.awaitDone(1, TimeUnit.SECONDS).assertResult(1, 2);
    }

    @Test
    public void exactBoundedReentrant() {
        TestScheduler scheduler = new TestScheduler();
        final FlowableProcessor<Integer> ps = PublishProcessor.<Integer>create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    ps.onNext(2);
                    ps.onComplete();
                }
            }
        };
        ps.window(1, TimeUnit.MILLISECONDS, scheduler, 10, true).flatMap(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> v) throws Exception {
                return v;
            }
        }).subscribe(ts);
        ps.onNext(1);
        ts.awaitDone(1, TimeUnit.SECONDS).assertResult(1, 2);
    }

    @Test
    public void exactBoundedReentrant2() {
        TestScheduler scheduler = new TestScheduler();
        final FlowableProcessor<Integer> ps = PublishProcessor.<Integer>create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    ps.onNext(2);
                    ps.onComplete();
                }
            }
        };
        ps.window(1, TimeUnit.MILLISECONDS, scheduler, 2, true).flatMap(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> v) throws Exception {
                return v;
            }
        }).subscribe(ts);
        ps.onNext(1);
        ts.awaitDone(1, TimeUnit.SECONDS).assertResult(1, 2);
    }

    @Test
    public void skipReentrant() {
        TestScheduler scheduler = new TestScheduler();
        final FlowableProcessor<Integer> ps = PublishProcessor.<Integer>create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    ps.onNext(2);
                    ps.onComplete();
                }
            }
        };
        ps.window(1, 2, TimeUnit.MILLISECONDS, scheduler).flatMap(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> v) throws Exception {
                return v;
            }
        }).subscribe(ts);
        ps.onNext(1);
        ts.awaitDone(1, TimeUnit.SECONDS).assertResult(1, 2);
    }

    @Test
    public void sizeTimeTimeout() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> pp = PublishProcessor.<Integer>create();
        TestSubscriber<Flowable<Integer>> ts = pp.window(5, TimeUnit.MILLISECONDS, scheduler, 100).test().assertValueCount(1);
        scheduler.advanceTimeBy(5, TimeUnit.MILLISECONDS);
        ts.assertValueCount(2).assertNoErrors().assertNotComplete();
        ts.values().get(0).test().assertResult();
    }

    @Test
    public void periodicWindowCompletion() {
        TestScheduler scheduler = new TestScheduler();
        FlowableProcessor<Integer> ps = PublishProcessor.<Integer>create();
        TestSubscriber<Flowable<Integer>> ts = ps.window(5, TimeUnit.MILLISECONDS, scheduler, Long.MAX_VALUE, false).test();
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        ts.assertValueCount(21).assertNoErrors().assertNotComplete();
    }

    @Test
    public void periodicWindowCompletionRestartTimer() {
        TestScheduler scheduler = new TestScheduler();
        FlowableProcessor<Integer> ps = PublishProcessor.<Integer>create();
        TestSubscriber<Flowable<Integer>> ts = ps.window(5, TimeUnit.MILLISECONDS, scheduler, Long.MAX_VALUE, true).test();
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        ts.assertValueCount(21).assertNoErrors().assertNotComplete();
    }

    @Test
    public void periodicWindowCompletionBounded() {
        TestScheduler scheduler = new TestScheduler();
        FlowableProcessor<Integer> ps = PublishProcessor.<Integer>create();
        TestSubscriber<Flowable<Integer>> ts = ps.window(5, TimeUnit.MILLISECONDS, scheduler, 5, false).test();
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        ts.assertValueCount(21).assertNoErrors().assertNotComplete();
    }

    @Test
    public void periodicWindowCompletionRestartTimerBounded() {
        TestScheduler scheduler = new TestScheduler();
        FlowableProcessor<Integer> ps = PublishProcessor.<Integer>create();
        TestSubscriber<Flowable<Integer>> ts = ps.window(5, TimeUnit.MILLISECONDS, scheduler, 5, true).test();
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        ts.assertValueCount(21).assertNoErrors().assertNotComplete();
    }

    @Test
    public void periodicWindowCompletionRestartTimerBoundedSomeData() {
        TestScheduler scheduler = new TestScheduler();
        FlowableProcessor<Integer> ps = PublishProcessor.<Integer>create();
        TestSubscriber<Flowable<Integer>> ts = ps.window(5, TimeUnit.MILLISECONDS, scheduler, 2, true).test();
        ps.onNext(1);
        ps.onNext(2);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        ts.assertValueCount(22).assertNoErrors().assertNotComplete();
    }

    @Test
    public void countRestartsOnTimeTick() {
        TestScheduler scheduler = new TestScheduler();
        FlowableProcessor<Integer> ps = PublishProcessor.<Integer>create();
        TestSubscriber<Flowable<Integer>> ts = ps.window(5, TimeUnit.MILLISECONDS, scheduler, 5, true).doOnNext(new Consumer<Flowable<Integer>>() {

            @Override
            public void accept(Flowable<Integer> w) throws Throwable {
                w.subscribe();
            }
        }).test();
        // window #1
        ps.onNext(1);
        ps.onNext(2);
        scheduler.advanceTimeBy(5, TimeUnit.MILLISECONDS);
        // window #2
        ps.onNext(3);
        ps.onNext(4);
        ps.onNext(5);
        ps.onNext(6);
        ts.assertValueCount(2).assertNoErrors().assertNotComplete();
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Flowable<Object>>>() {

            @Override
            public Publisher<Flowable<Object>> apply(Flowable<Object> f) throws Exception {
                return f.window(1, TimeUnit.SECONDS, 1).takeLast(0);
            }
        });
    }

    @Test
    public void firstWindowMissingBackpressure() {
        Flowable.never().window(1, TimeUnit.SECONDS, 1).test(0L).assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void nextWindowMissingBackpressure() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Flowable<Integer>> ts = pp.window(1, TimeUnit.SECONDS, 1).test(1L);
        pp.onNext(1);
        ts.assertValueCount(1).assertError(MissingBackpressureException.class).assertNotComplete();
    }

    @Test
    public void cancelUpfront() {
        Flowable.never().window(1, TimeUnit.SECONDS, 1).test(0L, true).assertEmpty();
    }

    @Test
    public void nextWindowMissingBackpressureDrainOnSize() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Flowable<Integer>> ts = pp.window(1, TimeUnit.MINUTES, 1).subscribeWith(new TestSubscriber<Flowable<Integer>>(2) {

            int calls;

            @Override
            public void onNext(Flowable<Integer> t) {
                super.onNext(t);
                if (++calls == 2) {
                    pp.onNext(2);
                }
            }
        });
        pp.onNext(1);
        ts.assertValueCount(2).assertError(MissingBackpressureException.class).assertNotComplete();
    }

    @Test
    public void nextWindowMissingBackpressureDrainOnTime() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final TestScheduler sch = new TestScheduler();
        TestSubscriber<Flowable<Integer>> ts = pp.window(1, TimeUnit.MILLISECONDS, sch, 10).test(1);
        sch.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        ts.assertValueCount(1).assertError(MissingBackpressureException.class).assertNotComplete();
    }

    @Test
    public void exactTimeBoundNoInterruptWindowOutputOnComplete() throws Exception {
        final AtomicBoolean isInterrupted = new AtomicBoolean();
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final CountDownLatch doOnNextDone = new CountDownLatch(1);
        final CountDownLatch secondWindowProcessing = new CountDownLatch(1);
        pp.window(100, TimeUnit.MILLISECONDS).doOnNext(new Consumer<Flowable<Integer>>() {

            int count;

            @Override
            public void accept(Flowable<Integer> v) throws Exception {
                // System.out.println(Thread.currentThread());
                if (count++ == 1) {
                    secondWindowProcessing.countDown();
                    try {
                        Thread.sleep(200);
                        isInterrupted.set(Thread.interrupted());
                    } catch (InterruptedException ex) {
                        isInterrupted.set(true);
                    }
                    doOnNextDone.countDown();
                }
            }
        }).test();
        pp.onNext(1);
        assertTrue(secondWindowProcessing.await(5, TimeUnit.SECONDS));
        pp.onComplete();
        assertTrue(doOnNextDone.await(5, TimeUnit.SECONDS));
        assertFalse("The doOnNext got interrupted!", isInterrupted.get());
    }

    @Test
    @SuppressUndeliverable
    public void exactTimeBoundNoInterruptWindowOutputOnError() throws Exception {
        final AtomicBoolean isInterrupted = new AtomicBoolean();
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final CountDownLatch doOnNextDone = new CountDownLatch(1);
        final CountDownLatch secondWindowProcessing = new CountDownLatch(1);
        pp.window(100, TimeUnit.MILLISECONDS).doOnNext(new Consumer<Flowable<Integer>>() {

            int count;

            @Override
            public void accept(Flowable<Integer> v) throws Exception {
                // System.out.println(Thread.currentThread());
                if (count++ == 1) {
                    secondWindowProcessing.countDown();
                    try {
                        Thread.sleep(200);
                        isInterrupted.set(Thread.interrupted());
                    } catch (InterruptedException ex) {
                        isInterrupted.set(true);
                    }
                    doOnNextDone.countDown();
                }
            }
        }).test();
        pp.onNext(1);
        assertTrue(secondWindowProcessing.await(5, TimeUnit.SECONDS));
        pp.onError(new TestException());
        assertTrue(doOnNextDone.await(5, TimeUnit.SECONDS));
        assertFalse("The doOnNext got interrupted!", isInterrupted.get());
    }

    @Test
    public void exactTimeAndSizeBoundNoInterruptWindowOutputOnComplete() throws Exception {
        final AtomicBoolean isInterrupted = new AtomicBoolean();
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final CountDownLatch doOnNextDone = new CountDownLatch(1);
        final CountDownLatch secondWindowProcessing = new CountDownLatch(1);
        pp.window(100, TimeUnit.MILLISECONDS, 10).doOnNext(new Consumer<Flowable<Integer>>() {

            int count;

            @Override
            public void accept(Flowable<Integer> v) throws Exception {
                // System.out.println(Thread.currentThread());
                if (count++ == 1) {
                    secondWindowProcessing.countDown();
                    try {
                        Thread.sleep(200);
                        isInterrupted.set(Thread.interrupted());
                    } catch (InterruptedException ex) {
                        isInterrupted.set(true);
                    }
                    doOnNextDone.countDown();
                }
            }
        }).test();
        pp.onNext(1);
        assertTrue(secondWindowProcessing.await(5, TimeUnit.SECONDS));
        pp.onComplete();
        assertTrue(doOnNextDone.await(5, TimeUnit.SECONDS));
        assertFalse("The doOnNext got interrupted!", isInterrupted.get());
    }

    @Test
    @SuppressUndeliverable
    public void exactTimeAndSizeBoundNoInterruptWindowOutputOnError() throws Exception {
        final AtomicBoolean isInterrupted = new AtomicBoolean();
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final CountDownLatch doOnNextDone = new CountDownLatch(1);
        final CountDownLatch secondWindowProcessing = new CountDownLatch(1);
        pp.window(100, TimeUnit.MILLISECONDS, 10).doOnNext(new Consumer<Flowable<Integer>>() {

            int count;

            @Override
            public void accept(Flowable<Integer> v) throws Exception {
                // System.out.println(Thread.currentThread());
                if (count++ == 1) {
                    secondWindowProcessing.countDown();
                    try {
                        Thread.sleep(200);
                        isInterrupted.set(Thread.interrupted());
                    } catch (InterruptedException ex) {
                        isInterrupted.set(true);
                    }
                    doOnNextDone.countDown();
                }
            }
        }).test();
        pp.onNext(1);
        assertTrue(secondWindowProcessing.await(5, TimeUnit.SECONDS));
        pp.onError(new TestException());
        assertTrue(doOnNextDone.await(5, TimeUnit.SECONDS));
        assertFalse("The doOnNext got interrupted!", isInterrupted.get());
    }

    @Test
    public void skipTimeAndSizeBoundNoInterruptWindowOutputOnComplete() throws Exception {
        final AtomicBoolean isInterrupted = new AtomicBoolean();
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final CountDownLatch doOnNextDone = new CountDownLatch(1);
        final CountDownLatch secondWindowProcessing = new CountDownLatch(1);
        pp.window(90, 100, TimeUnit.MILLISECONDS).doOnNext(new Consumer<Flowable<Integer>>() {

            int count;

            @Override
            public void accept(Flowable<Integer> v) throws Exception {
                // System.out.println(Thread.currentThread());
                if (count++ == 1) {
                    secondWindowProcessing.countDown();
                    try {
                        Thread.sleep(200);
                        isInterrupted.set(Thread.interrupted());
                    } catch (InterruptedException ex) {
                        isInterrupted.set(true);
                    }
                    doOnNextDone.countDown();
                }
            }
        }).test();
        pp.onNext(1);
        assertTrue(secondWindowProcessing.await(5, TimeUnit.SECONDS));
        pp.onComplete();
        assertTrue(doOnNextDone.await(5, TimeUnit.SECONDS));
        assertFalse("The doOnNext got interrupted!", isInterrupted.get());
    }

    @Test
    @SuppressUndeliverable
    public void skipTimeAndSizeBoundNoInterruptWindowOutputOnError() throws Exception {
        final AtomicBoolean isInterrupted = new AtomicBoolean();
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final CountDownLatch doOnNextDone = new CountDownLatch(1);
        final CountDownLatch secondWindowProcessing = new CountDownLatch(1);
        pp.window(90, 100, TimeUnit.MILLISECONDS).doOnNext(new Consumer<Flowable<Integer>>() {

            int count;

            @Override
            public void accept(Flowable<Integer> v) throws Exception {
                // System.out.println(Thread.currentThread());
                if (count++ == 1) {
                    secondWindowProcessing.countDown();
                    try {
                        Thread.sleep(200);
                        isInterrupted.set(Thread.interrupted());
                    } catch (InterruptedException ex) {
                        isInterrupted.set(true);
                    }
                    doOnNextDone.countDown();
                }
            }
        }).test();
        pp.onNext(1);
        assertTrue(secondWindowProcessing.await(5, TimeUnit.SECONDS));
        pp.onError(new TestException());
        assertTrue(doOnNextDone.await(5, TimeUnit.SECONDS));
        assertFalse("The doOnNext got interrupted!", isInterrupted.get());
    }

    @Test
    public void cancellingWindowCancelsUpstreamExactTime() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.window(10, TimeUnit.MINUTES).take(1).flatMap(new Function<Flowable<Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Integer> w) throws Throwable {
                return w.take(1);
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertResult(1);
        assertFalse("Processor still has subscribers!", pp.hasSubscribers());
    }

    @Test
    public void windowAbandonmentCancelsUpstreamExactTime() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final AtomicReference<Flowable<Integer>> inner = new AtomicReference<>();
        TestSubscriber<Flowable<Integer>> ts = pp.window(10, TimeUnit.MINUTES).take(1).doOnNext(new Consumer<Flowable<Integer>>() {

            @Override
            public void accept(Flowable<Integer> v) throws Throwable {
                inner.set(v);
            }
        }).test();
        assertFalse("Processor still has subscribers!", pp.hasSubscribers());
        ts.assertValueCount(1).assertNoErrors().assertComplete();
        inner.get().test().assertResult();
    }

    @Test
    public void cancellingWindowCancelsUpstreamExactTimeAndSize() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.window(10, TimeUnit.MINUTES, 100).take(1).flatMap(new Function<Flowable<Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Integer> w) throws Throwable {
                return w.take(1);
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertResult(1);
        assertFalse("Processor still has subscribers!", pp.hasSubscribers());
    }

    @Test
    public void windowAbandonmentCancelsUpstreamExactTimeAndSize() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final AtomicReference<Flowable<Integer>> inner = new AtomicReference<>();
        TestSubscriber<Flowable<Integer>> ts = pp.window(10, TimeUnit.MINUTES, 100).take(1).doOnNext(new Consumer<Flowable<Integer>>() {

            @Override
            public void accept(Flowable<Integer> v) throws Throwable {
                inner.set(v);
            }
        }).test();
        assertFalse("Processor still has subscribers!", pp.hasSubscribers());
        ts.assertValueCount(1).assertNoErrors().assertComplete();
        inner.get().test().assertResult();
    }

    @Test
    public void cancellingWindowCancelsUpstreamExactTimeSkip() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.window(10, 15, TimeUnit.MINUTES).take(1).flatMap(new Function<Flowable<Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Integer> w) throws Throwable {
                return w.take(1);
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertResult(1);
        assertFalse("Processor still has subscribers!", pp.hasSubscribers());
    }

    @Test
    public void windowAbandonmentCancelsUpstreamExactTimeSkip() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final AtomicReference<Flowable<Integer>> inner = new AtomicReference<>();
        TestSubscriber<Flowable<Integer>> ts = pp.window(10, 15, TimeUnit.MINUTES).take(1).doOnNext(new Consumer<Flowable<Integer>>() {

            @Override
            public void accept(Flowable<Integer> v) throws Throwable {
                inner.set(v);
            }
        }).test();
        assertFalse("Processor still has subscribers!", pp.hasSubscribers());
        ts.assertValueCount(1).assertNoErrors().assertComplete();
        inner.get().test().assertResult();
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().window(1, TimeUnit.SECONDS));
    }

    @Test
    public void timedBoundarySignalAndDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            TestScheduler scheduler = new TestScheduler();
            PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriber<Flowable<Integer>> ts = pp.window(1, TimeUnit.MINUTES, scheduler, 1).test();
            TestHelper.race(() -> pp.onNext(1), () -> ts.cancel());
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableWindowWithTimeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedAndCount() throws java.lang.Throwable {
            this.payloads.timedAndCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timed() throws java.lang.Throwable {
            this.payloads.timed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exactWindowSize() throws java.lang.Throwable {
            this.payloads.exactWindowSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeFlatMapCompletes() throws java.lang.Throwable {
            this.payloads.takeFlatMapCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timespanTimeskipCustomSchedulerBufferSize() throws java.lang.Throwable {
            this.payloads.timespanTimeskipCustomSchedulerBufferSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timespanDefaultSchedulerSize() throws java.lang.Throwable {
            this.payloads.timespanDefaultSchedulerSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timespanDefaultSchedulerSizeRestart() throws java.lang.Throwable {
            this.payloads.timespanDefaultSchedulerSizeRestart.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_invalidSpan() throws java.lang.Throwable {
            this.payloads.invalidSpan.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timespanTimeskipDefaultScheduler() throws java.lang.Throwable {
            this.payloads.timespanTimeskipDefaultScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timespanTimeskipCustomScheduler() throws java.lang.Throwable {
            this.payloads.timespanTimeskipCustomScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeskipJustOverlap() throws java.lang.Throwable {
            this.payloads.timeskipJustOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeskipJustSkip() throws java.lang.Throwable {
            this.payloads.timeskipJustSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeskipSkipping() throws java.lang.Throwable {
            this.payloads.timeskipSkipping.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeskipOverlapping() throws java.lang.Throwable {
            this.payloads.timeskipOverlapping.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exactOnError() throws java.lang.Throwable {
            this.payloads.exactOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overlappingOnError() throws java.lang.Throwable {
            this.payloads.overlappingOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipOnError() throws java.lang.Throwable {
            this.payloads.skipOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exactBackpressure() throws java.lang.Throwable {
            this.payloads.exactBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipBackpressure() throws java.lang.Throwable {
            this.payloads.skipBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overlapBackpressure() throws java.lang.Throwable {
            this.payloads.overlapBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exactBackpressure2() throws java.lang.Throwable {
            this.payloads.exactBackpressure2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipBackpressure2() throws java.lang.Throwable {
            this.payloads.skipBackpressure2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overlapBackpressure2() throws java.lang.Throwable {
            this.payloads.overlapBackpressure2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_restartTimer() throws java.lang.Throwable {
            this.payloads.restartTimer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exactBoundaryError() throws java.lang.Throwable {
            this.payloads.exactBoundaryError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_restartTimerMany() throws java.lang.Throwable {
            this.payloads.restartTimerMany.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exactUnboundedReentrant() throws java.lang.Throwable {
            this.payloads.exactUnboundedReentrant.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exactBoundedReentrant() throws java.lang.Throwable {
            this.payloads.exactBoundedReentrant.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exactBoundedReentrant2() throws java.lang.Throwable {
            this.payloads.exactBoundedReentrant2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipReentrant() throws java.lang.Throwable {
            this.payloads.skipReentrant.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sizeTimeTimeout() throws java.lang.Throwable {
            this.payloads.sizeTimeTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_periodicWindowCompletion() throws java.lang.Throwable {
            this.payloads.periodicWindowCompletion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_periodicWindowCompletionRestartTimer() throws java.lang.Throwable {
            this.payloads.periodicWindowCompletionRestartTimer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_periodicWindowCompletionBounded() throws java.lang.Throwable {
            this.payloads.periodicWindowCompletionBounded.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_periodicWindowCompletionRestartTimerBounded() throws java.lang.Throwable {
            this.payloads.periodicWindowCompletionRestartTimerBounded.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_periodicWindowCompletionRestartTimerBoundedSomeData() throws java.lang.Throwable {
            this.payloads.periodicWindowCompletionRestartTimerBoundedSomeData.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_countRestartsOnTimeTick() throws java.lang.Throwable {
            this.payloads.countRestartsOnTimeTick.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWindowMissingBackpressure() throws java.lang.Throwable {
            this.payloads.firstWindowMissingBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextWindowMissingBackpressure() throws java.lang.Throwable {
            this.payloads.nextWindowMissingBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelUpfront() throws java.lang.Throwable {
            this.payloads.cancelUpfront.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextWindowMissingBackpressureDrainOnSize() throws java.lang.Throwable {
            this.payloads.nextWindowMissingBackpressureDrainOnSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextWindowMissingBackpressureDrainOnTime() throws java.lang.Throwable {
            this.payloads.nextWindowMissingBackpressureDrainOnTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exactTimeBoundNoInterruptWindowOutputOnComplete() throws java.lang.Throwable {
            this.payloads.exactTimeBoundNoInterruptWindowOutputOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exactTimeBoundNoInterruptWindowOutputOnError() throws java.lang.Throwable {
            this.payloads.exactTimeBoundNoInterruptWindowOutputOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exactTimeAndSizeBoundNoInterruptWindowOutputOnComplete() throws java.lang.Throwable {
            this.payloads.exactTimeAndSizeBoundNoInterruptWindowOutputOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exactTimeAndSizeBoundNoInterruptWindowOutputOnError() throws java.lang.Throwable {
            this.payloads.exactTimeAndSizeBoundNoInterruptWindowOutputOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipTimeAndSizeBoundNoInterruptWindowOutputOnComplete() throws java.lang.Throwable {
            this.payloads.skipTimeAndSizeBoundNoInterruptWindowOutputOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipTimeAndSizeBoundNoInterruptWindowOutputOnError() throws java.lang.Throwable {
            this.payloads.skipTimeAndSizeBoundNoInterruptWindowOutputOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellingWindowCancelsUpstreamExactTime() throws java.lang.Throwable {
            this.payloads.cancellingWindowCancelsUpstreamExactTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowAbandonmentCancelsUpstreamExactTime() throws java.lang.Throwable {
            this.payloads.windowAbandonmentCancelsUpstreamExactTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellingWindowCancelsUpstreamExactTimeAndSize() throws java.lang.Throwable {
            this.payloads.cancellingWindowCancelsUpstreamExactTimeAndSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowAbandonmentCancelsUpstreamExactTimeAndSize() throws java.lang.Throwable {
            this.payloads.windowAbandonmentCancelsUpstreamExactTimeAndSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellingWindowCancelsUpstreamExactTimeSkip() throws java.lang.Throwable {
            this.payloads.cancellingWindowCancelsUpstreamExactTimeSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowAbandonmentCancelsUpstreamExactTimeSkip() throws java.lang.Throwable {
            this.payloads.windowAbandonmentCancelsUpstreamExactTimeSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedBoundarySignalAndDisposeRace() throws java.lang.Throwable {
            this.payloads.timedBoundarySignalAndDisposeRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithTimeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithTimeTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithTimeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithTimeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableWindowWithTimeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithTimeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableWindowWithTimeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableWindowWithTimeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement timedAndCount;

            public org.junit.runners.model.Statement timed;

            public org.junit.runners.model.Statement exactWindowSize;

            public org.junit.runners.model.Statement takeFlatMapCompletes;

            public org.junit.runners.model.Statement timespanTimeskipCustomSchedulerBufferSize;

            public org.junit.runners.model.Statement timespanDefaultSchedulerSize;

            public org.junit.runners.model.Statement timespanDefaultSchedulerSizeRestart;

            public org.junit.runners.model.Statement invalidSpan;

            public org.junit.runners.model.Statement timespanTimeskipDefaultScheduler;

            public org.junit.runners.model.Statement timespanTimeskipCustomScheduler;

            public org.junit.runners.model.Statement timeskipJustOverlap;

            public org.junit.runners.model.Statement timeskipJustSkip;

            public org.junit.runners.model.Statement timeskipSkipping;

            public org.junit.runners.model.Statement timeskipOverlapping;

            public org.junit.runners.model.Statement exactOnError;

            public org.junit.runners.model.Statement overlappingOnError;

            public org.junit.runners.model.Statement skipOnError;

            public org.junit.runners.model.Statement exactBackpressure;

            public org.junit.runners.model.Statement skipBackpressure;

            public org.junit.runners.model.Statement overlapBackpressure;

            public org.junit.runners.model.Statement exactBackpressure2;

            public org.junit.runners.model.Statement skipBackpressure2;

            public org.junit.runners.model.Statement overlapBackpressure2;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement restartTimer;

            public org.junit.runners.model.Statement exactBoundaryError;

            public org.junit.runners.model.Statement restartTimerMany;

            public org.junit.runners.model.Statement exactUnboundedReentrant;

            public org.junit.runners.model.Statement exactBoundedReentrant;

            public org.junit.runners.model.Statement exactBoundedReentrant2;

            public org.junit.runners.model.Statement skipReentrant;

            public org.junit.runners.model.Statement sizeTimeTimeout;

            public org.junit.runners.model.Statement periodicWindowCompletion;

            public org.junit.runners.model.Statement periodicWindowCompletionRestartTimer;

            public org.junit.runners.model.Statement periodicWindowCompletionBounded;

            public org.junit.runners.model.Statement periodicWindowCompletionRestartTimerBounded;

            public org.junit.runners.model.Statement periodicWindowCompletionRestartTimerBoundedSomeData;

            public org.junit.runners.model.Statement countRestartsOnTimeTick;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement firstWindowMissingBackpressure;

            public org.junit.runners.model.Statement nextWindowMissingBackpressure;

            public org.junit.runners.model.Statement cancelUpfront;

            public org.junit.runners.model.Statement nextWindowMissingBackpressureDrainOnSize;

            public org.junit.runners.model.Statement nextWindowMissingBackpressureDrainOnTime;

            public org.junit.runners.model.Statement exactTimeBoundNoInterruptWindowOutputOnComplete;

            public org.junit.runners.model.Statement exactTimeBoundNoInterruptWindowOutputOnError;

            public org.junit.runners.model.Statement exactTimeAndSizeBoundNoInterruptWindowOutputOnComplete;

            public org.junit.runners.model.Statement exactTimeAndSizeBoundNoInterruptWindowOutputOnError;

            public org.junit.runners.model.Statement skipTimeAndSizeBoundNoInterruptWindowOutputOnComplete;

            public org.junit.runners.model.Statement skipTimeAndSizeBoundNoInterruptWindowOutputOnError;

            public org.junit.runners.model.Statement cancellingWindowCancelsUpstreamExactTime;

            public org.junit.runners.model.Statement windowAbandonmentCancelsUpstreamExactTime;

            public org.junit.runners.model.Statement cancellingWindowCancelsUpstreamExactTimeAndSize;

            public org.junit.runners.model.Statement windowAbandonmentCancelsUpstreamExactTimeAndSize;

            public org.junit.runners.model.Statement cancellingWindowCancelsUpstreamExactTimeSkip;

            public org.junit.runners.model.Statement windowAbandonmentCancelsUpstreamExactTimeSkip;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement timedBoundarySignalAndDisposeRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.timedAndCount = _ClassStatement.forPayload(FlowableWindowWithTimeTest::timedAndCount, "timedAndCount", this);
            this.payloads.timed = _ClassStatement.forPayload(FlowableWindowWithTimeTest::timed, "timed", this);
            this.payloads.exactWindowSize = _ClassStatement.forPayload(FlowableWindowWithTimeTest::exactWindowSize, "exactWindowSize", this);
            this.payloads.takeFlatMapCompletes = _ClassStatement.forPayload(FlowableWindowWithTimeTest::takeFlatMapCompletes, "takeFlatMapCompletes", this);
            this.payloads.timespanTimeskipCustomSchedulerBufferSize = _ClassStatement.forPayload(FlowableWindowWithTimeTest::timespanTimeskipCustomSchedulerBufferSize, "timespanTimeskipCustomSchedulerBufferSize", this);
            this.payloads.timespanDefaultSchedulerSize = _ClassStatement.forPayload(FlowableWindowWithTimeTest::timespanDefaultSchedulerSize, "timespanDefaultSchedulerSize", this);
            this.payloads.timespanDefaultSchedulerSizeRestart = _ClassStatement.forPayload(FlowableWindowWithTimeTest::timespanDefaultSchedulerSizeRestart, "timespanDefaultSchedulerSizeRestart", this);
            this.payloads.invalidSpan = _ClassStatement.forPayload(FlowableWindowWithTimeTest::invalidSpan, "invalidSpan", this);
            this.payloads.timespanTimeskipDefaultScheduler = _ClassStatement.forPayload(FlowableWindowWithTimeTest::timespanTimeskipDefaultScheduler, "timespanTimeskipDefaultScheduler", this);
            this.payloads.timespanTimeskipCustomScheduler = _ClassStatement.forPayload(FlowableWindowWithTimeTest::timespanTimeskipCustomScheduler, "timespanTimeskipCustomScheduler", this);
            this.payloads.timeskipJustOverlap = _ClassStatement.forPayload(FlowableWindowWithTimeTest::timeskipJustOverlap, "timeskipJustOverlap", this);
            this.payloads.timeskipJustSkip = _ClassStatement.forPayload(FlowableWindowWithTimeTest::timeskipJustSkip, "timeskipJustSkip", this);
            this.payloads.timeskipSkipping = _ClassStatement.forPayload(FlowableWindowWithTimeTest::timeskipSkipping, "timeskipSkipping", this);
            this.payloads.timeskipOverlapping = _ClassStatement.forPayload(FlowableWindowWithTimeTest::timeskipOverlapping, "timeskipOverlapping", this);
            this.payloads.exactOnError = _ClassStatement.forPayload(FlowableWindowWithTimeTest::exactOnError, "exactOnError", this);
            this.payloads.overlappingOnError = _ClassStatement.forPayload(FlowableWindowWithTimeTest::overlappingOnError, "overlappingOnError", this);
            this.payloads.skipOnError = _ClassStatement.forPayload(FlowableWindowWithTimeTest::skipOnError, "skipOnError", this);
            this.payloads.exactBackpressure = _ClassStatement.forPayload(FlowableWindowWithTimeTest::exactBackpressure, "exactBackpressure", this);
            this.payloads.skipBackpressure = _ClassStatement.forPayload(FlowableWindowWithTimeTest::skipBackpressure, "skipBackpressure", this);
            this.payloads.overlapBackpressure = _ClassStatement.forPayload(FlowableWindowWithTimeTest::overlapBackpressure, "overlapBackpressure", this);
            this.payloads.exactBackpressure2 = _ClassStatement.forPayload(FlowableWindowWithTimeTest::exactBackpressure2, "exactBackpressure2", this);
            this.payloads.skipBackpressure2 = _ClassStatement.forPayload(FlowableWindowWithTimeTest::skipBackpressure2, "skipBackpressure2", this);
            this.payloads.overlapBackpressure2 = _ClassStatement.forPayload(FlowableWindowWithTimeTest::overlapBackpressure2, "overlapBackpressure2", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableWindowWithTimeTest::dispose, "dispose", this);
            this.payloads.restartTimer = _ClassStatement.forPayload(FlowableWindowWithTimeTest::restartTimer, "restartTimer", this);
            this.payloads.exactBoundaryError = _ClassStatement.forPayload(FlowableWindowWithTimeTest::exactBoundaryError, "exactBoundaryError", this);
            this.payloads.restartTimerMany = _ClassStatement.forPayload(FlowableWindowWithTimeTest::restartTimerMany, "restartTimerMany", this);
            this.payloads.exactUnboundedReentrant = _ClassStatement.forPayload(FlowableWindowWithTimeTest::exactUnboundedReentrant, "exactUnboundedReentrant", this);
            this.payloads.exactBoundedReentrant = _ClassStatement.forPayload(FlowableWindowWithTimeTest::exactBoundedReentrant, "exactBoundedReentrant", this);
            this.payloads.exactBoundedReentrant2 = _ClassStatement.forPayload(FlowableWindowWithTimeTest::exactBoundedReentrant2, "exactBoundedReentrant2", this);
            this.payloads.skipReentrant = _ClassStatement.forPayload(FlowableWindowWithTimeTest::skipReentrant, "skipReentrant", this);
            this.payloads.sizeTimeTimeout = _ClassStatement.forPayload(FlowableWindowWithTimeTest::sizeTimeTimeout, "sizeTimeTimeout", this);
            this.payloads.periodicWindowCompletion = _ClassStatement.forPayload(FlowableWindowWithTimeTest::periodicWindowCompletion, "periodicWindowCompletion", this);
            this.payloads.periodicWindowCompletionRestartTimer = _ClassStatement.forPayload(FlowableWindowWithTimeTest::periodicWindowCompletionRestartTimer, "periodicWindowCompletionRestartTimer", this);
            this.payloads.periodicWindowCompletionBounded = _ClassStatement.forPayload(FlowableWindowWithTimeTest::periodicWindowCompletionBounded, "periodicWindowCompletionBounded", this);
            this.payloads.periodicWindowCompletionRestartTimerBounded = _ClassStatement.forPayload(FlowableWindowWithTimeTest::periodicWindowCompletionRestartTimerBounded, "periodicWindowCompletionRestartTimerBounded", this);
            this.payloads.periodicWindowCompletionRestartTimerBoundedSomeData = _ClassStatement.forPayload(FlowableWindowWithTimeTest::periodicWindowCompletionRestartTimerBoundedSomeData, "periodicWindowCompletionRestartTimerBoundedSomeData", this);
            this.payloads.countRestartsOnTimeTick = _ClassStatement.forPayload(FlowableWindowWithTimeTest::countRestartsOnTimeTick, "countRestartsOnTimeTick", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableWindowWithTimeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.firstWindowMissingBackpressure = _ClassStatement.forPayload(FlowableWindowWithTimeTest::firstWindowMissingBackpressure, "firstWindowMissingBackpressure", this);
            this.payloads.nextWindowMissingBackpressure = _ClassStatement.forPayload(FlowableWindowWithTimeTest::nextWindowMissingBackpressure, "nextWindowMissingBackpressure", this);
            this.payloads.cancelUpfront = _ClassStatement.forPayload(FlowableWindowWithTimeTest::cancelUpfront, "cancelUpfront", this);
            this.payloads.nextWindowMissingBackpressureDrainOnSize = _ClassStatement.forPayload(FlowableWindowWithTimeTest::nextWindowMissingBackpressureDrainOnSize, "nextWindowMissingBackpressureDrainOnSize", this);
            this.payloads.nextWindowMissingBackpressureDrainOnTime = _ClassStatement.forPayload(FlowableWindowWithTimeTest::nextWindowMissingBackpressureDrainOnTime, "nextWindowMissingBackpressureDrainOnTime", this);
            this.payloads.exactTimeBoundNoInterruptWindowOutputOnComplete = _ClassStatement.forPayload(FlowableWindowWithTimeTest::exactTimeBoundNoInterruptWindowOutputOnComplete, "exactTimeBoundNoInterruptWindowOutputOnComplete", this);
            this.payloads.exactTimeBoundNoInterruptWindowOutputOnError = _ClassStatement.forPayload(FlowableWindowWithTimeTest::exactTimeBoundNoInterruptWindowOutputOnError, "exactTimeBoundNoInterruptWindowOutputOnError", this);
            this.payloads.exactTimeAndSizeBoundNoInterruptWindowOutputOnComplete = _ClassStatement.forPayload(FlowableWindowWithTimeTest::exactTimeAndSizeBoundNoInterruptWindowOutputOnComplete, "exactTimeAndSizeBoundNoInterruptWindowOutputOnComplete", this);
            this.payloads.exactTimeAndSizeBoundNoInterruptWindowOutputOnError = _ClassStatement.forPayload(FlowableWindowWithTimeTest::exactTimeAndSizeBoundNoInterruptWindowOutputOnError, "exactTimeAndSizeBoundNoInterruptWindowOutputOnError", this);
            this.payloads.skipTimeAndSizeBoundNoInterruptWindowOutputOnComplete = _ClassStatement.forPayload(FlowableWindowWithTimeTest::skipTimeAndSizeBoundNoInterruptWindowOutputOnComplete, "skipTimeAndSizeBoundNoInterruptWindowOutputOnComplete", this);
            this.payloads.skipTimeAndSizeBoundNoInterruptWindowOutputOnError = _ClassStatement.forPayload(FlowableWindowWithTimeTest::skipTimeAndSizeBoundNoInterruptWindowOutputOnError, "skipTimeAndSizeBoundNoInterruptWindowOutputOnError", this);
            this.payloads.cancellingWindowCancelsUpstreamExactTime = _ClassStatement.forPayload(FlowableWindowWithTimeTest::cancellingWindowCancelsUpstreamExactTime, "cancellingWindowCancelsUpstreamExactTime", this);
            this.payloads.windowAbandonmentCancelsUpstreamExactTime = _ClassStatement.forPayload(FlowableWindowWithTimeTest::windowAbandonmentCancelsUpstreamExactTime, "windowAbandonmentCancelsUpstreamExactTime", this);
            this.payloads.cancellingWindowCancelsUpstreamExactTimeAndSize = _ClassStatement.forPayload(FlowableWindowWithTimeTest::cancellingWindowCancelsUpstreamExactTimeAndSize, "cancellingWindowCancelsUpstreamExactTimeAndSize", this);
            this.payloads.windowAbandonmentCancelsUpstreamExactTimeAndSize = _ClassStatement.forPayload(FlowableWindowWithTimeTest::windowAbandonmentCancelsUpstreamExactTimeAndSize, "windowAbandonmentCancelsUpstreamExactTimeAndSize", this);
            this.payloads.cancellingWindowCancelsUpstreamExactTimeSkip = _ClassStatement.forPayload(FlowableWindowWithTimeTest::cancellingWindowCancelsUpstreamExactTimeSkip, "cancellingWindowCancelsUpstreamExactTimeSkip", this);
            this.payloads.windowAbandonmentCancelsUpstreamExactTimeSkip = _ClassStatement.forPayload(FlowableWindowWithTimeTest::windowAbandonmentCancelsUpstreamExactTimeSkip, "windowAbandonmentCancelsUpstreamExactTimeSkip", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableWindowWithTimeTest::badRequest, "badRequest", this);
            this.payloads.timedBoundarySignalAndDisposeRace = _ClassStatement.forPayload(FlowableWindowWithTimeTest::timedBoundarySignalAndDisposeRace, "timedBoundarySignalAndDisposeRace", this);
        }
    }
}
