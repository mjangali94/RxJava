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
package io.reactivex.rxjava3.internal.functions;

import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions.*;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FunctionsTest extends RxJavaTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(Functions.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void hashSetCallableEnum() {
        // inlined TestHelper.checkEnum due to access restrictions
        try {
            Method m = Functions.HashSetSupplier.class.getMethod("values");
            m.setAccessible(true);
            Method e = Functions.HashSetSupplier.class.getMethod("valueOf", String.class);
            e.setAccessible(true);
            for (Enum<HashSetSupplier> o : (Enum<HashSetSupplier>[]) m.invoke(null)) {
                assertSame(o, e.invoke(null, o.name()));
            }
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void naturalComparatorEnum() {
        // inlined TestHelper.checkEnum due to access restrictions
        try {
            Method m = Functions.NaturalComparator.class.getMethod("values");
            m.setAccessible(true);
            Method e = Functions.NaturalComparator.class.getMethod("valueOf", String.class);
            e.setAccessible(true);
            for (Enum<NaturalComparator> o : (Enum<NaturalComparator>[]) m.invoke(null)) {
                assertSame(o, e.invoke(null, o.name()));
            }
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    @Test
    public void booleanSupplierPredicateReverse() throws Throwable {
        BooleanSupplier s = new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return false;
            }
        };
        assertTrue(Functions.predicateReverseFor(s).test(1));
        s = new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return true;
            }
        };
        assertFalse(Functions.predicateReverseFor(s).test(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void toFunction2() throws Throwable {
        Functions.toFunction(new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2) throws Exception {
                return null;
            }
        }).apply(new Object[20]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toFunction3() throws Throwable {
        Functions.toFunction(new Function3<Integer, Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2, Integer t3) throws Exception {
                return null;
            }
        }).apply(new Object[20]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toFunction4() throws Throwable {
        Functions.toFunction(new Function4<Integer, Integer, Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2, Integer t3, Integer t4) throws Exception {
                return null;
            }
        }).apply(new Object[20]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toFunction5() throws Throwable {
        Functions.toFunction(new Function5<Integer, Integer, Integer, Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5) throws Exception {
                return null;
            }
        }).apply(new Object[20]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toFunction6() throws Throwable {
        Functions.toFunction(new Function6<Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6) throws Exception {
                return null;
            }
        }).apply(new Object[20]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toFunction7() throws Throwable {
        Functions.toFunction(new Function7<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6, Integer t7) throws Exception {
                return null;
            }
        }).apply(new Object[20]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toFunction8() throws Throwable {
        Functions.toFunction(new Function8<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6, Integer t7, Integer t8) throws Exception {
                return null;
            }
        }).apply(new Object[20]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toFunction9() throws Throwable {
        Functions.toFunction(new Function9<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6, Integer t7, Integer t8, Integer t9) throws Exception {
                return null;
            }
        }).apply(new Object[20]);
    }

    @Test
    public void identityFunctionToString() {
        assertEquals("IdentityFunction", Functions.identity().toString());
    }

    @Test
    public void emptyActionToString() {
        assertEquals("EmptyAction", Functions.EMPTY_ACTION.toString());
    }

    @Test
    public void emptyRunnableToString() {
        assertEquals("EmptyRunnable", Functions.EMPTY_RUNNABLE.toString());
    }

    @Test
    public void emptyConsumerToString() {
        assertEquals("EmptyConsumer", Functions.EMPTY_CONSUMER.toString());
    }

    @Test
    public void errorConsumerEmpty() throws Throwable {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Functions.ERROR_CONSUMER.accept(new TestException());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            assertEquals(errors.toString(), 1, errors.size());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FunctionsTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_utilityClass() throws java.lang.Throwable {
            this.payloads.utilityClass.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hashSetCallableEnum() throws java.lang.Throwable {
            this.payloads.hashSetCallableEnum.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_naturalComparatorEnum() throws java.lang.Throwable {
            this.payloads.naturalComparatorEnum.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_booleanSupplierPredicateReverse() throws java.lang.Throwable {
            this.payloads.booleanSupplierPredicateReverse.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFunction2() throws java.lang.Throwable {
            this.payloads.toFunction2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFunction3() throws java.lang.Throwable {
            this.payloads.toFunction3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFunction4() throws java.lang.Throwable {
            this.payloads.toFunction4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFunction5() throws java.lang.Throwable {
            this.payloads.toFunction5.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFunction6() throws java.lang.Throwable {
            this.payloads.toFunction6.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFunction7() throws java.lang.Throwable {
            this.payloads.toFunction7.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFunction8() throws java.lang.Throwable {
            this.payloads.toFunction8.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFunction9() throws java.lang.Throwable {
            this.payloads.toFunction9.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_identityFunctionToString() throws java.lang.Throwable {
            this.payloads.identityFunctionToString.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyActionToString() throws java.lang.Throwable {
            this.payloads.emptyActionToString.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyRunnableToString() throws java.lang.Throwable {
            this.payloads.emptyRunnableToString.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyConsumerToString() throws java.lang.Throwable {
            this.payloads.emptyConsumerToString.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorConsumerEmpty() throws java.lang.Throwable {
            this.payloads.errorConsumerEmpty.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FunctionsTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FunctionsTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FunctionsTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FunctionsTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FunctionsTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FunctionsTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FunctionsTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FunctionsTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement utilityClass;

            public org.junit.runners.model.Statement hashSetCallableEnum;

            public org.junit.runners.model.Statement naturalComparatorEnum;

            public org.junit.runners.model.Statement booleanSupplierPredicateReverse;

            public org.junit.runners.model.Statement toFunction2;

            public org.junit.runners.model.Statement toFunction3;

            public org.junit.runners.model.Statement toFunction4;

            public org.junit.runners.model.Statement toFunction5;

            public org.junit.runners.model.Statement toFunction6;

            public org.junit.runners.model.Statement toFunction7;

            public org.junit.runners.model.Statement toFunction8;

            public org.junit.runners.model.Statement toFunction9;

            public org.junit.runners.model.Statement identityFunctionToString;

            public org.junit.runners.model.Statement emptyActionToString;

            public org.junit.runners.model.Statement emptyRunnableToString;

            public org.junit.runners.model.Statement emptyConsumerToString;

            public org.junit.runners.model.Statement errorConsumerEmpty;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.utilityClass = _ClassStatement.forPayload(FunctionsTest::utilityClass, "utilityClass", this);
            this.payloads.hashSetCallableEnum = _ClassStatement.forPayload(FunctionsTest::hashSetCallableEnum, "hashSetCallableEnum", this);
            this.payloads.naturalComparatorEnum = _ClassStatement.forPayload(FunctionsTest::naturalComparatorEnum, "naturalComparatorEnum", this);
            this.payloads.booleanSupplierPredicateReverse = _ClassStatement.forPayload(FunctionsTest::booleanSupplierPredicateReverse, "booleanSupplierPredicateReverse", this);
            this.payloads.toFunction2 = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FunctionsTest::toFunction2, java.lang.IllegalArgumentException.class), "toFunction2", this);
            this.payloads.toFunction3 = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FunctionsTest::toFunction3, java.lang.IllegalArgumentException.class), "toFunction3", this);
            this.payloads.toFunction4 = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FunctionsTest::toFunction4, java.lang.IllegalArgumentException.class), "toFunction4", this);
            this.payloads.toFunction5 = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FunctionsTest::toFunction5, java.lang.IllegalArgumentException.class), "toFunction5", this);
            this.payloads.toFunction6 = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FunctionsTest::toFunction6, java.lang.IllegalArgumentException.class), "toFunction6", this);
            this.payloads.toFunction7 = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FunctionsTest::toFunction7, java.lang.IllegalArgumentException.class), "toFunction7", this);
            this.payloads.toFunction8 = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FunctionsTest::toFunction8, java.lang.IllegalArgumentException.class), "toFunction8", this);
            this.payloads.toFunction9 = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FunctionsTest::toFunction9, java.lang.IllegalArgumentException.class), "toFunction9", this);
            this.payloads.identityFunctionToString = _ClassStatement.forPayload(FunctionsTest::identityFunctionToString, "identityFunctionToString", this);
            this.payloads.emptyActionToString = _ClassStatement.forPayload(FunctionsTest::emptyActionToString, "emptyActionToString", this);
            this.payloads.emptyRunnableToString = _ClassStatement.forPayload(FunctionsTest::emptyRunnableToString, "emptyRunnableToString", this);
            this.payloads.emptyConsumerToString = _ClassStatement.forPayload(FunctionsTest::emptyConsumerToString, "emptyConsumerToString", this);
            this.payloads.errorConsumerEmpty = _ClassStatement.forPayload(FunctionsTest::errorConsumerEmpty, "errorConsumerEmpty", this);
        }
    }
}
