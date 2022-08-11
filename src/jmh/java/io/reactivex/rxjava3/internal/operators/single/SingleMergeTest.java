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
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleMergeTest extends RxJavaTest {

    @Test
    public void mergeSingleSingle() {
        Single.merge(Single.just(Single.just(1))).test().assertResult(1);
    }

    @Test
    public void merge2() {
        Single.merge(Single.just(1), Single.just(2)).test().assertResult(1, 2);
    }

    @Test
    public void merge3() {
        Single.merge(Single.just(1), Single.just(2), Single.just(3)).test().assertResult(1, 2, 3);
    }

    @Test
    public void merge4() {
        Single.merge(Single.just(1), Single.just(2), Single.just(3), Single.just(4)).test().assertResult(1, 2, 3, 4);
    }

    @Test
    public void mergeErrors() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Single<Integer> source1 = Single.error(new TestException("First"));
            Single<Integer> source2 = Single.error(new TestException("Second"));
            Single.merge(source1, source2).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void mergeDelayErrorIterable() {
        Single.mergeDelayError(Arrays.asList(Single.just(1), Single.<Integer>error(new TestException()), Single.just(2))).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void mergeDelayErrorPublisher() {
        Single.mergeDelayError(Flowable.just(Single.just(1), Single.<Integer>error(new TestException()), Single.just(2))).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void mergeDelayError2() {
        Single.mergeDelayError(Single.just(1), Single.<Integer>error(new TestException())).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void mergeDelayError2ErrorFirst() {
        Single.mergeDelayError(Single.<Integer>error(new TestException()), Single.just(1)).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void mergeDelayError3() {
        Single.mergeDelayError(Single.just(1), Single.<Integer>error(new TestException()), Single.just(2)).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void mergeDelayError4() {
        Single.mergeDelayError(Single.just(1), Single.<Integer>error(new TestException()), Single.just(2), Single.just(3)).test().assertFailure(TestException.class, 1, 2, 3);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleMergeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeSingleSingle() throws java.lang.Throwable {
            this.payloads.mergeSingleSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_merge2() throws java.lang.Throwable {
            this.payloads.merge2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_merge3() throws java.lang.Throwable {
            this.payloads.merge3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_merge4() throws java.lang.Throwable {
            this.payloads.merge4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeErrors() throws java.lang.Throwable {
            this.payloads.mergeErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterable() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorPublisher() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorPublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayError2() throws java.lang.Throwable {
            this.payloads.mergeDelayError2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayError2ErrorFirst() throws java.lang.Throwable {
            this.payloads.mergeDelayError2ErrorFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayError3() throws java.lang.Throwable {
            this.payloads.mergeDelayError3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayError4() throws java.lang.Throwable {
            this.payloads.mergeDelayError4.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleMergeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleMergeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleMergeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleMergeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleMergeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleMergeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleMergeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleMergeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement mergeSingleSingle;

            public org.junit.runners.model.Statement merge2;

            public org.junit.runners.model.Statement merge3;

            public org.junit.runners.model.Statement merge4;

            public org.junit.runners.model.Statement mergeErrors;

            public org.junit.runners.model.Statement mergeDelayErrorIterable;

            public org.junit.runners.model.Statement mergeDelayErrorPublisher;

            public org.junit.runners.model.Statement mergeDelayError2;

            public org.junit.runners.model.Statement mergeDelayError2ErrorFirst;

            public org.junit.runners.model.Statement mergeDelayError3;

            public org.junit.runners.model.Statement mergeDelayError4;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.mergeSingleSingle = _ClassStatement.forPayload(SingleMergeTest::mergeSingleSingle, "mergeSingleSingle", this);
            this.payloads.merge2 = _ClassStatement.forPayload(SingleMergeTest::merge2, "merge2", this);
            this.payloads.merge3 = _ClassStatement.forPayload(SingleMergeTest::merge3, "merge3", this);
            this.payloads.merge4 = _ClassStatement.forPayload(SingleMergeTest::merge4, "merge4", this);
            this.payloads.mergeErrors = _ClassStatement.forPayload(SingleMergeTest::mergeErrors, "mergeErrors", this);
            this.payloads.mergeDelayErrorIterable = _ClassStatement.forPayload(SingleMergeTest::mergeDelayErrorIterable, "mergeDelayErrorIterable", this);
            this.payloads.mergeDelayErrorPublisher = _ClassStatement.forPayload(SingleMergeTest::mergeDelayErrorPublisher, "mergeDelayErrorPublisher", this);
            this.payloads.mergeDelayError2 = _ClassStatement.forPayload(SingleMergeTest::mergeDelayError2, "mergeDelayError2", this);
            this.payloads.mergeDelayError2ErrorFirst = _ClassStatement.forPayload(SingleMergeTest::mergeDelayError2ErrorFirst, "mergeDelayError2ErrorFirst", this);
            this.payloads.mergeDelayError3 = _ClassStatement.forPayload(SingleMergeTest::mergeDelayError3, "mergeDelayError3", this);
            this.payloads.mergeDelayError4 = _ClassStatement.forPayload(SingleMergeTest::mergeDelayError4, "mergeDelayError4", this);
        }
    }
}
