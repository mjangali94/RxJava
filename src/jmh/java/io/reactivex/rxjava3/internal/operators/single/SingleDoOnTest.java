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

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class SingleDoOnTest extends RxJavaTest {

    @Test
    public void doOnDispose() {
        final int[] count = { 0 };
        Single.never().doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                count[0]++;
            }
        }).test(true);
        assertEquals(1, count[0]);
    }

    @Test
    public void doOnError() {
        final Object[] event = { null };
        Single.error(new TestException()).doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                event[0] = e;
            }
        }).test();
        assertTrue(event[0].toString(), event[0] instanceof TestException);
    }

    @Test
    public void doOnSubscribe() {
        final int[] count = { 0 };
        Single.never().doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                count[0]++;
            }
        }).test();
        assertEquals(1, count[0]);
    }

    @Test
    public void doOnSuccess() {
        final Object[] event = { null };
        Single.just(1).doOnSuccess(new Consumer<Integer>() {

            @Override
            public void accept(Integer e) throws Exception {
                event[0] = e;
            }
        }).test();
        assertEquals(1, event[0]);
    }

    @Test
    public void doOnSubscribeNormal() {
        final int[] count = { 0 };
        Single.just(1).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                count[0]++;
            }
        }).test().assertResult(1);
        assertEquals(1, count[0]);
    }

    @Test
    public void doOnSubscribeError() {
        final int[] count = { 0 };
        Single.error(new TestException()).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                count[0]++;
            }
        }).test().assertFailure(TestException.class);
        assertEquals(1, count[0]);
    }

    @Test
    public void doOnSubscribeJustCrash() {
        Single.just(1).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void doOnSubscribeErrorCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Single.error(new TestException("Outer")).doOnSubscribe(new Consumer<Disposable>() {

                @Override
                public void accept(Disposable d) throws Exception {
                    throw new TestException("Inner");
                }
            }).to(TestHelper.testConsumer()).assertFailureAndMessage(TestException.class, "Inner");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Outer");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onErrorSuccess() {
        final int[] call = { 0 };
        Single.just(1).doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable v) throws Exception {
                call[0]++;
            }
        }).test().assertResult(1);
        assertEquals(0, call[0]);
    }

    @Test
    public void onErrorCrashes() {
        TestObserverEx<Object> to = Single.error(new TestException("Outer")).doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable v) throws Exception {
                throw new TestException("Inner");
            }
        }).to(TestHelper.testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(errors, 0, TestException.class, "Outer");
        TestHelper.assertError(errors, 1, TestException.class, "Inner");
    }

    @Test
    public void doOnEventThrowsSuccess() {
        Single.just(1).doOnEvent(new BiConsumer<Integer, Throwable>() {

            @Override
            public void accept(Integer v, Throwable e) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void doOnEventThrowsError() {
        TestObserverEx<Integer> to = Single.<Integer>error(new TestException("Main")).doOnEvent(new BiConsumer<Integer, Throwable>() {

            @Override
            public void accept(Integer v, Throwable e) throws Exception {
                throw new TestException("Inner");
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(errors, 0, TestException.class, "Main");
        TestHelper.assertError(errors, 1, TestException.class, "Inner");
    }

    @Test
    public void doOnDisposeDispose() {
        final int[] calls = { 0 };
        TestHelper.checkDisposed(PublishSubject.create().singleOrError().doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                calls[0]++;
            }
        }));
        assertEquals(1, calls[0]);
    }

    @Test
    public void doOnDisposeSuccess() {
        final int[] calls = { 0 };
        Single.just(1).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                calls[0]++;
            }
        }).test().assertResult(1);
        assertEquals(0, calls[0]);
    }

    @Test
    public void doOnDisposeError() {
        final int[] calls = { 0 };
        Single.error(new TestException()).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                calls[0]++;
            }
        }).test().assertFailure(TestException.class);
        assertEquals(0, calls[0]);
    }

    @Test
    public void doOnDisposeDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingle(new Function<Single<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Single<Object> s) throws Exception {
                return s.doOnDispose(Functions.EMPTY_ACTION);
            }
        });
    }

    @Test
    public void doOnDisposeCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            PublishSubject<Integer> ps = PublishSubject.create();
            ps.singleOrError().doOnDispose(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).test().dispose();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doOnSuccessErrors() {
        final int[] call = { 0 };
        Single.error(new TestException()).doOnSuccess(new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                call[0]++;
            }
        }).test().assertFailure(TestException.class);
        assertEquals(0, call[0]);
    }

    @Test
    public void doOnSuccessCrash() {
        Single.just(1).doOnSuccess(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void onSubscribeCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable bs = Disposable.empty();
            new Single<Integer>() {

                @Override
                protected void subscribeActual(SingleObserver<? super Integer> observer) {
                    observer.onSubscribe(bs);
                    observer.onError(new TestException("Second"));
                    observer.onSuccess(1);
                }
            }.doOnSubscribe(new Consumer<Disposable>() {

                @Override
                public void accept(Disposable d) throws Exception {
                    throw new TestException("First");
                }
            }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            assertTrue(bs.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public SingleDoOnTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDispose() throws java.lang.Throwable {
            this.payloads.doOnDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnError() throws java.lang.Throwable {
            this.payloads.doOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribe() throws java.lang.Throwable {
            this.payloads.doOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSuccess() throws java.lang.Throwable {
            this.payloads.doOnSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribeNormal() throws java.lang.Throwable {
            this.payloads.doOnSubscribeNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribeError() throws java.lang.Throwable {
            this.payloads.doOnSubscribeError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribeJustCrash() throws java.lang.Throwable {
            this.payloads.doOnSubscribeJustCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribeErrorCrash() throws java.lang.Throwable {
            this.payloads.doOnSubscribeErrorCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorSuccess() throws java.lang.Throwable {
            this.payloads.onErrorSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCrashes() throws java.lang.Throwable {
            this.payloads.onErrorCrashes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEventThrowsSuccess() throws java.lang.Throwable {
            this.payloads.doOnEventThrowsSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEventThrowsError() throws java.lang.Throwable {
            this.payloads.doOnEventThrowsError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeDispose() throws java.lang.Throwable {
            this.payloads.doOnDisposeDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeSuccess() throws java.lang.Throwable {
            this.payloads.doOnDisposeSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeError() throws java.lang.Throwable {
            this.payloads.doOnDisposeError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doOnDisposeDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeCrash() throws java.lang.Throwable {
            this.payloads.doOnDisposeCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSuccessErrors() throws java.lang.Throwable {
            this.payloads.doOnSuccessErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSuccessCrash() throws java.lang.Throwable {
            this.payloads.doOnSuccessCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeCrash() throws java.lang.Throwable {
            this.payloads.onSubscribeCrash.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoOnTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoOnTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoOnTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoOnTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleDoOnTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDoOnTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleDoOnTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleDoOnTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement doOnDispose;

            public org.junit.runners.model.Statement doOnError;

            public org.junit.runners.model.Statement doOnSubscribe;

            public org.junit.runners.model.Statement doOnSuccess;

            public org.junit.runners.model.Statement doOnSubscribeNormal;

            public org.junit.runners.model.Statement doOnSubscribeError;

            public org.junit.runners.model.Statement doOnSubscribeJustCrash;

            public org.junit.runners.model.Statement doOnSubscribeErrorCrash;

            public org.junit.runners.model.Statement onErrorSuccess;

            public org.junit.runners.model.Statement onErrorCrashes;

            public org.junit.runners.model.Statement doOnEventThrowsSuccess;

            public org.junit.runners.model.Statement doOnEventThrowsError;

            public org.junit.runners.model.Statement doOnDisposeDispose;

            public org.junit.runners.model.Statement doOnDisposeSuccess;

            public org.junit.runners.model.Statement doOnDisposeError;

            public org.junit.runners.model.Statement doOnDisposeDoubleOnSubscribe;

            public org.junit.runners.model.Statement doOnDisposeCrash;

            public org.junit.runners.model.Statement doOnSuccessErrors;

            public org.junit.runners.model.Statement doOnSuccessCrash;

            public org.junit.runners.model.Statement onSubscribeCrash;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.doOnDispose = _ClassStatement.forPayload(SingleDoOnTest::doOnDispose, "doOnDispose", this);
            this.payloads.doOnError = _ClassStatement.forPayload(SingleDoOnTest::doOnError, "doOnError", this);
            this.payloads.doOnSubscribe = _ClassStatement.forPayload(SingleDoOnTest::doOnSubscribe, "doOnSubscribe", this);
            this.payloads.doOnSuccess = _ClassStatement.forPayload(SingleDoOnTest::doOnSuccess, "doOnSuccess", this);
            this.payloads.doOnSubscribeNormal = _ClassStatement.forPayload(SingleDoOnTest::doOnSubscribeNormal, "doOnSubscribeNormal", this);
            this.payloads.doOnSubscribeError = _ClassStatement.forPayload(SingleDoOnTest::doOnSubscribeError, "doOnSubscribeError", this);
            this.payloads.doOnSubscribeJustCrash = _ClassStatement.forPayload(SingleDoOnTest::doOnSubscribeJustCrash, "doOnSubscribeJustCrash", this);
            this.payloads.doOnSubscribeErrorCrash = _ClassStatement.forPayload(SingleDoOnTest::doOnSubscribeErrorCrash, "doOnSubscribeErrorCrash", this);
            this.payloads.onErrorSuccess = _ClassStatement.forPayload(SingleDoOnTest::onErrorSuccess, "onErrorSuccess", this);
            this.payloads.onErrorCrashes = _ClassStatement.forPayload(SingleDoOnTest::onErrorCrashes, "onErrorCrashes", this);
            this.payloads.doOnEventThrowsSuccess = _ClassStatement.forPayload(SingleDoOnTest::doOnEventThrowsSuccess, "doOnEventThrowsSuccess", this);
            this.payloads.doOnEventThrowsError = _ClassStatement.forPayload(SingleDoOnTest::doOnEventThrowsError, "doOnEventThrowsError", this);
            this.payloads.doOnDisposeDispose = _ClassStatement.forPayload(SingleDoOnTest::doOnDisposeDispose, "doOnDisposeDispose", this);
            this.payloads.doOnDisposeSuccess = _ClassStatement.forPayload(SingleDoOnTest::doOnDisposeSuccess, "doOnDisposeSuccess", this);
            this.payloads.doOnDisposeError = _ClassStatement.forPayload(SingleDoOnTest::doOnDisposeError, "doOnDisposeError", this);
            this.payloads.doOnDisposeDoubleOnSubscribe = _ClassStatement.forPayload(SingleDoOnTest::doOnDisposeDoubleOnSubscribe, "doOnDisposeDoubleOnSubscribe", this);
            this.payloads.doOnDisposeCrash = _ClassStatement.forPayload(SingleDoOnTest::doOnDisposeCrash, "doOnDisposeCrash", this);
            this.payloads.doOnSuccessErrors = _ClassStatement.forPayload(SingleDoOnTest::doOnSuccessErrors, "doOnSuccessErrors", this);
            this.payloads.doOnSuccessCrash = _ClassStatement.forPayload(SingleDoOnTest::doOnSuccessCrash, "doOnSuccessCrash", this);
            this.payloads.onSubscribeCrash = _ClassStatement.forPayload(SingleDoOnTest::onSubscribeCrash, "onSubscribeCrash", this);
        }
    }
}
