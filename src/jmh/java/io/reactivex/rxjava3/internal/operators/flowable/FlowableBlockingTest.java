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

import static org.junit.Assert.assertEquals;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableBlockingTest extends RxJavaTest {

    @Test
    public void blockingFirst() {
        assertEquals(1, Flowable.range(1, 10).subscribeOn(Schedulers.computation()).blockingFirst().intValue());
    }

    @Test
    public void blockingFirstDefault() {
        assertEquals(1, Flowable.<Integer>empty().subscribeOn(Schedulers.computation()).blockingFirst(1).intValue());
    }

    @Test
    public void blockingSubscribeConsumer() {
        final List<Integer> list = new ArrayList<>();
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumer() {
        final List<Integer> list = new ArrayList<>();
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        }, 128);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumerBufferExceed() {
        final List<Integer> list = new ArrayList<>();
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        }, 3);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void blockingSubscribeConsumerConsumer() {
        final List<Object> list = new ArrayList<>();
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        }, Functions.emptyConsumer());
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumerConsumer() {
        final List<Object> list = new ArrayList<>();
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        }, Functions.emptyConsumer(), 128);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumerConsumerBufferExceed() {
        final List<Object> list = new ArrayList<>();
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        }, Functions.emptyConsumer(), 3);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void blockingSubscribeConsumerConsumerError() {
        final List<Object> list = new ArrayList<>();
        TestException ex = new TestException();
        Consumer<Object> cons = new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add(v);
            }
        };
        Flowable.range(1, 5).concatWith(Flowable.<Integer>error(ex)).subscribeOn(Schedulers.computation()).blockingSubscribe(cons, cons);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, ex), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumerConsumerError() {
        final List<Object> list = new ArrayList<>();
        TestException ex = new TestException();
        Consumer<Object> cons = new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add(v);
            }
        };
        Flowable.range(1, 5).concatWith(Flowable.<Integer>error(ex)).subscribeOn(Schedulers.computation()).blockingSubscribe(cons, cons, 128);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, ex), list);
    }

    @Test
    public void blockingSubscribeConsumerConsumerAction() {
        final List<Object> list = new ArrayList<>();
        Consumer<Object> cons = new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add(v);
            }
        };
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(cons, cons, new Action() {

            @Override
            public void run() throws Exception {
                list.add(100);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 100), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumerConsumerAction() {
        final List<Object> list = new ArrayList<>();
        Consumer<Object> cons = new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add(v);
            }
        };
        Action action = new Action() {

            @Override
            public void run() throws Exception {
                list.add(100);
            }
        };
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(cons, cons, action, 128);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 100), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumerConsumerActionBufferExceed() {
        final List<Object> list = new ArrayList<>();
        Consumer<Object> cons = new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add(v);
            }
        };
        Action action = new Action() {

            @Override
            public void run() throws Exception {
                list.add(100);
            }
        };
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(cons, cons, action, 3);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 100), list);
    }

    @Test
    public void boundedBlockingSubscribeConsumerConsumerActionBufferExceedMillionItem() {
        final List<Object> list = new ArrayList<>();
        Consumer<Object> cons = new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add(v);
            }
        };
        Action action = new Action() {

            @Override
            public void run() throws Exception {
                list.add(1000001);
            }
        };
        Flowable.range(1, 1000000).subscribeOn(Schedulers.computation()).blockingSubscribe(cons, cons, action, 128);
        assertEquals(1000000 + 1, list.size());
    }

    @Test
    public void blockingSubscribeObserver() {
        final List<Object> list = new ArrayList<>();
        Flowable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new FlowableSubscriber<Object>() {

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Object value) {
                list.add(value);
            }

            @Override
            public void onError(Throwable e) {
                list.add(e);
            }

            @Override
            public void onComplete() {
                list.add(100);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 100), list);
    }

    @Test
    public void blockingSubscribeObserverError() {
        final List<Object> list = new ArrayList<>();
        final TestException ex = new TestException();
        Flowable.range(1, 5).concatWith(Flowable.<Integer>error(ex)).subscribeOn(Schedulers.computation()).blockingSubscribe(new FlowableSubscriber<Object>() {

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Object value) {
                list.add(value);
            }

            @Override
            public void onError(Throwable e) {
                list.add(e);
            }

            @Override
            public void onComplete() {
                list.add(100);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, ex), list);
    }

    @Test(expected = TestException.class)
    public void blockingForEachThrows() {
        Flowable.just(1).blockingForEach(new Consumer<Integer>() {

            @Override
            public void accept(Integer e) throws Exception {
                throw new TestException();
            }
        });
    }

    @Test(expected = NoSuchElementException.class)
    public void blockingFirstEmpty() {
        Flowable.empty().blockingFirst();
    }

    @Test(expected = NoSuchElementException.class)
    public void blockingLastEmpty() {
        Flowable.empty().blockingLast();
    }

    @Test
    public void blockingFirstNormal() {
        assertEquals(1, Flowable.just(1, 2).blockingFirst(3).intValue());
    }

    @Test
    public void blockingLastNormal() {
        assertEquals(2, Flowable.just(1, 2).blockingLast(3).intValue());
    }

    @Test
    public void firstFgnoredCancelAndOnNext() {
        Flowable<Integer> source = Flowable.fromPublisher(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onNext(1);
                s.onNext(2);
            }
        });
        assertEquals(1, source.blockingFirst().intValue());
    }

    @Test
    public void firstIgnoredCancelAndOnError() {
        List<Throwable> list = TestHelper.trackPluginErrors();
        try {
            Flowable<Integer> source = Flowable.fromPublisher(new Publisher<Integer>() {

                @Override
                public void subscribe(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onError(new TestException());
                }
            });
            assertEquals(1, source.blockingFirst().intValue());
            TestHelper.assertUndeliverable(list, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test(expected = TestException.class)
    public void firstOnError() {
        Flowable<Integer> source = Flowable.fromPublisher(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onError(new TestException());
            }
        });
        source.blockingFirst();
    }

    @Test
    public void interrupt() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        Thread.currentThread().interrupt();
        try {
            Flowable.just(1).blockingSubscribe(ts);
            ts.assertFailure(InterruptedException.class);
        } finally {
            // clear interrupted status just in case
            Thread.interrupted();
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void blockingSingleEmpty() {
        Flowable.empty().blockingSingle();
    }

    @Test
    public void onCompleteDelayed() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.empty().delay(100, TimeUnit.MILLISECONDS).blockingSubscribe(ts);
        ts.assertResult();
    }

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(FlowableBlockingSubscribe.class);
    }

    @Test
    public void disposeUpFront() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        ts.cancel();
        Flowable.just(1).blockingSubscribe(ts);
        ts.assertEmpty();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void delayed() throws Exception {
        final TestSubscriber<Object> ts = new TestSubscriber<>();
        final Subscriber[] s = { null };
        Schedulers.single().scheduleDirect(new Runnable() {

            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                ts.cancel();
                s[0].onNext(1);
            }
        }, 200, TimeUnit.MILLISECONDS);
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                s[0] = subscriber;
            }
        }.blockingSubscribe(ts);
        while (!ts.isCancelled()) {
            Thread.sleep(100);
        }
        ts.assertEmpty();
    }

    @Test
    public void blockinsSubscribeCancelAsync() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Integer> ts = new TestSubscriber<>();
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            final Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            final AtomicInteger c = new AtomicInteger(2);
            Schedulers.computation().scheduleDirect(new Runnable() {

                @Override
                public void run() {
                    c.decrementAndGet();
                    while (c.get() != 0 && !pp.hasSubscribers()) {
                    }
                    TestHelper.race(r1, r2);
                }
            });
            c.decrementAndGet();
            while (c.get() != 0) {
            }
            pp.blockingSubscribe(ts);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableBlockingTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirst() throws java.lang.Throwable {
            this.payloads.blockingFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirstDefault() throws java.lang.Throwable {
            this.payloads.blockingFirstDefault.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeConsumer() throws java.lang.Throwable {
            this.payloads.blockingSubscribeConsumer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumer() throws java.lang.Throwable {
            this.payloads.boundedBlockingSubscribeConsumer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumerBufferExceed() throws java.lang.Throwable {
            this.payloads.boundedBlockingSubscribeConsumerBufferExceed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeConsumerConsumer() throws java.lang.Throwable {
            this.payloads.blockingSubscribeConsumerConsumer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumerConsumer() throws java.lang.Throwable {
            this.payloads.boundedBlockingSubscribeConsumerConsumer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumerConsumerBufferExceed() throws java.lang.Throwable {
            this.payloads.boundedBlockingSubscribeConsumerConsumerBufferExceed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeConsumerConsumerError() throws java.lang.Throwable {
            this.payloads.blockingSubscribeConsumerConsumerError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumerConsumerError() throws java.lang.Throwable {
            this.payloads.boundedBlockingSubscribeConsumerConsumerError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeConsumerConsumerAction() throws java.lang.Throwable {
            this.payloads.blockingSubscribeConsumerConsumerAction.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumerConsumerAction() throws java.lang.Throwable {
            this.payloads.boundedBlockingSubscribeConsumerConsumerAction.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumerConsumerActionBufferExceed() throws java.lang.Throwable {
            this.payloads.boundedBlockingSubscribeConsumerConsumerActionBufferExceed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundedBlockingSubscribeConsumerConsumerActionBufferExceedMillionItem() throws java.lang.Throwable {
            this.payloads.boundedBlockingSubscribeConsumerConsumerActionBufferExceedMillionItem.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeObserver() throws java.lang.Throwable {
            this.payloads.blockingSubscribeObserver.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeObserverError() throws java.lang.Throwable {
            this.payloads.blockingSubscribeObserverError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingForEachThrows() throws java.lang.Throwable {
            this.payloads.blockingForEachThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirstEmpty() throws java.lang.Throwable {
            this.payloads.blockingFirstEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingLastEmpty() throws java.lang.Throwable {
            this.payloads.blockingLastEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirstNormal() throws java.lang.Throwable {
            this.payloads.blockingFirstNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingLastNormal() throws java.lang.Throwable {
            this.payloads.blockingLastNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstFgnoredCancelAndOnNext() throws java.lang.Throwable {
            this.payloads.firstFgnoredCancelAndOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstIgnoredCancelAndOnError() throws java.lang.Throwable {
            this.payloads.firstIgnoredCancelAndOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstOnError() throws java.lang.Throwable {
            this.payloads.firstOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interrupt() throws java.lang.Throwable {
            this.payloads.interrupt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSingleEmpty() throws java.lang.Throwable {
            this.payloads.blockingSingleEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteDelayed() throws java.lang.Throwable {
            this.payloads.onCompleteDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_utilityClass() throws java.lang.Throwable {
            this.payloads.utilityClass.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeUpFront() throws java.lang.Throwable {
            this.payloads.disposeUpFront.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayed() throws java.lang.Throwable {
            this.payloads.delayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockinsSubscribeCancelAsync() throws java.lang.Throwable {
            this.payloads.blockinsSubscribeCancelAsync.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBlockingTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBlockingTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBlockingTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBlockingTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableBlockingTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableBlockingTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableBlockingTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableBlockingTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement blockingFirst;

            public org.junit.runners.model.Statement blockingFirstDefault;

            public org.junit.runners.model.Statement blockingSubscribeConsumer;

            public org.junit.runners.model.Statement boundedBlockingSubscribeConsumer;

            public org.junit.runners.model.Statement boundedBlockingSubscribeConsumerBufferExceed;

            public org.junit.runners.model.Statement blockingSubscribeConsumerConsumer;

            public org.junit.runners.model.Statement boundedBlockingSubscribeConsumerConsumer;

            public org.junit.runners.model.Statement boundedBlockingSubscribeConsumerConsumerBufferExceed;

            public org.junit.runners.model.Statement blockingSubscribeConsumerConsumerError;

            public org.junit.runners.model.Statement boundedBlockingSubscribeConsumerConsumerError;

            public org.junit.runners.model.Statement blockingSubscribeConsumerConsumerAction;

            public org.junit.runners.model.Statement boundedBlockingSubscribeConsumerConsumerAction;

            public org.junit.runners.model.Statement boundedBlockingSubscribeConsumerConsumerActionBufferExceed;

            public org.junit.runners.model.Statement boundedBlockingSubscribeConsumerConsumerActionBufferExceedMillionItem;

            public org.junit.runners.model.Statement blockingSubscribeObserver;

            public org.junit.runners.model.Statement blockingSubscribeObserverError;

            public org.junit.runners.model.Statement blockingForEachThrows;

            public org.junit.runners.model.Statement blockingFirstEmpty;

            public org.junit.runners.model.Statement blockingLastEmpty;

            public org.junit.runners.model.Statement blockingFirstNormal;

            public org.junit.runners.model.Statement blockingLastNormal;

            public org.junit.runners.model.Statement firstFgnoredCancelAndOnNext;

            public org.junit.runners.model.Statement firstIgnoredCancelAndOnError;

            public org.junit.runners.model.Statement firstOnError;

            public org.junit.runners.model.Statement interrupt;

            public org.junit.runners.model.Statement blockingSingleEmpty;

            public org.junit.runners.model.Statement onCompleteDelayed;

            public org.junit.runners.model.Statement utilityClass;

            public org.junit.runners.model.Statement disposeUpFront;

            public org.junit.runners.model.Statement delayed;

            public org.junit.runners.model.Statement blockinsSubscribeCancelAsync;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.blockingFirst = _ClassStatement.forPayload(FlowableBlockingTest::blockingFirst, "blockingFirst", this);
            this.payloads.blockingFirstDefault = _ClassStatement.forPayload(FlowableBlockingTest::blockingFirstDefault, "blockingFirstDefault", this);
            this.payloads.blockingSubscribeConsumer = _ClassStatement.forPayload(FlowableBlockingTest::blockingSubscribeConsumer, "blockingSubscribeConsumer", this);
            this.payloads.boundedBlockingSubscribeConsumer = _ClassStatement.forPayload(FlowableBlockingTest::boundedBlockingSubscribeConsumer, "boundedBlockingSubscribeConsumer", this);
            this.payloads.boundedBlockingSubscribeConsumerBufferExceed = _ClassStatement.forPayload(FlowableBlockingTest::boundedBlockingSubscribeConsumerBufferExceed, "boundedBlockingSubscribeConsumerBufferExceed", this);
            this.payloads.blockingSubscribeConsumerConsumer = _ClassStatement.forPayload(FlowableBlockingTest::blockingSubscribeConsumerConsumer, "blockingSubscribeConsumerConsumer", this);
            this.payloads.boundedBlockingSubscribeConsumerConsumer = _ClassStatement.forPayload(FlowableBlockingTest::boundedBlockingSubscribeConsumerConsumer, "boundedBlockingSubscribeConsumerConsumer", this);
            this.payloads.boundedBlockingSubscribeConsumerConsumerBufferExceed = _ClassStatement.forPayload(FlowableBlockingTest::boundedBlockingSubscribeConsumerConsumerBufferExceed, "boundedBlockingSubscribeConsumerConsumerBufferExceed", this);
            this.payloads.blockingSubscribeConsumerConsumerError = _ClassStatement.forPayload(FlowableBlockingTest::blockingSubscribeConsumerConsumerError, "blockingSubscribeConsumerConsumerError", this);
            this.payloads.boundedBlockingSubscribeConsumerConsumerError = _ClassStatement.forPayload(FlowableBlockingTest::boundedBlockingSubscribeConsumerConsumerError, "boundedBlockingSubscribeConsumerConsumerError", this);
            this.payloads.blockingSubscribeConsumerConsumerAction = _ClassStatement.forPayload(FlowableBlockingTest::blockingSubscribeConsumerConsumerAction, "blockingSubscribeConsumerConsumerAction", this);
            this.payloads.boundedBlockingSubscribeConsumerConsumerAction = _ClassStatement.forPayload(FlowableBlockingTest::boundedBlockingSubscribeConsumerConsumerAction, "boundedBlockingSubscribeConsumerConsumerAction", this);
            this.payloads.boundedBlockingSubscribeConsumerConsumerActionBufferExceed = _ClassStatement.forPayload(FlowableBlockingTest::boundedBlockingSubscribeConsumerConsumerActionBufferExceed, "boundedBlockingSubscribeConsumerConsumerActionBufferExceed", this);
            this.payloads.boundedBlockingSubscribeConsumerConsumerActionBufferExceedMillionItem = _ClassStatement.forPayload(FlowableBlockingTest::boundedBlockingSubscribeConsumerConsumerActionBufferExceedMillionItem, "boundedBlockingSubscribeConsumerConsumerActionBufferExceedMillionItem", this);
            this.payloads.blockingSubscribeObserver = _ClassStatement.forPayload(FlowableBlockingTest::blockingSubscribeObserver, "blockingSubscribeObserver", this);
            this.payloads.blockingSubscribeObserverError = _ClassStatement.forPayload(FlowableBlockingTest::blockingSubscribeObserverError, "blockingSubscribeObserverError", this);
            this.payloads.blockingForEachThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableBlockingTest::blockingForEachThrows, io.reactivex.rxjava3.exceptions.TestException.class), "blockingForEachThrows", this);
            this.payloads.blockingFirstEmpty = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableBlockingTest::blockingFirstEmpty, java.util.NoSuchElementException.class), "blockingFirstEmpty", this);
            this.payloads.blockingLastEmpty = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableBlockingTest::blockingLastEmpty, java.util.NoSuchElementException.class), "blockingLastEmpty", this);
            this.payloads.blockingFirstNormal = _ClassStatement.forPayload(FlowableBlockingTest::blockingFirstNormal, "blockingFirstNormal", this);
            this.payloads.blockingLastNormal = _ClassStatement.forPayload(FlowableBlockingTest::blockingLastNormal, "blockingLastNormal", this);
            this.payloads.firstFgnoredCancelAndOnNext = _ClassStatement.forPayload(FlowableBlockingTest::firstFgnoredCancelAndOnNext, "firstFgnoredCancelAndOnNext", this);
            this.payloads.firstIgnoredCancelAndOnError = _ClassStatement.forPayload(FlowableBlockingTest::firstIgnoredCancelAndOnError, "firstIgnoredCancelAndOnError", this);
            this.payloads.firstOnError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableBlockingTest::firstOnError, io.reactivex.rxjava3.exceptions.TestException.class), "firstOnError", this);
            this.payloads.interrupt = _ClassStatement.forPayload(FlowableBlockingTest::interrupt, "interrupt", this);
            this.payloads.blockingSingleEmpty = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableBlockingTest::blockingSingleEmpty, java.util.NoSuchElementException.class), "blockingSingleEmpty", this);
            this.payloads.onCompleteDelayed = _ClassStatement.forPayload(FlowableBlockingTest::onCompleteDelayed, "onCompleteDelayed", this);
            this.payloads.utilityClass = _ClassStatement.forPayload(FlowableBlockingTest::utilityClass, "utilityClass", this);
            this.payloads.disposeUpFront = _ClassStatement.forPayload(FlowableBlockingTest::disposeUpFront, "disposeUpFront", this);
            this.payloads.delayed = _ClassStatement.forPayload(FlowableBlockingTest::delayed, "delayed", this);
            this.payloads.blockinsSubscribeCancelAsync = _ClassStatement.forPayload(FlowableBlockingTest::blockinsSubscribeCancelAsync, "blockinsSubscribeCancelAsync", this);
        }
    }
}
