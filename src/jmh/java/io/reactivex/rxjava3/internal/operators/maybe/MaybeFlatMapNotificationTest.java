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

import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.testsupport.*;

public class MaybeFlatMapNotificationTest extends RxJavaTest {

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Maybe.just(1).flatMap(Functions.justFunction(Maybe.just(1)), Functions.justFunction(Maybe.just(1)), Functions.justSupplier(Maybe.just(1))));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Integer>, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Maybe<Integer> m) throws Exception {
                return m.flatMap(Functions.justFunction(Maybe.just(1)), Functions.justFunction(Maybe.just(1)), Functions.justSupplier(Maybe.just(1)));
            }
        });
    }

    @Test
    public void onSuccessNull() {
        Maybe.just(1).flatMap(Functions.justFunction((Maybe<Integer>) null), Functions.justFunction(Maybe.just(1)), Functions.justSupplier(Maybe.just(1))).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void onErrorNull() {
        TestObserverEx<Integer> to = Maybe.<Integer>error(new TestException()).flatMap(Functions.justFunction(Maybe.just(1)), Functions.justFunction((Maybe<Integer>) null), Functions.justSupplier(Maybe.just(1))).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> ce = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(ce, 0, TestException.class);
        TestHelper.assertError(ce, 1, NullPointerException.class);
    }

    @Test
    public void onCompleteNull() {
        Maybe.<Integer>empty().flatMap(Functions.justFunction(Maybe.just(1)), Functions.justFunction(Maybe.just(1)), Functions.justSupplier((Maybe<Integer>) null)).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void onSuccessEmpty() {
        Maybe.just(1).flatMap(Functions.justFunction(Maybe.<Integer>empty()), Functions.justFunction(Maybe.just(1)), Functions.justSupplier(Maybe.just(1))).test().assertResult();
    }

    @Test
    public void onSuccessError() {
        Maybe.just(1).flatMap(Functions.justFunction(Maybe.<Integer>error(new TestException())), Functions.justFunction((Maybe<Integer>) null), Functions.justSupplier(Maybe.just(1))).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeFlatMapNotificationTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessNull() throws java.lang.Throwable {
            this.payloads.onSuccessNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorNull() throws java.lang.Throwable {
            this.payloads.onErrorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteNull() throws java.lang.Throwable {
            this.payloads.onCompleteNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessEmpty() throws java.lang.Throwable {
            this.payloads.onSuccessEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessError() throws java.lang.Throwable {
            this.payloads.onSuccessError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapNotificationTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapNotificationTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapNotificationTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapNotificationTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeFlatMapNotificationTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeFlatMapNotificationTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeFlatMapNotificationTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeFlatMapNotificationTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement onSuccessNull;

            public org.junit.runners.model.Statement onErrorNull;

            public org.junit.runners.model.Statement onCompleteNull;

            public org.junit.runners.model.Statement onSuccessEmpty;

            public org.junit.runners.model.Statement onSuccessError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.dispose = _ClassStatement.forPayload(MaybeFlatMapNotificationTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(MaybeFlatMapNotificationTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.onSuccessNull = _ClassStatement.forPayload(MaybeFlatMapNotificationTest::onSuccessNull, "onSuccessNull", this);
            this.payloads.onErrorNull = _ClassStatement.forPayload(MaybeFlatMapNotificationTest::onErrorNull, "onErrorNull", this);
            this.payloads.onCompleteNull = _ClassStatement.forPayload(MaybeFlatMapNotificationTest::onCompleteNull, "onCompleteNull", this);
            this.payloads.onSuccessEmpty = _ClassStatement.forPayload(MaybeFlatMapNotificationTest::onSuccessEmpty, "onSuccessEmpty", this);
            this.payloads.onSuccessError = _ClassStatement.forPayload(MaybeFlatMapNotificationTest::onSuccessError, "onSuccessError", this);
        }
    }
}
