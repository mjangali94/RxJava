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
package io.reactivex.rxjava3.observable;

import static org.junit.Assert.assertEquals;
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.observable.ObservableCovarianceTest.*;

public class ObservableConcatTests extends RxJavaTest {

    @Test
    public void concatSimple() {
        Observable<String> o1 = Observable.just("one", "two");
        Observable<String> o2 = Observable.just("three", "four");
        List<String> values = Observable.concat(o1, o2).toList().blockingGet();
        assertEquals("one", values.get(0));
        assertEquals("two", values.get(1));
        assertEquals("three", values.get(2));
        assertEquals("four", values.get(3));
    }

    @Test
    public void concatWithObservableOfObservable() {
        Observable<String> o1 = Observable.just("one", "two");
        Observable<String> o2 = Observable.just("three", "four");
        Observable<String> o3 = Observable.just("five", "six");
        Observable<Observable<String>> os = Observable.just(o1, o2, o3);
        List<String> values = Observable.concat(os).toList().blockingGet();
        assertEquals("one", values.get(0));
        assertEquals("two", values.get(1));
        assertEquals("three", values.get(2));
        assertEquals("four", values.get(3));
        assertEquals("five", values.get(4));
        assertEquals("six", values.get(5));
    }

    @Test
    public void concatWithIterableOfObservable() {
        Observable<String> o1 = Observable.just("one", "two");
        Observable<String> o2 = Observable.just("three", "four");
        Observable<String> o3 = Observable.just("five", "six");
        Iterable<Observable<String>> is = Arrays.asList(o1, o2, o3);
        List<String> values = Observable.concat(Observable.fromIterable(is)).toList().blockingGet();
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
        Observable<Media> o1 = Observable.<Media>just(horrorMovie1, movie);
        Observable<Media> o2 = Observable.just(media, horrorMovie2);
        Observable<Observable<Media>> os = Observable.just(o1, o2);
        List<Media> values = Observable.concat(os).toList().blockingGet();
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
        Observable<Media> o1 = Observable.just(horrorMovie1, movie, media1);
        Observable<Media> o2 = Observable.just(media2, horrorMovie2);
        Observable<Observable<Media>> os = Observable.just(o1, o2);
        List<Media> values = Observable.concat(os).toList().blockingGet();
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
        Observable<Movie> o1 = Observable.just(horrorMovie1, movie);
        Observable<Media> o2 = Observable.just(media, horrorMovie2);
        List<Media> values = Observable.concat(o1, o2).toList().blockingGet();
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
        Observable<Movie> o1 = Observable.unsafeCreate(new ObservableSource<Movie>() {

            @Override
            public void subscribe(Observer<? super Movie> o) {
                o.onNext(horrorMovie1);
                o.onNext(movie);
                // o.onNext(new Media()); // correctly doesn't compile
                o.onComplete();
            }
        });
        Observable<Media> o2 = Observable.just(media, horrorMovie2);
        List<Media> values = Observable.concat(o1, o2).toList().blockingGet();
        assertEquals(horrorMovie1, values.get(0));
        assertEquals(movie, values.get(1));
        assertEquals(media, values.get(2));
        assertEquals(horrorMovie2, values.get(3));
        assertEquals(4, values.size());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableConcatTests instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatSimple() throws java.lang.Throwable {
            this.payloads.concatSimple.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWithObservableOfObservable() throws java.lang.Throwable {
            this.payloads.concatWithObservableOfObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWithIterableOfObservable() throws java.lang.Throwable {
            this.payloads.concatWithIterableOfObservable.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatTests> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableConcatTests();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableConcatTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableConcatTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableConcatTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement concatSimple;

            public org.junit.runners.model.Statement concatWithObservableOfObservable;

            public org.junit.runners.model.Statement concatWithIterableOfObservable;

            public org.junit.runners.model.Statement concatCovariance;

            public org.junit.runners.model.Statement concatCovariance2;

            public org.junit.runners.model.Statement concatCovariance3;

            public org.junit.runners.model.Statement concatCovariance4;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.concatSimple = _ClassStatement.forPayload(ObservableConcatTests::concatSimple, "concatSimple", this);
            this.payloads.concatWithObservableOfObservable = _ClassStatement.forPayload(ObservableConcatTests::concatWithObservableOfObservable, "concatWithObservableOfObservable", this);
            this.payloads.concatWithIterableOfObservable = _ClassStatement.forPayload(ObservableConcatTests::concatWithIterableOfObservable, "concatWithIterableOfObservable", this);
            this.payloads.concatCovariance = _ClassStatement.forPayload(ObservableConcatTests::concatCovariance, "concatCovariance", this);
            this.payloads.concatCovariance2 = _ClassStatement.forPayload(ObservableConcatTests::concatCovariance2, "concatCovariance2", this);
            this.payloads.concatCovariance3 = _ClassStatement.forPayload(ObservableConcatTests::concatCovariance3, "concatCovariance3", this);
            this.payloads.concatCovariance4 = _ClassStatement.forPayload(ObservableConcatTests::concatCovariance4, "concatCovariance4", this);
        }
    }
}
