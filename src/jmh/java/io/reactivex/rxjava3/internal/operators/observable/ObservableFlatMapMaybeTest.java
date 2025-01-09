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
import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableFlatMapMaybeTest extends RxJavaTest {

    @Test
    public void normal() {
        Observable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalEmpty() {
        Observable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.empty();
            }
        }).test().assertResult();
    }

    @Test
    public void normalDelayError() {
        Observable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }, true).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalAsync() {
        TestObserverEx<Integer> to = Observable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v).subscribeOn(Schedulers.computation());
            }
        }).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertNoErrors().assertComplete();
        TestHelper.assertValueSet(to, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void mapperThrowsObservable() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertFailure(TestException.class);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void mapperReturnsNullObservable() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return null;
            }
        }).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertFailure(NullPointerException.class);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void normalDelayErrorAll() {
        TestObserverEx<Integer> to = Observable.range(1, 10).concatWith(Observable.<Integer>error(new TestException())).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.error(new TestException());
            }
        }, true).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        for (int i = 0; i < 11; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void takeAsync() {
        TestObserverEx<Integer> to = Observable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v).subscribeOn(Schedulers.computation());
            }
        }).take(2).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(2).assertNoErrors().assertComplete();
        TestHelper.assertValueSet(to, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void take() {
        Observable.range(1, 10).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }).take(2).test().assertResult(1, 2);
    }

    @Test
    public void middleError() {
        Observable.fromArray(new String[] { "1", "a", "2" }).flatMapMaybe(new Function<String, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(final String s) throws NumberFormatException {
                // return Single.just(Integer.valueOf(s)); //This works
                return Maybe.fromCallable(new Callable<Integer>() {

                    @Override
                    public Integer call() throws NumberFormatException {
                        return Integer.valueOf(s);
                    }
                });
            }
        }).test().assertFailure(NumberFormatException.class, 1);
    }

    @Test
    public void asyncFlatten() {
        Observable.range(1, 1000).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(1).subscribeOn(Schedulers.computation());
            }
        }).take(500).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(500).assertNoErrors().assertComplete();
    }

    @Test
    public void asyncFlattenNone() {
        Observable.range(1, 1000).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.<Integer>empty().subscribeOn(Schedulers.computation());
            }
        }).take(500).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void successError() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = Observable.range(1, 2).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                if (v == 2) {
                    return ps.singleElement();
                }
                return Maybe.error(new TestException());
            }
        }, true).test();
        ps.onNext(1);
        ps.onComplete();
        to.assertFailure(TestException.class, 1);
    }

    @Test
    public void completeError() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = Observable.range(1, 2).flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                if (v == 2) {
                    return ps.singleElement();
                }
                return Maybe.error(new TestException());
            }
        }, true).test();
        ps.onComplete();
        to.assertFailure(TestException.class);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishSubject.<Integer>create().flatMapMaybe(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.<Integer>empty();
            }
        }));
    }

    @Test
    public void innerSuccessCompletesAfterMain() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = Observable.just(1).flatMapMaybe(Functions.justFunction(ps.singleElement())).test();
        ps.onNext(2);
        ps.onComplete();
        to.assertResult(2);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Observable<Object> f) throws Exception {
                return f.flatMapMaybe(Functions.justFunction(Maybe.just(2)));
            }
        });
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onError(new TestException("First"));
                    observer.onError(new TestException("Second"));
                }
            }.flatMapMaybe(Functions.justFunction(Maybe.just(2))).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badInnerSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.just(1).flatMapMaybe(Functions.justFunction(new Maybe<Integer>() {

                @Override
                protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onError(new TestException("First"));
                    observer.onError(new TestException("Second"));
                }
            })).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void emissionQueueTrigger() {
        final PublishSubject<Integer> ps1 = PublishSubject.create();
        final PublishSubject<Integer> ps2 = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    ps2.onNext(2);
                    ps2.onComplete();
                }
            }
        };
        Observable.just(ps1, ps2).flatMapMaybe(new Function<PublishSubject<Integer>, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(PublishSubject<Integer> v) throws Exception {
                return v.singleElement();
            }
        }).subscribe(to);
        ps1.onNext(1);
        ps1.onComplete();
        to.assertResult(1, 2);
    }

    @Test
    public void emissionQueueTrigger2() {
        final PublishSubject<Integer> ps1 = PublishSubject.create();
        final PublishSubject<Integer> ps2 = PublishSubject.create();
        final PublishSubject<Integer> ps3 = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    ps2.onNext(2);
                    ps2.onComplete();
                }
            }
        };
        Observable.just(ps1, ps2, ps3).flatMapMaybe(new Function<PublishSubject<Integer>, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(PublishSubject<Integer> v) throws Exception {
                return v.singleElement();
            }
        }).subscribe(to);
        ps1.onNext(1);
        ps1.onComplete();
        ps3.onComplete();
        to.assertResult(1, 2);
    }

    @Test
    public void disposeInner() {
        final TestObserver<Object> to = new TestObserver<>();
        Observable.just(1).flatMapMaybe(new Function<Integer, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Integer v) throws Exception {
                return new Maybe<Object>() {

                    @Override
                    protected void subscribeActual(MaybeObserver<? super Object> observer) {
                        observer.onSubscribe(Disposable.empty());
                        assertFalse(((Disposable) observer).isDisposed());
                        to.dispose();
                        assertTrue(((Disposable) observer).isDisposed());
                    }
                };
            }
        }).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> upstream) {
                return upstream.flatMapMaybe(new Function<Integer, Maybe<Integer>>() {

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
                return upstream.flatMapMaybe(new Function<Integer, Maybe<Integer>>() {

                    @Override
                    public Maybe<Integer> apply(Integer v) throws Throwable {
                        return Maybe.just(v).hide();
                    }
                }, true);
            }
        });
    }

    @Test
    public void cancelWhileMapping() throws Throwable {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishSubject<Integer> ps1 = PublishSubject.create();
            TestObserver<Integer> to = new TestObserver<>();
            CountDownLatch cdl = new CountDownLatch(1);
            ps1.flatMapMaybe(v -> {
                TestHelper.raceOther(() -> {
                    to.dispose();
                }, cdl);
                return Maybe.just(1);
            }).subscribe(to);
            ps1.onNext(1);
            cdl.await();
        }
    }

    @Test
    public void successCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            MaybeSubject<Integer> ms1 = MaybeSubject.create();
            MaybeSubject<Integer> ms2 = MaybeSubject.create();
            TestObserver<Integer> to = Observable.just(1, 2).flatMapMaybe(v -> v == 1 ? ms1 : ms2).test();
            TestHelper.race(() -> ms1.onComplete(), () -> ms2.onSuccess(1));
            to.assertResult(1);
        }
    }

    @Test
    public void successCompleteRace2() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            MaybeSubject<Integer> ms1 = MaybeSubject.create();
            MaybeSubject<Integer> ms2 = MaybeSubject.create();
            TestObserver<Integer> to = Observable.just(1, 2).flatMapMaybe(v -> v == 1 ? ms1 : ms2).test();
            TestHelper.race(() -> ms2.onSuccess(1), () -> ms1.onComplete());
            to.assertResult(1);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableFlatMapMaybeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmpty() throws java.lang.Throwable {
            this.payloads.normalEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayError() throws java.lang.Throwable {
            this.payloads.normalDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsync() throws java.lang.Throwable {
            this.payloads.normalAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrowsObservable() throws java.lang.Throwable {
            this.payloads.mapperThrowsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperReturnsNullObservable() throws java.lang.Throwable {
            this.payloads.mapperReturnsNullObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayErrorAll() throws java.lang.Throwable {
            this.payloads.normalDelayErrorAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeAsync() throws java.lang.Throwable {
            this.payloads.takeAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_middleError() throws java.lang.Throwable {
            this.payloads.middleError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFlatten() throws java.lang.Throwable {
            this.payloads.asyncFlatten.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFlattenNone() throws java.lang.Throwable {
            this.payloads.asyncFlattenNone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successError() throws java.lang.Throwable {
            this.payloads.successError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeError() throws java.lang.Throwable {
            this.payloads.completeError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerSuccessCompletesAfterMain() throws java.lang.Throwable {
            this.payloads.innerSuccessCompletesAfterMain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badInnerSource() throws java.lang.Throwable {
            this.payloads.badInnerSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emissionQueueTrigger() throws java.lang.Throwable {
            this.payloads.emissionQueueTrigger.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emissionQueueTrigger2() throws java.lang.Throwable {
            this.payloads.emissionQueueTrigger2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeInner() throws java.lang.Throwable {
            this.payloads.disposeInner.evaluate();
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
        public void benchmark_cancelWhileMapping() throws java.lang.Throwable {
            this.payloads.cancelWhileMapping.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successCompleteRace() throws java.lang.Throwable {
            this.payloads.successCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successCompleteRace2() throws java.lang.Throwable {
            this.payloads.successCompleteRace2.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapMaybeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapMaybeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapMaybeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapMaybeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableFlatMapMaybeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapMaybeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableFlatMapMaybeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableFlatMapMaybeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement normalEmpty;

            public org.junit.runners.model.Statement normalDelayError;

            public org.junit.runners.model.Statement normalAsync;

            public org.junit.runners.model.Statement mapperThrowsObservable;

            public org.junit.runners.model.Statement mapperReturnsNullObservable;

            public org.junit.runners.model.Statement normalDelayErrorAll;

            public org.junit.runners.model.Statement takeAsync;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement middleError;

            public org.junit.runners.model.Statement asyncFlatten;

            public org.junit.runners.model.Statement asyncFlattenNone;

            public org.junit.runners.model.Statement successError;

            public org.junit.runners.model.Statement completeError;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement innerSuccessCompletesAfterMain;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement badInnerSource;

            public org.junit.runners.model.Statement emissionQueueTrigger;

            public org.junit.runners.model.Statement emissionQueueTrigger2;

            public org.junit.runners.model.Statement disposeInner;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement cancelWhileMapping;

            public org.junit.runners.model.Statement successCompleteRace;

            public org.junit.runners.model.Statement successCompleteRace2;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::normal, "normal", this);
            this.payloads.normalEmpty = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::normalEmpty, "normalEmpty", this);
            this.payloads.normalDelayError = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::normalDelayError, "normalDelayError", this);
            this.payloads.normalAsync = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::normalAsync, "normalAsync", this);
            this.payloads.mapperThrowsObservable = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::mapperThrowsObservable, "mapperThrowsObservable", this);
            this.payloads.mapperReturnsNullObservable = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::mapperReturnsNullObservable, "mapperReturnsNullObservable", this);
            this.payloads.normalDelayErrorAll = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::normalDelayErrorAll, "normalDelayErrorAll", this);
            this.payloads.takeAsync = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::takeAsync, "takeAsync", this);
            this.payloads.take = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::take, "take", this);
            this.payloads.middleError = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::middleError, "middleError", this);
            this.payloads.asyncFlatten = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::asyncFlatten, "asyncFlatten", this);
            this.payloads.asyncFlattenNone = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::asyncFlattenNone, "asyncFlattenNone", this);
            this.payloads.successError = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::successError, "successError", this);
            this.payloads.completeError = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::completeError, "completeError", this);
            this.payloads.disposed = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::disposed, "disposed", this);
            this.payloads.innerSuccessCompletesAfterMain = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::innerSuccessCompletesAfterMain, "innerSuccessCompletesAfterMain", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::badSource, "badSource", this);
            this.payloads.badInnerSource = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::badInnerSource, "badInnerSource", this);
            this.payloads.emissionQueueTrigger = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::emissionQueueTrigger, "emissionQueueTrigger", this);
            this.payloads.emissionQueueTrigger2 = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::emissionQueueTrigger2, "emissionQueueTrigger2", this);
            this.payloads.disposeInner = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::disposeInner, "disposeInner", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.cancelWhileMapping = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::cancelWhileMapping, "cancelWhileMapping", this);
            this.payloads.successCompleteRace = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::successCompleteRace, "successCompleteRace", this);
            this.payloads.successCompleteRace2 = _ClassStatement.forPayload(ObservableFlatMapMaybeTest::successCompleteRace2, "successCompleteRace2", this);
        }
    }
}
