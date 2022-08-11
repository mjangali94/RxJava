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
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class CompletableDoOnTest extends RxJavaTest {

    @Test
    public void successAcceptThrows() {
        Completable.complete().doOnEvent(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void errorAcceptThrows() {
        TestObserverEx<Void> to = Completable.error(new TestException("Outer")).doOnEvent(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                throw new TestException("Inner");
            }
        }).to(TestHelper.<Void>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(errors, 0, TestException.class, "Outer");
        TestHelper.assertError(errors, 1, TestException.class, "Inner");
    }

    @Test
    public void doOnDisposeCalled() {
        final AtomicBoolean atomicBoolean = new AtomicBoolean();
        assertFalse(atomicBoolean.get());
        Completable.complete().doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                atomicBoolean.set(true);
            }
        }).test().assertResult().dispose();
        assertTrue(atomicBoolean.get());
    }

    @Test
    public void onSubscribeCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable bs = Disposable.empty();
            new Completable() {

                @Override
                protected void subscribeActual(CompletableObserver observer) {
                    observer.onSubscribe(bs);
                    observer.onError(new TestException("Second"));
                    observer.onComplete();
                }
            }.doOnSubscribe(new Consumer<Disposable>() {

                @Override
                public void accept(Disposable d) throws Exception {
                    throw new TestException("First");
                }
            }).to(TestHelper.<Void>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            assertTrue(bs.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private CompletableDoOnTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successAcceptThrows() throws java.lang.Throwable {
            this.payloads.successAcceptThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorAcceptThrows() throws java.lang.Throwable {
            this.payloads.errorAcceptThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeCalled() throws java.lang.Throwable {
            this.payloads.doOnDisposeCalled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeCrash() throws java.lang.Throwable {
            this.payloads.onSubscribeCrash.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDoOnTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDoOnTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDoOnTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDoOnTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableDoOnTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDoOnTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableDoOnTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableDoOnTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement successAcceptThrows;

            public org.junit.runners.model.Statement errorAcceptThrows;

            public org.junit.runners.model.Statement doOnDisposeCalled;

            public org.junit.runners.model.Statement onSubscribeCrash;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.successAcceptThrows = _ClassStatement.forPayload(CompletableDoOnTest::successAcceptThrows, "successAcceptThrows", this);
            this.payloads.errorAcceptThrows = _ClassStatement.forPayload(CompletableDoOnTest::errorAcceptThrows, "errorAcceptThrows", this);
            this.payloads.doOnDisposeCalled = _ClassStatement.forPayload(CompletableDoOnTest::doOnDisposeCalled, "doOnDisposeCalled", this);
            this.payloads.onSubscribeCrash = _ClassStatement.forPayload(CompletableDoOnTest::onSubscribeCrash, "onSubscribeCrash", this);
        }
    }
}
