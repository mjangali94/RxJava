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

public class FlowableLastTest extends RxJavaTest {

    @Test
    public void lastWithElements() {
        Maybe<Integer> last = Flowable.just(1, 2, 3).lastElement();
        assertEquals(3, last.blockingGet().intValue());
    }

    @Test
    public void lastWithNoElements() {
        Maybe<?> last = Flowable.empty().lastElement();
        assertNull(last.blockingGet());
    }

    @Test
    public void lastMultiSubscribe() {
        Maybe<Integer> last = Flowable.just(1, 2, 3).lastElement();
        assertEquals(3, last.blockingGet().intValue());
        assertEquals(3, last.blockingGet().intValue());
    }

    @Test
    public void lastViaFlowable() {
        Flowable.just(1, 2, 3).lastElement();
    }

    @Test
    public void last() {
        Maybe<Integer> maybe = Flowable.just(1, 2, 3).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(3);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithOneElement() {
        Maybe<Integer> maybe = Flowable.just(1).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithEmpty() {
        Maybe<Integer> maybe = Flowable.<Integer>empty().lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithPredicate() {
        Maybe<Integer> maybe = Flowable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(6);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithPredicateAndOneElement() {
        Maybe<Integer> maybe = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastWithPredicateAndEmpty() {
        Maybe<Integer> maybe = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).lastElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefault() {
        Single<Integer> single = Flowable.just(1, 2, 3).last(4);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(3);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithOneElement() {
        Single<Integer> single = Flowable.just(1).last(2);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithEmpty() {
        Single<Integer> single = Flowable.<Integer>empty().last(1);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithPredicate() {
        Single<Integer> single = Flowable.just(1, 2, 3, 4, 5, 6).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).last(8);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(6);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithPredicateAndOneElement() {
        Single<Integer> single = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).last(4);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrDefaultWithPredicateAndEmpty() {
        Single<Integer> single = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).last(2);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        // inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void lastOrErrorNoElement() {
        Flowable.empty().lastOrError().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void lastOrErrorOneElement() {
        Flowable.just(1).lastOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void lastOrErrorMultipleElements() {
        Flowable.just(1, 2, 3).lastOrError().test().assertNoErrors().assertValue(3);
    }

    @Test
    public void lastOrErrorError() {
        Flowable.error(new RuntimeException("error")).lastOrError().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.never().lastElement().toFlowable());
        TestHelper.checkDisposed(Flowable.never().lastElement());
        TestHelper.checkDisposed(Flowable.just(1).lastOrError().toFlowable());
        TestHelper.checkDisposed(Flowable.just(1).lastOrError());
        TestHelper.checkDisposed(Flowable.just(1).last(2).toFlowable());
        TestHelper.checkDisposed(Flowable.just(1).last(2));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToMaybe(new Function<Flowable<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Flowable<Object> f) throws Exception {
                return f.lastElement();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.lastElement().toFlowable();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Flowable<Object> f) throws Exception {
                return f.lastOrError();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.lastOrError().toFlowable();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Flowable<Object> f) throws Exception {
                return f.last(2);
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.last(2).toFlowable();
            }
        });
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).lastElement().test().assertFailure(TestException.class);
    }

    @Test
    public void errorLastOrErrorFlowable() {
        Flowable.error(new TestException()).lastOrError().toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void emptyLastOrErrorFlowable() {
        Flowable.empty().lastOrError().toFlowable().test().assertFailure(NoSuchElementException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableLastTest instance;

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
        public void benchmark_lastViaFlowable() throws java.lang.Throwable {
            this.payloads.lastViaFlowable.evaluate();
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
        public void benchmark_errorLastOrErrorFlowable() throws java.lang.Throwable {
            this.payloads.errorLastOrErrorFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyLastOrErrorFlowable() throws java.lang.Throwable {
            this.payloads.emptyLastOrErrorFlowable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableLastTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableLastTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableLastTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableLastTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableLastTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableLastTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableLastTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableLastTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement lastWithElements;

            public org.junit.runners.model.Statement lastWithNoElements;

            public org.junit.runners.model.Statement lastMultiSubscribe;

            public org.junit.runners.model.Statement lastViaFlowable;

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

            public org.junit.runners.model.Statement errorLastOrErrorFlowable;

            public org.junit.runners.model.Statement emptyLastOrErrorFlowable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.lastWithElements = _ClassStatement.forPayload(FlowableLastTest::lastWithElements, "lastWithElements", this);
            this.payloads.lastWithNoElements = _ClassStatement.forPayload(FlowableLastTest::lastWithNoElements, "lastWithNoElements", this);
            this.payloads.lastMultiSubscribe = _ClassStatement.forPayload(FlowableLastTest::lastMultiSubscribe, "lastMultiSubscribe", this);
            this.payloads.lastViaFlowable = _ClassStatement.forPayload(FlowableLastTest::lastViaFlowable, "lastViaFlowable", this);
            this.payloads.last = _ClassStatement.forPayload(FlowableLastTest::last, "last", this);
            this.payloads.lastWithOneElement = _ClassStatement.forPayload(FlowableLastTest::lastWithOneElement, "lastWithOneElement", this);
            this.payloads.lastWithEmpty = _ClassStatement.forPayload(FlowableLastTest::lastWithEmpty, "lastWithEmpty", this);
            this.payloads.lastWithPredicate = _ClassStatement.forPayload(FlowableLastTest::lastWithPredicate, "lastWithPredicate", this);
            this.payloads.lastWithPredicateAndOneElement = _ClassStatement.forPayload(FlowableLastTest::lastWithPredicateAndOneElement, "lastWithPredicateAndOneElement", this);
            this.payloads.lastWithPredicateAndEmpty = _ClassStatement.forPayload(FlowableLastTest::lastWithPredicateAndEmpty, "lastWithPredicateAndEmpty", this);
            this.payloads.lastOrDefault = _ClassStatement.forPayload(FlowableLastTest::lastOrDefault, "lastOrDefault", this);
            this.payloads.lastOrDefaultWithOneElement = _ClassStatement.forPayload(FlowableLastTest::lastOrDefaultWithOneElement, "lastOrDefaultWithOneElement", this);
            this.payloads.lastOrDefaultWithEmpty = _ClassStatement.forPayload(FlowableLastTest::lastOrDefaultWithEmpty, "lastOrDefaultWithEmpty", this);
            this.payloads.lastOrDefaultWithPredicate = _ClassStatement.forPayload(FlowableLastTest::lastOrDefaultWithPredicate, "lastOrDefaultWithPredicate", this);
            this.payloads.lastOrDefaultWithPredicateAndOneElement = _ClassStatement.forPayload(FlowableLastTest::lastOrDefaultWithPredicateAndOneElement, "lastOrDefaultWithPredicateAndOneElement", this);
            this.payloads.lastOrDefaultWithPredicateAndEmpty = _ClassStatement.forPayload(FlowableLastTest::lastOrDefaultWithPredicateAndEmpty, "lastOrDefaultWithPredicateAndEmpty", this);
            this.payloads.lastOrErrorNoElement = _ClassStatement.forPayload(FlowableLastTest::lastOrErrorNoElement, "lastOrErrorNoElement", this);
            this.payloads.lastOrErrorOneElement = _ClassStatement.forPayload(FlowableLastTest::lastOrErrorOneElement, "lastOrErrorOneElement", this);
            this.payloads.lastOrErrorMultipleElements = _ClassStatement.forPayload(FlowableLastTest::lastOrErrorMultipleElements, "lastOrErrorMultipleElements", this);
            this.payloads.lastOrErrorError = _ClassStatement.forPayload(FlowableLastTest::lastOrErrorError, "lastOrErrorError", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableLastTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableLastTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableLastTest::error, "error", this);
            this.payloads.errorLastOrErrorFlowable = _ClassStatement.forPayload(FlowableLastTest::errorLastOrErrorFlowable, "errorLastOrErrorFlowable", this);
            this.payloads.emptyLastOrErrorFlowable = _ClassStatement.forPayload(FlowableLastTest::emptyLastOrErrorFlowable, "emptyLastOrErrorFlowable", this);
        }
    }
}
