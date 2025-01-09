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

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.BiPredicate;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableSequenceEqualTest extends RxJavaTest {

    @Test
    public void observable1() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.just("one", "two", "three")).toObservable();
        verifyResult(o, true);
    }

    @Test
    public void observable2() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.just("one", "two", "three", "four")).toObservable();
        verifyResult(o, false);
    }

    @Test
    public void observable3() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three", "four"), Observable.just("one", "two", "three")).toObservable();
        verifyResult(o, false);
    }

    @Test
    public void withError1Observable() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.concat(Observable.just("one"), Observable.<String>error(new TestException())), Observable.just("one", "two", "three")).toObservable();
        verifyError(o);
    }

    @Test
    public void withError2Observable() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.concat(Observable.just("one"), Observable.<String>error(new TestException()))).toObservable();
        verifyError(o);
    }

    @Test
    public void withError3Observable() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.concat(Observable.just("one"), Observable.<String>error(new TestException())), Observable.concat(Observable.just("one"), Observable.<String>error(new TestException()))).toObservable();
        verifyError(o);
    }

    @Test
    public void withEmpty1Observable() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.<String>empty(), Observable.just("one", "two", "three")).toObservable();
        verifyResult(o, false);
    }

    @Test
    public void withEmpty2Observable() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.<String>empty()).toObservable();
        verifyResult(o, false);
    }

    @Test
    public void withEmpty3Observable() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.<String>empty(), Observable.<String>empty()).toObservable();
        verifyResult(o, true);
    }

    @Test
    public void withEqualityErrorObservable() {
        Observable<Boolean> o = Observable.sequenceEqual(Observable.just("one"), Observable.just("one"), new BiPredicate<String, String>() {

            @Override
            public boolean test(String t1, String t2) {
                throw new TestException();
            }
        }).toObservable();
        verifyError(o);
    }

    private void verifyResult(Single<Boolean> o, boolean result) {
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(result);
        inOrder.verifyNoMoreInteractions();
    }

    private void verifyError(Observable<Boolean> observable) {
        Observer<Boolean> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(TestException.class));
        inOrder.verifyNoMoreInteractions();
    }

    private void verifyError(Single<Boolean> single) {
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(TestException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void prefetchObservable() {
        Observable.sequenceEqual(Observable.range(1, 20), Observable.range(1, 20), 2).toObservable().test().assertResult(true);
    }

    @Test
    public void disposedObservable() {
        TestHelper.checkDisposed(Observable.sequenceEqual(Observable.just(1), Observable.just(2)).toObservable());
    }

    @Test
    public void one() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.just("one", "two", "three"));
        verifyResult(o, true);
    }

    @Test
    public void two() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.just("one", "two", "three", "four"));
        verifyResult(o, false);
    }

    @Test
    public void three() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three", "four"), Observable.just("one", "two", "three"));
        verifyResult(o, false);
    }

    @Test
    public void withError1() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.concat(Observable.just("one"), Observable.<String>error(new TestException())), Observable.just("one", "two", "three"));
        verifyError(o);
    }

    @Test
    public void withError2() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.concat(Observable.just("one"), Observable.<String>error(new TestException())));
        verifyError(o);
    }

    @Test
    public void withError3() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.concat(Observable.just("one"), Observable.<String>error(new TestException())), Observable.concat(Observable.just("one"), Observable.<String>error(new TestException())));
        verifyError(o);
    }

    @Test
    public void withEmpty1() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.<String>empty(), Observable.just("one", "two", "three"));
        verifyResult(o, false);
    }

    @Test
    public void withEmpty2() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.just("one", "two", "three"), Observable.<String>empty());
        verifyResult(o, false);
    }

    @Test
    public void withEmpty3() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.<String>empty(), Observable.<String>empty());
        verifyResult(o, true);
    }

    @Test
    public void withEqualityError() {
        Single<Boolean> o = Observable.sequenceEqual(Observable.just("one"), Observable.just("one"), new BiPredicate<String, String>() {

            @Override
            public boolean test(String t1, String t2) {
                throw new TestException();
            }
        });
        verifyError(o);
    }

    private void verifyResult(Observable<Boolean> o, boolean result) {
        Observer<Boolean> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext(result);
        inOrder.verify(observer).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void prefetch() {
        Observable.sequenceEqual(Observable.range(1, 20), Observable.range(1, 20), 2).test().assertResult(true);
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Observable.sequenceEqual(Observable.just(1), Observable.just(2)));
    }

    @Test
    public void simpleInequal() {
        Observable.sequenceEqual(Observable.just(1), Observable.just(2)).test().assertResult(false);
    }

    @Test
    public void simpleInequalObservable() {
        Observable.sequenceEqual(Observable.just(1), Observable.just(2)).toObservable().test().assertResult(false);
    }

    @Test
    public void onNextCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Boolean> to = Observable.sequenceEqual(Observable.never(), ps).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ps.onNext(1);
                }
            };
            TestHelper.race(r1, r2);
            to.assertEmpty();
        }
    }

    @Test
    public void onNextCancelRaceObservable() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Boolean> to = Observable.sequenceEqual(Observable.never(), ps).toObservable().test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ps.onNext(1);
                }
            };
            TestHelper.race(r1, r2);
            to.assertEmpty();
        }
    }

    @Test
    public void firstCompletesBeforeSecond() {
        Observable.sequenceEqual(Observable.just(1), Observable.empty()).test().assertResult(false);
    }

    @Test
    public void secondCompletesBeforeFirst() {
        Observable.sequenceEqual(Observable.empty(), Observable.just(1)).test().assertResult(false);
    }

    @Test
    public void bothEmpty() {
        Observable.sequenceEqual(Observable.empty(), Observable.empty()).test().assertResult(true);
    }

    @Test
    public void bothJust() {
        Observable.sequenceEqual(Observable.just(1), Observable.just(1)).test().assertResult(true);
    }

    @Test
    public void bothCompleteWhileComparing() {
        PublishSubject<Integer> ps1 = PublishSubject.create();
        PublishSubject<Integer> ps2 = PublishSubject.create();
        TestObserver<Boolean> to = Observable.sequenceEqual(ps1, ps2, (a, b) -> {
            ps1.onNext(1);
            ps1.onComplete();
            ps2.onNext(1);
            ps2.onComplete();
            return a.equals(b);
        }).test();
        ps1.onNext(0);
        ps2.onNext(0);
        to.assertResult(true);
    }

    @Test
    public void bothCompleteWhileComparingAsObservable() {
        PublishSubject<Integer> ps1 = PublishSubject.create();
        PublishSubject<Integer> ps2 = PublishSubject.create();
        TestObserver<Boolean> to = Observable.sequenceEqual(ps1, ps2, (a, b) -> {
            ps1.onNext(1);
            ps1.onComplete();
            ps2.onNext(1);
            ps2.onComplete();
            return a.equals(b);
        }).toObservable().test();
        ps1.onNext(0);
        ps2.onNext(0);
        to.assertResult(true);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableSequenceEqualTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observable1() throws java.lang.Throwable {
            this.payloads.observable1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observable2() throws java.lang.Throwable {
            this.payloads.observable2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observable3() throws java.lang.Throwable {
            this.payloads.observable3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError1Observable() throws java.lang.Throwable {
            this.payloads.withError1Observable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError2Observable() throws java.lang.Throwable {
            this.payloads.withError2Observable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError3Observable() throws java.lang.Throwable {
            this.payloads.withError3Observable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty1Observable() throws java.lang.Throwable {
            this.payloads.withEmpty1Observable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty2Observable() throws java.lang.Throwable {
            this.payloads.withEmpty2Observable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty3Observable() throws java.lang.Throwable {
            this.payloads.withEmpty3Observable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEqualityErrorObservable() throws java.lang.Throwable {
            this.payloads.withEqualityErrorObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_prefetchObservable() throws java.lang.Throwable {
            this.payloads.prefetchObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedObservable() throws java.lang.Throwable {
            this.payloads.disposedObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_one() throws java.lang.Throwable {
            this.payloads.one.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_two() throws java.lang.Throwable {
            this.payloads.two.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_three() throws java.lang.Throwable {
            this.payloads.three.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError1() throws java.lang.Throwable {
            this.payloads.withError1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError2() throws java.lang.Throwable {
            this.payloads.withError2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError3() throws java.lang.Throwable {
            this.payloads.withError3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty1() throws java.lang.Throwable {
            this.payloads.withEmpty1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty2() throws java.lang.Throwable {
            this.payloads.withEmpty2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty3() throws java.lang.Throwable {
            this.payloads.withEmpty3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEqualityError() throws java.lang.Throwable {
            this.payloads.withEqualityError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_prefetch() throws java.lang.Throwable {
            this.payloads.prefetch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleInequal() throws java.lang.Throwable {
            this.payloads.simpleInequal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleInequalObservable() throws java.lang.Throwable {
            this.payloads.simpleInequalObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextCancelRace() throws java.lang.Throwable {
            this.payloads.onNextCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextCancelRaceObservable() throws java.lang.Throwable {
            this.payloads.onNextCancelRaceObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstCompletesBeforeSecond() throws java.lang.Throwable {
            this.payloads.firstCompletesBeforeSecond.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_secondCompletesBeforeFirst() throws java.lang.Throwable {
            this.payloads.secondCompletesBeforeFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothEmpty() throws java.lang.Throwable {
            this.payloads.bothEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothJust() throws java.lang.Throwable {
            this.payloads.bothJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothCompleteWhileComparing() throws java.lang.Throwable {
            this.payloads.bothCompleteWhileComparing.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothCompleteWhileComparingAsObservable() throws java.lang.Throwable {
            this.payloads.bothCompleteWhileComparingAsObservable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSequenceEqualTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSequenceEqualTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSequenceEqualTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSequenceEqualTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableSequenceEqualTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSequenceEqualTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableSequenceEqualTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableSequenceEqualTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement observable1;

            public org.junit.runners.model.Statement observable2;

            public org.junit.runners.model.Statement observable3;

            public org.junit.runners.model.Statement withError1Observable;

            public org.junit.runners.model.Statement withError2Observable;

            public org.junit.runners.model.Statement withError3Observable;

            public org.junit.runners.model.Statement withEmpty1Observable;

            public org.junit.runners.model.Statement withEmpty2Observable;

            public org.junit.runners.model.Statement withEmpty3Observable;

            public org.junit.runners.model.Statement withEqualityErrorObservable;

            public org.junit.runners.model.Statement prefetchObservable;

            public org.junit.runners.model.Statement disposedObservable;

            public org.junit.runners.model.Statement one;

            public org.junit.runners.model.Statement two;

            public org.junit.runners.model.Statement three;

            public org.junit.runners.model.Statement withError1;

            public org.junit.runners.model.Statement withError2;

            public org.junit.runners.model.Statement withError3;

            public org.junit.runners.model.Statement withEmpty1;

            public org.junit.runners.model.Statement withEmpty2;

            public org.junit.runners.model.Statement withEmpty3;

            public org.junit.runners.model.Statement withEqualityError;

            public org.junit.runners.model.Statement prefetch;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement simpleInequal;

            public org.junit.runners.model.Statement simpleInequalObservable;

            public org.junit.runners.model.Statement onNextCancelRace;

            public org.junit.runners.model.Statement onNextCancelRaceObservable;

            public org.junit.runners.model.Statement firstCompletesBeforeSecond;

            public org.junit.runners.model.Statement secondCompletesBeforeFirst;

            public org.junit.runners.model.Statement bothEmpty;

            public org.junit.runners.model.Statement bothJust;

            public org.junit.runners.model.Statement bothCompleteWhileComparing;

            public org.junit.runners.model.Statement bothCompleteWhileComparingAsObservable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.observable1 = _ClassStatement.forPayload(ObservableSequenceEqualTest::observable1, "observable1", this);
            this.payloads.observable2 = _ClassStatement.forPayload(ObservableSequenceEqualTest::observable2, "observable2", this);
            this.payloads.observable3 = _ClassStatement.forPayload(ObservableSequenceEqualTest::observable3, "observable3", this);
            this.payloads.withError1Observable = _ClassStatement.forPayload(ObservableSequenceEqualTest::withError1Observable, "withError1Observable", this);
            this.payloads.withError2Observable = _ClassStatement.forPayload(ObservableSequenceEqualTest::withError2Observable, "withError2Observable", this);
            this.payloads.withError3Observable = _ClassStatement.forPayload(ObservableSequenceEqualTest::withError3Observable, "withError3Observable", this);
            this.payloads.withEmpty1Observable = _ClassStatement.forPayload(ObservableSequenceEqualTest::withEmpty1Observable, "withEmpty1Observable", this);
            this.payloads.withEmpty2Observable = _ClassStatement.forPayload(ObservableSequenceEqualTest::withEmpty2Observable, "withEmpty2Observable", this);
            this.payloads.withEmpty3Observable = _ClassStatement.forPayload(ObservableSequenceEqualTest::withEmpty3Observable, "withEmpty3Observable", this);
            this.payloads.withEqualityErrorObservable = _ClassStatement.forPayload(ObservableSequenceEqualTest::withEqualityErrorObservable, "withEqualityErrorObservable", this);
            this.payloads.prefetchObservable = _ClassStatement.forPayload(ObservableSequenceEqualTest::prefetchObservable, "prefetchObservable", this);
            this.payloads.disposedObservable = _ClassStatement.forPayload(ObservableSequenceEqualTest::disposedObservable, "disposedObservable", this);
            this.payloads.one = _ClassStatement.forPayload(ObservableSequenceEqualTest::one, "one", this);
            this.payloads.two = _ClassStatement.forPayload(ObservableSequenceEqualTest::two, "two", this);
            this.payloads.three = _ClassStatement.forPayload(ObservableSequenceEqualTest::three, "three", this);
            this.payloads.withError1 = _ClassStatement.forPayload(ObservableSequenceEqualTest::withError1, "withError1", this);
            this.payloads.withError2 = _ClassStatement.forPayload(ObservableSequenceEqualTest::withError2, "withError2", this);
            this.payloads.withError3 = _ClassStatement.forPayload(ObservableSequenceEqualTest::withError3, "withError3", this);
            this.payloads.withEmpty1 = _ClassStatement.forPayload(ObservableSequenceEqualTest::withEmpty1, "withEmpty1", this);
            this.payloads.withEmpty2 = _ClassStatement.forPayload(ObservableSequenceEqualTest::withEmpty2, "withEmpty2", this);
            this.payloads.withEmpty3 = _ClassStatement.forPayload(ObservableSequenceEqualTest::withEmpty3, "withEmpty3", this);
            this.payloads.withEqualityError = _ClassStatement.forPayload(ObservableSequenceEqualTest::withEqualityError, "withEqualityError", this);
            this.payloads.prefetch = _ClassStatement.forPayload(ObservableSequenceEqualTest::prefetch, "prefetch", this);
            this.payloads.disposed = _ClassStatement.forPayload(ObservableSequenceEqualTest::disposed, "disposed", this);
            this.payloads.simpleInequal = _ClassStatement.forPayload(ObservableSequenceEqualTest::simpleInequal, "simpleInequal", this);
            this.payloads.simpleInequalObservable = _ClassStatement.forPayload(ObservableSequenceEqualTest::simpleInequalObservable, "simpleInequalObservable", this);
            this.payloads.onNextCancelRace = _ClassStatement.forPayload(ObservableSequenceEqualTest::onNextCancelRace, "onNextCancelRace", this);
            this.payloads.onNextCancelRaceObservable = _ClassStatement.forPayload(ObservableSequenceEqualTest::onNextCancelRaceObservable, "onNextCancelRaceObservable", this);
            this.payloads.firstCompletesBeforeSecond = _ClassStatement.forPayload(ObservableSequenceEqualTest::firstCompletesBeforeSecond, "firstCompletesBeforeSecond", this);
            this.payloads.secondCompletesBeforeFirst = _ClassStatement.forPayload(ObservableSequenceEqualTest::secondCompletesBeforeFirst, "secondCompletesBeforeFirst", this);
            this.payloads.bothEmpty = _ClassStatement.forPayload(ObservableSequenceEqualTest::bothEmpty, "bothEmpty", this);
            this.payloads.bothJust = _ClassStatement.forPayload(ObservableSequenceEqualTest::bothJust, "bothJust", this);
            this.payloads.bothCompleteWhileComparing = _ClassStatement.forPayload(ObservableSequenceEqualTest::bothCompleteWhileComparing, "bothCompleteWhileComparing", this);
            this.payloads.bothCompleteWhileComparingAsObservable = _ClassStatement.forPayload(ObservableSequenceEqualTest::bothCompleteWhileComparingAsObservable, "bothCompleteWhileComparingAsObservable", this);
        }
    }
}
