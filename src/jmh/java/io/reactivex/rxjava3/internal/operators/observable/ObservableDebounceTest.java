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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.observable.ObservableDebounceTimed.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableDebounceTest extends RxJavaTest {

    private TestScheduler scheduler;

    private Observer<String> observer;

    private Scheduler.Worker innerScheduler;

    @Before
    public void before() {
        scheduler = new TestScheduler();
        observer = TestHelper.mockObserver();
        innerScheduler = scheduler.createWorker();
    }

    @Test
    public void debounceWithCompleted() {
        Observable<String> source = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> observer) {
                observer.onSubscribe(Disposable.empty());
                // Should be skipped since "two" will arrive before the timeout expires.
                publishNext(observer, 100, "one");
                // Should be published since "three" will arrive after the timeout expires.
                publishNext(observer, 400, "two");
                // Should be skipped since onComplete will arrive before the timeout expires.
                publishNext(observer, 900, "three");
                // Should be published as soon as the timeout expires.
                publishCompleted(observer, 1000);
            }
        });
        Observable<String> sampled = source.debounce(400, TimeUnit.MILLISECONDS, scheduler);
        sampled.subscribe(observer);
        scheduler.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        InOrder inOrder = inOrder(observer);
        // must go to 800 since it must be 400 after when two is sent, which is at 400
        scheduler.advanceTimeTo(800, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext("two");
        scheduler.advanceTimeTo(1000, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void debounceNeverEmits() {
        Observable<String> source = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> observer) {
                observer.onSubscribe(Disposable.empty());
                // all should be skipped since they are happening faster than the 200ms timeout
                // Should be skipped
                publishNext(observer, 100, "a");
                // Should be skipped
                publishNext(observer, 200, "b");
                // Should be skipped
                publishNext(observer, 300, "c");
                // Should be skipped
                publishNext(observer, 400, "d");
                // Should be skipped
                publishNext(observer, 500, "e");
                // Should be skipped
                publishNext(observer, 600, "f");
                // Should be skipped
                publishNext(observer, 700, "g");
                // Should be skipped
                publishNext(observer, 800, "h");
                // Should be published as soon as the timeout expires.
                publishCompleted(observer, 900);
            }
        });
        Observable<String> sampled = source.debounce(200, TimeUnit.MILLISECONDS, scheduler);
        sampled.subscribe(observer);
        scheduler.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(0)).onNext(anyString());
        scheduler.advanceTimeTo(1000, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void debounceWithError() {
        Observable<String> source = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> observer) {
                observer.onSubscribe(Disposable.empty());
                Exception error = new TestException();
                // Should be published since "two" will arrive after the timeout expires.
                publishNext(observer, 100, "one");
                // Should be skipped since onError will arrive before the timeout expires.
                publishNext(observer, 600, "two");
                // Should be published as soon as the timeout expires.
                publishError(observer, 700, error);
            }
        });
        Observable<String> sampled = source.debounce(400, TimeUnit.MILLISECONDS, scheduler);
        sampled.subscribe(observer);
        scheduler.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        InOrder inOrder = inOrder(observer);
        // 100 + 400 means it triggers at 500
        scheduler.advanceTimeTo(500, TimeUnit.MILLISECONDS);
        inOrder.verify(observer).onNext("one");
        scheduler.advanceTimeTo(701, TimeUnit.MILLISECONDS);
        inOrder.verify(observer).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
    }

    private <T> void publishCompleted(final Observer<T> observer, long delay) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                observer.onComplete();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private <T> void publishError(final Observer<T> observer, long delay, final Exception error) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                observer.onError(error);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private <T> void publishNext(final Observer<T> observer, final long delay, final T value) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                observer.onNext(value);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    @Test
    public void debounceSelectorNormal1() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> debouncer = PublishSubject.create();
        Function<Integer, Observable<Integer>> debounceSel = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return debouncer;
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.debounce(debounceSel).subscribe(o);
        source.onNext(1);
        debouncer.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onNext(4);
        debouncer.onNext(2);
        source.onNext(5);
        source.onComplete();
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(4);
        inOrder.verify(o).onNext(5);
        inOrder.verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void debounceSelectorFuncThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        Function<Integer, Observable<Integer>> debounceSel = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                throw new TestException();
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        source.debounce(debounceSel).subscribe(o);
        source.onNext(1);
        verify(o, never()).onNext(any());
        verify(o, never()).onComplete();
        verify(o).onError(any(TestException.class));
    }

    @Test
    public void debounceSelectorObservableThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        Function<Integer, Observable<Integer>> debounceSel = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return Observable.error(new TestException());
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        source.debounce(debounceSel).subscribe(o);
        source.onNext(1);
        verify(o, never()).onNext(any());
        verify(o, never()).onComplete();
        verify(o).onError(any(TestException.class));
    }

    @Test
    public void debounceTimedLastIsNotLost() {
        PublishSubject<Integer> source = PublishSubject.create();
        Observer<Object> o = TestHelper.mockObserver();
        source.debounce(100, TimeUnit.MILLISECONDS, scheduler).subscribe(o);
        source.onNext(1);
        source.onComplete();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        verify(o).onNext(1);
        verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void debounceSelectorLastIsNotLost() {
        PublishSubject<Integer> source = PublishSubject.create();
        final PublishSubject<Integer> debouncer = PublishSubject.create();
        Function<Integer, Observable<Integer>> debounceSel = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                return debouncer;
            }
        };
        Observer<Object> o = TestHelper.mockObserver();
        source.debounce(debounceSel).subscribe(o);
        source.onNext(1);
        source.onComplete();
        debouncer.onComplete();
        verify(o).onNext(1);
        verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void debounceWithTimeBackpressure() throws InterruptedException {
        TestScheduler scheduler = new TestScheduler();
        TestObserverEx<Integer> observer = new TestObserverEx<>();
        Observable.merge(Observable.just(1), Observable.just(2).delay(10, TimeUnit.MILLISECONDS, scheduler)).debounce(20, TimeUnit.MILLISECONDS, scheduler).take(1).subscribe(observer);
        scheduler.advanceTimeBy(30, TimeUnit.MILLISECONDS);
        observer.assertValue(2);
        observer.assertTerminated();
        observer.assertNoErrors();
    }

    @Test
    public void debounceDefault() throws Exception {
        Observable.just(1).debounce(1, TimeUnit.SECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().debounce(1, TimeUnit.SECONDS, new TestScheduler()));
        TestHelper.checkDisposed(PublishSubject.create().debounce(Functions.justFunction(Observable.never())));
        Disposable d = new ObservableDebounceTimed.DebounceEmitter<>(1, 1, null);
        assertFalse(d.isDisposed());
        d.dispose();
        assertTrue(d.isDisposed());
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onComplete();
                    observer.onNext(1);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.debounce(1, TimeUnit.SECONDS, new TestScheduler()).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSourceSelector() {
        TestHelper.checkBadSourceObservable(new Function<Observable<Integer>, Object>() {

            @Override
            public Object apply(Observable<Integer> o) throws Exception {
                return o.debounce(new Function<Integer, ObservableSource<Long>>() {

                    @Override
                    public ObservableSource<Long> apply(Integer v) throws Exception {
                        return Observable.timer(1, TimeUnit.SECONDS);
                    }
                });
            }
        }, false, 1, 1, 1);
        TestHelper.checkBadSourceObservable(new Function<Observable<Integer>, Object>() {

            @Override
            public Object apply(final Observable<Integer> o) throws Exception {
                return Observable.just(1).debounce(new Function<Integer, ObservableSource<Integer>>() {

                    @Override
                    public ObservableSource<Integer> apply(Integer v) throws Exception {
                        return o;
                    }
                });
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void debounceWithEmpty() {
        Observable.just(1).debounce(Functions.justFunction(Observable.empty())).test().assertResult(1);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> o) throws Exception {
                return o.debounce(Functions.justFunction(Observable.never()));
            }
        });
    }

    @Test
    public void disposeInOnNext() {
        final TestObserver<Integer> to = new TestObserver<>();
        BehaviorSubject.createDefault(1).debounce(new Function<Integer, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Integer o) throws Exception {
                to.dispose();
                return Observable.never();
            }
        }).subscribeWith(to).assertEmpty();
        assertTrue(to.isDisposed());
    }

    @Test
    public void disposedInOnComplete() {
        final TestObserver<Integer> to = new TestObserver<>();
        new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                to.dispose();
                observer.onComplete();
            }
        }.debounce(Functions.justFunction(Observable.never())).subscribeWith(to).assertEmpty();
    }

    @Test
    public void emitLate() {
        final AtomicReference<Observer<? super Integer>> ref = new AtomicReference<>();
        TestObserver<Integer> to = Observable.range(1, 2).debounce(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer o) throws Exception {
                if (o != 1) {
                    return Observable.never();
                }
                return new Observable<Integer>() {

                    @Override
                    protected void subscribeActual(Observer<? super Integer> observer) {
                        observer.onSubscribe(Disposable.empty());
                        ref.set(observer);
                    }
                };
            }
        }).test();
        ref.get().onNext(1);
        to.assertResult(2);
    }

    @Test
    public void timedDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.debounce(1, TimeUnit.SECONDS);
            }
        });
    }

    @Test
    public void timedDisposedIgnoredBySource() {
        final TestObserver<Integer> to = new TestObserver<>();
        new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                to.dispose();
                observer.onNext(1);
                observer.onComplete();
            }
        }.debounce(1, TimeUnit.SECONDS).subscribe(to);
    }

    @Test
    public void timedLateEmit() {
        TestObserver<Integer> to = new TestObserver<>();
        DebounceTimedObserver<Integer> sub = new DebounceTimedObserver<>(to, 1, TimeUnit.SECONDS, new TestScheduler().createWorker());
        sub.onSubscribe(Disposable.empty());
        DebounceEmitter<Integer> de = new DebounceEmitter<>(1, 50, sub);
        de.run();
        de.run();
        to.assertEmpty();
    }

    @Test
    public void timedError() {
        Observable.error(new TestException()).debounce(1, TimeUnit.SECONDS).test().assertFailure(TestException.class);
    }

    @Test
    public void debounceOnEmpty() {
        Observable.empty().debounce(new Function<Object, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Object o) {
                return Observable.just(new Object());
            }
        }).subscribe();
    }

    @Test
    public void doubleOnSubscribeTime() {
        TestHelper.checkDoubleOnSubscribeObservable(o -> o.debounce(1, TimeUnit.SECONDS));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableDebounceTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceWithCompleted() throws java.lang.Throwable {
            this.payloads.debounceWithCompleted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceNeverEmits() throws java.lang.Throwable {
            this.payloads.debounceNeverEmits.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceWithError() throws java.lang.Throwable {
            this.payloads.debounceWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceSelectorNormal1() throws java.lang.Throwable {
            this.payloads.debounceSelectorNormal1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceSelectorFuncThrows() throws java.lang.Throwable {
            this.payloads.debounceSelectorFuncThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceSelectorObservableThrows() throws java.lang.Throwable {
            this.payloads.debounceSelectorObservableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceTimedLastIsNotLost() throws java.lang.Throwable {
            this.payloads.debounceTimedLastIsNotLost.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceSelectorLastIsNotLost() throws java.lang.Throwable {
            this.payloads.debounceSelectorLastIsNotLost.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceWithTimeBackpressure() throws java.lang.Throwable {
            this.payloads.debounceWithTimeBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceDefault() throws java.lang.Throwable {
            this.payloads.debounceDefault.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceSelector() throws java.lang.Throwable {
            this.payloads.badSourceSelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceWithEmpty() throws java.lang.Throwable {
            this.payloads.debounceWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeInOnNext() throws java.lang.Throwable {
            this.payloads.disposeInOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedInOnComplete() throws java.lang.Throwable {
            this.payloads.disposedInOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLate() throws java.lang.Throwable {
            this.payloads.emitLate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.timedDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedDisposedIgnoredBySource() throws java.lang.Throwable {
            this.payloads.timedDisposedIgnoredBySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedLateEmit() throws java.lang.Throwable {
            this.payloads.timedLateEmit.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedError() throws java.lang.Throwable {
            this.payloads.timedError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceOnEmpty() throws java.lang.Throwable {
            this.payloads.debounceOnEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeTime() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribeTime.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDebounceTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDebounceTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDebounceTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDebounceTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableDebounceTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDebounceTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableDebounceTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableDebounceTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement debounceWithCompleted;

            public org.junit.runners.model.Statement debounceNeverEmits;

            public org.junit.runners.model.Statement debounceWithError;

            public org.junit.runners.model.Statement debounceSelectorNormal1;

            public org.junit.runners.model.Statement debounceSelectorFuncThrows;

            public org.junit.runners.model.Statement debounceSelectorObservableThrows;

            public org.junit.runners.model.Statement debounceTimedLastIsNotLost;

            public org.junit.runners.model.Statement debounceSelectorLastIsNotLost;

            public org.junit.runners.model.Statement debounceWithTimeBackpressure;

            public org.junit.runners.model.Statement debounceDefault;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement badSourceSelector;

            public org.junit.runners.model.Statement debounceWithEmpty;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement disposeInOnNext;

            public org.junit.runners.model.Statement disposedInOnComplete;

            public org.junit.runners.model.Statement emitLate;

            public org.junit.runners.model.Statement timedDoubleOnSubscribe;

            public org.junit.runners.model.Statement timedDisposedIgnoredBySource;

            public org.junit.runners.model.Statement timedLateEmit;

            public org.junit.runners.model.Statement timedError;

            public org.junit.runners.model.Statement debounceOnEmpty;

            public org.junit.runners.model.Statement doubleOnSubscribeTime;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.debounceWithCompleted = _ClassStatement.forPayload(ObservableDebounceTest::debounceWithCompleted, "debounceWithCompleted", this);
            this.payloads.debounceNeverEmits = _ClassStatement.forPayload(ObservableDebounceTest::debounceNeverEmits, "debounceNeverEmits", this);
            this.payloads.debounceWithError = _ClassStatement.forPayload(ObservableDebounceTest::debounceWithError, "debounceWithError", this);
            this.payloads.debounceSelectorNormal1 = _ClassStatement.forPayload(ObservableDebounceTest::debounceSelectorNormal1, "debounceSelectorNormal1", this);
            this.payloads.debounceSelectorFuncThrows = _ClassStatement.forPayload(ObservableDebounceTest::debounceSelectorFuncThrows, "debounceSelectorFuncThrows", this);
            this.payloads.debounceSelectorObservableThrows = _ClassStatement.forPayload(ObservableDebounceTest::debounceSelectorObservableThrows, "debounceSelectorObservableThrows", this);
            this.payloads.debounceTimedLastIsNotLost = _ClassStatement.forPayload(ObservableDebounceTest::debounceTimedLastIsNotLost, "debounceTimedLastIsNotLost", this);
            this.payloads.debounceSelectorLastIsNotLost = _ClassStatement.forPayload(ObservableDebounceTest::debounceSelectorLastIsNotLost, "debounceSelectorLastIsNotLost", this);
            this.payloads.debounceWithTimeBackpressure = _ClassStatement.forPayload(ObservableDebounceTest::debounceWithTimeBackpressure, "debounceWithTimeBackpressure", this);
            this.payloads.debounceDefault = _ClassStatement.forPayload(ObservableDebounceTest::debounceDefault, "debounceDefault", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableDebounceTest::dispose, "dispose", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableDebounceTest::badSource, "badSource", this);
            this.payloads.badSourceSelector = _ClassStatement.forPayload(ObservableDebounceTest::badSourceSelector, "badSourceSelector", this);
            this.payloads.debounceWithEmpty = _ClassStatement.forPayload(ObservableDebounceTest::debounceWithEmpty, "debounceWithEmpty", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableDebounceTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.disposeInOnNext = _ClassStatement.forPayload(ObservableDebounceTest::disposeInOnNext, "disposeInOnNext", this);
            this.payloads.disposedInOnComplete = _ClassStatement.forPayload(ObservableDebounceTest::disposedInOnComplete, "disposedInOnComplete", this);
            this.payloads.emitLate = _ClassStatement.forPayload(ObservableDebounceTest::emitLate, "emitLate", this);
            this.payloads.timedDoubleOnSubscribe = _ClassStatement.forPayload(ObservableDebounceTest::timedDoubleOnSubscribe, "timedDoubleOnSubscribe", this);
            this.payloads.timedDisposedIgnoredBySource = _ClassStatement.forPayload(ObservableDebounceTest::timedDisposedIgnoredBySource, "timedDisposedIgnoredBySource", this);
            this.payloads.timedLateEmit = _ClassStatement.forPayload(ObservableDebounceTest::timedLateEmit, "timedLateEmit", this);
            this.payloads.timedError = _ClassStatement.forPayload(ObservableDebounceTest::timedError, "timedError", this);
            this.payloads.debounceOnEmpty = _ClassStatement.forPayload(ObservableDebounceTest::debounceOnEmpty, "debounceOnEmpty", this);
            this.payloads.doubleOnSubscribeTime = _ClassStatement.forPayload(ObservableDebounceTest::doubleOnSubscribeTime, "doubleOnSubscribeTime", this);
        }
    }
}
