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
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableSkipUntilTest extends RxJavaTest {

    Observer<Object> observer;

    @Before
    public void before() {
        observer = TestHelper.mockObserver();
    }

    @Test
    public void normal1() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        Observable<Integer> m = source.skipUntil(other);
        m.subscribe(observer);
        source.onNext(0);
        source.onNext(1);
        other.onNext(100);
        source.onNext(2);
        source.onNext(3);
        source.onNext(4);
        source.onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(2);
        verify(observer, times(1)).onNext(3);
        verify(observer, times(1)).onNext(4);
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void otherNeverFires() {
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> m = source.skipUntil(Observable.never());
        m.subscribe(observer);
        source.onNext(0);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onNext(4);
        source.onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, never()).onNext(any());
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void otherEmpty() {
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> m = source.skipUntil(Observable.empty());
        m.subscribe(observer);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
    }

    @Test
    public void otherFiresAndCompletes() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        Observable<Integer> m = source.skipUntil(other);
        m.subscribe(observer);
        source.onNext(0);
        source.onNext(1);
        other.onNext(100);
        other.onComplete();
        source.onNext(2);
        source.onNext(3);
        source.onNext(4);
        source.onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(2);
        verify(observer, times(1)).onNext(3);
        verify(observer, times(1)).onNext(4);
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void sourceThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        Observable<Integer> m = source.skipUntil(other);
        m.subscribe(observer);
        source.onNext(0);
        source.onNext(1);
        other.onNext(100);
        other.onComplete();
        source.onNext(2);
        source.onError(new RuntimeException("Forced failure"));
        verify(observer, times(1)).onNext(2);
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
    }

    @Test
    public void otherThrowsImmediately() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> other = PublishSubject.create();
        Observable<Integer> m = source.skipUntil(other);
        m.subscribe(observer);
        source.onNext(0);
        source.onNext(1);
        other.onError(new RuntimeException("Forced failure"));
        verify(observer, never()).onNext(any());
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().skipUntil(PublishSubject.create()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.skipUntil(Observable.never());
            }
        });
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return Observable.never().skipUntil(o);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableSkipUntilTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal1() throws java.lang.Throwable {
            this.payloads.normal1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherNeverFires() throws java.lang.Throwable {
            this.payloads.otherNeverFires.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherEmpty() throws java.lang.Throwable {
            this.payloads.otherEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherFiresAndCompletes() throws java.lang.Throwable {
            this.payloads.otherFiresAndCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceThrows() throws java.lang.Throwable {
            this.payloads.sourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherThrowsImmediately() throws java.lang.Throwable {
            this.payloads.otherThrowsImmediately.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipUntilTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipUntilTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.before();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipUntilTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipUntilTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableSkipUntilTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSkipUntilTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableSkipUntilTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableSkipUntilTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal1;

            public org.junit.runners.model.Statement otherNeverFires;

            public org.junit.runners.model.Statement otherEmpty;

            public org.junit.runners.model.Statement otherFiresAndCompletes;

            public org.junit.runners.model.Statement sourceThrows;

            public org.junit.runners.model.Statement otherThrowsImmediately;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal1 = _ClassStatement.forPayload(ObservableSkipUntilTest::normal1, "normal1", this);
            this.payloads.otherNeverFires = _ClassStatement.forPayload(ObservableSkipUntilTest::otherNeverFires, "otherNeverFires", this);
            this.payloads.otherEmpty = _ClassStatement.forPayload(ObservableSkipUntilTest::otherEmpty, "otherEmpty", this);
            this.payloads.otherFiresAndCompletes = _ClassStatement.forPayload(ObservableSkipUntilTest::otherFiresAndCompletes, "otherFiresAndCompletes", this);
            this.payloads.sourceThrows = _ClassStatement.forPayload(ObservableSkipUntilTest::sourceThrows, "sourceThrows", this);
            this.payloads.otherThrowsImmediately = _ClassStatement.forPayload(ObservableSkipUntilTest::otherThrowsImmediately, "otherThrowsImmediately", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableSkipUntilTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableSkipUntilTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
