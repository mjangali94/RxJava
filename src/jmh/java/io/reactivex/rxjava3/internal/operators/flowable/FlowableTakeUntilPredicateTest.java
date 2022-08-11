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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableTakeUntilPredicateTest extends RxJavaTest {

    @Test
    public void takeEmpty() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        Flowable.empty().takeUntil(new Predicate<Object>() {

            @Override
            public boolean test(Object v) {
                return true;
            }
        }).subscribe(subscriber);
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber).onComplete();
    }

    @Test
    public void takeAll() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        Flowable.just(1, 2).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return false;
            }
        }).subscribe(subscriber);
        verify(subscriber).onNext(1);
        verify(subscriber).onNext(2);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber).onComplete();
    }

    @Test
    public void takeFirst() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        Flowable.just(1, 2).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).subscribe(subscriber);
        verify(subscriber).onNext(1);
        verify(subscriber, never()).onNext(2);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber).onComplete();
    }

    @Test
    public void takeSome() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        Flowable.just(1, 2, 3).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                return t1 == 2;
            }
        }).subscribe(subscriber);
        verify(subscriber).onNext(1);
        verify(subscriber).onNext(2);
        verify(subscriber, never()).onNext(3);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber).onComplete();
    }

    @Test
    public void functionThrows() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        Predicate<Integer> predicate = new Predicate<Integer>() {

            @Override
            public boolean test(Integer t1) {
                throw new TestException("Forced failure");
            }
        };
        Flowable.just(1, 2, 3).takeUntil(predicate).subscribe(subscriber);
        verify(subscriber).onNext(1);
        verify(subscriber, never()).onNext(2);
        verify(subscriber, never()).onNext(3);
        verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void sourceThrows() {
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).concatWith(Flowable.just(2)).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return false;
            }
        }).subscribe(subscriber);
        verify(subscriber).onNext(1);
        verify(subscriber, never()).onNext(2);
        verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void backpressure() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(5L);
        Flowable.range(1, 1000).takeUntil(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return false;
            }
        }).subscribe(ts);
        ts.assertNoErrors();
        ts.assertValues(1, 2, 3, 4, 5);
        ts.assertNotComplete();
    }

    @Test
    public void errorIncludesLastValueAsCause() {
        TestSubscriberEx<String> ts = new TestSubscriberEx<>();
        final TestException e = new TestException("Forced failure");
        Predicate<String> predicate = new Predicate<String>() {

            @Override
            public boolean test(String t) {
                throw e;
            }
        };
        Flowable.just("abc").takeUntil(predicate).subscribe(ts);
        ts.assertTerminated();
        ts.assertNotComplete();
        ts.assertError(TestException.class);
    // FIXME last cause value is not saved
    // assertTrue(ts.errors().get(0).getCause().getMessage().contains("abc"));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().takeUntil(Functions.alwaysFalse()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.takeUntil(Functions.alwaysFalse());
            }
        });
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onComplete();
                    subscriber.onNext(1);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                }
            }.takeUntil(Functions.alwaysFalse()).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableTakeUntilPredicateTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeEmpty() throws java.lang.Throwable {
            this.payloads.takeEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeAll() throws java.lang.Throwable {
            this.payloads.takeAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeFirst() throws java.lang.Throwable {
            this.payloads.takeFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeSome() throws java.lang.Throwable {
            this.payloads.takeSome.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_functionThrows() throws java.lang.Throwable {
            this.payloads.functionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceThrows() throws java.lang.Throwable {
            this.payloads.sourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.payloads.backpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorIncludesLastValueAsCause() throws java.lang.Throwable {
            this.payloads.errorIncludesLastValueAsCause.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeUntilPredicateTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeUntilPredicateTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeUntilPredicateTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeUntilPredicateTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableTakeUntilPredicateTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableTakeUntilPredicateTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableTakeUntilPredicateTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableTakeUntilPredicateTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement takeEmpty;

            public org.junit.runners.model.Statement takeAll;

            public org.junit.runners.model.Statement takeFirst;

            public org.junit.runners.model.Statement takeSome;

            public org.junit.runners.model.Statement functionThrows;

            public org.junit.runners.model.Statement sourceThrows;

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement errorIncludesLastValueAsCause;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement badSource;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.takeEmpty = _ClassStatement.forPayload(FlowableTakeUntilPredicateTest::takeEmpty, "takeEmpty", this);
            this.payloads.takeAll = _ClassStatement.forPayload(FlowableTakeUntilPredicateTest::takeAll, "takeAll", this);
            this.payloads.takeFirst = _ClassStatement.forPayload(FlowableTakeUntilPredicateTest::takeFirst, "takeFirst", this);
            this.payloads.takeSome = _ClassStatement.forPayload(FlowableTakeUntilPredicateTest::takeSome, "takeSome", this);
            this.payloads.functionThrows = _ClassStatement.forPayload(FlowableTakeUntilPredicateTest::functionThrows, "functionThrows", this);
            this.payloads.sourceThrows = _ClassStatement.forPayload(FlowableTakeUntilPredicateTest::sourceThrows, "sourceThrows", this);
            this.payloads.backpressure = _ClassStatement.forPayload(FlowableTakeUntilPredicateTest::backpressure, "backpressure", this);
            this.payloads.errorIncludesLastValueAsCause = _ClassStatement.forPayload(FlowableTakeUntilPredicateTest::errorIncludesLastValueAsCause, "errorIncludesLastValueAsCause", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableTakeUntilPredicateTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableTakeUntilPredicateTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableTakeUntilPredicateTest::badSource, "badSource", this);
        }
    }
}
