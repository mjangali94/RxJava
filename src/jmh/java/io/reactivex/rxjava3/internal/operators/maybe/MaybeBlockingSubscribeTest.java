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
package io.reactivex.rxjava3.internal.operators.maybe;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class MaybeBlockingSubscribeTest {

    @Test
    public void noArgSuccess() {
        Maybe.just(1).blockingSubscribe();
    }

    @Test
    public void noArgSuccessAsync() {
        Maybe.just(1).delay(100, TimeUnit.MILLISECONDS).blockingSubscribe();
    }

    @Test
    public void noArgEmpty() {
        Maybe.empty().blockingSubscribe();
    }

    @Test
    public void noArgEmptyAsync() {
        Maybe.empty().delay(100, TimeUnit.MILLISECONDS).blockingSubscribe();
    }

    @Test
    public void noArgError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Maybe.error(new TestException()).blockingSubscribe();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void noArgErrorAsync() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Maybe.error(new TestException()).delay(100, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        });
    }

    @Test
    public void oneArgSuccess() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        Maybe.just(1).blockingSubscribe(success);
        verify(success).accept(1);
    }

    @Test
    public void oneArgSuccessAsync() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        Maybe.just(1).delay(50, TimeUnit.MILLISECONDS).blockingSubscribe(success);
        verify(success).accept(1);
    }

    @Test
    public void oneArgEmpty() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        Maybe.<Integer>empty().blockingSubscribe(success);
        verify(success, never()).accept(any());
    }

    @Test
    public void oneArgEmptyAsync() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        Maybe.<Integer>empty().delay(50, TimeUnit.MILLISECONDS).blockingSubscribe(success);
        verify(success, never()).accept(any());
    }

    @Test
    public void oneArgSuccessFails() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            doThrow(new TestException()).when(success).accept(any());
            Maybe.just(1).blockingSubscribe(success);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(success).accept(1);
        });
    }

    @Test
    public void oneArgError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            Maybe.<Integer>error(new TestException()).blockingSubscribe(success);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(success, never()).accept(any());
        });
    }

    @Test
    public void oneArgErrorAsync() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            Maybe.<Integer>error(new TestException()).delay(50, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe(success);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(success, never()).accept(any());
        });
    }

    @Test
    public void twoArgSuccess() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Maybe.just(1).blockingSubscribe(success, consumer);
        verify(success).accept(1);
        verify(consumer, never()).accept(any());
    }

    @Test
    public void twoArgSuccessAsync() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Maybe.just(1).delay(50, TimeUnit.MILLISECONDS).blockingSubscribe(success, consumer);
        verify(success).accept(any());
        verify(consumer, never()).accept(any());
    }

    @Test
    public void twoArgEmpty() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Maybe.<Integer>empty().blockingSubscribe(success, consumer);
        verify(success, never()).accept(any());
        verify(consumer, never()).accept(any());
    }

    @Test
    public void twoArgEmptyAsync() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Maybe.<Integer>empty().delay(50, TimeUnit.MILLISECONDS).blockingSubscribe(success, consumer);
        verify(success, never()).accept(any());
        verify(consumer, never()).accept(any());
    }

    @Test
    public void twoArgSuccessFails() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            doThrow(new TestException()).when(success).accept(any());
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            Maybe.just(1).blockingSubscribe(success, consumer);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(success).accept(any());
            verify(consumer, never()).accept(any());
        });
    }

    @Test
    public void twoArgError() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            Maybe.<Integer>error(new TestException()).blockingSubscribe(success, consumer);
            assertTrue("" + errors, errors.isEmpty());
            verify(success, never()).accept(any());
            verify(consumer).accept(any(TestException.class));
        });
    }

    @Test
    public void twoArgErrorAsync() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            Maybe.<Integer>error(new TestException()).delay(50, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe(success, consumer);
            assertTrue("" + errors, errors.isEmpty());
            verify(success, never()).accept(any());
            verify(consumer).accept(any(TestException.class));
        });
    }

    @Test
    public void twoArgErrorFails() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            doThrow(new TestException()).when(consumer).accept(any());
            Maybe.<Integer>error(new TestException()).delay(50, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe(success, consumer);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(success, never()).accept(any());
            verify(consumer).accept(any(TestException.class));
        });
    }

    @Test
    public void threeArgSuccess() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Action action = mock(Action.class);
        Maybe.just(1).blockingSubscribe(success, consumer, action);
        verify(success).accept(any());
        verify(consumer, never()).accept(any(Throwable.class));
        verify(action, never()).run();
    }

    @Test
    public void threeArgEmpty() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Action action = mock(Action.class);
        Maybe.<Integer>empty().blockingSubscribe(success, consumer, action);
        verify(success, never()).accept(any());
        verify(consumer, never()).accept(any(Throwable.class));
        verify(action).run();
    }

    @Test
    public void threeArgError() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> success = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<? super Throwable> consumer = mock(Consumer.class);
        Action action = mock(Action.class);
        Maybe.<Integer>error(new TestException()).blockingSubscribe(success, consumer, action);
        verify(success, never()).accept(any());
        verify(consumer).accept(any(TestException.class));
        verify(action, never()).run();
    }

    @Test
    public void threeArgEmptyFails() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            Action action = mock(Action.class);
            doThrow(new TestException()).when(action).run();
            Maybe.<Integer>empty().delay(50, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe(success, consumer, action);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            verify(success, never()).accept(any());
            verify(consumer, never()).accept(any());
            verify(action).run();
        });
    }

    @Test
    public void threeArgInterrupted() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Action onDispose = mock(Action.class);
            @SuppressWarnings("unchecked")
            Consumer<Integer> success = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<? super Throwable> consumer = mock(Consumer.class);
            Action action = mock(Action.class);
            Thread.currentThread().interrupt();
            Maybe.<Integer>never().doOnDispose(onDispose).blockingSubscribe(success, consumer, action);
            assertTrue("" + errors, errors.isEmpty());
            verify(onDispose).run();
            verify(success, never()).accept(any());
            verify(action, never()).run();
            verify(consumer).accept(any(InterruptedException.class));
        });
    }

    @Test
    public void observerSuccess() {
        TestObserver<Integer> to = new TestObserver<>();
        Maybe.just(1).blockingSubscribe(to);
        to.assertResult(1);
    }

    @Test
    public void observerSuccessAsync() {
        TestObserver<Integer> to = new TestObserver<>();
        Maybe.just(1).delay(50, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe(to);
        to.assertResult(1);
    }

    @Test
    public void observerEmpty() {
        TestObserver<Integer> to = new TestObserver<>();
        Maybe.<Integer>empty().blockingSubscribe(to);
        to.assertResult();
    }

    @Test
    public void observerEmptyAsync() {
        TestObserver<Integer> to = new TestObserver<>();
        Maybe.<Integer>empty().delay(50, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe(to);
        to.assertResult();
    }

    @Test
    public void observerError() {
        TestObserver<Object> to = new TestObserver<>();
        Maybe.error(new TestException()).blockingSubscribe(to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void observerErrorAsync() {
        TestObserver<Object> to = new TestObserver<>();
        Maybe.error(new TestException()).delay(50, TimeUnit.MILLISECONDS, Schedulers.computation()).blockingSubscribe(to);
        to.assertFailure(TestException.class);
    }

    @Test
    public void observerDispose() throws Throwable {
        Action onDispose = mock(Action.class);
        TestObserver<Object> to = new TestObserver<>();
        to.dispose();
        Maybe.never().doOnDispose(onDispose).blockingSubscribe(to);
        to.assertEmpty();
        verify(onDispose).run();
    }

    @Test
    public void ovserverInterrupted() throws Throwable {
        TestHelper.withErrorTracking(errors -> {
            Action onDispose = mock(Action.class);
            TestObserver<Object> to = new TestObserver<>();
            Thread.currentThread().interrupt();
            Maybe.never().doOnDispose(onDispose).blockingSubscribe(to);
            assertTrue("" + errors, errors.isEmpty());
            verify(onDispose).run();
            to.assertFailure(InterruptedException.class);
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeBlockingSubscribeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgSuccess() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.noArgSuccess);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgSuccessAsync() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.noArgSuccessAsync);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgEmpty() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.noArgEmpty);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgEmptyAsync() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.noArgEmptyAsync);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.noArgError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noArgErrorAsync() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.noArgErrorAsync);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgSuccess() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.oneArgSuccess);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgSuccessAsync() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.oneArgSuccessAsync);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgEmpty() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.oneArgEmpty);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgEmptyAsync() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.oneArgEmptyAsync);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgSuccessFails() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.oneArgSuccessFails);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.oneArgError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_oneArgErrorAsync() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.oneArgErrorAsync);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgSuccess() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.twoArgSuccess);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgSuccessAsync() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.twoArgSuccessAsync);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgEmpty() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.twoArgEmpty);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgEmptyAsync() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.twoArgEmptyAsync);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgSuccessFails() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.twoArgSuccessFails);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.twoArgError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgErrorAsync() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.twoArgErrorAsync);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_twoArgErrorFails() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.twoArgErrorFails);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_threeArgSuccess() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.threeArgSuccess);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_threeArgEmpty() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.threeArgEmpty);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_threeArgError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.threeArgError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_threeArgEmptyFails() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.threeArgEmptyFails);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_threeArgInterrupted() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.threeArgInterrupted);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerSuccess() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.observerSuccess);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerSuccessAsync() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.observerSuccessAsync);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerEmpty() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.observerEmpty);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerEmptyAsync() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.observerEmptyAsync);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerError() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.observerError);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerErrorAsync() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.observerErrorAsync);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerDispose() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.observerDispose);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ovserverInterrupted() throws java.lang.Throwable {
            this.runBenchmark(this.payloads.ovserverInterrupted);
        }

        private void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> payload) throws java.lang.Throwable {
            this.instance = new MaybeBlockingSubscribeTest();
            payload.accept(this.instance);
        }

        private static class _Payloads {

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> noArgSuccess;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> noArgSuccessAsync;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> noArgEmpty;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> noArgEmptyAsync;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> noArgError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> noArgErrorAsync;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> oneArgSuccess;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> oneArgSuccessAsync;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> oneArgEmpty;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> oneArgEmptyAsync;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> oneArgSuccessFails;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> oneArgError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> oneArgErrorAsync;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> twoArgSuccess;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> twoArgSuccessAsync;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> twoArgEmpty;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> twoArgEmptyAsync;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> twoArgSuccessFails;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> twoArgError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> twoArgErrorAsync;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> twoArgErrorFails;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> threeArgSuccess;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> threeArgEmpty;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> threeArgError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> threeArgEmptyFails;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> threeArgInterrupted;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> observerSuccess;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> observerSuccessAsync;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> observerEmpty;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> observerEmptyAsync;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> observerError;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> observerErrorAsync;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> observerDispose;

            public se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeBlockingSubscribeTest> ovserverInterrupted;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.noArgSuccess = MaybeBlockingSubscribeTest::noArgSuccess;
            this.payloads.noArgSuccessAsync = MaybeBlockingSubscribeTest::noArgSuccessAsync;
            this.payloads.noArgEmpty = MaybeBlockingSubscribeTest::noArgEmpty;
            this.payloads.noArgEmptyAsync = MaybeBlockingSubscribeTest::noArgEmptyAsync;
            this.payloads.noArgError = MaybeBlockingSubscribeTest::noArgError;
            this.payloads.noArgErrorAsync = MaybeBlockingSubscribeTest::noArgErrorAsync;
            this.payloads.oneArgSuccess = MaybeBlockingSubscribeTest::oneArgSuccess;
            this.payloads.oneArgSuccessAsync = MaybeBlockingSubscribeTest::oneArgSuccessAsync;
            this.payloads.oneArgEmpty = MaybeBlockingSubscribeTest::oneArgEmpty;
            this.payloads.oneArgEmptyAsync = MaybeBlockingSubscribeTest::oneArgEmptyAsync;
            this.payloads.oneArgSuccessFails = MaybeBlockingSubscribeTest::oneArgSuccessFails;
            this.payloads.oneArgError = MaybeBlockingSubscribeTest::oneArgError;
            this.payloads.oneArgErrorAsync = MaybeBlockingSubscribeTest::oneArgErrorAsync;
            this.payloads.twoArgSuccess = MaybeBlockingSubscribeTest::twoArgSuccess;
            this.payloads.twoArgSuccessAsync = MaybeBlockingSubscribeTest::twoArgSuccessAsync;
            this.payloads.twoArgEmpty = MaybeBlockingSubscribeTest::twoArgEmpty;
            this.payloads.twoArgEmptyAsync = MaybeBlockingSubscribeTest::twoArgEmptyAsync;
            this.payloads.twoArgSuccessFails = MaybeBlockingSubscribeTest::twoArgSuccessFails;
            this.payloads.twoArgError = MaybeBlockingSubscribeTest::twoArgError;
            this.payloads.twoArgErrorAsync = MaybeBlockingSubscribeTest::twoArgErrorAsync;
            this.payloads.twoArgErrorFails = MaybeBlockingSubscribeTest::twoArgErrorFails;
            this.payloads.threeArgSuccess = MaybeBlockingSubscribeTest::threeArgSuccess;
            this.payloads.threeArgEmpty = MaybeBlockingSubscribeTest::threeArgEmpty;
            this.payloads.threeArgError = MaybeBlockingSubscribeTest::threeArgError;
            this.payloads.threeArgEmptyFails = MaybeBlockingSubscribeTest::threeArgEmptyFails;
            this.payloads.threeArgInterrupted = MaybeBlockingSubscribeTest::threeArgInterrupted;
            this.payloads.observerSuccess = MaybeBlockingSubscribeTest::observerSuccess;
            this.payloads.observerSuccessAsync = MaybeBlockingSubscribeTest::observerSuccessAsync;
            this.payloads.observerEmpty = MaybeBlockingSubscribeTest::observerEmpty;
            this.payloads.observerEmptyAsync = MaybeBlockingSubscribeTest::observerEmptyAsync;
            this.payloads.observerError = MaybeBlockingSubscribeTest::observerError;
            this.payloads.observerErrorAsync = MaybeBlockingSubscribeTest::observerErrorAsync;
            this.payloads.observerDispose = MaybeBlockingSubscribeTest::observerDispose;
            this.payloads.ovserverInterrupted = MaybeBlockingSubscribeTest::ovserverInterrupted;
        }
    }
}
