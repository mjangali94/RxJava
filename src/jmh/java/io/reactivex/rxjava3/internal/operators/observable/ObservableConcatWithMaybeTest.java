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
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.MaybeSubject;

public class ObservableConcatWithMaybeTest extends RxJavaTest {

    @Test
    public void normalEmpty() {
        final TestObserver<Integer> to = new TestObserver<>();
        Observable.range(1, 5).concatWith(Maybe.<Integer>fromAction(new Action() {

            @Override
            public void run() throws Exception {
                to.onNext(100);
            }
        })).subscribe(to);
        to.assertResult(1, 2, 3, 4, 5, 100);
    }

    @Test
    public void normalNonEmpty() {
        final TestObserver<Integer> to = new TestObserver<>();
        Observable.range(1, 5).concatWith(Maybe.just(100)).subscribe(to);
        to.assertResult(1, 2, 3, 4, 5, 100);
    }

    @Test
    public void mainError() {
        final TestObserver<Integer> to = new TestObserver<>();
        Observable.<Integer>error(new TestException()).concatWith(Maybe.<Integer>fromAction(new Action() {

            @Override
            public void run() throws Exception {
                to.onNext(100);
            }
        })).subscribe(to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void otherError() {
        final TestObserver<Integer> to = new TestObserver<>();
        Observable.range(1, 5).concatWith(Maybe.<Integer>error(new TestException())).subscribe(to);
        to.assertFailure(TestException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void takeMain() {
        final TestObserver<Integer> to = new TestObserver<>();
        Observable.range(1, 5).concatWith(Maybe.<Integer>fromAction(new Action() {

            @Override
            public void run() throws Exception {
                to.onNext(100);
            }
        })).take(3).subscribe(to);
        to.assertResult(1, 2, 3);
    }

    @Test
    public void cancelOther() {
        MaybeSubject<Object> other = MaybeSubject.create();
        TestObserver<Object> to = Observable.empty().concatWith(other).test();
        assertTrue(other.hasObservers());
        to.dispose();
        assertFalse(other.hasObservers());
    }

    @Test
    public void consumerDisposed() {
        new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                Disposable bs1 = Disposable.empty();
                observer.onSubscribe(bs1);
                assertFalse(((Disposable) observer).isDisposed());
                observer.onNext(1);
                assertTrue(((Disposable) observer).isDisposed());
                assertTrue(bs1.isDisposed());
            }
        }.concatWith(Maybe.just(100)).take(1).test().assertResult(1);
    }

    @Test
    public void badSource() {
        new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                Disposable bs1 = Disposable.empty();
                observer.onSubscribe(bs1);
                Disposable bs2 = Disposable.empty();
                observer.onSubscribe(bs2);
                assertFalse(bs1.isDisposed());
                assertTrue(bs2.isDisposed());
                observer.onComplete();
            }
        }.concatWith(Maybe.<Integer>empty()).test().assertResult();
    }

    @Test
    public void badSource2() {
        Flowable.empty().concatWith(new Maybe<Integer>() {

            @Override
            protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                Disposable bs1 = Disposable.empty();
                observer.onSubscribe(bs1);
                Disposable bs2 = Disposable.empty();
                observer.onSubscribe(bs2);
                assertFalse(bs1.isDisposed());
                assertTrue(bs2.isDisposed());
                observer.onComplete();
            }
        }).test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableConcatWithMaybeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmpty() throws java.lang.Throwable {
            this.payloads.normalEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalNonEmpty() throws java.lang.Throwable {
            this.payloads.normalNonEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.payloads.mainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherError() throws java.lang.Throwable {
            this.payloads.otherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeMain() throws java.lang.Throwable {
            this.payloads.takeMain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOther() throws java.lang.Throwable {
            this.payloads.cancelOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_consumerDisposed() throws java.lang.Throwable {
            this.payloads.consumerDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource2() throws java.lang.Throwable {
            this.payloads.badSource2.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatWithMaybeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatWithMaybeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatWithMaybeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatWithMaybeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableConcatWithMaybeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatWithMaybeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableConcatWithMaybeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableConcatWithMaybeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normalEmpty;

            public org.junit.runners.model.Statement normalNonEmpty;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement otherError;

            public org.junit.runners.model.Statement takeMain;

            public org.junit.runners.model.Statement cancelOther;

            public org.junit.runners.model.Statement consumerDisposed;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement badSource2;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normalEmpty = _ClassStatement.forPayload(ObservableConcatWithMaybeTest::normalEmpty, "normalEmpty", this);
            this.payloads.normalNonEmpty = _ClassStatement.forPayload(ObservableConcatWithMaybeTest::normalNonEmpty, "normalNonEmpty", this);
            this.payloads.mainError = _ClassStatement.forPayload(ObservableConcatWithMaybeTest::mainError, "mainError", this);
            this.payloads.otherError = _ClassStatement.forPayload(ObservableConcatWithMaybeTest::otherError, "otherError", this);
            this.payloads.takeMain = _ClassStatement.forPayload(ObservableConcatWithMaybeTest::takeMain, "takeMain", this);
            this.payloads.cancelOther = _ClassStatement.forPayload(ObservableConcatWithMaybeTest::cancelOther, "cancelOther", this);
            this.payloads.consumerDisposed = _ClassStatement.forPayload(ObservableConcatWithMaybeTest::consumerDisposed, "consumerDisposed", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableConcatWithMaybeTest::badSource, "badSource", this);
            this.payloads.badSource2 = _ClassStatement.forPayload(ObservableConcatWithMaybeTest::badSource2, "badSource2", this);
        }
    }
}
