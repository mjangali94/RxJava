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
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompositeDisposableTest extends RxJavaTest {

    @Test
    public void success() {
        final AtomicInteger counter = new AtomicInteger();
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                counter.incrementAndGet();
            }
        }));
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                counter.incrementAndGet();
            }
        }));
        cd.dispose();
        assertEquals(2, counter.get());
    }

    @Test
    public void shouldUnsubscribeAll() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        final CompositeDisposable cd = new CompositeDisposable();
        final int count = 10;
        final CountDownLatch start = new CountDownLatch(1);
        for (int i = 0; i < count; i++) {
            cd.add(Disposable.fromRunnable(new Runnable() {

                @Override
                public void run() {
                    counter.incrementAndGet();
                }
            }));
        }
        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        start.await();
                        cd.dispose();
                    } catch (final InterruptedException e) {
                        fail(e.getMessage());
                    }
                }
            };
            t.start();
            threads.add(t);
        }
        start.countDown();
        for (final Thread t : threads) {
            t.join();
        }
        assertEquals(count, counter.get());
    }

    @Test
    public void exception() {
        final AtomicInteger counter = new AtomicInteger();
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                throw new RuntimeException("failed on first one");
            }
        }));
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                counter.incrementAndGet();
            }
        }));
        try {
            cd.dispose();
            fail("Expecting an exception");
        } catch (RuntimeException e) {
            // we expect this
            assertEquals(e.getMessage(), "failed on first one");
        }
        // we should still have disposed to the second one
        assertEquals(1, counter.get());
    }

    @Test
    public void compositeException() {
        final AtomicInteger counter = new AtomicInteger();
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                throw new RuntimeException("failed on first one");
            }
        }));
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                throw new RuntimeException("failed on second one too");
            }
        }));
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                counter.incrementAndGet();
            }
        }));
        try {
            cd.dispose();
            fail("Expecting an exception");
        } catch (CompositeException e) {
            // we expect this
            assertEquals(e.getExceptions().size(), 2);
        }
        // we should still have disposed to the second one
        assertEquals(1, counter.get());
    }

    @Test
    public void removeUnsubscribes() {
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(d1);
        cd.add(d2);
        cd.remove(d1);
        assertTrue(d1.isDisposed());
        assertFalse(d2.isDisposed());
    }

    @Test
    public void clear() {
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(d1);
        cd.add(d2);
        assertFalse(d1.isDisposed());
        assertFalse(d2.isDisposed());
        cd.clear();
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        assertFalse(cd.isDisposed());
        Disposable d3 = Disposable.empty();
        cd.add(d3);
        cd.dispose();
        assertTrue(d3.isDisposed());
        assertTrue(cd.isDisposed());
    }

    @Test
    public void unsubscribeIdempotence() {
        final AtomicInteger counter = new AtomicInteger();
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                counter.incrementAndGet();
            }
        }));
        cd.dispose();
        cd.dispose();
        cd.dispose();
        // we should have only disposed once
        assertEquals(1, counter.get());
    }

    @Test
    public void unsubscribeIdempotenceConcurrently() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        final CompositeDisposable cd = new CompositeDisposable();
        final int count = 10;
        final CountDownLatch start = new CountDownLatch(1);
        cd.add(Disposable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                counter.incrementAndGet();
            }
        }));
        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        start.await();
                        cd.dispose();
                    } catch (final InterruptedException e) {
                        fail(e.getMessage());
                    }
                }
            };
            t.start();
            threads.add(t);
        }
        start.countDown();
        for (final Thread t : threads) {
            t.join();
        }
        // we should have only disposed once
        assertEquals(1, counter.get());
    }

    @Test
    public void tryRemoveIfNotIn() {
        CompositeDisposable cd = new CompositeDisposable();
        CompositeDisposable cd1 = new CompositeDisposable();
        CompositeDisposable cd2 = new CompositeDisposable();
        cd.add(cd1);
        cd.remove(cd1);
        cd.add(cd2);
        // try removing agian
        cd.remove(cd1);
    }

    @Test(expected = NullPointerException.class)
    public void addingNullDisposableIllegal() {
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(null);
    }

    @Test
    public void initializeVarargs() {
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        CompositeDisposable cd = new CompositeDisposable(d1, d2);
        assertEquals(2, cd.size());
        cd.clear();
        assertEquals(0, cd.size());
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        Disposable d3 = Disposable.empty();
        Disposable d4 = Disposable.empty();
        cd = new CompositeDisposable(d3, d4);
        cd.dispose();
        assertTrue(d3.isDisposed());
        assertTrue(d4.isDisposed());
        assertEquals(0, cd.size());
    }

    @Test
    public void initializeIterable() {
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        CompositeDisposable cd = new CompositeDisposable(Arrays.asList(d1, d2));
        assertEquals(2, cd.size());
        cd.clear();
        assertEquals(0, cd.size());
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        Disposable d3 = Disposable.empty();
        Disposable d4 = Disposable.empty();
        cd = new CompositeDisposable(Arrays.asList(d3, d4));
        assertEquals(2, cd.size());
        cd.dispose();
        assertTrue(d3.isDisposed());
        assertTrue(d4.isDisposed());
        assertEquals(0, cd.size());
    }

    @Test
    public void addAll() {
        CompositeDisposable cd = new CompositeDisposable();
        Disposable d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        Disposable d3 = Disposable.empty();
        cd.addAll(d1, d2);
        cd.addAll(d3);
        assertFalse(d1.isDisposed());
        assertFalse(d2.isDisposed());
        assertFalse(d3.isDisposed());
        cd.clear();
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        d1 = Disposable.empty();
        d2 = Disposable.empty();
        cd = new CompositeDisposable();
        cd.addAll(d1, d2);
        assertFalse(d1.isDisposed());
        assertFalse(d2.isDisposed());
        cd.dispose();
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
        assertEquals(0, cd.size());
        cd.clear();
        assertEquals(0, cd.size());
    }

    @Test
    public void addAfterDisposed() {
        CompositeDisposable cd = new CompositeDisposable();
        cd.dispose();
        Disposable d1 = Disposable.empty();
        assertFalse(cd.add(d1));
        assertTrue(d1.isDisposed());
        d1 = Disposable.empty();
        Disposable d2 = Disposable.empty();
        assertFalse(cd.addAll(d1, d2));
        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
    }

    @Test
    public void delete() {
        CompositeDisposable cd = new CompositeDisposable();
        Disposable d1 = Disposable.empty();
        assertFalse(cd.delete(d1));
        Disposable d2 = Disposable.empty();
        cd.add(d2);
        assertFalse(cd.delete(d1));
        cd.dispose();
        assertFalse(cd.delete(d1));
    }

    @Test
    public void disposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final CompositeDisposable cd = new CompositeDisposable();
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
            final CompositeDisposable cd = new CompositeDisposable();
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
            final CompositeDisposable cd = new CompositeDisposable();
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
            final CompositeDisposable cd = new CompositeDisposable();
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
            final CompositeDisposable cd = new CompositeDisposable();
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
            final CompositeDisposable cd = new CompositeDisposable();
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
            final CompositeDisposable cd = new CompositeDisposable();
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
            final CompositeDisposable cd = new CompositeDisposable();
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
            final CompositeDisposable cd = new CompositeDisposable();
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
            final CompositeDisposable cd = new CompositeDisposable();
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
            final CompositeDisposable cd = new CompositeDisposable();
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

    @Test
    public void sizeDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final CompositeDisposable cd = new CompositeDisposable();
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
                    cd.size();
                }
            };
            TestHelper.race(run, run2);
        }
    }

    @Test
    public void disposeThrowsIAE() {
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(Disposable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                throw new IllegalArgumentException();
            }
        }));
        Disposable d1 = Disposable.empty();
        cd.add(d1);
        try {
            cd.dispose();
            fail("Failed to throw");
        } catch (IllegalArgumentException ex) {
        // expected
        }
        assertTrue(d1.isDisposed());
    }

    @Test
    public void disposeThrowsError() {
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(Disposable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                throw new AssertionError();
            }
        }));
        Disposable d1 = Disposable.empty();
        cd.add(d1);
        try {
            cd.dispose();
            fail("Failed to throw");
        } catch (AssertionError ex) {
        // expected
        }
        assertTrue(d1.isDisposed());
    }

    @Test
    public void disposeThrowsCheckedException() {
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(Disposable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                throw new IOException();
            }
        }));
        Disposable d1 = Disposable.empty();
        cd.add(d1);
        try {
            cd.dispose();
            fail("Failed to throw");
        } catch (RuntimeException ex) {
            // expected
            if (!(ex.getCause() instanceof IOException)) {
                fail(ex.toString() + " should have thrown RuntimeException(IOException)");
            }
        }
        assertTrue(d1.isDisposed());
    }

    @SuppressWarnings("unchecked")
    static <E extends Throwable> void throwSneaky() throws E {
        throw (E) new IOException();
    }

    @Test
    public void disposeThrowsCheckedExceptionSneaky() {
        CompositeDisposable cd = new CompositeDisposable();
        cd.add(new Disposable() {

            @Override
            public void dispose() {
                CompositeDisposableTest.<RuntimeException>throwSneaky();
            }

            @Override
            public boolean isDisposed() {
                // TODO Auto-generated method stub
                return false;
            }
        });
        Disposable d1 = Disposable.empty();
        cd.add(d1);
        try {
            cd.dispose();
            fail("Failed to throw");
        } catch (RuntimeException ex) {
            // expected
            if (!(ex.getCause() instanceof IOException)) {
                fail(ex.toString() + " should have thrown RuntimeException(IOException)");
            }
        }
        assertTrue(d1.isDisposed());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public CompositeDisposableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_success() throws java.lang.Throwable {
            this.payloads.success.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldUnsubscribeAll() throws java.lang.Throwable {
            this.payloads.shouldUnsubscribeAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exception() throws java.lang.Throwable {
            this.payloads.exception.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_compositeException() throws java.lang.Throwable {
            this.payloads.compositeException.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_removeUnsubscribes() throws java.lang.Throwable {
            this.payloads.removeUnsubscribes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_clear() throws java.lang.Throwable {
            this.payloads.clear.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeIdempotence() throws java.lang.Throwable {
            this.payloads.unsubscribeIdempotence.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeIdempotenceConcurrently() throws java.lang.Throwable {
            this.payloads.unsubscribeIdempotenceConcurrently.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryRemoveIfNotIn() throws java.lang.Throwable {
            this.payloads.tryRemoveIfNotIn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addingNullDisposableIllegal() throws java.lang.Throwable {
            this.payloads.addingNullDisposableIllegal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_initializeVarargs() throws java.lang.Throwable {
            this.payloads.initializeVarargs.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_initializeIterable() throws java.lang.Throwable {
            this.payloads.initializeIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addAll() throws java.lang.Throwable {
            this.payloads.addAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addAfterDisposed() throws java.lang.Throwable {
            this.payloads.addAfterDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delete() throws java.lang.Throwable {
            this.payloads.delete.evaluate();
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

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sizeDisposeRace() throws java.lang.Throwable {
            this.payloads.sizeDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeThrowsIAE() throws java.lang.Throwable {
            this.payloads.disposeThrowsIAE.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeThrowsError() throws java.lang.Throwable {
            this.payloads.disposeThrowsError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeThrowsCheckedException() throws java.lang.Throwable {
            this.payloads.disposeThrowsCheckedException.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeThrowsCheckedExceptionSneaky() throws java.lang.Throwable {
            this.payloads.disposeThrowsCheckedExceptionSneaky.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompositeDisposableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompositeDisposableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompositeDisposableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompositeDisposableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompositeDisposableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompositeDisposableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompositeDisposableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompositeDisposableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement success;

            public org.junit.runners.model.Statement shouldUnsubscribeAll;

            public org.junit.runners.model.Statement exception;

            public org.junit.runners.model.Statement compositeException;

            public org.junit.runners.model.Statement removeUnsubscribes;

            public org.junit.runners.model.Statement clear;

            public org.junit.runners.model.Statement unsubscribeIdempotence;

            public org.junit.runners.model.Statement unsubscribeIdempotenceConcurrently;

            public org.junit.runners.model.Statement tryRemoveIfNotIn;

            public org.junit.runners.model.Statement addingNullDisposableIllegal;

            public org.junit.runners.model.Statement initializeVarargs;

            public org.junit.runners.model.Statement initializeIterable;

            public org.junit.runners.model.Statement addAll;

            public org.junit.runners.model.Statement addAfterDisposed;

            public org.junit.runners.model.Statement delete;

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

            public org.junit.runners.model.Statement sizeDisposeRace;

            public org.junit.runners.model.Statement disposeThrowsIAE;

            public org.junit.runners.model.Statement disposeThrowsError;

            public org.junit.runners.model.Statement disposeThrowsCheckedException;

            public org.junit.runners.model.Statement disposeThrowsCheckedExceptionSneaky;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.success = _ClassStatement.forPayload(CompositeDisposableTest::success, "success", this);
            this.payloads.shouldUnsubscribeAll = _ClassStatement.forPayload(CompositeDisposableTest::shouldUnsubscribeAll, "shouldUnsubscribeAll", this);
            this.payloads.exception = _ClassStatement.forPayload(CompositeDisposableTest::exception, "exception", this);
            this.payloads.compositeException = _ClassStatement.forPayload(CompositeDisposableTest::compositeException, "compositeException", this);
            this.payloads.removeUnsubscribes = _ClassStatement.forPayload(CompositeDisposableTest::removeUnsubscribes, "removeUnsubscribes", this);
            this.payloads.clear = _ClassStatement.forPayload(CompositeDisposableTest::clear, "clear", this);
            this.payloads.unsubscribeIdempotence = _ClassStatement.forPayload(CompositeDisposableTest::unsubscribeIdempotence, "unsubscribeIdempotence", this);
            this.payloads.unsubscribeIdempotenceConcurrently = _ClassStatement.forPayload(CompositeDisposableTest::unsubscribeIdempotenceConcurrently, "unsubscribeIdempotenceConcurrently", this);
            this.payloads.tryRemoveIfNotIn = _ClassStatement.forPayload(CompositeDisposableTest::tryRemoveIfNotIn, "tryRemoveIfNotIn", this);
            this.payloads.addingNullDisposableIllegal = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompositeDisposableTest::addingNullDisposableIllegal, java.lang.NullPointerException.class), "addingNullDisposableIllegal", this);
            this.payloads.initializeVarargs = _ClassStatement.forPayload(CompositeDisposableTest::initializeVarargs, "initializeVarargs", this);
            this.payloads.initializeIterable = _ClassStatement.forPayload(CompositeDisposableTest::initializeIterable, "initializeIterable", this);
            this.payloads.addAll = _ClassStatement.forPayload(CompositeDisposableTest::addAll, "addAll", this);
            this.payloads.addAfterDisposed = _ClassStatement.forPayload(CompositeDisposableTest::addAfterDisposed, "addAfterDisposed", this);
            this.payloads.delete = _ClassStatement.forPayload(CompositeDisposableTest::delete, "delete", this);
            this.payloads.disposeRace = _ClassStatement.forPayload(CompositeDisposableTest::disposeRace, "disposeRace", this);
            this.payloads.addRace = _ClassStatement.forPayload(CompositeDisposableTest::addRace, "addRace", this);
            this.payloads.addAllRace = _ClassStatement.forPayload(CompositeDisposableTest::addAllRace, "addAllRace", this);
            this.payloads.removeRace = _ClassStatement.forPayload(CompositeDisposableTest::removeRace, "removeRace", this);
            this.payloads.deleteRace = _ClassStatement.forPayload(CompositeDisposableTest::deleteRace, "deleteRace", this);
            this.payloads.clearRace = _ClassStatement.forPayload(CompositeDisposableTest::clearRace, "clearRace", this);
            this.payloads.addDisposeRace = _ClassStatement.forPayload(CompositeDisposableTest::addDisposeRace, "addDisposeRace", this);
            this.payloads.addAllDisposeRace = _ClassStatement.forPayload(CompositeDisposableTest::addAllDisposeRace, "addAllDisposeRace", this);
            this.payloads.removeDisposeRace = _ClassStatement.forPayload(CompositeDisposableTest::removeDisposeRace, "removeDisposeRace", this);
            this.payloads.deleteDisposeRace = _ClassStatement.forPayload(CompositeDisposableTest::deleteDisposeRace, "deleteDisposeRace", this);
            this.payloads.clearDisposeRace = _ClassStatement.forPayload(CompositeDisposableTest::clearDisposeRace, "clearDisposeRace", this);
            this.payloads.sizeDisposeRace = _ClassStatement.forPayload(CompositeDisposableTest::sizeDisposeRace, "sizeDisposeRace", this);
            this.payloads.disposeThrowsIAE = _ClassStatement.forPayload(CompositeDisposableTest::disposeThrowsIAE, "disposeThrowsIAE", this);
            this.payloads.disposeThrowsError = _ClassStatement.forPayload(CompositeDisposableTest::disposeThrowsError, "disposeThrowsError", this);
            this.payloads.disposeThrowsCheckedException = _ClassStatement.forPayload(CompositeDisposableTest::disposeThrowsCheckedException, "disposeThrowsCheckedException", this);
            this.payloads.disposeThrowsCheckedExceptionSneaky = _ClassStatement.forPayload(CompositeDisposableTest::disposeThrowsCheckedExceptionSneaky, "disposeThrowsCheckedExceptionSneaky", this);
        }
    }
}
