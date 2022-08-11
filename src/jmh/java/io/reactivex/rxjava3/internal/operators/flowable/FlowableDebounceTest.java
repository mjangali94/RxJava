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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableDebounceTimed.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableDebounceTest extends RxJavaTest {

    private TestScheduler scheduler;

    private Subscriber<String> Subscriber;

    private Scheduler.Worker innerScheduler;

    @Before
    public void before() {
        scheduler = new TestScheduler();
        Subscriber = TestHelper.mockSubscriber();
        innerScheduler = scheduler.createWorker();
    }

    @Test
    public void debounceWithCompleted() {
        Flowable<String> source = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                // Should be skipped since "two" will arrive before the timeout expires.
                publishNext(subscriber, 100, "one");
                // Should be published since "three" will arrive after the timeout expires.
                publishNext(subscriber, 400, "two");
                // Should be skipped since onComplete will arrive before the timeout expires.
                publishNext(subscriber, 900, "three");
                // Should be published as soon as the timeout expires.
                publishCompleted(subscriber, 1000);
            }
        });
        Flowable<String> sampled = source.debounce(400, TimeUnit.MILLISECONDS, scheduler);
        sampled.subscribe(Subscriber);
        scheduler.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        InOrder inOrder = inOrder(Subscriber);
        // must go to 800 since it must be 400 after when two is sent, which is at 400
        scheduler.advanceTimeTo(800, TimeUnit.MILLISECONDS);
        inOrder.verify(Subscriber, times(1)).onNext("two");
        scheduler.advanceTimeTo(1000, TimeUnit.MILLISECONDS);
        inOrder.verify(Subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void debounceNeverEmits() {
        Flowable<String> source = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                // all should be skipped since they are happening faster than the 200ms timeout
                // Should be skipped
                publishNext(subscriber, 100, "a");
                // Should be skipped
                publishNext(subscriber, 200, "b");
                // Should be skipped
                publishNext(subscriber, 300, "c");
                // Should be skipped
                publishNext(subscriber, 400, "d");
                // Should be skipped
                publishNext(subscriber, 500, "e");
                // Should be skipped
                publishNext(subscriber, 600, "f");
                // Should be skipped
                publishNext(subscriber, 700, "g");
                // Should be skipped
                publishNext(subscriber, 800, "h");
                // Should be published as soon as the timeout expires.
                publishCompleted(subscriber, 900);
            }
        });
        Flowable<String> sampled = source.debounce(200, TimeUnit.MILLISECONDS, scheduler);
        sampled.subscribe(Subscriber);
        scheduler.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        InOrder inOrder = inOrder(Subscriber);
        inOrder.verify(Subscriber, times(0)).onNext(anyString());
        scheduler.advanceTimeTo(1000, TimeUnit.MILLISECONDS);
        inOrder.verify(Subscriber, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void debounceWithError() {
        Flowable<String> source = Flowable.unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                Exception error = new TestException();
                // Should be published since "two" will arrive after the timeout expires.
                publishNext(subscriber, 100, "one");
                // Should be skipped since onError will arrive before the timeout expires.
                publishNext(subscriber, 600, "two");
                // Should be published as soon as the timeout expires.
                publishError(subscriber, 700, error);
            }
        });
        Flowable<String> sampled = source.debounce(400, TimeUnit.MILLISECONDS, scheduler);
        sampled.subscribe(Subscriber);
        scheduler.advanceTimeTo(0, TimeUnit.MILLISECONDS);
        InOrder inOrder = inOrder(Subscriber);
        // 100 + 400 means it triggers at 500
        scheduler.advanceTimeTo(500, TimeUnit.MILLISECONDS);
        inOrder.verify(Subscriber).onNext("one");
        scheduler.advanceTimeTo(701, TimeUnit.MILLISECONDS);
        inOrder.verify(Subscriber).onError(any(TestException.class));
        inOrder.verifyNoMoreInteractions();
    }

    private <T> void publishCompleted(final Subscriber<T> subscriber, long delay) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                subscriber.onComplete();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private <T> void publishError(final Subscriber<T> subscriber, long delay, final Exception error) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                subscriber.onError(error);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private <T> void publishNext(final Subscriber<T> subscriber, final long delay, final T value) {
        innerScheduler.schedule(new Runnable() {

            @Override
            public void run() {
                subscriber.onNext(value);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    @Test
    public void debounceSelectorNormal1() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> debouncer = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> debounceSel = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return debouncer;
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        InOrder inOrder = inOrder(subscriber);
        source.debounce(debounceSel).subscribe(subscriber);
        source.onNext(1);
        debouncer.onNext(1);
        source.onNext(2);
        source.onNext(3);
        source.onNext(4);
        debouncer.onNext(2);
        source.onNext(5);
        source.onComplete();
        inOrder.verify(subscriber).onNext(1);
        inOrder.verify(subscriber).onNext(4);
        inOrder.verify(subscriber).onNext(5);
        inOrder.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void debounceSelectorFuncThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> debounceSel = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                throw new TestException();
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.debounce(debounceSel).subscribe(subscriber);
        source.onNext(1);
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
        verify(subscriber).onError(any(TestException.class));
    }

    @Test
    public void debounceSelectorFlowableThrows() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> debounceSel = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return Flowable.error(new TestException());
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.debounce(debounceSel).subscribe(subscriber);
        source.onNext(1);
        verify(subscriber, never()).onNext(any());
        verify(subscriber, never()).onComplete();
        verify(subscriber).onError(any(TestException.class));
    }

    @Test
    public void debounceTimedLastIsNotLost() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.debounce(100, TimeUnit.MILLISECONDS, scheduler).subscribe(subscriber);
        source.onNext(1);
        source.onComplete();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        verify(subscriber).onNext(1);
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void debounceSelectorLastIsNotLost() {
        PublishProcessor<Integer> source = PublishProcessor.create();
        final PublishProcessor<Integer> debouncer = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> debounceSel = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                return debouncer;
            }
        };
        Subscriber<Object> subscriber = TestHelper.mockSubscriber();
        source.debounce(debounceSel).subscribe(subscriber);
        source.onNext(1);
        source.onComplete();
        debouncer.onComplete();
        verify(subscriber).onNext(1);
        verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void debounceWithTimeBackpressure() throws InterruptedException {
        TestScheduler scheduler = new TestScheduler();
        TestSubscriberEx<Integer> subscriber = new TestSubscriberEx<>();
        Flowable.merge(Flowable.just(1), Flowable.just(2).delay(10, TimeUnit.MILLISECONDS, scheduler)).debounce(20, TimeUnit.MILLISECONDS, scheduler).take(1).subscribe(subscriber);
        scheduler.advanceTimeBy(30, TimeUnit.MILLISECONDS);
        subscriber.assertValue(2);
        subscriber.assertTerminated();
        subscriber.assertNoErrors();
    }

    @Test
    public void debounceDefaultScheduler() throws Exception {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.range(1, 1000).debounce(1, TimeUnit.SECONDS).subscribe(ts);
        ts.awaitDone(5, TimeUnit.SECONDS);
        ts.assertValue(1000);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void debounceDefault() throws Exception {
        Flowable.just(1).debounce(1, TimeUnit.SECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishProcessor.create().debounce(1, TimeUnit.SECONDS, new TestScheduler()));
        TestHelper.checkDisposed(PublishProcessor.create().debounce(Functions.justFunction(Flowable.never())));
        Disposable d = new FlowableDebounceTimed.DebounceEmitter<>(1, 1, null);
        assertFalse(d.isDisposed());
        d.dispose();
        assertTrue(d.isDisposed());
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onComplete();
                    subscriber.onNext(1);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                }
            }.debounce(1, TimeUnit.SECONDS, new TestScheduler()).test().assertResult();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void badSourceSelector() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.debounce(new Function<Integer, Flowable<Long>>() {

                    @Override
                    public Flowable<Long> apply(Integer v) throws Exception {
                        return Flowable.timer(1, TimeUnit.SECONDS);
                    }
                });
            }
        }, false, 1, 1, 1);
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(final Flowable<Integer> f) throws Exception {
                return Flowable.just(1).debounce(new Function<Integer, Flowable<Integer>>() {

                    @Override
                    public Flowable<Integer> apply(Integer v) throws Exception {
                        return f;
                    }
                });
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void debounceWithEmpty() {
        Flowable.just(1).debounce(Functions.justFunction(Flowable.empty())).test().assertResult(1);
    }

    @Test
    public void backpressureNoRequest() {
        Flowable.just(1).debounce(Functions.justFunction(Flowable.timer(1, TimeUnit.MILLISECONDS))).test(0L).awaitDone(5, TimeUnit.SECONDS).assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void backpressureNoRequestTimed() {
        Flowable.just(1).debounce(1, TimeUnit.MILLISECONDS).test(0L).awaitDone(5, TimeUnit.SECONDS).assertFailure(MissingBackpressureException.class);
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Object> f) throws Exception {
                return f.debounce(Functions.justFunction(Flowable.never()));
            }
        });
    }

    @Test
    public void disposeInOnNext() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        BehaviorProcessor.createDefault(1).debounce(new Function<Integer, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Integer o) throws Exception {
                ts.cancel();
                return Flowable.never();
            }
        }).subscribeWith(ts).assertEmpty();
        assertTrue(ts.isCancelled());
    }

    @Test
    public void disposedInOnComplete() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                ts.cancel();
                subscriber.onComplete();
            }
        }.debounce(Functions.justFunction(Flowable.never())).subscribeWith(ts).assertEmpty();
    }

    @Test
    public void emitLate() {
        final AtomicReference<Subscriber<? super Integer>> ref = new AtomicReference<>();
        TestSubscriber<Integer> ts = Flowable.range(1, 2).debounce(new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer o) throws Exception {
                if (o != 1) {
                    return Flowable.never();
                }
                return new Flowable<Integer>() {

                    @Override
                    protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                        subscriber.onSubscribe(new BooleanSubscription());
                        ref.set(subscriber);
                    }
                };
            }
        }).test();
        ref.get().onNext(1);
        ts.assertResult(2);
    }

    @Test
    public void badRequestReported() {
        TestHelper.assertBadRequestReported(Flowable.never().debounce(Functions.justFunction(Flowable.never())));
    }

    @Test
    public void timedDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> f) throws Exception {
                return f.debounce(1, TimeUnit.SECONDS);
            }
        });
    }

    @Test
    public void timedDisposedIgnoredBySource() {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        new Flowable<Integer>() {

            @Override
            protected void subscribeActual(org.reactivestreams.Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                ts.cancel();
                s.onNext(1);
                s.onComplete();
            }
        }.debounce(1, TimeUnit.SECONDS).subscribe(ts);
    }

    @Test
    public void timedBadRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().debounce(1, TimeUnit.SECONDS));
    }

    @Test
    public void timedLateEmit() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        DebounceTimedSubscriber<Integer> sub = new DebounceTimedSubscriber<>(ts, 1, TimeUnit.SECONDS, new TestScheduler().createWorker());
        sub.onSubscribe(new BooleanSubscription());
        DebounceEmitter<Integer> de = new DebounceEmitter<>(1, 50, sub);
        de.emit();
        de.emit();
        ts.assertEmpty();
    }

    @Test
    public void timedError() {
        Flowable.error(new TestException()).debounce(1, TimeUnit.SECONDS).test().assertFailure(TestException.class);
    }

    @Test
    public void debounceOnEmpty() {
        Flowable.empty().debounce(new Function<Object, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Object o) {
                return Flowable.just(new Object());
            }
        }).subscribe();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableDebounceTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceWithCompleted() throws java.lang.Throwable {
            this.payloads.debounceWithCompleted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceNeverEmits() throws java.lang.Throwable {
            this.payloads.debounceNeverEmits.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceWithError() throws java.lang.Throwable {
            this.payloads.debounceWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceSelectorNormal1() throws java.lang.Throwable {
            this.payloads.debounceSelectorNormal1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceSelectorFuncThrows() throws java.lang.Throwable {
            this.payloads.debounceSelectorFuncThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceSelectorFlowableThrows() throws java.lang.Throwable {
            this.payloads.debounceSelectorFlowableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceTimedLastIsNotLost() throws java.lang.Throwable {
            this.payloads.debounceTimedLastIsNotLost.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceSelectorLastIsNotLost() throws java.lang.Throwable {
            this.payloads.debounceSelectorLastIsNotLost.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceWithTimeBackpressure() throws java.lang.Throwable {
            this.payloads.debounceWithTimeBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceDefaultScheduler() throws java.lang.Throwable {
            this.payloads.debounceDefaultScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceDefault() throws java.lang.Throwable {
            this.payloads.debounceDefault.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceSelector() throws java.lang.Throwable {
            this.payloads.badSourceSelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceWithEmpty() throws java.lang.Throwable {
            this.payloads.debounceWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNoRequest() throws java.lang.Throwable {
            this.payloads.backpressureNoRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_backpressureNoRequestTimed() throws java.lang.Throwable {
            this.payloads.backpressureNoRequestTimed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeInOnNext() throws java.lang.Throwable {
            this.payloads.disposeInOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedInOnComplete() throws java.lang.Throwable {
            this.payloads.disposedInOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emitLate() throws java.lang.Throwable {
            this.payloads.emitLate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequestReported() throws java.lang.Throwable {
            this.payloads.badRequestReported.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.timedDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedDisposedIgnoredBySource() throws java.lang.Throwable {
            this.payloads.timedDisposedIgnoredBySource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedBadRequest() throws java.lang.Throwable {
            this.payloads.timedBadRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedLateEmit() throws java.lang.Throwable {
            this.payloads.timedLateEmit.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timedError() throws java.lang.Throwable {
            this.payloads.timedError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_debounceOnEmpty() throws java.lang.Throwable {
            this.payloads.debounceOnEmpty.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDebounceTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDebounceTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.before();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDebounceTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDebounceTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableDebounceTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDebounceTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableDebounceTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableDebounceTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement debounceWithCompleted;

            public org.junit.runners.model.Statement debounceNeverEmits;

            public org.junit.runners.model.Statement debounceWithError;

            public org.junit.runners.model.Statement debounceSelectorNormal1;

            public org.junit.runners.model.Statement debounceSelectorFuncThrows;

            public org.junit.runners.model.Statement debounceSelectorFlowableThrows;

            public org.junit.runners.model.Statement debounceTimedLastIsNotLost;

            public org.junit.runners.model.Statement debounceSelectorLastIsNotLost;

            public org.junit.runners.model.Statement debounceWithTimeBackpressure;

            public org.junit.runners.model.Statement debounceDefaultScheduler;

            public org.junit.runners.model.Statement debounceDefault;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement badSourceSelector;

            public org.junit.runners.model.Statement debounceWithEmpty;

            public org.junit.runners.model.Statement backpressureNoRequest;

            public org.junit.runners.model.Statement backpressureNoRequestTimed;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement disposeInOnNext;

            public org.junit.runners.model.Statement disposedInOnComplete;

            public org.junit.runners.model.Statement emitLate;

            public org.junit.runners.model.Statement badRequestReported;

            public org.junit.runners.model.Statement timedDoubleOnSubscribe;

            public org.junit.runners.model.Statement timedDisposedIgnoredBySource;

            public org.junit.runners.model.Statement timedBadRequest;

            public org.junit.runners.model.Statement timedLateEmit;

            public org.junit.runners.model.Statement timedError;

            public org.junit.runners.model.Statement debounceOnEmpty;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.debounceWithCompleted = _ClassStatement.forPayload(FlowableDebounceTest::debounceWithCompleted, "debounceWithCompleted", this);
            this.payloads.debounceNeverEmits = _ClassStatement.forPayload(FlowableDebounceTest::debounceNeverEmits, "debounceNeverEmits", this);
            this.payloads.debounceWithError = _ClassStatement.forPayload(FlowableDebounceTest::debounceWithError, "debounceWithError", this);
            this.payloads.debounceSelectorNormal1 = _ClassStatement.forPayload(FlowableDebounceTest::debounceSelectorNormal1, "debounceSelectorNormal1", this);
            this.payloads.debounceSelectorFuncThrows = _ClassStatement.forPayload(FlowableDebounceTest::debounceSelectorFuncThrows, "debounceSelectorFuncThrows", this);
            this.payloads.debounceSelectorFlowableThrows = _ClassStatement.forPayload(FlowableDebounceTest::debounceSelectorFlowableThrows, "debounceSelectorFlowableThrows", this);
            this.payloads.debounceTimedLastIsNotLost = _ClassStatement.forPayload(FlowableDebounceTest::debounceTimedLastIsNotLost, "debounceTimedLastIsNotLost", this);
            this.payloads.debounceSelectorLastIsNotLost = _ClassStatement.forPayload(FlowableDebounceTest::debounceSelectorLastIsNotLost, "debounceSelectorLastIsNotLost", this);
            this.payloads.debounceWithTimeBackpressure = _ClassStatement.forPayload(FlowableDebounceTest::debounceWithTimeBackpressure, "debounceWithTimeBackpressure", this);
            this.payloads.debounceDefaultScheduler = _ClassStatement.forPayload(FlowableDebounceTest::debounceDefaultScheduler, "debounceDefaultScheduler", this);
            this.payloads.debounceDefault = _ClassStatement.forPayload(FlowableDebounceTest::debounceDefault, "debounceDefault", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableDebounceTest::dispose, "dispose", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableDebounceTest::badSource, "badSource", this);
            this.payloads.badSourceSelector = _ClassStatement.forPayload(FlowableDebounceTest::badSourceSelector, "badSourceSelector", this);
            this.payloads.debounceWithEmpty = _ClassStatement.forPayload(FlowableDebounceTest::debounceWithEmpty, "debounceWithEmpty", this);
            this.payloads.backpressureNoRequest = _ClassStatement.forPayload(FlowableDebounceTest::backpressureNoRequest, "backpressureNoRequest", this);
            this.payloads.backpressureNoRequestTimed = _ClassStatement.forPayload(FlowableDebounceTest::backpressureNoRequestTimed, "backpressureNoRequestTimed", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableDebounceTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.disposeInOnNext = _ClassStatement.forPayload(FlowableDebounceTest::disposeInOnNext, "disposeInOnNext", this);
            this.payloads.disposedInOnComplete = _ClassStatement.forPayload(FlowableDebounceTest::disposedInOnComplete, "disposedInOnComplete", this);
            this.payloads.emitLate = _ClassStatement.forPayload(FlowableDebounceTest::emitLate, "emitLate", this);
            this.payloads.badRequestReported = _ClassStatement.forPayload(FlowableDebounceTest::badRequestReported, "badRequestReported", this);
            this.payloads.timedDoubleOnSubscribe = _ClassStatement.forPayload(FlowableDebounceTest::timedDoubleOnSubscribe, "timedDoubleOnSubscribe", this);
            this.payloads.timedDisposedIgnoredBySource = _ClassStatement.forPayload(FlowableDebounceTest::timedDisposedIgnoredBySource, "timedDisposedIgnoredBySource", this);
            this.payloads.timedBadRequest = _ClassStatement.forPayload(FlowableDebounceTest::timedBadRequest, "timedBadRequest", this);
            this.payloads.timedLateEmit = _ClassStatement.forPayload(FlowableDebounceTest::timedLateEmit, "timedLateEmit", this);
            this.payloads.timedError = _ClassStatement.forPayload(FlowableDebounceTest::timedError, "timedError", this);
            this.payloads.debounceOnEmpty = _ClassStatement.forPayload(FlowableDebounceTest::debounceOnEmpty, "debounceOnEmpty", this);
        }
    }
}
