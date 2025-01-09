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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.BiConsumer;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleAmbTest extends RxJavaTest {

    @Test
    public void ambWithFirstFires() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.single(-99).ambWith(pp2.single(-99)).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onNext(1);
        pp1.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult(1);
    }

    @Test
    public void ambWithSecondFires() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.single(-99).ambWith(pp2.single(-99)).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onNext(2);
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult(2);
    }

    @Test
    public void ambIterableWithFirstFires() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        List<Single<Integer>> singles = Arrays.asList(pp1.single(-99), pp2.single(-99));
        TestObserver<Integer> to = Single.amb(singles).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onNext(1);
        pp1.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult(1);
    }

    @Test
    public void ambIterableWithSecondFires() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        List<Single<Integer>> singles = Arrays.asList(pp1.single(-99), pp2.single(-99));
        TestObserver<Integer> to = Single.amb(singles).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onNext(2);
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult(2);
    }

    @Test
    public void ambArrayEmpty() {
        Single.ambArray().test().assertFailure(NoSuchElementException.class);
    }

    @Test
    public void ambSingleSource() {
        assertSame(Single.never(), Single.ambArray(Single.never()));
    }

    @Test
    public void error() {
        Single.ambArray(Single.error(new TestException()), Single.just(1)).test().assertFailure(TestException.class);
    }

    @Test
    public void nullSourceSuccessRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final Subject<Integer> ps = ReplaySubject.create();
                ps.onNext(1);
                final Single<Integer> source = Single.ambArray(ps.singleOrError(), Single.<Integer>never(), Single.<Integer>never(), null);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        source.test();
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ps.onComplete();
                    }
                };
                TestHelper.race(r1, r2);
                if (!errors.isEmpty()) {
                    TestHelper.assertError(errors, 0, NullPointerException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void multipleErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final Subject<Integer> ps1 = PublishSubject.create();
                final Subject<Integer> ps2 = PublishSubject.create();
                Single.ambArray(ps1.singleOrError(), ps2.singleOrError()).test();
                final TestException ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps1.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ps2.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void successErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final Subject<Integer> ps1 = PublishSubject.create();
                final Subject<Integer> ps2 = PublishSubject.create();
                Single.ambArray(ps1.singleOrError(), ps2.singleOrError()).test();
                final TestException ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps1.onNext(1);
                        ps1.onComplete();
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ps2.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void manySources() {
        Single<?>[] sources = new Single[32];
        Arrays.fill(sources, Single.never());
        sources[31] = Single.just(31);
        Single.amb(Arrays.asList(sources)).test().assertResult(31);
    }

    @Test
    public void ambWithOrder() {
        Single<Integer> error = Single.error(new RuntimeException());
        Single.just(1).ambWith(error).test().assertValue(1);
    }

    @Test
    public void ambIterableOrder() {
        Single<Integer> error = Single.error(new RuntimeException());
        Single.amb(Arrays.asList(Single.just(1), error)).test().assertValue(1);
    }

    @Test
    public void ambArrayOrder() {
        Single<Integer> error = Single.error(new RuntimeException());
        Single.ambArray(Single.just(1), error).test().assertValue(1);
    }

    @Test
    public void noWinnerSuccessDispose() throws Exception {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final AtomicBoolean interrupted = new AtomicBoolean();
            final CountDownLatch cdl = new CountDownLatch(1);
            Single.ambArray(Single.just(1).subscribeOn(Schedulers.single()).observeOn(Schedulers.computation()), Single.never()).subscribe(new BiConsumer<Object, Throwable>() {

                @Override
                public void accept(Object v, Throwable e) throws Exception {
                    assertNotNull(v);
                    assertNull(e);
                    interrupted.set(Thread.currentThread().isInterrupted());
                    cdl.countDown();
                }
            });
            assertTrue(cdl.await(500, TimeUnit.SECONDS));
            assertFalse("Interrupted!", interrupted.get());
        }
    }

    @Test
    public void noWinnerErrorDispose() throws Exception {
        final TestException ex = new TestException();
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final AtomicBoolean interrupted = new AtomicBoolean();
            final CountDownLatch cdl = new CountDownLatch(1);
            Single.ambArray(Single.error(ex).subscribeOn(Schedulers.single()).observeOn(Schedulers.computation()), Single.never()).subscribe(new BiConsumer<Object, Throwable>() {

                @Override
                public void accept(Object v, Throwable e) throws Exception {
                    assertNull(v);
                    assertNotNull(e);
                    interrupted.set(Thread.currentThread().isInterrupted());
                    cdl.countDown();
                }
            });
            assertTrue(cdl.await(500, TimeUnit.SECONDS));
            assertFalse("Interrupted!", interrupted.get());
        }
    }

    @Test
    public void singleSourcesInIterable() {
        SingleSource<Integer> source = new SingleSource<Integer>() {

            @Override
            public void subscribe(SingleObserver<? super Integer> observer) {
                Single.just(1).subscribe(observer);
            }
        };
        Single.amb(Arrays.asList(source, source)).test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public SingleAmbTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWithFirstFires() throws java.lang.Throwable {
            this.payloads.ambWithFirstFires.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWithSecondFires() throws java.lang.Throwable {
            this.payloads.ambWithSecondFires.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableWithFirstFires() throws java.lang.Throwable {
            this.payloads.ambIterableWithFirstFires.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableWithSecondFires() throws java.lang.Throwable {
            this.payloads.ambIterableWithSecondFires.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArrayEmpty() throws java.lang.Throwable {
            this.payloads.ambArrayEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambSingleSource() throws java.lang.Throwable {
            this.payloads.ambSingleSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullSourceSuccessRace() throws java.lang.Throwable {
            this.payloads.nullSourceSuccessRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleErrorRace() throws java.lang.Throwable {
            this.payloads.multipleErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successErrorRace() throws java.lang.Throwable {
            this.payloads.successErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manySources() throws java.lang.Throwable {
            this.payloads.manySources.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWithOrder() throws java.lang.Throwable {
            this.payloads.ambWithOrder.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableOrder() throws java.lang.Throwable {
            this.payloads.ambIterableOrder.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArrayOrder() throws java.lang.Throwable {
            this.payloads.ambArrayOrder.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noWinnerSuccessDispose() throws java.lang.Throwable {
            this.payloads.noWinnerSuccessDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noWinnerErrorDispose() throws java.lang.Throwable {
            this.payloads.noWinnerErrorDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSourcesInIterable() throws java.lang.Throwable {
            this.payloads.singleSourcesInIterable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleAmbTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleAmbTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleAmbTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleAmbTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleAmbTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleAmbTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleAmbTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleAmbTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement ambWithFirstFires;

            public org.junit.runners.model.Statement ambWithSecondFires;

            public org.junit.runners.model.Statement ambIterableWithFirstFires;

            public org.junit.runners.model.Statement ambIterableWithSecondFires;

            public org.junit.runners.model.Statement ambArrayEmpty;

            public org.junit.runners.model.Statement ambSingleSource;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement nullSourceSuccessRace;

            public org.junit.runners.model.Statement multipleErrorRace;

            public org.junit.runners.model.Statement successErrorRace;

            public org.junit.runners.model.Statement manySources;

            public org.junit.runners.model.Statement ambWithOrder;

            public org.junit.runners.model.Statement ambIterableOrder;

            public org.junit.runners.model.Statement ambArrayOrder;

            public org.junit.runners.model.Statement noWinnerSuccessDispose;

            public org.junit.runners.model.Statement noWinnerErrorDispose;

            public org.junit.runners.model.Statement singleSourcesInIterable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.ambWithFirstFires = _ClassStatement.forPayload(SingleAmbTest::ambWithFirstFires, "ambWithFirstFires", this);
            this.payloads.ambWithSecondFires = _ClassStatement.forPayload(SingleAmbTest::ambWithSecondFires, "ambWithSecondFires", this);
            this.payloads.ambIterableWithFirstFires = _ClassStatement.forPayload(SingleAmbTest::ambIterableWithFirstFires, "ambIterableWithFirstFires", this);
            this.payloads.ambIterableWithSecondFires = _ClassStatement.forPayload(SingleAmbTest::ambIterableWithSecondFires, "ambIterableWithSecondFires", this);
            this.payloads.ambArrayEmpty = _ClassStatement.forPayload(SingleAmbTest::ambArrayEmpty, "ambArrayEmpty", this);
            this.payloads.ambSingleSource = _ClassStatement.forPayload(SingleAmbTest::ambSingleSource, "ambSingleSource", this);
            this.payloads.error = _ClassStatement.forPayload(SingleAmbTest::error, "error", this);
            this.payloads.nullSourceSuccessRace = _ClassStatement.forPayload(SingleAmbTest::nullSourceSuccessRace, "nullSourceSuccessRace", this);
            this.payloads.multipleErrorRace = _ClassStatement.forPayload(SingleAmbTest::multipleErrorRace, "multipleErrorRace", this);
            this.payloads.successErrorRace = _ClassStatement.forPayload(SingleAmbTest::successErrorRace, "successErrorRace", this);
            this.payloads.manySources = _ClassStatement.forPayload(SingleAmbTest::manySources, "manySources", this);
            this.payloads.ambWithOrder = _ClassStatement.forPayload(SingleAmbTest::ambWithOrder, "ambWithOrder", this);
            this.payloads.ambIterableOrder = _ClassStatement.forPayload(SingleAmbTest::ambIterableOrder, "ambIterableOrder", this);
            this.payloads.ambArrayOrder = _ClassStatement.forPayload(SingleAmbTest::ambArrayOrder, "ambArrayOrder", this);
            this.payloads.noWinnerSuccessDispose = _ClassStatement.forPayload(SingleAmbTest::noWinnerSuccessDispose, "noWinnerSuccessDispose", this);
            this.payloads.noWinnerErrorDispose = _ClassStatement.forPayload(SingleAmbTest::noWinnerErrorDispose, "noWinnerErrorDispose", this);
            this.payloads.singleSourcesInIterable = _ClassStatement.forPayload(SingleAmbTest::singleSourcesInIterable, "singleSourcesInIterable", this);
        }
    }
}
