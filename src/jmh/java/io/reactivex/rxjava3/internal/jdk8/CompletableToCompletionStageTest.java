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
package io.reactivex.rxjava3.internal.jdk8;

import static org.junit.Assert.*;
import java.util.concurrent.CompletableFuture;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableToCompletionStageTest extends RxJavaTest {

    @Test
    public void complete() throws Exception {
        Object v = Completable.complete().toCompletionStage(null).toCompletableFuture().get();
        assertNull(v);
    }

    @Test
    public void completableFutureCancels() throws Exception {
        CompletableSubject source = CompletableSubject.create();
        CompletableFuture<Object> cf = source.toCompletionStage(null).toCompletableFuture();
        assertTrue(source.hasObservers());
        cf.cancel(true);
        assertTrue(cf.isCancelled());
        assertFalse(source.hasObservers());
    }

    @Test
    public void completableManualCompleteCancels() throws Exception {
        CompletableSubject source = CompletableSubject.create();
        CompletableFuture<Object> cf = source.toCompletionStage(null).toCompletableFuture();
        assertTrue(source.hasObservers());
        cf.complete(1);
        assertTrue(cf.isDone());
        assertFalse(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        assertFalse(source.hasObservers());
        assertEquals(1, cf.get());
    }

    @Test
    public void completableManualCompleteExceptionallyCancels() throws Exception {
        CompletableSubject source = CompletableSubject.create();
        CompletableFuture<Object> cf = source.toCompletionStage(null).toCompletableFuture();
        assertTrue(source.hasObservers());
        cf.completeExceptionally(new TestException());
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        assertFalse(source.hasObservers());
        TestHelper.assertError(cf, TestException.class);
    }

    @Test
    public void error() throws Exception {
        CompletableFuture<Object> cf = Completable.error(new TestException()).toCompletionStage(null).toCompletableFuture();
        assertTrue(cf.isDone());
        assertTrue(cf.isCompletedExceptionally());
        assertFalse(cf.isCancelled());
        TestHelper.assertError(cf, TestException.class);
    }

    @Test
    public void sourceIgnoresCancel() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Object v = new Completable() {

                @Override
                protected void subscribeActual(CompletableObserver observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onComplete();
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.toCompletionStage(null).toCompletableFuture().get();
            assertNull(v);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void doubleOnSubscribe() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Object v = new Completable() {

                @Override
                protected void subscribeActual(CompletableObserver observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onSubscribe(Disposable.empty());
                    observer.onComplete();
                }
            }.toCompletionStage(null).toCompletableFuture().get();
            assertNull(v);
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public CompletableToCompletionStageTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_complete() throws java.lang.Throwable {
            this.payloads.complete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableFutureCancels() throws java.lang.Throwable {
            this.payloads.completableFutureCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableManualCompleteCancels() throws java.lang.Throwable {
            this.payloads.completableManualCompleteCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableManualCompleteExceptionallyCancels() throws java.lang.Throwable {
            this.payloads.completableManualCompleteExceptionallyCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceIgnoresCancel() throws java.lang.Throwable {
            this.payloads.sourceIgnoresCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableToCompletionStageTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableToCompletionStageTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableToCompletionStageTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableToCompletionStageTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableToCompletionStageTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableToCompletionStageTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableToCompletionStageTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableToCompletionStageTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement complete;

            public org.junit.runners.model.Statement completableFutureCancels;

            public org.junit.runners.model.Statement completableManualCompleteCancels;

            public org.junit.runners.model.Statement completableManualCompleteExceptionallyCancels;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement sourceIgnoresCancel;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.complete = _ClassStatement.forPayload(CompletableToCompletionStageTest::complete, "complete", this);
            this.payloads.completableFutureCancels = _ClassStatement.forPayload(CompletableToCompletionStageTest::completableFutureCancels, "completableFutureCancels", this);
            this.payloads.completableManualCompleteCancels = _ClassStatement.forPayload(CompletableToCompletionStageTest::completableManualCompleteCancels, "completableManualCompleteCancels", this);
            this.payloads.completableManualCompleteExceptionallyCancels = _ClassStatement.forPayload(CompletableToCompletionStageTest::completableManualCompleteExceptionallyCancels, "completableManualCompleteExceptionallyCancels", this);
            this.payloads.error = _ClassStatement.forPayload(CompletableToCompletionStageTest::error, "error", this);
            this.payloads.sourceIgnoresCancel = _ClassStatement.forPayload(CompletableToCompletionStageTest::sourceIgnoresCancel, "sourceIgnoresCancel", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(CompletableToCompletionStageTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
