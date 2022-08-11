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
import java.util.concurrent.TimeUnit;
import org.junit.*;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableSampleTest extends RxJavaTest {

    private TestScheduler scheduler;

    private Scheduler.Worker innerScheduler;

    private Observer<Long> observer;

    private Observer<Object> observer2;

    @Before
    public // due to mocking
    void before() {
        scheduler = new TestScheduler();
        innerScheduler = scheduler.createWorker();
        observer = TestHelper.mockObserver();
        observer2 = TestHelper.mockObserver();
    }

    @Test
    public void sample() {
        Observable<Long> source = Observable.unsafeCreate(new ObservableSource<Long>() {

            @Override
            public void subscribe(final Observer<? super Long> observer1) {
                observer1.onSubscribe(Disposable.empty());
                innerScheduler.schedule(new Runnable() {

                    @Override
                    public void run() {
                        observer1.onNext(1L);
                    }
                }, 1, TimeUnit.SECONDS);
                innerScheduler.schedule(new Runnable() {

                    @Override
                    public void run() {
                        observer1.onNext(2L);
                    }
                }, 2, TimeUnit.SECONDS);
                innerScheduler.schedule(new Runnable() {

                    @Override
                    public void run() {
                        observer1.onComplete();
                    }
                }, 3, TimeUnit.SECONDS);
            }
        });
        Observable<Long> sampled = source.sample(400L, TimeUnit.MILLISECONDS, scheduler);
        sampled.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        scheduler.advanceTimeTo(800L, TimeUnit.MILLISECONDS);
        verify(observer, never()).onNext(any(Long.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(1200L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, times(1)).onNext(1L);
        verify(observer, never()).onNext(2L);
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(1600L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(1L);
        verify(observer, never()).onNext(2L);
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(2000L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(1L);
        inOrder.verify(observer, times(1)).onNext(2L);
        verify(observer, never()).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
        scheduler.advanceTimeTo(3000L, TimeUnit.MILLISECONDS);
        inOrder.verify(observer, never()).onNext(1L);
        inOrder.verify(observer, never()).onNext(2L);
        verify(observer, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerNormal() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> sampler = PublishSubject.create();
        Observable<Integer> m = source.sample(sampler);
        m.subscribe(observer2);
        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        source.onNext(3);
        source.onNext(4);
        sampler.onNext(2);
        source.onComplete();
        sampler.onNext(3);
        InOrder inOrder = inOrder(observer2);
        inOrder.verify(observer2, never()).onNext(1);
        inOrder.verify(observer2, times(1)).onNext(2);
        inOrder.verify(observer2, never()).onNext(3);
        inOrder.verify(observer2, times(1)).onNext(4);
        inOrder.verify(observer2, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerNoDuplicates() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> sampler = PublishSubject.create();
        Observable<Integer> m = source.sample(sampler);
        m.subscribe(observer2);
        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        sampler.onNext(1);
        source.onNext(3);
        source.onNext(4);
        sampler.onNext(2);
        sampler.onNext(2);
        source.onComplete();
        sampler.onNext(3);
        InOrder inOrder = inOrder(observer2);
        inOrder.verify(observer2, never()).onNext(1);
        inOrder.verify(observer2, times(1)).onNext(2);
        inOrder.verify(observer2, never()).onNext(3);
        inOrder.verify(observer2, times(1)).onNext(4);
        inOrder.verify(observer2, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerTerminatingEarly() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> sampler = PublishSubject.create();
        Observable<Integer> m = source.sample(sampler);
        m.subscribe(observer2);
        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        sampler.onComplete();
        source.onNext(3);
        source.onNext(4);
        InOrder inOrder = inOrder(observer2);
        inOrder.verify(observer2, never()).onNext(1);
        inOrder.verify(observer2, times(1)).onNext(2);
        inOrder.verify(observer2, times(1)).onComplete();
        inOrder.verify(observer2, never()).onNext(any());
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerEmitAndTerminate() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> sampler = PublishSubject.create();
        Observable<Integer> m = source.sample(sampler);
        m.subscribe(observer2);
        source.onNext(1);
        source.onNext(2);
        sampler.onNext(1);
        source.onNext(3);
        source.onComplete();
        sampler.onNext(2);
        sampler.onComplete();
        InOrder inOrder = inOrder(observer2);
        inOrder.verify(observer2, never()).onNext(1);
        inOrder.verify(observer2, times(1)).onNext(2);
        inOrder.verify(observer2, never()).onNext(3);
        inOrder.verify(observer2, times(1)).onComplete();
        inOrder.verify(observer2, never()).onNext(any());
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerEmptySource() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> sampler = PublishSubject.create();
        Observable<Integer> m = source.sample(sampler);
        m.subscribe(observer2);
        source.onComplete();
        sampler.onNext(1);
        InOrder inOrder = inOrder(observer2);
        inOrder.verify(observer2, times(1)).onComplete();
        verify(observer2, never()).onNext(any());
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void sampleWithSamplerSourceThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> sampler = PublishSubject.create();
        Observable<Integer> m = source.sample(sampler);
        m.subscribe(observer2);
        source.onNext(1);
        source.onError(new RuntimeException("Forced failure!"));
        sampler.onNext(1);
        InOrder inOrder = inOrder(observer2);
        inOrder.verify(observer2, times(1)).onError(any(Throwable.class));
        verify(observer2, never()).onNext(any());
        verify(observer, never()).onComplete();
    }

    @Test
    public void sampleWithSamplerThrows() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> sampler = PublishSubject.create();
        Observable<Integer> m = source.sample(sampler);
        m.subscribe(observer2);
        source.onNext(1);
        sampler.onNext(1);
        sampler.onError(new RuntimeException("Forced failure!"));
        InOrder inOrder = inOrder(observer2);
        inOrder.verify(observer2, times(1)).onNext(1);
        inOrder.verify(observer2, times(1)).onError(any(RuntimeException.class));
        verify(observer, never()).onComplete();
    }

    @Test
    public void sampleUnsubscribe() {
        final Disposable upstream = mock(Disposable.class);
        Observable<Integer> o = Observable.unsafeCreate(new ObservableSource<Integer>() {

            @Override
            public void subscribe(Observer<? super Integer> observer) {
                observer.onSubscribe(upstream);
            }
        });
        o.throttleLast(1, TimeUnit.MILLISECONDS).subscribe().dispose();
        verify(upstream).dispose();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().sample(1, TimeUnit.SECONDS, new TestScheduler()));
        TestHelper.checkDisposed(PublishSubject.create().sample(Observable.never()));
    }

    @Test
    public void error() {
        Observable.error(new TestException()).sample(1, TimeUnit.SECONDS).test().assertFailure(TestException.class);
    }

    @Test
    public void emitLastTimed() {
        Observable.just(1).sample(1, TimeUnit.DAYS, true).test().assertResult(1);
    }

    @Test
    public void emitLastTimedEmpty() {
        Observable.empty().sample(1, TimeUnit.DAYS, true).test().assertResult();
    }

    @Test
    public void emitLastTimedCustomScheduler() {
        Observable.just(1).sample(1, TimeUnit.DAYS, Schedulers.single(), true).test().assertResult(1);
    }

    @Test
    public void emitLastTimedRunCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestScheduler scheduler = new TestScheduler();
            final PublishSubject<Integer> ps = PublishSubject.create();
            TestObserver<Integer> to = ps.sample(1, TimeUnit.SECONDS, scheduler, true).test();
            ps.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
                }
            };
            TestHelper.race(r1, r2);
            to.assertResult(1);
        }
    }

    @Test
    public void emitLastOther() {
        Observable.just(1).sample(Observable.timer(1, TimeUnit.DAYS), true).test().assertResult(1);
    }

    @Test
    public void emitLastOtherEmpty() {
        Observable.empty().sample(Observable.timer(1, TimeUnit.DAYS), true).test().assertResult();
    }

    @Test
    public void emitLastOtherRunCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final PublishSubject<Integer> sampler = PublishSubject.create();
            TestObserver<Integer> to = ps.sample(sampler, true).test();
            ps.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sampler.onNext(1);
                }
            };
            TestHelper.race(r1, r2);
            to.assertResult(1);
        }
    }

    @Test
    public void emitLastOtherCompleteCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final PublishSubject<Integer> sampler = PublishSubject.create();
            TestObserver<Integer> to = ps.sample(sampler, true).test();
            ps.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sampler.onComplete();
                }
            };
            TestHelper.race(r1, r2);
            to.assertResult(1);
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<Object> o) throws Exception {
                return o.sample(1, TimeUnit.SECONDS);
            }
        });
    }

    @Test
    public void doubleOnSubscribeObservable() {
        TestHelper.checkDoubleOnSubscribeObservable(o -> o.sample(Observable.never()));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableSampleTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sample() throws java.lang.Throwable {
            this.payloads.sample.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerNormal() throws java.lang.Throwable {
            this.payloads.sampleWithSamplerNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerNoDuplicates() throws java.lang.Throwable {
            this.payloads.sampleWithSamplerNoDuplicates.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerTerminatingEarly() throws java.lang.Throwable {
            this.payloads.sampleWithSamplerTerminatingEarly.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerEmitAndTerminate() throws java.lang.Throwable {
            this.payloads.sampleWithSamplerEmitAndTerminate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerEmptySource() throws java.lang.Throwable {
            this.payloads.sampleWithSamplerEmptySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerSourceThrows() throws java.lang.Throwable {
            this.payloads.sampleWithSamplerSourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleWithSamplerThrows() throws java.lang.Throwable {
            this.payloads.sampleWithSamplerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sampleUnsubscribe() throws java.lang.Throwable {
            this.payloads.sampleUnsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastTimed() throws java.lang.Throwable {
            this.payloads.emitLastTimed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastTimedEmpty() throws java.lang.Throwable {
            this.payloads.emitLastTimedEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastTimedCustomScheduler() throws java.lang.Throwable {
            this.payloads.emitLastTimedCustomScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastTimedRunCompleteRace() throws java.lang.Throwable {
            this.payloads.emitLastTimedRunCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastOther() throws java.lang.Throwable {
            this.payloads.emitLastOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastOtherEmpty() throws java.lang.Throwable {
            this.payloads.emitLastOtherEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastOtherRunCompleteRace() throws java.lang.Throwable {
            this.payloads.emitLastOtherRunCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLastOtherCompleteCompleteRace() throws java.lang.Throwable {
            this.payloads.emitLastOtherCompleteCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeObservable() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribeObservable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSampleTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSampleTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSampleTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSampleTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableSampleTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSampleTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableSampleTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableSampleTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement sample;

            public org.junit.runners.model.Statement sampleWithSamplerNormal;

            public org.junit.runners.model.Statement sampleWithSamplerNoDuplicates;

            public org.junit.runners.model.Statement sampleWithSamplerTerminatingEarly;

            public org.junit.runners.model.Statement sampleWithSamplerEmitAndTerminate;

            public org.junit.runners.model.Statement sampleWithSamplerEmptySource;

            public org.junit.runners.model.Statement sampleWithSamplerSourceThrows;

            public org.junit.runners.model.Statement sampleWithSamplerThrows;

            public org.junit.runners.model.Statement sampleUnsubscribe;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement emitLastTimed;

            public org.junit.runners.model.Statement emitLastTimedEmpty;

            public org.junit.runners.model.Statement emitLastTimedCustomScheduler;

            public org.junit.runners.model.Statement emitLastTimedRunCompleteRace;

            public org.junit.runners.model.Statement emitLastOther;

            public org.junit.runners.model.Statement emitLastOtherEmpty;

            public org.junit.runners.model.Statement emitLastOtherRunCompleteRace;

            public org.junit.runners.model.Statement emitLastOtherCompleteCompleteRace;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement doubleOnSubscribeObservable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.sample = _ClassStatement.forPayload(ObservableSampleTest::sample, "sample", this);
            this.payloads.sampleWithSamplerNormal = _ClassStatement.forPayload(ObservableSampleTest::sampleWithSamplerNormal, "sampleWithSamplerNormal", this);
            this.payloads.sampleWithSamplerNoDuplicates = _ClassStatement.forPayload(ObservableSampleTest::sampleWithSamplerNoDuplicates, "sampleWithSamplerNoDuplicates", this);
            this.payloads.sampleWithSamplerTerminatingEarly = _ClassStatement.forPayload(ObservableSampleTest::sampleWithSamplerTerminatingEarly, "sampleWithSamplerTerminatingEarly", this);
            this.payloads.sampleWithSamplerEmitAndTerminate = _ClassStatement.forPayload(ObservableSampleTest::sampleWithSamplerEmitAndTerminate, "sampleWithSamplerEmitAndTerminate", this);
            this.payloads.sampleWithSamplerEmptySource = _ClassStatement.forPayload(ObservableSampleTest::sampleWithSamplerEmptySource, "sampleWithSamplerEmptySource", this);
            this.payloads.sampleWithSamplerSourceThrows = _ClassStatement.forPayload(ObservableSampleTest::sampleWithSamplerSourceThrows, "sampleWithSamplerSourceThrows", this);
            this.payloads.sampleWithSamplerThrows = _ClassStatement.forPayload(ObservableSampleTest::sampleWithSamplerThrows, "sampleWithSamplerThrows", this);
            this.payloads.sampleUnsubscribe = _ClassStatement.forPayload(ObservableSampleTest::sampleUnsubscribe, "sampleUnsubscribe", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableSampleTest::dispose, "dispose", this);
            this.payloads.error = _ClassStatement.forPayload(ObservableSampleTest::error, "error", this);
            this.payloads.emitLastTimed = _ClassStatement.forPayload(ObservableSampleTest::emitLastTimed, "emitLastTimed", this);
            this.payloads.emitLastTimedEmpty = _ClassStatement.forPayload(ObservableSampleTest::emitLastTimedEmpty, "emitLastTimedEmpty", this);
            this.payloads.emitLastTimedCustomScheduler = _ClassStatement.forPayload(ObservableSampleTest::emitLastTimedCustomScheduler, "emitLastTimedCustomScheduler", this);
            this.payloads.emitLastTimedRunCompleteRace = _ClassStatement.forPayload(ObservableSampleTest::emitLastTimedRunCompleteRace, "emitLastTimedRunCompleteRace", this);
            this.payloads.emitLastOther = _ClassStatement.forPayload(ObservableSampleTest::emitLastOther, "emitLastOther", this);
            this.payloads.emitLastOtherEmpty = _ClassStatement.forPayload(ObservableSampleTest::emitLastOtherEmpty, "emitLastOtherEmpty", this);
            this.payloads.emitLastOtherRunCompleteRace = _ClassStatement.forPayload(ObservableSampleTest::emitLastOtherRunCompleteRace, "emitLastOtherRunCompleteRace", this);
            this.payloads.emitLastOtherCompleteCompleteRace = _ClassStatement.forPayload(ObservableSampleTest::emitLastOtherCompleteCompleteRace, "emitLastOtherCompleteCompleteRace", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableSampleTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.doubleOnSubscribeObservable = _ClassStatement.forPayload(ObservableSampleTest::doubleOnSubscribeObservable, "doubleOnSubscribeObservable", this);
        }
    }
}
