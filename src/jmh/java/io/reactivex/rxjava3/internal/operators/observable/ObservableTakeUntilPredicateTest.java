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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableTakeUntilPredicateTest extends RxJavaTest {

    @Test
    public void takeEmpty() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.empty().takeUntil(new Predicate<Object>() {

            @Override
            public boolean test(Object v) {
                return true;
            }
        }).subscribe(o);
        verify(o, never()).onNext(any());
        verify(o, never()).onError(any(Throwable.class));
        verify(o).onComplete();
    }

    @Test
    public void takeAll() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.just(1, 2).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return false;
            }
        }).subscribe(o);
        verify(o).onNext(1);
        verify(o).onNext(2);
        verify(o, never()).onError(any(Throwable.class));
        verify(o).onComplete();
    }

    @Test
    public void takeFirst() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.just(1, 2).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).subscribe(o);
        verify(o).onNext(1);
        verify(o, never()).onNext(2);
        verify(o, never()).onError(any(Throwable.class));
        verify(o).onComplete();
    }

    @Test
    public void takeSome() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.just(1, 2, 3).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 == 2;
            }
        }).subscribe(o);
        verify(o).onNext(1);
        verify(o).onNext(2);
        verify(o, never()).onNext(3);
        verify(o, never()).onError(any(Throwable.class));
        verify(o).onComplete();
    }

    @Test
    public void functionThrows() {
        Observer<Object> o = TestHelper.mockObserver();
        Predicate<Integer> predicate = (new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                throw new TestException("Forced failure");
            }
        });
        Observable.just(1, 2, 3).takeUntil(predicate).subscribe(o);
        verify(o).onNext(1);
        verify(o, never()).onNext(2);
        verify(o, never()).onNext(3);
        verify(o).onError(any(TestException.class));
        verify(o, never()).onComplete();
    }

    @Test
    public void sourceThrows() {
        Observer<Object> o = TestHelper.mockObserver();
        Observable.just(1).concatWith(Observable.<Integer>error(new TestException())).concatWith(Observable.just(2)).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return false;
            }
        }).subscribe(o);
        verify(o).onNext(1);
        verify(o, never()).onNext(2);
        verify(o).onError(any(TestException.class));
        verify(o, never()).onComplete();
    }

    @Test
    public void errorIncludesLastValueAsCause() {
        TestObserverEx<String> to = new TestObserverEx<>();
        final TestException e = new TestException("Forced failure");
        Predicate<String> predicate = (new Predicate<String>() {

            @Override
            public boolean test(String t) {
                throw e;
            }
        });
        Observable.just("abc").takeUntil(predicate).subscribe(to);
        to.assertTerminated();
        to.assertNotComplete();
        to.assertError(TestException.class);
    // FIXME last cause value is not saved
    // assertTrue(ts.errors().get(0).getCause().getMessage().contains("abc"));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().takeUntil(Functions.alwaysFalse()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.takeUntil(Functions.alwaysFalse());
            }
        });
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onComplete();
                    observer.onNext(1);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.takeUntil(Functions.alwaysFalse()).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableTakeUntilPredicateTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeEmpty() throws java.lang.Throwable {
            this.payloads.takeEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeAll() throws java.lang.Throwable {
            this.payloads.takeAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeFirst() throws java.lang.Throwable {
            this.payloads.takeFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeSome() throws java.lang.Throwable {
            this.payloads.takeSome.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_functionThrows() throws java.lang.Throwable {
            this.payloads.functionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceThrows() throws java.lang.Throwable {
            this.payloads.sourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorIncludesLastValueAsCause() throws java.lang.Throwable {
            this.payloads.errorIncludesLastValueAsCause.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeUntilPredicateTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeUntilPredicateTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeUntilPredicateTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeUntilPredicateTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableTakeUntilPredicateTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeUntilPredicateTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableTakeUntilPredicateTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableTakeUntilPredicateTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement takeEmpty;

            public org.junit.runners.model.Statement takeAll;

            public org.junit.runners.model.Statement takeFirst;

            public org.junit.runners.model.Statement takeSome;

            public org.junit.runners.model.Statement functionThrows;

            public org.junit.runners.model.Statement sourceThrows;

            public org.junit.runners.model.Statement errorIncludesLastValueAsCause;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badSource;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.takeEmpty = _ClassStatement.forPayload(ObservableTakeUntilPredicateTest::takeEmpty, "takeEmpty", this);
            this.payloads.takeAll = _ClassStatement.forPayload(ObservableTakeUntilPredicateTest::takeAll, "takeAll", this);
            this.payloads.takeFirst = _ClassStatement.forPayload(ObservableTakeUntilPredicateTest::takeFirst, "takeFirst", this);
            this.payloads.takeSome = _ClassStatement.forPayload(ObservableTakeUntilPredicateTest::takeSome, "takeSome", this);
            this.payloads.functionThrows = _ClassStatement.forPayload(ObservableTakeUntilPredicateTest::functionThrows, "functionThrows", this);
            this.payloads.sourceThrows = _ClassStatement.forPayload(ObservableTakeUntilPredicateTest::sourceThrows, "sourceThrows", this);
            this.payloads.errorIncludesLastValueAsCause = _ClassStatement.forPayload(ObservableTakeUntilPredicateTest::errorIncludesLastValueAsCause, "errorIncludesLastValueAsCause", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableTakeUntilPredicateTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableTakeUntilPredicateTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableTakeUntilPredicateTest::badSource, "badSource", this);
        }
    }
}
