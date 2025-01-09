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
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableConcatMapEagerTest extends RxJavaTest {

    @Test
    public void normal() {
        Observable.range(1, 5).concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer t) {
                return Observable.range(t, 2);
            }
        }).test().assertResult(1, 2, 2, 3, 3, 4, 4, 5, 5, 6);
    }

    @Test
    public void normalDelayBoundary() {
        Observable.range(1, 5).concatMapEagerDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer t) {
                return Observable.range(t, 2);
            }
        }, false).test().assertResult(1, 2, 2, 3, 3, 4, 4, 5, 5, 6);
    }

    @Test
    public void normalDelayEnd() {
        Observable.range(1, 5).concatMapEagerDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer t) {
                return Observable.range(t, 2);
            }
        }, true).test().assertResult(1, 2, 2, 3, 3, 4, 4, 5, 5, 6);
    }

    @Test
    public void mainErrorsDelayBoundary() {
        PublishSubject<Integer> main = PublishSubject.create();
        final PublishSubject<Integer> inner = PublishSubject.create();
        TestObserverEx<Integer> to = main.concatMapEagerDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer t) {
                return inner;
            }
        }, false).to(TestHelper.<Integer>testConsumer());
        main.onNext(1);
        inner.onNext(2);
        to.assertValue(2);
        main.onError(new TestException("Forced failure"));
        to.assertNoErrors();
        inner.onNext(3);
        inner.onComplete();
        to.assertFailureAndMessage(TestException.class, "Forced failure", 2, 3);
    }

    @Test
    public void mainErrorsDelayEnd() {
        PublishSubject<Integer> main = PublishSubject.create();
        final PublishSubject<Integer> inner = PublishSubject.create();
        TestObserverEx<Integer> to = main.concatMapEagerDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer t) {
                return inner;
            }
        }, true).to(TestHelper.<Integer>testConsumer());
        main.onNext(1);
        main.onNext(2);
        inner.onNext(2);
        to.assertValue(2);
        main.onError(new TestException("Forced failure"));
        to.assertNoErrors();
        inner.onNext(3);
        inner.onComplete();
        to.assertFailureAndMessage(TestException.class, "Forced failure", 2, 3, 2, 3);
    }

    @Test
    public void mainErrorsImmediate() {
        PublishSubject<Integer> main = PublishSubject.create();
        final PublishSubject<Integer> inner = PublishSubject.create();
        TestObserverEx<Integer> to = main.concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer t) {
                return inner;
            }
        }).to(TestHelper.<Integer>testConsumer());
        main.onNext(1);
        main.onNext(2);
        inner.onNext(2);
        to.assertValue(2);
        main.onError(new TestException("Forced failure"));
        assertFalse("inner has subscribers?", inner.hasObservers());
        inner.onNext(3);
        inner.onComplete();
        to.assertFailureAndMessage(TestException.class, "Forced failure", 2);
    }

    @Test
    public void longEager() {
        Observable.range(1, 2 * Observable.bufferSize()).concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) {
                return Observable.just(1);
            }
        }).test().assertValueCount(2 * Observable.bufferSize()).assertNoErrors().assertComplete();
    }

    TestObserver<Object> to;

    Function<Integer, Observable<Integer>> toJust = new Function<Integer, Observable<Integer>>() {

        @Override
        public Observable<Integer> apply(Integer t) {
            return Observable.just(t);
        }
    };

    Function<Integer, Observable<Integer>> toRange = new Function<Integer, Observable<Integer>>() {

        @Override
        public Observable<Integer> apply(Integer t) {
            return Observable.range(t, 2);
        }
    };

    @Before
    public void before() {
        to = new TestObserver<>();
    }

    @Test
    public void simple() {
        Observable.range(1, 100).concatMapEager(toJust).subscribe(to);
        to.assertNoErrors();
        to.assertValueCount(100);
        to.assertComplete();
    }

    @Test
    public void simple2() {
        Observable.range(1, 100).concatMapEager(toRange).subscribe(to);
        to.assertNoErrors();
        to.assertValueCount(200);
        to.assertComplete();
    }

    @Test
    public void eagerness2() {
        final AtomicInteger count = new AtomicInteger();
        Observable<Integer> source = Observable.just(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                count.getAndIncrement();
            }
        }).hide();
        Observable.concatArrayEager(source, source).subscribe(to);
        Assert.assertEquals(2, count.get());
        to.assertValueCount(count.get());
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void eagerness3() {
        final AtomicInteger count = new AtomicInteger();
        Observable<Integer> source = Observable.just(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                count.getAndIncrement();
            }
        }).hide();
        Observable.concatArrayEager(source, source, source).subscribe(to);
        Assert.assertEquals(3, count.get());
        to.assertValueCount(count.get());
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void eagerness4() {
        final AtomicInteger count = new AtomicInteger();
        Observable<Integer> source = Observable.just(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                count.getAndIncrement();
            }
        }).hide();
        Observable.concatArrayEager(source, source, source, source).subscribe(to);
        Assert.assertEquals(4, count.get());
        to.assertValueCount(count.get());
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void eagerness5() {
        final AtomicInteger count = new AtomicInteger();
        Observable<Integer> source = Observable.just(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                count.getAndIncrement();
            }
        }).hide();
        Observable.concatArrayEager(source, source, source, source, source).subscribe(to);
        Assert.assertEquals(5, count.get());
        to.assertValueCount(count.get());
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void eagerness6() {
        final AtomicInteger count = new AtomicInteger();
        Observable<Integer> source = Observable.just(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                count.getAndIncrement();
            }
        }).hide();
        Observable.concatArrayEager(source, source, source, source, source, source).subscribe(to);
        Assert.assertEquals(6, count.get());
        to.assertValueCount(count.get());
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void eagerness7() {
        final AtomicInteger count = new AtomicInteger();
        Observable<Integer> source = Observable.just(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                count.getAndIncrement();
            }
        }).hide();
        Observable.concatArrayEager(source, source, source, source, source, source, source).subscribe(to);
        Assert.assertEquals(7, count.get());
        to.assertValueCount(count.get());
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void eagerness8() {
        final AtomicInteger count = new AtomicInteger();
        Observable<Integer> source = Observable.just(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                count.getAndIncrement();
            }
        }).hide();
        Observable.concatArrayEager(source, source, source, source, source, source, source, source).subscribe(to);
        Assert.assertEquals(8, count.get());
        to.assertValueCount(count.get());
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void eagerness9() {
        final AtomicInteger count = new AtomicInteger();
        Observable<Integer> source = Observable.just(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                count.getAndIncrement();
            }
        }).hide();
        Observable.concatArrayEager(source, source, source, source, source, source, source, source, source).subscribe(to);
        Assert.assertEquals(9, count.get());
        to.assertValueCount(count.get());
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void mainError() {
        Observable.<Integer>error(new TestException()).concatMapEager(toJust).subscribe(to);
        to.assertNoValues();
        to.assertError(TestException.class);
        to.assertNotComplete();
    }

    @Test
    public void innerError() {
        // TODO verify: concatMapEager subscribes first then consumes the sources is okay
        PublishSubject<Integer> ps = PublishSubject.create();
        Observable.concatArrayEager(Observable.just(1), ps).subscribe(to);
        ps.onError(new TestException());
        to.assertValue(1);
        to.assertError(TestException.class);
        to.assertNotComplete();
    }

    @Test
    public void innerEmpty() {
        Observable.concatArrayEager(Observable.empty(), Observable.empty()).subscribe(to);
        to.assertNoValues();
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void mapperThrows() {
        Observable.just(1).concatMapEager(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t) {
                throw new TestException();
            }
        }).subscribe(to);
        to.assertNoValues();
        to.assertNotComplete();
        to.assertError(TestException.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidMaxConcurrent() {
        Observable.just(1).concatMapEager(toJust, 0, Observable.bufferSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCapacityHint() {
        Observable.just(1).concatMapEager(toJust, Observable.bufferSize(), 0);
    }

    @Test
    public void asynchronousRun() {
        Observable.range(1, 2).concatMapEager(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t) {
                return Observable.range(1, 1000).subscribeOn(Schedulers.computation());
            }
        }).observeOn(Schedulers.newThread()).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors();
        to.assertValueCount(2000);
    }

    @Test
    public void reentrantWork() {
        final PublishSubject<Integer> subject = PublishSubject.create();
        final AtomicBoolean once = new AtomicBoolean();
        subject.concatMapEager(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t) {
                return Observable.just(t);
            }
        }).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                if (once.compareAndSet(false, true)) {
                    subject.onNext(2);
                }
            }
        }).subscribe(to);
        subject.onNext(1);
        to.assertNoErrors();
        to.assertNotComplete();
        to.assertValues(1, 2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void concatArrayEager() throws Exception {
        for (int i = 2; i < 10; i++) {
            Observable<Integer>[] obs = new Observable[i];
            Arrays.fill(obs, Observable.just(1));
            Integer[] expected = new Integer[i];
            Arrays.fill(expected, 1);
            Method m = Observable.class.getMethod("concatArrayEager", ObservableSource[].class);
            TestObserver<Integer> to = TestObserver.create();
            ((Observable<Integer>) m.invoke(null, new Object[] { obs })).subscribe(to);
            to.assertValues(expected);
            to.assertNoErrors();
            to.assertComplete();
        }
    }

    @Test
    public void capacityHint() {
        Observable<Integer> source = Observable.just(1);
        TestObserver<Integer> to = TestObserver.create();
        Observable.concatEager(Arrays.asList(source, source, source), 1, 1).subscribe(to);
        to.assertValues(1, 1, 1);
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void Observable() {
        Observable<Integer> source = Observable.just(1);
        TestObserver<Integer> to = TestObserver.create();
        Observable.concatEager(Observable.just(source, source, source)).subscribe(to);
        to.assertValues(1, 1, 1);
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void ObservableCapacityHint() {
        Observable<Integer> source = Observable.just(1);
        TestObserver<Integer> to = TestObserver.create();
        Observable.concatEager(Observable.just(source, source, source), 1, 1).subscribe(to);
        to.assertValues(1, 1, 1);
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void badCapacityHint() throws Exception {
        Observable<Integer> source = Observable.just(1);
        try {
            Observable.concatEager(Arrays.asList(source, source, source), 1, -99);
        } catch (IllegalArgumentException ex) {
            assertEquals("bufferSize > 0 required but it was -99", ex.getMessage());
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void mappingBadCapacityHint() throws Exception {
        Observable<Integer> source = Observable.just(1);
        try {
            Observable.just(source, source, source).concatMapEager((Function) Functions.identity(), 10, -99);
        } catch (IllegalArgumentException ex) {
            assertEquals("bufferSize > 0 required but it was -99", ex.getMessage());
        }
    }

    @Test
    public void concatEagerIterable() {
        Observable.concatEager(Arrays.asList(Observable.just(1), Observable.just(2))).test().assertResult(1, 2);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).hide().concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(1, 2);
            }
        }));
    }

    @Test
    public void empty() {
        Observable.<Integer>empty().hide().concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(1, 2);
            }
        }).test().assertResult();
    }

    @Test
    public void innerError2() {
        Observable.<Integer>just(1).hide().concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void innerErrorMaxConcurrency() {
        Observable.<Integer>just(1).hide().concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.error(new TestException());
            }
        }, 1, 128).test().assertFailure(TestException.class);
    }

    @Test
    public void innerCallableThrows() {
        Observable.<Integer>just(1).hide().concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.fromCallable(new Callable<Integer>() {

                    @Override
                    public Integer call() throws Exception {
                        throw new TestException();
                    }
                });
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void innerOuterRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> ps1 = PublishSubject.create();
                final PublishSubject<Integer> ps2 = PublishSubject.create();
                TestObserverEx<Integer> to = ps1.concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

                    @Override
                    public ObservableSource<Integer> apply(Integer v) throws Exception {
                        return ps2;
                    }
                }).to(TestHelper.<Integer>testConsumer());
                final TestException ex1 = new TestException();
                final TestException ex2 = new TestException();
                ps1.onNext(1);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps1.onError(ex1);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ps2.onError(ex2);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertSubscribed().assertNoValues().assertNotComplete();
                Throwable ex = to.errors().get(0);
                if (ex instanceof CompositeException) {
                    List<Throwable> es = TestHelper.errorList(to);
                    TestHelper.assertError(es, 0, TestException.class);
                    TestHelper.assertError(es, 1, TestException.class);
                } else {
                    to.assertError(TestException.class);
                    if (!errors.isEmpty()) {
                        TestHelper.assertUndeliverable(errors, 0, TestException.class);
                    }
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
            final TestObserver<Integer> to = ps1.concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

                @Override
                public ObservableSource<Integer> apply(Integer v) throws Exception {
                    return Observable.never();
                }
            }).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps1.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
            to.assertEmpty();
        }
    }

    @Test
    public void mapperCancels() {
        final TestObserver<Integer> to = new TestObserver<>();
        Observable.just(1).hide().concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                to.dispose();
                return Observable.never();
            }
        }, 1, 128).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void innerErrorFused() {
        Observable.<Integer>just(1).hide().concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(1, 2).map(new Function<Integer, Integer>() {

                    @Override
                    public Integer apply(Integer v) throws Exception {
                        throw new TestException();
                    }
                });
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void innerErrorAfterPoll() {
        final UnicastSubject<Integer> us = UnicastSubject.create();
        us.onNext(1);
        TestObserver<Integer> to = new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                us.onError(new TestException());
            }
        };
        Observable.<Integer>just(1).hide().concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return us;
            }
        }, 1, 128).subscribe(to);
        to.assertFailure(TestException.class, 1);
    }

    @Test
    public void fuseAndTake() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        us.onNext(1);
        us.onComplete();
        us.concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.just(1);
            }
        }).take(1).test().assertResult(1);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.concatMapEager(new Function<Object, ObservableSource<Object>>() {

                    @Override
                    public ObservableSource<Object> apply(Object v) throws Exception {
                        return Observable.just(v);
                    }
                });
            }
        });
    }

    @Test
    public void oneDelayed() {
        Observable.just(1, 2, 3, 4, 5).concatMapEager(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer i) throws Exception {
                return i == 3 ? Observable.just(i) : Observable.just(i).delay(1, TimeUnit.MILLISECONDS, Schedulers.io());
            }
        }).observeOn(Schedulers.io()).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void maxConcurrencyOf2() {
        List<Integer>[] list = new ArrayList[100];
        for (int i = 0; i < 100; i++) {
            List<Integer> lst = new ArrayList<>();
            list[i] = lst;
            for (int k = 1; k <= 10; k++) {
                lst.add((i) * 10 + k);
            }
        }
        Observable.range(1, 1000).buffer(10).concatMapEager(new Function<List<Integer>, ObservableSource<List<Integer>>>() {

            @Override
            public ObservableSource<List<Integer>> apply(List<Integer> v) throws Exception {
                return Observable.just(v).subscribeOn(Schedulers.io()).doOnNext(new Consumer<List<Integer>>() {

                    @Override
                    public void accept(List<Integer> v) throws Exception {
                        Thread.sleep(new Random().nextInt(20));
                    }
                });
            }
        }, 2, 3).test().awaitDone(5, TimeUnit.SECONDS).assertResult(list);
    }

    @Test
    public void arrayDelayErrorDefault() {
        PublishSubject<Integer> ps1 = PublishSubject.create();
        PublishSubject<Integer> ps2 = PublishSubject.create();
        PublishSubject<Integer> ps3 = PublishSubject.create();
        TestObserver<Integer> to = Observable.concatArrayEagerDelayError(ps1, ps2, ps3).test();
        to.assertEmpty();
        assertTrue(ps1.hasObservers());
        assertTrue(ps2.hasObservers());
        assertTrue(ps3.hasObservers());
        ps2.onNext(2);
        ps2.onComplete();
        to.assertEmpty();
        ps1.onNext(1);
        to.assertValuesOnly(1);
        ps1.onComplete();
        to.assertValuesOnly(1, 2);
        ps3.onComplete();
        to.assertResult(1, 2);
    }

    @Test
    public void arrayDelayErrorMaxConcurrency() {
        PublishSubject<Integer> ps1 = PublishSubject.create();
        PublishSubject<Integer> ps2 = PublishSubject.create();
        PublishSubject<Integer> ps3 = PublishSubject.create();
        TestObserver<Integer> to = Observable.concatArrayEagerDelayError(2, 2, ps1, ps2, ps3).test();
        to.assertEmpty();
        assertTrue(ps1.hasObservers());
        assertTrue(ps2.hasObservers());
        assertFalse(ps3.hasObservers());
        ps2.onNext(2);
        ps2.onComplete();
        to.assertEmpty();
        ps1.onNext(1);
        to.assertValuesOnly(1);
        ps1.onComplete();
        assertTrue(ps3.hasObservers());
        to.assertValuesOnly(1, 2);
        ps3.onComplete();
        to.assertResult(1, 2);
    }

    @Test
    public void arrayDelayErrorMaxConcurrencyErrorDelayed() {
        PublishSubject<Integer> ps1 = PublishSubject.create();
        PublishSubject<Integer> ps2 = PublishSubject.create();
        PublishSubject<Integer> ps3 = PublishSubject.create();
        TestObserver<Integer> to = Observable.concatArrayEagerDelayError(2, 2, ps1, ps2, ps3).test();
        to.assertEmpty();
        assertTrue(ps1.hasObservers());
        assertTrue(ps2.hasObservers());
        assertFalse(ps3.hasObservers());
        ps2.onNext(2);
        ps2.onError(new TestException());
        to.assertEmpty();
        ps1.onNext(1);
        to.assertValuesOnly(1);
        ps1.onComplete();
        assertTrue(ps3.hasObservers());
        to.assertValuesOnly(1, 2);
        ps3.onComplete();
        to.assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void cancelActive() {
        PublishSubject<Integer> ps1 = PublishSubject.create();
        PublishSubject<Integer> ps2 = PublishSubject.create();
        TestObserver<Integer> to = Observable.concatEager(Observable.just(ps1, ps2)).test();
        assertTrue(ps1.hasObservers());
        assertTrue(ps2.hasObservers());
        to.dispose();
        assertFalse(ps1.hasObservers());
        assertFalse(ps2.hasObservers());
    }

    @Test
    public void cancelNoInnerYet() {
        PublishSubject<Observable<Integer>> ps1 = PublishSubject.create();
        TestObserver<Integer> to = Observable.concatEager(ps1).test();
        assertTrue(ps1.hasObservers());
        to.dispose();
        assertFalse(ps1.hasObservers());
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> upstream) {
                return upstream.concatMapEager(new Function<Integer, Observable<Integer>>() {

                    @Override
                    public Observable<Integer> apply(Integer v) throws Throwable {
                        return Observable.just(v).hide();
                    }
                });
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayError() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> upstream) {
                return upstream.concatMapEagerDelayError(new Function<Integer, Observable<Integer>>() {

                    @Override
                    public Observable<Integer> apply(Integer v) throws Throwable {
                        return Observable.just(v).hide();
                    }
                }, false);
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayErrorTillEnd() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> upstream) {
                return upstream.concatMapEagerDelayError(new Function<Integer, Observable<Integer>>() {

                    @Override
                    public Observable<Integer> apply(Integer v) throws Throwable {
                        return Observable.just(v).hide();
                    }
                }, true);
            }
        });
    }

    @Test
    public void iterableDelayError() {
        Observable.concatEagerDelayError(Arrays.asList(Observable.range(1, 2), Observable.error(new TestException()), Observable.range(3, 3))).test().assertFailure(TestException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void iterableDelayErrorMaxConcurrency() {
        Observable.concatEagerDelayError(Arrays.asList(Observable.range(1, 2), Observable.error(new TestException()), Observable.range(3, 3)), 1, 1).test().assertFailure(TestException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void observerDelayError() {
        Observable.concatEagerDelayError(Observable.fromArray(Observable.range(1, 2), Observable.error(new TestException()), Observable.range(3, 3))).test().assertFailure(TestException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void observerDelayErrorMaxConcurrency() {
        Observable.concatEagerDelayError(Observable.fromArray(Observable.range(1, 2), Observable.error(new TestException()), Observable.range(3, 3)), 1, 1).test().assertFailure(TestException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void innerFusionRejected() {
        Observable.just(1).hide().concatMapEager(v -> TestHelper.rejectObservableFusion()).test().assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableConcatMapEagerTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayBoundary() throws java.lang.Throwable {
            this.payloads.normalDelayBoundary.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayEnd() throws java.lang.Throwable {
            this.payloads.normalDelayEnd.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorsDelayBoundary() throws java.lang.Throwable {
            this.payloads.mainErrorsDelayBoundary.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorsDelayEnd() throws java.lang.Throwable {
            this.payloads.mainErrorsDelayEnd.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorsImmediate() throws java.lang.Throwable {
            this.payloads.mainErrorsImmediate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_longEager() throws java.lang.Throwable {
            this.payloads.longEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.payloads.simple.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple2() throws java.lang.Throwable {
            this.payloads.simple2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eagerness2() throws java.lang.Throwable {
            this.payloads.eagerness2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eagerness3() throws java.lang.Throwable {
            this.payloads.eagerness3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eagerness4() throws java.lang.Throwable {
            this.payloads.eagerness4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eagerness5() throws java.lang.Throwable {
            this.payloads.eagerness5.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eagerness6() throws java.lang.Throwable {
            this.payloads.eagerness6.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eagerness7() throws java.lang.Throwable {
            this.payloads.eagerness7.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eagerness8() throws java.lang.Throwable {
            this.payloads.eagerness8.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eagerness9() throws java.lang.Throwable {
            this.payloads.eagerness9.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.payloads.mainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerError() throws java.lang.Throwable {
            this.payloads.innerError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerEmpty() throws java.lang.Throwable {
            this.payloads.innerEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrows() throws java.lang.Throwable {
            this.payloads.mapperThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_invalidMaxConcurrent() throws java.lang.Throwable {
            this.payloads.invalidMaxConcurrent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_invalidCapacityHint() throws java.lang.Throwable {
            this.payloads.invalidCapacityHint.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asynchronousRun() throws java.lang.Throwable {
            this.payloads.asynchronousRun.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantWork() throws java.lang.Throwable {
            this.payloads.reentrantWork.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArrayEager() throws java.lang.Throwable {
            this.payloads.concatArrayEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_capacityHint() throws java.lang.Throwable {
            this.payloads.capacityHint.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_Observable() throws java.lang.Throwable {
            this.payloads.Observable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ObservableCapacityHint() throws java.lang.Throwable {
            this.payloads.ObservableCapacityHint.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badCapacityHint() throws java.lang.Throwable {
            this.payloads.badCapacityHint.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mappingBadCapacityHint() throws java.lang.Throwable {
            this.payloads.mappingBadCapacityHint.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatEagerIterable() throws java.lang.Throwable {
            this.payloads.concatEagerIterable.evaluate();
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
        public void benchmark_innerError2() throws java.lang.Throwable {
            this.payloads.innerError2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorMaxConcurrency() throws java.lang.Throwable {
            this.payloads.innerErrorMaxConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerCallableThrows() throws java.lang.Throwable {
            this.payloads.innerCallableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerOuterRace() throws java.lang.Throwable {
            this.payloads.innerOuterRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextCancelRace() throws java.lang.Throwable {
            this.payloads.nextCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperCancels() throws java.lang.Throwable {
            this.payloads.mapperCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorFused() throws java.lang.Throwable {
            this.payloads.innerErrorFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorAfterPoll() throws java.lang.Throwable {
            this.payloads.innerErrorAfterPoll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fuseAndTake() throws java.lang.Throwable {
            this.payloads.fuseAndTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneDelayed() throws java.lang.Throwable {
            this.payloads.oneDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maxConcurrencyOf2() throws java.lang.Throwable {
            this.payloads.maxConcurrencyOf2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_arrayDelayErrorDefault() throws java.lang.Throwable {
            this.payloads.arrayDelayErrorDefault.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_arrayDelayErrorMaxConcurrency() throws java.lang.Throwable {
            this.payloads.arrayDelayErrorMaxConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_arrayDelayErrorMaxConcurrencyErrorDelayed() throws java.lang.Throwable {
            this.payloads.arrayDelayErrorMaxConcurrencyErrorDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelActive() throws java.lang.Throwable {
            this.payloads.cancelActive.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelNoInnerYet() throws java.lang.Throwable {
            this.payloads.cancelNoInnerYet.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelDelayError() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancelDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelDelayErrorTillEnd() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancelDelayErrorTillEnd.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableDelayError() throws java.lang.Throwable {
            this.payloads.iterableDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableDelayErrorMaxConcurrency() throws java.lang.Throwable {
            this.payloads.iterableDelayErrorMaxConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerDelayError() throws java.lang.Throwable {
            this.payloads.observerDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerDelayErrorMaxConcurrency() throws java.lang.Throwable {
            this.payloads.observerDelayErrorMaxConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerFusionRejected() throws java.lang.Throwable {
            this.payloads.innerFusionRejected.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapEagerTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapEagerTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapEagerTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapEagerTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableConcatMapEagerTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapEagerTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableConcatMapEagerTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableConcatMapEagerTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement normalDelayBoundary;

            public org.junit.runners.model.Statement normalDelayEnd;

            public org.junit.runners.model.Statement mainErrorsDelayBoundary;

            public org.junit.runners.model.Statement mainErrorsDelayEnd;

            public org.junit.runners.model.Statement mainErrorsImmediate;

            public org.junit.runners.model.Statement longEager;

            public org.junit.runners.model.Statement simple;

            public org.junit.runners.model.Statement simple2;

            public org.junit.runners.model.Statement eagerness2;

            public org.junit.runners.model.Statement eagerness3;

            public org.junit.runners.model.Statement eagerness4;

            public org.junit.runners.model.Statement eagerness5;

            public org.junit.runners.model.Statement eagerness6;

            public org.junit.runners.model.Statement eagerness7;

            public org.junit.runners.model.Statement eagerness8;

            public org.junit.runners.model.Statement eagerness9;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement innerError;

            public org.junit.runners.model.Statement innerEmpty;

            public org.junit.runners.model.Statement mapperThrows;

            public org.junit.runners.model.Statement invalidMaxConcurrent;

            public org.junit.runners.model.Statement invalidCapacityHint;

            public org.junit.runners.model.Statement asynchronousRun;

            public org.junit.runners.model.Statement reentrantWork;

            public org.junit.runners.model.Statement concatArrayEager;

            public org.junit.runners.model.Statement capacityHint;

            public org.junit.runners.model.Statement Observable;

            public org.junit.runners.model.Statement ObservableCapacityHint;

            public org.junit.runners.model.Statement badCapacityHint;

            public org.junit.runners.model.Statement mappingBadCapacityHint;

            public org.junit.runners.model.Statement concatEagerIterable;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement innerError2;

            public org.junit.runners.model.Statement innerErrorMaxConcurrency;

            public org.junit.runners.model.Statement innerCallableThrows;

            public org.junit.runners.model.Statement innerOuterRace;

            public org.junit.runners.model.Statement nextCancelRace;

            public org.junit.runners.model.Statement mapperCancels;

            public org.junit.runners.model.Statement innerErrorFused;

            public org.junit.runners.model.Statement innerErrorAfterPoll;

            public org.junit.runners.model.Statement fuseAndTake;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement oneDelayed;

            public org.junit.runners.model.Statement maxConcurrencyOf2;

            public org.junit.runners.model.Statement arrayDelayErrorDefault;

            public org.junit.runners.model.Statement arrayDelayErrorMaxConcurrency;

            public org.junit.runners.model.Statement arrayDelayErrorMaxConcurrencyErrorDelayed;

            public org.junit.runners.model.Statement cancelActive;

            public org.junit.runners.model.Statement cancelNoInnerYet;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayErrorTillEnd;

            public org.junit.runners.model.Statement iterableDelayError;

            public org.junit.runners.model.Statement iterableDelayErrorMaxConcurrency;

            public org.junit.runners.model.Statement observerDelayError;

            public org.junit.runners.model.Statement observerDelayErrorMaxConcurrency;

            public org.junit.runners.model.Statement innerFusionRejected;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(ObservableConcatMapEagerTest::normal, "normal", this);
            this.payloads.normalDelayBoundary = _ClassStatement.forPayload(ObservableConcatMapEagerTest::normalDelayBoundary, "normalDelayBoundary", this);
            this.payloads.normalDelayEnd = _ClassStatement.forPayload(ObservableConcatMapEagerTest::normalDelayEnd, "normalDelayEnd", this);
            this.payloads.mainErrorsDelayBoundary = _ClassStatement.forPayload(ObservableConcatMapEagerTest::mainErrorsDelayBoundary, "mainErrorsDelayBoundary", this);
            this.payloads.mainErrorsDelayEnd = _ClassStatement.forPayload(ObservableConcatMapEagerTest::mainErrorsDelayEnd, "mainErrorsDelayEnd", this);
            this.payloads.mainErrorsImmediate = _ClassStatement.forPayload(ObservableConcatMapEagerTest::mainErrorsImmediate, "mainErrorsImmediate", this);
            this.payloads.longEager = _ClassStatement.forPayload(ObservableConcatMapEagerTest::longEager, "longEager", this);
            this.payloads.simple = _ClassStatement.forPayload(ObservableConcatMapEagerTest::simple, "simple", this);
            this.payloads.simple2 = _ClassStatement.forPayload(ObservableConcatMapEagerTest::simple2, "simple2", this);
            this.payloads.eagerness2 = _ClassStatement.forPayload(ObservableConcatMapEagerTest::eagerness2, "eagerness2", this);
            this.payloads.eagerness3 = _ClassStatement.forPayload(ObservableConcatMapEagerTest::eagerness3, "eagerness3", this);
            this.payloads.eagerness4 = _ClassStatement.forPayload(ObservableConcatMapEagerTest::eagerness4, "eagerness4", this);
            this.payloads.eagerness5 = _ClassStatement.forPayload(ObservableConcatMapEagerTest::eagerness5, "eagerness5", this);
            this.payloads.eagerness6 = _ClassStatement.forPayload(ObservableConcatMapEagerTest::eagerness6, "eagerness6", this);
            this.payloads.eagerness7 = _ClassStatement.forPayload(ObservableConcatMapEagerTest::eagerness7, "eagerness7", this);
            this.payloads.eagerness8 = _ClassStatement.forPayload(ObservableConcatMapEagerTest::eagerness8, "eagerness8", this);
            this.payloads.eagerness9 = _ClassStatement.forPayload(ObservableConcatMapEagerTest::eagerness9, "eagerness9", this);
            this.payloads.mainError = _ClassStatement.forPayload(ObservableConcatMapEagerTest::mainError, "mainError", this);
            this.payloads.innerError = _ClassStatement.forPayload(ObservableConcatMapEagerTest::innerError, "innerError", this);
            this.payloads.innerEmpty = _ClassStatement.forPayload(ObservableConcatMapEagerTest::innerEmpty, "innerEmpty", this);
            this.payloads.mapperThrows = _ClassStatement.forPayload(ObservableConcatMapEagerTest::mapperThrows, "mapperThrows", this);
            this.payloads.invalidMaxConcurrent = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableConcatMapEagerTest::invalidMaxConcurrent, java.lang.IllegalArgumentException.class), "invalidMaxConcurrent", this);
            this.payloads.invalidCapacityHint = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableConcatMapEagerTest::invalidCapacityHint, java.lang.IllegalArgumentException.class), "invalidCapacityHint", this);
            this.payloads.asynchronousRun = _ClassStatement.forPayload(ObservableConcatMapEagerTest::asynchronousRun, "asynchronousRun", this);
            this.payloads.reentrantWork = _ClassStatement.forPayload(ObservableConcatMapEagerTest::reentrantWork, "reentrantWork", this);
            this.payloads.concatArrayEager = _ClassStatement.forPayload(ObservableConcatMapEagerTest::concatArrayEager, "concatArrayEager", this);
            this.payloads.capacityHint = _ClassStatement.forPayload(ObservableConcatMapEagerTest::capacityHint, "capacityHint", this);
            this.payloads.Observable = _ClassStatement.forPayload(ObservableConcatMapEagerTest::Observable, "Observable", this);
            this.payloads.ObservableCapacityHint = _ClassStatement.forPayload(ObservableConcatMapEagerTest::ObservableCapacityHint, "ObservableCapacityHint", this);
            this.payloads.badCapacityHint = _ClassStatement.forPayload(ObservableConcatMapEagerTest::badCapacityHint, "badCapacityHint", this);
            this.payloads.mappingBadCapacityHint = _ClassStatement.forPayload(ObservableConcatMapEagerTest::mappingBadCapacityHint, "mappingBadCapacityHint", this);
            this.payloads.concatEagerIterable = _ClassStatement.forPayload(ObservableConcatMapEagerTest::concatEagerIterable, "concatEagerIterable", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableConcatMapEagerTest::dispose, "dispose", this);
            this.payloads.empty = _ClassStatement.forPayload(ObservableConcatMapEagerTest::empty, "empty", this);
            this.payloads.innerError2 = _ClassStatement.forPayload(ObservableConcatMapEagerTest::innerError2, "innerError2", this);
            this.payloads.innerErrorMaxConcurrency = _ClassStatement.forPayload(ObservableConcatMapEagerTest::innerErrorMaxConcurrency, "innerErrorMaxConcurrency", this);
            this.payloads.innerCallableThrows = _ClassStatement.forPayload(ObservableConcatMapEagerTest::innerCallableThrows, "innerCallableThrows", this);
            this.payloads.innerOuterRace = _ClassStatement.forPayload(ObservableConcatMapEagerTest::innerOuterRace, "innerOuterRace", this);
            this.payloads.nextCancelRace = _ClassStatement.forPayload(ObservableConcatMapEagerTest::nextCancelRace, "nextCancelRace", this);
            this.payloads.mapperCancels = _ClassStatement.forPayload(ObservableConcatMapEagerTest::mapperCancels, "mapperCancels", this);
            this.payloads.innerErrorFused = _ClassStatement.forPayload(ObservableConcatMapEagerTest::innerErrorFused, "innerErrorFused", this);
            this.payloads.innerErrorAfterPoll = _ClassStatement.forPayload(ObservableConcatMapEagerTest::innerErrorAfterPoll, "innerErrorAfterPoll", this);
            this.payloads.fuseAndTake = _ClassStatement.forPayload(ObservableConcatMapEagerTest::fuseAndTake, "fuseAndTake", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableConcatMapEagerTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.oneDelayed = _ClassStatement.forPayload(ObservableConcatMapEagerTest::oneDelayed, "oneDelayed", this);
            this.payloads.maxConcurrencyOf2 = _ClassStatement.forPayload(ObservableConcatMapEagerTest::maxConcurrencyOf2, "maxConcurrencyOf2", this);
            this.payloads.arrayDelayErrorDefault = _ClassStatement.forPayload(ObservableConcatMapEagerTest::arrayDelayErrorDefault, "arrayDelayErrorDefault", this);
            this.payloads.arrayDelayErrorMaxConcurrency = _ClassStatement.forPayload(ObservableConcatMapEagerTest::arrayDelayErrorMaxConcurrency, "arrayDelayErrorMaxConcurrency", this);
            this.payloads.arrayDelayErrorMaxConcurrencyErrorDelayed = _ClassStatement.forPayload(ObservableConcatMapEagerTest::arrayDelayErrorMaxConcurrencyErrorDelayed, "arrayDelayErrorMaxConcurrencyErrorDelayed", this);
            this.payloads.cancelActive = _ClassStatement.forPayload(ObservableConcatMapEagerTest::cancelActive, "cancelActive", this);
            this.payloads.cancelNoInnerYet = _ClassStatement.forPayload(ObservableConcatMapEagerTest::cancelNoInnerYet, "cancelNoInnerYet", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(ObservableConcatMapEagerTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(ObservableConcatMapEagerTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.undeliverableUponCancelDelayErrorTillEnd = _ClassStatement.forPayload(ObservableConcatMapEagerTest::undeliverableUponCancelDelayErrorTillEnd, "undeliverableUponCancelDelayErrorTillEnd", this);
            this.payloads.iterableDelayError = _ClassStatement.forPayload(ObservableConcatMapEagerTest::iterableDelayError, "iterableDelayError", this);
            this.payloads.iterableDelayErrorMaxConcurrency = _ClassStatement.forPayload(ObservableConcatMapEagerTest::iterableDelayErrorMaxConcurrency, "iterableDelayErrorMaxConcurrency", this);
            this.payloads.observerDelayError = _ClassStatement.forPayload(ObservableConcatMapEagerTest::observerDelayError, "observerDelayError", this);
            this.payloads.observerDelayErrorMaxConcurrency = _ClassStatement.forPayload(ObservableConcatMapEagerTest::observerDelayErrorMaxConcurrency, "observerDelayErrorMaxConcurrency", this);
            this.payloads.innerFusionRejected = _ClassStatement.forPayload(ObservableConcatMapEagerTest::innerFusionRejected, "innerFusionRejected", this);
        }
    }
}
