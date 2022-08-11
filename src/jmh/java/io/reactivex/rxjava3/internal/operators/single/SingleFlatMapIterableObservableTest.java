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

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.util.CrashingIterable;
import io.reactivex.rxjava3.operators.QueueDisposable;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.*;

public class SingleFlatMapIterableObservableTest extends RxJavaTest {

    @Test
    public void normal() {
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).test().assertResult(1, 2);
    }

    @Test
    public void emptyIterable() {
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Collections.<Integer>emptyList();
            }
        }).test().assertResult();
    }

    @Test
    public void error() {
        Single.<Integer>error(new TestException()).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void take() {
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).take(1).test().assertResult(1);
    }

    @Test
    public void fused() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2);
        ;
    }

    @Test
    public void fusedNoSync() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC);
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(v, v + 1);
            }
        }).subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2);
        ;
    }

    @Test
    public void iteratorCrash() {
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new CrashingIterable(1, 100, 100);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "iterator()");
    }

    @Test
    public void hasNextCrash() {
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new CrashingIterable(100, 1, 100);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "hasNext()");
    }

    @Test
    public void nextCrash() {
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new CrashingIterable(100, 100, 1);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "next()");
    }

    @Test
    public void hasNextCrash2() {
        Single.just(1).flattenAsObservable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new CrashingIterable(100, 2, 100);
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "hasNext()", 0);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingleToObservable(new Function<Single<Object>, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Single<Object> o) throws Exception {
                return o.flattenAsObservable(new Function<Object, Iterable<Integer>>() {

                    @Override
                    public Iterable<Integer> apply(Object v) throws Exception {
                        return Collections.singleton(1);
                    }
                });
            }
        });
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Single.just(1).flattenAsObservable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                return Collections.singleton(1);
            }
        }));
    }

    @Test
    public void async1() {
        Single.just(1).flattenAsObservable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                Integer[] array = new Integer[1000 * 1000];
                Arrays.fill(array, 1);
                return Arrays.asList(array);
            }
        }).hide().observeOn(Schedulers.single()).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(1000 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void async2() {
        Single.just(1).flattenAsObservable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                Integer[] array = new Integer[1000 * 1000];
                Arrays.fill(array, 1);
                return Arrays.asList(array);
            }
        }).observeOn(Schedulers.single()).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(1000 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void async3() {
        Single.just(1).flattenAsObservable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                Integer[] array = new Integer[1000 * 1000];
                Arrays.fill(array, 1);
                return Arrays.asList(array);
            }
        }).take(500 * 1000).observeOn(Schedulers.single()).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(500 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void async4() {
        Single.just(1).flattenAsObservable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                Integer[] array = new Integer[1000 * 1000];
                Arrays.fill(array, 1);
                return Arrays.asList(array);
            }
        }).observeOn(Schedulers.single()).take(500 * 1000).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(500 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void fusedEmptyCheck() {
        Single.just(1).flattenAsObservable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                return Arrays.asList(1, 2, 3);
            }
        }).subscribe(new Observer<Integer>() {

            QueueDisposable<Integer> qd;

            @SuppressWarnings("unchecked")
            @Override
            public void onSubscribe(Disposable d) {
                qd = (QueueDisposable<Integer>) d;
                assertEquals(QueueFuseable.ASYNC, qd.requestFusion(QueueFuseable.ANY));
            }

            @Override
            public void onNext(Integer value) {
                assertFalse(qd.isEmpty());
                qd.clear();
                assertTrue(qd.isEmpty());
                qd.dispose();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleFlatMapIterableObservableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyIterable() throws java.lang.Throwable {
            this.payloads.emptyIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.payloads.fused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedNoSync() throws java.lang.Throwable {
            this.payloads.fusedNoSync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorCrash() throws java.lang.Throwable {
            this.payloads.iteratorCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCrash() throws java.lang.Throwable {
            this.payloads.hasNextCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextCrash() throws java.lang.Throwable {
            this.payloads.nextCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCrash2() throws java.lang.Throwable {
            this.payloads.hasNextCrash2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_async1() throws java.lang.Throwable {
            this.payloads.async1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_async2() throws java.lang.Throwable {
            this.payloads.async2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_async3() throws java.lang.Throwable {
            this.payloads.async3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_async4() throws java.lang.Throwable {
            this.payloads.async4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedEmptyCheck() throws java.lang.Throwable {
            this.payloads.fusedEmptyCheck.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapIterableObservableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapIterableObservableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapIterableObservableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapIterableObservableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleFlatMapIterableObservableTest();
                org.junit.runners.model.Statement statement = new _InstanceStatement(this.payload, this.benchmark);
                statement = this.applyRule(this.benchmark.instance.globalTimeout, statement);
                statement = this.applyRule(this.benchmark.instance.suppressUndeliverableRule, statement);
                statement.evaluate();
            }

            private org.junit.runners.model.Statement applyRule(org.junit.rules.TestRule rule, org.junit.runners.model.Statement statement) {
                return se.chalmers.ju2jmh.api.Rules.apply(rule, statement, this.description);
            }

            private org.junit.runners.model.Statement applyRule(org.junit.rules.MethodRule rule, org.junit.runners.model.Statement statement) {
                return se.chalmers.ju2jmh.api.Rules.apply(rule, statement, this.frameworkMethod, this.benchmark.instance);
            }

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapIterableObservableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleFlatMapIterableObservableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleFlatMapIterableObservableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement emptyIterable;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement fused;

            public org.junit.runners.model.Statement fusedNoSync;

            public org.junit.runners.model.Statement iteratorCrash;

            public org.junit.runners.model.Statement hasNextCrash;

            public org.junit.runners.model.Statement nextCrash;

            public org.junit.runners.model.Statement hasNextCrash2;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement async1;

            public org.junit.runners.model.Statement async2;

            public org.junit.runners.model.Statement async3;

            public org.junit.runners.model.Statement async4;

            public org.junit.runners.model.Statement fusedEmptyCheck;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::normal, "normal", this);
            this.payloads.emptyIterable = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::emptyIterable, "emptyIterable", this);
            this.payloads.error = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::error, "error", this);
            this.payloads.take = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::take, "take", this);
            this.payloads.fused = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::fused, "fused", this);
            this.payloads.fusedNoSync = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::fusedNoSync, "fusedNoSync", this);
            this.payloads.iteratorCrash = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::iteratorCrash, "iteratorCrash", this);
            this.payloads.hasNextCrash = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::hasNextCrash, "hasNextCrash", this);
            this.payloads.nextCrash = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::nextCrash, "nextCrash", this);
            this.payloads.hasNextCrash2 = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::hasNextCrash2, "hasNextCrash2", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.dispose = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::dispose, "dispose", this);
            this.payloads.async1 = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::async1, "async1", this);
            this.payloads.async2 = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::async2, "async2", this);
            this.payloads.async3 = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::async3, "async3", this);
            this.payloads.async4 = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::async4, "async4", this);
            this.payloads.fusedEmptyCheck = _ClassStatement.forPayload(SingleFlatMapIterableObservableTest::fusedEmptyCheck, "fusedEmptyCheck", this);
        }
    }
}
