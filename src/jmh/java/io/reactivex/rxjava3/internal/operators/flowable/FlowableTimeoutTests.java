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

import static io.reactivex.rxjava3.internal.util.ExceptionHelper.timeoutMessage;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableTimeoutTests extends RxJavaTest {

    private PublishProcessor<String> underlyingSubject;

    private TestScheduler testScheduler;

    private Flowable<String> withTimeout;

    private static final long TIMEOUT = 3;

    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    @Before
    public void setUp() {
        underlyingSubject = PublishProcessor.create();
        testScheduler = new TestScheduler();
        withTimeout = underlyingSubject.timeout(TIMEOUT, TIME_UNIT, testScheduler);
    }

    @Test
    public void shouldNotTimeoutIfOnNextWithinTimeout() {
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<String> ts = new TestSubscriber<>(subscriber);
        withTimeout.subscribe(ts);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("One");
        verify(subscriber).onNext("One");
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        verify(subscriber, never()).onError(any(Throwable.class));
        ts.cancel();
    }

    @Test
    public void shouldNotTimeoutIfSecondOnNextWithinTimeout() {
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<String> ts = new TestSubscriber<>(subscriber);
        withTimeout.subscribe(ts);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("One");
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("Two");
        verify(subscriber).onNext("Two");
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        verify(subscriber, never()).onError(any(Throwable.class));
        ts.cancel();
    }

    @Test
    public void shouldTimeoutIfOnNextNotWithinTimeout() {
        TestSubscriberEx<String> subscriber = new TestSubscriberEx<>();
        withTimeout.subscribe(subscriber);
        testScheduler.advanceTimeBy(TIMEOUT + 1, TimeUnit.SECONDS);
        subscriber.assertFailureAndMessage(TimeoutException.class, timeoutMessage(TIMEOUT, TIME_UNIT));
    }

    @Test
    public void shouldTimeoutIfSecondOnNextNotWithinTimeout() {
        TestSubscriberEx<String> subscriber = new TestSubscriberEx<>();
        TestSubscriber<String> ts = new TestSubscriber<>(subscriber);
        withTimeout.subscribe(subscriber);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("One");
        subscriber.assertValue("One");
        testScheduler.advanceTimeBy(TIMEOUT + 1, TimeUnit.SECONDS);
        subscriber.assertFailureAndMessage(TimeoutException.class, timeoutMessage(TIMEOUT, TIME_UNIT), "One");
        ts.cancel();
    }

    @Test
    public void shouldCompleteIfUnderlyingComletes() {
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<String> ts = new TestSubscriber<>(subscriber);
        withTimeout.subscribe(subscriber);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onComplete();
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        ts.cancel();
    }

    @Test
    public void shouldErrorIfUnderlyingErrors() {
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<String> ts = new TestSubscriber<>(subscriber);
        withTimeout.subscribe(subscriber);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onError(new UnsupportedOperationException());
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        verify(subscriber).onError(any(UnsupportedOperationException.class));
        ts.cancel();
    }

    @Test
    public void shouldSwitchToOtherIfOnNextNotWithinTimeout() {
        Flowable<String> other = Flowable.just("a", "b", "c");
        Flowable<String> source = underlyingSubject.timeout(TIMEOUT, TIME_UNIT, testScheduler, other);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<String> ts = new TestSubscriber<>(subscriber);
        source.subscribe(ts);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("One");
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS);
        underlyingSubject.onNext("Two");
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext("One");
        inOrder.verify(subscriber, times(1)).onNext("a");
        inOrder.verify(subscriber, times(1)).onNext("b");
        inOrder.verify(subscriber, times(1)).onNext("c");
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
        ts.cancel();
    }

    @Test
    public void shouldSwitchToOtherIfOnErrorNotWithinTimeout() {
        Flowable<String> other = Flowable.just("a", "b", "c");
        Flowable<String> source = underlyingSubject.timeout(TIMEOUT, TIME_UNIT, testScheduler, other);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<String> ts = new TestSubscriber<>(subscriber);
        source.subscribe(ts);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("One");
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS);
        underlyingSubject.onError(new UnsupportedOperationException());
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext("One");
        inOrder.verify(subscriber, times(1)).onNext("a");
        inOrder.verify(subscriber, times(1)).onNext("b");
        inOrder.verify(subscriber, times(1)).onNext("c");
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
        ts.cancel();
    }

    @Test
    public void shouldSwitchToOtherIfOnCompletedNotWithinTimeout() {
        Flowable<String> other = Flowable.just("a", "b", "c");
        Flowable<String> source = underlyingSubject.timeout(TIMEOUT, TIME_UNIT, testScheduler, other);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<String> ts = new TestSubscriber<>(subscriber);
        source.subscribe(ts);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("One");
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS);
        underlyingSubject.onComplete();
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext("One");
        inOrder.verify(subscriber, times(1)).onNext("a");
        inOrder.verify(subscriber, times(1)).onNext("b");
        inOrder.verify(subscriber, times(1)).onNext("c");
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
        ts.cancel();
    }

    @Test
    public void shouldSwitchToOtherAndCanBeUnsubscribedIfOnNextNotWithinTimeout() {
        PublishProcessor<String> other = PublishProcessor.create();
        Flowable<String> source = underlyingSubject.timeout(TIMEOUT, TIME_UNIT, testScheduler, other);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        TestSubscriber<String> ts = new TestSubscriber<>(subscriber);
        source.subscribe(ts);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("One");
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS);
        underlyingSubject.onNext("Two");
        other.onNext("a");
        other.onNext("b");
        ts.cancel();
        // The following messages should not be delivered.
        other.onNext("c");
        other.onNext("d");
        other.onComplete();
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext("One");
        inOrder.verify(subscriber, times(1)).onNext("a");
        inOrder.verify(subscriber, times(1)).onNext("b");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldTimeoutIfSynchronizedFlowableEmitFirstOnNextNotWithinTimeout() throws InterruptedException {
        final CountDownLatch exit = new CountDownLatch(1);
        final CountDownLatch timeoutSetuped = new CountDownLatch(1);
        final TestSubscriberEx<String> subscriber = new TestSubscriberEx<>();
        new Thread(new Runnable() {

            @Override
            public void run() {
                Flowable.unsafeCreate(new Publisher<String>() {

                    @Override
                    public void subscribe(Subscriber<? super String> subscriber) {
                        subscriber.onSubscribe(new BooleanSubscription());
                        try {
                            timeoutSetuped.countDown();
                            exit.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        subscriber.onNext("a");
                        subscriber.onComplete();
                    }
                }).timeout(1, TimeUnit.SECONDS, testScheduler).subscribe(subscriber);
            }
        }).start();
        timeoutSetuped.await();
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        subscriber.assertFailureAndMessage(TimeoutException.class, timeoutMessage(1, TimeUnit.SECONDS));
        // exit the thread
        exit.countDown();
    }

    @Test
    public void shouldUnsubscribeFromUnderlyingSubscriptionOnTimeout() throws InterruptedException {
        // From https://github.com/ReactiveX/RxJava/pull/951
        final Subscription s = mock(Subscription.class);
        Flowable<String> never = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(s);
            }
        });
        TestScheduler testScheduler = new TestScheduler();
        Flowable<String> observableWithTimeout = never.timeout(1000, TimeUnit.MILLISECONDS, testScheduler);
        TestSubscriberEx<String> subscriber = new TestSubscriberEx<>();
        observableWithTimeout.subscribe(subscriber);
        testScheduler.advanceTimeBy(2000, TimeUnit.MILLISECONDS);
        subscriber.assertFailureAndMessage(TimeoutException.class, timeoutMessage(1000, TimeUnit.MILLISECONDS));
        verify(s, times(1)).cancel();
    }

    @Test
    public void shouldUnsubscribeFromUnderlyingSubscriptionOnDispose() {
        final PublishProcessor<String> processor = PublishProcessor.create();
        final TestScheduler scheduler = new TestScheduler();
        final TestSubscriber<String> subscriber = processor.timeout(100, TimeUnit.MILLISECONDS, scheduler).test();
        assertTrue(processor.hasSubscribers());
        subscriber.cancel();
        assertFalse(processor.hasSubscribers());
    }

    @Test
    public void timedAndOther() {
        Flowable.never().timeout(100, TimeUnit.MILLISECONDS, Flowable.just(1)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishProcessor.create().timeout(1, TimeUnit.DAYS));
        TestHelper.checkDisposed(PublishProcessor.create().timeout(1, TimeUnit.DAYS, Flowable.just(1)));
    }

    @Test
    public void timedErrorOther() {
        Flowable.error(new TestException()).timeout(1, TimeUnit.DAYS, Flowable.just(1)).test().assertFailure(TestException.class);
    }

    @Test
    public void timedError() {
        Flowable.error(new TestException()).timeout(1, TimeUnit.DAYS).test().assertFailure(TestException.class);
    }

    @Test
    public void timedEmptyOther() {
        Flowable.empty().timeout(1, TimeUnit.DAYS, Flowable.just(1)).test().assertResult();
    }

    @Test
    public void timedEmpty() {
        Flowable.empty().timeout(1, TimeUnit.DAYS).test().assertResult();
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
                    subscriber.onComplete();
                    subscriber.onNext(2);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                }
            }.timeout(1, TimeUnit.DAYS).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSourceOther() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onComplete();
                    subscriber.onNext(2);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                }
            }.timeout(1, TimeUnit.DAYS, Flowable.just(3)).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void timedTake() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.timeout(1, TimeUnit.DAYS).take(1).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        assertFalse(pp.hasSubscribers());
        ts.assertResult(1);
    }

    @Test
    public void timedFallbackTake() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.timeout(1, TimeUnit.DAYS, Flowable.just(2)).take(1).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        assertFalse(pp.hasSubscribers());
        ts.assertResult(1);
    }

    @Test
    public void fallbackErrors() {
        Flowable.never().timeout(1, TimeUnit.MILLISECONDS, Flowable.error(new TestException())).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void onNextOnTimeoutRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestScheduler sch = new TestScheduler();
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriberEx<Integer> ts = pp.timeout(1, TimeUnit.SECONDS, sch).to(TestHelper.<Integer>testConsumer());
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sch.advanceTimeBy(1, TimeUnit.SECONDS);
                }
            };
            TestHelper.race(r1, r2);
            if (ts.values().size() != 0) {
                if (ts.errors().size() != 0) {
                    ts.assertFailure(TimeoutException.class, 1);
                    ts.assertErrorMessage(timeoutMessage(1, TimeUnit.SECONDS));
                } else {
                    ts.assertValuesOnly(1);
                }
            } else {
                ts.assertFailure(TimeoutException.class);
                ts.assertErrorMessage(timeoutMessage(1, TimeUnit.SECONDS));
            }
        }
    }

    @Test
    public void onNextOnTimeoutRaceFallback() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestScheduler sch = new TestScheduler();
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriberEx<Integer> ts = pp.timeout(1, TimeUnit.SECONDS, sch, Flowable.just(2)).to(TestHelper.<Integer>testConsumer());
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sch.advanceTimeBy(1, TimeUnit.SECONDS);
                }
            };
            TestHelper.race(r1, r2);
            if (ts.isTerminated()) {
                int c = ts.values().size();
                if (c == 1) {
                    int v = ts.values().get(0);
                    assertTrue("" + v, v == 1 || v == 2);
                } else {
                    ts.assertResult(1, 2);
                }
            } else {
                ts.assertValuesOnly(1);
            }
        }
    }

    @Test
    public void doubleOnSubscribeFallback() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.timeout(1, TimeUnit.MINUTES, Flowable.<Object>never()));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableTimeoutTests instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotTimeoutIfOnNextWithinTimeout() throws java.lang.Throwable {
            this.payloads.shouldNotTimeoutIfOnNextWithinTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotTimeoutIfSecondOnNextWithinTimeout() throws java.lang.Throwable {
            this.payloads.shouldNotTimeoutIfSecondOnNextWithinTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldTimeoutIfOnNextNotWithinTimeout() throws java.lang.Throwable {
            this.payloads.shouldTimeoutIfOnNextNotWithinTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldTimeoutIfSecondOnNextNotWithinTimeout() throws java.lang.Throwable {
            this.payloads.shouldTimeoutIfSecondOnNextNotWithinTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldCompleteIfUnderlyingComletes() throws java.lang.Throwable {
            this.payloads.shouldCompleteIfUnderlyingComletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldErrorIfUnderlyingErrors() throws java.lang.Throwable {
            this.payloads.shouldErrorIfUnderlyingErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldSwitchToOtherIfOnNextNotWithinTimeout() throws java.lang.Throwable {
            this.payloads.shouldSwitchToOtherIfOnNextNotWithinTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldSwitchToOtherIfOnErrorNotWithinTimeout() throws java.lang.Throwable {
            this.payloads.shouldSwitchToOtherIfOnErrorNotWithinTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldSwitchToOtherIfOnCompletedNotWithinTimeout() throws java.lang.Throwable {
            this.payloads.shouldSwitchToOtherIfOnCompletedNotWithinTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldSwitchToOtherAndCanBeUnsubscribedIfOnNextNotWithinTimeout() throws java.lang.Throwable {
            this.payloads.shouldSwitchToOtherAndCanBeUnsubscribedIfOnNextNotWithinTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldTimeoutIfSynchronizedFlowableEmitFirstOnNextNotWithinTimeout() throws java.lang.Throwable {
            this.payloads.shouldTimeoutIfSynchronizedFlowableEmitFirstOnNextNotWithinTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldUnsubscribeFromUnderlyingSubscriptionOnTimeout() throws java.lang.Throwable {
            this.payloads.shouldUnsubscribeFromUnderlyingSubscriptionOnTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldUnsubscribeFromUnderlyingSubscriptionOnDispose() throws java.lang.Throwable {
            this.payloads.shouldUnsubscribeFromUnderlyingSubscriptionOnDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedAndOther() throws java.lang.Throwable {
            this.payloads.timedAndOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedErrorOther() throws java.lang.Throwable {
            this.payloads.timedErrorOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedError() throws java.lang.Throwable {
            this.payloads.timedError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedEmptyOther() throws java.lang.Throwable {
            this.payloads.timedEmptyOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedEmpty() throws java.lang.Throwable {
            this.payloads.timedEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceOther() throws java.lang.Throwable {
            this.payloads.badSourceOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedTake() throws java.lang.Throwable {
            this.payloads.timedTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedFallbackTake() throws java.lang.Throwable {
            this.payloads.timedFallbackTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fallbackErrors() throws java.lang.Throwable {
            this.payloads.fallbackErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextOnTimeoutRace() throws java.lang.Throwable {
            this.payloads.onNextOnTimeoutRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextOnTimeoutRaceFallback() throws java.lang.Throwable {
            this.payloads.onNextOnTimeoutRaceFallback.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeFallback() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribeFallback.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeoutTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeoutTests> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.setUp();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeoutTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeoutTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableTimeoutTests();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimeoutTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableTimeoutTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableTimeoutTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement shouldNotTimeoutIfOnNextWithinTimeout;

            public org.junit.runners.model.Statement shouldNotTimeoutIfSecondOnNextWithinTimeout;

            public org.junit.runners.model.Statement shouldTimeoutIfOnNextNotWithinTimeout;

            public org.junit.runners.model.Statement shouldTimeoutIfSecondOnNextNotWithinTimeout;

            public org.junit.runners.model.Statement shouldCompleteIfUnderlyingComletes;

            public org.junit.runners.model.Statement shouldErrorIfUnderlyingErrors;

            public org.junit.runners.model.Statement shouldSwitchToOtherIfOnNextNotWithinTimeout;

            public org.junit.runners.model.Statement shouldSwitchToOtherIfOnErrorNotWithinTimeout;

            public org.junit.runners.model.Statement shouldSwitchToOtherIfOnCompletedNotWithinTimeout;

            public org.junit.runners.model.Statement shouldSwitchToOtherAndCanBeUnsubscribedIfOnNextNotWithinTimeout;

            public org.junit.runners.model.Statement shouldTimeoutIfSynchronizedFlowableEmitFirstOnNextNotWithinTimeout;

            public org.junit.runners.model.Statement shouldUnsubscribeFromUnderlyingSubscriptionOnTimeout;

            public org.junit.runners.model.Statement shouldUnsubscribeFromUnderlyingSubscriptionOnDispose;

            public org.junit.runners.model.Statement timedAndOther;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement timedErrorOther;

            public org.junit.runners.model.Statement timedError;

            public org.junit.runners.model.Statement timedEmptyOther;

            public org.junit.runners.model.Statement timedEmpty;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement badSourceOther;

            public org.junit.runners.model.Statement timedTake;

            public org.junit.runners.model.Statement timedFallbackTake;

            public org.junit.runners.model.Statement fallbackErrors;

            public org.junit.runners.model.Statement onNextOnTimeoutRace;

            public org.junit.runners.model.Statement onNextOnTimeoutRaceFallback;

            public org.junit.runners.model.Statement doubleOnSubscribeFallback;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.shouldNotTimeoutIfOnNextWithinTimeout = _ClassStatement.forPayload(FlowableTimeoutTests::shouldNotTimeoutIfOnNextWithinTimeout, "shouldNotTimeoutIfOnNextWithinTimeout", this);
            this.payloads.shouldNotTimeoutIfSecondOnNextWithinTimeout = _ClassStatement.forPayload(FlowableTimeoutTests::shouldNotTimeoutIfSecondOnNextWithinTimeout, "shouldNotTimeoutIfSecondOnNextWithinTimeout", this);
            this.payloads.shouldTimeoutIfOnNextNotWithinTimeout = _ClassStatement.forPayload(FlowableTimeoutTests::shouldTimeoutIfOnNextNotWithinTimeout, "shouldTimeoutIfOnNextNotWithinTimeout", this);
            this.payloads.shouldTimeoutIfSecondOnNextNotWithinTimeout = _ClassStatement.forPayload(FlowableTimeoutTests::shouldTimeoutIfSecondOnNextNotWithinTimeout, "shouldTimeoutIfSecondOnNextNotWithinTimeout", this);
            this.payloads.shouldCompleteIfUnderlyingComletes = _ClassStatement.forPayload(FlowableTimeoutTests::shouldCompleteIfUnderlyingComletes, "shouldCompleteIfUnderlyingComletes", this);
            this.payloads.shouldErrorIfUnderlyingErrors = _ClassStatement.forPayload(FlowableTimeoutTests::shouldErrorIfUnderlyingErrors, "shouldErrorIfUnderlyingErrors", this);
            this.payloads.shouldSwitchToOtherIfOnNextNotWithinTimeout = _ClassStatement.forPayload(FlowableTimeoutTests::shouldSwitchToOtherIfOnNextNotWithinTimeout, "shouldSwitchToOtherIfOnNextNotWithinTimeout", this);
            this.payloads.shouldSwitchToOtherIfOnErrorNotWithinTimeout = _ClassStatement.forPayload(FlowableTimeoutTests::shouldSwitchToOtherIfOnErrorNotWithinTimeout, "shouldSwitchToOtherIfOnErrorNotWithinTimeout", this);
            this.payloads.shouldSwitchToOtherIfOnCompletedNotWithinTimeout = _ClassStatement.forPayload(FlowableTimeoutTests::shouldSwitchToOtherIfOnCompletedNotWithinTimeout, "shouldSwitchToOtherIfOnCompletedNotWithinTimeout", this);
            this.payloads.shouldSwitchToOtherAndCanBeUnsubscribedIfOnNextNotWithinTimeout = _ClassStatement.forPayload(FlowableTimeoutTests::shouldSwitchToOtherAndCanBeUnsubscribedIfOnNextNotWithinTimeout, "shouldSwitchToOtherAndCanBeUnsubscribedIfOnNextNotWithinTimeout", this);
            this.payloads.shouldTimeoutIfSynchronizedFlowableEmitFirstOnNextNotWithinTimeout = _ClassStatement.forPayload(FlowableTimeoutTests::shouldTimeoutIfSynchronizedFlowableEmitFirstOnNextNotWithinTimeout, "shouldTimeoutIfSynchronizedFlowableEmitFirstOnNextNotWithinTimeout", this);
            this.payloads.shouldUnsubscribeFromUnderlyingSubscriptionOnTimeout = _ClassStatement.forPayload(FlowableTimeoutTests::shouldUnsubscribeFromUnderlyingSubscriptionOnTimeout, "shouldUnsubscribeFromUnderlyingSubscriptionOnTimeout", this);
            this.payloads.shouldUnsubscribeFromUnderlyingSubscriptionOnDispose = _ClassStatement.forPayload(FlowableTimeoutTests::shouldUnsubscribeFromUnderlyingSubscriptionOnDispose, "shouldUnsubscribeFromUnderlyingSubscriptionOnDispose", this);
            this.payloads.timedAndOther = _ClassStatement.forPayload(FlowableTimeoutTests::timedAndOther, "timedAndOther", this);
            this.payloads.disposed = _ClassStatement.forPayload(FlowableTimeoutTests::disposed, "disposed", this);
            this.payloads.timedErrorOther = _ClassStatement.forPayload(FlowableTimeoutTests::timedErrorOther, "timedErrorOther", this);
            this.payloads.timedError = _ClassStatement.forPayload(FlowableTimeoutTests::timedError, "timedError", this);
            this.payloads.timedEmptyOther = _ClassStatement.forPayload(FlowableTimeoutTests::timedEmptyOther, "timedEmptyOther", this);
            this.payloads.timedEmpty = _ClassStatement.forPayload(FlowableTimeoutTests::timedEmpty, "timedEmpty", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableTimeoutTests::badSource, "badSource", this);
            this.payloads.badSourceOther = _ClassStatement.forPayload(FlowableTimeoutTests::badSourceOther, "badSourceOther", this);
            this.payloads.timedTake = _ClassStatement.forPayload(FlowableTimeoutTests::timedTake, "timedTake", this);
            this.payloads.timedFallbackTake = _ClassStatement.forPayload(FlowableTimeoutTests::timedFallbackTake, "timedFallbackTake", this);
            this.payloads.fallbackErrors = _ClassStatement.forPayload(FlowableTimeoutTests::fallbackErrors, "fallbackErrors", this);
            this.payloads.onNextOnTimeoutRace = _ClassStatement.forPayload(FlowableTimeoutTests::onNextOnTimeoutRace, "onNextOnTimeoutRace", this);
            this.payloads.onNextOnTimeoutRaceFallback = _ClassStatement.forPayload(FlowableTimeoutTests::onNextOnTimeoutRaceFallback, "onNextOnTimeoutRaceFallback", this);
            this.payloads.doubleOnSubscribeFallback = _ClassStatement.forPayload(FlowableTimeoutTests::doubleOnSubscribeFallback, "doubleOnSubscribeFallback", this);
        }
    }
}
