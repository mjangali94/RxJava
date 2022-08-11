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

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.parallel.ParallelInvalid;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class ParallelCollectorTest extends RxJavaTest {

    static Set<Integer> set(int count) {
        return IntStream.rangeClosed(1, count).boxed().collect(Collectors.toSet());
    }

    @Test
    public void basic() {
        TestSubscriberEx<List<Integer>> ts = Flowable.range(1, 5).parallel().collect(Collectors.toList()).subscribeWith(new TestSubscriberEx<>());
        ts.assertValueCount(1).assertNoErrors().assertComplete();
        assertEquals(5, ts.values().get(0).size());
        assertTrue(ts.values().get(0).containsAll(set(5)));
    }

    @Test
    public void empty() {
        Flowable.empty().parallel().collect(Collectors.toList()).test().assertResult(Collections.emptyList());
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).parallel().collect(Collectors.toList()).test().assertFailure(TestException.class);
    }

    @Test
    public void collectorSupplierCrash() {
        Flowable.range(1, 5).parallel().collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                throw new TestException();
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> a;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void collectorAccumulatorCrash() {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        source.parallel().collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                return () -> 1;
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                    throw new TestException();
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> a;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).test().assertFailure(TestException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    @SuppressUndeliverable
    public void collectorCombinerCrash() {
        Flowable.range(1, 5).parallel().collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                return () -> 1;
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> {
                    throw new TestException();
                };
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> a;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void collectorFinisherCrash() {
        Flowable.range(1, 5).parallel().collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                return () -> 1;
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> {
                    throw new TestException();
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void async() {
        for (int i = 1; i < 32; i++) {
            TestSubscriber<List<Integer>> ts = Flowable.range(1, 1000).parallel(i).runOn(Schedulers.computation()).collect(Collectors.toList()).test().withTag("Parallelism: " + i).awaitDone(5, TimeUnit.SECONDS).assertValueCount(1).assertNoErrors().assertComplete();
            assertEquals(1000, ts.values().get(0).size());
            assertTrue(ts.values().get(0).containsAll(set(1000)));
        }
    }

    @Test
    public void asyncHidden() {
        for (int i = 1; i < 32; i++) {
            TestSubscriber<List<Integer>> ts = Flowable.range(1, 1000).hide().parallel(i).runOn(Schedulers.computation()).collect(Collectors.toList()).test().withTag("Parallelism: " + i).awaitDone(5, TimeUnit.SECONDS).assertValueCount(1).assertNoErrors().assertComplete();
            assertEquals(1000, ts.values().get(0).size());
            assertTrue(ts.values().get(0).containsAll(set(1000)));
        }
    }

    @Test
    public void doubleError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().collect(Collectors.toList()).test().assertFailure(TestException.class);
            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void asyncSum() {
        long n = 1_000;
        for (int i = 1; i < 32; i++) {
            Flowable.rangeLong(1, n).parallel(i).runOn(Schedulers.computation()).collect(Collectors.summingLong(v -> v)).test().withTag("Parallelism: " + i).awaitDone(5, TimeUnit.SECONDS).assertResult(n * (n + 1) / 2);
        }
    }

    @Test
    public void asyncSumLong() {
        long n = 1_000_000;
        Flowable.rangeLong(1, n).parallel().runOn(Schedulers.computation()).collect(Collectors.summingLong(v -> v)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(n * (n + 1) / 2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ParallelCollectorTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basic() throws java.lang.Throwable {
            this.payloads.basic.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorSupplierCrash() throws java.lang.Throwable {
            this.payloads.collectorSupplierCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorAccumulatorCrash() throws java.lang.Throwable {
            this.payloads.collectorAccumulatorCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorCombinerCrash() throws java.lang.Throwable {
            this.payloads.collectorCombinerCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFinisherCrash() throws java.lang.Throwable {
            this.payloads.collectorFinisherCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_async() throws java.lang.Throwable {
            this.payloads.async.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncHidden() throws java.lang.Throwable {
            this.payloads.asyncHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleError() throws java.lang.Throwable {
            this.payloads.doubleError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncSum() throws java.lang.Throwable {
            this.payloads.asyncSum.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncSumLong() throws java.lang.Throwable {
            this.payloads.asyncSumLong.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelCollectorTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelCollectorTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelCollectorTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelCollectorTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ParallelCollectorTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelCollectorTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ParallelCollectorTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ParallelCollectorTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement basic;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement collectorSupplierCrash;

            public org.junit.runners.model.Statement collectorAccumulatorCrash;

            public org.junit.runners.model.Statement collectorCombinerCrash;

            public org.junit.runners.model.Statement collectorFinisherCrash;

            public org.junit.runners.model.Statement async;

            public org.junit.runners.model.Statement asyncHidden;

            public org.junit.runners.model.Statement doubleError;

            public org.junit.runners.model.Statement asyncSum;

            public org.junit.runners.model.Statement asyncSumLong;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.basic = _ClassStatement.forPayload(ParallelCollectorTest::basic, "basic", this);
            this.payloads.empty = _ClassStatement.forPayload(ParallelCollectorTest::empty, "empty", this);
            this.payloads.error = _ClassStatement.forPayload(ParallelCollectorTest::error, "error", this);
            this.payloads.collectorSupplierCrash = _ClassStatement.forPayload(ParallelCollectorTest::collectorSupplierCrash, "collectorSupplierCrash", this);
            this.payloads.collectorAccumulatorCrash = _ClassStatement.forPayload(ParallelCollectorTest::collectorAccumulatorCrash, "collectorAccumulatorCrash", this);
            this.payloads.collectorCombinerCrash = _ClassStatement.forPayload(ParallelCollectorTest::collectorCombinerCrash, "collectorCombinerCrash", this);
            this.payloads.collectorFinisherCrash = _ClassStatement.forPayload(ParallelCollectorTest::collectorFinisherCrash, "collectorFinisherCrash", this);
            this.payloads.async = _ClassStatement.forPayload(ParallelCollectorTest::async, "async", this);
            this.payloads.asyncHidden = _ClassStatement.forPayload(ParallelCollectorTest::asyncHidden, "asyncHidden", this);
            this.payloads.doubleError = _ClassStatement.forPayload(ParallelCollectorTest::doubleError, "doubleError", this);
            this.payloads.asyncSum = _ClassStatement.forPayload(ParallelCollectorTest::asyncSum, "asyncSum", this);
            this.payloads.asyncSumLong = _ClassStatement.forPayload(ParallelCollectorTest::asyncSumLong, "asyncSumLong", this);
        }
    }
}
