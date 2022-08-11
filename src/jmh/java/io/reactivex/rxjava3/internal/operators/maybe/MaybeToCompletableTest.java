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
package io.reactivex.rxjava3.internal.operators.maybe;

import static org.junit.Assert.assertSame;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.fuseable.HasUpstreamMaybeSource;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeToCompletableTest extends RxJavaTest {

    @SuppressWarnings("unchecked")
    @Test
    public void source() {
        Maybe<Integer> source = Maybe.just(1);
        assertSame(source, ((HasUpstreamMaybeSource<Integer>) source.ignoreElement().toMaybe()).source());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Maybe.never().ignoreElement().toMaybe());
    }

    @Test
    public void successToComplete() {
        Maybe.just(1).ignoreElement().test().assertResult();
    }

    @Test
    public void doubleSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybeToCompletable(new Function<Maybe<Object>, CompletableSource>() {

            @Override
            public CompletableSource apply(Maybe<Object> m) throws Exception {
                return m.ignoreElement();
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeToCompletableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_source() throws java.lang.Throwable {
            this.payloads.source.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successToComplete() throws java.lang.Throwable {
            this.payloads.successToComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleSubscribe() throws java.lang.Throwable {
            this.payloads.doubleSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeToCompletableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeToCompletableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeToCompletableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeToCompletableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeToCompletableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeToCompletableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeToCompletableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeToCompletableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement source;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement successToComplete;

            public org.junit.runners.model.Statement doubleSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.source = _ClassStatement.forPayload(MaybeToCompletableTest::source, "source", this);
            this.payloads.dispose = _ClassStatement.forPayload(MaybeToCompletableTest::dispose, "dispose", this);
            this.payloads.successToComplete = _ClassStatement.forPayload(MaybeToCompletableTest::successToComplete, "successToComplete", this);
            this.payloads.doubleSubscribe = _ClassStatement.forPayload(MaybeToCompletableTest::doubleSubscribe, "doubleSubscribe", this);
        }
    }
}
