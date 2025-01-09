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
package io.reactivex.rxjava3.internal.observers;

import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.SpscArrayQueue;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class QueueDrainObserverTest extends RxJavaTest {

    static final QueueDrainObserver<Integer, Integer, Integer> createUnordered(TestObserver<Integer> to, final Disposable d) {
        return new QueueDrainObserver<Integer, Integer, Integer>(to, new SpscArrayQueue<>(4)) {

            @Override
            public void onNext(Integer t) {
                fastPathEmit(t, false, d);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void accept(Observer<? super Integer> a, Integer v) {
                super.accept(a, v);
                a.onNext(v);
            }
        };
    }

    static final QueueDrainObserver<Integer, Integer, Integer> createOrdered(TestObserver<Integer> to, final Disposable d) {
        return new QueueDrainObserver<Integer, Integer, Integer>(to, new SpscArrayQueue<>(4)) {

            @Override
            public void onNext(Integer t) {
                fastPathOrderedEmit(t, false, d);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void accept(Observer<? super Integer> a, Integer v) {
                super.accept(a, v);
                a.onNext(v);
            }
        };
    }

    @Test
    public void unorderedSlowPath() {
        TestObserver<Integer> to = new TestObserver<>();
        Disposable d = Disposable.empty();
        QueueDrainObserver<Integer, Integer, Integer> qd = createUnordered(to, d);
        to.onSubscribe(Disposable.empty());
        qd.enter();
        qd.onNext(1);
        to.assertEmpty();
    }

    @Test
    public void orderedSlowPath() {
        TestObserver<Integer> to = new TestObserver<>();
        Disposable d = Disposable.empty();
        QueueDrainObserver<Integer, Integer, Integer> qd = createOrdered(to, d);
        to.onSubscribe(Disposable.empty());
        qd.enter();
        qd.onNext(1);
        to.assertEmpty();
    }

    @Test
    public void orderedSlowPathNonEmptyQueue() {
        TestObserver<Integer> to = new TestObserver<>();
        Disposable d = Disposable.empty();
        QueueDrainObserver<Integer, Integer, Integer> qd = createOrdered(to, d);
        to.onSubscribe(Disposable.empty());
        qd.queue.offer(0);
        qd.onNext(1);
        to.assertValuesOnly(0, 1);
    }

    @Test
    public void unorderedOnNextRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            TestObserver<Integer> to = new TestObserver<>();
            Disposable d = Disposable.empty();
            final QueueDrainObserver<Integer, Integer, Integer> qd = createUnordered(to, d);
            to.onSubscribe(Disposable.empty());
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    qd.onNext(1);
                }
            };
            TestHelper.race(r1, r1);
            to.assertValuesOnly(1, 1);
        }
    }

    @Test
    public void orderedOnNextRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            TestObserver<Integer> to = new TestObserver<>();
            Disposable d = Disposable.empty();
            final QueueDrainObserver<Integer, Integer, Integer> qd = createOrdered(to, d);
            to.onSubscribe(Disposable.empty());
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    qd.onNext(1);
                }
            };
            TestHelper.race(r1, r1);
            to.assertValuesOnly(1, 1);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public QueueDrainObserverTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unorderedSlowPath() throws java.lang.Throwable {
            this.payloads.unorderedSlowPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_orderedSlowPath() throws java.lang.Throwable {
            this.payloads.orderedSlowPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_orderedSlowPathNonEmptyQueue() throws java.lang.Throwable {
            this.payloads.orderedSlowPathNonEmptyQueue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unorderedOnNextRace() throws java.lang.Throwable {
            this.payloads.unorderedOnNextRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_orderedOnNextRace() throws java.lang.Throwable {
            this.payloads.orderedOnNextRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainObserverTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainObserverTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainObserverTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainObserverTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new QueueDrainObserverTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<QueueDrainObserverTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(QueueDrainObserverTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(QueueDrainObserverTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement unorderedSlowPath;

            public org.junit.runners.model.Statement orderedSlowPath;

            public org.junit.runners.model.Statement orderedSlowPathNonEmptyQueue;

            public org.junit.runners.model.Statement unorderedOnNextRace;

            public org.junit.runners.model.Statement orderedOnNextRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.unorderedSlowPath = _ClassStatement.forPayload(QueueDrainObserverTest::unorderedSlowPath, "unorderedSlowPath", this);
            this.payloads.orderedSlowPath = _ClassStatement.forPayload(QueueDrainObserverTest::orderedSlowPath, "orderedSlowPath", this);
            this.payloads.orderedSlowPathNonEmptyQueue = _ClassStatement.forPayload(QueueDrainObserverTest::orderedSlowPathNonEmptyQueue, "orderedSlowPathNonEmptyQueue", this);
            this.payloads.unorderedOnNextRace = _ClassStatement.forPayload(QueueDrainObserverTest::unorderedOnNextRace, "unorderedOnNextRace", this);
            this.payloads.orderedOnNextRace = _ClassStatement.forPayload(QueueDrainObserverTest::orderedOnNextRace, "orderedOnNextRace", this);
        }
    }
}
