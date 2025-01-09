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

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.*;
import org.mockito.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.operators.observable.ObservableTimer.TimerObserver;
import io.reactivex.rxjava3.observables.ConnectableObservable;
import io.reactivex.rxjava3.observers.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableTimerTest extends RxJavaTest {

    @Mock
    Observer<Object> observer;

    @Mock
    Observer<Long> observer2;

    TestScheduler scheduler;

    @Before
    public void before() {
        observer = TestHelper.mockObserver();
        observer2 = TestHelper.mockObserver();
        scheduler = new TestScheduler();
    }

    @Test
    public void timerOnce() {
        Observable.timer(100, TimeUnit.MILLISECONDS, scheduler).subscribe(observer);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        verify(observer, times(1)).onNext(0L);
        verify(observer, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void timerPeriodically() {
        TestObserver<Long> to = new TestObserver<>();
        Observable.interval(100, 100, TimeUnit.MILLISECONDS, scheduler).subscribe(to);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        to.assertValue(0L);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        to.assertValues(0L, 1L);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        to.assertValues(0L, 1L, 2L);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        to.assertValues(0L, 1L, 2L, 3L);
        to.dispose();
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        to.assertValues(0L, 1L, 2L, 3L);
        to.assertNotComplete();
        to.assertNoErrors();
    }

    @Test
    public void interval() {
        Observable<Long> w = Observable.interval(1, TimeUnit.SECONDS, scheduler);
        TestObserver<Long> to = new TestObserver<>();
        w.subscribe(to);
        to.assertNoValues();
        to.assertNoErrors();
        to.assertNotComplete();
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS);
        to.assertValues(0L, 1L);
        to.assertNoErrors();
        to.assertNotComplete();
        to.dispose();
        scheduler.advanceTimeTo(4, TimeUnit.SECONDS);
        to.assertValues(0L, 1L);
        to.assertNoErrors();
        to.assertNotComplete();
    }

    @Test
    public void withMultipleSubscribersStartingAtSameTime() {
        Observable<Long> w = Observable.interval(1, TimeUnit.SECONDS, scheduler);
        TestObserver<Long> to1 = new TestObserver<>();
        TestObserver<Long> to2 = new TestObserver<>();
        w.subscribe(to1);
        w.subscribe(to2);
        to1.assertNoValues();
        to2.assertNoValues();
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS);
        to1.assertValues(0L, 1L);
        to1.assertNoErrors();
        to1.assertNotComplete();
        to2.assertValues(0L, 1L);
        to2.assertNoErrors();
        to2.assertNotComplete();
        to1.dispose();
        to2.dispose();
        scheduler.advanceTimeTo(4, TimeUnit.SECONDS);
        to1.assertValues(0L, 1L);
        to1.assertNoErrors();
        to1.assertNotComplete();
        to2.assertValues(0L, 1L);
        to2.assertNoErrors();
        to2.assertNotComplete();
    }

    @Test
    public void withMultipleStaggeredSubscribers() {
        Observable<Long> w = Observable.interval(1, TimeUnit.SECONDS, scheduler);
        TestObserver<Long> to1 = new TestObserver<>();
        w.subscribe(to1);
        to1.assertNoErrors();
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS);
        TestObserver<Long> to2 = new TestObserver<>();
        w.subscribe(to2);
        to1.assertValues(0L, 1L);
        to1.assertNoErrors();
        to1.assertNotComplete();
        to2.assertNoValues();
        scheduler.advanceTimeTo(4, TimeUnit.SECONDS);
        to1.assertValues(0L, 1L, 2L, 3L);
        to2.assertValues(0L, 1L);
        to1.dispose();
        to2.dispose();
        to1.assertValues(0L, 1L, 2L, 3L);
        to1.assertNoErrors();
        to1.assertNotComplete();
        to2.assertValues(0L, 1L);
        to2.assertNoErrors();
        to2.assertNotComplete();
    }

    @Test
    public void withMultipleStaggeredSubscribersAndPublish() {
        ConnectableObservable<Long> w = Observable.interval(1, TimeUnit.SECONDS, scheduler).publish();
        TestObserver<Long> to1 = new TestObserver<>();
        w.subscribe(to1);
        w.connect();
        to1.assertNoValues();
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS);
        TestObserver<Long> to2 = new TestObserver<>();
        w.subscribe(to2);
        to1.assertValues(0L, 1L);
        to1.assertNoErrors();
        to1.assertNotComplete();
        to2.assertNoValues();
        scheduler.advanceTimeTo(4, TimeUnit.SECONDS);
        to1.assertValues(0L, 1L, 2L, 3L);
        to2.assertValues(2L, 3L);
        to1.dispose();
        to2.dispose();
        to1.assertValues(0L, 1L, 2L, 3L);
        to1.assertNoErrors();
        to1.assertNotComplete();
        to2.assertValues(2L, 3L);
        to2.assertNoErrors();
        to2.assertNotComplete();
    }

    @Test
    public void onceObserverThrows() {
        Observable<Long> source = Observable.timer(100, TimeUnit.MILLISECONDS, scheduler);
        source.safeSubscribe(new DefaultObserver<Long>() {

            @Override
            public void onNext(Long t) {
                throw new TestException();
            }

            @Override
            public void onError(Throwable e) {
                observer.onError(e);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        });
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        verify(observer).onError(any(TestException.class));
        verify(observer, never()).onNext(anyLong());
        verify(observer, never()).onComplete();
    }

    @Test
    public void periodicObserverThrows() {
        Observable<Long> source = Observable.interval(100, 100, TimeUnit.MILLISECONDS, scheduler);
        InOrder inOrder = inOrder(observer);
        source.safeSubscribe(new DefaultObserver<Long>() {

            @Override
            public void onNext(Long t) {
                if (t > 0) {
                    throw new TestException();
                }
                observer.onNext(t);
            }

            @Override
            public void onError(Throwable e) {
                observer.onError(e);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        });
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        inOrder.verify(observer).onNext(0L);
        inOrder.verify(observer).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(observer, never()).onComplete();
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Observable.timer(1, TimeUnit.DAYS));
    }

    @Test
    public void timerDelayZero() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            for (int i = 0; i < 1000; i++) {
                Observable.timer(0, TimeUnit.MILLISECONDS).blockingFirst();
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
                TestObserver<Long> to = Observable.timer(1, TimeUnit.MILLISECONDS, s).map(new Function<Long, Long>() {

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
                to.dispose();
                Thread.sleep(500);
                assertTrue(s.getClass().getSimpleName(), interrupted.get());
            }
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void cancelledAndRun() {
        TestObserver<Long> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        TimerObserver tm = new TimerObserver(to);
        tm.dispose();
        tm.run();
        to.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableTimerTest instance;

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
        public void benchmark_timerDelayZero() throws java.lang.Throwable {
            this.payloads.timerDelayZero.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timerInterruptible() throws java.lang.Throwable {
            this.payloads.timerInterruptible.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelledAndRun() throws java.lang.Throwable {
            this.payloads.cancelledAndRun.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTimerTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTimerTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTimerTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTimerTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableTimerTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTimerTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableTimerTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableTimerTest.class, name);
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

            public org.junit.runners.model.Statement timerDelayZero;

            public org.junit.runners.model.Statement timerInterruptible;

            public org.junit.runners.model.Statement cancelledAndRun;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.timerOnce = _ClassStatement.forPayload(ObservableTimerTest::timerOnce, "timerOnce", this);
            this.payloads.timerPeriodically = _ClassStatement.forPayload(ObservableTimerTest::timerPeriodically, "timerPeriodically", this);
            this.payloads.interval = _ClassStatement.forPayload(ObservableTimerTest::interval, "interval", this);
            this.payloads.withMultipleSubscribersStartingAtSameTime = _ClassStatement.forPayload(ObservableTimerTest::withMultipleSubscribersStartingAtSameTime, "withMultipleSubscribersStartingAtSameTime", this);
            this.payloads.withMultipleStaggeredSubscribers = _ClassStatement.forPayload(ObservableTimerTest::withMultipleStaggeredSubscribers, "withMultipleStaggeredSubscribers", this);
            this.payloads.withMultipleStaggeredSubscribersAndPublish = _ClassStatement.forPayload(ObservableTimerTest::withMultipleStaggeredSubscribersAndPublish, "withMultipleStaggeredSubscribersAndPublish", this);
            this.payloads.onceObserverThrows = _ClassStatement.forPayload(ObservableTimerTest::onceObserverThrows, "onceObserverThrows", this);
            this.payloads.periodicObserverThrows = _ClassStatement.forPayload(ObservableTimerTest::periodicObserverThrows, "periodicObserverThrows", this);
            this.payloads.disposed = _ClassStatement.forPayload(ObservableTimerTest::disposed, "disposed", this);
            this.payloads.timerDelayZero = _ClassStatement.forPayload(ObservableTimerTest::timerDelayZero, "timerDelayZero", this);
            this.payloads.timerInterruptible = _ClassStatement.forPayload(ObservableTimerTest::timerInterruptible, "timerInterruptible", this);
            this.payloads.cancelledAndRun = _ClassStatement.forPayload(ObservableTimerTest::cancelledAndRun, "cancelledAndRun", this);
        }
    }
}
