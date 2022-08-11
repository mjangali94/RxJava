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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableTakeTimedTest extends RxJavaTest {

    @Test
    public void takeTimed() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.take(1, TimeUnit.SECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        source.onNext(4);
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onNext(4);
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void takeTimedErrorBeforeTime() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.take(1, TimeUnit.SECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onError(new TestException());
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        source.onNext(4);
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onComplete();
        verify(o, never()).onNext(4);
    }

    @Test
    public void takeTimedErrorAfterTime() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<Integer> source = PublishSubject.create();
        Observable<Integer> result = source.take(1, TimeUnit.SECONDS, scheduler);
        Observer<Object> o = TestHelper.mockObserver();
        result.subscribe(o);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        source.onNext(4);
        source.onError(new TestException());
        InOrder inOrder = inOrder(o);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o).onComplete();
        inOrder.verifyNoMoreInteractions();
        verify(o, never()).onNext(4);
        verify(o, never()).onError(any(TestException.class));
    }

    @Test
    public void timedDefaultScheduler() {
        Observable.range(1, 5).take(1, TimeUnit.MINUTES).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1, 2, 3, 4, 5);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableTakeTimedTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeTimed() throws java.lang.Throwable {
            this.payloads.takeTimed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeTimedErrorBeforeTime() throws java.lang.Throwable {
            this.payloads.takeTimedErrorBeforeTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeTimedErrorAfterTime() throws java.lang.Throwable {
            this.payloads.takeTimedErrorAfterTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedDefaultScheduler() throws java.lang.Throwable {
            this.payloads.timedDefaultScheduler.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeTimedTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeTimedTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeTimedTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeTimedTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableTakeTimedTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeTimedTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableTakeTimedTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableTakeTimedTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement takeTimed;

            public org.junit.runners.model.Statement takeTimedErrorBeforeTime;

            public org.junit.runners.model.Statement takeTimedErrorAfterTime;

            public org.junit.runners.model.Statement timedDefaultScheduler;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.takeTimed = _ClassStatement.forPayload(ObservableTakeTimedTest::takeTimed, "takeTimed", this);
            this.payloads.takeTimedErrorBeforeTime = _ClassStatement.forPayload(ObservableTakeTimedTest::takeTimedErrorBeforeTime, "takeTimedErrorBeforeTime", this);
            this.payloads.takeTimedErrorAfterTime = _ClassStatement.forPayload(ObservableTakeTimedTest::takeTimedErrorAfterTime, "takeTimedErrorAfterTime", this);
            this.payloads.timedDefaultScheduler = _ClassStatement.forPayload(ObservableTakeTimedTest::timedDefaultScheduler, "timedDefaultScheduler", this);
        }
    }
}
