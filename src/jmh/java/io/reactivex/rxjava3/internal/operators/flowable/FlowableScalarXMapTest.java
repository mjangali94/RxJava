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

import static org.junit.Assert.*;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.subscriptions.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableScalarXMapTest extends RxJavaTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(FlowableScalarXMap.class);
    }

    static final class CallablePublisher implements Publisher<Integer>, Supplier<Integer> {

        @Override
        public void subscribe(Subscriber<? super Integer> s) {
            EmptySubscription.error(new TestException(), s);
        }

        @Override
        public Integer get() throws Exception {
            throw new TestException();
        }
    }

    static final class EmptyCallablePublisher implements Publisher<Integer>, Supplier<Integer> {

        @Override
        public void subscribe(Subscriber<? super Integer> s) {
            EmptySubscription.complete(s);
        }

        @Override
        public Integer get() throws Exception {
            return null;
        }
    }

    static final class OneCallablePublisher implements Publisher<Integer>, Supplier<Integer> {

        @Override
        public void subscribe(Subscriber<? super Integer> s) {
            s.onSubscribe(new ScalarSubscription<>(s, 1));
        }

        @Override
        public Integer get() throws Exception {
            return 1;
        }
    }

    @Test
    public void tryScalarXMap() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        assertTrue(FlowableScalarXMap.tryScalarXMapSubscribe(new CallablePublisher(), ts, new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer f) throws Exception {
                return Flowable.just(1);
            }
        }));
        ts.assertFailure(TestException.class);
    }

    @Test
    public void emptyXMap() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        assertTrue(FlowableScalarXMap.tryScalarXMapSubscribe(new EmptyCallablePublisher(), ts, new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer f) throws Exception {
                return Flowable.just(1);
            }
        }));
        ts.assertResult();
    }

    @Test
    public void mapperCrashes() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        assertTrue(FlowableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), ts, new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer f) throws Exception {
                throw new TestException();
            }
        }));
        ts.assertFailure(TestException.class);
    }

    @Test
    public void mapperToJust() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        assertTrue(FlowableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), ts, new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer f) throws Exception {
                return Flowable.just(1);
            }
        }));
        ts.assertResult(1);
    }

    @Test
    public void mapperToEmpty() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        assertTrue(FlowableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), ts, new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer f) throws Exception {
                return Flowable.empty();
            }
        }));
        ts.assertResult();
    }

    @Test
    public void mapperToCrashingCallable() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        assertTrue(FlowableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), ts, new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer f) throws Exception {
                return new CallablePublisher();
            }
        }));
        ts.assertFailure(TestException.class);
    }

    @Test
    public void scalarMapToEmpty() {
        FlowableScalarXMap.scalarXMap(1, new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.empty();
            }
        }).test().assertResult();
    }

    @Test
    public void scalarMapToCrashingCallable() {
        FlowableScalarXMap.scalarXMap(1, new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return new CallablePublisher();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void scalarDisposableStateCheck() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        ScalarSubscription<Integer> sd = new ScalarSubscription<>(ts, 1);
        ts.onSubscribe(sd);
        assertFalse(sd.isCancelled());
        assertTrue(sd.isEmpty());
        sd.request(1);
        assertFalse(sd.isCancelled());
        assertTrue(sd.isEmpty());
        ts.assertResult(1);
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
            TestSubscriber<Integer> ts = new TestSubscriber<>();
            final ScalarSubscription<Integer> sd = new ScalarSubscription<>(ts, 1);
            ts.onSubscribe(sd);
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    sd.request(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    sd.cancel();
                }
            };
            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void cancelled() {
        ScalarSubscription<Integer> scalar = new ScalarSubscription<>(new TestSubscriber<>(), 1);
        assertFalse(scalar.isCancelled());
        scalar.cancel();
        assertTrue(scalar.isCancelled());
    }

    @Test
    public void mapToNonScalar() {
        Flowable.fromCallable(() -> 1).concatMap(v -> Flowable.range(1, 5)).test().assertResult(1, 2, 3, 4, 5);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableScalarXMapTest instance;

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
        public void benchmark_cancelled() throws java.lang.Throwable {
            this.payloads.cancelled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapToNonScalar() throws java.lang.Throwable {
            this.payloads.mapToNonScalar.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableScalarXMapTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableScalarXMapTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableScalarXMapTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableScalarXMapTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableScalarXMapTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableScalarXMapTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableScalarXMapTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableScalarXMapTest.class, name);
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

            public org.junit.runners.model.Statement cancelled;

            public org.junit.runners.model.Statement mapToNonScalar;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.utilityClass = _ClassStatement.forPayload(FlowableScalarXMapTest::utilityClass, "utilityClass", this);
            this.payloads.tryScalarXMap = _ClassStatement.forPayload(FlowableScalarXMapTest::tryScalarXMap, "tryScalarXMap", this);
            this.payloads.emptyXMap = _ClassStatement.forPayload(FlowableScalarXMapTest::emptyXMap, "emptyXMap", this);
            this.payloads.mapperCrashes = _ClassStatement.forPayload(FlowableScalarXMapTest::mapperCrashes, "mapperCrashes", this);
            this.payloads.mapperToJust = _ClassStatement.forPayload(FlowableScalarXMapTest::mapperToJust, "mapperToJust", this);
            this.payloads.mapperToEmpty = _ClassStatement.forPayload(FlowableScalarXMapTest::mapperToEmpty, "mapperToEmpty", this);
            this.payloads.mapperToCrashingCallable = _ClassStatement.forPayload(FlowableScalarXMapTest::mapperToCrashingCallable, "mapperToCrashingCallable", this);
            this.payloads.scalarMapToEmpty = _ClassStatement.forPayload(FlowableScalarXMapTest::scalarMapToEmpty, "scalarMapToEmpty", this);
            this.payloads.scalarMapToCrashingCallable = _ClassStatement.forPayload(FlowableScalarXMapTest::scalarMapToCrashingCallable, "scalarMapToCrashingCallable", this);
            this.payloads.scalarDisposableStateCheck = _ClassStatement.forPayload(FlowableScalarXMapTest::scalarDisposableStateCheck, "scalarDisposableStateCheck", this);
            this.payloads.scalarDisposableRunDisposeRace = _ClassStatement.forPayload(FlowableScalarXMapTest::scalarDisposableRunDisposeRace, "scalarDisposableRunDisposeRace", this);
            this.payloads.cancelled = _ClassStatement.forPayload(FlowableScalarXMapTest::cancelled, "cancelled", this);
            this.payloads.mapToNonScalar = _ClassStatement.forPayload(FlowableScalarXMapTest::mapToNonScalar, "mapToNonScalar", this);
        }
    }
}
