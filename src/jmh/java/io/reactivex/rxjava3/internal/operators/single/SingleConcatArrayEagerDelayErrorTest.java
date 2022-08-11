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

import org.junit.Test;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.exceptions.TestException;

public class SingleConcatArrayEagerDelayErrorTest {

    @Test
    public void normal() {
        Single.concatArrayEagerDelayError(Single.just(1), Single.<Integer>error(new TestException()), Single.just(2)).test().assertFailure(TestException.class, 1, 2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleConcatArrayEagerDelayErrorTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.normal);
        }

        private void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatArrayEagerDelayErrorTest> payload) throws java.lang.Throwable {
            this.instance = new SingleConcatArrayEagerDelayErrorTest();
            payload.accept(this.instance);
        }

        private static class _Payloads {

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatArrayEagerDelayErrorTest> normal;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = SingleConcatArrayEagerDelayErrorTest::normal;
        }
    }
}
