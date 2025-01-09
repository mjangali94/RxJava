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
package io.reactivex.rxjava3.internal.operators.single;

import static org.junit.Assert.*;
import java.util.Arrays;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleConcatTest extends RxJavaTest {

    @Test
    public void concatWith() {
        Single.just(1).concatWith(Single.just(2)).test().assertResult(1, 2);
    }

    @Test
    public void concat2() {
        Single.concat(Single.just(1), Single.just(2)).test().assertResult(1, 2);
    }

    @Test
    public void concat3() {
        Single.concat(Single.just(1), Single.just(2), Single.just(3)).test().assertResult(1, 2, 3);
    }

    @Test
    public void concat4() {
        Single.concat(Single.just(1), Single.just(2), Single.just(3), Single.just(4)).test().assertResult(1, 2, 3, 4);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void concatArray() {
        for (int i = 1; i < 100; i++) {
            Single<Integer>[] array = new Single[i];
            Arrays.fill(array, Single.just(1));
            Single.concatArray(array).to(TestHelper.<Integer>testConsumer()).assertSubscribed().assertValueCount(i).assertNoErrors().assertComplete();
        }
    }

    @Test
    public void concatArrayEagerTest() {
        PublishProcessor<String> pp1 = PublishProcessor.create();
        PublishProcessor<String> pp2 = PublishProcessor.create();
        TestSubscriber<String> ts = Single.concatArrayEager(pp1.single("1"), pp2.single("2")).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        ts.assertEmpty();
        pp1.onComplete();
        ts.assertResult("1", "2");
        ts.assertComplete();
    }

    @Test
    public void concatEagerIterableTest() {
        PublishProcessor<String> pp1 = PublishProcessor.create();
        PublishProcessor<String> pp2 = PublishProcessor.create();
        TestSubscriber<String> ts = Single.concatEager(Arrays.asList(pp1.single("2"), pp2.single("1"))).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        ts.assertEmpty();
        pp1.onComplete();
        ts.assertResult("2", "1");
        ts.assertComplete();
    }

    @Test
    public void concatEagerPublisherTest() {
        PublishProcessor<String> pp1 = PublishProcessor.create();
        PublishProcessor<String> pp2 = PublishProcessor.create();
        TestSubscriber<String> ts = Single.concatEager(Flowable.just(pp1.single("1"), pp2.single("2"))).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        ts.assertEmpty();
        pp1.onComplete();
        ts.assertResult("1", "2");
        ts.assertComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void concatObservable() {
        for (int i = 1; i < 100; i++) {
            Single<Integer>[] array = new Single[i];
            Arrays.fill(array, Single.just(1));
            Single.concat(Observable.fromArray(array)).to(TestHelper.<Integer>testConsumer()).assertSubscribed().assertValueCount(i).assertNoErrors().assertComplete();
        }
    }

    @Test
    public void noSubsequentSubscription() {
        final int[] calls = { 0 };
        Single<Integer> source = Single.create(new SingleOnSubscribe<Integer>() {

            @Override
            public void subscribe(SingleEmitter<Integer> s) throws Exception {
                calls[0]++;
                s.onSuccess(1);
            }
        });
        Single.concatArray(source, source).firstElement().test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @Test
    public void noSubsequentSubscriptionIterable() {
        final int[] calls = { 0 };
        Single<Integer> source = Single.create(new SingleOnSubscribe<Integer>() {

            @Override
            public void subscribe(SingleEmitter<Integer> s) throws Exception {
                calls[0]++;
                s.onSuccess(1);
            }
        });
        Single.concat(Arrays.asList(source, source)).firstElement().test().assertResult(1);
        assertEquals(1, calls[0]);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public SingleConcatTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWith() throws java.lang.Throwable {
            this.payloads.concatWith.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat2() throws java.lang.Throwable {
            this.payloads.concat2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat3() throws java.lang.Throwable {
            this.payloads.concat3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat4() throws java.lang.Throwable {
            this.payloads.concat4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArray() throws java.lang.Throwable {
            this.payloads.concatArray.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArrayEagerTest() throws java.lang.Throwable {
            this.payloads.concatArrayEagerTest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatEagerIterableTest() throws java.lang.Throwable {
            this.payloads.concatEagerIterableTest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatEagerPublisherTest() throws java.lang.Throwable {
            this.payloads.concatEagerPublisherTest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservable() throws java.lang.Throwable {
            this.payloads.concatObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubsequentSubscription() throws java.lang.Throwable {
            this.payloads.noSubsequentSubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubsequentSubscriptionIterable() throws java.lang.Throwable {
            this.payloads.noSubsequentSubscriptionIterable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleConcatTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleConcatTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleConcatTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleConcatTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement concatWith;

            public org.junit.runners.model.Statement concat2;

            public org.junit.runners.model.Statement concat3;

            public org.junit.runners.model.Statement concat4;

            public org.junit.runners.model.Statement concatArray;

            public org.junit.runners.model.Statement concatArrayEagerTest;

            public org.junit.runners.model.Statement concatEagerIterableTest;

            public org.junit.runners.model.Statement concatEagerPublisherTest;

            public org.junit.runners.model.Statement concatObservable;

            public org.junit.runners.model.Statement noSubsequentSubscription;

            public org.junit.runners.model.Statement noSubsequentSubscriptionIterable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.concatWith = _ClassStatement.forPayload(SingleConcatTest::concatWith, "concatWith", this);
            this.payloads.concat2 = _ClassStatement.forPayload(SingleConcatTest::concat2, "concat2", this);
            this.payloads.concat3 = _ClassStatement.forPayload(SingleConcatTest::concat3, "concat3", this);
            this.payloads.concat4 = _ClassStatement.forPayload(SingleConcatTest::concat4, "concat4", this);
            this.payloads.concatArray = _ClassStatement.forPayload(SingleConcatTest::concatArray, "concatArray", this);
            this.payloads.concatArrayEagerTest = _ClassStatement.forPayload(SingleConcatTest::concatArrayEagerTest, "concatArrayEagerTest", this);
            this.payloads.concatEagerIterableTest = _ClassStatement.forPayload(SingleConcatTest::concatEagerIterableTest, "concatEagerIterableTest", this);
            this.payloads.concatEagerPublisherTest = _ClassStatement.forPayload(SingleConcatTest::concatEagerPublisherTest, "concatEagerPublisherTest", this);
            this.payloads.concatObservable = _ClassStatement.forPayload(SingleConcatTest::concatObservable, "concatObservable", this);
            this.payloads.noSubsequentSubscription = _ClassStatement.forPayload(SingleConcatTest::noSubsequentSubscription, "noSubsequentSubscription", this);
            this.payloads.noSubsequentSubscriptionIterable = _ClassStatement.forPayload(SingleConcatTest::noSubsequentSubscriptionIterable, "noSubsequentSubscriptionIterable", this);
        }
    }
}
