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
package io.reactivex.rxjava3.internal.schedulers;

import static io.reactivex.rxjava3.core.Flowable.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.schedulers.SchedulerWhen.*;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SchedulerWhenTest extends RxJavaTest {

    @Test
    public void asyncMaxConcurrent() {
        TestScheduler tSched = new TestScheduler();
        SchedulerWhen sched = maxConcurrentScheduler(tSched);
        TestSubscriber<Long> tSub = TestSubscriber.create();
        asyncWork(sched).subscribe(tSub);
        tSub.assertValueCount(0);
        tSched.advanceTimeBy(0, SECONDS);
        tSub.assertValueCount(0);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(2);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(4);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(5);
        tSub.assertComplete();
        sched.dispose();
    }

    @Test
    public void asyncDelaySubscription() {
        final TestScheduler tSched = new TestScheduler();
        SchedulerWhen sched = throttleScheduler(tSched);
        TestSubscriber<Long> tSub = TestSubscriber.create();
        asyncWork(sched).subscribe(tSub);
        tSub.assertValueCount(0);
        tSched.advanceTimeBy(0, SECONDS);
        tSub.assertValueCount(0);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(1);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(1);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(2);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(2);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(3);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(3);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(4);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(4);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(5);
        tSub.assertComplete();
        sched.dispose();
    }

    @Test
    public void syncMaxConcurrent() {
        TestScheduler tSched = new TestScheduler();
        SchedulerWhen sched = maxConcurrentScheduler(tSched);
        TestSubscriber<Long> tSub = TestSubscriber.create();
        syncWork(sched).subscribe(tSub);
        tSub.assertValueCount(0);
        tSched.advanceTimeBy(0, SECONDS);
        // since all the work is synchronous nothing is blocked and its all done
        tSub.assertValueCount(5);
        tSub.assertComplete();
        sched.dispose();
    }

    @Test
    public void syncDelaySubscription() {
        final TestScheduler tSched = new TestScheduler();
        SchedulerWhen sched = throttleScheduler(tSched);
        TestSubscriber<Long> tSub = TestSubscriber.create();
        syncWork(sched).subscribe(tSub);
        tSub.assertValueCount(0);
        tSched.advanceTimeBy(0, SECONDS);
        tSub.assertValueCount(1);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(2);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(3);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(4);
        tSched.advanceTimeBy(1, SECONDS);
        tSub.assertValueCount(5);
        tSub.assertComplete();
        sched.dispose();
    }

    private Flowable<Long> asyncWork(final Scheduler sched) {
        return Flowable.range(1, 5).flatMap(new Function<Integer, Flowable<Long>>() {

            @Override
            public Flowable<Long> apply(Integer t) {
                return Flowable.timer(1, SECONDS, sched);
            }
        });
    }

    private Flowable<Long> syncWork(final Scheduler sched) {
        return Flowable.range(1, 5).flatMap(new Function<Integer, Flowable<Long>>() {

            @Override
            public Flowable<Long> apply(Integer t) {
                return Flowable.defer(new Supplier<Flowable<Long>>() {

                    @Override
                    public Flowable<Long> get() {
                        return Flowable.just(0l);
                    }
                }).subscribeOn(sched);
            }
        });
    }

    private SchedulerWhen maxConcurrentScheduler(TestScheduler tSched) {
        SchedulerWhen sched = new SchedulerWhen(new Function<Flowable<Flowable<Completable>>, Completable>() {

            @Override
            public Completable apply(Flowable<Flowable<Completable>> workerActions) {
                Flowable<Completable> workers = workerActions.map(new Function<Flowable<Completable>, Completable>() {

                    @Override
                    public Completable apply(Flowable<Completable> actions) {
                        return Completable.concat(actions);
                    }
                });
                return Completable.merge(workers, 2);
            }
        }, tSched);
        return sched;
    }

    private SchedulerWhen throttleScheduler(final TestScheduler tSched) {
        SchedulerWhen sched = new SchedulerWhen(new Function<Flowable<Flowable<Completable>>, Completable>() {

            @Override
            public Completable apply(Flowable<Flowable<Completable>> workerActions) {
                Flowable<Completable> workers = workerActions.map(new Function<Flowable<Completable>, Completable>() {

                    @Override
                    public Completable apply(Flowable<Completable> actions) {
                        return Completable.concat(actions);
                    }
                });
                return Completable.concat(workers.map(new Function<Completable, Completable>() {

                    @Override
                    public Completable apply(Completable worker) {
                        return worker.delay(1, SECONDS, tSched);
                    }
                }));
            }
        }, tSched);
        return sched;
    }

    @Test
    public void raceConditions() {
        Scheduler comp = Schedulers.computation();
        Scheduler limited = comp.when(new Function<Flowable<Flowable<Completable>>, Completable>() {

            @Override
            public Completable apply(Flowable<Flowable<Completable>> t) {
                return Completable.merge(Flowable.merge(t, 10));
            }
        });
        merge(just(just(1).subscribeOn(limited).observeOn(comp)).repeat(1000)).blockingSubscribe();
    }

    @Test
    public void subscribedDisposable() {
        SchedulerWhen.SUBSCRIBED.dispose();
        assertFalse(SchedulerWhen.SUBSCRIBED.isDisposed());
    }

    @Test(expected = TestException.class)
    public void combineCrashInConstructor() {
        new SchedulerWhen(new Function<Flowable<Flowable<Completable>>, Completable>() {

            @Override
            public Completable apply(Flowable<Flowable<Completable>> v) throws Exception {
                throw new TestException();
            }
        }, Schedulers.single());
    }

    @Test
    public void disposed() {
        SchedulerWhen sw = new SchedulerWhen(new Function<Flowable<Flowable<Completable>>, Completable>() {

            @Override
            public Completable apply(Flowable<Flowable<Completable>> v) throws Exception {
                return Completable.never();
            }
        }, Schedulers.single());
        assertFalse(sw.isDisposed());
        sw.dispose();
        assertTrue(sw.isDisposed());
    }

    @Test
    public void scheduledActiondisposedSetRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final ScheduledAction sa = new ScheduledAction() {

                private static final long serialVersionUID = -672980251643733156L;

                @Override
                protected Disposable callActual(Worker actualWorker, CompletableObserver actionCompletable) {
                    return Disposable.empty();
                }
            };
            assertFalse(sa.isDisposed());
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    sa.dispose();
                }
            };
            TestHelper.race(r1, r1);
            assertTrue(sa.isDisposed());
        }
    }

    @Test
    public void scheduledActionStates() {
        final AtomicInteger count = new AtomicInteger();
        ScheduledAction sa = new ScheduledAction() {

            private static final long serialVersionUID = -672980251643733156L;

            @Override
            protected Disposable callActual(Worker actualWorker, CompletableObserver actionCompletable) {
                count.incrementAndGet();
                return Disposable.empty();
            }
        };
        assertFalse(sa.isDisposed());
        sa.dispose();
        assertTrue(sa.isDisposed());
        sa.dispose();
        assertTrue(sa.isDisposed());
        // should not run when disposed
        sa.call(Schedulers.single().createWorker(), null);
        assertEquals(0, count.get());
        // should not run when already scheduled
        sa.set(Disposable.empty());
        sa.call(Schedulers.single().createWorker(), null);
        assertEquals(0, count.get());
        // disposed while in call
        sa = new ScheduledAction() {

            private static final long serialVersionUID = -672980251643733156L;

            @Override
            protected Disposable callActual(Worker actualWorker, CompletableObserver actionCompletable) {
                count.incrementAndGet();
                dispose();
                return Disposable.empty();
            }
        };
        sa.call(Schedulers.single().createWorker(), null);
        assertEquals(1, count.get());
    }

    @Test
    public void onCompleteActionRunCrash() {
        final AtomicInteger count = new AtomicInteger();
        OnCompletedAction a = new OnCompletedAction(new Runnable() {

            @Override
            public void run() {
                throw new TestException();
            }
        }, new DisposableCompletableObserver() {

            @Override
            public void onComplete() {
                count.incrementAndGet();
            }

            @Override
            public void onError(Throwable e) {
                count.decrementAndGet();
                e.printStackTrace();
            }
        });
        try {
            a.run();
            fail("Should have thrown");
        } catch (TestException expected) {
        }
        assertEquals(1, count.get());
    }

    @Test
    public void queueWorkerDispose() {
        QueueWorker qw = new QueueWorker(PublishProcessor.<ScheduledAction>create(), Schedulers.single().createWorker());
        assertFalse(qw.isDisposed());
        qw.dispose();
        assertTrue(qw.isDisposed());
        qw.dispose();
        assertTrue(qw.isDisposed());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SchedulerWhenTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncMaxConcurrent() throws java.lang.Throwable {
            this.payloads.asyncMaxConcurrent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncDelaySubscription() throws java.lang.Throwable {
            this.payloads.asyncDelaySubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncMaxConcurrent() throws java.lang.Throwable {
            this.payloads.syncMaxConcurrent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncDelaySubscription() throws java.lang.Throwable {
            this.payloads.syncDelaySubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_raceConditions() throws java.lang.Throwable {
            this.payloads.raceConditions.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribedDisposable() throws java.lang.Throwable {
            this.payloads.subscribedDisposable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineCrashInConstructor() throws java.lang.Throwable {
            this.payloads.combineCrashInConstructor.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduledActiondisposedSetRace() throws java.lang.Throwable {
            this.payloads.scheduledActiondisposedSetRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduledActionStates() throws java.lang.Throwable {
            this.payloads.scheduledActionStates.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteActionRunCrash() throws java.lang.Throwable {
            this.payloads.onCompleteActionRunCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_queueWorkerDispose() throws java.lang.Throwable {
            this.payloads.queueWorkerDispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SchedulerWhenTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SchedulerWhenTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SchedulerWhenTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SchedulerWhenTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SchedulerWhenTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SchedulerWhenTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SchedulerWhenTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SchedulerWhenTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement asyncMaxConcurrent;

            public org.junit.runners.model.Statement asyncDelaySubscription;

            public org.junit.runners.model.Statement syncMaxConcurrent;

            public org.junit.runners.model.Statement syncDelaySubscription;

            public org.junit.runners.model.Statement raceConditions;

            public org.junit.runners.model.Statement subscribedDisposable;

            public org.junit.runners.model.Statement combineCrashInConstructor;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement scheduledActiondisposedSetRace;

            public org.junit.runners.model.Statement scheduledActionStates;

            public org.junit.runners.model.Statement onCompleteActionRunCrash;

            public org.junit.runners.model.Statement queueWorkerDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.asyncMaxConcurrent = _ClassStatement.forPayload(SchedulerWhenTest::asyncMaxConcurrent, "asyncMaxConcurrent", this);
            this.payloads.asyncDelaySubscription = _ClassStatement.forPayload(SchedulerWhenTest::asyncDelaySubscription, "asyncDelaySubscription", this);
            this.payloads.syncMaxConcurrent = _ClassStatement.forPayload(SchedulerWhenTest::syncMaxConcurrent, "syncMaxConcurrent", this);
            this.payloads.syncDelaySubscription = _ClassStatement.forPayload(SchedulerWhenTest::syncDelaySubscription, "syncDelaySubscription", this);
            this.payloads.raceConditions = _ClassStatement.forPayload(SchedulerWhenTest::raceConditions, "raceConditions", this);
            this.payloads.subscribedDisposable = _ClassStatement.forPayload(SchedulerWhenTest::subscribedDisposable, "subscribedDisposable", this);
            this.payloads.combineCrashInConstructor = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(SchedulerWhenTest::combineCrashInConstructor, io.reactivex.rxjava3.exceptions.TestException.class), "combineCrashInConstructor", this);
            this.payloads.disposed = _ClassStatement.forPayload(SchedulerWhenTest::disposed, "disposed", this);
            this.payloads.scheduledActiondisposedSetRace = _ClassStatement.forPayload(SchedulerWhenTest::scheduledActiondisposedSetRace, "scheduledActiondisposedSetRace", this);
            this.payloads.scheduledActionStates = _ClassStatement.forPayload(SchedulerWhenTest::scheduledActionStates, "scheduledActionStates", this);
            this.payloads.onCompleteActionRunCrash = _ClassStatement.forPayload(SchedulerWhenTest::onCompleteActionRunCrash, "onCompleteActionRunCrash", this);
            this.payloads.queueWorkerDispose = _ClassStatement.forPayload(SchedulerWhenTest::queueWorkerDispose, "queueWorkerDispose", this);
        }
    }
}
