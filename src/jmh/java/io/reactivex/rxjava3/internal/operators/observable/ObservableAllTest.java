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
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableAllTest extends RxJavaTest {

    @Test
    public void allObservable() {
        Observable<String> obs = Observable.just("one", "two", "six");
        Observer<Boolean> observer = TestHelper.mockObserver();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).toObservable().subscribe(observer);
        verify(observer).onSubscribe((Disposable) any());
        verify(observer).onNext(true);
        verify(observer).onComplete();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void notAllObservable() {
        Observable<String> obs = Observable.just("one", "two", "three", "six");
        Observer<Boolean> observer = TestHelper.mockObserver();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).toObservable().subscribe(observer);
        verify(observer).onSubscribe((Disposable) any());
        verify(observer).onNext(false);
        verify(observer).onComplete();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void emptyObservable() {
        Observable<String> obs = Observable.empty();
        Observer<Boolean> observer = TestHelper.mockObserver();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).toObservable().subscribe(observer);
        verify(observer).onSubscribe((Disposable) any());
        verify(observer).onNext(true);
        verify(observer).onComplete();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void errorObservable() {
        Throwable error = new Throwable();
        Observable<String> obs = Observable.error(error);
        Observer<Boolean> observer = TestHelper.mockObserver();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).toObservable().subscribe(observer);
        verify(observer).onSubscribe((Disposable) any());
        verify(observer).onError(error);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void followingFirstObservable() {
        Observable<Integer> o = Observable.fromArray(1, 3, 5, 6);
        Observable<Boolean> allOdd = o.all(new Predicate<Integer>() {

            @Override
            public boolean test(Integer i) {
                return i % 2 == 1;
            }
        }).toObservable();
        assertFalse(allOdd.blockingFirst());
    }

    @Test
    public void issue1935NoUnsubscribeDownstreamObservable() {
        Observable<Integer> source = Observable.just(1).all(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return false;
            }
        }).toObservable().flatMap(new Function<Boolean, Observable<Integer>>() {

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
        Observable.just("Boo!").all(new Predicate<String>() {

            @Override
            public boolean test(String v) {
                throw ex;
            }
        }).subscribe(to);
        to.assertTerminated();
        to.assertNoValues();
        to.assertNotComplete();
        to.assertError(ex);
    // FIXME need to decide about adding the value that probably caused the crash in some way
    // assertTrue(ex.getCause().getMessage().contains("Boo!"));
    }

    @Test
    public void all() {
        Observable<String> obs = Observable.just("one", "two", "six");
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).subscribe(observer);
        verify(observer).onSubscribe((Disposable) any());
        verify(observer).onSuccess(true);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void notAll() {
        Observable<String> obs = Observable.just("one", "two", "three", "six");
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).subscribe(observer);
        verify(observer).onSubscribe((Disposable) any());
        verify(observer).onSuccess(false);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void empty() {
        Observable<String> obs = Observable.empty();
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).subscribe(observer);
        verify(observer).onSubscribe((Disposable) any());
        verify(observer).onSuccess(true);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void error() {
        Throwable error = new Throwable();
        Observable<String> obs = Observable.error(error);
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).subscribe(observer);
        verify(observer).onSubscribe((Disposable) any());
        verify(observer).onError(error);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void followingFirst() {
        Observable<Integer> o = Observable.fromArray(1, 3, 5, 6);
        Single<Boolean> allOdd = o.all(new Predicate<Integer>() {

            @Override
            public boolean test(Integer i) {
                return i % 2 == 1;
            }
        });
        assertFalse(allOdd.blockingGet());
    }

    @Test
    public void issue1935NoUnsubscribeDownstream() {
        Observable<Integer> source = Observable.just(1).all(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return false;
            }
        }).flatMapObservable(new Function<Boolean, Observable<Integer>>() {

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
        Observable.just("Boo!").all(new Predicate<String>() {

            @Override
            public boolean test(String v) {
                throw ex;
            }
        }).subscribe(to);
        to.assertTerminated();
        to.assertNoValues();
        to.assertNotComplete();
        to.assertError(ex);
    // FIXME need to decide about adding the value that probably caused the crash in some way
    // assertTrue(ex.getCause().getMessage().contains("Boo!"));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).all(Functions.alwaysTrue()).toObservable());
        TestHelper.checkDisposed(Observable.just(1).all(Functions.alwaysTrue()));
    }

    @Test
    public void predicateThrowsObservable() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.all(new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) throws Exception {
                    throw new TestException();
                }
            }).toObservable().test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void predicateThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.all(new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) throws Exception {
                    throw new TestException();
                }
            }).test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToSingle(o -> o.all(v -> true));
    }

    @Test
    public void doubleOnSubscribeObservable() {
        TestHelper.checkDoubleOnSubscribeObservable(o -> o.all(v -> true).toObservable());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableAllTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allObservable() throws java.lang.Throwable {
            this.payloads.allObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notAllObservable() throws java.lang.Throwable {
            this.payloads.notAllObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyObservable() throws java.lang.Throwable {
            this.payloads.emptyObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorObservable() throws java.lang.Throwable {
            this.payloads.errorObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_followingFirstObservable() throws java.lang.Throwable {
            this.payloads.followingFirstObservable.evaluate();
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
        public void benchmark_all() throws java.lang.Throwable {
            this.payloads.all.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notAll() throws java.lang.Throwable {
            this.payloads.notAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_followingFirst() throws java.lang.Throwable {
            this.payloads.followingFirst.evaluate();
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
        public void benchmark_predicateThrowsObservable() throws java.lang.Throwable {
            this.payloads.predicateThrowsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_predicateThrows() throws java.lang.Throwable {
            this.payloads.predicateThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeObservable() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribeObservable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableAllTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableAllTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableAllTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableAllTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableAllTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableAllTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableAllTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableAllTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement allObservable;

            public org.junit.runners.model.Statement notAllObservable;

            public org.junit.runners.model.Statement emptyObservable;

            public org.junit.runners.model.Statement errorObservable;

            public org.junit.runners.model.Statement followingFirstObservable;

            public org.junit.runners.model.Statement issue1935NoUnsubscribeDownstreamObservable;

            public org.junit.runners.model.Statement predicateThrowsExceptionAndValueInCauseMessageObservable;

            public org.junit.runners.model.Statement all;

            public org.junit.runners.model.Statement notAll;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement followingFirst;

            public org.junit.runners.model.Statement issue1935NoUnsubscribeDownstream;

            public org.junit.runners.model.Statement predicateThrowsExceptionAndValueInCauseMessage;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement predicateThrowsObservable;

            public org.junit.runners.model.Statement predicateThrows;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement doubleOnSubscribeObservable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.allObservable = _ClassStatement.forPayload(ObservableAllTest::allObservable, "allObservable", this);
            this.payloads.notAllObservable = _ClassStatement.forPayload(ObservableAllTest::notAllObservable, "notAllObservable", this);
            this.payloads.emptyObservable = _ClassStatement.forPayload(ObservableAllTest::emptyObservable, "emptyObservable", this);
            this.payloads.errorObservable = _ClassStatement.forPayload(ObservableAllTest::errorObservable, "errorObservable", this);
            this.payloads.followingFirstObservable = _ClassStatement.forPayload(ObservableAllTest::followingFirstObservable, "followingFirstObservable", this);
            this.payloads.issue1935NoUnsubscribeDownstreamObservable = _ClassStatement.forPayload(ObservableAllTest::issue1935NoUnsubscribeDownstreamObservable, "issue1935NoUnsubscribeDownstreamObservable", this);
            this.payloads.predicateThrowsExceptionAndValueInCauseMessageObservable = _ClassStatement.forPayload(ObservableAllTest::predicateThrowsExceptionAndValueInCauseMessageObservable, "predicateThrowsExceptionAndValueInCauseMessageObservable", this);
            this.payloads.all = _ClassStatement.forPayload(ObservableAllTest::all, "all", this);
            this.payloads.notAll = _ClassStatement.forPayload(ObservableAllTest::notAll, "notAll", this);
            this.payloads.empty = _ClassStatement.forPayload(ObservableAllTest::empty, "empty", this);
            this.payloads.error = _ClassStatement.forPayload(ObservableAllTest::error, "error", this);
            this.payloads.followingFirst = _ClassStatement.forPayload(ObservableAllTest::followingFirst, "followingFirst", this);
            this.payloads.issue1935NoUnsubscribeDownstream = _ClassStatement.forPayload(ObservableAllTest::issue1935NoUnsubscribeDownstream, "issue1935NoUnsubscribeDownstream", this);
            this.payloads.predicateThrowsExceptionAndValueInCauseMessage = _ClassStatement.forPayload(ObservableAllTest::predicateThrowsExceptionAndValueInCauseMessage, "predicateThrowsExceptionAndValueInCauseMessage", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableAllTest::dispose, "dispose", this);
            this.payloads.predicateThrowsObservable = _ClassStatement.forPayload(ObservableAllTest::predicateThrowsObservable, "predicateThrowsObservable", this);
            this.payloads.predicateThrows = _ClassStatement.forPayload(ObservableAllTest::predicateThrows, "predicateThrows", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableAllTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.doubleOnSubscribeObservable = _ClassStatement.forPayload(ObservableAllTest::doubleOnSubscribeObservable, "doubleOnSubscribeObservable", this);
        }
    }
}
