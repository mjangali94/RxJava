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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableSkipLastTimedTest extends RxJavaTest {

    @Test
    public void skipLastTimed() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        // FIXME the timeunit now matters due to rounding
        Observable<Integer> result = source.skipLast(1000, TimeUnit.MILLISECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onNext(4);
        source.onNext(5);
        source.onNext(6);
        scheduler.advanceTimeBy(950, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o, never()).onNext(4);
        inOrder.verify(o, never()).onNext(5);
        inOrder.verify(o, never()).onNext(6);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipLastTimedErrorBeforeTime() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.skipLast(1, TimeUnit.SECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onError(new TestException());
        scheduler.advanceTimeBy(1050, TimeUnit.MILLISECONDS);
        verify(o).onError(any(TestException.class));
        verify(o, never()).onComplete();
        verify(o, never()).onNext(any());
    }

    @Test
    public void skipLastTimedCompleteBeforeTime() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.skipLast(1, TimeUnit.SECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onNext(any());
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipLastTimedWhenAllElementsAreValid() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.skipLast(1, TimeUnit.MILLISECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void skipLastTimedDefaultScheduler() {
        Observable.just(1).concatWith(Observable.just(2).delay(500, TimeUnit.MILLISECONDS)).skipLast(300, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void skipLastTimedDefaultSchedulerDelayError() {
        Observable.just(1).concatWith(Observable.just(2).delay(500, TimeUnit.MILLISECONDS)).skipLast(300, TimeUnit.MILLISECONDS, true).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void skipLastTimedCustomSchedulerDelayError() {
        Observable.just(1).concatWith(Observable.just(2).delay(500, TimeUnit.MILLISECONDS)).skipLast(300, TimeUnit.MILLISECONDS, Schedulers.io(), true).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().skipLast(1, TimeUnit.DAYS));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.skipLast(1, TimeUnit.DAYS);
            }
        });
    }

    @Test
    public void onCompleteDisposeRace() {
        TestScheduler scheduler = new TestScheduler();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = ps.skipLast(1, TimeUnit.DAYS, scheduler).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void onCompleteDisposeDelayErrorRace() {
        TestScheduler scheduler = new TestScheduler();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = ps.skipLast(1, TimeUnit.DAYS, scheduler, true).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void errorDelayed() {
        Observable.error(new TestException()).skipLast(1, TimeUnit.DAYS, new TestScheduler(), true).test().assertFailure(TestException.class);
    }

    @Test
    public void take() {
        Observable.just(1).skipLast(0, TimeUnit.SECONDS).take(1).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void onNextDisposeRace() {
        TestScheduler scheduler = new TestScheduler();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = ps.skipLast(1, TimeUnit.DAYS, scheduler).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void onNextOnCompleteDisposeDelayErrorRace() {
        TestScheduler scheduler = new TestScheduler();
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = ps.skipLast(1, TimeUnit.DAYS, scheduler, true).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onNext(1);
                    ps.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void skipLastTimedDelayError() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        // FIXME the timeunit now matters due to rounding
        Observable<Integer> result = source.skipLast(1000, TimeUnit.MILLISECONDS, scheduler, true);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onNext(4);
        source.onNext(5);
        source.onNext(6);
        scheduler.advanceTimeBy(950, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o, never()).onNext(4);
        inOrder.verify(o, never()).onNext(5);
        inOrder.verify(o, never()).onNext(6);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipLastTimedErrorBeforeTimeDelayError() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.skipLast(1, TimeUnit.SECONDS, scheduler, true);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onError(new TestException());
        scheduler.advanceTimeBy(1050, TimeUnit.MILLISECONDS);
        verify(o).onError(any(TestException.class));
        verify(o, never()).onComplete();
        verify(o, never()).onNext(any());
    }

    @Test
    public void skipLastTimedCompleteBeforeTimeDelayError() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.skipLast(1, TimeUnit.SECONDS, scheduler, true);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onNext(any());
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipLastTimedWhenAllElementsAreValidDelayError() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.skipLast(1, TimeUnit.MILLISECONDS, scheduler, true);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        source.onComplete();
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableSkipLastTimedTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimed() throws java.lang.Throwable {
            this.payloads.skipLastTimed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedErrorBeforeTime() throws java.lang.Throwable {
            this.payloads.skipLastTimedErrorBeforeTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedCompleteBeforeTime() throws java.lang.Throwable {
            this.payloads.skipLastTimedCompleteBeforeTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedWhenAllElementsAreValid() throws java.lang.Throwable {
            this.payloads.skipLastTimedWhenAllElementsAreValid.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedDefaultScheduler() throws java.lang.Throwable {
            this.payloads.skipLastTimedDefaultScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedDefaultSchedulerDelayError() throws java.lang.Throwable {
            this.payloads.skipLastTimedDefaultSchedulerDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedCustomSchedulerDelayError() throws java.lang.Throwable {
            this.payloads.skipLastTimedCustomSchedulerDelayError.evaluate();
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
        public void benchmark_onCompleteDisposeRace() throws java.lang.Throwable {
            this.payloads.onCompleteDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteDisposeDelayErrorRace() throws java.lang.Throwable {
            this.payloads.onCompleteDisposeDelayErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDelayed() throws java.lang.Throwable {
            this.payloads.errorDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextDisposeRace() throws java.lang.Throwable {
            this.payloads.onNextDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextOnCompleteDisposeDelayErrorRace() throws java.lang.Throwable {
            this.payloads.onNextOnCompleteDisposeDelayErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedDelayError() throws java.lang.Throwable {
            this.payloads.skipLastTimedDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedErrorBeforeTimeDelayError() throws java.lang.Throwable {
            this.payloads.skipLastTimedErrorBeforeTimeDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedCompleteBeforeTimeDelayError() throws java.lang.Throwable {
            this.payloads.skipLastTimedCompleteBeforeTimeDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipLastTimedWhenAllElementsAreValidDelayError() throws java.lang.Throwable {
            this.payloads.skipLastTimedWhenAllElementsAreValidDelayError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipLastTimedTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipLastTimedTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipLastTimedTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipLastTimedTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableSkipLastTimedTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipLastTimedTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableSkipLastTimedTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableSkipLastTimedTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement skipLastTimed;

            public org.junit.runners.model.Statement skipLastTimedErrorBeforeTime;

            public org.junit.runners.model.Statement skipLastTimedCompleteBeforeTime;

            public org.junit.runners.model.Statement skipLastTimedWhenAllElementsAreValid;

            public org.junit.runners.model.Statement skipLastTimedDefaultScheduler;

            public org.junit.runners.model.Statement skipLastTimedDefaultSchedulerDelayError;

            public org.junit.runners.model.Statement skipLastTimedCustomSchedulerDelayError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement onCompleteDisposeRace;

            public org.junit.runners.model.Statement onCompleteDisposeDelayErrorRace;

            public org.junit.runners.model.Statement errorDelayed;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement onNextDisposeRace;

            public org.junit.runners.model.Statement onNextOnCompleteDisposeDelayErrorRace;

            public org.junit.runners.model.Statement skipLastTimedDelayError;

            public org.junit.runners.model.Statement skipLastTimedErrorBeforeTimeDelayError;

            public org.junit.runners.model.Statement skipLastTimedCompleteBeforeTimeDelayError;

            public org.junit.runners.model.Statement skipLastTimedWhenAllElementsAreValidDelayError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.skipLastTimed = _ClassStatement.forPayload(ObservableSkipLastTimedTest::skipLastTimed, "skipLastTimed", this);
            this.payloads.skipLastTimedErrorBeforeTime = _ClassStatement.forPayload(ObservableSkipLastTimedTest::skipLastTimedErrorBeforeTime, "skipLastTimedErrorBeforeTime", this);
            this.payloads.skipLastTimedCompleteBeforeTime = _ClassStatement.forPayload(ObservableSkipLastTimedTest::skipLastTimedCompleteBeforeTime, "skipLastTimedCompleteBeforeTime", this);
            this.payloads.skipLastTimedWhenAllElementsAreValid = _ClassStatement.forPayload(ObservableSkipLastTimedTest::skipLastTimedWhenAllElementsAreValid, "skipLastTimedWhenAllElementsAreValid", this);
            this.payloads.skipLastTimedDefaultScheduler = _ClassStatement.forPayload(ObservableSkipLastTimedTest::skipLastTimedDefaultScheduler, "skipLastTimedDefaultScheduler", this);
            this.payloads.skipLastTimedDefaultSchedulerDelayError = _ClassStatement.forPayload(ObservableSkipLastTimedTest::skipLastTimedDefaultSchedulerDelayError, "skipLastTimedDefaultSchedulerDelayError", this);
            this.payloads.skipLastTimedCustomSchedulerDelayError = _ClassStatement.forPayload(ObservableSkipLastTimedTest::skipLastTimedCustomSchedulerDelayError, "skipLastTimedCustomSchedulerDelayError", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableSkipLastTimedTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableSkipLastTimedTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.onCompleteDisposeRace = _ClassStatement.forPayload(ObservableSkipLastTimedTest::onCompleteDisposeRace, "onCompleteDisposeRace", this);
            this.payloads.onCompleteDisposeDelayErrorRace = _ClassStatement.forPayload(ObservableSkipLastTimedTest::onCompleteDisposeDelayErrorRace, "onCompleteDisposeDelayErrorRace", this);
            this.payloads.errorDelayed = _ClassStatement.forPayload(ObservableSkipLastTimedTest::errorDelayed, "errorDelayed", this);
            this.payloads.take = _ClassStatement.forPayload(ObservableSkipLastTimedTest::take, "take", this);
            this.payloads.onNextDisposeRace = _ClassStatement.forPayload(ObservableSkipLastTimedTest::onNextDisposeRace, "onNextDisposeRace", this);
            this.payloads.onNextOnCompleteDisposeDelayErrorRace = _ClassStatement.forPayload(ObservableSkipLastTimedTest::onNextOnCompleteDisposeDelayErrorRace, "onNextOnCompleteDisposeDelayErrorRace", this);
            this.payloads.skipLastTimedDelayError = _ClassStatement.forPayload(ObservableSkipLastTimedTest::skipLastTimedDelayError, "skipLastTimedDelayError", this);
            this.payloads.skipLastTimedErrorBeforeTimeDelayError = _ClassStatement.forPayload(ObservableSkipLastTimedTest::skipLastTimedErrorBeforeTimeDelayError, "skipLastTimedErrorBeforeTimeDelayError", this);
            this.payloads.skipLastTimedCompleteBeforeTimeDelayError = _ClassStatement.forPayload(ObservableSkipLastTimedTest::skipLastTimedCompleteBeforeTimeDelayError, "skipLastTimedCompleteBeforeTimeDelayError", this);
            this.payloads.skipLastTimedWhenAllElementsAreValidDelayError = _ClassStatement.forPayload(ObservableSkipLastTimedTest::skipLastTimedWhenAllElementsAreValidDelayError, "skipLastTimedWhenAllElementsAreValidDelayError", this);
        }
    }
}
