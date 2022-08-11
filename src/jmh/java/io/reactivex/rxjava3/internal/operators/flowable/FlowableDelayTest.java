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
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableDelayTest extends RxJavaTest {

    private Subscriber<Long> subscriber;

    private Subscriber<Long> subscriber2;

    private TestScheduler scheduler;

    @Before
    public void before() {
        subscriber = TestHelper.mockSubscriber();
        subscriber2 = TestHelper.mockSubscriber();
        scheduler = new TestScheduler();
    }

    @Test
    public void delay() {
        Flowable<Long> source = Flowable.interval(1L, TimeUnit.SECONDS, scheduler).take(3);
        Flowable<Long> delayed = source.delay(500L, TimeUnit.MILLISECONDS, scheduler);
        delayed.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        scheduler.advanceTimeTo(1499L, TimeUnit.MILLISECONDS);
        verify(subscriber, never()).onNext(anyLong());
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(1500L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(0L);
        inOrder.verify(subscriber, never()).onNext(anyLong());
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(2400L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, never()).onNext(anyLong());
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(2500L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(1L);
        inOrder.verify(subscriber, never()).onNext(anyLong());
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(3400L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, never()).onNext(anyLong());
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(3500L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(2L);
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void longDelay() {
        Flowable<Long> source = Flowable.interval(1L, TimeUnit.SECONDS, scheduler).take(3);
        Flowable<Long> delayed = source.delay(5L, TimeUnit.SECONDS, scheduler);
        delayed.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        scheduler.advanceTimeTo(5999L, TimeUnit.MILLISECONDS);
        verify(subscriber, never()).onNext(anyLong());
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(6000L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(0L);
        scheduler.advanceTimeTo(6999L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, never()).onNext(anyLong());
        scheduler.advanceTimeTo(7000L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(1L);
        scheduler.advanceTimeTo(7999L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, never()).onNext(anyLong());
        scheduler.advanceTimeTo(8000L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(2L);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verify(subscriber, never()).onNext(anyLong());
        inOrder.verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void delayWithError() {
        Flowable<Long> source = Flowable.interval(1L, TimeUnit.SECONDS, scheduler).map(new Function<Long, Long>() {

            @Override
            public Long apply(Long value) {
                if (value == 1L) {
                    throw new RuntimeException("error!");
                }
                return value;
            }
        });
        Flowable<Long> delayed = source.delay(1L, TimeUnit.SECONDS, scheduler);
        delayed.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        scheduler.advanceTimeTo(1999L, TimeUnit.MILLISECONDS);
        verify(subscriber, never()).onNext(anyLong());
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(2000L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onError(any(Throwable.class));
        inOrder.verify(subscriber, never()).onNext(anyLong());
        verify(subscriber, never()).onComplete();
        scheduler.advanceTimeTo(5000L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, never()).onNext(anyLong());
        inOrder.verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void delayWithMultipleSubscriptions() {
        Flowable<Long> source = Flowable.interval(1L, TimeUnit.SECONDS, scheduler).take(3);
        Flowable<Long> delayed = source.delay(500L, TimeUnit.MILLISECONDS, scheduler);
        delayed.subscribe(subscriber);
        delayed.subscribe(subscriber2);
        InOrder inOrder = inOrder(subscriber);
        InOrder inOrder2 = inOrder(subscriber2);
        scheduler.advanceTimeTo(1499L, TimeUnit.MILLISECONDS);
        verify(subscriber, never()).onNext(anyLong());
        verify(subscriber2, never()).onNext(anyLong());
        scheduler.advanceTimeTo(1500L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(0L);
        inOrder2.verify(subscriber2, times(1)).onNext(0L);
        scheduler.advanceTimeTo(2499L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, never()).onNext(anyLong());
        inOrder2.verify(subscriber2, never()).onNext(anyLong());
        scheduler.advanceTimeTo(2500L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(1L);
        inOrder2.verify(subscriber2, times(1)).onNext(1L);
        verify(subscriber, never()).onComplete();
        verify(subscriber2, never()).onComplete();
        scheduler.advanceTimeTo(3500L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(2L);
        inOrder2.verify(subscriber2, times(1)).onNext(2L);
        inOrder.verify(subscriber, never()).onNext(anyLong());
        inOrder2.verify(subscriber2, never()).onNext(anyLong());
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder2.verify(subscriber2, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber2, never()).onError(any(Throwable.class));
    }

    @Test
    public void delaySubscription() {
        Flowable<Integer> result = Flowable.just(1, 2, 3).delaySubscription(100, TimeUnit.MILLISECONDS, scheduler);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        result.subscribe(subscriber);
        inOrder.verify(subscriber, never()).onNext(any());
        inOrder.verify(subscriber, never()).onComplete();
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onNext(3);
        inOrder.verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void delaySubscriptionCancelBeforeTime() {
        Flowable<Integer> result = Flowable.just(1, 2, 3).delaySubscription(100, TimeUnit.MILLISECONDS, scheduler);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<Object> ts = new TestSubscriber<>(subscriber);
        result.subscribe(ts);
        ts.cancel();
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void delayWithFlowableNormal1() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final List<PublishProcessor<Integer>> delays = new ArrayList<>();
        final int n = 10;
        for (int i = 0; i < n; i++) {
            PublishProcessor<Integer> delay = PublishProcessor.create();
            delays.add(delay);
        }
        Function<Integer, Flowable<Integer>> delayFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return delays.get(t1);
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.delay(delayFunc).subscribe(subscriber);
        for (int i = 0; i < n; i++) {
            source.onNext(i);
            delays.get(i).onNext(i);
            inOrder.verify(subscriber).onNext(i);
        }
        source.onComplete();
        inOrder.verify(subscriber).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void delayWithFlowableSingleSend1() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> delay = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> delayFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return delay;
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.delay(delayFunc).subscribe(subscriber);
        source.onNext(1);
        delay.onNext(1);
        delay.onNext(2);
        inOrder.verify(subscriber).onNext(1);
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void delayWithFlowableSourceThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> delay = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> delayFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return delay;
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.delay(delayFunc).subscribe(subscriber);
        source.onNext(1);
        source.onError(new TestException());
        delay.onNext(1);
        inOrder.verify(subscriber).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void delayWithFlowableDelayFunctionThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> delayFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                throw new TestException();
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.delay(delayFunc).subscribe(subscriber);
        source.onNext(1);
        inOrder.verify(subscriber).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void delayWithFlowableDelayThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> delay = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> delayFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return delay;
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.delay(delayFunc).subscribe(subscriber);
        source.onNext(1);
        delay.onError(new TestException());
        inOrder.verify(subscriber).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void delayWithFlowableSubscriptionNormal() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> delay = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> delayFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return delay;
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.delay(delay, delayFunc).subscribe(subscriber);
        source.onNext(1);
        delay.onNext(1);
        source.onNext(2);
        delay.onNext(2);
        inOrder.verify(subscriber).onNext(2);
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void delayWithFlowableSubscriptionFunctionThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> delay = PublishProcessor.create();
        Supplier<Flowable<Integer>> subFunc = new Supplier<Flowable<Integer>>() {

            @Override
            public Flowable<Integer> get() {
                throw new TestException();
            }
        };
        Function<Integer, Flowable<Integer>> delayFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return delay;
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.delay(Flowable.defer(subFunc), delayFunc).subscribe(subscriber);
        source.onNext(1);
        delay.onNext(1);
        source.onNext(2);
        inOrder.verify(subscriber).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void delayWithFlowableSubscriptionThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> delay = PublishProcessor.create();
        Supplier<Flowable<Integer>> subFunc = new Supplier<Flowable<Integer>>() {

            @Override
            public Flowable<Integer> get() {
                return delay;
            }
        };
        Function<Integer, Flowable<Integer>> delayFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return delay;
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.delay(Flowable.defer(subFunc), delayFunc).subscribe(subscriber);
        source.onNext(1);
        delay.onError(new TestException());
        source.onNext(2);
        inOrder.verify(subscriber).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void delayWithFlowableEmptyDelayer() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> delayFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return Flowable.empty();
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.delay(delayFunc).subscribe(subscriber);
        source.onNext(1);
        source.onComplete();
        inOrder.verify(subscriber).onNext(1);
        inOrder.verify(subscriber).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void delayWithFlowableSubscriptionRunCompletion() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> sdelay = PublishProcessor.create();
        final PublishProcessor<Integer> delay = PublishProcessor.create();
        Supplier<Flowable<Integer>> subFunc = new Supplier<Flowable<Integer>>() {

            @Override
            public Flowable<Integer> get() {
                return sdelay;
            }
        };
        Function<Integer, Flowable<Integer>> delayFunc = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return delay;
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.delay(Flowable.defer(subFunc), delayFunc).subscribe(subscriber);
        source.onNext(1);
        sdelay.onComplete();
        source.onNext(2);
        delay.onNext(2);
        inOrder.verify(subscriber).onNext(2);
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void delayWithFlowableAsTimed() {
        Flowable<Long> source = Flowable.interval(1L, TimeUnit.SECONDS, scheduler).take(3);
        final Flowable<Long> delayer = Flowable.timer(500L, TimeUnit.MILLISECONDS, scheduler);
        Function<Long, Flowable<Long>> delayFunc = new Function<Long, Flowable<Long>>() {

            @Override
            public Flowable<Long> apply(Long t1) {
                return delayer;
            }
        };
        Flowable<Long> delayed = source.delay(delayFunc);
        delayed.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        scheduler.advanceTimeTo(1499L, TimeUnit.MILLISECONDS);
        verify(subscriber, never()).onNext(anyLong());
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(1500L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(0L);
        inOrder.verify(subscriber, never()).onNext(anyLong());
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(2400L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, never()).onNext(anyLong());
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(2500L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(1L);
        inOrder.verify(subscriber, never()).onNext(anyLong());
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(3400L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, never()).onNext(anyLong());
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(3500L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(2L);
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void delayWithFlowableReorder() {
        int n = 3;
        PublishProcessor<Integer> source = PublishProcessor.create();
        final List<PublishProcessor<Integer>> subjects = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            subjects.add(PublishProcessor.<Integer>create());
        }
        Flowable<Integer> result = source.delay(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return subjects.get(t1);
            }
        });
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        result.subscribe(subscriber);
        for (int i = 0; i < n; i++) {
            source.onNext(i);
        }
        source.onComplete();
        inOrder.verify(subscriber, never()).onNext(anyInt());
        inOrder.verify(subscriber, never()).onComplete();
        for (int i = n - 1; i >= 0; i--) {
            subjects.get(i).onComplete();
            inOrder.verify(subscriber).onNext(i);
        }
        inOrder.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void delayEmitsEverything() {
        Flowable<Integer> source = Flowable.range(1, 5);
        Flowable<Integer> delayed = source.delay(500L, TimeUnit.MILLISECONDS, scheduler);
        delayed = delayed.doOnEach(new Consumer<Notification<Integer>>() {

            @Override
            public void accept(Notification<Integer> t1) {
                System.out.println(t1);
            }
        });
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        delayed.subscribe(ts);
        // all will be delivered after 500ms since range does not delay between them
        scheduler.advanceTimeBy(500L, TimeUnit.MILLISECONDS);
        ts.assertValues(1, 2, 3, 4, 5);
    }

    @Test
    public void backpressureWithTimedDelay() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, Flowable.bufferSize() * 2).delay(100, TimeUnit.MILLISECONDS).observeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

            int c;

            @Override
            public Integer apply(Integer t) {
                if (c++ <= 0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
                return t;
            }
        }).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        assertEquals(Flowable.bufferSize() * 2, ts.values().size());
    }

    @Test
    public void backpressureWithSubscriptionTimedDelay() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, Flowable.bufferSize() * 2).delaySubscription(100, TimeUnit.MILLISECONDS).delay(100, TimeUnit.MILLISECONDS).observeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

            int c;

            @Override
            public Integer apply(Integer t) {
                if (c++ <= 0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
                return t;
            }
        }).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        assertEquals(Flowable.bufferSize() * 2, ts.values().size());
    }

    @Test
    public void backpressureWithSelectorDelay() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, Flowable.bufferSize() * 2).delay(new Function<Integer, Flowable<Long>>() {

            @Override
            public Flowable<Long> apply(Integer i) {
                return Flowable.timer(100, TimeUnit.MILLISECONDS);
            }
        }).observeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

            int c;

            @Override
            public Integer apply(Integer t) {
                if (c++ <= 0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
                return t;
            }
        }).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        assertEquals(Flowable.bufferSize() * 2, ts.values().size());
    }

    @Test
    public void backpressureWithSelectorDelayAndSubscriptionDelay() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, Flowable.bufferSize() * 2).delay(Flowable.defer(new Supplier<Flowable<Long>>() {

            @Override
            public Flowable<Long> get() {
                return Flowable.timer(500, TimeUnit.MILLISECONDS);
            }
        }), new Function<Integer, Flowable<Long>>() {

            @Override
            public Flowable<Long> apply(Integer i) {
                return Flowable.timer(100, TimeUnit.MILLISECONDS);
            }
        }).observeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

            int c;

            @Override
            public Integer apply(Integer t) {
                if (c++ <= 0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
                return t;
            }
        }).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        assertEquals(Flowable.bufferSize() * 2, ts.values().size());
    }

    @Test
    public void errorRunsBeforeOnNext() {
        TestScheduler test = new TestScheduler();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        pp.delay(1, TimeUnit.SECONDS, test).subscribe(ts);
        pp.onNext(1);
        test.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        pp.onError(new TestException());
        test.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void delaySupplierSimple() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        Flowable<Integer> source = Flowable.range(1, 5);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        source.delaySubscription(Flowable.defer(new Supplier<Publisher<Integer>>() {

            @Override
            public Publisher<Integer> get() {
                return pp;
            }
        })).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        pp.onNext(1);
        ts.assertValues(1, 2, 3, 4, 5);
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void delaySupplierCompletes() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        Flowable<Integer> source = Flowable.range(1, 5);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        source.delaySubscription(Flowable.defer(new Supplier<Publisher<Integer>>() {

            @Override
            public Publisher<Integer> get() {
                return pp;
            }
        })).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        // FIXME should this complete the source instead of consuming it?
        pp.onComplete();
        ts.assertValues(1, 2, 3, 4, 5);
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void delaySupplierErrors() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        Flowable<Integer> source = Flowable.range(1, 5);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        source.delaySubscription(Flowable.defer(new Supplier<Publisher<Integer>>() {

            @Override
            public Publisher<Integer> get() {
                return pp;
            }
        })).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        pp.onError(new TestException());
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(TestException.class);
    }

    @Test
    public void delayAndTakeUntilNeverSubscribeToSource() {
        PublishProcessor<Integer> delayUntil = PublishProcessor.create();
        PublishProcessor<Integer> interrupt = PublishProcessor.create();
        final AtomicBoolean subscribed = new AtomicBoolean(false);
        Flowable.just(1).doOnSubscribe(new Consumer<Object>() {

            @Override
            public void accept(Object o) {
                subscribed.set(true);
            }
        }).delaySubscription(delayUntil).takeUntil(interrupt).subscribe();
        interrupt.onNext(9000);
        delayUntil.onNext(1);
        Assert.assertFalse(subscribed.get());
    }

    @Test
    public void delayWithTimeDelayError() throws Exception {
        Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).delay(100, TimeUnit.MILLISECONDS, true).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class, 1);
    }

    @Test
    public void delaySubscriptionDisposeBeforeTime() {
        Flowable<Integer> result = Flowable.just(1, 2, 3).delaySubscription(100, TimeUnit.MILLISECONDS, scheduler);
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<Object> ts = new TestSubscriber<>(subscriber);
        result.subscribe(ts);
        ts.cancel();
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void onErrorCalledOnScheduler() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Thread> thread = new AtomicReference<>();
        Flowable.<String>error(new Exception()).delay(0, TimeUnit.MILLISECONDS, Schedulers.newThread()).doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable throwable) throws Exception {
                thread.set(Thread.currentThread());
                latch.countDown();
            }
        }).onErrorResumeWith(Flowable.<String>empty()).subscribe();
        latch.await();
        assertNotEquals(Thread.currentThread(), thread.get());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().delay(1, TimeUnit.SECONDS));
        TestHelper.checkDisposed(PublishProcessor.create().delay(Functions.justFunction(Flowable.never())));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.delay(1, TimeUnit.SECONDS);
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.delay(Functions.justFunction(Flowable.never()));
            }
        });
    }

    @Test
    public void onCompleteFinal() {
        TestScheduler scheduler = new TestScheduler();
        Flowable.empty().delay(1, TimeUnit.MILLISECONDS, scheduler).subscribe(new DisposableSubscriber<Object>() {

            @Override
            public void onNext(Object value) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                throw new TestException();
            }
        });
        try {
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            fail("Should have thrown");
        } catch (TestException ex) {
        // expected
        }
    }

    @Test
    public void onErrorFinal() {
        TestScheduler scheduler = new TestScheduler();
        Flowable.error(new TestException()).delay(1, TimeUnit.MILLISECONDS, scheduler).subscribe(new DisposableSubscriber<Object>() {

            @Override
            public void onNext(Object value) {
            }

            @Override
            public void onError(Throwable e) {
                throw new TestException();
            }

            @Override
            public void onComplete() {
            }
        });
        try {
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            fail("Should have thrown");
        } catch (TestException ex) {
        // expected
        }
    }

    @Test
    public void itemDelayReturnsNull() {
        Flowable.just(1).delay(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer t) throws Exception {
                return null;
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(NullPointerException.class, "The itemDelay returned a null Publisher");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableDelayTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delay() throws java.lang.Throwable {
            this.payloads.delay.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_longDelay() throws java.lang.Throwable {
            this.payloads.longDelay.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithError() throws java.lang.Throwable {
            this.payloads.delayWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithMultipleSubscriptions() throws java.lang.Throwable {
            this.payloads.delayWithMultipleSubscriptions.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delaySubscription() throws java.lang.Throwable {
            this.payloads.delaySubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delaySubscriptionCancelBeforeTime() throws java.lang.Throwable {
            this.payloads.delaySubscriptionCancelBeforeTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithFlowableNormal1() throws java.lang.Throwable {
            this.payloads.delayWithFlowableNormal1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithFlowableSingleSend1() throws java.lang.Throwable {
            this.payloads.delayWithFlowableSingleSend1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithFlowableSourceThrows() throws java.lang.Throwable {
            this.payloads.delayWithFlowableSourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithFlowableDelayFunctionThrows() throws java.lang.Throwable {
            this.payloads.delayWithFlowableDelayFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithFlowableDelayThrows() throws java.lang.Throwable {
            this.payloads.delayWithFlowableDelayThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithFlowableSubscriptionNormal() throws java.lang.Throwable {
            this.payloads.delayWithFlowableSubscriptionNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithFlowableSubscriptionFunctionThrows() throws java.lang.Throwable {
            this.payloads.delayWithFlowableSubscriptionFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithFlowableSubscriptionThrows() throws java.lang.Throwable {
            this.payloads.delayWithFlowableSubscriptionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithFlowableEmptyDelayer() throws java.lang.Throwable {
            this.payloads.delayWithFlowableEmptyDelayer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithFlowableSubscriptionRunCompletion() throws java.lang.Throwable {
            this.payloads.delayWithFlowableSubscriptionRunCompletion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithFlowableAsTimed() throws java.lang.Throwable {
            this.payloads.delayWithFlowableAsTimed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithFlowableReorder() throws java.lang.Throwable {
            this.payloads.delayWithFlowableReorder.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayEmitsEverything() throws java.lang.Throwable {
            this.payloads.delayEmitsEverything.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithTimedDelay() throws java.lang.Throwable {
            this.payloads.backpressureWithTimedDelay.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithSubscriptionTimedDelay() throws java.lang.Throwable {
            this.payloads.backpressureWithSubscriptionTimedDelay.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithSelectorDelay() throws java.lang.Throwable {
            this.payloads.backpressureWithSelectorDelay.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithSelectorDelayAndSubscriptionDelay() throws java.lang.Throwable {
            this.payloads.backpressureWithSelectorDelayAndSubscriptionDelay.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorRunsBeforeOnNext() throws java.lang.Throwable {
            this.payloads.errorRunsBeforeOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delaySupplierSimple() throws java.lang.Throwable {
            this.payloads.delaySupplierSimple.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delaySupplierCompletes() throws java.lang.Throwable {
            this.payloads.delaySupplierCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delaySupplierErrors() throws java.lang.Throwable {
            this.payloads.delaySupplierErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayAndTakeUntilNeverSubscribeToSource() throws java.lang.Throwable {
            this.payloads.delayAndTakeUntilNeverSubscribeToSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithTimeDelayError() throws java.lang.Throwable {
            this.payloads.delayWithTimeDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delaySubscriptionDisposeBeforeTime() throws java.lang.Throwable {
            this.payloads.delaySubscriptionDisposeBeforeTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCalledOnScheduler() throws java.lang.Throwable {
            this.payloads.onErrorCalledOnScheduler.evaluate();
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
        public void benchmark_onCompleteFinal() throws java.lang.Throwable {
            this.payloads.onCompleteFinal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorFinal() throws java.lang.Throwable {
            this.payloads.onErrorFinal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_itemDelayReturnsNull() throws java.lang.Throwable {
            this.payloads.itemDelayReturnsNull.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDelayTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDelayTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDelayTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDelayTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableDelayTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDelayTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableDelayTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableDelayTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement delay;

            public org.junit.runners.model.Statement longDelay;

            public org.junit.runners.model.Statement delayWithError;

            public org.junit.runners.model.Statement delayWithMultipleSubscriptions;

            public org.junit.runners.model.Statement delaySubscription;

            public org.junit.runners.model.Statement delaySubscriptionCancelBeforeTime;

            public org.junit.runners.model.Statement delayWithFlowableNormal1;

            public org.junit.runners.model.Statement delayWithFlowableSingleSend1;

            public org.junit.runners.model.Statement delayWithFlowableSourceThrows;

            public org.junit.runners.model.Statement delayWithFlowableDelayFunctionThrows;

            public org.junit.runners.model.Statement delayWithFlowableDelayThrows;

            public org.junit.runners.model.Statement delayWithFlowableSubscriptionNormal;

            public org.junit.runners.model.Statement delayWithFlowableSubscriptionFunctionThrows;

            public org.junit.runners.model.Statement delayWithFlowableSubscriptionThrows;

            public org.junit.runners.model.Statement delayWithFlowableEmptyDelayer;

            public org.junit.runners.model.Statement delayWithFlowableSubscriptionRunCompletion;

            public org.junit.runners.model.Statement delayWithFlowableAsTimed;

            public org.junit.runners.model.Statement delayWithFlowableReorder;

            public org.junit.runners.model.Statement delayEmitsEverything;

            public org.junit.runners.model.Statement backpressureWithTimedDelay;

            public org.junit.runners.model.Statement backpressureWithSubscriptionTimedDelay;

            public org.junit.runners.model.Statement backpressureWithSelectorDelay;

            public org.junit.runners.model.Statement backpressureWithSelectorDelayAndSubscriptionDelay;

            public org.junit.runners.model.Statement errorRunsBeforeOnNext;

            public org.junit.runners.model.Statement delaySupplierSimple;

            public org.junit.runners.model.Statement delaySupplierCompletes;

            public org.junit.runners.model.Statement delaySupplierErrors;

            public org.junit.runners.model.Statement delayAndTakeUntilNeverSubscribeToSource;

            public org.junit.runners.model.Statement delayWithTimeDelayError;

            public org.junit.runners.model.Statement delaySubscriptionDisposeBeforeTime;

            public org.junit.runners.model.Statement onErrorCalledOnScheduler;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement onCompleteFinal;

            public org.junit.runners.model.Statement onErrorFinal;

            public org.junit.runners.model.Statement itemDelayReturnsNull;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.delay = _ClassStatement.forPayload(FlowableDelayTest::delay, "delay", this);
            this.payloads.longDelay = _ClassStatement.forPayload(FlowableDelayTest::longDelay, "longDelay", this);
            this.payloads.delayWithError = _ClassStatement.forPayload(FlowableDelayTest::delayWithError, "delayWithError", this);
            this.payloads.delayWithMultipleSubscriptions = _ClassStatement.forPayload(FlowableDelayTest::delayWithMultipleSubscriptions, "delayWithMultipleSubscriptions", this);
            this.payloads.delaySubscription = _ClassStatement.forPayload(FlowableDelayTest::delaySubscription, "delaySubscription", this);
            this.payloads.delaySubscriptionCancelBeforeTime = _ClassStatement.forPayload(FlowableDelayTest::delaySubscriptionCancelBeforeTime, "delaySubscriptionCancelBeforeTime", this);
            this.payloads.delayWithFlowableNormal1 = _ClassStatement.forPayload(FlowableDelayTest::delayWithFlowableNormal1, "delayWithFlowableNormal1", this);
            this.payloads.delayWithFlowableSingleSend1 = _ClassStatement.forPayload(FlowableDelayTest::delayWithFlowableSingleSend1, "delayWithFlowableSingleSend1", this);
            this.payloads.delayWithFlowableSourceThrows = _ClassStatement.forPayload(FlowableDelayTest::delayWithFlowableSourceThrows, "delayWithFlowableSourceThrows", this);
            this.payloads.delayWithFlowableDelayFunctionThrows = _ClassStatement.forPayload(FlowableDelayTest::delayWithFlowableDelayFunctionThrows, "delayWithFlowableDelayFunctionThrows", this);
            this.payloads.delayWithFlowableDelayThrows = _ClassStatement.forPayload(FlowableDelayTest::delayWithFlowableDelayThrows, "delayWithFlowableDelayThrows", this);
            this.payloads.delayWithFlowableSubscriptionNormal = _ClassStatement.forPayload(FlowableDelayTest::delayWithFlowableSubscriptionNormal, "delayWithFlowableSubscriptionNormal", this);
            this.payloads.delayWithFlowableSubscriptionFunctionThrows = _ClassStatement.forPayload(FlowableDelayTest::delayWithFlowableSubscriptionFunctionThrows, "delayWithFlowableSubscriptionFunctionThrows", this);
            this.payloads.delayWithFlowableSubscriptionThrows = _ClassStatement.forPayload(FlowableDelayTest::delayWithFlowableSubscriptionThrows, "delayWithFlowableSubscriptionThrows", this);
            this.payloads.delayWithFlowableEmptyDelayer = _ClassStatement.forPayload(FlowableDelayTest::delayWithFlowableEmptyDelayer, "delayWithFlowableEmptyDelayer", this);
            this.payloads.delayWithFlowableSubscriptionRunCompletion = _ClassStatement.forPayload(FlowableDelayTest::delayWithFlowableSubscriptionRunCompletion, "delayWithFlowableSubscriptionRunCompletion", this);
            this.payloads.delayWithFlowableAsTimed = _ClassStatement.forPayload(FlowableDelayTest::delayWithFlowableAsTimed, "delayWithFlowableAsTimed", this);
            this.payloads.delayWithFlowableReorder = _ClassStatement.forPayload(FlowableDelayTest::delayWithFlowableReorder, "delayWithFlowableReorder", this);
            this.payloads.delayEmitsEverything = _ClassStatement.forPayload(FlowableDelayTest::delayEmitsEverything, "delayEmitsEverything", this);
            this.payloads.backpressureWithTimedDelay = _ClassStatement.forPayload(FlowableDelayTest::backpressureWithTimedDelay, "backpressureWithTimedDelay", this);
            this.payloads.backpressureWithSubscriptionTimedDelay = _ClassStatement.forPayload(FlowableDelayTest::backpressureWithSubscriptionTimedDelay, "backpressureWithSubscriptionTimedDelay", this);
            this.payloads.backpressureWithSelectorDelay = _ClassStatement.forPayload(FlowableDelayTest::backpressureWithSelectorDelay, "backpressureWithSelectorDelay", this);
            this.payloads.backpressureWithSelectorDelayAndSubscriptionDelay = _ClassStatement.forPayload(FlowableDelayTest::backpressureWithSelectorDelayAndSubscriptionDelay, "backpressureWithSelectorDelayAndSubscriptionDelay", this);
            this.payloads.errorRunsBeforeOnNext = _ClassStatement.forPayload(FlowableDelayTest::errorRunsBeforeOnNext, "errorRunsBeforeOnNext", this);
            this.payloads.delaySupplierSimple = _ClassStatement.forPayload(FlowableDelayTest::delaySupplierSimple, "delaySupplierSimple", this);
            this.payloads.delaySupplierCompletes = _ClassStatement.forPayload(FlowableDelayTest::delaySupplierCompletes, "delaySupplierCompletes", this);
            this.payloads.delaySupplierErrors = _ClassStatement.forPayload(FlowableDelayTest::delaySupplierErrors, "delaySupplierErrors", this);
            this.payloads.delayAndTakeUntilNeverSubscribeToSource = _ClassStatement.forPayload(FlowableDelayTest::delayAndTakeUntilNeverSubscribeToSource, "delayAndTakeUntilNeverSubscribeToSource", this);
            this.payloads.delayWithTimeDelayError = _ClassStatement.forPayload(FlowableDelayTest::delayWithTimeDelayError, "delayWithTimeDelayError", this);
            this.payloads.delaySubscriptionDisposeBeforeTime = _ClassStatement.forPayload(FlowableDelayTest::delaySubscriptionDisposeBeforeTime, "delaySubscriptionDisposeBeforeTime", this);
            this.payloads.onErrorCalledOnScheduler = _ClassStatement.forPayload(FlowableDelayTest::onErrorCalledOnScheduler, "onErrorCalledOnScheduler", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableDelayTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableDelayTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.onCompleteFinal = _ClassStatement.forPayload(FlowableDelayTest::onCompleteFinal, "onCompleteFinal", this);
            this.payloads.onErrorFinal = _ClassStatement.forPayload(FlowableDelayTest::onErrorFinal, "onErrorFinal", this);
            this.payloads.itemDelayReturnsNull = _ClassStatement.forPayload(FlowableDelayTest::itemDelayReturnsNull, "itemDelayReturnsNull", this);
        }
    }
}
