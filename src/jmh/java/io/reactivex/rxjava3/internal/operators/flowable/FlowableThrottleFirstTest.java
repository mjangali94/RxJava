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
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableThrottleFirstTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttlingWithCompleted() throws java.lang.Throwable {
            this.payloads.throttlingWithCompleted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttlingWithError() throws java.lang.Throwable {
            this.payloads.throttlingWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttle() throws java.lang.Throwable {
            this.payloads.throttle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_throttleFirstDefaultScheduler() throws java.lang.Throwable {
            this.payloads.throttleFirstDefaultScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNoRequest() throws java.lang.Throwable {
            this.payloads.backpressureNoRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableThrottleFirstTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableThrottleFirstTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableThrottleFirstTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableThrottleFirstTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableThrottleFirstTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableThrottleFirstTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableThrottleFirstTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableThrottleFirstTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement throttlingWithCompleted;

            public org.junit.runners.model.Statement throttlingWithError;

            public org.junit.runners.model.Statement throttle;

            public org.junit.runners.model.Statement throttleFirstDefaultScheduler;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement backpressureNoRequest;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.throttlingWithCompleted = _ClassStatement.forPayload(FlowableThrottleFirstTest::throttlingWithCompleted, "throttlingWithCompleted", this);
            this.payloads.throttlingWithError = _ClassStatement.forPayload(FlowableThrottleFirstTest::throttlingWithError, "throttlingWithError", this);
            this.payloads.throttle = _ClassStatement.forPayload(FlowableThrottleFirstTest::throttle, "throttle", this);
            this.payloads.throttleFirstDefaultScheduler = _ClassStatement.forPayload(FlowableThrottleFirstTest::throttleFirstDefaultScheduler, "throttleFirstDefaultScheduler", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableThrottleFirstTest::dispose, "dispose", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableThrottleFirstTest::badSource, "badSource", this);
            this.payloads.backpressureNoRequest = _ClassStatement.forPayload(FlowableThrottleFirstTest::backpressureNoRequest, "backpressureNoRequest", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableThrottleFirstTest::badRequest, "badRequest", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableThrottleFirstTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
