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
package io.reactivex.rxjava3.flowable;

import static io.reactivex.rxjava3.internal.util.TestingHelper.*;
import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public final class FlowableCollectTest extends RxJavaTest {

    @Test
    public void collectToListFlowable() {
        Flowable<List<Integer>> f = Flowable.just(1, 2, 3).collect(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() {
                return new ArrayList<>();
            }
        }, new BiConsumer<List<Integer>, Integer>() {

            @Override
            public void accept(List<Integer> list, Integer v) {
                list.add(v);
            }
        }).toFlowable();
        List<Integer> list = f.blockingLast();
        assertEquals(3, list.size());
        assertEquals(1, list.get(0).intValue());
        assertEquals(2, list.get(1).intValue());
        assertEquals(3, list.get(2).intValue());
        // test multiple subscribe
        List<Integer> list2 = f.blockingLast();
        assertEquals(3, list2.size());
        assertEquals(1, list2.get(0).intValue());
        assertEquals(2, list2.get(1).intValue());
        assertEquals(3, list2.get(2).intValue());
    }

    @Test
    public void collectToStringFlowable() {
        String value = Flowable.just(1, 2, 3).collect(new Supplier<StringBuilder>() {

            @Override
            public StringBuilder get() {
                return new StringBuilder();
            }
        }, new BiConsumer<StringBuilder, Integer>() {

            @Override
            public void accept(StringBuilder sb, Integer v) {
                if (sb.length() > 0) {
                    sb.append("-");
                }
                sb.append(v);
            }
        }).toFlowable().blockingLast().toString();
        assertEquals("1-2-3", value);
    }

    @Test
    public void factoryFailureResultsInErrorEmissionFlowable() {
        final RuntimeException e = new RuntimeException();
        Flowable.just(1).collect(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() throws Exception {
                throw e;
            }
        }, new BiConsumer<List<Integer>, Integer>() {

            @Override
            public void accept(List<Integer> list, Integer t) {
                list.add(t);
            }
        }).test().assertNoValues().assertError(e).assertNotComplete();
    }

    @Test
    public void collectorFailureDoesNotResultInTwoErrorEmissionsFlowable() {
        try {
            final List<Throwable> list = new CopyOnWriteArrayList<>();
            RxJavaPlugins.setErrorHandler(addToList(list));
            final RuntimeException e1 = new RuntimeException();
            final RuntimeException e2 = new RuntimeException();
            // 
            Burst.items(1).error(e2).collect(supplierListCreator(), biConsumerThrows(e1)).toFlowable().test().assertError(// 
            e1).assertNotComplete();
            assertEquals(1, list.size());
            assertEquals(e2, list.get(0).getCause());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void collectorFailureDoesNotResultInErrorAndCompletedEmissionsFlowable() {
        final RuntimeException e = new RuntimeException();
        // 
        Burst.item(1).create().collect(supplierListCreator(), // 
        biConsumerThrows(e)).toFlowable().test().assertError(// 
        e).assertNotComplete();
    }

    @Test
    public void collectorFailureDoesNotResultInErrorAndOnNextEmissionsFlowable() {
        final RuntimeException e = new RuntimeException();
        final AtomicBoolean added = new AtomicBoolean();
        BiConsumer<Object, Integer> throwOnFirstOnly = new BiConsumer<Object, Integer>() {

            boolean once = true;

            @Override
            public void accept(Object o, Integer t) {
                if (once) {
                    once = false;
                    throw e;
                } else {
                    added.set(true);
                }
            }
        };
        // 
        Burst.items(1, 2).create().collect(supplierListCreator(), // 
        throwOnFirstOnly).toFlowable().test().assertError(// 
        e).assertNoValues().assertNotComplete();
        assertFalse(added.get());
    }

    @Test
    public void collectIntoFlowable() {
        Flowable.just(1, 1, 1, 1, 2).collectInto(new HashSet<>(), new BiConsumer<HashSet<Integer>, Integer>() {

            @Override
            public void accept(HashSet<Integer> s, Integer v) throws Exception {
                s.add(v);
            }
        }).toFlowable().test().assertResult(new HashSet<>(Arrays.asList(1, 2)));
    }

    @Test
    public void collectToList() {
        Single<List<Integer>> o = Flowable.just(1, 2, 3).collect(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() {
                return new ArrayList<>();
            }
        }, new BiConsumer<List<Integer>, Integer>() {

            @Override
            public void accept(List<Integer> list, Integer v) {
                list.add(v);
            }
        });
        List<Integer> list = o.blockingGet();
        assertEquals(3, list.size());
        assertEquals(1, list.get(0).intValue());
        assertEquals(2, list.get(1).intValue());
        assertEquals(3, list.get(2).intValue());
        // test multiple subscribe
        List<Integer> list2 = o.blockingGet();
        assertEquals(3, list2.size());
        assertEquals(1, list2.get(0).intValue());
        assertEquals(2, list2.get(1).intValue());
        assertEquals(3, list2.get(2).intValue());
    }

    @Test
    public void collectToString() {
        String value = Flowable.just(1, 2, 3).collect(new Supplier<StringBuilder>() {

            @Override
            public StringBuilder get() {
                return new StringBuilder();
            }
        }, new BiConsumer<StringBuilder, Integer>() {

            @Override
            public void accept(StringBuilder sb, Integer v) {
                if (sb.length() > 0) {
                    sb.append("-");
                }
                sb.append(v);
            }
        }).blockingGet().toString();
        assertEquals("1-2-3", value);
    }

    @Test
    public void factoryFailureResultsInErrorEmission() {
        final RuntimeException e = new RuntimeException();
        Flowable.just(1).collect(new Supplier<List<Integer>>() {

            @Override
            public List<Integer> get() throws Exception {
                throw e;
            }
        }, new BiConsumer<List<Integer>, Integer>() {

            @Override
            public void accept(List<Integer> list, Integer t) {
                list.add(t);
            }
        }).test().assertNoValues().assertError(e).assertNotComplete();
    }

    @Test
    public void collectorFailureDoesNotResultInTwoErrorEmissions() {
        try {
            final List<Throwable> list = new CopyOnWriteArrayList<>();
            RxJavaPlugins.setErrorHandler(addToList(list));
            final RuntimeException e1 = new RuntimeException();
            final RuntimeException e2 = new RuntimeException();
            // 
            Burst.items(1).error(e2).collect(supplierListCreator(), // 
            biConsumerThrows(e1)).test().assertError(// 
            e1).assertNotComplete();
            assertEquals(1, list.size());
            assertEquals(e2, list.get(0).getCause());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void collectorFailureDoesNotResultInErrorAndCompletedEmissions() {
        final RuntimeException e = new RuntimeException();
        // 
        Burst.item(1).create().collect(supplierListCreator(), // 
        biConsumerThrows(e)).test().assertError(// 
        e).assertNotComplete();
    }

    @Test
    public void collectorFailureDoesNotResultInErrorAndOnNextEmissions() {
        final RuntimeException e = new RuntimeException();
        final AtomicBoolean added = new AtomicBoolean();
        BiConsumer<Object, Integer> throwOnFirstOnly = new BiConsumer<Object, Integer>() {

            boolean once = true;

            @Override
            public void accept(Object o, Integer t) {
                if (once) {
                    once = false;
                    throw e;
                } else {
                    added.set(true);
                }
            }
        };
        // 
        Burst.items(1, 2).create().collect(supplierListCreator(), // 
        throwOnFirstOnly).test().assertError(// 
        e).assertNoValues().assertNotComplete();
        assertFalse(added.get());
    }

    @Test
    public void collectInto() {
        Flowable.just(1, 1, 1, 1, 2).collectInto(new HashSet<>(), new BiConsumer<HashSet<Integer>, Integer>() {

            @Override
            public void accept(HashSet<Integer> s, Integer v) throws Exception {
                s.add(v);
            }
        }).test().assertResult(new HashSet<>(Arrays.asList(1, 2)));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1, 2).collect(Functions.justSupplier(new ArrayList<>()), new BiConsumer<ArrayList<Integer>, Integer>() {

            @Override
            public void accept(ArrayList<Integer> a, Integer b) throws Exception {
                a.add(b);
            }
        }));
        TestHelper.checkDisposed(Flowable.just(1, 2).collect(Functions.justSupplier(new ArrayList<>()), new BiConsumer<ArrayList<Integer>, Integer>() {

            @Override
            public void accept(ArrayList<Integer> a, Integer b) throws Exception {
                a.add(b);
            }
        }).toFlowable());
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Integer>, Flowable<ArrayList<Integer>>>() {

            @Override
            public Flowable<ArrayList<Integer>> apply(Flowable<Integer> f) throws Exception {
                return f.collect(Functions.justSupplier(new ArrayList<>()), new BiConsumer<ArrayList<Integer>, Integer>() {

                    @Override
                    public void accept(ArrayList<Integer> a, Integer b) throws Exception {
                        a.add(b);
                    }
                }).toFlowable();
            }
        });
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(new Function<Flowable<Integer>, Single<ArrayList<Integer>>>() {

            @Override
            public Single<ArrayList<Integer>> apply(Flowable<Integer> f) throws Exception {
                return f.collect(Functions.justSupplier(new ArrayList<>()), new BiConsumer<ArrayList<Integer>, Integer>() {

                    @Override
                    public void accept(ArrayList<Integer> a, Integer b) throws Exception {
                        a.add(b);
                    }
                });
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableCollectTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectToListFlowable() throws java.lang.Throwable {
            this.payloads.collectToListFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectToStringFlowable() throws java.lang.Throwable {
            this.payloads.collectToStringFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_factoryFailureResultsInErrorEmissionFlowable() throws java.lang.Throwable {
            this.payloads.factoryFailureResultsInErrorEmissionFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFailureDoesNotResultInTwoErrorEmissionsFlowable() throws java.lang.Throwable {
            this.payloads.collectorFailureDoesNotResultInTwoErrorEmissionsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFailureDoesNotResultInErrorAndCompletedEmissionsFlowable() throws java.lang.Throwable {
            this.payloads.collectorFailureDoesNotResultInErrorAndCompletedEmissionsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFailureDoesNotResultInErrorAndOnNextEmissionsFlowable() throws java.lang.Throwable {
            this.payloads.collectorFailureDoesNotResultInErrorAndOnNextEmissionsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectIntoFlowable() throws java.lang.Throwable {
            this.payloads.collectIntoFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectToList() throws java.lang.Throwable {
            this.payloads.collectToList.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectToString() throws java.lang.Throwable {
            this.payloads.collectToString.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_factoryFailureResultsInErrorEmission() throws java.lang.Throwable {
            this.payloads.factoryFailureResultsInErrorEmission.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFailureDoesNotResultInTwoErrorEmissions() throws java.lang.Throwable {
            this.payloads.collectorFailureDoesNotResultInTwoErrorEmissions.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFailureDoesNotResultInErrorAndCompletedEmissions() throws java.lang.Throwable {
            this.payloads.collectorFailureDoesNotResultInErrorAndCompletedEmissions.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectorFailureDoesNotResultInErrorAndOnNextEmissions() throws java.lang.Throwable {
            this.payloads.collectorFailureDoesNotResultInErrorAndOnNextEmissions.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectInto() throws java.lang.Throwable {
            this.payloads.collectInto.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCollectTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCollectTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCollectTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCollectTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableCollectTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCollectTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableCollectTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableCollectTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement collectToListFlowable;

            public org.junit.runners.model.Statement collectToStringFlowable;

            public org.junit.runners.model.Statement factoryFailureResultsInErrorEmissionFlowable;

            public org.junit.runners.model.Statement collectorFailureDoesNotResultInTwoErrorEmissionsFlowable;

            public org.junit.runners.model.Statement collectorFailureDoesNotResultInErrorAndCompletedEmissionsFlowable;

            public org.junit.runners.model.Statement collectorFailureDoesNotResultInErrorAndOnNextEmissionsFlowable;

            public org.junit.runners.model.Statement collectIntoFlowable;

            public org.junit.runners.model.Statement collectToList;

            public org.junit.runners.model.Statement collectToString;

            public org.junit.runners.model.Statement factoryFailureResultsInErrorEmission;

            public org.junit.runners.model.Statement collectorFailureDoesNotResultInTwoErrorEmissions;

            public org.junit.runners.model.Statement collectorFailureDoesNotResultInErrorAndCompletedEmissions;

            public org.junit.runners.model.Statement collectorFailureDoesNotResultInErrorAndOnNextEmissions;

            public org.junit.runners.model.Statement collectInto;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.collectToListFlowable = _ClassStatement.forPayload(FlowableCollectTest::collectToListFlowable, "collectToListFlowable", this);
            this.payloads.collectToStringFlowable = _ClassStatement.forPayload(FlowableCollectTest::collectToStringFlowable, "collectToStringFlowable", this);
            this.payloads.factoryFailureResultsInErrorEmissionFlowable = _ClassStatement.forPayload(FlowableCollectTest::factoryFailureResultsInErrorEmissionFlowable, "factoryFailureResultsInErrorEmissionFlowable", this);
            this.payloads.collectorFailureDoesNotResultInTwoErrorEmissionsFlowable = _ClassStatement.forPayload(FlowableCollectTest::collectorFailureDoesNotResultInTwoErrorEmissionsFlowable, "collectorFailureDoesNotResultInTwoErrorEmissionsFlowable", this);
            this.payloads.collectorFailureDoesNotResultInErrorAndCompletedEmissionsFlowable = _ClassStatement.forPayload(FlowableCollectTest::collectorFailureDoesNotResultInErrorAndCompletedEmissionsFlowable, "collectorFailureDoesNotResultInErrorAndCompletedEmissionsFlowable", this);
            this.payloads.collectorFailureDoesNotResultInErrorAndOnNextEmissionsFlowable = _ClassStatement.forPayload(FlowableCollectTest::collectorFailureDoesNotResultInErrorAndOnNextEmissionsFlowable, "collectorFailureDoesNotResultInErrorAndOnNextEmissionsFlowable", this);
            this.payloads.collectIntoFlowable = _ClassStatement.forPayload(FlowableCollectTest::collectIntoFlowable, "collectIntoFlowable", this);
            this.payloads.collectToList = _ClassStatement.forPayload(FlowableCollectTest::collectToList, "collectToList", this);
            this.payloads.collectToString = _ClassStatement.forPayload(FlowableCollectTest::collectToString, "collectToString", this);
            this.payloads.factoryFailureResultsInErrorEmission = _ClassStatement.forPayload(FlowableCollectTest::factoryFailureResultsInErrorEmission, "factoryFailureResultsInErrorEmission", this);
            this.payloads.collectorFailureDoesNotResultInTwoErrorEmissions = _ClassStatement.forPayload(FlowableCollectTest::collectorFailureDoesNotResultInTwoErrorEmissions, "collectorFailureDoesNotResultInTwoErrorEmissions", this);
            this.payloads.collectorFailureDoesNotResultInErrorAndCompletedEmissions = _ClassStatement.forPayload(FlowableCollectTest::collectorFailureDoesNotResultInErrorAndCompletedEmissions, "collectorFailureDoesNotResultInErrorAndCompletedEmissions", this);
            this.payloads.collectorFailureDoesNotResultInErrorAndOnNextEmissions = _ClassStatement.forPayload(FlowableCollectTest::collectorFailureDoesNotResultInErrorAndOnNextEmissions, "collectorFailureDoesNotResultInErrorAndOnNextEmissions", this);
            this.payloads.collectInto = _ClassStatement.forPayload(FlowableCollectTest::collectInto, "collectInto", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableCollectTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableCollectTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
