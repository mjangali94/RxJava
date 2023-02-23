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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.mockito.InOrder;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.schedulers.ImmediateThinScheduler;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.observers.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableSwitchTest extends RxJavaTest {

    private TestScheduler scheduler;

    private Scheduler.Worker innerScheduler;

    private Observer<String> observer;

    @Before
    public void before() {
        scheduler = new TestScheduler();
        innerScheduler = scheduler.createWorker();
        observer = TestHelper.mockObserver();
    }

    @Test
    public void switchWhenOuterCompleteBeforeInner() {
        Observable<Observable<String>> source = Observable.unsafeCreate(new ObservableSource<Observable<String>>() {

            @Override
            public void subscribe(Observer<? super Observable<String>> outerObserver) {
                outerObserver.onSubscribe(Disposable.empty());
                publishNext(outerObserver, 50, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(Observer<? super String> innerObserver) {
                        innerObserver.onSubscribe(Disposable.empty());
                        publishNext(innerObserver, 70, "one");
                        publishNext(innerObserver, 100, "two");
                        publishCompleted(innerObserver, 200);
                    }
                }));
                publishCompleted(outerObserver, 60);
            }
        });
        Observable<String> sampled = Observable.switchOnNext(source);
        sampled.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        scheduler.advanceTimeTo(350, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(2)).onNext(anyString());
        inOrder.verify(observer, times(1)).onComplete();
    }

    @Test
    public void switchWhenInnerCompleteBeforeOuter() {
        Observable<Observable<String>> source = Observable.unsafeCreate(new ObservableSource<Observable<String>>() {

            @Override
            public void subscribe(Observer<? super Observable<String>> outerObserver) {
                outerObserver.onSubscribe(Disposable.empty());
                publishNext(outerObserver, 10, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(Observer<? super String> innerObserver) {
                        innerObserver.onSubscribe(Disposable.empty());
                        publishNext(innerObserver, 0, "one");
                        publishNext(innerObserver, 10, "two");
                        publishCompleted(innerObserver, 20);
                    }
                }));
                publishNext(outerObserver, 100, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(Observer<? super String> innerObserver) {
                        innerObserver.onSubscribe(Disposable.empty());
                        publishNext(innerObserver, 0, "three");
                        publishNext(innerObserver, 10, "four");
                        publishCompleted(innerObserver, 20);
                    }
                }));
                publishCompleted(outerObserver, 200);
            }
        });
        Observable<String> sampled = Observable.switchOnNext(source);
        sampled.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        scheduler.advanceTimeTo(150, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onComplete();
        inOrder.verify(observer, times(1)).onNext("one");
        inOrder.verify(observer, times(1)).onNext("two");
        inOrder.verify(observer, times(1)).onNext("three");
        inOrder.verify(observer, times(1)).onNext("four");
        scheduler.advanceTimeTo(250, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(anyString());
        inOrder.verify(observer, times(1)).onComplete();
    }

    @Test
    public void switchWithComplete() {
        Observable<Observable<String>> source = Observable.unsafeCreate(new ObservableSource<Observable<String>>() {

            @Override
            public void subscribe(Observer<? super Observable<String>> outerObserver) {
                outerObserver.onSubscribe(Disposable.empty());
                publishNext(outerObserver, 50, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(final Observer<? super String> innerObserver) {
                        innerObserver.onSubscribe(Disposable.empty());
                        publishNext(innerObserver, 60, "one");
                        publishNext(innerObserver, 100, "two");
                    }
                }));
                publishNext(outerObserver, 200, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(final Observer<? super String> innerObserver) {
                        innerObserver.onSubscribe(Disposable.empty());
                        publishNext(innerObserver, 0, "three");
                        publishNext(innerObserver, 100, "four");
                    }
                }));
                publishCompleted(outerObserver, 250);
            }
        });
        Observable<String> sampled = Observable.switchOnNext(source);
        sampled.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        scheduler.advanceTimeTo(90, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(anyString());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(125, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext("one");
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(175, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext("two");
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(225, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext("three");
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(350, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext("four");
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void switchWithError() {
        Observable<Observable<String>> source = Observable.unsafeCreate(new ObservableSource<Observable<String>>() {

            @Override
            public void subscribe(Observer<? super Observable<String>> outerObserver) {
                outerObserver.onSubscribe(Disposable.empty());
                publishNext(outerObserver, 50, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(final Observer<? super String> innerObserver) {
                        innerObserver.onSubscribe(Disposable.empty());
                        publishNext(innerObserver, 50, "one");
                        publishNext(innerObserver, 100, "two");
                    }
                }));
                publishNext(outerObserver, 200, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(Observer<? super String> innerObserver) {
                        innerObserver.onSubscribe(Disposable.empty());
                        publishNext(innerObserver, 0, "three");
                        publishNext(innerObserver, 100, "four");
                    }
                }));
                publishError(outerObserver, 250, new TestException());
            }
        });
        Observable<String> sampled = Observable.switchOnNext(source);
        sampled.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        scheduler.advanceTimeTo(90, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(anyString());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(125, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext("one");
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(175, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext("two");
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(225, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext("three");
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(350, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(anyString());
        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void switchWithSubsequenceComplete() {
        Observable<Observable<String>> source = Observable.unsafeCreate(new ObservableSource<Observable<String>>() {

            @Override
            public void subscribe(Observer<? super Observable<String>> outerObserver) {
                outerObserver.onSubscribe(Disposable.empty());
                publishNext(outerObserver, 50, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(Observer<? super String> innerObserver) {
                        innerObserver.onSubscribe(Disposable.empty());
                        publishNext(innerObserver, 50, "one");
                        publishNext(innerObserver, 100, "two");
                    }
                }));
                publishNext(outerObserver, 130, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(Observer<? super String> innerObserver) {
                        innerObserver.onSubscribe(Disposable.empty());
                        publishCompleted(innerObserver, 0);
                    }
                }));
                publishNext(outerObserver, 150, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(Observer<? super String> innerObserver) {
                        innerObserver.onSubscribe(Disposable.empty());
                        publishNext(innerObserver, 50, "three");
                    }
                }));
            }
        });
        Observable<String> sampled = Observable.switchOnNext(source);
        sampled.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        scheduler.advanceTimeTo(90, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(anyString());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(125, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext("one");
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(250, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext("three");
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void switchWithSubsequenceError() {
        Observable<Observable<String>> source = Observable.unsafeCreate(new ObservableSource<Observable<String>>() {

            @Override
            public void subscribe(Observer<? super Observable<String>> observer) {
                observer.onSubscribe(Disposable.empty());
                publishNext(observer, 50, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(Observer<? super String> observer) {
                        observer.onSubscribe(Disposable.empty());
                        publishNext(observer, 50, "one");
                        publishNext(observer, 100, "two");
                    }
                }));
                publishNext(observer, 130, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(Observer<? super String> observer) {
                        observer.onSubscribe(Disposable.empty());
                        publishError(observer, 0, new TestException());
                    }
                }));
                publishNext(observer, 150, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(Observer<? super String> observer) {
                        observer.onSubscribe(Disposable.empty());
                        publishNext(observer, 50, "three");
                    }
                }));
            }
        });
        Observable<String> sampled = Observable.switchOnNext(source);
        sampled.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        scheduler.advanceTimeTo(90, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(anyString());
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(125, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext("one");
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(250, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext("three");
        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    private <T> void publishCompleted(final Observer<T> observer, long delay) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                observer.onComplete();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private <T> void publishError(final Observer<T> observer, long delay, final Throwable error) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                observer.onError(error);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private <T> void publishNext(final Observer<T> observer, long delay, final T value) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                observer.onNext(value);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    @Test
    public void switchIssue737() {
        // https://github.com/ReactiveX/RxJava/issues/737
        Observable<Observable<String>> source = Observable.unsafeCreate(new ObservableSource<Observable<String>>() {

            @Override
            public void subscribe(Observer<? super Observable<String>> outerObserver) {
                outerObserver.onSubscribe(Disposable.empty());
                publishNext(outerObserver, 0, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(Observer<? super String> innerObserver) {
                        innerObserver.onSubscribe(Disposable.empty());
                        publishNext(innerObserver, 10, "1-one");
                        publishNext(innerObserver, 20, "1-two");
                        // The following events will be ignored
                        publishNext(innerObserver, 30, "1-three");
                        publishCompleted(innerObserver, 40);
                    }
                }));
                publishNext(outerObserver, 25, Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(Observer<? super String> innerObserver) {
                        innerObserver.onSubscribe(Disposable.empty());
                        publishNext(innerObserver, 10, "2-one");
                        publishNext(innerObserver, 20, "2-two");
                        publishNext(innerObserver, 30, "2-three");
                        publishCompleted(innerObserver, 40);
                    }
                }));
                publishCompleted(outerObserver, 30);
            }
        });
        Observable<String> sampled = Observable.switchOnNext(source);
        sampled.subscribe(observer);
        scheduler.advanceTimeTo(1000, TimeUnit.MILLISECONDS);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext("1-one");
        inOrder.verify(observer, times(1)).onNext("1-two");
        inOrder.verify(observer, times(1)).onNext("2-one");
        inOrder.verify(observer, times(1)).onNext("2-two");
        inOrder.verify(observer, times(1)).onNext("2-three");
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void unsubscribe() {
        final AtomicBoolean isUnsubscribed = new AtomicBoolean();
        Observable.switchOnNext(Observable.unsafeCreate(new ObservableSource<Observable<Integer>>() {

            @Override
            public void subscribe(final Observer<? super Observable<Integer>> observer) {
                Disposable bs = Disposable.empty();
                observer.onSubscribe(bs);
                observer.onNext(Observable.just(1));
                isUnsubscribed.set(bs.isDisposed());
            }
        })).take(1).subscribe();
        assertTrue("Switch doesn't propagate 'unsubscribe'", isUnsubscribed.get());
    }

    /**
     * The upstream producer hijacked the switch producer stopping the requests aimed at the inner observables.
     */
    @Test
    public void issue2654() {
        Observable<String> oneItem = Observable.just("Hello").mergeWith(Observable.<String>never());
        Observable<String> src = oneItem.switchMap(new Function<String, Observable<String>>() {

            @Override
            public Observable<String> apply(final String s) {
                return Observable.just(s).mergeWith(Observable.interval(10, TimeUnit.MILLISECONDS).map(new Function<Long, String>() {

                    @Override
                    public String apply(Long i) {
                        return s + " " + i;
                    }
                })).take(250);
            }
        }).share();
        TestObserverEx<String> to = new TestObserverEx<String>() {

            @Override
            public void onNext(String t) {
                super.onNext(t);
                if (values().size() == 250) {
                    onComplete();
                    dispose();
                }
            }
        };
        src.subscribe(to);
        to.awaitDone(10, TimeUnit.SECONDS);
        // System.out.println("> testIssue2654: " + to.values().size());
        to.assertTerminated();
        to.assertNoErrors();
        Assert.assertEquals(250, to.values().size());
    }

    @Test
    public void delayErrors() {
        PublishSubject<ObservableSource<Integer>> source = PublishSubject.create();
        TestObserverEx<Integer> to = source.switchMapDelayError(Functions.<ObservableSource<Integer>>identity()).to(TestHelper.<Integer>testConsumer());
        to.assertNoValues().assertNoErrors().assertNotComplete();
        source.onNext(Observable.just(1));
        source.onNext(Observable.<Integer>error(new TestException("Forced failure 1")));
        source.onNext(Observable.just(2, 3, 4));
        source.onNext(Observable.<Integer>error(new TestException("Forced failure 2")));
        source.onNext(Observable.just(5));
        source.onError(new TestException("Forced failure 3"));
        to.assertValues(1, 2, 3, 4, 5).assertNotComplete().assertError(CompositeException.class);
        List<Throwable> errors = ExceptionHelper.flatten(to.errors().get(0));
        TestHelper.assertError(errors, 0, TestException.class, "Forced failure 1");
        TestHelper.assertError(errors, 1, TestException.class, "Forced failure 2");
        TestHelper.assertError(errors, 2, TestException.class, "Forced failure 3");
    }

    @Test
    public void switchOnNextDelayError() {
        PublishSubject<Observable<Integer>> ps = PublishSubject.create();
        TestObserver<Integer> to = Observable.switchOnNextDelayError(ps).test();
        ps.onNext(Observable.just(1));
        ps.onNext(Observable.range(2, 4));
        ps.onComplete();
        to.assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void switchOnNextDelayErrorWithError() {
        PublishSubject<Observable<Integer>> ps = PublishSubject.create();
        TestObserver<Integer> to = Observable.switchOnNextDelayError(ps).test();
        ps.onNext(Observable.just(1));
        ps.onNext(Observable.<Integer>error(new TestException()));
        ps.onNext(Observable.range(2, 4));
        ps.onComplete();
        to.assertFailure(TestException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void switchOnNextDelayErrorBufferSize() {
        PublishSubject<Observable<Integer>> ps = PublishSubject.create();
        TestObserver<Integer> to = Observable.switchOnNextDelayError(ps, 2).test();
        ps.onNext(Observable.just(1));
        ps.onNext(Observable.range(2, 4));
        ps.onComplete();
        to.assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void switchMapDelayErrorEmptySource() {
        assertSame(Observable.empty(), Observable.<Object>empty().switchMapDelayError(new Function<Object, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Object v) throws Exception {
                return Observable.just(1);
            }
        }, 16));
    }

    @Test
    public void switchMapDelayErrorJustSource() {
        Observable.just(0).switchMapDelayError(new Function<Object, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Object v) throws Exception {
                return Observable.just(1);
            }
        }, 16).test().assertResult(1);
    }

    @Test
    public void switchMapErrorEmptySource() {
        assertSame(Observable.empty(), Observable.<Object>empty().switchMap(new Function<Object, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Object v) throws Exception {
                return Observable.just(1);
            }
        }, 16));
    }

    @Test
    public void switchMapJustSource() {
        Observable.just(0).switchMap(new Function<Object, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Object v) throws Exception {
                return Observable.just(1);
            }
        }, 16).test().assertResult(1);
    }

    @Test
    public void switchMapInnerCancelled() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = Observable.just(1).switchMap(Functions.justFunction(ps)).test();
        assertTrue(ps.hasObservers());
        to.dispose();
        assertFalse(ps.hasObservers());
    }

    @Test
    public void switchMapSingleJustSource() {
        Observable.just(0).switchMapSingle(new Function<Object, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Object v) throws Exception {
                return Single.just(1);
            }
        }).test().assertResult(1);
    }

    @Test
    public void switchMapSingleMapperReturnsNull() {
        Observable.just(0).switchMapSingle(new Function<Object, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Object v) throws Exception {
                return null;
            }
        }).test().assertError(NullPointerException.class);
    }

    @Test
    public void switchMapSingleFunctionDoesntReturnSingle() {
        Observable.just(0).switchMapSingle(new Function<Object, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Object v) throws Exception {
                return new SingleSource<Integer>() {

                    @Override
                    public void subscribe(SingleObserver<? super Integer> observer) {
                        observer.onSubscribe(Disposable.empty());
                        observer.onSuccess(1);
                    }
                };
            }
        }).test().assertResult(1);
    }

    @Test
    public void switchMapSingleDelayErrorJustSource() {
        final AtomicBoolean completed = new AtomicBoolean();
        Observable.just(0, 1).switchMapSingleDelayError(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                if (v == 0) {
                    return Single.error(new RuntimeException());
                } else {
                    return Single.just(1).doOnSuccess(new Consumer<Integer>() {

                        @Override
                        public void accept(Integer n) throws Exception {
                            completed.set(true);
                        }
                    });
                }
            }
        }).test().assertValue(1).assertError(RuntimeException.class);
        assertTrue(completed.get());
    }

    @Test
    public void scalarMap() {
        Observable.switchOnNext(Observable.just(Observable.just(1))).test().assertResult(1);
    }

    @Test
    public void scalarMapDelayError() {
        Observable.switchOnNextDelayError(Observable.just(Observable.just(1))).test().assertResult(1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.switchOnNext(Observable.just(Observable.just(1)).hide()));
    }

    @Test
    public void nextSourceErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> ps1 = PublishSubject.create();
                final PublishSubject<Integer> ps2 = PublishSubject.create();
                ps1.switchMap(new Function<Integer, ObservableSource<Integer>>() {

                    @Override
                    public ObservableSource<Integer> apply(Integer v) throws Exception {
                        if (v == 1) {
                            return ps2;
                        }
                        return Observable.never();
                    }
                }).test();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps1.onNext(2);
                    }
                };
                final TestException ex = new TestException();
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ps2.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                for (Throwable e : errors) {
                    assertTrue(e.toString(), e instanceof TestException);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void outerInnerErrorRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> ps1 = PublishSubject.create();
                final PublishSubject<Integer> ps2 = PublishSubject.create();
                ps1.switchMap(new Function<Integer, ObservableSource<Integer>>() {

                    @Override
                    public ObservableSource<Integer> apply(Integer v) throws Exception {
                        if (v == 1) {
                            return ps2;
                        }
                        return Observable.never();
                    }
                }).test();
                ps1.onNext(1);
                final TestException ex1 = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps1.onError(ex1);
                    }
                };
                final TestException ex2 = new TestException();
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ps2.onError(ex2);
                    }
                };
                TestHelper.race(r1, r2);
                for (Throwable e : errors) {
                    assertTrue(e.getCause().toString(), e.getCause() instanceof TestException);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void nextCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps1 = PublishSubject.create();
            final TestObserver<Integer> to = ps1.switchMap(new Function<Integer, ObservableSource<Integer>>() {

                @Override
                public ObservableSource<Integer> apply(Integer v) throws Exception {
                    return Observable.never();
                }
            }).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps1.onNext(2);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void mapperThrows() {
        Observable.just(1).hide().switchMap(new Function<Integer, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void badMainSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onComplete();
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.switchMap(Functions.justFunction(Observable.never())).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void emptyInner() {
        Observable.range(1, 5).switchMap(Functions.justFunction(Observable.empty())).test().assertResult();
    }

    @Test
    public void justInner() {
        Observable.range(1, 5).switchMap(Functions.justFunction(Observable.just(1))).test().assertResult(1, 1, 1, 1, 1);
    }

    @Test
    public void badInnerSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.just(1).hide().switchMap(Functions.justFunction(new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onError(new TestException());
                    observer.onComplete();
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            })).test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void innerCompletesReentrant() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                ps.onComplete();
            }
        };
        Observable.just(1).hide().switchMap(Functions.justFunction(ps)).subscribe(to);
        ps.onNext(1);
        to.assertResult(1);
    }

    @Test
    public void innerErrorsReentrant() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                ps.onError(new TestException());
            }
        };
        Observable.just(1).hide().switchMap(Functions.justFunction(ps)).subscribe(to);
        ps.onNext(1);
        to.assertFailure(TestException.class, 1);
    }

    @Test
    public void innerDisposedOnMainError() {
        final PublishSubject<Integer> main = PublishSubject.create();
        final PublishSubject<Integer> inner = PublishSubject.create();
        TestObserver<Integer> to = main.switchMap(Functions.justFunction(inner)).test();
        assertTrue(main.hasObservers());
        main.onNext(1);
        assertTrue(inner.hasObservers());
        main.onError(new TestException());
        assertFalse(inner.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void outerInnerErrorRaceIgnoreDispose() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final AtomicReference<Observer<? super Integer>> obs1 = new AtomicReference<>();
                final Observable<Integer> ps1 = new Observable<Integer>() {

                    @Override
                    protected void subscribeActual(Observer<? super Integer> observer) {
                        obs1.set(observer);
                    }
                };
                final AtomicReference<Observer<? super Integer>> obs2 = new AtomicReference<>();
                final Observable<Integer> ps2 = new Observable<Integer>() {

                    @Override
                    protected void subscribeActual(Observer<? super Integer> observer) {
                        obs2.set(observer);
                    }
                };
                ps1.switchMap(new Function<Integer, ObservableSource<Integer>>() {

                    @Override
                    public ObservableSource<Integer> apply(Integer v) throws Exception {
                        if (v == 1) {
                            return ps2;
                        }
                        return Observable.never();
                    }
                }).test();
                obs1.get().onSubscribe(Disposable.empty());
                obs1.get().onNext(1);
                obs2.get().onSubscribe(Disposable.empty());
                final TestException ex1 = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        obs1.get().onError(ex1);
                    }
                };
                final TestException ex2 = new TestException();
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        obs2.get().onError(ex2);
                    }
                };
                TestHelper.race(r1, r2);
                for (Throwable e : errors) {
                    assertTrue(e.toString(), e.getCause() instanceof TestException);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void asyncFused() {
        Observable.just(1).hide().switchMap(Functions.justFunction(Observable.range(1, 5).observeOn(ImmediateThinScheduler.INSTANCE))).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void syncFusedMaybe() {
        Observable.range(1, 5).hide().switchMap(Functions.justFunction(Maybe.just(1).toObservable())).test().assertResult(1, 1, 1, 1, 1);
    }

    @Test
    public void syncFusedSingle() {
        Observable.range(1, 5).hide().switchMap(Functions.justFunction(Single.just(1).toObservable())).test().assertResult(1, 1, 1, 1, 1);
    }

    @Test
    public void syncFusedCompletable() {
        Observable.range(1, 5).hide().switchMap(Functions.justFunction(Completable.complete().toObservable())).test().assertResult();
    }

    @Test
    public void asyncFusedRejecting() {
        Observable.just(1).hide().switchMap(Functions.justFunction(TestHelper.rejectObservableFusion())).test().assertEmpty();
    }

    @Test
    public void asyncFusedPollCrash() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.switchMap(Functions.justFunction(Observable.range(1, 5).observeOn(ImmediateThinScheduler.INSTANCE).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).compose(TestHelper.<Integer>observableStripBoundary()))).test();
        to.assertEmpty();
        ps.onNext(1);
        to.assertFailure(TestException.class);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void asyncFusedPollCrashDelayError() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.switchMapDelayError(Functions.justFunction(Observable.range(1, 5).observeOn(ImmediateThinScheduler.INSTANCE).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).compose(TestHelper.<Integer>observableStripBoundary()))).test();
        to.assertEmpty();
        ps.onNext(1);
        assertTrue(ps.hasObservers());
        to.assertEmpty();
        ps.onComplete();
        to.assertFailure(TestException.class);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void fusedBoundary() {
        String thread = Thread.currentThread().getName();
        TestObserver<Object> to = Observable.range(1, 10000).switchMap(new Function<Integer, ObservableSource<? extends Object>>() {

            @Override
            public ObservableSource<? extends Object> apply(Integer v) throws Exception {
                return Observable.just(2).hide().observeOn(Schedulers.single()).map(new Function<Integer, Object>() {

                    @Override
                    public Object apply(Integer w) throws Exception {
                        return Thread.currentThread().getName();
                    }
                });
            }
        }).test().awaitDone(5, TimeUnit.SECONDS).assertNoErrors().assertComplete();
        for (Object o : to.values()) {
            assertNotEquals(thread, o);
        }
    }

    @Test
    public void undeliverableUponCancel() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final TestObserverEx<Integer> to = new TestObserverEx<>();
            Observable.just(1).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Throwable {
                    to.dispose();
                    throw new TestException();
                }
            }).switchMap(new Function<Integer, Observable<Integer>>() {

                @Override
                public Observable<Integer> apply(Integer v) throws Throwable {
                    return Observable.just(v).hide();
                }
            }).subscribe(to);
            to.assertEmpty();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void switchMapFusedIterable() {
        Observable.range(1, 2).switchMap(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer v) throws Throwable {
                return Observable.fromIterable(Arrays.asList(v * 10));
            }
        }).test().assertResult(10, 20);
    }

    @Test
    public void switchMapHiddenIterable() {
        Observable.range(1, 2).switchMap(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer v) throws Throwable {
                return Observable.fromIterable(Arrays.asList(v * 10)).hide();
            }
        }).test().assertResult(10, 20);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(f -> f.switchMap(v -> Observable.never()));
    }

    @Test
    public void mainCompleteCancelRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            AtomicReference<Observer<? super Integer>> ref = new AtomicReference<>();
            Observable<Integer> o = new Observable<Integer>() {

                @Override
                protected void subscribeActual(@NonNull Observer<? super @NonNull Integer> observer) {
                    ref.set(observer);
                }
            };
            TestObserver<Object> to = o.switchMap(v -> Observable.never()).test();
            ref.get().onSubscribe(Disposable.empty());
            TestHelper.race(() -> ref.get().onComplete(), () -> to.dispose());
        }
    }

    @Test
    public void mainCompleteInnerErrorRace() {
        TestException ex = new TestException();
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            AtomicReference<Observer<? super Integer>> ref1 = new AtomicReference<>();
            Observable<Integer> o1 = new Observable<Integer>() {

                @Override
                protected void subscribeActual(@NonNull Observer<? super @NonNull Integer> observer) {
                    ref1.set(observer);
                }
            };
            AtomicReference<Observer<? super Integer>> ref2 = new AtomicReference<>();
            Observable<Integer> o2 = new Observable<Integer>() {

                @Override
                protected void subscribeActual(@NonNull Observer<? super @NonNull Integer> observer) {
                    ref2.set(observer);
                }
            };
            o1.switchMap(v -> o2).test();
            ref1.get().onSubscribe(Disposable.empty());
            ref1.get().onNext(1);
            ref2.get().onSubscribe(Disposable.empty());
            TestHelper.race(() -> ref1.get().onComplete(), () -> ref2.get().onError(ex));
        }
    }

    @Test
    public void innerNoSubscriptionYet() {
        AtomicReference<Observer<? super Integer>> ref1 = new AtomicReference<>();
        Observable<Integer> o1 = new Observable<Integer>() {

            @Override
            protected void subscribeActual(@NonNull Observer<? super @NonNull Integer> observer) {
                ref1.set(observer);
            }
        };
        AtomicReference<Observer<? super Integer>> ref2 = new AtomicReference<>();
        Observable<Integer> o2 = new Observable<Integer>() {

            @Override
            protected void subscribeActual(@NonNull Observer<? super @NonNull Integer> observer) {
                ref2.set(observer);
            }
        };
        o1.switchMap(v -> o2).test();
        ref1.get().onSubscribe(Disposable.empty());
        ref1.get().onNext(1);
        ref1.get().onComplete();
    }

    @Test
    public void switchDuringOnNext() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.switchMap(v -> Observable.range(v, 5)).doOnNext(v -> {
            if (v == 1) {
                ps.onNext(2);
            }
        }).test();
        ps.onNext(1);
        to.assertValuesOnly(1, 2, 3, 4, 5, 6);
    }

    @Test
    public void mainCompleteWhileInnerActive() {
        PublishSubject<Integer> ps1 = PublishSubject.create();
        PublishSubject<Integer> ps2 = PublishSubject.create();
        TestObserver<Integer> to = ps1.switchMapDelayError(v -> ps2).test();
        ps1.onNext(1);
        ps1.onComplete();
        ps2.onComplete();
        to.assertResult();
    }

    @Test
    public void innerIgnoresCancelAndErrors() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            PublishSubject<Integer> ps = PublishSubject.create();
            TestObserver<Object> to = ps.switchMap(v -> {
                if (v == 1) {
                    return Observable.unsafeCreate(s -> {
                        s.onSubscribe(Disposable.empty());
                        ps.onNext(2);
                        s.onError(new TestException());
                    });
                }
                return Observable.never();
            }).test();
            ps.onNext(1);
            to.assertEmpty();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void cancellationShouldTriggerInnerCancellationRace() throws Throwable {
        AtomicInteger outer = new AtomicInteger();
        AtomicInteger inner = new AtomicInteger();
        int n = 10_000;
        for (int i = 0; i < n; i++) {
            Observable.<Integer>create(it -> {
                it.onNext(0);
            }).switchMap(v -> createObservable(inner)).observeOn(Schedulers.computation()).doFinally(() -> {
                outer.incrementAndGet();
            }).take(1).blockingSubscribe(v -> {
            }, Throwable::printStackTrace);
        }
        Thread.sleep(100);
        assertEquals(inner.get(), outer.get());
        assertEquals(n, inner.get());
    }

    Observable<Integer> createObservable(AtomicInteger inner) {
        return Observable.<Integer>unsafeCreate(s -> {
            SerializedObserver<Integer> it = new SerializedObserver<>(s);
            it.onSubscribe(Disposable.empty());
            Schedulers.io().scheduleDirect(() -> {
                it.onNext(1);
            }, 0, TimeUnit.MILLISECONDS);
            Schedulers.io().scheduleDirect(() -> {
                it.onNext(2);
            }, 0, TimeUnit.MILLISECONDS);
        }).doFinally(() -> {
            inner.incrementAndGet();
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableSwitchTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchWhenOuterCompleteBeforeInner() throws java.lang.Throwable {
            this.payloads.switchWhenOuterCompleteBeforeInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchWhenInnerCompleteBeforeOuter() throws java.lang.Throwable {
            this.payloads.switchWhenInnerCompleteBeforeOuter.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchWithComplete() throws java.lang.Throwable {
            this.payloads.switchWithComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchWithError() throws java.lang.Throwable {
            this.payloads.switchWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchWithSubsequenceComplete() throws java.lang.Throwable {
            this.payloads.switchWithSubsequenceComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchWithSubsequenceError() throws java.lang.Throwable {
            this.payloads.switchWithSubsequenceError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchIssue737() throws java.lang.Throwable {
            this.payloads.switchIssue737.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribe() throws java.lang.Throwable {
            this.payloads.unsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue2654() throws java.lang.Throwable {
            this.payloads.issue2654.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrors() throws java.lang.Throwable {
            this.payloads.delayErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchOnNextDelayError() throws java.lang.Throwable {
            this.payloads.switchOnNextDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchOnNextDelayErrorWithError() throws java.lang.Throwable {
            this.payloads.switchOnNextDelayErrorWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchOnNextDelayErrorBufferSize() throws java.lang.Throwable {
            this.payloads.switchOnNextDelayErrorBufferSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchMapDelayErrorEmptySource() throws java.lang.Throwable {
            this.payloads.switchMapDelayErrorEmptySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchMapDelayErrorJustSource() throws java.lang.Throwable {
            this.payloads.switchMapDelayErrorJustSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchMapErrorEmptySource() throws java.lang.Throwable {
            this.payloads.switchMapErrorEmptySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchMapJustSource() throws java.lang.Throwable {
            this.payloads.switchMapJustSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchMapInnerCancelled() throws java.lang.Throwable {
            this.payloads.switchMapInnerCancelled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchMapSingleJustSource() throws java.lang.Throwable {
            this.payloads.switchMapSingleJustSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchMapSingleMapperReturnsNull() throws java.lang.Throwable {
            this.payloads.switchMapSingleMapperReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchMapSingleFunctionDoesntReturnSingle() throws java.lang.Throwable {
            this.payloads.switchMapSingleFunctionDoesntReturnSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchMapSingleDelayErrorJustSource() throws java.lang.Throwable {
            this.payloads.switchMapSingleDelayErrorJustSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarMap() throws java.lang.Throwable {
            this.payloads.scalarMap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarMapDelayError() throws java.lang.Throwable {
            this.payloads.scalarMapDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextSourceErrorRace() throws java.lang.Throwable {
            this.payloads.nextSourceErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_outerInnerErrorRace() throws java.lang.Throwable {
            this.payloads.outerInnerErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextCancelRace() throws java.lang.Throwable {
            this.payloads.nextCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrows() throws java.lang.Throwable {
            this.payloads.mapperThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badMainSource() throws java.lang.Throwable {
            this.payloads.badMainSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyInner() throws java.lang.Throwable {
            this.payloads.emptyInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justInner() throws java.lang.Throwable {
            this.payloads.justInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badInnerSource() throws java.lang.Throwable {
            this.payloads.badInnerSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerCompletesReentrant() throws java.lang.Throwable {
            this.payloads.innerCompletesReentrant.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorsReentrant() throws java.lang.Throwable {
            this.payloads.innerErrorsReentrant.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerDisposedOnMainError() throws java.lang.Throwable {
            this.payloads.innerDisposedOnMainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_outerInnerErrorRaceIgnoreDispose() throws java.lang.Throwable {
            this.payloads.outerInnerErrorRaceIgnoreDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFused() throws java.lang.Throwable {
            this.payloads.asyncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedMaybe() throws java.lang.Throwable {
            this.payloads.syncFusedMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedSingle() throws java.lang.Throwable {
            this.payloads.syncFusedSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedCompletable() throws java.lang.Throwable {
            this.payloads.syncFusedCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedRejecting() throws java.lang.Throwable {
            this.payloads.asyncFusedRejecting.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedPollCrash() throws java.lang.Throwable {
            this.payloads.asyncFusedPollCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedPollCrashDelayError() throws java.lang.Throwable {
            this.payloads.asyncFusedPollCrashDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedBoundary() throws java.lang.Throwable {
            this.payloads.fusedBoundary.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchMapFusedIterable() throws java.lang.Throwable {
            this.payloads.switchMapFusedIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchMapHiddenIterable() throws java.lang.Throwable {
            this.payloads.switchMapHiddenIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompleteCancelRace() throws java.lang.Throwable {
            this.payloads.mainCompleteCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompleteInnerErrorRace() throws java.lang.Throwable {
            this.payloads.mainCompleteInnerErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerNoSubscriptionYet() throws java.lang.Throwable {
            this.payloads.innerNoSubscriptionYet.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchDuringOnNext() throws java.lang.Throwable {
            this.payloads.switchDuringOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompleteWhileInnerActive() throws java.lang.Throwable {
            this.payloads.mainCompleteWhileInnerActive.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerIgnoresCancelAndErrors() throws java.lang.Throwable {
            this.payloads.innerIgnoresCancelAndErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellationShouldTriggerInnerCancellationRace() throws java.lang.Throwable {
            this.payloads.cancellationShouldTriggerInnerCancellationRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSwitchTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSwitchTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSwitchTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSwitchTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableSwitchTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSwitchTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableSwitchTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableSwitchTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement switchWhenOuterCompleteBeforeInner;

            public org.junit.runners.model.Statement switchWhenInnerCompleteBeforeOuter;

            public org.junit.runners.model.Statement switchWithComplete;

            public org.junit.runners.model.Statement switchWithError;

            public org.junit.runners.model.Statement switchWithSubsequenceComplete;

            public org.junit.runners.model.Statement switchWithSubsequenceError;

            public org.junit.runners.model.Statement switchIssue737;

            public org.junit.runners.model.Statement unsubscribe;

            public org.junit.runners.model.Statement issue2654;

            public org.junit.runners.model.Statement delayErrors;

            public org.junit.runners.model.Statement switchOnNextDelayError;

            public org.junit.runners.model.Statement switchOnNextDelayErrorWithError;

            public org.junit.runners.model.Statement switchOnNextDelayErrorBufferSize;

            public org.junit.runners.model.Statement switchMapDelayErrorEmptySource;

            public org.junit.runners.model.Statement switchMapDelayErrorJustSource;

            public org.junit.runners.model.Statement switchMapErrorEmptySource;

            public org.junit.runners.model.Statement switchMapJustSource;

            public org.junit.runners.model.Statement switchMapInnerCancelled;

            public org.junit.runners.model.Statement switchMapSingleJustSource;

            public org.junit.runners.model.Statement switchMapSingleMapperReturnsNull;

            public org.junit.runners.model.Statement switchMapSingleFunctionDoesntReturnSingle;

            public org.junit.runners.model.Statement switchMapSingleDelayErrorJustSource;

            public org.junit.runners.model.Statement scalarMap;

            public org.junit.runners.model.Statement scalarMapDelayError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement nextSourceErrorRace;

            public org.junit.runners.model.Statement outerInnerErrorRace;

            public org.junit.runners.model.Statement nextCancelRace;

            public org.junit.runners.model.Statement mapperThrows;

            public org.junit.runners.model.Statement badMainSource;

            public org.junit.runners.model.Statement emptyInner;

            public org.junit.runners.model.Statement justInner;

            public org.junit.runners.model.Statement badInnerSource;

            public org.junit.runners.model.Statement innerCompletesReentrant;

            public org.junit.runners.model.Statement innerErrorsReentrant;

            public org.junit.runners.model.Statement innerDisposedOnMainError;

            public org.junit.runners.model.Statement outerInnerErrorRaceIgnoreDispose;

            public org.junit.runners.model.Statement asyncFused;

            public org.junit.runners.model.Statement syncFusedMaybe;

            public org.junit.runners.model.Statement syncFusedSingle;

            public org.junit.runners.model.Statement syncFusedCompletable;

            public org.junit.runners.model.Statement asyncFusedRejecting;

            public org.junit.runners.model.Statement asyncFusedPollCrash;

            public org.junit.runners.model.Statement asyncFusedPollCrashDelayError;

            public org.junit.runners.model.Statement fusedBoundary;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement switchMapFusedIterable;

            public org.junit.runners.model.Statement switchMapHiddenIterable;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement mainCompleteCancelRace;

            public org.junit.runners.model.Statement mainCompleteInnerErrorRace;

            public org.junit.runners.model.Statement innerNoSubscriptionYet;

            public org.junit.runners.model.Statement switchDuringOnNext;

            public org.junit.runners.model.Statement mainCompleteWhileInnerActive;

            public org.junit.runners.model.Statement innerIgnoresCancelAndErrors;

            public org.junit.runners.model.Statement cancellationShouldTriggerInnerCancellationRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.switchWhenOuterCompleteBeforeInner = _ClassStatement.forPayload(ObservableSwitchTest::switchWhenOuterCompleteBeforeInner, "switchWhenOuterCompleteBeforeInner", this);
            this.payloads.switchWhenInnerCompleteBeforeOuter = _ClassStatement.forPayload(ObservableSwitchTest::switchWhenInnerCompleteBeforeOuter, "switchWhenInnerCompleteBeforeOuter", this);
            this.payloads.switchWithComplete = _ClassStatement.forPayload(ObservableSwitchTest::switchWithComplete, "switchWithComplete", this);
            this.payloads.switchWithError = _ClassStatement.forPayload(ObservableSwitchTest::switchWithError, "switchWithError", this);
            this.payloads.switchWithSubsequenceComplete = _ClassStatement.forPayload(ObservableSwitchTest::switchWithSubsequenceComplete, "switchWithSubsequenceComplete", this);
            this.payloads.switchWithSubsequenceError = _ClassStatement.forPayload(ObservableSwitchTest::switchWithSubsequenceError, "switchWithSubsequenceError", this);
            this.payloads.switchIssue737 = _ClassStatement.forPayload(ObservableSwitchTest::switchIssue737, "switchIssue737", this);
            this.payloads.unsubscribe = _ClassStatement.forPayload(ObservableSwitchTest::unsubscribe, "unsubscribe", this);
            this.payloads.issue2654 = _ClassStatement.forPayload(ObservableSwitchTest::issue2654, "issue2654", this);
            this.payloads.delayErrors = _ClassStatement.forPayload(ObservableSwitchTest::delayErrors, "delayErrors", this);
            this.payloads.switchOnNextDelayError = _ClassStatement.forPayload(ObservableSwitchTest::switchOnNextDelayError, "switchOnNextDelayError", this);
            this.payloads.switchOnNextDelayErrorWithError = _ClassStatement.forPayload(ObservableSwitchTest::switchOnNextDelayErrorWithError, "switchOnNextDelayErrorWithError", this);
            this.payloads.switchOnNextDelayErrorBufferSize = _ClassStatement.forPayload(ObservableSwitchTest::switchOnNextDelayErrorBufferSize, "switchOnNextDelayErrorBufferSize", this);
            this.payloads.switchMapDelayErrorEmptySource = _ClassStatement.forPayload(ObservableSwitchTest::switchMapDelayErrorEmptySource, "switchMapDelayErrorEmptySource", this);
            this.payloads.switchMapDelayErrorJustSource = _ClassStatement.forPayload(ObservableSwitchTest::switchMapDelayErrorJustSource, "switchMapDelayErrorJustSource", this);
            this.payloads.switchMapErrorEmptySource = _ClassStatement.forPayload(ObservableSwitchTest::switchMapErrorEmptySource, "switchMapErrorEmptySource", this);
            this.payloads.switchMapJustSource = _ClassStatement.forPayload(ObservableSwitchTest::switchMapJustSource, "switchMapJustSource", this);
            this.payloads.switchMapInnerCancelled = _ClassStatement.forPayload(ObservableSwitchTest::switchMapInnerCancelled, "switchMapInnerCancelled", this);
            this.payloads.switchMapSingleJustSource = _ClassStatement.forPayload(ObservableSwitchTest::switchMapSingleJustSource, "switchMapSingleJustSource", this);
            this.payloads.switchMapSingleMapperReturnsNull = _ClassStatement.forPayload(ObservableSwitchTest::switchMapSingleMapperReturnsNull, "switchMapSingleMapperReturnsNull", this);
            this.payloads.switchMapSingleFunctionDoesntReturnSingle = _ClassStatement.forPayload(ObservableSwitchTest::switchMapSingleFunctionDoesntReturnSingle, "switchMapSingleFunctionDoesntReturnSingle", this);
            this.payloads.switchMapSingleDelayErrorJustSource = _ClassStatement.forPayload(ObservableSwitchTest::switchMapSingleDelayErrorJustSource, "switchMapSingleDelayErrorJustSource", this);
            this.payloads.scalarMap = _ClassStatement.forPayload(ObservableSwitchTest::scalarMap, "scalarMap", this);
            this.payloads.scalarMapDelayError = _ClassStatement.forPayload(ObservableSwitchTest::scalarMapDelayError, "scalarMapDelayError", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableSwitchTest::dispose, "dispose", this);
            this.payloads.nextSourceErrorRace = _ClassStatement.forPayload(ObservableSwitchTest::nextSourceErrorRace, "nextSourceErrorRace", this);
            this.payloads.outerInnerErrorRace = _ClassStatement.forPayload(ObservableSwitchTest::outerInnerErrorRace, "outerInnerErrorRace", this);
            this.payloads.nextCancelRace = _ClassStatement.forPayload(ObservableSwitchTest::nextCancelRace, "nextCancelRace", this);
            this.payloads.mapperThrows = _ClassStatement.forPayload(ObservableSwitchTest::mapperThrows, "mapperThrows", this);
            this.payloads.badMainSource = _ClassStatement.forPayload(ObservableSwitchTest::badMainSource, "badMainSource", this);
            this.payloads.emptyInner = _ClassStatement.forPayload(ObservableSwitchTest::emptyInner, "emptyInner", this);
            this.payloads.justInner = _ClassStatement.forPayload(ObservableSwitchTest::justInner, "justInner", this);
            this.payloads.badInnerSource = _ClassStatement.forPayload(ObservableSwitchTest::badInnerSource, "badInnerSource", this);
            this.payloads.innerCompletesReentrant = _ClassStatement.forPayload(ObservableSwitchTest::innerCompletesReentrant, "innerCompletesReentrant", this);
            this.payloads.innerErrorsReentrant = _ClassStatement.forPayload(ObservableSwitchTest::innerErrorsReentrant, "innerErrorsReentrant", this);
            this.payloads.innerDisposedOnMainError = _ClassStatement.forPayload(ObservableSwitchTest::innerDisposedOnMainError, "innerDisposedOnMainError", this);
            this.payloads.outerInnerErrorRaceIgnoreDispose = _ClassStatement.forPayload(ObservableSwitchTest::outerInnerErrorRaceIgnoreDispose, "outerInnerErrorRaceIgnoreDispose", this);
            this.payloads.asyncFused = _ClassStatement.forPayload(ObservableSwitchTest::asyncFused, "asyncFused", this);
            this.payloads.syncFusedMaybe = _ClassStatement.forPayload(ObservableSwitchTest::syncFusedMaybe, "syncFusedMaybe", this);
            this.payloads.syncFusedSingle = _ClassStatement.forPayload(ObservableSwitchTest::syncFusedSingle, "syncFusedSingle", this);
            this.payloads.syncFusedCompletable = _ClassStatement.forPayload(ObservableSwitchTest::syncFusedCompletable, "syncFusedCompletable", this);
            this.payloads.asyncFusedRejecting = _ClassStatement.forPayload(ObservableSwitchTest::asyncFusedRejecting, "asyncFusedRejecting", this);
            this.payloads.asyncFusedPollCrash = _ClassStatement.forPayload(ObservableSwitchTest::asyncFusedPollCrash, "asyncFusedPollCrash", this);
            this.payloads.asyncFusedPollCrashDelayError = _ClassStatement.forPayload(ObservableSwitchTest::asyncFusedPollCrashDelayError, "asyncFusedPollCrashDelayError", this);
            this.payloads.fusedBoundary = _ClassStatement.forPayload(ObservableSwitchTest::fusedBoundary, "fusedBoundary", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(ObservableSwitchTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.switchMapFusedIterable = _ClassStatement.forPayload(ObservableSwitchTest::switchMapFusedIterable, "switchMapFusedIterable", this);
            this.payloads.switchMapHiddenIterable = _ClassStatement.forPayload(ObservableSwitchTest::switchMapHiddenIterable, "switchMapHiddenIterable", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableSwitchTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.mainCompleteCancelRace = _ClassStatement.forPayload(ObservableSwitchTest::mainCompleteCancelRace, "mainCompleteCancelRace", this);
            this.payloads.mainCompleteInnerErrorRace = _ClassStatement.forPayload(ObservableSwitchTest::mainCompleteInnerErrorRace, "mainCompleteInnerErrorRace", this);
            this.payloads.innerNoSubscriptionYet = _ClassStatement.forPayload(ObservableSwitchTest::innerNoSubscriptionYet, "innerNoSubscriptionYet", this);
            this.payloads.switchDuringOnNext = _ClassStatement.forPayload(ObservableSwitchTest::switchDuringOnNext, "switchDuringOnNext", this);
            this.payloads.mainCompleteWhileInnerActive = _ClassStatement.forPayload(ObservableSwitchTest::mainCompleteWhileInnerActive, "mainCompleteWhileInnerActive", this);
            this.payloads.innerIgnoresCancelAndErrors = _ClassStatement.forPayload(ObservableSwitchTest::innerIgnoresCancelAndErrors, "innerIgnoresCancelAndErrors", this);
            this.payloads.cancellationShouldTriggerInnerCancellationRace = _ClassStatement.forPayload(ObservableSwitchTest::cancellationShouldTriggerInnerCancellationRace, "cancellationShouldTriggerInnerCancellationRace", this);
        }
    }
}
