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

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeIsEmptyTest extends RxJavaTest {

    @Test
    public void normal() {
        Maybe.just(1).isEmpty().test().assertResult(false);
    }

    @Test
    public void empty() {
        Maybe.empty().isEmpty().test().assertResult(true);
    }

    @Test
    public void error() {
        Maybe.error(new TestException()).isEmpty().test().assertFailure(TestException.class);
    }

    @Test
    public void fusedBackToMaybe() {
        assertTrue(Maybe.just(1).isEmpty().toMaybe() instanceof MaybeIsEmpty);
    }

    @Test
    public void normalToMaybe() {
        Maybe.just(1).isEmpty().toMaybe().test().assertResult(false);
    }

    @Test
    public void emptyToMaybe() {
        Maybe.empty().isEmpty().toMaybe().test().assertResult(true);
    }

    @Test
    public void errorToMaybe() {
        Maybe.error(new TestException()).isEmpty().toMaybe().test().assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposedMaybeToSingle(new Function<Maybe<Object>, SingleSource<Boolean>>() {

            @Override
            public SingleSource<Boolean> apply(Maybe<Object> m) throws Exception {
                return m.isEmpty();
            }
        });
    }

    @Test
    public void isDisposed() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestHelper.checkDisposed(pp.singleElement().isEmpty());
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybeToSingle(new Function<Maybe<Object>, Single<Boolean>>() {

            @Override
            public Single<Boolean> apply(Maybe<Object> f) throws Exception {
                return f.isEmpty();
            }
        });
    }

    @Test
    public void disposeToMaybe() {
        TestHelper.checkDisposedMaybe(new Function<Maybe<Object>, Maybe<Boolean>>() {

            @Override
            public Maybe<Boolean> apply(Maybe<Object> m) throws Exception {
                return m.isEmpty().toMaybe();
            }
        });
    }

    @Test
    public void isDisposedToMaybe() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestHelper.checkDisposed(pp.singleElement().isEmpty().toMaybe());
    }

    @Test
    public void doubleOnSubscribeToMaybe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, Maybe<Boolean>>() {

            @Override
            public Maybe<Boolean> apply(Maybe<Object> f) throws Exception {
                return f.isEmpty().toMaybe();
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeIsEmptyTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedBackToMaybe() throws java.lang.Throwable {
            this.payloads.fusedBackToMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalToMaybe() throws java.lang.Throwable {
            this.payloads.normalToMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyToMaybe() throws java.lang.Throwable {
            this.payloads.emptyToMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorToMaybe() throws java.lang.Throwable {
            this.payloads.errorToMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposed() throws java.lang.Throwable {
            this.payloads.isDisposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeToMaybe() throws java.lang.Throwable {
            this.payloads.disposeToMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_isDisposedToMaybe() throws java.lang.Throwable {
            this.payloads.isDisposedToMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeToMaybe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribeToMaybe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeIsEmptyTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeIsEmptyTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeIsEmptyTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeIsEmptyTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeIsEmptyTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeIsEmptyTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeIsEmptyTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeIsEmptyTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement fusedBackToMaybe;

            public org.junit.runners.model.Statement normalToMaybe;

            public org.junit.runners.model.Statement emptyToMaybe;

            public org.junit.runners.model.Statement errorToMaybe;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement isDisposed;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement disposeToMaybe;

            public org.junit.runners.model.Statement isDisposedToMaybe;

            public org.junit.runners.model.Statement doubleOnSubscribeToMaybe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(MaybeIsEmptyTest::normal, "normal", this);
            this.payloads.empty = _ClassStatement.forPayload(MaybeIsEmptyTest::empty, "empty", this);
            this.payloads.error = _ClassStatement.forPayload(MaybeIsEmptyTest::error, "error", this);
            this.payloads.fusedBackToMaybe = _ClassStatement.forPayload(MaybeIsEmptyTest::fusedBackToMaybe, "fusedBackToMaybe", this);
            this.payloads.normalToMaybe = _ClassStatement.forPayload(MaybeIsEmptyTest::normalToMaybe, "normalToMaybe", this);
            this.payloads.emptyToMaybe = _ClassStatement.forPayload(MaybeIsEmptyTest::emptyToMaybe, "emptyToMaybe", this);
            this.payloads.errorToMaybe = _ClassStatement.forPayload(MaybeIsEmptyTest::errorToMaybe, "errorToMaybe", this);
            this.payloads.dispose = _ClassStatement.forPayload(MaybeIsEmptyTest::dispose, "dispose", this);
            this.payloads.isDisposed = _ClassStatement.forPayload(MaybeIsEmptyTest::isDisposed, "isDisposed", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(MaybeIsEmptyTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.disposeToMaybe = _ClassStatement.forPayload(MaybeIsEmptyTest::disposeToMaybe, "disposeToMaybe", this);
            this.payloads.isDisposedToMaybe = _ClassStatement.forPayload(MaybeIsEmptyTest::isDisposedToMaybe, "isDisposedToMaybe", this);
            this.payloads.doubleOnSubscribeToMaybe = _ClassStatement.forPayload(MaybeIsEmptyTest::doubleOnSubscribeToMaybe, "doubleOnSubscribeToMaybe", this);
        }
    }
}
