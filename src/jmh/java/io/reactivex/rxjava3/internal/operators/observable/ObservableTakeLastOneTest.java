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
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableTakeLastOneTest extends RxJavaTest {

    @Test
    public void lastOfManyReturnsLast() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        Observable.range(1, 10).takeLast(1).subscribe(to);
        to.assertValue(10);
        to.assertNoErrors();
        to.assertTerminated();
    }

    @Test
    public void lastOfEmptyReturnsEmpty() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        Observable.empty().takeLast(1).subscribe(to);
        to.assertNoValues();
        to.assertNoErrors();
        to.assertTerminated();
    }

    @Test
    public void lastOfOneReturnsLast() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        Observable.just(1).takeLast(1).subscribe(to);
        to.assertValue(1);
        to.assertNoErrors();
        to.assertTerminated();
    }

    @Test
    public void unsubscribesFromUpstream() {
        final AtomicBoolean unsubscribed = new AtomicBoolean(false);
        Action unsubscribeAction = new Action() {

            @Override
            public void run() {
                unsubscribed.set(true);
            }
        };
        Observable.just(1).concatWith(Observable.<Integer>never()).doOnDispose(unsubscribeAction).takeLast(1).subscribe().dispose();
        assertTrue(unsubscribed.get());
    }

    @Test
    public void takeLastZeroProcessesAllItemsButIgnoresThem() {
        final AtomicInteger upstreamCount = new AtomicInteger();
        final int num = 10;
        long count = Observable.range(1, num).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).takeLast(0).count().blockingGet();
        assertEquals(num, upstreamCount.get());
        assertEquals(0L, count);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).takeLast(1));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> f) throws Exception {
                return f.takeLast(1);
            }
        });
    }

    @Test
    public void error() {
        Observable.error(new TestException()).takeLast(1).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableTakeLastOneTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOfManyReturnsLast() throws java.lang.Throwable {
            this.payloads.lastOfManyReturnsLast.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOfEmptyReturnsEmpty() throws java.lang.Throwable {
            this.payloads.lastOfEmptyReturnsEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOfOneReturnsLast() throws java.lang.Throwable {
            this.payloads.lastOfOneReturnsLast.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribesFromUpstream() throws java.lang.Throwable {
            this.payloads.unsubscribesFromUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastZeroProcessesAllItemsButIgnoresThem() throws java.lang.Throwable {
            this.payloads.takeLastZeroProcessesAllItemsButIgnoresThem.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeLastOneTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeLastOneTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeLastOneTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeLastOneTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableTakeLastOneTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeLastOneTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableTakeLastOneTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableTakeLastOneTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement lastOfManyReturnsLast;

            public org.junit.runners.model.Statement lastOfEmptyReturnsEmpty;

            public org.junit.runners.model.Statement lastOfOneReturnsLast;

            public org.junit.runners.model.Statement unsubscribesFromUpstream;

            public org.junit.runners.model.Statement takeLastZeroProcessesAllItemsButIgnoresThem;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement error;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.lastOfManyReturnsLast = _ClassStatement.forPayload(ObservableTakeLastOneTest::lastOfManyReturnsLast, "lastOfManyReturnsLast", this);
            this.payloads.lastOfEmptyReturnsEmpty = _ClassStatement.forPayload(ObservableTakeLastOneTest::lastOfEmptyReturnsEmpty, "lastOfEmptyReturnsEmpty", this);
            this.payloads.lastOfOneReturnsLast = _ClassStatement.forPayload(ObservableTakeLastOneTest::lastOfOneReturnsLast, "lastOfOneReturnsLast", this);
            this.payloads.unsubscribesFromUpstream = _ClassStatement.forPayload(ObservableTakeLastOneTest::unsubscribesFromUpstream, "unsubscribesFromUpstream", this);
            this.payloads.takeLastZeroProcessesAllItemsButIgnoresThem = _ClassStatement.forPayload(ObservableTakeLastOneTest::takeLastZeroProcessesAllItemsButIgnoresThem, "takeLastZeroProcessesAllItemsButIgnoresThem", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableTakeLastOneTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableTakeLastOneTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.error = _ClassStatement.forPayload(ObservableTakeLastOneTest::error, "error", this);
        }
    }
}
