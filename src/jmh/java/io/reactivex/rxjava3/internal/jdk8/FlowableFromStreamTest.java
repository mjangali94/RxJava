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
package io.reactivex.rxjava3.internal.jdk8;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.QueueSubscription;
import io.reactivex.rxjava3.operators.SimpleQueue;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableFromStreamTest extends RxJavaTest {

    @Test
    public void empty() {
        Flowable.fromStream(Stream.<Integer>of()).test().assertResult();
    }

    @Test
    public void just() {
        Flowable.fromStream(Stream.<Integer>of(1)).test().assertResult(1);
    }

    @Test
    public void many() {
        Flowable.fromStream(Stream.<Integer>of(1, 2, 3, 4, 5)).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void manyBackpressured() {
        Flowable.fromStream(Stream.<Integer>of(1, 2, 3, 4, 5)).test(0L).assertEmpty().requestMore(1).assertValuesOnly(1).requestMore(2).assertValuesOnly(1, 2, 3).requestMore(2).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void noReuse() {
        Flowable<Integer> source = Flowable.fromStream(Stream.<Integer>of(1, 2, 3, 4, 5));
        source.test().assertResult(1, 2, 3, 4, 5);
        source.test().assertFailure(IllegalStateException.class);
    }

    @Test
    public void take() {
        Flowable.fromStream(IntStream.rangeClosed(1, 10).boxed()).take(5).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void emptyConditional() {
        Flowable.fromStream(Stream.<Integer>of()).filter(v -> true).test().assertResult();
    }

    @Test
    public void justConditional() {
        Flowable.fromStream(Stream.<Integer>of(1)).filter(v -> true).test().assertResult(1);
    }

    @Test
    public void manyConditional() {
        Flowable.fromStream(Stream.<Integer>of(1, 2, 3, 4, 5)).filter(v -> true).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void manyBackpressuredConditional() {
        Flowable.fromStream(Stream.<Integer>of(1, 2, 3, 4, 5)).filter(v -> true).test(0L).assertEmpty().requestMore(1).assertValuesOnly(1).requestMore(2).assertValuesOnly(1, 2, 3).requestMore(2).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void manyConditionalSkip() {
        Flowable.fromStream(IntStream.rangeClosed(1, 10).boxed()).filter(v -> v % 2 == 0).test().assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void takeConditional() {
        Flowable.fromStream(IntStream.rangeClosed(1, 10).boxed()).filter(v -> true).take(5).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void noOfferNoCrashAfterClear() throws Throwable {
        AtomicReference<SimpleQueue<?>> queue = new AtomicReference<>();
        Flowable.fromStream(IntStream.rangeClosed(1, 10).boxed()).subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onSubscribe(@NonNull Subscription s) {
                queue.set((SimpleQueue<?>) s);
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
        TestHelper.assertNoOffer(q);
        assertFalse(q.isEmpty());
        q.clear();
        assertNull(q.poll());
        assertTrue(q.isEmpty());
        q.clear();
        assertNull(q.poll());
        assertTrue(q.isEmpty());
    }

    @Test
    public void fusedPoll() throws Throwable {
        AtomicReference<SimpleQueue<?>> queue = new AtomicReference<>();
        AtomicInteger calls = new AtomicInteger();
        Flowable.fromStream(Stream.of(1).onClose(() -> calls.getAndIncrement())).subscribe(new FlowableSubscriber<Integer>() {

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
        assertEquals(1, calls.get());
    }

    @Test
    public void streamOfNull() {
        Flowable.fromStream(Stream.of((Integer) null)).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void streamOfNullConditional() {
        Flowable.fromStream(Stream.of((Integer) null)).filter(v -> true).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void syncFusionSupport() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ANY);
        Flowable.fromStream(IntStream.rangeClosed(1, 10).boxed()).subscribeWith(ts).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void asyncFusionNotSupported() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ASYNC);
        Flowable.fromStream(IntStream.rangeClosed(1, 10).boxed()).subscribeWith(ts).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void fusedForParallel() {
        Flowable.fromStream(IntStream.rangeClosed(1, 1000).boxed()).parallel().runOn(Schedulers.computation(), 1).map(v -> v + 1).sequential().test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(1000).assertNoErrors().assertComplete();
    }

    @Test
    public void runToEndCloseCrash() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5).onClose(() -> {
                throw new TestException();
            });
            Flowable.fromStream(stream).test().assertResult(1, 2, 3, 4, 5);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void takeCloseCrash() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5).onClose(() -> {
                throw new TestException();
            });
            Flowable.fromStream(stream).take(3).test().assertResult(1, 2, 3);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void hasNextCrash() {
        AtomicInteger v = new AtomicInteger();
        Flowable.fromStream(Stream.<Integer>generate(() -> {
            int value = v.getAndIncrement();
            if (value == 1) {
                throw new TestException();
            }
            return value;
        })).test().assertFailure(TestException.class, 0);
    }

    @Test
    public void hasNextCrashConditional() {
        AtomicInteger counter = new AtomicInteger();
        Flowable.fromStream(Stream.<Integer>generate(() -> {
            int value = counter.getAndIncrement();
            if (value == 1) {
                throw new TestException();
            }
            return value;
        })).filter(v -> true).test().assertFailure(TestException.class, 0);
    }

    void requestOneByOneBase(boolean conditional) {
        List<Object> list = new ArrayList<>();
        Flowable<Integer> source = Flowable.fromStream(IntStream.rangeClosed(1, 10).boxed());
        if (conditional) {
            source = source.filter(v -> true);
        }
        source.subscribe(new FlowableSubscriber<Integer>() {

            @NonNull
            Subscription upstream;

            @Override
            public void onSubscribe(@NonNull Subscription s) {
                this.upstream = s;
                s.request(1);
            }

            @Override
            public void onNext(Integer t) {
                list.add(t);
                upstream.request(1);
            }

            @Override
            public void onError(Throwable t) {
                list.add(t);
            }

            @Override
            public void onComplete() {
                list.add(100);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 100), list);
    }

    @Test
    public void requestOneByOne() {
        requestOneByOneBase(false);
    }

    @Test
    public void requestOneByOneConditional() {
        requestOneByOneBase(true);
    }

    void requestRaceBase(boolean conditional) throws Exception {
        ExecutorService exec = Executors.newCachedThreadPool();
        try {
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                AtomicInteger counter = new AtomicInteger();
                int max = 100;
                Flowable<Integer> source = Flowable.fromStream(IntStream.rangeClosed(1, max).boxed());
                if (conditional) {
                    source = source.filter(v -> true);
                }
                CountDownLatch cdl = new CountDownLatch(1);
                source.subscribe(new FlowableSubscriber<Integer>() {

                    @NonNull
                    Subscription upstream;

                    @Override
                    public void onSubscribe(@NonNull Subscription s) {
                        this.upstream = s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(Integer t) {
                        counter.getAndIncrement();
                        AtomicInteger sync = new AtomicInteger(2);
                        exec.submit(() -> {
                            if (sync.decrementAndGet() != 0) {
                                while (sync.get() != 0) {
                                }
                            }
                            upstream.request(1);
                        });
                        if (sync.decrementAndGet() != 0) {
                            while (sync.get() != 0) {
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                        cdl.countDown();
                    }

                    @Override
                    public void onComplete() {
                        counter.getAndIncrement();
                        cdl.countDown();
                    }
                });
                assertTrue(cdl.await(60, TimeUnit.SECONDS));
                assertEquals(max + 1, counter.get());
            }
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void requestRace() throws Exception {
        requestRaceBase(false);
    }

    @Test
    public void requestRaceConditional() throws Exception {
        requestRaceBase(true);
    }

    @Test
    public void closeCalledOnEmpty() {
        AtomicInteger calls = new AtomicInteger();
        Flowable.fromStream(Stream.of().onClose(() -> calls.getAndIncrement())).test().assertResult();
        assertEquals(1, calls.get());
    }

    @Test
    public void closeCalledAfterItems() {
        AtomicInteger calls = new AtomicInteger();
        Flowable.fromStream(Stream.of(1, 2, 3, 4, 5).onClose(() -> calls.getAndIncrement())).test().assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls.get());
    }

    @Test
    public void closeCalledOnCancel() {
        AtomicInteger calls = new AtomicInteger();
        Flowable.fromStream(Stream.of(1, 2, 3, 4, 5).onClose(() -> calls.getAndIncrement())).take(3).test().assertResult(1, 2, 3);
        assertEquals(1, calls.get());
    }

    @Test
    public void closeCalledOnItemCrash() {
        AtomicInteger calls = new AtomicInteger();
        AtomicInteger counter = new AtomicInteger();
        Flowable.fromStream(Stream.<Integer>generate(() -> {
            int value = counter.getAndIncrement();
            if (value == 1) {
                throw new TestException();
            }
            return value;
        }).onClose(() -> calls.getAndIncrement())).test().assertFailure(TestException.class, 0);
        assertEquals(1, calls.get());
    }

    @Test
    public void closeCalledAfterItemsConditional() {
        AtomicInteger calls = new AtomicInteger();
        Flowable.fromStream(Stream.of(1, 2, 3, 4, 5).onClose(() -> calls.getAndIncrement())).filter(v -> true).test().assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls.get());
    }

    @Test
    public void closeCalledOnCancelConditional() {
        AtomicInteger calls = new AtomicInteger();
        Flowable.fromStream(Stream.of(1, 2, 3, 4, 5).onClose(() -> calls.getAndIncrement())).filter(v -> true).take(3).test().assertResult(1, 2, 3);
        assertEquals(1, calls.get());
    }

    @Test
    public void closeCalledOnItemCrashConditional() {
        AtomicInteger calls = new AtomicInteger();
        AtomicInteger counter = new AtomicInteger();
        Flowable.fromStream(Stream.<Integer>generate(() -> {
            int value = counter.getAndIncrement();
            if (value == 1) {
                throw new TestException();
            }
            return value;
        }).onClose(() -> calls.getAndIncrement())).filter(v -> true).test().assertFailure(TestException.class, 0);
        assertEquals(1, calls.get());
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.fromStream(Stream.of(1)));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableFromStreamTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.payloads.just.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_many() throws java.lang.Throwable {
            this.payloads.many.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyBackpressured() throws java.lang.Throwable {
            this.payloads.manyBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noReuse() throws java.lang.Throwable {
            this.payloads.noReuse.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyConditional() throws java.lang.Throwable {
            this.payloads.emptyConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justConditional() throws java.lang.Throwable {
            this.payloads.justConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyConditional() throws java.lang.Throwable {
            this.payloads.manyConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyBackpressuredConditional() throws java.lang.Throwable {
            this.payloads.manyBackpressuredConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyConditionalSkip() throws java.lang.Throwable {
            this.payloads.manyConditionalSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeConditional() throws java.lang.Throwable {
            this.payloads.takeConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noOfferNoCrashAfterClear() throws java.lang.Throwable {
            this.payloads.noOfferNoCrashAfterClear.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPoll() throws java.lang.Throwable {
            this.payloads.fusedPoll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_streamOfNull() throws java.lang.Throwable {
            this.payloads.streamOfNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_streamOfNullConditional() throws java.lang.Throwable {
            this.payloads.streamOfNullConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusionSupport() throws java.lang.Throwable {
            this.payloads.syncFusionSupport.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusionNotSupported() throws java.lang.Throwable {
            this.payloads.asyncFusionNotSupported.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedForParallel() throws java.lang.Throwable {
            this.payloads.fusedForParallel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runToEndCloseCrash() throws java.lang.Throwable {
            this.payloads.runToEndCloseCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeCloseCrash() throws java.lang.Throwable {
            this.payloads.takeCloseCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCrash() throws java.lang.Throwable {
            this.payloads.hasNextCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCrashConditional() throws java.lang.Throwable {
            this.payloads.hasNextCrashConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestOneByOne() throws java.lang.Throwable {
            this.payloads.requestOneByOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestOneByOneConditional() throws java.lang.Throwable {
            this.payloads.requestOneByOneConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestRace() throws java.lang.Throwable {
            this.payloads.requestRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestRaceConditional() throws java.lang.Throwable {
            this.payloads.requestRaceConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeCalledOnEmpty() throws java.lang.Throwable {
            this.payloads.closeCalledOnEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeCalledAfterItems() throws java.lang.Throwable {
            this.payloads.closeCalledAfterItems.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeCalledOnCancel() throws java.lang.Throwable {
            this.payloads.closeCalledOnCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeCalledOnItemCrash() throws java.lang.Throwable {
            this.payloads.closeCalledOnItemCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeCalledAfterItemsConditional() throws java.lang.Throwable {
            this.payloads.closeCalledAfterItemsConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeCalledOnCancelConditional() throws java.lang.Throwable {
            this.payloads.closeCalledOnCancelConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeCalledOnItemCrashConditional() throws java.lang.Throwable {
            this.payloads.closeCalledOnItemCrashConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromStreamTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromStreamTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromStreamTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromStreamTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableFromStreamTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFromStreamTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableFromStreamTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableFromStreamTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement just;

            public org.junit.runners.model.Statement many;

            public org.junit.runners.model.Statement manyBackpressured;

            public org.junit.runners.model.Statement noReuse;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement emptyConditional;

            public org.junit.runners.model.Statement justConditional;

            public org.junit.runners.model.Statement manyConditional;

            public org.junit.runners.model.Statement manyBackpressuredConditional;

            public org.junit.runners.model.Statement manyConditionalSkip;

            public org.junit.runners.model.Statement takeConditional;

            public org.junit.runners.model.Statement noOfferNoCrashAfterClear;

            public org.junit.runners.model.Statement fusedPoll;

            public org.junit.runners.model.Statement streamOfNull;

            public org.junit.runners.model.Statement streamOfNullConditional;

            public org.junit.runners.model.Statement syncFusionSupport;

            public org.junit.runners.model.Statement asyncFusionNotSupported;

            public org.junit.runners.model.Statement fusedForParallel;

            public org.junit.runners.model.Statement runToEndCloseCrash;

            public org.junit.runners.model.Statement takeCloseCrash;

            public org.junit.runners.model.Statement hasNextCrash;

            public org.junit.runners.model.Statement hasNextCrashConditional;

            public org.junit.runners.model.Statement requestOneByOne;

            public org.junit.runners.model.Statement requestOneByOneConditional;

            public org.junit.runners.model.Statement requestRace;

            public org.junit.runners.model.Statement requestRaceConditional;

            public org.junit.runners.model.Statement closeCalledOnEmpty;

            public org.junit.runners.model.Statement closeCalledAfterItems;

            public org.junit.runners.model.Statement closeCalledOnCancel;

            public org.junit.runners.model.Statement closeCalledOnItemCrash;

            public org.junit.runners.model.Statement closeCalledAfterItemsConditional;

            public org.junit.runners.model.Statement closeCalledOnCancelConditional;

            public org.junit.runners.model.Statement closeCalledOnItemCrashConditional;

            public org.junit.runners.model.Statement badRequest;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.empty = _ClassStatement.forPayload(FlowableFromStreamTest::empty, "empty", this);
            this.payloads.just = _ClassStatement.forPayload(FlowableFromStreamTest::just, "just", this);
            this.payloads.many = _ClassStatement.forPayload(FlowableFromStreamTest::many, "many", this);
            this.payloads.manyBackpressured = _ClassStatement.forPayload(FlowableFromStreamTest::manyBackpressured, "manyBackpressured", this);
            this.payloads.noReuse = _ClassStatement.forPayload(FlowableFromStreamTest::noReuse, "noReuse", this);
            this.payloads.take = _ClassStatement.forPayload(FlowableFromStreamTest::take, "take", this);
            this.payloads.emptyConditional = _ClassStatement.forPayload(FlowableFromStreamTest::emptyConditional, "emptyConditional", this);
            this.payloads.justConditional = _ClassStatement.forPayload(FlowableFromStreamTest::justConditional, "justConditional", this);
            this.payloads.manyConditional = _ClassStatement.forPayload(FlowableFromStreamTest::manyConditional, "manyConditional", this);
            this.payloads.manyBackpressuredConditional = _ClassStatement.forPayload(FlowableFromStreamTest::manyBackpressuredConditional, "manyBackpressuredConditional", this);
            this.payloads.manyConditionalSkip = _ClassStatement.forPayload(FlowableFromStreamTest::manyConditionalSkip, "manyConditionalSkip", this);
            this.payloads.takeConditional = _ClassStatement.forPayload(FlowableFromStreamTest::takeConditional, "takeConditional", this);
            this.payloads.noOfferNoCrashAfterClear = _ClassStatement.forPayload(FlowableFromStreamTest::noOfferNoCrashAfterClear, "noOfferNoCrashAfterClear", this);
            this.payloads.fusedPoll = _ClassStatement.forPayload(FlowableFromStreamTest::fusedPoll, "fusedPoll", this);
            this.payloads.streamOfNull = _ClassStatement.forPayload(FlowableFromStreamTest::streamOfNull, "streamOfNull", this);
            this.payloads.streamOfNullConditional = _ClassStatement.forPayload(FlowableFromStreamTest::streamOfNullConditional, "streamOfNullConditional", this);
            this.payloads.syncFusionSupport = _ClassStatement.forPayload(FlowableFromStreamTest::syncFusionSupport, "syncFusionSupport", this);
            this.payloads.asyncFusionNotSupported = _ClassStatement.forPayload(FlowableFromStreamTest::asyncFusionNotSupported, "asyncFusionNotSupported", this);
            this.payloads.fusedForParallel = _ClassStatement.forPayload(FlowableFromStreamTest::fusedForParallel, "fusedForParallel", this);
            this.payloads.runToEndCloseCrash = _ClassStatement.forPayload(FlowableFromStreamTest::runToEndCloseCrash, "runToEndCloseCrash", this);
            this.payloads.takeCloseCrash = _ClassStatement.forPayload(FlowableFromStreamTest::takeCloseCrash, "takeCloseCrash", this);
            this.payloads.hasNextCrash = _ClassStatement.forPayload(FlowableFromStreamTest::hasNextCrash, "hasNextCrash", this);
            this.payloads.hasNextCrashConditional = _ClassStatement.forPayload(FlowableFromStreamTest::hasNextCrashConditional, "hasNextCrashConditional", this);
            this.payloads.requestOneByOne = _ClassStatement.forPayload(FlowableFromStreamTest::requestOneByOne, "requestOneByOne", this);
            this.payloads.requestOneByOneConditional = _ClassStatement.forPayload(FlowableFromStreamTest::requestOneByOneConditional, "requestOneByOneConditional", this);
            this.payloads.requestRace = _ClassStatement.forPayload(FlowableFromStreamTest::requestRace, "requestRace", this);
            this.payloads.requestRaceConditional = _ClassStatement.forPayload(FlowableFromStreamTest::requestRaceConditional, "requestRaceConditional", this);
            this.payloads.closeCalledOnEmpty = _ClassStatement.forPayload(FlowableFromStreamTest::closeCalledOnEmpty, "closeCalledOnEmpty", this);
            this.payloads.closeCalledAfterItems = _ClassStatement.forPayload(FlowableFromStreamTest::closeCalledAfterItems, "closeCalledAfterItems", this);
            this.payloads.closeCalledOnCancel = _ClassStatement.forPayload(FlowableFromStreamTest::closeCalledOnCancel, "closeCalledOnCancel", this);
            this.payloads.closeCalledOnItemCrash = _ClassStatement.forPayload(FlowableFromStreamTest::closeCalledOnItemCrash, "closeCalledOnItemCrash", this);
            this.payloads.closeCalledAfterItemsConditional = _ClassStatement.forPayload(FlowableFromStreamTest::closeCalledAfterItemsConditional, "closeCalledAfterItemsConditional", this);
            this.payloads.closeCalledOnCancelConditional = _ClassStatement.forPayload(FlowableFromStreamTest::closeCalledOnCancelConditional, "closeCalledOnCancelConditional", this);
            this.payloads.closeCalledOnItemCrashConditional = _ClassStatement.forPayload(FlowableFromStreamTest::closeCalledOnItemCrashConditional, "closeCalledOnItemCrashConditional", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableFromStreamTest::badRequest, "badRequest", this);
        }
    }
}
