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
package io.reactivex.rxjava3.internal.operators.observable;

import static org.junit.Assert.*;
import java.io.IOException;
import org.junit.Test;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableOnErrorCompleteTest {

    @Test
    public void normal() {
        Observable.range(1, 10).onErrorComplete().test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void empty() {
        Observable.empty().onErrorComplete().test().assertResult();
    }

    @Test
    public void error() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Observable.error(new TestException()).onErrorComplete().test().assertResult();
            assertTrue("" + errors, errors.isEmpty());
        });
    }

    @Test
    public void errorMatches() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Observable.error(new TestException()).onErrorComplete(error -> error instanceof TestException).test().assertResult();
            assertTrue("" + errors, errors.isEmpty());
        });
    }

    @Test
    public void errorNotMatches() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Observable.error(new IOException()).onErrorComplete(error -> error instanceof TestException).test().assertFailure(IOException.class);
            assertTrue("" + errors, errors.isEmpty());
        });
    }

    @Test
    public void errorPredicateCrash() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            TestObserverEx<Object> to = Observable.error(new IOException()).onErrorComplete(error -> {
                throw new TestException();
            }).subscribeWith(new TestObserverEx<>()).assertFailure(CompositeException.class);
            TestHelper.assertError(to, 0, IOException.class);
            TestHelper.assertError(to, 1, TestException.class);
            assertTrue("" + errors, errors.isEmpty());
        });
    }

    @Test
    public void itemsThenError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Observable.range(1, 5).map(v -> 4 / (3 - v)).onErrorComplete().test().assertResult(2, 4);
            assertTrue("" + errors, errors.isEmpty());
        });
    }

    @Test
    public void dispose() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.onErrorComplete().test();
        assertTrue("No subscribers?!", ps.hasObservers());
        to.dispose();
        assertFalse("Still subscribers?!", ps.hasObservers());
    }

    @Test
    public void onSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(f -> f.onErrorComplete());
    }

    @Test
    public void isDisposed() {
        TestHelper.checkDisposed(PublishSubject.create().onErrorComplete());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableOnErrorCompleteTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.normal);
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
        public void benchmark_dispose() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.dispose);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribe() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.onSubscribe);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposed() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.isDisposed);
        }

        private void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorCompleteTest> payload) throws java.lang.Throwable {
            this.instance = new ObservableOnErrorCompleteTest();
            payload.accept(this.instance);
        }

        private static class _Payloads {

            public se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorCompleteTest> normal;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorCompleteTest> empty;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorCompleteTest> error;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorCompleteTest> errorMatches;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorCompleteTest> errorNotMatches;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorCompleteTest> errorPredicateCrash;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorCompleteTest> itemsThenError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorCompleteTest> dispose;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorCompleteTest> onSubscribe;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorCompleteTest> isDisposed;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = ObservableOnErrorCompleteTest::normal;
            this.payloads.empty = ObservableOnErrorCompleteTest::empty;
            this.payloads.error = ObservableOnErrorCompleteTest::error;
            this.payloads.errorMatches = ObservableOnErrorCompleteTest::errorMatches;
            this.payloads.errorNotMatches = ObservableOnErrorCompleteTest::errorNotMatches;
            this.payloads.errorPredicateCrash = ObservableOnErrorCompleteTest::errorPredicateCrash;
            this.payloads.itemsThenError = ObservableOnErrorCompleteTest::itemsThenError;
            this.payloads.dispose = ObservableOnErrorCompleteTest::dispose;
            this.payloads.onSubscribe = ObservableOnErrorCompleteTest::onSubscribe;
            this.payloads.isDisposed = ObservableOnErrorCompleteTest::isDisposed;
        }
    }
}
