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

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeCallbackObserverTest extends RxJavaTest {

    @Test
    public void dispose() {
        MaybeCallbackObserver<Object> mo = new MaybeCallbackObserver<>(Functions.emptyConsumer(), Functions.emptyConsumer(), Functions.EMPTY_ACTION);
        Disposable d = Disposable.empty();
        mo.onSubscribe(d);
        assertFalse(mo.isDisposed());
        mo.dispose();
        assertTrue(mo.isDisposed());
        assertTrue(d.isDisposed());
    }

    @Test
    public void onSuccessCrashes() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            MaybeCallbackObserver<Object> mo = new MaybeCallbackObserver<>(new Consumer<Object>() {

                @Override
                public void accept(Object v) throws Exception {
                    throw new TestException();
                }
            }, Functions.emptyConsumer(), Functions.EMPTY_ACTION);
            mo.onSubscribe(Disposable.empty());
            mo.onSuccess(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onErrorCrashes() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            MaybeCallbackObserver<Object> mo = new MaybeCallbackObserver<>(Functions.emptyConsumer(), new Consumer<Object>() {

                @Override
                public void accept(Object v) throws Exception {
                    throw new TestException("Inner");
                }
            }, Functions.EMPTY_ACTION);
            mo.onSubscribe(Disposable.empty());
            mo.onError(new TestException("Outer"));
            TestHelper.assertError(errors, 0, CompositeException.class);
            List<Throwable> ce = TestHelper.compositeList(errors.get(0));
            TestHelper.assertError(ce, 0, TestException.class, "Outer");
            TestHelper.assertError(ce, 1, TestException.class, "Inner");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onCompleteCrashes() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            MaybeCallbackObserver<Object> mo = new MaybeCallbackObserver<>(Functions.emptyConsumer(), Functions.emptyConsumer(), new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            });
            mo.onSubscribe(Disposable.empty());
            mo.onComplete();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onErrorMissingShouldReportNoCustomOnError() {
        MaybeCallbackObserver<Integer> o = new MaybeCallbackObserver<>(Functions.<Integer>emptyConsumer(), Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION);
        assertFalse(o.hasCustomOnError());
    }

    @Test
    public void customOnErrorShouldReportCustomOnError() {
        MaybeCallbackObserver<Integer> o = new MaybeCallbackObserver<>(Functions.<Integer>emptyConsumer(), Functions.<Throwable>emptyConsumer(), Functions.EMPTY_ACTION);
        assertTrue(o.hasCustomOnError());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeCallbackObserverTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessCrashes() throws java.lang.Throwable {
            this.payloads.onSuccessCrashes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCrashes() throws java.lang.Throwable {
            this.payloads.onErrorCrashes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteCrashes() throws java.lang.Throwable {
            this.payloads.onCompleteCrashes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorMissingShouldReportNoCustomOnError() throws java.lang.Throwable {
            this.payloads.onErrorMissingShouldReportNoCustomOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customOnErrorShouldReportCustomOnError() throws java.lang.Throwable {
            this.payloads.customOnErrorShouldReportCustomOnError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeCallbackObserverTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeCallbackObserverTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeCallbackObserverTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeCallbackObserverTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeCallbackObserverTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeCallbackObserverTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeCallbackObserverTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeCallbackObserverTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement onSuccessCrashes;

            public org.junit.runners.model.Statement onErrorCrashes;

            public org.junit.runners.model.Statement onCompleteCrashes;

            public org.junit.runners.model.Statement onErrorMissingShouldReportNoCustomOnError;

            public org.junit.runners.model.Statement customOnErrorShouldReportCustomOnError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.dispose = _ClassStatement.forPayload(MaybeCallbackObserverTest::dispose, "dispose", this);
            this.payloads.onSuccessCrashes = _ClassStatement.forPayload(MaybeCallbackObserverTest::onSuccessCrashes, "onSuccessCrashes", this);
            this.payloads.onErrorCrashes = _ClassStatement.forPayload(MaybeCallbackObserverTest::onErrorCrashes, "onErrorCrashes", this);
            this.payloads.onCompleteCrashes = _ClassStatement.forPayload(MaybeCallbackObserverTest::onCompleteCrashes, "onCompleteCrashes", this);
            this.payloads.onErrorMissingShouldReportNoCustomOnError = _ClassStatement.forPayload(MaybeCallbackObserverTest::onErrorMissingShouldReportNoCustomOnError, "onErrorMissingShouldReportNoCustomOnError", this);
            this.payloads.customOnErrorShouldReportCustomOnError = _ClassStatement.forPayload(MaybeCallbackObserverTest::customOnErrorShouldReportCustomOnError, "customOnErrorShouldReportCustomOnError", this);
        }
    }
}
