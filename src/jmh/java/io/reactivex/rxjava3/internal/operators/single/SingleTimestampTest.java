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

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleTimestampTest {

    @Test
    public void just() {
        Single.just(1).timeInterval().test().assertValueCount(1).assertNoErrors().assertComplete();
    }

    @Test
    public void error() {
        Single.error(new TestException()).timeInterval().test().assertFailure(TestException.class);
    }

    @Test
    public void justSeconds() {
        Single.just(1).timeInterval(TimeUnit.SECONDS).test().assertValueCount(1).assertNoErrors().assertComplete();
    }

    @Test
    public void justScheduler() {
        Single.just(1).timeInterval(Schedulers.single()).test().assertValueCount(1).assertNoErrors().assertComplete();
    }

    @Test
    public void justSecondsScheduler() {
        Single.just(1).timeInterval(TimeUnit.SECONDS, Schedulers.single()).test().assertValueCount(1).assertNoErrors().assertComplete();
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingle(m -> m.timeInterval());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(SingleSubject.create().timeInterval());
    }

    @Test
    public void timeInfo() {
        TestScheduler scheduler = new TestScheduler();
        SingleSubject<Integer> ss = SingleSubject.create();
        TestObserver<Timed<Integer>> to = ss.timeInterval(scheduler).test();
        scheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS);
        ss.onSuccess(1);
        to.assertResult(new Timed<>(1, 1000L, TimeUnit.MILLISECONDS));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleTimestampTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.just);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.error);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justSeconds() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.justSeconds);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justScheduler() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.justScheduler);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justSecondsScheduler() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.justSecondsScheduler);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.doubleOnSubscribe);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.dispose);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeInfo() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.timeInfo);
        }

        private void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTimestampTest> payload) throws java.lang.Throwable {
            this.instance = new SingleTimestampTest();
            payload.accept(this.instance);
        }

        private static class _Payloads {

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTimestampTest> just;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTimestampTest> error;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTimestampTest> justSeconds;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTimestampTest> justScheduler;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTimestampTest> justSecondsScheduler;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTimestampTest> doubleOnSubscribe;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTimestampTest> dispose;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTimestampTest> timeInfo;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.just = SingleTimestampTest::just;
            this.payloads.error = SingleTimestampTest::error;
            this.payloads.justSeconds = SingleTimestampTest::justSeconds;
            this.payloads.justScheduler = SingleTimestampTest::justScheduler;
            this.payloads.justSecondsScheduler = SingleTimestampTest::justSecondsScheduler;
            this.payloads.doubleOnSubscribe = SingleTimestampTest::doubleOnSubscribe;
            this.payloads.dispose = SingleTimestampTest::dispose;
            this.payloads.timeInfo = SingleTimestampTest::timeInfo;
        }
    }
}
