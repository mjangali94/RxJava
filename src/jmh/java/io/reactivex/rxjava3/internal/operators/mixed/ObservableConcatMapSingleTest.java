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
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapSingle.ConcatMapSingleMainObserver;
import io.reactivex.rxjava3.internal.util.ErrorMode;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableConcatMapSingleTest extends RxJavaTest {

    @Test
    public void simple() {
        Observable.range(1, 5).concatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void simpleLong() {
        Observable.range(1, 1024).concatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }, 32).test().assertValueCount(1024).assertNoErrors().assertComplete();
    }

    @Test
    public void mainError() {
        Observable.error(new TestException()).concatMapSingle(Functions.justFunction(Single.just(1))).test().assertFailure(TestException.class);
    }

    @Test
    public void innerError() {
        Observable.just(1).concatMapSingle(Functions.justFunction(Single.error(new TestException()))).test().assertFailure(TestException.class);
    }

    @Test
    public void mainBoundaryErrorInnerSuccess() {
        PublishSubject<Integer> ps = PublishSubject.create();
        SingleSubject<Integer> ms = SingleSubject.create();
        TestObserver<Integer> to = ps.concatMapSingleDelayError(Functions.justFunction(ms), false).test();
        to.assertEmpty();
        ps.onNext(1);
        assertTrue(ms.hasObservers());
        ps.onError(new TestException());
        assertTrue(ms.hasObservers());
        to.assertEmpty();
        ms.onSuccess(1);
        to.assertFailure(TestException.class, 1);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> f) throws Exception {
                return f.concatMapSingleDelayError(Functions.justFunction(Single.never()));
            }
        });
    }

    @Test
    public void take() {
        Observable.range(1, 5).concatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }).take(3).test().assertResult(1, 2, 3);
    }

    @Test
    public void cancel() {
        Observable.range(1, 5).concatWith(Observable.<Integer>never()).concatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }).test().assertValues(1, 2, 3, 4, 5).assertNoErrors().assertNotComplete().dispose();
    }

    @Test
    public void mainErrorAfterInnerError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onError(new TestException("outer"));
                }
            }.concatMapSingle(Functions.justFunction(Single.error(new TestException("inner"))), 1).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(TestException.class, "inner");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "outer");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void innerErrorAfterMainError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final AtomicReference<SingleObserver<? super Integer>> obs = new AtomicReference<>();
            TestObserverEx<Integer> to = ps.concatMapSingle(new Function<Integer, SingleSource<Integer>>() {

                @Override
                public SingleSource<Integer> apply(Integer v) throws Exception {
                    return new Single<Integer>() {

                        @Override
                        protected void subscribeActual(SingleObserver<? super Integer> observer) {
                            observer.onSubscribe(Disposable.empty());
                            obs.set(observer);
                        }
                    };
                }
            }).to(TestHelper.<Integer>testConsumer());
            ps.onNext(1);
            ps.onError(new TestException("outer"));
            obs.get().onError(new TestException("inner"));
            to.assertFailureAndMessage(TestException.class, "outer");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "inner");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void delayAllErrors() {
        TestObserverEx<Object> to = Observable.range(1, 5).concatMapSingleDelayError(new Function<Integer, SingleSource<? extends Object>>() {

            @Override
            public SingleSource<? extends Object> apply(Integer v) throws Exception {
                return Single.error(new TestException());
            }
        }).to(TestHelper.<Object>testConsumer()).assertFailure(CompositeException.class);
        CompositeException ce = (CompositeException) to.errors().get(0);
        assertEquals(5, ce.getExceptions().size());
    }

    @Test
    public void mapperCrash() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Object> to = ps.concatMapSingle(new Function<Integer, SingleSource<? extends Object>>() {

            @Override
            public SingleSource<? extends Object> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test();
        to.assertEmpty();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertFailure(TestException.class);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void mapperCrashScalar() {
        TestObserver<Object> to = Observable.just(1).concatMapSingle(new Function<Integer, SingleSource<? extends Object>>() {

            @Override
            public SingleSource<? extends Object> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test();
        to.assertFailure(TestException.class);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Observable.just(1).hide().concatMapSingle(Functions.justFunction(Single.never())));
    }

    @Test
    public void mainCompletesWhileInnerActive() {
        PublishSubject<Integer> ps = PublishSubject.create();
        SingleSubject<Integer> ms = SingleSubject.create();
        TestObserver<Integer> to = ps.concatMapSingleDelayError(Functions.justFunction(ms), false).test();
        to.assertEmpty();
        ps.onNext(1);
        ps.onNext(2);
        ps.onComplete();
        assertTrue(ms.hasObservers());
        to.assertEmpty();
        ms.onSuccess(1);
        to.assertResult(1, 1);
    }

    @Test
    public void scalarEmptySource() {
        SingleSubject<Integer> ss = SingleSubject.create();
        Observable.empty().concatMapSingle(Functions.justFunction(ss)).test().assertResult();
        assertFalse(ss.hasObservers());
    }

    @Test
    public void cancelNoConcurrentClean() {
        TestObserver<Integer> to = new TestObserver<>();
        ConcatMapSingleMainObserver<Integer, Integer> operator = new ConcatMapSingleMainObserver<>(to, Functions.justFunction(Single.<Integer>never()), 16, ErrorMode.IMMEDIATE);
        operator.onSubscribe(Disposable.empty());
        operator.queue.offer(1);
        operator.getAndIncrement();
        to.dispose();
        assertFalse(operator.queue.isEmpty());
        operator.addAndGet(-2);
        operator.dispose();
        assertTrue(operator.queue.isEmpty());
    }

    @Test
    public void checkUnboundedInnerQueue() {
        SingleSubject<Integer> ss = SingleSubject.create();
        TestObserver<Integer> to = Observable.fromArray(ss, Single.just(2), Single.just(3), Single.just(4)).concatMapSingle(Functions.<Single<Integer>>identity(), 2).test();
        to.assertEmpty();
        ss.onSuccess(1);
        to.assertResult(1, 2, 3, 4);
    }

    @Test
    public void innerSuccessDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final SingleSubject<Integer> ss = SingleSubject.create();
            final TestObserver<Integer> to = Observable.just(1).hide().concatMapSingle(Functions.justFunction(ss)).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ss.onSuccess(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
            to.assertNoErrors();
        }
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> upstream) {
                return upstream.concatMapSingle(new Function<Integer, Single<Integer>>() {

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
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> upstream) {
                return upstream.concatMapSingleDelayError(new Function<Integer, Single<Integer>>() {

                    @Override
                    public Single<Integer> apply(Integer v) throws Throwable {
                        return Single.just(v).hide();
                    }
                }, false, 2);
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayErrorTillEnd() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> upstream) {
                return upstream.concatMapSingleDelayError(new Function<Integer, Single<Integer>>() {

                    @Override
                    public Single<Integer> apply(Integer v) throws Throwable {
                        return Single.just(v).hide();
                    }
                }, true, 2);
            }
        });
    }

    @Test
    public void basicNonFused() {
        Observable.range(1, 5).hide().concatMapSingle(v -> Single.just(v).hide()).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void basicSyncFused() {
        Observable.range(1, 5).concatMapSingle(v -> Single.just(v).hide()).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void basicAsyncFused() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.concatMapSingle(v -> Single.just(v).hide()).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void basicFusionRejected() {
        TestHelper.<Integer>rejectObservableFusion().concatMapSingle(v -> Single.just(v).hide()).test().assertEmpty();
    }

    @Test
    public void fusedPollCrash() {
        Observable.range(1, 5).map(v -> {
            if (v == 3) {
                throw new TestException();
            }
            return v;
        }).compose(TestHelper.observableStripBoundary()).concatMapSingle(v -> Single.just(v).hide()).test().assertFailure(TestException.class, 1, 2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableConcatMapSingleTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.payloads.simple.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleLong() throws java.lang.Throwable {
            this.payloads.simpleLong.evaluate();
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
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
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
        public void benchmark_mainErrorAfterInnerError() throws java.lang.Throwable {
            this.payloads.mainErrorAfterInnerError.evaluate();
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
        public void benchmark_mapperCrashScalar() throws java.lang.Throwable {
            this.payloads.mapperCrashScalar.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompletesWhileInnerActive() throws java.lang.Throwable {
            this.payloads.mainCompletesWhileInnerActive.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarEmptySource() throws java.lang.Throwable {
            this.payloads.scalarEmptySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelNoConcurrentClean() throws java.lang.Throwable {
            this.payloads.cancelNoConcurrentClean.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkUnboundedInnerQueue() throws java.lang.Throwable {
            this.payloads.checkUnboundedInnerQueue.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapSingleTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapSingleTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapSingleTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapSingleTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableConcatMapSingleTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapSingleTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableConcatMapSingleTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableConcatMapSingleTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement simple;

            public org.junit.runners.model.Statement simpleLong;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement innerError;

            public org.junit.runners.model.Statement mainBoundaryErrorInnerSuccess;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement mainErrorAfterInnerError;

            public org.junit.runners.model.Statement innerErrorAfterMainError;

            public org.junit.runners.model.Statement delayAllErrors;

            public org.junit.runners.model.Statement mapperCrash;

            public org.junit.runners.model.Statement mapperCrashScalar;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement mainCompletesWhileInnerActive;

            public org.junit.runners.model.Statement scalarEmptySource;

            public org.junit.runners.model.Statement cancelNoConcurrentClean;

            public org.junit.runners.model.Statement checkUnboundedInnerQueue;

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
            this.payloads.simple = _ClassStatement.forPayload(ObservableConcatMapSingleTest::simple, "simple", this);
            this.payloads.simpleLong = _ClassStatement.forPayload(ObservableConcatMapSingleTest::simpleLong, "simpleLong", this);
            this.payloads.mainError = _ClassStatement.forPayload(ObservableConcatMapSingleTest::mainError, "mainError", this);
            this.payloads.innerError = _ClassStatement.forPayload(ObservableConcatMapSingleTest::innerError, "innerError", this);
            this.payloads.mainBoundaryErrorInnerSuccess = _ClassStatement.forPayload(ObservableConcatMapSingleTest::mainBoundaryErrorInnerSuccess, "mainBoundaryErrorInnerSuccess", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableConcatMapSingleTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.take = _ClassStatement.forPayload(ObservableConcatMapSingleTest::take, "take", this);
            this.payloads.cancel = _ClassStatement.forPayload(ObservableConcatMapSingleTest::cancel, "cancel", this);
            this.payloads.mainErrorAfterInnerError = _ClassStatement.forPayload(ObservableConcatMapSingleTest::mainErrorAfterInnerError, "mainErrorAfterInnerError", this);
            this.payloads.innerErrorAfterMainError = _ClassStatement.forPayload(ObservableConcatMapSingleTest::innerErrorAfterMainError, "innerErrorAfterMainError", this);
            this.payloads.delayAllErrors = _ClassStatement.forPayload(ObservableConcatMapSingleTest::delayAllErrors, "delayAllErrors", this);
            this.payloads.mapperCrash = _ClassStatement.forPayload(ObservableConcatMapSingleTest::mapperCrash, "mapperCrash", this);
            this.payloads.mapperCrashScalar = _ClassStatement.forPayload(ObservableConcatMapSingleTest::mapperCrashScalar, "mapperCrashScalar", this);
            this.payloads.disposed = _ClassStatement.forPayload(ObservableConcatMapSingleTest::disposed, "disposed", this);
            this.payloads.mainCompletesWhileInnerActive = _ClassStatement.forPayload(ObservableConcatMapSingleTest::mainCompletesWhileInnerActive, "mainCompletesWhileInnerActive", this);
            this.payloads.scalarEmptySource = _ClassStatement.forPayload(ObservableConcatMapSingleTest::scalarEmptySource, "scalarEmptySource", this);
            this.payloads.cancelNoConcurrentClean = _ClassStatement.forPayload(ObservableConcatMapSingleTest::cancelNoConcurrentClean, "cancelNoConcurrentClean", this);
            this.payloads.checkUnboundedInnerQueue = _ClassStatement.forPayload(ObservableConcatMapSingleTest::checkUnboundedInnerQueue, "checkUnboundedInnerQueue", this);
            this.payloads.innerSuccessDisposeRace = _ClassStatement.forPayload(ObservableConcatMapSingleTest::innerSuccessDisposeRace, "innerSuccessDisposeRace", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(ObservableConcatMapSingleTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(ObservableConcatMapSingleTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.undeliverableUponCancelDelayErrorTillEnd = _ClassStatement.forPayload(ObservableConcatMapSingleTest::undeliverableUponCancelDelayErrorTillEnd, "undeliverableUponCancelDelayErrorTillEnd", this);
            this.payloads.basicNonFused = _ClassStatement.forPayload(ObservableConcatMapSingleTest::basicNonFused, "basicNonFused", this);
            this.payloads.basicSyncFused = _ClassStatement.forPayload(ObservableConcatMapSingleTest::basicSyncFused, "basicSyncFused", this);
            this.payloads.basicAsyncFused = _ClassStatement.forPayload(ObservableConcatMapSingleTest::basicAsyncFused, "basicAsyncFused", this);
            this.payloads.basicFusionRejected = _ClassStatement.forPayload(ObservableConcatMapSingleTest::basicFusionRejected, "basicFusionRejected", this);
            this.payloads.fusedPollCrash = _ClassStatement.forPayload(ObservableConcatMapSingleTest::fusedPollCrash, "fusedPollCrash", this);
        }
    }
}
