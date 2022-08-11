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
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.parallel.*;

public final class ConverterTest extends RxJavaTest {

    @Test
    public void flowableConverterThrows() {
        try {
            Flowable.just(1).to(new FlowableConverter<Integer, Integer>() {

                @Override
                public Integer apply(Flowable<Integer> v) {
                    throw new TestException("Forced failure");
                }
            });
            fail("Should have thrown!");
        } catch (TestException ex) {
            assertEquals("Forced failure", ex.getMessage());
        }
    }

    @Test
    public void observableConverterThrows() {
        try {
            Observable.just(1).to(new ObservableConverter<Integer, Integer>() {

                @Override
                public Integer apply(Observable<Integer> v) {
                    throw new TestException("Forced failure");
                }
            });
            fail("Should have thrown!");
        } catch (TestException ex) {
            assertEquals("Forced failure", ex.getMessage());
        }
    }

    @Test
    public void singleConverterThrows() {
        try {
            Single.just(1).to(new SingleConverter<Integer, Integer>() {

                @Override
                public Integer apply(Single<Integer> v) {
                    throw new TestException("Forced failure");
                }
            });
            fail("Should have thrown!");
        } catch (TestException ex) {
            assertEquals("Forced failure", ex.getMessage());
        }
    }

    @Test
    public void maybeConverterThrows() {
        try {
            Maybe.just(1).to(new MaybeConverter<Integer, Integer>() {

                @Override
                public Integer apply(Maybe<Integer> v) {
                    throw new TestException("Forced failure");
                }
            });
            fail("Should have thrown!");
        } catch (TestException ex) {
            assertEquals("Forced failure", ex.getMessage());
        }
    }

    @Test
    public void completableConverterThrows() {
        try {
            Completable.complete().to(new CompletableConverter<Completable>() {

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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void observableGenericsSignatureTest() {
        A<String, Integer> a = new A<String, Integer>() {
        };
        Observable.just(a).to((ObservableConverter) ConverterTest.testObservableConverterCreator());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void singleGenericsSignatureTest() {
        A<String, Integer> a = new A<String, Integer>() {
        };
        Single.just(a).to((SingleConverter) ConverterTest.<String>testSingleConverterCreator());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void maybeGenericsSignatureTest() {
        A<String, Integer> a = new A<String, Integer>() {
        };
        Maybe.just(a).to((MaybeConverter) ConverterTest.<String>testMaybeConverterCreator());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void flowableGenericsSignatureTest() {
        A<String, Integer> a = new A<String, Integer>() {
        };
        Flowable.just(a).to((FlowableConverter) ConverterTest.<String>testFlowableConverterCreator());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void parallelFlowableGenericsSignatureTest() {
        A<String, Integer> a = new A<String, Integer>() {
        };
        Flowable.just(a).parallel().to((ParallelFlowableConverter) ConverterTest.<String>testParallelFlowableConverterCreator());
    }

    @Test
    public void compositeTest() {
        CompositeConverter converter = new CompositeConverter();
        Flowable.just(1).to(converter).test().assertValue(1);
        Observable.just(1).to(converter).test().assertValue(1);
        Maybe.just(1).to(converter).test().assertValue(1);
        Single.just(1).to(converter).test().assertValue(1);
        Completable.complete().to(converter).test().assertComplete();
        Flowable.just(1).parallel().to(converter).test().assertValue(1);
    }

    /**
     * Two argument type.
     * @param <T> the input type
     * @param <R> the output type
     */
    interface A<T, R> {
    }

    /**
     * One argument type.
     * @param <T> the type
     */
    interface B<T> {
    }

    private static <T> ObservableConverter<A<T, ?>, B<T>> testObservableConverterCreator() {
        return new ObservableConverter<A<T, ?>, B<T>>() {

            @Override
            public B<T> apply(Observable<A<T, ?>> a) {
                return new B<T>() {
                };
            }
        };
    }

    private static <T> SingleConverter<A<T, ?>, B<T>> testSingleConverterCreator() {
        return new SingleConverter<A<T, ?>, B<T>>() {

            @Override
            public B<T> apply(Single<A<T, ?>> a) {
                return new B<T>() {
                };
            }
        };
    }

    private static <T> MaybeConverter<A<T, ?>, B<T>> testMaybeConverterCreator() {
        return new MaybeConverter<A<T, ?>, B<T>>() {

            @Override
            public B<T> apply(Maybe<A<T, ?>> a) {
                return new B<T>() {
                };
            }
        };
    }

    private static <T> FlowableConverter<A<T, ?>, B<T>> testFlowableConverterCreator() {
        return new FlowableConverter<A<T, ?>, B<T>>() {

            @Override
            public B<T> apply(Flowable<A<T, ?>> a) {
                return new B<T>() {
                };
            }
        };
    }

    private static <T> ParallelFlowableConverter<A<T, ?>, B<T>> testParallelFlowableConverterCreator() {
        return new ParallelFlowableConverter<A<T, ?>, B<T>>() {

            @Override
            public B<T> apply(ParallelFlowable<A<T, ?>> a) {
                return new B<T>() {
                };
            }
        };
    }

    static class CompositeConverter implements ObservableConverter<Integer, Flowable<Integer>>, ParallelFlowableConverter<Integer, Flowable<Integer>>, FlowableConverter<Integer, Observable<Integer>>, MaybeConverter<Integer, Flowable<Integer>>, SingleConverter<Integer, Flowable<Integer>>, CompletableConverter<Flowable<Integer>> {

        @Override
        public Flowable<Integer> apply(ParallelFlowable<Integer> upstream) {
            return upstream.sequential();
        }

        @Override
        public Flowable<Integer> apply(Completable upstream) {
            return upstream.toFlowable();
        }

        @Override
        public Observable<Integer> apply(Flowable<Integer> upstream) {
            return upstream.toObservable();
        }

        @Override
        public Flowable<Integer> apply(Maybe<Integer> upstream) {
            return upstream.toFlowable();
        }

        @Override
        public Flowable<Integer> apply(Observable<Integer> upstream) {
            return upstream.toFlowable(BackpressureStrategy.MISSING);
        }

        @Override
        public Flowable<Integer> apply(Single<Integer> upstream) {
            return upstream.toFlowable();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ConverterTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableConverterThrows() throws java.lang.Throwable {
            this.payloads.flowableConverterThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableConverterThrows() throws java.lang.Throwable {
            this.payloads.observableConverterThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleConverterThrows() throws java.lang.Throwable {
            this.payloads.singleConverterThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeConverterThrows() throws java.lang.Throwable {
            this.payloads.maybeConverterThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableConverterThrows() throws java.lang.Throwable {
            this.payloads.completableConverterThrows.evaluate();
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

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_parallelFlowableGenericsSignatureTest() throws java.lang.Throwable {
            this.payloads.parallelFlowableGenericsSignatureTest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_compositeTest() throws java.lang.Throwable {
            this.payloads.compositeTest.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ConverterTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ConverterTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ConverterTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ConverterTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ConverterTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ConverterTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ConverterTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ConverterTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement flowableConverterThrows;

            public org.junit.runners.model.Statement observableConverterThrows;

            public org.junit.runners.model.Statement singleConverterThrows;

            public org.junit.runners.model.Statement maybeConverterThrows;

            public org.junit.runners.model.Statement completableConverterThrows;

            public org.junit.runners.model.Statement observableGenericsSignatureTest;

            public org.junit.runners.model.Statement singleGenericsSignatureTest;

            public org.junit.runners.model.Statement maybeGenericsSignatureTest;

            public org.junit.runners.model.Statement flowableGenericsSignatureTest;

            public org.junit.runners.model.Statement parallelFlowableGenericsSignatureTest;

            public org.junit.runners.model.Statement compositeTest;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.flowableConverterThrows = _ClassStatement.forPayload(ConverterTest::flowableConverterThrows, "flowableConverterThrows", this);
            this.payloads.observableConverterThrows = _ClassStatement.forPayload(ConverterTest::observableConverterThrows, "observableConverterThrows", this);
            this.payloads.singleConverterThrows = _ClassStatement.forPayload(ConverterTest::singleConverterThrows, "singleConverterThrows", this);
            this.payloads.maybeConverterThrows = _ClassStatement.forPayload(ConverterTest::maybeConverterThrows, "maybeConverterThrows", this);
            this.payloads.completableConverterThrows = _ClassStatement.forPayload(ConverterTest::completableConverterThrows, "completableConverterThrows", this);
            this.payloads.observableGenericsSignatureTest = _ClassStatement.forPayload(ConverterTest::observableGenericsSignatureTest, "observableGenericsSignatureTest", this);
            this.payloads.singleGenericsSignatureTest = _ClassStatement.forPayload(ConverterTest::singleGenericsSignatureTest, "singleGenericsSignatureTest", this);
            this.payloads.maybeGenericsSignatureTest = _ClassStatement.forPayload(ConverterTest::maybeGenericsSignatureTest, "maybeGenericsSignatureTest", this);
            this.payloads.flowableGenericsSignatureTest = _ClassStatement.forPayload(ConverterTest::flowableGenericsSignatureTest, "flowableGenericsSignatureTest", this);
            this.payloads.parallelFlowableGenericsSignatureTest = _ClassStatement.forPayload(ConverterTest::parallelFlowableGenericsSignatureTest, "parallelFlowableGenericsSignatureTest", this);
            this.payloads.compositeTest = _ClassStatement.forPayload(ConverterTest::compositeTest, "compositeTest", this);
        }
    }
}
