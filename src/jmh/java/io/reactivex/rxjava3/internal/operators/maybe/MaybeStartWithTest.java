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

import static org.mockito.Mockito.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;

public class MaybeStartWithTest {

    @Test
    public void justCompletableComplete() {
        Maybe.just(1).startWith(Completable.complete()).test().assertResult(1);
    }

    @Test
    public void emptyCompletableComplete() {
        Maybe.empty().startWith(Completable.complete()).test().assertResult();
    }

    @Test
    public void runCompletableError() {
        Runnable run = mock(Runnable.class);
        Maybe.fromRunnable(run).startWith(Completable.error(new TestException())).test().assertFailure(TestException.class);
        verify(run, never()).run();
    }

    @Test
    public void justSingleJust() {
        Maybe.just(1).startWith(Single.just(2)).test().assertResult(2, 1);
    }

    @Test
    public void emptySingleJust() {
        Runnable run = mock(Runnable.class);
        Maybe.fromRunnable(run).startWith(Single.just(2)).test().assertResult(2);
        verify(run).run();
    }

    @Test
    public void runSingleError() {
        Runnable run = mock(Runnable.class);
        Maybe.fromRunnable(run).startWith(Single.error(new TestException())).test().assertFailure(TestException.class);
        verify(run, never()).run();
    }

    @Test
    public void justMaybeJust() {
        Maybe.just(1).startWith(Maybe.just(2)).test().assertResult(2, 1);
    }

    @Test
    public void emptyMaybeJust() {
        Runnable run = mock(Runnable.class);
        Maybe.fromRunnable(run).startWith(Maybe.just(2)).test().assertResult(2);
        verify(run).run();
    }

    @Test
    public void runMaybeError() {
        Runnable run = mock(Runnable.class);
        Maybe.fromRunnable(run).startWith(Maybe.error(new TestException())).test().assertFailure(TestException.class);
        verify(run, never()).run();
    }

    @Test
    public void justObservableJust() {
        Maybe.just(1).startWith(Observable.just(2, 3, 4, 5)).test().assertResult(2, 3, 4, 5, 1);
    }

    @Test
    public void emptyObservableJust() {
        Runnable run = mock(Runnable.class);
        Maybe.fromRunnable(run).startWith(Observable.just(2, 3, 4, 5)).test().assertResult(2, 3, 4, 5);
        verify(run).run();
    }

    @Test
    public void emptyObservableEmpty() {
        Runnable run = mock(Runnable.class);
        Runnable run2 = mock(Runnable.class);
        Maybe.fromRunnable(run).startWith(Observable.fromRunnable(run2)).test().assertResult();
        verify(run).run();
        verify(run2).run();
    }

    @Test
    public void runObservableError() {
        Runnable run = mock(Runnable.class);
        Maybe.fromRunnable(run).startWith(Observable.error(new TestException())).test().assertFailure(TestException.class);
        verify(run, never()).run();
    }

    @Test
    public void justFlowableJust() {
        Maybe.just(1).startWith(Flowable.just(2, 3, 4, 5)).test().assertResult(2, 3, 4, 5, 1);
    }

    @Test
    public void emptyFlowableJust() {
        Runnable run = mock(Runnable.class);
        Maybe.fromRunnable(run).startWith(Flowable.just(2, 3, 4, 5)).test().assertResult(2, 3, 4, 5);
        verify(run).run();
    }

    @Test
    public void emptyFlowableEmpty() {
        Runnable run = mock(Runnable.class);
        Runnable run2 = mock(Runnable.class);
        Maybe.fromRunnable(run).startWith(Flowable.fromRunnable(run2)).test().assertResult();
        verify(run).run();
        verify(run2).run();
    }

    @Test
    public void runFlowableError() {
        Runnable run = mock(Runnable.class);
        Maybe.fromRunnable(run).startWith(Flowable.error(new TestException())).test().assertFailure(TestException.class);
        verify(run, never()).run();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeStartWithTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justCompletableComplete() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.justCompletableComplete);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyCompletableComplete() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.emptyCompletableComplete);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runCompletableError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.runCompletableError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justSingleJust() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.justSingleJust);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptySingleJust() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.emptySingleJust);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runSingleError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.runSingleError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justMaybeJust() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.justMaybeJust);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyMaybeJust() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.emptyMaybeJust);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runMaybeError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.runMaybeError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justObservableJust() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.justObservableJust);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyObservableJust() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.emptyObservableJust);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyObservableEmpty() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.emptyObservableEmpty);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runObservableError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.runObservableError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justFlowableJust() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.justFlowableJust);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyFlowableJust() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.emptyFlowableJust);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyFlowableEmpty() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.emptyFlowableEmpty);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runFlowableError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.runFlowableError);
        }

        public void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> payload) throws java.lang.Throwable {
            this.instance = new MaybeStartWithTest();
            payload.accept(this.instance);
        }

        public static class _Payloads {

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> justCompletableComplete;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> emptyCompletableComplete;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> runCompletableError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> justSingleJust;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> emptySingleJust;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> runSingleError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> justMaybeJust;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> emptyMaybeJust;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> runMaybeError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> justObservableJust;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> emptyObservableJust;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> emptyObservableEmpty;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> runObservableError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> justFlowableJust;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> emptyFlowableJust;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> emptyFlowableEmpty;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeStartWithTest> runFlowableError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.justCompletableComplete = MaybeStartWithTest::justCompletableComplete;
            this.payloads.emptyCompletableComplete = MaybeStartWithTest::emptyCompletableComplete;
            this.payloads.runCompletableError = MaybeStartWithTest::runCompletableError;
            this.payloads.justSingleJust = MaybeStartWithTest::justSingleJust;
            this.payloads.emptySingleJust = MaybeStartWithTest::emptySingleJust;
            this.payloads.runSingleError = MaybeStartWithTest::runSingleError;
            this.payloads.justMaybeJust = MaybeStartWithTest::justMaybeJust;
            this.payloads.emptyMaybeJust = MaybeStartWithTest::emptyMaybeJust;
            this.payloads.runMaybeError = MaybeStartWithTest::runMaybeError;
            this.payloads.justObservableJust = MaybeStartWithTest::justObservableJust;
            this.payloads.emptyObservableJust = MaybeStartWithTest::emptyObservableJust;
            this.payloads.emptyObservableEmpty = MaybeStartWithTest::emptyObservableEmpty;
            this.payloads.runObservableError = MaybeStartWithTest::runObservableError;
            this.payloads.justFlowableJust = MaybeStartWithTest::justFlowableJust;
            this.payloads.emptyFlowableJust = MaybeStartWithTest::emptyFlowableJust;
            this.payloads.emptyFlowableEmpty = MaybeStartWithTest::emptyFlowableEmpty;
            this.payloads.runFlowableError = MaybeStartWithTest::runFlowableError;
        }
    }
}
