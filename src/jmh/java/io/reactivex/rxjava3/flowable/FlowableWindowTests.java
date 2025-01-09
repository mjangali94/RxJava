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
package io.reactivex.rxjava3.flowable;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

public class FlowableWindowTests extends RxJavaTest {

    @Test
    public void window() {
        final ArrayList<List<Integer>> lists = new ArrayList<>();
        Flowable.concat(Flowable.just(1, 2, 3, 4, 5, 6).window(3).map(new Function<Flowable<Integer>, Flowable<List<Integer>>>() {

            @Override
            public Flowable<List<Integer>> apply(Flowable<Integer> xs) {
                return xs.toList().toFlowable();
            }
        })).blockingForEach(new Consumer<List<Integer>>() {

            @Override
            public void accept(List<Integer> xs) {
                lists.add(xs);
            }
        });
        assertArrayEquals(lists.get(0).toArray(new Integer[3]), new Integer[] { 1, 2, 3 });
        assertArrayEquals(lists.get(1).toArray(new Integer[3]), new Integer[] { 4, 5, 6 });
        assertEquals(2, lists.size());
    }

    @Test
    public void timeSizeWindowAlternatingBounds() {
        TestScheduler scheduler = new TestScheduler();
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<List<Integer>> ts = pp.window(5, TimeUnit.SECONDS, scheduler, 2).flatMapSingle(new Function<Flowable<Integer>, SingleSource<List<Integer>>>() {

            @Override
            public SingleSource<List<Integer>> apply(Flowable<Integer> v) throws Throwable {
                return v.toList();
            }
        }).test();
        pp.onNext(1);
        pp.onNext(2);
        // size bound hit
        ts.assertValueCount(1);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        pp.onNext(3);
        scheduler.advanceTimeBy(6, TimeUnit.SECONDS);
        // time bound hit
        ts.assertValueCount(2);
        pp.onNext(4);
        pp.onNext(5);
        // size bound hit again
        ts.assertValueCount(3);
        pp.onNext(4);
        scheduler.advanceTimeBy(6, TimeUnit.SECONDS);
        ts.assertValueCount(4).assertNoErrors().assertNotComplete();
        ts.cancel();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableWindowTests instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_window() throws java.lang.Throwable {
            this.payloads.window.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeSizeWindowAlternatingBounds() throws java.lang.Throwable {
            this.payloads.timeSizeWindowAlternatingBounds.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowTests> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableWindowTests();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableWindowTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableWindowTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableWindowTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement window;

            public org.junit.runners.model.Statement timeSizeWindowAlternatingBounds;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.window = _ClassStatement.forPayload(FlowableWindowTests::window, "window", this);
            this.payloads.timeSizeWindowAlternatingBounds = _ClassStatement.forPayload(FlowableWindowTests::timeSizeWindowAlternatingBounds, "timeSizeWindowAlternatingBounds", this);
        }
    }
}
