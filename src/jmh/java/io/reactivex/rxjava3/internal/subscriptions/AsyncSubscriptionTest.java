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
package io.reactivex.rxjava3.internal.subscriptions;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.Disposable;

public class AsyncSubscriptionTest extends RxJavaTest {

    @Test
    public void noResource() {
        AsyncSubscription as = new AsyncSubscription();
        Subscription s = mock(Subscription.class);
        as.setSubscription(s);
        as.request(1);
        as.cancel();
        verify(s).request(1);
        verify(s).cancel();
    }

    @Test
    public void requestBeforeSet() {
        AsyncSubscription as = new AsyncSubscription();
        Subscription s = mock(Subscription.class);
        as.request(1);
        as.setSubscription(s);
        as.cancel();
        verify(s).request(1);
        verify(s).cancel();
    }

    @Test
    public void cancelBeforeSet() {
        AsyncSubscription as = new AsyncSubscription();
        Subscription s = mock(Subscription.class);
        as.request(1);
        as.cancel();
        as.setSubscription(s);
        verify(s, never()).request(1);
        verify(s).cancel();
    }

    @Test
    public void singleSet() {
        AsyncSubscription as = new AsyncSubscription();
        Subscription s = mock(Subscription.class);
        as.setSubscription(s);
        Subscription s1 = mock(Subscription.class);
        as.setSubscription(s1);
        assertSame(as.actual.get(), s);
        verify(s1).cancel();
    }

    @Test
    public void initialResource() {
        Disposable r = mock(Disposable.class);
        AsyncSubscription as = new AsyncSubscription(r);
        as.cancel();
        verify(r).dispose();
    }

    @Test
    public void setResource() {
        AsyncSubscription as = new AsyncSubscription();
        Disposable r = mock(Disposable.class);
        assertTrue(as.setResource(r));
        as.cancel();
        verify(r).dispose();
    }

    @Test
    public void replaceResource() {
        AsyncSubscription as = new AsyncSubscription();
        Disposable r = mock(Disposable.class);
        assertTrue(as.replaceResource(r));
        as.cancel();
        verify(r).dispose();
    }

    @Test
    public void setResource2() {
        AsyncSubscription as = new AsyncSubscription();
        Disposable r = mock(Disposable.class);
        assertTrue(as.setResource(r));
        Disposable r2 = mock(Disposable.class);
        assertTrue(as.setResource(r2));
        as.cancel();
        verify(r).dispose();
        verify(r2).dispose();
    }

    @Test
    public void replaceResource2() {
        AsyncSubscription as = new AsyncSubscription();
        Disposable r = mock(Disposable.class);
        assertTrue(as.replaceResource(r));
        Disposable r2 = mock(Disposable.class);
        as.replaceResource(r2);
        as.cancel();
        verify(r, never()).dispose();
        verify(r2).dispose();
    }

    @Test
    public void setResourceAfterCancel() {
        AsyncSubscription as = new AsyncSubscription();
        as.cancel();
        Disposable r = mock(Disposable.class);
        as.setResource(r);
        verify(r).dispose();
    }

    @Test
    public void replaceResourceAfterCancel() {
        AsyncSubscription as = new AsyncSubscription();
        as.cancel();
        Disposable r = mock(Disposable.class);
        as.replaceResource(r);
        verify(r).dispose();
    }

    @Test
    public void cancelOnce() {
        Disposable r = mock(Disposable.class);
        AsyncSubscription as = new AsyncSubscription(r);
        Subscription s = mock(Subscription.class);
        as.setSubscription(s);
        as.cancel();
        as.cancel();
        as.cancel();
        verify(s, never()).request(anyLong());
        verify(s).cancel();
        verify(r).dispose();
    }

    @Test
    public void disposed() {
        AsyncSubscription as = new AsyncSubscription();
        assertFalse(as.isDisposed());
        as.dispose();
        assertTrue(as.isDisposed());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public AsyncSubscriptionTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noResource() throws java.lang.Throwable {
            this.payloads.noResource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestBeforeSet() throws java.lang.Throwable {
            this.payloads.requestBeforeSet.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelBeforeSet() throws java.lang.Throwable {
            this.payloads.cancelBeforeSet.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSet() throws java.lang.Throwable {
            this.payloads.singleSet.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_initialResource() throws java.lang.Throwable {
            this.payloads.initialResource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setResource() throws java.lang.Throwable {
            this.payloads.setResource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaceResource() throws java.lang.Throwable {
            this.payloads.replaceResource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setResource2() throws java.lang.Throwable {
            this.payloads.setResource2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaceResource2() throws java.lang.Throwable {
            this.payloads.replaceResource2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setResourceAfterCancel() throws java.lang.Throwable {
            this.payloads.setResourceAfterCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaceResourceAfterCancel() throws java.lang.Throwable {
            this.payloads.replaceResourceAfterCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelOnce() throws java.lang.Throwable {
            this.payloads.cancelOnce.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<AsyncSubscriptionTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<AsyncSubscriptionTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<AsyncSubscriptionTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<AsyncSubscriptionTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new AsyncSubscriptionTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<AsyncSubscriptionTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(AsyncSubscriptionTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(AsyncSubscriptionTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement noResource;

            public org.junit.runners.model.Statement requestBeforeSet;

            public org.junit.runners.model.Statement cancelBeforeSet;

            public org.junit.runners.model.Statement singleSet;

            public org.junit.runners.model.Statement initialResource;

            public org.junit.runners.model.Statement setResource;

            public org.junit.runners.model.Statement replaceResource;

            public org.junit.runners.model.Statement setResource2;

            public org.junit.runners.model.Statement replaceResource2;

            public org.junit.runners.model.Statement setResourceAfterCancel;

            public org.junit.runners.model.Statement replaceResourceAfterCancel;

            public org.junit.runners.model.Statement cancelOnce;

            public org.junit.runners.model.Statement disposed;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.noResource = _ClassStatement.forPayload(AsyncSubscriptionTest::noResource, "noResource", this);
            this.payloads.requestBeforeSet = _ClassStatement.forPayload(AsyncSubscriptionTest::requestBeforeSet, "requestBeforeSet", this);
            this.payloads.cancelBeforeSet = _ClassStatement.forPayload(AsyncSubscriptionTest::cancelBeforeSet, "cancelBeforeSet", this);
            this.payloads.singleSet = _ClassStatement.forPayload(AsyncSubscriptionTest::singleSet, "singleSet", this);
            this.payloads.initialResource = _ClassStatement.forPayload(AsyncSubscriptionTest::initialResource, "initialResource", this);
            this.payloads.setResource = _ClassStatement.forPayload(AsyncSubscriptionTest::setResource, "setResource", this);
            this.payloads.replaceResource = _ClassStatement.forPayload(AsyncSubscriptionTest::replaceResource, "replaceResource", this);
            this.payloads.setResource2 = _ClassStatement.forPayload(AsyncSubscriptionTest::setResource2, "setResource2", this);
            this.payloads.replaceResource2 = _ClassStatement.forPayload(AsyncSubscriptionTest::replaceResource2, "replaceResource2", this);
            this.payloads.setResourceAfterCancel = _ClassStatement.forPayload(AsyncSubscriptionTest::setResourceAfterCancel, "setResourceAfterCancel", this);
            this.payloads.replaceResourceAfterCancel = _ClassStatement.forPayload(AsyncSubscriptionTest::replaceResourceAfterCancel, "replaceResourceAfterCancel", this);
            this.payloads.cancelOnce = _ClassStatement.forPayload(AsyncSubscriptionTest::cancelOnce, "cancelOnce", this);
            this.payloads.disposed = _ClassStatement.forPayload(AsyncSubscriptionTest::disposed, "disposed", this);
        }
    }
}
