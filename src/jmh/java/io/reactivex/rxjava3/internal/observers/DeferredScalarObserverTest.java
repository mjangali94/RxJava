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
package io.reactivex.rxjava3.internal.observers;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueDisposable;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class DeferredScalarObserverTest extends RxJavaTest {

    static final class TakeFirst extends DeferredScalarObserver<Integer, Integer> {

        private static final long serialVersionUID = -2793723002312330530L;

        TakeFirst(Observer<? super Integer> downstream) {
            super(downstream);
        }

        @Override
        public void onNext(Integer value) {
            upstream.dispose();
            complete(value);
            complete(value);
        }
    }

    @Test
    public void normal() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = new TestObserver<>();
            TakeFirst source = new TakeFirst(to);
            source.onSubscribe(Disposable.empty());
            Disposable d = Disposable.empty();
            source.onSubscribe(d);
            assertTrue(d.isDisposed());
            source.onNext(1);
            to.assertResult(1);
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void error() {
        TestObserver<Integer> to = new TestObserver<>();
        TakeFirst source = new TakeFirst(to);
        source.onSubscribe(Disposable.empty());
        source.onError(new TestException());
        to.assertFailure(TestException.class);
    }

    @Test
    public void complete() {
        TestObserver<Integer> to = new TestObserver<>();
        TakeFirst source = new TakeFirst(to);
        source.onSubscribe(Disposable.empty());
        source.onComplete();
        to.assertResult();
    }

    @Test
    public void dispose() {
        TestObserver<Integer> to = new TestObserver<>();
        TakeFirst source = new TakeFirst(to);
        Disposable d = Disposable.empty();
        source.onSubscribe(d);
        assertFalse(d.isDisposed());
        to.dispose();
        assertTrue(d.isDisposed());
        assertTrue(source.isDisposed());
    }

    @Test
    public void fused() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
            TakeFirst source = new TakeFirst(to);
            Disposable d = Disposable.empty();
            source.onSubscribe(d);
            to.assertFuseable();
            to.assertFusionMode(QueueFuseable.ASYNC);
            source.onNext(1);
            source.onNext(1);
            source.onError(new TestException());
            source.onComplete();
            assertTrue(d.isDisposed());
            to.assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void fusedReject() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.SYNC);
            TakeFirst source = new TakeFirst(to);
            Disposable d = Disposable.empty();
            source.onSubscribe(d);
            to.assertFuseable();
            to.assertFusionMode(QueueFuseable.NONE);
            source.onNext(1);
            source.onNext(1);
            source.onError(new TestException());
            source.onComplete();
            assertTrue(d.isDisposed());
            to.assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    static final class TakeLast extends DeferredScalarObserver<Integer, Integer> {

        private static final long serialVersionUID = -2793723002312330530L;

        TakeLast(Observer<? super Integer> downstream) {
            super(downstream);
        }

        @Override
        public void onNext(Integer value) {
            this.value = value;
        }
    }

    @Test
    public void nonfusedTerminateMore() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.NONE);
            TakeLast source = new TakeLast(to);
            Disposable d = Disposable.empty();
            source.onSubscribe(d);
            source.onNext(1);
            source.onComplete();
            source.onComplete();
            source.onError(new TestException());
            to.assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void nonfusedError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.NONE);
            TakeLast source = new TakeLast(to);
            Disposable d = Disposable.empty();
            source.onSubscribe(d);
            source.onNext(1);
            source.onError(new TestException());
            source.onError(new TestException("second"));
            source.onComplete();
            to.assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void fusedTerminateMore() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
            TakeLast source = new TakeLast(to);
            Disposable d = Disposable.empty();
            source.onSubscribe(d);
            source.onNext(1);
            source.onComplete();
            source.onComplete();
            source.onError(new TestException());
            to.assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void fusedError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
            TakeLast source = new TakeLast(to);
            Disposable d = Disposable.empty();
            source.onSubscribe(d);
            source.onNext(1);
            source.onError(new TestException());
            source.onError(new TestException("second"));
            source.onComplete();
            to.assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void disposed() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.NONE);
        TakeLast source = new TakeLast(to);
        Disposable d = Disposable.empty();
        source.onSubscribe(d);
        to.dispose();
        source.onNext(1);
        source.onComplete();
        to.assertNoValues().assertNoErrors().assertNotComplete();
    }

    @Test
    public void disposedAfterOnNext() {
        final TestObserver<Integer> to = new TestObserver<>();
        TakeLast source = new TakeLast(new Observer<Integer>() {

            Disposable upstream;

            @Override
            public void onSubscribe(Disposable d) {
                this.upstream = d;
                to.onSubscribe(d);
            }

            @Override
            public void onNext(Integer value) {
                to.onNext(value);
                upstream.dispose();
            }

            @Override
            public void onError(Throwable e) {
                to.onError(e);
            }

            @Override
            public void onComplete() {
                to.onComplete();
            }
        });
        source.onSubscribe(Disposable.empty());
        source.onNext(1);
        source.onComplete();
        to.assertValue(1).assertNoErrors().assertNotComplete();
    }

    @Test
    public void fusedEmpty() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ANY);
        TakeLast source = new TakeLast(to);
        Disposable d = Disposable.empty();
        source.onSubscribe(d);
        source.onComplete();
        to.assertResult();
    }

    @Test
    public void nonfusedEmpty() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.NONE);
        TakeLast source = new TakeLast(to);
        Disposable d = Disposable.empty();
        source.onSubscribe(d);
        source.onComplete();
        to.assertResult();
    }

    @Test
    public void customFusion() {
        final TestObserver<Integer> to = new TestObserver<>();
        TakeLast source = new TakeLast(new Observer<Integer>() {

            QueueDisposable<Integer> d;

            @SuppressWarnings("unchecked")
            @Override
            public void onSubscribe(Disposable d) {
                this.d = (QueueDisposable<Integer>) d;
                to.onSubscribe(d);
                this.d.requestFusion(QueueFuseable.ANY);
            }

            @Override
            public void onNext(Integer value) {
                if (!d.isEmpty()) {
                    Integer v = null;
                    try {
                        to.onNext(d.poll());
                        v = d.poll();
                    } catch (Throwable ex) {
                        to.onError(ex);
                    }
                    assertNull(v);
                    assertTrue(d.isEmpty());
                }
            }

            @Override
            public void onError(Throwable e) {
                to.onError(e);
            }

            @Override
            public void onComplete() {
                to.onComplete();
            }
        });
        source.onSubscribe(Disposable.empty());
        source.onNext(1);
        source.onComplete();
        to.assertResult(1);
    }

    @Test
    public void customFusionClear() {
        final TestObserver<Integer> to = new TestObserver<>();
        TakeLast source = new TakeLast(new Observer<Integer>() {

            QueueDisposable<Integer> d;

            @SuppressWarnings("unchecked")
            @Override
            public void onSubscribe(Disposable d) {
                this.d = (QueueDisposable<Integer>) d;
                to.onSubscribe(d);
                this.d.requestFusion(QueueFuseable.ANY);
            }

            @Override
            public void onNext(Integer value) {
                d.clear();
                assertTrue(d.isEmpty());
            }

            @Override
            public void onError(Throwable e) {
                to.onError(e);
            }

            @Override
            public void onComplete() {
                to.onComplete();
            }
        });
        source.onSubscribe(Disposable.empty());
        source.onNext(1);
        source.onComplete();
        to.assertNoValues().assertNoErrors().assertComplete();
    }

    @Test
    public void offerThrow() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.NONE);
        TakeLast source = new TakeLast(to);
        TestHelper.assertNoOffer(source);
    }

    @Test
    public void customFusionDontConsume() {
        final TestObserver<Integer> to = new TestObserver<>();
        TakeFirst source = new TakeFirst(new Observer<Integer>() {

            QueueDisposable<Integer> d;

            @SuppressWarnings("unchecked")
            @Override
            public void onSubscribe(Disposable d) {
                this.d = (QueueDisposable<Integer>) d;
                to.onSubscribe(d);
                this.d.requestFusion(QueueFuseable.ANY);
            }

            @Override
            public void onNext(Integer value) {
            // not consuming
            }

            @Override
            public void onError(Throwable e) {
                to.onError(e);
            }

            @Override
            public void onComplete() {
                to.onComplete();
            }
        });
        source.onSubscribe(Disposable.empty());
        source.onNext(1);
        to.assertNoValues().assertNoErrors().assertComplete();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public DeferredScalarObserverTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_complete() throws java.lang.Throwable {
            this.payloads.complete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.payloads.fused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedReject() throws java.lang.Throwable {
            this.payloads.fusedReject.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonfusedTerminateMore() throws java.lang.Throwable {
            this.payloads.nonfusedTerminateMore.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonfusedError() throws java.lang.Throwable {
            this.payloads.nonfusedError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedTerminateMore() throws java.lang.Throwable {
            this.payloads.fusedTerminateMore.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedError() throws java.lang.Throwable {
            this.payloads.fusedError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedAfterOnNext() throws java.lang.Throwable {
            this.payloads.disposedAfterOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedEmpty() throws java.lang.Throwable {
            this.payloads.fusedEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonfusedEmpty() throws java.lang.Throwable {
            this.payloads.nonfusedEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customFusion() throws java.lang.Throwable {
            this.payloads.customFusion.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customFusionClear() throws java.lang.Throwable {
            this.payloads.customFusionClear.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_offerThrow() throws java.lang.Throwable {
            this.payloads.offerThrow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customFusionDontConsume() throws java.lang.Throwable {
            this.payloads.customFusionDontConsume.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<DeferredScalarObserverTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<DeferredScalarObserverTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<DeferredScalarObserverTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<DeferredScalarObserverTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new DeferredScalarObserverTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<DeferredScalarObserverTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(DeferredScalarObserverTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(DeferredScalarObserverTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement complete;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement fused;

            public org.junit.runners.model.Statement fusedReject;

            public org.junit.runners.model.Statement nonfusedTerminateMore;

            public org.junit.runners.model.Statement nonfusedError;

            public org.junit.runners.model.Statement fusedTerminateMore;

            public org.junit.runners.model.Statement fusedError;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement disposedAfterOnNext;

            public org.junit.runners.model.Statement fusedEmpty;

            public org.junit.runners.model.Statement nonfusedEmpty;

            public org.junit.runners.model.Statement customFusion;

            public org.junit.runners.model.Statement customFusionClear;

            public org.junit.runners.model.Statement offerThrow;

            public org.junit.runners.model.Statement customFusionDontConsume;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(DeferredScalarObserverTest::normal, "normal", this);
            this.payloads.error = _ClassStatement.forPayload(DeferredScalarObserverTest::error, "error", this);
            this.payloads.complete = _ClassStatement.forPayload(DeferredScalarObserverTest::complete, "complete", this);
            this.payloads.dispose = _ClassStatement.forPayload(DeferredScalarObserverTest::dispose, "dispose", this);
            this.payloads.fused = _ClassStatement.forPayload(DeferredScalarObserverTest::fused, "fused", this);
            this.payloads.fusedReject = _ClassStatement.forPayload(DeferredScalarObserverTest::fusedReject, "fusedReject", this);
            this.payloads.nonfusedTerminateMore = _ClassStatement.forPayload(DeferredScalarObserverTest::nonfusedTerminateMore, "nonfusedTerminateMore", this);
            this.payloads.nonfusedError = _ClassStatement.forPayload(DeferredScalarObserverTest::nonfusedError, "nonfusedError", this);
            this.payloads.fusedTerminateMore = _ClassStatement.forPayload(DeferredScalarObserverTest::fusedTerminateMore, "fusedTerminateMore", this);
            this.payloads.fusedError = _ClassStatement.forPayload(DeferredScalarObserverTest::fusedError, "fusedError", this);
            this.payloads.disposed = _ClassStatement.forPayload(DeferredScalarObserverTest::disposed, "disposed", this);
            this.payloads.disposedAfterOnNext = _ClassStatement.forPayload(DeferredScalarObserverTest::disposedAfterOnNext, "disposedAfterOnNext", this);
            this.payloads.fusedEmpty = _ClassStatement.forPayload(DeferredScalarObserverTest::fusedEmpty, "fusedEmpty", this);
            this.payloads.nonfusedEmpty = _ClassStatement.forPayload(DeferredScalarObserverTest::nonfusedEmpty, "nonfusedEmpty", this);
            this.payloads.customFusion = _ClassStatement.forPayload(DeferredScalarObserverTest::customFusion, "customFusion", this);
            this.payloads.customFusionClear = _ClassStatement.forPayload(DeferredScalarObserverTest::customFusionClear, "customFusionClear", this);
            this.payloads.offerThrow = _ClassStatement.forPayload(DeferredScalarObserverTest::offerThrow, "offerThrow", this);
            this.payloads.customFusionDontConsume = _ClassStatement.forPayload(DeferredScalarObserverTest::customFusionDontConsume, "customFusionDontConsume", this);
        }
    }
}
