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
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class MaybeUsingTest extends RxJavaTest {

    @Test
    public void resourceSupplierThrows() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                throw new TestException();
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.just(1);
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void errorEager() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.error(new TestException());
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }, true).test().assertFailure(TestException.class);
    }

    @Test
    public void emptyEager() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.empty();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }, true).test().assertResult();
    }

    @Test
    public void errorNonEager() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.error(new TestException());
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }, false).test().assertFailure(TestException.class);
    }

    @Test
    public void emptyNonEager() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.empty();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }, false).test().assertResult();
    }

    @Test
    public void supplierCrashEager() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                throw new TestException();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }, true).test().assertFailure(TestException.class);
    }

    @Test
    public void supplierCrashNonEager() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                throw new TestException();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }, false).test().assertFailure(TestException.class);
    }

    @Test
    public void supplierAndDisposerCrashEager() {
        TestObserverEx<Integer> to = Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                throw new TestException("Main");
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
                throw new TestException("Disposer");
            }
        }, true).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> list = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(list, 0, TestException.class, "Main");
        TestHelper.assertError(list, 1, TestException.class, "Disposer");
    }

    @Test
    public void supplierAndDisposerCrashNonEager() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Maybe.using(new Supplier<Object>() {

                @Override
                public Object get() throws Exception {
                    return 1;
                }
            }, new Function<Object, MaybeSource<Integer>>() {

                @Override
                public MaybeSource<Integer> apply(Object v) throws Exception {
                    throw new TestException("Main");
                }
            }, new Consumer<Object>() {

                @Override
                public void accept(Object d) throws Exception {
                    throw new TestException("Disposer");
                }
            }, false).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "Main");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Disposer");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void dispose() {
        final int[] call = { 0 };
        TestObserver<Integer> to = Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.never();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
                call[0]++;
            }
        }, false).test();
        to.dispose();
        assertEquals(1, call[0]);
    }

    @Test
    public void disposeCrashes() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Maybe.using(new Supplier<Object>() {

                @Override
                public Object get() throws Exception {
                    return 1;
                }
            }, new Function<Object, MaybeSource<Integer>>() {

                @Override
                public MaybeSource<Integer> apply(Object v) throws Exception {
                    return Maybe.never();
                }
            }, new Consumer<Object>() {

                @Override
                public void accept(Object d) throws Exception {
                    throw new TestException();
                }
            }, false).test();
            to.dispose();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void isDisposed() {
        TestHelper.checkDisposed(Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.never();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }, false));
    }

    @Test
    public void justDisposerCrashes() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.just(1);
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
                throw new TestException("Disposer");
            }
        }, true).test().assertFailure(TestException.class);
    }

    @Test
    public void emptyDisposerCrashes() {
        Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.empty();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
                throw new TestException("Disposer");
            }
        }, true).test().assertFailure(TestException.class);
    }

    @Test
    public void errorDisposerCrash() {
        TestObserverEx<Integer> to = Maybe.using(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new Function<Object, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Object v) throws Exception {
                return Maybe.error(new TestException("Main"));
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
                throw new TestException("Disposer");
            }
        }, true).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> list = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(list, 0, TestException.class, "Main");
        TestHelper.assertError(list, 1, TestException.class, "Disposer");
    }

    @Test
    public void doubleOnSubscribe() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Maybe.using(new Supplier<Object>() {

                @Override
                public Object get() throws Exception {
                    return 1;
                }
            }, new Function<Object, MaybeSource<Integer>>() {

                @Override
                public MaybeSource<Integer> apply(Object v) throws Exception {
                    return Maybe.wrap(new MaybeSource<Integer>() {

                        @Override
                        public void subscribe(MaybeObserver<? super Integer> observer) {
                            Disposable d1 = Disposable.empty();
                            observer.onSubscribe(d1);
                            Disposable d2 = Disposable.empty();
                            observer.onSubscribe(d2);
                            assertFalse(d1.isDisposed());
                            assertTrue(d2.isDisposed());
                        }
                    });
                }
            }, new Consumer<Object>() {

                @Override
                public void accept(Object d) throws Exception {
                }
            }, false).test();
            TestHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void successDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = Maybe.using(new Supplier<Object>() {

                @Override
                public Object get() throws Exception {
                    return 1;
                }
            }, new Function<Object, MaybeSource<Integer>>() {

                @Override
                public MaybeSource<Integer> apply(Object v) throws Exception {
                    return ps.lastElement();
                }
            }, new Consumer<Object>() {

                @Override
                public void accept(Object d) throws Exception {
                }
            }, true).test();
            ps.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    @SuppressUndeliverable
    public void errorDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = Maybe.using(new Supplier<Object>() {

                @Override
                public Object get() throws Exception {
                    return 1;
                }
            }, new Function<Object, MaybeSource<Integer>>() {

                @Override
                public MaybeSource<Integer> apply(Object v) throws Exception {
                    return ps.firstElement();
                }
            }, new Consumer<Object>() {

                @Override
                public void accept(Object d) throws Exception {
                }
            }, true).test();
            final TestException ex = new TestException();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ps.onError(ex);
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void emptyDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = Maybe.using(new Supplier<Object>() {

                @Override
                public Object get() throws Exception {
                    return 1;
                }
            }, new Function<Object, MaybeSource<Integer>>() {

                @Override
                public MaybeSource<Integer> apply(Object v) throws Exception {
                    return ps.firstElement();
                }
            }, new Consumer<Object>() {

                @Override
                public void accept(Object d) throws Exception {
                }
            }, true).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void eagerDisposeResourceThenDisposeUpstream() {
        final StringBuilder sb = new StringBuilder();
        TestObserver<Integer> to = Maybe.using(Functions.justSupplier(1), new Function<Integer, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Integer t) throws Throwable {
                return Maybe.<Integer>never().doOnDispose(new Action() {

                    @Override
                    public void run() throws Throwable {
                        sb.append("Dispose");
                    }
                });
            }
        }, new Consumer<Integer>() {

            @Override
            public void accept(Integer t) throws Throwable {
                sb.append("Resource");
            }
        }, true).test();
        to.assertEmpty();
        to.dispose();
        assertEquals("ResourceDispose", sb.toString());
    }

    @Test
    public void nonEagerDisposeUpstreamThenDisposeResource() {
        final StringBuilder sb = new StringBuilder();
        TestObserver<Integer> to = Maybe.using(Functions.justSupplier(1), new Function<Integer, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Integer t) throws Throwable {
                return Maybe.<Integer>never().doOnDispose(new Action() {

                    @Override
                    public void run() throws Throwable {
                        sb.append("Dispose");
                    }
                });
            }
        }, new Consumer<Integer>() {

            @Override
            public void accept(Integer t) throws Throwable {
                sb.append("Resource");
            }
        }, false).test();
        to.assertEmpty();
        to.dispose();
        assertEquals("DisposeResource", sb.toString());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeUsingTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resourceSupplierThrows() throws java.lang.Throwable {
            this.payloads.resourceSupplierThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorEager() throws java.lang.Throwable {
            this.payloads.errorEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyEager() throws java.lang.Throwable {
            this.payloads.emptyEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNonEager() throws java.lang.Throwable {
            this.payloads.errorNonEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyNonEager() throws java.lang.Throwable {
            this.payloads.emptyNonEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierCrashEager() throws java.lang.Throwable {
            this.payloads.supplierCrashEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierCrashNonEager() throws java.lang.Throwable {
            this.payloads.supplierCrashNonEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierAndDisposerCrashEager() throws java.lang.Throwable {
            this.payloads.supplierAndDisposerCrashEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierAndDisposerCrashNonEager() throws java.lang.Throwable {
            this.payloads.supplierAndDisposerCrashNonEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeCrashes() throws java.lang.Throwable {
            this.payloads.disposeCrashes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposed() throws java.lang.Throwable {
            this.payloads.isDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justDisposerCrashes() throws java.lang.Throwable {
            this.payloads.justDisposerCrashes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyDisposerCrashes() throws java.lang.Throwable {
            this.payloads.emptyDisposerCrashes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDisposerCrash() throws java.lang.Throwable {
            this.payloads.errorDisposerCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successDisposeRace() throws java.lang.Throwable {
            this.payloads.successDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDisposeRace() throws java.lang.Throwable {
            this.payloads.errorDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyDisposeRace() throws java.lang.Throwable {
            this.payloads.emptyDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eagerDisposeResourceThenDisposeUpstream() throws java.lang.Throwable {
            this.payloads.eagerDisposeResourceThenDisposeUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonEagerDisposeUpstreamThenDisposeResource() throws java.lang.Throwable {
            this.payloads.nonEagerDisposeUpstreamThenDisposeResource.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeUsingTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeUsingTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeUsingTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeUsingTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeUsingTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeUsingTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeUsingTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeUsingTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement resourceSupplierThrows;

            public org.junit.runners.model.Statement errorEager;

            public org.junit.runners.model.Statement emptyEager;

            public org.junit.runners.model.Statement errorNonEager;

            public org.junit.runners.model.Statement emptyNonEager;

            public org.junit.runners.model.Statement supplierCrashEager;

            public org.junit.runners.model.Statement supplierCrashNonEager;

            public org.junit.runners.model.Statement supplierAndDisposerCrashEager;

            public org.junit.runners.model.Statement supplierAndDisposerCrashNonEager;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement disposeCrashes;

            public org.junit.runners.model.Statement isDisposed;

            public org.junit.runners.model.Statement justDisposerCrashes;

            public org.junit.runners.model.Statement emptyDisposerCrashes;

            public org.junit.runners.model.Statement errorDisposerCrash;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement successDisposeRace;

            public org.junit.runners.model.Statement errorDisposeRace;

            public org.junit.runners.model.Statement emptyDisposeRace;

            public org.junit.runners.model.Statement eagerDisposeResourceThenDisposeUpstream;

            public org.junit.runners.model.Statement nonEagerDisposeUpstreamThenDisposeResource;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.resourceSupplierThrows = _ClassStatement.forPayload(MaybeUsingTest::resourceSupplierThrows, "resourceSupplierThrows", this);
            this.payloads.errorEager = _ClassStatement.forPayload(MaybeUsingTest::errorEager, "errorEager", this);
            this.payloads.emptyEager = _ClassStatement.forPayload(MaybeUsingTest::emptyEager, "emptyEager", this);
            this.payloads.errorNonEager = _ClassStatement.forPayload(MaybeUsingTest::errorNonEager, "errorNonEager", this);
            this.payloads.emptyNonEager = _ClassStatement.forPayload(MaybeUsingTest::emptyNonEager, "emptyNonEager", this);
            this.payloads.supplierCrashEager = _ClassStatement.forPayload(MaybeUsingTest::supplierCrashEager, "supplierCrashEager", this);
            this.payloads.supplierCrashNonEager = _ClassStatement.forPayload(MaybeUsingTest::supplierCrashNonEager, "supplierCrashNonEager", this);
            this.payloads.supplierAndDisposerCrashEager = _ClassStatement.forPayload(MaybeUsingTest::supplierAndDisposerCrashEager, "supplierAndDisposerCrashEager", this);
            this.payloads.supplierAndDisposerCrashNonEager = _ClassStatement.forPayload(MaybeUsingTest::supplierAndDisposerCrashNonEager, "supplierAndDisposerCrashNonEager", this);
            this.payloads.dispose = _ClassStatement.forPayload(MaybeUsingTest::dispose, "dispose", this);
            this.payloads.disposeCrashes = _ClassStatement.forPayload(MaybeUsingTest::disposeCrashes, "disposeCrashes", this);
            this.payloads.isDisposed = _ClassStatement.forPayload(MaybeUsingTest::isDisposed, "isDisposed", this);
            this.payloads.justDisposerCrashes = _ClassStatement.forPayload(MaybeUsingTest::justDisposerCrashes, "justDisposerCrashes", this);
            this.payloads.emptyDisposerCrashes = _ClassStatement.forPayload(MaybeUsingTest::emptyDisposerCrashes, "emptyDisposerCrashes", this);
            this.payloads.errorDisposerCrash = _ClassStatement.forPayload(MaybeUsingTest::errorDisposerCrash, "errorDisposerCrash", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(MaybeUsingTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.successDisposeRace = _ClassStatement.forPayload(MaybeUsingTest::successDisposeRace, "successDisposeRace", this);
            this.payloads.errorDisposeRace = _ClassStatement.forPayload(MaybeUsingTest::errorDisposeRace, "errorDisposeRace", this);
            this.payloads.emptyDisposeRace = _ClassStatement.forPayload(MaybeUsingTest::emptyDisposeRace, "emptyDisposeRace", this);
            this.payloads.eagerDisposeResourceThenDisposeUpstream = _ClassStatement.forPayload(MaybeUsingTest::eagerDisposeResourceThenDisposeUpstream, "eagerDisposeResourceThenDisposeUpstream", this);
            this.payloads.nonEagerDisposeUpstreamThenDisposeResource = _ClassStatement.forPayload(MaybeUsingTest::nonEagerDisposeUpstreamThenDisposeResource, "nonEagerDisposeUpstreamThenDisposeResource", this);
        }
    }
}
