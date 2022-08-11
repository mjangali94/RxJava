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
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class DisposableHelperTest extends RxJavaTest {

    @Test
    public void enumMethods() {
        assertEquals(1, DisposableHelper.values().length);
        assertNotNull(DisposableHelper.valueOf("DISPOSED"));
    }

    @Test
    public void innerDisposed() {
        assertTrue(DisposableHelper.DISPOSED.isDisposed());
        DisposableHelper.DISPOSED.dispose();
        assertTrue(DisposableHelper.DISPOSED.isDisposed());
    }

    @Test
    public void validationNull() {
        List<Throwable> list = TestHelper.trackPluginErrors();
        try {
            assertFalse(DisposableHelper.validate(null, null));
            TestHelper.assertError(list, 0, NullPointerException.class, "next is null");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void disposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Disposable> d = new AtomicReference<>();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    DisposableHelper.dispose(d);
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void setReplace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Disposable> d = new AtomicReference<>();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    DisposableHelper.replace(d, Disposable.empty());
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void setRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Disposable> d = new AtomicReference<>();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    DisposableHelper.set(d, Disposable.empty());
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void setReplaceNull() {
        final AtomicReference<Disposable> d = new AtomicReference<>();
        DisposableHelper.dispose(d);
        assertFalse(DisposableHelper.set(d, null));
        assertFalse(DisposableHelper.replace(d, null));
    }

    @Test
    public void dispose() {
        Disposable u = Disposable.empty();
        final AtomicReference<Disposable> d = new AtomicReference<>(u);
        DisposableHelper.dispose(d);
        assertTrue(u.isDisposed());
    }

    @Test
    public void trySet() {
        AtomicReference<Disposable> ref = new AtomicReference<>();
        Disposable d1 = Disposable.empty();
        assertTrue(DisposableHelper.trySet(ref, d1));
        Disposable d2 = Disposable.empty();
        assertFalse(DisposableHelper.trySet(ref, d2));
        assertFalse(d1.isDisposed());
        assertFalse(d2.isDisposed());
        DisposableHelper.dispose(ref);
        Disposable d3 = Disposable.empty();
        assertFalse(DisposableHelper.trySet(ref, d3));
        assertTrue(d3.isDisposed());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private DisposableHelperTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_enumMethods() throws java.lang.Throwable {
            this.payloads.enumMethods.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerDisposed() throws java.lang.Throwable {
            this.payloads.innerDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_validationNull() throws java.lang.Throwable {
            this.payloads.validationNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRace() throws java.lang.Throwable {
            this.payloads.disposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setReplace() throws java.lang.Throwable {
            this.payloads.setReplace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setRace() throws java.lang.Throwable {
            this.payloads.setRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setReplaceNull() throws java.lang.Throwable {
            this.payloads.setReplaceNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_trySet() throws java.lang.Throwable {
            this.payloads.trySet.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<DisposableHelperTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<DisposableHelperTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<DisposableHelperTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<DisposableHelperTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new DisposableHelperTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<DisposableHelperTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(DisposableHelperTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(DisposableHelperTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement enumMethods;

            public org.junit.runners.model.Statement innerDisposed;

            public org.junit.runners.model.Statement validationNull;

            public org.junit.runners.model.Statement disposeRace;

            public org.junit.runners.model.Statement setReplace;

            public org.junit.runners.model.Statement setRace;

            public org.junit.runners.model.Statement setReplaceNull;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement trySet;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.enumMethods = _ClassStatement.forPayload(DisposableHelperTest::enumMethods, "enumMethods", this);
            this.payloads.innerDisposed = _ClassStatement.forPayload(DisposableHelperTest::innerDisposed, "innerDisposed", this);
            this.payloads.validationNull = _ClassStatement.forPayload(DisposableHelperTest::validationNull, "validationNull", this);
            this.payloads.disposeRace = _ClassStatement.forPayload(DisposableHelperTest::disposeRace, "disposeRace", this);
            this.payloads.setReplace = _ClassStatement.forPayload(DisposableHelperTest::setReplace, "setReplace", this);
            this.payloads.setRace = _ClassStatement.forPayload(DisposableHelperTest::setRace, "setRace", this);
            this.payloads.setReplaceNull = _ClassStatement.forPayload(DisposableHelperTest::setReplaceNull, "setReplaceNull", this);
            this.payloads.dispose = _ClassStatement.forPayload(DisposableHelperTest::dispose, "dispose", this);
            this.payloads.trySet = _ClassStatement.forPayload(DisposableHelperTest::trySet, "trySet", this);
        }
    }
}
