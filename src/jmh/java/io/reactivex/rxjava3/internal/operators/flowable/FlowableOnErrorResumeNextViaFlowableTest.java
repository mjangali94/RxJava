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

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.Mockito;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableOnErrorResumeNextViaFlowableTest extends RxJavaTest {

    @Test
    public void resumeNext() {
        Subscription s = mock(Subscription.class);
        // Trigger failure on second element
        TestObservable f = new TestObservable(s, "one", "fail", "two", "three");
        Flowable<String> w = Flowable.unsafeCreate(f);
        Flowable<String> resume = Flowable.just("twoResume", "threeResume");
        Flowable<String> flowable = w.onErrorResumeWith(resume);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        try {
            f.t.join();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
        verify(subscriber, Mockito.never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, times(1)).onNext("one");
        verify(subscriber, Mockito.never()).onNext("two");
        verify(subscriber, Mockito.never()).onNext("three");
        verify(subscriber, times(1)).onNext("twoResume");
        verify(subscriber, times(1)).onNext("threeResume");
    }

    @Test
    public void mapResumeAsyncNext() {
        Subscription sr = mock(Subscription.class);
        // Trigger multiple failures
        Flowable<String> w = Flowable.just("one", "fail", "two", "three", "fail");
        // Resume Observable is async
        TestObservable f = new TestObservable(sr, "twoResume", "threeResume");
        Flowable<String> resume = Flowable.unsafeCreate(f);
        // Introduce map function that fails intermittently (Map does not prevent this when the observer is a
        // rx.operator incl onErrorResumeNextViaObservable)
        w = w.map(new Function<String, String>() {

            @Override
            public String apply(String s) {
                if ("fail".equals(s)) {
                    throw new RuntimeException("Forced Failure");
                }
                System.out.println("BadMapper:" + s);
                return s;
            }
        });
        Flowable<String> flowable = w.onErrorResumeWith(resume);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        try {
            f.t.join();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
        verify(subscriber, Mockito.never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, times(1)).onNext("one");
        verify(subscriber, Mockito.never()).onNext("two");
        verify(subscriber, Mockito.never()).onNext("three");
        verify(subscriber, times(1)).onNext("twoResume");
        verify(subscriber, times(1)).onNext("threeResume");
    }

    static final class TestObservable implements Publisher<String> {

        final Subscription upstream;

        final String[] values;

        Thread t;

        TestObservable(Subscription s, String... values) {
            this.upstream = s;
            this.values = values;
        }

        @Override
        public void subscribe(final Subscriber<? super String> subscriber) {
            System.out.println("TestObservable subscribed to ...");
            subscriber.onSubscribe(upstream);
            t = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        System.out.println("running TestObservable thread");
                        for (String s : values) {
                            if ("fail".equals(s)) {
                                throw new RuntimeException("Forced Failure");
                            }
                            System.out.println("TestObservable onNext: " + s);
                            subscriber.onNext(s);
                        }
                        System.out.println("TestObservable onComplete");
                        subscriber.onComplete();
                    } catch (Throwable e) {
                        System.out.println("TestObservable onError: " + e);
                        subscriber.onError(e);
                    }
                }
            });
            System.out.println("starting TestObservable thread");
            t.start();
            System.out.println("done starting TestObservable thread");
        }
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(0, 100000).onErrorResumeWith(Flowable.just(1)).observeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

            int c;

            @Override
            public Integer apply(Integer t1) {
                if (c++ <= 1) {
                    // slow
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return t1;
            }
        }).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
    }

    @Test
    public void normalBackpressure() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.onErrorResumeWith(Flowable.range(3, 2)).subscribe(ts);
        ts.request(2);
        pp.onNext(1);
        pp.onNext(2);
        pp.onError(new TestException("Forced failure"));
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(2);
        ts.assertValues(1, 2, 3, 4);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableOnErrorResumeNextViaFlowableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resumeNext() throws java.lang.Throwable {
            this.payloads.resumeNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapResumeAsyncNext() throws java.lang.Throwable {
            this.payloads.mapResumeAsyncNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.payloads.backpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalBackpressure() throws java.lang.Throwable {
            this.payloads.normalBackpressure.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorResumeNextViaFlowableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorResumeNextViaFlowableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorResumeNextViaFlowableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorResumeNextViaFlowableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableOnErrorResumeNextViaFlowableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableOnErrorResumeNextViaFlowableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableOnErrorResumeNextViaFlowableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableOnErrorResumeNextViaFlowableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement resumeNext;

            public org.junit.runners.model.Statement mapResumeAsyncNext;

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement normalBackpressure;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.resumeNext = _ClassStatement.forPayload(FlowableOnErrorResumeNextViaFlowableTest::resumeNext, "resumeNext", this);
            this.payloads.mapResumeAsyncNext = _ClassStatement.forPayload(FlowableOnErrorResumeNextViaFlowableTest::mapResumeAsyncNext, "mapResumeAsyncNext", this);
            this.payloads.backpressure = _ClassStatement.forPayload(FlowableOnErrorResumeNextViaFlowableTest::backpressure, "backpressure", this);
            this.payloads.normalBackpressure = _ClassStatement.forPayload(FlowableOnErrorResumeNextViaFlowableTest::normalBackpressure, "normalBackpressure", this);
        }
    }
}
