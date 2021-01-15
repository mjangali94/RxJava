/**
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
package io.reactivex.rxjava3.internal.operators.single;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleFromSupplierTest extends RxJavaTest {

    @Test
    public void fromSupplierValue() {
        Single.fromSupplier(new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                return 5;
            }
        }).test().assertResult(5);
    }

    @Test
    public void fromSupplierError() {
        Single.fromSupplier(new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                throw new UnsupportedOperationException();
            }
        }).test().assertFailure(UnsupportedOperationException.class);
    }

    @Test
    public void fromSupplierNull() {
        Single.fromSupplier(new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                return null;
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(NullPointerException.class, "The supplier returned a null value");
    }

    @Test
    public void fromSupplierTwice() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Supplier<Integer> supplier = new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                return atomicInteger.incrementAndGet();
            }
        };
        Single.fromSupplier(supplier).test().assertResult(1);
        assertEquals(1, atomicInteger.get());
        Single.fromSupplier(supplier).test().assertResult(2);
        assertEquals(2, atomicInteger.get());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotInvokeFuncUntilSubscription() throws Throwable {
        Supplier<Object> func = mock(Supplier.class);
        when(func.get()).thenReturn(new Object());
        Single<Object> fromSupplierSingle = Single.fromSupplier(func);
        verifyNoInteractions(func);
        fromSupplierSingle.subscribe();
        verify(func).get();
    }

    @Test
    public void noErrorLoss() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final CountDownLatch cdl1 = new CountDownLatch(1);
            final CountDownLatch cdl2 = new CountDownLatch(1);
            TestObserver<Integer> to = Single.fromSupplier(new Supplier<Integer>() {

                @Override
                public Integer get() throws Exception {
                    cdl1.countDown();
                    cdl2.await(5, TimeUnit.SECONDS);
                    return 1;
                }
            }).subscribeOn(Schedulers.single()).test();
            assertTrue(cdl1.await(5, TimeUnit.SECONDS));
            to.dispose();
            int timeout = 10;
            while (timeout-- > 0 && errors.isEmpty()) {
                Thread.sleep(100);
            }
            TestHelper.assertUndeliverable(errors, 0, InterruptedException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission() throws Throwable {
        Supplier<String> func = mock(Supplier.class);
        final CountDownLatch funcLatch = new CountDownLatch(1);
        final CountDownLatch observerLatch = new CountDownLatch(1);
        when(func.get()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                observerLatch.countDown();
                try {
                    funcLatch.await();
                } catch (InterruptedException e) {
                    // It's okay, unsubscription causes Thread interruption
                    // Restoring interruption status of the Thread
                    Thread.currentThread().interrupt();
                }
                return "should_not_be_delivered";
            }
        });
        Single<String> fromSupplierObservable = Single.fromSupplier(func);
        Observer<Object> observer = TestHelper.mockObserver();
        TestObserver<String> outer = new TestObserver<>(observer);
        fromSupplierObservable.subscribeOn(Schedulers.computation()).subscribe(outer);
        // Wait until func will be invoked
        observerLatch.await();
        // Unsubscribing before emission
        outer.dispose();
        // Emitting result
        funcLatch.countDown();
        // func must be invoked
        verify(func).get();
        // Observer must not be notified at all
        verify(observer).onSubscribe(any(Disposable.class));
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void shouldAllowToThrowCheckedException() {
        final Exception checkedException = new Exception("test exception");
        Single<Object> fromSupplierObservable = Single.fromSupplier(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                throw checkedException;
            }
        });
        SingleObserver<Object> observer = TestHelper.mockSingleObserver();
        fromSupplierObservable.subscribe(observer);
        verify(observer).onSubscribe(any(Disposable.class));
        verify(observer).onError(checkedException);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void disposedOnArrival() {
        final int[] count = { 0 };
        Single.fromSupplier(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                count[0]++;
                return 1;
            }
        }).test(true).assertEmpty();
        assertEquals(0, count[0]);
    }

    @Test
    public void disposedOnCall() {
        final TestObserver<Integer> to = new TestObserver<>();
        Single.fromSupplier(new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                to.dispose();
                return 1;
            }
        }).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void toObservableTake() {
        Single.fromSupplier(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }).toObservable().take(1).test().assertResult(1);
    }

    @Test
    public void toObservableAndBack() {
        Single.fromSupplier(new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                return 1;
            }
        }).toObservable().singleOrError().test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class myBenchmark extends io.reactivex.rxjava3.core.RxJavaTest.myBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromSupplierValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromSupplierValue, this.description("fromSupplierValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromSupplierError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromSupplierError, this.description("fromSupplierError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromSupplierNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromSupplierNull, this.description("fromSupplierNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromSupplierTwice() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::fromSupplierTwice, this.description("fromSupplierTwice"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotInvokeFuncUntilSubscription() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotInvokeFuncUntilSubscription, this.description("shouldNotInvokeFuncUntilSubscription"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noErrorLoss() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::noErrorLoss, this.description("noErrorLoss"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission, this.description("shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldAllowToThrowCheckedException() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldAllowToThrowCheckedException, this.description("shouldAllowToThrowCheckedException"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedOnArrival() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposedOnArrival, this.description("disposedOnArrival"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedOnCall() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::disposedOnCall, this.description("disposedOnCall"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservableTake() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toObservableTake, this.description("toObservableTake"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservableAndBack() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toObservableAndBack, this.description("toObservableAndBack"));
        }

        private SingleFromSupplierTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleFromSupplierTest();
        }

        @java.lang.Override
        public SingleFromSupplierTest implementation() {
            return this.implementation;
        }
    }
}
