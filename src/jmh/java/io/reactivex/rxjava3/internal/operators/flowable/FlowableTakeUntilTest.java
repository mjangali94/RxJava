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
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableTakeUntilTest extends RxJavaTest {

    @Test
    public void takeUntil() {
        Subscription sSource = mock(Subscription.class);
        Subscription sOther = mock(Subscription.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Subscriber<String> result = TestHelper.mockSubscriber();
        Flowable<String> stringObservable = Flowable.unsafeCreate(source).takeUntil(Flowable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        other.sendOnNext("three");
        source.sendOnNext("four");
        source.sendOnCompleted();
        other.sendOnCompleted();
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(0)).onNext("four");
        verify(sSource, times(1)).cancel();
        verify(sOther, times(1)).cancel();
    }

    @Test
    public void takeUntilSourceCompleted() {
        Subscription sSource = mock(Subscription.class);
        Subscription sOther = mock(Subscription.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Subscriber<String> result = TestHelper.mockSubscriber();
        Flowable<String> stringObservable = Flowable.unsafeCreate(source).takeUntil(Flowable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        source.sendOnCompleted();
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(sSource, never()).cancel();
        verify(sOther, times(1)).cancel();
    }

    @Test
    public void takeUntilSourceError() {
        Subscription sSource = mock(Subscription.class);
        Subscription sOther = mock(Subscription.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Throwable error = new Throwable();
        Subscriber<String> result = TestHelper.mockSubscriber();
        Flowable<String> stringObservable = Flowable.unsafeCreate(source).takeUntil(Flowable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        source.sendOnError(error);
        source.sendOnNext("three");
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(1)).onError(error);
        verify(sSource, never()).cancel();
        verify(sOther, times(1)).cancel();
    }

    @Test
    public void takeUntilOtherError() {
        Subscription sSource = mock(Subscription.class);
        Subscription sOther = mock(Subscription.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Throwable error = new Throwable();
        Subscriber<String> result = TestHelper.mockSubscriber();
        Flowable<String> stringObservable = Flowable.unsafeCreate(source).takeUntil(Flowable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        other.sendOnError(error);
        source.sendOnNext("three");
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(1)).onError(error);
        verify(result, times(0)).onComplete();
        verify(sSource, times(1)).cancel();
        verify(sOther, never()).cancel();
    }

    /**
     * If the 'other' onCompletes then we unsubscribe from the source and onComplete.
     */
    @Test
    public void takeUntilOtherCompleted() {
        Subscription sSource = mock(Subscription.class);
        Subscription sOther = mock(Subscription.class);
        TestObservable source = new TestObservable(sSource);
        TestObservable other = new TestObservable(sOther);
        Subscriber<String> result = TestHelper.mockSubscriber();
        Flowable<String> stringObservable = Flowable.unsafeCreate(source).takeUntil(Flowable.unsafeCreate(other));
        stringObservable.subscribe(result);
        source.sendOnNext("one");
        source.sendOnNext("two");
        other.sendOnCompleted();
        source.sendOnNext("three");
        verify(result, times(1)).onNext("one");
        verify(result, times(1)).onNext("two");
        verify(result, times(0)).onNext("three");
        verify(result, times(1)).onComplete();
        verify(sSource, times(1)).cancel();
        // unsubscribed since SafeSubscriber unsubscribes after onComplete
        verify(sOther, never()).cancel();
    }

    private static class TestObservable implements Publisher<String> {

        Subscriber<? super String> subscriber;

        Subscription upstream;

        TestObservable(Subscription s) {
            this.upstream = s;
        }

        /* used to simulate subscription */
        public void sendOnCompleted() {
            subscriber.onComplete();
        }

        /* used to simulate subscription */
        public void sendOnNext(String value) {
            subscriber.onNext(value);
        }

        /* used to simulate subscription */
        public void sendOnError(Throwable e) {
            subscriber.onError(e);
        }

        @Override
        public void subscribe(Subscriber<? super String> subscriber) {
            this.subscriber = subscriber;
            subscriber.onSubscribe(upstream);
        }
    }

    @Test
    public void untilFires() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> until = PublishProcessor.create();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        source.takeUntil(until).subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(until.hasSubscribers());
        source.onNext(1);
        ts.assertValue(1);
        until.onNext(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertTerminated();
        assertFalse("Source still has observers", source.hasSubscribers());
        assertFalse("Until still has observers", until.hasSubscribers());
        assertFalse("TestSubscriber is unsubscribed", ts.isCancelled());
    }

    @Test
    public void mainCompletes() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> until = PublishProcessor.create();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        source.takeUntil(until).subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(until.hasSubscribers());
        source.onNext(1);
        source.onComplete();
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertTerminated();
        assertFalse("Source still has observers", source.hasSubscribers());
        assertFalse("Until still has observers", until.hasSubscribers());
        assertFalse("TestSubscriber is unsubscribed", ts.isCancelled());
    }

    @Test
    public void downstreamUnsubscribes() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> until = PublishProcessor.create();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        source.takeUntil(until).take(1).subscribe(ts);
        assertTrue(source.hasSubscribers());
        assertTrue(until.hasSubscribers());
        source.onNext(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertTerminated();
        assertFalse("Source still has observers", source.hasSubscribers());
        assertFalse("Until still has observers", until.hasSubscribers());
        assertFalse("TestSubscriber is unsubscribed", ts.isCancelled());
    }

    @Test
    public void backpressure() {
        PublishProcessor<Integer> until = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        Flowable.range(1, 10).takeUntil(until).subscribe(ts);
        assertTrue(until.hasSubscribers());
        ts.request(1);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertNotComplete();
        until.onNext(5);
        ts.assertComplete();
        ts.assertNoErrors();
        assertFalse("Until still has observers", until.hasSubscribers());
        assertFalse("TestSubscriber is unsubscribed", ts.isCancelled());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().takeUntil(Flowable.never()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Integer> c) throws Exception {
                return c.takeUntil(Flowable.never());
            }
        });
    }

    @Test
    public void untilPublisherMainSuccess() {
        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = main.takeUntil(other).test();
        assertTrue("Main no subscribers?", main.hasSubscribers());
        assertTrue("Other no subscribers?", other.hasSubscribers());
        main.onNext(1);
        main.onNext(2);
        main.onComplete();
        assertFalse("Main has subscribers?", main.hasSubscribers());
        assertFalse("Other has subscribers?", other.hasSubscribers());
        ts.assertResult(1, 2);
    }

    @Test
    public void untilPublisherMainComplete() {
        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = main.takeUntil(other).test();
        assertTrue("Main no subscribers?", main.hasSubscribers());
        assertTrue("Other no subscribers?", other.hasSubscribers());
        main.onComplete();
        assertFalse("Main has subscribers?", main.hasSubscribers());
        assertFalse("Other has subscribers?", other.hasSubscribers());
        ts.assertResult();
    }

    @Test
    public void untilPublisherMainError() {
        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = main.takeUntil(other).test();
        assertTrue("Main no subscribers?", main.hasSubscribers());
        assertTrue("Other no subscribers?", other.hasSubscribers());
        main.onError(new TestException());
        assertFalse("Main has subscribers?", main.hasSubscribers());
        assertFalse("Other has subscribers?", other.hasSubscribers());
        ts.assertFailure(TestException.class);
    }

    @Test
    public void untilPublisherOtherOnNext() {
        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = main.takeUntil(other).test();
        assertTrue("Main no subscribers?", main.hasSubscribers());
        assertTrue("Other no subscribers?", other.hasSubscribers());
        other.onNext(1);
        assertFalse("Main has subscribers?", main.hasSubscribers());
        assertFalse("Other has subscribers?", other.hasSubscribers());
        ts.assertResult();
    }

    @Test
    public void untilPublisherOtherOnComplete() {
        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = main.takeUntil(other).test();
        assertTrue("Main no subscribers?", main.hasSubscribers());
        assertTrue("Other no subscribers?", other.hasSubscribers());
        other.onComplete();
        assertFalse("Main has subscribers?", main.hasSubscribers());
        assertFalse("Other has subscribers?", other.hasSubscribers());
        ts.assertResult();
    }

    @Test
    public void untilPublisherOtherError() {
        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = main.takeUntil(other).test();
        assertTrue("Main no subscribers?", main.hasSubscribers());
        assertTrue("Other no subscribers?", other.hasSubscribers());
        other.onError(new TestException());
        assertFalse("Main has subscribers?", main.hasSubscribers());
        assertFalse("Other has subscribers?", other.hasSubscribers());
        ts.assertFailure(TestException.class);
    }

    @Test
    public void untilPublisherDispose() {
        PublishProcessor<Integer> main = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        TestSubscriber<Integer> ts = main.takeUntil(other).test();
        assertTrue("Main no subscribers?", main.hasSubscribers());
        assertTrue("Other no subscribers?", other.hasSubscribers());
        ts.cancel();
        assertFalse("Main has subscribers?", main.hasSubscribers());
        assertFalse("Other has subscribers?", other.hasSubscribers());
        ts.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableTakeUntilTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntil() throws java.lang.Throwable {
            this.payloads.takeUntil.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilSourceCompleted() throws java.lang.Throwable {
            this.payloads.takeUntilSourceCompleted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilSourceError() throws java.lang.Throwable {
            this.payloads.takeUntilSourceError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilOtherError() throws java.lang.Throwable {
            this.payloads.takeUntilOtherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUntilOtherCompleted() throws java.lang.Throwable {
            this.payloads.takeUntilOtherCompleted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilFires() throws java.lang.Throwable {
            this.payloads.untilFires.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mainCompletes() throws java.lang.Throwable {
            this.payloads.mainCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_downstreamUnsubscribes() throws java.lang.Throwable {
            this.payloads.downstreamUnsubscribes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.payloads.backpressure.evaluate();
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
        public void benchmark_untilPublisherMainSuccess() throws java.lang.Throwable {
            this.payloads.untilPublisherMainSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherMainComplete() throws java.lang.Throwable {
            this.payloads.untilPublisherMainComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherMainError() throws java.lang.Throwable {
            this.payloads.untilPublisherMainError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherOtherOnNext() throws java.lang.Throwable {
            this.payloads.untilPublisherOtherOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherOtherOnComplete() throws java.lang.Throwable {
            this.payloads.untilPublisherOtherOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherOtherError() throws java.lang.Throwable {
            this.payloads.untilPublisherOtherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_untilPublisherDispose() throws java.lang.Throwable {
            this.payloads.untilPublisherDispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeUntilTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeUntilTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeUntilTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeUntilTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableTakeUntilTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeUntilTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableTakeUntilTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableTakeUntilTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement takeUntil;

            public org.junit.runners.model.Statement takeUntilSourceCompleted;

            public org.junit.runners.model.Statement takeUntilSourceError;

            public org.junit.runners.model.Statement takeUntilOtherError;

            public org.junit.runners.model.Statement takeUntilOtherCompleted;

            public org.junit.runners.model.Statement untilFires;

            public org.junit.runners.model.Statement mainCompletes;

            public org.junit.runners.model.Statement downstreamUnsubscribes;

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement untilPublisherMainSuccess;

            public org.junit.runners.model.Statement untilPublisherMainComplete;

            public org.junit.runners.model.Statement untilPublisherMainError;

            public org.junit.runners.model.Statement untilPublisherOtherOnNext;

            public org.junit.runners.model.Statement untilPublisherOtherOnComplete;

            public org.junit.runners.model.Statement untilPublisherOtherError;

            public org.junit.runners.model.Statement untilPublisherDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.takeUntil = _ClassStatement.forPayload(FlowableTakeUntilTest::takeUntil, "takeUntil", this);
            this.payloads.takeUntilSourceCompleted = _ClassStatement.forPayload(FlowableTakeUntilTest::takeUntilSourceCompleted, "takeUntilSourceCompleted", this);
            this.payloads.takeUntilSourceError = _ClassStatement.forPayload(FlowableTakeUntilTest::takeUntilSourceError, "takeUntilSourceError", this);
            this.payloads.takeUntilOtherError = _ClassStatement.forPayload(FlowableTakeUntilTest::takeUntilOtherError, "takeUntilOtherError", this);
            this.payloads.takeUntilOtherCompleted = _ClassStatement.forPayload(FlowableTakeUntilTest::takeUntilOtherCompleted, "takeUntilOtherCompleted", this);
            this.payloads.untilFires = _ClassStatement.forPayload(FlowableTakeUntilTest::untilFires, "untilFires", this);
            this.payloads.mainCompletes = _ClassStatement.forPayload(FlowableTakeUntilTest::mainCompletes, "mainCompletes", this);
            this.payloads.downstreamUnsubscribes = _ClassStatement.forPayload(FlowableTakeUntilTest::downstreamUnsubscribes, "downstreamUnsubscribes", this);
            this.payloads.backpressure = _ClassStatement.forPayload(FlowableTakeUntilTest::backpressure, "backpressure", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableTakeUntilTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableTakeUntilTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.untilPublisherMainSuccess = _ClassStatement.forPayload(FlowableTakeUntilTest::untilPublisherMainSuccess, "untilPublisherMainSuccess", this);
            this.payloads.untilPublisherMainComplete = _ClassStatement.forPayload(FlowableTakeUntilTest::untilPublisherMainComplete, "untilPublisherMainComplete", this);
            this.payloads.untilPublisherMainError = _ClassStatement.forPayload(FlowableTakeUntilTest::untilPublisherMainError, "untilPublisherMainError", this);
            this.payloads.untilPublisherOtherOnNext = _ClassStatement.forPayload(FlowableTakeUntilTest::untilPublisherOtherOnNext, "untilPublisherOtherOnNext", this);
            this.payloads.untilPublisherOtherOnComplete = _ClassStatement.forPayload(FlowableTakeUntilTest::untilPublisherOtherOnComplete, "untilPublisherOtherOnComplete", this);
            this.payloads.untilPublisherOtherError = _ClassStatement.forPayload(FlowableTakeUntilTest::untilPublisherOtherError, "untilPublisherOtherError", this);
            this.payloads.untilPublisherDispose = _ClassStatement.forPayload(FlowableTakeUntilTest::untilPublisherDispose, "untilPublisherDispose", this);
        }
    }
}
