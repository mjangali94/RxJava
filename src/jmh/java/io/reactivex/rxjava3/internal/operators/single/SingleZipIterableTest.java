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

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.util.CrashingMappedIterable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleZipIterableTest extends RxJavaTest {

    final Function<Object[], Object> addString = new Function<Object[], Object>() {

        @Override
        public Object apply(Object[] a) throws Exception {
            return Arrays.toString(a);
        }
    };

    @Test
    public void firstError() {
        Single.zip(Arrays.asList(Single.error(new TestException()), Single.just(1)), addString).test().assertFailure(TestException.class);
    }

    @Test
    public void secondError() {
        Single.zip(Arrays.asList(Single.just(1), Single.<Integer>error(new TestException())), addString).test().assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Object> to = Single.zip(Arrays.asList(pp.single(0), pp.single(0)), addString).test();
        assertTrue(pp.hasSubscribers());
        to.dispose();
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void zipperThrows() {
        Single.zip(Arrays.asList(Single.just(1), Single.just(2)), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] b) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void zipperReturnsNull() {
        Single.zip(Arrays.asList(Single.just(1), Single.just(2)), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void middleError() {
        PublishProcessor<Integer> pp0 = PublishProcessor.create();
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        TestObserver<Object> to = Single.zip(Arrays.asList(pp0.single(0), pp1.single(0), pp0.single(0)), addString).test();
        pp1.onError(new TestException());
        assertFalse(pp0.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void innerErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishProcessor<Integer> pp0 = PublishProcessor.create();
                final PublishProcessor<Integer> pp1 = PublishProcessor.create();
                final TestObserver<Object> to = Single.zip(Arrays.asList(pp0.single(0), pp1.single(0)), addString).test();
                final TestException ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp0.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        pp1.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertFailure(TestException.class);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void iteratorThrows() {
        Single.zip(new CrashingMappedIterable<>(1, 100, 100, new Function<Integer, Single<Integer>>() {

            @Override
            public Single<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }), addString).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(TestException.class, "iterator()");
    }

    @Test
    public void hasNextThrows() {
        Single.zip(new CrashingMappedIterable<>(100, 20, 100, new Function<Integer, Single<Integer>>() {

            @Override
            public Single<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }), addString).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(TestException.class, "hasNext()");
    }

    @Test
    public void nextThrows() {
        Single.zip(new CrashingMappedIterable<>(100, 100, 5, new Function<Integer, Single<Integer>>() {

            @Override
            public Single<Integer> apply(Integer v) throws Exception {
                return Single.just(v);
            }
        }), addString).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(TestException.class, "next()");
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableOneIsNull() {
        Single.zip(Arrays.asList(null, Single.just(1)), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableTwoIsNull() {
        Single.zip(Arrays.asList(Single.just(1), null), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }).blockingGet();
    }

    @Test
    public void emptyIterable() {
        Single.zip(Collections.<SingleSource<Integer>>emptyList(), new Function<Object[], Object[]>() {

            @Override
            public Object[] apply(Object[] a) throws Exception {
                return a;
            }
        }).test().assertFailure(NoSuchElementException.class);
    }

    @Test
    public void oneIterable() {
        Single.zip(Collections.singleton(Single.just(1)), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) throws Exception {
                return (Integer) a[0] + 1;
            }
        }).test().assertResult(2);
    }

    @Test
    public void singleSourceZipperReturnsNull() {
        Single.zip(Arrays.asList(Single.just(1)), Functions.justFunction(null)).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(NullPointerException.class, "The zipper returned a null value");
    }

    @Test
    public void singleSourcesInIterable() {
        SingleSource<Integer> source = new SingleSource<Integer>() {

            @Override
            public void subscribe(SingleObserver<? super Integer> observer) {
                Single.just(1).subscribe(observer);
            }
        };
        Single.zip(Arrays.asList(source, source), new Function<Object[], Integer>() {

            @Override
            public Integer apply(Object[] t) throws Throwable {
                return 2;
            }
        }).test().assertResult(2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleZipIterableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstError() throws java.lang.Throwable {
            this.payloads.firstError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_secondError() throws java.lang.Throwable {
            this.payloads.secondError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipperThrows() throws java.lang.Throwable {
            this.payloads.zipperThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipperReturnsNull() throws java.lang.Throwable {
            this.payloads.zipperReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_middleError() throws java.lang.Throwable {
            this.payloads.middleError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorRace() throws java.lang.Throwable {
            this.payloads.innerErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorThrows() throws java.lang.Throwable {
            this.payloads.iteratorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrows() throws java.lang.Throwable {
            this.payloads.hasNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextThrows() throws java.lang.Throwable {
            this.payloads.nextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableOneIsNull() throws java.lang.Throwable {
            this.payloads.zipIterableOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableTwoIsNull() throws java.lang.Throwable {
            this.payloads.zipIterableTwoIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyIterable() throws java.lang.Throwable {
            this.payloads.emptyIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneIterable() throws java.lang.Throwable {
            this.payloads.oneIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSourceZipperReturnsNull() throws java.lang.Throwable {
            this.payloads.singleSourceZipperReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSourcesInIterable() throws java.lang.Throwable {
            this.payloads.singleSourcesInIterable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipIterableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipIterableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipIterableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipIterableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleZipIterableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipIterableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleZipIterableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleZipIterableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement firstError;

            public org.junit.runners.model.Statement secondError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement zipperThrows;

            public org.junit.runners.model.Statement zipperReturnsNull;

            public org.junit.runners.model.Statement middleError;

            public org.junit.runners.model.Statement innerErrorRace;

            public org.junit.runners.model.Statement iteratorThrows;

            public org.junit.runners.model.Statement hasNextThrows;

            public org.junit.runners.model.Statement nextThrows;

            public org.junit.runners.model.Statement zipIterableOneIsNull;

            public org.junit.runners.model.Statement zipIterableTwoIsNull;

            public org.junit.runners.model.Statement emptyIterable;

            public org.junit.runners.model.Statement oneIterable;

            public org.junit.runners.model.Statement singleSourceZipperReturnsNull;

            public org.junit.runners.model.Statement singleSourcesInIterable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.firstError = _ClassStatement.forPayload(SingleZipIterableTest::firstError, "firstError", this);
            this.payloads.secondError = _ClassStatement.forPayload(SingleZipIterableTest::secondError, "secondError", this);
            this.payloads.dispose = _ClassStatement.forPayload(SingleZipIterableTest::dispose, "dispose", this);
            this.payloads.zipperThrows = _ClassStatement.forPayload(SingleZipIterableTest::zipperThrows, "zipperThrows", this);
            this.payloads.zipperReturnsNull = _ClassStatement.forPayload(SingleZipIterableTest::zipperReturnsNull, "zipperReturnsNull", this);
            this.payloads.middleError = _ClassStatement.forPayload(SingleZipIterableTest::middleError, "middleError", this);
            this.payloads.innerErrorRace = _ClassStatement.forPayload(SingleZipIterableTest::innerErrorRace, "innerErrorRace", this);
            this.payloads.iteratorThrows = _ClassStatement.forPayload(SingleZipIterableTest::iteratorThrows, "iteratorThrows", this);
            this.payloads.hasNextThrows = _ClassStatement.forPayload(SingleZipIterableTest::hasNextThrows, "hasNextThrows", this);
            this.payloads.nextThrows = _ClassStatement.forPayload(SingleZipIterableTest::nextThrows, "nextThrows", this);
            this.payloads.zipIterableOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(SingleZipIterableTest::zipIterableOneIsNull, java.lang.NullPointerException.class), "zipIterableOneIsNull", this);
            this.payloads.zipIterableTwoIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(SingleZipIterableTest::zipIterableTwoIsNull, java.lang.NullPointerException.class), "zipIterableTwoIsNull", this);
            this.payloads.emptyIterable = _ClassStatement.forPayload(SingleZipIterableTest::emptyIterable, "emptyIterable", this);
            this.payloads.oneIterable = _ClassStatement.forPayload(SingleZipIterableTest::oneIterable, "oneIterable", this);
            this.payloads.singleSourceZipperReturnsNull = _ClassStatement.forPayload(SingleZipIterableTest::singleSourceZipperReturnsNull, "singleSourceZipperReturnsNull", this);
            this.payloads.singleSourcesInIterable = _ClassStatement.forPayload(SingleZipIterableTest::singleSourcesInIterable, "singleSourcesInIterable", this);
        }
    }
}
