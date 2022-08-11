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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import org.mockito.Mockito;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableOnErrorReturnTest extends RxJavaTest {

    @Test
    public void resumeNext() {
        TestObservable f = new TestObservable("one");
        Observable<String> w = Observable.unsafeCreate(f);
        final AtomicReference<Throwable> capturedException = new AtomicReference<>();
        Observable<String> observable = w.onErrorReturn(new Function<Throwable, String>() {

            @Override
            public String apply(Throwable e) {
                capturedException.set(e);
                return "failure";
            }
        });
        Observer<String> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        try {
            f.t.join();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("failure");
        assertNotNull(capturedException.get());
    }

    /**
     * Test that when a function throws an exception this is propagated through onError.
     */
    @Test
    public void functionThrowsError() {
        TestObservable f = new TestObservable("one");
        Observable<String> w = Observable.unsafeCreate(f);
        final AtomicReference<Throwable> capturedException = new AtomicReference<>();
        Observable<String> observable = w.onErrorReturn(new Function<Throwable, String>() {

            @Override
            public String apply(Throwable e) {
                capturedException.set(e);
                throw new RuntimeException("exception from function");
            }
        });
        Observer<String> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        try {
            f.t.join();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
        // we should get the "one" value before the error
        verify(observer, times(1)).onNext("one");
        // we should have received an onError call on the Observer since the resume function threw an exception
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, times(0)).onComplete();
        assertNotNull(capturedException.get());
    }

    @Test
    public void mapResumeAsyncNext() {
        // Trigger multiple failures
        Observable<String> w = Observable.just("one", "fail", "two", "three", "fail");
        // Introduce map function that fails intermittently (Map does not prevent this when the Observer is a
        // rx.operator incl onErrorResumeNextViaObservable)
        w = w.map(new Function<String, String>() {

            @Override
            public String apply(String s) {
                if ("fail".equals(s)) {
                    throw new RuntimeException("Forced Failure");
                }
                System.out.println("BadMapper:" + s);
                return s;
            }
        });
        Observable<String> observable = w.onErrorReturn(new Function<Throwable, String>() {

            @Override
            public String apply(Throwable t1) {
                return "resume";
            }
        });
        Observer<String> observer = TestHelper.mockObserver();
        TestObserver<String> to = new TestObserver<>(observer);
        observable.subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
        verify(observer, times(1)).onNext("one");
        verify(observer, Mockito.never()).onNext("two");
        verify(observer, Mockito.never()).onNext("three");
        verify(observer, times(1)).onNext("resume");
    }

    @Test
    public void backpressure() {
        TestObserver<Integer> to = new TestObserver<>();
        Observable.range(0, 100000).onErrorReturn(new Function<Throwable, Integer>() {

            @Override
            public Integer apply(Throwable t1) {
                return 1;
            }
        }).observeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

            int c;

            @Override
            public Integer apply(Integer t1) {
                if (c++ <= 1) {
                    // slow
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return t1;
            }
        }).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors();
    }

    private static class TestObservable implements ObservableSource<String> {

        final String[] values;

        Thread t;

        TestObservable(String... values) {
            this.values = values;
        }

        @Override
        public void subscribe(final Observer<? super String> observer) {
            observer.onSubscribe(Disposable.empty());
            System.out.println("TestObservable subscribed to ...");
            t = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        System.out.println("running TestObservable thread");
                        for (String s : values) {
                            System.out.println("TestObservable onNext: " + s);
                            observer.onNext(s);
                        }
                        throw new RuntimeException("Forced Failure");
                    } catch (Throwable e) {
                        observer.onError(e);
                    }
                }
            });
            System.out.println("starting TestObservable thread");
            t.start();
            System.out.println("done starting TestObservable thread");
        }
    }

    @Test
    public void returnItem() {
        Observable.error(new TestException()).onErrorReturnItem(1).test().assertResult(1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).onErrorReturnItem(1));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> f) throws Exception {
                return f.onErrorReturnItem(1);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableOnErrorReturnTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resumeNext() throws java.lang.Throwable {
            this.payloads.resumeNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_functionThrowsError() throws java.lang.Throwable {
            this.payloads.functionThrowsError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapResumeAsyncNext() throws java.lang.Throwable {
            this.payloads.mapResumeAsyncNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.payloads.backpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_returnItem() throws java.lang.Throwable {
            this.payloads.returnItem.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorReturnTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorReturnTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorReturnTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorReturnTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableOnErrorReturnTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableOnErrorReturnTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableOnErrorReturnTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableOnErrorReturnTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement resumeNext;

            public org.junit.runners.model.Statement functionThrowsError;

            public org.junit.runners.model.Statement mapResumeAsyncNext;

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement returnItem;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.resumeNext = _ClassStatement.forPayload(ObservableOnErrorReturnTest::resumeNext, "resumeNext", this);
            this.payloads.functionThrowsError = _ClassStatement.forPayload(ObservableOnErrorReturnTest::functionThrowsError, "functionThrowsError", this);
            this.payloads.mapResumeAsyncNext = _ClassStatement.forPayload(ObservableOnErrorReturnTest::mapResumeAsyncNext, "mapResumeAsyncNext", this);
            this.payloads.backpressure = _ClassStatement.forPayload(ObservableOnErrorReturnTest::backpressure, "backpressure", this);
            this.payloads.returnItem = _ClassStatement.forPayload(ObservableOnErrorReturnTest::returnItem, "returnItem", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableOnErrorReturnTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableOnErrorReturnTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
