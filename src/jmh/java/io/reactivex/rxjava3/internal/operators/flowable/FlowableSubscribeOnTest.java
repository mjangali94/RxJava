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
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableSubscribeOn.SubscribeOnSubscriber;
import io.reactivex.rxjava3.internal.schedulers.ImmediateThinScheduler;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableSubscribeOnTest extends RxJavaTest {

    @Test
    public void issue813() throws InterruptedException {
        // https://github.com/ReactiveX/RxJava/issues/813
        final CountDownLatch scheduled = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(1);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(final Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                scheduled.countDown();
                try {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                    // this means we were unsubscribed (Scheduler shut down and interrupts)
                    // ... but we'll pretend we are like many Flowables that ignore interrupts
                    }
                    subscriber.onComplete();
                } catch (Throwable e) {
                    subscriber.onError(e);
                } finally {
                    doneLatch.countDown();
                }
            }
        }).subscribeOn(Schedulers.computation()).subscribe(ts);
        // wait for scheduling
        scheduled.await();
        // trigger unsubscribe
        ts.cancel();
        latch.countDown();
        doneLatch.await();
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void onError() {
        TestSubscriberEx<String> ts = new TestSubscriberEx<>();
        Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onError(new RuntimeException("fail"));
            }
        }).subscribeOn(Schedulers.computation()).subscribe(ts);
        ts.awaitDone(1000, TimeUnit.MILLISECONDS);
        ts.assertTerminated();
    }

    public static class SlowScheduler extends Scheduler {

        final Scheduler actual;

        final long delay;

        final TimeUnit unit;

        public SlowScheduler() {
            this(Schedulers.computation(), 2, TimeUnit.SECONDS);
        }

        public SlowScheduler(Scheduler actual, long delay, TimeUnit unit) {
            this.actual = actual;
            this.delay = delay;
            this.unit = unit;
        }

        @NonNull
        @Override
        public Worker createWorker() {
            return new SlowInner(actual.createWorker());
        }

        private final class SlowInner extends Worker {

            private final Scheduler.Worker actualInner;

            private SlowInner(Worker actual) {
                this.actualInner = actual;
            }

            @Override
            public void dispose() {
                actualInner.dispose();
            }

            @Override
            public boolean isDisposed() {
                return actualInner.isDisposed();
            }

            @NonNull
            @Override
            public Disposable schedule(@NonNull final Runnable action) {
                return actualInner.schedule(action, delay, unit);
            }

            @NonNull
            @Override
            public Disposable schedule(@NonNull final Runnable action, final long delayTime, @NonNull final TimeUnit delayUnit) {
                TimeUnit common = delayUnit.compareTo(unit) < 0 ? delayUnit : unit;
                long t = common.convert(delayTime, delayUnit) + common.convert(delay, unit);
                return actualInner.schedule(action, t, common);
            }
        }
    }

    @Test
    public void unsubscribeInfiniteStream() throws InterruptedException {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final AtomicInteger count = new AtomicInteger();
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                BooleanSubscription bs = new BooleanSubscription();
                sub.onSubscribe(bs);
                for (int i = 1; !bs.isCancelled(); i++) {
                    count.incrementAndGet();
                    sub.onNext(i);
                }
            }
        }).subscribeOn(Schedulers.newThread()).take(10).subscribe(ts);
        ts.awaitDone(1000, TimeUnit.MILLISECONDS);
        ts.cancel();
        // give time for the loop to continue
        Thread.sleep(200);
        ts.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertEquals(10, count.get());
    }

    @Test
    public void backpressureReschedulesCorrectly() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(10);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(new DefaultSubscriber<Integer>() {

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
                latch.countDown();
            }
        });
        ts.request(10);
        Flowable.range(1, 10000000).subscribeOn(Schedulers.newThread()).take(20).subscribe(ts);
        latch.await();
        Thread t = ts.lastThread();
        System.out.println("First schedule: " + t);
        assertTrue(t.getName().startsWith("Rx"));
        ts.request(10);
        ts.awaitDone(20, TimeUnit.SECONDS);
        System.out.println("After reschedule: " + ts.lastThread());
        assertEquals(t, ts.lastThread());
    }

    @Test
    public void setProducerSynchronousRequest() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.just(1, 2, 3).lift(new FlowableOperator<Integer, Integer>() {

            @Override
            public Subscriber<? super Integer> apply(final Subscriber<? super Integer> child) {
                final AtomicLong requested = new AtomicLong();
                child.onSubscribe(new Subscription() {

                    @Override
                    public void request(long n) {
                        if (!requested.compareAndSet(0, n)) {
                            child.onError(new RuntimeException("Expected to receive request before onNext but didn't"));
                        }
                    }

                    @Override
                    public void cancel() {
                    }
                });
                Subscriber<Integer> parent = new DefaultSubscriber<Integer>() {

                    @Override
                    public void onComplete() {
                        child.onComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        child.onError(e);
                    }

                    @Override
                    public void onNext(Integer t) {
                        if (requested.compareAndSet(0, -99)) {
                            child.onError(new RuntimeException("Got values before requested"));
                        }
                    }
                };
                return parent;
            }
        }).subscribeOn(Schedulers.newThread()).subscribe(ts);
        ts.awaitDone(20, TimeUnit.SECONDS);
        ts.assertNoErrors();
    }

    @Test
    public void cancelBeforeActualSubscribe() {
        TestScheduler test = new TestScheduler();
        TestSubscriberEx<Integer> ts = Flowable.just(1).hide().subscribeOn(test).to(TestHelper.<Integer>testConsumer(true));
        test.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertSubscribed().assertNoValues().assertNotTerminated();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).subscribeOn(Schedulers.single()));
    }

    @Test
    public void deferredRequestRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
            Worker w = Schedulers.computation().createWorker();
            final SubscribeOnSubscriber<Integer> so = new SubscribeOnSubscriber<>(ts, w, Flowable.<Integer>never(), true);
            ts.onSubscribe(so);
            final BooleanSubscription bs = new BooleanSubscription();
            try {
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        so.onSubscribe(bs);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        so.request(1);
                    }
                };
                TestHelper.race(r1, r2);
            } finally {
                w.dispose();
            }
        }
    }

    @Test
    public void nonScheduledRequests() {
        TestSubscriber<Object> ts = Flowable.create(new FlowableOnSubscribe<Object>() {

            @Override
            public void subscribe(FlowableEmitter<Object> s) throws Exception {
                for (int i = 1; i < 1001; i++) {
                    s.onNext(i);
                    Thread.sleep(1);
                }
                s.onComplete();
            }
        }, BackpressureStrategy.DROP).subscribeOn(Schedulers.single()).observeOn(Schedulers.computation()).test().awaitDone(20, TimeUnit.SECONDS).assertNoErrors().assertComplete();
        int c = ts.values().size();
        assertTrue("" + c, c > Flowable.bufferSize());
    }

    @Test
    public void scheduledRequests() {
        Flowable.create(new FlowableOnSubscribe<Object>() {

            @Override
            public void subscribe(FlowableEmitter<Object> s) throws Exception {
                for (int i = 1; i < 1001; i++) {
                    s.onNext(i);
                    Thread.sleep(1);
                }
                s.onComplete();
            }
        }, BackpressureStrategy.DROP).map(Functions.identity()).subscribeOn(Schedulers.single()).observeOn(Schedulers.computation()).test().awaitDone(20, TimeUnit.SECONDS).assertValueCount(Flowable.bufferSize()).assertNoErrors().assertComplete();
    }

    @Test
    public void nonScheduledRequestsNotSubsequentSubscribeOn() {
        TestSubscriber<Object> ts = Flowable.create(new FlowableOnSubscribe<Object>() {

            @Override
            public void subscribe(FlowableEmitter<Object> s) throws Exception {
                for (int i = 1; i < 1001; i++) {
                    s.onNext(i);
                    Thread.sleep(1);
                }
                s.onComplete();
            }
        }, BackpressureStrategy.DROP).map(Functions.identity()).subscribeOn(Schedulers.single(), false).observeOn(Schedulers.computation()).test().awaitDone(20, TimeUnit.SECONDS).assertNoErrors().assertComplete();
        int c = ts.values().size();
        assertTrue("" + c, c > Flowable.bufferSize());
    }

    @Test
    public void scheduledRequestsNotSubsequentSubscribeOn() {
        Flowable.create(new FlowableOnSubscribe<Object>() {

            @Override
            public void subscribe(FlowableEmitter<Object> s) throws Exception {
                for (int i = 1; i < 1001; i++) {
                    s.onNext(i);
                    Thread.sleep(1);
                }
                s.onComplete();
            }
        }, BackpressureStrategy.DROP).map(Functions.identity()).subscribeOn(Schedulers.single(), true).observeOn(Schedulers.computation()).test().awaitDone(20, TimeUnit.SECONDS).assertValueCount(Flowable.bufferSize()).assertNoErrors().assertComplete();
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().subscribeOn(ImmediateThinScheduler.INSTANCE));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableSubscribeOnTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue813() throws java.lang.Throwable {
            this.payloads.issue813.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onError() throws java.lang.Throwable {
            this.payloads.onError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeInfiniteStream() throws java.lang.Throwable {
            this.payloads.unsubscribeInfiniteStream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureReschedulesCorrectly() throws java.lang.Throwable {
            this.payloads.backpressureReschedulesCorrectly.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setProducerSynchronousRequest() throws java.lang.Throwable {
            this.payloads.setProducerSynchronousRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelBeforeActualSubscribe() throws java.lang.Throwable {
            this.payloads.cancelBeforeActualSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferredRequestRace() throws java.lang.Throwable {
            this.payloads.deferredRequestRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonScheduledRequests() throws java.lang.Throwable {
            this.payloads.nonScheduledRequests.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduledRequests() throws java.lang.Throwable {
            this.payloads.scheduledRequests.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonScheduledRequestsNotSubsequentSubscribeOn() throws java.lang.Throwable {
            this.payloads.nonScheduledRequestsNotSubsequentSubscribeOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scheduledRequestsNotSubsequentSubscribeOn() throws java.lang.Throwable {
            this.payloads.scheduledRequestsNotSubsequentSubscribeOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSubscribeOnTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSubscribeOnTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSubscribeOnTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSubscribeOnTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableSubscribeOnTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSubscribeOnTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableSubscribeOnTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableSubscribeOnTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement issue813;

            public org.junit.runners.model.Statement onError;

            public org.junit.runners.model.Statement unsubscribeInfiniteStream;

            public org.junit.runners.model.Statement backpressureReschedulesCorrectly;

            public org.junit.runners.model.Statement setProducerSynchronousRequest;

            public org.junit.runners.model.Statement cancelBeforeActualSubscribe;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement deferredRequestRace;

            public org.junit.runners.model.Statement nonScheduledRequests;

            public org.junit.runners.model.Statement scheduledRequests;

            public org.junit.runners.model.Statement nonScheduledRequestsNotSubsequentSubscribeOn;

            public org.junit.runners.model.Statement scheduledRequestsNotSubsequentSubscribeOn;

            public org.junit.runners.model.Statement badRequest;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.issue813 = _ClassStatement.forPayload(FlowableSubscribeOnTest::issue813, "issue813", this);
            this.payloads.onError = _ClassStatement.forPayload(FlowableSubscribeOnTest::onError, "onError", this);
            this.payloads.unsubscribeInfiniteStream = _ClassStatement.forPayload(FlowableSubscribeOnTest::unsubscribeInfiniteStream, "unsubscribeInfiniteStream", this);
            this.payloads.backpressureReschedulesCorrectly = _ClassStatement.forPayload(FlowableSubscribeOnTest::backpressureReschedulesCorrectly, "backpressureReschedulesCorrectly", this);
            this.payloads.setProducerSynchronousRequest = _ClassStatement.forPayload(FlowableSubscribeOnTest::setProducerSynchronousRequest, "setProducerSynchronousRequest", this);
            this.payloads.cancelBeforeActualSubscribe = _ClassStatement.forPayload(FlowableSubscribeOnTest::cancelBeforeActualSubscribe, "cancelBeforeActualSubscribe", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableSubscribeOnTest::dispose, "dispose", this);
            this.payloads.deferredRequestRace = _ClassStatement.forPayload(FlowableSubscribeOnTest::deferredRequestRace, "deferredRequestRace", this);
            this.payloads.nonScheduledRequests = _ClassStatement.forPayload(FlowableSubscribeOnTest::nonScheduledRequests, "nonScheduledRequests", this);
            this.payloads.scheduledRequests = _ClassStatement.forPayload(FlowableSubscribeOnTest::scheduledRequests, "scheduledRequests", this);
            this.payloads.nonScheduledRequestsNotSubsequentSubscribeOn = _ClassStatement.forPayload(FlowableSubscribeOnTest::nonScheduledRequestsNotSubsequentSubscribeOn, "nonScheduledRequestsNotSubsequentSubscribeOn", this);
            this.payloads.scheduledRequestsNotSubsequentSubscribeOn = _ClassStatement.forPayload(FlowableSubscribeOnTest::scheduledRequestsNotSubsequentSubscribeOn, "scheduledRequestsNotSubsequentSubscribeOn", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableSubscribeOnTest::badRequest, "badRequest", this);
        }
    }
}
