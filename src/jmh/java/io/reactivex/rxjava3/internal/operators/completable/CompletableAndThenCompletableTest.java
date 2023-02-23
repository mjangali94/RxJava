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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableAndThenCompletableTest extends RxJavaTest {

    @Test
    public void andThenCompletableCompleteComplete() {
        Completable.complete().andThen(Completable.complete()).test().assertComplete();
    }

    @Test
    public void andThenCompletableCompleteError() {
        Completable.complete().andThen(Completable.error(new TestException("test"))).to(TestHelper.testConsumer()).assertNotComplete().assertNoValues().assertError(TestException.class).assertErrorMessage("test");
    }

    @Test
    public void andThenCompletableCompleteNever() {
        Completable.complete().andThen(Completable.never()).test().assertNoValues().assertNoErrors().assertNotComplete();
    }

    @Test
    public void andThenCompletableErrorComplete() {
        Completable.error(new TestException("bla")).andThen(Completable.complete()).to(TestHelper.testConsumer()).assertNotComplete().assertNoValues().assertError(TestException.class).assertErrorMessage("bla");
    }

    @Test
    public void andThenCompletableErrorNever() {
        Completable.error(new TestException("bla")).andThen(Completable.never()).to(TestHelper.testConsumer()).assertNotComplete().assertNoValues().assertError(TestException.class).assertErrorMessage("bla");
    }

    @Test
    public void andThenCompletableErrorError() {
        Completable.error(new TestException("error1")).andThen(Completable.error(new TestException("error2"))).to(TestHelper.testConsumer()).assertNotComplete().assertNoValues().assertError(TestException.class).assertErrorMessage("error1");
    }

    @Test
    public void andThenCanceled() {
        final AtomicInteger completableRunCount = new AtomicInteger();
        Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                completableRunCount.incrementAndGet();
            }
        }).andThen(Completable.complete()).test(true).assertEmpty();
        assertEquals(0, completableRunCount.get());
    }

    @Test
    public void andThenFirstCancels() {
        final TestObserver<Void> to = new TestObserver<>();
        Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                to.dispose();
            }
        }).andThen(Completable.complete()).subscribe(to);
        to.assertNotComplete().assertNoErrors();
    }

    @Test
    public void andThenSecondCancels() {
        final TestObserver<Void> to = new TestObserver<>();
        Completable.complete().andThen(Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                to.dispose();
            }
        })).subscribe(to);
        to.assertNotComplete().assertNoErrors();
    }

    @Test
    public void andThenDisposed() {
        TestHelper.checkDisposed(Completable.complete().andThen(Completable.complete()));
    }

    @Test
    public void andThenNoInterrupt() throws InterruptedException {
        for (int k = 0; k < 100; k++) {
            final int count = 10;
            final CountDownLatch latch = new CountDownLatch(count);
            final boolean[] interrupted = { false };
            for (int i = 0; i < count; i++) {
                Completable.complete().subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).andThen(Completable.fromAction(new Action() {

                    @Override
                    public void run() throws Exception {
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            // System.out.println("Interrupted! " + Thread.currentThread());
                            interrupted[0] = true;
                        }
                    }
                })).subscribe(new Action() {

                    @Override
                    public void run() throws Exception {
                        latch.countDown();
                    }
                });
            }
            latch.await();
            assertFalse("The second Completable was interrupted!", interrupted[0]);
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeCompletable(c -> c.andThen(c));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private CompletableAndThenCompletableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenCompletableCompleteComplete() throws java.lang.Throwable {
            this.payloads.andThenCompletableCompleteComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenCompletableCompleteError() throws java.lang.Throwable {
            this.payloads.andThenCompletableCompleteError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenCompletableCompleteNever() throws java.lang.Throwable {
            this.payloads.andThenCompletableCompleteNever.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenCompletableErrorComplete() throws java.lang.Throwable {
            this.payloads.andThenCompletableErrorComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenCompletableErrorNever() throws java.lang.Throwable {
            this.payloads.andThenCompletableErrorNever.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenCompletableErrorError() throws java.lang.Throwable {
            this.payloads.andThenCompletableErrorError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenCanceled() throws java.lang.Throwable {
            this.payloads.andThenCanceled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenFirstCancels() throws java.lang.Throwable {
            this.payloads.andThenFirstCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenSecondCancels() throws java.lang.Throwable {
            this.payloads.andThenSecondCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenDisposed() throws java.lang.Throwable {
            this.payloads.andThenDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenNoInterrupt() throws java.lang.Throwable {
            this.payloads.andThenNoInterrupt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAndThenCompletableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAndThenCompletableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAndThenCompletableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAndThenCompletableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableAndThenCompletableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAndThenCompletableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableAndThenCompletableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableAndThenCompletableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement andThenCompletableCompleteComplete;

            public org.junit.runners.model.Statement andThenCompletableCompleteError;

            public org.junit.runners.model.Statement andThenCompletableCompleteNever;

            public org.junit.runners.model.Statement andThenCompletableErrorComplete;

            public org.junit.runners.model.Statement andThenCompletableErrorNever;

            public org.junit.runners.model.Statement andThenCompletableErrorError;

            public org.junit.runners.model.Statement andThenCanceled;

            public org.junit.runners.model.Statement andThenFirstCancels;

            public org.junit.runners.model.Statement andThenSecondCancels;

            public org.junit.runners.model.Statement andThenDisposed;

            public org.junit.runners.model.Statement andThenNoInterrupt;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.andThenCompletableCompleteComplete = _ClassStatement.forPayload(CompletableAndThenCompletableTest::andThenCompletableCompleteComplete, "andThenCompletableCompleteComplete", this);
            this.payloads.andThenCompletableCompleteError = _ClassStatement.forPayload(CompletableAndThenCompletableTest::andThenCompletableCompleteError, "andThenCompletableCompleteError", this);
            this.payloads.andThenCompletableCompleteNever = _ClassStatement.forPayload(CompletableAndThenCompletableTest::andThenCompletableCompleteNever, "andThenCompletableCompleteNever", this);
            this.payloads.andThenCompletableErrorComplete = _ClassStatement.forPayload(CompletableAndThenCompletableTest::andThenCompletableErrorComplete, "andThenCompletableErrorComplete", this);
            this.payloads.andThenCompletableErrorNever = _ClassStatement.forPayload(CompletableAndThenCompletableTest::andThenCompletableErrorNever, "andThenCompletableErrorNever", this);
            this.payloads.andThenCompletableErrorError = _ClassStatement.forPayload(CompletableAndThenCompletableTest::andThenCompletableErrorError, "andThenCompletableErrorError", this);
            this.payloads.andThenCanceled = _ClassStatement.forPayload(CompletableAndThenCompletableTest::andThenCanceled, "andThenCanceled", this);
            this.payloads.andThenFirstCancels = _ClassStatement.forPayload(CompletableAndThenCompletableTest::andThenFirstCancels, "andThenFirstCancels", this);
            this.payloads.andThenSecondCancels = _ClassStatement.forPayload(CompletableAndThenCompletableTest::andThenSecondCancels, "andThenSecondCancels", this);
            this.payloads.andThenDisposed = _ClassStatement.forPayload(CompletableAndThenCompletableTest::andThenDisposed, "andThenDisposed", this);
            this.payloads.andThenNoInterrupt = _ClassStatement.forPayload(CompletableAndThenCompletableTest::andThenNoInterrupt, "andThenNoInterrupt", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(CompletableAndThenCompletableTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
