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
import java.util.List;
import org.junit.*;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableJoinTest extends RxJavaTest {

    Subscriber<Object> subscriber = TestHelper.mockSubscriber();

    BiFunction<Integer, Integer, Integer> add = new BiFunction<Integer, Integer, Integer>() {

        @Override
        public Integer apply(Integer t1, Integer t2) {
            return t1 + t2;
        }
    };

    <T> Function<Integer, Flowable<T>> just(final Flowable<T> flowable) {
        return new Function<Integer, Flowable<T>>() {

            @Override
            public Flowable<T> apply(Integer t1) {
                return flowable;
            }
        };
    }

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void normal1() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Flowable<Integer> m = source1.join(source2, just(Flowable.never()), just(Flowable.never()), add);
        m.subscribe(subscriber);
        source1.onNext(1);
        source1.onNext(2);
        source1.onNext(4);
        source2.onNext(16);
        source2.onNext(32);
        source2.onNext(64);
        source1.onComplete();
        source2.onComplete();
        verify(subscriber, times(1)).onNext(17);
        verify(subscriber, times(1)).onNext(18);
        verify(subscriber, times(1)).onNext(20);
        verify(subscriber, times(1)).onNext(33);
        verify(subscriber, times(1)).onNext(34);
        verify(subscriber, times(1)).onNext(36);
        verify(subscriber, times(1)).onNext(65);
        verify(subscriber, times(1)).onNext(66);
        verify(subscriber, times(1)).onNext(68);
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void normal1WithDuration() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        PublishProcessor<Integer> duration1 = PublishProcessor.create();
        Flowable<Integer> m = source1.join(source2, just(duration1), just(Flowable.never()), add);
        m.subscribe(subscriber);
        source1.onNext(1);
        source1.onNext(2);
        source2.onNext(16);
        duration1.onNext(1);
        source1.onNext(4);
        source1.onNext(8);
        source1.onComplete();
        source2.onComplete();
        verify(subscriber, times(1)).onNext(17);
        verify(subscriber, times(1)).onNext(18);
        verify(subscriber, times(1)).onNext(20);
        verify(subscriber, times(1)).onNext(24);
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void normal2() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Flowable<Integer> m = source1.join(source2, just(Flowable.never()), just(Flowable.never()), add);
        m.subscribe(subscriber);
        source1.onNext(1);
        source1.onNext(2);
        source1.onComplete();
        source2.onNext(16);
        source2.onNext(32);
        source2.onNext(64);
        source2.onComplete();
        verify(subscriber, times(1)).onNext(17);
        verify(subscriber, times(1)).onNext(18);
        verify(subscriber, times(1)).onNext(33);
        verify(subscriber, times(1)).onNext(34);
        verify(subscriber, times(1)).onNext(65);
        verify(subscriber, times(1)).onNext(66);
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void leftThrows() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Flowable<Integer> m = source1.join(source2, just(Flowable.never()), just(Flowable.never()), add);
        m.subscribe(subscriber);
        source2.onNext(1);
        source1.onError(new RuntimeException("Forced failure"));
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void rightThrows() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Flowable<Integer> m = source1.join(source2, just(Flowable.never()), just(Flowable.never()), add);
        m.subscribe(subscriber);
        source1.onNext(1);
        source2.onError(new RuntimeException("Forced failure"));
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void leftDurationThrows() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Flowable<Integer> duration1 = Flowable.<Integer>error(new RuntimeException("Forced failure"));
        Flowable<Integer> m = source1.join(source2, just(duration1), just(Flowable.never()), add);
        m.subscribe(subscriber);
        source1.onNext(1);
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void rightDurationThrows() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Flowable<Integer> duration1 = Flowable.<Integer>error(new RuntimeException("Forced failure"));
        Flowable<Integer> m = source1.join(source2, just(Flowable.never()), just(duration1), add);
        m.subscribe(subscriber);
        source2.onNext(1);
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void leftDurationSelectorThrows() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> fail = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                throw new RuntimeException("Forced failure");
            }
        };
        Flowable<Integer> m = source1.join(source2, fail, just(Flowable.never()), add);
        m.subscribe(subscriber);
        source1.onNext(1);
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void rightDurationSelectorThrows() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> fail = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                throw new RuntimeException("Forced failure");
            }
        };
        Flowable<Integer> m = source1.join(source2, just(Flowable.never()), fail, add);
        m.subscribe(subscriber);
        source2.onNext(1);
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void resultSelectorThrows() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        BiFunction<Integer, Integer, Integer> fail = new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                throw new RuntimeException("Forced failure");
            }
        };
        Flowable<Integer> m = source1.join(source2, just(Flowable.never()), just(Flowable.never()), fail);
        m.subscribe(subscriber);
        source1.onNext(1);
        source2.onNext(2);
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.<Integer>create().join(Flowable.just(1), Functions.justFunction(Flowable.never()), Functions.justFunction(Flowable.never()), new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }));
    }

    @Test
    public void take() {
        Flowable.just(1).join(Flowable.just(2), Functions.justFunction(Flowable.never()), Functions.justFunction(Flowable.never()), new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).take(1).test().assertResult(3);
    }

    @Test
    public void rightClose() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.join(Flowable.just(2), Functions.justFunction(Flowable.never()), Functions.justFunction(Flowable.empty()), new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).test().assertEmpty();
        pp.onNext(1);
        ts.assertEmpty();
    }

    @Test
    public void resultSelectorThrows2() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.join(Flowable.just(2), Functions.justFunction(Flowable.never()), Functions.justFunction(Flowable.never()), new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                throw new TestException();
            }
        }).test();
        pp.onNext(1);
        pp.onComplete();
        ts.assertFailure(TestException.class);
    }

    @Test
    public void badOuterSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onError(new TestException("First"));
                    subscriber.onError(new TestException("Second"));
                }
            }.join(Flowable.just(2), Functions.justFunction(Flowable.never()), Functions.justFunction(Flowable.never()), new BiFunction<Integer, Integer, Integer>() {

                @Override
                public Integer apply(Integer a, Integer b) throws Exception {
                    return a + b;
                }
            }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badEndSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            @SuppressWarnings("rawtypes")
            final Subscriber[] o = { null };
            TestSubscriberEx<Integer> ts = Flowable.just(1).join(Flowable.just(2), Functions.justFunction(Flowable.never()), Functions.justFunction(new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    o[0] = subscriber;
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onError(new TestException("First"));
                }
            }), new BiFunction<Integer, Integer, Integer>() {

                @Override
                public Integer apply(Integer a, Integer b) throws Exception {
                    return a + b;
                }
            }).to(TestHelper.<Integer>testConsumer());
            o[0].onError(new TestException("Second"));
            ts.assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void backpressureOverflowRight() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Object> ts = pp1.join(pp2, Functions.justFunction(Flowable.never()), Functions.justFunction(Flowable.never()), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).test(0L);
        pp1.onNext(1);
        pp2.onNext(2);
        ts.assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void backpressureOverflowLeft() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Object> ts = pp1.join(pp2, Functions.justFunction(Flowable.never()), Functions.justFunction(Flowable.never()), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).test(0L);
        pp2.onNext(2);
        pp1.onNext(1);
        ts.assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void badRequest() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestHelper.assertBadRequestReported(pp1.join(pp2, Functions.justFunction(Flowable.never()), Functions.justFunction(Flowable.never()), (a, b) -> a + b));
    }

    @Test
    public void bothTerminateWithWorkRemaining() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp1.join(pp2, v -> Flowable.never(), v -> Flowable.never(), (a, b) -> a + b).doOnNext(v -> {
            pp1.onComplete();
            pp2.onNext(2);
            pp2.onComplete();
        }).test();
        pp1.onNext(0);
        pp2.onNext(1);
        ts.assertComplete();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableJoinTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal1() throws java.lang.Throwable {
            this.payloads.normal1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal1WithDuration() throws java.lang.Throwable {
            this.payloads.normal1WithDuration.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal2() throws java.lang.Throwable {
            this.payloads.normal2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_leftThrows() throws java.lang.Throwable {
            this.payloads.leftThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rightThrows() throws java.lang.Throwable {
            this.payloads.rightThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_leftDurationThrows() throws java.lang.Throwable {
            this.payloads.leftDurationThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rightDurationThrows() throws java.lang.Throwable {
            this.payloads.rightDurationThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_leftDurationSelectorThrows() throws java.lang.Throwable {
            this.payloads.leftDurationSelectorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rightDurationSelectorThrows() throws java.lang.Throwable {
            this.payloads.rightDurationSelectorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resultSelectorThrows() throws java.lang.Throwable {
            this.payloads.resultSelectorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rightClose() throws java.lang.Throwable {
            this.payloads.rightClose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resultSelectorThrows2() throws java.lang.Throwable {
            this.payloads.resultSelectorThrows2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badOuterSource() throws java.lang.Throwable {
            this.payloads.badOuterSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badEndSource() throws java.lang.Throwable {
            this.payloads.badEndSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureOverflowRight() throws java.lang.Throwable {
            this.payloads.backpressureOverflowRight.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureOverflowLeft() throws java.lang.Throwable {
            this.payloads.backpressureOverflowLeft.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothTerminateWithWorkRemaining() throws java.lang.Throwable {
            this.payloads.bothTerminateWithWorkRemaining.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableJoinTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableJoinTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.before();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableJoinTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableJoinTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableJoinTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableJoinTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableJoinTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableJoinTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal1;

            public org.junit.runners.model.Statement normal1WithDuration;

            public org.junit.runners.model.Statement normal2;

            public org.junit.runners.model.Statement leftThrows;

            public org.junit.runners.model.Statement rightThrows;

            public org.junit.runners.model.Statement leftDurationThrows;

            public org.junit.runners.model.Statement rightDurationThrows;

            public org.junit.runners.model.Statement leftDurationSelectorThrows;

            public org.junit.runners.model.Statement rightDurationSelectorThrows;

            public org.junit.runners.model.Statement resultSelectorThrows;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement rightClose;

            public org.junit.runners.model.Statement resultSelectorThrows2;

            public org.junit.runners.model.Statement badOuterSource;

            public org.junit.runners.model.Statement badEndSource;

            public org.junit.runners.model.Statement backpressureOverflowRight;

            public org.junit.runners.model.Statement backpressureOverflowLeft;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement bothTerminateWithWorkRemaining;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal1 = _ClassStatement.forPayload(FlowableJoinTest::normal1, "normal1", this);
            this.payloads.normal1WithDuration = _ClassStatement.forPayload(FlowableJoinTest::normal1WithDuration, "normal1WithDuration", this);
            this.payloads.normal2 = _ClassStatement.forPayload(FlowableJoinTest::normal2, "normal2", this);
            this.payloads.leftThrows = _ClassStatement.forPayload(FlowableJoinTest::leftThrows, "leftThrows", this);
            this.payloads.rightThrows = _ClassStatement.forPayload(FlowableJoinTest::rightThrows, "rightThrows", this);
            this.payloads.leftDurationThrows = _ClassStatement.forPayload(FlowableJoinTest::leftDurationThrows, "leftDurationThrows", this);
            this.payloads.rightDurationThrows = _ClassStatement.forPayload(FlowableJoinTest::rightDurationThrows, "rightDurationThrows", this);
            this.payloads.leftDurationSelectorThrows = _ClassStatement.forPayload(FlowableJoinTest::leftDurationSelectorThrows, "leftDurationSelectorThrows", this);
            this.payloads.rightDurationSelectorThrows = _ClassStatement.forPayload(FlowableJoinTest::rightDurationSelectorThrows, "rightDurationSelectorThrows", this);
            this.payloads.resultSelectorThrows = _ClassStatement.forPayload(FlowableJoinTest::resultSelectorThrows, "resultSelectorThrows", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableJoinTest::dispose, "dispose", this);
            this.payloads.take = _ClassStatement.forPayload(FlowableJoinTest::take, "take", this);
            this.payloads.rightClose = _ClassStatement.forPayload(FlowableJoinTest::rightClose, "rightClose", this);
            this.payloads.resultSelectorThrows2 = _ClassStatement.forPayload(FlowableJoinTest::resultSelectorThrows2, "resultSelectorThrows2", this);
            this.payloads.badOuterSource = _ClassStatement.forPayload(FlowableJoinTest::badOuterSource, "badOuterSource", this);
            this.payloads.badEndSource = _ClassStatement.forPayload(FlowableJoinTest::badEndSource, "badEndSource", this);
            this.payloads.backpressureOverflowRight = _ClassStatement.forPayload(FlowableJoinTest::backpressureOverflowRight, "backpressureOverflowRight", this);
            this.payloads.backpressureOverflowLeft = _ClassStatement.forPayload(FlowableJoinTest::backpressureOverflowLeft, "backpressureOverflowLeft", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableJoinTest::badRequest, "badRequest", this);
            this.payloads.bothTerminateWithWorkRemaining = _ClassStatement.forPayload(FlowableJoinTest::bothTerminateWithWorkRemaining, "bothTerminateWithWorkRemaining", this);
        }
    }
}
