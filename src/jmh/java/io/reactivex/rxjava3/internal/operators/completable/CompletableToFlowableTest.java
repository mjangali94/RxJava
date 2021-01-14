/**
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
package io.reactivex.rxjava3.internal.operators.completable;

import org.junit.Test;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableToFlowableTest extends RxJavaTest {

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeCompletableToFlowable(new Function<Completable, Publisher<?>>() {

            @Override
            public Publisher<?> apply(Completable c) throws Exception {
                return c.toFlowable();
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends io.reactivex.rxjava3.core.RxJavaTest._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doubleOnSubscribe, this.description("doubleOnSubscribe"));
        }

        private CompletableToFlowableTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CompletableToFlowableTest();
        }

        @java.lang.Override
        public CompletableToFlowableTest implementation() {
            return this.implementation;
        }
    }
}
