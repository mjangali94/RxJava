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

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.*;
import org.mockito.*;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.flowables.ConnectableFlowable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableTimerTest extends RxJavaTest {

    @Mock
    Subscriber<Object> subscriber;

    @Mock
    Subscriber<Long> subscriber2;

    TestScheduler scheduler;

    @Before
    public void before() {
        subscriber = TestHelper.mockSubscriber();
        subscriber2 = TestHelper.mockSubscriber();
        scheduler = new TestScheduler();
    }

    @Test
    public void timerOnce() {
        Flowable.timer(100, TimeUnit.MILLISECONDS, scheduler).subscribe(subscriber);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        verify(subscriber, times(1)).onNext(0L);
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void timerPeriodically() {
        TestSubscriber<Long> ts = new TestSubscriber<>();
        Flowable.interval(100, 100, TimeUnit.MILLISECONDS, scheduler).subscribe(ts);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        ts.assertValue(0L);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        ts.assertValues(0L, 1L);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        ts.assertValues(0L, 1L, 2L);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        ts.assertValues(0L, 1L, 2L, 3L);
        ts.cancel();
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        ts.assertValues(0L, 1L, 2L, 3L);
        ts.assertNotComplete();
        ts.assertNoErrors();
    }

    @Test
    public void interval() {
        Flowable<Long> w = Flowable.interval(1, TimeUnit.SECONDS, scheduler);
        TestSubscriber<Long> ts = new TestSubscriber<>();
        w.subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS);
        ts.assertValues(0L, 1L);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.cancel();
        scheduler.advanceTimeTo(4, TimeUnit.SECONDS);
        ts.assertValues(0L, 1L);
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void withMultipleSubscribersStartingAtSameTime() {
        Flowable<Long> w = Flowable.interval(1, TimeUnit.SECONDS, scheduler);
        TestSubscriber<Long> ts1 = new TestSubscriber<>();
        TestSubscriber<Long> ts2 = new TestSubscriber<>();
        w.subscribe(ts1);
        w.subscribe(ts2);
        ts1.assertNoValues();
        ts2.assertNoValues();
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS);
        ts1.assertValues(0L, 1L);
        ts1.assertNoErrors();
        ts1.assertNotComplete();
        ts2.assertValues(0L, 1L);
        ts2.assertNoErrors();
        ts2.assertNotComplete();
        ts1.cancel();
        ts2.cancel();
        scheduler.advanceTimeTo(4, TimeUnit.SECONDS);
        ts1.assertValues(0L, 1L);
        ts1.assertNoErrors();
        ts1.assertNotComplete();
        ts2.assertValues(0L, 1L);
        ts2.assertNoErrors();
        ts2.assertNotComplete();
    }

    @Test
    public void withMultipleStaggeredSubscribers() {
        Flowable<Long> w = Flowable.interval(1, TimeUnit.SECONDS, scheduler);
        TestSubscriber<Long> ts1 = new TestSubscriber<>();
        w.subscribe(ts1);
        ts1.assertNoErrors();
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS);
        TestSubscriber<Long> ts2 = new TestSubscriber<>();
        w.subscribe(ts2);
        ts1.assertValues(0L, 1L);
        ts1.assertNoErrors();
        ts1.assertNotComplete();
        ts2.assertNoValues();
        scheduler.advanceTimeTo(4, TimeUnit.SECONDS);
        ts1.assertValues(0L, 1L, 2L, 3L);
        ts2.assertValues(0L, 1L);
        ts1.cancel();
        ts2.cancel();
        ts1.assertValues(0L, 1L, 2L, 3L);
        ts1.assertNoErrors();
        ts1.assertNotComplete();
        ts2.assertValues(0L, 1L);
        ts2.assertNoErrors();
        ts2.assertNotComplete();
    }

    @Test
    public void withMultipleStaggeredSubscribersAndPublish() {
        ConnectableFlowable<Long> w = Flowable.interval(1, TimeUnit.SECONDS, scheduler).publish();
        TestSubscriber<Long> ts1 = new TestSubscriber<>();
        w.subscribe(ts1);
        w.connect();
        ts1.assertNoValues();
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS);
        TestSubscriber<Long> ts2 = new TestSubscriber<>();
        w.subscribe(ts2);
        ts1.assertValues(0L, 1L);
        ts1.assertNoErrors();
        ts1.assertNotComplete();
        ts2.assertNoValues();
        scheduler.advanceTimeTo(4, TimeUnit.SECONDS);
        ts1.assertValues(0L, 1L, 2L, 3L);
        ts2.assertValues(2L, 3L);
        ts1.cancel();
        ts2.cancel();
        ts1.assertValues(0L, 1L, 2L, 3L);
        ts1.assertNoErrors();
        ts1.assertNotComplete();
        ts2.assertValues(2L, 3L);
        ts2.assertNoErrors();
        ts2.assertNotComplete();
    }

    @Test
    public void onceObserverThrows() {
        Flowable<Long> source = Flowable.timer(100, TimeUnit.MILLISECONDS, scheduler);
        source.safeSubscribe(new DefaultSubscriber<Long>() {

            @Override
            public void onNext(Long t) {
                throw new TestException();
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }
        });
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onNext(anyLong());
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void periodicObserverThrows() {
        Flowable<Long> source = Flowable.interval(100, 100, TimeUnit.MILLISECONDS, scheduler);
        InOrder inOrder = inOrder(subscriber);
        source.safeSubscribe(new DefaultSubscriber<Long>() {

            @Override
            public void onNext(Long t) {
                if (t > 0) {
                    throw new TestException();
                }
                subscriber.onNext(t);
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }
        });
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        inOrder.verify(subscriber).onNext(0L);
        inOrder.verify(subscriber).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Flowable.timer(1, TimeUnit.DAYS));
    }

    @Test
    public void backpressureNotReady() {
        Flowable.timer(1, TimeUnit.MILLISECONDS).test(0L).awaitDone(5, TimeUnit.SECONDS).assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void timerCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Long> ts = new TestSubscriber<>();
            final TestScheduler scheduler = new TestScheduler();
            Flowable.timer(1, TimeUnit.SECONDS, scheduler).subscribe(ts);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void timerDelayZero() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            for (int i = 0; i < 1000; i++) {
                Flowable.timer(0, TimeUnit.MILLISECONDS).blockingFirst();
            }
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void timerInterruptible() throws Exception {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        try {
            for (Scheduler s : new Scheduler[] { Schedulers.single(), Schedulers.computation(), Schedulers.newThread(), Schedulers.io(), Schedulers.from(exec, true) }) {
                final AtomicBoolean interrupted = new AtomicBoolean();
                TestSubscriber<Long> ts = Flowable.timer(1, TimeUnit.MILLISECONDS, s).map(new Function<Long, Long>() {

                    @Override
                    public Long apply(Long v) throws Exception {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ex) {
                            interrupted.set(true);
                        }
                        return v;
                    }
                }).test();
                Thread.sleep(500);
                ts.cancel();
                Thread.sleep(500);
                assertTrue(s.getClass().getSimpleName(), interrupted.get());
            }
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.timer(1, TimeUnit.MINUTES));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableTimerTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timerOnce() throws java.lang.Throwable {
            this.payloads.timerOnce.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timerPeriodically() throws java.lang.Throwable {
            this.payloads.timerPeriodically.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interval() throws java.lang.Throwable {
            this.payloads.interval.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withMultipleSubscribersStartingAtSameTime() throws java.lang.Throwable {
            this.payloads.withMultipleSubscribersStartingAtSameTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withMultipleStaggeredSubscribers() throws java.lang.Throwable {
            this.payloads.withMultipleStaggeredSubscribers.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withMultipleStaggeredSubscribersAndPublish() throws java.lang.Throwable {
            this.payloads.withMultipleStaggeredSubscribersAndPublish.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onceObserverThrows() throws java.lang.Throwable {
            this.payloads.onceObserverThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_periodicObserverThrows() throws java.lang.Throwable {
            this.payloads.periodicObserverThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNotReady() throws java.lang.Throwable {
            this.payloads.backpressureNotReady.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timerCancelRace() throws java.lang.Throwable {
            this.payloads.timerCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timerDelayZero() throws java.lang.Throwable {
            this.payloads.timerDelayZero.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timerInterruptible() throws java.lang.Throwable {
            this.payloads.timerInterruptible.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimerTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimerTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.before();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimerTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimerTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableTimerTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTimerTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableTimerTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableTimerTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement timerOnce;

            public org.junit.runners.model.Statement timerPeriodically;

            public org.junit.runners.model.Statement interval;

            public org.junit.runners.model.Statement withMultipleSubscribersStartingAtSameTime;

            public org.junit.runners.model.Statement withMultipleStaggeredSubscribers;

            public org.junit.runners.model.Statement withMultipleStaggeredSubscribersAndPublish;

            public org.junit.runners.model.Statement onceObserverThrows;

            public org.junit.runners.model.Statement periodicObserverThrows;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement backpressureNotReady;

            public org.junit.runners.model.Statement timerCancelRace;

            public org.junit.runners.model.Statement timerDelayZero;

            public org.junit.runners.model.Statement timerInterruptible;

            public org.junit.runners.model.Statement badRequest;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.timerOnce = _ClassStatement.forPayload(FlowableTimerTest::timerOnce, "timerOnce", this);
            this.payloads.timerPeriodically = _ClassStatement.forPayload(FlowableTimerTest::timerPeriodically, "timerPeriodically", this);
            this.payloads.interval = _ClassStatement.forPayload(FlowableTimerTest::interval, "interval", this);
            this.payloads.withMultipleSubscribersStartingAtSameTime = _ClassStatement.forPayload(FlowableTimerTest::withMultipleSubscribersStartingAtSameTime, "withMultipleSubscribersStartingAtSameTime", this);
            this.payloads.withMultipleStaggeredSubscribers = _ClassStatement.forPayload(FlowableTimerTest::withMultipleStaggeredSubscribers, "withMultipleStaggeredSubscribers", this);
            this.payloads.withMultipleStaggeredSubscribersAndPublish = _ClassStatement.forPayload(FlowableTimerTest::withMultipleStaggeredSubscribersAndPublish, "withMultipleStaggeredSubscribersAndPublish", this);
            this.payloads.onceObserverThrows = _ClassStatement.forPayload(FlowableTimerTest::onceObserverThrows, "onceObserverThrows", this);
            this.payloads.periodicObserverThrows = _ClassStatement.forPayload(FlowableTimerTest::periodicObserverThrows, "periodicObserverThrows", this);
            this.payloads.disposed = _ClassStatement.forPayload(FlowableTimerTest::disposed, "disposed", this);
            this.payloads.backpressureNotReady = _ClassStatement.forPayload(FlowableTimerTest::backpressureNotReady, "backpressureNotReady", this);
            this.payloads.timerCancelRace = _ClassStatement.forPayload(FlowableTimerTest::timerCancelRace, "timerCancelRace", this);
            this.payloads.timerDelayZero = _ClassStatement.forPayload(FlowableTimerTest::timerDelayZero, "timerDelayZero", this);
            this.payloads.timerInterruptible = _ClassStatement.forPayload(FlowableTimerTest::timerInterruptible, "timerInterruptible", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableTimerTest::badRequest, "badRequest", this);
        }
    }
}
