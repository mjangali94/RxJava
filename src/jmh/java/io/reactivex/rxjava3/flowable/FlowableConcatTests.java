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

import static org.junit.Assert.assertEquals;
import java.util.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.flowable.FlowableCovarianceTest.*;

public class FlowableConcatTests extends RxJavaTest {

    @Test
    public void concatSimple() {
        Flowable<String> f1 = Flowable.just("one", "two");
        Flowable<String> f2 = Flowable.just("three", "four");
        List<String> values = Flowable.concat(f1, f2).toList().blockingGet();
        assertEquals("one", values.get(0));
        assertEquals("two", values.get(1));
        assertEquals("three", values.get(2));
        assertEquals("four", values.get(3));
    }

    @Test
    public void concatWithFlowableOfFlowable() {
        Flowable<String> f1 = Flowable.just("one", "two");
        Flowable<String> f2 = Flowable.just("three", "four");
        Flowable<String> f3 = Flowable.just("five", "six");
        Flowable<Flowable<String>> os = Flowable.just(f1, f2, f3);
        List<String> values = Flowable.concat(os).toList().blockingGet();
        assertEquals("one", values.get(0));
        assertEquals("two", values.get(1));
        assertEquals("three", values.get(2));
        assertEquals("four", values.get(3));
        assertEquals("five", values.get(4));
        assertEquals("six", values.get(5));
    }

    @Test
    public void concatWithIterableOfFlowable() {
        Flowable<String> f1 = Flowable.just("one", "two");
        Flowable<String> f2 = Flowable.just("three", "four");
        Flowable<String> f3 = Flowable.just("five", "six");
        Iterable<Flowable<String>> is = Arrays.asList(f1, f2, f3);
        List<String> values = Flowable.concat(Flowable.fromIterable(is)).toList().blockingGet();
        assertEquals("one", values.get(0));
        assertEquals("two", values.get(1));
        assertEquals("three", values.get(2));
        assertEquals("four", values.get(3));
        assertEquals("five", values.get(4));
        assertEquals("six", values.get(5));
    }

    @Test
    public void concatCovariance() {
        HorrorMovie horrorMovie1 = new HorrorMovie();
        Movie movie = new Movie();
        Media media = new Media();
        HorrorMovie horrorMovie2 = new HorrorMovie();
        Flowable<Media> f1 = Flowable.<Media>just(horrorMovie1, movie);
        Flowable<Media> f2 = Flowable.just(media, horrorMovie2);
        Flowable<Flowable<Media>> os = Flowable.just(f1, f2);
        List<Media> values = Flowable.concat(os).toList().blockingGet();
        assertEquals(horrorMovie1, values.get(0));
        assertEquals(movie, values.get(1));
        assertEquals(media, values.get(2));
        assertEquals(horrorMovie2, values.get(3));
        assertEquals(4, values.size());
    }

    @Test
    public void concatCovariance2() {
        HorrorMovie horrorMovie1 = new HorrorMovie();
        Movie movie = new Movie();
        Media media1 = new Media();
        Media media2 = new Media();
        HorrorMovie horrorMovie2 = new HorrorMovie();
        Flowable<Media> f1 = Flowable.just(horrorMovie1, movie, media1);
        Flowable<Media> f2 = Flowable.just(media2, horrorMovie2);
        Flowable<Flowable<Media>> os = Flowable.just(f1, f2);
        List<Media> values = Flowable.concat(os).toList().blockingGet();
        assertEquals(horrorMovie1, values.get(0));
        assertEquals(movie, values.get(1));
        assertEquals(media1, values.get(2));
        assertEquals(media2, values.get(3));
        assertEquals(horrorMovie2, values.get(4));
        assertEquals(5, values.size());
    }

    @Test
    public void concatCovariance3() {
        HorrorMovie horrorMovie1 = new HorrorMovie();
        Movie movie = new Movie();
        Media media = new Media();
        HorrorMovie horrorMovie2 = new HorrorMovie();
        Flowable<Movie> f1 = Flowable.just(horrorMovie1, movie);
        Flowable<Media> f2 = Flowable.just(media, horrorMovie2);
        List<Media> values = Flowable.concat(f1, f2).toList().blockingGet();
        assertEquals(horrorMovie1, values.get(0));
        assertEquals(movie, values.get(1));
        assertEquals(media, values.get(2));
        assertEquals(horrorMovie2, values.get(3));
        assertEquals(4, values.size());
    }

    @Test
    public void concatCovariance4() {
        final HorrorMovie horrorMovie1 = new HorrorMovie();
        final Movie movie = new Movie();
        Media media = new Media();
        HorrorMovie horrorMovie2 = new HorrorMovie();
        Flowable<Movie> f1 = Flowable.unsafeCreate(new Publisher<Movie>() {

            @Override
            public void subscribe(Subscriber<? super Movie> subscriber) {
                subscriber.onNext(horrorMovie1);
                subscriber.onNext(movie);
                // o.onNext(new Media()); // correctly doesn't compile
                subscriber.onComplete();
            }
        });
        Flowable<Media> f2 = Flowable.just(media, horrorMovie2);
        List<Media> values = Flowable.concat(f1, f2).toList().blockingGet();
        assertEquals(horrorMovie1, values.get(0));
        assertEquals(movie, values.get(1));
        assertEquals(media, values.get(2));
        assertEquals(horrorMovie2, values.get(3));
        assertEquals(4, values.size());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        public _Payloads payloads;

        public FlowableConcatTests instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatSimple() throws java.lang.Throwable {
            this.payloads.concatSimple.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWithFlowableOfFlowable() throws java.lang.Throwable {
            this.payloads.concatWithFlowableOfFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWithIterableOfFlowable() throws java.lang.Throwable {
            this.payloads.concatWithIterableOfFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatCovariance() throws java.lang.Throwable {
            this.payloads.concatCovariance.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatCovariance2() throws java.lang.Throwable {
            this.payloads.concatCovariance2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatCovariance3() throws java.lang.Throwable {
            this.payloads.concatCovariance3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatCovariance4() throws java.lang.Throwable {
            this.payloads.concatCovariance4.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTests> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableConcatTests();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableConcatTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableConcatTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableConcatTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement concatSimple;

            public org.junit.runners.model.Statement concatWithFlowableOfFlowable;

            public org.junit.runners.model.Statement concatWithIterableOfFlowable;

            public org.junit.runners.model.Statement concatCovariance;

            public org.junit.runners.model.Statement concatCovariance2;

            public org.junit.runners.model.Statement concatCovariance3;

            public org.junit.runners.model.Statement concatCovariance4;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.concatSimple = _ClassStatement.forPayload(FlowableConcatTests::concatSimple, "concatSimple", this);
            this.payloads.concatWithFlowableOfFlowable = _ClassStatement.forPayload(FlowableConcatTests::concatWithFlowableOfFlowable, "concatWithFlowableOfFlowable", this);
            this.payloads.concatWithIterableOfFlowable = _ClassStatement.forPayload(FlowableConcatTests::concatWithIterableOfFlowable, "concatWithIterableOfFlowable", this);
            this.payloads.concatCovariance = _ClassStatement.forPayload(FlowableConcatTests::concatCovariance, "concatCovariance", this);
            this.payloads.concatCovariance2 = _ClassStatement.forPayload(FlowableConcatTests::concatCovariance2, "concatCovariance2", this);
            this.payloads.concatCovariance3 = _ClassStatement.forPayload(FlowableConcatTests::concatCovariance3, "concatCovariance3", this);
            this.payloads.concatCovariance4 = _ClassStatement.forPayload(FlowableConcatTests::concatCovariance4, "concatCovariance4", this);
        }
    }
}
