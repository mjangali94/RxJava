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

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.testsupport.*;

public class HalfSerializerObserverTest extends RxJavaTest {

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void reentrantOnNextOnNext() {
        final AtomicInteger wip = new AtomicInteger();
        final AtomicThrowable error = new AtomicThrowable();
        final Observer[] a = { null };
        final TestObserver to = new TestObserver();
        Observer observer = new Observer() {

            @Override
            public void onSubscribe(Disposable d) {
                to.onSubscribe(d);
            }

            @Override
            public void onNext(Object t) {
                if (t.equals(1)) {
                    HalfSerializer.onNext(a[0], 2, wip, error);
                }
                to.onNext(t);
            }

            @Override
            public void onError(Throwable t) {
                to.onError(t);
            }

            @Override
            public void onComplete() {
                to.onComplete();
            }
        };
        a[0] = observer;
        observer.onSubscribe(Disposable.empty());
        HalfSerializer.onNext(observer, 1, wip, error);
        to.assertValue(1).assertNoErrors().assertNotComplete();
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void reentrantOnNextOnError() {
        final AtomicInteger wip = new AtomicInteger();
        final AtomicThrowable error = new AtomicThrowable();
        final Observer[] a = { null };
        final TestObserver to = new TestObserver();
        Observer observer = new Observer() {

            @Override
            public void onSubscribe(Disposable d) {
                to.onSubscribe(d);
            }

            @Override
            public void onNext(Object t) {
                if (t.equals(1)) {
                    HalfSerializer.onError(a[0], new TestException(), wip, error);
                }
                to.onNext(t);
            }

            @Override
            public void onError(Throwable t) {
                to.onError(t);
            }

            @Override
            public void onComplete() {
                to.onComplete();
            }
        };
        a[0] = observer;
        observer.onSubscribe(Disposable.empty());
        HalfSerializer.onNext(observer, 1, wip, error);
        to.assertFailure(TestException.class, 1);
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void reentrantOnNextOnComplete() {
        final AtomicInteger wip = new AtomicInteger();
        final AtomicThrowable error = new AtomicThrowable();
        final Observer[] a = { null };
        final TestObserver to = new TestObserver();
        Observer observer = new Observer() {

            @Override
            public void onSubscribe(Disposable d) {
                to.onSubscribe(d);
            }

            @Override
            public void onNext(Object t) {
                if (t.equals(1)) {
                    HalfSerializer.onComplete(a[0], wip, error);
                }
                to.onNext(t);
            }

            @Override
            public void onError(Throwable t) {
                to.onError(t);
            }

            @Override
            public void onComplete() {
                to.onComplete();
            }
        };
        a[0] = observer;
        observer.onSubscribe(Disposable.empty());
        HalfSerializer.onNext(observer, 1, wip, error);
        to.assertResult(1);
    }

    @Test
    @SuppressUndeliverable
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void reentrantErrorOnError() {
        final AtomicInteger wip = new AtomicInteger();
        final AtomicThrowable error = new AtomicThrowable();
        final Observer[] a = { null };
        final TestObserver to = new TestObserver();
        Observer observer = new Observer() {

            @Override
            public void onSubscribe(Disposable d) {
                to.onSubscribe(d);
            }

            @Override
            public void onNext(Object t) {
                to.onNext(t);
            }

            @Override
            public void onError(Throwable t) {
                to.onError(t);
                HalfSerializer.onError(a[0], new IOException(), wip, error);
            }

            @Override
            public void onComplete() {
                to.onComplete();
            }
        };
        a[0] = observer;
        observer.onSubscribe(Disposable.empty());
        HalfSerializer.onError(observer, new TestException(), wip, error);
        to.assertFailure(TestException.class);
    }

    @Test
    public void onNextOnCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicInteger wip = new AtomicInteger();
            final AtomicThrowable error = new AtomicThrowable();
            final TestObserver<Integer> to = new TestObserver<>();
            to.onSubscribe(Disposable.empty());
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    HalfSerializer.onNext(to, 1, wip, error);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    HalfSerializer.onComplete(to, wip, error);
                }
            };
            TestHelper.race(r1, r2);
            to.assertComplete().assertNoErrors();
            assertTrue(to.values().size() <= 1);
        }
    }

    @Test
    @SuppressUndeliverable
    public void onErrorOnCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicInteger wip = new AtomicInteger();
            final AtomicThrowable error = new AtomicThrowable();
            final TestObserverEx<Integer> to = new TestObserverEx<>();
            to.onSubscribe(Disposable.empty());
            final TestException ex = new TestException();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    HalfSerializer.onError(to, ex, wip, error);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    HalfSerializer.onComplete(to, wip, error);
                }
            };
            TestHelper.race(r1, r2);
            if (to.completions() != 0) {
                to.assertResult();
            } else {
                to.assertFailure(TestException.class);
            }
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private HalfSerializerObserverTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantOnNextOnNext() throws java.lang.Throwable {
            this.payloads.reentrantOnNextOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantOnNextOnError() throws java.lang.Throwable {
            this.payloads.reentrantOnNextOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantOnNextOnComplete() throws java.lang.Throwable {
            this.payloads.reentrantOnNextOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_reentrantErrorOnError() throws java.lang.Throwable {
            this.payloads.reentrantErrorOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextOnCompleteRace() throws java.lang.Throwable {
            this.payloads.onNextOnCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorOnCompleteRace() throws java.lang.Throwable {
            this.payloads.onErrorOnCompleteRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<HalfSerializerObserverTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<HalfSerializerObserverTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<HalfSerializerObserverTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<HalfSerializerObserverTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new HalfSerializerObserverTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<HalfSerializerObserverTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(HalfSerializerObserverTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(HalfSerializerObserverTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement reentrantOnNextOnNext;

            public org.junit.runners.model.Statement reentrantOnNextOnError;

            public org.junit.runners.model.Statement reentrantOnNextOnComplete;

            public org.junit.runners.model.Statement reentrantErrorOnError;

            public org.junit.runners.model.Statement onNextOnCompleteRace;

            public org.junit.runners.model.Statement onErrorOnCompleteRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.reentrantOnNextOnNext = _ClassStatement.forPayload(HalfSerializerObserverTest::reentrantOnNextOnNext, "reentrantOnNextOnNext", this);
            this.payloads.reentrantOnNextOnError = _ClassStatement.forPayload(HalfSerializerObserverTest::reentrantOnNextOnError, "reentrantOnNextOnError", this);
            this.payloads.reentrantOnNextOnComplete = _ClassStatement.forPayload(HalfSerializerObserverTest::reentrantOnNextOnComplete, "reentrantOnNextOnComplete", this);
            this.payloads.reentrantErrorOnError = _ClassStatement.forPayload(HalfSerializerObserverTest::reentrantErrorOnError, "reentrantErrorOnError", this);
            this.payloads.onNextOnCompleteRace = _ClassStatement.forPayload(HalfSerializerObserverTest::onNextOnCompleteRace, "onNextOnCompleteRace", this);
            this.payloads.onErrorOnCompleteRace = _ClassStatement.forPayload(HalfSerializerObserverTest::onErrorOnCompleteRace, "onErrorOnCompleteRace", this);
        }
    }
}
