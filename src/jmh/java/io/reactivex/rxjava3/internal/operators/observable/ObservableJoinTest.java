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
import org.junit.*;
import org.mockito.MockitoAnnotations;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableJoinTest extends RxJavaTest {

    Observer<Object> observer = TestHelper.mockObserver();

    BiFunction<Integer, Integer, Integer> add = new BiFunction<Integer, Integer, Integer>() {

        @Override
        public Integer apply(Integer t1, Integer t2) {
            return t1 + t2;
        }
    };

    <T> Function<Integer, Observable<T>> just(final Observable<T> observable) {
        return new Function<Integer, Observable<T>>() {

            @Override
            public Observable<T> apply(Integer t1) {
                return observable;
            }
        };
    }

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void normal1() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Observable<Integer> m = source1.join(source2, just(Observable.never()), just(Observable.never()), add);
        m.subscribe(observer);
        source1.onNext(1);
        source1.onNext(2);
        source1.onNext(4);
        source2.onNext(16);
        source2.onNext(32);
        source2.onNext(64);
        source1.onComplete();
        source2.onComplete();
        verify(observer, times(1)).onNext(17);
        verify(observer, times(1)).onNext(18);
        verify(observer, times(1)).onNext(20);
        verify(observer, times(1)).onNext(33);
        verify(observer, times(1)).onNext(34);
        verify(observer, times(1)).onNext(36);
        verify(observer, times(1)).onNext(65);
        verify(observer, times(1)).onNext(66);
        verify(observer, times(1)).onNext(68);
        verify(observer, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void normal1WithDuration() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        PublishSubject<Integer> duration1 = PublishSubject.create();
        Observable<Integer> m = source1.join(source2, just(duration1), just(Observable.never()), add);
        m.subscribe(observer);
        source1.onNext(1);
        source1.onNext(2);
        source2.onNext(16);
        duration1.onNext(1);
        source1.onNext(4);
        source1.onNext(8);
        source1.onComplete();
        source2.onComplete();
        verify(observer, times(1)).onNext(17);
        verify(observer, times(1)).onNext(18);
        verify(observer, times(1)).onNext(20);
        verify(observer, times(1)).onNext(24);
        verify(observer, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void normal2() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Observable<Integer> m = source1.join(source2, just(Observable.never()), just(Observable.never()), add);
        m.subscribe(observer);
        source1.onNext(1);
        source1.onNext(2);
        source1.onComplete();
        source2.onNext(16);
        source2.onNext(32);
        source2.onNext(64);
        source2.onComplete();
        verify(observer, times(1)).onNext(17);
        verify(observer, times(1)).onNext(18);
        verify(observer, times(1)).onNext(33);
        verify(observer, times(1)).onNext(34);
        verify(observer, times(1)).onNext(65);
        verify(observer, times(1)).onNext(66);
        verify(observer, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void leftThrows() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Observable<Integer> m = source1.join(source2, just(Observable.never()), just(Observable.never()), add);
        m.subscribe(observer);
        source2.onNext(1);
        source1.onError(new RuntimeException("Forced failure"));
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onNext(any());
    }

    @Test
    public void rightThrows() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Observable<Integer> m = source1.join(source2, just(Observable.never()), just(Observable.never()), add);
        m.subscribe(observer);
        source1.onNext(1);
        source2.onError(new RuntimeException("Forced failure"));
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onNext(any());
    }

    @Test
    public void leftDurationThrows() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Observable<Integer> duration1 = Observable.<Integer>error(new RuntimeException("Forced failure"));
        Observable<Integer> m = source1.join(source2, just(duration1), just(Observable.never()), add);
        m.subscribe(observer);
        source1.onNext(1);
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onNext(any());
    }

    @Test
    public void rightDurationThrows() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Observable<Integer> duration1 = Observable.<Integer>error(new RuntimeException("Forced failure"));
        Observable<Integer> m = source1.join(source2, just(Observable.never()), just(duration1), add);
        m.subscribe(observer);
        source2.onNext(1);
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onNext(any());
    }

    @Test
    public void leftDurationSelectorThrows() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Function<Integer, Observable<Integer>> fail = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                throw new RuntimeException("Forced failure");
            }
        };
        Observable<Integer> m = source1.join(source2, fail, just(Observable.never()), add);
        m.subscribe(observer);
        source1.onNext(1);
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onNext(any());
    }

    @Test
    public void rightDurationSelectorThrows() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Function<Integer, Observable<Integer>> fail = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                throw new RuntimeException("Forced failure");
            }
        };
        Observable<Integer> m = source1.join(source2, just(Observable.never()), fail, add);
        m.subscribe(observer);
        source2.onNext(1);
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onNext(any());
    }

    @Test
    public void resultSelectorThrows() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        BiFunction<Integer, Integer, Integer> fail = new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                throw new RuntimeException("Forced failure");
            }
        };
        Observable<Integer> m = source1.join(source2, just(Observable.never()), just(Observable.never()), fail);
        m.subscribe(observer);
        source1.onNext(1);
        source2.onNext(2);
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onNext(any());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.<Integer>create().join(Observable.just(1), Functions.justFunction(Observable.never()), Functions.justFunction(Observable.never()), new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }));
    }

    @Test
    public void take() {
        Observable.just(1).join(Observable.just(2), Functions.justFunction(Observable.never()), Functions.justFunction(Observable.never()), new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).take(1).test().assertResult(3);
    }

    @Test
    public void rightClose() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.join(Observable.just(2), Functions.justFunction(Observable.never()), Functions.justFunction(Observable.empty()), new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).test().assertEmpty();
        ps.onNext(1);
        to.assertEmpty();
    }

    @Test
    public void resultSelectorThrows2() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.join(Observable.just(2), Functions.justFunction(Observable.never()), Functions.justFunction(Observable.never()), new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                throw new TestException();
            }
        }).test();
        ps.onNext(1);
        ps.onComplete();
        to.assertFailure(TestException.class);
    }

    @Test
    public void badOuterSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onError(new TestException("First"));
                    observer.onError(new TestException("Second"));
                }
            }.join(Observable.just(2), Functions.justFunction(Observable.never()), Functions.justFunction(Observable.never()), new BiFunction<Integer, Integer, Integer>() {

                @Override
                public Integer apply(Integer a, Integer b) throws Exception {
                    return a + b;
                }
            }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badEndSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            @SuppressWarnings("rawtypes")
            final Observer[] o = { null };
            TestObserverEx<Integer> to = Observable.just(1).join(Observable.just(2), Functions.justFunction(Observable.never()), Functions.justFunction(new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    o[0] = observer;
                    observer.onSubscribe(Disposable.empty());
                    observer.onError(new TestException("First"));
                }
            }), new BiFunction<Integer, Integer, Integer>() {

                @Override
                public Integer apply(Integer a, Integer b) throws Exception {
                    return a + b;
                }
            }).to(TestHelper.<Integer>testConsumer());
            o[0].onError(new TestException("Second"));
            to.assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void bothTerminateWithWorkRemaining() {
        PublishSubject<Integer> ps1 = PublishSubject.create();
        PublishSubject<Integer> ps2 = PublishSubject.create();
        TestObserver<Integer> to = ps1.join(ps2, v -> Observable.never(), v -> Observable.never(), (a, b) -> a + b).doOnNext(v -> {
            ps1.onComplete();
            ps2.onNext(2);
            ps2.onComplete();
        }).test();
        ps1.onNext(0);
        ps2.onNext(1);
        to.assertComplete();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableJoinTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal1() throws java.lang.Throwable {
            this.payloads.normal1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal1WithDuration() throws java.lang.Throwable {
            this.payloads.normal1WithDuration.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal2() throws java.lang.Throwable {
            this.payloads.normal2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_leftThrows() throws java.lang.Throwable {
            this.payloads.leftThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rightThrows() throws java.lang.Throwable {
            this.payloads.rightThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_leftDurationThrows() throws java.lang.Throwable {
            this.payloads.leftDurationThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rightDurationThrows() throws java.lang.Throwable {
            this.payloads.rightDurationThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_leftDurationSelectorThrows() throws java.lang.Throwable {
            this.payloads.leftDurationSelectorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rightDurationSelectorThrows() throws java.lang.Throwable {
            this.payloads.rightDurationSelectorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resultSelectorThrows() throws java.lang.Throwable {
            this.payloads.resultSelectorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rightClose() throws java.lang.Throwable {
            this.payloads.rightClose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resultSelectorThrows2() throws java.lang.Throwable {
            this.payloads.resultSelectorThrows2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badOuterSource() throws java.lang.Throwable {
            this.payloads.badOuterSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badEndSource() throws java.lang.Throwable {
            this.payloads.badEndSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothTerminateWithWorkRemaining() throws java.lang.Throwable {
            this.payloads.bothTerminateWithWorkRemaining.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableJoinTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableJoinTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableJoinTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableJoinTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableJoinTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableJoinTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableJoinTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableJoinTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal1;

            public org.junit.runners.model.Statement normal1WithDuration;

            public org.junit.runners.model.Statement normal2;

            public org.junit.runners.model.Statement leftThrows;

            public org.junit.runners.model.Statement rightThrows;

            public org.junit.runners.model.Statement leftDurationThrows;

            public org.junit.runners.model.Statement rightDurationThrows;

            public org.junit.runners.model.Statement leftDurationSelectorThrows;

            public org.junit.runners.model.Statement rightDurationSelectorThrows;

            public org.junit.runners.model.Statement resultSelectorThrows;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement rightClose;

            public org.junit.runners.model.Statement resultSelectorThrows2;

            public org.junit.runners.model.Statement badOuterSource;

            public org.junit.runners.model.Statement badEndSource;

            public org.junit.runners.model.Statement bothTerminateWithWorkRemaining;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal1 = _ClassStatement.forPayload(ObservableJoinTest::normal1, "normal1", this);
            this.payloads.normal1WithDuration = _ClassStatement.forPayload(ObservableJoinTest::normal1WithDuration, "normal1WithDuration", this);
            this.payloads.normal2 = _ClassStatement.forPayload(ObservableJoinTest::normal2, "normal2", this);
            this.payloads.leftThrows = _ClassStatement.forPayload(ObservableJoinTest::leftThrows, "leftThrows", this);
            this.payloads.rightThrows = _ClassStatement.forPayload(ObservableJoinTest::rightThrows, "rightThrows", this);
            this.payloads.leftDurationThrows = _ClassStatement.forPayload(ObservableJoinTest::leftDurationThrows, "leftDurationThrows", this);
            this.payloads.rightDurationThrows = _ClassStatement.forPayload(ObservableJoinTest::rightDurationThrows, "rightDurationThrows", this);
            this.payloads.leftDurationSelectorThrows = _ClassStatement.forPayload(ObservableJoinTest::leftDurationSelectorThrows, "leftDurationSelectorThrows", this);
            this.payloads.rightDurationSelectorThrows = _ClassStatement.forPayload(ObservableJoinTest::rightDurationSelectorThrows, "rightDurationSelectorThrows", this);
            this.payloads.resultSelectorThrows = _ClassStatement.forPayload(ObservableJoinTest::resultSelectorThrows, "resultSelectorThrows", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableJoinTest::dispose, "dispose", this);
            this.payloads.take = _ClassStatement.forPayload(ObservableJoinTest::take, "take", this);
            this.payloads.rightClose = _ClassStatement.forPayload(ObservableJoinTest::rightClose, "rightClose", this);
            this.payloads.resultSelectorThrows2 = _ClassStatement.forPayload(ObservableJoinTest::resultSelectorThrows2, "resultSelectorThrows2", this);
            this.payloads.badOuterSource = _ClassStatement.forPayload(ObservableJoinTest::badOuterSource, "badOuterSource", this);
            this.payloads.badEndSource = _ClassStatement.forPayload(ObservableJoinTest::badEndSource, "badEndSource", this);
            this.payloads.bothTerminateWithWorkRemaining = _ClassStatement.forPayload(ObservableJoinTest::bothTerminateWithWorkRemaining, "bothTerminateWithWorkRemaining", this);
        }
    }
}
