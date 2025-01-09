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
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableAnyTest extends RxJavaTest {

    @Test
    public void anyWithTwoItems() {
        Flowable<Integer> w = Flowable.just(1, 2);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(false);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void isEmptyWithTwoItems() {
        Flowable<Integer> w = Flowable.just(1, 2);
        Single<Boolean> single = w.isEmpty();
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(true);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithOneItem() {
        Flowable<Integer> w = Flowable.just(1);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(false);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void isEmptyWithOneItem() {
        Flowable<Integer> w = Flowable.just(1);
        Single<Boolean> single = w.isEmpty();
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(true);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithEmpty() {
        Flowable<Integer> w = Flowable.empty();
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void isEmptyWithEmpty() {
        Flowable<Integer> w = Flowable.empty();
        Single<Boolean> single = w.isEmpty();
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onSuccess(false);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithPredicate1() {
        Flowable<Integer> w = Flowable.just(1, 2, 3);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 2;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(false);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void exists1() {
        Flowable<Integer> w = Flowable.just(1, 2, 3);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 2;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(false);
        verify(observer, times(1)).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithPredicate2() {
        Flowable<Integer> w = Flowable.just(1, 2, 3);
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 1;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithEmptyAndPredicate() {
        // If the source is empty, always output false.
        Flowable<Integer> w = Flowable.empty();
        Single<Boolean> single = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t) {
                return true;
            }
        });
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onSuccess(true);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void withFollowingFirst() {
        Flowable<Integer> f = Flowable.fromArray(1, 3, 5, 6);
        Single<Boolean> anyEven = f.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer i) {
                return i % 2 == 0;
            }
        });
        assertTrue(anyEven.blockingGet());
    }

    @Test
    public void issue1935NoUnsubscribeDownstream() {
        Flowable<Integer> source = Flowable.just(1).isEmpty().flatMapPublisher(new Function<Boolean, Publisher<Integer>>() {

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
        Flowable.just(1).any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
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
        Flowable.just("Boo!").any(new Predicate<String>() {

            @Override
            public boolean test(String v) {
                throw ex;
            }
        }).subscribe(to);
        to.assertTerminated();
        to.assertNoValues();
        to.assertNotComplete();
        to.assertError(ex);
    // FIXME value as last cause?
    // assertTrue(ex.getCause().getMessage().contains("Boo!"));
    }

    @Test
    public void anyWithTwoItemsFlowable() {
        Flowable<Integer> w = Flowable.just(1, 2);
        Flowable<Boolean> flowable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(false);
        verify(subscriber, times(1)).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void isEmptyWithTwoItemsFlowable() {
        Flowable<Integer> w = Flowable.just(1, 2);
        Flowable<Boolean> flowable = w.isEmpty().toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(true);
        verify(subscriber, times(1)).onNext(false);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void anyWithOneItemFlowable() {
        Flowable<Integer> w = Flowable.just(1);
        Flowable<Boolean> flowable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(false);
        verify(subscriber, times(1)).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void isEmptyWithOneItemFlowable() {
        Flowable<Integer> w = Flowable.just(1);
        Single<Boolean> single = w.isEmpty();
        SingleObserver<Boolean> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, never()).onSuccess(true);
        verify(observer, times(1)).onSuccess(false);
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void anyWithEmptyFlowable() {
        Flowable<Integer> w = Flowable.empty();
        Flowable<Boolean> flowable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(false);
        verify(subscriber, never()).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void isEmptyWithEmptyFlowable() {
        Flowable<Integer> w = Flowable.empty();
        Flowable<Boolean> flowable = w.isEmpty().toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(true);
        verify(subscriber, never()).onNext(false);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void anyWithPredicate1Flowable() {
        Flowable<Integer> w = Flowable.just(1, 2, 3);
        Flowable<Boolean> flowable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 2;
            }
        }).toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(false);
        verify(subscriber, times(1)).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void exists1Flowable() {
        Flowable<Integer> w = Flowable.just(1, 2, 3);
        Flowable<Boolean> flowable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 2;
            }
        }).toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, never()).onNext(false);
        verify(subscriber, times(1)).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void anyWithPredicate2Flowable() {
        Flowable<Integer> w = Flowable.just(1, 2, 3);
        Flowable<Boolean> flowable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 1;
            }
        }).toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(false);
        verify(subscriber, never()).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void anyWithEmptyAndPredicateFlowable() {
        // If the source is empty, always output false.
        Flowable<Integer> w = Flowable.empty();
        Flowable<Boolean> flowable = w.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t) {
                return true;
            }
        }).toFlowable();
        Subscriber<Boolean> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(false);
        verify(subscriber, never()).onNext(true);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void withFollowingFirstFlowable() {
        Flowable<Integer> f = Flowable.fromArray(1, 3, 5, 6);
        Flowable<Boolean> anyEven = f.any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer i) {
                return i % 2 == 0;
            }
        }).toFlowable();
        assertTrue(anyEven.blockingFirst());
    }

    @Test
    public void issue1935NoUnsubscribeDownstreamFlowable() {
        Flowable<Integer> source = Flowable.just(1).isEmpty().flatMapPublisher(new Function<Boolean, Publisher<Integer>>() {

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
        Flowable.just(1).any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t) {
                return true;
            }
        }).toFlowable().subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void backpressureIfOneRequestedOneShouldBeDeliveredFlowable() {
        TestSubscriberEx<Boolean> ts = new TestSubscriberEx<>(1L);
        Flowable.just(1).any(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
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
        Flowable.just("Boo!").any(new Predicate<String>() {

            @Override
            public boolean test(String v) {
                throw ex;
            }
        }).toFlowable().subscribe(ts);
        ts.assertTerminated();
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(ex);
    // FIXME value as last cause?
    // assertTrue(ex.getCause().getMessage().contains("Boo!"));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).any(Functions.alwaysTrue()).toFlowable());
        TestHelper.checkDisposed(Flowable.just(1).any(Functions.alwaysTrue()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Boolean>>() {

            @Override
            public Publisher<Boolean> apply(Flowable<Object> f) throws Exception {
                return f.any(Functions.alwaysTrue()).toFlowable();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Object>, Single<Boolean>>() {

            @Override
            public Single<Boolean> apply(Flowable<Object> f) throws Exception {
                return f.any(Functions.alwaysTrue());
            }
        });
    }

    @Test
    public void predicateThrowsSuppressOthers() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onNext(2);
                    subscriber.onError(new IOException());
                    subscriber.onComplete();
                }
            }.any(new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) throws Exception {
                    throw new TestException();
                }
            }).toFlowable().test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSourceSingle() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onError(new TestException("First"));
                    subscriber.onNext(1);
                    subscriber.onError(new TestException("Second"));
                    subscriber.onComplete();
                }
            }.any(Functions.alwaysTrue()).to(TestHelper.<Boolean>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableAnyTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithTwoItems() throws java.lang.Throwable {
            this.payloads.anyWithTwoItems.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithTwoItems() throws java.lang.Throwable {
            this.payloads.isEmptyWithTwoItems.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithOneItem() throws java.lang.Throwable {
            this.payloads.anyWithOneItem.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithOneItem() throws java.lang.Throwable {
            this.payloads.isEmptyWithOneItem.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithEmpty() throws java.lang.Throwable {
            this.payloads.anyWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithEmpty() throws java.lang.Throwable {
            this.payloads.isEmptyWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithPredicate1() throws java.lang.Throwable {
            this.payloads.anyWithPredicate1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exists1() throws java.lang.Throwable {
            this.payloads.exists1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithPredicate2() throws java.lang.Throwable {
            this.payloads.anyWithPredicate2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithEmptyAndPredicate() throws java.lang.Throwable {
            this.payloads.anyWithEmptyAndPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFollowingFirst() throws java.lang.Throwable {
            this.payloads.withFollowingFirst.evaluate();
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
        public void benchmark_anyWithTwoItemsFlowable() throws java.lang.Throwable {
            this.payloads.anyWithTwoItemsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithTwoItemsFlowable() throws java.lang.Throwable {
            this.payloads.isEmptyWithTwoItemsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithOneItemFlowable() throws java.lang.Throwable {
            this.payloads.anyWithOneItemFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithOneItemFlowable() throws java.lang.Throwable {
            this.payloads.isEmptyWithOneItemFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithEmptyFlowable() throws java.lang.Throwable {
            this.payloads.anyWithEmptyFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isEmptyWithEmptyFlowable() throws java.lang.Throwable {
            this.payloads.isEmptyWithEmptyFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithPredicate1Flowable() throws java.lang.Throwable {
            this.payloads.anyWithPredicate1Flowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exists1Flowable() throws java.lang.Throwable {
            this.payloads.exists1Flowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithPredicate2Flowable() throws java.lang.Throwable {
            this.payloads.anyWithPredicate2Flowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_anyWithEmptyAndPredicateFlowable() throws java.lang.Throwable {
            this.payloads.anyWithEmptyAndPredicateFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withFollowingFirstFlowable() throws java.lang.Throwable {
            this.payloads.withFollowingFirstFlowable.evaluate();
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
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_predicateThrowsSuppressOthers() throws java.lang.Throwable {
            this.payloads.predicateThrowsSuppressOthers.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceSingle() throws java.lang.Throwable {
            this.payloads.badSourceSingle.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableAnyTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableAnyTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableAnyTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableAnyTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableAnyTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableAnyTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableAnyTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableAnyTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement anyWithTwoItems;

            public org.junit.runners.model.Statement isEmptyWithTwoItems;

            public org.junit.runners.model.Statement anyWithOneItem;

            public org.junit.runners.model.Statement isEmptyWithOneItem;

            public org.junit.runners.model.Statement anyWithEmpty;

            public org.junit.runners.model.Statement isEmptyWithEmpty;

            public org.junit.runners.model.Statement anyWithPredicate1;

            public org.junit.runners.model.Statement exists1;

            public org.junit.runners.model.Statement anyWithPredicate2;

            public org.junit.runners.model.Statement anyWithEmptyAndPredicate;

            public org.junit.runners.model.Statement withFollowingFirst;

            public org.junit.runners.model.Statement issue1935NoUnsubscribeDownstream;

            public org.junit.runners.model.Statement backpressureIfOneRequestedOneShouldBeDelivered;

            public org.junit.runners.model.Statement predicateThrowsExceptionAndValueInCauseMessage;

            public org.junit.runners.model.Statement anyWithTwoItemsFlowable;

            public org.junit.runners.model.Statement isEmptyWithTwoItemsFlowable;

            public org.junit.runners.model.Statement anyWithOneItemFlowable;

            public org.junit.runners.model.Statement isEmptyWithOneItemFlowable;

            public org.junit.runners.model.Statement anyWithEmptyFlowable;

            public org.junit.runners.model.Statement isEmptyWithEmptyFlowable;

            public org.junit.runners.model.Statement anyWithPredicate1Flowable;

            public org.junit.runners.model.Statement exists1Flowable;

            public org.junit.runners.model.Statement anyWithPredicate2Flowable;

            public org.junit.runners.model.Statement anyWithEmptyAndPredicateFlowable;

            public org.junit.runners.model.Statement withFollowingFirstFlowable;

            public org.junit.runners.model.Statement issue1935NoUnsubscribeDownstreamFlowable;

            public org.junit.runners.model.Statement backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable;

            public org.junit.runners.model.Statement backpressureIfOneRequestedOneShouldBeDeliveredFlowable;

            public org.junit.runners.model.Statement predicateThrowsExceptionAndValueInCauseMessageFlowable;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement predicateThrowsSuppressOthers;

            public org.junit.runners.model.Statement badSourceSingle;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.anyWithTwoItems = _ClassStatement.forPayload(FlowableAnyTest::anyWithTwoItems, "anyWithTwoItems", this);
            this.payloads.isEmptyWithTwoItems = _ClassStatement.forPayload(FlowableAnyTest::isEmptyWithTwoItems, "isEmptyWithTwoItems", this);
            this.payloads.anyWithOneItem = _ClassStatement.forPayload(FlowableAnyTest::anyWithOneItem, "anyWithOneItem", this);
            this.payloads.isEmptyWithOneItem = _ClassStatement.forPayload(FlowableAnyTest::isEmptyWithOneItem, "isEmptyWithOneItem", this);
            this.payloads.anyWithEmpty = _ClassStatement.forPayload(FlowableAnyTest::anyWithEmpty, "anyWithEmpty", this);
            this.payloads.isEmptyWithEmpty = _ClassStatement.forPayload(FlowableAnyTest::isEmptyWithEmpty, "isEmptyWithEmpty", this);
            this.payloads.anyWithPredicate1 = _ClassStatement.forPayload(FlowableAnyTest::anyWithPredicate1, "anyWithPredicate1", this);
            this.payloads.exists1 = _ClassStatement.forPayload(FlowableAnyTest::exists1, "exists1", this);
            this.payloads.anyWithPredicate2 = _ClassStatement.forPayload(FlowableAnyTest::anyWithPredicate2, "anyWithPredicate2", this);
            this.payloads.anyWithEmptyAndPredicate = _ClassStatement.forPayload(FlowableAnyTest::anyWithEmptyAndPredicate, "anyWithEmptyAndPredicate", this);
            this.payloads.withFollowingFirst = _ClassStatement.forPayload(FlowableAnyTest::withFollowingFirst, "withFollowingFirst", this);
            this.payloads.issue1935NoUnsubscribeDownstream = _ClassStatement.forPayload(FlowableAnyTest::issue1935NoUnsubscribeDownstream, "issue1935NoUnsubscribeDownstream", this);
            this.payloads.backpressureIfOneRequestedOneShouldBeDelivered = _ClassStatement.forPayload(FlowableAnyTest::backpressureIfOneRequestedOneShouldBeDelivered, "backpressureIfOneRequestedOneShouldBeDelivered", this);
            this.payloads.predicateThrowsExceptionAndValueInCauseMessage = _ClassStatement.forPayload(FlowableAnyTest::predicateThrowsExceptionAndValueInCauseMessage, "predicateThrowsExceptionAndValueInCauseMessage", this);
            this.payloads.anyWithTwoItemsFlowable = _ClassStatement.forPayload(FlowableAnyTest::anyWithTwoItemsFlowable, "anyWithTwoItemsFlowable", this);
            this.payloads.isEmptyWithTwoItemsFlowable = _ClassStatement.forPayload(FlowableAnyTest::isEmptyWithTwoItemsFlowable, "isEmptyWithTwoItemsFlowable", this);
            this.payloads.anyWithOneItemFlowable = _ClassStatement.forPayload(FlowableAnyTest::anyWithOneItemFlowable, "anyWithOneItemFlowable", this);
            this.payloads.isEmptyWithOneItemFlowable = _ClassStatement.forPayload(FlowableAnyTest::isEmptyWithOneItemFlowable, "isEmptyWithOneItemFlowable", this);
            this.payloads.anyWithEmptyFlowable = _ClassStatement.forPayload(FlowableAnyTest::anyWithEmptyFlowable, "anyWithEmptyFlowable", this);
            this.payloads.isEmptyWithEmptyFlowable = _ClassStatement.forPayload(FlowableAnyTest::isEmptyWithEmptyFlowable, "isEmptyWithEmptyFlowable", this);
            this.payloads.anyWithPredicate1Flowable = _ClassStatement.forPayload(FlowableAnyTest::anyWithPredicate1Flowable, "anyWithPredicate1Flowable", this);
            this.payloads.exists1Flowable = _ClassStatement.forPayload(FlowableAnyTest::exists1Flowable, "exists1Flowable", this);
            this.payloads.anyWithPredicate2Flowable = _ClassStatement.forPayload(FlowableAnyTest::anyWithPredicate2Flowable, "anyWithPredicate2Flowable", this);
            this.payloads.anyWithEmptyAndPredicateFlowable = _ClassStatement.forPayload(FlowableAnyTest::anyWithEmptyAndPredicateFlowable, "anyWithEmptyAndPredicateFlowable", this);
            this.payloads.withFollowingFirstFlowable = _ClassStatement.forPayload(FlowableAnyTest::withFollowingFirstFlowable, "withFollowingFirstFlowable", this);
            this.payloads.issue1935NoUnsubscribeDownstreamFlowable = _ClassStatement.forPayload(FlowableAnyTest::issue1935NoUnsubscribeDownstreamFlowable, "issue1935NoUnsubscribeDownstreamFlowable", this);
            this.payloads.backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable = _ClassStatement.forPayload(FlowableAnyTest::backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable, "backpressureIfNoneRequestedNoneShouldBeDeliveredFlowable", this);
            this.payloads.backpressureIfOneRequestedOneShouldBeDeliveredFlowable = _ClassStatement.forPayload(FlowableAnyTest::backpressureIfOneRequestedOneShouldBeDeliveredFlowable, "backpressureIfOneRequestedOneShouldBeDeliveredFlowable", this);
            this.payloads.predicateThrowsExceptionAndValueInCauseMessageFlowable = _ClassStatement.forPayload(FlowableAnyTest::predicateThrowsExceptionAndValueInCauseMessageFlowable, "predicateThrowsExceptionAndValueInCauseMessageFlowable", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableAnyTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableAnyTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.predicateThrowsSuppressOthers = _ClassStatement.forPayload(FlowableAnyTest::predicateThrowsSuppressOthers, "predicateThrowsSuppressOthers", this);
            this.payloads.badSourceSingle = _ClassStatement.forPayload(FlowableAnyTest::badSourceSingle, "badSourceSingle", this);
        }
    }
}
