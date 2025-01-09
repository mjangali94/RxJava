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
package io.reactivex.rxjava3.internal.operators.flowable;

import static org.mockito.Mockito.*;
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

/**
 * Systematically tests that when zipping an infinite and a finite Observable,
 * the resulting Observable is finite.
 */
public class FlowableZipCompletionTest extends RxJavaTest {

    BiFunction<String, String, String> concat2Strings;

    PublishProcessor<String> s1;

    PublishProcessor<String> s2;

    Flowable<String> zipped;

    Subscriber<String> subscriber;

    InOrder inOrder;

    @Before
    public void setUp() {
        concat2Strings = new BiFunction<String, String, String>() {

            @Override
            public String apply(String t1, String t2) {
                return t1 + "-" + t2;
            }
        };
        s1 = PublishProcessor.create();
        s2 = PublishProcessor.create();
        zipped = Flowable.zip(s1, s2, concat2Strings);
        subscriber = TestHelper.mockSubscriber();
        inOrder = inOrder(subscriber);
        zipped.subscribe(subscriber);
    }

    @Test
    public void firstCompletesThenSecondInfinite() {
        s1.onNext("a");
        s1.onNext("b");
        s1.onComplete();
        s2.onNext("1");
        inOrder.verify(subscriber, times(1)).onNext("a-1");
        s2.onNext("2");
        inOrder.verify(subscriber, times(1)).onNext("b-2");
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void secondInfiniteThenFirstCompletes() {
        s2.onNext("1");
        s2.onNext("2");
        s1.onNext("a");
        inOrder.verify(subscriber, times(1)).onNext("a-1");
        s1.onNext("b");
        inOrder.verify(subscriber, times(1)).onNext("b-2");
        s1.onComplete();
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void secondCompletesThenFirstInfinite() {
        s2.onNext("1");
        s2.onNext("2");
        s2.onComplete();
        s1.onNext("a");
        inOrder.verify(subscriber, times(1)).onNext("a-1");
        s1.onNext("b");
        inOrder.verify(subscriber, times(1)).onNext("b-2");
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void firstInfiniteThenSecondCompletes() {
        s1.onNext("a");
        s1.onNext("b");
        s2.onNext("1");
        inOrder.verify(subscriber, times(1)).onNext("a-1");
        s2.onNext("2");
        inOrder.verify(subscriber, times(1)).onNext("b-2");
        s2.onComplete();
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableZipCompletionTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstCompletesThenSecondInfinite() throws java.lang.Throwable {
            this.payloads.firstCompletesThenSecondInfinite.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_secondInfiniteThenFirstCompletes() throws java.lang.Throwable {
            this.payloads.secondInfiniteThenFirstCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_secondCompletesThenFirstInfinite() throws java.lang.Throwable {
            this.payloads.secondCompletesThenFirstInfinite.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstInfiniteThenSecondCompletes() throws java.lang.Throwable {
            this.payloads.firstInfiniteThenSecondCompletes.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipCompletionTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipCompletionTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.setUp();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipCompletionTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipCompletionTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableZipCompletionTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipCompletionTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableZipCompletionTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableZipCompletionTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement firstCompletesThenSecondInfinite;

            public org.junit.runners.model.Statement secondInfiniteThenFirstCompletes;

            public org.junit.runners.model.Statement secondCompletesThenFirstInfinite;

            public org.junit.runners.model.Statement firstInfiniteThenSecondCompletes;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.firstCompletesThenSecondInfinite = _ClassStatement.forPayload(FlowableZipCompletionTest::firstCompletesThenSecondInfinite, "firstCompletesThenSecondInfinite", this);
            this.payloads.secondInfiniteThenFirstCompletes = _ClassStatement.forPayload(FlowableZipCompletionTest::secondInfiniteThenFirstCompletes, "secondInfiniteThenFirstCompletes", this);
            this.payloads.secondCompletesThenFirstInfinite = _ClassStatement.forPayload(FlowableZipCompletionTest::secondCompletesThenFirstInfinite, "secondCompletesThenFirstInfinite", this);
            this.payloads.firstInfiniteThenSecondCompletes = _ClassStatement.forPayload(FlowableZipCompletionTest::firstInfiniteThenSecondCompletes, "firstInfiniteThenSecondCompletes", this);
        }
    }
}
