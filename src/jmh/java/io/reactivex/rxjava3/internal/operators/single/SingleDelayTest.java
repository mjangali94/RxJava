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
package io.reactivex.rxjava3.internal.operators.single;

import static org.junit.Assert.assertNotEquals;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class SingleDelayTest extends RxJavaTest {

    @Test
    public void delayOnSuccess() {
        final TestScheduler scheduler = new TestScheduler();
        final TestObserver<Integer> observer = Single.just(1).delay(5, TimeUnit.SECONDS, scheduler).test();
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS);
        observer.assertNoValues();
        scheduler.advanceTimeTo(5, TimeUnit.SECONDS);
        observer.assertValue(1);
    }

    @Test
    public void delayOnError() {
        final TestScheduler scheduler = new TestScheduler();
        final TestObserver<?> observer = Single.error(new TestException()).delay(5, TimeUnit.SECONDS, scheduler).test();
        scheduler.triggerActions();
        observer.assertError(TestException.class);
    }

    @Test
    public void delayedErrorOnSuccess() {
        final TestScheduler scheduler = new TestScheduler();
        final TestObserver<Integer> observer = Single.just(1).delay(5, TimeUnit.SECONDS, scheduler, true).test();
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS);
        observer.assertNoValues();
        scheduler.advanceTimeTo(5, TimeUnit.SECONDS);
        observer.assertValue(1);
    }

    @Test
    public void delayedErrorOnError() {
        final TestScheduler scheduler = new TestScheduler();
        final TestObserver<?> observer = Single.error(new TestException()).delay(5, TimeUnit.SECONDS, scheduler, true).test();
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS);
        observer.assertNoErrors();
        scheduler.advanceTimeTo(5, TimeUnit.SECONDS);
        observer.assertError(TestException.class);
    }

    @Test
    public void delaySubscriptionCompletable() throws Exception {
        Single.just(1).delaySubscription(Completable.complete().delay(100, TimeUnit.MILLISECONDS)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void delaySubscriptionObservable() throws Exception {
        Single.just(1).delaySubscription(Observable.timer(100, TimeUnit.MILLISECONDS)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void delaySubscriptionFlowable() throws Exception {
        Single.just(1).delaySubscription(Flowable.timer(100, TimeUnit.MILLISECONDS)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void delaySubscriptionSingle() throws Exception {
        Single.just(1).delaySubscription(Single.timer(100, TimeUnit.MILLISECONDS)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void delaySubscriptionTime() throws Exception {
        Single.just(1).delaySubscription(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void delaySubscriptionTimeCustomScheduler() throws Exception {
        Single.just(1).delaySubscription(100, TimeUnit.MILLISECONDS, Schedulers.io()).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void onErrorCalledOnScheduler() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Thread> thread = new AtomicReference<>();
        Single.<String>error(new Exception()).delay(0, TimeUnit.MILLISECONDS, Schedulers.newThread()).doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable throwable) throws Exception {
                thread.set(Thread.currentThread());
                latch.countDown();
            }
        }).onErrorResumeWith(Single.just("")).subscribe();
        latch.await();
        assertNotEquals(Thread.currentThread(), thread.get());
    }

    @Test
    public void withPublisherDispose() {
        TestHelper.checkDisposed(PublishSubject.create().singleOrError().delaySubscription(Flowable.just(1)));
    }

    @Test
    public void withPublisherError() {
        Single.just(1).delaySubscription(Flowable.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void withPublisherError2() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Single.just(1).delaySubscription(new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onError(new TestException());
                }
            }).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void withObservableDispose() {
        TestHelper.checkDisposed(PublishSubject.create().singleOrError().delaySubscription(Observable.just(1)));
    }

    @Test
    public void withObservableError() {
        Single.just(1).delaySubscription(Observable.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void withObservableError2() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Single.just(1).delaySubscription(new Observable<Integer>() {

                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposable.empty());
                    observer.onNext(1);
                    observer.onError(new TestException());
                }
            }).test().assertResult(1);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void withSingleErrors() {
        Single.just(1).delaySubscription(Single.error(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void withSingleDispose() {
        TestHelper.checkDisposed(Single.just(1).delaySubscription(Single.just(2)));
    }

    @Test
    public void withCompletableDispose() {
        TestHelper.checkDisposed(Completable.complete().andThen(Single.just(1)));
    }

    @Test
    public void withCompletableDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeCompletableToSingle(new Function<Completable, Single<Object>>() {

            @Override
            public Single<Object> apply(Completable c) throws Exception {
                return c.andThen(Single.just((Object) 1));
            }
        });
    }

    @Test
    public void withSingleDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingle(new Function<Single<Object>, Single<Object>>() {

            @Override
            public Single<Object> apply(Single<Object> s) throws Exception {
                return Single.just((Object) 1).delaySubscription(s);
            }
        });
    }

    @Test
    public void withPublisherDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowableToSingle(f -> SingleSubject.create().delaySubscription(f));
    }

    @Test
    public void withObservableDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToSingle(o -> SingleSubject.create().delaySubscription(o));
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleDelayTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayOnSuccess() throws java.lang.Throwable {
            this.payloads.delayOnSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayOnError() throws java.lang.Throwable {
            this.payloads.delayOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayedErrorOnSuccess() throws java.lang.Throwable {
            this.payloads.delayedErrorOnSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayedErrorOnError() throws java.lang.Throwable {
            this.payloads.delayedErrorOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delaySubscriptionCompletable() throws java.lang.Throwable {
            this.payloads.delaySubscriptionCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delaySubscriptionObservable() throws java.lang.Throwable {
            this.payloads.delaySubscriptionObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delaySubscriptionFlowable() throws java.lang.Throwable {
            this.payloads.delaySubscriptionFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delaySubscriptionSingle() throws java.lang.Throwable {
            this.payloads.delaySubscriptionSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delaySubscriptionTime() throws java.lang.Throwable {
            this.payloads.delaySubscriptionTime.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delaySubscriptionTimeCustomScheduler() throws java.lang.Throwable {
            this.payloads.delaySubscriptionTimeCustomScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCalledOnScheduler() throws java.lang.Throwable {
            this.payloads.onErrorCalledOnScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withPublisherDispose() throws java.lang.Throwable {
            this.payloads.withPublisherDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withPublisherError() throws java.lang.Throwable {
            this.payloads.withPublisherError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withPublisherError2() throws java.lang.Throwable {
            this.payloads.withPublisherError2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withObservableDispose() throws java.lang.Throwable {
            this.payloads.withObservableDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withObservableError() throws java.lang.Throwable {
            this.payloads.withObservableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withObservableError2() throws java.lang.Throwable {
            this.payloads.withObservableError2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withSingleErrors() throws java.lang.Throwable {
            this.payloads.withSingleErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withSingleDispose() throws java.lang.Throwable {
            this.payloads.withSingleDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withCompletableDispose() throws java.lang.Throwable {
            this.payloads.withCompletableDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withCompletableDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.withCompletableDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withSingleDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.withSingleDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withPublisherDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.withPublisherDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withObservableDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.withObservableDoubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDelayTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDelayTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDelayTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDelayTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleDelayTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleDelayTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleDelayTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleDelayTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement delayOnSuccess;

            public org.junit.runners.model.Statement delayOnError;

            public org.junit.runners.model.Statement delayedErrorOnSuccess;

            public org.junit.runners.model.Statement delayedErrorOnError;

            public org.junit.runners.model.Statement delaySubscriptionCompletable;

            public org.junit.runners.model.Statement delaySubscriptionObservable;

            public org.junit.runners.model.Statement delaySubscriptionFlowable;

            public org.junit.runners.model.Statement delaySubscriptionSingle;

            public org.junit.runners.model.Statement delaySubscriptionTime;

            public org.junit.runners.model.Statement delaySubscriptionTimeCustomScheduler;

            public org.junit.runners.model.Statement onErrorCalledOnScheduler;

            public org.junit.runners.model.Statement withPublisherDispose;

            public org.junit.runners.model.Statement withPublisherError;

            public org.junit.runners.model.Statement withPublisherError2;

            public org.junit.runners.model.Statement withObservableDispose;

            public org.junit.runners.model.Statement withObservableError;

            public org.junit.runners.model.Statement withObservableError2;

            public org.junit.runners.model.Statement withSingleErrors;

            public org.junit.runners.model.Statement withSingleDispose;

            public org.junit.runners.model.Statement withCompletableDispose;

            public org.junit.runners.model.Statement withCompletableDoubleOnSubscribe;

            public org.junit.runners.model.Statement withSingleDoubleOnSubscribe;

            public org.junit.runners.model.Statement withPublisherDoubleOnSubscribe;

            public org.junit.runners.model.Statement withObservableDoubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.delayOnSuccess = _ClassStatement.forPayload(SingleDelayTest::delayOnSuccess, "delayOnSuccess", this);
            this.payloads.delayOnError = _ClassStatement.forPayload(SingleDelayTest::delayOnError, "delayOnError", this);
            this.payloads.delayedErrorOnSuccess = _ClassStatement.forPayload(SingleDelayTest::delayedErrorOnSuccess, "delayedErrorOnSuccess", this);
            this.payloads.delayedErrorOnError = _ClassStatement.forPayload(SingleDelayTest::delayedErrorOnError, "delayedErrorOnError", this);
            this.payloads.delaySubscriptionCompletable = _ClassStatement.forPayload(SingleDelayTest::delaySubscriptionCompletable, "delaySubscriptionCompletable", this);
            this.payloads.delaySubscriptionObservable = _ClassStatement.forPayload(SingleDelayTest::delaySubscriptionObservable, "delaySubscriptionObservable", this);
            this.payloads.delaySubscriptionFlowable = _ClassStatement.forPayload(SingleDelayTest::delaySubscriptionFlowable, "delaySubscriptionFlowable", this);
            this.payloads.delaySubscriptionSingle = _ClassStatement.forPayload(SingleDelayTest::delaySubscriptionSingle, "delaySubscriptionSingle", this);
            this.payloads.delaySubscriptionTime = _ClassStatement.forPayload(SingleDelayTest::delaySubscriptionTime, "delaySubscriptionTime", this);
            this.payloads.delaySubscriptionTimeCustomScheduler = _ClassStatement.forPayload(SingleDelayTest::delaySubscriptionTimeCustomScheduler, "delaySubscriptionTimeCustomScheduler", this);
            this.payloads.onErrorCalledOnScheduler = _ClassStatement.forPayload(SingleDelayTest::onErrorCalledOnScheduler, "onErrorCalledOnScheduler", this);
            this.payloads.withPublisherDispose = _ClassStatement.forPayload(SingleDelayTest::withPublisherDispose, "withPublisherDispose", this);
            this.payloads.withPublisherError = _ClassStatement.forPayload(SingleDelayTest::withPublisherError, "withPublisherError", this);
            this.payloads.withPublisherError2 = _ClassStatement.forPayload(SingleDelayTest::withPublisherError2, "withPublisherError2", this);
            this.payloads.withObservableDispose = _ClassStatement.forPayload(SingleDelayTest::withObservableDispose, "withObservableDispose", this);
            this.payloads.withObservableError = _ClassStatement.forPayload(SingleDelayTest::withObservableError, "withObservableError", this);
            this.payloads.withObservableError2 = _ClassStatement.forPayload(SingleDelayTest::withObservableError2, "withObservableError2", this);
            this.payloads.withSingleErrors = _ClassStatement.forPayload(SingleDelayTest::withSingleErrors, "withSingleErrors", this);
            this.payloads.withSingleDispose = _ClassStatement.forPayload(SingleDelayTest::withSingleDispose, "withSingleDispose", this);
            this.payloads.withCompletableDispose = _ClassStatement.forPayload(SingleDelayTest::withCompletableDispose, "withCompletableDispose", this);
            this.payloads.withCompletableDoubleOnSubscribe = _ClassStatement.forPayload(SingleDelayTest::withCompletableDoubleOnSubscribe, "withCompletableDoubleOnSubscribe", this);
            this.payloads.withSingleDoubleOnSubscribe = _ClassStatement.forPayload(SingleDelayTest::withSingleDoubleOnSubscribe, "withSingleDoubleOnSubscribe", this);
            this.payloads.withPublisherDoubleOnSubscribe = _ClassStatement.forPayload(SingleDelayTest::withPublisherDoubleOnSubscribe, "withPublisherDoubleOnSubscribe", this);
            this.payloads.withObservableDoubleOnSubscribe = _ClassStatement.forPayload(SingleDelayTest::withObservableDoubleOnSubscribe, "withObservableDoubleOnSubscribe", this);
        }
    }
}
