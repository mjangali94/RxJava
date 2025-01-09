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

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableOnBackpressureLatestTest extends RxJavaTest {

    @Test
    public void simple() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Flowable.range(1, 5).onBackpressureLatest().subscribe(ts);
        ts.assertNoErrors();
        ts.assertTerminated();
        ts.assertValues(1, 2, 3, 4, 5);
    }

    @Test
    public void simpleError() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Flowable.range(1, 5).concatWith(Flowable.<Integer>error(new TestException())).onBackpressureLatest().subscribe(ts);
        ts.assertTerminated();
        ts.assertError(TestException.class);
        ts.assertValues(1, 2, 3, 4, 5);
    }

    @Test
    public void simpleBackpressure() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(2L);
        Flowable.range(1, 5).onBackpressureLatest().subscribe(ts);
        ts.assertNoErrors();
        ts.assertValues(1, 2);
        ts.assertNotComplete();
    }

    @Test
    public void synchronousDrop() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0L);
        source.onBackpressureLatest().subscribe(ts);
        ts.assertNoValues();
        source.onNext(1);
        ts.request(2);
        ts.assertValue(1);
        source.onNext(2);
        ts.assertValues(1, 2);
        source.onNext(3);
        source.onNext(4);
        source.onNext(5);
        source.onNext(6);
        ts.request(2);
        ts.assertValues(1, 2, 6);
        source.onNext(7);
        ts.assertValues(1, 2, 6, 7);
        source.onNext(8);
        source.onNext(9);
        source.onComplete();
        ts.request(1);
        ts.assertValues(1, 2, 6, 7, 9);
        ts.assertNoErrors();
        ts.assertTerminated();
    }

    @Test
    public void asynchronousDrop() throws InterruptedException {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>(1L) {

            final Random rnd = new Random();

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (rnd.nextDouble() < 0.001) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                request(1);
            }
        };
        int m = 100000;
        Flowable.range(1, m).subscribeOn(Schedulers.computation()).onBackpressureLatest().observeOn(Schedulers.io()).subscribe(ts);
        ts.awaitDone(2, TimeUnit.SECONDS);
        ts.assertTerminated();
        int n = ts.values().size();
        // System.out.println("testAsynchronousDrop -> " + n);
        Assert.assertTrue("All events received?", n < m);
        int previous = 0;
        for (Integer current : ts.values()) {
            Assert.assertTrue("The sequence must be increasing [current value=" + previous + ", previous value=" + current + "]", previous <= current);
            previous = current;
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.onBackpressureLatest();
            }
        });
    }

    @Test
    public void take() {
        Flowable.just(1, 2).onBackpressureLatest().take(1).test().assertResult(1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.never().onBackpressureLatest());
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().onBackpressureLatest());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableOnBackpressureLatestTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.payloads.simple.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleError() throws java.lang.Throwable {
            this.payloads.simpleError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleBackpressure() throws java.lang.Throwable {
            this.payloads.simpleBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_synchronousDrop() throws java.lang.Throwable {
            this.payloads.synchronousDrop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asynchronousDrop() throws java.lang.Throwable {
            this.payloads.asynchronousDrop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureLatestTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureLatestTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureLatestTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureLatestTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableOnBackpressureLatestTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnBackpressureLatestTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableOnBackpressureLatestTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableOnBackpressureLatestTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement simple;

            public org.junit.runners.model.Statement simpleError;

            public org.junit.runners.model.Statement simpleBackpressure;

            public org.junit.runners.model.Statement synchronousDrop;

            public org.junit.runners.model.Statement asynchronousDrop;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement badRequest;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.simple = _ClassStatement.forPayload(FlowableOnBackpressureLatestTest::simple, "simple", this);
            this.payloads.simpleError = _ClassStatement.forPayload(FlowableOnBackpressureLatestTest::simpleError, "simpleError", this);
            this.payloads.simpleBackpressure = _ClassStatement.forPayload(FlowableOnBackpressureLatestTest::simpleBackpressure, "simpleBackpressure", this);
            this.payloads.synchronousDrop = _ClassStatement.forPayload(FlowableOnBackpressureLatestTest::synchronousDrop, "synchronousDrop", this);
            this.payloads.asynchronousDrop = _ClassStatement.forPayload(FlowableOnBackpressureLatestTest::asynchronousDrop, "asynchronousDrop", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableOnBackpressureLatestTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.take = _ClassStatement.forPayload(FlowableOnBackpressureLatestTest::take, "take", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableOnBackpressureLatestTest::dispose, "dispose", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableOnBackpressureLatestTest::badRequest, "badRequest", this);
        }
    }
}
