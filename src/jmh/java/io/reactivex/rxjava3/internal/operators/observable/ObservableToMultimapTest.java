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
package io.reactivex.rxjava3.internal.operators.observable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableToMultimapTest extends RxJavaTest {

    Observer<Object> objectObserver;

    SingleObserver<Object> singleObserver;

    @Before
    public void before() {
        objectObserver = TestHelper.mockObserver();
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
    public void toMultimapObservable() {
        Observable<String> source = Observable.just("a", "b", "cc", "dd");
        Observable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc).toObservable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(1, Arrays.asList("a", "b"));
        expected.put(2, Arrays.asList("cc", "dd"));
        mapped.subscribe(objectObserver);
        verify(objectObserver, never()).onError(any(Throwable.class));
        verify(objectObserver, times(1)).onNext(expected);
        verify(objectObserver, times(1)).onComplete();
    }

    @Test
    public void toMultimapWithValueSelectorObservable() {
        Observable<String> source = Observable.just("a", "b", "cc", "dd");
        Observable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, duplicate).toObservable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(1, Arrays.asList("aa", "bb"));
        expected.put(2, Arrays.asList("cccc", "dddd"));
        mapped.subscribe(objectObserver);
        verify(objectObserver, never()).onError(any(Throwable.class));
        verify(objectObserver, times(1)).onNext(expected);
        verify(objectObserver, times(1)).onComplete();
    }

    @Test
    public void toMultimapWithMapFactoryObservable() {
        Observable<String> source = Observable.just("a", "b", "cc", "dd", "eee", "fff");
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
        Observable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, identity, mapFactory, new Function<Integer, Collection<String>>() {

            @Override
            public Collection<String> apply(Integer v) {
                return new ArrayList<>();
            }
        }).toObservable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(2, Arrays.asList("cc", "dd"));
        expected.put(3, Arrays.asList("eee", "fff"));
        mapped.subscribe(objectObserver);
        verify(objectObserver, never()).onError(any(Throwable.class));
        verify(objectObserver, times(1)).onNext(expected);
        verify(objectObserver, times(1)).onComplete();
    }

    @Test
    public void toMultimapWithCollectionFactoryObservable() {
        Observable<String> source = Observable.just("cc", "dd", "eee", "eee");
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
        Observable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, identity, mapSupplier, collectionFactory).toObservable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(2, Arrays.asList("cc", "dd"));
        expected.put(3, new HashSet<>(Arrays.asList("eee")));
        mapped.subscribe(objectObserver);
        verify(objectObserver, never()).onError(any(Throwable.class));
        verify(objectObserver, times(1)).onNext(expected);
        verify(objectObserver, times(1)).onComplete();
    }

    @Test
    public void toMultimapWithErrorObservable() {
        Observable<String> source = Observable.just("a", "b", "cc", "dd");
        Function<String, Integer> lengthFuncErr = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                if ("b".equals(t1)) {
                    throw new RuntimeException("Forced Failure");
                }
                return t1.length();
            }
        };
        Observable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFuncErr).toObservable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(1, Arrays.asList("a", "b"));
        expected.put(2, Arrays.asList("cc", "dd"));
        mapped.subscribe(objectObserver);
        verify(objectObserver, times(1)).onError(any(Throwable.class));
        verify(objectObserver, never()).onNext(expected);
        verify(objectObserver, never()).onComplete();
    }

    @Test
    public void toMultimapWithErrorInValueSelectorObservable() {
        Observable<String> source = Observable.just("a", "b", "cc", "dd");
        Function<String, String> duplicateErr = new Function<String, String>() {

            @Override
            public String apply(String t1) {
                if ("b".equals(t1)) {
                    throw new RuntimeException("Forced failure");
                }
                return t1 + t1;
            }
        };
        Observable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, duplicateErr).toObservable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(1, Arrays.asList("aa", "bb"));
        expected.put(2, Arrays.asList("cccc", "dddd"));
        mapped.subscribe(objectObserver);
        verify(objectObserver, times(1)).onError(any(Throwable.class));
        verify(objectObserver, never()).onNext(expected);
        verify(objectObserver, never()).onComplete();
    }

    @Test
    public void toMultimapWithMapThrowingFactoryObservable() {
        Observable<String> source = Observable.just("a", "b", "cc", "dd", "eee", "fff");
        Supplier<Map<Integer, Collection<String>>> mapFactory = new Supplier<Map<Integer, Collection<String>>>() {

            @Override
            public Map<Integer, Collection<String>> get() {
                throw new RuntimeException("Forced failure");
            }
        };
        Observable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        }, mapFactory).toObservable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(2, Arrays.asList("cc", "dd"));
        expected.put(3, Arrays.asList("eee", "fff"));
        mapped.subscribe(objectObserver);
        verify(objectObserver, times(1)).onError(any(Throwable.class));
        verify(objectObserver, never()).onNext(expected);
        verify(objectObserver, never()).onComplete();
    }

    @Test
    public void toMultimapWithThrowingCollectionFactoryObservable() {
        Observable<String> source = Observable.just("cc", "cc", "eee", "eee");
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
        Observable<Map<Integer, Collection<String>>> mapped = source.toMultimap(lengthFunc, identity, mapSupplier, collectionFactory).toObservable();
        Map<Integer, Collection<String>> expected = new HashMap<>();
        expected.put(2, Arrays.asList("cc", "dd"));
        expected.put(3, Collections.singleton("eee"));
        mapped.subscribe(objectObserver);
        verify(objectObserver, times(1)).onError(any(Throwable.class));
        verify(objectObserver, never()).onNext(expected);
        verify(objectObserver, never()).onComplete();
    }

    @Test
    public void toMultimap() {
        Observable<String> source = Observable.just("a", "b", "cc", "dd");
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
        Observable<String> source = Observable.just("a", "b", "cc", "dd");
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
        Observable<String> source = Observable.just("a", "b", "cc", "dd", "eee", "fff");
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
            public Collection<String> apply(Integer v) {
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
        Observable<String> source = Observable.just("cc", "dd", "eee", "eee");
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
        Observable<String> source = Observable.just("a", "b", "cc", "dd");
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
        Observable<String> source = Observable.just("a", "b", "cc", "dd");
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
        Observable<String> source = Observable.just("a", "b", "cc", "dd", "eee", "fff");
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
        Observable<String> source = Observable.just("cc", "cc", "eee", "eee");
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

        private _Payloads payloads;

        private ObservableToMultimapTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapObservable() throws java.lang.Throwable {
            this.payloads.toMultimapObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithValueSelectorObservable() throws java.lang.Throwable {
            this.payloads.toMultimapWithValueSelectorObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithMapFactoryObservable() throws java.lang.Throwable {
            this.payloads.toMultimapWithMapFactoryObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithCollectionFactoryObservable() throws java.lang.Throwable {
            this.payloads.toMultimapWithCollectionFactoryObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithErrorObservable() throws java.lang.Throwable {
            this.payloads.toMultimapWithErrorObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithErrorInValueSelectorObservable() throws java.lang.Throwable {
            this.payloads.toMultimapWithErrorInValueSelectorObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithMapThrowingFactoryObservable() throws java.lang.Throwable {
            this.payloads.toMultimapWithMapThrowingFactoryObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMultimapWithThrowingCollectionFactoryObservable() throws java.lang.Throwable {
            this.payloads.toMultimapWithThrowingCollectionFactoryObservable.evaluate();
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToMultimapTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToMultimapTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToMultimapTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToMultimapTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableToMultimapTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToMultimapTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableToMultimapTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableToMultimapTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement toMultimapObservable;

            public org.junit.runners.model.Statement toMultimapWithValueSelectorObservable;

            public org.junit.runners.model.Statement toMultimapWithMapFactoryObservable;

            public org.junit.runners.model.Statement toMultimapWithCollectionFactoryObservable;

            public org.junit.runners.model.Statement toMultimapWithErrorObservable;

            public org.junit.runners.model.Statement toMultimapWithErrorInValueSelectorObservable;

            public org.junit.runners.model.Statement toMultimapWithMapThrowingFactoryObservable;

            public org.junit.runners.model.Statement toMultimapWithThrowingCollectionFactoryObservable;

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
            this.payloads.toMultimapObservable = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapObservable, "toMultimapObservable", this);
            this.payloads.toMultimapWithValueSelectorObservable = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapWithValueSelectorObservable, "toMultimapWithValueSelectorObservable", this);
            this.payloads.toMultimapWithMapFactoryObservable = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapWithMapFactoryObservable, "toMultimapWithMapFactoryObservable", this);
            this.payloads.toMultimapWithCollectionFactoryObservable = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapWithCollectionFactoryObservable, "toMultimapWithCollectionFactoryObservable", this);
            this.payloads.toMultimapWithErrorObservable = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapWithErrorObservable, "toMultimapWithErrorObservable", this);
            this.payloads.toMultimapWithErrorInValueSelectorObservable = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapWithErrorInValueSelectorObservable, "toMultimapWithErrorInValueSelectorObservable", this);
            this.payloads.toMultimapWithMapThrowingFactoryObservable = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapWithMapThrowingFactoryObservable, "toMultimapWithMapThrowingFactoryObservable", this);
            this.payloads.toMultimapWithThrowingCollectionFactoryObservable = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapWithThrowingCollectionFactoryObservable, "toMultimapWithThrowingCollectionFactoryObservable", this);
            this.payloads.toMultimap = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimap, "toMultimap", this);
            this.payloads.toMultimapWithValueSelector = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapWithValueSelector, "toMultimapWithValueSelector", this);
            this.payloads.toMultimapWithMapFactory = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapWithMapFactory, "toMultimapWithMapFactory", this);
            this.payloads.toMultimapWithCollectionFactory = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapWithCollectionFactory, "toMultimapWithCollectionFactory", this);
            this.payloads.toMultimapWithError = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapWithError, "toMultimapWithError", this);
            this.payloads.toMultimapWithErrorInValueSelector = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapWithErrorInValueSelector, "toMultimapWithErrorInValueSelector", this);
            this.payloads.toMultimapWithMapThrowingFactory = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapWithMapThrowingFactory, "toMultimapWithMapThrowingFactory", this);
            this.payloads.toMultimapWithThrowingCollectionFactory = _ClassStatement.forPayload(ObservableToMultimapTest::toMultimapWithThrowingCollectionFactory, "toMultimapWithThrowingCollectionFactory", this);
        }
    }
}
