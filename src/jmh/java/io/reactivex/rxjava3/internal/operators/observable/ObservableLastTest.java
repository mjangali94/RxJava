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
import java.util.NoSuchElementException;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableLastTest extends RxJavaTest {

    @Test
    public void lastWithElements() {
        Maybe<Integer> last = Observable.just(1, 2, 3).lastElement();
        assertEquals(3, last.blockingGet().intValue());
    }

    @Test
    public void lastWithNoElements() {
        Maybe<?> last = Observable.empty().lastElement();
        assertNull(last.blockingGet());
    }

    @Test
    public void lastMultiSubscribe() {
        Maybe<Integer> last = Observable.just(1, 2, 3).lastElement();
        assertEquals(3, last.blockingGet().intValue());
        assertEquals(3, last.blockingGet().intValue());
    }

    @Test
    public void lastViaObservable() {
        Observable.just(1, 2, 3).lastElement();
    }

    @Test
    public void last() {
        Maybe<Integer> o = Observable.just(1, 2, 3).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(3);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithOneElement() {
        Maybe<Integer> o = Observable.just(1).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithEmpty() {
        Maybe<Integer> o = Observable.<Integer>empty().lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithPredicate() {
        Maybe<Integer> o = Observable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(6);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithPredicateAndOneElement() {
        Maybe<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithPredicateAndEmpty() {
        Maybe<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefault() {
        Single<Integer> o = Observable.just(1, 2, 3).last(4);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(3);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithOneElement() {
        Single<Integer> o = Observable.just(1).last(2);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithEmpty() {
        Single<Integer> o = Observable.<Integer>empty().last(1);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithPredicate() {
        Single<Integer> o = Observable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).last(8);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(6);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithPredicateAndOneElement() {
        Single<Integer> o = Observable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).last(4);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithPredicateAndEmpty() {
        Single<Integer> o = Observable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).last(2);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrErrorNoElement() {
        Observable.empty().lastOrError().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void lastOrErrorOneElement() {
        Observable.just(1).lastOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void lastOrErrorMultipleElements() {
        Observable.just(1, 2, 3).lastOrError().test().assertNoErrors().assertValue(3);
    }

    @Test
    public void lastOrErrorError() {
        Observable.error(new RuntimeException("error")).lastOrError().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.never().lastElement().toObservable());
        TestHelper.checkDisposed(Observable.never().lastElement());
        TestHelper.checkDisposed(Observable.just(1).lastOrError().toObservable());
        TestHelper.checkDisposed(Observable.just(1).lastOrError());
        TestHelper.checkDisposed(Observable.just(1).last(2).toObservable());
        TestHelper.checkDisposed(Observable.just(1).last(2));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToMaybe(new Function<Observable<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Observable<Object> o) throws Exception {
                return o.lastElement();
            }
        });
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.lastElement().toObservable();
            }
        });
        TestHelper.checkDoubleOnSubscribeObservableToSingle(new Function<Observable<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Observable<Object> o) throws Exception {
                return o.lastOrError();
            }
        });
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.lastOrError().toObservable();
            }
        });
        TestHelper.checkDoubleOnSubscribeObservableToSingle(new Function<Observable<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Observable<Object> o) throws Exception {
                return o.last(2);
            }
        });
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.last(2).toObservable();
            }
        });
    }

    @Test
    public void error() {
        Observable.error(new TestException()).lastElement().test().assertFailure(TestException.class);
    }

    @Test
    public void errorLastOrErrorObservable() {
        Observable.error(new TestException()).lastOrError().toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void emptyLastOrErrorObservable() {
        Observable.empty().lastOrError().toObservable().test().assertFailure(NoSuchElementException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableLastTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithElements() throws java.lang.Throwable {
            this.payloads.lastWithElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithNoElements() throws java.lang.Throwable {
            this.payloads.lastWithNoElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastMultiSubscribe() throws java.lang.Throwable {
            this.payloads.lastMultiSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastViaObservable() throws java.lang.Throwable {
            this.payloads.lastViaObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_last() throws java.lang.Throwable {
            this.payloads.last.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithOneElement() throws java.lang.Throwable {
            this.payloads.lastWithOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithEmpty() throws java.lang.Throwable {
            this.payloads.lastWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithPredicate() throws java.lang.Throwable {
            this.payloads.lastWithPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithPredicateAndOneElement() throws java.lang.Throwable {
            this.payloads.lastWithPredicateAndOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithPredicateAndEmpty() throws java.lang.Throwable {
            this.payloads.lastWithPredicateAndEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrDefault() throws java.lang.Throwable {
            this.payloads.lastOrDefault.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrDefaultWithOneElement() throws java.lang.Throwable {
            this.payloads.lastOrDefaultWithOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrDefaultWithEmpty() throws java.lang.Throwable {
            this.payloads.lastOrDefaultWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrDefaultWithPredicate() throws java.lang.Throwable {
            this.payloads.lastOrDefaultWithPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrDefaultWithPredicateAndOneElement() throws java.lang.Throwable {
            this.payloads.lastOrDefaultWithPredicateAndOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrDefaultWithPredicateAndEmpty() throws java.lang.Throwable {
            this.payloads.lastOrDefaultWithPredicateAndEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrErrorNoElement() throws java.lang.Throwable {
            this.payloads.lastOrErrorNoElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrErrorOneElement() throws java.lang.Throwable {
            this.payloads.lastOrErrorOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrErrorMultipleElements() throws java.lang.Throwable {
            this.payloads.lastOrErrorMultipleElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOrErrorError() throws java.lang.Throwable {
            this.payloads.lastOrErrorError.evaluate();
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
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorLastOrErrorObservable() throws java.lang.Throwable {
            this.payloads.errorLastOrErrorObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyLastOrErrorObservable() throws java.lang.Throwable {
            this.payloads.emptyLastOrErrorObservable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableLastTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableLastTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableLastTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableLastTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableLastTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableLastTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableLastTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableLastTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement lastWithElements;

            public org.junit.runners.model.Statement lastWithNoElements;

            public org.junit.runners.model.Statement lastMultiSubscribe;

            public org.junit.runners.model.Statement lastViaObservable;

            public org.junit.runners.model.Statement last;

            public org.junit.runners.model.Statement lastWithOneElement;

            public org.junit.runners.model.Statement lastWithEmpty;

            public org.junit.runners.model.Statement lastWithPredicate;

            public org.junit.runners.model.Statement lastWithPredicateAndOneElement;

            public org.junit.runners.model.Statement lastWithPredicateAndEmpty;

            public org.junit.runners.model.Statement lastOrDefault;

            public org.junit.runners.model.Statement lastOrDefaultWithOneElement;

            public org.junit.runners.model.Statement lastOrDefaultWithEmpty;

            public org.junit.runners.model.Statement lastOrDefaultWithPredicate;

            public org.junit.runners.model.Statement lastOrDefaultWithPredicateAndOneElement;

            public org.junit.runners.model.Statement lastOrDefaultWithPredicateAndEmpty;

            public org.junit.runners.model.Statement lastOrErrorNoElement;

            public org.junit.runners.model.Statement lastOrErrorOneElement;

            public org.junit.runners.model.Statement lastOrErrorMultipleElements;

            public org.junit.runners.model.Statement lastOrErrorError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement errorLastOrErrorObservable;

            public org.junit.runners.model.Statement emptyLastOrErrorObservable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.lastWithElements = _ClassStatement.forPayload(ObservableLastTest::lastWithElements, "lastWithElements", this);
            this.payloads.lastWithNoElements = _ClassStatement.forPayload(ObservableLastTest::lastWithNoElements, "lastWithNoElements", this);
            this.payloads.lastMultiSubscribe = _ClassStatement.forPayload(ObservableLastTest::lastMultiSubscribe, "lastMultiSubscribe", this);
            this.payloads.lastViaObservable = _ClassStatement.forPayload(ObservableLastTest::lastViaObservable, "lastViaObservable", this);
            this.payloads.last = _ClassStatement.forPayload(ObservableLastTest::last, "last", this);
            this.payloads.lastWithOneElement = _ClassStatement.forPayload(ObservableLastTest::lastWithOneElement, "lastWithOneElement", this);
            this.payloads.lastWithEmpty = _ClassStatement.forPayload(ObservableLastTest::lastWithEmpty, "lastWithEmpty", this);
            this.payloads.lastWithPredicate = _ClassStatement.forPayload(ObservableLastTest::lastWithPredicate, "lastWithPredicate", this);
            this.payloads.lastWithPredicateAndOneElement = _ClassStatement.forPayload(ObservableLastTest::lastWithPredicateAndOneElement, "lastWithPredicateAndOneElement", this);
            this.payloads.lastWithPredicateAndEmpty = _ClassStatement.forPayload(ObservableLastTest::lastWithPredicateAndEmpty, "lastWithPredicateAndEmpty", this);
            this.payloads.lastOrDefault = _ClassStatement.forPayload(ObservableLastTest::lastOrDefault, "lastOrDefault", this);
            this.payloads.lastOrDefaultWithOneElement = _ClassStatement.forPayload(ObservableLastTest::lastOrDefaultWithOneElement, "lastOrDefaultWithOneElement", this);
            this.payloads.lastOrDefaultWithEmpty = _ClassStatement.forPayload(ObservableLastTest::lastOrDefaultWithEmpty, "lastOrDefaultWithEmpty", this);
            this.payloads.lastOrDefaultWithPredicate = _ClassStatement.forPayload(ObservableLastTest::lastOrDefaultWithPredicate, "lastOrDefaultWithPredicate", this);
            this.payloads.lastOrDefaultWithPredicateAndOneElement = _ClassStatement.forPayload(ObservableLastTest::lastOrDefaultWithPredicateAndOneElement, "lastOrDefaultWithPredicateAndOneElement", this);
            this.payloads.lastOrDefaultWithPredicateAndEmpty = _ClassStatement.forPayload(ObservableLastTest::lastOrDefaultWithPredicateAndEmpty, "lastOrDefaultWithPredicateAndEmpty", this);
            this.payloads.lastOrErrorNoElement = _ClassStatement.forPayload(ObservableLastTest::lastOrErrorNoElement, "lastOrErrorNoElement", this);
            this.payloads.lastOrErrorOneElement = _ClassStatement.forPayload(ObservableLastTest::lastOrErrorOneElement, "lastOrErrorOneElement", this);
            this.payloads.lastOrErrorMultipleElements = _ClassStatement.forPayload(ObservableLastTest::lastOrErrorMultipleElements, "lastOrErrorMultipleElements", this);
            this.payloads.lastOrErrorError = _ClassStatement.forPayload(ObservableLastTest::lastOrErrorError, "lastOrErrorError", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableLastTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableLastTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.error = _ClassStatement.forPayload(ObservableLastTest::error, "error", this);
            this.payloads.errorLastOrErrorObservable = _ClassStatement.forPayload(ObservableLastTest::errorLastOrErrorObservable, "errorLastOrErrorObservable", this);
            this.payloads.emptyLastOrErrorObservable = _ClassStatement.forPayload(ObservableLastTest::emptyLastOrErrorObservable, "emptyLastOrErrorObservable", this);
        }
    }
}
