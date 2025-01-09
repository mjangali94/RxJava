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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeAmbTest extends RxJavaTest {

    @Test
    public void ambLots() {
        List<Maybe<Integer>> ms = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            ms.add(Maybe.<Integer>never());
        }
        ms.add(Maybe.just(1));
        Maybe.amb(ms).test().assertResult(1);
    }

    @Test
    public void ambFirstDone() {
        Maybe.amb(Arrays.asList(Maybe.just(1), Maybe.just(2))).test().assertResult(1);
    }

    @Test
    public void dispose() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.amb(Arrays.asList(pp1.singleElement(), pp2.singleElement())).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        to.dispose();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
    }

    @Test
    public void innerErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp0 = PublishProcessor.create();
                final PublishProcessor<Integer> pp1 = PublishProcessor.create();
                final TestObserver<Integer> to = Maybe.amb(Arrays.asList(pp0.singleElement(), pp1.singleElement())).test();
                final TestException ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp0.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        pp1.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertFailure(TestException.class);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void disposeNoFurtherSignals() {
        TestObserver<Integer> to = Maybe.ambArray(new Maybe<Integer>() {

            @Override
            protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onSuccess(1);
                observer.onSuccess(2);
                observer.onComplete();
            }
        }, Maybe.<Integer>never()).test();
        to.dispose();
        to.assertResult(1);
    }

    @Test
    public void noWinnerSuccessDispose() throws Exception {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final AtomicBoolean interrupted = new AtomicBoolean();
            final CountDownLatch cdl = new CountDownLatch(1);
            Maybe.ambArray(Maybe.just(1).subscribeOn(Schedulers.single()).observeOn(Schedulers.computation()), Maybe.never()).subscribe(new Consumer<Object>() {

                @Override
                public void accept(Object v) throws Exception {
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
            Maybe.ambArray(Maybe.error(ex).subscribeOn(Schedulers.single()).observeOn(Schedulers.computation()), Maybe.never()).subscribe(Functions.emptyConsumer(), new Consumer<Throwable>() {

                @Override
                public void accept(Throwable e) throws Exception {
                    interrupted.set(Thread.currentThread().isInterrupted());
                    cdl.countDown();
                }
            });
            assertTrue(cdl.await(500, TimeUnit.SECONDS));
            assertFalse("Interrupted!", interrupted.get());
        }
    }

    @Test
    public void noWinnerCompleteDispose() throws Exception {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final AtomicBoolean interrupted = new AtomicBoolean();
            final CountDownLatch cdl = new CountDownLatch(1);
            Maybe.ambArray(Maybe.empty().subscribeOn(Schedulers.single()).observeOn(Schedulers.computation()), Maybe.never()).subscribe(Functions.emptyConsumer(), Functions.emptyConsumer(), new Action() {

                @Override
                public void run() throws Exception {
                    interrupted.set(Thread.currentThread().isInterrupted());
                    cdl.countDown();
                }
            });
            assertTrue(cdl.await(500, TimeUnit.SECONDS));
            assertFalse("Interrupted!", interrupted.get());
        }
    }

    @Test
    public void nullSourceSuccessRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final Subject<Integer> ps = ReplaySubject.create();
                ps.onNext(1);
                final Maybe<Integer> source = Maybe.ambArray(ps.singleElement(), Maybe.<Integer>never(), Maybe.<Integer>never(), null);
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
    public void maybeSourcesInIterable() {
        MaybeSource<Integer> source = new MaybeSource<Integer>() {

            @Override
            public void subscribe(MaybeObserver<? super Integer> observer) {
                Maybe.just(1).subscribe(observer);
            }
        };
        Maybe.amb(Arrays.asList(source, source)).test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeAmbTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambLots() throws java.lang.Throwable {
            this.payloads.ambLots.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambFirstDone() throws java.lang.Throwable {
            this.payloads.ambFirstDone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorRace() throws java.lang.Throwable {
            this.payloads.innerErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeNoFurtherSignals() throws java.lang.Throwable {
            this.payloads.disposeNoFurtherSignals.evaluate();
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
        public void benchmark_noWinnerCompleteDispose() throws java.lang.Throwable {
            this.payloads.noWinnerCompleteDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullSourceSuccessRace() throws java.lang.Throwable {
            this.payloads.nullSourceSuccessRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeSourcesInIterable() throws java.lang.Throwable {
            this.payloads.maybeSourcesInIterable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeAmbTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeAmbTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeAmbTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeAmbTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeAmbTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeAmbTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeAmbTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeAmbTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement ambLots;

            public org.junit.runners.model.Statement ambFirstDone;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement innerErrorRace;

            public org.junit.runners.model.Statement disposeNoFurtherSignals;

            public org.junit.runners.model.Statement noWinnerSuccessDispose;

            public org.junit.runners.model.Statement noWinnerErrorDispose;

            public org.junit.runners.model.Statement noWinnerCompleteDispose;

            public org.junit.runners.model.Statement nullSourceSuccessRace;

            public org.junit.runners.model.Statement maybeSourcesInIterable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.ambLots = _ClassStatement.forPayload(MaybeAmbTest::ambLots, "ambLots", this);
            this.payloads.ambFirstDone = _ClassStatement.forPayload(MaybeAmbTest::ambFirstDone, "ambFirstDone", this);
            this.payloads.dispose = _ClassStatement.forPayload(MaybeAmbTest::dispose, "dispose", this);
            this.payloads.innerErrorRace = _ClassStatement.forPayload(MaybeAmbTest::innerErrorRace, "innerErrorRace", this);
            this.payloads.disposeNoFurtherSignals = _ClassStatement.forPayload(MaybeAmbTest::disposeNoFurtherSignals, "disposeNoFurtherSignals", this);
            this.payloads.noWinnerSuccessDispose = _ClassStatement.forPayload(MaybeAmbTest::noWinnerSuccessDispose, "noWinnerSuccessDispose", this);
            this.payloads.noWinnerErrorDispose = _ClassStatement.forPayload(MaybeAmbTest::noWinnerErrorDispose, "noWinnerErrorDispose", this);
            this.payloads.noWinnerCompleteDispose = _ClassStatement.forPayload(MaybeAmbTest::noWinnerCompleteDispose, "noWinnerCompleteDispose", this);
            this.payloads.nullSourceSuccessRace = _ClassStatement.forPayload(MaybeAmbTest::nullSourceSuccessRace, "nullSourceSuccessRace", this);
            this.payloads.maybeSourcesInIterable = _ClassStatement.forPayload(MaybeAmbTest::maybeSourcesInIterable, "maybeSourcesInIterable", this);
        }
    }
}
