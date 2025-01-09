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
import java.util.*;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableElementAtTest extends RxJavaTest {

    @Test
    public void elementAtObservable() {
        assertEquals(2, Observable.fromArray(1, 2).elementAt(1).toObservable().blockingSingle().intValue());
    }

    @Test
    public void elementAtWithIndexOutOfBoundsObservable() {
        assertEquals(-99, Observable.fromArray(1, 2).elementAt(2).toObservable().blockingSingle(-99).intValue());
    }

    @Test
    public void elementAtOrDefaultObservable() {
        assertEquals(2, Observable.fromArray(1, 2).elementAt(1, 0).toObservable().blockingSingle().intValue());
    }

    @Test
    public void elementAtOrDefaultWithIndexOutOfBoundsObservable() {
        assertEquals(0, Observable.fromArray(1, 2).elementAt(2, 0).toObservable().blockingSingle().intValue());
    }

    @Test
    public void elementAt() {
        assertEquals(2, Observable.fromArray(1, 2).elementAt(1).blockingGet().intValue());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtWithMinusIndex() {
        Observable.fromArray(1, 2).elementAt(-1);
    }

    @Test
    public void elementAtWithIndexOutOfBounds() {
        assertNull(Observable.fromArray(1, 2).elementAt(2).blockingGet());
    }

    @Test
    public void elementAtOrDefault() {
        assertEquals(2, Observable.fromArray(1, 2).elementAt(1, 0).blockingGet().intValue());
    }

    @Test
    public void elementAtOrDefaultWithIndexOutOfBounds() {
        assertEquals(0, Observable.fromArray(1, 2).elementAt(2, 0).blockingGet().intValue());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtOrDefaultWithMinusIndex() {
        Observable.fromArray(1, 2).elementAt(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtOrErrorNegativeIndex() {
        Observable.empty().elementAtOrError(-1);
    }

    @Test
    public void elementAtOrErrorNoElement() {
        Observable.empty().elementAtOrError(0).test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void elementAtOrErrorOneElement() {
        Observable.just(1).elementAtOrError(0).test().assertNoErrors().assertValue(1);
    }

    @Test
    public void elementAtOrErrorMultipleElements() {
        Observable.just(1, 2, 3).elementAtOrError(1).test().assertNoErrors().assertValue(2);
    }

    @Test
    public void elementAtOrErrorInvalidIndex() {
        Observable.just(1, 2, 3).elementAtOrError(3).test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void elementAtOrErrorError() {
        Observable.error(new RuntimeException("error")).elementAtOrError(0).to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void elementAtIndex0OnEmptySource() {
        Observable.empty().elementAt(0).test().assertResult();
    }

    @Test
    public void elementAtIndex0WithDefaultOnEmptySource() {
        Observable.empty().elementAt(0, 5).test().assertResult(5);
    }

    @Test
    public void elementAtIndex1OnEmptySource() {
        Observable.empty().elementAt(1).test().assertResult();
    }

    @Test
    public void elementAtIndex1WithDefaultOnEmptySource() {
        Observable.empty().elementAt(1, 10).test().assertResult(10);
    }

    @Test
    public void elementAtOrErrorIndex1OnEmptySource() {
        Observable.empty().elementAtOrError(1).test().assertFailure(NoSuchElementException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().elementAt(0).toObservable());
        TestHelper.checkDisposed(PublishSubject.create().elementAt(0));
        TestHelper.checkDisposed(PublishSubject.create().elementAt(0, 1).toObservable());
        TestHelper.checkDisposed(PublishSubject.create().elementAt(0, 1));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.elementAt(0).toObservable();
            }
        });
        TestHelper.checkDoubleOnSubscribeObservableToMaybe(new Function<Observable<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Observable<Object> o) throws Exception {
                return o.elementAt(0);
            }
        });
        TestHelper.checkDoubleOnSubscribeObservableToSingle(new Function<Observable<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Observable<Object> o) throws Exception {
                return o.elementAt(0, 1);
            }
        });
    }

    @Test
    public void elementAtIndex1WithDefaultOnEmptySourceObservable() {
        Observable.empty().elementAt(1, 10).toObservable().test().assertResult(10);
    }

    @Test
    public void errorObservable() {
        Observable.error(new TestException()).elementAt(1, 10).toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void badSourceObservable() {
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
            }.elementAt(0).toObservable().test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
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
                    observer.onNext(2);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.elementAt(0).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSource2() {
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
            }.elementAt(0, 1).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableElementAtTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtObservable() throws java.lang.Throwable {
            this.payloads.elementAtObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtWithIndexOutOfBoundsObservable() throws java.lang.Throwable {
            this.payloads.elementAtWithIndexOutOfBoundsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefaultObservable() throws java.lang.Throwable {
            this.payloads.elementAtOrDefaultObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefaultWithIndexOutOfBoundsObservable() throws java.lang.Throwable {
            this.payloads.elementAtOrDefaultWithIndexOutOfBoundsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAt() throws java.lang.Throwable {
            this.payloads.elementAt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtWithMinusIndex() throws java.lang.Throwable {
            this.payloads.elementAtWithMinusIndex.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtWithIndexOutOfBounds() throws java.lang.Throwable {
            this.payloads.elementAtWithIndexOutOfBounds.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefault() throws java.lang.Throwable {
            this.payloads.elementAtOrDefault.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefaultWithIndexOutOfBounds() throws java.lang.Throwable {
            this.payloads.elementAtOrDefaultWithIndexOutOfBounds.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefaultWithMinusIndex() throws java.lang.Throwable {
            this.payloads.elementAtOrDefaultWithMinusIndex.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrErrorNegativeIndex() throws java.lang.Throwable {
            this.payloads.elementAtOrErrorNegativeIndex.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrErrorNoElement() throws java.lang.Throwable {
            this.payloads.elementAtOrErrorNoElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrErrorOneElement() throws java.lang.Throwable {
            this.payloads.elementAtOrErrorOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrErrorMultipleElements() throws java.lang.Throwable {
            this.payloads.elementAtOrErrorMultipleElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrErrorInvalidIndex() throws java.lang.Throwable {
            this.payloads.elementAtOrErrorInvalidIndex.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrErrorError() throws java.lang.Throwable {
            this.payloads.elementAtOrErrorError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtIndex0OnEmptySource() throws java.lang.Throwable {
            this.payloads.elementAtIndex0OnEmptySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtIndex0WithDefaultOnEmptySource() throws java.lang.Throwable {
            this.payloads.elementAtIndex0WithDefaultOnEmptySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtIndex1OnEmptySource() throws java.lang.Throwable {
            this.payloads.elementAtIndex1OnEmptySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtIndex1WithDefaultOnEmptySource() throws java.lang.Throwable {
            this.payloads.elementAtIndex1WithDefaultOnEmptySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrErrorIndex1OnEmptySource() throws java.lang.Throwable {
            this.payloads.elementAtOrErrorIndex1OnEmptySource.evaluate();
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
        public void benchmark_elementAtIndex1WithDefaultOnEmptySourceObservable() throws java.lang.Throwable {
            this.payloads.elementAtIndex1WithDefaultOnEmptySourceObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorObservable() throws java.lang.Throwable {
            this.payloads.errorObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceObservable() throws java.lang.Throwable {
            this.payloads.badSourceObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource2() throws java.lang.Throwable {
            this.payloads.badSource2.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableElementAtTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableElementAtTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableElementAtTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableElementAtTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableElementAtTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableElementAtTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableElementAtTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableElementAtTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement elementAtObservable;

            public org.junit.runners.model.Statement elementAtWithIndexOutOfBoundsObservable;

            public org.junit.runners.model.Statement elementAtOrDefaultObservable;

            public org.junit.runners.model.Statement elementAtOrDefaultWithIndexOutOfBoundsObservable;

            public org.junit.runners.model.Statement elementAt;

            public org.junit.runners.model.Statement elementAtWithMinusIndex;

            public org.junit.runners.model.Statement elementAtWithIndexOutOfBounds;

            public org.junit.runners.model.Statement elementAtOrDefault;

            public org.junit.runners.model.Statement elementAtOrDefaultWithIndexOutOfBounds;

            public org.junit.runners.model.Statement elementAtOrDefaultWithMinusIndex;

            public org.junit.runners.model.Statement elementAtOrErrorNegativeIndex;

            public org.junit.runners.model.Statement elementAtOrErrorNoElement;

            public org.junit.runners.model.Statement elementAtOrErrorOneElement;

            public org.junit.runners.model.Statement elementAtOrErrorMultipleElements;

            public org.junit.runners.model.Statement elementAtOrErrorInvalidIndex;

            public org.junit.runners.model.Statement elementAtOrErrorError;

            public org.junit.runners.model.Statement elementAtIndex0OnEmptySource;

            public org.junit.runners.model.Statement elementAtIndex0WithDefaultOnEmptySource;

            public org.junit.runners.model.Statement elementAtIndex1OnEmptySource;

            public org.junit.runners.model.Statement elementAtIndex1WithDefaultOnEmptySource;

            public org.junit.runners.model.Statement elementAtOrErrorIndex1OnEmptySource;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement elementAtIndex1WithDefaultOnEmptySourceObservable;

            public org.junit.runners.model.Statement errorObservable;

            public org.junit.runners.model.Statement badSourceObservable;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement badSource2;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.elementAtObservable = _ClassStatement.forPayload(ObservableElementAtTest::elementAtObservable, "elementAtObservable", this);
            this.payloads.elementAtWithIndexOutOfBoundsObservable = _ClassStatement.forPayload(ObservableElementAtTest::elementAtWithIndexOutOfBoundsObservable, "elementAtWithIndexOutOfBoundsObservable", this);
            this.payloads.elementAtOrDefaultObservable = _ClassStatement.forPayload(ObservableElementAtTest::elementAtOrDefaultObservable, "elementAtOrDefaultObservable", this);
            this.payloads.elementAtOrDefaultWithIndexOutOfBoundsObservable = _ClassStatement.forPayload(ObservableElementAtTest::elementAtOrDefaultWithIndexOutOfBoundsObservable, "elementAtOrDefaultWithIndexOutOfBoundsObservable", this);
            this.payloads.elementAt = _ClassStatement.forPayload(ObservableElementAtTest::elementAt, "elementAt", this);
            this.payloads.elementAtWithMinusIndex = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableElementAtTest::elementAtWithMinusIndex, java.lang.IndexOutOfBoundsException.class), "elementAtWithMinusIndex", this);
            this.payloads.elementAtWithIndexOutOfBounds = _ClassStatement.forPayload(ObservableElementAtTest::elementAtWithIndexOutOfBounds, "elementAtWithIndexOutOfBounds", this);
            this.payloads.elementAtOrDefault = _ClassStatement.forPayload(ObservableElementAtTest::elementAtOrDefault, "elementAtOrDefault", this);
            this.payloads.elementAtOrDefaultWithIndexOutOfBounds = _ClassStatement.forPayload(ObservableElementAtTest::elementAtOrDefaultWithIndexOutOfBounds, "elementAtOrDefaultWithIndexOutOfBounds", this);
            this.payloads.elementAtOrDefaultWithMinusIndex = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableElementAtTest::elementAtOrDefaultWithMinusIndex, java.lang.IndexOutOfBoundsException.class), "elementAtOrDefaultWithMinusIndex", this);
            this.payloads.elementAtOrErrorNegativeIndex = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableElementAtTest::elementAtOrErrorNegativeIndex, java.lang.IndexOutOfBoundsException.class), "elementAtOrErrorNegativeIndex", this);
            this.payloads.elementAtOrErrorNoElement = _ClassStatement.forPayload(ObservableElementAtTest::elementAtOrErrorNoElement, "elementAtOrErrorNoElement", this);
            this.payloads.elementAtOrErrorOneElement = _ClassStatement.forPayload(ObservableElementAtTest::elementAtOrErrorOneElement, "elementAtOrErrorOneElement", this);
            this.payloads.elementAtOrErrorMultipleElements = _ClassStatement.forPayload(ObservableElementAtTest::elementAtOrErrorMultipleElements, "elementAtOrErrorMultipleElements", this);
            this.payloads.elementAtOrErrorInvalidIndex = _ClassStatement.forPayload(ObservableElementAtTest::elementAtOrErrorInvalidIndex, "elementAtOrErrorInvalidIndex", this);
            this.payloads.elementAtOrErrorError = _ClassStatement.forPayload(ObservableElementAtTest::elementAtOrErrorError, "elementAtOrErrorError", this);
            this.payloads.elementAtIndex0OnEmptySource = _ClassStatement.forPayload(ObservableElementAtTest::elementAtIndex0OnEmptySource, "elementAtIndex0OnEmptySource", this);
            this.payloads.elementAtIndex0WithDefaultOnEmptySource = _ClassStatement.forPayload(ObservableElementAtTest::elementAtIndex0WithDefaultOnEmptySource, "elementAtIndex0WithDefaultOnEmptySource", this);
            this.payloads.elementAtIndex1OnEmptySource = _ClassStatement.forPayload(ObservableElementAtTest::elementAtIndex1OnEmptySource, "elementAtIndex1OnEmptySource", this);
            this.payloads.elementAtIndex1WithDefaultOnEmptySource = _ClassStatement.forPayload(ObservableElementAtTest::elementAtIndex1WithDefaultOnEmptySource, "elementAtIndex1WithDefaultOnEmptySource", this);
            this.payloads.elementAtOrErrorIndex1OnEmptySource = _ClassStatement.forPayload(ObservableElementAtTest::elementAtOrErrorIndex1OnEmptySource, "elementAtOrErrorIndex1OnEmptySource", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableElementAtTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableElementAtTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.elementAtIndex1WithDefaultOnEmptySourceObservable = _ClassStatement.forPayload(ObservableElementAtTest::elementAtIndex1WithDefaultOnEmptySourceObservable, "elementAtIndex1WithDefaultOnEmptySourceObservable", this);
            this.payloads.errorObservable = _ClassStatement.forPayload(ObservableElementAtTest::errorObservable, "errorObservable", this);
            this.payloads.badSourceObservable = _ClassStatement.forPayload(ObservableElementAtTest::badSourceObservable, "badSourceObservable", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableElementAtTest::badSource, "badSource", this);
            this.payloads.badSource2 = _ClassStatement.forPayload(ObservableElementAtTest::badSource2, "badSource2", this);
        }
    }
}
