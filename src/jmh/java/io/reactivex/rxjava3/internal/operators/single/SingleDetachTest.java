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
package io.reactivex.rxjava3.internal.operators.single;

import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.lang.ref.WeakReference;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleDetachTest extends RxJavaTest {

    @Test
    public void doubleSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingle(new Function<Single<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Single<Object> m) throws Exception {
                return m.onTerminateDetach();
            }
        });
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().singleOrError().onTerminateDetach());
    }

    @Test
    public void onError() {
        Single.error(new TestException()).onTerminateDetach().test().assertFailure(TestException.class);
    }

    @Test
    public void onSuccess() {
        Single.just(1).onTerminateDetach().test().assertResult(1);
    }

    @Test
    public void cancelDetaches() throws Exception {
        Disposable d = Disposable.empty();
        final WeakReference<Disposable> wr = new WeakReference<>(d);
        TestObserver<Object> to = new Single<Object>() {

            @Override
            protected void subscribeActual(SingleObserver<? super Object> observer) {
                observer.onSubscribe(wr.get());
            }
        }.onTerminateDetach().test();
        d = null;
        to.dispose();
        System.gc();
        Thread.sleep(200);
        to.assertEmpty();
        assertNull(wr.get());
    }

    @Test
    public void errorDetaches() throws Exception {
        Disposable d = Disposable.empty();
        final WeakReference<Disposable> wr = new WeakReference<>(d);
        TestObserver<Integer> to = new Single<Integer>() {

            @Override
            protected void subscribeActual(SingleObserver<? super Integer> observer) {
                observer.onSubscribe(wr.get());
                observer.onError(new TestException());
                observer.onError(new IOException());
            }
        }.onTerminateDetach().test();
        d = null;
        System.gc();
        Thread.sleep(200);
        to.assertFailure(TestException.class);
        assertNull(wr.get());
    }

    @Test
    public void successDetaches() throws Exception {
        Disposable d = Disposable.empty();
        final WeakReference<Disposable> wr = new WeakReference<>(d);
        TestObserver<Integer> to = new Single<Integer>() {

            @Override
            protected void subscribeActual(SingleObserver<? super Integer> observer) {
                observer.onSubscribe(wr.get());
                observer.onSuccess(1);
                observer.onSuccess(2);
            }
        }.onTerminateDetach().test();
        d = null;
        System.gc();
        Thread.sleep(200);
        to.assertResult(1);
        assertNull(wr.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleDetachTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleSubscribe() throws java.lang.Throwable {
            this.payloads.doubleSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onError() throws java.lang.Throwable {
            this.payloads.onError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccess() throws java.lang.Throwable {
            this.payloads.onSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelDetaches() throws java.lang.Throwable {
            this.payloads.cancelDetaches.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDetaches() throws java.lang.Throwable {
            this.payloads.errorDetaches.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successDetaches() throws java.lang.Throwable {
            this.payloads.successDetaches.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDetachTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDetachTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDetachTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDetachTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleDetachTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDetachTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleDetachTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleDetachTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement doubleSubscribe;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement onError;

            public org.junit.runners.model.Statement onSuccess;

            public org.junit.runners.model.Statement cancelDetaches;

            public org.junit.runners.model.Statement errorDetaches;

            public org.junit.runners.model.Statement successDetaches;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.doubleSubscribe = _ClassStatement.forPayload(SingleDetachTest::doubleSubscribe, "doubleSubscribe", this);
            this.payloads.dispose = _ClassStatement.forPayload(SingleDetachTest::dispose, "dispose", this);
            this.payloads.onError = _ClassStatement.forPayload(SingleDetachTest::onError, "onError", this);
            this.payloads.onSuccess = _ClassStatement.forPayload(SingleDetachTest::onSuccess, "onSuccess", this);
            this.payloads.cancelDetaches = _ClassStatement.forPayload(SingleDetachTest::cancelDetaches, "cancelDetaches", this);
            this.payloads.errorDetaches = _ClassStatement.forPayload(SingleDetachTest::errorDetaches, "errorDetaches", this);
            this.payloads.successDetaches = _ClassStatement.forPayload(SingleDetachTest::successDetaches, "successDetaches", this);
        }
    }
}
