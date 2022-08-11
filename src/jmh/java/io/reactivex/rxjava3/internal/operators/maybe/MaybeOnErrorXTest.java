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

import java.io.IOException;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeOnErrorXTest extends RxJavaTest {

    @Test
    public void onErrorReturnConst() {
        Maybe.error(new TestException()).onErrorReturnItem(1).test().assertResult(1);
    }

    @Test
    public void onErrorReturn() {
        Maybe.error(new TestException()).onErrorReturn(Functions.justFunction(1)).test().assertResult(1);
    }

    @Test
    public void onErrorComplete() {
        Maybe.error(new TestException()).onErrorComplete().test().assertResult();
    }

    @Test
    public void onErrorCompleteTrue() {
        Maybe.error(new TestException()).onErrorComplete(Functions.alwaysTrue()).test().assertResult();
    }

    @Test
    public void onErrorCompleteFalse() {
        Maybe.error(new TestException()).onErrorComplete(Functions.alwaysFalse()).test().assertFailure(TestException.class);
    }

    @Test
    public void onErrorReturnFunctionThrows() {
        TestHelper.assertCompositeExceptions(Maybe.error(new TestException()).onErrorReturn(new Function<Throwable, Object>() {

            @Override
            public Object apply(Throwable v) throws Exception {
                throw new IOException();
            }
        }).to(TestHelper.testConsumer()), TestException.class, IOException.class);
    }

    @Test
    public void onErrorCompletePredicateThrows() {
        TestHelper.assertCompositeExceptions(Maybe.error(new TestException()).onErrorComplete(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable v) throws Exception {
                throw new IOException();
            }
        }).to(TestHelper.testConsumer()), TestException.class, IOException.class);
    }

    @Test
    public void onErrorResumeNext() {
        Maybe.error(new TestException()).onErrorResumeNext(Functions.justFunction(Maybe.just(1))).test().assertResult(1);
    }

    @Test
    public void onErrorResumeNextFunctionThrows() {
        TestHelper.assertCompositeExceptions(Maybe.error(new TestException()).onErrorResumeNext(new Function<Throwable, Maybe<Object>>() {

            @Override
            public Maybe<Object> apply(Throwable v) throws Exception {
                throw new IOException();
            }
        }).to(TestHelper.testConsumer()), TestException.class, IOException.class);
    }

    @Test
    public void onErrorReturnSuccess() {
        Maybe.just(1).onErrorReturnItem(2).test().assertResult(1);
    }

    @Test
    public void onErrorReturnEmpty() {
        Maybe.<Integer>empty().onErrorReturnItem(2).test().assertResult();
    }

    @Test
    public void onErrorReturnDispose() {
        TestHelper.checkDisposed(PublishProcessor.create().singleElement().onErrorReturnItem(1));
    }

    @Test
    public void onErrorReturnDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Maybe<Object> v) throws Exception {
                return v.onErrorReturnItem(1);
            }
        });
    }

    @Test
    public void onErrorCompleteSuccess() {
        Maybe.just(1).onErrorComplete().test().assertResult(1);
    }

    @Test
    public void onErrorCompleteEmpty() {
        Maybe.<Integer>empty().onErrorComplete().test().assertResult();
    }

    @Test
    public void onErrorCompleteDispose() {
        TestHelper.checkDisposed(PublishProcessor.create().singleElement().onErrorComplete());
    }

    @Test
    public void onErrorCompleteDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Maybe<Object> v) throws Exception {
                return v.onErrorComplete();
            }
        });
    }

    @Test
    public void onErrorNextDispose() {
        TestHelper.checkDisposed(PublishProcessor.create().singleElement().onErrorResumeWith(Maybe.just(1)));
    }

    @Test
    public void onErrorNextDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Maybe<Object> v) throws Exception {
                return v.onErrorResumeWith(Maybe.just(1));
            }
        });
    }

    @Test
    public void onErrorNextIsAlsoError() {
        Maybe.error(new TestException("Main")).onErrorResumeWith(Maybe.error(new TestException("Secondary"))).to(TestHelper.testConsumer()).assertFailureAndMessage(TestException.class, "Secondary");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeOnErrorXTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnConst() throws java.lang.Throwable {
            this.payloads.onErrorReturnConst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturn() throws java.lang.Throwable {
            this.payloads.onErrorReturn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorComplete() throws java.lang.Throwable {
            this.payloads.onErrorComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteTrue() throws java.lang.Throwable {
            this.payloads.onErrorCompleteTrue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteFalse() throws java.lang.Throwable {
            this.payloads.onErrorCompleteFalse.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnFunctionThrows() throws java.lang.Throwable {
            this.payloads.onErrorReturnFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompletePredicateThrows() throws java.lang.Throwable {
            this.payloads.onErrorCompletePredicateThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNext() throws java.lang.Throwable {
            this.payloads.onErrorResumeNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNextFunctionThrows() throws java.lang.Throwable {
            this.payloads.onErrorResumeNextFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnSuccess() throws java.lang.Throwable {
            this.payloads.onErrorReturnSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnEmpty() throws java.lang.Throwable {
            this.payloads.onErrorReturnEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnDispose() throws java.lang.Throwable {
            this.payloads.onErrorReturnDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.onErrorReturnDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteSuccess() throws java.lang.Throwable {
            this.payloads.onErrorCompleteSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteEmpty() throws java.lang.Throwable {
            this.payloads.onErrorCompleteEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteDispose() throws java.lang.Throwable {
            this.payloads.onErrorCompleteDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.onErrorCompleteDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorNextDispose() throws java.lang.Throwable {
            this.payloads.onErrorNextDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorNextDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.onErrorNextDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorNextIsAlsoError() throws java.lang.Throwable {
            this.payloads.onErrorNextIsAlsoError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeOnErrorXTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeOnErrorXTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeOnErrorXTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeOnErrorXTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeOnErrorXTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeOnErrorXTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeOnErrorXTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeOnErrorXTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement onErrorReturnConst;

            public org.junit.runners.model.Statement onErrorReturn;

            public org.junit.runners.model.Statement onErrorComplete;

            public org.junit.runners.model.Statement onErrorCompleteTrue;

            public org.junit.runners.model.Statement onErrorCompleteFalse;

            public org.junit.runners.model.Statement onErrorReturnFunctionThrows;

            public org.junit.runners.model.Statement onErrorCompletePredicateThrows;

            public org.junit.runners.model.Statement onErrorResumeNext;

            public org.junit.runners.model.Statement onErrorResumeNextFunctionThrows;

            public org.junit.runners.model.Statement onErrorReturnSuccess;

            public org.junit.runners.model.Statement onErrorReturnEmpty;

            public org.junit.runners.model.Statement onErrorReturnDispose;

            public org.junit.runners.model.Statement onErrorReturnDoubleOnSubscribe;

            public org.junit.runners.model.Statement onErrorCompleteSuccess;

            public org.junit.runners.model.Statement onErrorCompleteEmpty;

            public org.junit.runners.model.Statement onErrorCompleteDispose;

            public org.junit.runners.model.Statement onErrorCompleteDoubleOnSubscribe;

            public org.junit.runners.model.Statement onErrorNextDispose;

            public org.junit.runners.model.Statement onErrorNextDoubleOnSubscribe;

            public org.junit.runners.model.Statement onErrorNextIsAlsoError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.onErrorReturnConst = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorReturnConst, "onErrorReturnConst", this);
            this.payloads.onErrorReturn = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorReturn, "onErrorReturn", this);
            this.payloads.onErrorComplete = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorComplete, "onErrorComplete", this);
            this.payloads.onErrorCompleteTrue = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorCompleteTrue, "onErrorCompleteTrue", this);
            this.payloads.onErrorCompleteFalse = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorCompleteFalse, "onErrorCompleteFalse", this);
            this.payloads.onErrorReturnFunctionThrows = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorReturnFunctionThrows, "onErrorReturnFunctionThrows", this);
            this.payloads.onErrorCompletePredicateThrows = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorCompletePredicateThrows, "onErrorCompletePredicateThrows", this);
            this.payloads.onErrorResumeNext = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorResumeNext, "onErrorResumeNext", this);
            this.payloads.onErrorResumeNextFunctionThrows = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorResumeNextFunctionThrows, "onErrorResumeNextFunctionThrows", this);
            this.payloads.onErrorReturnSuccess = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorReturnSuccess, "onErrorReturnSuccess", this);
            this.payloads.onErrorReturnEmpty = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorReturnEmpty, "onErrorReturnEmpty", this);
            this.payloads.onErrorReturnDispose = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorReturnDispose, "onErrorReturnDispose", this);
            this.payloads.onErrorReturnDoubleOnSubscribe = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorReturnDoubleOnSubscribe, "onErrorReturnDoubleOnSubscribe", this);
            this.payloads.onErrorCompleteSuccess = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorCompleteSuccess, "onErrorCompleteSuccess", this);
            this.payloads.onErrorCompleteEmpty = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorCompleteEmpty, "onErrorCompleteEmpty", this);
            this.payloads.onErrorCompleteDispose = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorCompleteDispose, "onErrorCompleteDispose", this);
            this.payloads.onErrorCompleteDoubleOnSubscribe = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorCompleteDoubleOnSubscribe, "onErrorCompleteDoubleOnSubscribe", this);
            this.payloads.onErrorNextDispose = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorNextDispose, "onErrorNextDispose", this);
            this.payloads.onErrorNextDoubleOnSubscribe = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorNextDoubleOnSubscribe, "onErrorNextDoubleOnSubscribe", this);
            this.payloads.onErrorNextIsAlsoError = _ClassStatement.forPayload(MaybeOnErrorXTest::onErrorNextIsAlsoError, "onErrorNextIsAlsoError", this);
        }
    }
}
