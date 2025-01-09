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
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.mixed.ObservableConcatMapMaybe.ConcatMapMaybeMainObserver;
import io.reactivex.rxjava3.internal.util.ErrorMode;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableConcatMapMaybeTest extends RxJavaTest {

    @Test
    public void simple() {
        Observable.range(1, 5).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void simpleLong() {
        Observable.range(1, 1024).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }, 32).test().assertValueCount(1024).assertNoErrors().assertComplete();
    }

    @Test
    public void empty() {
        Observable.range(1, 10).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.empty();
            }
        }).test().assertResult();
    }

    @Test
    public void mixed() {
        Observable.range(1, 10).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

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
        TestObserverEx<Integer> to = Observable.range(1, 1024).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                if (v % 2 == 0) {
                    return Maybe.just(v).subscribeOn(Schedulers.computation());
                }
                return Maybe.<Integer>empty().subscribeOn(Schedulers.computation());
            }
        }).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertValueCount(512).assertNoErrors().assertComplete();
        for (int i = 0; i < 512; i++) {
            to.assertValueAt(i, (i + 1) * 2);
        }
    }

    @Test
    public void mainError() {
        Observable.error(new TestException()).concatMapMaybe(Functions.justFunction(Maybe.just(1))).test().assertFailure(TestException.class);
    }

    @Test
    public void innerError() {
        Observable.just(1).concatMapMaybe(Functions.justFunction(Maybe.error(new TestException()))).test().assertFailure(TestException.class);
    }

    @Test
    public void mainBoundaryErrorInnerSuccess() {
        PublishSubject<Integer> ps = PublishSubject.create();
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestObserver<Integer> to = ps.concatMapMaybeDelayError(Functions.justFunction(ms), false).test();
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
    public void mainBoundaryErrorInnerEmpty() {
        PublishSubject<Integer> ps = PublishSubject.create();
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestObserver<Integer> to = ps.concatMapMaybeDelayError(Functions.justFunction(ms), false).test();
        to.assertEmpty();
        ps.onNext(1);
        assertTrue(ms.hasObservers());
        ps.onError(new TestException());
        assertTrue(ms.hasObservers());
        to.assertEmpty();
        ms.onComplete();
        to.assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> f) throws Exception {
                return f.concatMapMaybeDelayError(Functions.justFunction(Maybe.empty()));
            }
        });
    }

    @Test
    public void take() {
        Observable.range(1, 5).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }).take(3).test().assertResult(1, 2, 3);
    }

    @Test
    public void cancel() {
        Observable.range(1, 5).concatWith(Observable.<Integer>never()).concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
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
            }.concatMapMaybe(Functions.justFunction(Maybe.error(new TestException("inner"))), 1).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(TestException.class, "inner");
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
            final AtomicReference<MaybeObserver<? super Integer>> obs = new AtomicReference<>();
            TestObserverEx<Integer> to = ps.concatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

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
        TestObserverEx<Object> to = Observable.range(1, 5).concatMapMaybeDelayError(new Function<Integer, MaybeSource<? extends Object>>() {

            @Override
            public MaybeSource<? extends Object> apply(Integer v) throws Exception {
                return Maybe.error(new TestException());
            }
        }).to(TestHelper.<Object>testConsumer()).assertFailure(CompositeException.class);
        CompositeException ce = (CompositeException) to.errors().get(0);
        assertEquals(5, ce.getExceptions().size());
    }

    @Test
    public void mapperCrash() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Object> to = ps.concatMapMaybe(new Function<Integer, MaybeSource<? extends Object>>() {

            @Override
            public MaybeSource<? extends Object> apply(Integer v) throws Exception {
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
    public void scalarMapperCrash() {
        TestObserver<Object> to = Observable.just(1).concatMapMaybe(new Function<Integer, MaybeSource<? extends Object>>() {

            @Override
            public MaybeSource<? extends Object> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test();
        to.assertFailure(TestException.class);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Observable.just(1).hide().concatMapMaybe(Functions.justFunction(Maybe.never())));
    }

    @Test
    public void scalarEmptySource() {
        MaybeSubject<Integer> ms = MaybeSubject.create();
        Observable.empty().concatMapMaybe(Functions.justFunction(ms)).test().assertResult();
        assertFalse(ms.hasObservers());
    }

    @Test
    public void cancelNoConcurrentClean() {
        TestObserver<Integer> to = new TestObserver<>();
        ConcatMapMaybeMainObserver<Integer, Integer> operator = new ConcatMapMaybeMainObserver<>(to, Functions.justFunction(Maybe.<Integer>never()), 16, ErrorMode.IMMEDIATE);
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
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestObserver<Integer> to = Observable.fromArray(ms, Maybe.just(2), Maybe.just(3), Maybe.just(4)).concatMapMaybe(Functions.<Maybe<Integer>>identity(), 2).test();
        to.assertEmpty();
        ms.onSuccess(1);
        to.assertResult(1, 2, 3, 4);
    }

    @Test
    public void innerSuccessDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final MaybeSubject<Integer> ms = MaybeSubject.create();
            final TestObserver<Integer> to = Observable.just(1).hide().concatMapMaybe(Functions.justFunction(ms)).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ms.onSuccess(1);
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
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> upstream) {
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
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> upstream) {
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
        Observable.range(1, 5).hide().concatMapMaybe(v -> Maybe.just(v).hide()).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void basicSyncFused() {
        Observable.range(1, 5).concatMapMaybe(v -> Maybe.just(v).hide()).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void basicAsyncFused() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.concatMapMaybe(v -> Maybe.just(v).hide()).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void basicFusionRejected() {
        TestHelper.<Integer>rejectObservableFusion().concatMapMaybe(v -> Maybe.just(v).hide()).test().assertEmpty();
    }

    @Test
    public void fusedPollCrash() {
        Observable.range(1, 5).map(v -> {
            if (v == 3) {
                throw new TestException();
            }
            return v;
        }).compose(TestHelper.observableStripBoundary()).concatMapMaybe(v -> Maybe.just(v).hide()).test().assertFailure(TestException.class, 1, 2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableConcatMapMaybeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.payloads.simple.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleLong() throws java.lang.Throwable {
            this.payloads.simpleLong.evaluate();
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
        public void benchmark_scalarMapperCrash() throws java.lang.Throwable {
            this.payloads.scalarMapperCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapMaybeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapMaybeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapMaybeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapMaybeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableConcatMapMaybeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapMaybeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableConcatMapMaybeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableConcatMapMaybeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement simple;

            public org.junit.runners.model.Statement simpleLong;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement mixed;

            public org.junit.runners.model.Statement mixedLong;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement innerError;

            public org.junit.runners.model.Statement mainBoundaryErrorInnerSuccess;

            public org.junit.runners.model.Statement mainBoundaryErrorInnerEmpty;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement mainErrorAfterInnerError;

            public org.junit.runners.model.Statement innerErrorAfterMainError;

            public org.junit.runners.model.Statement delayAllErrors;

            public org.junit.runners.model.Statement mapperCrash;

            public org.junit.runners.model.Statement scalarMapperCrash;

            public org.junit.runners.model.Statement disposed;

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
            this.payloads.simple = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::simple, "simple", this);
            this.payloads.simpleLong = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::simpleLong, "simpleLong", this);
            this.payloads.empty = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::empty, "empty", this);
            this.payloads.mixed = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::mixed, "mixed", this);
            this.payloads.mixedLong = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::mixedLong, "mixedLong", this);
            this.payloads.mainError = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::mainError, "mainError", this);
            this.payloads.innerError = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::innerError, "innerError", this);
            this.payloads.mainBoundaryErrorInnerSuccess = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::mainBoundaryErrorInnerSuccess, "mainBoundaryErrorInnerSuccess", this);
            this.payloads.mainBoundaryErrorInnerEmpty = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::mainBoundaryErrorInnerEmpty, "mainBoundaryErrorInnerEmpty", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.take = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::take, "take", this);
            this.payloads.cancel = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::cancel, "cancel", this);
            this.payloads.mainErrorAfterInnerError = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::mainErrorAfterInnerError, "mainErrorAfterInnerError", this);
            this.payloads.innerErrorAfterMainError = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::innerErrorAfterMainError, "innerErrorAfterMainError", this);
            this.payloads.delayAllErrors = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::delayAllErrors, "delayAllErrors", this);
            this.payloads.mapperCrash = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::mapperCrash, "mapperCrash", this);
            this.payloads.scalarMapperCrash = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::scalarMapperCrash, "scalarMapperCrash", this);
            this.payloads.disposed = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::disposed, "disposed", this);
            this.payloads.scalarEmptySource = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::scalarEmptySource, "scalarEmptySource", this);
            this.payloads.cancelNoConcurrentClean = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::cancelNoConcurrentClean, "cancelNoConcurrentClean", this);
            this.payloads.checkUnboundedInnerQueue = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::checkUnboundedInnerQueue, "checkUnboundedInnerQueue", this);
            this.payloads.innerSuccessDisposeRace = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::innerSuccessDisposeRace, "innerSuccessDisposeRace", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.undeliverableUponCancelDelayErrorTillEnd = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::undeliverableUponCancelDelayErrorTillEnd, "undeliverableUponCancelDelayErrorTillEnd", this);
            this.payloads.basicNonFused = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::basicNonFused, "basicNonFused", this);
            this.payloads.basicSyncFused = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::basicSyncFused, "basicSyncFused", this);
            this.payloads.basicAsyncFused = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::basicAsyncFused, "basicAsyncFused", this);
            this.payloads.basicFusionRejected = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::basicFusionRejected, "basicFusionRejected", this);
            this.payloads.fusedPollCrash = _ClassStatement.forPayload(ObservableConcatMapMaybeTest::fusedPollCrash, "fusedPollCrash", this);
        }
    }
}
