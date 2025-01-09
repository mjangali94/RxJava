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
package io.reactivex.rxjava3.flowable;

import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

/**
 * Verifies the operators handle null values properly by emitting/throwing NullPointerExceptions.
 */
public class FlowableNullTests extends RxJavaTest {

    Flowable<Integer> just1 = Flowable.just(1);

    // ***********************************************************
    // Static methods
    // ***********************************************************
    @Test(expected = NullPointerException.class)
    public void ambVarargsOneIsNull() {
        Flowable.ambArray(Flowable.never(), null).blockingLast();
    }

    @Test
    public void ambIterableIteratorNull() {
        Flowable.amb(new Iterable<Publisher<Object>>() {

            @Override
            public Iterator<Publisher<Object>> iterator() {
                return null;
            }
        }).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambIterableOneIsNull() {
        Flowable.amb(Arrays.asList(Flowable.never(), null)).test().assertError(NullPointerException.class);
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestIterableIteratorNull() {
        Flowable.combineLatestDelayError(new Iterable<Publisher<Object>>() {

            @Override
            public Iterator<Publisher<Object>> iterator() {
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
    public void combineLatestIterableOneIsNull() {
        Flowable.combineLatestDelayError(Arrays.asList(Flowable.never(), null), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestIterableFunctionReturnsNull() {
        Flowable.combineLatestDelayError(Arrays.asList(just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void concatIterableIteratorNull() {
        Flowable.concat(new Iterable<Publisher<Object>>() {

            @Override
            public Iterator<Publisher<Object>> iterator() {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void concatIterableOneIsNull() {
        Flowable.concat(Arrays.asList(just1, null)).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void concatArrayOneIsNull() {
        Flowable.concatArray(just1, null).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void deferFunctionReturnsNull() {
        Flowable.defer(new Supplier<Publisher<Object>>() {

            @Override
            public Publisher<Object> get() {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void errorFunctionReturnsNull() {
        Flowable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void fromArrayOneIsNull() {
        Flowable.fromArray(1, null).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void fromCallableReturnsNull() {
        Flowable.fromCallable(new Callable<Object>() {

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
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.fromFuture(f).subscribe(ts);
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(NullPointerException.class);
    }

    @Test(expected = NullPointerException.class)
    public void fromFutureTimedReturnsNull() {
        FutureTask<Object> f = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
        f.run();
        Flowable.fromFuture(f, 1, TimeUnit.SECONDS).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void fromIterableIteratorNull() {
        Flowable.fromIterable(new Iterable<Object>() {

            @Override
            public Iterator<Object> iterator() {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void fromIterableValueNull() {
        Flowable.fromIterable(Arrays.asList(1, null)).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void generateConsumerEmitsNull() {
        Flowable.generate(new Consumer<Emitter<Object>>() {

            @Override
            public void accept(Emitter<Object> s) {
                s.onNext(null);
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void generateStateConsumerInitialStateNull() {
        BiConsumer<Integer, Emitter<Integer>> generator = new BiConsumer<Integer, Emitter<Integer>>() {

            @Override
            public void accept(Integer s, Emitter<Integer> o) {
                o.onNext(1);
            }
        };
        Flowable.generate(null, generator);
    }

    @Test(expected = NullPointerException.class)
    public void generateStateFunctionInitialStateNull() {
        Flowable.generate(null, new BiFunction<Object, Emitter<Object>, Object>() {

            @Override
            public Object apply(Object s, Emitter<Object> o) {
                o.onNext(1);
                return s;
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void generateStateConsumerNull() {
        Flowable.generate(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, (BiConsumer<Integer, Emitter<Object>>) null);
    }

    @Test
    public void generateConsumerStateNullAllowed() {
        BiConsumer<Integer, Emitter<Integer>> generator = new BiConsumer<Integer, Emitter<Integer>>() {

            @Override
            public void accept(Integer s, Emitter<Integer> o) {
                o.onComplete();
            }
        };
        Flowable.generate(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return null;
            }
        }, generator).blockingSubscribe();
    }

    @Test
    public void generateFunctionStateNullAllowed() {
        Flowable.generate(new Supplier<Object>() {

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

    @Test
    public void justNull() throws Exception {
        @SuppressWarnings("rawtypes")
        Class<Flowable> clazz = Flowable.class;
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
        Flowable.merge(new Iterable<Publisher<Object>>() {

            @Override
            public Iterator<Publisher<Object>> iterator() {
                return null;
            }
        }, 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeIterableOneIsNull() {
        Flowable.merge(Arrays.asList(just1, null), 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeArrayOneIsNull() {
        Flowable.mergeArray(128, 128, just1, null).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeDelayErrorIterableIteratorNull() {
        Flowable.mergeDelayError(new Iterable<Publisher<Object>>() {

            @Override
            public Iterator<Publisher<Object>> iterator() {
                return null;
            }
        }, 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeDelayErrorIterableOneIsNull() {
        Flowable.mergeDelayError(Arrays.asList(just1, null), 128, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void mergeDelayErrorArrayOneIsNull() {
        Flowable.mergeArrayDelayError(128, 128, just1, null).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void usingFlowableSupplierReturnsNull() {
        Flowable.using(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, new Function<Object, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Object d) {
                return null;
            }
        }, Functions.emptyConsumer()).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableIteratorNull() {
        Flowable.zip(new Iterable<Publisher<Object>>() {

            @Override
            public Iterator<Publisher<Object>> iterator() {
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
        Flowable.zip(Arrays.asList(just1, just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) {
                return null;
            }
        }).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterable2Null() {
        Flowable.zip((Iterable<Publisher<Object>>) null, new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) {
                return 1;
            }
        }, true, 128);
    }

    @Test(expected = NullPointerException.class)
    public void zipIterable2IteratorNull() {
        Flowable.zip(new Iterable<Publisher<Object>>() {

            @Override
            public Iterator<Publisher<Object>> iterator() {
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
        Flowable.zip(Arrays.asList(just1, just1), new Function<Object[], Object>() {

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
        just1.buffer(just1, new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
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
    public void concatMapReturnsNull() {
        just1.concatMap(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
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
        just1.debounce(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void delayWithFunctionReturnsNull() {
        just1.delay(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void delayBothItemSupplierReturnsNull() {
        just1.delay(just1, new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
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
        Flowable.range(1, 2).distinctUntilChanged(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return null;
            }
        }).test().assertResult(1);
    }

    @Test(expected = NullPointerException.class)
    public void flatMapFunctionReturnsNull() {
        just1.flatMap(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapNotificationOnNextReturnsNull() {
        just1.flatMap(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) {
                return null;
            }
        }, new Function<Throwable, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Throwable e) {
                return just1;
            }
        }, new Supplier<Publisher<Integer>>() {

            @Override
            public Publisher<Integer> get() {
                return just1;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapNotificationOnCompleteReturnsNull() {
        just1.flatMap(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) {
                return just1;
            }
        }, new Function<Throwable, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Throwable e) {
                return just1;
            }
        }, new Supplier<Publisher<Integer>>() {

            @Override
            public Publisher<Integer> get() {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void flatMapCombinerMapperReturnsNull() {
        just1.flatMap(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
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
        just1.flatMap(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) {
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
        just1.flatMapIterable(new Function<Integer, Iterable<Object>>() {

            @Override
            public Iterable<Object> apply(Integer v) {
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
        just1.lift(new FlowableOperator<Object, Integer>() {

            @Override
            public Subscriber<? super Integer> apply(Subscriber<? super Object> s) {
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

    @Test
    public void onErrorResumeNextFunctionReturnsNull() {
        try {
            Flowable.error(new TestException()).onErrorResumeNext(new Function<Throwable, Publisher<Object>>() {

                @Override
                public Publisher<Object> apply(Throwable e) {
                    return null;
                }
            }).blockingSubscribe();
            fail("Should have thrown");
        } catch (CompositeException ex) {
            List<Throwable> errors = ex.getExceptions();
            TestHelper.assertError(errors, 0, TestException.class);
            TestHelper.assertError(errors, 1, NullPointerException.class);
            assertEquals(2, errors.size());
        }
    }

    @Test
    public void onErrorReturnFunctionReturnsNull() {
        try {
            Flowable.error(new TestException()).onErrorReturn(new Function<Throwable, Object>() {

                @Override
                public Object apply(Throwable e) {
                    return null;
                }
            }).blockingSubscribe();
            fail("Should have thrown");
        } catch (CompositeException ex) {
            List<Throwable> errors = TestHelper.compositeList(ex);
            TestHelper.assertError(errors, 0, TestException.class);
            TestHelper.assertError(errors, 1, NullPointerException.class, "The valueSupplier returned a null value");
        }
    }

    @Test(expected = NullPointerException.class)
    public void publishFunctionReturnsNull() {
        just1.publish(new Function<Flowable<Integer>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Integer> v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void reduceFunctionReturnsNull() {
        Flowable.just(1, 1).reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return null;
            }
        }).toFlowable().blockingSubscribe();
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
    public void reduceWithSeedNull() {
        just1.reduceWith(null, new BiFunction<Object, Integer, Object>() {

            @Override
            public Object apply(Object a, Integer b) {
                return 1;
            }
        });
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
        just1.repeatWhen(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replaySelectorNull() {
        just1.replay((Function<Flowable<Integer>, Flowable<Integer>>) null);
    }

    @Test(expected = NullPointerException.class)
    public void replaySelectorReturnsNull() {
        just1.replay(new Function<Flowable<Integer>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Integer> f) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replayBoundedSelectorReturnsNull() {
        just1.replay(new Function<Flowable<Integer>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Integer> v) {
                return null;
            }
        }, 1, 1, TimeUnit.SECONDS).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replayTimeBoundedSelectorReturnsNull() {
        just1.replay(new Function<Flowable<Integer>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Integer> v) {
                return null;
            }
        }, 1, TimeUnit.SECONDS, Schedulers.single()).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void retryWhenFunctionReturnsNull() {
        Flowable.error(new TestException()).retryWhen(new Function<Flowable<? extends Throwable>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<? extends Throwable> f) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void scanFunctionReturnsNull() {
        Flowable.just(1, 1).scan(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void scanSeedNull() {
        just1.scan(null, new BiFunction<Object, Integer, Object>() {

            @Override
            public Object apply(Object a, Integer b) {
                return 1;
            }
        });
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
    public void startWithArrayOneNull() {
        just1.startWithArray(1, null).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void switchMapFunctionReturnsNull() {
        just1.switchMap(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void timeoutSelectorReturnsNull() {
        just1.timeout(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void timeoutSelectorOtherNull() {
        just1.timeout(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) {
                return just1;
            }
        }, null);
    }

    @Test(expected = NullPointerException.class)
    public void timeoutFirstItemReturnsNull() {
        just1.timeout(Flowable.never(), new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void timestampUnitNull() {
        just1.timestamp(null, Schedulers.single());
    }

    @Test(expected = NullPointerException.class)
    public void timestampSchedulerNull() {
        just1.timestamp(TimeUnit.SECONDS, null);
    }

    @Test(expected = NullPointerException.class)
    public void toListSupplierReturnsNull() {
        just1.toList(new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() {
                return null;
            }
        }).toFlowable().blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void toListSupplierReturnsNullSingle() {
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
    public void windowOpenCloseOpenNull() {
        just1.window(null, new Function<Object, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Object v) {
                return just1;
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void windowOpenCloseCloseReturnsNull() {
        Flowable.never().window(just1, new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) {
                return null;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void withLatestFromOtherNull() {
        just1.withLatestFrom(null, new BiFunction<Integer, Object, Object>() {

            @Override
            public Object apply(Integer a, Object b) {
                return 1;
            }
        });
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
    public void zipWithIterableNull() {
        just1.zipWith((Iterable<Integer>) null, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return 1;
            }
        });
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
        Flowable.just(1, 2).zipWith(Arrays.asList(1, null), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return 1;
            }
        }).blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void zipWithPublisherNull() {
        just1.zipWith((Publisher<Integer>) null, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) {
                return 1;
            }
        });
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

    // *********************************************
    // Subject null tests
    // *********************************************
    @Test(expected = NullPointerException.class)
    public void asyncSubjectOnNextNull() {
        FlowableProcessor<Integer> processor = AsyncProcessor.create();
        processor.onNext(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void asyncSubjectOnErrorNull() {
        FlowableProcessor<Integer> processor = AsyncProcessor.create();
        processor.onError(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void behaviorSubjectOnNextNull() {
        FlowableProcessor<Integer> processor = BehaviorProcessor.create();
        processor.onNext(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void behaviorSubjectOnErrorNull() {
        FlowableProcessor<Integer> processor = BehaviorProcessor.create();
        processor.onError(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void publishSubjectOnNextNull() {
        FlowableProcessor<Integer> processor = PublishProcessor.create();
        processor.onNext(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void publishSubjectOnErrorNull() {
        FlowableProcessor<Integer> processor = PublishProcessor.create();
        processor.onError(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replaycSubjectOnNextNull() {
        FlowableProcessor<Integer> processor = ReplayProcessor.create();
        processor.onNext(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void replaySubjectOnErrorNull() {
        FlowableProcessor<Integer> processor = ReplayProcessor.create();
        processor.onError(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void serializedcSubjectOnNextNull() {
        FlowableProcessor<Integer> processor = PublishProcessor.<Integer>create().toSerialized();
        processor.onNext(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void serializedSubjectOnErrorNull() {
        FlowableProcessor<Integer> processor = PublishProcessor.<Integer>create().toSerialized();
        processor.onError(null);
        processor.blockingSubscribe();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestDelayErrorIterableFunctionReturnsNull() {
        Flowable.combineLatestDelayError(Arrays.asList(just1), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return null;
            }
        }, 128).blockingLast();
    }

    @Test(expected = NullPointerException.class)
    public void combineLatestDelayErrorIterableIteratorNull() {
        Flowable.combineLatestDelayError(new Iterable<Flowable<Object>>() {

            @Override
            public Iterator<Flowable<Object>> iterator() {
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
        Flowable.combineLatestDelayError(Arrays.asList(Flowable.never(), null), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }, 128).blockingLast();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableNullTests instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambVarargsOneIsNull() throws java.lang.Throwable {
            this.payloads.ambVarargsOneIsNull.evaluate();
        }

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
        public void benchmark_generateStateConsumerInitialStateNull() throws java.lang.Throwable {
            this.payloads.generateStateConsumerInitialStateNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_generateStateFunctionInitialStateNull() throws java.lang.Throwable {
            this.payloads.generateStateFunctionInitialStateNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_generateStateConsumerNull() throws java.lang.Throwable {
            this.payloads.generateStateConsumerNull.evaluate();
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
        public void benchmark_mergeArrayOneIsNull() throws java.lang.Throwable {
            this.payloads.mergeArrayOneIsNull.evaluate();
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
        public void benchmark_mergeDelayErrorArrayOneIsNull() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorArrayOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingFlowableSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.usingFlowableSupplierReturnsNull.evaluate();
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
        public void benchmark_zipIterable2Null() throws java.lang.Throwable {
            this.payloads.zipIterable2Null.evaluate();
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
        public void benchmark_reduceWithSeedNull() throws java.lang.Throwable {
            this.payloads.reduceWithSeedNull.evaluate();
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
        public void benchmark_replaySelectorNull() throws java.lang.Throwable {
            this.payloads.replaySelectorNull.evaluate();
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
        public void benchmark_scanSeedNull() throws java.lang.Throwable {
            this.payloads.scanSeedNull.evaluate();
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
        public void benchmark_startWithArrayOneNull() throws java.lang.Throwable {
            this.payloads.startWithArrayOneNull.evaluate();
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
        public void benchmark_timeoutSelectorOtherNull() throws java.lang.Throwable {
            this.payloads.timeoutSelectorOtherNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutFirstItemReturnsNull() throws java.lang.Throwable {
            this.payloads.timeoutFirstItemReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timestampUnitNull() throws java.lang.Throwable {
            this.payloads.timestampUnitNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timestampSchedulerNull() throws java.lang.Throwable {
            this.payloads.timestampSchedulerNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toListSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.toListSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toListSupplierReturnsNullSingle() throws java.lang.Throwable {
            this.payloads.toListSupplierReturnsNullSingle.evaluate();
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
        public void benchmark_windowOpenCloseOpenNull() throws java.lang.Throwable {
            this.payloads.windowOpenCloseOpenNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowOpenCloseCloseReturnsNull() throws java.lang.Throwable {
            this.payloads.windowOpenCloseCloseReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withLatestFromOtherNull() throws java.lang.Throwable {
            this.payloads.withLatestFromOtherNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withLatestFromCombinerReturnsNull() throws java.lang.Throwable {
            this.payloads.withLatestFromCombinerReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithIterableNull() throws java.lang.Throwable {
            this.payloads.zipWithIterableNull.evaluate();
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
        public void benchmark_zipWithPublisherNull() throws java.lang.Throwable {
            this.payloads.zipWithPublisherNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithCombinerReturnsNull() throws java.lang.Throwable {
            this.payloads.zipWithCombinerReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncSubjectOnNextNull() throws java.lang.Throwable {
            this.payloads.asyncSubjectOnNextNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncSubjectOnErrorNull() throws java.lang.Throwable {
            this.payloads.asyncSubjectOnErrorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_behaviorSubjectOnNextNull() throws java.lang.Throwable {
            this.payloads.behaviorSubjectOnNextNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_behaviorSubjectOnErrorNull() throws java.lang.Throwable {
            this.payloads.behaviorSubjectOnErrorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishSubjectOnNextNull() throws java.lang.Throwable {
            this.payloads.publishSubjectOnNextNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishSubjectOnErrorNull() throws java.lang.Throwable {
            this.payloads.publishSubjectOnErrorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaycSubjectOnNextNull() throws java.lang.Throwable {
            this.payloads.replaycSubjectOnNextNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySubjectOnErrorNull() throws java.lang.Throwable {
            this.payloads.replaySubjectOnErrorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedcSubjectOnNextNull() throws java.lang.Throwable {
            this.payloads.serializedcSubjectOnNextNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedSubjectOnErrorNull() throws java.lang.Throwable {
            this.payloads.serializedSubjectOnErrorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestDelayErrorIterableFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.combineLatestDelayErrorIterableFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestDelayErrorIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.combineLatestDelayErrorIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineLatestDelayErrorIterableOneIsNull() throws java.lang.Throwable {
            this.payloads.combineLatestDelayErrorIterableOneIsNull.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableNullTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableNullTests> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableNullTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableNullTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableNullTests();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableNullTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableNullTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableNullTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement ambVarargsOneIsNull;

            public org.junit.runners.model.Statement ambIterableIteratorNull;

            public org.junit.runners.model.Statement ambIterableOneIsNull;

            public org.junit.runners.model.Statement combineLatestIterableIteratorNull;

            public org.junit.runners.model.Statement combineLatestIterableOneIsNull;

            public org.junit.runners.model.Statement combineLatestIterableFunctionReturnsNull;

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

            public org.junit.runners.model.Statement generateStateConsumerInitialStateNull;

            public org.junit.runners.model.Statement generateStateFunctionInitialStateNull;

            public org.junit.runners.model.Statement generateStateConsumerNull;

            public org.junit.runners.model.Statement generateConsumerStateNullAllowed;

            public org.junit.runners.model.Statement generateFunctionStateNullAllowed;

            public org.junit.runners.model.Statement justNull;

            public org.junit.runners.model.Statement mergeIterableIteratorNull;

            public org.junit.runners.model.Statement mergeIterableOneIsNull;

            public org.junit.runners.model.Statement mergeArrayOneIsNull;

            public org.junit.runners.model.Statement mergeDelayErrorIterableIteratorNull;

            public org.junit.runners.model.Statement mergeDelayErrorIterableOneIsNull;

            public org.junit.runners.model.Statement mergeDelayErrorArrayOneIsNull;

            public org.junit.runners.model.Statement usingFlowableSupplierReturnsNull;

            public org.junit.runners.model.Statement zipIterableIteratorNull;

            public org.junit.runners.model.Statement zipIterableFunctionReturnsNull;

            public org.junit.runners.model.Statement zipIterable2Null;

            public org.junit.runners.model.Statement zipIterable2IteratorNull;

            public org.junit.runners.model.Statement zipIterable2FunctionReturnsNull;

            public org.junit.runners.model.Statement bufferSupplierReturnsNull;

            public org.junit.runners.model.Statement bufferTimedSupplierReturnsNull;

            public org.junit.runners.model.Statement bufferOpenCloseCloseReturnsNull;

            public org.junit.runners.model.Statement bufferBoundarySupplierReturnsNull;

            public org.junit.runners.model.Statement collectInitialSupplierReturnsNull;

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

            public org.junit.runners.model.Statement reduceWithSeedNull;

            public org.junit.runners.model.Statement reduceWithSeedReturnsNull;

            public org.junit.runners.model.Statement repeatWhenFunctionReturnsNull;

            public org.junit.runners.model.Statement replaySelectorNull;

            public org.junit.runners.model.Statement replaySelectorReturnsNull;

            public org.junit.runners.model.Statement replayBoundedSelectorReturnsNull;

            public org.junit.runners.model.Statement replayTimeBoundedSelectorReturnsNull;

            public org.junit.runners.model.Statement retryWhenFunctionReturnsNull;

            public org.junit.runners.model.Statement scanFunctionReturnsNull;

            public org.junit.runners.model.Statement scanSeedNull;

            public org.junit.runners.model.Statement scanSeedFunctionReturnsNull;

            public org.junit.runners.model.Statement scanSeedSupplierReturnsNull;

            public org.junit.runners.model.Statement scanSeedSupplierFunctionReturnsNull;

            public org.junit.runners.model.Statement startWithIterableIteratorNull;

            public org.junit.runners.model.Statement startWithIterableOneNull;

            public org.junit.runners.model.Statement startWithArrayOneNull;

            public org.junit.runners.model.Statement switchMapFunctionReturnsNull;

            public org.junit.runners.model.Statement timeoutSelectorReturnsNull;

            public org.junit.runners.model.Statement timeoutSelectorOtherNull;

            public org.junit.runners.model.Statement timeoutFirstItemReturnsNull;

            public org.junit.runners.model.Statement timestampUnitNull;

            public org.junit.runners.model.Statement timestampSchedulerNull;

            public org.junit.runners.model.Statement toListSupplierReturnsNull;

            public org.junit.runners.model.Statement toListSupplierReturnsNullSingle;

            public org.junit.runners.model.Statement toMapValueSelectorReturnsNull;

            public org.junit.runners.model.Statement toMapMapSupplierReturnsNull;

            public org.junit.runners.model.Statement toMultiMapValueSelectorReturnsNullAllowed;

            public org.junit.runners.model.Statement toMultimapMapSupplierReturnsNull;

            public org.junit.runners.model.Statement toMultimapMapCollectionSupplierReturnsNull;

            public org.junit.runners.model.Statement windowOpenCloseOpenNull;

            public org.junit.runners.model.Statement windowOpenCloseCloseReturnsNull;

            public org.junit.runners.model.Statement withLatestFromOtherNull;

            public org.junit.runners.model.Statement withLatestFromCombinerReturnsNull;

            public org.junit.runners.model.Statement zipWithIterableNull;

            public org.junit.runners.model.Statement zipWithIterableCombinerReturnsNull;

            public org.junit.runners.model.Statement zipWithIterableIteratorNull;

            public org.junit.runners.model.Statement zipWithIterableOneIsNull;

            public org.junit.runners.model.Statement zipWithPublisherNull;

            public org.junit.runners.model.Statement zipWithCombinerReturnsNull;

            public org.junit.runners.model.Statement asyncSubjectOnNextNull;

            public org.junit.runners.model.Statement asyncSubjectOnErrorNull;

            public org.junit.runners.model.Statement behaviorSubjectOnNextNull;

            public org.junit.runners.model.Statement behaviorSubjectOnErrorNull;

            public org.junit.runners.model.Statement publishSubjectOnNextNull;

            public org.junit.runners.model.Statement publishSubjectOnErrorNull;

            public org.junit.runners.model.Statement replaycSubjectOnNextNull;

            public org.junit.runners.model.Statement replaySubjectOnErrorNull;

            public org.junit.runners.model.Statement serializedcSubjectOnNextNull;

            public org.junit.runners.model.Statement serializedSubjectOnErrorNull;

            public org.junit.runners.model.Statement combineLatestDelayErrorIterableFunctionReturnsNull;

            public org.junit.runners.model.Statement combineLatestDelayErrorIterableIteratorNull;

            public org.junit.runners.model.Statement combineLatestDelayErrorIterableOneIsNull;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.ambVarargsOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::ambVarargsOneIsNull, java.lang.NullPointerException.class), "ambVarargsOneIsNull", this);
            this.payloads.ambIterableIteratorNull = _ClassStatement.forPayload(FlowableNullTests::ambIterableIteratorNull, "ambIterableIteratorNull", this);
            this.payloads.ambIterableOneIsNull = _ClassStatement.forPayload(FlowableNullTests::ambIterableOneIsNull, "ambIterableOneIsNull", this);
            this.payloads.combineLatestIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::combineLatestIterableIteratorNull, java.lang.NullPointerException.class), "combineLatestIterableIteratorNull", this);
            this.payloads.combineLatestIterableOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::combineLatestIterableOneIsNull, java.lang.NullPointerException.class), "combineLatestIterableOneIsNull", this);
            this.payloads.combineLatestIterableFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::combineLatestIterableFunctionReturnsNull, java.lang.NullPointerException.class), "combineLatestIterableFunctionReturnsNull", this);
            this.payloads.concatIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::concatIterableIteratorNull, java.lang.NullPointerException.class), "concatIterableIteratorNull", this);
            this.payloads.concatIterableOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::concatIterableOneIsNull, java.lang.NullPointerException.class), "concatIterableOneIsNull", this);
            this.payloads.concatArrayOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::concatArrayOneIsNull, java.lang.NullPointerException.class), "concatArrayOneIsNull", this);
            this.payloads.deferFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::deferFunctionReturnsNull, java.lang.NullPointerException.class), "deferFunctionReturnsNull", this);
            this.payloads.errorFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::errorFunctionReturnsNull, java.lang.NullPointerException.class), "errorFunctionReturnsNull", this);
            this.payloads.fromArrayOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::fromArrayOneIsNull, java.lang.NullPointerException.class), "fromArrayOneIsNull", this);
            this.payloads.fromCallableReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::fromCallableReturnsNull, java.lang.NullPointerException.class), "fromCallableReturnsNull", this);
            this.payloads.fromFutureReturnsNull = _ClassStatement.forPayload(FlowableNullTests::fromFutureReturnsNull, "fromFutureReturnsNull", this);
            this.payloads.fromFutureTimedReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::fromFutureTimedReturnsNull, java.lang.NullPointerException.class), "fromFutureTimedReturnsNull", this);
            this.payloads.fromIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::fromIterableIteratorNull, java.lang.NullPointerException.class), "fromIterableIteratorNull", this);
            this.payloads.fromIterableValueNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::fromIterableValueNull, java.lang.NullPointerException.class), "fromIterableValueNull", this);
            this.payloads.generateConsumerEmitsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::generateConsumerEmitsNull, java.lang.NullPointerException.class), "generateConsumerEmitsNull", this);
            this.payloads.generateStateConsumerInitialStateNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::generateStateConsumerInitialStateNull, java.lang.NullPointerException.class), "generateStateConsumerInitialStateNull", this);
            this.payloads.generateStateFunctionInitialStateNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::generateStateFunctionInitialStateNull, java.lang.NullPointerException.class), "generateStateFunctionInitialStateNull", this);
            this.payloads.generateStateConsumerNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::generateStateConsumerNull, java.lang.NullPointerException.class), "generateStateConsumerNull", this);
            this.payloads.generateConsumerStateNullAllowed = _ClassStatement.forPayload(FlowableNullTests::generateConsumerStateNullAllowed, "generateConsumerStateNullAllowed", this);
            this.payloads.generateFunctionStateNullAllowed = _ClassStatement.forPayload(FlowableNullTests::generateFunctionStateNullAllowed, "generateFunctionStateNullAllowed", this);
            this.payloads.justNull = _ClassStatement.forPayload(FlowableNullTests::justNull, "justNull", this);
            this.payloads.mergeIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::mergeIterableIteratorNull, java.lang.NullPointerException.class), "mergeIterableIteratorNull", this);
            this.payloads.mergeIterableOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::mergeIterableOneIsNull, java.lang.NullPointerException.class), "mergeIterableOneIsNull", this);
            this.payloads.mergeArrayOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::mergeArrayOneIsNull, java.lang.NullPointerException.class), "mergeArrayOneIsNull", this);
            this.payloads.mergeDelayErrorIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::mergeDelayErrorIterableIteratorNull, java.lang.NullPointerException.class), "mergeDelayErrorIterableIteratorNull", this);
            this.payloads.mergeDelayErrorIterableOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::mergeDelayErrorIterableOneIsNull, java.lang.NullPointerException.class), "mergeDelayErrorIterableOneIsNull", this);
            this.payloads.mergeDelayErrorArrayOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::mergeDelayErrorArrayOneIsNull, java.lang.NullPointerException.class), "mergeDelayErrorArrayOneIsNull", this);
            this.payloads.usingFlowableSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::usingFlowableSupplierReturnsNull, java.lang.NullPointerException.class), "usingFlowableSupplierReturnsNull", this);
            this.payloads.zipIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::zipIterableIteratorNull, java.lang.NullPointerException.class), "zipIterableIteratorNull", this);
            this.payloads.zipIterableFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::zipIterableFunctionReturnsNull, java.lang.NullPointerException.class), "zipIterableFunctionReturnsNull", this);
            this.payloads.zipIterable2Null = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::zipIterable2Null, java.lang.NullPointerException.class), "zipIterable2Null", this);
            this.payloads.zipIterable2IteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::zipIterable2IteratorNull, java.lang.NullPointerException.class), "zipIterable2IteratorNull", this);
            this.payloads.zipIterable2FunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::zipIterable2FunctionReturnsNull, java.lang.NullPointerException.class), "zipIterable2FunctionReturnsNull", this);
            this.payloads.bufferSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::bufferSupplierReturnsNull, java.lang.NullPointerException.class), "bufferSupplierReturnsNull", this);
            this.payloads.bufferTimedSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::bufferTimedSupplierReturnsNull, java.lang.NullPointerException.class), "bufferTimedSupplierReturnsNull", this);
            this.payloads.bufferOpenCloseCloseReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::bufferOpenCloseCloseReturnsNull, java.lang.NullPointerException.class), "bufferOpenCloseCloseReturnsNull", this);
            this.payloads.bufferBoundarySupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::bufferBoundarySupplierReturnsNull, java.lang.NullPointerException.class), "bufferBoundarySupplierReturnsNull", this);
            this.payloads.collectInitialSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::collectInitialSupplierReturnsNull, java.lang.NullPointerException.class), "collectInitialSupplierReturnsNull", this);
            this.payloads.concatMapReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::concatMapReturnsNull, java.lang.NullPointerException.class), "concatMapReturnsNull", this);
            this.payloads.concatMapIterableReturnNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::concatMapIterableReturnNull, java.lang.NullPointerException.class), "concatMapIterableReturnNull", this);
            this.payloads.concatMapIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::concatMapIterableIteratorNull, java.lang.NullPointerException.class), "concatMapIterableIteratorNull", this);
            this.payloads.debounceFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::debounceFunctionReturnsNull, java.lang.NullPointerException.class), "debounceFunctionReturnsNull", this);
            this.payloads.delayWithFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::delayWithFunctionReturnsNull, java.lang.NullPointerException.class), "delayWithFunctionReturnsNull", this);
            this.payloads.delayBothItemSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::delayBothItemSupplierReturnsNull, java.lang.NullPointerException.class), "delayBothItemSupplierReturnsNull", this);
            this.payloads.distinctSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::distinctSupplierReturnsNull, java.lang.NullPointerException.class), "distinctSupplierReturnsNull", this);
            this.payloads.distinctFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::distinctFunctionReturnsNull, java.lang.NullPointerException.class), "distinctFunctionReturnsNull", this);
            this.payloads.distinctUntilChangedFunctionReturnsNull = _ClassStatement.forPayload(FlowableNullTests::distinctUntilChangedFunctionReturnsNull, "distinctUntilChangedFunctionReturnsNull", this);
            this.payloads.flatMapFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::flatMapFunctionReturnsNull, java.lang.NullPointerException.class), "flatMapFunctionReturnsNull", this);
            this.payloads.flatMapNotificationOnNextReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::flatMapNotificationOnNextReturnsNull, java.lang.NullPointerException.class), "flatMapNotificationOnNextReturnsNull", this);
            this.payloads.flatMapNotificationOnCompleteReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::flatMapNotificationOnCompleteReturnsNull, java.lang.NullPointerException.class), "flatMapNotificationOnCompleteReturnsNull", this);
            this.payloads.flatMapCombinerMapperReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::flatMapCombinerMapperReturnsNull, java.lang.NullPointerException.class), "flatMapCombinerMapperReturnsNull", this);
            this.payloads.flatMapCombinerCombinerReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::flatMapCombinerCombinerReturnsNull, java.lang.NullPointerException.class), "flatMapCombinerCombinerReturnsNull", this);
            this.payloads.flatMapIterableMapperReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::flatMapIterableMapperReturnsNull, java.lang.NullPointerException.class), "flatMapIterableMapperReturnsNull", this);
            this.payloads.flatMapIterableMapperIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::flatMapIterableMapperIteratorNull, java.lang.NullPointerException.class), "flatMapIterableMapperIteratorNull", this);
            this.payloads.flatMapIterableMapperIterableOneNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::flatMapIterableMapperIterableOneNull, java.lang.NullPointerException.class), "flatMapIterableMapperIterableOneNull", this);
            this.payloads.flatMapIterableCombinerReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::flatMapIterableCombinerReturnsNull, java.lang.NullPointerException.class), "flatMapIterableCombinerReturnsNull", this);
            this.payloads.groupByValueReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::groupByValueReturnsNull, java.lang.NullPointerException.class), "groupByValueReturnsNull", this);
            this.payloads.liftReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::liftReturnsNull, java.lang.NullPointerException.class), "liftReturnsNull", this);
            this.payloads.mapReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::mapReturnsNull, java.lang.NullPointerException.class), "mapReturnsNull", this);
            this.payloads.onErrorResumeNextFunctionReturnsNull = _ClassStatement.forPayload(FlowableNullTests::onErrorResumeNextFunctionReturnsNull, "onErrorResumeNextFunctionReturnsNull", this);
            this.payloads.onErrorReturnFunctionReturnsNull = _ClassStatement.forPayload(FlowableNullTests::onErrorReturnFunctionReturnsNull, "onErrorReturnFunctionReturnsNull", this);
            this.payloads.publishFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::publishFunctionReturnsNull, java.lang.NullPointerException.class), "publishFunctionReturnsNull", this);
            this.payloads.reduceFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::reduceFunctionReturnsNull, java.lang.NullPointerException.class), "reduceFunctionReturnsNull", this);
            this.payloads.reduceSeedFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::reduceSeedFunctionReturnsNull, java.lang.NullPointerException.class), "reduceSeedFunctionReturnsNull", this);
            this.payloads.reduceWithSeedNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::reduceWithSeedNull, java.lang.NullPointerException.class), "reduceWithSeedNull", this);
            this.payloads.reduceWithSeedReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::reduceWithSeedReturnsNull, java.lang.NullPointerException.class), "reduceWithSeedReturnsNull", this);
            this.payloads.repeatWhenFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::repeatWhenFunctionReturnsNull, java.lang.NullPointerException.class), "repeatWhenFunctionReturnsNull", this);
            this.payloads.replaySelectorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::replaySelectorNull, java.lang.NullPointerException.class), "replaySelectorNull", this);
            this.payloads.replaySelectorReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::replaySelectorReturnsNull, java.lang.NullPointerException.class), "replaySelectorReturnsNull", this);
            this.payloads.replayBoundedSelectorReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::replayBoundedSelectorReturnsNull, java.lang.NullPointerException.class), "replayBoundedSelectorReturnsNull", this);
            this.payloads.replayTimeBoundedSelectorReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::replayTimeBoundedSelectorReturnsNull, java.lang.NullPointerException.class), "replayTimeBoundedSelectorReturnsNull", this);
            this.payloads.retryWhenFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::retryWhenFunctionReturnsNull, java.lang.NullPointerException.class), "retryWhenFunctionReturnsNull", this);
            this.payloads.scanFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::scanFunctionReturnsNull, java.lang.NullPointerException.class), "scanFunctionReturnsNull", this);
            this.payloads.scanSeedNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::scanSeedNull, java.lang.NullPointerException.class), "scanSeedNull", this);
            this.payloads.scanSeedFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::scanSeedFunctionReturnsNull, java.lang.NullPointerException.class), "scanSeedFunctionReturnsNull", this);
            this.payloads.scanSeedSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::scanSeedSupplierReturnsNull, java.lang.NullPointerException.class), "scanSeedSupplierReturnsNull", this);
            this.payloads.scanSeedSupplierFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::scanSeedSupplierFunctionReturnsNull, java.lang.NullPointerException.class), "scanSeedSupplierFunctionReturnsNull", this);
            this.payloads.startWithIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::startWithIterableIteratorNull, java.lang.NullPointerException.class), "startWithIterableIteratorNull", this);
            this.payloads.startWithIterableOneNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::startWithIterableOneNull, java.lang.NullPointerException.class), "startWithIterableOneNull", this);
            this.payloads.startWithArrayOneNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::startWithArrayOneNull, java.lang.NullPointerException.class), "startWithArrayOneNull", this);
            this.payloads.switchMapFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::switchMapFunctionReturnsNull, java.lang.NullPointerException.class), "switchMapFunctionReturnsNull", this);
            this.payloads.timeoutSelectorReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::timeoutSelectorReturnsNull, java.lang.NullPointerException.class), "timeoutSelectorReturnsNull", this);
            this.payloads.timeoutSelectorOtherNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::timeoutSelectorOtherNull, java.lang.NullPointerException.class), "timeoutSelectorOtherNull", this);
            this.payloads.timeoutFirstItemReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::timeoutFirstItemReturnsNull, java.lang.NullPointerException.class), "timeoutFirstItemReturnsNull", this);
            this.payloads.timestampUnitNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::timestampUnitNull, java.lang.NullPointerException.class), "timestampUnitNull", this);
            this.payloads.timestampSchedulerNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::timestampSchedulerNull, java.lang.NullPointerException.class), "timestampSchedulerNull", this);
            this.payloads.toListSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::toListSupplierReturnsNull, java.lang.NullPointerException.class), "toListSupplierReturnsNull", this);
            this.payloads.toListSupplierReturnsNullSingle = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::toListSupplierReturnsNullSingle, java.lang.NullPointerException.class), "toListSupplierReturnsNullSingle", this);
            this.payloads.toMapValueSelectorReturnsNull = _ClassStatement.forPayload(FlowableNullTests::toMapValueSelectorReturnsNull, "toMapValueSelectorReturnsNull", this);
            this.payloads.toMapMapSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::toMapMapSupplierReturnsNull, java.lang.NullPointerException.class), "toMapMapSupplierReturnsNull", this);
            this.payloads.toMultiMapValueSelectorReturnsNullAllowed = _ClassStatement.forPayload(FlowableNullTests::toMultiMapValueSelectorReturnsNullAllowed, "toMultiMapValueSelectorReturnsNullAllowed", this);
            this.payloads.toMultimapMapSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::toMultimapMapSupplierReturnsNull, java.lang.NullPointerException.class), "toMultimapMapSupplierReturnsNull", this);
            this.payloads.toMultimapMapCollectionSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::toMultimapMapCollectionSupplierReturnsNull, java.lang.NullPointerException.class), "toMultimapMapCollectionSupplierReturnsNull", this);
            this.payloads.windowOpenCloseOpenNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::windowOpenCloseOpenNull, java.lang.NullPointerException.class), "windowOpenCloseOpenNull", this);
            this.payloads.windowOpenCloseCloseReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::windowOpenCloseCloseReturnsNull, java.lang.NullPointerException.class), "windowOpenCloseCloseReturnsNull", this);
            this.payloads.withLatestFromOtherNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::withLatestFromOtherNull, java.lang.NullPointerException.class), "withLatestFromOtherNull", this);
            this.payloads.withLatestFromCombinerReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::withLatestFromCombinerReturnsNull, java.lang.NullPointerException.class), "withLatestFromCombinerReturnsNull", this);
            this.payloads.zipWithIterableNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::zipWithIterableNull, java.lang.NullPointerException.class), "zipWithIterableNull", this);
            this.payloads.zipWithIterableCombinerReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::zipWithIterableCombinerReturnsNull, java.lang.NullPointerException.class), "zipWithIterableCombinerReturnsNull", this);
            this.payloads.zipWithIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::zipWithIterableIteratorNull, java.lang.NullPointerException.class), "zipWithIterableIteratorNull", this);
            this.payloads.zipWithIterableOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::zipWithIterableOneIsNull, java.lang.NullPointerException.class), "zipWithIterableOneIsNull", this);
            this.payloads.zipWithPublisherNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::zipWithPublisherNull, java.lang.NullPointerException.class), "zipWithPublisherNull", this);
            this.payloads.zipWithCombinerReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::zipWithCombinerReturnsNull, java.lang.NullPointerException.class), "zipWithCombinerReturnsNull", this);
            this.payloads.asyncSubjectOnNextNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::asyncSubjectOnNextNull, java.lang.NullPointerException.class), "asyncSubjectOnNextNull", this);
            this.payloads.asyncSubjectOnErrorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::asyncSubjectOnErrorNull, java.lang.NullPointerException.class), "asyncSubjectOnErrorNull", this);
            this.payloads.behaviorSubjectOnNextNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::behaviorSubjectOnNextNull, java.lang.NullPointerException.class), "behaviorSubjectOnNextNull", this);
            this.payloads.behaviorSubjectOnErrorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::behaviorSubjectOnErrorNull, java.lang.NullPointerException.class), "behaviorSubjectOnErrorNull", this);
            this.payloads.publishSubjectOnNextNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::publishSubjectOnNextNull, java.lang.NullPointerException.class), "publishSubjectOnNextNull", this);
            this.payloads.publishSubjectOnErrorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::publishSubjectOnErrorNull, java.lang.NullPointerException.class), "publishSubjectOnErrorNull", this);
            this.payloads.replaycSubjectOnNextNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::replaycSubjectOnNextNull, java.lang.NullPointerException.class), "replaycSubjectOnNextNull", this);
            this.payloads.replaySubjectOnErrorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::replaySubjectOnErrorNull, java.lang.NullPointerException.class), "replaySubjectOnErrorNull", this);
            this.payloads.serializedcSubjectOnNextNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::serializedcSubjectOnNextNull, java.lang.NullPointerException.class), "serializedcSubjectOnNextNull", this);
            this.payloads.serializedSubjectOnErrorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::serializedSubjectOnErrorNull, java.lang.NullPointerException.class), "serializedSubjectOnErrorNull", this);
            this.payloads.combineLatestDelayErrorIterableFunctionReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::combineLatestDelayErrorIterableFunctionReturnsNull, java.lang.NullPointerException.class), "combineLatestDelayErrorIterableFunctionReturnsNull", this);
            this.payloads.combineLatestDelayErrorIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::combineLatestDelayErrorIterableIteratorNull, java.lang.NullPointerException.class), "combineLatestDelayErrorIterableIteratorNull", this);
            this.payloads.combineLatestDelayErrorIterableOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNullTests::combineLatestDelayErrorIterableOneIsNull, java.lang.NullPointerException.class), "combineLatestDelayErrorIterableOneIsNull", this);
        }
    }
}
