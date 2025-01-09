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
package io.reactivex.rxjava3.disposables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.internal.disposables.DisposableHelper;

@RunWith(MockitoJUnitRunner.class)
public class SerialDisposableTests extends RxJavaTest {

    private SerialDisposable serialDisposable;

    @Before
    public void setUp() {
        serialDisposable = new SerialDisposable();
    }

    @Test
    public void unsubscribingWithoutUnderlyingDoesNothing() {
        serialDisposable.dispose();
    }

    @Test
    public void getDisposableShouldReturnset() {
        final Disposable underlying = mock(Disposable.class);
        serialDisposable.set(underlying);
        assertSame(underlying, serialDisposable.get());
        final Disposable another = mock(Disposable.class);
        serialDisposable.set(another);
        assertSame(another, serialDisposable.get());
    }

    @Test
    public void notDisposedWhenReplaced() {
        final Disposable underlying = mock(Disposable.class);
        serialDisposable.set(underlying);
        serialDisposable.replace(Disposable.empty());
        serialDisposable.dispose();
        verify(underlying, never()).dispose();
    }

    @Test
    public void unsubscribingTwiceDoesUnsubscribeOnce() {
        Disposable underlying = mock(Disposable.class);
        serialDisposable.set(underlying);
        serialDisposable.dispose();
        verify(underlying).dispose();
        serialDisposable.dispose();
        verifyNoMoreInteractions(underlying);
    }

    @Test
    public void settingSameDisposableTwiceDoesUnsubscribeIt() {
        Disposable underlying = mock(Disposable.class);
        serialDisposable.set(underlying);
        verifyNoInteractions(underlying);
        serialDisposable.set(underlying);
        verify(underlying).dispose();
    }

    @Test
    public void unsubscribingWithSingleUnderlyingUnsubscribes() {
        Disposable underlying = mock(Disposable.class);
        serialDisposable.set(underlying);
        underlying.dispose();
        verify(underlying).dispose();
    }

    @Test
    public void replacingFirstUnderlyingCausesUnsubscription() {
        Disposable first = mock(Disposable.class);
        serialDisposable.set(first);
        Disposable second = mock(Disposable.class);
        serialDisposable.set(second);
        verify(first).dispose();
    }

    @Test
    public void whenUnsubscribingSecondUnderlyingUnsubscribed() {
        Disposable first = mock(Disposable.class);
        serialDisposable.set(first);
        Disposable second = mock(Disposable.class);
        serialDisposable.set(second);
        serialDisposable.dispose();
        verify(second).dispose();
    }

    @Test
    public void settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscription() {
        serialDisposable.dispose();
        Disposable underlying = mock(Disposable.class);
        serialDisposable.set(underlying);
        verify(underlying).dispose();
    }

    @Test
    public void settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscriptionConcurrently() throws InterruptedException {
        final Disposable firstSet = mock(Disposable.class);
        serialDisposable.set(firstSet);
        final CountDownLatch start = new CountDownLatch(1);
        final int count = 10;
        final CountDownLatch end = new CountDownLatch(count);
        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        start.await();
                        serialDisposable.dispose();
                    } catch (InterruptedException e) {
                        fail(e.getMessage());
                    } finally {
                        end.countDown();
                    }
                }
            };
            t.start();
            threads.add(t);
        }
        final Disposable underlying = mock(Disposable.class);
        start.countDown();
        serialDisposable.set(underlying);
        end.await();
        verify(firstSet).dispose();
        verify(underlying).dispose();
        for (final Thread t : threads) {
            t.join();
        }
    }

    @Test
    public void concurrentSetDisposableShouldNotInterleave() throws InterruptedException {
        final int count = 10;
        final List<Disposable> subscriptions = new ArrayList<>();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(count);
        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            final Disposable subscription = mock(Disposable.class);
            subscriptions.add(subscription);
            final Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        start.await();
                        serialDisposable.set(subscription);
                    } catch (InterruptedException e) {
                        fail(e.getMessage());
                    } finally {
                        end.countDown();
                    }
                }
            };
            t.start();
            threads.add(t);
        }
        start.countDown();
        end.await();
        serialDisposable.dispose();
        for (final Disposable subscription : subscriptions) {
            verify(subscription).dispose();
        }
        for (final Thread t : threads) {
            t.join();
        }
    }

    @Test
    public void disposeState() {
        Disposable empty = Disposable.empty();
        SerialDisposable d = new SerialDisposable(empty);
        assertFalse(d.isDisposed());
        assertSame(empty, d.get());
        d.dispose();
        assertTrue(d.isDisposed());
        assertNotSame(empty, d.get());
        assertNotSame(DisposableHelper.DISPOSED, d.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public SerialDisposableTests instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribingWithoutUnderlyingDoesNothing() throws java.lang.Throwable {
            this.payloads.unsubscribingWithoutUnderlyingDoesNothing.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_getDisposableShouldReturnset() throws java.lang.Throwable {
            this.payloads.getDisposableShouldReturnset.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notDisposedWhenReplaced() throws java.lang.Throwable {
            this.payloads.notDisposedWhenReplaced.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribingTwiceDoesUnsubscribeOnce() throws java.lang.Throwable {
            this.payloads.unsubscribingTwiceDoesUnsubscribeOnce.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_settingSameDisposableTwiceDoesUnsubscribeIt() throws java.lang.Throwable {
            this.payloads.settingSameDisposableTwiceDoesUnsubscribeIt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribingWithSingleUnderlyingUnsubscribes() throws java.lang.Throwable {
            this.payloads.unsubscribingWithSingleUnderlyingUnsubscribes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replacingFirstUnderlyingCausesUnsubscription() throws java.lang.Throwable {
            this.payloads.replacingFirstUnderlyingCausesUnsubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_whenUnsubscribingSecondUnderlyingUnsubscribed() throws java.lang.Throwable {
            this.payloads.whenUnsubscribingSecondUnderlyingUnsubscribed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscription() throws java.lang.Throwable {
            this.payloads.settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscriptionConcurrently() throws java.lang.Throwable {
            this.payloads.settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscriptionConcurrently.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concurrentSetDisposableShouldNotInterleave() throws java.lang.Throwable {
            this.payloads.concurrentSetDisposableShouldNotInterleave.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeState() throws java.lang.Throwable {
            this.payloads.disposeState.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SerialDisposableTests> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SerialDisposableTests> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.setUp();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SerialDisposableTests> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SerialDisposableTests> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SerialDisposableTests();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SerialDisposableTests> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SerialDisposableTests.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SerialDisposableTests.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement unsubscribingWithoutUnderlyingDoesNothing;

            public org.junit.runners.model.Statement getDisposableShouldReturnset;

            public org.junit.runners.model.Statement notDisposedWhenReplaced;

            public org.junit.runners.model.Statement unsubscribingTwiceDoesUnsubscribeOnce;

            public org.junit.runners.model.Statement settingSameDisposableTwiceDoesUnsubscribeIt;

            public org.junit.runners.model.Statement unsubscribingWithSingleUnderlyingUnsubscribes;

            public org.junit.runners.model.Statement replacingFirstUnderlyingCausesUnsubscription;

            public org.junit.runners.model.Statement whenUnsubscribingSecondUnderlyingUnsubscribed;

            public org.junit.runners.model.Statement settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscription;

            public org.junit.runners.model.Statement settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscriptionConcurrently;

            public org.junit.runners.model.Statement concurrentSetDisposableShouldNotInterleave;

            public org.junit.runners.model.Statement disposeState;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.unsubscribingWithoutUnderlyingDoesNothing = _ClassStatement.forPayload(SerialDisposableTests::unsubscribingWithoutUnderlyingDoesNothing, "unsubscribingWithoutUnderlyingDoesNothing", this);
            this.payloads.getDisposableShouldReturnset = _ClassStatement.forPayload(SerialDisposableTests::getDisposableShouldReturnset, "getDisposableShouldReturnset", this);
            this.payloads.notDisposedWhenReplaced = _ClassStatement.forPayload(SerialDisposableTests::notDisposedWhenReplaced, "notDisposedWhenReplaced", this);
            this.payloads.unsubscribingTwiceDoesUnsubscribeOnce = _ClassStatement.forPayload(SerialDisposableTests::unsubscribingTwiceDoesUnsubscribeOnce, "unsubscribingTwiceDoesUnsubscribeOnce", this);
            this.payloads.settingSameDisposableTwiceDoesUnsubscribeIt = _ClassStatement.forPayload(SerialDisposableTests::settingSameDisposableTwiceDoesUnsubscribeIt, "settingSameDisposableTwiceDoesUnsubscribeIt", this);
            this.payloads.unsubscribingWithSingleUnderlyingUnsubscribes = _ClassStatement.forPayload(SerialDisposableTests::unsubscribingWithSingleUnderlyingUnsubscribes, "unsubscribingWithSingleUnderlyingUnsubscribes", this);
            this.payloads.replacingFirstUnderlyingCausesUnsubscription = _ClassStatement.forPayload(SerialDisposableTests::replacingFirstUnderlyingCausesUnsubscription, "replacingFirstUnderlyingCausesUnsubscription", this);
            this.payloads.whenUnsubscribingSecondUnderlyingUnsubscribed = _ClassStatement.forPayload(SerialDisposableTests::whenUnsubscribingSecondUnderlyingUnsubscribed, "whenUnsubscribingSecondUnderlyingUnsubscribed", this);
            this.payloads.settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscription = _ClassStatement.forPayload(SerialDisposableTests::settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscription, "settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscription", this);
            this.payloads.settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscriptionConcurrently = _ClassStatement.forPayload(SerialDisposableTests::settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscriptionConcurrently, "settingUnderlyingWhenUnsubscribedCausesImmediateUnsubscriptionConcurrently", this);
            this.payloads.concurrentSetDisposableShouldNotInterleave = _ClassStatement.forPayload(SerialDisposableTests::concurrentSetDisposableShouldNotInterleave, "concurrentSetDisposableShouldNotInterleave", this);
            this.payloads.disposeState = _ClassStatement.forPayload(SerialDisposableTests::disposeState, "disposeState", this);
        }
    }
}
