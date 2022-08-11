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

import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleEqualsTest extends RxJavaTest {

    @Test
    public void bothSucceedEqual() {
        Single.sequenceEqual(Single.just(1), Single.just(1)).test().assertResult(true);
    }

    @Test
    public void bothSucceedNotEqual() {
        Single.sequenceEqual(Single.just(1), Single.just(2)).test().assertResult(false);
    }

    @Test
    public void firstSucceedOtherError() {
        Single.sequenceEqual(Single.just(1), Single.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void firstErrorOtherSucceed() {
        Single.sequenceEqual(Single.error(new TestException()), Single.just(1)).test().assertFailure(TestException.class);
    }

    @Test
    public void bothError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Single.sequenceEqual(Single.error(new TestException("One")), Single.error(new TestException("Two"))).to(TestHelper.<Boolean>testConsumer()).assertFailureAndMessage(TestException.class, "One");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Two");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleEqualsTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothSucceedEqual() throws java.lang.Throwable {
            this.payloads.bothSucceedEqual.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothSucceedNotEqual() throws java.lang.Throwable {
            this.payloads.bothSucceedNotEqual.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstSucceedOtherError() throws java.lang.Throwable {
            this.payloads.firstSucceedOtherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstErrorOtherSucceed() throws java.lang.Throwable {
            this.payloads.firstErrorOtherSucceed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothError() throws java.lang.Throwable {
            this.payloads.bothError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleEqualsTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleEqualsTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleEqualsTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleEqualsTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleEqualsTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleEqualsTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleEqualsTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleEqualsTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement bothSucceedEqual;

            public org.junit.runners.model.Statement bothSucceedNotEqual;

            public org.junit.runners.model.Statement firstSucceedOtherError;

            public org.junit.runners.model.Statement firstErrorOtherSucceed;

            public org.junit.runners.model.Statement bothError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.bothSucceedEqual = _ClassStatement.forPayload(SingleEqualsTest::bothSucceedEqual, "bothSucceedEqual", this);
            this.payloads.bothSucceedNotEqual = _ClassStatement.forPayload(SingleEqualsTest::bothSucceedNotEqual, "bothSucceedNotEqual", this);
            this.payloads.firstSucceedOtherError = _ClassStatement.forPayload(SingleEqualsTest::firstSucceedOtherError, "firstSucceedOtherError", this);
            this.payloads.firstErrorOtherSucceed = _ClassStatement.forPayload(SingleEqualsTest::firstErrorOtherSucceed, "firstErrorOtherSucceed", this);
            this.payloads.bothError = _ClassStatement.forPayload(SingleEqualsTest::bothError, "bothError", this);
        }
    }
}
