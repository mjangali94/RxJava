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

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.MaybeSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeTimeIntervalTest {

    @Test
    public void just() {
        Maybe.just(1).timeInterval().test().assertValueCount(1).assertNoErrors().assertComplete();
    }

    @Test
    public void empty() {
        Maybe.empty().timeInterval().test().assertResult();
    }

    @Test
    public void error() {
        Maybe.error(new TestException()).timeInterval().test().assertFailure(TestException.class);
    }

    @Test
    public void justSeconds() {
        Maybe.just(1).timeInterval(TimeUnit.SECONDS).test().assertValueCount(1).assertNoErrors().assertComplete();
    }

    @Test
    public void justScheduler() {
        Maybe.just(1).timeInterval(Schedulers.single()).test().assertValueCount(1).assertNoErrors().assertComplete();
    }

    @Test
    public void justSecondsScheduler() {
        Maybe.just(1).timeInterval(TimeUnit.SECONDS, Schedulers.single()).test().assertValueCount(1).assertNoErrors().assertComplete();
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(m -> m.timeInterval());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(MaybeSubject.create().timeInterval());
    }

    @Test
    public void timeInfo() {
        TestScheduler scheduler = new TestScheduler();
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestObserver<Timed<Integer>> to = ms.timeInterval(scheduler).test();
        scheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS);
        ms.onSuccess(1);
        to.assertResult(new Timed<>(1, 1000L, TimeUnit.MILLISECONDS));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeTimeIntervalTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.just);
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

        public void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeIntervalTest> payload) throws java.lang.Throwable {
            this.instance = new MaybeTimeIntervalTest();
            payload.accept(this.instance);
        }

        public static class _Payloads {

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeIntervalTest> just;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeIntervalTest> empty;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeIntervalTest> error;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeIntervalTest> justSeconds;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeIntervalTest> justScheduler;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeIntervalTest> justSecondsScheduler;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeIntervalTest> doubleOnSubscribe;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeIntervalTest> dispose;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeIntervalTest> timeInfo;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.just = MaybeTimeIntervalTest::just;
            this.payloads.empty = MaybeTimeIntervalTest::empty;
            this.payloads.error = MaybeTimeIntervalTest::error;
            this.payloads.justSeconds = MaybeTimeIntervalTest::justSeconds;
            this.payloads.justScheduler = MaybeTimeIntervalTest::justScheduler;
            this.payloads.justSecondsScheduler = MaybeTimeIntervalTest::justSecondsScheduler;
            this.payloads.doubleOnSubscribe = MaybeTimeIntervalTest::doubleOnSubscribe;
            this.payloads.dispose = MaybeTimeIntervalTest::dispose;
            this.payloads.timeInfo = MaybeTimeIntervalTest::timeInfo;
        }
    }
}
