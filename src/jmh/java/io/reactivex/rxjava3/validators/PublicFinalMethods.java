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
package io.reactivex.rxjava3.validators;

import static org.junit.Assert.fail;
import java.lang.reflect.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;

/**
 * Verifies that instance methods of the base reactive classes are all declared final.
 */
public class PublicFinalMethods {

    static void scan(Class<?> clazz) {
        for (Method m : clazz.getMethods()) {
            if (m.getDeclaringClass() == clazz) {
                if ((m.getModifiers() & Modifier.STATIC) == 0) {
                    if ((m.getModifiers() & (Modifier.PUBLIC | Modifier.FINAL)) == Modifier.PUBLIC) {
                        fail("Not final: " + m);
                    }
                }
            }
        }
    }

    @Test
    public void flowable() {
        scan(Flowable.class);
    }

    @Test
    public void observable() {
        scan(Observable.class);
    }

    @Test
    public void single() {
        scan(Single.class);
    }

    @Test
    public void completable() {
        scan(Completable.class);
    }

    @Test
    public void maybe() {
        scan(Maybe.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private PublicFinalMethods instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.flowable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.observable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_single() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.single);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completable() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.completable);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybe() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.maybe);
        }

        private void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<PublicFinalMethods> payload) throws java.lang.Throwable {
            this.instance = new PublicFinalMethods();
            payload.accept(this.instance);
        }

        private static class _Payloads {

            public se.chalmers.ju2jmh.api.ThrowingConsumer<PublicFinalMethods> flowable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<PublicFinalMethods> observable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<PublicFinalMethods> single;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<PublicFinalMethods> completable;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<PublicFinalMethods> maybe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.flowable = PublicFinalMethods::flowable;
            this.payloads.observable = PublicFinalMethods::observable;
            this.payloads.single = PublicFinalMethods::single;
            this.payloads.completable = PublicFinalMethods::completable;
            this.payloads.maybe = PublicFinalMethods::maybe;
        }
    }
}
