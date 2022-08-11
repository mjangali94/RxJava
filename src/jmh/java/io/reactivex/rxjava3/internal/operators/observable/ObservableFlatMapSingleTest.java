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

public class ObservableFlatMapSingleTest extends RxJavaTest {

    @Test
    public void normal() {
        Observable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalDelayError() {
        Observable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }, true).test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void normalAsync() {
        TestObserverEx<Integer> to = Observable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v).subscribeOn(Schedulers.computation());
            }
        }).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertNoErrors().assertComplete();
        TestHelper.assertValueSet(to, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void mapperThrowsObservable() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
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
        TestObserver<Integer> to = ps.flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
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
        TestObserverEx<Integer> to = Observable.range(1, 10).concatWith(Observable.<Integer>error(new TestException())).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.error(new TestException());
            }
        }, true).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        for (int i = 0; i < 11; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void takeAsync() {
        TestObserverEx<Integer> to = Observable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v).subscribeOn(Schedulers.computation());
            }
        }).take(2).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(2).assertNoErrors().assertComplete();
        TestHelper.assertValueSet(to, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void take() {
        Observable.range(1, 10).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }).take(2).test().assertResult(1, 2);
    }

    @Test
    public void middleError() {
        Observable.fromArray(new String[] { "1", "a", "2" }).flatMapSingle(new Function<String, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(final String s) throws NumberFormatException {
                // return Single.just(Integer.valueOf(s)); //This works
                return Single.fromCallable(new Callable<Integer>() {

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
        Observable.range(1, 1000).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.just(1).subscribeOn(Schedulers.computation());
            }
        }).take(500).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertSubscribed().assertValueCount(500).assertNoErrors().assertComplete();
    }

    @Test
    public void successError() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = Observable.range(1, 2).flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                if (v == 2) {
                    return ps.singleOrError();
                }
                return Single.error(new TestException());
            }
        }, true).test();
        ps.onNext(1);
        ps.onComplete();
        to.assertFailure(TestException.class, 1);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishSubject.<Integer>create().flatMapSingle(new Function<Integer, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Integer v) throws Exception {
                return Single.<Integer>just(1);
            }
        }));
    }

    @Test
    public void innerSuccessCompletesAfterMain() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = Observable.just(1).flatMapSingle(Functions.justFunction(ps.singleOrError())).test();
        ps.onNext(2);
        ps.onComplete();
        to.assertResult(2);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Observable<Object> f) throws Exception {
                return f.flatMapSingle(Functions.justFunction(Single.just(2)));
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
            }.flatMapSingle(Functions.justFunction(Single.just(2))).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badInnerSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.just(1).flatMapSingle(Functions.justFunction(new Single<Integer>() {

                @Override
                protected void subscribeActual(SingleObserver<? super Integer> observer) {
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
        Observable.just(ps1, ps2).flatMapSingle(new Function<PublishSubject<Integer>, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(PublishSubject<Integer> v) throws Exception {
                return v.singleOrError();
            }
        }).subscribe(to);
        ps1.onNext(1);
        ps1.onComplete();
        to.assertResult(1, 2);
    }

    @Test
    public void disposeInner() {
        final TestObserver<Object> to = new TestObserver<>();
        Observable.just(1).flatMapSingle(new Function<Integer, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Integer v) throws Exception {
                return new Single<Object>() {

                    @Override
                    protected void subscribeActual(SingleObserver<? super Object> observer) {
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
                return upstream.flatMapSingle(new Function<Integer, Single<Integer>>() {

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
                return upstream.flatMapSingle(new Function<Integer, Single<Integer>>() {

                    @Override
                    public Single<Integer> apply(Integer v) throws Throwable {
                        return Single.just(v).hide();
                    }
                }, true);
            }
        });
    }

    @Test
    public void innerErrorOuterCompleteRace() {
        TestException ex = new TestException();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishSubject<Integer> ps1 = PublishSubject.create();
            SingleSubject<Integer> ps2 = SingleSubject.create();
            TestObserver<Integer> to = ps1.flatMapSingle(v -> ps2).test();
            ps1.onNext(1);
            TestHelper.race(() -> ps1.onComplete(), () -> ps2.onError(ex));
            to.assertFailure(TestException.class);
        }
    }

    @Test
    public void cancelWhileMapping() throws Throwable {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishSubject<Integer> ps1 = PublishSubject.create();
            TestObserver<Integer> to = new TestObserver<>();
            CountDownLatch cdl = new CountDownLatch(1);
            ps1.flatMapSingle(v -> {
                TestHelper.raceOther(() -> {
                    to.dispose();
                }, cdl);
                return Single.just(1);
            }).subscribe(to);
            ps1.onNext(1);
            cdl.await();
        }
    }

    @Test
    public void onNextDrainCancel() {
        SingleSubject<Integer> ss1 = SingleSubject.create();
        SingleSubject<Integer> ss2 = SingleSubject.create();
        TestObserver<Integer> to = new TestObserver<>();
        Observable.just(1, 2).flatMapSingle(v -> v == 1 ? ss1 : ss2).doOnNext(v -> {
            if (v == 1) {
                ss2.onSuccess(2);
                to.dispose();
            }
        }).subscribe(to);
        ss1.onSuccess(1);
        to.assertValuesOnly(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableFlatMapSingleTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
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
        public void benchmark_successError() throws java.lang.Throwable {
            this.payloads.successError.evaluate();
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
        public void benchmark_innerErrorOuterCompleteRace() throws java.lang.Throwable {
            this.payloads.innerErrorOuterCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWhileMapping() throws java.lang.Throwable {
            this.payloads.cancelWhileMapping.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextDrainCancel() throws java.lang.Throwable {
            this.payloads.onNextDrainCancel.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapSingleTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapSingleTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapSingleTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapSingleTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableFlatMapSingleTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapSingleTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableFlatMapSingleTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableFlatMapSingleTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement normalDelayError;

            public org.junit.runners.model.Statement normalAsync;

            public org.junit.runners.model.Statement mapperThrowsObservable;

            public org.junit.runners.model.Statement mapperReturnsNullObservable;

            public org.junit.runners.model.Statement normalDelayErrorAll;

            public org.junit.runners.model.Statement takeAsync;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement middleError;

            public org.junit.runners.model.Statement asyncFlatten;

            public org.junit.runners.model.Statement successError;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement innerSuccessCompletesAfterMain;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement badInnerSource;

            public org.junit.runners.model.Statement emissionQueueTrigger;

            public org.junit.runners.model.Statement disposeInner;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement innerErrorOuterCompleteRace;

            public org.junit.runners.model.Statement cancelWhileMapping;

            public org.junit.runners.model.Statement onNextDrainCancel;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(ObservableFlatMapSingleTest::normal, "normal", this);
            this.payloads.normalDelayError = _ClassStatement.forPayload(ObservableFlatMapSingleTest::normalDelayError, "normalDelayError", this);
            this.payloads.normalAsync = _ClassStatement.forPayload(ObservableFlatMapSingleTest::normalAsync, "normalAsync", this);
            this.payloads.mapperThrowsObservable = _ClassStatement.forPayload(ObservableFlatMapSingleTest::mapperThrowsObservable, "mapperThrowsObservable", this);
            this.payloads.mapperReturnsNullObservable = _ClassStatement.forPayload(ObservableFlatMapSingleTest::mapperReturnsNullObservable, "mapperReturnsNullObservable", this);
            this.payloads.normalDelayErrorAll = _ClassStatement.forPayload(ObservableFlatMapSingleTest::normalDelayErrorAll, "normalDelayErrorAll", this);
            this.payloads.takeAsync = _ClassStatement.forPayload(ObservableFlatMapSingleTest::takeAsync, "takeAsync", this);
            this.payloads.take = _ClassStatement.forPayload(ObservableFlatMapSingleTest::take, "take", this);
            this.payloads.middleError = _ClassStatement.forPayload(ObservableFlatMapSingleTest::middleError, "middleError", this);
            this.payloads.asyncFlatten = _ClassStatement.forPayload(ObservableFlatMapSingleTest::asyncFlatten, "asyncFlatten", this);
            this.payloads.successError = _ClassStatement.forPayload(ObservableFlatMapSingleTest::successError, "successError", this);
            this.payloads.disposed = _ClassStatement.forPayload(ObservableFlatMapSingleTest::disposed, "disposed", this);
            this.payloads.innerSuccessCompletesAfterMain = _ClassStatement.forPayload(ObservableFlatMapSingleTest::innerSuccessCompletesAfterMain, "innerSuccessCompletesAfterMain", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableFlatMapSingleTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableFlatMapSingleTest::badSource, "badSource", this);
            this.payloads.badInnerSource = _ClassStatement.forPayload(ObservableFlatMapSingleTest::badInnerSource, "badInnerSource", this);
            this.payloads.emissionQueueTrigger = _ClassStatement.forPayload(ObservableFlatMapSingleTest::emissionQueueTrigger, "emissionQueueTrigger", this);
            this.payloads.disposeInner = _ClassStatement.forPayload(ObservableFlatMapSingleTest::disposeInner, "disposeInner", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(ObservableFlatMapSingleTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(ObservableFlatMapSingleTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.innerErrorOuterCompleteRace = _ClassStatement.forPayload(ObservableFlatMapSingleTest::innerErrorOuterCompleteRace, "innerErrorOuterCompleteRace", this);
            this.payloads.cancelWhileMapping = _ClassStatement.forPayload(ObservableFlatMapSingleTest::cancelWhileMapping, "cancelWhileMapping", this);
            this.payloads.onNextDrainCancel = _ClassStatement.forPayload(ObservableFlatMapSingleTest::onNextDrainCancel, "onNextDrainCancel", this);
        }
    }
}
