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

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableDelaySubscriptionOtherTest extends RxJavaTest {

    @Test
    public void noPrematureSubscription() {
        PublishProcessor<Object> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final AtomicInteger subscribed = new AtomicInteger();
        Flowable.just(1).doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) {
                subscribed.getAndIncrement();
            }
        }).delaySubscription(other).subscribe(ts);
        ts.assertNotComplete();
        ts.assertNoErrors();
        ts.assertNoValues();
        Assert.assertEquals("Premature subscription", 0, subscribed.get());
        other.onNext(1);
        Assert.assertEquals("No subscription", 1, subscribed.get());
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void noMultipleSubscriptions() {
        PublishProcessor<Object> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final AtomicInteger subscribed = new AtomicInteger();
        Flowable.just(1).doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) {
                subscribed.getAndIncrement();
            }
        }).delaySubscription(other).subscribe(ts);
        ts.assertNotComplete();
        ts.assertNoErrors();
        ts.assertNoValues();
        Assert.assertEquals("Premature subscription", 0, subscribed.get());
        other.onNext(1);
        other.onNext(2);
        Assert.assertEquals("No subscription", 1, subscribed.get());
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void completeTriggersSubscription() {
        PublishProcessor<Object> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final AtomicInteger subscribed = new AtomicInteger();
        Flowable.just(1).doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) {
                subscribed.getAndIncrement();
            }
        }).delaySubscription(other).subscribe(ts);
        ts.assertNotComplete();
        ts.assertNoErrors();
        ts.assertNoValues();
        Assert.assertEquals("Premature subscription", 0, subscribed.get());
        other.onComplete();
        Assert.assertEquals("No subscription", 1, subscribed.get());
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void noPrematureSubscriptionToError() {
        PublishProcessor<Object> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final AtomicInteger subscribed = new AtomicInteger();
        Flowable.<Integer>error(new TestException()).doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) {
                subscribed.getAndIncrement();
            }
        }).delaySubscription(other).subscribe(ts);
        ts.assertNotComplete();
        ts.assertNoErrors();
        ts.assertNoValues();
        Assert.assertEquals("Premature subscription", 0, subscribed.get());
        other.onComplete();
        Assert.assertEquals("No subscription", 1, subscribed.get());
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(TestException.class);
    }

    @Test
    public void noSubscriptionIfOtherErrors() {
        PublishProcessor<Object> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final AtomicInteger subscribed = new AtomicInteger();
        Flowable.<Integer>error(new TestException()).doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) {
                subscribed.getAndIncrement();
            }
        }).delaySubscription(other).subscribe(ts);
        ts.assertNotComplete();
        ts.assertNoErrors();
        ts.assertNoValues();
        Assert.assertEquals("Premature subscription", 0, subscribed.get());
        other.onError(new TestException());
        Assert.assertEquals("Premature subscription", 0, subscribed.get());
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(TestException.class);
    }

    @Test
    public void backpressurePassesThrough() {
        PublishProcessor<Object> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        final AtomicInteger subscribed = new AtomicInteger();
        Flowable.just(1, 2, 3, 4, 5).doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) {
                subscribed.getAndIncrement();
            }
        }).delaySubscription(other).subscribe(ts);
        ts.assertNotComplete();
        ts.assertNoErrors();
        ts.assertNoValues();
        Assert.assertEquals("Premature subscription", 0, subscribed.get());
        other.onNext(1);
        Assert.assertEquals("No subscription", 1, subscribed.get());
        Assert.assertFalse("Not unsubscribed from other", other.hasSubscribers());
        ts.assertNotComplete();
        ts.assertNoErrors();
        ts.assertNoValues();
        ts.request(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(2);
        ts.assertValues(1, 2, 3);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(10);
        ts.assertValues(1, 2, 3, 4, 5);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void unsubscriptionPropagatesBeforeSubscribe() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        source.delaySubscription(other).subscribe(ts);
        Assert.assertFalse("source subscribed?", source.hasSubscribers());
        Assert.assertTrue("other not subscribed?", other.hasSubscribers());
        ts.cancel();
        Assert.assertFalse("source subscribed?", source.hasSubscribers());
        Assert.assertFalse("other still subscribed?", other.hasSubscribers());
    }

    @Test
    public void unsubscriptionPropagatesAfterSubscribe() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        source.delaySubscription(other).subscribe(ts);
        Assert.assertFalse("source subscribed?", source.hasSubscribers());
        Assert.assertTrue("other not subscribed?", other.hasSubscribers());
        other.onComplete();
        Assert.assertTrue("source not subscribed?", source.hasSubscribers());
        Assert.assertFalse("other still subscribed?", other.hasSubscribers());
        ts.cancel();
        Assert.assertFalse("source subscribed?", source.hasSubscribers());
        Assert.assertFalse("other still subscribed?", other.hasSubscribers());
    }

    @Test
    public void delayAndTakeUntilNeverSubscribeToSource() {
        PublishProcessor<Integer> delayUntil = PublishProcessor.create();
        PublishProcessor<Integer> interrupt = PublishProcessor.create();
        final AtomicBoolean subscribed = new AtomicBoolean(false);
        Flowable.just(1).doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) {
                subscribed.set(true);
            }
        }).delaySubscription(delayUntil).takeUntil(interrupt).subscribe();
        interrupt.onNext(9000);
        delayUntil.onNext(1);
        Assert.assertFalse(subscribed.get());
    }

    @Test
    public void badSourceOther() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return Flowable.just(1).delaySubscription(f);
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void afterDelayNoInterrupt() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        try {
            for (Scheduler s : new Scheduler[] { Schedulers.single(), Schedulers.computation(), Schedulers.newThread(), Schedulers.io(), Schedulers.from(exec) }) {
                final TestSubscriber<Boolean> ts = TestSubscriber.create();
                ts.withTag(s.getClass().getSimpleName());
                Flowable.<Boolean>create(new FlowableOnSubscribe<Boolean>() {

                    @Override
                    public void subscribe(FlowableEmitter<Boolean> emitter) throws Exception {
                        emitter.onNext(Thread.interrupted());
                        emitter.onComplete();
                    }
                }, BackpressureStrategy.MISSING).delaySubscription(100, TimeUnit.MILLISECONDS, s).subscribe(ts);
                ts.awaitDone(5, TimeUnit.SECONDS);
                ts.assertValue(false);
            }
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void doubleOnSubscribeMain() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.delaySubscription(Flowable.empty()));
    }

    @Test
    public void doubleOnSubscribeOther() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> PublishProcessor.create().delaySubscription(f));
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(PublishProcessor.create().delaySubscription(Flowable.empty()));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableDelaySubscriptionOtherTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noPrematureSubscription() throws java.lang.Throwable {
            this.payloads.noPrematureSubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noMultipleSubscriptions() throws java.lang.Throwable {
            this.payloads.noMultipleSubscriptions.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeTriggersSubscription() throws java.lang.Throwable {
            this.payloads.completeTriggersSubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noPrematureSubscriptionToError() throws java.lang.Throwable {
            this.payloads.noPrematureSubscriptionToError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubscriptionIfOtherErrors() throws java.lang.Throwable {
            this.payloads.noSubscriptionIfOtherErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressurePassesThrough() throws java.lang.Throwable {
            this.payloads.backpressurePassesThrough.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscriptionPropagatesBeforeSubscribe() throws java.lang.Throwable {
            this.payloads.unsubscriptionPropagatesBeforeSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscriptionPropagatesAfterSubscribe() throws java.lang.Throwable {
            this.payloads.unsubscriptionPropagatesAfterSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayAndTakeUntilNeverSubscribeToSource() throws java.lang.Throwable {
            this.payloads.delayAndTakeUntilNeverSubscribeToSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceOther() throws java.lang.Throwable {
            this.payloads.badSourceOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_afterDelayNoInterrupt() throws java.lang.Throwable {
            this.payloads.afterDelayNoInterrupt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeMain() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribeMain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeOther() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribeOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDelaySubscriptionOtherTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDelaySubscriptionOtherTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDelaySubscriptionOtherTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDelaySubscriptionOtherTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableDelaySubscriptionOtherTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDelaySubscriptionOtherTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableDelaySubscriptionOtherTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableDelaySubscriptionOtherTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement noPrematureSubscription;

            public org.junit.runners.model.Statement noMultipleSubscriptions;

            public org.junit.runners.model.Statement completeTriggersSubscription;

            public org.junit.runners.model.Statement noPrematureSubscriptionToError;

            public org.junit.runners.model.Statement noSubscriptionIfOtherErrors;

            public org.junit.runners.model.Statement backpressurePassesThrough;

            public org.junit.runners.model.Statement unsubscriptionPropagatesBeforeSubscribe;

            public org.junit.runners.model.Statement unsubscriptionPropagatesAfterSubscribe;

            public org.junit.runners.model.Statement delayAndTakeUntilNeverSubscribeToSource;

            public org.junit.runners.model.Statement badSourceOther;

            public org.junit.runners.model.Statement afterDelayNoInterrupt;

            public org.junit.runners.model.Statement doubleOnSubscribeMain;

            public org.junit.runners.model.Statement doubleOnSubscribeOther;

            public org.junit.runners.model.Statement badRequest;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.noPrematureSubscription = _ClassStatement.forPayload(FlowableDelaySubscriptionOtherTest::noPrematureSubscription, "noPrematureSubscription", this);
            this.payloads.noMultipleSubscriptions = _ClassStatement.forPayload(FlowableDelaySubscriptionOtherTest::noMultipleSubscriptions, "noMultipleSubscriptions", this);
            this.payloads.completeTriggersSubscription = _ClassStatement.forPayload(FlowableDelaySubscriptionOtherTest::completeTriggersSubscription, "completeTriggersSubscription", this);
            this.payloads.noPrematureSubscriptionToError = _ClassStatement.forPayload(FlowableDelaySubscriptionOtherTest::noPrematureSubscriptionToError, "noPrematureSubscriptionToError", this);
            this.payloads.noSubscriptionIfOtherErrors = _ClassStatement.forPayload(FlowableDelaySubscriptionOtherTest::noSubscriptionIfOtherErrors, "noSubscriptionIfOtherErrors", this);
            this.payloads.backpressurePassesThrough = _ClassStatement.forPayload(FlowableDelaySubscriptionOtherTest::backpressurePassesThrough, "backpressurePassesThrough", this);
            this.payloads.unsubscriptionPropagatesBeforeSubscribe = _ClassStatement.forPayload(FlowableDelaySubscriptionOtherTest::unsubscriptionPropagatesBeforeSubscribe, "unsubscriptionPropagatesBeforeSubscribe", this);
            this.payloads.unsubscriptionPropagatesAfterSubscribe = _ClassStatement.forPayload(FlowableDelaySubscriptionOtherTest::unsubscriptionPropagatesAfterSubscribe, "unsubscriptionPropagatesAfterSubscribe", this);
            this.payloads.delayAndTakeUntilNeverSubscribeToSource = _ClassStatement.forPayload(FlowableDelaySubscriptionOtherTest::delayAndTakeUntilNeverSubscribeToSource, "delayAndTakeUntilNeverSubscribeToSource", this);
            this.payloads.badSourceOther = _ClassStatement.forPayload(FlowableDelaySubscriptionOtherTest::badSourceOther, "badSourceOther", this);
            this.payloads.afterDelayNoInterrupt = _ClassStatement.forPayload(FlowableDelaySubscriptionOtherTest::afterDelayNoInterrupt, "afterDelayNoInterrupt", this);
            this.payloads.doubleOnSubscribeMain = _ClassStatement.forPayload(FlowableDelaySubscriptionOtherTest::doubleOnSubscribeMain, "doubleOnSubscribeMain", this);
            this.payloads.doubleOnSubscribeOther = _ClassStatement.forPayload(FlowableDelaySubscriptionOtherTest::doubleOnSubscribeOther, "doubleOnSubscribeOther", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableDelaySubscriptionOtherTest::badRequest, "badRequest", this);
        }
    }
}
