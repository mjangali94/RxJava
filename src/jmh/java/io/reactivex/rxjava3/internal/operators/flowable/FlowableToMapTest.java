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

public class FlowableToMapTest extends RxJavaTest {

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
    public void toMapFlowable() {
        Flowable<String> source = Flowable.just("a", "bb", "ccc", "dddd");
        Flowable<Map<Integer, String>> mapped = source.toMap(lengthFunc).toFlowable();
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "a");
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(objectSubscriber);
        verify(objectSubscriber, never()).onError(any(Throwable.class));
        verify(objectSubscriber, times(1)).onNext(expected);
        verify(objectSubscriber, times(1)).onComplete();
    }

    @Test
    public void toMapWithValueSelectorFlowable() {
        Flowable<String> source = Flowable.just("a", "bb", "ccc", "dddd");
        Flowable<Map<Integer, String>> mapped = source.toMap(lengthFunc, duplicate).toFlowable();
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "aa");
        expected.put(2, "bbbb");
        expected.put(3, "cccccc");
        expected.put(4, "dddddddd");
        mapped.subscribe(objectSubscriber);
        verify(objectSubscriber, never()).onError(any(Throwable.class));
        verify(objectSubscriber, times(1)).onNext(expected);
        verify(objectSubscriber, times(1)).onComplete();
    }

    @Test
    public void toMapWithErrorFlowable() {
        Flowable<String> source = Flowable.just("a", "bb", "ccc", "dddd");
        Function<String, Integer> lengthFuncErr = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                if ("bb".equals(t1)) {
                    throw new RuntimeException("Forced Failure");
                }
                return t1.length();
            }
        };
        Flowable<Map<Integer, String>> mapped = source.toMap(lengthFuncErr).toFlowable();
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "a");
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(objectSubscriber);
        verify(objectSubscriber, never()).onNext(expected);
        verify(objectSubscriber, never()).onComplete();
        verify(objectSubscriber, times(1)).onError(any(Throwable.class));
    }

    @Test
    public void toMapWithErrorInValueSelectorFlowable() {
        Flowable<String> source = Flowable.just("a", "bb", "ccc", "dddd");
        Function<String, String> duplicateErr = new Function<String, String>() {

            @Override
            public String apply(String t1) {
                if ("bb".equals(t1)) {
                    throw new RuntimeException("Forced failure");
                }
                return t1 + t1;
            }
        };
        Flowable<Map<Integer, String>> mapped = source.toMap(lengthFunc, duplicateErr).toFlowable();
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "aa");
        expected.put(2, "bbbb");
        expected.put(3, "cccccc");
        expected.put(4, "dddddddd");
        mapped.subscribe(objectSubscriber);
        verify(objectSubscriber, never()).onNext(expected);
        verify(objectSubscriber, never()).onComplete();
        verify(objectSubscriber, times(1)).onError(any(Throwable.class));
    }

    @Test
    public void toMapWithFactoryFlowable() {
        Flowable<String> source = Flowable.just("a", "bb", "ccc", "dddd");
        Supplier<Map<Integer, String>> mapFactory = new Supplier<Map<Integer, String>>() {

            @Override
            public Map<Integer, String> get() {
                return new LinkedHashMap<Integer, String>() {

                    private static final long serialVersionUID = -3296811238780863394L;

                    @Override
                    protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                        return size() > 3;
                    }
                };
            }
        };
        Function<String, Integer> lengthFunc = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                return t1.length();
            }
        };
        Flowable<Map<Integer, String>> mapped = source.toMap(lengthFunc, new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        }, mapFactory).toFlowable();
        Map<Integer, String> expected = new LinkedHashMap<>();
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(objectSubscriber);
        verify(objectSubscriber, never()).onError(any(Throwable.class));
        verify(objectSubscriber, times(1)).onNext(expected);
        verify(objectSubscriber, times(1)).onComplete();
    }

    @Test
    public void toMapWithErrorThrowingFactoryFlowable() {
        Flowable<String> source = Flowable.just("a", "bb", "ccc", "dddd");
        Supplier<Map<Integer, String>> mapFactory = new Supplier<Map<Integer, String>>() {

            @Override
            public Map<Integer, String> get() {
                throw new RuntimeException("Forced failure");
            }
        };
        Function<String, Integer> lengthFunc = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                return t1.length();
            }
        };
        Flowable<Map<Integer, String>> mapped = source.toMap(lengthFunc, new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        }, mapFactory).toFlowable();
        Map<Integer, String> expected = new LinkedHashMap<>();
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(objectSubscriber);
        verify(objectSubscriber, never()).onNext(expected);
        verify(objectSubscriber, never()).onComplete();
        verify(objectSubscriber, times(1)).onError(any(Throwable.class));
    }

    @Test
    public void toMap() {
        Flowable<String> source = Flowable.just("a", "bb", "ccc", "dddd");
        Single<Map<Integer, String>> mapped = source.toMap(lengthFunc);
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "a");
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onError(any(Throwable.class));
        verify(singleObserver, times(1)).onSuccess(expected);
    }

    @Test
    public void toMapWithValueSelector() {
        Flowable<String> source = Flowable.just("a", "bb", "ccc", "dddd");
        Single<Map<Integer, String>> mapped = source.toMap(lengthFunc, duplicate);
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "aa");
        expected.put(2, "bbbb");
        expected.put(3, "cccccc");
        expected.put(4, "dddddddd");
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onError(any(Throwable.class));
        verify(singleObserver, times(1)).onSuccess(expected);
    }

    @Test
    public void toMapWithError() {
        Flowable<String> source = Flowable.just("a", "bb", "ccc", "dddd");
        Function<String, Integer> lengthFuncErr = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                if ("bb".equals(t1)) {
                    throw new RuntimeException("Forced Failure");
                }
                return t1.length();
            }
        };
        Single<Map<Integer, String>> mapped = source.toMap(lengthFuncErr);
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "a");
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onSuccess(expected);
        verify(singleObserver, times(1)).onError(any(Throwable.class));
    }

    @Test
    public void toMapWithErrorInValueSelector() {
        Flowable<String> source = Flowable.just("a", "bb", "ccc", "dddd");
        Function<String, String> duplicateErr = new Function<String, String>() {

            @Override
            public String apply(String t1) {
                if ("bb".equals(t1)) {
                    throw new RuntimeException("Forced failure");
                }
                return t1 + t1;
            }
        };
        Single<Map<Integer, String>> mapped = source.toMap(lengthFunc, duplicateErr);
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "aa");
        expected.put(2, "bbbb");
        expected.put(3, "cccccc");
        expected.put(4, "dddddddd");
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onSuccess(expected);
        verify(singleObserver, times(1)).onError(any(Throwable.class));
    }

    @Test
    public void toMapWithFactory() {
        Flowable<String> source = Flowable.just("a", "bb", "ccc", "dddd");
        Supplier<Map<Integer, String>> mapFactory = new Supplier<Map<Integer, String>>() {

            @Override
            public Map<Integer, String> get() {
                return new LinkedHashMap<Integer, String>() {

                    private static final long serialVersionUID = -3296811238780863394L;

                    @Override
                    protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                        return size() > 3;
                    }
                };
            }
        };
        Function<String, Integer> lengthFunc = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                return t1.length();
            }
        };
        Single<Map<Integer, String>> mapped = source.toMap(lengthFunc, new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        }, mapFactory);
        Map<Integer, String> expected = new LinkedHashMap<>();
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onError(any(Throwable.class));
        verify(singleObserver, times(1)).onSuccess(expected);
    }

    @Test
    public void toMapWithErrorThrowingFactory() {
        Flowable<String> source = Flowable.just("a", "bb", "ccc", "dddd");
        Supplier<Map<Integer, String>> mapFactory = new Supplier<Map<Integer, String>>() {

            @Override
            public Map<Integer, String> get() {
                throw new RuntimeException("Forced failure");
            }
        };
        Function<String, Integer> lengthFunc = new Function<String, Integer>() {

            @Override
            public Integer apply(String t1) {
                return t1.length();
            }
        };
        Single<Map<Integer, String>> mapped = source.toMap(lengthFunc, new Function<String, String>() {

            @Override
            public String apply(String v) {
                return v;
            }
        }, mapFactory);
        Map<Integer, String> expected = new LinkedHashMap<>();
        expected.put(2, "bb");
        expected.put(3, "ccc");
        expected.put(4, "dddd");
        mapped.subscribe(singleObserver);
        verify(singleObserver, never()).onSuccess(expected);
        verify(singleObserver, times(1)).onError(any(Throwable.class));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableToMapTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapFlowable() throws java.lang.Throwable {
            this.payloads.toMapFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithValueSelectorFlowable() throws java.lang.Throwable {
            this.payloads.toMapWithValueSelectorFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithErrorFlowable() throws java.lang.Throwable {
            this.payloads.toMapWithErrorFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithErrorInValueSelectorFlowable() throws java.lang.Throwable {
            this.payloads.toMapWithErrorInValueSelectorFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithFactoryFlowable() throws java.lang.Throwable {
            this.payloads.toMapWithFactoryFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithErrorThrowingFactoryFlowable() throws java.lang.Throwable {
            this.payloads.toMapWithErrorThrowingFactoryFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMap() throws java.lang.Throwable {
            this.payloads.toMap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithValueSelector() throws java.lang.Throwable {
            this.payloads.toMapWithValueSelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithError() throws java.lang.Throwable {
            this.payloads.toMapWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithErrorInValueSelector() throws java.lang.Throwable {
            this.payloads.toMapWithErrorInValueSelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithFactory() throws java.lang.Throwable {
            this.payloads.toMapWithFactory.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toMapWithErrorThrowingFactory() throws java.lang.Throwable {
            this.payloads.toMapWithErrorThrowingFactory.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToMapTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToMapTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToMapTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToMapTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableToMapTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableToMapTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableToMapTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableToMapTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement toMapFlowable;

            public org.junit.runners.model.Statement toMapWithValueSelectorFlowable;

            public org.junit.runners.model.Statement toMapWithErrorFlowable;

            public org.junit.runners.model.Statement toMapWithErrorInValueSelectorFlowable;

            public org.junit.runners.model.Statement toMapWithFactoryFlowable;

            public org.junit.runners.model.Statement toMapWithErrorThrowingFactoryFlowable;

            public org.junit.runners.model.Statement toMap;

            public org.junit.runners.model.Statement toMapWithValueSelector;

            public org.junit.runners.model.Statement toMapWithError;

            public org.junit.runners.model.Statement toMapWithErrorInValueSelector;

            public org.junit.runners.model.Statement toMapWithFactory;

            public org.junit.runners.model.Statement toMapWithErrorThrowingFactory;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.toMapFlowable = _ClassStatement.forPayload(FlowableToMapTest::toMapFlowable, "toMapFlowable", this);
            this.payloads.toMapWithValueSelectorFlowable = _ClassStatement.forPayload(FlowableToMapTest::toMapWithValueSelectorFlowable, "toMapWithValueSelectorFlowable", this);
            this.payloads.toMapWithErrorFlowable = _ClassStatement.forPayload(FlowableToMapTest::toMapWithErrorFlowable, "toMapWithErrorFlowable", this);
            this.payloads.toMapWithErrorInValueSelectorFlowable = _ClassStatement.forPayload(FlowableToMapTest::toMapWithErrorInValueSelectorFlowable, "toMapWithErrorInValueSelectorFlowable", this);
            this.payloads.toMapWithFactoryFlowable = _ClassStatement.forPayload(FlowableToMapTest::toMapWithFactoryFlowable, "toMapWithFactoryFlowable", this);
            this.payloads.toMapWithErrorThrowingFactoryFlowable = _ClassStatement.forPayload(FlowableToMapTest::toMapWithErrorThrowingFactoryFlowable, "toMapWithErrorThrowingFactoryFlowable", this);
            this.payloads.toMap = _ClassStatement.forPayload(FlowableToMapTest::toMap, "toMap", this);
            this.payloads.toMapWithValueSelector = _ClassStatement.forPayload(FlowableToMapTest::toMapWithValueSelector, "toMapWithValueSelector", this);
            this.payloads.toMapWithError = _ClassStatement.forPayload(FlowableToMapTest::toMapWithError, "toMapWithError", this);
            this.payloads.toMapWithErrorInValueSelector = _ClassStatement.forPayload(FlowableToMapTest::toMapWithErrorInValueSelector, "toMapWithErrorInValueSelector", this);
            this.payloads.toMapWithFactory = _ClassStatement.forPayload(FlowableToMapTest::toMapWithFactory, "toMapWithFactory", this);
            this.payloads.toMapWithErrorThrowingFactory = _ClassStatement.forPayload(FlowableToMapTest::toMapWithErrorThrowingFactory, "toMapWithErrorThrowingFactory", this);
        }
    }
}
