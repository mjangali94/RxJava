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

import static org.junit.Assert.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjava3.testsupport.TestObserverEx;

public class ObservableFromSingleTest extends RxJavaTest {

    @Test
    public void success() {
        Observable.fromSingle(Single.just(1).hide()).test().assertResult(1);
    }

    @Test
    public void error() {
        Observable.fromSingle(Single.error(new TestException()).hide()).test().assertFailure(TestException.class);
    }

    @Test
    public void cancelComposes() {
        SingleSubject<Integer> ms = SingleSubject.create();
        TestObserver<Integer> to = Observable.fromSingle(ms).test();
        to.assertEmpty();
        assertTrue(ms.hasObservers());
        to.dispose();
        assertFalse(ms.hasObservers());
    }

    @Test
    public void asyncFusion() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.ASYNC);
        Observable.fromSingle(Single.just(1)).subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1);
    }

    @Test
    public void syncFusionRejected() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.SYNC);
        Observable.fromSingle(Single.just(1)).subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableFromSingleTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_success() throws java.lang.Throwable {
            this.payloads.success.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelComposes() throws java.lang.Throwable {
            this.payloads.cancelComposes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusion() throws java.lang.Throwable {
            this.payloads.asyncFusion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusionRejected() throws java.lang.Throwable {
            this.payloads.syncFusionRejected.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromSingleTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromSingleTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromSingleTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromSingleTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableFromSingleTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromSingleTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableFromSingleTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableFromSingleTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement success;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement cancelComposes;

            public org.junit.runners.model.Statement asyncFusion;

            public org.junit.runners.model.Statement syncFusionRejected;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.success = _ClassStatement.forPayload(ObservableFromSingleTest::success, "success", this);
            this.payloads.error = _ClassStatement.forPayload(ObservableFromSingleTest::error, "error", this);
            this.payloads.cancelComposes = _ClassStatement.forPayload(ObservableFromSingleTest::cancelComposes, "cancelComposes", this);
            this.payloads.asyncFusion = _ClassStatement.forPayload(ObservableFromSingleTest::asyncFusion, "asyncFusion", this);
            this.payloads.syncFusionRejected = _ClassStatement.forPayload(ObservableFromSingleTest::syncFusionRejected, "syncFusionRejected", this);
        }
    }
}
