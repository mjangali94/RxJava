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
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableMergeWithMaybeTest extends RxJavaTest {

    @Test
    public void normal() {
        Flowable.range(1, 5).mergeWith(Maybe.just(100)).test().assertResult(1, 2, 3, 4, 5, 100);
    }

    @Test
    public void emptyOther() {
        Flowable.range(1, 5).mergeWith(Maybe.<Integer>empty()).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void normalLong() {
        Flowable.range(1, 512).mergeWith(Maybe.just(100)).test().assertValueCount(513).assertComplete();
    }

    @Test
    public void normalLongRequestExact() {
        Flowable.range(1, 512).mergeWith(Maybe.just(100)).test(513).assertValueCount(513).assertComplete();
    }

    @Test
    public void take() {
        Flowable.range(1, 5).mergeWith(Maybe.just(100)).take(3).test().assertResult(1, 2, 3);
    }

    @Test
    public void cancel() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final MaybeSubject<Integer> cs = MaybeSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).test();
        assertTrue(pp.hasSubscribers());
        assertTrue(cs.hasObservers());
        ts.cancel();
        assertFalse(pp.hasSubscribers());
        assertFalse(cs.hasObservers());
    }

    @Test
    public void normalBackpressured() {
        Flowable.range(1, 5).mergeWith(Maybe.just(100)).test(0L).assertEmpty().requestMore(2).assertValues(100, 1).requestMore(2).assertValues(100, 1, 2, 3).requestMore(2).assertResult(100, 1, 2, 3, 4, 5);
    }

    @Test
    public void mainError() {
        Flowable.error(new TestException()).mergeWith(Maybe.just(100)).test().assertFailure(TestException.class);
    }

    @Test
    public void otherError() {
        Flowable.never().mergeWith(Maybe.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void completeRace() {
        for (int i = 0; i < 10000; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final MaybeSubject<Integer> cs = MaybeSubject.create();
            TestSubscriber<Integer> ts = pp.mergeWith(cs).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                    pp.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    cs.onSuccess(1);
                }
            };
            TestHelper.race(r1, r2);
            ts.assertResult(1, 1);
        }
    }

    @Test
    public void onNextSlowPath() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final MaybeSubject<Integer> cs = MaybeSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).subscribeWith(new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    pp.onNext(2);
                }
            }
        });
        pp.onNext(1);
        cs.onSuccess(3);
        pp.onNext(4);
        pp.onComplete();
        ts.assertResult(1, 2, 3, 4);
    }

    @Test
    public void onSuccessSlowPath() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final MaybeSubject<Integer> cs = MaybeSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).subscribeWith(new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    cs.onSuccess(2);
                }
            }
        });
        pp.onNext(1);
        pp.onNext(3);
        pp.onComplete();
        ts.assertResult(1, 2, 3);
    }

    @Test
    public void onSuccessSlowPathBackpressured() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final MaybeSubject<Integer> cs = MaybeSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).subscribeWith(new TestSubscriber<Integer>(1) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    cs.onSuccess(2);
                }
            }
        });
        pp.onNext(1);
        pp.onNext(3);
        pp.onComplete();
        ts.request(2);
        ts.assertResult(1, 2, 3);
    }

    @Test
    public void onSuccessFastPathBackpressuredRace() {
        for (int i = 0; i < 10000; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final MaybeSubject<Integer> cs = MaybeSubject.create();
            final TestSubscriber<Integer> ts = pp.mergeWith(cs).subscribeWith(new TestSubscriber<>(0));
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    cs.onSuccess(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.request(2);
                }
            };
            TestHelper.race(r1, r2);
            pp.onNext(2);
            pp.onComplete();
            ts.assertResult(1, 2);
        }
    }

    @Test
    public void onErrorMainOverflow() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final AtomicReference<Subscriber<?>> subscriber = new AtomicReference<>();
            TestSubscriber<Integer> ts = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    subscriber.set(s);
                }
            }.mergeWith(Maybe.<Integer>error(new IOException())).test();
            subscriber.get().onError(new TestException());
            ts.assertFailure(IOException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onErrorOtherOverflow() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.error(new IOException()).mergeWith(Maybe.error(new TestException())).test().assertFailure(IOException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onNextRequestRace() {
        for (int i = 0; i < 10000; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final MaybeSubject<Integer> cs = MaybeSubject.create();
            final TestSubscriber<Integer> ts = pp.mergeWith(cs).test(0);
            pp.onNext(0);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.request(3);
                }
            };
            TestHelper.race(r1, r2);
            cs.onSuccess(1);
            pp.onComplete();
            ts.assertResult(0, 1, 1);
        }
    }

    @Test
    public void doubleOnSubscribeMain() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.mergeWith(Maybe.just(1));
            }
        });
    }

    @Test
    public void noRequestOnError() {
        Flowable.empty().mergeWith(Maybe.error(new TestException())).test(0).assertFailure(TestException.class);
    }

    @Test
    public void drainExactRequestCancel() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final MaybeSubject<Integer> cs = MaybeSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).take(2).subscribeWith(new TestSubscriber<Integer>(2) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    cs.onSuccess(2);
                }
            }
        });
        pp.onNext(1);
        pp.onComplete();
        ts.request(2);
        ts.assertResult(1, 2);
    }

    @Test
    public void drainRequestWhenLimitReached() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final MaybeSubject<Integer> cs = MaybeSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).subscribeWith(new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    for (int i = 0; i < Flowable.bufferSize() - 1; i++) {
                        pp.onNext(i + 2);
                    }
                }
            }
        });
        cs.onSuccess(1);
        pp.onComplete();
        ts.request(2);
        ts.assertValueCount(Flowable.bufferSize());
        ts.assertComplete();
    }

    @Test
    public void cancelOtherOnMainError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(ms).test();
        assertTrue(pp.hasSubscribers());
        assertTrue(ms.hasObservers());
        pp.onError(new TestException());
        ts.assertFailure(TestException.class);
        assertFalse("main has observers!", pp.hasSubscribers());
        assertFalse("other has observers", ms.hasObservers());
    }

    @Test
    public void cancelMainOnOtherError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(ms).test();
        assertTrue(pp.hasSubscribers());
        assertTrue(ms.hasObservers());
        ms.onError(new TestException());
        ts.assertFailure(TestException.class);
        assertFalse("main has observers!", pp.hasSubscribers());
        assertFalse("other has observers", ms.hasObservers());
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.mergeWith(Maybe.just(1).hide());
            }
        });
    }

    @Test
    public void drainMoreWorkBeforeCancel() {
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).mergeWith(ms).doOnNext(v -> {
            if (v == 1) {
                ms.onSuccess(6);
                ts.cancel();
            }
        }).subscribe(ts);
        ts.assertValuesOnly(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableMergeWithMaybeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyOther() throws java.lang.Throwable {
            this.payloads.emptyOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalLong() throws java.lang.Throwable {
            this.payloads.normalLong.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalLongRequestExact() throws java.lang.Throwable {
            this.payloads.normalLongRequestExact.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalBackpressured() throws java.lang.Throwable {
            this.payloads.normalBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.payloads.mainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherError() throws java.lang.Throwable {
            this.payloads.otherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeRace() throws java.lang.Throwable {
            this.payloads.completeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextSlowPath() throws java.lang.Throwable {
            this.payloads.onNextSlowPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessSlowPath() throws java.lang.Throwable {
            this.payloads.onSuccessSlowPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessSlowPathBackpressured() throws java.lang.Throwable {
            this.payloads.onSuccessSlowPathBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessFastPathBackpressuredRace() throws java.lang.Throwable {
            this.payloads.onSuccessFastPathBackpressuredRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorMainOverflow() throws java.lang.Throwable {
            this.payloads.onErrorMainOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorOtherOverflow() throws java.lang.Throwable {
            this.payloads.onErrorOtherOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextRequestRace() throws java.lang.Throwable {
            this.payloads.onNextRequestRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeMain() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribeMain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noRequestOnError() throws java.lang.Throwable {
            this.payloads.noRequestOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_drainExactRequestCancel() throws java.lang.Throwable {
            this.payloads.drainExactRequestCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_drainRequestWhenLimitReached() throws java.lang.Throwable {
            this.payloads.drainRequestWhenLimitReached.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOtherOnMainError() throws java.lang.Throwable {
            this.payloads.cancelOtherOnMainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelMainOnOtherError() throws java.lang.Throwable {
            this.payloads.cancelMainOnOtherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_drainMoreWorkBeforeCancel() throws java.lang.Throwable {
            this.payloads.drainMoreWorkBeforeCancel.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeWithMaybeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeWithMaybeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeWithMaybeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeWithMaybeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableMergeWithMaybeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeWithMaybeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableMergeWithMaybeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableMergeWithMaybeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement emptyOther;

            public org.junit.runners.model.Statement normalLong;

            public org.junit.runners.model.Statement normalLongRequestExact;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement normalBackpressured;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement otherError;

            public org.junit.runners.model.Statement completeRace;

            public org.junit.runners.model.Statement onNextSlowPath;

            public org.junit.runners.model.Statement onSuccessSlowPath;

            public org.junit.runners.model.Statement onSuccessSlowPathBackpressured;

            public org.junit.runners.model.Statement onSuccessFastPathBackpressuredRace;

            public org.junit.runners.model.Statement onErrorMainOverflow;

            public org.junit.runners.model.Statement onErrorOtherOverflow;

            public org.junit.runners.model.Statement onNextRequestRace;

            public org.junit.runners.model.Statement doubleOnSubscribeMain;

            public org.junit.runners.model.Statement noRequestOnError;

            public org.junit.runners.model.Statement drainExactRequestCancel;

            public org.junit.runners.model.Statement drainRequestWhenLimitReached;

            public org.junit.runners.model.Statement cancelOtherOnMainError;

            public org.junit.runners.model.Statement cancelMainOnOtherError;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement drainMoreWorkBeforeCancel;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::normal, "normal", this);
            this.payloads.emptyOther = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::emptyOther, "emptyOther", this);
            this.payloads.normalLong = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::normalLong, "normalLong", this);
            this.payloads.normalLongRequestExact = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::normalLongRequestExact, "normalLongRequestExact", this);
            this.payloads.take = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::take, "take", this);
            this.payloads.cancel = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::cancel, "cancel", this);
            this.payloads.normalBackpressured = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::normalBackpressured, "normalBackpressured", this);
            this.payloads.mainError = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::mainError, "mainError", this);
            this.payloads.otherError = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::otherError, "otherError", this);
            this.payloads.completeRace = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::completeRace, "completeRace", this);
            this.payloads.onNextSlowPath = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::onNextSlowPath, "onNextSlowPath", this);
            this.payloads.onSuccessSlowPath = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::onSuccessSlowPath, "onSuccessSlowPath", this);
            this.payloads.onSuccessSlowPathBackpressured = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::onSuccessSlowPathBackpressured, "onSuccessSlowPathBackpressured", this);
            this.payloads.onSuccessFastPathBackpressuredRace = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::onSuccessFastPathBackpressuredRace, "onSuccessFastPathBackpressuredRace", this);
            this.payloads.onErrorMainOverflow = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::onErrorMainOverflow, "onErrorMainOverflow", this);
            this.payloads.onErrorOtherOverflow = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::onErrorOtherOverflow, "onErrorOtherOverflow", this);
            this.payloads.onNextRequestRace = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::onNextRequestRace, "onNextRequestRace", this);
            this.payloads.doubleOnSubscribeMain = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::doubleOnSubscribeMain, "doubleOnSubscribeMain", this);
            this.payloads.noRequestOnError = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::noRequestOnError, "noRequestOnError", this);
            this.payloads.drainExactRequestCancel = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::drainExactRequestCancel, "drainExactRequestCancel", this);
            this.payloads.drainRequestWhenLimitReached = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::drainRequestWhenLimitReached, "drainRequestWhenLimitReached", this);
            this.payloads.cancelOtherOnMainError = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::cancelOtherOnMainError, "cancelOtherOnMainError", this);
            this.payloads.cancelMainOnOtherError = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::cancelMainOnOtherError, "cancelMainOnOtherError", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.drainMoreWorkBeforeCancel = _ClassStatement.forPayload(FlowableMergeWithMaybeTest::drainMoreWorkBeforeCancel, "drainMoreWorkBeforeCancel", this);
        }
    }
}
