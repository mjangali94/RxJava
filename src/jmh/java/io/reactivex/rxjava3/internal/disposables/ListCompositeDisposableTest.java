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
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ListCompositeDisposableTest extends RxJavaTest {

    @Test
    public void constructorAndAddVarargs() {
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        ListCompositeDisposable lcd = new ListCompositeDisposable(d1, d2);
        lcd.clear();
        assertFalse(lcd.isDisposed());
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        d1 = Disposable.empty();
        d2 = Disposable.empty();
        lcd.addAll(d1, d2);
        lcd.dispose();
        assertTrue(lcd.isDisposed());
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
    }

    @Test
    public void constructorIterable() {
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        ListCompositeDisposable lcd = new ListCompositeDisposable(Arrays.asList(d1, d2));
        lcd.clear();
        assertFalse(lcd.isDisposed());
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        d1 = Disposable.empty();
        d2 = Disposable.empty();
        lcd.add(d1);
        lcd.addAll(d2);
        lcd.dispose();
        assertTrue(lcd.isDisposed());
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
    }

    @Test
    public void empty() {
        ListCompositeDisposable lcd = new ListCompositeDisposable();
        assertFalse(lcd.isDisposed());
        lcd.clear();
        assertFalse(lcd.isDisposed());
        lcd.dispose();
        lcd.dispose();
        lcd.clear();
        assertTrue(lcd.isDisposed());
    }

    @Test
    public void afterDispose() {
        ListCompositeDisposable lcd = new ListCompositeDisposable();
        lcd.dispose();
        Disposable d = Disposable.empty();
        assertFalse(lcd.add(d));
        assertTrue(d.isDisposed());
        d = Disposable.empty();
        assertFalse(lcd.addAll(d));
        assertTrue(d.isDisposed());
    }

    @Test
    public void disposeThrows() {
        Disposable d = new Disposable() {

            @Override
            public void dispose() {
                throw new TestException();
            }

            @Override
            public boolean isDisposed() {
                return false;
            }
        };
        ListCompositeDisposable lcd = new ListCompositeDisposable(d, d);
        try {
            lcd.dispose();
            fail("Should have thrown!");
        } catch (CompositeException ex) {
            List<Throwable> list = ex.getExceptions();
            TestHelper.assertError(list, 0, TestException.class);
            TestHelper.assertError(list, 1, TestException.class);
        }
        lcd = new ListCompositeDisposable(d);
        try {
            lcd.dispose();
            fail("Should have thrown!");
        } catch (TestException ex) {
        // expected
        }
    }

    @Test
    public void remove() {
        ListCompositeDisposable lcd = new ListCompositeDisposable();
        Disposable d = Disposable.empty();
        lcd.add(d);
        assertTrue(lcd.delete(d));
        assertFalse(d.isDisposed());
        lcd.add(d);
        assertTrue(lcd.remove(d));
        assertTrue(d.isDisposed());
        assertFalse(lcd.remove(d));
        assertFalse(lcd.delete(d));
        lcd = new ListCompositeDisposable();
        assertFalse(lcd.remove(d));
        assertFalse(lcd.delete(d));
    }

    @Test
    public void disposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void addRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.add(Disposable.empty());
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void addAllRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.addAll(Disposable.empty());
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void removeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.remove(d1);
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void deleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.delete(d1);
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void clearRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.clear();
                }
            };
            TestHelper.race(run, run);
        }
    }

    @Test
    public void addDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.add(Disposable.empty());
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void addAllDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.addAll(Disposable.empty());
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void removeDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.remove(d1);
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void deleteDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.delete(d1);
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void clearDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final ListCompositeDisposable cd = new ListCompositeDisposable();
            final Disposable d1 = Disposable.empty();
            cd.add(d1);
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    cd.dispose();
                }
            };
            Runnable run2 = new Runnable() {

                @Override
                public void run() {
                    cd.clear();
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ListCompositeDisposableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_constructorAndAddVarargs() throws java.lang.Throwable {
            this.payloads.constructorAndAddVarargs.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_constructorIterable() throws java.lang.Throwable {
            this.payloads.constructorIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_afterDispose() throws java.lang.Throwable {
            this.payloads.afterDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeThrows() throws java.lang.Throwable {
            this.payloads.disposeThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_remove() throws java.lang.Throwable {
            this.payloads.remove.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeRace() throws java.lang.Throwable {
            this.payloads.disposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addRace() throws java.lang.Throwable {
            this.payloads.addRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addAllRace() throws java.lang.Throwable {
            this.payloads.addAllRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_removeRace() throws java.lang.Throwable {
            this.payloads.removeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deleteRace() throws java.lang.Throwable {
            this.payloads.deleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clearRace() throws java.lang.Throwable {
            this.payloads.clearRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addDisposeRace() throws java.lang.Throwable {
            this.payloads.addDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addAllDisposeRace() throws java.lang.Throwable {
            this.payloads.addAllDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_removeDisposeRace() throws java.lang.Throwable {
            this.payloads.removeDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deleteDisposeRace() throws java.lang.Throwable {
            this.payloads.deleteDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clearDisposeRace() throws java.lang.Throwable {
            this.payloads.clearDisposeRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ListCompositeDisposableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ListCompositeDisposableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ListCompositeDisposableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ListCompositeDisposableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ListCompositeDisposableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ListCompositeDisposableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ListCompositeDisposableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ListCompositeDisposableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement constructorAndAddVarargs;

            public org.junit.runners.model.Statement constructorIterable;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement afterDispose;

            public org.junit.runners.model.Statement disposeThrows;

            public org.junit.runners.model.Statement remove;

            public org.junit.runners.model.Statement disposeRace;

            public org.junit.runners.model.Statement addRace;

            public org.junit.runners.model.Statement addAllRace;

            public org.junit.runners.model.Statement removeRace;

            public org.junit.runners.model.Statement deleteRace;

            public org.junit.runners.model.Statement clearRace;

            public org.junit.runners.model.Statement addDisposeRace;

            public org.junit.runners.model.Statement addAllDisposeRace;

            public org.junit.runners.model.Statement removeDisposeRace;

            public org.junit.runners.model.Statement deleteDisposeRace;

            public org.junit.runners.model.Statement clearDisposeRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.constructorAndAddVarargs = _ClassStatement.forPayload(ListCompositeDisposableTest::constructorAndAddVarargs, "constructorAndAddVarargs", this);
            this.payloads.constructorIterable = _ClassStatement.forPayload(ListCompositeDisposableTest::constructorIterable, "constructorIterable", this);
            this.payloads.empty = _ClassStatement.forPayload(ListCompositeDisposableTest::empty, "empty", this);
            this.payloads.afterDispose = _ClassStatement.forPayload(ListCompositeDisposableTest::afterDispose, "afterDispose", this);
            this.payloads.disposeThrows = _ClassStatement.forPayload(ListCompositeDisposableTest::disposeThrows, "disposeThrows", this);
            this.payloads.remove = _ClassStatement.forPayload(ListCompositeDisposableTest::remove, "remove", this);
            this.payloads.disposeRace = _ClassStatement.forPayload(ListCompositeDisposableTest::disposeRace, "disposeRace", this);
            this.payloads.addRace = _ClassStatement.forPayload(ListCompositeDisposableTest::addRace, "addRace", this);
            this.payloads.addAllRace = _ClassStatement.forPayload(ListCompositeDisposableTest::addAllRace, "addAllRace", this);
            this.payloads.removeRace = _ClassStatement.forPayload(ListCompositeDisposableTest::removeRace, "removeRace", this);
            this.payloads.deleteRace = _ClassStatement.forPayload(ListCompositeDisposableTest::deleteRace, "deleteRace", this);
            this.payloads.clearRace = _ClassStatement.forPayload(ListCompositeDisposableTest::clearRace, "clearRace", this);
            this.payloads.addDisposeRace = _ClassStatement.forPayload(ListCompositeDisposableTest::addDisposeRace, "addDisposeRace", this);
            this.payloads.addAllDisposeRace = _ClassStatement.forPayload(ListCompositeDisposableTest::addAllDisposeRace, "addAllDisposeRace", this);
            this.payloads.removeDisposeRace = _ClassStatement.forPayload(ListCompositeDisposableTest::removeDisposeRace, "removeDisposeRace", this);
            this.payloads.deleteDisposeRace = _ClassStatement.forPayload(ListCompositeDisposableTest::deleteDisposeRace, "deleteDisposeRace", this);
            this.payloads.clearDisposeRace = _ClassStatement.forPayload(ListCompositeDisposableTest::clearDisposeRace, "clearDisposeRace", this);
        }
    }
}
