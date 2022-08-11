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
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableMapOptionalTest extends RxJavaTest {

    static final Function<? super Integer, Optional<? extends Integer>> MODULO = v -> v % 2 == 0 ? Optional.of(v) : Optional.<Integer>empty();

    @Test
    public void allPresent() {
        Observable.range(1, 5).mapOptional(Optional::of).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void allEmpty() {
        Observable.range(1, 5).mapOptional(v -> Optional.<Integer>empty()).test().assertResult();
    }

    @Test
    public void mixed() {
        Observable.range(1, 10).mapOptional(MODULO).test().assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void mapperChash() {
        BehaviorSubject<Integer> source = BehaviorSubject.createDefault(1);
        source.mapOptional(v -> {
            throw new TestException();
        }).test().assertFailure(TestException.class);
        assertFalse(source.hasObservers());
    }

    @Test
    public void mapperNull() {
        BehaviorSubject<Integer> source = BehaviorSubject.createDefault(1);
        source.mapOptional(v -> null).test().assertFailure(NullPointerException.class);
        assertFalse(source.hasObservers());
    }

    @Test
    public void crashDropsOnNexts() {
        Observable<Integer> source = new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext(1);
                observer.onNext(2);
            }
        };
        source.mapOptional(v -> {
            throw new TestException();
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void syncFusedAll() {
        Observable.range(1, 5).mapOptional(Optional::of).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void asyncFusedAll() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(Optional::of).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void boundaryFusedAll() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(Optional::of).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void syncFusedNone() {
        Observable.range(1, 5).mapOptional(v -> Optional.empty()).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult();
    }

    @Test
    public void asyncFusedNone() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(v -> Optional.empty()).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void boundaryFusedNone() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(v -> Optional.empty()).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult();
    }

    @Test
    public void syncFusedMixed() {
        Observable.range(1, 10).mapOptional(MODULO).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void asyncFusedMixed() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        us.mapOptional(MODULO).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void boundaryFusedMixed() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        us.mapOptional(MODULO).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void allPresentConditional() {
        Observable.range(1, 5).mapOptional(Optional::of).filter(v -> true).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void allEmptyConditional() {
        Observable.range(1, 5).mapOptional(v -> Optional.<Integer>empty()).filter(v -> true).test().assertResult();
    }

    @Test
    public void mixedConditional() {
        Observable.range(1, 10).mapOptional(MODULO).filter(v -> true).test().assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void mapperChashConditional() {
        BehaviorSubject<Integer> source = BehaviorSubject.createDefault(1);
        source.mapOptional(v -> {
            throw new TestException();
        }).filter(v -> true).test().assertFailure(TestException.class);
        assertFalse(source.hasObservers());
    }

    @Test
    public void mapperNullConditional() {
        BehaviorSubject<Integer> source = BehaviorSubject.createDefault(1);
        source.mapOptional(v -> null).filter(v -> true).test().assertFailure(NullPointerException.class);
        assertFalse(source.hasObservers());
    }

    @Test
    public void crashDropsOnNextsConditional() {
        Observable<Integer> source = new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext(1);
                observer.onNext(2);
            }
        };
        source.mapOptional(v -> {
            throw new TestException();
        }).filter(v -> true).test().assertFailure(TestException.class);
    }

    @Test
    public void syncFusedAllConditional() {
        Observable.range(1, 5).mapOptional(Optional::of).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void asyncFusedAllConditional() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(Optional::of).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void boundaryFusedAllConditiona() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(Optional::of).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void syncFusedNoneConditional() {
        Observable.range(1, 5).mapOptional(v -> Optional.empty()).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult();
    }

    @Test
    public void asyncFusedNoneConditional() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(v -> Optional.empty()).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void boundaryFusedNoneConditional() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5);
        us.mapOptional(v -> Optional.empty()).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult();
    }

    @Test
    public void syncFusedMixedConditional() {
        Observable.range(1, 10).mapOptional(MODULO).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.SYNC)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void asyncFusedMixedConditional() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        us.mapOptional(MODULO).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC)).assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void boundaryFusedMixedConditional() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestHelper.emit(us, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        us.mapOptional(MODULO).filter(v -> true).to(TestHelper.testConsumer(false, QueueFuseable.ASYNC | QueueFuseable.BOUNDARY)).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(2, 4, 6, 8, 10);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableMapOptionalTest instance;

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

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMapOptionalTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMapOptionalTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMapOptionalTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMapOptionalTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableMapOptionalTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableMapOptionalTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableMapOptionalTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableMapOptionalTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement allPresent;

            public org.junit.runners.model.Statement allEmpty;

            public org.junit.runners.model.Statement mixed;

            public org.junit.runners.model.Statement mapperChash;

            public org.junit.runners.model.Statement mapperNull;

            public org.junit.runners.model.Statement crashDropsOnNexts;

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

            public org.junit.runners.model.Statement syncFusedAllConditional;

            public org.junit.runners.model.Statement asyncFusedAllConditional;

            public org.junit.runners.model.Statement boundaryFusedAllConditiona;

            public org.junit.runners.model.Statement syncFusedNoneConditional;

            public org.junit.runners.model.Statement asyncFusedNoneConditional;

            public org.junit.runners.model.Statement boundaryFusedNoneConditional;

            public org.junit.runners.model.Statement syncFusedMixedConditional;

            public org.junit.runners.model.Statement asyncFusedMixedConditional;

            public org.junit.runners.model.Statement boundaryFusedMixedConditional;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.allPresent = _ClassStatement.forPayload(ObservableMapOptionalTest::allPresent, "allPresent", this);
            this.payloads.allEmpty = _ClassStatement.forPayload(ObservableMapOptionalTest::allEmpty, "allEmpty", this);
            this.payloads.mixed = _ClassStatement.forPayload(ObservableMapOptionalTest::mixed, "mixed", this);
            this.payloads.mapperChash = _ClassStatement.forPayload(ObservableMapOptionalTest::mapperChash, "mapperChash", this);
            this.payloads.mapperNull = _ClassStatement.forPayload(ObservableMapOptionalTest::mapperNull, "mapperNull", this);
            this.payloads.crashDropsOnNexts = _ClassStatement.forPayload(ObservableMapOptionalTest::crashDropsOnNexts, "crashDropsOnNexts", this);
            this.payloads.syncFusedAll = _ClassStatement.forPayload(ObservableMapOptionalTest::syncFusedAll, "syncFusedAll", this);
            this.payloads.asyncFusedAll = _ClassStatement.forPayload(ObservableMapOptionalTest::asyncFusedAll, "asyncFusedAll", this);
            this.payloads.boundaryFusedAll = _ClassStatement.forPayload(ObservableMapOptionalTest::boundaryFusedAll, "boundaryFusedAll", this);
            this.payloads.syncFusedNone = _ClassStatement.forPayload(ObservableMapOptionalTest::syncFusedNone, "syncFusedNone", this);
            this.payloads.asyncFusedNone = _ClassStatement.forPayload(ObservableMapOptionalTest::asyncFusedNone, "asyncFusedNone", this);
            this.payloads.boundaryFusedNone = _ClassStatement.forPayload(ObservableMapOptionalTest::boundaryFusedNone, "boundaryFusedNone", this);
            this.payloads.syncFusedMixed = _ClassStatement.forPayload(ObservableMapOptionalTest::syncFusedMixed, "syncFusedMixed", this);
            this.payloads.asyncFusedMixed = _ClassStatement.forPayload(ObservableMapOptionalTest::asyncFusedMixed, "asyncFusedMixed", this);
            this.payloads.boundaryFusedMixed = _ClassStatement.forPayload(ObservableMapOptionalTest::boundaryFusedMixed, "boundaryFusedMixed", this);
            this.payloads.allPresentConditional = _ClassStatement.forPayload(ObservableMapOptionalTest::allPresentConditional, "allPresentConditional", this);
            this.payloads.allEmptyConditional = _ClassStatement.forPayload(ObservableMapOptionalTest::allEmptyConditional, "allEmptyConditional", this);
            this.payloads.mixedConditional = _ClassStatement.forPayload(ObservableMapOptionalTest::mixedConditional, "mixedConditional", this);
            this.payloads.mapperChashConditional = _ClassStatement.forPayload(ObservableMapOptionalTest::mapperChashConditional, "mapperChashConditional", this);
            this.payloads.mapperNullConditional = _ClassStatement.forPayload(ObservableMapOptionalTest::mapperNullConditional, "mapperNullConditional", this);
            this.payloads.crashDropsOnNextsConditional = _ClassStatement.forPayload(ObservableMapOptionalTest::crashDropsOnNextsConditional, "crashDropsOnNextsConditional", this);
            this.payloads.syncFusedAllConditional = _ClassStatement.forPayload(ObservableMapOptionalTest::syncFusedAllConditional, "syncFusedAllConditional", this);
            this.payloads.asyncFusedAllConditional = _ClassStatement.forPayload(ObservableMapOptionalTest::asyncFusedAllConditional, "asyncFusedAllConditional", this);
            this.payloads.boundaryFusedAllConditiona = _ClassStatement.forPayload(ObservableMapOptionalTest::boundaryFusedAllConditiona, "boundaryFusedAllConditiona", this);
            this.payloads.syncFusedNoneConditional = _ClassStatement.forPayload(ObservableMapOptionalTest::syncFusedNoneConditional, "syncFusedNoneConditional", this);
            this.payloads.asyncFusedNoneConditional = _ClassStatement.forPayload(ObservableMapOptionalTest::asyncFusedNoneConditional, "asyncFusedNoneConditional", this);
            this.payloads.boundaryFusedNoneConditional = _ClassStatement.forPayload(ObservableMapOptionalTest::boundaryFusedNoneConditional, "boundaryFusedNoneConditional", this);
            this.payloads.syncFusedMixedConditional = _ClassStatement.forPayload(ObservableMapOptionalTest::syncFusedMixedConditional, "syncFusedMixedConditional", this);
            this.payloads.asyncFusedMixedConditional = _ClassStatement.forPayload(ObservableMapOptionalTest::asyncFusedMixedConditional, "asyncFusedMixedConditional", this);
            this.payloads.boundaryFusedMixedConditional = _ClassStatement.forPayload(ObservableMapOptionalTest::boundaryFusedMixedConditional, "boundaryFusedMixedConditional", this);
        }
    }
}
