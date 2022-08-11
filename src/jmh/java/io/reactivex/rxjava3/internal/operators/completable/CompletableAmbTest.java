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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.completable.CompletableAmb.Amb;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableAmbTest extends RxJavaTest {

    @Test
    public void ambLots() {
        List<Completable> ms = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            ms.add(Completable.never());
        }
        ms.add(Completable.complete());
        Completable.amb(ms).test().assertResult();
    }

    @Test
    public void ambFirstDone() {
        Completable.amb(Arrays.asList(Completable.complete(), Completable.complete())).test().assertResult();
    }

    @Test
    public void dispose() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Void> to = Completable.amb(Arrays.asList(pp1.ignoreElements(), pp2.ignoreElements())).test();
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
                final TestObserver<Void> to = Completable.amb(Arrays.asList(pp0.ignoreElements(), pp1.ignoreElements())).test();
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
    public void nullSourceSuccessRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final Subject<Integer> ps = ReplaySubject.create();
                ps.onNext(1);
                final Completable source = Completable.ambArray(ps.ignoreElements(), Completable.never(), Completable.never(), null);
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
    public void ambWithOrder() {
        Completable error = Completable.error(new RuntimeException());
        Completable.complete().ambWith(error).test().assertComplete();
    }

    @Test
    public void ambIterableOrder() {
        Completable error = Completable.error(new RuntimeException());
        Completable.amb(Arrays.asList(Completable.complete(), error)).test().assertComplete();
    }

    @Test
    public void ambArrayOrder() {
        Completable error = Completable.error(new RuntimeException());
        Completable.ambArray(Completable.complete(), error).test().assertComplete();
    }

    @Test
    public void ambRace() {
        TestObserver<Void> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        CompositeDisposable cd = new CompositeDisposable();
        AtomicBoolean once = new AtomicBoolean();
        Amb a = new Amb(once, cd, to);
        a.onSubscribe(Disposable.empty());
        a.onComplete();
        a.onComplete();
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            a.onError(new TestException());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void untilCompletableMainComplete() {
        CompletableSubject main = CompletableSubject.create();
        CompletableSubject other = CompletableSubject.create();
        TestObserver<Void> to = main.ambWith(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult();
    }

    @Test
    public void untilCompletableMainError() {
        CompletableSubject main = CompletableSubject.create();
        CompletableSubject other = CompletableSubject.create();
        TestObserver<Void> to = main.ambWith(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilCompletableOtherOnComplete() {
        CompletableSubject main = CompletableSubject.create();
        CompletableSubject other = CompletableSubject.create();
        TestObserver<Void> to = main.ambWith(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult();
    }

    @Test
    public void untilCompletableOtherError() {
        CompletableSubject main = CompletableSubject.create();
        CompletableSubject other = CompletableSubject.create();
        TestObserver<Void> to = main.ambWith(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void noWinnerErrorDispose() throws Exception {
        final TestException ex = new TestException();
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final AtomicBoolean interrupted = new AtomicBoolean();
            final CountDownLatch cdl = new CountDownLatch(1);
            Completable.ambArray(Completable.error(ex).subscribeOn(Schedulers.single()).observeOn(Schedulers.computation()), Completable.never()).subscribe(Functions.EMPTY_ACTION, new Consumer<Throwable>() {

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
            Completable.ambArray(Completable.complete().subscribeOn(Schedulers.single()).observeOn(Schedulers.computation()), Completable.never()).subscribe(new Action() {

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
    public void completableSourcesInIterable() {
        CompletableSource source = new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                Completable.complete().subscribe(observer);
            }
        };
        Completable.amb(Arrays.asList(source, source)).test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private CompletableAmbTest instance;

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
        public void benchmark_nullSourceSuccessRace() throws java.lang.Throwable {
            this.payloads.nullSourceSuccessRace.evaluate();
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
        public void benchmark_ambRace() throws java.lang.Throwable {
            this.payloads.ambRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilCompletableMainComplete() throws java.lang.Throwable {
            this.payloads.untilCompletableMainComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilCompletableMainError() throws java.lang.Throwable {
            this.payloads.untilCompletableMainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilCompletableOtherOnComplete() throws java.lang.Throwable {
            this.payloads.untilCompletableOtherOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilCompletableOtherError() throws java.lang.Throwable {
            this.payloads.untilCompletableOtherError.evaluate();
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
        public void benchmark_completableSourcesInIterable() throws java.lang.Throwable {
            this.payloads.completableSourcesInIterable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAmbTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAmbTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAmbTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAmbTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableAmbTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableAmbTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableAmbTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableAmbTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement ambLots;

            public org.junit.runners.model.Statement ambFirstDone;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement innerErrorRace;

            public org.junit.runners.model.Statement nullSourceSuccessRace;

            public org.junit.runners.model.Statement ambWithOrder;

            public org.junit.runners.model.Statement ambIterableOrder;

            public org.junit.runners.model.Statement ambArrayOrder;

            public org.junit.runners.model.Statement ambRace;

            public org.junit.runners.model.Statement untilCompletableMainComplete;

            public org.junit.runners.model.Statement untilCompletableMainError;

            public org.junit.runners.model.Statement untilCompletableOtherOnComplete;

            public org.junit.runners.model.Statement untilCompletableOtherError;

            public org.junit.runners.model.Statement noWinnerErrorDispose;

            public org.junit.runners.model.Statement noWinnerCompleteDispose;

            public org.junit.runners.model.Statement completableSourcesInIterable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.ambLots = _ClassStatement.forPayload(CompletableAmbTest::ambLots, "ambLots", this);
            this.payloads.ambFirstDone = _ClassStatement.forPayload(CompletableAmbTest::ambFirstDone, "ambFirstDone", this);
            this.payloads.dispose = _ClassStatement.forPayload(CompletableAmbTest::dispose, "dispose", this);
            this.payloads.innerErrorRace = _ClassStatement.forPayload(CompletableAmbTest::innerErrorRace, "innerErrorRace", this);
            this.payloads.nullSourceSuccessRace = _ClassStatement.forPayload(CompletableAmbTest::nullSourceSuccessRace, "nullSourceSuccessRace", this);
            this.payloads.ambWithOrder = _ClassStatement.forPayload(CompletableAmbTest::ambWithOrder, "ambWithOrder", this);
            this.payloads.ambIterableOrder = _ClassStatement.forPayload(CompletableAmbTest::ambIterableOrder, "ambIterableOrder", this);
            this.payloads.ambArrayOrder = _ClassStatement.forPayload(CompletableAmbTest::ambArrayOrder, "ambArrayOrder", this);
            this.payloads.ambRace = _ClassStatement.forPayload(CompletableAmbTest::ambRace, "ambRace", this);
            this.payloads.untilCompletableMainComplete = _ClassStatement.forPayload(CompletableAmbTest::untilCompletableMainComplete, "untilCompletableMainComplete", this);
            this.payloads.untilCompletableMainError = _ClassStatement.forPayload(CompletableAmbTest::untilCompletableMainError, "untilCompletableMainError", this);
            this.payloads.untilCompletableOtherOnComplete = _ClassStatement.forPayload(CompletableAmbTest::untilCompletableOtherOnComplete, "untilCompletableOtherOnComplete", this);
            this.payloads.untilCompletableOtherError = _ClassStatement.forPayload(CompletableAmbTest::untilCompletableOtherError, "untilCompletableOtherError", this);
            this.payloads.noWinnerErrorDispose = _ClassStatement.forPayload(CompletableAmbTest::noWinnerErrorDispose, "noWinnerErrorDispose", this);
            this.payloads.noWinnerCompleteDispose = _ClassStatement.forPayload(CompletableAmbTest::noWinnerCompleteDispose, "noWinnerCompleteDispose", this);
            this.payloads.completableSourcesInIterable = _ClassStatement.forPayload(CompletableAmbTest::completableSourcesInIterable, "completableSourcesInIterable", this);
        }
    }
}
