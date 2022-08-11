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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableFlattenIterable.FlattenIterableSubscriber;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.QueueSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableFlattenIterableTest extends RxJavaTest {

    @Test
    public void normal0() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 2).reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return Math.max(a, b);
            }
        }).toFlowable().flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return Arrays.asList(v, v + 1);
            }
        }).subscribe(ts);
        ts.assertValues(2, 3).assertNoErrors().assertComplete();
    }

    final Function<Integer, Iterable<Integer>> mapper = new Function<Integer, Iterable<Integer>>() {

        @Override
        public Iterable<Integer> apply(Integer v) {
            return Arrays.asList(v, v + 1);
        }
    };

    @Test
    public void normal() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).concatMapIterable(mapper).subscribe(ts);
        ts.assertValues(1, 2, 2, 3, 3, 4, 4, 5, 5, 6);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void normalViaFlatMap() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).flatMapIterable(mapper).subscribe(ts);
        ts.assertValues(1, 2, 2, 3, 3, 4, 4, 5, 5, 6);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void normalBackpressured() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0);
        Flowable.range(1, 5).concatMapIterable(mapper).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(2);
        ts.assertValues(1, 2, 2);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(7);
        ts.assertValues(1, 2, 2, 3, 3, 4, 4, 5, 5, 6);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void longRunning() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        int n = 1000 * 1000;
        Flowable.range(1, n).concatMapIterable(mapper).subscribe(ts);
        ts.assertValueCount(n * 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void asIntermediate() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        int n = 1000 * 1000;
        Flowable.range(1, n).concatMapIterable(mapper).concatMap(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return Flowable.just(v);
            }
        }).subscribe(ts);
        ts.assertValueCount(n * 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void just() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.just(1).concatMapIterable(mapper).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void justHidden() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.just(1).hide().concatMapIterable(mapper).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void empty() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.<Integer>empty().concatMapIterable(mapper).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void error() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.<Integer>just(1).concatWith(Flowable.<Integer>error(new TestException())).concatMapIterable(mapper).subscribe(ts);
        ts.assertValues(1, 2);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void iteratorHasNextThrowsImmediately() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        throw new TestException();
                    }

                    @Override
                    public Integer next() {
                        return 1;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        Flowable.range(1, 2).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return it;
            }
        }).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void iteratorHasNextThrowsImmediatelyJust() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        throw new TestException();
                    }

                    @Override
                    public Integer next() {
                        return 1;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        Flowable.just(1).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return it;
            }
        }).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void iteratorHasNextThrowsSecondCall() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    int count;

                    @Override
                    public boolean hasNext() {
                        if (++count >= 2) {
                            throw new TestException();
                        }
                        return true;
                    }

                    @Override
                    public Integer next() {
                        return 1;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        Flowable.range(1, 2).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return it;
            }
        }).subscribe(ts);
        ts.assertValue(1);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void iteratorNextThrows() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public Integer next() {
                        throw new TestException();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        Flowable.range(1, 2).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return it;
            }
        }).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void iteratorNextThrowsAndUnsubscribes() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public Integer next() {
                        throw new TestException();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return it;
            }
        }).subscribe(ts);
        pp.onNext(1);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
        Assert.assertFalse("PublishProcessor has Subscribers?!", pp.hasSubscribers());
    }

    @Test
    public void mixture() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(0, 1000).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return (v % 2) == 0 ? Collections.singleton(1) : Collections.<Integer>emptySet();
            }
        }).subscribe(ts);
        ts.assertValueCount(500);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void emptyInnerThenSingleBackpressured() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(1);
        Flowable.range(1, 2).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return v == 2 ? Collections.singleton(1) : Collections.<Integer>emptySet();
            }
        }).subscribe(ts);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void manyEmptyInnerThenSingleBackpressured() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(1);
        Flowable.range(1, 1000).concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return v == 1000 ? Collections.singleton(1) : Collections.<Integer>emptySet();
            }
        }).subscribe(ts);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void hasNextIsNotCalledAfterChildUnsubscribedOnNext() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final AtomicInteger counter = new AtomicInteger();
        final Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        counter.getAndIncrement();
                        return true;
                    }

                    @Override
                    public Integer next() {
                        return 1;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.concatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return it;
            }
        }).take(1).subscribe(ts);
        pp.onNext(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
        Assert.assertFalse("PublishProcessor has Subscribers?!", pp.hasSubscribers());
        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void normalPrefetchViaFlatMap() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 5).flatMapIterable(mapper, 2).subscribe(ts);
        ts.assertValues(1, 2, 2, 3, 3, 4, 4, 5, 5, 6);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void withResultSelectorMaxConcurrent() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(1, 5).flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) {
                return Collections.singletonList(1);
            }
        }, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) {
                return a * 10 + b;
            }
        }, 2).subscribe(ts);
        ts.assertValues(11, 21, 31, 41, 51);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void flatMapIterablePrefetch() {
        Flowable.just(1, 2).flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer t) throws Exception {
                return Arrays.asList(t * 10);
            }
        }, 1).test().assertResult(10, 20);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().flatMapIterable(new Function<Object, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Object v) throws Exception {
                return Arrays.asList(10, 20);
            }
        }));
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.flatMapIterable(new Function<Object, Iterable<Integer>>() {

                    @Override
                    public Iterable<Integer> apply(Object v) throws Exception {
                        return Arrays.asList(10, 20);
                    }
                });
            }
        }, false, 1, 1, 10, 20);
    }

    @Test
    public void callableThrows() {
        Flowable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw new TestException();
            }
        }).flatMapIterable(Functions.justFunction(Arrays.asList(1, 2, 3))).test().assertFailure(TestException.class);
    }

    @Test
    public void fusionMethods() {
        Flowable.just(1, 2).flatMapIterable(Functions.justFunction(Arrays.asList(1, 2, 3))).subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                @SuppressWarnings("unchecked")
                QueueSubscription<Integer> qs = (QueueSubscription<Integer>) s;
                assertEquals(QueueFuseable.SYNC, qs.requestFusion(QueueFuseable.ANY));
                try {
                    assertFalse("Source reports being empty!", qs.isEmpty());
                    assertEquals(1, qs.poll().intValue());
                    assertFalse("Source reports being empty!", qs.isEmpty());
                    assertEquals(2, qs.poll().intValue());
                    assertFalse("Source reports being empty!", qs.isEmpty());
                    qs.clear();
                    assertTrue("Source reports not empty!", qs.isEmpty());
                    assertNull(qs.poll());
                } catch (Throwable ex) {
                    throw ExceptionHelper.wrapOrThrow(ex);
                }
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void smallPrefetch() {
        Flowable.just(1, 2, 3).flatMapIterable(Functions.justFunction(Arrays.asList(1, 2, 3)), 1).test().assertResult(1, 2, 3, 1, 2, 3, 1, 2, 3);
    }

    @Test
    public void smallPrefetch2() {
        Flowable.just(1, 2, 3).hide().flatMapIterable(Functions.justFunction(Collections.emptyList()), 1).test().assertResult();
    }

    @Test
    public void mixedInnerSource() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.just(1, 2, 3).flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                if ((v & 1) == 0) {
                    return Collections.emptyList();
                }
                return Arrays.asList(1, 2);
            }
        }).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 1, 2);
    }

    @Test
    public void mixedInnerSource2() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.just(1, 2, 3).flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                if ((v & 1) == 1) {
                    return Collections.emptyList();
                }
                return Arrays.asList(1, 2);
            }
        }).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2);
    }

    @Test
    public void fusionRejected() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.just(1, 2, 3).hide().flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return Arrays.asList(1, 2);
            }
        }).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 1, 2, 1, 2);
    }

    @Test
    public void fusedIsEmptyWithEmptySource() {
        Flowable.just(1, 2, 3).flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                if ((v & 1) == 0) {
                    return Collections.emptyList();
                }
                return Arrays.asList(v);
            }
        }).subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                @SuppressWarnings("unchecked")
                QueueSubscription<Integer> qs = (QueueSubscription<Integer>) s;
                assertEquals(QueueFuseable.SYNC, qs.requestFusion(QueueFuseable.ANY));
                try {
                    assertFalse("Source reports being empty!", qs.isEmpty());
                    assertEquals(1, qs.poll().intValue());
                    assertFalse("Source reports being empty!", qs.isEmpty());
                    assertEquals(3, qs.poll().intValue());
                    assertTrue("Source reports being non-empty!", qs.isEmpty());
                } catch (Throwable ex) {
                    throw ExceptionHelper.wrapOrThrow(ex);
                }
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void fusedSourceCrash() {
        Flowable.range(1, 3).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).flatMapIterable(Functions.justFunction(Collections.emptyList()), 1).test().assertFailure(TestException.class);
    }

    @Test
    public void take() {
        Flowable.range(1, 3).flatMapIterable(Functions.justFunction(Arrays.asList(1)), 1).take(1).test().assertResult(1);
    }

    @Test
    public void overflowSource() {
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onNext(1);
                s.onNext(2);
                s.onNext(3);
            }
        }.flatMapIterable(Functions.justFunction(Arrays.asList(1)), 1).test(0L).assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void oneByOne() {
        Flowable.range(1, 3).hide().flatMapIterable(Functions.justFunction(Arrays.asList(1)), 1).rebatchRequests(1).test().assertResult(1, 1, 1);
    }

    @Test
    public void cancelAfterHasNext() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 3).hide().flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new Iterable<Integer>() {

                    int count;

                    @Override
                    public Iterator<Integer> iterator() {
                        return new Iterator<Integer>() {

                            @Override
                            public boolean hasNext() {
                                if (++count == 2) {
                                    ts.cancel();
                                    ts.onComplete();
                                }
                                return true;
                            }

                            @Override
                            public Integer next() {
                                return 1;
                            }

                            @Override
                            public void remove() {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                };
            }
        }).subscribe(ts);
        ts.assertResult(1);
    }

    @Test
    public void doubleShare() {
        Iterable<Integer> it = Flowable.range(1, 300).blockingIterable();
        Flowable.just(it, it).flatMapIterable(Functions.<Iterable<Integer>>identity()).share().share().count().test().assertResult(600L);
    }

    @Test
    public void multiShare() {
        Iterable<Integer> it = Flowable.range(1, 300).blockingIterable();
        for (int i = 0; i < 5; i++) {
            Flowable<Integer> f = Flowable.just(it, it).flatMapIterable(Functions.<Iterable<Integer>>identity());
            for (int j = 0; j < i; j++) {
                f = f.share();
            }
            f.count().test().withTag("Share: " + i).assertResult(600L);
        }
    }

    @Test
    public void multiShareHidden() {
        Iterable<Integer> it = Flowable.range(1, 300).blockingIterable();
        for (int i = 0; i < 5; i++) {
            Flowable<Integer> f = Flowable.just(it, it).flatMapIterable(Functions.<Iterable<Integer>>identity()).hide();
            for (int j = 0; j < i; j++) {
                f = f.share();
            }
            f.count().test().withTag("Share: " + i).assertResult(600L);
        }
    }

    @Test
    public void failingInnerCancelsSource() {
        final AtomicInteger counter = new AtomicInteger();
        Flowable.range(1, 5).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                counter.getAndIncrement();
            }
        }).flatMapIterable(new Function<Integer, Iterable<Integer>>() {

            @Override
            public Iterable<Integer> apply(Integer v) throws Exception {
                return new Iterable<Integer>() {

                    @Override
                    public Iterator<Integer> iterator() {
                        return new Iterator<Integer>() {

                            @Override
                            public boolean hasNext() {
                                return true;
                            }

                            @Override
                            public Integer next() {
                                throw new TestException();
                            }

                            @Override
                            public void remove() {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                };
            }
        }).test().assertFailure(TestException.class);
        assertEquals(1, counter.get());
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.flatMapIterable(Functions.justFunction(Collections.emptyList()));
            }
        });
    }

    @Test
    public void upstreamFusionRejected() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        FlattenIterableSubscriber<Integer, Integer> f = new FlattenIterableSubscriber<>(ts, Functions.justFunction(Collections.<Integer>emptyList()), 128);
        final AtomicLong requested = new AtomicLong();
        f.onSubscribe(new QueueSubscription<Integer>() {

            @Override
            public int requestFusion(int mode) {
                return 0;
            }

            @Override
            public boolean offer(Integer value) {
                return false;
            }

            @Override
            public boolean offer(Integer v1, Integer v2) {
                return false;
            }

            @Override
            public Integer poll() throws Exception {
                return null;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public void clear() {
            }

            @Override
            public void request(long n) {
                requested.set(n);
            }

            @Override
            public void cancel() {
            }
        });
        assertEquals(128, requested.get());
        assertNotNull(f.queue);
        ts.assertEmpty();
    }

    @Test
    public void onErrorLate() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
            FlattenIterableSubscriber<Integer, Integer> f = new FlattenIterableSubscriber<>(ts, Functions.justFunction(Collections.<Integer>emptyList()), 128);
            f.onSubscribe(new BooleanSubscription());
            f.onError(new TestException("first"));
            ts.assertFailureAndMessage(TestException.class, "first");
            assertTrue(errors.isEmpty());
            f.done = false;
            f.onError(new TestException("second"));
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().flatMapIterable(Functions.justFunction(Collections.emptyList())));
    }

    @Test
    public void fusedCurrentIteratorEmpty() throws Throwable {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0);
        FlattenIterableSubscriber<Integer, Integer> f = new FlattenIterableSubscriber<>(ts, Functions.justFunction(Arrays.<Integer>asList(1, 2)), 128);
        f.onSubscribe(new BooleanSubscription());
        f.onNext(1);
        assertFalse(f.isEmpty());
        assertEquals(1, f.poll().intValue());
        assertFalse(f.isEmpty());
        assertEquals(2, f.poll().intValue());
        assertTrue(f.isEmpty());
    }

    @Test
    public void fusionRequestedState() throws Exception {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0);
        FlattenIterableSubscriber<Integer, Integer> f = new FlattenIterableSubscriber<>(ts, Functions.justFunction(Arrays.<Integer>asList(1, 2)), 128);
        f.onSubscribe(new BooleanSubscription());
        f.fusionMode = QueueFuseable.NONE;
        assertEquals(QueueFuseable.NONE, f.requestFusion(QueueFuseable.SYNC));
        assertEquals(QueueFuseable.NONE, f.requestFusion(QueueFuseable.ASYNC));
        f.fusionMode = QueueFuseable.SYNC;
        assertEquals(QueueFuseable.SYNC, f.requestFusion(QueueFuseable.SYNC));
        assertEquals(QueueFuseable.NONE, f.requestFusion(QueueFuseable.ASYNC));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableFlattenIterableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal0() throws java.lang.Throwable {
            this.payloads.normal0.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalViaFlatMap() throws java.lang.Throwable {
            this.payloads.normalViaFlatMap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalBackpressured() throws java.lang.Throwable {
            this.payloads.normalBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_longRunning() throws java.lang.Throwable {
            this.payloads.longRunning.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asIntermediate() throws java.lang.Throwable {
            this.payloads.asIntermediate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.payloads.just.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justHidden() throws java.lang.Throwable {
            this.payloads.justHidden.evaluate();
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
        public void benchmark_iteratorHasNextThrowsImmediately() throws java.lang.Throwable {
            this.payloads.iteratorHasNextThrowsImmediately.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorHasNextThrowsImmediatelyJust() throws java.lang.Throwable {
            this.payloads.iteratorHasNextThrowsImmediatelyJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorHasNextThrowsSecondCall() throws java.lang.Throwable {
            this.payloads.iteratorHasNextThrowsSecondCall.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorNextThrows() throws java.lang.Throwable {
            this.payloads.iteratorNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorNextThrowsAndUnsubscribes() throws java.lang.Throwable {
            this.payloads.iteratorNextThrowsAndUnsubscribes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixture() throws java.lang.Throwable {
            this.payloads.mixture.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyInnerThenSingleBackpressured() throws java.lang.Throwable {
            this.payloads.emptyInnerThenSingleBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyEmptyInnerThenSingleBackpressured() throws java.lang.Throwable {
            this.payloads.manyEmptyInnerThenSingleBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextIsNotCalledAfterChildUnsubscribedOnNext() throws java.lang.Throwable {
            this.payloads.hasNextIsNotCalledAfterChildUnsubscribedOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalPrefetchViaFlatMap() throws java.lang.Throwable {
            this.payloads.normalPrefetchViaFlatMap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withResultSelectorMaxConcurrent() throws java.lang.Throwable {
            this.payloads.withResultSelectorMaxConcurrent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapIterablePrefetch() throws java.lang.Throwable {
            this.payloads.flatMapIterablePrefetch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_callableThrows() throws java.lang.Throwable {
            this.payloads.callableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionMethods() throws java.lang.Throwable {
            this.payloads.fusionMethods.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_smallPrefetch() throws java.lang.Throwable {
            this.payloads.smallPrefetch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_smallPrefetch2() throws java.lang.Throwable {
            this.payloads.smallPrefetch2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixedInnerSource() throws java.lang.Throwable {
            this.payloads.mixedInnerSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixedInnerSource2() throws java.lang.Throwable {
            this.payloads.mixedInnerSource2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejected() throws java.lang.Throwable {
            this.payloads.fusionRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedIsEmptyWithEmptySource() throws java.lang.Throwable {
            this.payloads.fusedIsEmptyWithEmptySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedSourceCrash() throws java.lang.Throwable {
            this.payloads.fusedSourceCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overflowSource() throws java.lang.Throwable {
            this.payloads.overflowSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneByOne() throws java.lang.Throwable {
            this.payloads.oneByOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterHasNext() throws java.lang.Throwable {
            this.payloads.cancelAfterHasNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleShare() throws java.lang.Throwable {
            this.payloads.doubleShare.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multiShare() throws java.lang.Throwable {
            this.payloads.multiShare.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multiShareHidden() throws java.lang.Throwable {
            this.payloads.multiShareHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_failingInnerCancelsSource() throws java.lang.Throwable {
            this.payloads.failingInnerCancelsSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamFusionRejected() throws java.lang.Throwable {
            this.payloads.upstreamFusionRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorLate() throws java.lang.Throwable {
            this.payloads.onErrorLate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedCurrentIteratorEmpty() throws java.lang.Throwable {
            this.payloads.fusedCurrentIteratorEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRequestedState() throws java.lang.Throwable {
            this.payloads.fusionRequestedState.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlattenIterableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlattenIterableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlattenIterableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlattenIterableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableFlattenIterableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlattenIterableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableFlattenIterableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableFlattenIterableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal0;

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement normalViaFlatMap;

            public org.junit.runners.model.Statement normalBackpressured;

            public org.junit.runners.model.Statement longRunning;

            public org.junit.runners.model.Statement asIntermediate;

            public org.junit.runners.model.Statement just;

            public org.junit.runners.model.Statement justHidden;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement iteratorHasNextThrowsImmediately;

            public org.junit.runners.model.Statement iteratorHasNextThrowsImmediatelyJust;

            public org.junit.runners.model.Statement iteratorHasNextThrowsSecondCall;

            public org.junit.runners.model.Statement iteratorNextThrows;

            public org.junit.runners.model.Statement iteratorNextThrowsAndUnsubscribes;

            public org.junit.runners.model.Statement mixture;

            public org.junit.runners.model.Statement emptyInnerThenSingleBackpressured;

            public org.junit.runners.model.Statement manyEmptyInnerThenSingleBackpressured;

            public org.junit.runners.model.Statement hasNextIsNotCalledAfterChildUnsubscribedOnNext;

            public org.junit.runners.model.Statement normalPrefetchViaFlatMap;

            public org.junit.runners.model.Statement withResultSelectorMaxConcurrent;

            public org.junit.runners.model.Statement flatMapIterablePrefetch;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement callableThrows;

            public org.junit.runners.model.Statement fusionMethods;

            public org.junit.runners.model.Statement smallPrefetch;

            public org.junit.runners.model.Statement smallPrefetch2;

            public org.junit.runners.model.Statement mixedInnerSource;

            public org.junit.runners.model.Statement mixedInnerSource2;

            public org.junit.runners.model.Statement fusionRejected;

            public org.junit.runners.model.Statement fusedIsEmptyWithEmptySource;

            public org.junit.runners.model.Statement fusedSourceCrash;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement overflowSource;

            public org.junit.runners.model.Statement oneByOne;

            public org.junit.runners.model.Statement cancelAfterHasNext;

            public org.junit.runners.model.Statement doubleShare;

            public org.junit.runners.model.Statement multiShare;

            public org.junit.runners.model.Statement multiShareHidden;

            public org.junit.runners.model.Statement failingInnerCancelsSource;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement upstreamFusionRejected;

            public org.junit.runners.model.Statement onErrorLate;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement fusedCurrentIteratorEmpty;

            public org.junit.runners.model.Statement fusionRequestedState;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal0 = _ClassStatement.forPayload(FlowableFlattenIterableTest::normal0, "normal0", this);
            this.payloads.normal = _ClassStatement.forPayload(FlowableFlattenIterableTest::normal, "normal", this);
            this.payloads.normalViaFlatMap = _ClassStatement.forPayload(FlowableFlattenIterableTest::normalViaFlatMap, "normalViaFlatMap", this);
            this.payloads.normalBackpressured = _ClassStatement.forPayload(FlowableFlattenIterableTest::normalBackpressured, "normalBackpressured", this);
            this.payloads.longRunning = _ClassStatement.forPayload(FlowableFlattenIterableTest::longRunning, "longRunning", this);
            this.payloads.asIntermediate = _ClassStatement.forPayload(FlowableFlattenIterableTest::asIntermediate, "asIntermediate", this);
            this.payloads.just = _ClassStatement.forPayload(FlowableFlattenIterableTest::just, "just", this);
            this.payloads.justHidden = _ClassStatement.forPayload(FlowableFlattenIterableTest::justHidden, "justHidden", this);
            this.payloads.empty = _ClassStatement.forPayload(FlowableFlattenIterableTest::empty, "empty", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableFlattenIterableTest::error, "error", this);
            this.payloads.iteratorHasNextThrowsImmediately = _ClassStatement.forPayload(FlowableFlattenIterableTest::iteratorHasNextThrowsImmediately, "iteratorHasNextThrowsImmediately", this);
            this.payloads.iteratorHasNextThrowsImmediatelyJust = _ClassStatement.forPayload(FlowableFlattenIterableTest::iteratorHasNextThrowsImmediatelyJust, "iteratorHasNextThrowsImmediatelyJust", this);
            this.payloads.iteratorHasNextThrowsSecondCall = _ClassStatement.forPayload(FlowableFlattenIterableTest::iteratorHasNextThrowsSecondCall, "iteratorHasNextThrowsSecondCall", this);
            this.payloads.iteratorNextThrows = _ClassStatement.forPayload(FlowableFlattenIterableTest::iteratorNextThrows, "iteratorNextThrows", this);
            this.payloads.iteratorNextThrowsAndUnsubscribes = _ClassStatement.forPayload(FlowableFlattenIterableTest::iteratorNextThrowsAndUnsubscribes, "iteratorNextThrowsAndUnsubscribes", this);
            this.payloads.mixture = _ClassStatement.forPayload(FlowableFlattenIterableTest::mixture, "mixture", this);
            this.payloads.emptyInnerThenSingleBackpressured = _ClassStatement.forPayload(FlowableFlattenIterableTest::emptyInnerThenSingleBackpressured, "emptyInnerThenSingleBackpressured", this);
            this.payloads.manyEmptyInnerThenSingleBackpressured = _ClassStatement.forPayload(FlowableFlattenIterableTest::manyEmptyInnerThenSingleBackpressured, "manyEmptyInnerThenSingleBackpressured", this);
            this.payloads.hasNextIsNotCalledAfterChildUnsubscribedOnNext = _ClassStatement.forPayload(FlowableFlattenIterableTest::hasNextIsNotCalledAfterChildUnsubscribedOnNext, "hasNextIsNotCalledAfterChildUnsubscribedOnNext", this);
            this.payloads.normalPrefetchViaFlatMap = _ClassStatement.forPayload(FlowableFlattenIterableTest::normalPrefetchViaFlatMap, "normalPrefetchViaFlatMap", this);
            this.payloads.withResultSelectorMaxConcurrent = _ClassStatement.forPayload(FlowableFlattenIterableTest::withResultSelectorMaxConcurrent, "withResultSelectorMaxConcurrent", this);
            this.payloads.flatMapIterablePrefetch = _ClassStatement.forPayload(FlowableFlattenIterableTest::flatMapIterablePrefetch, "flatMapIterablePrefetch", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableFlattenIterableTest::dispose, "dispose", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableFlattenIterableTest::badSource, "badSource", this);
            this.payloads.callableThrows = _ClassStatement.forPayload(FlowableFlattenIterableTest::callableThrows, "callableThrows", this);
            this.payloads.fusionMethods = _ClassStatement.forPayload(FlowableFlattenIterableTest::fusionMethods, "fusionMethods", this);
            this.payloads.smallPrefetch = _ClassStatement.forPayload(FlowableFlattenIterableTest::smallPrefetch, "smallPrefetch", this);
            this.payloads.smallPrefetch2 = _ClassStatement.forPayload(FlowableFlattenIterableTest::smallPrefetch2, "smallPrefetch2", this);
            this.payloads.mixedInnerSource = _ClassStatement.forPayload(FlowableFlattenIterableTest::mixedInnerSource, "mixedInnerSource", this);
            this.payloads.mixedInnerSource2 = _ClassStatement.forPayload(FlowableFlattenIterableTest::mixedInnerSource2, "mixedInnerSource2", this);
            this.payloads.fusionRejected = _ClassStatement.forPayload(FlowableFlattenIterableTest::fusionRejected, "fusionRejected", this);
            this.payloads.fusedIsEmptyWithEmptySource = _ClassStatement.forPayload(FlowableFlattenIterableTest::fusedIsEmptyWithEmptySource, "fusedIsEmptyWithEmptySource", this);
            this.payloads.fusedSourceCrash = _ClassStatement.forPayload(FlowableFlattenIterableTest::fusedSourceCrash, "fusedSourceCrash", this);
            this.payloads.take = _ClassStatement.forPayload(FlowableFlattenIterableTest::take, "take", this);
            this.payloads.overflowSource = _ClassStatement.forPayload(FlowableFlattenIterableTest::overflowSource, "overflowSource", this);
            this.payloads.oneByOne = _ClassStatement.forPayload(FlowableFlattenIterableTest::oneByOne, "oneByOne", this);
            this.payloads.cancelAfterHasNext = _ClassStatement.forPayload(FlowableFlattenIterableTest::cancelAfterHasNext, "cancelAfterHasNext", this);
            this.payloads.doubleShare = _ClassStatement.forPayload(FlowableFlattenIterableTest::doubleShare, "doubleShare", this);
            this.payloads.multiShare = _ClassStatement.forPayload(FlowableFlattenIterableTest::multiShare, "multiShare", this);
            this.payloads.multiShareHidden = _ClassStatement.forPayload(FlowableFlattenIterableTest::multiShareHidden, "multiShareHidden", this);
            this.payloads.failingInnerCancelsSource = _ClassStatement.forPayload(FlowableFlattenIterableTest::failingInnerCancelsSource, "failingInnerCancelsSource", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableFlattenIterableTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.upstreamFusionRejected = _ClassStatement.forPayload(FlowableFlattenIterableTest::upstreamFusionRejected, "upstreamFusionRejected", this);
            this.payloads.onErrorLate = _ClassStatement.forPayload(FlowableFlattenIterableTest::onErrorLate, "onErrorLate", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableFlattenIterableTest::badRequest, "badRequest", this);
            this.payloads.fusedCurrentIteratorEmpty = _ClassStatement.forPayload(FlowableFlattenIterableTest::fusedCurrentIteratorEmpty, "fusedCurrentIteratorEmpty", this);
            this.payloads.fusionRequestedState = _ClassStatement.forPayload(FlowableFlattenIterableTest::fusionRequestedState, "fusionRequestedState", this);
        }
    }
}
