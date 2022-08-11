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
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ArrayCompositeDisposableTest extends RxJavaTest {

    @Test
    public void normal() {
        ArrayCompositeDisposable acd = new ArrayCompositeDisposable(2);
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        assertTrue(acd.setResource(0, d1));
        assertTrue(acd.setResource(1, d2));
        Disposable d3 = Disposable.empty();
        Disposable d4 = Disposable.empty();
        acd.replaceResource(0, d3);
        acd.replaceResource(1, d4);
        assertFalse(d1.isDisposed());
        assertFalse(d2.isDisposed());
        acd.setResource(0, d1);
        acd.setResource(1, d2);
        assertTrue(d3.isDisposed());
        assertTrue(d4.isDisposed());
        assertFalse(acd.isDisposed());
        acd.dispose();
        acd.dispose();
        assertTrue(acd.isDisposed());
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        Disposable d5 = Disposable.empty();
        Disposable d6 = Disposable.empty();
        assertFalse(acd.setResource(0, d5));
        acd.replaceResource(1, d6);
        assertTrue(d5.isDisposed());
        assertTrue(d6.isDisposed());
    }

    @Test
    public void disposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ArrayCompositeDisposable acd = new ArrayCompositeDisposable(2);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    acd.dispose();
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void replaceRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ArrayCompositeDisposable acd = new ArrayCompositeDisposable(2);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    acd.replaceResource(0, Disposable.empty());
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void setRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ArrayCompositeDisposable acd = new ArrayCompositeDisposable(2);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    acd.setResource(0, Disposable.empty());
                }
            };
            TestHelper.race(r, r);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ArrayCompositeDisposableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRace() throws java.lang.Throwable {
            this.payloads.disposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaceRace() throws java.lang.Throwable {
            this.payloads.replaceRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setRace() throws java.lang.Throwable {
            this.payloads.setRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ArrayCompositeDisposableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ArrayCompositeDisposableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ArrayCompositeDisposableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ArrayCompositeDisposableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ArrayCompositeDisposableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ArrayCompositeDisposableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ArrayCompositeDisposableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ArrayCompositeDisposableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement disposeRace;

            public org.junit.runners.model.Statement replaceRace;

            public org.junit.runners.model.Statement setRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(ArrayCompositeDisposableTest::normal, "normal", this);
            this.payloads.disposeRace = _ClassStatement.forPayload(ArrayCompositeDisposableTest::disposeRace, "disposeRace", this);
            this.payloads.replaceRace = _ClassStatement.forPayload(ArrayCompositeDisposableTest::replaceRace, "replaceRace", this);
            this.payloads.setRace = _ClassStatement.forPayload(ArrayCompositeDisposableTest::setRace, "setRace", this);
        }
    }
}
