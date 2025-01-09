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
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableWindowWithStartEndObservableTest extends RxJavaTest {

    private TestScheduler scheduler;

    private Scheduler.Worker innerScheduler;

    @Before
    public void before() {
        scheduler = new TestScheduler();
        innerScheduler = scheduler.createWorker();
    }

    @Test
    public void observableBasedOpenerAndCloser() {
        final List<String> list = new ArrayList<>();
        final List<List<String>> lists = new ArrayList<>();
        Observable<String> source = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> innerObserver) {
                innerObserver.onSubscribe(Disposable.empty());
                push(innerObserver, "one", 10);
                push(innerObserver, "two", 60);
                push(innerObserver, "three", 110);
                push(innerObserver, "four", 160);
                push(innerObserver, "five", 210);
                complete(innerObserver, 500);
            }
        });
        Observable<Object> openings = Observable.unsafeCreate(new ObservableSource<Object>() {

            @Override
            public void subscribe(Observer<? super Object> innerObserver) {
                innerObserver.onSubscribe(Disposable.empty());
                push(innerObserver, new Object(), 50);
                push(innerObserver, new Object(), 200);
                complete(innerObserver, 250);
            }
        });
        Function<Object, Observable<Object>> closer = new Function<Object, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Object opening) {
                return Observable.unsafeCreate(new ObservableSource<Object>() {

                    @Override
                    public void subscribe(Observer<? super Object> innerObserver) {
                        innerObserver.onSubscribe(Disposable.empty());
                        push(innerObserver, new Object(), 100);
                        complete(innerObserver, 101);
                    }
                });
            }
        };
        Observable<Observable<String>> windowed = source.window(openings, closer);
        windowed.subscribe(observeWindow(list, lists));
        scheduler.advanceTimeTo(500, TimeUnit.MILLISECONDS);
        assertEquals(2, lists.size());
        assertEquals(lists.get(0), list("two", "three"));
        assertEquals(lists.get(1), list("five"));
    }

    private List<String> list(String... args) {
        List<String> list = new ArrayList<>();
        for (String arg : args) {
            list.add(arg);
        }
        return list;
    }

    private <T> void push(final Observer<T> observer, final T value, int delay) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                observer.onNext(value);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void complete(final Observer<?> observer, int delay) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                observer.onComplete();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private Consumer<Observable<String>> observeWindow(final List<String> list, final List<List<String>> lists) {
        return new Consumer<Observable<String>>() {

            @Override
            public void accept(Observable<String> stringObservable) {
                stringObservable.subscribe(new DefaultObserver<String>() {

                    @Override
                    public void onComplete() {
                        lists.add(new ArrayList<>(list));
                        list.clear();
                    }

                    @Override
                    public void onError(Throwable e) {
                        fail(e.getMessage());
                    }

                    @Override
                    public void onNext(String args) {
                        list.add(args);
                    }
                });
            }
        };
    }

    @Test
    public void noUnsubscribeAndNoLeak() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> open = PublishSubject.create();
        final PublishSubject<Integer> close = PublishSubject.create();
        TestObserver<Observable<Integer>> to = new TestObserver<>();
        source.window(open, new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t) {
                return close;
            }
        }).doOnNext(new Consumer<Observable<Integer>>() {

            @Override
            public void accept(Observable<Integer> w) throws Throwable {
                // avoid abandonment
                w.subscribe(Functions.emptyConsumer(), Functions.emptyConsumer());
            }
        }).subscribe(to);
        open.onNext(1);
        source.onNext(1);
        assertTrue(open.hasObservers());
        assertTrue(close.hasObservers());
        close.onNext(1);
        assertFalse(close.hasObservers());
        source.onComplete();
        to.assertComplete();
        to.assertNoErrors();
        to.assertValueCount(1);
        // 2.0.2 - not anymore
        // assertTrue("Not cancelled!", ts.isCancelled());
        assertFalse(open.hasObservers());
        assertFalse(close.hasObservers());
    }

    @Test
    public void unsubscribeAll() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> open = PublishSubject.create();
        final PublishSubject<Integer> close = PublishSubject.create();
        TestObserver<Observable<Integer>> to = new TestObserver<>();
        source.window(open, new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t) {
                return close;
            }
        }).doOnNext(new Consumer<Observable<Integer>>() {

            @Override
            public void accept(Observable<Integer> w) throws Throwable {
                // avoid abandonment
                w.subscribe(Functions.emptyConsumer(), Functions.emptyConsumer());
            }
        }).subscribe(to);
        open.onNext(1);
        assertTrue(open.hasObservers());
        assertTrue(close.hasObservers());
        to.dispose();
        // Disposing the outer sequence stops the opening of new windows
        assertFalse(open.hasObservers());
        // FIXME subject has subscribers because of the open window
        assertTrue(close.hasObservers());
    }

    @Test
    public void boundarySelectorNormal() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> start = PublishSubject.create();
        final PublishSubject<Integer> end = PublishSubject.create();
        TestObserver<Integer> to = source.window(start, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return end;
            }
        }).flatMap(Functions.<Observable<Integer>>identity()).test();
        start.onNext(0);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onNext(4);
        start.onNext(1);
        source.onNext(5);
        source.onNext(6);
        end.onNext(1);
        start.onNext(2);
        TestHelper.emit(source, 7, 8);
        to.assertResult(1, 2, 3, 4, 5, 5, 6, 6, 7, 8);
    }

    @Test
    public void startError() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> start = PublishSubject.create();
        final PublishSubject<Integer> end = PublishSubject.create();
        TestObserver<Integer> to = source.window(start, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return end;
            }
        }).flatMap(Functions.<Observable<Integer>>identity()).test();
        start.onError(new TestException());
        to.assertFailure(TestException.class);
        assertFalse("Source has observers!", source.hasObservers());
        assertFalse("Start has observers!", start.hasObservers());
        assertFalse("End has observers!", end.hasObservers());
    }

    @Test
    @SuppressUndeliverable
    public void endError() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> start = PublishSubject.create();
        final PublishSubject<Integer> end = PublishSubject.create();
        TestObserver<Integer> to = source.window(start, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return end;
            }
        }).flatMap(Functions.<Observable<Integer>>identity()).test();
        start.onNext(1);
        end.onError(new TestException());
        to.assertFailure(TestException.class);
        assertFalse("Source has observers!", source.hasObservers());
        assertFalse("Start has observers!", start.hasObservers());
        assertFalse("End has observers!", end.hasObservers());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).window(Observable.just(2), Functions.justFunction(Observable.never())));
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
        ps.window(BehaviorSubject.createDefault(1), Functions.justFunction(Observable.never())).flatMap(new Function<Observable<Integer>, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Observable<Integer> v) throws Exception {
                return v;
            }
        }).subscribe(to);
        ps.onNext(1);
        to.awaitDone(1, TimeUnit.SECONDS).assertResult(1, 2);
    }

    @Test
    public void badSourceCallable() {
        TestHelper.checkBadSourceObservable(new Function<Observable<Object>, Object>() {

            @Override
            public Object apply(Observable<Object> o) throws Exception {
                return o.window(Observable.just(1), Functions.justFunction(Observable.never()));
            }
        }, false, 1, 1, (Object[]) null);
    }

    @Test
    public void windowCloseIngoresCancel() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            BehaviorSubject.createDefault(1).window(BehaviorSubject.createDefault(1), new Function<Integer, Observable<Integer>>() {

                @Override
                public Observable<Integer> apply(Integer f) throws Exception {
                    return new Observable<Integer>() {

                        @Override
                        protected void subscribeActual(Observer<? super Integer> observer) {
                            observer.onSubscribe(Disposable.empty());
                            observer.onNext(1);
                            observer.onNext(2);
                            observer.onError(new TestException());
                        }
                    };
                }
            }).doOnNext(new Consumer<Observable<Integer>>() {

                @Override
                public void accept(Observable<Integer> w) throws Throwable {
                    // avoid abandonment
                    w.subscribe(Functions.emptyConsumer(), Functions.emptyConsumer());
                }
            }).test().assertValueCount(1).assertNoErrors().assertNotComplete();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    static Observable<Integer> observableDisposed(final AtomicBoolean ref) {
        return Observable.just(1).concatWith(Observable.<Integer>never()).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                ref.set(true);
            }
        });
    }

    @Test
    public void mainAndBoundaryDisposeOnNoWindows() {
        AtomicBoolean mainDisposed = new AtomicBoolean();
        AtomicBoolean openDisposed = new AtomicBoolean();
        final AtomicBoolean closeDisposed = new AtomicBoolean();
        observableDisposed(mainDisposed).window(observableDisposed(openDisposed), new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return observableDisposed(closeDisposed);
            }
        }).doOnNext(new Consumer<Observable<Integer>>() {

            @Override
            public void accept(Observable<Integer> w) throws Throwable {
                // avoid abandonment
                w.subscribe(Functions.emptyConsumer(), Functions.emptyConsumer());
            }
        }).to(TestHelper.<Observable<Integer>>testConsumer()).assertSubscribed().assertNoErrors().assertNotComplete().dispose();
        assertTrue(mainDisposed.get());
        assertTrue(openDisposed.get());
        assertTrue(closeDisposed.get());
    }

    @Test
    public void cancellingWindowCancelsUpstream() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.window(Observable.just(1).concatWith(Observable.<Integer>never()), Functions.justFunction(Observable.never())).take(1).flatMap(new Function<Observable<Integer>, Observable<Integer>>() {

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
        TestObserver<Observable<Integer>> to = ps.window(Observable.<Integer>just(1).concatWith(Observable.<Integer>never()), Functions.justFunction(Observable.never())).doOnNext(new Consumer<Observable<Integer>>() {

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

    @Test
    public void closingIndicatorFunctionCrash() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> boundary = PublishSubject.create();
        TestObserver<Observable<Integer>> to = source.window(boundary, new Function<Integer, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Integer end) throws Throwable {
                throw new TestException();
            }
        }).test();
        to.assertEmpty();
        boundary.onNext(1);
        to.assertFailure(TestException.class);
        assertFalse(source.hasObservers());
        assertFalse(boundary.hasObservers());
    }

    @Test
    public void mainError() {
        Observable.error(new TestException()).window(Observable.never(), Functions.justFunction(Observable.never())).test().assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(o -> o.window(Observable.never(), v -> Observable.never()));
    }

    @Test
    public void openError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            TestException ex1 = new TestException();
            TestException ex2 = new TestException();
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                AtomicReference<Observer<? super Integer>> ref1 = new AtomicReference<>();
                AtomicReference<Observer<? super Integer>> ref2 = new AtomicReference<>();
                Observable<Integer> o1 = Observable.<Integer>unsafeCreate(ref1::set);
                Observable<Integer> o2 = Observable.<Integer>unsafeCreate(ref2::set);
                TestObserver<Observable<Integer>> to = BehaviorSubject.createDefault(1).window(o1, v -> o2).doOnNext(w -> w.test()).test();
                ref1.get().onSubscribe(Disposable.empty());
                ref1.get().onNext(1);
                ref2.get().onSubscribe(Disposable.empty());
                TestHelper.race(() -> ref1.get().onError(ex1), () -> ref2.get().onError(ex2));
                to.assertError(RuntimeException.class);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
                errors.clear();
            }
        });
    }

    @Test
    public void closeError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            AtomicReference<Observer<? super Integer>> ref1 = new AtomicReference<>();
            AtomicReference<Observer<? super Integer>> ref2 = new AtomicReference<>();
            Observable<Integer> o1 = Observable.<Integer>unsafeCreate(ref1::set);
            Observable<Integer> o2 = Observable.<Integer>unsafeCreate(ref2::set);
            TestObserver<Integer> to = BehaviorSubject.createDefault(1).window(o1, v -> o2).flatMap(v -> v).test();
            ref1.get().onSubscribe(Disposable.empty());
            ref1.get().onNext(1);
            ref2.get().onSubscribe(Disposable.empty());
            ref2.get().onError(new TestException());
            ref2.get().onError(new TestException());
            to.assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void upstreamFailsBeforeFirstWindow() {
        Observable.error(new TestException()).window(Observable.never(), v -> Observable.never()).test().assertFailure(TestException.class);
    }

    @Test
    public void windowOpenMainCompletes() {
        AtomicReference<Observer<? super Integer>> ref1 = new AtomicReference<>();
        PublishSubject<Object> ps = PublishSubject.create();
        Observable<Integer> o1 = Observable.<Integer>unsafeCreate(ref1::set);
        AtomicInteger counter = new AtomicInteger();
        TestObserver<Observable<Object>> to = ps.window(o1, v -> Observable.never()).doOnNext(w -> {
            if (counter.getAndIncrement() == 0) {
                ref1.get().onNext(2);
                ps.onNext(1);
                ps.onComplete();
            }
            w.test();
        }).test();
        ref1.get().onSubscribe(Disposable.empty());
        ref1.get().onNext(1);
        to.assertComplete();
    }

    @Test
    public void windowOpenMainError() {
        AtomicReference<Observer<? super Integer>> ref1 = new AtomicReference<>();
        PublishSubject<Object> ps = PublishSubject.create();
        Observable<Integer> o1 = Observable.<Integer>unsafeCreate(ref1::set);
        AtomicInteger counter = new AtomicInteger();
        TestObserver<Observable<Object>> to = ps.window(o1, v -> Observable.never()).doOnNext(w -> {
            if (counter.getAndIncrement() == 0) {
                ref1.get().onNext(2);
                ps.onNext(1);
                ps.onError(new TestException());
            }
            w.test();
        }).test();
        ref1.get().onSubscribe(Disposable.empty());
        ref1.get().onNext(1);
        to.assertError(TestException.class);
    }

    @Test
    public void windowOpenIgnoresDispose() {
        AtomicReference<Observer<? super Integer>> ref1 = new AtomicReference<>();
        PublishSubject<Object> ps = PublishSubject.create();
        Observable<Integer> o1 = Observable.<Integer>unsafeCreate(ref1::set);
        TestObserver<Observable<Object>> to = ps.window(o1, v -> Observable.never()).take(1).doOnNext(w -> {
            w.test();
        }).test();
        ref1.get().onSubscribe(Disposable.empty());
        ref1.get().onNext(1);
        ref1.get().onNext(2);
        to.assertValueCount(1);
    }

    @Test
    public void mainIgnoresCancelBeforeOnError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Observable.unsafeCreate(s -> {
                s.onSubscribe(Disposable.empty());
                s.onNext(1);
                s.onError(new IOException());
            }).window(BehaviorSubject.createDefault(1), v -> Observable.error(new TestException())).doOnNext(w -> w.test()).test().assertError(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableWindowWithStartEndObservableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableBasedOpenerAndCloser() throws java.lang.Throwable {
            this.payloads.observableBasedOpenerAndCloser.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noUnsubscribeAndNoLeak() throws java.lang.Throwable {
            this.payloads.noUnsubscribeAndNoLeak.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeAll() throws java.lang.Throwable {
            this.payloads.unsubscribeAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundarySelectorNormal() throws java.lang.Throwable {
            this.payloads.boundarySelectorNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startError() throws java.lang.Throwable {
            this.payloads.startError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_endError() throws java.lang.Throwable {
            this.payloads.endError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrant() throws java.lang.Throwable {
            this.payloads.reentrant.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceCallable() throws java.lang.Throwable {
            this.payloads.badSourceCallable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowCloseIngoresCancel() throws java.lang.Throwable {
            this.payloads.windowCloseIngoresCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainAndBoundaryDisposeOnNoWindows() throws java.lang.Throwable {
            this.payloads.mainAndBoundaryDisposeOnNoWindows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellingWindowCancelsUpstream() throws java.lang.Throwable {
            this.payloads.cancellingWindowCancelsUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowAbandonmentCancelsUpstream() throws java.lang.Throwable {
            this.payloads.windowAbandonmentCancelsUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closingIndicatorFunctionCrash() throws java.lang.Throwable {
            this.payloads.closingIndicatorFunctionCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.payloads.mainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_openError() throws java.lang.Throwable {
            this.payloads.openError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeError() throws java.lang.Throwable {
            this.payloads.closeError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamFailsBeforeFirstWindow() throws java.lang.Throwable {
            this.payloads.upstreamFailsBeforeFirstWindow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowOpenMainCompletes() throws java.lang.Throwable {
            this.payloads.windowOpenMainCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowOpenMainError() throws java.lang.Throwable {
            this.payloads.windowOpenMainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowOpenIgnoresDispose() throws java.lang.Throwable {
            this.payloads.windowOpenIgnoresDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainIgnoresCancelBeforeOnError() throws java.lang.Throwable {
            this.payloads.mainIgnoresCancelBeforeOnError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithStartEndObservableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithStartEndObservableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.before();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithStartEndObservableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithStartEndObservableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableWindowWithStartEndObservableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithStartEndObservableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableWindowWithStartEndObservableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableWindowWithStartEndObservableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement observableBasedOpenerAndCloser;

            public org.junit.runners.model.Statement noUnsubscribeAndNoLeak;

            public org.junit.runners.model.Statement unsubscribeAll;

            public org.junit.runners.model.Statement boundarySelectorNormal;

            public org.junit.runners.model.Statement startError;

            public org.junit.runners.model.Statement endError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement reentrant;

            public org.junit.runners.model.Statement badSourceCallable;

            public org.junit.runners.model.Statement windowCloseIngoresCancel;

            public org.junit.runners.model.Statement mainAndBoundaryDisposeOnNoWindows;

            public org.junit.runners.model.Statement cancellingWindowCancelsUpstream;

            public org.junit.runners.model.Statement windowAbandonmentCancelsUpstream;

            public org.junit.runners.model.Statement closingIndicatorFunctionCrash;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement openError;

            public org.junit.runners.model.Statement closeError;

            public org.junit.runners.model.Statement upstreamFailsBeforeFirstWindow;

            public org.junit.runners.model.Statement windowOpenMainCompletes;

            public org.junit.runners.model.Statement windowOpenMainError;

            public org.junit.runners.model.Statement windowOpenIgnoresDispose;

            public org.junit.runners.model.Statement mainIgnoresCancelBeforeOnError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.observableBasedOpenerAndCloser = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::observableBasedOpenerAndCloser, "observableBasedOpenerAndCloser", this);
            this.payloads.noUnsubscribeAndNoLeak = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::noUnsubscribeAndNoLeak, "noUnsubscribeAndNoLeak", this);
            this.payloads.unsubscribeAll = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::unsubscribeAll, "unsubscribeAll", this);
            this.payloads.boundarySelectorNormal = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::boundarySelectorNormal, "boundarySelectorNormal", this);
            this.payloads.startError = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::startError, "startError", this);
            this.payloads.endError = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::endError, "endError", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::dispose, "dispose", this);
            this.payloads.reentrant = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::reentrant, "reentrant", this);
            this.payloads.badSourceCallable = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::badSourceCallable, "badSourceCallable", this);
            this.payloads.windowCloseIngoresCancel = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::windowCloseIngoresCancel, "windowCloseIngoresCancel", this);
            this.payloads.mainAndBoundaryDisposeOnNoWindows = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::mainAndBoundaryDisposeOnNoWindows, "mainAndBoundaryDisposeOnNoWindows", this);
            this.payloads.cancellingWindowCancelsUpstream = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::cancellingWindowCancelsUpstream, "cancellingWindowCancelsUpstream", this);
            this.payloads.windowAbandonmentCancelsUpstream = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::windowAbandonmentCancelsUpstream, "windowAbandonmentCancelsUpstream", this);
            this.payloads.closingIndicatorFunctionCrash = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::closingIndicatorFunctionCrash, "closingIndicatorFunctionCrash", this);
            this.payloads.mainError = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::mainError, "mainError", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.openError = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::openError, "openError", this);
            this.payloads.closeError = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::closeError, "closeError", this);
            this.payloads.upstreamFailsBeforeFirstWindow = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::upstreamFailsBeforeFirstWindow, "upstreamFailsBeforeFirstWindow", this);
            this.payloads.windowOpenMainCompletes = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::windowOpenMainCompletes, "windowOpenMainCompletes", this);
            this.payloads.windowOpenMainError = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::windowOpenMainError, "windowOpenMainError", this);
            this.payloads.windowOpenIgnoresDispose = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::windowOpenIgnoresDispose, "windowOpenIgnoresDispose", this);
            this.payloads.mainIgnoresCancelBeforeOnError = _ClassStatement.forPayload(ObservableWindowWithStartEndObservableTest::mainIgnoresCancelBeforeOnError, "mainIgnoresCancelBeforeOnError", this);
        }
    }
}
