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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableTakeLastTest extends RxJavaTest {

    @Test
    public void takeLastEmpty() {
        Flowable<String> w = Flowable.empty();
        Flowable<String> take = w.takeLast(2);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        take.subscribe(subscriber);
        verify(subscriber, never()).onNext(any(String.class));
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void takeLast1() {
        Flowable<String> w = Flowable.just("one", "two", "three");
        Flowable<String> take = w.takeLast(2);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        take.subscribe(subscriber);
        inOrder.verify(subscriber, times(1)).onNext("two");
        inOrder.verify(subscriber, times(1)).onNext("three");
        verify(subscriber, never()).onNext("one");
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void takeLast2() {
        Flowable<String> w = Flowable.just("one");
        Flowable<String> take = w.takeLast(10);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        take.subscribe(subscriber);
        verify(subscriber, times(1)).onNext("one");
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void takeLastWithZeroCount() {
        Flowable<String> w = Flowable.just("one");
        Flowable<String> take = w.takeLast(0);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        take.subscribe(subscriber);
        verify(subscriber, never()).onNext("one");
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void takeLastWithNegativeCount() {
        Flowable.just("one").takeLast(-1);
    }

    @Test
    public void backpressure1() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 100000).takeLast(1).observeOn(Schedulers.newThread()).map(newSlowProcessor()).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        ts.assertValue(100000);
    }

    @Test
    public void backpressure2() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 100000).takeLast(Flowable.bufferSize() * 4).observeOn(Schedulers.newThread()).map(newSlowProcessor()).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertNoErrors();
        assertEquals(Flowable.bufferSize() * 4, ts.values().size());
    }

    private Function<Integer, Integer> newSlowProcessor() {
        return new Function<Integer, Integer>() {

            int c;

            @Override
            public Integer apply(Integer i) {
                if (c++ < 100) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }
                }
                return i;
            }
        };
    }

    @Test
    public void issue1522() {
        // https://github.com/ReactiveX/RxJava/issues/1522
        assertEquals(0, Flowable.empty().count().toFlowable().filter(new Predicate<Long>() {

            @Override
            public boolean test(Long v) {
                return false;
            }
        }).toList().blockingGet().size());
    }

    @Test
    public void ignoreRequest1() {
        // If `takeLast` does not ignore `request` properly, StackOverflowError will be thrown.
        Flowable.range(0, 100000).takeLast(100000).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(Long.MAX_VALUE);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                request(Long.MAX_VALUE);
            }
        });
    }

    @Test
    public void ignoreRequest2() {
        // If `takeLast` does not ignore `request` properly, StackOverflowError will be thrown.
        Flowable.range(0, 100000).takeLast(100000).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                request(1);
            }
        });
    }

    @Test
    public void ignoreRequest3() {
        // If `takeLast` does not ignore `request` properly, it will enter an infinite loop.
        Flowable.range(0, 100000).takeLast(100000).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                request(Long.MAX_VALUE);
            }
        });
    }

    @Test
    public void ignoreRequest4() {
        // If `takeLast` does not ignore `request` properly, StackOverflowError will be thrown.
        Flowable.range(0, 100000).takeLast(100000).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(Long.MAX_VALUE);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                request(1);
            }
        });
    }

    @Test
    public void unsubscribeTakesEffectEarlyOnFastPath() {
        final AtomicInteger count = new AtomicInteger();
        Flowable.range(0, 100000).takeLast(100000).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(Long.MAX_VALUE);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                count.incrementAndGet();
                cancel();
            }
        });
        assertEquals(1, count.get());
    }

    @Test
    public void requestOverflow() {
        final List<Integer> list = new ArrayList<>();
        Flowable.range(1, 100).takeLast(50).subscribe(new DefaultSubscriber<Integer>() {

            @Override
            public void onStart() {
                request(2);
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
                list.add(t);
                request(Long.MAX_VALUE - 1);
            }
        });
        assertEquals(50, list.size());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.range(1, 10).takeLast(5));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.takeLast(5);
            }
        });
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).takeLast(5).test().assertFailure(TestException.class);
    }

    @Test
    public void takeLastTake() {
        Flowable.range(1, 10).takeLast(5).take(2).test().assertResult(6, 7);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().takeLast(2));
    }

    @Test
    public void cancelThenRequest() {
        Flowable.never().takeLast(2).subscribe(new FlowableSubscriber<Object>() {

            @Override
            public void onNext(@NonNull Object t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onSubscribe(@NonNull Subscription s) {
                s.cancel();
                s.request(1);
            }
        });
    }

    @Test
    public void noRequestEmpty() {
        Flowable.empty().takeLast(2).test(0L).assertResult();
    }

    @Test
    public void moreValuesRemainingThanRequested() {
        Flowable.range(1, 4).takeLast(3).test(0L).assertEmpty().requestMore(2).assertValuesOnly(2, 3).requestMore(2).assertResult(2, 3, 4);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableTakeLastTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastEmpty() throws java.lang.Throwable {
            this.payloads.takeLastEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLast1() throws java.lang.Throwable {
            this.payloads.takeLast1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLast2() throws java.lang.Throwable {
            this.payloads.takeLast2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastWithZeroCount() throws java.lang.Throwable {
            this.payloads.takeLastWithZeroCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastWithNegativeCount() throws java.lang.Throwable {
            this.payloads.takeLastWithNegativeCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure1() throws java.lang.Throwable {
            this.payloads.backpressure1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure2() throws java.lang.Throwable {
            this.payloads.backpressure2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1522() throws java.lang.Throwable {
            this.payloads.issue1522.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreRequest1() throws java.lang.Throwable {
            this.payloads.ignoreRequest1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreRequest2() throws java.lang.Throwable {
            this.payloads.ignoreRequest2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreRequest3() throws java.lang.Throwable {
            this.payloads.ignoreRequest3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreRequest4() throws java.lang.Throwable {
            this.payloads.ignoreRequest4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeTakesEffectEarlyOnFastPath() throws java.lang.Throwable {
            this.payloads.unsubscribeTakesEffectEarlyOnFastPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestOverflow() throws java.lang.Throwable {
            this.payloads.requestOverflow.evaluate();
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
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTake() throws java.lang.Throwable {
            this.payloads.takeLastTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelThenRequest() throws java.lang.Throwable {
            this.payloads.cancelThenRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noRequestEmpty() throws java.lang.Throwable {
            this.payloads.noRequestEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_moreValuesRemainingThanRequested() throws java.lang.Throwable {
            this.payloads.moreValuesRemainingThanRequested.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableTakeLastTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeLastTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableTakeLastTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableTakeLastTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement takeLastEmpty;

            public org.junit.runners.model.Statement takeLast1;

            public org.junit.runners.model.Statement takeLast2;

            public org.junit.runners.model.Statement takeLastWithZeroCount;

            public org.junit.runners.model.Statement takeLastWithNegativeCount;

            public org.junit.runners.model.Statement backpressure1;

            public org.junit.runners.model.Statement backpressure2;

            public org.junit.runners.model.Statement issue1522;

            public org.junit.runners.model.Statement ignoreRequest1;

            public org.junit.runners.model.Statement ignoreRequest2;

            public org.junit.runners.model.Statement ignoreRequest3;

            public org.junit.runners.model.Statement ignoreRequest4;

            public org.junit.runners.model.Statement unsubscribeTakesEffectEarlyOnFastPath;

            public org.junit.runners.model.Statement requestOverflow;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement takeLastTake;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement cancelThenRequest;

            public org.junit.runners.model.Statement noRequestEmpty;

            public org.junit.runners.model.Statement moreValuesRemainingThanRequested;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.takeLastEmpty = _ClassStatement.forPayload(FlowableTakeLastTest::takeLastEmpty, "takeLastEmpty", this);
            this.payloads.takeLast1 = _ClassStatement.forPayload(FlowableTakeLastTest::takeLast1, "takeLast1", this);
            this.payloads.takeLast2 = _ClassStatement.forPayload(FlowableTakeLastTest::takeLast2, "takeLast2", this);
            this.payloads.takeLastWithZeroCount = _ClassStatement.forPayload(FlowableTakeLastTest::takeLastWithZeroCount, "takeLastWithZeroCount", this);
            this.payloads.takeLastWithNegativeCount = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableTakeLastTest::takeLastWithNegativeCount, java.lang.IllegalArgumentException.class), "takeLastWithNegativeCount", this);
            this.payloads.backpressure1 = _ClassStatement.forPayload(FlowableTakeLastTest::backpressure1, "backpressure1", this);
            this.payloads.backpressure2 = _ClassStatement.forPayload(FlowableTakeLastTest::backpressure2, "backpressure2", this);
            this.payloads.issue1522 = _ClassStatement.forPayload(FlowableTakeLastTest::issue1522, "issue1522", this);
            this.payloads.ignoreRequest1 = _ClassStatement.forPayload(FlowableTakeLastTest::ignoreRequest1, "ignoreRequest1", this);
            this.payloads.ignoreRequest2 = _ClassStatement.forPayload(FlowableTakeLastTest::ignoreRequest2, "ignoreRequest2", this);
            this.payloads.ignoreRequest3 = _ClassStatement.forPayload(FlowableTakeLastTest::ignoreRequest3, "ignoreRequest3", this);
            this.payloads.ignoreRequest4 = _ClassStatement.forPayload(FlowableTakeLastTest::ignoreRequest4, "ignoreRequest4", this);
            this.payloads.unsubscribeTakesEffectEarlyOnFastPath = _ClassStatement.forPayload(FlowableTakeLastTest::unsubscribeTakesEffectEarlyOnFastPath, "unsubscribeTakesEffectEarlyOnFastPath", this);
            this.payloads.requestOverflow = _ClassStatement.forPayload(FlowableTakeLastTest::requestOverflow, "requestOverflow", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableTakeLastTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableTakeLastTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableTakeLastTest::error, "error", this);
            this.payloads.takeLastTake = _ClassStatement.forPayload(FlowableTakeLastTest::takeLastTake, "takeLastTake", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableTakeLastTest::badRequest, "badRequest", this);
            this.payloads.cancelThenRequest = _ClassStatement.forPayload(FlowableTakeLastTest::cancelThenRequest, "cancelThenRequest", this);
            this.payloads.noRequestEmpty = _ClassStatement.forPayload(FlowableTakeLastTest::noRequestEmpty, "noRequestEmpty", this);
            this.payloads.moreValuesRemainingThanRequested = _ClassStatement.forPayload(FlowableTakeLastTest::moreValuesRemainingThanRequested, "moreValuesRemainingThanRequested", this);
        }
    }
}
