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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableSkipLastTest extends RxJavaTest {

    @Test
    public void skipLastEmpty() {
        Observable<String> o = Observable.<String>empty().skipLast(2);
        Observer<String> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        verify(observer, never()).onNext(any(String.class));
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipLast1() {
        Observable<String> o = Observable.fromIterable(Arrays.asList("one", "two", "three")).skipLast(2);
        Observer<String> observer = TestHelper.mockObserver();
        InOrder inOrder = inOrder(observer);
        o.subscribe(observer);
        inOrder.verify(observer, never()).onNext("two");
        inOrder.verify(observer, never()).onNext("three");
        verify(observer, times(1)).onNext("one");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipLast2() {
        Observable<String> o = Observable.fromIterable(Arrays.asList("one", "two")).skipLast(2);
        Observer<String> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        verify(observer, never()).onNext(any(String.class));
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipLastWithZeroCount() {
        Observable<String> w = Observable.just("one", "two");
        Observable<String> observable = w.skipLast(0);
        Observer<String> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipLastWithBackpressure() {
        Observable<Integer> o = Observable.range(0, Flowable.bufferSize() * 2).skipLast(Flowable.bufferSize() + 10);
        TestObserver<Integer> to = new TestObserver<>();
        o.observeOn(Schedulers.computation()).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors();
        assertEquals((Flowable.bufferSize()) - 10, to.values().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void skipLastWithNegativeCount() {
        Observable.just("one").skipLast(-1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).skipLast(1));
    }

    @Test
    public void error() {
        Observable.error(new TestException()).skipLast(1).test().assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> o) throws Exception {
                return o.skipLast(1);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableSkipLastTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastEmpty() throws java.lang.Throwable {
            this.payloads.skipLastEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLast1() throws java.lang.Throwable {
            this.payloads.skipLast1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLast2() throws java.lang.Throwable {
            this.payloads.skipLast2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastWithZeroCount() throws java.lang.Throwable {
            this.payloads.skipLastWithZeroCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastWithBackpressure() throws java.lang.Throwable {
            this.payloads.skipLastWithBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastWithNegativeCount() throws java.lang.Throwable {
            this.payloads.skipLastWithNegativeCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipLastTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipLastTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipLastTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipLastTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableSkipLastTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipLastTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableSkipLastTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableSkipLastTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement skipLastEmpty;

            public org.junit.runners.model.Statement skipLast1;

            public org.junit.runners.model.Statement skipLast2;

            public org.junit.runners.model.Statement skipLastWithZeroCount;

            public org.junit.runners.model.Statement skipLastWithBackpressure;

            public org.junit.runners.model.Statement skipLastWithNegativeCount;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.skipLastEmpty = _ClassStatement.forPayload(ObservableSkipLastTest::skipLastEmpty, "skipLastEmpty", this);
            this.payloads.skipLast1 = _ClassStatement.forPayload(ObservableSkipLastTest::skipLast1, "skipLast1", this);
            this.payloads.skipLast2 = _ClassStatement.forPayload(ObservableSkipLastTest::skipLast2, "skipLast2", this);
            this.payloads.skipLastWithZeroCount = _ClassStatement.forPayload(ObservableSkipLastTest::skipLastWithZeroCount, "skipLastWithZeroCount", this);
            this.payloads.skipLastWithBackpressure = _ClassStatement.forPayload(ObservableSkipLastTest::skipLastWithBackpressure, "skipLastWithBackpressure", this);
            this.payloads.skipLastWithNegativeCount = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableSkipLastTest::skipLastWithNegativeCount, java.lang.IllegalArgumentException.class), "skipLastWithNegativeCount", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableSkipLastTest::dispose, "dispose", this);
            this.payloads.error = _ClassStatement.forPayload(ObservableSkipLastTest::error, "error", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableSkipLastTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
