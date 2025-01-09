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
import java.util.List;
import java.util.stream.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.processors.UnicastProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ObservableBlockingStreamTest extends RxJavaTest {

    @Test
    public void empty() {
        try (Stream<Integer> stream = Observable.<Integer>empty().blockingStream()) {
            assertEquals(0, stream.toArray().length);
        }
    }

    @Test
    public void just() {
        try (Stream<Integer> stream = Observable.just(1).blockingStream()) {
            assertArrayEquals(new Integer[] { 1 }, stream.toArray(Integer[]::new));
        }
    }

    @Test
    public void range() {
        try (Stream<Integer> stream = Observable.range(1, 5).blockingStream()) {
            assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5 }, stream.toArray(Integer[]::new));
        }
    }

    @Test
    public void rangeBackpressured() {
        try (Stream<Integer> stream = Observable.range(1, 5).blockingStream(1)) {
            assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5 }, stream.toArray(Integer[]::new));
        }
    }

    @Test
    public void rangeAsyncBackpressured() {
        try (Stream<Integer> stream = Observable.range(1, 1000).subscribeOn(Schedulers.computation()).blockingStream()) {
            List<Integer> list = stream.collect(Collectors.toList());
            assertEquals(1000, list.size());
            for (int i = 1; i <= 1000; i++) {
                assertEquals(i, list.get(i - 1).intValue());
            }
        }
    }

    @Test
    public void rangeAsyncBackpressured1() {
        try (Stream<Integer> stream = Observable.range(1, 1000).subscribeOn(Schedulers.computation()).blockingStream(1)) {
            List<Integer> list = stream.collect(Collectors.toList());
            assertEquals(1000, list.size());
            for (int i = 1; i <= 1000; i++) {
                assertEquals(i, list.get(i - 1).intValue());
            }
        }
    }

    @Test
    public void error() {
        try (Stream<Integer> stream = Observable.<Integer>error(new TestException()).blockingStream()) {
            stream.toArray(Integer[]::new);
            fail("Should have thrown!");
        } catch (TestException expected) {
        // expected
        }
    }

    @Test
    public void close() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        up.onNext(1);
        up.onNext(2);
        up.onNext(3);
        up.onNext(4);
        up.onNext(5);
        try (Stream<Integer> stream = up.blockingStream()) {
            assertArrayEquals(new Integer[] { 1, 2, 3 }, stream.limit(3).toArray(Integer[]::new));
        }
        assertFalse(up.hasSubscribers());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableBlockingStreamTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.payloads.just.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_range() throws java.lang.Throwable {
            this.payloads.range.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeBackpressured() throws java.lang.Throwable {
            this.payloads.rangeBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeAsyncBackpressured() throws java.lang.Throwable {
            this.payloads.rangeAsyncBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeAsyncBackpressured1() throws java.lang.Throwable {
            this.payloads.rangeAsyncBackpressured1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_close() throws java.lang.Throwable {
            this.payloads.close.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableBlockingStreamTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableBlockingStreamTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableBlockingStreamTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableBlockingStreamTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableBlockingStreamTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableBlockingStreamTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableBlockingStreamTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableBlockingStreamTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement just;

            public org.junit.runners.model.Statement range;

            public org.junit.runners.model.Statement rangeBackpressured;

            public org.junit.runners.model.Statement rangeAsyncBackpressured;

            public org.junit.runners.model.Statement rangeAsyncBackpressured1;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement close;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.empty = _ClassStatement.forPayload(ObservableBlockingStreamTest::empty, "empty", this);
            this.payloads.just = _ClassStatement.forPayload(ObservableBlockingStreamTest::just, "just", this);
            this.payloads.range = _ClassStatement.forPayload(ObservableBlockingStreamTest::range, "range", this);
            this.payloads.rangeBackpressured = _ClassStatement.forPayload(ObservableBlockingStreamTest::rangeBackpressured, "rangeBackpressured", this);
            this.payloads.rangeAsyncBackpressured = _ClassStatement.forPayload(ObservableBlockingStreamTest::rangeAsyncBackpressured, "rangeAsyncBackpressured", this);
            this.payloads.rangeAsyncBackpressured1 = _ClassStatement.forPayload(ObservableBlockingStreamTest::rangeAsyncBackpressured1, "rangeAsyncBackpressured1", this);
            this.payloads.error = _ClassStatement.forPayload(ObservableBlockingStreamTest::error, "error", this);
            this.payloads.close = _ClassStatement.forPayload(ObservableBlockingStreamTest::close, "close", this);
        }
    }
}
