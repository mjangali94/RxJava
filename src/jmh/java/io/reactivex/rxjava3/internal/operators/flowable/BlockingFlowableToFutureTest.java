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
import java.util.*;
import java.util.concurrent.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;

public class BlockingFlowableToFutureTest {

    @Test
    public void toFuture() throws InterruptedException, ExecutionException {
        Flowable<String> obs = Flowable.just("one");
        Future<String> f = obs.toFuture();
        assertEquals("one", f.get());
    }

    @Test
    public void toFutureList() throws InterruptedException, ExecutionException {
        Flowable<String> obs = Flowable.just("one", "two", "three");
        Future<List<String>> f = obs.toList().toFuture();
        assertEquals("one", f.get().get(0));
        assertEquals("two", f.get().get(1));
        assertEquals("three", f.get().get(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void exceptionWithMoreThanOneElement() throws Throwable {
        Flowable<String> obs = Flowable.just("one", "two");
        Future<String> f = obs.toFuture();
        try {
            // we expect an exception since there are more than 1 element
            f.get();
            fail("Should have thrown!");
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void toFutureWithException() {
        Flowable<String> obs = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                subscriber.onNext("one");
                subscriber.onError(new TestException());
            }
        });
        Future<String> f = obs.toFuture();
        try {
            f.get();
            fail("expected exception");
        } catch (Throwable e) {
            assertEquals(TestException.class, e.getCause().getClass());
        }
    }

    @Test(expected = CancellationException.class)
    public void getAfterCancel() throws Exception {
        Flowable<String> obs = Flowable.never();
        Future<String> f = obs.toFuture();
        boolean cancelled = f.cancel(true);
        // because OperationNeverComplete never does
        assertTrue(cancelled);
        // Future.get() docs require this to throw
        f.get();
    }

    @Test(expected = CancellationException.class)
    public void getWithTimeoutAfterCancel() throws Exception {
        Flowable<String> obs = Flowable.never();
        Future<String> f = obs.toFuture();
        boolean cancelled = f.cancel(true);
        // because OperationNeverComplete never does
        assertTrue(cancelled);
        // Future.get() docs require this to throw
        f.get(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    @Test(expected = NoSuchElementException.class)
    public void getWithEmptyFlowable() throws Throwable {
        Flowable<String> obs = Flowable.empty();
        Future<String> f = obs.toFuture();
        try {
            f.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private BlockingFlowableToFutureTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFuture() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.toFuture);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFutureList() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.toFutureList);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exceptionWithMoreThanOneElement() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.exceptionWithMoreThanOneElement);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFutureWithException() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.toFutureWithException);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getAfterCancel() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.getAfterCancel);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getWithTimeoutAfterCancel() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.getWithTimeoutAfterCancel);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getWithEmptyFlowable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.getWithEmptyFlowable);
        }

        private void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableToFutureTest> payload) throws java.lang.Throwable {
            this.instance = new BlockingFlowableToFutureTest();
            payload.accept(this.instance);
        }

        private static class _Payloads {

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableToFutureTest> toFuture;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableToFutureTest> toFutureList;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableToFutureTest> exceptionWithMoreThanOneElement;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableToFutureTest> toFutureWithException;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableToFutureTest> getAfterCancel;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableToFutureTest> getWithTimeoutAfterCancel;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableToFutureTest> getWithEmptyFlowable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.toFuture = BlockingFlowableToFutureTest::toFuture;
            this.payloads.toFutureList = BlockingFlowableToFutureTest::toFutureList;
            this.payloads.exceptionWithMoreThanOneElement = new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableToFutureTest::exceptionWithMoreThanOneElement, java.lang.IndexOutOfBoundsException.class);
            this.payloads.toFutureWithException = BlockingFlowableToFutureTest::toFutureWithException;
            this.payloads.getAfterCancel = new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableToFutureTest::getAfterCancel, java.util.concurrent.CancellationException.class);
            this.payloads.getWithTimeoutAfterCancel = new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableToFutureTest::getWithTimeoutAfterCancel, java.util.concurrent.CancellationException.class);
            this.payloads.getWithEmptyFlowable = new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableToFutureTest::getWithEmptyFlowable, java.util.NoSuchElementException.class);
        }
    }
}
