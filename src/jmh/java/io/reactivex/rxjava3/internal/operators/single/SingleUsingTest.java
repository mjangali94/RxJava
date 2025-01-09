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
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.*;

public class SingleUsingTest extends RxJavaTest {

    Function<Disposable, Single<Integer>> mapper = new Function<Disposable, Single<Integer>>() {

        @Override
        public Single<Integer> apply(Disposable d) throws Exception {
            return Single.just(1);
        }
    };

    Function<Disposable, Single<Integer>> mapperThrows = new Function<Disposable, Single<Integer>>() {

        @Override
        public Single<Integer> apply(Disposable d) throws Exception {
            throw new TestException("Mapper");
        }
    };

    Consumer<Disposable> disposer = new Consumer<Disposable>() {

        @Override
        public void accept(Disposable d) throws Exception {
            d.dispose();
        }
    };

    Consumer<Disposable> disposerThrows = new Consumer<Disposable>() {

        @Override
        public void accept(Disposable d) throws Exception {
            throw new TestException("Disposer");
        }
    };

    @Test
    public void resourceSupplierThrows() {
        Single.using(new Supplier<Integer>() {

            @Override
            public Integer get() throws Exception {
                throw new TestException();
            }
        }, Functions.justFunction(Single.just(1)), Functions.emptyConsumer()).test().assertFailure(TestException.class);
    }

    @Test
    public void normalEager() {
        Single.using(Functions.justSupplier(1), Functions.justFunction(Single.just(1)), Functions.emptyConsumer()).test().assertResult(1);
    }

    @Test
    public void normalNonEager() {
        Single.using(Functions.justSupplier(1), Functions.justFunction(Single.just(1)), Functions.emptyConsumer(), false).test().assertResult(1);
    }

    @Test
    public void errorEager() {
        Single.using(Functions.justSupplier(1), Functions.justFunction(Single.error(new TestException())), Functions.emptyConsumer()).test().assertFailure(TestException.class);
    }

    @Test
    public void errorNonEager() {
        Single.using(Functions.justSupplier(1), Functions.justFunction(Single.error(new TestException())), Functions.emptyConsumer(), false).test().assertFailure(TestException.class);
    }

    @Test
    public void eagerMapperThrowsDisposerThrows() {
        TestObserverEx<Integer> to = Single.using(Functions.justSupplier(Disposable.empty()), mapperThrows, disposerThrows).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> ce = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(ce, 0, TestException.class, "Mapper");
        TestHelper.assertError(ce, 1, TestException.class, "Disposer");
    }

    @Test
    public void noneagerMapperThrowsDisposerThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Single.using(Functions.justSupplier(Disposable.empty()), mapperThrows, disposerThrows, false).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "Mapper");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Disposer");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void resourceDisposedIfMapperCrashes() {
        Disposable d = Disposable.empty();
        Single.using(Functions.justSupplier(d), mapperThrows, disposer).test().assertFailure(TestException.class);
        assertTrue(d.isDisposed());
    }

    @Test
    public void resourceDisposedIfMapperCrashesNonEager() {
        Disposable d = Disposable.empty();
        Single.using(Functions.justSupplier(d), mapperThrows, disposer, false).test().assertFailure(TestException.class);
        assertTrue(d.isDisposed());
    }

    @Test
    public void dispose() {
        Disposable d = Disposable.empty();
        Single.using(Functions.justSupplier(d), mapper, disposer, false).test(true);
        assertTrue(d.isDisposed());
    }

    @Test
    public void disposerThrowsEager() {
        Single.using(Functions.justSupplier(Disposable.empty()), mapper, disposerThrows).test().assertFailure(TestException.class);
    }

    @Test
    public void disposerThrowsNonEager() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Single.using(Functions.justSupplier(Disposable.empty()), mapper, disposerThrows, false).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Disposer");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void errorAndDisposerThrowsEager() {
        TestObserverEx<Integer> to = Single.using(Functions.justSupplier(Disposable.empty()), new Function<Disposable, SingleSource<Integer>>() {

            @Override
            public SingleSource<Integer> apply(Disposable v) throws Exception {
                return Single.<Integer>error(new TestException("Mapper-run"));
            }
        }, disposerThrows).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> ce = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(ce, 0, TestException.class, "Mapper-run");
        TestHelper.assertError(ce, 1, TestException.class, "Disposer");
    }

    @Test
    public void errorAndDisposerThrowsNonEager() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Single.using(Functions.justSupplier(Disposable.empty()), new Function<Disposable, SingleSource<Integer>>() {

                @Override
                public SingleSource<Integer> apply(Disposable v) throws Exception {
                    return Single.<Integer>error(new TestException("Mapper-run"));
                }
            }, disposerThrows, false).test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Disposer");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void successDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            Disposable d = Disposable.empty();
            final TestObserver<Integer> to = Single.using(Functions.justSupplier(d), new Function<Disposable, SingleSource<Integer>>() {

                @Override
                public SingleSource<Integer> apply(Disposable v) throws Exception {
                    return pp.single(-99);
                }
            }, disposer).test();
            pp.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
            assertTrue(d.isDisposed());
        }
    }

    @Test
    public void doubleOnSubscribe() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Single.using(Functions.justSupplier(1), new Function<Integer, SingleSource<Integer>>() {

                @Override
                public SingleSource<Integer> apply(Integer v) throws Exception {
                    return new Single<Integer>() {

                        @Override
                        protected void subscribeActual(SingleObserver<? super Integer> observer) {
                            observer.onSubscribe(Disposable.empty());
                            assertFalse(((Disposable) observer).isDisposed());
                            Disposable d = Disposable.empty();
                            observer.onSubscribe(d);
                            assertTrue(d.isDisposed());
                            assertFalse(((Disposable) observer).isDisposed());
                            observer.onSuccess(1);
                            assertTrue(((Disposable) observer).isDisposed());
                        }
                    };
                }
            }, Functions.emptyConsumer()).test().assertResult(1);
            TestHelper.assertError(errors, 0, IllegalStateException.class, "Disposable already set!");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    @SuppressUndeliverable
    public void errorDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            Disposable d = Disposable.empty();
            final TestObserver<Integer> to = Single.using(Functions.justSupplier(d), new Function<Disposable, SingleSource<Integer>>() {

                @Override
                public SingleSource<Integer> apply(Disposable v) throws Exception {
                    return pp.single(-99);
                }
            }, disposer).test();
            final TestException ex = new TestException();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onError(ex);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
            assertTrue(d.isDisposed());
        }
    }

    @Test
    public void eagerDisposeResourceThenDisposeUpstream() {
        final StringBuilder sb = new StringBuilder();
        TestObserver<Integer> to = Single.using(Functions.justSupplier(1), new Function<Integer, Single<Integer>>() {

            @Override
            public Single<Integer> apply(Integer t) throws Throwable {
                return Single.<Integer>never().doOnDispose(new Action() {

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
        TestObserver<Integer> to = Single.using(Functions.justSupplier(1), new Function<Integer, Single<Integer>>() {

            @Override
            public Single<Integer> apply(Integer t) throws Throwable {
                return Single.<Integer>never().doOnDispose(new Action() {

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

        public SingleUsingTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resourceSupplierThrows() throws java.lang.Throwable {
            this.payloads.resourceSupplierThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalEager() throws java.lang.Throwable {
            this.payloads.normalEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalNonEager() throws java.lang.Throwable {
            this.payloads.normalNonEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorEager() throws java.lang.Throwable {
            this.payloads.errorEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNonEager() throws java.lang.Throwable {
            this.payloads.errorNonEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eagerMapperThrowsDisposerThrows() throws java.lang.Throwable {
            this.payloads.eagerMapperThrowsDisposerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noneagerMapperThrowsDisposerThrows() throws java.lang.Throwable {
            this.payloads.noneagerMapperThrowsDisposerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resourceDisposedIfMapperCrashes() throws java.lang.Throwable {
            this.payloads.resourceDisposedIfMapperCrashes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resourceDisposedIfMapperCrashesNonEager() throws java.lang.Throwable {
            this.payloads.resourceDisposedIfMapperCrashesNonEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposerThrowsEager() throws java.lang.Throwable {
            this.payloads.disposerThrowsEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposerThrowsNonEager() throws java.lang.Throwable {
            this.payloads.disposerThrowsNonEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorAndDisposerThrowsEager() throws java.lang.Throwable {
            this.payloads.errorAndDisposerThrowsEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorAndDisposerThrowsNonEager() throws java.lang.Throwable {
            this.payloads.errorAndDisposerThrowsNonEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successDisposeRace() throws java.lang.Throwable {
            this.payloads.successDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorDisposeRace() throws java.lang.Throwable {
            this.payloads.errorDisposeRace.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleUsingTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleUsingTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleUsingTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleUsingTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleUsingTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleUsingTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleUsingTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleUsingTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement resourceSupplierThrows;

            public org.junit.runners.model.Statement normalEager;

            public org.junit.runners.model.Statement normalNonEager;

            public org.junit.runners.model.Statement errorEager;

            public org.junit.runners.model.Statement errorNonEager;

            public org.junit.runners.model.Statement eagerMapperThrowsDisposerThrows;

            public org.junit.runners.model.Statement noneagerMapperThrowsDisposerThrows;

            public org.junit.runners.model.Statement resourceDisposedIfMapperCrashes;

            public org.junit.runners.model.Statement resourceDisposedIfMapperCrashesNonEager;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement disposerThrowsEager;

            public org.junit.runners.model.Statement disposerThrowsNonEager;

            public org.junit.runners.model.Statement errorAndDisposerThrowsEager;

            public org.junit.runners.model.Statement errorAndDisposerThrowsNonEager;

            public org.junit.runners.model.Statement successDisposeRace;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement errorDisposeRace;

            public org.junit.runners.model.Statement eagerDisposeResourceThenDisposeUpstream;

            public org.junit.runners.model.Statement nonEagerDisposeUpstreamThenDisposeResource;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.resourceSupplierThrows = _ClassStatement.forPayload(SingleUsingTest::resourceSupplierThrows, "resourceSupplierThrows", this);
            this.payloads.normalEager = _ClassStatement.forPayload(SingleUsingTest::normalEager, "normalEager", this);
            this.payloads.normalNonEager = _ClassStatement.forPayload(SingleUsingTest::normalNonEager, "normalNonEager", this);
            this.payloads.errorEager = _ClassStatement.forPayload(SingleUsingTest::errorEager, "errorEager", this);
            this.payloads.errorNonEager = _ClassStatement.forPayload(SingleUsingTest::errorNonEager, "errorNonEager", this);
            this.payloads.eagerMapperThrowsDisposerThrows = _ClassStatement.forPayload(SingleUsingTest::eagerMapperThrowsDisposerThrows, "eagerMapperThrowsDisposerThrows", this);
            this.payloads.noneagerMapperThrowsDisposerThrows = _ClassStatement.forPayload(SingleUsingTest::noneagerMapperThrowsDisposerThrows, "noneagerMapperThrowsDisposerThrows", this);
            this.payloads.resourceDisposedIfMapperCrashes = _ClassStatement.forPayload(SingleUsingTest::resourceDisposedIfMapperCrashes, "resourceDisposedIfMapperCrashes", this);
            this.payloads.resourceDisposedIfMapperCrashesNonEager = _ClassStatement.forPayload(SingleUsingTest::resourceDisposedIfMapperCrashesNonEager, "resourceDisposedIfMapperCrashesNonEager", this);
            this.payloads.dispose = _ClassStatement.forPayload(SingleUsingTest::dispose, "dispose", this);
            this.payloads.disposerThrowsEager = _ClassStatement.forPayload(SingleUsingTest::disposerThrowsEager, "disposerThrowsEager", this);
            this.payloads.disposerThrowsNonEager = _ClassStatement.forPayload(SingleUsingTest::disposerThrowsNonEager, "disposerThrowsNonEager", this);
            this.payloads.errorAndDisposerThrowsEager = _ClassStatement.forPayload(SingleUsingTest::errorAndDisposerThrowsEager, "errorAndDisposerThrowsEager", this);
            this.payloads.errorAndDisposerThrowsNonEager = _ClassStatement.forPayload(SingleUsingTest::errorAndDisposerThrowsNonEager, "errorAndDisposerThrowsNonEager", this);
            this.payloads.successDisposeRace = _ClassStatement.forPayload(SingleUsingTest::successDisposeRace, "successDisposeRace", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(SingleUsingTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.errorDisposeRace = _ClassStatement.forPayload(SingleUsingTest::errorDisposeRace, "errorDisposeRace", this);
            this.payloads.eagerDisposeResourceThenDisposeUpstream = _ClassStatement.forPayload(SingleUsingTest::eagerDisposeResourceThenDisposeUpstream, "eagerDisposeResourceThenDisposeUpstream", this);
            this.payloads.nonEagerDisposeUpstreamThenDisposeResource = _ClassStatement.forPayload(SingleUsingTest::nonEagerDisposeUpstreamThenDisposeResource, "nonEagerDisposeUpstreamThenDisposeResource", this);
        }
    }
}
