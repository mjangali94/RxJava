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

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableSampleTest extends RxJavaTest {

    private TestScheduler scheduler;

    private Scheduler.Worker innerScheduler;

    private Subscriber<Long> subscriber;

    private Subscriber<Object> subscriber2;

    @Before
    public // due to mocking
    void before() {
        scheduler = new TestScheduler();
        innerScheduler = scheduler.createWorker();
        subscriber = TestHelper.mockSubscriber();
        subscriber2 = TestHelper.mockSubscriber();
    }

    @Test
    public void sample() {
        Flowable<Long> source = Flowable.unsafeCreate(new Publisher<Long>() {

            @Override
            public void subscribe(final Subscriber<? super Long> subscriber1) {
                subscriber1.onSubscribe(new BooleanSubscription());
                innerScheduler.schedule(new Runnable() {

                    @Override
                    public void run() {
                        subscriber1.onNext(1L);
                    }
                }, 1, TimeUnit.SECONDS);
                innerScheduler.schedule(new Runnable() {

                    @Override
                    public void run() {
                        subscriber1.onNext(2L);
                    }
                }, 2, TimeUnit.SECONDS);
                innerScheduler.schedule(new Runnable() {

                    @Override
                    public void run() {
                        subscriber1.onComplete();
                    }
                }, 3, TimeUnit.SECONDS);
            }
        });
        Flowable<Long> sampled = source.sample(400L, TimeUnit.MILLISECONDS, scheduler);
        sampled.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        scheduler.advanceTimeTo(800L, TimeUnit.MILLISECONDS);
        verify(subscriber, never()).onNext(any(Long.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(1200L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, times(1)).onNext(1L);
        verify(subscriber, never()).onNext(2L);
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(1600L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, never()).onNext(1L);
        verify(subscriber, never()).onNext(2L);
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(2000L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, never()).onNext(1L);
        inOrder.verify(subscriber, times(1)).onNext(2L);
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(3000L, TimeUnit.MILLISECONDS);
        inOrder.verify(subscriber, never()).onNext(1L);
        inOrder.verify(subscriber, never()).onNext(2L);
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerNormal() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> sampler = PublishProcessor.create();
        Flowable<Integer> m = source.sample(sampler);
        m.subscribe(subscriber2);
        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        source.onNext(3);
        source.onNext(4);
        sampler.onNext(2);
        source.onComplete();
        sampler.onNext(3);
        InOrder inOrder = inOrder(subscriber2);
        inOrder.verify(subscriber2, never()).onNext(1);
        inOrder.verify(subscriber2, times(1)).onNext(2);
        inOrder.verify(subscriber2, never()).onNext(3);
        inOrder.verify(subscriber2, times(1)).onNext(4);
        inOrder.verify(subscriber2, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerNoDuplicates() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> sampler = PublishProcessor.create();
        Flowable<Integer> m = source.sample(sampler);
        m.subscribe(subscriber2);
        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        sampler.onNext(1);
        source.onNext(3);
        source.onNext(4);
        sampler.onNext(2);
        sampler.onNext(2);
        source.onComplete();
        sampler.onNext(3);
        InOrder inOrder = inOrder(subscriber2);
        inOrder.verify(subscriber2, never()).onNext(1);
        inOrder.verify(subscriber2, times(1)).onNext(2);
        inOrder.verify(subscriber2, never()).onNext(3);
        inOrder.verify(subscriber2, times(1)).onNext(4);
        inOrder.verify(subscriber2, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerTerminatingEarly() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> sampler = PublishProcessor.create();
        Flowable<Integer> m = source.sample(sampler);
        m.subscribe(subscriber2);
        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        sampler.onComplete();
        source.onNext(3);
        source.onNext(4);
        InOrder inOrder = inOrder(subscriber2);
        inOrder.verify(subscriber2, never()).onNext(1);
        inOrder.verify(subscriber2, times(1)).onNext(2);
        inOrder.verify(subscriber2, times(1)).onComplete();
        inOrder.verify(subscriber2, never()).onNext(any());
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerEmitAndTerminate() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> sampler = PublishProcessor.create();
        Flowable<Integer> m = source.sample(sampler);
        m.subscribe(subscriber2);
        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        source.onNext(3);
        source.onComplete();
        sampler.onNext(2);
        sampler.onComplete();
        InOrder inOrder = inOrder(subscriber2);
        inOrder.verify(subscriber2, never()).onNext(1);
        inOrder.verify(subscriber2, times(1)).onNext(2);
        inOrder.verify(subscriber2, never()).onNext(3);
        inOrder.verify(subscriber2, times(1)).onComplete();
        inOrder.verify(subscriber2, never()).onNext(any());
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerEmptySource() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> sampler = PublishProcessor.create();
        Flowable<Integer> m = source.sample(sampler);
        m.subscribe(subscriber2);
        source.onComplete();
        sampler.onNext(1);
        InOrder inOrder = inOrder(subscriber2);
        inOrder.verify(subscriber2, times(1)).onComplete();
        verify(subscriber2, never()).onNext(any());
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerSourceThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> sampler = PublishProcessor.create();
        Flowable<Integer> m = source.sample(sampler);
        m.subscribe(subscriber2);
        source.onNext(1);
        source.onError(new RuntimeException("Forced failure!"));
        sampler.onNext(1);
        InOrder inOrder = inOrder(subscriber2);
        inOrder.verify(subscriber2, times(1)).onError(any(Throwable.class));
        verify(subscriber2, never()).onNext(any());
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void sampleWithSamplerThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> sampler = PublishProcessor.create();
        Flowable<Integer> m = source.sample(sampler);
        m.subscribe(subscriber2);
        source.onNext(1);
        sampler.onNext(1);
        sampler.onError(new RuntimeException("Forced failure!"));
        InOrder inOrder = inOrder(subscriber2);
        inOrder.verify(subscriber2, times(1)).onNext(1);
        inOrder.verify(subscriber2, times(1)).onError(any(RuntimeException.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void sampleUnsubscribe() {
        final Subscription s = mock(Subscription.class);
        Flowable<Integer> f = Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(s);
            }
        });
        f.throttleLast(1, TimeUnit.MILLISECONDS).subscribe().dispose();
        verify(s).cancel();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().sample(1, TimeUnit.SECONDS, new TestScheduler()));
        TestHelper.checkDisposed(PublishProcessor.create().sample(Flowable.never()));
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).sample(1, TimeUnit.SECONDS).test().assertFailure(TestException.class);
    }

    @Test
    public void backpressureOverflow() {
        BehaviorProcessor.createDefault(1).sample(1, TimeUnit.MILLISECONDS).test(0L).awaitDone(5, TimeUnit.SECONDS).assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void backpressureOverflowWithOtherPublisher() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp1.sample(pp2).test(0L);
        pp1.onNext(1);
        pp2.onNext(2);
        ts.assertFailure(MissingBackpressureException.class);
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
    }

    @Test
    public void emitLastTimed() {
        Flowable.just(1).sample(1, TimeUnit.DAYS, true).test().assertResult(1);
    }

    @Test
    public void emitLastTimedEmpty() {
        Flowable.empty().sample(1, TimeUnit.DAYS, true).test().assertResult();
    }

    @Test
    public void emitLastTimedCustomScheduler() {
        Flowable.just(1).sample(1, TimeUnit.DAYS, Schedulers.single(), true).test().assertResult(1);
    }

    @Test
    public void emitLastTimedRunCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestScheduler scheduler = new TestScheduler();
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriber<Integer> ts = pp.sample(1, TimeUnit.SECONDS, scheduler, true).test();
            pp.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
                }
            };
            TestHelper.race(r1, r2);
            ts.assertResult(1);
        }
    }

    @Test
    public void emitLastOther() {
        Flowable.just(1).sample(Flowable.timer(1, TimeUnit.DAYS), true).test().assertResult(1);
    }

    @Test
    public void emitLastOtherEmpty() {
        Flowable.empty().sample(Flowable.timer(1, TimeUnit.DAYS), true).test().assertResult();
    }

    @Test
    public void emitLastOtherRunCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final PublishProcessor<Integer> sampler = PublishProcessor.create();
            TestSubscriber<Integer> ts = pp.sample(sampler, true).test();
            pp.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sampler.onNext(1);
                }
            };
            TestHelper.race(r1, r2);
            ts.assertResult(1);
        }
    }

    @Test
    public void emitLastOtherCompleteCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final PublishProcessor<Integer> sampler = PublishProcessor.create();
            TestSubscriber<Integer> ts = pp.sample(sampler, true).test();
            pp.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sampler.onComplete();
                }
            };
            TestHelper.race(r1, r2);
            ts.assertResult(1);
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.sample(1, TimeUnit.SECONDS);
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.sample(PublishProcessor.create());
            }
        });
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(PublishProcessor.create().sample(PublishProcessor.create()));
    }

    @Test
    public void badRequestTimed() {
        TestHelper.assertBadRequestReported(PublishProcessor.create().sample(1, TimeUnit.MINUTES));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableSampleTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sample() throws java.lang.Throwable {
            this.payloads.sample.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerNormal() throws java.lang.Throwable {
            this.payloads.sampleWithSamplerNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerNoDuplicates() throws java.lang.Throwable {
            this.payloads.sampleWithSamplerNoDuplicates.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerTerminatingEarly() throws java.lang.Throwable {
            this.payloads.sampleWithSamplerTerminatingEarly.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerEmitAndTerminate() throws java.lang.Throwable {
            this.payloads.sampleWithSamplerEmitAndTerminate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerEmptySource() throws java.lang.Throwable {
            this.payloads.sampleWithSamplerEmptySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerSourceThrows() throws java.lang.Throwable {
            this.payloads.sampleWithSamplerSourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerThrows() throws java.lang.Throwable {
            this.payloads.sampleWithSamplerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleUnsubscribe() throws java.lang.Throwable {
            this.payloads.sampleUnsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureOverflow() throws java.lang.Throwable {
            this.payloads.backpressureOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureOverflowWithOtherPublisher() throws java.lang.Throwable {
            this.payloads.backpressureOverflowWithOtherPublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastTimed() throws java.lang.Throwable {
            this.payloads.emitLastTimed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastTimedEmpty() throws java.lang.Throwable {
            this.payloads.emitLastTimedEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastTimedCustomScheduler() throws java.lang.Throwable {
            this.payloads.emitLastTimedCustomScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastTimedRunCompleteRace() throws java.lang.Throwable {
            this.payloads.emitLastTimedRunCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastOther() throws java.lang.Throwable {
            this.payloads.emitLastOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastOtherEmpty() throws java.lang.Throwable {
            this.payloads.emitLastOtherEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastOtherRunCompleteRace() throws java.lang.Throwable {
            this.payloads.emitLastOtherRunCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastOtherCompleteCompleteRace() throws java.lang.Throwable {
            this.payloads.emitLastOtherCompleteCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequestTimed() throws java.lang.Throwable {
            this.payloads.badRequestTimed.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSampleTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSampleTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSampleTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSampleTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableSampleTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSampleTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableSampleTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableSampleTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement sample;

            public org.junit.runners.model.Statement sampleWithSamplerNormal;

            public org.junit.runners.model.Statement sampleWithSamplerNoDuplicates;

            public org.junit.runners.model.Statement sampleWithSamplerTerminatingEarly;

            public org.junit.runners.model.Statement sampleWithSamplerEmitAndTerminate;

            public org.junit.runners.model.Statement sampleWithSamplerEmptySource;

            public org.junit.runners.model.Statement sampleWithSamplerSourceThrows;

            public org.junit.runners.model.Statement sampleWithSamplerThrows;

            public org.junit.runners.model.Statement sampleUnsubscribe;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement backpressureOverflow;

            public org.junit.runners.model.Statement backpressureOverflowWithOtherPublisher;

            public org.junit.runners.model.Statement emitLastTimed;

            public org.junit.runners.model.Statement emitLastTimedEmpty;

            public org.junit.runners.model.Statement emitLastTimedCustomScheduler;

            public org.junit.runners.model.Statement emitLastTimedRunCompleteRace;

            public org.junit.runners.model.Statement emitLastOther;

            public org.junit.runners.model.Statement emitLastOtherEmpty;

            public org.junit.runners.model.Statement emitLastOtherRunCompleteRace;

            public org.junit.runners.model.Statement emitLastOtherCompleteCompleteRace;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement badRequestTimed;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.sample = _ClassStatement.forPayload(FlowableSampleTest::sample, "sample", this);
            this.payloads.sampleWithSamplerNormal = _ClassStatement.forPayload(FlowableSampleTest::sampleWithSamplerNormal, "sampleWithSamplerNormal", this);
            this.payloads.sampleWithSamplerNoDuplicates = _ClassStatement.forPayload(FlowableSampleTest::sampleWithSamplerNoDuplicates, "sampleWithSamplerNoDuplicates", this);
            this.payloads.sampleWithSamplerTerminatingEarly = _ClassStatement.forPayload(FlowableSampleTest::sampleWithSamplerTerminatingEarly, "sampleWithSamplerTerminatingEarly", this);
            this.payloads.sampleWithSamplerEmitAndTerminate = _ClassStatement.forPayload(FlowableSampleTest::sampleWithSamplerEmitAndTerminate, "sampleWithSamplerEmitAndTerminate", this);
            this.payloads.sampleWithSamplerEmptySource = _ClassStatement.forPayload(FlowableSampleTest::sampleWithSamplerEmptySource, "sampleWithSamplerEmptySource", this);
            this.payloads.sampleWithSamplerSourceThrows = _ClassStatement.forPayload(FlowableSampleTest::sampleWithSamplerSourceThrows, "sampleWithSamplerSourceThrows", this);
            this.payloads.sampleWithSamplerThrows = _ClassStatement.forPayload(FlowableSampleTest::sampleWithSamplerThrows, "sampleWithSamplerThrows", this);
            this.payloads.sampleUnsubscribe = _ClassStatement.forPayload(FlowableSampleTest::sampleUnsubscribe, "sampleUnsubscribe", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableSampleTest::dispose, "dispose", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableSampleTest::error, "error", this);
            this.payloads.backpressureOverflow = _ClassStatement.forPayload(FlowableSampleTest::backpressureOverflow, "backpressureOverflow", this);
            this.payloads.backpressureOverflowWithOtherPublisher = _ClassStatement.forPayload(FlowableSampleTest::backpressureOverflowWithOtherPublisher, "backpressureOverflowWithOtherPublisher", this);
            this.payloads.emitLastTimed = _ClassStatement.forPayload(FlowableSampleTest::emitLastTimed, "emitLastTimed", this);
            this.payloads.emitLastTimedEmpty = _ClassStatement.forPayload(FlowableSampleTest::emitLastTimedEmpty, "emitLastTimedEmpty", this);
            this.payloads.emitLastTimedCustomScheduler = _ClassStatement.forPayload(FlowableSampleTest::emitLastTimedCustomScheduler, "emitLastTimedCustomScheduler", this);
            this.payloads.emitLastTimedRunCompleteRace = _ClassStatement.forPayload(FlowableSampleTest::emitLastTimedRunCompleteRace, "emitLastTimedRunCompleteRace", this);
            this.payloads.emitLastOther = _ClassStatement.forPayload(FlowableSampleTest::emitLastOther, "emitLastOther", this);
            this.payloads.emitLastOtherEmpty = _ClassStatement.forPayload(FlowableSampleTest::emitLastOtherEmpty, "emitLastOtherEmpty", this);
            this.payloads.emitLastOtherRunCompleteRace = _ClassStatement.forPayload(FlowableSampleTest::emitLastOtherRunCompleteRace, "emitLastOtherRunCompleteRace", this);
            this.payloads.emitLastOtherCompleteCompleteRace = _ClassStatement.forPayload(FlowableSampleTest::emitLastOtherCompleteCompleteRace, "emitLastOtherCompleteCompleteRace", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableSampleTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableSampleTest::badRequest, "badRequest", this);
            this.payloads.badRequestTimed = _ClassStatement.forPayload(FlowableSampleTest::badRequestTimed, "badRequestTimed", this);
        }
    }
}
