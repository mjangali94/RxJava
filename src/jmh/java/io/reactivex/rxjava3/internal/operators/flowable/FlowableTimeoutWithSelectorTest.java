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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableTimeout.TimeoutConsumer;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableTimeoutWithSelectorTest extends RxJavaTest {

    @Test
    public void timeoutSelectorNormal1() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> timeout = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> timeoutFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return timeout;
            }
        };
        Flowable<Integer> other = Flowable.fromIterable(Arrays.asList(100));
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.timeout(timeout, timeoutFunc, other).subscribe(subscriber);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        timeout.onNext(1);
        inOrder.verify(subscriber).onNext(1);
        inOrder.verify(subscriber).onNext(2);
        inOrder.verify(subscriber).onNext(3);
        inOrder.verify(subscriber).onNext(100);
        inOrder.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void timeoutSelectorTimeoutFirst() throws InterruptedException {
        Flowable<Integer> source = Flowable.<Integer>never();
        final PublishProcessor<Integer> timeout = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> timeoutFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return timeout;
            }
        };
        Flowable<Integer> other = Flowable.fromIterable(Arrays.asList(100));
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.timeout(timeout, timeoutFunc, other).subscribe(subscriber);
        timeout.onNext(1);
        inOrder.verify(subscriber).onNext(100);
        inOrder.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void timeoutSelectorFirstThrows() {
        Flowable<Integer> source = Flowable.<Integer>never();
        final PublishProcessor<Integer> timeout = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> timeoutFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return timeout;
            }
        };
        Supplier<Flowable<Integer>> firstTimeoutFunc = new Supplier<Flowable<Integer>>() {

            @Override
            public Flowable<Integer> get() {
                throw new TestException();
            }
        };
        Flowable<Integer> other = Flowable.fromIterable(Arrays.asList(100));
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.timeout(Flowable.defer(firstTimeoutFunc), timeoutFunc, other).subscribe(subscriber);
        verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void timeoutSelectorSubsequentThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> timeout = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> timeoutFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                throw new TestException();
            }
        };
        Flowable<Integer> other = Flowable.fromIterable(Arrays.asList(100));
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.timeout(timeout, timeoutFunc, other).subscribe(subscriber);
        source.onNext(1);
        inOrder.verify(subscriber).onNext(1);
        inOrder.verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void timeoutSelectorFirstFlowableThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> timeout = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> timeoutFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return timeout;
            }
        };
        Flowable<Integer> other = Flowable.fromIterable(Arrays.asList(100));
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.timeout(Flowable.<Integer>error(new TestException()), timeoutFunc, other).subscribe(subscriber);
        verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void timeoutSelectorSubsequentFlowableThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> timeout = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> timeoutFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return Flowable.<Integer>error(new TestException());
            }
        };
        Flowable<Integer> other = Flowable.fromIterable(Arrays.asList(100));
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.timeout(timeout, timeoutFunc, other).subscribe(subscriber);
        source.onNext(1);
        inOrder.verify(subscriber).onNext(1);
        inOrder.verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void timeoutSelectorWithFirstTimeoutFirstAndNoOtherFlowable() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> timeout = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> timeoutFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return PublishProcessor.create();
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.timeout(timeout, timeoutFunc).subscribe(subscriber);
        timeout.onNext(1);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onError(isA(TimeoutException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void timeoutSelectorWithTimeoutFirstAndNoOtherFlowable() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> timeout = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> timeoutFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return timeout;
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.timeout(PublishProcessor.create(), timeoutFunc).subscribe(subscriber);
        source.onNext(1);
        timeout.onNext(1);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onNext(1);
        inOrder.verify(subscriber).onError(isA(TimeoutException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void timeoutSelectorWithTimeoutAndOnNextRaceCondition() throws InterruptedException {
        // Thread 1                                    Thread 2
        // 
        // observer.onNext(1)
        // start timeout
        // unsubscribe timeout in thread 2          start to do some long-time work in "unsubscribe"
        // observer.onNext(2)
        // timeout.onNext(1)
        // "unsubscribe" done
        // 
        // 
        // In the above case, the timeout operator should ignore "timeout.onNext(1)"
        // since "observer" has already seen 2.
        final CountDownLatch observerReceivedTwo = new CountDownLatch(1);
        final CountDownLatch timeoutEmittedOne = new CountDownLatch(1);
        final CountDownLatch observerCompleted = new CountDownLatch(1);
        final CountDownLatch enteredTimeoutOne = new CountDownLatch(1);
        final AtomicBoolean latchTimeout = new AtomicBoolean(false);
        final Function<Integer, Flowable<Integer>> timeoutFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                if (t1 == 1) {
                    // Force "unsubscribe" run on another thread
                    return Flowable.unsafeCreate(new Publisher<Integer>() {

                        @Override
                        public void subscribe(Subscriber<? super Integer> subscriber) {
                            subscriber.onSubscribe(new BooleanSubscription());
                            enteredTimeoutOne.countDown();
                            // force the timeout message be sent after observer.onNext(2)
                            while (true) {
                                try {
                                    if (!observerReceivedTwo.await(30, TimeUnit.SECONDS)) {
                                        // CountDownLatch timeout
                                        // There should be something wrong
                                        latchTimeout.set(true);
                                    }
                                    break;
                                } catch (InterruptedException e) {
                                // Since we just want to emulate a busy method,
                                // we ignore the interrupt signal from Scheduler.
                                }
                            }
                            subscriber.onNext(1);
                            timeoutEmittedOne.countDown();
                        }
                    }).subscribeOn(Schedulers.newThread());
                } else {
                    return PublishProcessor.create();
                }
            }
        };
        final Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                observerReceivedTwo.countDown();
                return null;
            }
        }).when(subscriber).onNext(2);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                observerCompleted.countDown();
                return null;
            }
        }).when(subscriber).onComplete();
        final TestSubscriber<Integer> ts = new TestSubscriber<>(subscriber);
        new Thread(new Runnable() {

            @Override
            public void run() {
                PublishProcessor<Integer> source = PublishProcessor.create();
                source.timeout(timeoutFunc, Flowable.just(3)).subscribe(ts);
                // start timeout
                source.onNext(1);
                try {
                    if (!enteredTimeoutOne.await(30, TimeUnit.SECONDS)) {
                        latchTimeout.set(true);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // disable timeout
                source.onNext(2);
                try {
                    if (!timeoutEmittedOne.await(30, TimeUnit.SECONDS)) {
                        latchTimeout.set(true);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                source.onComplete();
            }
        }).start();
        if (!observerCompleted.await(30, TimeUnit.SECONDS)) {
            latchTimeout.set(true);
        }
        assertFalse("CoundDownLatch timeout", latchTimeout.get());
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onSubscribe((Subscription) notNull());
        inOrder.verify(subscriber).onNext(1);
        inOrder.verify(subscriber).onNext(2);
        inOrder.verify(subscriber, never()).onNext(3);
        inOrder.verify(subscriber).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().timeout(Functions.justFunction(Flowable.never())));
        TestHelper.checkDisposed(PublishProcessor.create().timeout(Functions.justFunction(Flowable.never()), Flowable.never()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.timeout(Functions.justFunction(Flowable.never()));
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.timeout(Functions.justFunction(Flowable.never()), Flowable.never());
            }
        });
    }

    @Test
    public void empty() {
        Flowable.empty().timeout(Functions.justFunction(Flowable.never())).test().assertResult();
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).timeout(Functions.justFunction(Flowable.never())).test().assertFailure(TestException.class);
    }

    @Test
    public void emptyInner() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.timeout(Functions.justFunction(Flowable.empty())).test();
        pp.onNext(1);
        ts.assertFailure(TimeoutException.class, 1);
    }

    @Test
    public void badInnerSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriberEx<Integer> ts = pp.timeout(Functions.justFunction(new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onError(new TestException("First"));
                    subscriber.onNext(2);
                    subscriber.onError(new TestException("Second"));
                    subscriber.onComplete();
                }
            })).to(TestHelper.<Integer>testConsumer());
            pp.onNext(1);
            ts.assertFailureAndMessage(TestException.class, "First", 1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badInnerSourceOther() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriberEx<Integer> ts = pp.timeout(Functions.justFunction(new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onError(new TestException("First"));
                    subscriber.onNext(2);
                    subscriber.onError(new TestException("Second"));
                    subscriber.onComplete();
                }
            }), Flowable.just(2)).to(TestHelper.<Integer>testConsumer());
            pp.onNext(1);
            ts.assertFailureAndMessage(TestException.class, "First", 1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void withOtherMainError() {
        Flowable.error(new TestException()).timeout(Functions.justFunction(Flowable.never()), Flowable.never()).test().assertFailure(TestException.class);
    }

    @Test
    public void badSourceTimeout() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onNext(2);
                    subscriber.onError(new TestException("First"));
                    subscriber.onNext(3);
                    subscriber.onComplete();
                    subscriber.onError(new TestException("Second"));
                }
            }.timeout(Functions.justFunction(Flowable.never()), Flowable.<Integer>never()).take(1).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 1, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void selectorTake() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.timeout(Functions.justFunction(Flowable.never())).take(1).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        assertFalse(pp.hasSubscribers());
        ts.assertResult(1);
    }

    @Test
    public void selectorFallbackTake() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.timeout(Functions.justFunction(Flowable.never()), Flowable.just(2)).take(1).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        assertFalse(pp.hasSubscribers());
        ts.assertResult(1);
    }

    @Test
    public void lateOnTimeoutError() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp = PublishProcessor.create();
                final Subscriber<?>[] sub = { null, null };
                final Flowable<Integer> pp2 = new Flowable<Integer>() {

                    int count;

                    @Override
                    protected void subscribeActual(Subscriber<? super Integer> s) {
                        s.onSubscribe(new BooleanSubscription());
                        sub[count++] = s;
                    }
                };
                TestSubscriber<Integer> ts = pp.timeout(Functions.justFunction(pp2)).test();
                pp.onNext(0);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp.onNext(1);
                    }
                };
                final Throwable ex = new TestException();
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        sub[0].onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                ts.assertValueAt(0, 0);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void lateOnTimeoutFallbackRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp = PublishProcessor.create();
                final Subscriber<?>[] sub = { null, null };
                final Flowable<Integer> pp2 = new Flowable<Integer>() {

                    int count;

                    @Override
                    protected void subscribeActual(Subscriber<? super Integer> s) {
                        assertFalse(((Disposable) s).isDisposed());
                        s.onSubscribe(new BooleanSubscription());
                        sub[count++] = s;
                    }
                };
                TestSubscriber<Integer> ts = pp.timeout(Functions.justFunction(pp2), Flowable.<Integer>never()).test();
                pp.onNext(0);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp.onNext(1);
                    }
                };
                final Throwable ex = new TestException();
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        sub[0].onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                ts.assertValueAt(0, 0);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void onErrorOnTimeoutRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp = PublishProcessor.create();
                final Subscriber<?>[] sub = { null, null };
                final Flowable<Integer> pp2 = new Flowable<Integer>() {

                    int count;

                    @Override
                    protected void subscribeActual(Subscriber<? super Integer> s) {
                        assertFalse(((Disposable) s).isDisposed());
                        s.onSubscribe(new BooleanSubscription());
                        sub[count++] = s;
                    }
                };
                TestSubscriber<Integer> ts = pp.timeout(Functions.justFunction(pp2)).test();
                pp.onNext(0);
                final Throwable ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        sub[0].onComplete();
                    }
                };
                TestHelper.race(r1, r2);
                ts.assertValueAt(0, 0);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void onCompleteOnTimeoutRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp = PublishProcessor.create();
                final Subscriber<?>[] sub = { null, null };
                final Flowable<Integer> pp2 = new Flowable<Integer>() {

                    int count;

                    @Override
                    protected void subscribeActual(Subscriber<? super Integer> s) {
                        assertFalse(((Disposable) s).isDisposed());
                        s.onSubscribe(new BooleanSubscription());
                        sub[count++] = s;
                    }
                };
                TestSubscriber<Integer> ts = pp.timeout(Functions.justFunction(pp2)).test();
                pp.onNext(0);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp.onComplete();
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        sub[0].onComplete();
                    }
                };
                TestHelper.race(r1, r2);
                ts.assertValueAt(0, 0);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void onCompleteOnTimeoutRaceFallback() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp = PublishProcessor.create();
                final Subscriber<?>[] sub = { null, null };
                final Flowable<Integer> pp2 = new Flowable<Integer>() {

                    int count;

                    @Override
                    protected void subscribeActual(Subscriber<? super Integer> s) {
                        assertFalse(((Disposable) s).isDisposed());
                        s.onSubscribe(new BooleanSubscription());
                        sub[count++] = s;
                    }
                };
                TestSubscriber<Integer> ts = pp.timeout(Functions.justFunction(pp2), Flowable.<Integer>never()).test();
                pp.onNext(0);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp.onComplete();
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        sub[0].onComplete();
                    }
                };
                TestHelper.race(r1, r2);
                ts.assertValueAt(0, 0);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void disposedUpfront() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final AtomicInteger counter = new AtomicInteger();
        Flowable<Object> timeoutAndFallback = Flowable.never().doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) throws Exception {
                counter.incrementAndGet();
            }
        });
        pp.timeout(timeoutAndFallback, Functions.justFunction(timeoutAndFallback)).test(1, true).assertEmpty();
        assertEquals(0, counter.get());
    }

    @Test
    public void disposedUpfrontFallback() {
        PublishProcessor<Object> pp = PublishProcessor.create();
        final AtomicInteger counter = new AtomicInteger();
        Flowable<Object> timeoutAndFallback = Flowable.never().doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) throws Exception {
                counter.incrementAndGet();
            }
        });
        pp.timeout(timeoutAndFallback, Functions.justFunction(timeoutAndFallback), timeoutAndFallback).test(1, true).assertEmpty();
        assertEquals(0, counter.get());
    }

    @Test
    public void timeoutConsumerDoubleOnSubscribe() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            BehaviorProcessor.createDefault(1).timeout(Functions.justFunction(new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    BooleanSubscription bs1 = new BooleanSubscription();
                    s.onSubscribe(bs1);
                    BooleanSubscription bs2 = new BooleanSubscription();
                    s.onSubscribe(bs2);
                    assertFalse(bs1.isCancelled());
                    assertTrue(bs2.isCancelled());
                    s.onComplete();
                }
            })).test().assertFailure(TimeoutException.class, 1);
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void timeoutConsumerIsDisposed() {
        TimeoutConsumer consumer = new TimeoutConsumer(0, null);
        assertFalse(consumer.isDisposed());
        consumer.dispose();
        assertTrue(consumer.isDisposed());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableTimeoutWithSelectorTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorNormal1() throws java.lang.Throwable {
            this.payloads.timeoutSelectorNormal1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorTimeoutFirst() throws java.lang.Throwable {
            this.payloads.timeoutSelectorTimeoutFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorFirstThrows() throws java.lang.Throwable {
            this.payloads.timeoutSelectorFirstThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorSubsequentThrows() throws java.lang.Throwable {
            this.payloads.timeoutSelectorSubsequentThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorFirstFlowableThrows() throws java.lang.Throwable {
            this.payloads.timeoutSelectorFirstFlowableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorSubsequentFlowableThrows() throws java.lang.Throwable {
            this.payloads.timeoutSelectorSubsequentFlowableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorWithFirstTimeoutFirstAndNoOtherFlowable() throws java.lang.Throwable {
            this.payloads.timeoutSelectorWithFirstTimeoutFirstAndNoOtherFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorWithTimeoutFirstAndNoOtherFlowable() throws java.lang.Throwable {
            this.payloads.timeoutSelectorWithTimeoutFirstAndNoOtherFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorWithTimeoutAndOnNextRaceCondition() throws java.lang.Throwable {
            this.payloads.timeoutSelectorWithTimeoutAndOnNextRaceCondition.evaluate();
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
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyInner() throws java.lang.Throwable {
            this.payloads.emptyInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badInnerSource() throws java.lang.Throwable {
            this.payloads.badInnerSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badInnerSourceOther() throws java.lang.Throwable {
            this.payloads.badInnerSourceOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withOtherMainError() throws java.lang.Throwable {
            this.payloads.withOtherMainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceTimeout() throws java.lang.Throwable {
            this.payloads.badSourceTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorTake() throws java.lang.Throwable {
            this.payloads.selectorTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorFallbackTake() throws java.lang.Throwable {
            this.payloads.selectorFallbackTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lateOnTimeoutError() throws java.lang.Throwable {
            this.payloads.lateOnTimeoutError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lateOnTimeoutFallbackRace() throws java.lang.Throwable {
            this.payloads.lateOnTimeoutFallbackRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorOnTimeoutRace() throws java.lang.Throwable {
            this.payloads.onErrorOnTimeoutRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteOnTimeoutRace() throws java.lang.Throwable {
            this.payloads.onCompleteOnTimeoutRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteOnTimeoutRaceFallback() throws java.lang.Throwable {
            this.payloads.onCompleteOnTimeoutRaceFallback.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedUpfront() throws java.lang.Throwable {
            this.payloads.disposedUpfront.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedUpfrontFallback() throws java.lang.Throwable {
            this.payloads.disposedUpfrontFallback.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutConsumerDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.timeoutConsumerDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutConsumerIsDisposed() throws java.lang.Throwable {
            this.payloads.timeoutConsumerIsDisposed.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeoutWithSelectorTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeoutWithSelectorTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeoutWithSelectorTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeoutWithSelectorTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableTimeoutWithSelectorTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeoutWithSelectorTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableTimeoutWithSelectorTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableTimeoutWithSelectorTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement timeoutSelectorNormal1;

            public org.junit.runners.model.Statement timeoutSelectorTimeoutFirst;

            public org.junit.runners.model.Statement timeoutSelectorFirstThrows;

            public org.junit.runners.model.Statement timeoutSelectorSubsequentThrows;

            public org.junit.runners.model.Statement timeoutSelectorFirstFlowableThrows;

            public org.junit.runners.model.Statement timeoutSelectorSubsequentFlowableThrows;

            public org.junit.runners.model.Statement timeoutSelectorWithFirstTimeoutFirstAndNoOtherFlowable;

            public org.junit.runners.model.Statement timeoutSelectorWithTimeoutFirstAndNoOtherFlowable;

            public org.junit.runners.model.Statement timeoutSelectorWithTimeoutAndOnNextRaceCondition;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement emptyInner;

            public org.junit.runners.model.Statement badInnerSource;

            public org.junit.runners.model.Statement badInnerSourceOther;

            public org.junit.runners.model.Statement withOtherMainError;

            public org.junit.runners.model.Statement badSourceTimeout;

            public org.junit.runners.model.Statement selectorTake;

            public org.junit.runners.model.Statement selectorFallbackTake;

            public org.junit.runners.model.Statement lateOnTimeoutError;

            public org.junit.runners.model.Statement lateOnTimeoutFallbackRace;

            public org.junit.runners.model.Statement onErrorOnTimeoutRace;

            public org.junit.runners.model.Statement onCompleteOnTimeoutRace;

            public org.junit.runners.model.Statement onCompleteOnTimeoutRaceFallback;

            public org.junit.runners.model.Statement disposedUpfront;

            public org.junit.runners.model.Statement disposedUpfrontFallback;

            public org.junit.runners.model.Statement timeoutConsumerDoubleOnSubscribe;

            public org.junit.runners.model.Statement timeoutConsumerIsDisposed;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.timeoutSelectorNormal1 = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::timeoutSelectorNormal1, "timeoutSelectorNormal1", this);
            this.payloads.timeoutSelectorTimeoutFirst = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::timeoutSelectorTimeoutFirst, "timeoutSelectorTimeoutFirst", this);
            this.payloads.timeoutSelectorFirstThrows = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::timeoutSelectorFirstThrows, "timeoutSelectorFirstThrows", this);
            this.payloads.timeoutSelectorSubsequentThrows = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::timeoutSelectorSubsequentThrows, "timeoutSelectorSubsequentThrows", this);
            this.payloads.timeoutSelectorFirstFlowableThrows = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::timeoutSelectorFirstFlowableThrows, "timeoutSelectorFirstFlowableThrows", this);
            this.payloads.timeoutSelectorSubsequentFlowableThrows = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::timeoutSelectorSubsequentFlowableThrows, "timeoutSelectorSubsequentFlowableThrows", this);
            this.payloads.timeoutSelectorWithFirstTimeoutFirstAndNoOtherFlowable = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::timeoutSelectorWithFirstTimeoutFirstAndNoOtherFlowable, "timeoutSelectorWithFirstTimeoutFirstAndNoOtherFlowable", this);
            this.payloads.timeoutSelectorWithTimeoutFirstAndNoOtherFlowable = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::timeoutSelectorWithTimeoutFirstAndNoOtherFlowable, "timeoutSelectorWithTimeoutFirstAndNoOtherFlowable", this);
            this.payloads.timeoutSelectorWithTimeoutAndOnNextRaceCondition = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::timeoutSelectorWithTimeoutAndOnNextRaceCondition, "timeoutSelectorWithTimeoutAndOnNextRaceCondition", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.empty = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::empty, "empty", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::error, "error", this);
            this.payloads.emptyInner = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::emptyInner, "emptyInner", this);
            this.payloads.badInnerSource = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::badInnerSource, "badInnerSource", this);
            this.payloads.badInnerSourceOther = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::badInnerSourceOther, "badInnerSourceOther", this);
            this.payloads.withOtherMainError = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::withOtherMainError, "withOtherMainError", this);
            this.payloads.badSourceTimeout = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::badSourceTimeout, "badSourceTimeout", this);
            this.payloads.selectorTake = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::selectorTake, "selectorTake", this);
            this.payloads.selectorFallbackTake = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::selectorFallbackTake, "selectorFallbackTake", this);
            this.payloads.lateOnTimeoutError = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::lateOnTimeoutError, "lateOnTimeoutError", this);
            this.payloads.lateOnTimeoutFallbackRace = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::lateOnTimeoutFallbackRace, "lateOnTimeoutFallbackRace", this);
            this.payloads.onErrorOnTimeoutRace = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::onErrorOnTimeoutRace, "onErrorOnTimeoutRace", this);
            this.payloads.onCompleteOnTimeoutRace = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::onCompleteOnTimeoutRace, "onCompleteOnTimeoutRace", this);
            this.payloads.onCompleteOnTimeoutRaceFallback = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::onCompleteOnTimeoutRaceFallback, "onCompleteOnTimeoutRaceFallback", this);
            this.payloads.disposedUpfront = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::disposedUpfront, "disposedUpfront", this);
            this.payloads.disposedUpfrontFallback = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::disposedUpfrontFallback, "disposedUpfrontFallback", this);
            this.payloads.timeoutConsumerDoubleOnSubscribe = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::timeoutConsumerDoubleOnSubscribe, "timeoutConsumerDoubleOnSubscribe", this);
            this.payloads.timeoutConsumerIsDisposed = _ClassStatement.forPayload(FlowableTimeoutWithSelectorTest::timeoutConsumerIsDisposed, "timeoutConsumerIsDisposed", this);
        }
    }
}
