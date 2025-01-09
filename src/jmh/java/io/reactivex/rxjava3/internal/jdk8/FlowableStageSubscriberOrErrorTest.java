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
package io.reactivex.rxjava3.internal.jdk8;

import static org.junit.Assert.*;
import java.util.NoSuchElementException;
import java.util.concurrent.*;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableStageSubscriberOrErrorTest extends RxJavaTest {

    @Test
    public void firstJust() throws Exception {
        Integer v = Flowable.just(1).firstOrErrorStage().toCompletableFuture().get();
        assertEquals((Integer) 1, v);
    }

    @Test
    public void firstEmpty() throws Exception {
        TestHelper.assertError(Flowable.<Integer>empty().firstOrErrorStage().toCompletableFuture(), NoSuchElementException.class);
    }

    @Test
    public void firstCancels() throws Exception {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        Integer v = source.firstOrErrorStage().toCompletableFuture().get();
        assertEquals((Integer) 1, v);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void firstCompletableFutureCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.firstOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.cancel(true);
        assertTrue(cf.isCancelled());
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void firstCompletableManualCompleteCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.firstOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.complete(1);
        assertTrue(cf.isDone());
        assertFalse(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        assertFalse(source.hasSubscribers());
        assertEquals((Integer) 1, cf.get());
    }

    @Test
    public void firstCompletableManualCompleteExceptionallyCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.firstOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.completeExceptionally(new TestException());
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        assertFalse(source.hasSubscribers());
        TestHelper.assertError(cf, TestException.class);
    }

    @Test
    public void firstError() throws Exception {
        CompletableFuture<Integer> cf = Flowable.<Integer>error(new TestException()).firstOrErrorStage().toCompletableFuture();
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        TestHelper.assertError(cf, TestException.class);
    }

    @Test
    public void firstSourceIgnoresCancel() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Integer v = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onError(new TestException());
                    s.onComplete();
                }
            }.firstOrErrorStage().toCompletableFuture().get();
            assertEquals((Integer) 1, v);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void firstDoubleOnSubscribe() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Integer v = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                }
            }.firstOrErrorStage().toCompletableFuture().get();
            assertEquals((Integer) 1, v);
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        });
    }

    @Test
    public void singleJust() throws Exception {
        Integer v = Flowable.just(1).singleOrErrorStage().toCompletableFuture().get();
        assertEquals((Integer) 1, v);
    }

    @Test
    public void singleEmpty() throws Exception {
        TestHelper.assertError(Flowable.<Integer>empty().singleOrErrorStage().toCompletableFuture(), NoSuchElementException.class);
    }

    @Test
    public void singleTooManyCancels() throws Exception {
        ReplayProcessor<Integer> source = ReplayProcessor.create();
        source.onNext(1);
        source.onNext(2);
        TestHelper.assertError(source.singleOrErrorStage().toCompletableFuture(), IllegalArgumentException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void singleCompletableFutureCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.singleOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.cancel(true);
        assertTrue(cf.isCancelled());
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void singleCompletableManualCompleteCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.singleOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.complete(1);
        assertTrue(cf.isDone());
        assertFalse(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        assertFalse(source.hasSubscribers());
        assertEquals((Integer) 1, cf.get());
    }

    @Test
    public void singleCompletableManualCompleteExceptionallyCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.singleOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.completeExceptionally(new TestException());
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        assertFalse(source.hasSubscribers());
        TestHelper.assertError(cf, TestException.class);
    }

    @Test
    public void singleError() throws Exception {
        CompletableFuture<Integer> cf = Flowable.<Integer>error(new TestException()).singleOrErrorStage().toCompletableFuture();
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        TestHelper.assertError(cf, TestException.class);
    }

    @Test
    public void singleSourceIgnoresCancel() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Integer v = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onComplete();
                    s.onError(new TestException());
                    s.onComplete();
                }
            }.singleOrErrorStage().toCompletableFuture().get();
            assertEquals((Integer) 1, v);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void singleDoubleOnSubscribe() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Integer v = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onComplete();
                }
            }.singleOrErrorStage().toCompletableFuture().get();
            assertEquals((Integer) 1, v);
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        });
    }

    @Test
    public void lastJust() throws Exception {
        Integer v = Flowable.just(1).lastOrErrorStage().toCompletableFuture().get();
        assertEquals((Integer) 1, v);
    }

    @Test
    public void lastRange() throws Exception {
        Integer v = Flowable.range(1, 5).lastOrErrorStage().toCompletableFuture().get();
        assertEquals((Integer) 5, v);
    }

    @Test
    public void lastEmpty() throws Exception {
        TestHelper.assertError(Flowable.<Integer>empty().lastOrErrorStage().toCompletableFuture(), NoSuchElementException.class);
    }

    @Test
    public void lastCompletableFutureCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.lastOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.cancel(true);
        assertTrue(cf.isCancelled());
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void lastCompletableManualCompleteCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.lastOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.complete(1);
        assertTrue(cf.isDone());
        assertFalse(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        assertFalse(source.hasSubscribers());
        assertEquals((Integer) 1, cf.get());
    }

    @Test
    public void lastCompletableManualCompleteExceptionallyCancels() throws Exception {
        PublishProcessor<Integer> source = PublishProcessor.create();
        CompletableFuture<Integer> cf = source.lastOrErrorStage().toCompletableFuture();
        assertTrue(source.hasSubscribers());
        cf.completeExceptionally(new TestException());
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        assertFalse(source.hasSubscribers());
        TestHelper.assertError(cf, TestException.class);
    }

    @Test
    public void lastError() throws Exception {
        CompletableFuture<Integer> cf = Flowable.<Integer>error(new TestException()).lastOrErrorStage().toCompletableFuture();
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        TestHelper.assertError(cf, TestException.class);
    }

    @Test
    public void lastSourceIgnoresCancel() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Integer v = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onComplete();
                    s.onError(new TestException());
                    s.onComplete();
                }
            }.lastOrErrorStage().toCompletableFuture().get();
            assertEquals((Integer) 1, v);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void lastDoubleOnSubscribe() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Integer v = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onComplete();
                }
            }.lastOrErrorStage().toCompletableFuture().get();
            assertEquals((Integer) 1, v);
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableStageSubscriberOrErrorTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstJust() throws java.lang.Throwable {
            this.payloads.firstJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstEmpty() throws java.lang.Throwable {
            this.payloads.firstEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstCancels() throws java.lang.Throwable {
            this.payloads.firstCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstCompletableFutureCancels() throws java.lang.Throwable {
            this.payloads.firstCompletableFutureCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstCompletableManualCompleteCancels() throws java.lang.Throwable {
            this.payloads.firstCompletableManualCompleteCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstCompletableManualCompleteExceptionallyCancels() throws java.lang.Throwable {
            this.payloads.firstCompletableManualCompleteExceptionallyCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstError() throws java.lang.Throwable {
            this.payloads.firstError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstSourceIgnoresCancel() throws java.lang.Throwable {
            this.payloads.firstSourceIgnoresCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.firstDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleJust() throws java.lang.Throwable {
            this.payloads.singleJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleEmpty() throws java.lang.Throwable {
            this.payloads.singleEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleTooManyCancels() throws java.lang.Throwable {
            this.payloads.singleTooManyCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleCompletableFutureCancels() throws java.lang.Throwable {
            this.payloads.singleCompletableFutureCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleCompletableManualCompleteCancels() throws java.lang.Throwable {
            this.payloads.singleCompletableManualCompleteCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleCompletableManualCompleteExceptionallyCancels() throws java.lang.Throwable {
            this.payloads.singleCompletableManualCompleteExceptionallyCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleError() throws java.lang.Throwable {
            this.payloads.singleError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSourceIgnoresCancel() throws java.lang.Throwable {
            this.payloads.singleSourceIgnoresCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.singleDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastJust() throws java.lang.Throwable {
            this.payloads.lastJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastRange() throws java.lang.Throwable {
            this.payloads.lastRange.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastEmpty() throws java.lang.Throwable {
            this.payloads.lastEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastCompletableFutureCancels() throws java.lang.Throwable {
            this.payloads.lastCompletableFutureCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastCompletableManualCompleteCancels() throws java.lang.Throwable {
            this.payloads.lastCompletableManualCompleteCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastCompletableManualCompleteExceptionallyCancels() throws java.lang.Throwable {
            this.payloads.lastCompletableManualCompleteExceptionallyCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastError() throws java.lang.Throwable {
            this.payloads.lastError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastSourceIgnoresCancel() throws java.lang.Throwable {
            this.payloads.lastSourceIgnoresCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lastDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.lastDoubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableStageSubscriberOrErrorTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableStageSubscriberOrErrorTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableStageSubscriberOrErrorTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableStageSubscriberOrErrorTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableStageSubscriberOrErrorTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableStageSubscriberOrErrorTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableStageSubscriberOrErrorTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableStageSubscriberOrErrorTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement firstJust;

            public org.junit.runners.model.Statement firstEmpty;

            public org.junit.runners.model.Statement firstCancels;

            public org.junit.runners.model.Statement firstCompletableFutureCancels;

            public org.junit.runners.model.Statement firstCompletableManualCompleteCancels;

            public org.junit.runners.model.Statement firstCompletableManualCompleteExceptionallyCancels;

            public org.junit.runners.model.Statement firstError;

            public org.junit.runners.model.Statement firstSourceIgnoresCancel;

            public org.junit.runners.model.Statement firstDoubleOnSubscribe;

            public org.junit.runners.model.Statement singleJust;

            public org.junit.runners.model.Statement singleEmpty;

            public org.junit.runners.model.Statement singleTooManyCancels;

            public org.junit.runners.model.Statement singleCompletableFutureCancels;

            public org.junit.runners.model.Statement singleCompletableManualCompleteCancels;

            public org.junit.runners.model.Statement singleCompletableManualCompleteExceptionallyCancels;

            public org.junit.runners.model.Statement singleError;

            public org.junit.runners.model.Statement singleSourceIgnoresCancel;

            public org.junit.runners.model.Statement singleDoubleOnSubscribe;

            public org.junit.runners.model.Statement lastJust;

            public org.junit.runners.model.Statement lastRange;

            public org.junit.runners.model.Statement lastEmpty;

            public org.junit.runners.model.Statement lastCompletableFutureCancels;

            public org.junit.runners.model.Statement lastCompletableManualCompleteCancels;

            public org.junit.runners.model.Statement lastCompletableManualCompleteExceptionallyCancels;

            public org.junit.runners.model.Statement lastError;

            public org.junit.runners.model.Statement lastSourceIgnoresCancel;

            public org.junit.runners.model.Statement lastDoubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.firstJust = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::firstJust, "firstJust", this);
            this.payloads.firstEmpty = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::firstEmpty, "firstEmpty", this);
            this.payloads.firstCancels = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::firstCancels, "firstCancels", this);
            this.payloads.firstCompletableFutureCancels = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::firstCompletableFutureCancels, "firstCompletableFutureCancels", this);
            this.payloads.firstCompletableManualCompleteCancels = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::firstCompletableManualCompleteCancels, "firstCompletableManualCompleteCancels", this);
            this.payloads.firstCompletableManualCompleteExceptionallyCancels = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::firstCompletableManualCompleteExceptionallyCancels, "firstCompletableManualCompleteExceptionallyCancels", this);
            this.payloads.firstError = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::firstError, "firstError", this);
            this.payloads.firstSourceIgnoresCancel = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::firstSourceIgnoresCancel, "firstSourceIgnoresCancel", this);
            this.payloads.firstDoubleOnSubscribe = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::firstDoubleOnSubscribe, "firstDoubleOnSubscribe", this);
            this.payloads.singleJust = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::singleJust, "singleJust", this);
            this.payloads.singleEmpty = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::singleEmpty, "singleEmpty", this);
            this.payloads.singleTooManyCancels = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::singleTooManyCancels, "singleTooManyCancels", this);
            this.payloads.singleCompletableFutureCancels = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::singleCompletableFutureCancels, "singleCompletableFutureCancels", this);
            this.payloads.singleCompletableManualCompleteCancels = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::singleCompletableManualCompleteCancels, "singleCompletableManualCompleteCancels", this);
            this.payloads.singleCompletableManualCompleteExceptionallyCancels = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::singleCompletableManualCompleteExceptionallyCancels, "singleCompletableManualCompleteExceptionallyCancels", this);
            this.payloads.singleError = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::singleError, "singleError", this);
            this.payloads.singleSourceIgnoresCancel = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::singleSourceIgnoresCancel, "singleSourceIgnoresCancel", this);
            this.payloads.singleDoubleOnSubscribe = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::singleDoubleOnSubscribe, "singleDoubleOnSubscribe", this);
            this.payloads.lastJust = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::lastJust, "lastJust", this);
            this.payloads.lastRange = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::lastRange, "lastRange", this);
            this.payloads.lastEmpty = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::lastEmpty, "lastEmpty", this);
            this.payloads.lastCompletableFutureCancels = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::lastCompletableFutureCancels, "lastCompletableFutureCancels", this);
            this.payloads.lastCompletableManualCompleteCancels = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::lastCompletableManualCompleteCancels, "lastCompletableManualCompleteCancels", this);
            this.payloads.lastCompletableManualCompleteExceptionallyCancels = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::lastCompletableManualCompleteExceptionallyCancels, "lastCompletableManualCompleteExceptionallyCancels", this);
            this.payloads.lastError = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::lastError, "lastError", this);
            this.payloads.lastSourceIgnoresCancel = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::lastSourceIgnoresCancel, "lastSourceIgnoresCancel", this);
            this.payloads.lastDoubleOnSubscribe = _ClassStatement.forPayload(FlowableStageSubscriberOrErrorTest::lastDoubleOnSubscribe, "lastDoubleOnSubscribe", this);
        }
    }
}
