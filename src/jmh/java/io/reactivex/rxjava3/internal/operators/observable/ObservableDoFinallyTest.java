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
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.operators.QueueDisposable;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.UnicastSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableDoFinallyTest extends RxJavaTest implements Action {

    int calls;

    @Override
    public void run() throws Exception {
        calls++;
    }

    @Test
    public void normalJust() {
        Observable.just(1).doFinally(this).test().assertResult(1);
        assertEquals(1, calls);
    }

    @Test
    public void normalEmpty() {
        Observable.empty().doFinally(this).test().assertResult();
        assertEquals(1, calls);
    }

    @Test
    public void normalError() {
        Observable.error(new TestException()).doFinally(this).test().assertFailure(TestException.class);
        assertEquals(1, calls);
    }

    @Test
    public void normalTake() {
        Observable.range(1, 10).doFinally(this).take(5).test().assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> f) throws Exception {
                return f.doFinally(ObservableDoFinallyTest.this);
            }
        });
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> f) throws Exception {
                return f.doFinally(ObservableDoFinallyTest.this).filter(Functions.alwaysTrue());
            }
        });
    }

    @Test
    public void syncFused() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC);
        Observable.range(1, 5).doFinally(this).subscribe(to);
        to.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void syncFusedBoundary() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC | QueueFuseable.BOUNDARY);
        Observable.range(1, 5).doFinally(this).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void asyncFused() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ASYNC);
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.doFinally(this).subscribe(to);
        to.assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void asyncFusedBoundary() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ASYNC | QueueFuseable.BOUNDARY);
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.doFinally(this).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void normalJustConditional() {
        Observable.just(1).doFinally(this).filter(Functions.alwaysTrue()).test().assertResult(1);
        assertEquals(1, calls);
    }

    @Test
    public void normalEmptyConditional() {
        Observable.empty().doFinally(this).filter(Functions.alwaysTrue()).test().assertResult();
        assertEquals(1, calls);
    }

    @Test
    public void normalErrorConditional() {
        Observable.error(new TestException()).doFinally(this).filter(Functions.alwaysTrue()).test().assertFailure(TestException.class);
        assertEquals(1, calls);
    }

    @Test
    public void normalTakeConditional() {
        Observable.range(1, 10).doFinally(this).filter(Functions.alwaysTrue()).take(5).test().assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void syncFusedConditional() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC);
        Observable.range(1, 5).doFinally(this).filter(Functions.alwaysTrue()).subscribe(to);
        to.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void nonFused() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC);
        Observable.range(1, 5).hide().doFinally(this).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void nonFusedConditional() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC);
        Observable.range(1, 5).hide().doFinally(this).filter(Functions.alwaysTrue()).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void syncFusedBoundaryConditional() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC | QueueFuseable.BOUNDARY);
        Observable.range(1, 5).doFinally(this).filter(Functions.alwaysTrue()).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void asyncFusedConditional() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ASYNC);
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.doFinally(this).filter(Functions.alwaysTrue()).subscribe(to);
        to.assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void asyncFusedBoundaryConditional() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ASYNC | QueueFuseable.BOUNDARY);
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.doFinally(this).filter(Functions.alwaysTrue()).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls);
    }

    @Test
    public void actionThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.just(1).doFinally(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).test().assertResult(1).dispose();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void actionThrowsConditional() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.just(1).doFinally(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).filter(Functions.alwaysTrue()).test().assertResult(1).dispose();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void clearIsEmpty() {
        Observable.range(1, 5).doFinally(this).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                @SuppressWarnings("unchecked")
                QueueDisposable<Integer> qd = (QueueDisposable<Integer>) d;
                qd.requestFusion(QueueFuseable.ANY);
                assertFalse(qd.isEmpty());
                try {
                    assertEquals(1, qd.poll().intValue());
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
                assertFalse(qd.isEmpty());
                qd.clear();
                assertTrue(qd.isEmpty());
                qd.dispose();
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
        assertEquals(1, calls);
    }

    @Test
    public void clearIsEmptyConditional() {
        Observable.range(1, 5).doFinally(this).filter(Functions.alwaysTrue()).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                @SuppressWarnings("unchecked")
                QueueDisposable<Integer> qd = (QueueDisposable<Integer>) d;
                qd.requestFusion(QueueFuseable.ANY);
                assertFalse(qd.isEmpty());
                assertFalse(qd.isDisposed());
                try {
                    assertEquals(1, qd.poll().intValue());
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
                assertFalse(qd.isEmpty());
                qd.clear();
                assertTrue(qd.isEmpty());
                qd.dispose();
                assertTrue(qd.isDisposed());
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
        assertEquals(1, calls);
    }

    @Test
    public void eventOrdering() {
        final List<String> list = new ArrayList<>();
        Observable.error(new TestException()).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                list.add("dispose");
            }
        }).doFinally(new Action() {

            @Override
            public void run() throws Exception {
                list.add("finally");
            }
        }).subscribe(new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add("onNext");
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                list.add("onError");
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
                list.add("onComplete");
            }
        });
        assertEquals(Arrays.asList("onError", "finally"), list);
    }

    @Test
    public void eventOrdering2() {
        final List<String> list = new ArrayList<>();
        Observable.just(1).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                list.add("dispose");
            }
        }).doFinally(new Action() {

            @Override
            public void run() throws Exception {
                list.add("finally");
            }
        }).subscribe(new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add("onNext");
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                list.add("onError");
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
                list.add("onComplete");
            }
        });
        assertEquals(Arrays.asList("onNext", "onComplete", "finally"), list);
    }

    @Test
    public void fusionRejected() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.ANY);
        TestHelper.rejectObservableFusion().doFinally(() -> {
        }).subscribeWith(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.NONE);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableDoFinallyTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalJust() throws java.lang.Throwable {
            this.payloads.normalJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmpty() throws java.lang.Throwable {
            this.payloads.normalEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalError() throws java.lang.Throwable {
            this.payloads.normalError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalTake() throws java.lang.Throwable {
            this.payloads.normalTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFused() throws java.lang.Throwable {
            this.payloads.syncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedBoundary() throws java.lang.Throwable {
            this.payloads.syncFusedBoundary.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFused() throws java.lang.Throwable {
            this.payloads.asyncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedBoundary() throws java.lang.Throwable {
            this.payloads.asyncFusedBoundary.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalJustConditional() throws java.lang.Throwable {
            this.payloads.normalJustConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEmptyConditional() throws java.lang.Throwable {
            this.payloads.normalEmptyConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalErrorConditional() throws java.lang.Throwable {
            this.payloads.normalErrorConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalTakeConditional() throws java.lang.Throwable {
            this.payloads.normalTakeConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedConditional() throws java.lang.Throwable {
            this.payloads.syncFusedConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonFused() throws java.lang.Throwable {
            this.payloads.nonFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonFusedConditional() throws java.lang.Throwable {
            this.payloads.nonFusedConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedBoundaryConditional() throws java.lang.Throwable {
            this.payloads.syncFusedBoundaryConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedConditional() throws java.lang.Throwable {
            this.payloads.asyncFusedConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedBoundaryConditional() throws java.lang.Throwable {
            this.payloads.asyncFusedBoundaryConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_actionThrows() throws java.lang.Throwable {
            this.payloads.actionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_actionThrowsConditional() throws java.lang.Throwable {
            this.payloads.actionThrowsConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clearIsEmpty() throws java.lang.Throwable {
            this.payloads.clearIsEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clearIsEmptyConditional() throws java.lang.Throwable {
            this.payloads.clearIsEmptyConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eventOrdering() throws java.lang.Throwable {
            this.payloads.eventOrdering.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eventOrdering2() throws java.lang.Throwable {
            this.payloads.eventOrdering2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejected() throws java.lang.Throwable {
            this.payloads.fusionRejected.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDoFinallyTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDoFinallyTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDoFinallyTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDoFinallyTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableDoFinallyTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDoFinallyTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableDoFinallyTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableDoFinallyTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normalJust;

            public org.junit.runners.model.Statement normalEmpty;

            public org.junit.runners.model.Statement normalError;

            public org.junit.runners.model.Statement normalTake;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement syncFused;

            public org.junit.runners.model.Statement syncFusedBoundary;

            public org.junit.runners.model.Statement asyncFused;

            public org.junit.runners.model.Statement asyncFusedBoundary;

            public org.junit.runners.model.Statement normalJustConditional;

            public org.junit.runners.model.Statement normalEmptyConditional;

            public org.junit.runners.model.Statement normalErrorConditional;

            public org.junit.runners.model.Statement normalTakeConditional;

            public org.junit.runners.model.Statement syncFusedConditional;

            public org.junit.runners.model.Statement nonFused;

            public org.junit.runners.model.Statement nonFusedConditional;

            public org.junit.runners.model.Statement syncFusedBoundaryConditional;

            public org.junit.runners.model.Statement asyncFusedConditional;

            public org.junit.runners.model.Statement asyncFusedBoundaryConditional;

            public org.junit.runners.model.Statement actionThrows;

            public org.junit.runners.model.Statement actionThrowsConditional;

            public org.junit.runners.model.Statement clearIsEmpty;

            public org.junit.runners.model.Statement clearIsEmptyConditional;

            public org.junit.runners.model.Statement eventOrdering;

            public org.junit.runners.model.Statement eventOrdering2;

            public org.junit.runners.model.Statement fusionRejected;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normalJust = _ClassStatement.forPayload(ObservableDoFinallyTest::normalJust, "normalJust", this);
            this.payloads.normalEmpty = _ClassStatement.forPayload(ObservableDoFinallyTest::normalEmpty, "normalEmpty", this);
            this.payloads.normalError = _ClassStatement.forPayload(ObservableDoFinallyTest::normalError, "normalError", this);
            this.payloads.normalTake = _ClassStatement.forPayload(ObservableDoFinallyTest::normalTake, "normalTake", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableDoFinallyTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.syncFused = _ClassStatement.forPayload(ObservableDoFinallyTest::syncFused, "syncFused", this);
            this.payloads.syncFusedBoundary = _ClassStatement.forPayload(ObservableDoFinallyTest::syncFusedBoundary, "syncFusedBoundary", this);
            this.payloads.asyncFused = _ClassStatement.forPayload(ObservableDoFinallyTest::asyncFused, "asyncFused", this);
            this.payloads.asyncFusedBoundary = _ClassStatement.forPayload(ObservableDoFinallyTest::asyncFusedBoundary, "asyncFusedBoundary", this);
            this.payloads.normalJustConditional = _ClassStatement.forPayload(ObservableDoFinallyTest::normalJustConditional, "normalJustConditional", this);
            this.payloads.normalEmptyConditional = _ClassStatement.forPayload(ObservableDoFinallyTest::normalEmptyConditional, "normalEmptyConditional", this);
            this.payloads.normalErrorConditional = _ClassStatement.forPayload(ObservableDoFinallyTest::normalErrorConditional, "normalErrorConditional", this);
            this.payloads.normalTakeConditional = _ClassStatement.forPayload(ObservableDoFinallyTest::normalTakeConditional, "normalTakeConditional", this);
            this.payloads.syncFusedConditional = _ClassStatement.forPayload(ObservableDoFinallyTest::syncFusedConditional, "syncFusedConditional", this);
            this.payloads.nonFused = _ClassStatement.forPayload(ObservableDoFinallyTest::nonFused, "nonFused", this);
            this.payloads.nonFusedConditional = _ClassStatement.forPayload(ObservableDoFinallyTest::nonFusedConditional, "nonFusedConditional", this);
            this.payloads.syncFusedBoundaryConditional = _ClassStatement.forPayload(ObservableDoFinallyTest::syncFusedBoundaryConditional, "syncFusedBoundaryConditional", this);
            this.payloads.asyncFusedConditional = _ClassStatement.forPayload(ObservableDoFinallyTest::asyncFusedConditional, "asyncFusedConditional", this);
            this.payloads.asyncFusedBoundaryConditional = _ClassStatement.forPayload(ObservableDoFinallyTest::asyncFusedBoundaryConditional, "asyncFusedBoundaryConditional", this);
            this.payloads.actionThrows = _ClassStatement.forPayload(ObservableDoFinallyTest::actionThrows, "actionThrows", this);
            this.payloads.actionThrowsConditional = _ClassStatement.forPayload(ObservableDoFinallyTest::actionThrowsConditional, "actionThrowsConditional", this);
            this.payloads.clearIsEmpty = _ClassStatement.forPayload(ObservableDoFinallyTest::clearIsEmpty, "clearIsEmpty", this);
            this.payloads.clearIsEmptyConditional = _ClassStatement.forPayload(ObservableDoFinallyTest::clearIsEmptyConditional, "clearIsEmptyConditional", this);
            this.payloads.eventOrdering = _ClassStatement.forPayload(ObservableDoFinallyTest::eventOrdering, "eventOrdering", this);
            this.payloads.eventOrdering2 = _ClassStatement.forPayload(ObservableDoFinallyTest::eventOrdering2, "eventOrdering2", this);
            this.payloads.fusionRejected = _ClassStatement.forPayload(ObservableDoFinallyTest::fusionRejected, "fusionRejected", this);
        }
    }
}
