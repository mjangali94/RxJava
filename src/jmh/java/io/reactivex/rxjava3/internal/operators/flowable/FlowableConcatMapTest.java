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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap.SimpleScalarSubscription;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableConcatMapTest extends RxJavaTest {

    @Test
    public void simpleSubscriptionRequest() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0);
        SimpleScalarSubscription<Integer> ws = new SimpleScalarSubscription<>(1, ts);
        ts.onSubscribe(ws);
        ws.request(0);
        ts.assertEmpty();
        ws.request(1);
        ts.assertResult(1);
        ws.request(1);
        ts.assertResult(1);
    }

    @Test
    public void boundaryFusion() {
        Flowable.range(1, 10000).observeOn(Schedulers.single()).map(new Function<Integer, String>() {

            @Override
            public String apply(Integer t) throws Exception {
                String name = Thread.currentThread().getName();
                if (name.contains("RxSingleScheduler")) {
                    return "RxSingleScheduler";
                }
                return name;
            }
        }).concatMap(new Function<String, Publisher<? extends Object>>() {

            @Override
            public Publisher<? extends Object> apply(String v) throws Exception {
                return Flowable.just(v);
            }
        }).observeOn(Schedulers.computation()).distinct().test().awaitDone(5, TimeUnit.SECONDS).assertResult("RxSingleScheduler");
    }

    @Test
    public void innerScalarRequestRace() {
        Flowable<Integer> just = Flowable.just(1);
        int n = 1000;
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishProcessor<Flowable<Integer>> source = PublishProcessor.create();
            TestSubscriber<Integer> ts = source.concatMap(v -> v, n + 1).test(1L);
            TestHelper.race(() -> {
                for (int j = 0; j < n; j++) {
                    source.onNext(just);
                }
            }, () -> {
                for (int j = 0; j < n; j++) {
                    ts.request(1);
                }
            });
            ts.assertValueCount(n);
        }
    }

    @Test
    public void innerScalarRequestRaceDelayError() {
        Flowable<Integer> just = Flowable.just(1);
        int n = 1000;
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishProcessor<Flowable<Integer>> source = PublishProcessor.create();
            TestSubscriber<Integer> ts = source.concatMapDelayError(v -> v, true, n + 1).test(1L);
            TestHelper.race(() -> {
                for (int j = 0; j < n; j++) {
                    source.onNext(just);
                }
            }, () -> {
                for (int j = 0; j < n; j++) {
                    ts.request(1);
                }
            });
            ts.assertValueCount(n);
        }
    }

    @Test
    public void boundaryFusionDelayError() {
        Flowable.range(1, 10000).observeOn(Schedulers.single()).map(new Function<Integer, String>() {

            @Override
            public String apply(Integer t) throws Exception {
                String name = Thread.currentThread().getName();
                if (name.contains("RxSingleScheduler")) {
                    return "RxSingleScheduler";
                }
                return name;
            }
        }).concatMapDelayError(new Function<String, Publisher<? extends Object>>() {

            @Override
            public Publisher<? extends Object> apply(String v) throws Exception {
                return Flowable.just(v);
            }
        }).observeOn(Schedulers.computation()).distinct().test().awaitDone(5, TimeUnit.SECONDS).assertResult("RxSingleScheduler");
    }

    @Test
    public void pollThrows() {
        Flowable.just(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).compose(TestHelper.<Integer>flowableStripBoundary()).concatMap(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.just(v);
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void pollThrowsDelayError() {
        Flowable.just(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).compose(TestHelper.<Integer>flowableStripBoundary()).concatMapDelayError(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.just(v);
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void noCancelPrevious() {
        final AtomicInteger counter = new AtomicInteger();
        Flowable.range(1, 5).concatMap(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) throws Exception {
                return Flowable.just(v).doOnCancel(new Action() {

                    @Override
                    public void run() throws Exception {
                        counter.getAndIncrement();
                    }
                });
            }
        }).test().assertResult(1, 2, 3, 4, 5);
        assertEquals(0, counter.get());
    }

    @Test
    public void delayErrorCallableTillTheEnd() {
        Flowable.just(1, 2, 3, 101, 102, 23, 890, 120, 32).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(final Integer integer) throws Exception {
                return Flowable.fromCallable(new Callable<Integer>() {

                    @Override
                    public Integer call() throws Exception {
                        if (integer >= 100) {
                            throw new NullPointerException("test null exp");
                        }
                        return integer;
                    }
                });
            }
        }).test().assertFailure(CompositeException.class, 1, 2, 3, 23, 32);
    }

    @Test
    public void delayErrorCallableEager() {
        Flowable.just(1, 2, 3, 101, 102, 23, 890, 120, 32).concatMapDelayError(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(final Integer integer) throws Exception {
                return Flowable.fromCallable(new Callable<Integer>() {

                    @Override
                    public Integer call() throws Exception {
                        if (integer >= 100) {
                            throw new NullPointerException("test null exp");
                        }
                        return integer;
                    }
                });
            }
        }, false, 2).test().assertFailure(NullPointerException.class, 1, 2, 3);
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.concatMap(new Function<Integer, Publisher<Integer>>() {

                    @Override
                    public Publisher<Integer> apply(Integer v) throws Throwable {
                        return Flowable.just(v).hide();
                    }
                });
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayError() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.concatMapDelayError(new Function<Integer, Publisher<Integer>>() {

                    @Override
                    public Publisher<Integer> apply(Integer v) throws Throwable {
                        return Flowable.just(v).hide();
                    }
                }, false, 2);
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayErrorTillEnd() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> upstream) {
                return upstream.concatMapDelayError(new Function<Integer, Publisher<Integer>>() {

                    @Override
                    public Publisher<Integer> apply(Integer v) throws Throwable {
                        return Flowable.just(v).hide();
                    }
                }, true, 2);
            }
        });
    }

    @Test
    public void asyncFusedSource() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        up.onNext(1);
        up.onComplete();
        up.concatMap(v -> Flowable.just(1).hide()).test().assertResult(1);
    }

    @Test
    public void scalarCallableSource() {
        Flowable.fromCallable(() -> 1).concatMap(v -> Flowable.just(1)).test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableConcatMapTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleSubscriptionRequest() throws java.lang.Throwable {
            this.payloads.simpleSubscriptionRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusion() throws java.lang.Throwable {
            this.payloads.boundaryFusion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerScalarRequestRace() throws java.lang.Throwable {
            this.payloads.innerScalarRequestRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerScalarRequestRaceDelayError() throws java.lang.Throwable {
            this.payloads.innerScalarRequestRaceDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusionDelayError() throws java.lang.Throwable {
            this.payloads.boundaryFusionDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_pollThrows() throws java.lang.Throwable {
            this.payloads.pollThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_pollThrowsDelayError() throws java.lang.Throwable {
            this.payloads.pollThrowsDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPrevious() throws java.lang.Throwable {
            this.payloads.noCancelPrevious.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorCallableTillTheEnd() throws java.lang.Throwable {
            this.payloads.delayErrorCallableTillTheEnd.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorCallableEager() throws java.lang.Throwable {
            this.payloads.delayErrorCallableEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelDelayError() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancelDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelDelayErrorTillEnd() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancelDelayErrorTillEnd.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedSource() throws java.lang.Throwable {
            this.payloads.asyncFusedSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarCallableSource() throws java.lang.Throwable {
            this.payloads.scalarCallableSource.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableConcatMapTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatMapTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableConcatMapTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableConcatMapTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement simpleSubscriptionRequest;

            public org.junit.runners.model.Statement boundaryFusion;

            public org.junit.runners.model.Statement innerScalarRequestRace;

            public org.junit.runners.model.Statement innerScalarRequestRaceDelayError;

            public org.junit.runners.model.Statement boundaryFusionDelayError;

            public org.junit.runners.model.Statement pollThrows;

            public org.junit.runners.model.Statement pollThrowsDelayError;

            public org.junit.runners.model.Statement noCancelPrevious;

            public org.junit.runners.model.Statement delayErrorCallableTillTheEnd;

            public org.junit.runners.model.Statement delayErrorCallableEager;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayErrorTillEnd;

            public org.junit.runners.model.Statement asyncFusedSource;

            public org.junit.runners.model.Statement scalarCallableSource;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.simpleSubscriptionRequest = _ClassStatement.forPayload(FlowableConcatMapTest::simpleSubscriptionRequest, "simpleSubscriptionRequest", this);
            this.payloads.boundaryFusion = _ClassStatement.forPayload(FlowableConcatMapTest::boundaryFusion, "boundaryFusion", this);
            this.payloads.innerScalarRequestRace = _ClassStatement.forPayload(FlowableConcatMapTest::innerScalarRequestRace, "innerScalarRequestRace", this);
            this.payloads.innerScalarRequestRaceDelayError = _ClassStatement.forPayload(FlowableConcatMapTest::innerScalarRequestRaceDelayError, "innerScalarRequestRaceDelayError", this);
            this.payloads.boundaryFusionDelayError = _ClassStatement.forPayload(FlowableConcatMapTest::boundaryFusionDelayError, "boundaryFusionDelayError", this);
            this.payloads.pollThrows = _ClassStatement.forPayload(FlowableConcatMapTest::pollThrows, "pollThrows", this);
            this.payloads.pollThrowsDelayError = _ClassStatement.forPayload(FlowableConcatMapTest::pollThrowsDelayError, "pollThrowsDelayError", this);
            this.payloads.noCancelPrevious = _ClassStatement.forPayload(FlowableConcatMapTest::noCancelPrevious, "noCancelPrevious", this);
            this.payloads.delayErrorCallableTillTheEnd = _ClassStatement.forPayload(FlowableConcatMapTest::delayErrorCallableTillTheEnd, "delayErrorCallableTillTheEnd", this);
            this.payloads.delayErrorCallableEager = _ClassStatement.forPayload(FlowableConcatMapTest::delayErrorCallableEager, "delayErrorCallableEager", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(FlowableConcatMapTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(FlowableConcatMapTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.undeliverableUponCancelDelayErrorTillEnd = _ClassStatement.forPayload(FlowableConcatMapTest::undeliverableUponCancelDelayErrorTillEnd, "undeliverableUponCancelDelayErrorTillEnd", this);
            this.payloads.asyncFusedSource = _ClassStatement.forPayload(FlowableConcatMapTest::asyncFusedSource, "asyncFusedSource", this);
            this.payloads.scalarCallableSource = _ClassStatement.forPayload(FlowableConcatMapTest::scalarCallableSource, "scalarCallableSource", this);
        }
    }
}
