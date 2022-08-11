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
package io.reactivex.rxjava3.internal.operators.observable;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.operators.observable.BlockingObservableIterable.BlockingObservableIterator;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class BlockingObservableToIteratorTest extends RxJavaTest {

    @Test
    public void toIterator() {
        Observable<String> obs = Observable.just("one", "two", "three");
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
        Observable<String> obs = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext("one");
                observer.onError(new TestException());
            }
        });
        Iterator<String> it = obs.blockingIterable().iterator();
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        assertTrue(it.hasNext());
        it.next();
    }

    @Test
    public void dispose() {
        BlockingObservableIterator<Integer> it = new BlockingObservableIterator<>(128);
        assertFalse(it.isDisposed());
        it.dispose();
        assertTrue(it.isDisposed());
    }

    @Test
    public void interruptWait() {
        BlockingObservableIterator<Integer> it = new BlockingObservableIterator<>(128);
        try {
            Thread.currentThread().interrupt();
            it.hasNext();
        } catch (RuntimeException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof InterruptedException);
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void emptyThrowsNoSuch() {
        BlockingObservableIterator<Integer> it = new BlockingObservableIterator<>(128);
        it.onComplete();
        it.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        BlockingObservableIterator<Integer> it = new BlockingObservableIterator<>(128);
        it.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void disposedIteratorHasNextReturns() {
        Iterator<Integer> it = PublishSubject.<Integer>create().blockingIterable().iterator();
        ((Disposable) it).dispose();
        assertFalse(it.hasNext());
        it.next();
    }

    @Test
    public void asyncDisposeUnblocks() {
        final Iterator<Integer> it = PublishSubject.<Integer>create().blockingIterable().iterator();
        Schedulers.single().scheduleDirect(new Runnable() {

            @Override
            public void run() {
                ((Disposable) it).dispose();
            }
        }, 1, TimeUnit.SECONDS);
        assertFalse(it.hasNext());
    }

    @Test(expected = TestException.class)
    public void errorAfterDispose() {
        Iterator<Object> it = Observable.error(new TestException()).blockingIterable().iterator();
        ((Disposable) it).dispose();
        it.hasNext();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private BlockingObservableToIteratorTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toIterator() throws java.lang.Throwable {
            this.payloads.toIterator.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toIteratorWithException() throws java.lang.Throwable {
            this.payloads.toIteratorWithException.evaluate();
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
        public void benchmark_remove() throws java.lang.Throwable {
            this.payloads.remove.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedIteratorHasNextReturns() throws java.lang.Throwable {
            this.payloads.disposedIteratorHasNextReturns.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncDisposeUnblocks() throws java.lang.Throwable {
            this.payloads.asyncDisposeUnblocks.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorAfterDispose() throws java.lang.Throwable {
            this.payloads.errorAfterDispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingObservableToIteratorTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingObservableToIteratorTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingObservableToIteratorTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingObservableToIteratorTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new BlockingObservableToIteratorTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingObservableToIteratorTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(BlockingObservableToIteratorTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(BlockingObservableToIteratorTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement toIterator;

            public org.junit.runners.model.Statement toIteratorWithException;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement interruptWait;

            public org.junit.runners.model.Statement emptyThrowsNoSuch;

            public org.junit.runners.model.Statement remove;

            public org.junit.runners.model.Statement disposedIteratorHasNextReturns;

            public org.junit.runners.model.Statement asyncDisposeUnblocks;

            public org.junit.runners.model.Statement errorAfterDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.toIterator = _ClassStatement.forPayload(BlockingObservableToIteratorTest::toIterator, "toIterator", this);
            this.payloads.toIteratorWithException = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingObservableToIteratorTest::toIteratorWithException, io.reactivex.rxjava3.exceptions.TestException.class), "toIteratorWithException", this);
            this.payloads.dispose = _ClassStatement.forPayload(BlockingObservableToIteratorTest::dispose, "dispose", this);
            this.payloads.interruptWait = _ClassStatement.forPayload(BlockingObservableToIteratorTest::interruptWait, "interruptWait", this);
            this.payloads.emptyThrowsNoSuch = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingObservableToIteratorTest::emptyThrowsNoSuch, java.util.NoSuchElementException.class), "emptyThrowsNoSuch", this);
            this.payloads.remove = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingObservableToIteratorTest::remove, java.lang.UnsupportedOperationException.class), "remove", this);
            this.payloads.disposedIteratorHasNextReturns = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingObservableToIteratorTest::disposedIteratorHasNextReturns, java.util.NoSuchElementException.class), "disposedIteratorHasNextReturns", this);
            this.payloads.asyncDisposeUnblocks = _ClassStatement.forPayload(BlockingObservableToIteratorTest::asyncDisposeUnblocks, "asyncDisposeUnblocks", this);
            this.payloads.errorAfterDispose = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingObservableToIteratorTest::errorAfterDispose, io.reactivex.rxjava3.exceptions.TestException.class), "errorAfterDispose", this);
        }
    }
}
