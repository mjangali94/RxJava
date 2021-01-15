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
package io.reactivex.rxjava3.internal.operators.flowable;

import static io.reactivex.rxjava3.core.BackpressureOverflowStrategy.*;
import static io.reactivex.rxjava3.internal.functions.Functions.EMPTY_ACTION;
import static org.junit.Assert.assertEquals;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableOnBackpressureBufferStrategyTest extends RxJavaTest {

    @Test
    public void backpressureWithBufferDropOldest() throws InterruptedException {
        int bufferSize = 3;
        final AtomicInteger droppedCount = new AtomicInteger(0);
        Action incrementOnDrop = new Action() {

            @Override
            public void run() throws Exception {
                droppedCount.incrementAndGet();
            }
        };
        TestSubscriber<Long> ts = createTestSubscriber();
        Flowable.fromPublisher(send500ValuesAndComplete.onBackpressureBuffer(bufferSize, incrementOnDrop, DROP_OLDEST)).subscribe(ts);
        // we request 10 but only 3 should come from the buffer
        ts.request(10);
        ts.awaitDone(5, TimeUnit.SECONDS);
        assertEquals(bufferSize, ts.values().size());
        ts.assertNoErrors();
        assertEquals(497, ts.values().get(0).intValue());
        assertEquals(498, ts.values().get(1).intValue());
        assertEquals(499, ts.values().get(2).intValue());
        assertEquals(droppedCount.get(), 500 - bufferSize);
    }

    private TestSubscriber<Long> createTestSubscriber() {
        return new TestSubscriber<>(new DefaultSubscriber<Long>() {

            @Override
            protected void onStart() {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Long t) {
            }
        }, 0L);
    }

    @Test
    public void backpressureWithBufferDropLatest() throws InterruptedException {
        int bufferSize = 3;
        final AtomicInteger droppedCount = new AtomicInteger(0);
        Action incrementOnDrop = new Action() {

            @Override
            public void run() throws Exception {
                droppedCount.incrementAndGet();
            }
        };
        TestSubscriber<Long> ts = createTestSubscriber();
        Flowable.fromPublisher(send500ValuesAndComplete.onBackpressureBuffer(bufferSize, incrementOnDrop, DROP_LATEST)).subscribe(ts);
        // we request 10 but only 3 should come from the buffer
        ts.request(10);
        ts.awaitDone(5, TimeUnit.SECONDS);
        assertEquals(bufferSize, ts.values().size());
        ts.assertNoErrors();
        assertEquals(0, ts.values().get(0).intValue());
        assertEquals(1, ts.values().get(1).intValue());
        assertEquals(499, ts.values().get(2).intValue());
        assertEquals(droppedCount.get(), 500 - bufferSize);
    }

    private static final Flowable<Long> send500ValuesAndComplete = Flowable.unsafeCreate(new Publisher<Long>() {

        @Override
        public void subscribe(Subscriber<? super Long> s) {
            BooleanSubscription bs = new BooleanSubscription();
            s.onSubscribe(bs);
            long i = 0;
            while (!bs.isCancelled() && i < 500) {
                s.onNext(i++);
            }
            if (!bs.isCancelled()) {
                s.onComplete();
            }
        }
    });

    @Test(expected = IllegalArgumentException.class)
    public void backpressureBufferNegativeCapacity() throws InterruptedException {
        Flowable.empty().onBackpressureBuffer(-1, EMPTY_ACTION, DROP_OLDEST);
    }

    @Test(expected = IllegalArgumentException.class)
    public void backpressureBufferZeroCapacity() throws InterruptedException {
        Flowable.empty().onBackpressureBuffer(0, EMPTY_ACTION, DROP_OLDEST);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).onBackpressureBuffer(16, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR));
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).onBackpressureBuffer(16, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR).test().assertFailure(TestException.class);
    }

    @Test
    public void overflowError() {
        Flowable.range(1, 20).onBackpressureBuffer(8, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR).test(0L).assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Object>, Object>() {

            @Override
            public Object apply(Flowable<Object> f) throws Exception {
                return f.onBackpressureBuffer(8, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR);
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.onBackpressureBuffer(8, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR);
            }
        });
    }

    @Test
    public void overflowCrashes() {
        Flowable.range(1, 20).onBackpressureBuffer(8, new Action() {

            @Override
            public void run() throws Exception {
                throw new TestException();
            }
        }, BackpressureOverflowStrategy.DROP_OLDEST).test(0L).assertFailure(TestException.class);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.just(1).onBackpressureBuffer(16, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR));
    }

    @Test
    public void empty() {
        Flowable.empty().onBackpressureBuffer(16, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR).test(0L).assertResult();
    }

    @Test
    public void justTake() {
        Flowable.just(1).onBackpressureBuffer(16, Functions.EMPTY_ACTION, BackpressureOverflowStrategy.ERROR).take(1).test().assertResult(1);
    }

    @Test
    public void overflowNullAction() {
        Flowable.range(1, 5).onBackpressureBuffer(1, null, BackpressureOverflowStrategy.DROP_OLDEST).test(0L).assertEmpty();
    }

    @Test
    public void cancelOnDrain() {
        Flowable.range(1, 5).onBackpressureBuffer(10, null, BackpressureOverflowStrategy.DROP_OLDEST).takeUntil(v -> true).test(0L).assertEmpty().requestMore(10).assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class myBenchmark extends io.reactivex.rxjava3.core.RxJavaTest.myBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithBufferDropOldest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureWithBufferDropOldest, this.description("backpressureWithBufferDropOldest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithBufferDropLatest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureWithBufferDropLatest, this.description("backpressureWithBufferDropLatest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureBufferNegativeCapacity() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::backpressureBufferNegativeCapacity, this.description("backpressureBufferNegativeCapacity"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureBufferZeroCapacity() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::backpressureBufferZeroCapacity, this.description("backpressureBufferZeroCapacity"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overflowError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::overflowError, this.description("overflowError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource, this.description("badSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overflowCrashes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::overflowCrashes, this.description("overflowCrashes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justTake() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justTake, this.description("justTake"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overflowNullAction() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::overflowNullAction, this.description("overflowNullAction"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOnDrain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::cancelOnDrain, this.description("cancelOnDrain"));
        }

        private FlowableOnBackpressureBufferStrategyTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableOnBackpressureBufferStrategyTest();
        }

        @java.lang.Override
        public FlowableOnBackpressureBufferStrategyTest implementation() {
            return this.implementation;
        }
    }
}
