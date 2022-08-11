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
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
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

public class ObservableRepeatTest extends RxJavaTest {

    @Test
    public void repetition() {
        int num = 10;
        final AtomicInteger count = new AtomicInteger();
        int value = Observable.unsafeCreate(new ObservableSource<Integer>() {

            @Override
            public void subscribe(final Observer<? super Integer> o) {
                o.onNext(count.incrementAndGet());
                o.onComplete();
            }
        }).repeat().subscribeOn(Schedulers.computation()).take(num).blockingLast();
        assertEquals(num, value);
    }

    @Test
    public void repeatTake() {
        Observable<Integer> xs = Observable.just(1, 2);
        Object[] ys = xs.repeat().subscribeOn(Schedulers.newThread()).take(4).toList().blockingGet().toArray();
        assertArrayEquals(new Object[] { 1, 2, 1, 2 }, ys);
    }

    @Test
    public void noStackOverFlow() {
        Observable.just(1).repeat().subscribeOn(Schedulers.newThread()).take(100000).blockingLast();
    }

    @Test
    public void repeatTakeWithSubscribeOn() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        Observable<Integer> oi = Observable.unsafeCreate(new ObservableSource<Integer>() {

            @Override
            public void subscribe(Observer<? super Integer> sub) {
                sub.onSubscribe(Disposable.empty());
                counter.incrementAndGet();
                sub.onNext(1);
                sub.onNext(2);
                sub.onComplete();
            }
        }).subscribeOn(Schedulers.newThread());
        Object[] ys = oi.repeat().subscribeOn(Schedulers.newThread()).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return t1;
            }
        }).take(4).toList().blockingGet().toArray();
        assertEquals(2, counter.get());
        assertArrayEquals(new Object[] { 1, 2, 1, 2 }, ys);
    }

    @Test
    public void repeatAndTake() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.just(1).repeat().take(10).subscribe(o);
        verify(o, times(10)).onNext(1);
        verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void repeatLimited() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.just(1).repeat(10).subscribe(o);
        verify(o, times(10)).onNext(1);
        verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void repeatError() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.error(new TestException()).repeat(10).subscribe(o);
        verify(o).onError(any(TestException.class));
        verify(o, never()).onNext(any());
        verify(o, never()).onComplete();
    }

    @Test
    public void repeatZero() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.just(1).repeat(0).subscribe(o);
        verify(o).onComplete();
        verify(o, never()).onNext(any());
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void repeatOne() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.just(1).repeat(1).subscribe(o);
        verify(o).onComplete();
        verify(o, times(1)).onNext(any());
        verify(o, never()).onError(any(Throwable.class));
    }

    /**
     * Issue #2587.
     */
    @Test
    public void repeatAndDistinctUnbounded() {
        Observable<Integer> src = Observable.fromIterable(Arrays.asList(1, 2, 3, 4, 5)).take(3).repeat(3).distinct();
        TestObserverEx<Integer> to = new TestObserverEx<>();
        src.subscribe(to);
        to.assertNoErrors();
        to.assertTerminated();
        to.assertValues(1, 2, 3);
    }

    /**
     * Issue #2844: wrong target of request.
     */
    @Test
    public void repeatRetarget() {
        final List<Integer> concatBase = new ArrayList<>();
        TestObserver<Integer> to = new TestObserver<>();
        Observable.just(1, 2).repeat(5).concatMap(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer x) {
                System.out.println("testRepeatRetarget -> " + x);
                concatBase.add(x);
                return Observable.<Integer>empty().delay(200, TimeUnit.MILLISECONDS);
            }
        }).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors();
        to.assertNoValues();
        assertEquals(Arrays.asList(1, 2, 1, 2, 1, 2, 1, 2, 1, 2), concatBase);
    }

    @Test
    public void repeatUntil() {
        Observable.just(1).repeatUntil(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return false;
            }
        }).take(5).test().assertResult(1, 1, 1, 1, 1);
    }

    @Test
    public void repeatLongPredicateInvalid() {
        try {
            Observable.just(1).repeat(-99);
            fail("Should have thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("times >= 0 required but it was -99", ex.getMessage());
        }
    }

    @Test
    public void repeatUntilError() {
        Observable.error(new TestException()).repeatUntil(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return true;
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void repeatUntilFalse() {
        Observable.just(1).repeatUntil(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return true;
            }
        }).test().assertResult(1);
    }

    @Test
    public void repeatUntilSupplierCrash() {
        Observable.just(1).repeatUntil(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void shouldDisposeInnerObservable() {
        final PublishSubject<Object> subject = PublishSubject.create();
        final Disposable disposable = Observable.just("Leak").repeatWhen(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> completions) throws Exception {
                return completions.switchMap(new Function<Object, ObservableSource<Object>>() {

                    @Override
                    public ObservableSource<Object> apply(Object ignore) throws Exception {
                        return subject;
                    }
                });
            }
        }).subscribe();
        assertTrue(subject.hasObservers());
        disposable.dispose();
        assertFalse(subject.hasObservers());
    }

    @Test
    public void repeatWhen() {
        Observable.error(new TestException()).repeatWhen(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> v) throws Exception {
                return v.delay(10, TimeUnit.SECONDS);
            }
        }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void whenTake() {
        Observable.range(1, 3).repeatWhen(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> handler) throws Exception {
                return handler.take(2);
            }
        }).test().assertResult(1, 2, 3, 1, 2, 3);
    }

    @Test
    public void handlerError() {
        Observable.range(1, 3).repeatWhen(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> v) throws Exception {
                return v.map(new Function<Object, Object>() {

                    @Override
                    public Object apply(Object w) throws Exception {
                        throw new TestException();
                    }
                });
            }
        }).test().assertFailure(TestException.class, 1, 2, 3);
    }

    @Test
    public void noCancelPreviousRepeat() {
        final AtomicInteger counter = new AtomicInteger();
        Observable<Integer> source = Observable.just(1).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        source.repeat(5).test().assertResult(1, 1, 1, 1, 1);
        assertEquals(0, counter.get());
    }

    @Test
    public void noCancelPreviousRepeatUntil() {
        final AtomicInteger counter = new AtomicInteger();
        Observable<Integer> source = Observable.just(1).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        final AtomicInteger times = new AtomicInteger();
        source.repeatUntil(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return times.getAndIncrement() == 4;
            }
        }).test().assertResult(1, 1, 1, 1, 1);
        assertEquals(0, counter.get());
    }

    @Test
    public void noCancelPreviousRepeatWhen() {
        final AtomicInteger counter = new AtomicInteger();
        Observable<Integer> source = Observable.just(1).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        final AtomicInteger times = new AtomicInteger();
        source.repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {

            @Override
            public ObservableSource<?> apply(Observable<Object> e) throws Exception {
                return e.takeWhile(new Predicate<Object>() {

                    @Override
                    public boolean test(Object v) throws Exception {
                        return times.getAndIncrement() < 4;
                    }
                });
            }
        }).test().assertResult(1, 1, 1, 1, 1);
        assertEquals(0, counter.get());
    }

    @Test
    public void repeatFloodNoSubscriptionError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final PublishSubject<Integer> source = PublishSubject.create();
            final PublishSubject<Integer> signaller = PublishSubject.create();
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                TestObserver<Integer> to = source.take(1).repeatWhen(new Function<Observable<Object>, ObservableSource<Integer>>() {

                    @Override
                    public ObservableSource<Integer> apply(Observable<Object> v) throws Exception {
                        return signaller;
                    }
                }).test();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                            source.onNext(1);
                        }
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                            signaller.onNext(1);
                        }
                    }
                };
                TestHelper.race(r1, r2);
                to.dispose();
            }
            if (!errors.isEmpty()) {
                for (Throwable e : errors) {
                    e.printStackTrace();
                }
                fail(errors + "");
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableRepeatTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repetition() throws java.lang.Throwable {
            this.payloads.repetition.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatTake() throws java.lang.Throwable {
            this.payloads.repeatTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noStackOverFlow() throws java.lang.Throwable {
            this.payloads.noStackOverFlow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatTakeWithSubscribeOn() throws java.lang.Throwable {
            this.payloads.repeatTakeWithSubscribeOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatAndTake() throws java.lang.Throwable {
            this.payloads.repeatAndTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatLimited() throws java.lang.Throwable {
            this.payloads.repeatLimited.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatError() throws java.lang.Throwable {
            this.payloads.repeatError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatZero() throws java.lang.Throwable {
            this.payloads.repeatZero.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatOne() throws java.lang.Throwable {
            this.payloads.repeatOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatAndDistinctUnbounded() throws java.lang.Throwable {
            this.payloads.repeatAndDistinctUnbounded.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatRetarget() throws java.lang.Throwable {
            this.payloads.repeatRetarget.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatUntil() throws java.lang.Throwable {
            this.payloads.repeatUntil.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatLongPredicateInvalid() throws java.lang.Throwable {
            this.payloads.repeatLongPredicateInvalid.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatUntilError() throws java.lang.Throwable {
            this.payloads.repeatUntilError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatUntilFalse() throws java.lang.Throwable {
            this.payloads.repeatUntilFalse.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatUntilSupplierCrash() throws java.lang.Throwable {
            this.payloads.repeatUntilSupplierCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDisposeInnerObservable() throws java.lang.Throwable {
            this.payloads.shouldDisposeInnerObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatWhen() throws java.lang.Throwable {
            this.payloads.repeatWhen.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_whenTake() throws java.lang.Throwable {
            this.payloads.whenTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_handlerError() throws java.lang.Throwable {
            this.payloads.handlerError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPreviousRepeat() throws java.lang.Throwable {
            this.payloads.noCancelPreviousRepeat.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPreviousRepeatUntil() throws java.lang.Throwable {
            this.payloads.noCancelPreviousRepeatUntil.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPreviousRepeatWhen() throws java.lang.Throwable {
            this.payloads.noCancelPreviousRepeatWhen.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatFloodNoSubscriptionError() throws java.lang.Throwable {
            this.payloads.repeatFloodNoSubscriptionError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRepeatTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRepeatTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRepeatTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRepeatTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableRepeatTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRepeatTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableRepeatTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableRepeatTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement repetition;

            public org.junit.runners.model.Statement repeatTake;

            public org.junit.runners.model.Statement noStackOverFlow;

            public org.junit.runners.model.Statement repeatTakeWithSubscribeOn;

            public org.junit.runners.model.Statement repeatAndTake;

            public org.junit.runners.model.Statement repeatLimited;

            public org.junit.runners.model.Statement repeatError;

            public org.junit.runners.model.Statement repeatZero;

            public org.junit.runners.model.Statement repeatOne;

            public org.junit.runners.model.Statement repeatAndDistinctUnbounded;

            public org.junit.runners.model.Statement repeatRetarget;

            public org.junit.runners.model.Statement repeatUntil;

            public org.junit.runners.model.Statement repeatLongPredicateInvalid;

            public org.junit.runners.model.Statement repeatUntilError;

            public org.junit.runners.model.Statement repeatUntilFalse;

            public org.junit.runners.model.Statement repeatUntilSupplierCrash;

            public org.junit.runners.model.Statement shouldDisposeInnerObservable;

            public org.junit.runners.model.Statement repeatWhen;

            public org.junit.runners.model.Statement whenTake;

            public org.junit.runners.model.Statement handlerError;

            public org.junit.runners.model.Statement noCancelPreviousRepeat;

            public org.junit.runners.model.Statement noCancelPreviousRepeatUntil;

            public org.junit.runners.model.Statement noCancelPreviousRepeatWhen;

            public org.junit.runners.model.Statement repeatFloodNoSubscriptionError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.repetition = _ClassStatement.forPayload(ObservableRepeatTest::repetition, "repetition", this);
            this.payloads.repeatTake = _ClassStatement.forPayload(ObservableRepeatTest::repeatTake, "repeatTake", this);
            this.payloads.noStackOverFlow = _ClassStatement.forPayload(ObservableRepeatTest::noStackOverFlow, "noStackOverFlow", this);
            this.payloads.repeatTakeWithSubscribeOn = _ClassStatement.forPayload(ObservableRepeatTest::repeatTakeWithSubscribeOn, "repeatTakeWithSubscribeOn", this);
            this.payloads.repeatAndTake = _ClassStatement.forPayload(ObservableRepeatTest::repeatAndTake, "repeatAndTake", this);
            this.payloads.repeatLimited = _ClassStatement.forPayload(ObservableRepeatTest::repeatLimited, "repeatLimited", this);
            this.payloads.repeatError = _ClassStatement.forPayload(ObservableRepeatTest::repeatError, "repeatError", this);
            this.payloads.repeatZero = _ClassStatement.forPayload(ObservableRepeatTest::repeatZero, "repeatZero", this);
            this.payloads.repeatOne = _ClassStatement.forPayload(ObservableRepeatTest::repeatOne, "repeatOne", this);
            this.payloads.repeatAndDistinctUnbounded = _ClassStatement.forPayload(ObservableRepeatTest::repeatAndDistinctUnbounded, "repeatAndDistinctUnbounded", this);
            this.payloads.repeatRetarget = _ClassStatement.forPayload(ObservableRepeatTest::repeatRetarget, "repeatRetarget", this);
            this.payloads.repeatUntil = _ClassStatement.forPayload(ObservableRepeatTest::repeatUntil, "repeatUntil", this);
            this.payloads.repeatLongPredicateInvalid = _ClassStatement.forPayload(ObservableRepeatTest::repeatLongPredicateInvalid, "repeatLongPredicateInvalid", this);
            this.payloads.repeatUntilError = _ClassStatement.forPayload(ObservableRepeatTest::repeatUntilError, "repeatUntilError", this);
            this.payloads.repeatUntilFalse = _ClassStatement.forPayload(ObservableRepeatTest::repeatUntilFalse, "repeatUntilFalse", this);
            this.payloads.repeatUntilSupplierCrash = _ClassStatement.forPayload(ObservableRepeatTest::repeatUntilSupplierCrash, "repeatUntilSupplierCrash", this);
            this.payloads.shouldDisposeInnerObservable = _ClassStatement.forPayload(ObservableRepeatTest::shouldDisposeInnerObservable, "shouldDisposeInnerObservable", this);
            this.payloads.repeatWhen = _ClassStatement.forPayload(ObservableRepeatTest::repeatWhen, "repeatWhen", this);
            this.payloads.whenTake = _ClassStatement.forPayload(ObservableRepeatTest::whenTake, "whenTake", this);
            this.payloads.handlerError = _ClassStatement.forPayload(ObservableRepeatTest::handlerError, "handlerError", this);
            this.payloads.noCancelPreviousRepeat = _ClassStatement.forPayload(ObservableRepeatTest::noCancelPreviousRepeat, "noCancelPreviousRepeat", this);
            this.payloads.noCancelPreviousRepeatUntil = _ClassStatement.forPayload(ObservableRepeatTest::noCancelPreviousRepeatUntil, "noCancelPreviousRepeatUntil", this);
            this.payloads.noCancelPreviousRepeatWhen = _ClassStatement.forPayload(ObservableRepeatTest::noCancelPreviousRepeatWhen, "noCancelPreviousRepeatWhen", this);
            this.payloads.repeatFloodNoSubscriptionError = _ClassStatement.forPayload(ObservableRepeatTest::repeatFloodNoSubscriptionError, "repeatFloodNoSubscriptionError", this);
        }
    }
}
