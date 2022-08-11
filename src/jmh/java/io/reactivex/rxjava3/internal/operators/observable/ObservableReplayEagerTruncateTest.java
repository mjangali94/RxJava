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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.lang.management.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.mockito.InOrder;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.fuseable.HasUpstreamObservableSource;
import io.reactivex.rxjava3.internal.operators.observable.ObservableReplay.*;
import io.reactivex.rxjava3.observables.ConnectableObservable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableReplayEagerTruncateTest extends RxJavaTest {

    @Test
    public void bufferedReplay() {
        PublishSubject<Integer> source = PublishSubject.create();
        ConnectableObservable<Integer> co = source.replay(3, true);
        co.connect();
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            source.onNext(1);
            source.onNext(2);
            source.onNext(3);
            inOrder.verify(observer1, times(1)).onNext(1);
            inOrder.verify(observer1, times(1)).onNext(2);
            inOrder.verify(observer1, times(1)).onNext(3);
            source.onNext(4);
            source.onComplete();
            inOrder.verify(observer1, times(1)).onNext(4);
            inOrder.verify(observer1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onError(any(Throwable.class));
        }
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            inOrder.verify(observer1, times(1)).onNext(2);
            inOrder.verify(observer1, times(1)).onNext(3);
            inOrder.verify(observer1, times(1)).onNext(4);
            inOrder.verify(observer1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onError(any(Throwable.class));
        }
    }

    @Test
    public void bufferedWindowReplay() {
        PublishSubject<Integer> source = PublishSubject.create();
        TestScheduler scheduler = new TestScheduler();
        ConnectableObservable<Integer> co = source.replay(3, 100, TimeUnit.MILLISECONDS, scheduler, true);
        co.connect();
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            source.onNext(1);
            scheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
            source.onNext(2);
            scheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
            source.onNext(3);
            scheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
            inOrder.verify(observer1, times(1)).onNext(1);
            inOrder.verify(observer1, times(1)).onNext(2);
            inOrder.verify(observer1, times(1)).onNext(3);
            source.onNext(4);
            source.onNext(5);
            scheduler.advanceTimeBy(90, TimeUnit.MILLISECONDS);
            inOrder.verify(observer1, times(1)).onNext(4);
            inOrder.verify(observer1, times(1)).onNext(5);
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onError(any(Throwable.class));
        }
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            inOrder.verify(observer1, times(1)).onNext(4);
            inOrder.verify(observer1, times(1)).onNext(5);
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onError(any(Throwable.class));
        }
    }

    @Test
    public void windowedReplay() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        ConnectableObservable<Integer> co = source.replay(100, TimeUnit.MILLISECONDS, scheduler, true);
        co.connect();
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            source.onNext(1);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onNext(2);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onNext(3);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onComplete();
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            inOrder.verify(observer1, times(1)).onNext(1);
            inOrder.verify(observer1, times(1)).onNext(2);
            inOrder.verify(observer1, times(1)).onNext(3);
            inOrder.verify(observer1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onError(any(Throwable.class));
        }
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            inOrder.verify(observer1, never()).onNext(3);
            inOrder.verify(observer1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onError(any(Throwable.class));
        }
    }

    @Test
    public void replaySelector() {
        final Function<Integer, Integer> dbl = new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                return t1 * 2;
            }
        };
        Function<Observable<Integer>, Observable<Integer>> selector = new Function<Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> t1) {
                return t1.map(dbl);
            }
        };
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> co = source.replay(selector);
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            source.onNext(1);
            source.onNext(2);
            source.onNext(3);
            inOrder.verify(observer1, times(1)).onNext(2);
            inOrder.verify(observer1, times(1)).onNext(4);
            inOrder.verify(observer1, times(1)).onNext(6);
            source.onNext(4);
            source.onComplete();
            inOrder.verify(observer1, times(1)).onNext(8);
            inOrder.verify(observer1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onError(any(Throwable.class));
        }
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            inOrder.verify(observer1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onError(any(Throwable.class));
        }
    }

    @Test
    public void bufferedReplaySelector() {
        final Function<Integer, Integer> dbl = new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                return t1 * 2;
            }
        };
        Function<Observable<Integer>, Observable<Integer>> selector = new Function<Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> t1) {
                return t1.map(dbl);
            }
        };
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> co = source.replay(selector, 3);
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            source.onNext(1);
            source.onNext(2);
            source.onNext(3);
            inOrder.verify(observer1, times(1)).onNext(2);
            inOrder.verify(observer1, times(1)).onNext(4);
            inOrder.verify(observer1, times(1)).onNext(6);
            source.onNext(4);
            source.onComplete();
            inOrder.verify(observer1, times(1)).onNext(8);
            inOrder.verify(observer1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onError(any(Throwable.class));
        }
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            inOrder.verify(observer1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onError(any(Throwable.class));
        }
    }

    @Test
    public void windowedReplaySelector() {
        final Function<Integer, Integer> dbl = new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                return t1 * 2;
            }
        };
        Function<Observable<Integer>, Observable<Integer>> selector = new Function<Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> t1) {
                return t1.map(dbl);
            }
        };
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> co = source.replay(selector, 100, TimeUnit.MILLISECONDS, scheduler);
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            source.onNext(1);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onNext(2);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onNext(3);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onComplete();
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            inOrder.verify(observer1, times(1)).onNext(2);
            inOrder.verify(observer1, times(1)).onNext(4);
            inOrder.verify(observer1, times(1)).onNext(6);
            inOrder.verify(observer1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onError(any(Throwable.class));
        }
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            inOrder.verify(observer1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onError(any(Throwable.class));
        }
    }

    @Test
    public void bufferedReplayError() {
        PublishSubject<Integer> source = PublishSubject.create();
        ConnectableObservable<Integer> co = source.replay(3, true);
        co.connect();
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            source.onNext(1);
            source.onNext(2);
            source.onNext(3);
            inOrder.verify(observer1, times(1)).onNext(1);
            inOrder.verify(observer1, times(1)).onNext(2);
            inOrder.verify(observer1, times(1)).onNext(3);
            source.onNext(4);
            source.onError(new RuntimeException("Forced failure"));
            inOrder.verify(observer1, times(1)).onNext(4);
            inOrder.verify(observer1, times(1)).onError(any(RuntimeException.class));
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onComplete();
        }
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            inOrder.verify(observer1, times(1)).onNext(2);
            inOrder.verify(observer1, times(1)).onNext(3);
            inOrder.verify(observer1, times(1)).onNext(4);
            inOrder.verify(observer1, times(1)).onError(any(RuntimeException.class));
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onComplete();
        }
    }

    @Test
    public void windowedReplayError() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        ConnectableObservable<Integer> co = source.replay(100, TimeUnit.MILLISECONDS, scheduler, true);
        co.connect();
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            source.onNext(1);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onNext(2);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onNext(3);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onError(new RuntimeException("Forced failure"));
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            inOrder.verify(observer1, times(1)).onNext(1);
            inOrder.verify(observer1, times(1)).onNext(2);
            inOrder.verify(observer1, times(1)).onNext(3);
            inOrder.verify(observer1, times(1)).onError(any(RuntimeException.class));
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onComplete();
        }
        {
            Observer<Object> observer1 = TestHelper.mockObserver();
            InOrder inOrder = inOrder(observer1);
            co.subscribe(observer1);
            inOrder.verify(observer1, never()).onNext(3);
            inOrder.verify(observer1, times(1)).onError(any(RuntimeException.class));
            inOrder.verifyNoMoreInteractions();
            verify(observer1, never()).onComplete();
        }
    }

    @Test
    public void synchronousDisconnect() {
        final AtomicInteger effectCounter = new AtomicInteger();
        Observable<Integer> source = Observable.just(1, 2, 3, 4).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) {
                effectCounter.incrementAndGet();
                System.out.println("Sideeffect #" + v);
            }
        });
        Observable<Integer> result = source.replay(new Function<Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> o) {
                return o.take(2);
            }
        });
        for (int i = 1; i < 3; i++) {
            effectCounter.set(0);
            System.out.printf("- %d -%n", i);
            result.subscribe(new Consumer<Integer>() {

                @Override
                public void accept(Integer t1) {
                    System.out.println(t1);
                }
            }, new Consumer<Throwable>() {

                @Override
                public void accept(Throwable t1) {
                    t1.printStackTrace();
                }
            }, new Action() {

                @Override
                public void run() {
                    System.out.println("Done");
                }
            });
            assertEquals(2, effectCounter.get());
        }
    }

    /*
     * test the basic expectation of OperatorMulticast via replay
     */
    @SuppressWarnings("unchecked")
    @Test
    public void issue2191_UnsubscribeSource() throws Throwable {
        // setup mocks
        Consumer<Integer> sourceNext = mock(Consumer.class);
        Action sourceCompleted = mock(Action.class);
        Action sourceUnsubscribed = mock(Action.class);
        Observer<Integer> spiedSubscriberBeforeConnect = TestHelper.mockObserver();
        Observer<Integer> spiedSubscriberAfterConnect = TestHelper.mockObserver();
        // Observable under test
        Observable<Integer> source = Observable.just(1, 2);
        ConnectableObservable<Integer> replay = source.doOnNext(sourceNext).doOnDispose(sourceUnsubscribed).doOnComplete(sourceCompleted).replay();
        replay.subscribe(spiedSubscriberBeforeConnect);
        replay.subscribe(spiedSubscriberBeforeConnect);
        replay.connect();
        replay.subscribe(spiedSubscriberAfterConnect);
        replay.subscribe(spiedSubscriberAfterConnect);
        verify(spiedSubscriberBeforeConnect, times(2)).onSubscribe((Disposable) any());
        verify(spiedSubscriberAfterConnect, times(2)).onSubscribe((Disposable) any());
        // verify interactions
        verify(sourceNext, times(1)).accept(1);
        verify(sourceNext, times(1)).accept(2);
        verify(sourceCompleted, times(1)).run();
        verifyObserverMock(spiedSubscriberBeforeConnect, 2, 4);
        verifyObserverMock(spiedSubscriberAfterConnect, 2, 4);
        // verify(sourceUnsubscribed, times(1)).run();
        verifyNoMoreInteractions(sourceNext);
        verifyNoMoreInteractions(sourceCompleted);
        verifyNoMoreInteractions(sourceUnsubscribed);
        verifyNoMoreInteractions(spiedSubscriberBeforeConnect);
        verifyNoMoreInteractions(spiedSubscriberAfterConnect);
    }

    /**
     * Specifically test interaction with a Scheduler with subscribeOn.
     *
     * @throws Throwable functional interfaces are declared with throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void issue2191_SchedulerUnsubscribe() throws Throwable {
        // setup mocks
        Consumer<Integer> sourceNext = mock(Consumer.class);
        Action sourceCompleted = mock(Action.class);
        Action sourceUnsubscribed = mock(Action.class);
        final TestScheduler mockScheduler = new TestScheduler();
        Observer<Integer> mockObserverBeforeConnect = TestHelper.mockObserver();
        Observer<Integer> mockObserverAfterConnect = TestHelper.mockObserver();
        // Observable under test
        ConnectableObservable<Integer> replay = Observable.just(1, 2, 3).doOnNext(sourceNext).doOnDispose(sourceUnsubscribed).doOnComplete(sourceCompleted).subscribeOn(mockScheduler).replay();
        replay.subscribe(mockObserverBeforeConnect);
        replay.connect();
        replay.subscribe(mockObserverAfterConnect);
        verify(mockObserverBeforeConnect).onSubscribe((Disposable) any());
        verify(mockObserverAfterConnect).onSubscribe((Disposable) any());
        mockScheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        // verify interactions
        verify(sourceNext, times(1)).accept(1);
        verify(sourceNext, times(1)).accept(2);
        verify(sourceNext, times(1)).accept(3);
        verify(sourceCompleted, times(1)).run();
        verifyObserverMock(mockObserverBeforeConnect, 1, 3);
        verifyObserverMock(mockObserverAfterConnect, 1, 3);
        // FIXME not supported
        // verify(spiedWorker, times(1)).isUnsubscribed();
        // FIXME publish calls cancel too
        // verify(sourceUnsubscribed, times(1)).run();
        verifyNoMoreInteractions(sourceNext);
        verifyNoMoreInteractions(sourceCompleted);
        verifyNoMoreInteractions(sourceUnsubscribed);
        verifyNoMoreInteractions(mockObserverBeforeConnect);
        verifyNoMoreInteractions(mockObserverAfterConnect);
    }

    /**
     * Specifically test interaction with a Scheduler with subscribeOn.
     *
     * @throws Throwable functional interfaces are declared with throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void issue2191_SchedulerUnsubscribeOnError() throws Throwable {
        // setup mocks
        Consumer<Integer> sourceNext = mock(Consumer.class);
        Action sourceCompleted = mock(Action.class);
        Consumer<Throwable> sourceError = mock(Consumer.class);
        Action sourceUnsubscribed = mock(Action.class);
        final TestScheduler mockScheduler = new TestScheduler();
        Observer<Integer> mockObserverBeforeConnect = TestHelper.mockObserver();
        Observer<Integer> mockObserverAfterConnect = TestHelper.mockObserver();
        // Observable under test
        Function<Integer, Integer> mockFunc = mock(Function.class);
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException();
        when(mockFunc.apply(1)).thenReturn(1);
        when(mockFunc.apply(2)).thenThrow(illegalArgumentException);
        ConnectableObservable<Integer> replay = Observable.just(1, 2, 3).map(mockFunc).doOnNext(sourceNext).doOnDispose(sourceUnsubscribed).doOnComplete(sourceCompleted).doOnError(sourceError).subscribeOn(mockScheduler).replay();
        replay.subscribe(mockObserverBeforeConnect);
        replay.connect();
        replay.subscribe(mockObserverAfterConnect);
        verify(mockObserverBeforeConnect).onSubscribe((Disposable) any());
        verify(mockObserverAfterConnect).onSubscribe((Disposable) any());
        mockScheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        // verify interactions
        verify(sourceNext, times(1)).accept(1);
        verify(sourceError, times(1)).accept(illegalArgumentException);
        verifyObserver(mockObserverBeforeConnect, 1, 1, illegalArgumentException);
        verifyObserver(mockObserverAfterConnect, 1, 1, illegalArgumentException);
        // FIXME no longer supported
        // verify(spiedWorker, times(1)).isUnsubscribed();
        // FIXME publish also calls cancel
        // verify(sourceUnsubscribed, times(1)).run();
        verifyNoMoreInteractions(sourceNext);
        verifyNoMoreInteractions(sourceCompleted);
        verifyNoMoreInteractions(sourceError);
        verifyNoMoreInteractions(sourceUnsubscribed);
        verifyNoMoreInteractions(mockObserverBeforeConnect);
        verifyNoMoreInteractions(mockObserverAfterConnect);
    }

    private static void verifyObserverMock(Observer<Integer> mock, int numSubscriptions, int numItemsExpected) {
        verify(mock, times(numItemsExpected)).onNext((Integer) notNull());
        verify(mock, times(numSubscriptions)).onComplete();
        verifyNoMoreInteractions(mock);
    }

    private static void verifyObserver(Observer<Integer> mock, int numSubscriptions, int numItemsExpected, Throwable error) {
        verify(mock, times(numItemsExpected)).onNext((Integer) notNull());
        verify(mock, times(numSubscriptions)).onError(error);
        verifyNoMoreInteractions(mock);
    }

    public static Worker workerSpy(final Disposable mockDisposable) {
        return spy(new InprocessWorker(mockDisposable));
    }

    static class InprocessWorker extends Worker {

        private final Disposable mockDisposable;

        public boolean unsubscribed;

        InprocessWorker(Disposable mockDisposable) {
            this.mockDisposable = mockDisposable;
        }

        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable action) {
            action.run();
            // this subscription is returned but discarded
            return mockDisposable;
        }

        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable action, long delayTime, @NonNull TimeUnit unit) {
            action.run();
            return mockDisposable;
        }

        @Override
        public void dispose() {
            unsubscribed = true;
        }

        @Override
        public boolean isDisposed() {
            return unsubscribed;
        }
    }

    @Test
    public void boundedReplayBuffer() {
        BoundedReplayBuffer<Integer> buf = new BoundedReplayBuffer<Integer>(false) {

            private static final long serialVersionUID = -5182053207244406872L;

            @Override
            void truncate() {
            }
        };
        buf.addLast(new Node(1));
        buf.addLast(new Node(2));
        buf.addLast(new Node(3));
        buf.addLast(new Node(4));
        buf.addLast(new Node(5));
        List<Integer> values = new ArrayList<>();
        buf.collect(values);
        Assert.assertEquals(Arrays.asList(1, 2, 3, 4, 5), values);
        buf.removeSome(2);
        buf.removeFirst();
        buf.removeSome(2);
        values.clear();
        buf.collect(values);
        Assert.assertTrue(values.isEmpty());
        buf.addLast(new Node(5));
        buf.addLast(new Node(6));
        buf.collect(values);
        Assert.assertEquals(Arrays.asList(5, 6), values);
    }

    @Test
    public void timedAndSizedTruncation() {
        TestScheduler test = new TestScheduler();
        SizeAndTimeBoundReplayBuffer<Integer> buf = new SizeAndTimeBoundReplayBuffer<>(2, 2000, TimeUnit.MILLISECONDS, test, false);
        List<Integer> values = new ArrayList<>();
        buf.next(1);
        test.advanceTimeBy(1, TimeUnit.SECONDS);
        buf.next(2);
        test.advanceTimeBy(1, TimeUnit.SECONDS);
        buf.collect(values);
        Assert.assertEquals(Arrays.asList(2), values);
        buf.next(3);
        buf.next(4);
        values.clear();
        buf.collect(values);
        Assert.assertEquals(Arrays.asList(3, 4), values);
        test.advanceTimeBy(2, TimeUnit.SECONDS);
        buf.next(5);
        values.clear();
        buf.collect(values);
        Assert.assertEquals(Arrays.asList(5), values);
        Assert.assertFalse(buf.hasCompleted());
        test.advanceTimeBy(2, TimeUnit.SECONDS);
        buf.complete();
        values.clear();
        buf.collect(values);
        Assert.assertTrue(values.isEmpty());
        Assert.assertEquals(1, buf.size);
        Assert.assertTrue(buf.hasCompleted());
        Assert.assertFalse(buf.hasError());
    }

    @Test
    public void timedAndSizedTruncationError() {
        TestScheduler test = new TestScheduler();
        SizeAndTimeBoundReplayBuffer<Integer> buf = new SizeAndTimeBoundReplayBuffer<>(2, 2000, TimeUnit.MILLISECONDS, test, false);
        Assert.assertFalse(buf.hasCompleted());
        Assert.assertFalse(buf.hasError());
        List<Integer> values = new ArrayList<>();
        buf.next(1);
        test.advanceTimeBy(1, TimeUnit.SECONDS);
        buf.next(2);
        test.advanceTimeBy(1, TimeUnit.SECONDS);
        buf.collect(values);
        Assert.assertEquals(Arrays.asList(2), values);
        buf.next(3);
        buf.next(4);
        values.clear();
        buf.collect(values);
        Assert.assertEquals(Arrays.asList(3, 4), values);
        test.advanceTimeBy(2, TimeUnit.SECONDS);
        buf.next(5);
        values.clear();
        buf.collect(values);
        Assert.assertEquals(Arrays.asList(5), values);
        Assert.assertFalse(buf.hasCompleted());
        Assert.assertFalse(buf.hasError());
        test.advanceTimeBy(2, TimeUnit.SECONDS);
        buf.error(new TestException());
        values.clear();
        buf.collect(values);
        Assert.assertTrue(values.isEmpty());
        Assert.assertEquals(1, buf.size);
        Assert.assertFalse(buf.hasCompleted());
        Assert.assertTrue(buf.hasError());
    }

    @Test
    public void sizedTruncation() {
        SizeBoundReplayBuffer<Integer> buf = new SizeBoundReplayBuffer<>(2, false);
        List<Integer> values = new ArrayList<>();
        buf.next(1);
        buf.next(2);
        buf.collect(values);
        Assert.assertEquals(Arrays.asList(1, 2), values);
        buf.next(3);
        buf.next(4);
        values.clear();
        buf.collect(values);
        Assert.assertEquals(Arrays.asList(3, 4), values);
        buf.next(5);
        values.clear();
        buf.collect(values);
        Assert.assertEquals(Arrays.asList(4, 5), values);
        Assert.assertFalse(buf.hasCompleted());
        buf.complete();
        values.clear();
        buf.collect(values);
        Assert.assertEquals(Arrays.asList(4, 5), values);
        Assert.assertEquals(3, buf.size);
        Assert.assertTrue(buf.hasCompleted());
        Assert.assertFalse(buf.hasError());
    }

    @Test
    public void coldReplayNoBackpressure() {
        Observable<Integer> source = Observable.range(0, 1000).replay().autoConnect();
        TestObserverEx<Integer> to = new TestObserverEx<>();
        source.subscribe(to);
        to.assertNoErrors();
        to.assertTerminated();
        List<Integer> onNextEvents = to.values();
        assertEquals(1000, onNextEvents.size());
        for (int i = 0; i < 1000; i++) {
            assertEquals((Integer) i, onNextEvents.get(i));
        }
    }

    @Test
    public void cache() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        Observable<String> o = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(final Observer<? super String> observer) {
                observer.onSubscribe(Disposable.empty());
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        counter.incrementAndGet();
                        System.out.println("published Observable being executed");
                        observer.onNext("one");
                        observer.onComplete();
                    }
                }).start();
            }
        }).replay().autoConnect();
        // we then expect the following 2 subscriptions to get that same value
        final CountDownLatch latch = new CountDownLatch(2);
        // subscribe once
        o.subscribe(new Consumer<String>() {

            @Override
            public void accept(String v) {
                assertEquals("one", v);
                System.out.println("v: " + v);
                latch.countDown();
            }
        });
        // subscribe again
        o.subscribe(new Consumer<String>() {

            @Override
            public void accept(String v) {
                assertEquals("one", v);
                System.out.println("v: " + v);
                latch.countDown();
            }
        });
        if (!latch.await(1000, TimeUnit.MILLISECONDS)) {
            fail("subscriptions did not receive values");
        }
        assertEquals(1, counter.get());
    }

    @Test
    public void unsubscribeSource() throws Throwable {
        Action unsubscribe = mock(Action.class);
        Observable<Integer> o = Observable.just(1).doOnDispose(unsubscribe).replay().autoConnect();
        o.subscribe();
        o.subscribe();
        o.subscribe();
        verify(unsubscribe, never()).run();
    }

    @Test
    public void take() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        Observable<Integer> cached = Observable.range(1, 100).replay().autoConnect();
        cached.take(10).subscribe(to);
        to.assertNoErrors();
        to.assertTerminated();
        to.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    // FIXME no longer assertable
    // ts.assertUnsubscribed();
    }

    @Test
    public void async() {
        Observable<Integer> source = Observable.range(1, 10000);
        for (int i = 0; i < 100; i++) {
            TestObserverEx<Integer> to1 = new TestObserverEx<>();
            Observable<Integer> cached = source.replay().autoConnect();
            cached.observeOn(Schedulers.computation()).subscribe(to1);
            to1.awaitDone(2, TimeUnit.SECONDS);
            to1.assertNoErrors();
            to1.assertTerminated();
            assertEquals(10000, to1.values().size());
            TestObserverEx<Integer> to2 = new TestObserverEx<>();
            cached.observeOn(Schedulers.computation()).subscribe(to2);
            to2.awaitDone(2, TimeUnit.SECONDS);
            to2.assertNoErrors();
            to2.assertTerminated();
            assertEquals(10000, to2.values().size());
        }
    }

    @Test
    public void asyncComeAndGo() {
        Observable<Long> source = Observable.interval(1, 1, TimeUnit.MILLISECONDS).take(1000).subscribeOn(Schedulers.io());
        Observable<Long> cached = source.replay().autoConnect();
        Observable<Long> output = cached.observeOn(Schedulers.computation());
        List<TestObserverEx<Long>> list = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            TestObserverEx<Long> to = new TestObserverEx<>();
            list.add(to);
            output.skip(i * 10).take(10).subscribe(to);
        }
        List<Long> expected = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            expected.add((long) (i - 10));
        }
        int j = 0;
        for (TestObserverEx<Long> to : list) {
            to.awaitDone(3, TimeUnit.SECONDS);
            to.assertNoErrors();
            to.assertTerminated();
            for (int i = j * 10; i < j * 10 + 10; i++) {
                expected.set(i - j * 10, (long) i);
            }
            to.assertValueSequence(expected);
            j++;
        }
    }

    @Test
    public void noMissingBackpressureException() {
        final int m = 4 * 1000 * 1000;
        Observable<Integer> firehose = Observable.unsafeCreate(new ObservableSource<Integer>() {

            @Override
            public void subscribe(Observer<? super Integer> t) {
                t.onSubscribe(Disposable.empty());
                for (int i = 0; i < m; i++) {
                    t.onNext(i);
                }
                t.onComplete();
            }
        });
        TestObserverEx<Integer> to = new TestObserverEx<>();
        firehose.replay().autoConnect().observeOn(Schedulers.computation()).takeLast(100).subscribe(to);
        to.awaitDone(3, TimeUnit.SECONDS);
        to.assertNoErrors();
        to.assertTerminated();
        assertEquals(100, to.values().size());
    }

    @Test
    public void valuesAndThenError() {
        Observable<Integer> source = Observable.range(1, 10).concatWith(Observable.<Integer>error(new TestException())).replay().autoConnect();
        TestObserverEx<Integer> to = new TestObserverEx<>();
        source.subscribe(to);
        to.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        to.assertNotComplete();
        Assert.assertEquals(1, to.errors().size());
        TestObserverEx<Integer> to2 = new TestObserverEx<>();
        source.subscribe(to2);
        to2.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        to2.assertNotComplete();
        Assert.assertEquals(1, to2.errors().size());
    }

    @Test
    public void replayTime() {
        Observable.just(1).replay(1, TimeUnit.MINUTES, Schedulers.computation(), true).autoConnect().test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void replaySizeAndTime() {
        Observable.just(1).replay(1, 1, TimeUnit.MILLISECONDS, Schedulers.computation(), true).autoConnect().test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void replaySelectorTime() {
        Observable.just(1).replay(Functions.<Observable<Integer>>identity(), 1, TimeUnit.MINUTES).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void replayMaxInt() {
        Observable.range(1, 2).replay(Integer.MAX_VALUE, true).autoConnect().test().assertResult(1, 2);
    }

    @Test
    public void source() {
        Observable<Integer> source = Observable.range(1, 3);
        assertSame(source, (((HasUpstreamObservableSource<?>) source.replay())).source());
    }

    @Test
    public void connectRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ConnectableObservable<Integer> co = Observable.range(1, 3).replay();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    co.connect();
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void subscribeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ConnectableObservable<Integer> co = Observable.range(1, 3).replay();
            final TestObserver<Integer> to1 = new TestObserver<>();
            final TestObserver<Integer> to2 = new TestObserver<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    co.subscribe(to1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    co.subscribe(to2);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void addRemoveRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ConnectableObservable<Integer> co = Observable.range(1, 3).replay();
            final TestObserver<Integer> to1 = new TestObserver<>();
            final TestObserver<Integer> to2 = new TestObserver<>();
            co.subscribe(to1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to1.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    co.subscribe(to2);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void cancelOnArrival() {
        Observable.range(1, 2).replay(Integer.MAX_VALUE, true).autoConnect().test(true).assertEmpty();
    }

    @Test
    public void cancelOnArrival2() {
        ConnectableObservable<Integer> co = PublishSubject.<Integer>create().replay(Integer.MAX_VALUE, true);
        co.test();
        co.autoConnect().test(true).assertEmpty();
    }

    @Test
    public void connectConsumerThrows() {
        ConnectableObservable<Integer> co = Observable.range(1, 2).replay();
        try {
            co.connect(new Consumer<Disposable>() {

                @Override
                public void accept(Disposable t) throws Exception {
                    throw new TestException();
                }
            });
            fail("Should have thrown");
        } catch (TestException ex) {
        // expected
        }
        co.test().assertEmpty().dispose();
        co.connect();
        co.test().assertResult(1, 2);
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onError(new TestException("First"));
                    observer.onNext(1);
                    observer.onError(new TestException("Second"));
                    observer.onComplete();
                }
            }.replay().autoConnect().to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void subscribeOnNextRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final ConnectableObservable<Integer> co = ps.replay();
            final TestObserver<Integer> to1 = new TestObserver<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    co.subscribe(to1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    for (int j = 0; j < 1000; j++) {
                        ps.onNext(j);
                    }
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void unsubscribeOnNextRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final ConnectableObservable<Integer> co = ps.replay();
            final TestObserver<Integer> to1 = new TestObserver<>();
            co.subscribe(to1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to1.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    for (int j = 0; j < 1000; j++) {
                        ps.onNext(j);
                    }
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void unsubscribeReplayRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ConnectableObservable<Integer> co = Observable.range(1, 1000).replay();
            final TestObserver<Integer> to1 = new TestObserver<>();
            co.connect();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    co.subscribe(to1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to1.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void reentrantOnNext() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                if (t == 1) {
                    ps.onNext(2);
                    ps.onComplete();
                }
                super.onNext(t);
            }
        };
        ps.replay().autoConnect().subscribe(to);
        ps.onNext(1);
        to.assertResult(1, 2);
    }

    @Test
    public void reentrantOnNextBound() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                if (t == 1) {
                    ps.onNext(2);
                    ps.onComplete();
                }
                super.onNext(t);
            }
        };
        ps.replay(10, true).autoConnect().subscribe(to);
        ps.onNext(1);
        to.assertResult(1, 2);
    }

    @Test
    public void reentrantOnNextCancel() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                if (t == 1) {
                    ps.onNext(2);
                    dispose();
                }
                super.onNext(t);
            }
        };
        ps.replay().autoConnect().subscribe(to);
        ps.onNext(1);
        to.assertValues(1);
    }

    @Test
    public void reentrantOnNextCancelBounded() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                if (t == 1) {
                    ps.onNext(2);
                    dispose();
                }
                super.onNext(t);
            }
        };
        ps.replay(10, true).autoConnect().subscribe(to);
        ps.onNext(1);
        to.assertValues(1);
    }

    @Test
    public void delayedUpstreamOnSubscribe() {
        final Observer<?>[] sub = { null };
        new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                sub[0] = observer;
            }
        }.replay().connect().dispose();
        Disposable bs = Disposable.empty();
        sub[0].onSubscribe(bs);
        assertTrue(bs.isDisposed());
    }

    @Test
    public void timedNoOutdatedData() {
        TestScheduler scheduler = new TestScheduler();
        Observable<Integer> source = Observable.just(1).replay(2, TimeUnit.SECONDS, scheduler, true).autoConnect();
        source.test().assertResult(1);
        source.test().assertResult(1);
        scheduler.advanceTimeBy(3, TimeUnit.SECONDS);
        source.test().assertResult();
    }

    @Test
    public void replaySelectorReturnsNull() {
        Observable.just(1).replay(new Function<Observable<Integer>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Integer> v) throws Exception {
                return null;
            }
        }).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(NullPointerException.class, "The selector returned a null ObservableSource");
    }

    @Test
    public void replaySelectorConnectableReturnsNull() {
        ObservableReplay.multicastSelector(Functions.justSupplier((ConnectableObservable<Integer>) null), Functions.justFunction(Observable.just(1))).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(NullPointerException.class, "The connectableFactory returned a null ConnectableObservable");
    }

    @Test
    public void noHeadRetentionCompleteSize() {
        PublishSubject<Integer> source = PublishSubject.create();
        ObservableReplay<Integer> co = (ObservableReplay<Integer>) source.replay(1, true);
        co.connect();
        BoundedReplayBuffer<Integer> buf = (BoundedReplayBuffer<Integer>) (co.current.get().buffer);
        source.onNext(1);
        source.onNext(2);
        source.onComplete();
        assertNull(buf.get().value);
        Object o = buf.get();
        buf.trimHead();
        assertSame(o, buf.get());
    }

    @Test
    public void noHeadRetentionErrorSize() {
        PublishSubject<Integer> source = PublishSubject.create();
        ObservableReplay<Integer> co = (ObservableReplay<Integer>) source.replay(1, true);
        co.connect();
        BoundedReplayBuffer<Integer> buf = (BoundedReplayBuffer<Integer>) (co.current.get().buffer);
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        assertNull(buf.get().value);
        Object o = buf.get();
        buf.trimHead();
        assertSame(o, buf.get());
    }

    @Test
    public void noHeadRetentionSize() {
        PublishSubject<Integer> source = PublishSubject.create();
        ObservableReplay<Integer> co = (ObservableReplay<Integer>) source.replay(1, true);
        co.connect();
        BoundedReplayBuffer<Integer> buf = (BoundedReplayBuffer<Integer>) (co.current.get().buffer);
        source.onNext(1);
        source.onNext(2);
        assertNull(buf.get().value);
        buf.trimHead();
        assertNull(buf.get().value);
        Object o = buf.get();
        buf.trimHead();
        assertSame(o, buf.get());
    }

    @Test
    public void noHeadRetentionCompleteTime() {
        PublishSubject<Integer> source = PublishSubject.create();
        ObservableReplay<Integer> co = (ObservableReplay<Integer>) source.replay(1, TimeUnit.MINUTES, Schedulers.computation(), true);
        co.connect();
        BoundedReplayBuffer<Integer> buf = (BoundedReplayBuffer<Integer>) (co.current.get().buffer);
        source.onNext(1);
        source.onNext(2);
        source.onComplete();
        assertNull(buf.get().value);
        Object o = buf.get();
        buf.trimHead();
        assertSame(o, buf.get());
    }

    @Test
    public void noHeadRetentionErrorTime() {
        PublishSubject<Integer> source = PublishSubject.create();
        ObservableReplay<Integer> co = (ObservableReplay<Integer>) source.replay(1, TimeUnit.MINUTES, Schedulers.computation(), true);
        co.connect();
        BoundedReplayBuffer<Integer> buf = (BoundedReplayBuffer<Integer>) (co.current.get().buffer);
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        assertNull(buf.get().value);
        Object o = buf.get();
        buf.trimHead();
        assertSame(o, buf.get());
    }

    @Test
    public void noHeadRetentionTime() {
        TestScheduler sch = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        ObservableReplay<Integer> co = (ObservableReplay<Integer>) source.replay(1, TimeUnit.MILLISECONDS, sch, true);
        co.connect();
        BoundedReplayBuffer<Integer> buf = (BoundedReplayBuffer<Integer>) (co.current.get().buffer);
        source.onNext(1);
        sch.advanceTimeBy(2, TimeUnit.MILLISECONDS);
        source.onNext(2);
        assertNull(buf.get().value);
        buf.trimHead();
        assertNull(buf.get().value);
        Object o = buf.get();
        buf.trimHead();
        assertSame(o, buf.get());
    }

    @Test
    public void noBoundedRetentionViaThreadLocal() throws Exception {
        Observable<byte[]> source = Observable.range(1, 200).map(new Function<Integer, byte[]>() {

            @Override
            public byte[] apply(Integer v) throws Exception {
                return new byte[1024 * 1024];
            }
        }).replay(new Function<Observable<byte[]>, Observable<byte[]>>() {

            @Override
            public Observable<byte[]> apply(final Observable<byte[]> o) throws Exception {
                return o.take(1).concatMap(new Function<byte[], Observable<byte[]>>() {

                    @Override
                    public Observable<byte[]> apply(byte[] v) throws Exception {
                        return o;
                    }
                });
            }
        }, 1).takeLast(1);
        System.out.println("Bounded Replay Leak check: Wait before GC");
        Thread.sleep(1000);
        System.out.println("Bounded Replay Leak check: GC");
        System.gc();
        Thread.sleep(500);
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage memHeap = memoryMXBean.getHeapMemoryUsage();
        long initial = memHeap.getUsed();
        System.out.printf("Bounded Replay Leak check: Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        final AtomicLong after = new AtomicLong();
        source.subscribe(new Consumer<byte[]>() {

            @Override
            public void accept(byte[] v) throws Exception {
                System.out.println("Bounded Replay Leak check: Wait before GC 2");
                Thread.sleep(1000);
                System.out.println("Bounded Replay Leak check:  GC 2");
                System.gc();
                Thread.sleep(500);
                after.set(memoryMXBean.getHeapMemoryUsage().getUsed());
            }
        });
        System.out.printf("Bounded Replay Leak check: After: %.3f MB%n", after.get() / 1024.0 / 1024.0);
        if (initial + 100 * 1024 * 1024 < after.get()) {
            Assert.fail("Bounded Replay Leak check: Memory leak detected: " + (initial / 1024.0 / 1024.0) + " -> " + after.get() / 1024.0 / 1024.0);
        }
    }

    @Test
    public void sizeBoundEagerTruncate() throws Exception {
        PublishSubject<int[]> ps = PublishSubject.create();
        ConnectableObservable<int[]> co = ps.replay(1, true);
        TestObserver<int[]> to = co.test();
        co.connect();
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long initial = memoryMXBean.getHeapMemoryUsage().getUsed();
        System.out.printf("Bounded Replay Leak check: Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        ps.onNext(new int[100 * 1024 * 1024]);
        to.assertValueCount(1);
        to.values().clear();
        ps.onNext(new int[0]);
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        long after = memoryMXBean.getHeapMemoryUsage().getUsed();
        to.dispose();
        System.out.printf("Bounded Replay Leak check: After: %.3f MB%n", after / 1024.0 / 1024.0);
        if (initial + 100 * 1024 * 1024 < after) {
            Assert.fail("Bounded Replay Leak check: Memory leak detected: " + (initial / 1024.0 / 1024.0) + " -> " + after / 1024.0 / 1024.0);
        }
    }

    @Test
    public void timeBoundEagerTruncate() throws Exception {
        PublishSubject<int[]> ps = PublishSubject.create();
        TestScheduler scheduler = new TestScheduler();
        ConnectableObservable<int[]> co = ps.replay(1, TimeUnit.SECONDS, scheduler, true);
        TestObserver<int[]> to = co.test();
        co.connect();
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long initial = memoryMXBean.getHeapMemoryUsage().getUsed();
        System.out.printf("Bounded Replay Leak check: Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        ps.onNext(new int[100 * 1024 * 1024]);
        to.assertValueCount(1);
        to.values().clear();
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        ps.onNext(new int[0]);
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        long after = memoryMXBean.getHeapMemoryUsage().getUsed();
        to.dispose();
        System.out.printf("Bounded Replay Leak check: After: %.3f MB%n", after / 1024.0 / 1024.0);
        if (initial + 100 * 1024 * 1024 < after) {
            Assert.fail("Bounded Replay Leak check: Memory leak detected: " + (initial / 1024.0 / 1024.0) + " -> " + after / 1024.0 / 1024.0);
        }
    }

    @Test
    public void timeAndSizeBoundEagerTruncate() throws Exception {
        PublishSubject<int[]> ps = PublishSubject.create();
        TestScheduler scheduler = new TestScheduler();
        ConnectableObservable<int[]> co = ps.replay(1, 5, TimeUnit.SECONDS, scheduler, true);
        TestObserver<int[]> to = co.test();
        co.connect();
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long initial = memoryMXBean.getHeapMemoryUsage().getUsed();
        System.out.printf("Bounded Replay Leak check: Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        ps.onNext(new int[100 * 1024 * 1024]);
        to.assertValueCount(1);
        to.values().clear();
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        ps.onNext(new int[0]);
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        long after = memoryMXBean.getHeapMemoryUsage().getUsed();
        to.dispose();
        System.out.printf("Bounded Replay Leak check: After: %.3f MB%n", after / 1024.0 / 1024.0);
        if (initial + 100 * 1024 * 1024 < after) {
            Assert.fail("Bounded Replay Leak check: Memory leak detected: " + (initial / 1024.0 / 1024.0) + " -> " + after / 1024.0 / 1024.0);
        }
    }

    @Test
    public void sizeBoundSelectorEagerTruncate() throws Exception {
        PublishSubject<int[]> ps = PublishSubject.create();
        Observable<int[]> co = ps.replay(Functions.<Observable<int[]>>identity(), 1, true);
        TestObserver<int[]> to = co.test();
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long initial = memoryMXBean.getHeapMemoryUsage().getUsed();
        System.out.printf("Bounded Replay Leak check: Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        ps.onNext(new int[100 * 1024 * 1024]);
        to.assertValueCount(1);
        to.values().clear();
        ps.onNext(new int[0]);
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        long after = memoryMXBean.getHeapMemoryUsage().getUsed();
        to.dispose();
        System.out.printf("Bounded Replay Leak check: After: %.3f MB%n", after / 1024.0 / 1024.0);
        if (initial + 100 * 1024 * 1024 < after) {
            Assert.fail("Bounded Replay Leak check: Memory leak detected: " + (initial / 1024.0 / 1024.0) + " -> " + after / 1024.0 / 1024.0);
        }
    }

    @Test
    public void timeBoundSelectorEagerTruncate() throws Exception {
        PublishSubject<int[]> ps = PublishSubject.create();
        TestScheduler scheduler = new TestScheduler();
        Observable<int[]> co = ps.replay(Functions.<Observable<int[]>>identity(), 1, TimeUnit.SECONDS, scheduler, true);
        TestObserver<int[]> to = co.test();
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long initial = memoryMXBean.getHeapMemoryUsage().getUsed();
        System.out.printf("Bounded Replay Leak check: Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        ps.onNext(new int[100 * 1024 * 1024]);
        to.assertValueCount(1);
        to.values().clear();
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        ps.onNext(new int[0]);
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        long after = memoryMXBean.getHeapMemoryUsage().getUsed();
        to.dispose();
        System.out.printf("Bounded Replay Leak check: After: %.3f MB%n", after / 1024.0 / 1024.0);
        if (initial + 100 * 1024 * 1024 < after) {
            Assert.fail("Bounded Replay Leak check: Memory leak detected: " + (initial / 1024.0 / 1024.0) + " -> " + after / 1024.0 / 1024.0);
        }
    }

    @Test
    public void timeAndSizeSelectorBoundEagerTruncate() throws Exception {
        PublishSubject<int[]> ps = PublishSubject.create();
        TestScheduler scheduler = new TestScheduler();
        Observable<int[]> co = ps.replay(Functions.<Observable<int[]>>identity(), 1, 5, TimeUnit.SECONDS, scheduler, true);
        TestObserver<int[]> to = co.test();
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long initial = memoryMXBean.getHeapMemoryUsage().getUsed();
        System.out.printf("Bounded Replay Leak check: Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        ps.onNext(new int[100 * 1024 * 1024]);
        to.assertValueCount(1);
        to.values().clear();
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        ps.onNext(new int[0]);
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        long after = memoryMXBean.getHeapMemoryUsage().getUsed();
        to.dispose();
        System.out.printf("Bounded Replay Leak check: After: %.3f MB%n", after / 1024.0 / 1024.0);
        if (initial + 100 * 1024 * 1024 < after) {
            Assert.fail("Bounded Replay Leak check: Memory leak detected: " + (initial / 1024.0 / 1024.0) + " -> " + after / 1024.0 / 1024.0);
        }
    }

    @Test
    public void timeAndSizeNoTerminalTruncationOnTimechange() {
        Observable.just(1).replay(1, 1, TimeUnit.SECONDS, new TimesteppingScheduler(), true).autoConnect().test().assertComplete().assertNoErrors();
    }

    @Test
    public void disposeNoNeedForResetSizeBound() {
        PublishSubject<Integer> ps = PublishSubject.create();
        ConnectableObservable<Integer> co = ps.replay(10, true);
        TestObserver<Integer> to = co.test();
        Disposable d = co.connect();
        ps.onNext(1);
        d.dispose();
        to = co.test();
        to.assertEmpty();
        co.connect();
        to.assertEmpty();
        ps.onNext(2);
        to.assertValuesOnly(2);
    }

    @Test
    public void disposeNoNeedForResetTimeBound() {
        PublishSubject<Integer> ps = PublishSubject.create();
        ConnectableObservable<Integer> co = ps.replay(10, TimeUnit.MINUTES, Schedulers.single(), true);
        TestObserver<Integer> to = co.test();
        Disposable d = co.connect();
        ps.onNext(1);
        d.dispose();
        to = co.test();
        to.assertEmpty();
        co.connect();
        to.assertEmpty();
        ps.onNext(2);
        to.assertValuesOnly(2);
    }

    @Test
    public void disposeNoNeedForResetTimeAndSIzeBound() {
        PublishSubject<Integer> ps = PublishSubject.create();
        ConnectableObservable<Integer> co = ps.replay(10, 10, TimeUnit.MINUTES, Schedulers.single(), true);
        TestObserver<Integer> to = co.test();
        Disposable d = co.connect();
        ps.onNext(1);
        d.dispose();
        to = co.test();
        to.assertEmpty();
        co.connect();
        to.assertEmpty();
        ps.onNext(2);
        to.assertValuesOnly(2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableReplayEagerTruncateTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferedReplay() throws java.lang.Throwable {
            this.payloads.bufferedReplay.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferedWindowReplay() throws java.lang.Throwable {
            this.payloads.bufferedWindowReplay.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowedReplay() throws java.lang.Throwable {
            this.payloads.windowedReplay.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySelector() throws java.lang.Throwable {
            this.payloads.replaySelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferedReplaySelector() throws java.lang.Throwable {
            this.payloads.bufferedReplaySelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowedReplaySelector() throws java.lang.Throwable {
            this.payloads.windowedReplaySelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferedReplayError() throws java.lang.Throwable {
            this.payloads.bufferedReplayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowedReplayError() throws java.lang.Throwable {
            this.payloads.windowedReplayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_synchronousDisconnect() throws java.lang.Throwable {
            this.payloads.synchronousDisconnect.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue2191_UnsubscribeSource() throws java.lang.Throwable {
            this.payloads.issue2191_UnsubscribeSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue2191_SchedulerUnsubscribe() throws java.lang.Throwable {
            this.payloads.issue2191_SchedulerUnsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue2191_SchedulerUnsubscribeOnError() throws java.lang.Throwable {
            this.payloads.issue2191_SchedulerUnsubscribeOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedReplayBuffer() throws java.lang.Throwable {
            this.payloads.boundedReplayBuffer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedAndSizedTruncation() throws java.lang.Throwable {
            this.payloads.timedAndSizedTruncation.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedAndSizedTruncationError() throws java.lang.Throwable {
            this.payloads.timedAndSizedTruncationError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sizedTruncation() throws java.lang.Throwable {
            this.payloads.sizedTruncation.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_coldReplayNoBackpressure() throws java.lang.Throwable {
            this.payloads.coldReplayNoBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cache() throws java.lang.Throwable {
            this.payloads.cache.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeSource() throws java.lang.Throwable {
            this.payloads.unsubscribeSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_async() throws java.lang.Throwable {
            this.payloads.async.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncComeAndGo() throws java.lang.Throwable {
            this.payloads.asyncComeAndGo.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noMissingBackpressureException() throws java.lang.Throwable {
            this.payloads.noMissingBackpressureException.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_valuesAndThenError() throws java.lang.Throwable {
            this.payloads.valuesAndThenError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replayTime() throws java.lang.Throwable {
            this.payloads.replayTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySizeAndTime() throws java.lang.Throwable {
            this.payloads.replaySizeAndTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySelectorTime() throws java.lang.Throwable {
            this.payloads.replaySelectorTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replayMaxInt() throws java.lang.Throwable {
            this.payloads.replayMaxInt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_source() throws java.lang.Throwable {
            this.payloads.source.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_connectRace() throws java.lang.Throwable {
            this.payloads.connectRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeRace() throws java.lang.Throwable {
            this.payloads.subscribeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addRemoveRace() throws java.lang.Throwable {
            this.payloads.addRemoveRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOnArrival() throws java.lang.Throwable {
            this.payloads.cancelOnArrival.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOnArrival2() throws java.lang.Throwable {
            this.payloads.cancelOnArrival2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_connectConsumerThrows() throws java.lang.Throwable {
            this.payloads.connectConsumerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOnNextRace() throws java.lang.Throwable {
            this.payloads.subscribeOnNextRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeOnNextRace() throws java.lang.Throwable {
            this.payloads.unsubscribeOnNextRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeReplayRace() throws java.lang.Throwable {
            this.payloads.unsubscribeReplayRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantOnNext() throws java.lang.Throwable {
            this.payloads.reentrantOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantOnNextBound() throws java.lang.Throwable {
            this.payloads.reentrantOnNextBound.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantOnNextCancel() throws java.lang.Throwable {
            this.payloads.reentrantOnNextCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantOnNextCancelBounded() throws java.lang.Throwable {
            this.payloads.reentrantOnNextCancelBounded.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayedUpstreamOnSubscribe() throws java.lang.Throwable {
            this.payloads.delayedUpstreamOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedNoOutdatedData() throws java.lang.Throwable {
            this.payloads.timedNoOutdatedData.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySelectorReturnsNull() throws java.lang.Throwable {
            this.payloads.replaySelectorReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySelectorConnectableReturnsNull() throws java.lang.Throwable {
            this.payloads.replaySelectorConnectableReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noHeadRetentionCompleteSize() throws java.lang.Throwable {
            this.payloads.noHeadRetentionCompleteSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noHeadRetentionErrorSize() throws java.lang.Throwable {
            this.payloads.noHeadRetentionErrorSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noHeadRetentionSize() throws java.lang.Throwable {
            this.payloads.noHeadRetentionSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noHeadRetentionCompleteTime() throws java.lang.Throwable {
            this.payloads.noHeadRetentionCompleteTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noHeadRetentionErrorTime() throws java.lang.Throwable {
            this.payloads.noHeadRetentionErrorTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noHeadRetentionTime() throws java.lang.Throwable {
            this.payloads.noHeadRetentionTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noBoundedRetentionViaThreadLocal() throws java.lang.Throwable {
            this.payloads.noBoundedRetentionViaThreadLocal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sizeBoundEagerTruncate() throws java.lang.Throwable {
            this.payloads.sizeBoundEagerTruncate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeBoundEagerTruncate() throws java.lang.Throwable {
            this.payloads.timeBoundEagerTruncate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeAndSizeBoundEagerTruncate() throws java.lang.Throwable {
            this.payloads.timeAndSizeBoundEagerTruncate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sizeBoundSelectorEagerTruncate() throws java.lang.Throwable {
            this.payloads.sizeBoundSelectorEagerTruncate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeBoundSelectorEagerTruncate() throws java.lang.Throwable {
            this.payloads.timeBoundSelectorEagerTruncate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeAndSizeSelectorBoundEagerTruncate() throws java.lang.Throwable {
            this.payloads.timeAndSizeSelectorBoundEagerTruncate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeAndSizeNoTerminalTruncationOnTimechange() throws java.lang.Throwable {
            this.payloads.timeAndSizeNoTerminalTruncationOnTimechange.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeNoNeedForResetSizeBound() throws java.lang.Throwable {
            this.payloads.disposeNoNeedForResetSizeBound.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeNoNeedForResetTimeBound() throws java.lang.Throwable {
            this.payloads.disposeNoNeedForResetTimeBound.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeNoNeedForResetTimeAndSIzeBound() throws java.lang.Throwable {
            this.payloads.disposeNoNeedForResetTimeAndSIzeBound.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableReplayEagerTruncateTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableReplayEagerTruncateTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableReplayEagerTruncateTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableReplayEagerTruncateTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableReplayEagerTruncateTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableReplayEagerTruncateTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableReplayEagerTruncateTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableReplayEagerTruncateTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement bufferedReplay;

            public org.junit.runners.model.Statement bufferedWindowReplay;

            public org.junit.runners.model.Statement windowedReplay;

            public org.junit.runners.model.Statement replaySelector;

            public org.junit.runners.model.Statement bufferedReplaySelector;

            public org.junit.runners.model.Statement windowedReplaySelector;

            public org.junit.runners.model.Statement bufferedReplayError;

            public org.junit.runners.model.Statement windowedReplayError;

            public org.junit.runners.model.Statement synchronousDisconnect;

            public org.junit.runners.model.Statement issue2191_UnsubscribeSource;

            public org.junit.runners.model.Statement issue2191_SchedulerUnsubscribe;

            public org.junit.runners.model.Statement issue2191_SchedulerUnsubscribeOnError;

            public org.junit.runners.model.Statement boundedReplayBuffer;

            public org.junit.runners.model.Statement timedAndSizedTruncation;

            public org.junit.runners.model.Statement timedAndSizedTruncationError;

            public org.junit.runners.model.Statement sizedTruncation;

            public org.junit.runners.model.Statement coldReplayNoBackpressure;

            public org.junit.runners.model.Statement cache;

            public org.junit.runners.model.Statement unsubscribeSource;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement async;

            public org.junit.runners.model.Statement asyncComeAndGo;

            public org.junit.runners.model.Statement noMissingBackpressureException;

            public org.junit.runners.model.Statement valuesAndThenError;

            public org.junit.runners.model.Statement replayTime;

            public org.junit.runners.model.Statement replaySizeAndTime;

            public org.junit.runners.model.Statement replaySelectorTime;

            public org.junit.runners.model.Statement replayMaxInt;

            public org.junit.runners.model.Statement source;

            public org.junit.runners.model.Statement connectRace;

            public org.junit.runners.model.Statement subscribeRace;

            public org.junit.runners.model.Statement addRemoveRace;

            public org.junit.runners.model.Statement cancelOnArrival;

            public org.junit.runners.model.Statement cancelOnArrival2;

            public org.junit.runners.model.Statement connectConsumerThrows;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement subscribeOnNextRace;

            public org.junit.runners.model.Statement unsubscribeOnNextRace;

            public org.junit.runners.model.Statement unsubscribeReplayRace;

            public org.junit.runners.model.Statement reentrantOnNext;

            public org.junit.runners.model.Statement reentrantOnNextBound;

            public org.junit.runners.model.Statement reentrantOnNextCancel;

            public org.junit.runners.model.Statement reentrantOnNextCancelBounded;

            public org.junit.runners.model.Statement delayedUpstreamOnSubscribe;

            public org.junit.runners.model.Statement timedNoOutdatedData;

            public org.junit.runners.model.Statement replaySelectorReturnsNull;

            public org.junit.runners.model.Statement replaySelectorConnectableReturnsNull;

            public org.junit.runners.model.Statement noHeadRetentionCompleteSize;

            public org.junit.runners.model.Statement noHeadRetentionErrorSize;

            public org.junit.runners.model.Statement noHeadRetentionSize;

            public org.junit.runners.model.Statement noHeadRetentionCompleteTime;

            public org.junit.runners.model.Statement noHeadRetentionErrorTime;

            public org.junit.runners.model.Statement noHeadRetentionTime;

            public org.junit.runners.model.Statement noBoundedRetentionViaThreadLocal;

            public org.junit.runners.model.Statement sizeBoundEagerTruncate;

            public org.junit.runners.model.Statement timeBoundEagerTruncate;

            public org.junit.runners.model.Statement timeAndSizeBoundEagerTruncate;

            public org.junit.runners.model.Statement sizeBoundSelectorEagerTruncate;

            public org.junit.runners.model.Statement timeBoundSelectorEagerTruncate;

            public org.junit.runners.model.Statement timeAndSizeSelectorBoundEagerTruncate;

            public org.junit.runners.model.Statement timeAndSizeNoTerminalTruncationOnTimechange;

            public org.junit.runners.model.Statement disposeNoNeedForResetSizeBound;

            public org.junit.runners.model.Statement disposeNoNeedForResetTimeBound;

            public org.junit.runners.model.Statement disposeNoNeedForResetTimeAndSIzeBound;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.bufferedReplay = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::bufferedReplay, "bufferedReplay", this);
            this.payloads.bufferedWindowReplay = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::bufferedWindowReplay, "bufferedWindowReplay", this);
            this.payloads.windowedReplay = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::windowedReplay, "windowedReplay", this);
            this.payloads.replaySelector = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::replaySelector, "replaySelector", this);
            this.payloads.bufferedReplaySelector = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::bufferedReplaySelector, "bufferedReplaySelector", this);
            this.payloads.windowedReplaySelector = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::windowedReplaySelector, "windowedReplaySelector", this);
            this.payloads.bufferedReplayError = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::bufferedReplayError, "bufferedReplayError", this);
            this.payloads.windowedReplayError = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::windowedReplayError, "windowedReplayError", this);
            this.payloads.synchronousDisconnect = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::synchronousDisconnect, "synchronousDisconnect", this);
            this.payloads.issue2191_UnsubscribeSource = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::issue2191_UnsubscribeSource, "issue2191_UnsubscribeSource", this);
            this.payloads.issue2191_SchedulerUnsubscribe = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::issue2191_SchedulerUnsubscribe, "issue2191_SchedulerUnsubscribe", this);
            this.payloads.issue2191_SchedulerUnsubscribeOnError = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::issue2191_SchedulerUnsubscribeOnError, "issue2191_SchedulerUnsubscribeOnError", this);
            this.payloads.boundedReplayBuffer = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::boundedReplayBuffer, "boundedReplayBuffer", this);
            this.payloads.timedAndSizedTruncation = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::timedAndSizedTruncation, "timedAndSizedTruncation", this);
            this.payloads.timedAndSizedTruncationError = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::timedAndSizedTruncationError, "timedAndSizedTruncationError", this);
            this.payloads.sizedTruncation = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::sizedTruncation, "sizedTruncation", this);
            this.payloads.coldReplayNoBackpressure = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::coldReplayNoBackpressure, "coldReplayNoBackpressure", this);
            this.payloads.cache = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::cache, "cache", this);
            this.payloads.unsubscribeSource = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::unsubscribeSource, "unsubscribeSource", this);
            this.payloads.take = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::take, "take", this);
            this.payloads.async = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::async, "async", this);
            this.payloads.asyncComeAndGo = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::asyncComeAndGo, "asyncComeAndGo", this);
            this.payloads.noMissingBackpressureException = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::noMissingBackpressureException, "noMissingBackpressureException", this);
            this.payloads.valuesAndThenError = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::valuesAndThenError, "valuesAndThenError", this);
            this.payloads.replayTime = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::replayTime, "replayTime", this);
            this.payloads.replaySizeAndTime = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::replaySizeAndTime, "replaySizeAndTime", this);
            this.payloads.replaySelectorTime = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::replaySelectorTime, "replaySelectorTime", this);
            this.payloads.replayMaxInt = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::replayMaxInt, "replayMaxInt", this);
            this.payloads.source = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::source, "source", this);
            this.payloads.connectRace = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::connectRace, "connectRace", this);
            this.payloads.subscribeRace = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::subscribeRace, "subscribeRace", this);
            this.payloads.addRemoveRace = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::addRemoveRace, "addRemoveRace", this);
            this.payloads.cancelOnArrival = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::cancelOnArrival, "cancelOnArrival", this);
            this.payloads.cancelOnArrival2 = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::cancelOnArrival2, "cancelOnArrival2", this);
            this.payloads.connectConsumerThrows = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::connectConsumerThrows, "connectConsumerThrows", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::badSource, "badSource", this);
            this.payloads.subscribeOnNextRace = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::subscribeOnNextRace, "subscribeOnNextRace", this);
            this.payloads.unsubscribeOnNextRace = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::unsubscribeOnNextRace, "unsubscribeOnNextRace", this);
            this.payloads.unsubscribeReplayRace = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::unsubscribeReplayRace, "unsubscribeReplayRace", this);
            this.payloads.reentrantOnNext = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::reentrantOnNext, "reentrantOnNext", this);
            this.payloads.reentrantOnNextBound = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::reentrantOnNextBound, "reentrantOnNextBound", this);
            this.payloads.reentrantOnNextCancel = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::reentrantOnNextCancel, "reentrantOnNextCancel", this);
            this.payloads.reentrantOnNextCancelBounded = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::reentrantOnNextCancelBounded, "reentrantOnNextCancelBounded", this);
            this.payloads.delayedUpstreamOnSubscribe = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::delayedUpstreamOnSubscribe, "delayedUpstreamOnSubscribe", this);
            this.payloads.timedNoOutdatedData = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::timedNoOutdatedData, "timedNoOutdatedData", this);
            this.payloads.replaySelectorReturnsNull = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::replaySelectorReturnsNull, "replaySelectorReturnsNull", this);
            this.payloads.replaySelectorConnectableReturnsNull = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::replaySelectorConnectableReturnsNull, "replaySelectorConnectableReturnsNull", this);
            this.payloads.noHeadRetentionCompleteSize = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::noHeadRetentionCompleteSize, "noHeadRetentionCompleteSize", this);
            this.payloads.noHeadRetentionErrorSize = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::noHeadRetentionErrorSize, "noHeadRetentionErrorSize", this);
            this.payloads.noHeadRetentionSize = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::noHeadRetentionSize, "noHeadRetentionSize", this);
            this.payloads.noHeadRetentionCompleteTime = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::noHeadRetentionCompleteTime, "noHeadRetentionCompleteTime", this);
            this.payloads.noHeadRetentionErrorTime = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::noHeadRetentionErrorTime, "noHeadRetentionErrorTime", this);
            this.payloads.noHeadRetentionTime = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::noHeadRetentionTime, "noHeadRetentionTime", this);
            this.payloads.noBoundedRetentionViaThreadLocal = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::noBoundedRetentionViaThreadLocal, "noBoundedRetentionViaThreadLocal", this);
            this.payloads.sizeBoundEagerTruncate = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::sizeBoundEagerTruncate, "sizeBoundEagerTruncate", this);
            this.payloads.timeBoundEagerTruncate = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::timeBoundEagerTruncate, "timeBoundEagerTruncate", this);
            this.payloads.timeAndSizeBoundEagerTruncate = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::timeAndSizeBoundEagerTruncate, "timeAndSizeBoundEagerTruncate", this);
            this.payloads.sizeBoundSelectorEagerTruncate = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::sizeBoundSelectorEagerTruncate, "sizeBoundSelectorEagerTruncate", this);
            this.payloads.timeBoundSelectorEagerTruncate = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::timeBoundSelectorEagerTruncate, "timeBoundSelectorEagerTruncate", this);
            this.payloads.timeAndSizeSelectorBoundEagerTruncate = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::timeAndSizeSelectorBoundEagerTruncate, "timeAndSizeSelectorBoundEagerTruncate", this);
            this.payloads.timeAndSizeNoTerminalTruncationOnTimechange = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::timeAndSizeNoTerminalTruncationOnTimechange, "timeAndSizeNoTerminalTruncationOnTimechange", this);
            this.payloads.disposeNoNeedForResetSizeBound = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::disposeNoNeedForResetSizeBound, "disposeNoNeedForResetSizeBound", this);
            this.payloads.disposeNoNeedForResetTimeBound = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::disposeNoNeedForResetTimeBound, "disposeNoNeedForResetTimeBound", this);
            this.payloads.disposeNoNeedForResetTimeAndSIzeBound = _ClassStatement.forPayload(ObservableReplayEagerTruncateTest::disposeNoNeedForResetTimeAndSIzeBound, "disposeNoNeedForResetTimeAndSIzeBound", this);
        }
    }
}
