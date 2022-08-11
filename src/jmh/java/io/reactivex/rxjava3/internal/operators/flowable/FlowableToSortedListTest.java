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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableToSortedListTest extends RxJavaTest {

    @Test
    public void sortedListFlowable() {
        Flowable<Integer> w = Flowable.just(1, 3, 2, 5, 4);
        Flowable<List<Integer>> flowable = w.toSortedList().toFlowable();
        Subscriber<List<Integer>> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(Arrays.asList(1, 2, 3, 4, 5));
        verify(subscriber, Mockito.never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void sortedListWithCustomFunctionFlowable() {
        Flowable<Integer> w = Flowable.just(1, 3, 2, 5, 4);
        Flowable<List<Integer>> flowable = w.toSortedList(new Comparator<Integer>() {

            @Override
            public int compare(Integer t1, Integer t2) {
                return t2 - t1;
            }
        }).toFlowable();
        Subscriber<List<Integer>> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(Arrays.asList(5, 4, 3, 2, 1));
        verify(subscriber, Mockito.never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void withFollowingFirstFlowable() {
        Flowable<Integer> f = Flowable.just(1, 3, 2, 5, 4);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), f.toSortedList().toFlowable().blockingFirst());
    }

    @Test
    public void backpressureHonoredFlowable() {
        Flowable<List<Integer>> w = Flowable.just(1, 3, 2, 5, 4).toSortedList().toFlowable();
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>(0L);
        w.subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(1);
        ts.assertValue(Arrays.asList(1, 2, 3, 4, 5));
        ts.assertNoErrors();
        ts.assertComplete();
        ts.request(1);
        ts.assertValue(Arrays.asList(1, 2, 3, 4, 5));
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void sorted() {
        Flowable.just(5, 1, 2, 4, 3).sorted().test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void sortedComparator() {
        Flowable.just(5, 1, 2, 4, 3).sorted(new Comparator<Integer>() {

            @Override
            public int compare(Integer a, Integer b) {
                return b - a;
            }
        }).test().assertResult(5, 4, 3, 2, 1);
    }

    @Test
    public void toSortedListCapacityFlowable() {
        Flowable.just(5, 1, 2, 4, 3).toSortedList(4).toFlowable().test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void toSortedListComparatorCapacityFlowable() {
        Flowable.just(5, 1, 2, 4, 3).toSortedList(new Comparator<Integer>() {

            @Override
            public int compare(Integer a, Integer b) {
                return b - a;
            }
        }, 4).toFlowable().test().assertResult(Arrays.asList(5, 4, 3, 2, 1));
    }

    @Test
    public void sortedList() {
        Flowable<Integer> w = Flowable.just(1, 3, 2, 5, 4);
        Single<List<Integer>> single = w.toSortedList();
        SingleObserver<List<Integer>> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(Arrays.asList(1, 2, 3, 4, 5));
        verify(observer, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void sortedListWithCustomFunction() {
        Flowable<Integer> w = Flowable.just(1, 3, 2, 5, 4);
        Single<List<Integer>> single = w.toSortedList(new Comparator<Integer>() {

            @Override
            public int compare(Integer t1, Integer t2) {
                return t2 - t1;
            }
        });
        SingleObserver<List<Integer>> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(Arrays.asList(5, 4, 3, 2, 1));
        verify(observer, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void withFollowingFirst() {
        Flowable<Integer> f = Flowable.just(1, 3, 2, 5, 4);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), f.toSortedList().blockingGet());
    }

    static void await(CyclicBarrier cb) {
        try {
            cb.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (BrokenBarrierException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void toSortedListCapacity() {
        Flowable.just(5, 1, 2, 4, 3).toSortedList(4).test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void toSortedListComparatorCapacity() {
        Flowable.just(5, 1, 2, 4, 3).toSortedList(new Comparator<Integer>() {

            @Override
            public int compare(Integer a, Integer b) {
                return b - a;
            }
        }, 4).test().assertResult(Arrays.asList(5, 4, 3, 2, 1));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableToSortedListTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sortedListFlowable() throws java.lang.Throwable {
            this.payloads.sortedListFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sortedListWithCustomFunctionFlowable() throws java.lang.Throwable {
            this.payloads.sortedListWithCustomFunctionFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFollowingFirstFlowable() throws java.lang.Throwable {
            this.payloads.withFollowingFirstFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureHonoredFlowable() throws java.lang.Throwable {
            this.payloads.backpressureHonoredFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sorted() throws java.lang.Throwable {
            this.payloads.sorted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sortedComparator() throws java.lang.Throwable {
            this.payloads.sortedComparator.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSortedListCapacityFlowable() throws java.lang.Throwable {
            this.payloads.toSortedListCapacityFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSortedListComparatorCapacityFlowable() throws java.lang.Throwable {
            this.payloads.toSortedListComparatorCapacityFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sortedList() throws java.lang.Throwable {
            this.payloads.sortedList.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sortedListWithCustomFunction() throws java.lang.Throwable {
            this.payloads.sortedListWithCustomFunction.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFollowingFirst() throws java.lang.Throwable {
            this.payloads.withFollowingFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSortedListCapacity() throws java.lang.Throwable {
            this.payloads.toSortedListCapacity.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSortedListComparatorCapacity() throws java.lang.Throwable {
            this.payloads.toSortedListComparatorCapacity.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToSortedListTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToSortedListTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToSortedListTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToSortedListTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableToSortedListTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToSortedListTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableToSortedListTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableToSortedListTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement sortedListFlowable;

            public org.junit.runners.model.Statement sortedListWithCustomFunctionFlowable;

            public org.junit.runners.model.Statement withFollowingFirstFlowable;

            public org.junit.runners.model.Statement backpressureHonoredFlowable;

            public org.junit.runners.model.Statement sorted;

            public org.junit.runners.model.Statement sortedComparator;

            public org.junit.runners.model.Statement toSortedListCapacityFlowable;

            public org.junit.runners.model.Statement toSortedListComparatorCapacityFlowable;

            public org.junit.runners.model.Statement sortedList;

            public org.junit.runners.model.Statement sortedListWithCustomFunction;

            public org.junit.runners.model.Statement withFollowingFirst;

            public org.junit.runners.model.Statement toSortedListCapacity;

            public org.junit.runners.model.Statement toSortedListComparatorCapacity;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.sortedListFlowable = _ClassStatement.forPayload(FlowableToSortedListTest::sortedListFlowable, "sortedListFlowable", this);
            this.payloads.sortedListWithCustomFunctionFlowable = _ClassStatement.forPayload(FlowableToSortedListTest::sortedListWithCustomFunctionFlowable, "sortedListWithCustomFunctionFlowable", this);
            this.payloads.withFollowingFirstFlowable = _ClassStatement.forPayload(FlowableToSortedListTest::withFollowingFirstFlowable, "withFollowingFirstFlowable", this);
            this.payloads.backpressureHonoredFlowable = _ClassStatement.forPayload(FlowableToSortedListTest::backpressureHonoredFlowable, "backpressureHonoredFlowable", this);
            this.payloads.sorted = _ClassStatement.forPayload(FlowableToSortedListTest::sorted, "sorted", this);
            this.payloads.sortedComparator = _ClassStatement.forPayload(FlowableToSortedListTest::sortedComparator, "sortedComparator", this);
            this.payloads.toSortedListCapacityFlowable = _ClassStatement.forPayload(FlowableToSortedListTest::toSortedListCapacityFlowable, "toSortedListCapacityFlowable", this);
            this.payloads.toSortedListComparatorCapacityFlowable = _ClassStatement.forPayload(FlowableToSortedListTest::toSortedListComparatorCapacityFlowable, "toSortedListComparatorCapacityFlowable", this);
            this.payloads.sortedList = _ClassStatement.forPayload(FlowableToSortedListTest::sortedList, "sortedList", this);
            this.payloads.sortedListWithCustomFunction = _ClassStatement.forPayload(FlowableToSortedListTest::sortedListWithCustomFunction, "sortedListWithCustomFunction", this);
            this.payloads.withFollowingFirst = _ClassStatement.forPayload(FlowableToSortedListTest::withFollowingFirst, "withFollowingFirst", this);
            this.payloads.toSortedListCapacity = _ClassStatement.forPayload(FlowableToSortedListTest::toSortedListCapacity, "toSortedListCapacity", this);
            this.payloads.toSortedListComparatorCapacity = _ClassStatement.forPayload(FlowableToSortedListTest::toSortedListComparatorCapacity, "toSortedListComparatorCapacity", this);
        }
    }
}
