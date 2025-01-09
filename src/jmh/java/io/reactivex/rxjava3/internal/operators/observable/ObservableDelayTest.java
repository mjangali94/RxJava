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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.*;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.*;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableDelayTest extends RxJavaTest {

    private Observer<Long> observer;

    private Observer<Long> observer2;

    private TestScheduler scheduler;

    @Before
    public void before() {
        observer = TestHelper.mockObserver();
        observer2 = TestHelper.mockObserver();
        scheduler = new TestScheduler();
    }

    @Test
    public void delay() {
        Observable<Long> source = Observable.interval(1L, TimeUnit.SECONDS, scheduler).take(3);
        Observable<Long> delayed = source.delay(500L, TimeUnit.MILLISECONDS, scheduler);
        delayed.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        scheduler.advanceTimeTo(1499L, TimeUnit.MILLISECONDS);
        verify(observer, never()).onNext(anyLong());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(1500L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(0L);
        inOrder.verify(observer, never()).onNext(anyLong());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(2400L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(anyLong());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(2500L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(1L);
        inOrder.verify(observer, never()).onNext(anyLong());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(3400L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(anyLong());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(3500L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(2L);
        verify(observer, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void longDelay() {
        Observable<Long> source = Observable.interval(1L, TimeUnit.SECONDS, scheduler).take(3);
        Observable<Long> delayed = source.delay(5L, TimeUnit.SECONDS, scheduler);
        delayed.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        scheduler.advanceTimeTo(5999L, TimeUnit.MILLISECONDS);
        verify(observer, never()).onNext(anyLong());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(6000L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(0L);
        scheduler.advanceTimeTo(6999L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(anyLong());
        scheduler.advanceTimeTo(7000L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(1L);
        scheduler.advanceTimeTo(7999L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(anyLong());
        scheduler.advanceTimeTo(8000L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(2L);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verify(observer, never()).onNext(anyLong());
        inOrder.verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void delayWithError() {
        Observable<Long> source = Observable.interval(1L, TimeUnit.SECONDS, scheduler).map(new Function<Long, Long>() {

            @Override
            public Long apply(Long value) {
                if (value == 1L) {
                    throw new RuntimeException("error!");
                }
                return value;
            }
        });
        Observable<Long> delayed = source.delay(1L, TimeUnit.SECONDS, scheduler);
        delayed.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        scheduler.advanceTimeTo(1999L, TimeUnit.MILLISECONDS);
        verify(observer, never()).onNext(anyLong());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(2000L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onNext(anyLong());
        verify(observer, never()).onComplete();
        scheduler.advanceTimeTo(5000L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(anyLong());
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
    }

    @Test
    public void delayWithMultipleSubscriptions() {
        Observable<Long> source = Observable.interval(1L, TimeUnit.SECONDS, scheduler).take(3);
        Observable<Long> delayed = source.delay(500L, TimeUnit.MILLISECONDS, scheduler);
        delayed.subscribe(observer);
        delayed.subscribe(observer2);
        InOrder inOrder = inOrder(observer);
        InOrder inOrder2 = inOrder(observer2);
        scheduler.advanceTimeTo(1499L, TimeUnit.MILLISECONDS);
        verify(observer, never()).onNext(anyLong());
        verify(observer2, never()).onNext(anyLong());
        scheduler.advanceTimeTo(1500L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(0L);
        inOrder2.verify(observer2, times(1)).onNext(0L);
        scheduler.advanceTimeTo(2499L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(anyLong());
        inOrder2.verify(observer2, never()).onNext(anyLong());
        scheduler.advanceTimeTo(2500L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(1L);
        inOrder2.verify(observer2, times(1)).onNext(1L);
        verify(observer, never()).onComplete();
        verify(observer2, never()).onComplete();
        scheduler.advanceTimeTo(3500L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(2L);
        inOrder2.verify(observer2, times(1)).onNext(2L);
        inOrder.verify(observer, never()).onNext(anyLong());
        inOrder2.verify(observer2, never()).onNext(anyLong());
        inOrder.verify(observer, times(1)).onComplete();
        inOrder2.verify(observer2, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer2, never()).onError(any(Throwable.class));
    }

    @Test
    public void delaySubscription() {
        Observable<Integer> result = Observable.just(1, 2, 3).delaySubscription(100, TimeUnit.MILLISECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        result.subscribe(o);
        inOrder.verify(o, never()).onNext(any());
        inOrder.verify(o, never()).onComplete();
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        inOrder.verify(o, times(1)).onNext(1);
        inOrder.verify(o, times(1)).onNext(2);
        inOrder.verify(o, times(1)).onNext(3);
        inOrder.verify(o, times(1)).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void delaySubscriptionDisposeBeforeTime() {
        Observable<Integer> result = Observable.just(1, 2, 3).delaySubscription(100, TimeUnit.MILLISECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        TestObserver<Object> to = new TestObserver<>(o);
        result.subscribe(to);
        to.dispose();
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        verify(o, never()).onNext(any());
        verify(o, never()).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void delayWithObservableNormal1() {
        PublishSubject<Integer> source = PublishSubject.create();
        final List<PublishSubject<Integer>> delays = new ArrayList<>();
        final int n = 10;
        for (int i = 0; i < n; i++) {
            PublishSubject<Integer> delay = PublishSubject.create();
            delays.add(delay);
        }
        Function<Integer, Observable<Integer>> delayFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return delays.get(t1);
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.delay(delayFunc).subscribe(o);
        for (int i = 0; i < n; i++) {
            source.onNext(i);
            delays.get(i).onNext(i);
            inOrder.verify(o).onNext(i);
        }
        source.onComplete();
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void delayWithObservableSingleSend1() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> delay = PublishSubject.create();
        Function<Integer, Observable<Integer>> delayFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return delay;
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.delay(delayFunc).subscribe(o);
        source.onNext(1);
        delay.onNext(1);
        delay.onNext(2);
        inOrder.verify(o).onNext(1);
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void delayWithObservableSourceThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> delay = PublishSubject.create();
        Function<Integer, Observable<Integer>> delayFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return delay;
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.delay(delayFunc).subscribe(o);
        source.onNext(1);
        source.onError(new TestException());
        delay.onNext(1);
        inOrder.verify(o).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onNext(any());
        verify(o, never()).onComplete();
    }

    @Test
    public void delayWithObservableDelayFunctionThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        Function<Integer, Observable<Integer>> delayFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                throw new TestException();
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.delay(delayFunc).subscribe(o);
        source.onNext(1);
        inOrder.verify(o).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onNext(any());
        verify(o, never()).onComplete();
    }

    @Test
    public void delayWithObservableDelayThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> delay = PublishSubject.create();
        Function<Integer, Observable<Integer>> delayFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return delay;
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.delay(delayFunc).subscribe(o);
        source.onNext(1);
        delay.onError(new TestException());
        inOrder.verify(o).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onNext(any());
        verify(o, never()).onComplete();
    }

    @Test
    public void delayWithObservableSubscriptionNormal() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> delay = PublishSubject.create();
        Supplier<Observable<Integer>> subFunc = new Supplier<Observable<Integer>>() {

            @Override
            public Observable<Integer> get() {
                return delay;
            }
        };
        Function<Integer, Observable<Integer>> delayFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return delay;
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.delay(Observable.defer(subFunc), delayFunc).subscribe(o);
        source.onNext(1);
        delay.onNext(1);
        source.onNext(2);
        delay.onNext(2);
        inOrder.verify(o).onNext(2);
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onError(any(Throwable.class));
        verify(o, never()).onComplete();
    }

    @Test
    public void delayWithObservableSubscriptionFunctionThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> delay = PublishSubject.create();
        Supplier<Observable<Integer>> subFunc = new Supplier<Observable<Integer>>() {

            @Override
            public Observable<Integer> get() {
                throw new TestException();
            }
        };
        Function<Integer, Observable<Integer>> delayFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return delay;
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.delay(Observable.defer(subFunc), delayFunc).subscribe(o);
        source.onNext(1);
        delay.onNext(1);
        source.onNext(2);
        inOrder.verify(o).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onNext(any());
        verify(o, never()).onComplete();
    }

    @Test
    public void delayWithObservableSubscriptionThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> delay = PublishSubject.create();
        Supplier<Observable<Integer>> subFunc = new Supplier<Observable<Integer>>() {

            @Override
            public Observable<Integer> get() {
                return delay;
            }
        };
        Function<Integer, Observable<Integer>> delayFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return delay;
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.delay(Observable.defer(subFunc), delayFunc).subscribe(o);
        source.onNext(1);
        delay.onError(new TestException());
        source.onNext(2);
        inOrder.verify(o).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onNext(any());
        verify(o, never()).onComplete();
    }

    @Test
    public void delayWithObservableEmptyDelayer() {
        PublishSubject<Integer> source = PublishSubject.create();
        Function<Integer, Observable<Integer>> delayFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return Observable.empty();
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.delay(delayFunc).subscribe(o);
        source.onNext(1);
        source.onComplete();
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void delayWithObservableSubscriptionRunCompletion() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> sdelay = PublishSubject.create();
        final PublishSubject<Integer> delay = PublishSubject.create();
        Supplier<Observable<Integer>> subFunc = new Supplier<Observable<Integer>>() {

            @Override
            public Observable<Integer> get() {
                return sdelay;
            }
        };
        Function<Integer, Observable<Integer>> delayFunc = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return delay;
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.delay(Observable.defer(subFunc), delayFunc).subscribe(o);
        source.onNext(1);
        sdelay.onComplete();
        source.onNext(2);
        delay.onNext(2);
        inOrder.verify(o).onNext(2);
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onError(any(Throwable.class));
        verify(o, never()).onComplete();
    }

    @Test
    public void delayWithObservableAsTimed() {
        Observable<Long> source = Observable.interval(1L, TimeUnit.SECONDS, scheduler).take(3);
        final Observable<Long> delayer = Observable.timer(500L, TimeUnit.MILLISECONDS, scheduler);
        Function<Long, Observable<Long>> delayFunc = new Function<Long, Observable<Long>>() {

            @Override
            public Observable<Long> apply(Long t1) {
                return delayer;
            }
        };
        Observable<Long> delayed = source.delay(delayFunc);
        delayed.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        scheduler.advanceTimeTo(1499L, TimeUnit.MILLISECONDS);
        verify(observer, never()).onNext(anyLong());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(1500L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(0L);
        inOrder.verify(observer, never()).onNext(anyLong());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(2400L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(anyLong());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(2500L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(1L);
        inOrder.verify(observer, never()).onNext(anyLong());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(3400L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(anyLong());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(3500L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(2L);
        verify(observer, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void delayWithObservableReorder() {
        int n = 3;
        PublishSubject<Integer> source = PublishSubject.create();
        final List<PublishSubject<Integer>> subjects = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            subjects.add(PublishSubject.<Integer>create());
        }
        Observable<Integer> result = source.delay(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return subjects.get(t1);
            }
        });
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        result.subscribe(o);
        for (int i = 0; i < n; i++) {
            source.onNext(i);
        }
        source.onComplete();
        inOrder.verify(o, never()).onNext(anyInt());
        inOrder.verify(o, never()).onComplete();
        for (int i = n - 1; i >= 0; i--) {
            subjects.get(i).onComplete();
            inOrder.verify(o).onNext(i);
        }
        inOrder.verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void delayEmitsEverything() {
        Observable<Integer> source = Observable.range(1, 5);
        Observable<Integer> delayed = source.delay(500L, TimeUnit.MILLISECONDS, scheduler);
        delayed = delayed.doOnEach(new Consumer<Notification<Integer>>() {

            @Override
            public void accept(Notification<Integer> t1) {
                // System.out.println(t1);
            }
        });
        TestObserver<Integer> observer = new TestObserver<>();
        delayed.subscribe(observer);
        // all will be delivered after 500ms since range does not delay between them
        scheduler.advanceTimeBy(500L, TimeUnit.MILLISECONDS);
        observer.assertValues(1, 2, 3, 4, 5);
    }

    @Test
    public void backpressureWithTimedDelay() {
        TestObserver<Integer> to = new TestObserver<>();
        Observable.range(1, Flowable.bufferSize() * 2).delay(100, TimeUnit.MILLISECONDS).observeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

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
        }).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors();
        assertEquals(Flowable.bufferSize() * 2, to.values().size());
    }

    @Test
    public void backpressureWithSubscriptionTimedDelay() {
        TestObserver<Integer> to = new TestObserver<>();
        Observable.range(1, Flowable.bufferSize() * 2).delaySubscription(100, TimeUnit.MILLISECONDS).delay(100, TimeUnit.MILLISECONDS).observeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

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
        }).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors();
        assertEquals(Flowable.bufferSize() * 2, to.values().size());
    }

    @Test
    public void backpressureWithSelectorDelay() {
        TestObserver<Integer> to = new TestObserver<>();
        Observable.range(1, Flowable.bufferSize() * 2).delay(new Function<Integer, Observable<Long>>() {

            @Override
            public Observable<Long> apply(Integer i) {
                return Observable.timer(100, TimeUnit.MILLISECONDS);
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
        }).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors();
        assertEquals(Flowable.bufferSize() * 2, to.values().size());
    }

    @Test
    public void backpressureWithSelectorDelayAndSubscriptionDelay() {
        TestObserver<Integer> to = new TestObserver<>();
        Observable.range(1, Flowable.bufferSize() * 2).delay(Observable.timer(500, TimeUnit.MILLISECONDS), new Function<Integer, Observable<Long>>() {

            @Override
            public Observable<Long> apply(Integer i) {
                return Observable.timer(100, TimeUnit.MILLISECONDS);
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
        }).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors();
        assertEquals(Flowable.bufferSize() * 2, to.values().size());
    }

    @Test
    public void errorRunsBeforeOnNext() {
        TestScheduler test = new TestScheduler();
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<>();
        ps.delay(1, TimeUnit.SECONDS, test).subscribe(to);
        ps.onNext(1);
        test.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        ps.onError(new TestException());
        test.advanceTimeBy(1, TimeUnit.SECONDS);
        to.assertNoValues();
        to.assertError(TestException.class);
        to.assertNotComplete();
    }

    @Test
    public void delaySupplierSimple() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        Observable<Integer> source = Observable.range(1, 5);
        TestObserver<Integer> to = new TestObserver<>();
        source.delaySubscription(ps).subscribe(to);
        to.assertNoValues();
        to.assertNoErrors();
        to.assertNotComplete();
        ps.onNext(1);
        to.assertValues(1, 2, 3, 4, 5);
        to.assertComplete();
        to.assertNoErrors();
    }

    @Test
    public void delaySupplierCompletes() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        Observable<Integer> source = Observable.range(1, 5);
        TestObserver<Integer> to = new TestObserver<>();
        source.delaySubscription(ps).subscribe(to);
        to.assertNoValues();
        to.assertNoErrors();
        to.assertNotComplete();
        // FIXME should this complete the source instead of consuming it?
        ps.onComplete();
        to.assertValues(1, 2, 3, 4, 5);
        to.assertComplete();
        to.assertNoErrors();
    }

    @Test
    public void delaySupplierErrors() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        Observable<Integer> source = Observable.range(1, 5);
        TestObserver<Integer> to = new TestObserver<>();
        source.delaySubscription(ps).subscribe(to);
        to.assertNoValues();
        to.assertNoErrors();
        to.assertNotComplete();
        ps.onError(new TestException());
        to.assertNoValues();
        to.assertNotComplete();
        to.assertError(TestException.class);
    }

    @Test
    public void delayWithTimeDelayError() throws Exception {
        Observable.just(1).concatWith(Observable.<Integer>error(new TestException())).delay(100, TimeUnit.MILLISECONDS, true).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class, 1);
    }

    @Test
    public void onErrorCalledOnScheduler() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Thread> thread = new AtomicReference<>();
        Observable.<String>error(new Exception()).delay(0, TimeUnit.MILLISECONDS, Schedulers.newThread()).doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable throwable) throws Exception {
                thread.set(Thread.currentThread());
                latch.countDown();
            }
        }).onErrorResumeWith(Observable.<String>empty()).subscribe();
        latch.await();
        assertNotEquals(Thread.currentThread(), thread.get());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().delay(1, TimeUnit.SECONDS));
        TestHelper.checkDisposed(PublishSubject.create().delay(Functions.justFunction(Observable.never())));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.delay(1, TimeUnit.SECONDS);
            }
        });
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.delay(Functions.justFunction(Observable.never()));
            }
        });
    }

    @Test
    public void onCompleteFinal() {
        TestScheduler scheduler = new TestScheduler();
        Observable.empty().delay(1, TimeUnit.MILLISECONDS, scheduler).subscribe(new DisposableObserver<Object>() {

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
        Observable.error(new TestException()).delay(1, TimeUnit.MILLISECONDS, scheduler).subscribe(new DisposableObserver<Object>() {

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
        Observable.just(1).delay(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer t) throws Exception {
                return null;
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(NullPointerException.class, "The itemDelay returned a null ObservableSource");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableDelayTest instance;

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
        public void benchmark_delaySubscriptionDisposeBeforeTime() throws java.lang.Throwable {
            this.payloads.delaySubscriptionDisposeBeforeTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithObservableNormal1() throws java.lang.Throwable {
            this.payloads.delayWithObservableNormal1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithObservableSingleSend1() throws java.lang.Throwable {
            this.payloads.delayWithObservableSingleSend1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithObservableSourceThrows() throws java.lang.Throwable {
            this.payloads.delayWithObservableSourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithObservableDelayFunctionThrows() throws java.lang.Throwable {
            this.payloads.delayWithObservableDelayFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithObservableDelayThrows() throws java.lang.Throwable {
            this.payloads.delayWithObservableDelayThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithObservableSubscriptionNormal() throws java.lang.Throwable {
            this.payloads.delayWithObservableSubscriptionNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithObservableSubscriptionFunctionThrows() throws java.lang.Throwable {
            this.payloads.delayWithObservableSubscriptionFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithObservableSubscriptionThrows() throws java.lang.Throwable {
            this.payloads.delayWithObservableSubscriptionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithObservableEmptyDelayer() throws java.lang.Throwable {
            this.payloads.delayWithObservableEmptyDelayer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithObservableSubscriptionRunCompletion() throws java.lang.Throwable {
            this.payloads.delayWithObservableSubscriptionRunCompletion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithObservableAsTimed() throws java.lang.Throwable {
            this.payloads.delayWithObservableAsTimed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithObservableReorder() throws java.lang.Throwable {
            this.payloads.delayWithObservableReorder.evaluate();
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
        public void benchmark_delayWithTimeDelayError() throws java.lang.Throwable {
            this.payloads.delayWithTimeDelayError.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDelayTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDelayTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDelayTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDelayTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableDelayTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDelayTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableDelayTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableDelayTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement delay;

            public org.junit.runners.model.Statement longDelay;

            public org.junit.runners.model.Statement delayWithError;

            public org.junit.runners.model.Statement delayWithMultipleSubscriptions;

            public org.junit.runners.model.Statement delaySubscription;

            public org.junit.runners.model.Statement delaySubscriptionDisposeBeforeTime;

            public org.junit.runners.model.Statement delayWithObservableNormal1;

            public org.junit.runners.model.Statement delayWithObservableSingleSend1;

            public org.junit.runners.model.Statement delayWithObservableSourceThrows;

            public org.junit.runners.model.Statement delayWithObservableDelayFunctionThrows;

            public org.junit.runners.model.Statement delayWithObservableDelayThrows;

            public org.junit.runners.model.Statement delayWithObservableSubscriptionNormal;

            public org.junit.runners.model.Statement delayWithObservableSubscriptionFunctionThrows;

            public org.junit.runners.model.Statement delayWithObservableSubscriptionThrows;

            public org.junit.runners.model.Statement delayWithObservableEmptyDelayer;

            public org.junit.runners.model.Statement delayWithObservableSubscriptionRunCompletion;

            public org.junit.runners.model.Statement delayWithObservableAsTimed;

            public org.junit.runners.model.Statement delayWithObservableReorder;

            public org.junit.runners.model.Statement delayEmitsEverything;

            public org.junit.runners.model.Statement backpressureWithTimedDelay;

            public org.junit.runners.model.Statement backpressureWithSubscriptionTimedDelay;

            public org.junit.runners.model.Statement backpressureWithSelectorDelay;

            public org.junit.runners.model.Statement backpressureWithSelectorDelayAndSubscriptionDelay;

            public org.junit.runners.model.Statement errorRunsBeforeOnNext;

            public org.junit.runners.model.Statement delaySupplierSimple;

            public org.junit.runners.model.Statement delaySupplierCompletes;

            public org.junit.runners.model.Statement delaySupplierErrors;

            public org.junit.runners.model.Statement delayWithTimeDelayError;

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
            this.payloads.delay = _ClassStatement.forPayload(ObservableDelayTest::delay, "delay", this);
            this.payloads.longDelay = _ClassStatement.forPayload(ObservableDelayTest::longDelay, "longDelay", this);
            this.payloads.delayWithError = _ClassStatement.forPayload(ObservableDelayTest::delayWithError, "delayWithError", this);
            this.payloads.delayWithMultipleSubscriptions = _ClassStatement.forPayload(ObservableDelayTest::delayWithMultipleSubscriptions, "delayWithMultipleSubscriptions", this);
            this.payloads.delaySubscription = _ClassStatement.forPayload(ObservableDelayTest::delaySubscription, "delaySubscription", this);
            this.payloads.delaySubscriptionDisposeBeforeTime = _ClassStatement.forPayload(ObservableDelayTest::delaySubscriptionDisposeBeforeTime, "delaySubscriptionDisposeBeforeTime", this);
            this.payloads.delayWithObservableNormal1 = _ClassStatement.forPayload(ObservableDelayTest::delayWithObservableNormal1, "delayWithObservableNormal1", this);
            this.payloads.delayWithObservableSingleSend1 = _ClassStatement.forPayload(ObservableDelayTest::delayWithObservableSingleSend1, "delayWithObservableSingleSend1", this);
            this.payloads.delayWithObservableSourceThrows = _ClassStatement.forPayload(ObservableDelayTest::delayWithObservableSourceThrows, "delayWithObservableSourceThrows", this);
            this.payloads.delayWithObservableDelayFunctionThrows = _ClassStatement.forPayload(ObservableDelayTest::delayWithObservableDelayFunctionThrows, "delayWithObservableDelayFunctionThrows", this);
            this.payloads.delayWithObservableDelayThrows = _ClassStatement.forPayload(ObservableDelayTest::delayWithObservableDelayThrows, "delayWithObservableDelayThrows", this);
            this.payloads.delayWithObservableSubscriptionNormal = _ClassStatement.forPayload(ObservableDelayTest::delayWithObservableSubscriptionNormal, "delayWithObservableSubscriptionNormal", this);
            this.payloads.delayWithObservableSubscriptionFunctionThrows = _ClassStatement.forPayload(ObservableDelayTest::delayWithObservableSubscriptionFunctionThrows, "delayWithObservableSubscriptionFunctionThrows", this);
            this.payloads.delayWithObservableSubscriptionThrows = _ClassStatement.forPayload(ObservableDelayTest::delayWithObservableSubscriptionThrows, "delayWithObservableSubscriptionThrows", this);
            this.payloads.delayWithObservableEmptyDelayer = _ClassStatement.forPayload(ObservableDelayTest::delayWithObservableEmptyDelayer, "delayWithObservableEmptyDelayer", this);
            this.payloads.delayWithObservableSubscriptionRunCompletion = _ClassStatement.forPayload(ObservableDelayTest::delayWithObservableSubscriptionRunCompletion, "delayWithObservableSubscriptionRunCompletion", this);
            this.payloads.delayWithObservableAsTimed = _ClassStatement.forPayload(ObservableDelayTest::delayWithObservableAsTimed, "delayWithObservableAsTimed", this);
            this.payloads.delayWithObservableReorder = _ClassStatement.forPayload(ObservableDelayTest::delayWithObservableReorder, "delayWithObservableReorder", this);
            this.payloads.delayEmitsEverything = _ClassStatement.forPayload(ObservableDelayTest::delayEmitsEverything, "delayEmitsEverything", this);
            this.payloads.backpressureWithTimedDelay = _ClassStatement.forPayload(ObservableDelayTest::backpressureWithTimedDelay, "backpressureWithTimedDelay", this);
            this.payloads.backpressureWithSubscriptionTimedDelay = _ClassStatement.forPayload(ObservableDelayTest::backpressureWithSubscriptionTimedDelay, "backpressureWithSubscriptionTimedDelay", this);
            this.payloads.backpressureWithSelectorDelay = _ClassStatement.forPayload(ObservableDelayTest::backpressureWithSelectorDelay, "backpressureWithSelectorDelay", this);
            this.payloads.backpressureWithSelectorDelayAndSubscriptionDelay = _ClassStatement.forPayload(ObservableDelayTest::backpressureWithSelectorDelayAndSubscriptionDelay, "backpressureWithSelectorDelayAndSubscriptionDelay", this);
            this.payloads.errorRunsBeforeOnNext = _ClassStatement.forPayload(ObservableDelayTest::errorRunsBeforeOnNext, "errorRunsBeforeOnNext", this);
            this.payloads.delaySupplierSimple = _ClassStatement.forPayload(ObservableDelayTest::delaySupplierSimple, "delaySupplierSimple", this);
            this.payloads.delaySupplierCompletes = _ClassStatement.forPayload(ObservableDelayTest::delaySupplierCompletes, "delaySupplierCompletes", this);
            this.payloads.delaySupplierErrors = _ClassStatement.forPayload(ObservableDelayTest::delaySupplierErrors, "delaySupplierErrors", this);
            this.payloads.delayWithTimeDelayError = _ClassStatement.forPayload(ObservableDelayTest::delayWithTimeDelayError, "delayWithTimeDelayError", this);
            this.payloads.onErrorCalledOnScheduler = _ClassStatement.forPayload(ObservableDelayTest::onErrorCalledOnScheduler, "onErrorCalledOnScheduler", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableDelayTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableDelayTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.onCompleteFinal = _ClassStatement.forPayload(ObservableDelayTest::onCompleteFinal, "onCompleteFinal", this);
            this.payloads.onErrorFinal = _ClassStatement.forPayload(ObservableDelayTest::onErrorFinal, "onErrorFinal", this);
            this.payloads.itemDelayReturnsNull = _ClassStatement.forPayload(ObservableDelayTest::itemDelayReturnsNull, "itemDelayReturnsNull", this);
        }
    }
}
