/**
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
package io.reactivex.rxjava3.internal.operators.flowable;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.fuseable.QueueFuseable;
import io.reactivex.rxjava3.processors.UnicastProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableDoAfterNextTest extends RxJavaTest {

    final List<Integer> values = new ArrayList<>();

    final Consumer<Integer> afterNext = new Consumer<Integer>() {

        @Override
        public void accept(Integer e) throws Exception {
            values.add(-e);
        }
    };

    final TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

        @Override
        public void onNext(Integer t) {
            super.onNext(t);
            FlowableDoAfterNextTest.this.values.add(t);
        }
    };

    @Test
    public void just() {
        Flowable.just(1).doAfterNext(afterNext).subscribeWith(ts).assertResult(1);
        assertEquals(Arrays.asList(1, -1), values);
    }

    @Test
    public void range() {
        Flowable.range(1, 5).doAfterNext(afterNext).subscribeWith(ts).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(1, -1, 2, -2, 3, -3, 4, -4, 5, -5), values);
    }

    @Test
    public void error() {
        Flowable.<Integer>error(new TestException()).doAfterNext(afterNext).subscribeWith(ts).assertFailure(TestException.class);
        assertTrue(values.isEmpty());
    }

    @Test
    public void empty() {
        Flowable.<Integer>empty().doAfterNext(afterNext).subscribeWith(ts).assertResult();
        assertTrue(values.isEmpty());
    }

    @Test
    public void syncFused() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.SYNC);
        Flowable.range(1, 5).doAfterNext(afterNext).subscribe(ts0);
        ts0.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(-1, -2, -3, -4, -5), values);
    }

    @Test
    public void asyncFusedRejected() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC);
        Flowable.range(1, 5).doAfterNext(afterNext).subscribe(ts0);
        ts0.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(-1, -2, -3, -4, -5), values);
    }

    @Test
    public void asyncFused() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.doAfterNext(afterNext).subscribe(ts0);
        ts0.assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(-1, -2, -3, -4, -5), values);
    }

    @Test
    public void justConditional() {
        Flowable.just(1).doAfterNext(afterNext).filter(Functions.alwaysTrue()).subscribeWith(ts).assertResult(1);
        assertEquals(Arrays.asList(1, -1), values);
    }

    @Test
    public void rangeConditional() {
        Flowable.range(1, 5).doAfterNext(afterNext).filter(Functions.alwaysTrue()).subscribeWith(ts).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(1, -1, 2, -2, 3, -3, 4, -4, 5, -5), values);
    }

    @Test
    public void errorConditional() {
        Flowable.<Integer>error(new TestException()).doAfterNext(afterNext).filter(Functions.alwaysTrue()).subscribeWith(ts).assertFailure(TestException.class);
        assertTrue(values.isEmpty());
    }

    @Test
    public void emptyConditional() {
        Flowable.<Integer>empty().doAfterNext(afterNext).filter(Functions.alwaysTrue()).subscribeWith(ts).assertResult();
        assertTrue(values.isEmpty());
    }

    @Test
    public void syncFusedConditional() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.SYNC);
        Flowable.range(1, 5).doAfterNext(afterNext).filter(Functions.alwaysTrue()).subscribe(ts0);
        ts0.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(-1, -2, -3, -4, -5), values);
    }

    @Test
    public void asyncFusedRejectedConditional() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC);
        Flowable.range(1, 5).doAfterNext(afterNext).filter(Functions.alwaysTrue()).subscribe(ts0);
        ts0.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(-1, -2, -3, -4, -5), values);
    }

    @Test
    public void asyncFusedConditional() {
        TestSubscriberEx<Integer> ts0 = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.doAfterNext(afterNext).filter(Functions.alwaysTrue()).subscribe(ts0);
        ts0.assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(Arrays.asList(-1, -2, -3, -4, -5), values);
    }

    @Test
    public void consumerThrows() {
        Flowable.just(1, 2).doAfterNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer e) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void consumerThrowsConditional() {
        Flowable.just(1, 2).doAfterNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer e) throws Exception {
                throw new TestException();
            }
        }).filter(Functions.alwaysTrue()).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void consumerThrowsConditional2() {
        Flowable.just(1, 2).hide().doAfterNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer e) throws Exception {
                throw new TestException();
            }
        }).filter(Functions.alwaysTrue()).test().assertFailure(TestException.class, 1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class myBenchmark extends io.reactivex.rxjava3.core.RxJavaTest.myBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::just, this.description("just"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_range() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::range, this.description("range"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::error, this.description("error"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::empty, this.description("empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFused, this.description("syncFused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedRejected() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedRejected, this.description("asyncFusedRejected"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFused() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFused, this.description("asyncFused"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::justConditional, this.description("justConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::rangeConditional, this.description("rangeConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorConditional, this.description("errorConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyConditional, this.description("emptyConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::syncFusedConditional, this.description("syncFusedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedRejectedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedRejectedConditional, this.description("asyncFusedRejectedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncFusedConditional, this.description("asyncFusedConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_consumerThrows() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::consumerThrows, this.description("consumerThrows"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_consumerThrowsConditional() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::consumerThrowsConditional, this.description("consumerThrowsConditional"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_consumerThrowsConditional2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::consumerThrowsConditional2, this.description("consumerThrowsConditional2"));
        }

        private FlowableDoAfterNextTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FlowableDoAfterNextTest();
        }

        @java.lang.Override
        public FlowableDoAfterNextTest implementation() {
            return this.implementation;
        }
    }
}
