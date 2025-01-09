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
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class BlockingSubscriberTest extends RxJavaTest {

    @Test
    public void doubleOnSubscribe() {
        TestHelper.doubleOnSubscribe(new BlockingSubscriber<Integer>(new ArrayDeque<>()));
    }

    @Test
    public void cancel() {
        BlockingSubscriber<Integer> bq = new BlockingSubscriber<>(new ArrayDeque<>());
        assertFalse(bq.isCancelled());
        bq.cancel();
        assertTrue(bq.isCancelled());
        bq.cancel();
        assertTrue(bq.isCancelled());
    }

    @Test
    public void blockingFirstDoubleOnSubscribe() {
        TestHelper.doubleOnSubscribe(new BlockingFirstSubscriber<Integer>());
    }

    @Test
    public void blockingFirstTimeout() {
        BlockingFirstSubscriber<Integer> bf = new BlockingFirstSubscriber<>();
        Thread.currentThread().interrupt();
        try {
            bf.blockingGet();
            fail("Should have thrown!");
        } catch (RuntimeException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof InterruptedException);
        }
    }

    @Test
    public void blockingFirstTimeout2() {
        BlockingFirstSubscriber<Integer> bf = new BlockingFirstSubscriber<>();
        bf.onSubscribe(new BooleanSubscription());
        Thread.currentThread().interrupt();
        try {
            bf.blockingGet();
            fail("Should have thrown!");
        } catch (RuntimeException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof InterruptedException);
        }
    }

    @Test
    public void cancelOnRequest() {
        final BlockingFirstSubscriber<Integer> bf = new BlockingFirstSubscriber<>();
        final AtomicBoolean b = new AtomicBoolean();
        Subscription s = new Subscription() {

            @Override
            public void request(long n) {
                bf.cancelled = true;
            }

            @Override
            public void cancel() {
                b.set(true);
            }
        };
        bf.onSubscribe(s);
        assertTrue(b.get());
    }

    @Test
    public void cancelUpfront() {
        final BlockingFirstSubscriber<Integer> bf = new BlockingFirstSubscriber<>();
        final AtomicBoolean b = new AtomicBoolean();
        bf.cancelled = true;
        Subscription s = new Subscription() {

            @Override
            public void request(long n) {
                b.set(true);
            }

            @Override
            public void cancel() {
            }
        };
        bf.onSubscribe(s);
        assertFalse(b.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public BlockingSubscriberTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirstDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.blockingFirstDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirstTimeout() throws java.lang.Throwable {
            this.payloads.blockingFirstTimeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirstTimeout2() throws java.lang.Throwable {
            this.payloads.blockingFirstTimeout2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOnRequest() throws java.lang.Throwable {
            this.payloads.cancelOnRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelUpfront() throws java.lang.Throwable {
            this.payloads.cancelUpfront.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingSubscriberTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingSubscriberTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingSubscriberTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingSubscriberTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new BlockingSubscriberTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingSubscriberTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(BlockingSubscriberTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(BlockingSubscriberTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement blockingFirstDoubleOnSubscribe;

            public org.junit.runners.model.Statement blockingFirstTimeout;

            public org.junit.runners.model.Statement blockingFirstTimeout2;

            public org.junit.runners.model.Statement cancelOnRequest;

            public org.junit.runners.model.Statement cancelUpfront;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(BlockingSubscriberTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.cancel = _ClassStatement.forPayload(BlockingSubscriberTest::cancel, "cancel", this);
            this.payloads.blockingFirstDoubleOnSubscribe = _ClassStatement.forPayload(BlockingSubscriberTest::blockingFirstDoubleOnSubscribe, "blockingFirstDoubleOnSubscribe", this);
            this.payloads.blockingFirstTimeout = _ClassStatement.forPayload(BlockingSubscriberTest::blockingFirstTimeout, "blockingFirstTimeout", this);
            this.payloads.blockingFirstTimeout2 = _ClassStatement.forPayload(BlockingSubscriberTest::blockingFirstTimeout2, "blockingFirstTimeout2", this);
            this.payloads.cancelOnRequest = _ClassStatement.forPayload(BlockingSubscriberTest::cancelOnRequest, "cancelOnRequest", this);
            this.payloads.cancelUpfront = _ClassStatement.forPayload(BlockingSubscriberTest::cancelUpfront, "cancelUpfront", this);
        }
    }
}
