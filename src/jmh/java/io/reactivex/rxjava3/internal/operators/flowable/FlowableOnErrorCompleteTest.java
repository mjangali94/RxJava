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
package io.reactivex.rxjava3.internal.operators.flowable;

import static org.junit.Assert.*;
import java.io.IOException;
import org.junit.Test;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableOnErrorCompleteTest {

    @Test
    public void normal() {
        Flowable.range(1, 10).onErrorComplete().test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalBackpressured() {
        Flowable.range(1, 10).onErrorComplete().test(0).assertEmpty().requestMore(3).assertValuesOnly(1, 2, 3).requestMore(3).assertValuesOnly(1, 2, 3, 4, 5, 6).requestMore(4).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void empty() {
        Flowable.empty().onErrorComplete().test().assertResult();
    }

    @Test
    public void error() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Flowable.error(new TestException()).onErrorComplete().test().assertResult();
            assertTrue("" + errors, errors.isEmpty());
        });
    }

    @Test
    public void errorMatches() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Flowable.error(new TestException()).onErrorComplete(error -> error instanceof TestException).test().assertResult();
            assertTrue("" + errors, errors.isEmpty());
        });
    }

    @Test
    public void errorNotMatches() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Flowable.error(new IOException()).onErrorComplete(error -> error instanceof TestException).test().assertFailure(IOException.class);
            assertTrue("" + errors, errors.isEmpty());
        });
    }

    @Test
    public void errorPredicateCrash() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            TestSubscriberEx<Object> ts = Flowable.error(new IOException()).onErrorComplete(error -> {
                throw new TestException();
            }).subscribeWith(new TestSubscriberEx<>()).assertFailure(CompositeException.class);
            TestHelper.assertError(ts, 0, IOException.class);
            TestHelper.assertError(ts, 1, TestException.class);
            assertTrue("" + errors, errors.isEmpty());
        });
    }

    @Test
    public void itemsThenError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Flowable.range(1, 5).map(v -> 4 / (3 - v)).onErrorComplete().test().assertResult(2, 4);
            assertTrue("" + errors, errors.isEmpty());
        });
    }

    @Test
    public void cancel() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.onErrorComplete().test();
        assertTrue("No subscribers?!", pp.hasSubscribers());
        ts.cancel();
        assertFalse("Still subscribers?!", pp.hasSubscribers());
    }

    @Test
    public void onSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.onErrorComplete());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableOnErrorCompleteTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.normal);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalBackpressured() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.normalBackpressured);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.empty);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.error);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorMatches() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.errorMatches);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNotMatches() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.errorNotMatches);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorPredicateCrash() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.errorPredicateCrash);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_itemsThenError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.itemsThenError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.cancel);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribe() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.onSubscribe);
        }

        public void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorCompleteTest> payload) throws java.lang.Throwable {
            this.instance = new FlowableOnErrorCompleteTest();
            payload.accept(this.instance);
        }

        public static class _Payloads {

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorCompleteTest> normal;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorCompleteTest> normalBackpressured;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorCompleteTest> empty;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorCompleteTest> error;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorCompleteTest> errorMatches;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorCompleteTest> errorNotMatches;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorCompleteTest> errorPredicateCrash;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorCompleteTest> itemsThenError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorCompleteTest> cancel;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorCompleteTest> onSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = FlowableOnErrorCompleteTest::normal;
            this.payloads.normalBackpressured = FlowableOnErrorCompleteTest::normalBackpressured;
            this.payloads.empty = FlowableOnErrorCompleteTest::empty;
            this.payloads.error = FlowableOnErrorCompleteTest::error;
            this.payloads.errorMatches = FlowableOnErrorCompleteTest::errorMatches;
            this.payloads.errorNotMatches = FlowableOnErrorCompleteTest::errorNotMatches;
            this.payloads.errorPredicateCrash = FlowableOnErrorCompleteTest::errorPredicateCrash;
            this.payloads.itemsThenError = FlowableOnErrorCompleteTest::itemsThenError;
            this.payloads.cancel = FlowableOnErrorCompleteTest::cancel;
            this.payloads.onSubscribe = FlowableOnErrorCompleteTest::onSubscribe;
        }
    }
}
