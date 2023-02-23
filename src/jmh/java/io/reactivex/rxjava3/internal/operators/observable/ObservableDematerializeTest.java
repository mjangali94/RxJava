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
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableDematerializeTest extends RxJavaTest {

    @Test
    public void simpleSelector() {
        Observable<Notification<Integer>> notifications = Observable.just(1, 2).materialize();
        Observable<Integer> dematerialize = notifications.dematerialize(Functions.<Notification<Integer>>identity());
        Observer<Integer> observer = TestHelper.mockObserver();
        dematerialize.subscribe(observer);
        verify(observer, times(1)).onNext(1);
        verify(observer, times(1)).onNext(2);
        verify(observer, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void selectorCrash() {
        Observable.just(1, 2).materialize().dematerialize(new Function<Notification<Integer>, Notification<Object>>() {

            @Override
            public Notification<Object> apply(Notification<Integer> v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void selectorNull() {
        Observable.just(1, 2).materialize().dematerialize(new Function<Notification<Integer>, Notification<Object>>() {

            @Override
            public Notification<Object> apply(Notification<Integer> v) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void dematerialize1() {
        Observable<Notification<Integer>> notifications = Observable.just(1, 2).materialize();
        Observable<Integer> dematerialize = notifications.dematerialize(Functions.<Notification<Integer>>identity());
        Observer<Integer> observer = TestHelper.mockObserver();
        dematerialize.subscribe(observer);
        verify(observer, times(1)).onNext(1);
        verify(observer, times(1)).onNext(2);
        verify(observer, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void dematerialize2() {
        Throwable exception = new Throwable("test");
        Observable<Integer> o = Observable.error(exception);
        Observable<Integer> dematerialize = o.materialize().dematerialize(Functions.<Notification<Integer>>identity());
        Observer<Integer> observer = TestHelper.mockObserver();
        dematerialize.subscribe(observer);
        verify(observer, times(1)).onError(exception);
        verify(observer, times(0)).onComplete();
        verify(observer, times(0)).onNext(any(Integer.class));
    }

    @Test
    public void dematerialize3() {
        Exception exception = new Exception("test");
        Observable<Integer> o = Observable.error(exception);
        Observable<Integer> dematerialize = o.materialize().dematerialize(Functions.<Notification<Integer>>identity());
        Observer<Integer> observer = TestHelper.mockObserver();
        dematerialize.subscribe(observer);
        verify(observer, times(1)).onError(exception);
        verify(observer, times(0)).onComplete();
        verify(observer, times(0)).onNext(any(Integer.class));
    }

    @Test
    public void errorPassThru() {
        Exception exception = new Exception("test");
        Observable<Notification<Integer>> o = Observable.error(exception);
        Observable<Integer> dematerialize = o.dematerialize(Functions.<Notification<Integer>>identity());
        Observer<Integer> observer = TestHelper.mockObserver();
        dematerialize.subscribe(observer);
        verify(observer, times(1)).onError(exception);
        verify(observer, times(0)).onComplete();
        verify(observer, times(0)).onNext(any(Integer.class));
    }

    @Test
    public void completePassThru() {
        Observable<Notification<Integer>> o = Observable.empty();
        Observable<Integer> dematerialize = o.dematerialize(Functions.<Notification<Integer>>identity());
        Observer<Integer> observer = TestHelper.mockObserver();
        TestObserverEx<Integer> to = new TestObserverEx<>(observer);
        dematerialize.subscribe(to);
        // System.out.println(to.errors());
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
        verify(observer, times(0)).onNext(any(Integer.class));
    }

    @Test
    public void honorsContractWhenCompleted() {
        Observable<Integer> source = Observable.just(1);
        Observable<Integer> result = source.materialize().dematerialize(Functions.<Notification<Integer>>identity());
        Observer<Integer> o = TestHelper.mockObserver();
        result.subscribe(o);
        verify(o).onNext(1);
        verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void honorsContractWhenThrows() {
        Observable<Integer> source = Observable.error(new TestException());
        Observable<Integer> result = source.materialize().dematerialize(Functions.<Notification<Integer>>identity());
        Observer<Integer> o = TestHelper.mockObserver();
        result.subscribe(o);
        verify(o, never()).onNext(any(Integer.class));
        verify(o, never()).onComplete();
        verify(o).onError(any(TestException.class));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(Notification.<Integer>createOnComplete()).dematerialize(Functions.<Notification<Integer>>identity()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Notification<Object>>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Notification<Object>> o) throws Exception {
                return o.dematerialize(Functions.<Notification<Object>>identity());
            }
        });
    }

    @Test
    public void eventsAfterDematerializedTerminal() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Notification<Object>>() {

                @Override
                protected void subscribeActual(Observer<? super Notification<Object>> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(Notification.createOnComplete());
                    observer.onNext(Notification.<Object>createOnNext(1));
                    observer.onNext(Notification.createOnError(new TestException("First")));
                    observer.onError(new TestException("Second"));
                }
            }.dematerialize(Functions.<Notification<Object>>identity()).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 1, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void nonNotificationInstanceAfterDispose() {
        new Observable<Object>() {

            @Override
            protected void subscribeActual(Observer<? super Object> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext(Notification.createOnComplete());
                observer.onNext(1);
            }
        }.dematerialize(v -> (Notification<Object>) v).test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableDematerializeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleSelector() throws java.lang.Throwable {
            this.payloads.simpleSelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorCrash() throws java.lang.Throwable {
            this.payloads.selectorCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorNull() throws java.lang.Throwable {
            this.payloads.selectorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dematerialize1() throws java.lang.Throwable {
            this.payloads.dematerialize1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dematerialize2() throws java.lang.Throwable {
            this.payloads.dematerialize2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dematerialize3() throws java.lang.Throwable {
            this.payloads.dematerialize3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorPassThru() throws java.lang.Throwable {
            this.payloads.errorPassThru.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completePassThru() throws java.lang.Throwable {
            this.payloads.completePassThru.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_honorsContractWhenCompleted() throws java.lang.Throwable {
            this.payloads.honorsContractWhenCompleted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_honorsContractWhenThrows() throws java.lang.Throwable {
            this.payloads.honorsContractWhenThrows.evaluate();
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
        public void benchmark_eventsAfterDematerializedTerminal() throws java.lang.Throwable {
            this.payloads.eventsAfterDematerializedTerminal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonNotificationInstanceAfterDispose() throws java.lang.Throwable {
            this.payloads.nonNotificationInstanceAfterDispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDematerializeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDematerializeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDematerializeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDematerializeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableDematerializeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDematerializeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableDematerializeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableDematerializeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement simpleSelector;

            public org.junit.runners.model.Statement selectorCrash;

            public org.junit.runners.model.Statement selectorNull;

            public org.junit.runners.model.Statement dematerialize1;

            public org.junit.runners.model.Statement dematerialize2;

            public org.junit.runners.model.Statement dematerialize3;

            public org.junit.runners.model.Statement errorPassThru;

            public org.junit.runners.model.Statement completePassThru;

            public org.junit.runners.model.Statement honorsContractWhenCompleted;

            public org.junit.runners.model.Statement honorsContractWhenThrows;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement eventsAfterDematerializedTerminal;

            public org.junit.runners.model.Statement nonNotificationInstanceAfterDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.simpleSelector = _ClassStatement.forPayload(ObservableDematerializeTest::simpleSelector, "simpleSelector", this);
            this.payloads.selectorCrash = _ClassStatement.forPayload(ObservableDematerializeTest::selectorCrash, "selectorCrash", this);
            this.payloads.selectorNull = _ClassStatement.forPayload(ObservableDematerializeTest::selectorNull, "selectorNull", this);
            this.payloads.dematerialize1 = _ClassStatement.forPayload(ObservableDematerializeTest::dematerialize1, "dematerialize1", this);
            this.payloads.dematerialize2 = _ClassStatement.forPayload(ObservableDematerializeTest::dematerialize2, "dematerialize2", this);
            this.payloads.dematerialize3 = _ClassStatement.forPayload(ObservableDematerializeTest::dematerialize3, "dematerialize3", this);
            this.payloads.errorPassThru = _ClassStatement.forPayload(ObservableDematerializeTest::errorPassThru, "errorPassThru", this);
            this.payloads.completePassThru = _ClassStatement.forPayload(ObservableDematerializeTest::completePassThru, "completePassThru", this);
            this.payloads.honorsContractWhenCompleted = _ClassStatement.forPayload(ObservableDematerializeTest::honorsContractWhenCompleted, "honorsContractWhenCompleted", this);
            this.payloads.honorsContractWhenThrows = _ClassStatement.forPayload(ObservableDematerializeTest::honorsContractWhenThrows, "honorsContractWhenThrows", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableDematerializeTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableDematerializeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.eventsAfterDematerializedTerminal = _ClassStatement.forPayload(ObservableDematerializeTest::eventsAfterDematerializedTerminal, "eventsAfterDematerializedTerminal", this);
            this.payloads.nonNotificationInstanceAfterDispose = _ClassStatement.forPayload(ObservableDematerializeTest::nonNotificationInstanceAfterDispose, "nonNotificationInstanceAfterDispose", this);
        }
    }
}
