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
package io.reactivex.rxjava3.internal.operators.flowable;

import static org.junit.Assert.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableIntervalRangeTest extends RxJavaTest {

    @Test
    public void simple() throws Exception {
        Flowable.intervalRange(5, 5, 50, 50, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(5L, 6L, 7L, 8L, 9L);
    }

    @Test
    public void customScheduler() {
        Flowable.intervalRange(1, 5, 1, 1, TimeUnit.MILLISECONDS, Schedulers.single()).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    public void countZero() {
        Flowable.intervalRange(1, 0, 1, 1, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void countNegative() {
        try {
            Flowable.intervalRange(1, -1, 1, 1, TimeUnit.MILLISECONDS);
            fail("Should have thrown!");
        } catch (IllegalArgumentException ex) {
            assertEquals("count >= 0 required but it was -1", ex.getMessage());
        }
    }

    @Test
    public void longOverflow() {
        Flowable.intervalRange(Long.MAX_VALUE - 1, 2, 1, 1, TimeUnit.MILLISECONDS);
        Flowable.intervalRange(Long.MIN_VALUE, Long.MAX_VALUE, 1, 1, TimeUnit.MILLISECONDS);
        try {
            Flowable.intervalRange(Long.MAX_VALUE - 1, 3, 1, 1, TimeUnit.MILLISECONDS);
            fail("Should have thrown!");
        } catch (IllegalArgumentException ex) {
            assertEquals("Overflow! start + count is bigger than Long.MAX_VALUE", ex.getMessage());
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.intervalRange(1, 2, 1, 1, TimeUnit.MILLISECONDS));
    }

    @Test
    public void backpressureBounded() {
        Flowable.intervalRange(1, 2, 1, 1, TimeUnit.MILLISECONDS).test(2L).awaitDone(5, TimeUnit.SECONDS).assertResult(1L, 2L);
    }

    @Test
    public void backpressureOverflow() {
        Flowable.intervalRange(1, 3, 1, 1, TimeUnit.MILLISECONDS).test(2L).awaitDone(5, TimeUnit.SECONDS).assertFailure(MissingBackpressureException.class, 1L, 2L);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.intervalRange(1, 3, 1, 1, TimeUnit.MILLISECONDS));
    }

    @Test
    public void take() {
        Flowable.intervalRange(1, 2, 1, 1, TimeUnit.MILLISECONDS).take(1).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1L);
    }

    @Test
    public void cancel() {
        Flowable.intervalRange(0, 20, 1, 1, TimeUnit.MILLISECONDS, Schedulers.trampoline()).take(10).test().assertResult(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);
    }

    @Test
    public void takeSameAsRange() {
        Flowable.intervalRange(0, 2, 1, 1, TimeUnit.MILLISECONDS, Schedulers.trampoline()).take(2).test().assertResult(0L, 1L);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableIntervalRangeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.payloads.simple.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customScheduler() throws java.lang.Throwable {
            this.payloads.customScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_countZero() throws java.lang.Throwable {
            this.payloads.countZero.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_countNegative() throws java.lang.Throwable {
            this.payloads.countNegative.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_longOverflow() throws java.lang.Throwable {
            this.payloads.longOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureBounded() throws java.lang.Throwable {
            this.payloads.backpressureBounded.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureOverflow() throws java.lang.Throwable {
            this.payloads.backpressureOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeSameAsRange() throws java.lang.Throwable {
            this.payloads.takeSameAsRange.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableIntervalRangeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableIntervalRangeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableIntervalRangeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableIntervalRangeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableIntervalRangeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableIntervalRangeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableIntervalRangeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableIntervalRangeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement simple;

            public org.junit.runners.model.Statement customScheduler;

            public org.junit.runners.model.Statement countZero;

            public org.junit.runners.model.Statement countNegative;

            public org.junit.runners.model.Statement longOverflow;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement backpressureBounded;

            public org.junit.runners.model.Statement backpressureOverflow;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement takeSameAsRange;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.simple = _ClassStatement.forPayload(FlowableIntervalRangeTest::simple, "simple", this);
            this.payloads.customScheduler = _ClassStatement.forPayload(FlowableIntervalRangeTest::customScheduler, "customScheduler", this);
            this.payloads.countZero = _ClassStatement.forPayload(FlowableIntervalRangeTest::countZero, "countZero", this);
            this.payloads.countNegative = _ClassStatement.forPayload(FlowableIntervalRangeTest::countNegative, "countNegative", this);
            this.payloads.longOverflow = _ClassStatement.forPayload(FlowableIntervalRangeTest::longOverflow, "longOverflow", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableIntervalRangeTest::dispose, "dispose", this);
            this.payloads.backpressureBounded = _ClassStatement.forPayload(FlowableIntervalRangeTest::backpressureBounded, "backpressureBounded", this);
            this.payloads.backpressureOverflow = _ClassStatement.forPayload(FlowableIntervalRangeTest::backpressureOverflow, "backpressureOverflow", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableIntervalRangeTest::badRequest, "badRequest", this);
            this.payloads.take = _ClassStatement.forPayload(FlowableIntervalRangeTest::take, "take", this);
            this.payloads.cancel = _ClassStatement.forPayload(FlowableIntervalRangeTest::cancel, "cancel", this);
            this.payloads.takeSameAsRange = _ClassStatement.forPayload(FlowableIntervalRangeTest::takeSameAsRange, "takeSameAsRange", this);
        }
    }
}
