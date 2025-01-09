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
package io.reactivex.rxjava3.internal.operators.single;

import static org.junit.Assert.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

public class SingleSwitchOnNextTest extends RxJavaTest {

    @Test
    public void normal() {
        Single.switchOnNext(Flowable.range(1, 5).map(v -> {
            if (v % 2 == 0) {
                return Single.just(v);
            }
            return Single.just(10 + v);
        })).test().assertResult(11, 2, 13, 4, 15);
    }

    @Test
    public void normalDelayError() {
        Single.switchOnNextDelayError(Flowable.range(1, 5).map(v -> {
            if (v % 2 == 0) {
                return Single.just(v);
            }
            return Single.just(10 + v);
        })).test().assertResult(11, 2, 13, 4, 15);
    }

    @Test
    public void noDelaySwitch() {
        PublishProcessor<Single<Integer>> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = Single.switchOnNext(pp).test();
        assertTrue(pp.hasSubscribers());
        ts.assertEmpty();
        SingleSubject<Integer> ss1 = SingleSubject.create();
        SingleSubject<Integer> ss2 = SingleSubject.create();
        pp.onNext(ss1);
        assertTrue(ss1.hasObservers());
        pp.onNext(ss2);
        assertFalse(ss1.hasObservers());
        assertTrue(ss2.hasObservers());
        pp.onComplete();
        assertTrue(ss2.hasObservers());
        ss2.onSuccess(1);
        ts.assertResult(1);
    }

    @Test
    public void delaySwitch() {
        PublishProcessor<Single<Integer>> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = Single.switchOnNextDelayError(pp).test();
        assertTrue(pp.hasSubscribers());
        ts.assertEmpty();
        SingleSubject<Integer> ss1 = SingleSubject.create();
        SingleSubject<Integer> ss2 = SingleSubject.create();
        pp.onNext(ss1);
        assertTrue(ss1.hasObservers());
        pp.onNext(ss2);
        assertFalse(ss1.hasObservers());
        assertTrue(ss2.hasObservers());
        assertTrue(ss2.hasObservers());
        ss2.onError(new TestException());
        assertTrue(pp.hasSubscribers());
        ts.assertEmpty();
        pp.onComplete();
        ts.assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public SingleSwitchOnNextTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayError() throws java.lang.Throwable {
            this.payloads.normalDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noDelaySwitch() throws java.lang.Throwable {
            this.payloads.noDelaySwitch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delaySwitch() throws java.lang.Throwable {
            this.payloads.delaySwitch.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleSwitchOnNextTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleSwitchOnNextTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleSwitchOnNextTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleSwitchOnNextTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleSwitchOnNextTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleSwitchOnNextTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleSwitchOnNextTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleSwitchOnNextTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement normalDelayError;

            public org.junit.runners.model.Statement noDelaySwitch;

            public org.junit.runners.model.Statement delaySwitch;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(SingleSwitchOnNextTest::normal, "normal", this);
            this.payloads.normalDelayError = _ClassStatement.forPayload(SingleSwitchOnNextTest::normalDelayError, "normalDelayError", this);
            this.payloads.noDelaySwitch = _ClassStatement.forPayload(SingleSwitchOnNextTest::noDelaySwitch, "noDelaySwitch", this);
            this.payloads.delaySwitch = _ClassStatement.forPayload(SingleSwitchOnNextTest::delaySwitch, "delaySwitch", this);
        }
    }
}
