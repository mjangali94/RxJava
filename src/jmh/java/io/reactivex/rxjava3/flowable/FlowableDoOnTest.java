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
package io.reactivex.rxjava3.flowable;

import static org.junit.Assert.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;

public class FlowableDoOnTest extends RxJavaTest {

    @Test
    public void doOnEach() {
        final AtomicReference<String> r = new AtomicReference<>();
        String output = Flowable.just("one").doOnNext(new Consumer<String>() {

            @Override
            public void accept(String v) {
                r.set(v);
            }
        }).blockingSingle();
        assertEquals("one", output);
        assertEquals("one", r.get());
    }

    @Test
    public void doOnError() {
        final AtomicReference<Throwable> r = new AtomicReference<>();
        Throwable t = null;
        try {
            Flowable.<String>error(new RuntimeException("an error")).doOnError(new Consumer<Throwable>() {

                @Override
                public void accept(Throwable v) {
                    r.set(v);
                }
            }).blockingSingle();
            fail("expected exception, not a return value");
        } catch (Throwable e) {
            t = e;
        }
        assertNotNull(t);
        assertEquals(t, r.get());
    }

    @Test
    public void doOnCompleted() {
        final AtomicBoolean r = new AtomicBoolean();
        String output = Flowable.just("one").doOnComplete(new Action() {

            @Override
            public void run() {
                r.set(true);
            }
        }).blockingSingle();
        assertEquals("one", output);
        assertTrue(r.get());
    }

    @Test
    public void doOnTerminateError() {
        final AtomicBoolean r = new AtomicBoolean();
        Flowable.<String>error(new TestException()).doOnTerminate(new Action() {

            @Override
            public void run() {
                r.set(true);
            }
        }).test().assertFailure(TestException.class);
        assertTrue(r.get());
    }

    @Test
    public void doOnTerminateComplete() {
        final AtomicBoolean r = new AtomicBoolean();
        String output = Flowable.just("one").doOnTerminate(new Action() {

            @Override
            public void run() {
                r.set(true);
            }
        }).blockingSingle();
        assertEquals("one", output);
        assertTrue(r.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableDoOnTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEach() throws java.lang.Throwable {
            this.payloads.doOnEach.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnError() throws java.lang.Throwable {
            this.payloads.doOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompleted() throws java.lang.Throwable {
            this.payloads.doOnCompleted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateError() throws java.lang.Throwable {
            this.payloads.doOnTerminateError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateComplete() throws java.lang.Throwable {
            this.payloads.doOnTerminateComplete.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableDoOnTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableDoOnTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableDoOnTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement doOnEach;

            public org.junit.runners.model.Statement doOnError;

            public org.junit.runners.model.Statement doOnCompleted;

            public org.junit.runners.model.Statement doOnTerminateError;

            public org.junit.runners.model.Statement doOnTerminateComplete;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.doOnEach = _ClassStatement.forPayload(FlowableDoOnTest::doOnEach, "doOnEach", this);
            this.payloads.doOnError = _ClassStatement.forPayload(FlowableDoOnTest::doOnError, "doOnError", this);
            this.payloads.doOnCompleted = _ClassStatement.forPayload(FlowableDoOnTest::doOnCompleted, "doOnCompleted", this);
            this.payloads.doOnTerminateError = _ClassStatement.forPayload(FlowableDoOnTest::doOnTerminateError, "doOnTerminateError", this);
            this.payloads.doOnTerminateComplete = _ClassStatement.forPayload(FlowableDoOnTest::doOnTerminateComplete, "doOnTerminateComplete", this);
        }
    }
}
