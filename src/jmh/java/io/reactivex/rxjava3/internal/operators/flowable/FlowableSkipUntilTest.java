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
import org.junit.*;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableSkipUntilTest extends RxJavaTest {

    Subscriber<Object> subscriber;

    @Before
    public void before() {
        subscriber = TestHelper.mockSubscriber();
    }

    @Test
    public void normal1() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> m = source.skipUntil(other);
        m.subscribe(subscriber);
        source.onNext(0);
        source.onNext(1);
        other.onNext(100);
        source.onNext(2);
        source.onNext(3);
        source.onNext(4);
        source.onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onNext(2);
        verify(subscriber, times(1)).onNext(3);
        verify(subscriber, times(1)).onNext(4);
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void otherNeverFires() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<Integer> m = source.skipUntil(Flowable.never());
        m.subscribe(subscriber);
        source.onNext(0);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onNext(4);
        source.onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, never()).onNext(any());
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void otherEmpty() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        Flowable<Integer> m = source.skipUntil(Flowable.empty());
        m.subscribe(subscriber);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void otherFiresAndCompletes() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> m = source.skipUntil(other);
        m.subscribe(subscriber);
        source.onNext(0);
        source.onNext(1);
        other.onNext(100);
        other.onComplete();
        source.onNext(2);
        source.onNext(3);
        source.onNext(4);
        source.onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber, times(1)).onNext(2);
        verify(subscriber, times(1)).onNext(3);
        verify(subscriber, times(1)).onNext(4);
        verify(subscriber, times(1)).onComplete();
    }

    @Test
    public void sourceThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> m = source.skipUntil(other);
        m.subscribe(subscriber);
        source.onNext(0);
        source.onNext(1);
        other.onNext(100);
        other.onComplete();
        source.onNext(2);
        source.onError(new RuntimeException("Forced failure"));
        verify(subscriber, times(1)).onNext(2);
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void otherThrowsImmediately() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        PublishProcessor<Integer> other = PublishProcessor.create();
        Flowable<Integer> m = source.skipUntil(other);
        m.subscribe(subscriber);
        source.onNext(0);
        source.onNext(1);
        other.onError(new RuntimeException("Forced failure"));
        verify(subscriber, never()).onNext(any());
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().skipUntil(PublishProcessor.create()));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.skipUntil(Flowable.never());
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return Flowable.never().skipUntil(f);
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableSkipUntilTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal1() throws java.lang.Throwable {
            this.payloads.normal1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherNeverFires() throws java.lang.Throwable {
            this.payloads.otherNeverFires.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherEmpty() throws java.lang.Throwable {
            this.payloads.otherEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherFiresAndCompletes() throws java.lang.Throwable {
            this.payloads.otherFiresAndCompletes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceThrows() throws java.lang.Throwable {
            this.payloads.sourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherThrowsImmediately() throws java.lang.Throwable {
            this.payloads.otherThrowsImmediately.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipUntilTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipUntilTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.before();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipUntilTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipUntilTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableSkipUntilTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableSkipUntilTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableSkipUntilTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableSkipUntilTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal1;

            public org.junit.runners.model.Statement otherNeverFires;

            public org.junit.runners.model.Statement otherEmpty;

            public org.junit.runners.model.Statement otherFiresAndCompletes;

            public org.junit.runners.model.Statement sourceThrows;

            public org.junit.runners.model.Statement otherThrowsImmediately;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal1 = _ClassStatement.forPayload(FlowableSkipUntilTest::normal1, "normal1", this);
            this.payloads.otherNeverFires = _ClassStatement.forPayload(FlowableSkipUntilTest::otherNeverFires, "otherNeverFires", this);
            this.payloads.otherEmpty = _ClassStatement.forPayload(FlowableSkipUntilTest::otherEmpty, "otherEmpty", this);
            this.payloads.otherFiresAndCompletes = _ClassStatement.forPayload(FlowableSkipUntilTest::otherFiresAndCompletes, "otherFiresAndCompletes", this);
            this.payloads.sourceThrows = _ClassStatement.forPayload(FlowableSkipUntilTest::sourceThrows, "sourceThrows", this);
            this.payloads.otherThrowsImmediately = _ClassStatement.forPayload(FlowableSkipUntilTest::otherThrowsImmediately, "otherThrowsImmediately", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableSkipUntilTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableSkipUntilTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
