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

import static org.junit.Assert.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.processors.PublishProcessor;

public class CompletableAwaitTest extends RxJavaTest {

    @Test
    public void awaitInterrupted() {
        Thread.currentThread().interrupt();
        try {
            PublishProcessor.create().ignoreElements().blockingAwait();
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException ex) {
            if (!(ex.getCause() instanceof InterruptedException)) {
                fail("Wrong cause: " + ex.getCause());
            }
        }
    }

    @Test
    public void awaitTimeoutInterrupted() {
        Thread.currentThread().interrupt();
        try {
            PublishProcessor.create().ignoreElements().blockingAwait(1, TimeUnit.SECONDS);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException ex) {
            if (!(ex.getCause() instanceof InterruptedException)) {
                fail("Wrong cause: " + ex.getCause());
            }
        }
    }

    @Test
    public void awaitTimeout() {
        assertFalse(PublishProcessor.create().ignoreElements().blockingAwait(100, TimeUnit.MILLISECONDS));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public CompletableAwaitTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_awaitInterrupted() throws java.lang.Throwable {
            this.payloads.awaitInterrupted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_awaitTimeoutInterrupted() throws java.lang.Throwable {
            this.payloads.awaitTimeoutInterrupted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_awaitTimeout() throws java.lang.Throwable {
            this.payloads.awaitTimeout.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAwaitTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAwaitTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAwaitTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAwaitTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableAwaitTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAwaitTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableAwaitTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableAwaitTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement awaitInterrupted;

            public org.junit.runners.model.Statement awaitTimeoutInterrupted;

            public org.junit.runners.model.Statement awaitTimeout;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.awaitInterrupted = _ClassStatement.forPayload(CompletableAwaitTest::awaitInterrupted, "awaitInterrupted", this);
            this.payloads.awaitTimeoutInterrupted = _ClassStatement.forPayload(CompletableAwaitTest::awaitTimeoutInterrupted, "awaitTimeoutInterrupted", this);
            this.payloads.awaitTimeout = _ClassStatement.forPayload(CompletableAwaitTest::awaitTimeout, "awaitTimeout", this);
        }
    }
}
