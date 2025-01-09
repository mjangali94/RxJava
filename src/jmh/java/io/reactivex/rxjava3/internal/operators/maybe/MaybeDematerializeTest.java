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

import static org.mockito.Mockito.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.subjects.MaybeSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeDematerializeTest extends RxJavaTest {

    @Test
    public void success() {
        Maybe.just(Notification.createOnNext(1)).dematerialize(Functions.<Notification<Integer>>identity()).test().assertResult(1);
    }

    @Test
    public void empty() {
        Maybe.just(Notification.<Integer>createOnComplete()).dematerialize(Functions.<Notification<Integer>>identity()).test().assertResult();
    }

    @Test
    public void emptySource() throws Throwable {
        @SuppressWarnings("unchecked")
        Function<Notification<Integer>, Notification<Integer>> function = mock(Function.class);
        Maybe.<Notification<Integer>>empty().dematerialize(function).test().assertResult();
        verify(function, never()).apply(any());
    }

    @Test
    public void error() {
        Maybe.<Notification<Integer>>error(new TestException()).dematerialize(Functions.<Notification<Integer>>identity()).test().assertFailure(TestException.class);
    }

    @Test
    public void errorNotification() {
        Maybe.just(Notification.<Integer>createOnError(new TestException())).dematerialize(Functions.<Notification<Integer>>identity()).test().assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public MaybeSource<Object> apply(Maybe<Object> v) throws Exception {
                return v.dematerialize((Function) Functions.identity());
            }
        });
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(MaybeSubject.<Notification<Integer>>create().dematerialize(Functions.<Notification<Integer>>identity()));
    }

    @Test
    public void selectorCrash() {
        Maybe.just(Notification.createOnNext(1)).dematerialize(new Function<Notification<Integer>, Notification<Integer>>() {

            @Override
            public Notification<Integer> apply(Notification<Integer> v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void selectorNull() {
        Maybe.just(Notification.createOnNext(1)).dematerialize(Functions.justFunction((Notification<Integer>) null)).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void selectorDifferentType() {
        Maybe.just(Notification.createOnNext(1)).dematerialize(new Function<Notification<Integer>, Notification<String>>() {

            @Override
            public Notification<String> apply(Notification<Integer> v) throws Exception {
                return Notification.createOnNext("Value-" + 1);
            }
        }).test().assertResult("Value-1");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeDematerializeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_success() throws java.lang.Throwable {
            this.payloads.success.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptySource() throws java.lang.Throwable {
            this.payloads.emptySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNotification() throws java.lang.Throwable {
            this.payloads.errorNotification.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorCrash() throws java.lang.Throwable {
            this.payloads.selectorCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorNull() throws java.lang.Throwable {
            this.payloads.selectorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorDifferentType() throws java.lang.Throwable {
            this.payloads.selectorDifferentType.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDematerializeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDematerializeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDematerializeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDematerializeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeDematerializeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDematerializeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeDematerializeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeDematerializeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement success;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement emptySource;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement errorNotification;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement selectorCrash;

            public org.junit.runners.model.Statement selectorNull;

            public org.junit.runners.model.Statement selectorDifferentType;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.success = _ClassStatement.forPayload(MaybeDematerializeTest::success, "success", this);
            this.payloads.empty = _ClassStatement.forPayload(MaybeDematerializeTest::empty, "empty", this);
            this.payloads.emptySource = _ClassStatement.forPayload(MaybeDematerializeTest::emptySource, "emptySource", this);
            this.payloads.error = _ClassStatement.forPayload(MaybeDematerializeTest::error, "error", this);
            this.payloads.errorNotification = _ClassStatement.forPayload(MaybeDematerializeTest::errorNotification, "errorNotification", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(MaybeDematerializeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.dispose = _ClassStatement.forPayload(MaybeDematerializeTest::dispose, "dispose", this);
            this.payloads.selectorCrash = _ClassStatement.forPayload(MaybeDematerializeTest::selectorCrash, "selectorCrash", this);
            this.payloads.selectorNull = _ClassStatement.forPayload(MaybeDematerializeTest::selectorNull, "selectorNull", this);
            this.payloads.selectorDifferentType = _ClassStatement.forPayload(MaybeDematerializeTest::selectorDifferentType, "selectorDifferentType", this);
        }
    }
}
