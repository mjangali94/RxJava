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
import org.junit.Test;
import org.mockito.InOrder;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableSkipLastTest extends RxJavaTest {

    @Test
    public void skipLastEmpty() {
        Flowable<String> flowable = Flowable.<String>empty().skipLast(2);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(any(String.class));
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void skipLast1() {
        Flowable<String> flowable = Flowable.fromIterable(Arrays.asList("one", "two", "three")).skipLast(2);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        flowable.subscribe(subscriber);
        inOrder.verify(subscriber, never()).onNext("two");
        inOrder.verify(subscriber, never()).onNext("three");
        verify(subscriber, times(1)).onNext("one");
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void skipLast2() {
        Flowable<String> flowable = Flowable.fromIterable(Arrays.asList("one", "two")).skipLast(2);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(any(String.class));
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void skipLastWithZeroCount() {
        Flowable<String> w = Flowable.just("one", "two");
        Flowable<String> flowable = w.skipLast(0);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext("one");
        verify(subscriber, times(1)).onNext("two");
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void skipLastWithBackpressure() {
        Flowable<Integer> f = Flowable.range(0, Flowable.bufferSize() * 2).skipLast(Flowable.bufferSize() + 10);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        f.observeOn(Schedulers.computation()).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        assertEquals((Flowable.bufferSize()) - 10, ts.values().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void skipLastWithNegativeCount() {
        Flowable.just("one").skipLast(-1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).skipLast(1));
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).skipLast(1).test().assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.skipLast(1);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableSkipLastTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastEmpty() throws java.lang.Throwable {
            this.payloads.skipLastEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLast1() throws java.lang.Throwable {
            this.payloads.skipLast1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLast2() throws java.lang.Throwable {
            this.payloads.skipLast2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastWithZeroCount() throws java.lang.Throwable {
            this.payloads.skipLastWithZeroCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastWithBackpressure() throws java.lang.Throwable {
            this.payloads.skipLastWithBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastWithNegativeCount() throws java.lang.Throwable {
            this.payloads.skipLastWithNegativeCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipLastTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipLastTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipLastTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipLastTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableSkipLastTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipLastTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableSkipLastTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableSkipLastTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement skipLastEmpty;

            public org.junit.runners.model.Statement skipLast1;

            public org.junit.runners.model.Statement skipLast2;

            public org.junit.runners.model.Statement skipLastWithZeroCount;

            public org.junit.runners.model.Statement skipLastWithBackpressure;

            public org.junit.runners.model.Statement skipLastWithNegativeCount;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.skipLastEmpty = _ClassStatement.forPayload(FlowableSkipLastTest::skipLastEmpty, "skipLastEmpty", this);
            this.payloads.skipLast1 = _ClassStatement.forPayload(FlowableSkipLastTest::skipLast1, "skipLast1", this);
            this.payloads.skipLast2 = _ClassStatement.forPayload(FlowableSkipLastTest::skipLast2, "skipLast2", this);
            this.payloads.skipLastWithZeroCount = _ClassStatement.forPayload(FlowableSkipLastTest::skipLastWithZeroCount, "skipLastWithZeroCount", this);
            this.payloads.skipLastWithBackpressure = _ClassStatement.forPayload(FlowableSkipLastTest::skipLastWithBackpressure, "skipLastWithBackpressure", this);
            this.payloads.skipLastWithNegativeCount = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableSkipLastTest::skipLastWithNegativeCount, java.lang.IllegalArgumentException.class), "skipLastWithNegativeCount", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableSkipLastTest::dispose, "dispose", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableSkipLastTest::error, "error", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableSkipLastTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
