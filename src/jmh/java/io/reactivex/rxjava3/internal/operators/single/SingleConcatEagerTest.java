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

import java.util.Arrays;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;

public class SingleConcatEagerTest {

    @Test
    public void iterableNormal() {
        Single.concatEager(Arrays.asList(Single.just(1), Single.just(2))).test().assertResult(1, 2);
    }

    @Test
    public void iterableNormalMaxConcurrency() {
        Single.concatEager(Arrays.asList(Single.just(1), Single.just(2)), 1).test().assertResult(1, 2);
    }

    @Test
    public void iterableError() {
        Single.concatEager(Arrays.asList(Single.just(1), Single.error(new TestException()), Single.just(2))).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void iterableErrorMaxConcurrency() {
        Single.concatEager(Arrays.asList(Single.just(1), Single.error(new TestException()), Single.just(2)), 1).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void publisherNormal() {
        Single.concatEager(Flowable.fromArray(Single.just(1), Single.just(2))).test().assertResult(1, 2);
    }

    @Test
    public void publisherNormalMaxConcurrency() {
        Single.concatEager(Flowable.fromArray(Single.just(1), Single.just(2)), 1).test().assertResult(1, 2);
    }

    @Test
    public void publisherError() {
        Single.concatEager(Flowable.fromArray(Single.just(1), Single.error(new TestException()), Single.just(2))).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void iterableDelayError() {
        Single.concatEagerDelayError(Arrays.asList(Single.just(1), Single.error(new TestException()), Single.just(2))).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void iterableDelayErrorMaxConcurrency() {
        Single.concatEagerDelayError(Arrays.asList(Single.just(1), Single.error(new TestException()), Single.just(2)), 1).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void publisherDelayError() {
        Single.concatEagerDelayError(Flowable.fromArray(Single.just(1), Single.error(new TestException()), Single.just(2))).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void publisherDelayErrorMaxConcurrency() {
        Single.concatEagerDelayError(Flowable.fromArray(Single.just(1), Single.error(new TestException()), Single.just(2)), 1).test().assertFailure(TestException.class, 1, 2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleConcatEagerTest instance;

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

        private void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatEagerTest> payload) throws java.lang.Throwable {
            this.instance = new SingleConcatEagerTest();
            payload.accept(this.instance);
        }

        private static class _Payloads {

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatEagerTest> iterableNormal;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatEagerTest> iterableNormalMaxConcurrency;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatEagerTest> iterableError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatEagerTest> iterableErrorMaxConcurrency;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatEagerTest> publisherNormal;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatEagerTest> publisherNormalMaxConcurrency;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatEagerTest> publisherError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatEagerTest> iterableDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatEagerTest> iterableDelayErrorMaxConcurrency;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatEagerTest> publisherDelayError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatEagerTest> publisherDelayErrorMaxConcurrency;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.iterableNormal = SingleConcatEagerTest::iterableNormal;
            this.payloads.iterableNormalMaxConcurrency = SingleConcatEagerTest::iterableNormalMaxConcurrency;
            this.payloads.iterableError = SingleConcatEagerTest::iterableError;
            this.payloads.iterableErrorMaxConcurrency = SingleConcatEagerTest::iterableErrorMaxConcurrency;
            this.payloads.publisherNormal = SingleConcatEagerTest::publisherNormal;
            this.payloads.publisherNormalMaxConcurrency = SingleConcatEagerTest::publisherNormalMaxConcurrency;
            this.payloads.publisherError = SingleConcatEagerTest::publisherError;
            this.payloads.iterableDelayError = SingleConcatEagerTest::iterableDelayError;
            this.payloads.iterableDelayErrorMaxConcurrency = SingleConcatEagerTest::iterableDelayErrorMaxConcurrency;
            this.payloads.publisherDelayError = SingleConcatEagerTest::publisherDelayError;
            this.payloads.publisherDelayErrorMaxConcurrency = SingleConcatEagerTest::publisherDelayErrorMaxConcurrency;
        }
    }
}
