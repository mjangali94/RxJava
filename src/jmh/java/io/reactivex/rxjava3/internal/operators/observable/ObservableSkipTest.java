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
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableSkipTest extends RxJavaTest {

    @Test(expected = IllegalArgumentException.class)
    public void skipNegativeElements() {
        Observable<String> skip = Observable.just("one", "two", "three").skip(-99);
        Observer<String> observer = TestHelper.mockObserver();
        skip.subscribe(observer);
        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, times(1)).onNext("three");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipZeroElements() {
        Observable<String> skip = Observable.just("one", "two", "three").skip(0);
        Observer<String> observer = TestHelper.mockObserver();
        skip.subscribe(observer);
        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, times(1)).onNext("three");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipOneElement() {
        Observable<String> skip = Observable.just("one", "two", "three").skip(1);
        Observer<String> observer = TestHelper.mockObserver();
        skip.subscribe(observer);
        verify(observer, never()).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, times(1)).onNext("three");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipTwoElements() {
        Observable<String> skip = Observable.just("one", "two", "three").skip(2);
        Observer<String> observer = TestHelper.mockObserver();
        skip.subscribe(observer);
        verify(observer, never()).onNext("one");
        verify(observer, never()).onNext("two");
        verify(observer, times(1)).onNext("three");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipEmptyStream() {
        Observable<String> w = Observable.empty();
        Observable<String> skip = w.skip(1);
        Observer<String> observer = TestHelper.mockObserver();
        skip.subscribe(observer);
        verify(observer, never()).onNext(any(String.class));
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void skipMultipleObservers() {
        Observable<String> skip = Observable.just("one", "two", "three").skip(2);
        Observer<String> observer1 = TestHelper.mockObserver();
        skip.subscribe(observer1);
        Observer<String> observer2 = TestHelper.mockObserver();
        skip.subscribe(observer2);
        verify(observer1, times(1)).onNext(any(String.class));
        verify(observer1, never()).onError(any(Throwable.class));
        verify(observer1, times(1)).onComplete();
        verify(observer2, times(1)).onNext(any(String.class));
        verify(observer2, never()).onError(any(Throwable.class));
        verify(observer2, times(1)).onComplete();
    }

    @Test
    public void skipError() {
        Exception e = new Exception();
        Observable<String> ok = Observable.just("one");
        Observable<String> error = Observable.error(e);
        Observable<String> skip = Observable.concat(ok, error).skip(100);
        Observer<String> observer = TestHelper.mockObserver();
        skip.subscribe(observer);
        verify(observer, never()).onNext(any(String.class));
        verify(observer, times(1)).onError(e);
        verify(observer, never()).onComplete();
    }

    @Test
    public void requestOverflowDoesNotOccur() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        Observable.range(1, 10).skip(5).subscribe(to);
        to.assertTerminated();
        to.assertComplete();
        to.assertNoErrors();
        assertEquals(Arrays.asList(6, 7, 8, 9, 10), to.values());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).skip(2));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> o) throws Exception {
                return o.skip(1);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableSkipTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipNegativeElements() throws java.lang.Throwable {
            this.payloads.skipNegativeElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipZeroElements() throws java.lang.Throwable {
            this.payloads.skipZeroElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipOneElement() throws java.lang.Throwable {
            this.payloads.skipOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipTwoElements() throws java.lang.Throwable {
            this.payloads.skipTwoElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipEmptyStream() throws java.lang.Throwable {
            this.payloads.skipEmptyStream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipMultipleObservers() throws java.lang.Throwable {
            this.payloads.skipMultipleObservers.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipError() throws java.lang.Throwable {
            this.payloads.skipError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestOverflowDoesNotOccur() throws java.lang.Throwable {
            this.payloads.requestOverflowDoesNotOccur.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableSkipTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableSkipTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableSkipTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement skipNegativeElements;

            public org.junit.runners.model.Statement skipZeroElements;

            public org.junit.runners.model.Statement skipOneElement;

            public org.junit.runners.model.Statement skipTwoElements;

            public org.junit.runners.model.Statement skipEmptyStream;

            public org.junit.runners.model.Statement skipMultipleObservers;

            public org.junit.runners.model.Statement skipError;

            public org.junit.runners.model.Statement requestOverflowDoesNotOccur;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.skipNegativeElements = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableSkipTest::skipNegativeElements, java.lang.IllegalArgumentException.class), "skipNegativeElements", this);
            this.payloads.skipZeroElements = _ClassStatement.forPayload(ObservableSkipTest::skipZeroElements, "skipZeroElements", this);
            this.payloads.skipOneElement = _ClassStatement.forPayload(ObservableSkipTest::skipOneElement, "skipOneElement", this);
            this.payloads.skipTwoElements = _ClassStatement.forPayload(ObservableSkipTest::skipTwoElements, "skipTwoElements", this);
            this.payloads.skipEmptyStream = _ClassStatement.forPayload(ObservableSkipTest::skipEmptyStream, "skipEmptyStream", this);
            this.payloads.skipMultipleObservers = _ClassStatement.forPayload(ObservableSkipTest::skipMultipleObservers, "skipMultipleObservers", this);
            this.payloads.skipError = _ClassStatement.forPayload(ObservableSkipTest::skipError, "skipError", this);
            this.payloads.requestOverflowDoesNotOccur = _ClassStatement.forPayload(ObservableSkipTest::requestOverflowDoesNotOccur, "requestOverflowDoesNotOccur", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableSkipTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableSkipTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
