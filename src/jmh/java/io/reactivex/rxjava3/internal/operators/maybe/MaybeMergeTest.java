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
package io.reactivex.rxjava3.internal.operators.maybe;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeMergeTest extends RxJavaTest {

    @Test
    public void delayErrorWithMaxConcurrency() {
        Maybe.mergeDelayError(Flowable.just(Maybe.just(1), Maybe.just(2), Maybe.just(3)), 1).test().assertResult(1, 2, 3);
    }

    @Test
    public void delayErrorWithMaxConcurrencyError() {
        Maybe.mergeDelayError(Flowable.just(Maybe.just(1), Maybe.<Integer>error(new TestException()), Maybe.just(3)), 1).test().assertFailure(TestException.class, 1, 3);
    }

    @Test
    public void delayErrorWithMaxConcurrencyAsync() {
        final AtomicInteger count = new AtomicInteger();
        @SuppressWarnings("unchecked")
        Maybe<Integer>[] sources = new Maybe[3];
        for (int i = 0; i < 3; i++) {
            final int j = i + 1;
            sources[i] = Maybe.fromCallable(new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
                    return count.incrementAndGet() - j;
                }
            }).subscribeOn(Schedulers.io());
        }
        for (int i = 0; i < 1000; i++) {
            count.set(0);
            Maybe.mergeDelayError(Flowable.fromArray(sources), 1).test().awaitDone(5, TimeUnit.SECONDS).assertResult(0, 0, 0);
        }
    }

    @Test
    public void delayErrorWithMaxConcurrencyAsyncError() {
        final AtomicInteger count = new AtomicInteger();
        @SuppressWarnings("unchecked")
        Maybe<Integer>[] sources = new Maybe[3];
        for (int i = 0; i < 3; i++) {
            final int j = i + 1;
            sources[i] = Maybe.fromCallable(new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
                    return count.incrementAndGet() - j;
                }
            }).subscribeOn(Schedulers.io());
        }
        sources[1] = Maybe.fromCallable(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                throw new TestException("" + count.incrementAndGet());
            }
        }).subscribeOn(Schedulers.io());
        for (int i = 0; i < 1000; i++) {
            count.set(0);
            Maybe.mergeDelayError(Flowable.fromArray(sources), 1).to(TestHelper.<Integer>testConsumer()).awaitDone(5, TimeUnit.SECONDS).assertFailureAndMessage(TestException.class, "2", 0, 0);
        }
    }

    @Test
    public void scalar() {
        Maybe.mergeDelayError(Flowable.just(Maybe.just(1))).test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeMergeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorWithMaxConcurrency() throws java.lang.Throwable {
            this.payloads.delayErrorWithMaxConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorWithMaxConcurrencyError() throws java.lang.Throwable {
            this.payloads.delayErrorWithMaxConcurrencyError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorWithMaxConcurrencyAsync() throws java.lang.Throwable {
            this.payloads.delayErrorWithMaxConcurrencyAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorWithMaxConcurrencyAsyncError() throws java.lang.Throwable {
            this.payloads.delayErrorWithMaxConcurrencyAsyncError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalar() throws java.lang.Throwable {
            this.payloads.scalar.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeMergeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeMergeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeMergeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeMergeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeMergeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeMergeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeMergeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeMergeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement delayErrorWithMaxConcurrency;

            public org.junit.runners.model.Statement delayErrorWithMaxConcurrencyError;

            public org.junit.runners.model.Statement delayErrorWithMaxConcurrencyAsync;

            public org.junit.runners.model.Statement delayErrorWithMaxConcurrencyAsyncError;

            public org.junit.runners.model.Statement scalar;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.delayErrorWithMaxConcurrency = _ClassStatement.forPayload(MaybeMergeTest::delayErrorWithMaxConcurrency, "delayErrorWithMaxConcurrency", this);
            this.payloads.delayErrorWithMaxConcurrencyError = _ClassStatement.forPayload(MaybeMergeTest::delayErrorWithMaxConcurrencyError, "delayErrorWithMaxConcurrencyError", this);
            this.payloads.delayErrorWithMaxConcurrencyAsync = _ClassStatement.forPayload(MaybeMergeTest::delayErrorWithMaxConcurrencyAsync, "delayErrorWithMaxConcurrencyAsync", this);
            this.payloads.delayErrorWithMaxConcurrencyAsyncError = _ClassStatement.forPayload(MaybeMergeTest::delayErrorWithMaxConcurrencyAsyncError, "delayErrorWithMaxConcurrencyAsyncError", this);
            this.payloads.scalar = _ClassStatement.forPayload(MaybeMergeTest::scalar, "scalar", this);
        }
    }
}
