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

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableConcatWithCompletableTest extends RxJavaTest {

    @Test
    public void normal() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).concatWith(Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                ts.onNext(100);
            }
        })).subscribe(ts);
        ts.assertResult(1, 2, 3, 4, 5, 100);
    }

    @Test
    public void mainError() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.<Integer>error(new TestException()).concatWith(Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                ts.onNext(100);
            }
        })).subscribe(ts);
        ts.assertFailure(TestException.class);
    }

    @Test
    public void otherError() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).concatWith(Completable.error(new TestException())).subscribe(ts);
        ts.assertFailure(TestException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void takeMain() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).concatWith(Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                ts.onNext(100);
            }
        })).take(3).subscribe(ts);
        ts.assertResult(1, 2, 3);
    }

    @Test
    public void cancelOther() {
        CompletableSubject other = CompletableSubject.create();
        TestSubscriber<Object> ts = Flowable.empty().concatWith(other).test();
        assertTrue(other.hasObservers());
        ts.cancel();
        assertFalse(other.hasObservers());
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    BooleanSubscription bs1 = new BooleanSubscription();
                    s.onSubscribe(bs1);
                    BooleanSubscription bs2 = new BooleanSubscription();
                    s.onSubscribe(bs2);
                    assertFalse(bs1.isCancelled());
                    assertTrue(bs2.isCancelled());
                    s.onComplete();
                }
            }.concatWith(Completable.complete()).test().assertResult();
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableConcatWithCompletableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.payloads.mainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherError() throws java.lang.Throwable {
            this.payloads.otherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeMain() throws java.lang.Throwable {
            this.payloads.takeMain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOther() throws java.lang.Throwable {
            this.payloads.cancelOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatWithCompletableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatWithCompletableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatWithCompletableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatWithCompletableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableConcatWithCompletableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatWithCompletableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableConcatWithCompletableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableConcatWithCompletableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement otherError;

            public org.junit.runners.model.Statement takeMain;

            public org.junit.runners.model.Statement cancelOther;

            public org.junit.runners.model.Statement badSource;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(FlowableConcatWithCompletableTest::normal, "normal", this);
            this.payloads.mainError = _ClassStatement.forPayload(FlowableConcatWithCompletableTest::mainError, "mainError", this);
            this.payloads.otherError = _ClassStatement.forPayload(FlowableConcatWithCompletableTest::otherError, "otherError", this);
            this.payloads.takeMain = _ClassStatement.forPayload(FlowableConcatWithCompletableTest::takeMain, "takeMain", this);
            this.payloads.cancelOther = _ClassStatement.forPayload(FlowableConcatWithCompletableTest::cancelOther, "cancelOther", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableConcatWithCompletableTest::badSource, "badSource", this);
        }
    }
}
