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
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.processors.UnicastProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableDoAfterNextTest extends RxJavaTest {

    final List<Integer> values = new ArrayList<>();

    final Consumer<Integer> afterNext = new Consumer<Integer>() {

        @Override
        public void accept(Integer e) throws Exception {
            values.add(-e);
        }
    };

    final TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

        @Override
        public void onNext(Integer t) {
            super.onNext(t);
            FlowableDoAfterNextTest.this.values.add(t);
        }
    };

    @Test
    public void just() {
        Flowable.just(1).doAfterNext(afterNext).subscribeWith(ts).assertResult(1);
        assertEquals(Arrays.asList(1, -1), values);
    }

    @Test
    public void range() {
        Flowable.range(1, 5).doAfterNext(afterNext).subscribeWith(ts).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(1, -1, 2, -2, 3, -3, 4, -4, 5, -5), values);
    }

    @Test
    public void error() {
        Flowable.<Integer>error(new TestException()).doAfterNext(afterNext).subscribeWith(ts).assertFailure(TestException.class);
        assertTrue(values.isEmpty());
    }

    @Test
    public void empty() {
        Flowable.<Integer>empty().doAfterNext(afterNext).subscribeWith(ts).assertResult();
        assertTrue(values.isEmpty());
    }

    @Test
    public void syncFused() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.SYNC);
        Flowable.range(1, 5).doAfterNext(afterNext).subscribe(ts0);
        ts0.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(-1, -2, -3, -4, -5), values);
    }

    @Test
    public void asyncFusedRejected() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC);
        Flowable.range(1, 5).doAfterNext(afterNext).subscribe(ts0);
        ts0.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(-1, -2, -3, -4, -5), values);
    }

    @Test
    public void asyncFused() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.doAfterNext(afterNext).subscribe(ts0);
        ts0.assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(-1, -2, -3, -4, -5), values);
    }

    @Test
    public void justConditional() {
        Flowable.just(1).doAfterNext(afterNext).filter(Functions.alwaysTrue()).subscribeWith(ts).assertResult(1);
        assertEquals(Arrays.asList(1, -1), values);
    }

    @Test
    public void rangeConditional() {
        Flowable.range(1, 5).doAfterNext(afterNext).filter(Functions.alwaysTrue()).subscribeWith(ts).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(1, -1, 2, -2, 3, -3, 4, -4, 5, -5), values);
    }

    @Test
    public void errorConditional() {
        Flowable.<Integer>error(new TestException()).doAfterNext(afterNext).filter(Functions.alwaysTrue()).subscribeWith(ts).assertFailure(TestException.class);
        assertTrue(values.isEmpty());
    }

    @Test
    public void emptyConditional() {
        Flowable.<Integer>empty().doAfterNext(afterNext).filter(Functions.alwaysTrue()).subscribeWith(ts).assertResult();
        assertTrue(values.isEmpty());
    }

    @Test
    public void syncFusedConditional() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.SYNC);
        Flowable.range(1, 5).doAfterNext(afterNext).filter(Functions.alwaysTrue()).subscribe(ts0);
        ts0.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(-1, -2, -3, -4, -5), values);
    }

    @Test
    public void asyncFusedRejectedConditional() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC);
        Flowable.range(1, 5).doAfterNext(afterNext).filter(Functions.alwaysTrue()).subscribe(ts0);
        ts0.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(-1, -2, -3, -4, -5), values);
    }

    @Test
    public void asyncFusedConditional() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.doAfterNext(afterNext).filter(Functions.alwaysTrue()).subscribe(ts0);
        ts0.assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(-1, -2, -3, -4, -5), values);
    }

    @Test
    public void consumerThrows() {
        Flowable.just(1, 2).doAfterNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer e) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void consumerThrowsConditional() {
        Flowable.just(1, 2).doAfterNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer e) throws Exception {
                throw new TestException();
            }
        }).filter(Functions.alwaysTrue()).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void consumerThrowsConditional2() {
        Flowable.just(1, 2).hide().doAfterNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer e) throws Exception {
                throw new TestException();
            }
        }).filter(Functions.alwaysTrue()).test().assertFailure(TestException.class, 1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableDoAfterNextTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.payloads.just.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_range() throws java.lang.Throwable {
            this.payloads.range.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFused() throws java.lang.Throwable {
            this.payloads.syncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedRejected() throws java.lang.Throwable {
            this.payloads.asyncFusedRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFused() throws java.lang.Throwable {
            this.payloads.asyncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justConditional() throws java.lang.Throwable {
            this.payloads.justConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeConditional() throws java.lang.Throwable {
            this.payloads.rangeConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorConditional() throws java.lang.Throwable {
            this.payloads.errorConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyConditional() throws java.lang.Throwable {
            this.payloads.emptyConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedConditional() throws java.lang.Throwable {
            this.payloads.syncFusedConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedRejectedConditional() throws java.lang.Throwable {
            this.payloads.asyncFusedRejectedConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedConditional() throws java.lang.Throwable {
            this.payloads.asyncFusedConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_consumerThrows() throws java.lang.Throwable {
            this.payloads.consumerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_consumerThrowsConditional() throws java.lang.Throwable {
            this.payloads.consumerThrowsConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_consumerThrowsConditional2() throws java.lang.Throwable {
            this.payloads.consumerThrowsConditional2.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoAfterNextTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoAfterNextTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoAfterNextTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoAfterNextTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableDoAfterNextTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoAfterNextTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableDoAfterNextTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableDoAfterNextTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement just;

            public org.junit.runners.model.Statement range;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement syncFused;

            public org.junit.runners.model.Statement asyncFusedRejected;

            public org.junit.runners.model.Statement asyncFused;

            public org.junit.runners.model.Statement justConditional;

            public org.junit.runners.model.Statement rangeConditional;

            public org.junit.runners.model.Statement errorConditional;

            public org.junit.runners.model.Statement emptyConditional;

            public org.junit.runners.model.Statement syncFusedConditional;

            public org.junit.runners.model.Statement asyncFusedRejectedConditional;

            public org.junit.runners.model.Statement asyncFusedConditional;

            public org.junit.runners.model.Statement consumerThrows;

            public org.junit.runners.model.Statement consumerThrowsConditional;

            public org.junit.runners.model.Statement consumerThrowsConditional2;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.just = _ClassStatement.forPayload(FlowableDoAfterNextTest::just, "just", this);
            this.payloads.range = _ClassStatement.forPayload(FlowableDoAfterNextTest::range, "range", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableDoAfterNextTest::error, "error", this);
            this.payloads.empty = _ClassStatement.forPayload(FlowableDoAfterNextTest::empty, "empty", this);
            this.payloads.syncFused = _ClassStatement.forPayload(FlowableDoAfterNextTest::syncFused, "syncFused", this);
            this.payloads.asyncFusedRejected = _ClassStatement.forPayload(FlowableDoAfterNextTest::asyncFusedRejected, "asyncFusedRejected", this);
            this.payloads.asyncFused = _ClassStatement.forPayload(FlowableDoAfterNextTest::asyncFused, "asyncFused", this);
            this.payloads.justConditional = _ClassStatement.forPayload(FlowableDoAfterNextTest::justConditional, "justConditional", this);
            this.payloads.rangeConditional = _ClassStatement.forPayload(FlowableDoAfterNextTest::rangeConditional, "rangeConditional", this);
            this.payloads.errorConditional = _ClassStatement.forPayload(FlowableDoAfterNextTest::errorConditional, "errorConditional", this);
            this.payloads.emptyConditional = _ClassStatement.forPayload(FlowableDoAfterNextTest::emptyConditional, "emptyConditional", this);
            this.payloads.syncFusedConditional = _ClassStatement.forPayload(FlowableDoAfterNextTest::syncFusedConditional, "syncFusedConditional", this);
            this.payloads.asyncFusedRejectedConditional = _ClassStatement.forPayload(FlowableDoAfterNextTest::asyncFusedRejectedConditional, "asyncFusedRejectedConditional", this);
            this.payloads.asyncFusedConditional = _ClassStatement.forPayload(FlowableDoAfterNextTest::asyncFusedConditional, "asyncFusedConditional", this);
            this.payloads.consumerThrows = _ClassStatement.forPayload(FlowableDoAfterNextTest::consumerThrows, "consumerThrows", this);
            this.payloads.consumerThrowsConditional = _ClassStatement.forPayload(FlowableDoAfterNextTest::consumerThrowsConditional, "consumerThrowsConditional", this);
            this.payloads.consumerThrowsConditional2 = _ClassStatement.forPayload(FlowableDoAfterNextTest::consumerThrowsConditional2, "consumerThrowsConditional2", this);
        }
    }
}
