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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.*;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.operators.QueueDisposable;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.UnicastSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableDistinctTest extends RxJavaTest {

    Observer<String> w;

    // nulls lead to exceptions
    final Function<String, String> TO_UPPER_WITH_EXCEPTION = new Function<String, String>() {

        @Override
        public String apply(String s) {
            if (s.equals("x")) {
                return "XX";
            }
            return s.toUpperCase();
        }
    };

    @Before
    public void before() {
        w = TestHelper.mockObserver();
    }

    @Test
    public void distinctOfNone() {
        Observable<String> src = Observable.empty();
        src.distinct().subscribe(w);
        verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void distinctOfNoneWithKeySelector() {
        Observable<String> src = Observable.empty();
        src.distinct(TO_UPPER_WITH_EXCEPTION).subscribe(w);
        verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void distinctOfNormalSource() {
        Observable<String> src = Observable.just("a", "b", "c", "c", "c", "b", "b", "a", "e");
        src.distinct().subscribe(w);
        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("b");
        inOrder.verify(w, times(1)).onNext("c");
        inOrder.verify(w, times(1)).onNext("e");
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void distinctOfNormalSourceWithKeySelector() {
        Observable<String> src = Observable.just("a", "B", "c", "C", "c", "B", "b", "a", "E");
        src.distinct(TO_UPPER_WITH_EXCEPTION).subscribe(w);
        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("B");
        inOrder.verify(w, times(1)).onNext("c");
        inOrder.verify(w, times(1)).onNext("E");
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void error() {
        Observable.error(new TestException()).distinct().test().assertFailure(TestException.class);
    }

    @Test
    public void fusedSync() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
        Observable.just(1, 1, 2, 1, 3, 2, 4, 5, 4).distinct().subscribe(to);
        to.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void fusedAsync() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
        UnicastSubject<Integer> us = UnicastSubject.create();
        us.distinct().subscribe(to);
        TestHelper.emit(us, 1, 1, 2, 1, 3, 2, 4, 5, 4);
        to.assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void fusedClear() {
        Observable.just(1, 1, 2, 1, 3, 2, 4, 5, 4).distinct().subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                QueueDisposable<?> qd = (QueueDisposable<?>) d;
                assertFalse(qd.isEmpty());
                qd.clear();
                assertTrue(qd.isEmpty());
            }

            @Override
            public void onNext(Integer value) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void collectionSupplierThrows() {
        Observable.just(1).distinct(Functions.identity(), new Supplier<Collection<Object>>() {

            @Override
            public Collection<Object> get() throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void collectionSupplierIsNull() {
        Observable.just(1).distinct(Functions.identity(), new Supplier<Collection<Object>>() {

            @Override
            public Collection<Object> get() throws Exception {
                return null;
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailure(NullPointerException.class).assertErrorMessage(ExceptionHelper.nullWarning("The collectionSupplier returned a null Collection."));
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onComplete();
                    observer.onNext(2);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.distinct().test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableDistinctTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctOfNone() throws java.lang.Throwable {
            this.payloads.distinctOfNone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctOfNoneWithKeySelector() throws java.lang.Throwable {
            this.payloads.distinctOfNoneWithKeySelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctOfNormalSource() throws java.lang.Throwable {
            this.payloads.distinctOfNormalSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctOfNormalSourceWithKeySelector() throws java.lang.Throwable {
            this.payloads.distinctOfNormalSourceWithKeySelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
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
        public void benchmark_fusedClear() throws java.lang.Throwable {
            this.payloads.fusedClear.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectionSupplierThrows() throws java.lang.Throwable {
            this.payloads.collectionSupplierThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectionSupplierIsNull() throws java.lang.Throwable {
            this.payloads.collectionSupplierIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDistinctTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDistinctTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.before();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDistinctTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDistinctTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableDistinctTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDistinctTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableDistinctTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableDistinctTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement distinctOfNone;

            public org.junit.runners.model.Statement distinctOfNoneWithKeySelector;

            public org.junit.runners.model.Statement distinctOfNormalSource;

            public org.junit.runners.model.Statement distinctOfNormalSourceWithKeySelector;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement fusedSync;

            public org.junit.runners.model.Statement fusedAsync;

            public org.junit.runners.model.Statement fusedClear;

            public org.junit.runners.model.Statement collectionSupplierThrows;

            public org.junit.runners.model.Statement collectionSupplierIsNull;

            public org.junit.runners.model.Statement badSource;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.distinctOfNone = _ClassStatement.forPayload(ObservableDistinctTest::distinctOfNone, "distinctOfNone", this);
            this.payloads.distinctOfNoneWithKeySelector = _ClassStatement.forPayload(ObservableDistinctTest::distinctOfNoneWithKeySelector, "distinctOfNoneWithKeySelector", this);
            this.payloads.distinctOfNormalSource = _ClassStatement.forPayload(ObservableDistinctTest::distinctOfNormalSource, "distinctOfNormalSource", this);
            this.payloads.distinctOfNormalSourceWithKeySelector = _ClassStatement.forPayload(ObservableDistinctTest::distinctOfNormalSourceWithKeySelector, "distinctOfNormalSourceWithKeySelector", this);
            this.payloads.error = _ClassStatement.forPayload(ObservableDistinctTest::error, "error", this);
            this.payloads.fusedSync = _ClassStatement.forPayload(ObservableDistinctTest::fusedSync, "fusedSync", this);
            this.payloads.fusedAsync = _ClassStatement.forPayload(ObservableDistinctTest::fusedAsync, "fusedAsync", this);
            this.payloads.fusedClear = _ClassStatement.forPayload(ObservableDistinctTest::fusedClear, "fusedClear", this);
            this.payloads.collectionSupplierThrows = _ClassStatement.forPayload(ObservableDistinctTest::collectionSupplierThrows, "collectionSupplierThrows", this);
            this.payloads.collectionSupplierIsNull = _ClassStatement.forPayload(ObservableDistinctTest::collectionSupplierIsNull, "collectionSupplierIsNull", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableDistinctTest::badSource, "badSource", this);
        }
    }
}
