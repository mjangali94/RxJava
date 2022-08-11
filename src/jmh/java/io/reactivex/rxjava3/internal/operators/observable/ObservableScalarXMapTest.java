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
package io.reactivex.rxjava3.internal.operators.observable;

import static org.junit.Assert.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava3.internal.operators.observable.ObservableScalarXMap.ScalarDisposable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableScalarXMapTest extends RxJavaTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(ObservableScalarXMap.class);
    }

    static final class CallablePublisher implements ObservableSource<Integer>, Supplier<Integer> {

        @Override
        public void subscribe(Observer<? super Integer> observer) {
            EmptyDisposable.error(new TestException(), observer);
        }

        @Override
        public Integer get() throws Exception {
            throw new TestException();
        }
    }

    static final class EmptyCallablePublisher implements ObservableSource<Integer>, Supplier<Integer> {

        @Override
        public void subscribe(Observer<? super Integer> observer) {
            EmptyDisposable.complete(observer);
        }

        @Override
        public Integer get() throws Exception {
            return null;
        }
    }

    static final class OneCallablePublisher implements ObservableSource<Integer>, Supplier<Integer> {

        @Override
        public void subscribe(Observer<? super Integer> observer) {
            ScalarDisposable<Integer> sd = new ScalarDisposable<>(observer, 1);
            observer.onSubscribe(sd);
            sd.run();
        }

        @Override
        public Integer get() throws Exception {
            return 1;
        }
    }

    @Test
    public void tryScalarXMap() {
        TestObserver<Integer> to = new TestObserver<>();
        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new CallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return Observable.just(1);
            }
        }));
        to.assertFailure(TestException.class);
    }

    @Test
    public void emptyXMap() {
        TestObserver<Integer> to = new TestObserver<>();
        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new EmptyCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return Observable.just(1);
            }
        }));
        to.assertResult();
    }

    @Test
    public void mapperCrashes() {
        TestObserver<Integer> to = new TestObserver<>();
        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                throw new TestException();
            }
        }));
        to.assertFailure(TestException.class);
    }

    @Test
    public void mapperToJust() {
        TestObserver<Integer> to = new TestObserver<>();
        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return Observable.just(1);
            }
        }));
        to.assertResult(1);
    }

    @Test
    public void mapperToEmpty() {
        TestObserver<Integer> to = new TestObserver<>();
        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return Observable.empty();
            }
        }));
        to.assertResult();
    }

    @Test
    public void mapperToCrashingCallable() {
        TestObserver<Integer> to = new TestObserver<>();
        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return new CallablePublisher();
            }
        }));
        to.assertFailure(TestException.class);
    }

    @Test
    public void scalarMapToEmpty() {
        ObservableScalarXMap.scalarXMap(1, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.empty();
            }
        }).test().assertResult();
    }

    @Test
    public void scalarMapToCrashingCallable() {
        ObservableScalarXMap.scalarXMap(1, new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return new CallablePublisher();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void scalarDisposableStateCheck() {
        TestObserver<Integer> to = new TestObserver<>();
        ScalarDisposable<Integer> sd = new ScalarDisposable<>(to, 1);
        to.onSubscribe(sd);
        assertFalse(sd.isDisposed());
        assertTrue(sd.isEmpty());
        sd.run();
        assertTrue(sd.isDisposed());
        assertTrue(sd.isEmpty());
        to.assertResult(1);
        try {
            sd.offer(1);
            fail("Should have thrown");
        } catch (UnsupportedOperationException ex) {
        // expected
        }
        try {
            sd.offer(1, 2);
            fail("Should have thrown");
        } catch (UnsupportedOperationException ex) {
        // expected
        }
    }

    @Test
    public void scalarDisposableRunDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            TestObserver<Integer> to = new TestObserver<>();
            final ScalarDisposable<Integer> sd = new ScalarDisposable<>(to, 1);
            to.onSubscribe(sd);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    sd.run();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sd.dispose();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void scalarDisposbleWrongFusion() {
        TestObserver<Integer> to = new TestObserver<>();
        final ScalarDisposable<Integer> sd = new ScalarDisposable<>(to, 1);
        to.onSubscribe(sd);
        assertEquals(QueueFuseable.NONE, sd.requestFusion(QueueFuseable.ASYNC));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableScalarXMapTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_utilityClass() throws java.lang.Throwable {
            this.payloads.utilityClass.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryScalarXMap() throws java.lang.Throwable {
            this.payloads.tryScalarXMap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyXMap() throws java.lang.Throwable {
            this.payloads.emptyXMap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperCrashes() throws java.lang.Throwable {
            this.payloads.mapperCrashes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperToJust() throws java.lang.Throwable {
            this.payloads.mapperToJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperToEmpty() throws java.lang.Throwable {
            this.payloads.mapperToEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperToCrashingCallable() throws java.lang.Throwable {
            this.payloads.mapperToCrashingCallable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarMapToEmpty() throws java.lang.Throwable {
            this.payloads.scalarMapToEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarMapToCrashingCallable() throws java.lang.Throwable {
            this.payloads.scalarMapToCrashingCallable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarDisposableStateCheck() throws java.lang.Throwable {
            this.payloads.scalarDisposableStateCheck.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarDisposableRunDisposeRace() throws java.lang.Throwable {
            this.payloads.scalarDisposableRunDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_scalarDisposbleWrongFusion() throws java.lang.Throwable {
            this.payloads.scalarDisposbleWrongFusion.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableScalarXMapTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableScalarXMapTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableScalarXMapTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableScalarXMapTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableScalarXMapTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableScalarXMapTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableScalarXMapTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableScalarXMapTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement utilityClass;

            public org.junit.runners.model.Statement tryScalarXMap;

            public org.junit.runners.model.Statement emptyXMap;

            public org.junit.runners.model.Statement mapperCrashes;

            public org.junit.runners.model.Statement mapperToJust;

            public org.junit.runners.model.Statement mapperToEmpty;

            public org.junit.runners.model.Statement mapperToCrashingCallable;

            public org.junit.runners.model.Statement scalarMapToEmpty;

            public org.junit.runners.model.Statement scalarMapToCrashingCallable;

            public org.junit.runners.model.Statement scalarDisposableStateCheck;

            public org.junit.runners.model.Statement scalarDisposableRunDisposeRace;

            public org.junit.runners.model.Statement scalarDisposbleWrongFusion;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.utilityClass = _ClassStatement.forPayload(ObservableScalarXMapTest::utilityClass, "utilityClass", this);
            this.payloads.tryScalarXMap = _ClassStatement.forPayload(ObservableScalarXMapTest::tryScalarXMap, "tryScalarXMap", this);
            this.payloads.emptyXMap = _ClassStatement.forPayload(ObservableScalarXMapTest::emptyXMap, "emptyXMap", this);
            this.payloads.mapperCrashes = _ClassStatement.forPayload(ObservableScalarXMapTest::mapperCrashes, "mapperCrashes", this);
            this.payloads.mapperToJust = _ClassStatement.forPayload(ObservableScalarXMapTest::mapperToJust, "mapperToJust", this);
            this.payloads.mapperToEmpty = _ClassStatement.forPayload(ObservableScalarXMapTest::mapperToEmpty, "mapperToEmpty", this);
            this.payloads.mapperToCrashingCallable = _ClassStatement.forPayload(ObservableScalarXMapTest::mapperToCrashingCallable, "mapperToCrashingCallable", this);
            this.payloads.scalarMapToEmpty = _ClassStatement.forPayload(ObservableScalarXMapTest::scalarMapToEmpty, "scalarMapToEmpty", this);
            this.payloads.scalarMapToCrashingCallable = _ClassStatement.forPayload(ObservableScalarXMapTest::scalarMapToCrashingCallable, "scalarMapToCrashingCallable", this);
            this.payloads.scalarDisposableStateCheck = _ClassStatement.forPayload(ObservableScalarXMapTest::scalarDisposableStateCheck, "scalarDisposableStateCheck", this);
            this.payloads.scalarDisposableRunDisposeRace = _ClassStatement.forPayload(ObservableScalarXMapTest::scalarDisposableRunDisposeRace, "scalarDisposableRunDisposeRace", this);
            this.payloads.scalarDisposbleWrongFusion = _ClassStatement.forPayload(ObservableScalarXMapTest::scalarDisposbleWrongFusion, "scalarDisposbleWrongFusion", this);
        }
    }
}
