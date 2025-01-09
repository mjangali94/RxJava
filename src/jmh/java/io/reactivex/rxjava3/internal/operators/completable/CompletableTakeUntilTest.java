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
package io.reactivex.rxjava3.internal.operators.completable;

import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableTakeUntilTest extends RxJavaTest {

    @Test
    public void consumerDisposes() {
        CompletableSubject cs1 = CompletableSubject.create();
        CompletableSubject cs2 = CompletableSubject.create();
        TestObserver<Void> to = cs1.takeUntil(cs2).test();
        to.assertEmpty();
        assertTrue(cs1.hasObservers());
        assertTrue(cs2.hasObservers());
        to.dispose();
        assertFalse(cs1.hasObservers());
        assertFalse(cs2.hasObservers());
    }

    @Test
    public void mainCompletes() {
        CompletableSubject cs1 = CompletableSubject.create();
        CompletableSubject cs2 = CompletableSubject.create();
        TestObserver<Void> to = cs1.takeUntil(cs2).test();
        to.assertEmpty();
        assertTrue(cs1.hasObservers());
        assertTrue(cs2.hasObservers());
        cs1.onComplete();
        assertFalse(cs1.hasObservers());
        assertFalse(cs2.hasObservers());
        to.assertResult();
    }

    @Test
    public void otherCompletes() {
        CompletableSubject cs1 = CompletableSubject.create();
        CompletableSubject cs2 = CompletableSubject.create();
        TestObserver<Void> to = cs1.takeUntil(cs2).test();
        to.assertEmpty();
        assertTrue(cs1.hasObservers());
        assertTrue(cs2.hasObservers());
        cs2.onComplete();
        assertFalse(cs1.hasObservers());
        assertFalse(cs2.hasObservers());
        to.assertResult();
    }

    @Test
    public void mainErrors() {
        CompletableSubject cs1 = CompletableSubject.create();
        CompletableSubject cs2 = CompletableSubject.create();
        TestObserver<Void> to = cs1.takeUntil(cs2).test();
        to.assertEmpty();
        assertTrue(cs1.hasObservers());
        assertTrue(cs2.hasObservers());
        cs1.onError(new TestException());
        assertFalse(cs1.hasObservers());
        assertFalse(cs2.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void otherErrors() {
        CompletableSubject cs1 = CompletableSubject.create();
        CompletableSubject cs2 = CompletableSubject.create();
        TestObserver<Void> to = cs1.takeUntil(cs2).test();
        to.assertEmpty();
        assertTrue(cs1.hasObservers());
        assertTrue(cs2.hasObservers());
        cs2.onError(new TestException());
        assertFalse(cs1.hasObservers());
        assertFalse(cs2.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void isDisposed() {
        CompletableSubject cs1 = CompletableSubject.create();
        CompletableSubject cs2 = CompletableSubject.create();
        TestHelper.checkDisposed(cs1.takeUntil(cs2));
    }

    @Test
    public void mainErrorLate() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Completable() {

                @Override
                protected void subscribeActual(CompletableObserver observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onError(new TestException());
                }
            }.takeUntil(Completable.complete()).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void mainCompleteLate() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Completable() {

                @Override
                protected void subscribeActual(CompletableObserver observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onComplete();
                }
            }.takeUntil(Completable.complete()).test().assertResult();
            assertTrue(errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void otherErrorLate() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final AtomicReference<CompletableObserver> ref = new AtomicReference<>();
            Completable.complete().takeUntil(new Completable() {

                @Override
                protected void subscribeActual(CompletableObserver observer) {
                    observer.onSubscribe(Disposable.empty());
                    ref.set(observer);
                }
            }).test().assertResult();
            ref.get().onError(new TestException());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void otherCompleteLate() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final AtomicReference<CompletableObserver> ref = new AtomicReference<>();
            Completable.complete().takeUntil(new Completable() {

                @Override
                protected void subscribeActual(CompletableObserver observer) {
                    observer.onSubscribe(Disposable.empty());
                    ref.set(observer);
                }
            }).test().assertResult();
            ref.get().onComplete();
            assertTrue(errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public CompletableTakeUntilTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_consumerDisposes() throws java.lang.Throwable {
            this.payloads.consumerDisposes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompletes() throws java.lang.Throwable {
            this.payloads.mainCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherCompletes() throws java.lang.Throwable {
            this.payloads.otherCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrors() throws java.lang.Throwable {
            this.payloads.mainErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherErrors() throws java.lang.Throwable {
            this.payloads.otherErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposed() throws java.lang.Throwable {
            this.payloads.isDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorLate() throws java.lang.Throwable {
            this.payloads.mainErrorLate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompleteLate() throws java.lang.Throwable {
            this.payloads.mainCompleteLate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherErrorLate() throws java.lang.Throwable {
            this.payloads.otherErrorLate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherCompleteLate() throws java.lang.Throwable {
            this.payloads.otherCompleteLate.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTakeUntilTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTakeUntilTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTakeUntilTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTakeUntilTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableTakeUntilTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTakeUntilTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableTakeUntilTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableTakeUntilTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement consumerDisposes;

            public org.junit.runners.model.Statement mainCompletes;

            public org.junit.runners.model.Statement otherCompletes;

            public org.junit.runners.model.Statement mainErrors;

            public org.junit.runners.model.Statement otherErrors;

            public org.junit.runners.model.Statement isDisposed;

            public org.junit.runners.model.Statement mainErrorLate;

            public org.junit.runners.model.Statement mainCompleteLate;

            public org.junit.runners.model.Statement otherErrorLate;

            public org.junit.runners.model.Statement otherCompleteLate;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.consumerDisposes = _ClassStatement.forPayload(CompletableTakeUntilTest::consumerDisposes, "consumerDisposes", this);
            this.payloads.mainCompletes = _ClassStatement.forPayload(CompletableTakeUntilTest::mainCompletes, "mainCompletes", this);
            this.payloads.otherCompletes = _ClassStatement.forPayload(CompletableTakeUntilTest::otherCompletes, "otherCompletes", this);
            this.payloads.mainErrors = _ClassStatement.forPayload(CompletableTakeUntilTest::mainErrors, "mainErrors", this);
            this.payloads.otherErrors = _ClassStatement.forPayload(CompletableTakeUntilTest::otherErrors, "otherErrors", this);
            this.payloads.isDisposed = _ClassStatement.forPayload(CompletableTakeUntilTest::isDisposed, "isDisposed", this);
            this.payloads.mainErrorLate = _ClassStatement.forPayload(CompletableTakeUntilTest::mainErrorLate, "mainErrorLate", this);
            this.payloads.mainCompleteLate = _ClassStatement.forPayload(CompletableTakeUntilTest::mainCompleteLate, "mainCompleteLate", this);
            this.payloads.otherErrorLate = _ClassStatement.forPayload(CompletableTakeUntilTest::otherErrorLate, "otherErrorLate", this);
            this.payloads.otherCompleteLate = _ClassStatement.forPayload(CompletableTakeUntilTest::otherCompleteLate, "otherCompleteLate", this);
        }
    }
}
