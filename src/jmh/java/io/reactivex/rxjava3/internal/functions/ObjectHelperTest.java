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
package io.reactivex.rxjava3.internal.functions;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObjectHelperTest extends RxJavaTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(ObjectHelper.class);
    }

    @Test
    public void verifyPositiveInt() throws Exception {
        assertEquals(1, ObjectHelper.verifyPositive(1, "param"));
    }

    @Test
    public void verifyPositiveLong() throws Exception {
        assertEquals(1L, ObjectHelper.verifyPositive(1L, "param"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyPositiveIntFail() throws Exception {
        assertEquals(-1, ObjectHelper.verifyPositive(-1, "param"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyPositiveLongFail() throws Exception {
        assertEquals(-1L, ObjectHelper.verifyPositive(-1L, "param"));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObjectHelperTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_utilityClass() throws java.lang.Throwable {
            this.payloads.utilityClass.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_verifyPositiveInt() throws java.lang.Throwable {
            this.payloads.verifyPositiveInt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_verifyPositiveLong() throws java.lang.Throwable {
            this.payloads.verifyPositiveLong.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_verifyPositiveIntFail() throws java.lang.Throwable {
            this.payloads.verifyPositiveIntFail.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_verifyPositiveLongFail() throws java.lang.Throwable {
            this.payloads.verifyPositiveLongFail.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObjectHelperTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObjectHelperTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObjectHelperTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObjectHelperTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObjectHelperTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObjectHelperTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObjectHelperTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObjectHelperTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement utilityClass;

            public org.junit.runners.model.Statement verifyPositiveInt;

            public org.junit.runners.model.Statement verifyPositiveLong;

            public org.junit.runners.model.Statement verifyPositiveIntFail;

            public org.junit.runners.model.Statement verifyPositiveLongFail;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.utilityClass = _ClassStatement.forPayload(ObjectHelperTest::utilityClass, "utilityClass", this);
            this.payloads.verifyPositiveInt = _ClassStatement.forPayload(ObjectHelperTest::verifyPositiveInt, "verifyPositiveInt", this);
            this.payloads.verifyPositiveLong = _ClassStatement.forPayload(ObjectHelperTest::verifyPositiveLong, "verifyPositiveLong", this);
            this.payloads.verifyPositiveIntFail = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObjectHelperTest::verifyPositiveIntFail, java.lang.IllegalArgumentException.class), "verifyPositiveIntFail", this);
            this.payloads.verifyPositiveLongFail = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObjectHelperTest::verifyPositiveLongFail, java.lang.IllegalArgumentException.class), "verifyPositiveLongFail", this);
        }
    }
}
