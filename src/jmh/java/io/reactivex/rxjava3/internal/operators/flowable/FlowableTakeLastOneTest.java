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
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.subscribers.DefaultSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableTakeLastOneTest extends RxJavaTest {

    @Test
    public void lastOfManyReturnsLast() {
        TestSubscriberEx<Integer> s = new TestSubscriberEx<>();
        Flowable.range(1, 10).takeLast(1).subscribe(s);
        s.assertValue(10);
        s.assertNoErrors();
        s.assertTerminated();
    // NO longer assertable
    // s.assertUnsubscribed();
    }

    @Test
    public void lastOfEmptyReturnsEmpty() {
        TestSubscriberEx<Object> s = new TestSubscriberEx<>();
        Flowable.empty().takeLast(1).subscribe(s);
        s.assertNoValues();
        s.assertNoErrors();
        s.assertTerminated();
    // NO longer assertable
    // s.assertUnsubscribed();
    }

    @Test
    public void lastOfOneReturnsLast() {
        TestSubscriberEx<Integer> s = new TestSubscriberEx<>();
        Flowable.just(1).takeLast(1).subscribe(s);
        s.assertValue(1);
        s.assertNoErrors();
        s.assertTerminated();
    // NO longer assertable
    // s.assertUnsubscribed();
    }

    @Test
    public void unsubscribesFromUpstream() {
        final AtomicBoolean unsubscribed = new AtomicBoolean(false);
        Action unsubscribeAction = new Action() {

            @Override
            public void run() {
                unsubscribed.set(true);
            }
        };
        Flowable.just(1).concatWith(Flowable.<Integer>never()).doOnCancel(unsubscribeAction).takeLast(1).subscribe().dispose();
        assertTrue(unsubscribed.get());
    }

    @Test
    public void lastWithBackpressure() {
        MySubscriber<Integer> s = new MySubscriber<>(0);
        Flowable.just(1).takeLast(1).subscribe(s);
        assertEquals(0, s.list.size());
        s.requestMore(1);
        assertEquals(1, s.list.size());
    }

    @Test
    public void takeLastZeroProcessesAllItemsButIgnoresThem() {
        final AtomicInteger upstreamCount = new AtomicInteger();
        final int num = 10;
        long count = Flowable.range(1, num).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).takeLast(0).count().blockingGet();
        assertEquals(num, upstreamCount.get());
        assertEquals(0L, count);
    }

    private static class MySubscriber<T> extends DefaultSubscriber<T> {

        private long initialRequest;

        MySubscriber(long initialRequest) {
            this.initialRequest = initialRequest;
        }

        final List<T> list = new ArrayList<>();

        public void requestMore(long n) {
            request(n);
        }

        @Override
        public void onStart() {
            if (initialRequest > 0) {
                request(initialRequest);
            }
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(T t) {
            list.add(t);
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).takeLast(1));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.takeLast(1);
            }
        });
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).takeLast(1).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableTakeLastOneTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOfManyReturnsLast() throws java.lang.Throwable {
            this.payloads.lastOfManyReturnsLast.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOfEmptyReturnsEmpty() throws java.lang.Throwable {
            this.payloads.lastOfEmptyReturnsEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastOfOneReturnsLast() throws java.lang.Throwable {
            this.payloads.lastOfOneReturnsLast.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribesFromUpstream() throws java.lang.Throwable {
            this.payloads.unsubscribesFromUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastWithBackpressure() throws java.lang.Throwable {
            this.payloads.lastWithBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastZeroProcessesAllItemsButIgnoresThem() throws java.lang.Throwable {
            this.payloads.takeLastZeroProcessesAllItemsButIgnoresThem.evaluate();
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

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastOneTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastOneTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastOneTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastOneTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableTakeLastOneTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastOneTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableTakeLastOneTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableTakeLastOneTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement lastOfManyReturnsLast;

            public org.junit.runners.model.Statement lastOfEmptyReturnsEmpty;

            public org.junit.runners.model.Statement lastOfOneReturnsLast;

            public org.junit.runners.model.Statement unsubscribesFromUpstream;

            public org.junit.runners.model.Statement lastWithBackpressure;

            public org.junit.runners.model.Statement takeLastZeroProcessesAllItemsButIgnoresThem;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement error;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.lastOfManyReturnsLast = _ClassStatement.forPayload(FlowableTakeLastOneTest::lastOfManyReturnsLast, "lastOfManyReturnsLast", this);
            this.payloads.lastOfEmptyReturnsEmpty = _ClassStatement.forPayload(FlowableTakeLastOneTest::lastOfEmptyReturnsEmpty, "lastOfEmptyReturnsEmpty", this);
            this.payloads.lastOfOneReturnsLast = _ClassStatement.forPayload(FlowableTakeLastOneTest::lastOfOneReturnsLast, "lastOfOneReturnsLast", this);
            this.payloads.unsubscribesFromUpstream = _ClassStatement.forPayload(FlowableTakeLastOneTest::unsubscribesFromUpstream, "unsubscribesFromUpstream", this);
            this.payloads.lastWithBackpressure = _ClassStatement.forPayload(FlowableTakeLastOneTest::lastWithBackpressure, "lastWithBackpressure", this);
            this.payloads.takeLastZeroProcessesAllItemsButIgnoresThem = _ClassStatement.forPayload(FlowableTakeLastOneTest::takeLastZeroProcessesAllItemsButIgnoresThem, "takeLastZeroProcessesAllItemsButIgnoresThem", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableTakeLastOneTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableTakeLastOneTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableTakeLastOneTest::error, "error", this);
        }
    }
}
