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
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.flowable.FlowableCovarianceTest.*;
import io.reactivex.rxjava3.functions.BiFunction;

public class FlowableReduceTests extends RxJavaTest {

    @Test
    public void reduceIntsFlowable() {
        Flowable<Integer> f = Flowable.just(1, 2, 3);
        int value = f.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).toFlowable().blockingSingle();
        assertEquals(6, value);
    }

    @SuppressWarnings("unused")
    @Test
    public void reduceWithObjectsFlowable() {
        Flowable<Movie> horrorMovies = Flowable.<Movie>just(new HorrorMovie());
        Flowable<Movie> reduceResult = horrorMovies.scan(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        }).takeLast(1);
        Flowable<Movie> reduceResult2 = horrorMovies.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        }).toFlowable();
        assertNotNull(reduceResult2);
    }

    /**
     * Reduce consumes and produces T so can't do covariance.
     *
     * https://github.com/ReactiveX/RxJava/issues/360#issuecomment-24203016
     */
    @Test
    public void reduceWithCovariantObjectsFlowable() {
        Flowable<Movie> horrorMovies = Flowable.<Movie>just(new HorrorMovie());
        Flowable<Movie> reduceResult2 = horrorMovies.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        }).toFlowable();
        assertNotNull(reduceResult2);
    }

    @Test
    public void reduceInts() {
        Flowable<Integer> f = Flowable.just(1, 2, 3);
        int value = f.reduce(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) {
                return t1 + t2;
            }
        }).toFlowable().blockingSingle();
        assertEquals(6, value);
    }

    @SuppressWarnings("unused")
    @Test
    public void reduceWithObjects() {
        Flowable<Movie> horrorMovies = Flowable.<Movie>just(new HorrorMovie());
        Flowable<Movie> reduceResult = horrorMovies.scan(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        }).takeLast(1);
        Maybe<Movie> reduceResult2 = horrorMovies.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        });
        assertNotNull(reduceResult2);
    }

    /**
     * Reduce consumes and produces T so can't do covariance.
     *
     * https://github.com/ReactiveX/RxJava/issues/360#issuecomment-24203016
     */
    @Test
    public void reduceWithCovariantObjects() {
        Flowable<Movie> horrorMovies = Flowable.<Movie>just(new HorrorMovie());
        Maybe<Movie> reduceResult2 = horrorMovies.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        });
        assertNotNull(reduceResult2);
    }

    /**
     * Reduce consumes and produces T so can't do covariance.
     *
     * https://github.com/ReactiveX/RxJava/issues/360#issuecomment-24203016
     */
    @Test
    public void reduceCovariance() {
        // must type it to <Movie>
        Flowable<Movie> horrorMovies = Flowable.<Movie>just(new HorrorMovie());
        libraryFunctionActingOnMovieObservables(horrorMovies);
    }

    /*
     * This accepts <Movie> instead of <? super Movie> since `reduce` can't handle covariants
     */
    public void libraryFunctionActingOnMovieObservables(Flowable<Movie> obs) {
        obs.reduce(new BiFunction<Movie, Movie, Movie>() {

            @Override
            public Movie apply(Movie t1, Movie t2) {
                return t2;
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableReduceTests instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceIntsFlowable() throws java.lang.Throwable {
            this.payloads.reduceIntsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithObjectsFlowable() throws java.lang.Throwable {
            this.payloads.reduceWithObjectsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithCovariantObjectsFlowable() throws java.lang.Throwable {
            this.payloads.reduceWithCovariantObjectsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceInts() throws java.lang.Throwable {
            this.payloads.reduceInts.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithObjects() throws java.lang.Throwable {
            this.payloads.reduceWithObjects.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceWithCovariantObjects() throws java.lang.Throwable {
            this.payloads.reduceWithCovariantObjects.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reduceCovariance() throws java.lang.Throwable {
            this.payloads.reduceCovariance.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReduceTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReduceTests> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReduceTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReduceTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableReduceTests();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableReduceTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableReduceTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableReduceTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement reduceIntsFlowable;

            public org.junit.runners.model.Statement reduceWithObjectsFlowable;

            public org.junit.runners.model.Statement reduceWithCovariantObjectsFlowable;

            public org.junit.runners.model.Statement reduceInts;

            public org.junit.runners.model.Statement reduceWithObjects;

            public org.junit.runners.model.Statement reduceWithCovariantObjects;

            public org.junit.runners.model.Statement reduceCovariance;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.reduceIntsFlowable = _ClassStatement.forPayload(FlowableReduceTests::reduceIntsFlowable, "reduceIntsFlowable", this);
            this.payloads.reduceWithObjectsFlowable = _ClassStatement.forPayload(FlowableReduceTests::reduceWithObjectsFlowable, "reduceWithObjectsFlowable", this);
            this.payloads.reduceWithCovariantObjectsFlowable = _ClassStatement.forPayload(FlowableReduceTests::reduceWithCovariantObjectsFlowable, "reduceWithCovariantObjectsFlowable", this);
            this.payloads.reduceInts = _ClassStatement.forPayload(FlowableReduceTests::reduceInts, "reduceInts", this);
            this.payloads.reduceWithObjects = _ClassStatement.forPayload(FlowableReduceTests::reduceWithObjects, "reduceWithObjects", this);
            this.payloads.reduceWithCovariantObjects = _ClassStatement.forPayload(FlowableReduceTests::reduceWithCovariantObjects, "reduceWithCovariantObjects", this);
            this.payloads.reduceCovariance = _ClassStatement.forPayload(FlowableReduceTests::reduceCovariance, "reduceCovariance", this);
        }
    }
}
