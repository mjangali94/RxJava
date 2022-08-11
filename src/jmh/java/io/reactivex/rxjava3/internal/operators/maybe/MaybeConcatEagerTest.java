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
package io.reactivex.rxjava3.internal.operators.maybe;

import java.util.Arrays;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;

public class MaybeConcatEagerTest {

    @Test
    public void iterableNormal() {
        Maybe.concatEager(Arrays.asList(Maybe.just(1), Maybe.empty(), Maybe.just(2))).test().assertResult(1, 2);
    }

    @Test
    public void iterableNormalMaxConcurrency() {
        Maybe.concatEager(Arrays.asList(Maybe.just(1), Maybe.empty(), Maybe.just(2)), 1).test().assertResult(1, 2);
    }

    @Test
    public void iterableError() {
        Maybe.concatEager(Arrays.asList(Maybe.just(1), Maybe.error(new TestException()), Maybe.empty(), Maybe.just(2))).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void iterableErrorMaxConcurrency() {
        Maybe.concatEager(Arrays.asList(Maybe.just(1), Maybe.error(new TestException()), Maybe.empty(), Maybe.just(2)), 1).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void publisherNormal() {
        Maybe.concatEager(Flowable.fromArray(Maybe.just(1), Maybe.empty(), Maybe.just(2))).test().assertResult(1, 2);
    }

    @Test
    public void publisherNormalMaxConcurrency() {
        Maybe.concatEager(Flowable.fromArray(Maybe.just(1), Maybe.empty(), Maybe.just(2)), 1).test().assertResult(1, 2);
    }

    @Test
    public void publisherError() {
        Maybe.concatEager(Flowable.fromArray(Maybe.just(1), Maybe.error(new TestException()), Maybe.empty(), Maybe.just(2))).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void iterableDelayError() {
        Maybe.concatEagerDelayError(Arrays.asList(Maybe.just(1), Maybe.error(new TestException()), Maybe.empty(), Maybe.just(2))).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void iterableDelayErrorMaxConcurrency() {
        Maybe.concatEagerDelayError(Arrays.asList(Maybe.just(1), Maybe.error(new TestException()), Maybe.empty(), Maybe.just(2)), 1).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void publisherDelayError() {
        Maybe.concatEagerDelayError(Flowable.fromArray(Maybe.just(1), Maybe.error(new TestException()), Maybe.empty(), Maybe.just(2))).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void publisherDelayErrorMaxConcurrency() {
        Maybe.concatEagerDelayError(Flowable.fromArray(Maybe.just(1), Maybe.error(new TestException()), Maybe.empty(), Maybe.just(2)), 1).test().assertFailure(TestException.class, 1, 2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeConcatEagerTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableNormal() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.iterableNormal);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableNormalMaxConcurrency() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.iterableNormalMaxConcurrency);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.iterableError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableErrorMaxConcurrency() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.iterableErrorMaxConcurrency);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publisherNormal() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.publisherNormal);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publisherNormalMaxConcurrency() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.publisherNormalMaxConcurrency);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publisherError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.publisherError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.iterableDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableDelayErrorMaxConcurrency() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.iterableDelayErrorMaxConcurrency);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publisherDelayError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.publisherDelayError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publisherDelayErrorMaxConcurrency() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.publisherDelayErrorMaxConcurrency);
        }

        private void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatEagerTest> payload) throws java.lang.Throwable {
            this.instance = new MaybeConcatEagerTest();
            payload.accept(this.instance);
        }

        private static class _Payloads {

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatEagerTest> iterableNormal;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatEagerTest> iterableNormalMaxConcurrency;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatEagerTest> iterableError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatEagerTest> iterableErrorMaxConcurrency;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatEagerTest> publisherNormal;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatEagerTest> publisherNormalMaxConcurrency;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatEagerTest> publisherError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatEagerTest> iterableDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatEagerTest> iterableDelayErrorMaxConcurrency;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatEagerTest> publisherDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatEagerTest> publisherDelayErrorMaxConcurrency;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.iterableNormal = MaybeConcatEagerTest::iterableNormal;
            this.payloads.iterableNormalMaxConcurrency = MaybeConcatEagerTest::iterableNormalMaxConcurrency;
            this.payloads.iterableError = MaybeConcatEagerTest::iterableError;
            this.payloads.iterableErrorMaxConcurrency = MaybeConcatEagerTest::iterableErrorMaxConcurrency;
            this.payloads.publisherNormal = MaybeConcatEagerTest::publisherNormal;
            this.payloads.publisherNormalMaxConcurrency = MaybeConcatEagerTest::publisherNormalMaxConcurrency;
            this.payloads.publisherError = MaybeConcatEagerTest::publisherError;
            this.payloads.iterableDelayError = MaybeConcatEagerTest::iterableDelayError;
            this.payloads.iterableDelayErrorMaxConcurrency = MaybeConcatEagerTest::iterableDelayErrorMaxConcurrency;
            this.payloads.publisherDelayError = MaybeConcatEagerTest::publisherDelayError;
            this.payloads.publisherDelayErrorMaxConcurrency = MaybeConcatEagerTest::publisherDelayErrorMaxConcurrency;
        }
    }
}
