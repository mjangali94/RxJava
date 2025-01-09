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
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleZipArrayTest extends RxJavaTest {

    final BiFunction<Object, Object, Object> addString = new BiFunction<Object, Object, Object>() {

        @Override
        public Object apply(Object a, Object b) throws Exception {
            return "" + a + b;
        }
    };

    final Function3<Object, Object, Object, Object> addString3 = new Function3<Object, Object, Object, Object>() {

        @Override
        public Object apply(Object a, Object b, Object c) throws Exception {
            return "" + a + b + c;
        }
    };

    @Test
    public void firstError() {
        Single.zip(Single.error(new TestException()), Single.just(1), addString).test().assertFailure(TestException.class);
    }

    @Test
    public void secondError() {
        Single.zip(Single.just(1), Single.<Integer>error(new TestException()), addString).test().assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Object> to = Single.zip(pp.single(0), pp.single(0), addString).test();
        assertTrue(pp.hasSubscribers());
        to.dispose();
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void zipperThrows() {
        Single.zip(Single.just(1), Single.just(2), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void zipperReturnsNull() {
        Single.zip(Single.just(1), Single.just(2), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void middleError() {
        PublishProcessor<Integer> pp0 = PublishProcessor.create();
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        TestObserver<Object> to = Single.zip(pp0.single(0), pp1.single(0), pp0.single(0), addString3).test();
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
                final TestObserver<Object> to = Single.zip(pp0.single(0), pp1.single(0), addString).test();
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

    @Test(expected = NullPointerException.class)
    public void zipArrayOneIsNull() {
        Single.zipArray(new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] v) {
                return 1;
            }
        }, Single.just(1), null).blockingGet();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void emptyArray() {
        Single.zipArray(new Function<Object[], Object[]>() {

            @Override
            public Object[] apply(Object[] a) throws Exception {
                return a;
            }
        }, new SingleSource[0]).test().assertFailure(NoSuchElementException.class);
    }

    @Test
    public void oneArray() {
        Single.zipArray(new Function<Object[], Object>() {

            @Override
            public Object apply(Object[] a) throws Exception {
                return (Integer) a[0] + 1;
            }
        }, Single.just(1)).test().assertResult(2);
    }

    @Test
    public void singleSourceZipperReturnsNull() {
        Single.zipArray(Functions.justFunction(null), Single.just(1)).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(NullPointerException.class, "The zipper returned a null value");
    }

    @Test
    public void singleSourceZipperReturnsNull2() {
        Single.zipArray(Functions.justFunction(null), Single.just(1), Single.just(2)).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(NullPointerException.class, "The zipper returned a null value");
    }

    @Test
    public void dispose2() {
        TestHelper.checkDisposed(Single.zipArray(Functions.justFunction(1), SingleSubject.create(), SingleSubject.create()));
    }

    @Test
    public void bothSucceed() {
        Single.zipArray(a -> Arrays.asList(a), Single.just(1), Single.just(2)).test().assertResult(Arrays.asList(1, 2));
    }

    @Test
    public void onSuccessAfterDispose() {
        AtomicReference<SingleObserver<? super Integer>> emitter = new AtomicReference<>();
        TestObserver<List<Object>> to = Single.zipArray(Arrays::asList, (SingleSource<Integer>) o -> emitter.set(o), Single.<Integer>never()).test();
        emitter.get().onSubscribe(Disposable.empty());
        to.dispose();
        emitter.get().onSuccess(1);
        to.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public SingleZipArrayTest instance;

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
        public void benchmark_zipArrayOneIsNull() throws java.lang.Throwable {
            this.payloads.zipArrayOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyArray() throws java.lang.Throwable {
            this.payloads.emptyArray.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArray() throws java.lang.Throwable {
            this.payloads.oneArray.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSourceZipperReturnsNull() throws java.lang.Throwable {
            this.payloads.singleSourceZipperReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSourceZipperReturnsNull2() throws java.lang.Throwable {
            this.payloads.singleSourceZipperReturnsNull2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose2() throws java.lang.Throwable {
            this.payloads.dispose2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bothSucceed() throws java.lang.Throwable {
            this.payloads.bothSucceed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSuccessAfterDispose() throws java.lang.Throwable {
            this.payloads.onSuccessAfterDispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipArrayTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipArrayTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipArrayTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipArrayTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleZipArrayTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleZipArrayTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleZipArrayTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleZipArrayTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement firstError;

            public org.junit.runners.model.Statement secondError;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement zipperThrows;

            public org.junit.runners.model.Statement zipperReturnsNull;

            public org.junit.runners.model.Statement middleError;

            public org.junit.runners.model.Statement innerErrorRace;

            public org.junit.runners.model.Statement zipArrayOneIsNull;

            public org.junit.runners.model.Statement emptyArray;

            public org.junit.runners.model.Statement oneArray;

            public org.junit.runners.model.Statement singleSourceZipperReturnsNull;

            public org.junit.runners.model.Statement singleSourceZipperReturnsNull2;

            public org.junit.runners.model.Statement dispose2;

            public org.junit.runners.model.Statement bothSucceed;

            public org.junit.runners.model.Statement onSuccessAfterDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.firstError = _ClassStatement.forPayload(SingleZipArrayTest::firstError, "firstError", this);
            this.payloads.secondError = _ClassStatement.forPayload(SingleZipArrayTest::secondError, "secondError", this);
            this.payloads.dispose = _ClassStatement.forPayload(SingleZipArrayTest::dispose, "dispose", this);
            this.payloads.zipperThrows = _ClassStatement.forPayload(SingleZipArrayTest::zipperThrows, "zipperThrows", this);
            this.payloads.zipperReturnsNull = _ClassStatement.forPayload(SingleZipArrayTest::zipperReturnsNull, "zipperReturnsNull", this);
            this.payloads.middleError = _ClassStatement.forPayload(SingleZipArrayTest::middleError, "middleError", this);
            this.payloads.innerErrorRace = _ClassStatement.forPayload(SingleZipArrayTest::innerErrorRace, "innerErrorRace", this);
            this.payloads.zipArrayOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(SingleZipArrayTest::zipArrayOneIsNull, java.lang.NullPointerException.class), "zipArrayOneIsNull", this);
            this.payloads.emptyArray = _ClassStatement.forPayload(SingleZipArrayTest::emptyArray, "emptyArray", this);
            this.payloads.oneArray = _ClassStatement.forPayload(SingleZipArrayTest::oneArray, "oneArray", this);
            this.payloads.singleSourceZipperReturnsNull = _ClassStatement.forPayload(SingleZipArrayTest::singleSourceZipperReturnsNull, "singleSourceZipperReturnsNull", this);
            this.payloads.singleSourceZipperReturnsNull2 = _ClassStatement.forPayload(SingleZipArrayTest::singleSourceZipperReturnsNull2, "singleSourceZipperReturnsNull2", this);
            this.payloads.dispose2 = _ClassStatement.forPayload(SingleZipArrayTest::dispose2, "dispose2", this);
            this.payloads.bothSucceed = _ClassStatement.forPayload(SingleZipArrayTest::bothSucceed, "bothSucceed", this);
            this.payloads.onSuccessAfterDispose = _ClassStatement.forPayload(SingleZipArrayTest::onSuccessAfterDispose, "onSuccessAfterDispose", this);
        }
    }
}
