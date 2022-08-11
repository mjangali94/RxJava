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

import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableConcatMapCompletableTest extends RxJavaTest {

    @Test
    public void asyncFused() throws Exception {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestObserver<Void> to = us.concatMapCompletable(completableComplete(), 2).test();
        us.onNext(1);
        us.onComplete();
        to.assertComplete();
        to.assertValueCount(0);
    }

    @Test
    public void notFused() throws Exception {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestObserver<Void> to = us.hide().concatMapCompletable(completableComplete(), 2).test();
        us.onNext(1);
        us.onNext(2);
        us.onComplete();
        to.assertComplete();
        to.assertValueCount(0);
        to.assertNoErrors();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.<Integer>just(1).hide().concatMapCompletable(completableError()));
    }

    @Test
    public void mainError() {
        Observable.<Integer>error(new TestException()).concatMapCompletable(completableComplete()).test().assertFailure(TestException.class);
    }

    @Test
    public void innerError() {
        Observable.<Integer>just(1).hide().concatMapCompletable(completableError()).test().assertFailure(TestException.class);
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onComplete();
                    observer.onNext(2);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.concatMapCompletable(completableComplete()).test().assertComplete();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> ps1 = PublishSubject.create();
                final PublishSubject<Integer> ps2 = PublishSubject.create();
                TestObserver<Void> to = ps1.concatMapCompletable(new Function<Integer, CompletableSource>() {

                    @Override
                    public CompletableSource apply(Integer v) throws Exception {
                        return Completable.fromObservable(ps2);
                    }
                }).test();
                final TestException ex1 = new TestException();
                final TestException ex2 = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps1.onError(ex1);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ps2.onError(ex2);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertFailure(TestException.class);
                if (!errors.isEmpty()) {
                    TestHelper.assertError(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void mapperThrows() {
        Observable.just(1).hide().concatMapCompletable(completableThrows()).test().assertFailure(TestException.class);
    }

    @Test
    public void fusedPollThrows() {
        Observable.just(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).concatMapCompletable(completableComplete()).test().assertFailure(TestException.class);
    }

    @Test
    public void concatReportsDisposedOnComplete() {
        final Disposable[] disposable = { null };
        Observable.just(1).hide().concatMapCompletable(completableComplete()).subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
                disposable[0] = d;
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
        assertTrue(disposable[0].isDisposed());
    }

    @Test
    public void concatReportsDisposedOnError() {
        final Disposable[] disposable = { null };
        Observable.just(1).hide().concatMapCompletable(completableError()).subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
                disposable[0] = d;
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
        assertTrue(disposable[0].isDisposed());
    }

    private Function<Integer, CompletableSource> completableComplete() {
        return new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        };
    }

    private Function<Integer, CompletableSource> completableError() {
        return new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        };
    }

    private Function<Integer, CompletableSource> completableThrows() {
        return new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        };
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableConcatMapCompletableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFused() throws java.lang.Throwable {
            this.payloads.asyncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notFused() throws java.lang.Throwable {
            this.payloads.notFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
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
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorRace() throws java.lang.Throwable {
            this.payloads.onErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrows() throws java.lang.Throwable {
            this.payloads.mapperThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPollThrows() throws java.lang.Throwable {
            this.payloads.fusedPollThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatReportsDisposedOnComplete() throws java.lang.Throwable {
            this.payloads.concatReportsDisposedOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatReportsDisposedOnError() throws java.lang.Throwable {
            this.payloads.concatReportsDisposedOnError.evaluate();
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

            public org.junit.runners.model.Statement asyncFused;

            public org.junit.runners.model.Statement notFused;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement mainError;

            public org.junit.runners.model.Statement innerError;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement onErrorRace;

            public org.junit.runners.model.Statement mapperThrows;

            public org.junit.runners.model.Statement fusedPollThrows;

            public org.junit.runners.model.Statement concatReportsDisposedOnComplete;

            public org.junit.runners.model.Statement concatReportsDisposedOnError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.asyncFused = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::asyncFused, "asyncFused", this);
            this.payloads.notFused = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::notFused, "notFused", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::dispose, "dispose", this);
            this.payloads.mainError = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::mainError, "mainError", this);
            this.payloads.innerError = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::innerError, "innerError", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::badSource, "badSource", this);
            this.payloads.onErrorRace = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::onErrorRace, "onErrorRace", this);
            this.payloads.mapperThrows = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::mapperThrows, "mapperThrows", this);
            this.payloads.fusedPollThrows = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::fusedPollThrows, "fusedPollThrows", this);
            this.payloads.concatReportsDisposedOnComplete = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::concatReportsDisposedOnComplete, "concatReportsDisposedOnComplete", this);
            this.payloads.concatReportsDisposedOnError = _ClassStatement.forPayload(ObservableConcatMapCompletableTest::concatReportsDisposedOnError, "concatReportsDisposedOnError", this);
        }
    }
}
