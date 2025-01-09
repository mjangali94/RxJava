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
package io.reactivex.rxjava3.internal.operators.mixed;

import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableConcatMapCompletableTest extends RxJavaTest {

    @Test
    public void simple() {
        Flowable.range(1, 5).concatMapCompletable(Functions.justFunction(Completable.complete())).test().assertResult();
    }

    @Test
    public void simple2() {
        final AtomicInteger counter = new AtomicInteger();
        Flowable.range(1, 5).concatMapCompletable(Functions.justFunction(Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                counter.incrementAndGet();
            }
        }))).test().assertResult();
        assertEquals(5, counter.get());
    }

    @Test
    public void simpleLongPrefetch() {
        Flowable.range(1, 1024).concatMapCompletable(Functions.justFunction(Completable.complete()), 32).test().assertResult();
    }

    @Test
    public void simpleLongPrefetchHidden() {
        Flowable.range(1, 1024).hide().concatMapCompletable(Functions.justFunction(Completable.complete()), 32).test().assertResult();
    }

    @Test
    public void mainError() {
        Flowable.<Integer>error(new TestException()).concatMapCompletable(Functions.justFunction(Completable.complete())).test().assertFailure(TestException.class);
    }

    @Test
    public void innerError() {
        Flowable.just(1).concatMapCompletable(Functions.justFunction(Completable.error(new TestException()))).test().assertFailure(TestException.class);
    }

    @Test
    public void innerErrorDelayed() {
        TestObserverEx<Void> to = Flowable.range(1, 5).concatMapCompletableDelayError(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }).to(TestHelper.<Void>testConsumer()).assertFailure(CompositeException.class);
        assertEquals(5, ((CompositeException) to.errors().get(0)).getExceptions().size());
    }

    @Test
    public void mapperCrash() {
        Flowable.just(1).concatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void immediateError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        CompletableSubject cs = CompletableSubject.create();
        TestObserver<Void> to = pp.concatMapCompletable(Functions.justFunction(cs)).test();
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        assertFalse(cs.hasObservers());
        pp.onNext(1);
        assertTrue(cs.hasObservers());
        pp.onError(new TestException());
        assertFalse(cs.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void immediateError2() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        CompletableSubject cs = CompletableSubject.create();
        TestObserver<Void> to = pp.concatMapCompletable(Functions.justFunction(cs)).test();
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        assertFalse(cs.hasObservers());
        pp.onNext(1);
        assertTrue(cs.hasObservers());
        cs.onError(new TestException());
        assertFalse(pp.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void boundaryError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        CompletableSubject cs = CompletableSubject.create();
        TestObserver<Void> to = pp.concatMapCompletableDelayError(Functions.justFunction(cs), false).test();
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        assertFalse(cs.hasObservers());
        pp.onNext(1);
        assertTrue(cs.hasObservers());
        pp.onError(new TestException());
        assertTrue(cs.hasObservers());
        to.assertEmpty();
        cs.onComplete();
        to.assertFailure(TestException.class);
    }

    @Test
    public void endError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final CompletableSubject cs = CompletableSubject.create();
        final CompletableSubject cs2 = CompletableSubject.create();
        TestObserver<Void> to = pp.concatMapCompletableDelayError(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                if (v == 1) {
                    return cs;
                }
                return cs2;
            }
        }, true, 32).test();
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        assertFalse(cs.hasObservers());
        pp.onNext(1);
        assertTrue(cs.hasObservers());
        cs.onError(new TestException());
        assertTrue(pp.hasSubscribers());
        pp.onNext(2);
        to.assertEmpty();
        cs2.onComplete();
        assertTrue(pp.hasSubscribers());
        to.assertEmpty();
        pp.onComplete();
        to.assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToCompletable(new Function<Flowable<Object>, Completable>() {

            @Override
            public Completable apply(Flowable<Object> f) throws Exception {
                return f.concatMapCompletable(Functions.justFunction(Completable.complete()));
            }
        });
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Flowable.never().concatMapCompletable(Functions.justFunction(Completable.complete())));
    }

    @Test
    public void queueOverflow() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onNext(2);
                    s.onNext(3);
                    s.onError(new TestException());
                }
            }.concatMapCompletable(Functions.justFunction(Completable.never()), 1).test().assertFailure(MissingBackpressureException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void immediateOuterInnerErrorRace() {
        final TestException ex = new TestException();
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp = PublishProcessor.create();
                final CompletableSubject cs = CompletableSubject.create();
                TestObserver<Void> to = pp.concatMapCompletable(Functions.justFunction(cs)).test();
                pp.onNext(1);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        cs.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertError(new Predicate<Throwable>() {

                    @Override
                    public boolean test(Throwable e) throws Exception {
                        return e instanceof TestException || e instanceof CompositeException;
                    }
                }).assertNotComplete();
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void disposeInDrainLoop() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final CompletableSubject cs = CompletableSubject.create();
            final TestObserver<Void> to = pp.concatMapCompletable(Functions.justFunction(cs)).test();
            pp.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(2);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    cs.onComplete();
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
            to.assertEmpty();
        }
    }

    @Test
    public void doneButNotEmpty() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        final CompletableSubject cs = CompletableSubject.create();
        final TestObserver<Void> to = pp.concatMapCompletable(Functions.justFunction(cs)).test();
        pp.onNext(1);
        pp.onNext(2);
        pp.onComplete();
        cs.onComplete();
        to.assertResult();
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Flowable<Integer> upstream) {
                return upstream.concatMapCompletable(new Function<Integer, Completable>() {

                    @Override
                    public Completable apply(Integer v) throws Throwable {
                        return Completable.complete().hide();
                    }
                });
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayError() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Flowable<Integer> upstream) {
                return upstream.concatMapCompletableDelayError(new Function<Integer, Completable>() {

                    @Override
                    public Completable apply(Integer v) throws Throwable {
                        return Completable.complete().hide();
                    }
                }, false, 2);
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayErrorTillEnd() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Flowable<Integer> upstream) {
                return upstream.concatMapCompletableDelayError(new Function<Integer, Completable>() {

                    @Override
                    public Completable apply(Integer v) throws Throwable {
                        return Completable.complete().hide();
                    }
                }, true, 2);
            }
        });
    }

    @Test
    public void basicNonFused() {
        Flowable.range(1, 5).hide().concatMapCompletable(v -> Completable.complete().hide()).test().assertResult();
    }

    @Test
    public void basicSyncFused() {
        Flowable.range(1, 5).concatMapCompletable(v -> Completable.complete().hide()).test().assertResult();
    }

    @Test
    public void basicAsyncFused() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.concatMapCompletable(v -> Completable.complete().hide()).test().assertResult();
    }

    @Test
    public void basicFusionRejected() {
        TestHelper.<Integer>rejectFlowableFusion().concatMapCompletable(v -> Completable.complete().hide()).test().assertEmpty();
    }

    @Test
    public void fusedPollCrash() {
        Flowable.range(1, 5).map(v -> {
            if (v == 3) {
                throw new TestException();
            }
            return v;
        }).compose(TestHelper.flowableStripBoundary()).concatMapCompletable(v -> Completable.complete().hide()).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableConcatMapCompletableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.payloads.simple.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple2() throws java.lang.Throwable {
            this.payloads.simple2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleLongPrefetch() throws java.lang.Throwable {
            this.payloads.simpleLongPrefetch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleLongPrefetchHidden() throws java.lang.Throwable {
            this.payloads.simpleLongPrefetchHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainError() throws java.lang.Throwable {
            this.payloads.mainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerError() throws java.lang.Throwable {
            this.payloads.innerError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorDelayed() throws java.lang.Throwable {
            this.payloads.innerErrorDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperCrash() throws java.lang.Throwable {
            this.payloads.mapperCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_immediateError() throws java.lang.Throwable {
            this.payloads.immediateError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_immediateError2() throws java.lang.Throwable {
            this.payloads.immediateError2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryError() throws java.lang.Throwable {
            this.payloads.boundaryError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_endError() throws java.lang.Throwable {
            this.payloads.endError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_queueOverflow() throws java.lang.Throwable {
            this.payloads.queueOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_immediateOuterInnerErrorRace() throws java.lang.Throwable {
            this.payloads.immediateOuterInnerErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeInDrainLoop() throws java.lang.Throwable {
            this.payloads.disposeInDrainLoop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doneButNotEmpty() throws java.lang.Throwable {
            this.payloads.doneButNotEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelDelayError() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancelDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelDelayErrorTillEnd() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancelDelayErrorTillEnd.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicNonFused() throws java.lang.Throwable {
            this.payloads.basicNonFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicSyncFused() throws java.lang.Throwable {
            this.payloads.basicSyncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicAsyncFused() throws java.lang.Throwable {
            this.payloads.basicAsyncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicFusionRejected() throws java.lang.Throwable {
            this.payloads.basicFusionRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPollCrash() throws java.lang.Throwable {
            this.payloads.fusedPollCrash.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapCompletableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapCompletableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapCompletableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapCompletableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableConcatMapCompletableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapCompletableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableConcatMapCompletableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableConcatMapCompletableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement simple;

            public org.junit.runners.model.Statement simple2;

            public org.junit.runners.model.Statement simpleLongPrefetch;

            public org.junit.runners.model.Statement simpleLongPrefetchHidden;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement innerError;

            public org.junit.runners.model.Statement innerErrorDelayed;

            public org.junit.runners.model.Statement mapperCrash;

            public org.junit.runners.model.Statement immediateError;

            public org.junit.runners.model.Statement immediateError2;

            public org.junit.runners.model.Statement boundaryError;

            public org.junit.runners.model.Statement endError;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement queueOverflow;

            public org.junit.runners.model.Statement immediateOuterInnerErrorRace;

            public org.junit.runners.model.Statement disposeInDrainLoop;

            public org.junit.runners.model.Statement doneButNotEmpty;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayErrorTillEnd;

            public org.junit.runners.model.Statement basicNonFused;

            public org.junit.runners.model.Statement basicSyncFused;

            public org.junit.runners.model.Statement basicAsyncFused;

            public org.junit.runners.model.Statement basicFusionRejected;

            public org.junit.runners.model.Statement fusedPollCrash;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.simple = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::simple, "simple", this);
            this.payloads.simple2 = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::simple2, "simple2", this);
            this.payloads.simpleLongPrefetch = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::simpleLongPrefetch, "simpleLongPrefetch", this);
            this.payloads.simpleLongPrefetchHidden = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::simpleLongPrefetchHidden, "simpleLongPrefetchHidden", this);
            this.payloads.mainError = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::mainError, "mainError", this);
            this.payloads.innerError = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::innerError, "innerError", this);
            this.payloads.innerErrorDelayed = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::innerErrorDelayed, "innerErrorDelayed", this);
            this.payloads.mapperCrash = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::mapperCrash, "mapperCrash", this);
            this.payloads.immediateError = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::immediateError, "immediateError", this);
            this.payloads.immediateError2 = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::immediateError2, "immediateError2", this);
            this.payloads.boundaryError = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::boundaryError, "boundaryError", this);
            this.payloads.endError = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::endError, "endError", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.disposed = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::disposed, "disposed", this);
            this.payloads.queueOverflow = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::queueOverflow, "queueOverflow", this);
            this.payloads.immediateOuterInnerErrorRace = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::immediateOuterInnerErrorRace, "immediateOuterInnerErrorRace", this);
            this.payloads.disposeInDrainLoop = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::disposeInDrainLoop, "disposeInDrainLoop", this);
            this.payloads.doneButNotEmpty = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::doneButNotEmpty, "doneButNotEmpty", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.undeliverableUponCancelDelayErrorTillEnd = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::undeliverableUponCancelDelayErrorTillEnd, "undeliverableUponCancelDelayErrorTillEnd", this);
            this.payloads.basicNonFused = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::basicNonFused, "basicNonFused", this);
            this.payloads.basicSyncFused = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::basicSyncFused, "basicSyncFused", this);
            this.payloads.basicAsyncFused = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::basicAsyncFused, "basicAsyncFused", this);
            this.payloads.basicFusionRejected = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::basicFusionRejected, "basicFusionRejected", this);
            this.payloads.fusedPollCrash = _ClassStatement.forPayload(FlowableConcatMapCompletableTest::fusedPollCrash, "fusedPollCrash", this);
        }
    }
}
