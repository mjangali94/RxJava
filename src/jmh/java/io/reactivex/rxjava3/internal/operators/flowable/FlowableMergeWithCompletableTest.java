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
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableMergeWithCompletableTest extends RxJavaTest {

    @Test
    public void normal() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).mergeWith(Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                ts.onNext(100);
            }
        })).subscribe(ts);
        ts.assertResult(1, 2, 3, 4, 5, 100);
    }

    @Test
    public void take() {
        Flowable.range(1, 5).mergeWith(Completable.complete()).take(3).test().assertResult(1, 2, 3);
    }

    @Test
    public void cancel() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final CompletableSubject cs = CompletableSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).test();
        assertTrue(pp.hasSubscribers());
        assertTrue(cs.hasObservers());
        ts.cancel();
        assertFalse(pp.hasSubscribers());
        assertFalse(cs.hasObservers());
    }

    @Test
    public void normalBackpressured() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        Flowable.range(1, 5).mergeWith(Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                ts.onNext(100);
            }
        })).subscribe(ts);
        ts.assertValue(100).requestMore(2).assertValues(100, 1, 2).requestMore(2).assertValues(100, 1, 2, 3, 4).requestMore(1).assertResult(100, 1, 2, 3, 4, 5);
    }

    @Test
    public void mainError() {
        Flowable.error(new TestException()).mergeWith(Completable.complete()).test().assertFailure(TestException.class);
    }

    @Test
    public void otherError() {
        Flowable.never().mergeWith(Completable.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void completeRace() {
        for (int i = 0; i < 1000; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final CompletableSubject cs = CompletableSubject.create();
            TestSubscriber<Integer> ts = pp.mergeWith(cs).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                    pp.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    cs.onComplete();
                }
            };
            TestHelper.race(r1, r2);
            ts.assertResult(1);
        }
    }

    @Test
    public void cancelOtherOnMainError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        CompletableSubject cs = CompletableSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).test();
        assertTrue(pp.hasSubscribers());
        assertTrue(cs.hasObservers());
        pp.onError(new TestException());
        ts.assertFailure(TestException.class);
        assertFalse("main has observers!", pp.hasSubscribers());
        assertFalse("other has observers", cs.hasObservers());
    }

    @Test
    public void cancelMainOnOtherError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        CompletableSubject cs = CompletableSubject.create();
        TestSubscriber<Integer> ts = pp.mergeWith(cs).test();
        assertTrue(pp.hasSubscribers());
        assertTrue(cs.hasObservers());
        cs.onError(new TestException());
        ts.assertFailure(TestException.class);
        assertFalse("main has observers!", pp.hasSubscribers());
        assertFalse("other has observers", cs.hasObservers());
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.mergeWith(Completable.complete().hide());
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableMergeWithCompletableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalBackpressured() throws java.lang.Throwable {
            this.payloads.normalBackpressured.evaluate();
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
        public void benchmark_completeRace() throws java.lang.Throwable {
            this.payloads.completeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOtherOnMainError() throws java.lang.Throwable {
            this.payloads.cancelOtherOnMainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelMainOnOtherError() throws java.lang.Throwable {
            this.payloads.cancelMainOnOtherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancel.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeWithCompletableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeWithCompletableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeWithCompletableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeWithCompletableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableMergeWithCompletableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeWithCompletableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableMergeWithCompletableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableMergeWithCompletableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement normalBackpressured;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement otherError;

            public org.junit.runners.model.Statement completeRace;

            public org.junit.runners.model.Statement cancelOtherOnMainError;

            public org.junit.runners.model.Statement cancelMainOnOtherError;

            public org.junit.runners.model.Statement undeliverableUponCancel;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(FlowableMergeWithCompletableTest::normal, "normal", this);
            this.payloads.take = _ClassStatement.forPayload(FlowableMergeWithCompletableTest::take, "take", this);
            this.payloads.cancel = _ClassStatement.forPayload(FlowableMergeWithCompletableTest::cancel, "cancel", this);
            this.payloads.normalBackpressured = _ClassStatement.forPayload(FlowableMergeWithCompletableTest::normalBackpressured, "normalBackpressured", this);
            this.payloads.mainError = _ClassStatement.forPayload(FlowableMergeWithCompletableTest::mainError, "mainError", this);
            this.payloads.otherError = _ClassStatement.forPayload(FlowableMergeWithCompletableTest::otherError, "otherError", this);
            this.payloads.completeRace = _ClassStatement.forPayload(FlowableMergeWithCompletableTest::completeRace, "completeRace", this);
            this.payloads.cancelOtherOnMainError = _ClassStatement.forPayload(FlowableMergeWithCompletableTest::cancelOtherOnMainError, "cancelOtherOnMainError", this);
            this.payloads.cancelMainOnOtherError = _ClassStatement.forPayload(FlowableMergeWithCompletableTest::cancelMainOnOtherError, "cancelMainOnOtherError", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(FlowableMergeWithCompletableTest::undeliverableUponCancel, "undeliverableUponCancel", this);
        }
    }
}
