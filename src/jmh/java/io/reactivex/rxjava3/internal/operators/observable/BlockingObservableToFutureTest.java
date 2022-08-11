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
import java.util.concurrent.*;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.exceptions.TestException;

public class BlockingObservableToFutureTest extends RxJavaTest {

    @Test
    public void toFuture() throws InterruptedException, ExecutionException {
        Observable<String> obs = Observable.just("one");
        Future<String> f = obs.toFuture();
        assertEquals("one", f.get());
    }

    @Test
    public void toFutureList() throws InterruptedException, ExecutionException {
        Observable<String> obs = Observable.just("one", "two", "three");
        Future<List<String>> f = obs.toList().toFuture();
        assertEquals("one", f.get().get(0));
        assertEquals("two", f.get().get(1));
        assertEquals("three", f.get().get(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void exceptionWithMoreThanOneElement() throws Throwable {
        Observable<String> obs = Observable.just("one", "two");
        Future<String> f = obs.toFuture();
        try {
            // we expect an exception since there are more than 1 element
            f.get();
            fail("Should have thrown!");
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void toFutureWithException() {
        Observable<String> obs = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext("one");
                observer.onError(new TestException());
            }
        });
        Future<String> f = obs.toFuture();
        try {
            f.get();
            fail("expected exception");
        } catch (Throwable e) {
            assertEquals(TestException.class, e.getCause().getClass());
        }
    }

    @Test(expected = CancellationException.class)
    public void getAfterCancel() throws Exception {
        Observable<String> obs = Observable.never();
        Future<String> f = obs.toFuture();
        boolean cancelled = f.cancel(true);
        // because OperationNeverComplete never does
        assertTrue(cancelled);
        // Future.get() docs require this to throw
        f.get();
    }

    @Test(expected = CancellationException.class)
    public void getWithTimeoutAfterCancel() throws Exception {
        Observable<String> obs = Observable.never();
        Future<String> f = obs.toFuture();
        boolean cancelled = f.cancel(true);
        // because OperationNeverComplete never does
        assertTrue(cancelled);
        // Future.get() docs require this to throw
        f.get(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    @Test(expected = NoSuchElementException.class)
    public void getWithEmptyFlowable() throws Throwable {
        Observable<String> obs = Observable.empty();
        Future<String> f = obs.toFuture();
        try {
            f.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private BlockingObservableToFutureTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFuture() throws java.lang.Throwable {
            this.payloads.toFuture.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFutureList() throws java.lang.Throwable {
            this.payloads.toFutureList.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exceptionWithMoreThanOneElement() throws java.lang.Throwable {
            this.payloads.exceptionWithMoreThanOneElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFutureWithException() throws java.lang.Throwable {
            this.payloads.toFutureWithException.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getAfterCancel() throws java.lang.Throwable {
            this.payloads.getAfterCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getWithTimeoutAfterCancel() throws java.lang.Throwable {
            this.payloads.getWithTimeoutAfterCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getWithEmptyFlowable() throws java.lang.Throwable {
            this.payloads.getWithEmptyFlowable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingObservableToFutureTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingObservableToFutureTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingObservableToFutureTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingObservableToFutureTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new BlockingObservableToFutureTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<BlockingObservableToFutureTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(BlockingObservableToFutureTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(BlockingObservableToFutureTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement toFuture;

            public org.junit.runners.model.Statement toFutureList;

            public org.junit.runners.model.Statement exceptionWithMoreThanOneElement;

            public org.junit.runners.model.Statement toFutureWithException;

            public org.junit.runners.model.Statement getAfterCancel;

            public org.junit.runners.model.Statement getWithTimeoutAfterCancel;

            public org.junit.runners.model.Statement getWithEmptyFlowable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.toFuture = _ClassStatement.forPayload(BlockingObservableToFutureTest::toFuture, "toFuture", this);
            this.payloads.toFutureList = _ClassStatement.forPayload(BlockingObservableToFutureTest::toFutureList, "toFutureList", this);
            this.payloads.exceptionWithMoreThanOneElement = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingObservableToFutureTest::exceptionWithMoreThanOneElement, java.lang.IndexOutOfBoundsException.class), "exceptionWithMoreThanOneElement", this);
            this.payloads.toFutureWithException = _ClassStatement.forPayload(BlockingObservableToFutureTest::toFutureWithException, "toFutureWithException", this);
            this.payloads.getAfterCancel = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingObservableToFutureTest::getAfterCancel, java.util.concurrent.CancellationException.class), "getAfterCancel", this);
            this.payloads.getWithTimeoutAfterCancel = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingObservableToFutureTest::getWithTimeoutAfterCancel, java.util.concurrent.CancellationException.class), "getWithTimeoutAfterCancel", this);
            this.payloads.getWithEmptyFlowable = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(BlockingObservableToFutureTest::getWithEmptyFlowable, java.util.NoSuchElementException.class), "getWithEmptyFlowable", this);
        }
    }
}
