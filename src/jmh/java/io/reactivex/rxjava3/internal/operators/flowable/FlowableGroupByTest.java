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
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.reactivestreams.*;
import com.google.common.base.Ticker;
import com.google.common.cache.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.flowables.GroupedFlowable;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.schedulers.ImmediateThinScheduler;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableGroupByTest extends RxJavaTest {

    static Function<GroupedFlowable<Integer, Integer>, Flowable<Integer>> FLATTEN_INTEGER = new Function<GroupedFlowable<Integer, Integer>, Flowable<Integer>>() {

        @Override
        public Flowable<Integer> apply(GroupedFlowable<Integer, Integer> t) {
            return t;
        }
    };

    final Function<String, Integer> length = new Function<String, Integer>() {

        @Override
        public Integer apply(String s) {
            return s.length();
        }
    };

    @Test
    public void groupBy() {
        Flowable<String> source = Flowable.just("one", "two", "three", "four", "five", "six");
        Flowable<GroupedFlowable<Integer, String>> grouped = source.groupBy(length);
        Map<Integer, Collection<String>> map = toMap(grouped);
        assertEquals(3, map.size());
        assertArrayEquals(Arrays.asList("one", "two", "six").toArray(), map.get(3).toArray());
        assertArrayEquals(Arrays.asList("four", "five").toArray(), map.get(4).toArray());
        assertArrayEquals(Arrays.asList("three").toArray(), map.get(5).toArray());
    }

    @Test
    public void groupByWithElementSelector() {
        Flowable<String> source = Flowable.just("one", "two", "three", "four", "five", "six");
        Flowable<GroupedFlowable<Integer, Integer>> grouped = source.groupBy(length, length);
        Map<Integer, Collection<Integer>> map = toMap(grouped);
        assertEquals(3, map.size());
        assertArrayEquals(Arrays.asList(3, 3, 3).toArray(), map.get(3).toArray());
        assertArrayEquals(Arrays.asList(4, 4).toArray(), map.get(4).toArray());
        assertArrayEquals(Arrays.asList(5).toArray(), map.get(5).toArray());
    }

    @Test
    public void groupByWithElementSelector2() {
        Flowable<String> source = Flowable.just("one", "two", "three", "four", "five", "six");
        Flowable<GroupedFlowable<Integer, Integer>> grouped = source.groupBy(length, length);
        Map<Integer, Collection<Integer>> map = toMap(grouped);
        assertEquals(3, map.size());
        assertArrayEquals(Arrays.asList(3, 3, 3).toArray(), map.get(3).toArray());
        assertArrayEquals(Arrays.asList(4, 4).toArray(), map.get(4).toArray());
        assertArrayEquals(Arrays.asList(5).toArray(), map.get(5).toArray());
    }

    @Test
    public void empty() {
        Flowable<String> source = Flowable.empty();
        Flowable<GroupedFlowable<Integer, String>> grouped = source.groupBy(length);
        Map<Integer, Collection<String>> map = toMap(grouped);
        assertTrue(map.isEmpty());
    }

    @Test
    @SuppressUndeliverable
    public void error() {
        Flowable<String> sourceStrings = Flowable.just("one", "two", "three", "four", "five", "six");
        Flowable<String> errorSource = Flowable.error(new TestException("forced failure"));
        Flowable<String> source = Flowable.concat(sourceStrings, errorSource);
        Flowable<GroupedFlowable<Integer, String>> grouped = source.groupBy(length);
        final AtomicInteger groupCounter = new AtomicInteger();
        final AtomicInteger eventCounter = new AtomicInteger();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        grouped.flatMap(new Function<GroupedFlowable<Integer, String>, Flowable<String>>() {

            @Override
            public Flowable<String> apply(final GroupedFlowable<Integer, String> f) {
                groupCounter.incrementAndGet();
                return f.map(new Function<String, String>() {

                    @Override
                    public String apply(String v) {
                        return "Event => key: " + f.getKey() + " value: " + v;
                    }
                });
            }
        }).subscribe(new DefaultSubscriber<String>() {

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
                // e.printStackTrace();
                error.set(e);
            }

            @Override
            public void onNext(String v) {
                eventCounter.incrementAndGet();
                // System.out.println(v);
            }
        });
        assertEquals(3, groupCounter.get());
        assertEquals(6, eventCounter.get());
        assertNotNull(error.get());
        assertTrue("" + error.get(), error.get() instanceof TestException);
        assertEquals(error.get().getMessage(), "forced failure");
    }

    private static <K, V> Map<K, Collection<V>> toMap(Flowable<GroupedFlowable<K, V>> flowable) {
        final ConcurrentHashMap<K, Collection<V>> result = new ConcurrentHashMap<>();
        flowable.doOnNext(new Consumer<GroupedFlowable<K, V>>() {

            @Override
            public void accept(final GroupedFlowable<K, V> f) {
                result.put(f.getKey(), new ConcurrentLinkedQueue<>());
                f.subscribe(new Consumer<V>() {

                    @Override
                    public void accept(V v) {
                        result.get(f.getKey()).add(v);
                    }
                });
            }
        }).blockingSubscribe();
        return result;
    }

    /**
     * Assert that only a single subscription to a stream occurs and that all events are received.
     *
     * @throws Throwable some method call is declared throws
     */
    @Test
    public void groupedEventStream() throws Throwable {
        final AtomicInteger eventCounter = new AtomicInteger();
        final AtomicInteger subscribeCounter = new AtomicInteger();
        final AtomicInteger groupCounter = new AtomicInteger();
        final CountDownLatch latch = new CountDownLatch(1);
        final int count = 100;
        final int groupCount = 2;
        Flowable<Event> es = Flowable.unsafeCreate(new Publisher<Event>() {

            @Override
            public void subscribe(final Subscriber<? super Event> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                // System.out.println("*** Subscribing to EventStream ***");
                subscribeCounter.incrementAndGet();
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < count; i++) {
                            Event e = new Event();
                            e.source = i % groupCount;
                            e.message = "Event-" + i;
                            subscriber.onNext(e);
                        }
                        subscriber.onComplete();
                    }
                }).start();
            }
        });
        es.groupBy(new Function<Event, Integer>() {

            @Override
            public Integer apply(Event e) {
                return e.source;
            }
        }).flatMap(new Function<GroupedFlowable<Integer, Event>, Flowable<String>>() {

            @Override
            public Flowable<String> apply(GroupedFlowable<Integer, Event> eventGroupedFlowable) {
                // System.out.println("GroupedFlowable Key: " + eventGroupedFlowable.getKey());
                groupCounter.incrementAndGet();
                return eventGroupedFlowable.map(new Function<Event, String>() {

                    @Override
                    public String apply(Event event) {
                        return "Source: " + event.source + "  Message: " + event.message;
                    }
                });
            }
        }).subscribe(new DefaultSubscriber<String>() {

            @Override
            public void onComplete() {
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onNext(String outputMessage) {
                // System.out.println(outputMessage);
                eventCounter.incrementAndGet();
            }
        });
        latch.await(5000, TimeUnit.MILLISECONDS);
        assertEquals(1, subscribeCounter.get());
        assertEquals(groupCount, groupCounter.get());
        assertEquals(count, eventCounter.get());
    }

    /*
     * We will only take 1 group with 20 events from it and then unsubscribe.
     */
    @Test
    public void unsubscribeOnNestedTakeAndSyncInfiniteStream() throws InterruptedException {
        final AtomicInteger subscribeCounter = new AtomicInteger();
        final AtomicInteger sentEventCounter = new AtomicInteger();
        doTestUnsubscribeOnNestedTakeAndAsyncInfiniteStream(SYNC_INFINITE_OBSERVABLE_OF_EVENT(2, subscribeCounter, sentEventCounter), subscribeCounter);
        Thread.sleep(500);
        assertEquals(39, sentEventCounter.get());
    }

    /*
     * We will only take 1 group with 20 events from it and then unsubscribe.
     */
    @Test
    public void unsubscribeOnNestedTakeAndAsyncInfiniteStream() throws InterruptedException {
        final AtomicInteger subscribeCounter = new AtomicInteger();
        final AtomicInteger sentEventCounter = new AtomicInteger();
        doTestUnsubscribeOnNestedTakeAndAsyncInfiniteStream(ASYNC_INFINITE_OBSERVABLE_OF_EVENT(2, subscribeCounter, sentEventCounter), subscribeCounter);
        Thread.sleep(500);
        assertEquals(39, sentEventCounter.get());
    }

    private void doTestUnsubscribeOnNestedTakeAndAsyncInfiniteStream(Flowable<Event> es, AtomicInteger subscribeCounter) throws InterruptedException {
        final AtomicInteger eventCounter = new AtomicInteger();
        final AtomicInteger groupCounter = new AtomicInteger();
        final CountDownLatch latch = new CountDownLatch(1);
        es.groupBy(new Function<Event, Integer>() {

            @Override
            public Integer apply(Event e) {
                return e.source;
            }
        }).take(// we want only the first group
        1).flatMap(new Function<GroupedFlowable<Integer, Event>, Flowable<String>>() {

            @Override
            public Flowable<String> apply(GroupedFlowable<Integer, Event> eventGroupedFlowable) {
                // System.out.println("testUnsubscribe => GroupedFlowable Key: " + eventGroupedFlowable.getKey());
                groupCounter.incrementAndGet();
                return eventGroupedFlowable.take(// limit to only 20 events on this group
                20).map(new Function<Event, String>() {

                    @Override
                    public String apply(Event event) {
                        return "testUnsubscribe => Source: " + event.source + "  Message: " + event.message;
                    }
                });
            }
        }).subscribe(new DefaultSubscriber<String>() {

            @Override
            public void onComplete() {
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onNext(String outputMessage) {
                // System.out.println(outputMessage);
                eventCounter.incrementAndGet();
            }
        });
        if (!latch.await(2000, TimeUnit.MILLISECONDS)) {
            fail("timed out so likely did not unsubscribe correctly");
        }
        assertEquals(1, subscribeCounter.get());
        assertEquals(1, groupCounter.get());
        assertEquals(20, eventCounter.get());
    // sentEvents will go until 'eventCounter' hits 20 and then unsubscribes
    // which means it will also send (but ignore) the 19/20 events for the other group
    // It will not however send all 100 events.
    }

    @Test
    public void unsubscribeViaTakeOnGroupThenMergeAndTake() {
        final AtomicInteger subscribeCounter = new AtomicInteger();
        final AtomicInteger sentEventCounter = new AtomicInteger();
        final AtomicInteger eventCounter = new AtomicInteger();
        SYNC_INFINITE_OBSERVABLE_OF_EVENT(4, subscribeCounter, sentEventCounter).groupBy(new Function<Event, Integer>() {

            @Override
            public Integer apply(Event e) {
                return e.source;
            }
        }).take(2).flatMap(new Function<GroupedFlowable<Integer, Event>, Flowable<String>>() {

            @Override
            public Flowable<String> apply(GroupedFlowable<Integer, Event> eventGroupedFlowable) {
                return eventGroupedFlowable.map(new Function<Event, String>() {

                    @Override
                    public String apply(Event event) {
                        return "testUnsubscribe => Source: " + event.source + "  Message: " + event.message;
                    }
                });
            }
        }).take(30).subscribe(new Consumer<String>() {

            @Override
            public void accept(String s) {
                eventCounter.incrementAndGet();
                // System.out.println("=> " + s);
            }
        });
        assertEquals(30, eventCounter.get());
        // we should send 28 additional events that are filtered out as they are in the groups we skip
        assertEquals(58, sentEventCounter.get());
    }

    @Test
    public void unsubscribeViaTakeOnGroupThenTakeOnInner() {
        final AtomicInteger subscribeCounter = new AtomicInteger();
        final AtomicInteger sentEventCounter = new AtomicInteger();
        final AtomicInteger eventCounter = new AtomicInteger();
        SYNC_INFINITE_OBSERVABLE_OF_EVENT(4, subscribeCounter, sentEventCounter).groupBy(new Function<Event, Integer>() {

            @Override
            public Integer apply(Event e) {
                return e.source;
            }
        }).take(2).flatMap(new Function<GroupedFlowable<Integer, Event>, Flowable<String>>() {

            @Override
            public Flowable<String> apply(GroupedFlowable<Integer, Event> eventGroupedFlowable) {
                int numToTake = 0;
                if (eventGroupedFlowable.getKey() == 1) {
                    numToTake = 10;
                } else if (eventGroupedFlowable.getKey() == 2) {
                    numToTake = 5;
                }
                return eventGroupedFlowable.take(numToTake).map(new Function<Event, String>() {

                    @Override
                    public String apply(Event event) {
                        return "testUnsubscribe => Source: " + event.source + "  Message: " + event.message;
                    }
                });
            }
        }).subscribe(new Consumer<String>() {

            @Override
            public void accept(String s) {
                eventCounter.incrementAndGet();
                // System.out.println("=> " + s);
            }
        });
        assertEquals(15, eventCounter.get());
        // we should send 22 additional events that are filtered out as they are skipped while taking the 15 we want
        assertEquals(37, sentEventCounter.get());
    }

    @Test
    public void staggeredCompletion() throws InterruptedException {
        final AtomicInteger eventCounter = new AtomicInteger();
        final CountDownLatch latch = new CountDownLatch(1);
        Flowable.range(0, 100).groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer i) {
                return i % 2;
            }
        }).flatMap(new Function<GroupedFlowable<Integer, Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(GroupedFlowable<Integer, Integer> group) {
                if (group.getKey() == 0) {
                    return group.delay(100, TimeUnit.MILLISECONDS).map(new Function<Integer, Integer>() {

                        @Override
                        public Integer apply(Integer t) {
                            return t * 10;
                        }
                    });
                } else {
                    return group;
                }
            }
        }).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onComplete() {
                // System.out.println("=> onComplete");
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onNext(Integer s) {
                eventCounter.incrementAndGet();
                // System.out.println("=> " + s);
            }
        });
        if (!latch.await(3000, TimeUnit.MILLISECONDS)) {
            fail("timed out");
        }
        assertEquals(100, eventCounter.get());
    }

    @Test
    public void completionIfInnerNotSubscribed() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger eventCounter = new AtomicInteger();
        Flowable.range(0, 100).groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer i) {
                return i % 2;
            }
        }).subscribe(new DefaultSubscriber<GroupedFlowable<Integer, Integer>>() {

            @Override
            public void onComplete() {
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onNext(GroupedFlowable<Integer, Integer> s) {
                eventCounter.incrementAndGet();
                // System.out.println("=> " + s);
            }
        });
        if (!latch.await(500, TimeUnit.MILLISECONDS)) {
            fail("timed out - never got completion");
        }
        // Behavior change: groups not subscribed immediately will be automatically abandoned
        // so this leads to group recreation
        assertEquals(100, eventCounter.get());
    }

    @Test
    public void ignoringGroups() {
        final AtomicInteger subscribeCounter = new AtomicInteger();
        final AtomicInteger sentEventCounter = new AtomicInteger();
        final AtomicInteger eventCounter = new AtomicInteger();
        SYNC_INFINITE_OBSERVABLE_OF_EVENT(4, subscribeCounter, sentEventCounter).groupBy(new Function<Event, Integer>() {

            @Override
            public Integer apply(Event e) {
                return e.source;
            }
        }).flatMap(new Function<GroupedFlowable<Integer, Event>, Flowable<String>>() {

            @Override
            public Flowable<String> apply(GroupedFlowable<Integer, Event> eventGroupedFlowable) {
                Flowable<Event> eventStream = eventGroupedFlowable;
                if (eventGroupedFlowable.getKey() >= 2) {
                    // filter these
                    eventStream = eventGroupedFlowable.filter(new Predicate<Event>() {

                        @Override
                        public boolean test(Event t1) {
                            return false;
                        }
                    });
                }
                return eventStream.map(new Function<Event, String>() {

                    @Override
                    public String apply(Event event) {
                        return "testUnsubscribe => Source: " + event.source + "  Message: " + event.message;
                    }
                });
            }
        }).take(30).subscribe(new Consumer<String>() {

            @Override
            public void accept(String s) {
                eventCounter.incrementAndGet();
                // System.out.println("=> " + s);
            }
        });
        assertEquals(30, eventCounter.get());
        // we should send 30 additional events that are filtered out as they are in the groups we skip
        assertEquals(60, sentEventCounter.get());
    }

    @Test
    public void firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsAndThenComplete() throws InterruptedException {
        // there are two groups to first complete
        final CountDownLatch first = new CountDownLatch(2);
        final ArrayList<String> results = new ArrayList<>();
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new BooleanSubscription());
                sub.onNext(1);
                sub.onNext(2);
                sub.onNext(1);
                sub.onNext(2);
                try {
                    first.await();
                } catch (InterruptedException e) {
                    sub.onError(e);
                    return;
                }
                sub.onNext(3);
                sub.onNext(3);
                sub.onComplete();
            }
        }).groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t) {
                return t;
            }
        }).flatMap(new Function<GroupedFlowable<Integer, Integer>, Flowable<String>>() {

            @Override
            public Flowable<String> apply(final GroupedFlowable<Integer, Integer> group) {
                if (group.getKey() < 3) {
                    return group.map(new Function<Integer, String>() {

                        @Override
                        public String apply(Integer t1) {
                            return "first groups: " + t1;
                        }
                    }).take(2).doOnComplete(new Action() {

                        @Override
                        public void run() {
                            first.countDown();
                        }
                    });
                } else {
                    return group.map(new Function<Integer, String>() {

                        @Override
                        public String apply(Integer t1) {
                            return "last group: " + t1;
                        }
                    });
                }
            }
        }).blockingForEach(new Consumer<String>() {

            @Override
            public void accept(String s) {
                results.add(s);
            }
        });
        // System.out.println("Results: " + results);
        assertEquals(6, results.size());
    }

    @Test
    public void firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsWhichThenSubscribesOnAndDelaysAndThenCompletes() throws InterruptedException {
        System.err.println("----------------------------------------------------------------------------------------------");
        // there are two groups to first complete
        final CountDownLatch first = new CountDownLatch(2);
        final ArrayList<String> results = new ArrayList<>();
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new BooleanSubscription());
                sub.onNext(1);
                sub.onNext(2);
                sub.onNext(1);
                sub.onNext(2);
                try {
                    first.await();
                } catch (InterruptedException e) {
                    sub.onError(e);
                    return;
                }
                sub.onNext(3);
                sub.onNext(3);
                sub.onComplete();
            }
        }).groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t) {
                return t;
            }
        }).flatMap(new Function<GroupedFlowable<Integer, Integer>, Flowable<String>>() {

            @Override
            public Flowable<String> apply(final GroupedFlowable<Integer, Integer> group) {
                if (group.getKey() < 3) {
                    return group.map(new Function<Integer, String>() {

                        @Override
                        public String apply(Integer t1) {
                            return "first groups: " + t1;
                        }
                    }).take(2).doOnComplete(new Action() {

                        @Override
                        public void run() {
                            first.countDown();
                        }
                    });
                } else {
                    return group.subscribeOn(Schedulers.newThread()).delay(400, TimeUnit.MILLISECONDS).map(new Function<Integer, String>() {

                        @Override
                        public String apply(Integer t1) {
                            return "last group: " + t1;
                        }
                    }).doOnEach(new Consumer<Notification<String>>() {

                        @Override
                        public void accept(Notification<String> t1) {
                            System.err.println("subscribeOn notification => " + t1);
                        }
                    });
                }
            }
        }).doOnEach(new Consumer<Notification<String>>() {

            @Override
            public void accept(Notification<String> t1) {
                System.err.println("outer notification => " + t1);
            }
        }).blockingForEach(new Consumer<String>() {

            @Override
            public void accept(String s) {
                results.add(s);
            }
        });
        // System.out.println("Results: " + results);
        assertEquals(6, results.size());
    }

    @Test
    public void firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsWhichThenObservesOnAndDelaysAndThenCompletes() throws InterruptedException {
        // there are two groups to first complete
        final CountDownLatch first = new CountDownLatch(2);
        final ArrayList<String> results = new ArrayList<>();
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new BooleanSubscription());
                sub.onNext(1);
                sub.onNext(2);
                sub.onNext(1);
                sub.onNext(2);
                try {
                    first.await();
                } catch (InterruptedException e) {
                    sub.onError(e);
                    return;
                }
                sub.onNext(3);
                sub.onNext(3);
                sub.onComplete();
            }
        }).groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t) {
                return t;
            }
        }).flatMap(new Function<GroupedFlowable<Integer, Integer>, Flowable<String>>() {

            @Override
            public Flowable<String> apply(final GroupedFlowable<Integer, Integer> group) {
                if (group.getKey() < 3) {
                    return group.map(new Function<Integer, String>() {

                        @Override
                        public String apply(Integer t1) {
                            return "first groups: " + t1;
                        }
                    }).take(2).doOnComplete(new Action() {

                        @Override
                        public void run() {
                            first.countDown();
                        }
                    });
                } else {
                    return group.observeOn(Schedulers.newThread()).delay(400, TimeUnit.MILLISECONDS).map(new Function<Integer, String>() {

                        @Override
                        public String apply(Integer t1) {
                            return "last group: " + t1;
                        }
                    });
                }
            }
        }).blockingForEach(new Consumer<String>() {

            @Override
            public void accept(String s) {
                results.add(s);
            }
        });
        // System.out.println("Results: " + results);
        assertEquals(6, results.size());
    }

    @Test
    public void groupsWithNestedSubscribeOn() throws InterruptedException {
        final ArrayList<String> results = new ArrayList<>();
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new BooleanSubscription());
                sub.onNext(1);
                sub.onNext(2);
                sub.onNext(1);
                sub.onNext(2);
                sub.onComplete();
            }
        }).groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t) {
                return t;
            }
        }).flatMap(new Function<GroupedFlowable<Integer, Integer>, Flowable<String>>() {

            @Override
            public Flowable<String> apply(final GroupedFlowable<Integer, Integer> group) {
                return group.subscribeOn(Schedulers.newThread()).map(new Function<Integer, String>() {

                    @Override
                    public String apply(Integer t1) {
                        // System.out.println("Received: " + t1 + " on group : " + group.getKey());
                        return "first groups: " + t1;
                    }
                });
            }
        }).doOnEach(new Consumer<Notification<String>>() {

            @Override
            public void accept(Notification<String> t1) {
                // System.out.println("notification => " + t1);
            }
        }).blockingForEach(new Consumer<String>() {

            @Override
            public void accept(String s) {
                results.add(s);
            }
        });
        // System.out.println("Results: " + results);
        assertEquals(4, results.size());
    }

    @Test
    public void groupsWithNestedObserveOn() throws InterruptedException {
        final ArrayList<String> results = new ArrayList<>();
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new BooleanSubscription());
                sub.onNext(1);
                sub.onNext(2);
                sub.onNext(1);
                sub.onNext(2);
                sub.onComplete();
            }
        }).groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t) {
                return t;
            }
        }).flatMap(new Function<GroupedFlowable<Integer, Integer>, Flowable<String>>() {

            @Override
            public Flowable<String> apply(final GroupedFlowable<Integer, Integer> group) {
                return group.observeOn(Schedulers.newThread()).delay(400, TimeUnit.MILLISECONDS).map(new Function<Integer, String>() {

                    @Override
                    public String apply(Integer t1) {
                        return "first groups: " + t1;
                    }
                });
            }
        }).blockingForEach(new Consumer<String>() {

            @Override
            public void accept(String s) {
                results.add(s);
            }
        });
        // System.out.println("Results: " + results);
        assertEquals(4, results.size());
    }

    private static class Event {

        int source;

        String message;

        @Override
        public String toString() {
            return "Event => source: " + source + " message: " + message;
        }
    }

    Flowable<Event> ASYNC_INFINITE_OBSERVABLE_OF_EVENT(final int numGroups, final AtomicInteger subscribeCounter, final AtomicInteger sentEventCounter) {
        return SYNC_INFINITE_OBSERVABLE_OF_EVENT(numGroups, subscribeCounter, sentEventCounter).subscribeOn(Schedulers.newThread());
    }

    Flowable<Event> SYNC_INFINITE_OBSERVABLE_OF_EVENT(final int numGroups, final AtomicInteger subscribeCounter, final AtomicInteger sentEventCounter) {
        return Flowable.unsafeCreate(new Publisher<Event>() {

            @Override
            public void subscribe(final Subscriber<? super Event> op) {
                BooleanSubscription bs = new BooleanSubscription();
                op.onSubscribe(bs);
                subscribeCounter.incrementAndGet();
                int i = 0;
                while (!bs.isCancelled()) {
                    i++;
                    Event e = new Event();
                    e.source = i % numGroups;
                    e.message = "Event-" + i;
                    op.onNext(e);
                    sentEventCounter.incrementAndGet();
                }
                op.onComplete();
            }
        });
    }

    @Test
    public void groupByOnAsynchronousSourceAcceptsMultipleSubscriptions() throws InterruptedException {
        // choose an asynchronous source
        Flowable<Long> source = Flowable.interval(10, TimeUnit.MILLISECONDS).take(1);
        // apply groupBy to the source
        Flowable<GroupedFlowable<Boolean, Long>> stream = source.groupBy(IS_EVEN);
        // create two observers
        Subscriber<GroupedFlowable<Boolean, Long>> f1 = TestHelper.mockSubscriber();
        Subscriber<GroupedFlowable<Boolean, Long>> f2 = TestHelper.mockSubscriber();
        // subscribe with the observers
        stream.subscribe(f1);
        stream.subscribe(f2);
        // check that subscriptions were successful
        verify(f1, never()).onError(Mockito.<Throwable>any());
        verify(f2, never()).onError(Mockito.<Throwable>any());
    }

    private static Function<Long, Boolean> IS_EVEN = new Function<Long, Boolean>() {

        @Override
        public Boolean apply(Long n) {
            return n % 2 == 0;
        }
    };

    private static Function<Integer, Boolean> IS_EVEN2 = new Function<Integer, Boolean>() {

        @Override
        public Boolean apply(Integer n) {
            return n % 2 == 0;
        }
    };

    @Test
    public void groupByBackpressure() throws InterruptedException {
        TestSubscriber<String> ts = new TestSubscriber<>();
        Flowable.range(1, 4000).groupBy(IS_EVEN2).flatMap(new Function<GroupedFlowable<Boolean, Integer>, Flowable<String>>() {

            @Override
            public Flowable<String> apply(final GroupedFlowable<Boolean, Integer> g) {
                return g.observeOn(Schedulers.computation()).map(new Function<Integer, String>() {

                    @Override
                    public String apply(Integer l) {
                        if (g.getKey()) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                            }
                            return l + " is even.";
                        } else {
                            return l + " is odd.";
                        }
                    }
                });
            }
        }).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
    }

    <T, R> Function<T, R> just(final R value) {
        return new Function<T, R>() {

            @Override
            public R apply(T t1) {
                return value;
            }
        };
    }

    <T> Function<Integer, T> fail(T dummy) {
        return new Function<Integer, T>() {

            @Override
            public T apply(Integer t1) {
                throw new RuntimeException("Forced failure");
            }
        };
    }

    <T, R> Function<T, R> fail2(R dummy2) {
        return new Function<T, R>() {

            @Override
            public R apply(T t1) {
                throw new RuntimeException("Forced failure");
            }
        };
    }

    Function<Integer, Integer> dbl = new Function<Integer, Integer>() {

        @Override
        public Integer apply(Integer t1) {
            return t1 * 2;
        }
    };

    Function<Integer, Integer> identity = new Function<Integer, Integer>() {

        @Override
        public Integer apply(Integer v) {
            return v;
        }
    };

    @Test
    public void normalBehavior() {
        Flowable<String> source = Flowable.fromIterable(Arrays.asList("  foo", " FoO ", "baR  ", "foO ", " Baz   ", "  qux ", "   bar", " BAR  ", "FOO ", "baz  ", " bAZ ", "    fOo    "));
        /*
         * foo FoO foO FOO fOo
         * baR bar BAR
         * Baz baz bAZ
         * qux
         *
         */
        Function<String, String> keysel = new Function<String, String>() {

            @Override
            public String apply(String t1) {
                return t1.trim().toLowerCase();
            }
        };
        Function<String, String> valuesel = new Function<String, String>() {

            @Override
            public String apply(String t1) {
                return t1 + t1;
            }
        };
        Flowable<String> m = source.groupBy(keysel, valuesel).flatMap(new Function<GroupedFlowable<String, String>, Publisher<String>>() {

            @Override
            public Publisher<String> apply(final GroupedFlowable<String, String> g) {
                // System.out.println("-----------> NEXT: " + g.getKey());
                return g.take(2).map(new Function<String, String>() {

                    int count;

                    @Override
                    public String apply(String v) {
                        // System.out.println(v);
                        return g.getKey() + "-" + count++;
                    }
                });
            }
        });
        TestSubscriber<String> ts = new TestSubscriber<>();
        m.subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        // System.out.println("ts .get " + ts.values());
        ts.assertNoErrors();
        assertEquals(ts.values(), Arrays.asList("foo-0", "foo-1", "bar-0", "foo-0", "baz-0", "qux-0", "bar-1", "bar-0", "foo-1", "baz-1", "baz-0", "foo-0"));
    }

    @Test
    public void keySelectorThrows() {
        Flowable<Integer> source = Flowable.just(0, 1, 2, 3, 4, 5, 6);
        Flowable<Integer> m = source.groupBy(fail(0), dbl).flatMap(FLATTEN_INTEGER);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        m.subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        assertEquals(1, ts.errors().size());
        ts.assertNoValues();
    }

    @Test
    @SuppressUndeliverable
    public void valueSelectorThrows() {
        Flowable<Integer> source = Flowable.just(0, 1, 2, 3, 4, 5, 6);
        Flowable<Integer> m = source.groupBy(identity, fail(0)).flatMap(FLATTEN_INTEGER);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        m.subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        assertEquals(1, ts.errors().size());
        ts.assertNoValues();
    }

    @Test
    public void innerEscapeCompleted() {
        Flowable<Integer> source = Flowable.just(0);
        Flowable<Integer> m = source.groupBy(identity, dbl).flatMap(FLATTEN_INTEGER);
        TestSubscriber<Object> ts = new TestSubscriber<>();
        m.subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        // System.out.println(ts.values());
    }

    /**
     * Assert we get an IllegalStateException if trying to subscribe to an inner GroupedFlowable more than once.
     */
    @Test
    public void exceptionIfSubscribeToChildMoreThanOnce() {
        Flowable<Integer> source = Flowable.just(0);
        final AtomicReference<GroupedFlowable<Integer, Integer>> inner = new AtomicReference<>();
        Flowable<GroupedFlowable<Integer, Integer>> m = source.groupBy(identity, dbl);
        m.subscribe(new Consumer<GroupedFlowable<Integer, Integer>>() {

            @Override
            public void accept(GroupedFlowable<Integer, Integer> t1) {
                inner.set(t1);
            }
        });
        inner.get().subscribe();
        Subscriber<Integer> subscriber2 = TestHelper.mockSubscriber();
        inner.get().subscribe(subscriber2);
        verify(subscriber2, never()).onComplete();
        verify(subscriber2, never()).onNext(anyInt());
        verify(subscriber2).onError(any(IllegalStateException.class));
    }

    @Test
    @SuppressUndeliverable
    public void error2() {
        Flowable<Integer> source = Flowable.concat(Flowable.just(0), Flowable.<Integer>error(new TestException("Forced failure")));
        Flowable<Integer> m = source.groupBy(identity, dbl).flatMap(FLATTEN_INTEGER);
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        m.subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        assertEquals(1, ts.errors().size());
        ts.assertValueCount(1);
    }

    @Test
    public void groupByBackpressure3() throws InterruptedException {
        TestSubscriber<String> ts = new TestSubscriber<>();
        Flowable.range(1, 4000).groupBy(IS_EVEN2).flatMap(new Function<GroupedFlowable<Boolean, Integer>, Flowable<String>>() {

            @Override
            public Flowable<String> apply(final GroupedFlowable<Boolean, Integer> g) {
                return g.doOnComplete(new Action() {

                    @Override
                    public void run() {
                        // System.out.println("//////////////////// COMPLETED-A");
                    }
                }).observeOn(Schedulers.computation()).map(new Function<Integer, String>() {

                    int c;

                    @Override
                    public String apply(Integer l) {
                        if (g.getKey()) {
                            if (c++ < 400) {
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {
                                }
                            }
                            return l + " is even.";
                        } else {
                            return l + " is odd.";
                        }
                    }
                }).doOnComplete(new Action() {

                    @Override
                    public void run() {
                        // System.out.println("//////////////////// COMPLETED-B");
                    }
                });
            }
        }).doOnEach(new Consumer<Notification<String>>() {

            @Override
            public void accept(Notification<String> t1) {
                // System.out.println("NEXT: " + t1);
            }
        }).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
    }

    @Test
    public void groupByBackpressure2() throws InterruptedException {
        TestSubscriber<String> ts = new TestSubscriber<>();
        Flowable.range(1, 4000).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) {
                // System.out.println("testgroupByBackpressure2 >> " + v);
            }
        }).groupBy(IS_EVEN2).flatMap(new Function<GroupedFlowable<Boolean, Integer>, Flowable<String>>() {

            @Override
            public Flowable<String> apply(final GroupedFlowable<Boolean, Integer> g) {
                return g.take(2).observeOn(Schedulers.computation()).map(new Function<Integer, String>() {

                    @Override
                    public String apply(Integer l) {
                        if (g.getKey()) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                            }
                            return l + " is even.";
                        } else {
                            return l + " is odd.";
                        }
                    }
                });
            }
        }, // a lot of groups are created due to take(2)
        4000).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
    }

    @Test
    public void groupByWithNullKey() {
        final String[] key = new String[] { "uninitialized" };
        final List<String> values = new ArrayList<>();
        Flowable.just("a", "b", "c").groupBy(new Function<String, String>() {

            @Override
            public String apply(String value) {
                return null;
            }
        }).subscribe(new Consumer<GroupedFlowable<String, String>>() {

            @Override
            public void accept(GroupedFlowable<String, String> groupedFlowable) {
                key[0] = groupedFlowable.getKey();
                groupedFlowable.subscribe(new Consumer<String>() {

                    @Override
                    public void accept(String s) {
                        values.add(s);
                    }
                });
            }
        });
        assertNull(key[0]);
        assertEquals(Arrays.asList("a", "b", "c"), values);
    }

    @Test
    public void groupByUnsubscribe() {
        final Subscription s = mock(Subscription.class);
        Flowable<Integer> f = Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(s);
            }
        });
        TestSubscriber<Object> ts = new TestSubscriber<>();
        f.groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer integer) {
                return null;
            }
        }).subscribe(ts);
        ts.cancel();
        verify(s).cancel();
    }

    @Test
    public void groupByShouldPropagateError() {
        final Throwable e = new RuntimeException("Oops");
        final TestSubscriberEx<Integer> inner1 = new TestSubscriberEx<>();
        final TestSubscriberEx<Integer> inner2 = new TestSubscriberEx<>();
        final TestSubscriberEx<GroupedFlowable<Integer, Integer>> outer = new TestSubscriberEx<>(new DefaultSubscriber<GroupedFlowable<Integer, Integer>>() {

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(GroupedFlowable<Integer, Integer> f) {
                if (f.getKey() == 0) {
                    f.subscribe(inner1);
                } else {
                    f.subscribe(inner2);
                }
            }
        });
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                subscriber.onNext(0);
                subscriber.onNext(1);
                subscriber.onError(e);
            }
        }).groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer i) {
                return i % 2;
            }
        }).subscribe(outer);
        assertEquals(Arrays.asList(e), outer.errors());
        assertEquals(Arrays.asList(e), inner1.errors());
        assertEquals(Arrays.asList(e), inner2.errors());
    }

    @Test
    public void requestOverflow() {
        final AtomicBoolean completed = new AtomicBoolean(false);
        Flowable.just(1, 2, 3).groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t) {
                return 1;
            }
        }).concatMap(new Function<GroupedFlowable<Integer, Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(GroupedFlowable<Integer, Integer> g) {
                return g;
            }
        }).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(2);
            }

            @Override
            public void onComplete() {
                completed.set(true);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
                // System.out.println(t);
                // provoke possible request overflow
                request(Long.MAX_VALUE - 1);
            }
        });
        assertTrue(completed.get());
    }

    /**
     * Issue #3425.
     *
     * The problem is that a request of 1 may create a new group, emit to the desired group
     * or emit to a completely different group. In this test, the merge requests N which
     * must be produced by the range, however it will create a bunch of groups before the actual
     * group receives a value.
     *
     * 12/03/2019: this test produces abandoned groups and as such keeps producing new groups
     * that have to be ready to be received by observeOn and merge.
     */
    @Test
    public void backpressureObserveOnOuter() {
        int n = 500;
        for (int j = 0; j < 1000; j++) {
            Flowable.merge(Flowable.range(0, n).groupBy(new Function<Integer, Object>() {

                @Override
                public Object apply(Integer i) {
                    return i % (Flowable.bufferSize() + 2);
                }
            }).observeOn(Schedulers.computation(), false, n), n).blockingLast();
        }
    }

    @Test(expected = MissingBackpressureException.class)
    public void backpressureObserveOnOuterMissingBackpressure() {
        for (int j = 0; j < 1000; j++) {
            Flowable.merge(Flowable.range(0, 500).groupBy(new Function<Integer, Object>() {

                @Override
                public Object apply(Integer i) {
                    return i % (Flowable.bufferSize() + 2);
                }
            }).observeOn(Schedulers.computation())).blockingLast();
        }
    }

    /**
     * Synchronous verification of issue #3425.
     */
    @Test
    public void backpressureInnerDoesntOverflowOuter() {
        TestSubscriber<GroupedFlowable<Integer, Integer>> ts = new TestSubscriber<>(0L);
        PublishProcessor<Integer> pp = PublishProcessor.create();
        pp.groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        }).doOnNext(new Consumer<GroupedFlowable<Integer, Integer>>() {

            @Override
            public void accept(GroupedFlowable<Integer, Integer> g) {
                g.subscribe();
            }
        }).subscribe(ts);
        ts.request(1);
        pp.onNext(1);
        ts.assertNotComplete();
        ts.assertNoErrors();
        ts.assertValueCount(1);
    }

    @Test
    public void backpressureInnerDoesntOverflowOuterMissingBackpressure() {
        TestSubscriber<GroupedFlowable<Integer, Integer>> ts = new TestSubscriber<>(1);
        Flowable.fromArray(1, 2).groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v;
            }
        }).doOnNext(new Consumer<GroupedFlowable<Integer, Integer>>() {

            @Override
            public void accept(GroupedFlowable<Integer, Integer> g) {
                g.subscribe();
            }
        }).subscribe(ts);
        ts.assertValueCount(1).assertError(MissingBackpressureException.class).assertNotComplete();
    }

    @Test
    public void oneGroupInnerRequestsTwiceBuffer() {
        // FIXME: delayed requesting in groupBy results in group abandonment
        TestSubscriber<Object> ts1 = new TestSubscriber<>(1L);
        final TestSubscriber<Object> ts2 = new TestSubscriber<>(0L);
        Flowable.range(1, Flowable.bufferSize() * 2).groupBy(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) {
                return 1;
            }
        }).doOnNext(new Consumer<GroupedFlowable<Object, Integer>>() {

            @Override
            public void accept(GroupedFlowable<Object, Integer> g) {
                g.subscribe(ts2);
            }
        }).subscribe(ts1);
        ts1.assertValueCount(1);
        ts1.assertNoErrors();
        ts1.assertNotComplete();
        ts2.assertNoValues();
        ts2.assertNoErrors();
        ts2.assertNotComplete();
        ts2.request(Flowable.bufferSize() * 2);
        ts2.assertValueCount(Flowable.bufferSize() * 2);
        ts2.assertNoErrors();
        ts2.assertComplete();
    }

    @Test
    public void outerInnerFusion() {
        final TestSubscriberEx<Integer> ts1 = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        final TestSubscriberEx<GroupedFlowable<Integer, Integer>> ts2 = new TestSubscriberEx<GroupedFlowable<Integer, Integer>>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.range(1, 10).groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return 1;
            }
        }, new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                return v + 1;
            }
        }).doOnNext(new Consumer<GroupedFlowable<Integer, Integer>>() {

            @Override
            public void accept(GroupedFlowable<Integer, Integer> g) {
                g.subscribe(ts1);
            }
        }).subscribe(ts2);
        ts1.assertFusionMode(QueueFuseable.NONE).assertValues(2, 3, 4, 5, 6, 7, 8, 9, 10, 11).assertNoErrors().assertComplete();
        ts2.assertValueCount(1).assertNoErrors().assertComplete();
    }

    @Test
    @SuppressUndeliverable
    public void keySelectorAndDelayError() {
        Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).groupBy(Functions.<Integer>identity(), true).flatMap(new Function<GroupedFlowable<Integer, Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(GroupedFlowable<Integer, Integer> g) throws Exception {
                return g;
            }
        }).test().assertFailure(TestException.class, 1);
    }

    @Test
    @SuppressUndeliverable
    public void keyAndValueSelectorAndDelayError() {
        Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).groupBy(Functions.<Integer>identity(), Functions.<Integer>identity(), true).flatMap(new Function<GroupedFlowable<Integer, Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(GroupedFlowable<Integer, Integer> g) throws Exception {
                return g;
            }
        }).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).groupBy(Functions.justFunction(1)));
        Flowable.just(1).groupBy(Functions.justFunction(1)).doOnNext(new Consumer<GroupedFlowable<Integer, Integer>>() {

            @Override
            public void accept(GroupedFlowable<Integer, Integer> g) throws Exception {
                TestHelper.checkDisposed(g);
            }
        }).test();
    }

    @Test
    public void reentrantComplete() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    pp.onComplete();
                }
            }
        };
        Flowable.merge(pp.groupBy(Functions.justFunction(1))).subscribe(ts);
        pp.onNext(1);
        ts.assertResult(1);
    }

    @Test
    public void reentrantCompleteCancel() {
        final PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>() {

            @Override
            public void onNext(Integer t) {
                super.onNext(t);
                if (t == 1) {
                    pp.onComplete();
                    dispose();
                }
            }
        };
        Flowable.merge(pp.groupBy(Functions.justFunction(1))).subscribe(ts);
        pp.onNext(1);
        ts.assertSubscribed().assertValue(1).assertNoErrors().assertNotComplete();
    }

    @Test
    public void delayErrorSimpleComplete() {
        Flowable.just(1).groupBy(Functions.justFunction(1), true).flatMap(Functions.<Flowable<Integer>>identity()).test().assertResult(1);
    }

    @Test
    public void mainFusionRejected() {
        TestSubscriberEx<Flowable<Integer>> ts = new TestSubscriberEx<Flowable<Integer>>().setInitialFusionMode(QueueFuseable.SYNC);
        Flowable.just(1).groupBy(Functions.justFunction(1)).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertValueCount(1).assertComplete().assertNoErrors();
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Object>, Object>() {

            @Override
            public Object apply(Flowable<Object> f) throws Exception {
                return f.groupBy(Functions.justFunction(1));
            }
        }, false, 1, 1, (Object[]) null);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.just(1).hide().groupBy(Functions.justFunction(1)));
    }

    @Test
    public void badRequestInner() {
        Flowable.just(1).hide().groupBy(Functions.justFunction(1)).doOnNext(g -> {
            TestHelper.assertBadRequestReported(g);
        }).test().assertNoErrors();
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<GroupedFlowable<Integer, Object>>>() {

            @Override
            public Publisher<GroupedFlowable<Integer, Object>> apply(Flowable<Object> f) throws Exception {
                return f.groupBy(Functions.justFunction(1));
            }
        });
    }

    @Test
    public void nullKeyTakeInner() {
        Flowable.just(1).groupBy(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                return null;
            }
        }).flatMap(new Function<GroupedFlowable<Object, Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(GroupedFlowable<Object, Integer> g) throws Exception {
                return g.take(1);
            }
        }).test().assertResult(1);
    }

    @Test
    @SuppressUndeliverable
    public void groupError() {
        Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).groupBy(Functions.justFunction(1), true).flatMap(new Function<GroupedFlowable<Integer, Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(GroupedFlowable<Integer, Integer> g) throws Exception {
                return g.hide();
            }
        }).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void groupComplete() {
        Flowable.just(1).groupBy(Functions.justFunction(1), true).flatMap(new Function<GroupedFlowable<Integer, Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(GroupedFlowable<Integer, Integer> g) throws Exception {
                return g.hide();
            }
        }).test().assertResult(1);
    }

    @Test
    public void mapFactoryThrows() {
        final IOException ex = new IOException("boo");
        // 
        Function<Consumer<Object>, Map<Integer, Object>> evictingMapFactory = new Function<Consumer<Object>, Map<Integer, Object>>() {

            @Override
            public Map<Integer, Object> apply(final Consumer<Object> notify) throws Exception {
                throw ex;
            }
        };
        Flowable.just(1).groupBy(Functions.<Integer>identity(), Functions.identity(), true, 16, evictingMapFactory).test().assertNoValues().assertError(ex);
    }

    // -----------------------------------------------------------------------------------------------------------------------
    private static final Function<Integer, Integer> mod5 = new Function<Integer, Integer>() {

        @Override
        public Integer apply(Integer n) throws Exception {
            return n % 5;
        }
    };

    private static Function<GroupedFlowable<Integer, Integer>, Publisher<? extends Integer>> addCompletedKey(final List<Integer> completed) {
        return new Function<GroupedFlowable<Integer, Integer>, Publisher<? extends Integer>>() {

            @Override
            public Publisher<? extends Integer> apply(final GroupedFlowable<Integer, Integer> g) throws Exception {
                return g.doOnComplete(new Action() {

                    @Override
                    public void run() throws Exception {
                        completed.add(g.getKey());
                    }
                });
            }
        };
    }

    private static final class TestTicker extends Ticker {

        long tick;

        @Override
        public long read() {
            return tick;
        }
    }

    @Test
    public void mapFactoryExpiryCompletesGroupedFlowable() {
        final List<Integer> completed = new CopyOnWriteArrayList<>();
        Function<Consumer<Object>, Map<Integer, Object>> evictingMapFactory = createEvictingMapFactorySynchronousOnly(1);
        PublishSubject<Integer> subject = PublishSubject.create();
        TestSubscriberEx<Integer> ts = subject.toFlowable(BackpressureStrategy.BUFFER).groupBy(Functions.<Integer>identity(), Functions.<Integer>identity(), true, 16, evictingMapFactory).flatMap(addCompletedKey(completed)).to(TestHelper.<Integer>testConsumer());
        subject.onNext(1);
        subject.onNext(2);
        subject.onNext(3);
        ts.assertValues(1, 2, 3).assertNotTerminated();
        assertEquals(Arrays.asList(1, 2), completed);
        // ensure coverage of the code that clears the evicted queue
        subject.onComplete();
        ts.assertComplete();
        ts.assertValueCount(3);
    }

    @Test
    public void mapFactoryEvictionQueueClearedOnErrorCoverageOnly() {
        Function<Consumer<Object>, Map<Integer, Object>> evictingMapFactory = createEvictingMapFactorySynchronousOnly(1);
        PublishSubject<Integer> subject = PublishSubject.create();
        TestSubscriber<Integer> ts = subject.toFlowable(BackpressureStrategy.BUFFER).groupBy(Functions.<Integer>identity(), Functions.<Integer>identity(), true, 16, evictingMapFactory).flatMap(new Function<GroupedFlowable<Integer, Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(GroupedFlowable<Integer, Integer> g) throws Exception {
                return g;
            }
        }).test();
        RuntimeException ex = new RuntimeException();
        // ensure coverage of the code that clears the evicted queue
        subject.onError(ex);
        ts.assertNoValues().assertError(ex);
    }

    @Test
    public void mapFactoryWithExpiringGuavaCacheDemonstrationCodeForUseInJavadoc() {
        // javadoc will be a version of this using lambdas and without assertions
        final List<Integer> completed = new CopyOnWriteArrayList<>();
        AtomicReference<Cache<Integer, Object>> cacheOut = new AtomicReference<>();
        // size should be less than 5 to notice the effect
        Function<Consumer<Object>, Map<Integer, Object>> evictingMapFactory = createEvictingMapFactoryGuava(3, cacheOut);
        int numValues = 1000;
        TestSubscriber<Integer> ts = Flowable.range(1, numValues).groupBy(mod5, Functions.<Integer>identity(), true, 16, evictingMapFactory).flatMap(addCompletedKey(completed)).test().assertComplete();
        ts.assertValueCount(numValues);
        // the exact eviction behaviour of the guava cache is not specified so we make some approximate tests
        assertTrue(completed.size() > numValues * 0.9);
        cacheOut.get().invalidateAll();
    }

    @Test
    public void groupByEvictionCancellationOfSource5933() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final TestTicker testTicker = new TestTicker();
        Function<Consumer<Object>, Map<Integer, Object>> mapFactory = new Function<Consumer<Object>, Map<Integer, Object>>() {

            @Override
            public Map<Integer, Object> apply(final Consumer<Object> action) throws Exception {
                return // 
                CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).removalListener(new RemovalListener<Object, Object>() {

                    @Override
                    public void onRemoval(RemovalNotification<Object, Object> notification) {
                        try {
                            action.accept(notification.getValue());
                        } catch (Throwable ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }).ticker(// 
                testTicker).<Integer, Object>build().asMap();
            }
        };
        final List<String> list = new CopyOnWriteArrayList<>();
        Flowable<Integer> stream = // 
        source.doOnCancel(new Action() {

            @Override
            public void run() throws Exception {
                list.add("Source canceled");
            }
        }).<Integer, Integer>groupBy(Functions.<Integer>identity(), Functions.<Integer>identity(), false, Flowable.bufferSize(), // 
        mapFactory).flatMap(new Function<GroupedFlowable<Integer, Integer>, Publisher<? extends Integer>>() {

            @Override
            public Publisher<? extends Integer> apply(GroupedFlowable<Integer, Integer> group) throws Exception {
                return // 
                group.doOnComplete(new Action() {

                    @Override
                    public void run() throws Exception {
                        list.add("Group completed");
                    }
                }).doOnCancel(new Action() {

                    @Override
                    public void run() throws Exception {
                        list.add("Group canceled");
                    }
                });
            }
        });
        TestSubscriber<Integer> ts = // 
        stream.doOnCancel(new Action() {

            @Override
            public void run() throws Exception {
                list.add("Outer group by canceled");
            }
        }).test();
        // Send 3 in the same group and wait for them to be seen
        source.onNext(1);
        source.onNext(1);
        source.onNext(1);
        ts.awaitCount(3);
        // Advance time far enough to evict the group.
        // NOTE -- Comment this line out to make the test "pass".
        testTicker.tick = TimeUnit.SECONDS.toNanos(6);
        // Send more data in the group (triggering eviction and recreation)
        source.onNext(1);
        // Wait for the last 2 and then cancel the subscription
        ts.awaitCount(4);
        ts.cancel();
        // Observe the result.  Note that right now the result differs depending on whether eviction occurred or
        // not.  The observed sequence in that case is:  Group completed, Outer group by canceled., Group canceled.
        // The addition of the "Group completed" is actually fine, but the fact that the cancel doesn't reach the
        // source seems like a bug.  Commenting out the setting of "tick" above will produce the "expected" sequence.
        // System.out.println(list);
        assertTrue(list.contains("Source canceled"));
        assertEquals(Arrays.asList(// this is here when eviction occurs
        "Group completed", "Outer group by canceled", "Group canceled", // This is *not* here when eviction occurs
        "Source canceled"), list);
    }

    // not thread safe
    private static final class SingleThreadEvictingHashMap<K, V> implements Map<K, V> {

        private final List<K> list = new ArrayList<>();

        private final Map<K, V> map = new HashMap<>();

        private final int maxSize;

        private final Consumer<V> evictedListener;

        SingleThreadEvictingHashMap(int maxSize, Consumer<V> evictedListener) {
            this.maxSize = maxSize;
            this.evictedListener = evictedListener;
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return map.get(key);
        }

        @Override
        public V put(K key, V value) {
            list.remove(key);
            V v;
            if (maxSize > 0 && list.size() == maxSize) {
                // remove first
                K k = list.get(0);
                list.remove(0);
                v = map.remove(k);
            } else {
                v = null;
            }
            list.add(key);
            V result = map.put(key, value);
            if (v != null) {
                try {
                    evictedListener.accept(v);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
            return result;
        }

        @Override
        public V remove(Object key) {
            list.remove(key);
            return map.remove(key);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public void clear() {
            list.clear();
            map.clear();
        }

        @Override
        public Set<K> keySet() {
            return map.keySet();
        }

        @Override
        public Collection<V> values() {
            return map.values();
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return map.entrySet();
        }
    }

    private static Function<Consumer<Object>, Map<Integer, Object>> createEvictingMapFactoryGuava(final int maxSize, final AtomicReference<Cache<Integer, Object>> cacheOut) {
        // 
        Function<Consumer<Object>, Map<Integer, Object>> evictingMapFactory = new Function<Consumer<Object>, Map<Integer, Object>>() {

            @Override
            public Map<Integer, Object> apply(final Consumer<Object> notify) throws Exception {
                Cache<Integer, Object> cache = // 
                CacheBuilder.newBuilder().maximumSize(// 
                maxSize).removalListener(new RemovalListener<Integer, Object>() {

                    @Override
                    public void onRemoval(RemovalNotification<Integer, Object> notification) {
                        try {
                            notify.accept(notification.getValue());
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).<Integer, Object>build();
                cacheOut.set(cache);
                return cache.asMap();
            }
        };
        return evictingMapFactory;
    }

    private static Function<Consumer<Object>, Map<Integer, Object>> createEvictingMapFactorySynchronousOnly(final int maxSize) {
        // 
        Function<Consumer<Object>, Map<Integer, Object>> evictingMapFactory = new Function<Consumer<Object>, Map<Integer, Object>>() {

            @Override
            public Map<Integer, Object> apply(final Consumer<Object> notify) throws Exception {
                return new SingleThreadEvictingHashMap<>(maxSize, new Consumer<Object>() {

                    @Override
                    public void accept(Object object) {
                        try {
                            notify.accept(object);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        };
        return evictingMapFactory;
    }

    // -----------------------------------------------------------------------------------------------------------------------
    @Test
    public void cancellationOfUpstreamWhenGroupedFlowableCompletes() {
        final AtomicBoolean cancelled = new AtomicBoolean();
        Flowable.just(1).repeat().doOnCancel(new Action() {

            @Override
            public void run() throws Exception {
                cancelled.set(true);
            }
        }).groupBy(Functions.<Integer>identity(), // 
        Functions.<Integer>identity()).flatMap(new Function<GroupedFlowable<Integer, Integer>, Publisher<? extends Object>>() {

            @Override
            public Publisher<? extends Object> apply(GroupedFlowable<Integer, Integer> g) throws Exception {
                return g.first(0).toFlowable();
            }
        }).take(// 
        4).test().assertComplete();
        assertTrue(cancelled.get());
    }

    @Test
    public void cancelOverFlatmapRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            final TestSubscriber<Integer> ts = new TestSubscriber<>();
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            pp.groupBy(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Throwable {
                    return v % 10;
                }
            }, Functions.<Integer>identity(), false, 2048).flatMap(new Function<GroupedFlowable<Integer, Integer>, GroupedFlowable<Integer, Integer>>() {

                @Override
                public GroupedFlowable<Integer, Integer> apply(GroupedFlowable<Integer, Integer> v) throws Throwable {
                    return v;
                }
            }).subscribe(ts);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    for (int j = 0; j < 1000; j++) {
                        pp.onNext(j);
                    }
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            TestHelper.race(r1, r2);
            assertFalse("Round " + i, pp.hasSubscribers());
        }
    }

    @Test
    public void abandonedGroupsNoDataloss() {
        final List<GroupedFlowable<Integer, Integer>> groups = new ArrayList<>();
        Flowable.range(1, 1000).groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Throwable {
                return v % 10;
            }
        }).doOnNext(new Consumer<GroupedFlowable<Integer, Integer>>() {

            @Override
            public void accept(GroupedFlowable<Integer, Integer> v) throws Throwable {
                groups.add(v);
            }
        }).test().assertValueCount(1000).assertComplete().assertNoErrors();
        Flowable.concat(groups).test().assertValueCount(1000).assertNoErrors().assertComplete();
    }

    @Test
    public void newGroupValueSelectorFails() {
        TestSubscriber<Object> ts1 = new TestSubscriber<>();
        final TestSubscriber<Object> ts2 = new TestSubscriber<>();
        Flowable.just(1).groupBy(Functions.<Integer>identity(), new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Throwable {
                throw new TestException();
            }
        }).doOnNext(new Consumer<GroupedFlowable<Integer, Object>>() {

            @Override
            public void accept(GroupedFlowable<Integer, Object> g) throws Throwable {
                g.subscribe(ts2);
            }
        }).subscribe(ts1);
        ts1.assertValueCount(1).assertError(TestException.class).assertNotComplete();
        ts2.assertFailure(TestException.class);
    }

    @Test
    public void existingGroupValueSelectorFails() {
        TestSubscriber<Object> ts1 = new TestSubscriber<>();
        final TestSubscriber<Object> ts2 = new TestSubscriber<>();
        Flowable.just(1, 2).groupBy(Functions.justFunction(1), new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Throwable {
                if (v == 2) {
                    throw new TestException();
                }
                return v;
            }
        }).doOnNext(new Consumer<GroupedFlowable<Integer, Object>>() {

            @Override
            public void accept(GroupedFlowable<Integer, Object> g) throws Throwable {
                g.subscribe(ts2);
            }
        }).subscribe(ts1);
        ts1.assertValueCount(1).assertError(TestException.class).assertNotComplete();
        ts2.assertFailure(TestException.class, 1);
    }

    @Test
    public void fusedParallelGroupProcessing() {
        Flowable.range(0, 500000).subscribeOn(Schedulers.single()).groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer i) throws Throwable {
                return i % 2;
            }
        }).flatMap(new Function<GroupedFlowable<Integer, Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(GroupedFlowable<Integer, Integer> g) {
                return g.getKey() == 0 ? g.parallel().runOn(Schedulers.computation()).map(Functions.<Integer>identity()).sequential() : // no need to use hide
                g.map(Functions.<Integer>identity());
            }
        }).test().awaitDone(20, TimeUnit.SECONDS).assertValueCount(500000).assertComplete().assertNoErrors();
    }

    @Test
    public void valueSelectorCrashAndMissingBackpressure() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriberEx<GroupedFlowable<Integer, Integer>> ts = pp.groupBy(Functions.justFunction(1), new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t) throws Throwable {
                throw new TestException();
            }
        }).subscribeWith(new TestSubscriberEx<>(0L));
        assertTrue(pp.offer(1));
        ts.assertFailure(MissingBackpressureException.class);
        assertTrue("" + ts.errors().get(0).getCause(), ts.errors().get(0).getCause() instanceof TestException);
    }

    @Test
    public void fusedGroupClearedOnCancel() {
        Flowable.just(1).groupBy(Functions.<Integer>identity()).flatMap(new Function<GroupedFlowable<Integer, Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(GroupedFlowable<Integer, Integer> g) throws Throwable {
                return g.observeOn(ImmediateThinScheduler.INSTANCE).take(1);
            }
        }).test().assertResult(1);
    }

    @Test
    public void fusedGroupClearedOnCancelDelayed() {
        Flowable.range(1, 100).groupBy(Functions.<Integer, Integer>justFunction(1)).flatMap(new Function<GroupedFlowable<Integer, Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(GroupedFlowable<Integer, Integer> g) throws Throwable {
                return g.observeOn(Schedulers.io()).doOnNext(new Consumer<Integer>() {

                    @Override
                    public void accept(Integer v) throws Throwable {
                        Thread.sleep(100);
                    }
                }).take(1);
            }
        }).test().awaitDone(5, TimeUnit.SECONDS).assertNoErrors().assertComplete();
    }

    @Test
    public void cancelledGroupResumesRequesting() {
        final List<TestSubscriber<Integer>> tss = new ArrayList<>();
        final AtomicInteger counter = new AtomicInteger();
        final AtomicBoolean done = new AtomicBoolean();
        Flowable.range(1, 1000).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                counter.getAndIncrement();
            }
        }).groupBy(Functions.justFunction(1)).subscribe(new Consumer<GroupedFlowable<Integer, Integer>>() {

            @Override
            public void accept(GroupedFlowable<Integer, Integer> v) throws Exception {
                TestSubscriber<Integer> ts = TestSubscriber.create(0L);
                tss.add(ts);
                v.subscribe(ts);
            }
        }, Functions.emptyConsumer(), new Action() {

            @Override
            public void run() throws Exception {
                done.set(true);
            }
        });
        while (!done.get()) {
            tss.remove(0).cancel();
        }
        assertEquals(1000, counter.get());
    }

    @Test
    public void delayErrorCompleteMoreWorkInGroup() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.groupBy(v -> 1, true).flatMap(g -> g.doOnNext(v -> {
            if (v == 1) {
                pp.onNext(2);
                pp.onComplete();
            }
        })).test();
        pp.onNext(1);
        ts.assertResult(1, 2);
    }

    @Test
    public void groupSyncFusionRejected() {
        Flowable.just(1).groupBy(v -> 1).doOnNext(g -> {
            g.subscribeWith(new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.NONE);
        }).test().assertComplete();
    }

    @Test
    public void subscribeAbandonRace() throws Throwable {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishProcessor<Integer> pp = PublishProcessor.create();
            TestSubscriber<Integer> ts = TestSubscriber.create();
            CountDownLatch cdl = new CountDownLatch(1);
            pp.groupBy(v -> 1).doOnNext(g -> {
                TestHelper.raceOther(() -> {
                    g.subscribe(ts);
                }, cdl);
            }).test();
            pp.onNext(1);
            cdl.await();
            ts.assertValueCount(1);
        }
    }

    @Test
    public void issue6974() {
        FlowableTransformer<Integer, Integer> operation = source -> source.publish(shared -> shared.firstElement().flatMapPublisher(firstElement -> Flowable.just(firstElement).concatWith(shared)));
        issue6974Run(20, 500_000, 20 - 1, 20 * 2, operation, false);
        issue6974Run(20, 500_000, 20, 20 * 2, operation, false);
    }

    static void issue6974Run(int groups, int iterations, int sizeCap, int flatMapConcurrency, FlowableTransformer<Integer, Integer> operation, boolean notifyOnExplicitRevoke) {
        TestSubscriber<Integer> test = Flowable.range(1, groups).repeat(iterations / groups).groupBy(i -> i, i -> i, false, 128, sizeCap(sizeCap, notifyOnExplicitRevoke)).flatMap(gf -> gf.compose(operation), flatMapConcurrency).test();
        test.awaitDone(5, TimeUnit.SECONDS);
        test.assertValueCount(iterations);
    }

    static <T> Function<Consumer<Object>, Map<T, Object>> sizeCap(int maxCapacity, boolean notifyOnExplicit) {
        return itemEvictConsumer -> CacheBuilder.newBuilder().maximumSize(maxCapacity).removalListener(notification -> {
            if (notification.getCause() != RemovalCause.EXPLICIT || notifyOnExplicit) {
                try {
                    itemEvictConsumer.accept(notification.getValue());
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        }).<T, Object>build().asMap();
    }

    static void issue6974RunPart2(int groupByBufferSize, int flatMapMaxConcurrency, int groups, boolean notifyOnExplicitEviction) {
        TestSubscriber<Integer> ts = Flowable.range(1, 500_000).map(i -> i % groups).groupBy(i -> i, i -> i, false, groupByBufferSize, // set cap too high
        sizeCap(groups * 100, notifyOnExplicitEviction)).flatMap(gf -> gf.take(10, TimeUnit.MILLISECONDS), flatMapMaxConcurrency).test();
        ts.awaitDone(5, TimeUnit.SECONDS).assertNoErrors().assertComplete();
    }

    @Test
    public void issue6974Part2Case1() {
        final int groups = 20;
        // Not completed (Timed out), buffer is too small
        int groupByBufferSize = groups * 2;
        int flatMapMaxConcurrency = 2 * groups;
        boolean notifyOnExplicitEviction = false;
        issue6974RunPart2(groupByBufferSize, flatMapMaxConcurrency, groups, notifyOnExplicitEviction);
    }

    @Test
    public void issue6974Part2Case2() {
        final int groups = 20;
        // Timeout... explicit eviction notification makes difference
        int groupByBufferSize = groups * 30;
        int flatMapMaxConcurrency = 2 * groups;
        boolean notifyOnExplicitEviction = true;
        issue6974RunPart2(groupByBufferSize, flatMapMaxConcurrency, groups, notifyOnExplicitEviction);
    }

    /*
     * Disabled: Takes very long. Run it locally only.
    @Test
    public void issue6974Part2Case2Loop() {
        for (int i = 0; i < 1000; i++) {
            issue6974Part2Case2();
        }
    }
    */
    static void issue6974RunPart2NoEvict(int groupByBufferSize, int flatMapMaxConcurrency, int groups, boolean notifyOnExplicitEviction) {
        Flowable.range(1, 500_000).map(i -> i % groups).groupBy(i -> i).flatMap(gf -> gf.take(10, TimeUnit.MILLISECONDS), flatMapMaxConcurrency).subscribeWith(new TestSubscriberEx<>()).awaitDone(5, TimeUnit.SECONDS).assertTerminated();
    }

    @Test
    public void issue6974Part2Case1NoEvict() {
        final int groups = 20;
        // Not completed (Timed out), buffer is too small
        int groupByBufferSize = groups * 2;
        int flatMapMaxConcurrency = 2 * groups;
        boolean notifyOnExplicitEviction = false;
        issue6974RunPart2NoEvict(groupByBufferSize, flatMapMaxConcurrency, groups, notifyOnExplicitEviction);
    }

    /*
     * Disabled: Takes very long. Run it locally only.
    @Test
    public void issue6974Part2Case1NoEvictLoop() {
        for (int i = 0; i < 1000; i++) {
            issue6974Part2Case1NoEvict();
        }
    }
    */
    @Test
    public void issue6974Part2Case1ObserveOn() {
        final int groups = 20;
        // Not completed (Timed out), buffer is too small
        int groupByBufferSize = groups * 2;
        int flatMapMaxConcurrency = 2 * groups;
        boolean notifyOnExplicitEviction = false;
        Flowable.range(1, 500_000).map(i -> i % groups).doOnCancel(() -> {
            // System.out.println("Cancelling upstream");
        }).groupBy(i -> i, i -> i, false, groupByBufferSize, sizeCap(groups * 2, notifyOnExplicitEviction)).flatMap(gf -> gf.observeOn(Schedulers.computation()).take(10, TimeUnit.MILLISECONDS), flatMapMaxConcurrency).subscribeWith(new TestSubscriberEx<>()).awaitDone(5, TimeUnit.SECONDS).assertTerminated();
    }

    @Test
    public void issue6974Part2Case1ObserveOnHide() {
        final int groups = 20;
        // Not completed (Timed out), buffer is too small
        int groupByBufferSize = groups * 2;
        int flatMapMaxConcurrency = 2 * groups;
        boolean notifyOnExplicitEviction = false;
        Flowable.range(1, 500_000).map(i -> i % groups).doOnCancel(() -> System.out.println("Cancelling upstream")).groupBy(i -> i, i -> i, false, groupByBufferSize, sizeCap(groups * 2, notifyOnExplicitEviction)).flatMap(gf -> gf.hide().observeOn(Schedulers.computation()).take(10, TimeUnit.MILLISECONDS), flatMapMaxConcurrency).subscribeWith(new TestSubscriberEx<>()).awaitDone(5, TimeUnit.SECONDS).assertTerminated();
    }

    @Test
    public void issue6974Part2Case1ObserveOnNoCap() {
        final int groups = 20;
        // Not completed (Timed out), buffer is too small
        int flatMapMaxConcurrency = 1_000_000;
        Flowable.range(1, 500_000).map(i -> i % groups).doOnRequest(v -> {
            // System.out.println("Source: " + v);
        }).groupBy(i -> i).flatMap(gf -> gf.observeOn(Schedulers.computation()).take(10, TimeUnit.MILLISECONDS), flatMapMaxConcurrency).test().awaitDone(5, TimeUnit.SECONDS).assertNoErrors().assertComplete();
    }

    @Test
    public void issue6974Part2Case1ObserveOnNoCapHide() {
        final int groups = 20;
        // Not completed (Timed out), buffer is too small
        int flatMapMaxConcurrency = 1_000_000;
        Flowable.range(1, 500_000).map(i -> i % groups).doOnRequest(v -> {
            // System.out.println("Source: " + v);
        }).groupBy(i -> i).flatMap(gf -> gf.hide().observeOn(Schedulers.computation()).take(10, TimeUnit.MILLISECONDS), flatMapMaxConcurrency).test().awaitDone(5, TimeUnit.SECONDS).assertNoErrors().assertComplete();
    }

    /*
     * Disabled: Takes very long. Run it locally only.
    @Test
    public void issue6974Part2Case1ObserveOnNoCapHideLoop() {
        for (int i = 0; i < 100; i++) {
            issue6974Part2Case1ObserveOnNoCapHide();
        }
    }
    */
    @Test
    public void issue6974Part2Case1ObserveOnConditional() {
        final int groups = 20;
        // Not completed (Timed out), buffer is too small
        int groupByBufferSize = groups * 2;
        int flatMapMaxConcurrency = 2 * groups;
        boolean notifyOnExplicitEviction = false;
        Flowable.range(1, 500_000).map(i -> i % groups).doOnCancel(() -> System.out.println("Cancelling upstream")).groupBy(i -> i, i -> i, false, groupByBufferSize, sizeCap(groups * 2, notifyOnExplicitEviction)).flatMap(gf -> gf.observeOn(Schedulers.computation()).filter(v -> true).take(10, TimeUnit.MILLISECONDS), flatMapMaxConcurrency).subscribeWith(new TestSubscriberEx<>()).awaitDone(5, TimeUnit.SECONDS).assertTerminated();
    }

    @Test
    public void issue6974Part2Case1ObserveOnConditionalHide() {
        final int groups = 20;
        // Not completed (Timed out), buffer is too small
        int groupByBufferSize = groups * 2;
        int flatMapMaxConcurrency = 2 * groups;
        boolean notifyOnExplicitEviction = false;
        Flowable.range(1, 500_000).map(i -> i % groups).doOnCancel(() -> System.out.println("Cancelling upstream")).groupBy(i -> i, i -> i, false, groupByBufferSize, sizeCap(groups * 2, notifyOnExplicitEviction)).flatMap(gf -> gf.hide().observeOn(Schedulers.computation()).filter(v -> true).take(10, TimeUnit.MILLISECONDS), flatMapMaxConcurrency).subscribeWith(new TestSubscriberEx<>()).awaitDone(5, TimeUnit.SECONDS).assertTerminated();
    }

    /*
     * Disabled: Takes very long. Run it locally only.
    @Test
    public void issue6974Part2Case1ObserveOnHideLoop() {
        for (int i = 0; i < 100; i++) {
            issue6974Part2Case1ObserveOnHide();
        }
    }
    */
    static <T> Function<Consumer<Object>, ConcurrentMap<T, Object>> ttlCapGuava(Duration ttl) {
        return itemEvictConsumer -> CacheBuilder.newBuilder().expireAfterWrite(ttl).removalListener(n -> {
            if (n.getCause() != com.google.common.cache.RemovalCause.EXPLICIT) {
                try {
                    itemEvictConsumer.accept(n.getValue());
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        }).<T, Object>build().asMap();
    }

    @Test
    public void issue6982Case1() {
        final int groups = 20;
        int groupByBufferSize = 2;
        int flatMapMaxConcurrency = 200 * groups;
        // ~50% of executions - Not completed (latch = 1, values = 500000, errors = 0, completions = 0, timeout!,
        // disposed!)
        Flowable.range(1, 500_000).map(i -> i % groups).groupBy(i -> i, i -> i, false, groupByBufferSize, ttlCapGuava(Duration.ofMillis(10))).flatMap(gf -> gf.observeOn(Schedulers.computation()), flatMapMaxConcurrency).test().awaitDone(5, TimeUnit.SECONDS).assertNoErrors().assertComplete();
    }

    /*
     * Disabled: Takes very long. Run it locally only.
    @Test
    public void issue6982Case1Loop() {
        for (int i = 0; i < 200; i++) {
            // System.out.println("issue6982Case1Loop "  + i);
            issue6982Case1();
        }
    }
     */
    @Test
    public void issue6982Case2() {
        final int groups = 20;
        int groupByBufferSize = groups * 30;
        int flatMapMaxConcurrency = groups * 500;
        // Always : Not completed (latch = 1, values = 14100, errors = 0, completions = 0, timeout!, disposed!)
        Flowable.range(1, 500_000).map(i -> i % groups).groupBy(i -> i, i -> i, false, groupByBufferSize, ttlCapGuava(Duration.ofMillis(10))).flatMap(gf -> gf.observeOn(Schedulers.computation()), flatMapMaxConcurrency).test().awaitDone(5, TimeUnit.SECONDS).assertNoErrors().assertComplete();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableGroupByTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupBy() throws java.lang.Throwable {
            this.payloads.groupBy.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupByWithElementSelector() throws java.lang.Throwable {
            this.payloads.groupByWithElementSelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupByWithElementSelector2() throws java.lang.Throwable {
            this.payloads.groupByWithElementSelector2.evaluate();
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
        public void benchmark_groupedEventStream() throws java.lang.Throwable {
            this.payloads.groupedEventStream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeOnNestedTakeAndSyncInfiniteStream() throws java.lang.Throwable {
            this.payloads.unsubscribeOnNestedTakeAndSyncInfiniteStream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeOnNestedTakeAndAsyncInfiniteStream() throws java.lang.Throwable {
            this.payloads.unsubscribeOnNestedTakeAndAsyncInfiniteStream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeViaTakeOnGroupThenMergeAndTake() throws java.lang.Throwable {
            this.payloads.unsubscribeViaTakeOnGroupThenMergeAndTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeViaTakeOnGroupThenTakeOnInner() throws java.lang.Throwable {
            this.payloads.unsubscribeViaTakeOnGroupThenTakeOnInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_staggeredCompletion() throws java.lang.Throwable {
            this.payloads.staggeredCompletion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completionIfInnerNotSubscribed() throws java.lang.Throwable {
            this.payloads.completionIfInnerNotSubscribed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoringGroups() throws java.lang.Throwable {
            this.payloads.ignoringGroups.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsAndThenComplete() throws java.lang.Throwable {
            this.payloads.firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsAndThenComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsWhichThenSubscribesOnAndDelaysAndThenCompletes() throws java.lang.Throwable {
            this.payloads.firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsWhichThenSubscribesOnAndDelaysAndThenCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsWhichThenObservesOnAndDelaysAndThenCompletes() throws java.lang.Throwable {
            this.payloads.firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsWhichThenObservesOnAndDelaysAndThenCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupsWithNestedSubscribeOn() throws java.lang.Throwable {
            this.payloads.groupsWithNestedSubscribeOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupsWithNestedObserveOn() throws java.lang.Throwable {
            this.payloads.groupsWithNestedObserveOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupByOnAsynchronousSourceAcceptsMultipleSubscriptions() throws java.lang.Throwable {
            this.payloads.groupByOnAsynchronousSourceAcceptsMultipleSubscriptions.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupByBackpressure() throws java.lang.Throwable {
            this.payloads.groupByBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalBehavior() throws java.lang.Throwable {
            this.payloads.normalBehavior.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_keySelectorThrows() throws java.lang.Throwable {
            this.payloads.keySelectorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_valueSelectorThrows() throws java.lang.Throwable {
            this.payloads.valueSelectorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerEscapeCompleted() throws java.lang.Throwable {
            this.payloads.innerEscapeCompleted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exceptionIfSubscribeToChildMoreThanOnce() throws java.lang.Throwable {
            this.payloads.exceptionIfSubscribeToChildMoreThanOnce.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error2() throws java.lang.Throwable {
            this.payloads.error2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupByBackpressure3() throws java.lang.Throwable {
            this.payloads.groupByBackpressure3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupByBackpressure2() throws java.lang.Throwable {
            this.payloads.groupByBackpressure2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupByWithNullKey() throws java.lang.Throwable {
            this.payloads.groupByWithNullKey.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupByUnsubscribe() throws java.lang.Throwable {
            this.payloads.groupByUnsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupByShouldPropagateError() throws java.lang.Throwable {
            this.payloads.groupByShouldPropagateError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestOverflow() throws java.lang.Throwable {
            this.payloads.requestOverflow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureObserveOnOuter() throws java.lang.Throwable {
            this.payloads.backpressureObserveOnOuter.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureObserveOnOuterMissingBackpressure() throws java.lang.Throwable {
            this.payloads.backpressureObserveOnOuterMissingBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureInnerDoesntOverflowOuter() throws java.lang.Throwable {
            this.payloads.backpressureInnerDoesntOverflowOuter.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureInnerDoesntOverflowOuterMissingBackpressure() throws java.lang.Throwable {
            this.payloads.backpressureInnerDoesntOverflowOuterMissingBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneGroupInnerRequestsTwiceBuffer() throws java.lang.Throwable {
            this.payloads.oneGroupInnerRequestsTwiceBuffer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_outerInnerFusion() throws java.lang.Throwable {
            this.payloads.outerInnerFusion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_keySelectorAndDelayError() throws java.lang.Throwable {
            this.payloads.keySelectorAndDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_keyAndValueSelectorAndDelayError() throws java.lang.Throwable {
            this.payloads.keyAndValueSelectorAndDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantComplete() throws java.lang.Throwable {
            this.payloads.reentrantComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantCompleteCancel() throws java.lang.Throwable {
            this.payloads.reentrantCompleteCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorSimpleComplete() throws java.lang.Throwable {
            this.payloads.delayErrorSimpleComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainFusionRejected() throws java.lang.Throwable {
            this.payloads.mainFusionRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequestInner() throws java.lang.Throwable {
            this.payloads.badRequestInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullKeyTakeInner() throws java.lang.Throwable {
            this.payloads.nullKeyTakeInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupError() throws java.lang.Throwable {
            this.payloads.groupError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupComplete() throws java.lang.Throwable {
            this.payloads.groupComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFactoryThrows() throws java.lang.Throwable {
            this.payloads.mapFactoryThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFactoryExpiryCompletesGroupedFlowable() throws java.lang.Throwable {
            this.payloads.mapFactoryExpiryCompletesGroupedFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFactoryEvictionQueueClearedOnErrorCoverageOnly() throws java.lang.Throwable {
            this.payloads.mapFactoryEvictionQueueClearedOnErrorCoverageOnly.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapFactoryWithExpiringGuavaCacheDemonstrationCodeForUseInJavadoc() throws java.lang.Throwable {
            this.payloads.mapFactoryWithExpiringGuavaCacheDemonstrationCodeForUseInJavadoc.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupByEvictionCancellationOfSource5933() throws java.lang.Throwable {
            this.payloads.groupByEvictionCancellationOfSource5933.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellationOfUpstreamWhenGroupedFlowableCompletes() throws java.lang.Throwable {
            this.payloads.cancellationOfUpstreamWhenGroupedFlowableCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOverFlatmapRace() throws java.lang.Throwable {
            this.payloads.cancelOverFlatmapRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_abandonedGroupsNoDataloss() throws java.lang.Throwable {
            this.payloads.abandonedGroupsNoDataloss.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_newGroupValueSelectorFails() throws java.lang.Throwable {
            this.payloads.newGroupValueSelectorFails.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_existingGroupValueSelectorFails() throws java.lang.Throwable {
            this.payloads.existingGroupValueSelectorFails.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedParallelGroupProcessing() throws java.lang.Throwable {
            this.payloads.fusedParallelGroupProcessing.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_valueSelectorCrashAndMissingBackpressure() throws java.lang.Throwable {
            this.payloads.valueSelectorCrashAndMissingBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedGroupClearedOnCancel() throws java.lang.Throwable {
            this.payloads.fusedGroupClearedOnCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedGroupClearedOnCancelDelayed() throws java.lang.Throwable {
            this.payloads.fusedGroupClearedOnCancelDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelledGroupResumesRequesting() throws java.lang.Throwable {
            this.payloads.cancelledGroupResumesRequesting.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorCompleteMoreWorkInGroup() throws java.lang.Throwable {
            this.payloads.delayErrorCompleteMoreWorkInGroup.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupSyncFusionRejected() throws java.lang.Throwable {
            this.payloads.groupSyncFusionRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeAbandonRace() throws java.lang.Throwable {
            this.payloads.subscribeAbandonRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue6974() throws java.lang.Throwable {
            this.payloads.issue6974.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue6974Part2Case1() throws java.lang.Throwable {
            this.payloads.issue6974Part2Case1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue6974Part2Case2() throws java.lang.Throwable {
            this.payloads.issue6974Part2Case2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue6974Part2Case1NoEvict() throws java.lang.Throwable {
            this.payloads.issue6974Part2Case1NoEvict.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue6974Part2Case1ObserveOn() throws java.lang.Throwable {
            this.payloads.issue6974Part2Case1ObserveOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue6974Part2Case1ObserveOnHide() throws java.lang.Throwable {
            this.payloads.issue6974Part2Case1ObserveOnHide.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue6974Part2Case1ObserveOnNoCap() throws java.lang.Throwable {
            this.payloads.issue6974Part2Case1ObserveOnNoCap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue6974Part2Case1ObserveOnNoCapHide() throws java.lang.Throwable {
            this.payloads.issue6974Part2Case1ObserveOnNoCapHide.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue6974Part2Case1ObserveOnConditional() throws java.lang.Throwable {
            this.payloads.issue6974Part2Case1ObserveOnConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue6974Part2Case1ObserveOnConditionalHide() throws java.lang.Throwable {
            this.payloads.issue6974Part2Case1ObserveOnConditionalHide.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue6982Case1() throws java.lang.Throwable {
            this.payloads.issue6982Case1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue6982Case2() throws java.lang.Throwable {
            this.payloads.issue6982Case2.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupByTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupByTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupByTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupByTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableGroupByTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupByTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableGroupByTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableGroupByTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement groupBy;

            public org.junit.runners.model.Statement groupByWithElementSelector;

            public org.junit.runners.model.Statement groupByWithElementSelector2;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement groupedEventStream;

            public org.junit.runners.model.Statement unsubscribeOnNestedTakeAndSyncInfiniteStream;

            public org.junit.runners.model.Statement unsubscribeOnNestedTakeAndAsyncInfiniteStream;

            public org.junit.runners.model.Statement unsubscribeViaTakeOnGroupThenMergeAndTake;

            public org.junit.runners.model.Statement unsubscribeViaTakeOnGroupThenTakeOnInner;

            public org.junit.runners.model.Statement staggeredCompletion;

            public org.junit.runners.model.Statement completionIfInnerNotSubscribed;

            public org.junit.runners.model.Statement ignoringGroups;

            public org.junit.runners.model.Statement firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsAndThenComplete;

            public org.junit.runners.model.Statement firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsWhichThenSubscribesOnAndDelaysAndThenCompletes;

            public org.junit.runners.model.Statement firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsWhichThenObservesOnAndDelaysAndThenCompletes;

            public org.junit.runners.model.Statement groupsWithNestedSubscribeOn;

            public org.junit.runners.model.Statement groupsWithNestedObserveOn;

            public org.junit.runners.model.Statement groupByOnAsynchronousSourceAcceptsMultipleSubscriptions;

            public org.junit.runners.model.Statement groupByBackpressure;

            public org.junit.runners.model.Statement normalBehavior;

            public org.junit.runners.model.Statement keySelectorThrows;

            public org.junit.runners.model.Statement valueSelectorThrows;

            public org.junit.runners.model.Statement innerEscapeCompleted;

            public org.junit.runners.model.Statement exceptionIfSubscribeToChildMoreThanOnce;

            public org.junit.runners.model.Statement error2;

            public org.junit.runners.model.Statement groupByBackpressure3;

            public org.junit.runners.model.Statement groupByBackpressure2;

            public org.junit.runners.model.Statement groupByWithNullKey;

            public org.junit.runners.model.Statement groupByUnsubscribe;

            public org.junit.runners.model.Statement groupByShouldPropagateError;

            public org.junit.runners.model.Statement requestOverflow;

            public org.junit.runners.model.Statement backpressureObserveOnOuter;

            public org.junit.runners.model.Statement backpressureObserveOnOuterMissingBackpressure;

            public org.junit.runners.model.Statement backpressureInnerDoesntOverflowOuter;

            public org.junit.runners.model.Statement backpressureInnerDoesntOverflowOuterMissingBackpressure;

            public org.junit.runners.model.Statement oneGroupInnerRequestsTwiceBuffer;

            public org.junit.runners.model.Statement outerInnerFusion;

            public org.junit.runners.model.Statement keySelectorAndDelayError;

            public org.junit.runners.model.Statement keyAndValueSelectorAndDelayError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement reentrantComplete;

            public org.junit.runners.model.Statement reentrantCompleteCancel;

            public org.junit.runners.model.Statement delayErrorSimpleComplete;

            public org.junit.runners.model.Statement mainFusionRejected;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement badRequestInner;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement nullKeyTakeInner;

            public org.junit.runners.model.Statement groupError;

            public org.junit.runners.model.Statement groupComplete;

            public org.junit.runners.model.Statement mapFactoryThrows;

            public org.junit.runners.model.Statement mapFactoryExpiryCompletesGroupedFlowable;

            public org.junit.runners.model.Statement mapFactoryEvictionQueueClearedOnErrorCoverageOnly;

            public org.junit.runners.model.Statement mapFactoryWithExpiringGuavaCacheDemonstrationCodeForUseInJavadoc;

            public org.junit.runners.model.Statement groupByEvictionCancellationOfSource5933;

            public org.junit.runners.model.Statement cancellationOfUpstreamWhenGroupedFlowableCompletes;

            public org.junit.runners.model.Statement cancelOverFlatmapRace;

            public org.junit.runners.model.Statement abandonedGroupsNoDataloss;

            public org.junit.runners.model.Statement newGroupValueSelectorFails;

            public org.junit.runners.model.Statement existingGroupValueSelectorFails;

            public org.junit.runners.model.Statement fusedParallelGroupProcessing;

            public org.junit.runners.model.Statement valueSelectorCrashAndMissingBackpressure;

            public org.junit.runners.model.Statement fusedGroupClearedOnCancel;

            public org.junit.runners.model.Statement fusedGroupClearedOnCancelDelayed;

            public org.junit.runners.model.Statement cancelledGroupResumesRequesting;

            public org.junit.runners.model.Statement delayErrorCompleteMoreWorkInGroup;

            public org.junit.runners.model.Statement groupSyncFusionRejected;

            public org.junit.runners.model.Statement subscribeAbandonRace;

            public org.junit.runners.model.Statement issue6974;

            public org.junit.runners.model.Statement issue6974Part2Case1;

            public org.junit.runners.model.Statement issue6974Part2Case2;

            public org.junit.runners.model.Statement issue6974Part2Case1NoEvict;

            public org.junit.runners.model.Statement issue6974Part2Case1ObserveOn;

            public org.junit.runners.model.Statement issue6974Part2Case1ObserveOnHide;

            public org.junit.runners.model.Statement issue6974Part2Case1ObserveOnNoCap;

            public org.junit.runners.model.Statement issue6974Part2Case1ObserveOnNoCapHide;

            public org.junit.runners.model.Statement issue6974Part2Case1ObserveOnConditional;

            public org.junit.runners.model.Statement issue6974Part2Case1ObserveOnConditionalHide;

            public org.junit.runners.model.Statement issue6982Case1;

            public org.junit.runners.model.Statement issue6982Case2;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.groupBy = _ClassStatement.forPayload(FlowableGroupByTest::groupBy, "groupBy", this);
            this.payloads.groupByWithElementSelector = _ClassStatement.forPayload(FlowableGroupByTest::groupByWithElementSelector, "groupByWithElementSelector", this);
            this.payloads.groupByWithElementSelector2 = _ClassStatement.forPayload(FlowableGroupByTest::groupByWithElementSelector2, "groupByWithElementSelector2", this);
            this.payloads.empty = _ClassStatement.forPayload(FlowableGroupByTest::empty, "empty", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableGroupByTest::error, "error", this);
            this.payloads.groupedEventStream = _ClassStatement.forPayload(FlowableGroupByTest::groupedEventStream, "groupedEventStream", this);
            this.payloads.unsubscribeOnNestedTakeAndSyncInfiniteStream = _ClassStatement.forPayload(FlowableGroupByTest::unsubscribeOnNestedTakeAndSyncInfiniteStream, "unsubscribeOnNestedTakeAndSyncInfiniteStream", this);
            this.payloads.unsubscribeOnNestedTakeAndAsyncInfiniteStream = _ClassStatement.forPayload(FlowableGroupByTest::unsubscribeOnNestedTakeAndAsyncInfiniteStream, "unsubscribeOnNestedTakeAndAsyncInfiniteStream", this);
            this.payloads.unsubscribeViaTakeOnGroupThenMergeAndTake = _ClassStatement.forPayload(FlowableGroupByTest::unsubscribeViaTakeOnGroupThenMergeAndTake, "unsubscribeViaTakeOnGroupThenMergeAndTake", this);
            this.payloads.unsubscribeViaTakeOnGroupThenTakeOnInner = _ClassStatement.forPayload(FlowableGroupByTest::unsubscribeViaTakeOnGroupThenTakeOnInner, "unsubscribeViaTakeOnGroupThenTakeOnInner", this);
            this.payloads.staggeredCompletion = _ClassStatement.forPayload(FlowableGroupByTest::staggeredCompletion, "staggeredCompletion", this);
            this.payloads.completionIfInnerNotSubscribed = _ClassStatement.forPayload(FlowableGroupByTest::completionIfInnerNotSubscribed, "completionIfInnerNotSubscribed", this);
            this.payloads.ignoringGroups = _ClassStatement.forPayload(FlowableGroupByTest::ignoringGroups, "ignoringGroups", this);
            this.payloads.firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsAndThenComplete = _ClassStatement.forPayload(FlowableGroupByTest::firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsAndThenComplete, "firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsAndThenComplete", this);
            this.payloads.firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsWhichThenSubscribesOnAndDelaysAndThenCompletes = _ClassStatement.forPayload(FlowableGroupByTest::firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsWhichThenSubscribesOnAndDelaysAndThenCompletes, "firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsWhichThenSubscribesOnAndDelaysAndThenCompletes", this);
            this.payloads.firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsWhichThenObservesOnAndDelaysAndThenCompletes = _ClassStatement.forPayload(FlowableGroupByTest::firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsWhichThenObservesOnAndDelaysAndThenCompletes, "firstGroupsCompleteAndParentSlowToThenEmitFinalGroupsWhichThenObservesOnAndDelaysAndThenCompletes", this);
            this.payloads.groupsWithNestedSubscribeOn = _ClassStatement.forPayload(FlowableGroupByTest::groupsWithNestedSubscribeOn, "groupsWithNestedSubscribeOn", this);
            this.payloads.groupsWithNestedObserveOn = _ClassStatement.forPayload(FlowableGroupByTest::groupsWithNestedObserveOn, "groupsWithNestedObserveOn", this);
            this.payloads.groupByOnAsynchronousSourceAcceptsMultipleSubscriptions = _ClassStatement.forPayload(FlowableGroupByTest::groupByOnAsynchronousSourceAcceptsMultipleSubscriptions, "groupByOnAsynchronousSourceAcceptsMultipleSubscriptions", this);
            this.payloads.groupByBackpressure = _ClassStatement.forPayload(FlowableGroupByTest::groupByBackpressure, "groupByBackpressure", this);
            this.payloads.normalBehavior = _ClassStatement.forPayload(FlowableGroupByTest::normalBehavior, "normalBehavior", this);
            this.payloads.keySelectorThrows = _ClassStatement.forPayload(FlowableGroupByTest::keySelectorThrows, "keySelectorThrows", this);
            this.payloads.valueSelectorThrows = _ClassStatement.forPayload(FlowableGroupByTest::valueSelectorThrows, "valueSelectorThrows", this);
            this.payloads.innerEscapeCompleted = _ClassStatement.forPayload(FlowableGroupByTest::innerEscapeCompleted, "innerEscapeCompleted", this);
            this.payloads.exceptionIfSubscribeToChildMoreThanOnce = _ClassStatement.forPayload(FlowableGroupByTest::exceptionIfSubscribeToChildMoreThanOnce, "exceptionIfSubscribeToChildMoreThanOnce", this);
            this.payloads.error2 = _ClassStatement.forPayload(FlowableGroupByTest::error2, "error2", this);
            this.payloads.groupByBackpressure3 = _ClassStatement.forPayload(FlowableGroupByTest::groupByBackpressure3, "groupByBackpressure3", this);
            this.payloads.groupByBackpressure2 = _ClassStatement.forPayload(FlowableGroupByTest::groupByBackpressure2, "groupByBackpressure2", this);
            this.payloads.groupByWithNullKey = _ClassStatement.forPayload(FlowableGroupByTest::groupByWithNullKey, "groupByWithNullKey", this);
            this.payloads.groupByUnsubscribe = _ClassStatement.forPayload(FlowableGroupByTest::groupByUnsubscribe, "groupByUnsubscribe", this);
            this.payloads.groupByShouldPropagateError = _ClassStatement.forPayload(FlowableGroupByTest::groupByShouldPropagateError, "groupByShouldPropagateError", this);
            this.payloads.requestOverflow = _ClassStatement.forPayload(FlowableGroupByTest::requestOverflow, "requestOverflow", this);
            this.payloads.backpressureObserveOnOuter = _ClassStatement.forPayload(FlowableGroupByTest::backpressureObserveOnOuter, "backpressureObserveOnOuter", this);
            this.payloads.backpressureObserveOnOuterMissingBackpressure = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableGroupByTest::backpressureObserveOnOuterMissingBackpressure, io.reactivex.rxjava3.exceptions.MissingBackpressureException.class), "backpressureObserveOnOuterMissingBackpressure", this);
            this.payloads.backpressureInnerDoesntOverflowOuter = _ClassStatement.forPayload(FlowableGroupByTest::backpressureInnerDoesntOverflowOuter, "backpressureInnerDoesntOverflowOuter", this);
            this.payloads.backpressureInnerDoesntOverflowOuterMissingBackpressure = _ClassStatement.forPayload(FlowableGroupByTest::backpressureInnerDoesntOverflowOuterMissingBackpressure, "backpressureInnerDoesntOverflowOuterMissingBackpressure", this);
            this.payloads.oneGroupInnerRequestsTwiceBuffer = _ClassStatement.forPayload(FlowableGroupByTest::oneGroupInnerRequestsTwiceBuffer, "oneGroupInnerRequestsTwiceBuffer", this);
            this.payloads.outerInnerFusion = _ClassStatement.forPayload(FlowableGroupByTest::outerInnerFusion, "outerInnerFusion", this);
            this.payloads.keySelectorAndDelayError = _ClassStatement.forPayload(FlowableGroupByTest::keySelectorAndDelayError, "keySelectorAndDelayError", this);
            this.payloads.keyAndValueSelectorAndDelayError = _ClassStatement.forPayload(FlowableGroupByTest::keyAndValueSelectorAndDelayError, "keyAndValueSelectorAndDelayError", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableGroupByTest::dispose, "dispose", this);
            this.payloads.reentrantComplete = _ClassStatement.forPayload(FlowableGroupByTest::reentrantComplete, "reentrantComplete", this);
            this.payloads.reentrantCompleteCancel = _ClassStatement.forPayload(FlowableGroupByTest::reentrantCompleteCancel, "reentrantCompleteCancel", this);
            this.payloads.delayErrorSimpleComplete = _ClassStatement.forPayload(FlowableGroupByTest::delayErrorSimpleComplete, "delayErrorSimpleComplete", this);
            this.payloads.mainFusionRejected = _ClassStatement.forPayload(FlowableGroupByTest::mainFusionRejected, "mainFusionRejected", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableGroupByTest::badSource, "badSource", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableGroupByTest::badRequest, "badRequest", this);
            this.payloads.badRequestInner = _ClassStatement.forPayload(FlowableGroupByTest::badRequestInner, "badRequestInner", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableGroupByTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.nullKeyTakeInner = _ClassStatement.forPayload(FlowableGroupByTest::nullKeyTakeInner, "nullKeyTakeInner", this);
            this.payloads.groupError = _ClassStatement.forPayload(FlowableGroupByTest::groupError, "groupError", this);
            this.payloads.groupComplete = _ClassStatement.forPayload(FlowableGroupByTest::groupComplete, "groupComplete", this);
            this.payloads.mapFactoryThrows = _ClassStatement.forPayload(FlowableGroupByTest::mapFactoryThrows, "mapFactoryThrows", this);
            this.payloads.mapFactoryExpiryCompletesGroupedFlowable = _ClassStatement.forPayload(FlowableGroupByTest::mapFactoryExpiryCompletesGroupedFlowable, "mapFactoryExpiryCompletesGroupedFlowable", this);
            this.payloads.mapFactoryEvictionQueueClearedOnErrorCoverageOnly = _ClassStatement.forPayload(FlowableGroupByTest::mapFactoryEvictionQueueClearedOnErrorCoverageOnly, "mapFactoryEvictionQueueClearedOnErrorCoverageOnly", this);
            this.payloads.mapFactoryWithExpiringGuavaCacheDemonstrationCodeForUseInJavadoc = _ClassStatement.forPayload(FlowableGroupByTest::mapFactoryWithExpiringGuavaCacheDemonstrationCodeForUseInJavadoc, "mapFactoryWithExpiringGuavaCacheDemonstrationCodeForUseInJavadoc", this);
            this.payloads.groupByEvictionCancellationOfSource5933 = _ClassStatement.forPayload(FlowableGroupByTest::groupByEvictionCancellationOfSource5933, "groupByEvictionCancellationOfSource5933", this);
            this.payloads.cancellationOfUpstreamWhenGroupedFlowableCompletes = _ClassStatement.forPayload(FlowableGroupByTest::cancellationOfUpstreamWhenGroupedFlowableCompletes, "cancellationOfUpstreamWhenGroupedFlowableCompletes", this);
            this.payloads.cancelOverFlatmapRace = _ClassStatement.forPayload(FlowableGroupByTest::cancelOverFlatmapRace, "cancelOverFlatmapRace", this);
            this.payloads.abandonedGroupsNoDataloss = _ClassStatement.forPayload(FlowableGroupByTest::abandonedGroupsNoDataloss, "abandonedGroupsNoDataloss", this);
            this.payloads.newGroupValueSelectorFails = _ClassStatement.forPayload(FlowableGroupByTest::newGroupValueSelectorFails, "newGroupValueSelectorFails", this);
            this.payloads.existingGroupValueSelectorFails = _ClassStatement.forPayload(FlowableGroupByTest::existingGroupValueSelectorFails, "existingGroupValueSelectorFails", this);
            this.payloads.fusedParallelGroupProcessing = _ClassStatement.forPayload(FlowableGroupByTest::fusedParallelGroupProcessing, "fusedParallelGroupProcessing", this);
            this.payloads.valueSelectorCrashAndMissingBackpressure = _ClassStatement.forPayload(FlowableGroupByTest::valueSelectorCrashAndMissingBackpressure, "valueSelectorCrashAndMissingBackpressure", this);
            this.payloads.fusedGroupClearedOnCancel = _ClassStatement.forPayload(FlowableGroupByTest::fusedGroupClearedOnCancel, "fusedGroupClearedOnCancel", this);
            this.payloads.fusedGroupClearedOnCancelDelayed = _ClassStatement.forPayload(FlowableGroupByTest::fusedGroupClearedOnCancelDelayed, "fusedGroupClearedOnCancelDelayed", this);
            this.payloads.cancelledGroupResumesRequesting = _ClassStatement.forPayload(FlowableGroupByTest::cancelledGroupResumesRequesting, "cancelledGroupResumesRequesting", this);
            this.payloads.delayErrorCompleteMoreWorkInGroup = _ClassStatement.forPayload(FlowableGroupByTest::delayErrorCompleteMoreWorkInGroup, "delayErrorCompleteMoreWorkInGroup", this);
            this.payloads.groupSyncFusionRejected = _ClassStatement.forPayload(FlowableGroupByTest::groupSyncFusionRejected, "groupSyncFusionRejected", this);
            this.payloads.subscribeAbandonRace = _ClassStatement.forPayload(FlowableGroupByTest::subscribeAbandonRace, "subscribeAbandonRace", this);
            this.payloads.issue6974 = _ClassStatement.forPayload(FlowableGroupByTest::issue6974, "issue6974", this);
            this.payloads.issue6974Part2Case1 = _ClassStatement.forPayload(FlowableGroupByTest::issue6974Part2Case1, "issue6974Part2Case1", this);
            this.payloads.issue6974Part2Case2 = _ClassStatement.forPayload(FlowableGroupByTest::issue6974Part2Case2, "issue6974Part2Case2", this);
            this.payloads.issue6974Part2Case1NoEvict = _ClassStatement.forPayload(FlowableGroupByTest::issue6974Part2Case1NoEvict, "issue6974Part2Case1NoEvict", this);
            this.payloads.issue6974Part2Case1ObserveOn = _ClassStatement.forPayload(FlowableGroupByTest::issue6974Part2Case1ObserveOn, "issue6974Part2Case1ObserveOn", this);
            this.payloads.issue6974Part2Case1ObserveOnHide = _ClassStatement.forPayload(FlowableGroupByTest::issue6974Part2Case1ObserveOnHide, "issue6974Part2Case1ObserveOnHide", this);
            this.payloads.issue6974Part2Case1ObserveOnNoCap = _ClassStatement.forPayload(FlowableGroupByTest::issue6974Part2Case1ObserveOnNoCap, "issue6974Part2Case1ObserveOnNoCap", this);
            this.payloads.issue6974Part2Case1ObserveOnNoCapHide = _ClassStatement.forPayload(FlowableGroupByTest::issue6974Part2Case1ObserveOnNoCapHide, "issue6974Part2Case1ObserveOnNoCapHide", this);
            this.payloads.issue6974Part2Case1ObserveOnConditional = _ClassStatement.forPayload(FlowableGroupByTest::issue6974Part2Case1ObserveOnConditional, "issue6974Part2Case1ObserveOnConditional", this);
            this.payloads.issue6974Part2Case1ObserveOnConditionalHide = _ClassStatement.forPayload(FlowableGroupByTest::issue6974Part2Case1ObserveOnConditionalHide, "issue6974Part2Case1ObserveOnConditionalHide", this);
            this.payloads.issue6982Case1 = _ClassStatement.forPayload(FlowableGroupByTest::issue6982Case1, "issue6982Case1", this);
            this.payloads.issue6982Case2 = _ClassStatement.forPayload(FlowableGroupByTest::issue6982Case2, "issue6982Case2", this);
        }
    }
    /*
     * Disabled: Takes very long. Run it locally only.
    @Test
    public void issue6982Case2Loop() {
        for (int i = 0; i < 200; i++) {
            // System.out.println("issue6982Case2Loop "  + i);
            issue6982Case2();
        }
    }
     */
}
