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
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.*;
import org.mockito.Mockito;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.operators.ConditionalSubscriber;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableFilterTest extends RxJavaTest {

    @Test
    public void filter() {
        Flowable<String> w = Flowable.just("one", "two", "three");
        Flowable<String> flowable = w.filter(new Predicate<String>() {

            @Override
            public boolean test(String t1) {
                return t1.equals("two");
            }
        });
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        flowable.subscribe(subscriber);
        verify(subscriber, Mockito.never()).onNext("one");
        verify(subscriber, times(1)).onNext("two");
        verify(subscriber, Mockito.never()).onNext("three");
        verify(subscriber, Mockito.never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
    }

    /**
     * Make sure we are adjusting subscriber.request() for filtered items.
     * @throws InterruptedException if the test is interrupted
     * @throws InterruptedException if the test is interrupted
     */
    @Test
    public void withBackpressure() throws InterruptedException {
        Flowable<String> w = Flowable.just("one", "two", "three");
        Flowable<String> f = w.filter(new Predicate<String>() {

            @Override
            public boolean test(String t1) {
                return t1.equals("three");
            }
        });
        final CountDownLatch latch = new CountDownLatch(1);
        TestSubscriber<String> ts = new TestSubscriber<String>() {

            @Override
            public void onComplete() {
                // System.out.println("onComplete");
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onNext(String t) {
                // System.out.println("Received: " + t);
                // request more each time we receive
                request(1);
            }
        };
        // this means it will only request "one" and "two", expecting to receive them before requesting more
        ts.request(2);
        f.subscribe(ts);
        // this will wait forever unless OperatorTake handles the request(n) on filtered items
        latch.await();
    }

    /**
     * Make sure we are adjusting subscriber.request() for filtered items.
     * @throws InterruptedException if the test is interrupted
     */
    @Test
    public void withBackpressure2() throws InterruptedException {
        Flowable<Integer> w = Flowable.range(1, Flowable.bufferSize() * 2);
        Flowable<Integer> f = w.filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 > 100;
            }
        });
        final CountDownLatch latch = new CountDownLatch(1);
        final TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {

            @Override
            public void onComplete() {
                // System.out.println("onComplete");
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onNext(Integer t) {
                // System.out.println("Received: " + t);
                // request more each time we receive
                request(1);
            }
        };
        // this means it will only request 1 item and expect to receive more
        ts.request(1);
        f.subscribe(ts);
        // this will wait forever unless OperatorTake handles the request(n) on filtered items
        latch.await();
    }

    @Test
    public void functionCrashUnsubscribes() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        pp.filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                throw new TestException();
            }
        }).subscribe(ts);
        Assert.assertTrue("Not subscribed?", pp.hasSubscribers());
        pp.onNext(1);
        Assert.assertFalse("Subscribed?", pp.hasSubscribers());
        ts.assertError(TestException.class);
    }

    @Test
    public void doesntRequestOnItsOwn() {
        TestSubscriber<Integer> ts = TestSubscriber.create(0L);
        Flowable.range(1, 10).filter(Functions.alwaysTrue()).subscribe(ts);
        ts.assertNoValues();
        ts.request(10);
        ts.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void conditional() {
        Flowable.range(1, 5).filter(Functions.alwaysTrue()).filter(Functions.alwaysTrue()).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void conditionalNone() {
        Flowable.range(1, 5).filter(Functions.alwaysTrue()).filter(Functions.alwaysFalse()).test().assertResult();
    }

    @Test
    public void conditionalNone2() {
        Flowable.range(1, 5).filter(Functions.alwaysFalse()).filter(Functions.alwaysFalse()).test().assertResult();
    }

    @Test
    public void conditionalFusedSync() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.range(1, 5).filter(Functions.alwaysTrue()).filter(Functions.alwaysTrue()).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void conditionalFusedSync2() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.range(1, 5).filter(Functions.alwaysFalse()).filter(Functions.alwaysFalse()).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult();
    }

    @Test
    public void conditionalFusedAsync() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        up.filter(Functions.alwaysTrue()).filter(Functions.alwaysTrue()).subscribe(ts);
        up.onNext(1);
        up.onNext(2);
        up.onNext(3);
        up.onNext(4);
        up.onNext(5);
        up.onComplete();
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void conditionalFusedNoneAsync() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        up.filter(Functions.alwaysTrue()).filter(Functions.alwaysFalse()).subscribe(ts);
        up.onNext(1);
        up.onNext(2);
        up.onNext(3);
        up.onNext(4);
        up.onNext(5);
        up.onComplete();
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void conditionalFusedNoneAsync2() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        up.filter(Functions.alwaysFalse()).filter(Functions.alwaysFalse()).subscribe(ts);
        up.onNext(1);
        up.onNext(2);
        up.onNext(3);
        up.onNext(4);
        up.onNext(5);
        up.onComplete();
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void sourceIgnoresCancelConditional() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.fromPublisher(new Publisher<Integer>() {

                @Override
                public void subscribe(Subscriber<? super Integer> s) {
                    ConditionalSubscriber<? super Integer> cs = (ConditionalSubscriber<? super Integer>) s;
                    cs.onSubscribe(new BooleanSubscription());
                    cs.tryOnNext(1);
                    cs.tryOnNext(2);
                    cs.onError(new IOException());
                    cs.onComplete();
                }
            }).filter(new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) throws Exception {
                    return true;
                }
            }).filter(new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) throws Exception {
                    throw new TestException();
                }
            }).test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void mapCrashesBeforeFilter() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.fromPublisher(new Publisher<Integer>() {

                @Override
                public void subscribe(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onNext(2);
                    s.onError(new IOException());
                    s.onComplete();
                }
            }).map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer v) throws Exception {
                    throw new TestException();
                }
            }).filter(new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) throws Exception {
                    return true;
                }
            }).test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void syncFused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.range(1, 5).filter(Functions.alwaysTrue()).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void syncNoneFused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.range(1, 5).filter(Functions.alwaysFalse()).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult();
    }

    @Test
    public void syncNoneFused2() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.range(1, 5).filter(Functions.alwaysFalse()).filter(Functions.alwaysFalse()).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult();
    }

    @Test
    public void sourceIgnoresCancel() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.fromPublisher(new Publisher<Integer>() {

                @Override
                public void subscribe(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onNext(2);
                    s.onError(new IOException());
                    s.onComplete();
                }
            }).filter(new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) throws Exception {
                    throw new TestException();
                }
            }).test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void sourceIgnoresCancel2() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.fromPublisher(new Publisher<Integer>() {

                @Override
                public void subscribe(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onNext(2);
                    s.onError(new IOException());
                    s.onComplete();
                }
            }).filter(new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) throws Exception {
                    throw new TestException();
                }
            }).filter(Functions.alwaysTrue()).test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void sourceIgnoresCancelConditional2() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.fromPublisher(new Publisher<Integer>() {

                @Override
                public void subscribe(Subscriber<? super Integer> s) {
                    ConditionalSubscriber<? super Integer> cs = (ConditionalSubscriber<? super Integer>) s;
                    cs.onSubscribe(new BooleanSubscription());
                    cs.tryOnNext(1);
                    cs.tryOnNext(2);
                    cs.onError(new IOException());
                    cs.onComplete();
                }
            }).filter(new Predicate<Integer>() {

                @Override
                public boolean test(Integer v) throws Exception {
                    throw new TestException();
                }
            }).filter(Functions.alwaysTrue()).test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.range(1, 5).filter(Functions.alwaysTrue()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.filter(Functions.alwaysTrue());
            }
        });
    }

    @Test
    public void fusedSync() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.range(1, 5).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.SYNC).assertResult(2, 4);
    }

    @Test
    public void fusedAsync() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        up.filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).subscribe(ts);
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        ts.assertFusionMode(QueueFuseable.ASYNC).assertResult(2, 4);
    }

    @Test
    public void fusedReject() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY | QueueFuseable.BOUNDARY);
        Flowable.range(1, 5).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.NONE).assertResult(2, 4);
    }

    @Test
    public void filterThrows() {
        Flowable.range(1, 5).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableFilterTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filter() throws java.lang.Throwable {
            this.payloads.filter.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withBackpressure() throws java.lang.Throwable {
            this.payloads.withBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withBackpressure2() throws java.lang.Throwable {
            this.payloads.withBackpressure2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_functionCrashUnsubscribes() throws java.lang.Throwable {
            this.payloads.functionCrashUnsubscribes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesntRequestOnItsOwn() throws java.lang.Throwable {
            this.payloads.doesntRequestOnItsOwn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditional() throws java.lang.Throwable {
            this.payloads.conditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalNone() throws java.lang.Throwable {
            this.payloads.conditionalNone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalNone2() throws java.lang.Throwable {
            this.payloads.conditionalNone2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFusedSync() throws java.lang.Throwable {
            this.payloads.conditionalFusedSync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFusedSync2() throws java.lang.Throwable {
            this.payloads.conditionalFusedSync2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFusedAsync() throws java.lang.Throwable {
            this.payloads.conditionalFusedAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFusedNoneAsync() throws java.lang.Throwable {
            this.payloads.conditionalFusedNoneAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFusedNoneAsync2() throws java.lang.Throwable {
            this.payloads.conditionalFusedNoneAsync2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceIgnoresCancelConditional() throws java.lang.Throwable {
            this.payloads.sourceIgnoresCancelConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapCrashesBeforeFilter() throws java.lang.Throwable {
            this.payloads.mapCrashesBeforeFilter.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFused() throws java.lang.Throwable {
            this.payloads.syncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncNoneFused() throws java.lang.Throwable {
            this.payloads.syncNoneFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncNoneFused2() throws java.lang.Throwable {
            this.payloads.syncNoneFused2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceIgnoresCancel() throws java.lang.Throwable {
            this.payloads.sourceIgnoresCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceIgnoresCancel2() throws java.lang.Throwable {
            this.payloads.sourceIgnoresCancel2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceIgnoresCancelConditional2() throws java.lang.Throwable {
            this.payloads.sourceIgnoresCancelConditional2.evaluate();
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
        public void benchmark_fusedSync() throws java.lang.Throwable {
            this.payloads.fusedSync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedAsync() throws java.lang.Throwable {
            this.payloads.fusedAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedReject() throws java.lang.Throwable {
            this.payloads.fusedReject.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterThrows() throws java.lang.Throwable {
            this.payloads.filterThrows.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFilterTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFilterTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFilterTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFilterTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableFilterTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFilterTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableFilterTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableFilterTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement filter;

            public org.junit.runners.model.Statement withBackpressure;

            public org.junit.runners.model.Statement withBackpressure2;

            public org.junit.runners.model.Statement functionCrashUnsubscribes;

            public org.junit.runners.model.Statement doesntRequestOnItsOwn;

            public org.junit.runners.model.Statement conditional;

            public org.junit.runners.model.Statement conditionalNone;

            public org.junit.runners.model.Statement conditionalNone2;

            public org.junit.runners.model.Statement conditionalFusedSync;

            public org.junit.runners.model.Statement conditionalFusedSync2;

            public org.junit.runners.model.Statement conditionalFusedAsync;

            public org.junit.runners.model.Statement conditionalFusedNoneAsync;

            public org.junit.runners.model.Statement conditionalFusedNoneAsync2;

            public org.junit.runners.model.Statement sourceIgnoresCancelConditional;

            public org.junit.runners.model.Statement mapCrashesBeforeFilter;

            public org.junit.runners.model.Statement syncFused;

            public org.junit.runners.model.Statement syncNoneFused;

            public org.junit.runners.model.Statement syncNoneFused2;

            public org.junit.runners.model.Statement sourceIgnoresCancel;

            public org.junit.runners.model.Statement sourceIgnoresCancel2;

            public org.junit.runners.model.Statement sourceIgnoresCancelConditional2;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement fusedSync;

            public org.junit.runners.model.Statement fusedAsync;

            public org.junit.runners.model.Statement fusedReject;

            public org.junit.runners.model.Statement filterThrows;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.filter = _ClassStatement.forPayload(FlowableFilterTest::filter, "filter", this);
            this.payloads.withBackpressure = _ClassStatement.forPayload(FlowableFilterTest::withBackpressure, "withBackpressure", this);
            this.payloads.withBackpressure2 = _ClassStatement.forPayload(FlowableFilterTest::withBackpressure2, "withBackpressure2", this);
            this.payloads.functionCrashUnsubscribes = _ClassStatement.forPayload(FlowableFilterTest::functionCrashUnsubscribes, "functionCrashUnsubscribes", this);
            this.payloads.doesntRequestOnItsOwn = _ClassStatement.forPayload(FlowableFilterTest::doesntRequestOnItsOwn, "doesntRequestOnItsOwn", this);
            this.payloads.conditional = _ClassStatement.forPayload(FlowableFilterTest::conditional, "conditional", this);
            this.payloads.conditionalNone = _ClassStatement.forPayload(FlowableFilterTest::conditionalNone, "conditionalNone", this);
            this.payloads.conditionalNone2 = _ClassStatement.forPayload(FlowableFilterTest::conditionalNone2, "conditionalNone2", this);
            this.payloads.conditionalFusedSync = _ClassStatement.forPayload(FlowableFilterTest::conditionalFusedSync, "conditionalFusedSync", this);
            this.payloads.conditionalFusedSync2 = _ClassStatement.forPayload(FlowableFilterTest::conditionalFusedSync2, "conditionalFusedSync2", this);
            this.payloads.conditionalFusedAsync = _ClassStatement.forPayload(FlowableFilterTest::conditionalFusedAsync, "conditionalFusedAsync", this);
            this.payloads.conditionalFusedNoneAsync = _ClassStatement.forPayload(FlowableFilterTest::conditionalFusedNoneAsync, "conditionalFusedNoneAsync", this);
            this.payloads.conditionalFusedNoneAsync2 = _ClassStatement.forPayload(FlowableFilterTest::conditionalFusedNoneAsync2, "conditionalFusedNoneAsync2", this);
            this.payloads.sourceIgnoresCancelConditional = _ClassStatement.forPayload(FlowableFilterTest::sourceIgnoresCancelConditional, "sourceIgnoresCancelConditional", this);
            this.payloads.mapCrashesBeforeFilter = _ClassStatement.forPayload(FlowableFilterTest::mapCrashesBeforeFilter, "mapCrashesBeforeFilter", this);
            this.payloads.syncFused = _ClassStatement.forPayload(FlowableFilterTest::syncFused, "syncFused", this);
            this.payloads.syncNoneFused = _ClassStatement.forPayload(FlowableFilterTest::syncNoneFused, "syncNoneFused", this);
            this.payloads.syncNoneFused2 = _ClassStatement.forPayload(FlowableFilterTest::syncNoneFused2, "syncNoneFused2", this);
            this.payloads.sourceIgnoresCancel = _ClassStatement.forPayload(FlowableFilterTest::sourceIgnoresCancel, "sourceIgnoresCancel", this);
            this.payloads.sourceIgnoresCancel2 = _ClassStatement.forPayload(FlowableFilterTest::sourceIgnoresCancel2, "sourceIgnoresCancel2", this);
            this.payloads.sourceIgnoresCancelConditional2 = _ClassStatement.forPayload(FlowableFilterTest::sourceIgnoresCancelConditional2, "sourceIgnoresCancelConditional2", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableFilterTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableFilterTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.fusedSync = _ClassStatement.forPayload(FlowableFilterTest::fusedSync, "fusedSync", this);
            this.payloads.fusedAsync = _ClassStatement.forPayload(FlowableFilterTest::fusedAsync, "fusedAsync", this);
            this.payloads.fusedReject = _ClassStatement.forPayload(FlowableFilterTest::fusedReject, "fusedReject", this);
            this.payloads.filterThrows = _ClassStatement.forPayload(FlowableFilterTest::filterThrows, "filterThrows", this);
        }
    }
}
