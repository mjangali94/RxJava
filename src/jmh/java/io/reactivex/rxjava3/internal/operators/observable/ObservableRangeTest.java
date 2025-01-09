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
import java.util.ArrayList;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.*;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableRangeTest extends RxJavaTest {

    @Test
    public void rangeStartAt2Count3() {
        Observer<Integer> observer = TestHelper.mockObserver();
        Observable.range(2, 3).subscribe(observer);
        verify(observer, times(1)).onNext(2);
        verify(observer, times(1)).onNext(3);
        verify(observer, times(1)).onNext(4);
        verify(observer, never()).onNext(5);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void rangeUnsubscribe() {
        Observer<Integer> observer = TestHelper.mockObserver();
        final AtomicInteger count = new AtomicInteger();
        Observable.range(1, 1000).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                count.incrementAndGet();
            }
        }).take(3).subscribe(observer);
        verify(observer, times(1)).onNext(1);
        verify(observer, times(1)).onNext(2);
        verify(observer, times(1)).onNext(3);
        verify(observer, never()).onNext(4);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
        assertEquals(3, count.get());
    }

    @Test
    public void rangeWithZero() {
        Observable.range(1, 0);
    }

    @Test
    public void rangeWithOverflow2() {
        Observable.range(Integer.MAX_VALUE, 0);
    }

    @Test
    public void rangeWithOverflow3() {
        Observable.range(1, Integer.MAX_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeWithOverflow4() {
        Observable.range(2, Integer.MAX_VALUE);
    }

    @Test
    public void rangeWithOverflow5() {
        assertFalse(Observable.range(Integer.MIN_VALUE, 0).blockingIterable().iterator().hasNext());
    }

    @Test
    public void noBackpressure() {
        ArrayList<Integer> list = new ArrayList<>(Flowable.bufferSize() * 2);
        for (int i = 1; i <= Flowable.bufferSize() * 2 + 1; i++) {
            list.add(i);
        }
        Observable<Integer> o = Observable.range(1, list.size());
        TestObserverEx<Integer> to = new TestObserverEx<>();
        o.subscribe(to);
        to.assertValueSequence(list);
        to.assertTerminated();
    }

    @Test
    public void emptyRangeSendsOnCompleteEagerlyWithRequestZero() {
        final AtomicBoolean completed = new AtomicBoolean(false);
        Observable.range(1, 0).subscribe(new DefaultObserver<Integer>() {

            @Override
            public void onStart() {
            // request(0);
            }

            @Override
            public void onComplete() {
                completed.set(true);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
            }
        });
        assertTrue(completed.get());
    }

    @Test
    public void nearMaxValueWithoutBackpressure() {
        TestObserver<Integer> to = new TestObserver<>();
        Observable.range(Integer.MAX_VALUE - 1, 2).subscribe(to);
        to.assertComplete();
        to.assertNoErrors();
        to.assertValues(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
    }

    @Test
    public void negativeCount() {
        try {
            Observable.range(1, -1);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("count >= 0 required but it was -1", ex.getMessage());
        }
    }

    @Test
    public void requestWrongFusion() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ASYNC);
        Observable.range(1, 5).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableRangeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeStartAt2Count3() throws java.lang.Throwable {
            this.payloads.rangeStartAt2Count3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeUnsubscribe() throws java.lang.Throwable {
            this.payloads.rangeUnsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeWithZero() throws java.lang.Throwable {
            this.payloads.rangeWithZero.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeWithOverflow2() throws java.lang.Throwable {
            this.payloads.rangeWithOverflow2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeWithOverflow3() throws java.lang.Throwable {
            this.payloads.rangeWithOverflow3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeWithOverflow4() throws java.lang.Throwable {
            this.payloads.rangeWithOverflow4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeWithOverflow5() throws java.lang.Throwable {
            this.payloads.rangeWithOverflow5.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noBackpressure() throws java.lang.Throwable {
            this.payloads.noBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyRangeSendsOnCompleteEagerlyWithRequestZero() throws java.lang.Throwable {
            this.payloads.emptyRangeSendsOnCompleteEagerlyWithRequestZero.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nearMaxValueWithoutBackpressure() throws java.lang.Throwable {
            this.payloads.nearMaxValueWithoutBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_negativeCount() throws java.lang.Throwable {
            this.payloads.negativeCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestWrongFusion() throws java.lang.Throwable {
            this.payloads.requestWrongFusion.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRangeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRangeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRangeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRangeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableRangeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRangeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableRangeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableRangeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement rangeStartAt2Count3;

            public org.junit.runners.model.Statement rangeUnsubscribe;

            public org.junit.runners.model.Statement rangeWithZero;

            public org.junit.runners.model.Statement rangeWithOverflow2;

            public org.junit.runners.model.Statement rangeWithOverflow3;

            public org.junit.runners.model.Statement rangeWithOverflow4;

            public org.junit.runners.model.Statement rangeWithOverflow5;

            public org.junit.runners.model.Statement noBackpressure;

            public org.junit.runners.model.Statement emptyRangeSendsOnCompleteEagerlyWithRequestZero;

            public org.junit.runners.model.Statement nearMaxValueWithoutBackpressure;

            public org.junit.runners.model.Statement negativeCount;

            public org.junit.runners.model.Statement requestWrongFusion;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.rangeStartAt2Count3 = _ClassStatement.forPayload(ObservableRangeTest::rangeStartAt2Count3, "rangeStartAt2Count3", this);
            this.payloads.rangeUnsubscribe = _ClassStatement.forPayload(ObservableRangeTest::rangeUnsubscribe, "rangeUnsubscribe", this);
            this.payloads.rangeWithZero = _ClassStatement.forPayload(ObservableRangeTest::rangeWithZero, "rangeWithZero", this);
            this.payloads.rangeWithOverflow2 = _ClassStatement.forPayload(ObservableRangeTest::rangeWithOverflow2, "rangeWithOverflow2", this);
            this.payloads.rangeWithOverflow3 = _ClassStatement.forPayload(ObservableRangeTest::rangeWithOverflow3, "rangeWithOverflow3", this);
            this.payloads.rangeWithOverflow4 = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableRangeTest::rangeWithOverflow4, java.lang.IllegalArgumentException.class), "rangeWithOverflow4", this);
            this.payloads.rangeWithOverflow5 = _ClassStatement.forPayload(ObservableRangeTest::rangeWithOverflow5, "rangeWithOverflow5", this);
            this.payloads.noBackpressure = _ClassStatement.forPayload(ObservableRangeTest::noBackpressure, "noBackpressure", this);
            this.payloads.emptyRangeSendsOnCompleteEagerlyWithRequestZero = _ClassStatement.forPayload(ObservableRangeTest::emptyRangeSendsOnCompleteEagerlyWithRequestZero, "emptyRangeSendsOnCompleteEagerlyWithRequestZero", this);
            this.payloads.nearMaxValueWithoutBackpressure = _ClassStatement.forPayload(ObservableRangeTest::nearMaxValueWithoutBackpressure, "nearMaxValueWithoutBackpressure", this);
            this.payloads.negativeCount = _ClassStatement.forPayload(ObservableRangeTest::negativeCount, "negativeCount", this);
            this.payloads.requestWrongFusion = _ClassStatement.forPayload(ObservableRangeTest::requestWrongFusion, "requestWrongFusion", this);
        }
    }
}
