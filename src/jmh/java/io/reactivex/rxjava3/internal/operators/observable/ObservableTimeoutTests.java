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
package io.reactivex.rxjava3.internal.operators.observable;

import static io.reactivex.rxjava3.internal.util.ExceptionHelper.timeoutMessage;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.*;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableTimeoutTests extends RxJavaTest {

    private PublishSubject<String> underlyingSubject;

    private TestScheduler testScheduler;

    private Observable<String> withTimeout;

    private static final long TIMEOUT = 3;

    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    @Before
    public void setUp() {
        underlyingSubject = PublishSubject.create();
        testScheduler = new TestScheduler();
        withTimeout = underlyingSubject.timeout(TIMEOUT, TIME_UNIT, testScheduler);
    }

    @Test
    public void shouldNotTimeoutIfOnNextWithinTimeout() {
        Observer<String> observer = TestHelper.mockObserver();
        TestObserver<String> to = new TestObserver<>(observer);
        withTimeout.subscribe(to);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("One");
        verify(observer).onNext("One");
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        verify(observer, never()).onError(any(Throwable.class));
        to.dispose();
    }

    @Test
    public void shouldNotTimeoutIfSecondOnNextWithinTimeout() {
        Observer<String> observer = TestHelper.mockObserver();
        TestObserver<String> to = new TestObserver<>(observer);
        withTimeout.subscribe(to);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("One");
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("Two");
        verify(observer).onNext("Two");
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        verify(observer, never()).onError(any(Throwable.class));
        to.dispose();
    }

    @Test
    public void shouldTimeoutIfOnNextNotWithinTimeout() {
        TestObserverEx<String> observer = new TestObserverEx<>();
        withTimeout.subscribe(observer);
        testScheduler.advanceTimeBy(TIMEOUT + 1, TimeUnit.SECONDS);
        observer.assertFailureAndMessage(TimeoutException.class, timeoutMessage(TIMEOUT, TIME_UNIT));
    }

    @Test
    public void shouldTimeoutIfSecondOnNextNotWithinTimeout() {
        TestObserverEx<String> observer = new TestObserverEx<>();
        withTimeout.subscribe(observer);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("One");
        observer.assertValue("One");
        testScheduler.advanceTimeBy(TIMEOUT + 1, TimeUnit.SECONDS);
        observer.assertFailureAndMessage(TimeoutException.class, timeoutMessage(TIMEOUT, TIME_UNIT), "One");
    }

    @Test
    public void shouldCompleteIfUnderlyingComletes() {
        Observer<String> observer = TestHelper.mockObserver();
        TestObserver<String> to = new TestObserver<>(observer);
        withTimeout.subscribe(observer);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onComplete();
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        verify(observer).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        to.dispose();
    }

    @Test
    public void shouldErrorIfUnderlyingErrors() {
        Observer<String> observer = TestHelper.mockObserver();
        TestObserver<String> to = new TestObserver<>(observer);
        withTimeout.subscribe(observer);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onError(new UnsupportedOperationException());
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        verify(observer).onError(any(UnsupportedOperationException.class));
        to.dispose();
    }

    @Test
    public void shouldSwitchToOtherIfOnNextNotWithinTimeout() {
        Observable<String> other = Observable.just("a", "b", "c");
        Observable<String> source = underlyingSubject.timeout(TIMEOUT, TIME_UNIT, testScheduler, other);
        Observer<String> observer = TestHelper.mockObserver();
        TestObserver<String> to = new TestObserver<>(observer);
        source.subscribe(to);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("One");
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS);
        underlyingSubject.onNext("Two");
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext("One");
        inOrder.verify(observer, times(1)).onNext("a");
        inOrder.verify(observer, times(1)).onNext("b");
        inOrder.verify(observer, times(1)).onNext("c");
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
        to.dispose();
    }

    @Test
    public void shouldSwitchToOtherIfOnErrorNotWithinTimeout() {
        Observable<String> other = Observable.just("a", "b", "c");
        Observable<String> source = underlyingSubject.timeout(TIMEOUT, TIME_UNIT, testScheduler, other);
        Observer<String> observer = TestHelper.mockObserver();
        TestObserver<String> to = new TestObserver<>(observer);
        source.subscribe(to);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("One");
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS);
        underlyingSubject.onError(new UnsupportedOperationException());
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext("One");
        inOrder.verify(observer, times(1)).onNext("a");
        inOrder.verify(observer, times(1)).onNext("b");
        inOrder.verify(observer, times(1)).onNext("c");
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
        to.dispose();
    }

    @Test
    public void shouldSwitchToOtherIfOnCompletedNotWithinTimeout() {
        Observable<String> other = Observable.just("a", "b", "c");
        Observable<String> source = underlyingSubject.timeout(TIMEOUT, TIME_UNIT, testScheduler, other);
        Observer<String> observer = TestHelper.mockObserver();
        TestObserver<String> to = new TestObserver<>(observer);
        source.subscribe(to);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("One");
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS);
        underlyingSubject.onComplete();
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext("One");
        inOrder.verify(observer, times(1)).onNext("a");
        inOrder.verify(observer, times(1)).onNext("b");
        inOrder.verify(observer, times(1)).onNext("c");
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
        to.dispose();
    }

    @Test
    public void shouldSwitchToOtherAndCanBeUnsubscribedIfOnNextNotWithinTimeout() {
        PublishSubject<String> other = PublishSubject.create();
        Observable<String> source = underlyingSubject.timeout(TIMEOUT, TIME_UNIT, testScheduler, other);
        Observer<String> observer = TestHelper.mockObserver();
        TestObserver<String> to = new TestObserver<>(observer);
        source.subscribe(to);
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        underlyingSubject.onNext("One");
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS);
        underlyingSubject.onNext("Two");
        other.onNext("a");
        other.onNext("b");
        to.dispose();
        // The following messages should not be delivered.
        other.onNext("c");
        other.onNext("d");
        other.onComplete();
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext("One");
        inOrder.verify(observer, times(1)).onNext("a");
        inOrder.verify(observer, times(1)).onNext("b");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldTimeoutIfSynchronizedObservableEmitFirstOnNextNotWithinTimeout() throws InterruptedException {
        final CountDownLatch exit = new CountDownLatch(1);
        final CountDownLatch timeoutSetuped = new CountDownLatch(1);
        final TestObserverEx<String> observer = new TestObserverEx<>();
        new Thread(new Runnable() {

            @Override
            public void run() {
                Observable.unsafeCreate(new ObservableSource<String>() {

                    @Override
                    public void subscribe(Observer<? super String> observer) {
                        observer.onSubscribe(Disposable.empty());
                        try {
                            timeoutSetuped.countDown();
                            exit.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        observer.onNext("a");
                        observer.onComplete();
                    }
                }).timeout(1, TimeUnit.SECONDS, testScheduler).subscribe(observer);
            }
        }).start();
        timeoutSetuped.await();
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        observer.assertFailureAndMessage(TimeoutException.class, timeoutMessage(1, TimeUnit.SECONDS));
        // exit the thread
        exit.countDown();
    }

    @Test
    public void shouldUnsubscribeFromUnderlyingSubscriptionOnTimeout() throws InterruptedException {
        // From https://github.com/ReactiveX/RxJava/pull/951
        final Disposable upstream = mock(Disposable.class);
        Observable<String> never = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> observer) {
                observer.onSubscribe(upstream);
            }
        });
        TestScheduler testScheduler = new TestScheduler();
        Observable<String> observableWithTimeout = never.timeout(1000, TimeUnit.MILLISECONDS, testScheduler);
        TestObserverEx<String> observer = new TestObserverEx<>();
        observableWithTimeout.subscribe(observer);
        testScheduler.advanceTimeBy(2000, TimeUnit.MILLISECONDS);
        observer.assertFailureAndMessage(TimeoutException.class, timeoutMessage(1000, TimeUnit.MILLISECONDS));
        verify(upstream, times(1)).dispose();
    }

    @Test
    public void shouldUnsubscribeFromUnderlyingSubscriptionOnDispose() {
        final PublishSubject<String> subject = PublishSubject.create();
        final TestScheduler scheduler = new TestScheduler();
        final TestObserver<String> observer = subject.timeout(100, TimeUnit.MILLISECONDS, scheduler).test();
        assertTrue(subject.hasObservers());
        observer.dispose();
        assertFalse(subject.hasObservers());
    }

    @Test
    public void timedAndOther() {
        Observable.never().timeout(100, TimeUnit.MILLISECONDS, Observable.just(1)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishSubject.create().timeout(1, TimeUnit.DAYS));
        TestHelper.checkDisposed(PublishSubject.create().timeout(1, TimeUnit.DAYS, Observable.just(1)));
    }

    @Test
    public void timedErrorOther() {
        Observable.error(new TestException()).timeout(1, TimeUnit.DAYS, Observable.just(1)).test().assertFailure(TestException.class);
    }

    @Test
    public void timedError() {
        Observable.error(new TestException()).timeout(1, TimeUnit.DAYS).test().assertFailure(TestException.class);
    }

    @Test
    public void timedEmptyOther() {
        Observable.empty().timeout(1, TimeUnit.DAYS, Observable.just(1)).test().assertResult();
    }

    @Test
    public void timedEmpty() {
        Observable.empty().timeout(1, TimeUnit.DAYS).test().assertResult();
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onComplete();
                    observer.onNext(2);
                    observer.onError(new TestException());
                    observer.onComplete();
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
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onComplete();
                    observer.onNext(2);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.timeout(1, TimeUnit.DAYS, Observable.just(3)).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void timedTake() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.timeout(1, TimeUnit.DAYS).take(1).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        assertFalse(ps.hasObservers());
        to.assertResult(1);
    }

    @Test
    public void timedFallbackTake() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.timeout(1, TimeUnit.DAYS, Observable.just(2)).take(1).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        assertFalse(ps.hasObservers());
        to.assertResult(1);
    }

    @Test
    public void fallbackErrors() {
        Observable.never().timeout(1, TimeUnit.MILLISECONDS, Observable.error(new TestException())).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void onNextOnTimeoutRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestScheduler sch = new TestScheduler();
            final PublishSubject<Integer> ps = PublishSubject.create();
            TestObserverEx<Integer> to = ps.timeout(1, TimeUnit.SECONDS, sch).to(TestHelper.<Integer>testConsumer());
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sch.advanceTimeBy(1, TimeUnit.SECONDS);
                }
            };
            TestHelper.race(r1, r2);
            if (to.values().size() != 0) {
                if (to.errors().size() != 0) {
                    to.assertFailure(TimeoutException.class, 1);
                    to.assertErrorMessage(timeoutMessage(1, TimeUnit.SECONDS));
                } else {
                    to.assertValuesOnly(1);
                }
            } else {
                to.assertFailure(TimeoutException.class);
                to.assertErrorMessage(timeoutMessage(1, TimeUnit.SECONDS));
            }
        }
    }

    @Test
    public void onNextOnTimeoutRaceFallback() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestScheduler sch = new TestScheduler();
            final PublishSubject<Integer> ps = PublishSubject.create();
            TestObserverEx<Integer> to = ps.timeout(1, TimeUnit.SECONDS, sch, Observable.just(2)).to(TestHelper.<Integer>testConsumer());
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sch.advanceTimeBy(1, TimeUnit.SECONDS);
                }
            };
            TestHelper.race(r1, r2);
            if (to.isTerminated()) {
                int c = to.values().size();
                if (c == 1) {
                    int v = to.values().get(0);
                    assertTrue("" + v, v == 1 || v == 2);
                } else {
                    to.assertResult(1, 2);
                }
            } else {
                to.assertValuesOnly(1);
            }
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableTimeoutTests instance;

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
        public void benchmark_shouldTimeoutIfSynchronizedObservableEmitFirstOnNextNotWithinTimeout() throws java.lang.Throwable {
            this.payloads.shouldTimeoutIfSynchronizedObservableEmitFirstOnNextNotWithinTimeout.evaluate();
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

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTimeoutTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTimeoutTests> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTimeoutTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTimeoutTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableTimeoutTests();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTimeoutTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableTimeoutTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableTimeoutTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

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

            public org.junit.runners.model.Statement shouldTimeoutIfSynchronizedObservableEmitFirstOnNextNotWithinTimeout;

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
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.shouldNotTimeoutIfOnNextWithinTimeout = _ClassStatement.forPayload(ObservableTimeoutTests::shouldNotTimeoutIfOnNextWithinTimeout, "shouldNotTimeoutIfOnNextWithinTimeout", this);
            this.payloads.shouldNotTimeoutIfSecondOnNextWithinTimeout = _ClassStatement.forPayload(ObservableTimeoutTests::shouldNotTimeoutIfSecondOnNextWithinTimeout, "shouldNotTimeoutIfSecondOnNextWithinTimeout", this);
            this.payloads.shouldTimeoutIfOnNextNotWithinTimeout = _ClassStatement.forPayload(ObservableTimeoutTests::shouldTimeoutIfOnNextNotWithinTimeout, "shouldTimeoutIfOnNextNotWithinTimeout", this);
            this.payloads.shouldTimeoutIfSecondOnNextNotWithinTimeout = _ClassStatement.forPayload(ObservableTimeoutTests::shouldTimeoutIfSecondOnNextNotWithinTimeout, "shouldTimeoutIfSecondOnNextNotWithinTimeout", this);
            this.payloads.shouldCompleteIfUnderlyingComletes = _ClassStatement.forPayload(ObservableTimeoutTests::shouldCompleteIfUnderlyingComletes, "shouldCompleteIfUnderlyingComletes", this);
            this.payloads.shouldErrorIfUnderlyingErrors = _ClassStatement.forPayload(ObservableTimeoutTests::shouldErrorIfUnderlyingErrors, "shouldErrorIfUnderlyingErrors", this);
            this.payloads.shouldSwitchToOtherIfOnNextNotWithinTimeout = _ClassStatement.forPayload(ObservableTimeoutTests::shouldSwitchToOtherIfOnNextNotWithinTimeout, "shouldSwitchToOtherIfOnNextNotWithinTimeout", this);
            this.payloads.shouldSwitchToOtherIfOnErrorNotWithinTimeout = _ClassStatement.forPayload(ObservableTimeoutTests::shouldSwitchToOtherIfOnErrorNotWithinTimeout, "shouldSwitchToOtherIfOnErrorNotWithinTimeout", this);
            this.payloads.shouldSwitchToOtherIfOnCompletedNotWithinTimeout = _ClassStatement.forPayload(ObservableTimeoutTests::shouldSwitchToOtherIfOnCompletedNotWithinTimeout, "shouldSwitchToOtherIfOnCompletedNotWithinTimeout", this);
            this.payloads.shouldSwitchToOtherAndCanBeUnsubscribedIfOnNextNotWithinTimeout = _ClassStatement.forPayload(ObservableTimeoutTests::shouldSwitchToOtherAndCanBeUnsubscribedIfOnNextNotWithinTimeout, "shouldSwitchToOtherAndCanBeUnsubscribedIfOnNextNotWithinTimeout", this);
            this.payloads.shouldTimeoutIfSynchronizedObservableEmitFirstOnNextNotWithinTimeout = _ClassStatement.forPayload(ObservableTimeoutTests::shouldTimeoutIfSynchronizedObservableEmitFirstOnNextNotWithinTimeout, "shouldTimeoutIfSynchronizedObservableEmitFirstOnNextNotWithinTimeout", this);
            this.payloads.shouldUnsubscribeFromUnderlyingSubscriptionOnTimeout = _ClassStatement.forPayload(ObservableTimeoutTests::shouldUnsubscribeFromUnderlyingSubscriptionOnTimeout, "shouldUnsubscribeFromUnderlyingSubscriptionOnTimeout", this);
            this.payloads.shouldUnsubscribeFromUnderlyingSubscriptionOnDispose = _ClassStatement.forPayload(ObservableTimeoutTests::shouldUnsubscribeFromUnderlyingSubscriptionOnDispose, "shouldUnsubscribeFromUnderlyingSubscriptionOnDispose", this);
            this.payloads.timedAndOther = _ClassStatement.forPayload(ObservableTimeoutTests::timedAndOther, "timedAndOther", this);
            this.payloads.disposed = _ClassStatement.forPayload(ObservableTimeoutTests::disposed, "disposed", this);
            this.payloads.timedErrorOther = _ClassStatement.forPayload(ObservableTimeoutTests::timedErrorOther, "timedErrorOther", this);
            this.payloads.timedError = _ClassStatement.forPayload(ObservableTimeoutTests::timedError, "timedError", this);
            this.payloads.timedEmptyOther = _ClassStatement.forPayload(ObservableTimeoutTests::timedEmptyOther, "timedEmptyOther", this);
            this.payloads.timedEmpty = _ClassStatement.forPayload(ObservableTimeoutTests::timedEmpty, "timedEmpty", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableTimeoutTests::badSource, "badSource", this);
            this.payloads.badSourceOther = _ClassStatement.forPayload(ObservableTimeoutTests::badSourceOther, "badSourceOther", this);
            this.payloads.timedTake = _ClassStatement.forPayload(ObservableTimeoutTests::timedTake, "timedTake", this);
            this.payloads.timedFallbackTake = _ClassStatement.forPayload(ObservableTimeoutTests::timedFallbackTake, "timedFallbackTake", this);
            this.payloads.fallbackErrors = _ClassStatement.forPayload(ObservableTimeoutTests::fallbackErrors, "fallbackErrors", this);
            this.payloads.onNextOnTimeoutRace = _ClassStatement.forPayload(ObservableTimeoutTests::onNextOnTimeoutRace, "onNextOnTimeoutRace", this);
            this.payloads.onNextOnTimeoutRaceFallback = _ClassStatement.forPayload(ObservableTimeoutTests::onNextOnTimeoutRaceFallback, "onNextOnTimeoutRaceFallback", this);
        }
    }
}
