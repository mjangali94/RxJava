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
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableMaterializeTest extends RxJavaTest {

    @Test
    public void materialize1() {
        // null will cause onError to be triggered before "three" can be
        // returned
        final TestAsyncErrorObservable o1 = new TestAsyncErrorObservable("one", "two", null, "three");
        TestNotificationSubscriber observer = new TestNotificationSubscriber();
        Flowable<Notification<String>> m = Flowable.unsafeCreate(o1).materialize();
        m.subscribe(observer);
        try {
            o1.t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertFalse(observer.onError);
        assertTrue(observer.onComplete);
        assertEquals(3, observer.notifications.size());
        assertTrue(observer.notifications.get(0).isOnNext());
        assertEquals("one", observer.notifications.get(0).getValue());
        assertTrue(observer.notifications.get(1).isOnNext());
        assertEquals("two", observer.notifications.get(1).getValue());
        assertTrue(observer.notifications.get(2).isOnError());
        assertEquals(NullPointerException.class, observer.notifications.get(2).getError().getClass());
    }

    @Test
    public void materialize2() {
        final TestAsyncErrorObservable o1 = new TestAsyncErrorObservable("one", "two", "three");
        TestNotificationSubscriber subscriber = new TestNotificationSubscriber();
        Flowable<Notification<String>> m = Flowable.unsafeCreate(o1).materialize();
        m.subscribe(subscriber);
        try {
            o1.t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertFalse(subscriber.onError);
        assertTrue(subscriber.onComplete);
        assertEquals(4, subscriber.notifications.size());
        assertTrue(subscriber.notifications.get(0).isOnNext());
        assertEquals("one", subscriber.notifications.get(0).getValue());
        assertTrue(subscriber.notifications.get(1).isOnNext());
        assertEquals("two", subscriber.notifications.get(1).getValue());
        assertTrue(subscriber.notifications.get(2).isOnNext());
        assertEquals("three", subscriber.notifications.get(2).getValue());
        assertTrue(subscriber.notifications.get(3).isOnComplete());
    }

    @Test
    public void multipleSubscribes() throws InterruptedException, ExecutionException {
        final TestAsyncErrorObservable o = new TestAsyncErrorObservable("one", "two", null, "three");
        Flowable<Notification<String>> m = Flowable.unsafeCreate(o).materialize();
        assertEquals(3, m.toList().toFuture().get().size());
        assertEquals(3, m.toList().toFuture().get().size());
    }

    @Test
    public void backpressureOnEmptyStream() {
        TestSubscriber<Notification<Integer>> ts = new TestSubscriber<>(0L);
        Flowable.<Integer>empty().materialize().subscribe(ts);
        ts.assertNoValues();
        ts.request(1);
        ts.assertValueCount(1);
        assertTrue(ts.values().get(0).isOnComplete());
        ts.assertComplete();
    }

    @Test
    public void backpressureNoError() {
        TestSubscriber<Notification<Integer>> ts = new TestSubscriber<>(0L);
        Flowable.just(1, 2, 3).materialize().subscribe(ts);
        ts.assertNoValues();
        ts.request(1);
        ts.assertValueCount(1);
        ts.request(2);
        ts.assertValueCount(3);
        ts.request(1);
        ts.assertValueCount(4);
        ts.assertComplete();
    }

    @Test
    public void backpressureNoErrorAsync() throws InterruptedException {
        TestSubscriber<Notification<Integer>> ts = new TestSubscriber<>(0L);
        Flowable.just(1, 2, 3).materialize().subscribeOn(Schedulers.computation()).subscribe(ts);
        Thread.sleep(100);
        ts.assertNoValues();
        ts.request(1);
        Thread.sleep(100);
        ts.assertValueCount(1);
        ts.request(2);
        Thread.sleep(100);
        ts.assertValueCount(3);
        ts.request(1);
        Thread.sleep(100);
        ts.assertValueCount(4);
        ts.assertComplete();
    }

    @Test
    public void backpressureWithError() {
        TestSubscriber<Notification<Integer>> ts = new TestSubscriber<>(0L);
        Flowable.<Integer>error(new IllegalArgumentException()).materialize().subscribe(ts);
        ts.assertNoValues();
        ts.request(1);
        ts.assertValueCount(1);
        ts.assertComplete();
    }

    @Test
    public void backpressureWithEmissionThenError() {
        TestSubscriber<Notification<Integer>> ts = new TestSubscriber<>(0L);
        IllegalArgumentException ex = new IllegalArgumentException();
        Flowable.fromIterable(Arrays.asList(1)).concatWith(Flowable.<Integer>error(ex)).materialize().subscribe(ts);
        ts.assertNoValues();
        ts.request(1);
        ts.assertValueCount(1);
        assertTrue(ts.values().get(0).isOnNext());
        ts.request(1);
        ts.assertValueCount(2);
        assertTrue(ts.values().get(1).isOnError());
        assertEquals(ex, ts.values().get(1).getError());
        ts.assertComplete();
    }

    @Test
    public void withCompletionCausingError() {
        TestSubscriberEx<Notification<Integer>> ts = new TestSubscriberEx<>();
        final RuntimeException ex = new RuntimeException("boo");
        Flowable.<Integer>empty().materialize().doOnNext(new Consumer<Object>() {

            @Override
            public void accept(Object t) {
                throw ex;
            }
        }).subscribe(ts);
        ts.assertError(ex);
        ts.assertNoValues();
        ts.assertTerminated();
    }

    @Test
    public void unsubscribeJustBeforeCompletionNotificationShouldPreventThatNotificationArriving() {
        TestSubscriber<Notification<Integer>> ts = new TestSubscriber<>(0L);
        Flowable.<Integer>empty().materialize().subscribe(ts);
        ts.assertNoValues();
        ts.cancel();
        ts.request(1);
        ts.assertNoValues();
    }

    private static class TestNotificationSubscriber extends DefaultSubscriber<Notification<String>> {

        boolean onComplete;

        boolean onError;

        List<Notification<String>> notifications = new Vector<>();

        @Override
        public void onComplete() {
            this.onComplete = true;
        }

        @Override
        public void onError(Throwable e) {
            this.onError = true;
        }

        @Override
        public void onNext(Notification<String> value) {
            this.notifications.add(value);
        }
    }

    private static class TestAsyncErrorObservable implements Publisher<String> {

        String[] valuesToReturn;

        TestAsyncErrorObservable(String... values) {
            valuesToReturn = values;
        }

        volatile Thread t;

        @Override
        public void subscribe(final Subscriber<? super String> subscriber) {
            subscriber.onSubscribe(new BooleanSubscription());
            t = new Thread(new Runnable() {

                @Override
                public void run() {
                    for (String s : valuesToReturn) {
                        if (s == null) {
                            System.out.println("throwing exception");
                            try {
                                Thread.sleep(100);
                            } catch (Throwable e) {
                            }
                            subscriber.onError(new NullPointerException());
                            return;
                        } else {
                            subscriber.onNext(s);
                        }
                    }
                    System.out.println("subscription complete");
                    subscriber.onComplete();
                }
            });
            t.start();
        }
    }

    @Test
    public void backpressure() {
        TestSubscriber<Notification<Integer>> ts = Flowable.range(1, 5).materialize().test(0);
        ts.assertEmpty();
        ts.request(5);
        ts.assertValueCount(5).assertNoErrors().assertNotComplete();
        ts.request(1);
        ts.assertValueCount(6).assertNoErrors().assertComplete();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).materialize());
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Notification<Object>>>() {

            @Override
            public Flowable<Notification<Object>> apply(Flowable<Object> f) throws Exception {
                return f.materialize();
            }
        });
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Object>, Object>() {

            @Override
            public Object apply(Flowable<Object> f) throws Exception {
                return f.materialize();
            }
        }, false, null, null, Notification.createOnComplete());
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.just(1).materialize());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableMaterializeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_materialize1() throws java.lang.Throwable {
            this.payloads.materialize1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_materialize2() throws java.lang.Throwable {
            this.payloads.materialize2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleSubscribes() throws java.lang.Throwable {
            this.payloads.multipleSubscribes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureOnEmptyStream() throws java.lang.Throwable {
            this.payloads.backpressureOnEmptyStream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNoError() throws java.lang.Throwable {
            this.payloads.backpressureNoError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNoErrorAsync() throws java.lang.Throwable {
            this.payloads.backpressureNoErrorAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithError() throws java.lang.Throwable {
            this.payloads.backpressureWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureWithEmissionThenError() throws java.lang.Throwable {
            this.payloads.backpressureWithEmissionThenError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withCompletionCausingError() throws java.lang.Throwable {
            this.payloads.withCompletionCausingError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeJustBeforeCompletionNotificationShouldPreventThatNotificationArriving() throws java.lang.Throwable {
            this.payloads.unsubscribeJustBeforeCompletionNotificationShouldPreventThatNotificationArriving.evaluate();
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
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMaterializeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMaterializeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMaterializeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMaterializeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableMaterializeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMaterializeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableMaterializeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableMaterializeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement materialize1;

            public org.junit.runners.model.Statement materialize2;

            public org.junit.runners.model.Statement multipleSubscribes;

            public org.junit.runners.model.Statement backpressureOnEmptyStream;

            public org.junit.runners.model.Statement backpressureNoError;

            public org.junit.runners.model.Statement backpressureNoErrorAsync;

            public org.junit.runners.model.Statement backpressureWithError;

            public org.junit.runners.model.Statement backpressureWithEmissionThenError;

            public org.junit.runners.model.Statement withCompletionCausingError;

            public org.junit.runners.model.Statement unsubscribeJustBeforeCompletionNotificationShouldPreventThatNotificationArriving;

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement badRequest;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.materialize1 = _ClassStatement.forPayload(FlowableMaterializeTest::materialize1, "materialize1", this);
            this.payloads.materialize2 = _ClassStatement.forPayload(FlowableMaterializeTest::materialize2, "materialize2", this);
            this.payloads.multipleSubscribes = _ClassStatement.forPayload(FlowableMaterializeTest::multipleSubscribes, "multipleSubscribes", this);
            this.payloads.backpressureOnEmptyStream = _ClassStatement.forPayload(FlowableMaterializeTest::backpressureOnEmptyStream, "backpressureOnEmptyStream", this);
            this.payloads.backpressureNoError = _ClassStatement.forPayload(FlowableMaterializeTest::backpressureNoError, "backpressureNoError", this);
            this.payloads.backpressureNoErrorAsync = _ClassStatement.forPayload(FlowableMaterializeTest::backpressureNoErrorAsync, "backpressureNoErrorAsync", this);
            this.payloads.backpressureWithError = _ClassStatement.forPayload(FlowableMaterializeTest::backpressureWithError, "backpressureWithError", this);
            this.payloads.backpressureWithEmissionThenError = _ClassStatement.forPayload(FlowableMaterializeTest::backpressureWithEmissionThenError, "backpressureWithEmissionThenError", this);
            this.payloads.withCompletionCausingError = _ClassStatement.forPayload(FlowableMaterializeTest::withCompletionCausingError, "withCompletionCausingError", this);
            this.payloads.unsubscribeJustBeforeCompletionNotificationShouldPreventThatNotificationArriving = _ClassStatement.forPayload(FlowableMaterializeTest::unsubscribeJustBeforeCompletionNotificationShouldPreventThatNotificationArriving, "unsubscribeJustBeforeCompletionNotificationShouldPreventThatNotificationArriving", this);
            this.payloads.backpressure = _ClassStatement.forPayload(FlowableMaterializeTest::backpressure, "backpressure", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableMaterializeTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableMaterializeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableMaterializeTest::badSource, "badSource", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableMaterializeTest::badRequest, "badRequest", this);
        }
    }
}
