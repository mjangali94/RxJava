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
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class BlockingFlowableLatestTest extends RxJavaTest {

    @Test
    public void simple() {
        TestScheduler scheduler = new TestScheduler();
        Flowable<Long> source = Flowable.interval(1, TimeUnit.SECONDS, scheduler).take(10);
        Iterable<Long> iter = source.blockingLatest();
        Iterator<Long> it = iter.iterator();
        // only 9 because take(10) will immediately call onComplete when receiving the 10th item
        // which onComplete will overwrite the previous value
        for (int i = 0; i < 9; i++) {
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            Assert.assertTrue(it.hasNext());
            Assert.assertEquals(Long.valueOf(i), it.next());
        }
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void sameSourceMultipleIterators() {
        TestScheduler scheduler = new TestScheduler();
        Flowable<Long> source = Flowable.interval(1, TimeUnit.SECONDS, scheduler).take(10);
        Iterable<Long> iter = source.blockingLatest();
        for (int j = 0; j < 3; j++) {
            Iterator<Long> it = iter.iterator();
            // only 9 because take(10) will immediately call onComplete when receiving the 10th item
            // which onComplete will overwrite the previous value
            for (int i = 0; i < 9; i++) {
                scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
                Assert.assertTrue(it.hasNext());
                Assert.assertEquals(Long.valueOf(i), it.next());
            }
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            Assert.assertFalse(it.hasNext());
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void empty() {
        Flowable<Long> source = Flowable.<Long>empty();
        Iterable<Long> iter = source.blockingLatest();
        Iterator<Long> it = iter.iterator();
        Assert.assertFalse(it.hasNext());
        it.next();
    }

    @Test(expected = NoSuchElementException.class)
    public void simpleJustNext() {
        TestScheduler scheduler = new TestScheduler();
        Flowable<Long> source = Flowable.interval(1, TimeUnit.SECONDS, scheduler).take(10);
        Iterable<Long> iter = source.blockingLatest();
        Iterator<Long> it = iter.iterator();
        // only 9 because take(10) will immediately call onComplete when receiving the 10th item
        // which onComplete will overwrite the previous value
        for (int i = 0; i < 10; i++) {
            scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
            Assert.assertEquals(Long.valueOf(i), it.next());
        }
    }

    @Test(expected = RuntimeException.class)
    public void hasNextThrows() {
        TestScheduler scheduler = new TestScheduler();
        Flowable<Long> source = Flowable.<Long>error(new RuntimeException("Forced failure!")).subscribeOn(scheduler);
        Iterable<Long> iter = source.blockingLatest();
        Iterator<Long> it = iter.iterator();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        it.hasNext();
    }

    @Test(expected = RuntimeException.class)
    public void nextThrows() {
        TestScheduler scheduler = new TestScheduler();
        Flowable<Long> source = Flowable.<Long>error(new RuntimeException("Forced failure!")).subscribeOn(scheduler);
        Iterable<Long> iter = source.blockingLatest();
        Iterator<Long> it = iter.iterator();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        it.next();
    }

    @Test
    public void fasterSource() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<Integer> blocker = source;
        Iterable<Integer> iter = blocker.blockingLatest();
        Iterator<Integer> it = iter.iterator();
        source.onNext(1);
        Assert.assertEquals(Integer.valueOf(1), it.next());
        source.onNext(2);
        source.onNext(3);
        Assert.assertEquals(Integer.valueOf(3), it.next());
        source.onNext(4);
        source.onNext(5);
        source.onNext(6);
        Assert.assertEquals(Integer.valueOf(6), it.next());
        source.onNext(7);
        source.onComplete();
        Assert.assertFalse(it.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        Flowable.never().blockingLatest().iterator().remove();
    }

    @Test
    public void interrupted() {
        Iterator<Object> it = Flowable.never().blockingLatest().iterator();
        Thread.currentThread().interrupt();
        try {
            it.hasNext();
        } catch (RuntimeException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof InterruptedException);
        }
        Thread.interrupted();
    }

    @Test(expected = NoSuchElementException.class)
    public void empty2() {
        Flowable.empty().blockingLatest().iterator().next();
    }

    @Test(expected = TestException.class)
    public void error() {
        Flowable.error(new TestException()).blockingLatest().iterator().next();
    }

    @Test
    public void error2() {
        Iterator<Object> it = Flowable.error(new TestException()).blockingLatest().iterator();
        for (int i = 0; i < 3; i++) {
            try {
                it.hasNext();
                fail("Should have thrown");
            } catch (TestException ex) {
            // expected
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onError() {
        Iterator<Object> it = Flowable.never().blockingLatest().iterator();
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            ((Subscriber<Object>) it).onError(new TestException());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private BlockingFlowableLatestTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simple() throws java.lang.Throwable {
            this.payloads.simple.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sameSourceMultipleIterators() throws java.lang.Throwable {
            this.payloads.sameSourceMultipleIterators.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_simpleJustNext() throws java.lang.Throwable {
            this.payloads.simpleJustNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrows() throws java.lang.Throwable {
            this.payloads.hasNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextThrows() throws java.lang.Throwable {
            this.payloads.nextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fasterSource() throws java.lang.Throwable {
            this.payloads.fasterSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_remove() throws java.lang.Throwable {
            this.payloads.remove.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interrupted() throws java.lang.Throwable {
            this.payloads.interrupted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty2() throws java.lang.Throwable {
            this.payloads.empty2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error2() throws java.lang.Throwable {
            this.payloads.error2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onError() throws java.lang.Throwable {
            this.payloads.onError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableLatestTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableLatestTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableLatestTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableLatestTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new BlockingFlowableLatestTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableLatestTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(BlockingFlowableLatestTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(BlockingFlowableLatestTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement simple;

            public org.junit.runners.model.Statement sameSourceMultipleIterators;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement simpleJustNext;

            public org.junit.runners.model.Statement hasNextThrows;

            public org.junit.runners.model.Statement nextThrows;

            public org.junit.runners.model.Statement fasterSource;

            public org.junit.runners.model.Statement remove;

            public org.junit.runners.model.Statement interrupted;

            public org.junit.runners.model.Statement empty2;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement error2;

            public org.junit.runners.model.Statement onError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.simple = _ClassStatement.forPayload(BlockingFlowableLatestTest::simple, "simple", this);
            this.payloads.sameSourceMultipleIterators = _ClassStatement.forPayload(BlockingFlowableLatestTest::sameSourceMultipleIterators, "sameSourceMultipleIterators", this);
            this.payloads.empty = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableLatestTest::empty, java.util.NoSuchElementException.class), "empty", this);
            this.payloads.simpleJustNext = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableLatestTest::simpleJustNext, java.util.NoSuchElementException.class), "simpleJustNext", this);
            this.payloads.hasNextThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableLatestTest::hasNextThrows, java.lang.RuntimeException.class), "hasNextThrows", this);
            this.payloads.nextThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableLatestTest::nextThrows, java.lang.RuntimeException.class), "nextThrows", this);
            this.payloads.fasterSource = _ClassStatement.forPayload(BlockingFlowableLatestTest::fasterSource, "fasterSource", this);
            this.payloads.remove = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableLatestTest::remove, java.lang.UnsupportedOperationException.class), "remove", this);
            this.payloads.interrupted = _ClassStatement.forPayload(BlockingFlowableLatestTest::interrupted, "interrupted", this);
            this.payloads.empty2 = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableLatestTest::empty2, java.util.NoSuchElementException.class), "empty2", this);
            this.payloads.error = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableLatestTest::error, io.reactivex.rxjava3.exceptions.TestException.class), "error", this);
            this.payloads.error2 = _ClassStatement.forPayload(BlockingFlowableLatestTest::error2, "error2", this);
            this.payloads.onError = _ClassStatement.forPayload(BlockingFlowableLatestTest::onError, "onError", this);
        }
    }
}
