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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.*;
import org.junit.Test;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableFlatMapStreamTest extends RxJavaTest {

    @Test
    public void empty() {
        Observable.empty().flatMapStream(v -> Stream.of(1, 2, 3, 4, 5)).test().assertResult();
    }

    @Test
    public void emptyHidden() {
        Observable.empty().hide().flatMapStream(v -> Stream.of(1, 2, 3, 4, 5)).test().assertResult();
    }

    @Test
    public void just() {
        Observable.just(1).flatMapStream(v -> Stream.of(v + 1, v + 2, v + 3, v + 4, v + 5)).test().assertResult(2, 3, 4, 5, 6);
    }

    @Test
    public void justHidden() {
        Observable.just(1).hide().flatMapStream(v -> Stream.of(v + 1, v + 2, v + 3, v + 4, v + 5)).test().assertResult(2, 3, 4, 5, 6);
    }

    @Test
    public void error() {
        Observable.error(new TestException()).flatMapStream(v -> Stream.of(1, 2, 3, 4, 5)).test().assertFailure(TestException.class);
    }

    @Test
    public void supplierFusedError() {
        Observable.fromCallable(() -> {
            throw new TestException();
        }).flatMapStream(v -> Stream.of(1, 2, 3, 4, 5)).test().assertFailure(TestException.class);
    }

    @Test
    public void errorHidden() {
        Observable.error(new TestException()).hide().flatMapStream(v -> Stream.of(1, 2, 3, 4, 5)).test().assertFailure(TestException.class);
    }

    @Test
    public void range() {
        Observable.range(1, 5).flatMapStream(v -> IntStream.range(v * 10, v * 10 + 5).boxed()).test().assertResult(10, 11, 12, 13, 14, 20, 21, 22, 23, 24, 30, 31, 32, 33, 34, 40, 41, 42, 43, 44, 50, 51, 52, 53, 54);
    }

    @Test
    public void rangeHidden() {
        Observable.range(1, 5).hide().flatMapStream(v -> IntStream.range(v * 10, v * 10 + 5).boxed()).test().assertResult(10, 11, 12, 13, 14, 20, 21, 22, 23, 24, 30, 31, 32, 33, 34, 40, 41, 42, 43, 44, 50, 51, 52, 53, 54);
    }

    @Test
    public void rangeToEmpty() {
        Observable.range(1, 5).flatMapStream(v -> Stream.of()).test().assertResult();
    }

    @Test
    public void rangeTake() {
        Observable.range(1, 5).flatMapStream(v -> IntStream.range(v * 10, v * 10 + 5).boxed()).take(12).test().assertResult(10, 11, 12, 13, 14, 20, 21, 22, 23, 24, 30, 31);
    }

    @Test
    public void rangeTakeHidden() {
        Observable.range(1, 5).hide().flatMapStream(v -> IntStream.range(v * 10, v * 10 + 5).boxed()).take(12).test().assertResult(10, 11, 12, 13, 14, 20, 21, 22, 23, 24, 30, 31);
    }

    @Test
    public void upstreamCancelled() {
        PublishSubject<Integer> ps = PublishSubject.create();
        AtomicInteger calls = new AtomicInteger();
        TestObserver<Integer> to = ps.flatMapStream(v -> Stream.of(v + 1, v + 2).onClose(() -> calls.getAndIncrement())).take(1).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertResult(2);
        assertFalse(ps.hasObservers());
        assertEquals(1, calls.get());
    }

    @Test
    public void upstreamCancelledCloseCrash() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            PublishSubject<Integer> ps = PublishSubject.create();
            TestObserver<Integer> to = ps.flatMapStream(v -> Stream.of(v + 1, v + 2).onClose(() -> {
                throw new TestException();
            })).take(1).test();
            assertTrue(ps.hasObservers());
            ps.onNext(1);
            to.assertResult(2);
            assertFalse(ps.hasObservers());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void crossMap() {
        Observable.range(1, 1000).flatMapStream(v -> IntStream.range(v * 1000, v * 1000 + 1000).boxed()).test().assertValueCount(1_000_000).assertNoErrors().assertComplete();
    }

    @Test
    public void crossMapHidden() {
        Observable.range(1, 1000).hide().flatMapStream(v -> IntStream.range(v * 1000, v * 1000 + 1000).boxed()).test().assertValueCount(1_000_000).assertNoErrors().assertComplete();
    }

    @Test
    public void onSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(f -> f.flatMapStream(v -> Stream.of(1, 2)));
    }

    @Test
    public void mapperThrows() {
        Observable.just(1).hide().concatMapStream(v -> {
            throw new TestException();
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void mapperNull() {
        Observable.just(1).hide().concatMapStream(v -> null).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void streamNull() {
        Observable.just(1).hide().concatMapStream(v -> Stream.of(1, null)).test().assertFailure(NullPointerException.class, 1);
    }

    @Test
    public void hasNextThrows() {
        Observable.just(1).hide().concatMapStream(v -> Stream.generate(() -> {
            throw new TestException();
        })).test().assertFailure(TestException.class);
    }

    @Test
    public void hasNextThrowsLater() {
        AtomicInteger counter = new AtomicInteger();
        Observable.just(1).hide().concatMapStream(v -> Stream.generate(() -> {
            if (counter.getAndIncrement() == 0) {
                return 1;
            }
            throw new TestException();
        })).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void mapperThrowsWhenUpstreamErrors() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            PublishSubject<Integer> ps = PublishSubject.create();
            AtomicInteger counter = new AtomicInteger();
            TestObserver<Integer> to = ps.hide().concatMapStream(v -> {
                if (counter.getAndIncrement() == 0) {
                    return Stream.of(1, 2);
                }
                ps.onError(new IOException());
                throw new TestException();
            }).test();
            ps.onNext(1);
            ps.onNext(2);
            to.assertFailure(IOException.class, 1, 2);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void cancelAfterIteratorNext() throws Exception {
        TestObserver<Integer> to = new TestObserver<>();
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                to.dispose();
                return 1;
            }
        });
        Observable.just(1).hide().concatMapStream(v -> stream).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void cancelAfterIteratorHasNext() throws Exception {
        TestObserver<Integer> to = new TestObserver<>();
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            @Override
            public boolean hasNext() {
                to.dispose();
                return true;
            }

            @Override
            public Integer next() {
                return 1;
            }
        });
        Observable.just(1).hide().concatMapStream(v -> stream).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void asyncUpstreamFused() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestObserver<Integer> to = us.flatMapStream(v -> Stream.of(1, 2)).test();
        assertTrue(us.hasObservers());
        us.onNext(1);
        to.assertValuesOnly(1, 2);
        us.onComplete();
        to.assertResult(1, 2);
    }

    @Test
    public void asyncUpstreamFusionBoundary() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestObserver<Integer> to = us.map(v -> v + 1).flatMapStream(v -> Stream.of(1, 2)).test();
        assertTrue(us.hasObservers());
        us.onNext(1);
        to.assertValuesOnly(1, 2);
        us.onComplete();
        to.assertResult(1, 2);
    }

    @Test
    public void fusedPollCrash() {
        UnicastSubject<Integer> us = UnicastSubject.create();
        TestObserver<Integer> to = us.map(v -> {
            throw new TestException();
        }).compose(TestHelper.observableStripBoundary()).flatMapStream(v -> Stream.of(1, 2)).test();
        assertTrue(us.hasObservers());
        us.onNext(1);
        assertFalse(us.hasObservers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().flatMapStream(v -> Stream.of(1)));
    }

    @Test
    public void eventsIgnoredAfterCrash() {
        AtomicInteger calls = new AtomicInteger();
        new Observable<Integer>() {

            @Override
            protected void subscribeActual(@NonNull Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext(1);
                observer.onNext(2);
                observer.onComplete();
            }
        }.flatMapStream(v -> {
            calls.getAndIncrement();
            throw new TestException();
        }).take(1).test().assertFailure(TestException.class);
        assertEquals(1, calls.get());
    }

    @Test
    public void eventsIgnoredAfterDispose() {
        AtomicInteger calls = new AtomicInteger();
        new Observable<Integer>() {

            @Override
            protected void subscribeActual(@NonNull Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onNext(1);
                observer.onNext(2);
                observer.onComplete();
            }
        }.flatMapStream(v -> {
            calls.getAndIncrement();
            return Stream.of(1);
        }).take(1).test().assertResult(1);
        assertEquals(1, calls.get());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableFlatMapStreamTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyHidden() throws java.lang.Throwable {
            this.payloads.emptyHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.payloads.just.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justHidden() throws java.lang.Throwable {
            this.payloads.justHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_supplierFusedError() throws java.lang.Throwable {
            this.payloads.supplierFusedError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorHidden() throws java.lang.Throwable {
            this.payloads.errorHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_range() throws java.lang.Throwable {
            this.payloads.range.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeHidden() throws java.lang.Throwable {
            this.payloads.rangeHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeToEmpty() throws java.lang.Throwable {
            this.payloads.rangeToEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeTake() throws java.lang.Throwable {
            this.payloads.rangeTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rangeTakeHidden() throws java.lang.Throwable {
            this.payloads.rangeTakeHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamCancelled() throws java.lang.Throwable {
            this.payloads.upstreamCancelled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_upstreamCancelledCloseCrash() throws java.lang.Throwable {
            this.payloads.upstreamCancelledCloseCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crossMap() throws java.lang.Throwable {
            this.payloads.crossMap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_crossMapHidden() throws java.lang.Throwable {
            this.payloads.crossMapHidden.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribe() throws java.lang.Throwable {
            this.payloads.onSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrows() throws java.lang.Throwable {
            this.payloads.mapperThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperNull() throws java.lang.Throwable {
            this.payloads.mapperNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_streamNull() throws java.lang.Throwable {
            this.payloads.streamNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrows() throws java.lang.Throwable {
            this.payloads.hasNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrowsLater() throws java.lang.Throwable {
            this.payloads.hasNextThrowsLater.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrowsWhenUpstreamErrors() throws java.lang.Throwable {
            this.payloads.mapperThrowsWhenUpstreamErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterIteratorNext() throws java.lang.Throwable {
            this.payloads.cancelAfterIteratorNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterIteratorHasNext() throws java.lang.Throwable {
            this.payloads.cancelAfterIteratorHasNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncUpstreamFused() throws java.lang.Throwable {
            this.payloads.asyncUpstreamFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncUpstreamFusionBoundary() throws java.lang.Throwable {
            this.payloads.asyncUpstreamFusionBoundary.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPollCrash() throws java.lang.Throwable {
            this.payloads.fusedPollCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eventsIgnoredAfterCrash() throws java.lang.Throwable {
            this.payloads.eventsIgnoredAfterCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_eventsIgnoredAfterDispose() throws java.lang.Throwable {
            this.payloads.eventsIgnoredAfterDispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapStreamTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapStreamTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapStreamTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapStreamTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableFlatMapStreamTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFlatMapStreamTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableFlatMapStreamTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableFlatMapStreamTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement emptyHidden;

            public org.junit.runners.model.Statement just;

            public org.junit.runners.model.Statement justHidden;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement supplierFusedError;

            public org.junit.runners.model.Statement errorHidden;

            public org.junit.runners.model.Statement range;

            public org.junit.runners.model.Statement rangeHidden;

            public org.junit.runners.model.Statement rangeToEmpty;

            public org.junit.runners.model.Statement rangeTake;

            public org.junit.runners.model.Statement rangeTakeHidden;

            public org.junit.runners.model.Statement upstreamCancelled;

            public org.junit.runners.model.Statement upstreamCancelledCloseCrash;

            public org.junit.runners.model.Statement crossMap;

            public org.junit.runners.model.Statement crossMapHidden;

            public org.junit.runners.model.Statement onSubscribe;

            public org.junit.runners.model.Statement mapperThrows;

            public org.junit.runners.model.Statement mapperNull;

            public org.junit.runners.model.Statement streamNull;

            public org.junit.runners.model.Statement hasNextThrows;

            public org.junit.runners.model.Statement hasNextThrowsLater;

            public org.junit.runners.model.Statement mapperThrowsWhenUpstreamErrors;

            public org.junit.runners.model.Statement cancelAfterIteratorNext;

            public org.junit.runners.model.Statement cancelAfterIteratorHasNext;

            public org.junit.runners.model.Statement asyncUpstreamFused;

            public org.junit.runners.model.Statement asyncUpstreamFusionBoundary;

            public org.junit.runners.model.Statement fusedPollCrash;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement eventsIgnoredAfterCrash;

            public org.junit.runners.model.Statement eventsIgnoredAfterDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.empty = _ClassStatement.forPayload(ObservableFlatMapStreamTest::empty, "empty", this);
            this.payloads.emptyHidden = _ClassStatement.forPayload(ObservableFlatMapStreamTest::emptyHidden, "emptyHidden", this);
            this.payloads.just = _ClassStatement.forPayload(ObservableFlatMapStreamTest::just, "just", this);
            this.payloads.justHidden = _ClassStatement.forPayload(ObservableFlatMapStreamTest::justHidden, "justHidden", this);
            this.payloads.error = _ClassStatement.forPayload(ObservableFlatMapStreamTest::error, "error", this);
            this.payloads.supplierFusedError = _ClassStatement.forPayload(ObservableFlatMapStreamTest::supplierFusedError, "supplierFusedError", this);
            this.payloads.errorHidden = _ClassStatement.forPayload(ObservableFlatMapStreamTest::errorHidden, "errorHidden", this);
            this.payloads.range = _ClassStatement.forPayload(ObservableFlatMapStreamTest::range, "range", this);
            this.payloads.rangeHidden = _ClassStatement.forPayload(ObservableFlatMapStreamTest::rangeHidden, "rangeHidden", this);
            this.payloads.rangeToEmpty = _ClassStatement.forPayload(ObservableFlatMapStreamTest::rangeToEmpty, "rangeToEmpty", this);
            this.payloads.rangeTake = _ClassStatement.forPayload(ObservableFlatMapStreamTest::rangeTake, "rangeTake", this);
            this.payloads.rangeTakeHidden = _ClassStatement.forPayload(ObservableFlatMapStreamTest::rangeTakeHidden, "rangeTakeHidden", this);
            this.payloads.upstreamCancelled = _ClassStatement.forPayload(ObservableFlatMapStreamTest::upstreamCancelled, "upstreamCancelled", this);
            this.payloads.upstreamCancelledCloseCrash = _ClassStatement.forPayload(ObservableFlatMapStreamTest::upstreamCancelledCloseCrash, "upstreamCancelledCloseCrash", this);
            this.payloads.crossMap = _ClassStatement.forPayload(ObservableFlatMapStreamTest::crossMap, "crossMap", this);
            this.payloads.crossMapHidden = _ClassStatement.forPayload(ObservableFlatMapStreamTest::crossMapHidden, "crossMapHidden", this);
            this.payloads.onSubscribe = _ClassStatement.forPayload(ObservableFlatMapStreamTest::onSubscribe, "onSubscribe", this);
            this.payloads.mapperThrows = _ClassStatement.forPayload(ObservableFlatMapStreamTest::mapperThrows, "mapperThrows", this);
            this.payloads.mapperNull = _ClassStatement.forPayload(ObservableFlatMapStreamTest::mapperNull, "mapperNull", this);
            this.payloads.streamNull = _ClassStatement.forPayload(ObservableFlatMapStreamTest::streamNull, "streamNull", this);
            this.payloads.hasNextThrows = _ClassStatement.forPayload(ObservableFlatMapStreamTest::hasNextThrows, "hasNextThrows", this);
            this.payloads.hasNextThrowsLater = _ClassStatement.forPayload(ObservableFlatMapStreamTest::hasNextThrowsLater, "hasNextThrowsLater", this);
            this.payloads.mapperThrowsWhenUpstreamErrors = _ClassStatement.forPayload(ObservableFlatMapStreamTest::mapperThrowsWhenUpstreamErrors, "mapperThrowsWhenUpstreamErrors", this);
            this.payloads.cancelAfterIteratorNext = _ClassStatement.forPayload(ObservableFlatMapStreamTest::cancelAfterIteratorNext, "cancelAfterIteratorNext", this);
            this.payloads.cancelAfterIteratorHasNext = _ClassStatement.forPayload(ObservableFlatMapStreamTest::cancelAfterIteratorHasNext, "cancelAfterIteratorHasNext", this);
            this.payloads.asyncUpstreamFused = _ClassStatement.forPayload(ObservableFlatMapStreamTest::asyncUpstreamFused, "asyncUpstreamFused", this);
            this.payloads.asyncUpstreamFusionBoundary = _ClassStatement.forPayload(ObservableFlatMapStreamTest::asyncUpstreamFusionBoundary, "asyncUpstreamFusionBoundary", this);
            this.payloads.fusedPollCrash = _ClassStatement.forPayload(ObservableFlatMapStreamTest::fusedPollCrash, "fusedPollCrash", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableFlatMapStreamTest::dispose, "dispose", this);
            this.payloads.eventsIgnoredAfterCrash = _ClassStatement.forPayload(ObservableFlatMapStreamTest::eventsIgnoredAfterCrash, "eventsIgnoredAfterCrash", this);
            this.payloads.eventsIgnoredAfterDispose = _ClassStatement.forPayload(ObservableFlatMapStreamTest::eventsIgnoredAfterDispose, "eventsIgnoredAfterDispose", this);
        }
    }
}
