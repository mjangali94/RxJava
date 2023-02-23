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
import org.reactivestreams.*;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableFlatMapTest extends RxJavaTest {

    @Test
    public void normal() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        final List<Integer> list = Arrays.asList(1, 2, 3);
        Function<Integer, List<Integer>> func = new Function<Integer, List<Integer>>() {

            @Override
            public List<Integer> apply(Integer t1) {
                return list;
            }
        };
        BiFunction<Integer, Integer, Integer> resFunc = new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 | t2;
            }
        };
        List<Integer> source = Arrays.asList(16, 32, 64);
        Flowable.fromIterable(source).flatMapIterable(func, resFunc).subscribe(subscriber);
        for (Integer s : source) {
            for (Integer v : list) {
                verify(subscriber).onNext(s | v);
            }
        }
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void collectionFunctionThrows() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        Function<Integer, List<Integer>> func = new Function<Integer, List<Integer>>() {

            @Override
            public List<Integer> apply(Integer t1) {
                throw new TestException();
            }
        };
        BiFunction<Integer, Integer, Integer> resFunc = new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 | t2;
            }
        };
        List<Integer> source = Arrays.asList(16, 32, 64);
        Flowable.fromIterable(source).flatMapIterable(func, resFunc).subscribe(subscriber);
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
        verify(subscriber).onError(any(TestException.class));
    }

    @Test
    public void resultFunctionThrows() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        final List<Integer> list = Arrays.asList(1, 2, 3);
        Function<Integer, List<Integer>> func = new Function<Integer, List<Integer>>() {

            @Override
            public List<Integer> apply(Integer t1) {
                return list;
            }
        };
        BiFunction<Integer, Integer, Integer> resFunc = new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                throw new TestException();
            }
        };
        List<Integer> source = Arrays.asList(16, 32, 64);
        Flowable.fromIterable(source).flatMapIterable(func, resFunc).subscribe(subscriber);
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
        verify(subscriber).onError(any(TestException.class));
    }

    @Test
    public void mergeError() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        Function<Integer, Flowable<Integer>> func = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return Flowable.error(new TestException());
            }
        };
        BiFunction<Integer, Integer, Integer> resFunc = new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 | t2;
            }
        };
        List<Integer> source = Arrays.asList(16, 32, 64);
        Flowable.fromIterable(source).flatMap(func, resFunc).subscribe(subscriber);
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
        verify(subscriber).onError(any(TestException.class));
    }

    <T, R> Function<T, R> just(final R value) {
        return new Function<T, R>() {

            @Override
            public R apply(T t1) {
                return value;
            }
        };
    }

    <R> Supplier<R> just0(final R value) {
        return new Supplier<R>() {

            @Override
            public R get() {
                return value;
            }
        };
    }

    @Test
    public void flatMapTransformsNormal() {
        Flowable<Integer> onNext = Flowable.fromIterable(Arrays.asList(1, 2, 3));
        Flowable<Integer> onComplete = Flowable.fromIterable(Arrays.asList(4));
        Flowable<Integer> onError = Flowable.fromIterable(Arrays.asList(5));
        Flowable<Integer> source = Flowable.fromIterable(Arrays.asList(10, 20, 30));
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.flatMap(just(onNext), just(onError), just0(onComplete)).subscribe(subscriber);
        verify(subscriber, times(3)).onNext(1);
        verify(subscriber, times(3)).onNext(2);
        verify(subscriber, times(3)).onNext(3);
        verify(subscriber).onNext(4);
        verify(subscriber).onComplete();
        verify(subscriber, never()).onNext(5);
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void flatMapTransformsException() {
        Flowable<Integer> onNext = Flowable.fromIterable(Arrays.asList(1, 2, 3));
        Flowable<Integer> onComplete = Flowable.fromIterable(Arrays.asList(4));
        Flowable<Integer> onError = Flowable.fromIterable(Arrays.asList(5));
        Flowable<Integer> source = Flowable.concat(Flowable.fromIterable(Arrays.asList(10, 20, 30)), Flowable.<Integer>error(new RuntimeException("Forced failure!")));
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.flatMap(just(onNext), just(onError), just0(onComplete)).subscribe(subscriber);
        verify(subscriber, times(3)).onNext(1);
        verify(subscriber, times(3)).onNext(2);
        verify(subscriber, times(3)).onNext(3);
        verify(subscriber).onNext(5);
        verify(subscriber).onComplete();
        verify(subscriber, never()).onNext(4);
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    <R> Supplier<R> funcThrow0(R r) {
        return new Supplier<R>() {

            @Override
            public R get() {
                throw new TestException();
            }
        };
    }

    <T, R> Function<T, R> funcThrow(T t, R r) {
        return new Function<T, R>() {

            @Override
            public R apply(T t) {
                throw new TestException();
            }
        };
    }

    @Test
    public void flatMapTransformsOnNextFuncThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable<Integer> onComplete = Flowable.fromIterable(Arrays.asList(4));
            Flowable<Integer> onError = Flowable.fromIterable(Arrays.asList(5));
            Flowable<Integer> source = Flowable.fromIterable(Arrays.asList(10, 20, 30));
            Subscriber<Object> subscriber = TestHelper.mockSubscriber();
            source.flatMap(funcThrow(1, onError), just(onError), just0(onComplete)).subscribe(subscriber);
            verify(subscriber).onError(any(TestException.class));
            verify(subscriber, never()).onNext(any());
            verify(subscriber, never()).onComplete();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void flatMapTransformsOnErrorFuncThrows() {
        Flowable<Integer> onNext = Flowable.fromIterable(Arrays.asList(1, 2, 3));
        Flowable<Integer> onComplete = Flowable.fromIterable(Arrays.asList(4));
        Flowable<Integer> onError = Flowable.fromIterable(Arrays.asList(5));
        Flowable<Integer> source = Flowable.error(new TestException());
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.flatMap(just(onNext), funcThrow((Throwable) null, onError), just0(onComplete)).subscribe(subscriber);
        verify(subscriber).onError(any(CompositeException.class));
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void flatMapTransformsOnCompletedFuncThrows() {
        Flowable<Integer> onNext = Flowable.fromIterable(Arrays.asList(1, 2, 3));
        Flowable<Integer> onComplete = Flowable.fromIterable(Arrays.asList(4));
        Flowable<Integer> onError = Flowable.fromIterable(Arrays.asList(5));
        Flowable<Integer> source = Flowable.fromIterable(Arrays.<Integer>asList());
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.flatMap(just(onNext), just(onError), funcThrow0(onComplete)).subscribe(subscriber);
        verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void flatMapTransformsMergeException() {
        Flowable<Integer> onNext = Flowable.error(new TestException());
        Flowable<Integer> onComplete = Flowable.fromIterable(Arrays.asList(4));
        Flowable<Integer> onError = Flowable.fromIterable(Arrays.asList(5));
        Flowable<Integer> source = Flowable.fromIterable(Arrays.asList(10, 20, 30));
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.flatMap(just(onNext), just(onError), funcThrow0(onComplete)).subscribe(subscriber);
        verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
    }

    private static <T> Flowable<T> composer(Flowable<T> source, final AtomicInteger subscriptionCount, final int m) {
        return source.doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) {
                int n = subscriptionCount.getAndIncrement();
                if (n >= m) {
                    Assert.fail("Too many subscriptions! " + (n + 1));
                }
            }
        }).doOnComplete(new Action() {

            @Override
            public void run() {
                int n = subscriptionCount.decrementAndGet();
                if (n < 0) {
                    Assert.fail("Too many unsubscriptions! " + (n - 1));
                }
            }
        });
    }

    @Test
    public void flatMapMaxConcurrent() {
        final int m = 4;
        final AtomicInteger subscriptionCount = new AtomicInteger();
        Flowable<Integer> source = Flowable.range(1, 10).flatMap(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return composer(Flowable.range(t1 * 10, 2), subscriptionCount, m).subscribeOn(Schedulers.computation());
            }
        }, m);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        source.subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        Set<Integer> expected = new HashSet<>(Arrays.asList(10, 11, 20, 21, 30, 31, 40, 41, 50, 51, 60, 61, 70, 71, 80, 81, 90, 91, 100, 101));
        Assert.assertEquals(expected.size(), ts.values().size());
        Assert.assertTrue(expected.containsAll(ts.values()));
    }

    @Test
    public void flatMapSelectorMaxConcurrent() {
        final int m = 4;
        final AtomicInteger subscriptionCount = new AtomicInteger();
        Flowable<Integer> source = Flowable.range(1, 10).flatMap(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return composer(Flowable.range(t1 * 10, 2), subscriptionCount, m).subscribeOn(Schedulers.computation());
            }
        }, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 * 1000 + t2;
            }
        }, m);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        source.subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        Set<Integer> expected = new HashSet<>(Arrays.asList(1010, 1011, 2020, 2021, 3030, 3031, 4040, 4041, 5050, 5051, 6060, 6061, 7070, 7071, 8080, 8081, 9090, 9091, 10100, 10101));
        Assert.assertEquals(expected.size(), ts.values().size());
        // System.out.println("--> testFlatMapSelectorMaxConcurrent: " + ts.values());
        Assert.assertTrue(expected.containsAll(ts.values()));
    }

    @Test
    public void flatMapTransformsMaxConcurrentNormalLoop() {
        for (int i = 0; i < 1000; i++) {
            if (i % 100 == 0) {
                // System.out.println("testFlatMapTransformsMaxConcurrentNormalLoop => " + i);
            }
            flatMapTransformsMaxConcurrentNormal();
        }
    }

    @Test
    public void flatMapTransformsMaxConcurrentNormal() {
        final int m = 2;
        final AtomicInteger subscriptionCount = new AtomicInteger();
        Flowable<Integer> onNext = composer(Flowable.fromIterable(Arrays.asList(1, 2, 3)).observeOn(Schedulers.computation()), subscriptionCount, m).subscribeOn(Schedulers.computation());
        Flowable<Integer> onComplete = composer(Flowable.fromIterable(Arrays.asList(4)), subscriptionCount, m).subscribeOn(Schedulers.computation());
        Flowable<Integer> onError = Flowable.fromIterable(Arrays.asList(5));
        Flowable<Integer> source = Flowable.fromIterable(Arrays.asList(10, 20, 30));
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>(subscriber);
        Function<Integer, Flowable<Integer>> just = just(onNext);
        Function<Throwable, Flowable<Integer>> just2 = just(onError);
        Supplier<Flowable<Integer>> just0 = just0(onComplete);
        source.flatMap(just, just2, just0, m).subscribe(ts);
        ts.awaitDone(1, TimeUnit.SECONDS);
        ts.assertNoErrors();
        ts.assertTerminated();
        verify(subscriber, times(3)).onNext(1);
        verify(subscriber, times(3)).onNext(2);
        verify(subscriber, times(3)).onNext(3);
        verify(subscriber).onNext(4);
        verify(subscriber).onComplete();
        verify(subscriber, never()).onNext(5);
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void flatMapRangeMixedAsyncLoop() {
        for (int i = 0; i < 2000; i++) {
            if (i % 10 == 0) {
                // System.out.println("flatMapRangeAsyncLoop > " + i);
            }
            TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
            Flowable.range(0, 1000).flatMap(new Function<Integer, Flowable<Integer>>() {

                final Random rnd = new Random();

                @Override
                public Flowable<Integer> apply(Integer t) {
                    Flowable<Integer> r = Flowable.just(t);
                    if (rnd.nextBoolean()) {
                        r = r.hide();
                    }
                    return r;
                }
            }).observeOn(Schedulers.computation()).subscribe(ts);
            ts.awaitDone(2500, TimeUnit.MILLISECONDS);
            if (ts.completions() == 0) {
                // System.out.println(ts.values().size());
            }
            ts.assertTerminated();
            ts.assertNoErrors();
            List<Integer> list = ts.values();
            if (list.size() < 1000) {
                Set<Integer> set = new HashSet<>(list);
                for (int j = 0; j < 1000; j++) {
                    if (!set.contains(j)) {
                        // System.out.println(j + " missing");
                    }
                }
            }
            assertEquals(1000, list.size());
        }
    }

    @Test
    public void flatMapIntPassthruAsync() {
        for (int i = 0; i < 1000; i++) {
            TestSubscriber<Integer> ts = new TestSubscriber<>();
            Flowable.range(1, 1000).flatMap(new Function<Integer, Flowable<Integer>>() {

                @Override
                public Flowable<Integer> apply(Integer t) {
                    return Flowable.just(1).subscribeOn(Schedulers.computation());
                }
            }).subscribe(ts);
            ts.awaitDone(5, TimeUnit.SECONDS);
            ts.assertNoErrors();
            ts.assertComplete();
            ts.assertValueCount(1000);
        }
    }

    @Test
    public void flatMapTwoNestedSync() {
        for (final int n : new int[] { 1, 1000, 1000000 }) {
            TestSubscriber<Integer> ts = new TestSubscriber<>();
            Flowable.just(1, 2).flatMap(new Function<Integer, Flowable<Integer>>() {

                @Override
                public Flowable<Integer> apply(Integer t) {
                    return Flowable.range(1, n);
                }
            }).subscribe(ts);
            // System.out.println("flatMapTwoNestedSync >> @ " + n);
            ts.assertNoErrors();
            ts.assertComplete();
            ts.assertValueCount(n * 2);
        }
    }

    @Test
    public void justEmptyMixture() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(0, 4 * Flowable.bufferSize()).flatMap(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return (v & 1) == 0 ? Flowable.<Integer>empty() : Flowable.just(v);
            }
        }).subscribe(ts);
        ts.assertValueCount(2 * Flowable.bufferSize());
        ts.assertNoErrors();
        ts.assertComplete();
        int j = 1;
        for (Integer v : ts.values()) {
            Assert.assertEquals(j, v.intValue());
            j += 2;
        }
    }

    @Test
    public void rangeEmptyMixture() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(0, 4 * Flowable.bufferSize()).flatMap(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return (v & 1) == 0 ? Flowable.<Integer>empty() : Flowable.range(v, 2);
            }
        }).subscribe(ts);
        ts.assertValueCount(4 * Flowable.bufferSize());
        ts.assertNoErrors();
        ts.assertComplete();
        int j = 1;
        List<Integer> list = ts.values();
        for (int i = 0; i < list.size(); i += 2) {
            Assert.assertEquals(j, list.get(i).intValue());
            Assert.assertEquals(j + 1, list.get(i + 1).intValue());
            j += 2;
        }
    }

    @Test
    public void justEmptyMixtureMaxConcurrent() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(0, 4 * Flowable.bufferSize()).flatMap(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return (v & 1) == 0 ? Flowable.<Integer>empty() : Flowable.just(v);
            }
        }, 16).subscribe(ts);
        ts.assertValueCount(2 * Flowable.bufferSize());
        ts.assertNoErrors();
        ts.assertComplete();
        int j = 1;
        for (Integer v : ts.values()) {
            Assert.assertEquals(j, v.intValue());
            j += 2;
        }
    }

    @Test
    public void rangeEmptyMixtureMaxConcurrent() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(0, 4 * Flowable.bufferSize()).flatMap(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return (v & 1) == 0 ? Flowable.<Integer>empty() : Flowable.range(v, 2);
            }
        }, 16).subscribe(ts);
        ts.assertValueCount(4 * Flowable.bufferSize());
        ts.assertNoErrors();
        ts.assertComplete();
        int j = 1;
        List<Integer> list = ts.values();
        for (int i = 0; i < list.size(); i += 2) {
            Assert.assertEquals(j, list.get(i).intValue());
            Assert.assertEquals(j + 1, list.get(i + 1).intValue());
            j += 2;
        }
    }

    @Test
    public void castCrashUnsubscribes() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = TestSubscriber.create();
        pp.flatMap(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer t) {
                throw new TestException();
            }
        }, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1;
            }
        }).subscribe(ts);
        Assert.assertTrue("Not subscribed?", pp.hasSubscribers());
        pp.onNext(1);
        Assert.assertFalse("Subscribed?", pp.hasSubscribers());
        ts.assertError(TestException.class);
    }

    @Test
    public void flatMapBiMapper() {
        Flowable.just(1).flatMap(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.just(v * 10);
            }
        }, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }, true).test().assertResult(11);
    }

    @Test
    public void flatMapBiMapperWithError() {
        Flowable.just(1).flatMap(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.just(v * 10).concatWith(Flowable.<Integer>error(new TestException()));
            }
        }, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }, true).test().assertFailure(TestException.class, 11);
    }

    @Test
    public void flatMapBiMapperMaxConcurrency() {
        Flowable.just(1, 2).flatMap(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.just(v * 10);
            }
        }, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }, true, 1).test().assertResult(11, 22);
    }

    @Test
    public void flatMapEmpty() {
        assertSame(Flowable.empty(), Flowable.empty().flatMap(new Function<Object, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Object v) throws Exception {
                return Flowable.just(v);
            }
        }));
    }

    @Test
    public void mergeScalar() {
        Flowable.merge(Flowable.just(Flowable.just(1))).test().assertResult(1);
    }

    @Test
    public void mergeScalar2() {
        Flowable.merge(Flowable.just(Flowable.just(1)).hide()).test().assertResult(1);
    }

    @Test
    public void mergeScalarEmpty() {
        Flowable.merge(Flowable.just(Flowable.empty()).hide()).test().assertResult();
    }

    @Test
    public void mergeScalarError() {
        Flowable.merge(Flowable.just(Flowable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw new TestException();
            }
        })).hide()).test().assertFailure(TestException.class);
    }

    @Test
    public void scalarReentrant() {
        final PublishProcessor<Flowable<Integer>> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    pp.onNext(Flowable.just(2));
                }
            }
        };
        Flowable.merge(pp).subscribe(ts);
        pp.onNext(Flowable.just(1));
        pp.onComplete();
        ts.assertResult(1, 2);
    }

    @Test
    public void scalarReentrant2() {
        final PublishProcessor<Flowable<Integer>> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    pp.onNext(Flowable.just(2));
                }
            }
        };
        Flowable.merge(pp, 2).subscribe(ts);
        pp.onNext(Flowable.just(1));
        pp.onComplete();
        ts.assertResult(1, 2);
    }

    @Test
    public void innerCompleteCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final TestSubscriber<Integer> ts = Flowable.merge(Flowable.just(pp)).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void fusedInnerThrows() {
        Flowable.just(1).hide().flatMap(new Function<Integer, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Integer v) throws Exception {
                return Flowable.range(1, 2).map(new Function<Integer, Object>() {

                    @Override
                    public Object apply(Integer w) throws Exception {
                        throw new TestException();
                    }
                });
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void fusedInnerThrows2() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 2).hide().flatMap(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) throws Exception {
                return Flowable.range(1, 2).map(new Function<Integer, Integer>() {

                    @Override
                    public Integer apply(Integer w) throws Exception {
                        throw new TestException();
                    }
                });
            }
        }, true).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.errorList(ts);
        TestHelper.assertError(errors, 0, TestException.class);
        TestHelper.assertError(errors, 1, TestException.class);
    }

    @Test
    public void scalarXMap() {
        Flowable.fromCallable(Functions.justCallable(1)).flatMap(Functions.justFunction(Flowable.fromCallable(Functions.justCallable(2)))).test().assertResult(2);
    }

    @Test
    public void noCrossBoundaryFusion() {
        for (int i = 0; i < 500; i++) {
            TestSubscriber<Object> ts = Flowable.merge(Flowable.just(1).observeOn(Schedulers.single()).map(new Function<Integer, Object>() {

                @Override
                public Object apply(Integer v) throws Exception {
                    return Thread.currentThread().getName().substring(0, 4);
                }
            }), Flowable.just(1).observeOn(Schedulers.computation()).map(new Function<Integer, Object>() {

                @Override
                public Object apply(Integer v) throws Exception {
                    return Thread.currentThread().getName().substring(0, 4);
                }
            })).test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(2);
            List<Object> list = ts.values();
            assertTrue(list.toString(), list.contains("RxSi"));
            assertTrue(list.toString(), list.contains("RxCo"));
        }
    }

    @Test
    public void cancelScalarDrainRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Flowable<Integer>> pp = PublishProcessor.create();
                final TestSubscriber<Integer> ts = pp.flatMap(Functions.<Flowable<Integer>>identity()).test(0);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ts.cancel();
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        pp.onComplete();
                    }
                };
                TestHelper.race(r1, r2);
                assertTrue(errors.toString(), errors.isEmpty());
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void cancelDrainRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            for (int j = 1; j < 50; j += 5) {
                List<Throwable> errors = TestHelper.trackPluginErrors();
                try {
                    final PublishProcessor<Flowable<Integer>> pp = PublishProcessor.create();
                    final TestSubscriber<Integer> ts = pp.flatMap(Functions.<Flowable<Integer>>identity()).test(0);
                    final PublishProcessor<Integer> just = PublishProcessor.create();
                    pp.onNext(just);
                    Runnable r1 = new Runnable() {

                        @Override
                        public void run() {
                            ts.request(1);
                            ts.cancel();
                        }
                    };
                    Runnable r2 = new Runnable() {

                        @Override
                        public void run() {
                            just.onNext(1);
                        }
                    };
                    TestHelper.race(r1, r2);
                    assertTrue(errors.toString(), errors.isEmpty());
                } finally {
                    RxJavaPlugins.reset();
                }
            }
        }
    }

    @Test
    public void iterableMapperFunctionReturnsNull() {
        Flowable.just(1).flatMapIterable(new Function<Integer, Iterable<Object>>() {

            @Override
            public Iterable<Object> apply(Integer v) throws Exception {
                return null;
            }
        }, new BiFunction<Integer, Object, Object>() {

            @Override
            public Object apply(Integer v, Object w) throws Exception {
                return v;
            }
        }).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(NullPointerException.class, "The mapper returned a null Iterable");
    }

    @Test
    public void combinerMapperFunctionReturnsNull() {
        Flowable.just(1).flatMap(new Function<Integer, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Integer v) throws Exception {
                return null;
            }
        }, new BiFunction<Integer, Object, Object>() {

            @Override
            public Object apply(Integer v, Object w) throws Exception {
                return v;
            }
        }).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(NullPointerException.class, "The mapper returned a null Publisher");
    }

    @Test
    public void failingFusedInnerCancelsSource() {
        final AtomicInteger counter = new AtomicInteger();
        Flowable.range(1, 5).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                counter.getAndIncrement();
            }
        }).flatMap(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.<Integer>fromIterable(new Iterable<Integer>() {

                    @Override
                    public Iterator<Integer> iterator() {
                        return new Iterator<Integer>() {

                            @Override
                            public boolean hasNext() {
                                return true;
                            }

                            @Override
                            public Integer next() {
                                throw new TestException();
                            }

                            @Override
                            public void remove() {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                });
            }
        }).test().assertFailure(TestException.class);
        assertEquals(1, counter.get());
    }

    @Test
    public void maxConcurrencySustained() {
        final PublishProcessor<Integer> pp1 = PublishProcessor.create();
        final PublishProcessor<Integer> pp2 = PublishProcessor.create();
        PublishProcessor<Integer> pp3 = PublishProcessor.create();
        PublishProcessor<Integer> pp4 = PublishProcessor.create();
        TestSubscriber<Integer> ts = Flowable.just(pp1, pp2, pp3, pp4).flatMap(new Function<PublishProcessor<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(PublishProcessor<Integer> v) throws Exception {
                return v;
            }
        }, 2).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                if (v == 1) {
                    // this will make sure the drain loop detects two completed
                    // inner sources and replaces them with fresh ones
                    pp1.onComplete();
                    pp2.onComplete();
                }
            }
        }).test();
        pp1.onNext(1);
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        assertTrue(pp3.hasSubscribers());
        assertTrue(pp4.hasSubscribers());
        ts.cancel();
        assertFalse(pp3.hasSubscribers());
        assertFalse(pp4.hasSubscribers());
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.flatMap(new Function<Integer, Publisher<Integer>>() {

                    @Override
                    public Publisher<Integer> apply(Integer v) throws Throwable {
                        return Flowable.just(v).hide();
                    }
                });
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayError() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.flatMap(new Function<Integer, Publisher<Integer>>() {

                    @Override
                    public Publisher<Integer> apply(Integer v) throws Throwable {
                        return Flowable.just(v).hide();
                    }
                }, true);
            }
        });
    }

    @Test
    public void mainErrorsInnerCancelled() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        pp1.flatMap(v -> pp2).test();
        pp1.onNext(1);
        assertTrue("No subscribers?", pp2.hasSubscribers());
        pp1.onError(new TestException());
        assertFalse("Has subscribers?", pp2.hasSubscribers());
    }

    @Test
    public void innerErrorsMainCancelled() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        pp1.flatMap(v -> pp2).test();
        pp1.onNext(1);
        assertTrue("No subscribers?", pp2.hasSubscribers());
        pp2.onError(new TestException());
        assertFalse("Has subscribers?", pp1.hasSubscribers());
    }

    @Test
    public void innerIsDisposed() {
        FlowableFlatMap.InnerSubscriber<Integer, Integer> inner = new FlowableFlatMap.InnerSubscriber<>(null, 10, 0L);
        assertFalse(inner.isDisposed());
        inner.dispose();
        assertTrue(inner.isDisposed());
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().flatMap(v -> Flowable.never()));
    }

    @Test
    public void signalsAfterMapperCrash() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(@NonNull Subscriber<? super @NonNull Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onNext(2);
                    s.onComplete();
                    s.onError(new IOException());
                }
            }.flatMap(v -> {
                throw new TestException();
            }).test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        });
    }

    @Test
    public void scalarQueueTerminate() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        pp.flatMap(v -> Flowable.just(v)).doOnNext(v -> {
            if (v == 1) {
                pp.onNext(2);
                pp.onNext(3);
            }
        }).take(2).subscribe(ts);
        pp.onNext(1);
        ts.assertResult(1, 2);
    }

    @Test
    public void scalarQueueCompleteMain() throws Exception {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        CountDownLatch cdl = new CountDownLatch(1);
        pp.flatMap(v -> Flowable.just(v)).doOnNext(v -> {
            if (v == 1) {
                pp.onNext(2);
                TestHelper.raceOther(() -> pp.onComplete(), cdl);
            }
        }).subscribe(ts);
        pp.onNext(1);
        cdl.await();
        ts.assertResult(1, 2);
    }

    @Test
    public void fusedInnerCrash() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = Flowable.just(pp, up.map(v -> {
            if (v == 10) {
                throw new TestException();
            }
            return v;
        }).compose(TestHelper.flowableStripBoundary())).flatMap(v -> v, true).doOnNext(v -> {
            if (v == 1) {
                pp.onNext(2);
                up.onNext(10);
            }
        }).test();
        pp.onNext(1);
        pp.onComplete();
        ts.assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void fusedInnerCrash2() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = Flowable.just(up.map(v -> {
            if (v == 10) {
                throw new TestException();
            }
            return v;
        }).compose(TestHelper.flowableStripBoundary()), pp).flatMap(v -> v, true).doOnNext(v -> {
            if (v == 1) {
                pp.onNext(2);
                up.onNext(10);
            }
        }).test();
        pp.onNext(1);
        pp.onComplete();
        ts.assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.flatMap(v -> Flowable.never()));
    }

    @Test
    public void allConcurrency() {
        Flowable.just(1).hide().flatMap(v -> Flowable.just(2).hide(), Integer.MAX_VALUE).test().assertResult(2);
    }

    @Test
    public void allConcurrencyScalarInner() {
        Flowable.just(1).hide().flatMap(v -> Flowable.just(2), Integer.MAX_VALUE).test().assertResult(2);
    }

    @Test
    public void allConcurrencyScalarInnerEmpty() {
        Flowable.just(1).hide().flatMap(v -> Flowable.empty(), Integer.MAX_VALUE).test().assertResult();
    }

    static final class ScalarEmptyCancel extends Flowable<Integer> implements Supplier<Integer> {

        final TestSubscriber<?> ts;

        ScalarEmptyCancel(TestSubscriber<?> ts) {
            this.ts = ts;
        }

        @Override
        @NonNull
        public Integer get() throws Throwable {
            ts.cancel();
            return null;
        }

        @Override
        protected void subscribeActual(@NonNull Subscriber<@NonNull ? super @NonNull Integer> subscriber) {
            EmptySubscription.complete(subscriber);
        }
    }

    @Test
    public void someConcurrencyScalarInnerCancel() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.just(1).hide().flatMap(v -> new ScalarEmptyCancel(ts)).subscribeWith(ts).assertEmpty();
    }

    @Test
    public void allConcurrencyBackpressured() {
        Flowable.just(1).hide().flatMap(v -> Flowable.just(2), Integer.MAX_VALUE).test(0L).assertEmpty().requestMore(1).assertResult(2);
    }

    @Test
    public void someConcurrencyInnerScalarCancel() {
        Flowable.just(1).hide().flatMap(v -> Flowable.just(2), 2).takeUntil(v -> true).test().assertResult(2);
    }

    @Test
    public void scalarInnerOuterOverflow() {
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(@NonNull Subscriber<@NonNull ? super @NonNull Integer> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                subscriber.onNext(1);
                subscriber.onNext(2);
                subscriber.onNext(3);
            }
        }.flatMap(v -> Flowable.just(v), 1).test(0L).assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void scalarInnerOuterOverflowSlowPath() {
        AtomicReference<Subscriber<? super Integer>> ref = new AtomicReference<>();
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(@NonNull Subscriber<@NonNull ? super @NonNull Integer> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                ref.set(subscriber);
                subscriber.onNext(1);
            }
        }.flatMap(v -> Flowable.just(v), 1).doOnNext(v -> {
            if (v == 1) {
                ref.get().onNext(2);
                ref.get().onNext(3);
            }
        }).test().assertFailure(MissingBackpressureException.class, 1);
    }

    @Test
    public void innerFastPathEmitOverflow() {
        Flowable.just(1).hide().flatMap(v -> new Flowable<Integer>() {

            @Override
            protected void subscribeActual(@NonNull Subscriber<@NonNull ? super @NonNull Integer> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                subscriber.onNext(1);
                subscriber.onNext(2);
                subscriber.onNext(3);
            }
        }, false, 1, 1).test(0L).assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void takeFromScalarQueue() {
        Flowable.just(1).hide().flatMap(v -> Flowable.just(2), 2).takeUntil(v -> true).test(0L).requestMore(2).assertResult(2);
    }

    @Test
    public void scalarInnerQueueEmpty() {
        Flowable.just(1).concatWith(Flowable.never()).hide().flatMap(v -> Flowable.just(2), 2).test(0L).requestMore(2).assertValuesOnly(2);
    }

    @Test
    public void innerCompletesAfterOnNextInDrainThenCancels() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        Flowable.just(1).hide().flatMap(v -> pp).doOnNext(v -> {
            if (v == 1) {
                pp.onComplete();
                ts.cancel();
            }
        }).subscribe(ts);
        pp.onNext(1);
        ts.requestMore(1).assertValuesOnly(1);
    }

    @Test(timeout = 5000)
    public void mixedScalarAsync() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            Flowable.range(0, 20).flatMap(integer -> {
                if (integer % 5 != 0) {
                    return Flowable.just(integer);
                }
                return Flowable.just(-integer).observeOn(Schedulers.computation());
            }, false, 1).ignoreElements().blockingAwait();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableFlatMapTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectionFunctionThrows() throws java.lang.Throwable {
            this.payloads.collectionFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resultFunctionThrows() throws java.lang.Throwable {
            this.payloads.resultFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeError() throws java.lang.Throwable {
            this.payloads.mergeError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapTransformsNormal() throws java.lang.Throwable {
            this.payloads.flatMapTransformsNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapTransformsException() throws java.lang.Throwable {
            this.payloads.flatMapTransformsException.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapTransformsOnNextFuncThrows() throws java.lang.Throwable {
            this.payloads.flatMapTransformsOnNextFuncThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapTransformsOnErrorFuncThrows() throws java.lang.Throwable {
            this.payloads.flatMapTransformsOnErrorFuncThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapTransformsOnCompletedFuncThrows() throws java.lang.Throwable {
            this.payloads.flatMapTransformsOnCompletedFuncThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapTransformsMergeException() throws java.lang.Throwable {
            this.payloads.flatMapTransformsMergeException.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapMaxConcurrent() throws java.lang.Throwable {
            this.payloads.flatMapMaxConcurrent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapSelectorMaxConcurrent() throws java.lang.Throwable {
            this.payloads.flatMapSelectorMaxConcurrent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapTransformsMaxConcurrentNormalLoop() throws java.lang.Throwable {
            this.payloads.flatMapTransformsMaxConcurrentNormalLoop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapTransformsMaxConcurrentNormal() throws java.lang.Throwable {
            this.payloads.flatMapTransformsMaxConcurrentNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapRangeMixedAsyncLoop() throws java.lang.Throwable {
            this.payloads.flatMapRangeMixedAsyncLoop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapIntPassthruAsync() throws java.lang.Throwable {
            this.payloads.flatMapIntPassthruAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapTwoNestedSync() throws java.lang.Throwable {
            this.payloads.flatMapTwoNestedSync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justEmptyMixture() throws java.lang.Throwable {
            this.payloads.justEmptyMixture.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeEmptyMixture() throws java.lang.Throwable {
            this.payloads.rangeEmptyMixture.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justEmptyMixtureMaxConcurrent() throws java.lang.Throwable {
            this.payloads.justEmptyMixtureMaxConcurrent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeEmptyMixtureMaxConcurrent() throws java.lang.Throwable {
            this.payloads.rangeEmptyMixtureMaxConcurrent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_castCrashUnsubscribes() throws java.lang.Throwable {
            this.payloads.castCrashUnsubscribes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapBiMapper() throws java.lang.Throwable {
            this.payloads.flatMapBiMapper.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapBiMapperWithError() throws java.lang.Throwable {
            this.payloads.flatMapBiMapperWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapBiMapperMaxConcurrency() throws java.lang.Throwable {
            this.payloads.flatMapBiMapperMaxConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapEmpty() throws java.lang.Throwable {
            this.payloads.flatMapEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeScalar() throws java.lang.Throwable {
            this.payloads.mergeScalar.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeScalar2() throws java.lang.Throwable {
            this.payloads.mergeScalar2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeScalarEmpty() throws java.lang.Throwable {
            this.payloads.mergeScalarEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeScalarError() throws java.lang.Throwable {
            this.payloads.mergeScalarError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarReentrant() throws java.lang.Throwable {
            this.payloads.scalarReentrant.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarReentrant2() throws java.lang.Throwable {
            this.payloads.scalarReentrant2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerCompleteCancelRace() throws java.lang.Throwable {
            this.payloads.innerCompleteCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedInnerThrows() throws java.lang.Throwable {
            this.payloads.fusedInnerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedInnerThrows2() throws java.lang.Throwable {
            this.payloads.fusedInnerThrows2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarXMap() throws java.lang.Throwable {
            this.payloads.scalarXMap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCrossBoundaryFusion() throws java.lang.Throwable {
            this.payloads.noCrossBoundaryFusion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelScalarDrainRace() throws java.lang.Throwable {
            this.payloads.cancelScalarDrainRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelDrainRace() throws java.lang.Throwable {
            this.payloads.cancelDrainRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableMapperFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.iterableMapperFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combinerMapperFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.combinerMapperFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failingFusedInnerCancelsSource() throws java.lang.Throwable {
            this.payloads.failingFusedInnerCancelsSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maxConcurrencySustained() throws java.lang.Throwable {
            this.payloads.maxConcurrencySustained.evaluate();
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
        public void benchmark_mainErrorsInnerCancelled() throws java.lang.Throwable {
            this.payloads.mainErrorsInnerCancelled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorsMainCancelled() throws java.lang.Throwable {
            this.payloads.innerErrorsMainCancelled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerIsDisposed() throws java.lang.Throwable {
            this.payloads.innerIsDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_signalsAfterMapperCrash() throws java.lang.Throwable {
            this.payloads.signalsAfterMapperCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarQueueTerminate() throws java.lang.Throwable {
            this.payloads.scalarQueueTerminate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarQueueCompleteMain() throws java.lang.Throwable {
            this.payloads.scalarQueueCompleteMain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedInnerCrash() throws java.lang.Throwable {
            this.payloads.fusedInnerCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedInnerCrash2() throws java.lang.Throwable {
            this.payloads.fusedInnerCrash2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allConcurrency() throws java.lang.Throwable {
            this.payloads.allConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allConcurrencyScalarInner() throws java.lang.Throwable {
            this.payloads.allConcurrencyScalarInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allConcurrencyScalarInnerEmpty() throws java.lang.Throwable {
            this.payloads.allConcurrencyScalarInnerEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_someConcurrencyScalarInnerCancel() throws java.lang.Throwable {
            this.payloads.someConcurrencyScalarInnerCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allConcurrencyBackpressured() throws java.lang.Throwable {
            this.payloads.allConcurrencyBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_someConcurrencyInnerScalarCancel() throws java.lang.Throwable {
            this.payloads.someConcurrencyInnerScalarCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarInnerOuterOverflow() throws java.lang.Throwable {
            this.payloads.scalarInnerOuterOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarInnerOuterOverflowSlowPath() throws java.lang.Throwable {
            this.payloads.scalarInnerOuterOverflowSlowPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerFastPathEmitOverflow() throws java.lang.Throwable {
            this.payloads.innerFastPathEmitOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeFromScalarQueue() throws java.lang.Throwable {
            this.payloads.takeFromScalarQueue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarInnerQueueEmpty() throws java.lang.Throwable {
            this.payloads.scalarInnerQueueEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerCompletesAfterOnNextInDrainThenCancels() throws java.lang.Throwable {
            this.payloads.innerCompletesAfterOnNextInDrainThenCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixedScalarAsync() throws java.lang.Throwable {
            this.payloads.mixedScalarAsync.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableFlatMapTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableFlatMapTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableFlatMapTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement collectionFunctionThrows;

            public org.junit.runners.model.Statement resultFunctionThrows;

            public org.junit.runners.model.Statement mergeError;

            public org.junit.runners.model.Statement flatMapTransformsNormal;

            public org.junit.runners.model.Statement flatMapTransformsException;

            public org.junit.runners.model.Statement flatMapTransformsOnNextFuncThrows;

            public org.junit.runners.model.Statement flatMapTransformsOnErrorFuncThrows;

            public org.junit.runners.model.Statement flatMapTransformsOnCompletedFuncThrows;

            public org.junit.runners.model.Statement flatMapTransformsMergeException;

            public org.junit.runners.model.Statement flatMapMaxConcurrent;

            public org.junit.runners.model.Statement flatMapSelectorMaxConcurrent;

            public org.junit.runners.model.Statement flatMapTransformsMaxConcurrentNormalLoop;

            public org.junit.runners.model.Statement flatMapTransformsMaxConcurrentNormal;

            public org.junit.runners.model.Statement flatMapRangeMixedAsyncLoop;

            public org.junit.runners.model.Statement flatMapIntPassthruAsync;

            public org.junit.runners.model.Statement flatMapTwoNestedSync;

            public org.junit.runners.model.Statement justEmptyMixture;

            public org.junit.runners.model.Statement rangeEmptyMixture;

            public org.junit.runners.model.Statement justEmptyMixtureMaxConcurrent;

            public org.junit.runners.model.Statement rangeEmptyMixtureMaxConcurrent;

            public org.junit.runners.model.Statement castCrashUnsubscribes;

            public org.junit.runners.model.Statement flatMapBiMapper;

            public org.junit.runners.model.Statement flatMapBiMapperWithError;

            public org.junit.runners.model.Statement flatMapBiMapperMaxConcurrency;

            public org.junit.runners.model.Statement flatMapEmpty;

            public org.junit.runners.model.Statement mergeScalar;

            public org.junit.runners.model.Statement mergeScalar2;

            public org.junit.runners.model.Statement mergeScalarEmpty;

            public org.junit.runners.model.Statement mergeScalarError;

            public org.junit.runners.model.Statement scalarReentrant;

            public org.junit.runners.model.Statement scalarReentrant2;

            public org.junit.runners.model.Statement innerCompleteCancelRace;

            public org.junit.runners.model.Statement fusedInnerThrows;

            public org.junit.runners.model.Statement fusedInnerThrows2;

            public org.junit.runners.model.Statement scalarXMap;

            public org.junit.runners.model.Statement noCrossBoundaryFusion;

            public org.junit.runners.model.Statement cancelScalarDrainRace;

            public org.junit.runners.model.Statement cancelDrainRace;

            public org.junit.runners.model.Statement iterableMapperFunctionReturnsNull;

            public org.junit.runners.model.Statement combinerMapperFunctionReturnsNull;

            public org.junit.runners.model.Statement failingFusedInnerCancelsSource;

            public org.junit.runners.model.Statement maxConcurrencySustained;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement mainErrorsInnerCancelled;

            public org.junit.runners.model.Statement innerErrorsMainCancelled;

            public org.junit.runners.model.Statement innerIsDisposed;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement signalsAfterMapperCrash;

            public org.junit.runners.model.Statement scalarQueueTerminate;

            public org.junit.runners.model.Statement scalarQueueCompleteMain;

            public org.junit.runners.model.Statement fusedInnerCrash;

            public org.junit.runners.model.Statement fusedInnerCrash2;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement allConcurrency;

            public org.junit.runners.model.Statement allConcurrencyScalarInner;

            public org.junit.runners.model.Statement allConcurrencyScalarInnerEmpty;

            public org.junit.runners.model.Statement someConcurrencyScalarInnerCancel;

            public org.junit.runners.model.Statement allConcurrencyBackpressured;

            public org.junit.runners.model.Statement someConcurrencyInnerScalarCancel;

            public org.junit.runners.model.Statement scalarInnerOuterOverflow;

            public org.junit.runners.model.Statement scalarInnerOuterOverflowSlowPath;

            public org.junit.runners.model.Statement innerFastPathEmitOverflow;

            public org.junit.runners.model.Statement takeFromScalarQueue;

            public org.junit.runners.model.Statement scalarInnerQueueEmpty;

            public org.junit.runners.model.Statement innerCompletesAfterOnNextInDrainThenCancels;

            public org.junit.runners.model.Statement mixedScalarAsync;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(FlowableFlatMapTest::normal, "normal", this);
            this.payloads.collectionFunctionThrows = _ClassStatement.forPayload(FlowableFlatMapTest::collectionFunctionThrows, "collectionFunctionThrows", this);
            this.payloads.resultFunctionThrows = _ClassStatement.forPayload(FlowableFlatMapTest::resultFunctionThrows, "resultFunctionThrows", this);
            this.payloads.mergeError = _ClassStatement.forPayload(FlowableFlatMapTest::mergeError, "mergeError", this);
            this.payloads.flatMapTransformsNormal = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapTransformsNormal, "flatMapTransformsNormal", this);
            this.payloads.flatMapTransformsException = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapTransformsException, "flatMapTransformsException", this);
            this.payloads.flatMapTransformsOnNextFuncThrows = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapTransformsOnNextFuncThrows, "flatMapTransformsOnNextFuncThrows", this);
            this.payloads.flatMapTransformsOnErrorFuncThrows = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapTransformsOnErrorFuncThrows, "flatMapTransformsOnErrorFuncThrows", this);
            this.payloads.flatMapTransformsOnCompletedFuncThrows = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapTransformsOnCompletedFuncThrows, "flatMapTransformsOnCompletedFuncThrows", this);
            this.payloads.flatMapTransformsMergeException = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapTransformsMergeException, "flatMapTransformsMergeException", this);
            this.payloads.flatMapMaxConcurrent = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapMaxConcurrent, "flatMapMaxConcurrent", this);
            this.payloads.flatMapSelectorMaxConcurrent = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapSelectorMaxConcurrent, "flatMapSelectorMaxConcurrent", this);
            this.payloads.flatMapTransformsMaxConcurrentNormalLoop = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapTransformsMaxConcurrentNormalLoop, "flatMapTransformsMaxConcurrentNormalLoop", this);
            this.payloads.flatMapTransformsMaxConcurrentNormal = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapTransformsMaxConcurrentNormal, "flatMapTransformsMaxConcurrentNormal", this);
            this.payloads.flatMapRangeMixedAsyncLoop = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapRangeMixedAsyncLoop, "flatMapRangeMixedAsyncLoop", this);
            this.payloads.flatMapIntPassthruAsync = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapIntPassthruAsync, "flatMapIntPassthruAsync", this);
            this.payloads.flatMapTwoNestedSync = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapTwoNestedSync, "flatMapTwoNestedSync", this);
            this.payloads.justEmptyMixture = _ClassStatement.forPayload(FlowableFlatMapTest::justEmptyMixture, "justEmptyMixture", this);
            this.payloads.rangeEmptyMixture = _ClassStatement.forPayload(FlowableFlatMapTest::rangeEmptyMixture, "rangeEmptyMixture", this);
            this.payloads.justEmptyMixtureMaxConcurrent = _ClassStatement.forPayload(FlowableFlatMapTest::justEmptyMixtureMaxConcurrent, "justEmptyMixtureMaxConcurrent", this);
            this.payloads.rangeEmptyMixtureMaxConcurrent = _ClassStatement.forPayload(FlowableFlatMapTest::rangeEmptyMixtureMaxConcurrent, "rangeEmptyMixtureMaxConcurrent", this);
            this.payloads.castCrashUnsubscribes = _ClassStatement.forPayload(FlowableFlatMapTest::castCrashUnsubscribes, "castCrashUnsubscribes", this);
            this.payloads.flatMapBiMapper = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapBiMapper, "flatMapBiMapper", this);
            this.payloads.flatMapBiMapperWithError = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapBiMapperWithError, "flatMapBiMapperWithError", this);
            this.payloads.flatMapBiMapperMaxConcurrency = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapBiMapperMaxConcurrency, "flatMapBiMapperMaxConcurrency", this);
            this.payloads.flatMapEmpty = _ClassStatement.forPayload(FlowableFlatMapTest::flatMapEmpty, "flatMapEmpty", this);
            this.payloads.mergeScalar = _ClassStatement.forPayload(FlowableFlatMapTest::mergeScalar, "mergeScalar", this);
            this.payloads.mergeScalar2 = _ClassStatement.forPayload(FlowableFlatMapTest::mergeScalar2, "mergeScalar2", this);
            this.payloads.mergeScalarEmpty = _ClassStatement.forPayload(FlowableFlatMapTest::mergeScalarEmpty, "mergeScalarEmpty", this);
            this.payloads.mergeScalarError = _ClassStatement.forPayload(FlowableFlatMapTest::mergeScalarError, "mergeScalarError", this);
            this.payloads.scalarReentrant = _ClassStatement.forPayload(FlowableFlatMapTest::scalarReentrant, "scalarReentrant", this);
            this.payloads.scalarReentrant2 = _ClassStatement.forPayload(FlowableFlatMapTest::scalarReentrant2, "scalarReentrant2", this);
            this.payloads.innerCompleteCancelRace = _ClassStatement.forPayload(FlowableFlatMapTest::innerCompleteCancelRace, "innerCompleteCancelRace", this);
            this.payloads.fusedInnerThrows = _ClassStatement.forPayload(FlowableFlatMapTest::fusedInnerThrows, "fusedInnerThrows", this);
            this.payloads.fusedInnerThrows2 = _ClassStatement.forPayload(FlowableFlatMapTest::fusedInnerThrows2, "fusedInnerThrows2", this);
            this.payloads.scalarXMap = _ClassStatement.forPayload(FlowableFlatMapTest::scalarXMap, "scalarXMap", this);
            this.payloads.noCrossBoundaryFusion = _ClassStatement.forPayload(FlowableFlatMapTest::noCrossBoundaryFusion, "noCrossBoundaryFusion", this);
            this.payloads.cancelScalarDrainRace = _ClassStatement.forPayload(FlowableFlatMapTest::cancelScalarDrainRace, "cancelScalarDrainRace", this);
            this.payloads.cancelDrainRace = _ClassStatement.forPayload(FlowableFlatMapTest::cancelDrainRace, "cancelDrainRace", this);
            this.payloads.iterableMapperFunctionReturnsNull = _ClassStatement.forPayload(FlowableFlatMapTest::iterableMapperFunctionReturnsNull, "iterableMapperFunctionReturnsNull", this);
            this.payloads.combinerMapperFunctionReturnsNull = _ClassStatement.forPayload(FlowableFlatMapTest::combinerMapperFunctionReturnsNull, "combinerMapperFunctionReturnsNull", this);
            this.payloads.failingFusedInnerCancelsSource = _ClassStatement.forPayload(FlowableFlatMapTest::failingFusedInnerCancelsSource, "failingFusedInnerCancelsSource", this);
            this.payloads.maxConcurrencySustained = _ClassStatement.forPayload(FlowableFlatMapTest::maxConcurrencySustained, "maxConcurrencySustained", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(FlowableFlatMapTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(FlowableFlatMapTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.mainErrorsInnerCancelled = _ClassStatement.forPayload(FlowableFlatMapTest::mainErrorsInnerCancelled, "mainErrorsInnerCancelled", this);
            this.payloads.innerErrorsMainCancelled = _ClassStatement.forPayload(FlowableFlatMapTest::innerErrorsMainCancelled, "innerErrorsMainCancelled", this);
            this.payloads.innerIsDisposed = _ClassStatement.forPayload(FlowableFlatMapTest::innerIsDisposed, "innerIsDisposed", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableFlatMapTest::badRequest, "badRequest", this);
            this.payloads.signalsAfterMapperCrash = _ClassStatement.forPayload(FlowableFlatMapTest::signalsAfterMapperCrash, "signalsAfterMapperCrash", this);
            this.payloads.scalarQueueTerminate = _ClassStatement.forPayload(FlowableFlatMapTest::scalarQueueTerminate, "scalarQueueTerminate", this);
            this.payloads.scalarQueueCompleteMain = _ClassStatement.forPayload(FlowableFlatMapTest::scalarQueueCompleteMain, "scalarQueueCompleteMain", this);
            this.payloads.fusedInnerCrash = _ClassStatement.forPayload(FlowableFlatMapTest::fusedInnerCrash, "fusedInnerCrash", this);
            this.payloads.fusedInnerCrash2 = _ClassStatement.forPayload(FlowableFlatMapTest::fusedInnerCrash2, "fusedInnerCrash2", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableFlatMapTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.allConcurrency = _ClassStatement.forPayload(FlowableFlatMapTest::allConcurrency, "allConcurrency", this);
            this.payloads.allConcurrencyScalarInner = _ClassStatement.forPayload(FlowableFlatMapTest::allConcurrencyScalarInner, "allConcurrencyScalarInner", this);
            this.payloads.allConcurrencyScalarInnerEmpty = _ClassStatement.forPayload(FlowableFlatMapTest::allConcurrencyScalarInnerEmpty, "allConcurrencyScalarInnerEmpty", this);
            this.payloads.someConcurrencyScalarInnerCancel = _ClassStatement.forPayload(FlowableFlatMapTest::someConcurrencyScalarInnerCancel, "someConcurrencyScalarInnerCancel", this);
            this.payloads.allConcurrencyBackpressured = _ClassStatement.forPayload(FlowableFlatMapTest::allConcurrencyBackpressured, "allConcurrencyBackpressured", this);
            this.payloads.someConcurrencyInnerScalarCancel = _ClassStatement.forPayload(FlowableFlatMapTest::someConcurrencyInnerScalarCancel, "someConcurrencyInnerScalarCancel", this);
            this.payloads.scalarInnerOuterOverflow = _ClassStatement.forPayload(FlowableFlatMapTest::scalarInnerOuterOverflow, "scalarInnerOuterOverflow", this);
            this.payloads.scalarInnerOuterOverflowSlowPath = _ClassStatement.forPayload(FlowableFlatMapTest::scalarInnerOuterOverflowSlowPath, "scalarInnerOuterOverflowSlowPath", this);
            this.payloads.innerFastPathEmitOverflow = _ClassStatement.forPayload(FlowableFlatMapTest::innerFastPathEmitOverflow, "innerFastPathEmitOverflow", this);
            this.payloads.takeFromScalarQueue = _ClassStatement.forPayload(FlowableFlatMapTest::takeFromScalarQueue, "takeFromScalarQueue", this);
            this.payloads.scalarInnerQueueEmpty = _ClassStatement.forPayload(FlowableFlatMapTest::scalarInnerQueueEmpty, "scalarInnerQueueEmpty", this);
            this.payloads.innerCompletesAfterOnNextInDrainThenCancels = _ClassStatement.forPayload(FlowableFlatMapTest::innerCompletesAfterOnNextInDrainThenCancels, "innerCompletesAfterOnNextInDrainThenCancels", this);
            this.payloads.mixedScalarAsync = _ClassStatement.forPayload(FlowableFlatMapTest::mixedScalarAsync, "mixedScalarAsync", this);
        }
    }
}
