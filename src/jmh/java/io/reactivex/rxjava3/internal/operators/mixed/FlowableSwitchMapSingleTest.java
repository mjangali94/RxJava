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
import java.util.concurrent.atomic.AtomicReference;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableSwitchMapSingleTest extends RxJavaTest {

    @Test
    public void simple() {
        Flowable.range(1, 5).switchMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void mainError() {
        Flowable.error(new TestException()).switchMapSingle(Functions.justFunction(Single.never())).test().assertFailure(TestException.class);
    }

    @Test
    public void innerError() {
        Flowable.just(1).switchMapSingle(Functions.justFunction(Single.error(new TestException()))).test().assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.switchMapSingle(Functions.justFunction(Single.never()));
            }
        });
    }

    @Test
    public void limit() {
        Flowable.range(1, 5).switchMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }).take(3).test().assertResult(1, 2, 3);
    }

    @Test
    public void switchOver() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final SingleSubject<Integer> ms1 = SingleSubject.create();
        final SingleSubject<Integer> ms2 = SingleSubject.create();
        TestSubscriber<Integer> ts = pp.switchMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                if (v == 1) {
                    return ms1;
                }
                return ms2;
            }
        }).test();
        ts.assertEmpty();
        pp.onNext(1);
        ts.assertEmpty();
        assertTrue(ms1.hasObservers());
        pp.onNext(2);
        assertFalse(ms1.hasObservers());
        assertTrue(ms2.hasObservers());
        ms2.onError(new TestException());
        assertFalse(pp.hasSubscribers());
        ts.assertFailure(TestException.class);
    }

    @Test
    public void switchOverDelayError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final SingleSubject<Integer> ms1 = SingleSubject.create();
        final SingleSubject<Integer> ms2 = SingleSubject.create();
        TestSubscriber<Integer> ts = pp.switchMapSingleDelayError(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                if (v == 1) {
                    return ms1;
                }
                return ms2;
            }
        }).test();
        ts.assertEmpty();
        pp.onNext(1);
        ts.assertEmpty();
        assertTrue(ms1.hasObservers());
        pp.onNext(2);
        assertFalse(ms1.hasObservers());
        assertTrue(ms2.hasObservers());
        ms2.onError(new TestException());
        ts.assertEmpty();
        assertTrue(pp.hasSubscribers());
        pp.onComplete();
        ts.assertFailure(TestException.class);
    }

    @Test
    public void mainErrorInnerCompleteDelayError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final SingleSubject<Integer> ms = SingleSubject.create();
        TestSubscriber<Integer> ts = pp.switchMapSingleDelayError(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return ms;
            }
        }).test();
        ts.assertEmpty();
        pp.onNext(1);
        ts.assertEmpty();
        assertTrue(ms.hasObservers());
        pp.onError(new TestException());
        assertTrue(ms.hasObservers());
        ts.assertEmpty();
        ms.onSuccess(1);
        ts.assertFailure(TestException.class, 1);
    }

    @Test
    public void mainErrorInnerSuccessDelayError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final SingleSubject<Integer> ms = SingleSubject.create();
        TestSubscriber<Integer> ts = pp.switchMapSingleDelayError(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return ms;
            }
        }).test();
        ts.assertEmpty();
        pp.onNext(1);
        ts.assertEmpty();
        assertTrue(ms.hasObservers());
        pp.onError(new TestException());
        assertTrue(ms.hasObservers());
        ts.assertEmpty();
        ms.onSuccess(1);
        ts.assertFailure(TestException.class, 1);
    }

    @Test
    public void mapperCrash() {
        Flowable.just(1).switchMapSingle(new Function<Integer, SingleSource<? extends Object>>() {

            @Override
            public SingleSource<? extends Object> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void disposeBeforeSwitchInOnNext() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.just(1).switchMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                ts.cancel();
                return Single.just(1);
            }
        }).subscribe(ts);
        ts.assertEmpty();
    }

    @Test
    public void disposeOnNextAfterFirst() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.just(1, 2).switchMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                if (v == 2) {
                    ts.cancel();
                }
                return Single.just(1);
            }
        }).subscribe(ts);
        ts.assertValue(1).assertNoErrors().assertNotComplete();
    }

    @Test
    public void cancel() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final SingleSubject<Integer> ms = SingleSubject.create();
        TestSubscriber<Integer> ts = pp.switchMapSingleDelayError(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return ms;
            }
        }).test();
        ts.assertEmpty();
        pp.onNext(1);
        ts.assertEmpty();
        assertTrue(pp.hasSubscribers());
        assertTrue(ms.hasObservers());
        ts.cancel();
        assertFalse(pp.hasSubscribers());
        assertFalse(ms.hasObservers());
    }

    @Test
    public void mainErrorAfterTermination() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onError(new TestException("outer"));
                }
            }.switchMapSingle(new Function<Integer, SingleSource<Integer>>() {

                @Override
                public SingleSource<Integer> apply(Integer v) throws Exception {
                    return Single.error(new TestException("inner"));
                }
            }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "inner");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "outer");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void innerErrorAfterTermination() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final AtomicReference<SingleObserver<? super Integer>> moRef = new AtomicReference<>();
            TestSubscriberEx<Integer> ts = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onError(new TestException("outer"));
                }
            }.switchMapSingle(new Function<Integer, SingleSource<Integer>>() {

                @Override
                public SingleSource<Integer> apply(Integer v) throws Exception {
                    return new Single<Integer>() {

                        @Override
                        protected void subscribeActual(SingleObserver<? super Integer> observer) {
                            observer.onSubscribe(Disposable.empty());
                            moRef.set(observer);
                        }
                    };
                }
            }).to(TestHelper.<Integer>testConsumer());
            ts.assertFailureAndMessage(TestException.class, "outer");
            moRef.get().onError(new TestException("inner"));
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "inner");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void nextCancelRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final SingleSubject<Integer> ms = SingleSubject.create();
            final TestSubscriber<Integer> ts = pp.switchMapSingleDelayError(new Function<Integer, SingleSource<Integer>>() {

                @Override
                public SingleSource<Integer> apply(Integer v) throws Exception {
                    return ms;
                }
            }).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            TestHelper.race(r1, r2);
            ts.assertNoErrors().assertNotComplete();
        }
    }

    @Test
    public void nextInnerErrorRace() {
        final TestException ex = new TestException();
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp = PublishProcessor.create();
                final SingleSubject<Integer> ms = SingleSubject.create();
                final TestSubscriberEx<Integer> ts = pp.switchMapSingleDelayError(new Function<Integer, SingleSource<Integer>>() {

                    @Override
                    public SingleSource<Integer> apply(Integer v) throws Exception {
                        if (v == 1) {
                            return ms;
                        }
                        return Single.never();
                    }
                }).to(TestHelper.<Integer>testConsumer());
                pp.onNext(1);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp.onNext(2);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ms.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                if (ts.errors().size() != 0) {
                    assertTrue(errors.isEmpty());
                    ts.assertFailure(TestException.class);
                } else if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void mainErrorInnerErrorRace() {
        final TestException ex = new TestException();
        final TestException ex2 = new TestException();
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp = PublishProcessor.create();
                final SingleSubject<Integer> ms = SingleSubject.create();
                final TestSubscriber<Integer> ts = pp.switchMapSingleDelayError(new Function<Integer, SingleSource<Integer>>() {

                    @Override
                    public SingleSource<Integer> apply(Integer v) throws Exception {
                        if (v == 1) {
                            return ms;
                        }
                        return Single.never();
                    }
                }).test();
                pp.onNext(1);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ms.onError(ex2);
                    }
                };
                TestHelper.race(r1, r2);
                ts.assertError(new Predicate<Throwable>() {

                    @Override
                    public boolean test(Throwable e) throws Exception {
                        return e instanceof TestException || e instanceof CompositeException;
                    }
                });
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void nextInnerSuccessRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final SingleSubject<Integer> ms = SingleSubject.create();
            final TestSubscriber<Integer> ts = pp.switchMapSingleDelayError(new Function<Integer, SingleSource<Integer>>() {

                @Override
                public SingleSource<Integer> apply(Integer v) throws Exception {
                    if (v == 1) {
                        return ms;
                    }
                    return Single.never();
                }
            }).test();
            pp.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(2);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ms.onSuccess(3);
                }
            };
            TestHelper.race(r1, r2);
            ts.assertNoErrors().assertNotComplete();
        }
    }

    @Test
    public void requestMoreOnNext() {
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>(1) {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                requestMore(1);
            }
        };
        Flowable.range(1, 5).switchMapSingle(Functions.justFunction(Single.just(1))).subscribe(ts);
        ts.assertResult(1, 1, 1, 1, 1);
    }

    @Test
    public void backpressured() {
        Flowable.just(1).switchMapSingle(Functions.justFunction(Single.just(1))).test(0).assertEmpty().requestMore(1).assertResult(1);
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.switchMapSingle(new Function<Integer, Single<Integer>>() {

                    @Override
                    public Single<Integer> apply(Integer v) throws Throwable {
                        return Single.just(v).hide();
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
                return upstream.switchMapSingleDelayError(new Function<Integer, Single<Integer>>() {

                    @Override
                    public Single<Integer> apply(Integer v) throws Throwable {
                        return Single.just(v).hide();
                    }
                });
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableSwitchMapSingleTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.payloads.simple.evaluate();
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
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_limit() throws java.lang.Throwable {
            this.payloads.limit.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchOver() throws java.lang.Throwable {
            this.payloads.switchOver.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchOverDelayError() throws java.lang.Throwable {
            this.payloads.switchOverDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorInnerCompleteDelayError() throws java.lang.Throwable {
            this.payloads.mainErrorInnerCompleteDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorInnerSuccessDelayError() throws java.lang.Throwable {
            this.payloads.mainErrorInnerSuccessDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperCrash() throws java.lang.Throwable {
            this.payloads.mapperCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeBeforeSwitchInOnNext() throws java.lang.Throwable {
            this.payloads.disposeBeforeSwitchInOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeOnNextAfterFirst() throws java.lang.Throwable {
            this.payloads.disposeOnNextAfterFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorAfterTermination() throws java.lang.Throwable {
            this.payloads.mainErrorAfterTermination.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorAfterTermination() throws java.lang.Throwable {
            this.payloads.innerErrorAfterTermination.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextCancelRace() throws java.lang.Throwable {
            this.payloads.nextCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextInnerErrorRace() throws java.lang.Throwable {
            this.payloads.nextInnerErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorInnerErrorRace() throws java.lang.Throwable {
            this.payloads.mainErrorInnerErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextInnerSuccessRace() throws java.lang.Throwable {
            this.payloads.nextInnerSuccessRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestMoreOnNext() throws java.lang.Throwable {
            this.payloads.requestMoreOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressured() throws java.lang.Throwable {
            this.payloads.backpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelDelayError() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancelDelayError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSwitchMapSingleTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSwitchMapSingleTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSwitchMapSingleTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSwitchMapSingleTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableSwitchMapSingleTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSwitchMapSingleTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableSwitchMapSingleTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableSwitchMapSingleTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement simple;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement innerError;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement limit;

            public org.junit.runners.model.Statement switchOver;

            public org.junit.runners.model.Statement switchOverDelayError;

            public org.junit.runners.model.Statement mainErrorInnerCompleteDelayError;

            public org.junit.runners.model.Statement mainErrorInnerSuccessDelayError;

            public org.junit.runners.model.Statement mapperCrash;

            public org.junit.runners.model.Statement disposeBeforeSwitchInOnNext;

            public org.junit.runners.model.Statement disposeOnNextAfterFirst;

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement mainErrorAfterTermination;

            public org.junit.runners.model.Statement innerErrorAfterTermination;

            public org.junit.runners.model.Statement nextCancelRace;

            public org.junit.runners.model.Statement nextInnerErrorRace;

            public org.junit.runners.model.Statement mainErrorInnerErrorRace;

            public org.junit.runners.model.Statement nextInnerSuccessRace;

            public org.junit.runners.model.Statement requestMoreOnNext;

            public org.junit.runners.model.Statement backpressured;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.simple = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::simple, "simple", this);
            this.payloads.mainError = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::mainError, "mainError", this);
            this.payloads.innerError = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::innerError, "innerError", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.limit = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::limit, "limit", this);
            this.payloads.switchOver = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::switchOver, "switchOver", this);
            this.payloads.switchOverDelayError = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::switchOverDelayError, "switchOverDelayError", this);
            this.payloads.mainErrorInnerCompleteDelayError = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::mainErrorInnerCompleteDelayError, "mainErrorInnerCompleteDelayError", this);
            this.payloads.mainErrorInnerSuccessDelayError = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::mainErrorInnerSuccessDelayError, "mainErrorInnerSuccessDelayError", this);
            this.payloads.mapperCrash = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::mapperCrash, "mapperCrash", this);
            this.payloads.disposeBeforeSwitchInOnNext = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::disposeBeforeSwitchInOnNext, "disposeBeforeSwitchInOnNext", this);
            this.payloads.disposeOnNextAfterFirst = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::disposeOnNextAfterFirst, "disposeOnNextAfterFirst", this);
            this.payloads.cancel = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::cancel, "cancel", this);
            this.payloads.mainErrorAfterTermination = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::mainErrorAfterTermination, "mainErrorAfterTermination", this);
            this.payloads.innerErrorAfterTermination = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::innerErrorAfterTermination, "innerErrorAfterTermination", this);
            this.payloads.nextCancelRace = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::nextCancelRace, "nextCancelRace", this);
            this.payloads.nextInnerErrorRace = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::nextInnerErrorRace, "nextInnerErrorRace", this);
            this.payloads.mainErrorInnerErrorRace = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::mainErrorInnerErrorRace, "mainErrorInnerErrorRace", this);
            this.payloads.nextInnerSuccessRace = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::nextInnerSuccessRace, "nextInnerSuccessRace", this);
            this.payloads.requestMoreOnNext = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::requestMoreOnNext, "requestMoreOnNext", this);
            this.payloads.backpressured = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::backpressured, "backpressured", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(FlowableSwitchMapSingleTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
        }
    }
}
