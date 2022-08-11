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
import static org.mockito.Mockito.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableTakeUntilTest extends RxJavaTest {

    @Test
    public void takeUntil() {
        Disposable sSource = mock(Disposable.class);
        Disposable sOther = mock(Disposable.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Observer<String> result = TestHelper.mockObserver();
        Observable<String> stringObservable = Observable.unsafeCreate(source).takeUntil(Observable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        other.sendOnNext("three");
        source.sendOnNext("four");
        source.sendOnCompleted();
        other.sendOnCompleted();
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(0)).onNext("four");
        verify(sSource, times(1)).dispose();
        verify(sOther, times(1)).dispose();
    }

    @Test
    public void takeUntilSourceCompleted() {
        Disposable sSource = mock(Disposable.class);
        Disposable sOther = mock(Disposable.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Observer<String> result = TestHelper.mockObserver();
        Observable<String> stringObservable = Observable.unsafeCreate(source).takeUntil(Observable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        source.sendOnCompleted();
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        // no longer disposing itself on terminal events
        verify(sSource, never()).dispose();
        verify(sOther, times(1)).dispose();
    }

    @Test
    public void takeUntilSourceError() {
        Disposable sSource = mock(Disposable.class);
        Disposable sOther = mock(Disposable.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Throwable error = new Throwable();
        Observer<String> result = TestHelper.mockObserver();
        Observable<String> stringObservable = Observable.unsafeCreate(source).takeUntil(Observable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        source.sendOnError(error);
        source.sendOnNext("three");
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(1)).onError(error);
        // no longer disposing itself on terminal events
        verify(sSource, never()).dispose();
        verify(sOther, times(1)).dispose();
    }

    @Test
    public void takeUntilOtherError() {
        Disposable sSource = mock(Disposable.class);
        Disposable sOther = mock(Disposable.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Throwable error = new Throwable();
        Observer<String> result = TestHelper.mockObserver();
        Observable<String> stringObservable = Observable.unsafeCreate(source).takeUntil(Observable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        other.sendOnError(error);
        source.sendOnNext("three");
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(1)).onError(error);
        verify(result, times(0)).onComplete();
        verify(sSource, times(1)).dispose();
        // no longer disposing itself on termination
        verify(sOther, never()).dispose();
    }

    /**
     * If the 'other' onCompletes then we unsubscribe from the source and onComplete.
     */
    @Test
    public void takeUntilOtherCompleted() {
        Disposable sSource = mock(Disposable.class);
        Disposable sOther = mock(Disposable.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Observer<String> result = TestHelper.mockObserver();
        Observable<String> stringObservable = Observable.unsafeCreate(source).takeUntil(Observable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        other.sendOnCompleted();
        source.sendOnNext("three");
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(1)).onComplete();
        verify(sSource, times(1)).dispose();
        // no longer disposing itself on terminal events
        verify(sOther, never()).dispose();
    }

    private static class TestObservable implements ObservableSource<String> {

        Observer<? super String> observer;

        Disposable upstream;

        TestObservable(Disposable d) {
            this.upstream = d;
        }

        /* used to simulate subscription */
        public void sendOnCompleted() {
            observer.onComplete();
        }

        /* used to simulate subscription */
        public void sendOnNext(String value) {
            observer.onNext(value);
        }

        /* used to simulate subscription */
        public void sendOnError(Throwable e) {
            observer.onError(e);
        }

        @Override
        public void subscribe(Observer<? super String> observer) {
            this.observer = observer;
            observer.onSubscribe(upstream);
        }
    }

    @Test
    public void untilFires() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> until = PublishSubject.create();
        TestObserverEx<Integer> to = new TestObserverEx<>();
        source.takeUntil(until).subscribe(to);
        assertTrue(source.hasObservers());
        assertTrue(until.hasObservers());
        source.onNext(1);
        to.assertValue(1);
        until.onNext(1);
        to.assertValue(1);
        to.assertNoErrors();
        to.assertTerminated();
        assertFalse("Source still has observers", source.hasObservers());
        assertFalse("Until still has observers", until.hasObservers());
    // 2.0.2 - not anymore
    // assertTrue("Not cancelled!", ts.isCancelled());
    }

    @Test
    public void mainCompletes() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> until = PublishSubject.create();
        TestObserverEx<Integer> to = new TestObserverEx<>();
        source.takeUntil(until).subscribe(to);
        assertTrue(source.hasObservers());
        assertTrue(until.hasObservers());
        source.onNext(1);
        source.onComplete();
        to.assertValue(1);
        to.assertNoErrors();
        to.assertTerminated();
        assertFalse("Source still has observers", source.hasObservers());
        assertFalse("Until still has observers", until.hasObservers());
    // 2.0.2 - not anymore
    // assertTrue("Not cancelled!", ts.isCancelled());
    }

    @Test
    public void downstreamUnsubscribes() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> until = PublishSubject.create();
        TestObserverEx<Integer> to = new TestObserverEx<>();
        source.takeUntil(until).take(1).subscribe(to);
        assertTrue(source.hasObservers());
        assertTrue(until.hasObservers());
        source.onNext(1);
        to.assertValue(1);
        to.assertNoErrors();
        to.assertTerminated();
        assertFalse("Source still has observers", source.hasObservers());
        assertFalse("Until still has observers", until.hasObservers());
    // 2.0.2 - not anymore
    // assertTrue("Not cancelled!", ts.isCancelled());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().takeUntil(Observable.never()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> o) throws Exception {
                return o.takeUntil(Observable.never());
            }
        });
    }

    @Test
    public void untilPublisherMainSuccess() {
        PublishSubject<Integer> main = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onNext(1);
        main.onNext(2);
        main.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult(1, 2);
    }

    @Test
    public void untilPublisherMainComplete() {
        PublishSubject<Integer> main = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult();
    }

    @Test
    public void untilPublisherMainError() {
        PublishSubject<Integer> main = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        main.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilPublisherOtherOnNext() {
        PublishSubject<Integer> main = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onNext(1);
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult();
    }

    @Test
    public void untilPublisherOtherOnComplete() {
        PublishSubject<Integer> main = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onComplete();
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertResult();
    }

    @Test
    public void untilPublisherOtherError() {
        PublishSubject<Integer> main = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        TestObserver<Integer> to = main.takeUntil(other).test();
        assertTrue("Main no observers?", main.hasObservers());
        assertTrue("Other no observers?", other.hasObservers());
        other.onError(new TestException());
        assertFalse("Main has observers?", main.hasObservers());
        assertFalse("Other has observers?", other.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void untilPublisherDispose() {
        PublishSubject<Integer> main = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
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

        private ObservableTakeUntilTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntil() throws java.lang.Throwable {
            this.payloads.takeUntil.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilSourceCompleted() throws java.lang.Throwable {
            this.payloads.takeUntilSourceCompleted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilSourceError() throws java.lang.Throwable {
            this.payloads.takeUntilSourceError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilOtherError() throws java.lang.Throwable {
            this.payloads.takeUntilOtherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilOtherCompleted() throws java.lang.Throwable {
            this.payloads.takeUntilOtherCompleted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilFires() throws java.lang.Throwable {
            this.payloads.untilFires.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompletes() throws java.lang.Throwable {
            this.payloads.mainCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_downstreamUnsubscribes() throws java.lang.Throwable {
            this.payloads.downstreamUnsubscribes.evaluate();
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
        public void benchmark_untilPublisherMainSuccess() throws java.lang.Throwable {
            this.payloads.untilPublisherMainSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherMainComplete() throws java.lang.Throwable {
            this.payloads.untilPublisherMainComplete.evaluate();
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

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeUntilTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeUntilTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeUntilTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeUntilTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableTakeUntilTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeUntilTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableTakeUntilTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableTakeUntilTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement takeUntil;

            public org.junit.runners.model.Statement takeUntilSourceCompleted;

            public org.junit.runners.model.Statement takeUntilSourceError;

            public org.junit.runners.model.Statement takeUntilOtherError;

            public org.junit.runners.model.Statement takeUntilOtherCompleted;

            public org.junit.runners.model.Statement untilFires;

            public org.junit.runners.model.Statement mainCompletes;

            public org.junit.runners.model.Statement downstreamUnsubscribes;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement untilPublisherMainSuccess;

            public org.junit.runners.model.Statement untilPublisherMainComplete;

            public org.junit.runners.model.Statement untilPublisherMainError;

            public org.junit.runners.model.Statement untilPublisherOtherOnNext;

            public org.junit.runners.model.Statement untilPublisherOtherOnComplete;

            public org.junit.runners.model.Statement untilPublisherOtherError;

            public org.junit.runners.model.Statement untilPublisherDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.takeUntil = _ClassStatement.forPayload(ObservableTakeUntilTest::takeUntil, "takeUntil", this);
            this.payloads.takeUntilSourceCompleted = _ClassStatement.forPayload(ObservableTakeUntilTest::takeUntilSourceCompleted, "takeUntilSourceCompleted", this);
            this.payloads.takeUntilSourceError = _ClassStatement.forPayload(ObservableTakeUntilTest::takeUntilSourceError, "takeUntilSourceError", this);
            this.payloads.takeUntilOtherError = _ClassStatement.forPayload(ObservableTakeUntilTest::takeUntilOtherError, "takeUntilOtherError", this);
            this.payloads.takeUntilOtherCompleted = _ClassStatement.forPayload(ObservableTakeUntilTest::takeUntilOtherCompleted, "takeUntilOtherCompleted", this);
            this.payloads.untilFires = _ClassStatement.forPayload(ObservableTakeUntilTest::untilFires, "untilFires", this);
            this.payloads.mainCompletes = _ClassStatement.forPayload(ObservableTakeUntilTest::mainCompletes, "mainCompletes", this);
            this.payloads.downstreamUnsubscribes = _ClassStatement.forPayload(ObservableTakeUntilTest::downstreamUnsubscribes, "downstreamUnsubscribes", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableTakeUntilTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableTakeUntilTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.untilPublisherMainSuccess = _ClassStatement.forPayload(ObservableTakeUntilTest::untilPublisherMainSuccess, "untilPublisherMainSuccess", this);
            this.payloads.untilPublisherMainComplete = _ClassStatement.forPayload(ObservableTakeUntilTest::untilPublisherMainComplete, "untilPublisherMainComplete", this);
            this.payloads.untilPublisherMainError = _ClassStatement.forPayload(ObservableTakeUntilTest::untilPublisherMainError, "untilPublisherMainError", this);
            this.payloads.untilPublisherOtherOnNext = _ClassStatement.forPayload(ObservableTakeUntilTest::untilPublisherOtherOnNext, "untilPublisherOtherOnNext", this);
            this.payloads.untilPublisherOtherOnComplete = _ClassStatement.forPayload(ObservableTakeUntilTest::untilPublisherOtherOnComplete, "untilPublisherOtherOnComplete", this);
            this.payloads.untilPublisherOtherError = _ClassStatement.forPayload(ObservableTakeUntilTest::untilPublisherOtherError, "untilPublisherOtherError", this);
            this.payloads.untilPublisherDispose = _ClassStatement.forPayload(ObservableTakeUntilTest::untilPublisherDispose, "untilPublisherDispose", this);
        }
    }
}
