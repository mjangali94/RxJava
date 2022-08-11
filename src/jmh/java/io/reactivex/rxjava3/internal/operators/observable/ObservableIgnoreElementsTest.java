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
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableIgnoreElementsTest extends RxJavaTest {

    @Test
    public void withEmptyObservable() {
        assertTrue(Observable.empty().ignoreElements().toObservable().isEmpty().blockingGet());
    }

    @Test
    public void withNonEmptyObservable() {
        assertTrue(Observable.just(1, 2, 3).ignoreElements().toObservable().isEmpty().blockingGet());
    }

    @Test
    public void upstreamIsProcessedButIgnoredObservable() {
        final int num = 10;
        final AtomicInteger upstreamCount = new AtomicInteger();
        long count = Observable.range(1, num).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).ignoreElements().toObservable().count().blockingGet();
        assertEquals(num, upstreamCount.get());
        assertEquals(0, count);
    }

    @Test
    public void completedOkObservable() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        Observable.range(1, 10).ignoreElements().toObservable().subscribe(to);
        to.assertNoErrors();
        to.assertNoValues();
        to.assertTerminated();
    }

    @Test
    public void errorReceivedObservable() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        TestException ex = new TestException("boo");
        Observable.error(ex).ignoreElements().toObservable().subscribe(to);
        to.assertNoValues();
        to.assertTerminated();
        to.assertError(TestException.class);
        to.assertErrorMessage("boo");
    }

    @Test
    public void unsubscribesFromUpstreamObservable() {
        final AtomicBoolean unsub = new AtomicBoolean();
        Observable.range(1, 10).concatWith(Observable.<Integer>never()).doOnDispose(new Action() {

            @Override
            public void run() {
                unsub.set(true);
            }
        }).ignoreElements().toObservable().subscribe().dispose();
        assertTrue(unsub.get());
    }

    @Test
    public void withEmpty() {
        Observable.empty().ignoreElements().blockingAwait();
    }

    @Test
    public void withNonEmpty() {
        Observable.just(1, 2, 3).ignoreElements().blockingAwait();
    }

    @Test
    public void upstreamIsProcessedButIgnored() {
        final int num = 10;
        final AtomicInteger upstreamCount = new AtomicInteger();
        Observable.range(1, num).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                upstreamCount.incrementAndGet();
            }
        }).ignoreElements().blockingAwait();
        assertEquals(num, upstreamCount.get());
    }

    @Test
    public void completedOk() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        Observable.range(1, 10).ignoreElements().subscribe(to);
        to.assertNoErrors();
        to.assertNoValues();
        to.assertTerminated();
    }

    @Test
    public void errorReceived() {
        TestObserverEx<Object> to = new TestObserverEx<>();
        TestException ex = new TestException("boo");
        Observable.error(ex).ignoreElements().subscribe(to);
        to.assertNoValues();
        to.assertTerminated();
        to.assertError(TestException.class);
        to.assertErrorMessage("boo");
    }

    @Test
    public void unsubscribesFromUpstream() {
        final AtomicBoolean unsub = new AtomicBoolean();
        Observable.range(1, 10).concatWith(Observable.<Integer>never()).doOnDispose(new Action() {

            @Override
            public void run() {
                unsub.set(true);
            }
        }).ignoreElements().subscribe().dispose();
        assertTrue(unsub.get());
    }

    @Test
    public void dispose() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.ignoreElements().<Integer>toObservable().test();
        assertTrue(ps.hasObservers());
        to.dispose();
        assertFalse(ps.hasObservers());
        TestHelper.checkDisposed(ps.ignoreElements().<Integer>toObservable());
    }

    @Test
    public void checkDispose() {
        TestHelper.checkDisposed(Observable.just(1).ignoreElements());
        TestHelper.checkDisposed(Observable.just(1).ignoreElements().toObservable());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableIgnoreElementsTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmptyObservable() throws java.lang.Throwable {
            this.payloads.withEmptyObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withNonEmptyObservable() throws java.lang.Throwable {
            this.payloads.withNonEmptyObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamIsProcessedButIgnoredObservable() throws java.lang.Throwable {
            this.payloads.upstreamIsProcessedButIgnoredObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completedOkObservable() throws java.lang.Throwable {
            this.payloads.completedOkObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorReceivedObservable() throws java.lang.Throwable {
            this.payloads.errorReceivedObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribesFromUpstreamObservable() throws java.lang.Throwable {
            this.payloads.unsubscribesFromUpstreamObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty() throws java.lang.Throwable {
            this.payloads.withEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withNonEmpty() throws java.lang.Throwable {
            this.payloads.withNonEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamIsProcessedButIgnored() throws java.lang.Throwable {
            this.payloads.upstreamIsProcessedButIgnored.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completedOk() throws java.lang.Throwable {
            this.payloads.completedOk.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorReceived() throws java.lang.Throwable {
            this.payloads.errorReceived.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribesFromUpstream() throws java.lang.Throwable {
            this.payloads.unsubscribesFromUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDispose() throws java.lang.Throwable {
            this.payloads.checkDispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableIgnoreElementsTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableIgnoreElementsTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableIgnoreElementsTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableIgnoreElementsTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableIgnoreElementsTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableIgnoreElementsTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableIgnoreElementsTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableIgnoreElementsTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement withEmptyObservable;

            public org.junit.runners.model.Statement withNonEmptyObservable;

            public org.junit.runners.model.Statement upstreamIsProcessedButIgnoredObservable;

            public org.junit.runners.model.Statement completedOkObservable;

            public org.junit.runners.model.Statement errorReceivedObservable;

            public org.junit.runners.model.Statement unsubscribesFromUpstreamObservable;

            public org.junit.runners.model.Statement withEmpty;

            public org.junit.runners.model.Statement withNonEmpty;

            public org.junit.runners.model.Statement upstreamIsProcessedButIgnored;

            public org.junit.runners.model.Statement completedOk;

            public org.junit.runners.model.Statement errorReceived;

            public org.junit.runners.model.Statement unsubscribesFromUpstream;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement checkDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.withEmptyObservable = _ClassStatement.forPayload(ObservableIgnoreElementsTest::withEmptyObservable, "withEmptyObservable", this);
            this.payloads.withNonEmptyObservable = _ClassStatement.forPayload(ObservableIgnoreElementsTest::withNonEmptyObservable, "withNonEmptyObservable", this);
            this.payloads.upstreamIsProcessedButIgnoredObservable = _ClassStatement.forPayload(ObservableIgnoreElementsTest::upstreamIsProcessedButIgnoredObservable, "upstreamIsProcessedButIgnoredObservable", this);
            this.payloads.completedOkObservable = _ClassStatement.forPayload(ObservableIgnoreElementsTest::completedOkObservable, "completedOkObservable", this);
            this.payloads.errorReceivedObservable = _ClassStatement.forPayload(ObservableIgnoreElementsTest::errorReceivedObservable, "errorReceivedObservable", this);
            this.payloads.unsubscribesFromUpstreamObservable = _ClassStatement.forPayload(ObservableIgnoreElementsTest::unsubscribesFromUpstreamObservable, "unsubscribesFromUpstreamObservable", this);
            this.payloads.withEmpty = _ClassStatement.forPayload(ObservableIgnoreElementsTest::withEmpty, "withEmpty", this);
            this.payloads.withNonEmpty = _ClassStatement.forPayload(ObservableIgnoreElementsTest::withNonEmpty, "withNonEmpty", this);
            this.payloads.upstreamIsProcessedButIgnored = _ClassStatement.forPayload(ObservableIgnoreElementsTest::upstreamIsProcessedButIgnored, "upstreamIsProcessedButIgnored", this);
            this.payloads.completedOk = _ClassStatement.forPayload(ObservableIgnoreElementsTest::completedOk, "completedOk", this);
            this.payloads.errorReceived = _ClassStatement.forPayload(ObservableIgnoreElementsTest::errorReceived, "errorReceived", this);
            this.payloads.unsubscribesFromUpstream = _ClassStatement.forPayload(ObservableIgnoreElementsTest::unsubscribesFromUpstream, "unsubscribesFromUpstream", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableIgnoreElementsTest::dispose, "dispose", this);
            this.payloads.checkDispose = _ClassStatement.forPayload(ObservableIgnoreElementsTest::checkDispose, "checkDispose", this);
        }
    }
}
