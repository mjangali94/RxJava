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
import java.lang.management.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.flowables.ConnectableFlowable;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.fuseable.HasUpstreamPublisher;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableReplay.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableReplayEagerTruncateTest extends RxJavaTest {

    @Test
    public void bufferedReplay() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        ConnectableFlowable<Integer> cf = source.replay(3, true);
        cf.connect();
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            cf.subscribe(subscriber1);
            source.onNext(1);
            source.onNext(2);
            source.onNext(3);
            inOrder.verify(subscriber1, times(1)).onNext(1);
            inOrder.verify(subscriber1, times(1)).onNext(2);
            inOrder.verify(subscriber1, times(1)).onNext(3);
            source.onNext(4);
            source.onComplete();
            inOrder.verify(subscriber1, times(1)).onNext(4);
            inOrder.verify(subscriber1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onError(any(Throwable.class));
        }
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            cf.subscribe(subscriber1);
            inOrder.verify(subscriber1, times(1)).onNext(2);
            inOrder.verify(subscriber1, times(1)).onNext(3);
            inOrder.verify(subscriber1, times(1)).onNext(4);
            inOrder.verify(subscriber1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onError(any(Throwable.class));
        }
    }

    @Test
    public void bufferedWindowReplay() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestScheduler scheduler = new TestScheduler();
        ConnectableFlowable<Integer> cf = source.replay(3, 100, TimeUnit.MILLISECONDS, scheduler, true);
        cf.connect();
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            cf.subscribe(subscriber1);
            source.onNext(1);
            scheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
            source.onNext(2);
            scheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
            source.onNext(3);
            scheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
            inOrder.verify(subscriber1, times(1)).onNext(1);
            inOrder.verify(subscriber1, times(1)).onNext(2);
            inOrder.verify(subscriber1, times(1)).onNext(3);
            source.onNext(4);
            source.onNext(5);
            scheduler.advanceTimeBy(90, TimeUnit.MILLISECONDS);
            inOrder.verify(subscriber1, times(1)).onNext(4);
            inOrder.verify(subscriber1, times(1)).onNext(5);
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onError(any(Throwable.class));
        }
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            cf.subscribe(subscriber1);
            inOrder.verify(subscriber1, times(1)).onNext(4);
            inOrder.verify(subscriber1, times(1)).onNext(5);
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onError(any(Throwable.class));
        }
    }

    @Test
    public void windowedReplay() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> source = PublishProcessor.create();
        ConnectableFlowable<Integer> cf = source.replay(100, TimeUnit.MILLISECONDS, scheduler, true);
        cf.connect();
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            cf.subscribe(subscriber1);
            source.onNext(1);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onNext(2);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onNext(3);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onComplete();
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            inOrder.verify(subscriber1, times(1)).onNext(1);
            inOrder.verify(subscriber1, times(1)).onNext(2);
            inOrder.verify(subscriber1, times(1)).onNext(3);
            inOrder.verify(subscriber1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onError(any(Throwable.class));
        }
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            cf.subscribe(subscriber1);
            inOrder.verify(subscriber1, never()).onNext(3);
            inOrder.verify(subscriber1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onError(any(Throwable.class));
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
        Function<Flowable<Integer>, Flowable<Integer>> selector = new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> t1) {
                return t1.map(dbl);
            }
        };
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<Integer> co = source.replay(selector);
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            co.subscribe(subscriber1);
            source.onNext(1);
            source.onNext(2);
            source.onNext(3);
            inOrder.verify(subscriber1, times(1)).onNext(2);
            inOrder.verify(subscriber1, times(1)).onNext(4);
            inOrder.verify(subscriber1, times(1)).onNext(6);
            source.onNext(4);
            source.onComplete();
            inOrder.verify(subscriber1, times(1)).onNext(8);
            inOrder.verify(subscriber1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onError(any(Throwable.class));
        }
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            co.subscribe(subscriber1);
            inOrder.verify(subscriber1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onError(any(Throwable.class));
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
        Function<Flowable<Integer>, Flowable<Integer>> selector = new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> t1) {
                return t1.map(dbl);
            }
        };
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<Integer> co = source.replay(selector, 3, true);
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            co.subscribe(subscriber1);
            source.onNext(1);
            source.onNext(2);
            source.onNext(3);
            inOrder.verify(subscriber1, times(1)).onNext(2);
            inOrder.verify(subscriber1, times(1)).onNext(4);
            inOrder.verify(subscriber1, times(1)).onNext(6);
            source.onNext(4);
            source.onComplete();
            inOrder.verify(subscriber1, times(1)).onNext(8);
            inOrder.verify(subscriber1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onError(any(Throwable.class));
        }
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            co.subscribe(subscriber1);
            inOrder.verify(subscriber1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onError(any(Throwable.class));
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
        Function<Flowable<Integer>, Flowable<Integer>> selector = new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> t1) {
                return t1.map(dbl);
            }
        };
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<Integer> co = source.replay(selector, 100, TimeUnit.MILLISECONDS, scheduler, true);
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            co.subscribe(subscriber1);
            source.onNext(1);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onNext(2);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onNext(3);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onComplete();
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            inOrder.verify(subscriber1, times(1)).onNext(2);
            inOrder.verify(subscriber1, times(1)).onNext(4);
            inOrder.verify(subscriber1, times(1)).onNext(6);
            inOrder.verify(subscriber1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onError(any(Throwable.class));
        }
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            co.subscribe(subscriber1);
            inOrder.verify(subscriber1, times(1)).onComplete();
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onError(any(Throwable.class));
        }
    }

    @Test
    public void bufferedReplayError() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        ConnectableFlowable<Integer> cf = source.replay(3, true);
        cf.connect();
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            cf.subscribe(subscriber1);
            source.onNext(1);
            source.onNext(2);
            source.onNext(3);
            inOrder.verify(subscriber1, times(1)).onNext(1);
            inOrder.verify(subscriber1, times(1)).onNext(2);
            inOrder.verify(subscriber1, times(1)).onNext(3);
            source.onNext(4);
            source.onError(new RuntimeException("Forced failure"));
            inOrder.verify(subscriber1, times(1)).onNext(4);
            inOrder.verify(subscriber1, times(1)).onError(any(RuntimeException.class));
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onComplete();
        }
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            cf.subscribe(subscriber1);
            inOrder.verify(subscriber1, times(1)).onNext(2);
            inOrder.verify(subscriber1, times(1)).onNext(3);
            inOrder.verify(subscriber1, times(1)).onNext(4);
            inOrder.verify(subscriber1, times(1)).onError(any(RuntimeException.class));
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onComplete();
        }
    }

    @Test
    public void windowedReplayError() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> source = PublishProcessor.create();
        ConnectableFlowable<Integer> cf = source.replay(100, TimeUnit.MILLISECONDS, scheduler, true);
        cf.connect();
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            cf.subscribe(subscriber1);
            source.onNext(1);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onNext(2);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onNext(3);
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            source.onError(new RuntimeException("Forced failure"));
            scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);
            inOrder.verify(subscriber1, times(1)).onNext(1);
            inOrder.verify(subscriber1, times(1)).onNext(2);
            inOrder.verify(subscriber1, times(1)).onNext(3);
            inOrder.verify(subscriber1, times(1)).onError(any(RuntimeException.class));
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onComplete();
        }
        {
            Subscriber<Object> subscriber1 = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber1);
            cf.subscribe(subscriber1);
            inOrder.verify(subscriber1, never()).onNext(3);
            inOrder.verify(subscriber1, times(1)).onError(any(RuntimeException.class));
            inOrder.verifyNoMoreInteractions();
            verify(subscriber1, never()).onComplete();
        }
    }

    @Test
    public void synchronousDisconnect() {
        final AtomicInteger effectCounter = new AtomicInteger();
        Flowable<Integer> source = Flowable.just(1, 2, 3, 4).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) {
                effectCounter.incrementAndGet();
                // System.out.println("Sideeffect #" + v);
            }
        });
        Flowable<Integer> result = source.replay(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> f) {
                return f.take(2);
            }
        });
        for (int i = 1; i < 3; i++) {
            effectCounter.set(0);
            // System.out.printf("- %d -%n", i);
            result.subscribe(new Consumer<Integer>() {

                @Override
                public void accept(Integer t1) {
                    // System.out.println(t1);
                }
            }, new Consumer<Throwable>() {

                @Override
                public void accept(Throwable t1) {
                    t1.printStackTrace();
                }
            }, new Action() {

                @Override
                public void run() {
                    // System.out.println("Done");
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
        Subscriber<Integer> spiedSubscriberBeforeConnect = TestHelper.mockSubscriber();
        Subscriber<Integer> spiedSubscriberAfterConnect = TestHelper.mockSubscriber();
        // Flowable under test
        Flowable<Integer> source = Flowable.just(1, 2);
        ConnectableFlowable<Integer> replay = source.doOnNext(sourceNext).doOnCancel(sourceUnsubscribed).doOnComplete(sourceCompleted).replay();
        replay.subscribe(spiedSubscriberBeforeConnect);
        replay.subscribe(spiedSubscriberBeforeConnect);
        replay.connect();
        replay.subscribe(spiedSubscriberAfterConnect);
        replay.subscribe(spiedSubscriberAfterConnect);
        verify(spiedSubscriberBeforeConnect, times(2)).onSubscribe((Subscription) any());
        verify(spiedSubscriberAfterConnect, times(2)).onSubscribe((Subscription) any());
        // verify interactions
        verify(sourceNext, times(1)).accept(1);
        verify(sourceNext, times(1)).accept(2);
        verify(sourceCompleted, times(1)).run();
        verifyObserverMock(spiedSubscriberBeforeConnect, 2, 4);
        verifyObserverMock(spiedSubscriberAfterConnect, 2, 4);
        verify(sourceUnsubscribed, never()).run();
        verifyNoMoreInteractions(sourceNext);
        verifyNoMoreInteractions(sourceCompleted);
        verifyNoMoreInteractions(sourceUnsubscribed);
        verifyNoMoreInteractions(spiedSubscriberBeforeConnect);
        verifyNoMoreInteractions(spiedSubscriberAfterConnect);
    }

    /**
     * Specifically test interaction with a Scheduler with subscribeOn.
     *
     * @throws Throwable functional interfaces declare throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void issue2191_SchedulerUnsubscribe() throws Throwable {
        // setup mocks
        Consumer<Integer> sourceNext = mock(Consumer.class);
        Action sourceCompleted = mock(Action.class);
        Action sourceUnsubscribed = mock(Action.class);
        final Scheduler mockScheduler = mock(Scheduler.class);
        final Disposable mockSubscription = mock(Disposable.class);
        Worker spiedWorker = workerSpy(mockSubscription);
        Subscriber<Integer> mockObserverBeforeConnect = TestHelper.mockSubscriber();
        Subscriber<Integer> mockObserverAfterConnect = TestHelper.mockSubscriber();
        when(mockScheduler.createWorker()).thenReturn(spiedWorker);
        // Flowable under test
        ConnectableFlowable<Integer> replay = Flowable.just(1, 2, 3).doOnNext(sourceNext).doOnCancel(sourceUnsubscribed).doOnComplete(sourceCompleted).subscribeOn(mockScheduler).replay();
        replay.subscribe(mockObserverBeforeConnect);
        replay.subscribe(mockObserverBeforeConnect);
        replay.connect();
        replay.subscribe(mockObserverAfterConnect);
        replay.subscribe(mockObserverAfterConnect);
        verify(mockObserverBeforeConnect, times(2)).onSubscribe((Subscription) any());
        verify(mockObserverAfterConnect, times(2)).onSubscribe((Subscription) any());
        // verify interactions
        verify(sourceNext, times(1)).accept(1);
        verify(sourceNext, times(1)).accept(2);
        verify(sourceNext, times(1)).accept(3);
        verify(sourceCompleted, times(1)).run();
        verify(mockScheduler, times(1)).createWorker();
        verify(spiedWorker, times(1)).schedule((Runnable) notNull());
        verifyObserverMock(mockObserverBeforeConnect, 2, 6);
        verifyObserverMock(mockObserverAfterConnect, 2, 6);
        // FIXME publish calls cancel too
        verify(spiedWorker, times(1)).dispose();
        verify(sourceUnsubscribed, never()).run();
        verifyNoMoreInteractions(sourceNext);
        verifyNoMoreInteractions(sourceCompleted);
        verifyNoMoreInteractions(sourceUnsubscribed);
        verifyNoMoreInteractions(spiedWorker);
        verifyNoMoreInteractions(mockSubscription);
        verifyNoMoreInteractions(mockScheduler);
        verifyNoMoreInteractions(mockObserverBeforeConnect);
        verifyNoMoreInteractions(mockObserverAfterConnect);
    }

    /**
     * Specifically test interaction with a Scheduler with subscribeOn.
     *
     * @throws Throwable functional interfaces declare throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void issue2191_SchedulerUnsubscribeOnError() throws Throwable {
        // setup mocks
        Consumer<Integer> sourceNext = mock(Consumer.class);
        Action sourceCompleted = mock(Action.class);
        Consumer<Throwable> sourceError = mock(Consumer.class);
        Action sourceUnsubscribed = mock(Action.class);
        final Scheduler mockScheduler = mock(Scheduler.class);
        final Disposable mockSubscription = mock(Disposable.class);
        Worker spiedWorker = workerSpy(mockSubscription);
        Subscriber<Integer> mockObserverBeforeConnect = TestHelper.mockSubscriber();
        Subscriber<Integer> mockObserverAfterConnect = TestHelper.mockSubscriber();
        when(mockScheduler.createWorker()).thenReturn(spiedWorker);
        // Flowable under test
        Function<Integer, Integer> mockFunc = mock(Function.class);
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException();
        when(mockFunc.apply(1)).thenReturn(1);
        when(mockFunc.apply(2)).thenThrow(illegalArgumentException);
        ConnectableFlowable<Integer> replay = Flowable.just(1, 2, 3).map(mockFunc).doOnNext(sourceNext).doOnCancel(sourceUnsubscribed).doOnComplete(sourceCompleted).doOnError(sourceError).subscribeOn(mockScheduler).replay();
        replay.subscribe(mockObserverBeforeConnect);
        replay.subscribe(mockObserverBeforeConnect);
        replay.connect();
        replay.subscribe(mockObserverAfterConnect);
        replay.subscribe(mockObserverAfterConnect);
        verify(mockObserverBeforeConnect, times(2)).onSubscribe((Subscription) any());
        verify(mockObserverAfterConnect, times(2)).onSubscribe((Subscription) any());
        // verify interactions
        verify(mockScheduler, times(1)).createWorker();
        verify(spiedWorker, times(1)).schedule((Runnable) notNull());
        verify(sourceNext, times(1)).accept(1);
        verify(sourceError, times(1)).accept(illegalArgumentException);
        verifyObserver(mockObserverBeforeConnect, 2, 2, illegalArgumentException);
        verifyObserver(mockObserverAfterConnect, 2, 2, illegalArgumentException);
        // FIXME publish also calls cancel
        verify(spiedWorker, times(1)).dispose();
        verify(sourceUnsubscribed, never()).run();
        verifyNoMoreInteractions(sourceNext);
        verifyNoMoreInteractions(sourceCompleted);
        verifyNoMoreInteractions(sourceError);
        verifyNoMoreInteractions(sourceUnsubscribed);
        verifyNoMoreInteractions(spiedWorker);
        verifyNoMoreInteractions(mockSubscription);
        verifyNoMoreInteractions(mockScheduler);
        verifyNoMoreInteractions(mockObserverBeforeConnect);
        verifyNoMoreInteractions(mockObserverAfterConnect);
    }

    private static void verifyObserverMock(Subscriber<Integer> mock, int numSubscriptions, int numItemsExpected) {
        verify(mock, times(numItemsExpected)).onNext((Integer) notNull());
        verify(mock, times(numSubscriptions)).onComplete();
        verifyNoMoreInteractions(mock);
    }

    private static void verifyObserver(Subscriber<Integer> mock, int numSubscriptions, int numItemsExpected, Throwable error) {
        verify(mock, times(numItemsExpected)).onNext((Integer) notNull());
        verify(mock, times(numSubscriptions)).onError(error);
        verifyNoMoreInteractions(mock);
    }

    public static Worker workerSpy(final Disposable mockDisposable) {
        return spy(new InprocessWorker(mockDisposable));
    }

    private static class InprocessWorker extends Worker {

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
        BoundedReplayBuffer<Integer> buf = new BoundedReplayBuffer<Integer>(true) {

            private static final long serialVersionUID = -9081211580719235896L;

            @Override
            void truncate() {
            }
        };
        buf.addLast(new Node(1, 0));
        buf.addLast(new Node(2, 1));
        buf.addLast(new Node(3, 2));
        buf.addLast(new Node(4, 3));
        buf.addLast(new Node(5, 4));
        List<Integer> values = new ArrayList<>();
        buf.collect(values);
        Assert.assertEquals(Arrays.asList(1, 2, 3, 4, 5), values);
        buf.removeSome(2);
        buf.removeFirst();
        buf.removeSome(2);
        values.clear();
        buf.collect(values);
        Assert.assertTrue(values.isEmpty());
        buf.addLast(new Node(5, 5));
        buf.addLast(new Node(6, 6));
        buf.collect(values);
        Assert.assertEquals(Arrays.asList(5, 6), values);
    }

    @Test
    public void timedAndSizedTruncation() {
        TestScheduler test = new TestScheduler();
        SizeAndTimeBoundReplayBuffer<Integer> buf = new SizeAndTimeBoundReplayBuffer<>(2, 2000, TimeUnit.MILLISECONDS, test, true);
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
        test.advanceTimeBy(2, TimeUnit.SECONDS);
        buf.complete();
        values.clear();
        buf.collect(values);
        Assert.assertTrue(values.isEmpty());
        Assert.assertEquals(1, buf.size);
        Assert.assertTrue(buf.hasCompleted());
    }

    @Test
    public void backpressure() {
        final AtomicLong requested = new AtomicLong();
        Flowable<Integer> source = Flowable.range(1, 1000).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long t) {
                requested.addAndGet(t);
            }
        });
        ConnectableFlowable<Integer> cf = source.replay();
        TestSubscriberEx<Integer> ts1 = new TestSubscriberEx<>(10L);
        TestSubscriberEx<Integer> ts2 = new TestSubscriberEx<>(90L);
        cf.subscribe(ts1);
        cf.subscribe(ts2);
        ts2.request(10);
        cf.connect();
        ts1.assertValueCount(10);
        ts1.assertNotTerminated();
        ts2.assertValueCount(100);
        ts2.assertNotTerminated();
        Assert.assertEquals(100, requested.get());
    }

    @Test
    public void backpressureBounded() {
        final AtomicLong requested = new AtomicLong();
        Flowable<Integer> source = Flowable.range(1, 1000).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long t) {
                requested.addAndGet(t);
            }
        });
        ConnectableFlowable<Integer> cf = source.replay(50, true);
        TestSubscriberEx<Integer> ts1 = new TestSubscriberEx<>(10L);
        TestSubscriberEx<Integer> ts2 = new TestSubscriberEx<>(90L);
        cf.subscribe(ts1);
        cf.subscribe(ts2);
        ts2.request(10);
        cf.connect();
        ts1.assertValueCount(10);
        ts1.assertNotTerminated();
        ts2.assertValueCount(100);
        ts2.assertNotTerminated();
        Assert.assertEquals(100, requested.get());
    }

    @Test
    public void coldReplayNoBackpressure() {
        Flowable<Integer> source = Flowable.range(0, 1000).replay().autoConnect();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        source.subscribe(ts);
        ts.assertNoErrors();
        ts.assertTerminated();
        List<Integer> onNextEvents = ts.values();
        assertEquals(1000, onNextEvents.size());
        for (int i = 0; i < 1000; i++) {
            assertEquals((Integer) i, onNextEvents.get(i));
        }
    }

    @Test
    public void coldReplayBackpressure() {
        Flowable<Integer> source = Flowable.range(0, 1000).replay().autoConnect();
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        ts.request(10);
        source.subscribe(ts);
        ts.assertNoErrors();
        ts.assertNotComplete();
        List<Integer> onNextEvents = ts.values();
        assertEquals(10, onNextEvents.size());
        for (int i = 0; i < 10; i++) {
            assertEquals((Integer) i, onNextEvents.get(i));
        }
        ts.cancel();
    }

    @Test
    public void cache() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        Flowable<String> f = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(final Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        counter.incrementAndGet();
                        // System.out.println("published observable being executed");
                        subscriber.onNext("one");
                        subscriber.onComplete();
                    }
                }).start();
            }
        }).replay().autoConnect();
        // we then expect the following 2 subscriptions to get that same value
        final CountDownLatch latch = new CountDownLatch(2);
        // subscribe once
        f.subscribe(new Consumer<String>() {

            @Override
            public void accept(String v) {
                assertEquals("one", v);
                // System.out.println("v: " + v);
                latch.countDown();
            }
        });
        // subscribe again
        f.subscribe(new Consumer<String>() {

            @Override
            public void accept(String v) {
                assertEquals("one", v);
                // System.out.println("v: " + v);
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
        Flowable<Integer> f = Flowable.just(1).doOnCancel(unsubscribe).replay().autoConnect();
        f.subscribe();
        f.subscribe();
        f.subscribe();
        verify(unsubscribe, never()).run();
    }

    @Test
    public void take() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Flowable<Integer> cached = Flowable.range(1, 100).replay().autoConnect();
        cached.take(10).subscribe(ts);
        ts.assertNoErrors();
        ts.assertTerminated();
        ts.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void async() {
        Flowable<Integer> source = Flowable.range(1, 10000);
        for (int i = 0; i < 100; i++) {
            TestSubscriberEx<Integer> ts1 = new TestSubscriberEx<>();
            Flowable<Integer> cached = source.replay().autoConnect();
            cached.observeOn(Schedulers.computation()).subscribe(ts1);
            ts1.awaitDone(2, TimeUnit.SECONDS);
            ts1.assertNoErrors();
            ts1.assertTerminated();
            assertEquals(10000, ts1.values().size());
            TestSubscriberEx<Integer> ts2 = new TestSubscriberEx<>();
            cached.observeOn(Schedulers.computation()).subscribe(ts2);
            ts2.awaitDone(2, TimeUnit.SECONDS);
            ts2.assertNoErrors();
            ts2.assertTerminated();
            assertEquals(10000, ts2.values().size());
        }
    }

    @Test
    public void asyncComeAndGo() {
        Flowable<Long> source = Flowable.interval(1, 1, TimeUnit.MILLISECONDS).take(1000).subscribeOn(Schedulers.io());
        Flowable<Long> cached = source.replay().autoConnect();
        Flowable<Long> output = cached.observeOn(Schedulers.computation(), false, 1024);
        List<TestSubscriberEx<Long>> list = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            TestSubscriberEx<Long> ts = new TestSubscriberEx<>();
            list.add(ts);
            output.skip(i * 10).take(10).subscribe(ts);
        }
        List<Long> expected = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            expected.add((long) (i - 10));
        }
        int j = 0;
        for (TestSubscriberEx<Long> ts : list) {
            ts.awaitDone(3, TimeUnit.SECONDS);
            ts.assertNoErrors();
            ts.assertTerminated();
            for (int i = j * 10; i < j * 10 + 10; i++) {
                expected.set(i - j * 10, (long) i);
            }
            ts.assertValueSequence(expected);
            j++;
        }
    }

    @Test
    public void noMissingBackpressureException() {
        final int m = 4 * 1000 * 1000;
        Flowable<Integer> firehose = Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> t) {
                t.onSubscribe(new BooleanSubscription());
                for (int i = 0; i < m; i++) {
                    t.onNext(i);
                }
                t.onComplete();
            }
        });
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        firehose.replay().autoConnect().observeOn(Schedulers.computation()).takeLast(100).subscribe(ts);
        ts.awaitDone(3, TimeUnit.SECONDS);
        ts.assertNoErrors();
        ts.assertTerminated();
        assertEquals(100, ts.values().size());
    }

    @Test
    public void valuesAndThenError() {
        Flowable<Integer> source = Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())).replay().autoConnect();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        source.subscribe(ts);
        ts.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        ts.assertNotComplete();
        Assert.assertEquals(1, ts.errors().size());
        TestSubscriberEx<Integer> ts2 = new TestSubscriberEx<>();
        source.subscribe(ts2);
        ts2.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        ts2.assertNotComplete();
        Assert.assertEquals(1, ts2.errors().size());
    }

    @Test
    public void unsafeChildThrows() {
        final AtomicInteger count = new AtomicInteger();
        Flowable<Integer> source = Flowable.range(1, 100).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                count.getAndIncrement();
            }
        }).replay().autoConnect();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                throw new TestException();
            }
        };
        source.subscribe(ts);
        Assert.assertEquals(100, count.get());
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(TestException.class);
    }

    @Test
    public void unboundedLeavesEarly() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final List<Long> requests = new ArrayList<>();
        Flowable<Integer> out = source.doOnRequest(new LongConsumer() {

            @Override
            public void accept(long t) {
                requests.add(t);
            }
        }).replay().autoConnect();
        TestSubscriber<Integer> ts1 = new TestSubscriber<>(5L);
        TestSubscriber<Integer> ts2 = new TestSubscriber<>(10L);
        out.subscribe(ts1);
        out.subscribe(ts2);
        ts2.cancel();
        Assert.assertEquals(Arrays.asList(5L, 5L), requests);
    }

    @Test
    public void subscribersComeAndGoAtRequestBoundaries() {
        ConnectableFlowable<Integer> source = Flowable.range(1, 10).replay(1, true);
        source.connect();
        TestSubscriber<Integer> ts1 = new TestSubscriber<>(2L);
        source.subscribe(ts1);
        ts1.assertValues(1, 2);
        ts1.assertNoErrors();
        ts1.cancel();
        TestSubscriber<Integer> ts2 = new TestSubscriber<>(2L);
        source.subscribe(ts2);
        ts2.assertValues(2, 3);
        ts2.assertNoErrors();
        ts2.cancel();
        TestSubscriber<Integer> ts21 = new TestSubscriber<>(1L);
        source.subscribe(ts21);
        ts21.assertValues(3);
        ts21.assertNoErrors();
        ts21.cancel();
        TestSubscriber<Integer> ts22 = new TestSubscriber<>(1L);
        source.subscribe(ts22);
        ts22.assertValues(3);
        ts22.assertNoErrors();
        ts22.cancel();
        TestSubscriber<Integer> ts3 = new TestSubscriber<>();
        source.subscribe(ts3);
        ts3.assertNoErrors();
        // System.out.println(ts3.values());
        ts3.assertValues(3, 4, 5, 6, 7, 8, 9, 10);
        ts3.assertComplete();
    }

    @Test
    public void subscribersComeAndGoAtRequestBoundaries2() {
        ConnectableFlowable<Integer> source = Flowable.range(1, 10).replay(2, true);
        source.connect();
        TestSubscriber<Integer> ts1 = new TestSubscriber<>(2L);
        source.subscribe(ts1);
        ts1.assertValues(1, 2);
        ts1.assertNoErrors();
        ts1.cancel();
        TestSubscriber<Integer> ts11 = new TestSubscriber<>(2L);
        source.subscribe(ts11);
        ts11.assertValues(1, 2);
        ts11.assertNoErrors();
        ts11.cancel();
        TestSubscriber<Integer> ts2 = new TestSubscriber<>(3L);
        source.subscribe(ts2);
        ts2.assertValues(1, 2, 3);
        ts2.assertNoErrors();
        ts2.cancel();
        TestSubscriber<Integer> ts21 = new TestSubscriber<>(1L);
        source.subscribe(ts21);
        ts21.assertValues(2);
        ts21.assertNoErrors();
        ts21.cancel();
        TestSubscriber<Integer> ts22 = new TestSubscriber<>(1L);
        source.subscribe(ts22);
        ts22.assertValues(2);
        ts22.assertNoErrors();
        ts22.cancel();
        TestSubscriber<Integer> ts3 = new TestSubscriber<>();
        source.subscribe(ts3);
        ts3.assertNoErrors();
        // System.out.println(ts3.values());
        ts3.assertValues(2, 3, 4, 5, 6, 7, 8, 9, 10);
        ts3.assertComplete();
    }

    @Test
    public void replayTime() {
        Flowable.just(1).replay(1, TimeUnit.MINUTES, Schedulers.computation(), true).autoConnect().test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void replaySizeAndTime() {
        Flowable.just(1).replay(1, 1, TimeUnit.MILLISECONDS, Schedulers.computation(), true).autoConnect().test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void replaySelectorTime() {
        Flowable.just(1).replay(Functions.<Flowable<Integer>>identity(), 1, TimeUnit.MINUTES, Schedulers.computation(), true).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void source() {
        Flowable<Integer> source = Flowable.range(1, 3);
        assertSame(source, (((HasUpstreamPublisher<?>) source.replay())).source());
    }

    @Test
    public void connectRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ConnectableFlowable<Integer> cf = Flowable.range(1, 3).replay();
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
    public void subscribeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ConnectableFlowable<Integer> cf = Flowable.range(1, 3).replay();
            final TestSubscriber<Integer> ts1 = new TestSubscriber<>();
            final TestSubscriber<Integer> ts2 = new TestSubscriber<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    cf.subscribe(ts1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    cf.subscribe(ts2);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void addRemoveRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ConnectableFlowable<Integer> cf = Flowable.range(1, 3).replay();
            final TestSubscriber<Integer> ts1 = new TestSubscriber<>();
            final TestSubscriber<Integer> ts2 = new TestSubscriber<>();
            cf.subscribe(ts1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts1.cancel();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    cf.subscribe(ts2);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void cancelOnArrival() {
        Flowable.range(1, 2).replay(Integer.MAX_VALUE, true).autoConnect().test(Long.MAX_VALUE, true).assertEmpty();
    }

    @Test
    public void cancelOnArrival2() {
        ConnectableFlowable<Integer> cf = PublishProcessor.<Integer>create().replay(Integer.MAX_VALUE, true);
        cf.test();
        cf.autoConnect().test(Long.MAX_VALUE, true).assertEmpty();
    }

    @Test
    public void connectConsumerThrows() {
        ConnectableFlowable<Integer> cf = Flowable.range(1, 2).replay();
        try {
            cf.connect(new Consumer<Disposable>() {

                @Override
                public void accept(Disposable t) throws Exception {
                    throw new TestException();
                }
            });
            fail("Should have thrown");
        } catch (TestException ex) {
        // expected
        }
        cf.test().assertEmpty().cancel();
        cf.connect();
        cf.test().assertResult(1, 2);
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
                    subscriber.onNext(1);
                    subscriber.onError(new TestException("Second"));
                    subscriber.onComplete();
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
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final ConnectableFlowable<Integer> cf = pp.replay();
            final TestSubscriber<Integer> ts1 = new TestSubscriber<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    cf.subscribe(ts1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    for (int j = 0; j < 1000; j++) {
                        pp.onNext(j);
                    }
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void unsubscribeOnNextRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final ConnectableFlowable<Integer> cf = pp.replay();
            final TestSubscriber<Integer> ts1 = new TestSubscriber<>();
            cf.subscribe(ts1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts1.cancel();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    for (int j = 0; j < 1000; j++) {
                        pp.onNext(j);
                    }
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void unsubscribeReplayRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ConnectableFlowable<Integer> cf = Flowable.range(1, 1000).replay();
            final TestSubscriber<Integer> ts1 = new TestSubscriber<>();
            cf.connect();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    cf.subscribe(ts1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts1.cancel();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void reentrantOnNext() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                if (t == 1) {
                    pp.onNext(2);
                    pp.onComplete();
                }
                super.onNext(t);
            }
        };
        pp.replay().autoConnect().subscribe(ts);
        pp.onNext(1);
        ts.assertResult(1, 2);
    }

    @Test
    public void reentrantOnNextBound() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                if (t == 1) {
                    pp.onNext(2);
                    pp.onComplete();
                }
                super.onNext(t);
            }
        };
        pp.replay(10, true).autoConnect().subscribe(ts);
        pp.onNext(1);
        ts.assertResult(1, 2);
    }

    @Test
    public void reentrantOnNextCancel() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                if (t == 1) {
                    pp.onNext(2);
                    cancel();
                }
                super.onNext(t);
            }
        };
        pp.replay().autoConnect().subscribe(ts);
        pp.onNext(1);
        ts.assertValues(1);
    }

    @Test
    public void reentrantOnNextCancelBounded() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                if (t == 1) {
                    pp.onNext(2);
                    cancel();
                }
                super.onNext(t);
            }
        };
        pp.replay(10, true).autoConnect().subscribe(ts);
        pp.onNext(1);
        ts.assertValues(1);
    }

    @Test
    public void replayMaxInt() {
        Flowable.range(1, 2).replay(Integer.MAX_VALUE, true).autoConnect().test().assertResult(1, 2);
    }

    @Test
    public void timedAndSizedTruncationError() {
        TestScheduler test = new TestScheduler();
        SizeAndTimeBoundReplayBuffer<Integer> buf = new SizeAndTimeBoundReplayBuffer<>(2, 2000, TimeUnit.MILLISECONDS, test, true);
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
        SizeBoundReplayBuffer<Integer> buf = new SizeBoundReplayBuffer<>(2, true);
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
    public void delayedUpstreamOnSubscribe() {
        final Subscriber<?>[] sub = { null };
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                sub[0] = s;
            }
        }.replay().connect().dispose();
        BooleanSubscription bs = new BooleanSubscription();
        sub[0].onSubscribe(bs);
        assertTrue(bs.isCancelled());
    }

    @Test
    public void timedNoOutdatedData() {
        TestScheduler scheduler = new TestScheduler();
        Flowable<Integer> source = Flowable.just(1).replay(2, TimeUnit.SECONDS, scheduler, true).autoConnect();
        source.test().assertResult(1);
        source.test().assertResult(1);
        scheduler.advanceTimeBy(3, TimeUnit.SECONDS);
        source.test().assertResult();
    }

    @Test
    public void multicastSelectorCallableConnectableCrash() {
        FlowableReplay.multicastSelector(new Supplier<ConnectableFlowable<Object>>() {

            @Override
            public ConnectableFlowable<Object> get() throws Exception {
                throw new TestException();
            }
        }, Functions.<Flowable<Object>>identity()).test().assertFailure(TestException.class);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().replay());
    }

    @Test
    public void noHeadRetentionCompleteSize() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        FlowableReplay<Integer> co = (FlowableReplay<Integer>) source.replay(1, true);
        // the backpressure coordination would not accept items from source otherwise
        co.test();
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
        PublishProcessor<Integer> source = PublishProcessor.create();
        FlowableReplay<Integer> co = (FlowableReplay<Integer>) source.replay(1, true);
        co.test();
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
        PublishProcessor<Integer> source = PublishProcessor.create();
        FlowableReplay<Integer> co = (FlowableReplay<Integer>) source.replay(1, true);
        co.test();
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
        PublishProcessor<Integer> source = PublishProcessor.create();
        FlowableReplay<Integer> co = (FlowableReplay<Integer>) source.replay(1, TimeUnit.MINUTES, Schedulers.computation(), true);
        co.test();
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
        PublishProcessor<Integer> source = PublishProcessor.create();
        FlowableReplay<Integer> co = (FlowableReplay<Integer>) source.replay(1, TimeUnit.MINUTES, Schedulers.computation(), true);
        co.test();
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
        PublishProcessor<Integer> source = PublishProcessor.create();
        FlowableReplay<Integer> co = (FlowableReplay<Integer>) source.replay(1, TimeUnit.MILLISECONDS, sch, true);
        co.test();
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

    @Test(expected = TestException.class)
    public void createBufferFactoryCrash() {
        FlowableReplay.create(Flowable.just(1), new Supplier<ReplayBuffer<Integer>>() {

            @Override
            public ReplayBuffer<Integer> get() throws Exception {
                throw new TestException();
            }
        }).connect();
    }

    @Test
    public void createBufferFactoryCrashOnSubscribe() {
        FlowableReplay.create(Flowable.just(1), new Supplier<ReplayBuffer<Integer>>() {

            @Override
            public ReplayBuffer<Integer> get() throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void noBoundedRetentionViaThreadLocal() throws Exception {
        Flowable<byte[]> source = Flowable.range(1, 200).map(new Function<Integer, byte[]>() {

            @Override
            public byte[] apply(Integer v) throws Exception {
                return new byte[1024 * 1024];
            }
        }).replay(new Function<Flowable<byte[]>, Publisher<byte[]>>() {

            @Override
            public Publisher<byte[]> apply(final Flowable<byte[]> f) throws Exception {
                return f.take(1).concatMap(new Function<byte[], Publisher<byte[]>>() {

                    @Override
                    public Publisher<byte[]> apply(byte[] v) throws Exception {
                        return f;
                    }
                });
            }
        }, 1, true).takeLast(1);
        // System.out.println("Bounded Replay Leak check: Wait before GC");
        Thread.sleep(1000);
        // System.out.println("Bounded Replay Leak check: GC");
        System.gc();
        Thread.sleep(500);
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage memHeap = memoryMXBean.getHeapMemoryUsage();
        long initial = memHeap.getUsed();
        // System.out.printf("Bounded Replay Leak check: Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        final AtomicLong after = new AtomicLong();
        source.subscribe(new Consumer<byte[]>() {

            @Override
            public void accept(byte[] v) throws Exception {
                // System.out.println("Bounded Replay Leak check: Wait before GC 2");
                Thread.sleep(1000);
                // System.out.println("Bounded Replay Leak check:  GC 2");
                System.gc();
                Thread.sleep(500);
                after.set(memoryMXBean.getHeapMemoryUsage().getUsed());
            }
        });
        // System.out.printf("Bounded Replay Leak check: After: %.3f MB%n", after.get() / 1024.0 / 1024.0);
        if (initial + 100 * 1024 * 1024 < after.get()) {
            Assert.fail("Bounded Replay Leak check: Memory leak detected: " + (initial / 1024.0 / 1024.0) + " -> " + after.get() / 1024.0 / 1024.0);
        }
    }

    @Test
    public void sizeBoundEagerTruncate() throws Exception {
        PublishProcessor<int[]> pp = PublishProcessor.create();
        ConnectableFlowable<int[]> cf = pp.replay(1, true);
        TestSubscriber<int[]> ts = cf.test();
        cf.connect();
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long initial = memoryMXBean.getHeapMemoryUsage().getUsed();
        // System.out.printf("Bounded Replay Leak check: Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        pp.onNext(new int[100 * 1024 * 1024]);
        ts.assertValueCount(1);
        ts.values().clear();
        pp.onNext(new int[0]);
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        long after = memoryMXBean.getHeapMemoryUsage().getUsed();
        ts.cancel();
        // System.out.printf("Bounded Replay Leak check: After: %.3f MB%n", after / 1024.0 / 1024.0);
        if (initial + 100 * 1024 * 1024 < after) {
            Assert.fail("Bounded Replay Leak check: Memory leak detected: " + (initial / 1024.0 / 1024.0) + " -> " + after / 1024.0 / 1024.0);
        }
    }

    @Test
    public void timeBoundEagerTruncate() throws Exception {
        PublishProcessor<int[]> pp = PublishProcessor.create();
        TestScheduler scheduler = new TestScheduler();
        ConnectableFlowable<int[]> cf = pp.replay(1, TimeUnit.SECONDS, scheduler, true);
        TestSubscriber<int[]> ts = cf.test();
        cf.connect();
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long initial = memoryMXBean.getHeapMemoryUsage().getUsed();
        // System.out.printf("Bounded Replay Leak check: Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        pp.onNext(new int[100 * 1024 * 1024]);
        ts.assertValueCount(1);
        ts.values().clear();
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        pp.onNext(new int[0]);
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        long after = memoryMXBean.getHeapMemoryUsage().getUsed();
        ts.cancel();
        // System.out.printf("Bounded Replay Leak check: After: %.3f MB%n", after / 1024.0 / 1024.0);
        if (initial + 100 * 1024 * 1024 < after) {
            Assert.fail("Bounded Replay Leak check: Memory leak detected: " + (initial / 1024.0 / 1024.0) + " -> " + after / 1024.0 / 1024.0);
        }
    }

    @Test
    public void timeAndSizeBoundEagerTruncate() throws Exception {
        PublishProcessor<int[]> pp = PublishProcessor.create();
        TestScheduler scheduler = new TestScheduler();
        ConnectableFlowable<int[]> cf = pp.replay(1, 5, TimeUnit.SECONDS, scheduler, true);
        TestSubscriber<int[]> ts = cf.test();
        cf.connect();
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long initial = memoryMXBean.getHeapMemoryUsage().getUsed();
        // System.out.printf("Bounded Replay Leak check: Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        pp.onNext(new int[100 * 1024 * 1024]);
        ts.assertValueCount(1);
        ts.values().clear();
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        pp.onNext(new int[0]);
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        long after = memoryMXBean.getHeapMemoryUsage().getUsed();
        ts.cancel();
        // System.out.printf("Bounded Replay Leak check: After: %.3f MB%n", after / 1024.0 / 1024.0);
        if (initial + 100 * 1024 * 1024 < after) {
            Assert.fail("Bounded Replay Leak check: Memory leak detected: " + (initial / 1024.0 / 1024.0) + " -> " + after / 1024.0 / 1024.0);
        }
    }

    @Test
    public void sizeBoundSelectorEagerTruncate() throws Exception {
        PublishProcessor<int[]> pp = PublishProcessor.create();
        Flowable<int[]> cf = pp.replay(Functions.<Flowable<int[]>>identity(), 1, true);
        TestSubscriber<int[]> ts = cf.test();
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long initial = memoryMXBean.getHeapMemoryUsage().getUsed();
        // System.out.printf("Bounded Replay Leak check: Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        pp.onNext(new int[100 * 1024 * 1024]);
        ts.assertValueCount(1);
        ts.values().clear();
        pp.onNext(new int[0]);
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        long after = memoryMXBean.getHeapMemoryUsage().getUsed();
        ts.cancel();
        // System.out.printf("Bounded Replay Leak check: After: %.3f MB%n", after / 1024.0 / 1024.0);
        if (initial + 100 * 1024 * 1024 < after) {
            Assert.fail("Bounded Replay Leak check: Memory leak detected: " + (initial / 1024.0 / 1024.0) + " -> " + after / 1024.0 / 1024.0);
        }
    }

    @Test
    public void timeBoundSelectorEagerTruncate() throws Exception {
        PublishProcessor<int[]> pp = PublishProcessor.create();
        TestScheduler scheduler = new TestScheduler();
        Flowable<int[]> cf = pp.replay(Functions.<Flowable<int[]>>identity(), 1, TimeUnit.SECONDS, scheduler, true);
        TestSubscriber<int[]> ts = cf.test();
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long initial = memoryMXBean.getHeapMemoryUsage().getUsed();
        // System.out.printf("Bounded Replay Leak check: Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        pp.onNext(new int[100 * 1024 * 1024]);
        ts.assertValueCount(1);
        ts.values().clear();
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        pp.onNext(new int[0]);
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        long after = memoryMXBean.getHeapMemoryUsage().getUsed();
        ts.cancel();
        // System.out.printf("Bounded Replay Leak check: After: %.3f MB%n", after / 1024.0 / 1024.0);
        if (initial + 100 * 1024 * 1024 < after) {
            Assert.fail("Bounded Replay Leak check: Memory leak detected: " + (initial / 1024.0 / 1024.0) + " -> " + after / 1024.0 / 1024.0);
        }
    }

    @Test
    public void timeAndSizeBoundSelectorEagerTruncate() throws Exception {
        PublishProcessor<int[]> pp = PublishProcessor.create();
        TestScheduler scheduler = new TestScheduler();
        Flowable<int[]> cf = pp.replay(Functions.<Flowable<int[]>>identity(), 1, 5, TimeUnit.SECONDS, scheduler, true);
        TestSubscriber<int[]> ts = cf.test();
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long initial = memoryMXBean.getHeapMemoryUsage().getUsed();
        // System.out.printf("Bounded Replay Leak check: Starting: %.3f MB%n", initial / 1024.0 / 1024.0);
        pp.onNext(new int[100 * 1024 * 1024]);
        ts.assertValueCount(1);
        ts.values().clear();
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        pp.onNext(new int[0]);
        Thread.sleep(200);
        System.gc();
        Thread.sleep(200);
        long after = memoryMXBean.getHeapMemoryUsage().getUsed();
        ts.cancel();
        // System.out.printf("Bounded Replay Leak check: After: %.3f MB%n", after / 1024.0 / 1024.0);
        if (initial + 100 * 1024 * 1024 < after) {
            Assert.fail("Bounded Replay Leak check: Memory leak detected: " + (initial / 1024.0 / 1024.0) + " -> " + after / 1024.0 / 1024.0);
        }
    }

    @Test
    public void timeAndSizeNoTerminalTruncationOnTimechange() {
        Flowable.just(1).replay(1, 1, TimeUnit.SECONDS, new TimesteppingScheduler(), true).autoConnect().test().assertComplete().assertNoErrors();
    }

    @Test
    public void disposeNoNeedForResetSizeBound() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        ConnectableFlowable<Integer> cf = pp.replay(10, true);
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

    @Test
    public void disposeNoNeedForResetTimeBound() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        ConnectableFlowable<Integer> cf = pp.replay(10, TimeUnit.MINUTES, Schedulers.single(), true);
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

    @Test
    public void disposeNoNeedForResetTimeAndSIzeBound() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        ConnectableFlowable<Integer> cf = pp.replay(10, 10, TimeUnit.MINUTES, Schedulers.single(), true);
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

        private FlowableReplayEagerTruncateTest instance;

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
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.payloads.backpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureBounded() throws java.lang.Throwable {
            this.payloads.backpressureBounded.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_coldReplayNoBackpressure() throws java.lang.Throwable {
            this.payloads.coldReplayNoBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_coldReplayBackpressure() throws java.lang.Throwable {
            this.payloads.coldReplayBackpressure.evaluate();
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
        public void benchmark_unsafeChildThrows() throws java.lang.Throwable {
            this.payloads.unsafeChildThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unboundedLeavesEarly() throws java.lang.Throwable {
            this.payloads.unboundedLeavesEarly.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribersComeAndGoAtRequestBoundaries() throws java.lang.Throwable {
            this.payloads.subscribersComeAndGoAtRequestBoundaries.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribersComeAndGoAtRequestBoundaries2() throws java.lang.Throwable {
            this.payloads.subscribersComeAndGoAtRequestBoundaries2.evaluate();
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
        public void benchmark_replayMaxInt() throws java.lang.Throwable {
            this.payloads.replayMaxInt.evaluate();
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
        public void benchmark_delayedUpstreamOnSubscribe() throws java.lang.Throwable {
            this.payloads.delayedUpstreamOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedNoOutdatedData() throws java.lang.Throwable {
            this.payloads.timedNoOutdatedData.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multicastSelectorCallableConnectableCrash() throws java.lang.Throwable {
            this.payloads.multicastSelectorCallableConnectableCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
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
        public void benchmark_createBufferFactoryCrash() throws java.lang.Throwable {
            this.payloads.createBufferFactoryCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createBufferFactoryCrashOnSubscribe() throws java.lang.Throwable {
            this.payloads.createBufferFactoryCrashOnSubscribe.evaluate();
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
        public void benchmark_timeAndSizeBoundSelectorEagerTruncate() throws java.lang.Throwable {
            this.payloads.timeAndSizeBoundSelectorEagerTruncate.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReplayEagerTruncateTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReplayEagerTruncateTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReplayEagerTruncateTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReplayEagerTruncateTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableReplayEagerTruncateTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReplayEagerTruncateTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableReplayEagerTruncateTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableReplayEagerTruncateTest.class, name);
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

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement backpressureBounded;

            public org.junit.runners.model.Statement coldReplayNoBackpressure;

            public org.junit.runners.model.Statement coldReplayBackpressure;

            public org.junit.runners.model.Statement cache;

            public org.junit.runners.model.Statement unsubscribeSource;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement async;

            public org.junit.runners.model.Statement asyncComeAndGo;

            public org.junit.runners.model.Statement noMissingBackpressureException;

            public org.junit.runners.model.Statement valuesAndThenError;

            public org.junit.runners.model.Statement unsafeChildThrows;

            public org.junit.runners.model.Statement unboundedLeavesEarly;

            public org.junit.runners.model.Statement subscribersComeAndGoAtRequestBoundaries;

            public org.junit.runners.model.Statement subscribersComeAndGoAtRequestBoundaries2;

            public org.junit.runners.model.Statement replayTime;

            public org.junit.runners.model.Statement replaySizeAndTime;

            public org.junit.runners.model.Statement replaySelectorTime;

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

            public org.junit.runners.model.Statement replayMaxInt;

            public org.junit.runners.model.Statement timedAndSizedTruncationError;

            public org.junit.runners.model.Statement sizedTruncation;

            public org.junit.runners.model.Statement delayedUpstreamOnSubscribe;

            public org.junit.runners.model.Statement timedNoOutdatedData;

            public org.junit.runners.model.Statement multicastSelectorCallableConnectableCrash;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement noHeadRetentionCompleteSize;

            public org.junit.runners.model.Statement noHeadRetentionErrorSize;

            public org.junit.runners.model.Statement noHeadRetentionSize;

            public org.junit.runners.model.Statement noHeadRetentionCompleteTime;

            public org.junit.runners.model.Statement noHeadRetentionErrorTime;

            public org.junit.runners.model.Statement noHeadRetentionTime;

            public org.junit.runners.model.Statement createBufferFactoryCrash;

            public org.junit.runners.model.Statement createBufferFactoryCrashOnSubscribe;

            public org.junit.runners.model.Statement noBoundedRetentionViaThreadLocal;

            public org.junit.runners.model.Statement sizeBoundEagerTruncate;

            public org.junit.runners.model.Statement timeBoundEagerTruncate;

            public org.junit.runners.model.Statement timeAndSizeBoundEagerTruncate;

            public org.junit.runners.model.Statement sizeBoundSelectorEagerTruncate;

            public org.junit.runners.model.Statement timeBoundSelectorEagerTruncate;

            public org.junit.runners.model.Statement timeAndSizeBoundSelectorEagerTruncate;

            public org.junit.runners.model.Statement timeAndSizeNoTerminalTruncationOnTimechange;

            public org.junit.runners.model.Statement disposeNoNeedForResetSizeBound;

            public org.junit.runners.model.Statement disposeNoNeedForResetTimeBound;

            public org.junit.runners.model.Statement disposeNoNeedForResetTimeAndSIzeBound;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.bufferedReplay = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::bufferedReplay, "bufferedReplay", this);
            this.payloads.bufferedWindowReplay = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::bufferedWindowReplay, "bufferedWindowReplay", this);
            this.payloads.windowedReplay = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::windowedReplay, "windowedReplay", this);
            this.payloads.replaySelector = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::replaySelector, "replaySelector", this);
            this.payloads.bufferedReplaySelector = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::bufferedReplaySelector, "bufferedReplaySelector", this);
            this.payloads.windowedReplaySelector = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::windowedReplaySelector, "windowedReplaySelector", this);
            this.payloads.bufferedReplayError = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::bufferedReplayError, "bufferedReplayError", this);
            this.payloads.windowedReplayError = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::windowedReplayError, "windowedReplayError", this);
            this.payloads.synchronousDisconnect = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::synchronousDisconnect, "synchronousDisconnect", this);
            this.payloads.issue2191_UnsubscribeSource = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::issue2191_UnsubscribeSource, "issue2191_UnsubscribeSource", this);
            this.payloads.issue2191_SchedulerUnsubscribe = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::issue2191_SchedulerUnsubscribe, "issue2191_SchedulerUnsubscribe", this);
            this.payloads.issue2191_SchedulerUnsubscribeOnError = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::issue2191_SchedulerUnsubscribeOnError, "issue2191_SchedulerUnsubscribeOnError", this);
            this.payloads.boundedReplayBuffer = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::boundedReplayBuffer, "boundedReplayBuffer", this);
            this.payloads.timedAndSizedTruncation = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::timedAndSizedTruncation, "timedAndSizedTruncation", this);
            this.payloads.backpressure = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::backpressure, "backpressure", this);
            this.payloads.backpressureBounded = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::backpressureBounded, "backpressureBounded", this);
            this.payloads.coldReplayNoBackpressure = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::coldReplayNoBackpressure, "coldReplayNoBackpressure", this);
            this.payloads.coldReplayBackpressure = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::coldReplayBackpressure, "coldReplayBackpressure", this);
            this.payloads.cache = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::cache, "cache", this);
            this.payloads.unsubscribeSource = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::unsubscribeSource, "unsubscribeSource", this);
            this.payloads.take = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::take, "take", this);
            this.payloads.async = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::async, "async", this);
            this.payloads.asyncComeAndGo = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::asyncComeAndGo, "asyncComeAndGo", this);
            this.payloads.noMissingBackpressureException = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::noMissingBackpressureException, "noMissingBackpressureException", this);
            this.payloads.valuesAndThenError = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::valuesAndThenError, "valuesAndThenError", this);
            this.payloads.unsafeChildThrows = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::unsafeChildThrows, "unsafeChildThrows", this);
            this.payloads.unboundedLeavesEarly = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::unboundedLeavesEarly, "unboundedLeavesEarly", this);
            this.payloads.subscribersComeAndGoAtRequestBoundaries = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::subscribersComeAndGoAtRequestBoundaries, "subscribersComeAndGoAtRequestBoundaries", this);
            this.payloads.subscribersComeAndGoAtRequestBoundaries2 = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::subscribersComeAndGoAtRequestBoundaries2, "subscribersComeAndGoAtRequestBoundaries2", this);
            this.payloads.replayTime = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::replayTime, "replayTime", this);
            this.payloads.replaySizeAndTime = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::replaySizeAndTime, "replaySizeAndTime", this);
            this.payloads.replaySelectorTime = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::replaySelectorTime, "replaySelectorTime", this);
            this.payloads.source = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::source, "source", this);
            this.payloads.connectRace = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::connectRace, "connectRace", this);
            this.payloads.subscribeRace = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::subscribeRace, "subscribeRace", this);
            this.payloads.addRemoveRace = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::addRemoveRace, "addRemoveRace", this);
            this.payloads.cancelOnArrival = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::cancelOnArrival, "cancelOnArrival", this);
            this.payloads.cancelOnArrival2 = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::cancelOnArrival2, "cancelOnArrival2", this);
            this.payloads.connectConsumerThrows = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::connectConsumerThrows, "connectConsumerThrows", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::badSource, "badSource", this);
            this.payloads.subscribeOnNextRace = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::subscribeOnNextRace, "subscribeOnNextRace", this);
            this.payloads.unsubscribeOnNextRace = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::unsubscribeOnNextRace, "unsubscribeOnNextRace", this);
            this.payloads.unsubscribeReplayRace = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::unsubscribeReplayRace, "unsubscribeReplayRace", this);
            this.payloads.reentrantOnNext = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::reentrantOnNext, "reentrantOnNext", this);
            this.payloads.reentrantOnNextBound = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::reentrantOnNextBound, "reentrantOnNextBound", this);
            this.payloads.reentrantOnNextCancel = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::reentrantOnNextCancel, "reentrantOnNextCancel", this);
            this.payloads.reentrantOnNextCancelBounded = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::reentrantOnNextCancelBounded, "reentrantOnNextCancelBounded", this);
            this.payloads.replayMaxInt = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::replayMaxInt, "replayMaxInt", this);
            this.payloads.timedAndSizedTruncationError = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::timedAndSizedTruncationError, "timedAndSizedTruncationError", this);
            this.payloads.sizedTruncation = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::sizedTruncation, "sizedTruncation", this);
            this.payloads.delayedUpstreamOnSubscribe = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::delayedUpstreamOnSubscribe, "delayedUpstreamOnSubscribe", this);
            this.payloads.timedNoOutdatedData = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::timedNoOutdatedData, "timedNoOutdatedData", this);
            this.payloads.multicastSelectorCallableConnectableCrash = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::multicastSelectorCallableConnectableCrash, "multicastSelectorCallableConnectableCrash", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::badRequest, "badRequest", this);
            this.payloads.noHeadRetentionCompleteSize = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::noHeadRetentionCompleteSize, "noHeadRetentionCompleteSize", this);
            this.payloads.noHeadRetentionErrorSize = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::noHeadRetentionErrorSize, "noHeadRetentionErrorSize", this);
            this.payloads.noHeadRetentionSize = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::noHeadRetentionSize, "noHeadRetentionSize", this);
            this.payloads.noHeadRetentionCompleteTime = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::noHeadRetentionCompleteTime, "noHeadRetentionCompleteTime", this);
            this.payloads.noHeadRetentionErrorTime = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::noHeadRetentionErrorTime, "noHeadRetentionErrorTime", this);
            this.payloads.noHeadRetentionTime = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::noHeadRetentionTime, "noHeadRetentionTime", this);
            this.payloads.createBufferFactoryCrash = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableReplayEagerTruncateTest::createBufferFactoryCrash, io.reactivex.rxjava3.exceptions.TestException.class), "createBufferFactoryCrash", this);
            this.payloads.createBufferFactoryCrashOnSubscribe = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::createBufferFactoryCrashOnSubscribe, "createBufferFactoryCrashOnSubscribe", this);
            this.payloads.noBoundedRetentionViaThreadLocal = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::noBoundedRetentionViaThreadLocal, "noBoundedRetentionViaThreadLocal", this);
            this.payloads.sizeBoundEagerTruncate = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::sizeBoundEagerTruncate, "sizeBoundEagerTruncate", this);
            this.payloads.timeBoundEagerTruncate = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::timeBoundEagerTruncate, "timeBoundEagerTruncate", this);
            this.payloads.timeAndSizeBoundEagerTruncate = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::timeAndSizeBoundEagerTruncate, "timeAndSizeBoundEagerTruncate", this);
            this.payloads.sizeBoundSelectorEagerTruncate = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::sizeBoundSelectorEagerTruncate, "sizeBoundSelectorEagerTruncate", this);
            this.payloads.timeBoundSelectorEagerTruncate = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::timeBoundSelectorEagerTruncate, "timeBoundSelectorEagerTruncate", this);
            this.payloads.timeAndSizeBoundSelectorEagerTruncate = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::timeAndSizeBoundSelectorEagerTruncate, "timeAndSizeBoundSelectorEagerTruncate", this);
            this.payloads.timeAndSizeNoTerminalTruncationOnTimechange = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::timeAndSizeNoTerminalTruncationOnTimechange, "timeAndSizeNoTerminalTruncationOnTimechange", this);
            this.payloads.disposeNoNeedForResetSizeBound = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::disposeNoNeedForResetSizeBound, "disposeNoNeedForResetSizeBound", this);
            this.payloads.disposeNoNeedForResetTimeBound = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::disposeNoNeedForResetTimeBound, "disposeNoNeedForResetTimeBound", this);
            this.payloads.disposeNoNeedForResetTimeAndSIzeBound = _ClassStatement.forPayload(FlowableReplayEagerTruncateTest::disposeNoNeedForResetTimeAndSIzeBound, "disposeNoNeedForResetTimeAndSIzeBound", this);
        }
    }
}
