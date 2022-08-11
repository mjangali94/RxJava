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

import static org.junit.Assert.assertEquals;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class ObservableDoOnUnsubscribeTest extends RxJavaTest {

    @Test
    public void doOnUnsubscribe() throws Exception {
        int subCount = 3;
        final CountDownLatch upperLatch = new CountDownLatch(subCount);
        final CountDownLatch lowerLatch = new CountDownLatch(subCount);
        final CountDownLatch onNextLatch = new CountDownLatch(subCount);
        final AtomicInteger upperCount = new AtomicInteger();
        final AtomicInteger lowerCount = new AtomicInteger();
        Observable<Long> longs = Observable.interval(50, TimeUnit.MILLISECONDS).doOnDispose(new Action() {

            @Override
            public void run() {
                // Test that upper stream will be notified for un-subscription
                // from a child Observer
                upperLatch.countDown();
                upperCount.incrementAndGet();
            }
        }).doOnNext(new Consumer<Long>() {

            @Override
            public void accept(Long aLong) {
                // Ensure there is at least some onNext events before un-subscription happens
                onNextLatch.countDown();
            }
        }).doOnDispose(new Action() {

            @Override
            public void run() {
                // Test that lower stream will be notified for a direct un-subscription
                lowerLatch.countDown();
                lowerCount.incrementAndGet();
            }
        });
        List<Disposable> subscriptions = new ArrayList<>();
        List<TestObserver<Long>> subscribers = new ArrayList<>();
        for (int i = 0; i < subCount; ++i) {
            TestObserver<Long> observer = new TestObserver<>();
            subscriptions.add(observer);
            longs.subscribe(observer);
            subscribers.add(observer);
        }
        onNextLatch.await();
        for (int i = 0; i < subCount; ++i) {
            subscriptions.get(i).dispose();
        // Test that unsubscribe() method is not affected in any way
        // FIXME no longer valid
        // subscribers.get(i).assertUnsubscribed();
        }
        upperLatch.await();
        lowerLatch.await();
        assertEquals(String.format("There should exactly %d un-subscription events for upper stream", subCount), subCount, upperCount.get());
        assertEquals(String.format("There should exactly %d un-subscription events for lower stream", subCount), subCount, lowerCount.get());
    }

    @Test
    public void doOnUnSubscribeWorksWithRefCount() throws Exception {
        int subCount = 3;
        final CountDownLatch upperLatch = new CountDownLatch(1);
        final CountDownLatch lowerLatch = new CountDownLatch(1);
        final CountDownLatch onNextLatch = new CountDownLatch(subCount);
        final AtomicInteger upperCount = new AtomicInteger();
        final AtomicInteger lowerCount = new AtomicInteger();
        Observable<Long> longs = Observable.interval(50, TimeUnit.MILLISECONDS).doOnDispose(new Action() {

            @Override
            public void run() {
                // Test that upper stream will be notified for un-subscription
                upperLatch.countDown();
                upperCount.incrementAndGet();
            }
        }).doOnNext(new Consumer<Long>() {

            @Override
            public void accept(Long aLong) {
                // Ensure there is at least some onNext events before un-subscription happens
                onNextLatch.countDown();
            }
        }).doOnDispose(new Action() {

            @Override
            public void run() {
                // Test that lower stream will be notified for un-subscription
                lowerLatch.countDown();
                lowerCount.incrementAndGet();
            }
        }).publish().refCount();
        List<Disposable> subscriptions = new ArrayList<>();
        List<TestObserver<Long>> subscribers = new ArrayList<>();
        for (int i = 0; i < subCount; ++i) {
            TestObserver<Long> observer = new TestObserver<>();
            longs.subscribe(observer);
            subscriptions.add(observer);
            subscribers.add(observer);
        }
        onNextLatch.await();
        for (int i = 0; i < subCount; ++i) {
            subscriptions.get(i).dispose();
        // Test that unsubscribe() method is not affected in any way
        // FIXME no longer valid
        // subscribers.get(i).assertUnsubscribed();
        }
        upperLatch.await();
        lowerLatch.await();
        assertEquals("There should exactly 1 un-subscription events for upper stream", 1, upperCount.get());
        assertEquals("There should exactly 1 un-subscription events for lower stream", 1, lowerCount.get());
    }

    @Test
    public void noReentrantDispose() {
        final AtomicInteger disposeCalled = new AtomicInteger();
        final BehaviorSubject<Integer> s = BehaviorSubject.create();
        s.doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                disposeCalled.incrementAndGet();
                s.onNext(2);
            }
        }).firstOrError().subscribe().dispose();
        assertEquals(1, disposeCalled.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableDoOnUnsubscribeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnUnsubscribe() throws java.lang.Throwable {
            this.payloads.doOnUnsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnUnSubscribeWorksWithRefCount() throws java.lang.Throwable {
            this.payloads.doOnUnSubscribeWorksWithRefCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noReentrantDispose() throws java.lang.Throwable {
            this.payloads.noReentrantDispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDoOnUnsubscribeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDoOnUnsubscribeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDoOnUnsubscribeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDoOnUnsubscribeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableDoOnUnsubscribeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDoOnUnsubscribeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableDoOnUnsubscribeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableDoOnUnsubscribeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement doOnUnsubscribe;

            public org.junit.runners.model.Statement doOnUnSubscribeWorksWithRefCount;

            public org.junit.runners.model.Statement noReentrantDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.doOnUnsubscribe = _ClassStatement.forPayload(ObservableDoOnUnsubscribeTest::doOnUnsubscribe, "doOnUnsubscribe", this);
            this.payloads.doOnUnSubscribeWorksWithRefCount = _ClassStatement.forPayload(ObservableDoOnUnsubscribeTest::doOnUnSubscribeWorksWithRefCount, "doOnUnSubscribeWorksWithRefCount", this);
            this.payloads.noReentrantDispose = _ClassStatement.forPayload(ObservableDoOnUnsubscribeTest::noReentrantDispose, "noReentrantDispose", this);
        }
    }
}
