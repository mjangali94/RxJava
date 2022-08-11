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
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.MaybeSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeTakeUntilTest extends RxJavaTest {

    @Test
    public void normalPublisher() {
        Maybe.just(1).takeUntil(Flowable.never()).test().assertResult(1);
    }

    @Test
    public void normalMaybe() {
        Maybe.just(1).takeUntil(Maybe.never()).test().assertResult(1);
    }

    @Test
    public void untilFirstPublisher() {
        Maybe.just(1).takeUntil(Flowable.just("one")).test().assertResult();
    }

    @Test
    public void untilFirstMaybe() {
        Maybe.just(1).takeUntil(Maybe.just("one")).test().assertResult();
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(PublishProcessor.create().singleElement().takeUntil(Maybe.never()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Maybe<Object> m) throws Exception {
                return m.takeUntil(Maybe.never());
            }
        });
    }

    @Test
    public void mainErrors() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().takeUntil(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onError(new TestException());
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void otherErrors() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().takeUntil(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onError(new TestException());
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void mainCompletes() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().takeUntil(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void otherCompletes() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().takeUntil(pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void onErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp1 = PublishProcessor.create();
            final PublishProcessor<Integer> pp2 = PublishProcessor.create();
            TestObserver<Integer> to = pp1.singleElement().takeUntil(pp2.singleElement()).test();
            final TestException ex1 = new TestException();
            final TestException ex2 = new TestException();
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp1.onError(ex1);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        pp2.onError(ex2);
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
    public void onCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp1 = PublishProcessor.create();
            final PublishProcessor<Integer> pp2 = PublishProcessor.create();
            TestObserver<Integer> to = pp1.singleElement().takeUntil(pp2.singleElement()).test();
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
            to.assertResult();
        }
    }

    @Test
    public void untilMaybeMainSuccess() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        MaybeSubject<Integer> other = MaybeSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onSuccess(1);
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult(1);
    }

    @Test
    public void untilMaybeMainComplete() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        MaybeSubject<Integer> other = MaybeSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult();
    }

    @Test
    public void untilMaybeMainError() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        MaybeSubject<Integer> other = MaybeSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilMaybeOtherSuccess() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        MaybeSubject<Integer> other = MaybeSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onSuccess(1);
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult();
    }

    @Test
    public void untilMaybeOtherComplete() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        MaybeSubject<Integer> other = MaybeSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult();
    }

    @Test
    public void untilMaybeOtherError() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        MaybeSubject<Integer> other = MaybeSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilMaybeDispose() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        MaybeSubject<Integer> other = MaybeSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        to.dispose();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertEmpty();
    }

    @Test
    public void untilPublisherMainSuccess() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        main.onSuccess(1);
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertResult(1);
    }

    @Test
    public void untilPublisherMainComplete() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        main.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void untilPublisherMainError() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        main.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilPublisherOtherOnNext() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        other.onNext(1);
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void untilPublisherOtherOnComplete() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        other.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void untilPublisherOtherError() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        other.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilPublisherDispose() {
        MaybeSubject<Integer> main = MaybeSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        to.dispose();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeTakeUntilTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalPublisher() throws java.lang.Throwable {
            this.payloads.normalPublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalMaybe() throws java.lang.Throwable {
            this.payloads.normalMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilFirstPublisher() throws java.lang.Throwable {
            this.payloads.untilFirstPublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilFirstMaybe() throws java.lang.Throwable {
            this.payloads.untilFirstMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrors() throws java.lang.Throwable {
            this.payloads.mainErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherErrors() throws java.lang.Throwable {
            this.payloads.otherErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompletes() throws java.lang.Throwable {
            this.payloads.mainCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherCompletes() throws java.lang.Throwable {
            this.payloads.otherCompletes.evaluate();
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
        public void benchmark_untilMaybeMainSuccess() throws java.lang.Throwable {
            this.payloads.untilMaybeMainSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilMaybeMainComplete() throws java.lang.Throwable {
            this.payloads.untilMaybeMainComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilMaybeMainError() throws java.lang.Throwable {
            this.payloads.untilMaybeMainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilMaybeOtherSuccess() throws java.lang.Throwable {
            this.payloads.untilMaybeOtherSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilMaybeOtherComplete() throws java.lang.Throwable {
            this.payloads.untilMaybeOtherComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilMaybeOtherError() throws java.lang.Throwable {
            this.payloads.untilMaybeOtherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilMaybeDispose() throws java.lang.Throwable {
            this.payloads.untilMaybeDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherMainSuccess() throws java.lang.Throwable {
            this.payloads.untilPublisherMainSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherMainComplete() throws java.lang.Throwable {
            this.payloads.untilPublisherMainComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherMainError() throws java.lang.Throwable {
            this.payloads.untilPublisherMainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherOtherOnNext() throws java.lang.Throwable {
            this.payloads.untilPublisherOtherOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherOtherOnComplete() throws java.lang.Throwable {
            this.payloads.untilPublisherOtherOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherOtherError() throws java.lang.Throwable {
            this.payloads.untilPublisherOtherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherDispose() throws java.lang.Throwable {
            this.payloads.untilPublisherDispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTakeUntilTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTakeUntilTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTakeUntilTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTakeUntilTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeTakeUntilTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTakeUntilTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeTakeUntilTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeTakeUntilTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normalPublisher;

            public org.junit.runners.model.Statement normalMaybe;

            public org.junit.runners.model.Statement untilFirstPublisher;

            public org.junit.runners.model.Statement untilFirstMaybe;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement mainErrors;

            public org.junit.runners.model.Statement otherErrors;

            public org.junit.runners.model.Statement mainCompletes;

            public org.junit.runners.model.Statement otherCompletes;

            public org.junit.runners.model.Statement onErrorRace;

            public org.junit.runners.model.Statement onCompleteRace;

            public org.junit.runners.model.Statement untilMaybeMainSuccess;

            public org.junit.runners.model.Statement untilMaybeMainComplete;

            public org.junit.runners.model.Statement untilMaybeMainError;

            public org.junit.runners.model.Statement untilMaybeOtherSuccess;

            public org.junit.runners.model.Statement untilMaybeOtherComplete;

            public org.junit.runners.model.Statement untilMaybeOtherError;

            public org.junit.runners.model.Statement untilMaybeDispose;

            public org.junit.runners.model.Statement untilPublisherMainSuccess;

            public org.junit.runners.model.Statement untilPublisherMainComplete;

            public org.junit.runners.model.Statement untilPublisherMainError;

            public org.junit.runners.model.Statement untilPublisherOtherOnNext;

            public org.junit.runners.model.Statement untilPublisherOtherOnComplete;

            public org.junit.runners.model.Statement untilPublisherOtherError;

            public org.junit.runners.model.Statement untilPublisherDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normalPublisher = _ClassStatement.forPayload(MaybeTakeUntilTest::normalPublisher, "normalPublisher", this);
            this.payloads.normalMaybe = _ClassStatement.forPayload(MaybeTakeUntilTest::normalMaybe, "normalMaybe", this);
            this.payloads.untilFirstPublisher = _ClassStatement.forPayload(MaybeTakeUntilTest::untilFirstPublisher, "untilFirstPublisher", this);
            this.payloads.untilFirstMaybe = _ClassStatement.forPayload(MaybeTakeUntilTest::untilFirstMaybe, "untilFirstMaybe", this);
            this.payloads.disposed = _ClassStatement.forPayload(MaybeTakeUntilTest::disposed, "disposed", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(MaybeTakeUntilTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.mainErrors = _ClassStatement.forPayload(MaybeTakeUntilTest::mainErrors, "mainErrors", this);
            this.payloads.otherErrors = _ClassStatement.forPayload(MaybeTakeUntilTest::otherErrors, "otherErrors", this);
            this.payloads.mainCompletes = _ClassStatement.forPayload(MaybeTakeUntilTest::mainCompletes, "mainCompletes", this);
            this.payloads.otherCompletes = _ClassStatement.forPayload(MaybeTakeUntilTest::otherCompletes, "otherCompletes", this);
            this.payloads.onErrorRace = _ClassStatement.forPayload(MaybeTakeUntilTest::onErrorRace, "onErrorRace", this);
            this.payloads.onCompleteRace = _ClassStatement.forPayload(MaybeTakeUntilTest::onCompleteRace, "onCompleteRace", this);
            this.payloads.untilMaybeMainSuccess = _ClassStatement.forPayload(MaybeTakeUntilTest::untilMaybeMainSuccess, "untilMaybeMainSuccess", this);
            this.payloads.untilMaybeMainComplete = _ClassStatement.forPayload(MaybeTakeUntilTest::untilMaybeMainComplete, "untilMaybeMainComplete", this);
            this.payloads.untilMaybeMainError = _ClassStatement.forPayload(MaybeTakeUntilTest::untilMaybeMainError, "untilMaybeMainError", this);
            this.payloads.untilMaybeOtherSuccess = _ClassStatement.forPayload(MaybeTakeUntilTest::untilMaybeOtherSuccess, "untilMaybeOtherSuccess", this);
            this.payloads.untilMaybeOtherComplete = _ClassStatement.forPayload(MaybeTakeUntilTest::untilMaybeOtherComplete, "untilMaybeOtherComplete", this);
            this.payloads.untilMaybeOtherError = _ClassStatement.forPayload(MaybeTakeUntilTest::untilMaybeOtherError, "untilMaybeOtherError", this);
            this.payloads.untilMaybeDispose = _ClassStatement.forPayload(MaybeTakeUntilTest::untilMaybeDispose, "untilMaybeDispose", this);
            this.payloads.untilPublisherMainSuccess = _ClassStatement.forPayload(MaybeTakeUntilTest::untilPublisherMainSuccess, "untilPublisherMainSuccess", this);
            this.payloads.untilPublisherMainComplete = _ClassStatement.forPayload(MaybeTakeUntilTest::untilPublisherMainComplete, "untilPublisherMainComplete", this);
            this.payloads.untilPublisherMainError = _ClassStatement.forPayload(MaybeTakeUntilTest::untilPublisherMainError, "untilPublisherMainError", this);
            this.payloads.untilPublisherOtherOnNext = _ClassStatement.forPayload(MaybeTakeUntilTest::untilPublisherOtherOnNext, "untilPublisherOtherOnNext", this);
            this.payloads.untilPublisherOtherOnComplete = _ClassStatement.forPayload(MaybeTakeUntilTest::untilPublisherOtherOnComplete, "untilPublisherOtherOnComplete", this);
            this.payloads.untilPublisherOtherError = _ClassStatement.forPayload(MaybeTakeUntilTest::untilPublisherOtherError, "untilPublisherOtherError", this);
            this.payloads.untilPublisherDispose = _ClassStatement.forPayload(MaybeTakeUntilTest::untilPublisherDispose, "untilPublisherDispose", this);
        }
    }
}
