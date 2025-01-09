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
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableFromActionTest extends RxJavaTest {

    @Test
    public void fromAction() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                atomicInteger.incrementAndGet();
            }
        }).test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromActionTwice() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Action run = new Action() {

            @Override
            public void run() throws Exception {
                atomicInteger.incrementAndGet();
            }
        };
        Completable.fromAction(run).test().assertResult();
        assertEquals(1, atomicInteger.get());
        Completable.fromAction(run).test().assertResult();
        assertEquals(2, atomicInteger.get());
    }

    @Test
    public void fromActionInvokesLazy() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Completable completable = Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                atomicInteger.incrementAndGet();
            }
        });
        assertEquals(0, atomicInteger.get());
        completable.test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromActionThrows() {
        Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                throw new UnsupportedOperationException();
            }
        }).test().assertFailure(UnsupportedOperationException.class);
    }

    @Test
    public void fromActionDisposed() {
        final AtomicInteger calls = new AtomicInteger();
        Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                calls.incrementAndGet();
            }
        }).test(true).assertEmpty();
        assertEquals(0, calls.get());
    }

    @Test
    public void fromActionErrorsDisposed() {
        final AtomicInteger calls = new AtomicInteger();
        Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                calls.incrementAndGet();
                throw new TestException();
            }
        }).test(true).assertEmpty();
        assertEquals(0, calls.get());
    }

    @Test
    public void disposedUpfront() throws Throwable {
        Action run = mock(Action.class);
        Completable.fromAction(run).test(true).assertEmpty();
        verify(run, never()).run();
    }

    @Test
    public void disposeWhileRunningComplete() {
        TestObserver<Void> to = new TestObserver<>();
        Completable.fromAction(() -> {
            to.dispose();
        }).subscribeWith(to).assertEmpty();
    }

    @Test
    public void disposeWhileRunningError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            TestObserver<Void> to = new TestObserver<>();
            Completable.fromAction(() -> {
                to.dispose();
                throw new TestException();
            }).subscribeWith(to).assertEmpty();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public CompletableFromActionTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromAction() throws java.lang.Throwable {
            this.payloads.fromAction.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionTwice() throws java.lang.Throwable {
            this.payloads.fromActionTwice.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionInvokesLazy() throws java.lang.Throwable {
            this.payloads.fromActionInvokesLazy.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionThrows() throws java.lang.Throwable {
            this.payloads.fromActionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionDisposed() throws java.lang.Throwable {
            this.payloads.fromActionDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionErrorsDisposed() throws java.lang.Throwable {
            this.payloads.fromActionErrorsDisposed.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromActionTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromActionTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromActionTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromActionTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableFromActionTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromActionTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableFromActionTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableFromActionTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement fromAction;

            public org.junit.runners.model.Statement fromActionTwice;

            public org.junit.runners.model.Statement fromActionInvokesLazy;

            public org.junit.runners.model.Statement fromActionThrows;

            public org.junit.runners.model.Statement fromActionDisposed;

            public org.junit.runners.model.Statement fromActionErrorsDisposed;

            public org.junit.runners.model.Statement disposedUpfront;

            public org.junit.runners.model.Statement disposeWhileRunningComplete;

            public org.junit.runners.model.Statement disposeWhileRunningError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.fromAction = _ClassStatement.forPayload(CompletableFromActionTest::fromAction, "fromAction", this);
            this.payloads.fromActionTwice = _ClassStatement.forPayload(CompletableFromActionTest::fromActionTwice, "fromActionTwice", this);
            this.payloads.fromActionInvokesLazy = _ClassStatement.forPayload(CompletableFromActionTest::fromActionInvokesLazy, "fromActionInvokesLazy", this);
            this.payloads.fromActionThrows = _ClassStatement.forPayload(CompletableFromActionTest::fromActionThrows, "fromActionThrows", this);
            this.payloads.fromActionDisposed = _ClassStatement.forPayload(CompletableFromActionTest::fromActionDisposed, "fromActionDisposed", this);
            this.payloads.fromActionErrorsDisposed = _ClassStatement.forPayload(CompletableFromActionTest::fromActionErrorsDisposed, "fromActionErrorsDisposed", this);
            this.payloads.disposedUpfront = _ClassStatement.forPayload(CompletableFromActionTest::disposedUpfront, "disposedUpfront", this);
            this.payloads.disposeWhileRunningComplete = _ClassStatement.forPayload(CompletableFromActionTest::disposeWhileRunningComplete, "disposeWhileRunningComplete", this);
            this.payloads.disposeWhileRunningError = _ClassStatement.forPayload(CompletableFromActionTest::disposeWhileRunningError, "disposeWhileRunningError", this);
        }
    }
}
