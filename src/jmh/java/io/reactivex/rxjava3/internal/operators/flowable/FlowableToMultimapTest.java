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
import java.util.*;
import org.junit.*;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableToMultimapTest extends RxJavaTest {

    Subscriber<Object> objectSubscriber;

    SingleObserver<Object> singleObserver;

    @Before
    public void before() {
        objectSubscriber = TestHelper.mockSubscriber();
        singleObserver = TestHelper.mockSingleObserver();
    }

    Function<String, Integer> lengthFunc = new Function<String, Integer>() {

        @Override
        public Integer apply(String t1) {
            return t1.length();
        }
    };

    Function<String, String> duplicate = new Function<String, String>() {

        @Override
        public String apply(String t1) {
            return t1 + t1;
        }
    };

    @Test
    public void toMultimapFlowable() {
        Flowable<String> source = Flowable.just("a", "b", "cc", "dd");
        Flowable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc).toFlowable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(1, Arrays.asList("a", "b"));
        expected.put(2, Arrays.asList("cc", "dd"));
        mapped.subscribe(objectSubscriber);
        verify(objectSubscriber, never()).onError(any(Throwable.class));
        verify(objectSubscriber, times(1)).onNext(expected);
        verify(objectSubscriber, times(1)).onComplete();
    }

    @Test
    public void toMultimapWithValueSelectorFlowable() {
        Flowable<String> source = Flowable.just("a", "b", "cc", "dd");
        Flowable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, duplicate).toFlowable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(1, Arrays.asList("aa", "bb"));
        expected.put(2, Arrays.asList("cccc", "dddd"));
        mapped.subscribe(objectSubscriber);
        verify(objectSubscriber, never()).onError(any(Throwable.class));
        verify(objectSubscriber, times(1)).onNext(expected);
        verify(objectSubscriber, times(1)).onComplete();
    }

    @Test
    public void toMultimapWithMapFactoryFlowable() {
        Flowable<String> source = Flowable.just("a", "b", "cc", "dd", "eee", "fff");
        Supplier<Map<Integer, Collection<String>>> mapFactory = new Supplier<Map<Integer, Collection<String>>>() {

            @Override
            public Map<Integer, Collection<String>> get() {
                return new LinkedHashMap<Integer, Collection<String>>() {

                    private static final long serialVersionUID = -2084477070717362859L;

                    @Override
                    protected boolean removeEldestEntry(Map.Entry<Integer, Collection<String>> eldest) {
                        return size() > 2;
                    }
                };
            }
        };
        Function<String, String> identity = new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        };
        Flowable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, identity, mapFactory, new Function<Integer, Collection<String>>() {

            @Override
            public Collection<String> apply(Integer e) {
                return new ArrayList<>();
            }
        }).toFlowable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(2, Arrays.asList("cc", "dd"));
        expected.put(3, Arrays.asList("eee", "fff"));
        mapped.subscribe(objectSubscriber);
        verify(objectSubscriber, never()).onError(any(Throwable.class));
        verify(objectSubscriber, times(1)).onNext(expected);
        verify(objectSubscriber, times(1)).onComplete();
    }

    @Test
    public void toMultimapWithCollectionFactoryFlowable() {
        Flowable<String> source = Flowable.just("cc", "dd", "eee", "eee");
        Function<Integer, Collection<String>> collectionFactory = new Function<Integer, Collection<String>>() {

            @Override
            public Collection<String> apply(Integer t1) {
                if (t1 == 2) {
                    return new ArrayList<>();
                } else {
                    return new HashSet<>();
                }
            }
        };
        Function<String, String> identity = new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        };
        Supplier<Map<Integer, Collection<String>>> mapSupplier = new Supplier<Map<Integer, Collection<String>>>() {

            @Override
            public Map<Integer, Collection<String>> get() {
                return new HashMap<>();
            }
        };
        Flowable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, identity, mapSupplier, collectionFactory).toFlowable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(2, Arrays.asList("cc", "dd"));
        expected.put(3, new HashSet<>(Arrays.asList("eee")));
        mapped.subscribe(objectSubscriber);
        verify(objectSubscriber, never()).onError(any(Throwable.class));
        verify(objectSubscriber, times(1)).onNext(expected);
        verify(objectSubscriber, times(1)).onComplete();
    }

    @Test
    public void toMultimapWithErrorFlowable() {
        Flowable<String> source = Flowable.just("a", "b", "cc", "dd");
        Function<String, Integer> lengthFuncErr = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                if ("b".equals(t1)) {
                    throw new RuntimeException("Forced Failure");
                }
                return t1.length();
            }
        };
        Flowable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFuncErr).toFlowable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(1, Arrays.asList("a", "b"));
        expected.put(2, Arrays.asList("cc", "dd"));
        mapped.subscribe(objectSubscriber);
        verify(objectSubscriber, times(1)).onError(any(Throwable.class));
        verify(objectSubscriber, never()).onNext(expected);
        verify(objectSubscriber, never()).onComplete();
    }

    @Test
    public void toMultimapWithErrorInValueSelectorFlowable() {
        Flowable<String> source = Flowable.just("a", "b", "cc", "dd");
        Function<String, String> duplicateErr = new Function<String, String>() {

            @Override
            public String apply(String t1) {
                if ("b".equals(t1)) {
                    throw new RuntimeException("Forced failure");
                }
                return t1 + t1;
            }
        };
        Flowable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, duplicateErr).toFlowable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(1, Arrays.asList("aa", "bb"));
        expected.put(2, Arrays.asList("cccc", "dddd"));
        mapped.subscribe(objectSubscriber);
        verify(objectSubscriber, times(1)).onError(any(Throwable.class));
        verify(objectSubscriber, never()).onNext(expected);
        verify(objectSubscriber, never()).onComplete();
    }

    @Test
    public void toMultimapWithMapThrowingFactoryFlowable() {
        Flowable<String> source = Flowable.just("a", "b", "cc", "dd", "eee", "fff");
        Supplier<Map<Integer, Collection<String>>> mapFactory = new Supplier<Map<Integer, Collection<String>>>() {

            @Override
            public Map<Integer, Collection<String>> get() {
                throw new RuntimeException("Forced failure");
            }
        };
        Flowable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        }, mapFactory).toFlowable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(2, Arrays.asList("cc", "dd"));
        expected.put(3, Arrays.asList("eee", "fff"));
        mapped.subscribe(objectSubscriber);
        verify(objectSubscriber, times(1)).onError(any(Throwable.class));
        verify(objectSubscriber, never()).onNext(expected);
        verify(objectSubscriber, never()).onComplete();
    }

    @Test
    public void toMultimapWithThrowingCollectionFactoryFlowable() {
        Flowable<String> source = Flowable.just("cc", "cc", "eee", "eee");
        Function<Integer, Collection<String>> collectionFactory = new Function<Integer, Collection<String>>() {

            @Override
            public Collection<String> apply(Integer t1) {
                if (t1 == 2) {
                    throw new RuntimeException("Forced failure");
                } else {
                    return new HashSet<>();
                }
            }
        };
        Function<String, String> identity = new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        };
        Supplier<Map<Integer, Collection<String>>> mapSupplier = new Supplier<Map<Integer, Collection<String>>>() {

            @Override
            public Map<Integer, Collection<String>> get() {
                return new HashMap<>();
            }
        };
        Flowable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, identity, mapSupplier, collectionFactory).toFlowable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(2, Arrays.asList("cc", "dd"));
        expected.put(3, Collections.singleton("eee"));
        mapped.subscribe(objectSubscriber);
        verify(objectSubscriber, times(1)).onError(any(Throwable.class));
        verify(objectSubscriber, never()).onNext(expected);
        verify(objectSubscriber, never()).onComplete();
    }

    @Test
    public void toMultimap() {
        Flowable<String> source = Flowable.just("a", "b", "cc", "dd");
        Single<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc);
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(1, Arrays.asList("a", "b"));
        expected.put(2, Arrays.asList("cc", "dd"));
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onError(any(Throwable.class));
        verify(singleObserver, times(1)).onSuccess(expected);
    }

    @Test
    public void toMultimapWithValueSelector() {
        Flowable<String> source = Flowable.just("a", "b", "cc", "dd");
        Single<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, duplicate);
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(1, Arrays.asList("aa", "bb"));
        expected.put(2, Arrays.asList("cccc", "dddd"));
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onError(any(Throwable.class));
        verify(singleObserver, times(1)).onSuccess(expected);
    }

    @Test
    public void toMultimapWithMapFactory() {
        Flowable<String> source = Flowable.just("a", "b", "cc", "dd", "eee", "fff");
        Supplier<Map<Integer, Collection<String>>> mapFactory = new Supplier<Map<Integer, Collection<String>>>() {

            @Override
            public Map<Integer, Collection<String>> get() {
                return new LinkedHashMap<Integer, Collection<String>>() {

                    private static final long serialVersionUID = -2084477070717362859L;

                    @Override
                    protected boolean removeEldestEntry(Map.Entry<Integer, Collection<String>> eldest) {
                        return size() > 2;
                    }
                };
            }
        };
        Function<String, String> identity = new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        };
        Single<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, identity, mapFactory, new Function<Integer, Collection<String>>() {

            @Override
            public Collection<String> apply(Integer e) {
                return new ArrayList<>();
            }
        });
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(2, Arrays.asList("cc", "dd"));
        expected.put(3, Arrays.asList("eee", "fff"));
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onError(any(Throwable.class));
        verify(singleObserver, times(1)).onSuccess(expected);
    }

    @Test
    public void toMultimapWithCollectionFactory() {
        Flowable<String> source = Flowable.just("cc", "dd", "eee", "eee");
        Function<Integer, Collection<String>> collectionFactory = new Function<Integer, Collection<String>>() {

            @Override
            public Collection<String> apply(Integer t1) {
                if (t1 == 2) {
                    return new ArrayList<>();
                } else {
                    return new HashSet<>();
                }
            }
        };
        Function<String, String> identity = new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        };
        Supplier<Map<Integer, Collection<String>>> mapSupplier = new Supplier<Map<Integer, Collection<String>>>() {

            @Override
            public Map<Integer, Collection<String>> get() {
                return new HashMap<>();
            }
        };
        Single<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, identity, mapSupplier, collectionFactory);
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(2, Arrays.asList("cc", "dd"));
        expected.put(3, new HashSet<>(Arrays.asList("eee")));
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onError(any(Throwable.class));
        verify(singleObserver, times(1)).onSuccess(expected);
    }

    @Test
    public void toMultimapWithError() {
        Flowable<String> source = Flowable.just("a", "b", "cc", "dd");
        Function<String, Integer> lengthFuncErr = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                if ("b".equals(t1)) {
                    throw new RuntimeException("Forced Failure");
                }
                return t1.length();
            }
        };
        Single<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFuncErr);
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(1, Arrays.asList("a", "b"));
        expected.put(2, Arrays.asList("cc", "dd"));
        mapped.subscribe(singleObserver);
        verify(singleObserver, times(1)).onError(any(Throwable.class));
        verify(singleObserver, never()).onSuccess(expected);
    }

    @Test
    public void toMultimapWithErrorInValueSelector() {
        Flowable<String> source = Flowable.just("a", "b", "cc", "dd");
        Function<String, String> duplicateErr = new Function<String, String>() {

            @Override
            public String apply(String t1) {
                if ("b".equals(t1)) {
                    throw new RuntimeException("Forced failure");
                }
                return t1 + t1;
            }
        };
        Single<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, duplicateErr);
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(1, Arrays.asList("aa", "bb"));
        expected.put(2, Arrays.asList("cccc", "dddd"));
        mapped.subscribe(singleObserver);
        verify(singleObserver, times(1)).onError(any(Throwable.class));
        verify(singleObserver, never()).onSuccess(expected);
    }

    @Test
    public void toMultimapWithMapThrowingFactory() {
        Flowable<String> source = Flowable.just("a", "b", "cc", "dd", "eee", "fff");
        Supplier<Map<Integer, Collection<String>>> mapFactory = new Supplier<Map<Integer, Collection<String>>>() {

            @Override
            public Map<Integer, Collection<String>> get() {
                throw new RuntimeException("Forced failure");
            }
        };
        Single<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        }, mapFactory);
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(2, Arrays.asList("cc", "dd"));
        expected.put(3, Arrays.asList("eee", "fff"));
        mapped.subscribe(singleObserver);
        verify(singleObserver, times(1)).onError(any(Throwable.class));
        verify(singleObserver, never()).onSuccess(expected);
    }

    @Test
    public void toMultimapWithThrowingCollectionFactory() {
        Flowable<String> source = Flowable.just("cc", "cc", "eee", "eee");
        Function<Integer, Collection<String>> collectionFactory = new Function<Integer, Collection<String>>() {

            @Override
            public Collection<String> apply(Integer t1) {
                if (t1 == 2) {
                    throw new RuntimeException("Forced failure");
                } else {
                    return new HashSet<>();
                }
            }
        };
        Function<String, String> identity = new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        };
        Supplier<Map<Integer, Collection<String>>> mapSupplier = new Supplier<Map<Integer, Collection<String>>>() {

            @Override
            public Map<Integer, Collection<String>> get() {
                return new HashMap<>();
            }
        };
        Single<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, identity, mapSupplier, collectionFactory);
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(2, Arrays.asList("cc", "dd"));
        expected.put(3, Collections.singleton("eee"));
        mapped.subscribe(singleObserver);
        verify(singleObserver, times(1)).onError(any(Throwable.class));
        verify(singleObserver, never()).onSuccess(expected);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableToMultimapTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapFlowable() throws java.lang.Throwable {
            this.payloads.toMultimapFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithValueSelectorFlowable() throws java.lang.Throwable {
            this.payloads.toMultimapWithValueSelectorFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithMapFactoryFlowable() throws java.lang.Throwable {
            this.payloads.toMultimapWithMapFactoryFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithCollectionFactoryFlowable() throws java.lang.Throwable {
            this.payloads.toMultimapWithCollectionFactoryFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithErrorFlowable() throws java.lang.Throwable {
            this.payloads.toMultimapWithErrorFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithErrorInValueSelectorFlowable() throws java.lang.Throwable {
            this.payloads.toMultimapWithErrorInValueSelectorFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithMapThrowingFactoryFlowable() throws java.lang.Throwable {
            this.payloads.toMultimapWithMapThrowingFactoryFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithThrowingCollectionFactoryFlowable() throws java.lang.Throwable {
            this.payloads.toMultimapWithThrowingCollectionFactoryFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimap() throws java.lang.Throwable {
            this.payloads.toMultimap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithValueSelector() throws java.lang.Throwable {
            this.payloads.toMultimapWithValueSelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithMapFactory() throws java.lang.Throwable {
            this.payloads.toMultimapWithMapFactory.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithCollectionFactory() throws java.lang.Throwable {
            this.payloads.toMultimapWithCollectionFactory.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithError() throws java.lang.Throwable {
            this.payloads.toMultimapWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithErrorInValueSelector() throws java.lang.Throwable {
            this.payloads.toMultimapWithErrorInValueSelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithMapThrowingFactory() throws java.lang.Throwable {
            this.payloads.toMultimapWithMapThrowingFactory.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithThrowingCollectionFactory() throws java.lang.Throwable {
            this.payloads.toMultimapWithThrowingCollectionFactory.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToMultimapTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToMultimapTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToMultimapTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToMultimapTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableToMultimapTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToMultimapTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableToMultimapTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableToMultimapTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement toMultimapFlowable;

            public org.junit.runners.model.Statement toMultimapWithValueSelectorFlowable;

            public org.junit.runners.model.Statement toMultimapWithMapFactoryFlowable;

            public org.junit.runners.model.Statement toMultimapWithCollectionFactoryFlowable;

            public org.junit.runners.model.Statement toMultimapWithErrorFlowable;

            public org.junit.runners.model.Statement toMultimapWithErrorInValueSelectorFlowable;

            public org.junit.runners.model.Statement toMultimapWithMapThrowingFactoryFlowable;

            public org.junit.runners.model.Statement toMultimapWithThrowingCollectionFactoryFlowable;

            public org.junit.runners.model.Statement toMultimap;

            public org.junit.runners.model.Statement toMultimapWithValueSelector;

            public org.junit.runners.model.Statement toMultimapWithMapFactory;

            public org.junit.runners.model.Statement toMultimapWithCollectionFactory;

            public org.junit.runners.model.Statement toMultimapWithError;

            public org.junit.runners.model.Statement toMultimapWithErrorInValueSelector;

            public org.junit.runners.model.Statement toMultimapWithMapThrowingFactory;

            public org.junit.runners.model.Statement toMultimapWithThrowingCollectionFactory;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.toMultimapFlowable = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapFlowable, "toMultimapFlowable", this);
            this.payloads.toMultimapWithValueSelectorFlowable = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapWithValueSelectorFlowable, "toMultimapWithValueSelectorFlowable", this);
            this.payloads.toMultimapWithMapFactoryFlowable = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapWithMapFactoryFlowable, "toMultimapWithMapFactoryFlowable", this);
            this.payloads.toMultimapWithCollectionFactoryFlowable = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapWithCollectionFactoryFlowable, "toMultimapWithCollectionFactoryFlowable", this);
            this.payloads.toMultimapWithErrorFlowable = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapWithErrorFlowable, "toMultimapWithErrorFlowable", this);
            this.payloads.toMultimapWithErrorInValueSelectorFlowable = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapWithErrorInValueSelectorFlowable, "toMultimapWithErrorInValueSelectorFlowable", this);
            this.payloads.toMultimapWithMapThrowingFactoryFlowable = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapWithMapThrowingFactoryFlowable, "toMultimapWithMapThrowingFactoryFlowable", this);
            this.payloads.toMultimapWithThrowingCollectionFactoryFlowable = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapWithThrowingCollectionFactoryFlowable, "toMultimapWithThrowingCollectionFactoryFlowable", this);
            this.payloads.toMultimap = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimap, "toMultimap", this);
            this.payloads.toMultimapWithValueSelector = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapWithValueSelector, "toMultimapWithValueSelector", this);
            this.payloads.toMultimapWithMapFactory = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapWithMapFactory, "toMultimapWithMapFactory", this);
            this.payloads.toMultimapWithCollectionFactory = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapWithCollectionFactory, "toMultimapWithCollectionFactory", this);
            this.payloads.toMultimapWithError = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapWithError, "toMultimapWithError", this);
            this.payloads.toMultimapWithErrorInValueSelector = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapWithErrorInValueSelector, "toMultimapWithErrorInValueSelector", this);
            this.payloads.toMultimapWithMapThrowingFactory = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapWithMapThrowingFactory, "toMultimapWithMapThrowingFactory", this);
            this.payloads.toMultimapWithThrowingCollectionFactory = _ClassStatement.forPayload(FlowableToMultimapTest::toMultimapWithThrowingCollectionFactory, "toMultimapWithThrowingCollectionFactory", this);
        }
    }
}
