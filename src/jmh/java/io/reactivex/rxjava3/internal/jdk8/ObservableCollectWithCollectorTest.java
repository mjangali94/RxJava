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
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableCollectWithCollectorTest extends RxJavaTest {

    @Test
    public void basic() {
        Observable.range(1, 5).collect(Collectors.toList()).test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void empty() {
        Observable.empty().collect(Collectors.toList()).test().assertResult(Collections.emptyList());
    }

    @Test
    public void error() {
        Observable.error(new TestException()).collect(Collectors.toList()).test().assertFailure(TestException.class);
    }

    @Test
    public void collectorSupplierCrash() {
        Observable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

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
        Observable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

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
            Observable<Integer> source = new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onError(new IOException());
                    observer.onComplete();
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
        TestHelper.checkDisposed(PublishSubject.create().collect(Collectors.toList()));
    }

    @Test
    public void onSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToSingle(f -> f.collect(Collectors.toList()));
    }

    @Test
    public void basicToObservable() {
        Observable.range(1, 5).collect(Collectors.toList()).toObservable().test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void emptyToObservable() {
        Observable.empty().collect(Collectors.toList()).toObservable().test().assertResult(Collections.emptyList());
    }

    @Test
    public void errorToObservable() {
        Observable.error(new TestException()).collect(Collectors.toList()).toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void collectorSupplierCrashToObservable() {
        Observable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

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
        }).toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void collectorAccumulatorCrashToObservable() {
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
        }).toObservable().test().assertFailure(TestException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void collectorFinisherCrashToObservable() {
        Observable.range(1, 5).collect(new Collector<Integer, Integer, Integer>() {

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
        }).toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void collectorAccumulatorDropSignalsToObservable() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Observable<Integer> source = new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onNext(2);
                    observer.onError(new IOException());
                    observer.onComplete();
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
            }).toObservable().test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        });
    }

    @Test
    public void disposeToObservable() {
        TestHelper.checkDisposed(PublishProcessor.create().collect(Collectors.toList()).toObservable());
    }

    @Test
    public void onSubscribeToObservable() {
        TestHelper.checkDoubleOnSubscribeObservable(f -> f.collect(Collectors.toList()).toObservable());
    }

    @Test
    public void toObservableTake() {
        Observable.range(1, 5).collect(Collectors.toList()).toObservable().take(1).test().assertResult(Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void disposeBeforeEnd() {
        TestObserver<List<Integer>> to = Observable.range(1, 5).concatWith(Observable.never()).collect(Collectors.toList()).test();
        to.dispose();
        to.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableCollectWithCollectorTest instance;

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
        public void benchmark_basicToObservable() throws java.lang.Throwable {
            this.payloads.basicToObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyToObservable() throws java.lang.Throwable {
            this.payloads.emptyToObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorToObservable() throws java.lang.Throwable {
            this.payloads.errorToObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorSupplierCrashToObservable() throws java.lang.Throwable {
            this.payloads.collectorSupplierCrashToObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorAccumulatorCrashToObservable() throws java.lang.Throwable {
            this.payloads.collectorAccumulatorCrashToObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFinisherCrashToObservable() throws java.lang.Throwable {
            this.payloads.collectorFinisherCrashToObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorAccumulatorDropSignalsToObservable() throws java.lang.Throwable {
            this.payloads.collectorAccumulatorDropSignalsToObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeToObservable() throws java.lang.Throwable {
            this.payloads.disposeToObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeToObservable() throws java.lang.Throwable {
            this.payloads.onSubscribeToObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservableTake() throws java.lang.Throwable {
            this.payloads.toObservableTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeBeforeEnd() throws java.lang.Throwable {
            this.payloads.disposeBeforeEnd.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableCollectWithCollectorTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableCollectWithCollectorTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableCollectWithCollectorTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableCollectWithCollectorTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableCollectWithCollectorTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableCollectWithCollectorTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableCollectWithCollectorTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableCollectWithCollectorTest.class, name);
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

            public org.junit.runners.model.Statement basicToObservable;

            public org.junit.runners.model.Statement emptyToObservable;

            public org.junit.runners.model.Statement errorToObservable;

            public org.junit.runners.model.Statement collectorSupplierCrashToObservable;

            public org.junit.runners.model.Statement collectorAccumulatorCrashToObservable;

            public org.junit.runners.model.Statement collectorFinisherCrashToObservable;

            public org.junit.runners.model.Statement collectorAccumulatorDropSignalsToObservable;

            public org.junit.runners.model.Statement disposeToObservable;

            public org.junit.runners.model.Statement onSubscribeToObservable;

            public org.junit.runners.model.Statement toObservableTake;

            public org.junit.runners.model.Statement disposeBeforeEnd;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.basic = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::basic, "basic", this);
            this.payloads.empty = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::empty, "empty", this);
            this.payloads.error = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::error, "error", this);
            this.payloads.collectorSupplierCrash = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::collectorSupplierCrash, "collectorSupplierCrash", this);
            this.payloads.collectorAccumulatorCrash = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::collectorAccumulatorCrash, "collectorAccumulatorCrash", this);
            this.payloads.collectorFinisherCrash = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::collectorFinisherCrash, "collectorFinisherCrash", this);
            this.payloads.collectorAccumulatorDropSignals = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::collectorAccumulatorDropSignals, "collectorAccumulatorDropSignals", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::dispose, "dispose", this);
            this.payloads.onSubscribe = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::onSubscribe, "onSubscribe", this);
            this.payloads.basicToObservable = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::basicToObservable, "basicToObservable", this);
            this.payloads.emptyToObservable = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::emptyToObservable, "emptyToObservable", this);
            this.payloads.errorToObservable = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::errorToObservable, "errorToObservable", this);
            this.payloads.collectorSupplierCrashToObservable = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::collectorSupplierCrashToObservable, "collectorSupplierCrashToObservable", this);
            this.payloads.collectorAccumulatorCrashToObservable = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::collectorAccumulatorCrashToObservable, "collectorAccumulatorCrashToObservable", this);
            this.payloads.collectorFinisherCrashToObservable = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::collectorFinisherCrashToObservable, "collectorFinisherCrashToObservable", this);
            this.payloads.collectorAccumulatorDropSignalsToObservable = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::collectorAccumulatorDropSignalsToObservable, "collectorAccumulatorDropSignalsToObservable", this);
            this.payloads.disposeToObservable = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::disposeToObservable, "disposeToObservable", this);
            this.payloads.onSubscribeToObservable = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::onSubscribeToObservable, "onSubscribeToObservable", this);
            this.payloads.toObservableTake = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::toObservableTake, "toObservableTake", this);
            this.payloads.disposeBeforeEnd = _ClassStatement.forPayload(ObservableCollectWithCollectorTest::disposeBeforeEnd, "disposeBeforeEnd", this);
        }
    }
}
