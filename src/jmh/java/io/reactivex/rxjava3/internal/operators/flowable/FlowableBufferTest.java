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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.mockito.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableBufferTimed.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableBufferTest extends RxJavaTest {

    private Subscriber<List<String>> subscriber;

    private TestScheduler scheduler;

    private Scheduler.Worker innerScheduler;

    @Before
    public void before() {
        subscriber = TestHelper.mockSubscriber();
        scheduler = new TestScheduler();
        innerScheduler = scheduler.createWorker();
    }

    @Test
    public void complete() {
        Flowable<String> source = Flowable.empty();
        Flowable<List<String>> buffered = source.buffer(3, 3);
        buffered.subscribe(subscriber);
        Mockito.verify(subscriber, Mockito.never()).onNext(Mockito.<String>anyList());
        Mockito.verify(subscriber, Mockito.never()).onError(Mockito.any(Throwable.class));
        Mockito.verify(subscriber, Mockito.times(1)).onComplete();
    }

    @Test
    public void skipAndCountOverlappingBuffers() {
        Flowable<String> source = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                subscriber.onNext("one");
                subscriber.onNext("two");
                subscriber.onNext("three");
                subscriber.onNext("four");
                subscriber.onNext("five");
            }
        });
        Flowable<List<String>> buffered = source.buffer(3, 1);
        buffered.subscribe(subscriber);
        InOrder inOrder = Mockito.inOrder(subscriber);
        inOrder.verify(subscriber, Mockito.times(1)).onNext(list("one", "two", "three"));
        inOrder.verify(subscriber, Mockito.times(1)).onNext(list("two", "three", "four"));
        inOrder.verify(subscriber, Mockito.times(1)).onNext(list("three", "four", "five"));
        inOrder.verify(subscriber, Mockito.never()).onNext(Mockito.<String>anyList());
        inOrder.verify(subscriber, Mockito.never()).onError(Mockito.any(Throwable.class));
        inOrder.verify(subscriber, Mockito.never()).onComplete();
    }

    @Test
    public void skipAndCountGaplessBuffers() {
        Flowable<String> source = Flowable.just("one", "two", "three", "four", "five");
        Flowable<List<String>> buffered = source.buffer(3, 3);
        buffered.subscribe(subscriber);
        InOrder inOrder = Mockito.inOrder(subscriber);
        inOrder.verify(subscriber, Mockito.times(1)).onNext(list("one", "two", "three"));
        inOrder.verify(subscriber, Mockito.times(1)).onNext(list("four", "five"));
        inOrder.verify(subscriber, Mockito.never()).onNext(Mockito.<String>anyList());
        inOrder.verify(subscriber, Mockito.never()).onError(Mockito.any(Throwable.class));
        inOrder.verify(subscriber, Mockito.times(1)).onComplete();
    }

    @Test
    public void skipAndCountBuffersWithGaps() {
        Flowable<String> source = Flowable.just("one", "two", "three", "four", "five");
        Flowable<List<String>> buffered = source.buffer(2, 3);
        buffered.subscribe(subscriber);
        InOrder inOrder = Mockito.inOrder(subscriber);
        inOrder.verify(subscriber, Mockito.times(1)).onNext(list("one", "two"));
        inOrder.verify(subscriber, Mockito.times(1)).onNext(list("four", "five"));
        inOrder.verify(subscriber, Mockito.never()).onNext(Mockito.<String>anyList());
        inOrder.verify(subscriber, Mockito.never()).onError(Mockito.any(Throwable.class));
        inOrder.verify(subscriber, Mockito.times(1)).onComplete();
    }

    @Test
    public void timedAndCount() {
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
        Flowable<List<String>> buffered = source.buffer(100, TimeUnit.MILLISECONDS, scheduler, 2);
        buffered.subscribe(subscriber);
        InOrder inOrder = Mockito.inOrder(subscriber);
        scheduler.advanceTimeTo(100, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, Mockito.times(1)).onNext(list("one", "two"));
        scheduler.advanceTimeTo(200, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, Mockito.times(1)).onNext(list("three", "four"));
        scheduler.advanceTimeTo(300, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, Mockito.times(1)).onNext(list("five"));
        inOrder.verify(subscriber, Mockito.never()).onNext(Mockito.<String>anyList());
        inOrder.verify(subscriber, Mockito.never()).onError(Mockito.any(Throwable.class));
        inOrder.verify(subscriber, Mockito.times(1)).onComplete();
    }

    @Test
    public void timed() {
        Flowable<String> source = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                push(subscriber, "one", 97);
                push(subscriber, "two", 98);
                /**
                 * Changed from 100. Because scheduling the cut to 100ms happens before this
                 * Flowable even runs due how lift works, pushing at 100ms would execute after the
                 * buffer cut.
                 */
                push(subscriber, "three", 99);
                push(subscriber, "four", 101);
                push(subscriber, "five", 102);
                complete(subscriber, 150);
            }
        });
        Flowable<List<String>> buffered = source.buffer(100, TimeUnit.MILLISECONDS, scheduler);
        buffered.subscribe(subscriber);
        InOrder inOrder = Mockito.inOrder(subscriber);
        scheduler.advanceTimeTo(101, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, Mockito.times(1)).onNext(list("one", "two", "three"));
        scheduler.advanceTimeTo(201, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, Mockito.times(1)).onNext(list("four", "five"));
        inOrder.verify(subscriber, Mockito.never()).onNext(Mockito.<String>anyList());
        inOrder.verify(subscriber, Mockito.never()).onError(Mockito.any(Throwable.class));
        inOrder.verify(subscriber, Mockito.times(1)).onComplete();
    }

    @Test
    public void flowableBasedOpenerAndCloser() {
        Flowable<String> source = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                push(subscriber, "one", 10);
                push(subscriber, "two", 60);
                push(subscriber, "three", 110);
                push(subscriber, "four", 160);
                push(subscriber, "five", 210);
                complete(subscriber, 500);
            }
        });
        Flowable<Object> openings = Flowable.unsafeCreate(new Publisher<Object>() {

            @Override
            public void subscribe(Subscriber<Object> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                push(subscriber, new Object(), 50);
                push(subscriber, new Object(), 200);
                complete(subscriber, 250);
            }
        });
        Function<Object, Flowable<Object>> closer = new Function<Object, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Object opening) {
                return Flowable.unsafeCreate(new Publisher<Object>() {

                    @Override
                    public void subscribe(Subscriber<? super Object> subscriber) {
                        subscriber.onSubscribe(new BooleanSubscription());
                        push(subscriber, new Object(), 100);
                        complete(subscriber, 101);
                    }
                });
            }
        };
        Flowable<List<String>> buffered = source.buffer(openings, closer);
        buffered.subscribe(subscriber);
        InOrder inOrder = Mockito.inOrder(subscriber);
        scheduler.advanceTimeTo(500, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, Mockito.times(1)).onNext(list("two", "three"));
        inOrder.verify(subscriber, Mockito.times(1)).onNext(list("five"));
        inOrder.verify(subscriber, Mockito.never()).onNext(Mockito.<String>anyList());
        inOrder.verify(subscriber, Mockito.never()).onError(Mockito.any(Throwable.class));
        inOrder.verify(subscriber, Mockito.times(1)).onComplete();
    }

    @Test
    public void longTimeAction() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        LongTimeAction action = new LongTimeAction(latch);
        Flowable.just(1).buffer(10, TimeUnit.MILLISECONDS, 10).subscribe(action);
        latch.await();
        assertFalse(action.fail);
    }

    static final class LongTimeAction implements Consumer<List<Integer>> {

        CountDownLatch latch;

        boolean fail;

        LongTimeAction(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void accept(List<Integer> t1) {
            try {
                if (fail) {
                    return;
                }
                Thread.sleep(200);
            } catch (InterruptedException e) {
                fail = true;
            } finally {
                latch.countDown();
            }
        }
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

    @Test
    public void bufferStopsWhenUnsubscribed1() {
        Flowable<Integer> source = Flowable.never();
        Subscriber<List<Integer>> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>(subscriber, 0L);
        source.buffer(100, 200, TimeUnit.MILLISECONDS, scheduler).doOnNext(new Consumer<List<Integer>>() {

            @Override
            public void accept(List<Integer> pv) {
                System.out.println(pv);
            }
        }).subscribe(ts);
        InOrder inOrder = Mockito.inOrder(subscriber);
        scheduler.advanceTimeBy(1001, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(5)).onNext(Arrays.<Integer>asList());
        ts.cancel();
        scheduler.advanceTimeBy(999, TimeUnit.MILLISECONDS);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void bufferWithBONormal1() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> boundary = PublishProcessor.create();
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = Mockito.inOrder(subscriber);
        source.buffer(boundary).subscribe(subscriber);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        boundary.onNext(1);
        inOrder.verify(subscriber, times(1)).onNext(Arrays.asList(1, 2, 3));
        source.onNext(4);
        source.onNext(5);
        boundary.onNext(2);
        inOrder.verify(subscriber, times(1)).onNext(Arrays.asList(4, 5));
        source.onNext(6);
        boundary.onComplete();
        inOrder.verify(subscriber, times(1)).onNext(Arrays.asList(6));
        inOrder.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void bufferWithBOEmptyLastViaBoundary() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> boundary = PublishProcessor.create();
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = Mockito.inOrder(subscriber);
        source.buffer(boundary).subscribe(subscriber);
        boundary.onComplete();
        inOrder.verify(subscriber, times(1)).onNext(Arrays.asList());
        inOrder.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void bufferWithBOEmptyLastViaSource() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> boundary = PublishProcessor.create();
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = Mockito.inOrder(subscriber);
        source.buffer(boundary).subscribe(subscriber);
        source.onComplete();
        inOrder.verify(subscriber, times(1)).onNext(Arrays.asList());
        inOrder.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void bufferWithBOEmptyLastViaBoth() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> boundary = PublishProcessor.create();
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = Mockito.inOrder(subscriber);
        source.buffer(boundary).subscribe(subscriber);
        source.onComplete();
        boundary.onComplete();
        inOrder.verify(subscriber, times(1)).onNext(Arrays.asList());
        inOrder.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void bufferWithBOSourceThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> boundary = PublishProcessor.create();
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.buffer(boundary).subscribe(subscriber);
        source.onNext(1);
        source.onError(new TestException());
        verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void bufferWithBOBoundaryThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> boundary = PublishProcessor.create();
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.buffer(boundary).subscribe(subscriber);
        source.onNext(1);
        boundary.onError(new TestException());
        verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void bufferWithSizeTake1() {
        Flowable<Integer> source = Flowable.just(1).repeat();
        Flowable<List<Integer>> result = source.buffer(2).take(1);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        verify(subscriber).onNext(Arrays.asList(1, 1));
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void bufferWithSizeSkipTake1() {
        Flowable<Integer> source = Flowable.just(1).repeat();
        Flowable<List<Integer>> result = source.buffer(2, 3).take(1);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        verify(subscriber).onNext(Arrays.asList(1, 1));
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void bufferWithTimeTake1() {
        Flowable<Long> source = Flowable.interval(40, 40, TimeUnit.MILLISECONDS, scheduler);
        Flowable<List<Long>> result = source.buffer(100, TimeUnit.MILLISECONDS, scheduler).take(1);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        verify(subscriber).onNext(Arrays.asList(0L, 1L));
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void bufferWithTimeSkipTake2() {
        Flowable<Long> source = Flowable.interval(40, 40, TimeUnit.MILLISECONDS, scheduler);
        Flowable<List<Long>> result = source.buffer(100, 60, TimeUnit.MILLISECONDS, scheduler).take(2);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        result.subscribe(subscriber);
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        inOrder.verify(subscriber).onNext(Arrays.asList(0L, 1L));
        inOrder.verify(subscriber).onNext(Arrays.asList(1L, 2L));
        inOrder.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void bufferWithBoundaryTake2() {
        Flowable<Long> boundary = Flowable.interval(60, 60, TimeUnit.MILLISECONDS, scheduler);
        Flowable<Long> source = Flowable.interval(40, 40, TimeUnit.MILLISECONDS, scheduler);
        Flowable<List<Long>> result = source.buffer(boundary).take(2);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        result.subscribe(subscriber);
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        inOrder.verify(subscriber).onNext(Arrays.asList(0L));
        inOrder.verify(subscriber).onNext(Arrays.asList(1L));
        inOrder.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void bufferWithStartEndBoundaryTake2() {
        Flowable<Long> start = Flowable.interval(61, 61, TimeUnit.MILLISECONDS, scheduler);
        Function<Long, Flowable<Long>> end = new Function<Long, Flowable<Long>>() {

            @Override
            public Flowable<Long> apply(Long t1) {
                return Flowable.interval(100, 100, TimeUnit.MILLISECONDS, scheduler);
            }
        };
        Flowable<Long> source = Flowable.interval(40, 40, TimeUnit.MILLISECONDS, scheduler);
        Flowable<List<Long>> result = source.buffer(start, end).take(2);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        result.doOnNext(new Consumer<List<Long>>() {

            @Override
            public void accept(List<Long> pv) {
                System.out.println(pv);
            }
        }).subscribe(subscriber);
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        inOrder.verify(subscriber).onNext(Arrays.asList(1L, 2L, 3L));
        inOrder.verify(subscriber).onNext(Arrays.asList(3L, 4L));
        inOrder.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void bufferWithSizeThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<List<Integer>> result = source.buffer(2);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        result.subscribe(subscriber);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onError(new TestException());
        inOrder.verify(subscriber).onNext(Arrays.asList(1, 2));
        inOrder.verify(subscriber).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onNext(Arrays.asList(3));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void bufferWithTimeThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<List<Integer>> result = source.buffer(100, TimeUnit.MILLISECONDS, scheduler);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        result.subscribe(subscriber);
        source.onNext(1);
        source.onNext(2);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        source.onNext(3);
        source.onError(new TestException());
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber).onNext(Arrays.asList(1, 2));
        inOrder.verify(subscriber).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onNext(Arrays.asList(3));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void bufferWithTimeAndSize() {
        Flowable<Long> source = Flowable.interval(30, 30, TimeUnit.MILLISECONDS, scheduler);
        Flowable<List<Long>> result = source.buffer(100, TimeUnit.MILLISECONDS, scheduler, 2).take(3);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        result.subscribe(subscriber);
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        inOrder.verify(subscriber).onNext(Arrays.asList(0L, 1L));
        inOrder.verify(subscriber).onNext(Arrays.asList(2L));
        inOrder.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void bufferWithStartEndStartThrows() {
        PublishProcessor<Integer> start = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> end = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return Flowable.never();
            }
        };
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<List<Integer>> result = source.buffer(start, end);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        start.onNext(1);
        source.onNext(1);
        source.onNext(2);
        start.onError(new TestException());
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
        verify(subscriber).onError(any(TestException.class));
    }

    @Test
    public void bufferWithStartEndEndFunctionThrows() {
        PublishProcessor<Integer> start = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> end = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                throw new TestException();
            }
        };
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<List<Integer>> result = source.buffer(start, end);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        start.onNext(1);
        source.onNext(1);
        source.onNext(2);
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
        verify(subscriber).onError(any(TestException.class));
    }

    @Test
    public void bufferWithStartEndEndThrows() {
        PublishProcessor<Integer> start = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> end = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return Flowable.error(new TestException());
            }
        };
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<List<Integer>> result = source.buffer(start, end);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        start.onNext(1);
        source.onNext(1);
        source.onNext(2);
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
        verify(subscriber).onError(any(TestException.class));
    }

    @Test
    public void producerRequestThroughBufferWithSize1() {
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>(3L);
        final AtomicLong requested = new AtomicLong();
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onSubscribe(new Subscription() {

                    @Override
                    public void request(long n) {
                        requested.set(n);
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        }).buffer(5, 5).subscribe(ts);
        assertEquals(15, requested.get());
        ts.request(4);
        assertEquals(20, requested.get());
    }

    @Test
    public void producerRequestThroughBufferWithSize2() {
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>();
        final AtomicLong requested = new AtomicLong();
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onSubscribe(new Subscription() {

                    @Override
                    public void request(long n) {
                        requested.set(n);
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        }).buffer(5, 5).subscribe(ts);
        assertEquals(Long.MAX_VALUE, requested.get());
    }

    @Test
    public void producerRequestThroughBufferWithSize3() {
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>(3L);
        final AtomicLong requested = new AtomicLong();
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onSubscribe(new Subscription() {

                    @Override
                    public void request(long n) {
                        requested.set(n);
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        }).buffer(5, 2).subscribe(ts);
        assertEquals(9, requested.get());
        ts.request(3);
        assertEquals(6, requested.get());
    }

    @Test
    public void producerRequestThroughBufferWithSize4() {
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>();
        final AtomicLong requested = new AtomicLong();
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onSubscribe(new Subscription() {

                    @Override
                    public void request(long n) {
                        requested.set(n);
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        }).buffer(5, 2).subscribe(ts);
        assertEquals(Long.MAX_VALUE, requested.get());
    }

    @Test
    public void producerRequestOverflowThroughBufferWithSize1() {
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>(Long.MAX_VALUE >> 1);
        final AtomicLong requested = new AtomicLong();
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onSubscribe(new Subscription() {

                    @Override
                    public void request(long n) {
                        requested.set(n);
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        }).buffer(3, 3).subscribe(ts);
        assertEquals(Long.MAX_VALUE, requested.get());
    }

    @Test
    public void producerRequestOverflowThroughBufferWithSize2() {
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>(Long.MAX_VALUE >> 1);
        final AtomicLong requested = new AtomicLong();
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onSubscribe(new Subscription() {

                    @Override
                    public void request(long n) {
                        requested.set(n);
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        }).buffer(3, 2).subscribe(ts);
        assertEquals(Long.MAX_VALUE, requested.get());
    }

    @Test
    public void producerRequestOverflowThroughBufferWithSize3() {
        final AtomicLong requested = new AtomicLong();
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(final Subscriber<? super Integer> s) {
                s.onSubscribe(new Subscription() {

                    AtomicBoolean once = new AtomicBoolean();

                    @Override
                    public void request(long n) {
                        requested.set(n);
                        if (once.compareAndSet(false, true)) {
                            s.onNext(1);
                            s.onNext(2);
                            s.onNext(3);
                        }
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        }).buffer(3, 2).subscribe(new DefaultSubscriber<List<Integer>>() {

            @Override
            public void onStart() {
                request(Long.MAX_VALUE / 2 - 4);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(List<Integer> t) {
                request(Long.MAX_VALUE / 2);
            }
        });
        // FIXME I'm not sure why this is MAX_VALUE in 1.x because MAX_VALUE/2 is even and thus can't overflow when multiplied by 2
        assertEquals(Long.MAX_VALUE - 1, requested.get());
    }

    @Test
    public void bufferWithTimeDoesntUnsubscribeDownstream() throws InterruptedException {
        final Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        final CountDownLatch cdl = new CountDownLatch(1);
        ResourceSubscriber<Object> s = new ResourceSubscriber<Object>() {

            @Override
            public void onNext(Object t) {
                subscriber.onNext(t);
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
                cdl.countDown();
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
                cdl.countDown();
            }
        };
        Flowable.range(1, 1).delay(1, TimeUnit.SECONDS).buffer(2, TimeUnit.SECONDS).subscribe(s);
        cdl.await();
        verify(subscriber).onNext(Arrays.asList(1));
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        assertFalse(s.isDisposed());
    }

    @Test
    public void postCompleteBackpressure() {
        Flowable<List<Integer>> source = Flowable.range(1, 10).buffer(3, 1);
        TestSubscriber<List<Integer>> ts = TestSubscriber.create(0L);
        source.subscribe(ts);
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertNoErrors();
        ts.request(7);
        ts.assertValues(Arrays.asList(1, 2, 3), Arrays.asList(2, 3, 4), Arrays.asList(3, 4, 5), Arrays.asList(4, 5, 6), Arrays.asList(5, 6, 7), Arrays.asList(6, 7, 8), Arrays.asList(7, 8, 9));
        ts.assertNotComplete();
        ts.assertNoErrors();
        ts.request(1);
        ts.assertValues(Arrays.asList(1, 2, 3), Arrays.asList(2, 3, 4), Arrays.asList(3, 4, 5), Arrays.asList(4, 5, 6), Arrays.asList(5, 6, 7), Arrays.asList(6, 7, 8), Arrays.asList(7, 8, 9), Arrays.asList(8, 9, 10));
        ts.assertNotComplete();
        ts.assertNoErrors();
        ts.request(1);
        ts.assertValues(Arrays.asList(1, 2, 3), Arrays.asList(2, 3, 4), Arrays.asList(3, 4, 5), Arrays.asList(4, 5, 6), Arrays.asList(5, 6, 7), Arrays.asList(6, 7, 8), Arrays.asList(7, 8, 9), Arrays.asList(8, 9, 10), Arrays.asList(9, 10));
        ts.assertNotComplete();
        ts.assertNoErrors();
        ts.request(1);
        ts.assertValues(Arrays.asList(1, 2, 3), Arrays.asList(2, 3, 4), Arrays.asList(3, 4, 5), Arrays.asList(4, 5, 6), Arrays.asList(5, 6, 7), Arrays.asList(6, 7, 8), Arrays.asList(7, 8, 9), Arrays.asList(8, 9, 10), Arrays.asList(9, 10), Arrays.asList(10));
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void timeAndSkipOverlap() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<List<Integer>> ts = TestSubscriber.create();
        pp.buffer(2, 1, TimeUnit.SECONDS, scheduler).subscribe(ts);
        pp.onNext(1);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onNext(2);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onNext(3);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onNext(4);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onComplete();
        ts.assertValues(Arrays.asList(1, 2), Arrays.asList(2, 3), Arrays.asList(3, 4), Arrays.asList(4), Collections.<Integer>emptyList());
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void timeAndSkipSkip() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<List<Integer>> ts = TestSubscriber.create();
        pp.buffer(2, 3, TimeUnit.SECONDS, scheduler).subscribe(ts);
        pp.onNext(1);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onNext(2);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onNext(3);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onNext(4);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onComplete();
        ts.assertValues(Arrays.asList(1, 2), Arrays.asList(4));
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void timeAndSkipOverlapScheduler() {
        RxJavaPlugins.setComputationSchedulerHandler(new Function<Scheduler, Scheduler>() {

            @Override
            public Scheduler apply(Scheduler t) {
                return scheduler;
            }
        });
        try {
            PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriber<List<Integer>> ts = TestSubscriber.create();
            pp.buffer(2, 1, TimeUnit.SECONDS).subscribe(ts);
            pp.onNext(1);
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            pp.onNext(2);
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            pp.onNext(3);
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            pp.onNext(4);
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            pp.onComplete();
            ts.assertValues(Arrays.asList(1, 2), Arrays.asList(2, 3), Arrays.asList(3, 4), Arrays.asList(4), Collections.<Integer>emptyList());
            ts.assertNoErrors();
            ts.assertComplete();
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void timeAndSkipSkipDefaultScheduler() {
        RxJavaPlugins.setComputationSchedulerHandler(new Function<Scheduler, Scheduler>() {

            @Override
            public Scheduler apply(Scheduler t) {
                return scheduler;
            }
        });
        try {
            PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriber<List<Integer>> ts = TestSubscriber.create();
            pp.buffer(2, 3, TimeUnit.SECONDS).subscribe(ts);
            pp.onNext(1);
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            pp.onNext(2);
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            pp.onNext(3);
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            pp.onNext(4);
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            pp.onComplete();
            ts.assertValues(Arrays.asList(1, 2), Arrays.asList(4));
            ts.assertNoErrors();
            ts.assertComplete();
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void bufferBoundaryHint() {
        Flowable.range(1, 5).buffer(Flowable.timer(1, TimeUnit.MINUTES), 2).test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    static HashSet<Integer> set(Integer... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    @Test
    public void bufferIntoCustomCollection() {
        Flowable.just(1, 1, 2, 2, 3, 3, 4, 4).buffer(3, new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() throws Exception {
                return new HashSet<>();
            }
        }).test().assertResult(set(1, 2), set(2, 3), set(4));
    }

    @Test
    public void bufferSkipIntoCustomCollection() {
        Flowable.just(1, 1, 2, 2, 3, 3, 4, 4).buffer(3, 3, new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() throws Exception {
                return new HashSet<>();
            }
        }).test().assertResult(set(1, 2), set(2, 3), set(4));
    }

    @Test
    public void bufferTimeSkipDefault() {
        Flowable.range(1, 5).buffer(1, 1, TimeUnit.MINUTES).test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.range(1, 5).buffer(1, TimeUnit.DAYS, Schedulers.single()));
        TestHelper.checkDisposed(Flowable.range(1, 5).buffer(2, 1, TimeUnit.DAYS, Schedulers.single()));
        TestHelper.checkDisposed(Flowable.range(1, 5).buffer(1, 2, TimeUnit.DAYS, Schedulers.single()));
        TestHelper.checkDisposed(Flowable.range(1, 5).buffer(1, TimeUnit.DAYS, Schedulers.single(), 2, Functions.<Integer>createArrayList(16), true));
        TestHelper.checkDisposed(Flowable.range(1, 5).buffer(1));
        TestHelper.checkDisposed(Flowable.range(1, 5).buffer(2, 1));
        TestHelper.checkDisposed(Flowable.range(1, 5).buffer(1, 2));
    }

    @Test
    public void supplierReturnsNull() {
        Flowable.<Integer>never().buffer(1, TimeUnit.MILLISECONDS, Schedulers.single(), Integer.MAX_VALUE, new Supplier<Collection<Integer>>() {

            int count;

            @Override
            public Collection<Integer> get() throws Exception {
                if (count++ == 1) {
                    return null;
                } else {
                    return new ArrayList<>();
                }
            }
        }, false).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(NullPointerException.class);
    }

    @Test
    public void supplierReturnsNull2() {
        Flowable.<Integer>never().buffer(1, TimeUnit.MILLISECONDS, Schedulers.single(), 10, new Supplier<Collection<Integer>>() {

            int count;

            @Override
            public Collection<Integer> get() throws Exception {
                if (count++ == 1) {
                    return null;
                } else {
                    return new ArrayList<>();
                }
            }
        }, false).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(NullPointerException.class);
    }

    @Test
    public void supplierReturnsNull3() {
        Flowable.<Integer>never().buffer(2, 1, TimeUnit.MILLISECONDS, Schedulers.single(), new Supplier<Collection<Integer>>() {

            int count;

            @Override
            public Collection<Integer> get() throws Exception {
                if (count++ == 1) {
                    return null;
                } else {
                    return new ArrayList<>();
                }
            }
        }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(NullPointerException.class);
    }

    @Test
    public void supplierThrows() {
        Flowable.just(1).buffer(1, TimeUnit.SECONDS, Schedulers.single(), Integer.MAX_VALUE, new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() throws Exception {
                throw new TestException();
            }
        }, false).test().assertFailure(TestException.class);
    }

    @Test
    public void supplierThrows2() {
        Flowable.just(1).buffer(1, TimeUnit.SECONDS, Schedulers.single(), 10, new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() throws Exception {
                throw new TestException();
            }
        }, false).test().assertFailure(TestException.class);
    }

    @Test
    public void supplierThrows3() {
        Flowable.just(1).buffer(2, 1, TimeUnit.SECONDS, Schedulers.single(), new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void supplierThrows4() {
        Flowable.<Integer>never().buffer(1, TimeUnit.MILLISECONDS, Schedulers.single(), Integer.MAX_VALUE, new Supplier<Collection<Integer>>() {

            int count;

            @Override
            public Collection<Integer> get() throws Exception {
                if (count++ == 1) {
                    throw new TestException();
                } else {
                    return new ArrayList<>();
                }
            }
        }, false).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void supplierThrows5() {
        Flowable.<Integer>never().buffer(1, TimeUnit.MILLISECONDS, Schedulers.single(), 10, new Supplier<Collection<Integer>>() {

            int count;

            @Override
            public Collection<Integer> get() throws Exception {
                if (count++ == 1) {
                    throw new TestException();
                } else {
                    return new ArrayList<>();
                }
            }
        }, false).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void supplierThrows6() {
        Flowable.<Integer>never().buffer(2, 1, TimeUnit.MILLISECONDS, Schedulers.single(), new Supplier<Collection<Integer>>() {

            int count;

            @Override
            public Collection<Integer> get() throws Exception {
                if (count++ == 1) {
                    throw new TestException();
                } else {
                    return new ArrayList<>();
                }
            }
        }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void restartTimer() {
        Flowable.range(1, 5).buffer(1, TimeUnit.DAYS, Schedulers.single(), 2, Functions.<Integer>createArrayList(16), true).test().assertResult(Arrays.asList(1, 2), Arrays.asList(3, 4), Arrays.asList(5));
    }

    @Test
    public void bufferSkipError() {
        Flowable.<Integer>error(new TestException()).buffer(2, 1).test().assertFailure(TestException.class);
    }

    @Test
    public void bufferSupplierCrash2() {
        Flowable.range(1, 2).buffer(1, new Supplier<List<Integer>>() {

            int calls;

            @Override
            public List<Integer> get() throws Exception {
                if (++calls == 2) {
                    throw new TestException();
                }
                return new ArrayList<>();
            }
        }).test().assertFailure(TestException.class, Arrays.asList(1));
    }

    @Test
    public void bufferSkipSupplierCrash2() {
        Flowable.range(1, 2).buffer(1, 2, new Supplier<List<Integer>>() {

            int calls;

            @Override
            public List<Integer> get() throws Exception {
                if (++calls == 1) {
                    throw new TestException();
                }
                return new ArrayList<>();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void bufferOverlapSupplierCrash2() {
        Flowable.range(1, 2).buffer(2, 1, new Supplier<List<Integer>>() {

            int calls;

            @Override
            public List<Integer> get() throws Exception {
                if (++calls == 2) {
                    throw new TestException();
                }
                return new ArrayList<>();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void bufferSkipOverlap() {
        Flowable.range(1, 5).buffer(5, 1).test().assertResult(Arrays.asList(1, 2, 3, 4, 5), Arrays.asList(2, 3, 4, 5), Arrays.asList(3, 4, 5), Arrays.asList(4, 5), Arrays.asList(5));
    }

    @Test
    public void bufferTimedExactError() {
        Flowable.error(new TestException()).buffer(1, TimeUnit.DAYS).test().assertFailure(TestException.class);
    }

    @Test
    public void bufferTimedSkipError() {
        Flowable.error(new TestException()).buffer(1, 2, TimeUnit.DAYS).test().assertFailure(TestException.class);
    }

    @Test
    public void bufferTimedOverlapError() {
        Flowable.error(new TestException()).buffer(2, 1, TimeUnit.DAYS).test().assertFailure(TestException.class);
    }

    @Test
    public void bufferTimedExactEmpty() {
        Flowable.empty().buffer(1, TimeUnit.DAYS).test().assertResult(Collections.emptyList());
    }

    @Test
    public void bufferTimedSkipEmpty() {
        Flowable.empty().buffer(1, 2, TimeUnit.DAYS).test().assertResult(Collections.emptyList());
    }

    @Test
    public void bufferTimedOverlapEmpty() {
        Flowable.empty().buffer(2, 1, TimeUnit.DAYS).test().assertResult(Collections.emptyList());
    }

    @Test
    public void bufferTimedExactSupplierCrash() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<List<Integer>> ts = pp.buffer(1, TimeUnit.MILLISECONDS, scheduler, 1, new Supplier<List<Integer>>() {

            int calls;

            @Override
            public List<Integer> get() throws Exception {
                if (++calls == 2) {
                    throw new TestException();
                }
                return new ArrayList<>();
            }
        }, true).test();
        pp.onNext(1);
        scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        pp.onNext(2);
        ts.assertFailure(TestException.class, Arrays.asList(1));
    }

    @Test
    public void bufferTimedExactBoundedError() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<List<Integer>> ts = pp.buffer(1, TimeUnit.MILLISECONDS, scheduler, 1, Functions.<Integer>createArrayList(16), true).test();
        pp.onError(new TestException());
        ts.assertFailure(TestException.class);
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.buffer(1);
            }
        }, false, 1, 1, Arrays.asList(1));
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.buffer(1, 2);
            }
        }, false, 1, 1, Arrays.asList(1));
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.buffer(2, 1);
            }
        }, false, 1, 1, Arrays.asList(1));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<List<Object>>>() {

            @Override
            public Publisher<List<Object>> apply(Flowable<Object> f) throws Exception {
                return f.buffer(1);
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<List<Object>>>() {

            @Override
            public Publisher<List<Object>> apply(Flowable<Object> f) throws Exception {
                return f.buffer(1, 2);
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<List<Object>>>() {

            @Override
            public Publisher<List<Object>> apply(Flowable<Object> f) throws Exception {
                return f.buffer(2, 1);
            }
        });
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(PublishProcessor.create().buffer(1));
        TestHelper.assertBadRequestReported(PublishProcessor.create().buffer(1, 2));
        TestHelper.assertBadRequestReported(PublishProcessor.create().buffer(2, 1));
    }

    @Test
    public void skipError() {
        Flowable.error(new TestException()).buffer(1, 2).test().assertFailure(TestException.class);
    }

    @Test
    public void skipSingleResult() {
        Flowable.just(1).buffer(2, 3).test().assertResult(Arrays.asList(1));
    }

    @Test
    public void skipBackpressure() {
        Flowable.range(1, 10).buffer(2, 3).rebatchRequests(1).test().assertResult(Arrays.asList(1, 2), Arrays.asList(4, 5), Arrays.asList(7, 8), Arrays.asList(10));
    }

    @Test
    public void withTimeAndSizeCapacityRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestScheduler scheduler = new TestScheduler();
            final PublishProcessor<Object> pp = PublishProcessor.create();
            TestSubscriber<List<Object>> ts = pp.buffer(1, TimeUnit.SECONDS, scheduler, 5).test();
            pp.onNext(1);
            pp.onNext(2);
            pp.onNext(3);
            pp.onNext(4);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(5);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
                }
            };
            TestHelper.race(r1, r2);
            pp.onComplete();
            int items = 0;
            for (List<Object> o : ts.values()) {
                items += o.size();
            }
            assertEquals("Round: " + i, 5, items);
        }
    }

    @Test
    public void noCompletionCancelExact() {
        final AtomicInteger counter = new AtomicInteger();
        Flowable.<Integer>empty().doOnCancel(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        }).buffer(5, TimeUnit.SECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(Collections.<Integer>emptyList());
        assertEquals(0, counter.get());
    }

    @Test
    public void noCompletionCancelSkip() {
        final AtomicInteger counter = new AtomicInteger();
        Flowable.<Integer>empty().doOnCancel(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        }).buffer(5, 10, TimeUnit.SECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(Collections.<Integer>emptyList());
        assertEquals(0, counter.get());
    }

    @Test
    public void noCompletionCancelOverlap() {
        final AtomicInteger counter = new AtomicInteger();
        Flowable.<Integer>empty().doOnCancel(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        }).buffer(10, 5, TimeUnit.SECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(Collections.<Integer>emptyList());
        assertEquals(0, counter.get());
    }

    @Test
    public void boundaryOpenCloseDisposedOnComplete() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> openIndicator = PublishProcessor.create();
        PublishProcessor<Integer> closeIndicator = PublishProcessor.create();
        TestSubscriber<List<Integer>> ts = source.buffer(openIndicator, Functions.justFunction(closeIndicator)).test();
        assertTrue(source.hasSubscribers());
        assertTrue(openIndicator.hasSubscribers());
        assertFalse(closeIndicator.hasSubscribers());
        openIndicator.onNext(1);
        assertTrue(openIndicator.hasSubscribers());
        assertTrue(closeIndicator.hasSubscribers());
        source.onComplete();
        ts.assertResult(Collections.<Integer>emptyList());
        assertFalse(openIndicator.hasSubscribers());
        assertFalse(closeIndicator.hasSubscribers());
    }

    @Test
    public void bufferedCanCompleteIfOpenNeverCompletesDropping() {
        Flowable.range(1, 50).zipWith(Flowable.interval(5, TimeUnit.MILLISECONDS), new BiFunction<Integer, Long, Integer>() {

            @Override
            public Integer apply(Integer integer, Long aLong) {
                return integer;
            }
        }).buffer(Flowable.interval(0, 200, TimeUnit.MILLISECONDS), new Function<Long, Publisher<?>>() {

            @Override
            public Publisher<?> apply(Long a) {
                return Flowable.just(a).delay(100, TimeUnit.MILLISECONDS);
            }
        }).to(TestHelper.<List<Integer>>testConsumer()).assertSubscribed().awaitDone(3, TimeUnit.SECONDS).assertComplete();
    }

    @Test
    public void bufferedCanCompleteIfOpenNeverCompletesOverlapping() {
        Flowable.range(1, 50).zipWith(Flowable.interval(5, TimeUnit.MILLISECONDS), new BiFunction<Integer, Long, Integer>() {

            @Override
            public Integer apply(Integer integer, Long aLong) {
                return integer;
            }
        }).buffer(Flowable.interval(0, 100, TimeUnit.MILLISECONDS), new Function<Long, Publisher<?>>() {

            @Override
            public Publisher<?> apply(Long a) {
                return Flowable.just(a).delay(200, TimeUnit.MILLISECONDS);
            }
        }).to(TestHelper.<List<Integer>>testConsumer()).assertSubscribed().awaitDone(3, TimeUnit.SECONDS).assertComplete();
    }

    @Test
    public void openClosemainError() {
        Flowable.error(new TestException()).buffer(Flowable.never(), Functions.justFunction(Flowable.never())).test().assertFailure(TestException.class);
    }

    @Test
    public void openClosebadSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Object>() {

                @Override
                protected void subscribeActual(Subscriber<? super Object> s) {
                    BooleanSubscription bs1 = new BooleanSubscription();
                    BooleanSubscription bs2 = new BooleanSubscription();
                    s.onSubscribe(bs1);
                    assertFalse(bs1.isCancelled());
                    assertFalse(bs2.isCancelled());
                    s.onSubscribe(bs2);
                    assertFalse(bs1.isCancelled());
                    assertTrue(bs2.isCancelled());
                    s.onError(new IOException());
                    s.onComplete();
                    s.onNext(1);
                    s.onError(new TestException());
                }
            }.buffer(Flowable.never(), Functions.justFunction(Flowable.never())).test().assertFailure(IOException.class);
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
            TestHelper.assertUndeliverable(errors, 1, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void openCloseOpenCompletes() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> openIndicator = PublishProcessor.create();
        PublishProcessor<Integer> closeIndicator = PublishProcessor.create();
        TestSubscriber<List<Integer>> ts = source.buffer(openIndicator, Functions.justFunction(closeIndicator)).test();
        openIndicator.onNext(1);
        assertTrue(closeIndicator.hasSubscribers());
        openIndicator.onComplete();
        assertTrue(source.hasSubscribers());
        assertTrue(closeIndicator.hasSubscribers());
        closeIndicator.onComplete();
        assertFalse(source.hasSubscribers());
        ts.assertResult(Collections.<Integer>emptyList());
    }

    @Test
    public void openCloseOpenCompletesNoBuffers() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> openIndicator = PublishProcessor.create();
        PublishProcessor<Integer> closeIndicator = PublishProcessor.create();
        TestSubscriber<List<Integer>> ts = source.buffer(openIndicator, Functions.justFunction(closeIndicator)).test();
        openIndicator.onNext(1);
        assertTrue(closeIndicator.hasSubscribers());
        closeIndicator.onComplete();
        assertTrue(source.hasSubscribers());
        assertTrue(openIndicator.hasSubscribers());
        openIndicator.onComplete();
        assertFalse(source.hasSubscribers());
        ts.assertResult(Collections.<Integer>emptyList());
    }

    @Test
    public void openCloseTake() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> openIndicator = PublishProcessor.create();
        PublishProcessor<Integer> closeIndicator = PublishProcessor.create();
        TestSubscriber<List<Integer>> ts = source.buffer(openIndicator, Functions.justFunction(closeIndicator)).take(1).test(2);
        openIndicator.onNext(1);
        closeIndicator.onComplete();
        assertFalse(source.hasSubscribers());
        assertFalse(openIndicator.hasSubscribers());
        assertFalse(closeIndicator.hasSubscribers());
        ts.assertResult(Collections.<Integer>emptyList());
    }

    @Test
    public void openCloseEmptyBackpressure() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> openIndicator = PublishProcessor.create();
        PublishProcessor<Integer> closeIndicator = PublishProcessor.create();
        TestSubscriber<List<Integer>> ts = source.buffer(openIndicator, Functions.justFunction(closeIndicator)).test(0);
        source.onComplete();
        assertFalse(openIndicator.hasSubscribers());
        assertFalse(closeIndicator.hasSubscribers());
        ts.assertResult();
    }

    @Test
    public void openCloseErrorBackpressure() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> openIndicator = PublishProcessor.create();
        PublishProcessor<Integer> closeIndicator = PublishProcessor.create();
        TestSubscriber<List<Integer>> ts = source.buffer(openIndicator, Functions.justFunction(closeIndicator)).test(0);
        source.onError(new TestException());
        assertFalse(openIndicator.hasSubscribers());
        assertFalse(closeIndicator.hasSubscribers());
        ts.assertFailure(TestException.class);
    }

    @Test
    public void openCloseBadOpen() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.never().buffer(new Flowable<Object>() {

                @Override
                protected void subscribeActual(Subscriber<? super Object> s) {
                    assertFalse(((Disposable) s).isDisposed());
                    BooleanSubscription bs1 = new BooleanSubscription();
                    BooleanSubscription bs2 = new BooleanSubscription();
                    s.onSubscribe(bs1);
                    assertFalse(bs1.isCancelled());
                    assertFalse(bs2.isCancelled());
                    s.onSubscribe(bs2);
                    assertFalse(bs1.isCancelled());
                    assertTrue(bs2.isCancelled());
                    s.onError(new IOException());
                    assertTrue(((Disposable) s).isDisposed());
                    s.onComplete();
                    s.onNext(1);
                    s.onError(new TestException());
                }
            }, Functions.justFunction(Flowable.never())).test().assertFailure(IOException.class);
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
            TestHelper.assertUndeliverable(errors, 1, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void openCloseBadClose() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.never().buffer(Flowable.just(1).concatWith(Flowable.<Integer>never()), Functions.justFunction(new Flowable<Object>() {

                @Override
                protected void subscribeActual(Subscriber<? super Object> s) {
                    assertFalse(((Disposable) s).isDisposed());
                    BooleanSubscription bs1 = new BooleanSubscription();
                    BooleanSubscription bs2 = new BooleanSubscription();
                    s.onSubscribe(bs1);
                    assertFalse(bs1.isCancelled());
                    assertFalse(bs2.isCancelled());
                    s.onSubscribe(bs2);
                    assertFalse(bs1.isCancelled());
                    assertTrue(bs2.isCancelled());
                    s.onError(new IOException());
                    assertTrue(((Disposable) s).isDisposed());
                    s.onComplete();
                    s.onNext(1);
                    s.onError(new TestException());
                }
            })).test().assertFailure(IOException.class);
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
            TestHelper.assertUndeliverable(errors, 1, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void bufferExactBoundaryDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<List<Object>>>() {

            @Override
            public Flowable<List<Object>> apply(Flowable<Object> f) throws Exception {
                return f.buffer(Flowable.never());
            }
        });
    }

    @Test
    public void bufferExactBoundarySecondBufferCrash() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> b = PublishProcessor.create();
        TestSubscriber<List<Integer>> ts = pp.buffer(b, new Supplier<List<Integer>>() {

            int calls;

            @Override
            public List<Integer> get() throws Exception {
                if (++calls == 2) {
                    throw new TestException();
                }
                return new ArrayList<>();
            }
        }).test();
        b.onNext(1);
        ts.assertFailure(TestException.class);
    }

    @Test
    public void bufferExactBoundaryBadSource() {
        Flowable<Integer> pp = new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                subscriber.onComplete();
                subscriber.onNext(1);
                subscriber.onComplete();
            }
        };
        final AtomicReference<Subscriber<? super Integer>> ref = new AtomicReference<>();
        Flowable<Integer> b = new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                ref.set(subscriber);
            }
        };
        TestSubscriber<List<Integer>> ts = pp.buffer(b).test();
        ref.get().onNext(1);
        ts.assertResult(Collections.<Integer>emptyList());
    }

    @Test
    public void bufferExactBoundaryCancelUpfront() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> b = PublishProcessor.create();
        pp.buffer(b).test(0L, true).assertEmpty();
        assertFalse(pp.hasSubscribers());
        assertFalse(b.hasSubscribers());
    }

    @Test
    public void bufferExactBoundaryDisposed() {
        Flowable<Integer> pp = new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                Disposable d = (Disposable) s;
                assertFalse(d.isDisposed());
                d.dispose();
                assertTrue(d.isDisposed());
            }
        };
        PublishProcessor<Integer> b = PublishProcessor.create();
        pp.buffer(b).test();
    }

    @Test
    public void timedDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<List<Object>>>() {

            @Override
            public Publisher<List<Object>> apply(Flowable<Object> f) throws Exception {
                return f.buffer(1, TimeUnit.SECONDS);
            }
        });
    }

    @Test
    public void timedCancelledUpfront() {
        TestScheduler sch = new TestScheduler();
        TestSubscriber<List<Object>> ts = Flowable.never().buffer(1, TimeUnit.MILLISECONDS, sch).test(1L, true);
        sch.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        ts.assertEmpty();
    }

    @Test
    public void timedInternalState() {
        TestScheduler sch = new TestScheduler();
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>();
        BufferExactUnboundedSubscriber<Integer, List<Integer>> sub = new BufferExactUnboundedSubscriber<>(ts, Functions.justSupplier((List<Integer>) new ArrayList<Integer>()), 1, TimeUnit.SECONDS, sch);
        sub.onSubscribe(new BooleanSubscription());
        assertFalse(sub.isDisposed());
        sub.onError(new TestException());
        sub.onNext(1);
        sub.onComplete();
        sub.run();
        sub.dispose();
        assertTrue(sub.isDisposed());
        sub.buffer = new ArrayList<>();
        sub.enter();
        sub.onComplete();
    }

    @Test
    public void timedSkipDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<List<Object>>>() {

            @Override
            public Publisher<List<Object>> apply(Flowable<Object> f) throws Exception {
                return f.buffer(2, 1, TimeUnit.SECONDS);
            }
        });
    }

    @Test
    public void timedSizedDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<List<Object>>>() {

            @Override
            public Publisher<List<Object>> apply(Flowable<Object> f) throws Exception {
                return f.buffer(2, TimeUnit.SECONDS, 10);
            }
        });
    }

    @Test
    public void timedSkipInternalState() {
        TestScheduler sch = new TestScheduler();
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>();
        BufferSkipBoundedSubscriber<Integer, List<Integer>> sub = new BufferSkipBoundedSubscriber<>(ts, Functions.justSupplier((List<Integer>) new ArrayList<Integer>()), 1, 1, TimeUnit.SECONDS, sch.createWorker());
        sub.onSubscribe(new BooleanSubscription());
        sub.enter();
        sub.onComplete();
        sub.cancel();
        sub.run();
    }

    @Test
    public void timedSkipCancelWhenSecondBuffer() {
        TestScheduler sch = new TestScheduler();
        final TestSubscriber<List<Integer>> ts = new TestSubscriber<>();
        BufferSkipBoundedSubscriber<Integer, List<Integer>> sub = new BufferSkipBoundedSubscriber<>(ts, new Supplier<List<Integer>>() {

            int calls;

            @Override
            public List<Integer> get() throws Exception {
                if (++calls == 2) {
                    ts.cancel();
                }
                return new ArrayList<>();
            }
        }, 1, 1, TimeUnit.SECONDS, sch.createWorker());
        sub.onSubscribe(new BooleanSubscription());
        sub.run();
        assertTrue(ts.isCancelled());
    }

    @Test
    public void timedSizeBufferAlreadyCleared() {
        TestScheduler sch = new TestScheduler();
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>();
        BufferExactBoundedSubscriber<Integer, List<Integer>> sub = new BufferExactBoundedSubscriber<>(ts, Functions.justSupplier((List<Integer>) new ArrayList<Integer>()), 1, TimeUnit.SECONDS, 1, false, sch.createWorker());
        BooleanSubscription bs = new BooleanSubscription();
        sub.onSubscribe(bs);
        sub.producerIndex++;
        sub.run();
        assertFalse(sub.isDisposed());
        sub.enter();
        sub.onComplete();
        assertTrue(sub.isDisposed());
        sub.run();
    }

    @Test
    public void bufferExactFailingSupplier() {
        Flowable.empty().buffer(1, TimeUnit.SECONDS, Schedulers.computation(), 10, new Supplier<List<Object>>() {

            @Override
            public List<Object> get() throws Exception {
                throw new TestException();
            }
        }, false).test().awaitDone(1, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void exactBadRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().buffer(1));
    }

    @Test
    public void skipBadRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().buffer(1, 2));
    }

    @Test
    public void overlapBadRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().buffer(2, 1));
    }

    @Test
    public void bufferExactBoundedOnNextAfterDispose() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.unsafeCreate(s -> {
            s.onSubscribe(new BooleanSubscription());
            ts.cancel();
            s.onNext(1);
        }).buffer(1, TimeUnit.MINUTES, 2).subscribe(ts);
        ts.assertEmpty();
    }

    @Test
    public void boundaryCloseCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            BehaviorProcessor<Integer> bp = BehaviorProcessor.createDefault(1);
            PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriber<List<Integer>> ts = bp.buffer(BehaviorProcessor.createDefault(0), v -> pp).test();
            TestHelper.race(() -> bp.onComplete(), () -> pp.onComplete());
            ts.assertResult(Arrays.asList(1));
        }
    }

    @Test
    public void doubleOnSubscribeStartEnd() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.buffer(Flowable.never(), v -> Flowable.never()));
    }

    @Test
    public void cancel() {
        TestHelper.checkDisposed(Flowable.never().buffer(Flowable.never(), v -> Flowable.never()));
    }

    @Test
    public void startEndCancelAfterOneBuffer() {
        BehaviorProcessor.createDefault(1).buffer(BehaviorProcessor.createDefault(2), v -> Flowable.just(1)).takeUntil(v -> true).test().assertResult(Arrays.asList());
    }

    @Test
    public void startEndCompleteOnBoundary() {
        Flowable.empty().buffer(Flowable.never(), v -> Flowable.just(1)).take(1).test().assertResult();
    }

    @Test
    public void startEndBackpressure() {
        BehaviorProcessor.createDefault(1).buffer(BehaviorProcessor.createDefault(2), v -> Flowable.just(1)).test(1L).assertValuesOnly(Arrays.asList());
    }

    @Test
    public void startEndBackpressureMoreWork() {
        PublishProcessor<Integer> bp = PublishProcessor.create();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        AtomicInteger counter = new AtomicInteger();
        TestSubscriber<List<Integer>> ts = bp.buffer(pp, v -> Flowable.just(1)).doOnNext(v -> {
            if (counter.getAndIncrement() == 0) {
                pp.onNext(2);
                pp.onComplete();
            }
        }).test(1L);
        pp.onNext(1);
        bp.onNext(1);
        ts.assertValuesOnly(Arrays.asList());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableBufferTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_complete() throws java.lang.Throwable {
            this.payloads.complete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipAndCountOverlappingBuffers() throws java.lang.Throwable {
            this.payloads.skipAndCountOverlappingBuffers.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipAndCountGaplessBuffers() throws java.lang.Throwable {
            this.payloads.skipAndCountGaplessBuffers.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipAndCountBuffersWithGaps() throws java.lang.Throwable {
            this.payloads.skipAndCountBuffersWithGaps.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedAndCount() throws java.lang.Throwable {
            this.payloads.timedAndCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timed() throws java.lang.Throwable {
            this.payloads.timed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableBasedOpenerAndCloser() throws java.lang.Throwable {
            this.payloads.flowableBasedOpenerAndCloser.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_longTimeAction() throws java.lang.Throwable {
            this.payloads.longTimeAction.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferStopsWhenUnsubscribed1() throws java.lang.Throwable {
            this.payloads.bufferStopsWhenUnsubscribed1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithBONormal1() throws java.lang.Throwable {
            this.payloads.bufferWithBONormal1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithBOEmptyLastViaBoundary() throws java.lang.Throwable {
            this.payloads.bufferWithBOEmptyLastViaBoundary.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithBOEmptyLastViaSource() throws java.lang.Throwable {
            this.payloads.bufferWithBOEmptyLastViaSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithBOEmptyLastViaBoth() throws java.lang.Throwable {
            this.payloads.bufferWithBOEmptyLastViaBoth.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithBOSourceThrows() throws java.lang.Throwable {
            this.payloads.bufferWithBOSourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithBOBoundaryThrows() throws java.lang.Throwable {
            this.payloads.bufferWithBOBoundaryThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithSizeTake1() throws java.lang.Throwable {
            this.payloads.bufferWithSizeTake1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithSizeSkipTake1() throws java.lang.Throwable {
            this.payloads.bufferWithSizeSkipTake1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithTimeTake1() throws java.lang.Throwable {
            this.payloads.bufferWithTimeTake1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithTimeSkipTake2() throws java.lang.Throwable {
            this.payloads.bufferWithTimeSkipTake2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithBoundaryTake2() throws java.lang.Throwable {
            this.payloads.bufferWithBoundaryTake2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithStartEndBoundaryTake2() throws java.lang.Throwable {
            this.payloads.bufferWithStartEndBoundaryTake2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithSizeThrows() throws java.lang.Throwable {
            this.payloads.bufferWithSizeThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithTimeThrows() throws java.lang.Throwable {
            this.payloads.bufferWithTimeThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithTimeAndSize() throws java.lang.Throwable {
            this.payloads.bufferWithTimeAndSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithStartEndStartThrows() throws java.lang.Throwable {
            this.payloads.bufferWithStartEndStartThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithStartEndEndFunctionThrows() throws java.lang.Throwable {
            this.payloads.bufferWithStartEndEndFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithStartEndEndThrows() throws java.lang.Throwable {
            this.payloads.bufferWithStartEndEndThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_producerRequestThroughBufferWithSize1() throws java.lang.Throwable {
            this.payloads.producerRequestThroughBufferWithSize1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_producerRequestThroughBufferWithSize2() throws java.lang.Throwable {
            this.payloads.producerRequestThroughBufferWithSize2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_producerRequestThroughBufferWithSize3() throws java.lang.Throwable {
            this.payloads.producerRequestThroughBufferWithSize3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_producerRequestThroughBufferWithSize4() throws java.lang.Throwable {
            this.payloads.producerRequestThroughBufferWithSize4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_producerRequestOverflowThroughBufferWithSize1() throws java.lang.Throwable {
            this.payloads.producerRequestOverflowThroughBufferWithSize1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_producerRequestOverflowThroughBufferWithSize2() throws java.lang.Throwable {
            this.payloads.producerRequestOverflowThroughBufferWithSize2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_producerRequestOverflowThroughBufferWithSize3() throws java.lang.Throwable {
            this.payloads.producerRequestOverflowThroughBufferWithSize3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferWithTimeDoesntUnsubscribeDownstream() throws java.lang.Throwable {
            this.payloads.bufferWithTimeDoesntUnsubscribeDownstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_postCompleteBackpressure() throws java.lang.Throwable {
            this.payloads.postCompleteBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeAndSkipOverlap() throws java.lang.Throwable {
            this.payloads.timeAndSkipOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeAndSkipSkip() throws java.lang.Throwable {
            this.payloads.timeAndSkipSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeAndSkipOverlapScheduler() throws java.lang.Throwable {
            this.payloads.timeAndSkipOverlapScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeAndSkipSkipDefaultScheduler() throws java.lang.Throwable {
            this.payloads.timeAndSkipSkipDefaultScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferBoundaryHint() throws java.lang.Throwable {
            this.payloads.bufferBoundaryHint.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferIntoCustomCollection() throws java.lang.Throwable {
            this.payloads.bufferIntoCustomCollection.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferSkipIntoCustomCollection() throws java.lang.Throwable {
            this.payloads.bufferSkipIntoCustomCollection.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferTimeSkipDefault() throws java.lang.Throwable {
            this.payloads.bufferTimeSkipDefault.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierReturnsNull() throws java.lang.Throwable {
            this.payloads.supplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierReturnsNull2() throws java.lang.Throwable {
            this.payloads.supplierReturnsNull2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierReturnsNull3() throws java.lang.Throwable {
            this.payloads.supplierReturnsNull3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierThrows() throws java.lang.Throwable {
            this.payloads.supplierThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierThrows2() throws java.lang.Throwable {
            this.payloads.supplierThrows2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierThrows3() throws java.lang.Throwable {
            this.payloads.supplierThrows3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierThrows4() throws java.lang.Throwable {
            this.payloads.supplierThrows4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierThrows5() throws java.lang.Throwable {
            this.payloads.supplierThrows5.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierThrows6() throws java.lang.Throwable {
            this.payloads.supplierThrows6.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_restartTimer() throws java.lang.Throwable {
            this.payloads.restartTimer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferSkipError() throws java.lang.Throwable {
            this.payloads.bufferSkipError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferSupplierCrash2() throws java.lang.Throwable {
            this.payloads.bufferSupplierCrash2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferSkipSupplierCrash2() throws java.lang.Throwable {
            this.payloads.bufferSkipSupplierCrash2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferOverlapSupplierCrash2() throws java.lang.Throwable {
            this.payloads.bufferOverlapSupplierCrash2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferSkipOverlap() throws java.lang.Throwable {
            this.payloads.bufferSkipOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferTimedExactError() throws java.lang.Throwable {
            this.payloads.bufferTimedExactError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferTimedSkipError() throws java.lang.Throwable {
            this.payloads.bufferTimedSkipError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferTimedOverlapError() throws java.lang.Throwable {
            this.payloads.bufferTimedOverlapError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferTimedExactEmpty() throws java.lang.Throwable {
            this.payloads.bufferTimedExactEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferTimedSkipEmpty() throws java.lang.Throwable {
            this.payloads.bufferTimedSkipEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferTimedOverlapEmpty() throws java.lang.Throwable {
            this.payloads.bufferTimedOverlapEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferTimedExactSupplierCrash() throws java.lang.Throwable {
            this.payloads.bufferTimedExactSupplierCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferTimedExactBoundedError() throws java.lang.Throwable {
            this.payloads.bufferTimedExactBoundedError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipError() throws java.lang.Throwable {
            this.payloads.skipError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipSingleResult() throws java.lang.Throwable {
            this.payloads.skipSingleResult.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipBackpressure() throws java.lang.Throwable {
            this.payloads.skipBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withTimeAndSizeCapacityRace() throws java.lang.Throwable {
            this.payloads.withTimeAndSizeCapacityRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCompletionCancelExact() throws java.lang.Throwable {
            this.payloads.noCompletionCancelExact.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCompletionCancelSkip() throws java.lang.Throwable {
            this.payloads.noCompletionCancelSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCompletionCancelOverlap() throws java.lang.Throwable {
            this.payloads.noCompletionCancelOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryOpenCloseDisposedOnComplete() throws java.lang.Throwable {
            this.payloads.boundaryOpenCloseDisposedOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferedCanCompleteIfOpenNeverCompletesDropping() throws java.lang.Throwable {
            this.payloads.bufferedCanCompleteIfOpenNeverCompletesDropping.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferedCanCompleteIfOpenNeverCompletesOverlapping() throws java.lang.Throwable {
            this.payloads.bufferedCanCompleteIfOpenNeverCompletesOverlapping.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_openClosemainError() throws java.lang.Throwable {
            this.payloads.openClosemainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_openClosebadSource() throws java.lang.Throwable {
            this.payloads.openClosebadSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_openCloseOpenCompletes() throws java.lang.Throwable {
            this.payloads.openCloseOpenCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_openCloseOpenCompletesNoBuffers() throws java.lang.Throwable {
            this.payloads.openCloseOpenCompletesNoBuffers.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_openCloseTake() throws java.lang.Throwable {
            this.payloads.openCloseTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_openCloseEmptyBackpressure() throws java.lang.Throwable {
            this.payloads.openCloseEmptyBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_openCloseErrorBackpressure() throws java.lang.Throwable {
            this.payloads.openCloseErrorBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_openCloseBadOpen() throws java.lang.Throwable {
            this.payloads.openCloseBadOpen.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_openCloseBadClose() throws java.lang.Throwable {
            this.payloads.openCloseBadClose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferExactBoundaryDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.bufferExactBoundaryDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferExactBoundarySecondBufferCrash() throws java.lang.Throwable {
            this.payloads.bufferExactBoundarySecondBufferCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferExactBoundaryBadSource() throws java.lang.Throwable {
            this.payloads.bufferExactBoundaryBadSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferExactBoundaryCancelUpfront() throws java.lang.Throwable {
            this.payloads.bufferExactBoundaryCancelUpfront.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferExactBoundaryDisposed() throws java.lang.Throwable {
            this.payloads.bufferExactBoundaryDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.timedDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedCancelledUpfront() throws java.lang.Throwable {
            this.payloads.timedCancelledUpfront.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedInternalState() throws java.lang.Throwable {
            this.payloads.timedInternalState.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedSkipDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.timedSkipDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedSizedDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.timedSizedDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedSkipInternalState() throws java.lang.Throwable {
            this.payloads.timedSkipInternalState.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedSkipCancelWhenSecondBuffer() throws java.lang.Throwable {
            this.payloads.timedSkipCancelWhenSecondBuffer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedSizeBufferAlreadyCleared() throws java.lang.Throwable {
            this.payloads.timedSizeBufferAlreadyCleared.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferExactFailingSupplier() throws java.lang.Throwable {
            this.payloads.bufferExactFailingSupplier.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exactBadRequest() throws java.lang.Throwable {
            this.payloads.exactBadRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipBadRequest() throws java.lang.Throwable {
            this.payloads.skipBadRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overlapBadRequest() throws java.lang.Throwable {
            this.payloads.overlapBadRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferExactBoundedOnNextAfterDispose() throws java.lang.Throwable {
            this.payloads.bufferExactBoundedOnNextAfterDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryCloseCompleteRace() throws java.lang.Throwable {
            this.payloads.boundaryCloseCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeStartEnd() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribeStartEnd.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startEndCancelAfterOneBuffer() throws java.lang.Throwable {
            this.payloads.startEndCancelAfterOneBuffer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startEndCompleteOnBoundary() throws java.lang.Throwable {
            this.payloads.startEndCompleteOnBoundary.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startEndBackpressure() throws java.lang.Throwable {
            this.payloads.startEndBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startEndBackpressureMoreWork() throws java.lang.Throwable {
            this.payloads.startEndBackpressureMoreWork.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBufferTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBufferTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBufferTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBufferTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableBufferTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBufferTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableBufferTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableBufferTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement complete;

            public org.junit.runners.model.Statement skipAndCountOverlappingBuffers;

            public org.junit.runners.model.Statement skipAndCountGaplessBuffers;

            public org.junit.runners.model.Statement skipAndCountBuffersWithGaps;

            public org.junit.runners.model.Statement timedAndCount;

            public org.junit.runners.model.Statement timed;

            public org.junit.runners.model.Statement flowableBasedOpenerAndCloser;

            public org.junit.runners.model.Statement longTimeAction;

            public org.junit.runners.model.Statement bufferStopsWhenUnsubscribed1;

            public org.junit.runners.model.Statement bufferWithBONormal1;

            public org.junit.runners.model.Statement bufferWithBOEmptyLastViaBoundary;

            public org.junit.runners.model.Statement bufferWithBOEmptyLastViaSource;

            public org.junit.runners.model.Statement bufferWithBOEmptyLastViaBoth;

            public org.junit.runners.model.Statement bufferWithBOSourceThrows;

            public org.junit.runners.model.Statement bufferWithBOBoundaryThrows;

            public org.junit.runners.model.Statement bufferWithSizeTake1;

            public org.junit.runners.model.Statement bufferWithSizeSkipTake1;

            public org.junit.runners.model.Statement bufferWithTimeTake1;

            public org.junit.runners.model.Statement bufferWithTimeSkipTake2;

            public org.junit.runners.model.Statement bufferWithBoundaryTake2;

            public org.junit.runners.model.Statement bufferWithStartEndBoundaryTake2;

            public org.junit.runners.model.Statement bufferWithSizeThrows;

            public org.junit.runners.model.Statement bufferWithTimeThrows;

            public org.junit.runners.model.Statement bufferWithTimeAndSize;

            public org.junit.runners.model.Statement bufferWithStartEndStartThrows;

            public org.junit.runners.model.Statement bufferWithStartEndEndFunctionThrows;

            public org.junit.runners.model.Statement bufferWithStartEndEndThrows;

            public org.junit.runners.model.Statement producerRequestThroughBufferWithSize1;

            public org.junit.runners.model.Statement producerRequestThroughBufferWithSize2;

            public org.junit.runners.model.Statement producerRequestThroughBufferWithSize3;

            public org.junit.runners.model.Statement producerRequestThroughBufferWithSize4;

            public org.junit.runners.model.Statement producerRequestOverflowThroughBufferWithSize1;

            public org.junit.runners.model.Statement producerRequestOverflowThroughBufferWithSize2;

            public org.junit.runners.model.Statement producerRequestOverflowThroughBufferWithSize3;

            public org.junit.runners.model.Statement bufferWithTimeDoesntUnsubscribeDownstream;

            public org.junit.runners.model.Statement postCompleteBackpressure;

            public org.junit.runners.model.Statement timeAndSkipOverlap;

            public org.junit.runners.model.Statement timeAndSkipSkip;

            public org.junit.runners.model.Statement timeAndSkipOverlapScheduler;

            public org.junit.runners.model.Statement timeAndSkipSkipDefaultScheduler;

            public org.junit.runners.model.Statement bufferBoundaryHint;

            public org.junit.runners.model.Statement bufferIntoCustomCollection;

            public org.junit.runners.model.Statement bufferSkipIntoCustomCollection;

            public org.junit.runners.model.Statement bufferTimeSkipDefault;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement supplierReturnsNull;

            public org.junit.runners.model.Statement supplierReturnsNull2;

            public org.junit.runners.model.Statement supplierReturnsNull3;

            public org.junit.runners.model.Statement supplierThrows;

            public org.junit.runners.model.Statement supplierThrows2;

            public org.junit.runners.model.Statement supplierThrows3;

            public org.junit.runners.model.Statement supplierThrows4;

            public org.junit.runners.model.Statement supplierThrows5;

            public org.junit.runners.model.Statement supplierThrows6;

            public org.junit.runners.model.Statement restartTimer;

            public org.junit.runners.model.Statement bufferSkipError;

            public org.junit.runners.model.Statement bufferSupplierCrash2;

            public org.junit.runners.model.Statement bufferSkipSupplierCrash2;

            public org.junit.runners.model.Statement bufferOverlapSupplierCrash2;

            public org.junit.runners.model.Statement bufferSkipOverlap;

            public org.junit.runners.model.Statement bufferTimedExactError;

            public org.junit.runners.model.Statement bufferTimedSkipError;

            public org.junit.runners.model.Statement bufferTimedOverlapError;

            public org.junit.runners.model.Statement bufferTimedExactEmpty;

            public org.junit.runners.model.Statement bufferTimedSkipEmpty;

            public org.junit.runners.model.Statement bufferTimedOverlapEmpty;

            public org.junit.runners.model.Statement bufferTimedExactSupplierCrash;

            public org.junit.runners.model.Statement bufferTimedExactBoundedError;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement skipError;

            public org.junit.runners.model.Statement skipSingleResult;

            public org.junit.runners.model.Statement skipBackpressure;

            public org.junit.runners.model.Statement withTimeAndSizeCapacityRace;

            public org.junit.runners.model.Statement noCompletionCancelExact;

            public org.junit.runners.model.Statement noCompletionCancelSkip;

            public org.junit.runners.model.Statement noCompletionCancelOverlap;

            public org.junit.runners.model.Statement boundaryOpenCloseDisposedOnComplete;

            public org.junit.runners.model.Statement bufferedCanCompleteIfOpenNeverCompletesDropping;

            public org.junit.runners.model.Statement bufferedCanCompleteIfOpenNeverCompletesOverlapping;

            public org.junit.runners.model.Statement openClosemainError;

            public org.junit.runners.model.Statement openClosebadSource;

            public org.junit.runners.model.Statement openCloseOpenCompletes;

            public org.junit.runners.model.Statement openCloseOpenCompletesNoBuffers;

            public org.junit.runners.model.Statement openCloseTake;

            public org.junit.runners.model.Statement openCloseEmptyBackpressure;

            public org.junit.runners.model.Statement openCloseErrorBackpressure;

            public org.junit.runners.model.Statement openCloseBadOpen;

            public org.junit.runners.model.Statement openCloseBadClose;

            public org.junit.runners.model.Statement bufferExactBoundaryDoubleOnSubscribe;

            public org.junit.runners.model.Statement bufferExactBoundarySecondBufferCrash;

            public org.junit.runners.model.Statement bufferExactBoundaryBadSource;

            public org.junit.runners.model.Statement bufferExactBoundaryCancelUpfront;

            public org.junit.runners.model.Statement bufferExactBoundaryDisposed;

            public org.junit.runners.model.Statement timedDoubleOnSubscribe;

            public org.junit.runners.model.Statement timedCancelledUpfront;

            public org.junit.runners.model.Statement timedInternalState;

            public org.junit.runners.model.Statement timedSkipDoubleOnSubscribe;

            public org.junit.runners.model.Statement timedSizedDoubleOnSubscribe;

            public org.junit.runners.model.Statement timedSkipInternalState;

            public org.junit.runners.model.Statement timedSkipCancelWhenSecondBuffer;

            public org.junit.runners.model.Statement timedSizeBufferAlreadyCleared;

            public org.junit.runners.model.Statement bufferExactFailingSupplier;

            public org.junit.runners.model.Statement exactBadRequest;

            public org.junit.runners.model.Statement skipBadRequest;

            public org.junit.runners.model.Statement overlapBadRequest;

            public org.junit.runners.model.Statement bufferExactBoundedOnNextAfterDispose;

            public org.junit.runners.model.Statement boundaryCloseCompleteRace;

            public org.junit.runners.model.Statement doubleOnSubscribeStartEnd;

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement startEndCancelAfterOneBuffer;

            public org.junit.runners.model.Statement startEndCompleteOnBoundary;

            public org.junit.runners.model.Statement startEndBackpressure;

            public org.junit.runners.model.Statement startEndBackpressureMoreWork;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.complete = _ClassStatement.forPayload(FlowableBufferTest::complete, "complete", this);
            this.payloads.skipAndCountOverlappingBuffers = _ClassStatement.forPayload(FlowableBufferTest::skipAndCountOverlappingBuffers, "skipAndCountOverlappingBuffers", this);
            this.payloads.skipAndCountGaplessBuffers = _ClassStatement.forPayload(FlowableBufferTest::skipAndCountGaplessBuffers, "skipAndCountGaplessBuffers", this);
            this.payloads.skipAndCountBuffersWithGaps = _ClassStatement.forPayload(FlowableBufferTest::skipAndCountBuffersWithGaps, "skipAndCountBuffersWithGaps", this);
            this.payloads.timedAndCount = _ClassStatement.forPayload(FlowableBufferTest::timedAndCount, "timedAndCount", this);
            this.payloads.timed = _ClassStatement.forPayload(FlowableBufferTest::timed, "timed", this);
            this.payloads.flowableBasedOpenerAndCloser = _ClassStatement.forPayload(FlowableBufferTest::flowableBasedOpenerAndCloser, "flowableBasedOpenerAndCloser", this);
            this.payloads.longTimeAction = _ClassStatement.forPayload(FlowableBufferTest::longTimeAction, "longTimeAction", this);
            this.payloads.bufferStopsWhenUnsubscribed1 = _ClassStatement.forPayload(FlowableBufferTest::bufferStopsWhenUnsubscribed1, "bufferStopsWhenUnsubscribed1", this);
            this.payloads.bufferWithBONormal1 = _ClassStatement.forPayload(FlowableBufferTest::bufferWithBONormal1, "bufferWithBONormal1", this);
            this.payloads.bufferWithBOEmptyLastViaBoundary = _ClassStatement.forPayload(FlowableBufferTest::bufferWithBOEmptyLastViaBoundary, "bufferWithBOEmptyLastViaBoundary", this);
            this.payloads.bufferWithBOEmptyLastViaSource = _ClassStatement.forPayload(FlowableBufferTest::bufferWithBOEmptyLastViaSource, "bufferWithBOEmptyLastViaSource", this);
            this.payloads.bufferWithBOEmptyLastViaBoth = _ClassStatement.forPayload(FlowableBufferTest::bufferWithBOEmptyLastViaBoth, "bufferWithBOEmptyLastViaBoth", this);
            this.payloads.bufferWithBOSourceThrows = _ClassStatement.forPayload(FlowableBufferTest::bufferWithBOSourceThrows, "bufferWithBOSourceThrows", this);
            this.payloads.bufferWithBOBoundaryThrows = _ClassStatement.forPayload(FlowableBufferTest::bufferWithBOBoundaryThrows, "bufferWithBOBoundaryThrows", this);
            this.payloads.bufferWithSizeTake1 = _ClassStatement.forPayload(FlowableBufferTest::bufferWithSizeTake1, "bufferWithSizeTake1", this);
            this.payloads.bufferWithSizeSkipTake1 = _ClassStatement.forPayload(FlowableBufferTest::bufferWithSizeSkipTake1, "bufferWithSizeSkipTake1", this);
            this.payloads.bufferWithTimeTake1 = _ClassStatement.forPayload(FlowableBufferTest::bufferWithTimeTake1, "bufferWithTimeTake1", this);
            this.payloads.bufferWithTimeSkipTake2 = _ClassStatement.forPayload(FlowableBufferTest::bufferWithTimeSkipTake2, "bufferWithTimeSkipTake2", this);
            this.payloads.bufferWithBoundaryTake2 = _ClassStatement.forPayload(FlowableBufferTest::bufferWithBoundaryTake2, "bufferWithBoundaryTake2", this);
            this.payloads.bufferWithStartEndBoundaryTake2 = _ClassStatement.forPayload(FlowableBufferTest::bufferWithStartEndBoundaryTake2, "bufferWithStartEndBoundaryTake2", this);
            this.payloads.bufferWithSizeThrows = _ClassStatement.forPayload(FlowableBufferTest::bufferWithSizeThrows, "bufferWithSizeThrows", this);
            this.payloads.bufferWithTimeThrows = _ClassStatement.forPayload(FlowableBufferTest::bufferWithTimeThrows, "bufferWithTimeThrows", this);
            this.payloads.bufferWithTimeAndSize = _ClassStatement.forPayload(FlowableBufferTest::bufferWithTimeAndSize, "bufferWithTimeAndSize", this);
            this.payloads.bufferWithStartEndStartThrows = _ClassStatement.forPayload(FlowableBufferTest::bufferWithStartEndStartThrows, "bufferWithStartEndStartThrows", this);
            this.payloads.bufferWithStartEndEndFunctionThrows = _ClassStatement.forPayload(FlowableBufferTest::bufferWithStartEndEndFunctionThrows, "bufferWithStartEndEndFunctionThrows", this);
            this.payloads.bufferWithStartEndEndThrows = _ClassStatement.forPayload(FlowableBufferTest::bufferWithStartEndEndThrows, "bufferWithStartEndEndThrows", this);
            this.payloads.producerRequestThroughBufferWithSize1 = _ClassStatement.forPayload(FlowableBufferTest::producerRequestThroughBufferWithSize1, "producerRequestThroughBufferWithSize1", this);
            this.payloads.producerRequestThroughBufferWithSize2 = _ClassStatement.forPayload(FlowableBufferTest::producerRequestThroughBufferWithSize2, "producerRequestThroughBufferWithSize2", this);
            this.payloads.producerRequestThroughBufferWithSize3 = _ClassStatement.forPayload(FlowableBufferTest::producerRequestThroughBufferWithSize3, "producerRequestThroughBufferWithSize3", this);
            this.payloads.producerRequestThroughBufferWithSize4 = _ClassStatement.forPayload(FlowableBufferTest::producerRequestThroughBufferWithSize4, "producerRequestThroughBufferWithSize4", this);
            this.payloads.producerRequestOverflowThroughBufferWithSize1 = _ClassStatement.forPayload(FlowableBufferTest::producerRequestOverflowThroughBufferWithSize1, "producerRequestOverflowThroughBufferWithSize1", this);
            this.payloads.producerRequestOverflowThroughBufferWithSize2 = _ClassStatement.forPayload(FlowableBufferTest::producerRequestOverflowThroughBufferWithSize2, "producerRequestOverflowThroughBufferWithSize2", this);
            this.payloads.producerRequestOverflowThroughBufferWithSize3 = _ClassStatement.forPayload(FlowableBufferTest::producerRequestOverflowThroughBufferWithSize3, "producerRequestOverflowThroughBufferWithSize3", this);
            this.payloads.bufferWithTimeDoesntUnsubscribeDownstream = _ClassStatement.forPayload(FlowableBufferTest::bufferWithTimeDoesntUnsubscribeDownstream, "bufferWithTimeDoesntUnsubscribeDownstream", this);
            this.payloads.postCompleteBackpressure = _ClassStatement.forPayload(FlowableBufferTest::postCompleteBackpressure, "postCompleteBackpressure", this);
            this.payloads.timeAndSkipOverlap = _ClassStatement.forPayload(FlowableBufferTest::timeAndSkipOverlap, "timeAndSkipOverlap", this);
            this.payloads.timeAndSkipSkip = _ClassStatement.forPayload(FlowableBufferTest::timeAndSkipSkip, "timeAndSkipSkip", this);
            this.payloads.timeAndSkipOverlapScheduler = _ClassStatement.forPayload(FlowableBufferTest::timeAndSkipOverlapScheduler, "timeAndSkipOverlapScheduler", this);
            this.payloads.timeAndSkipSkipDefaultScheduler = _ClassStatement.forPayload(FlowableBufferTest::timeAndSkipSkipDefaultScheduler, "timeAndSkipSkipDefaultScheduler", this);
            this.payloads.bufferBoundaryHint = _ClassStatement.forPayload(FlowableBufferTest::bufferBoundaryHint, "bufferBoundaryHint", this);
            this.payloads.bufferIntoCustomCollection = _ClassStatement.forPayload(FlowableBufferTest::bufferIntoCustomCollection, "bufferIntoCustomCollection", this);
            this.payloads.bufferSkipIntoCustomCollection = _ClassStatement.forPayload(FlowableBufferTest::bufferSkipIntoCustomCollection, "bufferSkipIntoCustomCollection", this);
            this.payloads.bufferTimeSkipDefault = _ClassStatement.forPayload(FlowableBufferTest::bufferTimeSkipDefault, "bufferTimeSkipDefault", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableBufferTest::dispose, "dispose", this);
            this.payloads.supplierReturnsNull = _ClassStatement.forPayload(FlowableBufferTest::supplierReturnsNull, "supplierReturnsNull", this);
            this.payloads.supplierReturnsNull2 = _ClassStatement.forPayload(FlowableBufferTest::supplierReturnsNull2, "supplierReturnsNull2", this);
            this.payloads.supplierReturnsNull3 = _ClassStatement.forPayload(FlowableBufferTest::supplierReturnsNull3, "supplierReturnsNull3", this);
            this.payloads.supplierThrows = _ClassStatement.forPayload(FlowableBufferTest::supplierThrows, "supplierThrows", this);
            this.payloads.supplierThrows2 = _ClassStatement.forPayload(FlowableBufferTest::supplierThrows2, "supplierThrows2", this);
            this.payloads.supplierThrows3 = _ClassStatement.forPayload(FlowableBufferTest::supplierThrows3, "supplierThrows3", this);
            this.payloads.supplierThrows4 = _ClassStatement.forPayload(FlowableBufferTest::supplierThrows4, "supplierThrows4", this);
            this.payloads.supplierThrows5 = _ClassStatement.forPayload(FlowableBufferTest::supplierThrows5, "supplierThrows5", this);
            this.payloads.supplierThrows6 = _ClassStatement.forPayload(FlowableBufferTest::supplierThrows6, "supplierThrows6", this);
            this.payloads.restartTimer = _ClassStatement.forPayload(FlowableBufferTest::restartTimer, "restartTimer", this);
            this.payloads.bufferSkipError = _ClassStatement.forPayload(FlowableBufferTest::bufferSkipError, "bufferSkipError", this);
            this.payloads.bufferSupplierCrash2 = _ClassStatement.forPayload(FlowableBufferTest::bufferSupplierCrash2, "bufferSupplierCrash2", this);
            this.payloads.bufferSkipSupplierCrash2 = _ClassStatement.forPayload(FlowableBufferTest::bufferSkipSupplierCrash2, "bufferSkipSupplierCrash2", this);
            this.payloads.bufferOverlapSupplierCrash2 = _ClassStatement.forPayload(FlowableBufferTest::bufferOverlapSupplierCrash2, "bufferOverlapSupplierCrash2", this);
            this.payloads.bufferSkipOverlap = _ClassStatement.forPayload(FlowableBufferTest::bufferSkipOverlap, "bufferSkipOverlap", this);
            this.payloads.bufferTimedExactError = _ClassStatement.forPayload(FlowableBufferTest::bufferTimedExactError, "bufferTimedExactError", this);
            this.payloads.bufferTimedSkipError = _ClassStatement.forPayload(FlowableBufferTest::bufferTimedSkipError, "bufferTimedSkipError", this);
            this.payloads.bufferTimedOverlapError = _ClassStatement.forPayload(FlowableBufferTest::bufferTimedOverlapError, "bufferTimedOverlapError", this);
            this.payloads.bufferTimedExactEmpty = _ClassStatement.forPayload(FlowableBufferTest::bufferTimedExactEmpty, "bufferTimedExactEmpty", this);
            this.payloads.bufferTimedSkipEmpty = _ClassStatement.forPayload(FlowableBufferTest::bufferTimedSkipEmpty, "bufferTimedSkipEmpty", this);
            this.payloads.bufferTimedOverlapEmpty = _ClassStatement.forPayload(FlowableBufferTest::bufferTimedOverlapEmpty, "bufferTimedOverlapEmpty", this);
            this.payloads.bufferTimedExactSupplierCrash = _ClassStatement.forPayload(FlowableBufferTest::bufferTimedExactSupplierCrash, "bufferTimedExactSupplierCrash", this);
            this.payloads.bufferTimedExactBoundedError = _ClassStatement.forPayload(FlowableBufferTest::bufferTimedExactBoundedError, "bufferTimedExactBoundedError", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableBufferTest::badSource, "badSource", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableBufferTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableBufferTest::badRequest, "badRequest", this);
            this.payloads.skipError = _ClassStatement.forPayload(FlowableBufferTest::skipError, "skipError", this);
            this.payloads.skipSingleResult = _ClassStatement.forPayload(FlowableBufferTest::skipSingleResult, "skipSingleResult", this);
            this.payloads.skipBackpressure = _ClassStatement.forPayload(FlowableBufferTest::skipBackpressure, "skipBackpressure", this);
            this.payloads.withTimeAndSizeCapacityRace = _ClassStatement.forPayload(FlowableBufferTest::withTimeAndSizeCapacityRace, "withTimeAndSizeCapacityRace", this);
            this.payloads.noCompletionCancelExact = _ClassStatement.forPayload(FlowableBufferTest::noCompletionCancelExact, "noCompletionCancelExact", this);
            this.payloads.noCompletionCancelSkip = _ClassStatement.forPayload(FlowableBufferTest::noCompletionCancelSkip, "noCompletionCancelSkip", this);
            this.payloads.noCompletionCancelOverlap = _ClassStatement.forPayload(FlowableBufferTest::noCompletionCancelOverlap, "noCompletionCancelOverlap", this);
            this.payloads.boundaryOpenCloseDisposedOnComplete = _ClassStatement.forPayload(FlowableBufferTest::boundaryOpenCloseDisposedOnComplete, "boundaryOpenCloseDisposedOnComplete", this);
            this.payloads.bufferedCanCompleteIfOpenNeverCompletesDropping = _ClassStatement.forPayload(FlowableBufferTest::bufferedCanCompleteIfOpenNeverCompletesDropping, "bufferedCanCompleteIfOpenNeverCompletesDropping", this);
            this.payloads.bufferedCanCompleteIfOpenNeverCompletesOverlapping = _ClassStatement.forPayload(FlowableBufferTest::bufferedCanCompleteIfOpenNeverCompletesOverlapping, "bufferedCanCompleteIfOpenNeverCompletesOverlapping", this);
            this.payloads.openClosemainError = _ClassStatement.forPayload(FlowableBufferTest::openClosemainError, "openClosemainError", this);
            this.payloads.openClosebadSource = _ClassStatement.forPayload(FlowableBufferTest::openClosebadSource, "openClosebadSource", this);
            this.payloads.openCloseOpenCompletes = _ClassStatement.forPayload(FlowableBufferTest::openCloseOpenCompletes, "openCloseOpenCompletes", this);
            this.payloads.openCloseOpenCompletesNoBuffers = _ClassStatement.forPayload(FlowableBufferTest::openCloseOpenCompletesNoBuffers, "openCloseOpenCompletesNoBuffers", this);
            this.payloads.openCloseTake = _ClassStatement.forPayload(FlowableBufferTest::openCloseTake, "openCloseTake", this);
            this.payloads.openCloseEmptyBackpressure = _ClassStatement.forPayload(FlowableBufferTest::openCloseEmptyBackpressure, "openCloseEmptyBackpressure", this);
            this.payloads.openCloseErrorBackpressure = _ClassStatement.forPayload(FlowableBufferTest::openCloseErrorBackpressure, "openCloseErrorBackpressure", this);
            this.payloads.openCloseBadOpen = _ClassStatement.forPayload(FlowableBufferTest::openCloseBadOpen, "openCloseBadOpen", this);
            this.payloads.openCloseBadClose = _ClassStatement.forPayload(FlowableBufferTest::openCloseBadClose, "openCloseBadClose", this);
            this.payloads.bufferExactBoundaryDoubleOnSubscribe = _ClassStatement.forPayload(FlowableBufferTest::bufferExactBoundaryDoubleOnSubscribe, "bufferExactBoundaryDoubleOnSubscribe", this);
            this.payloads.bufferExactBoundarySecondBufferCrash = _ClassStatement.forPayload(FlowableBufferTest::bufferExactBoundarySecondBufferCrash, "bufferExactBoundarySecondBufferCrash", this);
            this.payloads.bufferExactBoundaryBadSource = _ClassStatement.forPayload(FlowableBufferTest::bufferExactBoundaryBadSource, "bufferExactBoundaryBadSource", this);
            this.payloads.bufferExactBoundaryCancelUpfront = _ClassStatement.forPayload(FlowableBufferTest::bufferExactBoundaryCancelUpfront, "bufferExactBoundaryCancelUpfront", this);
            this.payloads.bufferExactBoundaryDisposed = _ClassStatement.forPayload(FlowableBufferTest::bufferExactBoundaryDisposed, "bufferExactBoundaryDisposed", this);
            this.payloads.timedDoubleOnSubscribe = _ClassStatement.forPayload(FlowableBufferTest::timedDoubleOnSubscribe, "timedDoubleOnSubscribe", this);
            this.payloads.timedCancelledUpfront = _ClassStatement.forPayload(FlowableBufferTest::timedCancelledUpfront, "timedCancelledUpfront", this);
            this.payloads.timedInternalState = _ClassStatement.forPayload(FlowableBufferTest::timedInternalState, "timedInternalState", this);
            this.payloads.timedSkipDoubleOnSubscribe = _ClassStatement.forPayload(FlowableBufferTest::timedSkipDoubleOnSubscribe, "timedSkipDoubleOnSubscribe", this);
            this.payloads.timedSizedDoubleOnSubscribe = _ClassStatement.forPayload(FlowableBufferTest::timedSizedDoubleOnSubscribe, "timedSizedDoubleOnSubscribe", this);
            this.payloads.timedSkipInternalState = _ClassStatement.forPayload(FlowableBufferTest::timedSkipInternalState, "timedSkipInternalState", this);
            this.payloads.timedSkipCancelWhenSecondBuffer = _ClassStatement.forPayload(FlowableBufferTest::timedSkipCancelWhenSecondBuffer, "timedSkipCancelWhenSecondBuffer", this);
            this.payloads.timedSizeBufferAlreadyCleared = _ClassStatement.forPayload(FlowableBufferTest::timedSizeBufferAlreadyCleared, "timedSizeBufferAlreadyCleared", this);
            this.payloads.bufferExactFailingSupplier = _ClassStatement.forPayload(FlowableBufferTest::bufferExactFailingSupplier, "bufferExactFailingSupplier", this);
            this.payloads.exactBadRequest = _ClassStatement.forPayload(FlowableBufferTest::exactBadRequest, "exactBadRequest", this);
            this.payloads.skipBadRequest = _ClassStatement.forPayload(FlowableBufferTest::skipBadRequest, "skipBadRequest", this);
            this.payloads.overlapBadRequest = _ClassStatement.forPayload(FlowableBufferTest::overlapBadRequest, "overlapBadRequest", this);
            this.payloads.bufferExactBoundedOnNextAfterDispose = _ClassStatement.forPayload(FlowableBufferTest::bufferExactBoundedOnNextAfterDispose, "bufferExactBoundedOnNextAfterDispose", this);
            this.payloads.boundaryCloseCompleteRace = _ClassStatement.forPayload(FlowableBufferTest::boundaryCloseCompleteRace, "boundaryCloseCompleteRace", this);
            this.payloads.doubleOnSubscribeStartEnd = _ClassStatement.forPayload(FlowableBufferTest::doubleOnSubscribeStartEnd, "doubleOnSubscribeStartEnd", this);
            this.payloads.cancel = _ClassStatement.forPayload(FlowableBufferTest::cancel, "cancel", this);
            this.payloads.startEndCancelAfterOneBuffer = _ClassStatement.forPayload(FlowableBufferTest::startEndCancelAfterOneBuffer, "startEndCancelAfterOneBuffer", this);
            this.payloads.startEndCompleteOnBoundary = _ClassStatement.forPayload(FlowableBufferTest::startEndCompleteOnBoundary, "startEndCompleteOnBoundary", this);
            this.payloads.startEndBackpressure = _ClassStatement.forPayload(FlowableBufferTest::startEndBackpressure, "startEndBackpressure", this);
            this.payloads.startEndBackpressureMoreWork = _ClassStatement.forPayload(FlowableBufferTest::startEndBackpressureMoreWork, "startEndBackpressureMoreWork", this);
        }
    }
}
