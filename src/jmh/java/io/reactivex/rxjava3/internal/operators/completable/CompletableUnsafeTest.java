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

import static org.junit.Assert.*;
import java.util.List;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableUnsafeTest extends RxJavaTest {

    @Test(expected = IllegalArgumentException.class)
    public void unsafeCreateRejectsCompletable() {
        Completable.unsafeCreate(Completable.complete());
    }

    @Test
    public void wrapAlreadyCompletable() {
        assertSame(Completable.complete(), Completable.wrap(Completable.complete()));
    }

    @Test
    public void wrapCustomCompletable() {
        Completable.wrap(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onComplete();
            }
        }).test().assertResult();
    }

    @Test(expected = NullPointerException.class)
    public void unsafeCreateThrowsNPE() {
        Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                throw new NullPointerException();
            }
        }).test();
    }

    @Test
    public void unsafeCreateThrowsIAE() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Completable.unsafeCreate(new CompletableSource() {

                @Override
                public void subscribe(CompletableObserver observer) {
                    throw new IllegalArgumentException();
                }
            }).test();
            fail("Should have thrown!");
        } catch (NullPointerException ex) {
            if (!(ex.getCause() instanceof IllegalArgumentException)) {
                fail(ex.toString() + ": should have thrown NPA(IAE)");
            }
            TestHelper.assertError(errors, 0, IllegalArgumentException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private CompletableUnsafeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsafeCreateRejectsCompletable() throws java.lang.Throwable {
            this.payloads.unsafeCreateRejectsCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_wrapAlreadyCompletable() throws java.lang.Throwable {
            this.payloads.wrapAlreadyCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_wrapCustomCompletable() throws java.lang.Throwable {
            this.payloads.wrapCustomCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsafeCreateThrowsNPE() throws java.lang.Throwable {
            this.payloads.unsafeCreateThrowsNPE.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsafeCreateThrowsIAE() throws java.lang.Throwable {
            this.payloads.unsafeCreateThrowsIAE.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableUnsafeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableUnsafeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableUnsafeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableUnsafeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableUnsafeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableUnsafeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableUnsafeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableUnsafeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement unsafeCreateRejectsCompletable;

            public org.junit.runners.model.Statement wrapAlreadyCompletable;

            public org.junit.runners.model.Statement wrapCustomCompletable;

            public org.junit.runners.model.Statement unsafeCreateThrowsNPE;

            public org.junit.runners.model.Statement unsafeCreateThrowsIAE;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.unsafeCreateRejectsCompletable = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableUnsafeTest::unsafeCreateRejectsCompletable, java.lang.IllegalArgumentException.class), "unsafeCreateRejectsCompletable", this);
            this.payloads.wrapAlreadyCompletable = _ClassStatement.forPayload(CompletableUnsafeTest::wrapAlreadyCompletable, "wrapAlreadyCompletable", this);
            this.payloads.wrapCustomCompletable = _ClassStatement.forPayload(CompletableUnsafeTest::wrapCustomCompletable, "wrapCustomCompletable", this);
            this.payloads.unsafeCreateThrowsNPE = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableUnsafeTest::unsafeCreateThrowsNPE, java.lang.NullPointerException.class), "unsafeCreateThrowsNPE", this);
            this.payloads.unsafeCreateThrowsIAE = _ClassStatement.forPayload(CompletableUnsafeTest::unsafeCreateThrowsIAE, "unsafeCreateThrowsIAE", this);
        }
    }
}
