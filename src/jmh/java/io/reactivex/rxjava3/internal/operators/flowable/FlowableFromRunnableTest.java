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
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableFromRunnableTest extends RxJavaTest {

    @Test
    public void fromRunnable() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Flowable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                atomicInteger.incrementAndGet();
            }
        }).test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromRunnableTwice() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Runnable run = new Runnable() {

            @Override
            public void run() {
                atomicInteger.incrementAndGet();
            }
        };
        Flowable.fromRunnable(run).test().assertResult();
        assertEquals(1, atomicInteger.get());
        Flowable.fromRunnable(run).test().assertResult();
        assertEquals(2, atomicInteger.get());
    }

    @Test
    public void fromRunnableInvokesLazy() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Flowable<Object> source = Flowable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                atomicInteger.incrementAndGet();
            }
        });
        assertEquals(0, atomicInteger.get());
        source.test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromRunnableThrows() {
        Flowable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                throw new UnsupportedOperationException();
            }
        }).test().assertFailure(UnsupportedOperationException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void callable() throws Throwable {
        final int[] counter = { 0 };
        Flowable<Void> m = Flowable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                counter[0]++;
            }
        });
        assertTrue(m.getClass().toString(), m instanceof Supplier);
        assertNull(((Supplier<Void>) m).get());
        assertEquals(1, counter[0]);
    }

    @Test
    public void noErrorLoss() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final CountDownLatch cdl1 = new CountDownLatch(1);
            final CountDownLatch cdl2 = new CountDownLatch(1);
            TestSubscriber<Object> ts = Flowable.fromRunnable(new Runnable() {

                @Override
                public void run() {
                    cdl1.countDown();
                    try {
                        cdl2.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        throw new TestException(e);
                    }
                }
            }).subscribeOn(Schedulers.single()).test();
            assertTrue(cdl1.await(5, TimeUnit.SECONDS));
            ts.cancel();
            int timeout = 10;
            while (timeout-- > 0 && errors.isEmpty()) {
                Thread.sleep(100);
            }
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void disposedUpfront() throws Throwable {
        Runnable run = mock(Runnable.class);
        Flowable.fromRunnable(run).test(1L, true).assertEmpty();
        verify(run, never()).run();
    }

    @Test
    public void cancelWhileRunning() {
        final TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                ts.cancel();
            }
        }).subscribeWith(ts).assertEmpty();
        assertTrue(ts.isCancelled());
    }

    @Test
    public void asyncFused() throws Throwable {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ASYNC);
        Runnable action = mock(Runnable.class);
        Flowable.fromRunnable(action).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.ASYNC).assertResult();
        verify(action).run();
    }

    @Test
    public void syncFusedRejected() throws Throwable {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.SYNC);
        Runnable action = mock(Runnable.class);
        Flowable.fromRunnable(action).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertResult();
        verify(action).run();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableFromRunnableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnable() throws java.lang.Throwable {
            this.payloads.fromRunnable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableTwice() throws java.lang.Throwable {
            this.payloads.fromRunnableTwice.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableInvokesLazy() throws java.lang.Throwable {
            this.payloads.fromRunnableInvokesLazy.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableThrows() throws java.lang.Throwable {
            this.payloads.fromRunnableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_callable() throws java.lang.Throwable {
            this.payloads.callable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noErrorLoss() throws java.lang.Throwable {
            this.payloads.noErrorLoss.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedUpfront() throws java.lang.Throwable {
            this.payloads.disposedUpfront.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWhileRunning() throws java.lang.Throwable {
            this.payloads.cancelWhileRunning.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFused() throws java.lang.Throwable {
            this.payloads.asyncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedRejected() throws java.lang.Throwable {
            this.payloads.syncFusedRejected.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromRunnableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromRunnableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromRunnableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromRunnableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableFromRunnableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromRunnableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableFromRunnableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableFromRunnableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement fromRunnable;

            public org.junit.runners.model.Statement fromRunnableTwice;

            public org.junit.runners.model.Statement fromRunnableInvokesLazy;

            public org.junit.runners.model.Statement fromRunnableThrows;

            public org.junit.runners.model.Statement callable;

            public org.junit.runners.model.Statement noErrorLoss;

            public org.junit.runners.model.Statement disposedUpfront;

            public org.junit.runners.model.Statement cancelWhileRunning;

            public org.junit.runners.model.Statement asyncFused;

            public org.junit.runners.model.Statement syncFusedRejected;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.fromRunnable = _ClassStatement.forPayload(FlowableFromRunnableTest::fromRunnable, "fromRunnable", this);
            this.payloads.fromRunnableTwice = _ClassStatement.forPayload(FlowableFromRunnableTest::fromRunnableTwice, "fromRunnableTwice", this);
            this.payloads.fromRunnableInvokesLazy = _ClassStatement.forPayload(FlowableFromRunnableTest::fromRunnableInvokesLazy, "fromRunnableInvokesLazy", this);
            this.payloads.fromRunnableThrows = _ClassStatement.forPayload(FlowableFromRunnableTest::fromRunnableThrows, "fromRunnableThrows", this);
            this.payloads.callable = _ClassStatement.forPayload(FlowableFromRunnableTest::callable, "callable", this);
            this.payloads.noErrorLoss = _ClassStatement.forPayload(FlowableFromRunnableTest::noErrorLoss, "noErrorLoss", this);
            this.payloads.disposedUpfront = _ClassStatement.forPayload(FlowableFromRunnableTest::disposedUpfront, "disposedUpfront", this);
            this.payloads.cancelWhileRunning = _ClassStatement.forPayload(FlowableFromRunnableTest::cancelWhileRunning, "cancelWhileRunning", this);
            this.payloads.asyncFused = _ClassStatement.forPayload(FlowableFromRunnableTest::asyncFused, "asyncFused", this);
            this.payloads.syncFusedRejected = _ClassStatement.forPayload(FlowableFromRunnableTest::syncFusedRejected, "syncFusedRejected", this);
        }
    }
}
