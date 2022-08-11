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
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableSkipTest extends RxJavaTest {

    @Test(expected = IllegalArgumentException.class)
    public void skipNegativeElements() {
        Flowable<String> skip = Flowable.just("one", "two", "three").skip(-99);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        skip.subscribe(subscriber);
        verify(subscriber, times(1)).onNext("one");
        verify(subscriber, times(1)).onNext("two");
        verify(subscriber, times(1)).onNext("three");
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void skipZeroElements() {
        Flowable<String> skip = Flowable.just("one", "two", "three").skip(0);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        skip.subscribe(subscriber);
        verify(subscriber, times(1)).onNext("one");
        verify(subscriber, times(1)).onNext("two");
        verify(subscriber, times(1)).onNext("three");
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void skipOneElement() {
        Flowable<String> skip = Flowable.just("one", "two", "three").skip(1);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        skip.subscribe(subscriber);
        verify(subscriber, never()).onNext("one");
        verify(subscriber, times(1)).onNext("two");
        verify(subscriber, times(1)).onNext("three");
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void skipTwoElements() {
        Flowable<String> skip = Flowable.just("one", "two", "three").skip(2);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        skip.subscribe(subscriber);
        verify(subscriber, never()).onNext("one");
        verify(subscriber, never()).onNext("two");
        verify(subscriber, times(1)).onNext("three");
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void skipEmptyStream() {
        Flowable<String> w = Flowable.empty();
        Flowable<String> skip = w.skip(1);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        skip.subscribe(subscriber);
        verify(subscriber, never()).onNext(any(String.class));
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void skipMultipleObservers() {
        Flowable<String> skip = Flowable.just("one", "two", "three").skip(2);
        Subscriber<String> subscriber1 = TestHelper.mockSubscriber();
        skip.subscribe(subscriber1);
        Subscriber<String> subscriber2 = TestHelper.mockSubscriber();
        skip.subscribe(subscriber2);
        verify(subscriber1, times(1)).onNext(any(String.class));
        verify(subscriber1, never()).onError(any(Throwable.class));
        verify(subscriber1, times(1)).onComplete();
        verify(subscriber2, times(1)).onNext(any(String.class));
        verify(subscriber2, never()).onError(any(Throwable.class));
        verify(subscriber2, times(1)).onComplete();
    }

    @Test
    public void skipError() {
        Exception e = new Exception();
        Flowable<String> ok = Flowable.just("one");
        Flowable<String> error = Flowable.error(e);
        Flowable<String> skip = Flowable.concat(ok, error).skip(100);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        skip.subscribe(subscriber);
        verify(subscriber, never()).onNext(any(String.class));
        verify(subscriber, times(1)).onError(e);
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void backpressureMultipleSmallAsyncRequests() throws InterruptedException {
        final AtomicLong requests = new AtomicLong(0);
        TestSubscriber<Long> ts = new TestSubscriber<>(0L);
        Flowable.interval(100, TimeUnit.MILLISECONDS).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long n) {
                requests.addAndGet(n);
            }
        }).skip(4).subscribe(ts);
        Thread.sleep(100);
        ts.request(1);
        ts.request(1);
        Thread.sleep(100);
        ts.cancel();
        ts.assertNoErrors();
        assertEquals(6, requests.get());
    }

    @Test
    public void requestOverflowDoesNotOccur() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(Long.MAX_VALUE - 1);
        Flowable.range(1, 10).skip(5).subscribe(ts);
        ts.assertTerminated();
        ts.assertComplete();
        ts.assertNoErrors();
        assertEquals(Arrays.asList(6, 7, 8, 9, 10), ts.values());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).skip(2));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.skip(1);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableSkipTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipNegativeElements() throws java.lang.Throwable {
            this.payloads.skipNegativeElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipZeroElements() throws java.lang.Throwable {
            this.payloads.skipZeroElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipOneElement() throws java.lang.Throwable {
            this.payloads.skipOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipTwoElements() throws java.lang.Throwable {
            this.payloads.skipTwoElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipEmptyStream() throws java.lang.Throwable {
            this.payloads.skipEmptyStream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipMultipleObservers() throws java.lang.Throwable {
            this.payloads.skipMultipleObservers.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipError() throws java.lang.Throwable {
            this.payloads.skipError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureMultipleSmallAsyncRequests() throws java.lang.Throwable {
            this.payloads.backpressureMultipleSmallAsyncRequests.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestOverflowDoesNotOccur() throws java.lang.Throwable {
            this.payloads.requestOverflowDoesNotOccur.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableSkipTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableSkipTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableSkipTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement skipNegativeElements;

            public org.junit.runners.model.Statement skipZeroElements;

            public org.junit.runners.model.Statement skipOneElement;

            public org.junit.runners.model.Statement skipTwoElements;

            public org.junit.runners.model.Statement skipEmptyStream;

            public org.junit.runners.model.Statement skipMultipleObservers;

            public org.junit.runners.model.Statement skipError;

            public org.junit.runners.model.Statement backpressureMultipleSmallAsyncRequests;

            public org.junit.runners.model.Statement requestOverflowDoesNotOccur;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.skipNegativeElements = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableSkipTest::skipNegativeElements, java.lang.IllegalArgumentException.class), "skipNegativeElements", this);
            this.payloads.skipZeroElements = _ClassStatement.forPayload(FlowableSkipTest::skipZeroElements, "skipZeroElements", this);
            this.payloads.skipOneElement = _ClassStatement.forPayload(FlowableSkipTest::skipOneElement, "skipOneElement", this);
            this.payloads.skipTwoElements = _ClassStatement.forPayload(FlowableSkipTest::skipTwoElements, "skipTwoElements", this);
            this.payloads.skipEmptyStream = _ClassStatement.forPayload(FlowableSkipTest::skipEmptyStream, "skipEmptyStream", this);
            this.payloads.skipMultipleObservers = _ClassStatement.forPayload(FlowableSkipTest::skipMultipleObservers, "skipMultipleObservers", this);
            this.payloads.skipError = _ClassStatement.forPayload(FlowableSkipTest::skipError, "skipError", this);
            this.payloads.backpressureMultipleSmallAsyncRequests = _ClassStatement.forPayload(FlowableSkipTest::backpressureMultipleSmallAsyncRequests, "backpressureMultipleSmallAsyncRequests", this);
            this.payloads.requestOverflowDoesNotOccur = _ClassStatement.forPayload(FlowableSkipTest::requestOverflowDoesNotOccur, "requestOverflowDoesNotOccur", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableSkipTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableSkipTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
