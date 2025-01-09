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
import java.util.concurrent.*;
import org.junit.Test;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.MaybeSubject;
import io.reactivex.rxjava3.testsupport.*;

public class MaybeTimeoutTest extends RxJavaTest {

    @Test
    public void normal() {
        Maybe.just(1).timeout(1, TimeUnit.DAYS).test().assertResult(1);
    }

    @Test
    public void normalMaybe() {
        Maybe.just(1).timeout(Maybe.timer(1, TimeUnit.DAYS)).test().assertResult(1);
    }

    @Test
    public void never() {
        Maybe.never().timeout(1, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TimeoutException.class);
    }

    @Test
    public void neverMaybe() {
        Maybe.never().timeout(Maybe.timer(1, TimeUnit.MILLISECONDS)).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TimeoutException.class);
    }

    @Test
    public void normalFallback() {
        Maybe.just(1).timeout(1, TimeUnit.DAYS, Maybe.just(2)).test().assertResult(1);
    }

    @Test
    public void normalMaybeFallback() {
        Maybe.just(1).timeout(Maybe.timer(1, TimeUnit.DAYS), Maybe.just(2)).test().assertResult(1);
    }

    @Test
    public void neverFallback() {
        Maybe.never().timeout(1, TimeUnit.MILLISECONDS, Maybe.just(2)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(2);
    }

    @Test
    public void neverMaybeFallback() {
        Maybe.never().timeout(Maybe.timer(1, TimeUnit.MILLISECONDS), Maybe.just(2)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(2);
    }

    @Test
    public void neverFallbackScheduler() {
        Maybe.never().timeout(1, TimeUnit.MILLISECONDS, Schedulers.single(), Maybe.just(2)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(2);
    }

    @Test
    public void neverScheduler() {
        Maybe.never().timeout(1, TimeUnit.MILLISECONDS, Schedulers.single()).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TimeoutException.class);
    }

    @Test
    public void normalFlowableFallback() {
        Maybe.just(1).timeout(Flowable.timer(1, TimeUnit.DAYS), Maybe.just(2)).test().assertResult(1);
    }

    @Test
    public void neverFlowableFallback() {
        Maybe.never().timeout(Flowable.timer(1, TimeUnit.MILLISECONDS), Maybe.just(2)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(2);
    }

    @Test
    public void normalFlowable() {
        Maybe.just(1).timeout(Flowable.timer(1, TimeUnit.DAYS)).test().assertResult(1);
    }

    @Test
    public void neverFlowable() {
        Maybe.never().timeout(Flowable.timer(1, TimeUnit.MILLISECONDS)).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TimeoutException.class);
    }

    @Test
    public void mainError() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onError(new TestException());
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void otherError() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onError(new TestException());
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void fallbackError() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2.singleElement(), Maybe.<Integer>error(new TestException())).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onNext(1);
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void fallbackComplete() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2.singleElement(), Maybe.<Integer>empty()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onNext(1);
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void mainComplete() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void otherComplete() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TimeoutException.class);
    }

    @Test
    public void dispose() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestHelper.checkDisposed(pp1.singleElement().timeout(pp2.singleElement()));
    }

    @Test
    public void dispose2() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestHelper.checkDisposed(pp1.singleElement().timeout(pp2.singleElement(), Maybe.just(1)));
    }

    @Test
    public void onErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp1 = PublishProcessor.create();
                final PublishProcessor<Integer> pp2 = PublishProcessor.create();
                TestObserver<Integer> to = pp1.singleElement().timeout(pp2.singleElement()).test();
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
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void onCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp1 = PublishProcessor.create();
            final PublishProcessor<Integer> pp2 = PublishProcessor.create();
            TestObserverEx<Integer> to = pp1.singleElement().timeout(pp2.singleElement()).to(TestHelper.<Integer>testConsumer());
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp1.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    pp2.onComplete();
                }
            };
            TestHelper.race(r1, r2);
            to.assertSubscribed().assertNoValues();
            if (to.errors().size() != 0) {
                to.assertError(TimeoutException.class).assertNotComplete();
            } else {
                to.assertNoErrors().assertComplete();
            }
        }
    }

    @Test
    public void mainSuccessAfterOtherSignal() {
        MaybeSubject<Integer> ms = MaybeSubject.create();
        new Maybe<Integer>() {

            @Override
            protected void subscribeActual(@NonNull MaybeObserver<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                ms.onSuccess(2);
                observer.onSuccess(1);
            }
        }.timeout(ms).test().assertFailure(TimeoutException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeTimeoutTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalMaybe() throws java.lang.Throwable {
            this.payloads.normalMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_never() throws java.lang.Throwable {
            this.payloads.never.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverMaybe() throws java.lang.Throwable {
            this.payloads.neverMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalFallback() throws java.lang.Throwable {
            this.payloads.normalFallback.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalMaybeFallback() throws java.lang.Throwable {
            this.payloads.normalMaybeFallback.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverFallback() throws java.lang.Throwable {
            this.payloads.neverFallback.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverMaybeFallback() throws java.lang.Throwable {
            this.payloads.neverMaybeFallback.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverFallbackScheduler() throws java.lang.Throwable {
            this.payloads.neverFallbackScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverScheduler() throws java.lang.Throwable {
            this.payloads.neverScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalFlowableFallback() throws java.lang.Throwable {
            this.payloads.normalFlowableFallback.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverFlowableFallback() throws java.lang.Throwable {
            this.payloads.neverFlowableFallback.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalFlowable() throws java.lang.Throwable {
            this.payloads.normalFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverFlowable() throws java.lang.Throwable {
            this.payloads.neverFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.payloads.mainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherError() throws java.lang.Throwable {
            this.payloads.otherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fallbackError() throws java.lang.Throwable {
            this.payloads.fallbackError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fallbackComplete() throws java.lang.Throwable {
            this.payloads.fallbackComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainComplete() throws java.lang.Throwable {
            this.payloads.mainComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherComplete() throws java.lang.Throwable {
            this.payloads.otherComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose2() throws java.lang.Throwable {
            this.payloads.dispose2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorRace() throws java.lang.Throwable {
            this.payloads.onErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteRace() throws java.lang.Throwable {
            this.payloads.onCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainSuccessAfterOtherSignal() throws java.lang.Throwable {
            this.payloads.mainSuccessAfterOtherSignal.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeoutTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeoutTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeoutTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeoutTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeTimeoutTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeoutTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeTimeoutTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeTimeoutTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement normalMaybe;

            public org.junit.runners.model.Statement never;

            public org.junit.runners.model.Statement neverMaybe;

            public org.junit.runners.model.Statement normalFallback;

            public org.junit.runners.model.Statement normalMaybeFallback;

            public org.junit.runners.model.Statement neverFallback;

            public org.junit.runners.model.Statement neverMaybeFallback;

            public org.junit.runners.model.Statement neverFallbackScheduler;

            public org.junit.runners.model.Statement neverScheduler;

            public org.junit.runners.model.Statement normalFlowableFallback;

            public org.junit.runners.model.Statement neverFlowableFallback;

            public org.junit.runners.model.Statement normalFlowable;

            public org.junit.runners.model.Statement neverFlowable;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement otherError;

            public org.junit.runners.model.Statement fallbackError;

            public org.junit.runners.model.Statement fallbackComplete;

            public org.junit.runners.model.Statement mainComplete;

            public org.junit.runners.model.Statement otherComplete;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement dispose2;

            public org.junit.runners.model.Statement onErrorRace;

            public org.junit.runners.model.Statement onCompleteRace;

            public org.junit.runners.model.Statement mainSuccessAfterOtherSignal;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(MaybeTimeoutTest::normal, "normal", this);
            this.payloads.normalMaybe = _ClassStatement.forPayload(MaybeTimeoutTest::normalMaybe, "normalMaybe", this);
            this.payloads.never = _ClassStatement.forPayload(MaybeTimeoutTest::never, "never", this);
            this.payloads.neverMaybe = _ClassStatement.forPayload(MaybeTimeoutTest::neverMaybe, "neverMaybe", this);
            this.payloads.normalFallback = _ClassStatement.forPayload(MaybeTimeoutTest::normalFallback, "normalFallback", this);
            this.payloads.normalMaybeFallback = _ClassStatement.forPayload(MaybeTimeoutTest::normalMaybeFallback, "normalMaybeFallback", this);
            this.payloads.neverFallback = _ClassStatement.forPayload(MaybeTimeoutTest::neverFallback, "neverFallback", this);
            this.payloads.neverMaybeFallback = _ClassStatement.forPayload(MaybeTimeoutTest::neverMaybeFallback, "neverMaybeFallback", this);
            this.payloads.neverFallbackScheduler = _ClassStatement.forPayload(MaybeTimeoutTest::neverFallbackScheduler, "neverFallbackScheduler", this);
            this.payloads.neverScheduler = _ClassStatement.forPayload(MaybeTimeoutTest::neverScheduler, "neverScheduler", this);
            this.payloads.normalFlowableFallback = _ClassStatement.forPayload(MaybeTimeoutTest::normalFlowableFallback, "normalFlowableFallback", this);
            this.payloads.neverFlowableFallback = _ClassStatement.forPayload(MaybeTimeoutTest::neverFlowableFallback, "neverFlowableFallback", this);
            this.payloads.normalFlowable = _ClassStatement.forPayload(MaybeTimeoutTest::normalFlowable, "normalFlowable", this);
            this.payloads.neverFlowable = _ClassStatement.forPayload(MaybeTimeoutTest::neverFlowable, "neverFlowable", this);
            this.payloads.mainError = _ClassStatement.forPayload(MaybeTimeoutTest::mainError, "mainError", this);
            this.payloads.otherError = _ClassStatement.forPayload(MaybeTimeoutTest::otherError, "otherError", this);
            this.payloads.fallbackError = _ClassStatement.forPayload(MaybeTimeoutTest::fallbackError, "fallbackError", this);
            this.payloads.fallbackComplete = _ClassStatement.forPayload(MaybeTimeoutTest::fallbackComplete, "fallbackComplete", this);
            this.payloads.mainComplete = _ClassStatement.forPayload(MaybeTimeoutTest::mainComplete, "mainComplete", this);
            this.payloads.otherComplete = _ClassStatement.forPayload(MaybeTimeoutTest::otherComplete, "otherComplete", this);
            this.payloads.dispose = _ClassStatement.forPayload(MaybeTimeoutTest::dispose, "dispose", this);
            this.payloads.dispose2 = _ClassStatement.forPayload(MaybeTimeoutTest::dispose2, "dispose2", this);
            this.payloads.onErrorRace = _ClassStatement.forPayload(MaybeTimeoutTest::onErrorRace, "onErrorRace", this);
            this.payloads.onCompleteRace = _ClassStatement.forPayload(MaybeTimeoutTest::onCompleteRace, "onCompleteRace", this);
            this.payloads.mainSuccessAfterOtherSignal = _ClassStatement.forPayload(MaybeTimeoutTest::mainSuccessAfterOtherSignal, "mainSuccessAfterOtherSignal", this);
        }
    }
}
