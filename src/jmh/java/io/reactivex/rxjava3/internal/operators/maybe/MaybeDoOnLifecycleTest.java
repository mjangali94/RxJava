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
package io.reactivex.rxjava3.internal.operators.maybe;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.MaybeSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeDoOnLifecycleTest extends RxJavaTest {

    @Test
    public void success() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<? super Disposable> onSubscribe = mock(Consumer.class);
        Action onDispose = mock(Action.class);
        Maybe.just(1).doOnLifecycle(onSubscribe, onDispose).test().assertResult(1);
        verify(onSubscribe).accept(any());
        verify(onDispose, never()).run();
    }

    @Test
    public void empty() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<? super Disposable> onSubscribe = mock(Consumer.class);
        Action onDispose = mock(Action.class);
        Maybe.empty().doOnLifecycle(onSubscribe, onDispose).test().assertResult();
        verify(onSubscribe).accept(any());
        verify(onDispose, never()).run();
    }

    @Test
    public void error() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<? super Disposable> onSubscribe = mock(Consumer.class);
        Action onDispose = mock(Action.class);
        Maybe.error(new TestException()).doOnLifecycle(onSubscribe, onDispose).test().assertFailure(TestException.class);
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
            new Maybe<Integer>() {

                @Override
                protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                    observer.onSubscribe(bs);
                    observer.onError(new TestException("Second"));
                    observer.onComplete();
                    observer.onSuccess(1);
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
            MaybeSubject<Integer> ms = MaybeSubject.create();
            TestObserver<Integer> to = ms.doOnLifecycle(onSubscribe, onDispose).test();
            assertTrue(ms.hasObservers());
            to.dispose();
            assertFalse(ms.hasObservers());
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
        MaybeSubject<Integer> ms = MaybeSubject.create();
        TestObserver<Integer> to = ms.doOnLifecycle(onSubscribe, onDispose).test();
        assertTrue(ms.hasObservers());
        to.dispose();
        assertFalse(ms.hasObservers());
        verify(onSubscribe).accept(any());
        verify(onDispose).run();
    }

    @Test
    public void isDisposed() {
        TestHelper.checkDisposed(MaybeSubject.create().doOnLifecycle(d -> {
        }, () -> {
        }));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(m -> m.doOnLifecycle(d -> {
        }, () -> {
        }));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeDoOnLifecycleTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_success() throws java.lang.Throwable {
            this.payloads.success.evaluate();
        }

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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDoOnLifecycleTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDoOnLifecycleTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDoOnLifecycleTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDoOnLifecycleTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeDoOnLifecycleTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDoOnLifecycleTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeDoOnLifecycleTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeDoOnLifecycleTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement success;

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
            this.payloads.success = _ClassStatement.forPayload(MaybeDoOnLifecycleTest::success, "success", this);
            this.payloads.empty = _ClassStatement.forPayload(MaybeDoOnLifecycleTest::empty, "empty", this);
            this.payloads.error = _ClassStatement.forPayload(MaybeDoOnLifecycleTest::error, "error", this);
            this.payloads.onSubscribeCrash = _ClassStatement.forPayload(MaybeDoOnLifecycleTest::onSubscribeCrash, "onSubscribeCrash", this);
            this.payloads.onDisposeCrash = _ClassStatement.forPayload(MaybeDoOnLifecycleTest::onDisposeCrash, "onDisposeCrash", this);
            this.payloads.dispose = _ClassStatement.forPayload(MaybeDoOnLifecycleTest::dispose, "dispose", this);
            this.payloads.isDisposed = _ClassStatement.forPayload(MaybeDoOnLifecycleTest::isDisposed, "isDisposed", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(MaybeDoOnLifecycleTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
