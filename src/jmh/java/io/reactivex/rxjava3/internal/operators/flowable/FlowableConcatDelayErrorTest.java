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
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestSubscriberEx;

public class FlowableConcatDelayErrorTest extends RxJavaTest {

    @Test
    public void mainCompletes() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestSubscriber<Integer> ts = TestSubscriber.create();
        source.concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return Flowable.range(v, 2);
            }
        }).subscribe(ts);
        source.onNext(1);
        source.onNext(2);
        source.onComplete();
        ts.assertValues(1, 2, 2, 3);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void mainErrors() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestSubscriber<Integer> ts = TestSubscriber.create();
        source.concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return Flowable.range(v, 2);
            }
        }).subscribe(ts);
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        ts.assertValues(1, 2, 2, 3);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void innerErrors() {
        final Flowable<Integer> inner = Flowable.range(1, 2).concatWith(Flowable.<Integer>error(new TestException()));
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(1, 3).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return inner;
            }
        }).subscribe(ts);
        ts.assertValues(1, 2, 1, 2, 1, 2);
        ts.assertError(CompositeException.class);
        ts.assertNotComplete();
    }

    @Test
    public void singleInnerErrors() {
        final Flowable<Integer> inner = Flowable.range(1, 2).concatWith(Flowable.<Integer>error(new TestException()));
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(1).hide().concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return inner;
            }
        }).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void innerNull() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(1).hide().concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return null;
            }
        }).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(NullPointerException.class);
        ts.assertNotComplete();
    }

    @Test
    public void innerThrows() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.just(1).hide().concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                throw new TestException();
            }
        }).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void innerWithEmpty() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(1, 3).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return v == 2 ? Flowable.<Integer>empty() : Flowable.range(1, 2);
            }
        }).subscribe(ts);
        ts.assertValues(1, 2, 1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void innerWithScalar() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(1, 3).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return v == 2 ? Flowable.just(3) : Flowable.range(1, 2);
            }
        }).subscribe(ts);
        ts.assertValues(1, 2, 3, 1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        Flowable.range(1, 3).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return Flowable.range(v, 2);
            }
        }).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(1);
        ts.assertValues(1);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(3);
        ts.assertValues(1, 2, 2, 3);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(2);
        ts.assertValues(1, 2, 2, 3, 3, 4);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    static <T> Flowable<T> withError(Flowable<T> source) {
        return source.concatWith(Flowable.<T>error(new TestException()));
    }

    @Test
    public void concatDelayErrorFlowable() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.concatDelayError(Flowable.just(Flowable.just(1), Flowable.just(2))).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void concatDelayErrorFlowableError() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Flowable.concatDelayError(withError(Flowable.just(withError(Flowable.just(1)), withError(Flowable.just(2))))).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertError(CompositeException.class);
        ts.assertNotComplete();
        CompositeException ce = (CompositeException) ts.errors().get(0);
        List<Throwable> cex = ce.getExceptions();
        assertEquals(3, cex.size());
        assertTrue(cex.get(0).toString(), cex.get(0) instanceof TestException);
        assertTrue(cex.get(1).toString(), cex.get(1) instanceof TestException);
        assertTrue(cex.get(2).toString(), cex.get(2) instanceof TestException);
    }

    @Test
    public void concatDelayErrorIterable() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.concatDelayError(Arrays.asList(Flowable.just(1), Flowable.just(2))).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void concatDelayErrorIterableError() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Flowable.concatDelayError(Arrays.asList(withError(Flowable.just(1)), withError(Flowable.just(2)))).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertError(CompositeException.class);
        ts.assertNotComplete();
        assertEquals(2, ((CompositeException) ts.errors().get(0)).getExceptions().size());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableConcatDelayErrorTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompletes() throws java.lang.Throwable {
            this.payloads.mainCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrors() throws java.lang.Throwable {
            this.payloads.mainErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrors() throws java.lang.Throwable {
            this.payloads.innerErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleInnerErrors() throws java.lang.Throwable {
            this.payloads.singleInnerErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerNull() throws java.lang.Throwable {
            this.payloads.innerNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerThrows() throws java.lang.Throwable {
            this.payloads.innerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerWithEmpty() throws java.lang.Throwable {
            this.payloads.innerWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerWithScalar() throws java.lang.Throwable {
            this.payloads.innerWithScalar.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.payloads.backpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatDelayErrorFlowable() throws java.lang.Throwable {
            this.payloads.concatDelayErrorFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatDelayErrorFlowableError() throws java.lang.Throwable {
            this.payloads.concatDelayErrorFlowableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatDelayErrorIterable() throws java.lang.Throwable {
            this.payloads.concatDelayErrorIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatDelayErrorIterableError() throws java.lang.Throwable {
            this.payloads.concatDelayErrorIterableError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatDelayErrorTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatDelayErrorTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatDelayErrorTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatDelayErrorTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableConcatDelayErrorTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatDelayErrorTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableConcatDelayErrorTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableConcatDelayErrorTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement mainCompletes;

            public org.junit.runners.model.Statement mainErrors;

            public org.junit.runners.model.Statement innerErrors;

            public org.junit.runners.model.Statement singleInnerErrors;

            public org.junit.runners.model.Statement innerNull;

            public org.junit.runners.model.Statement innerThrows;

            public org.junit.runners.model.Statement innerWithEmpty;

            public org.junit.runners.model.Statement innerWithScalar;

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement concatDelayErrorFlowable;

            public org.junit.runners.model.Statement concatDelayErrorFlowableError;

            public org.junit.runners.model.Statement concatDelayErrorIterable;

            public org.junit.runners.model.Statement concatDelayErrorIterableError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.mainCompletes = _ClassStatement.forPayload(FlowableConcatDelayErrorTest::mainCompletes, "mainCompletes", this);
            this.payloads.mainErrors = _ClassStatement.forPayload(FlowableConcatDelayErrorTest::mainErrors, "mainErrors", this);
            this.payloads.innerErrors = _ClassStatement.forPayload(FlowableConcatDelayErrorTest::innerErrors, "innerErrors", this);
            this.payloads.singleInnerErrors = _ClassStatement.forPayload(FlowableConcatDelayErrorTest::singleInnerErrors, "singleInnerErrors", this);
            this.payloads.innerNull = _ClassStatement.forPayload(FlowableConcatDelayErrorTest::innerNull, "innerNull", this);
            this.payloads.innerThrows = _ClassStatement.forPayload(FlowableConcatDelayErrorTest::innerThrows, "innerThrows", this);
            this.payloads.innerWithEmpty = _ClassStatement.forPayload(FlowableConcatDelayErrorTest::innerWithEmpty, "innerWithEmpty", this);
            this.payloads.innerWithScalar = _ClassStatement.forPayload(FlowableConcatDelayErrorTest::innerWithScalar, "innerWithScalar", this);
            this.payloads.backpressure = _ClassStatement.forPayload(FlowableConcatDelayErrorTest::backpressure, "backpressure", this);
            this.payloads.concatDelayErrorFlowable = _ClassStatement.forPayload(FlowableConcatDelayErrorTest::concatDelayErrorFlowable, "concatDelayErrorFlowable", this);
            this.payloads.concatDelayErrorFlowableError = _ClassStatement.forPayload(FlowableConcatDelayErrorTest::concatDelayErrorFlowableError, "concatDelayErrorFlowableError", this);
            this.payloads.concatDelayErrorIterable = _ClassStatement.forPayload(FlowableConcatDelayErrorTest::concatDelayErrorIterable, "concatDelayErrorIterable", this);
            this.payloads.concatDelayErrorIterableError = _ClassStatement.forPayload(FlowableConcatDelayErrorTest::concatDelayErrorIterableError, "concatDelayErrorIterableError", this);
        }
    }
}
