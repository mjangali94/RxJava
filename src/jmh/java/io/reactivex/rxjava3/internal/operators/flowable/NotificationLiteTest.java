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
package io.reactivex.rxjava3.internal.operators.flowable;

import static org.junit.Assert.*;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.internal.util.NotificationLite;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class NotificationLiteTest extends RxJavaTest {

    @Test
    public void complete() {
        Object n = NotificationLite.next("Hello");
        Object c = NotificationLite.complete();
        assertTrue(NotificationLite.isComplete(c));
        assertFalse(NotificationLite.isComplete(n));
        assertEquals("Hello", NotificationLite.getValue(n));
    }

    @Test
    public void valueKind() {
        assertSame(1, NotificationLite.next(1));
    }

    @Test
    public void soloEnum() {
        TestHelper.checkEnum(NotificationLite.class);
    }

    @Test
    public void errorNotification() {
        Object o = NotificationLite.error(new TestException());
        assertEquals("NotificationLite.Error[io.reactivex.rxjava3.exceptions.TestException]", o.toString());
        assertTrue(NotificationLite.isError(o));
        assertFalse(NotificationLite.isComplete(o));
        assertFalse(NotificationLite.isDisposable(o));
        assertFalse(NotificationLite.isSubscription(o));
        assertTrue(NotificationLite.getError(o) instanceof TestException);
    }

    @Test
    public void completeNotification() {
        Object o = NotificationLite.complete();
        Object o2 = NotificationLite.complete();
        assertSame(o, o2);
        assertFalse(NotificationLite.isError(o));
        assertTrue(NotificationLite.isComplete(o));
        assertFalse(NotificationLite.isDisposable(o));
        assertFalse(NotificationLite.isSubscription(o));
        assertEquals("NotificationLite.Complete", o.toString());
        assertTrue(NotificationLite.isComplete(o));
    }

    @Test
    public void disposableNotification() {
        Object o = NotificationLite.disposable(Disposable.empty());
        assertEquals("NotificationLite.Disposable[RunnableDisposable(disposed=false, EmptyRunnable)]", o.toString());
        assertFalse(NotificationLite.isError(o));
        assertFalse(NotificationLite.isComplete(o));
        assertTrue(NotificationLite.isDisposable(o));
        assertFalse(NotificationLite.isSubscription(o));
        assertNotNull(NotificationLite.getDisposable(o));
    }

    @Test
    public void subscriptionNotification() {
        Object o = NotificationLite.subscription(new BooleanSubscription());
        assertEquals("NotificationLite.Subscription[BooleanSubscription(cancelled=false)]", o.toString());
        assertFalse(NotificationLite.isError(o));
        assertFalse(NotificationLite.isComplete(o));
        assertFalse(NotificationLite.isDisposable(o));
        assertTrue(NotificationLite.isSubscription(o));
        assertNotNull(NotificationLite.getSubscription(o));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private NotificationLiteTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_complete() throws java.lang.Throwable {
            this.payloads.complete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_valueKind() throws java.lang.Throwable {
            this.payloads.valueKind.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_soloEnum() throws java.lang.Throwable {
            this.payloads.soloEnum.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNotification() throws java.lang.Throwable {
            this.payloads.errorNotification.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeNotification() throws java.lang.Throwable {
            this.payloads.completeNotification.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposableNotification() throws java.lang.Throwable {
            this.payloads.disposableNotification.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscriptionNotification() throws java.lang.Throwable {
            this.payloads.subscriptionNotification.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<NotificationLiteTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<NotificationLiteTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<NotificationLiteTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<NotificationLiteTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new NotificationLiteTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<NotificationLiteTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(NotificationLiteTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(NotificationLiteTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement complete;

            public org.junit.runners.model.Statement valueKind;

            public org.junit.runners.model.Statement soloEnum;

            public org.junit.runners.model.Statement errorNotification;

            public org.junit.runners.model.Statement completeNotification;

            public org.junit.runners.model.Statement disposableNotification;

            public org.junit.runners.model.Statement subscriptionNotification;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.complete = _ClassStatement.forPayload(NotificationLiteTest::complete, "complete", this);
            this.payloads.valueKind = _ClassStatement.forPayload(NotificationLiteTest::valueKind, "valueKind", this);
            this.payloads.soloEnum = _ClassStatement.forPayload(NotificationLiteTest::soloEnum, "soloEnum", this);
            this.payloads.errorNotification = _ClassStatement.forPayload(NotificationLiteTest::errorNotification, "errorNotification", this);
            this.payloads.completeNotification = _ClassStatement.forPayload(NotificationLiteTest::completeNotification, "completeNotification", this);
            this.payloads.disposableNotification = _ClassStatement.forPayload(NotificationLiteTest::disposableNotification, "disposableNotification", this);
            this.payloads.subscriptionNotification = _ClassStatement.forPayload(NotificationLiteTest::subscriptionNotification, "subscriptionNotification", this);
        }
    }
    // TODO this test is no longer relevant as nulls are not allowed and value maps to itself
    // @Test
    // public void testValueKind() {
    // assertTrue(NotificationLite.isNull(NotificationLite.next(null)));
    // assertFalse(NotificationLite.isNull(NotificationLite.next(1)));
    // assertFalse(NotificationLite.isNull(NotificationLite.error(new TestException())));
    // assertFalse(NotificationLite.isNull(NotificationLite.completed()));
    // assertFalse(NotificationLite.isNull(null));
    // 
    // assertTrue(NotificationLite.isNext(NotificationLite.next(null)));
    // assertTrue(NotificationLite.isNext(NotificationLite.next(1)));
    // assertFalse(NotificationLite.isNext(NotificationLite.completed()));
    // assertFalse(NotificationLite.isNext(null));
    // assertFalse(NotificationLite.isNext(NotificationLite.error(new TestException())));
    // }
}
