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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableWindowWithSizeTest extends RxJavaTest {

    private static <T> List<List<T>> toLists(Flowable<Flowable<T>> observables) {
        return observables.flatMapSingle(new Function<Flowable<T>, SingleSource<List<T>>>() {

            @Override
            public SingleSource<List<T>> apply(Flowable<T> w) throws Throwable {
                return w.toList();
            }
        }).toList().blockingGet();
    }

    @Test
    public void nonOverlappingWindows() {
        Flowable<String> subject = Flowable.just("one", "two", "three", "four", "five");
        Flowable<Flowable<String>> windowed = subject.window(3);
        List<List<String>> windows = toLists(windowed);
        assertEquals(2, windows.size());
        assertEquals(list("one", "two", "three"), windows.get(0));
        assertEquals(list("four", "five"), windows.get(1));
    }

    @Test
    public void skipAndCountGaplessWindows() {
        Flowable<String> subject = Flowable.just("one", "two", "three", "four", "five");
        Flowable<Flowable<String>> windowed = subject.window(3, 3);
        List<List<String>> windows = toLists(windowed);
        assertEquals(2, windows.size());
        assertEquals(list("one", "two", "three"), windows.get(0));
        assertEquals(list("four", "five"), windows.get(1));
    }

    @Test
    public void overlappingWindows() {
        Flowable<String> subject = Flowable.fromArray(new String[] { "zero", "one", "two", "three", "four", "five" });
        Flowable<Flowable<String>> windowed = subject.window(3, 1);
        List<List<String>> windows = toLists(windowed);
        assertEquals(6, windows.size());
        assertEquals(list("zero", "one", "two"), windows.get(0));
        assertEquals(list("one", "two", "three"), windows.get(1));
        assertEquals(list("two", "three", "four"), windows.get(2));
        assertEquals(list("three", "four", "five"), windows.get(3));
        assertEquals(list("four", "five"), windows.get(4));
        assertEquals(list("five"), windows.get(5));
    }

    @Test
    public void skipAndCountWindowsWithGaps() {
        Flowable<String> subject = Flowable.just("one", "two", "three", "four", "five");
        Flowable<Flowable<String>> windowed = subject.window(2, 3);
        List<List<String>> windows = toLists(windowed);
        assertEquals(2, windows.size());
        assertEquals(list("one", "two"), windows.get(0));
        assertEquals(list("four", "five"), windows.get(1));
    }

    @Test
    public void windowUnsubscribeNonOverlapping() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        final AtomicInteger count = new AtomicInteger();
        Flowable.merge(Flowable.range(1, 10000).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                count.incrementAndGet();
            }
        }).window(5).take(2)).subscribe(ts);
        ts.awaitDone(500, TimeUnit.MILLISECONDS);
        ts.assertTerminated();
        ts.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        // // System.out.println(ts.getOnNextEvents());
        assertEquals(10, count.get());
    }

    @Test
    public void windowUnsubscribeNonOverlappingAsyncSource() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        final AtomicInteger count = new AtomicInteger();
        Flowable.merge(Flowable.range(1, 100000).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                count.incrementAndGet();
            }
        }).observeOn(Schedulers.computation()).window(5).take(2)).subscribe(ts);
        ts.awaitDone(500, TimeUnit.MILLISECONDS);
        ts.assertTerminated();
        ts.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        // make sure we don't emit all values ... the unsubscribe should propagate
        assertTrue(count.get() < 100000);
    }

    @Test
    public void windowUnsubscribeOverlapping() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        final AtomicInteger count = new AtomicInteger();
        Flowable.merge(Flowable.range(1, 10000).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                count.incrementAndGet();
            }
        }).window(5, 4).take(2)).subscribe(ts);
        ts.awaitDone(500, TimeUnit.MILLISECONDS);
        ts.assertTerminated();
        // // System.out.println(ts.getOnNextEvents());
        ts.assertValues(1, 2, 3, 4, 5, 5, 6, 7, 8, 9);
        assertEquals(9, count.get());
    }

    @Test
    public void windowUnsubscribeOverlappingAsyncSource() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        final AtomicInteger count = new AtomicInteger();
        Flowable.merge(Flowable.range(1, 100000).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                count.incrementAndGet();
            }
        }).observeOn(Schedulers.computation()).window(5, 4).take(2), 128).subscribe(ts);
        ts.awaitDone(500, TimeUnit.MILLISECONDS);
        ts.assertTerminated();
        ts.assertValues(1, 2, 3, 4, 5, 5, 6, 7, 8, 9);
    // make sure we don't emit all values ... the unsubscribe should propagate
    // assertTrue(count.get() < 100000); // disabled: a small hiccup in the consumption may allow the source to run to completion
    }

    private List<String> list(String... args) {
        List<String> list = new ArrayList<>();
        for (String arg : args) {
            list.add(arg);
        }
        return list;
    }

    @Test
    public void backpressureOuter() {
        Flowable<Flowable<Integer>> source = Flowable.range(1, 10).window(3);
        final List<Integer> list = new ArrayList<>();
        final Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        source.subscribe(new DefaultSubscriber<Flowable<Integer>>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onNext(Flowable<Integer> t) {
                t.subscribe(new DefaultSubscriber<Integer>() {

                    @Override
                    public void onNext(Integer t) {
                        list.add(t);
                    }

                    @Override
                    public void onError(Throwable e) {
                        subscriber.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        subscriber.onComplete();
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }
        });
        assertEquals(Arrays.asList(1, 2, 3), list);
        verify(subscriber, never()).onError(any(Throwable.class));
        // 1 inner
        verify(subscriber, times(1)).onComplete();
    }

    public static Flowable<Integer> hotStream() {
        return Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                BooleanSubscription bs = new BooleanSubscription();
                s.onSubscribe(bs);
                while (!bs.isCancelled()) {
                    // burst some number of items
                    for (int i = 0; i < Math.random() * 20; i++) {
                        s.onNext(i);
                    }
                    try {
                        // sleep for a random amount of time
                        // NOTE: Only using Thread.sleep here as an artificial demo.
                        Thread.sleep((long) (Math.random() * 200));
                    } catch (Exception e) {
                    // do nothing
                    }
                }
                // System.out.println("Hot done.");
            }
        }).subscribeOn(// use newThread since we are using sleep to block
        Schedulers.newThread());
    }

    @Test
    public void takeFlatMapCompletes() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        final int indicator = 999999999;
        hotStream().window(10).take(2).flatMap(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> w) {
                return w.startWithItem(indicator);
            }
        }).subscribe(ts);
        ts.awaitDone(2, TimeUnit.SECONDS);
        ts.assertComplete();
        ts.assertValueCount(22);
    }

    @Test
    public void backpressureOuterInexact() {
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>(0L);
        Flowable.range(1, 5).window(2, 1).map(new Function<Flowable<Integer>, Flowable<List<Integer>>>() {

            @Override
            public Flowable<List<Integer>> apply(Flowable<Integer> t) {
                return t.toList().toFlowable();
            }
        }).concatMapEager(new Function<Flowable<List<Integer>>, Publisher<List<Integer>>>() {

            @Override
            public Publisher<List<Integer>> apply(Flowable<List<Integer>> v) {
                return v;
            }
        }).subscribe(ts);
        ts.assertNoErrors();
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.request(2);
        ts.assertValues(Arrays.asList(1, 2), Arrays.asList(2, 3));
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(5);
        // System.out.println(ts.values());
        ts.assertValues(Arrays.asList(1, 2), Arrays.asList(2, 3), Arrays.asList(3, 4), Arrays.asList(4, 5), Arrays.asList(5));
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().window(1));
        TestHelper.checkDisposed(PublishProcessor.create().window(2, 1));
        TestHelper.checkDisposed(PublishProcessor.create().window(1, 2));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Flowable<Object>>>() {

            @Override
            public Flowable<Flowable<Object>> apply(Flowable<Object> f) throws Exception {
                return f.window(1);
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Flowable<Object>>>() {

            @Override
            public Flowable<Flowable<Object>> apply(Flowable<Object> f) throws Exception {
                return f.window(2, 1);
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Flowable<Object>>>() {

            @Override
            public Flowable<Flowable<Object>> apply(Flowable<Object> f) throws Exception {
                return f.window(1, 2);
            }
        });
    }

    @Test
    public void errorExact() {
        Flowable.error(new TestException()).window(1).test().assertFailure(TestException.class);
    }

    @Test
    public void errorSkip() {
        Flowable.error(new TestException()).window(1, 2).test().assertFailure(TestException.class);
    }

    @Test
    public void errorOverlap() {
        Flowable.error(new TestException()).window(2, 1).test().assertFailure(TestException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void errorExactInner() {
        @SuppressWarnings("rawtypes")
        final TestSubscriber[] to = { null };
        Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).window(2).doOnNext(new Consumer<Flowable<Integer>>() {

            @Override
            public void accept(Flowable<Integer> w) throws Exception {
                to[0] = w.test();
            }
        }).test().assertError(TestException.class);
        to[0].assertFailure(TestException.class, 1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void errorSkipInner() {
        @SuppressWarnings("rawtypes")
        final TestSubscriber[] to = { null };
        Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).window(2, 3).doOnNext(new Consumer<Flowable<Integer>>() {

            @Override
            public void accept(Flowable<Integer> w) throws Exception {
                to[0] = w.test();
            }
        }).test().assertError(TestException.class);
        to[0].assertFailure(TestException.class, 1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void errorOverlapInner() {
        @SuppressWarnings("rawtypes")
        final TestSubscriber[] to = { null };
        Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).window(3, 2).doOnNext(new Consumer<Flowable<Integer>>() {

            @Override
            public void accept(Flowable<Integer> w) throws Exception {
                to[0] = w.test();
            }
        }).test().assertError(TestException.class);
        to[0].assertFailure(TestException.class, 1);
    }

    @Test
    public void cancellingWindowCancelsUpstreamSize() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.window(10).take(1).flatMap(new Function<Flowable<Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Integer> w) throws Throwable {
                return w.take(1);
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertResult(1);
        assertFalse("Processor still has subscribers!", pp.hasSubscribers());
    }

    @Test
    public void windowAbandonmentCancelsUpstreamSize() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final AtomicReference<Flowable<Integer>> inner = new AtomicReference<>();
        TestSubscriber<Flowable<Integer>> ts = pp.window(10).take(1).doOnNext(new Consumer<Flowable<Integer>>() {

            @Override
            public void accept(Flowable<Integer> v) throws Throwable {
                inner.set(v);
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertValueCount(1).assertNoErrors().assertComplete();
        assertFalse("Processor still has subscribers!", pp.hasSubscribers());
        inner.get().test().assertResult(1);
    }

    @Test
    public void cancellingWindowCancelsUpstreamSkip() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.window(5, 10).take(1).flatMap(new Function<Flowable<Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Integer> w) throws Throwable {
                return w.take(1);
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertResult(1);
        assertFalse("Processor still has subscribers!", pp.hasSubscribers());
    }

    @Test
    public void windowAbandonmentCancelsUpstreamSkip() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final AtomicReference<Flowable<Integer>> inner = new AtomicReference<>();
        TestSubscriber<Flowable<Integer>> ts = pp.window(5, 10).take(1).doOnNext(new Consumer<Flowable<Integer>>() {

            @Override
            public void accept(Flowable<Integer> v) throws Throwable {
                inner.set(v);
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertValueCount(1).assertNoErrors().assertComplete();
        assertFalse("Processor still has subscribers!", pp.hasSubscribers());
        inner.get().test().assertResult(1);
    }

    @Test
    public void cancellingWindowCancelsUpstreamOverlap() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.window(5, 3).take(1).flatMap(new Function<Flowable<Integer>, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Flowable<Integer> w) throws Throwable {
                return w.take(1);
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertResult(1);
        assertFalse("Processor still has subscribers!", pp.hasSubscribers());
    }

    @Test
    public void windowAbandonmentCancelsUpstreamOverlap() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final AtomicReference<Flowable<Integer>> inner = new AtomicReference<>();
        TestSubscriber<Flowable<Integer>> ts = pp.window(5, 3).take(1).doOnNext(new Consumer<Flowable<Integer>>() {

            @Override
            public void accept(Flowable<Integer> v) throws Throwable {
                inner.set(v);
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertValueCount(1).assertNoErrors().assertComplete();
        assertFalse("Processor still has subscribers!", pp.hasSubscribers());
        inner.get().test().assertResult(1);
    }

    @Test
    public void badRequestExact() {
        TestHelper.assertBadRequestReported(Flowable.never().window(1));
    }

    @Test
    public void badRequestSkip() {
        TestHelper.assertBadRequestReported(Flowable.never().window(1, 2));
    }

    @Test
    public void badRequestOverlap() {
        TestHelper.assertBadRequestReported(Flowable.never().window(2, 1));
    }

    @Test
    public void skipEmpty() {
        Flowable.empty().window(1, 2).test().assertResult();
    }

    @Test
    public void exactEmpty() {
        Flowable.empty().window(2).test().assertResult();
    }

    @Test
    public void skipMultipleRequests() {
        Flowable.range(1, 10).window(1, 2).doOnNext(w -> w.test()).rebatchRequests(1).test().assertComplete();
    }

    @Test
    public void skipOne() {
        Flowable.just(1).window(2, 3).flatMap(v -> v).test().assertResult(1);
    }

    @Test
    public void overlapMultipleRequests() {
        Flowable.range(1, 10).window(2, 1).doOnNext(w -> w.test()).rebatchRequests(1).test().assertComplete();
    }

    @Test
    public void overlapCancelAfterWindow() {
        Flowable.range(1, 10).window(2, 1).takeUntil(v -> true).doOnNext(w -> w.test()).test(0L).requestMore(10).assertComplete();
    }

    @Test
    public void overlapEmpty() {
        Flowable.empty().window(2, 1).test().assertResult();
    }

    @Test
    public void overlapEmptyNoRequest() {
        Flowable.empty().window(2, 1).test(0L).assertResult();
    }

    @Test
    public void overlapMoreWorkAfterOnNext() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        AtomicBoolean once = new AtomicBoolean();
        TestSubscriber<Flowable<Integer>> ts = pp.window(2, 1).doOnNext(v -> {
            v.test();
            if (once.compareAndSet(false, true)) {
                pp.onNext(2);
                pp.onComplete();
            }
        }).test();
        pp.onNext(1);
        ts.assertComplete();
    }

    @Test
    public void moreQueuedClean() {
        Flowable.range(1, 10).window(5, 1).doOnNext(w -> w.test()).test(3).cancel();
    }

    @Test
    public void cancelWithoutWindowSize() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Flowable<Integer>> ts = pp.window(10).test();
        assertTrue(pp.hasSubscribers());
        ts.cancel();
        assertFalse("Subject still has subscribers!", pp.hasSubscribers());
    }

    @Test
    public void cancelAfterAbandonmentSize() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Flowable<Integer>> ts = pp.window(10).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.cancel();
        assertFalse("Subject still has subscribers!", pp.hasSubscribers());
    }

    @Test
    public void cancelWithoutWindowSkip() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Flowable<Integer>> ts = pp.window(10, 15).test();
        assertTrue(pp.hasSubscribers());
        ts.cancel();
        assertFalse("Subject still has subscribers!", pp.hasSubscribers());
    }

    @Test
    public void cancelAfterAbandonmentSkip() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Flowable<Integer>> ts = pp.window(10, 15).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.cancel();
        assertFalse("Subject still has subscribers!", pp.hasSubscribers());
    }

    @Test
    public void cancelWithoutWindowOverlap() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Flowable<Integer>> ts = pp.window(10, 5).test();
        assertTrue(pp.hasSubscribers());
        ts.cancel();
        assertFalse("Subject still has subscribers!", pp.hasSubscribers());
    }

    @Test
    public void cancelAfterAbandonmentOverlap() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Flowable<Integer>> ts = pp.window(10, 5).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.cancel();
        assertFalse("Subject still has subscribers!", pp.hasSubscribers());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableWindowWithSizeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonOverlappingWindows() throws java.lang.Throwable {
            this.payloads.nonOverlappingWindows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipAndCountGaplessWindows() throws java.lang.Throwable {
            this.payloads.skipAndCountGaplessWindows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overlappingWindows() throws java.lang.Throwable {
            this.payloads.overlappingWindows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipAndCountWindowsWithGaps() throws java.lang.Throwable {
            this.payloads.skipAndCountWindowsWithGaps.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowUnsubscribeNonOverlapping() throws java.lang.Throwable {
            this.payloads.windowUnsubscribeNonOverlapping.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowUnsubscribeNonOverlappingAsyncSource() throws java.lang.Throwable {
            this.payloads.windowUnsubscribeNonOverlappingAsyncSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowUnsubscribeOverlapping() throws java.lang.Throwable {
            this.payloads.windowUnsubscribeOverlapping.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowUnsubscribeOverlappingAsyncSource() throws java.lang.Throwable {
            this.payloads.windowUnsubscribeOverlappingAsyncSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureOuter() throws java.lang.Throwable {
            this.payloads.backpressureOuter.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeFlatMapCompletes() throws java.lang.Throwable {
            this.payloads.takeFlatMapCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureOuterInexact() throws java.lang.Throwable {
            this.payloads.backpressureOuterInexact.evaluate();
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
        public void benchmark_errorExact() throws java.lang.Throwable {
            this.payloads.errorExact.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSkip() throws java.lang.Throwable {
            this.payloads.errorSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorOverlap() throws java.lang.Throwable {
            this.payloads.errorOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorExactInner() throws java.lang.Throwable {
            this.payloads.errorExactInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSkipInner() throws java.lang.Throwable {
            this.payloads.errorSkipInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorOverlapInner() throws java.lang.Throwable {
            this.payloads.errorOverlapInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellingWindowCancelsUpstreamSize() throws java.lang.Throwable {
            this.payloads.cancellingWindowCancelsUpstreamSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowAbandonmentCancelsUpstreamSize() throws java.lang.Throwable {
            this.payloads.windowAbandonmentCancelsUpstreamSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellingWindowCancelsUpstreamSkip() throws java.lang.Throwable {
            this.payloads.cancellingWindowCancelsUpstreamSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowAbandonmentCancelsUpstreamSkip() throws java.lang.Throwable {
            this.payloads.windowAbandonmentCancelsUpstreamSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellingWindowCancelsUpstreamOverlap() throws java.lang.Throwable {
            this.payloads.cancellingWindowCancelsUpstreamOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowAbandonmentCancelsUpstreamOverlap() throws java.lang.Throwable {
            this.payloads.windowAbandonmentCancelsUpstreamOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequestExact() throws java.lang.Throwable {
            this.payloads.badRequestExact.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequestSkip() throws java.lang.Throwable {
            this.payloads.badRequestSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequestOverlap() throws java.lang.Throwable {
            this.payloads.badRequestOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipEmpty() throws java.lang.Throwable {
            this.payloads.skipEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exactEmpty() throws java.lang.Throwable {
            this.payloads.exactEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipMultipleRequests() throws java.lang.Throwable {
            this.payloads.skipMultipleRequests.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipOne() throws java.lang.Throwable {
            this.payloads.skipOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overlapMultipleRequests() throws java.lang.Throwable {
            this.payloads.overlapMultipleRequests.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overlapCancelAfterWindow() throws java.lang.Throwable {
            this.payloads.overlapCancelAfterWindow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overlapEmpty() throws java.lang.Throwable {
            this.payloads.overlapEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overlapEmptyNoRequest() throws java.lang.Throwable {
            this.payloads.overlapEmptyNoRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overlapMoreWorkAfterOnNext() throws java.lang.Throwable {
            this.payloads.overlapMoreWorkAfterOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_moreQueuedClean() throws java.lang.Throwable {
            this.payloads.moreQueuedClean.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWithoutWindowSize() throws java.lang.Throwable {
            this.payloads.cancelWithoutWindowSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterAbandonmentSize() throws java.lang.Throwable {
            this.payloads.cancelAfterAbandonmentSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWithoutWindowSkip() throws java.lang.Throwable {
            this.payloads.cancelWithoutWindowSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterAbandonmentSkip() throws java.lang.Throwable {
            this.payloads.cancelAfterAbandonmentSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWithoutWindowOverlap() throws java.lang.Throwable {
            this.payloads.cancelWithoutWindowOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterAbandonmentOverlap() throws java.lang.Throwable {
            this.payloads.cancelAfterAbandonmentOverlap.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithSizeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithSizeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithSizeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithSizeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableWindowWithSizeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowWithSizeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableWindowWithSizeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableWindowWithSizeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement nonOverlappingWindows;

            public org.junit.runners.model.Statement skipAndCountGaplessWindows;

            public org.junit.runners.model.Statement overlappingWindows;

            public org.junit.runners.model.Statement skipAndCountWindowsWithGaps;

            public org.junit.runners.model.Statement windowUnsubscribeNonOverlapping;

            public org.junit.runners.model.Statement windowUnsubscribeNonOverlappingAsyncSource;

            public org.junit.runners.model.Statement windowUnsubscribeOverlapping;

            public org.junit.runners.model.Statement windowUnsubscribeOverlappingAsyncSource;

            public org.junit.runners.model.Statement backpressureOuter;

            public org.junit.runners.model.Statement takeFlatMapCompletes;

            public org.junit.runners.model.Statement backpressureOuterInexact;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement errorExact;

            public org.junit.runners.model.Statement errorSkip;

            public org.junit.runners.model.Statement errorOverlap;

            public org.junit.runners.model.Statement errorExactInner;

            public org.junit.runners.model.Statement errorSkipInner;

            public org.junit.runners.model.Statement errorOverlapInner;

            public org.junit.runners.model.Statement cancellingWindowCancelsUpstreamSize;

            public org.junit.runners.model.Statement windowAbandonmentCancelsUpstreamSize;

            public org.junit.runners.model.Statement cancellingWindowCancelsUpstreamSkip;

            public org.junit.runners.model.Statement windowAbandonmentCancelsUpstreamSkip;

            public org.junit.runners.model.Statement cancellingWindowCancelsUpstreamOverlap;

            public org.junit.runners.model.Statement windowAbandonmentCancelsUpstreamOverlap;

            public org.junit.runners.model.Statement badRequestExact;

            public org.junit.runners.model.Statement badRequestSkip;

            public org.junit.runners.model.Statement badRequestOverlap;

            public org.junit.runners.model.Statement skipEmpty;

            public org.junit.runners.model.Statement exactEmpty;

            public org.junit.runners.model.Statement skipMultipleRequests;

            public org.junit.runners.model.Statement skipOne;

            public org.junit.runners.model.Statement overlapMultipleRequests;

            public org.junit.runners.model.Statement overlapCancelAfterWindow;

            public org.junit.runners.model.Statement overlapEmpty;

            public org.junit.runners.model.Statement overlapEmptyNoRequest;

            public org.junit.runners.model.Statement overlapMoreWorkAfterOnNext;

            public org.junit.runners.model.Statement moreQueuedClean;

            public org.junit.runners.model.Statement cancelWithoutWindowSize;

            public org.junit.runners.model.Statement cancelAfterAbandonmentSize;

            public org.junit.runners.model.Statement cancelWithoutWindowSkip;

            public org.junit.runners.model.Statement cancelAfterAbandonmentSkip;

            public org.junit.runners.model.Statement cancelWithoutWindowOverlap;

            public org.junit.runners.model.Statement cancelAfterAbandonmentOverlap;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.nonOverlappingWindows = _ClassStatement.forPayload(FlowableWindowWithSizeTest::nonOverlappingWindows, "nonOverlappingWindows", this);
            this.payloads.skipAndCountGaplessWindows = _ClassStatement.forPayload(FlowableWindowWithSizeTest::skipAndCountGaplessWindows, "skipAndCountGaplessWindows", this);
            this.payloads.overlappingWindows = _ClassStatement.forPayload(FlowableWindowWithSizeTest::overlappingWindows, "overlappingWindows", this);
            this.payloads.skipAndCountWindowsWithGaps = _ClassStatement.forPayload(FlowableWindowWithSizeTest::skipAndCountWindowsWithGaps, "skipAndCountWindowsWithGaps", this);
            this.payloads.windowUnsubscribeNonOverlapping = _ClassStatement.forPayload(FlowableWindowWithSizeTest::windowUnsubscribeNonOverlapping, "windowUnsubscribeNonOverlapping", this);
            this.payloads.windowUnsubscribeNonOverlappingAsyncSource = _ClassStatement.forPayload(FlowableWindowWithSizeTest::windowUnsubscribeNonOverlappingAsyncSource, "windowUnsubscribeNonOverlappingAsyncSource", this);
            this.payloads.windowUnsubscribeOverlapping = _ClassStatement.forPayload(FlowableWindowWithSizeTest::windowUnsubscribeOverlapping, "windowUnsubscribeOverlapping", this);
            this.payloads.windowUnsubscribeOverlappingAsyncSource = _ClassStatement.forPayload(FlowableWindowWithSizeTest::windowUnsubscribeOverlappingAsyncSource, "windowUnsubscribeOverlappingAsyncSource", this);
            this.payloads.backpressureOuter = _ClassStatement.forPayload(FlowableWindowWithSizeTest::backpressureOuter, "backpressureOuter", this);
            this.payloads.takeFlatMapCompletes = _ClassStatement.forPayload(FlowableWindowWithSizeTest::takeFlatMapCompletes, "takeFlatMapCompletes", this);
            this.payloads.backpressureOuterInexact = _ClassStatement.forPayload(FlowableWindowWithSizeTest::backpressureOuterInexact, "backpressureOuterInexact", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableWindowWithSizeTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableWindowWithSizeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.errorExact = _ClassStatement.forPayload(FlowableWindowWithSizeTest::errorExact, "errorExact", this);
            this.payloads.errorSkip = _ClassStatement.forPayload(FlowableWindowWithSizeTest::errorSkip, "errorSkip", this);
            this.payloads.errorOverlap = _ClassStatement.forPayload(FlowableWindowWithSizeTest::errorOverlap, "errorOverlap", this);
            this.payloads.errorExactInner = _ClassStatement.forPayload(FlowableWindowWithSizeTest::errorExactInner, "errorExactInner", this);
            this.payloads.errorSkipInner = _ClassStatement.forPayload(FlowableWindowWithSizeTest::errorSkipInner, "errorSkipInner", this);
            this.payloads.errorOverlapInner = _ClassStatement.forPayload(FlowableWindowWithSizeTest::errorOverlapInner, "errorOverlapInner", this);
            this.payloads.cancellingWindowCancelsUpstreamSize = _ClassStatement.forPayload(FlowableWindowWithSizeTest::cancellingWindowCancelsUpstreamSize, "cancellingWindowCancelsUpstreamSize", this);
            this.payloads.windowAbandonmentCancelsUpstreamSize = _ClassStatement.forPayload(FlowableWindowWithSizeTest::windowAbandonmentCancelsUpstreamSize, "windowAbandonmentCancelsUpstreamSize", this);
            this.payloads.cancellingWindowCancelsUpstreamSkip = _ClassStatement.forPayload(FlowableWindowWithSizeTest::cancellingWindowCancelsUpstreamSkip, "cancellingWindowCancelsUpstreamSkip", this);
            this.payloads.windowAbandonmentCancelsUpstreamSkip = _ClassStatement.forPayload(FlowableWindowWithSizeTest::windowAbandonmentCancelsUpstreamSkip, "windowAbandonmentCancelsUpstreamSkip", this);
            this.payloads.cancellingWindowCancelsUpstreamOverlap = _ClassStatement.forPayload(FlowableWindowWithSizeTest::cancellingWindowCancelsUpstreamOverlap, "cancellingWindowCancelsUpstreamOverlap", this);
            this.payloads.windowAbandonmentCancelsUpstreamOverlap = _ClassStatement.forPayload(FlowableWindowWithSizeTest::windowAbandonmentCancelsUpstreamOverlap, "windowAbandonmentCancelsUpstreamOverlap", this);
            this.payloads.badRequestExact = _ClassStatement.forPayload(FlowableWindowWithSizeTest::badRequestExact, "badRequestExact", this);
            this.payloads.badRequestSkip = _ClassStatement.forPayload(FlowableWindowWithSizeTest::badRequestSkip, "badRequestSkip", this);
            this.payloads.badRequestOverlap = _ClassStatement.forPayload(FlowableWindowWithSizeTest::badRequestOverlap, "badRequestOverlap", this);
            this.payloads.skipEmpty = _ClassStatement.forPayload(FlowableWindowWithSizeTest::skipEmpty, "skipEmpty", this);
            this.payloads.exactEmpty = _ClassStatement.forPayload(FlowableWindowWithSizeTest::exactEmpty, "exactEmpty", this);
            this.payloads.skipMultipleRequests = _ClassStatement.forPayload(FlowableWindowWithSizeTest::skipMultipleRequests, "skipMultipleRequests", this);
            this.payloads.skipOne = _ClassStatement.forPayload(FlowableWindowWithSizeTest::skipOne, "skipOne", this);
            this.payloads.overlapMultipleRequests = _ClassStatement.forPayload(FlowableWindowWithSizeTest::overlapMultipleRequests, "overlapMultipleRequests", this);
            this.payloads.overlapCancelAfterWindow = _ClassStatement.forPayload(FlowableWindowWithSizeTest::overlapCancelAfterWindow, "overlapCancelAfterWindow", this);
            this.payloads.overlapEmpty = _ClassStatement.forPayload(FlowableWindowWithSizeTest::overlapEmpty, "overlapEmpty", this);
            this.payloads.overlapEmptyNoRequest = _ClassStatement.forPayload(FlowableWindowWithSizeTest::overlapEmptyNoRequest, "overlapEmptyNoRequest", this);
            this.payloads.overlapMoreWorkAfterOnNext = _ClassStatement.forPayload(FlowableWindowWithSizeTest::overlapMoreWorkAfterOnNext, "overlapMoreWorkAfterOnNext", this);
            this.payloads.moreQueuedClean = _ClassStatement.forPayload(FlowableWindowWithSizeTest::moreQueuedClean, "moreQueuedClean", this);
            this.payloads.cancelWithoutWindowSize = _ClassStatement.forPayload(FlowableWindowWithSizeTest::cancelWithoutWindowSize, "cancelWithoutWindowSize", this);
            this.payloads.cancelAfterAbandonmentSize = _ClassStatement.forPayload(FlowableWindowWithSizeTest::cancelAfterAbandonmentSize, "cancelAfterAbandonmentSize", this);
            this.payloads.cancelWithoutWindowSkip = _ClassStatement.forPayload(FlowableWindowWithSizeTest::cancelWithoutWindowSkip, "cancelWithoutWindowSkip", this);
            this.payloads.cancelAfterAbandonmentSkip = _ClassStatement.forPayload(FlowableWindowWithSizeTest::cancelAfterAbandonmentSkip, "cancelAfterAbandonmentSkip", this);
            this.payloads.cancelWithoutWindowOverlap = _ClassStatement.forPayload(FlowableWindowWithSizeTest::cancelWithoutWindowOverlap, "cancelWithoutWindowOverlap", this);
            this.payloads.cancelAfterAbandonmentOverlap = _ClassStatement.forPayload(FlowableWindowWithSizeTest::cancelAfterAbandonmentOverlap, "cancelAfterAbandonmentOverlap", this);
        }
    }
}
