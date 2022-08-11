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
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;

public class FlowableDoOnSubscribeTest extends RxJavaTest {

    @Test
    public void doOnSubscribe() throws Exception {
        final AtomicInteger count = new AtomicInteger();
        Flowable<Integer> f = Flowable.just(1).doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) {
                count.incrementAndGet();
            }
        });
        f.subscribe();
        f.subscribe();
        f.subscribe();
        assertEquals(3, count.get());
    }

    @Test
    public void doOnSubscribe2() throws Exception {
        final AtomicInteger count = new AtomicInteger();
        Flowable<Integer> f = Flowable.just(1).doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) {
                count.incrementAndGet();
            }
        }).take(1).doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) {
                count.incrementAndGet();
            }
        });
        f.subscribe();
        assertEquals(2, count.get());
    }

    @Test
    public void doOnUnSubscribeWorksWithRefCount() throws Exception {
        final AtomicInteger onSubscribed = new AtomicInteger();
        final AtomicInteger countBefore = new AtomicInteger();
        final AtomicInteger countAfter = new AtomicInteger();
        final AtomicReference<Subscriber<? super Integer>> sref = new AtomicReference<>();
        Flowable<Integer> f = Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                onSubscribed.incrementAndGet();
                sref.set(s);
            }
        }).doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) {
                countBefore.incrementAndGet();
            }
        }).publish().refCount().doOnSubscribe(new Consumer<Subscription>() {

            @Override
            public void accept(Subscription s) {
                countAfter.incrementAndGet();
            }
        });
        f.subscribe();
        f.subscribe();
        f.subscribe();
        assertEquals(1, countBefore.get());
        assertEquals(1, onSubscribed.get());
        assertEquals(3, countAfter.get());
        sref.get().onComplete();
        f.subscribe();
        f.subscribe();
        f.subscribe();
        assertEquals(2, countBefore.get());
        assertEquals(2, onSubscribed.get());
        assertEquals(6, countAfter.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableDoOnSubscribeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribe() throws java.lang.Throwable {
            this.payloads.doOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribe2() throws java.lang.Throwable {
            this.payloads.doOnSubscribe2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnUnSubscribeWorksWithRefCount() throws java.lang.Throwable {
            this.payloads.doOnUnSubscribeWorksWithRefCount.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnSubscribeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnSubscribeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnSubscribeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnSubscribeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableDoOnSubscribeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoOnSubscribeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableDoOnSubscribeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableDoOnSubscribeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement doOnSubscribe;

            public org.junit.runners.model.Statement doOnSubscribe2;

            public org.junit.runners.model.Statement doOnUnSubscribeWorksWithRefCount;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.doOnSubscribe = _ClassStatement.forPayload(FlowableDoOnSubscribeTest::doOnSubscribe, "doOnSubscribe", this);
            this.payloads.doOnSubscribe2 = _ClassStatement.forPayload(FlowableDoOnSubscribeTest::doOnSubscribe2, "doOnSubscribe2", this);
            this.payloads.doOnUnSubscribeWorksWithRefCount = _ClassStatement.forPayload(FlowableDoOnSubscribeTest::doOnUnSubscribeWorksWithRefCount, "doOnUnSubscribeWorksWithRefCount", this);
        }
    }
}
