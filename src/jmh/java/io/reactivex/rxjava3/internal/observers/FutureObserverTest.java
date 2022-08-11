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
package io.reactivex.rxjava3.internal.observers;

import static io.reactivex.rxjava3.internal.util.ExceptionHelper.timeoutMessage;
import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FutureObserverTest extends RxJavaTest {

    FutureObserver<Integer> fo;

    @Before
    public void before() {
        fo = new FutureObserver<>();
    }

    @Test
    public void cancel2() {
        fo.dispose();
        assertFalse(fo.isCancelled());
        assertFalse(fo.isDisposed());
        assertFalse(fo.isDone());
        for (int i = 0; i < 2; i++) {
            fo.cancel(i == 0);
            assertTrue(fo.isCancelled());
            assertTrue(fo.isDisposed());
            assertTrue(fo.isDone());
        }
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fo.onNext(1);
            fo.onError(new TestException("First"));
            fo.onError(new TestException("Second"));
            fo.onComplete();
            assertTrue(fo.isCancelled());
            assertTrue(fo.isDisposed());
            assertTrue(fo.isDone());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            TestHelper.assertUndeliverable(errors, 1, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancel() throws Exception {
        assertFalse(fo.isDone());
        assertFalse(fo.isCancelled());
        fo.cancel(false);
        assertTrue(fo.isDone());
        assertTrue(fo.isCancelled());
        try {
            fo.get();
            fail("Should have thrown");
        } catch (CancellationException ex) {
        // expected
        }
        try {
            fo.get(1, TimeUnit.MILLISECONDS);
            fail("Should have thrown");
        } catch (CancellationException ex) {
        // expected
        }
    }

    @Test
    public void onError() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fo.onError(new TestException("One"));
            fo.onError(new TestException("Two"));
            try {
                fo.get(5, TimeUnit.MILLISECONDS);
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
        fo.onNext(1);
        fo.onComplete();
        assertEquals(1, fo.get(5, TimeUnit.MILLISECONDS).intValue());
    }

    @Test
    public void onSubscribe() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Disposable d1 = Disposable.empty();
            fo.onSubscribe(d1);
            Disposable d2 = Disposable.empty();
            fo.onSubscribe(d2);
            assertFalse(d1.isDisposed());
            assertTrue(d2.isDisposed());
            TestHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final FutureObserver<Integer> fo = new FutureObserver<>();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    fo.cancel(false);
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
                fo.onNext(1);
                fo.onComplete();
            }
        }, 100, TimeUnit.MILLISECONDS);
        assertEquals(1, fo.get(5, TimeUnit.SECONDS).intValue());
    }

    @Test
    public void onErrorCancelRace() {
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer());
        try {
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                final FutureObserver<Integer> fo = new FutureObserver<>();
                final TestException ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        fo.cancel(false);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        fo.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onCompleteCancelRace() {
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer());
        try {
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                final FutureObserver<Integer> fo = new FutureObserver<>();
                if (i % 3 == 0) {
                    fo.onSubscribe(Disposable.empty());
                }
                if (i % 2 == 0) {
                    fo.onNext(1);
                }
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        fo.cancel(false);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        fo.onComplete();
                    }
                };
                TestHelper.race(r1, r2);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onErrorOnComplete() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fo.onError(new TestException("One"));
            fo.onComplete();
            try {
                fo.get(5, TimeUnit.MILLISECONDS);
            } catch (ExecutionException ex) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
                assertEquals("One", ex.getCause().getMessage());
            }
            TestHelper.assertUndeliverable(errors, 0, NoSuchElementException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onCompleteOnError() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fo.onComplete();
            fo.onError(new TestException("One"));
            try {
                assertNull(fo.get(5, TimeUnit.MILLISECONDS));
            } catch (ExecutionException ex) {
                assertTrue(ex.toString(), ex.getCause() instanceof NoSuchElementException);
            }
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onNextCompleteOnError() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fo.onNext(1);
            fo.onComplete();
            fo.onError(new TestException("One"));
            assertEquals((Integer) 1, fo.get(5, TimeUnit.MILLISECONDS));
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancelOnError() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fo.cancel(true);
            fo.onError(new TestException("One"));
            try {
                fo.get(5, TimeUnit.MILLISECONDS);
                fail("Should have thrown");
            } catch (CancellationException ex) {
            // expected
            }
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancelOnComplete() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            fo.cancel(true);
            fo.onComplete();
            try {
                fo.get(5, TimeUnit.MILLISECONDS);
                fail("Should have thrown");
            } catch (CancellationException ex) {
            // expected
            }
            TestHelper.assertUndeliverable(errors, 0, NoSuchElementException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onNextThenOnCompleteTwice() throws Exception {
        fo.onNext(1);
        fo.onComplete();
        fo.onComplete();
        assertEquals(1, fo.get(5, TimeUnit.MILLISECONDS).intValue());
    }

    @Test(expected = InterruptedException.class)
    public void getInterrupted() throws Exception {
        Thread.currentThread().interrupt();
        fo.get();
    }

    @Test
    public void completeAsync() throws Exception {
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                fo.onNext(1);
                fo.onComplete();
            }
        }, 500, TimeUnit.MILLISECONDS);
        assertEquals(1, fo.get().intValue());
    }

    @Test
    public void getTimedOut() throws Exception {
        try {
            fo.get(1, TimeUnit.NANOSECONDS);
            fail("Should have thrown");
        } catch (TimeoutException expected) {
            assertEquals(timeoutMessage(1, TimeUnit.NANOSECONDS), expected.getMessage());
        }
    }

    @Test
    public void cancelOnSubscribeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final FutureObserver<Integer> fo = new FutureObserver<>();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    fo.cancel(false);
                }
            };
            Disposable d = Disposable.empty();
            TestHelper.race(r, () -> fo.onSubscribe(d));
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FutureObserverTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel2() throws java.lang.Throwable {
            this.payloads.cancel2.evaluate();
        }

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
        public void benchmark_onNextCompleteOnError() throws java.lang.Throwable {
            this.payloads.onNextCompleteOnError.evaluate();
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
        public void benchmark_getInterrupted() throws java.lang.Throwable {
            this.payloads.getInterrupted.evaluate();
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
        public void benchmark_cancelOnSubscribeRace() throws java.lang.Throwable {
            this.payloads.cancelOnSubscribeRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FutureObserverTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FutureObserverTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FutureObserverTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FutureObserverTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FutureObserverTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FutureObserverTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FutureObserverTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FutureObserverTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement cancel2;

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

            public org.junit.runners.model.Statement onNextCompleteOnError;

            public org.junit.runners.model.Statement cancelOnError;

            public org.junit.runners.model.Statement cancelOnComplete;

            public org.junit.runners.model.Statement onNextThenOnCompleteTwice;

            public org.junit.runners.model.Statement getInterrupted;

            public org.junit.runners.model.Statement completeAsync;

            public org.junit.runners.model.Statement getTimedOut;

            public org.junit.runners.model.Statement cancelOnSubscribeRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.cancel2 = _ClassStatement.forPayload(FutureObserverTest::cancel2, "cancel2", this);
            this.payloads.cancel = _ClassStatement.forPayload(FutureObserverTest::cancel, "cancel", this);
            this.payloads.onError = _ClassStatement.forPayload(FutureObserverTest::onError, "onError", this);
            this.payloads.onNext = _ClassStatement.forPayload(FutureObserverTest::onNext, "onNext", this);
            this.payloads.onSubscribe = _ClassStatement.forPayload(FutureObserverTest::onSubscribe, "onSubscribe", this);
            this.payloads.cancelRace = _ClassStatement.forPayload(FutureObserverTest::cancelRace, "cancelRace", this);
            this.payloads.await = _ClassStatement.forPayload(FutureObserverTest::await, "await", this);
            this.payloads.onErrorCancelRace = _ClassStatement.forPayload(FutureObserverTest::onErrorCancelRace, "onErrorCancelRace", this);
            this.payloads.onCompleteCancelRace = _ClassStatement.forPayload(FutureObserverTest::onCompleteCancelRace, "onCompleteCancelRace", this);
            this.payloads.onErrorOnComplete = _ClassStatement.forPayload(FutureObserverTest::onErrorOnComplete, "onErrorOnComplete", this);
            this.payloads.onCompleteOnError = _ClassStatement.forPayload(FutureObserverTest::onCompleteOnError, "onCompleteOnError", this);
            this.payloads.onNextCompleteOnError = _ClassStatement.forPayload(FutureObserverTest::onNextCompleteOnError, "onNextCompleteOnError", this);
            this.payloads.cancelOnError = _ClassStatement.forPayload(FutureObserverTest::cancelOnError, "cancelOnError", this);
            this.payloads.cancelOnComplete = _ClassStatement.forPayload(FutureObserverTest::cancelOnComplete, "cancelOnComplete", this);
            this.payloads.onNextThenOnCompleteTwice = _ClassStatement.forPayload(FutureObserverTest::onNextThenOnCompleteTwice, "onNextThenOnCompleteTwice", this);
            this.payloads.getInterrupted = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FutureObserverTest::getInterrupted, java.lang.InterruptedException.class), "getInterrupted", this);
            this.payloads.completeAsync = _ClassStatement.forPayload(FutureObserverTest::completeAsync, "completeAsync", this);
            this.payloads.getTimedOut = _ClassStatement.forPayload(FutureObserverTest::getTimedOut, "getTimedOut", this);
            this.payloads.cancelOnSubscribeRace = _ClassStatement.forPayload(FutureObserverTest::cancelOnSubscribeRace, "cancelOnSubscribeRace", this);
        }
    }
}
