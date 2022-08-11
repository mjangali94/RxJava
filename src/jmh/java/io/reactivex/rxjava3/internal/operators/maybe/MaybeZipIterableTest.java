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
package io.reactivex.rxjava3.internal.operators.maybe;

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

public class MaybeZipIterableTest extends RxJavaTest {

    final Function<Object[], Object> addString = new Function<Object[], Object>() {

        @Override
        public Object apply(Object[] a) throws Exception {
            return Arrays.toString(a);
        }
    };

    @Test
    public void firstError() {
        Maybe.zip(Arrays.asList(Maybe.error(new TestException()), Maybe.just(1)), addString).test().assertFailure(TestException.class);
    }

    @Test
    public void secondError() {
        Maybe.zip(Arrays.asList(Maybe.just(1), Maybe.<Integer>error(new TestException())), addString).test().assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Object> to = Maybe.zip(Arrays.asList(pp.singleElement(), pp.singleElement()), addString).test();
        assertTrue(pp.hasSubscribers());
        to.dispose();
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void zipperThrows() {
        Maybe.zip(Arrays.asList(Maybe.just(1), Maybe.just(2)), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] b) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void zipperReturnsNull() {
        Maybe.zip(Arrays.asList(Maybe.just(1), Maybe.just(2)), new Function<Object[], Object>() {

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
        TestObserver<Object> to = Maybe.zip(Arrays.asList(pp0.singleElement(), pp1.singleElement(), pp0.singleElement()), addString).test();
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
                final TestObserver<Object> to = Maybe.zip(Arrays.asList(pp0.singleElement(), pp1.singleElement()), addString).test();
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
        Maybe.zip(new CrashingMappedIterable<>(1, 100, 100, new Function<Integer, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }), addString).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(TestException.class, "iterator()");
    }

    @Test
    public void hasNextThrows() {
        Maybe.zip(new CrashingMappedIterable<>(100, 20, 100, new Function<Integer, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }), addString).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(TestException.class, "hasNext()");
    }

    @Test
    public void nextThrows() {
        Maybe.zip(new CrashingMappedIterable<>(100, 100, 5, new Function<Integer, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }), addString).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(TestException.class, "next()");
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableOneIsNull() {
        Maybe.zip(Arrays.asList(null, Maybe.just(1)), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void zipIterableTwoIsNull() {
        Maybe.zip(Arrays.asList(Maybe.just(1), null), new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }).blockingGet();
    }

    @Test
    public void singleSourceZipperReturnsNull() {
        Maybe.zipArray(Functions.justFunction(null), Maybe.just(1)).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(NullPointerException.class, "The zipper returned a null value");
    }

    @Test
    public void maybeSourcesInIterable() {
        MaybeSource<Integer> source = new MaybeSource<Integer>() {

            @Override
            public void subscribe(MaybeObserver<? super Integer> observer) {
                Maybe.just(1).subscribe(observer);
            }
        };
        Maybe.zip(Arrays.asList(source, source), new Function<Object[], Integer>() {

            @Override
            public Integer apply(Object[] t) throws Throwable {
                return 2;
            }
        }).test().assertResult(2);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeZipIterableTest instance;

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
        public void benchmark_singleSourceZipperReturnsNull() throws java.lang.Throwable {
            this.payloads.singleSourceZipperReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeSourcesInIterable() throws java.lang.Throwable {
            this.payloads.maybeSourcesInIterable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeZipIterableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeZipIterableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeZipIterableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeZipIterableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeZipIterableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeZipIterableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeZipIterableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeZipIterableTest.class, name);
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

            public org.junit.runners.model.Statement singleSourceZipperReturnsNull;

            public org.junit.runners.model.Statement maybeSourcesInIterable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.firstError = _ClassStatement.forPayload(MaybeZipIterableTest::firstError, "firstError", this);
            this.payloads.secondError = _ClassStatement.forPayload(MaybeZipIterableTest::secondError, "secondError", this);
            this.payloads.dispose = _ClassStatement.forPayload(MaybeZipIterableTest::dispose, "dispose", this);
            this.payloads.zipperThrows = _ClassStatement.forPayload(MaybeZipIterableTest::zipperThrows, "zipperThrows", this);
            this.payloads.zipperReturnsNull = _ClassStatement.forPayload(MaybeZipIterableTest::zipperReturnsNull, "zipperReturnsNull", this);
            this.payloads.middleError = _ClassStatement.forPayload(MaybeZipIterableTest::middleError, "middleError", this);
            this.payloads.innerErrorRace = _ClassStatement.forPayload(MaybeZipIterableTest::innerErrorRace, "innerErrorRace", this);
            this.payloads.iteratorThrows = _ClassStatement.forPayload(MaybeZipIterableTest::iteratorThrows, "iteratorThrows", this);
            this.payloads.hasNextThrows = _ClassStatement.forPayload(MaybeZipIterableTest::hasNextThrows, "hasNextThrows", this);
            this.payloads.nextThrows = _ClassStatement.forPayload(MaybeZipIterableTest::nextThrows, "nextThrows", this);
            this.payloads.zipIterableOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(MaybeZipIterableTest::zipIterableOneIsNull, java.lang.NullPointerException.class), "zipIterableOneIsNull", this);
            this.payloads.zipIterableTwoIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(MaybeZipIterableTest::zipIterableTwoIsNull, java.lang.NullPointerException.class), "zipIterableTwoIsNull", this);
            this.payloads.singleSourceZipperReturnsNull = _ClassStatement.forPayload(MaybeZipIterableTest::singleSourceZipperReturnsNull, "singleSourceZipperReturnsNull", this);
            this.payloads.maybeSourcesInIterable = _ClassStatement.forPayload(MaybeZipIterableTest::maybeSourcesInIterable, "maybeSourcesInIterable", this);
        }
    }
}
