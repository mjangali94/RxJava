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
package io.reactivex.rxjava3.internal.operators.single;

import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.testsupport.*;

public class SingleDoOnTerminateTest extends RxJavaTest {

    @Test
    public void doOnTerminateSuccess() {
        final AtomicBoolean atomicBoolean = new AtomicBoolean();
        Single.just(1).doOnTerminate(new Action() {

            @Override
            public void run() throws Exception {
                atomicBoolean.set(true);
            }
        }).test().assertResult(1);
        assertTrue(atomicBoolean.get());
    }

    @Test
    public void doOnTerminateError() {
        final AtomicBoolean atomicBoolean = new AtomicBoolean();
        Single.error(new TestException()).doOnTerminate(new Action() {

            @Override
            public void run() {
                atomicBoolean.set(true);
            }
        }).test().assertFailure(TestException.class);
        assertTrue(atomicBoolean.get());
    }

    @Test
    public void doOnTerminateSuccessCrash() {
        Single.just(1).doOnTerminate(new Action() {

            @Override
            public void run() throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void doOnTerminateErrorCrash() {
        TestObserverEx<Object> to = Single.error(new TestException("Outer")).doOnTerminate(new Action() {

            @Override
            public void run() {
                throw new TestException("Inner");
            }
        }).to(TestHelper.testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(errors, 0, TestException.class, "Outer");
        TestHelper.assertError(errors, 1, TestException.class, "Inner");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public SingleDoOnTerminateTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateSuccess() throws java.lang.Throwable {
            this.payloads.doOnTerminateSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateError() throws java.lang.Throwable {
            this.payloads.doOnTerminateError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateSuccessCrash() throws java.lang.Throwable {
            this.payloads.doOnTerminateSuccessCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateErrorCrash() throws java.lang.Throwable {
            this.payloads.doOnTerminateErrorCrash.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoOnTerminateTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoOnTerminateTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoOnTerminateTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoOnTerminateTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleDoOnTerminateTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoOnTerminateTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleDoOnTerminateTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleDoOnTerminateTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement doOnTerminateSuccess;

            public org.junit.runners.model.Statement doOnTerminateError;

            public org.junit.runners.model.Statement doOnTerminateSuccessCrash;

            public org.junit.runners.model.Statement doOnTerminateErrorCrash;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.doOnTerminateSuccess = _ClassStatement.forPayload(SingleDoOnTerminateTest::doOnTerminateSuccess, "doOnTerminateSuccess", this);
            this.payloads.doOnTerminateError = _ClassStatement.forPayload(SingleDoOnTerminateTest::doOnTerminateError, "doOnTerminateError", this);
            this.payloads.doOnTerminateSuccessCrash = _ClassStatement.forPayload(SingleDoOnTerminateTest::doOnTerminateSuccessCrash, "doOnTerminateSuccessCrash", this);
            this.payloads.doOnTerminateErrorCrash = _ClassStatement.forPayload(SingleDoOnTerminateTest::doOnTerminateErrorCrash, "doOnTerminateErrorCrash", this);
        }
    }
}
