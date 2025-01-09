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
package io.reactivex.rxjava3.internal.subscribers;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.testsupport.TestSubscriberEx;

public class StrictSubscriberTest extends RxJavaTest {

    @Test
    public void strictMode() {
        final List<Object> list = new ArrayList<>();
        Subscriber<Object> sub = new Subscriber<Object>() {

            @Override
            public void onSubscribe(Subscription s) {
                s.request(10);
            }

            @Override
            public void onNext(Object t) {
                list.add(t);
            }

            @Override
            public void onError(Throwable t) {
                list.add(t);
            }

            @Override
            public void onComplete() {
                list.add("Done");
            }
        };
        new Flowable<Object>() {

            @Override
            protected void subscribeActual(Subscriber<? super Object> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onNext(s);
            }
        }.subscribe(sub);
        assertTrue(list.toString(), list.get(0) instanceof StrictSubscriber);
    }

    static final class SubscriberWrapper<T> implements Subscriber<T> {

        final TestSubscriberEx<T> tester;

        SubscriberWrapper(TestSubscriberEx<T> tester) {
            this.tester = tester;
        }

        @Override
        public void onSubscribe(Subscription s) {
            tester.onSubscribe(s);
        }

        @Override
        public void onNext(T t) {
            tester.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            tester.onError(t);
        }

        @Override
        public void onComplete() {
            tester.onComplete();
        }
    }

    @Test
    public void normalOnNext() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        SubscriberWrapper<Integer> wrapper = new SubscriberWrapper<>(ts);
        Flowable.range(1, 5).subscribe(wrapper);
        ts.assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void normalOnNextBackpressured() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>(0);
        SubscriberWrapper<Integer> wrapper = new SubscriberWrapper<>(ts);
        Flowable.range(1, 5).subscribe(wrapper);
        ts.assertEmpty().requestMore(1).assertValue(1).requestMore(2).assertValues(1, 2, 3).requestMore(2).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void normalOnError() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        SubscriberWrapper<Integer> wrapper = new SubscriberWrapper<>(ts);
        Flowable.range(1, 5).concatWith(Flowable.<Integer>error(new TestException())).subscribe(wrapper);
        ts.assertFailure(TestException.class, 1, 2, 3, 4, 5);
    }

    @Test
    public void deferredRequest() {
        final List<Object> list = new ArrayList<>();
        Subscriber<Object> sub = new Subscriber<Object>() {

            @Override
            public void onSubscribe(Subscription s) {
                s.request(5);
                list.add(0);
            }

            @Override
            public void onNext(Object t) {
                list.add(t);
            }

            @Override
            public void onError(Throwable t) {
                list.add(t);
            }

            @Override
            public void onComplete() {
                list.add("Done");
            }
        };
        Flowable.range(1, 5).subscribe(sub);
        assertEquals(Arrays.<Object>asList(0, 1, 2, 3, 4, 5, "Done"), list);
    }

    @Test
    public void requestZero() {
        final List<Object> list = new ArrayList<>();
        Subscriber<Object> sub = new Subscriber<Object>() {

            @Override
            public void onSubscribe(Subscription s) {
                s.request(0);
            }

            @Override
            public void onNext(Object t) {
                list.add(t);
            }

            @Override
            public void onError(Throwable t) {
                list.add(t);
            }

            @Override
            public void onComplete() {
                list.add("Done");
            }
        };
        Flowable.range(1, 5).subscribe(sub);
        assertTrue(list.toString(), list.get(0) instanceof IllegalArgumentException);
        assertTrue(list.toString(), list.get(0).toString().contains("3.9"));
    }

    @Test
    public void requestNegative() {
        final List<Object> list = new ArrayList<>();
        Subscriber<Object> sub = new Subscriber<Object>() {

            @Override
            public void onSubscribe(Subscription s) {
                s.request(-99);
            }

            @Override
            public void onNext(Object t) {
                list.add(t);
            }

            @Override
            public void onError(Throwable t) {
                list.add(t);
            }

            @Override
            public void onComplete() {
                list.add("Done");
            }
        };
        Flowable.range(1, 5).subscribe(sub);
        assertTrue(list.toString(), list.get(0) instanceof IllegalArgumentException);
        assertTrue(list.toString(), list.get(0).toString().contains("3.9"));
    }

    @Test
    public void cancelAfterOnComplete() {
        final List<Object> list = new ArrayList<>();
        Subscriber<Object> sub = new Subscriber<Object>() {

            Subscription upstream;

            @Override
            public void onSubscribe(Subscription s) {
                this.upstream = s;
            }

            @Override
            public void onNext(Object t) {
                list.add(t);
            }

            @Override
            public void onError(Throwable t) {
                upstream.cancel();
                list.add(t);
            }

            @Override
            public void onComplete() {
                upstream.cancel();
                list.add("Done");
            }
        };
        new Flowable<Object>() {

            @Override
            protected void subscribeActual(Subscriber<? super Object> s) {
                BooleanSubscription b = new BooleanSubscription();
                s.onSubscribe(b);
                s.onComplete();
                list.add(b.isCancelled());
            }
        }.subscribe(sub);
        assertEquals(Arrays.<Object>asList("Done", false), list);
    }

    @Test
    public void cancelAfterOnError() {
        final List<Object> list = new ArrayList<>();
        Subscriber<Object> sub = new Subscriber<Object>() {

            Subscription upstream;

            @Override
            public void onSubscribe(Subscription s) {
                this.upstream = s;
            }

            @Override
            public void onNext(Object t) {
                list.add(t);
            }

            @Override
            public void onError(Throwable t) {
                upstream.cancel();
                list.add(t.getMessage());
            }

            @Override
            public void onComplete() {
                upstream.cancel();
                list.add("Done");
            }
        };
        new Flowable<Object>() {

            @Override
            protected void subscribeActual(Subscriber<? super Object> s) {
                BooleanSubscription b = new BooleanSubscription();
                s.onSubscribe(b);
                s.onError(new TestException("Forced failure"));
                list.add(b.isCancelled());
            }
        }.subscribe(sub);
        assertEquals(Arrays.<Object>asList("Forced failure", false), list);
    }

    @Test
    public void doubleOnSubscribe() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        SubscriberWrapper<Integer> wrapper = new SubscriberWrapper<>(ts);
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                BooleanSubscription b1 = new BooleanSubscription();
                s.onSubscribe(b1);
                BooleanSubscription b2 = new BooleanSubscription();
                s.onSubscribe(b2);
                assertTrue(b1.isCancelled());
                assertTrue(b2.isCancelled());
            }
        }.subscribe(wrapper);
        ts.assertFailure(IllegalStateException.class);
        assertTrue(ts.errors().toString(), ts.errors().get(0).getMessage().contains("2.12"));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public StrictSubscriberTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_strictMode() throws java.lang.Throwable {
            this.payloads.strictMode.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalOnNext() throws java.lang.Throwable {
            this.payloads.normalOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalOnNextBackpressured() throws java.lang.Throwable {
            this.payloads.normalOnNextBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalOnError() throws java.lang.Throwable {
            this.payloads.normalOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferredRequest() throws java.lang.Throwable {
            this.payloads.deferredRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestZero() throws java.lang.Throwable {
            this.payloads.requestZero.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestNegative() throws java.lang.Throwable {
            this.payloads.requestNegative.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterOnComplete() throws java.lang.Throwable {
            this.payloads.cancelAfterOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterOnError() throws java.lang.Throwable {
            this.payloads.cancelAfterOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<StrictSubscriberTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<StrictSubscriberTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<StrictSubscriberTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<StrictSubscriberTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new StrictSubscriberTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<StrictSubscriberTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(StrictSubscriberTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(StrictSubscriberTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement strictMode;

            public org.junit.runners.model.Statement normalOnNext;

            public org.junit.runners.model.Statement normalOnNextBackpressured;

            public org.junit.runners.model.Statement normalOnError;

            public org.junit.runners.model.Statement deferredRequest;

            public org.junit.runners.model.Statement requestZero;

            public org.junit.runners.model.Statement requestNegative;

            public org.junit.runners.model.Statement cancelAfterOnComplete;

            public org.junit.runners.model.Statement cancelAfterOnError;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.strictMode = _ClassStatement.forPayload(StrictSubscriberTest::strictMode, "strictMode", this);
            this.payloads.normalOnNext = _ClassStatement.forPayload(StrictSubscriberTest::normalOnNext, "normalOnNext", this);
            this.payloads.normalOnNextBackpressured = _ClassStatement.forPayload(StrictSubscriberTest::normalOnNextBackpressured, "normalOnNextBackpressured", this);
            this.payloads.normalOnError = _ClassStatement.forPayload(StrictSubscriberTest::normalOnError, "normalOnError", this);
            this.payloads.deferredRequest = _ClassStatement.forPayload(StrictSubscriberTest::deferredRequest, "deferredRequest", this);
            this.payloads.requestZero = _ClassStatement.forPayload(StrictSubscriberTest::requestZero, "requestZero", this);
            this.payloads.requestNegative = _ClassStatement.forPayload(StrictSubscriberTest::requestNegative, "requestNegative", this);
            this.payloads.cancelAfterOnComplete = _ClassStatement.forPayload(StrictSubscriberTest::cancelAfterOnComplete, "cancelAfterOnComplete", this);
            this.payloads.cancelAfterOnError = _ClassStatement.forPayload(StrictSubscriberTest::cancelAfterOnError, "cancelAfterOnError", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(StrictSubscriberTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
