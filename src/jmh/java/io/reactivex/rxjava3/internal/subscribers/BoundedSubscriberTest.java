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
package io.reactivex.rxjava3.internal.subscribers;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.*;

public class BoundedSubscriberTest extends RxJavaTest {

    @Test
    public void onSubscribeThrows() {
        final List<Object> received = new ArrayList<>();
        BoundedSubscriber<Object> subscriber = new BoundedSubscriber<>(new Consumer<Object>() {

            @Override
            public void accept(Object o) throws Exception {
                received.add(o);
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable throwable) throws Exception {
                received.add(throwable);
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
                received.add(1);
            }
        }, new Consumer<Subscription>() {

            @Override
            public void accept(Subscription subscription) throws Exception {
                throw new TestException();
            }
        }, 128);
        assertFalse(subscriber.isDisposed());
        Flowable.just(1).subscribe(subscriber);
        assertTrue(received.toString(), received.get(0) instanceof TestException);
        assertEquals(received.toString(), 1, received.size());
        assertTrue(subscriber.isDisposed());
    }

    @Test
    public void onNextThrows() {
        final List<Object> received = new ArrayList<>();
        BoundedSubscriber<Object> subscriber = new BoundedSubscriber<>(new Consumer<Object>() {

            @Override
            public void accept(Object o) throws Exception {
                throw new TestException();
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable throwable) throws Exception {
                received.add(throwable);
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
                received.add(1);
            }
        }, new Consumer<Subscription>() {

            @Override
            public void accept(Subscription subscription) throws Exception {
                subscription.request(128);
            }
        }, 128);
        assertFalse(subscriber.isDisposed());
        Flowable.just(1).subscribe(subscriber);
        assertTrue(received.toString(), received.get(0) instanceof TestException);
        assertEquals(received.toString(), 1, received.size());
        assertTrue(subscriber.isDisposed());
    }

    @Test
    public void onErrorThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final List<Object> received = new ArrayList<>();
            BoundedSubscriber<Object> subscriber = new BoundedSubscriber<>(new Consumer<Object>() {

                @Override
                public void accept(Object o) throws Exception {
                    received.add(o);
                }
            }, new Consumer<Throwable>() {

                @Override
                public void accept(Throwable throwable) throws Exception {
                    throw new TestException("Inner");
                }
            }, new Action() {

                @Override
                public void run() throws Exception {
                    received.add(1);
                }
            }, new Consumer<Subscription>() {

                @Override
                public void accept(Subscription subscription) throws Exception {
                    subscription.request(128);
                }
            }, 128);
            assertFalse(subscriber.isDisposed());
            Flowable.<Integer>error(new TestException("Outer")).subscribe(subscriber);
            assertTrue(received.toString(), received.isEmpty());
            assertTrue(subscriber.isDisposed());
            TestHelper.assertError(errors, 0, CompositeException.class);
            List<Throwable> ce = TestHelper.compositeList(errors.get(0));
            TestHelper.assertError(ce, 0, TestException.class, "Outer");
            TestHelper.assertError(ce, 1, TestException.class, "Inner");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onCompleteThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final List<Object> received = new ArrayList<>();
            BoundedSubscriber<Object> subscriber = new BoundedSubscriber<>(new Consumer<Object>() {

                @Override
                public void accept(Object o) throws Exception {
                    received.add(o);
                }
            }, new Consumer<Throwable>() {

                @Override
                public void accept(Throwable throwable) throws Exception {
                    received.add(throwable);
                }
            }, new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }, new Consumer<Subscription>() {

                @Override
                public void accept(Subscription subscription) throws Exception {
                    subscription.request(128);
                }
            }, 128);
            assertFalse(subscriber.isDisposed());
            Flowable.<Integer>empty().subscribe(subscriber);
            assertTrue(received.toString(), received.isEmpty());
            assertTrue(subscriber.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onNextThrowsCancelsUpstream() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final List<Throwable> errors = new ArrayList<>();
        BoundedSubscriber<Integer> s = new BoundedSubscriber<>(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                throw new TestException();
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                errors.add(e);
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
            }
        }, new Consumer<Subscription>() {

            @Override
            public void accept(Subscription subscription) throws Exception {
                subscription.request(128);
            }
        }, 128);
        pp.subscribe(s);
        assertTrue("No observers?!", pp.hasSubscribers());
        assertTrue("Has errors already?!", errors.isEmpty());
        pp.onNext(1);
        assertFalse("Has observers?!", pp.hasSubscribers());
        assertFalse("No errors?!", errors.isEmpty());
        assertTrue(errors.toString(), errors.get(0) instanceof TestException);
    }

    @Test
    public void onSubscribeThrowsCancelsUpstream() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        final List<Throwable> errors = new ArrayList<>();
        BoundedSubscriber<Integer> s = new BoundedSubscriber<>(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                errors.add(e);
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
            }
        }, new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) throws Exception {
                throw new TestException();
            }
        }, 128);
        pp.subscribe(s);
        assertFalse("Has observers?!", pp.hasSubscribers());
        assertFalse("No errors?!", errors.isEmpty());
        assertTrue(errors.toString(), errors.get(0) instanceof TestException);
    }

    @Test
    public void badSourceOnSubscribe() {
        Flowable<Integer> source = Flowable.fromPublisher(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                BooleanSubscription s1 = new BooleanSubscription();
                s.onSubscribe(s1);
                BooleanSubscription s2 = new BooleanSubscription();
                s.onSubscribe(s2);
                assertFalse(s1.isCancelled());
                assertTrue(s2.isCancelled());
                s.onNext(1);
                s.onComplete();
            }
        });
        final List<Object> received = new ArrayList<>();
        BoundedSubscriber<Object> subscriber = new BoundedSubscriber<>(new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                received.add(v);
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                received.add(e);
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
                received.add(100);
            }
        }, new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) throws Exception {
                s.request(128);
            }
        }, 128);
        source.subscribe(subscriber);
        assertEquals(Arrays.asList(1, 100), received);
    }

    @Test
    @SuppressUndeliverable
    public void badSourceEmitAfterDone() {
        Flowable<Integer> source = Flowable.fromPublisher(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                BooleanSubscription s1 = new BooleanSubscription();
                s.onSubscribe(s1);
                s.onNext(1);
                s.onComplete();
                s.onNext(2);
                s.onError(new TestException());
                s.onComplete();
            }
        });
        final List<Object> received = new ArrayList<>();
        BoundedSubscriber<Object> subscriber = new BoundedSubscriber<>(new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                received.add(v);
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                received.add(e);
            }
        }, new Action() {

            @Override
            public void run() throws Exception {
                received.add(100);
            }
        }, new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) throws Exception {
                s.request(128);
            }
        }, 128);
        source.subscribe(subscriber);
        assertEquals(Arrays.asList(1, 100), received);
    }

    @Test
    public void onErrorMissingShouldReportNoCustomOnError() {
        BoundedSubscriber<Integer> subscriber = new BoundedSubscriber<>(Functions.<Integer>emptyConsumer(), Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION, Functions.<Subscription>boundedConsumer(128), 128);
        assertFalse(subscriber.hasCustomOnError());
    }

    @Test
    public void customOnErrorShouldReportCustomOnError() {
        BoundedSubscriber<Integer> subscriber = new BoundedSubscriber<>(Functions.<Integer>emptyConsumer(), Functions.<Throwable>emptyConsumer(), Functions.EMPTY_ACTION, Functions.<Subscription>boundedConsumer(128), 128);
        assertTrue(subscriber.hasCustomOnError());
    }

    @Test
    public void cancel() {
        BoundedSubscriber<Integer> subscriber = new BoundedSubscriber<>(Functions.<Integer>emptyConsumer(), Functions.<Throwable>emptyConsumer(), Functions.EMPTY_ACTION, Functions.<Subscription>boundedConsumer(128), 128);
        BooleanSubscription bs = new BooleanSubscription();
        subscriber.onSubscribe(bs);
        subscriber.cancel();
        assertTrue(bs.isCancelled());
    }

    @Test
    public void dispose() {
        BoundedSubscriber<Integer> subscriber = new BoundedSubscriber<>(Functions.<Integer>emptyConsumer(), Functions.<Throwable>emptyConsumer(), Functions.EMPTY_ACTION, Functions.<Subscription>boundedConsumer(128), 128);
        BooleanSubscription bs = new BooleanSubscription();
        subscriber.onSubscribe(bs);
        assertFalse(subscriber.isDisposed());
        subscriber.dispose();
        assertTrue(bs.isCancelled());
        assertTrue(subscriber.isDisposed());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public BoundedSubscriberTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeThrows() throws java.lang.Throwable {
            this.payloads.onSubscribeThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextThrows() throws java.lang.Throwable {
            this.payloads.onNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorThrows() throws java.lang.Throwable {
            this.payloads.onErrorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteThrows() throws java.lang.Throwable {
            this.payloads.onCompleteThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextThrowsCancelsUpstream() throws java.lang.Throwable {
            this.payloads.onNextThrowsCancelsUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeThrowsCancelsUpstream() throws java.lang.Throwable {
            this.payloads.onSubscribeThrowsCancelsUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceOnSubscribe() throws java.lang.Throwable {
            this.payloads.badSourceOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceEmitAfterDone() throws java.lang.Throwable {
            this.payloads.badSourceEmitAfterDone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorMissingShouldReportNoCustomOnError() throws java.lang.Throwable {
            this.payloads.onErrorMissingShouldReportNoCustomOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customOnErrorShouldReportCustomOnError() throws java.lang.Throwable {
            this.payloads.customOnErrorShouldReportCustomOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BoundedSubscriberTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BoundedSubscriberTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BoundedSubscriberTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BoundedSubscriberTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new BoundedSubscriberTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<BoundedSubscriberTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(BoundedSubscriberTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(BoundedSubscriberTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement onSubscribeThrows;

            public org.junit.runners.model.Statement onNextThrows;

            public org.junit.runners.model.Statement onErrorThrows;

            public org.junit.runners.model.Statement onCompleteThrows;

            public org.junit.runners.model.Statement onNextThrowsCancelsUpstream;

            public org.junit.runners.model.Statement onSubscribeThrowsCancelsUpstream;

            public org.junit.runners.model.Statement badSourceOnSubscribe;

            public org.junit.runners.model.Statement badSourceEmitAfterDone;

            public org.junit.runners.model.Statement onErrorMissingShouldReportNoCustomOnError;

            public org.junit.runners.model.Statement customOnErrorShouldReportCustomOnError;

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement dispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.onSubscribeThrows = _ClassStatement.forPayload(BoundedSubscriberTest::onSubscribeThrows, "onSubscribeThrows", this);
            this.payloads.onNextThrows = _ClassStatement.forPayload(BoundedSubscriberTest::onNextThrows, "onNextThrows", this);
            this.payloads.onErrorThrows = _ClassStatement.forPayload(BoundedSubscriberTest::onErrorThrows, "onErrorThrows", this);
            this.payloads.onCompleteThrows = _ClassStatement.forPayload(BoundedSubscriberTest::onCompleteThrows, "onCompleteThrows", this);
            this.payloads.onNextThrowsCancelsUpstream = _ClassStatement.forPayload(BoundedSubscriberTest::onNextThrowsCancelsUpstream, "onNextThrowsCancelsUpstream", this);
            this.payloads.onSubscribeThrowsCancelsUpstream = _ClassStatement.forPayload(BoundedSubscriberTest::onSubscribeThrowsCancelsUpstream, "onSubscribeThrowsCancelsUpstream", this);
            this.payloads.badSourceOnSubscribe = _ClassStatement.forPayload(BoundedSubscriberTest::badSourceOnSubscribe, "badSourceOnSubscribe", this);
            this.payloads.badSourceEmitAfterDone = _ClassStatement.forPayload(BoundedSubscriberTest::badSourceEmitAfterDone, "badSourceEmitAfterDone", this);
            this.payloads.onErrorMissingShouldReportNoCustomOnError = _ClassStatement.forPayload(BoundedSubscriberTest::onErrorMissingShouldReportNoCustomOnError, "onErrorMissingShouldReportNoCustomOnError", this);
            this.payloads.customOnErrorShouldReportCustomOnError = _ClassStatement.forPayload(BoundedSubscriberTest::customOnErrorShouldReportCustomOnError, "customOnErrorShouldReportCustomOnError", this);
            this.payloads.cancel = _ClassStatement.forPayload(BoundedSubscriberTest::cancel, "cancel", this);
            this.payloads.dispose = _ClassStatement.forPayload(BoundedSubscriberTest::dispose, "dispose", this);
        }
    }
}
