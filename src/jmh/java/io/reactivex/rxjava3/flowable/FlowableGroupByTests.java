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

import org.junit.Test;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.flowable.FlowableEventStream.Event;
import io.reactivex.rxjava3.flowables.GroupedFlowable;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

public class FlowableGroupByTests extends RxJavaTest {

    @Test
    public void takeUnsubscribesOnGroupBy() {
        Flowable.merge(FlowableEventStream.getEventStream("HTTP-ClusterA", 50), FlowableEventStream.getEventStream("HTTP-ClusterB", 20)).groupBy(new Function<Event, Object>() {

            @Override
            public Object apply(Event event) {
                return event.type;
            }
        }).take(1).blockingForEach(new Consumer<GroupedFlowable<Object, Event>>() {

            @Override
            public void accept(GroupedFlowable<Object, Event> v) {
                // System.out.println(v);
                // FIXME groups need consumption to a certain degree to cancel upstream
                v.take(1).subscribe();
            }
        });
        // System.out.println("**** finished");
    }

    @Test
    public void takeUnsubscribesOnFlatMapOfGroupBy() {
        Flowable.merge(FlowableEventStream.getEventStream("HTTP-ClusterA", 50), FlowableEventStream.getEventStream("HTTP-ClusterB", 20)).groupBy(new Function<Event, Object>() {

            @Override
            public Object apply(Event event) {
                return event.type;
            }
        }).flatMap(new Function<GroupedFlowable<Object, Event>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(GroupedFlowable<Object, Event> g) {
                return g.map(new Function<Event, Object>() {

                    @Override
                    public Object apply(Event event) {
                        return event.instanceId + " - " + event.values.get("count200");
                    }
                });
            }
        }).take(20).blockingForEach(new Consumer<Object>() {

            @Override
            public void accept(Object v) {
                // System.out.println(v);
            }
        });
        // System.out.println("**** finished");
    }

    @Test
    public void groupsCompleteAsSoonAsMainCompletes() {
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Flowable.range(0, 20).groupBy(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer i) {
                return i % 5;
            }
        }).concatMap(new Function<GroupedFlowable<Integer, Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(GroupedFlowable<Integer, Integer> v) {
                return v;
            }
        }, // need to prefetch as many groups as groupBy produces to avoid MBE
        20).subscribe(ts);
        // Behavior change: this now counts as group abandonment because concatMap
        // doesn't subscribe to the 2nd+ emitted groups immediately
        ts.assertValues(// First group is okay
        0, // First group is okay
        5, // First group is okay
        10, // First group is okay
        15, // any other group gets abandoned so we get 16 one-element group
        1, 2, 3, 4, 6, 7, 8, 9, 11, 12, 13, 14, 16, 17, 18, 19);
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableGroupByTests instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUnsubscribesOnGroupBy() throws java.lang.Throwable {
            this.payloads.takeUnsubscribesOnGroupBy.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeUnsubscribesOnFlatMapOfGroupBy() throws java.lang.Throwable {
            this.payloads.takeUnsubscribesOnFlatMapOfGroupBy.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupsCompleteAsSoonAsMainCompletes() throws java.lang.Throwable {
            this.payloads.groupsCompleteAsSoonAsMainCompletes.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupByTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupByTests> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupByTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupByTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableGroupByTests();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupByTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableGroupByTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableGroupByTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement takeUnsubscribesOnGroupBy;

            public org.junit.runners.model.Statement takeUnsubscribesOnFlatMapOfGroupBy;

            public org.junit.runners.model.Statement groupsCompleteAsSoonAsMainCompletes;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.takeUnsubscribesOnGroupBy = _ClassStatement.forPayload(FlowableGroupByTests::takeUnsubscribesOnGroupBy, "takeUnsubscribesOnGroupBy", this);
            this.payloads.takeUnsubscribesOnFlatMapOfGroupBy = _ClassStatement.forPayload(FlowableGroupByTests::takeUnsubscribesOnFlatMapOfGroupBy, "takeUnsubscribesOnFlatMapOfGroupBy", this);
            this.payloads.groupsCompleteAsSoonAsMainCompletes = _ClassStatement.forPayload(FlowableGroupByTests::groupsCompleteAsSoonAsMainCompletes, "groupsCompleteAsSoonAsMainCompletes", this);
        }
    }
}
