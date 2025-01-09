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
package io.reactivex.rxjava3.internal.schedulers;

import static org.junit.Assert.*;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ScheduledRunnableTest extends RxJavaTest {

    @Test
    public void dispose() {
        CompositeDisposable set = new CompositeDisposable();
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
        set.add(run);
        assertFalse(run.isDisposed());
        set.dispose();
        assertTrue(run.isDisposed());
    }

    @Test
    public void disposeRun() {
        CompositeDisposable set = new CompositeDisposable();
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
        set.add(run);
        assertFalse(run.isDisposed());
        run.dispose();
        run.dispose();
        assertTrue(run.isDisposed());
    }

    @Test
    public void setFutureCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            CompositeDisposable set = new CompositeDisposable();
            final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
            set.add(run);
            final FutureTask<Object> ft = new FutureTask<>(Functions.EMPTY_RUNNABLE, 0);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    run.setFuture(ft);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    run.dispose();
                }
            };
            TestHelper.race(r1, r2);
            assertEquals(0, set.size());
        }
    }

    @Test
    public void setFutureRunRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            CompositeDisposable set = new CompositeDisposable();
            final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
            set.add(run);
            final FutureTask<Object> ft = new FutureTask<>(Functions.EMPTY_RUNNABLE, 0);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    run.setFuture(ft);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    run.run();
                }
            };
            TestHelper.race(r1, r2);
            assertEquals(0, set.size());
        }
    }

    @Test
    public void disposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            CompositeDisposable set = new CompositeDisposable();
            final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
            set.add(run);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    run.dispose();
                }
            };
            TestHelper.race(r1, r1);
            assertEquals(0, set.size());
        }
    }

    @Test
    public void runDispose() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            CompositeDisposable set = new CompositeDisposable();
            final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
            set.add(run);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    run.call();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    run.dispose();
                }
            };
            TestHelper.race(r1, r2);
            assertEquals(0, set.size());
        }
    }

    @Test
    public void pluginCrash() {
        Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                throw new TestException("Second");
            }
        });
        CompositeDisposable set = new CompositeDisposable();
        final ScheduledRunnable run = new ScheduledRunnable(new Runnable() {

            @Override
            public void run() {
                throw new TestException("First");
            }
        }, set);
        set.add(run);
        try {
            run.run();
            fail("Should have thrown!");
        } catch (TestException ex) {
            assertEquals("Second", ex.getMessage());
        } finally {
            Thread.currentThread().setUncaughtExceptionHandler(null);
        }
        assertTrue(run.isDisposed());
        assertEquals(0, set.size());
    }

    @Test
    public void crashReported() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            CompositeDisposable set = new CompositeDisposable();
            final ScheduledRunnable run = new ScheduledRunnable(new Runnable() {

                @Override
                public void run() {
                    throw new TestException("First");
                }
            }, set);
            set.add(run);
            try {
                run.run();
                fail("Should have thrown!");
            } catch (TestException expected) {
            // expected
            }
            assertTrue(run.isDisposed());
            assertEquals(0, set.size());
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "First");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void withoutParentDisposed() {
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        run.dispose();
        run.call();
    }

    @Test
    public void withParentDisposed() {
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, new CompositeDisposable());
        run.dispose();
        run.call();
    }

    @Test
    public void withFutureDisposed() {
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        run.setFuture(new FutureTask<Void>(Functions.EMPTY_RUNNABLE, null));
        run.dispose();
        run.call();
    }

    @Test
    public void withFutureDisposed2() {
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        run.dispose();
        run.setFuture(new FutureTask<Void>(Functions.EMPTY_RUNNABLE, null));
        run.call();
    }

    @Test
    public void withFutureDisposed3() {
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        run.dispose();
        run.set(2, Thread.currentThread());
        run.setFuture(new FutureTask<Void>(Functions.EMPTY_RUNNABLE, null));
        run.call();
    }

    @Test
    public void runFuture() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            CompositeDisposable set = new CompositeDisposable();
            final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
            set.add(run);
            final FutureTask<Void> ft = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    run.call();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    run.setFuture(ft);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void syncWorkerCancelRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final CompositeDisposable set = new CompositeDisposable();
            final AtomicBoolean interrupted = new AtomicBoolean();
            final AtomicInteger sync = new AtomicInteger(2);
            final AtomicInteger syncb = new AtomicInteger(2);
            Runnable r0 = new Runnable() {

                @Override
                public void run() {
                    set.dispose();
                    if (sync.decrementAndGet() != 0) {
                        while (sync.get() != 0) {
                        }
                    }
                    if (syncb.decrementAndGet() != 0) {
                        while (syncb.get() != 0) {
                        }
                    }
                    for (int j = 0; j < 1000; j++) {
                        if (Thread.currentThread().isInterrupted()) {
                            interrupted.set(true);
                            break;
                        }
                    }
                }
            };
            final ScheduledRunnable run = new ScheduledRunnable(r0, set);
            set.add(run);
            final FutureTask<Void> ft = new FutureTask<>(run, null);
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    if (sync.decrementAndGet() != 0) {
                        while (sync.get() != 0) {
                        }
                    }
                    run.setFuture(ft);
                    if (syncb.decrementAndGet() != 0) {
                        while (syncb.get() != 0) {
                        }
                    }
                }
            };
            TestHelper.race(ft, r2);
            assertFalse("The task was interrupted", interrupted.get());
        }
    }

    @Test
    public void disposeAfterRun() {
        final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        run.run();
        assertEquals(ScheduledRunnable.DONE, run.get(ScheduledRunnable.FUTURE_INDEX));
        run.dispose();
        assertEquals(ScheduledRunnable.DONE, run.get(ScheduledRunnable.FUTURE_INDEX));
    }

    @Test
    public void syncDisposeIdempotent() {
        final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        run.set(ScheduledRunnable.THREAD_INDEX, Thread.currentThread());
        run.dispose();
        assertEquals(ScheduledRunnable.SYNC_DISPOSED, run.get(ScheduledRunnable.FUTURE_INDEX));
        run.dispose();
        assertEquals(ScheduledRunnable.SYNC_DISPOSED, run.get(ScheduledRunnable.FUTURE_INDEX));
        run.run();
        assertEquals(ScheduledRunnable.SYNC_DISPOSED, run.get(ScheduledRunnable.FUTURE_INDEX));
    }

    @Test
    public void asyncDisposeIdempotent() {
        final ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        run.dispose();
        assertEquals(ScheduledRunnable.ASYNC_DISPOSED, run.get(ScheduledRunnable.FUTURE_INDEX));
        run.dispose();
        assertEquals(ScheduledRunnable.ASYNC_DISPOSED, run.get(ScheduledRunnable.FUTURE_INDEX));
        run.run();
        assertEquals(ScheduledRunnable.ASYNC_DISPOSED, run.get(ScheduledRunnable.FUTURE_INDEX));
    }

    @Test
    public void noParentIsDisposed() {
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, null);
        assertFalse(run.isDisposed());
        run.run();
        assertTrue(run.isDisposed());
    }

    @Test
    public void withParentIsDisposed() {
        CompositeDisposable set = new CompositeDisposable();
        ScheduledRunnable run = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
        set.add(run);
        assertFalse(run.isDisposed());
        run.run();
        assertTrue(run.isDisposed());
        assertFalse(set.remove(run));
    }

    @Test
    public void toStringStates() {
        CompositeDisposable set = new CompositeDisposable();
        ScheduledRunnable task = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
        assertEquals("ScheduledRunnable[Waiting]", task.toString());
        task.set(ScheduledRunnable.THREAD_INDEX, Thread.currentThread());
        assertEquals("ScheduledRunnable[Running on " + Thread.currentThread() + "]", task.toString());
        task.dispose();
        assertEquals("ScheduledRunnable[Disposed(Sync)]", task.toString());
        task.set(ScheduledRunnable.FUTURE_INDEX, ScheduledRunnable.DONE);
        assertEquals("ScheduledRunnable[Finished]", task.toString());
        task = new ScheduledRunnable(Functions.EMPTY_RUNNABLE, set);
        task.dispose();
        assertEquals("ScheduledRunnable[Disposed(Async)]", task.toString());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ScheduledRunnableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRun() throws java.lang.Throwable {
            this.payloads.disposeRun.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setFutureCancelRace() throws java.lang.Throwable {
            this.payloads.setFutureCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setFutureRunRace() throws java.lang.Throwable {
            this.payloads.setFutureRunRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRace() throws java.lang.Throwable {
            this.payloads.disposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runDispose() throws java.lang.Throwable {
            this.payloads.runDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_pluginCrash() throws java.lang.Throwable {
            this.payloads.pluginCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crashReported() throws java.lang.Throwable {
            this.payloads.crashReported.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withoutParentDisposed() throws java.lang.Throwable {
            this.payloads.withoutParentDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withParentDisposed() throws java.lang.Throwable {
            this.payloads.withParentDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFutureDisposed() throws java.lang.Throwable {
            this.payloads.withFutureDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFutureDisposed2() throws java.lang.Throwable {
            this.payloads.withFutureDisposed2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFutureDisposed3() throws java.lang.Throwable {
            this.payloads.withFutureDisposed3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runFuture() throws java.lang.Throwable {
            this.payloads.runFuture.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncWorkerCancelRace() throws java.lang.Throwable {
            this.payloads.syncWorkerCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeAfterRun() throws java.lang.Throwable {
            this.payloads.disposeAfterRun.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncDisposeIdempotent() throws java.lang.Throwable {
            this.payloads.syncDisposeIdempotent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncDisposeIdempotent() throws java.lang.Throwable {
            this.payloads.asyncDisposeIdempotent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noParentIsDisposed() throws java.lang.Throwable {
            this.payloads.noParentIsDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withParentIsDisposed() throws java.lang.Throwable {
            this.payloads.withParentIsDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toStringStates() throws java.lang.Throwable {
            this.payloads.toStringStates.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ScheduledRunnableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ScheduledRunnableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ScheduledRunnableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ScheduledRunnableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ScheduledRunnableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ScheduledRunnableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ScheduledRunnableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ScheduledRunnableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement disposeRun;

            public org.junit.runners.model.Statement setFutureCancelRace;

            public org.junit.runners.model.Statement setFutureRunRace;

            public org.junit.runners.model.Statement disposeRace;

            public org.junit.runners.model.Statement runDispose;

            public org.junit.runners.model.Statement pluginCrash;

            public org.junit.runners.model.Statement crashReported;

            public org.junit.runners.model.Statement withoutParentDisposed;

            public org.junit.runners.model.Statement withParentDisposed;

            public org.junit.runners.model.Statement withFutureDisposed;

            public org.junit.runners.model.Statement withFutureDisposed2;

            public org.junit.runners.model.Statement withFutureDisposed3;

            public org.junit.runners.model.Statement runFuture;

            public org.junit.runners.model.Statement syncWorkerCancelRace;

            public org.junit.runners.model.Statement disposeAfterRun;

            public org.junit.runners.model.Statement syncDisposeIdempotent;

            public org.junit.runners.model.Statement asyncDisposeIdempotent;

            public org.junit.runners.model.Statement noParentIsDisposed;

            public org.junit.runners.model.Statement withParentIsDisposed;

            public org.junit.runners.model.Statement toStringStates;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.dispose = _ClassStatement.forPayload(ScheduledRunnableTest::dispose, "dispose", this);
            this.payloads.disposeRun = _ClassStatement.forPayload(ScheduledRunnableTest::disposeRun, "disposeRun", this);
            this.payloads.setFutureCancelRace = _ClassStatement.forPayload(ScheduledRunnableTest::setFutureCancelRace, "setFutureCancelRace", this);
            this.payloads.setFutureRunRace = _ClassStatement.forPayload(ScheduledRunnableTest::setFutureRunRace, "setFutureRunRace", this);
            this.payloads.disposeRace = _ClassStatement.forPayload(ScheduledRunnableTest::disposeRace, "disposeRace", this);
            this.payloads.runDispose = _ClassStatement.forPayload(ScheduledRunnableTest::runDispose, "runDispose", this);
            this.payloads.pluginCrash = _ClassStatement.forPayload(ScheduledRunnableTest::pluginCrash, "pluginCrash", this);
            this.payloads.crashReported = _ClassStatement.forPayload(ScheduledRunnableTest::crashReported, "crashReported", this);
            this.payloads.withoutParentDisposed = _ClassStatement.forPayload(ScheduledRunnableTest::withoutParentDisposed, "withoutParentDisposed", this);
            this.payloads.withParentDisposed = _ClassStatement.forPayload(ScheduledRunnableTest::withParentDisposed, "withParentDisposed", this);
            this.payloads.withFutureDisposed = _ClassStatement.forPayload(ScheduledRunnableTest::withFutureDisposed, "withFutureDisposed", this);
            this.payloads.withFutureDisposed2 = _ClassStatement.forPayload(ScheduledRunnableTest::withFutureDisposed2, "withFutureDisposed2", this);
            this.payloads.withFutureDisposed3 = _ClassStatement.forPayload(ScheduledRunnableTest::withFutureDisposed3, "withFutureDisposed3", this);
            this.payloads.runFuture = _ClassStatement.forPayload(ScheduledRunnableTest::runFuture, "runFuture", this);
            this.payloads.syncWorkerCancelRace = _ClassStatement.forPayload(ScheduledRunnableTest::syncWorkerCancelRace, "syncWorkerCancelRace", this);
            this.payloads.disposeAfterRun = _ClassStatement.forPayload(ScheduledRunnableTest::disposeAfterRun, "disposeAfterRun", this);
            this.payloads.syncDisposeIdempotent = _ClassStatement.forPayload(ScheduledRunnableTest::syncDisposeIdempotent, "syncDisposeIdempotent", this);
            this.payloads.asyncDisposeIdempotent = _ClassStatement.forPayload(ScheduledRunnableTest::asyncDisposeIdempotent, "asyncDisposeIdempotent", this);
            this.payloads.noParentIsDisposed = _ClassStatement.forPayload(ScheduledRunnableTest::noParentIsDisposed, "noParentIsDisposed", this);
            this.payloads.withParentIsDisposed = _ClassStatement.forPayload(ScheduledRunnableTest::withParentIsDisposed, "withParentIsDisposed", this);
            this.payloads.toStringStates = _ClassStatement.forPayload(ScheduledRunnableTest::toStringStates, "toStringStates", this);
        }
    }
}
