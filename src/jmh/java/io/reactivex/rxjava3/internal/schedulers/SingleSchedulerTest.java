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
import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.schedulers.SingleScheduler.ScheduledWorker;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.testsupport.*;

public class SingleSchedulerTest extends AbstractSchedulerTests {

    @Test
    @SuppressUndeliverable
    public void shutdownRejects() {
        final int[] calls = { 0 };
        Runnable r = new Runnable() {

            @Override
            public void run() {
                calls[0]++;
            }
        };
        Scheduler s = new SingleScheduler();
        s.shutdown();
        assertEquals(Disposable.disposed(), s.scheduleDirect(r));
        assertEquals(Disposable.disposed(), s.scheduleDirect(r, 1, TimeUnit.SECONDS));
        assertEquals(Disposable.disposed(), s.schedulePeriodicallyDirect(r, 1, 1, TimeUnit.SECONDS));
        Worker w = s.createWorker();
        ((ScheduledWorker) w).executor.shutdownNow();
        assertEquals(Disposable.disposed(), w.schedule(r));
        assertEquals(Disposable.disposed(), w.schedule(r, 1, TimeUnit.SECONDS));
        assertEquals(Disposable.disposed(), w.schedulePeriodically(r, 1, 1, TimeUnit.SECONDS));
        assertEquals(0, calls[0]);
        w.dispose();
        assertTrue(w.isDisposed());
    }

    @Test
    public void startRace() {
        final Scheduler s = new SingleScheduler();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            s.shutdown();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    s.start();
                }
            };
            TestHelper.race(r1, r1);
        }
    }

    @Test
    public void runnableDisposedAsync() throws Exception {
        final Scheduler s = Schedulers.single();
        Disposable d = s.scheduleDirect(Functions.EMPTY_RUNNABLE);
        while (!d.isDisposed()) {
            Thread.sleep(1);
        }
    }

    @Test
    public void runnableDisposedAsyncCrash() throws Exception {
        final Scheduler s = Schedulers.single();
        Disposable d = s.scheduleDirect(new Runnable() {

            @Override
            public void run() {
                throw new IllegalStateException();
            }
        });
        while (!d.isDisposed()) {
            Thread.sleep(1);
        }
    }

    @Test
    public void runnableDisposedAsyncTimed() throws Exception {
        final Scheduler s = Schedulers.single();
        Disposable d = s.scheduleDirect(Functions.EMPTY_RUNNABLE, 1, TimeUnit.MILLISECONDS);
        while (!d.isDisposed()) {
            Thread.sleep(1);
        }
    }

    @Override
    protected Scheduler getScheduler() {
        return Schedulers.single();
    }

    @Test
    public void zeroPeriodRejectedExecution() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Scheduler s = RxJavaPlugins.createSingleScheduler(new RxThreadFactory("Test"));
            s.shutdown();
            Runnable run = mock(Runnable.class);
            s.schedulePeriodicallyDirect(run, 1, 0, TimeUnit.MILLISECONDS);
            Thread.sleep(100);
            verify(run, never()).run();
            TestHelper.assertUndeliverable(errors, 0, RejectedExecutionException.class);
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public SingleSchedulerTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nestedActions() throws java.lang.Throwable {
            this.payloads.nestedActions.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nestedScheduling() throws java.lang.Throwable {
            this.payloads.nestedScheduling.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sequenceOfActions() throws java.lang.Throwable {
            this.payloads.sequenceOfActions.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sequenceOfDelayedActions() throws java.lang.Throwable {
            this.payloads.sequenceOfDelayedActions.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixOfDelayedAndNonDelayedActions() throws java.lang.Throwable {
            this.payloads.mixOfDelayedAndNonDelayedActions.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_recursiveExecution() throws java.lang.Throwable {
            this.payloads.recursiveExecution.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_recursiveExecutionWithDelayTime() throws java.lang.Throwable {
            this.payloads.recursiveExecutionWithDelayTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_recursiveSchedulerInObservable() throws java.lang.Throwable {
            this.payloads.recursiveSchedulerInObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concurrentOnNextFailsValidation() throws java.lang.Throwable {
            this.payloads.concurrentOnNextFailsValidation.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOn() throws java.lang.Throwable {
            this.payloads.observeOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOnNestedConcurrency() throws java.lang.Throwable {
            this.payloads.subscribeOnNestedConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduleDirect() throws java.lang.Throwable {
            this.payloads.scheduleDirect.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduleDirectDelayed() throws java.lang.Throwable {
            this.payloads.scheduleDirectDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduleDirectPeriodic() throws java.lang.Throwable {
            this.payloads.scheduleDirectPeriodic.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulePeriodicallyDirectZeroPeriod() throws java.lang.Throwable {
            this.payloads.schedulePeriodicallyDirectZeroPeriod.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulePeriodicallyZeroPeriod() throws java.lang.Throwable {
            this.payloads.schedulePeriodicallyZeroPeriod.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduleDirectDecoratesRunnable() throws java.lang.Throwable {
            this.payloads.scheduleDirectDecoratesRunnable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduleDirectWithDelayDecoratesRunnable() throws java.lang.Throwable {
            this.payloads.scheduleDirectWithDelayDecoratesRunnable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulePeriodicallyDirectDecoratesRunnable() throws java.lang.Throwable {
            this.payloads.schedulePeriodicallyDirectDecoratesRunnable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unwrapDefaultPeriodicTask() throws java.lang.Throwable {
            this.payloads.unwrapDefaultPeriodicTask.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unwrapScheduleDirectTask() throws java.lang.Throwable {
            this.payloads.unwrapScheduleDirectTask.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduleDirectNullRunnable() throws java.lang.Throwable {
            this.payloads.scheduleDirectNullRunnable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduleDirectWithDelayNullRunnable() throws java.lang.Throwable {
            this.payloads.scheduleDirectWithDelayNullRunnable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulePeriodicallyDirectNullRunnable() throws java.lang.Throwable {
            this.payloads.schedulePeriodicallyDirectNullRunnable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduleDirectPrint() throws java.lang.Throwable {
            this.payloads.scheduleDirectPrint.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulePrint() throws java.lang.Throwable {
            this.payloads.schedulePrint.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shutdownRejects() throws java.lang.Throwable {
            this.payloads.shutdownRejects.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startRace() throws java.lang.Throwable {
            this.payloads.startRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runnableDisposedAsync() throws java.lang.Throwable {
            this.payloads.runnableDisposedAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runnableDisposedAsyncCrash() throws java.lang.Throwable {
            this.payloads.runnableDisposedAsyncCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runnableDisposedAsyncTimed() throws java.lang.Throwable {
            this.payloads.runnableDisposedAsyncTimed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zeroPeriodRejectedExecution() throws java.lang.Throwable {
            this.payloads.zeroPeriodRejectedExecution.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleSchedulerTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleSchedulerTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleSchedulerTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleSchedulerTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleSchedulerTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleSchedulerTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleSchedulerTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleSchedulerTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement nestedActions;

            public org.junit.runners.model.Statement nestedScheduling;

            public org.junit.runners.model.Statement sequenceOfActions;

            public org.junit.runners.model.Statement sequenceOfDelayedActions;

            public org.junit.runners.model.Statement mixOfDelayedAndNonDelayedActions;

            public org.junit.runners.model.Statement recursiveExecution;

            public org.junit.runners.model.Statement recursiveExecutionWithDelayTime;

            public org.junit.runners.model.Statement recursiveSchedulerInObservable;

            public org.junit.runners.model.Statement concurrentOnNextFailsValidation;

            public org.junit.runners.model.Statement observeOn;

            public org.junit.runners.model.Statement subscribeOnNestedConcurrency;

            public org.junit.runners.model.Statement scheduleDirect;

            public org.junit.runners.model.Statement scheduleDirectDelayed;

            public org.junit.runners.model.Statement scheduleDirectPeriodic;

            public org.junit.runners.model.Statement schedulePeriodicallyDirectZeroPeriod;

            public org.junit.runners.model.Statement schedulePeriodicallyZeroPeriod;

            public org.junit.runners.model.Statement scheduleDirectDecoratesRunnable;

            public org.junit.runners.model.Statement scheduleDirectWithDelayDecoratesRunnable;

            public org.junit.runners.model.Statement schedulePeriodicallyDirectDecoratesRunnable;

            public org.junit.runners.model.Statement unwrapDefaultPeriodicTask;

            public org.junit.runners.model.Statement unwrapScheduleDirectTask;

            public org.junit.runners.model.Statement scheduleDirectNullRunnable;

            public org.junit.runners.model.Statement scheduleDirectWithDelayNullRunnable;

            public org.junit.runners.model.Statement schedulePeriodicallyDirectNullRunnable;

            public org.junit.runners.model.Statement scheduleDirectPrint;

            public org.junit.runners.model.Statement schedulePrint;

            public org.junit.runners.model.Statement shutdownRejects;

            public org.junit.runners.model.Statement startRace;

            public org.junit.runners.model.Statement runnableDisposedAsync;

            public org.junit.runners.model.Statement runnableDisposedAsyncCrash;

            public org.junit.runners.model.Statement runnableDisposedAsyncTimed;

            public org.junit.runners.model.Statement zeroPeriodRejectedExecution;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.nestedActions = _ClassStatement.forPayload(SingleSchedulerTest::nestedActions, "nestedActions", this);
            this.payloads.nestedScheduling = _ClassStatement.forPayload(SingleSchedulerTest::nestedScheduling, "nestedScheduling", this);
            this.payloads.sequenceOfActions = _ClassStatement.forPayload(SingleSchedulerTest::sequenceOfActions, "sequenceOfActions", this);
            this.payloads.sequenceOfDelayedActions = _ClassStatement.forPayload(SingleSchedulerTest::sequenceOfDelayedActions, "sequenceOfDelayedActions", this);
            this.payloads.mixOfDelayedAndNonDelayedActions = _ClassStatement.forPayload(SingleSchedulerTest::mixOfDelayedAndNonDelayedActions, "mixOfDelayedAndNonDelayedActions", this);
            this.payloads.recursiveExecution = _ClassStatement.forPayload(SingleSchedulerTest::recursiveExecution, "recursiveExecution", this);
            this.payloads.recursiveExecutionWithDelayTime = _ClassStatement.forPayload(SingleSchedulerTest::recursiveExecutionWithDelayTime, "recursiveExecutionWithDelayTime", this);
            this.payloads.recursiveSchedulerInObservable = _ClassStatement.forPayload(SingleSchedulerTest::recursiveSchedulerInObservable, "recursiveSchedulerInObservable", this);
            this.payloads.concurrentOnNextFailsValidation = _ClassStatement.forPayload(SingleSchedulerTest::concurrentOnNextFailsValidation, "concurrentOnNextFailsValidation", this);
            this.payloads.observeOn = _ClassStatement.forPayload(SingleSchedulerTest::observeOn, "observeOn", this);
            this.payloads.subscribeOnNestedConcurrency = _ClassStatement.forPayload(SingleSchedulerTest::subscribeOnNestedConcurrency, "subscribeOnNestedConcurrency", this);
            this.payloads.scheduleDirect = _ClassStatement.forPayload(SingleSchedulerTest::scheduleDirect, "scheduleDirect", this);
            this.payloads.scheduleDirectDelayed = _ClassStatement.forPayload(SingleSchedulerTest::scheduleDirectDelayed, "scheduleDirectDelayed", this);
            this.payloads.scheduleDirectPeriodic = _ClassStatement.forPayload(SingleSchedulerTest::scheduleDirectPeriodic, "scheduleDirectPeriodic", this);
            this.payloads.schedulePeriodicallyDirectZeroPeriod = _ClassStatement.forPayload(SingleSchedulerTest::schedulePeriodicallyDirectZeroPeriod, "schedulePeriodicallyDirectZeroPeriod", this);
            this.payloads.schedulePeriodicallyZeroPeriod = _ClassStatement.forPayload(SingleSchedulerTest::schedulePeriodicallyZeroPeriod, "schedulePeriodicallyZeroPeriod", this);
            this.payloads.scheduleDirectDecoratesRunnable = _ClassStatement.forPayload(SingleSchedulerTest::scheduleDirectDecoratesRunnable, "scheduleDirectDecoratesRunnable", this);
            this.payloads.scheduleDirectWithDelayDecoratesRunnable = _ClassStatement.forPayload(SingleSchedulerTest::scheduleDirectWithDelayDecoratesRunnable, "scheduleDirectWithDelayDecoratesRunnable", this);
            this.payloads.schedulePeriodicallyDirectDecoratesRunnable = _ClassStatement.forPayload(SingleSchedulerTest::schedulePeriodicallyDirectDecoratesRunnable, "schedulePeriodicallyDirectDecoratesRunnable", this);
            this.payloads.unwrapDefaultPeriodicTask = _ClassStatement.forPayload(SingleSchedulerTest::unwrapDefaultPeriodicTask, "unwrapDefaultPeriodicTask", this);
            this.payloads.unwrapScheduleDirectTask = _ClassStatement.forPayload(SingleSchedulerTest::unwrapScheduleDirectTask, "unwrapScheduleDirectTask", this);
            this.payloads.scheduleDirectNullRunnable = _ClassStatement.forPayload(SingleSchedulerTest::scheduleDirectNullRunnable, "scheduleDirectNullRunnable", this);
            this.payloads.scheduleDirectWithDelayNullRunnable = _ClassStatement.forPayload(SingleSchedulerTest::scheduleDirectWithDelayNullRunnable, "scheduleDirectWithDelayNullRunnable", this);
            this.payloads.schedulePeriodicallyDirectNullRunnable = _ClassStatement.forPayload(SingleSchedulerTest::schedulePeriodicallyDirectNullRunnable, "schedulePeriodicallyDirectNullRunnable", this);
            this.payloads.scheduleDirectPrint = _ClassStatement.forPayload(SingleSchedulerTest::scheduleDirectPrint, "scheduleDirectPrint", this);
            this.payloads.schedulePrint = _ClassStatement.forPayload(SingleSchedulerTest::schedulePrint, "schedulePrint", this);
            this.payloads.shutdownRejects = _ClassStatement.forPayload(SingleSchedulerTest::shutdownRejects, "shutdownRejects", this);
            this.payloads.startRace = _ClassStatement.forPayload(SingleSchedulerTest::startRace, "startRace", this);
            this.payloads.runnableDisposedAsync = _ClassStatement.forPayload(SingleSchedulerTest::runnableDisposedAsync, "runnableDisposedAsync", this);
            this.payloads.runnableDisposedAsyncCrash = _ClassStatement.forPayload(SingleSchedulerTest::runnableDisposedAsyncCrash, "runnableDisposedAsyncCrash", this);
            this.payloads.runnableDisposedAsyncTimed = _ClassStatement.forPayload(SingleSchedulerTest::runnableDisposedAsyncTimed, "runnableDisposedAsyncTimed", this);
            this.payloads.zeroPeriodRejectedExecution = _ClassStatement.forPayload(SingleSchedulerTest::zeroPeriodRejectedExecution, "zeroPeriodRejectedExecution", this);
        }
    }
}
