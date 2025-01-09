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
import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FutureSingleObserverTest extends RxJavaTest {

    @Test
    public void cancel() {
        final Future<?> f = Single.never().toFuture();
        assertFalse(f.isCancelled());
        assertFalse(f.isDone());
        f.cancel(true);
        assertTrue(f.isCancelled());
        assertTrue(f.isDone());
        try {
            f.get();
            fail("Should have thrown!");
        } catch (CancellationException ex) {
        // expected
        } catch (InterruptedException ex) {
            throw new AssertionError(ex);
        } catch (ExecutionException ex) {
            throw new AssertionError(ex);
        }
        try {
            f.get(5, TimeUnit.SECONDS);
            fail("Should have thrown!");
        } catch (CancellationException ex) {
        // expected
        } catch (InterruptedException ex) {
            throw new AssertionError(ex);
        } catch (ExecutionException ex) {
            throw new AssertionError(ex);
        } catch (TimeoutException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void cancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final Future<?> f = Single.never().toFuture();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    f.cancel(true);
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void timeout() throws Exception {
        Future<?> f = Single.never().toFuture();
        try {
            f.get(100, TimeUnit.MILLISECONDS);
            fail("Should have thrown");
        } catch (TimeoutException expected) {
            assertEquals(timeoutMessage(100, TimeUnit.MILLISECONDS), expected.getMessage());
        }
    }

    @Test
    public void dispose() {
        Future<Integer> f = Single.just(1).toFuture();
        ((Disposable) f).dispose();
        assertTrue(((Disposable) f).isDisposed());
    }

    @Test
    public void errorGetWithTimeout() throws Exception {
        Future<?> f = Single.error(new TestException()).toFuture();
        try {
            f.get(5, TimeUnit.SECONDS);
            fail("Should have thrown");
        } catch (ExecutionException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof TestException);
        }
    }

    @Test
    public void normalGetWitHTimeout() throws Exception {
        Future<Integer> f = Single.just(1).toFuture();
        assertEquals(1, f.get(5, TimeUnit.SECONDS).intValue());
    }

    @Test
    public void getAwait() throws Exception {
        Future<Integer> f = Single.just(1).delay(100, TimeUnit.MILLISECONDS).toFuture();
        assertEquals(1, f.get(5, TimeUnit.SECONDS).intValue());
    }

    @Test
    public void onSuccessCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final Future<?> f = ps.single(-99).toFuture();
            ps.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    f.cancel(true);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void onErrorCancelRace() {
        RxJavaPlugins.setErrorHandler(Functions.emptyConsumer());
        try {
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                final PublishSubject<Integer> ps = PublishSubject.create();
                final Future<?> f = ps.single(-99).toFuture();
                final TestException ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        f.cancel(true);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ps.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FutureSingleObserverTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelRace() throws java.lang.Throwable {
            this.payloads.cancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeout() throws java.lang.Throwable {
            this.payloads.timeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorGetWithTimeout() throws java.lang.Throwable {
            this.payloads.errorGetWithTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalGetWitHTimeout() throws java.lang.Throwable {
            this.payloads.normalGetWitHTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getAwait() throws java.lang.Throwable {
            this.payloads.getAwait.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessCancelRace() throws java.lang.Throwable {
            this.payloads.onSuccessCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCancelRace() throws java.lang.Throwable {
            this.payloads.onErrorCancelRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FutureSingleObserverTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FutureSingleObserverTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FutureSingleObserverTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FutureSingleObserverTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FutureSingleObserverTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FutureSingleObserverTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FutureSingleObserverTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FutureSingleObserverTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement cancelRace;

            public org.junit.runners.model.Statement timeout;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement errorGetWithTimeout;

            public org.junit.runners.model.Statement normalGetWitHTimeout;

            public org.junit.runners.model.Statement getAwait;

            public org.junit.runners.model.Statement onSuccessCancelRace;

            public org.junit.runners.model.Statement onErrorCancelRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.cancel = _ClassStatement.forPayload(FutureSingleObserverTest::cancel, "cancel", this);
            this.payloads.cancelRace = _ClassStatement.forPayload(FutureSingleObserverTest::cancelRace, "cancelRace", this);
            this.payloads.timeout = _ClassStatement.forPayload(FutureSingleObserverTest::timeout, "timeout", this);
            this.payloads.dispose = _ClassStatement.forPayload(FutureSingleObserverTest::dispose, "dispose", this);
            this.payloads.errorGetWithTimeout = _ClassStatement.forPayload(FutureSingleObserverTest::errorGetWithTimeout, "errorGetWithTimeout", this);
            this.payloads.normalGetWitHTimeout = _ClassStatement.forPayload(FutureSingleObserverTest::normalGetWitHTimeout, "normalGetWitHTimeout", this);
            this.payloads.getAwait = _ClassStatement.forPayload(FutureSingleObserverTest::getAwait, "getAwait", this);
            this.payloads.onSuccessCancelRace = _ClassStatement.forPayload(FutureSingleObserverTest::onSuccessCancelRace, "onSuccessCancelRace", this);
            this.payloads.onErrorCancelRace = _ClassStatement.forPayload(FutureSingleObserverTest::onErrorCancelRace, "onErrorCancelRace", this);
        }
    }
}
