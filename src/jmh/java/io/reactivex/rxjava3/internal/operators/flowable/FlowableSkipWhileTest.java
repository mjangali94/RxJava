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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.mockito.InOrder;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableSkipWhileTest extends RxJavaTest {

    Subscriber<Integer> w = TestHelper.mockSubscriber();

    private static final Predicate<Integer> LESS_THAN_FIVE = new Predicate<Integer>() {

        @Override
        public boolean test(Integer v) {
            if (v == 42) {
                throw new RuntimeException("that's not the answer to everything!");
            }
            return v < 5;
        }
    };

    private static final Predicate<Integer> INDEX_LESS_THAN_THREE = new Predicate<Integer>() {

        int index;

        @Override
        public boolean test(Integer value) {
            return index++ < 3;
        }
    };

    @Test
    public void skipWithIndex() {
        Flowable<Integer> src = Flowable.just(1, 2, 3, 4, 5);
        src.skipWhile(INDEX_LESS_THAN_THREE).subscribe(w);
        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext(4);
        inOrder.verify(w, times(1)).onNext(5);
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipEmpty() {
        Flowable<Integer> src = Flowable.empty();
        src.skipWhile(LESS_THAN_FIVE).subscribe(w);
        verify(w, never()).onNext(anyInt());
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void skipEverything() {
        Flowable<Integer> src = Flowable.just(1, 2, 3, 4, 3, 2, 1);
        src.skipWhile(LESS_THAN_FIVE).subscribe(w);
        verify(w, never()).onNext(anyInt());
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void skipNothing() {
        Flowable<Integer> src = Flowable.just(5, 3, 1);
        src.skipWhile(LESS_THAN_FIVE).subscribe(w);
        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext(5);
        inOrder.verify(w, times(1)).onNext(3);
        inOrder.verify(w, times(1)).onNext(1);
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipSome() {
        Flowable<Integer> src = Flowable.just(1, 2, 3, 4, 5, 3, 1, 5);
        src.skipWhile(LESS_THAN_FIVE).subscribe(w);
        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext(5);
        inOrder.verify(w, times(1)).onNext(3);
        inOrder.verify(w, times(1)).onNext(1);
        inOrder.verify(w, times(1)).onNext(5);
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void skipError() {
        Flowable<Integer> src = Flowable.just(1, 2, 42, 5, 3, 1);
        src.skipWhile(LESS_THAN_FIVE).subscribe(w);
        InOrder inOrder = inOrder(w);
        inOrder.verify(w, never()).onNext(anyInt());
        inOrder.verify(w, never()).onComplete();
        inOrder.verify(w, times(1)).onError(any(RuntimeException.class));
    }

    @Test
    public void skipManySubscribers() {
        Flowable<Integer> src = Flowable.range(1, 10).skipWhile(LESS_THAN_FIVE);
        int n = 5;
        for (int i = 0; i < n; i++) {
            Subscriber<Object> subscriber = TestHelper.mockSubscriber();
            InOrder inOrder = inOrder(subscriber);
            src.subscribe(subscriber);
            for (int j = 5; j < 10; j++) {
                inOrder.verify(subscriber).onNext(j);
            }
            inOrder.verify(subscriber).onComplete();
            verify(subscriber, never()).onError(any(Throwable.class));
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().skipWhile(Functions.alwaysFalse()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.skipWhile(Functions.alwaysFalse());
            }
        });
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).skipWhile(Functions.alwaysFalse()).test().assertFailure(TestException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableSkipWhileTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipWithIndex() throws java.lang.Throwable {
            this.payloads.skipWithIndex.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipEmpty() throws java.lang.Throwable {
            this.payloads.skipEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipEverything() throws java.lang.Throwable {
            this.payloads.skipEverything.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipNothing() throws java.lang.Throwable {
            this.payloads.skipNothing.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipSome() throws java.lang.Throwable {
            this.payloads.skipSome.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipError() throws java.lang.Throwable {
            this.payloads.skipError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipManySubscribers() throws java.lang.Throwable {
            this.payloads.skipManySubscribers.evaluate();
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
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipWhileTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipWhileTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipWhileTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipWhileTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableSkipWhileTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipWhileTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableSkipWhileTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableSkipWhileTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement skipWithIndex;

            public org.junit.runners.model.Statement skipEmpty;

            public org.junit.runners.model.Statement skipEverything;

            public org.junit.runners.model.Statement skipNothing;

            public org.junit.runners.model.Statement skipSome;

            public org.junit.runners.model.Statement skipError;

            public org.junit.runners.model.Statement skipManySubscribers;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement error;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.skipWithIndex = _ClassStatement.forPayload(FlowableSkipWhileTest::skipWithIndex, "skipWithIndex", this);
            this.payloads.skipEmpty = _ClassStatement.forPayload(FlowableSkipWhileTest::skipEmpty, "skipEmpty", this);
            this.payloads.skipEverything = _ClassStatement.forPayload(FlowableSkipWhileTest::skipEverything, "skipEverything", this);
            this.payloads.skipNothing = _ClassStatement.forPayload(FlowableSkipWhileTest::skipNothing, "skipNothing", this);
            this.payloads.skipSome = _ClassStatement.forPayload(FlowableSkipWhileTest::skipSome, "skipSome", this);
            this.payloads.skipError = _ClassStatement.forPayload(FlowableSkipWhileTest::skipError, "skipError", this);
            this.payloads.skipManySubscribers = _ClassStatement.forPayload(FlowableSkipWhileTest::skipManySubscribers, "skipManySubscribers", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableSkipWhileTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableSkipWhileTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableSkipWhileTest::error, "error", this);
        }
    }
}
