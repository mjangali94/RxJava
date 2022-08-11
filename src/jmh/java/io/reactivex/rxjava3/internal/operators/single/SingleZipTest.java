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
package io.reactivex.rxjava3.internal.operators.single;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.*;

public class SingleZipTest extends RxJavaTest {

    @Test
    public void zip2() {
        Single.zip(Single.just(1), Single.just(2), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                return a + "" + b;
            }
        }).test().assertResult("12");
    }

    @Test
    public void zip3() {
        Single.zip(Single.just(1), Single.just(2), Single.just(3), new Function3<Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c) throws Exception {
                return a + "" + b + c;
            }
        }).test().assertResult("123");
    }

    @Test
    public void zip4() {
        Single.zip(Single.just(1), Single.just(2), Single.just(3), Single.just(4), new Function4<Integer, Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c, Integer d) throws Exception {
                return a + "" + b + c + d;
            }
        }).test().assertResult("1234");
    }

    @Test
    public void zip5() {
        Single.zip(Single.just(1), Single.just(2), Single.just(3), Single.just(4), Single.just(5), new Function5<Integer, Integer, Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c, Integer d, Integer e) throws Exception {
                return a + "" + b + c + d + e;
            }
        }).test().assertResult("12345");
    }

    @Test
    public void zip6() {
        Single.zip(Single.just(1), Single.just(2), Single.just(3), Single.just(4), Single.just(5), Single.just(6), new Function6<Integer, Integer, Integer, Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c, Integer d, Integer e, Integer f) throws Exception {
                return a + "" + b + c + d + e + f;
            }
        }).test().assertResult("123456");
    }

    @Test
    public void zip7() {
        Single.zip(Single.just(1), Single.just(2), Single.just(3), Single.just(4), Single.just(5), Single.just(6), Single.just(7), new Function7<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c, Integer d, Integer e, Integer f, Integer g) throws Exception {
                return a + "" + b + c + d + e + f + g;
            }
        }).test().assertResult("1234567");
    }

    @Test
    public void zip8() {
        Single.zip(Single.just(1), Single.just(2), Single.just(3), Single.just(4), Single.just(5), Single.just(6), Single.just(7), Single.just(8), new Function8<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c, Integer d, Integer e, Integer f, Integer g, Integer h) throws Exception {
                return a + "" + b + c + d + e + f + g + h;
            }
        }).test().assertResult("12345678");
    }

    @Test
    public void zip9() {
        Single.zip(Single.just(1), Single.just(2), Single.just(3), Single.just(4), Single.just(5), Single.just(6), Single.just(7), Single.just(8), Single.just(9), new Function9<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b, Integer c, Integer d, Integer e, Integer f, Integer g, Integer h, Integer i) throws Exception {
                return a + "" + b + c + d + e + f + g + h + i;
            }
        }).test().assertResult("123456789");
    }

    @Test
    public void noDisposeOnAllSuccess() {
        final AtomicInteger counter = new AtomicInteger();
        Single<Integer> source = Single.just(1).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        Single.zip(source, source, new BiFunction<Integer, Integer, Object>() {

            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).test().assertResult(2);
        assertEquals(0, counter.get());
    }

    @Test
    public void noDisposeOnAllSuccess2() {
        final AtomicInteger counter = new AtomicInteger();
        Single<Integer> source = Single.just(1).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        Single.zip(Arrays.asList(source, source), new Function<Object[], Object>() {

            @Override
            public Integer apply(Object[] o) throws Exception {
                return (Integer) o[0] + (Integer) o[1];
            }
        }).test().assertResult(2);
        assertEquals(0, counter.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleZipTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip2() throws java.lang.Throwable {
            this.payloads.zip2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip3() throws java.lang.Throwable {
            this.payloads.zip3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip4() throws java.lang.Throwable {
            this.payloads.zip4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip5() throws java.lang.Throwable {
            this.payloads.zip5.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip6() throws java.lang.Throwable {
            this.payloads.zip6.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip7() throws java.lang.Throwable {
            this.payloads.zip7.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip8() throws java.lang.Throwable {
            this.payloads.zip8.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip9() throws java.lang.Throwable {
            this.payloads.zip9.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noDisposeOnAllSuccess() throws java.lang.Throwable {
            this.payloads.noDisposeOnAllSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noDisposeOnAllSuccess2() throws java.lang.Throwable {
            this.payloads.noDisposeOnAllSuccess2.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleZipTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleZipTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleZipTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement zip2;

            public org.junit.runners.model.Statement zip3;

            public org.junit.runners.model.Statement zip4;

            public org.junit.runners.model.Statement zip5;

            public org.junit.runners.model.Statement zip6;

            public org.junit.runners.model.Statement zip7;

            public org.junit.runners.model.Statement zip8;

            public org.junit.runners.model.Statement zip9;

            public org.junit.runners.model.Statement noDisposeOnAllSuccess;

            public org.junit.runners.model.Statement noDisposeOnAllSuccess2;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.zip2 = _ClassStatement.forPayload(SingleZipTest::zip2, "zip2", this);
            this.payloads.zip3 = _ClassStatement.forPayload(SingleZipTest::zip3, "zip3", this);
            this.payloads.zip4 = _ClassStatement.forPayload(SingleZipTest::zip4, "zip4", this);
            this.payloads.zip5 = _ClassStatement.forPayload(SingleZipTest::zip5, "zip5", this);
            this.payloads.zip6 = _ClassStatement.forPayload(SingleZipTest::zip6, "zip6", this);
            this.payloads.zip7 = _ClassStatement.forPayload(SingleZipTest::zip7, "zip7", this);
            this.payloads.zip8 = _ClassStatement.forPayload(SingleZipTest::zip8, "zip8", this);
            this.payloads.zip9 = _ClassStatement.forPayload(SingleZipTest::zip9, "zip9", this);
            this.payloads.noDisposeOnAllSuccess = _ClassStatement.forPayload(SingleZipTest::noDisposeOnAllSuccess, "noDisposeOnAllSuccess", this);
            this.payloads.noDisposeOnAllSuccess2 = _ClassStatement.forPayload(SingleZipTest::noDisposeOnAllSuccess2, "noDisposeOnAllSuccess2", this);
        }
    }
}
