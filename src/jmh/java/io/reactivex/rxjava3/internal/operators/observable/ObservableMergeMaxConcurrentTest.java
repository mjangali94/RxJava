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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.internal.schedulers.IoScheduler;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableMergeMaxConcurrentTest extends RxJavaTest {

    Observer<String> stringObserver;

    @Before
    public void before() {
        stringObserver = TestHelper.mockObserver();
    }

    @Test
    public void whenMaxConcurrentIsOne() {
        for (int i = 0; i < 100; i++) {
            List<Observable<String>> os = new ArrayList<>();
            os.add(Observable.just("one", "two", "three", "four", "five").subscribeOn(Schedulers.newThread()));
            os.add(Observable.just("one", "two", "three", "four", "five").subscribeOn(Schedulers.newThread()));
            os.add(Observable.just("one", "two", "three", "four", "five").subscribeOn(Schedulers.newThread()));
            List<String> expected = Arrays.asList("one", "two", "three", "four", "five", "one", "two", "three", "four", "five", "one", "two", "three", "four", "five");
            Iterator<String> iter = Observable.merge(os, 1).blockingIterable().iterator();
            List<String> actual = new ArrayList<>();
            while (iter.hasNext()) {
                actual.add(iter.next());
            }
            assertEquals(expected, actual);
        }
    }

    @Test
    public void maxConcurrent() {
        for (int times = 0; times < 100; times++) {
            int observableCount = 100;
            // Test maxConcurrent from 2 to 12
            int maxConcurrent = 2 + (times % 10);
            AtomicInteger subscriptionCount = new AtomicInteger(0);
            List<Observable<String>> os = new ArrayList<>();
            List<SubscriptionCheckObservable> scos = new ArrayList<>();
            for (int i = 0; i < observableCount; i++) {
                SubscriptionCheckObservable sco = new SubscriptionCheckObservable(subscriptionCount, maxConcurrent);
                scos.add(sco);
                os.add(Observable.unsafeCreate(sco));
            }
            Iterator<String> iter = Observable.merge(os, maxConcurrent).blockingIterable().iterator();
            List<String> actual = new ArrayList<>();
            while (iter.hasNext()) {
                actual.add(iter.next());
            }
            // // System.out.println("actual: " + actual);
            assertEquals(5 * observableCount, actual.size());
            for (SubscriptionCheckObservable sco : scos) {
                assertFalse(sco.failed);
            }
        }
    }

    private static class SubscriptionCheckObservable implements ObservableSource<String> {

        private final AtomicInteger subscriptionCount;

        private final int maxConcurrent;

        volatile boolean failed;

        SubscriptionCheckObservable(AtomicInteger subscriptionCount, int maxConcurrent) {
            this.subscriptionCount = subscriptionCount;
            this.maxConcurrent = maxConcurrent;
        }

        @Override
        public void subscribe(final Observer<? super String> t1) {
            t1.onSubscribe(Disposable.empty());
            new Thread(new Runnable() {

                @Override
                public void run() {
                    if (subscriptionCount.incrementAndGet() > maxConcurrent) {
                        failed = true;
                    }
                    t1.onNext("one");
                    t1.onNext("two");
                    t1.onNext("three");
                    t1.onNext("four");
                    t1.onNext("five");
                    // We could not decrement subscriptionCount in the unsubscribe method
                    // as "unsubscribe" is not guaranteed to be called before the next "subscribe".
                    subscriptionCount.decrementAndGet();
                    t1.onComplete();
                }
            }).start();
        }
    }

    @Test
    public void mergeALotOfSourcesOneByOneSynchronously() {
        int n = 10000;
        List<Observable<Integer>> sourceList = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            sourceList.add(Observable.just(i));
        }
        Iterator<Integer> it = Observable.merge(Observable.fromIterable(sourceList), 1).blockingIterable().iterator();
        int j = 0;
        while (it.hasNext()) {
            assertEquals((Integer) j, it.next());
            j++;
        }
        assertEquals(j, n);
    }

    @Test
    public void mergeALotOfSourcesOneByOneSynchronouslyTakeHalf() {
        int n = 10000;
        List<Observable<Integer>> sourceList = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            sourceList.add(Observable.just(i));
        }
        Iterator<Integer> it = Observable.merge(Observable.fromIterable(sourceList), 1).take(n / 2).blockingIterable().iterator();
        int j = 0;
        while (it.hasNext()) {
            assertEquals((Integer) j, it.next());
            j++;
        }
        assertEquals(j, n / 2);
    }

    @Test
    public void simple() {
        for (int i = 1; i < 100; i++) {
            TestObserverEx<Integer> to = new TestObserverEx<>();
            List<Observable<Integer>> sourceList = new ArrayList<>(i);
            List<Integer> result = new ArrayList<>(i);
            for (int j = 1; j <= i; j++) {
                sourceList.add(Observable.just(j));
                result.add(j);
            }
            Observable.merge(sourceList, i).subscribe(to);
            to.assertNoErrors();
            to.assertTerminated();
            to.assertValueSequence(result);
        }
    }

    @Test
    public void simpleOneLess() {
        for (int i = 2; i < 100; i++) {
            TestObserverEx<Integer> to = new TestObserverEx<>();
            List<Observable<Integer>> sourceList = new ArrayList<>(i);
            List<Integer> result = new ArrayList<>(i);
            for (int j = 1; j <= i; j++) {
                sourceList.add(Observable.just(j));
                result.add(j);
            }
            Observable.merge(sourceList, i - 1).subscribe(to);
            to.assertNoErrors();
            to.assertTerminated();
            to.assertValueSequence(result);
        }
    }

    @Test
    public void simpleAsyncLoop() {
        IoScheduler ios = (IoScheduler) Schedulers.io();
        int c = ios.size();
        for (int i = 0; i < 200; i++) {
            simpleAsync();
            int c1 = ios.size();
            if (c + 60 < c1) {
                throw new AssertionError("Worker leak: " + c + " - " + c1);
            }
        }
    }

    @Test
    public void simpleAsync() {
        for (int i = 1; i < 50; i++) {
            TestObserver<Integer> to = new TestObserver<>();
            List<Observable<Integer>> sourceList = new ArrayList<>(i);
            Set<Integer> expected = new HashSet<>(i);
            for (int j = 1; j <= i; j++) {
                sourceList.add(Observable.just(j).subscribeOn(Schedulers.io()));
                expected.add(j);
            }
            Observable.merge(sourceList, i).subscribe(to);
            to.awaitDone(1, TimeUnit.SECONDS);
            to.assertNoErrors();
            Set<Integer> actual = new HashSet<>(to.values());
            assertEquals(expected, actual);
        }
    }

    @Test
    public void simpleOneLessAsyncLoop() {
        for (int i = 0; i < 200; i++) {
            simpleOneLessAsync();
        }
    }

    @Test
    public void simpleOneLessAsync() {
        long t = System.currentTimeMillis();
        for (int i = 2; i < 50; i++) {
            if (System.currentTimeMillis() - t > TimeUnit.SECONDS.toMillis(9)) {
                break;
            }
            TestObserver<Integer> to = new TestObserver<>();
            List<Observable<Integer>> sourceList = new ArrayList<>(i);
            Set<Integer> expected = new HashSet<>(i);
            for (int j = 1; j <= i; j++) {
                sourceList.add(Observable.just(j).subscribeOn(Schedulers.io()));
                expected.add(j);
            }
            Observable.merge(sourceList, i - 1).subscribe(to);
            to.awaitDone(1, TimeUnit.SECONDS);
            to.assertNoErrors();
            Set<Integer> actual = new HashSet<>(to.values());
            assertEquals(expected, actual);
        }
    }

    @Test
    public void take() throws Exception {
        List<Observable<Integer>> sourceList = new ArrayList<>(3);
        sourceList.add(Observable.range(0, 100000).subscribeOn(Schedulers.io()));
        sourceList.add(Observable.range(0, 100000).subscribeOn(Schedulers.io()));
        sourceList.add(Observable.range(0, 100000).subscribeOn(Schedulers.io()));
        TestObserver<Integer> to = new TestObserver<>();
        Observable.merge(sourceList, 2).take(5).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors();
        to.assertValueCount(5);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableMergeMaxConcurrentTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_whenMaxConcurrentIsOne() throws java.lang.Throwable {
            this.payloads.whenMaxConcurrentIsOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maxConcurrent() throws java.lang.Throwable {
            this.payloads.maxConcurrent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeALotOfSourcesOneByOneSynchronously() throws java.lang.Throwable {
            this.payloads.mergeALotOfSourcesOneByOneSynchronously.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeALotOfSourcesOneByOneSynchronouslyTakeHalf() throws java.lang.Throwable {
            this.payloads.mergeALotOfSourcesOneByOneSynchronouslyTakeHalf.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.payloads.simple.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleOneLess() throws java.lang.Throwable {
            this.payloads.simpleOneLess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleAsyncLoop() throws java.lang.Throwable {
            this.payloads.simpleAsyncLoop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleAsync() throws java.lang.Throwable {
            this.payloads.simpleAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleOneLessAsyncLoop() throws java.lang.Throwable {
            this.payloads.simpleOneLessAsyncLoop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleOneLessAsync() throws java.lang.Throwable {
            this.payloads.simpleOneLessAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMergeMaxConcurrentTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMergeMaxConcurrentTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.before();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMergeMaxConcurrentTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMergeMaxConcurrentTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableMergeMaxConcurrentTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMergeMaxConcurrentTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableMergeMaxConcurrentTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableMergeMaxConcurrentTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement whenMaxConcurrentIsOne;

            public org.junit.runners.model.Statement maxConcurrent;

            public org.junit.runners.model.Statement mergeALotOfSourcesOneByOneSynchronously;

            public org.junit.runners.model.Statement mergeALotOfSourcesOneByOneSynchronouslyTakeHalf;

            public org.junit.runners.model.Statement simple;

            public org.junit.runners.model.Statement simpleOneLess;

            public org.junit.runners.model.Statement simpleAsyncLoop;

            public org.junit.runners.model.Statement simpleAsync;

            public org.junit.runners.model.Statement simpleOneLessAsyncLoop;

            public org.junit.runners.model.Statement simpleOneLessAsync;

            public org.junit.runners.model.Statement take;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.whenMaxConcurrentIsOne = _ClassStatement.forPayload(ObservableMergeMaxConcurrentTest::whenMaxConcurrentIsOne, "whenMaxConcurrentIsOne", this);
            this.payloads.maxConcurrent = _ClassStatement.forPayload(ObservableMergeMaxConcurrentTest::maxConcurrent, "maxConcurrent", this);
            this.payloads.mergeALotOfSourcesOneByOneSynchronously = _ClassStatement.forPayload(ObservableMergeMaxConcurrentTest::mergeALotOfSourcesOneByOneSynchronously, "mergeALotOfSourcesOneByOneSynchronously", this);
            this.payloads.mergeALotOfSourcesOneByOneSynchronouslyTakeHalf = _ClassStatement.forPayload(ObservableMergeMaxConcurrentTest::mergeALotOfSourcesOneByOneSynchronouslyTakeHalf, "mergeALotOfSourcesOneByOneSynchronouslyTakeHalf", this);
            this.payloads.simple = _ClassStatement.forPayload(ObservableMergeMaxConcurrentTest::simple, "simple", this);
            this.payloads.simpleOneLess = _ClassStatement.forPayload(ObservableMergeMaxConcurrentTest::simpleOneLess, "simpleOneLess", this);
            this.payloads.simpleAsyncLoop = _ClassStatement.forPayload(ObservableMergeMaxConcurrentTest::simpleAsyncLoop, "simpleAsyncLoop", this);
            this.payloads.simpleAsync = _ClassStatement.forPayload(ObservableMergeMaxConcurrentTest::simpleAsync, "simpleAsync", this);
            this.payloads.simpleOneLessAsyncLoop = _ClassStatement.forPayload(ObservableMergeMaxConcurrentTest::simpleOneLessAsyncLoop, "simpleOneLessAsyncLoop", this);
            this.payloads.simpleOneLessAsync = _ClassStatement.forPayload(ObservableMergeMaxConcurrentTest::simpleOneLessAsync, "simpleOneLessAsync", this);
            this.payloads.take = _ClassStatement.forPayload(ObservableMergeMaxConcurrentTest::take, "take", this);
        }
    }
}
