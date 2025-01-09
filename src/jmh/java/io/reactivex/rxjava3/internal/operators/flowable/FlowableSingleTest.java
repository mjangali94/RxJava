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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableSingleTest extends RxJavaTest {

    @Test
    public void singleFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).singleElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithTooManyElementsFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2).singleElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.<Integer>empty().singleElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onComplete();
        inOrder.verify(subscriber, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleDoesNotRequestMoreThanItNeedsIf1Then2RequestedFlowable() {
        final List<Long> requests = new ArrayList<>();
        Flowable.just(1).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long n) {
                requests.add(n);
            }
        }).singleElement().toFlowable().subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
                request(2);
            }
        });
        // FIXME single now triggers fast-path
        assertEquals(Arrays.asList(Long.MAX_VALUE), requests);
    }

    @Test
    public void singleDoesNotRequestMoreThanItNeedsIf3RequestedFlowable() {
        final List<Long> requests = new ArrayList<>();
        Flowable.just(1).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long n) {
                requests.add(n);
            }
        }).singleElement().toFlowable().subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(3);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
            }
        });
        // FIXME single now triggers fast-path
        assertEquals(Arrays.asList(Long.MAX_VALUE), requests);
    }

    @Test
    public void singleRequestsExactlyWhatItNeedsIf1RequestedFlowable() {
        final List<Long> requests = new ArrayList<>();
        Flowable.just(1).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long n) {
                requests.add(n);
            }
        }).singleElement().toFlowable().subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
            }
        });
        // FIXME single now triggers fast-path
        assertEquals(Arrays.asList(Long.MAX_VALUE), requests);
    }

    @Test
    public void singleWithPredicateFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndTooManyElementsFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement().toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber).onComplete();
        inOrder.verify(subscriber, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).single(2).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithTooManyElementsFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2).single(3).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.<Integer>empty().single(1).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(1);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(4).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndTooManyElementsFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(6).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndEmptyFlowable() {
        Flowable<Integer> flowable = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(2).toFlowable();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onNext(2);
        inOrder.verify(subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithBackpressureFlowable() {
        Flowable<Integer> flowable = Flowable.just(1, 2).singleElement().toFlowable();
        Subscriber<Integer> subscriber = spy(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                request(1);
            }
        });
        flowable.subscribe(subscriber);
        InOrder inOrder = inOrder(subscriber);
        inOrder.verify(subscriber, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void single() {
        Maybe<Integer> maybe = Flowable.just(1).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithTooManyElements() {
        Maybe<Integer> maybe = Flowable.just(1, 2).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithEmpty() {
        Maybe<Integer> maybe = Flowable.<Integer>empty().singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleDoesNotRequestMoreThanItNeedsToEmitItem() {
        final AtomicLong request = new AtomicLong();
        Flowable.just(1).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long n) {
                request.addAndGet(n);
            }
        }).blockingSingle();
        // FIXME single now triggers fast-path
        assertEquals(Long.MAX_VALUE, request.get());
    }

    @Test
    public void singleDoesNotRequestMoreThanItNeedsToEmitErrorFromEmpty() {
        final AtomicLong request = new AtomicLong();
        try {
            Flowable.empty().doOnRequest(new LongConsumer() {

                @Override
                public void accept(long n) {
                    request.addAndGet(n);
                }
            }).blockingSingle();
        } catch (NoSuchElementException e) {
            // FIXME single now triggers fast-path
            assertEquals(Long.MAX_VALUE, request.get());
        }
    }

    @Test
    public void singleDoesNotRequestMoreThanItNeedsToEmitErrorFromMoreThanOne() {
        final AtomicLong request = new AtomicLong();
        try {
            Flowable.just(1, 2).doOnRequest(new LongConsumer() {

                @Override
                public void accept(long n) {
                    request.addAndGet(n);
                }
            }).blockingSingle();
        } catch (IllegalArgumentException e) {
            // FIXME single now triggers fast-path
            assertEquals(Long.MAX_VALUE, request.get());
        }
    }

    @Test
    public void singleWithPredicate() {
        Maybe<Integer> maybe = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndTooManyElements() {
        Maybe<Integer> maybe = Flowable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleWithPredicateAndEmpty() {
        Maybe<Integer> maybe = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).singleElement();
        MaybeObserver<Integer> observer = TestHelper.mockMaybeObserver();
        maybe.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onComplete();
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefault() {
        Single<Integer> single = Flowable.just(1).single(2);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithTooManyElements() {
        Single<Integer> single = Flowable.just(1, 2).single(3);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithEmpty() {
        Single<Integer> single = Flowable.<Integer>empty().single(1);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(1);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicate() {
        Single<Integer> single = Flowable.just(1, 2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(4);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndTooManyElements() {
        Single<Integer> single = Flowable.just(1, 2, 3, 4).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(6);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onError(isA(IllegalArgumentException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleOrDefaultWithPredicateAndEmpty() {
        Single<Integer> single = Flowable.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 % 2 == 0;
            }
        }).single(2);
        SingleObserver<Integer> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onSuccess(2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void issue1527() throws InterruptedException {
        // https://github.com/ReactiveX/RxJava/pull/1527
        Flowable<Integer> source = Flowable.just(1, 2, 3, 4, 5, 6);
        Maybe<Integer> reduced = source.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });
        Integer r = reduced.blockingGet();
        assertEquals(21, r.intValue());
    }

    @Test
    public void singleOrErrorNoElement() {
        Flowable.empty().singleOrError().test().assertNoValues().assertError(NoSuchElementException.class);
    }

    @Test
    public void singleOrErrorOneElement() {
        Flowable.just(1).singleOrError().test().assertNoErrors().assertValue(1);
    }

    @Test
    public void singleOrErrorMultipleElements() {
        Flowable.just(1, 2, 3).singleOrError().test().assertNoValues().assertError(IllegalArgumentException.class);
    }

    @Test
    public void singleOrErrorError() {
        Flowable.error(new RuntimeException("error")).singleOrError().to(TestHelper.testConsumer()).assertNoValues().assertErrorMessage("error").assertError(RuntimeException.class);
    }

    @Test
    public void issue1527Flowable() throws InterruptedException {
        // https://github.com/ReactiveX/RxJava/pull/1527
        Flowable<Integer> source = Flowable.just(1, 2, 3, 4, 5, 6);
        Flowable<Integer> reduced = source.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer i1, Integer i2) {
                return i1 + i2;
            }
        }).toFlowable();
        Integer r = reduced.blockingFirst();
        assertEquals(21, r.intValue());
    }

    @Test
    public void singleElementOperatorDoNotSwallowExceptionWhenDone() {
        final Throwable exception = new RuntimeException("some error");
        final AtomicReference<Throwable> error = new AtomicReference<>();
        try {
            RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {

                @Override
                public void accept(final Throwable throwable) throws Exception {
                    error.set(throwable);
                }
            });
            Flowable.unsafeCreate(new Publisher<Integer>() {

                @Override
                public void subscribe(final Subscriber<? super Integer> subscriber) {
                    subscriber.onComplete();
                    subscriber.onError(exception);
                }
            }).singleElement().test().assertComplete();
            assertSame(exception, error.get().getCause());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Object>, Object>() {

            @Override
            public Object apply(Flowable<Object> f) throws Exception {
                return f.singleOrError();
            }
        }, false, 1, 1, 1);
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Object>, Object>() {

            @Override
            public Object apply(Flowable<Object> f) throws Exception {
                return f.singleElement();
            }
        }, false, 1, 1, 1);
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Object>, Object>() {

            @Override
            public Object apply(Flowable<Object> f) throws Exception {
                return f.singleOrError().toFlowable();
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Flowable<Object> f) throws Exception {
                return f.singleOrError();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.singleOrError().toFlowable();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToMaybe(new Function<Flowable<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Flowable<Object> f) throws Exception {
                return f.singleElement();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.singleElement().toFlowable();
            }
        });
    }

    @Test
    public void cancelAsFlowable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.singleOrError().toFlowable().test();
        assertTrue(pp.hasSubscribers());
        ts.assertEmpty();
        ts.cancel();
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void singleOrError() {
        Flowable.empty().singleOrError().toFlowable().test().assertFailure(NoSuchElementException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().single(1));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableSingleTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleFlowable() throws java.lang.Throwable {
            this.payloads.singleFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithTooManyElementsFlowable() throws java.lang.Throwable {
            this.payloads.singleWithTooManyElementsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithEmptyFlowable() throws java.lang.Throwable {
            this.payloads.singleWithEmptyFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleDoesNotRequestMoreThanItNeedsIf1Then2RequestedFlowable() throws java.lang.Throwable {
            this.payloads.singleDoesNotRequestMoreThanItNeedsIf1Then2RequestedFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleDoesNotRequestMoreThanItNeedsIf3RequestedFlowable() throws java.lang.Throwable {
            this.payloads.singleDoesNotRequestMoreThanItNeedsIf3RequestedFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleRequestsExactlyWhatItNeedsIf1RequestedFlowable() throws java.lang.Throwable {
            this.payloads.singleRequestsExactlyWhatItNeedsIf1RequestedFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateFlowable() throws java.lang.Throwable {
            this.payloads.singleWithPredicateFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndTooManyElementsFlowable() throws java.lang.Throwable {
            this.payloads.singleWithPredicateAndTooManyElementsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndEmptyFlowable() throws java.lang.Throwable {
            this.payloads.singleWithPredicateAndEmptyFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultFlowable() throws java.lang.Throwable {
            this.payloads.singleOrDefaultFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithTooManyElementsFlowable() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithTooManyElementsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithEmptyFlowable() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithEmptyFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateFlowable() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithPredicateFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndTooManyElementsFlowable() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithPredicateAndTooManyElementsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndEmptyFlowable() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithPredicateAndEmptyFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithBackpressureFlowable() throws java.lang.Throwable {
            this.payloads.singleWithBackpressureFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_single() throws java.lang.Throwable {
            this.payloads.single.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithTooManyElements() throws java.lang.Throwable {
            this.payloads.singleWithTooManyElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithEmpty() throws java.lang.Throwable {
            this.payloads.singleWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleDoesNotRequestMoreThanItNeedsToEmitItem() throws java.lang.Throwable {
            this.payloads.singleDoesNotRequestMoreThanItNeedsToEmitItem.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleDoesNotRequestMoreThanItNeedsToEmitErrorFromEmpty() throws java.lang.Throwable {
            this.payloads.singleDoesNotRequestMoreThanItNeedsToEmitErrorFromEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleDoesNotRequestMoreThanItNeedsToEmitErrorFromMoreThanOne() throws java.lang.Throwable {
            this.payloads.singleDoesNotRequestMoreThanItNeedsToEmitErrorFromMoreThanOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicate() throws java.lang.Throwable {
            this.payloads.singleWithPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndTooManyElements() throws java.lang.Throwable {
            this.payloads.singleWithPredicateAndTooManyElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleWithPredicateAndEmpty() throws java.lang.Throwable {
            this.payloads.singleWithPredicateAndEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefault() throws java.lang.Throwable {
            this.payloads.singleOrDefault.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithTooManyElements() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithTooManyElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithEmpty() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicate() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndTooManyElements() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithPredicateAndTooManyElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrDefaultWithPredicateAndEmpty() throws java.lang.Throwable {
            this.payloads.singleOrDefaultWithPredicateAndEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1527() throws java.lang.Throwable {
            this.payloads.issue1527.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorNoElement() throws java.lang.Throwable {
            this.payloads.singleOrErrorNoElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorOneElement() throws java.lang.Throwable {
            this.payloads.singleOrErrorOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorMultipleElements() throws java.lang.Throwable {
            this.payloads.singleOrErrorMultipleElements.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrErrorError() throws java.lang.Throwable {
            this.payloads.singleOrErrorError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1527Flowable() throws java.lang.Throwable {
            this.payloads.issue1527Flowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleElementOperatorDoNotSwallowExceptionWhenDone() throws java.lang.Throwable {
            this.payloads.singleElementOperatorDoNotSwallowExceptionWhenDone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAsFlowable() throws java.lang.Throwable {
            this.payloads.cancelAsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleOrError() throws java.lang.Throwable {
            this.payloads.singleOrError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSingleTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSingleTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSingleTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSingleTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableSingleTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSingleTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableSingleTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableSingleTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement singleFlowable;

            public org.junit.runners.model.Statement singleWithTooManyElementsFlowable;

            public org.junit.runners.model.Statement singleWithEmptyFlowable;

            public org.junit.runners.model.Statement singleDoesNotRequestMoreThanItNeedsIf1Then2RequestedFlowable;

            public org.junit.runners.model.Statement singleDoesNotRequestMoreThanItNeedsIf3RequestedFlowable;

            public org.junit.runners.model.Statement singleRequestsExactlyWhatItNeedsIf1RequestedFlowable;

            public org.junit.runners.model.Statement singleWithPredicateFlowable;

            public org.junit.runners.model.Statement singleWithPredicateAndTooManyElementsFlowable;

            public org.junit.runners.model.Statement singleWithPredicateAndEmptyFlowable;

            public org.junit.runners.model.Statement singleOrDefaultFlowable;

            public org.junit.runners.model.Statement singleOrDefaultWithTooManyElementsFlowable;

            public org.junit.runners.model.Statement singleOrDefaultWithEmptyFlowable;

            public org.junit.runners.model.Statement singleOrDefaultWithPredicateFlowable;

            public org.junit.runners.model.Statement singleOrDefaultWithPredicateAndTooManyElementsFlowable;

            public org.junit.runners.model.Statement singleOrDefaultWithPredicateAndEmptyFlowable;

            public org.junit.runners.model.Statement singleWithBackpressureFlowable;

            public org.junit.runners.model.Statement single;

            public org.junit.runners.model.Statement singleWithTooManyElements;

            public org.junit.runners.model.Statement singleWithEmpty;

            public org.junit.runners.model.Statement singleDoesNotRequestMoreThanItNeedsToEmitItem;

            public org.junit.runners.model.Statement singleDoesNotRequestMoreThanItNeedsToEmitErrorFromEmpty;

            public org.junit.runners.model.Statement singleDoesNotRequestMoreThanItNeedsToEmitErrorFromMoreThanOne;

            public org.junit.runners.model.Statement singleWithPredicate;

            public org.junit.runners.model.Statement singleWithPredicateAndTooManyElements;

            public org.junit.runners.model.Statement singleWithPredicateAndEmpty;

            public org.junit.runners.model.Statement singleOrDefault;

            public org.junit.runners.model.Statement singleOrDefaultWithTooManyElements;

            public org.junit.runners.model.Statement singleOrDefaultWithEmpty;

            public org.junit.runners.model.Statement singleOrDefaultWithPredicate;

            public org.junit.runners.model.Statement singleOrDefaultWithPredicateAndTooManyElements;

            public org.junit.runners.model.Statement singleOrDefaultWithPredicateAndEmpty;

            public org.junit.runners.model.Statement issue1527;

            public org.junit.runners.model.Statement singleOrErrorNoElement;

            public org.junit.runners.model.Statement singleOrErrorOneElement;

            public org.junit.runners.model.Statement singleOrErrorMultipleElements;

            public org.junit.runners.model.Statement singleOrErrorError;

            public org.junit.runners.model.Statement issue1527Flowable;

            public org.junit.runners.model.Statement singleElementOperatorDoNotSwallowExceptionWhenDone;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement cancelAsFlowable;

            public org.junit.runners.model.Statement singleOrError;

            public org.junit.runners.model.Statement dispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.singleFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleFlowable, "singleFlowable", this);
            this.payloads.singleWithTooManyElementsFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleWithTooManyElementsFlowable, "singleWithTooManyElementsFlowable", this);
            this.payloads.singleWithEmptyFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleWithEmptyFlowable, "singleWithEmptyFlowable", this);
            this.payloads.singleDoesNotRequestMoreThanItNeedsIf1Then2RequestedFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleDoesNotRequestMoreThanItNeedsIf1Then2RequestedFlowable, "singleDoesNotRequestMoreThanItNeedsIf1Then2RequestedFlowable", this);
            this.payloads.singleDoesNotRequestMoreThanItNeedsIf3RequestedFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleDoesNotRequestMoreThanItNeedsIf3RequestedFlowable, "singleDoesNotRequestMoreThanItNeedsIf3RequestedFlowable", this);
            this.payloads.singleRequestsExactlyWhatItNeedsIf1RequestedFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleRequestsExactlyWhatItNeedsIf1RequestedFlowable, "singleRequestsExactlyWhatItNeedsIf1RequestedFlowable", this);
            this.payloads.singleWithPredicateFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleWithPredicateFlowable, "singleWithPredicateFlowable", this);
            this.payloads.singleWithPredicateAndTooManyElementsFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleWithPredicateAndTooManyElementsFlowable, "singleWithPredicateAndTooManyElementsFlowable", this);
            this.payloads.singleWithPredicateAndEmptyFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleWithPredicateAndEmptyFlowable, "singleWithPredicateAndEmptyFlowable", this);
            this.payloads.singleOrDefaultFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleOrDefaultFlowable, "singleOrDefaultFlowable", this);
            this.payloads.singleOrDefaultWithTooManyElementsFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleOrDefaultWithTooManyElementsFlowable, "singleOrDefaultWithTooManyElementsFlowable", this);
            this.payloads.singleOrDefaultWithEmptyFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleOrDefaultWithEmptyFlowable, "singleOrDefaultWithEmptyFlowable", this);
            this.payloads.singleOrDefaultWithPredicateFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleOrDefaultWithPredicateFlowable, "singleOrDefaultWithPredicateFlowable", this);
            this.payloads.singleOrDefaultWithPredicateAndTooManyElementsFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleOrDefaultWithPredicateAndTooManyElementsFlowable, "singleOrDefaultWithPredicateAndTooManyElementsFlowable", this);
            this.payloads.singleOrDefaultWithPredicateAndEmptyFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleOrDefaultWithPredicateAndEmptyFlowable, "singleOrDefaultWithPredicateAndEmptyFlowable", this);
            this.payloads.singleWithBackpressureFlowable = _ClassStatement.forPayload(FlowableSingleTest::singleWithBackpressureFlowable, "singleWithBackpressureFlowable", this);
            this.payloads.single = _ClassStatement.forPayload(FlowableSingleTest::single, "single", this);
            this.payloads.singleWithTooManyElements = _ClassStatement.forPayload(FlowableSingleTest::singleWithTooManyElements, "singleWithTooManyElements", this);
            this.payloads.singleWithEmpty = _ClassStatement.forPayload(FlowableSingleTest::singleWithEmpty, "singleWithEmpty", this);
            this.payloads.singleDoesNotRequestMoreThanItNeedsToEmitItem = _ClassStatement.forPayload(FlowableSingleTest::singleDoesNotRequestMoreThanItNeedsToEmitItem, "singleDoesNotRequestMoreThanItNeedsToEmitItem", this);
            this.payloads.singleDoesNotRequestMoreThanItNeedsToEmitErrorFromEmpty = _ClassStatement.forPayload(FlowableSingleTest::singleDoesNotRequestMoreThanItNeedsToEmitErrorFromEmpty, "singleDoesNotRequestMoreThanItNeedsToEmitErrorFromEmpty", this);
            this.payloads.singleDoesNotRequestMoreThanItNeedsToEmitErrorFromMoreThanOne = _ClassStatement.forPayload(FlowableSingleTest::singleDoesNotRequestMoreThanItNeedsToEmitErrorFromMoreThanOne, "singleDoesNotRequestMoreThanItNeedsToEmitErrorFromMoreThanOne", this);
            this.payloads.singleWithPredicate = _ClassStatement.forPayload(FlowableSingleTest::singleWithPredicate, "singleWithPredicate", this);
            this.payloads.singleWithPredicateAndTooManyElements = _ClassStatement.forPayload(FlowableSingleTest::singleWithPredicateAndTooManyElements, "singleWithPredicateAndTooManyElements", this);
            this.payloads.singleWithPredicateAndEmpty = _ClassStatement.forPayload(FlowableSingleTest::singleWithPredicateAndEmpty, "singleWithPredicateAndEmpty", this);
            this.payloads.singleOrDefault = _ClassStatement.forPayload(FlowableSingleTest::singleOrDefault, "singleOrDefault", this);
            this.payloads.singleOrDefaultWithTooManyElements = _ClassStatement.forPayload(FlowableSingleTest::singleOrDefaultWithTooManyElements, "singleOrDefaultWithTooManyElements", this);
            this.payloads.singleOrDefaultWithEmpty = _ClassStatement.forPayload(FlowableSingleTest::singleOrDefaultWithEmpty, "singleOrDefaultWithEmpty", this);
            this.payloads.singleOrDefaultWithPredicate = _ClassStatement.forPayload(FlowableSingleTest::singleOrDefaultWithPredicate, "singleOrDefaultWithPredicate", this);
            this.payloads.singleOrDefaultWithPredicateAndTooManyElements = _ClassStatement.forPayload(FlowableSingleTest::singleOrDefaultWithPredicateAndTooManyElements, "singleOrDefaultWithPredicateAndTooManyElements", this);
            this.payloads.singleOrDefaultWithPredicateAndEmpty = _ClassStatement.forPayload(FlowableSingleTest::singleOrDefaultWithPredicateAndEmpty, "singleOrDefaultWithPredicateAndEmpty", this);
            this.payloads.issue1527 = _ClassStatement.forPayload(FlowableSingleTest::issue1527, "issue1527", this);
            this.payloads.singleOrErrorNoElement = _ClassStatement.forPayload(FlowableSingleTest::singleOrErrorNoElement, "singleOrErrorNoElement", this);
            this.payloads.singleOrErrorOneElement = _ClassStatement.forPayload(FlowableSingleTest::singleOrErrorOneElement, "singleOrErrorOneElement", this);
            this.payloads.singleOrErrorMultipleElements = _ClassStatement.forPayload(FlowableSingleTest::singleOrErrorMultipleElements, "singleOrErrorMultipleElements", this);
            this.payloads.singleOrErrorError = _ClassStatement.forPayload(FlowableSingleTest::singleOrErrorError, "singleOrErrorError", this);
            this.payloads.issue1527Flowable = _ClassStatement.forPayload(FlowableSingleTest::issue1527Flowable, "issue1527Flowable", this);
            this.payloads.singleElementOperatorDoNotSwallowExceptionWhenDone = _ClassStatement.forPayload(FlowableSingleTest::singleElementOperatorDoNotSwallowExceptionWhenDone, "singleElementOperatorDoNotSwallowExceptionWhenDone", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableSingleTest::badSource, "badSource", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableSingleTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.cancelAsFlowable = _ClassStatement.forPayload(FlowableSingleTest::cancelAsFlowable, "cancelAsFlowable", this);
            this.payloads.singleOrError = _ClassStatement.forPayload(FlowableSingleTest::singleOrError, "singleOrError", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableSingleTest::dispose, "dispose", this);
        }
    }
}
