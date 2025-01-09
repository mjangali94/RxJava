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
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableCacheTest extends RxJavaTest implements Consumer<Object>, Action {

    volatile int count;

    @Override
    public void accept(Object t) throws Exception {
        count++;
    }

    @Override
    public void run() throws Exception {
        count++;
    }

    @Test
    public void normal() {
        Completable c = Completable.complete().doOnSubscribe(this).cache();
        assertEquals(0, count);
        c.test().assertResult();
        assertEquals(1, count);
        c.test().assertResult();
        assertEquals(1, count);
        c.test().assertResult();
        assertEquals(1, count);
    }

    @Test
    public void error() {
        Completable c = Completable.error(new TestException()).doOnSubscribe(this).cache();
        assertEquals(0, count);
        c.test().assertFailure(TestException.class);
        assertEquals(1, count);
        c.test().assertFailure(TestException.class);
        assertEquals(1, count);
        c.test().assertFailure(TestException.class);
        assertEquals(1, count);
    }

    @Test
    public void crossDispose() {
        PublishSubject<Integer> ps = PublishSubject.create();
        final TestObserver<Void> to1 = new TestObserver<>();
        final TestObserver<Void> to2 = new TestObserver<Void>() {

            @Override
            public void onComplete() {
                super.onComplete();
                to1.dispose();
            }
        };
        Completable c = ps.ignoreElements().cache();
        c.subscribe(to2);
        c.subscribe(to1);
        ps.onComplete();
        to1.assertEmpty();
        to2.assertResult();
    }

    @Test
    public void crossDisposeOnError() {
        PublishSubject<Integer> ps = PublishSubject.create();
        final TestObserver<Void> to1 = new TestObserver<>();
        final TestObserver<Void> to2 = new TestObserver<Void>() {

            @Override
            public void onError(Throwable ex) {
                super.onError(ex);
                to1.dispose();
            }
        };
        Completable c = ps.ignoreElements().cache();
        c.subscribe(to2);
        c.subscribe(to1);
        ps.onError(new TestException());
        to1.assertEmpty();
        to2.assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        PublishSubject<Integer> ps = PublishSubject.create();
        Completable c = ps.ignoreElements().cache();
        assertFalse(ps.hasObservers());
        TestObserver<Void> to1 = c.test();
        assertTrue(ps.hasObservers());
        to1.dispose();
        assertTrue(ps.hasObservers());
        TestObserver<Void> to2 = c.test();
        TestObserver<Void> to3 = c.test();
        to3.dispose();
        TestObserver<Void> to4 = c.test(true);
        to3.dispose();
        ps.onComplete();
        to1.assertEmpty();
        to2.assertResult();
        to3.assertEmpty();
        to4.assertEmpty();
    }

    @Test
    public void subscribeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishSubject<Integer> ps = PublishSubject.create();
            final Completable c = ps.ignoreElements().cache();
            final TestObserver<Void> to1 = new TestObserver<>();
            final TestObserver<Void> to2 = new TestObserver<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    c.subscribe(to1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    c.subscribe(to2);
                }
            };
            TestHelper.race(r1, r2);
            ps.onComplete();
            to1.assertResult();
            to2.assertResult();
        }
    }

    @Test
    public void subscribeDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishSubject<Integer> ps = PublishSubject.create();
            final Completable c = ps.ignoreElements().cache();
            final TestObserver<Void> to1 = c.test();
            final TestObserver<Void> to2 = new TestObserver<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to1.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    c.subscribe(to2);
                }
            };
            TestHelper.race(r1, r2);
            ps.onComplete();
            to1.assertEmpty();
            to2.assertResult();
        }
    }

    @Test
    public void doubleDispose() {
        PublishSubject<Integer> ps = PublishSubject.create();
        final TestObserver<Void> to = new TestObserver<>();
        ps.ignoreElements().cache().subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
                to.onSubscribe(EmptyDisposable.INSTANCE);
                d.dispose();
                d.dispose();
            }

            @Override
            public void onComplete() {
                to.onComplete();
            }

            @Override
            public void onError(Throwable e) {
                to.onError(e);
            }
        });
        ps.onComplete();
        to.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public CompletableCacheTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crossDispose() throws java.lang.Throwable {
            this.payloads.crossDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crossDisposeOnError() throws java.lang.Throwable {
            this.payloads.crossDisposeOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeRace() throws java.lang.Throwable {
            this.payloads.subscribeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeDisposeRace() throws java.lang.Throwable {
            this.payloads.subscribeDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleDispose() throws java.lang.Throwable {
            this.payloads.doubleDispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableCacheTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableCacheTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableCacheTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableCacheTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableCacheTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableCacheTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableCacheTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableCacheTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement crossDispose;

            public org.junit.runners.model.Statement crossDisposeOnError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement subscribeRace;

            public org.junit.runners.model.Statement subscribeDisposeRace;

            public org.junit.runners.model.Statement doubleDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(CompletableCacheTest::normal, "normal", this);
            this.payloads.error = _ClassStatement.forPayload(CompletableCacheTest::error, "error", this);
            this.payloads.crossDispose = _ClassStatement.forPayload(CompletableCacheTest::crossDispose, "crossDispose", this);
            this.payloads.crossDisposeOnError = _ClassStatement.forPayload(CompletableCacheTest::crossDisposeOnError, "crossDisposeOnError", this);
            this.payloads.dispose = _ClassStatement.forPayload(CompletableCacheTest::dispose, "dispose", this);
            this.payloads.subscribeRace = _ClassStatement.forPayload(CompletableCacheTest::subscribeRace, "subscribeRace", this);
            this.payloads.subscribeDisposeRace = _ClassStatement.forPayload(CompletableCacheTest::subscribeDisposeRace, "subscribeDisposeRace", this);
            this.payloads.doubleDispose = _ClassStatement.forPayload(CompletableCacheTest::doubleDispose, "doubleDispose", this);
        }
    }
}
