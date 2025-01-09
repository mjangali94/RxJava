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
package io.reactivex.rxjava3.observable;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Verifies the operators handle null values properly by emitting/throwing NullPointerExceptions.
 */
public class ObservableNullTests extends RxJavaTest {

    Observable<Integer> just1 = Observable.just(1);

    // ***********************************************************
    // Static methods
    // ***********************************************************
    @Test
    public void ambIterableIteratorNull() {
        Observable.amb(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
                return null;
            }
        }).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambIterableOneIsNull() {
        Observable.amb(Arrays.asList(Observable.never(), null)).test().assertError(NullPointerException.class);
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestIterableIteratorNull() {
        Observable.combineLatest(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
                return null;
            }
        }, new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestIterableOneIsNull() {
        Observable.combineLatest(Arrays.asList(Observable.never(), null), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestIterableFunctionReturnsNull() {
        Observable.combineLatest(Arrays.asList(just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return null;
            }
        }, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestDelayErrorIterableIteratorNull() {
        Observable.combineLatestDelayError(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
                return null;
            }
        }, new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestDelayErrorIterableOneIsNull() {
        Observable.combineLatestDelayError(Arrays.asList(Observable.never(), null), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestDelayErrorIterableFunctionReturnsNull() {
        Observable.combineLatestDelayError(Arrays.asList(just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return null;
            }
        }, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void concatIterableIteratorNull() {
        Observable.concat(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void concatIterableOneIsNull() {
        Observable.concat(Arrays.asList(just1, null)).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void concatArrayOneIsNull() {
        Observable.concatArray(just1, null).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void deferFunctionReturnsNull() {
        Observable.defer(new Supplier<Observable<Object>>() {

            @Override
            public Observable<Object> get() {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void errorFunctionReturnsNull() {
        Observable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void fromArrayOneIsNull() {
        Observable.fromArray(1, null).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void fromCallableReturnsNull() {
        Observable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return null;
            }
        }).blockingLast();
    }

    @Test
    public void fromFutureReturnsNull() {
        FutureTask<Object> f = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
        f.run();
        TestObserver<Object> to = new TestObserver<>();
        Observable.fromFuture(f).subscribe(to);
        to.assertNoValues();
        to.assertNotComplete();
        to.assertError(NullPointerException.class);
    }

    @Test(expected = NullPointerException.class)
    public void fromFutureTimedReturnsNull() {
        FutureTask<Object> f = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
        f.run();
        Observable.fromFuture(f, 1, TimeUnit.SECONDS).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void fromIterableIteratorNull() {
        Observable.fromIterable(new Iterable<Object>() {

            @Override
            public Iterator<Object> iterator() {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void fromIterableValueNull() {
        Observable.fromIterable(Arrays.asList(1, null)).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void generateConsumerEmitsNull() {
        Observable.generate(new Consumer<Emitter<Object>>() {

            @Override
            public void accept(Emitter<Object> s) {
                s.onNext(null);
            }
        }).blockingLast();
    }

    @Test
    public void generateConsumerStateNullAllowed() {
        BiConsumer<Integer, Emitter<Integer>> generator = new BiConsumer<Integer, Emitter<Integer>>() {

            @Override
            public void accept(Integer s, Emitter<Integer> o) {
                o.onComplete();
            }
        };
        Observable.generate(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return null;
            }
        }, generator).blockingSubscribe();
    }

    @Test
    public void generateFunctionStateNullAllowed() {
        Observable.generate(new Supplier<Object>() {

            @Override
            public Object get() {
                return null;
            }
        }, new BiFunction<Object, Emitter<Object>, Object>() {

            @Override
            public Object apply(Object s, Emitter<Object> o) {
                o.onComplete();
                return s;
            }
        }).blockingSubscribe();
    }

    public void intervalSchedulerNull() {
        Observable.interval(1, TimeUnit.SECONDS, null);
    }

    @Test
    public void justNull() throws Exception {
        @SuppressWarnings("rawtypes")
        Class<Observable> clazz = Observable.class;
        for (int argCount = 1; argCount < 10; argCount++) {
            for (int argNull = 1; argNull <= argCount; argNull++) {
                Class<?>[] params = new Class[argCount];
                Arrays.fill(params, Object.class);
                Object[] values = new Object[argCount];
                Arrays.fill(values, 1);
                values[argNull - 1] = null;
                Method m = clazz.getMethod("just", params);
                try {
                    m.invoke(null, values);
                    Assert.fail("No exception for argCount " + argCount + " / argNull " + argNull);
                } catch (InvocationTargetException ex) {
                    if (!(ex.getCause() instanceof NullPointerException)) {
                        Assert.fail("Unexpected exception for argCount " + argCount + " / argNull " + argNull + ": " + ex);
                    }
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void mergeIterableIteratorNull() {
        Observable.merge(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
                return null;
            }
        }, 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeIterableOneIsNull() {
        Observable.merge(Arrays.asList(just1, null), 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeDelayErrorIterableIteratorNull() {
        Observable.mergeDelayError(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
                return null;
            }
        }, 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeDelayErrorIterableOneIsNull() {
        Observable.mergeDelayError(Arrays.asList(just1, null), 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void usingObservableSupplierReturnsNull() {
        Observable.using(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, new Function<Object, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Object d) {
                return null;
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) {
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableIteratorNull() {
        Observable.zip(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
                return null;
            }
        }, new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableFunctionReturnsNull() {
        Observable.zip(Arrays.asList(just1, just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterable2IteratorNull() {
        Observable.zip(new Iterable<Observable<Object>>() {

            @Override
            public Iterator<Observable<Object>> iterator() {
                return null;
            }
        }, new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) {
                return 1;
            }
        }, true, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterable2FunctionReturnsNull() {
        Observable.zip(Arrays.asList(just1, just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) {
                return null;
            }
        }, true, 128).blockingLast();
    }

    // *************************************************************
    // Instance methods
    // *************************************************************
    @Test(expected = NullPointerException.class)
    public void bufferSupplierReturnsNull() {
        just1.buffer(1, 1, new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void bufferTimedSupplierReturnsNull() {
        just1.buffer(1L, 1L, TimeUnit.SECONDS, Schedulers.single(), new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void bufferOpenCloseCloseReturnsNull() {
        just1.buffer(just1, new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void bufferBoundarySupplierReturnsNull() {
        just1.buffer(just1, new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void collectInitialSupplierReturnsNull() {
        just1.collect(new Supplier<Object>() {

            @Override
            public Object get() {
                return null;
            }
        }, new BiConsumer<Object, Integer>() {

            @Override
            public void accept(Object a, Integer b) {
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void collectInitialCollectorNull() {
        just1.collect(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, null);
    }

    @Test(expected = NullPointerException.class)
    public void concatMapReturnsNull() {
        just1.concatMap(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void concatMapIterableReturnNull() {
        just1.concatMapIterable(new Function<Integer, Iterable<Object>>() {

            @Override
            public Iterable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void concatMapIterableIteratorNull() {
        just1.concatMapIterable(new Function<Integer, Iterable<Object>>() {

            @Override
            public Iterable<Object> apply(Integer v) {
                return new Iterable<Object>() {

                    @Override
                    public Iterator<Object> iterator() {
                        return null;
                    }
                };
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void debounceFunctionReturnsNull() {
        just1.debounce(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void delayWithFunctionReturnsNull() {
        just1.delay(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void delayBothItemSupplierReturnsNull() {
        just1.delay(just1, new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void distinctSupplierReturnsNull() {
        just1.distinct(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Supplier<Collection<Object>>() {

            @Override
            public Collection<Object> get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void distinctFunctionReturnsNull() {
        just1.distinct(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test
    public void distinctUntilChangedFunctionReturnsNull() {
        Observable.range(1, 2).distinctUntilChanged(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).test().assertResult(1);
    }

    @Test(expected = NullPointerException.class)
    public void flatMapFunctionReturnsNull() {
        just1.flatMap(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapNotificationOnNextReturnsNull() {
        just1.flatMap(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer v) {
                return null;
            }
        }, new Function<Throwable, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Throwable e) {
                return just1;
            }
        }, new Supplier<Observable<Integer>>() {

            @Override
            public Observable<Integer> get() {
                return just1;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapNotificationOnCompleteReturnsNull() {
        just1.flatMap(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer v) {
                return just1;
            }
        }, new Function<Throwable, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Throwable e) {
                return just1;
            }
        }, new Supplier<Observable<Integer>>() {

            @Override
            public Observable<Integer> get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapCombinerMapperReturnsNull() {
        just1.flatMap(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }, new BiFunction<Integer, Object, Object>() {

            @Override
            public Object apply(Integer a, Object b) {
                return 1;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapCombinerCombinerReturnsNull() {
        just1.flatMap(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer v) {
                return just1;
            }
        }, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapIterableMapperReturnsNull() {
        just1.flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapIterableMapperIteratorNull() {
        just1.flatMapIterable(new Function<Integer, Iterable<Object>>() {

            @Override
            public Iterable<Object> apply(Integer v) {
                return new Iterable<Object>() {

                    @Override
                    public Iterator<Object> iterator() {
                        return null;
                    }
                };
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapIterableMapperIterableOneNull() {
        just1.flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return Arrays.asList(1, null);
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapIterableCombinerReturnsNull() {
        just1.flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return Arrays.asList(1);
            }
        }, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    public void groupByKeyNull() {
        just1.groupBy(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void groupByValueReturnsNull() {
        just1.groupBy(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void liftReturnsNull() {
        just1.lift(new ObservableOperator<Object, Integer>() {

            @Override
            public Observer<? super Integer> apply(Observer<? super Object> observer) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void mapReturnsNull() {
        just1.map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void onErrorResumeNextFunctionReturnsNull() {
        Observable.error(new TestException()).onErrorResumeNext(new Function<Throwable, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Throwable e) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void onErrorReturnFunctionReturnsNull() {
        Observable.error(new TestException()).onErrorReturn(new Function<Throwable, Object>() {

            @Override
            public Object apply(Throwable e) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void publishFunctionReturnsNull() {
        just1.publish(new Function<Observable<Integer>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Integer> v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void reduceFunctionReturnsNull() {
        Observable.just(1, 1).reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void reduceSeedFunctionReturnsNull() {
        just1.reduce(1, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void reduceWithSeedReturnsNull() {
        just1.reduceWith(new Supplier<Object>() {

            @Override
            public Object get() {
                return null;
            }
        }, new BiFunction<Object, Integer, Object>() {

            @Override
            public Object apply(Object a, Integer b) {
                return 1;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void repeatWhenFunctionReturnsNull() {
        just1.repeatWhen(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replaySelectorReturnsNull() {
        just1.replay(new Function<Observable<Integer>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Integer> o) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replayBoundedSelectorReturnsNull() {
        just1.replay(new Function<Observable<Integer>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Integer> v) {
                return null;
            }
        }, 1, 1, TimeUnit.SECONDS).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replayTimeBoundedSelectorReturnsNull() {
        just1.replay(new Function<Observable<Integer>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Integer> v) {
                return null;
            }
        }, 1, TimeUnit.SECONDS, Schedulers.single()).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void retryWhenFunctionReturnsNull() {
        Observable.error(new TestException()).retryWhen(new Function<Observable<? extends Throwable>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<? extends Throwable> f) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void scanFunctionReturnsNull() {
        Observable.just(1, 1).scan(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void scanSeedFunctionReturnsNull() {
        just1.scan(1, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void scanSeedSupplierReturnsNull() {
        just1.scanWith(new Supplier<Object>() {

            @Override
            public Object get() {
                return null;
            }
        }, new BiFunction<Object, Integer, Object>() {

            @Override
            public Object apply(Object a, Integer b) {
                return 1;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void scanSeedSupplierFunctionReturnsNull() {
        just1.scanWith(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, new BiFunction<Object, Integer, Object>() {

            @Override
            public Object apply(Object a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void startWithIterableIteratorNull() {
        just1.startWithIterable(new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void startWithIterableOneNull() {
        just1.startWithIterable(Arrays.asList(1, null)).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void switchMapFunctionReturnsNull() {
        just1.switchMap(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void timeoutSelectorReturnsNull() {
        just1.timeout(new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void timeoutFirstItemReturnsNull() {
        Observable.just(1, 1).timeout(Observable.never(), new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void toListSupplierReturnsNull() {
        just1.toList(new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() {
                return null;
            }
        }).blockingGet();
    }

    @Test
    public void toMapValueSelectorReturnsNull() {
        just1.toMap(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void toMapMapSupplierReturnsNull() {
        just1.toMap(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Supplier<Map<Object, Object>>() {

            @Override
            public Map<Object, Object> get() {
                return null;
            }
        }).blockingGet();
    }

    @Test
    public void toMultiMapValueSelectorReturnsNullAllowed() {
        just1.toMap(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void toMultimapMapSupplierReturnsNull() {
        just1.toMultimap(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return v;
            }
        }, new Supplier<Map<Object, Collection<Object>>>() {

            @Override
            public Map<Object, Collection<Object>> get() {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void toMultimapMapCollectionSupplierReturnsNull() {
        just1.toMultimap(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        }, new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        }, new Supplier<Map<Integer, Collection<Integer>>>() {

            @Override
            public Map<Integer, Collection<Integer>> get() {
                return new HashMap<>();
            }
        }, new Function<Integer, Collection<Integer>>() {

            @Override
            public Collection<Integer> apply(Integer v) {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void windowOpenCloseCloseReturnsNull() {
        Observable.never().window(just1, new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void withLatestFromCombinerReturnsNull() {
        just1.withLatestFrom(just1, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void zipWithIterableCombinerReturnsNull() {
        just1.zipWith(Arrays.asList(1), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void zipWithIterableIteratorNull() {
        just1.zipWith(new Iterable<Object>() {

            @Override
            public Iterator<Object> iterator() {
                return null;
            }
        }, new BiFunction<Integer, Object, Object>() {

            @Override
            public Object apply(Integer a, Object b) {
                return 1;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void zipWithIterableOneIsNull() {
        Observable.just(1, 2).zipWith(Arrays.asList(1, null), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return 1;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void zipWithCombinerReturnsNull() {
        just1.zipWith(just1, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableNullTests instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.ambIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableOneIsNull() throws java.lang.Throwable {
            this.payloads.ambIterableOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.combineLatestIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestIterableOneIsNull() throws java.lang.Throwable {
            this.payloads.combineLatestIterableOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestIterableFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.combineLatestIterableFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestDelayErrorIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.combineLatestDelayErrorIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestDelayErrorIterableOneIsNull() throws java.lang.Throwable {
            this.payloads.combineLatestDelayErrorIterableOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestDelayErrorIterableFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.combineLatestDelayErrorIterableFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.concatIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableOneIsNull() throws java.lang.Throwable {
            this.payloads.concatIterableOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArrayOneIsNull() throws java.lang.Throwable {
            this.payloads.concatArrayOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.deferFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.errorFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromArrayOneIsNull() throws java.lang.Throwable {
            this.payloads.fromArrayOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallableReturnsNull() throws java.lang.Throwable {
            this.payloads.fromCallableReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFutureReturnsNull() throws java.lang.Throwable {
            this.payloads.fromFutureReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFutureTimedReturnsNull() throws java.lang.Throwable {
            this.payloads.fromFutureTimedReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.fromIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromIterableValueNull() throws java.lang.Throwable {
            this.payloads.fromIterableValueNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_generateConsumerEmitsNull() throws java.lang.Throwable {
            this.payloads.generateConsumerEmitsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_generateConsumerStateNullAllowed() throws java.lang.Throwable {
            this.payloads.generateConsumerStateNullAllowed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_generateFunctionStateNullAllowed() throws java.lang.Throwable {
            this.payloads.generateFunctionStateNullAllowed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justNull() throws java.lang.Throwable {
            this.payloads.justNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.mergeIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableOneIsNull() throws java.lang.Throwable {
            this.payloads.mergeIterableOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableOneIsNull() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorIterableOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingObservableSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.usingObservableSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.zipIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.zipIterableFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterable2IteratorNull() throws java.lang.Throwable {
            this.payloads.zipIterable2IteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterable2FunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.zipIterable2FunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.bufferSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferTimedSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.bufferTimedSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferOpenCloseCloseReturnsNull() throws java.lang.Throwable {
            this.payloads.bufferOpenCloseCloseReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bufferBoundarySupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.bufferBoundarySupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectInitialSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.collectInitialSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectInitialCollectorNull() throws java.lang.Throwable {
            this.payloads.collectInitialCollectorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapReturnsNull() throws java.lang.Throwable {
            this.payloads.concatMapReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapIterableReturnNull() throws java.lang.Throwable {
            this.payloads.concatMapIterableReturnNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMapIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.concatMapIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.debounceFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayWithFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.delayWithFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayBothItemSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.delayBothItemSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.distinctSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.distinctFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctUntilChangedFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.distinctUntilChangedFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.flatMapFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapNotificationOnNextReturnsNull() throws java.lang.Throwable {
            this.payloads.flatMapNotificationOnNextReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapNotificationOnCompleteReturnsNull() throws java.lang.Throwable {
            this.payloads.flatMapNotificationOnCompleteReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapCombinerMapperReturnsNull() throws java.lang.Throwable {
            this.payloads.flatMapCombinerMapperReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapCombinerCombinerReturnsNull() throws java.lang.Throwable {
            this.payloads.flatMapCombinerCombinerReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapIterableMapperReturnsNull() throws java.lang.Throwable {
            this.payloads.flatMapIterableMapperReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapIterableMapperIteratorNull() throws java.lang.Throwable {
            this.payloads.flatMapIterableMapperIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapIterableMapperIterableOneNull() throws java.lang.Throwable {
            this.payloads.flatMapIterableMapperIterableOneNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapIterableCombinerReturnsNull() throws java.lang.Throwable {
            this.payloads.flatMapIterableCombinerReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupByValueReturnsNull() throws java.lang.Throwable {
            this.payloads.groupByValueReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_liftReturnsNull() throws java.lang.Throwable {
            this.payloads.liftReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapReturnsNull() throws java.lang.Throwable {
            this.payloads.mapReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNextFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.onErrorResumeNextFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.onErrorReturnFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.publishFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.reduceFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceSeedFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.reduceSeedFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithSeedReturnsNull() throws java.lang.Throwable {
            this.payloads.reduceWithSeedReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatWhenFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.repeatWhenFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySelectorReturnsNull() throws java.lang.Throwable {
            this.payloads.replaySelectorReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replayBoundedSelectorReturnsNull() throws java.lang.Throwable {
            this.payloads.replayBoundedSelectorReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replayTimeBoundedSelectorReturnsNull() throws java.lang.Throwable {
            this.payloads.replayTimeBoundedSelectorReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryWhenFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.retryWhenFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.scanFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanSeedFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.scanSeedFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanSeedSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.scanSeedSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanSeedSupplierFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.scanSeedSupplierFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.startWithIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithIterableOneNull() throws java.lang.Throwable {
            this.payloads.startWithIterableOneNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchMapFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.switchMapFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSelectorReturnsNull() throws java.lang.Throwable {
            this.payloads.timeoutSelectorReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutFirstItemReturnsNull() throws java.lang.Throwable {
            this.payloads.timeoutFirstItemReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toListSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.toListSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapValueSelectorReturnsNull() throws java.lang.Throwable {
            this.payloads.toMapValueSelectorReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapMapSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.toMapMapSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultiMapValueSelectorReturnsNullAllowed() throws java.lang.Throwable {
            this.payloads.toMultiMapValueSelectorReturnsNullAllowed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapMapSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.toMultimapMapSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapMapCollectionSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.toMultimapMapCollectionSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowOpenCloseCloseReturnsNull() throws java.lang.Throwable {
            this.payloads.windowOpenCloseCloseReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withLatestFromCombinerReturnsNull() throws java.lang.Throwable {
            this.payloads.withLatestFromCombinerReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithIterableCombinerReturnsNull() throws java.lang.Throwable {
            this.payloads.zipWithIterableCombinerReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.zipWithIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithIterableOneIsNull() throws java.lang.Throwable {
            this.payloads.zipWithIterableOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithCombinerReturnsNull() throws java.lang.Throwable {
            this.payloads.zipWithCombinerReturnsNull.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableNullTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableNullTests> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableNullTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableNullTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableNullTests();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableNullTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableNullTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableNullTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement ambIterableIteratorNull;

            public org.junit.runners.model.Statement ambIterableOneIsNull;

            public org.junit.runners.model.Statement combineLatestIterableIteratorNull;

            public org.junit.runners.model.Statement combineLatestIterableOneIsNull;

            public org.junit.runners.model.Statement combineLatestIterableFunctionReturnsNull;

            public org.junit.runners.model.Statement combineLatestDelayErrorIterableIteratorNull;

            public org.junit.runners.model.Statement combineLatestDelayErrorIterableOneIsNull;

            public org.junit.runners.model.Statement combineLatestDelayErrorIterableFunctionReturnsNull;

            public org.junit.runners.model.Statement concatIterableIteratorNull;

            public org.junit.runners.model.Statement concatIterableOneIsNull;

            public org.junit.runners.model.Statement concatArrayOneIsNull;

            public org.junit.runners.model.Statement deferFunctionReturnsNull;

            public org.junit.runners.model.Statement errorFunctionReturnsNull;

            public org.junit.runners.model.Statement fromArrayOneIsNull;

            public org.junit.runners.model.Statement fromCallableReturnsNull;

            public org.junit.runners.model.Statement fromFutureReturnsNull;

            public org.junit.runners.model.Statement fromFutureTimedReturnsNull;

            public org.junit.runners.model.Statement fromIterableIteratorNull;

            public org.junit.runners.model.Statement fromIterableValueNull;

            public org.junit.runners.model.Statement generateConsumerEmitsNull;

            public org.junit.runners.model.Statement generateConsumerStateNullAllowed;

            public org.junit.runners.model.Statement generateFunctionStateNullAllowed;

            public org.junit.runners.model.Statement justNull;

            public org.junit.runners.model.Statement mergeIterableIteratorNull;

            public org.junit.runners.model.Statement mergeIterableOneIsNull;

            public org.junit.runners.model.Statement mergeDelayErrorIterableIteratorNull;

            public org.junit.runners.model.Statement mergeDelayErrorIterableOneIsNull;

            public org.junit.runners.model.Statement usingObservableSupplierReturnsNull;

            public org.junit.runners.model.Statement zipIterableIteratorNull;

            public org.junit.runners.model.Statement zipIterableFunctionReturnsNull;

            public org.junit.runners.model.Statement zipIterable2IteratorNull;

            public org.junit.runners.model.Statement zipIterable2FunctionReturnsNull;

            public org.junit.runners.model.Statement bufferSupplierReturnsNull;

            public org.junit.runners.model.Statement bufferTimedSupplierReturnsNull;

            public org.junit.runners.model.Statement bufferOpenCloseCloseReturnsNull;

            public org.junit.runners.model.Statement bufferBoundarySupplierReturnsNull;

            public org.junit.runners.model.Statement collectInitialSupplierReturnsNull;

            public org.junit.runners.model.Statement collectInitialCollectorNull;

            public org.junit.runners.model.Statement concatMapReturnsNull;

            public org.junit.runners.model.Statement concatMapIterableReturnNull;

            public org.junit.runners.model.Statement concatMapIterableIteratorNull;

            public org.junit.runners.model.Statement debounceFunctionReturnsNull;

            public org.junit.runners.model.Statement delayWithFunctionReturnsNull;

            public org.junit.runners.model.Statement delayBothItemSupplierReturnsNull;

            public org.junit.runners.model.Statement distinctSupplierReturnsNull;

            public org.junit.runners.model.Statement distinctFunctionReturnsNull;

            public org.junit.runners.model.Statement distinctUntilChangedFunctionReturnsNull;

            public org.junit.runners.model.Statement flatMapFunctionReturnsNull;

            public org.junit.runners.model.Statement flatMapNotificationOnNextReturnsNull;

            public org.junit.runners.model.Statement flatMapNotificationOnCompleteReturnsNull;

            public org.junit.runners.model.Statement flatMapCombinerMapperReturnsNull;

            public org.junit.runners.model.Statement flatMapCombinerCombinerReturnsNull;

            public org.junit.runners.model.Statement flatMapIterableMapperReturnsNull;

            public org.junit.runners.model.Statement flatMapIterableMapperIteratorNull;

            public org.junit.runners.model.Statement flatMapIterableMapperIterableOneNull;

            public org.junit.runners.model.Statement flatMapIterableCombinerReturnsNull;

            public org.junit.runners.model.Statement groupByValueReturnsNull;

            public org.junit.runners.model.Statement liftReturnsNull;

            public org.junit.runners.model.Statement mapReturnsNull;

            public org.junit.runners.model.Statement onErrorResumeNextFunctionReturnsNull;

            public org.junit.runners.model.Statement onErrorReturnFunctionReturnsNull;

            public org.junit.runners.model.Statement publishFunctionReturnsNull;

            public org.junit.runners.model.Statement reduceFunctionReturnsNull;

            public org.junit.runners.model.Statement reduceSeedFunctionReturnsNull;

            public org.junit.runners.model.Statement reduceWithSeedReturnsNull;

            public org.junit.runners.model.Statement repeatWhenFunctionReturnsNull;

            public org.junit.runners.model.Statement replaySelectorReturnsNull;

            public org.junit.runners.model.Statement replayBoundedSelectorReturnsNull;

            public org.junit.runners.model.Statement replayTimeBoundedSelectorReturnsNull;

            public org.junit.runners.model.Statement retryWhenFunctionReturnsNull;

            public org.junit.runners.model.Statement scanFunctionReturnsNull;

            public org.junit.runners.model.Statement scanSeedFunctionReturnsNull;

            public org.junit.runners.model.Statement scanSeedSupplierReturnsNull;

            public org.junit.runners.model.Statement scanSeedSupplierFunctionReturnsNull;

            public org.junit.runners.model.Statement startWithIterableIteratorNull;

            public org.junit.runners.model.Statement startWithIterableOneNull;

            public org.junit.runners.model.Statement switchMapFunctionReturnsNull;

            public org.junit.runners.model.Statement timeoutSelectorReturnsNull;

            public org.junit.runners.model.Statement timeoutFirstItemReturnsNull;

            public org.junit.runners.model.Statement toListSupplierReturnsNull;

            public org.junit.runners.model.Statement toMapValueSelectorReturnsNull;

            public org.junit.runners.model.Statement toMapMapSupplierReturnsNull;

            public org.junit.runners.model.Statement toMultiMapValueSelectorReturnsNullAllowed;

            public org.junit.runners.model.Statement toMultimapMapSupplierReturnsNull;

            public org.junit.runners.model.Statement toMultimapMapCollectionSupplierReturnsNull;

            public org.junit.runners.model.Statement windowOpenCloseCloseReturnsNull;

            public org.junit.runners.model.Statement withLatestFromCombinerReturnsNull;

            public org.junit.runners.model.Statement zipWithIterableCombinerReturnsNull;

            public org.junit.runners.model.Statement zipWithIterableIteratorNull;

            public org.junit.runners.model.Statement zipWithIterableOneIsNull;

            public org.junit.runners.model.Statement zipWithCombinerReturnsNull;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.ambIterableIteratorNull = _ClassStatement.forPayload(ObservableNullTests::ambIterableIteratorNull, "ambIterableIteratorNull", this);
            this.payloads.ambIterableOneIsNull = _ClassStatement.forPayload(ObservableNullTests::ambIterableOneIsNull, "ambIterableOneIsNull", this);
            this.payloads.combineLatestIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::combineLatestIterableIteratorNull, java.lang.NullPointerException.class), "combineLatestIterableIteratorNull", this);
            this.payloads.combineLatestIterableOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::combineLatestIterableOneIsNull, java.lang.NullPointerException.class), "combineLatestIterableOneIsNull", this);
            this.payloads.combineLatestIterableFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::combineLatestIterableFunctionReturnsNull, java.lang.NullPointerException.class), "combineLatestIterableFunctionReturnsNull", this);
            this.payloads.combineLatestDelayErrorIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::combineLatestDelayErrorIterableIteratorNull, java.lang.NullPointerException.class), "combineLatestDelayErrorIterableIteratorNull", this);
            this.payloads.combineLatestDelayErrorIterableOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::combineLatestDelayErrorIterableOneIsNull, java.lang.NullPointerException.class), "combineLatestDelayErrorIterableOneIsNull", this);
            this.payloads.combineLatestDelayErrorIterableFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::combineLatestDelayErrorIterableFunctionReturnsNull, java.lang.NullPointerException.class), "combineLatestDelayErrorIterableFunctionReturnsNull", this);
            this.payloads.concatIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::concatIterableIteratorNull, java.lang.NullPointerException.class), "concatIterableIteratorNull", this);
            this.payloads.concatIterableOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::concatIterableOneIsNull, java.lang.NullPointerException.class), "concatIterableOneIsNull", this);
            this.payloads.concatArrayOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::concatArrayOneIsNull, java.lang.NullPointerException.class), "concatArrayOneIsNull", this);
            this.payloads.deferFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::deferFunctionReturnsNull, java.lang.NullPointerException.class), "deferFunctionReturnsNull", this);
            this.payloads.errorFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::errorFunctionReturnsNull, java.lang.NullPointerException.class), "errorFunctionReturnsNull", this);
            this.payloads.fromArrayOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::fromArrayOneIsNull, java.lang.NullPointerException.class), "fromArrayOneIsNull", this);
            this.payloads.fromCallableReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::fromCallableReturnsNull, java.lang.NullPointerException.class), "fromCallableReturnsNull", this);
            this.payloads.fromFutureReturnsNull = _ClassStatement.forPayload(ObservableNullTests::fromFutureReturnsNull, "fromFutureReturnsNull", this);
            this.payloads.fromFutureTimedReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::fromFutureTimedReturnsNull, java.lang.NullPointerException.class), "fromFutureTimedReturnsNull", this);
            this.payloads.fromIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::fromIterableIteratorNull, java.lang.NullPointerException.class), "fromIterableIteratorNull", this);
            this.payloads.fromIterableValueNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::fromIterableValueNull, java.lang.NullPointerException.class), "fromIterableValueNull", this);
            this.payloads.generateConsumerEmitsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::generateConsumerEmitsNull, java.lang.NullPointerException.class), "generateConsumerEmitsNull", this);
            this.payloads.generateConsumerStateNullAllowed = _ClassStatement.forPayload(ObservableNullTests::generateConsumerStateNullAllowed, "generateConsumerStateNullAllowed", this);
            this.payloads.generateFunctionStateNullAllowed = _ClassStatement.forPayload(ObservableNullTests::generateFunctionStateNullAllowed, "generateFunctionStateNullAllowed", this);
            this.payloads.justNull = _ClassStatement.forPayload(ObservableNullTests::justNull, "justNull", this);
            this.payloads.mergeIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::mergeIterableIteratorNull, java.lang.NullPointerException.class), "mergeIterableIteratorNull", this);
            this.payloads.mergeIterableOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::mergeIterableOneIsNull, java.lang.NullPointerException.class), "mergeIterableOneIsNull", this);
            this.payloads.mergeDelayErrorIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::mergeDelayErrorIterableIteratorNull, java.lang.NullPointerException.class), "mergeDelayErrorIterableIteratorNull", this);
            this.payloads.mergeDelayErrorIterableOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::mergeDelayErrorIterableOneIsNull, java.lang.NullPointerException.class), "mergeDelayErrorIterableOneIsNull", this);
            this.payloads.usingObservableSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::usingObservableSupplierReturnsNull, java.lang.NullPointerException.class), "usingObservableSupplierReturnsNull", this);
            this.payloads.zipIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::zipIterableIteratorNull, java.lang.NullPointerException.class), "zipIterableIteratorNull", this);
            this.payloads.zipIterableFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::zipIterableFunctionReturnsNull, java.lang.NullPointerException.class), "zipIterableFunctionReturnsNull", this);
            this.payloads.zipIterable2IteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::zipIterable2IteratorNull, java.lang.NullPointerException.class), "zipIterable2IteratorNull", this);
            this.payloads.zipIterable2FunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::zipIterable2FunctionReturnsNull, java.lang.NullPointerException.class), "zipIterable2FunctionReturnsNull", this);
            this.payloads.bufferSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::bufferSupplierReturnsNull, java.lang.NullPointerException.class), "bufferSupplierReturnsNull", this);
            this.payloads.bufferTimedSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::bufferTimedSupplierReturnsNull, java.lang.NullPointerException.class), "bufferTimedSupplierReturnsNull", this);
            this.payloads.bufferOpenCloseCloseReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::bufferOpenCloseCloseReturnsNull, java.lang.NullPointerException.class), "bufferOpenCloseCloseReturnsNull", this);
            this.payloads.bufferBoundarySupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::bufferBoundarySupplierReturnsNull, java.lang.NullPointerException.class), "bufferBoundarySupplierReturnsNull", this);
            this.payloads.collectInitialSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::collectInitialSupplierReturnsNull, java.lang.NullPointerException.class), "collectInitialSupplierReturnsNull", this);
            this.payloads.collectInitialCollectorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::collectInitialCollectorNull, java.lang.NullPointerException.class), "collectInitialCollectorNull", this);
            this.payloads.concatMapReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::concatMapReturnsNull, java.lang.NullPointerException.class), "concatMapReturnsNull", this);
            this.payloads.concatMapIterableReturnNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::concatMapIterableReturnNull, java.lang.NullPointerException.class), "concatMapIterableReturnNull", this);
            this.payloads.concatMapIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::concatMapIterableIteratorNull, java.lang.NullPointerException.class), "concatMapIterableIteratorNull", this);
            this.payloads.debounceFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::debounceFunctionReturnsNull, java.lang.NullPointerException.class), "debounceFunctionReturnsNull", this);
            this.payloads.delayWithFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::delayWithFunctionReturnsNull, java.lang.NullPointerException.class), "delayWithFunctionReturnsNull", this);
            this.payloads.delayBothItemSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::delayBothItemSupplierReturnsNull, java.lang.NullPointerException.class), "delayBothItemSupplierReturnsNull", this);
            this.payloads.distinctSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::distinctSupplierReturnsNull, java.lang.NullPointerException.class), "distinctSupplierReturnsNull", this);
            this.payloads.distinctFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::distinctFunctionReturnsNull, java.lang.NullPointerException.class), "distinctFunctionReturnsNull", this);
            this.payloads.distinctUntilChangedFunctionReturnsNull = _ClassStatement.forPayload(ObservableNullTests::distinctUntilChangedFunctionReturnsNull, "distinctUntilChangedFunctionReturnsNull", this);
            this.payloads.flatMapFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::flatMapFunctionReturnsNull, java.lang.NullPointerException.class), "flatMapFunctionReturnsNull", this);
            this.payloads.flatMapNotificationOnNextReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::flatMapNotificationOnNextReturnsNull, java.lang.NullPointerException.class), "flatMapNotificationOnNextReturnsNull", this);
            this.payloads.flatMapNotificationOnCompleteReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::flatMapNotificationOnCompleteReturnsNull, java.lang.NullPointerException.class), "flatMapNotificationOnCompleteReturnsNull", this);
            this.payloads.flatMapCombinerMapperReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::flatMapCombinerMapperReturnsNull, java.lang.NullPointerException.class), "flatMapCombinerMapperReturnsNull", this);
            this.payloads.flatMapCombinerCombinerReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::flatMapCombinerCombinerReturnsNull, java.lang.NullPointerException.class), "flatMapCombinerCombinerReturnsNull", this);
            this.payloads.flatMapIterableMapperReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::flatMapIterableMapperReturnsNull, java.lang.NullPointerException.class), "flatMapIterableMapperReturnsNull", this);
            this.payloads.flatMapIterableMapperIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::flatMapIterableMapperIteratorNull, java.lang.NullPointerException.class), "flatMapIterableMapperIteratorNull", this);
            this.payloads.flatMapIterableMapperIterableOneNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::flatMapIterableMapperIterableOneNull, java.lang.NullPointerException.class), "flatMapIterableMapperIterableOneNull", this);
            this.payloads.flatMapIterableCombinerReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::flatMapIterableCombinerReturnsNull, java.lang.NullPointerException.class), "flatMapIterableCombinerReturnsNull", this);
            this.payloads.groupByValueReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::groupByValueReturnsNull, java.lang.NullPointerException.class), "groupByValueReturnsNull", this);
            this.payloads.liftReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::liftReturnsNull, java.lang.NullPointerException.class), "liftReturnsNull", this);
            this.payloads.mapReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::mapReturnsNull, java.lang.NullPointerException.class), "mapReturnsNull", this);
            this.payloads.onErrorResumeNextFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::onErrorResumeNextFunctionReturnsNull, java.lang.NullPointerException.class), "onErrorResumeNextFunctionReturnsNull", this);
            this.payloads.onErrorReturnFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::onErrorReturnFunctionReturnsNull, java.lang.NullPointerException.class), "onErrorReturnFunctionReturnsNull", this);
            this.payloads.publishFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::publishFunctionReturnsNull, java.lang.NullPointerException.class), "publishFunctionReturnsNull", this);
            this.payloads.reduceFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::reduceFunctionReturnsNull, java.lang.NullPointerException.class), "reduceFunctionReturnsNull", this);
            this.payloads.reduceSeedFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::reduceSeedFunctionReturnsNull, java.lang.NullPointerException.class), "reduceSeedFunctionReturnsNull", this);
            this.payloads.reduceWithSeedReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::reduceWithSeedReturnsNull, java.lang.NullPointerException.class), "reduceWithSeedReturnsNull", this);
            this.payloads.repeatWhenFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::repeatWhenFunctionReturnsNull, java.lang.NullPointerException.class), "repeatWhenFunctionReturnsNull", this);
            this.payloads.replaySelectorReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::replaySelectorReturnsNull, java.lang.NullPointerException.class), "replaySelectorReturnsNull", this);
            this.payloads.replayBoundedSelectorReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::replayBoundedSelectorReturnsNull, java.lang.NullPointerException.class), "replayBoundedSelectorReturnsNull", this);
            this.payloads.replayTimeBoundedSelectorReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::replayTimeBoundedSelectorReturnsNull, java.lang.NullPointerException.class), "replayTimeBoundedSelectorReturnsNull", this);
            this.payloads.retryWhenFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::retryWhenFunctionReturnsNull, java.lang.NullPointerException.class), "retryWhenFunctionReturnsNull", this);
            this.payloads.scanFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::scanFunctionReturnsNull, java.lang.NullPointerException.class), "scanFunctionReturnsNull", this);
            this.payloads.scanSeedFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::scanSeedFunctionReturnsNull, java.lang.NullPointerException.class), "scanSeedFunctionReturnsNull", this);
            this.payloads.scanSeedSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::scanSeedSupplierReturnsNull, java.lang.NullPointerException.class), "scanSeedSupplierReturnsNull", this);
            this.payloads.scanSeedSupplierFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::scanSeedSupplierFunctionReturnsNull, java.lang.NullPointerException.class), "scanSeedSupplierFunctionReturnsNull", this);
            this.payloads.startWithIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::startWithIterableIteratorNull, java.lang.NullPointerException.class), "startWithIterableIteratorNull", this);
            this.payloads.startWithIterableOneNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::startWithIterableOneNull, java.lang.NullPointerException.class), "startWithIterableOneNull", this);
            this.payloads.switchMapFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::switchMapFunctionReturnsNull, java.lang.NullPointerException.class), "switchMapFunctionReturnsNull", this);
            this.payloads.timeoutSelectorReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::timeoutSelectorReturnsNull, java.lang.NullPointerException.class), "timeoutSelectorReturnsNull", this);
            this.payloads.timeoutFirstItemReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::timeoutFirstItemReturnsNull, java.lang.NullPointerException.class), "timeoutFirstItemReturnsNull", this);
            this.payloads.toListSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::toListSupplierReturnsNull, java.lang.NullPointerException.class), "toListSupplierReturnsNull", this);
            this.payloads.toMapValueSelectorReturnsNull = _ClassStatement.forPayload(ObservableNullTests::toMapValueSelectorReturnsNull, "toMapValueSelectorReturnsNull", this);
            this.payloads.toMapMapSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::toMapMapSupplierReturnsNull, java.lang.NullPointerException.class), "toMapMapSupplierReturnsNull", this);
            this.payloads.toMultiMapValueSelectorReturnsNullAllowed = _ClassStatement.forPayload(ObservableNullTests::toMultiMapValueSelectorReturnsNullAllowed, "toMultiMapValueSelectorReturnsNullAllowed", this);
            this.payloads.toMultimapMapSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::toMultimapMapSupplierReturnsNull, java.lang.NullPointerException.class), "toMultimapMapSupplierReturnsNull", this);
            this.payloads.toMultimapMapCollectionSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::toMultimapMapCollectionSupplierReturnsNull, java.lang.NullPointerException.class), "toMultimapMapCollectionSupplierReturnsNull", this);
            this.payloads.windowOpenCloseCloseReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::windowOpenCloseCloseReturnsNull, java.lang.NullPointerException.class), "windowOpenCloseCloseReturnsNull", this);
            this.payloads.withLatestFromCombinerReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::withLatestFromCombinerReturnsNull, java.lang.NullPointerException.class), "withLatestFromCombinerReturnsNull", this);
            this.payloads.zipWithIterableCombinerReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::zipWithIterableCombinerReturnsNull, java.lang.NullPointerException.class), "zipWithIterableCombinerReturnsNull", this);
            this.payloads.zipWithIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::zipWithIterableIteratorNull, java.lang.NullPointerException.class), "zipWithIterableIteratorNull", this);
            this.payloads.zipWithIterableOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::zipWithIterableOneIsNull, java.lang.NullPointerException.class), "zipWithIterableOneIsNull", this);
            this.payloads.zipWithCombinerReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableNullTests::zipWithCombinerReturnsNull, java.lang.NullPointerException.class), "zipWithCombinerReturnsNull", this);
        }
    }
}
