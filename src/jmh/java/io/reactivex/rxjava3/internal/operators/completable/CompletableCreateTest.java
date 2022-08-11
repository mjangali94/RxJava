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
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Cancellable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableCreateTest extends RxJavaTest {

    @Test
    public void basic() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable d = Disposable.empty();
            Completable.create(new CompletableOnSubscribe() {

                @Override
                public void subscribe(CompletableEmitter e) throws Exception {
                    e.setDisposable(d);
                    e.onComplete();
                    e.onError(new TestException());
                    e.onComplete();
                }
            }).test().assertResult();
            assertTrue(d.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void basicWithCancellable() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable d1 = Disposable.empty();
            final Disposable d2 = Disposable.empty();
            Completable.create(new CompletableOnSubscribe() {

                @Override
                public void subscribe(CompletableEmitter e) throws Exception {
                    e.setDisposable(d1);
                    e.setCancellable(new Cancellable() {

                        @Override
                        public void cancel() throws Exception {
                            d2.dispose();
                        }
                    });
                    e.onComplete();
                    e.onError(new TestException());
                    e.onComplete();
                }
            }).test().assertResult();
            assertTrue(d1.isDisposed());
            assertTrue(d2.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void basicWithError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable d = Disposable.empty();
            Completable.create(new CompletableOnSubscribe() {

                @Override
                public void subscribe(CompletableEmitter e) throws Exception {
                    e.setDisposable(d);
                    e.onError(new TestException());
                    e.onComplete();
                    e.onError(new TestException("second"));
                }
            }).test().assertFailure(TestException.class);
            assertTrue(d.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void callbackThrows() {
        Completable.create(new CompletableOnSubscribe() {

            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void onErrorNull() {
        Completable.create(new CompletableOnSubscribe() {

            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                e.onError(null);
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Completable.create(new CompletableOnSubscribe() {

            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                e.onComplete();
            }
        }));
    }

    @Test
    public void onErrorThrows() {
        Completable.create(new CompletableOnSubscribe() {

            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                Disposable d = Disposable.empty();
                e.setDisposable(d);
                try {
                    e.onError(new IOException());
                    fail("Should have thrown");
                } catch (TestException ex) {
                // expected
                }
                assertTrue(d.isDisposed());
                assertTrue(e.isDisposed());
            }
        }).subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                throw new TestException();
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void onCompleteThrows() {
        Completable.create(new CompletableOnSubscribe() {

            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                Disposable d = Disposable.empty();
                e.setDisposable(d);
                try {
                    e.onComplete();
                    fail("Should have thrown");
                } catch (TestException ex) {
                // expected
                }
                assertTrue(d.isDisposed());
                assertTrue(e.isDisposed());
            }
        }).subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                throw new TestException();
            }
        });
    }

    @Test
    public void onErrorThrows2() {
        Completable.create(new CompletableOnSubscribe() {

            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                try {
                    e.onError(new IOException());
                    fail("Should have thrown");
                } catch (TestException ex) {
                // expected
                }
                assertTrue(e.isDisposed());
            }
        }).subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                throw new TestException();
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void onCompleteThrows2() {
        Completable.create(new CompletableOnSubscribe() {

            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                try {
                    e.onComplete();
                    fail("Should have thrown");
                } catch (TestException ex) {
                // expected
                }
                assertTrue(e.isDisposed());
            }
        }).subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                throw new TestException();
            }
        });
    }

    @Test
    public void tryOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Boolean[] response = { null };
            Completable.create(new CompletableOnSubscribe() {

                @Override
                public void subscribe(CompletableEmitter e) throws Exception {
                    e.onComplete();
                    response[0] = e.tryOnError(new TestException());
                }
            }).test().assertResult();
            assertFalse(response[0]);
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void emitterHasToString() {
        Completable.create(new CompletableOnSubscribe() {

            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                assertTrue(emitter.toString().contains(CompletableCreate.Emitter.class.getSimpleName()));
            }
        }).test().assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private CompletableCreateTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basic() throws java.lang.Throwable {
            this.payloads.basic.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicWithCancellable() throws java.lang.Throwable {
            this.payloads.basicWithCancellable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicWithError() throws java.lang.Throwable {
            this.payloads.basicWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_callbackThrows() throws java.lang.Throwable {
            this.payloads.callbackThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorNull() throws java.lang.Throwable {
            this.payloads.onErrorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorThrows() throws java.lang.Throwable {
            this.payloads.onErrorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteThrows() throws java.lang.Throwable {
            this.payloads.onCompleteThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorThrows2() throws java.lang.Throwable {
            this.payloads.onErrorThrows2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteThrows2() throws java.lang.Throwable {
            this.payloads.onCompleteThrows2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryOnError() throws java.lang.Throwable {
            this.payloads.tryOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitterHasToString() throws java.lang.Throwable {
            this.payloads.emitterHasToString.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableCreateTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableCreateTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableCreateTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableCreateTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableCreateTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableCreateTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableCreateTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableCreateTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement basic;

            public org.junit.runners.model.Statement basicWithCancellable;

            public org.junit.runners.model.Statement basicWithError;

            public org.junit.runners.model.Statement callbackThrows;

            public org.junit.runners.model.Statement onErrorNull;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement onErrorThrows;

            public org.junit.runners.model.Statement onCompleteThrows;

            public org.junit.runners.model.Statement onErrorThrows2;

            public org.junit.runners.model.Statement onCompleteThrows2;

            public org.junit.runners.model.Statement tryOnError;

            public org.junit.runners.model.Statement emitterHasToString;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.basic = _ClassStatement.forPayload(CompletableCreateTest::basic, "basic", this);
            this.payloads.basicWithCancellable = _ClassStatement.forPayload(CompletableCreateTest::basicWithCancellable, "basicWithCancellable", this);
            this.payloads.basicWithError = _ClassStatement.forPayload(CompletableCreateTest::basicWithError, "basicWithError", this);
            this.payloads.callbackThrows = _ClassStatement.forPayload(CompletableCreateTest::callbackThrows, "callbackThrows", this);
            this.payloads.onErrorNull = _ClassStatement.forPayload(CompletableCreateTest::onErrorNull, "onErrorNull", this);
            this.payloads.dispose = _ClassStatement.forPayload(CompletableCreateTest::dispose, "dispose", this);
            this.payloads.onErrorThrows = _ClassStatement.forPayload(CompletableCreateTest::onErrorThrows, "onErrorThrows", this);
            this.payloads.onCompleteThrows = _ClassStatement.forPayload(CompletableCreateTest::onCompleteThrows, "onCompleteThrows", this);
            this.payloads.onErrorThrows2 = _ClassStatement.forPayload(CompletableCreateTest::onErrorThrows2, "onErrorThrows2", this);
            this.payloads.onCompleteThrows2 = _ClassStatement.forPayload(CompletableCreateTest::onCompleteThrows2, "onCompleteThrows2", this);
            this.payloads.tryOnError = _ClassStatement.forPayload(CompletableCreateTest::tryOnError, "tryOnError", this);
            this.payloads.emitterHasToString = _ClassStatement.forPayload(CompletableCreateTest::emitterHasToString, "emitterHasToString", this);
        }
    }
}
