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

public class ObservableRangeLongTest extends RxJavaTest {

    @Test
    public void rangeStartAt2Count3() {
        Observer<Long> observer = TestHelper.mockObserver();
        Observable.rangeLong(2, 3).subscribe(observer);
        verify(observer, times(1)).onNext(2L);
        verify(observer, times(1)).onNext(3L);
        verify(observer, times(1)).onNext(4L);
        verify(observer, never()).onNext(5L);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void rangeUnsubscribe() {
        Observer<Long> observer = TestHelper.mockObserver();
        final AtomicInteger count = new AtomicInteger();
        Observable.rangeLong(1, 1000).doOnNext(new Consumer<Long>() {

            @Override
            public void accept(Long t1) {
                count.incrementAndGet();
            }
        }).take(3).subscribe(observer);
        verify(observer, times(1)).onNext(1L);
        verify(observer, times(1)).onNext(2L);
        verify(observer, times(1)).onNext(3L);
        verify(observer, never()).onNext(4L);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
        assertEquals(3, count.get());
    }

    @Test
    public void rangeWithZero() {
        Observable.rangeLong(1L, 0L);
    }

    @Test
    public void rangeWithOverflow2() {
        Observable.rangeLong(Long.MAX_VALUE, 0L);
    }

    @Test
    public void rangeWithOverflow3() {
        Observable.rangeLong(1L, Long.MAX_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeWithOverflow4() {
        Observable.rangeLong(2L, Long.MAX_VALUE);
    }

    @Test
    public void rangeWithOverflow5() {
        assertFalse(Observable.rangeLong(Long.MIN_VALUE, 0).blockingIterable().iterator().hasNext());
    }

    @Test
    public void noBackpressure() {
        ArrayList<Long> list = new ArrayList<>(Flowable.bufferSize() * 2);
        for (long i = 1; i <= Flowable.bufferSize() * 2 + 1; i++) {
            list.add(i);
        }
        Observable<Long> o = Observable.rangeLong(1, list.size());
        TestObserverEx<Long> to = new TestObserverEx<>();
        o.subscribe(to);
        to.assertValueSequence(list);
        to.assertTerminated();
    }

    @Test
    public void emptyRangeSendsOnCompleteEagerlyWithRequestZero() {
        final AtomicBoolean completed = new AtomicBoolean(false);
        Observable.rangeLong(1L, 0L).subscribe(new DefaultObserver<Long>() {

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
            public void onNext(Long t) {
            }
        });
        assertTrue(completed.get());
    }

    @Test
    public void nearMaxValueWithoutBackpressure() {
        TestObserver<Long> to = new TestObserver<>();
        Observable.rangeLong(Long.MAX_VALUE - 1L, 2L).subscribe(to);
        to.assertComplete();
        to.assertNoErrors();
        to.assertValues(Long.MAX_VALUE - 1, Long.MAX_VALUE);
    }

    @Test
    public void negativeCount() {
        try {
            Observable.rangeLong(1L, -1L);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("count >= 0 required but it was -1", ex.getMessage());
        }
    }

    @Test
    public void countOne() {
        Observable.rangeLong(5495454L, 1L).test().assertResult(5495454L);
    }

    @Test
    public void noOverflow() {
        Observable.rangeLong(Long.MAX_VALUE - 1, 2);
        Observable.rangeLong(Long.MIN_VALUE, 2);
        Observable.rangeLong(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Test
    public void fused() {
        TestObserverEx<Long> to = new TestObserverEx<>(QueueFuseable.ANY);
        Observable.rangeLong(1, 2).subscribe(to);
        to.assertFusionMode(QueueFuseable.SYNC).assertResult(1L, 2L);
    }

    @Test
    public void fusedReject() {
        TestObserverEx<Long> to = new TestObserverEx<>(QueueFuseable.ASYNC);
        Observable.rangeLong(1, 2).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1L, 2L);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Observable.rangeLong(1, 2));
    }

    @Test
    public void fusedClearIsEmpty() {
        TestHelper.checkFusedIsEmptyClear(Observable.rangeLong(1, 2));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableRangeLongTest instance;

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
        public void benchmark_countOne() throws java.lang.Throwable {
            this.payloads.countOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noOverflow() throws java.lang.Throwable {
            this.payloads.noOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.payloads.fused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedReject() throws java.lang.Throwable {
            this.payloads.fusedReject.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedClearIsEmpty() throws java.lang.Throwable {
            this.payloads.fusedClearIsEmpty.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRangeLongTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRangeLongTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRangeLongTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRangeLongTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableRangeLongTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRangeLongTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableRangeLongTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableRangeLongTest.class, name);
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

            public org.junit.runners.model.Statement countOne;

            public org.junit.runners.model.Statement noOverflow;

            public org.junit.runners.model.Statement fused;

            public org.junit.runners.model.Statement fusedReject;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement fusedClearIsEmpty;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.rangeStartAt2Count3 = _ClassStatement.forPayload(ObservableRangeLongTest::rangeStartAt2Count3, "rangeStartAt2Count3", this);
            this.payloads.rangeUnsubscribe = _ClassStatement.forPayload(ObservableRangeLongTest::rangeUnsubscribe, "rangeUnsubscribe", this);
            this.payloads.rangeWithZero = _ClassStatement.forPayload(ObservableRangeLongTest::rangeWithZero, "rangeWithZero", this);
            this.payloads.rangeWithOverflow2 = _ClassStatement.forPayload(ObservableRangeLongTest::rangeWithOverflow2, "rangeWithOverflow2", this);
            this.payloads.rangeWithOverflow3 = _ClassStatement.forPayload(ObservableRangeLongTest::rangeWithOverflow3, "rangeWithOverflow3", this);
            this.payloads.rangeWithOverflow4 = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableRangeLongTest::rangeWithOverflow4, java.lang.IllegalArgumentException.class), "rangeWithOverflow4", this);
            this.payloads.rangeWithOverflow5 = _ClassStatement.forPayload(ObservableRangeLongTest::rangeWithOverflow5, "rangeWithOverflow5", this);
            this.payloads.noBackpressure = _ClassStatement.forPayload(ObservableRangeLongTest::noBackpressure, "noBackpressure", this);
            this.payloads.emptyRangeSendsOnCompleteEagerlyWithRequestZero = _ClassStatement.forPayload(ObservableRangeLongTest::emptyRangeSendsOnCompleteEagerlyWithRequestZero, "emptyRangeSendsOnCompleteEagerlyWithRequestZero", this);
            this.payloads.nearMaxValueWithoutBackpressure = _ClassStatement.forPayload(ObservableRangeLongTest::nearMaxValueWithoutBackpressure, "nearMaxValueWithoutBackpressure", this);
            this.payloads.negativeCount = _ClassStatement.forPayload(ObservableRangeLongTest::negativeCount, "negativeCount", this);
            this.payloads.countOne = _ClassStatement.forPayload(ObservableRangeLongTest::countOne, "countOne", this);
            this.payloads.noOverflow = _ClassStatement.forPayload(ObservableRangeLongTest::noOverflow, "noOverflow", this);
            this.payloads.fused = _ClassStatement.forPayload(ObservableRangeLongTest::fused, "fused", this);
            this.payloads.fusedReject = _ClassStatement.forPayload(ObservableRangeLongTest::fusedReject, "fusedReject", this);
            this.payloads.disposed = _ClassStatement.forPayload(ObservableRangeLongTest::disposed, "disposed", this);
            this.payloads.fusedClearIsEmpty = _ClassStatement.forPayload(ObservableRangeLongTest::fusedClearIsEmpty, "fusedClearIsEmpty", this);
        }
    }
}
