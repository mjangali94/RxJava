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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableToListTest extends RxJavaTest {

    @Test
    public void listFlowable() {
        Flowable<String> w = Flowable.fromIterable(Arrays.asList("one", "two", "three"));
        Flowable<List<String>> flowable = w.toList().toFlowable();
        Subscriber<List<String>> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(Arrays.asList("one", "two", "three"));
        verify(subscriber, Mockito.never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void listViaFlowableFlowable() {
        Flowable<String> w = Flowable.fromIterable(Arrays.asList("one", "two", "three"));
        Flowable<List<String>> flowable = w.toList().toFlowable();
        Subscriber<List<String>> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(Arrays.asList("one", "two", "three"));
        verify(subscriber, Mockito.never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void listMultipleSubscribersFlowable() {
        Flowable<String> w = Flowable.fromIterable(Arrays.asList("one", "two", "three"));
        Flowable<List<String>> flowable = w.toList().toFlowable();
        Subscriber<List<String>> subscriber1 = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber1);
        Subscriber<List<String>> subscriber2 = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber2);
        List<String> expected = Arrays.asList("one", "two", "three");
        verify(subscriber1, times(1)).onNext(expected);
        verify(subscriber1, Mockito.never()).onError(any(Throwable.class));
        verify(subscriber1, times(1)).onComplete();
        verify(subscriber2, times(1)).onNext(expected);
        verify(subscriber2, Mockito.never()).onError(any(Throwable.class));
        verify(subscriber2, times(1)).onComplete();
    }

    @Test
    public void listWithBlockingFirstFlowable() {
        Flowable<String> f = Flowable.fromIterable(Arrays.asList("one", "two", "three"));
        List<String> actual = f.toList().toFlowable().blockingFirst();
        Assert.assertEquals(Arrays.asList("one", "two", "three"), actual);
    }

    @Test
    public void backpressureHonoredFlowable() {
        Flowable<List<Integer>> w = Flowable.just(1, 2, 3, 4, 5).toList().toFlowable();
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>(0L);
        w.subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(1);
        ts.assertValue(Arrays.asList(1, 2, 3, 4, 5));
        ts.assertNoErrors();
        ts.assertComplete();
        ts.request(1);
        ts.assertValue(Arrays.asList(1, 2, 3, 4, 5));
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void capacityHintFlowable() {
        Flowable.range(1, 10).toList(4).toFlowable().test().assertResult(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void list() {
        Flowable<String> w = Flowable.fromIterable(Arrays.asList("one", "two", "three"));
        Single<List<String>> single = w.toList();
        SingleObserver<List<String>> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(Arrays.asList("one", "two", "three"));
        verify(observer, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void listViaFlowable() {
        Flowable<String> w = Flowable.fromIterable(Arrays.asList("one", "two", "three"));
        Single<List<String>> single = w.toList();
        SingleObserver<List<String>> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(Arrays.asList("one", "two", "three"));
        verify(observer, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void listMultipleSubscribers() {
        Flowable<String> w = Flowable.fromIterable(Arrays.asList("one", "two", "three"));
        Single<List<String>> single = w.toList();
        SingleObserver<List<String>> o1 = TestHelper.mockSingleObserver();
        single.subscribe(o1);
        SingleObserver<List<String>> o2 = TestHelper.mockSingleObserver();
        single.subscribe(o2);
        List<String> expected = Arrays.asList("one", "two", "three");
        verify(o1, times(1)).onSuccess(expected);
        verify(o1, Mockito.never()).onError(any(Throwable.class));
        verify(o2, times(1)).onSuccess(expected);
        verify(o2, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void listWithBlockingFirst() {
        Flowable<String> f = Flowable.fromIterable(Arrays.asList("one", "two", "three"));
        List<String> actual = f.toList().blockingGet();
        Assert.assertEquals(Arrays.asList("one", "two", "three"), actual);
    }

    static void await(CyclicBarrier cb) {
        try {
            cb.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (BrokenBarrierException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void capacityHint() {
        Flowable.range(1, 10).toList(4).test().assertResult(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).toList().toFlowable());
        TestHelper.checkDisposed(Flowable.just(1).toList());
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).toList().toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void errorSingle() {
        Flowable.error(new TestException()).toList().test().assertFailure(TestException.class);
    }

    @Test
    public void collectionSupplierThrows() {
        Flowable.just(1).toList(new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() throws Exception {
                throw new TestException();
            }
        }).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void collectionSupplierReturnsNull() {
        Flowable.just(1).toList(new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() throws Exception {
                return null;
            }
        }).toFlowable().to(TestHelper.<Collection<Integer>>testConsumer()).assertFailure(NullPointerException.class).assertErrorMessage(ExceptionHelper.nullWarning("The collectionSupplier returned a null Collection."));
    }

    @Test
    public void singleCollectionSupplierThrows() {
        Flowable.just(1).toList(new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void singleCollectionSupplierReturnsNull() {
        Flowable.just(1).toList(new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() throws Exception {
                return null;
            }
        }).to(TestHelper.<Collection<Integer>>testConsumer()).assertFailure(NullPointerException.class).assertErrorMessage(ExceptionHelper.nullWarning("The collectionSupplier returned a null Collection."));
    }

    @Test
    public void onNextCancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final TestObserver<List<Integer>> to = pp.toList().test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void onNextCancelRaceFlowable() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final TestSubscriber<List<Integer>> ts = pp.toList().toFlowable().test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void onCompleteCancelRaceFlowable() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final TestSubscriber<List<Integer>> ts = pp.toList().toFlowable().test();
            pp.onNext(1);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    pp.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            TestHelper.race(r1, r2);
            if (ts.values().size() != 0) {
                ts.assertValue(Arrays.asList(1)).assertNoErrors();
            }
        }
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<List<Object>>>() {

            @Override
            public Flowable<List<Object>> apply(Flowable<Object> f) throws Exception {
                return f.toList().toFlowable();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Object>, Single<List<Object>>>() {

            @Override
            public Single<List<Object>> apply(Flowable<Object> f) throws Exception {
                return f.toList();
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableToListTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listFlowable() throws java.lang.Throwable {
            this.payloads.listFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listViaFlowableFlowable() throws java.lang.Throwable {
            this.payloads.listViaFlowableFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listMultipleSubscribersFlowable() throws java.lang.Throwable {
            this.payloads.listMultipleSubscribersFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listWithBlockingFirstFlowable() throws java.lang.Throwable {
            this.payloads.listWithBlockingFirstFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureHonoredFlowable() throws java.lang.Throwable {
            this.payloads.backpressureHonoredFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_capacityHintFlowable() throws java.lang.Throwable {
            this.payloads.capacityHintFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_list() throws java.lang.Throwable {
            this.payloads.list.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listViaFlowable() throws java.lang.Throwable {
            this.payloads.listViaFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listMultipleSubscribers() throws java.lang.Throwable {
            this.payloads.listMultipleSubscribers.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listWithBlockingFirst() throws java.lang.Throwable {
            this.payloads.listWithBlockingFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_capacityHint() throws java.lang.Throwable {
            this.payloads.capacityHint.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSingle() throws java.lang.Throwable {
            this.payloads.errorSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectionSupplierThrows() throws java.lang.Throwable {
            this.payloads.collectionSupplierThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectionSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.collectionSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleCollectionSupplierThrows() throws java.lang.Throwable {
            this.payloads.singleCollectionSupplierThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleCollectionSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.singleCollectionSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextCancelRace() throws java.lang.Throwable {
            this.payloads.onNextCancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextCancelRaceFlowable() throws java.lang.Throwable {
            this.payloads.onNextCancelRaceFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteCancelRaceFlowable() throws java.lang.Throwable {
            this.payloads.onCompleteCancelRaceFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToListTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToListTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToListTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToListTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableToListTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToListTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableToListTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableToListTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement listFlowable;

            public org.junit.runners.model.Statement listViaFlowableFlowable;

            public org.junit.runners.model.Statement listMultipleSubscribersFlowable;

            public org.junit.runners.model.Statement listWithBlockingFirstFlowable;

            public org.junit.runners.model.Statement backpressureHonoredFlowable;

            public org.junit.runners.model.Statement capacityHintFlowable;

            public org.junit.runners.model.Statement list;

            public org.junit.runners.model.Statement listViaFlowable;

            public org.junit.runners.model.Statement listMultipleSubscribers;

            public org.junit.runners.model.Statement listWithBlockingFirst;

            public org.junit.runners.model.Statement capacityHint;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement errorSingle;

            public org.junit.runners.model.Statement collectionSupplierThrows;

            public org.junit.runners.model.Statement collectionSupplierReturnsNull;

            public org.junit.runners.model.Statement singleCollectionSupplierThrows;

            public org.junit.runners.model.Statement singleCollectionSupplierReturnsNull;

            public org.junit.runners.model.Statement onNextCancelRace;

            public org.junit.runners.model.Statement onNextCancelRaceFlowable;

            public org.junit.runners.model.Statement onCompleteCancelRaceFlowable;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.listFlowable = _ClassStatement.forPayload(FlowableToListTest::listFlowable, "listFlowable", this);
            this.payloads.listViaFlowableFlowable = _ClassStatement.forPayload(FlowableToListTest::listViaFlowableFlowable, "listViaFlowableFlowable", this);
            this.payloads.listMultipleSubscribersFlowable = _ClassStatement.forPayload(FlowableToListTest::listMultipleSubscribersFlowable, "listMultipleSubscribersFlowable", this);
            this.payloads.listWithBlockingFirstFlowable = _ClassStatement.forPayload(FlowableToListTest::listWithBlockingFirstFlowable, "listWithBlockingFirstFlowable", this);
            this.payloads.backpressureHonoredFlowable = _ClassStatement.forPayload(FlowableToListTest::backpressureHonoredFlowable, "backpressureHonoredFlowable", this);
            this.payloads.capacityHintFlowable = _ClassStatement.forPayload(FlowableToListTest::capacityHintFlowable, "capacityHintFlowable", this);
            this.payloads.list = _ClassStatement.forPayload(FlowableToListTest::list, "list", this);
            this.payloads.listViaFlowable = _ClassStatement.forPayload(FlowableToListTest::listViaFlowable, "listViaFlowable", this);
            this.payloads.listMultipleSubscribers = _ClassStatement.forPayload(FlowableToListTest::listMultipleSubscribers, "listMultipleSubscribers", this);
            this.payloads.listWithBlockingFirst = _ClassStatement.forPayload(FlowableToListTest::listWithBlockingFirst, "listWithBlockingFirst", this);
            this.payloads.capacityHint = _ClassStatement.forPayload(FlowableToListTest::capacityHint, "capacityHint", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableToListTest::dispose, "dispose", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableToListTest::error, "error", this);
            this.payloads.errorSingle = _ClassStatement.forPayload(FlowableToListTest::errorSingle, "errorSingle", this);
            this.payloads.collectionSupplierThrows = _ClassStatement.forPayload(FlowableToListTest::collectionSupplierThrows, "collectionSupplierThrows", this);
            this.payloads.collectionSupplierReturnsNull = _ClassStatement.forPayload(FlowableToListTest::collectionSupplierReturnsNull, "collectionSupplierReturnsNull", this);
            this.payloads.singleCollectionSupplierThrows = _ClassStatement.forPayload(FlowableToListTest::singleCollectionSupplierThrows, "singleCollectionSupplierThrows", this);
            this.payloads.singleCollectionSupplierReturnsNull = _ClassStatement.forPayload(FlowableToListTest::singleCollectionSupplierReturnsNull, "singleCollectionSupplierReturnsNull", this);
            this.payloads.onNextCancelRace = _ClassStatement.forPayload(FlowableToListTest::onNextCancelRace, "onNextCancelRace", this);
            this.payloads.onNextCancelRaceFlowable = _ClassStatement.forPayload(FlowableToListTest::onNextCancelRaceFlowable, "onNextCancelRaceFlowable", this);
            this.payloads.onCompleteCancelRaceFlowable = _ClassStatement.forPayload(FlowableToListTest::onCompleteCancelRaceFlowable, "onCompleteCancelRaceFlowable", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableToListTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
