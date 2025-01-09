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
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableDisposeOnTest extends RxJavaTest {

    @Test
    public void cancelDelayed() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> ps = PublishSubject.create();
        ps.ignoreElements().unsubscribeOn(scheduler).test().dispose();
        assertTrue(ps.hasObservers());
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().ignoreElements().unsubscribeOn(new TestScheduler()));
    }

    @Test
    public void completeAfterCancel() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Void> to = ps.ignoreElements().unsubscribeOn(scheduler).test();
        to.dispose();
        ps.onComplete();
        to.assertEmpty();
    }

    @Test
    public void errorAfterCancel() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestScheduler scheduler = new TestScheduler();
            PublishSubject<Integer> ps = PublishSubject.create();
            TestObserver<Void> to = ps.ignoreElements().unsubscribeOn(scheduler).test();
            to.dispose();
            ps.onError(new TestException());
            to.assertEmpty();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void normal() {
        TestScheduler scheduler = new TestScheduler();
        final int[] call = { 0 };
        Completable.complete().doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                call[0]++;
            }
        }).unsubscribeOn(scheduler).test().assertResult();
        scheduler.triggerActions();
        assertEquals(0, call[0]);
    }

    @Test
    public void error() {
        TestScheduler scheduler = new TestScheduler();
        final int[] call = { 0 };
        Completable.error(new TestException()).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                call[0]++;
            }
        }).unsubscribeOn(scheduler).test().assertFailure(TestException.class);
        scheduler.triggerActions();
        assertEquals(0, call[0]);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeCompletable(c -> c.unsubscribeOn(Schedulers.computation()));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public CompletableDisposeOnTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelDelayed() throws java.lang.Throwable {
            this.payloads.cancelDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeAfterCancel() throws java.lang.Throwable {
            this.payloads.completeAfterCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorAfterCancel() throws java.lang.Throwable {
            this.payloads.errorAfterCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDisposeOnTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDisposeOnTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDisposeOnTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDisposeOnTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableDisposeOnTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDisposeOnTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableDisposeOnTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableDisposeOnTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement cancelDelayed;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement completeAfterCancel;

            public org.junit.runners.model.Statement errorAfterCancel;

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.cancelDelayed = _ClassStatement.forPayload(CompletableDisposeOnTest::cancelDelayed, "cancelDelayed", this);
            this.payloads.dispose = _ClassStatement.forPayload(CompletableDisposeOnTest::dispose, "dispose", this);
            this.payloads.completeAfterCancel = _ClassStatement.forPayload(CompletableDisposeOnTest::completeAfterCancel, "completeAfterCancel", this);
            this.payloads.errorAfterCancel = _ClassStatement.forPayload(CompletableDisposeOnTest::errorAfterCancel, "errorAfterCancel", this);
            this.payloads.normal = _ClassStatement.forPayload(CompletableDisposeOnTest::normal, "normal", this);
            this.payloads.error = _ClassStatement.forPayload(CompletableDisposeOnTest::error, "error", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(CompletableDisposeOnTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
