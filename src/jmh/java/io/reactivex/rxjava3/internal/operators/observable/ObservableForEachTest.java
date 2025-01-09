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
package io.reactivex.rxjava3.internal.operators.observable;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableForEachTest extends RxJavaTest {

    @Test
    public void forEachWile() {
        final List<Object> list = new ArrayList<>();
        Observable.range(1, 5).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        }).forEachWhile(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v < 3;
            }
        });
        assertEquals(Arrays.asList(1, 2, 3), list);
    }

    @Test
    public void forEachWileWithError() {
        final List<Object> list = new ArrayList<>();
        Observable.range(1, 5).concatWith(Observable.<Integer>error(new TestException())).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        }).forEachWhile(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return true;
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                list.add(100);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 100), list);
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceObservable(new Function<Observable<Integer>, Object>() {

            @Override
            public Object apply(Observable<Integer> f) throws Exception {
                return f.forEachWhile(Functions.alwaysTrue());
            }
        }, false, 1, 1, (Object[]) null);
    }

    @Test
    public void dispose() {
        PublishSubject<Integer> ps = PublishSubject.create();
        Disposable d = ps.forEachWhile(Functions.alwaysTrue());
        assertFalse(d.isDisposed());
        d.dispose();
        assertTrue(d.isDisposed());
    }

    @Test
    public void whilePredicateThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.just(1).forEachWhile(new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) throws Exception {
                    throw new TestException();
                }
            });
            TestHelper.assertError(errors, 0, OnErrorNotImplementedException.class);
            Throwable c = errors.get(0).getCause();
            assertTrue("" + c, c instanceof TestException);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void whileErrorThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.<Integer>error(new TestException("Outer")).forEachWhile(Functions.alwaysTrue(), new Consumer<Throwable>() {

                @Override
                public void accept(Throwable v) throws Exception {
                    throw new TestException("Inner");
                }
            });
            TestHelper.assertError(errors, 0, CompositeException.class);
            List<Throwable> ce = TestHelper.compositeList(errors.get(0));
            TestHelper.assertError(ce, 0, TestException.class, "Outer");
            TestHelper.assertError(ce, 1, TestException.class, "Inner");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void whileCompleteThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.just(1).forEachWhile(Functions.alwaysTrue(), Functions.emptyConsumer(), new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            });
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableForEachTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_forEachWile() throws java.lang.Throwable {
            this.payloads.forEachWile.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_forEachWileWithError() throws java.lang.Throwable {
            this.payloads.forEachWileWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_whilePredicateThrows() throws java.lang.Throwable {
            this.payloads.whilePredicateThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_whileErrorThrows() throws java.lang.Throwable {
            this.payloads.whileErrorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_whileCompleteThrows() throws java.lang.Throwable {
            this.payloads.whileCompleteThrows.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableForEachTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableForEachTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableForEachTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableForEachTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableForEachTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableForEachTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableForEachTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableForEachTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement forEachWile;

            public org.junit.runners.model.Statement forEachWileWithError;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement whilePredicateThrows;

            public org.junit.runners.model.Statement whileErrorThrows;

            public org.junit.runners.model.Statement whileCompleteThrows;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.forEachWile = _ClassStatement.forPayload(ObservableForEachTest::forEachWile, "forEachWile", this);
            this.payloads.forEachWileWithError = _ClassStatement.forPayload(ObservableForEachTest::forEachWileWithError, "forEachWileWithError", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableForEachTest::badSource, "badSource", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableForEachTest::dispose, "dispose", this);
            this.payloads.whilePredicateThrows = _ClassStatement.forPayload(ObservableForEachTest::whilePredicateThrows, "whilePredicateThrows", this);
            this.payloads.whileErrorThrows = _ClassStatement.forPayload(ObservableForEachTest::whileErrorThrows, "whileErrorThrows", this);
            this.payloads.whileCompleteThrows = _ClassStatement.forPayload(ObservableForEachTest::whileCompleteThrows, "whileCompleteThrows", this);
        }
    }
}
