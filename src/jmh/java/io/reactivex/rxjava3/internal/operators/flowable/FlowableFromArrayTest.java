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

import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.operators.ScalarSupplier;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableFromArrayTest extends RxJavaTest {

    Flowable<Integer> create(int n) {
        Integer[] array = new Integer[n];
        for (int i = 0; i < n; i++) {
            array[i] = i;
        }
        return Flowable.fromArray(array);
    }

    @Test
    public void simple() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        create(1000).subscribe(ts);
        ts.assertNoErrors();
        ts.assertValueCount(1000);
        ts.assertComplete();
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        create(1000).subscribe(ts);
        ts.assertNoErrors();
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.request(10);
        ts.assertNoErrors();
        ts.assertValueCount(10);
        ts.assertNotComplete();
        ts.request(1000);
        ts.assertNoErrors();
        ts.assertValueCount(1000);
        ts.assertComplete();
    }

    @Test
    public void conditionalBackpressure() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        create(1000).filter(Functions.alwaysTrue()).subscribe(ts);
        ts.assertNoErrors();
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.request(10);
        ts.assertNoErrors();
        ts.assertValueCount(10);
        ts.assertNotComplete();
        ts.request(1000);
        ts.assertNoErrors();
        ts.assertValueCount(1000);
        ts.assertComplete();
    }

    @Test
    public void empty() {
        Assert.assertSame(Flowable.empty(), Flowable.fromArray(new Object[0]));
    }

    @Test
    public void just() {
        Flowable<Integer> source = Flowable.fromArray(new Integer[] { 1 });
        Assert.assertTrue(source.getClass().toString(), source instanceof ScalarSupplier);
    }

    @Test
    public void just10Arguments() {
        Flowable.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.just(1, 2, 3));
    }

    @Test
    public void conditionalOneIsNull() {
        Flowable.fromArray(new Integer[] { null, 1 }).filter(Functions.alwaysTrue()).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void conditionalOneIsNullSlowPath() {
        Flowable.fromArray(new Integer[] { null, 1 }).filter(Functions.alwaysTrue()).test(2L).assertFailure(NullPointerException.class);
    }

    @Test
    public void conditionalOneByOne() {
        Flowable.fromArray(new Integer[] { 1, 2, 3, 4, 5 }).filter(Functions.alwaysTrue()).rebatchRequests(1).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void conditionalFiltered() {
        Flowable.fromArray(new Integer[] { 1, 2, 3, 4, 5 }).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).test().assertResult(2, 4);
    }

    @Test
    public void conditionalSlowPathCancel() {
        Flowable.fromArray(new Integer[] { 1, 2, 3, 4, 5 }).filter(Functions.alwaysTrue()).subscribeWith(new TestSubscriber<Integer>(5L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    cancel();
                    onComplete();
                }
            }
        }).assertResult(1);
    }

    @Test
    public void conditionalSlowPathSkipCancel() {
        Flowable.fromArray(new Integer[] { 1, 2, 3, 4, 5 }).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v < 2;
            }
        }).subscribeWith(new TestSubscriber<Integer>(5L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    cancel();
                    onComplete();
                }
            }
        }).assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableFromArrayTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.payloads.simple.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.payloads.backpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalBackpressure() throws java.lang.Throwable {
            this.payloads.conditionalBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.payloads.just.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just10Arguments() throws java.lang.Throwable {
            this.payloads.just10Arguments.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalOneIsNull() throws java.lang.Throwable {
            this.payloads.conditionalOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalOneIsNullSlowPath() throws java.lang.Throwable {
            this.payloads.conditionalOneIsNullSlowPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalOneByOne() throws java.lang.Throwable {
            this.payloads.conditionalOneByOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFiltered() throws java.lang.Throwable {
            this.payloads.conditionalFiltered.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalSlowPathCancel() throws java.lang.Throwable {
            this.payloads.conditionalSlowPathCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalSlowPathSkipCancel() throws java.lang.Throwable {
            this.payloads.conditionalSlowPathSkipCancel.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromArrayTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromArrayTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromArrayTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromArrayTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableFromArrayTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromArrayTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableFromArrayTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableFromArrayTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement simple;

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement conditionalBackpressure;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement just;

            public org.junit.runners.model.Statement just10Arguments;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement conditionalOneIsNull;

            public org.junit.runners.model.Statement conditionalOneIsNullSlowPath;

            public org.junit.runners.model.Statement conditionalOneByOne;

            public org.junit.runners.model.Statement conditionalFiltered;

            public org.junit.runners.model.Statement conditionalSlowPathCancel;

            public org.junit.runners.model.Statement conditionalSlowPathSkipCancel;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.simple = _ClassStatement.forPayload(FlowableFromArrayTest::simple, "simple", this);
            this.payloads.backpressure = _ClassStatement.forPayload(FlowableFromArrayTest::backpressure, "backpressure", this);
            this.payloads.conditionalBackpressure = _ClassStatement.forPayload(FlowableFromArrayTest::conditionalBackpressure, "conditionalBackpressure", this);
            this.payloads.empty = _ClassStatement.forPayload(FlowableFromArrayTest::empty, "empty", this);
            this.payloads.just = _ClassStatement.forPayload(FlowableFromArrayTest::just, "just", this);
            this.payloads.just10Arguments = _ClassStatement.forPayload(FlowableFromArrayTest::just10Arguments, "just10Arguments", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableFromArrayTest::badRequest, "badRequest", this);
            this.payloads.conditionalOneIsNull = _ClassStatement.forPayload(FlowableFromArrayTest::conditionalOneIsNull, "conditionalOneIsNull", this);
            this.payloads.conditionalOneIsNullSlowPath = _ClassStatement.forPayload(FlowableFromArrayTest::conditionalOneIsNullSlowPath, "conditionalOneIsNullSlowPath", this);
            this.payloads.conditionalOneByOne = _ClassStatement.forPayload(FlowableFromArrayTest::conditionalOneByOne, "conditionalOneByOne", this);
            this.payloads.conditionalFiltered = _ClassStatement.forPayload(FlowableFromArrayTest::conditionalFiltered, "conditionalFiltered", this);
            this.payloads.conditionalSlowPathCancel = _ClassStatement.forPayload(FlowableFromArrayTest::conditionalSlowPathCancel, "conditionalSlowPathCancel", this);
            this.payloads.conditionalSlowPathSkipCancel = _ClassStatement.forPayload(FlowableFromArrayTest::conditionalSlowPathSkipCancel, "conditionalSlowPathSkipCancel", this);
        }
    }
}
