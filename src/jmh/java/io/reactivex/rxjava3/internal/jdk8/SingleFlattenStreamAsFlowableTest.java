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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.*;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.QueueSubscription;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class SingleFlattenStreamAsFlowableTest extends RxJavaTest {

    @Test
    public void successJust() {
        Single.just(1).flattenStreamAsFlowable(Stream::of).test().assertResult(1);
    }

    @Test
    public void successEmpty() {
        Single.just(1).flattenStreamAsFlowable(v -> Stream.of()).test().assertResult();
    }

    @Test
    public void successMany() {
        Single.just(1).flattenStreamAsFlowable(v -> Stream.of(2, 3, 4, 5, 6)).test().assertResult(2, 3, 4, 5, 6);
    }

    @Test
    public void successManyTake() {
        Single.just(1).flattenStreamAsFlowable(v -> Stream.of(2, 3, 4, 5, 6)).take(3).test().assertResult(2, 3, 4);
    }

    @Test
    public void error() throws Throwable {
        @SuppressWarnings("unchecked")
        Function<? super Integer, Stream<? extends Integer>> f = mock(Function.class);
        Single.<Integer>error(new TestException()).flattenStreamAsFlowable(f).test().assertFailure(TestException.class);
        verify(f, never()).apply(any());
    }

    @Test
    public void mapperCrash() {
        Single.just(1).flattenStreamAsFlowable(v -> {
            throw new TestException();
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Single.never().flattenStreamAsFlowable(Stream::of));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingleToFlowable(m -> m.flattenStreamAsFlowable(Stream::of));
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(SingleSubject.create().flattenStreamAsFlowable(Stream::of));
    }

    @Test
    public void fusedEmpty() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ANY);
        Single.just(1).flattenStreamAsFlowable(v -> Stream.<Integer>of()).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void fusedJust() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ANY);
        Single.just(1).flattenStreamAsFlowable(v -> Stream.<Integer>of(v)).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1);
    }

    @Test
    public void fusedMany() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ANY);
        Single.just(1).flattenStreamAsFlowable(v -> Stream.<Integer>of(v, v + 1, v + 2)).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3);
    }

    @Test
    public void fusedManyRejected() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.SYNC);
        Single.just(1).flattenStreamAsFlowable(v -> Stream.<Integer>of(v, v + 1, v + 2)).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3);
    }

    @Test
    public void manyBackpressured() {
        Single.just(1).flattenStreamAsFlowable(v -> IntStream.rangeClosed(1, 5).boxed()).test(0L).assertEmpty().requestMore(2).assertValuesOnly(1, 2).requestMore(2).assertValuesOnly(1, 2, 3, 4).requestMore(1).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void manyBackpressured2() {
        Single.just(1).flattenStreamAsFlowable(v -> IntStream.rangeClosed(1, 5).boxed()).rebatchRequests(1).test(0L).assertEmpty().requestMore(2).assertValuesOnly(1, 2).requestMore(2).assertValuesOnly(1, 2, 3, 4).requestMore(1).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void fusedStreamAvailableLater() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        ts.setInitialFusionMode(QueueFuseable.ANY);
        SingleSubject<Integer> ss = SingleSubject.create();
        ss.flattenStreamAsFlowable(v -> Stream.<Integer>of(v, v + 1, v + 2)).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertEmpty();
        ss.onSuccess(1);
        ts.assertResult(1, 2, 3);
    }

    @Test
    public void fused() throws Throwable {
        AtomicReference<QueueSubscription<Integer>> qsr = new AtomicReference<>();
        SingleSubject<Integer> ss = SingleSubject.create();
        ss.flattenStreamAsFlowable(Stream::of).subscribe(new FlowableSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onSubscribe(@NonNull Subscription s) {
                qsr.set((QueueSubscription<Integer>) s);
            }
        });
        QueueSubscription<Integer> qs = qsr.get();
        assertEquals(QueueFuseable.ASYNC, qs.requestFusion(QueueFuseable.ASYNC));
        assertTrue(qs.isEmpty());
        assertNull(qs.poll());
        ss.onSuccess(1);
        assertFalse(qs.isEmpty());
        assertEquals(1, qs.poll().intValue());
        assertTrue(qs.isEmpty());
        assertNull(qs.poll());
        qs.cancel();
        assertTrue(qs.isEmpty());
        assertNull(qs.poll());
    }

    @Test
    public void requestOneByOne() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Single.just(1).flattenStreamAsFlowable(v -> Stream.of(1, 2, 3, 4, 5)).subscribe(new FlowableSubscriber<Integer>() {

            Subscription upstream;

            @Override
            public void onSubscribe(@NonNull Subscription s) {
                ts.onSubscribe(new BooleanSubscription());
                upstream = s;
                s.request(1);
            }

            @Override
            public void onNext(Integer t) {
                ts.onNext(t);
                upstream.request(1);
            }

            @Override
            public void onError(Throwable t) {
                ts.onError(t);
            }

            @Override
            public void onComplete() {
                ts.onComplete();
            }
        });
        ts.assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void streamCloseCrash() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Single.just(1).flattenStreamAsFlowable(v -> Stream.of(v).onClose(() -> {
                throw new TestException();
            })).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void hasNextThrowsInDrain() {
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            int count;

            @Override
            public boolean hasNext() {
                if (count++ > 0) {
                    throw new TestException();
                }
                return true;
            }

            @Override
            public Integer next() {
                return 1;
            }
        });
        Single.just(1).flattenStreamAsFlowable(v -> stream).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void nextThrowsInDrain() {
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                throw new TestException();
            }
        });
        Single.just(1).flattenStreamAsFlowable(v -> stream).test().assertFailure(TestException.class);
    }

    @Test
    public void cancelAfterHasNextInDrain() {
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            int count;

            @Override
            public boolean hasNext() {
                if (count++ > 0) {
                    ts.cancel();
                }
                return true;
            }

            @Override
            public Integer next() {
                return 1;
            }
        });
        Single.just(1).flattenStreamAsFlowable(v -> stream).subscribeWith(ts).assertValuesOnly(1);
    }

    @Test
    public void cancelAfterNextInDrain() {
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                ts.cancel();
                return 1;
            }
        });
        Single.just(1).flattenStreamAsFlowable(v -> stream).subscribeWith(ts).assertEmpty();
    }

    @Test
    public void requestSuccessRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            SingleSubject<Integer> ss = SingleSubject.create();
            TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
            ss.flattenStreamAsFlowable(Stream::of).subscribe(ts);
            Runnable r1 = () -> ss.onSuccess(1);
            Runnable r2 = () -> ts.request(1);
            TestHelper.race(r1, r2);
            ts.assertResult(1);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleFlattenStreamAsFlowableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successJust() throws java.lang.Throwable {
            this.payloads.successJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successEmpty() throws java.lang.Throwable {
            this.payloads.successEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successMany() throws java.lang.Throwable {
            this.payloads.successMany.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successManyTake() throws java.lang.Throwable {
            this.payloads.successManyTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperCrash() throws java.lang.Throwable {
            this.payloads.mapperCrash.evaluate();
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
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedEmpty() throws java.lang.Throwable {
            this.payloads.fusedEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedJust() throws java.lang.Throwable {
            this.payloads.fusedJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedMany() throws java.lang.Throwable {
            this.payloads.fusedMany.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedManyRejected() throws java.lang.Throwable {
            this.payloads.fusedManyRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyBackpressured() throws java.lang.Throwable {
            this.payloads.manyBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyBackpressured2() throws java.lang.Throwable {
            this.payloads.manyBackpressured2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedStreamAvailableLater() throws java.lang.Throwable {
            this.payloads.fusedStreamAvailableLater.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.payloads.fused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestOneByOne() throws java.lang.Throwable {
            this.payloads.requestOneByOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_streamCloseCrash() throws java.lang.Throwable {
            this.payloads.streamCloseCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrowsInDrain() throws java.lang.Throwable {
            this.payloads.hasNextThrowsInDrain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextThrowsInDrain() throws java.lang.Throwable {
            this.payloads.nextThrowsInDrain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterHasNextInDrain() throws java.lang.Throwable {
            this.payloads.cancelAfterHasNextInDrain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterNextInDrain() throws java.lang.Throwable {
            this.payloads.cancelAfterNextInDrain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestSuccessRace() throws java.lang.Throwable {
            this.payloads.requestSuccessRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlattenStreamAsFlowableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlattenStreamAsFlowableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlattenStreamAsFlowableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlattenStreamAsFlowableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleFlattenStreamAsFlowableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlattenStreamAsFlowableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleFlattenStreamAsFlowableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleFlattenStreamAsFlowableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement successJust;

            public org.junit.runners.model.Statement successEmpty;

            public org.junit.runners.model.Statement successMany;

            public org.junit.runners.model.Statement successManyTake;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement mapperCrash;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement fusedEmpty;

            public org.junit.runners.model.Statement fusedJust;

            public org.junit.runners.model.Statement fusedMany;

            public org.junit.runners.model.Statement fusedManyRejected;

            public org.junit.runners.model.Statement manyBackpressured;

            public org.junit.runners.model.Statement manyBackpressured2;

            public org.junit.runners.model.Statement fusedStreamAvailableLater;

            public org.junit.runners.model.Statement fused;

            public org.junit.runners.model.Statement requestOneByOne;

            public org.junit.runners.model.Statement streamCloseCrash;

            public org.junit.runners.model.Statement hasNextThrowsInDrain;

            public org.junit.runners.model.Statement nextThrowsInDrain;

            public org.junit.runners.model.Statement cancelAfterHasNextInDrain;

            public org.junit.runners.model.Statement cancelAfterNextInDrain;

            public org.junit.runners.model.Statement requestSuccessRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.successJust = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::successJust, "successJust", this);
            this.payloads.successEmpty = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::successEmpty, "successEmpty", this);
            this.payloads.successMany = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::successMany, "successMany", this);
            this.payloads.successManyTake = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::successManyTake, "successManyTake", this);
            this.payloads.error = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::error, "error", this);
            this.payloads.mapperCrash = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::mapperCrash, "mapperCrash", this);
            this.payloads.dispose = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badRequest = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::badRequest, "badRequest", this);
            this.payloads.fusedEmpty = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::fusedEmpty, "fusedEmpty", this);
            this.payloads.fusedJust = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::fusedJust, "fusedJust", this);
            this.payloads.fusedMany = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::fusedMany, "fusedMany", this);
            this.payloads.fusedManyRejected = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::fusedManyRejected, "fusedManyRejected", this);
            this.payloads.manyBackpressured = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::manyBackpressured, "manyBackpressured", this);
            this.payloads.manyBackpressured2 = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::manyBackpressured2, "manyBackpressured2", this);
            this.payloads.fusedStreamAvailableLater = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::fusedStreamAvailableLater, "fusedStreamAvailableLater", this);
            this.payloads.fused = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::fused, "fused", this);
            this.payloads.requestOneByOne = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::requestOneByOne, "requestOneByOne", this);
            this.payloads.streamCloseCrash = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::streamCloseCrash, "streamCloseCrash", this);
            this.payloads.hasNextThrowsInDrain = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::hasNextThrowsInDrain, "hasNextThrowsInDrain", this);
            this.payloads.nextThrowsInDrain = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::nextThrowsInDrain, "nextThrowsInDrain", this);
            this.payloads.cancelAfterHasNextInDrain = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::cancelAfterHasNextInDrain, "cancelAfterHasNextInDrain", this);
            this.payloads.cancelAfterNextInDrain = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::cancelAfterNextInDrain, "cancelAfterNextInDrain", this);
            this.payloads.requestSuccessRace = _ClassStatement.forPayload(SingleFlattenStreamAsFlowableTest::requestSuccessRace, "requestSuccessRace", this);
        }
    }
}
