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
package io.reactivex.rxjava3.internal.operators.observable;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableMergeWithSingleTest extends RxJavaTest {

    @Test
    public void normal() {
        Observable.range(1, 5).mergeWith(Single.just(100)).test().assertResult(1, 2, 3, 4, 5, 100);
    }

    @Test
    public void normalLong() {
        Observable.range(1, 512).mergeWith(Single.just(100)).test().assertValueCount(513).assertComplete();
    }

    @Test
    public void take() {
        Observable.range(1, 5).mergeWith(Single.just(100)).take(3).test().assertResult(1, 2, 3);
    }

    @Test
    public void cancel() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        final SingleSubject<Integer> cs = SingleSubject.create();
        TestObserver<Integer> to = ps.mergeWith(cs).test();
        assertTrue(ps.hasObservers());
        assertTrue(cs.hasObservers());
        to.dispose();
        assertFalse(ps.hasObservers());
        assertFalse(cs.hasObservers());
    }

    @Test
    public void mainError() {
        Observable.error(new TestException()).mergeWith(Single.just(100)).test().assertFailure(TestException.class);
    }

    @Test
    public void otherError() {
        Observable.never().mergeWith(Single.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void completeRace() {
        for (int i = 0; i < 10000; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final SingleSubject<Integer> cs = SingleSubject.create();
            TestObserver<Integer> to = ps.mergeWith(cs).test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onNext(1);
                    ps.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    cs.onSuccess(1);
                }
            };
            TestHelper.race(r1, r2);
            to.assertResult(1, 1);
        }
    }

    @Test
    public void onNextSlowPath() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        final SingleSubject<Integer> cs = SingleSubject.create();
        TestObserver<Integer> to = ps.mergeWith(cs).subscribeWith(new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    ps.onNext(2);
                }
            }
        });
        ps.onNext(1);
        cs.onSuccess(3);
        ps.onNext(4);
        ps.onComplete();
        to.assertResult(1, 2, 3, 4);
    }

    @Test
    public void onSuccessSlowPath() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        final SingleSubject<Integer> cs = SingleSubject.create();
        TestObserver<Integer> to = ps.mergeWith(cs).subscribeWith(new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    cs.onSuccess(2);
                }
            }
        });
        ps.onNext(1);
        ps.onNext(3);
        ps.onComplete();
        to.assertResult(1, 2, 3);
    }

    @Test
    public void onErrorMainOverflow() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final AtomicReference<Observer<?>> observerRef = new AtomicReference<>();
            TestObserver<Integer> to = new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observerRef.set(observer);
                }
            }.mergeWith(Single.<Integer>error(new IOException())).test();
            observerRef.get().onError(new TestException());
            to.assertFailure(IOException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onErrorOtherOverflow() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Observable.error(new IOException()).mergeWith(Single.error(new TestException())).test().assertFailure(IOException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doubleOnSubscribeMain() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> f) throws Exception {
                return f.mergeWith(Single.just(1));
            }
        });
    }

    @Test
    public void isDisposed() {
        new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                assertFalse(((Disposable) observer).isDisposed());
                observer.onNext(1);
                assertTrue(((Disposable) observer).isDisposed());
            }
        }.mergeWith(Single.<Integer>just(1)).take(1).test().assertResult(1);
    }

    @Test
    public void onNextSlowPathCreateQueue() {
        final PublishSubject<Integer> ps = PublishSubject.create();
        final SingleSubject<Integer> cs = SingleSubject.create();
        TestObserver<Integer> to = ps.mergeWith(cs).subscribeWith(new TestObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    ps.onNext(2);
                    ps.onNext(3);
                }
            }
        });
        cs.onSuccess(0);
        ps.onNext(1);
        ps.onNext(4);
        ps.onComplete();
        to.assertResult(0, 1, 2, 3, 4);
    }

    @Test
    public void cancelOtherOnMainError() {
        PublishSubject<Integer> ps = PublishSubject.create();
        SingleSubject<Integer> ss = SingleSubject.create();
        TestObserver<Integer> to = ps.mergeWith(ss).test();
        assertTrue(ps.hasObservers());
        assertTrue(ss.hasObservers());
        ps.onError(new TestException());
        to.assertFailure(TestException.class);
        assertFalse("main has observers!", ps.hasObservers());
        assertFalse("other has observers", ss.hasObservers());
    }

    @Test
    public void cancelMainOnOtherError() {
        PublishSubject<Integer> ps = PublishSubject.create();
        SingleSubject<Integer> ss = SingleSubject.create();
        TestObserver<Integer> to = ps.mergeWith(ss).test();
        assertTrue(ps.hasObservers());
        assertTrue(ss.hasObservers());
        ss.onError(new TestException());
        to.assertFailure(TestException.class);
        assertFalse("main has observers!", ps.hasObservers());
        assertFalse("other has observers", ss.hasObservers());
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new ObservableConverter<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> upstream) {
                return upstream.mergeWith(Single.just(1).hide());
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableMergeWithSingleTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalLong() throws java.lang.Throwable {
            this.payloads.normalLong.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
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
        public void benchmark_completeRace() throws java.lang.Throwable {
            this.payloads.completeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextSlowPath() throws java.lang.Throwable {
            this.payloads.onNextSlowPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessSlowPath() throws java.lang.Throwable {
            this.payloads.onSuccessSlowPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorMainOverflow() throws java.lang.Throwable {
            this.payloads.onErrorMainOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorOtherOverflow() throws java.lang.Throwable {
            this.payloads.onErrorOtherOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeMain() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribeMain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposed() throws java.lang.Throwable {
            this.payloads.isDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextSlowPathCreateQueue() throws java.lang.Throwable {
            this.payloads.onNextSlowPathCreateQueue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOtherOnMainError() throws java.lang.Throwable {
            this.payloads.cancelOtherOnMainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelMainOnOtherError() throws java.lang.Throwable {
            this.payloads.cancelMainOnOtherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancel.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMergeWithSingleTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMergeWithSingleTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMergeWithSingleTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMergeWithSingleTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableMergeWithSingleTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMergeWithSingleTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableMergeWithSingleTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableMergeWithSingleTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement normalLong;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement otherError;

            public org.junit.runners.model.Statement completeRace;

            public org.junit.runners.model.Statement onNextSlowPath;

            public org.junit.runners.model.Statement onSuccessSlowPath;

            public org.junit.runners.model.Statement onErrorMainOverflow;

            public org.junit.runners.model.Statement onErrorOtherOverflow;

            public org.junit.runners.model.Statement doubleOnSubscribeMain;

            public org.junit.runners.model.Statement isDisposed;

            public org.junit.runners.model.Statement onNextSlowPathCreateQueue;

            public org.junit.runners.model.Statement cancelOtherOnMainError;

            public org.junit.runners.model.Statement cancelMainOnOtherError;

            public org.junit.runners.model.Statement undeliverableUponCancel;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(ObservableMergeWithSingleTest::normal, "normal", this);
            this.payloads.normalLong = _ClassStatement.forPayload(ObservableMergeWithSingleTest::normalLong, "normalLong", this);
            this.payloads.take = _ClassStatement.forPayload(ObservableMergeWithSingleTest::take, "take", this);
            this.payloads.cancel = _ClassStatement.forPayload(ObservableMergeWithSingleTest::cancel, "cancel", this);
            this.payloads.mainError = _ClassStatement.forPayload(ObservableMergeWithSingleTest::mainError, "mainError", this);
            this.payloads.otherError = _ClassStatement.forPayload(ObservableMergeWithSingleTest::otherError, "otherError", this);
            this.payloads.completeRace = _ClassStatement.forPayload(ObservableMergeWithSingleTest::completeRace, "completeRace", this);
            this.payloads.onNextSlowPath = _ClassStatement.forPayload(ObservableMergeWithSingleTest::onNextSlowPath, "onNextSlowPath", this);
            this.payloads.onSuccessSlowPath = _ClassStatement.forPayload(ObservableMergeWithSingleTest::onSuccessSlowPath, "onSuccessSlowPath", this);
            this.payloads.onErrorMainOverflow = _ClassStatement.forPayload(ObservableMergeWithSingleTest::onErrorMainOverflow, "onErrorMainOverflow", this);
            this.payloads.onErrorOtherOverflow = _ClassStatement.forPayload(ObservableMergeWithSingleTest::onErrorOtherOverflow, "onErrorOtherOverflow", this);
            this.payloads.doubleOnSubscribeMain = _ClassStatement.forPayload(ObservableMergeWithSingleTest::doubleOnSubscribeMain, "doubleOnSubscribeMain", this);
            this.payloads.isDisposed = _ClassStatement.forPayload(ObservableMergeWithSingleTest::isDisposed, "isDisposed", this);
            this.payloads.onNextSlowPathCreateQueue = _ClassStatement.forPayload(ObservableMergeWithSingleTest::onNextSlowPathCreateQueue, "onNextSlowPathCreateQueue", this);
            this.payloads.cancelOtherOnMainError = _ClassStatement.forPayload(ObservableMergeWithSingleTest::cancelOtherOnMainError, "cancelOtherOnMainError", this);
            this.payloads.cancelMainOnOtherError = _ClassStatement.forPayload(ObservableMergeWithSingleTest::cancelMainOnOtherError, "cancelMainOnOtherError", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(ObservableMergeWithSingleTest::undeliverableUponCancel, "undeliverableUponCancel", this);
        }
    }
}
