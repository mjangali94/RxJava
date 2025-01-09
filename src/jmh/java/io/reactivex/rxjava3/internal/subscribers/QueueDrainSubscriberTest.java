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
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.operators.SpscArrayQueue;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class QueueDrainSubscriberTest extends RxJavaTest {

    static final QueueDrainSubscriber<Integer, Integer, Integer> createUnordered(TestSubscriber<Integer> ts, final Disposable d) {
        return new QueueDrainSubscriber<Integer, Integer, Integer>(ts, new SpscArrayQueue<>(4)) {

            @Override
            public void onNext(Integer t) {
                fastPathEmitMax(t, false, d);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onSubscribe(Subscription s) {
            }

            @Override
            public boolean accept(Subscriber<? super Integer> a, Integer v) {
                super.accept(a, v);
                a.onNext(v);
                return true;
            }
        };
    }

    static final QueueDrainSubscriber<Integer, Integer, Integer> createOrdered(TestSubscriber<Integer> ts, final Disposable d) {
        return new QueueDrainSubscriber<Integer, Integer, Integer>(ts, new SpscArrayQueue<>(4)) {

            @Override
            public void onNext(Integer t) {
                fastPathOrderedEmitMax(t, false, d);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onSubscribe(Subscription s) {
            }

            @Override
            public boolean accept(Subscriber<? super Integer> a, Integer v) {
                super.accept(a, v);
                a.onNext(v);
                return true;
            }
        };
    }

    static final QueueDrainSubscriber<Integer, Integer, Integer> createUnorderedReject(TestSubscriber<Integer> ts, final Disposable d) {
        return new QueueDrainSubscriber<Integer, Integer, Integer>(ts, new SpscArrayQueue<>(4)) {

            @Override
            public void onNext(Integer t) {
                fastPathEmitMax(t, false, d);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onSubscribe(Subscription s) {
            }

            @Override
            public boolean accept(Subscriber<? super Integer> a, Integer v) {
                super.accept(a, v);
                a.onNext(v);
                return false;
            }
        };
    }

    static final QueueDrainSubscriber<Integer, Integer, Integer> createOrderedReject(TestSubscriber<Integer> ts, final Disposable d) {
        return new QueueDrainSubscriber<Integer, Integer, Integer>(ts, new SpscArrayQueue<>(4)) {

            @Override
            public void onNext(Integer t) {
                fastPathOrderedEmitMax(t, false, d);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onSubscribe(Subscription s) {
            }

            @Override
            public boolean accept(Subscriber<? super Integer> a, Integer v) {
                super.accept(a, v);
                a.onNext(v);
                return false;
            }
        };
    }

    @Test
    public void unorderedFastPathNoRequest() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0);
        Disposable d = Disposable.empty();
        QueueDrainSubscriber<Integer, Integer, Integer> qd = createUnordered(ts, d);
        ts.onSubscribe(new BooleanSubscription());
        qd.onNext(1);
        ts.assertFailure(MissingBackpressureException.class);
        assertTrue(d.isDisposed());
    }

    @Test
    public void orderedFastPathNoRequest() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0);
        Disposable d = Disposable.empty();
        QueueDrainSubscriber<Integer, Integer, Integer> qd = createOrdered(ts, d);
        ts.onSubscribe(new BooleanSubscription());
        qd.onNext(1);
        ts.assertFailure(MissingBackpressureException.class);
        assertTrue(d.isDisposed());
    }

    @Test
    public void acceptBadRequest() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0);
        Disposable d = Disposable.empty();
        QueueDrainSubscriber<Integer, Integer, Integer> qd = createUnordered(ts, d);
        ts.onSubscribe(new BooleanSubscription());
        assertTrue(qd.accept(ts, 0));
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            qd.requested(-1);
            TestHelper.assertError(errors, 0, IllegalArgumentException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void unorderedFastPathRequest1() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(1);
        Disposable d = Disposable.empty();
        QueueDrainSubscriber<Integer, Integer, Integer> qd = createUnordered(ts, d);
        ts.onSubscribe(new BooleanSubscription());
        qd.requested(1);
        qd.onNext(1);
        ts.assertValuesOnly(1);
    }

    @Test
    public void orderedFastPathRequest1() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(1);
        Disposable d = Disposable.empty();
        QueueDrainSubscriber<Integer, Integer, Integer> qd = createOrdered(ts, d);
        ts.onSubscribe(new BooleanSubscription());
        qd.requested(1);
        qd.onNext(1);
        ts.assertValuesOnly(1);
    }

    @Test
    public void unorderedSlowPath() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(1);
        Disposable d = Disposable.empty();
        QueueDrainSubscriber<Integer, Integer, Integer> qd = createUnordered(ts, d);
        ts.onSubscribe(new BooleanSubscription());
        qd.enter();
        qd.onNext(1);
        ts.assertEmpty();
    }

    @Test
    public void orderedSlowPath() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(1);
        Disposable d = Disposable.empty();
        QueueDrainSubscriber<Integer, Integer, Integer> qd = createOrdered(ts, d);
        ts.onSubscribe(new BooleanSubscription());
        qd.enter();
        qd.onNext(1);
        ts.assertEmpty();
    }

    @Test
    public void orderedSlowPathNonEmptyQueue() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(1);
        Disposable d = Disposable.empty();
        QueueDrainSubscriber<Integer, Integer, Integer> qd = createOrdered(ts, d);
        ts.onSubscribe(new BooleanSubscription());
        qd.queue.offer(0);
        qd.requested(2);
        qd.onNext(1);
        ts.assertValuesOnly(0, 1);
    }

    @Test
    public void unorderedOnNextRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            TestSubscriber<Integer> ts = new TestSubscriber<>(1);
            Disposable d = Disposable.empty();
            final QueueDrainSubscriber<Integer, Integer, Integer> qd = createUnordered(ts, d);
            ts.onSubscribe(new BooleanSubscription());
            qd.requested(Long.MAX_VALUE);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    qd.onNext(1);
                }
            };
            TestHelper.race(r1, r1);
            ts.assertValuesOnly(1, 1);
        }
    }

    @Test
    public void orderedOnNextRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            TestSubscriber<Integer> ts = new TestSubscriber<>(1);
            Disposable d = Disposable.empty();
            final QueueDrainSubscriber<Integer, Integer, Integer> qd = createOrdered(ts, d);
            ts.onSubscribe(new BooleanSubscription());
            qd.requested(Long.MAX_VALUE);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    qd.onNext(1);
                }
            };
            TestHelper.race(r1, r1);
            ts.assertValuesOnly(1, 1);
        }
    }

    @Test
    public void unorderedFastPathReject() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(1);
        Disposable d = Disposable.empty();
        QueueDrainSubscriber<Integer, Integer, Integer> qd = createUnorderedReject(ts, d);
        ts.onSubscribe(new BooleanSubscription());
        qd.requested(1);
        qd.onNext(1);
        ts.assertValuesOnly(1);
        assertEquals(1, qd.requested());
    }

    @Test
    public void orderedFastPathReject() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(1);
        Disposable d = Disposable.empty();
        QueueDrainSubscriber<Integer, Integer, Integer> qd = createOrderedReject(ts, d);
        ts.onSubscribe(new BooleanSubscription());
        qd.requested(1);
        qd.onNext(1);
        ts.assertValuesOnly(1);
        assertEquals(1, qd.requested());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public QueueDrainSubscriberTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unorderedFastPathNoRequest() throws java.lang.Throwable {
            this.payloads.unorderedFastPathNoRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_orderedFastPathNoRequest() throws java.lang.Throwable {
            this.payloads.orderedFastPathNoRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_acceptBadRequest() throws java.lang.Throwable {
            this.payloads.acceptBadRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unorderedFastPathRequest1() throws java.lang.Throwable {
            this.payloads.unorderedFastPathRequest1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_orderedFastPathRequest1() throws java.lang.Throwable {
            this.payloads.orderedFastPathRequest1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unorderedSlowPath() throws java.lang.Throwable {
            this.payloads.unorderedSlowPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_orderedSlowPath() throws java.lang.Throwable {
            this.payloads.orderedSlowPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_orderedSlowPathNonEmptyQueue() throws java.lang.Throwable {
            this.payloads.orderedSlowPathNonEmptyQueue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unorderedOnNextRace() throws java.lang.Throwable {
            this.payloads.unorderedOnNextRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_orderedOnNextRace() throws java.lang.Throwable {
            this.payloads.orderedOnNextRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unorderedFastPathReject() throws java.lang.Throwable {
            this.payloads.unorderedFastPathReject.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_orderedFastPathReject() throws java.lang.Throwable {
            this.payloads.orderedFastPathReject.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainSubscriberTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainSubscriberTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainSubscriberTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainSubscriberTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new QueueDrainSubscriberTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainSubscriberTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(QueueDrainSubscriberTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(QueueDrainSubscriberTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement unorderedFastPathNoRequest;

            public org.junit.runners.model.Statement orderedFastPathNoRequest;

            public org.junit.runners.model.Statement acceptBadRequest;

            public org.junit.runners.model.Statement unorderedFastPathRequest1;

            public org.junit.runners.model.Statement orderedFastPathRequest1;

            public org.junit.runners.model.Statement unorderedSlowPath;

            public org.junit.runners.model.Statement orderedSlowPath;

            public org.junit.runners.model.Statement orderedSlowPathNonEmptyQueue;

            public org.junit.runners.model.Statement unorderedOnNextRace;

            public org.junit.runners.model.Statement orderedOnNextRace;

            public org.junit.runners.model.Statement unorderedFastPathReject;

            public org.junit.runners.model.Statement orderedFastPathReject;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.unorderedFastPathNoRequest = _ClassStatement.forPayload(QueueDrainSubscriberTest::unorderedFastPathNoRequest, "unorderedFastPathNoRequest", this);
            this.payloads.orderedFastPathNoRequest = _ClassStatement.forPayload(QueueDrainSubscriberTest::orderedFastPathNoRequest, "orderedFastPathNoRequest", this);
            this.payloads.acceptBadRequest = _ClassStatement.forPayload(QueueDrainSubscriberTest::acceptBadRequest, "acceptBadRequest", this);
            this.payloads.unorderedFastPathRequest1 = _ClassStatement.forPayload(QueueDrainSubscriberTest::unorderedFastPathRequest1, "unorderedFastPathRequest1", this);
            this.payloads.orderedFastPathRequest1 = _ClassStatement.forPayload(QueueDrainSubscriberTest::orderedFastPathRequest1, "orderedFastPathRequest1", this);
            this.payloads.unorderedSlowPath = _ClassStatement.forPayload(QueueDrainSubscriberTest::unorderedSlowPath, "unorderedSlowPath", this);
            this.payloads.orderedSlowPath = _ClassStatement.forPayload(QueueDrainSubscriberTest::orderedSlowPath, "orderedSlowPath", this);
            this.payloads.orderedSlowPathNonEmptyQueue = _ClassStatement.forPayload(QueueDrainSubscriberTest::orderedSlowPathNonEmptyQueue, "orderedSlowPathNonEmptyQueue", this);
            this.payloads.unorderedOnNextRace = _ClassStatement.forPayload(QueueDrainSubscriberTest::unorderedOnNextRace, "unorderedOnNextRace", this);
            this.payloads.orderedOnNextRace = _ClassStatement.forPayload(QueueDrainSubscriberTest::orderedOnNextRace, "orderedOnNextRace", this);
            this.payloads.unorderedFastPathReject = _ClassStatement.forPayload(QueueDrainSubscriberTest::unorderedFastPathReject, "unorderedFastPathReject", this);
            this.payloads.orderedFastPathReject = _ClassStatement.forPayload(QueueDrainSubscriberTest::orderedFastPathReject, "orderedFastPathReject", this);
        }
    }
}
