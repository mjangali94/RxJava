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

import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableThrottleLatestTest extends RxJavaTest {

    @Test
    public void just() {
        Observable.just(1).throttleLatest(1, TimeUnit.MINUTES).test().assertResult(1);
    }

    @Test
    public void range() {
        Observable.range(1, 5).throttleLatest(1, TimeUnit.MINUTES).test().assertResult(1);
    }

    @Test
    public void rangeEmitLatest() {
        Observable.range(1, 5).throttleLatest(1, TimeUnit.MINUTES, true).test().assertResult(1, 5);
    }

    @Test
    public void error() {
        Observable.error(new TestException()).throttleLatest(1, TimeUnit.MINUTES).test().assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> f) throws Exception {
                return f.throttleLatest(1, TimeUnit.MINUTES);
            }
        });
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Observable.never().throttleLatest(1, TimeUnit.MINUTES));
    }

    @Test
    public void normal() {
        TestScheduler sch = new TestScheduler();
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.throttleLatest(1, TimeUnit.SECONDS, sch).test();
        ps.onNext(1);
        to.assertValuesOnly(1);
        ps.onNext(2);
        to.assertValuesOnly(1);
        ps.onNext(3);
        to.assertValuesOnly(1);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        to.assertValuesOnly(1, 3);
        ps.onNext(4);
        to.assertValuesOnly(1, 3);
        ps.onNext(5);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        to.assertValuesOnly(1, 3, 5);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        to.assertValuesOnly(1, 3, 5);
        ps.onNext(6);
        to.assertValuesOnly(1, 3, 5, 6);
        ps.onNext(7);
        ps.onComplete();
        to.assertResult(1, 3, 5, 6);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        to.assertResult(1, 3, 5, 6);
    }

    @Test
    public void normalEmitLast() {
        TestScheduler sch = new TestScheduler();
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.throttleLatest(1, TimeUnit.SECONDS, sch, true).test();
        ps.onNext(1);
        to.assertValuesOnly(1);
        ps.onNext(2);
        to.assertValuesOnly(1);
        ps.onNext(3);
        to.assertValuesOnly(1);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        to.assertValuesOnly(1, 3);
        ps.onNext(4);
        to.assertValuesOnly(1, 3);
        ps.onNext(5);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        to.assertValuesOnly(1, 3, 5);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        to.assertValuesOnly(1, 3, 5);
        ps.onNext(6);
        to.assertValuesOnly(1, 3, 5, 6);
        ps.onNext(7);
        ps.onComplete();
        to.assertResult(1, 3, 5, 6, 7);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        to.assertResult(1, 3, 5, 6, 7);
    }

    @Test
    public void take() throws Throwable {
        Action onCancel = mock(Action.class);
        Observable.range(1, 5).doOnDispose(onCancel).throttleLatest(1, TimeUnit.MINUTES).take(1).test().assertResult(1);
        verify(onCancel).run();
    }

    @Test
    public void reentrantComplete() {
        TestScheduler sch = new TestScheduler();
        final PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    ps.onNext(2);
                }
                if (t == 2) {
                    ps.onComplete();
                }
            }
        };
        ps.throttleLatest(1, TimeUnit.SECONDS, sch).subscribe(to);
        ps.onNext(1);
        sch.advanceTimeBy(1, TimeUnit.SECONDS);
        to.assertResult(1, 2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableThrottleLatestTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.payloads.just.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_range() throws java.lang.Throwable {
            this.payloads.range.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeEmitLatest() throws java.lang.Throwable {
            this.payloads.rangeEmitLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmitLast() throws java.lang.Throwable {
            this.payloads.normalEmitLast.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantComplete() throws java.lang.Throwable {
            this.payloads.reentrantComplete.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableThrottleLatestTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableThrottleLatestTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableThrottleLatestTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableThrottleLatestTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableThrottleLatestTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableThrottleLatestTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableThrottleLatestTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableThrottleLatestTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement just;

            public org.junit.runners.model.Statement range;

            public org.junit.runners.model.Statement rangeEmitLatest;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement normalEmitLast;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement reentrantComplete;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.just = _ClassStatement.forPayload(ObservableThrottleLatestTest::just, "just", this);
            this.payloads.range = _ClassStatement.forPayload(ObservableThrottleLatestTest::range, "range", this);
            this.payloads.rangeEmitLatest = _ClassStatement.forPayload(ObservableThrottleLatestTest::rangeEmitLatest, "rangeEmitLatest", this);
            this.payloads.error = _ClassStatement.forPayload(ObservableThrottleLatestTest::error, "error", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableThrottleLatestTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.disposed = _ClassStatement.forPayload(ObservableThrottleLatestTest::disposed, "disposed", this);
            this.payloads.normal = _ClassStatement.forPayload(ObservableThrottleLatestTest::normal, "normal", this);
            this.payloads.normalEmitLast = _ClassStatement.forPayload(ObservableThrottleLatestTest::normalEmitLast, "normalEmitLast", this);
            this.payloads.take = _ClassStatement.forPayload(ObservableThrottleLatestTest::take, "take", this);
            this.payloads.reentrantComplete = _ClassStatement.forPayload(ObservableThrottleLatestTest::reentrantComplete, "reentrantComplete", this);
        }
    }
}
