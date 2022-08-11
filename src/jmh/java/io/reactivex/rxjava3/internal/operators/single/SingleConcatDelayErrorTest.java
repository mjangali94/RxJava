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

public class SingleConcatDelayErrorTest {

    @Test
    public void normalIterable() {
        Single.concatDelayError(Arrays.asList(Single.just(1), Single.<Integer>error(new TestException()), Single.just(2))).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void normalPublisher() {
        Single.concatDelayError(Flowable.fromArray(Single.just(1), Single.<Integer>error(new TestException()), Single.just(2))).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void normalPublisherPrefetch() {
        Single.concatDelayError(Flowable.fromArray(Single.just(1), Single.<Integer>error(new TestException()), Single.just(2)), 1).test().assertFailure(TestException.class, 1, 2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleConcatDelayErrorTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalIterable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.normalIterable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalPublisher() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.normalPublisher);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalPublisherPrefetch() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.normalPublisherPrefetch);
        }

        private void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatDelayErrorTest> payload) throws java.lang.Throwable {
            this.instance = new SingleConcatDelayErrorTest();
            payload.accept(this.instance);
        }

        private static class _Payloads {

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatDelayErrorTest> normalIterable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatDelayErrorTest> normalPublisher;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatDelayErrorTest> normalPublisherPrefetch;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normalIterable = SingleConcatDelayErrorTest::normalIterable;
            this.payloads.normalPublisher = SingleConcatDelayErrorTest::normalPublisher;
            this.payloads.normalPublisherPrefetch = SingleConcatDelayErrorTest::normalPublisherPrefetch;
        }
    }
}
