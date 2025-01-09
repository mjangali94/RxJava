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
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableConcatTest {

    @Test
    public void concat() {
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        final String[] o = { "1", "3", "5", "7" };
        final String[] e = { "2", "4", "6" };
        final Flowable<String> odds = Flowable.fromArray(o);
        final Flowable<String> even = Flowable.fromArray(e);
        Flowable<String> concat = Flowable.concat(odds, even);
        concat.subscribe(subscriber);
        verify(subscriber, times(7)).onNext(anyString());
    }

    @Test
    public void concatWithList() {
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        final String[] o = { "1", "3", "5", "7" };
        final String[] e = { "2", "4", "6" };
        final Flowable<String> odds = Flowable.fromArray(o);
        final Flowable<String> even = Flowable.fromArray(e);
        final List<Flowable<String>> list = new ArrayList<>();
        list.add(odds);
        list.add(even);
        Flowable<String> concat = Flowable.concat(Flowable.fromIterable(list));
        concat.subscribe(subscriber);
        verify(subscriber, times(7)).onNext(anyString());
    }

    @Test
    public void concatObservableOfObservables() {
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        final String[] o = { "1", "3", "5", "7" };
        final String[] e = { "2", "4", "6" };
        final Flowable<String> odds = Flowable.fromArray(o);
        final Flowable<String> even = Flowable.fromArray(e);
        Flowable<Flowable<String>> flowableOfFlowables = Flowable.unsafeCreate(new Publisher<Flowable<String>>() {

            @Override
            public void subscribe(Subscriber<? super Flowable<String>> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                // simulate what would happen in an observable
                subscriber.onNext(odds);
                subscriber.onNext(even);
                subscriber.onComplete();
            }
        });
        Flowable<String> concat = Flowable.concat(flowableOfFlowables);
        concat.subscribe(subscriber);
        verify(subscriber, times(7)).onNext(anyString());
    }

    /**
     * Simple concat of 2 asynchronous observables ensuring it emits in correct order.
     */
    @Test
    public void simpleAsyncConcat() {
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        TestObservable<String> o1 = new TestObservable<>("one", "two", "three");
        TestObservable<String> o2 = new TestObservable<>("four", "five", "six");
        Flowable.concat(Flowable.unsafeCreate(o1), Flowable.unsafeCreate(o2)).subscribe(subscriber);
        try {
            // wait for async observables to complete
            o1.t.join();
            o2.t.join();
        } catch (Throwable e) {
            throw new RuntimeException("failed waiting on threads");
        }
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext("one");
        inOrder.verify(subscriber, times(1)).onNext("two");
        inOrder.verify(subscriber, times(1)).onNext("three");
        inOrder.verify(subscriber, times(1)).onNext("four");
        inOrder.verify(subscriber, times(1)).onNext("five");
        inOrder.verify(subscriber, times(1)).onNext("six");
    }

    @Test
    public void nestedAsyncConcatLoop() throws Throwable {
        for (int i = 0; i < 500; i++) {
            if (i % 10 == 0) {
                // System.out.println("testNestedAsyncConcat >> " + i);
            }
            nestedAsyncConcat();
        }
    }

    /**
     * Test an async Flowable that emits more async Observables.
     * @throws InterruptedException if the test is interrupted
     */
    @Test
    public void nestedAsyncConcat() throws InterruptedException {
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        final TestObservable<String> o1 = new TestObservable<>("one", "two", "three");
        final TestObservable<String> o2 = new TestObservable<>("four", "five", "six");
        final TestObservable<String> o3 = new TestObservable<>("seven", "eight", "nine");
        final CountDownLatch allowThird = new CountDownLatch(1);
        final AtomicReference<Thread> parent = new AtomicReference<>();
        final CountDownLatch parentHasStarted = new CountDownLatch(1);
        final CountDownLatch parentHasFinished = new CountDownLatch(1);
        Flowable<Flowable<String>> observableOfObservables = Flowable.unsafeCreate(new Publisher<Flowable<String>>() {

            @Override
            public void subscribe(final Subscriber<? super Flowable<String>> subscriber) {
                final Disposable d = Disposable.empty();
                subscriber.onSubscribe(new Subscription() {

                    @Override
                    public void request(long n) {
                    }

                    @Override
                    public void cancel() {
                        d.dispose();
                    }
                });
                parent.set(new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            // emit first
                            if (!d.isDisposed()) {
                                // System.out.println("Emit o1");
                                subscriber.onNext(Flowable.unsafeCreate(o1));
                            }
                            // emit second
                            if (!d.isDisposed()) {
                                // System.out.println("Emit o2");
                                subscriber.onNext(Flowable.unsafeCreate(o2));
                            }
                            // wait until sometime later and emit third
                            try {
                                allowThird.await();
                            } catch (InterruptedException e) {
                                subscriber.onError(e);
                            }
                            if (!d.isDisposed()) {
                                // System.out.println("Emit o3");
                                subscriber.onNext(Flowable.unsafeCreate(o3));
                            }
                        } catch (Throwable e) {
                            subscriber.onError(e);
                        } finally {
                            // System.out.println("Done parent Flowable");
                            subscriber.onComplete();
                            parentHasFinished.countDown();
                        }
                    }
                }));
                parent.get().start();
                parentHasStarted.countDown();
            }
        });
        Flowable.concat(observableOfObservables).subscribe(subscriber);
        // wait for parent to start
        parentHasStarted.await();
        try {
            // wait for first 2 async observables to complete
            // System.out.println("Thread1 is starting ... waiting for it to complete ...");
            o1.waitForThreadDone();
            // System.out.println("Thread2 is starting ... waiting for it to complete ...");
            o2.waitForThreadDone();
        } catch (Throwable e) {
            throw new RuntimeException("failed waiting on threads", e);
        }
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext("one");
        inOrder.verify(subscriber, times(1)).onNext("two");
        inOrder.verify(subscriber, times(1)).onNext("three");
        inOrder.verify(subscriber, times(1)).onNext("four");
        inOrder.verify(subscriber, times(1)).onNext("five");
        inOrder.verify(subscriber, times(1)).onNext("six");
        // we shouldn't have the following 3 yet
        inOrder.verify(subscriber, never()).onNext("seven");
        inOrder.verify(subscriber, never()).onNext("eight");
        inOrder.verify(subscriber, never()).onNext("nine");
        // we should not be completed yet
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        // now allow the third
        allowThird.countDown();
        try {
            // wait for 3rd to complete
            o3.waitForThreadDone();
        } catch (Throwable e) {
            throw new RuntimeException("failed waiting on threads", e);
        }
        try {
            // wait for the parent to complete
            if (!parentHasFinished.await(5, TimeUnit.SECONDS)) {
                fail("Parent didn't finish within the time limit");
            }
        } catch (Throwable e) {
            throw new RuntimeException("failed waiting on threads", e);
        }
        inOrder.verify(subscriber, times(1)).onNext("seven");
        inOrder.verify(subscriber, times(1)).onNext("eight");
        inOrder.verify(subscriber, times(1)).onNext("nine");
        verify(subscriber, never()).onError(any(Throwable.class));
        inOrder.verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void blockedObservableOfObservables() {
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        final String[] o = { "1", "3", "5", "7" };
        final String[] e = { "2", "4", "6" };
        final Flowable<String> odds = Flowable.fromArray(o);
        final Flowable<String> even = Flowable.fromArray(e);
        final CountDownLatch callOnce = new CountDownLatch(1);
        final CountDownLatch okToContinue = new CountDownLatch(1);
        TestObservable<Flowable<String>> observableOfObservables = new TestObservable<>(callOnce, okToContinue, odds, even);
        Flowable<String> concatF = Flowable.concat(Flowable.unsafeCreate(observableOfObservables));
        concatF.subscribe(subscriber);
        try {
            // Block main thread to allow observables to serve up o1.
            callOnce.await();
        } catch (Throwable ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
        // The concated observable should have served up all of the odds.
        verify(subscriber, times(1)).onNext("1");
        verify(subscriber, times(1)).onNext("3");
        verify(subscriber, times(1)).onNext("5");
        verify(subscriber, times(1)).onNext("7");
        try {
            // unblock observables so it can serve up o2 and complete
            okToContinue.countDown();
            observableOfObservables.t.join();
        } catch (Throwable ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
        // The concatenated observable should now have served up all the evens.
        verify(subscriber, times(1)).onNext("2");
        verify(subscriber, times(1)).onNext("4");
        verify(subscriber, times(1)).onNext("6");
    }

    @Test
    public void concatConcurrentWithInfinity() {
        final TestObservable<String> w1 = new TestObservable<>("one", "two", "three");
        // This observable will send "hello" MAX_VALUE time.
        final TestObservable<String> w2 = new TestObservable<>("hello", Integer.MAX_VALUE);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        TestObservable<Flowable<String>> observableOfObservables = new TestObservable<>(Flowable.unsafeCreate(w1), Flowable.unsafeCreate(w2));
        Flowable<String> concatF = Flowable.concat(Flowable.unsafeCreate(observableOfObservables));
        concatF.take(50).subscribe(subscriber);
        // Wait for the thread to start up.
        try {
            w1.waitForThreadDone();
            w2.waitForThreadDone();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext("one");
        inOrder.verify(subscriber, times(1)).onNext("two");
        inOrder.verify(subscriber, times(1)).onNext("three");
        inOrder.verify(subscriber, times(47)).onNext("hello");
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void concatNonBlockingObservables() {
        final CountDownLatch okToContinueW1 = new CountDownLatch(1);
        final CountDownLatch okToContinueW2 = new CountDownLatch(1);
        final TestObservable<String> w1 = new TestObservable<>(null, okToContinueW1, "one", "two", "three");
        final TestObservable<String> w2 = new TestObservable<>(null, okToContinueW2, "four", "five", "six");
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        Flowable<Flowable<String>> observableOfObservables = Flowable.unsafeCreate(new Publisher<Flowable<String>>() {

            @Override
            public void subscribe(Subscriber<? super Flowable<String>> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                // simulate what would happen in an observable
                subscriber.onNext(Flowable.unsafeCreate(w1));
                subscriber.onNext(Flowable.unsafeCreate(w2));
                subscriber.onComplete();
            }
        });
        Flowable<String> concat = Flowable.concat(observableOfObservables);
        concat.subscribe(subscriber);
        verify(subscriber, times(0)).onComplete();
        try {
            // release both threads
            okToContinueW1.countDown();
            okToContinueW2.countDown();
            // wait for both to finish
            w1.t.join();
            w2.t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext("one");
        inOrder.verify(subscriber, times(1)).onNext("two");
        inOrder.verify(subscriber, times(1)).onNext("three");
        inOrder.verify(subscriber, times(1)).onNext("four");
        inOrder.verify(subscriber, times(1)).onNext("five");
        inOrder.verify(subscriber, times(1)).onNext("six");
        verify(subscriber, times(1)).onComplete();
    }

    /**
     * Test unsubscribing the concatenated Flowable in a single thread.
     */
    @Test
    public void concatUnsubscribe() {
        final CountDownLatch callOnce = new CountDownLatch(1);
        final CountDownLatch okToContinue = new CountDownLatch(1);
        final TestObservable<String> w1 = new TestObservable<>("one", "two", "three");
        final TestObservable<String> w2 = new TestObservable<>(callOnce, okToContinue, "four", "five", "six");
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<String> ts = new TestSubscriber<>(subscriber, 0L);
        final Flowable<String> concat = Flowable.concat(Flowable.unsafeCreate(w1), Flowable.unsafeCreate(w2));
        try {
            // Subscribe
            concat.subscribe(ts);
            // Block main thread to allow observable "w1" to complete and observable "w2" to call onNext once.
            callOnce.await();
            // Unsubcribe
            ts.cancel();
            // Unblock the observable to continue.
            okToContinue.countDown();
            w1.t.join();
            w2.t.join();
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext("one");
        inOrder.verify(subscriber, times(1)).onNext("two");
        inOrder.verify(subscriber, times(1)).onNext("three");
        inOrder.verify(subscriber, times(1)).onNext("four");
        inOrder.verify(subscriber, never()).onNext("five");
        inOrder.verify(subscriber, never()).onNext("six");
        inOrder.verify(subscriber, never()).onComplete();
    }

    /**
     * All observables will be running in different threads so subscribe() is unblocked. CountDownLatch is only used in order to call unsubscribe() in a predictable manner.
     */
    @Test
    public void concatUnsubscribeConcurrent() {
        final CountDownLatch callOnce = new CountDownLatch(1);
        final CountDownLatch okToContinue = new CountDownLatch(1);
        final TestObservable<String> w1 = new TestObservable<>("one", "two", "three");
        final TestObservable<String> w2 = new TestObservable<>(callOnce, okToContinue, "four", "five", "six");
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<String> ts = new TestSubscriber<>(subscriber, 0L);
        TestObservable<Flowable<String>> observableOfObservables = new TestObservable<>(Flowable.unsafeCreate(w1), Flowable.unsafeCreate(w2));
        Flowable<String> concatF = Flowable.concat(Flowable.unsafeCreate(observableOfObservables));
        concatF.subscribe(ts);
        try {
            // Block main thread to allow observable "w1" to complete and observable "w2" to call onNext exactly once.
            callOnce.await();
            // "four" from w2 has been processed by onNext()
            ts.cancel();
            // "five" and "six" will NOT be processed by onNext()
            // Unblock the observable to continue.
            okToContinue.countDown();
            w1.t.join();
            w2.t.join();
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext("one");
        inOrder.verify(subscriber, times(1)).onNext("two");
        inOrder.verify(subscriber, times(1)).onNext("three");
        inOrder.verify(subscriber, times(1)).onNext("four");
        inOrder.verify(subscriber, never()).onNext("five");
        inOrder.verify(subscriber, never()).onNext("six");
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    private static class TestObservable<T> implements Publisher<T> {

        private final Subscription s = new Subscription() {

            @Override
            public void request(long n) {
            }

            @Override
            public void cancel() {
                subscribed = false;
            }
        };

        private final List<T> values;

        private Thread t;

        private int count;

        private volatile boolean subscribed = true;

        private final CountDownLatch once;

        private final CountDownLatch okToContinue;

        private final CountDownLatch threadHasStarted = new CountDownLatch(1);

        private final T seed;

        private final int size;

        @SafeVarargs
        TestObservable(T... values) {
            this(null, null, values);
        }

        @SafeVarargs
        TestObservable(CountDownLatch once, CountDownLatch okToContinue, T... values) {
            this.values = Arrays.asList(values);
            this.size = this.values.size();
            this.once = once;
            this.okToContinue = okToContinue;
            this.seed = null;
        }

        TestObservable(T seed, int size) {
            values = null;
            once = null;
            okToContinue = null;
            this.seed = seed;
            this.size = size;
        }

        @Override
        public void subscribe(final Subscriber<? super T> subscriber) {
            subscriber.onSubscribe(s);
            t = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        while (count < size && subscribed) {
                            if (null != values) {
                                subscriber.onNext(values.get(count));
                            } else {
                                subscriber.onNext(seed);
                            }
                            count++;
                            // Unblock the main thread to call unsubscribe.
                            if (null != once) {
                                once.countDown();
                            }
                            // Block until the main thread has called unsubscribe.
                            if (null != okToContinue) {
                                okToContinue.await(5, TimeUnit.SECONDS);
                            }
                        }
                        if (subscribed) {
                            subscriber.onComplete();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        fail(e.getMessage());
                    }
                }
            });
            t.start();
            threadHasStarted.countDown();
        }

        void waitForThreadDone() throws InterruptedException {
            threadHasStarted.await();
            t.join();
        }
    }

    @Test
    public void multipleObservers() {
        Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
        Subscriber<Object> subscriber2 = TestHelper.mockSubscriber();
        TestScheduler s = new TestScheduler();
        Flowable<Long> timer = Flowable.interval(500, TimeUnit.MILLISECONDS, s).take(2);
        Flowable<Long> f = Flowable.concat(timer, timer);
        f.subscribe(subscriber1);
        f.subscribe(subscriber2);
        InOrder inOrder1 = inOrder(subscriber1);
        InOrder inOrder2 = inOrder(subscriber2);
        s.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        inOrder1.verify(subscriber1, times(1)).onNext(0L);
        inOrder2.verify(subscriber2, times(1)).onNext(0L);
        s.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        inOrder1.verify(subscriber1, times(1)).onNext(1L);
        inOrder2.verify(subscriber2, times(1)).onNext(1L);
        s.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        inOrder1.verify(subscriber1, times(1)).onNext(0L);
        inOrder2.verify(subscriber2, times(1)).onNext(0L);
        s.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        inOrder1.verify(subscriber1, times(1)).onNext(1L);
        inOrder2.verify(subscriber2, times(1)).onNext(1L);
        inOrder1.verify(subscriber1, times(1)).onComplete();
        inOrder2.verify(subscriber2, times(1)).onComplete();
        verify(subscriber1, never()).onError(any(Throwable.class));
        verify(subscriber2, never()).onError(any(Throwable.class));
    }

    @Test
    public void concatVeryLongObservableOfObservables() {
        final int n = 10000;
        Flowable<Flowable<Integer>> source = Flowable.range(0, n).map(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return Flowable.just(v);
            }
        });
        Single<List<Integer>> result = Flowable.concat(source).toList();
        SingleObserver<List<Integer>> o = TestHelper.mockSingleObserver();
        InOrder inOrder = inOrder(o);
        result.subscribe(o);
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        inOrder.verify(o).onSuccess(list);
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void concatVeryLongObservableOfObservablesTakeHalf() {
        final int n = 10000;
        Flowable<Flowable<Integer>> source = Flowable.range(0, n).map(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return Flowable.just(v);
            }
        });
        Single<List<Integer>> result = Flowable.concat(source).take(n / 2).toList();
        SingleObserver<List<Integer>> o = TestHelper.mockSingleObserver();
        InOrder inOrder = inOrder(o);
        result.subscribe(o);
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n / 2; i++) {
            list.add(i);
        }
        inOrder.verify(o).onSuccess(list);
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void concatOuterBackpressure() {
        assertEquals(1, (int) Flowable.<Integer>empty().concatWith(Flowable.just(1)).take(1).blockingSingle());
    }

    @Test
    public void innerBackpressureWithAlignedBoundaries() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(0, Flowable.bufferSize() * 2).concatWith(Flowable.range(0, Flowable.bufferSize() * 2)).observeOn(// observeOn has a backpressured RxRingBuffer
        Schedulers.computation()).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        assertEquals(Flowable.bufferSize() * 4, ts.values().size());
    }

    /*
     * Testing without counts aligned with buffer sizes because concat must prevent the subscription
     * to the next Flowable if request == 0 which can happen at the end of a subscription
     * if the request size == emitted size. It needs to delay subscription until the next request when aligned,
     * when not aligned, it just subscribesNext with the outstanding request amount.
     */
    @Test
    public void innerBackpressureWithoutAlignedBoundaries() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(0, (Flowable.bufferSize() * 2) + 10).concatWith(Flowable.range(0, (Flowable.bufferSize() * 2) + 10)).observeOn(// observeOn has a backpressured RxRingBuffer
        Schedulers.computation()).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        assertEquals((Flowable.bufferSize() * 4) + 20, ts.values().size());
    }

    // https://github.com/ReactiveX/RxJava/issues/1818
    @Test
    public void concatWithNonCompliantSourceDoubleOnComplete() {
        Flowable<String> f = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onNext("hello");
                s.onComplete();
                s.onComplete();
            }
        });
        TestSubscriberEx<String> ts = new TestSubscriberEx<>();
        Flowable.concat(f, f).subscribe(ts);
        ts.awaitDone(500, TimeUnit.MILLISECONDS);
        ts.assertTerminated();
        ts.assertNoErrors();
        ts.assertValues("hello", "hello");
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
        Flowable.range(1, n).concatMap(func).subscribe(new DefaultSubscriber<Integer>() {

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
    public void requestOverflowDoesNotStallStream() {
        Flowable<Integer> f1 = Flowable.just(1, 2, 3);
        Flowable<Integer> f2 = Flowable.just(4, 5, 6);
        final AtomicBoolean completed = new AtomicBoolean(false);
        f1.concatWith(f2).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onComplete() {
                completed.set(true);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
                request(2);
            }
        });
        assertTrue(completed.get());
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
            }).observeOn(Schedulers.computation()).subscribe(ts);
            ts.awaitDone(2500, TimeUnit.MILLISECONDS);
            ts.assertTerminated();
            ts.assertNoErrors();
            assertEquals(1000, ts.values().size());
            assertEquals((Integer) 999, ts.values().get(999));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void arrayDelayError() {
        Publisher<Integer>[] sources = new Publisher[] { Flowable.just(1), null, Flowable.range(2, 3), Flowable.error(new TestException()), Flowable.empty() };
        TestSubscriberEx<Integer> ts = Flowable.concatArrayDelayError(sources).to(TestHelper.<Integer>testConsumer());
        ts.assertFailure(CompositeException.class, 1, 2, 3, 4);
        CompositeException composite = (CompositeException) ts.errors().get(0);
        List<Throwable> list = composite.getExceptions();
        assertTrue(list.get(0).toString(), list.get(0) instanceof NullPointerException);
        assertTrue(list.get(1).toString(), list.get(1) instanceof TestException);
    }

    @Test
    public void scalarAndRangeBackpressured() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        Flowable.just(1).concatWith(Flowable.range(2, 3)).subscribe(ts);
        ts.assertNoValues();
        ts.request(5);
        ts.assertValues(1, 2, 3, 4);
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void scalarAndEmptyBackpressured() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        Flowable.just(1).concatWith(Flowable.<Integer>empty()).subscribe(ts);
        ts.assertNoValues();
        ts.request(5);
        ts.assertValue(1);
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void rangeAndEmptyBackpressured() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        Flowable.range(1, 2).concatWith(Flowable.<Integer>empty()).subscribe(ts);
        ts.assertNoValues();
        ts.request(5);
        ts.assertValues(1, 2);
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void emptyAndScalarBackpressured() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        Flowable.<Integer>empty().concatWith(Flowable.just(1)).subscribe(ts);
        ts.assertNoValues();
        ts.request(5);
        ts.assertValue(1);
        ts.assertComplete();
        ts.assertNoErrors();
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
        Flowable.just(Flowable.just(1)).concatMap((Function) Functions.identity()).subscribe(ts);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void concatMapJustRange() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(Flowable.range(1, 5)).concatMap((Function) Functions.identity()).subscribe(ts);
        ts.assertValues(1, 2, 3, 4, 5);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void concatMapDelayErrorJustJust() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(Flowable.just(1)).concatMapDelayError((Function) Functions.identity()).subscribe(ts);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void concatMapDelayErrorJustRange() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(Flowable.range(1, 5)).concatMapDelayError((Function) Functions.identity()).subscribe(ts);
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

    static final class InfiniteIterator implements Iterator<Integer>, Iterable<Integer> {

        int count;

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Integer next() {
            return count++;
        }

        @Override
        public void remove() {
        }

        @Override
        public Iterator<Integer> iterator() {
            return this;
        }
    }

    @Test
    public void veryLongTake() {
        Flowable.fromIterable(new InfiniteIterator()).concatWith(Flowable.<Integer>empty()).take(10).test().assertResult(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Test
    public void concat3() {
        Flowable<Integer> source = Flowable.just(1);
        Flowable.concat(source, source, source).test().assertResult(1, 1, 1);
    }

    @Test
    public void concat4() {
        Flowable<Integer> source = Flowable.just(1);
        Flowable.concat(source, source, source, source).test().assertResult(1, 1, 1, 1);
    }

    @Test
    public void concatArrayDelayError() {
        Flowable.concatArrayDelayError(Flowable.just(1), Flowable.just(2), Flowable.just(3), Flowable.just(4)).test().assertResult(1, 2, 3, 4);
    }

    @Test
    public void concatArrayDelayErrorWithError() {
        Flowable.concatArrayDelayError(Flowable.just(1), Flowable.just(2), Flowable.just(3).concatWith(Flowable.<Integer>error(new TestException())), Flowable.just(4)).test().assertFailure(TestException.class, 1, 2, 3, 4);
    }

    @Test
    public void concatIterableDelayError() {
        Flowable.concatDelayError(Arrays.asList(Flowable.just(1), Flowable.just(2), Flowable.just(3), Flowable.just(4))).test().assertResult(1, 2, 3, 4);
    }

    @Test
    public void concatIterableDelayErrorWithError() {
        Flowable.concatDelayError(Arrays.asList(Flowable.just(1), Flowable.just(2), Flowable.just(3).concatWith(Flowable.<Integer>error(new TestException())), Flowable.just(4))).test().assertFailure(TestException.class, 1, 2, 3, 4);
    }

    @Test
    public void concatObservableDelayError() {
        Flowable.concatDelayError(Flowable.just(Flowable.just(1), Flowable.just(2), Flowable.just(3), Flowable.just(4))).test().assertResult(1, 2, 3, 4);
    }

    @Test
    public void concatObservableDelayErrorWithError() {
        Flowable.concatDelayError(Flowable.just(Flowable.just(1), Flowable.just(2), Flowable.just(3).concatWith(Flowable.<Integer>error(new TestException())), Flowable.just(4))).test().assertFailure(TestException.class, 1, 2, 3, 4);
    }

    @Test
    public void concatObservableDelayErrorBoundary() {
        Flowable.concatDelayError(Flowable.just(Flowable.just(1), Flowable.just(2), Flowable.just(3).concatWith(Flowable.<Integer>error(new TestException())), Flowable.just(4)), 2, false).test().assertFailure(TestException.class, 1, 2, 3);
    }

    @Test
    public void concatObservableDelayErrorTillEnd() {
        Flowable.concatDelayError(Flowable.just(Flowable.just(1), Flowable.just(2), Flowable.just(3).concatWith(Flowable.<Integer>error(new TestException())), Flowable.just(4)), 2, true).test().assertFailure(TestException.class, 1, 2, 3, 4);
    }

    @Test
    public void concatMapDelayError() {
        Flowable.just(Flowable.just(1), Flowable.just(2)).concatMapDelayError(Functions.<Flowable<Integer>>identity()).test().assertResult(1, 2);
    }

    @Test
    public void concatMapDelayErrorWithError() {
        Flowable.just(Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())), Flowable.just(2)).concatMapDelayError(Functions.<Flowable<Integer>>identity()).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void concatMapIterableBufferSize() {
        Flowable.just(1, 2).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(1, 2, 3, 4, 5);
            }
        }, 1).test().assertResult(1, 2, 3, 4, 5, 1, 2, 3, 4, 5);
    }

    @Test
    public void emptyArray() {
        assertSame(Flowable.empty(), Flowable.concatArrayDelayError());
    }

    @Test
    public void singleElementArray() {
        assertSame(Flowable.never(), Flowable.concatArrayDelayError(Flowable.never()));
    }

    @Test
    public void concatMapDelayErrorEmptySource() {
        assertSame(Flowable.empty(), Flowable.<Object>empty().concatMapDelayError(new Function<Object, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Object v) throws Exception {
                return Flowable.just(1);
            }
        }, true, 16));
    }

    @Test
    public void concatMapDelayErrorJustSource() {
        Flowable.just(0).concatMapDelayError(new Function<Object, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Object v) throws Exception {
                return Flowable.just(1);
            }
        }, true, 16).test().assertResult(1);
    }

    @Test
    public void concatArrayEmpty() {
        assertSame(Flowable.empty(), Flowable.concatArray());
    }

    @Test
    public void concatArraySingleElement() {
        assertSame(Flowable.never(), Flowable.concatArray(Flowable.never()));
    }

    @Test
    public void concatMapErrorEmptySource() {
        assertSame(Flowable.empty(), Flowable.<Object>empty().concatMap(new Function<Object, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Object v) throws Exception {
                return Flowable.just(1);
            }
        }, 16));
    }

    @Test
    public void concatMapJustSource() {
        Flowable.just(0).hide().concatMap(new Function<Object, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Object v) throws Exception {
                return Flowable.just(1);
            }
        }, 16).test().assertResult(1);
    }

    @Test
    public void concatMapJustSourceDelayError() {
        Flowable.just(0).hide().concatMapDelayError(new Function<Object, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Object v) throws Exception {
                return Flowable.just(1);
            }
        }, false, 16).test().assertResult(1);
    }

    @Test
    public void concatMapScalarBackpressured() {
        Flowable.just(1).hide().concatMap(Functions.justFunction(Flowable.just(2))).test(1L).assertResult(2);
    }

    @Test
    public void concatMapScalarBackpressuredDelayError() {
        Flowable.just(1).hide().concatMapDelayError(Functions.justFunction(Flowable.just(2))).test(1L).assertResult(2);
    }

    @Test
    public void concatMapEmpty() {
        Flowable.just(1).hide().concatMap(Functions.justFunction(Flowable.empty())).test().assertResult();
    }

    @Test
    public void concatMapEmptyDelayError() {
        Flowable.just(1).hide().concatMapDelayError(Functions.justFunction(Flowable.empty())).test().assertResult();
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
        }.concatMap(Functions.justFunction(Flowable.just(2)), 8).test(0L).assertFailure(IllegalStateException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Object> f) throws Exception {
                return f.concatMap(Functions.justFunction(Flowable.just(2)));
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Object> f) throws Exception {
                return f.concatMapDelayError(Functions.justFunction(Flowable.just(2)));
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
        pp.concatMap(Functions.justFunction(Flowable.just(1))).subscribe(ts);
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
        pp.concatMap(Functions.justFunction(Flowable.just(1).hide())).subscribe(ts);
        pp.onNext(1);
        assertFalse(pp.hasSubscribers());
        ts.assertFailureAndMessage(TestException.class, "First", 1);
    }

    @Test
    public void concatMapInnerError() {
        Flowable.just(1).hide().concatMap(Functions.justFunction(Flowable.error(new TestException()))).test().assertFailure(TestException.class);
    }

    @Test
    public void concatMapInnerErrorDelayError() {
        Flowable.just(1).hide().concatMapDelayError(Functions.justFunction(Flowable.error(new TestException()))).test().assertFailure(TestException.class);
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.concatMap(Functions.justFunction(Flowable.just(1).hide()));
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
        })).to(TestHelper.<Integer>testConsumer());
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
        })).to(TestHelper.<Integer>testConsumer());
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
                return f.concatMapDelayError(Functions.justFunction(Flowable.just(1).hide()));
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
        }).concatMap(Functions.justFunction(Flowable.just(1))).test().assertFailure(TestException.class);
    }

    @Test
    public void fusedCrashDelayError() {
        Flowable.range(1, 2).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).concatMapDelayError(Functions.justFunction(Flowable.just(1))).test().assertFailure(TestException.class);
    }

    @Test
    public void callableCrash() {
        Flowable.just(1).hide().concatMap(Functions.justFunction(Flowable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw new TestException();
            }
        }))).test().assertFailure(TestException.class);
    }

    @Test
    public void callableCrashDelayError() {
        Flowable.just(1).hide().concatMapDelayError(Functions.justFunction(Flowable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw new TestException();
            }
        }))).test().assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.range(1, 2).concatMap(Functions.justFunction(Flowable.just(1))));
        TestHelper.checkDisposed(Flowable.range(1, 2).concatMapDelayError(Functions.justFunction(Flowable.just(1))));
    }

    @Test
    public void notVeryEnd() {
        Flowable.range(1, 2).concatMapDelayError(Functions.justFunction(Flowable.error(new TestException())), false, 16).test().assertFailure(TestException.class);
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).concatMapDelayError(Functions.justFunction(Flowable.just(2)), false, 16).test().assertFailure(TestException.class);
    }

    @Test
    public void mapperThrows() {
        Flowable.range(1, 2).concatMap(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void noSubsequentSubscription() {
        final int[] calls = { 0 };
        Flowable<Integer> source = Flowable.create(new FlowableOnSubscribe<Integer>() {

            @Override
            public void subscribe(FlowableEmitter<Integer> s) throws Exception {
                calls[0]++;
                s.onNext(1);
                s.onComplete();
            }
        }, BackpressureStrategy.MISSING);
        Flowable.concatArray(source, source).firstElement().test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @Test
    public void noSubsequentSubscriptionDelayError() {
        final int[] calls = { 0 };
        Flowable<Integer> source = Flowable.create(new FlowableOnSubscribe<Integer>() {

            @Override
            public void subscribe(FlowableEmitter<Integer> s) throws Exception {
                calls[0]++;
                s.onNext(1);
                s.onComplete();
            }
        }, BackpressureStrategy.MISSING);
        Flowable.concatArrayDelayError(source, source).firstElement().test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @Test
    public void noSubsequentSubscriptionIterable() {
        final int[] calls = { 0 };
        Flowable<Integer> source = Flowable.create(new FlowableOnSubscribe<Integer>() {

            @Override
            public void subscribe(FlowableEmitter<Integer> s) throws Exception {
                calls[0]++;
                s.onNext(1);
                s.onComplete();
            }
        }, BackpressureStrategy.MISSING);
        Flowable.concat(Arrays.asList(source, source)).firstElement().test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @Test
    public void noSubsequentSubscriptionDelayErrorIterable() {
        final int[] calls = { 0 };
        Flowable<Integer> source = Flowable.create(new FlowableOnSubscribe<Integer>() {

            @Override
            public void subscribe(FlowableEmitter<Integer> s) throws Exception {
                calls[0]++;
                s.onNext(1);
                s.onComplete();
            }
        }, BackpressureStrategy.MISSING);
        Flowable.concatDelayError(Arrays.asList(source, source)).firstElement().test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @Test
    public void noCancelPreviousArray() {
        final AtomicInteger counter = new AtomicInteger();
        Flowable<Integer> source = Flowable.just(1).doOnCancel(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        Flowable.concatArray(source, source, source, source, source).test().assertResult(1, 1, 1, 1, 1);
        assertEquals(0, counter.get());
    }

    @Test
    public void noCancelPreviousIterable() {
        final AtomicInteger counter = new AtomicInteger();
        Flowable<Integer> source = Flowable.just(1).doOnCancel(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        Flowable.concat(Arrays.asList(source, source, source, source, source)).test().assertResult(1, 1, 1, 1, 1);
        assertEquals(0, counter.get());
    }

    @Test
    public void arrayDelayErrorMultipleErrors() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        Flowable.concatArrayDelayError(Flowable.error(new IOException()), Flowable.error(new TestException())).subscribe(ts);
        ts.assertFailure(CompositeException.class);
        TestHelper.assertCompositeExceptions(ts, IOException.class, TestException.class);
    }

    @Test
    public void arrayDelayErrorMultipleNullErrors() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        Flowable.concatArrayDelayError(null, null).subscribe(ts);
        ts.assertFailure(CompositeException.class);
        TestHelper.assertCompositeExceptions(ts, NullPointerException.class, NullPointerException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableConcatTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concat);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWithList() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatWithList);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableOfObservables() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatObservableOfObservables);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleAsyncConcat() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.simpleAsyncConcat);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nestedAsyncConcatLoop() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.nestedAsyncConcatLoop);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nestedAsyncConcat() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.nestedAsyncConcat);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockedObservableOfObservables() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.blockedObservableOfObservables);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatConcurrentWithInfinity() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatConcurrentWithInfinity);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatNonBlockingObservables() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatNonBlockingObservables);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatUnsubscribe() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatUnsubscribe);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatUnsubscribeConcurrent() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatUnsubscribeConcurrent);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleObservers() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.multipleObservers);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatVeryLongObservableOfObservables() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatVeryLongObservableOfObservables);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatVeryLongObservableOfObservablesTakeHalf() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatVeryLongObservableOfObservablesTakeHalf);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatOuterBackpressure() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatOuterBackpressure);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerBackpressureWithAlignedBoundaries() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.innerBackpressureWithAlignedBoundaries);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerBackpressureWithoutAlignedBoundaries() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.innerBackpressureWithoutAlignedBoundaries);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWithNonCompliantSourceDoubleOnComplete() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatWithNonCompliantSourceDoubleOnComplete);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue2890NoStackoverflow() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.issue2890NoStackoverflow);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestOverflowDoesNotStallStream() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.requestOverflowDoesNotStallStream);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapRangeAsyncLoopIssue2876() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapRangeAsyncLoopIssue2876);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_arrayDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.arrayDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarAndRangeBackpressured() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.scalarAndRangeBackpressured);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarAndEmptyBackpressured() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.scalarAndEmptyBackpressured);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeAndEmptyBackpressured() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.rangeAndEmptyBackpressured);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyAndScalarBackpressured() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.emptyAndScalarBackpressured);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArray() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatArray);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapJustJust() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapJustJust);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapJustRange() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapJustRange);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapDelayErrorJustJust() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapDelayErrorJustJust);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapDelayErrorJustRange() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapDelayErrorJustRange);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithArray() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.startWithArray);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_veryLongTake() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.veryLongTake);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat3() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concat3);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat4() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concat4);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArrayDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatArrayDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArrayDelayErrorWithError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatArrayDelayErrorWithError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatIterableDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableDelayErrorWithError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatIterableDelayErrorWithError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatObservableDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableDelayErrorWithError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatObservableDelayErrorWithError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableDelayErrorBoundary() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatObservableDelayErrorBoundary);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableDelayErrorTillEnd() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatObservableDelayErrorTillEnd);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapDelayErrorWithError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapDelayErrorWithError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapIterableBufferSize() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapIterableBufferSize);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyArray() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.emptyArray);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleElementArray() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.singleElementArray);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapDelayErrorEmptySource() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapDelayErrorEmptySource);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapDelayErrorJustSource() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapDelayErrorJustSource);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArrayEmpty() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatArrayEmpty);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArraySingleElement() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatArraySingleElement);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapErrorEmptySource() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapErrorEmptySource);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapJustSource() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapJustSource);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapJustSourceDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapJustSourceDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapScalarBackpressured() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapScalarBackpressured);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapScalarBackpressuredDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapScalarBackpressuredDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapEmpty() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapEmpty);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapEmptyDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapEmptyDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreBackpressure() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.ignoreBackpressure);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.doubleOnSubscribe);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_immediateInnerNextOuterError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.immediateInnerNextOuterError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_immediateInnerNextOuterError2() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.immediateInnerNextOuterError2);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapInnerError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapInnerError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapInnerErrorDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.concatMapInnerErrorDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.badSource);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badInnerSource() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.badInnerSource);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badInnerSourceDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.badInnerSourceDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.badSourceDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedCrash() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.fusedCrash);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedCrashDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.fusedCrashDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_callableCrash() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.callableCrash);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_callableCrashDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.callableCrashDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.dispose);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notVeryEnd() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.notVeryEnd);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.error);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrows() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.mapperThrows);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubsequentSubscription() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.noSubsequentSubscription);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubsequentSubscriptionDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.noSubsequentSubscriptionDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubsequentSubscriptionIterable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.noSubsequentSubscriptionIterable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubsequentSubscriptionDelayErrorIterable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.noSubsequentSubscriptionDelayErrorIterable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPreviousArray() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.noCancelPreviousArray);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPreviousIterable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.noCancelPreviousIterable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_arrayDelayErrorMultipleErrors() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.arrayDelayErrorMultipleErrors);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_arrayDelayErrorMultipleNullErrors() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.arrayDelayErrorMultipleNullErrors);
        }

        public void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> payload) throws java.lang.Throwable {
            this.instance = new FlowableConcatTest();
            payload.accept(this.instance);
        }

        public static class _Payloads {

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concat;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatWithList;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatObservableOfObservables;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> simpleAsyncConcat;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> nestedAsyncConcatLoop;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> nestedAsyncConcat;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> blockedObservableOfObservables;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatConcurrentWithInfinity;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatNonBlockingObservables;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatUnsubscribe;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatUnsubscribeConcurrent;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> multipleObservers;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatVeryLongObservableOfObservables;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatVeryLongObservableOfObservablesTakeHalf;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatOuterBackpressure;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> innerBackpressureWithAlignedBoundaries;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> innerBackpressureWithoutAlignedBoundaries;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatWithNonCompliantSourceDoubleOnComplete;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> issue2890NoStackoverflow;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> requestOverflowDoesNotStallStream;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapRangeAsyncLoopIssue2876;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> arrayDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> scalarAndRangeBackpressured;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> scalarAndEmptyBackpressured;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> rangeAndEmptyBackpressured;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> emptyAndScalarBackpressured;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatArray;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapJustJust;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapJustRange;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapDelayErrorJustJust;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapDelayErrorJustRange;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> startWithArray;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> veryLongTake;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concat3;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concat4;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatArrayDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatArrayDelayErrorWithError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatIterableDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatIterableDelayErrorWithError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatObservableDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatObservableDelayErrorWithError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatObservableDelayErrorBoundary;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatObservableDelayErrorTillEnd;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapDelayErrorWithError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapIterableBufferSize;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> emptyArray;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> singleElementArray;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapDelayErrorEmptySource;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapDelayErrorJustSource;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatArrayEmpty;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatArraySingleElement;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapErrorEmptySource;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapJustSource;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapJustSourceDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapScalarBackpressured;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapScalarBackpressuredDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapEmpty;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapEmptyDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> ignoreBackpressure;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> doubleOnSubscribe;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> immediateInnerNextOuterError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> immediateInnerNextOuterError2;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapInnerError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> concatMapInnerErrorDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> badSource;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> badInnerSource;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> badInnerSourceDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> badSourceDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> fusedCrash;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> fusedCrashDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> callableCrash;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> callableCrashDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> dispose;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> notVeryEnd;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> error;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> mapperThrows;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> noSubsequentSubscription;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> noSubsequentSubscriptionDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> noSubsequentSubscriptionIterable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> noSubsequentSubscriptionDelayErrorIterable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> noCancelPreviousArray;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> noCancelPreviousIterable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> arrayDelayErrorMultipleErrors;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTest> arrayDelayErrorMultipleNullErrors;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.concat = FlowableConcatTest::concat;
            this.payloads.concatWithList = FlowableConcatTest::concatWithList;
            this.payloads.concatObservableOfObservables = FlowableConcatTest::concatObservableOfObservables;
            this.payloads.simpleAsyncConcat = FlowableConcatTest::simpleAsyncConcat;
            this.payloads.nestedAsyncConcatLoop = FlowableConcatTest::nestedAsyncConcatLoop;
            this.payloads.nestedAsyncConcat = FlowableConcatTest::nestedAsyncConcat;
            this.payloads.blockedObservableOfObservables = FlowableConcatTest::blockedObservableOfObservables;
            this.payloads.concatConcurrentWithInfinity = FlowableConcatTest::concatConcurrentWithInfinity;
            this.payloads.concatNonBlockingObservables = FlowableConcatTest::concatNonBlockingObservables;
            this.payloads.concatUnsubscribe = FlowableConcatTest::concatUnsubscribe;
            this.payloads.concatUnsubscribeConcurrent = FlowableConcatTest::concatUnsubscribeConcurrent;
            this.payloads.multipleObservers = FlowableConcatTest::multipleObservers;
            this.payloads.concatVeryLongObservableOfObservables = FlowableConcatTest::concatVeryLongObservableOfObservables;
            this.payloads.concatVeryLongObservableOfObservablesTakeHalf = FlowableConcatTest::concatVeryLongObservableOfObservablesTakeHalf;
            this.payloads.concatOuterBackpressure = FlowableConcatTest::concatOuterBackpressure;
            this.payloads.innerBackpressureWithAlignedBoundaries = FlowableConcatTest::innerBackpressureWithAlignedBoundaries;
            this.payloads.innerBackpressureWithoutAlignedBoundaries = FlowableConcatTest::innerBackpressureWithoutAlignedBoundaries;
            this.payloads.concatWithNonCompliantSourceDoubleOnComplete = FlowableConcatTest::concatWithNonCompliantSourceDoubleOnComplete;
            this.payloads.issue2890NoStackoverflow = FlowableConcatTest::issue2890NoStackoverflow;
            this.payloads.requestOverflowDoesNotStallStream = FlowableConcatTest::requestOverflowDoesNotStallStream;
            this.payloads.concatMapRangeAsyncLoopIssue2876 = FlowableConcatTest::concatMapRangeAsyncLoopIssue2876;
            this.payloads.arrayDelayError = FlowableConcatTest::arrayDelayError;
            this.payloads.scalarAndRangeBackpressured = FlowableConcatTest::scalarAndRangeBackpressured;
            this.payloads.scalarAndEmptyBackpressured = FlowableConcatTest::scalarAndEmptyBackpressured;
            this.payloads.rangeAndEmptyBackpressured = FlowableConcatTest::rangeAndEmptyBackpressured;
            this.payloads.emptyAndScalarBackpressured = FlowableConcatTest::emptyAndScalarBackpressured;
            this.payloads.concatArray = FlowableConcatTest::concatArray;
            this.payloads.concatMapJustJust = FlowableConcatTest::concatMapJustJust;
            this.payloads.concatMapJustRange = FlowableConcatTest::concatMapJustRange;
            this.payloads.concatMapDelayErrorJustJust = FlowableConcatTest::concatMapDelayErrorJustJust;
            this.payloads.concatMapDelayErrorJustRange = FlowableConcatTest::concatMapDelayErrorJustRange;
            this.payloads.startWithArray = FlowableConcatTest::startWithArray;
            this.payloads.veryLongTake = FlowableConcatTest::veryLongTake;
            this.payloads.concat3 = FlowableConcatTest::concat3;
            this.payloads.concat4 = FlowableConcatTest::concat4;
            this.payloads.concatArrayDelayError = FlowableConcatTest::concatArrayDelayError;
            this.payloads.concatArrayDelayErrorWithError = FlowableConcatTest::concatArrayDelayErrorWithError;
            this.payloads.concatIterableDelayError = FlowableConcatTest::concatIterableDelayError;
            this.payloads.concatIterableDelayErrorWithError = FlowableConcatTest::concatIterableDelayErrorWithError;
            this.payloads.concatObservableDelayError = FlowableConcatTest::concatObservableDelayError;
            this.payloads.concatObservableDelayErrorWithError = FlowableConcatTest::concatObservableDelayErrorWithError;
            this.payloads.concatObservableDelayErrorBoundary = FlowableConcatTest::concatObservableDelayErrorBoundary;
            this.payloads.concatObservableDelayErrorTillEnd = FlowableConcatTest::concatObservableDelayErrorTillEnd;
            this.payloads.concatMapDelayError = FlowableConcatTest::concatMapDelayError;
            this.payloads.concatMapDelayErrorWithError = FlowableConcatTest::concatMapDelayErrorWithError;
            this.payloads.concatMapIterableBufferSize = FlowableConcatTest::concatMapIterableBufferSize;
            this.payloads.emptyArray = FlowableConcatTest::emptyArray;
            this.payloads.singleElementArray = FlowableConcatTest::singleElementArray;
            this.payloads.concatMapDelayErrorEmptySource = FlowableConcatTest::concatMapDelayErrorEmptySource;
            this.payloads.concatMapDelayErrorJustSource = FlowableConcatTest::concatMapDelayErrorJustSource;
            this.payloads.concatArrayEmpty = FlowableConcatTest::concatArrayEmpty;
            this.payloads.concatArraySingleElement = FlowableConcatTest::concatArraySingleElement;
            this.payloads.concatMapErrorEmptySource = FlowableConcatTest::concatMapErrorEmptySource;
            this.payloads.concatMapJustSource = FlowableConcatTest::concatMapJustSource;
            this.payloads.concatMapJustSourceDelayError = FlowableConcatTest::concatMapJustSourceDelayError;
            this.payloads.concatMapScalarBackpressured = FlowableConcatTest::concatMapScalarBackpressured;
            this.payloads.concatMapScalarBackpressuredDelayError = FlowableConcatTest::concatMapScalarBackpressuredDelayError;
            this.payloads.concatMapEmpty = FlowableConcatTest::concatMapEmpty;
            this.payloads.concatMapEmptyDelayError = FlowableConcatTest::concatMapEmptyDelayError;
            this.payloads.ignoreBackpressure = FlowableConcatTest::ignoreBackpressure;
            this.payloads.doubleOnSubscribe = FlowableConcatTest::doubleOnSubscribe;
            this.payloads.immediateInnerNextOuterError = FlowableConcatTest::immediateInnerNextOuterError;
            this.payloads.immediateInnerNextOuterError2 = FlowableConcatTest::immediateInnerNextOuterError2;
            this.payloads.concatMapInnerError = FlowableConcatTest::concatMapInnerError;
            this.payloads.concatMapInnerErrorDelayError = FlowableConcatTest::concatMapInnerErrorDelayError;
            this.payloads.badSource = FlowableConcatTest::badSource;
            this.payloads.badInnerSource = FlowableConcatTest::badInnerSource;
            this.payloads.badInnerSourceDelayError = FlowableConcatTest::badInnerSourceDelayError;
            this.payloads.badSourceDelayError = FlowableConcatTest::badSourceDelayError;
            this.payloads.fusedCrash = FlowableConcatTest::fusedCrash;
            this.payloads.fusedCrashDelayError = FlowableConcatTest::fusedCrashDelayError;
            this.payloads.callableCrash = FlowableConcatTest::callableCrash;
            this.payloads.callableCrashDelayError = FlowableConcatTest::callableCrashDelayError;
            this.payloads.dispose = FlowableConcatTest::dispose;
            this.payloads.notVeryEnd = FlowableConcatTest::notVeryEnd;
            this.payloads.error = FlowableConcatTest::error;
            this.payloads.mapperThrows = FlowableConcatTest::mapperThrows;
            this.payloads.noSubsequentSubscription = FlowableConcatTest::noSubsequentSubscription;
            this.payloads.noSubsequentSubscriptionDelayError = FlowableConcatTest::noSubsequentSubscriptionDelayError;
            this.payloads.noSubsequentSubscriptionIterable = FlowableConcatTest::noSubsequentSubscriptionIterable;
            this.payloads.noSubsequentSubscriptionDelayErrorIterable = FlowableConcatTest::noSubsequentSubscriptionDelayErrorIterable;
            this.payloads.noCancelPreviousArray = FlowableConcatTest::noCancelPreviousArray;
            this.payloads.noCancelPreviousIterable = FlowableConcatTest::noCancelPreviousIterable;
            this.payloads.arrayDelayErrorMultipleErrors = FlowableConcatTest::arrayDelayErrorMultipleErrors;
            this.payloads.arrayDelayErrorMultipleNullErrors = FlowableConcatTest::arrayDelayErrorMultipleNullErrors;
        }
    }
}
