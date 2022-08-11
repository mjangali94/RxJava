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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableFromRunnableTest extends RxJavaTest {

    @Test
    public void fromRunnable() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                atomicInteger.incrementAndGet();
            }
        }).test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromRunnableTwice() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Runnable run = new Runnable() {

            @Override
            public void run() {
                atomicInteger.incrementAndGet();
            }
        };
        Completable.fromRunnable(run).test().assertResult();
        assertEquals(1, atomicInteger.get());
        Completable.fromRunnable(run).test().assertResult();
        assertEquals(2, atomicInteger.get());
    }

    @Test
    public void fromRunnableInvokesLazy() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Completable completable = Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                atomicInteger.incrementAndGet();
            }
        });
        assertEquals(0, atomicInteger.get());
        completable.test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromRunnableThrows() {
        Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                throw new UnsupportedOperationException();
            }
        }).test().assertFailure(UnsupportedOperationException.class);
    }

    @Test
    public void fromRunnableDisposed() {
        final AtomicInteger calls = new AtomicInteger();
        Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                calls.incrementAndGet();
            }
        }).test(true).assertEmpty();
        assertEquals(0, calls.get());
    }

    @Test
    public void fromRunnableErrorsDisposed() {
        final AtomicInteger calls = new AtomicInteger();
        Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                calls.incrementAndGet();
                throw new TestException();
            }
        }).test(true).assertEmpty();
        assertEquals(0, calls.get());
    }

    @Test
    public void disposedUpfront() throws Throwable {
        Runnable run = mock(Runnable.class);
        Completable.fromRunnable(run).test(true).assertEmpty();
        verify(run, never()).run();
    }

    @Test
    public void disposeWhileRunningComplete() {
        TestObserver<Void> to = new TestObserver<>();
        Completable.fromRunnable(() -> {
            to.dispose();
        }).subscribeWith(to).assertEmpty();
    }

    @Test
    public void disposeWhileRunningError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            TestObserver<Void> to = new TestObserver<>();
            Completable.fromRunnable(() -> {
                to.dispose();
                throw new TestException();
            }).subscribeWith(to).assertEmpty();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private CompletableFromRunnableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnable() throws java.lang.Throwable {
            this.payloads.fromRunnable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableTwice() throws java.lang.Throwable {
            this.payloads.fromRunnableTwice.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableInvokesLazy() throws java.lang.Throwable {
            this.payloads.fromRunnableInvokesLazy.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableThrows() throws java.lang.Throwable {
            this.payloads.fromRunnableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableDisposed() throws java.lang.Throwable {
            this.payloads.fromRunnableDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableErrorsDisposed() throws java.lang.Throwable {
            this.payloads.fromRunnableErrorsDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedUpfront() throws java.lang.Throwable {
            this.payloads.disposedUpfront.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeWhileRunningComplete() throws java.lang.Throwable {
            this.payloads.disposeWhileRunningComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeWhileRunningError() throws java.lang.Throwable {
            this.payloads.disposeWhileRunningError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromRunnableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromRunnableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromRunnableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromRunnableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableFromRunnableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromRunnableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableFromRunnableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableFromRunnableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement fromRunnable;

            public org.junit.runners.model.Statement fromRunnableTwice;

            public org.junit.runners.model.Statement fromRunnableInvokesLazy;

            public org.junit.runners.model.Statement fromRunnableThrows;

            public org.junit.runners.model.Statement fromRunnableDisposed;

            public org.junit.runners.model.Statement fromRunnableErrorsDisposed;

            public org.junit.runners.model.Statement disposedUpfront;

            public org.junit.runners.model.Statement disposeWhileRunningComplete;

            public org.junit.runners.model.Statement disposeWhileRunningError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.fromRunnable = _ClassStatement.forPayload(CompletableFromRunnableTest::fromRunnable, "fromRunnable", this);
            this.payloads.fromRunnableTwice = _ClassStatement.forPayload(CompletableFromRunnableTest::fromRunnableTwice, "fromRunnableTwice", this);
            this.payloads.fromRunnableInvokesLazy = _ClassStatement.forPayload(CompletableFromRunnableTest::fromRunnableInvokesLazy, "fromRunnableInvokesLazy", this);
            this.payloads.fromRunnableThrows = _ClassStatement.forPayload(CompletableFromRunnableTest::fromRunnableThrows, "fromRunnableThrows", this);
            this.payloads.fromRunnableDisposed = _ClassStatement.forPayload(CompletableFromRunnableTest::fromRunnableDisposed, "fromRunnableDisposed", this);
            this.payloads.fromRunnableErrorsDisposed = _ClassStatement.forPayload(CompletableFromRunnableTest::fromRunnableErrorsDisposed, "fromRunnableErrorsDisposed", this);
            this.payloads.disposedUpfront = _ClassStatement.forPayload(CompletableFromRunnableTest::disposedUpfront, "disposedUpfront", this);
            this.payloads.disposeWhileRunningComplete = _ClassStatement.forPayload(CompletableFromRunnableTest::disposeWhileRunningComplete, "disposeWhileRunningComplete", this);
            this.payloads.disposeWhileRunningError = _ClassStatement.forPayload(CompletableFromRunnableTest::disposeWhileRunningError, "disposeWhileRunningError", this);
        }
    }
}
