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
package io.reactivex.rxjava3.internal.jdk8;

import java.util.concurrent.CompletableFuture;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

public class FlowableFromCompletionStageTest extends RxJavaTest {

    @Test
    public void syncSuccess() {
        Flowable.fromCompletionStage(CompletableFuture.completedFuture(1)).test().assertResult(1);
    }

    @Test
    public void syncFailure() {
        CompletableFuture<Integer> cf = new CompletableFuture<>();
        cf.completeExceptionally(new TestException());
        Flowable.fromCompletionStage(cf).test().assertFailure(TestException.class);
    }

    @Test
    public void syncNull() {
        Flowable.fromCompletionStage(CompletableFuture.<Integer>completedFuture(null)).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void cancel() {
        CompletableFuture<Integer> cf = new CompletableFuture<>();
        TestSubscriber<Integer> ts = Flowable.fromCompletionStage(cf).test();
        ts.assertEmpty();
        ts.cancel();
        cf.complete(1);
        ts.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableFromCompletionStageTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncSuccess() throws java.lang.Throwable {
            this.payloads.syncSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFailure() throws java.lang.Throwable {
            this.payloads.syncFailure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncNull() throws java.lang.Throwable {
            this.payloads.syncNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromCompletionStageTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromCompletionStageTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromCompletionStageTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromCompletionStageTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableFromCompletionStageTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromCompletionStageTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableFromCompletionStageTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableFromCompletionStageTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement syncSuccess;

            public org.junit.runners.model.Statement syncFailure;

            public org.junit.runners.model.Statement syncNull;

            public org.junit.runners.model.Statement cancel;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.syncSuccess = _ClassStatement.forPayload(FlowableFromCompletionStageTest::syncSuccess, "syncSuccess", this);
            this.payloads.syncFailure = _ClassStatement.forPayload(FlowableFromCompletionStageTest::syncFailure, "syncFailure", this);
            this.payloads.syncNull = _ClassStatement.forPayload(FlowableFromCompletionStageTest::syncNull, "syncNull", this);
            this.payloads.cancel = _ClassStatement.forPayload(FlowableFromCompletionStageTest::cancel, "cancel", this);
        }
    }
}
