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
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueDisposable;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableFlatMapCompletableTest extends RxJavaTest {

    @Test
    public void normalObservable() {
        Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).toObservable().test().assertResult();
    }

    @Test
    public void mapperThrowsObservable() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).<Integer>toObservable().test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertFailure(TestException.class);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void mapperReturnsNullObservable() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return null;
            }
        }).<Integer>toObservable().test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertFailure(NullPointerException.class);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void normalDelayErrorObservable() {
        Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, true).toObservable().test().assertResult();
    }

    @Test
    public void normalAsyncObservable() {
        Observable.range(1, 1000).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Observable.range(1, 100).subscribeOn(Schedulers.computation()).ignoreElements();
            }
        }).toObservable().test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void normalDelayErrorAllObservable() {
        TestObserverEx<Integer> to = Observable.range(1, 10).concatWith(Observable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true).<Integer>toObservable().to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        for (int i = 0; i < 11; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalDelayInnerErrorAllObservable() {
        TestObserverEx<Integer> to = Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true).<Integer>toObservable().to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        for (int i = 0; i < 10; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalNonDelayErrorOuterObservable() {
        Observable.range(1, 10).concatWith(Observable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, false).toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void fusedObservable() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
        Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).<Integer>toObservable().subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void disposedObservable() {
        TestHelper.checkDisposed(Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).toObservable());
    }

    @Test
    public void normal() {
        Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).test().assertResult();
    }

    @Test
    public void mapperThrows() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Void> to = ps.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertFailure(TestException.class);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void mapperReturnsNull() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Void> to = ps.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return null;
            }
        }).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertFailure(NullPointerException.class);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void normalDelayError() {
        Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, true).test().assertResult();
    }

    @Test
    public void normalAsync() {
        Observable.range(1, 1000).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Observable.range(1, 100).subscribeOn(Schedulers.computation()).ignoreElements();
            }
        }).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void normalDelayErrorAll() {
        TestObserverEx<Void> to = Observable.range(1, 10).concatWith(Observable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true).to(TestHelper.<Void>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        for (int i = 0; i < 11; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalDelayInnerErrorAll() {
        TestObserverEx<Void> to = Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true).to(TestHelper.<Void>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        for (int i = 0; i < 10; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalNonDelayErrorOuter() {
        Observable.range(1, 10).concatWith(Observable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, false).test().assertFailure(TestException.class);
    }

    @Test
    public void fused() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
        Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).<Integer>toObservable().subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }));
    }

    @Test
    public void innerObserver() {
        Observable.range(1, 3).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return new Completable() {

                    @Override
                    protected void subscribeActual(CompletableObserver observer) {
                        observer.onSubscribe(Disposable.empty());
                        assertFalse(((Disposable) observer).isDisposed());
                        ((Disposable) observer).dispose();
                        assertTrue(((Disposable) observer).isDisposed());
                    }
                };
            }
        }).test();
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceObservable(new Function<Observable<Integer>, Object>() {

            @Override
            public Object apply(Observable<Integer> o) throws Exception {
                return o.flatMapCompletable(new Function<Integer, CompletableSource>() {

                    @Override
                    public CompletableSource apply(Integer v) throws Exception {
                        return Completable.complete();
                    }
                });
            }
        }, false, 1, null);
    }

    @Test
    public void fusedInternalsObservable() {
        Observable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).toObservable().subscribe(new Observer<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
                QueueDisposable<?> qd = (QueueDisposable<?>) d;
                try {
                    assertNull(qd.poll());
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
                assertTrue(qd.isEmpty());
                qd.clear();
            }

            @Override
            public void onNext(Object t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void innerObserverObservable() {
        Observable.range(1, 3).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return new Completable() {

                    @Override
                    protected void subscribeActual(CompletableObserver observer) {
                        observer.onSubscribe(Disposable.empty());
                        assertFalse(((Disposable) observer).isDisposed());
                        ((Disposable) observer).dispose();
                        assertTrue(((Disposable) observer).isDisposed());
                    }
                };
            }
        }).toObservable().test();
    }

    @Test
    public void badSourceObservable() {
        TestHelper.checkBadSourceObservable(new Function<Observable<Integer>, Object>() {

            @Override
            public Object apply(Observable<Integer> o) throws Exception {
                return o.flatMapCompletable(new Function<Integer, CompletableSource>() {

                    @Override
                    public CompletableSource apply(Integer v) throws Exception {
                        return Completable.complete();
                    }
                }).toObservable();
            }
        }, false, 1, null);
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Observable<Integer> upstream) {
                return upstream.flatMapCompletable(new Function<Integer, Completable>() {

                    @Override
                    public Completable apply(Integer v) throws Throwable {
                        return Completable.complete().hide();
                    }
                });
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayError() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Observable<Integer> upstream) {
                return upstream.flatMapCompletable(new Function<Integer, Completable>() {

                    @Override
                    public Completable apply(Integer v) throws Throwable {
                        return Completable.complete().hide();
                    }
                }, true);
            }
        });
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(o -> o.flatMapCompletable(v -> Completable.never()).toObservable());
    }

    @Test
    public void doubleOnSubscribeCompletable() {
        TestHelper.checkDoubleOnSubscribeObservableToCompletable(o -> o.flatMapCompletable(v -> Completable.never()));
    }

    @Test
    public void cancelWhileMapping() throws Throwable {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishSubject<Integer> ps1 = PublishSubject.create();
            TestObserver<Object> to = new TestObserver<>();
            CountDownLatch cdl = new CountDownLatch(1);
            ps1.flatMapCompletable(v -> {
                TestHelper.raceOther(() -> {
                    to.dispose();
                }, cdl);
                return Completable.complete();
            }).toObservable().subscribe(to);
            ps1.onNext(1);
            cdl.await();
        }
    }

    @Test
    public void cancelWhileMappingCompletable() throws Throwable {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishSubject<Integer> ps1 = PublishSubject.create();
            TestObserver<Void> to = new TestObserver<>();
            CountDownLatch cdl = new CountDownLatch(1);
            ps1.flatMapCompletable(v -> {
                TestHelper.raceOther(() -> {
                    to.dispose();
                }, cdl);
                return Completable.complete();
            }).subscribe(to);
            ps1.onNext(1);
            cdl.await();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableFlatMapCompletableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalObservable() throws java.lang.Throwable {
            this.payloads.normalObservable.evaluate();
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
        public void benchmark_normalDelayErrorObservable() throws java.lang.Throwable {
            this.payloads.normalDelayErrorObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsyncObservable() throws java.lang.Throwable {
            this.payloads.normalAsyncObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayErrorAllObservable() throws java.lang.Throwable {
            this.payloads.normalDelayErrorAllObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayInnerErrorAllObservable() throws java.lang.Throwable {
            this.payloads.normalDelayInnerErrorAllObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalNonDelayErrorOuterObservable() throws java.lang.Throwable {
            this.payloads.normalNonDelayErrorOuterObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedObservable() throws java.lang.Throwable {
            this.payloads.fusedObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedObservable() throws java.lang.Throwable {
            this.payloads.disposedObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrows() throws java.lang.Throwable {
            this.payloads.mapperThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperReturnsNull() throws java.lang.Throwable {
            this.payloads.mapperReturnsNull.evaluate();
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
        public void benchmark_normalDelayErrorAll() throws java.lang.Throwable {
            this.payloads.normalDelayErrorAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayInnerErrorAll() throws java.lang.Throwable {
            this.payloads.normalDelayInnerErrorAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalNonDelayErrorOuter() throws java.lang.Throwable {
            this.payloads.normalNonDelayErrorOuter.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.payloads.fused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerObserver() throws java.lang.Throwable {
            this.payloads.innerObserver.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedInternalsObservable() throws java.lang.Throwable {
            this.payloads.fusedInternalsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerObserverObservable() throws java.lang.Throwable {
            this.payloads.innerObserverObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceObservable() throws java.lang.Throwable {
            this.payloads.badSourceObservable.evaluate();
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
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeCompletable() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribeCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWhileMapping() throws java.lang.Throwable {
            this.payloads.cancelWhileMapping.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWhileMappingCompletable() throws java.lang.Throwable {
            this.payloads.cancelWhileMappingCompletable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapCompletableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapCompletableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapCompletableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapCompletableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableFlatMapCompletableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapCompletableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableFlatMapCompletableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableFlatMapCompletableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normalObservable;

            public org.junit.runners.model.Statement mapperThrowsObservable;

            public org.junit.runners.model.Statement mapperReturnsNullObservable;

            public org.junit.runners.model.Statement normalDelayErrorObservable;

            public org.junit.runners.model.Statement normalAsyncObservable;

            public org.junit.runners.model.Statement normalDelayErrorAllObservable;

            public org.junit.runners.model.Statement normalDelayInnerErrorAllObservable;

            public org.junit.runners.model.Statement normalNonDelayErrorOuterObservable;

            public org.junit.runners.model.Statement fusedObservable;

            public org.junit.runners.model.Statement disposedObservable;

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement mapperThrows;

            public org.junit.runners.model.Statement mapperReturnsNull;

            public org.junit.runners.model.Statement normalDelayError;

            public org.junit.runners.model.Statement normalAsync;

            public org.junit.runners.model.Statement normalDelayErrorAll;

            public org.junit.runners.model.Statement normalDelayInnerErrorAll;

            public org.junit.runners.model.Statement normalNonDelayErrorOuter;

            public org.junit.runners.model.Statement fused;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement innerObserver;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement fusedInternalsObservable;

            public org.junit.runners.model.Statement innerObserverObservable;

            public org.junit.runners.model.Statement badSourceObservable;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement doubleOnSubscribeCompletable;

            public org.junit.runners.model.Statement cancelWhileMapping;

            public org.junit.runners.model.Statement cancelWhileMappingCompletable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normalObservable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::normalObservable, "normalObservable", this);
            this.payloads.mapperThrowsObservable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::mapperThrowsObservable, "mapperThrowsObservable", this);
            this.payloads.mapperReturnsNullObservable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::mapperReturnsNullObservable, "mapperReturnsNullObservable", this);
            this.payloads.normalDelayErrorObservable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::normalDelayErrorObservable, "normalDelayErrorObservable", this);
            this.payloads.normalAsyncObservable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::normalAsyncObservable, "normalAsyncObservable", this);
            this.payloads.normalDelayErrorAllObservable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::normalDelayErrorAllObservable, "normalDelayErrorAllObservable", this);
            this.payloads.normalDelayInnerErrorAllObservable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::normalDelayInnerErrorAllObservable, "normalDelayInnerErrorAllObservable", this);
            this.payloads.normalNonDelayErrorOuterObservable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::normalNonDelayErrorOuterObservable, "normalNonDelayErrorOuterObservable", this);
            this.payloads.fusedObservable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::fusedObservable, "fusedObservable", this);
            this.payloads.disposedObservable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::disposedObservable, "disposedObservable", this);
            this.payloads.normal = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::normal, "normal", this);
            this.payloads.mapperThrows = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::mapperThrows, "mapperThrows", this);
            this.payloads.mapperReturnsNull = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::mapperReturnsNull, "mapperReturnsNull", this);
            this.payloads.normalDelayError = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::normalDelayError, "normalDelayError", this);
            this.payloads.normalAsync = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::normalAsync, "normalAsync", this);
            this.payloads.normalDelayErrorAll = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::normalDelayErrorAll, "normalDelayErrorAll", this);
            this.payloads.normalDelayInnerErrorAll = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::normalDelayInnerErrorAll, "normalDelayInnerErrorAll", this);
            this.payloads.normalNonDelayErrorOuter = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::normalNonDelayErrorOuter, "normalNonDelayErrorOuter", this);
            this.payloads.fused = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::fused, "fused", this);
            this.payloads.disposed = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::disposed, "disposed", this);
            this.payloads.innerObserver = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::innerObserver, "innerObserver", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::badSource, "badSource", this);
            this.payloads.fusedInternalsObservable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::fusedInternalsObservable, "fusedInternalsObservable", this);
            this.payloads.innerObserverObservable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::innerObserverObservable, "innerObserverObservable", this);
            this.payloads.badSourceObservable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::badSourceObservable, "badSourceObservable", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.doubleOnSubscribeCompletable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::doubleOnSubscribeCompletable, "doubleOnSubscribeCompletable", this);
            this.payloads.cancelWhileMapping = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::cancelWhileMapping, "cancelWhileMapping", this);
            this.payloads.cancelWhileMappingCompletable = _ClassStatement.forPayload(ObservableFlatMapCompletableTest::cancelWhileMappingCompletable, "cancelWhileMappingCompletable", this);
        }
    }
}
