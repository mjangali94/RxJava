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
package io.reactivex.rxjava3.flowable;

import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.junit.rules.TestName;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.*;

public class FlowableBackpressureTests extends RxJavaTest {

    static final class FirehoseNoBackpressure extends AtomicBoolean implements Subscription {

        private static final long serialVersionUID = -669931580197884015L;

        final Subscriber<? super Integer> downstream;

        final AtomicInteger counter;

        volatile boolean cancelled;

        private FirehoseNoBackpressure(AtomicInteger counter, Subscriber<? super Integer> s) {
            this.counter = counter;
            this.downstream = s;
        }

        @Override
        public void request(long n) {
            if (!SubscriptionHelper.validate(n)) {
                return;
            }
            if (compareAndSet(false, true)) {
                int i = 0;
                final Subscriber<? super Integer> a = downstream;
                final AtomicInteger c = counter;
                while (!cancelled) {
                    a.onNext(i++);
                    c.incrementAndGet();
                }
                // System.out.println("unsubscribed after: " + i);
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }

    @Rule
    public TestName testName = new TestName();

    @After
    public void doAfterTest() {
    // FIXME LATER
    // TestObstructionDetection.checkObstruction();
    }

    @Test
    public void observeOn() {
        int num = (int) (Flowable.bufferSize() * 2.1);
        AtomicInteger c = new AtomicInteger();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        incrementingIntegers(c).observeOn(Schedulers.computation()).take(num).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        // System.out.println("testObserveOn => Received: " + ts.values().size() + "  Emitted: " + c.get());
        assertEquals(num, ts.values().size());
        assertTrue(c.get() < Flowable.bufferSize() * 4);
    }

    @Test
    public void observeOnWithSlowConsumer() {
        int num = (int) (Flowable.bufferSize() * 0.2);
        AtomicInteger c = new AtomicInteger();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        incrementingIntegers(c).observeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer i) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return i;
            }
        }).take(num).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        // System.out.println("testObserveOnWithSlowConsumer => Received: " + ts.values().size() + "  Emitted: " + c.get());
        assertEquals(num, ts.values().size());
        assertTrue(c.get() < Flowable.bufferSize() * 2);
    }

    @Test
    public void mergeSync() {
        int num = (int) (Flowable.bufferSize() * 4.1);
        AtomicInteger c1 = new AtomicInteger();
        AtomicInteger c2 = new AtomicInteger();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable<Integer> merged = Flowable.merge(incrementingIntegers(c1), incrementingIntegers(c2));
        merged.take(num).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        // System.out.println("Expected: " + num + " got: " + ts.values().size());
        // System.out.println("testMergeSync => Received: " + ts.values().size() + "  Emitted: " + c1.get() + " / " + c2.get());
        assertEquals(num, ts.values().size());
        // either one can starve the other, but neither should be capable of doing more than 5 batches (taking 4.1)
        // TODO is it possible to make this deterministic rather than one possibly starving the other?
        // benjchristensen => In general I'd say it's not worth trying to make it so, as "fair" algoritms generally take a performance hit
        assertTrue(c1.get() < Flowable.bufferSize() * 5);
        assertTrue(c2.get() < Flowable.bufferSize() * 5);
    }

    @Test
    public void mergeAsync() {
        int num = (int) (Flowable.bufferSize() * 4.1);
        AtomicInteger c1 = new AtomicInteger();
        AtomicInteger c2 = new AtomicInteger();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable<Integer> merged = Flowable.merge(incrementingIntegers(c1).subscribeOn(Schedulers.computation()), incrementingIntegers(c2).subscribeOn(Schedulers.computation()));
        merged.take(num).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        // System.out.println("testMergeAsync => Received: " + ts.values().size() + "  Emitted: " + c1.get() + " / " + c2.get());
        assertEquals(num, ts.values().size());
        // either one can starve the other, but neither should be capable of doing more than 5 batches (taking 4.1)
        // TODO is it possible to make this deterministic rather than one possibly starving the other?
        // benjchristensen => In general I'd say it's not worth trying to make it so, as "fair" algoritms generally take a performance hit
        int max = Flowable.bufferSize() * 7;
        assertTrue("" + c1.get() + " >= " + max, c1.get() < max);
        assertTrue("" + c2.get() + " >= " + max, c2.get() < max);
    }

    @Test
    public void mergeAsyncThenObserveOnLoop() {
        for (int i = 0; i < 500; i++) {
            if (i % 10 == 0) {
                // System.out.println("testMergeAsyncThenObserveOnLoop >> " + i);
            }
            // Verify there is no MissingBackpressureException
            int num = (int) (Flowable.bufferSize() * 4.1);
            AtomicInteger c1 = new AtomicInteger();
            AtomicInteger c2 = new AtomicInteger();
            TestSubscriber<Integer> ts = new TestSubscriber<>();
            Flowable<Integer> merged = Flowable.merge(incrementingIntegers(c1).subscribeOn(Schedulers.computation()), incrementingIntegers(c2).subscribeOn(Schedulers.computation()));
            merged.observeOn(Schedulers.io()).take(num).subscribe(ts);
            ts.awaitDone(5, TimeUnit.SECONDS);
            ts.assertComplete();
            ts.assertNoErrors();
            // System.out.println("testMergeAsyncThenObserveOn => Received: " + ts.values().size() + "  Emitted: " + c1.get() + " / " + c2.get());
            assertEquals(num, ts.values().size());
        }
    }

    @Test
    public void mergeAsyncThenObserveOn() {
        int num = (int) (Flowable.bufferSize() * 4.1);
        AtomicInteger c1 = new AtomicInteger();
        AtomicInteger c2 = new AtomicInteger();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable<Integer> merged = Flowable.merge(incrementingIntegers(c1).subscribeOn(Schedulers.computation()), incrementingIntegers(c2).subscribeOn(Schedulers.computation()));
        merged.observeOn(Schedulers.newThread()).take(num).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        // System.out.println("testMergeAsyncThenObserveOn => Received: " + ts.values().size() + "  Emitted: " + c1.get() + " / " + c2.get());
        assertEquals(num, ts.values().size());
        // either one can starve the other, but neither should be capable of doing more than 5 batches (taking 4.1)
        // TODO is it possible to make this deterministic rather than one possibly starving the other?
        // benjchristensen => In general I'd say it's not worth trying to make it so, as "fair" algoritms generally take a performance hit
        // akarnokd => run this in a loop over 10k times and never saw values get as high as 7*SIZE, but since observeOn delays the unsubscription non-deterministically, the test will remain unreliable
        assertTrue(c1.get() < Flowable.bufferSize() * 7);
        assertTrue(c2.get() < Flowable.bufferSize() * 7);
    }

    @Test
    public void flatMapSync() {
        int num = (int) (Flowable.bufferSize() * 2.1);
        AtomicInteger c = new AtomicInteger();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        incrementingIntegers(c).flatMap(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer i) {
                return incrementingIntegers(new AtomicInteger()).take(10);
            }
        }).take(num).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        // System.out.println("testFlatMapSync => Received: " + ts.values().size() + "  Emitted: " + c.get());
        assertEquals(num, ts.values().size());
        // expect less than 1 buffer since the flatMap is emitting 10 each time, so it is num/10 that will be taken.
        assertTrue(c.get() < Flowable.bufferSize());
    }

    @Test
    public void zipSync() {
        int num = (int) (Flowable.bufferSize() * 4.1);
        AtomicInteger c1 = new AtomicInteger();
        AtomicInteger c2 = new AtomicInteger();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable<Integer> zipped = Flowable.zip(incrementingIntegers(c1), incrementingIntegers(c2), new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        });
        zipped.take(num).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        // System.out.println("testZipSync => Received: " + ts.values().size() + "  Emitted: " + c1.get() + " / " + c2.get());
        assertEquals(num, ts.values().size());
        assertTrue(c1.get() < Flowable.bufferSize() * 7);
        assertTrue(c2.get() < Flowable.bufferSize() * 7);
    }

    @Test
    public void zipAsync() {
        int num = (int) (Flowable.bufferSize() * 2.1);
        AtomicInteger c1 = new AtomicInteger();
        AtomicInteger c2 = new AtomicInteger();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable<Integer> zipped = Flowable.zip(incrementingIntegers(c1).subscribeOn(Schedulers.computation()), incrementingIntegers(c2).subscribeOn(Schedulers.computation()), new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        });
        zipped.take(num).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        // System.out.println("testZipAsync => Received: " + ts.values().size() + "  Emitted: " + c1.get() + " / " + c2.get());
        assertEquals(num, ts.values().size());
        int max = Flowable.bufferSize() * 5;
        assertTrue("" + c1.get() + " >= " + max, c1.get() < max);
        assertTrue("" + c2.get() + " >= " + max, c2.get() < max);
    }

    @Test
    public void subscribeOnScheduling() {
        // in a loop for repeating the concurrency in this to increase chance of failure
        for (int i = 0; i < 100; i++) {
            int num = (int) (Flowable.bufferSize() * 2.1);
            AtomicInteger c = new AtomicInteger();
            ConcurrentLinkedQueue<Thread> threads = new ConcurrentLinkedQueue<>();
            TestSubscriber<Integer> ts = new TestSubscriber<>();
            // observeOn is there to make it async and need backpressure
            incrementingIntegers(c, threads).subscribeOn(Schedulers.computation()).observeOn(Schedulers.computation()).take(num).subscribe(ts);
            ts.awaitDone(5, TimeUnit.SECONDS);
            ts.assertNoErrors();
            // System.out.println("testSubscribeOnScheduling => Received: " + ts.values().size() + "  Emitted: " + c.get());
            assertEquals(num, ts.values().size());
            assertTrue(c.get() < Flowable.bufferSize() * 4);
            Thread first = null;
            for (Thread t : threads) {
                // System.out.println("testSubscribeOnScheduling => thread: " + t);
                if (first == null) {
                    first = t;
                } else {
                    if (!first.equals(t)) {
                        fail("Expected to see the same thread");
                    }
                }
            }
            // System.out.println("testSubscribeOnScheduling => Number of batch requests seen: " + threads.size());
            assertTrue(threads.size() > 1);
            // System.out.println("-------------------------------------------------------------------------------------------");
        }
    }

    @Test
    public void takeFilterSkipChainAsync() {
        int num = (int) (Flowable.bufferSize() * 2.1);
        AtomicInteger c = new AtomicInteger();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        incrementingIntegers(c).observeOn(Schedulers.computation()).skip(10000).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer i) {
                return i > 11000;
            }
        }).take(num).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        // emit 10000 that are skipped
        // emit next 1000 that are filtered out
        // take num
        // so emitted is at least 10000+1000+num + extra for buffer size/threshold
        int expected = 10000 + 1000 + Flowable.bufferSize() * 3 + Flowable.bufferSize() / 2;
        // System.out.println("testTakeFilterSkipChain => Received: " + ts.values().size() + "  Emitted: " + c.get() + " Expected: " + expected);
        assertEquals(num, ts.values().size());
        assertTrue(c.get() < expected);
    }

    @Test
    public void userSubscriberUsingRequestSync() {
        AtomicInteger c = new AtomicInteger();
        final AtomicInteger totalReceived = new AtomicInteger();
        final AtomicInteger batches = new AtomicInteger();
        final AtomicInteger received = new AtomicInteger();
        incrementingIntegers(c).subscribe(new ResourceSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(100);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
                int total = totalReceived.incrementAndGet();
                received.incrementAndGet();
                if (total >= 2000) {
                    dispose();
                }
                if (received.get() == 100) {
                    batches.incrementAndGet();
                    request(100);
                    received.set(0);
                }
            }
        });
        // System.out.println("testUserSubscriberUsingRequestSync => Received: " + totalReceived.get() + "  Emitted: " + c.get() + " Request Batches: " + batches.get());
        assertEquals(2000, c.get());
        assertEquals(2000, totalReceived.get());
        assertEquals(20, batches.get());
    }

    @Test
    public void userSubscriberUsingRequestAsync() throws InterruptedException {
        AtomicInteger c = new AtomicInteger();
        final AtomicInteger totalReceived = new AtomicInteger();
        final AtomicInteger received = new AtomicInteger();
        final AtomicInteger batches = new AtomicInteger();
        final CountDownLatch latch = new CountDownLatch(1);
        incrementingIntegers(c).subscribeOn(Schedulers.newThread()).subscribe(new ResourceSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(100);
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }

            @Override
            public void onNext(Integer t) {
                int total = totalReceived.incrementAndGet();
                received.incrementAndGet();
                boolean done = false;
                if (total >= 2000) {
                    done = true;
                    dispose();
                }
                if (received.get() == 100) {
                    batches.incrementAndGet();
                    received.set(0);
                    if (!done) {
                        request(100);
                    }
                }
                if (done) {
                    latch.countDown();
                }
            }
        });
        latch.await();
        // System.out.println("testUserSubscriberUsingRequestAsync => Received: " + totalReceived.get() + "  Emitted: " + c.get() + " Request Batches: " + batches.get());
        assertEquals(2000, c.get());
        assertEquals(2000, totalReceived.get());
        assertEquals(20, batches.get());
    }

    @Test
    public void firehoseFailsAsExpected() {
        AtomicInteger c = new AtomicInteger();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        firehose(c).observeOn(Schedulers.computation()).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) {
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return v;
            }
        }).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        // System.out.println("testFirehoseFailsAsExpected => Received: " + ts.values().size() + "  Emitted: " + c.get());
        // FIXME it is possible slow is not slow enough or the main gets delayed and thus more than one source value is emitted.
        int vc = ts.values().size();
        assertTrue("10 < " + vc, vc <= 10);
        ts.assertError(MissingBackpressureException.class);
    }

    @Test
    public void firehoseFailsAsExpectedLoop() {
        for (int i = 0; i < 100; i++) {
            firehoseFailsAsExpected();
        }
    }

    @Test
    public void onBackpressureDrop() {
        long t = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            // stop the test if we are getting close to the timeout because slow machines
            // may not get through 100 iterations
            if (System.currentTimeMillis() - t > TimeUnit.SECONDS.toMillis(9)) {
                break;
            }
            // > 1 so that take doesn't prevent buffer overflow
            int num = (int) (Flowable.bufferSize() * 1.1);
            AtomicInteger c = new AtomicInteger();
            TestSubscriber<Integer> ts = new TestSubscriber<>();
            firehose(c).onBackpressureDrop().observeOn(Schedulers.computation()).map(SLOW_PASS_THRU).take(num).subscribe(ts);
            ts.awaitDone(5, TimeUnit.SECONDS);
            ts.assertNoErrors();
            List<Integer> onNextEvents = ts.values();
            assertEquals(num, onNextEvents.size());
            Integer lastEvent = onNextEvents.get(num - 1);
            // System.out.println("testOnBackpressureDrop => Received: " + onNextEvents.size() + "  Emitted: " + c.get() + " Last value: " + lastEvent);
            // it drop, so we should get some number far higher than what would have sequentially incremented
            assertTrue(num - 1 <= lastEvent.intValue());
        }
    }

    @Test
    public void onBackpressureDropWithAction() {
        for (int i = 0; i < 100; i++) {
            final AtomicInteger emitCount = new AtomicInteger();
            final AtomicInteger dropCount = new AtomicInteger();
            final AtomicInteger passCount = new AtomicInteger();
            // > 1 so that take doesn't prevent buffer overflow
            final int num = Flowable.bufferSize() * 3;
            TestSubscriber<Integer> ts = new TestSubscriber<>();
            firehose(emitCount).onBackpressureDrop(new Consumer<Integer>() {

                @Override
                public void accept(Integer v) {
                    dropCount.incrementAndGet();
                }
            }).doOnNext(new Consumer<Integer>() {

                @Override
                public void accept(Integer v) {
                    passCount.incrementAndGet();
                }
            }).observeOn(Schedulers.computation()).map(SLOW_PASS_THRU).take(num).subscribe(ts);
            ts.awaitDone(5, TimeUnit.SECONDS);
            ts.assertNoErrors();
            List<Integer> onNextEvents = ts.values();
            Integer lastEvent = onNextEvents.get(num - 1);
            // System.out.println(testName.getMethodName() + " => Received: " + onNextEvents.size() + " Passed: " + passCount.get() + " Dropped: " + dropCount.get() + "  Emitted: " + emitCount.get() + " Last value: " + lastEvent);
            assertEquals(num, onNextEvents.size());
            // in reality, num < passCount
            assertTrue(num <= passCount.get());
            // it drop, so we should get some number far higher than what would have sequentially incremented
            assertTrue(num - 1 <= lastEvent.intValue());
            assertTrue(0 < dropCount.get());
            assertEquals(emitCount.get(), passCount.get() + dropCount.get());
        }
    }

    @Test
    public void onBackpressureDropSynchronous() {
        for (int i = 0; i < 100; i++) {
            // > 1 so that take doesn't prevent buffer overflow
            int num = (int) (Flowable.bufferSize() * 1.1);
            AtomicInteger c = new AtomicInteger();
            TestSubscriber<Integer> ts = new TestSubscriber<>();
            firehose(c).onBackpressureDrop().map(SLOW_PASS_THRU).take(num).subscribe(ts);
            ts.awaitDone(5, TimeUnit.SECONDS);
            ts.assertNoErrors();
            List<Integer> onNextEvents = ts.values();
            assertEquals(num, onNextEvents.size());
            Integer lastEvent = onNextEvents.get(num - 1);
            // System.out.println("testOnBackpressureDrop => Received: " + onNextEvents.size() + "  Emitted: " + c.get() + " Last value: " + lastEvent);
            // it drop, so we should get some number far higher than what would have sequentially incremented
            assertTrue(num - 1 <= lastEvent.intValue());
        }
    }

    @Test
    public void onBackpressureDropSynchronousWithAction() {
        for (int i = 0; i < 100; i++) {
            final AtomicInteger dropCount = new AtomicInteger();
            // > 1 so that take doesn't prevent buffer overflow
            int num = (int) (Flowable.bufferSize() * 1.1);
            AtomicInteger c = new AtomicInteger();
            TestSubscriber<Integer> ts = new TestSubscriber<>();
            firehose(c).onBackpressureDrop(new Consumer<Integer>() {

                @Override
                public void accept(Integer j) {
                    dropCount.incrementAndGet();
                }
            }).map(SLOW_PASS_THRU).take(num).subscribe(ts);
            ts.awaitDone(5, TimeUnit.SECONDS);
            ts.assertNoErrors();
            List<Integer> onNextEvents = ts.values();
            assertEquals(num, onNextEvents.size());
            Integer lastEvent = onNextEvents.get(num - 1);
            // System.out.println("testOnBackpressureDrop => Received: " + onNextEvents.size() + " Dropped: " + dropCount.get() + "  Emitted: " + c.get() + " Last value: " + lastEvent);
            // it drop, so we should get some number far higher than what would have sequentially incremented
            assertTrue(num - 1 <= lastEvent.intValue());
            // no drop in synchronous mode
            assertEquals(0, dropCount.get());
            assertEquals(c.get(), onNextEvents.size());
        }
    }

    @Test
    public void onBackpressureBuffer() {
        // > 1 so that take doesn't prevent buffer overflow
        int num = (int) (Flowable.bufferSize() * 1.1);
        AtomicInteger c = new AtomicInteger();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        firehose(c).takeWhile(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 < 100000;
            }
        }).onBackpressureBuffer().observeOn(Schedulers.computation()).map(SLOW_PASS_THRU).take(num).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        // System.out.println("testOnBackpressureBuffer => Received: " + ts.values().size() + "  Emitted: " + c.get());
        assertEquals(num, ts.values().size());
        // it buffers, so we should get the right value sequentially
        assertEquals(num - 1, ts.values().get(num - 1).intValue());
    }

    /**
     * A synchronous Flowable that will emit incrementing integers as requested.
     *
     * @param counter the shared value to be incremented
     * @return the incrementing Flowable instance
     */
    private static Flowable<Integer> incrementingIntegers(final AtomicInteger counter) {
        return incrementingIntegers(counter, null);
    }

    private static Flowable<Integer> incrementingIntegers(final AtomicInteger counter, final ConcurrentLinkedQueue<Thread> threadsSeen) {
        return Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(final Subscriber<? super Integer> s) {
                s.onSubscribe(new Subscription() {

                    int i;

                    volatile boolean cancelled;

                    final AtomicLong requested = new AtomicLong();

                    @Override
                    public void request(long n) {
                        if (!SubscriptionHelper.validate(n)) {
                            return;
                        }
                        if (threadsSeen != null) {
                            threadsSeen.offer(Thread.currentThread());
                        }
                        long c = BackpressureHelper.add(requested, n);
                        if (c == 0) {
                            while (!cancelled) {
                                counter.incrementAndGet();
                                s.onNext(i++);
                                if (requested.decrementAndGet() == 0) {
                                    // we're done emitting the number requested so return
                                    return;
                                }
                            }
                        }
                    }

                    @Override
                    public void cancel() {
                        cancelled = true;
                    }
                });
            }
        });
    }

    /**
     * Incrementing int without backpressure.
     *
     * @param counter the shared value to increment
     * @return the Flowable doing the increments
     */
    private static Flowable<Integer> firehose(final AtomicInteger counter) {
        return Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                Subscription s2 = new FirehoseNoBackpressure(counter, s);
                s.onSubscribe(s2);
            }
        });
    }

    static final Function<Integer, Integer> SLOW_PASS_THRU = new Function<Integer, Integer>() {

        volatile int sink;

        @Override
        public Integer apply(Integer t1) {
            // be slow ... but faster than Thread.sleep(1)
            String t = "";
            int s = sink;
            for (int i = 2000; i >= 0; i--) {
                t = String.valueOf(i + t.hashCode() + s);
            }
            sink = t.hashCode();
            return t1;
        }
    };

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableBackpressureTests instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOn() throws java.lang.Throwable {
            this.payloads.observeOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOnWithSlowConsumer() throws java.lang.Throwable {
            this.payloads.observeOnWithSlowConsumer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeSync() throws java.lang.Throwable {
            this.payloads.mergeSync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeAsync() throws java.lang.Throwable {
            this.payloads.mergeAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeAsyncThenObserveOnLoop() throws java.lang.Throwable {
            this.payloads.mergeAsyncThenObserveOnLoop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeAsyncThenObserveOn() throws java.lang.Throwable {
            this.payloads.mergeAsyncThenObserveOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapSync() throws java.lang.Throwable {
            this.payloads.flatMapSync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipSync() throws java.lang.Throwable {
            this.payloads.zipSync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipAsync() throws java.lang.Throwable {
            this.payloads.zipAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOnScheduling() throws java.lang.Throwable {
            this.payloads.subscribeOnScheduling.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeFilterSkipChainAsync() throws java.lang.Throwable {
            this.payloads.takeFilterSkipChainAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_userSubscriberUsingRequestSync() throws java.lang.Throwable {
            this.payloads.userSubscriberUsingRequestSync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_userSubscriberUsingRequestAsync() throws java.lang.Throwable {
            this.payloads.userSubscriberUsingRequestAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firehoseFailsAsExpected() throws java.lang.Throwable {
            this.payloads.firehoseFailsAsExpected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firehoseFailsAsExpectedLoop() throws java.lang.Throwable {
            this.payloads.firehoseFailsAsExpectedLoop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onBackpressureDrop() throws java.lang.Throwable {
            this.payloads.onBackpressureDrop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onBackpressureDropWithAction() throws java.lang.Throwable {
            this.payloads.onBackpressureDropWithAction.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onBackpressureDropSynchronous() throws java.lang.Throwable {
            this.payloads.onBackpressureDropSynchronous.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onBackpressureDropSynchronousWithAction() throws java.lang.Throwable {
            this.payloads.onBackpressureDropSynchronousWithAction.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onBackpressureBuffer() throws java.lang.Throwable {
            this.payloads.onBackpressureBuffer.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBackpressureTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBackpressureTests> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                try {
                    this.payload.accept(this.benchmark.instance);
                } finally {
                    this.benchmark.instance.doAfterTest();
                }
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBackpressureTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBackpressureTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableBackpressureTests();
                org.junit.runners.model.Statement statement = new _InstanceStatement(this.payload, this.benchmark);
                statement = this.applyRule(this.benchmark.instance.testName, statement);
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBackpressureTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableBackpressureTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableBackpressureTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement observeOn;

            public org.junit.runners.model.Statement observeOnWithSlowConsumer;

            public org.junit.runners.model.Statement mergeSync;

            public org.junit.runners.model.Statement mergeAsync;

            public org.junit.runners.model.Statement mergeAsyncThenObserveOnLoop;

            public org.junit.runners.model.Statement mergeAsyncThenObserveOn;

            public org.junit.runners.model.Statement flatMapSync;

            public org.junit.runners.model.Statement zipSync;

            public org.junit.runners.model.Statement zipAsync;

            public org.junit.runners.model.Statement subscribeOnScheduling;

            public org.junit.runners.model.Statement takeFilterSkipChainAsync;

            public org.junit.runners.model.Statement userSubscriberUsingRequestSync;

            public org.junit.runners.model.Statement userSubscriberUsingRequestAsync;

            public org.junit.runners.model.Statement firehoseFailsAsExpected;

            public org.junit.runners.model.Statement firehoseFailsAsExpectedLoop;

            public org.junit.runners.model.Statement onBackpressureDrop;

            public org.junit.runners.model.Statement onBackpressureDropWithAction;

            public org.junit.runners.model.Statement onBackpressureDropSynchronous;

            public org.junit.runners.model.Statement onBackpressureDropSynchronousWithAction;

            public org.junit.runners.model.Statement onBackpressureBuffer;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.observeOn = _ClassStatement.forPayload(FlowableBackpressureTests::observeOn, "observeOn", this);
            this.payloads.observeOnWithSlowConsumer = _ClassStatement.forPayload(FlowableBackpressureTests::observeOnWithSlowConsumer, "observeOnWithSlowConsumer", this);
            this.payloads.mergeSync = _ClassStatement.forPayload(FlowableBackpressureTests::mergeSync, "mergeSync", this);
            this.payloads.mergeAsync = _ClassStatement.forPayload(FlowableBackpressureTests::mergeAsync, "mergeAsync", this);
            this.payloads.mergeAsyncThenObserveOnLoop = _ClassStatement.forPayload(FlowableBackpressureTests::mergeAsyncThenObserveOnLoop, "mergeAsyncThenObserveOnLoop", this);
            this.payloads.mergeAsyncThenObserveOn = _ClassStatement.forPayload(FlowableBackpressureTests::mergeAsyncThenObserveOn, "mergeAsyncThenObserveOn", this);
            this.payloads.flatMapSync = _ClassStatement.forPayload(FlowableBackpressureTests::flatMapSync, "flatMapSync", this);
            this.payloads.zipSync = _ClassStatement.forPayload(FlowableBackpressureTests::zipSync, "zipSync", this);
            this.payloads.zipAsync = _ClassStatement.forPayload(FlowableBackpressureTests::zipAsync, "zipAsync", this);
            this.payloads.subscribeOnScheduling = _ClassStatement.forPayload(FlowableBackpressureTests::subscribeOnScheduling, "subscribeOnScheduling", this);
            this.payloads.takeFilterSkipChainAsync = _ClassStatement.forPayload(FlowableBackpressureTests::takeFilterSkipChainAsync, "takeFilterSkipChainAsync", this);
            this.payloads.userSubscriberUsingRequestSync = _ClassStatement.forPayload(FlowableBackpressureTests::userSubscriberUsingRequestSync, "userSubscriberUsingRequestSync", this);
            this.payloads.userSubscriberUsingRequestAsync = _ClassStatement.forPayload(FlowableBackpressureTests::userSubscriberUsingRequestAsync, "userSubscriberUsingRequestAsync", this);
            this.payloads.firehoseFailsAsExpected = _ClassStatement.forPayload(FlowableBackpressureTests::firehoseFailsAsExpected, "firehoseFailsAsExpected", this);
            this.payloads.firehoseFailsAsExpectedLoop = _ClassStatement.forPayload(FlowableBackpressureTests::firehoseFailsAsExpectedLoop, "firehoseFailsAsExpectedLoop", this);
            this.payloads.onBackpressureDrop = _ClassStatement.forPayload(FlowableBackpressureTests::onBackpressureDrop, "onBackpressureDrop", this);
            this.payloads.onBackpressureDropWithAction = _ClassStatement.forPayload(FlowableBackpressureTests::onBackpressureDropWithAction, "onBackpressureDropWithAction", this);
            this.payloads.onBackpressureDropSynchronous = _ClassStatement.forPayload(FlowableBackpressureTests::onBackpressureDropSynchronous, "onBackpressureDropSynchronous", this);
            this.payloads.onBackpressureDropSynchronousWithAction = _ClassStatement.forPayload(FlowableBackpressureTests::onBackpressureDropSynchronousWithAction, "onBackpressureDropSynchronousWithAction", this);
            this.payloads.onBackpressureBuffer = _ClassStatement.forPayload(FlowableBackpressureTests::onBackpressureBuffer, "onBackpressureBuffer", this);
        }
    }
}
