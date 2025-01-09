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
package io.reactivex.rxjava3.internal.operators.observable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.fuseable.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableFromCompletableTest extends RxJavaTest {

    @Test
    public void fromCompletable() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Observable.fromCompletable(Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                atomicInteger.incrementAndGet();
            }
        })).test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromCompletableTwice() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Action run = new Action() {

            @Override
            public void run() throws Exception {
                atomicInteger.incrementAndGet();
            }
        };
        Observable.fromCompletable(Completable.fromAction(run)).test().assertResult();
        assertEquals(1, atomicInteger.get());
        Observable.fromCompletable(Completable.fromAction(run)).test().assertResult();
        assertEquals(2, atomicInteger.get());
    }

    @Test
    public void fromCompletableInvokesLazy() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Observable<Object> source = Observable.fromCompletable(Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                atomicInteger.incrementAndGet();
            }
        }));
        assertEquals(0, atomicInteger.get());
        source.test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromCompletableThrows() {
        Observable.fromCompletable(Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                throw new UnsupportedOperationException();
            }
        })).test().assertFailure(UnsupportedOperationException.class);
    }

    @Test
    public void noErrorLoss() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final CountDownLatch cdl1 = new CountDownLatch(1);
            final CountDownLatch cdl2 = new CountDownLatch(1);
            TestObserver<Object> to = Observable.fromCompletable(Completable.fromAction(new Action() {

                @Override
                public void run() throws Exception {
                    cdl1.countDown();
                    cdl2.await(5, TimeUnit.SECONDS);
                }
            })).subscribeOn(Schedulers.single()).test();
            assertTrue(cdl1.await(5, TimeUnit.SECONDS));
            to.dispose();
            int timeout = 10;
            while (timeout-- > 0 && errors.isEmpty()) {
                Thread.sleep(100);
            }
            TestHelper.assertUndeliverable(errors, 0, InterruptedException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void disposedUpfront() throws Throwable {
        Action run = mock(Action.class);
        Observable.fromCompletable(Completable.fromAction(run)).test(true).assertEmpty();
        verify(run, never()).run();
    }

    @Test
    public void cancelWhileRunning() {
        final TestObserver<Object> to = new TestObserver<>();
        Observable.fromCompletable(Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                to.dispose();
            }
        })).subscribeWith(to).assertEmpty();
        assertTrue(to.isDisposed());
    }

    @Test
    public void asyncFused() throws Throwable {
        TestObserverEx<Object> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.ASYNC);
        Action action = mock(Action.class);
        Observable.fromCompletable(Completable.fromAction(action)).subscribe(to);
        to.assertFusionMode(QueueFuseable.ASYNC).assertResult();
        verify(action).run();
    }

    @Test
    public void syncFusedRejected() throws Throwable {
        TestObserverEx<Object> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.SYNC);
        Action action = mock(Action.class);
        Observable.fromCompletable(Completable.fromAction(action)).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult();
        verify(action).run();
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Observable.fromCompletable(Completable.never()));
    }

    @Test
    public void upstream() {
        Observable<?> o = Observable.fromCompletable(Completable.never());
        assertTrue(o instanceof HasUpstreamCompletableSource);
        assertSame(Completable.never(), ((HasUpstreamCompletableSource) o).source());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableFromCompletableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCompletable() throws java.lang.Throwable {
            this.payloads.fromCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCompletableTwice() throws java.lang.Throwable {
            this.payloads.fromCompletableTwice.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCompletableInvokesLazy() throws java.lang.Throwable {
            this.payloads.fromCompletableInvokesLazy.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCompletableThrows() throws java.lang.Throwable {
            this.payloads.fromCompletableThrows.evaluate();
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

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstream() throws java.lang.Throwable {
            this.payloads.upstream.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromCompletableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromCompletableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromCompletableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromCompletableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableFromCompletableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromCompletableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableFromCompletableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableFromCompletableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement fromCompletable;

            public org.junit.runners.model.Statement fromCompletableTwice;

            public org.junit.runners.model.Statement fromCompletableInvokesLazy;

            public org.junit.runners.model.Statement fromCompletableThrows;

            public org.junit.runners.model.Statement noErrorLoss;

            public org.junit.runners.model.Statement disposedUpfront;

            public org.junit.runners.model.Statement cancelWhileRunning;

            public org.junit.runners.model.Statement asyncFused;

            public org.junit.runners.model.Statement syncFusedRejected;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement upstream;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.fromCompletable = _ClassStatement.forPayload(ObservableFromCompletableTest::fromCompletable, "fromCompletable", this);
            this.payloads.fromCompletableTwice = _ClassStatement.forPayload(ObservableFromCompletableTest::fromCompletableTwice, "fromCompletableTwice", this);
            this.payloads.fromCompletableInvokesLazy = _ClassStatement.forPayload(ObservableFromCompletableTest::fromCompletableInvokesLazy, "fromCompletableInvokesLazy", this);
            this.payloads.fromCompletableThrows = _ClassStatement.forPayload(ObservableFromCompletableTest::fromCompletableThrows, "fromCompletableThrows", this);
            this.payloads.noErrorLoss = _ClassStatement.forPayload(ObservableFromCompletableTest::noErrorLoss, "noErrorLoss", this);
            this.payloads.disposedUpfront = _ClassStatement.forPayload(ObservableFromCompletableTest::disposedUpfront, "disposedUpfront", this);
            this.payloads.cancelWhileRunning = _ClassStatement.forPayload(ObservableFromCompletableTest::cancelWhileRunning, "cancelWhileRunning", this);
            this.payloads.asyncFused = _ClassStatement.forPayload(ObservableFromCompletableTest::asyncFused, "asyncFused", this);
            this.payloads.syncFusedRejected = _ClassStatement.forPayload(ObservableFromCompletableTest::syncFusedRejected, "syncFusedRejected", this);
            this.payloads.disposed = _ClassStatement.forPayload(ObservableFromCompletableTest::disposed, "disposed", this);
            this.payloads.upstream = _ClassStatement.forPayload(ObservableFromCompletableTest::upstream, "upstream", this);
        }
    }
}
