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
package io.reactivex.rxjava3.internal.operators.maybe;

import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeDelaySubscriptionTest extends RxJavaTest {

    @Test
    public void normal() {
        PublishProcessor<Object> pp = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.just(1).delaySubscription(pp).test();
        assertTrue(pp.hasSubscribers());
        to.assertEmpty();
        pp.onNext("one");
        assertFalse(pp.hasSubscribers());
        to.assertResult(1);
    }

    @Test
    public void timed() {
        Maybe.just(1).delaySubscription(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void timedEmpty() {
        Maybe.<Integer>empty().delaySubscription(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void timedTestScheduler() {
        TestScheduler scheduler = new TestScheduler();
        TestObserver<Integer> to = Maybe.just(1).delaySubscription(100, TimeUnit.MILLISECONDS, scheduler).test();
        to.assertEmpty();
        scheduler.advanceTimeBy(99, TimeUnit.MILLISECONDS);
        to.assertEmpty();
        scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        to.assertResult(1);
    }

    @Test
    public void otherError() {
        Maybe.just(1).delaySubscription(Flowable.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void mainError() {
        Maybe.error(new TestException()).delaySubscription(Flowable.empty()).test().assertFailure(TestException.class);
    }

    @Test
    public void withPublisherDispose() {
        TestHelper.checkDisposed(Maybe.just(1).delaySubscription(Flowable.never()));
    }

    @Test
    public void withPublisherDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Maybe<Object> m) throws Exception {
                return m.delaySubscription(Flowable.just(1));
            }
        });
    }

    @Test
    public void withPublisherCallAfterTerminalEvent() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable<Integer> f = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                    subscriber.onNext(2);
                }
            };
            Maybe.just(1).delaySubscription(f).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doubleOnSubscribePublisher() {
        TestHelper.checkDoubleOnSubscribeFlowableToMaybe(f -> Maybe.just(1).delaySubscription(f));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeDelaySubscriptionTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timed() throws java.lang.Throwable {
            this.payloads.timed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedEmpty() throws java.lang.Throwable {
            this.payloads.timedEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedTestScheduler() throws java.lang.Throwable {
            this.payloads.timedTestScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherError() throws java.lang.Throwable {
            this.payloads.otherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.payloads.mainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withPublisherDispose() throws java.lang.Throwable {
            this.payloads.withPublisherDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withPublisherDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.withPublisherDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withPublisherCallAfterTerminalEvent() throws java.lang.Throwable {
            this.payloads.withPublisherCallAfterTerminalEvent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribePublisher() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribePublisher.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDelaySubscriptionTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDelaySubscriptionTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDelaySubscriptionTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDelaySubscriptionTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeDelaySubscriptionTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDelaySubscriptionTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeDelaySubscriptionTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeDelaySubscriptionTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement timed;

            public org.junit.runners.model.Statement timedEmpty;

            public org.junit.runners.model.Statement timedTestScheduler;

            public org.junit.runners.model.Statement otherError;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement withPublisherDispose;

            public org.junit.runners.model.Statement withPublisherDoubleOnSubscribe;

            public org.junit.runners.model.Statement withPublisherCallAfterTerminalEvent;

            public org.junit.runners.model.Statement doubleOnSubscribePublisher;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(MaybeDelaySubscriptionTest::normal, "normal", this);
            this.payloads.timed = _ClassStatement.forPayload(MaybeDelaySubscriptionTest::timed, "timed", this);
            this.payloads.timedEmpty = _ClassStatement.forPayload(MaybeDelaySubscriptionTest::timedEmpty, "timedEmpty", this);
            this.payloads.timedTestScheduler = _ClassStatement.forPayload(MaybeDelaySubscriptionTest::timedTestScheduler, "timedTestScheduler", this);
            this.payloads.otherError = _ClassStatement.forPayload(MaybeDelaySubscriptionTest::otherError, "otherError", this);
            this.payloads.mainError = _ClassStatement.forPayload(MaybeDelaySubscriptionTest::mainError, "mainError", this);
            this.payloads.withPublisherDispose = _ClassStatement.forPayload(MaybeDelaySubscriptionTest::withPublisherDispose, "withPublisherDispose", this);
            this.payloads.withPublisherDoubleOnSubscribe = _ClassStatement.forPayload(MaybeDelaySubscriptionTest::withPublisherDoubleOnSubscribe, "withPublisherDoubleOnSubscribe", this);
            this.payloads.withPublisherCallAfterTerminalEvent = _ClassStatement.forPayload(MaybeDelaySubscriptionTest::withPublisherCallAfterTerminalEvent, "withPublisherCallAfterTerminalEvent", this);
            this.payloads.doubleOnSubscribePublisher = _ClassStatement.forPayload(MaybeDelaySubscriptionTest::doubleOnSubscribePublisher, "doubleOnSubscribePublisher", this);
        }
    }
}
