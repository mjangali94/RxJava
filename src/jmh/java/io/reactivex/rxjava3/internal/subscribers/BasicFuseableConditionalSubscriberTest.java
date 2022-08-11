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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.annotations.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.internal.subscriptions.*;
import io.reactivex.rxjava3.operators.ConditionalSubscriber;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.testsupport.*;

public class BasicFuseableConditionalSubscriberTest extends RxJavaTest {

    @Test
    public void offerThrows() {
        ConditionalSubscriber<Integer> cs = new ConditionalSubscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
            }

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
            public boolean tryOnNext(Integer t) {
                return false;
            }
        };
        BasicFuseableConditionalSubscriber<Integer, Integer> fcs = new BasicFuseableConditionalSubscriber<Integer, Integer>(cs) {

            @Override
            public boolean tryOnNext(Integer t) {
                return false;
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public int requestFusion(int mode) {
                return 0;
            }

            @Nullable
            @Override
            public Integer poll() throws Exception {
                return null;
            }
        };
        fcs.onSubscribe(new ScalarSubscription<>(fcs, 1));
        TestHelper.assertNoOffer(fcs);
        assertFalse(fcs.isEmpty());
        fcs.clear();
        assertTrue(fcs.isEmpty());
    }

    @Test
    public void implementationStopsOnSubscribe() {
        @SuppressWarnings("unchecked")
        ConditionalSubscriber<Integer> ts = mock(ConditionalSubscriber.class);
        BasicFuseableConditionalSubscriber<Integer, Integer> bfs = new BasicFuseableConditionalSubscriber<Integer, Integer>(ts) {

            @Override
            protected boolean beforeDownstream() {
                return false;
            }

            @Override
            public void onNext(@NonNull Integer t) {
                ts.onNext(t);
            }

            @Override
            public int requestFusion(int mode) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public boolean tryOnNext(@NonNull Integer t) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            @Nullable
            public Integer poll() throws Throwable {
                return null;
            }
        };
        bfs.onSubscribe(new BooleanSubscription());
        verify(ts, never()).onSubscribe(any());
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.map(v -> v).filter(v -> true));
    }

    @Test
    public void transitiveBoundaryFusionNone() {
        @SuppressWarnings("unchecked")
        ConditionalSubscriber<Integer> ts = mock(ConditionalSubscriber.class);
        BasicFuseableConditionalSubscriber<Integer, Integer> bfs = new BasicFuseableConditionalSubscriber<Integer, Integer>(ts) {

            @Override
            protected boolean beforeDownstream() {
                return false;
            }

            @Override
            public void onNext(@NonNull Integer t) {
                ts.onNext(t);
            }

            @Override
            public int requestFusion(int mode) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public boolean tryOnNext(@NonNull Integer t) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            @Nullable
            public Integer poll() throws Throwable {
                return null;
            }
        };
        bfs.onSubscribe(new BooleanSubscription());
        assertEquals(QueueFuseable.NONE, bfs.transitiveBoundaryFusion(QueueFuseable.ANY));
    }

    @Test
    public void transitiveBoundaryFusionAsync() {
        @SuppressWarnings("unchecked")
        ConditionalSubscriber<Integer> ts = mock(ConditionalSubscriber.class);
        BasicFuseableConditionalSubscriber<Integer, Integer> bfs = new BasicFuseableConditionalSubscriber<Integer, Integer>(ts) {

            @Override
            protected boolean beforeDownstream() {
                return false;
            }

            @Override
            public void onNext(@NonNull Integer t) {
                ts.onNext(t);
            }

            @Override
            public int requestFusion(int mode) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public boolean tryOnNext(@NonNull Integer t) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            @Nullable
            public Integer poll() throws Throwable {
                return null;
            }
        };
        bfs.onSubscribe(EmptySubscription.INSTANCE);
        assertEquals(QueueFuseable.ASYNC, bfs.transitiveBoundaryFusion(QueueFuseable.ANY));
    }

    @Test
    public void transitiveBoundaryFusionAsyncBoundary() {
        @SuppressWarnings("unchecked")
        ConditionalSubscriber<Integer> ts = mock(ConditionalSubscriber.class);
        BasicFuseableConditionalSubscriber<Integer, Integer> bfs = new BasicFuseableConditionalSubscriber<Integer, Integer>(ts) {

            @Override
            protected boolean beforeDownstream() {
                return false;
            }

            @Override
            public void onNext(@NonNull Integer t) {
                ts.onNext(t);
            }

            @Override
            public int requestFusion(int mode) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public boolean tryOnNext(@NonNull Integer t) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            @Nullable
            public Integer poll() throws Throwable {
                return null;
            }
        };
        bfs.onSubscribe(EmptySubscription.INSTANCE);
        assertEquals(QueueFuseable.NONE, bfs.transitiveBoundaryFusion(QueueFuseable.ANY | QueueFuseable.BOUNDARY));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private BasicFuseableConditionalSubscriberTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_offerThrows() throws java.lang.Throwable {
            this.payloads.offerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_implementationStopsOnSubscribe() throws java.lang.Throwable {
            this.payloads.implementationStopsOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_transitiveBoundaryFusionNone() throws java.lang.Throwable {
            this.payloads.transitiveBoundaryFusionNone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_transitiveBoundaryFusionAsync() throws java.lang.Throwable {
            this.payloads.transitiveBoundaryFusionAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_transitiveBoundaryFusionAsyncBoundary() throws java.lang.Throwable {
            this.payloads.transitiveBoundaryFusionAsyncBoundary.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BasicFuseableConditionalSubscriberTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BasicFuseableConditionalSubscriberTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BasicFuseableConditionalSubscriberTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BasicFuseableConditionalSubscriberTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new BasicFuseableConditionalSubscriberTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<BasicFuseableConditionalSubscriberTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(BasicFuseableConditionalSubscriberTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(BasicFuseableConditionalSubscriberTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement offerThrows;

            public org.junit.runners.model.Statement implementationStopsOnSubscribe;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement transitiveBoundaryFusionNone;

            public org.junit.runners.model.Statement transitiveBoundaryFusionAsync;

            public org.junit.runners.model.Statement transitiveBoundaryFusionAsyncBoundary;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.offerThrows = _ClassStatement.forPayload(BasicFuseableConditionalSubscriberTest::offerThrows, "offerThrows", this);
            this.payloads.implementationStopsOnSubscribe = _ClassStatement.forPayload(BasicFuseableConditionalSubscriberTest::implementationStopsOnSubscribe, "implementationStopsOnSubscribe", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(BasicFuseableConditionalSubscriberTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.transitiveBoundaryFusionNone = _ClassStatement.forPayload(BasicFuseableConditionalSubscriberTest::transitiveBoundaryFusionNone, "transitiveBoundaryFusionNone", this);
            this.payloads.transitiveBoundaryFusionAsync = _ClassStatement.forPayload(BasicFuseableConditionalSubscriberTest::transitiveBoundaryFusionAsync, "transitiveBoundaryFusionAsync", this);
            this.payloads.transitiveBoundaryFusionAsyncBoundary = _ClassStatement.forPayload(BasicFuseableConditionalSubscriberTest::transitiveBoundaryFusionAsyncBoundary, "transitiveBoundaryFusionAsyncBoundary", this);
        }
    }
}
