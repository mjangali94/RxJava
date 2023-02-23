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

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableTakeWhileTest extends RxJavaTest {

    @Test
    public void takeWhile1() {
        Observable<Integer> w = Observable.just(1, 2, 3);
        Observable<Integer> take = w.takeWhile(new Predicate<Integer>() {

            @Override
            public boolean test(Integer input) {
                return input < 3;
            }
        });
        Observer<Integer> observer = TestHelper.mockObserver();
        take.subscribe(observer);
        verify(observer, times(1)).onNext(1);
        verify(observer, times(1)).onNext(2);
        verify(observer, never()).onNext(3);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void takeWhileOnSubject1() {
        Subject<Integer> s = PublishSubject.create();
        Observable<Integer> take = s.takeWhile(new Predicate<Integer>() {

            @Override
            public boolean test(Integer input) {
                return input < 3;
            }
        });
        Observer<Integer> observer = TestHelper.mockObserver();
        take.subscribe(observer);
        s.onNext(1);
        s.onNext(2);
        s.onNext(3);
        s.onNext(4);
        s.onNext(5);
        s.onComplete();
        verify(observer, times(1)).onNext(1);
        verify(observer, times(1)).onNext(2);
        verify(observer, never()).onNext(3);
        verify(observer, never()).onNext(4);
        verify(observer, never()).onNext(5);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void takeWhile2() {
        Observable<String> w = Observable.just("one", "two", "three");
        Observable<String> take = w.takeWhile(new Predicate<String>() {

            int index;

            @Override
            public boolean test(String input) {
                return index++ < 2;
            }
        });
        Observer<String> observer = TestHelper.mockObserver();
        take.subscribe(observer);
        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, never()).onNext("three");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    @SuppressUndeliverable
    public void takeWhileDoesntLeakErrors() {
        Observable<String> source = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext("one");
                observer.onError(new Throwable("test failed"));
            }
        });
        source.takeWhile(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return false;
            }
        }).blockingLast("");
    }

    @Test
    public void takeWhileProtectsPredicateCall() {
        TestObservable source = new TestObservable(mock(Disposable.class), "one");
        final RuntimeException testException = new RuntimeException("test exception");
        Observer<String> observer = TestHelper.mockObserver();
        Observable<String> take = Observable.unsafeCreate(source).takeWhile(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                throw testException;
            }
        });
        take.subscribe(observer);
        // wait for the Observable to complete
        try {
            source.t.join();
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        verify(observer, never()).onNext(any(String.class));
        verify(observer, times(1)).onError(testException);
    }

    @Test
    public void unsubscribeAfterTake() {
        Disposable upstream = mock(Disposable.class);
        TestObservable w = new TestObservable(upstream, "one", "two", "three");
        Observer<String> observer = TestHelper.mockObserver();
        Observable<String> take = Observable.unsafeCreate(w).takeWhile(new Predicate<String>() {

            int index;

            @Override
            public boolean test(String s) {
                return index++ < 1;
            }
        });
        take.subscribe(observer);
        // wait for the Observable to complete
        try {
            w.t.join();
        } catch (Throwable e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        // System.out.println("TestObservable thread finished");
        verify(observer, times(1)).onNext("one");
        verify(observer, never()).onNext("two");
        verify(observer, never()).onNext("three");
        verify(upstream, times(1)).dispose();
    }

    private static class TestObservable implements ObservableSource<String> {

        final Disposable upstream;

        final String[] values;

        Thread t;

        TestObservable(Disposable upstream, String... values) {
            this.upstream = upstream;
            this.values = values;
        }

        @Override
        public void subscribe(final Observer<? super String> observer) {
            // System.out.println("TestObservable subscribed to ...");
            observer.onSubscribe(upstream);
            t = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        // System.out.println("running TestObservable thread");
                        for (String s : values) {
                            // System.out.println("TestObservable onNext: " + s);
                            observer.onNext(s);
                        }
                        observer.onComplete();
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            // System.out.println("starting TestObservable thread");
            t.start();
            // System.out.println("done starting TestObservable thread");
        }
    }

    @Test
    public void noUnsubscribeDownstream() {
        Observable<Integer> source = Observable.range(1, 1000).takeWhile(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 2;
            }
        });
        TestObserver<Integer> to = new TestObserver<>();
        source.subscribe(to);
        to.assertNoErrors();
        to.assertValue(1);
    // 2.0.2 - not anymore
    // Assert.assertTrue("Not cancelled!", ts.isCancelled());
    }

    @Test
    public void errorCauseIncludesLastValue() {
        TestObserverEx<String> to = new TestObserverEx<>();
        Observable.just("abc").takeWhile(new Predicate<String>() {

            @Override
            public boolean test(String t1) {
                throw new TestException();
            }
        }).subscribe(to);
        to.assertTerminated();
        to.assertNoValues();
        to.assertError(TestException.class);
    // FIXME last cause value not recorded
    // assertTrue(ts.getOnErrorEvents().get(0).getCause().getMessage().contains("abc"));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().takeWhile(Functions.alwaysTrue()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.takeWhile(Functions.alwaysTrue());
            }
        });
    }

    @Test
    public void badSource() {
        new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onComplete();
                observer.onComplete();
            }
        }.takeWhile(Functions.alwaysTrue()).test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableTakeWhileTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeWhile1() throws java.lang.Throwable {
            this.payloads.takeWhile1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeWhileOnSubject1() throws java.lang.Throwable {
            this.payloads.takeWhileOnSubject1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeWhile2() throws java.lang.Throwable {
            this.payloads.takeWhile2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeWhileDoesntLeakErrors() throws java.lang.Throwable {
            this.payloads.takeWhileDoesntLeakErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeWhileProtectsPredicateCall() throws java.lang.Throwable {
            this.payloads.takeWhileProtectsPredicateCall.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeAfterTake() throws java.lang.Throwable {
            this.payloads.unsubscribeAfterTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noUnsubscribeDownstream() throws java.lang.Throwable {
            this.payloads.noUnsubscribeDownstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorCauseIncludesLastValue() throws java.lang.Throwable {
            this.payloads.errorCauseIncludesLastValue.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeWhileTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeWhileTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeWhileTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeWhileTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableTakeWhileTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeWhileTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableTakeWhileTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableTakeWhileTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement takeWhile1;

            public org.junit.runners.model.Statement takeWhileOnSubject1;

            public org.junit.runners.model.Statement takeWhile2;

            public org.junit.runners.model.Statement takeWhileDoesntLeakErrors;

            public org.junit.runners.model.Statement takeWhileProtectsPredicateCall;

            public org.junit.runners.model.Statement unsubscribeAfterTake;

            public org.junit.runners.model.Statement noUnsubscribeDownstream;

            public org.junit.runners.model.Statement errorCauseIncludesLastValue;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badSource;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.takeWhile1 = _ClassStatement.forPayload(ObservableTakeWhileTest::takeWhile1, "takeWhile1", this);
            this.payloads.takeWhileOnSubject1 = _ClassStatement.forPayload(ObservableTakeWhileTest::takeWhileOnSubject1, "takeWhileOnSubject1", this);
            this.payloads.takeWhile2 = _ClassStatement.forPayload(ObservableTakeWhileTest::takeWhile2, "takeWhile2", this);
            this.payloads.takeWhileDoesntLeakErrors = _ClassStatement.forPayload(ObservableTakeWhileTest::takeWhileDoesntLeakErrors, "takeWhileDoesntLeakErrors", this);
            this.payloads.takeWhileProtectsPredicateCall = _ClassStatement.forPayload(ObservableTakeWhileTest::takeWhileProtectsPredicateCall, "takeWhileProtectsPredicateCall", this);
            this.payloads.unsubscribeAfterTake = _ClassStatement.forPayload(ObservableTakeWhileTest::unsubscribeAfterTake, "unsubscribeAfterTake", this);
            this.payloads.noUnsubscribeDownstream = _ClassStatement.forPayload(ObservableTakeWhileTest::noUnsubscribeDownstream, "noUnsubscribeDownstream", this);
            this.payloads.errorCauseIncludesLastValue = _ClassStatement.forPayload(ObservableTakeWhileTest::errorCauseIncludesLastValue, "errorCauseIncludesLastValue", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableTakeWhileTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableTakeWhileTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badSource = _ClassStatement.forPayload(ObservableTakeWhileTest::badSource, "badSource", this);
        }
    }
}
