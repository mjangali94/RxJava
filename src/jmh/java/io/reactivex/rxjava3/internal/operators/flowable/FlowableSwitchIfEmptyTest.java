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
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.*;

public class FlowableSwitchIfEmptyTest extends RxJavaTest {

    @Test
    public void switchWhenNotEmpty() throws Exception {
        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Flowable<Integer> flowable = Flowable.just(4).switchIfEmpty(Flowable.just(2).doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) {
                subscribed.set(true);
            }
        }));
        assertEquals(4, flowable.blockingSingle().intValue());
        assertFalse(subscribed.get());
    }

    @Test
    public void switchWhenEmpty() throws Exception {
        final Flowable<Integer> flowable = Flowable.<Integer>empty().switchIfEmpty(Flowable.fromIterable(Arrays.asList(42)));
        assertEquals(42, flowable.blockingSingle().intValue());
    }

    @Test
    public void switchWithProducer() throws Exception {
        final AtomicBoolean emitted = new AtomicBoolean(false);
        Flowable<Long> withProducer = Flowable.unsafeCreate(new Publisher<Long>() {

            @Override
            public void subscribe(final Subscriber<? super Long> subscriber) {
                subscriber.onSubscribe(new Subscription() {

                    @Override
                    public void request(long n) {
                        if (n > 0 && emitted.compareAndSet(false, true)) {
                            emitted.set(true);
                            subscriber.onNext(42L);
                            subscriber.onComplete();
                        }
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        });
        final Flowable<Long> flowable = Flowable.<Long>empty().switchIfEmpty(withProducer);
        assertEquals(42, flowable.blockingSingle().intValue());
    }

    @Test
    public void switchTriggerUnsubscribe() throws Exception {
        final BooleanSubscription bs = new BooleanSubscription();
        Flowable<Long> withProducer = Flowable.unsafeCreate(new Publisher<Long>() {

            @Override
            public void subscribe(final Subscriber<? super Long> subscriber) {
                subscriber.onSubscribe(bs);
                subscriber.onNext(42L);
            }
        });
        Flowable.<Long>empty().switchIfEmpty(withProducer).lift(new FlowableOperator<Long, Long>() {

            @Override
            public Subscriber<? super Long> apply(final Subscriber<? super Long> child) {
                return new DefaultSubscriber<Long>() {

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Long aLong) {
                        cancel();
                    }
                };
            }
        }).subscribe();
        assertTrue(bs.isCancelled());
    // FIXME no longer assertable
    // assertTrue(sub.isUnsubscribed());
    }

    @Test
    public void switchShouldNotTriggerUnsubscribe() {
        final BooleanSubscription bs = new BooleanSubscription();
        Flowable.unsafeCreate(new Publisher<Long>() {

            @Override
            public void subscribe(final Subscriber<? super Long> subscriber) {
                subscriber.onSubscribe(bs);
                subscriber.onComplete();
            }
        }).switchIfEmpty(Flowable.<Long>never()).subscribe();
        assertFalse(bs.isCancelled());
    }

    @Test
    public void switchRequestAlternativeObservableWithBackpressure() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(1L);
        Flowable.<Integer>empty().switchIfEmpty(Flowable.just(1, 2, 3)).subscribe(ts);
        assertEquals(Arrays.asList(1), ts.values());
        ts.assertNoErrors();
        ts.request(1);
        ts.assertValueCount(2);
        ts.request(1);
        ts.assertValueCount(3);
    }

    @Test
    public void backpressureNoRequest() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        Flowable.<Integer>empty().switchIfEmpty(Flowable.just(1, 2, 3)).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
    }

    @Test
    public void backpressureOnFirstObservable() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        Flowable.just(1, 2, 3).switchIfEmpty(Flowable.just(4, 5, 6)).subscribe(ts);
        ts.assertNotComplete();
        ts.assertNoErrors();
        ts.assertNoValues();
    }

    @Test
    public void requestsNotLost() throws InterruptedException {
        final TestSubscriber<Long> ts = new TestSubscriber<>(0L);
        Flowable.unsafeCreate(new Publisher<Long>() {

            @Override
            public void subscribe(final Subscriber<? super Long> subscriber) {
                subscriber.onSubscribe(new Subscription() {

                    final AtomicBoolean completed = new AtomicBoolean(false);

                    @Override
                    public void request(long n) {
                        if (n > 0 && completed.compareAndSet(false, true)) {
                            Schedulers.io().createWorker().schedule(new Runnable() {

                                @Override
                                public void run() {
                                    subscriber.onComplete();
                                }
                            }, 100, TimeUnit.MILLISECONDS);
                        }
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        }).switchIfEmpty(Flowable.fromIterable(Arrays.asList(1L, 2L, 3L))).subscribeOn(Schedulers.computation()).subscribe(ts);
        Thread.sleep(50);
        // request while first observable is still finishing (as empty)
        ts.request(1);
        ts.request(1);
        Thread.sleep(500);
        ts.assertNotComplete();
        ts.assertNoErrors();
        ts.assertValueCount(2);
        ts.cancel();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableSwitchIfEmptyTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchWhenNotEmpty() throws java.lang.Throwable {
            this.payloads.switchWhenNotEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchWhenEmpty() throws java.lang.Throwable {
            this.payloads.switchWhenEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchWithProducer() throws java.lang.Throwable {
            this.payloads.switchWithProducer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchTriggerUnsubscribe() throws java.lang.Throwable {
            this.payloads.switchTriggerUnsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchShouldNotTriggerUnsubscribe() throws java.lang.Throwable {
            this.payloads.switchShouldNotTriggerUnsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchRequestAlternativeObservableWithBackpressure() throws java.lang.Throwable {
            this.payloads.switchRequestAlternativeObservableWithBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNoRequest() throws java.lang.Throwable {
            this.payloads.backpressureNoRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureOnFirstObservable() throws java.lang.Throwable {
            this.payloads.backpressureOnFirstObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestsNotLost() throws java.lang.Throwable {
            this.payloads.requestsNotLost.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSwitchIfEmptyTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSwitchIfEmptyTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSwitchIfEmptyTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSwitchIfEmptyTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableSwitchIfEmptyTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSwitchIfEmptyTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableSwitchIfEmptyTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableSwitchIfEmptyTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement switchWhenNotEmpty;

            public org.junit.runners.model.Statement switchWhenEmpty;

            public org.junit.runners.model.Statement switchWithProducer;

            public org.junit.runners.model.Statement switchTriggerUnsubscribe;

            public org.junit.runners.model.Statement switchShouldNotTriggerUnsubscribe;

            public org.junit.runners.model.Statement switchRequestAlternativeObservableWithBackpressure;

            public org.junit.runners.model.Statement backpressureNoRequest;

            public org.junit.runners.model.Statement backpressureOnFirstObservable;

            public org.junit.runners.model.Statement requestsNotLost;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.switchWhenNotEmpty = _ClassStatement.forPayload(FlowableSwitchIfEmptyTest::switchWhenNotEmpty, "switchWhenNotEmpty", this);
            this.payloads.switchWhenEmpty = _ClassStatement.forPayload(FlowableSwitchIfEmptyTest::switchWhenEmpty, "switchWhenEmpty", this);
            this.payloads.switchWithProducer = _ClassStatement.forPayload(FlowableSwitchIfEmptyTest::switchWithProducer, "switchWithProducer", this);
            this.payloads.switchTriggerUnsubscribe = _ClassStatement.forPayload(FlowableSwitchIfEmptyTest::switchTriggerUnsubscribe, "switchTriggerUnsubscribe", this);
            this.payloads.switchShouldNotTriggerUnsubscribe = _ClassStatement.forPayload(FlowableSwitchIfEmptyTest::switchShouldNotTriggerUnsubscribe, "switchShouldNotTriggerUnsubscribe", this);
            this.payloads.switchRequestAlternativeObservableWithBackpressure = _ClassStatement.forPayload(FlowableSwitchIfEmptyTest::switchRequestAlternativeObservableWithBackpressure, "switchRequestAlternativeObservableWithBackpressure", this);
            this.payloads.backpressureNoRequest = _ClassStatement.forPayload(FlowableSwitchIfEmptyTest::backpressureNoRequest, "backpressureNoRequest", this);
            this.payloads.backpressureOnFirstObservable = _ClassStatement.forPayload(FlowableSwitchIfEmptyTest::backpressureOnFirstObservable, "backpressureOnFirstObservable", this);
            this.payloads.requestsNotLost = _ClassStatement.forPayload(FlowableSwitchIfEmptyTest::requestsNotLost, "requestsNotLost", this);
        }
    }
}
