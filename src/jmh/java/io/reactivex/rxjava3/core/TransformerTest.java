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
package io.reactivex.rxjava3.core;

import static org.junit.Assert.*;
import org.junit.Test;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.ConverterTest.*;
import io.reactivex.rxjava3.exceptions.TestException;

public class TransformerTest extends RxJavaTest {

    @Test
    public void flowableTransformerThrows() {
        try {
            Flowable.just(1).compose(new FlowableTransformer<Integer, Integer>() {

                @Override
                public Publisher<Integer> apply(Flowable<Integer> v) {
                    throw new TestException("Forced failure");
                }
            });
            fail("Should have thrown!");
        } catch (TestException ex) {
            assertEquals("Forced failure", ex.getMessage());
        }
    }

    @Test
    public void observableTransformerThrows() {
        try {
            Observable.just(1).compose(new ObservableTransformer<Integer, Integer>() {

                @Override
                public Observable<Integer> apply(Observable<Integer> v) {
                    throw new TestException("Forced failure");
                }
            });
            fail("Should have thrown!");
        } catch (TestException ex) {
            assertEquals("Forced failure", ex.getMessage());
        }
    }

    @Test
    public void singleTransformerThrows() {
        try {
            Single.just(1).compose(new SingleTransformer<Integer, Integer>() {

                @Override
                public Single<Integer> apply(Single<Integer> v) {
                    throw new TestException("Forced failure");
                }
            });
            fail("Should have thrown!");
        } catch (TestException ex) {
            assertEquals("Forced failure", ex.getMessage());
        }
    }

    @Test
    public void maybeTransformerThrows() {
        try {
            Maybe.just(1).compose(new MaybeTransformer<Integer, Integer>() {

                @Override
                public Maybe<Integer> apply(Maybe<Integer> v) {
                    throw new TestException("Forced failure");
                }
            });
            fail("Should have thrown!");
        } catch (TestException ex) {
            assertEquals("Forced failure", ex.getMessage());
        }
    }

    @Test
    public void completableTransformerThrows() {
        try {
            Completable.complete().compose(new CompletableTransformer() {

                @Override
                public Completable apply(Completable v) {
                    throw new TestException("Forced failure");
                }
            });
            fail("Should have thrown!");
        } catch (TestException ex) {
            assertEquals("Forced failure", ex.getMessage());
        }
    }

    // Test demos for signature generics in compose() methods. Just needs to compile.
    @Test
    public void observableGenericsSignatureTest() {
        A<String, Integer> a = new A<String, Integer>() {
        };
        Observable.just(a).compose(TransformerTest.<String>testObservableTransformerCreator());
    }

    @Test
    public void singleGenericsSignatureTest() {
        A<String, Integer> a = new A<String, Integer>() {
        };
        Single.just(a).compose(TransformerTest.<String>testSingleTransformerCreator());
    }

    @Test
    public void maybeGenericsSignatureTest() {
        A<String, Integer> a = new A<String, Integer>() {
        };
        Maybe.just(a).compose(TransformerTest.<String>testMaybeTransformerCreator());
    }

    @Test
    public void flowableGenericsSignatureTest() {
        A<String, Integer> a = new A<String, Integer>() {
        };
        Flowable.just(a).compose(TransformerTest.<String>testFlowableTransformerCreator());
    }

    private static <T> ObservableTransformer<A<T, ?>, B<T>> testObservableTransformerCreator() {
        return new ObservableTransformer<A<T, ?>, B<T>>() {

            @Override
            public ObservableSource<B<T>> apply(Observable<A<T, ?>> a) {
                return Observable.empty();
            }
        };
    }

    private static <T> SingleTransformer<A<T, ?>, B<T>> testSingleTransformerCreator() {
        return new SingleTransformer<A<T, ?>, B<T>>() {

            @Override
            public SingleSource<B<T>> apply(Single<A<T, ?>> a) {
                return Single.never();
            }
        };
    }

    private static <T> MaybeTransformer<A<T, ?>, B<T>> testMaybeTransformerCreator() {
        return new MaybeTransformer<A<T, ?>, B<T>>() {

            @Override
            public MaybeSource<B<T>> apply(Maybe<A<T, ?>> a) {
                return Maybe.empty();
            }
        };
    }

    private static <T> FlowableTransformer<A<T, ?>, B<T>> testFlowableTransformerCreator() {
        return new FlowableTransformer<A<T, ?>, B<T>>() {

            @Override
            public Publisher<B<T>> apply(Flowable<A<T, ?>> a) {
                return Flowable.empty();
            }
        };
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private TransformerTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableTransformerThrows() throws java.lang.Throwable {
            this.payloads.flowableTransformerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableTransformerThrows() throws java.lang.Throwable {
            this.payloads.observableTransformerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleTransformerThrows() throws java.lang.Throwable {
            this.payloads.singleTransformerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeTransformerThrows() throws java.lang.Throwable {
            this.payloads.maybeTransformerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableTransformerThrows() throws java.lang.Throwable {
            this.payloads.completableTransformerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableGenericsSignatureTest() throws java.lang.Throwable {
            this.payloads.observableGenericsSignatureTest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleGenericsSignatureTest() throws java.lang.Throwable {
            this.payloads.singleGenericsSignatureTest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeGenericsSignatureTest() throws java.lang.Throwable {
            this.payloads.maybeGenericsSignatureTest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableGenericsSignatureTest() throws java.lang.Throwable {
            this.payloads.flowableGenericsSignatureTest.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<TransformerTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<TransformerTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<TransformerTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<TransformerTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new TransformerTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<TransformerTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(TransformerTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(TransformerTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement flowableTransformerThrows;

            public org.junit.runners.model.Statement observableTransformerThrows;

            public org.junit.runners.model.Statement singleTransformerThrows;

            public org.junit.runners.model.Statement maybeTransformerThrows;

            public org.junit.runners.model.Statement completableTransformerThrows;

            public org.junit.runners.model.Statement observableGenericsSignatureTest;

            public org.junit.runners.model.Statement singleGenericsSignatureTest;

            public org.junit.runners.model.Statement maybeGenericsSignatureTest;

            public org.junit.runners.model.Statement flowableGenericsSignatureTest;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.flowableTransformerThrows = _ClassStatement.forPayload(TransformerTest::flowableTransformerThrows, "flowableTransformerThrows", this);
            this.payloads.observableTransformerThrows = _ClassStatement.forPayload(TransformerTest::observableTransformerThrows, "observableTransformerThrows", this);
            this.payloads.singleTransformerThrows = _ClassStatement.forPayload(TransformerTest::singleTransformerThrows, "singleTransformerThrows", this);
            this.payloads.maybeTransformerThrows = _ClassStatement.forPayload(TransformerTest::maybeTransformerThrows, "maybeTransformerThrows", this);
            this.payloads.completableTransformerThrows = _ClassStatement.forPayload(TransformerTest::completableTransformerThrows, "completableTransformerThrows", this);
            this.payloads.observableGenericsSignatureTest = _ClassStatement.forPayload(TransformerTest::observableGenericsSignatureTest, "observableGenericsSignatureTest", this);
            this.payloads.singleGenericsSignatureTest = _ClassStatement.forPayload(TransformerTest::singleGenericsSignatureTest, "singleGenericsSignatureTest", this);
            this.payloads.maybeGenericsSignatureTest = _ClassStatement.forPayload(TransformerTest::maybeGenericsSignatureTest, "maybeGenericsSignatureTest", this);
            this.payloads.flowableGenericsSignatureTest = _ClassStatement.forPayload(TransformerTest::flowableGenericsSignatureTest, "flowableGenericsSignatureTest", this);
        }
    }
}
