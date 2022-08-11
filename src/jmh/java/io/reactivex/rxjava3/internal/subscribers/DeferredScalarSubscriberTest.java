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
package io.reactivex.rxjava3.internal.subscribers;

import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class DeferredScalarSubscriberTest extends RxJavaTest {

    @Test
    public void completeFirst() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0L);
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        ds.setupDownstream();
        ds.onNext(1);
        ts.assertNoValues();
        ds.onComplete();
        ts.assertNoValues();
        ts.request(1);
        ts.assertValues(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void requestFirst() {
        TestSubscriber<Integer> ts = TestSubscriber.create(1);
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        ds.setupDownstream();
        ds.onNext(1);
        ts.assertNoValues();
        ds.onComplete();
        ts.assertValues(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void empty() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0L);
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        ds.setupDownstream();
        ts.assertNoValues();
        ds.onComplete();
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void error() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0L);
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        ds.setupDownstream();
        ts.assertNoValues();
        ds.onError(new TestException());
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void unsubscribeComposes() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = TestSubscriber.create(0L);
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        pp.subscribe(ds);
        assertTrue("No subscribers?", pp.hasSubscribers());
        ts.cancel();
        ds.onNext(1);
        ds.onComplete();
        ts.request(1);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        assertFalse("Subscribers?", pp.hasSubscribers());
        assertTrue("Deferred not unsubscribed?", ds.isCancelled());
    }

    @Test
    public void emptySource() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0L);
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        // we need a producer from upstream
        Flowable.just(1).ignoreElements().<Integer>toFlowable().subscribe(ds);
        ts.assertNoValues();
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void justSource() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0L);
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        ds.subscribeTo(Flowable.just(1));
        ts.assertNoValues();
        ts.request(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void rangeSource() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        ds.subscribeTo(Flowable.range(1, 10));
        ts.assertNoValues();
        ts.request(1);
        ts.assertValue(10);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void completeAfterNext() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                cancel();
            }
        };
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        ds.setupDownstream();
        ds.onNext(1);
        ts.assertNoValues();
        ds.onComplete();
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void completeAfterNextViaRequest() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>(0L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                cancel();
            }
        };
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        ds.setupDownstream();
        ds.onNext(1);
        ds.onComplete();
        ts.assertNoValues();
        ts.request(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void doubleComplete() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        ds.setupDownstream();
        ds.onNext(1);
        ts.request(1);
        ds.onComplete();
        ds.onComplete();
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void doubleComplete2() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        ds.setupDownstream();
        ds.onNext(1);
        ds.onComplete();
        ds.onComplete();
        ts.request(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void doubleRequest() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        ds.setupDownstream();
        ds.onNext(1);
        ts.request(1);
        ts.request(1);
        ds.onComplete();
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void negativeRequest() {
        List<Throwable> list = TestHelper.trackPluginErrors();
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        ds.setupDownstream();
        ds.downstreamRequest(-99);
        RxJavaPlugins.reset();
        TestHelper.assertError(list, 0, IllegalArgumentException.class, "n > 0 required but it was -99");
    }

    @Test
    public void callsAfterUnsubscribe() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0);
        TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
        ds.setupDownstream();
        ts.cancel();
        ds.downstreamRequest(1);
        ds.onNext(1);
        ds.onComplete();
        ds.onComplete();
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void emissionRequestRace() {
        Worker w = Schedulers.computation().createWorker();
        try {
            for (int i = 0; i < 10000; i++) {
                final TestSubscriber<Integer> ts = TestSubscriber.create(0L);
                TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
                ds.setupDownstream();
                ds.onNext(1);
                final AtomicInteger ready = new AtomicInteger(2);
                w.schedule(new Runnable() {

                    @Override
                    public void run() {
                        ready.decrementAndGet();
                        while (ready.get() != 0) {
                        }
                        ts.request(1);
                    }
                });
                ready.decrementAndGet();
                while (ready.get() != 0) {
                }
                ds.onComplete();
                ts.awaitDone(5, TimeUnit.SECONDS);
                ts.assertValues(1);
                ts.assertNoErrors();
                ts.assertComplete();
            }
        } finally {
            w.dispose();
        }
    }

    @Test
    public void emissionRequestRace2() {
        Worker w = Schedulers.io().createWorker();
        Worker w2 = Schedulers.io().createWorker();
        int m = 10000;
        if (Runtime.getRuntime().availableProcessors() < 3) {
            m = 1000;
        }
        try {
            for (int i = 0; i < m; i++) {
                final TestSubscriber<Integer> ts = TestSubscriber.create(0L);
                TestingDeferredScalarSubscriber ds = new TestingDeferredScalarSubscriber(ts);
                ds.setupDownstream();
                ds.onNext(1);
                final AtomicInteger ready = new AtomicInteger(3);
                w.schedule(new Runnable() {

                    @Override
                    public void run() {
                        ready.decrementAndGet();
                        while (ready.get() != 0) {
                        }
                        ts.request(1);
                    }
                });
                w2.schedule(new Runnable() {

                    @Override
                    public void run() {
                        ready.decrementAndGet();
                        while (ready.get() != 0) {
                        }
                        ts.request(1);
                    }
                });
                ready.decrementAndGet();
                while (ready.get() != 0) {
                }
                ds.onComplete();
                ts.awaitDone(5, TimeUnit.SECONDS);
                ts.assertValues(1);
                ts.assertNoErrors();
                ts.assertComplete();
            }
        } finally {
            w.dispose();
            w2.dispose();
        }
    }

    static final class TestingDeferredScalarSubscriber extends DeferredScalarSubscriber<Integer, Integer> {

        private static final long serialVersionUID = 6285096158319517837L;

        TestingDeferredScalarSubscriber(Subscriber<? super Integer> downstream) {
            super(downstream);
        }

        @Override
        public void onNext(Integer t) {
            value = t;
            hasValue = true;
        }

        public void setupDownstream() {
            onSubscribe(new BooleanSubscription());
        }

        public void subscribeTo(Publisher<Integer> p) {
            p.subscribe(this);
        }

        public void downstreamRequest(long n) {
            request(n);
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.doubleOnSubscribe(new DeferredScalarSubscriber<Integer, Integer>(new TestSubscriber<>()) {

            private static final long serialVersionUID = -4445381578878059054L;

            @Override
            public void onNext(Integer t) {
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private DeferredScalarSubscriberTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeFirst() throws java.lang.Throwable {
            this.payloads.completeFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestFirst() throws java.lang.Throwable {
            this.payloads.requestFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeComposes() throws java.lang.Throwable {
            this.payloads.unsubscribeComposes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptySource() throws java.lang.Throwable {
            this.payloads.emptySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justSource() throws java.lang.Throwable {
            this.payloads.justSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeSource() throws java.lang.Throwable {
            this.payloads.rangeSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeAfterNext() throws java.lang.Throwable {
            this.payloads.completeAfterNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeAfterNextViaRequest() throws java.lang.Throwable {
            this.payloads.completeAfterNextViaRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleComplete() throws java.lang.Throwable {
            this.payloads.doubleComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleComplete2() throws java.lang.Throwable {
            this.payloads.doubleComplete2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleRequest() throws java.lang.Throwable {
            this.payloads.doubleRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_negativeRequest() throws java.lang.Throwable {
            this.payloads.negativeRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_callsAfterUnsubscribe() throws java.lang.Throwable {
            this.payloads.callsAfterUnsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emissionRequestRace() throws java.lang.Throwable {
            this.payloads.emissionRequestRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emissionRequestRace2() throws java.lang.Throwable {
            this.payloads.emissionRequestRace2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<DeferredScalarSubscriberTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<DeferredScalarSubscriberTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<DeferredScalarSubscriberTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<DeferredScalarSubscriberTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new DeferredScalarSubscriberTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<DeferredScalarSubscriberTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(DeferredScalarSubscriberTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(DeferredScalarSubscriberTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement completeFirst;

            public org.junit.runners.model.Statement requestFirst;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement unsubscribeComposes;

            public org.junit.runners.model.Statement emptySource;

            public org.junit.runners.model.Statement justSource;

            public org.junit.runners.model.Statement rangeSource;

            public org.junit.runners.model.Statement completeAfterNext;

            public org.junit.runners.model.Statement completeAfterNextViaRequest;

            public org.junit.runners.model.Statement doubleComplete;

            public org.junit.runners.model.Statement doubleComplete2;

            public org.junit.runners.model.Statement doubleRequest;

            public org.junit.runners.model.Statement negativeRequest;

            public org.junit.runners.model.Statement callsAfterUnsubscribe;

            public org.junit.runners.model.Statement emissionRequestRace;

            public org.junit.runners.model.Statement emissionRequestRace2;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.completeFirst = _ClassStatement.forPayload(DeferredScalarSubscriberTest::completeFirst, "completeFirst", this);
            this.payloads.requestFirst = _ClassStatement.forPayload(DeferredScalarSubscriberTest::requestFirst, "requestFirst", this);
            this.payloads.empty = _ClassStatement.forPayload(DeferredScalarSubscriberTest::empty, "empty", this);
            this.payloads.error = _ClassStatement.forPayload(DeferredScalarSubscriberTest::error, "error", this);
            this.payloads.unsubscribeComposes = _ClassStatement.forPayload(DeferredScalarSubscriberTest::unsubscribeComposes, "unsubscribeComposes", this);
            this.payloads.emptySource = _ClassStatement.forPayload(DeferredScalarSubscriberTest::emptySource, "emptySource", this);
            this.payloads.justSource = _ClassStatement.forPayload(DeferredScalarSubscriberTest::justSource, "justSource", this);
            this.payloads.rangeSource = _ClassStatement.forPayload(DeferredScalarSubscriberTest::rangeSource, "rangeSource", this);
            this.payloads.completeAfterNext = _ClassStatement.forPayload(DeferredScalarSubscriberTest::completeAfterNext, "completeAfterNext", this);
            this.payloads.completeAfterNextViaRequest = _ClassStatement.forPayload(DeferredScalarSubscriberTest::completeAfterNextViaRequest, "completeAfterNextViaRequest", this);
            this.payloads.doubleComplete = _ClassStatement.forPayload(DeferredScalarSubscriberTest::doubleComplete, "doubleComplete", this);
            this.payloads.doubleComplete2 = _ClassStatement.forPayload(DeferredScalarSubscriberTest::doubleComplete2, "doubleComplete2", this);
            this.payloads.doubleRequest = _ClassStatement.forPayload(DeferredScalarSubscriberTest::doubleRequest, "doubleRequest", this);
            this.payloads.negativeRequest = _ClassStatement.forPayload(DeferredScalarSubscriberTest::negativeRequest, "negativeRequest", this);
            this.payloads.callsAfterUnsubscribe = _ClassStatement.forPayload(DeferredScalarSubscriberTest::callsAfterUnsubscribe, "callsAfterUnsubscribe", this);
            this.payloads.emissionRequestRace = _ClassStatement.forPayload(DeferredScalarSubscriberTest::emissionRequestRace, "emissionRequestRace", this);
            this.payloads.emissionRequestRace2 = _ClassStatement.forPayload(DeferredScalarSubscriberTest::emissionRequestRace2, "emissionRequestRace2", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(DeferredScalarSubscriberTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
