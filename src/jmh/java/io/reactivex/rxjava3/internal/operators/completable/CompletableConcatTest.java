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
import java.util.concurrent.CountDownLatch;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableConcatTest extends RxJavaTest {

    @Test
    public void overflowReported() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Completable.concat(Flowable.fromPublisher(new Publisher<Completable>() {

                @Override
                public void subscribe(Subscriber<? super Completable> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(Completable.never());
                    s.onNext(Completable.never());
                    s.onNext(Completable.never());
                    s.onNext(Completable.never());
                    s.onComplete();
                }
            }), 1).test().assertFailure(MissingBackpressureException.class);
            TestHelper.assertError(errors, 0, MissingBackpressureException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void invalidPrefetch() {
        try {
            Completable.concat(Flowable.just(Completable.complete()), -99);
            fail("Should have thrown IllegalArgumentExceptio");
        } catch (IllegalArgumentException ex) {
            assertEquals("prefetch > 0 required but it was -99", ex.getMessage());
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Completable.concat(Flowable.just(Completable.complete())));
    }

    @Test
    public void errorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp1 = PublishProcessor.create();
                final PublishProcessor<Integer> pp2 = PublishProcessor.create();
                TestObserver<Void> to = Completable.concat(pp1.map(new Function<Integer, Completable>() {

                    @Override
                    public Completable apply(Integer v) throws Exception {
                        return pp2.ignoreElements();
                    }
                })).test();
                pp1.onNext(1);
                final TestException ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp1.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        pp2.onError(ex);
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
    public void synchronousFusedCrash() {
        Completable.concat(Flowable.range(1, 2).map(new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer v) throws Exception {
                throw new TestException();
            }
        })).test().assertFailure(TestException.class);
    }

    @Test
    public void unboundedIn() {
        Completable.concat(Flowable.just(Completable.complete()).hide(), Integer.MAX_VALUE).test().assertResult();
    }

    @Test
    public void syncFusedUnboundedIn() {
        Completable.concat(Flowable.just(Completable.complete()), Integer.MAX_VALUE).test().assertResult();
    }

    @Test
    public void asyncFusedUnboundedIn() {
        UnicastProcessor<Completable> up = UnicastProcessor.create();
        up.onNext(Completable.complete());
        up.onComplete();
        Completable.concat(up, Integer.MAX_VALUE).test().assertResult();
    }

    @Test
    public void arrayCancelled() {
        Completable.concatArray(Completable.complete(), Completable.complete()).test(true).assertEmpty();
    }

    @Test
    public void arrayFirstCancels() {
        final TestObserver<Void> to = new TestObserver<>();
        Completable.concatArray(new Completable() {

            @Override
            protected void subscribeActual(CompletableObserver observer) {
                observer.onSubscribe(Disposable.empty());
                to.dispose();
                observer.onComplete();
            }
        }, Completable.complete()).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void iterableCancelled() {
        Completable.concat(Arrays.asList(Completable.complete(), Completable.complete())).test(true).assertEmpty();
    }

    @Test
    public void iterableFirstCancels() {
        final TestObserver<Void> to = new TestObserver<>();
        Completable.concat(Arrays.asList(new Completable() {

            @Override
            protected void subscribeActual(CompletableObserver observer) {
                observer.onSubscribe(Disposable.empty());
                to.dispose();
                observer.onComplete();
            }
        }, Completable.complete())).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void arrayCancelRace() {
        Completable[] a = new Completable[1024];
        Arrays.fill(a, Completable.complete());
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final Completable c = Completable.concatArray(a);
            final TestObserver<Void> to = new TestObserver<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    c.subscribe(to);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void iterableCancelRace() {
        Completable[] a = new Completable[1024];
        Arrays.fill(a, Completable.complete());
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final Completable c = Completable.concat(Arrays.asList(a));
            final TestObserver<Void> to = new TestObserver<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    c.subscribe(to);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void noInterrupt() throws InterruptedException {
        for (int k = 0; k < 100; k++) {
            final int count = 10;
            final CountDownLatch latch = new CountDownLatch(count);
            final boolean[] interrupted = { false };
            for (int i = 0; i < count; i++) {
                Completable c0 = Completable.fromAction(new Action() {

                    @Override
                    public void run() throws Exception {
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            // System.out.println("Interrupted! " + Thread.currentThread());
                            interrupted[0] = true;
                        }
                    }
                });
                Completable.concat(Arrays.asList(Completable.complete().subscribeOn(Schedulers.io()).observeOn(Schedulers.io()), c0)).subscribe(new Action() {

                    @Override
                    public void run() throws Exception {
                        latch.countDown();
                    }
                });
            }
            latch.await();
            assertFalse("The second Completable was interrupted!", interrupted[0]);
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.<Completable>checkDoubleOnSubscribeFlowableToCompletable(f -> Completable.concat(f));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private CompletableConcatTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overflowReported() throws java.lang.Throwable {
            this.payloads.overflowReported.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_invalidPrefetch() throws java.lang.Throwable {
            this.payloads.invalidPrefetch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorRace() throws java.lang.Throwable {
            this.payloads.errorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_synchronousFusedCrash() throws java.lang.Throwable {
            this.payloads.synchronousFusedCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unboundedIn() throws java.lang.Throwable {
            this.payloads.unboundedIn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedUnboundedIn() throws java.lang.Throwable {
            this.payloads.syncFusedUnboundedIn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedUnboundedIn() throws java.lang.Throwable {
            this.payloads.asyncFusedUnboundedIn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_arrayCancelled() throws java.lang.Throwable {
            this.payloads.arrayCancelled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_arrayFirstCancels() throws java.lang.Throwable {
            this.payloads.arrayFirstCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableCancelled() throws java.lang.Throwable {
            this.payloads.iterableCancelled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableFirstCancels() throws java.lang.Throwable {
            this.payloads.iterableFirstCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_arrayCancelRace() throws java.lang.Throwable {
            this.payloads.arrayCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableCancelRace() throws java.lang.Throwable {
            this.payloads.iterableCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noInterrupt() throws java.lang.Throwable {
            this.payloads.noInterrupt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableConcatTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableConcatTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableConcatTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableConcatTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableConcatTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableConcatTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableConcatTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableConcatTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement overflowReported;

            public org.junit.runners.model.Statement invalidPrefetch;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement errorRace;

            public org.junit.runners.model.Statement synchronousFusedCrash;

            public org.junit.runners.model.Statement unboundedIn;

            public org.junit.runners.model.Statement syncFusedUnboundedIn;

            public org.junit.runners.model.Statement asyncFusedUnboundedIn;

            public org.junit.runners.model.Statement arrayCancelled;

            public org.junit.runners.model.Statement arrayFirstCancels;

            public org.junit.runners.model.Statement iterableCancelled;

            public org.junit.runners.model.Statement iterableFirstCancels;

            public org.junit.runners.model.Statement arrayCancelRace;

            public org.junit.runners.model.Statement iterableCancelRace;

            public org.junit.runners.model.Statement noInterrupt;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.overflowReported = _ClassStatement.forPayload(CompletableConcatTest::overflowReported, "overflowReported", this);
            this.payloads.invalidPrefetch = _ClassStatement.forPayload(CompletableConcatTest::invalidPrefetch, "invalidPrefetch", this);
            this.payloads.dispose = _ClassStatement.forPayload(CompletableConcatTest::dispose, "dispose", this);
            this.payloads.errorRace = _ClassStatement.forPayload(CompletableConcatTest::errorRace, "errorRace", this);
            this.payloads.synchronousFusedCrash = _ClassStatement.forPayload(CompletableConcatTest::synchronousFusedCrash, "synchronousFusedCrash", this);
            this.payloads.unboundedIn = _ClassStatement.forPayload(CompletableConcatTest::unboundedIn, "unboundedIn", this);
            this.payloads.syncFusedUnboundedIn = _ClassStatement.forPayload(CompletableConcatTest::syncFusedUnboundedIn, "syncFusedUnboundedIn", this);
            this.payloads.asyncFusedUnboundedIn = _ClassStatement.forPayload(CompletableConcatTest::asyncFusedUnboundedIn, "asyncFusedUnboundedIn", this);
            this.payloads.arrayCancelled = _ClassStatement.forPayload(CompletableConcatTest::arrayCancelled, "arrayCancelled", this);
            this.payloads.arrayFirstCancels = _ClassStatement.forPayload(CompletableConcatTest::arrayFirstCancels, "arrayFirstCancels", this);
            this.payloads.iterableCancelled = _ClassStatement.forPayload(CompletableConcatTest::iterableCancelled, "iterableCancelled", this);
            this.payloads.iterableFirstCancels = _ClassStatement.forPayload(CompletableConcatTest::iterableFirstCancels, "iterableFirstCancels", this);
            this.payloads.arrayCancelRace = _ClassStatement.forPayload(CompletableConcatTest::arrayCancelRace, "arrayCancelRace", this);
            this.payloads.iterableCancelRace = _ClassStatement.forPayload(CompletableConcatTest::iterableCancelRace, "iterableCancelRace", this);
            this.payloads.noInterrupt = _ClassStatement.forPayload(CompletableConcatTest::noInterrupt, "noInterrupt", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(CompletableConcatTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
