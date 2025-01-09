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

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableGenerateTest extends RxJavaTest {

    @Test
    public void statefulBiconsumer() {
        Flowable.generate(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 10;
            }
        }, new BiConsumer<Object, Emitter<Object>>() {

            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                e.onNext(s);
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object d) throws Exception {
            }
        }).take(5).test().assertResult(10, 10, 10, 10, 10);
    }

    @Test
    public void stateSupplierThrows() {
        Flowable.generate(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                throw new TestException();
            }
        }, new BiConsumer<Object, Emitter<Object>>() {

            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                e.onNext(s);
            }
        }, Functions.emptyConsumer()).test().assertFailure(TestException.class);
    }

    @Test
    public void generatorThrows() {
        Flowable.generate(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new BiConsumer<Object, Emitter<Object>>() {

            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                throw new TestException();
            }
        }, Functions.emptyConsumer()).test().assertFailure(TestException.class);
    }

    @Test
    public void disposerThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.generate(new Supplier<Object>() {

                @Override
                public Object get() throws Exception {
                    return 1;
                }
            }, new BiConsumer<Object, Emitter<Object>>() {

                @Override
                public void accept(Object s, Emitter<Object> e) throws Exception {
                    e.onComplete();
                }
            }, new Consumer<Object>() {

                @Override
                public void accept(Object d) throws Exception {
                    throw new TestException();
                }
            }).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.generate(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new BiConsumer<Object, Emitter<Object>>() {

            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                e.onComplete();
            }
        }, Functions.emptyConsumer()));
    }

    @Test
    public void nullError() {
        final int[] call = { 0 };
        Flowable.generate(Functions.justSupplier(1), new BiConsumer<Integer, Emitter<Object>>() {

            @Override
            public void accept(Integer s, Emitter<Object> e) throws Exception {
                try {
                    e.onError(null);
                } catch (NullPointerException ex) {
                    call[0]++;
                }
            }
        }, Functions.emptyConsumer()).test().assertFailure(NullPointerException.class);
        assertEquals(0, call[0]);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.generate(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new BiConsumer<Object, Emitter<Object>>() {

            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                e.onComplete();
            }
        }, Functions.emptyConsumer()));
    }

    @Test
    public void rebatchAndTake() {
        Flowable.generate(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new BiConsumer<Object, Emitter<Object>>() {

            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                e.onNext(1);
            }
        }, Functions.emptyConsumer()).rebatchRequests(1).take(5).test().assertResult(1, 1, 1, 1, 1);
    }

    @Test
    public void backpressure() {
        Flowable.generate(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new BiConsumer<Object, Emitter<Object>>() {

            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                e.onNext(1);
            }
        }, Functions.emptyConsumer()).rebatchRequests(1).to(TestHelper.<Object>testSubscriber(5L)).assertSubscribed().assertValues(1, 1, 1, 1, 1).assertNoErrors().assertNotComplete();
    }

    @Test
    public void requestRace() {
        Flowable<Object> source = Flowable.generate(new Supplier<Object>() {

            @Override
            public Object get() throws Exception {
                return 1;
            }
        }, new BiConsumer<Object, Emitter<Object>>() {

            @Override
            public void accept(Object s, Emitter<Object> e) throws Exception {
                e.onNext(1);
            }
        }, Functions.emptyConsumer());
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final TestSubscriber<Object> ts = source.test(0L);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    for (int j = 0; j < 500; j++) {
                        ts.request(1);
                    }
                }
            };
            TestHelper.race(r, r);
            ts.assertValueCount(1000);
        }
    }

    @Test
    public void multipleOnNext() {
        Flowable.generate(new Consumer<Emitter<Object>>() {

            @Override
            public void accept(Emitter<Object> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
            }
        }).test(1).assertFailure(IllegalStateException.class, 1);
    }

    @Test
    public void multipleOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.generate(new Consumer<Emitter<Object>>() {

                @Override
                public void accept(Emitter<Object> e) throws Exception {
                    e.onError(new TestException("First"));
                    e.onError(new TestException("Second"));
                }
            }).test(1).assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void multipleOnComplete() {
        Flowable.generate(new Consumer<Emitter<Object>>() {

            @Override
            public void accept(Emitter<Object> e) throws Exception {
                e.onComplete();
                e.onComplete();
            }
        }).test(1).assertResult();
    }

    @Test
    public void onNextAfterOnComplete() {
        Flowable.generate(new Consumer<Emitter<Object>>() {

            @Override
            public void accept(Emitter<Object> e) throws Exception {
                e.onComplete();
                e.onNext(1);
            }
        }).test().assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableGenerateTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_statefulBiconsumer() throws java.lang.Throwable {
            this.payloads.statefulBiconsumer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_stateSupplierThrows() throws java.lang.Throwable {
            this.payloads.stateSupplierThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_generatorThrows() throws java.lang.Throwable {
            this.payloads.generatorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposerThrows() throws java.lang.Throwable {
            this.payloads.disposerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullError() throws java.lang.Throwable {
            this.payloads.nullError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rebatchAndTake() throws java.lang.Throwable {
            this.payloads.rebatchAndTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure() throws java.lang.Throwable {
            this.payloads.backpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_requestRace() throws java.lang.Throwable {
            this.payloads.requestRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleOnNext() throws java.lang.Throwable {
            this.payloads.multipleOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleOnError() throws java.lang.Throwable {
            this.payloads.multipleOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_multipleOnComplete() throws java.lang.Throwable {
            this.payloads.multipleOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextAfterOnComplete() throws java.lang.Throwable {
            this.payloads.onNextAfterOnComplete.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGenerateTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGenerateTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGenerateTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGenerateTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableGenerateTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGenerateTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableGenerateTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableGenerateTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement statefulBiconsumer;

            public org.junit.runners.model.Statement stateSupplierThrows;

            public org.junit.runners.model.Statement generatorThrows;

            public org.junit.runners.model.Statement disposerThrows;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement nullError;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement rebatchAndTake;

            public org.junit.runners.model.Statement backpressure;

            public org.junit.runners.model.Statement requestRace;

            public org.junit.runners.model.Statement multipleOnNext;

            public org.junit.runners.model.Statement multipleOnError;

            public org.junit.runners.model.Statement multipleOnComplete;

            public org.junit.runners.model.Statement onNextAfterOnComplete;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.statefulBiconsumer = _ClassStatement.forPayload(FlowableGenerateTest::statefulBiconsumer, "statefulBiconsumer", this);
            this.payloads.stateSupplierThrows = _ClassStatement.forPayload(FlowableGenerateTest::stateSupplierThrows, "stateSupplierThrows", this);
            this.payloads.generatorThrows = _ClassStatement.forPayload(FlowableGenerateTest::generatorThrows, "generatorThrows", this);
            this.payloads.disposerThrows = _ClassStatement.forPayload(FlowableGenerateTest::disposerThrows, "disposerThrows", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableGenerateTest::dispose, "dispose", this);
            this.payloads.nullError = _ClassStatement.forPayload(FlowableGenerateTest::nullError, "nullError", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableGenerateTest::badRequest, "badRequest", this);
            this.payloads.rebatchAndTake = _ClassStatement.forPayload(FlowableGenerateTest::rebatchAndTake, "rebatchAndTake", this);
            this.payloads.backpressure = _ClassStatement.forPayload(FlowableGenerateTest::backpressure, "backpressure", this);
            this.payloads.requestRace = _ClassStatement.forPayload(FlowableGenerateTest::requestRace, "requestRace", this);
            this.payloads.multipleOnNext = _ClassStatement.forPayload(FlowableGenerateTest::multipleOnNext, "multipleOnNext", this);
            this.payloads.multipleOnError = _ClassStatement.forPayload(FlowableGenerateTest::multipleOnError, "multipleOnError", this);
            this.payloads.multipleOnComplete = _ClassStatement.forPayload(FlowableGenerateTest::multipleOnComplete, "multipleOnComplete", this);
            this.payloads.onNextAfterOnComplete = _ClassStatement.forPayload(FlowableGenerateTest::onNextAfterOnComplete, "onNextAfterOnComplete", this);
        }
    }
}
