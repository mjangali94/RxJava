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
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableDematerializeTest extends RxJavaTest {

    @Test
    public void simpleSelector() {
        Flowable<Notification<Integer>> notifications = Flowable.just(1, 2).materialize();
        Flowable<Integer> dematerialize = notifications.dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        dematerialize.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(1);
        verify(subscriber, times(1)).onNext(2);
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void selectorCrash() {
        Flowable.just(1, 2).materialize().dematerialize(new Function<Notification<Integer>, Notification<Object>>() {

            @Override
            public Notification<Object> apply(Notification<Integer> v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void selectorNull() {
        Flowable.just(1, 2).materialize().dematerialize(new Function<Notification<Integer>, Notification<Object>>() {

            @Override
            public Notification<Object> apply(Notification<Integer> v) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void dematerialize1() {
        Flowable<Notification<Integer>> notifications = Flowable.just(1, 2).materialize();
        Flowable<Integer> dematerialize = notifications.dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        dematerialize.subscribe(subscriber);
        verify(subscriber, times(1)).onNext(1);
        verify(subscriber, times(1)).onNext(2);
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void dematerialize2() {
        Throwable exception = new Throwable("test");
        Flowable<Integer> flowable = Flowable.error(exception);
        Flowable<Integer> dematerialize = flowable.materialize().dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        dematerialize.subscribe(subscriber);
        verify(subscriber, times(1)).onError(exception);
        verify(subscriber, times(0)).onComplete();
        verify(subscriber, times(0)).onNext(any(Integer.class));
    }

    @Test
    public void dematerialize3() {
        Exception exception = new Exception("test");
        Flowable<Integer> flowable = Flowable.error(exception);
        Flowable<Integer> dematerialize = flowable.materialize().dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        dematerialize.subscribe(subscriber);
        verify(subscriber, times(1)).onError(exception);
        verify(subscriber, times(0)).onComplete();
        verify(subscriber, times(0)).onNext(any(Integer.class));
    }

    @Test
    public void errorPassThru() {
        Exception exception = new Exception("test");
        Flowable<Notification<Integer>> flowable = Flowable.error(exception);
        Flowable<Integer> dematerialize = flowable.dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        dematerialize.subscribe(subscriber);
        verify(subscriber, times(1)).onError(exception);
        verify(subscriber, times(0)).onComplete();
        verify(subscriber, times(0)).onNext(any(Integer.class));
    }

    @Test
    public void completePassThru() {
        Flowable<Notification<Integer>> flowable = Flowable.empty();
        Flowable<Integer> dematerialize = flowable.dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(subscriber);
        dematerialize.subscribe(ts);
        // System.out.println(ts.errors());
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, times(0)).onNext(any(Integer.class));
    }

    @Test
    public void honorsContractWhenCompleted() {
        Flowable<Integer> source = Flowable.just(1);
        Flowable<Integer> result = source.materialize().dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        verify(subscriber).onNext(1);
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void honorsContractWhenThrows() {
        Flowable<Integer> source = Flowable.error(new TestException());
        Flowable<Integer> result = source.materialize().dematerialize(Functions.<Notification<Integer>>identity());
        Subscriber<Integer> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        verify(subscriber, never()).onNext(any(Integer.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber).onError(any(TestException.class));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(Notification.createOnComplete()).dematerialize(Functions.<Notification<Object>>identity()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Notification<Object>>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Notification<Object>> f) throws Exception {
                return f.dematerialize(Functions.<Notification<Object>>identity());
            }
        });
    }

    @Test
    public void eventsAfterDematerializedTerminal() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Notification<Object>>() {

                @Override
                protected void subscribeActual(Subscriber<? super Notification<Object>> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(Notification.createOnComplete());
                    subscriber.onNext(Notification.<Object>createOnNext(1));
                    subscriber.onNext(Notification.createOnError(new TestException("First")));
                    subscriber.onError(new TestException("Second"));
                }
            }.dematerialize(Functions.<Notification<Object>>identity()).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "First");
            TestHelper.assertUndeliverable(errors, 1, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void notificationInstanceAfterDispose() {
        new Flowable<Notification<Object>>() {

            @Override
            protected void subscribeActual(Subscriber<? super Notification<Object>> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                subscriber.onNext(Notification.createOnComplete());
                subscriber.onNext(Notification.<Object>createOnNext(1));
            }
        }.dematerialize(Functions.<Notification<Object>>identity()).test().assertResult();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void nonNotificationInstanceAfterDispose() {
        new Flowable<Object>() {

            @Override
            protected void subscribeActual(Subscriber<? super Object> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                subscriber.onNext(Notification.createOnComplete());
                subscriber.onNext(1);
            }
        }.dematerialize(v -> (Notification<Object>) v).test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableDematerializeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleSelector() throws java.lang.Throwable {
            this.payloads.simpleSelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorCrash() throws java.lang.Throwable {
            this.payloads.selectorCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_selectorNull() throws java.lang.Throwable {
            this.payloads.selectorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dematerialize1() throws java.lang.Throwable {
            this.payloads.dematerialize1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dematerialize2() throws java.lang.Throwable {
            this.payloads.dematerialize2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dematerialize3() throws java.lang.Throwable {
            this.payloads.dematerialize3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorPassThru() throws java.lang.Throwable {
            this.payloads.errorPassThru.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completePassThru() throws java.lang.Throwable {
            this.payloads.completePassThru.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_honorsContractWhenCompleted() throws java.lang.Throwable {
            this.payloads.honorsContractWhenCompleted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_honorsContractWhenThrows() throws java.lang.Throwable {
            this.payloads.honorsContractWhenThrows.evaluate();
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
        public void benchmark_eventsAfterDematerializedTerminal() throws java.lang.Throwable {
            this.payloads.eventsAfterDematerializedTerminal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notificationInstanceAfterDispose() throws java.lang.Throwable {
            this.payloads.notificationInstanceAfterDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonNotificationInstanceAfterDispose() throws java.lang.Throwable {
            this.payloads.nonNotificationInstanceAfterDispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDematerializeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDematerializeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDematerializeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDematerializeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableDematerializeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDematerializeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableDematerializeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableDematerializeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement simpleSelector;

            public org.junit.runners.model.Statement selectorCrash;

            public org.junit.runners.model.Statement selectorNull;

            public org.junit.runners.model.Statement dematerialize1;

            public org.junit.runners.model.Statement dematerialize2;

            public org.junit.runners.model.Statement dematerialize3;

            public org.junit.runners.model.Statement errorPassThru;

            public org.junit.runners.model.Statement completePassThru;

            public org.junit.runners.model.Statement honorsContractWhenCompleted;

            public org.junit.runners.model.Statement honorsContractWhenThrows;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement eventsAfterDematerializedTerminal;

            public org.junit.runners.model.Statement notificationInstanceAfterDispose;

            public org.junit.runners.model.Statement nonNotificationInstanceAfterDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.simpleSelector = _ClassStatement.forPayload(FlowableDematerializeTest::simpleSelector, "simpleSelector", this);
            this.payloads.selectorCrash = _ClassStatement.forPayload(FlowableDematerializeTest::selectorCrash, "selectorCrash", this);
            this.payloads.selectorNull = _ClassStatement.forPayload(FlowableDematerializeTest::selectorNull, "selectorNull", this);
            this.payloads.dematerialize1 = _ClassStatement.forPayload(FlowableDematerializeTest::dematerialize1, "dematerialize1", this);
            this.payloads.dematerialize2 = _ClassStatement.forPayload(FlowableDematerializeTest::dematerialize2, "dematerialize2", this);
            this.payloads.dematerialize3 = _ClassStatement.forPayload(FlowableDematerializeTest::dematerialize3, "dematerialize3", this);
            this.payloads.errorPassThru = _ClassStatement.forPayload(FlowableDematerializeTest::errorPassThru, "errorPassThru", this);
            this.payloads.completePassThru = _ClassStatement.forPayload(FlowableDematerializeTest::completePassThru, "completePassThru", this);
            this.payloads.honorsContractWhenCompleted = _ClassStatement.forPayload(FlowableDematerializeTest::honorsContractWhenCompleted, "honorsContractWhenCompleted", this);
            this.payloads.honorsContractWhenThrows = _ClassStatement.forPayload(FlowableDematerializeTest::honorsContractWhenThrows, "honorsContractWhenThrows", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableDematerializeTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableDematerializeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.eventsAfterDematerializedTerminal = _ClassStatement.forPayload(FlowableDematerializeTest::eventsAfterDematerializedTerminal, "eventsAfterDematerializedTerminal", this);
            this.payloads.notificationInstanceAfterDispose = _ClassStatement.forPayload(FlowableDematerializeTest::notificationInstanceAfterDispose, "notificationInstanceAfterDispose", this);
            this.payloads.nonNotificationInstanceAfterDispose = _ClassStatement.forPayload(FlowableDematerializeTest::nonNotificationInstanceAfterDispose, "nonNotificationInstanceAfterDispose", this);
        }
    }
}
