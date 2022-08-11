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

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.TestScheduler;

public class BlockingFlowableMostRecentTest extends RxJavaTest {

    @Test
    public void mostRecent() {
        FlowableProcessor<String> s = PublishProcessor.create();
        Iterator<String> it = s.blockingMostRecent("default").iterator();
        assertTrue(it.hasNext());
        assertEquals("default", it.next());
        assertEquals("default", it.next());
        s.onNext("one");
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        assertEquals("one", it.next());
        s.onNext("two");
        assertTrue(it.hasNext());
        assertEquals("two", it.next());
        assertEquals("two", it.next());
        s.onComplete();
        assertFalse(it.hasNext());
    }

    @Test(expected = TestException.class)
    public void mostRecentWithException() {
        FlowableProcessor<String> s = PublishProcessor.create();
        Iterator<String> it = s.blockingMostRecent("default").iterator();
        assertTrue(it.hasNext());
        assertEquals("default", it.next());
        assertEquals("default", it.next());
        s.onError(new TestException());
        assertTrue(it.hasNext());
        it.next();
    }

    @Test
    public void singleSourceManyIterators() {
        TestScheduler scheduler = new TestScheduler();
        Flowable<Long> source = Flowable.interval(1, TimeUnit.SECONDS, scheduler).take(10);
        Iterable<Long> iter = source.blockingMostRecent(-1L);
        for (int j = 0; j < 3; j++) {
            Iterator<Long> it = iter.iterator();
            Assert.assertEquals(Long.valueOf(-1), it.next());
            for (int i = 0; i < 9; i++) {
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
                Assert.assertTrue(it.hasNext());
                Assert.assertEquals(Long.valueOf(i), it.next());
            }
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            Assert.assertFalse(it.hasNext());
        }
    }

    @Test
    public void empty() {
        Iterator<Integer> it = Flowable.<Integer>empty().blockingMostRecent(1).iterator();
        try {
            it.next();
            fail("Should have thrown");
        } catch (NoSuchElementException ex) {
        // expected
        }
        try {
            it.remove();
            fail("Should have thrown");
        } catch (UnsupportedOperationException ex) {
        // expected
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private BlockingFlowableMostRecentTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mostRecent() throws java.lang.Throwable {
            this.payloads.mostRecent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mostRecentWithException() throws java.lang.Throwable {
            this.payloads.mostRecentWithException.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSourceManyIterators() throws java.lang.Throwable {
            this.payloads.singleSourceManyIterators.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableMostRecentTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableMostRecentTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableMostRecentTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableMostRecentTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new BlockingFlowableMostRecentTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableMostRecentTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(BlockingFlowableMostRecentTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(BlockingFlowableMostRecentTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement mostRecent;

            public org.junit.runners.model.Statement mostRecentWithException;

            public org.junit.runners.model.Statement singleSourceManyIterators;

            public org.junit.runners.model.Statement empty;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.mostRecent = _ClassStatement.forPayload(BlockingFlowableMostRecentTest::mostRecent, "mostRecent", this);
            this.payloads.mostRecentWithException = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableMostRecentTest::mostRecentWithException, io.reactivex.rxjava3.exceptions.TestException.class), "mostRecentWithException", this);
            this.payloads.singleSourceManyIterators = _ClassStatement.forPayload(BlockingFlowableMostRecentTest::singleSourceManyIterators, "singleSourceManyIterators", this);
            this.payloads.empty = _ClassStatement.forPayload(BlockingFlowableMostRecentTest::empty, "empty", this);
        }
    }
}
