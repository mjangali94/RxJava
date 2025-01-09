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

import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.parallel.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class ParallelMapTryOptionalTest extends RxJavaTest implements Consumer<Object> {

    volatile int calls;

    @Override
    public void accept(Object t) throws Exception {
        calls++;
    }

    @Test
    public void mapNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.just(1).parallel(1).mapOptional(Optional::of, e).sequential().test().assertResult(1);
        }
    }

    @Test
    public void mapErrorNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.<Integer>error(new TestException()).parallel(1).mapOptional(Optional::of, e).sequential().test().assertFailure(TestException.class);
        }
    }

    @Test
    public void mapConditionalNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.just(1).parallel(1).mapOptional(Optional::of, e).filter(Functions.alwaysTrue()).sequential().test().assertResult(1);
        }
    }

    @Test
    public void mapErrorConditionalNoError() {
        for (ParallelFailureHandling e : ParallelFailureHandling.values()) {
            Flowable.<Integer>error(new TestException()).parallel(1).mapOptional(Optional::of, e).filter(Functions.alwaysTrue()).sequential().test().assertFailure(TestException.class);
        }
    }

    @Test
    public void mapFailWithError() {
        Flowable.range(0, 2).parallel(1).mapOptional(v -> Optional.of(1 / v), ParallelFailureHandling.ERROR).sequential().test().assertFailure(ArithmeticException.class);
    }

    @Test
    public void mapFailWithStop() {
        Flowable.range(0, 2).parallel(1).mapOptional(v -> Optional.of(1 / v), ParallelFailureHandling.STOP).sequential().test().assertResult();
    }

    @Test
    public void mapFailWithRetry() {
        Flowable.range(0, 2).parallel(1).mapOptional(new Function<Integer, Optional<? extends Integer>>() {

            int count;

            @Override
            public Optional<? extends Integer> apply(Integer v) throws Exception {
                if (count++ == 1) {
                    return Optional.of(-1);
                }
                return Optional.of(1 / v);
            }
        }, ParallelFailureHandling.RETRY).sequential().test().assertResult(-1, 1);
    }

    @Test
    public void mapFailWithRetryLimited() {
        Flowable.range(0, 2).parallel(1).mapOptional(v -> Optional.of(1 / v), new BiFunction<Long, Throwable, ParallelFailureHandling>() {

            @Override
            public ParallelFailureHandling apply(Long n, Throwable e) throws Exception {
                return n < 5 ? ParallelFailureHandling.RETRY : ParallelFailureHandling.SKIP;
            }
        }).sequential().test().assertResult(1);
    }

    @Test
    public void mapFailWithSkip() {
        Flowable.range(0, 2).parallel(1).mapOptional(v -> Optional.of(1 / v), ParallelFailureHandling.SKIP).sequential().test().assertResult(1);
    }

    @Test
    public void mapFailHandlerThrows() {
        TestSubscriberEx<Integer> ts = Flowable.range(0, 2).parallel(1).mapOptional(v -> Optional.of(1 / v), new BiFunction<Long, Throwable, ParallelFailureHandling>() {

            @Override
            public ParallelFailureHandling apply(Long n, Throwable e) throws Exception {
                throw new TestException();
            }
        }).sequential().to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        TestHelper.assertCompositeExceptions(ts, ArithmeticException.class, TestException.class);
    }

    @Test
    public void mapInvalidSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().mapOptional(Optional::of, ParallelFailureHandling.ERROR).sequential().test();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void mapFailWithErrorConditional() {
        Flowable.range(0, 2).parallel(1).mapOptional(v -> Optional.of(1 / v), ParallelFailureHandling.ERROR).filter(Functions.alwaysTrue()).sequential().test().assertFailure(ArithmeticException.class);
    }

    @Test
    public void mapFailWithStopConditional() {
        Flowable.range(0, 2).parallel(1).mapOptional(v -> Optional.of(1 / v), ParallelFailureHandling.STOP).filter(Functions.alwaysTrue()).sequential().test().assertResult();
    }

    @Test
    public void mapFailWithRetryConditional() {
        Flowable.range(0, 2).parallel(1).mapOptional(new Function<Integer, Optional<? extends Integer>>() {

            int count;

            @Override
            public Optional<? extends Integer> apply(Integer v) throws Exception {
                if (count++ == 1) {
                    return Optional.of(-1);
                }
                return Optional.of(1 / v);
            }
        }, ParallelFailureHandling.RETRY).filter(Functions.alwaysTrue()).sequential().test().assertResult(-1, 1);
    }

    @Test
    public void mapFailWithRetryLimitedConditional() {
        Flowable.range(0, 2).parallel(1).mapOptional(v -> Optional.of(1 / v), new BiFunction<Long, Throwable, ParallelFailureHandling>() {

            @Override
            public ParallelFailureHandling apply(Long n, Throwable e) throws Exception {
                return n < 5 ? ParallelFailureHandling.RETRY : ParallelFailureHandling.SKIP;
            }
        }).filter(Functions.alwaysTrue()).sequential().test().assertResult(1);
    }

    @Test
    public void mapFailWithSkipConditional() {
        Flowable.range(0, 2).parallel(1).mapOptional(v -> Optional.of(1 / v), ParallelFailureHandling.SKIP).filter(Functions.alwaysTrue()).sequential().test().assertResult(1);
    }

    @Test
    public void mapFailHandlerThrowsConditional() {
        TestSubscriberEx<Integer> ts = Flowable.range(0, 2).parallel(1).mapOptional(v -> Optional.of(1 / v), new BiFunction<Long, Throwable, ParallelFailureHandling>() {

            @Override
            public ParallelFailureHandling apply(Long n, Throwable e) throws Exception {
                throw new TestException();
            }
        }).filter(Functions.alwaysTrue()).sequential().to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        TestHelper.assertCompositeExceptions(ts, ArithmeticException.class, TestException.class);
    }

    @Test
    public void mapWrongParallelismConditional() {
        TestHelper.checkInvalidParallelSubscribers(Flowable.just(1).parallel(1).mapOptional(Optional::of, ParallelFailureHandling.ERROR).filter(Functions.alwaysTrue()));
    }

    @Test
    public void mapInvalidSourceConditional() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new ParallelInvalid().mapOptional(Optional::of, ParallelFailureHandling.ERROR).filter(Functions.alwaysTrue()).sequential().test();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void failureHandlingEnum() {
        TestHelper.checkEnum(ParallelFailureHandling.class);
    }

    @Test
    public void allNone() {
        Flowable.range(1, 1000).parallel().mapOptional(v -> Optional.empty(), ParallelFailureHandling.SKIP).sequential().test().assertResult();
    }

    @Test
    public void allNoneConditional() {
        Flowable.range(1, 1000).parallel().mapOptional(v -> Optional.empty(), ParallelFailureHandling.SKIP).filter(v -> true).sequential().test().assertResult();
    }

    @Test
    public void mixed() {
        Flowable.range(1, 1000).parallel().mapOptional(v -> v % 2 == 0 ? Optional.of(v) : Optional.empty(), ParallelFailureHandling.SKIP).sequential().test().assertValueCount(500).assertNoErrors().assertComplete();
    }

    @Test
    public void mixedConditional() {
        Flowable.range(1, 1000).parallel().mapOptional(v -> v % 2 == 0 ? Optional.of(v) : Optional.empty(), ParallelFailureHandling.SKIP).filter(v -> true).sequential().test().assertValueCount(500).assertNoErrors().assertComplete();
    }

    @Test
    public void mixedConditional2() {
        Flowable.range(1, 1000).parallel().mapOptional(v -> v % 2 == 0 ? Optional.of(v) : Optional.empty(), ParallelFailureHandling.SKIP).filter(v -> v % 4 == 0).sequential().test().assertValueCount(250).assertNoErrors().assertComplete();
    }

    @Test
    public void invalidSubscriberCount() {
        TestHelper.checkInvalidParallelSubscribers(Flowable.range(1, 10).parallel().mapOptional(Optional::of, ParallelFailureHandling.SKIP));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeParallel(p -> p.mapOptional(Optional::of, ParallelFailureHandling.ERROR));
        TestHelper.checkDoubleOnSubscribeParallel(p -> p.mapOptional(Optional::of, ParallelFailureHandling.ERROR).filter(v -> true));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ParallelMapTryOptionalTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapNoError() throws java.lang.Throwable {
            this.payloads.mapNoError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapErrorNoError() throws java.lang.Throwable {
            this.payloads.mapErrorNoError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapConditionalNoError() throws java.lang.Throwable {
            this.payloads.mapConditionalNoError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapErrorConditionalNoError() throws java.lang.Throwable {
            this.payloads.mapErrorConditionalNoError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithError() throws java.lang.Throwable {
            this.payloads.mapFailWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithStop() throws java.lang.Throwable {
            this.payloads.mapFailWithStop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithRetry() throws java.lang.Throwable {
            this.payloads.mapFailWithRetry.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithRetryLimited() throws java.lang.Throwable {
            this.payloads.mapFailWithRetryLimited.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithSkip() throws java.lang.Throwable {
            this.payloads.mapFailWithSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailHandlerThrows() throws java.lang.Throwable {
            this.payloads.mapFailHandlerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapInvalidSource() throws java.lang.Throwable {
            this.payloads.mapInvalidSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithErrorConditional() throws java.lang.Throwable {
            this.payloads.mapFailWithErrorConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithStopConditional() throws java.lang.Throwable {
            this.payloads.mapFailWithStopConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithRetryConditional() throws java.lang.Throwable {
            this.payloads.mapFailWithRetryConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithRetryLimitedConditional() throws java.lang.Throwable {
            this.payloads.mapFailWithRetryLimitedConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailWithSkipConditional() throws java.lang.Throwable {
            this.payloads.mapFailWithSkipConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFailHandlerThrowsConditional() throws java.lang.Throwable {
            this.payloads.mapFailHandlerThrowsConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapWrongParallelismConditional() throws java.lang.Throwable {
            this.payloads.mapWrongParallelismConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapInvalidSourceConditional() throws java.lang.Throwable {
            this.payloads.mapInvalidSourceConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failureHandlingEnum() throws java.lang.Throwable {
            this.payloads.failureHandlingEnum.evaluate();
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
        public void benchmark_mixedConditional2() throws java.lang.Throwable {
            this.payloads.mixedConditional2.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelMapTryOptionalTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelMapTryOptionalTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelMapTryOptionalTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelMapTryOptionalTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ParallelMapTryOptionalTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ParallelMapTryOptionalTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ParallelMapTryOptionalTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ParallelMapTryOptionalTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement mapNoError;

            public org.junit.runners.model.Statement mapErrorNoError;

            public org.junit.runners.model.Statement mapConditionalNoError;

            public org.junit.runners.model.Statement mapErrorConditionalNoError;

            public org.junit.runners.model.Statement mapFailWithError;

            public org.junit.runners.model.Statement mapFailWithStop;

            public org.junit.runners.model.Statement mapFailWithRetry;

            public org.junit.runners.model.Statement mapFailWithRetryLimited;

            public org.junit.runners.model.Statement mapFailWithSkip;

            public org.junit.runners.model.Statement mapFailHandlerThrows;

            public org.junit.runners.model.Statement mapInvalidSource;

            public org.junit.runners.model.Statement mapFailWithErrorConditional;

            public org.junit.runners.model.Statement mapFailWithStopConditional;

            public org.junit.runners.model.Statement mapFailWithRetryConditional;

            public org.junit.runners.model.Statement mapFailWithRetryLimitedConditional;

            public org.junit.runners.model.Statement mapFailWithSkipConditional;

            public org.junit.runners.model.Statement mapFailHandlerThrowsConditional;

            public org.junit.runners.model.Statement mapWrongParallelismConditional;

            public org.junit.runners.model.Statement mapInvalidSourceConditional;

            public org.junit.runners.model.Statement failureHandlingEnum;

            public org.junit.runners.model.Statement allNone;

            public org.junit.runners.model.Statement allNoneConditional;

            public org.junit.runners.model.Statement mixed;

            public org.junit.runners.model.Statement mixedConditional;

            public org.junit.runners.model.Statement mixedConditional2;

            public org.junit.runners.model.Statement invalidSubscriberCount;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.mapNoError = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapNoError, "mapNoError", this);
            this.payloads.mapErrorNoError = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapErrorNoError, "mapErrorNoError", this);
            this.payloads.mapConditionalNoError = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapConditionalNoError, "mapConditionalNoError", this);
            this.payloads.mapErrorConditionalNoError = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapErrorConditionalNoError, "mapErrorConditionalNoError", this);
            this.payloads.mapFailWithError = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapFailWithError, "mapFailWithError", this);
            this.payloads.mapFailWithStop = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapFailWithStop, "mapFailWithStop", this);
            this.payloads.mapFailWithRetry = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapFailWithRetry, "mapFailWithRetry", this);
            this.payloads.mapFailWithRetryLimited = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapFailWithRetryLimited, "mapFailWithRetryLimited", this);
            this.payloads.mapFailWithSkip = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapFailWithSkip, "mapFailWithSkip", this);
            this.payloads.mapFailHandlerThrows = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapFailHandlerThrows, "mapFailHandlerThrows", this);
            this.payloads.mapInvalidSource = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapInvalidSource, "mapInvalidSource", this);
            this.payloads.mapFailWithErrorConditional = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapFailWithErrorConditional, "mapFailWithErrorConditional", this);
            this.payloads.mapFailWithStopConditional = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapFailWithStopConditional, "mapFailWithStopConditional", this);
            this.payloads.mapFailWithRetryConditional = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapFailWithRetryConditional, "mapFailWithRetryConditional", this);
            this.payloads.mapFailWithRetryLimitedConditional = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapFailWithRetryLimitedConditional, "mapFailWithRetryLimitedConditional", this);
            this.payloads.mapFailWithSkipConditional = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapFailWithSkipConditional, "mapFailWithSkipConditional", this);
            this.payloads.mapFailHandlerThrowsConditional = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapFailHandlerThrowsConditional, "mapFailHandlerThrowsConditional", this);
            this.payloads.mapWrongParallelismConditional = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapWrongParallelismConditional, "mapWrongParallelismConditional", this);
            this.payloads.mapInvalidSourceConditional = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mapInvalidSourceConditional, "mapInvalidSourceConditional", this);
            this.payloads.failureHandlingEnum = _ClassStatement.forPayload(ParallelMapTryOptionalTest::failureHandlingEnum, "failureHandlingEnum", this);
            this.payloads.allNone = _ClassStatement.forPayload(ParallelMapTryOptionalTest::allNone, "allNone", this);
            this.payloads.allNoneConditional = _ClassStatement.forPayload(ParallelMapTryOptionalTest::allNoneConditional, "allNoneConditional", this);
            this.payloads.mixed = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mixed, "mixed", this);
            this.payloads.mixedConditional = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mixedConditional, "mixedConditional", this);
            this.payloads.mixedConditional2 = _ClassStatement.forPayload(ParallelMapTryOptionalTest::mixedConditional2, "mixedConditional2", this);
            this.payloads.invalidSubscriberCount = _ClassStatement.forPayload(ParallelMapTryOptionalTest::invalidSubscriberCount, "invalidSubscriberCount", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ParallelMapTryOptionalTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
