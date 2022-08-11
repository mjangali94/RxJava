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

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableDetachTest extends RxJavaTest {

    Object o;

    @Test
    public void just() throws Exception {
        o = new Object();
        WeakReference<Object> wr = new WeakReference<>(o);
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.just(o).count().toFlowable().onTerminateDetach().subscribe(ts);
        ts.assertValue(1L);
        ts.assertComplete();
        ts.assertNoErrors();
        o = null;
        System.gc();
        Thread.sleep(200);
        Assert.assertNull("Object retained!", wr.get());
    }

    @Test
    public void error() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.error(new TestException()).onTerminateDetach().subscribe(ts);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void empty() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.empty().onTerminateDetach().subscribe(ts);
        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void range() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        Flowable.range(1, 1000).onTerminateDetach().subscribe(ts);
        ts.assertValueCount(1000);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void backpressured() throws Exception {
        o = new Object();
        WeakReference<Object> wr = new WeakReference<>(o);
        TestSubscriber<Object> ts = new TestSubscriber<>(0L);
        Flowable.just(o).count().toFlowable().onTerminateDetach().subscribe(ts);
        ts.assertNoValues();
        ts.request(1);
        ts.assertValue(1L);
        ts.assertComplete();
        ts.assertNoErrors();
        o = null;
        System.gc();
        Thread.sleep(200);
        Assert.assertNull("Object retained!", wr.get());
    }

    @Test
    public void justUnsubscribed() throws Exception {
        o = new Object();
        WeakReference<Object> wr = new WeakReference<>(o);
        TestSubscriber<Object> ts = new TestSubscriber<>(0);
        Flowable.just(o).count().toFlowable().onTerminateDetach().subscribe(ts);
        ts.cancel();
        o = null;
        System.gc();
        Thread.sleep(200);
        Assert.assertNull("Object retained!", wr.get());
    }

    @Test
    public void deferredUpstreamProducer() {
        final AtomicReference<Subscriber<? super Object>> subscriber = new AtomicReference<>();
        TestSubscriber<Object> ts = new TestSubscriber<>(0);
        Flowable.unsafeCreate(new Publisher<Object>() {

            @Override
            public void subscribe(Subscriber<? super Object> t) {
                subscriber.set(t);
            }
        }).onTerminateDetach().subscribe(ts);
        ts.request(2);
        new FlowableRange(1, 3).subscribe(subscriber.get());
        ts.assertValues(1, 2);
        ts.request(1);
        ts.assertValues(1, 2, 3);
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.never().onTerminateDetach());
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.onTerminateDetach();
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableDetachTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.payloads.just.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_range() throws java.lang.Throwable {
            this.payloads.range.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressured() throws java.lang.Throwable {
            this.payloads.backpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justUnsubscribed() throws java.lang.Throwable {
            this.payloads.justUnsubscribed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferredUpstreamProducer() throws java.lang.Throwable {
            this.payloads.deferredUpstreamProducer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDetachTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDetachTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDetachTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDetachTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableDetachTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDetachTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableDetachTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableDetachTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement just;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement range;

            public org.junit.runners.model.Statement backpressured;

            public org.junit.runners.model.Statement justUnsubscribed;

            public org.junit.runners.model.Statement deferredUpstreamProducer;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.just = _ClassStatement.forPayload(FlowableDetachTest::just, "just", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableDetachTest::error, "error", this);
            this.payloads.empty = _ClassStatement.forPayload(FlowableDetachTest::empty, "empty", this);
            this.payloads.range = _ClassStatement.forPayload(FlowableDetachTest::range, "range", this);
            this.payloads.backpressured = _ClassStatement.forPayload(FlowableDetachTest::backpressured, "backpressured", this);
            this.payloads.justUnsubscribed = _ClassStatement.forPayload(FlowableDetachTest::justUnsubscribed, "justUnsubscribed", this);
            this.payloads.deferredUpstreamProducer = _ClassStatement.forPayload(FlowableDetachTest::deferredUpstreamProducer, "deferredUpstreamProducer", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableDetachTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableDetachTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
