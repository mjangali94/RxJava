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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.reactivestreams.*;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.util.CrashingIterable;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.QueueSubscription;
import io.reactivex.rxjava3.operators.SimpleQueue;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableFromIterableTest extends RxJavaTest {

    @Test
    public void listIterable() {
        Flowable<String> flowable = Flowable.fromIterable(Arrays.<String>asList("one", "two", "three"));
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext("one");
        verify(subscriber, times(1)).onNext("two");
        verify(subscriber, times(1)).onNext("three");
        verify(subscriber, Mockito.never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    /**
     * This tests the path that can not optimize based on size so must use setProducer.
     */
    @Test
    public void rawIterable() {
        Iterable<String> it = new Iterable<String>() {

            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {

                    int i;

                    @Override
                    public boolean hasNext() {
                        return i < 3;
                    }

                    @Override
                    public String next() {
                        return String.valueOf(++i);
                    }

                    @Override
                    public void remove() {
                    }
                };
            }
        };
        Flowable<String> flowable = Flowable.fromIterable(it);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext("1");
        verify(subscriber, times(1)).onNext("2");
        verify(subscriber, times(1)).onNext("3");
        verify(subscriber, Mockito.never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void observableFromIterable() {
        Flowable<String> flowable = Flowable.fromIterable(Arrays.<String>asList("one", "two", "three"));
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext("one");
        verify(subscriber, times(1)).onNext("two");
        verify(subscriber, times(1)).onNext("three");
        verify(subscriber, Mockito.never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void backpressureViaRequest() {
        ArrayList<Integer> list = new ArrayList<>(Flowable.bufferSize());
        for (int i = 1; i <= Flowable.bufferSize() + 1; i++) {
            list.add(i);
        }
        Flowable<Integer> f = Flowable.fromIterable(list);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0L);
        ts.assertNoValues();
        ts.request(1);
        f.subscribe(ts);
        ts.assertValue(1);
        ts.request(2);
        ts.assertValues(1, 2, 3);
        ts.request(3);
        ts.assertValues(1, 2, 3, 4, 5, 6);
        ts.request(list.size());
        ts.assertTerminated();
    }

    @Test
    public void noBackpressure() {
        Flowable<Integer> f = Flowable.fromIterable(Arrays.asList(1, 2, 3, 4, 5));
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0L);
        ts.assertNoValues();
        // infinite
        ts.request(Long.MAX_VALUE);
        f.subscribe(ts);
        ts.assertValues(1, 2, 3, 4, 5);
        ts.assertTerminated();
    }

    @Test
    public void subscribeMultipleTimes() {
        Flowable<Integer> f = Flowable.fromIterable(Arrays.asList(1, 2, 3));
        for (int i = 0; i < 10; i++) {
            TestSubscriber<Integer> ts = new TestSubscriber<>();
            f.subscribe(ts);
            ts.assertValues(1, 2, 3);
            ts.assertNoErrors();
            ts.assertComplete();
        }
    }

    @Test
    public void fromIterableRequestOverflow() throws InterruptedException {
        Flowable<Integer> f = Flowable.fromIterable(Arrays.asList(1, 2, 3, 4));
        final int expectedCount = 4;
        final CountDownLatch latch = new CountDownLatch(expectedCount);
        f.subscribeOn(Schedulers.computation()).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(2);
            }

            @Override
            public void onComplete() {
            // ignore
            }

            @Override
            public void onError(Throwable e) {
                throw new RuntimeException(e);
            }

            @Override
            public void onNext(Integer t) {
                latch.countDown();
                request(Long.MAX_VALUE - 1);
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void fromEmptyIterableWhenZeroRequestedShouldStillEmitOnCompletedEagerly() {
        final AtomicBoolean completed = new AtomicBoolean(false);
        Flowable.fromIterable(Collections.emptyList()).subscribe(new DefaultSubscriber<Object>() {

            @Override
            public void onStart() {
            // request(0);
            }

            @Override
            public void onComplete() {
                completed.set(true);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Object t) {
            }
        });
        assertTrue(completed.get());
    }

    @Test
    public void doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure() {
        final AtomicBoolean called = new AtomicBoolean(false);
        Iterable<Integer> iterable = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    int count = 1;

                    @Override
                    public void remove() {
                    // ignore
                    }

                    @Override
                    public boolean hasNext() {
                        if (count > 1) {
                            called.set(true);
                            return false;
                        }
                        return true;
                    }

                    @Override
                    public Integer next() {
                        return count++;
                    }
                };
            }
        };
        Flowable.fromIterable(iterable).take(1).subscribe();
        assertFalse(called.get());
    }

    @Test
    public void doesNotCallIteratorHasNextMoreThanRequiredFastPath() {
        final AtomicBoolean called = new AtomicBoolean(false);
        Iterable<Integer> iterable = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public void remove() {
                    // ignore
                    }

                    int count = 1;

                    @Override
                    public boolean hasNext() {
                        if (count > 1) {
                            called.set(true);
                            return false;
                        }
                        return true;
                    }

                    @Override
                    public Integer next() {
                        return count++;
                    }
                };
            }
        };
        Flowable.fromIterable(iterable).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
                // unsubscribe on first emission
                cancel();
            }
        });
        assertFalse(called.get());
    }

    @Test
    public void getIteratorThrows() {
        Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                throw new TestException("Forced failure");
            }
        };
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.fromIterable(it).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void hasNextThrowsImmediately() {
        Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        throw new TestException("Forced failure");
                    }

                    @Override
                    public Integer next() {
                        return null;
                    }

                    @Override
                    public void remove() {
                    // ignored
                    }
                };
            }
        };
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.fromIterable(it).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void hasNextThrowsSecondTimeFastpath() {
        Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    int count;

                    @Override
                    public boolean hasNext() {
                        if (++count >= 2) {
                            throw new TestException("Forced failure");
                        }
                        return true;
                    }

                    @Override
                    public Integer next() {
                        return 1;
                    }

                    @Override
                    public void remove() {
                    // ignored
                    }
                };
            }
        };
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.fromIterable(it).subscribe(ts);
        ts.assertValues(1);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void hasNextThrowsSecondTimeSlowpath() {
        Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    int count;

                    @Override
                    public boolean hasNext() {
                        if (++count >= 2) {
                            throw new TestException("Forced failure");
                        }
                        return true;
                    }

                    @Override
                    public Integer next() {
                        return 1;
                    }

                    @Override
                    public void remove() {
                    // ignored
                    }
                };
            }
        };
        TestSubscriber<Integer> ts = new TestSubscriber<>(5);
        Flowable.fromIterable(it).subscribe(ts);
        ts.assertValues(1);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void nextThrowsFastpath() {
        Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public Integer next() {
                        throw new TestException("Forced failure");
                    }

                    @Override
                    public void remove() {
                    // ignored
                    }
                };
            }
        };
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.fromIterable(it).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void nextThrowsSlowpath() {
        Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public Integer next() {
                        throw new TestException("Forced failure");
                    }

                    @Override
                    public void remove() {
                    // ignored
                    }
                };
            }
        };
        TestSubscriber<Integer> ts = new TestSubscriber<>(5);
        Flowable.fromIterable(it).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void deadOnArrival() {
        Iterable<Integer> it = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public Integer next() {
                        throw new NoSuchElementException();
                    }

                    @Override
                    public void remove() {
                    // ignored
                    }
                };
            }
        };
        TestSubscriber<Integer> ts = new TestSubscriber<>(5);
        ts.cancel();
        Flowable.fromIterable(it).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
    }

    @Test
    public void fusionWithConcatMap() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.fromIterable(Arrays.asList(1, 2, 3, 4)).concatMap(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) {
                return Flowable.range(v, 2);
            }
        }).subscribe(ts);
        ts.assertValues(1, 2, 2, 3, 3, 4, 4, 5);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void fusedAPICalls() {
        Flowable.fromIterable(Arrays.asList(1, 2, 3)).subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                @SuppressWarnings("unchecked")
                QueueSubscription<Integer> qs = (QueueSubscription<Integer>) s;
                assertFalse(qs.isEmpty());
                try {
                    assertEquals(1, qs.poll().intValue());
                } catch (Throwable ex) {
                    throw new AssertionError(ex);
                }
                assertFalse(qs.isEmpty());
                qs.clear();
                List<Throwable> errors = TestHelper.trackPluginErrors();
                try {
                    qs.request(-99);
                    TestHelper.assertError(errors, 0, IllegalArgumentException.class, "n > 0 required but it was -99");
                } finally {
                    RxJavaPlugins.reset();
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
    public void normalConditional() {
        Flowable.fromIterable(Arrays.asList(1, 2, 3, 4, 5)).filter(Functions.alwaysTrue()).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void normalConditionalBackpressured() {
        Flowable.fromIterable(Arrays.asList(1, 2, 3, 4, 5)).filter(Functions.alwaysTrue()).test(5L).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void normalConditionalBackpressured2() {
        Flowable.fromIterable(Arrays.asList(1, 2, 3, 4, 5)).filter(Functions.alwaysTrue()).to(TestHelper.<Integer>testSubscriber(4L)).assertSubscribed().assertValues(1, 2, 3, 4).assertNoErrors().assertNotComplete();
    }

    @Test
    public void emptyConditional() {
        Flowable.fromIterable(Arrays.asList(1, 2, 3, 4, 5)).filter(Functions.alwaysFalse()).test().assertResult();
    }

    @Test
    public void nullConditional() {
        Flowable.fromIterable(Arrays.asList(1, null, 3, 4, 5)).filter(Functions.alwaysTrue()).test().assertFailure(NullPointerException.class, 1);
    }

    @Test
    public void nullConditionalBackpressured() {
        Flowable.fromIterable(Arrays.asList(1, null, 3, 4, 5)).filter(Functions.alwaysTrue()).test(5L).assertFailure(NullPointerException.class, 1);
    }

    @Test
    public void normalConditionalCrash() {
        Flowable.fromIterable(new CrashingIterable(100, 2, 100)).filter(Functions.alwaysTrue()).test().assertFailure(TestException.class, 0);
    }

    @Test
    public void normalConditionalCrash2() {
        Flowable.fromIterable(new CrashingIterable(100, 100, 2)).filter(Functions.alwaysTrue()).test().assertFailure(TestException.class, 0);
    }

    @Test
    public void normalConditionalCrashBackpressured() {
        Flowable.fromIterable(new CrashingIterable(100, 2, 100)).filter(Functions.alwaysTrue()).test(5L).assertFailure(TestException.class, 0);
    }

    @Test
    public void normalConditionalCrashBackpressured2() {
        Flowable.fromIterable(new CrashingIterable(100, 100, 2)).filter(Functions.alwaysTrue()).test(5L).assertFailure(TestException.class, 0);
    }

    @Test
    public void normalConditionalLong() {
        Flowable.fromIterable(new CrashingIterable(100, 10 * 1000 * 1000, 10 * 1000 * 1000)).filter(Functions.alwaysTrue()).take(1000 * 1000).to(TestHelper.<Integer>testConsumer()).assertSubscribed().assertValueCount(1000 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void normalConditionalLong2() {
        Flowable.fromIterable(new CrashingIterable(100, 10 * 1000 * 1000, 10 * 1000 * 1000)).filter(Functions.alwaysTrue()).rebatchRequests(128).take(1000 * 1000).to(TestHelper.<Integer>testConsumer()).assertSubscribed().assertValueCount(1000 * 1000).assertNoErrors().assertComplete();
    }

    @Test
    public void requestRaceConditional() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    ts.request(1);
                }
            };
            Flowable.fromIterable(Arrays.asList(1, 2, 3, 4)).filter(Functions.alwaysTrue()).subscribe(ts);
            TestHelper.race(r, r);
        }
    }

    @Test
    public void requestRaceConditional2() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    ts.request(1);
                }
            };
            Flowable.fromIterable(Arrays.asList(1, 2, 3, 4)).filter(Functions.alwaysFalse()).subscribe(ts);
            TestHelper.race(r, r);
        }
    }

    @Test
    public void requestCancelConditionalRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts.request(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            Flowable.fromIterable(Arrays.asList(1, 2, 3, 4)).filter(Functions.alwaysTrue()).subscribe(ts);
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void requestCancelConditionalRace2() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts.request(Long.MAX_VALUE);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            Flowable.fromIterable(Arrays.asList(1, 2, 3, 4)).filter(Functions.alwaysTrue()).subscribe(ts);
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void requestCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts.request(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            Flowable.fromIterable(Arrays.asList(1, 2, 3, 4)).subscribe(ts);
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void requestCancelRace2() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts.request(Long.MAX_VALUE);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            Flowable.fromIterable(Arrays.asList(1, 2, 3, 4)).subscribe(ts);
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void fusionRejected() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ASYNC);
        Flowable.fromIterable(Arrays.asList(1, 2, 3)).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3);
    }

    @Test
    public void fusionClear() {
        Flowable.fromIterable(Arrays.asList(1, 2, 3)).subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                @SuppressWarnings("unchecked")
                QueueSubscription<Integer> qs = (QueueSubscription<Integer>) s;
                qs.requestFusion(QueueFuseable.ANY);
                try {
                    assertEquals(1, qs.poll().intValue());
                } catch (Throwable ex) {
                    fail(ex.toString());
                }
                qs.clear();
                try {
                    assertNull(qs.poll());
                } catch (Throwable ex) {
                    fail(ex.toString());
                }
            }

            @Override
            public void onNext(Integer value) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void iteratorThrows() {
        Flowable.fromIterable(new CrashingIterable(1, 100, 100)).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "iterator()");
    }

    @Test
    public void hasNext2Throws() {
        Flowable.fromIterable(new CrashingIterable(100, 2, 100)).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "hasNext()", 0);
    }

    @Test
    public void hasNextCancels() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.fromIterable(new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    int count;

                    @Override
                    public boolean hasNext() {
                        if (++count == 2) {
                            ts.cancel();
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
        }).subscribe(ts);
        ts.assertValue(1).assertNoErrors().assertNotComplete();
    }

    @Test
    public void hasNextCancelsAndCompletesFastPath() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.fromIterable(new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    int count;

                    @Override
                    public boolean hasNext() {
                        if (++count == 2) {
                            ts.cancel();
                            return false;
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
        }).subscribe(ts);
        ts.assertValue(1).assertNoErrors().assertNotComplete();
    }

    @Test
    public void hasNextCancelsAndCompletesSlowPath() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>(10L);
        Flowable.fromIterable(new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    int count;

                    @Override
                    public boolean hasNext() {
                        if (++count == 2) {
                            ts.cancel();
                            return false;
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
        }).subscribe(ts);
        ts.assertValue(1).assertNoErrors().assertNotComplete();
    }

    @Test
    public void hasNextCancelsAndCompletesFastPathConditional() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.fromIterable(new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    int count;

                    @Override
                    public boolean hasNext() {
                        if (++count == 2) {
                            ts.cancel();
                            return false;
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
        }).filter(v -> true).subscribe(ts);
        ts.assertValue(1).assertNoErrors().assertNotComplete();
    }

    @Test
    public void hasNextCancelsAndCompletesSlowPathConditional() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>(10);
        Flowable.fromIterable(new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    int count;

                    @Override
                    public boolean hasNext() {
                        if (++count == 2) {
                            ts.cancel();
                            return false;
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
        }).filter(v -> true).subscribe(ts);
        ts.assertValue(1).assertNoErrors().assertNotComplete();
    }

    @Test
    public void fusedPoll() throws Throwable {
        AtomicReference<SimpleQueue<?>> queue = new AtomicReference<>();
        Flowable.fromIterable(Arrays.asList(1)).subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onSubscribe(@NonNull Subscription s) {
                queue.set((SimpleQueue<?>) s);
                ((QueueSubscription<?>) s).requestFusion(QueueFuseable.ANY);
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
        SimpleQueue<?> q = queue.get();
        assertFalse(q.isEmpty());
        assertEquals(1, q.poll());
        assertTrue(q.isEmpty());
        q.clear();
        assertTrue(q.isEmpty());
    }

    @Test
    public void disposeWhileIteratorNext() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>(10);
        Flowable.fromIterable(new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public Integer next() {
                        ts.cancel();
                        return 1;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        }).subscribe(ts);
        ts.assertEmpty();
    }

    @Test
    public void disposeWhileIteratorNextConditional() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>(10);
        Flowable.fromIterable(new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public Integer next() {
                        ts.cancel();
                        return 1;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        }).filter(v -> true).subscribe(ts);
        ts.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableFromIterableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listIterable() throws java.lang.Throwable {
            this.payloads.listIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rawIterable() throws java.lang.Throwable {
            this.payloads.rawIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableFromIterable() throws java.lang.Throwable {
            this.payloads.observableFromIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureViaRequest() throws java.lang.Throwable {
            this.payloads.backpressureViaRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noBackpressure() throws java.lang.Throwable {
            this.payloads.noBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeMultipleTimes() throws java.lang.Throwable {
            this.payloads.subscribeMultipleTimes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromIterableRequestOverflow() throws java.lang.Throwable {
            this.payloads.fromIterableRequestOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromEmptyIterableWhenZeroRequestedShouldStillEmitOnCompletedEagerly() throws java.lang.Throwable {
            this.payloads.fromEmptyIterableWhenZeroRequestedShouldStillEmitOnCompletedEagerly.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure() throws java.lang.Throwable {
            this.payloads.doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesNotCallIteratorHasNextMoreThanRequiredFastPath() throws java.lang.Throwable {
            this.payloads.doesNotCallIteratorHasNextMoreThanRequiredFastPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getIteratorThrows() throws java.lang.Throwable {
            this.payloads.getIteratorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrowsImmediately() throws java.lang.Throwable {
            this.payloads.hasNextThrowsImmediately.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrowsSecondTimeFastpath() throws java.lang.Throwable {
            this.payloads.hasNextThrowsSecondTimeFastpath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrowsSecondTimeSlowpath() throws java.lang.Throwable {
            this.payloads.hasNextThrowsSecondTimeSlowpath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextThrowsFastpath() throws java.lang.Throwable {
            this.payloads.nextThrowsFastpath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextThrowsSlowpath() throws java.lang.Throwable {
            this.payloads.nextThrowsSlowpath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deadOnArrival() throws java.lang.Throwable {
            this.payloads.deadOnArrival.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionWithConcatMap() throws java.lang.Throwable {
            this.payloads.fusionWithConcatMap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedAPICalls() throws java.lang.Throwable {
            this.payloads.fusedAPICalls.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalConditional() throws java.lang.Throwable {
            this.payloads.normalConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalConditionalBackpressured() throws java.lang.Throwable {
            this.payloads.normalConditionalBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalConditionalBackpressured2() throws java.lang.Throwable {
            this.payloads.normalConditionalBackpressured2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyConditional() throws java.lang.Throwable {
            this.payloads.emptyConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullConditional() throws java.lang.Throwable {
            this.payloads.nullConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullConditionalBackpressured() throws java.lang.Throwable {
            this.payloads.nullConditionalBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalConditionalCrash() throws java.lang.Throwable {
            this.payloads.normalConditionalCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalConditionalCrash2() throws java.lang.Throwable {
            this.payloads.normalConditionalCrash2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalConditionalCrashBackpressured() throws java.lang.Throwable {
            this.payloads.normalConditionalCrashBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalConditionalCrashBackpressured2() throws java.lang.Throwable {
            this.payloads.normalConditionalCrashBackpressured2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalConditionalLong() throws java.lang.Throwable {
            this.payloads.normalConditionalLong.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalConditionalLong2() throws java.lang.Throwable {
            this.payloads.normalConditionalLong2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestRaceConditional() throws java.lang.Throwable {
            this.payloads.requestRaceConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestRaceConditional2() throws java.lang.Throwable {
            this.payloads.requestRaceConditional2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestCancelConditionalRace() throws java.lang.Throwable {
            this.payloads.requestCancelConditionalRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestCancelConditionalRace2() throws java.lang.Throwable {
            this.payloads.requestCancelConditionalRace2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestCancelRace() throws java.lang.Throwable {
            this.payloads.requestCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestCancelRace2() throws java.lang.Throwable {
            this.payloads.requestCancelRace2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejected() throws java.lang.Throwable {
            this.payloads.fusionRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionClear() throws java.lang.Throwable {
            this.payloads.fusionClear.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorThrows() throws java.lang.Throwable {
            this.payloads.iteratorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNext2Throws() throws java.lang.Throwable {
            this.payloads.hasNext2Throws.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCancels() throws java.lang.Throwable {
            this.payloads.hasNextCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCancelsAndCompletesFastPath() throws java.lang.Throwable {
            this.payloads.hasNextCancelsAndCompletesFastPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCancelsAndCompletesSlowPath() throws java.lang.Throwable {
            this.payloads.hasNextCancelsAndCompletesSlowPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCancelsAndCompletesFastPathConditional() throws java.lang.Throwable {
            this.payloads.hasNextCancelsAndCompletesFastPathConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCancelsAndCompletesSlowPathConditional() throws java.lang.Throwable {
            this.payloads.hasNextCancelsAndCompletesSlowPathConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPoll() throws java.lang.Throwable {
            this.payloads.fusedPoll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeWhileIteratorNext() throws java.lang.Throwable {
            this.payloads.disposeWhileIteratorNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeWhileIteratorNextConditional() throws java.lang.Throwable {
            this.payloads.disposeWhileIteratorNextConditional.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromIterableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromIterableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromIterableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromIterableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableFromIterableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromIterableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableFromIterableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableFromIterableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement listIterable;

            public org.junit.runners.model.Statement rawIterable;

            public org.junit.runners.model.Statement observableFromIterable;

            public org.junit.runners.model.Statement backpressureViaRequest;

            public org.junit.runners.model.Statement noBackpressure;

            public org.junit.runners.model.Statement subscribeMultipleTimes;

            public org.junit.runners.model.Statement fromIterableRequestOverflow;

            public org.junit.runners.model.Statement fromEmptyIterableWhenZeroRequestedShouldStillEmitOnCompletedEagerly;

            public org.junit.runners.model.Statement doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure;

            public org.junit.runners.model.Statement doesNotCallIteratorHasNextMoreThanRequiredFastPath;

            public org.junit.runners.model.Statement getIteratorThrows;

            public org.junit.runners.model.Statement hasNextThrowsImmediately;

            public org.junit.runners.model.Statement hasNextThrowsSecondTimeFastpath;

            public org.junit.runners.model.Statement hasNextThrowsSecondTimeSlowpath;

            public org.junit.runners.model.Statement nextThrowsFastpath;

            public org.junit.runners.model.Statement nextThrowsSlowpath;

            public org.junit.runners.model.Statement deadOnArrival;

            public org.junit.runners.model.Statement fusionWithConcatMap;

            public org.junit.runners.model.Statement fusedAPICalls;

            public org.junit.runners.model.Statement normalConditional;

            public org.junit.runners.model.Statement normalConditionalBackpressured;

            public org.junit.runners.model.Statement normalConditionalBackpressured2;

            public org.junit.runners.model.Statement emptyConditional;

            public org.junit.runners.model.Statement nullConditional;

            public org.junit.runners.model.Statement nullConditionalBackpressured;

            public org.junit.runners.model.Statement normalConditionalCrash;

            public org.junit.runners.model.Statement normalConditionalCrash2;

            public org.junit.runners.model.Statement normalConditionalCrashBackpressured;

            public org.junit.runners.model.Statement normalConditionalCrashBackpressured2;

            public org.junit.runners.model.Statement normalConditionalLong;

            public org.junit.runners.model.Statement normalConditionalLong2;

            public org.junit.runners.model.Statement requestRaceConditional;

            public org.junit.runners.model.Statement requestRaceConditional2;

            public org.junit.runners.model.Statement requestCancelConditionalRace;

            public org.junit.runners.model.Statement requestCancelConditionalRace2;

            public org.junit.runners.model.Statement requestCancelRace;

            public org.junit.runners.model.Statement requestCancelRace2;

            public org.junit.runners.model.Statement fusionRejected;

            public org.junit.runners.model.Statement fusionClear;

            public org.junit.runners.model.Statement iteratorThrows;

            public org.junit.runners.model.Statement hasNext2Throws;

            public org.junit.runners.model.Statement hasNextCancels;

            public org.junit.runners.model.Statement hasNextCancelsAndCompletesFastPath;

            public org.junit.runners.model.Statement hasNextCancelsAndCompletesSlowPath;

            public org.junit.runners.model.Statement hasNextCancelsAndCompletesFastPathConditional;

            public org.junit.runners.model.Statement hasNextCancelsAndCompletesSlowPathConditional;

            public org.junit.runners.model.Statement fusedPoll;

            public org.junit.runners.model.Statement disposeWhileIteratorNext;

            public org.junit.runners.model.Statement disposeWhileIteratorNextConditional;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.listIterable = _ClassStatement.forPayload(FlowableFromIterableTest::listIterable, "listIterable", this);
            this.payloads.rawIterable = _ClassStatement.forPayload(FlowableFromIterableTest::rawIterable, "rawIterable", this);
            this.payloads.observableFromIterable = _ClassStatement.forPayload(FlowableFromIterableTest::observableFromIterable, "observableFromIterable", this);
            this.payloads.backpressureViaRequest = _ClassStatement.forPayload(FlowableFromIterableTest::backpressureViaRequest, "backpressureViaRequest", this);
            this.payloads.noBackpressure = _ClassStatement.forPayload(FlowableFromIterableTest::noBackpressure, "noBackpressure", this);
            this.payloads.subscribeMultipleTimes = _ClassStatement.forPayload(FlowableFromIterableTest::subscribeMultipleTimes, "subscribeMultipleTimes", this);
            this.payloads.fromIterableRequestOverflow = _ClassStatement.forPayload(FlowableFromIterableTest::fromIterableRequestOverflow, "fromIterableRequestOverflow", this);
            this.payloads.fromEmptyIterableWhenZeroRequestedShouldStillEmitOnCompletedEagerly = _ClassStatement.forPayload(FlowableFromIterableTest::fromEmptyIterableWhenZeroRequestedShouldStillEmitOnCompletedEagerly, "fromEmptyIterableWhenZeroRequestedShouldStillEmitOnCompletedEagerly", this);
            this.payloads.doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure = _ClassStatement.forPayload(FlowableFromIterableTest::doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure, "doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure", this);
            this.payloads.doesNotCallIteratorHasNextMoreThanRequiredFastPath = _ClassStatement.forPayload(FlowableFromIterableTest::doesNotCallIteratorHasNextMoreThanRequiredFastPath, "doesNotCallIteratorHasNextMoreThanRequiredFastPath", this);
            this.payloads.getIteratorThrows = _ClassStatement.forPayload(FlowableFromIterableTest::getIteratorThrows, "getIteratorThrows", this);
            this.payloads.hasNextThrowsImmediately = _ClassStatement.forPayload(FlowableFromIterableTest::hasNextThrowsImmediately, "hasNextThrowsImmediately", this);
            this.payloads.hasNextThrowsSecondTimeFastpath = _ClassStatement.forPayload(FlowableFromIterableTest::hasNextThrowsSecondTimeFastpath, "hasNextThrowsSecondTimeFastpath", this);
            this.payloads.hasNextThrowsSecondTimeSlowpath = _ClassStatement.forPayload(FlowableFromIterableTest::hasNextThrowsSecondTimeSlowpath, "hasNextThrowsSecondTimeSlowpath", this);
            this.payloads.nextThrowsFastpath = _ClassStatement.forPayload(FlowableFromIterableTest::nextThrowsFastpath, "nextThrowsFastpath", this);
            this.payloads.nextThrowsSlowpath = _ClassStatement.forPayload(FlowableFromIterableTest::nextThrowsSlowpath, "nextThrowsSlowpath", this);
            this.payloads.deadOnArrival = _ClassStatement.forPayload(FlowableFromIterableTest::deadOnArrival, "deadOnArrival", this);
            this.payloads.fusionWithConcatMap = _ClassStatement.forPayload(FlowableFromIterableTest::fusionWithConcatMap, "fusionWithConcatMap", this);
            this.payloads.fusedAPICalls = _ClassStatement.forPayload(FlowableFromIterableTest::fusedAPICalls, "fusedAPICalls", this);
            this.payloads.normalConditional = _ClassStatement.forPayload(FlowableFromIterableTest::normalConditional, "normalConditional", this);
            this.payloads.normalConditionalBackpressured = _ClassStatement.forPayload(FlowableFromIterableTest::normalConditionalBackpressured, "normalConditionalBackpressured", this);
            this.payloads.normalConditionalBackpressured2 = _ClassStatement.forPayload(FlowableFromIterableTest::normalConditionalBackpressured2, "normalConditionalBackpressured2", this);
            this.payloads.emptyConditional = _ClassStatement.forPayload(FlowableFromIterableTest::emptyConditional, "emptyConditional", this);
            this.payloads.nullConditional = _ClassStatement.forPayload(FlowableFromIterableTest::nullConditional, "nullConditional", this);
            this.payloads.nullConditionalBackpressured = _ClassStatement.forPayload(FlowableFromIterableTest::nullConditionalBackpressured, "nullConditionalBackpressured", this);
            this.payloads.normalConditionalCrash = _ClassStatement.forPayload(FlowableFromIterableTest::normalConditionalCrash, "normalConditionalCrash", this);
            this.payloads.normalConditionalCrash2 = _ClassStatement.forPayload(FlowableFromIterableTest::normalConditionalCrash2, "normalConditionalCrash2", this);
            this.payloads.normalConditionalCrashBackpressured = _ClassStatement.forPayload(FlowableFromIterableTest::normalConditionalCrashBackpressured, "normalConditionalCrashBackpressured", this);
            this.payloads.normalConditionalCrashBackpressured2 = _ClassStatement.forPayload(FlowableFromIterableTest::normalConditionalCrashBackpressured2, "normalConditionalCrashBackpressured2", this);
            this.payloads.normalConditionalLong = _ClassStatement.forPayload(FlowableFromIterableTest::normalConditionalLong, "normalConditionalLong", this);
            this.payloads.normalConditionalLong2 = _ClassStatement.forPayload(FlowableFromIterableTest::normalConditionalLong2, "normalConditionalLong2", this);
            this.payloads.requestRaceConditional = _ClassStatement.forPayload(FlowableFromIterableTest::requestRaceConditional, "requestRaceConditional", this);
            this.payloads.requestRaceConditional2 = _ClassStatement.forPayload(FlowableFromIterableTest::requestRaceConditional2, "requestRaceConditional2", this);
            this.payloads.requestCancelConditionalRace = _ClassStatement.forPayload(FlowableFromIterableTest::requestCancelConditionalRace, "requestCancelConditionalRace", this);
            this.payloads.requestCancelConditionalRace2 = _ClassStatement.forPayload(FlowableFromIterableTest::requestCancelConditionalRace2, "requestCancelConditionalRace2", this);
            this.payloads.requestCancelRace = _ClassStatement.forPayload(FlowableFromIterableTest::requestCancelRace, "requestCancelRace", this);
            this.payloads.requestCancelRace2 = _ClassStatement.forPayload(FlowableFromIterableTest::requestCancelRace2, "requestCancelRace2", this);
            this.payloads.fusionRejected = _ClassStatement.forPayload(FlowableFromIterableTest::fusionRejected, "fusionRejected", this);
            this.payloads.fusionClear = _ClassStatement.forPayload(FlowableFromIterableTest::fusionClear, "fusionClear", this);
            this.payloads.iteratorThrows = _ClassStatement.forPayload(FlowableFromIterableTest::iteratorThrows, "iteratorThrows", this);
            this.payloads.hasNext2Throws = _ClassStatement.forPayload(FlowableFromIterableTest::hasNext2Throws, "hasNext2Throws", this);
            this.payloads.hasNextCancels = _ClassStatement.forPayload(FlowableFromIterableTest::hasNextCancels, "hasNextCancels", this);
            this.payloads.hasNextCancelsAndCompletesFastPath = _ClassStatement.forPayload(FlowableFromIterableTest::hasNextCancelsAndCompletesFastPath, "hasNextCancelsAndCompletesFastPath", this);
            this.payloads.hasNextCancelsAndCompletesSlowPath = _ClassStatement.forPayload(FlowableFromIterableTest::hasNextCancelsAndCompletesSlowPath, "hasNextCancelsAndCompletesSlowPath", this);
            this.payloads.hasNextCancelsAndCompletesFastPathConditional = _ClassStatement.forPayload(FlowableFromIterableTest::hasNextCancelsAndCompletesFastPathConditional, "hasNextCancelsAndCompletesFastPathConditional", this);
            this.payloads.hasNextCancelsAndCompletesSlowPathConditional = _ClassStatement.forPayload(FlowableFromIterableTest::hasNextCancelsAndCompletesSlowPathConditional, "hasNextCancelsAndCompletesSlowPathConditional", this);
            this.payloads.fusedPoll = _ClassStatement.forPayload(FlowableFromIterableTest::fusedPoll, "fusedPoll", this);
            this.payloads.disposeWhileIteratorNext = _ClassStatement.forPayload(FlowableFromIterableTest::disposeWhileIteratorNext, "disposeWhileIteratorNext", this);
            this.payloads.disposeWhileIteratorNextConditional = _ClassStatement.forPayload(FlowableFromIterableTest::disposeWhileIteratorNextConditional, "disposeWhileIteratorNextConditional", this);
        }
    }
}
