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
package io.reactivex.rxjava3.internal.operators.mixed;

import static org.junit.Assert.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleFlatMapObservableTest extends RxJavaTest {

    @Test
    public void cancelMain() {
        SingleSubject<Integer> ss = SingleSubject.create();
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ss.flatMapObservable(Functions.justFunction(ps)).test();
        assertTrue(ss.hasObservers());
        assertFalse(ps.hasObservers());
        to.dispose();
        assertFalse(ss.hasObservers());
        assertFalse(ps.hasObservers());
    }

    @Test
    public void cancelOther() {
        SingleSubject<Integer> ss = SingleSubject.create();
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ss.flatMapObservable(Functions.justFunction(ps)).test();
        assertTrue(ss.hasObservers());
        assertFalse(ps.hasObservers());
        ss.onSuccess(1);
        assertFalse(ss.hasObservers());
        assertTrue(ps.hasObservers());
        to.dispose();
        assertFalse(ss.hasObservers());
        assertFalse(ps.hasObservers());
    }

    @Test
    public void errorMain() {
        SingleSubject<Integer> ss = SingleSubject.create();
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ss.flatMapObservable(Functions.justFunction(ps)).test();
        assertTrue(ss.hasObservers());
        assertFalse(ps.hasObservers());
        ss.onError(new TestException());
        assertFalse(ss.hasObservers());
        assertFalse(ps.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void errorOther() {
        SingleSubject<Integer> ss = SingleSubject.create();
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ss.flatMapObservable(Functions.justFunction(ps)).test();
        assertTrue(ss.hasObservers());
        assertFalse(ps.hasObservers());
        ss.onSuccess(1);
        assertFalse(ss.hasObservers());
        assertTrue(ps.hasObservers());
        ps.onError(new TestException());
        assertFalse(ss.hasObservers());
        assertFalse(ps.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void mapperCrash() {
        Single.just(1).flatMapObservable(new Function<Integer, ObservableSource<? extends Object>>() {

            @Override
            public ObservableSource<? extends Object> apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void isDisposed() {
        TestHelper.checkDisposed(Single.never().flatMapObservable(Functions.justFunction(Observable.never())));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleFlatMapObservableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelMain() throws java.lang.Throwable {
            this.payloads.cancelMain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOther() throws java.lang.Throwable {
            this.payloads.cancelOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorMain() throws java.lang.Throwable {
            this.payloads.errorMain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorOther() throws java.lang.Throwable {
            this.payloads.errorOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperCrash() throws java.lang.Throwable {
            this.payloads.mapperCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposed() throws java.lang.Throwable {
            this.payloads.isDisposed.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapObservableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapObservableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapObservableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapObservableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleFlatMapObservableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlatMapObservableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleFlatMapObservableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleFlatMapObservableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement cancelMain;

            public org.junit.runners.model.Statement cancelOther;

            public org.junit.runners.model.Statement errorMain;

            public org.junit.runners.model.Statement errorOther;

            public org.junit.runners.model.Statement mapperCrash;

            public org.junit.runners.model.Statement isDisposed;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.cancelMain = _ClassStatement.forPayload(SingleFlatMapObservableTest::cancelMain, "cancelMain", this);
            this.payloads.cancelOther = _ClassStatement.forPayload(SingleFlatMapObservableTest::cancelOther, "cancelOther", this);
            this.payloads.errorMain = _ClassStatement.forPayload(SingleFlatMapObservableTest::errorMain, "errorMain", this);
            this.payloads.errorOther = _ClassStatement.forPayload(SingleFlatMapObservableTest::errorOther, "errorOther", this);
            this.payloads.mapperCrash = _ClassStatement.forPayload(SingleFlatMapObservableTest::mapperCrash, "mapperCrash", this);
            this.payloads.isDisposed = _ClassStatement.forPayload(SingleFlatMapObservableTest::isDisposed, "isDisposed", this);
        }
    }
}
