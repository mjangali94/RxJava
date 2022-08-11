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
import java.util.*;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableElementAtTest extends RxJavaTest {

    @Test
    public void elementAtFlowable() {
        assertEquals(2, Flowable.fromArray(1, 2).elementAt(1).toFlowable().blockingSingle().intValue());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtWithMinusIndexFlowable() {
        Flowable.fromArray(1, 2).elementAt(-1);
    }

    @Test
    public void elementAtWithIndexOutOfBoundsFlowable() {
        assertEquals(-100, Flowable.fromArray(1, 2).elementAt(2).toFlowable().blockingFirst(-100).intValue());
    }

    @Test
    public void elementAtOrDefaultFlowable() {
        assertEquals(2, Flowable.fromArray(1, 2).elementAt(1, 0).toFlowable().blockingSingle().intValue());
    }

    @Test
    public void elementAtOrDefaultWithIndexOutOfBoundsFlowable() {
        assertEquals(0, Flowable.fromArray(1, 2).elementAt(2, 0).toFlowable().blockingSingle().intValue());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtOrDefaultWithMinusIndexFlowable() {
        Flowable.fromArray(1, 2).elementAt(-1, 0);
    }

    @Test
    public void elementAt() {
        assertEquals(2, Flowable.fromArray(1, 2).elementAt(1).blockingGet().intValue());
    }

    @Test
    public void elementAtConstrainsUpstreamRequests() {
        final List<Long> requests = new ArrayList<>();
        Flowable.fromArray(1, 2, 3, 4).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long n) throws Throwable {
                requests.add(n);
            }
        }).elementAt(2).blockingGet().intValue();
        assertEquals(Arrays.asList(3L), requests);
    }

    @Test
    public void elementAtWithDefaultConstrainsUpstreamRequests() {
        final List<Long> requests = new ArrayList<>();
        Flowable.fromArray(1, 2, 3, 4).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long n) throws Throwable {
                requests.add(n);
            }
        }).elementAt(2, 100).blockingGet().intValue();
        assertEquals(Arrays.asList(3L), requests);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtWithMinusIndex() {
        Flowable.fromArray(1, 2).elementAt(-1);
    }

    @Test
    public void elementAtWithIndexOutOfBounds() {
        assertNull(Flowable.fromArray(1, 2).elementAt(2).blockingGet());
    }

    @Test
    public void elementAtOrDefault() {
        assertEquals(2, Flowable.fromArray(1, 2).elementAt(1, 0).blockingGet().intValue());
    }

    @Test
    public void elementAtOrDefaultWithIndexOutOfBounds() {
        assertEquals(0, Flowable.fromArray(1, 2).elementAt(2, 0).blockingGet().intValue());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtOrDefaultWithMinusIndex() {
        Flowable.fromArray(1, 2).elementAt(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void elementAtOrErrorNegativeIndex() {
        Flowable.empty().elementAtOrError(-1);
    }

    @Test
    public void elementAtOrErrorNoElement() {
        Flowable.empty().elementAtOrError(0).test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void elementAtOrErrorOneElement() {
        Flowable.just(1).elementAtOrError(0).test().assertNoErrors().assertValue(1);
    }

    @Test
    public void elementAtOrErrorMultipleElements() {
        Flowable.just(1, 2, 3).elementAtOrError(1).test().assertNoErrors().assertValue(2);
    }

    @Test
    public void elementAtOrErrorInvalidIndex() {
        Flowable.just(1, 2, 3).elementAtOrError(3).test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void elementAtOrErrorError() {
        Flowable.error(new RuntimeException("error")).elementAtOrError(0).to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void elementAtIndex0OnEmptySource() {
        Flowable.empty().elementAt(0).test().assertResult();
    }

    @Test
    public void elementAtIndex0WithDefaultOnEmptySource() {
        Flowable.empty().elementAt(0, 5).test().assertResult(5);
    }

    @Test
    public void elementAtIndex1OnEmptySource() {
        Flowable.empty().elementAt(1).test().assertResult();
    }

    @Test
    public void elementAtIndex1WithDefaultOnEmptySource() {
        Flowable.empty().elementAt(1, 10).test().assertResult(10);
    }

    @Test
    public void elementAtOrErrorIndex1OnEmptySource() {
        Flowable.empty().elementAtOrError(1).test().assertFailure(NoSuchElementException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.elementAt(0).toFlowable();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToMaybe(new Function<Flowable<Object>, Maybe<Object>>() {

            @Override
            public Maybe<Object> apply(Flowable<Object> f) throws Exception {
                return f.elementAt(0);
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Object>, Single<Object>>() {

            @Override
            public Single<Object> apply(Flowable<Object> f) throws Exception {
                return f.elementAt(0, 1);
            }
        });
    }

    @Test
    public void elementAtIndex1WithDefaultOnEmptySourceObservable() {
        Flowable.empty().elementAt(1, 10).toFlowable().test().assertResult(10);
    }

    @Test
    public void errorFlowable() {
        Flowable.error(new TestException()).elementAt(1, 10).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).elementAt(1, 10).test().assertFailure(TestException.class);
        Flowable.error(new TestException()).elementAt(1).test().assertFailure(TestException.class);
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onNext(2);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                }
            }.elementAt(0).toFlowable().test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.elementAt(0);
            }
        }, false, null, 1);
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.elementAt(0, 1);
            }
        }, false, null, 1, 1);
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.elementAt(0).toFlowable();
            }
        }, false, null, 1);
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.elementAt(0, 1).toFlowable();
            }
        }, false, null, 1, 1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().elementAt(0).toFlowable());
        TestHelper.checkDisposed(PublishProcessor.create().elementAt(0, 1).toFlowable());
        TestHelper.checkDisposed(PublishProcessor.create().elementAt(0));
        TestHelper.checkDisposed(PublishProcessor.create().elementAt(0, 1));
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
            }.elementAt(0).toFlowable().test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSource2() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onNext(2);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                }
            }.elementAt(0, 1).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableElementAtTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtFlowable() throws java.lang.Throwable {
            this.payloads.elementAtFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtWithMinusIndexFlowable() throws java.lang.Throwable {
            this.payloads.elementAtWithMinusIndexFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtWithIndexOutOfBoundsFlowable() throws java.lang.Throwable {
            this.payloads.elementAtWithIndexOutOfBoundsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefaultFlowable() throws java.lang.Throwable {
            this.payloads.elementAtOrDefaultFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefaultWithIndexOutOfBoundsFlowable() throws java.lang.Throwable {
            this.payloads.elementAtOrDefaultWithIndexOutOfBoundsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtOrDefaultWithMinusIndexFlowable() throws java.lang.Throwable {
            this.payloads.elementAtOrDefaultWithMinusIndexFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAt() throws java.lang.Throwable {
            this.payloads.elementAt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtConstrainsUpstreamRequests() throws java.lang.Throwable {
            this.payloads.elementAtConstrainsUpstreamRequests.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtWithDefaultConstrainsUpstreamRequests() throws java.lang.Throwable {
            this.payloads.elementAtWithDefaultConstrainsUpstreamRequests.evaluate();
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
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_elementAtIndex1WithDefaultOnEmptySourceObservable() throws java.lang.Throwable {
            this.payloads.elementAtIndex1WithDefaultOnEmptySourceObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorFlowable() throws java.lang.Throwable {
            this.payloads.errorFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceObservable() throws java.lang.Throwable {
            this.payloads.badSourceObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource2() throws java.lang.Throwable {
            this.payloads.badSource2.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableElementAtTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableElementAtTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableElementAtTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableElementAtTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableElementAtTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableElementAtTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableElementAtTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableElementAtTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement elementAtFlowable;

            public org.junit.runners.model.Statement elementAtWithMinusIndexFlowable;

            public org.junit.runners.model.Statement elementAtWithIndexOutOfBoundsFlowable;

            public org.junit.runners.model.Statement elementAtOrDefaultFlowable;

            public org.junit.runners.model.Statement elementAtOrDefaultWithIndexOutOfBoundsFlowable;

            public org.junit.runners.model.Statement elementAtOrDefaultWithMinusIndexFlowable;

            public org.junit.runners.model.Statement elementAt;

            public org.junit.runners.model.Statement elementAtConstrainsUpstreamRequests;

            public org.junit.runners.model.Statement elementAtWithDefaultConstrainsUpstreamRequests;

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

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement elementAtIndex1WithDefaultOnEmptySourceObservable;

            public org.junit.runners.model.Statement errorFlowable;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement badSourceObservable;

            public org.junit.runners.model.Statement badSource2;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.elementAtFlowable = _ClassStatement.forPayload(FlowableElementAtTest::elementAtFlowable, "elementAtFlowable", this);
            this.payloads.elementAtWithMinusIndexFlowable = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableElementAtTest::elementAtWithMinusIndexFlowable, java.lang.IndexOutOfBoundsException.class), "elementAtWithMinusIndexFlowable", this);
            this.payloads.elementAtWithIndexOutOfBoundsFlowable = _ClassStatement.forPayload(FlowableElementAtTest::elementAtWithIndexOutOfBoundsFlowable, "elementAtWithIndexOutOfBoundsFlowable", this);
            this.payloads.elementAtOrDefaultFlowable = _ClassStatement.forPayload(FlowableElementAtTest::elementAtOrDefaultFlowable, "elementAtOrDefaultFlowable", this);
            this.payloads.elementAtOrDefaultWithIndexOutOfBoundsFlowable = _ClassStatement.forPayload(FlowableElementAtTest::elementAtOrDefaultWithIndexOutOfBoundsFlowable, "elementAtOrDefaultWithIndexOutOfBoundsFlowable", this);
            this.payloads.elementAtOrDefaultWithMinusIndexFlowable = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableElementAtTest::elementAtOrDefaultWithMinusIndexFlowable, java.lang.IndexOutOfBoundsException.class), "elementAtOrDefaultWithMinusIndexFlowable", this);
            this.payloads.elementAt = _ClassStatement.forPayload(FlowableElementAtTest::elementAt, "elementAt", this);
            this.payloads.elementAtConstrainsUpstreamRequests = _ClassStatement.forPayload(FlowableElementAtTest::elementAtConstrainsUpstreamRequests, "elementAtConstrainsUpstreamRequests", this);
            this.payloads.elementAtWithDefaultConstrainsUpstreamRequests = _ClassStatement.forPayload(FlowableElementAtTest::elementAtWithDefaultConstrainsUpstreamRequests, "elementAtWithDefaultConstrainsUpstreamRequests", this);
            this.payloads.elementAtWithMinusIndex = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableElementAtTest::elementAtWithMinusIndex, java.lang.IndexOutOfBoundsException.class), "elementAtWithMinusIndex", this);
            this.payloads.elementAtWithIndexOutOfBounds = _ClassStatement.forPayload(FlowableElementAtTest::elementAtWithIndexOutOfBounds, "elementAtWithIndexOutOfBounds", this);
            this.payloads.elementAtOrDefault = _ClassStatement.forPayload(FlowableElementAtTest::elementAtOrDefault, "elementAtOrDefault", this);
            this.payloads.elementAtOrDefaultWithIndexOutOfBounds = _ClassStatement.forPayload(FlowableElementAtTest::elementAtOrDefaultWithIndexOutOfBounds, "elementAtOrDefaultWithIndexOutOfBounds", this);
            this.payloads.elementAtOrDefaultWithMinusIndex = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableElementAtTest::elementAtOrDefaultWithMinusIndex, java.lang.IndexOutOfBoundsException.class), "elementAtOrDefaultWithMinusIndex", this);
            this.payloads.elementAtOrErrorNegativeIndex = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableElementAtTest::elementAtOrErrorNegativeIndex, java.lang.IndexOutOfBoundsException.class), "elementAtOrErrorNegativeIndex", this);
            this.payloads.elementAtOrErrorNoElement = _ClassStatement.forPayload(FlowableElementAtTest::elementAtOrErrorNoElement, "elementAtOrErrorNoElement", this);
            this.payloads.elementAtOrErrorOneElement = _ClassStatement.forPayload(FlowableElementAtTest::elementAtOrErrorOneElement, "elementAtOrErrorOneElement", this);
            this.payloads.elementAtOrErrorMultipleElements = _ClassStatement.forPayload(FlowableElementAtTest::elementAtOrErrorMultipleElements, "elementAtOrErrorMultipleElements", this);
            this.payloads.elementAtOrErrorInvalidIndex = _ClassStatement.forPayload(FlowableElementAtTest::elementAtOrErrorInvalidIndex, "elementAtOrErrorInvalidIndex", this);
            this.payloads.elementAtOrErrorError = _ClassStatement.forPayload(FlowableElementAtTest::elementAtOrErrorError, "elementAtOrErrorError", this);
            this.payloads.elementAtIndex0OnEmptySource = _ClassStatement.forPayload(FlowableElementAtTest::elementAtIndex0OnEmptySource, "elementAtIndex0OnEmptySource", this);
            this.payloads.elementAtIndex0WithDefaultOnEmptySource = _ClassStatement.forPayload(FlowableElementAtTest::elementAtIndex0WithDefaultOnEmptySource, "elementAtIndex0WithDefaultOnEmptySource", this);
            this.payloads.elementAtIndex1OnEmptySource = _ClassStatement.forPayload(FlowableElementAtTest::elementAtIndex1OnEmptySource, "elementAtIndex1OnEmptySource", this);
            this.payloads.elementAtIndex1WithDefaultOnEmptySource = _ClassStatement.forPayload(FlowableElementAtTest::elementAtIndex1WithDefaultOnEmptySource, "elementAtIndex1WithDefaultOnEmptySource", this);
            this.payloads.elementAtOrErrorIndex1OnEmptySource = _ClassStatement.forPayload(FlowableElementAtTest::elementAtOrErrorIndex1OnEmptySource, "elementAtOrErrorIndex1OnEmptySource", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableElementAtTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.elementAtIndex1WithDefaultOnEmptySourceObservable = _ClassStatement.forPayload(FlowableElementAtTest::elementAtIndex1WithDefaultOnEmptySourceObservable, "elementAtIndex1WithDefaultOnEmptySourceObservable", this);
            this.payloads.errorFlowable = _ClassStatement.forPayload(FlowableElementAtTest::errorFlowable, "errorFlowable", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableElementAtTest::error, "error", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableElementAtTest::badSource, "badSource", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableElementAtTest::dispose, "dispose", this);
            this.payloads.badSourceObservable = _ClassStatement.forPayload(FlowableElementAtTest::badSourceObservable, "badSourceObservable", this);
            this.payloads.badSource2 = _ClassStatement.forPayload(FlowableElementAtTest::badSource2, "badSource2", this);
        }
    }
}
