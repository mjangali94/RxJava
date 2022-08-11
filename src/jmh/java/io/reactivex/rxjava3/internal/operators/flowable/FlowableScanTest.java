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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.flowable.*;
import io.reactivex.rxjava3.flowable.FlowableEventStream.Event;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableScanTest extends RxJavaTest {

    @Test
    public void scanIntegersWithInitialValue() {
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        Flowable<Integer> flowable = Flowable.just(1, 2, 3);
        Flowable<String> m = flowable.scan("", new BiFunction<String, Integer, String>() {

            @Override
            public String apply(String s, Integer n) {
                return s + n.toString();
            }
        });
        m.subscribe(subscriber);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onNext("");
        verify(subscriber, times(1)).onNext("1");
        verify(subscriber, times(1)).onNext("12");
        verify(subscriber, times(1)).onNext("123");
        verify(subscriber, times(4)).onNext(anyString());
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void scanIntegersWithoutInitialValue() {
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        Flowable<Integer> flowable = Flowable.just(1, 2, 3);
        Flowable<Integer> m = flowable.scan(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        });
        m.subscribe(subscriber);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, never()).onNext(0);
        verify(subscriber, times(1)).onNext(1);
        verify(subscriber, times(1)).onNext(3);
        verify(subscriber, times(1)).onNext(6);
        verify(subscriber, times(3)).onNext(anyInt());
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void scanIntegersWithoutInitialValueAndOnlyOneValue() {
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        Flowable<Integer> flowable = Flowable.just(1);
        Flowable<Integer> m = flowable.scan(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        });
        m.subscribe(subscriber);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, never()).onNext(0);
        verify(subscriber, times(1)).onNext(1);
        verify(subscriber, times(1)).onNext(anyInt());
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void shouldNotEmitUntilAfterSubscription() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 100).scan(0, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                // this will cause request(1) when 0 is emitted
                return t1 > 0;
            }
        }).subscribe(ts);
        assertEquals(100, ts.values().size());
    }

    @Test
    public void backpressureWithInitialValue() {
        final AtomicInteger count = new AtomicInteger();
        Flowable.range(1, 100).scan(0, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(10);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
                Assert.fail(e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onNext(Integer t) {
                count.incrementAndGet();
            }
        });
        // we only expect to receive 10 since we request(10)
        assertEquals(10, count.get());
    }

    @Test
    public void backpressureWithoutInitialValue() {
        final AtomicInteger count = new AtomicInteger();
        Flowable.range(1, 100).scan(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(10);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
                Assert.fail(e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onNext(Integer t) {
                count.incrementAndGet();
            }
        });
        // we only expect to receive 10 since we request(10)
        assertEquals(10, count.get());
    }

    @Test
    public void noBackpressureWithInitialValue() {
        final AtomicInteger count = new AtomicInteger();
        Flowable.range(1, 100).scan(0, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
                Assert.fail(e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onNext(Integer t) {
                count.incrementAndGet();
            }
        });
        // we only expect to receive 101 as we'll receive all 100 + the initial value
        assertEquals(101, count.get());
    }

    /**
     * This uses the public API collect which uses scan under the covers.
     */
    @Test
    public void seedFactory() {
        Single<List<Integer>> o = Flowable.range(1, 10).collect(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() {
                return new ArrayList<>();
            }
        }, new BiConsumer<List<Integer>, Integer>() {

            @Override
            public void accept(List<Integer> list, Integer t2) {
                list.add(t2);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), o.blockingGet());
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), o.blockingGet());
    }

    /**
     * This uses the public API collect which uses scan under the covers.
     */
    @Test
    public void seedFactoryFlowable() {
        Flowable<List<Integer>> f = Flowable.range(1, 10).collect(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() {
                return new ArrayList<>();
            }
        }, new BiConsumer<List<Integer>, Integer>() {

            @Override
            public void accept(List<Integer> list, Integer t2) {
                list.add(t2);
            }
        }).toFlowable().takeLast(1);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), f.blockingSingle());
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), f.blockingSingle());
    }

    @Test
    public void scanWithRequestOne() {
        Flowable<Integer> f = Flowable.just(1, 2).scan(0, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).take(1);
        TestSubscriberEx<Integer> subscriber = new TestSubscriberEx<>();
        f.subscribe(subscriber);
        subscriber.assertValue(0);
        subscriber.assertTerminated();
        subscriber.assertNoErrors();
    }

    @Test
    public void scanShouldNotRequestZero() {
        final AtomicReference<Subscription> producer = new AtomicReference<>();
        Flowable<Integer> f = Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(final Subscriber<? super Integer> subscriber) {
                Subscription p = spy(new Subscription() {

                    private AtomicBoolean requested = new AtomicBoolean(false);

                    @Override
                    public void request(long n) {
                        if (requested.compareAndSet(false, true)) {
                            subscriber.onNext(1);
                            subscriber.onComplete();
                        }
                    }

                    @Override
                    public void cancel() {
                    }
                });
                producer.set(p);
                subscriber.onSubscribe(p);
            }
        }).scan(100, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        });
        f.subscribe(new TestSubscriber<Integer>(1L) {

            @Override
            public void onNext(Integer integer) {
                request(1);
            }
        });
        verify(producer.get(), never()).request(0);
        verify(producer.get(), times(1)).request(Flowable.bufferSize() - 1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().scan(new BiFunction<Object, Object, Object>() {

            @Override
            public Object apply(Object a, Object b) throws Exception {
                return a;
            }
        }));
        TestHelper.checkDisposed(PublishProcessor.<Integer>create().scan(0, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.scan(new BiFunction<Object, Object, Object>() {

                    @Override
                    public Object apply(Object a, Object b) throws Exception {
                        return a;
                    }
                });
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.scan(0, new BiFunction<Object, Object, Object>() {

                    @Override
                    public Object apply(Object a, Object b) throws Exception {
                        return a;
                    }
                });
            }
        });
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).scan(new BiFunction<Object, Object, Object>() {

            @Override
            public Object apply(Object a, Object b) throws Exception {
                return a;
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void neverSource() {
        Flowable.<Integer>never().scan(0, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).test().assertValue(0).assertNoErrors().assertNotComplete();
    }

    @Test
    public void unsubscribeScan() {
        FlowableEventStream.getEventStream("HTTP-ClusterB", 20).scan(new HashMap<>(), new BiFunction<HashMap<String, String>, Event, HashMap<String, String>>() {

            @Override
            public HashMap<String, String> apply(HashMap<String, String> accum, Event perInstanceEvent) {
                accum.put("instance", perInstanceEvent.instanceId);
                return accum;
            }
        }).take(10).blockingForEach(new Consumer<HashMap<String, String>>() {

            @Override
            public void accept(HashMap<String, String> v) {
                System.out.println(v);
            }
        });
    }

    @Test
    public void scanWithSeedDoesNotEmitErrorTwiceIfScanFunctionThrows() {
        final List<Throwable> list = new CopyOnWriteArrayList<>();
        Consumer<Throwable> errorConsumer = new Consumer<Throwable>() {

            @Override
            public void accept(Throwable t) throws Exception {
                list.add(t);
            }
        };
        try {
            RxJavaPlugins.setErrorHandler(errorConsumer);
            final RuntimeException e = new RuntimeException();
            final RuntimeException e2 = new RuntimeException();
            Burst.items(1).error(e2).scan(0, throwingBiFunction(e)).test().assertValues(0).assertError(e);
            assertEquals("" + list, 1, list.size());
            assertTrue("" + list, list.get(0) instanceof UndeliverableException);
            assertEquals(e2, list.get(0).getCause());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void scanWithSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows() {
        final RuntimeException e = new RuntimeException();
        Burst.item(1).create().scan(0, throwingBiFunction(e)).test().assertValue(0).assertError(e);
    }

    @Test
    public void scanWithSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows() {
        final RuntimeException e = new RuntimeException();
        final AtomicInteger count = new AtomicInteger();
        Burst.items(1, 2).create().scan(0, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer n1, Integer n2) throws Exception {
                count.incrementAndGet();
                throw e;
            }
        }).test().assertValues(0).assertError(e);
        assertEquals(1, count.get());
    }

    @Test
    public void scanWithSeedCompletesNormally() {
        Flowable.just(1, 2, 3).scan(0, SUM).test().assertValues(0, 1, 3, 6).assertComplete();
    }

    @Test
    public void scanWithSeedWhenScanSeedProviderThrows() {
        final RuntimeException e = new RuntimeException();
        Flowable.just(1, 2, 3).scanWith(throwingSupplier(e), SUM).test().assertError(e).assertNoValues();
    }

    @Test
    public void scanNoSeed() {
        Flowable.just(1, 2, 3).scan(SUM).test().assertValues(1, 3, 6).assertComplete();
    }

    @Test
    public void scanNoSeedDoesNotEmitErrorTwiceIfScanFunctionThrows() {
        final List<Throwable> list = new CopyOnWriteArrayList<>();
        Consumer<Throwable> errorConsumer = new Consumer<Throwable>() {

            @Override
            public void accept(Throwable t) throws Exception {
                list.add(t);
            }
        };
        try {
            RxJavaPlugins.setErrorHandler(errorConsumer);
            final RuntimeException e = new RuntimeException();
            final RuntimeException e2 = new RuntimeException();
            Burst.items(1, 2).error(e2).scan(throwingBiFunction(e)).test().assertValue(1).assertError(e);
            assertEquals("" + list, 1, list.size());
            assertTrue("" + list, list.get(0) instanceof UndeliverableException);
            assertEquals(e2, list.get(0).getCause());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void scanNoSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows() {
        final RuntimeException e = new RuntimeException();
        Burst.items(1, 2).create().scan(throwingBiFunction(e)).test().assertValue(1).assertError(e);
    }

    @Test
    public void scanNoSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows() {
        final RuntimeException e = new RuntimeException();
        final AtomicInteger count = new AtomicInteger();
        Burst.items(1, 2, 3).create().scan(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer n1, Integer n2) throws Exception {
                count.incrementAndGet();
                throw e;
            }
        }).test().assertValue(1).assertError(e);
        assertEquals(1, count.get());
    }

    private static BiFunction<Integer, Integer, Integer> throwingBiFunction(final RuntimeException e) {
        return new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer n1, Integer n2) throws Exception {
                throw e;
            }
        };
    }

    private static final BiFunction<Integer, Integer, Integer> SUM = new BiFunction<Integer, Integer, Integer>() {

        @Override
        public Integer apply(Integer t1, Integer t2) throws Exception {
            return t1 + t2;
        }
    };

    private static Supplier<Integer> throwingSupplier(final RuntimeException e) {
        return new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                throw e;
            }
        };
    }

    @Test
    public void scanEmptyBackpressured() {
        Flowable.<Integer>empty().scan(0, SUM).test(1).assertResult(0);
    }

    @Test
    public void scanErrorBackpressured() {
        Flowable.<Integer>error(new TestException()).scan(0, SUM).test(0).assertFailure(TestException.class);
    }

    @Test
    public void scanTake() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                onComplete();
                cancel();
            }
        };
        Flowable.range(1, 10).scan(0, SUM).subscribe(ts);
        ts.assertResult(0);
    }

    @Test
    public void scanLong() {
        int n = 2 * Flowable.bufferSize();
        for (int b = 1; b <= n; b *= 2) {
            List<Integer> list = Flowable.range(1, n).scan(0, new BiFunction<Integer, Integer, Integer>() {

                @Override
                public Integer apply(Integer a, Integer b) throws Exception {
                    return b;
                }
            }).rebatchRequests(b).toList().blockingGet();
            for (int i = 0; i <= n; i++) {
                assertEquals(i, list.get(i).intValue());
            }
        }
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.<Integer>never().scanWith(() -> 1, (a, b) -> a + b));
    }

    @Test
    public void drainMoreWork() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.scanWith(() -> 0, (a, b) -> a + b).doOnNext(v -> {
            if (v == 1) {
                pp.onNext(2);
                pp.onComplete();
            }
        }).test();
        pp.onNext(1);
        ts.assertResult(0, 1, 3);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableScanTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanIntegersWithInitialValue() throws java.lang.Throwable {
            this.payloads.scanIntegersWithInitialValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanIntegersWithoutInitialValue() throws java.lang.Throwable {
            this.payloads.scanIntegersWithoutInitialValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanIntegersWithoutInitialValueAndOnlyOneValue() throws java.lang.Throwable {
            this.payloads.scanIntegersWithoutInitialValueAndOnlyOneValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotEmitUntilAfterSubscription() throws java.lang.Throwable {
            this.payloads.shouldNotEmitUntilAfterSubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithInitialValue() throws java.lang.Throwable {
            this.payloads.backpressureWithInitialValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithoutInitialValue() throws java.lang.Throwable {
            this.payloads.backpressureWithoutInitialValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noBackpressureWithInitialValue() throws java.lang.Throwable {
            this.payloads.noBackpressureWithInitialValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_seedFactory() throws java.lang.Throwable {
            this.payloads.seedFactory.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_seedFactoryFlowable() throws java.lang.Throwable {
            this.payloads.seedFactoryFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanWithRequestOne() throws java.lang.Throwable {
            this.payloads.scanWithRequestOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanShouldNotRequestZero() throws java.lang.Throwable {
            this.payloads.scanShouldNotRequestZero.evaluate();
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
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverSource() throws java.lang.Throwable {
            this.payloads.neverSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeScan() throws java.lang.Throwable {
            this.payloads.unsubscribeScan.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanWithSeedDoesNotEmitErrorTwiceIfScanFunctionThrows() throws java.lang.Throwable {
            this.payloads.scanWithSeedDoesNotEmitErrorTwiceIfScanFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanWithSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows() throws java.lang.Throwable {
            this.payloads.scanWithSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanWithSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows() throws java.lang.Throwable {
            this.payloads.scanWithSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanWithSeedCompletesNormally() throws java.lang.Throwable {
            this.payloads.scanWithSeedCompletesNormally.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanWithSeedWhenScanSeedProviderThrows() throws java.lang.Throwable {
            this.payloads.scanWithSeedWhenScanSeedProviderThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanNoSeed() throws java.lang.Throwable {
            this.payloads.scanNoSeed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanNoSeedDoesNotEmitErrorTwiceIfScanFunctionThrows() throws java.lang.Throwable {
            this.payloads.scanNoSeedDoesNotEmitErrorTwiceIfScanFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanNoSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows() throws java.lang.Throwable {
            this.payloads.scanNoSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanNoSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows() throws java.lang.Throwable {
            this.payloads.scanNoSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanEmptyBackpressured() throws java.lang.Throwable {
            this.payloads.scanEmptyBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanErrorBackpressured() throws java.lang.Throwable {
            this.payloads.scanErrorBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanTake() throws java.lang.Throwable {
            this.payloads.scanTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scanLong() throws java.lang.Throwable {
            this.payloads.scanLong.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_drainMoreWork() throws java.lang.Throwable {
            this.payloads.drainMoreWork.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableScanTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableScanTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableScanTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableScanTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableScanTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableScanTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableScanTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableScanTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement scanIntegersWithInitialValue;

            public org.junit.runners.model.Statement scanIntegersWithoutInitialValue;

            public org.junit.runners.model.Statement scanIntegersWithoutInitialValueAndOnlyOneValue;

            public org.junit.runners.model.Statement shouldNotEmitUntilAfterSubscription;

            public org.junit.runners.model.Statement backpressureWithInitialValue;

            public org.junit.runners.model.Statement backpressureWithoutInitialValue;

            public org.junit.runners.model.Statement noBackpressureWithInitialValue;

            public org.junit.runners.model.Statement seedFactory;

            public org.junit.runners.model.Statement seedFactoryFlowable;

            public org.junit.runners.model.Statement scanWithRequestOne;

            public org.junit.runners.model.Statement scanShouldNotRequestZero;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement neverSource;

            public org.junit.runners.model.Statement unsubscribeScan;

            public org.junit.runners.model.Statement scanWithSeedDoesNotEmitErrorTwiceIfScanFunctionThrows;

            public org.junit.runners.model.Statement scanWithSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows;

            public org.junit.runners.model.Statement scanWithSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows;

            public org.junit.runners.model.Statement scanWithSeedCompletesNormally;

            public org.junit.runners.model.Statement scanWithSeedWhenScanSeedProviderThrows;

            public org.junit.runners.model.Statement scanNoSeed;

            public org.junit.runners.model.Statement scanNoSeedDoesNotEmitErrorTwiceIfScanFunctionThrows;

            public org.junit.runners.model.Statement scanNoSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows;

            public org.junit.runners.model.Statement scanNoSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows;

            public org.junit.runners.model.Statement scanEmptyBackpressured;

            public org.junit.runners.model.Statement scanErrorBackpressured;

            public org.junit.runners.model.Statement scanTake;

            public org.junit.runners.model.Statement scanLong;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement drainMoreWork;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.scanIntegersWithInitialValue = _ClassStatement.forPayload(FlowableScanTest::scanIntegersWithInitialValue, "scanIntegersWithInitialValue", this);
            this.payloads.scanIntegersWithoutInitialValue = _ClassStatement.forPayload(FlowableScanTest::scanIntegersWithoutInitialValue, "scanIntegersWithoutInitialValue", this);
            this.payloads.scanIntegersWithoutInitialValueAndOnlyOneValue = _ClassStatement.forPayload(FlowableScanTest::scanIntegersWithoutInitialValueAndOnlyOneValue, "scanIntegersWithoutInitialValueAndOnlyOneValue", this);
            this.payloads.shouldNotEmitUntilAfterSubscription = _ClassStatement.forPayload(FlowableScanTest::shouldNotEmitUntilAfterSubscription, "shouldNotEmitUntilAfterSubscription", this);
            this.payloads.backpressureWithInitialValue = _ClassStatement.forPayload(FlowableScanTest::backpressureWithInitialValue, "backpressureWithInitialValue", this);
            this.payloads.backpressureWithoutInitialValue = _ClassStatement.forPayload(FlowableScanTest::backpressureWithoutInitialValue, "backpressureWithoutInitialValue", this);
            this.payloads.noBackpressureWithInitialValue = _ClassStatement.forPayload(FlowableScanTest::noBackpressureWithInitialValue, "noBackpressureWithInitialValue", this);
            this.payloads.seedFactory = _ClassStatement.forPayload(FlowableScanTest::seedFactory, "seedFactory", this);
            this.payloads.seedFactoryFlowable = _ClassStatement.forPayload(FlowableScanTest::seedFactoryFlowable, "seedFactoryFlowable", this);
            this.payloads.scanWithRequestOne = _ClassStatement.forPayload(FlowableScanTest::scanWithRequestOne, "scanWithRequestOne", this);
            this.payloads.scanShouldNotRequestZero = _ClassStatement.forPayload(FlowableScanTest::scanShouldNotRequestZero, "scanShouldNotRequestZero", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableScanTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableScanTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableScanTest::error, "error", this);
            this.payloads.neverSource = _ClassStatement.forPayload(FlowableScanTest::neverSource, "neverSource", this);
            this.payloads.unsubscribeScan = _ClassStatement.forPayload(FlowableScanTest::unsubscribeScan, "unsubscribeScan", this);
            this.payloads.scanWithSeedDoesNotEmitErrorTwiceIfScanFunctionThrows = _ClassStatement.forPayload(FlowableScanTest::scanWithSeedDoesNotEmitErrorTwiceIfScanFunctionThrows, "scanWithSeedDoesNotEmitErrorTwiceIfScanFunctionThrows", this);
            this.payloads.scanWithSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows = _ClassStatement.forPayload(FlowableScanTest::scanWithSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows, "scanWithSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows", this);
            this.payloads.scanWithSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows = _ClassStatement.forPayload(FlowableScanTest::scanWithSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows, "scanWithSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows", this);
            this.payloads.scanWithSeedCompletesNormally = _ClassStatement.forPayload(FlowableScanTest::scanWithSeedCompletesNormally, "scanWithSeedCompletesNormally", this);
            this.payloads.scanWithSeedWhenScanSeedProviderThrows = _ClassStatement.forPayload(FlowableScanTest::scanWithSeedWhenScanSeedProviderThrows, "scanWithSeedWhenScanSeedProviderThrows", this);
            this.payloads.scanNoSeed = _ClassStatement.forPayload(FlowableScanTest::scanNoSeed, "scanNoSeed", this);
            this.payloads.scanNoSeedDoesNotEmitErrorTwiceIfScanFunctionThrows = _ClassStatement.forPayload(FlowableScanTest::scanNoSeedDoesNotEmitErrorTwiceIfScanFunctionThrows, "scanNoSeedDoesNotEmitErrorTwiceIfScanFunctionThrows", this);
            this.payloads.scanNoSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows = _ClassStatement.forPayload(FlowableScanTest::scanNoSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows, "scanNoSeedDoesNotEmitTerminalEventTwiceIfScanFunctionThrows", this);
            this.payloads.scanNoSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows = _ClassStatement.forPayload(FlowableScanTest::scanNoSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows, "scanNoSeedDoesNotProcessOnNextAfterTerminalEventIfScanFunctionThrows", this);
            this.payloads.scanEmptyBackpressured = _ClassStatement.forPayload(FlowableScanTest::scanEmptyBackpressured, "scanEmptyBackpressured", this);
            this.payloads.scanErrorBackpressured = _ClassStatement.forPayload(FlowableScanTest::scanErrorBackpressured, "scanErrorBackpressured", this);
            this.payloads.scanTake = _ClassStatement.forPayload(FlowableScanTest::scanTake, "scanTake", this);
            this.payloads.scanLong = _ClassStatement.forPayload(FlowableScanTest::scanLong, "scanLong", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableScanTest::badRequest, "badRequest", this);
            this.payloads.drainMoreWork = _ClassStatement.forPayload(FlowableScanTest::drainMoreWork, "drainMoreWork", this);
        }
    }
}
