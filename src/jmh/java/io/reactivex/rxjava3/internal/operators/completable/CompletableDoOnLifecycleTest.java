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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class CompletableDoOnLifecycleTest extends RxJavaTest {

    @Test
    public void empty() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<? super Disposable> onSubscribe = mock(Consumer.class);
        Action onDispose = mock(Action.class);
        Completable.complete().doOnLifecycle(onSubscribe, onDispose).test().assertResult();
        verify(onSubscribe).accept(any());
        verify(onDispose, never()).run();
    }

    @Test
    public void error() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<? super Disposable> onSubscribe = mock(Consumer.class);
        Action onDispose = mock(Action.class);
        Completable.error(new TestException()).doOnLifecycle(onSubscribe, onDispose).test().assertFailure(TestException.class);
        verify(onSubscribe).accept(any());
        verify(onDispose, never()).run();
    }

    @Test
    public void onSubscribeCrash() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<? super Disposable> onSubscribe = mock(Consumer.class);
            Action onDispose = mock(Action.class);
            doThrow(new TestException("First")).when(onSubscribe).accept(any());
            Disposable bs = Disposable.empty();
            new Completable() {

                @Override
                protected void subscribeActual(CompletableObserver observer) {
                    observer.onSubscribe(bs);
                    observer.onError(new TestException("Second"));
                    observer.onComplete();
                }
            }.doOnLifecycle(onSubscribe, onDispose).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            assertTrue(bs.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
            verify(onSubscribe).accept(any());
            verify(onDispose, never()).run();
        });
    }

    @Test
    public void onDisposeCrash() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<? super Disposable> onSubscribe = mock(Consumer.class);
            Action onDispose = mock(Action.class);
            doThrow(new TestException("First")).when(onDispose).run();
            CompletableSubject cs = CompletableSubject.create();
            TestObserver<Void> to = cs.doOnLifecycle(onSubscribe, onDispose).test();
            assertTrue(cs.hasObservers());
            to.dispose();
            assertFalse(cs.hasObservers());
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "First");
            verify(onSubscribe).accept(any());
            verify(onDispose).run();
        });
    }

    @Test
    public void dispose() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<? super Disposable> onSubscribe = mock(Consumer.class);
        Action onDispose = mock(Action.class);
        CompletableSubject cs = CompletableSubject.create();
        TestObserver<Void> to = cs.doOnLifecycle(onSubscribe, onDispose).test();
        assertTrue(cs.hasObservers());
        to.dispose();
        assertFalse(cs.hasObservers());
        verify(onSubscribe).accept(any());
        verify(onDispose).run();
    }

    @Test
    public void isDisposed() {
        TestHelper.checkDisposed(CompletableSubject.create().doOnLifecycle(d -> {
        }, () -> {
        }));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeCompletable(m -> m.doOnLifecycle(d -> {
        }, () -> {
        }));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private CompletableDoOnLifecycleTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeCrash() throws java.lang.Throwable {
            this.payloads.onSubscribeCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onDisposeCrash() throws java.lang.Throwable {
            this.payloads.onDisposeCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposed() throws java.lang.Throwable {
            this.payloads.isDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDoOnLifecycleTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDoOnLifecycleTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDoOnLifecycleTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDoOnLifecycleTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableDoOnLifecycleTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableDoOnLifecycleTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableDoOnLifecycleTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableDoOnLifecycleTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement onSubscribeCrash;

            public org.junit.runners.model.Statement onDisposeCrash;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement isDisposed;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.empty = _ClassStatement.forPayload(CompletableDoOnLifecycleTest::empty, "empty", this);
            this.payloads.error = _ClassStatement.forPayload(CompletableDoOnLifecycleTest::error, "error", this);
            this.payloads.onSubscribeCrash = _ClassStatement.forPayload(CompletableDoOnLifecycleTest::onSubscribeCrash, "onSubscribeCrash", this);
            this.payloads.onDisposeCrash = _ClassStatement.forPayload(CompletableDoOnLifecycleTest::onDisposeCrash, "onDisposeCrash", this);
            this.payloads.dispose = _ClassStatement.forPayload(CompletableDoOnLifecycleTest::dispose, "dispose", this);
            this.payloads.isDisposed = _ClassStatement.forPayload(CompletableDoOnLifecycleTest::isDisposed, "isDisposed", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(CompletableDoOnLifecycleTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
