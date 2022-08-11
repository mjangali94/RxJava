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
package io.reactivex.rxjava3.exceptions;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ExceptionsTest extends RxJavaTest {

    @Test
    public void constructorShouldBePrivate() {
        TestHelper.checkUtilityClass(ExceptionHelper.class);
    }

    @Test
    public void onErrorNotImplementedIsThrown() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        Observable.just(1, 2, 3).subscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                throw new RuntimeException("hello");
            }
        });
        TestHelper.assertError(errors, 0, RuntimeException.class);
        assertTrue(errors.get(0).toString(), errors.get(0).getMessage().contains("hello"));
        RxJavaPlugins.reset();
    }

    @Test
    public void stackOverflowWouldOccur() {
        final PublishSubject<Integer> a = PublishSubject.create();
        final PublishSubject<Integer> b = PublishSubject.create();
        final int MAX_STACK_DEPTH = 800;
        final AtomicInteger depth = new AtomicInteger();
        a.subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Integer n) {
                b.onNext(n + 1);
            }
        });
        b.subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
            // TODO Auto-generated method stub
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Integer n) {
                if (depth.get() < MAX_STACK_DEPTH) {
                    depth.set(Thread.currentThread().getStackTrace().length);
                    a.onNext(n + 1);
                }
            }
        });
        a.onNext(1);
        assertTrue(depth.get() >= MAX_STACK_DEPTH);
    }

    @Test(expected = StackOverflowError.class)
    public void stackOverflowErrorIsThrown() {
        Observable.just(1).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Integer t) {
                throw new StackOverflowError();
            }
        });
    }

    @Test(expected = ThreadDeath.class)
    public void threadDeathIsThrown() {
        Observable.just(1).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Integer t) {
                throw new ThreadDeath();
            }
        });
    }

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(Exceptions.class);
    }

    @Test
    public void manualThrowIfFatal() {
        try {
            Exceptions.throwIfFatal(new ThreadDeath());
            fail("Didn't throw fatal exception");
        } catch (ThreadDeath ex) {
        // expected
        }
        try {
            Exceptions.throwIfFatal(new LinkageError());
            fail("Didn't throw fatal error");
        } catch (LinkageError ex) {
        // expected
        }
        try {
            ExceptionHelper.wrapOrThrow(new LinkageError());
            fail("Didn't propagate Error");
        } catch (LinkageError ex) {
        // expected
        }
    }

    @Test
    public void manualPropagate() {
        try {
            Exceptions.propagate(new InternalError());
            fail("Didn't throw exception");
        } catch (InternalError ex) {
        // expected
        }
        try {
            throw Exceptions.propagate(new IllegalArgumentException());
        } catch (IllegalArgumentException ex) {
        // expected
        }
        try {
            throw ExceptionHelper.wrapOrThrow(new IOException());
        } catch (RuntimeException ex) {
            if (!(ex.getCause() instanceof IOException)) {
                fail(ex.toString() + ": should have thrown RuntimeException(IOException)");
            }
        }
    }

    @Test
    public void errorNotImplementedNull1() {
        OnErrorNotImplementedException ex = new OnErrorNotImplementedException(null);
        assertTrue("" + ex.getCause(), ex.getCause() instanceof NullPointerException);
    }

    @Test
    public void errorNotImplementedNull2() {
        OnErrorNotImplementedException ex = new OnErrorNotImplementedException("Message", null);
        assertTrue("" + ex.getCause(), ex.getCause() instanceof NullPointerException);
    }

    @Test
    public void errorNotImplementedWithCause() {
        OnErrorNotImplementedException ex = new OnErrorNotImplementedException("Message", new TestException("Forced failure"));
        assertTrue("" + ex.getCause(), ex.getCause() instanceof TestException);
        assertEquals("" + ex.getCause(), "Forced failure", ex.getCause().getMessage());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ExceptionsTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_constructorShouldBePrivate() throws java.lang.Throwable {
            this.payloads.constructorShouldBePrivate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorNotImplementedIsThrown() throws java.lang.Throwable {
            this.payloads.onErrorNotImplementedIsThrown.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stackOverflowWouldOccur() throws java.lang.Throwable {
            this.payloads.stackOverflowWouldOccur.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stackOverflowErrorIsThrown() throws java.lang.Throwable {
            this.payloads.stackOverflowErrorIsThrown.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_threadDeathIsThrown() throws java.lang.Throwable {
            this.payloads.threadDeathIsThrown.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_utilityClass() throws java.lang.Throwable {
            this.payloads.utilityClass.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manualThrowIfFatal() throws java.lang.Throwable {
            this.payloads.manualThrowIfFatal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manualPropagate() throws java.lang.Throwable {
            this.payloads.manualPropagate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNotImplementedNull1() throws java.lang.Throwable {
            this.payloads.errorNotImplementedNull1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNotImplementedNull2() throws java.lang.Throwable {
            this.payloads.errorNotImplementedNull2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNotImplementedWithCause() throws java.lang.Throwable {
            this.payloads.errorNotImplementedWithCause.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ExceptionsTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ExceptionsTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ExceptionsTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ExceptionsTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ExceptionsTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ExceptionsTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ExceptionsTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ExceptionsTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement constructorShouldBePrivate;

            public org.junit.runners.model.Statement onErrorNotImplementedIsThrown;

            public org.junit.runners.model.Statement stackOverflowWouldOccur;

            public org.junit.runners.model.Statement stackOverflowErrorIsThrown;

            public org.junit.runners.model.Statement threadDeathIsThrown;

            public org.junit.runners.model.Statement utilityClass;

            public org.junit.runners.model.Statement manualThrowIfFatal;

            public org.junit.runners.model.Statement manualPropagate;

            public org.junit.runners.model.Statement errorNotImplementedNull1;

            public org.junit.runners.model.Statement errorNotImplementedNull2;

            public org.junit.runners.model.Statement errorNotImplementedWithCause;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.constructorShouldBePrivate = _ClassStatement.forPayload(ExceptionsTest::constructorShouldBePrivate, "constructorShouldBePrivate", this);
            this.payloads.onErrorNotImplementedIsThrown = _ClassStatement.forPayload(ExceptionsTest::onErrorNotImplementedIsThrown, "onErrorNotImplementedIsThrown", this);
            this.payloads.stackOverflowWouldOccur = _ClassStatement.forPayload(ExceptionsTest::stackOverflowWouldOccur, "stackOverflowWouldOccur", this);
            this.payloads.stackOverflowErrorIsThrown = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ExceptionsTest::stackOverflowErrorIsThrown, java.lang.StackOverflowError.class), "stackOverflowErrorIsThrown", this);
            this.payloads.threadDeathIsThrown = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ExceptionsTest::threadDeathIsThrown, java.lang.ThreadDeath.class), "threadDeathIsThrown", this);
            this.payloads.utilityClass = _ClassStatement.forPayload(ExceptionsTest::utilityClass, "utilityClass", this);
            this.payloads.manualThrowIfFatal = _ClassStatement.forPayload(ExceptionsTest::manualThrowIfFatal, "manualThrowIfFatal", this);
            this.payloads.manualPropagate = _ClassStatement.forPayload(ExceptionsTest::manualPropagate, "manualPropagate", this);
            this.payloads.errorNotImplementedNull1 = _ClassStatement.forPayload(ExceptionsTest::errorNotImplementedNull1, "errorNotImplementedNull1", this);
            this.payloads.errorNotImplementedNull2 = _ClassStatement.forPayload(ExceptionsTest::errorNotImplementedNull2, "errorNotImplementedNull2", this);
            this.payloads.errorNotImplementedWithCause = _ClassStatement.forPayload(ExceptionsTest::errorNotImplementedWithCause, "errorNotImplementedWithCause", this);
        }
    }
}
