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
package io.reactivex.rxjava3.internal.operators.completable;

import static io.reactivex.rxjava3.internal.util.ExceptionHelper.timeoutMessage;
import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.internal.operators.completable.CompletableTimeout.TimeOutObserver;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class CompletableTimeoutTest extends RxJavaTest {

    @Test
    public void timeoutException() throws Exception {
        Completable.never().timeout(100, TimeUnit.MILLISECONDS, Schedulers.io()).to(TestHelper.<Void>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertFailureAndMessage(TimeoutException.class, timeoutMessage(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void timeoutContinueOther() throws Exception {
        final int[] call = { 0 };
        Completable other = Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                call[0]++;
            }
        });
        Completable.never().timeout(100, TimeUnit.MILLISECONDS, Schedulers.io(), other).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
        assertEquals(1, call[0]);
    }

    @Test
    public void shouldUnsubscribeFromUnderlyingSubscriptionOnDispose() {
        final PublishSubject<String> subject = PublishSubject.create();
        final TestScheduler scheduler = new TestScheduler();
        final TestObserver<Void> observer = subject.ignoreElements().timeout(100, TimeUnit.MILLISECONDS, scheduler).test();
        assertTrue(subject.hasObservers());
        observer.dispose();
        assertFalse(subject.hasObservers());
    }

    @Test
    public void otherErrors() {
        Completable.never().timeout(1, TimeUnit.MILLISECONDS, Completable.error(new TestException())).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void mainSuccess() {
        Completable.complete().timeout(1, TimeUnit.DAYS).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void mainError() {
        Completable.error(new TestException()).timeout(1, TimeUnit.DAYS).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void errorTimeoutRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final TestScheduler scheduler = new TestScheduler();
                final PublishSubject<Integer> ps = PublishSubject.create();
                TestObserverEx<Void> to = ps.ignoreElements().timeout(1, TimeUnit.MILLISECONDS, scheduler, Completable.complete()).to(TestHelper.<Void>testConsumer());
                final TestException ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertTerminated();
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void ambRace() {
        TestObserver<Void> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        CompositeDisposable cd = new CompositeDisposable();
        AtomicBoolean once = new AtomicBoolean();
        TimeOutObserver a = new TimeOutObserver(cd, once, to);
        a.onComplete();
        a.onComplete();
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            a.onError(new TestException());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private CompletableTimeoutTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutException() throws java.lang.Throwable {
            this.payloads.timeoutException.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutContinueOther() throws java.lang.Throwable {
            this.payloads.timeoutContinueOther.evaluate();
        }

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
        public void benchmark_errorTimeoutRace() throws java.lang.Throwable {
            this.payloads.errorTimeoutRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambRace() throws java.lang.Throwable {
            this.payloads.ambRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTimeoutTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTimeoutTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTimeoutTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTimeoutTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableTimeoutTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTimeoutTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableTimeoutTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableTimeoutTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement timeoutException;

            public org.junit.runners.model.Statement timeoutContinueOther;

            public org.junit.runners.model.Statement shouldUnsubscribeFromUnderlyingSubscriptionOnDispose;

            public org.junit.runners.model.Statement otherErrors;

            public org.junit.runners.model.Statement mainSuccess;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement errorTimeoutRace;

            public org.junit.runners.model.Statement ambRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.timeoutException = _ClassStatement.forPayload(CompletableTimeoutTest::timeoutException, "timeoutException", this);
            this.payloads.timeoutContinueOther = _ClassStatement.forPayload(CompletableTimeoutTest::timeoutContinueOther, "timeoutContinueOther", this);
            this.payloads.shouldUnsubscribeFromUnderlyingSubscriptionOnDispose = _ClassStatement.forPayload(CompletableTimeoutTest::shouldUnsubscribeFromUnderlyingSubscriptionOnDispose, "shouldUnsubscribeFromUnderlyingSubscriptionOnDispose", this);
            this.payloads.otherErrors = _ClassStatement.forPayload(CompletableTimeoutTest::otherErrors, "otherErrors", this);
            this.payloads.mainSuccess = _ClassStatement.forPayload(CompletableTimeoutTest::mainSuccess, "mainSuccess", this);
            this.payloads.mainError = _ClassStatement.forPayload(CompletableTimeoutTest::mainError, "mainError", this);
            this.payloads.errorTimeoutRace = _ClassStatement.forPayload(CompletableTimeoutTest::errorTimeoutRace, "errorTimeoutRace", this);
            this.payloads.ambRace = _ClassStatement.forPayload(CompletableTimeoutTest::ambRace, "ambRace", this);
        }
    }
}
