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
package io.reactivex.rxjava3.internal.operators.observable;

import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

public class ObservableToXTest extends RxJavaTest {

    @Test
    public void toFlowableBuffer() {
        Observable.range(1, 5).toFlowable(BackpressureStrategy.BUFFER).test(2L).assertValues(1, 2).assertNoErrors().assertNotComplete();
    }

    @Test
    public void toFlowableDrop() {
        Observable.range(1, 5).toFlowable(BackpressureStrategy.DROP).test(1).assertResult(1);
    }

    @Test
    public void toFlowableLatest() {
        TestSubscriber<Integer> ts = Observable.range(1, 5).toFlowable(BackpressureStrategy.LATEST).test(0);
        ts.request(1);
        ts.assertResult(5);
    }

    @Test
    public void toFlowableError1() {
        Observable.range(1, 5).toFlowable(BackpressureStrategy.ERROR).test(1).assertFailure(MissingBackpressureException.class, 1);
    }

    @Test
    public void toFlowableError2() {
        Observable.range(1, 5).toFlowable(BackpressureStrategy.ERROR).test(5).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void toFlowableMissing() {
        TestSubscriber<Integer> ts = Observable.range(1, 5).toFlowable(BackpressureStrategy.MISSING).test(0);
        ts.request(2);
        ts.assertResult(1, 2, 3, 4, 5);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableToXTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableBuffer() throws java.lang.Throwable {
            this.payloads.toFlowableBuffer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableDrop() throws java.lang.Throwable {
            this.payloads.toFlowableDrop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableLatest() throws java.lang.Throwable {
            this.payloads.toFlowableLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableError1() throws java.lang.Throwable {
            this.payloads.toFlowableError1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableError2() throws java.lang.Throwable {
            this.payloads.toFlowableError2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableMissing() throws java.lang.Throwable {
            this.payloads.toFlowableMissing.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToXTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToXTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToXTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToXTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableToXTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToXTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableToXTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableToXTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement toFlowableBuffer;

            public org.junit.runners.model.Statement toFlowableDrop;

            public org.junit.runners.model.Statement toFlowableLatest;

            public org.junit.runners.model.Statement toFlowableError1;

            public org.junit.runners.model.Statement toFlowableError2;

            public org.junit.runners.model.Statement toFlowableMissing;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.toFlowableBuffer = _ClassStatement.forPayload(ObservableToXTest::toFlowableBuffer, "toFlowableBuffer", this);
            this.payloads.toFlowableDrop = _ClassStatement.forPayload(ObservableToXTest::toFlowableDrop, "toFlowableDrop", this);
            this.payloads.toFlowableLatest = _ClassStatement.forPayload(ObservableToXTest::toFlowableLatest, "toFlowableLatest", this);
            this.payloads.toFlowableError1 = _ClassStatement.forPayload(ObservableToXTest::toFlowableError1, "toFlowableError1", this);
            this.payloads.toFlowableError2 = _ClassStatement.forPayload(ObservableToXTest::toFlowableError2, "toFlowableError2", this);
            this.payloads.toFlowableMissing = _ClassStatement.forPayload(ObservableToXTest::toFlowableMissing, "toFlowableMissing", this);
        }
    }
}
