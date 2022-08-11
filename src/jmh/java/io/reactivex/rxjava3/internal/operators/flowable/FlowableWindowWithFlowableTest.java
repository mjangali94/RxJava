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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableWindowWithFlowableTest extends RxJavaTest {

    @Test
    public void windowViaFlowableNormal1() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> boundary = PublishProcessor.create();
        final Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        final List<Subscriber<Object>> values = new ArrayList<>();
        Subscriber<Flowable<Integer>> wo = new DefaultSubscriber<Flowable<Integer>>() {

            @Override
            public void onNext(Flowable<Integer> args) {
                final Subscriber<Object> mo = TestHelper.mockSubscriber();
                values.add(mo);
                args.subscribe(mo);
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }
        };
        source.window(boundary).subscribe(wo);
        int n = 30;
        for (int i = 0; i < n; i++) {
            source.onNext(i);
            if (i % 3 == 2 && i < n - 1) {
                boundary.onNext(i / 3);
            }
        }
        source.onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        assertEquals(n / 3, values.size());
        int j = 0;
        for (Subscriber<Object> mo : values) {
            verify(mo, never()).onError(any(Throwable.class));
            for (int i = 0; i < 3; i++) {
                verify(mo).onNext(j + i);
            }
            verify(mo).onComplete();
            j += 3;
        }
        verify(subscriber).onComplete();
    }

    @Test
    public void windowViaFlowableBoundaryCompletes() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> boundary = PublishProcessor.create();
        final Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        final List<Subscriber<Object>> values = new ArrayList<>();
        Subscriber<Flowable<Integer>> wo = new DefaultSubscriber<Flowable<Integer>>() {

            @Override
            public void onNext(Flowable<Integer> args) {
                final Subscriber<Object> mo = TestHelper.mockSubscriber();
                values.add(mo);
                args.subscribe(mo);
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }
        };
        source.window(boundary).subscribe(wo);
        int n = 30;
        for (int i = 0; i < n; i++) {
            source.onNext(i);
            if (i % 3 == 2 && i < n - 1) {
                boundary.onNext(i / 3);
            }
        }
        boundary.onComplete();
        assertEquals(n / 3, values.size());
        int j = 0;
        for (Subscriber<Object> mo : values) {
            for (int i = 0; i < 3; i++) {
                verify(mo).onNext(j + i);
            }
            verify(mo).onComplete();
            verify(mo, never()).onError(any(Throwable.class));
            j += 3;
        }
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void windowViaFlowableBoundaryThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> boundary = PublishProcessor.create();
        final Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        final List<Subscriber<Object>> values = new ArrayList<>();
        Subscriber<Flowable<Integer>> wo = new DefaultSubscriber<Flowable<Integer>>() {

            @Override
            public void onNext(Flowable<Integer> args) {
                final Subscriber<Object> mo = TestHelper.mockSubscriber();
                values.add(mo);
                args.subscribe(mo);
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }
        };
        source.window(boundary).subscribe(wo);
        source.onNext(0);
        source.onNext(1);
        source.onNext(2);
        boundary.onError(new TestException());
        assertEquals(1, values.size());
        Subscriber<Object> mo = values.get(0);
        verify(mo).onNext(0);
        verify(mo).onNext(1);
        verify(mo).onNext(2);
        verify(mo).onError(any(TestException.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber).onError(any(TestException.class));
    }

    @Test
    public void windowViaFlowableThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> boundary = PublishProcessor.create();
        final Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        final List<Subscriber<Object>> values = new ArrayList<>();
        Subscriber<Flowable<Integer>> wo = new DefaultSubscriber<Flowable<Integer>>() {

            @Override
            public void onNext(Flowable<Integer> args) {
                final Subscriber<Object> mo = TestHelper.mockSubscriber();
                values.add(mo);
                args.subscribe(mo);
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }
        };
        source.window(boundary).subscribe(wo);
        source.onNext(0);
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        assertEquals(1, values.size());
        Subscriber<Object> mo = values.get(0);
        verify(mo).onNext(0);
        verify(mo).onNext(1);
        verify(mo).onNext(2);
        verify(mo).onError(any(TestException.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber).onError(any(TestException.class));
    }

    @Test
    public void boundaryDispose() {
        TestHelper.checkDisposed(Flowable.never().window(Flowable.never()));
    }

    @Test
    public void boundaryOnError() {
        TestSubscriberEx<Object> ts = Flowable.error(new TestException()).window(Flowable.never()).flatMap(Functions.<Flowable<Object>>identity(), true).to(TestHelper.<Object>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(ts.errors().get(0));
        TestHelper.assertError(errors, 0, TestException.class);
    }

    @Test
    public void innerBadSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return Flowable.just(1).window(f).flatMap(new Function<Flowable<Integer>, Flowable<Integer>>() {

                    @Override
                    public Flowable<Integer> apply(Flowable<Integer> v) throws Exception {
                        return v;
                    }
                });
            }
        }, false, 1, 1, (Object[]) null);
    }

    @Test
    public void reentrant() {
        final FlowableProcessor<Integer> ps = PublishProcessor.<Integer>create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    ps.onNext(2);
                    ps.onComplete();
                }
            }
        };
        ps.window(BehaviorProcessor.createDefault(1)).flatMap(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> v) throws Exception {
                return v;
            }
        }).subscribe(ts);
        ps.onNext(1);
        ts.awaitDone(1, TimeUnit.SECONDS).assertResult(1, 2);
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Object>, Object>() {

            @Override
            public Object apply(Flowable<Object> f) throws Exception {
                return f.window(Flowable.never()).flatMap(new Function<Flowable<Object>, Flowable<Object>>() {

                    @Override
                    public Flowable<Object> apply(Flowable<Object> v) throws Exception {
                        return v;
                    }
                });
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void boundaryDirectMissingBackpressure() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            BehaviorProcessor.create().window(Flowable.error(new TestException())).test(0).assertFailure(MissingBackpressureException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void boundaryDirectMissingBackpressureNoNullPointerException() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            BehaviorProcessor.createDefault(1).window(Flowable.error(new TestException())).test(0).assertFailure(MissingBackpressureException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void boundaryDirectSecondMissingBackpressure() {
        BehaviorProcessor.createDefault(1).window(Flowable.just(1)).test(1).assertError(MissingBackpressureException.class).assertNotComplete();
    }

    @Test
    public void boundaryDirectDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Flowable<Object>>>() {

            @Override
            public Publisher<Flowable<Object>> apply(Flowable<Object> f) throws Exception {
                return f.window(Flowable.never()).takeLast(1);
            }
        });
    }

    @Test
    public void upstreamDisposedWhenOutputsDisposed() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> boundary = PublishProcessor.create();
        TestSubscriber<Integer> ts = source.window(boundary).take(1).flatMap(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> w) throws Exception {
                return w.take(1);
            }
        }).test();
        source.onNext(1);
        assertFalse("source not disposed", source.hasSubscribers());
        assertFalse("boundary not disposed", boundary.hasSubscribers());
        ts.assertResult(1);
    }

    @Test
    public void mainAndBoundaryBothError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final AtomicReference<Subscriber<? super Object>> ref = new AtomicReference<>();
            TestSubscriberEx<Flowable<Object>> ts = Flowable.error(new TestException("main")).window(new Flowable<Object>() {

                @Override
                protected void subscribeActual(Subscriber<? super Object> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    ref.set(subscriber);
                }
            }).doOnNext(new Consumer<Flowable<Object>>() {

                @Override
                public void accept(Flowable<Object> w) throws Throwable {
                    // avoid abandonment
                    w.subscribe(Functions.emptyConsumer(), Functions.emptyConsumer());
                }
            }).to(TestHelper.<Flowable<Object>>testConsumer());
            ts.assertValueCount(1).assertError(TestException.class).assertErrorMessage("main").assertNotComplete();
            ref.get().onError(new TestException("inner"));
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "inner");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void mainCompleteBoundaryErrorRace() {
        final TestException ex = new TestException();
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final AtomicReference<Subscriber<? super Object>> refMain = new AtomicReference<>();
                final AtomicReference<Subscriber<? super Object>> ref = new AtomicReference<>();
                TestSubscriberEx<Flowable<Object>> ts = new Flowable<Object>() {

                    @Override
                    protected void subscribeActual(Subscriber<? super Object> subscriber) {
                        subscriber.onSubscribe(new BooleanSubscription());
                        refMain.set(subscriber);
                    }
                }.window(new Flowable<Object>() {

                    @Override
                    protected void subscribeActual(Subscriber<? super Object> subscriber) {
                        subscriber.onSubscribe(new BooleanSubscription());
                        ref.set(subscriber);
                    }
                }).to(TestHelper.<Flowable<Object>>testConsumer());
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        refMain.get().onComplete();
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ref.get().onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                ts.assertValueCount(1).assertTerminated();
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void mainNextBoundaryNextRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final AtomicReference<Subscriber<? super Object>> refMain = new AtomicReference<>();
            final AtomicReference<Subscriber<? super Object>> ref = new AtomicReference<>();
            TestSubscriber<Flowable<Object>> ts = new Flowable<Object>() {

                @Override
                protected void subscribeActual(Subscriber<? super Object> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    refMain.set(subscriber);
                }
            }.window(new Flowable<Object>() {

                @Override
                protected void subscribeActual(Subscriber<? super Object> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    ref.set(subscriber);
                }
            }).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    refMain.get().onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ref.get().onNext(1);
                }
            };
            TestHelper.race(r1, r2);
            ts.assertValueCount(2).assertNotComplete().assertNoErrors();
        }
    }

    @Test
    public void takeOneAnotherBoundary() {
        final AtomicReference<Subscriber<? super Object>> refMain = new AtomicReference<>();
        final AtomicReference<Subscriber<? super Object>> ref = new AtomicReference<>();
        TestSubscriberEx<Flowable<Object>> ts = new Flowable<Object>() {

            @Override
            protected void subscribeActual(Subscriber<? super Object> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                refMain.set(subscriber);
            }
        }.window(new Flowable<Object>() {

            @Override
            protected void subscribeActual(Subscriber<? super Object> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                ref.set(subscriber);
            }
        }).to(TestHelper.<Flowable<Object>>testConsumer());
        ts.assertValueCount(1).assertNotTerminated().cancel();
        ref.get().onNext(1);
        ts.assertValueCount(1).assertNotTerminated();
    }

    @Test
    public void disposeMainBoundaryCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final AtomicReference<Subscriber<? super Object>> refMain = new AtomicReference<>();
            final AtomicReference<Subscriber<? super Object>> ref = new AtomicReference<>();
            final TestSubscriber<Flowable<Object>> ts = new Flowable<Object>() {

                @Override
                protected void subscribeActual(Subscriber<? super Object> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    refMain.set(subscriber);
                }
            }.window(new Flowable<Object>() {

                @Override
                protected void subscribeActual(Subscriber<? super Object> subscriber) {
                    final AtomicInteger counter = new AtomicInteger();
                    subscriber.onSubscribe(new Subscription() {

                        @Override
                        public void cancel() {
                            // about a microsecond
                            for (int i = 0; i < 100; i++) {
                                counter.incrementAndGet();
                            }
                        }

                        @Override
                        public void request(long n) {
                        }
                    });
                    ref.set(subscriber);
                }
            }).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    Subscriber<Object> subscriber = ref.get();
                    subscriber.onNext(1);
                    subscriber.onComplete();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    @SuppressUndeliverable
    public void disposeMainBoundaryErrorRace() {
        final TestException ex = new TestException();
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final AtomicReference<Subscriber<? super Object>> refMain = new AtomicReference<>();
            final AtomicReference<Subscriber<? super Object>> ref = new AtomicReference<>();
            final TestSubscriber<Flowable<Object>> ts = new Flowable<Object>() {

                @Override
                protected void subscribeActual(Subscriber<? super Object> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    refMain.set(subscriber);
                }
            }.window(new Flowable<Object>() {

                @Override
                protected void subscribeActual(Subscriber<? super Object> subscriber) {
                    final AtomicInteger counter = new AtomicInteger();
                    subscriber.onSubscribe(new Subscription() {

                        @Override
                        public void cancel() {
                            // about a microsecond
                            for (int i = 0; i < 100; i++) {
                                counter.incrementAndGet();
                            }
                        }

                        @Override
                        public void request(long n) {
                        }
                    });
                    ref.set(subscriber);
                }
            }).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    Subscriber<Object> subscriber = ref.get();
                    subscriber.onNext(1);
                    subscriber.onError(ex);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void cancellingWindowCancelsUpstream() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.window(Flowable.just(1).concatWith(Flowable.<Integer>never())).take(1).flatMap(new Function<Flowable<Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Integer> w) throws Throwable {
                return w.take(1);
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertResult(1);
        assertFalse("Processor still has subscribers!", pp.hasSubscribers());
    }

    @Test
    public void windowAbandonmentCancelsUpstream() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final AtomicReference<Flowable<Integer>> inner = new AtomicReference<>();
        TestSubscriber<Flowable<Integer>> ts = pp.window(Flowable.<Integer>never()).doOnNext(new Consumer<Flowable<Integer>>() {

            @Override
            public void accept(Flowable<Integer> v) throws Throwable {
                inner.set(v);
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        ts.assertValueCount(1);
        pp.onNext(1);
        assertTrue(pp.hasSubscribers());
        ts.cancel();
        ts.assertValueCount(1).assertNoErrors().assertNotComplete();
        assertFalse("Processor still has subscribers!", pp.hasSubscribers());
        inner.get().test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableWindowWithFlowableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowViaFlowableNormal1() throws java.lang.Throwable {
            this.payloads.windowViaFlowableNormal1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowViaFlowableBoundaryCompletes() throws java.lang.Throwable {
            this.payloads.windowViaFlowableBoundaryCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowViaFlowableBoundaryThrows() throws java.lang.Throwable {
            this.payloads.windowViaFlowableBoundaryThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowViaFlowableThrows() throws java.lang.Throwable {
            this.payloads.windowViaFlowableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryDispose() throws java.lang.Throwable {
            this.payloads.boundaryDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryOnError() throws java.lang.Throwable {
            this.payloads.boundaryOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerBadSource() throws java.lang.Throwable {
            this.payloads.innerBadSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrant() throws java.lang.Throwable {
            this.payloads.reentrant.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryDirectMissingBackpressure() throws java.lang.Throwable {
            this.payloads.boundaryDirectMissingBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryDirectMissingBackpressureNoNullPointerException() throws java.lang.Throwable {
            this.payloads.boundaryDirectMissingBackpressureNoNullPointerException.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryDirectSecondMissingBackpressure() throws java.lang.Throwable {
            this.payloads.boundaryDirectSecondMissingBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryDirectDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.boundaryDirectDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamDisposedWhenOutputsDisposed() throws java.lang.Throwable {
            this.payloads.upstreamDisposedWhenOutputsDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainAndBoundaryBothError() throws java.lang.Throwable {
            this.payloads.mainAndBoundaryBothError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompleteBoundaryErrorRace() throws java.lang.Throwable {
            this.payloads.mainCompleteBoundaryErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainNextBoundaryNextRace() throws java.lang.Throwable {
            this.payloads.mainNextBoundaryNextRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeOneAnotherBoundary() throws java.lang.Throwable {
            this.payloads.takeOneAnotherBoundary.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeMainBoundaryCompleteRace() throws java.lang.Throwable {
            this.payloads.disposeMainBoundaryCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeMainBoundaryErrorRace() throws java.lang.Throwable {
            this.payloads.disposeMainBoundaryErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellingWindowCancelsUpstream() throws java.lang.Throwable {
            this.payloads.cancellingWindowCancelsUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowAbandonmentCancelsUpstream() throws java.lang.Throwable {
            this.payloads.windowAbandonmentCancelsUpstream.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithFlowableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithFlowableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithFlowableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithFlowableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableWindowWithFlowableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithFlowableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableWindowWithFlowableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableWindowWithFlowableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement windowViaFlowableNormal1;

            public org.junit.runners.model.Statement windowViaFlowableBoundaryCompletes;

            public org.junit.runners.model.Statement windowViaFlowableBoundaryThrows;

            public org.junit.runners.model.Statement windowViaFlowableThrows;

            public org.junit.runners.model.Statement boundaryDispose;

            public org.junit.runners.model.Statement boundaryOnError;

            public org.junit.runners.model.Statement innerBadSource;

            public org.junit.runners.model.Statement reentrant;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement boundaryDirectMissingBackpressure;

            public org.junit.runners.model.Statement boundaryDirectMissingBackpressureNoNullPointerException;

            public org.junit.runners.model.Statement boundaryDirectSecondMissingBackpressure;

            public org.junit.runners.model.Statement boundaryDirectDoubleOnSubscribe;

            public org.junit.runners.model.Statement upstreamDisposedWhenOutputsDisposed;

            public org.junit.runners.model.Statement mainAndBoundaryBothError;

            public org.junit.runners.model.Statement mainCompleteBoundaryErrorRace;

            public org.junit.runners.model.Statement mainNextBoundaryNextRace;

            public org.junit.runners.model.Statement takeOneAnotherBoundary;

            public org.junit.runners.model.Statement disposeMainBoundaryCompleteRace;

            public org.junit.runners.model.Statement disposeMainBoundaryErrorRace;

            public org.junit.runners.model.Statement cancellingWindowCancelsUpstream;

            public org.junit.runners.model.Statement windowAbandonmentCancelsUpstream;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.windowViaFlowableNormal1 = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::windowViaFlowableNormal1, "windowViaFlowableNormal1", this);
            this.payloads.windowViaFlowableBoundaryCompletes = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::windowViaFlowableBoundaryCompletes, "windowViaFlowableBoundaryCompletes", this);
            this.payloads.windowViaFlowableBoundaryThrows = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::windowViaFlowableBoundaryThrows, "windowViaFlowableBoundaryThrows", this);
            this.payloads.windowViaFlowableThrows = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::windowViaFlowableThrows, "windowViaFlowableThrows", this);
            this.payloads.boundaryDispose = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::boundaryDispose, "boundaryDispose", this);
            this.payloads.boundaryOnError = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::boundaryOnError, "boundaryOnError", this);
            this.payloads.innerBadSource = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::innerBadSource, "innerBadSource", this);
            this.payloads.reentrant = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::reentrant, "reentrant", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::badSource, "badSource", this);
            this.payloads.boundaryDirectMissingBackpressure = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::boundaryDirectMissingBackpressure, "boundaryDirectMissingBackpressure", this);
            this.payloads.boundaryDirectMissingBackpressureNoNullPointerException = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::boundaryDirectMissingBackpressureNoNullPointerException, "boundaryDirectMissingBackpressureNoNullPointerException", this);
            this.payloads.boundaryDirectSecondMissingBackpressure = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::boundaryDirectSecondMissingBackpressure, "boundaryDirectSecondMissingBackpressure", this);
            this.payloads.boundaryDirectDoubleOnSubscribe = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::boundaryDirectDoubleOnSubscribe, "boundaryDirectDoubleOnSubscribe", this);
            this.payloads.upstreamDisposedWhenOutputsDisposed = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::upstreamDisposedWhenOutputsDisposed, "upstreamDisposedWhenOutputsDisposed", this);
            this.payloads.mainAndBoundaryBothError = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::mainAndBoundaryBothError, "mainAndBoundaryBothError", this);
            this.payloads.mainCompleteBoundaryErrorRace = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::mainCompleteBoundaryErrorRace, "mainCompleteBoundaryErrorRace", this);
            this.payloads.mainNextBoundaryNextRace = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::mainNextBoundaryNextRace, "mainNextBoundaryNextRace", this);
            this.payloads.takeOneAnotherBoundary = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::takeOneAnotherBoundary, "takeOneAnotherBoundary", this);
            this.payloads.disposeMainBoundaryCompleteRace = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::disposeMainBoundaryCompleteRace, "disposeMainBoundaryCompleteRace", this);
            this.payloads.disposeMainBoundaryErrorRace = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::disposeMainBoundaryErrorRace, "disposeMainBoundaryErrorRace", this);
            this.payloads.cancellingWindowCancelsUpstream = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::cancellingWindowCancelsUpstream, "cancellingWindowCancelsUpstream", this);
            this.payloads.windowAbandonmentCancelsUpstream = _ClassStatement.forPayload(FlowableWindowWithFlowableTest::windowAbandonmentCancelsUpstream, "windowAbandonmentCancelsUpstream", this);
        }
    }
}
