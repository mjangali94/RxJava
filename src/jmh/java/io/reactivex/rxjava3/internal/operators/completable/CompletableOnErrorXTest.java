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

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableOnErrorXTest extends RxJavaTest {

    @Test
    public void normalReturn() {
        Completable.complete().onErrorComplete().test().assertResult();
    }

    @Test
    public void normalResumeNext() {
        final int[] call = { 0 };
        Completable.complete().onErrorResumeNext(new Function<Throwable, CompletableSource>() {

            @Override
            public CompletableSource apply(Throwable e) throws Exception {
                call[0]++;
                return Completable.complete();
            }
        }).test().assertResult();
        assertEquals(0, call[0]);
    }

    @Test
    public void onErrorReturnConst() {
        Completable.error(new TestException()).onErrorReturnItem(1).test().assertResult(1);
    }

    @Test
    public void onErrorReturn() {
        Completable.error(new TestException()).onErrorReturn(Functions.justFunction(1)).test().assertResult(1);
    }

    @Test
    public void onErrorReturnFunctionThrows() {
        TestHelper.assertCompositeExceptions(Completable.error(new TestException()).onErrorReturn(new Function<Throwable, Object>() {

            @Override
            public Object apply(Throwable v) throws Exception {
                throw new IOException();
            }
        }).to(TestHelper.testConsumer()), TestException.class, IOException.class);
    }

    @Test
    public void onErrorReturnEmpty() {
        Completable.complete().onErrorReturnItem(2).test().assertResult();
    }

    @Test
    public void onErrorReturnDispose() {
        TestHelper.checkDisposed(CompletableSubject.create().onErrorReturnItem(1));
    }

    @Test
    public void onErrorReturnDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeCompletableToMaybe(new Function<Completable, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Completable v) throws Exception {
                return v.onErrorReturnItem(1);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public CompletableOnErrorXTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalReturn() throws java.lang.Throwable {
            this.payloads.normalReturn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalResumeNext() throws java.lang.Throwable {
            this.payloads.normalResumeNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnConst() throws java.lang.Throwable {
            this.payloads.onErrorReturnConst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturn() throws java.lang.Throwable {
            this.payloads.onErrorReturn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnFunctionThrows() throws java.lang.Throwable {
            this.payloads.onErrorReturnFunctionThrows.evaluate();
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

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableOnErrorXTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableOnErrorXTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableOnErrorXTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableOnErrorXTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableOnErrorXTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableOnErrorXTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableOnErrorXTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableOnErrorXTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normalReturn;

            public org.junit.runners.model.Statement normalResumeNext;

            public org.junit.runners.model.Statement onErrorReturnConst;

            public org.junit.runners.model.Statement onErrorReturn;

            public org.junit.runners.model.Statement onErrorReturnFunctionThrows;

            public org.junit.runners.model.Statement onErrorReturnEmpty;

            public org.junit.runners.model.Statement onErrorReturnDispose;

            public org.junit.runners.model.Statement onErrorReturnDoubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normalReturn = _ClassStatement.forPayload(CompletableOnErrorXTest::normalReturn, "normalReturn", this);
            this.payloads.normalResumeNext = _ClassStatement.forPayload(CompletableOnErrorXTest::normalResumeNext, "normalResumeNext", this);
            this.payloads.onErrorReturnConst = _ClassStatement.forPayload(CompletableOnErrorXTest::onErrorReturnConst, "onErrorReturnConst", this);
            this.payloads.onErrorReturn = _ClassStatement.forPayload(CompletableOnErrorXTest::onErrorReturn, "onErrorReturn", this);
            this.payloads.onErrorReturnFunctionThrows = _ClassStatement.forPayload(CompletableOnErrorXTest::onErrorReturnFunctionThrows, "onErrorReturnFunctionThrows", this);
            this.payloads.onErrorReturnEmpty = _ClassStatement.forPayload(CompletableOnErrorXTest::onErrorReturnEmpty, "onErrorReturnEmpty", this);
            this.payloads.onErrorReturnDispose = _ClassStatement.forPayload(CompletableOnErrorXTest::onErrorReturnDispose, "onErrorReturnDispose", this);
            this.payloads.onErrorReturnDoubleOnSubscribe = _ClassStatement.forPayload(CompletableOnErrorXTest::onErrorReturnDoubleOnSubscribe, "onErrorReturnDoubleOnSubscribe", this);
        }
    }
}
