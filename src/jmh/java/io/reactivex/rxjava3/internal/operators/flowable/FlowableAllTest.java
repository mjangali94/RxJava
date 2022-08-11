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
package io.reactivex.rxjava3.internal.operators.flowable;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableAllTest extends RxJavaTest {

    @Test
    public void all() {
        Flowable<String> obs = Flowable.just("one", "two", "six");
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).subscribe(observer);
        verify(observer).onSubscribe((Disposable) any());
        verify(observer).onSuccess(true);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void notAll() {
        Flowable<String> obs = Flowable.just("one", "two", "three", "six");
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).subscribe(observer);
        verify(observer).onSubscribe((Disposable) any());
        verify(observer).onSuccess(false);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void empty() {
        Flowable<String> obs = Flowable.empty();
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).subscribe(observer);
        verify(observer).onSubscribe((Disposable) any());
        verify(observer).onSuccess(true);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void error() {
        Throwable error = new Throwable();
        Flowable<String> obs = Flowable.error(error);
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).subscribe(observer);
        verify(observer).onSubscribe((Disposable) any());
        verify(observer).onError(error);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void followingFirst() {
        Flowable<Integer> f = Flowable.fromArray(1, 3, 5, 6);
        Single<Boolean> allOdd = f.all(new Predicate<Integer>() {

            @Override
            public boolean test(Integer i) {
                return i % 2 == 1;
            }
        });
        assertFalse(allOdd.blockingGet());
    }

    @Test
    public void issue1935NoUnsubscribeDownstream() {
        Flowable<Integer> source = Flowable.just(1).all(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return false;
            }
        }).flatMapPublisher(new Function<Boolean, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Boolean t1) {
                return Flowable.just(2).delay(500, TimeUnit.MILLISECONDS);
            }
        });
        assertEquals((Object) 2, source.blockingFirst());
    }

    @Test
    public void backpressureIfOneRequestedOneShouldBeDelivered() {
        TestObserverEx<Boolean> to = new TestObserverEx<>();
        Flowable.empty().all(new Predicate<Object>() {

            @Override
            public boolean test(Object t) {
                return false;
            }
        }).subscribe(to);
        to.assertTerminated();
        to.assertNoErrors();
        to.assertComplete();
        to.assertValue(true);
    }

    @Test
    public void predicateThrowsExceptionAndValueInCauseMessage() {
        TestObserverEx<Boolean> to = new TestObserverEx<>();
        final IllegalArgumentException ex = new IllegalArgumentException();
        Flowable.just("Boo!").all(new Predicate<String>() {

            @Override
            public boolean test(String v) {
                throw ex;
            }
        }).subscribe(to);
        to.assertTerminated();
        to.assertNoValues();
        to.assertNotComplete();
        to.assertError(ex);
    // FIXME need to decide about adding the value that probably caused the crash in some way
    // assertTrue(ex.getCause().getMessage().contains("Boo!"));
    }

    @Test
    public void allFlowable() {
        Flowable<String> obs = Flowable.just("one", "two", "six");
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).toFlowable().subscribe(subscriber);
        verify(subscriber).onSubscribe((Subscription) any());
        verify(subscriber).onNext(true);
        verify(subscriber).onComplete();
        verifyNoMoreInteractions(subscriber);
    }

    @Test
    public void notAllFlowable() {
        Flowable<String> obs = Flowable.just("one", "two", "three", "six");
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).toFlowable().subscribe(subscriber);
        verify(subscriber).onSubscribe((Subscription) any());
        verify(subscriber).onNext(false);
        verify(subscriber).onComplete();
        verifyNoMoreInteractions(subscriber);
    }

    @Test
    public void emptyFlowable() {
        Flowable<String> obs = Flowable.empty();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).toFlowable().subscribe(subscriber);
        verify(subscriber).onSubscribe((Subscription) any());
        verify(subscriber).onNext(true);
        verify(subscriber).onComplete();
        verifyNoMoreInteractions(subscriber);
    }

    @Test
    public void errorFlowable() {
        Throwable error = new Throwable();
        Flowable<String> obs = Flowable.error(error);
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        obs.all(new Predicate<String>() {

            @Override
            public boolean test(String s) {
                return s.length() == 3;
            }
        }).toFlowable().subscribe(subscriber);
        verify(subscriber).onSubscribe((Subscription) any());
        verify(subscriber).onError(error);
        verifyNoMoreInteractions(subscriber);
    }

    @Test
    public void followingFirstFlowable() {
        Flowable<Integer> f = Flowable.fromArray(1, 3, 5, 6);
        Flowable<Boolean> allOdd = f.all(new Predicate<Integer>() {

            @Override
            public boolean test(Integer i) {
                return i % 2 == 1;
            }
        }).toFlowable();
        assertFalse(allOdd.blockingFirst());
    }

    @Test
    public void issue1935NoUnsubscribeDownstreamFlowable() {
        Flowable<Integer> source = Flowable.just(1).all(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return false;
            }
        }).toFlowable().flatMap(new Function<Boolean, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Boolean t1) {
                return Flowable.just(2).delay(500, TimeUnit.MILLISECONDS);
            }
        });
        assertEquals((Object) 2, source.blockingFirst());
    }

    @Test
    public void backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable() {
        TestSubscriber<Boolean> ts = new TestSubscriber<>(0L);
        Flowable.empty().all(new Predicate<Object>() {

            @Override
            public boolean test(Object t1) {
                return false;
            }
        }).toFlowable().subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void backpressureIfOneRequestedOneShouldBeDeliveredFlowable() {
        TestSubscriberEx<Boolean> ts = new TestSubscriberEx<>(1L);
        Flowable.empty().all(new Predicate<Object>() {

            @Override
            public boolean test(Object t) {
                return false;
            }
        }).toFlowable().subscribe(ts);
        ts.assertTerminated();
        ts.assertNoErrors();
        ts.assertComplete();
        ts.assertValue(true);
    }

    @Test
    public void predicateThrowsExceptionAndValueInCauseMessageFlowable() {
        TestSubscriberEx<Boolean> ts = new TestSubscriberEx<>();
        final IllegalArgumentException ex = new IllegalArgumentException();
        Flowable.just("Boo!").all(new Predicate<String>() {

            @Override
            public boolean test(String v) {
                throw ex;
            }
        }).toFlowable().subscribe(ts);
        ts.assertTerminated();
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(ex);
    // FIXME need to decide about adding the value that probably caused the crash in some way
    // assertTrue(ex.getCause().getMessage().contains("Boo!"));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).all(Functions.alwaysTrue()).toFlowable());
        TestHelper.checkDisposed(Flowable.just(1).all(Functions.alwaysTrue()));
    }

    @Test
    public void predicateThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onNext(2);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                }
            }.all(new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) throws Exception {
                    throw new TestException();
                }
            }).toFlowable().test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void predicateThrowsObservable() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onNext(2);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                }
            }.all(new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) throws Exception {
                    throw new TestException();
                }
            }).toFlowable().test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.all(Functions.alwaysTrue());
            }
        }, false, 1, 1, true);
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.all(Functions.alwaysTrue()).toFlowable();
            }
        }, false, 1, 1, true);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Boolean>>() {

            @Override
            public Publisher<Boolean> apply(Flowable<Object> f) throws Exception {
                return f.all(Functions.alwaysTrue()).toFlowable();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Object>, Single<Boolean>>() {

            @Override
            public Single<Boolean> apply(Flowable<Object> f) throws Exception {
                return f.all(Functions.alwaysTrue());
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableAllTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_all() throws java.lang.Throwable {
            this.payloads.all.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notAll() throws java.lang.Throwable {
            this.payloads.notAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_followingFirst() throws java.lang.Throwable {
            this.payloads.followingFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1935NoUnsubscribeDownstream() throws java.lang.Throwable {
            this.payloads.issue1935NoUnsubscribeDownstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureIfOneRequestedOneShouldBeDelivered() throws java.lang.Throwable {
            this.payloads.backpressureIfOneRequestedOneShouldBeDelivered.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_predicateThrowsExceptionAndValueInCauseMessage() throws java.lang.Throwable {
            this.payloads.predicateThrowsExceptionAndValueInCauseMessage.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allFlowable() throws java.lang.Throwable {
            this.payloads.allFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notAllFlowable() throws java.lang.Throwable {
            this.payloads.notAllFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyFlowable() throws java.lang.Throwable {
            this.payloads.emptyFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorFlowable() throws java.lang.Throwable {
            this.payloads.errorFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_followingFirstFlowable() throws java.lang.Throwable {
            this.payloads.followingFirstFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1935NoUnsubscribeDownstreamFlowable() throws java.lang.Throwable {
            this.payloads.issue1935NoUnsubscribeDownstreamFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable() throws java.lang.Throwable {
            this.payloads.backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureIfOneRequestedOneShouldBeDeliveredFlowable() throws java.lang.Throwable {
            this.payloads.backpressureIfOneRequestedOneShouldBeDeliveredFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_predicateThrowsExceptionAndValueInCauseMessageFlowable() throws java.lang.Throwable {
            this.payloads.predicateThrowsExceptionAndValueInCauseMessageFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_predicateThrows() throws java.lang.Throwable {
            this.payloads.predicateThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_predicateThrowsObservable() throws java.lang.Throwable {
            this.payloads.predicateThrowsObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableAllTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableAllTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableAllTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableAllTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableAllTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableAllTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableAllTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableAllTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement all;

            public org.junit.runners.model.Statement notAll;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement followingFirst;

            public org.junit.runners.model.Statement issue1935NoUnsubscribeDownstream;

            public org.junit.runners.model.Statement backpressureIfOneRequestedOneShouldBeDelivered;

            public org.junit.runners.model.Statement predicateThrowsExceptionAndValueInCauseMessage;

            public org.junit.runners.model.Statement allFlowable;

            public org.junit.runners.model.Statement notAllFlowable;

            public org.junit.runners.model.Statement emptyFlowable;

            public org.junit.runners.model.Statement errorFlowable;

            public org.junit.runners.model.Statement followingFirstFlowable;

            public org.junit.runners.model.Statement issue1935NoUnsubscribeDownstreamFlowable;

            public org.junit.runners.model.Statement backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable;

            public org.junit.runners.model.Statement backpressureIfOneRequestedOneShouldBeDeliveredFlowable;

            public org.junit.runners.model.Statement predicateThrowsExceptionAndValueInCauseMessageFlowable;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement predicateThrows;

            public org.junit.runners.model.Statement predicateThrowsObservable;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.all = _ClassStatement.forPayload(FlowableAllTest::all, "all", this);
            this.payloads.notAll = _ClassStatement.forPayload(FlowableAllTest::notAll, "notAll", this);
            this.payloads.empty = _ClassStatement.forPayload(FlowableAllTest::empty, "empty", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableAllTest::error, "error", this);
            this.payloads.followingFirst = _ClassStatement.forPayload(FlowableAllTest::followingFirst, "followingFirst", this);
            this.payloads.issue1935NoUnsubscribeDownstream = _ClassStatement.forPayload(FlowableAllTest::issue1935NoUnsubscribeDownstream, "issue1935NoUnsubscribeDownstream", this);
            this.payloads.backpressureIfOneRequestedOneShouldBeDelivered = _ClassStatement.forPayload(FlowableAllTest::backpressureIfOneRequestedOneShouldBeDelivered, "backpressureIfOneRequestedOneShouldBeDelivered", this);
            this.payloads.predicateThrowsExceptionAndValueInCauseMessage = _ClassStatement.forPayload(FlowableAllTest::predicateThrowsExceptionAndValueInCauseMessage, "predicateThrowsExceptionAndValueInCauseMessage", this);
            this.payloads.allFlowable = _ClassStatement.forPayload(FlowableAllTest::allFlowable, "allFlowable", this);
            this.payloads.notAllFlowable = _ClassStatement.forPayload(FlowableAllTest::notAllFlowable, "notAllFlowable", this);
            this.payloads.emptyFlowable = _ClassStatement.forPayload(FlowableAllTest::emptyFlowable, "emptyFlowable", this);
            this.payloads.errorFlowable = _ClassStatement.forPayload(FlowableAllTest::errorFlowable, "errorFlowable", this);
            this.payloads.followingFirstFlowable = _ClassStatement.forPayload(FlowableAllTest::followingFirstFlowable, "followingFirstFlowable", this);
            this.payloads.issue1935NoUnsubscribeDownstreamFlowable = _ClassStatement.forPayload(FlowableAllTest::issue1935NoUnsubscribeDownstreamFlowable, "issue1935NoUnsubscribeDownstreamFlowable", this);
            this.payloads.backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable = _ClassStatement.forPayload(FlowableAllTest::backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable, "backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable", this);
            this.payloads.backpressureIfOneRequestedOneShouldBeDeliveredFlowable = _ClassStatement.forPayload(FlowableAllTest::backpressureIfOneRequestedOneShouldBeDeliveredFlowable, "backpressureIfOneRequestedOneShouldBeDeliveredFlowable", this);
            this.payloads.predicateThrowsExceptionAndValueInCauseMessageFlowable = _ClassStatement.forPayload(FlowableAllTest::predicateThrowsExceptionAndValueInCauseMessageFlowable, "predicateThrowsExceptionAndValueInCauseMessageFlowable", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableAllTest::dispose, "dispose", this);
            this.payloads.predicateThrows = _ClassStatement.forPayload(FlowableAllTest::predicateThrows, "predicateThrows", this);
            this.payloads.predicateThrowsObservable = _ClassStatement.forPayload(FlowableAllTest::predicateThrowsObservable, "predicateThrowsObservable", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableAllTest::badSource, "badSource", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableAllTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
