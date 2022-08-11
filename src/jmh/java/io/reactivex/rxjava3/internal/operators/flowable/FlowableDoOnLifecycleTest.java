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
import java.util.List;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableDoOnLifecycleTest extends RxJavaTest {

    @Test
    public void onSubscribeCrashed() {
        Flowable.just(1).doOnLifecycle(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) throws Exception {
                throw new TestException();
            }
        }, Functions.EMPTY_LONG_CONSUMER, Functions.EMPTY_ACTION).test().assertFailure(TestException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        final int[] calls = { 0, 0 };
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.doOnLifecycle(new Consumer<Subscription>() {

                    @Override
                    public void accept(Subscription s) throws Exception {
                        calls[0]++;
                    }
                }, Functions.EMPTY_LONG_CONSUMER, new Action() {

                    @Override
                    public void run() throws Exception {
                        calls[1]++;
                    }
                });
            }
        });
        assertEquals(2, calls[0]);
        assertEquals(0, calls[1]);
    }

    @Test
    public void dispose() {
        final int[] calls = { 0, 0 };
        TestHelper.checkDisposed(Flowable.just(1).doOnLifecycle(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) throws Exception {
                calls[0]++;
            }
        }, Functions.EMPTY_LONG_CONSUMER, new Action() {

            @Override
            public void run() throws Exception {
                calls[1]++;
            }
        }));
        assertEquals(1, calls[0]);
        assertEquals(1, calls[1]);
    }

    @Test
    public void requestCrashed() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.just(1).doOnLifecycle(Functions.emptyConsumer(), new LongConsumer() {

                @Override
                public void accept(long v) throws Exception {
                    throw new TestException();
                }
            }, Functions.EMPTY_ACTION).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancelCrashed() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.just(1).doOnLifecycle(Functions.emptyConsumer(), Functions.EMPTY_LONG_CONSUMER, new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).take(1).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onSubscribeCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final BooleanSubscription bs = new BooleanSubscription();
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(bs);
                    s.onError(new TestException("Second"));
                    s.onComplete();
                }
            }.doOnSubscribe(new Consumer<Subscription>() {

                @Override
                public void accept(Subscription s) throws Exception {
                    throw new TestException("First");
                }
            }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            assertTrue(bs.isCancelled());
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableDoOnLifecycleTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeCrashed() throws java.lang.Throwable {
            this.payloads.onSubscribeCrashed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestCrashed() throws java.lang.Throwable {
            this.payloads.requestCrashed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelCrashed() throws java.lang.Throwable {
            this.payloads.cancelCrashed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeCrash() throws java.lang.Throwable {
            this.payloads.onSubscribeCrash.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnLifecycleTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnLifecycleTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnLifecycleTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnLifecycleTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableDoOnLifecycleTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnLifecycleTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableDoOnLifecycleTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableDoOnLifecycleTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement onSubscribeCrashed;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement requestCrashed;

            public org.junit.runners.model.Statement cancelCrashed;

            public org.junit.runners.model.Statement onSubscribeCrash;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.onSubscribeCrashed = _ClassStatement.forPayload(FlowableDoOnLifecycleTest::onSubscribeCrashed, "onSubscribeCrashed", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableDoOnLifecycleTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableDoOnLifecycleTest::dispose, "dispose", this);
            this.payloads.requestCrashed = _ClassStatement.forPayload(FlowableDoOnLifecycleTest::requestCrashed, "requestCrashed", this);
            this.payloads.cancelCrashed = _ClassStatement.forPayload(FlowableDoOnLifecycleTest::cancelCrashed, "cancelCrashed", this);
            this.payloads.onSubscribeCrash = _ClassStatement.forPayload(FlowableDoOnLifecycleTest::onSubscribeCrash, "onSubscribeCrash", this);
        }
    }
}
