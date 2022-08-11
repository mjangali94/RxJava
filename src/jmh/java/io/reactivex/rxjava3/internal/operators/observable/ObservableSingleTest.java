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
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableSingleTest extends RxJavaTest {

    @Test
    public void singleObservable() {
        Observable<Integer> o = Observable.just(1).singleElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithTooManyElementsObservable() {
        Observable<Integer> o = Observable.just(1, 2).singleElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithEmptyObservable() {
        Observable<Integer> o = Observable.<Integer>empty().singleElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateObservable() {
        Observable<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndTooManyElementsObservable() {
        Observable<Integer> o = Observable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndEmptyObservable() {
        Observable<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement().toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultObservable() {
        Observable<Integer> o = Observable.just(1).single(2).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithTooManyElementsObservable() {
        Observable<Integer> o = Observable.just(1, 2).single(3).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithEmptyObservable() {
        Observable<Integer> o = Observable.<Integer>empty().single(1).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(1);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateObservable() {
        Observable<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(4).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndTooManyElementsObservable() {
        Observable<Integer> o = Observable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(6).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndEmptyObservable() {
        Observable<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(2).toObservable();
        Observer<Integer> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(2);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void issue1527Observable() throws InterruptedException {
        // https://github.com/ReactiveX/RxJava/pull/1527
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Observable<Integer> reduced = source.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer i1, Integer i2) {
                return i1 + i2;
            }
        }).toObservable();
        Integer r = reduced.blockingFirst();
        assertEquals(21, r.intValue());
    }

    @Test
    public void single() {
        Maybe<Integer> o = Observable.just(1).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithTooManyElements() {
        Maybe<Integer> o = Observable.just(1, 2).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithEmpty() {
        Maybe<Integer> o = Observable.<Integer>empty().singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicate() {
        Maybe<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndTooManyElements() {
        Maybe<Integer> o = Observable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndEmpty() {
        Maybe<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefault() {
        Single<Integer> o = Observable.just(1).single(2);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithTooManyElements() {
        Single<Integer> o = Observable.just(1, 2).single(3);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithEmpty() {
        Single<Integer> o = Observable.<Integer>empty().single(1);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicate() {
        Single<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(4);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndTooManyElements() {
        Single<Integer> o = Observable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(6);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndEmpty() {
        Single<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(2);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void issue1527() throws InterruptedException {
        // https://github.com/ReactiveX/RxJava/pull/1527
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Maybe<Integer> reduced = source.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });
        Integer r = reduced.blockingGet();
        assertEquals(21, r.intValue());
    }

    @Test
    public void singleElementOperatorDoNotSwallowExceptionWhenDone() {
        final Throwable exception = new RuntimeException("some error");
        final AtomicReference<Throwable> error = new AtomicReference<>();
        try {
            RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {

                @Override
                public void accept(final Throwable throwable) throws Exception {
                    error.set(throwable);
                }
            });
            Observable.unsafeCreate(new ObservableSource<Integer>() {

                @Override
                public void subscribe(final Observer<? super Integer> observer) {
                    observer.onComplete();
                    observer.onError(exception);
                }
            }).singleElement().test().assertComplete();
            assertSame(exception, error.get().getCause());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void singleOrErrorNoElement() {
        Observable.empty().singleOrError().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void singleOrErrorOneElement() {
        Observable.just(1).singleOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void singleOrErrorMultipleElements() {
        Observable.just(1, 2, 3).singleOrError().test().assertNoValues().assertError(IllegalArgumentException.class);
    }

    @Test
    public void singleOrErrorError() {
        Observable.error(new RuntimeException("error")).singleOrError().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceObservable(new Function<Observable<Object>, Object>() {

            @Override
            public Object apply(Observable<Object> o) throws Exception {
                return o.singleOrError();
            }
        }, false, 1, 1, 1);
        TestHelper.checkBadSourceObservable(new Function<Observable<Object>, Object>() {

            @Override
            public Object apply(Observable<Object> o) throws Exception {
                return o.singleElement();
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToSingle(new Function<Observable<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Observable<Object> o) throws Exception {
                return o.singleOrError();
            }
        });
        TestHelper.checkDoubleOnSubscribeObservableToMaybe(new Function<Observable<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Observable<Object> o) throws Exception {
                return o.singleElement();
            }
        });
    }

    @Test
    public void singleOrError() {
        Observable.empty().singleOrError().toObservable().test().assertFailure(NoSuchElementException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableSingleTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleObservable() throws java.lang.Throwable {
            this.payloads.singleObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithTooManyElementsObservable() throws java.lang.Throwable {
            this.payloads.singleWithTooManyElementsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithEmptyObservable() throws java.lang.Throwable {
            this.payloads.singleWithEmptyObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateObservable() throws java.lang.Throwable {
            this.payloads.singleWithPredicateObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndTooManyElementsObservable() throws java.lang.Throwable {
            this.payloads.singleWithPredicateAndTooManyElementsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndEmptyObservable() throws java.lang.Throwable {
            this.payloads.singleWithPredicateAndEmptyObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultObservable() throws java.lang.Throwable {
            this.payloads.singleOrDefaultObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithTooManyElementsObservable() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithTooManyElementsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithEmptyObservable() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithEmptyObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateObservable() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithPredicateObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndTooManyElementsObservable() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithPredicateAndTooManyElementsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndEmptyObservable() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithPredicateAndEmptyObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1527Observable() throws java.lang.Throwable {
            this.payloads.issue1527Observable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_single() throws java.lang.Throwable {
            this.payloads.single.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithTooManyElements() throws java.lang.Throwable {
            this.payloads.singleWithTooManyElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithEmpty() throws java.lang.Throwable {
            this.payloads.singleWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicate() throws java.lang.Throwable {
            this.payloads.singleWithPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndTooManyElements() throws java.lang.Throwable {
            this.payloads.singleWithPredicateAndTooManyElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndEmpty() throws java.lang.Throwable {
            this.payloads.singleWithPredicateAndEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefault() throws java.lang.Throwable {
            this.payloads.singleOrDefault.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithTooManyElements() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithTooManyElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithEmpty() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicate() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndTooManyElements() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithPredicateAndTooManyElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndEmpty() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithPredicateAndEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1527() throws java.lang.Throwable {
            this.payloads.issue1527.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleElementOperatorDoNotSwallowExceptionWhenDone() throws java.lang.Throwable {
            this.payloads.singleElementOperatorDoNotSwallowExceptionWhenDone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorNoElement() throws java.lang.Throwable {
            this.payloads.singleOrErrorNoElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorOneElement() throws java.lang.Throwable {
            this.payloads.singleOrErrorOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorMultipleElements() throws java.lang.Throwable {
            this.payloads.singleOrErrorMultipleElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorError() throws java.lang.Throwable {
            this.payloads.singleOrErrorError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrError() throws java.lang.Throwable {
            this.payloads.singleOrError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSingleTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSingleTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSingleTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSingleTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableSingleTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSingleTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableSingleTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableSingleTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement singleObservable;

            public org.junit.runners.model.Statement singleWithTooManyElementsObservable;

            public org.junit.runners.model.Statement singleWithEmptyObservable;

            public org.junit.runners.model.Statement singleWithPredicateObservable;

            public org.junit.runners.model.Statement singleWithPredicateAndTooManyElementsObservable;

            public org.junit.runners.model.Statement singleWithPredicateAndEmptyObservable;

            public org.junit.runners.model.Statement singleOrDefaultObservable;

            public org.junit.runners.model.Statement singleOrDefaultWithTooManyElementsObservable;

            public org.junit.runners.model.Statement singleOrDefaultWithEmptyObservable;

            public org.junit.runners.model.Statement singleOrDefaultWithPredicateObservable;

            public org.junit.runners.model.Statement singleOrDefaultWithPredicateAndTooManyElementsObservable;

            public org.junit.runners.model.Statement singleOrDefaultWithPredicateAndEmptyObservable;

            public org.junit.runners.model.Statement issue1527Observable;

            public org.junit.runners.model.Statement single;

            public org.junit.runners.model.Statement singleWithTooManyElements;

            public org.junit.runners.model.Statement singleWithEmpty;

            public org.junit.runners.model.Statement singleWithPredicate;

            public org.junit.runners.model.Statement singleWithPredicateAndTooManyElements;

            public org.junit.runners.model.Statement singleWithPredicateAndEmpty;

            public org.junit.runners.model.Statement singleOrDefault;

            public org.junit.runners.model.Statement singleOrDefaultWithTooManyElements;

            public org.junit.runners.model.Statement singleOrDefaultWithEmpty;

            public org.junit.runners.model.Statement singleOrDefaultWithPredicate;

            public org.junit.runners.model.Statement singleOrDefaultWithPredicateAndTooManyElements;

            public org.junit.runners.model.Statement singleOrDefaultWithPredicateAndEmpty;

            public org.junit.runners.model.Statement issue1527;

            public org.junit.runners.model.Statement singleElementOperatorDoNotSwallowExceptionWhenDone;

            public org.junit.runners.model.Statement singleOrErrorNoElement;

            public org.junit.runners.model.Statement singleOrErrorOneElement;

            public org.junit.runners.model.Statement singleOrErrorMultipleElements;

            public org.junit.runners.model.Statement singleOrErrorError;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement singleOrError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.singleObservable = _ClassStatement.forPayload(ObservableSingleTest::singleObservable, "singleObservable", this);
            this.payloads.singleWithTooManyElementsObservable = _ClassStatement.forPayload(ObservableSingleTest::singleWithTooManyElementsObservable, "singleWithTooManyElementsObservable", this);
            this.payloads.singleWithEmptyObservable = _ClassStatement.forPayload(ObservableSingleTest::singleWithEmptyObservable, "singleWithEmptyObservable", this);
            this.payloads.singleWithPredicateObservable = _ClassStatement.forPayload(ObservableSingleTest::singleWithPredicateObservable, "singleWithPredicateObservable", this);
            this.payloads.singleWithPredicateAndTooManyElementsObservable = _ClassStatement.forPayload(ObservableSingleTest::singleWithPredicateAndTooManyElementsObservable, "singleWithPredicateAndTooManyElementsObservable", this);
            this.payloads.singleWithPredicateAndEmptyObservable = _ClassStatement.forPayload(ObservableSingleTest::singleWithPredicateAndEmptyObservable, "singleWithPredicateAndEmptyObservable", this);
            this.payloads.singleOrDefaultObservable = _ClassStatement.forPayload(ObservableSingleTest::singleOrDefaultObservable, "singleOrDefaultObservable", this);
            this.payloads.singleOrDefaultWithTooManyElementsObservable = _ClassStatement.forPayload(ObservableSingleTest::singleOrDefaultWithTooManyElementsObservable, "singleOrDefaultWithTooManyElementsObservable", this);
            this.payloads.singleOrDefaultWithEmptyObservable = _ClassStatement.forPayload(ObservableSingleTest::singleOrDefaultWithEmptyObservable, "singleOrDefaultWithEmptyObservable", this);
            this.payloads.singleOrDefaultWithPredicateObservable = _ClassStatement.forPayload(ObservableSingleTest::singleOrDefaultWithPredicateObservable, "singleOrDefaultWithPredicateObservable", this);
            this.payloads.singleOrDefaultWithPredicateAndTooManyElementsObservable = _ClassStatement.forPayload(ObservableSingleTest::singleOrDefaultWithPredicateAndTooManyElementsObservable, "singleOrDefaultWithPredicateAndTooManyElementsObservable", this);
            this.payloads.singleOrDefaultWithPredicateAndEmptyObservable = _ClassStatement.forPayload(ObservableSingleTest::singleOrDefaultWithPredicateAndEmptyObservable, "singleOrDefaultWithPredicateAndEmptyObservable", this);
            this.payloads.issue1527Observable = _ClassStatement.forPayload(ObservableSingleTest::issue1527Observable, "issue1527Observable", this);
            this.payloads.single = _ClassStatement.forPayload(ObservableSingleTest::single, "single", this);
            this.payloads.singleWithTooManyElements = _ClassStatement.forPayload(ObservableSingleTest::singleWithTooManyElements, "singleWithTooManyElements", this);
            this.payloads.singleWithEmpty = _ClassStatement.forPayload(ObservableSingleTest::singleWithEmpty, "singleWithEmpty", this);
            this.payloads.singleWithPredicate = _ClassStatement.forPayload(ObservableSingleTest::singleWithPredicate, "singleWithPredicate", this);
            this.payloads.singleWithPredicateAndTooManyElements = _ClassStatement.forPayload(ObservableSingleTest::singleWithPredicateAndTooManyElements, "singleWithPredicateAndTooManyElements", this);
            this.payloads.singleWithPredicateAndEmpty = _ClassStatement.forPayload(ObservableSingleTest::singleWithPredicateAndEmpty, "singleWithPredicateAndEmpty", this);
            this.payloads.singleOrDefault = _ClassStatement.forPayload(ObservableSingleTest::singleOrDefault, "singleOrDefault", this);
            this.payloads.singleOrDefaultWithTooManyElements = _ClassStatement.forPayload(ObservableSingleTest::singleOrDefaultWithTooManyElements, "singleOrDefaultWithTooManyElements", this);
            this.payloads.singleOrDefaultWithEmpty = _ClassStatement.forPayload(ObservableSingleTest::singleOrDefaultWithEmpty, "singleOrDefaultWithEmpty", this);
            this.payloads.singleOrDefaultWithPredicate = _ClassStatement.forPayload(ObservableSingleTest::singleOrDefaultWithPredicate, "singleOrDefaultWithPredicate", this);
            this.payloads.singleOrDefaultWithPredicateAndTooManyElements = _ClassStatement.forPayload(ObservableSingleTest::singleOrDefaultWithPredicateAndTooManyElements, "singleOrDefaultWithPredicateAndTooManyElements", this);
            this.payloads.singleOrDefaultWithPredicateAndEmpty = _ClassStatement.forPayload(ObservableSingleTest::singleOrDefaultWithPredicateAndEmpty, "singleOrDefaultWithPredicateAndEmpty", this);
            this.payloads.issue1527 = _ClassStatement.forPayload(ObservableSingleTest::issue1527, "issue1527", this);
            this.payloads.singleElementOperatorDoNotSwallowExceptionWhenDone = _ClassStatement.forPayload(ObservableSingleTest::singleElementOperatorDoNotSwallowExceptionWhenDone, "singleElementOperatorDoNotSwallowExceptionWhenDone", this);
            this.payloads.singleOrErrorNoElement = _ClassStatement.forPayload(ObservableSingleTest::singleOrErrorNoElement, "singleOrErrorNoElement", this);
            this.payloads.singleOrErrorOneElement = _ClassStatement.forPayload(ObservableSingleTest::singleOrErrorOneElement, "singleOrErrorOneElement", this);
            this.payloads.singleOrErrorMultipleElements = _ClassStatement.forPayload(ObservableSingleTest::singleOrErrorMultipleElements, "singleOrErrorMultipleElements", this);
            this.payloads.singleOrErrorError = _ClassStatement.forPayload(ObservableSingleTest::singleOrErrorError, "singleOrErrorError", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableSingleTest::badSource, "badSource", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableSingleTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.singleOrError = _ClassStatement.forPayload(ObservableSingleTest::singleOrError, "singleOrError", this);
        }
    }
}
