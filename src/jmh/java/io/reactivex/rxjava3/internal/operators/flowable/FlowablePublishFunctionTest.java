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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowablePublishFunctionTest extends RxJavaTest {

    @Test
    public void concatTakeFirstLastCompletes() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 3).publish(f -> Flowable.concat(f.take(5), f.takeLast(5))).subscribe(ts);
        ts.assertValues(1, 2, 3);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void concatTakeFirstLastBackpressureCompletes() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0L);
        Flowable.range(1, 6).publish(f -> Flowable.concat(f.take(5), f.takeLast(5))).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        // make sure take() doesn't go unbounded
        ts.request(1);
        ts.request(4);
        ts.assertValues(1, 2, 3, 4, 5);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(5);
        ts.assertValues(1, 2, 3, 4, 5, 6);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void canBeCancelled() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.publish(f -> Flowable.concat(f.take(5), f.takeLast(5))).subscribe(ts);
        pp.onNext(1);
        pp.onNext(2);
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.cancel();
        Assert.assertFalse("Source has subscribers?", pp.hasSubscribers());
    }

    @Test
    public void invalidPrefetch() {
        try {
            Flowable.<Integer>never().publish(Functions.identity(), -99);
            fail("Didn't throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("prefetch > 0 required but it was -99", ex.getMessage());
        }
    }

    @Test
    public void takeCompletes() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.publish(f -> f.take(1)).subscribe(ts);
        pp.onNext(1);
        ts.assertValues(1);
        ts.assertNoErrors();
        ts.assertComplete();
        Assert.assertFalse("Source has subscribers?", pp.hasSubscribers());
    }

    @Test
    public void oneStartOnly() {
        final AtomicInteger startCount = new AtomicInteger();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onStart() {
                startCount.incrementAndGet();
            }
        };
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.publish(f -> f.take(1)).subscribe(ts);
        Assert.assertEquals(1, startCount.get());
    }

    @Test
    public void takeCompletesUnsafe() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.publish(f -> f.take(1)).subscribe(ts);
        pp.onNext(1);
        ts.assertValues(1);
        ts.assertNoErrors();
        ts.assertComplete();
        Assert.assertFalse("Source has subscribers?", pp.hasSubscribers());
    }

    @Test
    public void directCompletesUnsafe() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.publish(Functions.identity()).subscribe(ts);
        pp.onNext(1);
        pp.onComplete();
        ts.assertValues(1);
        ts.assertNoErrors();
        ts.assertComplete();
        Assert.assertFalse("Source has subscribers?", pp.hasSubscribers());
    }

    @Test
    public void overflowMissingBackpressureException() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0);
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.publish(Functions.identity()).subscribe(ts);
        for (int i = 0; i < Flowable.bufferSize() * 2; i++) {
            pp.onNext(i);
        }
        ts.assertNoValues();
        ts.assertError(MissingBackpressureException.class);
        ts.assertNotComplete();
        Assert.assertEquals("Could not emit value due to lack of requests", ts.errors().get(0).getMessage());
        Assert.assertFalse("Source has subscribers?", pp.hasSubscribers());
    }

    @Test
    public void overflowMissingBackpressureExceptionDelayed() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0);
        PublishProcessor<Integer> pp = PublishProcessor.create();
        new FlowablePublishMulticast<>(pp, Functions.identity(), Flowable.bufferSize(), true).subscribe(ts);
        for (int i = 0; i < Flowable.bufferSize() * 2; i++) {
            pp.onNext(i);
        }
        ts.request(Flowable.bufferSize());
        ts.assertValueCount(Flowable.bufferSize());
        ts.assertError(MissingBackpressureException.class);
        ts.assertNotComplete();
        Assert.assertEquals("Could not emit value due to lack of requests", ts.errors().get(0).getMessage());
        Assert.assertFalse("Source has subscribers?", pp.hasSubscribers());
    }

    @Test
    public void emptyIdentityMapped() {
        Flowable.empty().publish(Functions.identity()).test().assertResult();
    }

    @Test
    public void independentlyMapped() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.publish(v -> Flowable.range(1, 5)).test(0);
        assertTrue("pp has no Subscribers?!", pp.hasSubscribers());
        ts.assertNoValues().assertNoErrors().assertNotComplete();
        ts.request(5);
        ts.assertResult(1, 2, 3, 4, 5);
        assertFalse("pp has Subscribers?!", pp.hasSubscribers());
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(f -> f.publish(Functions.identity()), false, 1, 1, 1);
    }

    @Test
    public void frontOverflow() {
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                for (int i = 0; i < 9; i++) {
                    s.onNext(i);
                }
            }
        }.publish(Functions.identity(), 8).test(0).assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void errorResubscribe() {
        Flowable.error(new TestException()).publish(f -> f.onErrorResumeWith(f)).test().assertFailure(TestException.class);
    }

    @Test
    public void fusedInputCrash() {
        Flowable.just(1).map(v -> {
            throw new TestException();
        }).publish(Functions.identity()).test().assertFailure(TestException.class);
    }

    @Test
    public void error() {
        new FlowablePublishMulticast<>(Flowable.just(1).concatWith(Flowable.error(new TestException())), Functions.identity(), 16, true).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void backpressuredEmpty() {
        Flowable.<Integer>empty().publish(Functions.identity()).test(0L).assertResult();
    }

    @Test
    public void oneByOne() {
        Flowable.range(1, 10).publish(Functions.identity()).rebatchRequests(1).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void completeCancelRaceNoRequest() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final TestSubscriber<Integer> ts = new TestSubscriber<Integer>(1L) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    cancel();
                    onComplete();
                }
            }
        };
        pp.publish(Functions.identity()).subscribe(ts);
        pp.onNext(1);
        assertFalse(pp.hasSubscribers());
        ts.assertResult(1);
    }

    @Test
    public void inputOutputSubscribeRace() {
        Flowable<Integer> source = Flowable.just(1).publish(f -> f.subscribeOn(Schedulers.single()));
        for (int i = 0; i < 500; i++) {
            source.test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
        }
    }

    @Test
    public void inputOutputSubscribeRace2() {
        Flowable<Integer> source = Flowable.just(1).subscribeOn(Schedulers.single()).publish(Functions.identity());
        for (int i = 0; i < 500; i++) {
            source.test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
        }
    }

    @Test
    public void sourceSubscriptionDelayed() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts1 = new TestSubscriber<>(0L);
            Flowable.just(1).publish(f -> {
                Runnable r1 = () -> f.subscribe(ts1);
                Runnable r2 = () -> {
                    for (int j = 0; j < 100; j++) {
                        ts1.request(1);
                    }
                };
                TestHelper.race(r1, r2);
                return f;
            }).test().assertResult(1);
            ts1.assertResult(1);
        }
    }

    @Test
    public void longFlow() {
        Flowable.range(1, 1000000).publish(v -> Flowable.mergeArray(v.filter(w -> w % 2 == 0), v.filter(w -> w % 2 != 0))).takeLast(1).test().assertResult(1000000);
    }

    @Test
    public void longFlow2() {
        Flowable.range(1, 100000).publish(v -> Flowable.mergeArray(v.filter(w -> w % 2 == 0), v.filter(w -> w % 2 != 0))).test().assertValueCount(100000).assertNoErrors().assertComplete();
    }

    @Test
    public void longFlowHidden() {
        Flowable.range(1, 1000000).hide().publish(v -> Flowable.mergeArray(v.filter(w -> w % 2 == 0), v.filter(w -> w % 2 != 0))).takeLast(1).test().assertResult(1000000);
    }

    @Test
    public void noUpstreamCancelOnCasualChainClose() {
        AtomicBoolean parentUpstreamCancelled = new AtomicBoolean(false);
        Flowable.range(1, 10).doOnCancel(() -> parentUpstreamCancelled.set(true)).publish(Functions.identity()).test().awaitDone(1, TimeUnit.SECONDS);
        assertFalse("Unnecessary upstream .cancel() call in FlowablePublishMulticast", parentUpstreamCancelled.get());
    }

    @Test
    public void noUpstreamCancelOnCasualChainCloseWithInnerCancels() {
        AtomicBoolean parentUpstreamCancelled = new AtomicBoolean(false);
        Flowable.range(1, 10).doOnCancel(() -> parentUpstreamCancelled.set(true)).publish(v -> Flowable.concat(v.take(1), v.skip(5))).test().awaitDone(1, TimeUnit.SECONDS);
        assertFalse("Unnecessary upstream .cancel() call in FlowablePublishMulticast", parentUpstreamCancelled.get());
    }

    @Test
    public void upstreamCancelOnDownstreamCancel() {
        AtomicBoolean parentUpstreamCancelled = new AtomicBoolean(false);
        Flowable.range(1, 10).doOnCancel(() -> parentUpstreamCancelled.set(true)).publish(Functions.identity()).take(1).test().awaitDone(1, TimeUnit.SECONDS);
        assertTrue("Upstream .cancel() not called in FlowablePublishMulticast", parentUpstreamCancelled.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowablePublishFunctionTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatTakeFirstLastCompletes() throws java.lang.Throwable {
            this.payloads.concatTakeFirstLastCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatTakeFirstLastBackpressureCompletes() throws java.lang.Throwable {
            this.payloads.concatTakeFirstLastBackpressureCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_canBeCancelled() throws java.lang.Throwable {
            this.payloads.canBeCancelled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_invalidPrefetch() throws java.lang.Throwable {
            this.payloads.invalidPrefetch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeCompletes() throws java.lang.Throwable {
            this.payloads.takeCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneStartOnly() throws java.lang.Throwable {
            this.payloads.oneStartOnly.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeCompletesUnsafe() throws java.lang.Throwable {
            this.payloads.takeCompletesUnsafe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_directCompletesUnsafe() throws java.lang.Throwable {
            this.payloads.directCompletesUnsafe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overflowMissingBackpressureException() throws java.lang.Throwable {
            this.payloads.overflowMissingBackpressureException.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overflowMissingBackpressureExceptionDelayed() throws java.lang.Throwable {
            this.payloads.overflowMissingBackpressureExceptionDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyIdentityMapped() throws java.lang.Throwable {
            this.payloads.emptyIdentityMapped.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_independentlyMapped() throws java.lang.Throwable {
            this.payloads.independentlyMapped.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_frontOverflow() throws java.lang.Throwable {
            this.payloads.frontOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorResubscribe() throws java.lang.Throwable {
            this.payloads.errorResubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedInputCrash() throws java.lang.Throwable {
            this.payloads.fusedInputCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressuredEmpty() throws java.lang.Throwable {
            this.payloads.backpressuredEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneByOne() throws java.lang.Throwable {
            this.payloads.oneByOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeCancelRaceNoRequest() throws java.lang.Throwable {
            this.payloads.completeCancelRaceNoRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_inputOutputSubscribeRace() throws java.lang.Throwable {
            this.payloads.inputOutputSubscribeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_inputOutputSubscribeRace2() throws java.lang.Throwable {
            this.payloads.inputOutputSubscribeRace2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceSubscriptionDelayed() throws java.lang.Throwable {
            this.payloads.sourceSubscriptionDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_longFlow() throws java.lang.Throwable {
            this.payloads.longFlow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_longFlow2() throws java.lang.Throwable {
            this.payloads.longFlow2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_longFlowHidden() throws java.lang.Throwable {
            this.payloads.longFlowHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noUpstreamCancelOnCasualChainClose() throws java.lang.Throwable {
            this.payloads.noUpstreamCancelOnCasualChainClose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noUpstreamCancelOnCasualChainCloseWithInnerCancels() throws java.lang.Throwable {
            this.payloads.noUpstreamCancelOnCasualChainCloseWithInnerCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamCancelOnDownstreamCancel() throws java.lang.Throwable {
            this.payloads.upstreamCancelOnDownstreamCancel.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowablePublishFunctionTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowablePublishFunctionTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowablePublishFunctionTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowablePublishFunctionTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowablePublishFunctionTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowablePublishFunctionTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowablePublishFunctionTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowablePublishFunctionTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement concatTakeFirstLastCompletes;

            public org.junit.runners.model.Statement concatTakeFirstLastBackpressureCompletes;

            public org.junit.runners.model.Statement canBeCancelled;

            public org.junit.runners.model.Statement invalidPrefetch;

            public org.junit.runners.model.Statement takeCompletes;

            public org.junit.runners.model.Statement oneStartOnly;

            public org.junit.runners.model.Statement takeCompletesUnsafe;

            public org.junit.runners.model.Statement directCompletesUnsafe;

            public org.junit.runners.model.Statement overflowMissingBackpressureException;

            public org.junit.runners.model.Statement overflowMissingBackpressureExceptionDelayed;

            public org.junit.runners.model.Statement emptyIdentityMapped;

            public org.junit.runners.model.Statement independentlyMapped;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement frontOverflow;

            public org.junit.runners.model.Statement errorResubscribe;

            public org.junit.runners.model.Statement fusedInputCrash;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement backpressuredEmpty;

            public org.junit.runners.model.Statement oneByOne;

            public org.junit.runners.model.Statement completeCancelRaceNoRequest;

            public org.junit.runners.model.Statement inputOutputSubscribeRace;

            public org.junit.runners.model.Statement inputOutputSubscribeRace2;

            public org.junit.runners.model.Statement sourceSubscriptionDelayed;

            public org.junit.runners.model.Statement longFlow;

            public org.junit.runners.model.Statement longFlow2;

            public org.junit.runners.model.Statement longFlowHidden;

            public org.junit.runners.model.Statement noUpstreamCancelOnCasualChainClose;

            public org.junit.runners.model.Statement noUpstreamCancelOnCasualChainCloseWithInnerCancels;

            public org.junit.runners.model.Statement upstreamCancelOnDownstreamCancel;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.concatTakeFirstLastCompletes = _ClassStatement.forPayload(FlowablePublishFunctionTest::concatTakeFirstLastCompletes, "concatTakeFirstLastCompletes", this);
            this.payloads.concatTakeFirstLastBackpressureCompletes = _ClassStatement.forPayload(FlowablePublishFunctionTest::concatTakeFirstLastBackpressureCompletes, "concatTakeFirstLastBackpressureCompletes", this);
            this.payloads.canBeCancelled = _ClassStatement.forPayload(FlowablePublishFunctionTest::canBeCancelled, "canBeCancelled", this);
            this.payloads.invalidPrefetch = _ClassStatement.forPayload(FlowablePublishFunctionTest::invalidPrefetch, "invalidPrefetch", this);
            this.payloads.takeCompletes = _ClassStatement.forPayload(FlowablePublishFunctionTest::takeCompletes, "takeCompletes", this);
            this.payloads.oneStartOnly = _ClassStatement.forPayload(FlowablePublishFunctionTest::oneStartOnly, "oneStartOnly", this);
            this.payloads.takeCompletesUnsafe = _ClassStatement.forPayload(FlowablePublishFunctionTest::takeCompletesUnsafe, "takeCompletesUnsafe", this);
            this.payloads.directCompletesUnsafe = _ClassStatement.forPayload(FlowablePublishFunctionTest::directCompletesUnsafe, "directCompletesUnsafe", this);
            this.payloads.overflowMissingBackpressureException = _ClassStatement.forPayload(FlowablePublishFunctionTest::overflowMissingBackpressureException, "overflowMissingBackpressureException", this);
            this.payloads.overflowMissingBackpressureExceptionDelayed = _ClassStatement.forPayload(FlowablePublishFunctionTest::overflowMissingBackpressureExceptionDelayed, "overflowMissingBackpressureExceptionDelayed", this);
            this.payloads.emptyIdentityMapped = _ClassStatement.forPayload(FlowablePublishFunctionTest::emptyIdentityMapped, "emptyIdentityMapped", this);
            this.payloads.independentlyMapped = _ClassStatement.forPayload(FlowablePublishFunctionTest::independentlyMapped, "independentlyMapped", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowablePublishFunctionTest::badSource, "badSource", this);
            this.payloads.frontOverflow = _ClassStatement.forPayload(FlowablePublishFunctionTest::frontOverflow, "frontOverflow", this);
            this.payloads.errorResubscribe = _ClassStatement.forPayload(FlowablePublishFunctionTest::errorResubscribe, "errorResubscribe", this);
            this.payloads.fusedInputCrash = _ClassStatement.forPayload(FlowablePublishFunctionTest::fusedInputCrash, "fusedInputCrash", this);
            this.payloads.error = _ClassStatement.forPayload(FlowablePublishFunctionTest::error, "error", this);
            this.payloads.backpressuredEmpty = _ClassStatement.forPayload(FlowablePublishFunctionTest::backpressuredEmpty, "backpressuredEmpty", this);
            this.payloads.oneByOne = _ClassStatement.forPayload(FlowablePublishFunctionTest::oneByOne, "oneByOne", this);
            this.payloads.completeCancelRaceNoRequest = _ClassStatement.forPayload(FlowablePublishFunctionTest::completeCancelRaceNoRequest, "completeCancelRaceNoRequest", this);
            this.payloads.inputOutputSubscribeRace = _ClassStatement.forPayload(FlowablePublishFunctionTest::inputOutputSubscribeRace, "inputOutputSubscribeRace", this);
            this.payloads.inputOutputSubscribeRace2 = _ClassStatement.forPayload(FlowablePublishFunctionTest::inputOutputSubscribeRace2, "inputOutputSubscribeRace2", this);
            this.payloads.sourceSubscriptionDelayed = _ClassStatement.forPayload(FlowablePublishFunctionTest::sourceSubscriptionDelayed, "sourceSubscriptionDelayed", this);
            this.payloads.longFlow = _ClassStatement.forPayload(FlowablePublishFunctionTest::longFlow, "longFlow", this);
            this.payloads.longFlow2 = _ClassStatement.forPayload(FlowablePublishFunctionTest::longFlow2, "longFlow2", this);
            this.payloads.longFlowHidden = _ClassStatement.forPayload(FlowablePublishFunctionTest::longFlowHidden, "longFlowHidden", this);
            this.payloads.noUpstreamCancelOnCasualChainClose = _ClassStatement.forPayload(FlowablePublishFunctionTest::noUpstreamCancelOnCasualChainClose, "noUpstreamCancelOnCasualChainClose", this);
            this.payloads.noUpstreamCancelOnCasualChainCloseWithInnerCancels = _ClassStatement.forPayload(FlowablePublishFunctionTest::noUpstreamCancelOnCasualChainCloseWithInnerCancels, "noUpstreamCancelOnCasualChainCloseWithInnerCancels", this);
            this.payloads.upstreamCancelOnDownstreamCancel = _ClassStatement.forPayload(FlowablePublishFunctionTest::upstreamCancelOnDownstreamCancel, "upstreamCancelOnDownstreamCancel", this);
        }
    }
}
