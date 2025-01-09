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
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.flowables.GroupedFlowable;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.testsupport.TestSubscriberEx;

/**
 * Test super/extends of generics.
 *
 * See https://github.com/Netflix/RxJava/pull/331
 */
public class FlowableCovarianceTest extends RxJavaTest {

    /**
     * This won't compile if super/extends isn't done correctly on generics.
     */
    @Test
    public void covarianceOfFrom() {
        Flowable.<Movie>just(new HorrorMovie());
        Flowable.<Movie>fromIterable(new ArrayList<HorrorMovie>());
    // Observable.<HorrorMovie>from(new Movie()); // may not compile
    }

    @Test
    public void sortedList() {
        Comparator<Media> sortFunction = new Comparator<Media>() {

            @Override
            public int compare(Media t1, Media t2) {
                return 1;
            }
        };
        // this one would work without the covariance generics
        Flowable<Media> f = Flowable.just(new Movie(), new TVSeason(), new Album());
        f.toSortedList(sortFunction);
        // this one would NOT work without the covariance generics
        Flowable<Movie> f2 = Flowable.just(new Movie(), new ActionMovie(), new HorrorMovie());
        f2.toSortedList(sortFunction);
    }

    @Test
    public void groupByCompose() {
        Flowable<Movie> movies = Flowable.just(new HorrorMovie(), new ActionMovie(), new Movie());
        TestSubscriberEx<String> ts = new TestSubscriberEx<>();
        movies.groupBy(new Function<Movie, Object>() {

            @Override
            public Object apply(Movie v) {
                return v.getClass();
            }
        }).doOnNext(new Consumer<GroupedFlowable<Object, Movie>>() {

            @Override
            public void accept(GroupedFlowable<Object, Movie> g) {
                // System.out.println(g.getKey());
            }
        }).flatMap(new Function<GroupedFlowable<Object, Movie>, Publisher<String>>() {

            @Override
            public Publisher<String> apply(GroupedFlowable<Object, Movie> g) {
                return g.doOnNext(new Consumer<Movie>() {

                    @Override
                    public void accept(Movie v) {
                        // System.out.println(v);
                    }
                }).compose(new FlowableTransformer<Movie, Movie>() {

                    @Override
                    public Publisher<Movie> apply(Flowable<Movie> m) {
                        return m.concatWith(Flowable.just(new ActionMovie()));
                    }
                }).map(new Function<Object, String>() {

                    @Override
                    public String apply(Object v) {
                        return v.toString();
                    }
                });
            }
        }).subscribe(ts);
        ts.assertTerminated();
        ts.assertNoErrors();
        // // System.out.println(ts.getOnNextEvents());
        assertEquals(6, ts.values().size());
    }

    @SuppressWarnings("unused")
    @Test
    public void covarianceOfCompose() {
        Flowable<HorrorMovie> movie = Flowable.just(new HorrorMovie());
        Flowable<Movie> movie2 = movie.compose(new FlowableTransformer<HorrorMovie, Movie>() {

            @Override
            public Publisher<Movie> apply(Flowable<HorrorMovie> t) {
                return Flowable.just(new Movie());
            }
        });
    }

    @SuppressWarnings("unused")
    @Test
    public void covarianceOfCompose2() {
        Flowable<Movie> movie = Flowable.<Movie>just(new HorrorMovie());
        Flowable<HorrorMovie> movie2 = movie.compose(new FlowableTransformer<Movie, HorrorMovie>() {

            @Override
            public Publisher<HorrorMovie> apply(Flowable<Movie> t) {
                return Flowable.just(new HorrorMovie());
            }
        });
    }

    @SuppressWarnings("unused")
    @Test
    public void covarianceOfCompose3() {
        Flowable<Movie> movie = Flowable.<Movie>just(new HorrorMovie());
        Flowable<HorrorMovie> movie2 = movie.compose(new FlowableTransformer<Movie, HorrorMovie>() {

            @Override
            public Publisher<HorrorMovie> apply(Flowable<Movie> t) {
                return Flowable.just(new HorrorMovie()).map(new Function<HorrorMovie, HorrorMovie>() {

                    @Override
                    public HorrorMovie apply(HorrorMovie v) {
                        return v;
                    }
                });
            }
        });
    }

    @SuppressWarnings("unused")
    @Test
    public void covarianceOfCompose4() {
        Flowable<HorrorMovie> movie = Flowable.just(new HorrorMovie());
        Flowable<HorrorMovie> movie2 = movie.compose(new FlowableTransformer<HorrorMovie, HorrorMovie>() {

            @Override
            public Publisher<HorrorMovie> apply(Flowable<HorrorMovie> t1) {
                return t1.map(new Function<HorrorMovie, HorrorMovie>() {

                    @Override
                    public HorrorMovie apply(HorrorMovie v) {
                        return v;
                    }
                });
            }
        });
    }

    @Test
    public void composeWithDeltaLogic() {
        List<Movie> list1 = Arrays.asList(new Movie(), new HorrorMovie(), new ActionMovie());
        List<Movie> list2 = Arrays.asList(new ActionMovie(), new Movie(), new HorrorMovie(), new ActionMovie());
        Flowable<List<Movie>> movies = Flowable.just(list1, list2);
        movies.compose(deltaTransformer);
    }

    static Function<List<List<Movie>>, Flowable<Movie>> calculateDelta = new Function<List<List<Movie>>, Flowable<Movie>>() {

        @Override
        public Flowable<Movie> apply(List<List<Movie>> listOfLists) {
            if (listOfLists.size() == 1) {
                return Flowable.fromIterable(listOfLists.get(0));
            } else {
                // diff the two
                List<Movie> newList = listOfLists.get(1);
                List<Movie> oldList = new ArrayList<>(listOfLists.get(0));
                Set<Movie> delta = new LinkedHashSet<>();
                delta.addAll(newList);
                // remove all that match in old
                delta.removeAll(oldList);
                // filter oldList to those that aren't in the newList
                oldList.removeAll(newList);
                // for all left in the oldList we'll create DROP events
                for (@SuppressWarnings("unused") Movie old : oldList) {
                    delta.add(new Movie());
                }
                return Flowable.fromIterable(delta);
            }
        }
    };

    static FlowableTransformer<List<Movie>, Movie> deltaTransformer = new FlowableTransformer<List<Movie>, Movie>() {

        @Override
        public Publisher<Movie> apply(Flowable<List<Movie>> movieList) {
            return movieList.startWithItem(new ArrayList<>()).buffer(2, 1).skip(1).flatMap(calculateDelta);
        }
    };

    /*
     * Most tests are moved into their applicable classes such as [Operator]Tests.java
     */
    static class Media {
    }

    static class Movie extends Media {
    }

    static class HorrorMovie extends Movie {
    }

    static class ActionMovie extends Movie {
    }

    static class Album extends Media {
    }

    static class TVSeason extends Media {
    }

    static class Rating {
    }

    static class CoolRating extends Rating {
    }

    static class Result {
    }

    static class ExtendedResult extends Result {
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableCovarianceTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_covarianceOfFrom() throws java.lang.Throwable {
            this.payloads.covarianceOfFrom.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sortedList() throws java.lang.Throwable {
            this.payloads.sortedList.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_groupByCompose() throws java.lang.Throwable {
            this.payloads.groupByCompose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_covarianceOfCompose() throws java.lang.Throwable {
            this.payloads.covarianceOfCompose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_covarianceOfCompose2() throws java.lang.Throwable {
            this.payloads.covarianceOfCompose2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_covarianceOfCompose3() throws java.lang.Throwable {
            this.payloads.covarianceOfCompose3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_covarianceOfCompose4() throws java.lang.Throwable {
            this.payloads.covarianceOfCompose4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_composeWithDeltaLogic() throws java.lang.Throwable {
            this.payloads.composeWithDeltaLogic.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCovarianceTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCovarianceTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCovarianceTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCovarianceTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableCovarianceTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCovarianceTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableCovarianceTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableCovarianceTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement covarianceOfFrom;

            public org.junit.runners.model.Statement sortedList;

            public org.junit.runners.model.Statement groupByCompose;

            public org.junit.runners.model.Statement covarianceOfCompose;

            public org.junit.runners.model.Statement covarianceOfCompose2;

            public org.junit.runners.model.Statement covarianceOfCompose3;

            public org.junit.runners.model.Statement covarianceOfCompose4;

            public org.junit.runners.model.Statement composeWithDeltaLogic;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.covarianceOfFrom = _ClassStatement.forPayload(FlowableCovarianceTest::covarianceOfFrom, "covarianceOfFrom", this);
            this.payloads.sortedList = _ClassStatement.forPayload(FlowableCovarianceTest::sortedList, "sortedList", this);
            this.payloads.groupByCompose = _ClassStatement.forPayload(FlowableCovarianceTest::groupByCompose, "groupByCompose", this);
            this.payloads.covarianceOfCompose = _ClassStatement.forPayload(FlowableCovarianceTest::covarianceOfCompose, "covarianceOfCompose", this);
            this.payloads.covarianceOfCompose2 = _ClassStatement.forPayload(FlowableCovarianceTest::covarianceOfCompose2, "covarianceOfCompose2", this);
            this.payloads.covarianceOfCompose3 = _ClassStatement.forPayload(FlowableCovarianceTest::covarianceOfCompose3, "covarianceOfCompose3", this);
            this.payloads.covarianceOfCompose4 = _ClassStatement.forPayload(FlowableCovarianceTest::covarianceOfCompose4, "covarianceOfCompose4", this);
            this.payloads.composeWithDeltaLogic = _ClassStatement.forPayload(FlowableCovarianceTest::composeWithDeltaLogic, "composeWithDeltaLogic", this);
        }
    }
}
