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
import java.util.Iterator;
import java.util.concurrent.atomic.*;
import java.util.stream.*;
import org.junit.Test;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueDisposable;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.SimpleQueue;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableFromStreamTest extends RxJavaTest {

    @Test
    public void empty() {
        Observable.fromStream(Stream.<Integer>of()).test().assertResult();
    }

    @Test
    public void just() {
        Observable.fromStream(Stream.<Integer>of(1)).test().assertResult(1);
    }

    @Test
    public void many() {
        Observable.fromStream(Stream.<Integer>of(1, 2, 3, 4, 5)).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void noReuse() {
        Observable<Integer> source = Observable.fromStream(Stream.<Integer>of(1, 2, 3, 4, 5));
        source.test().assertResult(1, 2, 3, 4, 5);
        source.test().assertFailure(IllegalStateException.class);
    }

    @Test
    public void take() {
        Observable.fromStream(IntStream.rangeClosed(1, 10).boxed()).take(5).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void emptyConditional() {
        Observable.fromStream(Stream.<Integer>of()).filter(v -> true).test().assertResult();
    }

    @Test
    public void justConditional() {
        Observable.fromStream(Stream.<Integer>of(1)).filter(v -> true).test().assertResult(1);
    }

    @Test
    public void manyConditional() {
        Observable.fromStream(Stream.<Integer>of(1, 2, 3, 4, 5)).filter(v -> true).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void manyConditionalSkip() {
        Observable.fromStream(IntStream.rangeClosed(1, 10).boxed()).filter(v -> v % 2 == 0).test().assertResult(2, 4, 6, 8, 10);
    }

    @Test
    public void takeConditional() {
        Observable.fromStream(IntStream.rangeClosed(1, 10).boxed()).filter(v -> true).take(5).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void noOfferNoCrashAfterClear() throws Throwable {
        AtomicReference<SimpleQueue<?>> queue = new AtomicReference<>();
        Observable.fromStream(IntStream.rangeClosed(1, 10).boxed()).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                queue.set((SimpleQueue<?>) d);
                ((QueueDisposable<?>) d).requestFusion(QueueFuseable.ANY);
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
        SimpleQueue<?> q = queue.get();
        TestHelper.assertNoOffer(q);
        assertFalse(q.isEmpty());
        q.clear();
        assertNull(q.poll());
        assertTrue(q.isEmpty());
        q.clear();
        assertNull(q.poll());
        assertTrue(q.isEmpty());
    }

    @Test
    public void fusedPoll() throws Throwable {
        AtomicReference<SimpleQueue<?>> queue = new AtomicReference<>();
        AtomicInteger calls = new AtomicInteger();
        Observable.fromStream(Stream.of(1).onClose(() -> calls.getAndIncrement())).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                queue.set((SimpleQueue<?>) d);
                ((QueueDisposable<?>) d).requestFusion(QueueFuseable.ANY);
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
        SimpleQueue<?> q = queue.get();
        assertFalse(q.isEmpty());
        assertEquals(1, q.poll());
        assertTrue(q.isEmpty());
        assertEquals(1, calls.get());
    }

    @Test
    public void fusedPoll2() throws Throwable {
        AtomicReference<SimpleQueue<?>> queue = new AtomicReference<>();
        AtomicInteger calls = new AtomicInteger();
        Observable.fromStream(Stream.of(1, 2).onClose(() -> calls.getAndIncrement())).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                queue.set((SimpleQueue<?>) d);
                ((QueueDisposable<?>) d).requestFusion(QueueFuseable.ANY);
            }

            @Override
            public void onNext(Integer t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
        SimpleQueue<?> q = queue.get();
        assertFalse(q.isEmpty());
        assertEquals(1, q.poll());
        assertFalse(q.isEmpty());
        assertEquals(2, q.poll());
        assertTrue(q.isEmpty());
        assertEquals(1, calls.get());
    }

    @Test
    public void streamOfNull() {
        Observable.fromStream(Stream.of((Integer) null)).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void streamOfNullConditional() {
        Observable.fromStream(Stream.of((Integer) null)).filter(v -> true).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void syncFusionSupport() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.ANY);
        Observable.fromStream(IntStream.rangeClosed(1, 10).boxed()).subscribeWith(to).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void asyncFusionNotSupported() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        to.setInitialFusionMode(QueueFuseable.ASYNC);
        Observable.fromStream(IntStream.rangeClosed(1, 10).boxed()).subscribeWith(to).assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void runToEndCloseCrash() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5).onClose(() -> {
                throw new TestException();
            });
            Observable.fromStream(stream).test().assertResult(1, 2, 3, 4, 5);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void takeCloseCrash() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5).onClose(() -> {
                throw new TestException();
            });
            Observable.fromStream(stream).take(3).test().assertResult(1, 2, 3);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void hasNextCrash() {
        AtomicInteger v = new AtomicInteger();
        Observable.fromStream(Stream.<Integer>generate(() -> {
            int value = v.getAndIncrement();
            if (value == 1) {
                throw new TestException();
            }
            return value;
        })).test().assertFailure(TestException.class, 0);
    }

    @Test
    public void hasNextCrashConditional() {
        AtomicInteger counter = new AtomicInteger();
        Observable.fromStream(Stream.<Integer>generate(() -> {
            int value = counter.getAndIncrement();
            if (value == 1) {
                throw new TestException();
            }
            return value;
        })).filter(v -> true).test().assertFailure(TestException.class, 0);
    }

    @Test
    public void closeCalledOnEmpty() {
        AtomicInteger calls = new AtomicInteger();
        Observable.fromStream(Stream.of().onClose(() -> calls.getAndIncrement())).test().assertResult();
        assertEquals(1, calls.get());
    }

    @Test
    public void closeCalledAfterItems() {
        AtomicInteger calls = new AtomicInteger();
        Observable.fromStream(Stream.of(1, 2, 3, 4, 5).onClose(() -> calls.getAndIncrement())).test().assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls.get());
    }

    @Test
    public void closeCalledOnCancel() {
        AtomicInteger calls = new AtomicInteger();
        Observable.fromStream(Stream.of(1, 2, 3, 4, 5).onClose(() -> calls.getAndIncrement())).take(3).test().assertResult(1, 2, 3);
        assertEquals(1, calls.get());
    }

    @Test
    public void closeCalledOnItemCrash() {
        AtomicInteger calls = new AtomicInteger();
        AtomicInteger counter = new AtomicInteger();
        Observable.fromStream(Stream.<Integer>generate(() -> {
            int value = counter.getAndIncrement();
            if (value == 1) {
                throw new TestException();
            }
            return value;
        }).onClose(() -> calls.getAndIncrement())).test().assertFailure(TestException.class, 0);
        assertEquals(1, calls.get());
    }

    @Test
    public void closeCalledAfterItemsConditional() {
        AtomicInteger calls = new AtomicInteger();
        Observable.fromStream(Stream.of(1, 2, 3, 4, 5).onClose(() -> calls.getAndIncrement())).filter(v -> true).test().assertResult(1, 2, 3, 4, 5);
        assertEquals(1, calls.get());
    }

    @Test
    public void closeCalledOnCancelConditional() {
        AtomicInteger calls = new AtomicInteger();
        Observable.fromStream(Stream.of(1, 2, 3, 4, 5).onClose(() -> calls.getAndIncrement())).filter(v -> true).take(3).test().assertResult(1, 2, 3);
        assertEquals(1, calls.get());
    }

    @Test
    public void closeCalledOnItemCrashConditional() {
        AtomicInteger calls = new AtomicInteger();
        AtomicInteger counter = new AtomicInteger();
        Observable.fromStream(Stream.<Integer>generate(() -> {
            int value = counter.getAndIncrement();
            if (value == 1) {
                throw new TestException();
            }
            return value;
        }).onClose(() -> calls.getAndIncrement())).filter(v -> true).test().assertFailure(TestException.class, 0);
        assertEquals(1, calls.get());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.fromStream(Stream.of(1)));
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
        Observable.fromStream(stream).subscribe(to);
        to.assertEmpty();
    }

    @Test
    public void cancelAfterIteratorHasNext() throws Exception {
        TestObserver<Integer> to = new TestObserver<>();
        @SuppressWarnings("unchecked")
        Stream<Integer> stream = mock(Stream.class);
        when(stream.iterator()).thenReturn(new Iterator<Integer>() {

            int calls;

            @Override
            public boolean hasNext() {
                if (++calls == 1) {
                    to.dispose();
                }
                return true;
            }

            @Override
            public Integer next() {
                return 1;
            }
        });
        Observable.fromStream(stream).subscribe(to);
        to.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableFromStreamTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.payloads.just.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_many() throws java.lang.Throwable {
            this.payloads.many.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noReuse() throws java.lang.Throwable {
            this.payloads.noReuse.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyConditional() throws java.lang.Throwable {
            this.payloads.emptyConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justConditional() throws java.lang.Throwable {
            this.payloads.justConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyConditional() throws java.lang.Throwable {
            this.payloads.manyConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_manyConditionalSkip() throws java.lang.Throwable {
            this.payloads.manyConditionalSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeConditional() throws java.lang.Throwable {
            this.payloads.takeConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noOfferNoCrashAfterClear() throws java.lang.Throwable {
            this.payloads.noOfferNoCrashAfterClear.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPoll() throws java.lang.Throwable {
            this.payloads.fusedPoll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPoll2() throws java.lang.Throwable {
            this.payloads.fusedPoll2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_streamOfNull() throws java.lang.Throwable {
            this.payloads.streamOfNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_streamOfNullConditional() throws java.lang.Throwable {
            this.payloads.streamOfNullConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_syncFusionSupport() throws java.lang.Throwable {
            this.payloads.syncFusionSupport.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncFusionNotSupported() throws java.lang.Throwable {
            this.payloads.asyncFusionNotSupported.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_runToEndCloseCrash() throws java.lang.Throwable {
            this.payloads.runToEndCloseCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeCloseCrash() throws java.lang.Throwable {
            this.payloads.takeCloseCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCrash() throws java.lang.Throwable {
            this.payloads.hasNextCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCrashConditional() throws java.lang.Throwable {
            this.payloads.hasNextCrashConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeCalledOnEmpty() throws java.lang.Throwable {
            this.payloads.closeCalledOnEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeCalledAfterItems() throws java.lang.Throwable {
            this.payloads.closeCalledAfterItems.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeCalledOnCancel() throws java.lang.Throwable {
            this.payloads.closeCalledOnCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeCalledOnItemCrash() throws java.lang.Throwable {
            this.payloads.closeCalledOnItemCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeCalledAfterItemsConditional() throws java.lang.Throwable {
            this.payloads.closeCalledAfterItemsConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeCalledOnCancelConditional() throws java.lang.Throwable {
            this.payloads.closeCalledOnCancelConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_closeCalledOnItemCrashConditional() throws java.lang.Throwable {
            this.payloads.closeCalledOnItemCrashConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterIteratorNext() throws java.lang.Throwable {
            this.payloads.cancelAfterIteratorNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterIteratorHasNext() throws java.lang.Throwable {
            this.payloads.cancelAfterIteratorHasNext.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromStreamTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromStreamTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromStreamTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromStreamTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableFromStreamTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromStreamTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableFromStreamTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableFromStreamTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement just;

            public org.junit.runners.model.Statement many;

            public org.junit.runners.model.Statement noReuse;

            public org.junit.runners.model.Statement take;

            public org.junit.runners.model.Statement emptyConditional;

            public org.junit.runners.model.Statement justConditional;

            public org.junit.runners.model.Statement manyConditional;

            public org.junit.runners.model.Statement manyConditionalSkip;

            public org.junit.runners.model.Statement takeConditional;

            public org.junit.runners.model.Statement noOfferNoCrashAfterClear;

            public org.junit.runners.model.Statement fusedPoll;

            public org.junit.runners.model.Statement fusedPoll2;

            public org.junit.runners.model.Statement streamOfNull;

            public org.junit.runners.model.Statement streamOfNullConditional;

            public org.junit.runners.model.Statement syncFusionSupport;

            public org.junit.runners.model.Statement asyncFusionNotSupported;

            public org.junit.runners.model.Statement runToEndCloseCrash;

            public org.junit.runners.model.Statement takeCloseCrash;

            public org.junit.runners.model.Statement hasNextCrash;

            public org.junit.runners.model.Statement hasNextCrashConditional;

            public org.junit.runners.model.Statement closeCalledOnEmpty;

            public org.junit.runners.model.Statement closeCalledAfterItems;

            public org.junit.runners.model.Statement closeCalledOnCancel;

            public org.junit.runners.model.Statement closeCalledOnItemCrash;

            public org.junit.runners.model.Statement closeCalledAfterItemsConditional;

            public org.junit.runners.model.Statement closeCalledOnCancelConditional;

            public org.junit.runners.model.Statement closeCalledOnItemCrashConditional;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement cancelAfterIteratorNext;

            public org.junit.runners.model.Statement cancelAfterIteratorHasNext;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.empty = _ClassStatement.forPayload(ObservableFromStreamTest::empty, "empty", this);
            this.payloads.just = _ClassStatement.forPayload(ObservableFromStreamTest::just, "just", this);
            this.payloads.many = _ClassStatement.forPayload(ObservableFromStreamTest::many, "many", this);
            this.payloads.noReuse = _ClassStatement.forPayload(ObservableFromStreamTest::noReuse, "noReuse", this);
            this.payloads.take = _ClassStatement.forPayload(ObservableFromStreamTest::take, "take", this);
            this.payloads.emptyConditional = _ClassStatement.forPayload(ObservableFromStreamTest::emptyConditional, "emptyConditional", this);
            this.payloads.justConditional = _ClassStatement.forPayload(ObservableFromStreamTest::justConditional, "justConditional", this);
            this.payloads.manyConditional = _ClassStatement.forPayload(ObservableFromStreamTest::manyConditional, "manyConditional", this);
            this.payloads.manyConditionalSkip = _ClassStatement.forPayload(ObservableFromStreamTest::manyConditionalSkip, "manyConditionalSkip", this);
            this.payloads.takeConditional = _ClassStatement.forPayload(ObservableFromStreamTest::takeConditional, "takeConditional", this);
            this.payloads.noOfferNoCrashAfterClear = _ClassStatement.forPayload(ObservableFromStreamTest::noOfferNoCrashAfterClear, "noOfferNoCrashAfterClear", this);
            this.payloads.fusedPoll = _ClassStatement.forPayload(ObservableFromStreamTest::fusedPoll, "fusedPoll", this);
            this.payloads.fusedPoll2 = _ClassStatement.forPayload(ObservableFromStreamTest::fusedPoll2, "fusedPoll2", this);
            this.payloads.streamOfNull = _ClassStatement.forPayload(ObservableFromStreamTest::streamOfNull, "streamOfNull", this);
            this.payloads.streamOfNullConditional = _ClassStatement.forPayload(ObservableFromStreamTest::streamOfNullConditional, "streamOfNullConditional", this);
            this.payloads.syncFusionSupport = _ClassStatement.forPayload(ObservableFromStreamTest::syncFusionSupport, "syncFusionSupport", this);
            this.payloads.asyncFusionNotSupported = _ClassStatement.forPayload(ObservableFromStreamTest::asyncFusionNotSupported, "asyncFusionNotSupported", this);
            this.payloads.runToEndCloseCrash = _ClassStatement.forPayload(ObservableFromStreamTest::runToEndCloseCrash, "runToEndCloseCrash", this);
            this.payloads.takeCloseCrash = _ClassStatement.forPayload(ObservableFromStreamTest::takeCloseCrash, "takeCloseCrash", this);
            this.payloads.hasNextCrash = _ClassStatement.forPayload(ObservableFromStreamTest::hasNextCrash, "hasNextCrash", this);
            this.payloads.hasNextCrashConditional = _ClassStatement.forPayload(ObservableFromStreamTest::hasNextCrashConditional, "hasNextCrashConditional", this);
            this.payloads.closeCalledOnEmpty = _ClassStatement.forPayload(ObservableFromStreamTest::closeCalledOnEmpty, "closeCalledOnEmpty", this);
            this.payloads.closeCalledAfterItems = _ClassStatement.forPayload(ObservableFromStreamTest::closeCalledAfterItems, "closeCalledAfterItems", this);
            this.payloads.closeCalledOnCancel = _ClassStatement.forPayload(ObservableFromStreamTest::closeCalledOnCancel, "closeCalledOnCancel", this);
            this.payloads.closeCalledOnItemCrash = _ClassStatement.forPayload(ObservableFromStreamTest::closeCalledOnItemCrash, "closeCalledOnItemCrash", this);
            this.payloads.closeCalledAfterItemsConditional = _ClassStatement.forPayload(ObservableFromStreamTest::closeCalledAfterItemsConditional, "closeCalledAfterItemsConditional", this);
            this.payloads.closeCalledOnCancelConditional = _ClassStatement.forPayload(ObservableFromStreamTest::closeCalledOnCancelConditional, "closeCalledOnCancelConditional", this);
            this.payloads.closeCalledOnItemCrashConditional = _ClassStatement.forPayload(ObservableFromStreamTest::closeCalledOnItemCrashConditional, "closeCalledOnItemCrashConditional", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableFromStreamTest::dispose, "dispose", this);
            this.payloads.cancelAfterIteratorNext = _ClassStatement.forPayload(ObservableFromStreamTest::cancelAfterIteratorNext, "cancelAfterIteratorNext", this);
            this.payloads.cancelAfterIteratorHasNext = _ClassStatement.forPayload(ObservableFromStreamTest::cancelAfterIteratorHasNext, "cancelAfterIteratorHasNext", this);
        }
    }
}
