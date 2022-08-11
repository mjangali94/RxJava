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
import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.schedulers.TrampolineScheduler.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.*;

public class TrampolineSchedulerInternalTest extends RxJavaTest {

    @Test
    @SuppressUndeliverable
    public void scheduleDirectInterrupt() {
        Thread.currentThread().interrupt();
        final int[] calls = { 0 };
        assertSame(EmptyDisposable.INSTANCE, Schedulers.trampoline().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                calls[0]++;
            }
        }, 1, TimeUnit.SECONDS));
        assertTrue(Thread.interrupted());
        assertEquals(0, calls[0]);
    }

    @Test
    public void dispose() {
        Worker w = Schedulers.trampoline().createWorker();
        assertFalse(w.isDisposed());
        w.dispose();
        assertTrue(w.isDisposed());
        assertEquals(EmptyDisposable.INSTANCE, w.schedule(Functions.EMPTY_RUNNABLE));
    }

    @Test
    public void reentrantScheduleDispose() {
        final Worker w = Schedulers.trampoline().createWorker();
        try {
            final int[] calls = { 0, 0 };
            w.schedule(new Runnable() {

                @Override
                public void run() {
                    calls[0]++;
                    w.schedule(new Runnable() {

                        @Override
                        public void run() {
                            calls[1]++;
                        }
                    }).dispose();
                }
            });
            assertEquals(1, calls[0]);
            assertEquals(0, calls[1]);
        } finally {
            w.dispose();
        }
    }

    @Test
    public void reentrantScheduleShutdown() {
        final Worker w = Schedulers.trampoline().createWorker();
        try {
            final int[] calls = { 0, 0 };
            w.schedule(new Runnable() {

                @Override
                public void run() {
                    calls[0]++;
                    w.schedule(new Runnable() {

                        @Override
                        public void run() {
                            calls[1]++;
                        }
                    }, 1, TimeUnit.MILLISECONDS);
                    w.dispose();
                }
            });
            assertEquals(1, calls[0]);
            assertEquals(0, calls[1]);
        } finally {
            w.dispose();
        }
    }

    @Test
    public void reentrantScheduleShutdown2() {
        final Worker w = Schedulers.trampoline().createWorker();
        try {
            final int[] calls = { 0, 0 };
            w.schedule(new Runnable() {

                @Override
                public void run() {
                    calls[0]++;
                    w.dispose();
                    assertSame(EmptyDisposable.INSTANCE, w.schedule(new Runnable() {

                        @Override
                        public void run() {
                            calls[1]++;
                        }
                    }, 1, TimeUnit.MILLISECONDS));
                }
            });
            assertEquals(1, calls[0]);
            assertEquals(0, calls[1]);
        } finally {
            w.dispose();
        }
    }

    @Test
    @SuppressUndeliverable
    public void reentrantScheduleInterrupt() {
        final Worker w = Schedulers.trampoline().createWorker();
        try {
            final int[] calls = { 0 };
            Thread.currentThread().interrupt();
            w.schedule(new Runnable() {

                @Override
                public void run() {
                    calls[0]++;
                }
            }, 1, TimeUnit.DAYS);
            assertTrue(Thread.interrupted());
            assertEquals(0, calls[0]);
        } finally {
            w.dispose();
        }
    }

    @Test
    public void sleepingRunnableDisposedOnRun() {
        TrampolineWorker w = new TrampolineWorker();
        Runnable r = mock(Runnable.class);
        SleepingRunnable run = new SleepingRunnable(r, w, 0);
        w.dispose();
        run.run();
        verify(r, never()).run();
    }

    @Test
    public void sleepingRunnableNoDelayRun() {
        TrampolineWorker w = new TrampolineWorker();
        Runnable r = mock(Runnable.class);
        SleepingRunnable run = new SleepingRunnable(r, w, 0);
        run.run();
        verify(r).run();
    }

    @Test
    public void sleepingRunnableDisposedOnDelayedRun() {
        final TrampolineWorker w = new TrampolineWorker();
        Runnable r = mock(Runnable.class);
        SleepingRunnable run = new SleepingRunnable(r, w, System.currentTimeMillis() + 200);
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                w.dispose();
            }
        }, 100, TimeUnit.MILLISECONDS);
        run.run();
        verify(r, never()).run();
    }

    @Test
    public void submitAndDisposeNextTask() {
        Scheduler.Worker w = Schedulers.trampoline().createWorker();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            Runnable run = mock(Runnable.class);
            AtomicInteger sync = new AtomicInteger(2);
            w.schedule(() -> {
                Disposable d = w.schedule(run);
                Schedulers.single().scheduleDirect(() -> {
                    if (sync.decrementAndGet() != 0) {
                        while (sync.get() != 0) {
                        }
                    }
                    d.dispose();
                });
                if (sync.decrementAndGet() != 0) {
                    while (sync.get() != 0) {
                    }
                }
            });
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private TrampolineSchedulerInternalTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduleDirectInterrupt() throws java.lang.Throwable {
            this.payloads.scheduleDirectInterrupt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantScheduleDispose() throws java.lang.Throwable {
            this.payloads.reentrantScheduleDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantScheduleShutdown() throws java.lang.Throwable {
            this.payloads.reentrantScheduleShutdown.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantScheduleShutdown2() throws java.lang.Throwable {
            this.payloads.reentrantScheduleShutdown2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantScheduleInterrupt() throws java.lang.Throwable {
            this.payloads.reentrantScheduleInterrupt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sleepingRunnableDisposedOnRun() throws java.lang.Throwable {
            this.payloads.sleepingRunnableDisposedOnRun.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sleepingRunnableNoDelayRun() throws java.lang.Throwable {
            this.payloads.sleepingRunnableNoDelayRun.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sleepingRunnableDisposedOnDelayedRun() throws java.lang.Throwable {
            this.payloads.sleepingRunnableDisposedOnDelayedRun.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_submitAndDisposeNextTask() throws java.lang.Throwable {
            this.payloads.submitAndDisposeNextTask.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<TrampolineSchedulerInternalTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<TrampolineSchedulerInternalTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<TrampolineSchedulerInternalTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<TrampolineSchedulerInternalTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new TrampolineSchedulerInternalTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<TrampolineSchedulerInternalTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(TrampolineSchedulerInternalTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(TrampolineSchedulerInternalTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement scheduleDirectInterrupt;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement reentrantScheduleDispose;

            public org.junit.runners.model.Statement reentrantScheduleShutdown;

            public org.junit.runners.model.Statement reentrantScheduleShutdown2;

            public org.junit.runners.model.Statement reentrantScheduleInterrupt;

            public org.junit.runners.model.Statement sleepingRunnableDisposedOnRun;

            public org.junit.runners.model.Statement sleepingRunnableNoDelayRun;

            public org.junit.runners.model.Statement sleepingRunnableDisposedOnDelayedRun;

            public org.junit.runners.model.Statement submitAndDisposeNextTask;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.scheduleDirectInterrupt = _ClassStatement.forPayload(TrampolineSchedulerInternalTest::scheduleDirectInterrupt, "scheduleDirectInterrupt", this);
            this.payloads.dispose = _ClassStatement.forPayload(TrampolineSchedulerInternalTest::dispose, "dispose", this);
            this.payloads.reentrantScheduleDispose = _ClassStatement.forPayload(TrampolineSchedulerInternalTest::reentrantScheduleDispose, "reentrantScheduleDispose", this);
            this.payloads.reentrantScheduleShutdown = _ClassStatement.forPayload(TrampolineSchedulerInternalTest::reentrantScheduleShutdown, "reentrantScheduleShutdown", this);
            this.payloads.reentrantScheduleShutdown2 = _ClassStatement.forPayload(TrampolineSchedulerInternalTest::reentrantScheduleShutdown2, "reentrantScheduleShutdown2", this);
            this.payloads.reentrantScheduleInterrupt = _ClassStatement.forPayload(TrampolineSchedulerInternalTest::reentrantScheduleInterrupt, "reentrantScheduleInterrupt", this);
            this.payloads.sleepingRunnableDisposedOnRun = _ClassStatement.forPayload(TrampolineSchedulerInternalTest::sleepingRunnableDisposedOnRun, "sleepingRunnableDisposedOnRun", this);
            this.payloads.sleepingRunnableNoDelayRun = _ClassStatement.forPayload(TrampolineSchedulerInternalTest::sleepingRunnableNoDelayRun, "sleepingRunnableNoDelayRun", this);
            this.payloads.sleepingRunnableDisposedOnDelayedRun = _ClassStatement.forPayload(TrampolineSchedulerInternalTest::sleepingRunnableDisposedOnDelayedRun, "sleepingRunnableDisposedOnDelayedRun", this);
            this.payloads.submitAndDisposeNextTask = _ClassStatement.forPayload(TrampolineSchedulerInternalTest::submitAndDisposeNextTask, "submitAndDisposeNextTask", this);
        }
    }
}
