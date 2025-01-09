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

import static org.junit.Assert.assertNotEquals;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableDelayTest extends RxJavaTest {

    @Test
    public void delayCustomScheduler() {
        Completable.complete().delay(100, TimeUnit.MILLISECONDS, Schedulers.trampoline()).test().assertResult();
    }

    @Test
    public void onErrorCalledOnScheduler() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Thread> thread = new AtomicReference<>();
        Completable.error(new Exception()).delay(0, TimeUnit.MILLISECONDS, Schedulers.newThread()).doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable throwable) throws Exception {
                thread.set(Thread.currentThread());
                latch.countDown();
            }
        }).onErrorComplete().subscribe();
        latch.await();
        assertNotEquals(Thread.currentThread(), thread.get());
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Completable.never().delay(1, TimeUnit.MINUTES));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeCompletable(new Function<Completable, CompletableSource>() {

            @Override
            public CompletableSource apply(Completable c) throws Exception {
                return c.delay(1, TimeUnit.MINUTES);
            }
        });
    }

    @Test
    public void normal() {
        Completable.complete().delay(1, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void errorNotDelayed() {
        TestScheduler scheduler = new TestScheduler();
        TestObserver<Void> to = Completable.error(new TestException()).delay(100, TimeUnit.MILLISECONDS, scheduler, false).test();
        to.assertEmpty();
        scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        to.assertFailure(TestException.class);
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        to.assertFailure(TestException.class);
    }

    @Test
    public void errorDelayed() {
        TestScheduler scheduler = new TestScheduler();
        TestObserver<Void> to = Completable.error(new TestException()).delay(100, TimeUnit.MILLISECONDS, scheduler, true).test();
        to.assertEmpty();
        scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        to.assertEmpty();
        scheduler.advanceTimeBy(99, TimeUnit.MILLISECONDS);
        to.assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public CompletableDelayTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayCustomScheduler() throws java.lang.Throwable {
            this.payloads.delayCustomScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCalledOnScheduler() throws java.lang.Throwable {
            this.payloads.onErrorCalledOnScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNotDelayed() throws java.lang.Throwable {
            this.payloads.errorNotDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDelayed() throws java.lang.Throwable {
            this.payloads.errorDelayed.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDelayTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDelayTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDelayTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDelayTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableDelayTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDelayTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableDelayTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableDelayTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement delayCustomScheduler;

            public org.junit.runners.model.Statement onErrorCalledOnScheduler;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement errorNotDelayed;

            public org.junit.runners.model.Statement errorDelayed;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.delayCustomScheduler = _ClassStatement.forPayload(CompletableDelayTest::delayCustomScheduler, "delayCustomScheduler", this);
            this.payloads.onErrorCalledOnScheduler = _ClassStatement.forPayload(CompletableDelayTest::onErrorCalledOnScheduler, "onErrorCalledOnScheduler", this);
            this.payloads.disposed = _ClassStatement.forPayload(CompletableDelayTest::disposed, "disposed", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(CompletableDelayTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.normal = _ClassStatement.forPayload(CompletableDelayTest::normal, "normal", this);
            this.payloads.errorNotDelayed = _ClassStatement.forPayload(CompletableDelayTest::errorNotDelayed, "errorNotDelayed", this);
            this.payloads.errorDelayed = _ClassStatement.forPayload(CompletableDelayTest::errorDelayed, "errorDelayed", this);
        }
    }
}
