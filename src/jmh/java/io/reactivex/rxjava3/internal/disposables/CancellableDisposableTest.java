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
package io.reactivex.rxjava3.internal.disposables;

import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Cancellable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CancellableDisposableTest extends RxJavaTest {

    @Test
    public void normal() {
        final AtomicInteger count = new AtomicInteger();
        Cancellable c = new Cancellable() {

            @Override
            public void cancel() throws Exception {
                count.getAndIncrement();
            }
        };
        CancellableDisposable cd = new CancellableDisposable(c);
        assertFalse(cd.isDisposed());
        cd.dispose();
        cd.dispose();
        assertTrue(cd.isDisposed());
        assertEquals(1, count.get());
    }

    @Test
    public void cancelThrows() {
        final AtomicInteger count = new AtomicInteger();
        Cancellable c = new Cancellable() {

            @Override
            public void cancel() throws Exception {
                count.getAndIncrement();
                throw new TestException();
            }
        };
        CancellableDisposable cd = new CancellableDisposable(c);
        assertFalse(cd.isDisposed());
        List<Throwable> list = TestHelper.trackPluginErrors();
        try {
            cd.dispose();
            cd.dispose();
            TestHelper.assertUndeliverable(list, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
        assertTrue(cd.isDisposed());
        assertEquals(1, count.get());
    }

    @Test
    public void disposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicInteger count = new AtomicInteger();
            Cancellable c = new Cancellable() {

                @Override
                public void cancel() throws Exception {
                    count.getAndIncrement();
                }
            };
            final CancellableDisposable cd = new CancellableDisposable(c);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            TestHelper.race(r, r);
            assertEquals(1, count.get());
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private CancellableDisposableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelThrows() throws java.lang.Throwable {
            this.payloads.cancelThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRace() throws java.lang.Throwable {
            this.payloads.disposeRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CancellableDisposableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CancellableDisposableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CancellableDisposableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CancellableDisposableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CancellableDisposableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CancellableDisposableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CancellableDisposableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CancellableDisposableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement cancelThrows;

            public org.junit.runners.model.Statement disposeRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(CancellableDisposableTest::normal, "normal", this);
            this.payloads.cancelThrows = _ClassStatement.forPayload(CancellableDisposableTest::cancelThrows, "cancelThrows", this);
            this.payloads.disposeRace = _ClassStatement.forPayload(CancellableDisposableTest::disposeRace, "disposeRace", this);
        }
    }
}
