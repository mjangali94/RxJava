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

import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableMapNotification.MapNotificationSubscriber;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableMapNotificationTest extends RxJavaTest {

    @Test
    public void just() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.just(1).flatMap(new Function<Integer, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Integer item) {
                return Flowable.just((Object) (item + 1));
            }
        }, new Function<Throwable, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Throwable e) {
                return Flowable.error(e);
            }
        }, new Supplier<Flowable<Object>>() {

            @Override
            public Flowable<Object> get() {
                return Flowable.never();
            }
        }).subscribe(ts);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.assertValue(2);
    }

    @Test
    public void backpressure() {
        TestSubscriber<Object> ts = TestSubscriber.create(0L);
        new FlowableMapNotification<>(Flowable.range(1, 3), new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer item) {
                return item + 1;
            }
        }, new Function<Throwable, Integer>() {

            @Override
            public Integer apply(Throwable e) {
                return 0;
            }
        }, new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 5;
            }
        }).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(3);
        ts.assertValues(2, 3, 4);
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(1);
        ts.assertValues(2, 3, 4, 5);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void noBackpressure() {
        TestSubscriber<Object> ts = TestSubscriber.create(0L);
        PublishProcessor<Integer> pp = PublishProcessor.create();
        new FlowableMapNotification<>(pp, new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer item) {
                return item + 1;
            }
        }, new Function<Throwable, Integer>() {

            @Override
            public Integer apply(Throwable e) {
                return 0;
            }
        }, new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 5;
            }
        }).subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        pp.onNext(1);
        pp.onNext(2);
        pp.onNext(3);
        pp.onComplete();
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();
        ts.request(1);
        ts.assertValue(0);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(new Flowable<Integer>() {

            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                MapNotificationSubscriber mn = new MapNotificationSubscriber(subscriber, Functions.justFunction(Flowable.just(1)), Functions.justFunction(Flowable.just(2)), Functions.justSupplier(Flowable.just(3)));
                mn.onSubscribe(new BooleanSubscription());
            }
        });
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Flowable<Object> f) throws Exception {
                return f.flatMap(Functions.justFunction(Flowable.just(1)), Functions.justFunction(Flowable.just(2)), Functions.justSupplier(Flowable.just(3)));
            }
        });
    }

    @Test
    public void onErrorCrash() {
        TestSubscriberEx<Integer> ts = Flowable.<Integer>error(new TestException("Outer")).flatMap(Functions.justFunction(Flowable.just(1)), new Function<Throwable, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Throwable t) throws Exception {
                throw new TestException("Inner");
            }
        }, Functions.justSupplier(Flowable.just(3))).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        TestHelper.assertError(ts, 0, TestException.class, "Outer");
        TestHelper.assertError(ts, 1, TestException.class, "Inner");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableMapNotificationTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.payloads.just.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.payloads.backpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noBackpressure() throws java.lang.Throwable {
            this.payloads.noBackpressure.evaluate();
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
        public void benchmark_onErrorCrash() throws java.lang.Throwable {
            this.payloads.onErrorCrash.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMapNotificationTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMapNotificationTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMapNotificationTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMapNotificationTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableMapNotificationTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMapNotificationTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableMapNotificationTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableMapNotificationTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement just;

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement noBackpressure;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement onErrorCrash;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.just = _ClassStatement.forPayload(FlowableMapNotificationTest::just, "just", this);
            this.payloads.backpressure = _ClassStatement.forPayload(FlowableMapNotificationTest::backpressure, "backpressure", this);
            this.payloads.noBackpressure = _ClassStatement.forPayload(FlowableMapNotificationTest::noBackpressure, "noBackpressure", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableMapNotificationTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableMapNotificationTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.onErrorCrash = _ClassStatement.forPayload(FlowableMapNotificationTest::onErrorCrash, "onErrorCrash", this);
        }
    }
}
