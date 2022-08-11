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
package io.reactivex.rxjava3.internal.operators.flowable;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BufferUntilSubscriberTest extends RxJavaTest {

    @Test
    public void issue1677() throws InterruptedException {
        final AtomicLong counter = new AtomicLong();
        final Integer[] numbers = new Integer[5000];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = i + 1;
        }
        final int NITERS = 250;
        final CountDownLatch latch = new CountDownLatch(NITERS);
        for (int iters = 0; iters < NITERS; iters++) {
            final CountDownLatch innerLatch = new CountDownLatch(1);
            final PublishProcessor<Void> s = PublishProcessor.create();
            final AtomicBoolean completed = new AtomicBoolean();
            Flowable.fromArray(numbers).takeUntil(s).window(50).flatMap(new Function<Flowable<Integer>, Publisher<Object>>() {

                @Override
                public Publisher<Object> apply(Flowable<Integer> integerObservable) {
                    return integerObservable.subscribeOn(Schedulers.computation()).map(new Function<Integer, Object>() {

                        @Override
                        public Object apply(Integer integer) {
                            if (integer >= 5 && completed.compareAndSet(false, true)) {
                                s.onComplete();
                            }
                            // do some work
                            Math.pow(Math.random(), Math.random());
                            return integer * 2;
                        }
                    });
                }
            }).toList().doOnSuccess(new Consumer<List<Object>>() {

                @Override
                public void accept(List<Object> integers) {
                    counter.incrementAndGet();
                    latch.countDown();
                    innerLatch.countDown();
                }
            }).subscribe();
            if (!innerLatch.await(30, TimeUnit.SECONDS)) {
                Assert.fail("Failed inner latch wait, iteration " + iters);
            }
        }
        if (!latch.await(30, TimeUnit.SECONDS)) {
            Assert.fail("Incomplete! Went through " + latch.getCount() + " iterations");
        } else {
            Assert.assertEquals(NITERS, counter.get());
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private BufferUntilSubscriberTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1677() throws java.lang.Throwable {
            this.payloads.issue1677.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BufferUntilSubscriberTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BufferUntilSubscriberTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BufferUntilSubscriberTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BufferUntilSubscriberTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new BufferUntilSubscriberTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<BufferUntilSubscriberTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(BufferUntilSubscriberTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(BufferUntilSubscriberTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement issue1677;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.issue1677 = _ClassStatement.forPayload(BufferUntilSubscriberTest::issue1677, "issue1677", this);
        }
    }
}
