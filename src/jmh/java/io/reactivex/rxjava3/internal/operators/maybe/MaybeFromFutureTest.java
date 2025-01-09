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
package io.reactivex.rxjava3.internal.operators.maybe;

import static org.junit.Assert.assertTrue;
import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MaybeFromFutureTest extends RxJavaTest {

    @Test
    public void cancelImmediately() {
        FutureTask<Integer> ft = new FutureTask<>(Functions.justCallable(1));
        Maybe.fromFuture(ft).test(true).assertEmpty();
    }

    @Test
    public void timeout() {
        FutureTask<Integer> ft = new FutureTask<>(Functions.justCallable(1));
        Maybe.fromFuture(ft, 1, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TimeoutException.class);
    }

    @Test
    public void timedWait() {
        FutureTask<Integer> ft = new FutureTask<>(Functions.justCallable(1));
        ft.run();
        Maybe.fromFuture(ft, 1, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void interrupt() {
        FutureTask<Integer> ft = new FutureTask<>(Functions.justCallable(1));
        Thread.currentThread().interrupt();
        Maybe.fromFuture(ft, 1, TimeUnit.MILLISECONDS).test().assertFailure(InterruptedException.class);
    }

    @Test
    public void cancelWhileRunning() {
        final TestObserver<Object> to = new TestObserver<>();
        FutureTask<Object> ft = new FutureTask<>(new Runnable() {

            @Override
            public void run() {
                to.dispose();
            }
        }, null);
        Schedulers.single().scheduleDirect(ft, 100, TimeUnit.MILLISECONDS);
        Maybe.fromFuture(ft).subscribeWith(to).assertEmpty();
        assertTrue(to.isDisposed());
    }

    @Test
    public void cancelAndCrashWhileRunning() {
        final TestObserver<Object> to = new TestObserver<>();
        FutureTask<Object> ft = new FutureTask<>(new Runnable() {

            @Override
            public void run() {
                to.dispose();
                throw new TestException();
            }
        }, null);
        Schedulers.single().scheduleDirect(ft, 100, TimeUnit.MILLISECONDS);
        Maybe.fromFuture(ft).subscribeWith(to).assertEmpty();
        assertTrue(to.isDisposed());
    }

    @Test
    public void futureNull() {
        FutureTask<Object> ft = new FutureTask<>(new Runnable() {

            @Override
            public void run() {
            }
        }, null);
        Schedulers.single().scheduleDirect(ft, 100, TimeUnit.MILLISECONDS);
        Maybe.fromFuture(ft).test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeFromFutureTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelImmediately() throws java.lang.Throwable {
            this.payloads.cancelImmediately.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeout() throws java.lang.Throwable {
            this.payloads.timeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedWait() throws java.lang.Throwable {
            this.payloads.timedWait.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interrupt() throws java.lang.Throwable {
            this.payloads.interrupt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWhileRunning() throws java.lang.Throwable {
            this.payloads.cancelWhileRunning.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAndCrashWhileRunning() throws java.lang.Throwable {
            this.payloads.cancelAndCrashWhileRunning.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_futureNull() throws java.lang.Throwable {
            this.payloads.futureNull.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFromFutureTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFromFutureTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFromFutureTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFromFutureTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeFromFutureTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFromFutureTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeFromFutureTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeFromFutureTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement cancelImmediately;

            public org.junit.runners.model.Statement timeout;

            public org.junit.runners.model.Statement timedWait;

            public org.junit.runners.model.Statement interrupt;

            public org.junit.runners.model.Statement cancelWhileRunning;

            public org.junit.runners.model.Statement cancelAndCrashWhileRunning;

            public org.junit.runners.model.Statement futureNull;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.cancelImmediately = _ClassStatement.forPayload(MaybeFromFutureTest::cancelImmediately, "cancelImmediately", this);
            this.payloads.timeout = _ClassStatement.forPayload(MaybeFromFutureTest::timeout, "timeout", this);
            this.payloads.timedWait = _ClassStatement.forPayload(MaybeFromFutureTest::timedWait, "timedWait", this);
            this.payloads.interrupt = _ClassStatement.forPayload(MaybeFromFutureTest::interrupt, "interrupt", this);
            this.payloads.cancelWhileRunning = _ClassStatement.forPayload(MaybeFromFutureTest::cancelWhileRunning, "cancelWhileRunning", this);
            this.payloads.cancelAndCrashWhileRunning = _ClassStatement.forPayload(MaybeFromFutureTest::cancelAndCrashWhileRunning, "cancelAndCrashWhileRunning", this);
            this.payloads.futureNull = _ClassStatement.forPayload(MaybeFromFutureTest::futureNull, "futureNull", this);
        }
    }
}
