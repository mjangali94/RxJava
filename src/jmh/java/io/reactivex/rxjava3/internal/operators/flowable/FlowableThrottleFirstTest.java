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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableThrottleFirstTest extends RxJavaTest {

    private TestScheduler scheduler;

    private Scheduler.Worker innerScheduler;

    private Subscriber<String> subscriber;

    @Before
    public void before() {
        scheduler = new TestScheduler();
        innerScheduler = scheduler.createWorker();
        subscriber = TestHelper.mockSubscriber();
    }

    @Test
    public void throttlingWithCompleted() {
        Flowable<String> source = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                // publish as it's first
                publishNext(subscriber, 100, "one");
                // skip as it's last within the first 400
                publishNext(subscriber, 300, "two");
                // publish
                publishNext(subscriber, 900, "three");
                // skip
                publishNext(subscriber, 905, "four");
                // Should be published as soon as the timeout expires.
                publishCompleted(subscriber, 1000);
            }
        });
        Flowable<String> sampled = source.throttleFirst(400, TimeUnit.MILLISECONDS, scheduler);
        sampled.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        scheduler.advanceTimeTo(1000, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext("one");
        inOrder.verify(subscriber, times(0)).onNext("two");
        inOrder.verify(subscriber, times(1)).onNext("three");
        inOrder.verify(subscriber, times(0)).onNext("four");
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void throttlingWithError() {
        Flowable<String> source = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                Exception error = new TestException();
                // Should be published since it is first
                publishNext(subscriber, 100, "one");
                // Should be skipped since onError will arrive before the timeout expires
                publishNext(subscriber, 200, "two");
                // Should be published as soon as the timeout expires.
                publishError(subscriber, 300, error);
            }
        });
        Flowable<String> sampled = source.throttleFirst(400, TimeUnit.MILLISECONDS, scheduler);
        sampled.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        scheduler.advanceTimeTo(400, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber).onNext("one");
        inOrder.verify(subscriber).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
    }

    private <T> void publishCompleted(final Subscriber<T> subscriber, long delay) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                subscriber.onComplete();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private <T> void publishError(final Subscriber<T> subscriber, long delay, final Exception error) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                subscriber.onError(error);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private <T> void publishNext(final Subscriber<T> subscriber, long delay, final T value) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                subscriber.onNext(value);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    @Test
    public void throttle() {
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        TestScheduler s = new TestScheduler();
        PublishProcessor<Integer> o = PublishProcessor.create();
        o.throttleFirst(500, TimeUnit.MILLISECONDS, s).subscribe(subscriber);
        // send events with simulated time increments
        s.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        // deliver
        o.onNext(1);
        // skip
        o.onNext(2);
        s.advanceTimeTo(501, TimeUnit.MILLISECONDS);
        // deliver
        o.onNext(3);
        s.advanceTimeTo(600, TimeUnit.MILLISECONDS);
        // skip
        o.onNext(4);
        s.advanceTimeTo(700, TimeUnit.MILLISECONDS);
        // skip
        o.onNext(5);
        // skip
        o.onNext(6);
        s.advanceTimeTo(1001, TimeUnit.MILLISECONDS);
        // deliver
        o.onNext(7);
        s.advanceTimeTo(1501, TimeUnit.MILLISECONDS);
        o.onComplete();
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onNext(1);
        inOrder.verify(subscriber).onNext(3);
        inOrder.verify(subscriber).onNext(7);
        inOrder.verify(subscriber).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void throttleFirstDefaultScheduler() {
        Flowable.just(1).throttleFirst(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).throttleFirst(1, TimeUnit.DAYS));
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onNext(2);
                    subscriber.onComplete();
                    subscriber.onNext(3);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                }
            }.throttleFirst(1, TimeUnit.DAYS).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void backpressureNoRequest() {
        Flowable.range(1, 3).throttleFirst(1, TimeUnit.MINUTES).test(0L).assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().throttleFirst(1, TimeUnit.MINUTES));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.throttleFirst(1, TimeUnit.MINUTES));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class myBenchmark extends io.reactivex.rxjava3.core.RxJavaTest.myBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttlingWithCompleted() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::throttlingWithCompleted, this.description("throttlingWithCompleted"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttlingWithError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::throttlingWithError, this.description("throttlingWithError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttle() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::throttle, this.description("throttle"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttleFirstDefaultScheduler() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::throttleFirstDefaultScheduler, this.description("throttleFirstDefaultScheduler"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dispose, this.description("dispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badSource, this.description("badSource"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNoRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::backpressureNoRequest, this.description("backpressureNoRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::badRequest, this.description("badRequest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        private FlowableThrottleFirstTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableThrottleFirstTest();
        }

        @java.lang.Override
        public FlowableThrottleFirstTest implementation() {
            return this.implementation;
        }
    }
}
