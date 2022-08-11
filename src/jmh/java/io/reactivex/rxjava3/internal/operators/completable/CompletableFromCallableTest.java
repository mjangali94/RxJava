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
package io.reactivex.rxjava3.internal.operators.completable;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.*;

public class CompletableFromCallableTest extends RxJavaTest {

    @Test
    public void fromCallable() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                atomicInteger.incrementAndGet();
                return null;
            }
        }).test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromCallableTwice() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Callable<Object> callable = new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                atomicInteger.incrementAndGet();
                return null;
            }
        };
        Completable.fromCallable(callable).test().assertResult();
        assertEquals(1, atomicInteger.get());
        Completable.fromCallable(callable).test().assertResult();
        assertEquals(2, atomicInteger.get());
    }

    @Test
    public void fromCallableInvokesLazy() {
        final AtomicInteger atomicInteger = new AtomicInteger();
        Completable completable = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                atomicInteger.incrementAndGet();
                return null;
            }
        });
        assertEquals(0, atomicInteger.get());
        completable.test().assertResult();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void fromCallableThrows() {
        Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw new UnsupportedOperationException();
            }
        }).test().assertFailure(UnsupportedOperationException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission() throws Exception {
        Callable<String> func = mock(Callable.class);
        final CountDownLatch funcLatch = new CountDownLatch(1);
        final CountDownLatch observerLatch = new CountDownLatch(1);
        when(func.call()).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                observerLatch.countDown();
                try {
                    funcLatch.await();
                } catch (InterruptedException e) {
                    // It's okay, unsubscription causes Thread interruption
                    // Restoring interruption status of the Thread
                    Thread.currentThread().interrupt();
                }
                return "should_not_be_delivered";
            }
        });
        Completable fromCallableObservable = Completable.fromCallable(func);
        Observer<Object> observer = TestHelper.mockObserver();
        TestObserver<String> outer = new TestObserver<>(observer);
        fromCallableObservable.subscribeOn(Schedulers.computation()).subscribe(outer);
        // Wait until func will be invoked
        observerLatch.await();
        // Unsubscribing before emission
        outer.dispose();
        // Emitting result
        funcLatch.countDown();
        // func must be invoked
        verify(func).call();
        // Observer must not be notified at all
        verify(observer).onSubscribe(any(Disposable.class));
        verifyNoMoreInteractions(observer);
    }

    @Test
    @SuppressUndeliverable
    public void fromActionErrorsDisposed() {
        final AtomicInteger calls = new AtomicInteger();
        Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                calls.incrementAndGet();
                throw new TestException();
            }
        }).test(true).assertEmpty();
        assertEquals(1, calls.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private CompletableFromCallableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallable() throws java.lang.Throwable {
            this.payloads.fromCallable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallableTwice() throws java.lang.Throwable {
            this.payloads.fromCallableTwice.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallableInvokesLazy() throws java.lang.Throwable {
            this.payloads.fromCallableInvokesLazy.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallableThrows() throws java.lang.Throwable {
            this.payloads.fromCallableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission() throws java.lang.Throwable {
            this.payloads.shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionErrorsDisposed() throws java.lang.Throwable {
            this.payloads.fromActionErrorsDisposed.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromCallableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromCallableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromCallableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromCallableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableFromCallableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableFromCallableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableFromCallableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableFromCallableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement fromCallable;

            public org.junit.runners.model.Statement fromCallableTwice;

            public org.junit.runners.model.Statement fromCallableInvokesLazy;

            public org.junit.runners.model.Statement fromCallableThrows;

            public org.junit.runners.model.Statement shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission;

            public org.junit.runners.model.Statement fromActionErrorsDisposed;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.fromCallable = _ClassStatement.forPayload(CompletableFromCallableTest::fromCallable, "fromCallable", this);
            this.payloads.fromCallableTwice = _ClassStatement.forPayload(CompletableFromCallableTest::fromCallableTwice, "fromCallableTwice", this);
            this.payloads.fromCallableInvokesLazy = _ClassStatement.forPayload(CompletableFromCallableTest::fromCallableInvokesLazy, "fromCallableInvokesLazy", this);
            this.payloads.fromCallableThrows = _ClassStatement.forPayload(CompletableFromCallableTest::fromCallableThrows, "fromCallableThrows", this);
            this.payloads.shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission = _ClassStatement.forPayload(CompletableFromCallableTest::shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission, "shouldNotDeliverResultIfSubscriberUnsubscribedBeforeEmission", this);
            this.payloads.fromActionErrorsDisposed = _ClassStatement.forPayload(CompletableFromCallableTest::fromActionErrorsDisposed, "fromActionErrorsDisposed", this);
        }
    }
}
