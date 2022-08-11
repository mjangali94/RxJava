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

import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.observable.ObservableConcatMapSchedulerTest.EmptyDisposingObservable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableConcatMapTest extends RxJavaTest {

    @Test
    public void asyncFused() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestObserver<Integer> to = us.concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 2);
            }
        }).test();
        us.onNext(1);
        us.onComplete();
        to.assertResult(1, 2);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.<Integer>just(1).hide().concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.error(new TestException());
            }
        }));
    }

    @Test
    public void dispose2() {
        TestHelper.checkDisposed(Observable.<Integer>just(1).hide().concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.error(new TestException());
            }
        }));
    }

    @Test
    public void mainError() {
        Observable.<Integer>error(new TestException()).concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 2);
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void innerError() {
        Observable.<Integer>just(1).hide().concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void mainErrorDelayed() {
        Observable.<Integer>error(new TestException()).concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 2);
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void innerErrorDelayError() {
        Observable.<Integer>just(1).hide().concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void innerErrorDelayError2() {
        Observable.<Integer>just(1).hide().concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.fromCallable(new Callable<Integer>() {

                    @Override
                    public Integer call() throws Exception {
                        throw new TestException();
                    }
                });
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onComplete();
                    observer.onNext(2);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.concatMap(new Function<Integer, ObservableSource<Integer>>() {

                @Override
                public ObservableSource<Integer> apply(Integer v) throws Exception {
                    return Observable.range(v, 2);
                }
            }).test().assertResult(1, 2);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSourceDelayError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onComplete();
                    observer.onNext(2);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

                @Override
                public ObservableSource<Integer> apply(Integer v) throws Exception {
                    return Observable.range(v, 2);
                }
            }).test().assertResult(1, 2);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void normalDelayErrors() {
        Observable.just(1).hide().concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 2);
            }
        }).test().assertResult(1, 2);
    }

    @Test
    public void normalDelayErrorsTillTheEnd() {
        Observable.just(1).hide().concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 2);
            }
        }, true, 16).test().assertResult(1, 2);
    }

    @Test
    public void onErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> ps1 = PublishSubject.create();
                final PublishSubject<Integer> ps2 = PublishSubject.create();
                TestObserver<Integer> to = ps1.concatMap(new Function<Integer, ObservableSource<Integer>>() {

                    @Override
                    public ObservableSource<Integer> apply(Integer v) throws Exception {
                        return ps2;
                    }
                }).test();
                final TestException ex1 = new TestException();
                final TestException ex2 = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps1.onError(ex1);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ps2.onError(ex2);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertFailure(TestException.class);
                if (!errors.isEmpty()) {
                    TestHelper.assertError(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void mapperThrows() {
        Observable.just(1).hide().concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void fusedPollThrows() {
        Observable.just(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 2);
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void fusedPollThrowsDelayError() {
        Observable.just(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.range(v, 2);
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void mapperThrowsDelayError() {
        Observable.just(1).hide().concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void badInnerDelayError() {
        @SuppressWarnings("rawtypes")
        final Observer[] o = { null };
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.just(1).hide().concatMapDelayError(new Function<Integer, ObservableSource<Integer>>() {

                @Override
                public ObservableSource<Integer> apply(Integer v) throws Exception {
                    return new Observable<Integer>() {

                        @Override
                        protected void subscribeActual(Observer<? super Integer> observer) {
                            o[0] = observer;
                            observer.onSubscribe(Disposable.empty());
                            observer.onComplete();
                        }
                    };
                }
            }).test().assertResult();
            o[0].onError(new TestException());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void concatReportsDisposedOnComplete() {
        final Disposable[] disposable = { null };
        Observable.fromArray(Observable.just(1), Observable.just(2)).hide().concatMap(Functions.<Observable<Integer>>identity()).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                disposable[0] = d;
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
        assertTrue(disposable[0].isDisposed());
    }

    @Test
    public void concatReportsDisposedOnError() {
        final Disposable[] disposable = { null };
        Observable.fromArray(Observable.just(1), Observable.<Integer>error(new TestException())).hide().concatMap(Functions.<Observable<Integer>>identity()).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                disposable[0] = d;
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
        assertTrue(disposable[0].isDisposed());
    }

    @Test
    public void reentrantNoOverflow() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final PublishSubject<Integer> ps = PublishSubject.create();
            TestObserver<Integer> to = ps.concatMap(new Function<Integer, Observable<Integer>>() {

                @Override
                public Observable<Integer> apply(Integer v) throws Exception {
                    return Observable.just(v + 1);
                }
            }, 1).subscribeWith(new TestObserver<Integer>() {

                @Override
                public void onNext(Integer t) {
                    super.onNext(t);
                    if (t == 1) {
                        for (int i = 1; i < 10; i++) {
                            ps.onNext(i);
                        }
                        ps.onComplete();
                    }
                }
            });
            ps.onNext(0);
            if (!errors.isEmpty()) {
                to.onError(new CompositeException(errors));
            }
            to.assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void reentrantNoOverflowHidden() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.concatMap(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer v) throws Exception {
                return Observable.just(v + 1).hide();
            }
        }, 1).subscribeWith(new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    for (int i = 1; i < 10; i++) {
                        ps.onNext(i);
                    }
                    ps.onComplete();
                }
            }
        });
        ps.onNext(0);
        to.assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void noCancelPrevious() {
        final AtomicInteger counter = new AtomicInteger();
        Observable.range(1, 5).concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.just(v).doOnDispose(new Action() {

                    @Override
                    public void run() throws Exception {
                        counter.getAndIncrement();
                    }
                });
            }
        }).test().assertResult(1, 2, 3, 4, 5);
        assertEquals(0, counter.get());
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> upstream) {
                return upstream.concatMap(new Function<Integer, Observable<Integer>>() {

                    @Override
                    public Observable<Integer> apply(Integer v) throws Throwable {
                        return Observable.just(v).hide();
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
                return upstream.concatMapDelayError(new Function<Integer, Observable<Integer>>() {

                    @Override
                    public Observable<Integer> apply(Integer v) throws Throwable {
                        return Observable.just(v).hide();
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
                return upstream.concatMapDelayError(new Function<Integer, Observable<Integer>>() {

                    @Override
                    public Observable<Integer> apply(Integer v) throws Throwable {
                        return Observable.just(v).hide();
                    }
                }, true, 2);
            }
        });
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(o -> o.concatMap(v -> Observable.never()));
    }

    @Test
    public void doubleOnSubscribeDelayError() {
        TestHelper.checkDoubleOnSubscribeObservable(o -> o.concatMapDelayError(v -> Observable.never()));
    }

    @Test
    public void scalarXMap() {
        Observable.fromCallable(() -> 1).concatMap(v -> Observable.just(2).hide()).test().assertResult(2);
    }

    @Test
    public void rejectedFusion() {
        TestHelper.rejectObservableFusion().concatMap(v -> Observable.never()).test();
    }

    @Test
    public void rejectedFusionDelayError() {
        TestHelper.rejectObservableFusion().concatMapDelayError(v -> Observable.never()).test();
    }

    @Test
    public void asyncFusedDelayError() {
        UnicastSubject<Integer> uc = UnicastSubject.create();
        TestObserver<Integer> to = uc.concatMapDelayError(v -> Observable.just(v).hide()).test();
        uc.onNext(1);
        uc.onComplete();
        to.assertResult(1);
    }

    @Test
    public void scalarInnerJustDelayError() {
        Observable.just(1).hide().concatMapDelayError(v -> Observable.just(v)).test().assertResult(1);
    }

    @Test
    public void scalarInnerEmptyDelayError() {
        Observable.just(1).hide().concatMapDelayError(v -> Observable.empty()).test().assertResult();
    }

    @Test
    public void scalarInnerJustDisposeDelayError() {
        TestObserver<Integer> to = new TestObserver<>();
        Observable.just(1).hide().concatMapDelayError(v -> Observable.fromCallable(() -> {
            to.dispose();
            return 1;
        })).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void scalarInnerEmptyDisposeDelayError() {
        TestObserver<Object> to = new TestObserver<>();
        Observable.just(1).hide().concatMapDelayError(v -> new EmptyDisposingObservable(to)).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void delayErrorInnerActive() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = Observable.range(1, 5).hide().concatMapDelayError(v -> ps).test();
        ps.onComplete();
        to.assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableConcatMapTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFused() throws java.lang.Throwable {
            this.payloads.asyncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose2() throws java.lang.Throwable {
            this.payloads.dispose2.evaluate();
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
        public void benchmark_mainErrorDelayed() throws java.lang.Throwable {
            this.payloads.mainErrorDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorDelayError() throws java.lang.Throwable {
            this.payloads.innerErrorDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorDelayError2() throws java.lang.Throwable {
            this.payloads.innerErrorDelayError2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceDelayError() throws java.lang.Throwable {
            this.payloads.badSourceDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayErrors() throws java.lang.Throwable {
            this.payloads.normalDelayErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayErrorsTillTheEnd() throws java.lang.Throwable {
            this.payloads.normalDelayErrorsTillTheEnd.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorRace() throws java.lang.Throwable {
            this.payloads.onErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrows() throws java.lang.Throwable {
            this.payloads.mapperThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPollThrows() throws java.lang.Throwable {
            this.payloads.fusedPollThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPollThrowsDelayError() throws java.lang.Throwable {
            this.payloads.fusedPollThrowsDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrowsDelayError() throws java.lang.Throwable {
            this.payloads.mapperThrowsDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badInnerDelayError() throws java.lang.Throwable {
            this.payloads.badInnerDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatReportsDisposedOnComplete() throws java.lang.Throwable {
            this.payloads.concatReportsDisposedOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatReportsDisposedOnError() throws java.lang.Throwable {
            this.payloads.concatReportsDisposedOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantNoOverflow() throws java.lang.Throwable {
            this.payloads.reentrantNoOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantNoOverflowHidden() throws java.lang.Throwable {
            this.payloads.reentrantNoOverflowHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPrevious() throws java.lang.Throwable {
            this.payloads.noCancelPrevious.evaluate();
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
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeDelayError() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribeDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarXMap() throws java.lang.Throwable {
            this.payloads.scalarXMap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rejectedFusion() throws java.lang.Throwable {
            this.payloads.rejectedFusion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rejectedFusionDelayError() throws java.lang.Throwable {
            this.payloads.rejectedFusionDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedDelayError() throws java.lang.Throwable {
            this.payloads.asyncFusedDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarInnerJustDelayError() throws java.lang.Throwable {
            this.payloads.scalarInnerJustDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarInnerEmptyDelayError() throws java.lang.Throwable {
            this.payloads.scalarInnerEmptyDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarInnerJustDisposeDelayError() throws java.lang.Throwable {
            this.payloads.scalarInnerJustDisposeDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarInnerEmptyDisposeDelayError() throws java.lang.Throwable {
            this.payloads.scalarInnerEmptyDisposeDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorInnerActive() throws java.lang.Throwable {
            this.payloads.delayErrorInnerActive.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableConcatMapTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableConcatMapTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableConcatMapTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement asyncFused;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement dispose2;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement innerError;

            public org.junit.runners.model.Statement mainErrorDelayed;

            public org.junit.runners.model.Statement innerErrorDelayError;

            public org.junit.runners.model.Statement innerErrorDelayError2;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement badSourceDelayError;

            public org.junit.runners.model.Statement normalDelayErrors;

            public org.junit.runners.model.Statement normalDelayErrorsTillTheEnd;

            public org.junit.runners.model.Statement onErrorRace;

            public org.junit.runners.model.Statement mapperThrows;

            public org.junit.runners.model.Statement fusedPollThrows;

            public org.junit.runners.model.Statement fusedPollThrowsDelayError;

            public org.junit.runners.model.Statement mapperThrowsDelayError;

            public org.junit.runners.model.Statement badInnerDelayError;

            public org.junit.runners.model.Statement concatReportsDisposedOnComplete;

            public org.junit.runners.model.Statement concatReportsDisposedOnError;

            public org.junit.runners.model.Statement reentrantNoOverflow;

            public org.junit.runners.model.Statement reentrantNoOverflowHidden;

            public org.junit.runners.model.Statement noCancelPrevious;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayErrorTillEnd;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement doubleOnSubscribeDelayError;

            public org.junit.runners.model.Statement scalarXMap;

            public org.junit.runners.model.Statement rejectedFusion;

            public org.junit.runners.model.Statement rejectedFusionDelayError;

            public org.junit.runners.model.Statement asyncFusedDelayError;

            public org.junit.runners.model.Statement scalarInnerJustDelayError;

            public org.junit.runners.model.Statement scalarInnerEmptyDelayError;

            public org.junit.runners.model.Statement scalarInnerJustDisposeDelayError;

            public org.junit.runners.model.Statement scalarInnerEmptyDisposeDelayError;

            public org.junit.runners.model.Statement delayErrorInnerActive;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.asyncFused = _ClassStatement.forPayload(ObservableConcatMapTest::asyncFused, "asyncFused", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableConcatMapTest::dispose, "dispose", this);
            this.payloads.dispose2 = _ClassStatement.forPayload(ObservableConcatMapTest::dispose2, "dispose2", this);
            this.payloads.mainError = _ClassStatement.forPayload(ObservableConcatMapTest::mainError, "mainError", this);
            this.payloads.innerError = _ClassStatement.forPayload(ObservableConcatMapTest::innerError, "innerError", this);
            this.payloads.mainErrorDelayed = _ClassStatement.forPayload(ObservableConcatMapTest::mainErrorDelayed, "mainErrorDelayed", this);
            this.payloads.innerErrorDelayError = _ClassStatement.forPayload(ObservableConcatMapTest::innerErrorDelayError, "innerErrorDelayError", this);
            this.payloads.innerErrorDelayError2 = _ClassStatement.forPayload(ObservableConcatMapTest::innerErrorDelayError2, "innerErrorDelayError2", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableConcatMapTest::badSource, "badSource", this);
            this.payloads.badSourceDelayError = _ClassStatement.forPayload(ObservableConcatMapTest::badSourceDelayError, "badSourceDelayError", this);
            this.payloads.normalDelayErrors = _ClassStatement.forPayload(ObservableConcatMapTest::normalDelayErrors, "normalDelayErrors", this);
            this.payloads.normalDelayErrorsTillTheEnd = _ClassStatement.forPayload(ObservableConcatMapTest::normalDelayErrorsTillTheEnd, "normalDelayErrorsTillTheEnd", this);
            this.payloads.onErrorRace = _ClassStatement.forPayload(ObservableConcatMapTest::onErrorRace, "onErrorRace", this);
            this.payloads.mapperThrows = _ClassStatement.forPayload(ObservableConcatMapTest::mapperThrows, "mapperThrows", this);
            this.payloads.fusedPollThrows = _ClassStatement.forPayload(ObservableConcatMapTest::fusedPollThrows, "fusedPollThrows", this);
            this.payloads.fusedPollThrowsDelayError = _ClassStatement.forPayload(ObservableConcatMapTest::fusedPollThrowsDelayError, "fusedPollThrowsDelayError", this);
            this.payloads.mapperThrowsDelayError = _ClassStatement.forPayload(ObservableConcatMapTest::mapperThrowsDelayError, "mapperThrowsDelayError", this);
            this.payloads.badInnerDelayError = _ClassStatement.forPayload(ObservableConcatMapTest::badInnerDelayError, "badInnerDelayError", this);
            this.payloads.concatReportsDisposedOnComplete = _ClassStatement.forPayload(ObservableConcatMapTest::concatReportsDisposedOnComplete, "concatReportsDisposedOnComplete", this);
            this.payloads.concatReportsDisposedOnError = _ClassStatement.forPayload(ObservableConcatMapTest::concatReportsDisposedOnError, "concatReportsDisposedOnError", this);
            this.payloads.reentrantNoOverflow = _ClassStatement.forPayload(ObservableConcatMapTest::reentrantNoOverflow, "reentrantNoOverflow", this);
            this.payloads.reentrantNoOverflowHidden = _ClassStatement.forPayload(ObservableConcatMapTest::reentrantNoOverflowHidden, "reentrantNoOverflowHidden", this);
            this.payloads.noCancelPrevious = _ClassStatement.forPayload(ObservableConcatMapTest::noCancelPrevious, "noCancelPrevious", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(ObservableConcatMapTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(ObservableConcatMapTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.undeliverableUponCancelDelayErrorTillEnd = _ClassStatement.forPayload(ObservableConcatMapTest::undeliverableUponCancelDelayErrorTillEnd, "undeliverableUponCancelDelayErrorTillEnd", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableConcatMapTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.doubleOnSubscribeDelayError = _ClassStatement.forPayload(ObservableConcatMapTest::doubleOnSubscribeDelayError, "doubleOnSubscribeDelayError", this);
            this.payloads.scalarXMap = _ClassStatement.forPayload(ObservableConcatMapTest::scalarXMap, "scalarXMap", this);
            this.payloads.rejectedFusion = _ClassStatement.forPayload(ObservableConcatMapTest::rejectedFusion, "rejectedFusion", this);
            this.payloads.rejectedFusionDelayError = _ClassStatement.forPayload(ObservableConcatMapTest::rejectedFusionDelayError, "rejectedFusionDelayError", this);
            this.payloads.asyncFusedDelayError = _ClassStatement.forPayload(ObservableConcatMapTest::asyncFusedDelayError, "asyncFusedDelayError", this);
            this.payloads.scalarInnerJustDelayError = _ClassStatement.forPayload(ObservableConcatMapTest::scalarInnerJustDelayError, "scalarInnerJustDelayError", this);
            this.payloads.scalarInnerEmptyDelayError = _ClassStatement.forPayload(ObservableConcatMapTest::scalarInnerEmptyDelayError, "scalarInnerEmptyDelayError", this);
            this.payloads.scalarInnerJustDisposeDelayError = _ClassStatement.forPayload(ObservableConcatMapTest::scalarInnerJustDisposeDelayError, "scalarInnerJustDisposeDelayError", this);
            this.payloads.scalarInnerEmptyDisposeDelayError = _ClassStatement.forPayload(ObservableConcatMapTest::scalarInnerEmptyDisposeDelayError, "scalarInnerEmptyDisposeDelayError", this);
            this.payloads.delayErrorInnerActive = _ClassStatement.forPayload(ObservableConcatMapTest::delayErrorInnerActive, "delayErrorInnerActive", this);
        }
    }
}
