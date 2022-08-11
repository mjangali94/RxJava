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
package io.reactivex.rxjava3.internal.operators.mixed;

import static org.junit.Assert.*;
import org.junit.Test;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableAndThenPublisherTest extends RxJavaTest {

    @Test
    public void cancelMain() {
        CompletableSubject cs = CompletableSubject.create();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = cs.andThen(pp).test();
        assertTrue(cs.hasObservers());
        assertFalse(pp.hasSubscribers());
        ts.cancel();
        assertFalse(cs.hasObservers());
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void cancelOther() {
        CompletableSubject cs = CompletableSubject.create();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = cs.andThen(pp).test();
        assertTrue(cs.hasObservers());
        assertFalse(pp.hasSubscribers());
        cs.onComplete();
        assertFalse(cs.hasObservers());
        assertTrue(pp.hasSubscribers());
        ts.cancel();
        assertFalse(cs.hasObservers());
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeCompletableToFlowable(new Function<Completable, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Completable m) throws Exception {
                return m.andThen(Flowable.never());
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private CompletableAndThenPublisherTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelMain() throws java.lang.Throwable {
            this.payloads.cancelMain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOther() throws java.lang.Throwable {
            this.payloads.cancelOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAndThenPublisherTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAndThenPublisherTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAndThenPublisherTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAndThenPublisherTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableAndThenPublisherTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAndThenPublisherTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableAndThenPublisherTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableAndThenPublisherTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement cancelMain;

            public org.junit.runners.model.Statement cancelOther;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.cancelMain = _ClassStatement.forPayload(CompletableAndThenPublisherTest::cancelMain, "cancelMain", this);
            this.payloads.cancelOther = _ClassStatement.forPayload(CompletableAndThenPublisherTest::cancelOther, "cancelOther", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(CompletableAndThenPublisherTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
