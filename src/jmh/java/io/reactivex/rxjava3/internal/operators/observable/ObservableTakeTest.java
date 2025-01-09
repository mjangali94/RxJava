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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableTakeTest extends RxJavaTest {

    @Test
    public void take1() {
        Observable<String> w = Observable.fromIterable(Arrays.asList("one", "two", "three"));
        Observable<String> take = w.take(2);
        Observer<String> observer = TestHelper.mockObserver();
        take.subscribe(observer);
        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, never()).onNext("three");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void take2() {
        Observable<String> w = Observable.fromIterable(Arrays.asList("one", "two", "three"));
        Observable<String> take = w.take(1);
        Observer<String> observer = TestHelper.mockObserver();
        take.subscribe(observer);
        verify(observer, times(1)).onNext("one");
        verify(observer, never()).onNext("two");
        verify(observer, never()).onNext("three");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void takeWithError() {
        Observable.fromIterable(Arrays.asList(1, 2, 3)).take(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                throw new IllegalArgumentException("some error");
            }
        }).blockingSingle();
    }

    @Test
    public void takeWithErrorHappeningInOnNext() {
        Observable<Integer> w = Observable.fromIterable(Arrays.asList(1, 2, 3)).take(2).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                throw new IllegalArgumentException("some error");
            }
        });
        Observer<Integer> observer = TestHelper.mockObserver();
        w.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(any(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void takeWithErrorHappeningInTheLastOnNext() {
        Observable<Integer> w = Observable.fromIterable(Arrays.asList(1, 2, 3)).take(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                throw new IllegalArgumentException("some error");
            }
        });
        Observer<Integer> observer = TestHelper.mockObserver();
        w.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(any(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @SuppressUndeliverable
    public void takeDoesntLeakErrors() {
        Observable<String> source = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext("one");
                observer.onError(new Throwable("test failed"));
            }
        });
        Observer<String> observer = TestHelper.mockObserver();
        source.take(1).subscribe(observer);
        verify(observer).onSubscribe((Disposable) notNull());
        verify(observer, times(1)).onNext("one");
        // even though onError is called we take(1) so shouldn't see it
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void unsubscribeAfterTake() {
        TestObservableFunc f = new TestObservableFunc("one", "two", "three");
        Observable<String> w = Observable.unsafeCreate(f);
        Observer<String> observer = TestHelper.mockObserver();
        Observable<String> take = w.take(1);
        take.subscribe(observer);
        // wait for the Observable to complete
        try {
            f.t.join();
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        // System.out.println("TestObservable thread finished");
        verify(observer).onSubscribe((Disposable) notNull());
        verify(observer, times(1)).onNext("one");
        verify(observer, never()).onNext("two");
        verify(observer, never()).onNext("three");
        verify(observer, times(1)).onComplete();
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void unsubscribeFromSynchronousInfiniteObservable() {
        final AtomicLong count = new AtomicLong();
        INFINITE_OBSERVABLE.take(10).subscribe(new Consumer<Long>() {

            @Override
            public void accept(Long l) {
                count.set(l);
            }
        });
        assertEquals(10, count.get());
    }

    @Test
    public void multiTake() {
        final AtomicInteger count = new AtomicInteger();
        Observable.unsafeCreate(new ObservableSource<Integer>() {

            @Override
            public void subscribe(Observer<? super Integer> observer) {
                Disposable bs = Disposable.empty();
                observer.onSubscribe(bs);
                for (int i = 0; !bs.isDisposed(); i++) {
                    // System.out.println("Emit: " + i);
                    count.incrementAndGet();
                    observer.onNext(i);
                }
            }
        }).take(100).take(1).blockingForEach(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                // System.out.println("Receive: " + t1);
            }
        });
        assertEquals(1, count.get());
    }

    static class TestObservableFunc implements ObservableSource<String> {

        final String[] values;

        Thread t;

        TestObservableFunc(String... values) {
            this.values = values;
        }

        @Override
        public void subscribe(final Observer<? super String> observer) {
            observer.onSubscribe(Disposable.empty());
            // System.out.println("TestObservable subscribed to ...");
            t = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        // System.out.println("running TestObservable thread");
                        for (String s : values) {
                            // System.out.println("TestObservable onNext: " + s);
                            observer.onNext(s);
                        }
                        observer.onComplete();
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            // System.out.println("starting TestObservable thread");
            t.start();
            // System.out.println("done starting TestObservable thread");
        }
    }

    private static Observable<Long> INFINITE_OBSERVABLE = Observable.unsafeCreate(new ObservableSource<Long>() {

        @Override
        public void subscribe(Observer<? super Long> op) {
            Disposable d = Disposable.empty();
            op.onSubscribe(d);
            long l = 1;
            while (!d.isDisposed()) {
                op.onNext(l++);
            }
            op.onComplete();
        }
    });

    @Test
    public void takeObserveOn() {
        Observer<Object> o = TestHelper.mockObserver();
        TestObserver<Object> to = new TestObserver<>(o);
        INFINITE_OBSERVABLE.observeOn(Schedulers.newThread()).take(1).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors();
        verify(o).onNext(1L);
        verify(o, never()).onNext(2L);
        verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void interrupt() throws InterruptedException {
        final AtomicReference<Object> exception = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        Observable.just(1).subscribeOn(Schedulers.computation()).take(1).subscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    exception.set(e);
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }
        });
        latch.await();
        assertNull(exception.get());
    }

    @Test
    public void takeFinalValueThrows() {
        Observable<Integer> source = Observable.just(1).take(1);
        TestObserver<Integer> to = new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                throw new TestException();
            }
        };
        source.safeSubscribe(to);
        to.assertNoValues();
        to.assertError(TestException.class);
        to.assertNotComplete();
    }

    @Test
    public void reentrantTake() {
        final PublishSubject<Integer> source = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<>();
        source.take(1).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) {
                source.onNext(2);
            }
        }).subscribe(to);
        source.onNext(1);
        to.assertValue(1);
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void takeNegative() {
        try {
            Observable.just(1).take(-99);
            fail("Should have thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("count >= 0 required but it was -99", ex.getMessage());
        }
    }

    @Test
    public void takeZero() {
        Observable.just(1).take(0).test().assertResult();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().take(2));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.take(2);
            }
        });
    }

    @Test
    public void errorAfterLimitReached() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.error(new TestException()).take(0).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableTakeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take1() throws java.lang.Throwable {
            this.payloads.take1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take2() throws java.lang.Throwable {
            this.payloads.take2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeWithError() throws java.lang.Throwable {
            this.payloads.takeWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeWithErrorHappeningInOnNext() throws java.lang.Throwable {
            this.payloads.takeWithErrorHappeningInOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeWithErrorHappeningInTheLastOnNext() throws java.lang.Throwable {
            this.payloads.takeWithErrorHappeningInTheLastOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeDoesntLeakErrors() throws java.lang.Throwable {
            this.payloads.takeDoesntLeakErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeAfterTake() throws java.lang.Throwable {
            this.payloads.unsubscribeAfterTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeFromSynchronousInfiniteObservable() throws java.lang.Throwable {
            this.payloads.unsubscribeFromSynchronousInfiniteObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multiTake() throws java.lang.Throwable {
            this.payloads.multiTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeObserveOn() throws java.lang.Throwable {
            this.payloads.takeObserveOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interrupt() throws java.lang.Throwable {
            this.payloads.interrupt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeFinalValueThrows() throws java.lang.Throwable {
            this.payloads.takeFinalValueThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantTake() throws java.lang.Throwable {
            this.payloads.reentrantTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeNegative() throws java.lang.Throwable {
            this.payloads.takeNegative.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeZero() throws java.lang.Throwable {
            this.payloads.takeZero.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorAfterLimitReached() throws java.lang.Throwable {
            this.payloads.errorAfterLimitReached.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableTakeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableTakeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableTakeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement take1;

            public org.junit.runners.model.Statement take2;

            public org.junit.runners.model.Statement takeWithError;

            public org.junit.runners.model.Statement takeWithErrorHappeningInOnNext;

            public org.junit.runners.model.Statement takeWithErrorHappeningInTheLastOnNext;

            public org.junit.runners.model.Statement takeDoesntLeakErrors;

            public org.junit.runners.model.Statement unsubscribeAfterTake;

            public org.junit.runners.model.Statement unsubscribeFromSynchronousInfiniteObservable;

            public org.junit.runners.model.Statement multiTake;

            public org.junit.runners.model.Statement takeObserveOn;

            public org.junit.runners.model.Statement interrupt;

            public org.junit.runners.model.Statement takeFinalValueThrows;

            public org.junit.runners.model.Statement reentrantTake;

            public org.junit.runners.model.Statement takeNegative;

            public org.junit.runners.model.Statement takeZero;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement errorAfterLimitReached;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.take1 = _ClassStatement.forPayload(ObservableTakeTest::take1, "take1", this);
            this.payloads.take2 = _ClassStatement.forPayload(ObservableTakeTest::take2, "take2", this);
            this.payloads.takeWithError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableTakeTest::takeWithError, java.lang.IllegalArgumentException.class), "takeWithError", this);
            this.payloads.takeWithErrorHappeningInOnNext = _ClassStatement.forPayload(ObservableTakeTest::takeWithErrorHappeningInOnNext, "takeWithErrorHappeningInOnNext", this);
            this.payloads.takeWithErrorHappeningInTheLastOnNext = _ClassStatement.forPayload(ObservableTakeTest::takeWithErrorHappeningInTheLastOnNext, "takeWithErrorHappeningInTheLastOnNext", this);
            this.payloads.takeDoesntLeakErrors = _ClassStatement.forPayload(ObservableTakeTest::takeDoesntLeakErrors, "takeDoesntLeakErrors", this);
            this.payloads.unsubscribeAfterTake = _ClassStatement.forPayload(ObservableTakeTest::unsubscribeAfterTake, "unsubscribeAfterTake", this);
            this.payloads.unsubscribeFromSynchronousInfiniteObservable = _ClassStatement.forPayload(ObservableTakeTest::unsubscribeFromSynchronousInfiniteObservable, "unsubscribeFromSynchronousInfiniteObservable", this);
            this.payloads.multiTake = _ClassStatement.forPayload(ObservableTakeTest::multiTake, "multiTake", this);
            this.payloads.takeObserveOn = _ClassStatement.forPayload(ObservableTakeTest::takeObserveOn, "takeObserveOn", this);
            this.payloads.interrupt = _ClassStatement.forPayload(ObservableTakeTest::interrupt, "interrupt", this);
            this.payloads.takeFinalValueThrows = _ClassStatement.forPayload(ObservableTakeTest::takeFinalValueThrows, "takeFinalValueThrows", this);
            this.payloads.reentrantTake = _ClassStatement.forPayload(ObservableTakeTest::reentrantTake, "reentrantTake", this);
            this.payloads.takeNegative = _ClassStatement.forPayload(ObservableTakeTest::takeNegative, "takeNegative", this);
            this.payloads.takeZero = _ClassStatement.forPayload(ObservableTakeTest::takeZero, "takeZero", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableTakeTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableTakeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.errorAfterLimitReached = _ClassStatement.forPayload(ObservableTakeTest::errorAfterLimitReached, "errorAfterLimitReached", this);
        }
    }
}
