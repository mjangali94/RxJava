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
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.internal.functions.Functions;

public class ImmediateThinSchedulerTest extends RxJavaTest {

    @Test
    public void scheduleDirect() {
        final int[] count = { 0 };
        ImmediateThinScheduler.INSTANCE.scheduleDirect(new Runnable() {

            @Override
            public void run() {
                count[0]++;
            }
        });
        assertEquals(1, count[0]);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void scheduleDirectTimed() {
        ImmediateThinScheduler.INSTANCE.scheduleDirect(Functions.EMPTY_RUNNABLE, 1, TimeUnit.SECONDS);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void scheduleDirectPeriodic() {
        ImmediateThinScheduler.INSTANCE.schedulePeriodicallyDirect(Functions.EMPTY_RUNNABLE, 1, 1, TimeUnit.SECONDS);
    }

    @Test
    public void schedule() {
        final int[] count = { 0 };
        Worker w = ImmediateThinScheduler.INSTANCE.createWorker();
        assertFalse(w.isDisposed());
        w.schedule(new Runnable() {

            @Override
            public void run() {
                count[0]++;
            }
        });
        assertEquals(1, count[0]);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void scheduleTimed() {
        ImmediateThinScheduler.INSTANCE.createWorker().schedule(Functions.EMPTY_RUNNABLE, 1, TimeUnit.SECONDS);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void schedulePeriodic() {
        ImmediateThinScheduler.INSTANCE.createWorker().schedulePeriodically(Functions.EMPTY_RUNNABLE, 1, 1, TimeUnit.SECONDS);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ImmediateThinSchedulerTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduleDirect() throws java.lang.Throwable {
            this.payloads.scheduleDirect.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduleDirectTimed() throws java.lang.Throwable {
            this.payloads.scheduleDirectTimed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduleDirectPeriodic() throws java.lang.Throwable {
            this.payloads.scheduleDirectPeriodic.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedule() throws java.lang.Throwable {
            this.payloads.schedule.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduleTimed() throws java.lang.Throwable {
            this.payloads.scheduleTimed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulePeriodic() throws java.lang.Throwable {
            this.payloads.schedulePeriodic.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ImmediateThinSchedulerTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ImmediateThinSchedulerTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ImmediateThinSchedulerTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ImmediateThinSchedulerTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ImmediateThinSchedulerTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ImmediateThinSchedulerTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ImmediateThinSchedulerTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ImmediateThinSchedulerTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement scheduleDirect;

            public org.junit.runners.model.Statement scheduleDirectTimed;

            public org.junit.runners.model.Statement scheduleDirectPeriodic;

            public org.junit.runners.model.Statement schedule;

            public org.junit.runners.model.Statement scheduleTimed;

            public org.junit.runners.model.Statement schedulePeriodic;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.scheduleDirect = _ClassStatement.forPayload(ImmediateThinSchedulerTest::scheduleDirect, "scheduleDirect", this);
            this.payloads.scheduleDirectTimed = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ImmediateThinSchedulerTest::scheduleDirectTimed, java.lang.UnsupportedOperationException.class), "scheduleDirectTimed", this);
            this.payloads.scheduleDirectPeriodic = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ImmediateThinSchedulerTest::scheduleDirectPeriodic, java.lang.UnsupportedOperationException.class), "scheduleDirectPeriodic", this);
            this.payloads.schedule = _ClassStatement.forPayload(ImmediateThinSchedulerTest::schedule, "schedule", this);
            this.payloads.scheduleTimed = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ImmediateThinSchedulerTest::scheduleTimed, java.lang.UnsupportedOperationException.class), "scheduleTimed", this);
            this.payloads.schedulePeriodic = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ImmediateThinSchedulerTest::schedulePeriodic, java.lang.UnsupportedOperationException.class), "schedulePeriodic", this);
        }
    }
}
