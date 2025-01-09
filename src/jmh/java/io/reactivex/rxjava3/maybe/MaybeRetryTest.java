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
package io.reactivex.rxjava3.maybe;

import static org.junit.Assert.assertEquals;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.internal.functions.Functions;

public class MaybeRetryTest extends RxJavaTest {

    @Test
    public void retryTimesPredicateWithMatchingPredicate() {
        final AtomicInteger atomicInteger = new AtomicInteger(3);
        final AtomicInteger numberOfSubscribeCalls = new AtomicInteger(0);
        Maybe.fromCallable(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                numberOfSubscribeCalls.incrementAndGet();
                if (atomicInteger.decrementAndGet() != 0) {
                    throw new RuntimeException();
                }
                throw new IllegalArgumentException();
            }
        }).retry(Integer.MAX_VALUE, new Predicate<Throwable>() {

            @Override
            public boolean test(final Throwable throwable) throws Exception {
                return !(throwable instanceof IllegalArgumentException);
            }
        }).test().assertFailure(IllegalArgumentException.class);
        assertEquals(3, numberOfSubscribeCalls.get());
    }

    @Test
    public void retryTimesPredicateWithMatchingRetryAmount() {
        final AtomicInteger atomicInteger = new AtomicInteger(3);
        final AtomicInteger numberOfSubscribeCalls = new AtomicInteger(0);
        Maybe.fromCallable(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                numberOfSubscribeCalls.incrementAndGet();
                if (atomicInteger.decrementAndGet() != 0) {
                    throw new RuntimeException();
                }
                return true;
            }
        }).retry(2, Functions.alwaysTrue()).test().assertResult(true);
        assertEquals(3, numberOfSubscribeCalls.get());
    }

    @Test
    public void retryTimesPredicateWithNotMatchingRetryAmount() {
        final AtomicInteger atomicInteger = new AtomicInteger(3);
        final AtomicInteger numberOfSubscribeCalls = new AtomicInteger(0);
        Maybe.fromCallable(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                numberOfSubscribeCalls.incrementAndGet();
                if (atomicInteger.decrementAndGet() != 0) {
                    throw new RuntimeException();
                }
                return true;
            }
        }).retry(1, Functions.alwaysTrue()).test().assertFailure(RuntimeException.class);
        assertEquals(2, numberOfSubscribeCalls.get());
    }

    @Test
    public void retryTimesPredicateWithZeroRetries() {
        final AtomicInteger atomicInteger = new AtomicInteger(2);
        final AtomicInteger numberOfSubscribeCalls = new AtomicInteger(0);
        Maybe.fromCallable(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                numberOfSubscribeCalls.incrementAndGet();
                if (atomicInteger.decrementAndGet() != 0) {
                    throw new RuntimeException();
                }
                return true;
            }
        }).retry(0, Functions.alwaysTrue()).test().assertFailure(RuntimeException.class);
        assertEquals(1, numberOfSubscribeCalls.get());
    }

    @Test
    public void untilTrueJust() {
        Maybe.just(1).retryUntil(() -> true).test().assertResult(1);
    }

    @Test
    public void untilFalseJust() {
        Maybe.just(1).retryUntil(() -> false).test().assertResult(1);
    }

    @Test
    public void untilTrueEmpty() {
        Maybe.empty().retryUntil(() -> true).test().assertResult();
    }

    @Test
    public void untilFalseEmpty() {
        Maybe.empty().retryUntil(() -> false).test().assertResult();
    }

    @Test
    public void untilTrueError() {
        Maybe.error(new TestException()).retryUntil(() -> true).test().assertFailure(TestException.class);
    }

    @Test
    public void untilFalseError() {
        AtomicInteger counter = new AtomicInteger();
        Maybe.defer(() -> {
            if (counter.getAndIncrement() == 0) {
                return Maybe.error(new TestException());
            }
            return Maybe.just(1);
        }).retryUntil(() -> false).test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeRetryTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTimesPredicateWithMatchingPredicate() throws java.lang.Throwable {
            this.payloads.retryTimesPredicateWithMatchingPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTimesPredicateWithMatchingRetryAmount() throws java.lang.Throwable {
            this.payloads.retryTimesPredicateWithMatchingRetryAmount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTimesPredicateWithNotMatchingRetryAmount() throws java.lang.Throwable {
            this.payloads.retryTimesPredicateWithNotMatchingRetryAmount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTimesPredicateWithZeroRetries() throws java.lang.Throwable {
            this.payloads.retryTimesPredicateWithZeroRetries.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilTrueJust() throws java.lang.Throwable {
            this.payloads.untilTrueJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilFalseJust() throws java.lang.Throwable {
            this.payloads.untilFalseJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilTrueEmpty() throws java.lang.Throwable {
            this.payloads.untilTrueEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilFalseEmpty() throws java.lang.Throwable {
            this.payloads.untilFalseEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilTrueError() throws java.lang.Throwable {
            this.payloads.untilTrueError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilFalseError() throws java.lang.Throwable {
            this.payloads.untilFalseError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeRetryTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeRetryTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeRetryTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeRetryTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeRetryTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeRetryTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeRetryTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeRetryTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement retryTimesPredicateWithMatchingPredicate;

            public org.junit.runners.model.Statement retryTimesPredicateWithMatchingRetryAmount;

            public org.junit.runners.model.Statement retryTimesPredicateWithNotMatchingRetryAmount;

            public org.junit.runners.model.Statement retryTimesPredicateWithZeroRetries;

            public org.junit.runners.model.Statement untilTrueJust;

            public org.junit.runners.model.Statement untilFalseJust;

            public org.junit.runners.model.Statement untilTrueEmpty;

            public org.junit.runners.model.Statement untilFalseEmpty;

            public org.junit.runners.model.Statement untilTrueError;

            public org.junit.runners.model.Statement untilFalseError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.retryTimesPredicateWithMatchingPredicate = _ClassStatement.forPayload(MaybeRetryTest::retryTimesPredicateWithMatchingPredicate, "retryTimesPredicateWithMatchingPredicate", this);
            this.payloads.retryTimesPredicateWithMatchingRetryAmount = _ClassStatement.forPayload(MaybeRetryTest::retryTimesPredicateWithMatchingRetryAmount, "retryTimesPredicateWithMatchingRetryAmount", this);
            this.payloads.retryTimesPredicateWithNotMatchingRetryAmount = _ClassStatement.forPayload(MaybeRetryTest::retryTimesPredicateWithNotMatchingRetryAmount, "retryTimesPredicateWithNotMatchingRetryAmount", this);
            this.payloads.retryTimesPredicateWithZeroRetries = _ClassStatement.forPayload(MaybeRetryTest::retryTimesPredicateWithZeroRetries, "retryTimesPredicateWithZeroRetries", this);
            this.payloads.untilTrueJust = _ClassStatement.forPayload(MaybeRetryTest::untilTrueJust, "untilTrueJust", this);
            this.payloads.untilFalseJust = _ClassStatement.forPayload(MaybeRetryTest::untilFalseJust, "untilFalseJust", this);
            this.payloads.untilTrueEmpty = _ClassStatement.forPayload(MaybeRetryTest::untilTrueEmpty, "untilTrueEmpty", this);
            this.payloads.untilFalseEmpty = _ClassStatement.forPayload(MaybeRetryTest::untilFalseEmpty, "untilFalseEmpty", this);
            this.payloads.untilTrueError = _ClassStatement.forPayload(MaybeRetryTest::untilTrueError, "untilTrueError", this);
            this.payloads.untilFalseError = _ClassStatement.forPayload(MaybeRetryTest::untilFalseError, "untilFalseError", this);
        }
    }
}
