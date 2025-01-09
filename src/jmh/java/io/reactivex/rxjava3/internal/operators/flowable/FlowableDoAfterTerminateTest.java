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
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.*;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableDoAfterTerminateTest extends RxJavaTest {

    private Action aAction0;

    private Subscriber<String> subscriber;

    @Before
    public void before() {
        aAction0 = Mockito.mock(Action.class);
        subscriber = TestHelper.mockSubscriber();
    }

    private void checkActionCalled(Flowable<String> input) {
        input.doAfterTerminate(aAction0).subscribe(subscriber);
        try {
            verify(aAction0, times(1)).run();
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    @Test
    public void doAfterTerminateCalledOnComplete() {
        checkActionCalled(Flowable.fromArray("1", "2", "3"));
    }

    @Test
    public void doAfterTerminateCalledOnError() {
        checkActionCalled(Flowable.<String>error(new RuntimeException("expected")));
    }

    @Test
    public void nullActionShouldBeCheckedInConstructor() {
        try {
            Flowable.empty().doAfterTerminate(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException expected) {
            assertEquals("onAfterTerminate is null", expected.getMessage());
        }
    }

    @Test
    public void nullFinallyActionShouldBeCheckedASAP() {
        try {
            Flowable.just("value").doAfterTerminate(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    @Test
    public void ifFinallyActionThrowsExceptionShouldNotBeSwallowedAndActionShouldBeCalledOnce() throws Throwable {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Action finallyAction = Mockito.mock(Action.class);
            doThrow(new IllegalStateException()).when(finallyAction).run();
            TestSubscriber<String> testSubscriber = new TestSubscriber<>();
            Flowable.just("value").doAfterTerminate(finallyAction).subscribe(testSubscriber);
            testSubscriber.assertValue("value");
            verify(finallyAction).run();
            TestHelper.assertError(errors, 0, IllegalStateException.class);
        // Actual result:
        // Not only IllegalStateException was swallowed
        // But finallyAction was called twice!
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableDoAfterTerminateTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doAfterTerminateCalledOnComplete() throws java.lang.Throwable {
            this.payloads.doAfterTerminateCalledOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doAfterTerminateCalledOnError() throws java.lang.Throwable {
            this.payloads.doAfterTerminateCalledOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullActionShouldBeCheckedInConstructor() throws java.lang.Throwable {
            this.payloads.nullActionShouldBeCheckedInConstructor.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullFinallyActionShouldBeCheckedASAP() throws java.lang.Throwable {
            this.payloads.nullFinallyActionShouldBeCheckedASAP.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ifFinallyActionThrowsExceptionShouldNotBeSwallowedAndActionShouldBeCalledOnce() throws java.lang.Throwable {
            this.payloads.ifFinallyActionThrowsExceptionShouldNotBeSwallowedAndActionShouldBeCalledOnce.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoAfterTerminateTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoAfterTerminateTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoAfterTerminateTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoAfterTerminateTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableDoAfterTerminateTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDoAfterTerminateTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableDoAfterTerminateTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableDoAfterTerminateTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement doAfterTerminateCalledOnComplete;

            public org.junit.runners.model.Statement doAfterTerminateCalledOnError;

            public org.junit.runners.model.Statement nullActionShouldBeCheckedInConstructor;

            public org.junit.runners.model.Statement nullFinallyActionShouldBeCheckedASAP;

            public org.junit.runners.model.Statement ifFinallyActionThrowsExceptionShouldNotBeSwallowedAndActionShouldBeCalledOnce;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.doAfterTerminateCalledOnComplete = _ClassStatement.forPayload(FlowableDoAfterTerminateTest::doAfterTerminateCalledOnComplete, "doAfterTerminateCalledOnComplete", this);
            this.payloads.doAfterTerminateCalledOnError = _ClassStatement.forPayload(FlowableDoAfterTerminateTest::doAfterTerminateCalledOnError, "doAfterTerminateCalledOnError", this);
            this.payloads.nullActionShouldBeCheckedInConstructor = _ClassStatement.forPayload(FlowableDoAfterTerminateTest::nullActionShouldBeCheckedInConstructor, "nullActionShouldBeCheckedInConstructor", this);
            this.payloads.nullFinallyActionShouldBeCheckedASAP = _ClassStatement.forPayload(FlowableDoAfterTerminateTest::nullFinallyActionShouldBeCheckedASAP, "nullFinallyActionShouldBeCheckedASAP", this);
            this.payloads.ifFinallyActionThrowsExceptionShouldNotBeSwallowedAndActionShouldBeCalledOnce = _ClassStatement.forPayload(FlowableDoAfterTerminateTest::ifFinallyActionThrowsExceptionShouldNotBeSwallowedAndActionShouldBeCalledOnce, "ifFinallyActionThrowsExceptionShouldNotBeSwallowedAndActionShouldBeCalledOnce", this);
        }
    }
}
