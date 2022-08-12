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
import org.junit.Test;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.flowable.FlowableCovarianceTest.*;
import io.reactivex.rxjava3.flowable.FlowableEventStream.Event;
import io.reactivex.rxjava3.flowables.GroupedFlowable;
import io.reactivex.rxjava3.functions.*;

public class FlowableZipTests extends RxJavaTest {

    @Test
    public void zipObservableOfObservables() {
        FlowableEventStream.getEventStream("HTTP-ClusterB", 20).groupBy(new Function<Event, String>() {

            @Override
            public String apply(Event e) {
                return e.instanceId;
            }
        }).flatMap(new Function<GroupedFlowable<String, Event>, Publisher<HashMap<String, String>>>() {

            @Override
            public Publisher<HashMap<String, String>> apply(final GroupedFlowable<String, Event> ge) {
                return ge.scan(new HashMap<>(), new BiFunction<HashMap<String, String>, Event, HashMap<String, String>>() {

                    @Override
                    public HashMap<String, String> apply(HashMap<String, String> accum, Event perInstanceEvent) {
                        synchronized (accum) {
                            accum.put("instance", ge.getKey());
                        }
                        return accum;
                    }
                });
            }
        }).take(10).blockingForEach(new Consumer<HashMap<String, String>>() {

            @Override
            public void accept(HashMap<String, String> v) {
                synchronized (v) {
                    // System.out.println(v);
                }
            }
        });
        // System.out.println("**** finished");
    }

    /**
     * This won't compile if super/extends isn't done correctly on generics.
     */
    @Test
    public void covarianceOfZip() {
        Flowable<HorrorMovie> horrors = Flowable.just(new HorrorMovie());
        Flowable<CoolRating> ratings = Flowable.just(new CoolRating());
        Flowable.<Movie, CoolRating, Result>zip(horrors, ratings, combine).blockingForEach(action);
        Flowable.<Movie, CoolRating, Result>zip(horrors, ratings, combine).blockingForEach(action);
        Flowable.<Media, Rating, ExtendedResult>zip(horrors, ratings, combine).blockingForEach(extendedAction);
        Flowable.<Media, Rating, Result>zip(horrors, ratings, combine).blockingForEach(action);
        Flowable.<Media, Rating, ExtendedResult>zip(horrors, ratings, combine).blockingForEach(action);
        Flowable.<Movie, CoolRating, Result>zip(horrors, ratings, combine);
    }

    /**
     * Occasionally zip may be invoked with 0 observables. Test that we don't block indefinitely instead
     * of immediately invoking zip with 0 argument.
     *
     * We now expect an NoSuchElementException since last() requires at least one value and nothing will be emitted.
     */
    @Test(expected = NoSuchElementException.class)
    public void nonBlockingObservable() {
        final Object invoked = new Object();
        Collection<Flowable<Object>> observables = Collections.emptyList();
        Flowable<Object> result = Flowable.zip(observables, new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] args) {
                // System.out.println("received: " + args);
                assertEquals("No argument should have been passed", 0, args.length);
                return invoked;
            }
        });
        assertSame(invoked, result.blockingLast());
    }

    BiFunction<Media, Rating, ExtendedResult> combine = new BiFunction<Media, Rating, ExtendedResult>() {

        @Override
        public ExtendedResult apply(Media m, Rating r) {
            return new ExtendedResult();
        }
    };

    Consumer<Result> action = new Consumer<Result>() {

        @Override
        public void accept(Result t1) {
            // System.out.println("Result: " + t1);
        }
    };

    Consumer<ExtendedResult> extendedAction = new Consumer<ExtendedResult>() {

        @Override
        public void accept(ExtendedResult t1) {
            // System.out.println("Result: " + t1);
        }
    };

    @Test
    public void zipWithDelayError() {
        Flowable.just(1).zipWith(Flowable.just(2), new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }, true).test().assertResult(3);
    }

    @Test
    public void zipWithDelayErrorBufferSize() {
        Flowable.just(1).zipWith(Flowable.just(2), new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }, true, 16).test().assertResult(3);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableZipTests instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipObservableOfObservables() throws java.lang.Throwable {
            this.payloads.zipObservableOfObservables.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_covarianceOfZip() throws java.lang.Throwable {
            this.payloads.covarianceOfZip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonBlockingObservable() throws java.lang.Throwable {
            this.payloads.nonBlockingObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithDelayError() throws java.lang.Throwable {
            this.payloads.zipWithDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWithDelayErrorBufferSize() throws java.lang.Throwable {
            this.payloads.zipWithDelayErrorBufferSize.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipTests> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableZipTests();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableZipTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableZipTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement zipObservableOfObservables;

            public org.junit.runners.model.Statement covarianceOfZip;

            public org.junit.runners.model.Statement nonBlockingObservable;

            public org.junit.runners.model.Statement zipWithDelayError;

            public org.junit.runners.model.Statement zipWithDelayErrorBufferSize;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.zipObservableOfObservables = _ClassStatement.forPayload(FlowableZipTests::zipObservableOfObservables, "zipObservableOfObservables", this);
            this.payloads.covarianceOfZip = _ClassStatement.forPayload(FlowableZipTests::covarianceOfZip, "covarianceOfZip", this);
            this.payloads.nonBlockingObservable = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableZipTests::nonBlockingObservable, java.util.NoSuchElementException.class), "nonBlockingObservable", this);
            this.payloads.zipWithDelayError = _ClassStatement.forPayload(FlowableZipTests::zipWithDelayError, "zipWithDelayError", this);
            this.payloads.zipWithDelayErrorBufferSize = _ClassStatement.forPayload(FlowableZipTests::zipWithDelayErrorBufferSize, "zipWithDelayErrorBufferSize", this);
        }
    }
}
