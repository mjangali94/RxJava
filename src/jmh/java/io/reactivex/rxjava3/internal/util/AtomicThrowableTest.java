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
package io.reactivex.rxjava3.internal.util;

import static org.junit.Assert.*;
import java.util.List;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class AtomicThrowableTest extends RxJavaTest {

    @Test
    public void isTerminated() {
        AtomicThrowable ex = new AtomicThrowable();
        assertFalse(ex.isTerminated());
        assertNull(ex.terminate());
        assertTrue(ex.isTerminated());
    }

    @Test
    public void tryTerminateAndReportNull() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            AtomicThrowable ex = new AtomicThrowable();
            ex.tryTerminateAndReport();
            assertTrue("" + errors, errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void tryTerminateAndReportAlreadyTerminated() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            AtomicThrowable ex = new AtomicThrowable();
            ex.terminate();
            ex.tryTerminateAndReport();
            assertTrue("" + errors, errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void tryTerminateAndReportHasError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            AtomicThrowable ex = new AtomicThrowable();
            ex.set(new TestException());
            ex.tryTerminateAndReport();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            assertEquals(1, errors.size());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void tryTerminateConsumerSubscriberNoError() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        ts.onSubscribe(new BooleanSubscription());
        AtomicThrowable ex = new AtomicThrowable();
        ex.tryTerminateConsumer(ts);
        ts.assertResult();
    }

    @Test
    public void tryTerminateConsumerSubscriberError() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        ts.onSubscribe(new BooleanSubscription());
        AtomicThrowable ex = new AtomicThrowable();
        ex.set(new TestException());
        ex.tryTerminateConsumer(ts);
        ts.assertFailure(TestException.class);
    }

    @Test
    public void tryTerminateConsumerSubscriberTerminated() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        ts.onSubscribe(new BooleanSubscription());
        AtomicThrowable ex = new AtomicThrowable();
        ex.terminate();
        ex.tryTerminateConsumer(ts);
        ts.assertEmpty();
    }

    @Test
    public void tryTerminateConsumerObserverNoError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.tryTerminateConsumer((Observer<Object>) to);
        to.assertResult();
    }

    @Test
    public void tryTerminateConsumerObserverError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.set(new TestException());
        ex.tryTerminateConsumer((Observer<Object>) to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void tryTerminateConsumerObserverTerminated() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.terminate();
        ex.tryTerminateConsumer((Observer<Object>) to);
        to.assertEmpty();
    }

    @Test
    public void tryTerminateConsumerMaybeObserverNoError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.tryTerminateConsumer((MaybeObserver<Object>) to);
        to.assertResult();
    }

    @Test
    public void tryTerminateConsumerMaybeObserverError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.set(new TestException());
        ex.tryTerminateConsumer((MaybeObserver<Object>) to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void tryTerminateConsumerMaybeObserverTerminated() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.terminate();
        ex.tryTerminateConsumer((MaybeObserver<Object>) to);
        to.assertEmpty();
    }

    @Test
    public void tryTerminateConsumerSingleNoError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.tryTerminateConsumer((SingleObserver<Object>) to);
        to.assertEmpty();
    }

    @Test
    public void tryTerminateConsumerSingleError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.set(new TestException());
        ex.tryTerminateConsumer((SingleObserver<Object>) to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void tryTerminateConsumerSingleTerminated() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.terminate();
        ex.tryTerminateConsumer((SingleObserver<Object>) to);
        to.assertEmpty();
    }

    @Test
    public void tryTerminateConsumerCompletableObserverNoError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.tryTerminateConsumer((CompletableObserver) to);
        to.assertResult();
    }

    @Test
    public void tryTerminateConsumerCompletableObserverError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.set(new TestException());
        ex.tryTerminateConsumer((CompletableObserver) to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void tryTerminateConsumerCompletableObserverTerminated() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.terminate();
        ex.tryTerminateConsumer((CompletableObserver) to);
        to.assertEmpty();
    }

    static <T> Emitter<T> wrapToEmitter(final Observer<T> observer) {
        return new Emitter<T>() {

            @Override
            public void onNext(T value) {
                observer.onNext(value);
            }

            @Override
            public void onError(Throwable error) {
                observer.onError(error);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }
        };
    }

    @Test
    public void tryTerminateConsumerEmitterNoError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.tryTerminateConsumer(wrapToEmitter(to));
        to.assertResult();
    }

    @Test
    public void tryTerminateConsumerEmitterError() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.set(new TestException());
        ex.tryTerminateConsumer(wrapToEmitter(to));
        to.assertFailure(TestException.class);
    }

    @Test
    public void tryTerminateConsumerEmitterTerminated() {
        TestObserver<Object> to = new TestObserver<>();
        to.onSubscribe(Disposable.empty());
        AtomicThrowable ex = new AtomicThrowable();
        ex.terminate();
        ex.tryTerminateConsumer(wrapToEmitter(to));
        to.assertEmpty();
    }

    @Test
    public void tryAddThrowableOrReportNull() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            AtomicThrowable ex = new AtomicThrowable();
            ex.tryAddThrowableOrReport(new TestException());
            assertTrue("" + errors, errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void tryAddThrowableOrReportTerminated() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            AtomicThrowable ex = new AtomicThrowable();
            ex.terminate();
            assertFalse(ex.tryAddThrowableOrReport(new TestException()));
            assertFalse("" + errors, errors.isEmpty());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private AtomicThrowableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isTerminated() throws java.lang.Throwable {
            this.payloads.isTerminated.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateAndReportNull() throws java.lang.Throwable {
            this.payloads.tryTerminateAndReportNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateAndReportAlreadyTerminated() throws java.lang.Throwable {
            this.payloads.tryTerminateAndReportAlreadyTerminated.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateAndReportHasError() throws java.lang.Throwable {
            this.payloads.tryTerminateAndReportHasError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerSubscriberNoError() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerSubscriberNoError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerSubscriberError() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerSubscriberError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerSubscriberTerminated() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerSubscriberTerminated.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerObserverNoError() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerObserverNoError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerObserverError() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerObserverError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerObserverTerminated() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerObserverTerminated.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerMaybeObserverNoError() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerMaybeObserverNoError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerMaybeObserverError() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerMaybeObserverError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerMaybeObserverTerminated() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerMaybeObserverTerminated.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerSingleNoError() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerSingleNoError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerSingleError() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerSingleError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerSingleTerminated() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerSingleTerminated.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerCompletableObserverNoError() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerCompletableObserverNoError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerCompletableObserverError() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerCompletableObserverError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerCompletableObserverTerminated() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerCompletableObserverTerminated.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerEmitterNoError() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerEmitterNoError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerEmitterError() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerEmitterError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryTerminateConsumerEmitterTerminated() throws java.lang.Throwable {
            this.payloads.tryTerminateConsumerEmitterTerminated.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryAddThrowableOrReportNull() throws java.lang.Throwable {
            this.payloads.tryAddThrowableOrReportNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryAddThrowableOrReportTerminated() throws java.lang.Throwable {
            this.payloads.tryAddThrowableOrReportTerminated.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<AtomicThrowableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<AtomicThrowableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<AtomicThrowableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<AtomicThrowableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new AtomicThrowableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<AtomicThrowableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(AtomicThrowableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(AtomicThrowableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement isTerminated;

            public org.junit.runners.model.Statement tryTerminateAndReportNull;

            public org.junit.runners.model.Statement tryTerminateAndReportAlreadyTerminated;

            public org.junit.runners.model.Statement tryTerminateAndReportHasError;

            public org.junit.runners.model.Statement tryTerminateConsumerSubscriberNoError;

            public org.junit.runners.model.Statement tryTerminateConsumerSubscriberError;

            public org.junit.runners.model.Statement tryTerminateConsumerSubscriberTerminated;

            public org.junit.runners.model.Statement tryTerminateConsumerObserverNoError;

            public org.junit.runners.model.Statement tryTerminateConsumerObserverError;

            public org.junit.runners.model.Statement tryTerminateConsumerObserverTerminated;

            public org.junit.runners.model.Statement tryTerminateConsumerMaybeObserverNoError;

            public org.junit.runners.model.Statement tryTerminateConsumerMaybeObserverError;

            public org.junit.runners.model.Statement tryTerminateConsumerMaybeObserverTerminated;

            public org.junit.runners.model.Statement tryTerminateConsumerSingleNoError;

            public org.junit.runners.model.Statement tryTerminateConsumerSingleError;

            public org.junit.runners.model.Statement tryTerminateConsumerSingleTerminated;

            public org.junit.runners.model.Statement tryTerminateConsumerCompletableObserverNoError;

            public org.junit.runners.model.Statement tryTerminateConsumerCompletableObserverError;

            public org.junit.runners.model.Statement tryTerminateConsumerCompletableObserverTerminated;

            public org.junit.runners.model.Statement tryTerminateConsumerEmitterNoError;

            public org.junit.runners.model.Statement tryTerminateConsumerEmitterError;

            public org.junit.runners.model.Statement tryTerminateConsumerEmitterTerminated;

            public org.junit.runners.model.Statement tryAddThrowableOrReportNull;

            public org.junit.runners.model.Statement tryAddThrowableOrReportTerminated;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.isTerminated = _ClassStatement.forPayload(AtomicThrowableTest::isTerminated, "isTerminated", this);
            this.payloads.tryTerminateAndReportNull = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateAndReportNull, "tryTerminateAndReportNull", this);
            this.payloads.tryTerminateAndReportAlreadyTerminated = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateAndReportAlreadyTerminated, "tryTerminateAndReportAlreadyTerminated", this);
            this.payloads.tryTerminateAndReportHasError = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateAndReportHasError, "tryTerminateAndReportHasError", this);
            this.payloads.tryTerminateConsumerSubscriberNoError = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerSubscriberNoError, "tryTerminateConsumerSubscriberNoError", this);
            this.payloads.tryTerminateConsumerSubscriberError = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerSubscriberError, "tryTerminateConsumerSubscriberError", this);
            this.payloads.tryTerminateConsumerSubscriberTerminated = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerSubscriberTerminated, "tryTerminateConsumerSubscriberTerminated", this);
            this.payloads.tryTerminateConsumerObserverNoError = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerObserverNoError, "tryTerminateConsumerObserverNoError", this);
            this.payloads.tryTerminateConsumerObserverError = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerObserverError, "tryTerminateConsumerObserverError", this);
            this.payloads.tryTerminateConsumerObserverTerminated = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerObserverTerminated, "tryTerminateConsumerObserverTerminated", this);
            this.payloads.tryTerminateConsumerMaybeObserverNoError = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerMaybeObserverNoError, "tryTerminateConsumerMaybeObserverNoError", this);
            this.payloads.tryTerminateConsumerMaybeObserverError = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerMaybeObserverError, "tryTerminateConsumerMaybeObserverError", this);
            this.payloads.tryTerminateConsumerMaybeObserverTerminated = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerMaybeObserverTerminated, "tryTerminateConsumerMaybeObserverTerminated", this);
            this.payloads.tryTerminateConsumerSingleNoError = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerSingleNoError, "tryTerminateConsumerSingleNoError", this);
            this.payloads.tryTerminateConsumerSingleError = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerSingleError, "tryTerminateConsumerSingleError", this);
            this.payloads.tryTerminateConsumerSingleTerminated = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerSingleTerminated, "tryTerminateConsumerSingleTerminated", this);
            this.payloads.tryTerminateConsumerCompletableObserverNoError = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerCompletableObserverNoError, "tryTerminateConsumerCompletableObserverNoError", this);
            this.payloads.tryTerminateConsumerCompletableObserverError = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerCompletableObserverError, "tryTerminateConsumerCompletableObserverError", this);
            this.payloads.tryTerminateConsumerCompletableObserverTerminated = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerCompletableObserverTerminated, "tryTerminateConsumerCompletableObserverTerminated", this);
            this.payloads.tryTerminateConsumerEmitterNoError = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerEmitterNoError, "tryTerminateConsumerEmitterNoError", this);
            this.payloads.tryTerminateConsumerEmitterError = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerEmitterError, "tryTerminateConsumerEmitterError", this);
            this.payloads.tryTerminateConsumerEmitterTerminated = _ClassStatement.forPayload(AtomicThrowableTest::tryTerminateConsumerEmitterTerminated, "tryTerminateConsumerEmitterTerminated", this);
            this.payloads.tryAddThrowableOrReportNull = _ClassStatement.forPayload(AtomicThrowableTest::tryAddThrowableOrReportNull, "tryAddThrowableOrReportNull", this);
            this.payloads.tryAddThrowableOrReportTerminated = _ClassStatement.forPayload(AtomicThrowableTest::tryAddThrowableOrReportTerminated, "tryAddThrowableOrReportTerminated", this);
        }
    }
}
