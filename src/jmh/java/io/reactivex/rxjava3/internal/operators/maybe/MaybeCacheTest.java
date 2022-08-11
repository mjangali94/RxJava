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
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeCacheTest extends RxJavaTest {

    @Test
    public void offlineSuccess() {
        Maybe<Integer> source = Maybe.just(1).cache();
        assertEquals(1, source.blockingGet().intValue());
        source.test().assertResult(1);
    }

    @Test
    public void offlineError() {
        Maybe<Integer> source = Maybe.<Integer>error(new TestException()).cache();
        try {
            source.blockingGet();
            fail("Should have thrown");
        } catch (TestException ex) {
        // expected
        }
        source.test().assertFailure(TestException.class);
    }

    @Test
    public void offlineComplete() {
        Maybe<Integer> source = Maybe.<Integer>empty().cache();
        assertNull(source.blockingGet());
        source.test().assertResult();
    }

    @Test
    public void onlineSuccess() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        Maybe<Integer> source = pp.singleElement().cache();
        assertFalse(pp.hasSubscribers());
        assertNotNull(((MaybeCache<Integer>) source).source.get());
        TestObserver<Integer> to = source.test();
        assertNull(((MaybeCache<Integer>) source).source.get());
        assertTrue(pp.hasSubscribers());
        source.test(true).assertEmpty();
        to.assertEmpty();
        pp.onNext(1);
        pp.onComplete();
        to.assertResult(1);
        source.test().assertResult(1);
        source.test(true).assertEmpty();
    }

    @Test
    public void onlineError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        Maybe<Integer> source = pp.singleElement().cache();
        assertFalse(pp.hasSubscribers());
        assertNotNull(((MaybeCache<Integer>) source).source.get());
        TestObserver<Integer> to = source.test();
        assertNull(((MaybeCache<Integer>) source).source.get());
        assertTrue(pp.hasSubscribers());
        source.test(true).assertEmpty();
        to.assertEmpty();
        pp.onError(new TestException());
        to.assertFailure(TestException.class);
        source.test().assertFailure(TestException.class);
        source.test(true).assertEmpty();
    }

    @Test
    public void onlineComplete() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        Maybe<Integer> source = pp.singleElement().cache();
        assertFalse(pp.hasSubscribers());
        assertNotNull(((MaybeCache<Integer>) source).source.get());
        TestObserver<Integer> to = source.test();
        assertNull(((MaybeCache<Integer>) source).source.get());
        assertTrue(pp.hasSubscribers());
        source.test(true).assertEmpty();
        to.assertEmpty();
        pp.onComplete();
        to.assertResult();
        source.test().assertResult();
        source.test(true).assertEmpty();
    }

    @Test
    public void crossCancelOnSuccess() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        Maybe<Integer> source = pp.singleElement().cache();
        source.subscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                ts.cancel();
            }
        });
        source.toFlowable().subscribe(ts);
        pp.onNext(1);
        pp.onComplete();
        ts.assertEmpty();
    }

    @Test
    public void crossCancelOnError() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        Maybe<Integer> source = pp.singleElement().cache();
        source.subscribe(Functions.emptyConsumer(), new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                ts.cancel();
            }
        });
        source.toFlowable().subscribe(ts);
        pp.onError(new TestException());
        ts.assertEmpty();
    }

    @Test
    public void crossCancelOnComplete() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        Maybe<Integer> source = pp.singleElement().cache();
        source.subscribe(Functions.emptyConsumer(), Functions.emptyConsumer(), new Action() {

            @Override
            public void run() throws Exception {
                ts.cancel();
            }
        });
        source.toFlowable().subscribe(ts);
        pp.onComplete();
        ts.assertEmpty();
    }

    @Test
    public void addAddRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishProcessor<Integer> pp = PublishProcessor.create();
            final Maybe<Integer> source = pp.singleElement().cache();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    source.test();
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void removeRemoveRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishProcessor<Integer> pp = PublishProcessor.create();
            final Maybe<Integer> source = pp.singleElement().cache();
            final TestObserver<Integer> to1 = source.test();
            final TestObserver<Integer> to2 = source.test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    to1.dispose();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to2.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void doubleDispose() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final Maybe<Integer> source = pp.singleElement().cache();
        final Disposable[] dout = { null };
        source.subscribe(new MaybeObserver<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                dout[0] = d;
            }

            @Override
            public void onSuccess(Integer value) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
        dout[0].dispose();
        dout[0].dispose();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeCacheTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_offlineSuccess() throws java.lang.Throwable {
            this.payloads.offlineSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_offlineError() throws java.lang.Throwable {
            this.payloads.offlineError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_offlineComplete() throws java.lang.Throwable {
            this.payloads.offlineComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onlineSuccess() throws java.lang.Throwable {
            this.payloads.onlineSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onlineError() throws java.lang.Throwable {
            this.payloads.onlineError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onlineComplete() throws java.lang.Throwable {
            this.payloads.onlineComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crossCancelOnSuccess() throws java.lang.Throwable {
            this.payloads.crossCancelOnSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crossCancelOnError() throws java.lang.Throwable {
            this.payloads.crossCancelOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crossCancelOnComplete() throws java.lang.Throwable {
            this.payloads.crossCancelOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_addAddRace() throws java.lang.Throwable {
            this.payloads.addAddRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_removeRemoveRace() throws java.lang.Throwable {
            this.payloads.removeRemoveRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleDispose() throws java.lang.Throwable {
            this.payloads.doubleDispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeCacheTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeCacheTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeCacheTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeCacheTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeCacheTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeCacheTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeCacheTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeCacheTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement offlineSuccess;

            public org.junit.runners.model.Statement offlineError;

            public org.junit.runners.model.Statement offlineComplete;

            public org.junit.runners.model.Statement onlineSuccess;

            public org.junit.runners.model.Statement onlineError;

            public org.junit.runners.model.Statement onlineComplete;

            public org.junit.runners.model.Statement crossCancelOnSuccess;

            public org.junit.runners.model.Statement crossCancelOnError;

            public org.junit.runners.model.Statement crossCancelOnComplete;

            public org.junit.runners.model.Statement addAddRace;

            public org.junit.runners.model.Statement removeRemoveRace;

            public org.junit.runners.model.Statement doubleDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.offlineSuccess = _ClassStatement.forPayload(MaybeCacheTest::offlineSuccess, "offlineSuccess", this);
            this.payloads.offlineError = _ClassStatement.forPayload(MaybeCacheTest::offlineError, "offlineError", this);
            this.payloads.offlineComplete = _ClassStatement.forPayload(MaybeCacheTest::offlineComplete, "offlineComplete", this);
            this.payloads.onlineSuccess = _ClassStatement.forPayload(MaybeCacheTest::onlineSuccess, "onlineSuccess", this);
            this.payloads.onlineError = _ClassStatement.forPayload(MaybeCacheTest::onlineError, "onlineError", this);
            this.payloads.onlineComplete = _ClassStatement.forPayload(MaybeCacheTest::onlineComplete, "onlineComplete", this);
            this.payloads.crossCancelOnSuccess = _ClassStatement.forPayload(MaybeCacheTest::crossCancelOnSuccess, "crossCancelOnSuccess", this);
            this.payloads.crossCancelOnError = _ClassStatement.forPayload(MaybeCacheTest::crossCancelOnError, "crossCancelOnError", this);
            this.payloads.crossCancelOnComplete = _ClassStatement.forPayload(MaybeCacheTest::crossCancelOnComplete, "crossCancelOnComplete", this);
            this.payloads.addAddRace = _ClassStatement.forPayload(MaybeCacheTest::addAddRace, "addAddRace", this);
            this.payloads.removeRemoveRace = _ClassStatement.forPayload(MaybeCacheTest::removeRemoveRace, "removeRemoveRace", this);
            this.payloads.doubleDispose = _ClassStatement.forPayload(MaybeCacheTest::doubleDispose, "doubleDispose", this);
        }
    }
}
