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
import java.util.*;
import org.junit.Test;
import org.mockito.InOrder;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.internal.util.CrashingMappedIterable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableWithLatestFromTest extends RxJavaTest {

    static final BiFunction<Integer, Integer, Integer> COMBINER = new BiFunction<Integer, Integer, Integer>() {

        @Override
        public Integer apply(Integer t1, Integer t2) {
            return (t1 << 8) + t2;
        }
    };

    static final BiFunction<Integer, Integer, Integer> COMBINER_ERROR = new BiFunction<Integer, Integer, Integer>() {

        @Override
        public Integer apply(Integer t1, Integer t2) {
            throw new TestException("Forced failure");
        }
    };

    @Test
    public void simple() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        result.subscribe(subscriber);
        source.onNext(1);
        inOrder.verify(subscriber, never()).onNext(anyInt());
        other.onNext(1);
        inOrder.verify(subscriber, never()).onNext(anyInt());
        source.onNext(2);
        inOrder.verify(subscriber).onNext((2 << 8) + 1);
        other.onNext(2);
        inOrder.verify(subscriber, never()).onNext(anyInt());
        other.onComplete();
        inOrder.verify(subscriber, never()).onComplete();
        source.onNext(3);
        inOrder.verify(subscriber).onNext((3 << 8) + 2);
        source.onComplete();
        inOrder.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void emptySource() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        result.subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(other.hasSubscribers());
        other.onNext(1);
        source.onComplete();
        ts.assertNoErrors();
        ts.assertTerminated();
        ts.assertNoValues();
        assertFalse(source.hasSubscribers());
        assertFalse(other.hasSubscribers());
    }

    @Test
    public void emptyOther() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        result.subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(other.hasSubscribers());
        source.onNext(1);
        source.onComplete();
        ts.assertNoErrors();
        ts.assertTerminated();
        ts.assertNoValues();
        assertFalse(source.hasSubscribers());
        assertFalse(other.hasSubscribers());
    }

    @Test
    public void unsubscription() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        result.subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(other.hasSubscribers());
        other.onNext(1);
        source.onNext(1);
        ts.cancel();
        ts.assertValue((1 << 8) + 1);
        ts.assertNoErrors();
        ts.assertNotComplete();
        assertFalse(source.hasSubscribers());
        assertFalse(other.hasSubscribers());
    }

    @Test
    public void sourceThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        result.subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(other.hasSubscribers());
        other.onNext(1);
        source.onNext(1);
        source.onError(new TestException());
        ts.assertTerminated();
        ts.assertValue((1 << 8) + 1);
        ts.assertError(TestException.class);
        ts.assertNotComplete();
        assertFalse(source.hasSubscribers());
        assertFalse(other.hasSubscribers());
    }

    @Test
    public void otherThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        result.subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(other.hasSubscribers());
        other.onNext(1);
        source.onNext(1);
        other.onError(new TestException());
        ts.assertTerminated();
        ts.assertValue((1 << 8) + 1);
        ts.assertNotComplete();
        ts.assertError(TestException.class);
        assertFalse(source.hasSubscribers());
        assertFalse(other.hasSubscribers());
    }

    @Test
    public void functionThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER_ERROR);
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        result.subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(other.hasSubscribers());
        other.onNext(1);
        source.onNext(1);
        ts.assertTerminated();
        ts.assertNotComplete();
        ts.assertNoValues();
        ts.assertError(TestException.class);
        assertFalse(source.hasSubscribers());
        assertFalse(other.hasSubscribers());
    }

    @Test
    public void noDownstreamUnsubscribe() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        result.subscribe(ts);
        source.onComplete();
        assertFalse(ts.isCancelled());
    }

    @Test
    public void backpressure() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        result.subscribe(ts);
        assertTrue("Other has no observers!", other.hasSubscribers());
        ts.request(1);
        source.onNext(1);
        assertTrue("Other has no observers!", other.hasSubscribers());
        ts.assertNoValues();
        other.onNext(1);
        source.onNext(2);
        ts.assertValue((2 << 8) + 1);
        ts.request(5);
        source.onNext(3);
        source.onNext(4);
        source.onNext(5);
        source.onNext(6);
        source.onNext(7);
        ts.assertValues((2 << 8) + 1, (3 << 8) + 1, (4 << 8) + 1, (5 << 8) + 1, (6 << 8) + 1, (7 << 8) + 1);
        ts.cancel();
        assertFalse("Other has observers!", other.hasSubscribers());
        ts.assertNoErrors();
    }

    static final Function<Object[], String> toArray = new Function<Object[], String>() {

        @Override
        public String apply(Object[] args) {
            return Arrays.toString(args);
        }
    };

    @Test
    public void manySources() {
        PublishProcessor<String> pp1 = PublishProcessor.create();
        PublishProcessor<String> pp2 = PublishProcessor.create();
        PublishProcessor<String> pp3 = PublishProcessor.create();
        PublishProcessor<String> main = PublishProcessor.create();
        TestSubscriber<String> ts = new TestSubscriber<>();
        main.withLatestFrom(new Flowable[] { pp1, pp2, pp3 }, toArray).subscribe(ts);
        main.onNext("1");
        ts.assertNoValues();
        pp1.onNext("a");
        ts.assertNoValues();
        pp2.onNext("A");
        ts.assertNoValues();
        pp3.onNext("=");
        ts.assertNoValues();
        main.onNext("2");
        ts.assertValues("[2, a, A, =]");
        pp2.onNext("B");
        ts.assertValues("[2, a, A, =]");
        pp3.onComplete();
        ts.assertValues("[2, a, A, =]");
        pp1.onNext("b");
        main.onNext("3");
        ts.assertValues("[2, a, A, =]", "[3, b, B, =]");
        main.onComplete();
        ts.assertValues("[2, a, A, =]", "[3, b, B, =]");
        ts.assertNoErrors();
        ts.assertComplete();
        assertFalse("ps1 has subscribers?", pp1.hasSubscribers());
        assertFalse("ps2 has subscribers?", pp2.hasSubscribers());
        assertFalse("ps3 has subscribers?", pp3.hasSubscribers());
    }

    @Test
    public void manySourcesIterable() {
        PublishProcessor<String> pp1 = PublishProcessor.create();
        PublishProcessor<String> pp2 = PublishProcessor.create();
        PublishProcessor<String> pp3 = PublishProcessor.create();
        PublishProcessor<String> main = PublishProcessor.create();
        TestSubscriber<String> ts = new TestSubscriber<>();
        main.withLatestFrom(Arrays.<Flowable<?>>asList(pp1, pp2, pp3), toArray).subscribe(ts);
        main.onNext("1");
        ts.assertNoValues();
        pp1.onNext("a");
        ts.assertNoValues();
        pp2.onNext("A");
        ts.assertNoValues();
        pp3.onNext("=");
        ts.assertNoValues();
        main.onNext("2");
        ts.assertValues("[2, a, A, =]");
        pp2.onNext("B");
        ts.assertValues("[2, a, A, =]");
        pp3.onComplete();
        ts.assertValues("[2, a, A, =]");
        pp1.onNext("b");
        main.onNext("3");
        ts.assertValues("[2, a, A, =]", "[3, b, B, =]");
        main.onComplete();
        ts.assertValues("[2, a, A, =]", "[3, b, B, =]");
        ts.assertNoErrors();
        ts.assertComplete();
        assertFalse("ps1 has subscribers?", pp1.hasSubscribers());
        assertFalse("ps2 has subscribers?", pp2.hasSubscribers());
        assertFalse("ps3 has subscribers?", pp3.hasSubscribers());
    }

    @Test
    public void manySourcesIterableSweep() {
        for (String val : new String[] { "1" /*, null*/
        }) {
            int n = 35;
            for (int i = 0; i < n; i++) {
                List<Flowable<?>> sources = new ArrayList<>();
                List<String> expected = new ArrayList<>();
                expected.add(val);
                for (int j = 0; j < i; j++) {
                    sources.add(Flowable.just(val));
                    expected.add(String.valueOf(val));
                }
                TestSubscriber<String> ts = new TestSubscriber<>();
                PublishProcessor<String> main = PublishProcessor.create();
                main.withLatestFrom(sources, toArray).subscribe(ts);
                ts.assertNoValues();
                main.onNext(val);
                main.onComplete();
                ts.assertValue(expected.toString());
                ts.assertNoErrors();
                ts.assertComplete();
            }
        }
    }

    @Test
    public void backpressureNoSignal() {
        PublishProcessor<String> pp1 = PublishProcessor.create();
        PublishProcessor<String> pp2 = PublishProcessor.create();
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        Flowable.range(1, 10).withLatestFrom(new Flowable<?>[] { pp1, pp2 }, toArray).subscribe(ts);
        ts.assertNoValues();
        ts.request(1);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertComplete();
        assertFalse("ps1 has subscribers?", pp1.hasSubscribers());
        assertFalse("ps2 has subscribers?", pp2.hasSubscribers());
    }

    @Test
    public void backpressureWithSignal() {
        PublishProcessor<String> pp1 = PublishProcessor.create();
        PublishProcessor<String> pp2 = PublishProcessor.create();
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        Flowable.range(1, 3).withLatestFrom(new Flowable<?>[] { pp1, pp2 }, toArray).subscribe(ts);
        ts.assertNoValues();
        pp1.onNext("1");
        pp2.onNext("1");
        ts.request(1);
        ts.assertValue("[1, 1, 1]");
        ts.request(1);
        ts.assertValues("[1, 1, 1]", "[2, 1, 1]");
        ts.request(1);
        ts.assertValues("[1, 1, 1]", "[2, 1, 1]", "[3, 1, 1]");
        ts.assertNoErrors();
        ts.assertComplete();
        assertFalse("ps1 has subscribers?", pp1.hasSubscribers());
        assertFalse("ps2 has subscribers?", pp2.hasSubscribers());
    }

    @Test
    public void withEmpty() {
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        Flowable.range(1, 3).withLatestFrom(new Flowable<?>[] { Flowable.just(1), Flowable.empty() }, toArray).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void withError() {
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        Flowable.range(1, 3).withLatestFrom(new Flowable<?>[] { Flowable.just(1), Flowable.error(new TestException()) }, toArray).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void withMainError() {
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        Flowable.error(new TestException()).withLatestFrom(new Flowable<?>[] { Flowable.just(1), Flowable.just(1) }, toArray).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void with2Others() {
        Flowable<Integer> just = Flowable.just(1);
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>();
        just.withLatestFrom(just, just, new Function3<Integer, Integer, Integer, List<Integer>>() {

            @Override
            public List<Integer> apply(Integer a, Integer b, Integer c) {
                return Arrays.asList(a, b, c);
            }
        }).subscribe(ts);
        ts.assertValue(Arrays.asList(1, 1, 1));
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void with3Others() {
        Flowable<Integer> just = Flowable.just(1);
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>();
        just.withLatestFrom(just, just, just, new Function4<Integer, Integer, Integer, Integer, List<Integer>>() {

            @Override
            public List<Integer> apply(Integer a, Integer b, Integer c, Integer d) {
                return Arrays.asList(a, b, c, d);
            }
        }).subscribe(ts);
        ts.assertValue(Arrays.asList(1, 1, 1, 1));
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void with4Others() {
        Flowable<Integer> just = Flowable.just(1);
        TestSubscriber<List<Integer>> ts = new TestSubscriber<>();
        just.withLatestFrom(just, just, just, just, new Function5<Integer, Integer, Integer, Integer, Integer, List<Integer>>() {

            @Override
            public List<Integer> apply(Integer a, Integer b, Integer c, Integer d, Integer e) {
                return Arrays.asList(a, b, c, d, e);
            }
        }).subscribe(ts);
        ts.assertValue(Arrays.asList(1, 1, 1, 1, 1));
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).withLatestFrom(Flowable.just(2), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                return a;
            }
        }));
        TestHelper.checkDisposed(Flowable.just(1).withLatestFrom(Flowable.just(2), Flowable.just(3), new Function3<Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c) throws Exception {
                return a;
            }
        }));
    }

    @Test
    public void manyIteratorThrows() {
        Flowable.just(1).withLatestFrom(new CrashingMappedIterable<>(1, 100, 100, new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer v) throws Exception {
                return Flowable.just(2);
            }
        }), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) throws Exception {
                return a;
            }
        }).to(TestHelper.testConsumer()).assertFailureAndMessage(TestException.class, "iterator()");
    }

    @Test
    public void manyCombinerThrows() {
        Flowable.just(1).withLatestFrom(Flowable.just(2), Flowable.just(3), new Function3<Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void manyErrors() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onError(new TestException("First"));
                    subscriber.onNext(1);
                    subscriber.onError(new TestException("Second"));
                    subscriber.onComplete();
                }
            }.withLatestFrom(Flowable.just(2), Flowable.just(3), new Function3<Integer, Integer, Integer, Object>() {

                @Override
                public Object apply(Integer a, Integer b, Integer c) throws Exception {
                    return a;
                }
            }).to(TestHelper.testConsumer()).assertFailureAndMessage(TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void otherErrors() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.just(1).withLatestFrom(new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onError(new TestException("First"));
                    s.onError(new TestException("Second"));
                }
            }, new BiFunction<Integer, Integer, Integer>() {

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
    public void combineToNull1() {
        Flowable.just(1).withLatestFrom(Flowable.just(2), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void combineToNull2() {
        Flowable.just(1).withLatestFrom(Arrays.asList(Flowable.just(2), Flowable.just(3)), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] o) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void zeroOtherCombinerReturnsNull() {
        Flowable.just(1).withLatestFrom(new Flowable[0], Functions.justFunction(null)).to(TestHelper.testConsumer()).assertFailureAndMessage(NullPointerException.class, "The combiner returned a null value");
    }

    @Test
    public void singleRequestNotForgottenWhenNoData() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> result = source.withLatestFrom(other, COMBINER);
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        result.subscribe(ts);
        ts.request(1);
        source.onNext(1);
        ts.assertNoValues();
        other.onNext(1);
        ts.assertNoValues();
        source.onNext(2);
        ts.assertValue((2 << 8) + 1);
    }

    @Test
    public void coldSourceConsumedWithoutOther() {
        Flowable.range(1, 10).withLatestFrom(Flowable.never(), new BiFunction<Integer, Object, Object>() {

            @Override
            public Object apply(Integer a, Object b) throws Exception {
                return a;
            }
        }).test(1).assertResult();
    }

    @Test
    public void coldSourceConsumedWithoutManyOthers() {
        Flowable.range(1, 10).withLatestFrom(Flowable.never(), Flowable.never(), Flowable.never(), new Function4<Integer, Object, Object, Object, Object>() {

            @Override
            public Object apply(Integer a, Object b, Object c, Object d) throws Exception {
                return a;
            }
        }).test(1).assertResult();
    }

    @Test
    public void otherOnSubscribeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp0 = PublishProcessor.create();
            final PublishProcessor<Integer> pp1 = PublishProcessor.create();
            final PublishProcessor<Integer> pp2 = PublishProcessor.create();
            final PublishProcessor<Integer> pp3 = PublishProcessor.create();
            final Flowable<Object> source = pp0.withLatestFrom(pp1, pp2, pp3, new Function4<Object, Integer, Integer, Integer, Object>() {

                @Override
                public Object apply(Object a, Integer b, Integer c, Integer d) throws Exception {
                    return a;
                }
            });
            final TestSubscriber<Object> ts = new TestSubscriber<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    source.subscribe(ts);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    ts.cancel();
                }
            };
            TestHelper.race(r1, r2);
            ts.assertEmpty();
            assertFalse(pp0.hasSubscribers());
            assertFalse(pp1.hasSubscribers());
            assertFalse(pp2.hasSubscribers());
            assertFalse(pp3.hasSubscribers());
        }
    }

    @Test
    public void otherCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp0 = PublishProcessor.create();
            final PublishProcessor<Integer> pp1 = PublishProcessor.create();
            final PublishProcessor<Integer> pp2 = PublishProcessor.create();
            final PublishProcessor<Integer> pp3 = PublishProcessor.create();
            final Flowable<Object> source = pp0.withLatestFrom(pp1, pp2, pp3, new Function4<Object, Integer, Integer, Integer, Object>() {

                @Override
                public Object apply(Object a, Integer b, Integer c, Integer d) throws Exception {
                    return a;
                }
            });
            final TestSubscriber<Object> ts = new TestSubscriber<>();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    source.subscribe(ts);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    pp1.onComplete();
                }
            };
            TestHelper.race(r1, r2);
            ts.assertResult();
            assertFalse(pp0.hasSubscribers());
            assertFalse(pp1.hasSubscribers());
            assertFalse(pp2.hasSubscribers());
            assertFalse(pp3.hasSubscribers());
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableWithLatestFromTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.payloads.simple.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptySource() throws java.lang.Throwable {
            this.payloads.emptySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyOther() throws java.lang.Throwable {
            this.payloads.emptyOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscription() throws java.lang.Throwable {
            this.payloads.unsubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceThrows() throws java.lang.Throwable {
            this.payloads.sourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherThrows() throws java.lang.Throwable {
            this.payloads.otherThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_functionThrows() throws java.lang.Throwable {
            this.payloads.functionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noDownstreamUnsubscribe() throws java.lang.Throwable {
            this.payloads.noDownstreamUnsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.payloads.backpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manySources() throws java.lang.Throwable {
            this.payloads.manySources.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manySourcesIterable() throws java.lang.Throwable {
            this.payloads.manySourcesIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manySourcesIterableSweep() throws java.lang.Throwable {
            this.payloads.manySourcesIterableSweep.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNoSignal() throws java.lang.Throwable {
            this.payloads.backpressureNoSignal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithSignal() throws java.lang.Throwable {
            this.payloads.backpressureWithSignal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withEmpty() throws java.lang.Throwable {
            this.payloads.withEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withError() throws java.lang.Throwable {
            this.payloads.withError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withMainError() throws java.lang.Throwable {
            this.payloads.withMainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_with2Others() throws java.lang.Throwable {
            this.payloads.with2Others.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_with3Others() throws java.lang.Throwable {
            this.payloads.with3Others.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_with4Others() throws java.lang.Throwable {
            this.payloads.with4Others.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyIteratorThrows() throws java.lang.Throwable {
            this.payloads.manyIteratorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyCombinerThrows() throws java.lang.Throwable {
            this.payloads.manyCombinerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyErrors() throws java.lang.Throwable {
            this.payloads.manyErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherErrors() throws java.lang.Throwable {
            this.payloads.otherErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineToNull1() throws java.lang.Throwable {
            this.payloads.combineToNull1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_combineToNull2() throws java.lang.Throwable {
            this.payloads.combineToNull2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zeroOtherCombinerReturnsNull() throws java.lang.Throwable {
            this.payloads.zeroOtherCombinerReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleRequestNotForgottenWhenNoData() throws java.lang.Throwable {
            this.payloads.singleRequestNotForgottenWhenNoData.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_coldSourceConsumedWithoutOther() throws java.lang.Throwable {
            this.payloads.coldSourceConsumedWithoutOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_coldSourceConsumedWithoutManyOthers() throws java.lang.Throwable {
            this.payloads.coldSourceConsumedWithoutManyOthers.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherOnSubscribeRace() throws java.lang.Throwable {
            this.payloads.otherOnSubscribeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherCompleteRace() throws java.lang.Throwable {
            this.payloads.otherCompleteRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWithLatestFromTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWithLatestFromTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWithLatestFromTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWithLatestFromTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableWithLatestFromTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWithLatestFromTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableWithLatestFromTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableWithLatestFromTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement simple;

            public org.junit.runners.model.Statement emptySource;

            public org.junit.runners.model.Statement emptyOther;

            public org.junit.runners.model.Statement unsubscription;

            public org.junit.runners.model.Statement sourceThrows;

            public org.junit.runners.model.Statement otherThrows;

            public org.junit.runners.model.Statement functionThrows;

            public org.junit.runners.model.Statement noDownstreamUnsubscribe;

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement manySources;

            public org.junit.runners.model.Statement manySourcesIterable;

            public org.junit.runners.model.Statement manySourcesIterableSweep;

            public org.junit.runners.model.Statement backpressureNoSignal;

            public org.junit.runners.model.Statement backpressureWithSignal;

            public org.junit.runners.model.Statement withEmpty;

            public org.junit.runners.model.Statement withError;

            public org.junit.runners.model.Statement withMainError;

            public org.junit.runners.model.Statement with2Others;

            public org.junit.runners.model.Statement with3Others;

            public org.junit.runners.model.Statement with4Others;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement manyIteratorThrows;

            public org.junit.runners.model.Statement manyCombinerThrows;

            public org.junit.runners.model.Statement manyErrors;

            public org.junit.runners.model.Statement otherErrors;

            public org.junit.runners.model.Statement combineToNull1;

            public org.junit.runners.model.Statement combineToNull2;

            public org.junit.runners.model.Statement zeroOtherCombinerReturnsNull;

            public org.junit.runners.model.Statement singleRequestNotForgottenWhenNoData;

            public org.junit.runners.model.Statement coldSourceConsumedWithoutOther;

            public org.junit.runners.model.Statement coldSourceConsumedWithoutManyOthers;

            public org.junit.runners.model.Statement otherOnSubscribeRace;

            public org.junit.runners.model.Statement otherCompleteRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.simple = _ClassStatement.forPayload(FlowableWithLatestFromTest::simple, "simple", this);
            this.payloads.emptySource = _ClassStatement.forPayload(FlowableWithLatestFromTest::emptySource, "emptySource", this);
            this.payloads.emptyOther = _ClassStatement.forPayload(FlowableWithLatestFromTest::emptyOther, "emptyOther", this);
            this.payloads.unsubscription = _ClassStatement.forPayload(FlowableWithLatestFromTest::unsubscription, "unsubscription", this);
            this.payloads.sourceThrows = _ClassStatement.forPayload(FlowableWithLatestFromTest::sourceThrows, "sourceThrows", this);
            this.payloads.otherThrows = _ClassStatement.forPayload(FlowableWithLatestFromTest::otherThrows, "otherThrows", this);
            this.payloads.functionThrows = _ClassStatement.forPayload(FlowableWithLatestFromTest::functionThrows, "functionThrows", this);
            this.payloads.noDownstreamUnsubscribe = _ClassStatement.forPayload(FlowableWithLatestFromTest::noDownstreamUnsubscribe, "noDownstreamUnsubscribe", this);
            this.payloads.backpressure = _ClassStatement.forPayload(FlowableWithLatestFromTest::backpressure, "backpressure", this);
            this.payloads.manySources = _ClassStatement.forPayload(FlowableWithLatestFromTest::manySources, "manySources", this);
            this.payloads.manySourcesIterable = _ClassStatement.forPayload(FlowableWithLatestFromTest::manySourcesIterable, "manySourcesIterable", this);
            this.payloads.manySourcesIterableSweep = _ClassStatement.forPayload(FlowableWithLatestFromTest::manySourcesIterableSweep, "manySourcesIterableSweep", this);
            this.payloads.backpressureNoSignal = _ClassStatement.forPayload(FlowableWithLatestFromTest::backpressureNoSignal, "backpressureNoSignal", this);
            this.payloads.backpressureWithSignal = _ClassStatement.forPayload(FlowableWithLatestFromTest::backpressureWithSignal, "backpressureWithSignal", this);
            this.payloads.withEmpty = _ClassStatement.forPayload(FlowableWithLatestFromTest::withEmpty, "withEmpty", this);
            this.payloads.withError = _ClassStatement.forPayload(FlowableWithLatestFromTest::withError, "withError", this);
            this.payloads.withMainError = _ClassStatement.forPayload(FlowableWithLatestFromTest::withMainError, "withMainError", this);
            this.payloads.with2Others = _ClassStatement.forPayload(FlowableWithLatestFromTest::with2Others, "with2Others", this);
            this.payloads.with3Others = _ClassStatement.forPayload(FlowableWithLatestFromTest::with3Others, "with3Others", this);
            this.payloads.with4Others = _ClassStatement.forPayload(FlowableWithLatestFromTest::with4Others, "with4Others", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableWithLatestFromTest::dispose, "dispose", this);
            this.payloads.manyIteratorThrows = _ClassStatement.forPayload(FlowableWithLatestFromTest::manyIteratorThrows, "manyIteratorThrows", this);
            this.payloads.manyCombinerThrows = _ClassStatement.forPayload(FlowableWithLatestFromTest::manyCombinerThrows, "manyCombinerThrows", this);
            this.payloads.manyErrors = _ClassStatement.forPayload(FlowableWithLatestFromTest::manyErrors, "manyErrors", this);
            this.payloads.otherErrors = _ClassStatement.forPayload(FlowableWithLatestFromTest::otherErrors, "otherErrors", this);
            this.payloads.combineToNull1 = _ClassStatement.forPayload(FlowableWithLatestFromTest::combineToNull1, "combineToNull1", this);
            this.payloads.combineToNull2 = _ClassStatement.forPayload(FlowableWithLatestFromTest::combineToNull2, "combineToNull2", this);
            this.payloads.zeroOtherCombinerReturnsNull = _ClassStatement.forPayload(FlowableWithLatestFromTest::zeroOtherCombinerReturnsNull, "zeroOtherCombinerReturnsNull", this);
            this.payloads.singleRequestNotForgottenWhenNoData = _ClassStatement.forPayload(FlowableWithLatestFromTest::singleRequestNotForgottenWhenNoData, "singleRequestNotForgottenWhenNoData", this);
            this.payloads.coldSourceConsumedWithoutOther = _ClassStatement.forPayload(FlowableWithLatestFromTest::coldSourceConsumedWithoutOther, "coldSourceConsumedWithoutOther", this);
            this.payloads.coldSourceConsumedWithoutManyOthers = _ClassStatement.forPayload(FlowableWithLatestFromTest::coldSourceConsumedWithoutManyOthers, "coldSourceConsumedWithoutManyOthers", this);
            this.payloads.otherOnSubscribeRace = _ClassStatement.forPayload(FlowableWithLatestFromTest::otherOnSubscribeRace, "otherOnSubscribeRace", this);
            this.payloads.otherCompleteRace = _ClassStatement.forPayload(FlowableWithLatestFromTest::otherCompleteRace, "otherCompleteRace", this);
        }
    }
}
