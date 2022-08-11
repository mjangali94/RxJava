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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.observable.ObservableRefCount.RefConnection;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.observables.ConnectableObservable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableRefCountTest extends RxJavaTest {

    @Test
    public void refCountAsync() throws InterruptedException {
        // Flaky
        for (int i = 0; i < 10; i++) {
            try {
                refCountAsyncActual();
                return;
            } catch (AssertionError ex) {
                if (i == 9) {
                    throw ex;
                }
                Thread.sleep((int) (200 * (Math.random() * 10 + 1)));
            }
        }
    }

    /**
     * Tries to coordinate async counting but it is flaky due to the low 10s of milliseconds.
     */
    void refCountAsyncActual() {
        final AtomicInteger subscribeCount = new AtomicInteger();
        final AtomicInteger nextCount = new AtomicInteger();
        Observable<Long> r = Observable.interval(0, 25, TimeUnit.MILLISECONDS).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                subscribeCount.incrementAndGet();
            }
        }).doOnNext(new Consumer<Long>() {

            @Override
            public void accept(Long l) {
                nextCount.incrementAndGet();
            }
        }).publish().refCount();
        final AtomicInteger receivedCount = new AtomicInteger();
        Disposable d1 = r.subscribe(new Consumer<Long>() {

            @Override
            public void accept(Long l) {
                receivedCount.incrementAndGet();
            }
        });
        Disposable d2 = r.subscribe();
        // give time to emit
        try {
            Thread.sleep(260);
        } catch (InterruptedException e) {
        }
        // now unsubscribe
        // unsubscribe s2 first as we're counting in 1 and there can be a race between unsubscribe and one Observer getting a value but not the other
        d2.dispose();
        d1.dispose();
        System.out.println("onNext: " + nextCount.get());
        // should emit once for both subscribers
        assertEquals(nextCount.get(), receivedCount.get());
        // only 1 subscribe
        assertEquals(1, subscribeCount.get());
    }

    @Test
    public void refCountSynchronous() {
        final AtomicInteger subscribeCount = new AtomicInteger();
        final AtomicInteger nextCount = new AtomicInteger();
        Observable<Integer> r = Observable.just(1, 2, 3, 4, 5, 6, 7, 8, 9).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                subscribeCount.incrementAndGet();
            }
        }).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer l) {
                nextCount.incrementAndGet();
            }
        }).publish().refCount();
        final AtomicInteger receivedCount = new AtomicInteger();
        Disposable d1 = r.subscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer l) {
                receivedCount.incrementAndGet();
            }
        });
        Disposable d2 = r.subscribe();
        // give time to emit
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
        // now unsubscribe
        // unsubscribe s2 first as we're counting in 1 and there can be a race between unsubscribe and one Observer getting a value but not the other
        d2.dispose();
        d1.dispose();
        System.out.println("onNext Count: " + nextCount.get());
        // it will emit twice because it is synchronous
        assertEquals(nextCount.get(), receivedCount.get() * 2);
        // it will subscribe twice because it is synchronous
        assertEquals(2, subscribeCount.get());
    }

    @Test
    public void refCountSynchronousTake() {
        final AtomicInteger nextCount = new AtomicInteger();
        Observable<Integer> r = Observable.just(1, 2, 3, 4, 5, 6, 7, 8, 9).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer l) {
                System.out.println("onNext --------> " + l);
                nextCount.incrementAndGet();
            }
        }).take(4).publish().refCount();
        final AtomicInteger receivedCount = new AtomicInteger();
        r.subscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer l) {
                receivedCount.incrementAndGet();
            }
        });
        System.out.println("onNext: " + nextCount.get());
        assertEquals(4, receivedCount.get());
        assertEquals(4, receivedCount.get());
    }

    @Test
    public void repeat() {
        final AtomicInteger subscribeCount = new AtomicInteger();
        final AtomicInteger unsubscribeCount = new AtomicInteger();
        Observable<Long> r = Observable.interval(0, 1, TimeUnit.MILLISECONDS).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                System.out.println("******************************* Subscribe received");
                // when we are subscribed
                subscribeCount.incrementAndGet();
            }
        }).doOnDispose(new Action() {

            @Override
            public void run() {
                System.out.println("******************************* Unsubscribe received");
                // when we are unsubscribed
                unsubscribeCount.incrementAndGet();
            }
        }).publish().refCount();
        for (int i = 0; i < 10; i++) {
            TestObserver<Long> to1 = new TestObserver<>();
            TestObserver<Long> to2 = new TestObserver<>();
            r.subscribe(to1);
            r.subscribe(to2);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            to1.dispose();
            to2.dispose();
            to1.assertNoErrors();
            to2.assertNoErrors();
            assertTrue(to1.values().size() > 0);
            assertTrue(to2.values().size() > 0);
        }
        assertEquals(10, subscribeCount.get());
        assertEquals(10, unsubscribeCount.get());
    }

    @Test
    public void connectUnsubscribe() throws InterruptedException {
        final CountDownLatch unsubscribeLatch = new CountDownLatch(1);
        final CountDownLatch subscribeLatch = new CountDownLatch(1);
        Observable<Long> o = synchronousInterval().doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                System.out.println("******************************* Subscribe received");
                // when we are subscribed
                subscribeLatch.countDown();
            }
        }).doOnDispose(new Action() {

            @Override
            public void run() {
                System.out.println("******************************* Unsubscribe received");
                // when we are unsubscribed
                unsubscribeLatch.countDown();
            }
        });
        TestObserverEx<Long> observer = new TestObserverEx<>();
        o.publish().refCount().subscribeOn(Schedulers.newThread()).subscribe(observer);
        System.out.println("send unsubscribe");
        // wait until connected
        subscribeLatch.await();
        // now unsubscribe
        observer.dispose();
        System.out.println("DONE sending unsubscribe ... now waiting");
        if (!unsubscribeLatch.await(3000, TimeUnit.MILLISECONDS)) {
            System.out.println("Errors: " + observer.errors());
            if (observer.errors().size() > 0) {
                observer.errors().get(0).printStackTrace();
            }
            fail("timed out waiting for unsubscribe");
        }
        observer.assertNoErrors();
    }

    @Test
    public void connectUnsubscribeRaceConditionLoop() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            connectUnsubscribeRaceCondition();
        }
    }

    @Test
    public void connectUnsubscribeRaceCondition() throws InterruptedException {
        final AtomicInteger subUnsubCount = new AtomicInteger();
        Observable<Long> o = synchronousInterval().doOnDispose(new Action() {

            @Override
            public void run() {
                System.out.println("******************************* Unsubscribe received");
                // when we are unsubscribed
                subUnsubCount.decrementAndGet();
            }
        }).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                System.out.println("******************************* SUBSCRIBE received");
                subUnsubCount.incrementAndGet();
            }
        });
        TestObserverEx<Long> observer = new TestObserverEx<>();
        o.publish().refCount().subscribeOn(Schedulers.computation()).subscribe(observer);
        System.out.println("send unsubscribe");
        // now immediately unsubscribe while subscribeOn is racing to subscribe
        observer.dispose();
        // this generally will mean it won't even subscribe as it is already unsubscribed by the time connect() gets scheduled
        // give time to the counter to update
        Thread.sleep(10);
        // make sure we wait a bit in case the counter is still nonzero
        int counter = 200;
        while (subUnsubCount.get() != 0 && counter-- != 0) {
            Thread.sleep(10);
        }
        // either we subscribed and then unsubscribed, or we didn't ever even subscribe
        assertEquals(0, subUnsubCount.get());
        System.out.println("DONE sending unsubscribe ... now waiting");
        System.out.println("Errors: " + observer.errors());
        if (observer.errors().size() > 0) {
            observer.errors().get(0).printStackTrace();
        }
        observer.assertNoErrors();
    }

    private Observable<Long> synchronousInterval() {
        return Observable.unsafeCreate(new ObservableSource<Long>() {

            @Override
            public void subscribe(Observer<? super Long> observer) {
                final AtomicBoolean cancel = new AtomicBoolean();
                observer.onSubscribe(Disposable.fromRunnable(new Runnable() {

                    @Override
                    public void run() {
                        cancel.set(true);
                    }
                }));
                for (; ; ) {
                    if (cancel.get()) {
                        break;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    observer.onNext(1L);
                }
            }
        });
    }

    @Test
    public void onlyFirstShouldSubscribeAndLastUnsubscribe() {
        final AtomicInteger subscriptionCount = new AtomicInteger();
        final AtomicInteger unsubscriptionCount = new AtomicInteger();
        Observable<Integer> o = Observable.unsafeCreate(new ObservableSource<Integer>() {

            @Override
            public void subscribe(Observer<? super Integer> observer) {
                subscriptionCount.incrementAndGet();
                observer.onSubscribe(Disposable.fromRunnable(new Runnable() {

                    @Override
                    public void run() {
                        unsubscriptionCount.incrementAndGet();
                    }
                }));
            }
        });
        Observable<Integer> refCounted = o.publish().refCount();
        Disposable first = refCounted.subscribe();
        assertEquals(1, subscriptionCount.get());
        Disposable second = refCounted.subscribe();
        assertEquals(1, subscriptionCount.get());
        first.dispose();
        assertEquals(0, unsubscriptionCount.get());
        second.dispose();
        assertEquals(1, unsubscriptionCount.get());
    }

    @Test
    public void refCount() {
        TestScheduler s = new TestScheduler();
        Observable<Long> interval = Observable.interval(100, TimeUnit.MILLISECONDS, s).publish().refCount();
        // subscribe list1
        final List<Long> list1 = new ArrayList<>();
        Disposable d1 = interval.subscribe(new Consumer<Long>() {

            @Override
            public void accept(Long t1) {
                list1.add(t1);
            }
        });
        s.advanceTimeBy(200, TimeUnit.MILLISECONDS);
        assertEquals(2, list1.size());
        assertEquals(0L, list1.get(0).longValue());
        assertEquals(1L, list1.get(1).longValue());
        // subscribe list2
        final List<Long> list2 = new ArrayList<>();
        Disposable d2 = interval.subscribe(new Consumer<Long>() {

            @Override
            public void accept(Long t1) {
                list2.add(t1);
            }
        });
        s.advanceTimeBy(300, TimeUnit.MILLISECONDS);
        // list 1 should have 5 items
        assertEquals(5, list1.size());
        assertEquals(2L, list1.get(2).longValue());
        assertEquals(3L, list1.get(3).longValue());
        assertEquals(4L, list1.get(4).longValue());
        // list 2 should only have 3 items
        assertEquals(3, list2.size());
        assertEquals(2L, list2.get(0).longValue());
        assertEquals(3L, list2.get(1).longValue());
        assertEquals(4L, list2.get(2).longValue());
        // unsubscribe list1
        d1.dispose();
        // advance further
        s.advanceTimeBy(300, TimeUnit.MILLISECONDS);
        // list 1 should still have 5 items
        assertEquals(5, list1.size());
        // list 2 should have 6 items
        assertEquals(6, list2.size());
        assertEquals(5L, list2.get(3).longValue());
        assertEquals(6L, list2.get(4).longValue());
        assertEquals(7L, list2.get(5).longValue());
        // unsubscribe list2
        d2.dispose();
        // advance further
        s.advanceTimeBy(1000, TimeUnit.MILLISECONDS);
        // subscribing a new one should start over because the source should have been unsubscribed
        // subscribe list3
        final List<Long> list3 = new ArrayList<>();
        interval.subscribe(new Consumer<Long>() {

            @Override
            public void accept(Long t1) {
                list3.add(t1);
            }
        });
        s.advanceTimeBy(200, TimeUnit.MILLISECONDS);
        assertEquals(2, list3.size());
        assertEquals(0L, list3.get(0).longValue());
        assertEquals(1L, list3.get(1).longValue());
    }

    @Test
    public void alreadyUnsubscribedClient() {
        Observer<Integer> done = DisposingObserver.INSTANCE;
        Observer<Integer> o = TestHelper.mockObserver();
        Observable<Integer> result = Observable.just(1).publish().refCount();
        result.subscribe(done);
        result.subscribe(o);
        verify(o).onNext(1);
        verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void alreadyUnsubscribedInterleavesWithClient() {
        ReplaySubject<Integer> source = ReplaySubject.create();
        Observer<Integer> done = DisposingObserver.INSTANCE;
        Observer<Integer> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        Observable<Integer> result = source.publish().refCount();
        result.subscribe(o);
        source.onNext(1);
        result.subscribe(done);
        source.onNext(2);
        source.onComplete();
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void connectDisconnectConnectAndSubjectState() {
        Observable<Integer> o1 = Observable.just(10);
        Observable<Integer> o2 = Observable.just(20);
        Observable<Integer> combined = Observable.combineLatest(o1, o2, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).publish().refCount();
        TestObserverEx<Integer> to1 = new TestObserverEx<>();
        TestObserverEx<Integer> to2 = new TestObserverEx<>();
        combined.subscribe(to1);
        combined.subscribe(to2);
        to1.assertTerminated();
        to1.assertNoErrors();
        to1.assertValue(30);
        to2.assertTerminated();
        to2.assertNoErrors();
        to2.assertValue(30);
    }

    @Test
    public void upstreamErrorAllowsRetry() throws InterruptedException {
        final AtomicInteger intervalSubscribed = new AtomicInteger();
        Observable<String> interval = Observable.interval(200, TimeUnit.MILLISECONDS).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                System.out.println("Subscribing to interval " + intervalSubscribed.incrementAndGet());
            }
        }).flatMap(new Function<Long, Observable<String>>() {

            @Override
            public Observable<String> apply(Long t1) {
                return Observable.defer(new Supplier<Observable<String>>() {

                    @Override
                    public Observable<String> get() {
                        return Observable.<String>error(new Exception("Some exception"));
                    }
                });
            }
        }).onErrorResumeNext(new Function<Throwable, Observable<String>>() {

            @Override
            public Observable<String> apply(Throwable t1) {
                return Observable.<String>error(t1);
            }
        }).publish().refCount();
        interval.doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable t1) {
                System.out.println("Observer 1 onError: " + t1);
            }
        }).retry(5).subscribe(new Consumer<String>() {

            @Override
            public void accept(String t1) {
                System.out.println("Observer 1: " + t1);
            }
        });
        Thread.sleep(100);
        interval.doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable t1) {
                System.out.println("Observer 2 onError: " + t1);
            }
        }).retry(5).subscribe(new Consumer<String>() {

            @Override
            public void accept(String t1) {
                System.out.println("Observer 2: " + t1);
            }
        });
        Thread.sleep(1300);
        System.out.println(intervalSubscribed.get());
        assertEquals(6, intervalSubscribed.get());
    }

    private enum DisposingObserver implements Observer<Integer> {

        INSTANCE;

        @Override
        public void onSubscribe(Disposable d) {
            d.dispose();
        }

        @Override
        public void onNext(Integer t) {
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onComplete() {
        }
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Observable.just(1).publish().refCount());
    }

    @Test
    public void noOpConnect() {
        final int[] calls = { 0 };
        Observable<Integer> o = new ConnectableObservable<Integer>() {

            @Override
            public void connect(Consumer<? super Disposable> connection) {
                calls[0]++;
            }

            @Override
            public void reset() {
            // nothing to do in this test
            }

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.disposed());
            }
        }.refCount();
        o.test();
        o.test();
        assertEquals(1, calls[0]);
    }

    Observable<Object> source;

    @Test
    public void replayNoLeak() throws Exception {
        System.gc();
        Thread.sleep(100);
        long start = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        source = Observable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return new byte[100 * 1000 * 1000];
            }
        }).replay(1).refCount();
        source.subscribe();
        long after = TestHelper.awaitGC(GC_SLEEP_TIME, 20, start + 20 * 1000 * 1000);
        source = null;
        assertTrue(String.format("%,3d -> %,3d%n", start, after), start + 20 * 1000 * 1000 > after);
    }

    @Test
    public void replayNoLeak2() throws Exception {
        System.gc();
        Thread.sleep(100);
        long start = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        source = Observable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return new byte[100 * 1000 * 1000];
            }
        }).concatWith(Observable.never()).replay(1).refCount();
        Disposable d1 = source.subscribe();
        Disposable d2 = source.subscribe();
        d1.dispose();
        d2.dispose();
        d1 = null;
        d2 = null;
        long after = TestHelper.awaitGC(GC_SLEEP_TIME, 20, start + 20 * 1000 * 1000);
        source = null;
        assertTrue(String.format("%,3d -> %,3d%n", start, after), start + 20 * 1000 * 1000 > after);
    }

    static final class ExceptionData extends Exception {

        private static final long serialVersionUID = -6763898015338136119L;

        public final Object data;

        ExceptionData(Object data) {
            this.data = data;
        }
    }

    static final int GC_SLEEP_TIME = 250;

    @Test
    public void publishNoLeak() throws Exception {
        System.gc();
        Thread.sleep(GC_SLEEP_TIME);
        source = Observable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw new ExceptionData(new byte[100 * 1000 * 1000]);
            }
        }).publish().refCount();
        long start = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        source.subscribe(Functions.emptyConsumer(), Functions.emptyConsumer());
        long after = TestHelper.awaitGC(GC_SLEEP_TIME, 20, start + 20 * 1000 * 1000);
        source = null;
        assertTrue(String.format("%,3d -> %,3d%n", start, after), start + 20 * 1000 * 1000 > after);
    }

    @Test
    public void publishNoLeak2() throws Exception {
        System.gc();
        Thread.sleep(GC_SLEEP_TIME);
        long start = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        source = Observable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return new byte[100 * 1000 * 1000];
            }
        }).concatWith(Observable.never()).publish().refCount();
        Disposable d1 = source.test();
        Disposable d2 = source.test();
        d1.dispose();
        d2.dispose();
        d1 = null;
        d2 = null;
        long after = TestHelper.awaitGC(GC_SLEEP_TIME, 20, start + 20 * 1000 * 1000);
        source = null;
        assertTrue(String.format("%,3d -> %,3d%n", start, after), start + 20 * 1000 * 1000 > after);
    }

    @Test
    public void replayIsUnsubscribed() {
        ConnectableObservable<Integer> co = Observable.just(1).concatWith(Observable.<Integer>never()).replay();
        if (co instanceof Disposable) {
            assertTrue(((Disposable) co).isDisposed());
            Disposable connection = co.connect();
            assertFalse(((Disposable) co).isDisposed());
            connection.dispose();
            assertTrue(((Disposable) co).isDisposed());
        }
    }

    static final class BadObservableSubscribe extends ConnectableObservable<Object> {

        @Override
        public void connect(Consumer<? super Disposable> connection) {
            try {
                connection.accept(Disposable.empty());
            } catch (Throwable ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }

        @Override
        public void reset() {
        // nothing to do in this test
        }

        @Override
        protected void subscribeActual(Observer<? super Object> observer) {
            throw new TestException("subscribeActual");
        }
    }

    static final class BadObservableDispose extends ConnectableObservable<Object> {

        @Override
        public void reset() {
            throw new TestException("dispose");
        }

        @Override
        public void connect(Consumer<? super Disposable> connection) {
            try {
                connection.accept(Disposable.empty());
            } catch (Throwable ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }

        @Override
        protected void subscribeActual(Observer<? super Object> observer) {
            observer.onSubscribe(Disposable.empty());
        }
    }

    static final class BadObservableConnect extends ConnectableObservable<Object> {

        @Override
        public void connect(Consumer<? super Disposable> connection) {
            throw new TestException("connect");
        }

        @Override
        public void reset() {
        // nothing to do in this test
        }

        @Override
        protected void subscribeActual(Observer<? super Object> observer) {
            observer.onSubscribe(Disposable.empty());
        }
    }

    @Test
    @SuppressUndeliverable
    public void badSourceSubscribe() {
        BadObservableSubscribe bo = new BadObservableSubscribe();
        try {
            bo.refCount().test();
            fail("Should have thrown");
        } catch (NullPointerException ex) {
            assertTrue(ex.getCause() instanceof TestException);
        }
    }

    @Test
    public void badSourceDispose() {
        BadObservableDispose bo = new BadObservableDispose();
        try {
            bo.refCount().test().dispose();
            fail("Should have thrown");
        } catch (TestException expected) {
        }
    }

    @Test
    @SuppressUndeliverable
    public void badSourceConnect() {
        BadObservableConnect bo = new BadObservableConnect();
        try {
            bo.refCount().test();
            fail("Should have thrown");
        } catch (NullPointerException ex) {
            assertTrue(ex.getCause() instanceof TestException);
        }
    }

    static final class BadObservableSubscribe2 extends ConnectableObservable<Object> {

        int count;

        @Override
        public void connect(Consumer<? super Disposable> connection) {
            try {
                connection.accept(Disposable.empty());
            } catch (Throwable ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }

        @Override
        public void reset() {
        // nothing to do in this test
        }

        @Override
        protected void subscribeActual(Observer<? super Object> observer) {
            if (++count == 1) {
                observer.onSubscribe(Disposable.empty());
            } else {
                throw new TestException("subscribeActual");
            }
        }
    }

    @Test
    @SuppressUndeliverable
    public void badSourceSubscribe2() {
        BadObservableSubscribe2 bo = new BadObservableSubscribe2();
        Observable<Object> o = bo.refCount();
        o.test();
        try {
            o.test();
            fail("Should have thrown");
        } catch (NullPointerException ex) {
            assertTrue(ex.getCause() instanceof TestException);
        }
    }

    static final class BadObservableConnect2 extends ConnectableObservable<Object> {

        @Override
        public void connect(Consumer<? super Disposable> connection) {
            try {
                connection.accept(Disposable.empty());
            } catch (Throwable ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }

        @Override
        public void reset() {
            throw new TestException("dispose");
        }

        @Override
        protected void subscribeActual(Observer<? super Object> observer) {
            observer.onSubscribe(Disposable.empty());
            observer.onComplete();
        }
    }

    @Test
    @SuppressUndeliverable
    public void badSourceCompleteDisconnect() {
        BadObservableConnect2 bo = new BadObservableConnect2();
        try {
            bo.refCount().test();
            fail("Should have thrown");
        } catch (NullPointerException ex) {
            assertTrue(ex.getCause() instanceof TestException);
        }
    }

    @Test
    public void blockingSourceAsnycCancel() throws Exception {
        BehaviorSubject<Integer> bs = BehaviorSubject.createDefault(1);
        Observable<Integer> o = bs.replay(1).refCount();
        o.subscribe();
        final AtomicBoolean interrupted = new AtomicBoolean();
        o.switchMap(new Function<Integer, ObservableSource<? extends Object>>() {

            @Override
            public ObservableSource<? extends Object> apply(Integer v) throws Exception {
                return Observable.create(new ObservableOnSubscribe<Object>() {

                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                        while (!emitter.isDisposed()) {
                            Thread.sleep(100);
                        }
                        interrupted.set(true);
                    }
                });
            }
        }).take(500, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
        assertTrue(interrupted.get());
    }

    @Test
    public void byCount() {
        final int[] subscriptions = { 0 };
        Observable<Integer> source = Observable.range(1, 5).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                subscriptions[0]++;
            }
        }).publish().refCount(2);
        for (int i = 0; i < 3; i++) {
            TestObserver<Integer> to1 = source.test();
            to1.withTag("to1 " + i);
            to1.assertEmpty();
            TestObserver<Integer> to2 = source.test();
            to2.withTag("to2 " + i);
            to1.assertResult(1, 2, 3, 4, 5);
            to2.assertResult(1, 2, 3, 4, 5);
        }
        assertEquals(3, subscriptions[0]);
    }

    @Test
    public void resubscribeBeforeTimeout() throws Exception {
        final int[] subscriptions = { 0 };
        PublishSubject<Integer> ps = PublishSubject.create();
        Observable<Integer> source = ps.doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                subscriptions[0]++;
            }
        }).publish().refCount(500, TimeUnit.MILLISECONDS);
        TestObserver<Integer> to1 = source.test();
        assertEquals(1, subscriptions[0]);
        to1.dispose();
        Thread.sleep(100);
        to1 = source.test();
        assertEquals(1, subscriptions[0]);
        Thread.sleep(500);
        assertEquals(1, subscriptions[0]);
        ps.onNext(1);
        ps.onNext(2);
        ps.onNext(3);
        ps.onNext(4);
        ps.onNext(5);
        ps.onComplete();
        to1.assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void letitTimeout() throws Exception {
        final int[] subscriptions = { 0 };
        PublishSubject<Integer> ps = PublishSubject.create();
        Observable<Integer> source = ps.doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                subscriptions[0]++;
            }
        }).publish().refCount(1, 100, TimeUnit.MILLISECONDS);
        TestObserver<Integer> to1 = source.test();
        assertEquals(1, subscriptions[0]);
        to1.dispose();
        assertTrue(ps.hasObservers());
        Thread.sleep(200);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void error() {
        Observable.<Integer>error(new IOException()).publish().refCount(500, TimeUnit.MILLISECONDS).test().assertFailure(IOException.class);
    }

    @Test
    public void comeAndGo() {
        PublishSubject<Integer> ps = PublishSubject.create();
        Observable<Integer> source = ps.publish().refCount(1);
        TestObserver<Integer> to1 = source.test();
        assertTrue(ps.hasObservers());
        for (int i = 0; i < 3; i++) {
            TestObserver<Integer> to2 = source.test();
            to1.dispose();
            to1 = to2;
        }
        to1.dispose();
        assertFalse(ps.hasObservers());
    }

    @Test
    public void unsubscribeSubscribeRace() {
        for (int i = 0; i < 1000; i++) {
            final Observable<Integer> source = Observable.range(1, 5).replay().refCount(1);
            final TestObserver<Integer> to1 = source.test();
            final TestObserver<Integer> to2 = new TestObserver<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to1.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    source.subscribe(to2);
                }
            };
            TestHelper.race(r1, r2, Schedulers.single());
            to2.withTag("Round: " + i).assertResult(1, 2, 3, 4, 5);
        }
    }

    static final class BadObservableDoubleOnX extends ConnectableObservable<Object> implements Disposable {

        @Override
        public void connect(Consumer<? super Disposable> connection) {
            try {
                connection.accept(Disposable.empty());
            } catch (Throwable ex) {
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }

        @Override
        public void reset() {
        // nothing to do in this test
        }

        @Override
        protected void subscribeActual(Observer<? super Object> observer) {
            observer.onSubscribe(Disposable.empty());
            observer.onSubscribe(Disposable.empty());
            observer.onComplete();
            observer.onComplete();
            observer.onError(new TestException());
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isDisposed() {
            return false;
        }
    }

    @Test
    public void doubleOnX() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new BadObservableDoubleOnX().refCount().test().assertResult();
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
            TestHelper.assertUndeliverable(errors, 1, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doubleOnXCount() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new BadObservableDoubleOnX().refCount(1).test().assertResult();
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
            TestHelper.assertUndeliverable(errors, 1, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doubleOnXTime() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new BadObservableDoubleOnX().refCount(5, TimeUnit.SECONDS, Schedulers.single()).test().assertResult();
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
            TestHelper.assertUndeliverable(errors, 1, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancelTerminateStateExclusion() {
        ObservableRefCount<Object> o = (ObservableRefCount<Object>) PublishSubject.create().publish().refCount();
        o.cancel(new RefConnection(o));
        RefConnection rc = new RefConnection(o);
        o.connection = null;
        rc.subscriberCount = 0;
        o.timeout(rc);
        rc.subscriberCount = 1;
        o.timeout(rc);
        o.connection = rc;
        o.timeout(rc);
        rc.subscriberCount = 0;
        o.timeout(rc);
        // -------------------
        rc.subscriberCount = 2;
        rc.connected = false;
        o.connection = rc;
        o.cancel(rc);
        rc.subscriberCount = 1;
        rc.connected = false;
        o.connection = rc;
        o.cancel(rc);
        rc.subscriberCount = 2;
        rc.connected = true;
        o.connection = rc;
        o.cancel(rc);
        rc.subscriberCount = 1;
        rc.connected = true;
        o.connection = rc;
        rc.lazySet(null);
        o.cancel(rc);
        o.connection = rc;
        o.cancel(new RefConnection(o));
    }

    @Test
    public void replayRefCountShallBeThreadSafe() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            Observable<Integer> observable = Observable.just(1).replay(1).refCount();
            TestObserver<Integer> observer1 = observable.subscribeOn(Schedulers.io()).test();
            TestObserver<Integer> observer2 = observable.subscribeOn(Schedulers.io()).test();
            observer1.withTag("" + i).awaitDone(5, TimeUnit.SECONDS).assertResult(1);
            observer2.withTag("" + i).awaitDone(5, TimeUnit.SECONDS).assertResult(1);
        }
    }

    static final class TestConnectableObservable<T> extends ConnectableObservable<T> {

        volatile boolean reset;

        @Override
        public void reset() {
            reset = true;
        }

        @Override
        public void connect(Consumer<? super Disposable> connection) {
        // not relevant
        }

        @Override
        protected void subscribeActual(Observer<? super T> observer) {
        // not relevant
        }
    }

    @Test
    public void timeoutResetsSource() {
        TestConnectableObservable<Object> tco = new TestConnectableObservable<>();
        ObservableRefCount<Object> o = (ObservableRefCount<Object>) tco.refCount();
        RefConnection rc = new RefConnection(o);
        rc.set(Disposable.empty());
        o.connection = rc;
        o.timeout(rc);
        assertTrue(tco.reset);
    }

    @Test
    public void disconnectBeforeConnect() {
        BehaviorSubject<Integer> subject = BehaviorSubject.create();
        Observable<Integer> observable = subject.replay(1).refCount();
        observable.takeUntil(Observable.just(1)).test();
        subject.onNext(2);
        observable.take(1).test().assertResult(2);
    }

    @Test
    public void publishRefCountShallBeThreadSafe() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            Observable<Integer> observable = Observable.just(1).publish().refCount();
            TestObserver<Integer> observer1 = observable.subscribeOn(Schedulers.io()).test();
            TestObserver<Integer> observer2 = observable.subscribeOn(Schedulers.io()).test();
            observer1.withTag("observer1 " + i).awaitDone(5, TimeUnit.SECONDS).assertNoErrors().assertComplete();
            observer2.withTag("observer2 " + i).awaitDone(5, TimeUnit.SECONDS).assertNoErrors().assertComplete();
        }
    }

    @Test
    public void upstreamTerminationTriggersAnotherCancel() throws Exception {
        ReplaySubject<Integer> rs = ReplaySubject.create();
        rs.onNext(1);
        rs.onComplete();
        Observable<Integer> shared = rs.share();
        shared.buffer(shared.debounce(5, TimeUnit.SECONDS)).test().assertValueCount(2);
        shared.buffer(shared.debounce(5, TimeUnit.SECONDS)).test().assertValueCount(2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableRefCountTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_refCountAsync() throws java.lang.Throwable {
            this.payloads.refCountAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_refCountSynchronous() throws java.lang.Throwable {
            this.payloads.refCountSynchronous.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_refCountSynchronousTake() throws java.lang.Throwable {
            this.payloads.refCountSynchronousTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeat() throws java.lang.Throwable {
            this.payloads.repeat.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_connectUnsubscribe() throws java.lang.Throwable {
            this.payloads.connectUnsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_connectUnsubscribeRaceConditionLoop() throws java.lang.Throwable {
            this.payloads.connectUnsubscribeRaceConditionLoop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_connectUnsubscribeRaceCondition() throws java.lang.Throwable {
            this.payloads.connectUnsubscribeRaceCondition.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onlyFirstShouldSubscribeAndLastUnsubscribe() throws java.lang.Throwable {
            this.payloads.onlyFirstShouldSubscribeAndLastUnsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_refCount() throws java.lang.Throwable {
            this.payloads.refCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_alreadyUnsubscribedClient() throws java.lang.Throwable {
            this.payloads.alreadyUnsubscribedClient.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_alreadyUnsubscribedInterleavesWithClient() throws java.lang.Throwable {
            this.payloads.alreadyUnsubscribedInterleavesWithClient.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_connectDisconnectConnectAndSubjectState() throws java.lang.Throwable {
            this.payloads.connectDisconnectConnectAndSubjectState.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamErrorAllowsRetry() throws java.lang.Throwable {
            this.payloads.upstreamErrorAllowsRetry.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noOpConnect() throws java.lang.Throwable {
            this.payloads.noOpConnect.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replayNoLeak() throws java.lang.Throwable {
            this.payloads.replayNoLeak.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replayNoLeak2() throws java.lang.Throwable {
            this.payloads.replayNoLeak2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishNoLeak() throws java.lang.Throwable {
            this.payloads.publishNoLeak.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishNoLeak2() throws java.lang.Throwable {
            this.payloads.publishNoLeak2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replayIsUnsubscribed() throws java.lang.Throwable {
            this.payloads.replayIsUnsubscribed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceSubscribe() throws java.lang.Throwable {
            this.payloads.badSourceSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceDispose() throws java.lang.Throwable {
            this.payloads.badSourceDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceConnect() throws java.lang.Throwable {
            this.payloads.badSourceConnect.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceSubscribe2() throws java.lang.Throwable {
            this.payloads.badSourceSubscribe2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceCompleteDisconnect() throws java.lang.Throwable {
            this.payloads.badSourceCompleteDisconnect.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSourceAsnycCancel() throws java.lang.Throwable {
            this.payloads.blockingSourceAsnycCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_byCount() throws java.lang.Throwable {
            this.payloads.byCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resubscribeBeforeTimeout() throws java.lang.Throwable {
            this.payloads.resubscribeBeforeTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_letitTimeout() throws java.lang.Throwable {
            this.payloads.letitTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_comeAndGo() throws java.lang.Throwable {
            this.payloads.comeAndGo.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeSubscribeRace() throws java.lang.Throwable {
            this.payloads.unsubscribeSubscribeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnX() throws java.lang.Throwable {
            this.payloads.doubleOnX.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnXCount() throws java.lang.Throwable {
            this.payloads.doubleOnXCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnXTime() throws java.lang.Throwable {
            this.payloads.doubleOnXTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelTerminateStateExclusion() throws java.lang.Throwable {
            this.payloads.cancelTerminateStateExclusion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replayRefCountShallBeThreadSafe() throws java.lang.Throwable {
            this.payloads.replayRefCountShallBeThreadSafe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutResetsSource() throws java.lang.Throwable {
            this.payloads.timeoutResetsSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disconnectBeforeConnect() throws java.lang.Throwable {
            this.payloads.disconnectBeforeConnect.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishRefCountShallBeThreadSafe() throws java.lang.Throwable {
            this.payloads.publishRefCountShallBeThreadSafe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamTerminationTriggersAnotherCancel() throws java.lang.Throwable {
            this.payloads.upstreamTerminationTriggersAnotherCancel.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRefCountTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRefCountTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRefCountTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRefCountTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableRefCountTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRefCountTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableRefCountTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableRefCountTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement refCountAsync;

            public org.junit.runners.model.Statement refCountSynchronous;

            public org.junit.runners.model.Statement refCountSynchronousTake;

            public org.junit.runners.model.Statement repeat;

            public org.junit.runners.model.Statement connectUnsubscribe;

            public org.junit.runners.model.Statement connectUnsubscribeRaceConditionLoop;

            public org.junit.runners.model.Statement connectUnsubscribeRaceCondition;

            public org.junit.runners.model.Statement onlyFirstShouldSubscribeAndLastUnsubscribe;

            public org.junit.runners.model.Statement refCount;

            public org.junit.runners.model.Statement alreadyUnsubscribedClient;

            public org.junit.runners.model.Statement alreadyUnsubscribedInterleavesWithClient;

            public org.junit.runners.model.Statement connectDisconnectConnectAndSubjectState;

            public org.junit.runners.model.Statement upstreamErrorAllowsRetry;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement noOpConnect;

            public org.junit.runners.model.Statement replayNoLeak;

            public org.junit.runners.model.Statement replayNoLeak2;

            public org.junit.runners.model.Statement publishNoLeak;

            public org.junit.runners.model.Statement publishNoLeak2;

            public org.junit.runners.model.Statement replayIsUnsubscribed;

            public org.junit.runners.model.Statement badSourceSubscribe;

            public org.junit.runners.model.Statement badSourceDispose;

            public org.junit.runners.model.Statement badSourceConnect;

            public org.junit.runners.model.Statement badSourceSubscribe2;

            public org.junit.runners.model.Statement badSourceCompleteDisconnect;

            public org.junit.runners.model.Statement blockingSourceAsnycCancel;

            public org.junit.runners.model.Statement byCount;

            public org.junit.runners.model.Statement resubscribeBeforeTimeout;

            public org.junit.runners.model.Statement letitTimeout;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement comeAndGo;

            public org.junit.runners.model.Statement unsubscribeSubscribeRace;

            public org.junit.runners.model.Statement doubleOnX;

            public org.junit.runners.model.Statement doubleOnXCount;

            public org.junit.runners.model.Statement doubleOnXTime;

            public org.junit.runners.model.Statement cancelTerminateStateExclusion;

            public org.junit.runners.model.Statement replayRefCountShallBeThreadSafe;

            public org.junit.runners.model.Statement timeoutResetsSource;

            public org.junit.runners.model.Statement disconnectBeforeConnect;

            public org.junit.runners.model.Statement publishRefCountShallBeThreadSafe;

            public org.junit.runners.model.Statement upstreamTerminationTriggersAnotherCancel;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.refCountAsync = _ClassStatement.forPayload(ObservableRefCountTest::refCountAsync, "refCountAsync", this);
            this.payloads.refCountSynchronous = _ClassStatement.forPayload(ObservableRefCountTest::refCountSynchronous, "refCountSynchronous", this);
            this.payloads.refCountSynchronousTake = _ClassStatement.forPayload(ObservableRefCountTest::refCountSynchronousTake, "refCountSynchronousTake", this);
            this.payloads.repeat = _ClassStatement.forPayload(ObservableRefCountTest::repeat, "repeat", this);
            this.payloads.connectUnsubscribe = _ClassStatement.forPayload(ObservableRefCountTest::connectUnsubscribe, "connectUnsubscribe", this);
            this.payloads.connectUnsubscribeRaceConditionLoop = _ClassStatement.forPayload(ObservableRefCountTest::connectUnsubscribeRaceConditionLoop, "connectUnsubscribeRaceConditionLoop", this);
            this.payloads.connectUnsubscribeRaceCondition = _ClassStatement.forPayload(ObservableRefCountTest::connectUnsubscribeRaceCondition, "connectUnsubscribeRaceCondition", this);
            this.payloads.onlyFirstShouldSubscribeAndLastUnsubscribe = _ClassStatement.forPayload(ObservableRefCountTest::onlyFirstShouldSubscribeAndLastUnsubscribe, "onlyFirstShouldSubscribeAndLastUnsubscribe", this);
            this.payloads.refCount = _ClassStatement.forPayload(ObservableRefCountTest::refCount, "refCount", this);
            this.payloads.alreadyUnsubscribedClient = _ClassStatement.forPayload(ObservableRefCountTest::alreadyUnsubscribedClient, "alreadyUnsubscribedClient", this);
            this.payloads.alreadyUnsubscribedInterleavesWithClient = _ClassStatement.forPayload(ObservableRefCountTest::alreadyUnsubscribedInterleavesWithClient, "alreadyUnsubscribedInterleavesWithClient", this);
            this.payloads.connectDisconnectConnectAndSubjectState = _ClassStatement.forPayload(ObservableRefCountTest::connectDisconnectConnectAndSubjectState, "connectDisconnectConnectAndSubjectState", this);
            this.payloads.upstreamErrorAllowsRetry = _ClassStatement.forPayload(ObservableRefCountTest::upstreamErrorAllowsRetry, "upstreamErrorAllowsRetry", this);
            this.payloads.disposed = _ClassStatement.forPayload(ObservableRefCountTest::disposed, "disposed", this);
            this.payloads.noOpConnect = _ClassStatement.forPayload(ObservableRefCountTest::noOpConnect, "noOpConnect", this);
            this.payloads.replayNoLeak = _ClassStatement.forPayload(ObservableRefCountTest::replayNoLeak, "replayNoLeak", this);
            this.payloads.replayNoLeak2 = _ClassStatement.forPayload(ObservableRefCountTest::replayNoLeak2, "replayNoLeak2", this);
            this.payloads.publishNoLeak = _ClassStatement.forPayload(ObservableRefCountTest::publishNoLeak, "publishNoLeak", this);
            this.payloads.publishNoLeak2 = _ClassStatement.forPayload(ObservableRefCountTest::publishNoLeak2, "publishNoLeak2", this);
            this.payloads.replayIsUnsubscribed = _ClassStatement.forPayload(ObservableRefCountTest::replayIsUnsubscribed, "replayIsUnsubscribed", this);
            this.payloads.badSourceSubscribe = _ClassStatement.forPayload(ObservableRefCountTest::badSourceSubscribe, "badSourceSubscribe", this);
            this.payloads.badSourceDispose = _ClassStatement.forPayload(ObservableRefCountTest::badSourceDispose, "badSourceDispose", this);
            this.payloads.badSourceConnect = _ClassStatement.forPayload(ObservableRefCountTest::badSourceConnect, "badSourceConnect", this);
            this.payloads.badSourceSubscribe2 = _ClassStatement.forPayload(ObservableRefCountTest::badSourceSubscribe2, "badSourceSubscribe2", this);
            this.payloads.badSourceCompleteDisconnect = _ClassStatement.forPayload(ObservableRefCountTest::badSourceCompleteDisconnect, "badSourceCompleteDisconnect", this);
            this.payloads.blockingSourceAsnycCancel = _ClassStatement.forPayload(ObservableRefCountTest::blockingSourceAsnycCancel, "blockingSourceAsnycCancel", this);
            this.payloads.byCount = _ClassStatement.forPayload(ObservableRefCountTest::byCount, "byCount", this);
            this.payloads.resubscribeBeforeTimeout = _ClassStatement.forPayload(ObservableRefCountTest::resubscribeBeforeTimeout, "resubscribeBeforeTimeout", this);
            this.payloads.letitTimeout = _ClassStatement.forPayload(ObservableRefCountTest::letitTimeout, "letitTimeout", this);
            this.payloads.error = _ClassStatement.forPayload(ObservableRefCountTest::error, "error", this);
            this.payloads.comeAndGo = _ClassStatement.forPayload(ObservableRefCountTest::comeAndGo, "comeAndGo", this);
            this.payloads.unsubscribeSubscribeRace = _ClassStatement.forPayload(ObservableRefCountTest::unsubscribeSubscribeRace, "unsubscribeSubscribeRace", this);
            this.payloads.doubleOnX = _ClassStatement.forPayload(ObservableRefCountTest::doubleOnX, "doubleOnX", this);
            this.payloads.doubleOnXCount = _ClassStatement.forPayload(ObservableRefCountTest::doubleOnXCount, "doubleOnXCount", this);
            this.payloads.doubleOnXTime = _ClassStatement.forPayload(ObservableRefCountTest::doubleOnXTime, "doubleOnXTime", this);
            this.payloads.cancelTerminateStateExclusion = _ClassStatement.forPayload(ObservableRefCountTest::cancelTerminateStateExclusion, "cancelTerminateStateExclusion", this);
            this.payloads.replayRefCountShallBeThreadSafe = _ClassStatement.forPayload(ObservableRefCountTest::replayRefCountShallBeThreadSafe, "replayRefCountShallBeThreadSafe", this);
            this.payloads.timeoutResetsSource = _ClassStatement.forPayload(ObservableRefCountTest::timeoutResetsSource, "timeoutResetsSource", this);
            this.payloads.disconnectBeforeConnect = _ClassStatement.forPayload(ObservableRefCountTest::disconnectBeforeConnect, "disconnectBeforeConnect", this);
            this.payloads.publishRefCountShallBeThreadSafe = _ClassStatement.forPayload(ObservableRefCountTest::publishRefCountShallBeThreadSafe, "publishRefCountShallBeThreadSafe", this);
            this.payloads.upstreamTerminationTriggersAnotherCancel = _ClassStatement.forPayload(ObservableRefCountTest::upstreamTerminationTriggersAnotherCancel, "upstreamTerminationTriggersAnotherCancel", this);
        }
    }
}
