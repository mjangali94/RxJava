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
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.testsupport.TestHelper;

@SuppressWarnings("unchecked")
public class FlowableDeferTest extends RxJavaTest {

    @Test
    public void defer() throws Throwable {
        Supplier<Flowable<String>> factory = mock(Supplier.class);
        Flowable<String> firstObservable = Flowable.just("one", "two");
        Flowable<String> secondObservable = Flowable.just("three", "four");
        when(factory.get()).thenReturn(firstObservable, secondObservable);
        Flowable<String> deferred = Flowable.defer(factory);
        verifyNoInteractions(factory);
        Subscriber<String> firstSubscriber = TestHelper.mockSubscriber();
        deferred.subscribe(firstSubscriber);
        verify(factory, times(1)).get();
        verify(firstSubscriber, times(1)).onNext("one");
        verify(firstSubscriber, times(1)).onNext("two");
        verify(firstSubscriber, times(0)).onNext("three");
        verify(firstSubscriber, times(0)).onNext("four");
        verify(firstSubscriber, times(1)).onComplete();
        Subscriber<String> secondSubscriber = TestHelper.mockSubscriber();
        deferred.subscribe(secondSubscriber);
        verify(factory, times(2)).get();
        verify(secondSubscriber, times(0)).onNext("one");
        verify(secondSubscriber, times(0)).onNext("two");
        verify(secondSubscriber, times(1)).onNext("three");
        verify(secondSubscriber, times(1)).onNext("four");
        verify(secondSubscriber, times(1)).onComplete();
    }

    @Test
    public void deferFunctionThrows() throws Throwable {
        Supplier<Flowable<String>> factory = mock(Supplier.class);
        when(factory.get()).thenThrow(new TestException());
        Flowable<String> result = Flowable.defer(factory);
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        result.subscribe(subscriber);
        verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onNext(any(String.class));
        verify(subscriber, never()).onComplete();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableDeferTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_defer() throws java.lang.Throwable {
            this.payloads.defer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferFunctionThrows() throws java.lang.Throwable {
            this.payloads.deferFunctionThrows.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDeferTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDeferTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDeferTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDeferTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableDeferTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDeferTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableDeferTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableDeferTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement defer;

            public org.junit.runners.model.Statement deferFunctionThrows;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.defer = _ClassStatement.forPayload(FlowableDeferTest::defer, "defer", this);
            this.payloads.deferFunctionThrows = _ClassStatement.forPayload(FlowableDeferTest::deferFunctionThrows, "deferFunctionThrows", this);
        }
    }
}
