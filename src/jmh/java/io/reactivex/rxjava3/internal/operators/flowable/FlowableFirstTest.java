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
package io.reactivex.rxjava3.internal.operators.flowable;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.NoSuchElementException;
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableFirstTest extends RxJavaTest {

    Subscriber<String> w;

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
        w = TestHelper.mockSubscriber();
        wo = TestHelper.mockSingleObserver();
        wm = TestHelper.mockMaybeObserver();
    }

    @Test
    public void firstOrElseOfNoneFlowable() {
        Flowable<String> src = Flowable.empty();
        src.first("default").toFlowable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("default");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstOrElseOfSomeFlowable() {
        Flowable<String> src = Flowable.just("a", "b", "c");
        src.first("default").toFlowable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("a");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstOrElseWithPredicateOfNoneMatchingThePredicateFlowable() {
        Flowable<String> src = Flowable.just("a", "b", "c");
        src.filter(IS_D).first("default").toFlowable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("default");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstOrElseWithPredicateOfSomeFlowable() {
        Flowable<String> src = Flowable.just("a", "b", "c", "d", "e", "f");
        src.filter(IS_D).first("default").toFlowable().subscribe(w);
        verify(w, times(1)).onNext(anyString());
        verify(w, times(1)).onNext("d");
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void firstFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3).firstElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithOneElementFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).firstElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.<Integer>empty().firstElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onComplete();
        inOrder.verify(subscriber, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndOneElementFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onComplete();
        inOrder.verify(subscriber, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3).first(4).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithOneElementFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).first(2).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.<Integer>empty().first(1).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(8).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndOneElementFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(4).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(2).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrElseOfNone() {
        Flowable<String> src = Flowable.empty();
        src.first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("default");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstOrElseOfSome() {
        Flowable<String> src = Flowable.just("a", "b", "c");
        src.first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("a");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstOrElseWithPredicateOfNoneMatchingThePredicate() {
        Flowable<String> src = Flowable.just("a", "b", "c");
        src.filter(IS_D).first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("default");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void firstOrElseWithPredicateOfSome() {
        Flowable<String> src = Flowable.just("a", "b", "c", "d", "e", "f");
        src.filter(IS_D).first("default").subscribe(wo);
        verify(wo, times(1)).onSuccess(anyString());
        verify(wo, times(1)).onSuccess("d");
        verify(wo, never()).onError(any(Throwable.class));
    }

    @Test
    public void first() {
        Maybe<Integer> maybe = Flowable.just(1, 2, 3).firstElement();
        maybe.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithOneElement() {
        Maybe<Integer> maybe = Flowable.just(1).firstElement();
        maybe.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithEmpty() {
        Maybe<Integer> maybe = Flowable.<Integer>empty().firstElement();
        maybe.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm).onComplete();
        inOrder.verify(wm, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicate() {
        Maybe<Integer> maybe = Flowable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement();
        maybe.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndOneElement() {
        Maybe<Integer> maybe = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement();
        maybe.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstWithPredicateAndEmpty() {
        Maybe<Integer> maybe = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).firstElement();
        maybe.subscribe(wm);
        InOrder inOrder = inOrder(wm);
        inOrder.verify(wm).onComplete();
        inOrder.verify(wm, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefault() {
        Single<Integer> single = Flowable.just(1, 2, 3).first(4);
        single.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithOneElement() {
        Single<Integer> single = Flowable.just(1).first(2);
        single.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithEmpty() {
        Single<Integer> single = Flowable.<Integer>empty().first(1);
        single.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicate() {
        Single<Integer> single = Flowable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(8);
        single.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndOneElement() {
        Single<Integer> single = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(4);
        single.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrDefaultWithPredicateAndEmpty() {
        Single<Integer> single = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).first(2);
        single.subscribe(wo);
        InOrder inOrder = inOrder(wo);
        inOrder.verify(wo, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstOrErrorNoElement() {
        Flowable.empty().firstOrError().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void firstOrErrorOneElement() {
        Flowable.just(1).firstOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorMultipleElements() {
        Flowable.just(1, 2, 3).firstOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorError() {
        Flowable.error(new RuntimeException("error")).firstOrError().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void firstOrErrorNoElementFlowable() {
        Flowable.empty().firstOrError().toFlowable().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void firstOrErrorOneElementFlowable() {
        Flowable.just(1).firstOrError().toFlowable().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorMultipleElementsFlowable() {
        Flowable.just(1, 2, 3).firstOrError().toFlowable().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void firstOrErrorErrorFlowable() {
        Flowable.error(new RuntimeException("error")).firstOrError().toFlowable().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableFirstTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseOfNoneFlowable() throws java.lang.Throwable {
            this.payloads.firstOrElseOfNoneFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseOfSomeFlowable() throws java.lang.Throwable {
            this.payloads.firstOrElseOfSomeFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseWithPredicateOfNoneMatchingThePredicateFlowable() throws java.lang.Throwable {
            this.payloads.firstOrElseWithPredicateOfNoneMatchingThePredicateFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrElseWithPredicateOfSomeFlowable() throws java.lang.Throwable {
            this.payloads.firstOrElseWithPredicateOfSomeFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstFlowable() throws java.lang.Throwable {
            this.payloads.firstFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithOneElementFlowable() throws java.lang.Throwable {
            this.payloads.firstWithOneElementFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithEmptyFlowable() throws java.lang.Throwable {
            this.payloads.firstWithEmptyFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateFlowable() throws java.lang.Throwable {
            this.payloads.firstWithPredicateFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateAndOneElementFlowable() throws java.lang.Throwable {
            this.payloads.firstWithPredicateAndOneElementFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstWithPredicateAndEmptyFlowable() throws java.lang.Throwable {
            this.payloads.firstWithPredicateAndEmptyFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultFlowable() throws java.lang.Throwable {
            this.payloads.firstOrDefaultFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithOneElementFlowable() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithOneElementFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithEmptyFlowable() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithEmptyFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateFlowable() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithPredicateFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateAndOneElementFlowable() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithPredicateAndOneElementFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrDefaultWithPredicateAndEmptyFlowable() throws java.lang.Throwable {
            this.payloads.firstOrDefaultWithPredicateAndEmptyFlowable.evaluate();
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
        public void benchmark_firstOrErrorNoElementFlowable() throws java.lang.Throwable {
            this.payloads.firstOrErrorNoElementFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorOneElementFlowable() throws java.lang.Throwable {
            this.payloads.firstOrErrorOneElementFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorMultipleElementsFlowable() throws java.lang.Throwable {
            this.payloads.firstOrErrorMultipleElementsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOrErrorErrorFlowable() throws java.lang.Throwable {
            this.payloads.firstOrErrorErrorFlowable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFirstTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFirstTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFirstTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFirstTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableFirstTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFirstTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableFirstTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableFirstTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement firstOrElseOfNoneFlowable;

            public org.junit.runners.model.Statement firstOrElseOfSomeFlowable;

            public org.junit.runners.model.Statement firstOrElseWithPredicateOfNoneMatchingThePredicateFlowable;

            public org.junit.runners.model.Statement firstOrElseWithPredicateOfSomeFlowable;

            public org.junit.runners.model.Statement firstFlowable;

            public org.junit.runners.model.Statement firstWithOneElementFlowable;

            public org.junit.runners.model.Statement firstWithEmptyFlowable;

            public org.junit.runners.model.Statement firstWithPredicateFlowable;

            public org.junit.runners.model.Statement firstWithPredicateAndOneElementFlowable;

            public org.junit.runners.model.Statement firstWithPredicateAndEmptyFlowable;

            public org.junit.runners.model.Statement firstOrDefaultFlowable;

            public org.junit.runners.model.Statement firstOrDefaultWithOneElementFlowable;

            public org.junit.runners.model.Statement firstOrDefaultWithEmptyFlowable;

            public org.junit.runners.model.Statement firstOrDefaultWithPredicateFlowable;

            public org.junit.runners.model.Statement firstOrDefaultWithPredicateAndOneElementFlowable;

            public org.junit.runners.model.Statement firstOrDefaultWithPredicateAndEmptyFlowable;

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

            public org.junit.runners.model.Statement firstOrErrorNoElementFlowable;

            public org.junit.runners.model.Statement firstOrErrorOneElementFlowable;

            public org.junit.runners.model.Statement firstOrErrorMultipleElementsFlowable;

            public org.junit.runners.model.Statement firstOrErrorErrorFlowable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.firstOrElseOfNoneFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstOrElseOfNoneFlowable, "firstOrElseOfNoneFlowable", this);
            this.payloads.firstOrElseOfSomeFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstOrElseOfSomeFlowable, "firstOrElseOfSomeFlowable", this);
            this.payloads.firstOrElseWithPredicateOfNoneMatchingThePredicateFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstOrElseWithPredicateOfNoneMatchingThePredicateFlowable, "firstOrElseWithPredicateOfNoneMatchingThePredicateFlowable", this);
            this.payloads.firstOrElseWithPredicateOfSomeFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstOrElseWithPredicateOfSomeFlowable, "firstOrElseWithPredicateOfSomeFlowable", this);
            this.payloads.firstFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstFlowable, "firstFlowable", this);
            this.payloads.firstWithOneElementFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstWithOneElementFlowable, "firstWithOneElementFlowable", this);
            this.payloads.firstWithEmptyFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstWithEmptyFlowable, "firstWithEmptyFlowable", this);
            this.payloads.firstWithPredicateFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstWithPredicateFlowable, "firstWithPredicateFlowable", this);
            this.payloads.firstWithPredicateAndOneElementFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstWithPredicateAndOneElementFlowable, "firstWithPredicateAndOneElementFlowable", this);
            this.payloads.firstWithPredicateAndEmptyFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstWithPredicateAndEmptyFlowable, "firstWithPredicateAndEmptyFlowable", this);
            this.payloads.firstOrDefaultFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstOrDefaultFlowable, "firstOrDefaultFlowable", this);
            this.payloads.firstOrDefaultWithOneElementFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstOrDefaultWithOneElementFlowable, "firstOrDefaultWithOneElementFlowable", this);
            this.payloads.firstOrDefaultWithEmptyFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstOrDefaultWithEmptyFlowable, "firstOrDefaultWithEmptyFlowable", this);
            this.payloads.firstOrDefaultWithPredicateFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstOrDefaultWithPredicateFlowable, "firstOrDefaultWithPredicateFlowable", this);
            this.payloads.firstOrDefaultWithPredicateAndOneElementFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstOrDefaultWithPredicateAndOneElementFlowable, "firstOrDefaultWithPredicateAndOneElementFlowable", this);
            this.payloads.firstOrDefaultWithPredicateAndEmptyFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstOrDefaultWithPredicateAndEmptyFlowable, "firstOrDefaultWithPredicateAndEmptyFlowable", this);
            this.payloads.firstOrElseOfNone = _ClassStatement.forPayload(FlowableFirstTest::firstOrElseOfNone, "firstOrElseOfNone", this);
            this.payloads.firstOrElseOfSome = _ClassStatement.forPayload(FlowableFirstTest::firstOrElseOfSome, "firstOrElseOfSome", this);
            this.payloads.firstOrElseWithPredicateOfNoneMatchingThePredicate = _ClassStatement.forPayload(FlowableFirstTest::firstOrElseWithPredicateOfNoneMatchingThePredicate, "firstOrElseWithPredicateOfNoneMatchingThePredicate", this);
            this.payloads.firstOrElseWithPredicateOfSome = _ClassStatement.forPayload(FlowableFirstTest::firstOrElseWithPredicateOfSome, "firstOrElseWithPredicateOfSome", this);
            this.payloads.first = _ClassStatement.forPayload(FlowableFirstTest::first, "first", this);
            this.payloads.firstWithOneElement = _ClassStatement.forPayload(FlowableFirstTest::firstWithOneElement, "firstWithOneElement", this);
            this.payloads.firstWithEmpty = _ClassStatement.forPayload(FlowableFirstTest::firstWithEmpty, "firstWithEmpty", this);
            this.payloads.firstWithPredicate = _ClassStatement.forPayload(FlowableFirstTest::firstWithPredicate, "firstWithPredicate", this);
            this.payloads.firstWithPredicateAndOneElement = _ClassStatement.forPayload(FlowableFirstTest::firstWithPredicateAndOneElement, "firstWithPredicateAndOneElement", this);
            this.payloads.firstWithPredicateAndEmpty = _ClassStatement.forPayload(FlowableFirstTest::firstWithPredicateAndEmpty, "firstWithPredicateAndEmpty", this);
            this.payloads.firstOrDefault = _ClassStatement.forPayload(FlowableFirstTest::firstOrDefault, "firstOrDefault", this);
            this.payloads.firstOrDefaultWithOneElement = _ClassStatement.forPayload(FlowableFirstTest::firstOrDefaultWithOneElement, "firstOrDefaultWithOneElement", this);
            this.payloads.firstOrDefaultWithEmpty = _ClassStatement.forPayload(FlowableFirstTest::firstOrDefaultWithEmpty, "firstOrDefaultWithEmpty", this);
            this.payloads.firstOrDefaultWithPredicate = _ClassStatement.forPayload(FlowableFirstTest::firstOrDefaultWithPredicate, "firstOrDefaultWithPredicate", this);
            this.payloads.firstOrDefaultWithPredicateAndOneElement = _ClassStatement.forPayload(FlowableFirstTest::firstOrDefaultWithPredicateAndOneElement, "firstOrDefaultWithPredicateAndOneElement", this);
            this.payloads.firstOrDefaultWithPredicateAndEmpty = _ClassStatement.forPayload(FlowableFirstTest::firstOrDefaultWithPredicateAndEmpty, "firstOrDefaultWithPredicateAndEmpty", this);
            this.payloads.firstOrErrorNoElement = _ClassStatement.forPayload(FlowableFirstTest::firstOrErrorNoElement, "firstOrErrorNoElement", this);
            this.payloads.firstOrErrorOneElement = _ClassStatement.forPayload(FlowableFirstTest::firstOrErrorOneElement, "firstOrErrorOneElement", this);
            this.payloads.firstOrErrorMultipleElements = _ClassStatement.forPayload(FlowableFirstTest::firstOrErrorMultipleElements, "firstOrErrorMultipleElements", this);
            this.payloads.firstOrErrorError = _ClassStatement.forPayload(FlowableFirstTest::firstOrErrorError, "firstOrErrorError", this);
            this.payloads.firstOrErrorNoElementFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstOrErrorNoElementFlowable, "firstOrErrorNoElementFlowable", this);
            this.payloads.firstOrErrorOneElementFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstOrErrorOneElementFlowable, "firstOrErrorOneElementFlowable", this);
            this.payloads.firstOrErrorMultipleElementsFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstOrErrorMultipleElementsFlowable, "firstOrErrorMultipleElementsFlowable", this);
            this.payloads.firstOrErrorErrorFlowable = _ClassStatement.forPayload(FlowableFirstTest::firstOrErrorErrorFlowable, "firstOrErrorErrorFlowable", this);
        }
    }
}
