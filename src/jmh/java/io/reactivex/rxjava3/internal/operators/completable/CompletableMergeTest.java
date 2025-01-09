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
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.internal.util.AtomicThrowable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.testsupport.*;

public class CompletableMergeTest extends RxJavaTest {

    @Test
    public void invalidPrefetch() {
        try {
            Completable.merge(Flowable.just(Completable.complete()), -99);
            fail("Should have thrown IllegalArgumentExceptio");
        } catch (IllegalArgumentException ex) {
            assertEquals("maxConcurrency > 0 required but it was -99", ex.getMessage());
        }
    }

    @Test
    public void cancelAfterFirst() {
        final TestObserver<Void> to = new TestObserver<>();
        Completable.mergeArray(new Completable() {

            @Override
            protected void subscribeActual(CompletableObserver observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onComplete();
                to.dispose();
            }
        }, Completable.complete()).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void cancelAfterFirstDelayError() {
        final TestObserver<Void> to = new TestObserver<>();
        Completable.mergeArrayDelayError(new Completable() {

            @Override
            protected void subscribeActual(CompletableObserver observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onComplete();
                to.dispose();
            }
        }, Completable.complete()).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void onErrorAfterComplete() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final CompletableObserver[] co = { null };
            Completable.mergeArrayDelayError(Completable.complete(), new Completable() {

                @Override
                protected void subscribeActual(CompletableObserver observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onComplete();
                    co[0] = observer;
                }
            }).test().assertResult();
            co[0].onError(new TestException());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void completeAfterMain() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Void> to = Completable.mergeArray(Completable.complete(), pp.ignoreElements()).test();
        pp.onComplete();
        to.assertResult();
    }

    @Test
    public void completeAfterMainDelayError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Void> to = Completable.mergeArrayDelayError(Completable.complete(), pp.ignoreElements()).test();
        pp.onComplete();
        to.assertResult();
    }

    @Test
    public void errorAfterMainDelayError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Void> to = Completable.mergeArrayDelayError(Completable.complete(), pp.ignoreElements()).test();
        pp.onError(new TestException());
        to.assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Completable.merge(Flowable.just(Completable.complete())));
    }

    @Test
    public void disposePropagates() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Void> to = Completable.merge(Flowable.just(pp.ignoreElements())).test();
        assertTrue(pp.hasSubscribers());
        to.dispose();
        assertFalse(pp.hasSubscribers());
        to.assertEmpty();
    }

    @Test
    public void innerComplete() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Void> to = Completable.merge(Flowable.just(pp.ignoreElements())).test();
        pp.onComplete();
        to.assertResult();
    }

    @Test
    public void innerError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Void> to = Completable.merge(Flowable.just(pp.ignoreElements())).test();
        pp.onError(new TestException());
        to.assertFailure(TestException.class);
    }

    @Test
    public void innerErrorDelayError() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Void> to = Completable.mergeDelayError(Flowable.just(pp.ignoreElements())).test();
        pp.onError(new TestException());
        to.assertFailure(TestException.class);
    }

    @Test
    public void mainErrorInnerErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp1 = PublishProcessor.create();
                final PublishProcessor<Integer> pp2 = PublishProcessor.create();
                TestObserverEx<Void> to = Completable.merge(pp1.map(new Function<Integer, Completable>() {

                    @Override
                    public Completable apply(Integer v) throws Exception {
                        return pp2.ignoreElements();
                    }
                })).to(TestHelper.<Void>testConsumer());
                pp1.onNext(1);
                final Throwable ex1 = new TestException();
                final Throwable ex2 = new TestException();
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
                Throwable ex = to.errors().get(0);
                if (ex instanceof CompositeException) {
                    to.assertSubscribed().assertNoValues().assertNotComplete();
                    errors = TestHelper.compositeList(ex);
                    TestHelper.assertError(errors, 0, TestException.class);
                    TestHelper.assertError(errors, 1, TestException.class);
                } else {
                    to.assertFailure(TestException.class);
                    if (!errors.isEmpty()) {
                        TestHelper.assertUndeliverable(errors, 0, TestException.class);
                    }
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void mainErrorInnerErrorDelayedRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp1 = PublishProcessor.create();
            final PublishProcessor<Integer> pp2 = PublishProcessor.create();
            TestObserverEx<Void> to = Completable.mergeDelayError(pp1.map(new Function<Integer, Completable>() {

                @Override
                public Completable apply(Integer v) throws Exception {
                    return pp2.ignoreElements();
                }
            })).to(TestHelper.<Void>testConsumer());
            pp1.onNext(1);
            final Throwable ex1 = new TestException();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp1.onError(ex1);
                }
            };
            final Throwable ex2 = new TestException();
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    pp2.onError(ex2);
                }
            };
            TestHelper.race(r1, r2);
            to.assertFailure(CompositeException.class);
            List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
            TestHelper.assertError(errors, 0, TestException.class);
            TestHelper.assertError(errors, 1, TestException.class);
        }
    }

    @Test
    public void maxConcurrencyOne() {
        final PublishProcessor<Integer> pp1 = PublishProcessor.create();
        final PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Void> to = Completable.merge(Flowable.just(pp1.ignoreElements(), pp2.ignoreElements()), 1).test();
        assertTrue(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        pp1.onComplete();
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        to.assertResult();
    }

    @Test
    public void maxConcurrencyOneDelayError() {
        final PublishProcessor<Integer> pp1 = PublishProcessor.create();
        final PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Void> to = Completable.mergeDelayError(Flowable.just(pp1.ignoreElements(), pp2.ignoreElements()), 1).test();
        assertTrue(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        pp1.onComplete();
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        to.assertResult();
    }

    @Test
    public void maxConcurrencyOneDelayErrorFirst() {
        final PublishProcessor<Integer> pp1 = PublishProcessor.create();
        final PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Void> to = Completable.mergeDelayError(Flowable.just(pp1.ignoreElements(), pp2.ignoreElements()), 1).test();
        assertTrue(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        pp1.onError(new TestException());
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        to.assertFailure(TestException.class);
    }

    @Test
    public void maxConcurrencyOneDelayMainErrors() {
        final PublishProcessor<PublishProcessor<Integer>> pp0 = PublishProcessor.create();
        final PublishProcessor<Integer> pp1 = PublishProcessor.create();
        final PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Void> to = Completable.mergeDelayError(pp0.map(new Function<PublishProcessor<Integer>, Completable>() {

            @Override
            public Completable apply(PublishProcessor<Integer> v) throws Exception {
                return v.ignoreElements();
            }
        }), 1).test();
        pp0.onNext(pp1);
        assertTrue(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        pp1.onComplete();
        pp0.onNext(pp2);
        pp0.onError(new TestException());
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        to.assertFailure(TestException.class);
    }

    @Test
    public void mainDoubleOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Completable.mergeDelayError(new Flowable<Completable>() {

                @Override
                protected void subscribeActual(Subscriber<? super Completable> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(Completable.complete());
                    s.onError(new TestException("First"));
                    s.onError(new TestException("Second"));
                }
            }).to(TestHelper.<Void>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void innerDoubleOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final CompletableObserver[] o = { null };
            Completable.mergeDelayError(Flowable.just(new Completable() {

                @Override
                protected void subscribeActual(CompletableObserver observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onError(new TestException("First"));
                    o[0] = observer;
                }
            })).to(TestHelper.<Void>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            o[0].onError(new TestException("Second"));
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void innerIsDisposed() {
        final TestObserver<Void> to = new TestObserver<>();
        Completable.mergeDelayError(Flowable.just(new Completable() {

            @Override
            protected void subscribeActual(CompletableObserver observer) {
                observer.onSubscribe(Disposable.empty());
                assertFalse(((Disposable) observer).isDisposed());
                to.dispose();
                assertTrue(((Disposable) observer).isDisposed());
            }
        })).subscribe(to);
    }

    @Test
    public void mergeArrayInnerErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp1 = PublishProcessor.create();
                final PublishProcessor<Integer> pp2 = PublishProcessor.create();
                TestObserver<Void> to = Completable.mergeArray(pp1.ignoreElements(), pp2.ignoreElements()).test();
                pp1.onNext(1);
                final Throwable ex1 = new TestException();
                final Throwable ex2 = new TestException();
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
    public void delayErrorIterableCancel() {
        Completable.mergeDelayError(Arrays.asList(Completable.complete())).test(true).assertEmpty();
    }

    @Test
    public void delayErrorIterableCancelAfterHasNext() {
        final TestObserver<Void> to = new TestObserver<>();
        Completable.mergeDelayError(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                return new Iterator<Completable>() {

                    @Override
                    public boolean hasNext() {
                        to.dispose();
                        return true;
                    }

                    @Override
                    public Completable next() {
                        return Completable.complete();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        }).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void delayErrorIterableCancelAfterNext() {
        final TestObserver<Void> to = new TestObserver<>();
        Completable.mergeDelayError(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                return new Iterator<Completable>() {

                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public Completable next() {
                        to.dispose();
                        return Completable.complete();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        }).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void arrayUndeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Flowable<Integer> upstream) {
                return Completable.mergeArray(upstream.ignoreElements(), Completable.complete().hide());
            }
        });
    }

    @Test
    public void iterableUndeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Flowable<Integer> upstream) {
                return Completable.merge(Arrays.asList(upstream.ignoreElements(), Completable.complete().hide()));
            }
        });
    }

    @Test
    public void arrayUndeliverableUponCancelDelayError() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Flowable<Integer> upstream) {
                return Completable.mergeArrayDelayError(upstream.ignoreElements(), Completable.complete().hide());
            }
        });
    }

    @Test
    public void iterableUndeliverableUponCancelDelayError() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Flowable<Integer> upstream) {
                return Completable.mergeDelayError(Arrays.asList(upstream.ignoreElements(), Completable.complete().hide()));
            }
        });
    }

    @Test
    public void iterableCompleteLater() {
        CompletableSubject cs = CompletableSubject.create();
        TestObserver<Void> to = Completable.mergeDelayError(Arrays.asList(cs, cs, cs)).test();
        to.assertEmpty();
        cs.onComplete();
        to.assertResult();
    }

    @Test
    public void terminalDisposed() {
        TestHelper.checkDisposed(new CompletableMergeArrayDelayError.TryTerminateAndReportDisposable(new AtomicThrowable()));
    }

    @Test
    public void innerDisposed() {
        TestHelper.checkDisposed(new CompletableMergeArray.InnerCompletableObserver(new TestObserver<Void>(), new AtomicBoolean(), new CompositeDisposable(), 1));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.<Completable>checkDoubleOnSubscribeFlowableToCompletable(f -> Completable.merge(f));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public CompletableMergeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_invalidPrefetch() throws java.lang.Throwable {
            this.payloads.invalidPrefetch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterFirst() throws java.lang.Throwable {
            this.payloads.cancelAfterFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterFirstDelayError() throws java.lang.Throwable {
            this.payloads.cancelAfterFirstDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorAfterComplete() throws java.lang.Throwable {
            this.payloads.onErrorAfterComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeAfterMain() throws java.lang.Throwable {
            this.payloads.completeAfterMain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeAfterMainDelayError() throws java.lang.Throwable {
            this.payloads.completeAfterMainDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorAfterMainDelayError() throws java.lang.Throwable {
            this.payloads.errorAfterMainDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposePropagates() throws java.lang.Throwable {
            this.payloads.disposePropagates.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerComplete() throws java.lang.Throwable {
            this.payloads.innerComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerError() throws java.lang.Throwable {
            this.payloads.innerError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorDelayError() throws java.lang.Throwable {
            this.payloads.innerErrorDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorInnerErrorRace() throws java.lang.Throwable {
            this.payloads.mainErrorInnerErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainErrorInnerErrorDelayedRace() throws java.lang.Throwable {
            this.payloads.mainErrorInnerErrorDelayedRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maxConcurrencyOne() throws java.lang.Throwable {
            this.payloads.maxConcurrencyOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maxConcurrencyOneDelayError() throws java.lang.Throwable {
            this.payloads.maxConcurrencyOneDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maxConcurrencyOneDelayErrorFirst() throws java.lang.Throwable {
            this.payloads.maxConcurrencyOneDelayErrorFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maxConcurrencyOneDelayMainErrors() throws java.lang.Throwable {
            this.payloads.maxConcurrencyOneDelayMainErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainDoubleOnError() throws java.lang.Throwable {
            this.payloads.mainDoubleOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerDoubleOnError() throws java.lang.Throwable {
            this.payloads.innerDoubleOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerIsDisposed() throws java.lang.Throwable {
            this.payloads.innerIsDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArrayInnerErrorRace() throws java.lang.Throwable {
            this.payloads.mergeArrayInnerErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorIterableCancel() throws java.lang.Throwable {
            this.payloads.delayErrorIterableCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorIterableCancelAfterHasNext() throws java.lang.Throwable {
            this.payloads.delayErrorIterableCancelAfterHasNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorIterableCancelAfterNext() throws java.lang.Throwable {
            this.payloads.delayErrorIterableCancelAfterNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_arrayUndeliverableUponCancel() throws java.lang.Throwable {
            this.payloads.arrayUndeliverableUponCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableUndeliverableUponCancel() throws java.lang.Throwable {
            this.payloads.iterableUndeliverableUponCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_arrayUndeliverableUponCancelDelayError() throws java.lang.Throwable {
            this.payloads.arrayUndeliverableUponCancelDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableUndeliverableUponCancelDelayError() throws java.lang.Throwable {
            this.payloads.iterableUndeliverableUponCancelDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterableCompleteLater() throws java.lang.Throwable {
            this.payloads.iterableCompleteLater.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_terminalDisposed() throws java.lang.Throwable {
            this.payloads.terminalDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerDisposed() throws java.lang.Throwable {
            this.payloads.innerDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableMergeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableMergeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableMergeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableMergeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableMergeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableMergeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableMergeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableMergeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement invalidPrefetch;

            public org.junit.runners.model.Statement cancelAfterFirst;

            public org.junit.runners.model.Statement cancelAfterFirstDelayError;

            public org.junit.runners.model.Statement onErrorAfterComplete;

            public org.junit.runners.model.Statement completeAfterMain;

            public org.junit.runners.model.Statement completeAfterMainDelayError;

            public org.junit.runners.model.Statement errorAfterMainDelayError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement disposePropagates;

            public org.junit.runners.model.Statement innerComplete;

            public org.junit.runners.model.Statement innerError;

            public org.junit.runners.model.Statement innerErrorDelayError;

            public org.junit.runners.model.Statement mainErrorInnerErrorRace;

            public org.junit.runners.model.Statement mainErrorInnerErrorDelayedRace;

            public org.junit.runners.model.Statement maxConcurrencyOne;

            public org.junit.runners.model.Statement maxConcurrencyOneDelayError;

            public org.junit.runners.model.Statement maxConcurrencyOneDelayErrorFirst;

            public org.junit.runners.model.Statement maxConcurrencyOneDelayMainErrors;

            public org.junit.runners.model.Statement mainDoubleOnError;

            public org.junit.runners.model.Statement innerDoubleOnError;

            public org.junit.runners.model.Statement innerIsDisposed;

            public org.junit.runners.model.Statement mergeArrayInnerErrorRace;

            public org.junit.runners.model.Statement delayErrorIterableCancel;

            public org.junit.runners.model.Statement delayErrorIterableCancelAfterHasNext;

            public org.junit.runners.model.Statement delayErrorIterableCancelAfterNext;

            public org.junit.runners.model.Statement arrayUndeliverableUponCancel;

            public org.junit.runners.model.Statement iterableUndeliverableUponCancel;

            public org.junit.runners.model.Statement arrayUndeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement iterableUndeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement iterableCompleteLater;

            public org.junit.runners.model.Statement terminalDisposed;

            public org.junit.runners.model.Statement innerDisposed;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.invalidPrefetch = _ClassStatement.forPayload(CompletableMergeTest::invalidPrefetch, "invalidPrefetch", this);
            this.payloads.cancelAfterFirst = _ClassStatement.forPayload(CompletableMergeTest::cancelAfterFirst, "cancelAfterFirst", this);
            this.payloads.cancelAfterFirstDelayError = _ClassStatement.forPayload(CompletableMergeTest::cancelAfterFirstDelayError, "cancelAfterFirstDelayError", this);
            this.payloads.onErrorAfterComplete = _ClassStatement.forPayload(CompletableMergeTest::onErrorAfterComplete, "onErrorAfterComplete", this);
            this.payloads.completeAfterMain = _ClassStatement.forPayload(CompletableMergeTest::completeAfterMain, "completeAfterMain", this);
            this.payloads.completeAfterMainDelayError = _ClassStatement.forPayload(CompletableMergeTest::completeAfterMainDelayError, "completeAfterMainDelayError", this);
            this.payloads.errorAfterMainDelayError = _ClassStatement.forPayload(CompletableMergeTest::errorAfterMainDelayError, "errorAfterMainDelayError", this);
            this.payloads.dispose = _ClassStatement.forPayload(CompletableMergeTest::dispose, "dispose", this);
            this.payloads.disposePropagates = _ClassStatement.forPayload(CompletableMergeTest::disposePropagates, "disposePropagates", this);
            this.payloads.innerComplete = _ClassStatement.forPayload(CompletableMergeTest::innerComplete, "innerComplete", this);
            this.payloads.innerError = _ClassStatement.forPayload(CompletableMergeTest::innerError, "innerError", this);
            this.payloads.innerErrorDelayError = _ClassStatement.forPayload(CompletableMergeTest::innerErrorDelayError, "innerErrorDelayError", this);
            this.payloads.mainErrorInnerErrorRace = _ClassStatement.forPayload(CompletableMergeTest::mainErrorInnerErrorRace, "mainErrorInnerErrorRace", this);
            this.payloads.mainErrorInnerErrorDelayedRace = _ClassStatement.forPayload(CompletableMergeTest::mainErrorInnerErrorDelayedRace, "mainErrorInnerErrorDelayedRace", this);
            this.payloads.maxConcurrencyOne = _ClassStatement.forPayload(CompletableMergeTest::maxConcurrencyOne, "maxConcurrencyOne", this);
            this.payloads.maxConcurrencyOneDelayError = _ClassStatement.forPayload(CompletableMergeTest::maxConcurrencyOneDelayError, "maxConcurrencyOneDelayError", this);
            this.payloads.maxConcurrencyOneDelayErrorFirst = _ClassStatement.forPayload(CompletableMergeTest::maxConcurrencyOneDelayErrorFirst, "maxConcurrencyOneDelayErrorFirst", this);
            this.payloads.maxConcurrencyOneDelayMainErrors = _ClassStatement.forPayload(CompletableMergeTest::maxConcurrencyOneDelayMainErrors, "maxConcurrencyOneDelayMainErrors", this);
            this.payloads.mainDoubleOnError = _ClassStatement.forPayload(CompletableMergeTest::mainDoubleOnError, "mainDoubleOnError", this);
            this.payloads.innerDoubleOnError = _ClassStatement.forPayload(CompletableMergeTest::innerDoubleOnError, "innerDoubleOnError", this);
            this.payloads.innerIsDisposed = _ClassStatement.forPayload(CompletableMergeTest::innerIsDisposed, "innerIsDisposed", this);
            this.payloads.mergeArrayInnerErrorRace = _ClassStatement.forPayload(CompletableMergeTest::mergeArrayInnerErrorRace, "mergeArrayInnerErrorRace", this);
            this.payloads.delayErrorIterableCancel = _ClassStatement.forPayload(CompletableMergeTest::delayErrorIterableCancel, "delayErrorIterableCancel", this);
            this.payloads.delayErrorIterableCancelAfterHasNext = _ClassStatement.forPayload(CompletableMergeTest::delayErrorIterableCancelAfterHasNext, "delayErrorIterableCancelAfterHasNext", this);
            this.payloads.delayErrorIterableCancelAfterNext = _ClassStatement.forPayload(CompletableMergeTest::delayErrorIterableCancelAfterNext, "delayErrorIterableCancelAfterNext", this);
            this.payloads.arrayUndeliverableUponCancel = _ClassStatement.forPayload(CompletableMergeTest::arrayUndeliverableUponCancel, "arrayUndeliverableUponCancel", this);
            this.payloads.iterableUndeliverableUponCancel = _ClassStatement.forPayload(CompletableMergeTest::iterableUndeliverableUponCancel, "iterableUndeliverableUponCancel", this);
            this.payloads.arrayUndeliverableUponCancelDelayError = _ClassStatement.forPayload(CompletableMergeTest::arrayUndeliverableUponCancelDelayError, "arrayUndeliverableUponCancelDelayError", this);
            this.payloads.iterableUndeliverableUponCancelDelayError = _ClassStatement.forPayload(CompletableMergeTest::iterableUndeliverableUponCancelDelayError, "iterableUndeliverableUponCancelDelayError", this);
            this.payloads.iterableCompleteLater = _ClassStatement.forPayload(CompletableMergeTest::iterableCompleteLater, "iterableCompleteLater", this);
            this.payloads.terminalDisposed = _ClassStatement.forPayload(CompletableMergeTest::terminalDisposed, "terminalDisposed", this);
            this.payloads.innerDisposed = _ClassStatement.forPayload(CompletableMergeTest::innerDisposed, "innerDisposed", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(CompletableMergeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
