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
package io.reactivex.rxjava3.flowable;

import org.junit.*;
import io.reactivex.rxjava3.core.*;

public class FlowableNotificationTest extends RxJavaTest {

    @Test(expected = NullPointerException.class)
    public void onNextIntegerNotificationDoesNotEqualNullNotification() {
        final Notification<Integer> integerNotification = Notification.createOnNext(1);
        final Notification<Integer> nullNotification = Notification.createOnNext(null);
        Assert.assertNotEquals(integerNotification, nullNotification);
    }

    @Test(expected = NullPointerException.class)
    public void onNextNullNotificationDoesNotEqualIntegerNotification() {
        final Notification<Integer> integerNotification = Notification.createOnNext(1);
        final Notification<Integer> nullNotification = Notification.createOnNext(null);
        Assert.assertNotEquals(nullNotification, integerNotification);
    }

    @Test
    public void onNextIntegerNotificationsWhenEqual() {
        final Notification<Integer> integerNotification = Notification.createOnNext(1);
        final Notification<Integer> integerNotification2 = Notification.createOnNext(1);
        Assert.assertEquals(integerNotification, integerNotification2);
    }

    @Test
    public void onNextIntegerNotificationsWhenNotEqual() {
        final Notification<Integer> integerNotification = Notification.createOnNext(1);
        final Notification<Integer> integerNotification2 = Notification.createOnNext(2);
        Assert.assertNotEquals(integerNotification, integerNotification2);
    }

    @Test
    public void onErrorIntegerNotificationsWhenEqual() {
        final Exception exception = new Exception();
        final Notification<Integer> onErrorNotification = Notification.createOnError(exception);
        final Notification<Integer> onErrorNotification2 = Notification.createOnError(exception);
        Assert.assertEquals(onErrorNotification, onErrorNotification2);
    }

    @Test
    public void onErrorIntegerNotificationWhenNotEqual() {
        final Notification<Integer> onErrorNotification = Notification.createOnError(new Exception());
        final Notification<Integer> onErrorNotification2 = Notification.createOnError(new Exception());
        Assert.assertNotEquals(onErrorNotification, onErrorNotification2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableNotificationTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextIntegerNotificationDoesNotEqualNullNotification() throws java.lang.Throwable {
            this.payloads.onNextIntegerNotificationDoesNotEqualNullNotification.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextNullNotificationDoesNotEqualIntegerNotification() throws java.lang.Throwable {
            this.payloads.onNextNullNotificationDoesNotEqualIntegerNotification.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextIntegerNotificationsWhenEqual() throws java.lang.Throwable {
            this.payloads.onNextIntegerNotificationsWhenEqual.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextIntegerNotificationsWhenNotEqual() throws java.lang.Throwable {
            this.payloads.onNextIntegerNotificationsWhenNotEqual.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorIntegerNotificationsWhenEqual() throws java.lang.Throwable {
            this.payloads.onErrorIntegerNotificationsWhenEqual.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorIntegerNotificationWhenNotEqual() throws java.lang.Throwable {
            this.payloads.onErrorIntegerNotificationWhenNotEqual.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableNotificationTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableNotificationTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableNotificationTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableNotificationTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableNotificationTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableNotificationTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableNotificationTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableNotificationTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement onNextIntegerNotificationDoesNotEqualNullNotification;

            public org.junit.runners.model.Statement onNextNullNotificationDoesNotEqualIntegerNotification;

            public org.junit.runners.model.Statement onNextIntegerNotificationsWhenEqual;

            public org.junit.runners.model.Statement onNextIntegerNotificationsWhenNotEqual;

            public org.junit.runners.model.Statement onErrorIntegerNotificationsWhenEqual;

            public org.junit.runners.model.Statement onErrorIntegerNotificationWhenNotEqual;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.onNextIntegerNotificationDoesNotEqualNullNotification = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNotificationTest::onNextIntegerNotificationDoesNotEqualNullNotification, java.lang.NullPointerException.class), "onNextIntegerNotificationDoesNotEqualNullNotification", this);
            this.payloads.onNextNullNotificationDoesNotEqualIntegerNotification = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableNotificationTest::onNextNullNotificationDoesNotEqualIntegerNotification, java.lang.NullPointerException.class), "onNextNullNotificationDoesNotEqualIntegerNotification", this);
            this.payloads.onNextIntegerNotificationsWhenEqual = _ClassStatement.forPayload(FlowableNotificationTest::onNextIntegerNotificationsWhenEqual, "onNextIntegerNotificationsWhenEqual", this);
            this.payloads.onNextIntegerNotificationsWhenNotEqual = _ClassStatement.forPayload(FlowableNotificationTest::onNextIntegerNotificationsWhenNotEqual, "onNextIntegerNotificationsWhenNotEqual", this);
            this.payloads.onErrorIntegerNotificationsWhenEqual = _ClassStatement.forPayload(FlowableNotificationTest::onErrorIntegerNotificationsWhenEqual, "onErrorIntegerNotificationsWhenEqual", this);
            this.payloads.onErrorIntegerNotificationWhenNotEqual = _ClassStatement.forPayload(FlowableNotificationTest::onErrorIntegerNotificationWhenNotEqual, "onErrorIntegerNotificationWhenNotEqual", this);
        }
    }
}
