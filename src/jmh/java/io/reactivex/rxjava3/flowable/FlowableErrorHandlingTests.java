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
package io.reactivex.rxjava3.flowable;

import static org.junit.Assert.assertNotNull;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.DefaultSubscriber;

public class FlowableErrorHandlingTests extends RxJavaTest {

    /**
     * Test that an error from a user provided Observer.onNext
     * is handled and emitted to the onError.
     * @throws InterruptedException if the test is interrupted
     */
    @Test
    public void onNextError() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> caughtError = new AtomicReference<>();
        Flowable<Long> f = Flowable.interval(50, TimeUnit.MILLISECONDS);
        Subscriber<Long> subscriber = new DefaultSubscriber<Long>() {

            @Override
            public void onComplete() {
                System.out.println("completed");
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("error: " + e);
                caughtError.set(e);
                latch.countDown();
            }

            @Override
            public void onNext(Long args) {
                throw new RuntimeException("forced failure");
            }
        };
        f.safeSubscribe(subscriber);
        latch.await(2000, TimeUnit.MILLISECONDS);
        assertNotNull(caughtError.get());
    }

    /**
     * Test that an error from a user provided Observer.onNext
     * is handled and emitted to the onError.
     * even when done across thread boundaries with observeOn
     * @throws InterruptedException if the test is interrupted
     */
    @Test
    public void onNextErrorAcrossThread() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> caughtError = new AtomicReference<>();
        Flowable<Long> f = Flowable.interval(50, TimeUnit.MILLISECONDS);
        Subscriber<Long> subscriber = new DefaultSubscriber<Long>() {

            @Override
            public void onComplete() {
                System.out.println("completed");
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("error: " + e);
                caughtError.set(e);
                latch.countDown();
            }

            @Override
            public void onNext(Long args) {
                throw new RuntimeException("forced failure");
            }
        };
        f.observeOn(Schedulers.newThread()).safeSubscribe(subscriber);
        latch.await(2000, TimeUnit.MILLISECONDS);
        assertNotNull(caughtError.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableErrorHandlingTests instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextError() throws java.lang.Throwable {
            this.payloads.onNextError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextErrorAcrossThread() throws java.lang.Throwable {
            this.payloads.onNextErrorAcrossThread.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableErrorHandlingTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableErrorHandlingTests> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableErrorHandlingTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableErrorHandlingTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableErrorHandlingTests();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableErrorHandlingTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableErrorHandlingTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableErrorHandlingTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement onNextError;

            public org.junit.runners.model.Statement onNextErrorAcrossThread;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.onNextError = _ClassStatement.forPayload(FlowableErrorHandlingTests::onNextError, "onNextError", this);
            this.payloads.onNextErrorAcrossThread = _ClassStatement.forPayload(FlowableErrorHandlingTests::onNextErrorAcrossThread, "onNextErrorAcrossThread", this);
        }
    }
}
