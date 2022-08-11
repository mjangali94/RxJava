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
import java.util.List;
import org.junit.Test;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.flowable.FlowableCovarianceTest.*;
import io.reactivex.rxjava3.functions.Supplier;

public class FlowableMergeTests extends RxJavaTest {

    /**
     * This won't compile if super/extends isn't done correctly on generics.
     */
    @Test
    public void covarianceOfMerge() {
        Flowable<HorrorMovie> horrors = Flowable.just(new HorrorMovie());
        Flowable<Flowable<HorrorMovie>> metaHorrors = Flowable.just(horrors);
        Flowable.<Media>merge(metaHorrors);
    }

    @Test
    public void mergeCovariance() {
        Flowable<Media> f1 = Flowable.<Media>just(new HorrorMovie(), new Movie());
        Flowable<Media> f2 = Flowable.just(new Media(), new HorrorMovie());
        Flowable<Flowable<Media>> os = Flowable.just(f1, f2);
        List<Media> values = Flowable.merge(os).toList().blockingGet();
        assertEquals(4, values.size());
    }

    @Test
    public void mergeCovariance2() {
        Flowable<Media> f1 = Flowable.just(new HorrorMovie(), new Movie(), new Media());
        Flowable<Media> f2 = Flowable.just(new Media(), new HorrorMovie());
        Flowable<Flowable<Media>> os = Flowable.just(f1, f2);
        List<Media> values = Flowable.merge(os).toList().blockingGet();
        assertEquals(5, values.size());
    }

    @Test
    public void mergeCovariance3() {
        Flowable<Movie> f1 = Flowable.just(new HorrorMovie(), new Movie());
        Flowable<Media> f2 = Flowable.just(new Media(), new HorrorMovie());
        List<Media> values = Flowable.merge(f1, f2).toList().blockingGet();
        assertTrue(values.get(0) instanceof HorrorMovie);
        assertTrue(values.get(1) instanceof Movie);
        assertNotNull(values.get(2));
        assertTrue(values.get(3) instanceof HorrorMovie);
    }

    @Test
    public void mergeCovariance4() {
        Flowable<Movie> f1 = Flowable.defer(new Supplier<Publisher<Movie>>() {

            @Override
            public Publisher<Movie> get() {
                return Flowable.just(new HorrorMovie(), new Movie());
            }
        });
        Flowable<Media> f2 = Flowable.just(new Media(), new HorrorMovie());
        List<Media> values = Flowable.merge(f1, f2).toList().blockingGet();
        assertTrue(values.get(0) instanceof HorrorMovie);
        assertTrue(values.get(1) instanceof Movie);
        assertNotNull(values.get(2));
        assertTrue(values.get(3) instanceof HorrorMovie);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableMergeTests instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_covarianceOfMerge() throws java.lang.Throwable {
            this.payloads.covarianceOfMerge.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeCovariance() throws java.lang.Throwable {
            this.payloads.mergeCovariance.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeCovariance2() throws java.lang.Throwable {
            this.payloads.mergeCovariance2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeCovariance3() throws java.lang.Throwable {
            this.payloads.mergeCovariance3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeCovariance4() throws java.lang.Throwable {
            this.payloads.mergeCovariance4.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeTests> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableMergeTests();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMergeTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableMergeTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableMergeTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement covarianceOfMerge;

            public org.junit.runners.model.Statement mergeCovariance;

            public org.junit.runners.model.Statement mergeCovariance2;

            public org.junit.runners.model.Statement mergeCovariance3;

            public org.junit.runners.model.Statement mergeCovariance4;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.covarianceOfMerge = _ClassStatement.forPayload(FlowableMergeTests::covarianceOfMerge, "covarianceOfMerge", this);
            this.payloads.mergeCovariance = _ClassStatement.forPayload(FlowableMergeTests::mergeCovariance, "mergeCovariance", this);
            this.payloads.mergeCovariance2 = _ClassStatement.forPayload(FlowableMergeTests::mergeCovariance2, "mergeCovariance2", this);
            this.payloads.mergeCovariance3 = _ClassStatement.forPayload(FlowableMergeTests::mergeCovariance3, "mergeCovariance3", this);
            this.payloads.mergeCovariance4 = _ClassStatement.forPayload(FlowableMergeTests::mergeCovariance4, "mergeCovariance4", this);
        }
    }
}
