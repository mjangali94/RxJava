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
package io.reactivex.rxjava3.internal.operators.single;

import static io.reactivex.rxjava3.internal.util.ExceptionHelper.timeoutMessage;
import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleTimeoutTest extends RxJavaTest {

    @Test
    public void shouldUnsubscribeFromUnderlyingSubscriptionOnDispose() {
        final PublishSubject<String> subject = PublishSubject.create();
        final TestScheduler scheduler = new TestScheduler();
        final TestObserver<String> observer = subject.single("").timeout(100, TimeUnit.MILLISECONDS, scheduler).test();
        assertTrue(subject.hasObservers());
        observer.dispose();
        assertFalse(subject.hasObservers());
    }

    @Test
    public void otherErrors() {
        Single.never().timeout(1, TimeUnit.MILLISECONDS, Single.error(new TestException())).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void mainSuccess() {
        Single.just(1).timeout(1, TimeUnit.DAYS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void mainError() {
        Single.error(new TestException()).timeout(1, TimeUnit.DAYS).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void disposeWhenFallback() {
        TestScheduler sch = new TestScheduler();
        SingleSubject<Integer> subj = SingleSubject.create();
        subj.timeout(1, TimeUnit.SECONDS, sch, Single.just(1)).test(true).assertEmpty();
        assertFalse(subj.hasObservers());
    }

    @Test
    public void isDisposed() {
        TestHelper.checkDisposed(SingleSubject.create().timeout(1, TimeUnit.DAYS));
    }

    @Test
    public void fallbackDispose() {
        TestScheduler sch = new TestScheduler();
        SingleSubject<Integer> subj = SingleSubject.create();
        SingleSubject<Integer> fallback = SingleSubject.create();
        TestObserver<Integer> to = subj.timeout(1, TimeUnit.SECONDS, sch, fallback).test();
        assertFalse(fallback.hasObservers());
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        assertFalse(subj.hasObservers());
        assertTrue(fallback.hasObservers());
        to.dispose();
        assertFalse(fallback.hasObservers());
    }

    @Test
    public void normalSuccessDoesntDisposeMain() {
        final int[] calls = { 0 };
        Single.just(1).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                calls[0]++;
            }
        }).timeout(1, TimeUnit.DAYS).test().assertResult(1);
        assertEquals(0, calls[0]);
    }

    @Test
    public void successTimeoutRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final SingleSubject<Integer> subj = SingleSubject.create();
            SingleSubject<Integer> fallback = SingleSubject.create();
            final TestScheduler sch = new TestScheduler();
            TestObserver<Integer> to = subj.timeout(1, TimeUnit.MILLISECONDS, sch, fallback).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    subj.onSuccess(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sch.advanceTimeBy(1, TimeUnit.MILLISECONDS);
                }
            };
            TestHelper.race(r1, r2);
            if (!fallback.hasObservers()) {
                to.assertResult(1);
            } else {
                to.assertEmpty();
            }
        }
    }

    @Test
    public void errorTimeoutRace() {
        final TestException ex = new TestException();
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                final SingleSubject<Integer> subj = SingleSubject.create();
                SingleSubject<Integer> fallback = SingleSubject.create();
                final TestScheduler sch = new TestScheduler();
                TestObserver<Integer> to = subj.timeout(1, TimeUnit.MILLISECONDS, sch, fallback).test();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        subj.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        sch.advanceTimeBy(1, TimeUnit.MILLISECONDS);
                    }
                };
                TestHelper.race(r1, r2);
                if (!fallback.hasObservers()) {
                    to.assertFailure(TestException.class);
                } else {
                    to.assertEmpty();
                }
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void mainTimedOut() {
        Single.never().timeout(1, TimeUnit.MILLISECONDS).to(TestHelper.<Object>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertFailureAndMessage(TimeoutException.class, timeoutMessage(1, TimeUnit.MILLISECONDS));
    }

    @Test
    public void mainTimeoutFallbackSuccess() {
        Single.never().timeout(1, TimeUnit.MILLISECONDS, Single.just(1)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void timeoutBeforeOnSubscribeFromMain() {
        Disposable d = Disposable.empty();
        new Single<Integer>() {

            @Override
            protected void subscribeActual(@NonNull SingleObserver<? super @NonNull Integer> observer) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                observer.onSubscribe(d);
            }
        }.timeout(1, TimeUnit.MILLISECONDS, Single.just(1)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
        assertTrue(d.isDisposed());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public SingleTimeoutTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldUnsubscribeFromUnderlyingSubscriptionOnDispose() throws java.lang.Throwable {
            this.payloads.shouldUnsubscribeFromUnderlyingSubscriptionOnDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherErrors() throws java.lang.Throwable {
            this.payloads.otherErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainSuccess() throws java.lang.Throwable {
            this.payloads.mainSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.payloads.mainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeWhenFallback() throws java.lang.Throwable {
            this.payloads.disposeWhenFallback.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposed() throws java.lang.Throwable {
            this.payloads.isDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fallbackDispose() throws java.lang.Throwable {
            this.payloads.fallbackDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalSuccessDoesntDisposeMain() throws java.lang.Throwable {
            this.payloads.normalSuccessDoesntDisposeMain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successTimeoutRace() throws java.lang.Throwable {
            this.payloads.successTimeoutRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorTimeoutRace() throws java.lang.Throwable {
            this.payloads.errorTimeoutRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainTimedOut() throws java.lang.Throwable {
            this.payloads.mainTimedOut.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainTimeoutFallbackSuccess() throws java.lang.Throwable {
            this.payloads.mainTimeoutFallbackSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutBeforeOnSubscribeFromMain() throws java.lang.Throwable {
            this.payloads.timeoutBeforeOnSubscribeFromMain.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTimeoutTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTimeoutTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTimeoutTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTimeoutTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleTimeoutTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTimeoutTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleTimeoutTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleTimeoutTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement shouldUnsubscribeFromUnderlyingSubscriptionOnDispose;

            public org.junit.runners.model.Statement otherErrors;

            public org.junit.runners.model.Statement mainSuccess;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement disposeWhenFallback;

            public org.junit.runners.model.Statement isDisposed;

            public org.junit.runners.model.Statement fallbackDispose;

            public org.junit.runners.model.Statement normalSuccessDoesntDisposeMain;

            public org.junit.runners.model.Statement successTimeoutRace;

            public org.junit.runners.model.Statement errorTimeoutRace;

            public org.junit.runners.model.Statement mainTimedOut;

            public org.junit.runners.model.Statement mainTimeoutFallbackSuccess;

            public org.junit.runners.model.Statement timeoutBeforeOnSubscribeFromMain;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.shouldUnsubscribeFromUnderlyingSubscriptionOnDispose = _ClassStatement.forPayload(SingleTimeoutTest::shouldUnsubscribeFromUnderlyingSubscriptionOnDispose, "shouldUnsubscribeFromUnderlyingSubscriptionOnDispose", this);
            this.payloads.otherErrors = _ClassStatement.forPayload(SingleTimeoutTest::otherErrors, "otherErrors", this);
            this.payloads.mainSuccess = _ClassStatement.forPayload(SingleTimeoutTest::mainSuccess, "mainSuccess", this);
            this.payloads.mainError = _ClassStatement.forPayload(SingleTimeoutTest::mainError, "mainError", this);
            this.payloads.disposeWhenFallback = _ClassStatement.forPayload(SingleTimeoutTest::disposeWhenFallback, "disposeWhenFallback", this);
            this.payloads.isDisposed = _ClassStatement.forPayload(SingleTimeoutTest::isDisposed, "isDisposed", this);
            this.payloads.fallbackDispose = _ClassStatement.forPayload(SingleTimeoutTest::fallbackDispose, "fallbackDispose", this);
            this.payloads.normalSuccessDoesntDisposeMain = _ClassStatement.forPayload(SingleTimeoutTest::normalSuccessDoesntDisposeMain, "normalSuccessDoesntDisposeMain", this);
            this.payloads.successTimeoutRace = _ClassStatement.forPayload(SingleTimeoutTest::successTimeoutRace, "successTimeoutRace", this);
            this.payloads.errorTimeoutRace = _ClassStatement.forPayload(SingleTimeoutTest::errorTimeoutRace, "errorTimeoutRace", this);
            this.payloads.mainTimedOut = _ClassStatement.forPayload(SingleTimeoutTest::mainTimedOut, "mainTimedOut", this);
            this.payloads.mainTimeoutFallbackSuccess = _ClassStatement.forPayload(SingleTimeoutTest::mainTimeoutFallbackSuccess, "mainTimeoutFallbackSuccess", this);
            this.payloads.timeoutBeforeOnSubscribeFromMain = _ClassStatement.forPayload(SingleTimeoutTest::timeoutBeforeOnSubscribeFromMain, "timeoutBeforeOnSubscribeFromMain", this);
        }
    }
}
