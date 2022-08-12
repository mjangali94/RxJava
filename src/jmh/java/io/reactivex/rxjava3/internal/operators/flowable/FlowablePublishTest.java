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
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.flowables.ConnectableFlowable;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.fuseable.HasUpstreamPublisher;
import io.reactivex.rxjava3.internal.operators.flowable.FlowablePublish.*;
import io.reactivex.rxjava3.internal.schedulers.ImmediateThinScheduler;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowablePublishTest extends RxJavaTest {

    @Test
    public void publish() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        ConnectableFlowable<String> f = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(final Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        counter.incrementAndGet();
                        subscriber.onNext("one");
                        subscriber.onComplete();
                    }
                }).start();
            }
        }).publish();
        final CountDownLatch latch = new CountDownLatch(2);
        // subscribe once
        f.subscribe(new Consumer<String>() {

            @Override
            public void accept(String v) {
                assertEquals("one", v);
                latch.countDown();
            }
        });
        // subscribe again
        f.subscribe(new Consumer<String>() {

            @Override
            public void accept(String v) {
                assertEquals("one", v);
                latch.countDown();
            }
        });
        Disposable connection = f.connect();
        try {
            if (!latch.await(1000, TimeUnit.MILLISECONDS)) {
                fail("subscriptions did not receive values");
            }
            assertEquals(1, counter.get());
        } finally {
            connection.dispose();
        }
    }

    @Test
    public void backpressureFastSlow() {
        ConnectableFlowable<Integer> is = Flowable.range(1, Flowable.bufferSize() * 2).publish();
        Flowable<Integer> fast = is.observeOn(Schedulers.computation()).doOnComplete(new Action() {

            @Override
            public void run() {
                // System.out.println("^^^^^^^^^^^^^ completed FAST");
            }
        });
        Flowable<Integer> slow = is.observeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

            int c;

            @Override
            public Integer apply(Integer i) {
                if (c == 0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
                c++;
                return i;
            }
        }).doOnComplete(new Action() {

            @Override
            public void run() {
                // System.out.println("^^^^^^^^^^^^^ completed SLOW");
            }
        });
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.merge(fast, slow).subscribe(ts);
        is.connect();
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        assertEquals(Flowable.bufferSize() * 4, ts.values().size());
    }

    // use case from https://github.com/ReactiveX/RxJava/issues/1732
    @Test
    public void takeUntilWithPublishedStreamUsingSelector() {
        final AtomicInteger emitted = new AtomicInteger();
        Flowable<Integer> xs = Flowable.range(0, Flowable.bufferSize() * 2).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                emitted.incrementAndGet();
            }
        });
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        xs.publish(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> xs) {
                return xs.takeUntil(xs.skipWhile(new Predicate<Integer>() {

                    @Override
                    public boolean test(Integer i) {
                        return i <= 3;
                    }
                }));
            }
        }).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        ts.assertValues(0, 1, 2, 3);
        assertEquals(5, emitted.get());
        // System.out.println(ts.values());
    }

    // use case from https://github.com/ReactiveX/RxJava/issues/1732
    @Test
    public void takeUntilWithPublishedStream() {
        Flowable<Integer> xs = Flowable.range(0, Flowable.bufferSize() * 2);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        ConnectableFlowable<Integer> xsp = xs.publish();
        xsp.takeUntil(xsp.skipWhile(new Predicate<Integer>() {

            @Override
            public boolean test(Integer i) {
                return i <= 3;
            }
        })).subscribe(ts);
        xsp.connect();
        // System.out.println(ts.values());
    }

    @Test
    public void backpressureTwoConsumers() {
        final AtomicInteger sourceEmission = new AtomicInteger();
        final AtomicBoolean sourceUnsubscribed = new AtomicBoolean();
        final Flowable<Integer> source = Flowable.range(1, 100).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                sourceEmission.incrementAndGet();
            }
        }).doOnCancel(new Action() {

            @Override
            public void run() {
                sourceUnsubscribed.set(true);
            }
        }).share();
        ;
        final AtomicBoolean child1Unsubscribed = new AtomicBoolean();
        final AtomicBoolean child2Unsubscribed = new AtomicBoolean();
        final TestSubscriber<Integer> ts2 = new TestSubscriber<>();
        final TestSubscriber<Integer> ts1 = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                if (values().size() == 2) {
                    source.doOnCancel(new Action() {

                        @Override
                        public void run() {
                            child2Unsubscribed.set(true);
                        }
                    }).take(5).subscribe(ts2);
                }
                super.onNext(t);
            }
        };
        source.doOnCancel(new Action() {

            @Override
            public void run() {
                child1Unsubscribed.set(true);
            }
        }).take(5).subscribe(ts1);
        ts1.awaitDone(5, TimeUnit.SECONDS);
        ts2.awaitDone(5, TimeUnit.SECONDS);
        ts1.assertNoErrors();
        ts2.assertNoErrors();
        assertTrue(sourceUnsubscribed.get());
        assertTrue(child1Unsubscribed.get());
        assertTrue(child2Unsubscribed.get());
        ts1.assertValues(1, 2, 3, 4, 5);
        ts2.assertValues(4, 5, 6, 7, 8);
        assertEquals(8, sourceEmission.get());
    }

    @Test
    public void connectWithNoSubscriber() {
        TestScheduler scheduler = new TestScheduler();
        ConnectableFlowable<Long> cf = Flowable.interval(10, 10, TimeUnit.MILLISECONDS, scheduler).take(3).publish();
        cf.connect();
        // Emit 0
        scheduler.advanceTimeBy(15, TimeUnit.MILLISECONDS);
        TestSubscriber<Long> subscriber = new TestSubscriber<>();
        cf.subscribe(subscriber);
        // Emit 1 and 2
        scheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS);
        // 3.x: Flowable.publish no longer drains the input buffer if there are no subscribers
        subscriber.assertResult(0L, 1L, 2L);
    }

    @Test
    public void subscribeAfterDisconnectThenConnect() {
        ConnectableFlowable<Integer> source = Flowable.just(1).publish();
        TestSubscriberEx<Integer> ts1 = new TestSubscriberEx<>();
        source.subscribe(ts1);
        Disposable connection = source.connect();
        ts1.assertValue(1);
        ts1.assertNoErrors();
        ts1.assertTerminated();
        source.reset();
        TestSubscriberEx<Integer> ts2 = new TestSubscriberEx<>();
        source.subscribe(ts2);
        Disposable connection2 = source.connect();
        ts2.assertValue(1);
        ts2.assertNoErrors();
        ts2.assertTerminated();
        // System.out.println(connection);
        // System.out.println(connection2);
    }

    @Test
    public void noSubscriberRetentionOnCompleted() {
        FlowablePublish<Integer> source = (FlowablePublish<Integer>) Flowable.just(1).publish();
        TestSubscriberEx<Integer> ts1 = new TestSubscriberEx<>();
        source.subscribe(ts1);
        ts1.assertNoValues();
        ts1.assertNoErrors();
        ts1.assertNotComplete();
        source.connect();
        ts1.assertValue(1);
        ts1.assertNoErrors();
        ts1.assertTerminated();
        assertEquals(0, source.current.get().subscribers.get().length);
    }

    @Test
    public void nonNullConnection() {
        ConnectableFlowable<Object> source = Flowable.never().publish();
        assertNotNull(source.connect());
        assertNotNull(source.connect());
    }

    @Test
    public void noDisconnectSomeoneElse() {
        ConnectableFlowable<Object> source = Flowable.never().publish();
        Disposable connection1 = source.connect();
        Disposable connection2 = source.connect();
        connection1.dispose();
        Disposable connection3 = source.connect();
        connection2.dispose();
        assertTrue(checkPublishDisposed(connection1));
        assertTrue(checkPublishDisposed(connection2));
        assertFalse(checkPublishDisposed(connection3));
    }

    @SuppressWarnings("unchecked")
    static boolean checkPublishDisposed(Disposable d) {
        return ((FlowablePublish.PublishConnection<Object>) d).isDisposed();
    }

    @Test
    public void zeroRequested() {
        ConnectableFlowable<Integer> source = Flowable.just(1).publish();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0L);
        source.subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        source.connect();
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(5);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertTerminated();
    }

    @Test
    public void connectIsIdempotent() {
        final AtomicInteger calls = new AtomicInteger();
        Flowable<Integer> source = Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> t) {
                t.onSubscribe(new BooleanSubscription());
                calls.getAndIncrement();
            }
        });
        ConnectableFlowable<Integer> conn = source.publish();
        assertEquals(0, calls.get());
        conn.connect();
        conn.connect();
        assertEquals(1, calls.get());
        conn.connect().dispose();
        conn.connect();
        conn.connect();
        assertEquals(2, calls.get());
    }

    @Test
    public void syncFusedObserveOn() {
        ConnectableFlowable<Integer> cf = Flowable.range(0, 1000).publish();
        Flowable<Integer> obs = cf.observeOn(Schedulers.computation());
        for (int i = 0; i < 1000; i++) {
            for (int j = 1; j < 6; j++) {
                List<TestSubscriberEx<Integer>> tss = new ArrayList<>();
                for (int k = 1; k < j; k++) {
                    TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
                    tss.add(ts);
                    obs.subscribe(ts);
                }
                Disposable connection = cf.connect();
                for (TestSubscriberEx<Integer> ts : tss) {
                    ts.awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(1000).assertNoErrors().assertComplete();
                }
                connection.dispose();
            }
        }
    }

    @Test
    public void syncFusedObserveOn2() {
        ConnectableFlowable<Integer> cf = Flowable.range(0, 1000).publish();
        Flowable<Integer> obs = cf.observeOn(ImmediateThinScheduler.INSTANCE);
        for (int i = 0; i < 1000; i++) {
            for (int j = 1; j < 6; j++) {
                List<TestSubscriberEx<Integer>> tss = new ArrayList<>();
                for (int k = 1; k < j; k++) {
                    TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
                    tss.add(ts);
                    obs.subscribe(ts);
                }
                Disposable connection = cf.connect();
                for (TestSubscriberEx<Integer> ts : tss) {
                    ts.awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(1000).assertNoErrors().assertComplete();
                }
                connection.dispose();
            }
        }
    }

    @Test
    public void asyncFusedObserveOn() {
        ConnectableFlowable<Integer> cf = Flowable.range(0, 1000).observeOn(ImmediateThinScheduler.INSTANCE).publish();
        for (int i = 0; i < 1000; i++) {
            for (int j = 1; j < 6; j++) {
                List<TestSubscriberEx<Integer>> tss = new ArrayList<>();
                for (int k = 1; k < j; k++) {
                    TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
                    tss.add(ts);
                    cf.subscribe(ts);
                }
                Disposable connection = cf.connect();
                for (TestSubscriberEx<Integer> ts : tss) {
                    ts.awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(1000).assertNoErrors().assertComplete();
                }
                connection.dispose();
            }
        }
    }

    @Test
    public void observeOn() {
        ConnectableFlowable<Integer> cf = Flowable.range(0, 1000).hide().publish();
        Flowable<Integer> obs = cf.observeOn(Schedulers.computation());
        for (int i = 0; i < 1000; i++) {
            for (int j = 1; j < 6; j++) {
                List<TestSubscriberEx<Integer>> tss = new ArrayList<>();
                for (int k = 1; k < j; k++) {
                    TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
                    tss.add(ts);
                    obs.subscribe(ts);
                }
                Disposable connection = cf.connect();
                for (TestSubscriberEx<Integer> ts : tss) {
                    ts.awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(1000).assertNoErrors().assertComplete();
                }
                connection.dispose();
            }
        }
    }

    @Test
    public void source() {
        Flowable<Integer> f = Flowable.never();
        assertSame(f, (((HasUpstreamPublisher<?>) f.publish()).source()));
    }

    @Test
    public void connectThrows() {
        ConnectableFlowable<Integer> cf = Flowable.<Integer>empty().publish();
        try {
            cf.connect(new Consumer<Disposable>() {

                @Override
                public void accept(Disposable d) throws Exception {
                    throw new TestException();
                }
            });
        } catch (TestException ex) {
        // expected
        }
    }

    @Test
    public void addRemoveRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ConnectableFlowable<Integer> cf = Flowable.<Integer>empty().publish();
            final TestSubscriber<Integer> ts = cf.test();
            final TestSubscriber<Integer> ts2 = new TestSubscriber<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    cf.subscribe(ts2);
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
    public void disposeOnArrival() {
        ConnectableFlowable<Integer> cf = Flowable.<Integer>empty().publish();
        cf.test(Long.MAX_VALUE, true).assertEmpty();
    }

    @Test
    public void disposeOnArrival2() {
        Flowable<Integer> co = Flowable.<Integer>never().publish().autoConnect();
        co.test(Long.MAX_VALUE, true).assertEmpty();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.never().publish());
        TestHelper.checkDisposed(Flowable.never().publish(Functions.<Flowable<Object>>identity()));
    }

    @Test
    public void empty() {
        ConnectableFlowable<Integer> cf = Flowable.<Integer>empty().publish();
        cf.connect();
    }

    @Test
    public void take() {
        ConnectableFlowable<Integer> cf = Flowable.range(1, 2).publish();
        TestSubscriber<Integer> ts = cf.take(1).test();
        cf.connect();
        ts.assertResult(1);
    }

    @Test
    public void just() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        ConnectableFlowable<Integer> cf = pp.publish();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                pp.onComplete();
            }
        };
        cf.subscribe(ts);
        cf.connect();
        pp.onNext(1);
        ts.assertResult(1);
    }

    @Test
    public void nextCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final ConnectableFlowable<Integer> cf = pp.publish();
            final TestSubscriber<Integer> ts = cf.test();
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
        }
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onComplete();
                    subscriber.onNext(2);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                }
            }.publish().autoConnect().test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void noErrorLoss() {
        ConnectableFlowable<Object> cf = Flowable.error(new TestException()).publish();
        cf.connect();
        // 3.x: terminal events are always kept until reset.
        cf.test().assertFailure(TestException.class);
    }

    @Test
    public void subscribeDisconnectRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final ConnectableFlowable<Integer> cf = pp.publish();
            final Disposable d = cf.connect();
            final TestSubscriber<Integer> ts = new TestSubscriber<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    d.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    cf.subscribe(ts);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void selectorDisconnectsIndependentSource() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.publish(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> v) throws Exception {
                return Flowable.range(1, 2);
            }
        }).test().assertResult(1, 2);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void selectorLatecommer() {
        Flowable.range(1, 5).publish(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> v) throws Exception {
                return v.concatWith(v);
            }
        }).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void mainError() {
        Flowable.error(new TestException()).publish(Functions.<Flowable<Object>>identity()).test().assertFailure(TestException.class);
    }

    @Test
    public void selectorInnerError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.publish(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> v) throws Exception {
                return Flowable.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void preNextConnect() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ConnectableFlowable<Integer> cf = Flowable.<Integer>empty().publish();
            cf.connect();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    cf.test();
                }
            };
            TestHelper.race(r1, r1);
        }
    }

    @Test
    public void connectRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ConnectableFlowable<Integer> cf = Flowable.<Integer>empty().publish();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    cf.connect();
                }
            };
            TestHelper.race(r1, r1);
        }
    }

    @Test
    public void selectorCrash() {
        Flowable.just(1).publish(new Function<Flowable<Integer>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Integer> v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void pollThrows() {
        Flowable.just(1).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).compose(TestHelper.flowableStripBoundary()).publish().autoConnect().test().assertFailure(TestException.class);
    }

    @Test
    public void pollThrowsNoSubscribers() {
        ConnectableFlowable<Integer> cf = Flowable.just(1, 2).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                if (v == 2) {
                    throw new TestException();
                }
                return v;
            }
        }).compose(TestHelper.<Integer>flowableStripBoundary()).publish();
        TestSubscriber<Integer> ts = cf.take(1).test();
        cf.connect();
        ts.assertResult(1);
    }

    @Test
    public void dryRunCrash() {
        final TestSubscriber<Object> ts = new TestSubscriber<Object>(1L) {

            @Override
            public void onNext(Object t) {
                super.onNext(t);
                onComplete();
                cancel();
            }
        };
        Flowable<Object> source = Flowable.range(1, 10).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                if (v == 2) {
                    throw new TestException();
                }
                return v;
            }
        }).publish().autoConnect();
        source.subscribe(ts);
        ts.assertResult(1);
        // 3.x: terminal events remain observable until reset
        source.test().assertFailure(TestException.class);
    }

    @Test
    public void overflowQueue() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.create(new FlowableOnSubscribe<Object>() {

                @Override
                public void subscribe(FlowableEmitter<Object> s) throws Exception {
                    for (int i = 0; i < 10; i++) {
                        s.onNext(i);
                    }
                }
            }, BackpressureStrategy.MISSING).publish(8).autoConnect().test(0L).requestMore(10).assertFailure(MissingBackpressureException.class, 0, 1, 2, 3, 4, 5, 6, 7);
            TestHelper.assertError(errors, 0, MissingBackpressureException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void delayedUpstreamOnSubscribe() {
        final Subscriber<?>[] sub = { null };
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                sub[0] = s;
            }
        }.publish().connect().dispose();
        BooleanSubscription bs = new BooleanSubscription();
        sub[0].onSubscribe(bs);
        assertTrue(bs.isCancelled());
    }

    @Test
    public void disposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Disposable> ref = new AtomicReference<>();
            final ConnectableFlowable<Integer> cf = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    ref.set((Disposable) s);
                }
            }.publish();
            cf.connect();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ref.get().dispose();
                }
            };
            TestHelper.race(r1, r1);
        }
    }

    @Test
    public void removeNotPresent() {
        final AtomicReference<PublishConnection<Integer>> ref = new AtomicReference<>();
        final ConnectableFlowable<Integer> cf = new Flowable<Integer>() {

            @Override
            @SuppressWarnings("unchecked")
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                ref.set((PublishConnection<Integer>) s);
            }
        }.publish();
        cf.connect();
        ref.get().add(new InnerSubscription<>(new TestSubscriber<>(), ref.get()));
        ref.get().remove(null);
    }

    @Test
    public void subscriberSwap() {
        final ConnectableFlowable<Integer> cf = Flowable.range(1, 5).publish();
        cf.connect();
        TestSubscriber<Integer> ts1 = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                cancel();
                onComplete();
            }
        };
        cf.subscribe(ts1);
        ts1.assertResult(1);
        TestSubscriber<Integer> ts2 = new TestSubscriber<>(0);
        cf.subscribe(ts2);
        ts2.assertEmpty().requestMore(4).assertResult(2, 3, 4, 5);
    }

    @Test
    public void subscriberLiveSwap() {
        final ConnectableFlowable<Integer> cf = Flowable.range(1, 5).publish();
        final TestSubscriber<Integer> ts2 = new TestSubscriber<>(0);
        TestSubscriber<Integer> ts1 = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                cancel();
                onComplete();
                cf.subscribe(ts2);
            }
        };
        cf.subscribe(ts1);
        cf.connect();
        ts1.assertResult(1);
        ts2.assertEmpty().requestMore(4).assertResult(2, 3, 4, 5);
    }

    @Test
    public void selectorSubscriberSwap() {
        final AtomicReference<Flowable<Integer>> ref = new AtomicReference<>();
        Flowable.range(1, 5).publish(new Function<Flowable<Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Integer> f) throws Exception {
                ref.set(f);
                return Flowable.never();
            }
        }).test();
        ref.get().take(2).test().assertResult(1, 2);
        ref.get().test(0).assertEmpty().requestMore(2).assertValuesOnly(3, 4).requestMore(1).assertResult(3, 4, 5);
    }

    @Test
    public void leavingSubscriberOverrequests() {
        final AtomicReference<Flowable<Integer>> ref = new AtomicReference<>();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.publish(new Function<Flowable<Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Integer> f) throws Exception {
                ref.set(f);
                return Flowable.never();
            }
        }).test();
        TestSubscriber<Integer> ts1 = ref.get().take(2).test();
        pp.onNext(1);
        pp.onNext(2);
        ts1.assertResult(1, 2);
        pp.onNext(3);
        pp.onNext(4);
        TestSubscriber<Integer> ts2 = ref.get().test(0L);
        ts2.assertEmpty();
        ts2.requestMore(2);
        ts2.assertValuesOnly(3, 4);
    }

    // call a transformer only if the input is non-empty
    @Test
    public void composeIfNotEmpty() {
        final FlowableTransformer<Integer, Integer> transformer = new FlowableTransformer<Integer, Integer>() {

            @Override
            public Publisher<Integer> apply(Flowable<Integer> g) {
                return g.map(new Function<Integer, Integer>() {

                    @Override
                    public Integer apply(Integer v) throws Exception {
                        return v + 1;
                    }
                });
            }
        };
        final AtomicInteger calls = new AtomicInteger();
        Flowable.range(1, 5).publish(new Function<Flowable<Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(final Flowable<Integer> shared) throws Exception {
                return shared.take(1).concatMap(new Function<Integer, Publisher<? extends Integer>>() {

                    @Override
                    public Publisher<? extends Integer> apply(Integer first) throws Exception {
                        calls.incrementAndGet();
                        return transformer.apply(Flowable.just(first).concatWith(shared));
                    }
                });
            }
        }).test().assertResult(2, 3, 4, 5, 6);
        assertEquals(1, calls.get());
    }

    // call a transformer only if the input is non-empty
    @Test
    public void composeIfNotEmptyNotFused() {
        final FlowableTransformer<Integer, Integer> transformer = new FlowableTransformer<Integer, Integer>() {

            @Override
            public Publisher<Integer> apply(Flowable<Integer> g) {
                return g.map(new Function<Integer, Integer>() {

                    @Override
                    public Integer apply(Integer v) throws Exception {
                        return v + 1;
                    }
                });
            }
        };
        final AtomicInteger calls = new AtomicInteger();
        Flowable.range(1, 5).hide().publish(new Function<Flowable<Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(final Flowable<Integer> shared) throws Exception {
                return shared.take(1).concatMap(new Function<Integer, Publisher<? extends Integer>>() {

                    @Override
                    public Publisher<? extends Integer> apply(Integer first) throws Exception {
                        calls.incrementAndGet();
                        return transformer.apply(Flowable.just(first).concatWith(shared));
                    }
                });
            }
        }).test().assertResult(2, 3, 4, 5, 6);
        assertEquals(1, calls.get());
    }

    // call a transformer only if the input is non-empty
    @Test
    public void composeIfNotEmptyIsEmpty() {
        final FlowableTransformer<Integer, Integer> transformer = new FlowableTransformer<Integer, Integer>() {

            @Override
            public Publisher<Integer> apply(Flowable<Integer> g) {
                return g.map(new Function<Integer, Integer>() {

                    @Override
                    public Integer apply(Integer v) throws Exception {
                        return v + 1;
                    }
                });
            }
        };
        final AtomicInteger calls = new AtomicInteger();
        Flowable.<Integer>empty().hide().publish(new Function<Flowable<Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(final Flowable<Integer> shared) throws Exception {
                return shared.take(1).concatMap(new Function<Integer, Publisher<? extends Integer>>() {

                    @Override
                    public Publisher<? extends Integer> apply(Integer first) throws Exception {
                        calls.incrementAndGet();
                        return transformer.apply(Flowable.just(first).concatWith(shared));
                    }
                });
            }
        }).test().assertResult();
        assertEquals(0, calls.get());
    }

    @Test
    public void publishFunctionCancelOuterAfterOneInner() {
        final AtomicReference<Flowable<Integer>> ref = new AtomicReference<>();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final TestSubscriber<Integer> ts = pp.publish(new Function<Flowable<Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Integer> f) throws Exception {
                ref.set(f);
                return Flowable.never();
            }
        }).test();
        ref.get().subscribe(new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                onComplete();
                ts.cancel();
            }
        });
        pp.onNext(1);
    }

    @Test
    public void publishFunctionCancelOuterAfterOneInnerBackpressured() {
        final AtomicReference<Flowable<Integer>> ref = new AtomicReference<>();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final TestSubscriber<Integer> ts = pp.publish(new Function<Flowable<Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Integer> f) throws Exception {
                ref.set(f);
                return Flowable.never();
            }
        }).test();
        ref.get().subscribe(new TestSubscriber<Integer>(1L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                onComplete();
                ts.cancel();
            }
        });
        pp.onNext(1);
    }

    @Test
    public void publishCancelOneAsync() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final AtomicReference<Flowable<Integer>> ref = new AtomicReference<>();
            pp.publish(new Function<Flowable<Integer>, Publisher<Integer>>() {

                @Override
                public Publisher<Integer> apply(Flowable<Integer> f) throws Exception {
                    ref.set(f);
                    return Flowable.never();
                }
            }).test();
            final TestSubscriber<Integer> ts1 = ref.get().test();
            TestSubscriber<Integer> ts2 = ref.get().test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts1.cancel();
                }
            };
            TestHelper.race(r1, r2);
            ts2.assertValuesOnly(1);
        }
    }

    @Test
    public void publishCancelOneAsync2() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        ConnectableFlowable<Integer> cf = pp.publish();
        final TestSubscriber<Integer> ts1 = new TestSubscriber<>();
        final AtomicReference<InnerSubscription<Integer>> ref = new AtomicReference<>();
        cf.subscribe(new FlowableSubscriber<Integer>() {

            @SuppressWarnings("unchecked")
            @Override
            public void onSubscribe(Subscription s) {
                ts1.onSubscribe(new BooleanSubscription());
                // pretend to be cancelled without removing it from the subscriber list
                ref.set((InnerSubscription<Integer>) s);
            }

            @Override
            public void onNext(Integer t) {
                ts1.onNext(t);
            }

            @Override
            public void onError(Throwable t) {
                ts1.onError(t);
            }

            @Override
            public void onComplete() {
                ts1.onComplete();
            }
        });
        TestSubscriber<Integer> ts2 = cf.test();
        cf.connect();
        ref.get().set(Long.MIN_VALUE);
        pp.onNext(1);
        ts1.assertEmpty();
        ts2.assertValuesOnly(1);
    }

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
        }).share().observeOn(Schedulers.computation()).distinct().test().awaitDone(5, TimeUnit.SECONDS).assertResult("RxSingleScheduler");
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.range(1, 5).publish());
    }

    @Test
    public void splitCombineSubscriberChangeAfterOnNext() {
        Flowable<Integer> source = Flowable.range(0, 20).doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription v) throws Exception {
                // System.out.println("Subscribed");
            }
        }).publish(10).refCount();
        Flowable<Integer> evenNumbers = source.filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        });
        Flowable<Integer> oddNumbers = source.filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 != 0;
            }
        });
        final Single<Integer> getNextOdd = oddNumbers.first(0);
        TestSubscriber<List<Integer>> ts = evenNumbers.concatMap(new Function<Integer, Publisher<List<Integer>>>() {

            @Override
            public Publisher<List<Integer>> apply(Integer v) throws Exception {
                return Single.zip(Single.just(v), getNextOdd, new BiFunction<Integer, Integer, List<Integer>>() {

                    @Override
                    public List<Integer> apply(Integer a, Integer b) throws Exception {
                        return Arrays.asList(a, b);
                    }
                }).toFlowable();
            }
        }).takeWhile(new Predicate<List<Integer>>() {

            @Override
            public boolean test(List<Integer> v) throws Exception {
                return v.get(0) < 20;
            }
        }).test();
        ts.assertResult(Arrays.asList(0, 1), Arrays.asList(2, 3), Arrays.asList(4, 5), Arrays.asList(6, 7), Arrays.asList(8, 9), Arrays.asList(10, 11), Arrays.asList(12, 13), Arrays.asList(14, 15), Arrays.asList(16, 17), Arrays.asList(18, 19));
    }

    @Test
    public void splitCombineSubscriberChangeAfterOnNextFused() {
        Flowable<Integer> source = Flowable.range(0, 20).publish(10).refCount();
        Flowable<Integer> evenNumbers = source.filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        });
        Flowable<Integer> oddNumbers = source.filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 != 0;
            }
        });
        final Single<Integer> getNextOdd = oddNumbers.first(0);
        TestSubscriber<List<Integer>> ts = evenNumbers.concatMap(new Function<Integer, Publisher<List<Integer>>>() {

            @Override
            public Publisher<List<Integer>> apply(Integer v) throws Exception {
                return Single.zip(Single.just(v), getNextOdd, new BiFunction<Integer, Integer, List<Integer>>() {

                    @Override
                    public List<Integer> apply(Integer a, Integer b) throws Exception {
                        return Arrays.asList(a, b);
                    }
                }).toFlowable();
            }
        }).takeWhile(new Predicate<List<Integer>>() {

            @Override
            public boolean test(List<Integer> v) throws Exception {
                return v.get(0) < 20;
            }
        }).test();
        ts.assertResult(Arrays.asList(0, 1), Arrays.asList(2, 3), Arrays.asList(4, 5), Arrays.asList(6, 7), Arrays.asList(8, 9), Arrays.asList(10, 11), Arrays.asList(12, 13), Arrays.asList(14, 15), Arrays.asList(16, 17), Arrays.asList(18, 19));
    }

    @Test
    public void altConnectCrash() {
        try {
            new FlowablePublish<>(Flowable.<Integer>empty(), 128).connect(new Consumer<Disposable>() {

                @Override
                public void accept(Disposable t) throws Exception {
                    throw new TestException();
                }
            });
            fail("Should have thrown");
        } catch (TestException expected) {
        // expected
        }
    }

    @Test
    public void altConnectRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final ConnectableFlowable<Integer> cf = new FlowablePublish<>(Flowable.<Integer>never(), 128);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    cf.connect();
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void fusedPollCrash() {
        Flowable.range(1, 5).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).compose(TestHelper.flowableStripBoundary()).publish().refCount().test().assertFailure(TestException.class);
    }

    @Test
    public void syncFusedNoRequest() {
        Flowable.range(1, 5).publish(1).refCount().test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void normalBackpressuredPolls() {
        Flowable.range(1, 5).hide().publish(1).refCount().test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void emptyHidden() {
        Flowable.empty().hide().publish(1).refCount().test().assertResult();
    }

    @Test
    public void emptyFused() {
        Flowable.empty().publish(1).refCount().test().assertResult();
    }

    @Test
    public void overflowQueueRefCount() {
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onNext(1);
                s.onNext(2);
            }
        }.publish(1).refCount().test(0).requestMore(1).assertFailure(MissingBackpressureException.class, 1);
    }

    @Test
    public void doubleErrorRefCount() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onError(new TestException("one"));
                    s.onError(new TestException("two"));
                }
            }.publish(1).refCount().to(TestHelper.<Integer>testSubscriber(0L)).assertFailureAndMessage(TestException.class, "one");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "two");
            assertEquals(1, errors.size());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onCompleteAvailableUntilReset() {
        ConnectableFlowable<Integer> cf = Flowable.just(1).publish();
        TestSubscriber<Integer> ts = cf.test();
        ts.assertEmpty();
        cf.connect();
        ts.assertResult(1);
        cf.test().assertResult();
        cf.reset();
        ts = cf.test();
        ts.assertEmpty();
        cf.connect();
        ts.assertResult(1);
    }

    @Test
    public void onErrorAvailableUntilReset() {
        ConnectableFlowable<Integer> cf = Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).publish();
        TestSubscriber<Integer> ts = cf.test();
        ts.assertEmpty();
        cf.connect();
        ts.assertFailure(TestException.class, 1);
        cf.test().assertFailure(TestException.class);
        cf.reset();
        ts = cf.test();
        ts.assertEmpty();
        cf.connect();
        ts.assertFailure(TestException.class, 1);
    }

    @Test
    public void disposeResets() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        ConnectableFlowable<Integer> cf = pp.publish();
        assertFalse(pp.hasSubscribers());
        Disposable d = cf.connect();
        assertTrue(pp.hasSubscribers());
        d.dispose();
        assertFalse(pp.hasSubscribers());
        TestSubscriber<Integer> ts = cf.test();
        cf.connect();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertValuesOnly(1);
    }

    @Test(expected = TestException.class)
    public void connectDisposeCrash() {
        ConnectableFlowable<Object> cf = Flowable.never().publish();
        cf.connect();
        cf.connect(d -> {
            throw new TestException();
        });
    }

    @Test
    public void resetWhileNotConnectedIsNoOp() {
        ConnectableFlowable<Object> cf = Flowable.never().publish();
        cf.reset();
    }

    @Test
    public void resetWhileActiveIsNoOp() {
        ConnectableFlowable<Object> cf = Flowable.never().publish();
        cf.connect();
        cf.reset();
    }

    @Test
    public void crossCancelOnComplete() {
        TestSubscriber<Integer> ts1 = new TestSubscriber<>();
        TestSubscriber<Integer> ts2 = new TestSubscriber<Integer>() {

            @Override
            public void onComplete() {
                super.onComplete();
                ts1.cancel();
            }
        };
        PublishProcessor<Integer> pp = PublishProcessor.create();
        ConnectableFlowable<Integer> cf = pp.publish();
        cf.subscribe(ts2);
        cf.subscribe(ts1);
        cf.connect();
        pp.onComplete();
        ts2.assertResult();
        ts1.assertEmpty();
    }

    @Test
    public void crossCancelOnError() {
        TestSubscriber<Integer> ts1 = new TestSubscriber<>();
        TestSubscriber<Integer> ts2 = new TestSubscriber<Integer>() {

            @Override
            public void onError(Throwable t) {
                super.onError(t);
                ts1.cancel();
            }
        };
        PublishProcessor<Integer> pp = PublishProcessor.create();
        ConnectableFlowable<Integer> cf = pp.publish();
        cf.subscribe(ts2);
        cf.subscribe(ts1);
        cf.connect();
        pp.onError(new TestException());
        ts2.assertFailure(TestException.class);
        ts1.assertEmpty();
    }

    @Test
    public void disposeNoNeedForReset() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        ConnectableFlowable<Integer> cf = pp.publish();
        TestSubscriber<Integer> ts = cf.test();
        Disposable d = cf.connect();
        pp.onNext(1);
        d.dispose();
        ts = cf.test();
        ts.assertEmpty();
        cf.connect();
        ts.assertEmpty();
        pp.onNext(2);
        ts.assertValuesOnly(2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowablePublishTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publish() throws java.lang.Throwable {
            this.payloads.publish.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureFastSlow() throws java.lang.Throwable {
            this.payloads.backpressureFastSlow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilWithPublishedStreamUsingSelector() throws java.lang.Throwable {
            this.payloads.takeUntilWithPublishedStreamUsingSelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilWithPublishedStream() throws java.lang.Throwable {
            this.payloads.takeUntilWithPublishedStream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureTwoConsumers() throws java.lang.Throwable {
            this.payloads.backpressureTwoConsumers.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_connectWithNoSubscriber() throws java.lang.Throwable {
            this.payloads.connectWithNoSubscriber.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeAfterDisconnectThenConnect() throws java.lang.Throwable {
            this.payloads.subscribeAfterDisconnectThenConnect.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubscriberRetentionOnCompleted() throws java.lang.Throwable {
            this.payloads.noSubscriberRetentionOnCompleted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonNullConnection() throws java.lang.Throwable {
            this.payloads.nonNullConnection.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noDisconnectSomeoneElse() throws java.lang.Throwable {
            this.payloads.noDisconnectSomeoneElse.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zeroRequested() throws java.lang.Throwable {
            this.payloads.zeroRequested.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_connectIsIdempotent() throws java.lang.Throwable {
            this.payloads.connectIsIdempotent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedObserveOn() throws java.lang.Throwable {
            this.payloads.syncFusedObserveOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedObserveOn2() throws java.lang.Throwable {
            this.payloads.syncFusedObserveOn2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedObserveOn() throws java.lang.Throwable {
            this.payloads.asyncFusedObserveOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOn() throws java.lang.Throwable {
            this.payloads.observeOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_source() throws java.lang.Throwable {
            this.payloads.source.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_connectThrows() throws java.lang.Throwable {
            this.payloads.connectThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addRemoveRace() throws java.lang.Throwable {
            this.payloads.addRemoveRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeOnArrival() throws java.lang.Throwable {
            this.payloads.disposeOnArrival.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeOnArrival2() throws java.lang.Throwable {
            this.payloads.disposeOnArrival2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.payloads.just.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextCancelRace() throws java.lang.Throwable {
            this.payloads.nextCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noErrorLoss() throws java.lang.Throwable {
            this.payloads.noErrorLoss.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeDisconnectRace() throws java.lang.Throwable {
            this.payloads.subscribeDisconnectRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorDisconnectsIndependentSource() throws java.lang.Throwable {
            this.payloads.selectorDisconnectsIndependentSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorLatecommer() throws java.lang.Throwable {
            this.payloads.selectorLatecommer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.payloads.mainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorInnerError() throws java.lang.Throwable {
            this.payloads.selectorInnerError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_preNextConnect() throws java.lang.Throwable {
            this.payloads.preNextConnect.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_connectRace() throws java.lang.Throwable {
            this.payloads.connectRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorCrash() throws java.lang.Throwable {
            this.payloads.selectorCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_pollThrows() throws java.lang.Throwable {
            this.payloads.pollThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_pollThrowsNoSubscribers() throws java.lang.Throwable {
            this.payloads.pollThrowsNoSubscribers.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dryRunCrash() throws java.lang.Throwable {
            this.payloads.dryRunCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overflowQueue() throws java.lang.Throwable {
            this.payloads.overflowQueue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayedUpstreamOnSubscribe() throws java.lang.Throwable {
            this.payloads.delayedUpstreamOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRace() throws java.lang.Throwable {
            this.payloads.disposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_removeNotPresent() throws java.lang.Throwable {
            this.payloads.removeNotPresent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriberSwap() throws java.lang.Throwable {
            this.payloads.subscriberSwap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriberLiveSwap() throws java.lang.Throwable {
            this.payloads.subscriberLiveSwap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorSubscriberSwap() throws java.lang.Throwable {
            this.payloads.selectorSubscriberSwap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_leavingSubscriberOverrequests() throws java.lang.Throwable {
            this.payloads.leavingSubscriberOverrequests.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_composeIfNotEmpty() throws java.lang.Throwable {
            this.payloads.composeIfNotEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_composeIfNotEmptyNotFused() throws java.lang.Throwable {
            this.payloads.composeIfNotEmptyNotFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_composeIfNotEmptyIsEmpty() throws java.lang.Throwable {
            this.payloads.composeIfNotEmptyIsEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishFunctionCancelOuterAfterOneInner() throws java.lang.Throwable {
            this.payloads.publishFunctionCancelOuterAfterOneInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishFunctionCancelOuterAfterOneInnerBackpressured() throws java.lang.Throwable {
            this.payloads.publishFunctionCancelOuterAfterOneInnerBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishCancelOneAsync() throws java.lang.Throwable {
            this.payloads.publishCancelOneAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishCancelOneAsync2() throws java.lang.Throwable {
            this.payloads.publishCancelOneAsync2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusion() throws java.lang.Throwable {
            this.payloads.boundaryFusion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_splitCombineSubscriberChangeAfterOnNext() throws java.lang.Throwable {
            this.payloads.splitCombineSubscriberChangeAfterOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_splitCombineSubscriberChangeAfterOnNextFused() throws java.lang.Throwable {
            this.payloads.splitCombineSubscriberChangeAfterOnNextFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_altConnectCrash() throws java.lang.Throwable {
            this.payloads.altConnectCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_altConnectRace() throws java.lang.Throwable {
            this.payloads.altConnectRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPollCrash() throws java.lang.Throwable {
            this.payloads.fusedPollCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedNoRequest() throws java.lang.Throwable {
            this.payloads.syncFusedNoRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalBackpressuredPolls() throws java.lang.Throwable {
            this.payloads.normalBackpressuredPolls.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyHidden() throws java.lang.Throwable {
            this.payloads.emptyHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyFused() throws java.lang.Throwable {
            this.payloads.emptyFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overflowQueueRefCount() throws java.lang.Throwable {
            this.payloads.overflowQueueRefCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleErrorRefCount() throws java.lang.Throwable {
            this.payloads.doubleErrorRefCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteAvailableUntilReset() throws java.lang.Throwable {
            this.payloads.onCompleteAvailableUntilReset.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorAvailableUntilReset() throws java.lang.Throwable {
            this.payloads.onErrorAvailableUntilReset.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeResets() throws java.lang.Throwable {
            this.payloads.disposeResets.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_connectDisposeCrash() throws java.lang.Throwable {
            this.payloads.connectDisposeCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resetWhileNotConnectedIsNoOp() throws java.lang.Throwable {
            this.payloads.resetWhileNotConnectedIsNoOp.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resetWhileActiveIsNoOp() throws java.lang.Throwable {
            this.payloads.resetWhileActiveIsNoOp.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crossCancelOnComplete() throws java.lang.Throwable {
            this.payloads.crossCancelOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crossCancelOnError() throws java.lang.Throwable {
            this.payloads.crossCancelOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeNoNeedForReset() throws java.lang.Throwable {
            this.payloads.disposeNoNeedForReset.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowablePublishTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowablePublishTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowablePublishTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowablePublishTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowablePublishTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowablePublishTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowablePublishTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowablePublishTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement publish;

            public org.junit.runners.model.Statement backpressureFastSlow;

            public org.junit.runners.model.Statement takeUntilWithPublishedStreamUsingSelector;

            public org.junit.runners.model.Statement takeUntilWithPublishedStream;

            public org.junit.runners.model.Statement backpressureTwoConsumers;

            public org.junit.runners.model.Statement connectWithNoSubscriber;

            public org.junit.runners.model.Statement subscribeAfterDisconnectThenConnect;

            public org.junit.runners.model.Statement noSubscriberRetentionOnCompleted;

            public org.junit.runners.model.Statement nonNullConnection;

            public org.junit.runners.model.Statement noDisconnectSomeoneElse;

            public org.junit.runners.model.Statement zeroRequested;

            public org.junit.runners.model.Statement connectIsIdempotent;

            public org.junit.runners.model.Statement syncFusedObserveOn;

            public org.junit.runners.model.Statement syncFusedObserveOn2;

            public org.junit.runners.model.Statement asyncFusedObserveOn;

            public org.junit.runners.model.Statement observeOn;

            public org.junit.runners.model.Statement source;

            public org.junit.runners.model.Statement connectThrows;

            public org.junit.runners.model.Statement addRemoveRace;

            public org.junit.runners.model.Statement disposeOnArrival;

            public org.junit.runners.model.Statement disposeOnArrival2;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement just;

            public org.junit.runners.model.Statement nextCancelRace;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement noErrorLoss;

            public org.junit.runners.model.Statement subscribeDisconnectRace;

            public org.junit.runners.model.Statement selectorDisconnectsIndependentSource;

            public org.junit.runners.model.Statement selectorLatecommer;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement selectorInnerError;

            public org.junit.runners.model.Statement preNextConnect;

            public org.junit.runners.model.Statement connectRace;

            public org.junit.runners.model.Statement selectorCrash;

            public org.junit.runners.model.Statement pollThrows;

            public org.junit.runners.model.Statement pollThrowsNoSubscribers;

            public org.junit.runners.model.Statement dryRunCrash;

            public org.junit.runners.model.Statement overflowQueue;

            public org.junit.runners.model.Statement delayedUpstreamOnSubscribe;

            public org.junit.runners.model.Statement disposeRace;

            public org.junit.runners.model.Statement removeNotPresent;

            public org.junit.runners.model.Statement subscriberSwap;

            public org.junit.runners.model.Statement subscriberLiveSwap;

            public org.junit.runners.model.Statement selectorSubscriberSwap;

            public org.junit.runners.model.Statement leavingSubscriberOverrequests;

            public org.junit.runners.model.Statement composeIfNotEmpty;

            public org.junit.runners.model.Statement composeIfNotEmptyNotFused;

            public org.junit.runners.model.Statement composeIfNotEmptyIsEmpty;

            public org.junit.runners.model.Statement publishFunctionCancelOuterAfterOneInner;

            public org.junit.runners.model.Statement publishFunctionCancelOuterAfterOneInnerBackpressured;

            public org.junit.runners.model.Statement publishCancelOneAsync;

            public org.junit.runners.model.Statement publishCancelOneAsync2;

            public org.junit.runners.model.Statement boundaryFusion;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement splitCombineSubscriberChangeAfterOnNext;

            public org.junit.runners.model.Statement splitCombineSubscriberChangeAfterOnNextFused;

            public org.junit.runners.model.Statement altConnectCrash;

            public org.junit.runners.model.Statement altConnectRace;

            public org.junit.runners.model.Statement fusedPollCrash;

            public org.junit.runners.model.Statement syncFusedNoRequest;

            public org.junit.runners.model.Statement normalBackpressuredPolls;

            public org.junit.runners.model.Statement emptyHidden;

            public org.junit.runners.model.Statement emptyFused;

            public org.junit.runners.model.Statement overflowQueueRefCount;

            public org.junit.runners.model.Statement doubleErrorRefCount;

            public org.junit.runners.model.Statement onCompleteAvailableUntilReset;

            public org.junit.runners.model.Statement onErrorAvailableUntilReset;

            public org.junit.runners.model.Statement disposeResets;

            public org.junit.runners.model.Statement connectDisposeCrash;

            public org.junit.runners.model.Statement resetWhileNotConnectedIsNoOp;

            public org.junit.runners.model.Statement resetWhileActiveIsNoOp;

            public org.junit.runners.model.Statement crossCancelOnComplete;

            public org.junit.runners.model.Statement crossCancelOnError;

            public org.junit.runners.model.Statement disposeNoNeedForReset;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.publish = _ClassStatement.forPayload(FlowablePublishTest::publish, "publish", this);
            this.payloads.backpressureFastSlow = _ClassStatement.forPayload(FlowablePublishTest::backpressureFastSlow, "backpressureFastSlow", this);
            this.payloads.takeUntilWithPublishedStreamUsingSelector = _ClassStatement.forPayload(FlowablePublishTest::takeUntilWithPublishedStreamUsingSelector, "takeUntilWithPublishedStreamUsingSelector", this);
            this.payloads.takeUntilWithPublishedStream = _ClassStatement.forPayload(FlowablePublishTest::takeUntilWithPublishedStream, "takeUntilWithPublishedStream", this);
            this.payloads.backpressureTwoConsumers = _ClassStatement.forPayload(FlowablePublishTest::backpressureTwoConsumers, "backpressureTwoConsumers", this);
            this.payloads.connectWithNoSubscriber = _ClassStatement.forPayload(FlowablePublishTest::connectWithNoSubscriber, "connectWithNoSubscriber", this);
            this.payloads.subscribeAfterDisconnectThenConnect = _ClassStatement.forPayload(FlowablePublishTest::subscribeAfterDisconnectThenConnect, "subscribeAfterDisconnectThenConnect", this);
            this.payloads.noSubscriberRetentionOnCompleted = _ClassStatement.forPayload(FlowablePublishTest::noSubscriberRetentionOnCompleted, "noSubscriberRetentionOnCompleted", this);
            this.payloads.nonNullConnection = _ClassStatement.forPayload(FlowablePublishTest::nonNullConnection, "nonNullConnection", this);
            this.payloads.noDisconnectSomeoneElse = _ClassStatement.forPayload(FlowablePublishTest::noDisconnectSomeoneElse, "noDisconnectSomeoneElse", this);
            this.payloads.zeroRequested = _ClassStatement.forPayload(FlowablePublishTest::zeroRequested, "zeroRequested", this);
            this.payloads.connectIsIdempotent = _ClassStatement.forPayload(FlowablePublishTest::connectIsIdempotent, "connectIsIdempotent", this);
            this.payloads.syncFusedObserveOn = _ClassStatement.forPayload(FlowablePublishTest::syncFusedObserveOn, "syncFusedObserveOn", this);
            this.payloads.syncFusedObserveOn2 = _ClassStatement.forPayload(FlowablePublishTest::syncFusedObserveOn2, "syncFusedObserveOn2", this);
            this.payloads.asyncFusedObserveOn = _ClassStatement.forPayload(FlowablePublishTest::asyncFusedObserveOn, "asyncFusedObserveOn", this);
            this.payloads.observeOn = _ClassStatement.forPayload(FlowablePublishTest::observeOn, "observeOn", this);
            this.payloads.source = _ClassStatement.forPayload(FlowablePublishTest::source, "source", this);
            this.payloads.connectThrows = _ClassStatement.forPayload(FlowablePublishTest::connectThrows, "connectThrows", this);
            this.payloads.addRemoveRace = _ClassStatement.forPayload(FlowablePublishTest::addRemoveRace, "addRemoveRace", this);
            this.payloads.disposeOnArrival = _ClassStatement.forPayload(FlowablePublishTest::disposeOnArrival, "disposeOnArrival", this);
            this.payloads.disposeOnArrival2 = _ClassStatement.forPayload(FlowablePublishTest::disposeOnArrival2, "disposeOnArrival2", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowablePublishTest::dispose, "dispose", this);
            this.payloads.empty = _ClassStatement.forPayload(FlowablePublishTest::empty, "empty", this);
            this.payloads.take = _ClassStatement.forPayload(FlowablePublishTest::take, "take", this);
            this.payloads.just = _ClassStatement.forPayload(FlowablePublishTest::just, "just", this);
            this.payloads.nextCancelRace = _ClassStatement.forPayload(FlowablePublishTest::nextCancelRace, "nextCancelRace", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowablePublishTest::badSource, "badSource", this);
            this.payloads.noErrorLoss = _ClassStatement.forPayload(FlowablePublishTest::noErrorLoss, "noErrorLoss", this);
            this.payloads.subscribeDisconnectRace = _ClassStatement.forPayload(FlowablePublishTest::subscribeDisconnectRace, "subscribeDisconnectRace", this);
            this.payloads.selectorDisconnectsIndependentSource = _ClassStatement.forPayload(FlowablePublishTest::selectorDisconnectsIndependentSource, "selectorDisconnectsIndependentSource", this);
            this.payloads.selectorLatecommer = _ClassStatement.forPayload(FlowablePublishTest::selectorLatecommer, "selectorLatecommer", this);
            this.payloads.mainError = _ClassStatement.forPayload(FlowablePublishTest::mainError, "mainError", this);
            this.payloads.selectorInnerError = _ClassStatement.forPayload(FlowablePublishTest::selectorInnerError, "selectorInnerError", this);
            this.payloads.preNextConnect = _ClassStatement.forPayload(FlowablePublishTest::preNextConnect, "preNextConnect", this);
            this.payloads.connectRace = _ClassStatement.forPayload(FlowablePublishTest::connectRace, "connectRace", this);
            this.payloads.selectorCrash = _ClassStatement.forPayload(FlowablePublishTest::selectorCrash, "selectorCrash", this);
            this.payloads.pollThrows = _ClassStatement.forPayload(FlowablePublishTest::pollThrows, "pollThrows", this);
            this.payloads.pollThrowsNoSubscribers = _ClassStatement.forPayload(FlowablePublishTest::pollThrowsNoSubscribers, "pollThrowsNoSubscribers", this);
            this.payloads.dryRunCrash = _ClassStatement.forPayload(FlowablePublishTest::dryRunCrash, "dryRunCrash", this);
            this.payloads.overflowQueue = _ClassStatement.forPayload(FlowablePublishTest::overflowQueue, "overflowQueue", this);
            this.payloads.delayedUpstreamOnSubscribe = _ClassStatement.forPayload(FlowablePublishTest::delayedUpstreamOnSubscribe, "delayedUpstreamOnSubscribe", this);
            this.payloads.disposeRace = _ClassStatement.forPayload(FlowablePublishTest::disposeRace, "disposeRace", this);
            this.payloads.removeNotPresent = _ClassStatement.forPayload(FlowablePublishTest::removeNotPresent, "removeNotPresent", this);
            this.payloads.subscriberSwap = _ClassStatement.forPayload(FlowablePublishTest::subscriberSwap, "subscriberSwap", this);
            this.payloads.subscriberLiveSwap = _ClassStatement.forPayload(FlowablePublishTest::subscriberLiveSwap, "subscriberLiveSwap", this);
            this.payloads.selectorSubscriberSwap = _ClassStatement.forPayload(FlowablePublishTest::selectorSubscriberSwap, "selectorSubscriberSwap", this);
            this.payloads.leavingSubscriberOverrequests = _ClassStatement.forPayload(FlowablePublishTest::leavingSubscriberOverrequests, "leavingSubscriberOverrequests", this);
            this.payloads.composeIfNotEmpty = _ClassStatement.forPayload(FlowablePublishTest::composeIfNotEmpty, "composeIfNotEmpty", this);
            this.payloads.composeIfNotEmptyNotFused = _ClassStatement.forPayload(FlowablePublishTest::composeIfNotEmptyNotFused, "composeIfNotEmptyNotFused", this);
            this.payloads.composeIfNotEmptyIsEmpty = _ClassStatement.forPayload(FlowablePublishTest::composeIfNotEmptyIsEmpty, "composeIfNotEmptyIsEmpty", this);
            this.payloads.publishFunctionCancelOuterAfterOneInner = _ClassStatement.forPayload(FlowablePublishTest::publishFunctionCancelOuterAfterOneInner, "publishFunctionCancelOuterAfterOneInner", this);
            this.payloads.publishFunctionCancelOuterAfterOneInnerBackpressured = _ClassStatement.forPayload(FlowablePublishTest::publishFunctionCancelOuterAfterOneInnerBackpressured, "publishFunctionCancelOuterAfterOneInnerBackpressured", this);
            this.payloads.publishCancelOneAsync = _ClassStatement.forPayload(FlowablePublishTest::publishCancelOneAsync, "publishCancelOneAsync", this);
            this.payloads.publishCancelOneAsync2 = _ClassStatement.forPayload(FlowablePublishTest::publishCancelOneAsync2, "publishCancelOneAsync2", this);
            this.payloads.boundaryFusion = _ClassStatement.forPayload(FlowablePublishTest::boundaryFusion, "boundaryFusion", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowablePublishTest::badRequest, "badRequest", this);
            this.payloads.splitCombineSubscriberChangeAfterOnNext = _ClassStatement.forPayload(FlowablePublishTest::splitCombineSubscriberChangeAfterOnNext, "splitCombineSubscriberChangeAfterOnNext", this);
            this.payloads.splitCombineSubscriberChangeAfterOnNextFused = _ClassStatement.forPayload(FlowablePublishTest::splitCombineSubscriberChangeAfterOnNextFused, "splitCombineSubscriberChangeAfterOnNextFused", this);
            this.payloads.altConnectCrash = _ClassStatement.forPayload(FlowablePublishTest::altConnectCrash, "altConnectCrash", this);
            this.payloads.altConnectRace = _ClassStatement.forPayload(FlowablePublishTest::altConnectRace, "altConnectRace", this);
            this.payloads.fusedPollCrash = _ClassStatement.forPayload(FlowablePublishTest::fusedPollCrash, "fusedPollCrash", this);
            this.payloads.syncFusedNoRequest = _ClassStatement.forPayload(FlowablePublishTest::syncFusedNoRequest, "syncFusedNoRequest", this);
            this.payloads.normalBackpressuredPolls = _ClassStatement.forPayload(FlowablePublishTest::normalBackpressuredPolls, "normalBackpressuredPolls", this);
            this.payloads.emptyHidden = _ClassStatement.forPayload(FlowablePublishTest::emptyHidden, "emptyHidden", this);
            this.payloads.emptyFused = _ClassStatement.forPayload(FlowablePublishTest::emptyFused, "emptyFused", this);
            this.payloads.overflowQueueRefCount = _ClassStatement.forPayload(FlowablePublishTest::overflowQueueRefCount, "overflowQueueRefCount", this);
            this.payloads.doubleErrorRefCount = _ClassStatement.forPayload(FlowablePublishTest::doubleErrorRefCount, "doubleErrorRefCount", this);
            this.payloads.onCompleteAvailableUntilReset = _ClassStatement.forPayload(FlowablePublishTest::onCompleteAvailableUntilReset, "onCompleteAvailableUntilReset", this);
            this.payloads.onErrorAvailableUntilReset = _ClassStatement.forPayload(FlowablePublishTest::onErrorAvailableUntilReset, "onErrorAvailableUntilReset", this);
            this.payloads.disposeResets = _ClassStatement.forPayload(FlowablePublishTest::disposeResets, "disposeResets", this);
            this.payloads.connectDisposeCrash = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowablePublishTest::connectDisposeCrash, io.reactivex.rxjava3.exceptions.TestException.class), "connectDisposeCrash", this);
            this.payloads.resetWhileNotConnectedIsNoOp = _ClassStatement.forPayload(FlowablePublishTest::resetWhileNotConnectedIsNoOp, "resetWhileNotConnectedIsNoOp", this);
            this.payloads.resetWhileActiveIsNoOp = _ClassStatement.forPayload(FlowablePublishTest::resetWhileActiveIsNoOp, "resetWhileActiveIsNoOp", this);
            this.payloads.crossCancelOnComplete = _ClassStatement.forPayload(FlowablePublishTest::crossCancelOnComplete, "crossCancelOnComplete", this);
            this.payloads.crossCancelOnError = _ClassStatement.forPayload(FlowablePublishTest::crossCancelOnError, "crossCancelOnError", this);
            this.payloads.disposeNoNeedForReset = _ClassStatement.forPayload(FlowablePublishTest::disposeNoNeedForReset, "disposeNoNeedForReset", this);
        }
    }
}
