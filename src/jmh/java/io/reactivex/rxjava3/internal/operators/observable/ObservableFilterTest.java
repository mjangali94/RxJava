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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.mockito.Mockito;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.subjects.UnicastSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableFilterTest extends RxJavaTest {

    @Test
    public void filter() {
        Observable<String> w = Observable.just("one", "two", "three");
        Observable<String> observable = w.filter(new Predicate<String>() {

            @Override
            public boolean test(String t1) {
                return t1.equals("two");
            }
        });
        Observer<String> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, Mockito.never()).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, Mockito.never()).onNext("three");
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.range(1, 5).filter(Functions.alwaysTrue()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.filter(Functions.alwaysTrue());
            }
        });
    }

    @Test
    public void fusedSync() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
        Observable.range(1, 5).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).subscribe(to);
        to.assertFusionMode(QueueFuseable.SYNC).assertResult(2, 4);
    }

    @Test
    public void fusedAsync() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
        UnicastSubject<Integer> us = UnicastSubject.create();
        us.filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).subscribe(to);
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        to.assertFusionMode(QueueFuseable.ASYNC).assertResult(2, 4);
    }

    @Test
    public void fusedReject() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY | QueueFuseable.BOUNDARY);
        Observable.range(1, 5).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(2, 4);
    }

    @Test
    public void filterThrows() {
        Observable.range(1, 5).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableFilterTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filter() throws java.lang.Throwable {
            this.payloads.filter.evaluate();
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
        public void benchmark_fusedSync() throws java.lang.Throwable {
            this.payloads.fusedSync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedAsync() throws java.lang.Throwable {
            this.payloads.fusedAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedReject() throws java.lang.Throwable {
            this.payloads.fusedReject.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterThrows() throws java.lang.Throwable {
            this.payloads.filterThrows.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFilterTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFilterTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFilterTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFilterTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableFilterTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFilterTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableFilterTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableFilterTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement filter;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement fusedSync;

            public org.junit.runners.model.Statement fusedAsync;

            public org.junit.runners.model.Statement fusedReject;

            public org.junit.runners.model.Statement filterThrows;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.filter = _ClassStatement.forPayload(ObservableFilterTest::filter, "filter", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableFilterTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableFilterTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.fusedSync = _ClassStatement.forPayload(ObservableFilterTest::fusedSync, "fusedSync", this);
            this.payloads.fusedAsync = _ClassStatement.forPayload(ObservableFilterTest::fusedAsync, "fusedAsync", this);
            this.payloads.fusedReject = _ClassStatement.forPayload(ObservableFilterTest::fusedReject, "fusedReject", this);
            this.payloads.filterThrows = _ClassStatement.forPayload(ObservableFilterTest::filterThrows, "filterThrows", this);
        }
    }
}
