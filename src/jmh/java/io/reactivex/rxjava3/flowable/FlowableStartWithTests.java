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

import static org.junit.Assert.assertEquals;
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;

public class FlowableStartWithTests extends RxJavaTest {

    @Test
    public void startWith1() {
        List<String> values = Flowable.just("one", "two").startWithArray("zero").toList().blockingGet();
        assertEquals("zero", values.get(0));
        assertEquals("two", values.get(2));
    }

    @Test
    public void startWithIterable() {
        List<String> li = new ArrayList<>();
        li.add("alpha");
        li.add("beta");
        List<String> values = Flowable.just("one", "two").startWithIterable(li).toList().blockingGet();
        assertEquals("alpha", values.get(0));
        assertEquals("beta", values.get(1));
        assertEquals("one", values.get(2));
        assertEquals("two", values.get(3));
    }

    @Test
    public void startWithObservable() {
        List<String> li = new ArrayList<>();
        li.add("alpha");
        li.add("beta");
        List<String> values = Flowable.just("one", "two").startWith(Flowable.fromIterable(li)).toList().blockingGet();
        assertEquals("alpha", values.get(0));
        assertEquals("beta", values.get(1));
        assertEquals("one", values.get(2));
        assertEquals("two", values.get(3));
    }

    @Test
    public void startWithEmpty() {
        Flowable.just(1).startWithArray().test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableStartWithTests instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWith1() throws java.lang.Throwable {
            this.payloads.startWith1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithIterable() throws java.lang.Throwable {
            this.payloads.startWithIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithObservable() throws java.lang.Throwable {
            this.payloads.startWithObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithEmpty() throws java.lang.Throwable {
            this.payloads.startWithEmpty.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableStartWithTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableStartWithTests> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableStartWithTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableStartWithTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableStartWithTests();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableStartWithTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableStartWithTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableStartWithTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement startWith1;

            public org.junit.runners.model.Statement startWithIterable;

            public org.junit.runners.model.Statement startWithObservable;

            public org.junit.runners.model.Statement startWithEmpty;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.startWith1 = _ClassStatement.forPayload(FlowableStartWithTests::startWith1, "startWith1", this);
            this.payloads.startWithIterable = _ClassStatement.forPayload(FlowableStartWithTests::startWithIterable, "startWithIterable", this);
            this.payloads.startWithObservable = _ClassStatement.forPayload(FlowableStartWithTests::startWithObservable, "startWithObservable", this);
            this.payloads.startWithEmpty = _ClassStatement.forPayload(FlowableStartWithTests::startWithEmpty, "startWithEmpty", this);
        }
    }
}
