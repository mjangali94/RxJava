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
package io.reactivex.rxjava3.internal.jdk8;

import static org.junit.Assert.assertFalse;
import java.io.IOException;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableCollectWithCollectorTest extends RxJavaTest {

    @Test
    public void basic() {
        Flowable.range(1, 5).collect(Collectors.toList()).test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void empty() {
        Flowable.empty().collect(Collectors.toList()).test().assertResult(Collections.emptyList());
    }

    @Test
    public void error() {
        Flowable.error(new TestException()).collect(Collectors.toList()).test().assertFailure(TestException.class);
    }

    @Test
    public void collectorSupplierCrash() {
        Flowable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                throw new TestException();
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> a;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void collectorAccumulatorCrash() {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        source.collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                return () -> 1;
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                    throw new TestException();
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> a;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).test().assertFailure(TestException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void collectorFinisherCrash() {
        Flowable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                return () -> 1;
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> {
                    throw new TestException();
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void collectorAccumulatorDropSignals() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Flowable<Integer> source = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onNext(2);
                    s.onError(new IOException());
                    s.onComplete();
                }
            };
            source.collect(new Collector<Integer, Integer, Integer>() {

                @Override
                public Supplier<Integer> supplier() {
                    return () -> 1;
                }

                @Override
                public BiConsumer<Integer, Integer> accumulator() {
                    return (a, b) -> {
                        throw new TestException();
                    };
                }

                @Override
                public BinaryOperator<Integer> combiner() {
                    return (a, b) -> a + b;
                }

                @Override
                public Function<Integer, Integer> finisher() {
                    return a -> a;
                }

                @Override
                public Set<Characteristics> characteristics() {
                    return Collections.emptySet();
                }
            }).test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        });
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().collect(Collectors.toList()));
    }

    @Test
    public void onSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(f -> f.collect(Collectors.toList()));
    }

    @Test
    public void basicToFlowable() {
        Flowable.range(1, 5).collect(Collectors.toList()).toFlowable().test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void emptyToFlowable() {
        Flowable.empty().collect(Collectors.toList()).toFlowable().test().assertResult(Collections.emptyList());
    }

    @Test
    public void errorToFlowable() {
        Flowable.error(new TestException()).collect(Collectors.toList()).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void collectorSupplierCrashToFlowable() {
        Flowable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                throw new TestException();
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> a;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void collectorAccumulatorCrashToFlowable() {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        source.collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                return () -> 1;
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                    throw new TestException();
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> a;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).toFlowable().test().assertFailure(TestException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void collectorFinisherCrashToFlowable() {
        Flowable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

            @Override
            public Supplier<Integer> supplier() {
                return () -> 1;
            }

            @Override
            public BiConsumer<Integer, Integer> accumulator() {
                return (a, b) -> {
                };
            }

            @Override
            public BinaryOperator<Integer> combiner() {
                return (a, b) -> a + b;
            }

            @Override
            public Function<Integer, Integer> finisher() {
                return a -> {
                    throw new TestException();
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        }).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void collectorAccumulatorDropSignalsToFlowable() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Flowable<Integer> source = new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onNext(2);
                    s.onError(new IOException());
                    s.onComplete();
                }
            };
            source.collect(new Collector<Integer, Integer, Integer>() {

                @Override
                public Supplier<Integer> supplier() {
                    return () -> 1;
                }

                @Override
                public BiConsumer<Integer, Integer> accumulator() {
                    return (a, b) -> {
                        throw new TestException();
                    };
                }

                @Override
                public BinaryOperator<Integer> combiner() {
                    return (a, b) -> a + b;
                }

                @Override
                public Function<Integer, Integer> finisher() {
                    return a -> a;
                }

                @Override
                public Set<Characteristics> characteristics() {
                    return Collections.emptySet();
                }
            }).toFlowable().test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        });
    }

    @Test
    public void disposeToFlowable() {
        TestHelper.checkDisposed(PublishProcessor.create().collect(Collectors.toList()).toFlowable());
    }

    @Test
    public void onSubscribeToFlowable() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.collect(Collectors.toList()).toFlowable());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableCollectWithCollectorTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basic() throws java.lang.Throwable {
            this.payloads.basic.evaluate();
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
        public void benchmark_collectorSupplierCrash() throws java.lang.Throwable {
            this.payloads.collectorSupplierCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorAccumulatorCrash() throws java.lang.Throwable {
            this.payloads.collectorAccumulatorCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFinisherCrash() throws java.lang.Throwable {
            this.payloads.collectorFinisherCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorAccumulatorDropSignals() throws java.lang.Throwable {
            this.payloads.collectorAccumulatorDropSignals.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribe() throws java.lang.Throwable {
            this.payloads.onSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicToFlowable() throws java.lang.Throwable {
            this.payloads.basicToFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyToFlowable() throws java.lang.Throwable {
            this.payloads.emptyToFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorToFlowable() throws java.lang.Throwable {
            this.payloads.errorToFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorSupplierCrashToFlowable() throws java.lang.Throwable {
            this.payloads.collectorSupplierCrashToFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorAccumulatorCrashToFlowable() throws java.lang.Throwable {
            this.payloads.collectorAccumulatorCrashToFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFinisherCrashToFlowable() throws java.lang.Throwable {
            this.payloads.collectorFinisherCrashToFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorAccumulatorDropSignalsToFlowable() throws java.lang.Throwable {
            this.payloads.collectorAccumulatorDropSignalsToFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeToFlowable() throws java.lang.Throwable {
            this.payloads.disposeToFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeToFlowable() throws java.lang.Throwable {
            this.payloads.onSubscribeToFlowable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCollectWithCollectorTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCollectWithCollectorTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCollectWithCollectorTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCollectWithCollectorTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableCollectWithCollectorTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCollectWithCollectorTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableCollectWithCollectorTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableCollectWithCollectorTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement basic;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement collectorSupplierCrash;

            public org.junit.runners.model.Statement collectorAccumulatorCrash;

            public org.junit.runners.model.Statement collectorFinisherCrash;

            public org.junit.runners.model.Statement collectorAccumulatorDropSignals;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement onSubscribe;

            public org.junit.runners.model.Statement basicToFlowable;

            public org.junit.runners.model.Statement emptyToFlowable;

            public org.junit.runners.model.Statement errorToFlowable;

            public org.junit.runners.model.Statement collectorSupplierCrashToFlowable;

            public org.junit.runners.model.Statement collectorAccumulatorCrashToFlowable;

            public org.junit.runners.model.Statement collectorFinisherCrashToFlowable;

            public org.junit.runners.model.Statement collectorAccumulatorDropSignalsToFlowable;

            public org.junit.runners.model.Statement disposeToFlowable;

            public org.junit.runners.model.Statement onSubscribeToFlowable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.basic = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::basic, "basic", this);
            this.payloads.empty = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::empty, "empty", this);
            this.payloads.error = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::error, "error", this);
            this.payloads.collectorSupplierCrash = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::collectorSupplierCrash, "collectorSupplierCrash", this);
            this.payloads.collectorAccumulatorCrash = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::collectorAccumulatorCrash, "collectorAccumulatorCrash", this);
            this.payloads.collectorFinisherCrash = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::collectorFinisherCrash, "collectorFinisherCrash", this);
            this.payloads.collectorAccumulatorDropSignals = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::collectorAccumulatorDropSignals, "collectorAccumulatorDropSignals", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::dispose, "dispose", this);
            this.payloads.onSubscribe = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::onSubscribe, "onSubscribe", this);
            this.payloads.basicToFlowable = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::basicToFlowable, "basicToFlowable", this);
            this.payloads.emptyToFlowable = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::emptyToFlowable, "emptyToFlowable", this);
            this.payloads.errorToFlowable = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::errorToFlowable, "errorToFlowable", this);
            this.payloads.collectorSupplierCrashToFlowable = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::collectorSupplierCrashToFlowable, "collectorSupplierCrashToFlowable", this);
            this.payloads.collectorAccumulatorCrashToFlowable = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::collectorAccumulatorCrashToFlowable, "collectorAccumulatorCrashToFlowable", this);
            this.payloads.collectorFinisherCrashToFlowable = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::collectorFinisherCrashToFlowable, "collectorFinisherCrashToFlowable", this);
            this.payloads.collectorAccumulatorDropSignalsToFlowable = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::collectorAccumulatorDropSignalsToFlowable, "collectorAccumulatorDropSignalsToFlowable", this);
            this.payloads.disposeToFlowable = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::disposeToFlowable, "disposeToFlowable", this);
            this.payloads.onSubscribeToFlowable = _ClassStatement.forPayload(FlowableCollectWithCollectorTest::onSubscribeToFlowable, "onSubscribeToFlowable", this);
        }
    }
}
