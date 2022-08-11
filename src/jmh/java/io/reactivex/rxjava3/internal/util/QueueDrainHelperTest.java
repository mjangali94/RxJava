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
package io.reactivex.rxjava3.internal.util;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.BooleanSupplier;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.SpscArrayQueue;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class QueueDrainHelperTest extends RxJavaTest {

    @Test
    public void isCancelled() {
        assertTrue(QueueDrainHelper.isCancelled(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                throw new IOException();
            }
        }));
    }

    @Test
    public void requestMaxInt() {
        QueueDrainHelper.request(new Subscription() {

            @Override
            public void request(long n) {
                assertEquals(Integer.MAX_VALUE, n);
            }

            @Override
            public void cancel() {
            }
        }, Integer.MAX_VALUE);
    }

    @Test
    public void requestMinInt() {
        QueueDrainHelper.request(new Subscription() {

            @Override
            public void request(long n) {
                assertEquals(Long.MAX_VALUE, n);
            }

            @Override
            public void cancel() {
            }
        }, Integer.MIN_VALUE);
    }

    @Test
    public void requestAlmostMaxInt() {
        QueueDrainHelper.request(new Subscription() {

            @Override
            public void request(long n) {
                assertEquals(Integer.MAX_VALUE - 1, n);
            }

            @Override
            public void cancel() {
            }
        }, Integer.MAX_VALUE - 1);
    }

    @Test
    public void postCompleteEmpty() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        AtomicLong state = new AtomicLong();
        BooleanSupplier isCancelled = new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return false;
            }
        };
        ts.onSubscribe(new BooleanSubscription());
        QueueDrainHelper.postComplete(ts, queue, state, isCancelled);
        ts.assertResult();
    }

    @Test
    public void postCompleteWithRequest() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        AtomicLong state = new AtomicLong();
        BooleanSupplier isCancelled = new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return false;
            }
        };
        ts.onSubscribe(new BooleanSubscription());
        queue.offer(1);
        state.getAndIncrement();
        QueueDrainHelper.postComplete(ts, queue, state, isCancelled);
        ts.assertResult(1);
    }

    @Test
    public void completeRequestRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = new TestSubscriber<>();
            final ArrayDeque<Integer> queue = new ArrayDeque<>();
            final AtomicLong state = new AtomicLong();
            final BooleanSupplier isCancelled = new BooleanSupplier() {

                @Override
                public boolean getAsBoolean() throws Exception {
                    return false;
                }
            };
            ts.onSubscribe(new BooleanSubscription());
            queue.offer(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    QueueDrainHelper.postCompleteRequest(1, ts, queue, state, isCancelled);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    QueueDrainHelper.postComplete(ts, queue, state, isCancelled);
                }
            };
            TestHelper.race(r1, r2);
            ts.assertResult(1);
        }
    }

    @Test
    public void postCompleteCancelled() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        AtomicLong state = new AtomicLong();
        BooleanSupplier isCancelled = new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return ts.isCancelled();
            }
        };
        ts.onSubscribe(new BooleanSubscription());
        queue.offer(1);
        state.getAndIncrement();
        ts.cancel();
        QueueDrainHelper.postComplete(ts, queue, state, isCancelled);
        ts.assertEmpty();
    }

    @Test
    public void postCompleteCancelledAfterOne() {
        final TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                cancel();
            }
        };
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        AtomicLong state = new AtomicLong();
        BooleanSupplier isCancelled = new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return ts.isCancelled();
            }
        };
        ts.onSubscribe(new BooleanSubscription());
        queue.offer(1);
        state.getAndIncrement();
        QueueDrainHelper.postComplete(ts, queue, state, isCancelled);
        ts.assertValue(1).assertNoErrors().assertNotComplete();
    }

    @Test
    public void drainMaxLoopMissingBackpressure() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        ts.onSubscribe(new BooleanSubscription());
        QueueDrain<Integer, Integer> qd = new QueueDrain<Integer, Integer>() {

            @Override
            public boolean cancelled() {
                return false;
            }

            @Override
            public boolean done() {
                return false;
            }

            @Override
            public Throwable error() {
                return null;
            }

            @Override
            public boolean enter() {
                return true;
            }

            @Override
            public long requested() {
                return 0;
            }

            @Override
            public long produced(long n) {
                return 0;
            }

            @Override
            public int leave(int m) {
                return 0;
            }

            @Override
            public boolean accept(Subscriber<? super Integer> a, Integer v) {
                return false;
            }
        };
        SpscArrayQueue<Integer> q = new SpscArrayQueue<>(32);
        q.offer(1);
        QueueDrainHelper.drainMaxLoop(q, ts, false, null, qd);
        ts.assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void drainMaxLoopMissingBackpressureWithResource() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        ts.onSubscribe(new BooleanSubscription());
        QueueDrain<Integer, Integer> qd = new QueueDrain<Integer, Integer>() {

            @Override
            public boolean cancelled() {
                return false;
            }

            @Override
            public boolean done() {
                return false;
            }

            @Override
            public Throwable error() {
                return null;
            }

            @Override
            public boolean enter() {
                return true;
            }

            @Override
            public long requested() {
                return 0;
            }

            @Override
            public long produced(long n) {
                return 0;
            }

            @Override
            public int leave(int m) {
                return 0;
            }

            @Override
            public boolean accept(Subscriber<? super Integer> a, Integer v) {
                return false;
            }
        };
        SpscArrayQueue<Integer> q = new SpscArrayQueue<>(32);
        q.offer(1);
        Disposable d = Disposable.empty();
        QueueDrainHelper.drainMaxLoop(q, ts, false, d, qd);
        ts.assertFailure(MissingBackpressureException.class);
        assertTrue(d.isDisposed());
    }

    @Test
    public void drainMaxLoopDontAccept() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        ts.onSubscribe(new BooleanSubscription());
        QueueDrain<Integer, Integer> qd = new QueueDrain<Integer, Integer>() {

            @Override
            public boolean cancelled() {
                return false;
            }

            @Override
            public boolean done() {
                return false;
            }

            @Override
            public Throwable error() {
                return null;
            }

            @Override
            public boolean enter() {
                return true;
            }

            @Override
            public long requested() {
                return 1;
            }

            @Override
            public long produced(long n) {
                return 0;
            }

            @Override
            public int leave(int m) {
                return 0;
            }

            @Override
            public boolean accept(Subscriber<? super Integer> a, Integer v) {
                return false;
            }
        };
        SpscArrayQueue<Integer> q = new SpscArrayQueue<>(32);
        q.offer(1);
        QueueDrainHelper.drainMaxLoop(q, ts, false, null, qd);
        ts.assertEmpty();
    }

    @Test
    public void checkTerminatedDelayErrorEmpty() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        ts.onSubscribe(new BooleanSubscription());
        QueueDrain<Integer, Integer> qd = new QueueDrain<Integer, Integer>() {

            @Override
            public boolean cancelled() {
                return false;
            }

            @Override
            public boolean done() {
                return false;
            }

            @Override
            public Throwable error() {
                return null;
            }

            @Override
            public boolean enter() {
                return true;
            }

            @Override
            public long requested() {
                return 0;
            }

            @Override
            public long produced(long n) {
                return 0;
            }

            @Override
            public int leave(int m) {
                return 0;
            }

            @Override
            public boolean accept(Subscriber<? super Integer> a, Integer v) {
                return false;
            }
        };
        SpscArrayQueue<Integer> q = new SpscArrayQueue<>(32);
        QueueDrainHelper.checkTerminated(true, true, ts, true, q, qd);
        ts.assertResult();
    }

    @Test
    public void checkTerminatedDelayErrorNonEmpty() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        ts.onSubscribe(new BooleanSubscription());
        QueueDrain<Integer, Integer> qd = new QueueDrain<Integer, Integer>() {

            @Override
            public boolean cancelled() {
                return false;
            }

            @Override
            public boolean done() {
                return false;
            }

            @Override
            public Throwable error() {
                return null;
            }

            @Override
            public boolean enter() {
                return true;
            }

            @Override
            public long requested() {
                return 0;
            }

            @Override
            public long produced(long n) {
                return 0;
            }

            @Override
            public int leave(int m) {
                return 0;
            }

            @Override
            public boolean accept(Subscriber<? super Integer> a, Integer v) {
                return false;
            }
        };
        SpscArrayQueue<Integer> q = new SpscArrayQueue<>(32);
        QueueDrainHelper.checkTerminated(true, false, ts, true, q, qd);
        ts.assertEmpty();
    }

    @Test
    public void checkTerminatedDelayErrorEmptyError() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        ts.onSubscribe(new BooleanSubscription());
        QueueDrain<Integer, Integer> qd = new QueueDrain<Integer, Integer>() {

            @Override
            public boolean cancelled() {
                return false;
            }

            @Override
            public boolean done() {
                return false;
            }

            @Override
            public Throwable error() {
                return new TestException();
            }

            @Override
            public boolean enter() {
                return true;
            }

            @Override
            public long requested() {
                return 0;
            }

            @Override
            public long produced(long n) {
                return 0;
            }

            @Override
            public int leave(int m) {
                return 0;
            }

            @Override
            public boolean accept(Subscriber<? super Integer> a, Integer v) {
                return false;
            }
        };
        SpscArrayQueue<Integer> q = new SpscArrayQueue<>(32);
        QueueDrainHelper.checkTerminated(true, true, ts, true, q, qd);
        ts.assertFailure(TestException.class);
    }

    @Test
    public void checkTerminatedNonDelayErrorError() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        ts.onSubscribe(new BooleanSubscription());
        QueueDrain<Integer, Integer> qd = new QueueDrain<Integer, Integer>() {

            @Override
            public boolean cancelled() {
                return false;
            }

            @Override
            public boolean done() {
                return false;
            }

            @Override
            public Throwable error() {
                return new TestException();
            }

            @Override
            public boolean enter() {
                return true;
            }

            @Override
            public long requested() {
                return 0;
            }

            @Override
            public long produced(long n) {
                return 0;
            }

            @Override
            public int leave(int m) {
                return 0;
            }

            @Override
            public boolean accept(Subscriber<? super Integer> a, Integer v) {
                return false;
            }
        };
        SpscArrayQueue<Integer> q = new SpscArrayQueue<>(32);
        QueueDrainHelper.checkTerminated(true, false, ts, false, q, qd);
        ts.assertFailure(TestException.class);
    }

    @Test
    public void observerCheckTerminatedDelayErrorEmpty() {
        TestObserver<Integer> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        ObservableQueueDrain<Integer, Integer> qd = new ObservableQueueDrain<Integer, Integer>() {

            @Override
            public boolean cancelled() {
                return false;
            }

            @Override
            public boolean done() {
                return false;
            }

            @Override
            public Throwable error() {
                return null;
            }

            @Override
            public boolean enter() {
                return true;
            }

            @Override
            public int leave(int m) {
                return 0;
            }

            @Override
            public void accept(Observer<? super Integer> a, Integer v) {
            }
        };
        SpscArrayQueue<Integer> q = new SpscArrayQueue<>(32);
        QueueDrainHelper.checkTerminated(true, true, to, true, q, null, qd);
        to.assertResult();
    }

    @Test
    public void observerCheckTerminatedDelayErrorEmptyResource() {
        TestObserver<Integer> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        ObservableQueueDrain<Integer, Integer> qd = new ObservableQueueDrain<Integer, Integer>() {

            @Override
            public boolean cancelled() {
                return false;
            }

            @Override
            public boolean done() {
                return false;
            }

            @Override
            public Throwable error() {
                return null;
            }

            @Override
            public boolean enter() {
                return true;
            }

            @Override
            public int leave(int m) {
                return 0;
            }

            @Override
            public void accept(Observer<? super Integer> a, Integer v) {
            }
        };
        SpscArrayQueue<Integer> q = new SpscArrayQueue<>(32);
        Disposable d = Disposable.empty();
        QueueDrainHelper.checkTerminated(true, true, to, true, q, d, qd);
        to.assertResult();
        assertTrue(d.isDisposed());
    }

    @Test
    public void observerCheckTerminatedDelayErrorNonEmpty() {
        TestObserver<Integer> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        ObservableQueueDrain<Integer, Integer> qd = new ObservableQueueDrain<Integer, Integer>() {

            @Override
            public boolean cancelled() {
                return false;
            }

            @Override
            public boolean done() {
                return false;
            }

            @Override
            public Throwable error() {
                return null;
            }

            @Override
            public boolean enter() {
                return true;
            }

            @Override
            public int leave(int m) {
                return 0;
            }

            @Override
            public void accept(Observer<? super Integer> a, Integer v) {
            }
        };
        SpscArrayQueue<Integer> q = new SpscArrayQueue<>(32);
        QueueDrainHelper.checkTerminated(true, false, to, true, q, null, qd);
        to.assertEmpty();
    }

    @Test
    public void observerCheckTerminatedDelayErrorEmptyError() {
        TestObserver<Integer> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        ObservableQueueDrain<Integer, Integer> qd = new ObservableQueueDrain<Integer, Integer>() {

            @Override
            public boolean cancelled() {
                return false;
            }

            @Override
            public boolean done() {
                return false;
            }

            @Override
            public Throwable error() {
                return new TestException();
            }

            @Override
            public boolean enter() {
                return true;
            }

            @Override
            public int leave(int m) {
                return 0;
            }

            @Override
            public void accept(Observer<? super Integer> a, Integer v) {
            }
        };
        SpscArrayQueue<Integer> q = new SpscArrayQueue<>(32);
        QueueDrainHelper.checkTerminated(true, true, to, true, q, null, qd);
        to.assertFailure(TestException.class);
    }

    @Test
    public void observerCheckTerminatedNonDelayErrorError() {
        TestObserver<Integer> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        ObservableQueueDrain<Integer, Integer> qd = new ObservableQueueDrain<Integer, Integer>() {

            @Override
            public boolean cancelled() {
                return false;
            }

            @Override
            public boolean done() {
                return false;
            }

            @Override
            public Throwable error() {
                return new TestException();
            }

            @Override
            public boolean enter() {
                return true;
            }

            @Override
            public int leave(int m) {
                return 0;
            }

            @Override
            public void accept(Observer<? super Integer> a, Integer v) {
            }
        };
        SpscArrayQueue<Integer> q = new SpscArrayQueue<>(32);
        QueueDrainHelper.checkTerminated(true, false, to, false, q, null, qd);
        to.assertFailure(TestException.class);
    }

    @Test
    public void observerCheckTerminatedNonDelayErrorErrorResource() {
        TestObserver<Integer> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        ObservableQueueDrain<Integer, Integer> qd = new ObservableQueueDrain<Integer, Integer>() {

            @Override
            public boolean cancelled() {
                return false;
            }

            @Override
            public boolean done() {
                return false;
            }

            @Override
            public Throwable error() {
                return new TestException();
            }

            @Override
            public boolean enter() {
                return true;
            }

            @Override
            public int leave(int m) {
                return 0;
            }

            @Override
            public void accept(Observer<? super Integer> a, Integer v) {
            }
        };
        SpscArrayQueue<Integer> q = new SpscArrayQueue<>(32);
        Disposable d = Disposable.empty();
        QueueDrainHelper.checkTerminated(true, false, to, false, q, d, qd);
        to.assertFailure(TestException.class);
        assertTrue(d.isDisposed());
    }

    @Test
    public void postCompleteAlreadyComplete() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Queue<Integer> q = new ArrayDeque<>();
        q.offer(1);
        AtomicLong state = new AtomicLong(QueueDrainHelper.COMPLETED_MASK);
        QueueDrainHelper.postComplete(ts, q, state, new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return false;
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private QueueDrainHelperTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isCancelled() throws java.lang.Throwable {
            this.payloads.isCancelled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestMaxInt() throws java.lang.Throwable {
            this.payloads.requestMaxInt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestMinInt() throws java.lang.Throwable {
            this.payloads.requestMinInt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestAlmostMaxInt() throws java.lang.Throwable {
            this.payloads.requestAlmostMaxInt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_postCompleteEmpty() throws java.lang.Throwable {
            this.payloads.postCompleteEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_postCompleteWithRequest() throws java.lang.Throwable {
            this.payloads.postCompleteWithRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeRequestRace() throws java.lang.Throwable {
            this.payloads.completeRequestRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_postCompleteCancelled() throws java.lang.Throwable {
            this.payloads.postCompleteCancelled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_postCompleteCancelledAfterOne() throws java.lang.Throwable {
            this.payloads.postCompleteCancelledAfterOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_drainMaxLoopMissingBackpressure() throws java.lang.Throwable {
            this.payloads.drainMaxLoopMissingBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_drainMaxLoopMissingBackpressureWithResource() throws java.lang.Throwable {
            this.payloads.drainMaxLoopMissingBackpressureWithResource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_drainMaxLoopDontAccept() throws java.lang.Throwable {
            this.payloads.drainMaxLoopDontAccept.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkTerminatedDelayErrorEmpty() throws java.lang.Throwable {
            this.payloads.checkTerminatedDelayErrorEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkTerminatedDelayErrorNonEmpty() throws java.lang.Throwable {
            this.payloads.checkTerminatedDelayErrorNonEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkTerminatedDelayErrorEmptyError() throws java.lang.Throwable {
            this.payloads.checkTerminatedDelayErrorEmptyError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkTerminatedNonDelayErrorError() throws java.lang.Throwable {
            this.payloads.checkTerminatedNonDelayErrorError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerCheckTerminatedDelayErrorEmpty() throws java.lang.Throwable {
            this.payloads.observerCheckTerminatedDelayErrorEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerCheckTerminatedDelayErrorEmptyResource() throws java.lang.Throwable {
            this.payloads.observerCheckTerminatedDelayErrorEmptyResource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerCheckTerminatedDelayErrorNonEmpty() throws java.lang.Throwable {
            this.payloads.observerCheckTerminatedDelayErrorNonEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerCheckTerminatedDelayErrorEmptyError() throws java.lang.Throwable {
            this.payloads.observerCheckTerminatedDelayErrorEmptyError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerCheckTerminatedNonDelayErrorError() throws java.lang.Throwable {
            this.payloads.observerCheckTerminatedNonDelayErrorError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerCheckTerminatedNonDelayErrorErrorResource() throws java.lang.Throwable {
            this.payloads.observerCheckTerminatedNonDelayErrorErrorResource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_postCompleteAlreadyComplete() throws java.lang.Throwable {
            this.payloads.postCompleteAlreadyComplete.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainHelperTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainHelperTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainHelperTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainHelperTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new QueueDrainHelperTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainHelperTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(QueueDrainHelperTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(QueueDrainHelperTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement isCancelled;

            public org.junit.runners.model.Statement requestMaxInt;

            public org.junit.runners.model.Statement requestMinInt;

            public org.junit.runners.model.Statement requestAlmostMaxInt;

            public org.junit.runners.model.Statement postCompleteEmpty;

            public org.junit.runners.model.Statement postCompleteWithRequest;

            public org.junit.runners.model.Statement completeRequestRace;

            public org.junit.runners.model.Statement postCompleteCancelled;

            public org.junit.runners.model.Statement postCompleteCancelledAfterOne;

            public org.junit.runners.model.Statement drainMaxLoopMissingBackpressure;

            public org.junit.runners.model.Statement drainMaxLoopMissingBackpressureWithResource;

            public org.junit.runners.model.Statement drainMaxLoopDontAccept;

            public org.junit.runners.model.Statement checkTerminatedDelayErrorEmpty;

            public org.junit.runners.model.Statement checkTerminatedDelayErrorNonEmpty;

            public org.junit.runners.model.Statement checkTerminatedDelayErrorEmptyError;

            public org.junit.runners.model.Statement checkTerminatedNonDelayErrorError;

            public org.junit.runners.model.Statement observerCheckTerminatedDelayErrorEmpty;

            public org.junit.runners.model.Statement observerCheckTerminatedDelayErrorEmptyResource;

            public org.junit.runners.model.Statement observerCheckTerminatedDelayErrorNonEmpty;

            public org.junit.runners.model.Statement observerCheckTerminatedDelayErrorEmptyError;

            public org.junit.runners.model.Statement observerCheckTerminatedNonDelayErrorError;

            public org.junit.runners.model.Statement observerCheckTerminatedNonDelayErrorErrorResource;

            public org.junit.runners.model.Statement postCompleteAlreadyComplete;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.isCancelled = _ClassStatement.forPayload(QueueDrainHelperTest::isCancelled, "isCancelled", this);
            this.payloads.requestMaxInt = _ClassStatement.forPayload(QueueDrainHelperTest::requestMaxInt, "requestMaxInt", this);
            this.payloads.requestMinInt = _ClassStatement.forPayload(QueueDrainHelperTest::requestMinInt, "requestMinInt", this);
            this.payloads.requestAlmostMaxInt = _ClassStatement.forPayload(QueueDrainHelperTest::requestAlmostMaxInt, "requestAlmostMaxInt", this);
            this.payloads.postCompleteEmpty = _ClassStatement.forPayload(QueueDrainHelperTest::postCompleteEmpty, "postCompleteEmpty", this);
            this.payloads.postCompleteWithRequest = _ClassStatement.forPayload(QueueDrainHelperTest::postCompleteWithRequest, "postCompleteWithRequest", this);
            this.payloads.completeRequestRace = _ClassStatement.forPayload(QueueDrainHelperTest::completeRequestRace, "completeRequestRace", this);
            this.payloads.postCompleteCancelled = _ClassStatement.forPayload(QueueDrainHelperTest::postCompleteCancelled, "postCompleteCancelled", this);
            this.payloads.postCompleteCancelledAfterOne = _ClassStatement.forPayload(QueueDrainHelperTest::postCompleteCancelledAfterOne, "postCompleteCancelledAfterOne", this);
            this.payloads.drainMaxLoopMissingBackpressure = _ClassStatement.forPayload(QueueDrainHelperTest::drainMaxLoopMissingBackpressure, "drainMaxLoopMissingBackpressure", this);
            this.payloads.drainMaxLoopMissingBackpressureWithResource = _ClassStatement.forPayload(QueueDrainHelperTest::drainMaxLoopMissingBackpressureWithResource, "drainMaxLoopMissingBackpressureWithResource", this);
            this.payloads.drainMaxLoopDontAccept = _ClassStatement.forPayload(QueueDrainHelperTest::drainMaxLoopDontAccept, "drainMaxLoopDontAccept", this);
            this.payloads.checkTerminatedDelayErrorEmpty = _ClassStatement.forPayload(QueueDrainHelperTest::checkTerminatedDelayErrorEmpty, "checkTerminatedDelayErrorEmpty", this);
            this.payloads.checkTerminatedDelayErrorNonEmpty = _ClassStatement.forPayload(QueueDrainHelperTest::checkTerminatedDelayErrorNonEmpty, "checkTerminatedDelayErrorNonEmpty", this);
            this.payloads.checkTerminatedDelayErrorEmptyError = _ClassStatement.forPayload(QueueDrainHelperTest::checkTerminatedDelayErrorEmptyError, "checkTerminatedDelayErrorEmptyError", this);
            this.payloads.checkTerminatedNonDelayErrorError = _ClassStatement.forPayload(QueueDrainHelperTest::checkTerminatedNonDelayErrorError, "checkTerminatedNonDelayErrorError", this);
            this.payloads.observerCheckTerminatedDelayErrorEmpty = _ClassStatement.forPayload(QueueDrainHelperTest::observerCheckTerminatedDelayErrorEmpty, "observerCheckTerminatedDelayErrorEmpty", this);
            this.payloads.observerCheckTerminatedDelayErrorEmptyResource = _ClassStatement.forPayload(QueueDrainHelperTest::observerCheckTerminatedDelayErrorEmptyResource, "observerCheckTerminatedDelayErrorEmptyResource", this);
            this.payloads.observerCheckTerminatedDelayErrorNonEmpty = _ClassStatement.forPayload(QueueDrainHelperTest::observerCheckTerminatedDelayErrorNonEmpty, "observerCheckTerminatedDelayErrorNonEmpty", this);
            this.payloads.observerCheckTerminatedDelayErrorEmptyError = _ClassStatement.forPayload(QueueDrainHelperTest::observerCheckTerminatedDelayErrorEmptyError, "observerCheckTerminatedDelayErrorEmptyError", this);
            this.payloads.observerCheckTerminatedNonDelayErrorError = _ClassStatement.forPayload(QueueDrainHelperTest::observerCheckTerminatedNonDelayErrorError, "observerCheckTerminatedNonDelayErrorError", this);
            this.payloads.observerCheckTerminatedNonDelayErrorErrorResource = _ClassStatement.forPayload(QueueDrainHelperTest::observerCheckTerminatedNonDelayErrorErrorResource, "observerCheckTerminatedNonDelayErrorErrorResource", this);
            this.payloads.postCompleteAlreadyComplete = _ClassStatement.forPayload(QueueDrainHelperTest::postCompleteAlreadyComplete, "postCompleteAlreadyComplete", this);
        }
    }
}
