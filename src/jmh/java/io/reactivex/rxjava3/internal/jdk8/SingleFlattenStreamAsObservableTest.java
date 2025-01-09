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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueDisposable;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.subjects.SingleSubject;
import io.reactivex.rxjava3.testsupport.*;

public class SingleFlattenStreamAsObservableTest extends RxJavaTest {

    @Test
    public void successJust() {
        Single.just(1).flattenStreamAsObservable(Stream::of).test().assertResult(1);
    }

    @Test
    public void successEmpty() {
        Single.just(1).flattenStreamAsObservable(v -> Stream.of()).test().assertResult();
    }

    @Test
    public void successMany() {
        Single.just(1).flattenStreamAsObservable(v -> Stream.of(2, 3, 4, 5, 6)).test().assertResult(2, 3, 4, 5, 6);
    }

    @Test
    public void successManyTake() {
        Single.just(1).flattenStreamAsObservable(v -> Stream.of(2, 3, 4, 5, 6)).take(3).test().assertResult(2, 3, 4);
    }

    @Test
    public void error() throws Throwable {
        @SuppressWarnings("unchecked")
        Function<? super Integer, Stream<? extends Integer>> f = mock(Function.class);
        Single.<Integer>error(new TestException()).flattenStreamAsObservable(f).test().assertFailure(TestException.class);
        verify(f, never()).apply(any());
    }

    @Test
    public void mapperCrash() {
        Single.just(1).flattenStreamAsObservable(v -> {
            throw new TestException();
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Single.never().flattenStreamAsObservable(Stream::of));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingleToObservable(m -> m.flattenStreamAsObservable(Stream::of));
    }

    @Test
    public void fusedEmpty() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.ANY);
        Single.just(1).flattenStreamAsObservable(v -> Stream.<Integer>of()).subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void fusedJust() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.ANY);
        Single.just(1).flattenStreamAsObservable(v -> Stream.<Integer>of(v)).subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1);
    }

    @Test
    public void fusedMany() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.ANY);
        Single.just(1).flattenStreamAsObservable(v -> Stream.<Integer>of(v, v + 1, v + 2)).subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3);
    }

    @Test
    public void fusedManyRejected() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.SYNC);
        Single.just(1).flattenStreamAsObservable(v -> Stream.<Integer>of(v, v + 1, v + 2)).subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3);
    }

    @Test
    public void fusedStreamAvailableLater() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.ANY);
        SingleSubject<Integer> ss = SingleSubject.create();
        ss.flattenStreamAsObservable(v -> Stream.<Integer>of(v, v + 1, v + 2)).subscribe(to);
        to.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertEmpty();
        ss.onSuccess(1);
        to.assertResult(1, 2, 3);
    }

    @Test
    public void fused() throws Throwable {
        AtomicReference<QueueDisposable<Integer>> qdr = new AtomicReference<>();
        SingleSubject<Integer> ss = SingleSubject.create();
        ss.flattenStreamAsObservable(Stream::of).subscribe(new Observer<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onSubscribe(Disposable d) {
                qdr.set((QueueDisposable<Integer>) d);
            }
        });
        QueueDisposable<Integer> qd = qdr.get();
        assertEquals(QueueFuseable.ASYNC, qd.requestFusion(QueueFuseable.ASYNC));
        assertTrue(qd.isEmpty());
        assertNull(qd.poll());
        ss.onSuccess(1);
        assertFalse(qd.isEmpty());
        assertEquals(1, qd.poll().intValue());
        assertTrue(qd.isEmpty());
        assertNull(qd.poll());
        qd.dispose();
        assertTrue(qd.isEmpty());
        assertNull(qd.poll());
    }

    @Test
    public void fused2() throws Throwable {
        AtomicReference<QueueDisposable<Integer>> qdr = new AtomicReference<>();
        SingleSubject<Integer> ss = SingleSubject.create();
        ss.flattenStreamAsObservable(v -> Stream.of(v, v + 1)).subscribe(new Observer<Integer>() {

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onSubscribe(Disposable d) {
                qdr.set((QueueDisposable<Integer>) d);
            }
        });
        QueueDisposable<Integer> qd = qdr.get();
        assertEquals(QueueFuseable.ASYNC, qd.requestFusion(QueueFuseable.ASYNC));
        assertTrue(qd.isEmpty());
        assertNull(qd.poll());
        ss.onSuccess(1);
        assertFalse(qd.isEmpty());
        assertEquals(1, qd.poll().intValue());
        assertFalse(qd.isEmpty());
        assertEquals(2, qd.poll().intValue());
        assertTrue(qd.isEmpty());
        assertNull(qd.poll());
        qd.dispose();
        assertTrue(qd.isEmpty());
        assertNull(qd.poll());
    }

    @Test
    public void streamCloseCrash() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Single.just(1).flattenStreamAsObservable(v -> Stream.of(v).onClose(() -> {
                throw new TestException();
            })).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void hasNextThrowsInDrain() {
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            int count;

            @Override
            public boolean hasNext() {
                if (count++ > 0) {
                    throw new TestException();
                }
                return true;
            }

            @Override
            public Integer next() {
                return 1;
            }
        });
        Single.just(1).flattenStreamAsObservable(v -> stream).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void nextThrowsInDrain() {
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                throw new TestException();
            }
        });
        Single.just(1).flattenStreamAsObservable(v -> stream).test().assertFailure(TestException.class);
    }

    @Test
    public void cancelAfterHasNextInDrain() {
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        TestObserver<Integer> to = new TestObserver<>();
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            int count;

            @Override
            public boolean hasNext() {
                if (count++ > 0) {
                    to.dispose();
                }
                return true;
            }

            @Override
            public Integer next() {
                return 1;
            }
        });
        Single.just(1).flattenStreamAsObservable(v -> stream).subscribeWith(to).assertValuesOnly(1);
    }

    @Test
    public void cancelAfterNextInDrain() {
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        TestObserver<Integer> to = new TestObserver<>();
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
        Single.just(1).flattenStreamAsObservable(v -> stream).subscribeWith(to).assertEmpty();
    }

    @Test
    public void cancelSuccessRace() {
        for (int i = 0; i < TestHelper.RACE_LONG_LOOPS; i++) {
            SingleSubject<Integer> ss = SingleSubject.create();
            TestObserver<Integer> to = new TestObserver<>();
            ss.flattenStreamAsObservable(Stream::of).subscribe(to);
            Runnable r1 = () -> ss.onSuccess(1);
            Runnable r2 = () -> to.dispose();
            TestHelper.race(r1, r2);
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public SingleFlattenStreamAsObservableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successJust() throws java.lang.Throwable {
            this.payloads.successJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successEmpty() throws java.lang.Throwable {
            this.payloads.successEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successMany() throws java.lang.Throwable {
            this.payloads.successMany.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_successManyTake() throws java.lang.Throwable {
            this.payloads.successManyTake.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperCrash() throws java.lang.Throwable {
            this.payloads.mapperCrash.evaluate();
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
        public void benchmark_fusedEmpty() throws java.lang.Throwable {
            this.payloads.fusedEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedJust() throws java.lang.Throwable {
            this.payloads.fusedJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedMany() throws java.lang.Throwable {
            this.payloads.fusedMany.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedManyRejected() throws java.lang.Throwable {
            this.payloads.fusedManyRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedStreamAvailableLater() throws java.lang.Throwable {
            this.payloads.fusedStreamAvailableLater.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.payloads.fused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused2() throws java.lang.Throwable {
            this.payloads.fused2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_streamCloseCrash() throws java.lang.Throwable {
            this.payloads.streamCloseCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextThrowsInDrain() throws java.lang.Throwable {
            this.payloads.hasNextThrowsInDrain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nextThrowsInDrain() throws java.lang.Throwable {
            this.payloads.nextThrowsInDrain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterHasNextInDrain() throws java.lang.Throwable {
            this.payloads.cancelAfterHasNextInDrain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterNextInDrain() throws java.lang.Throwable {
            this.payloads.cancelAfterNextInDrain.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelSuccessRace() throws java.lang.Throwable {
            this.payloads.cancelSuccessRace.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlattenStreamAsObservableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlattenStreamAsObservableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlattenStreamAsObservableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlattenStreamAsObservableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleFlattenStreamAsObservableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleFlattenStreamAsObservableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleFlattenStreamAsObservableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleFlattenStreamAsObservableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement successJust;

            public org.junit.runners.model.Statement successEmpty;

            public org.junit.runners.model.Statement successMany;

            public org.junit.runners.model.Statement successManyTake;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement mapperCrash;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement fusedEmpty;

            public org.junit.runners.model.Statement fusedJust;

            public org.junit.runners.model.Statement fusedMany;

            public org.junit.runners.model.Statement fusedManyRejected;

            public org.junit.runners.model.Statement fusedStreamAvailableLater;

            public org.junit.runners.model.Statement fused;

            public org.junit.runners.model.Statement fused2;

            public org.junit.runners.model.Statement streamCloseCrash;

            public org.junit.runners.model.Statement hasNextThrowsInDrain;

            public org.junit.runners.model.Statement nextThrowsInDrain;

            public org.junit.runners.model.Statement cancelAfterHasNextInDrain;

            public org.junit.runners.model.Statement cancelAfterNextInDrain;

            public org.junit.runners.model.Statement cancelSuccessRace;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.successJust = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::successJust, "successJust", this);
            this.payloads.successEmpty = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::successEmpty, "successEmpty", this);
            this.payloads.successMany = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::successMany, "successMany", this);
            this.payloads.successManyTake = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::successManyTake, "successManyTake", this);
            this.payloads.error = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::error, "error", this);
            this.payloads.mapperCrash = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::mapperCrash, "mapperCrash", this);
            this.payloads.dispose = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.fusedEmpty = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::fusedEmpty, "fusedEmpty", this);
            this.payloads.fusedJust = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::fusedJust, "fusedJust", this);
            this.payloads.fusedMany = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::fusedMany, "fusedMany", this);
            this.payloads.fusedManyRejected = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::fusedManyRejected, "fusedManyRejected", this);
            this.payloads.fusedStreamAvailableLater = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::fusedStreamAvailableLater, "fusedStreamAvailableLater", this);
            this.payloads.fused = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::fused, "fused", this);
            this.payloads.fused2 = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::fused2, "fused2", this);
            this.payloads.streamCloseCrash = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::streamCloseCrash, "streamCloseCrash", this);
            this.payloads.hasNextThrowsInDrain = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::hasNextThrowsInDrain, "hasNextThrowsInDrain", this);
            this.payloads.nextThrowsInDrain = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::nextThrowsInDrain, "nextThrowsInDrain", this);
            this.payloads.cancelAfterHasNextInDrain = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::cancelAfterHasNextInDrain, "cancelAfterHasNextInDrain", this);
            this.payloads.cancelAfterNextInDrain = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::cancelAfterNextInDrain, "cancelAfterNextInDrain", this);
            this.payloads.cancelSuccessRace = _ClassStatement.forPayload(SingleFlattenStreamAsObservableTest::cancelSuccessRace, "cancelSuccessRace", this);
        }
    }
}
