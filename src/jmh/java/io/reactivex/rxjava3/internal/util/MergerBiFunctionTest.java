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
package io.reactivex.rxjava3.internal.util;

import static org.junit.Assert.assertEquals;
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;

public class MergerBiFunctionTest extends RxJavaTest {

    @Test
    public void firstEmpty() throws Exception {
        MergerBiFunction<Integer> merger = new MergerBiFunction<>(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        List<Integer> list = merger.apply(Collections.<Integer>emptyList(), Arrays.asList(3, 5));
        assertEquals(Arrays.asList(3, 5), list);
    }

    @Test
    public void bothEmpty() throws Exception {
        MergerBiFunction<Integer> merger = new MergerBiFunction<>(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        List<Integer> list = merger.apply(Collections.<Integer>emptyList(), Collections.<Integer>emptyList());
        assertEquals(Collections.<Integer>emptyList(), list);
    }

    @Test
    public void secondEmpty() throws Exception {
        MergerBiFunction<Integer> merger = new MergerBiFunction<>(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        List<Integer> list = merger.apply(Arrays.asList(2, 4), Collections.<Integer>emptyList());
        assertEquals(Arrays.asList(2, 4), list);
    }

    @Test
    public void sameSize() throws Exception {
        MergerBiFunction<Integer> merger = new MergerBiFunction<>(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        List<Integer> list = merger.apply(Arrays.asList(2, 4), Arrays.asList(3, 5));
        assertEquals(Arrays.asList(2, 3, 4, 5), list);
    }

    @Test
    public void sameSizeReverse() throws Exception {
        MergerBiFunction<Integer> merger = new MergerBiFunction<>(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        List<Integer> list = merger.apply(Arrays.asList(3, 5), Arrays.asList(2, 4));
        assertEquals(Arrays.asList(2, 3, 4, 5), list);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MergerBiFunctionTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstEmpty() throws java.lang.Throwable {
            this.payloads.firstEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothEmpty() throws java.lang.Throwable {
            this.payloads.bothEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_secondEmpty() throws java.lang.Throwable {
            this.payloads.secondEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sameSize() throws java.lang.Throwable {
            this.payloads.sameSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sameSizeReverse() throws java.lang.Throwable {
            this.payloads.sameSizeReverse.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MergerBiFunctionTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MergerBiFunctionTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MergerBiFunctionTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MergerBiFunctionTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MergerBiFunctionTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MergerBiFunctionTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MergerBiFunctionTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MergerBiFunctionTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement firstEmpty;

            public org.junit.runners.model.Statement bothEmpty;

            public org.junit.runners.model.Statement secondEmpty;

            public org.junit.runners.model.Statement sameSize;

            public org.junit.runners.model.Statement sameSizeReverse;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.firstEmpty = _ClassStatement.forPayload(MergerBiFunctionTest::firstEmpty, "firstEmpty", this);
            this.payloads.bothEmpty = _ClassStatement.forPayload(MergerBiFunctionTest::bothEmpty, "bothEmpty", this);
            this.payloads.secondEmpty = _ClassStatement.forPayload(MergerBiFunctionTest::secondEmpty, "secondEmpty", this);
            this.payloads.sameSize = _ClassStatement.forPayload(MergerBiFunctionTest::sameSize, "sameSize", this);
            this.payloads.sameSizeReverse = _ClassStatement.forPayload(MergerBiFunctionTest::sameSizeReverse, "sameSizeReverse", this);
        }
    }
}
