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
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SchedulerPoolFactoryTest extends RxJavaTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(SchedulerPoolFactory.class);
    }

    @Test
    public void boolPropertiesDisabledReturnsDefaultDisabled() throws Throwable {
        assertTrue(SchedulerPoolFactory.getBooleanProperty(false, "key", false, true, failingPropertiesAccessor));
        assertFalse(SchedulerPoolFactory.getBooleanProperty(false, "key", true, false, failingPropertiesAccessor));
    }

    @Test
    public void boolPropertiesEnabledMissingReturnsDefaultMissing() throws Throwable {
        assertTrue(SchedulerPoolFactory.getBooleanProperty(true, "key", true, false, missingPropertiesAccessor));
        assertFalse(SchedulerPoolFactory.getBooleanProperty(true, "key", false, true, missingPropertiesAccessor));
    }

    @Test
    public void boolPropertiesFailureReturnsDefaultMissing() throws Throwable {
        assertTrue(SchedulerPoolFactory.getBooleanProperty(true, "key", true, false, failingPropertiesAccessor));
        assertFalse(SchedulerPoolFactory.getBooleanProperty(true, "key", false, true, failingPropertiesAccessor));
    }

    @Test
    public void boolPropertiesReturnsValue() throws Throwable {
        assertTrue(SchedulerPoolFactory.getBooleanProperty(true, "true", true, false, Functions.<String>identity()));
        assertFalse(SchedulerPoolFactory.getBooleanProperty(true, "false", false, true, Functions.<String>identity()));
    }

    static final Function<String, String> failingPropertiesAccessor = new Function<String, String>() {

        @Override
        public String apply(String v) throws Throwable {
            throw new SecurityException();
        }
    };

    static final Function<String, String> missingPropertiesAccessor = new Function<String, String>() {

        @Override
        public String apply(String v) throws Throwable {
            return null;
        }
    };

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SchedulerPoolFactoryTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_utilityClass() throws java.lang.Throwable {
            this.payloads.utilityClass.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boolPropertiesDisabledReturnsDefaultDisabled() throws java.lang.Throwable {
            this.payloads.boolPropertiesDisabledReturnsDefaultDisabled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boolPropertiesEnabledMissingReturnsDefaultMissing() throws java.lang.Throwable {
            this.payloads.boolPropertiesEnabledMissingReturnsDefaultMissing.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boolPropertiesFailureReturnsDefaultMissing() throws java.lang.Throwable {
            this.payloads.boolPropertiesFailureReturnsDefaultMissing.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boolPropertiesReturnsValue() throws java.lang.Throwable {
            this.payloads.boolPropertiesReturnsValue.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SchedulerPoolFactoryTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SchedulerPoolFactoryTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SchedulerPoolFactoryTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SchedulerPoolFactoryTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SchedulerPoolFactoryTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SchedulerPoolFactoryTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SchedulerPoolFactoryTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SchedulerPoolFactoryTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement utilityClass;

            public org.junit.runners.model.Statement boolPropertiesDisabledReturnsDefaultDisabled;

            public org.junit.runners.model.Statement boolPropertiesEnabledMissingReturnsDefaultMissing;

            public org.junit.runners.model.Statement boolPropertiesFailureReturnsDefaultMissing;

            public org.junit.runners.model.Statement boolPropertiesReturnsValue;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.utilityClass = _ClassStatement.forPayload(SchedulerPoolFactoryTest::utilityClass, "utilityClass", this);
            this.payloads.boolPropertiesDisabledReturnsDefaultDisabled = _ClassStatement.forPayload(SchedulerPoolFactoryTest::boolPropertiesDisabledReturnsDefaultDisabled, "boolPropertiesDisabledReturnsDefaultDisabled", this);
            this.payloads.boolPropertiesEnabledMissingReturnsDefaultMissing = _ClassStatement.forPayload(SchedulerPoolFactoryTest::boolPropertiesEnabledMissingReturnsDefaultMissing, "boolPropertiesEnabledMissingReturnsDefaultMissing", this);
            this.payloads.boolPropertiesFailureReturnsDefaultMissing = _ClassStatement.forPayload(SchedulerPoolFactoryTest::boolPropertiesFailureReturnsDefaultMissing, "boolPropertiesFailureReturnsDefaultMissing", this);
            this.payloads.boolPropertiesReturnsValue = _ClassStatement.forPayload(SchedulerPoolFactoryTest::boolPropertiesReturnsValue, "boolPropertiesReturnsValue", this);
        }
    }
}
