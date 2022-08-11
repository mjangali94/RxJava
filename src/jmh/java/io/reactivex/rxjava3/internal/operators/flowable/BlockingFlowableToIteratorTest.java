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
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.internal.operators.flowable.BlockingFlowableIterable.BlockingFlowableIterator;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BlockingFlowableToIteratorTest extends RxJavaTest {

    @Test
    public void toIterator() {
        Flowable<String> obs = Flowable.just("one", "two", "three");
        Iterator<String> it = obs.blockingIterable().iterator();
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        assertTrue(it.hasNext());
        assertEquals("two", it.next());
        assertTrue(it.hasNext());
        assertEquals("three", it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = TestException.class)
    public void toIteratorWithException() {
        Flowable<String> obs = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                subscriber.onNext("one");
                subscriber.onError(new TestException());
            }
        });
        Iterator<String> it = obs.blockingIterable().iterator();
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        assertTrue(it.hasNext());
        it.next();
    }

    @Test
    public void iteratorExertBackpressure() {
        final Counter src = new Counter();
        Flowable<Integer> obs = Flowable.fromIterable(new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return src;
            }
        });
        Iterator<Integer> it = obs.blockingIterable().iterator();
        while (it.hasNext()) {
            // Correct backpressure should cause this interleaved behavior.
            // We first request RxRingBuffer.SIZE. Then in increments of
            // SubscriberIterator.LIMIT.
            int i = it.next();
            int expected = i - (i % (Flowable.bufferSize() - (Flowable.bufferSize() >> 2))) + Flowable.bufferSize();
            expected = Math.min(expected, Counter.MAX);
            assertEquals(expected, src.count);
        }
    }

    public static final class Counter implements Iterator<Integer> {

        static final int MAX = 5 * Flowable.bufferSize();

        public int count;

        @Override
        public boolean hasNext() {
            return count < MAX;
        }

        @Override
        public Integer next() {
            return ++count;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        BlockingFlowableIterator<Integer> it = new BlockingFlowableIterator<>(128);
        it.remove();
    }

    @Test
    public void dispose() {
        BlockingFlowableIterator<Integer> it = new BlockingFlowableIterator<>(128);
        assertFalse(it.isDisposed());
        it.dispose();
        assertTrue(it.isDisposed());
    }

    @Test
    public void interruptWait() {
        BlockingFlowableIterator<Integer> it = new BlockingFlowableIterator<>(128);
        try {
            Thread.currentThread().interrupt();
            it.hasNext();
        } catch (RuntimeException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof InterruptedException);
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void emptyThrowsNoSuch() {
        BlockingFlowableIterator<Integer> it = new BlockingFlowableIterator<>(128);
        it.onComplete();
        it.next();
    }

    @Test(expected = MissingBackpressureException.class)
    public void overflowQueue() {
        Iterator<Integer> it = new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onNext(1);
                s.onNext(2);
            }
        }.blockingIterable(1).iterator();
        it.next();
    }

    @Test(expected = NoSuchElementException.class)
    public void disposedIteratorHasNextReturns() {
        Iterator<Integer> it = PublishProcessor.<Integer>create().blockingIterable().iterator();
        ((Disposable) it).dispose();
        assertFalse(it.hasNext());
        it.next();
    }

    @Test
    public void asyncDisposeUnblocks() {
        final Iterator<Integer> it = PublishProcessor.<Integer>create().blockingIterable().iterator();
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                ((Disposable) it).dispose();
            }
        }, 1, TimeUnit.SECONDS);
        assertFalse(it.hasNext());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private BlockingFlowableToIteratorTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toIterator() throws java.lang.Throwable {
            this.payloads.toIterator.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toIteratorWithException() throws java.lang.Throwable {
            this.payloads.toIteratorWithException.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorExertBackpressure() throws java.lang.Throwable {
            this.payloads.iteratorExertBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_remove() throws java.lang.Throwable {
            this.payloads.remove.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interruptWait() throws java.lang.Throwable {
            this.payloads.interruptWait.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyThrowsNoSuch() throws java.lang.Throwable {
            this.payloads.emptyThrowsNoSuch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overflowQueue() throws java.lang.Throwable {
            this.payloads.overflowQueue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedIteratorHasNextReturns() throws java.lang.Throwable {
            this.payloads.disposedIteratorHasNextReturns.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncDisposeUnblocks() throws java.lang.Throwable {
            this.payloads.asyncDisposeUnblocks.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableToIteratorTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableToIteratorTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableToIteratorTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableToIteratorTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new BlockingFlowableToIteratorTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingFlowableToIteratorTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(BlockingFlowableToIteratorTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(BlockingFlowableToIteratorTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement toIterator;

            public org.junit.runners.model.Statement toIteratorWithException;

            public org.junit.runners.model.Statement iteratorExertBackpressure;

            public org.junit.runners.model.Statement remove;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement interruptWait;

            public org.junit.runners.model.Statement emptyThrowsNoSuch;

            public org.junit.runners.model.Statement overflowQueue;

            public org.junit.runners.model.Statement disposedIteratorHasNextReturns;

            public org.junit.runners.model.Statement asyncDisposeUnblocks;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.toIterator = _ClassStatement.forPayload(BlockingFlowableToIteratorTest::toIterator, "toIterator", this);
            this.payloads.toIteratorWithException = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableToIteratorTest::toIteratorWithException, io.reactivex.rxjava3.exceptions.TestException.class), "toIteratorWithException", this);
            this.payloads.iteratorExertBackpressure = _ClassStatement.forPayload(BlockingFlowableToIteratorTest::iteratorExertBackpressure, "iteratorExertBackpressure", this);
            this.payloads.remove = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableToIteratorTest::remove, java.lang.UnsupportedOperationException.class), "remove", this);
            this.payloads.dispose = _ClassStatement.forPayload(BlockingFlowableToIteratorTest::dispose, "dispose", this);
            this.payloads.interruptWait = _ClassStatement.forPayload(BlockingFlowableToIteratorTest::interruptWait, "interruptWait", this);
            this.payloads.emptyThrowsNoSuch = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableToIteratorTest::emptyThrowsNoSuch, java.util.NoSuchElementException.class), "emptyThrowsNoSuch", this);
            this.payloads.overflowQueue = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableToIteratorTest::overflowQueue, io.reactivex.rxjava3.exceptions.MissingBackpressureException.class), "overflowQueue", this);
            this.payloads.disposedIteratorHasNextReturns = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingFlowableToIteratorTest::disposedIteratorHasNextReturns, java.util.NoSuchElementException.class), "disposedIteratorHasNextReturns", this);
            this.payloads.asyncDisposeUnblocks = _ClassStatement.forPayload(BlockingFlowableToIteratorTest::asyncDisposeUnblocks, "asyncDisposeUnblocks", this);
        }
    }
}
