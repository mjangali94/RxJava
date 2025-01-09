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
package io.reactivex.rxjava3.internal.jdk8;

import java.util.stream.Stream;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.parallel.ParallelFlowableTest;

public class ParallelFlatMapStreamTest extends RxJavaTest {

    @Test
    public void subscriberCount() {
        ParallelFlowableTest.checkSubscriberCount(Flowable.range(1, 5).parallel().flatMapStream(v -> Stream.of(1, 2, 3)));
    }

    @Test
    public void normal() {
        for (int i = 1; i < 32; i++) {
            Flowable.range(1, 1000).parallel(i).flatMapStream(v -> Stream.of(v, v + 1)).sequential().test().withTag("Parallelism: " + i).assertValueCount(2000).assertNoErrors().assertComplete();
        }
    }

    @Test
    public void none() {
        for (int i = 1; i < 32; i++) {
            Flowable.range(1, 1000).parallel(i).flatMapStream(v -> Stream.of()).sequential().test().withTag("Parallelism: " + i).assertResult();
        }
    }

    @Test
    public void mixed() {
        for (int i = 1; i < 32; i++) {
            Flowable.range(1, 1000).parallel(i).flatMapStream(v -> v % 2 == 0 ? Stream.of(v) : Stream.of()).sequential().test().withTag("Parallelism: " + i).assertValueCount(500).assertNoErrors().assertComplete();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ParallelFlatMapStreamTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriberCount() throws java.lang.Throwable {
            this.payloads.subscriberCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_none() throws java.lang.Throwable {
            this.payloads.none.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixed() throws java.lang.Throwable {
            this.payloads.mixed.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelFlatMapStreamTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelFlatMapStreamTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelFlatMapStreamTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelFlatMapStreamTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ParallelFlatMapStreamTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelFlatMapStreamTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ParallelFlatMapStreamTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ParallelFlatMapStreamTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement subscriberCount;

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement none;

            public org.junit.runners.model.Statement mixed;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.subscriberCount = _ClassStatement.forPayload(ParallelFlatMapStreamTest::subscriberCount, "subscriberCount", this);
            this.payloads.normal = _ClassStatement.forPayload(ParallelFlatMapStreamTest::normal, "normal", this);
            this.payloads.none = _ClassStatement.forPayload(ParallelFlatMapStreamTest::none, "none", this);
            this.payloads.mixed = _ClassStatement.forPayload(ParallelFlatMapStreamTest::mixed, "mixed", this);
        }
    }
}
