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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableTakeLastTest extends RxJavaTest {

    @Test
    public void takeLastEmpty() {
        Observable<String> w = Observable.empty();
        Observable<String> take = w.takeLast(2);
        Observer<String> observer = TestHelper.mockObserver();
        take.subscribe(observer);
        verify(observer, never()).onNext(any(String.class));
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void takeLast1() {
        Observable<String> w = Observable.just("one", "two", "three");
        Observable<String> take = w.takeLast(2);
        Observer<String> observer = TestHelper.mockObserver();
        InOrder inOrder = inOrder(observer);
        take.subscribe(observer);
        inOrder.verify(observer, times(1)).onNext("two");
        inOrder.verify(observer, times(1)).onNext("three");
        verify(observer, never()).onNext("one");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void takeLast2() {
        Observable<String> w = Observable.just("one");
        Observable<String> take = w.takeLast(10);
        Observer<String> observer = TestHelper.mockObserver();
        take.subscribe(observer);
        verify(observer, times(1)).onNext("one");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void takeLastWithZeroCount() {
        Observable<String> w = Observable.just("one");
        Observable<String> take = w.takeLast(0);
        Observer<String> observer = TestHelper.mockObserver();
        take.subscribe(observer);
        verify(observer, never()).onNext("one");
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void takeLastWithNegativeCount() {
        Observable.just("one").takeLast(-1);
    }

    @Test
    public void backpressure1() {
        TestObserver<Integer> to = new TestObserver<>();
        Observable.range(1, 100000).takeLast(1).observeOn(Schedulers.newThread()).map(newSlowProcessor()).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors();
        to.assertValue(100000);
    }

    @Test
    public void backpressure2() {
        TestObserver<Integer> to = new TestObserver<>();
        Observable.range(1, 100000).takeLast(Flowable.bufferSize() * 4).observeOn(Schedulers.newThread()).map(newSlowProcessor()).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors();
        assertEquals(Flowable.bufferSize() * 4, to.values().size());
    }

    private Function<Integer, Integer> newSlowProcessor() {
        return new Function<Integer, Integer>() {

            int c;

            @Override
            public Integer apply(Integer i) {
                if (c++ < 100) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }
                }
                return i;
            }
        };
    }

    @Test
    public void issue1522() {
        // https://github.com/ReactiveX/RxJava/issues/1522
        assertNull(Observable.empty().count().filter(new Predicate<Long>() {

            @Override
            public boolean test(Long v) {
                return false;
            }
        }).blockingGet());
    }

    @Test
    public void unsubscribeTakesEffectEarlyOnFastPath() {
        final AtomicInteger count = new AtomicInteger();
        Observable.range(0, 100000).takeLast(100000).subscribe(new DefaultObserver<Integer>() {

            @Override
            public void onStart() {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                count.incrementAndGet();
                cancel();
            }
        });
        assertEquals(1, count.get());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.range(1, 10).takeLast(5));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Object> o) throws Exception {
                return o.takeLast(5);
            }
        });
    }

    @Test
    public void error() {
        Observable.error(new TestException()).takeLast(5).test().assertFailure(TestException.class);
    }

    @Test
    public void takeLastTake() {
        Observable.range(1, 10).takeLast(5).take(2).test().assertResult(6, 7);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableTakeLastTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastEmpty() throws java.lang.Throwable {
            this.payloads.takeLastEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLast1() throws java.lang.Throwable {
            this.payloads.takeLast1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLast2() throws java.lang.Throwable {
            this.payloads.takeLast2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastWithZeroCount() throws java.lang.Throwable {
            this.payloads.takeLastWithZeroCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastWithNegativeCount() throws java.lang.Throwable {
            this.payloads.takeLastWithNegativeCount.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure1() throws java.lang.Throwable {
            this.payloads.backpressure1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressure2() throws java.lang.Throwable {
            this.payloads.backpressure2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1522() throws java.lang.Throwable {
            this.payloads.issue1522.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeTakesEffectEarlyOnFastPath() throws java.lang.Throwable {
            this.payloads.unsubscribeTakesEffectEarlyOnFastPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeLastTake() throws java.lang.Throwable {
            this.payloads.takeLastTake.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeLastTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeLastTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeLastTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeLastTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableTakeLastTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableTakeLastTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableTakeLastTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableTakeLastTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement takeLastEmpty;

            public org.junit.runners.model.Statement takeLast1;

            public org.junit.runners.model.Statement takeLast2;

            public org.junit.runners.model.Statement takeLastWithZeroCount;

            public org.junit.runners.model.Statement takeLastWithNegativeCount;

            public org.junit.runners.model.Statement backpressure1;

            public org.junit.runners.model.Statement backpressure2;

            public org.junit.runners.model.Statement issue1522;

            public org.junit.runners.model.Statement unsubscribeTakesEffectEarlyOnFastPath;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement takeLastTake;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.takeLastEmpty = _ClassStatement.forPayload(ObservableTakeLastTest::takeLastEmpty, "takeLastEmpty", this);
            this.payloads.takeLast1 = _ClassStatement.forPayload(ObservableTakeLastTest::takeLast1, "takeLast1", this);
            this.payloads.takeLast2 = _ClassStatement.forPayload(ObservableTakeLastTest::takeLast2, "takeLast2", this);
            this.payloads.takeLastWithZeroCount = _ClassStatement.forPayload(ObservableTakeLastTest::takeLastWithZeroCount, "takeLastWithZeroCount", this);
            this.payloads.takeLastWithNegativeCount = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableTakeLastTest::takeLastWithNegativeCount, java.lang.IllegalArgumentException.class), "takeLastWithNegativeCount", this);
            this.payloads.backpressure1 = _ClassStatement.forPayload(ObservableTakeLastTest::backpressure1, "backpressure1", this);
            this.payloads.backpressure2 = _ClassStatement.forPayload(ObservableTakeLastTest::backpressure2, "backpressure2", this);
            this.payloads.issue1522 = _ClassStatement.forPayload(ObservableTakeLastTest::issue1522, "issue1522", this);
            this.payloads.unsubscribeTakesEffectEarlyOnFastPath = _ClassStatement.forPayload(ObservableTakeLastTest::unsubscribeTakesEffectEarlyOnFastPath, "unsubscribeTakesEffectEarlyOnFastPath", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableTakeLastTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableTakeLastTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.error = _ClassStatement.forPayload(ObservableTakeLastTest::error, "error", this);
            this.payloads.takeLastTake = _ClassStatement.forPayload(ObservableTakeLastTest::takeLastTake, "takeLastTake", this);
        }
    }
}
