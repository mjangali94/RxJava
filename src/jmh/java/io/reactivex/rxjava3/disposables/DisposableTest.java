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
package io.reactivex.rxjava3.disposables;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.internal.disposables.DisposableHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class DisposableTest extends RxJavaTest {

    @Test
    public void unsubscribeOnlyOnce() {
        Runnable run = mock(Runnable.class);
        Disposable d = Disposable.fromRunnable(run);
        assertTrue(d.toString(), d.toString().contains("RunnableDisposable(disposed=false, "));
        d.dispose();
        assertTrue(d.toString(), d.toString().contains("RunnableDisposable(disposed=true, "));
        d.dispose();
        assertTrue(d.toString(), d.toString().contains("RunnableDisposable(disposed=true, "));
        verify(run, times(1)).run();
    }

    @Test
    public void empty() {
        Disposable empty = Disposable.empty();
        assertFalse(empty.isDisposed());
        empty.dispose();
        assertTrue(empty.isDisposed());
    }

    @Test
    public void unsubscribed() {
        Disposable disposed = Disposable.disposed();
        assertTrue(disposed.isDisposed());
    }

    @Test
    public void fromAction() throws Throwable {
        Action action = mock(Action.class);
        Disposable d = Disposable.fromAction(action);
        assertTrue(d.toString(), d.toString().contains("ActionDisposable(disposed=false, "));
        d.dispose();
        assertTrue(d.toString(), d.toString().contains("ActionDisposable(disposed=true, "));
        d.dispose();
        assertTrue(d.toString(), d.toString().contains("ActionDisposable(disposed=true, "));
        verify(action, times(1)).run();
    }

    @Test
    public void fromActionThrows() {
        try {
            Disposable.fromAction(new Action() {

                @Override
                public void run() throws Exception {
                    throw new IllegalArgumentException();
                }
            }).dispose();
            fail("Should have thrown!");
        } catch (IllegalArgumentException ex) {
        // expected
        }
        try {
            Disposable.fromAction(new Action() {

                @Override
                public void run() throws Exception {
                    throw new InternalError();
                }
            }).dispose();
            fail("Should have thrown!");
        } catch (InternalError ex) {
        // expected
        }
        try {
            Disposable.fromAction(new Action() {

                @Override
                public void run() throws Exception {
                    throw new IOException();
                }
            }).dispose();
            fail("Should have thrown!");
        } catch (RuntimeException ex) {
            if (!(ex.getCause() instanceof IOException)) {
                fail(ex.toString() + ": Should have cause of IOException");
            }
        // expected
        }
    }

    @Test
    public void disposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final Disposable d = Disposable.empty();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    d.dispose();
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test(expected = NullPointerException.class)
    public void fromSubscriptionNull() {
        Disposable.fromSubscription(null);
    }

    @Test
    public void fromSubscription() {
        Subscription s = mock(Subscription.class);
        Disposable.fromSubscription(s).dispose();
        verify(s).cancel();
        verify(s, never()).request(anyInt());
    }

    @Test
    public void setOnceTwice() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            AtomicReference<Disposable> target = new AtomicReference<>();
            Disposable d = Disposable.empty();
            DisposableHelper.setOnce(target, d);
            Disposable d1 = Disposable.empty();
            DisposableHelper.setOnce(target, d1);
            assertTrue(d1.isDisposed());
            TestHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void fromAutoCloseable() {
        AtomicInteger counter = new AtomicInteger();
        AutoCloseable ac = () -> counter.getAndIncrement();
        Disposable d = Disposable.fromAutoCloseable(ac);
        assertFalse(d.isDisposed());
        assertEquals(0, counter.get());
        assertTrue(d.toString(), d.toString().contains("AutoCloseableDisposable(disposed=false, "));
        d.dispose();
        assertTrue(d.isDisposed());
        assertEquals(1, counter.get());
        assertTrue(d.toString(), d.toString().contains("AutoCloseableDisposable(disposed=true, "));
        d.dispose();
        assertTrue(d.isDisposed());
        assertEquals(1, counter.get());
        assertTrue(d.toString(), d.toString().contains("AutoCloseableDisposable(disposed=true, "));
    }

    @Test
    public void fromAutoCloseableThrows() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            AutoCloseable ac = () -> {
                throw new TestException();
            };
            Disposable d = Disposable.fromAutoCloseable(ac);
            assertFalse(d.isDisposed());
            assertTrue(errors.isEmpty());
            try {
                d.dispose();
                fail("Should have thrown!");
            } catch (TestException expected) {
            // expected
            }
            assertTrue(d.isDisposed());
            d.dispose();
            assertTrue(d.isDisposed());
            assertTrue(errors.isEmpty());
        });
    }

    @Test
    public void toAutoCloseable() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        Disposable d = Disposable.fromAction(() -> counter.getAndIncrement());
        AutoCloseable ac = Disposable.toAutoCloseable(d);
        assertFalse(d.isDisposed());
        assertEquals(0, counter.get());
        ac.close();
        assertTrue(d.isDisposed());
        assertEquals(1, counter.get());
        ac.close();
        assertTrue(d.isDisposed());
        assertEquals(1, counter.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public DisposableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeOnlyOnce() throws java.lang.Throwable {
            this.payloads.unsubscribeOnlyOnce.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribed() throws java.lang.Throwable {
            this.payloads.unsubscribed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromAction() throws java.lang.Throwable {
            this.payloads.fromAction.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionThrows() throws java.lang.Throwable {
            this.payloads.fromActionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRace() throws java.lang.Throwable {
            this.payloads.disposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromSubscriptionNull() throws java.lang.Throwable {
            this.payloads.fromSubscriptionNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromSubscription() throws java.lang.Throwable {
            this.payloads.fromSubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setOnceTwice() throws java.lang.Throwable {
            this.payloads.setOnceTwice.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromAutoCloseable() throws java.lang.Throwable {
            this.payloads.fromAutoCloseable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromAutoCloseableThrows() throws java.lang.Throwable {
            this.payloads.fromAutoCloseableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toAutoCloseable() throws java.lang.Throwable {
            this.payloads.toAutoCloseable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<DisposableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<DisposableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<DisposableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<DisposableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new DisposableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<DisposableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(DisposableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(DisposableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement unsubscribeOnlyOnce;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement unsubscribed;

            public org.junit.runners.model.Statement fromAction;

            public org.junit.runners.model.Statement fromActionThrows;

            public org.junit.runners.model.Statement disposeRace;

            public org.junit.runners.model.Statement fromSubscriptionNull;

            public org.junit.runners.model.Statement fromSubscription;

            public org.junit.runners.model.Statement setOnceTwice;

            public org.junit.runners.model.Statement fromAutoCloseable;

            public org.junit.runners.model.Statement fromAutoCloseableThrows;

            public org.junit.runners.model.Statement toAutoCloseable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.unsubscribeOnlyOnce = _ClassStatement.forPayload(DisposableTest::unsubscribeOnlyOnce, "unsubscribeOnlyOnce", this);
            this.payloads.empty = _ClassStatement.forPayload(DisposableTest::empty, "empty", this);
            this.payloads.unsubscribed = _ClassStatement.forPayload(DisposableTest::unsubscribed, "unsubscribed", this);
            this.payloads.fromAction = _ClassStatement.forPayload(DisposableTest::fromAction, "fromAction", this);
            this.payloads.fromActionThrows = _ClassStatement.forPayload(DisposableTest::fromActionThrows, "fromActionThrows", this);
            this.payloads.disposeRace = _ClassStatement.forPayload(DisposableTest::disposeRace, "disposeRace", this);
            this.payloads.fromSubscriptionNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(DisposableTest::fromSubscriptionNull, java.lang.NullPointerException.class), "fromSubscriptionNull", this);
            this.payloads.fromSubscription = _ClassStatement.forPayload(DisposableTest::fromSubscription, "fromSubscription", this);
            this.payloads.setOnceTwice = _ClassStatement.forPayload(DisposableTest::setOnceTwice, "setOnceTwice", this);
            this.payloads.fromAutoCloseable = _ClassStatement.forPayload(DisposableTest::fromAutoCloseable, "fromAutoCloseable", this);
            this.payloads.fromAutoCloseableThrows = _ClassStatement.forPayload(DisposableTest::fromAutoCloseableThrows, "fromAutoCloseableThrows", this);
            this.payloads.toAutoCloseable = _ClassStatement.forPayload(DisposableTest::toAutoCloseable, "toAutoCloseable", this);
        }
    }
}
