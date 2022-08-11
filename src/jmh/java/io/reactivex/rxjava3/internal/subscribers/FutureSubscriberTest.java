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
package io.reactivex.rxjava3.internal.subscribers;

import static io.reactivex.rxjava3.internal.util.ExceptionHelper.timeoutMessage;
import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.*;

public class FutureSubscriberTest extends RxJavaTest {

    FutureSubscriber<Integer> fs;

    @Before
    public void before() {
        fs = new FutureSubscriber<>();
    }

    @Test
    public void cancel() throws Exception {
        assertFalse(fs.isDone());
        assertFalse(fs.isCancelled());
        fs.cancel();
        fs.cancel();
        fs.request(10);
        fs.request(-99);
        fs.cancel(false);
        assertTrue(fs.isDone());
        assertTrue(fs.isCancelled());
        try {
            fs.get();
            fail("Should have thrown");
        } catch (CancellationException ex) {
        // expected
        }
        try {
            fs.get(1, TimeUnit.MILLISECONDS);
            fail("Should have thrown");
        } catch (CancellationException ex) {
        // expected
        }
    }

    @Test
    public void onError() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fs.onError(new TestException("One"));
            fs.onError(new TestException("Two"));
            try {
                fs.get(5, TimeUnit.MILLISECONDS);
            } catch (ExecutionException ex) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
                assertEquals("One", ex.getCause().getMessage());
            }
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Two");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onNext() throws Exception {
        fs.onNext(1);
        fs.onComplete();
        assertEquals(1, fs.get(5, TimeUnit.MILLISECONDS).intValue());
    }

    @Test
    public void onSubscribe() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            BooleanSubscription s = new BooleanSubscription();
            fs.onSubscribe(s);
            BooleanSubscription s2 = new BooleanSubscription();
            fs.onSubscribe(s2);
            assertFalse(s.isCancelled());
            assertTrue(s2.isCancelled());
            TestHelper.assertError(errors, 0, IllegalStateException.class, "Subscription already set!");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final FutureSubscriber<Integer> fs = new FutureSubscriber<>();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    fs.cancel(false);
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void await() throws Exception {
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                fs.onNext(1);
                fs.onComplete();
            }
        }, 100, TimeUnit.MILLISECONDS);
        assertEquals(1, fs.get(5, TimeUnit.SECONDS).intValue());
    }

    @Test
    @SuppressUndeliverable
    public void onErrorCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final FutureSubscriber<Integer> fs = new FutureSubscriber<>();
            final TestException ex = new TestException();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    fs.cancel(false);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    fs.onError(ex);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    @SuppressUndeliverable
    public void onCompleteCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final FutureSubscriber<Integer> fs = new FutureSubscriber<>();
            if (i % 3 == 0) {
                fs.onSubscribe(new BooleanSubscription());
            }
            if (i % 2 == 0) {
                fs.onNext(1);
            }
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    fs.cancel(false);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    fs.onComplete();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    @SuppressUndeliverable
    public void onErrorOnComplete() throws Exception {
        fs.onError(new TestException("One"));
        fs.onComplete();
        try {
            fs.get(5, TimeUnit.MILLISECONDS);
        } catch (ExecutionException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            assertEquals("One", ex.getCause().getMessage());
        }
    }

    @Test
    @SuppressUndeliverable
    public void onCompleteOnError() throws Exception {
        fs.onComplete();
        fs.onError(new TestException("One"));
        try {
            assertNull(fs.get(5, TimeUnit.MILLISECONDS));
        } catch (ExecutionException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof NoSuchElementException);
        }
    }

    @Test
    @SuppressUndeliverable
    public void cancelOnError() throws Exception {
        fs.cancel(true);
        fs.onError(new TestException("One"));
        try {
            fs.get(5, TimeUnit.MILLISECONDS);
            fail("Should have thrown");
        } catch (CancellationException ex) {
        // expected
        }
    }

    @Test
    @SuppressUndeliverable
    public void cancelOnComplete() throws Exception {
        fs.cancel(true);
        fs.onComplete();
        try {
            fs.get(5, TimeUnit.MILLISECONDS);
            fail("Should have thrown");
        } catch (CancellationException ex) {
        // expected
        }
    }

    @Test
    public void onNextThenOnCompleteTwice() throws Exception {
        fs.onNext(1);
        fs.onComplete();
        fs.onComplete();
        assertEquals(1, fs.get(5, TimeUnit.MILLISECONDS).intValue());
    }

    @Test
    public void completeAsync() throws Exception {
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                fs.onNext(1);
                fs.onComplete();
            }
        }, 500, TimeUnit.MILLISECONDS);
        assertEquals(1, fs.get().intValue());
    }

    @Test
    public void getTimedOut() throws Exception {
        try {
            fs.get(1, TimeUnit.NANOSECONDS);
            fail("Should have thrown");
        } catch (TimeoutException expected) {
            assertEquals(timeoutMessage(1, TimeUnit.NANOSECONDS), expected.getMessage());
        }
    }

    @Test
    public void onNextCompleteOnError() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fs.onNext(1);
            fs.onComplete();
            fs.onError(new TestException("One"));
            assertEquals((Integer) 1, fs.get(5, TimeUnit.MILLISECONDS));
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FutureSubscriberTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onError() throws java.lang.Throwable {
            this.payloads.onError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNext() throws java.lang.Throwable {
            this.payloads.onNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribe() throws java.lang.Throwable {
            this.payloads.onSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelRace() throws java.lang.Throwable {
            this.payloads.cancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_await() throws java.lang.Throwable {
            this.payloads.await.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCancelRace() throws java.lang.Throwable {
            this.payloads.onErrorCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteCancelRace() throws java.lang.Throwable {
            this.payloads.onCompleteCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorOnComplete() throws java.lang.Throwable {
            this.payloads.onErrorOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteOnError() throws java.lang.Throwable {
            this.payloads.onCompleteOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOnError() throws java.lang.Throwable {
            this.payloads.cancelOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOnComplete() throws java.lang.Throwable {
            this.payloads.cancelOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextThenOnCompleteTwice() throws java.lang.Throwable {
            this.payloads.onNextThenOnCompleteTwice.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeAsync() throws java.lang.Throwable {
            this.payloads.completeAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getTimedOut() throws java.lang.Throwable {
            this.payloads.getTimedOut.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextCompleteOnError() throws java.lang.Throwable {
            this.payloads.onNextCompleteOnError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FutureSubscriberTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FutureSubscriberTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FutureSubscriberTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FutureSubscriberTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FutureSubscriberTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FutureSubscriberTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FutureSubscriberTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FutureSubscriberTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement onError;

            public org.junit.runners.model.Statement onNext;

            public org.junit.runners.model.Statement onSubscribe;

            public org.junit.runners.model.Statement cancelRace;

            public org.junit.runners.model.Statement await;

            public org.junit.runners.model.Statement onErrorCancelRace;

            public org.junit.runners.model.Statement onCompleteCancelRace;

            public org.junit.runners.model.Statement onErrorOnComplete;

            public org.junit.runners.model.Statement onCompleteOnError;

            public org.junit.runners.model.Statement cancelOnError;

            public org.junit.runners.model.Statement cancelOnComplete;

            public org.junit.runners.model.Statement onNextThenOnCompleteTwice;

            public org.junit.runners.model.Statement completeAsync;

            public org.junit.runners.model.Statement getTimedOut;

            public org.junit.runners.model.Statement onNextCompleteOnError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.cancel = _ClassStatement.forPayload(FutureSubscriberTest::cancel, "cancel", this);
            this.payloads.onError = _ClassStatement.forPayload(FutureSubscriberTest::onError, "onError", this);
            this.payloads.onNext = _ClassStatement.forPayload(FutureSubscriberTest::onNext, "onNext", this);
            this.payloads.onSubscribe = _ClassStatement.forPayload(FutureSubscriberTest::onSubscribe, "onSubscribe", this);
            this.payloads.cancelRace = _ClassStatement.forPayload(FutureSubscriberTest::cancelRace, "cancelRace", this);
            this.payloads.await = _ClassStatement.forPayload(FutureSubscriberTest::await, "await", this);
            this.payloads.onErrorCancelRace = _ClassStatement.forPayload(FutureSubscriberTest::onErrorCancelRace, "onErrorCancelRace", this);
            this.payloads.onCompleteCancelRace = _ClassStatement.forPayload(FutureSubscriberTest::onCompleteCancelRace, "onCompleteCancelRace", this);
            this.payloads.onErrorOnComplete = _ClassStatement.forPayload(FutureSubscriberTest::onErrorOnComplete, "onErrorOnComplete", this);
            this.payloads.onCompleteOnError = _ClassStatement.forPayload(FutureSubscriberTest::onCompleteOnError, "onCompleteOnError", this);
            this.payloads.cancelOnError = _ClassStatement.forPayload(FutureSubscriberTest::cancelOnError, "cancelOnError", this);
            this.payloads.cancelOnComplete = _ClassStatement.forPayload(FutureSubscriberTest::cancelOnComplete, "cancelOnComplete", this);
            this.payloads.onNextThenOnCompleteTwice = _ClassStatement.forPayload(FutureSubscriberTest::onNextThenOnCompleteTwice, "onNextThenOnCompleteTwice", this);
            this.payloads.completeAsync = _ClassStatement.forPayload(FutureSubscriberTest::completeAsync, "completeAsync", this);
            this.payloads.getTimedOut = _ClassStatement.forPayload(FutureSubscriberTest::getTimedOut, "getTimedOut", this);
            this.payloads.onNextCompleteOnError = _ClassStatement.forPayload(FutureSubscriberTest::onNextCompleteOnError, "onNextCompleteOnError", this);
        }
    }
}
