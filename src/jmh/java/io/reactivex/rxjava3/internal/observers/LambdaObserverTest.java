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
package io.reactivex.rxjava3.internal.observers;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class LambdaObserverTest extends RxJavaTest {

    @Test
    public void onSubscribeThrows() {
        final List<Object> received = new ArrayList<>();
        LambdaObserver<Object> o = new LambdaObserver<>(new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                received.add(v);
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                received.add(e);
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
                received.add(100);
            }
        }, new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                throw new TestException();
            }
        });
        assertFalse(o.isDisposed());
        Observable.just(1).subscribe(o);
        assertTrue(received.toString(), received.get(0) instanceof TestException);
        assertEquals(received.toString(), 1, received.size());
        assertTrue(o.isDisposed());
    }

    @Test
    public void onNextThrows() {
        final List<Object> received = new ArrayList<>();
        LambdaObserver<Object> o = new LambdaObserver<>(new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                throw new TestException();
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                received.add(e);
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
                received.add(100);
            }
        }, new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
            }
        });
        assertFalse(o.isDisposed());
        Observable.just(1).subscribe(o);
        assertTrue(received.toString(), received.get(0) instanceof TestException);
        assertEquals(received.toString(), 1, received.size());
        assertTrue(o.isDisposed());
    }

    @Test
    public void onErrorThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final List<Object> received = new ArrayList<>();
            LambdaObserver<Object> o = new LambdaObserver<>(new Consumer<Object>() {

                @Override
                public void accept(Object v) throws Exception {
                    received.add(v);
                }
            }, new Consumer<Throwable>() {

                @Override
                public void accept(Throwable e) throws Exception {
                    throw new TestException("Inner");
                }
            }, new Action() {

                @Override
                public void run() throws Exception {
                    received.add(100);
                }
            }, new Consumer<Disposable>() {

                @Override
                public void accept(Disposable d) throws Exception {
                }
            });
            assertFalse(o.isDisposed());
            Observable.<Integer>error(new TestException("Outer")).subscribe(o);
            assertTrue(received.toString(), received.isEmpty());
            assertTrue(o.isDisposed());
            TestHelper.assertError(errors, 0, CompositeException.class);
            List<Throwable> ce = TestHelper.compositeList(errors.get(0));
            TestHelper.assertError(ce, 0, TestException.class, "Outer");
            TestHelper.assertError(ce, 1, TestException.class, "Inner");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onCompleteThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final List<Object> received = new ArrayList<>();
            LambdaObserver<Object> o = new LambdaObserver<>(new Consumer<Object>() {

                @Override
                public void accept(Object v) throws Exception {
                    received.add(v);
                }
            }, new Consumer<Throwable>() {

                @Override
                public void accept(Throwable e) throws Exception {
                    received.add(e);
                }
            }, new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }, new Consumer<Disposable>() {

                @Override
                public void accept(Disposable d) throws Exception {
                }
            });
            assertFalse(o.isDisposed());
            Observable.<Integer>empty().subscribe(o);
            assertTrue(received.toString(), received.isEmpty());
            assertTrue(o.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSourceOnSubscribe() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable<Integer> source = new Observable<Integer>() {

                @Override
                public void subscribeActual(Observer<? super Integer> observer) {
                    Disposable d1 = Disposable.empty();
                    observer.onSubscribe(d1);
                    Disposable d2 = Disposable.empty();
                    observer.onSubscribe(d2);
                    assertFalse(d1.isDisposed());
                    assertTrue(d2.isDisposed());
                    observer.onNext(1);
                    observer.onComplete();
                }
            };
            final List<Object> received = new ArrayList<>();
            LambdaObserver<Object> o = new LambdaObserver<>(new Consumer<Object>() {

                @Override
                public void accept(Object v) throws Exception {
                    received.add(v);
                }
            }, new Consumer<Throwable>() {

                @Override
                public void accept(Throwable e) throws Exception {
                    received.add(e);
                }
            }, new Action() {

                @Override
                public void run() throws Exception {
                    received.add(100);
                }
            }, new Consumer<Disposable>() {

                @Override
                public void accept(Disposable d) throws Exception {
                }
            });
            source.subscribe(o);
            assertEquals(Arrays.asList(1, 100), received);
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSourceEmitAfterDone() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable<Integer> source = new Observable<Integer>() {

                @Override
                public void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onComplete();
                    observer.onNext(2);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            };
            final List<Object> received = new ArrayList<>();
            LambdaObserver<Object> o = new LambdaObserver<>(new Consumer<Object>() {

                @Override
                public void accept(Object v) throws Exception {
                    received.add(v);
                }
            }, new Consumer<Throwable>() {

                @Override
                public void accept(Throwable e) throws Exception {
                    received.add(e);
                }
            }, new Action() {

                @Override
                public void run() throws Exception {
                    received.add(100);
                }
            }, new Consumer<Disposable>() {

                @Override
                public void accept(Disposable d) throws Exception {
                }
            });
            source.subscribe(o);
            assertEquals(Arrays.asList(1, 100), received);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onNextThrowsCancelsUpstream() {
        PublishSubject<Integer> ps = PublishSubject.create();
        final List<Throwable> errors = new ArrayList<>();
        ps.subscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                throw new TestException();
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                errors.add(e);
            }
        });
        assertTrue("No observers?!", ps.hasObservers());
        assertTrue("Has errors already?!", errors.isEmpty());
        ps.onNext(1);
        assertFalse("Has observers?!", ps.hasObservers());
        assertFalse("No errors?!", errors.isEmpty());
        assertTrue(errors.toString(), errors.get(0) instanceof TestException);
    }

    @Test
    public void onSubscribeThrowsCancelsUpstream() {
        PublishSubject<Integer> ps = PublishSubject.create();
        final List<Throwable> errors = new ArrayList<>();
        ps.subscribe(new LambdaObserver<>(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                errors.add(e);
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
            }
        }, new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                throw new TestException();
            }
        }));
        assertFalse("Has observers?!", ps.hasObservers());
        assertFalse("No errors?!", errors.isEmpty());
        assertTrue(errors.toString(), errors.get(0) instanceof TestException);
    }

    @Test
    public void onErrorMissingShouldReportNoCustomOnError() {
        LambdaObserver<Integer> o = new LambdaObserver<>(Functions.<Integer>emptyConsumer(), Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION, Functions.<Disposable>emptyConsumer());
        assertFalse(o.hasCustomOnError());
    }

    @Test
    public void customOnErrorShouldReportCustomOnError() {
        LambdaObserver<Integer> o = new LambdaObserver<>(Functions.<Integer>emptyConsumer(), Functions.<Throwable>emptyConsumer(), Functions.EMPTY_ACTION, Functions.<Disposable>emptyConsumer());
        assertTrue(o.hasCustomOnError());
    }

    @Test
    public void disposedObserverShouldReportErrorOnGlobalErrorHandler() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final List<Throwable> observerErrors = Collections.synchronizedList(new ArrayList<>());
            LambdaObserver<Integer> o = new LambdaObserver<>(Functions.<Integer>emptyConsumer(), new Consumer<Throwable>() {

                @Override
                public void accept(Throwable t) {
                    observerErrors.add(t);
                }
            }, Functions.EMPTY_ACTION, Functions.<Disposable>emptyConsumer());
            o.dispose();
            o.onError(new IOException());
            o.onError(new IOException());
            assertTrue(observerErrors.isEmpty());
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
            TestHelper.assertUndeliverable(errors, 1, IOException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private LambdaObserverTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeThrows() throws java.lang.Throwable {
            this.payloads.onSubscribeThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextThrows() throws java.lang.Throwable {
            this.payloads.onNextThrows.evaluate();
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
        public void benchmark_badSourceOnSubscribe() throws java.lang.Throwable {
            this.payloads.badSourceOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceEmitAfterDone() throws java.lang.Throwable {
            this.payloads.badSourceEmitAfterDone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextThrowsCancelsUpstream() throws java.lang.Throwable {
            this.payloads.onNextThrowsCancelsUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeThrowsCancelsUpstream() throws java.lang.Throwable {
            this.payloads.onSubscribeThrowsCancelsUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorMissingShouldReportNoCustomOnError() throws java.lang.Throwable {
            this.payloads.onErrorMissingShouldReportNoCustomOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customOnErrorShouldReportCustomOnError() throws java.lang.Throwable {
            this.payloads.customOnErrorShouldReportCustomOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedObserverShouldReportErrorOnGlobalErrorHandler() throws java.lang.Throwable {
            this.payloads.disposedObserverShouldReportErrorOnGlobalErrorHandler.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<LambdaObserverTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<LambdaObserverTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<LambdaObserverTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<LambdaObserverTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new LambdaObserverTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<LambdaObserverTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(LambdaObserverTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(LambdaObserverTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement onSubscribeThrows;

            public org.junit.runners.model.Statement onNextThrows;

            public org.junit.runners.model.Statement onErrorThrows;

            public org.junit.runners.model.Statement onCompleteThrows;

            public org.junit.runners.model.Statement badSourceOnSubscribe;

            public org.junit.runners.model.Statement badSourceEmitAfterDone;

            public org.junit.runners.model.Statement onNextThrowsCancelsUpstream;

            public org.junit.runners.model.Statement onSubscribeThrowsCancelsUpstream;

            public org.junit.runners.model.Statement onErrorMissingShouldReportNoCustomOnError;

            public org.junit.runners.model.Statement customOnErrorShouldReportCustomOnError;

            public org.junit.runners.model.Statement disposedObserverShouldReportErrorOnGlobalErrorHandler;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.onSubscribeThrows = _ClassStatement.forPayload(LambdaObserverTest::onSubscribeThrows, "onSubscribeThrows", this);
            this.payloads.onNextThrows = _ClassStatement.forPayload(LambdaObserverTest::onNextThrows, "onNextThrows", this);
            this.payloads.onErrorThrows = _ClassStatement.forPayload(LambdaObserverTest::onErrorThrows, "onErrorThrows", this);
            this.payloads.onCompleteThrows = _ClassStatement.forPayload(LambdaObserverTest::onCompleteThrows, "onCompleteThrows", this);
            this.payloads.badSourceOnSubscribe = _ClassStatement.forPayload(LambdaObserverTest::badSourceOnSubscribe, "badSourceOnSubscribe", this);
            this.payloads.badSourceEmitAfterDone = _ClassStatement.forPayload(LambdaObserverTest::badSourceEmitAfterDone, "badSourceEmitAfterDone", this);
            this.payloads.onNextThrowsCancelsUpstream = _ClassStatement.forPayload(LambdaObserverTest::onNextThrowsCancelsUpstream, "onNextThrowsCancelsUpstream", this);
            this.payloads.onSubscribeThrowsCancelsUpstream = _ClassStatement.forPayload(LambdaObserverTest::onSubscribeThrowsCancelsUpstream, "onSubscribeThrowsCancelsUpstream", this);
            this.payloads.onErrorMissingShouldReportNoCustomOnError = _ClassStatement.forPayload(LambdaObserverTest::onErrorMissingShouldReportNoCustomOnError, "onErrorMissingShouldReportNoCustomOnError", this);
            this.payloads.customOnErrorShouldReportCustomOnError = _ClassStatement.forPayload(LambdaObserverTest::customOnErrorShouldReportCustomOnError, "customOnErrorShouldReportCustomOnError", this);
            this.payloads.disposedObserverShouldReportErrorOnGlobalErrorHandler = _ClassStatement.forPayload(LambdaObserverTest::disposedObserverShouldReportErrorOnGlobalErrorHandler, "disposedObserverShouldReportErrorOnGlobalErrorHandler", this);
        }
    }
}
