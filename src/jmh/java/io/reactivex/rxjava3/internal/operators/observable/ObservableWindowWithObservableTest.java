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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableWindowWithObservableTest extends RxJavaTest {

    @Test
    public void windowViaObservableNormal1() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> boundary = PublishSubject.create();
        final Observer<Object> o = TestHelper.mockObserver();
        final List<Observer<Object>> values = new ArrayList<>();
        Observer<Observable<Integer>> wo = new DefaultObserver<Observable<Integer>>() {

            @Override
            public void onNext(Observable<Integer> args) {
                final Observer<Object> mo = TestHelper.mockObserver();
                values.add(mo);
                args.subscribe(mo);
            }

            @Override
            public void onError(Throwable e) {
                o.onError(e);
            }

            @Override
            public void onComplete() {
                o.onComplete();
            }
        };
        source.window(boundary).subscribe(wo);
        int n = 30;
        for (int i = 0; i < n; i++) {
            source.onNext(i);
            if (i % 3 == 2 && i < n - 1) {
                boundary.onNext(i / 3);
            }
        }
        source.onComplete();
        verify(o, never()).onError(any(Throwable.class));
        assertEquals(n / 3, values.size());
        int j = 0;
        for (Observer<Object> mo : values) {
            verify(mo, never()).onError(any(Throwable.class));
            for (int i = 0; i < 3; i++) {
                verify(mo).onNext(j + i);
            }
            verify(mo).onComplete();
            j += 3;
        }
        verify(o).onComplete();
    }

    @Test
    public void windowViaObservableBoundaryCompletes() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> boundary = PublishSubject.create();
        final Observer<Object> o = TestHelper.mockObserver();
        final List<Observer<Object>> values = new ArrayList<>();
        Observer<Observable<Integer>> wo = new DefaultObserver<Observable<Integer>>() {

            @Override
            public void onNext(Observable<Integer> args) {
                final Observer<Object> mo = TestHelper.mockObserver();
                values.add(mo);
                args.subscribe(mo);
            }

            @Override
            public void onError(Throwable e) {
                o.onError(e);
            }

            @Override
            public void onComplete() {
                o.onComplete();
            }
        };
        source.window(boundary).subscribe(wo);
        int n = 30;
        for (int i = 0; i < n; i++) {
            source.onNext(i);
            if (i % 3 == 2 && i < n - 1) {
                boundary.onNext(i / 3);
            }
        }
        boundary.onComplete();
        assertEquals(n / 3, values.size());
        int j = 0;
        for (Observer<Object> mo : values) {
            for (int i = 0; i < 3; i++) {
                verify(mo).onNext(j + i);
            }
            verify(mo).onComplete();
            verify(mo, never()).onError(any(Throwable.class));
            j += 3;
        }
        verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void windowViaObservableBoundaryThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> boundary = PublishSubject.create();
        final Observer<Object> o = TestHelper.mockObserver();
        final List<Observer<Object>> values = new ArrayList<>();
        Observer<Observable<Integer>> wo = new DefaultObserver<Observable<Integer>>() {

            @Override
            public void onNext(Observable<Integer> args) {
                final Observer<Object> mo = TestHelper.mockObserver();
                values.add(mo);
                args.subscribe(mo);
            }

            @Override
            public void onError(Throwable e) {
                o.onError(e);
            }

            @Override
            public void onComplete() {
                o.onComplete();
            }
        };
        source.window(boundary).subscribe(wo);
        source.onNext(0);
        source.onNext(1);
        source.onNext(2);
        boundary.onError(new TestException());
        assertEquals(1, values.size());
        Observer<Object> mo = values.get(0);
        verify(mo).onNext(0);
        verify(mo).onNext(1);
        verify(mo).onNext(2);
        verify(mo).onError(any(TestException.class));
        verify(o, never()).onComplete();
        verify(o).onError(any(TestException.class));
    }

    @Test
    public void windowViaObservableSourceThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> boundary = PublishSubject.create();
        final Observer<Object> o = TestHelper.mockObserver();
        final List<Observer<Object>> values = new ArrayList<>();
        Observer<Observable<Integer>> wo = new DefaultObserver<Observable<Integer>>() {

            @Override
            public void onNext(Observable<Integer> args) {
                final Observer<Object> mo = TestHelper.mockObserver();
                values.add(mo);
                args.subscribe(mo);
            }

            @Override
            public void onError(Throwable e) {
                o.onError(e);
            }

            @Override
            public void onComplete() {
                o.onComplete();
            }
        };
        source.window(boundary).subscribe(wo);
        source.onNext(0);
        source.onNext(1);
        source.onNext(2);
        source.onError(new TestException());
        assertEquals(1, values.size());
        Observer<Object> mo = values.get(0);
        verify(mo).onNext(0);
        verify(mo).onNext(1);
        verify(mo).onNext(2);
        verify(mo).onError(any(TestException.class));
        verify(o, never()).onComplete();
        verify(o).onError(any(TestException.class));
    }

    @Test
    public void boundaryDispose() {
        TestHelper.checkDisposed(Observable.never().window(Observable.never()));
    }

    @Test
    public void boundaryOnError() {
        TestObserverEx<Object> to = Observable.error(new TestException()).window(Observable.never()).flatMap(Functions.<Observable<Object>>identity(), true).to(TestHelper.testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(errors, 0, TestException.class);
    }

    @Test
    public void innerBadSource() {
        TestHelper.checkBadSourceObservable(new Function<Observable<Integer>, Object>() {

            @Override
            public Object apply(Observable<Integer> o) throws Exception {
                return Observable.just(1).window(o).flatMap(new Function<Observable<Integer>, ObservableSource<Integer>>() {

                    @Override
                    public ObservableSource<Integer> apply(Observable<Integer> v) throws Exception {
                        return v;
                    }
                });
            }
        }, false, 1, 1, (Object[]) null);
    }

    @Test
    public void reentrant() {
        final Subject<Integer> ps = PublishSubject.<Integer>create();
        TestObserver<Integer> to = new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    ps.onNext(2);
                    ps.onComplete();
                }
            }
        };
        ps.window(BehaviorSubject.createDefault(1)).flatMap(new Function<Observable<Integer>, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Observable<Integer> v) throws Exception {
                return v;
            }
        }).subscribe(to);
        ps.onNext(1);
        to.awaitDone(1, TimeUnit.SECONDS).assertResult(1, 2);
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceObservable(new Function<Observable<Object>, Object>() {

            @Override
            public Object apply(Observable<Object> o) throws Exception {
                return o.window(Observable.never()).flatMap(new Function<Observable<Object>, ObservableSource<Object>>() {

                    @Override
                    public ObservableSource<Object> apply(Observable<Object> v) throws Exception {
                        return v;
                    }
                });
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void boundaryDirectDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Observable<Object>>>() {

            @Override
            public Observable<Observable<Object>> apply(Observable<Object> f) throws Exception {
                return f.window(Observable.never()).takeLast(1);
            }
        });
    }

    @Test
    public void upstreamDisposedWhenOutputsDisposed() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> boundary = PublishSubject.create();
        TestObserver<Integer> to = source.window(boundary).take(1).flatMap(new Function<Observable<Integer>, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Observable<Integer> w) throws Exception {
                return w.take(1);
            }
        }).test();
        source.onNext(1);
        assertFalse("source not disposed", source.hasObservers());
        assertFalse("boundary not disposed", boundary.hasObservers());
        to.assertResult(1);
    }

    @Test
    public void mainAndBoundaryBothError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final AtomicReference<Observer<? super Object>> ref = new AtomicReference<>();
            TestObserverEx<Observable<Object>> to = Observable.error(new TestException("main")).window(new Observable<Object>() {

                @Override
                protected void subscribeActual(Observer<? super Object> observer) {
                    observer.onSubscribe(Disposable.empty());
                    ref.set(observer);
                }
            }).doOnNext(new Consumer<Observable<Object>>() {

                @Override
                public void accept(Observable<Object> w) throws Throwable {
                    // avoid abandonment
                    w.subscribe(Functions.emptyConsumer(), Functions.emptyConsumer());
                }
            }).to(TestHelper.<Observable<Object>>testConsumer());
            to.assertValueCount(1).assertError(TestException.class).assertErrorMessage("main").assertNotComplete();
            ref.get().onError(new TestException("inner"));
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "inner");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void mainCompleteBoundaryErrorRace() {
        final TestException ex = new TestException();
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final AtomicReference<Observer<? super Object>> refMain = new AtomicReference<>();
                final AtomicReference<Observer<? super Object>> ref = new AtomicReference<>();
                TestObserverEx<Observable<Object>> to = new Observable<Object>() {

                    @Override
                    protected void subscribeActual(Observer<? super Object> observer) {
                        observer.onSubscribe(Disposable.empty());
                        refMain.set(observer);
                    }
                }.window(new Observable<Object>() {

                    @Override
                    protected void subscribeActual(Observer<? super Object> observer) {
                        observer.onSubscribe(Disposable.empty());
                        ref.set(observer);
                    }
                }).to(TestHelper.<Observable<Object>>testConsumer());
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        refMain.get().onComplete();
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ref.get().onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertValueCount(1).assertTerminated();
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void mainNextBoundaryNextRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final AtomicReference<Observer<? super Object>> refMain = new AtomicReference<>();
            final AtomicReference<Observer<? super Object>> ref = new AtomicReference<>();
            TestObserver<Observable<Object>> to = new Observable<Object>() {

                @Override
                protected void subscribeActual(Observer<? super Object> observer) {
                    observer.onSubscribe(Disposable.empty());
                    refMain.set(observer);
                }
            }.window(new Observable<Object>() {

                @Override
                protected void subscribeActual(Observer<? super Object> observer) {
                    observer.onSubscribe(Disposable.empty());
                    ref.set(observer);
                }
            }).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    refMain.get().onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ref.get().onNext(1);
                }
            };
            TestHelper.race(r1, r2);
            to.assertValueCount(2).assertNotComplete().assertNoErrors();
        }
    }

    @Test
    public void takeOneAnotherBoundary() {
        final AtomicReference<Observer<? super Object>> refMain = new AtomicReference<>();
        final AtomicReference<Observer<? super Object>> ref = new AtomicReference<>();
        TestObserverEx<Observable<Object>> to = new Observable<Object>() {

            @Override
            protected void subscribeActual(Observer<? super Object> observer) {
                observer.onSubscribe(Disposable.empty());
                refMain.set(observer);
            }
        }.window(new Observable<Object>() {

            @Override
            protected void subscribeActual(Observer<? super Object> observer) {
                observer.onSubscribe(Disposable.empty());
                ref.set(observer);
            }
        }).to(TestHelper.<Observable<Object>>testConsumer());
        to.assertValueCount(1).assertNotTerminated().dispose();
        ref.get().onNext(1);
        to.assertValueCount(1).assertNotTerminated();
    }

    @Test
    public void disposeMainBoundaryCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final AtomicReference<Observer<? super Object>> refMain = new AtomicReference<>();
            final AtomicReference<Observer<? super Object>> ref = new AtomicReference<>();
            final TestObserver<Observable<Object>> to = new Observable<Object>() {

                @Override
                protected void subscribeActual(Observer<? super Object> observer) {
                    observer.onSubscribe(Disposable.empty());
                    refMain.set(observer);
                }
            }.window(new Observable<Object>() {

                @Override
                protected void subscribeActual(Observer<? super Object> observer) {
                    final AtomicInteger counter = new AtomicInteger();
                    observer.onSubscribe(new Disposable() {

                        @Override
                        public void dispose() {
                            // about a microsecond
                            for (int i = 0; i < 100; i++) {
                                counter.incrementAndGet();
                            }
                        }

                        @Override
                        public boolean isDisposed() {
                            return false;
                        }
                    });
                    ref.set(observer);
                }
            }).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    Observer<Object> o = ref.get();
                    o.onNext(1);
                    o.onComplete();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    @SuppressUndeliverable
    public void disposeMainBoundaryErrorRace() {
        final TestException ex = new TestException();
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final AtomicReference<Observer<? super Object>> refMain = new AtomicReference<>();
            final AtomicReference<Observer<? super Object>> ref = new AtomicReference<>();
            final TestObserver<Observable<Object>> to = new Observable<Object>() {

                @Override
                protected void subscribeActual(Observer<? super Object> observer) {
                    observer.onSubscribe(Disposable.empty());
                    refMain.set(observer);
                }
            }.window(new Observable<Object>() {

                @Override
                protected void subscribeActual(Observer<? super Object> observer) {
                    final AtomicInteger counter = new AtomicInteger();
                    observer.onSubscribe(new Disposable() {

                        @Override
                        public void dispose() {
                            // about a microsecond
                            for (int i = 0; i < 100; i++) {
                                counter.incrementAndGet();
                            }
                        }

                        @Override
                        public boolean isDisposed() {
                            return false;
                        }
                    });
                    ref.set(observer);
                }
            }).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    Observer<Object> o = ref.get();
                    o.onNext(1);
                    o.onError(ex);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void cancellingWindowCancelsUpstream() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.window(Observable.<Integer>never()).take(1).flatMap(new Function<Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> w) throws Throwable {
                return w.take(1);
            }
        }).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertResult(1);
        assertFalse("Subject still has observers!", ps.hasObservers());
    }

    @Test
    public void windowAbandonmentCancelsUpstream() {
        PublishSubject<Integer> ps = PublishSubject.create();
        final AtomicReference<Observable<Integer>> inner = new AtomicReference<>();
        TestObserver<Observable<Integer>> to = ps.window(Observable.<Integer>never()).doOnNext(new Consumer<Observable<Integer>>() {

            @Override
            public void accept(Observable<Integer> v) throws Throwable {
                inner.set(v);
            }
        }).test();
        assertTrue(ps.hasObservers());
        to.assertValueCount(1);
        ps.onNext(1);
        assertTrue(ps.hasObservers());
        to.dispose();
        to.assertValueCount(1).assertNoErrors().assertNotComplete();
        assertFalse("Subject still has observers!", ps.hasObservers());
        inner.get().test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableWindowWithObservableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowViaObservableNormal1() throws java.lang.Throwable {
            this.payloads.windowViaObservableNormal1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowViaObservableBoundaryCompletes() throws java.lang.Throwable {
            this.payloads.windowViaObservableBoundaryCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowViaObservableBoundaryThrows() throws java.lang.Throwable {
            this.payloads.windowViaObservableBoundaryThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowViaObservableSourceThrows() throws java.lang.Throwable {
            this.payloads.windowViaObservableSourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryDispose() throws java.lang.Throwable {
            this.payloads.boundaryDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryOnError() throws java.lang.Throwable {
            this.payloads.boundaryOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerBadSource() throws java.lang.Throwable {
            this.payloads.innerBadSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrant() throws java.lang.Throwable {
            this.payloads.reentrant.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryDirectDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.boundaryDirectDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamDisposedWhenOutputsDisposed() throws java.lang.Throwable {
            this.payloads.upstreamDisposedWhenOutputsDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainAndBoundaryBothError() throws java.lang.Throwable {
            this.payloads.mainAndBoundaryBothError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompleteBoundaryErrorRace() throws java.lang.Throwable {
            this.payloads.mainCompleteBoundaryErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainNextBoundaryNextRace() throws java.lang.Throwable {
            this.payloads.mainNextBoundaryNextRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeOneAnotherBoundary() throws java.lang.Throwable {
            this.payloads.takeOneAnotherBoundary.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeMainBoundaryCompleteRace() throws java.lang.Throwable {
            this.payloads.disposeMainBoundaryCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeMainBoundaryErrorRace() throws java.lang.Throwable {
            this.payloads.disposeMainBoundaryErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellingWindowCancelsUpstream() throws java.lang.Throwable {
            this.payloads.cancellingWindowCancelsUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowAbandonmentCancelsUpstream() throws java.lang.Throwable {
            this.payloads.windowAbandonmentCancelsUpstream.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithObservableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithObservableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithObservableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithObservableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableWindowWithObservableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithObservableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableWindowWithObservableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableWindowWithObservableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement windowViaObservableNormal1;

            public org.junit.runners.model.Statement windowViaObservableBoundaryCompletes;

            public org.junit.runners.model.Statement windowViaObservableBoundaryThrows;

            public org.junit.runners.model.Statement windowViaObservableSourceThrows;

            public org.junit.runners.model.Statement boundaryDispose;

            public org.junit.runners.model.Statement boundaryOnError;

            public org.junit.runners.model.Statement innerBadSource;

            public org.junit.runners.model.Statement reentrant;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement boundaryDirectDoubleOnSubscribe;

            public org.junit.runners.model.Statement upstreamDisposedWhenOutputsDisposed;

            public org.junit.runners.model.Statement mainAndBoundaryBothError;

            public org.junit.runners.model.Statement mainCompleteBoundaryErrorRace;

            public org.junit.runners.model.Statement mainNextBoundaryNextRace;

            public org.junit.runners.model.Statement takeOneAnotherBoundary;

            public org.junit.runners.model.Statement disposeMainBoundaryCompleteRace;

            public org.junit.runners.model.Statement disposeMainBoundaryErrorRace;

            public org.junit.runners.model.Statement cancellingWindowCancelsUpstream;

            public org.junit.runners.model.Statement windowAbandonmentCancelsUpstream;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.windowViaObservableNormal1 = _ClassStatement.forPayload(ObservableWindowWithObservableTest::windowViaObservableNormal1, "windowViaObservableNormal1", this);
            this.payloads.windowViaObservableBoundaryCompletes = _ClassStatement.forPayload(ObservableWindowWithObservableTest::windowViaObservableBoundaryCompletes, "windowViaObservableBoundaryCompletes", this);
            this.payloads.windowViaObservableBoundaryThrows = _ClassStatement.forPayload(ObservableWindowWithObservableTest::windowViaObservableBoundaryThrows, "windowViaObservableBoundaryThrows", this);
            this.payloads.windowViaObservableSourceThrows = _ClassStatement.forPayload(ObservableWindowWithObservableTest::windowViaObservableSourceThrows, "windowViaObservableSourceThrows", this);
            this.payloads.boundaryDispose = _ClassStatement.forPayload(ObservableWindowWithObservableTest::boundaryDispose, "boundaryDispose", this);
            this.payloads.boundaryOnError = _ClassStatement.forPayload(ObservableWindowWithObservableTest::boundaryOnError, "boundaryOnError", this);
            this.payloads.innerBadSource = _ClassStatement.forPayload(ObservableWindowWithObservableTest::innerBadSource, "innerBadSource", this);
            this.payloads.reentrant = _ClassStatement.forPayload(ObservableWindowWithObservableTest::reentrant, "reentrant", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableWindowWithObservableTest::badSource, "badSource", this);
            this.payloads.boundaryDirectDoubleOnSubscribe = _ClassStatement.forPayload(ObservableWindowWithObservableTest::boundaryDirectDoubleOnSubscribe, "boundaryDirectDoubleOnSubscribe", this);
            this.payloads.upstreamDisposedWhenOutputsDisposed = _ClassStatement.forPayload(ObservableWindowWithObservableTest::upstreamDisposedWhenOutputsDisposed, "upstreamDisposedWhenOutputsDisposed", this);
            this.payloads.mainAndBoundaryBothError = _ClassStatement.forPayload(ObservableWindowWithObservableTest::mainAndBoundaryBothError, "mainAndBoundaryBothError", this);
            this.payloads.mainCompleteBoundaryErrorRace = _ClassStatement.forPayload(ObservableWindowWithObservableTest::mainCompleteBoundaryErrorRace, "mainCompleteBoundaryErrorRace", this);
            this.payloads.mainNextBoundaryNextRace = _ClassStatement.forPayload(ObservableWindowWithObservableTest::mainNextBoundaryNextRace, "mainNextBoundaryNextRace", this);
            this.payloads.takeOneAnotherBoundary = _ClassStatement.forPayload(ObservableWindowWithObservableTest::takeOneAnotherBoundary, "takeOneAnotherBoundary", this);
            this.payloads.disposeMainBoundaryCompleteRace = _ClassStatement.forPayload(ObservableWindowWithObservableTest::disposeMainBoundaryCompleteRace, "disposeMainBoundaryCompleteRace", this);
            this.payloads.disposeMainBoundaryErrorRace = _ClassStatement.forPayload(ObservableWindowWithObservableTest::disposeMainBoundaryErrorRace, "disposeMainBoundaryErrorRace", this);
            this.payloads.cancellingWindowCancelsUpstream = _ClassStatement.forPayload(ObservableWindowWithObservableTest::cancellingWindowCancelsUpstream, "cancellingWindowCancelsUpstream", this);
            this.payloads.windowAbandonmentCancelsUpstream = _ClassStatement.forPayload(ObservableWindowWithObservableTest::windowAbandonmentCancelsUpstream, "windowAbandonmentCancelsUpstream", this);
        }
    }
}
