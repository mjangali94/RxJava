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
package io.reactivex.rxjava3.internal.jdk8;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.parallel.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ParallelMapOptionalTest extends RxJavaTest {

    @Test
    public void doubleFilter() {
        Flowable.range(1, 10).parallel().mapOptional(Optional::of).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 3 == 0;
            }
        }).sequential().test().assertResult(6);
    }

    @Test
    public void doubleFilterAsync() {
        Flowable.range(1, 10).parallel().runOn(Schedulers.computation()).mapOptional(Optional::of).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 3 == 0;
            }
        }).sequential().test().awaitDone(5, TimeUnit.SECONDS).assertResult(6);
    }

    @Test
    public void doubleError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().mapOptional(Optional::of).sequential().test().assertFailure(TestException.class);
            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doubleError2() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().mapOptional(Optional::of).filter(Functions.alwaysTrue()).sequential().test().assertFailure(TestException.class);
            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).parallel().mapOptional(Optional::of).sequential().test().assertFailure(TestException.class);
    }

    @Test
    public void mapCrash() {
        Flowable.just(1).parallel().mapOptional(v -> {
            throw new TestException();
        }).sequential().test().assertFailure(TestException.class);
    }

    @Test
    public void mapCrashConditional() {
        Flowable.just(1).parallel().mapOptional(v -> {
            throw new TestException();
        }).filter(Functions.alwaysTrue()).sequential().test().assertFailure(TestException.class);
    }

    @Test
    public void mapCrashConditional2() {
        Flowable.just(1).parallel().runOn(Schedulers.computation()).mapOptional(v -> {
            throw new TestException();
        }).filter(Functions.alwaysTrue()).sequential().test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void allNone() {
        Flowable.range(1, 1000).parallel().mapOptional(v -> Optional.empty()).sequential().test().assertResult();
    }

    @Test
    public void allNoneConditional() {
        Flowable.range(1, 1000).parallel().mapOptional(v -> Optional.empty()).filter(v -> true).sequential().test().assertResult();
    }

    @Test
    public void mixed() {
        Flowable.range(1, 1000).parallel().mapOptional(v -> v % 2 == 0 ? Optional.of(v) : Optional.empty()).sequential().test().assertValueCount(500).assertNoErrors().assertComplete();
    }

    @Test
    public void mixedConditional() {
        Flowable.range(1, 1000).parallel().mapOptional(v -> v % 2 == 0 ? Optional.of(v) : Optional.empty()).filter(v -> true).sequential().test().assertValueCount(500).assertNoErrors().assertComplete();
    }

    @Test
    public void invalidSubscriberCount() {
        TestHelper.checkInvalidParallelSubscribers(Flowable.range(1, 10).parallel().mapOptional(Optional::of));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeParallel(p -> p.mapOptional(Optional::of));
        TestHelper.checkDoubleOnSubscribeParallel(p -> p.mapOptional(Optional::of).filter(v -> true));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ParallelMapOptionalTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleFilter() throws java.lang.Throwable {
            this.payloads.doubleFilter.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleFilterAsync() throws java.lang.Throwable {
            this.payloads.doubleFilterAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleError() throws java.lang.Throwable {
            this.payloads.doubleError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleError2() throws java.lang.Throwable {
            this.payloads.doubleError2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapCrash() throws java.lang.Throwable {
            this.payloads.mapCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapCrashConditional() throws java.lang.Throwable {
            this.payloads.mapCrashConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapCrashConditional2() throws java.lang.Throwable {
            this.payloads.mapCrashConditional2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allNone() throws java.lang.Throwable {
            this.payloads.allNone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allNoneConditional() throws java.lang.Throwable {
            this.payloads.allNoneConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixed() throws java.lang.Throwable {
            this.payloads.mixed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixedConditional() throws java.lang.Throwable {
            this.payloads.mixedConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_invalidSubscriberCount() throws java.lang.Throwable {
            this.payloads.invalidSubscriberCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelMapOptionalTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelMapOptionalTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelMapOptionalTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelMapOptionalTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ParallelMapOptionalTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelMapOptionalTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ParallelMapOptionalTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ParallelMapOptionalTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement doubleFilter;

            public org.junit.runners.model.Statement doubleFilterAsync;

            public org.junit.runners.model.Statement doubleError;

            public org.junit.runners.model.Statement doubleError2;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement mapCrash;

            public org.junit.runners.model.Statement mapCrashConditional;

            public org.junit.runners.model.Statement mapCrashConditional2;

            public org.junit.runners.model.Statement allNone;

            public org.junit.runners.model.Statement allNoneConditional;

            public org.junit.runners.model.Statement mixed;

            public org.junit.runners.model.Statement mixedConditional;

            public org.junit.runners.model.Statement invalidSubscriberCount;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.doubleFilter = _ClassStatement.forPayload(ParallelMapOptionalTest::doubleFilter, "doubleFilter", this);
            this.payloads.doubleFilterAsync = _ClassStatement.forPayload(ParallelMapOptionalTest::doubleFilterAsync, "doubleFilterAsync", this);
            this.payloads.doubleError = _ClassStatement.forPayload(ParallelMapOptionalTest::doubleError, "doubleError", this);
            this.payloads.doubleError2 = _ClassStatement.forPayload(ParallelMapOptionalTest::doubleError2, "doubleError2", this);
            this.payloads.error = _ClassStatement.forPayload(ParallelMapOptionalTest::error, "error", this);
            this.payloads.mapCrash = _ClassStatement.forPayload(ParallelMapOptionalTest::mapCrash, "mapCrash", this);
            this.payloads.mapCrashConditional = _ClassStatement.forPayload(ParallelMapOptionalTest::mapCrashConditional, "mapCrashConditional", this);
            this.payloads.mapCrashConditional2 = _ClassStatement.forPayload(ParallelMapOptionalTest::mapCrashConditional2, "mapCrashConditional2", this);
            this.payloads.allNone = _ClassStatement.forPayload(ParallelMapOptionalTest::allNone, "allNone", this);
            this.payloads.allNoneConditional = _ClassStatement.forPayload(ParallelMapOptionalTest::allNoneConditional, "allNoneConditional", this);
            this.payloads.mixed = _ClassStatement.forPayload(ParallelMapOptionalTest::mixed, "mixed", this);
            this.payloads.mixedConditional = _ClassStatement.forPayload(ParallelMapOptionalTest::mixedConditional, "mixedConditional", this);
            this.payloads.invalidSubscriberCount = _ClassStatement.forPayload(ParallelMapOptionalTest::invalidSubscriberCount, "invalidSubscriberCount", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ParallelMapOptionalTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
