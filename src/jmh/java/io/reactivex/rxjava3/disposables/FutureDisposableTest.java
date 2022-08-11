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
package io.reactivex.rxjava3.disposables;

import static org.junit.Assert.*;
import java.util.concurrent.FutureTask;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.internal.functions.Functions;

public class FutureDisposableTest extends RxJavaTest {

    @Test
    public void normal() {
        FutureTask<Object> ft = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
        Disposable d = Disposable.fromFuture(ft);
        assertFalse(d.isDisposed());
        d.dispose();
        assertTrue(d.isDisposed());
        d.dispose();
        assertTrue(d.isDisposed());
        assertTrue(ft.isCancelled());
    }

    @Test
    public void interruptible() {
        FutureTask<Object> ft = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
        Disposable d = Disposable.fromFuture(ft, true);
        assertFalse(d.isDisposed());
        d.dispose();
        assertTrue(d.isDisposed());
        d.dispose();
        assertTrue(d.isDisposed());
        assertTrue(ft.isCancelled());
    }

    @Test
    public void normalDone() {
        FutureTask<Object> ft = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
        FutureDisposable d = new FutureDisposable(ft, false);
        assertFalse(d.isDisposed());
        assertFalse(d.isDisposed());
        ft.run();
        assertTrue(d.isDisposed());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FutureDisposableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interruptible() throws java.lang.Throwable {
            this.payloads.interruptible.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDone() throws java.lang.Throwable {
            this.payloads.normalDone.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FutureDisposableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FutureDisposableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FutureDisposableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FutureDisposableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FutureDisposableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FutureDisposableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FutureDisposableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FutureDisposableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement interruptible;

            public org.junit.runners.model.Statement normalDone;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(FutureDisposableTest::normal, "normal", this);
            this.payloads.interruptible = _ClassStatement.forPayload(FutureDisposableTest::interruptible, "interruptible", this);
            this.payloads.normalDone = _ClassStatement.forPayload(FutureDisposableTest::normalDone, "normalDone", this);
        }
    }
}
