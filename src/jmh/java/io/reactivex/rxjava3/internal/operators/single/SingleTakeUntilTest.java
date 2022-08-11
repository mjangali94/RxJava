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
import java.util.concurrent.CancellationException;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleTakeUntilTest extends RxJavaTest {

    @Test
    public void mainSuccessPublisher() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestObserver<Integer> to = source.single(-99).takeUntil(pp).test();
        source.onNext(1);
        source.onComplete();
        to.assertResult(1);
    }

    @Test
    public void mainSuccessSingle() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestObserver<Integer> to = source.single(-99).takeUntil(pp.single(-99)).test();
        source.onNext(1);
        source.onComplete();
        to.assertResult(1);
    }

    @Test
    public void mainSuccessCompletable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestObserver<Integer> to = source.single(-99).takeUntil(pp.ignoreElements()).test();
        source.onNext(1);
        source.onComplete();
        to.assertResult(1);
    }

    @Test
    public void mainErrorPublisher() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestObserver<Integer> to = source.single(-99).takeUntil(pp).test();
        source.onError(new TestException());
        to.assertFailure(TestException.class);
    }

    @Test
    public void mainErrorSingle() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestObserver<Integer> to = source.single(-99).takeUntil(pp.single(-99)).test();
        source.onError(new TestException());
        to.assertFailure(TestException.class);
    }

    @Test
    public void mainErrorCompletable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestObserver<Integer> to = source.single(-99).takeUntil(pp.ignoreElements()).test();
        source.onError(new TestException());
        to.assertFailure(TestException.class);
    }

    @Test
    public void otherOnNextPublisher() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestObserver<Integer> to = source.single(-99).takeUntil(pp).test();
        pp.onNext(1);
        to.assertFailure(CancellationException.class);
    }

    @Test
    public void otherOnNextSingle() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestObserver<Integer> to = source.single(-99).takeUntil(pp.single(-99)).test();
        pp.onNext(1);
        pp.onComplete();
        to.assertFailure(CancellationException.class);
    }

    @Test
    public void otherOnNextCompletable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestObserver<Integer> to = source.single(-99).takeUntil(pp.ignoreElements()).test();
        pp.onNext(1);
        pp.onComplete();
        to.assertFailure(CancellationException.class);
    }

    @Test
    public void otherOnCompletePublisher() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestObserver<Integer> to = source.single(-99).takeUntil(pp).test();
        pp.onComplete();
        to.assertFailure(CancellationException.class);
    }

    @Test
    public void otherOnCompleteCompletable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestObserver<Integer> to = source.single(-99).takeUntil(pp.ignoreElements()).test();
        pp.onComplete();
        to.assertFailure(CancellationException.class);
    }

    @Test
    public void otherErrorPublisher() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestObserver<Integer> to = source.single(-99).takeUntil(pp).test();
        pp.onError(new TestException());
        to.assertFailure(TestException.class);
    }

    @Test
    public void otherErrorSingle() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestObserver<Integer> to = source.single(-99).takeUntil(pp.single(-99)).test();
        pp.onError(new TestException());
        to.assertFailure(TestException.class);
    }

    @Test
    public void otherErrorCompletable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        PublishProcessor<Integer> source = PublishProcessor.create();
        TestObserver<Integer> to = source.single(-99).takeUntil(pp.ignoreElements()).test();
        pp.onError(new TestException());
        to.assertFailure(TestException.class);
    }

    @Test
    public void withPublisherDispose() {
        TestHelper.checkDisposed(Single.never().takeUntil(Flowable.never()));
    }

    @Test
    public void onErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp1 = PublishProcessor.create();
                final PublishProcessor<Integer> pp2 = PublishProcessor.create();
                TestObserver<Integer> to = pp1.singleOrError().takeUntil(pp2).test();
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
    public void otherSignalsAndCompletes() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Single.just(1).takeUntil(Flowable.just(1).take(1)).test().assertFailure(CancellationException.class);
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void flowableCancelDelayed() {
        Single.never().takeUntil(new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onNext(1);
                s.onNext(2);
            }
        }).test().assertFailure(CancellationException.class);
    }

    @Test
    public void untilSingleMainSuccess() {
        SingleSubject<Integer> main = SingleSubject.create();
        SingleSubject<Integer> other = SingleSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onSuccess(1);
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult(1);
    }

    @Test
    public void untilSingleMainError() {
        SingleSubject<Integer> main = SingleSubject.create();
        SingleSubject<Integer> other = SingleSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilSingleOtherSuccess() {
        SingleSubject<Integer> main = SingleSubject.create();
        SingleSubject<Integer> other = SingleSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onSuccess(1);
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(CancellationException.class);
    }

    @Test
    public void untilSingleOtherError() {
        SingleSubject<Integer> main = SingleSubject.create();
        SingleSubject<Integer> other = SingleSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilSingleDispose() {
        SingleSubject<Integer> main = SingleSubject.create();
        SingleSubject<Integer> other = SingleSubject.create();
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
        SingleSubject<Integer> main = SingleSubject.create();
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
    public void untilPublisherMainError() {
        SingleSubject<Integer> main = SingleSubject.create();
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
        SingleSubject<Integer> main = SingleSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        other.onNext(1);
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertFailure(CancellationException.class);
    }

    @Test
    public void untilPublisherOtherOnComplete() {
        SingleSubject<Integer> main = SingleSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        other.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertFailure(CancellationException.class);
    }

    @Test
    public void untilPublisherOtherError() {
        SingleSubject<Integer> main = SingleSubject.create();
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
        SingleSubject<Integer> main = SingleSubject.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasSubscribers());
        to.dispose();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasSubscribers());
        to.assertEmpty();
    }

    @Test
    public void untilCompletableMainSuccess() {
        SingleSubject<Integer> main = SingleSubject.create();
        CompletableSubject other = CompletableSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onSuccess(1);
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult(1);
    }

    @Test
    public void untilCompletableMainError() {
        SingleSubject<Integer> main = SingleSubject.create();
        CompletableSubject other = CompletableSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilCompletableOtherOnComplete() {
        SingleSubject<Integer> main = SingleSubject.create();
        CompletableSubject other = CompletableSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(CancellationException.class);
    }

    @Test
    public void untilCompletableOtherError() {
        SingleSubject<Integer> main = SingleSubject.create();
        CompletableSubject other = CompletableSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilCompletableDispose() {
        SingleSubject<Integer> main = SingleSubject.create();
        CompletableSubject other = CompletableSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        to.dispose();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleTakeUntilTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainSuccessPublisher() throws java.lang.Throwable {
            this.payloads.mainSuccessPublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainSuccessSingle() throws java.lang.Throwable {
            this.payloads.mainSuccessSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainSuccessCompletable() throws java.lang.Throwable {
            this.payloads.mainSuccessCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorPublisher() throws java.lang.Throwable {
            this.payloads.mainErrorPublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorSingle() throws java.lang.Throwable {
            this.payloads.mainErrorSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorCompletable() throws java.lang.Throwable {
            this.payloads.mainErrorCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherOnNextPublisher() throws java.lang.Throwable {
            this.payloads.otherOnNextPublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherOnNextSingle() throws java.lang.Throwable {
            this.payloads.otherOnNextSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherOnNextCompletable() throws java.lang.Throwable {
            this.payloads.otherOnNextCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherOnCompletePublisher() throws java.lang.Throwable {
            this.payloads.otherOnCompletePublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherOnCompleteCompletable() throws java.lang.Throwable {
            this.payloads.otherOnCompleteCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherErrorPublisher() throws java.lang.Throwable {
            this.payloads.otherErrorPublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherErrorSingle() throws java.lang.Throwable {
            this.payloads.otherErrorSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherErrorCompletable() throws java.lang.Throwable {
            this.payloads.otherErrorCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withPublisherDispose() throws java.lang.Throwable {
            this.payloads.withPublisherDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorRace() throws java.lang.Throwable {
            this.payloads.onErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherSignalsAndCompletes() throws java.lang.Throwable {
            this.payloads.otherSignalsAndCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableCancelDelayed() throws java.lang.Throwable {
            this.payloads.flowableCancelDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilSingleMainSuccess() throws java.lang.Throwable {
            this.payloads.untilSingleMainSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilSingleMainError() throws java.lang.Throwable {
            this.payloads.untilSingleMainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilSingleOtherSuccess() throws java.lang.Throwable {
            this.payloads.untilSingleOtherSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilSingleOtherError() throws java.lang.Throwable {
            this.payloads.untilSingleOtherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilSingleDispose() throws java.lang.Throwable {
            this.payloads.untilSingleDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherMainSuccess() throws java.lang.Throwable {
            this.payloads.untilPublisherMainSuccess.evaluate();
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

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilCompletableMainSuccess() throws java.lang.Throwable {
            this.payloads.untilCompletableMainSuccess.evaluate();
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
        public void benchmark_untilCompletableDispose() throws java.lang.Throwable {
            this.payloads.untilCompletableDispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTakeUntilTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTakeUntilTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTakeUntilTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTakeUntilTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleTakeUntilTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleTakeUntilTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleTakeUntilTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleTakeUntilTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement mainSuccessPublisher;

            public org.junit.runners.model.Statement mainSuccessSingle;

            public org.junit.runners.model.Statement mainSuccessCompletable;

            public org.junit.runners.model.Statement mainErrorPublisher;

            public org.junit.runners.model.Statement mainErrorSingle;

            public org.junit.runners.model.Statement mainErrorCompletable;

            public org.junit.runners.model.Statement otherOnNextPublisher;

            public org.junit.runners.model.Statement otherOnNextSingle;

            public org.junit.runners.model.Statement otherOnNextCompletable;

            public org.junit.runners.model.Statement otherOnCompletePublisher;

            public org.junit.runners.model.Statement otherOnCompleteCompletable;

            public org.junit.runners.model.Statement otherErrorPublisher;

            public org.junit.runners.model.Statement otherErrorSingle;

            public org.junit.runners.model.Statement otherErrorCompletable;

            public org.junit.runners.model.Statement withPublisherDispose;

            public org.junit.runners.model.Statement onErrorRace;

            public org.junit.runners.model.Statement otherSignalsAndCompletes;

            public org.junit.runners.model.Statement flowableCancelDelayed;

            public org.junit.runners.model.Statement untilSingleMainSuccess;

            public org.junit.runners.model.Statement untilSingleMainError;

            public org.junit.runners.model.Statement untilSingleOtherSuccess;

            public org.junit.runners.model.Statement untilSingleOtherError;

            public org.junit.runners.model.Statement untilSingleDispose;

            public org.junit.runners.model.Statement untilPublisherMainSuccess;

            public org.junit.runners.model.Statement untilPublisherMainError;

            public org.junit.runners.model.Statement untilPublisherOtherOnNext;

            public org.junit.runners.model.Statement untilPublisherOtherOnComplete;

            public org.junit.runners.model.Statement untilPublisherOtherError;

            public org.junit.runners.model.Statement untilPublisherDispose;

            public org.junit.runners.model.Statement untilCompletableMainSuccess;

            public org.junit.runners.model.Statement untilCompletableMainError;

            public org.junit.runners.model.Statement untilCompletableOtherOnComplete;

            public org.junit.runners.model.Statement untilCompletableOtherError;

            public org.junit.runners.model.Statement untilCompletableDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.mainSuccessPublisher = _ClassStatement.forPayload(SingleTakeUntilTest::mainSuccessPublisher, "mainSuccessPublisher", this);
            this.payloads.mainSuccessSingle = _ClassStatement.forPayload(SingleTakeUntilTest::mainSuccessSingle, "mainSuccessSingle", this);
            this.payloads.mainSuccessCompletable = _ClassStatement.forPayload(SingleTakeUntilTest::mainSuccessCompletable, "mainSuccessCompletable", this);
            this.payloads.mainErrorPublisher = _ClassStatement.forPayload(SingleTakeUntilTest::mainErrorPublisher, "mainErrorPublisher", this);
            this.payloads.mainErrorSingle = _ClassStatement.forPayload(SingleTakeUntilTest::mainErrorSingle, "mainErrorSingle", this);
            this.payloads.mainErrorCompletable = _ClassStatement.forPayload(SingleTakeUntilTest::mainErrorCompletable, "mainErrorCompletable", this);
            this.payloads.otherOnNextPublisher = _ClassStatement.forPayload(SingleTakeUntilTest::otherOnNextPublisher, "otherOnNextPublisher", this);
            this.payloads.otherOnNextSingle = _ClassStatement.forPayload(SingleTakeUntilTest::otherOnNextSingle, "otherOnNextSingle", this);
            this.payloads.otherOnNextCompletable = _ClassStatement.forPayload(SingleTakeUntilTest::otherOnNextCompletable, "otherOnNextCompletable", this);
            this.payloads.otherOnCompletePublisher = _ClassStatement.forPayload(SingleTakeUntilTest::otherOnCompletePublisher, "otherOnCompletePublisher", this);
            this.payloads.otherOnCompleteCompletable = _ClassStatement.forPayload(SingleTakeUntilTest::otherOnCompleteCompletable, "otherOnCompleteCompletable", this);
            this.payloads.otherErrorPublisher = _ClassStatement.forPayload(SingleTakeUntilTest::otherErrorPublisher, "otherErrorPublisher", this);
            this.payloads.otherErrorSingle = _ClassStatement.forPayload(SingleTakeUntilTest::otherErrorSingle, "otherErrorSingle", this);
            this.payloads.otherErrorCompletable = _ClassStatement.forPayload(SingleTakeUntilTest::otherErrorCompletable, "otherErrorCompletable", this);
            this.payloads.withPublisherDispose = _ClassStatement.forPayload(SingleTakeUntilTest::withPublisherDispose, "withPublisherDispose", this);
            this.payloads.onErrorRace = _ClassStatement.forPayload(SingleTakeUntilTest::onErrorRace, "onErrorRace", this);
            this.payloads.otherSignalsAndCompletes = _ClassStatement.forPayload(SingleTakeUntilTest::otherSignalsAndCompletes, "otherSignalsAndCompletes", this);
            this.payloads.flowableCancelDelayed = _ClassStatement.forPayload(SingleTakeUntilTest::flowableCancelDelayed, "flowableCancelDelayed", this);
            this.payloads.untilSingleMainSuccess = _ClassStatement.forPayload(SingleTakeUntilTest::untilSingleMainSuccess, "untilSingleMainSuccess", this);
            this.payloads.untilSingleMainError = _ClassStatement.forPayload(SingleTakeUntilTest::untilSingleMainError, "untilSingleMainError", this);
            this.payloads.untilSingleOtherSuccess = _ClassStatement.forPayload(SingleTakeUntilTest::untilSingleOtherSuccess, "untilSingleOtherSuccess", this);
            this.payloads.untilSingleOtherError = _ClassStatement.forPayload(SingleTakeUntilTest::untilSingleOtherError, "untilSingleOtherError", this);
            this.payloads.untilSingleDispose = _ClassStatement.forPayload(SingleTakeUntilTest::untilSingleDispose, "untilSingleDispose", this);
            this.payloads.untilPublisherMainSuccess = _ClassStatement.forPayload(SingleTakeUntilTest::untilPublisherMainSuccess, "untilPublisherMainSuccess", this);
            this.payloads.untilPublisherMainError = _ClassStatement.forPayload(SingleTakeUntilTest::untilPublisherMainError, "untilPublisherMainError", this);
            this.payloads.untilPublisherOtherOnNext = _ClassStatement.forPayload(SingleTakeUntilTest::untilPublisherOtherOnNext, "untilPublisherOtherOnNext", this);
            this.payloads.untilPublisherOtherOnComplete = _ClassStatement.forPayload(SingleTakeUntilTest::untilPublisherOtherOnComplete, "untilPublisherOtherOnComplete", this);
            this.payloads.untilPublisherOtherError = _ClassStatement.forPayload(SingleTakeUntilTest::untilPublisherOtherError, "untilPublisherOtherError", this);
            this.payloads.untilPublisherDispose = _ClassStatement.forPayload(SingleTakeUntilTest::untilPublisherDispose, "untilPublisherDispose", this);
            this.payloads.untilCompletableMainSuccess = _ClassStatement.forPayload(SingleTakeUntilTest::untilCompletableMainSuccess, "untilCompletableMainSuccess", this);
            this.payloads.untilCompletableMainError = _ClassStatement.forPayload(SingleTakeUntilTest::untilCompletableMainError, "untilCompletableMainError", this);
            this.payloads.untilCompletableOtherOnComplete = _ClassStatement.forPayload(SingleTakeUntilTest::untilCompletableOtherOnComplete, "untilCompletableOtherOnComplete", this);
            this.payloads.untilCompletableOtherError = _ClassStatement.forPayload(SingleTakeUntilTest::untilCompletableOtherError, "untilCompletableOtherError", this);
            this.payloads.untilCompletableDispose = _ClassStatement.forPayload(SingleTakeUntilTest::untilCompletableDispose, "untilCompletableDispose", this);
        }
    }
}
