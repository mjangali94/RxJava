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
package io.reactivex.rxjava3.internal.operators.mixed;

import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapMaybe.ConcatMapMaybeSubscriber;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.internal.util.ErrorMode;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.MaybeSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableConcatMapMaybeTest extends RxJavaTest {

    @Test
    public void simple() {
        Flowable.range(1, 5).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void simpleLongPrefetch() {
        Flowable.range(1, 1024).concatMapMaybe(Maybe::just, 32).test().assertValueCount(1024).assertNoErrors().assertComplete();
    }

    @Test
    public void simpleLongPrefetchHidden() {
        Flowable.range(1, 1024).hide().concatMapMaybe(Maybe::just, 32).test().assertValueCount(1024).assertNoErrors().assertComplete();
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = Flowable.range(1, 1024).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }, 32).test(0);
        for (int i = 1; i <= 1024; i++) {
            ts.assertValueCount(i - 1).assertNoErrors().assertNotComplete().requestMore(1).assertValueCount(i).assertNoErrors();
        }
        ts.assertComplete();
    }

    @Test
    public void empty() {
        Flowable.range(1, 10).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.empty();
            }
        }).test().assertResult();
    }

    @Test
    public void mixed() {
        Flowable.range(1, 10).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                if (v % 2 == 0) {
                    return Maybe.just(v);
                }
                return Maybe.empty();
            }
        }).test().assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void mixedLong() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 1024).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                if (v % 2 == 0) {
                    return Maybe.just(v).subscribeOn(Schedulers.computation());
                }
                return Maybe.<Integer>empty().subscribeOn(Schedulers.computation());
            }
        }).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertValueCount(512).assertNoErrors().assertComplete();
        for (int i = 0; i < 512; i++) {
            ts.assertValueAt(i, (i + 1) * 2);
        }
    }

    @Test
    public void mainError() {
        Flowable.error(new TestException()).concatMapMaybe(Functions.justFunction(Maybe.just(1))).test().assertFailure(TestException.class);
    }

    @Test
    public void innerError() {
        Flowable.just(1).concatMapMaybe(Functions.justFunction(Maybe.error(new TestException()))).test().assertFailure(TestException.class);
    }

    @Test
    public void mainBoundaryErrorInnerSuccess() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestSubscriber<Integer> ts = pp.concatMapMaybeDelayError(Functions.justFunction(ms), false).test();
        ts.assertEmpty();
        pp.onNext(1);
        assertTrue(ms.hasObservers());
        pp.onError(new TestException());
        assertTrue(ms.hasObservers());
        ts.assertEmpty();
        ms.onSuccess(1);
        ts.assertFailure(TestException.class, 1);
    }

    @Test
    public void mainBoundaryErrorInnerEmpty() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestSubscriber<Integer> ts = pp.concatMapMaybeDelayError(Functions.justFunction(ms), false).test();
        ts.assertEmpty();
        pp.onNext(1);
        assertTrue(ms.hasObservers());
        pp.onError(new TestException());
        assertTrue(ms.hasObservers());
        ts.assertEmpty();
        ms.onComplete();
        ts.assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.concatMapMaybeDelayError(Functions.justFunction(Maybe.empty()));
            }
        });
    }

    @Test
    public void queueOverflow() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onNext(2);
                    s.onNext(3);
                    s.onError(new TestException());
                }
            }.concatMapMaybe(Functions.justFunction(Maybe.never()), 1).test().assertFailure(MissingBackpressureException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void limit() {
        Flowable.range(1, 5).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }).take(3).test().assertResult(1, 2, 3);
    }

    @Test
    public void cancel() {
        Flowable.range(1, 5).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }).test(3).assertValues(1, 2, 3).assertNoErrors().assertNotComplete().cancel();
    }

    @Test
    public void innerErrorAfterMainError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final AtomicReference<MaybeObserver<? super Integer>> obs = new AtomicReference<>();
            TestSubscriberEx<Integer> ts = pp.concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

                @Override
                public MaybeSource<Integer> apply(Integer v) throws Exception {
                    return new Maybe<Integer>() {

                        @Override
                        protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                            observer.onSubscribe(Disposable.empty());
                            obs.set(observer);
                        }
                    };
                }
            }).to(TestHelper.<Integer>testConsumer());
            pp.onNext(1);
            pp.onError(new TestException("outer"));
            obs.get().onError(new TestException("inner"));
            ts.assertFailureAndMessage(TestException.class, "outer");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "inner");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void delayAllErrors() {
        TestSubscriberEx<Object> ts = Flowable.range(1, 5).concatMapMaybeDelayError(new Function<Integer, MaybeSource<? extends Object>>() {

            @Override
            public MaybeSource<? extends Object> apply(Integer v) throws Exception {
                return Maybe.error(new TestException());
            }
        }).to(TestHelper.<Object>testConsumer()).assertFailure(CompositeException.class);
        CompositeException ce = (CompositeException) ts.errors().get(0);
        assertEquals(5, ce.getExceptions().size());
    }

    @Test
    public void mapperCrash() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Object> ts = pp.concatMapMaybe(new Function<Integer, MaybeSource<? extends Object>>() {

            @Override
            public MaybeSource<? extends Object> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test();
        ts.assertEmpty();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertFailure(TestException.class);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void cancelNoConcurrentClean() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        ConcatMapMaybeSubscriber<Integer, Integer> operator = new ConcatMapMaybeSubscriber<>(ts, Functions.justFunction(Maybe.<Integer>never()), 16, ErrorMode.IMMEDIATE);
        operator.onSubscribe(new BooleanSubscription());
        operator.queue.offer(1);
        operator.getAndIncrement();
        ts.cancel();
        assertFalse(operator.queue.isEmpty());
        operator.addAndGet(-2);
        operator.cancel();
        assertTrue(operator.queue.isEmpty());
    }

    @Test
    public void innerSuccessDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final MaybeSubject<Integer> ms = MaybeSubject.create();
            final TestSubscriber<Integer> ts = Flowable.just(1).hide().concatMapMaybe(Functions.justFunction(ms)).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ms.onSuccess(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            TestHelper.race(r1, r2);
            ts.assertNoErrors();
        }
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.concatMapMaybe(new Function<Integer, Maybe<Integer>>() {

                    @Override
                    public Maybe<Integer> apply(Integer v) throws Throwable {
                        return Maybe.just(v).hide();
                    }
                });
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayError() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.concatMapMaybeDelayError(new Function<Integer, Maybe<Integer>>() {

                    @Override
                    public Maybe<Integer> apply(Integer v) throws Throwable {
                        return Maybe.just(v).hide();
                    }
                }, false, 2);
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayErrorTillEnd() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.concatMapMaybeDelayError(new Function<Integer, Maybe<Integer>>() {

                    @Override
                    public Maybe<Integer> apply(Integer v) throws Throwable {
                        return Maybe.just(v).hide();
                    }
                }, true, 2);
            }
        });
    }

    @Test
    public void basicNonFused() {
        Flowable.range(1, 5).hide().concatMapMaybe(v -> Maybe.just(v).hide()).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void basicSyncFused() {
        Flowable.range(1, 5).concatMapMaybe(v -> Maybe.just(v).hide()).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void basicAsyncFused() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.concatMapMaybe(v -> Maybe.just(v).hide()).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void basicFusionRejected() {
        TestHelper.<Integer>rejectFlowableFusion().concatMapMaybe(v -> Maybe.just(v).hide()).test().assertEmpty();
    }

    @Test
    public void fusedPollCrash() {
        Flowable.range(1, 5).map(v -> {
            if (v == 3) {
                throw new TestException();
            }
            return v;
        }).compose(TestHelper.flowableStripBoundary()).concatMapMaybe(v -> Maybe.just(v).hide()).test().assertFailure(TestException.class, 1, 2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableConcatMapMaybeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.payloads.simple.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleLongPrefetch() throws java.lang.Throwable {
            this.payloads.simpleLongPrefetch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleLongPrefetchHidden() throws java.lang.Throwable {
            this.payloads.simpleLongPrefetchHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.payloads.backpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixed() throws java.lang.Throwable {
            this.payloads.mixed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixedLong() throws java.lang.Throwable {
            this.payloads.mixedLong.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.payloads.mainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerError() throws java.lang.Throwable {
            this.payloads.innerError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainBoundaryErrorInnerSuccess() throws java.lang.Throwable {
            this.payloads.mainBoundaryErrorInnerSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainBoundaryErrorInnerEmpty() throws java.lang.Throwable {
            this.payloads.mainBoundaryErrorInnerEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_queueOverflow() throws java.lang.Throwable {
            this.payloads.queueOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_limit() throws java.lang.Throwable {
            this.payloads.limit.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorAfterMainError() throws java.lang.Throwable {
            this.payloads.innerErrorAfterMainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayAllErrors() throws java.lang.Throwable {
            this.payloads.delayAllErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperCrash() throws java.lang.Throwable {
            this.payloads.mapperCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelNoConcurrentClean() throws java.lang.Throwable {
            this.payloads.cancelNoConcurrentClean.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerSuccessDisposeRace() throws java.lang.Throwable {
            this.payloads.innerSuccessDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelDelayError() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancelDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelDelayErrorTillEnd() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancelDelayErrorTillEnd.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicNonFused() throws java.lang.Throwable {
            this.payloads.basicNonFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicSyncFused() throws java.lang.Throwable {
            this.payloads.basicSyncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicAsyncFused() throws java.lang.Throwable {
            this.payloads.basicAsyncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicFusionRejected() throws java.lang.Throwable {
            this.payloads.basicFusionRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPollCrash() throws java.lang.Throwable {
            this.payloads.fusedPollCrash.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapMaybeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapMaybeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapMaybeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapMaybeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableConcatMapMaybeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapMaybeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableConcatMapMaybeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableConcatMapMaybeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement simple;

            public org.junit.runners.model.Statement simpleLongPrefetch;

            public org.junit.runners.model.Statement simpleLongPrefetchHidden;

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement mixed;

            public org.junit.runners.model.Statement mixedLong;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement innerError;

            public org.junit.runners.model.Statement mainBoundaryErrorInnerSuccess;

            public org.junit.runners.model.Statement mainBoundaryErrorInnerEmpty;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement queueOverflow;

            public org.junit.runners.model.Statement limit;

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement innerErrorAfterMainError;

            public org.junit.runners.model.Statement delayAllErrors;

            public org.junit.runners.model.Statement mapperCrash;

            public org.junit.runners.model.Statement cancelNoConcurrentClean;

            public org.junit.runners.model.Statement innerSuccessDisposeRace;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayErrorTillEnd;

            public org.junit.runners.model.Statement basicNonFused;

            public org.junit.runners.model.Statement basicSyncFused;

            public org.junit.runners.model.Statement basicAsyncFused;

            public org.junit.runners.model.Statement basicFusionRejected;

            public org.junit.runners.model.Statement fusedPollCrash;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.simple = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::simple, "simple", this);
            this.payloads.simpleLongPrefetch = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::simpleLongPrefetch, "simpleLongPrefetch", this);
            this.payloads.simpleLongPrefetchHidden = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::simpleLongPrefetchHidden, "simpleLongPrefetchHidden", this);
            this.payloads.backpressure = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::backpressure, "backpressure", this);
            this.payloads.empty = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::empty, "empty", this);
            this.payloads.mixed = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::mixed, "mixed", this);
            this.payloads.mixedLong = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::mixedLong, "mixedLong", this);
            this.payloads.mainError = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::mainError, "mainError", this);
            this.payloads.innerError = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::innerError, "innerError", this);
            this.payloads.mainBoundaryErrorInnerSuccess = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::mainBoundaryErrorInnerSuccess, "mainBoundaryErrorInnerSuccess", this);
            this.payloads.mainBoundaryErrorInnerEmpty = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::mainBoundaryErrorInnerEmpty, "mainBoundaryErrorInnerEmpty", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.queueOverflow = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::queueOverflow, "queueOverflow", this);
            this.payloads.limit = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::limit, "limit", this);
            this.payloads.cancel = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::cancel, "cancel", this);
            this.payloads.innerErrorAfterMainError = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::innerErrorAfterMainError, "innerErrorAfterMainError", this);
            this.payloads.delayAllErrors = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::delayAllErrors, "delayAllErrors", this);
            this.payloads.mapperCrash = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::mapperCrash, "mapperCrash", this);
            this.payloads.cancelNoConcurrentClean = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::cancelNoConcurrentClean, "cancelNoConcurrentClean", this);
            this.payloads.innerSuccessDisposeRace = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::innerSuccessDisposeRace, "innerSuccessDisposeRace", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.undeliverableUponCancelDelayErrorTillEnd = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::undeliverableUponCancelDelayErrorTillEnd, "undeliverableUponCancelDelayErrorTillEnd", this);
            this.payloads.basicNonFused = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::basicNonFused, "basicNonFused", this);
            this.payloads.basicSyncFused = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::basicSyncFused, "basicSyncFused", this);
            this.payloads.basicAsyncFused = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::basicAsyncFused, "basicAsyncFused", this);
            this.payloads.basicFusionRejected = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::basicFusionRejected, "basicFusionRejected", this);
            this.payloads.fusedPollCrash = _ClassStatement.forPayload(FlowableConcatMapMaybeTest::fusedPollCrash, "fusedPollCrash", this);
        }
    }
}
