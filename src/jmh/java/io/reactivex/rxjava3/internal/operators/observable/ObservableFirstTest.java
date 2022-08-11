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
import java.util.NoSuchElementException;
import org.junit.*;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableFirstTest extends RxJavaTest {

    Observer<String> w;

    SingleObserver<Object> wo;

    MaybeObserver<Object> wm;

    private static final Predicate<String> IS_D = new Predicate<String>() {

        @Override
        public boolean test(String value) {
            return "d".equals(value);
        }
    };

    @Before
    public void before() {
        w = TestHelper.mockObserver();
        wo = TestHelper.mockSingleObserver();
        wm = TestHelper.mockMaybeObserver();
    }

    @Test
    public void firstOrElseOfNoneObservable() {
        Observable<String> src = Observable.empty();
        src.first("default").toObservable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("default");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstOrElseOfSomeObservable() {
        Observable<String> src = Observable.just("a", "b", "c");
        src.first("default").toObservable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("a");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstOrElseWithPredicateOfNoneMatchingThePredicateObservable() {
        Observable<String> src = Observable.just("a", "b", "c");
        src.filter(IS_D).first("default").toObservable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("default");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstOrElseWithPredicateOfSomeObservable() {
        Observable<String> src = Observable.just("a", "b", "c", "d", "e", "f");
        src.filter(IS_D).first("default").toObservable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("d");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstObservable() {
        Observable<Integer> o = Observable.just(1, 2, 3).firstElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithOneElementObservable() {
        Observable<Integer> o = Observable.just(1).firstElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithEmptyObservable() {
        Observable<Integer> o = Observable.<Integer>empty().firstElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateObservable() {
        Observable<Integer> o = Observable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndOneElementObservable() {
        Observable<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndEmptyObservable() {
        Observable<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultObservable() {
        Observable<Integer> o = Observable.just(1, 2, 3).first(4).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithOneElementObservable() {
        Observable<Integer> o = Observable.just(1).first(2).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithEmptyObservable() {
        Observable<Integer> o = Observable.<Integer>empty().first(1).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateObservable() {
        Observable<Integer> o = Observable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(8).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndOneElementObservable() {
        Observable<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(4).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndEmptyObservable() {
        Observable<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(2).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrElseOfNone() {
        Observable<String> src = Observable.empty();
        src.first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("default");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstOrElseOfSome() {
        Observable<String> src = Observable.just("a", "b", "c");
        src.first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("a");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstOrElseWithPredicateOfNoneMatchingThePredicate() {
        Observable<String> src = Observable.just("a", "b", "c");
        src.filter(IS_D).first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("default");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstOrElseWithPredicateOfSome() {
        Observable<String> src = Observable.just("a", "b", "c", "d", "e", "f");
        src.filter(IS_D).first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("d");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void first() {
        Maybe<Integer> o = Observable.just(1, 2, 3).firstElement();
        o.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithOneElement() {
        Maybe<Integer> o = Observable.just(1).firstElement();
        o.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithEmpty() {
        Maybe<Integer> o = Observable.<Integer>empty().firstElement();
        o.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onComplete();
        inOrder.verify(wm, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicate() {
        Maybe<Integer> o = Observable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement();
        o.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndOneElement() {
        Maybe<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement();
        o.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndEmpty() {
        Maybe<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement();
        o.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onComplete();
        inOrder.verify(wm, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefault() {
        Single<Integer> o = Observable.just(1, 2, 3).first(4);
        o.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithOneElement() {
        Single<Integer> o = Observable.just(1).first(2);
        o.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithEmpty() {
        Single<Integer> o = Observable.<Integer>empty().first(1);
        o.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicate() {
        Single<Integer> o = Observable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(8);
        o.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndOneElement() {
        Single<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(4);
        o.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndEmpty() {
        Single<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(2);
        o.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrErrorNoElement() {
        Observable.empty().firstOrError().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void firstOrErrorOneElement() {
        Observable.just(1).firstOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorMultipleElements() {
        Observable.just(1, 2, 3).firstOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorError() {
        Observable.error(new RuntimeException("error")).firstOrError().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void firstOrErrorNoElementObservable() {
        Observable.empty().firstOrError().toObservable().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void firstOrErrorOneElementObservable() {
        Observable.just(1).firstOrError().toObservable().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorMultipleElementsObservable() {
        Observable.just(1, 2, 3).firstOrError().toObservable().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorErrorObservable() {
        Observable.error(new RuntimeException("error")).firstOrError().toObservable().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableFirstTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseOfNoneObservable() throws java.lang.Throwable {
            this.payloads.firstOrElseOfNoneObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseOfSomeObservable() throws java.lang.Throwable {
            this.payloads.firstOrElseOfSomeObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseWithPredicateOfNoneMatchingThePredicateObservable() throws java.lang.Throwable {
            this.payloads.firstOrElseWithPredicateOfNoneMatchingThePredicateObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseWithPredicateOfSomeObservable() throws java.lang.Throwable {
            this.payloads.firstOrElseWithPredicateOfSomeObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstObservable() throws java.lang.Throwable {
            this.payloads.firstObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithOneElementObservable() throws java.lang.Throwable {
            this.payloads.firstWithOneElementObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithEmptyObservable() throws java.lang.Throwable {
            this.payloads.firstWithEmptyObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateObservable() throws java.lang.Throwable {
            this.payloads.firstWithPredicateObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateAndOneElementObservable() throws java.lang.Throwable {
            this.payloads.firstWithPredicateAndOneElementObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateAndEmptyObservable() throws java.lang.Throwable {
            this.payloads.firstWithPredicateAndEmptyObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultObservable() throws java.lang.Throwable {
            this.payloads.firstOrDefaultObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithOneElementObservable() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithOneElementObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithEmptyObservable() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithEmptyObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateObservable() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithPredicateObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateAndOneElementObservable() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithPredicateAndOneElementObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateAndEmptyObservable() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithPredicateAndEmptyObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseOfNone() throws java.lang.Throwable {
            this.payloads.firstOrElseOfNone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseOfSome() throws java.lang.Throwable {
            this.payloads.firstOrElseOfSome.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseWithPredicateOfNoneMatchingThePredicate() throws java.lang.Throwable {
            this.payloads.firstOrElseWithPredicateOfNoneMatchingThePredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseWithPredicateOfSome() throws java.lang.Throwable {
            this.payloads.firstOrElseWithPredicateOfSome.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_first() throws java.lang.Throwable {
            this.payloads.first.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithOneElement() throws java.lang.Throwable {
            this.payloads.firstWithOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithEmpty() throws java.lang.Throwable {
            this.payloads.firstWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicate() throws java.lang.Throwable {
            this.payloads.firstWithPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateAndOneElement() throws java.lang.Throwable {
            this.payloads.firstWithPredicateAndOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateAndEmpty() throws java.lang.Throwable {
            this.payloads.firstWithPredicateAndEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefault() throws java.lang.Throwable {
            this.payloads.firstOrDefault.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithOneElement() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithEmpty() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicate() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateAndOneElement() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithPredicateAndOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateAndEmpty() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithPredicateAndEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorNoElement() throws java.lang.Throwable {
            this.payloads.firstOrErrorNoElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorOneElement() throws java.lang.Throwable {
            this.payloads.firstOrErrorOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorMultipleElements() throws java.lang.Throwable {
            this.payloads.firstOrErrorMultipleElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorError() throws java.lang.Throwable {
            this.payloads.firstOrErrorError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorNoElementObservable() throws java.lang.Throwable {
            this.payloads.firstOrErrorNoElementObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorOneElementObservable() throws java.lang.Throwable {
            this.payloads.firstOrErrorOneElementObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorMultipleElementsObservable() throws java.lang.Throwable {
            this.payloads.firstOrErrorMultipleElementsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorErrorObservable() throws java.lang.Throwable {
            this.payloads.firstOrErrorErrorObservable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFirstTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFirstTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFirstTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFirstTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableFirstTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFirstTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableFirstTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableFirstTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement firstOrElseOfNoneObservable;

            public org.junit.runners.model.Statement firstOrElseOfSomeObservable;

            public org.junit.runners.model.Statement firstOrElseWithPredicateOfNoneMatchingThePredicateObservable;

            public org.junit.runners.model.Statement firstOrElseWithPredicateOfSomeObservable;

            public org.junit.runners.model.Statement firstObservable;

            public org.junit.runners.model.Statement firstWithOneElementObservable;

            public org.junit.runners.model.Statement firstWithEmptyObservable;

            public org.junit.runners.model.Statement firstWithPredicateObservable;

            public org.junit.runners.model.Statement firstWithPredicateAndOneElementObservable;

            public org.junit.runners.model.Statement firstWithPredicateAndEmptyObservable;

            public org.junit.runners.model.Statement firstOrDefaultObservable;

            public org.junit.runners.model.Statement firstOrDefaultWithOneElementObservable;

            public org.junit.runners.model.Statement firstOrDefaultWithEmptyObservable;

            public org.junit.runners.model.Statement firstOrDefaultWithPredicateObservable;

            public org.junit.runners.model.Statement firstOrDefaultWithPredicateAndOneElementObservable;

            public org.junit.runners.model.Statement firstOrDefaultWithPredicateAndEmptyObservable;

            public org.junit.runners.model.Statement firstOrElseOfNone;

            public org.junit.runners.model.Statement firstOrElseOfSome;

            public org.junit.runners.model.Statement firstOrElseWithPredicateOfNoneMatchingThePredicate;

            public org.junit.runners.model.Statement firstOrElseWithPredicateOfSome;

            public org.junit.runners.model.Statement first;

            public org.junit.runners.model.Statement firstWithOneElement;

            public org.junit.runners.model.Statement firstWithEmpty;

            public org.junit.runners.model.Statement firstWithPredicate;

            public org.junit.runners.model.Statement firstWithPredicateAndOneElement;

            public org.junit.runners.model.Statement firstWithPredicateAndEmpty;

            public org.junit.runners.model.Statement firstOrDefault;

            public org.junit.runners.model.Statement firstOrDefaultWithOneElement;

            public org.junit.runners.model.Statement firstOrDefaultWithEmpty;

            public org.junit.runners.model.Statement firstOrDefaultWithPredicate;

            public org.junit.runners.model.Statement firstOrDefaultWithPredicateAndOneElement;

            public org.junit.runners.model.Statement firstOrDefaultWithPredicateAndEmpty;

            public org.junit.runners.model.Statement firstOrErrorNoElement;

            public org.junit.runners.model.Statement firstOrErrorOneElement;

            public org.junit.runners.model.Statement firstOrErrorMultipleElements;

            public org.junit.runners.model.Statement firstOrErrorError;

            public org.junit.runners.model.Statement firstOrErrorNoElementObservable;

            public org.junit.runners.model.Statement firstOrErrorOneElementObservable;

            public org.junit.runners.model.Statement firstOrErrorMultipleElementsObservable;

            public org.junit.runners.model.Statement firstOrErrorErrorObservable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.firstOrElseOfNoneObservable = _ClassStatement.forPayload(ObservableFirstTest::firstOrElseOfNoneObservable, "firstOrElseOfNoneObservable", this);
            this.payloads.firstOrElseOfSomeObservable = _ClassStatement.forPayload(ObservableFirstTest::firstOrElseOfSomeObservable, "firstOrElseOfSomeObservable", this);
            this.payloads.firstOrElseWithPredicateOfNoneMatchingThePredicateObservable = _ClassStatement.forPayload(ObservableFirstTest::firstOrElseWithPredicateOfNoneMatchingThePredicateObservable, "firstOrElseWithPredicateOfNoneMatchingThePredicateObservable", this);
            this.payloads.firstOrElseWithPredicateOfSomeObservable = _ClassStatement.forPayload(ObservableFirstTest::firstOrElseWithPredicateOfSomeObservable, "firstOrElseWithPredicateOfSomeObservable", this);
            this.payloads.firstObservable = _ClassStatement.forPayload(ObservableFirstTest::firstObservable, "firstObservable", this);
            this.payloads.firstWithOneElementObservable = _ClassStatement.forPayload(ObservableFirstTest::firstWithOneElementObservable, "firstWithOneElementObservable", this);
            this.payloads.firstWithEmptyObservable = _ClassStatement.forPayload(ObservableFirstTest::firstWithEmptyObservable, "firstWithEmptyObservable", this);
            this.payloads.firstWithPredicateObservable = _ClassStatement.forPayload(ObservableFirstTest::firstWithPredicateObservable, "firstWithPredicateObservable", this);
            this.payloads.firstWithPredicateAndOneElementObservable = _ClassStatement.forPayload(ObservableFirstTest::firstWithPredicateAndOneElementObservable, "firstWithPredicateAndOneElementObservable", this);
            this.payloads.firstWithPredicateAndEmptyObservable = _ClassStatement.forPayload(ObservableFirstTest::firstWithPredicateAndEmptyObservable, "firstWithPredicateAndEmptyObservable", this);
            this.payloads.firstOrDefaultObservable = _ClassStatement.forPayload(ObservableFirstTest::firstOrDefaultObservable, "firstOrDefaultObservable", this);
            this.payloads.firstOrDefaultWithOneElementObservable = _ClassStatement.forPayload(ObservableFirstTest::firstOrDefaultWithOneElementObservable, "firstOrDefaultWithOneElementObservable", this);
            this.payloads.firstOrDefaultWithEmptyObservable = _ClassStatement.forPayload(ObservableFirstTest::firstOrDefaultWithEmptyObservable, "firstOrDefaultWithEmptyObservable", this);
            this.payloads.firstOrDefaultWithPredicateObservable = _ClassStatement.forPayload(ObservableFirstTest::firstOrDefaultWithPredicateObservable, "firstOrDefaultWithPredicateObservable", this);
            this.payloads.firstOrDefaultWithPredicateAndOneElementObservable = _ClassStatement.forPayload(ObservableFirstTest::firstOrDefaultWithPredicateAndOneElementObservable, "firstOrDefaultWithPredicateAndOneElementObservable", this);
            this.payloads.firstOrDefaultWithPredicateAndEmptyObservable = _ClassStatement.forPayload(ObservableFirstTest::firstOrDefaultWithPredicateAndEmptyObservable, "firstOrDefaultWithPredicateAndEmptyObservable", this);
            this.payloads.firstOrElseOfNone = _ClassStatement.forPayload(ObservableFirstTest::firstOrElseOfNone, "firstOrElseOfNone", this);
            this.payloads.firstOrElseOfSome = _ClassStatement.forPayload(ObservableFirstTest::firstOrElseOfSome, "firstOrElseOfSome", this);
            this.payloads.firstOrElseWithPredicateOfNoneMatchingThePredicate = _ClassStatement.forPayload(ObservableFirstTest::firstOrElseWithPredicateOfNoneMatchingThePredicate, "firstOrElseWithPredicateOfNoneMatchingThePredicate", this);
            this.payloads.firstOrElseWithPredicateOfSome = _ClassStatement.forPayload(ObservableFirstTest::firstOrElseWithPredicateOfSome, "firstOrElseWithPredicateOfSome", this);
            this.payloads.first = _ClassStatement.forPayload(ObservableFirstTest::first, "first", this);
            this.payloads.firstWithOneElement = _ClassStatement.forPayload(ObservableFirstTest::firstWithOneElement, "firstWithOneElement", this);
            this.payloads.firstWithEmpty = _ClassStatement.forPayload(ObservableFirstTest::firstWithEmpty, "firstWithEmpty", this);
            this.payloads.firstWithPredicate = _ClassStatement.forPayload(ObservableFirstTest::firstWithPredicate, "firstWithPredicate", this);
            this.payloads.firstWithPredicateAndOneElement = _ClassStatement.forPayload(ObservableFirstTest::firstWithPredicateAndOneElement, "firstWithPredicateAndOneElement", this);
            this.payloads.firstWithPredicateAndEmpty = _ClassStatement.forPayload(ObservableFirstTest::firstWithPredicateAndEmpty, "firstWithPredicateAndEmpty", this);
            this.payloads.firstOrDefault = _ClassStatement.forPayload(ObservableFirstTest::firstOrDefault, "firstOrDefault", this);
            this.payloads.firstOrDefaultWithOneElement = _ClassStatement.forPayload(ObservableFirstTest::firstOrDefaultWithOneElement, "firstOrDefaultWithOneElement", this);
            this.payloads.firstOrDefaultWithEmpty = _ClassStatement.forPayload(ObservableFirstTest::firstOrDefaultWithEmpty, "firstOrDefaultWithEmpty", this);
            this.payloads.firstOrDefaultWithPredicate = _ClassStatement.forPayload(ObservableFirstTest::firstOrDefaultWithPredicate, "firstOrDefaultWithPredicate", this);
            this.payloads.firstOrDefaultWithPredicateAndOneElement = _ClassStatement.forPayload(ObservableFirstTest::firstOrDefaultWithPredicateAndOneElement, "firstOrDefaultWithPredicateAndOneElement", this);
            this.payloads.firstOrDefaultWithPredicateAndEmpty = _ClassStatement.forPayload(ObservableFirstTest::firstOrDefaultWithPredicateAndEmpty, "firstOrDefaultWithPredicateAndEmpty", this);
            this.payloads.firstOrErrorNoElement = _ClassStatement.forPayload(ObservableFirstTest::firstOrErrorNoElement, "firstOrErrorNoElement", this);
            this.payloads.firstOrErrorOneElement = _ClassStatement.forPayload(ObservableFirstTest::firstOrErrorOneElement, "firstOrErrorOneElement", this);
            this.payloads.firstOrErrorMultipleElements = _ClassStatement.forPayload(ObservableFirstTest::firstOrErrorMultipleElements, "firstOrErrorMultipleElements", this);
            this.payloads.firstOrErrorError = _ClassStatement.forPayload(ObservableFirstTest::firstOrErrorError, "firstOrErrorError", this);
            this.payloads.firstOrErrorNoElementObservable = _ClassStatement.forPayload(ObservableFirstTest::firstOrErrorNoElementObservable, "firstOrErrorNoElementObservable", this);
            this.payloads.firstOrErrorOneElementObservable = _ClassStatement.forPayload(ObservableFirstTest::firstOrErrorOneElementObservable, "firstOrErrorOneElementObservable", this);
            this.payloads.firstOrErrorMultipleElementsObservable = _ClassStatement.forPayload(ObservableFirstTest::firstOrErrorMultipleElementsObservable, "firstOrErrorMultipleElementsObservable", this);
            this.payloads.firstOrErrorErrorObservable = _ClassStatement.forPayload(ObservableFirstTest::firstOrErrorErrorObservable, "firstOrErrorErrorObservable", this);
        }
    }
}
