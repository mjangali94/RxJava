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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableAnyTest extends RxJavaTest {

    @Test
    public void anyWithTwoItemsObservable() {
        Observable<Integer> w = Observable.just(1, 2);
        Observable<Boolean> observable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).toObservable();
        Observer<Boolean> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, never()).onNext(false);
        verify(observer, times(1)).onNext(true);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void isEmptyWithTwoItemsObservable() {
        Observable<Integer> w = Observable.just(1, 2);
        Observable<Boolean> observable = w.isEmpty().toObservable();
        Observer<Boolean> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, never()).onNext(true);
        verify(observer, times(1)).onNext(false);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void anyWithOneItemObservable() {
        Observable<Integer> w = Observable.just(1);
        Observable<Boolean> observable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).toObservable();
        Observer<Boolean> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, never()).onNext(false);
        verify(observer, times(1)).onNext(true);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void isEmptyWithOneItemObservable() {
        Observable<Integer> w = Observable.just(1);
        Observable<Boolean> observable = w.isEmpty().toObservable();
        Observer<Boolean> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, never()).onNext(true);
        verify(observer, times(1)).onNext(false);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void anyWithEmptyObservable() {
        Observable<Integer> w = Observable.empty();
        Observable<Boolean> observable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).toObservable();
        Observer<Boolean> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, times(1)).onNext(false);
        verify(observer, never()).onNext(true);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void isEmptyWithEmptyObservable() {
        Observable<Integer> w = Observable.empty();
        Observable<Boolean> observable = w.isEmpty().toObservable();
        Observer<Boolean> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, times(1)).onNext(true);
        verify(observer, never()).onNext(false);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void anyWithPredicate1Observable() {
        Observable<Integer> w = Observable.just(1, 2, 3);
        Observable<Boolean> observable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 2;
            }
        }).toObservable();
        Observer<Boolean> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, never()).onNext(false);
        verify(observer, times(1)).onNext(true);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void exists1Observable() {
        Observable<Integer> w = Observable.just(1, 2, 3);
        Observable<Boolean> observable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 2;
            }
        }).toObservable();
        Observer<Boolean> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, never()).onNext(false);
        verify(observer, times(1)).onNext(true);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void anyWithPredicate2Observable() {
        Observable<Integer> w = Observable.just(1, 2, 3);
        Observable<Boolean> observable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 1;
            }
        }).toObservable();
        Observer<Boolean> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, times(1)).onNext(false);
        verify(observer, never()).onNext(true);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void anyWithEmptyAndPredicateObservable() {
        // If the source is empty, always output false.
        Observable<Integer> w = Observable.empty();
        Observable<Boolean> observable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t) {
                return true;
            }
        }).toObservable();
        Observer<Boolean> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, times(1)).onNext(false);
        verify(observer, never()).onNext(true);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void withFollowingFirstObservable() {
        Observable<Integer> o = Observable.fromArray(1, 3, 5, 6);
        Observable<Boolean> anyEven = o.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer i) {
                return i % 2 == 0;
            }
        }).toObservable();
        assertTrue(anyEven.blockingFirst());
    }

    @Test
    public void issue1935NoUnsubscribeDownstreamObservable() {
        Observable<Integer> source = Observable.just(1).isEmpty().toObservable().flatMap(new Function<Boolean, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Boolean t1) {
                return Observable.just(2).delay(500, TimeUnit.MILLISECONDS);
            }
        });
        assertEquals((Object) 2, source.blockingFirst());
    }

    @Test
    public void predicateThrowsExceptionAndValueInCauseMessageObservable() {
        TestObserverEx<Boolean> to = new TestObserverEx<>();
        final IllegalArgumentException ex = new IllegalArgumentException();
        Observable.just("Boo!").any(new Predicate<String>() {

            @Override
            public boolean test(String v) {
                throw ex;
            }
        }).subscribe(to);
        to.assertTerminated();
        to.assertNoValues();
        to.assertNotComplete();
        to.assertError(ex);
    // FIXME value as last cause?
    // assertTrue(ex.getCause().getMessage().contains("Boo!"));
    }

    @Test
    public void anyWithTwoItems() {
        Observable<Integer> w = Observable.just(1, 2);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(false);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void isEmptyWithTwoItems() {
        Observable<Integer> w = Observable.just(1, 2);
        Single<Boolean> single = w.isEmpty();
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(true);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithOneItem() {
        Observable<Integer> w = Observable.just(1);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(false);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void isEmptyWithOneItem() {
        Observable<Integer> w = Observable.just(1);
        Single<Boolean> single = w.isEmpty();
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(true);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithEmpty() {
        Observable<Integer> w = Observable.empty();
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void isEmptyWithEmpty() {
        Observable<Integer> w = Observable.empty();
        Single<Boolean> single = w.isEmpty();
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onSuccess(false);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithPredicate1() {
        Observable<Integer> w = Observable.just(1, 2, 3);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 2;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(false);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void exists1() {
        Observable<Integer> w = Observable.just(1, 2, 3);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 2;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(false);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithPredicate2() {
        Observable<Integer> w = Observable.just(1, 2, 3);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 1;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithEmptyAndPredicate() {
        // If the source is empty, always output false.
        Observable<Integer> w = Observable.empty();
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t) {
                return true;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void withFollowingFirst() {
        Observable<Integer> o = Observable.fromArray(1, 3, 5, 6);
        Single<Boolean> anyEven = o.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer i) {
                return i % 2 == 0;
            }
        });
        assertTrue(anyEven.blockingGet());
    }

    @Test
    public void issue1935NoUnsubscribeDownstream() {
        Observable<Integer> source = Observable.just(1).isEmpty().flatMapObservable(new Function<Boolean, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Boolean t1) {
                return Observable.just(2).delay(500, TimeUnit.MILLISECONDS);
            }
        });
        assertEquals((Object) 2, source.blockingFirst());
    }

    @Test
    public void predicateThrowsExceptionAndValueInCauseMessage() {
        TestObserverEx<Boolean> to = new TestObserverEx<>();
        final IllegalArgumentException ex = new IllegalArgumentException();
        Observable.just("Boo!").any(new Predicate<String>() {

            @Override
            public boolean test(String v) {
                throw ex;
            }
        }).subscribe(to);
        to.assertTerminated();
        to.assertNoValues();
        to.assertNotComplete();
        to.assertError(ex);
    // FIXME value as last cause?
    // assertTrue(ex.getCause().getMessage().contains("Boo!"));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).any(Functions.alwaysTrue()).toObservable());
        TestHelper.checkDisposed(Observable.just(1).any(Functions.alwaysTrue()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Boolean>>() {

            @Override
            public ObservableSource<Boolean> apply(Observable<Object> o) throws Exception {
                return o.any(Functions.alwaysTrue()).toObservable();
            }
        });
        TestHelper.checkDoubleOnSubscribeObservableToSingle(new Function<Observable<Object>, SingleSource<Boolean>>() {

            @Override
            public SingleSource<Boolean> apply(Observable<Object> o) throws Exception {
                return o.any(Functions.alwaysTrue());
            }
        });
    }

    @Test
    public void predicateThrowsSuppressOthers() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onError(new IOException());
                    observer.onComplete();
                }
            }.any(new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) throws Exception {
                    throw new TestException();
                }
            }).toObservable().test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSourceSingle() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onError(new TestException("First"));
                    observer.onNext(1);
                    observer.onError(new TestException("Second"));
                    observer.onComplete();
                }
            }.any(Functions.alwaysTrue()).to(TestHelper.<Boolean>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableAnyTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithTwoItemsObservable() throws java.lang.Throwable {
            this.payloads.anyWithTwoItemsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithTwoItemsObservable() throws java.lang.Throwable {
            this.payloads.isEmptyWithTwoItemsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithOneItemObservable() throws java.lang.Throwable {
            this.payloads.anyWithOneItemObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithOneItemObservable() throws java.lang.Throwable {
            this.payloads.isEmptyWithOneItemObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithEmptyObservable() throws java.lang.Throwable {
            this.payloads.anyWithEmptyObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithEmptyObservable() throws java.lang.Throwable {
            this.payloads.isEmptyWithEmptyObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithPredicate1Observable() throws java.lang.Throwable {
            this.payloads.anyWithPredicate1Observable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exists1Observable() throws java.lang.Throwable {
            this.payloads.exists1Observable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithPredicate2Observable() throws java.lang.Throwable {
            this.payloads.anyWithPredicate2Observable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithEmptyAndPredicateObservable() throws java.lang.Throwable {
            this.payloads.anyWithEmptyAndPredicateObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFollowingFirstObservable() throws java.lang.Throwable {
            this.payloads.withFollowingFirstObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1935NoUnsubscribeDownstreamObservable() throws java.lang.Throwable {
            this.payloads.issue1935NoUnsubscribeDownstreamObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_predicateThrowsExceptionAndValueInCauseMessageObservable() throws java.lang.Throwable {
            this.payloads.predicateThrowsExceptionAndValueInCauseMessageObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithTwoItems() throws java.lang.Throwable {
            this.payloads.anyWithTwoItems.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithTwoItems() throws java.lang.Throwable {
            this.payloads.isEmptyWithTwoItems.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithOneItem() throws java.lang.Throwable {
            this.payloads.anyWithOneItem.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithOneItem() throws java.lang.Throwable {
            this.payloads.isEmptyWithOneItem.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithEmpty() throws java.lang.Throwable {
            this.payloads.anyWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithEmpty() throws java.lang.Throwable {
            this.payloads.isEmptyWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithPredicate1() throws java.lang.Throwable {
            this.payloads.anyWithPredicate1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exists1() throws java.lang.Throwable {
            this.payloads.exists1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithPredicate2() throws java.lang.Throwable {
            this.payloads.anyWithPredicate2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithEmptyAndPredicate() throws java.lang.Throwable {
            this.payloads.anyWithEmptyAndPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFollowingFirst() throws java.lang.Throwable {
            this.payloads.withFollowingFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1935NoUnsubscribeDownstream() throws java.lang.Throwable {
            this.payloads.issue1935NoUnsubscribeDownstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_predicateThrowsExceptionAndValueInCauseMessage() throws java.lang.Throwable {
            this.payloads.predicateThrowsExceptionAndValueInCauseMessage.evaluate();
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
        public void benchmark_predicateThrowsSuppressOthers() throws java.lang.Throwable {
            this.payloads.predicateThrowsSuppressOthers.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceSingle() throws java.lang.Throwable {
            this.payloads.badSourceSingle.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableAnyTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableAnyTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableAnyTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableAnyTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableAnyTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableAnyTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableAnyTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableAnyTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement anyWithTwoItemsObservable;

            public org.junit.runners.model.Statement isEmptyWithTwoItemsObservable;

            public org.junit.runners.model.Statement anyWithOneItemObservable;

            public org.junit.runners.model.Statement isEmptyWithOneItemObservable;

            public org.junit.runners.model.Statement anyWithEmptyObservable;

            public org.junit.runners.model.Statement isEmptyWithEmptyObservable;

            public org.junit.runners.model.Statement anyWithPredicate1Observable;

            public org.junit.runners.model.Statement exists1Observable;

            public org.junit.runners.model.Statement anyWithPredicate2Observable;

            public org.junit.runners.model.Statement anyWithEmptyAndPredicateObservable;

            public org.junit.runners.model.Statement withFollowingFirstObservable;

            public org.junit.runners.model.Statement issue1935NoUnsubscribeDownstreamObservable;

            public org.junit.runners.model.Statement predicateThrowsExceptionAndValueInCauseMessageObservable;

            public org.junit.runners.model.Statement anyWithTwoItems;

            public org.junit.runners.model.Statement isEmptyWithTwoItems;

            public org.junit.runners.model.Statement anyWithOneItem;

            public org.junit.runners.model.Statement isEmptyWithOneItem;

            public org.junit.runners.model.Statement anyWithEmpty;

            public org.junit.runners.model.Statement isEmptyWithEmpty;

            public org.junit.runners.model.Statement anyWithPredicate1;

            public org.junit.runners.model.Statement exists1;

            public org.junit.runners.model.Statement anyWithPredicate2;

            public org.junit.runners.model.Statement anyWithEmptyAndPredicate;

            public org.junit.runners.model.Statement withFollowingFirst;

            public org.junit.runners.model.Statement issue1935NoUnsubscribeDownstream;

            public org.junit.runners.model.Statement predicateThrowsExceptionAndValueInCauseMessage;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement predicateThrowsSuppressOthers;

            public org.junit.runners.model.Statement badSourceSingle;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.anyWithTwoItemsObservable = _ClassStatement.forPayload(ObservableAnyTest::anyWithTwoItemsObservable, "anyWithTwoItemsObservable", this);
            this.payloads.isEmptyWithTwoItemsObservable = _ClassStatement.forPayload(ObservableAnyTest::isEmptyWithTwoItemsObservable, "isEmptyWithTwoItemsObservable", this);
            this.payloads.anyWithOneItemObservable = _ClassStatement.forPayload(ObservableAnyTest::anyWithOneItemObservable, "anyWithOneItemObservable", this);
            this.payloads.isEmptyWithOneItemObservable = _ClassStatement.forPayload(ObservableAnyTest::isEmptyWithOneItemObservable, "isEmptyWithOneItemObservable", this);
            this.payloads.anyWithEmptyObservable = _ClassStatement.forPayload(ObservableAnyTest::anyWithEmptyObservable, "anyWithEmptyObservable", this);
            this.payloads.isEmptyWithEmptyObservable = _ClassStatement.forPayload(ObservableAnyTest::isEmptyWithEmptyObservable, "isEmptyWithEmptyObservable", this);
            this.payloads.anyWithPredicate1Observable = _ClassStatement.forPayload(ObservableAnyTest::anyWithPredicate1Observable, "anyWithPredicate1Observable", this);
            this.payloads.exists1Observable = _ClassStatement.forPayload(ObservableAnyTest::exists1Observable, "exists1Observable", this);
            this.payloads.anyWithPredicate2Observable = _ClassStatement.forPayload(ObservableAnyTest::anyWithPredicate2Observable, "anyWithPredicate2Observable", this);
            this.payloads.anyWithEmptyAndPredicateObservable = _ClassStatement.forPayload(ObservableAnyTest::anyWithEmptyAndPredicateObservable, "anyWithEmptyAndPredicateObservable", this);
            this.payloads.withFollowingFirstObservable = _ClassStatement.forPayload(ObservableAnyTest::withFollowingFirstObservable, "withFollowingFirstObservable", this);
            this.payloads.issue1935NoUnsubscribeDownstreamObservable = _ClassStatement.forPayload(ObservableAnyTest::issue1935NoUnsubscribeDownstreamObservable, "issue1935NoUnsubscribeDownstreamObservable", this);
            this.payloads.predicateThrowsExceptionAndValueInCauseMessageObservable = _ClassStatement.forPayload(ObservableAnyTest::predicateThrowsExceptionAndValueInCauseMessageObservable, "predicateThrowsExceptionAndValueInCauseMessageObservable", this);
            this.payloads.anyWithTwoItems = _ClassStatement.forPayload(ObservableAnyTest::anyWithTwoItems, "anyWithTwoItems", this);
            this.payloads.isEmptyWithTwoItems = _ClassStatement.forPayload(ObservableAnyTest::isEmptyWithTwoItems, "isEmptyWithTwoItems", this);
            this.payloads.anyWithOneItem = _ClassStatement.forPayload(ObservableAnyTest::anyWithOneItem, "anyWithOneItem", this);
            this.payloads.isEmptyWithOneItem = _ClassStatement.forPayload(ObservableAnyTest::isEmptyWithOneItem, "isEmptyWithOneItem", this);
            this.payloads.anyWithEmpty = _ClassStatement.forPayload(ObservableAnyTest::anyWithEmpty, "anyWithEmpty", this);
            this.payloads.isEmptyWithEmpty = _ClassStatement.forPayload(ObservableAnyTest::isEmptyWithEmpty, "isEmptyWithEmpty", this);
            this.payloads.anyWithPredicate1 = _ClassStatement.forPayload(ObservableAnyTest::anyWithPredicate1, "anyWithPredicate1", this);
            this.payloads.exists1 = _ClassStatement.forPayload(ObservableAnyTest::exists1, "exists1", this);
            this.payloads.anyWithPredicate2 = _ClassStatement.forPayload(ObservableAnyTest::anyWithPredicate2, "anyWithPredicate2", this);
            this.payloads.anyWithEmptyAndPredicate = _ClassStatement.forPayload(ObservableAnyTest::anyWithEmptyAndPredicate, "anyWithEmptyAndPredicate", this);
            this.payloads.withFollowingFirst = _ClassStatement.forPayload(ObservableAnyTest::withFollowingFirst, "withFollowingFirst", this);
            this.payloads.issue1935NoUnsubscribeDownstream = _ClassStatement.forPayload(ObservableAnyTest::issue1935NoUnsubscribeDownstream, "issue1935NoUnsubscribeDownstream", this);
            this.payloads.predicateThrowsExceptionAndValueInCauseMessage = _ClassStatement.forPayload(ObservableAnyTest::predicateThrowsExceptionAndValueInCauseMessage, "predicateThrowsExceptionAndValueInCauseMessage", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableAnyTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableAnyTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.predicateThrowsSuppressOthers = _ClassStatement.forPayload(ObservableAnyTest::predicateThrowsSuppressOthers, "predicateThrowsSuppressOthers", this);
            this.payloads.badSourceSingle = _ClassStatement.forPayload(ObservableAnyTest::badSourceSingle, "badSourceSingle", this);
        }
    }
}
