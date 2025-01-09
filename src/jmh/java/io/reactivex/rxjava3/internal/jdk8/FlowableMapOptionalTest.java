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
package io.reactivex.rxjava3.internal.jdk8;

import static org.junit.Assert.assertFalse;
import java.util.Optional;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.schedulers.ImmediateThinScheduler;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableMapOptionalTest extends RxJavaTest {

    static final Function<? super Integer, Optional<? extends Integer>> MODULO = v -> v % 2 == 0 ? Optional.of(v) : Optional.<Integer>empty();

    @Test
    public void allPresent() {
        Flowable.range(1, 5).mapOptional(Optional::of).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void allEmpty() {
        Flowable.range(1, 5).mapOptional(v -> Optional.<Integer>empty()).test().assertResult();
    }

    @Test
    public void mixed() {
        Flowable.range(1, 10).mapOptional(MODULO).test().assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void mapperChash() {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        source.mapOptional(v -> {
            throw new TestException();
        }).test().assertFailure(TestException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void mapperNull() {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        source.mapOptional(v -> null).test().assertFailure(NullPointerException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void crashDropsOnNexts() {
        Flowable<Integer> source = new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onNext(1);
                s.onNext(2);
            }
        };
        source.mapOptional(v -> {
            throw new TestException();
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void backpressureAll() {
        Flowable.range(1, 5).mapOptional(Optional::of).test(0L).assertEmpty().requestMore(2).assertValuesOnly(1, 2).requestMore(2).assertValuesOnly(1, 2, 3, 4).requestMore(1).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void backpressureNone() {
        Flowable.range(1, 5).mapOptional(v -> Optional.empty()).test(1L).assertResult();
    }

    @Test
    public void backpressureMixed() {
        Flowable.range(1, 10).mapOptional(MODULO).test(0L).assertEmpty().requestMore(2).assertValuesOnly(2, 4).requestMore(2).assertValuesOnly(2, 4, 6, 8).requestMore(1).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void syncFusedAll() {
        Flowable.range(1, 5).mapOptional(Optional::of).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void asyncFusedAll() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(Optional::of).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void boundaryFusedAll() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(Optional::of).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void syncFusedNone() {
        Flowable.range(1, 5).mapOptional(v -> Optional.empty()).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult();
    }

    @Test
    public void asyncFusedNone() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(v -> Optional.empty()).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void boundaryFusedNone() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(v -> Optional.empty()).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult();
    }

    @Test
    public void syncFusedMixed() {
        Flowable.range(1, 10).mapOptional(MODULO).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void asyncFusedMixed() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        up.mapOptional(MODULO).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void boundaryFusedMixed() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        up.mapOptional(MODULO).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void allPresentConditional() {
        Flowable.range(1, 5).mapOptional(Optional::of).filter(v -> true).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void allEmptyConditional() {
        Flowable.range(1, 5).mapOptional(v -> Optional.<Integer>empty()).filter(v -> true).test().assertResult();
    }

    @Test
    public void mixedConditional() {
        Flowable.range(1, 10).mapOptional(MODULO).filter(v -> true).test().assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void mapperChashConditional() {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        source.mapOptional(v -> {
            throw new TestException();
        }).filter(v -> true).test().assertFailure(TestException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void mapperNullConditional() {
        BehaviorProcessor<Integer> source = BehaviorProcessor.createDefault(1);
        source.mapOptional(v -> null).filter(v -> true).test().assertFailure(NullPointerException.class);
        assertFalse(source.hasSubscribers());
    }

    @Test
    public void crashDropsOnNextsConditional() {
        Flowable<Integer> source = new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onNext(1);
                s.onNext(2);
            }
        };
        source.mapOptional(v -> {
            throw new TestException();
        }).filter(v -> true).test().assertFailure(TestException.class);
    }

    @Test
    public void backpressureAllConditional() {
        Flowable.range(1, 5).mapOptional(Optional::of).filter(v -> true).test(0L).assertEmpty().requestMore(2).assertValuesOnly(1, 2).requestMore(2).assertValuesOnly(1, 2, 3, 4).requestMore(1).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void backpressureNoneConditional() {
        Flowable.range(1, 5).mapOptional(v -> Optional.empty()).filter(v -> true).test(1L).assertResult();
    }

    @Test
    public void backpressureMixedConditional() {
        Flowable.range(1, 10).mapOptional(MODULO).filter(v -> true).test(0L).assertEmpty().requestMore(2).assertValuesOnly(2, 4).requestMore(2).assertValuesOnly(2, 4, 6, 8).requestMore(1).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void syncFusedAllConditional() {
        Flowable.range(1, 5).mapOptional(Optional::of).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void asyncFusedAllConditional() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(Optional::of).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void boundaryFusedAllConditiona() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(Optional::of).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void syncFusedNoneConditional() {
        Flowable.range(1, 5).mapOptional(v -> Optional.empty()).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult();
    }

    @Test
    public void asyncFusedNoneConditional() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(v -> Optional.empty()).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void boundaryFusedNoneConditional() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5);
        up.mapOptional(v -> Optional.empty()).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult();
    }

    @Test
    public void syncFusedMixedConditional() {
        Flowable.range(1, 10).mapOptional(MODULO).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void asyncFusedMixedConditional() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        up.mapOptional(MODULO).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void boundaryFusedMixedConditional() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestHelper.emit(up, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        up.mapOptional(MODULO).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void conditionalFusionNoNPE() {
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.empty().observeOn(ImmediateThinScheduler.INSTANCE).filter(v -> true).mapOptional(Optional::of).filter(v -> true).subscribe(ts);
        ts.assertResult();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableMapOptionalTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allPresent() throws java.lang.Throwable {
            this.payloads.allPresent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allEmpty() throws java.lang.Throwable {
            this.payloads.allEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixed() throws java.lang.Throwable {
            this.payloads.mixed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperChash() throws java.lang.Throwable {
            this.payloads.mapperChash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperNull() throws java.lang.Throwable {
            this.payloads.mapperNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crashDropsOnNexts() throws java.lang.Throwable {
            this.payloads.crashDropsOnNexts.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureAll() throws java.lang.Throwable {
            this.payloads.backpressureAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNone() throws java.lang.Throwable {
            this.payloads.backpressureNone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureMixed() throws java.lang.Throwable {
            this.payloads.backpressureMixed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedAll() throws java.lang.Throwable {
            this.payloads.syncFusedAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedAll() throws java.lang.Throwable {
            this.payloads.asyncFusedAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusedAll() throws java.lang.Throwable {
            this.payloads.boundaryFusedAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedNone() throws java.lang.Throwable {
            this.payloads.syncFusedNone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedNone() throws java.lang.Throwable {
            this.payloads.asyncFusedNone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusedNone() throws java.lang.Throwable {
            this.payloads.boundaryFusedNone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedMixed() throws java.lang.Throwable {
            this.payloads.syncFusedMixed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedMixed() throws java.lang.Throwable {
            this.payloads.asyncFusedMixed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusedMixed() throws java.lang.Throwable {
            this.payloads.boundaryFusedMixed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allPresentConditional() throws java.lang.Throwable {
            this.payloads.allPresentConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_allEmptyConditional() throws java.lang.Throwable {
            this.payloads.allEmptyConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mixedConditional() throws java.lang.Throwable {
            this.payloads.mixedConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperChashConditional() throws java.lang.Throwable {
            this.payloads.mapperChashConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperNullConditional() throws java.lang.Throwable {
            this.payloads.mapperNullConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crashDropsOnNextsConditional() throws java.lang.Throwable {
            this.payloads.crashDropsOnNextsConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureAllConditional() throws java.lang.Throwable {
            this.payloads.backpressureAllConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNoneConditional() throws java.lang.Throwable {
            this.payloads.backpressureNoneConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureMixedConditional() throws java.lang.Throwable {
            this.payloads.backpressureMixedConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedAllConditional() throws java.lang.Throwable {
            this.payloads.syncFusedAllConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedAllConditional() throws java.lang.Throwable {
            this.payloads.asyncFusedAllConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusedAllConditiona() throws java.lang.Throwable {
            this.payloads.boundaryFusedAllConditiona.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedNoneConditional() throws java.lang.Throwable {
            this.payloads.syncFusedNoneConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedNoneConditional() throws java.lang.Throwable {
            this.payloads.asyncFusedNoneConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusedNoneConditional() throws java.lang.Throwable {
            this.payloads.boundaryFusedNoneConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusedMixedConditional() throws java.lang.Throwable {
            this.payloads.syncFusedMixedConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusedMixedConditional() throws java.lang.Throwable {
            this.payloads.asyncFusedMixedConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_boundaryFusedMixedConditional() throws java.lang.Throwable {
            this.payloads.boundaryFusedMixedConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFusionNoNPE() throws java.lang.Throwable {
            this.payloads.conditionalFusionNoNPE.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMapOptionalTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMapOptionalTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMapOptionalTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMapOptionalTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableMapOptionalTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableMapOptionalTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableMapOptionalTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableMapOptionalTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement allPresent;

            public org.junit.runners.model.Statement allEmpty;

            public org.junit.runners.model.Statement mixed;

            public org.junit.runners.model.Statement mapperChash;

            public org.junit.runners.model.Statement mapperNull;

            public org.junit.runners.model.Statement crashDropsOnNexts;

            public org.junit.runners.model.Statement backpressureAll;

            public org.junit.runners.model.Statement backpressureNone;

            public org.junit.runners.model.Statement backpressureMixed;

            public org.junit.runners.model.Statement syncFusedAll;

            public org.junit.runners.model.Statement asyncFusedAll;

            public org.junit.runners.model.Statement boundaryFusedAll;

            public org.junit.runners.model.Statement syncFusedNone;

            public org.junit.runners.model.Statement asyncFusedNone;

            public org.junit.runners.model.Statement boundaryFusedNone;

            public org.junit.runners.model.Statement syncFusedMixed;

            public org.junit.runners.model.Statement asyncFusedMixed;

            public org.junit.runners.model.Statement boundaryFusedMixed;

            public org.junit.runners.model.Statement allPresentConditional;

            public org.junit.runners.model.Statement allEmptyConditional;

            public org.junit.runners.model.Statement mixedConditional;

            public org.junit.runners.model.Statement mapperChashConditional;

            public org.junit.runners.model.Statement mapperNullConditional;

            public org.junit.runners.model.Statement crashDropsOnNextsConditional;

            public org.junit.runners.model.Statement backpressureAllConditional;

            public org.junit.runners.model.Statement backpressureNoneConditional;

            public org.junit.runners.model.Statement backpressureMixedConditional;

            public org.junit.runners.model.Statement syncFusedAllConditional;

            public org.junit.runners.model.Statement asyncFusedAllConditional;

            public org.junit.runners.model.Statement boundaryFusedAllConditiona;

            public org.junit.runners.model.Statement syncFusedNoneConditional;

            public org.junit.runners.model.Statement asyncFusedNoneConditional;

            public org.junit.runners.model.Statement boundaryFusedNoneConditional;

            public org.junit.runners.model.Statement syncFusedMixedConditional;

            public org.junit.runners.model.Statement asyncFusedMixedConditional;

            public org.junit.runners.model.Statement boundaryFusedMixedConditional;

            public org.junit.runners.model.Statement conditionalFusionNoNPE;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.allPresent = _ClassStatement.forPayload(FlowableMapOptionalTest::allPresent, "allPresent", this);
            this.payloads.allEmpty = _ClassStatement.forPayload(FlowableMapOptionalTest::allEmpty, "allEmpty", this);
            this.payloads.mixed = _ClassStatement.forPayload(FlowableMapOptionalTest::mixed, "mixed", this);
            this.payloads.mapperChash = _ClassStatement.forPayload(FlowableMapOptionalTest::mapperChash, "mapperChash", this);
            this.payloads.mapperNull = _ClassStatement.forPayload(FlowableMapOptionalTest::mapperNull, "mapperNull", this);
            this.payloads.crashDropsOnNexts = _ClassStatement.forPayload(FlowableMapOptionalTest::crashDropsOnNexts, "crashDropsOnNexts", this);
            this.payloads.backpressureAll = _ClassStatement.forPayload(FlowableMapOptionalTest::backpressureAll, "backpressureAll", this);
            this.payloads.backpressureNone = _ClassStatement.forPayload(FlowableMapOptionalTest::backpressureNone, "backpressureNone", this);
            this.payloads.backpressureMixed = _ClassStatement.forPayload(FlowableMapOptionalTest::backpressureMixed, "backpressureMixed", this);
            this.payloads.syncFusedAll = _ClassStatement.forPayload(FlowableMapOptionalTest::syncFusedAll, "syncFusedAll", this);
            this.payloads.asyncFusedAll = _ClassStatement.forPayload(FlowableMapOptionalTest::asyncFusedAll, "asyncFusedAll", this);
            this.payloads.boundaryFusedAll = _ClassStatement.forPayload(FlowableMapOptionalTest::boundaryFusedAll, "boundaryFusedAll", this);
            this.payloads.syncFusedNone = _ClassStatement.forPayload(FlowableMapOptionalTest::syncFusedNone, "syncFusedNone", this);
            this.payloads.asyncFusedNone = _ClassStatement.forPayload(FlowableMapOptionalTest::asyncFusedNone, "asyncFusedNone", this);
            this.payloads.boundaryFusedNone = _ClassStatement.forPayload(FlowableMapOptionalTest::boundaryFusedNone, "boundaryFusedNone", this);
            this.payloads.syncFusedMixed = _ClassStatement.forPayload(FlowableMapOptionalTest::syncFusedMixed, "syncFusedMixed", this);
            this.payloads.asyncFusedMixed = _ClassStatement.forPayload(FlowableMapOptionalTest::asyncFusedMixed, "asyncFusedMixed", this);
            this.payloads.boundaryFusedMixed = _ClassStatement.forPayload(FlowableMapOptionalTest::boundaryFusedMixed, "boundaryFusedMixed", this);
            this.payloads.allPresentConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::allPresentConditional, "allPresentConditional", this);
            this.payloads.allEmptyConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::allEmptyConditional, "allEmptyConditional", this);
            this.payloads.mixedConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::mixedConditional, "mixedConditional", this);
            this.payloads.mapperChashConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::mapperChashConditional, "mapperChashConditional", this);
            this.payloads.mapperNullConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::mapperNullConditional, "mapperNullConditional", this);
            this.payloads.crashDropsOnNextsConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::crashDropsOnNextsConditional, "crashDropsOnNextsConditional", this);
            this.payloads.backpressureAllConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::backpressureAllConditional, "backpressureAllConditional", this);
            this.payloads.backpressureNoneConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::backpressureNoneConditional, "backpressureNoneConditional", this);
            this.payloads.backpressureMixedConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::backpressureMixedConditional, "backpressureMixedConditional", this);
            this.payloads.syncFusedAllConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::syncFusedAllConditional, "syncFusedAllConditional", this);
            this.payloads.asyncFusedAllConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::asyncFusedAllConditional, "asyncFusedAllConditional", this);
            this.payloads.boundaryFusedAllConditiona = _ClassStatement.forPayload(FlowableMapOptionalTest::boundaryFusedAllConditiona, "boundaryFusedAllConditiona", this);
            this.payloads.syncFusedNoneConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::syncFusedNoneConditional, "syncFusedNoneConditional", this);
            this.payloads.asyncFusedNoneConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::asyncFusedNoneConditional, "asyncFusedNoneConditional", this);
            this.payloads.boundaryFusedNoneConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::boundaryFusedNoneConditional, "boundaryFusedNoneConditional", this);
            this.payloads.syncFusedMixedConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::syncFusedMixedConditional, "syncFusedMixedConditional", this);
            this.payloads.asyncFusedMixedConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::asyncFusedMixedConditional, "asyncFusedMixedConditional", this);
            this.payloads.boundaryFusedMixedConditional = _ClassStatement.forPayload(FlowableMapOptionalTest::boundaryFusedMixedConditional, "boundaryFusedMixedConditional", this);
            this.payloads.conditionalFusionNoNPE = _ClassStatement.forPayload(FlowableMapOptionalTest::conditionalFusionNoNPE, "conditionalFusionNoNPE", this);
        }
    }
}
