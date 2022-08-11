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
import java.util.List;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.exceptions.ProtocolViolationException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SubscriptionHelperTest extends RxJavaTest {

    @Test
    public void checkEnum() {
        TestHelper.checkEnum(SubscriptionHelper.class);
    }

    @Test
    public void validateNullThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            SubscriptionHelper.validate(null, null);
            TestHelper.assertError(errors, 0, NullPointerException.class, "next is null");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void cancelNoOp() {
        SubscriptionHelper.CANCELLED.cancel();
    }

    @Test
    public void set() {
        AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
        BooleanSubscription bs1 = new BooleanSubscription();
        assertTrue(SubscriptionHelper.set(atomicSubscription, bs1));
        BooleanSubscription bs2 = new BooleanSubscription();
        assertTrue(SubscriptionHelper.set(atomicSubscription, bs2));
        assertTrue(bs1.isCancelled());
        assertFalse(bs2.isCancelled());
    }

    @Test
    public void replace() {
        AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
        BooleanSubscription bs1 = new BooleanSubscription();
        assertTrue(SubscriptionHelper.replace(atomicSubscription, bs1));
        BooleanSubscription bs2 = new BooleanSubscription();
        assertTrue(SubscriptionHelper.replace(atomicSubscription, bs2));
        assertFalse(bs1.isCancelled());
        assertFalse(bs2.isCancelled());
    }

    @Test
    public void cancelRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    SubscriptionHelper.cancel(atomicSubscription);
                }
            };
            TestHelper.race(r, r);
        }
    }

    @Test
    public void setRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
            final BooleanSubscription bs1 = new BooleanSubscription();
            final BooleanSubscription bs2 = new BooleanSubscription();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    SubscriptionHelper.set(atomicSubscription, bs1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    SubscriptionHelper.set(atomicSubscription, bs2);
                }
            };
            TestHelper.race(r1, r2);
            assertTrue(bs1.isCancelled() ^ bs2.isCancelled());
        }
    }

    @Test
    public void replaceRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
            final BooleanSubscription bs1 = new BooleanSubscription();
            final BooleanSubscription bs2 = new BooleanSubscription();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    SubscriptionHelper.replace(atomicSubscription, bs1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    SubscriptionHelper.replace(atomicSubscription, bs2);
                }
            };
            TestHelper.race(r1, r2);
            assertFalse(bs1.isCancelled());
            assertFalse(bs2.isCancelled());
        }
    }

    @Test
    public void cancelAndChange() {
        AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
        SubscriptionHelper.cancel(atomicSubscription);
        BooleanSubscription bs1 = new BooleanSubscription();
        assertFalse(SubscriptionHelper.set(atomicSubscription, bs1));
        assertTrue(bs1.isCancelled());
        assertFalse(SubscriptionHelper.set(atomicSubscription, null));
        BooleanSubscription bs2 = new BooleanSubscription();
        assertFalse(SubscriptionHelper.replace(atomicSubscription, bs2));
        assertTrue(bs2.isCancelled());
        assertFalse(SubscriptionHelper.replace(atomicSubscription, null));
    }

    @Test
    public void invalidDeferredRequest() {
        AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
        AtomicLong r = new AtomicLong();
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            SubscriptionHelper.deferredRequest(atomicSubscription, r, -99);
            TestHelper.assertError(errors, 0, IllegalArgumentException.class, "n > 0 required but it was -99");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void deferredRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final AtomicReference<Subscription> atomicSubscription = new AtomicReference<>();
            final AtomicLong r = new AtomicLong();
            final AtomicLong q = new AtomicLong();
            final Subscription a = new Subscription() {

                @Override
                public void request(long n) {
                    q.addAndGet(n);
                }

                @Override
                public void cancel() {
                }
            };
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    SubscriptionHelper.deferredSetOnce(atomicSubscription, r, a);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    SubscriptionHelper.deferredRequest(atomicSubscription, r, 1);
                }
            };
            TestHelper.race(r1, r2);
            assertSame(a, atomicSubscription.get());
            assertEquals(1, q.get());
            assertEquals(0, r.get());
        }
    }

    @Test
    public void setOnceAndRequest() {
        AtomicReference<Subscription> ref = new AtomicReference<>();
        Subscription sub = mock(Subscription.class);
        assertTrue(SubscriptionHelper.setOnce(ref, sub, 1));
        verify(sub).request(1);
        verify(sub, never()).cancel();
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            sub = mock(Subscription.class);
            assertFalse(SubscriptionHelper.setOnce(ref, sub, 1));
            verify(sub, never()).request(anyLong());
            verify(sub).cancel();
            TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SubscriptionHelperTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkEnum() throws java.lang.Throwable {
            this.payloads.checkEnum.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_validateNullThrows() throws java.lang.Throwable {
            this.payloads.validateNullThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelNoOp() throws java.lang.Throwable {
            this.payloads.cancelNoOp.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_set() throws java.lang.Throwable {
            this.payloads.set.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replace() throws java.lang.Throwable {
            this.payloads.replace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelRace() throws java.lang.Throwable {
            this.payloads.cancelRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setRace() throws java.lang.Throwable {
            this.payloads.setRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaceRace() throws java.lang.Throwable {
            this.payloads.replaceRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAndChange() throws java.lang.Throwable {
            this.payloads.cancelAndChange.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_invalidDeferredRequest() throws java.lang.Throwable {
            this.payloads.invalidDeferredRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferredRace() throws java.lang.Throwable {
            this.payloads.deferredRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_setOnceAndRequest() throws java.lang.Throwable {
            this.payloads.setOnceAndRequest.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SubscriptionHelperTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SubscriptionHelperTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SubscriptionHelperTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SubscriptionHelperTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SubscriptionHelperTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SubscriptionHelperTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SubscriptionHelperTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SubscriptionHelperTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement checkEnum;

            public org.junit.runners.model.Statement validateNullThrows;

            public org.junit.runners.model.Statement cancelNoOp;

            public org.junit.runners.model.Statement set;

            public org.junit.runners.model.Statement replace;

            public org.junit.runners.model.Statement cancelRace;

            public org.junit.runners.model.Statement setRace;

            public org.junit.runners.model.Statement replaceRace;

            public org.junit.runners.model.Statement cancelAndChange;

            public org.junit.runners.model.Statement invalidDeferredRequest;

            public org.junit.runners.model.Statement deferredRace;

            public org.junit.runners.model.Statement setOnceAndRequest;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.checkEnum = _ClassStatement.forPayload(SubscriptionHelperTest::checkEnum, "checkEnum", this);
            this.payloads.validateNullThrows = _ClassStatement.forPayload(SubscriptionHelperTest::validateNullThrows, "validateNullThrows", this);
            this.payloads.cancelNoOp = _ClassStatement.forPayload(SubscriptionHelperTest::cancelNoOp, "cancelNoOp", this);
            this.payloads.set = _ClassStatement.forPayload(SubscriptionHelperTest::set, "set", this);
            this.payloads.replace = _ClassStatement.forPayload(SubscriptionHelperTest::replace, "replace", this);
            this.payloads.cancelRace = _ClassStatement.forPayload(SubscriptionHelperTest::cancelRace, "cancelRace", this);
            this.payloads.setRace = _ClassStatement.forPayload(SubscriptionHelperTest::setRace, "setRace", this);
            this.payloads.replaceRace = _ClassStatement.forPayload(SubscriptionHelperTest::replaceRace, "replaceRace", this);
            this.payloads.cancelAndChange = _ClassStatement.forPayload(SubscriptionHelperTest::cancelAndChange, "cancelAndChange", this);
            this.payloads.invalidDeferredRequest = _ClassStatement.forPayload(SubscriptionHelperTest::invalidDeferredRequest, "invalidDeferredRequest", this);
            this.payloads.deferredRace = _ClassStatement.forPayload(SubscriptionHelperTest::deferredRace, "deferredRace", this);
            this.payloads.setOnceAndRequest = _ClassStatement.forPayload(SubscriptionHelperTest::setOnceAndRequest, "setOnceAndRequest", this);
        }
    }
}
