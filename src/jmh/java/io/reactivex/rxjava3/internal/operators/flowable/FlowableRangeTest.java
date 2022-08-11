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
import java.util.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableRangeTest extends RxJavaTest {

    @Test
    public void rangeStartAt2Count3() {
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        Flowable.range(2, 3).subscribe(subscriber);
        verify(subscriber, times(1)).onNext(2);
        verify(subscriber, times(1)).onNext(3);
        verify(subscriber, times(1)).onNext(4);
        verify(subscriber, never()).onNext(5);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void rangeUnsubscribe() {
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        final AtomicInteger count = new AtomicInteger();
        Flowable.range(1, 1000).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                count.incrementAndGet();
            }
        }).take(3).subscribe(subscriber);
        verify(subscriber, times(1)).onNext(1);
        verify(subscriber, times(1)).onNext(2);
        verify(subscriber, times(1)).onNext(3);
        verify(subscriber, never()).onNext(4);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
        assertEquals(3, count.get());
    }

    @Test
    public void rangeWithZero() {
        Flowable.range(1, 0);
    }

    @Test
    public void rangeWithOverflow2() {
        Flowable.range(Integer.MAX_VALUE, 0);
    }

    @Test
    public void rangeWithOverflow3() {
        Flowable.range(1, Integer.MAX_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeWithOverflow4() {
        Flowable.range(2, Integer.MAX_VALUE);
    }

    @Test
    public void rangeWithOverflow5() {
        assertFalse(Flowable.range(Integer.MIN_VALUE, 0).blockingIterable().iterator().hasNext());
    }

    @Test
    public void backpressureViaRequest() {
        Flowable<Integer> f = Flowable.range(1, Flowable.bufferSize());
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0L);
        ts.assertNoValues();
        ts.request(1);
        f.subscribe(ts);
        ts.assertValue(1);
        ts.request(2);
        ts.assertValues(1, 2, 3);
        ts.request(3);
        ts.assertValues(1, 2, 3, 4, 5, 6);
        ts.request(Flowable.bufferSize());
        ts.assertTerminated();
    }

    @Test
    public void noBackpressure() {
        ArrayList<Integer> list = new ArrayList<>(Flowable.bufferSize() * 2);
        for (int i = 1; i <= Flowable.bufferSize() * 2 + 1; i++) {
            list.add(i);
        }
        Flowable<Integer> f = Flowable.range(1, list.size());
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0L);
        ts.assertNoValues();
        // infinite
        ts.request(Long.MAX_VALUE);
        f.subscribe(ts);
        ts.assertValueSequence(list);
        ts.assertTerminated();
    }

    void withBackpressureOneByOne(int start) {
        Flowable<Integer> source = Flowable.range(start, 100);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0L);
        ts.request(1);
        source.subscribe(ts);
        List<Integer> list = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            list.add(i + start);
            ts.request(1);
        }
        ts.assertValueSequence(list);
        ts.assertTerminated();
    }

    void withBackpressureAllAtOnce(int start) {
        Flowable<Integer> source = Flowable.range(start, 100);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0L);
        ts.request(100);
        source.subscribe(ts);
        List<Integer> list = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            list.add(i + start);
        }
        ts.assertValueSequence(list);
        ts.assertTerminated();
    }

    @Test
    public void withBackpressure1() {
        for (int i = 0; i < 100; i++) {
            withBackpressureOneByOne(i);
        }
    }

    @Test
    public void withBackpressureAllAtOnce() {
        for (int i = 0; i < 100; i++) {
            withBackpressureAllAtOnce(i);
        }
    }

    @Test
    public void withBackpressureRequestWayMore() {
        Flowable<Integer> source = Flowable.range(50, 100);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0L);
        ts.request(150);
        source.subscribe(ts);
        List<Integer> list = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            list.add(i + 50);
        }
        // and then some
        ts.request(50);
        ts.assertValueSequence(list);
        ts.assertTerminated();
    }

    @Test
    public void requestOverflow() {
        final AtomicInteger count = new AtomicInteger();
        int n = 10;
        Flowable.range(1, n).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(2);
            }

            @Override
            public void onComplete() {
            // do nothing
            }

            @Override
            public void onError(Throwable e) {
                throw new RuntimeException(e);
            }

            @Override
            public void onNext(Integer t) {
                count.incrementAndGet();
                request(Long.MAX_VALUE - 1);
            }
        });
        assertEquals(n, count.get());
    }

    @Test
    public void emptyRangeSendsOnCompleteEagerlyWithRequestZero() {
        final AtomicBoolean completed = new AtomicBoolean(false);
        Flowable.range(1, 0).subscribe(new DefaultSubscriber<Integer>() {

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
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(Integer.MAX_VALUE - 1, 2).subscribe(ts);
        ts.assertComplete();
        ts.assertNoErrors();
        ts.assertValues(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
    }

    @Test
    public void nearMaxValueWithBackpressure() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(3L);
        Flowable.range(Integer.MAX_VALUE - 1, 2).subscribe(ts);
        ts.assertComplete();
        ts.assertNoErrors();
        ts.assertValues(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
    }

    @Test
    public void negativeCount() {
        try {
            Flowable.range(1, -1);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("count >= 0 required but it was -1", ex.getMessage());
        }
    }

    @Test
    public void requestWrongFusion() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC);
        Flowable.range(1, 5).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void countOne() {
        Flowable.range(5495454, 1).test().assertResult(5495454);
    }

    @Test
    public void fused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.range(1, 2).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2);
    }

    @Test
    public void fusedReject() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC);
        Flowable.range(1, 2).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Flowable.range(1, 2));
    }

    @Test
    public void fusedClearIsEmpty() {
        TestHelper.checkFusedIsEmptyClear(Flowable.range(1, 2));
    }

    @Test
    public void noOverflow() {
        Flowable.range(Integer.MAX_VALUE - 1, 2);
        Flowable.range(Integer.MIN_VALUE, 2);
        Flowable.range(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Test
    public void conditionalNormal() {
        Flowable.range(1, 5).filter(Functions.alwaysTrue()).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.range(1, 5));
        TestHelper.assertBadRequestReported(Flowable.range(1, 5).filter(Functions.alwaysTrue()));
    }

    @Test
    public void conditionalNormalSlowpath() {
        Flowable.range(1, 5).filter(Functions.alwaysTrue()).test(5).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void conditionalSlowPathTakeExact() {
        Flowable.range(1, 5).filter(Functions.alwaysTrue()).take(5).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void slowPathTakeExact() {
        Flowable.range(1, 5).filter(Functions.alwaysTrue()).take(5).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void conditionalSlowPathRebatch() {
        Flowable.range(1, 5).filter(Functions.alwaysTrue()).rebatchRequests(1).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void slowPathRebatch() {
        Flowable.range(1, 5).rebatchRequests(1).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void slowPathCancel() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>(2L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                cancel();
                onComplete();
            }
        };
        Flowable.range(1, 5).subscribe(ts);
        ts.assertResult(1);
    }

    @Test
    public void fastPathCancel() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                cancel();
                onComplete();
            }
        };
        Flowable.range(1, 5).subscribe(ts);
        ts.assertResult(1);
    }

    @Test
    public void conditionalSlowPathCancel() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>(1L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                cancel();
                onComplete();
            }
        };
        Flowable.range(1, 5).filter(Functions.alwaysTrue()).subscribe(ts);
        ts.assertResult(1);
    }

    @Test
    public void conditionalFastPathCancel() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                cancel();
                onComplete();
            }
        };
        Flowable.range(1, 5).filter(Functions.alwaysTrue()).subscribe(ts);
        ts.assertResult(1);
    }

    @Test
    public void conditionalRequestOneByOne() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>(1L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                request(1);
            }
        };
        Flowable.range(1, 5).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).subscribe(ts);
        ts.assertResult(2, 4);
    }

    @Test
    public void conditionalRequestOneByOne2() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>(1L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                request(1);
            }
        };
        Flowable.range(1, 5).filter(Functions.alwaysTrue()).subscribe(ts);
        ts.assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void fastPathCancelExact() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 5L) {
                    cancel();
                    onComplete();
                }
            }
        };
        Flowable.range(1, 5).subscribe(ts);
        ts.assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void conditionalFastPathCancelExact() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 5L) {
                    cancel();
                    onComplete();
                }
            }
        };
        Flowable.range(1, 5).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).subscribe(ts);
        ts.assertResult(2, 4);
    }

    @Test
    public void conditionalCancel1() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>(2L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    cancel();
                    onComplete();
                }
            }
        };
        Flowable.range(1, 2).filter(Functions.alwaysTrue()).subscribe(ts);
        ts.assertResult(1);
    }

    @Test
    public void conditionalCancel2() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>(2L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 2) {
                    cancel();
                    onComplete();
                }
            }
        };
        Flowable.range(1, 2).filter(Functions.alwaysTrue()).subscribe(ts);
        ts.assertResult(1, 2);
    }

    @Test
    public void slowPathCancelBeforeComplete() {
        Flowable.range(1, 2).take(2).test().assertResult(1, 2);
    }

    @Test
    public void conditionalFastPatchCancelBeforeComplete() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 2).compose(TestHelper.conditional()).doOnNext(v -> {
            if (v == 2) {
                ts.cancel();
            }
        }).subscribe(ts);
        ts.assertValuesOnly(1, 2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableRangeTest instance;

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
        public void benchmark_backpressureViaRequest() throws java.lang.Throwable {
            this.payloads.backpressureViaRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noBackpressure() throws java.lang.Throwable {
            this.payloads.noBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withBackpressure1() throws java.lang.Throwable {
            this.payloads.withBackpressure1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withBackpressureAllAtOnce() throws java.lang.Throwable {
            this.payloads.withBackpressureAllAtOnce.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withBackpressureRequestWayMore() throws java.lang.Throwable {
            this.payloads.withBackpressureRequestWayMore.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestOverflow() throws java.lang.Throwable {
            this.payloads.requestOverflow.evaluate();
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
        public void benchmark_nearMaxValueWithBackpressure() throws java.lang.Throwable {
            this.payloads.nearMaxValueWithBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_negativeCount() throws java.lang.Throwable {
            this.payloads.negativeCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestWrongFusion() throws java.lang.Throwable {
            this.payloads.requestWrongFusion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_countOne() throws java.lang.Throwable {
            this.payloads.countOne.evaluate();
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

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noOverflow() throws java.lang.Throwable {
            this.payloads.noOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalNormal() throws java.lang.Throwable {
            this.payloads.conditionalNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalNormalSlowpath() throws java.lang.Throwable {
            this.payloads.conditionalNormalSlowpath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalSlowPathTakeExact() throws java.lang.Throwable {
            this.payloads.conditionalSlowPathTakeExact.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_slowPathTakeExact() throws java.lang.Throwable {
            this.payloads.slowPathTakeExact.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalSlowPathRebatch() throws java.lang.Throwable {
            this.payloads.conditionalSlowPathRebatch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_slowPathRebatch() throws java.lang.Throwable {
            this.payloads.slowPathRebatch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_slowPathCancel() throws java.lang.Throwable {
            this.payloads.slowPathCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fastPathCancel() throws java.lang.Throwable {
            this.payloads.fastPathCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalSlowPathCancel() throws java.lang.Throwable {
            this.payloads.conditionalSlowPathCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFastPathCancel() throws java.lang.Throwable {
            this.payloads.conditionalFastPathCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalRequestOneByOne() throws java.lang.Throwable {
            this.payloads.conditionalRequestOneByOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalRequestOneByOne2() throws java.lang.Throwable {
            this.payloads.conditionalRequestOneByOne2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fastPathCancelExact() throws java.lang.Throwable {
            this.payloads.fastPathCancelExact.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFastPathCancelExact() throws java.lang.Throwable {
            this.payloads.conditionalFastPathCancelExact.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalCancel1() throws java.lang.Throwable {
            this.payloads.conditionalCancel1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalCancel2() throws java.lang.Throwable {
            this.payloads.conditionalCancel2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_slowPathCancelBeforeComplete() throws java.lang.Throwable {
            this.payloads.slowPathCancelBeforeComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFastPatchCancelBeforeComplete() throws java.lang.Throwable {
            this.payloads.conditionalFastPatchCancelBeforeComplete.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableRangeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableRangeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableRangeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableRangeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableRangeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableRangeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableRangeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableRangeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement rangeStartAt2Count3;

            public org.junit.runners.model.Statement rangeUnsubscribe;

            public org.junit.runners.model.Statement rangeWithZero;

            public org.junit.runners.model.Statement rangeWithOverflow2;

            public org.junit.runners.model.Statement rangeWithOverflow3;

            public org.junit.runners.model.Statement rangeWithOverflow4;

            public org.junit.runners.model.Statement rangeWithOverflow5;

            public org.junit.runners.model.Statement backpressureViaRequest;

            public org.junit.runners.model.Statement noBackpressure;

            public org.junit.runners.model.Statement withBackpressure1;

            public org.junit.runners.model.Statement withBackpressureAllAtOnce;

            public org.junit.runners.model.Statement withBackpressureRequestWayMore;

            public org.junit.runners.model.Statement requestOverflow;

            public org.junit.runners.model.Statement emptyRangeSendsOnCompleteEagerlyWithRequestZero;

            public org.junit.runners.model.Statement nearMaxValueWithoutBackpressure;

            public org.junit.runners.model.Statement nearMaxValueWithBackpressure;

            public org.junit.runners.model.Statement negativeCount;

            public org.junit.runners.model.Statement requestWrongFusion;

            public org.junit.runners.model.Statement countOne;

            public org.junit.runners.model.Statement fused;

            public org.junit.runners.model.Statement fusedReject;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement fusedClearIsEmpty;

            public org.junit.runners.model.Statement noOverflow;

            public org.junit.runners.model.Statement conditionalNormal;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement conditionalNormalSlowpath;

            public org.junit.runners.model.Statement conditionalSlowPathTakeExact;

            public org.junit.runners.model.Statement slowPathTakeExact;

            public org.junit.runners.model.Statement conditionalSlowPathRebatch;

            public org.junit.runners.model.Statement slowPathRebatch;

            public org.junit.runners.model.Statement slowPathCancel;

            public org.junit.runners.model.Statement fastPathCancel;

            public org.junit.runners.model.Statement conditionalSlowPathCancel;

            public org.junit.runners.model.Statement conditionalFastPathCancel;

            public org.junit.runners.model.Statement conditionalRequestOneByOne;

            public org.junit.runners.model.Statement conditionalRequestOneByOne2;

            public org.junit.runners.model.Statement fastPathCancelExact;

            public org.junit.runners.model.Statement conditionalFastPathCancelExact;

            public org.junit.runners.model.Statement conditionalCancel1;

            public org.junit.runners.model.Statement conditionalCancel2;

            public org.junit.runners.model.Statement slowPathCancelBeforeComplete;

            public org.junit.runners.model.Statement conditionalFastPatchCancelBeforeComplete;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.rangeStartAt2Count3 = _ClassStatement.forPayload(FlowableRangeTest::rangeStartAt2Count3, "rangeStartAt2Count3", this);
            this.payloads.rangeUnsubscribe = _ClassStatement.forPayload(FlowableRangeTest::rangeUnsubscribe, "rangeUnsubscribe", this);
            this.payloads.rangeWithZero = _ClassStatement.forPayload(FlowableRangeTest::rangeWithZero, "rangeWithZero", this);
            this.payloads.rangeWithOverflow2 = _ClassStatement.forPayload(FlowableRangeTest::rangeWithOverflow2, "rangeWithOverflow2", this);
            this.payloads.rangeWithOverflow3 = _ClassStatement.forPayload(FlowableRangeTest::rangeWithOverflow3, "rangeWithOverflow3", this);
            this.payloads.rangeWithOverflow4 = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableRangeTest::rangeWithOverflow4, java.lang.IllegalArgumentException.class), "rangeWithOverflow4", this);
            this.payloads.rangeWithOverflow5 = _ClassStatement.forPayload(FlowableRangeTest::rangeWithOverflow5, "rangeWithOverflow5", this);
            this.payloads.backpressureViaRequest = _ClassStatement.forPayload(FlowableRangeTest::backpressureViaRequest, "backpressureViaRequest", this);
            this.payloads.noBackpressure = _ClassStatement.forPayload(FlowableRangeTest::noBackpressure, "noBackpressure", this);
            this.payloads.withBackpressure1 = _ClassStatement.forPayload(FlowableRangeTest::withBackpressure1, "withBackpressure1", this);
            this.payloads.withBackpressureAllAtOnce = _ClassStatement.forPayload(FlowableRangeTest::withBackpressureAllAtOnce, "withBackpressureAllAtOnce", this);
            this.payloads.withBackpressureRequestWayMore = _ClassStatement.forPayload(FlowableRangeTest::withBackpressureRequestWayMore, "withBackpressureRequestWayMore", this);
            this.payloads.requestOverflow = _ClassStatement.forPayload(FlowableRangeTest::requestOverflow, "requestOverflow", this);
            this.payloads.emptyRangeSendsOnCompleteEagerlyWithRequestZero = _ClassStatement.forPayload(FlowableRangeTest::emptyRangeSendsOnCompleteEagerlyWithRequestZero, "emptyRangeSendsOnCompleteEagerlyWithRequestZero", this);
            this.payloads.nearMaxValueWithoutBackpressure = _ClassStatement.forPayload(FlowableRangeTest::nearMaxValueWithoutBackpressure, "nearMaxValueWithoutBackpressure", this);
            this.payloads.nearMaxValueWithBackpressure = _ClassStatement.forPayload(FlowableRangeTest::nearMaxValueWithBackpressure, "nearMaxValueWithBackpressure", this);
            this.payloads.negativeCount = _ClassStatement.forPayload(FlowableRangeTest::negativeCount, "negativeCount", this);
            this.payloads.requestWrongFusion = _ClassStatement.forPayload(FlowableRangeTest::requestWrongFusion, "requestWrongFusion", this);
            this.payloads.countOne = _ClassStatement.forPayload(FlowableRangeTest::countOne, "countOne", this);
            this.payloads.fused = _ClassStatement.forPayload(FlowableRangeTest::fused, "fused", this);
            this.payloads.fusedReject = _ClassStatement.forPayload(FlowableRangeTest::fusedReject, "fusedReject", this);
            this.payloads.disposed = _ClassStatement.forPayload(FlowableRangeTest::disposed, "disposed", this);
            this.payloads.fusedClearIsEmpty = _ClassStatement.forPayload(FlowableRangeTest::fusedClearIsEmpty, "fusedClearIsEmpty", this);
            this.payloads.noOverflow = _ClassStatement.forPayload(FlowableRangeTest::noOverflow, "noOverflow", this);
            this.payloads.conditionalNormal = _ClassStatement.forPayload(FlowableRangeTest::conditionalNormal, "conditionalNormal", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableRangeTest::badRequest, "badRequest", this);
            this.payloads.conditionalNormalSlowpath = _ClassStatement.forPayload(FlowableRangeTest::conditionalNormalSlowpath, "conditionalNormalSlowpath", this);
            this.payloads.conditionalSlowPathTakeExact = _ClassStatement.forPayload(FlowableRangeTest::conditionalSlowPathTakeExact, "conditionalSlowPathTakeExact", this);
            this.payloads.slowPathTakeExact = _ClassStatement.forPayload(FlowableRangeTest::slowPathTakeExact, "slowPathTakeExact", this);
            this.payloads.conditionalSlowPathRebatch = _ClassStatement.forPayload(FlowableRangeTest::conditionalSlowPathRebatch, "conditionalSlowPathRebatch", this);
            this.payloads.slowPathRebatch = _ClassStatement.forPayload(FlowableRangeTest::slowPathRebatch, "slowPathRebatch", this);
            this.payloads.slowPathCancel = _ClassStatement.forPayload(FlowableRangeTest::slowPathCancel, "slowPathCancel", this);
            this.payloads.fastPathCancel = _ClassStatement.forPayload(FlowableRangeTest::fastPathCancel, "fastPathCancel", this);
            this.payloads.conditionalSlowPathCancel = _ClassStatement.forPayload(FlowableRangeTest::conditionalSlowPathCancel, "conditionalSlowPathCancel", this);
            this.payloads.conditionalFastPathCancel = _ClassStatement.forPayload(FlowableRangeTest::conditionalFastPathCancel, "conditionalFastPathCancel", this);
            this.payloads.conditionalRequestOneByOne = _ClassStatement.forPayload(FlowableRangeTest::conditionalRequestOneByOne, "conditionalRequestOneByOne", this);
            this.payloads.conditionalRequestOneByOne2 = _ClassStatement.forPayload(FlowableRangeTest::conditionalRequestOneByOne2, "conditionalRequestOneByOne2", this);
            this.payloads.fastPathCancelExact = _ClassStatement.forPayload(FlowableRangeTest::fastPathCancelExact, "fastPathCancelExact", this);
            this.payloads.conditionalFastPathCancelExact = _ClassStatement.forPayload(FlowableRangeTest::conditionalFastPathCancelExact, "conditionalFastPathCancelExact", this);
            this.payloads.conditionalCancel1 = _ClassStatement.forPayload(FlowableRangeTest::conditionalCancel1, "conditionalCancel1", this);
            this.payloads.conditionalCancel2 = _ClassStatement.forPayload(FlowableRangeTest::conditionalCancel2, "conditionalCancel2", this);
            this.payloads.slowPathCancelBeforeComplete = _ClassStatement.forPayload(FlowableRangeTest::slowPathCancelBeforeComplete, "slowPathCancelBeforeComplete", this);
            this.payloads.conditionalFastPatchCancelBeforeComplete = _ClassStatement.forPayload(FlowableRangeTest::conditionalFastPatchCancelBeforeComplete, "conditionalFastPatchCancelBeforeComplete", this);
        }
    }
}
