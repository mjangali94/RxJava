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
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.*;

public class MaybeTimeoutPublisherTest extends RxJavaTest {

    @Test
    public void mainError() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2).test();
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
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2).test();
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
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2, Maybe.<Integer>error(new TestException())).test();
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
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2, Maybe.<Integer>empty()).test();
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
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2).test();
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
        TestObserver<Integer> to = pp1.singleElement().timeout(pp2).test();
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
        TestHelper.checkDisposed(pp1.singleElement().timeout(pp2));
    }

    @Test
    public void dispose2() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestHelper.checkDisposed(pp1.singleElement().timeout(pp2, Maybe.just(1)));
    }

    @Test
    public void onErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp1 = PublishProcessor.create();
                final PublishProcessor<Integer> pp2 = PublishProcessor.create();
                TestObserver<Integer> to = pp1.singleElement().timeout(pp2).test();
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
            TestObserverEx<Integer> to = pp1.singleElement().timeout(pp2).to(TestHelper.<Integer>testConsumer());
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
    public void badSourceOther() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return Maybe.never().timeout(f, Maybe.just(1));
            }
        }, false, null, 1, 1);
    }

    @Test
    public void mainSuccessAfterOtherSignal() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        new Maybe<Integer>() {

            @Override
            protected void subscribeActual(@NonNull MaybeObserver<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                pp.onNext(2);
                observer.onSuccess(1);
            }
        }.timeout(pp).test().assertFailure(TimeoutException.class);
    }

    @Test
    public void mainSuccess() {
        Maybe.just(1).timeout(Flowable.never()).test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeTimeoutPublisherTest instance;

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
        public void benchmark_badSourceOther() throws java.lang.Throwable {
            this.payloads.badSourceOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainSuccessAfterOtherSignal() throws java.lang.Throwable {
            this.payloads.mainSuccessAfterOtherSignal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainSuccess() throws java.lang.Throwable {
            this.payloads.mainSuccess.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeoutPublisherTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeoutPublisherTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeoutPublisherTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeoutPublisherTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeTimeoutPublisherTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTimeoutPublisherTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeTimeoutPublisherTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeTimeoutPublisherTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

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

            public org.junit.runners.model.Statement badSourceOther;

            public org.junit.runners.model.Statement mainSuccessAfterOtherSignal;

            public org.junit.runners.model.Statement mainSuccess;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.mainError = _ClassStatement.forPayload(MaybeTimeoutPublisherTest::mainError, "mainError", this);
            this.payloads.otherError = _ClassStatement.forPayload(MaybeTimeoutPublisherTest::otherError, "otherError", this);
            this.payloads.fallbackError = _ClassStatement.forPayload(MaybeTimeoutPublisherTest::fallbackError, "fallbackError", this);
            this.payloads.fallbackComplete = _ClassStatement.forPayload(MaybeTimeoutPublisherTest::fallbackComplete, "fallbackComplete", this);
            this.payloads.mainComplete = _ClassStatement.forPayload(MaybeTimeoutPublisherTest::mainComplete, "mainComplete", this);
            this.payloads.otherComplete = _ClassStatement.forPayload(MaybeTimeoutPublisherTest::otherComplete, "otherComplete", this);
            this.payloads.dispose = _ClassStatement.forPayload(MaybeTimeoutPublisherTest::dispose, "dispose", this);
            this.payloads.dispose2 = _ClassStatement.forPayload(MaybeTimeoutPublisherTest::dispose2, "dispose2", this);
            this.payloads.onErrorRace = _ClassStatement.forPayload(MaybeTimeoutPublisherTest::onErrorRace, "onErrorRace", this);
            this.payloads.onCompleteRace = _ClassStatement.forPayload(MaybeTimeoutPublisherTest::onCompleteRace, "onCompleteRace", this);
            this.payloads.badSourceOther = _ClassStatement.forPayload(MaybeTimeoutPublisherTest::badSourceOther, "badSourceOther", this);
            this.payloads.mainSuccessAfterOtherSignal = _ClassStatement.forPayload(MaybeTimeoutPublisherTest::mainSuccessAfterOtherSignal, "mainSuccessAfterOtherSignal", this);
            this.payloads.mainSuccess = _ClassStatement.forPayload(MaybeTimeoutPublisherTest::mainSuccess, "mainSuccess", this);
        }
    }
}
