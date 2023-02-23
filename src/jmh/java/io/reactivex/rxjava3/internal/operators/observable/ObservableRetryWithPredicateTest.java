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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.mockito.InOrder;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableRetryWithPredicateTest extends RxJavaTest {

    BiPredicate<Integer, Throwable> retryTwice = new BiPredicate<Integer, Throwable>() {

        @Override
        public boolean test(Integer t1, Throwable t2) {
            return t1 <= 2;
        }
    };

    BiPredicate<Integer, Throwable> retry5 = new BiPredicate<Integer, Throwable>() {

        @Override
        public boolean test(Integer t1, Throwable t2) {
            return t1 <= 5;
        }
    };

    BiPredicate<Integer, Throwable> retryOnTestException = new BiPredicate<Integer, Throwable>() {

        @Override
        public boolean test(Integer t1, Throwable t2) {
            return t2 instanceof IOException;
        }
    };

    @Test
    public void withNothingToRetry() {
        Observable<Integer> source = Observable.range(0, 3);
        Observer<Integer> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.retry(retryTwice).subscribe(o);
        inOrder.verify(o).onNext(0);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void retryTwice() {
        Observable<Integer> source = Observable.unsafeCreate(new ObservableSource<Integer>() {

            int count;

            @Override
            public void subscribe(Observer<? super Integer> t1) {
                t1.onSubscribe(Disposable.empty());
                count++;
                t1.onNext(0);
                t1.onNext(1);
                if (count == 1) {
                    t1.onError(new TestException());
                    return;
                }
                t1.onNext(2);
                t1.onNext(3);
                t1.onComplete();
            }
        });
        Observer<Integer> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.retry(retryTwice).subscribe(o);
        inOrder.verify(o).onNext(0);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(0);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void retryTwiceAndGiveUp() {
        Observable<Integer> source = Observable.unsafeCreate(new ObservableSource<Integer>() {

            @Override
            public void subscribe(Observer<? super Integer> t1) {
                t1.onSubscribe(Disposable.empty());
                t1.onNext(0);
                t1.onNext(1);
                t1.onError(new TestException());
            }
        });
        Observer<Integer> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.retry(retryTwice).subscribe(o);
        inOrder.verify(o).onNext(0);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(0);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(0);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onError(any(TestException.class));
        verify(o, never()).onComplete();
    }

    @Test
    public void retryOnSpecificException() {
        Observable<Integer> source = Observable.unsafeCreate(new ObservableSource<Integer>() {

            int count;

            @Override
            public void subscribe(Observer<? super Integer> t1) {
                t1.onSubscribe(Disposable.empty());
                count++;
                t1.onNext(0);
                t1.onNext(1);
                if (count == 1) {
                    t1.onError(new IOException());
                    return;
                }
                t1.onNext(2);
                t1.onNext(3);
                t1.onComplete();
            }
        });
        Observer<Integer> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.retry(retryOnTestException).subscribe(o);
        inOrder.verify(o).onNext(0);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(0);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o).onComplete();
        verify(o, never()).onError(any(Throwable.class));
    }

    @Test
    public void retryOnSpecificExceptionAndNotOther() {
        final IOException ioe = new IOException();
        final TestException te = new TestException();
        Observable<Integer> source = Observable.unsafeCreate(new ObservableSource<Integer>() {

            int count;

            @Override
            public void subscribe(Observer<? super Integer> t1) {
                t1.onSubscribe(Disposable.empty());
                count++;
                t1.onNext(0);
                t1.onNext(1);
                if (count == 1) {
                    t1.onError(ioe);
                    return;
                }
                t1.onNext(2);
                t1.onNext(3);
                t1.onError(te);
            }
        });
        Observer<Integer> o = TestHelper.mockObserver();
        InOrder inOrder = inOrder(o);
        source.retry(retryOnTestException).subscribe(o);
        inOrder.verify(o).onNext(0);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(0);
        inOrder.verify(o).onNext(1);
        inOrder.verify(o).onNext(2);
        inOrder.verify(o).onNext(3);
        inOrder.verify(o).onError(te);
        verify(o, never()).onError(ioe);
        verify(o, never()).onComplete();
    }

    @Test
    public void unsubscribeFromRetry() {
        PublishSubject<Integer> subject = PublishSubject.create();
        final AtomicInteger count = new AtomicInteger(0);
        Disposable sub = subject.retry(retryTwice).subscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer n) {
                count.incrementAndGet();
            }
        });
        subject.onNext(1);
        sub.dispose();
        subject.onNext(2);
        assertEquals(1, count.get());
    }

    @Test
    public void unsubscribeAfterError() {
        Observer<Long> observer = TestHelper.mockObserver();
        // Observable that always fails after 100ms
        ObservableRetryTest.SlowObservable so = new ObservableRetryTest.SlowObservable(100, 0, "testUnsubscribeAfterError");
        Observable<Long> o = Observable.unsafeCreate(so).retry(retry5);
        ObservableRetryTest.AsyncObserver<Long> async = new ObservableRetryTest.AsyncObserver<>(observer);
        o.subscribe(async);
        async.await();
        InOrder inOrder = inOrder(observer);
        // Should fail once
        inOrder.verify(observer, times(1)).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onComplete();
        assertEquals("Start 6 threads, retry 5 then fail on 6", 6, so.efforts.get());
        assertEquals("Only 1 active subscription", 1, so.maxActive.get());
    }

    @Test
    public void timeoutWithRetry() {
        Observer<Long> observer = TestHelper.mockObserver();
        // Observable that sends every 100ms (timeout fails instead)
        ObservableRetryTest.SlowObservable so = new ObservableRetryTest.SlowObservable(100, 10, "testTimeoutWithRetry");
        Observable<Long> o = Observable.unsafeCreate(so).timeout(80, TimeUnit.MILLISECONDS).retry(retry5);
        ObservableRetryTest.AsyncObserver<Long> async = new ObservableRetryTest.AsyncObserver<>(observer);
        o.subscribe(async);
        async.await();
        InOrder inOrder = inOrder(observer);
        // Should fail once
        inOrder.verify(observer, times(1)).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onComplete();
        assertEquals("Start 6 threads, retry 5 then fail on 6", 6, so.efforts.get());
    }

    @Test
    public void issue2826() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        final RuntimeException e = new RuntimeException("You shall not pass");
        final AtomicInteger c = new AtomicInteger();
        Observable.just(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                c.incrementAndGet();
                throw e;
            }
        }).retry(retry5).subscribe(to);
        to.assertTerminated();
        assertEquals(6, c.get());
        assertEquals(Collections.singletonList(e), to.errors());
    }

    @Test
    public void justAndRetry() throws Exception {
        final AtomicBoolean throwException = new AtomicBoolean(true);
        int value = Observable.just(1).map(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                if (throwException.compareAndSet(true, false)) {
                    throw new TestException();
                }
                return t1;
            }
        }).retry(1).blockingSingle();
        assertEquals(1, value);
    }

    @Test
    public void issue3008RetryWithPredicate() {
        final List<Long> list = new CopyOnWriteArrayList<>();
        final AtomicBoolean isFirst = new AtomicBoolean(true);
        Observable.<Long>just(1L, 2L, 3L).map(new Function<Long, Long>() {

            @Override
            public Long apply(Long x) {
                // System.out.println("map " + x);
                if (x == 2 && isFirst.getAndSet(false)) {
                    throw new RuntimeException("retryable error");
                }
                return x;
            }
        }).retry(new BiPredicate<Integer, Throwable>() {

            @Override
            public boolean test(Integer t1, Throwable t2) {
                return true;
            }
        }).forEach(new Consumer<Long>() {

            @Override
            public void accept(Long t) {
                // System.out.println(t);
                list.add(t);
            }
        });
        assertEquals(Arrays.asList(1L, 1L, 2L, 3L), list);
    }

    @Test
    public void issue3008RetryInfinite() {
        final List<Long> list = new CopyOnWriteArrayList<>();
        final AtomicBoolean isFirst = new AtomicBoolean(true);
        Observable.<Long>just(1L, 2L, 3L).map(new Function<Long, Long>() {

            @Override
            public Long apply(Long x) {
                // System.out.println("map " + x);
                if (x == 2 && isFirst.getAndSet(false)) {
                    throw new RuntimeException("retryable error");
                }
                return x;
            }
        }).retry().forEach(new Consumer<Long>() {

            @Override
            public void accept(Long t) {
                // System.out.println(t);
                list.add(t);
            }
        });
        assertEquals(Arrays.asList(1L, 1L, 2L, 3L), list);
    }

    @Test
    public void predicateThrows() {
        TestObserverEx<Object> to = Observable.error(new TestException("Outer")).retry(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable e) throws Exception {
                throw new TestException("Inner");
            }
        }).to(TestHelper.testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(errors, 0, TestException.class, "Outer");
        TestHelper.assertError(errors, 1, TestException.class, "Inner");
    }

    @Test
    public void dontRetry() {
        Observable.error(new TestException("Outer")).retry(Functions.alwaysFalse()).to(TestHelper.testConsumer()).assertFailureAndMessage(TestException.class, "Outer");
    }

    @Test
    @SuppressUndeliverable
    public void retryDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = ps.retry(Functions.alwaysTrue()).test();
            final TestException ex = new TestException();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onError(ex);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
            to.assertEmpty();
        }
    }

    @Test
    public void bipredicateThrows() {
        TestObserverEx<Object> to = Observable.error(new TestException("Outer")).retry(new BiPredicate<Integer, Throwable>() {

            @Override
            public boolean test(Integer n, Throwable e) throws Exception {
                throw new TestException("Inner");
            }
        }).to(TestHelper.testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(errors, 0, TestException.class, "Outer");
        TestHelper.assertError(errors, 1, TestException.class, "Inner");
    }

    @Test
    @SuppressUndeliverable
    public void retryBiPredicateDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Integer> ps = PublishSubject.create();
            final TestObserver<Integer> to = ps.retry(new BiPredicate<Object, Object>() {

                @Override
                public boolean test(Object t1, Object t2) throws Exception {
                    return true;
                }
            }).test();
            final TestException ex = new TestException();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    ps.onError(ex);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    to.dispose();
                }
            };
            TestHelper.race(r1, r2);
            to.assertEmpty();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableRetryWithPredicateTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withNothingToRetry() throws java.lang.Throwable {
            this.payloads.withNothingToRetry.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTwice() throws java.lang.Throwable {
            this.payloads.retryTwice.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTwiceAndGiveUp() throws java.lang.Throwable {
            this.payloads.retryTwiceAndGiveUp.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryOnSpecificException() throws java.lang.Throwable {
            this.payloads.retryOnSpecificException.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryOnSpecificExceptionAndNotOther() throws java.lang.Throwable {
            this.payloads.retryOnSpecificExceptionAndNotOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeFromRetry() throws java.lang.Throwable {
            this.payloads.unsubscribeFromRetry.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeAfterError() throws java.lang.Throwable {
            this.payloads.unsubscribeAfterError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutWithRetry() throws java.lang.Throwable {
            this.payloads.timeoutWithRetry.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue2826() throws java.lang.Throwable {
            this.payloads.issue2826.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justAndRetry() throws java.lang.Throwable {
            this.payloads.justAndRetry.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue3008RetryWithPredicate() throws java.lang.Throwable {
            this.payloads.issue3008RetryWithPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue3008RetryInfinite() throws java.lang.Throwable {
            this.payloads.issue3008RetryInfinite.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_predicateThrows() throws java.lang.Throwable {
            this.payloads.predicateThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dontRetry() throws java.lang.Throwable {
            this.payloads.dontRetry.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryDisposeRace() throws java.lang.Throwable {
            this.payloads.retryDisposeRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_bipredicateThrows() throws java.lang.Throwable {
            this.payloads.bipredicateThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryBiPredicateDisposeRace() throws java.lang.Throwable {
            this.payloads.retryBiPredicateDisposeRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRetryWithPredicateTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRetryWithPredicateTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRetryWithPredicateTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRetryWithPredicateTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableRetryWithPredicateTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRetryWithPredicateTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableRetryWithPredicateTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableRetryWithPredicateTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement withNothingToRetry;

            public org.junit.runners.model.Statement retryTwice;

            public org.junit.runners.model.Statement retryTwiceAndGiveUp;

            public org.junit.runners.model.Statement retryOnSpecificException;

            public org.junit.runners.model.Statement retryOnSpecificExceptionAndNotOther;

            public org.junit.runners.model.Statement unsubscribeFromRetry;

            public org.junit.runners.model.Statement unsubscribeAfterError;

            public org.junit.runners.model.Statement timeoutWithRetry;

            public org.junit.runners.model.Statement issue2826;

            public org.junit.runners.model.Statement justAndRetry;

            public org.junit.runners.model.Statement issue3008RetryWithPredicate;

            public org.junit.runners.model.Statement issue3008RetryInfinite;

            public org.junit.runners.model.Statement predicateThrows;

            public org.junit.runners.model.Statement dontRetry;

            public org.junit.runners.model.Statement retryDisposeRace;

            public org.junit.runners.model.Statement bipredicateThrows;

            public org.junit.runners.model.Statement retryBiPredicateDisposeRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.withNothingToRetry = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::withNothingToRetry, "withNothingToRetry", this);
            this.payloads.retryTwice = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::retryTwice, "retryTwice", this);
            this.payloads.retryTwiceAndGiveUp = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::retryTwiceAndGiveUp, "retryTwiceAndGiveUp", this);
            this.payloads.retryOnSpecificException = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::retryOnSpecificException, "retryOnSpecificException", this);
            this.payloads.retryOnSpecificExceptionAndNotOther = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::retryOnSpecificExceptionAndNotOther, "retryOnSpecificExceptionAndNotOther", this);
            this.payloads.unsubscribeFromRetry = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::unsubscribeFromRetry, "unsubscribeFromRetry", this);
            this.payloads.unsubscribeAfterError = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::unsubscribeAfterError, "unsubscribeAfterError", this);
            this.payloads.timeoutWithRetry = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::timeoutWithRetry, "timeoutWithRetry", this);
            this.payloads.issue2826 = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::issue2826, "issue2826", this);
            this.payloads.justAndRetry = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::justAndRetry, "justAndRetry", this);
            this.payloads.issue3008RetryWithPredicate = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::issue3008RetryWithPredicate, "issue3008RetryWithPredicate", this);
            this.payloads.issue3008RetryInfinite = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::issue3008RetryInfinite, "issue3008RetryInfinite", this);
            this.payloads.predicateThrows = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::predicateThrows, "predicateThrows", this);
            this.payloads.dontRetry = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::dontRetry, "dontRetry", this);
            this.payloads.retryDisposeRace = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::retryDisposeRace, "retryDisposeRace", this);
            this.payloads.bipredicateThrows = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::bipredicateThrows, "bipredicateThrows", this);
            this.payloads.retryBiPredicateDisposeRace = _ClassStatement.forPayload(ObservableRetryWithPredicateTest::retryBiPredicateDisposeRace, "retryBiPredicateDisposeRace", this);
        }
    }
}
