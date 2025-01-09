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

import static org.junit.Assert.*;
import java.util.List;
import org.junit.*;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.ProtocolViolationException;
import io.reactivex.rxjava3.internal.disposables.DisposableHelper;
import io.reactivex.rxjava3.internal.subscriptions.*;
import io.reactivex.rxjava3.observers.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subscribers.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class EndConsumerHelperTest extends RxJavaTest {

    List<Throwable> errors;

    @Before
    public void before() {
        errors = TestHelper.trackPluginErrors();
    }

    @After
    public void after() {
        RxJavaPlugins.reset();
    }

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(EndConsumerHelper.class);
    }

    @Test
    public void checkDoubleDefaultSubscriber() {
        Subscriber<Integer> consumer = new DefaultSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        BooleanSubscription sub1 = new BooleanSubscription();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isCancelled());
        BooleanSubscription sub2 = new BooleanSubscription();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isCancelled());
        assertTrue(sub2.isCancelled());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    static final class EndDefaultSubscriber extends DefaultSubscriber<Integer> {

        @Override
        public void onNext(Integer t) {
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onComplete() {
        }
    }

    @Test
    public void checkDoubleDefaultSubscriberNonAnonymous() {
        Subscriber<Integer> consumer = new EndDefaultSubscriber();
        BooleanSubscription sub1 = new BooleanSubscription();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isCancelled());
        BooleanSubscription sub2 = new BooleanSubscription();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isCancelled());
        assertTrue(sub2.isCancelled());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        // with this consumer, the class name should be predictable
        assertEquals(EndConsumerHelper.composeMessage("io.reactivex.rxjava3.internal.util.EndConsumerHelperTest$EndDefaultSubscriber"), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableSubscriber() {
        Subscriber<Integer> consumer = new DisposableSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        BooleanSubscription sub1 = new BooleanSubscription();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isCancelled());
        BooleanSubscription sub2 = new BooleanSubscription();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isCancelled());
        assertTrue(sub2.isCancelled());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceSubscriber() {
        Subscriber<Integer> consumer = new ResourceSubscriber<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        BooleanSubscription sub1 = new BooleanSubscription();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isCancelled());
        BooleanSubscription sub2 = new BooleanSubscription();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isCancelled());
        assertTrue(sub2.isCancelled());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDefaultObserver() {
        Observer<Integer> consumer = new DefaultObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableObserver() {
        Observer<Integer> consumer = new DisposableObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceObserver() {
        Observer<Integer> consumer = new ResourceObserver<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableSingleObserver() {
        SingleObserver<Integer> consumer = new DisposableSingleObserver<Integer>() {

            @Override
            public void onSuccess(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceSingleObserver() {
        SingleObserver<Integer> consumer = new ResourceSingleObserver<Integer>() {

            @Override
            public void onSuccess(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableMaybeObserver() {
        MaybeObserver<Integer> consumer = new DisposableMaybeObserver<Integer>() {

            @Override
            public void onSuccess(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceMaybeObserver() {
        MaybeObserver<Integer> consumer = new ResourceMaybeObserver<Integer>() {

            @Override
            public void onSuccess(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleDisposableCompletableObserver() {
        CompletableObserver consumer = new DisposableCompletableObserver() {

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void checkDoubleResourceCompletableObserver() {
        CompletableObserver consumer = new ResourceCompletableObserver() {

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        };
        Disposable sub1 = Disposable.empty();
        consumer.onSubscribe(sub1);
        assertFalse(sub1.isDisposed());
        Disposable sub2 = Disposable.empty();
        consumer.onSubscribe(sub2);
        assertFalse(sub1.isDisposed());
        assertTrue(sub2.isDisposed());
        TestHelper.assertError(errors, 0, ProtocolViolationException.class);
        assertEquals(EndConsumerHelper.composeMessage(consumer.getClass().getName()), errors.get(0).getMessage());
        assertEquals(errors.toString(), 1, errors.size());
    }

    @Test
    public void validateDisposable() {
        Disposable d1 = Disposable.empty();
        assertFalse(EndConsumerHelper.validate(DisposableHelper.DISPOSED, d1, getClass()));
        assertTrue(d1.isDisposed());
        assertTrue(errors.toString(), errors.isEmpty());
    }

    @Test
    public void validateSubscription() {
        BooleanSubscription bs1 = new BooleanSubscription();
        assertFalse(EndConsumerHelper.validate(SubscriptionHelper.CANCELLED, bs1, getClass()));
        assertTrue(bs1.isCancelled());
        assertTrue(errors.toString(), errors.isEmpty());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public EndConsumerHelperTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_utilityClass() throws java.lang.Throwable {
            this.payloads.utilityClass.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDefaultSubscriber() throws java.lang.Throwable {
            this.payloads.checkDoubleDefaultSubscriber.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDefaultSubscriberNonAnonymous() throws java.lang.Throwable {
            this.payloads.checkDoubleDefaultSubscriberNonAnonymous.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDisposableSubscriber() throws java.lang.Throwable {
            this.payloads.checkDoubleDisposableSubscriber.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleResourceSubscriber() throws java.lang.Throwable {
            this.payloads.checkDoubleResourceSubscriber.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDefaultObserver() throws java.lang.Throwable {
            this.payloads.checkDoubleDefaultObserver.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDisposableObserver() throws java.lang.Throwable {
            this.payloads.checkDoubleDisposableObserver.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleResourceObserver() throws java.lang.Throwable {
            this.payloads.checkDoubleResourceObserver.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDisposableSingleObserver() throws java.lang.Throwable {
            this.payloads.checkDoubleDisposableSingleObserver.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleResourceSingleObserver() throws java.lang.Throwable {
            this.payloads.checkDoubleResourceSingleObserver.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDisposableMaybeObserver() throws java.lang.Throwable {
            this.payloads.checkDoubleDisposableMaybeObserver.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleResourceMaybeObserver() throws java.lang.Throwable {
            this.payloads.checkDoubleResourceMaybeObserver.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleDisposableCompletableObserver() throws java.lang.Throwable {
            this.payloads.checkDoubleDisposableCompletableObserver.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_checkDoubleResourceCompletableObserver() throws java.lang.Throwable {
            this.payloads.checkDoubleResourceCompletableObserver.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_validateDisposable() throws java.lang.Throwable {
            this.payloads.validateDisposable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_validateSubscription() throws java.lang.Throwable {
            this.payloads.validateSubscription.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<EndConsumerHelperTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<EndConsumerHelperTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.before();
                try {
                    this.payload.accept(this.benchmark.instance);
                } finally {
                    this.benchmark.instance.after();
                }
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<EndConsumerHelperTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<EndConsumerHelperTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new EndConsumerHelperTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<EndConsumerHelperTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(EndConsumerHelperTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(EndConsumerHelperTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement utilityClass;

            public org.junit.runners.model.Statement checkDoubleDefaultSubscriber;

            public org.junit.runners.model.Statement checkDoubleDefaultSubscriberNonAnonymous;

            public org.junit.runners.model.Statement checkDoubleDisposableSubscriber;

            public org.junit.runners.model.Statement checkDoubleResourceSubscriber;

            public org.junit.runners.model.Statement checkDoubleDefaultObserver;

            public org.junit.runners.model.Statement checkDoubleDisposableObserver;

            public org.junit.runners.model.Statement checkDoubleResourceObserver;

            public org.junit.runners.model.Statement checkDoubleDisposableSingleObserver;

            public org.junit.runners.model.Statement checkDoubleResourceSingleObserver;

            public org.junit.runners.model.Statement checkDoubleDisposableMaybeObserver;

            public org.junit.runners.model.Statement checkDoubleResourceMaybeObserver;

            public org.junit.runners.model.Statement checkDoubleDisposableCompletableObserver;

            public org.junit.runners.model.Statement checkDoubleResourceCompletableObserver;

            public org.junit.runners.model.Statement validateDisposable;

            public org.junit.runners.model.Statement validateSubscription;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.utilityClass = _ClassStatement.forPayload(EndConsumerHelperTest::utilityClass, "utilityClass", this);
            this.payloads.checkDoubleDefaultSubscriber = _ClassStatement.forPayload(EndConsumerHelperTest::checkDoubleDefaultSubscriber, "checkDoubleDefaultSubscriber", this);
            this.payloads.checkDoubleDefaultSubscriberNonAnonymous = _ClassStatement.forPayload(EndConsumerHelperTest::checkDoubleDefaultSubscriberNonAnonymous, "checkDoubleDefaultSubscriberNonAnonymous", this);
            this.payloads.checkDoubleDisposableSubscriber = _ClassStatement.forPayload(EndConsumerHelperTest::checkDoubleDisposableSubscriber, "checkDoubleDisposableSubscriber", this);
            this.payloads.checkDoubleResourceSubscriber = _ClassStatement.forPayload(EndConsumerHelperTest::checkDoubleResourceSubscriber, "checkDoubleResourceSubscriber", this);
            this.payloads.checkDoubleDefaultObserver = _ClassStatement.forPayload(EndConsumerHelperTest::checkDoubleDefaultObserver, "checkDoubleDefaultObserver", this);
            this.payloads.checkDoubleDisposableObserver = _ClassStatement.forPayload(EndConsumerHelperTest::checkDoubleDisposableObserver, "checkDoubleDisposableObserver", this);
            this.payloads.checkDoubleResourceObserver = _ClassStatement.forPayload(EndConsumerHelperTest::checkDoubleResourceObserver, "checkDoubleResourceObserver", this);
            this.payloads.checkDoubleDisposableSingleObserver = _ClassStatement.forPayload(EndConsumerHelperTest::checkDoubleDisposableSingleObserver, "checkDoubleDisposableSingleObserver", this);
            this.payloads.checkDoubleResourceSingleObserver = _ClassStatement.forPayload(EndConsumerHelperTest::checkDoubleResourceSingleObserver, "checkDoubleResourceSingleObserver", this);
            this.payloads.checkDoubleDisposableMaybeObserver = _ClassStatement.forPayload(EndConsumerHelperTest::checkDoubleDisposableMaybeObserver, "checkDoubleDisposableMaybeObserver", this);
            this.payloads.checkDoubleResourceMaybeObserver = _ClassStatement.forPayload(EndConsumerHelperTest::checkDoubleResourceMaybeObserver, "checkDoubleResourceMaybeObserver", this);
            this.payloads.checkDoubleDisposableCompletableObserver = _ClassStatement.forPayload(EndConsumerHelperTest::checkDoubleDisposableCompletableObserver, "checkDoubleDisposableCompletableObserver", this);
            this.payloads.checkDoubleResourceCompletableObserver = _ClassStatement.forPayload(EndConsumerHelperTest::checkDoubleResourceCompletableObserver, "checkDoubleResourceCompletableObserver", this);
            this.payloads.validateDisposable = _ClassStatement.forPayload(EndConsumerHelperTest::validateDisposable, "validateDisposable", this);
            this.payloads.validateSubscription = _ClassStatement.forPayload(EndConsumerHelperTest::validateSubscription, "validateSubscription", this);
        }
    }
}
