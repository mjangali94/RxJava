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
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.schedulers.ImmediateThinScheduler;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableConcatMapCompletableTest extends RxJavaTest {

    @Test
    public void simple() {
        Observable.range(1, 5).concatMapCompletable(Functions.justFunction(Completable.complete())).test().assertResult();
    }

    @Test
    public void simple2() {
        final AtomicInteger counter = new AtomicInteger();
        Observable.range(1, 5).concatMapCompletable(Functions.justFunction(Completable.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                counter.incrementAndGet();
            }
        }))).test().assertResult();
        assertEquals(5, counter.get());
    }

    @Test
    public void simpleLongPrefetch() {
        Observable.range(1, 1024).concatMapCompletable(Functions.justFunction(Completable.complete()), 32).test().assertResult();
    }

    @Test
    public void mainError() {
        Observable.<Integer>error(new TestException()).concatMapCompletable(Functions.justFunction(Completable.complete())).test().assertFailure(TestException.class);
    }

    @Test
    public void innerError() {
        Observable.just(1).concatMapCompletable(Functions.justFunction(Completable.error(new TestException()))).test().assertFailure(TestException.class);
    }

    @Test
    public void innerErrorDelayed() {
        TestObserverEx<Void> to = Observable.range(1, 5).concatMapCompletableDelayError(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }).to(TestHelper.<Void>testConsumer()).assertFailure(CompositeException.class);
        assertEquals(5, ((CompositeException) to.errors().get(0)).getExceptions().size());
    }

    @Test
    public void mapperCrash() {
        Observable.just(1).concatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void mapperCrashHidden() {
        Observable.just(1).hide().concatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void immediateError() {
        PublishSubject<Integer> ps = PublishSubject.create();
        CompletableSubject cs = CompletableSubject.create();
        TestObserver<Void> to = ps.concatMapCompletable(Functions.justFunction(cs)).test();
        to.assertEmpty();
        assertTrue(ps.hasObservers());
        assertFalse(cs.hasObservers());
        ps.onNext(1);
        assertTrue(cs.hasObservers());
        ps.onError(new TestException());
        assertFalse(cs.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void immediateError2() {
        PublishSubject<Integer> ps = PublishSubject.create();
        CompletableSubject cs = CompletableSubject.create();
        TestObserver<Void> to = ps.concatMapCompletable(Functions.justFunction(cs)).test();
        to.assertEmpty();
        assertTrue(ps.hasObservers());
        assertFalse(cs.hasObservers());
        ps.onNext(1);
        assertTrue(cs.hasObservers());
        cs.onError(new TestException());
        assertFalse(ps.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void boundaryError() {
        PublishSubject<Integer> ps = PublishSubject.create();
        CompletableSubject cs = CompletableSubject.create();
        TestObserver<Void> to = ps.concatMapCompletableDelayError(Functions.justFunction(cs), false).test();
        to.assertEmpty();
        assertTrue(ps.hasObservers());
        assertFalse(cs.hasObservers());
        ps.onNext(1);
        assertTrue(cs.hasObservers());
        ps.onError(new TestException());
        assertTrue(cs.hasObservers());
        to.assertEmpty();
        cs.onComplete();
        to.assertFailure(TestException.class);
    }

    @Test
    public void endError() {
        PublishSubject<Integer> ps = PublishSubject.create();
        final CompletableSubject cs = CompletableSubject.create();
        final CompletableSubject cs2 = CompletableSubject.create();
        TestObserver<Void> to = ps.concatMapCompletableDelayError(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                if (v == 1) {
                    return cs;
                }
                return cs2;
            }
        }, true, 32).test();
        to.assertEmpty();
        assertTrue(ps.hasObservers());
        assertFalse(cs.hasObservers());
        ps.onNext(1);
        assertTrue(cs.hasObservers());
        cs.onError(new TestException());
        assertTrue(ps.hasObservers());
        ps.onNext(2);
        to.assertEmpty();
        cs2.onComplete();
        assertTrue(ps.hasObservers());
        to.assertEmpty();
        ps.onComplete();
        to.assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToCompletable(new Function<Observable<Object>, Completable>() {

            @Override
            public Completable apply(Observable<Object> f) throws Exception {
                return f.concatMapCompletable(Functions.justFunction(Completable.complete()));
            }
        });
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Observable.never().concatMapCompletable(Functions.justFunction(Completable.complete())));
    }

    @Test
    public void immediateOuterInnerErrorRace() {
        final TestException ex = new TestException();
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> ps = PublishSubject.create();
                final CompletableSubject cs = CompletableSubject.create();
                TestObserver<Void> to = ps.concatMapCompletable(Functions.justFunction(cs)).test();
                ps.onNext(1);
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps.onError(ex);
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
            final PublishSubject<Integer> ps = PublishSubject.create();
            final CompletableSubject cs = CompletableSubject.create();
            final TestObserver<Void> to = ps.concatMapCompletable(Functions.justFunction(cs)).test();
            ps.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onNext(2);
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
        final PublishSubject<Integer> ps = PublishSubject.create();
        final CompletableSubject cs = CompletableSubject.create();
        final TestObserver<Void> to = ps.concatMapCompletable(Functions.justFunction(cs)).test();
        ps.onNext(1);
        ps.onNext(2);
        ps.onComplete();
        cs.onComplete();
        to.assertResult();
    }

    @Test
    public void asyncFused() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        final CompletableSubject cs = CompletableSubject.create();
        final TestObserver<Void> to = ps.observeOn(ImmediateThinScheduler.INSTANCE).concatMapCompletable(Functions.justFunction(cs)).test();
        ps.onNext(1);
        ps.onComplete();
        cs.onComplete();
        to.assertResult();
    }

    @Test
    public void fusionRejected() {
        final CompletableSubject cs = CompletableSubject.create();
        TestHelper.rejectObservableFusion().concatMapCompletable(Functions.justFunction(cs)).test().assertEmpty();
    }

    @Test
    public void emptyScalarSource() {
        final CompletableSubject cs = CompletableSubject.create();
        Observable.empty().concatMapCompletable(Functions.justFunction(cs)).test().assertResult();
    }

    @Test
    public void justScalarSource() {
        final CompletableSubject cs = CompletableSubject.create();
        TestObserver<Void> to = Observable.just(1).concatMapCompletable(Functions.justFunction(cs)).test();
        to.assertEmpty();
        assertTrue(cs.hasObservers());
        cs.onComplete();
        to.assertResult();
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Observable<Integer> upstream) {
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
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Observable<Integer> upstream) {
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
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Observable<Integer> upstream) {
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
        Observable.range(1, 5).hide().concatMapCompletable(v -> Completable.complete().hide()).test().assertResult();
    }

    @Test
    public void basicSyncFused() {
        Observable.range(1, 5).concatMapCompletable(v -> Completable.complete().hide()).test().assertResult();
    }

    @Test
    public void basicAsyncFused() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.concatMapCompletable(v -> Completable.complete().hide()).test().assertResult();
    }

    @Test
    public void basicFusionRejected() {
        TestHelper.<Integer>rejectObservableFusion().concatMapCompletable(v -> Completable.complete().hide()).test().assertEmpty();
    }

    @Test
    public void fusedPollCrash() {
        Observable.range(1, 5).map(v -> {
            if (v == 3) {
                throw new TestException();
            }
            return v;
        }).compose(TestHelper.observableStripBoundary()).concatMapCompletable(v -> Completable.complete().hide()).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableConcatMapCompletableTest instance;

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
        public void benchmark_mapperCrashHidden() throws java.lang.Throwable {
            this.payloads.mapperCrashHidden.evaluate();
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
        public void benchmark_asyncFused() throws java.lang.Throwable {
            this.payloads.asyncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejected() throws java.lang.Throwable {
            this.payloads.fusionRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyScalarSource() throws java.lang.Throwable {
            this.payloads.emptyScalarSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justScalarSource() throws java.lang.Throwable {
            this.payloads.justScalarSource.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapCompletableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapCompletableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapCompletableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapCompletableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableConcatMapCompletableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatMapCompletableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableConcatMapCompletableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableConcatMapCompletableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement simple;

            public org.junit.runners.model.Statement simple2;

            public org.junit.runners.model.Statement simpleLongPrefetch;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement innerError;

            public org.junit.runners.model.Statement innerErrorDelayed;

            public org.junit.runners.model.Statement mapperCrash;

            public org.junit.runners.model.Statement mapperCrashHidden;

            public org.junit.runners.model.Statement immediateError;

            public org.junit.runners.model.Statement immediateError2;

            public org.junit.runners.model.Statement boundaryError;

            public org.junit.runners.model.Statement endError;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement immediateOuterInnerErrorRace;

            public org.junit.runners.model.Statement disposeInDrainLoop;

            public org.junit.runners.model.Statement doneButNotEmpty;

            public org.junit.runners.model.Statement asyncFused;

            public org.junit.runners.model.Statement fusionRejected;

            public org.junit.runners.model.Statement emptyScalarSource;

            public org.junit.runners.model.Statement justScalarSource;

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
            this.payloads.simple = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::simple, "simple", this);
            this.payloads.simple2 = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::simple2, "simple2", this);
            this.payloads.simpleLongPrefetch = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::simpleLongPrefetch, "simpleLongPrefetch", this);
            this.payloads.mainError = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::mainError, "mainError", this);
            this.payloads.innerError = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::innerError, "innerError", this);
            this.payloads.innerErrorDelayed = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::innerErrorDelayed, "innerErrorDelayed", this);
            this.payloads.mapperCrash = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::mapperCrash, "mapperCrash", this);
            this.payloads.mapperCrashHidden = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::mapperCrashHidden, "mapperCrashHidden", this);
            this.payloads.immediateError = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::immediateError, "immediateError", this);
            this.payloads.immediateError2 = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::immediateError2, "immediateError2", this);
            this.payloads.boundaryError = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::boundaryError, "boundaryError", this);
            this.payloads.endError = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::endError, "endError", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.disposed = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::disposed, "disposed", this);
            this.payloads.immediateOuterInnerErrorRace = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::immediateOuterInnerErrorRace, "immediateOuterInnerErrorRace", this);
            this.payloads.disposeInDrainLoop = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::disposeInDrainLoop, "disposeInDrainLoop", this);
            this.payloads.doneButNotEmpty = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::doneButNotEmpty, "doneButNotEmpty", this);
            this.payloads.asyncFused = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::asyncFused, "asyncFused", this);
            this.payloads.fusionRejected = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::fusionRejected, "fusionRejected", this);
            this.payloads.emptyScalarSource = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::emptyScalarSource, "emptyScalarSource", this);
            this.payloads.justScalarSource = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::justScalarSource, "justScalarSource", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.undeliverableUponCancelDelayErrorTillEnd = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::undeliverableUponCancelDelayErrorTillEnd, "undeliverableUponCancelDelayErrorTillEnd", this);
            this.payloads.basicNonFused = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::basicNonFused, "basicNonFused", this);
            this.payloads.basicSyncFused = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::basicSyncFused, "basicSyncFused", this);
            this.payloads.basicAsyncFused = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::basicAsyncFused, "basicAsyncFused", this);
            this.payloads.basicFusionRejected = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::basicFusionRejected, "basicFusionRejected", this);
            this.payloads.fusedPollCrash = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::fusedPollCrash, "fusedPollCrash", this);
        }
    }
}
