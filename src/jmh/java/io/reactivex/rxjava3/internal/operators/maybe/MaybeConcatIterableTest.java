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

import static org.junit.Assert.assertEquals;
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.util.CrashingMappedIterable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.MaybeSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeConcatIterableTest extends RxJavaTest {

    @Test
    public void take() {
        Maybe.concat(Arrays.asList(Maybe.just(1), Maybe.just(2), Maybe.just(3))).take(1).test().assertResult(1);
    }

    @Test
    public void iteratorThrows() {
        Maybe.concat(new Iterable<MaybeSource<Object>>() {

            @Override
            public Iterator<MaybeSource<Object>> iterator() {
                throw new TestException("iterator()");
            }
        }).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(TestException.class, "iterator()");
    }

    @Test
    public void error() {
        Maybe.concat(Arrays.asList(Maybe.just(1), Maybe.<Integer>error(new TestException()), Maybe.just(3))).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void successCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final TestSubscriber<Integer> ts = Maybe.concat(Arrays.asList(pp.singleElement())).test();
            pp.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    pp.onComplete();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void hasNextThrows() {
        Maybe.concat(new CrashingMappedIterable<>(100, 1, 100, new Function<Integer, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Integer v) throws Exception {
                return Maybe.just(1);
            }
        })).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "hasNext()");
    }

    @Test
    public void nextThrows() {
        Maybe.concat(new CrashingMappedIterable<>(100, 100, 1, new Function<Integer, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Integer v) throws Exception {
                return Maybe.just(1);
            }
        })).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "next()");
    }

    @Test
    public void nextReturnsNull() {
        Maybe.concat(new CrashingMappedIterable<>(100, 100, 100, new Function<Integer, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Integer v) throws Exception {
                return null;
            }
        })).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void noSubsequentSubscription() {
        final int[] calls = { 0 };
        Maybe<Integer> source = Maybe.create(new MaybeOnSubscribe<Integer>() {

            @Override
            public void subscribe(MaybeEmitter<Integer> s) throws Exception {
                calls[0]++;
                s.onSuccess(1);
            }
        });
        Maybe.concat(Arrays.asList(source, source)).firstElement().test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @Test
    public void noSubsequentSubscriptionDelayError() {
        final int[] calls = { 0 };
        Maybe<Integer> source = Maybe.create(new MaybeOnSubscribe<Integer>() {

            @Override
            public void subscribe(MaybeEmitter<Integer> s) throws Exception {
                calls[0]++;
                s.onSuccess(1);
            }
        });
        Maybe.concatDelayError(Arrays.asList(source, source)).firstElement().test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Maybe.concat(Arrays.asList(MaybeSubject.create(), MaybeSubject.create())));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeConcatIterableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorThrows() throws java.lang.Throwable {
            this.payloads.iteratorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successCancelRace() throws java.lang.Throwable {
            this.payloads.successCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrows() throws java.lang.Throwable {
            this.payloads.hasNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextThrows() throws java.lang.Throwable {
            this.payloads.nextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextReturnsNull() throws java.lang.Throwable {
            this.payloads.nextReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubsequentSubscription() throws java.lang.Throwable {
            this.payloads.noSubsequentSubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubsequentSubscriptionDelayError() throws java.lang.Throwable {
            this.payloads.noSubsequentSubscriptionDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatIterableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatIterableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatIterableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatIterableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeConcatIterableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeConcatIterableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeConcatIterableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeConcatIterableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement iteratorThrows;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement successCancelRace;

            public org.junit.runners.model.Statement hasNextThrows;

            public org.junit.runners.model.Statement nextThrows;

            public org.junit.runners.model.Statement nextReturnsNull;

            public org.junit.runners.model.Statement noSubsequentSubscription;

            public org.junit.runners.model.Statement noSubsequentSubscriptionDelayError;

            public org.junit.runners.model.Statement badRequest;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.take = _ClassStatement.forPayload(MaybeConcatIterableTest::take, "take", this);
            this.payloads.iteratorThrows = _ClassStatement.forPayload(MaybeConcatIterableTest::iteratorThrows, "iteratorThrows", this);
            this.payloads.error = _ClassStatement.forPayload(MaybeConcatIterableTest::error, "error", this);
            this.payloads.successCancelRace = _ClassStatement.forPayload(MaybeConcatIterableTest::successCancelRace, "successCancelRace", this);
            this.payloads.hasNextThrows = _ClassStatement.forPayload(MaybeConcatIterableTest::hasNextThrows, "hasNextThrows", this);
            this.payloads.nextThrows = _ClassStatement.forPayload(MaybeConcatIterableTest::nextThrows, "nextThrows", this);
            this.payloads.nextReturnsNull = _ClassStatement.forPayload(MaybeConcatIterableTest::nextReturnsNull, "nextReturnsNull", this);
            this.payloads.noSubsequentSubscription = _ClassStatement.forPayload(MaybeConcatIterableTest::noSubsequentSubscription, "noSubsequentSubscription", this);
            this.payloads.noSubsequentSubscriptionDelayError = _ClassStatement.forPayload(MaybeConcatIterableTest::noSubsequentSubscriptionDelayError, "noSubsequentSubscriptionDelayError", this);
            this.payloads.badRequest = _ClassStatement.forPayload(MaybeConcatIterableTest::badRequest, "badRequest", this);
        }
    }
}
