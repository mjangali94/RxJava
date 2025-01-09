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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.List;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.*;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableDistinctUntilChangedTest extends RxJavaTest {

    Observer<String> w;

    Observer<String> w2;

    // nulls lead to exceptions
    final Function<String, String> TO_UPPER_WITH_EXCEPTION = new Function<String, String>() {

        @Override
        public String apply(String s) {
            if (s.equals("x")) {
                return "xx";
            }
            return s.toUpperCase();
        }
    };

    @Before
    public void before() {
        w = TestHelper.mockObserver();
        w2 = TestHelper.mockObserver();
    }

    @Test
    public void distinctUntilChangedOfNone() {
        Observable<String> src = Observable.empty();
        src.distinctUntilChanged().subscribe(w);
        verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void distinctUntilChangedOfNoneWithKeySelector() {
        Observable<String> src = Observable.empty();
        src.distinctUntilChanged(TO_UPPER_WITH_EXCEPTION).subscribe(w);
        verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void distinctUntilChangedOfNormalSource() {
        Observable<String> src = Observable.just("a", "b", "c", "c", "c", "b", "b", "a", "e");
        src.distinctUntilChanged().subscribe(w);
        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("b");
        inOrder.verify(w, times(1)).onNext("c");
        inOrder.verify(w, times(1)).onNext("b");
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("e");
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void distinctUntilChangedOfNormalSourceWithKeySelector() {
        Observable<String> src = Observable.just("a", "b", "c", "C", "c", "B", "b", "a", "e");
        src.distinctUntilChanged(TO_UPPER_WITH_EXCEPTION).subscribe(w);
        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("b");
        inOrder.verify(w, times(1)).onNext("c");
        inOrder.verify(w, times(1)).onNext("B");
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("e");
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void customComparator() {
        Observable<String> source = Observable.just("a", "b", "B", "A", "a", "C");
        TestObserver<String> to = TestObserver.create();
        source.distinctUntilChanged(new BiPredicate<String, String>() {

            @Override
            public boolean test(String a, String b) {
                return a.compareToIgnoreCase(b) == 0;
            }
        }).subscribe(to);
        to.assertValues("a", "b", "A", "C");
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void customComparatorThrows() {
        Observable<String> source = Observable.just("a", "b", "B", "A", "a", "C");
        TestObserver<String> to = TestObserver.create();
        source.distinctUntilChanged(new BiPredicate<String, String>() {

            @Override
            public boolean test(String a, String b) {
                throw new TestException();
            }
        }).subscribe(to);
        to.assertValue("a");
        to.assertNotComplete();
        to.assertError(TestException.class);
    }

    @Test
    public void fused() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
        Observable.just(1, 2, 2, 3, 3, 4, 5).distinctUntilChanged(new BiPredicate<Integer, Integer>() {

            @Override
            public boolean test(Integer a, Integer b) throws Exception {
                return a.equals(b);
            }
        }).subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void fusedAsync() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
        UnicastSubject<Integer> us = UnicastSubject.create();
        us.distinctUntilChanged(new BiPredicate<Integer, Integer>() {

            @Override
            public boolean test(Integer a, Integer b) throws Exception {
                return a.equals(b);
            }
        }).subscribe(to);
        TestHelper.emit(us, 1, 2, 2, 3, 3, 4, 5);
        to.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void ignoreCancel() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.wrap(new ObservableSource<Integer>() {

                @Override
                public void subscribe(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onNext(3);
                    observer.onError(new IOException());
                    observer.onComplete();
                }
            }).distinctUntilChanged(new BiPredicate<Integer, Integer>() {

                @Override
                public boolean test(Integer a, Integer b) throws Exception {
                    throw new TestException();
                }
            }).test().assertFailure(TestException.class, 1);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    class Mutable {

        int value;
    }

    @Test
    public void mutableWithSelector() {
        Mutable m = new Mutable();
        PublishSubject<Mutable> ps = PublishSubject.create();
        TestObserver<Mutable> to = ps.distinctUntilChanged(new Function<Mutable, Object>() {

            @Override
            public Object apply(Mutable m) throws Exception {
                return m.value;
            }
        }).test();
        ps.onNext(m);
        m.value = 1;
        ps.onNext(m);
        ps.onComplete();
        to.assertResult(m, m);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableDistinctUntilChangedTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctUntilChangedOfNone() throws java.lang.Throwable {
            this.payloads.distinctUntilChangedOfNone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctUntilChangedOfNoneWithKeySelector() throws java.lang.Throwable {
            this.payloads.distinctUntilChangedOfNoneWithKeySelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctUntilChangedOfNormalSource() throws java.lang.Throwable {
            this.payloads.distinctUntilChangedOfNormalSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctUntilChangedOfNormalSourceWithKeySelector() throws java.lang.Throwable {
            this.payloads.distinctUntilChangedOfNormalSourceWithKeySelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customComparator() throws java.lang.Throwable {
            this.payloads.customComparator.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customComparatorThrows() throws java.lang.Throwable {
            this.payloads.customComparatorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.payloads.fused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedAsync() throws java.lang.Throwable {
            this.payloads.fusedAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreCancel() throws java.lang.Throwable {
            this.payloads.ignoreCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mutableWithSelector() throws java.lang.Throwable {
            this.payloads.mutableWithSelector.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDistinctUntilChangedTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDistinctUntilChangedTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDistinctUntilChangedTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDistinctUntilChangedTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableDistinctUntilChangedTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDistinctUntilChangedTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableDistinctUntilChangedTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableDistinctUntilChangedTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement distinctUntilChangedOfNone;

            public org.junit.runners.model.Statement distinctUntilChangedOfNoneWithKeySelector;

            public org.junit.runners.model.Statement distinctUntilChangedOfNormalSource;

            public org.junit.runners.model.Statement distinctUntilChangedOfNormalSourceWithKeySelector;

            public org.junit.runners.model.Statement customComparator;

            public org.junit.runners.model.Statement customComparatorThrows;

            public org.junit.runners.model.Statement fused;

            public org.junit.runners.model.Statement fusedAsync;

            public org.junit.runners.model.Statement ignoreCancel;

            public org.junit.runners.model.Statement mutableWithSelector;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.distinctUntilChangedOfNone = _ClassStatement.forPayload(ObservableDistinctUntilChangedTest::distinctUntilChangedOfNone, "distinctUntilChangedOfNone", this);
            this.payloads.distinctUntilChangedOfNoneWithKeySelector = _ClassStatement.forPayload(ObservableDistinctUntilChangedTest::distinctUntilChangedOfNoneWithKeySelector, "distinctUntilChangedOfNoneWithKeySelector", this);
            this.payloads.distinctUntilChangedOfNormalSource = _ClassStatement.forPayload(ObservableDistinctUntilChangedTest::distinctUntilChangedOfNormalSource, "distinctUntilChangedOfNormalSource", this);
            this.payloads.distinctUntilChangedOfNormalSourceWithKeySelector = _ClassStatement.forPayload(ObservableDistinctUntilChangedTest::distinctUntilChangedOfNormalSourceWithKeySelector, "distinctUntilChangedOfNormalSourceWithKeySelector", this);
            this.payloads.customComparator = _ClassStatement.forPayload(ObservableDistinctUntilChangedTest::customComparator, "customComparator", this);
            this.payloads.customComparatorThrows = _ClassStatement.forPayload(ObservableDistinctUntilChangedTest::customComparatorThrows, "customComparatorThrows", this);
            this.payloads.fused = _ClassStatement.forPayload(ObservableDistinctUntilChangedTest::fused, "fused", this);
            this.payloads.fusedAsync = _ClassStatement.forPayload(ObservableDistinctUntilChangedTest::fusedAsync, "fusedAsync", this);
            this.payloads.ignoreCancel = _ClassStatement.forPayload(ObservableDistinctUntilChangedTest::ignoreCancel, "ignoreCancel", this);
            this.payloads.mutableWithSelector = _ClassStatement.forPayload(ObservableDistinctUntilChangedTest::mutableWithSelector, "mutableWithSelector", this);
        }
    }
}
