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
package io.reactivex.rxjava3.core;

import static org.junit.Assert.*;
import org.junit.Test;
import io.reactivex.rxjava3.exceptions.TestException;

public class NotificationTest extends RxJavaTest {

    @Test
    public void valueOfOnErrorIsNull() {
        Notification<Integer> notification = Notification.createOnError(new TestException());
        assertNull(notification.getValue());
        assertTrue(notification.getError().toString(), notification.getError() instanceof TestException);
    }

    @Test
    public void valueOfOnCompleteIsNull() {
        Notification<Integer> notification = Notification.createOnComplete();
        assertNull(notification.getValue());
        assertNull(notification.getError());
        assertTrue(notification.isOnComplete());
    }

    @Test
    public void notEqualsToObject() {
        Notification<Integer> n1 = Notification.createOnNext(0);
        assertNotEquals(0, n1);
        assertNotEquals(n1, 0);
        Notification<Integer> n2 = Notification.createOnError(new TestException());
        assertNotEquals(0, n2);
        assertNotEquals(n2, 0);
        Notification<Integer> n3 = Notification.createOnComplete();
        assertNotEquals(0, n3);
        assertNotEquals(n3, 0);
    }

    @Test
    public void twoEqual() {
        Notification<Integer> n1 = Notification.createOnNext(0);
        Notification<Integer> n2 = Notification.createOnNext(0);
        assertEquals(n1, n2);
        assertEquals(n2, n1);
    }

    @Test
    public void hashCodeIsTheInner() {
        Notification<Integer> n1 = Notification.createOnNext(1337);
        assertEquals(Integer.valueOf(1337).hashCode(), n1.hashCode());
        assertEquals(0, Notification.createOnComplete().hashCode());
    }

    @Test
    public void toStringPattern() {
        assertEquals("OnNextNotification[1]", Notification.createOnNext(1).toString());
        assertEquals("OnErrorNotification[io.reactivex.rxjava3.exceptions.TestException]", Notification.createOnError(new TestException()).toString());
        assertEquals("OnCompleteNotification", Notification.createOnComplete().toString());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public NotificationTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_valueOfOnErrorIsNull() throws java.lang.Throwable {
            this.payloads.valueOfOnErrorIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_valueOfOnCompleteIsNull() throws java.lang.Throwable {
            this.payloads.valueOfOnCompleteIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_notEqualsToObject() throws java.lang.Throwable {
            this.payloads.notEqualsToObject.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoEqual() throws java.lang.Throwable {
            this.payloads.twoEqual.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hashCodeIsTheInner() throws java.lang.Throwable {
            this.payloads.hashCodeIsTheInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toStringPattern() throws java.lang.Throwable {
            this.payloads.toStringPattern.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<NotificationTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<NotificationTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<NotificationTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<NotificationTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new NotificationTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<NotificationTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(NotificationTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(NotificationTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement valueOfOnErrorIsNull;

            public org.junit.runners.model.Statement valueOfOnCompleteIsNull;

            public org.junit.runners.model.Statement notEqualsToObject;

            public org.junit.runners.model.Statement twoEqual;

            public org.junit.runners.model.Statement hashCodeIsTheInner;

            public org.junit.runners.model.Statement toStringPattern;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.valueOfOnErrorIsNull = _ClassStatement.forPayload(NotificationTest::valueOfOnErrorIsNull, "valueOfOnErrorIsNull", this);
            this.payloads.valueOfOnCompleteIsNull = _ClassStatement.forPayload(NotificationTest::valueOfOnCompleteIsNull, "valueOfOnCompleteIsNull", this);
            this.payloads.notEqualsToObject = _ClassStatement.forPayload(NotificationTest::notEqualsToObject, "notEqualsToObject", this);
            this.payloads.twoEqual = _ClassStatement.forPayload(NotificationTest::twoEqual, "twoEqual", this);
            this.payloads.hashCodeIsTheInner = _ClassStatement.forPayload(NotificationTest::hashCodeIsTheInner, "hashCodeIsTheInner", this);
            this.payloads.toStringPattern = _ClassStatement.forPayload(NotificationTest::toStringPattern, "toStringPattern", this);
        }
    }
}
