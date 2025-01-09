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
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subjects.CompletableSubject;

public class CompletableDelaySubscriptionTest extends RxJavaTest {

    @Test
    public void normal() {
        final AtomicInteger counter = new AtomicInteger();
        Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                counter.incrementAndGet();
            }
        }).delaySubscription(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
        assertEquals(1, counter.get());
    }

    @Test
    public void error() {
        final AtomicInteger counter = new AtomicInteger();
        Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                counter.incrementAndGet();
                throw new TestException();
            }
        }).delaySubscription(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
        assertEquals(1, counter.get());
    }

    @Test
    public void disposeBeforeTime() {
        TestScheduler scheduler = new TestScheduler();
        final AtomicInteger counter = new AtomicInteger();
        Completable result = Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                counter.incrementAndGet();
            }
        }).delaySubscription(100, TimeUnit.MILLISECONDS, scheduler);
        TestObserver<Void> to = result.test();
        to.assertEmpty();
        scheduler.advanceTimeBy(90, TimeUnit.MILLISECONDS);
        to.dispose();
        scheduler.advanceTimeBy(15, TimeUnit.MILLISECONDS);
        to.assertEmpty();
        assertEquals(0, counter.get());
    }

    @Test
    public void timestep() {
        TestScheduler scheduler = new TestScheduler();
        final AtomicInteger counter = new AtomicInteger();
        Completable result = Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                counter.incrementAndGet();
            }
        }).delaySubscription(100, TimeUnit.MILLISECONDS, scheduler);
        TestObserver<Void> to = result.test();
        scheduler.advanceTimeBy(90, TimeUnit.MILLISECONDS);
        to.assertEmpty();
        scheduler.advanceTimeBy(15, TimeUnit.MILLISECONDS);
        to.assertResult();
        assertEquals(1, counter.get());
    }

    @Test
    public void timestepError() {
        TestScheduler scheduler = new TestScheduler();
        final AtomicInteger counter = new AtomicInteger();
        Completable result = Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                counter.incrementAndGet();
                throw new TestException();
            }
        }).delaySubscription(100, TimeUnit.MILLISECONDS, scheduler);
        TestObserver<Void> to = result.test();
        scheduler.advanceTimeBy(90, TimeUnit.MILLISECONDS);
        to.assertEmpty();
        scheduler.advanceTimeBy(15, TimeUnit.MILLISECONDS);
        to.assertFailure(TestException.class);
        assertEquals(1, counter.get());
    }

    @Test
    public void disposeMain() {
        CompletableSubject cs = CompletableSubject.create();
        TestScheduler scheduler = new TestScheduler();
        TestObserver<Void> to = cs.delaySubscription(1, TimeUnit.SECONDS, scheduler).test();
        assertFalse(cs.hasObservers());
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        assertTrue(cs.hasObservers());
        to.dispose();
        assertFalse(cs.hasObservers());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public CompletableDelaySubscriptionTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeBeforeTime() throws java.lang.Throwable {
            this.payloads.disposeBeforeTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timestep() throws java.lang.Throwable {
            this.payloads.timestep.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timestepError() throws java.lang.Throwable {
            this.payloads.timestepError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeMain() throws java.lang.Throwable {
            this.payloads.disposeMain.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDelaySubscriptionTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDelaySubscriptionTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDelaySubscriptionTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDelaySubscriptionTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableDelaySubscriptionTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDelaySubscriptionTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableDelaySubscriptionTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableDelaySubscriptionTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement disposeBeforeTime;

            public org.junit.runners.model.Statement timestep;

            public org.junit.runners.model.Statement timestepError;

            public org.junit.runners.model.Statement disposeMain;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(CompletableDelaySubscriptionTest::normal, "normal", this);
            this.payloads.error = _ClassStatement.forPayload(CompletableDelaySubscriptionTest::error, "error", this);
            this.payloads.disposeBeforeTime = _ClassStatement.forPayload(CompletableDelaySubscriptionTest::disposeBeforeTime, "disposeBeforeTime", this);
            this.payloads.timestep = _ClassStatement.forPayload(CompletableDelaySubscriptionTest::timestep, "timestep", this);
            this.payloads.timestepError = _ClassStatement.forPayload(CompletableDelaySubscriptionTest::timestepError, "timestepError", this);
            this.payloads.disposeMain = _ClassStatement.forPayload(CompletableDelaySubscriptionTest::disposeMain, "disposeMain", this);
        }
    }
}
