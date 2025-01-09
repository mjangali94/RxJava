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

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableDelaySubscriptionOtherTest extends RxJavaTest {

    @Test
    public void noPrematureSubscription() {
        PublishSubject<Object> other = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<>();
        final AtomicInteger subscribed = new AtomicInteger();
        Observable.just(1).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                subscribed.getAndIncrement();
            }
        }).delaySubscription(other).subscribe(to);
        to.assertNotComplete();
        to.assertNoErrors();
        to.assertNoValues();
        Assert.assertEquals("Premature subscription", 0, subscribed.get());
        other.onNext(1);
        Assert.assertEquals("No subscription", 1, subscribed.get());
        to.assertValue(1);
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void noMultipleSubscriptions() {
        PublishSubject<Object> other = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<>();
        final AtomicInteger subscribed = new AtomicInteger();
        Observable.just(1).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                subscribed.getAndIncrement();
            }
        }).delaySubscription(other).subscribe(to);
        to.assertNotComplete();
        to.assertNoErrors();
        to.assertNoValues();
        Assert.assertEquals("Premature subscription", 0, subscribed.get());
        other.onNext(1);
        other.onNext(2);
        Assert.assertEquals("No subscription", 1, subscribed.get());
        to.assertValue(1);
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void completeTriggersSubscription() {
        PublishSubject<Object> other = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<>();
        final AtomicInteger subscribed = new AtomicInteger();
        Observable.just(1).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                subscribed.getAndIncrement();
            }
        }).delaySubscription(other).subscribe(to);
        to.assertNotComplete();
        to.assertNoErrors();
        to.assertNoValues();
        Assert.assertEquals("Premature subscription", 0, subscribed.get());
        other.onComplete();
        Assert.assertEquals("No subscription", 1, subscribed.get());
        to.assertValue(1);
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void noPrematureSubscriptionToError() {
        PublishSubject<Object> other = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<>();
        final AtomicInteger subscribed = new AtomicInteger();
        Observable.<Integer>error(new TestException()).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                subscribed.getAndIncrement();
            }
        }).delaySubscription(other).subscribe(to);
        to.assertNotComplete();
        to.assertNoErrors();
        to.assertNoValues();
        Assert.assertEquals("Premature subscription", 0, subscribed.get());
        other.onComplete();
        Assert.assertEquals("No subscription", 1, subscribed.get());
        to.assertNoValues();
        to.assertNotComplete();
        to.assertError(TestException.class);
    }

    @Test
    public void noSubscriptionIfOtherErrors() {
        PublishSubject<Object> other = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<>();
        final AtomicInteger subscribed = new AtomicInteger();
        Observable.<Integer>error(new TestException()).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                subscribed.getAndIncrement();
            }
        }).delaySubscription(other).subscribe(to);
        to.assertNotComplete();
        to.assertNoErrors();
        to.assertNoValues();
        Assert.assertEquals("Premature subscription", 0, subscribed.get());
        other.onError(new TestException());
        Assert.assertEquals("Premature subscription", 0, subscribed.get());
        to.assertNoValues();
        to.assertNotComplete();
        to.assertError(TestException.class);
    }

    @Test
    public void badSourceOther() {
        TestHelper.checkBadSourceObservable(new Function<Observable<Integer>, Object>() {

            @Override
            public Object apply(Observable<Integer> o) throws Exception {
                return Observable.just(1).delaySubscription(o);
            }
        }, false, 1, 1, 1);
    }

    @Test
    public void afterDelayNoInterrupt() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        try {
            for (Scheduler s : new Scheduler[] { Schedulers.single(), Schedulers.computation(), Schedulers.newThread(), Schedulers.io(), Schedulers.from(exec) }) {
                final TestObserver<Boolean> observer = TestObserver.create();
                observer.withTag(s.getClass().getSimpleName());
                Observable.<Boolean>create(new ObservableOnSubscribe<Boolean>() {

                    @Override
                    public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                        emitter.onNext(Thread.interrupted());
                        emitter.onComplete();
                    }
                }).delaySubscription(100, TimeUnit.MILLISECONDS, s).subscribe(observer);
                observer.awaitDone(5, TimeUnit.SECONDS);
                observer.assertValue(false);
            }
        } finally {
            exec.shutdown();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableDelaySubscriptionOtherTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noPrematureSubscription() throws java.lang.Throwable {
            this.payloads.noPrematureSubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noMultipleSubscriptions() throws java.lang.Throwable {
            this.payloads.noMultipleSubscriptions.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeTriggersSubscription() throws java.lang.Throwable {
            this.payloads.completeTriggersSubscription.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noPrematureSubscriptionToError() throws java.lang.Throwable {
            this.payloads.noPrematureSubscriptionToError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noSubscriptionIfOtherErrors() throws java.lang.Throwable {
            this.payloads.noSubscriptionIfOtherErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceOther() throws java.lang.Throwable {
            this.payloads.badSourceOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_afterDelayNoInterrupt() throws java.lang.Throwable {
            this.payloads.afterDelayNoInterrupt.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDelaySubscriptionOtherTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDelaySubscriptionOtherTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDelaySubscriptionOtherTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDelaySubscriptionOtherTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableDelaySubscriptionOtherTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableDelaySubscriptionOtherTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableDelaySubscriptionOtherTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableDelaySubscriptionOtherTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement noPrematureSubscription;

            public org.junit.runners.model.Statement noMultipleSubscriptions;

            public org.junit.runners.model.Statement completeTriggersSubscription;

            public org.junit.runners.model.Statement noPrematureSubscriptionToError;

            public org.junit.runners.model.Statement noSubscriptionIfOtherErrors;

            public org.junit.runners.model.Statement badSourceOther;

            public org.junit.runners.model.Statement afterDelayNoInterrupt;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.noPrematureSubscription = _ClassStatement.forPayload(ObservableDelaySubscriptionOtherTest::noPrematureSubscription, "noPrematureSubscription", this);
            this.payloads.noMultipleSubscriptions = _ClassStatement.forPayload(ObservableDelaySubscriptionOtherTest::noMultipleSubscriptions, "noMultipleSubscriptions", this);
            this.payloads.completeTriggersSubscription = _ClassStatement.forPayload(ObservableDelaySubscriptionOtherTest::completeTriggersSubscription, "completeTriggersSubscription", this);
            this.payloads.noPrematureSubscriptionToError = _ClassStatement.forPayload(ObservableDelaySubscriptionOtherTest::noPrematureSubscriptionToError, "noPrematureSubscriptionToError", this);
            this.payloads.noSubscriptionIfOtherErrors = _ClassStatement.forPayload(ObservableDelaySubscriptionOtherTest::noSubscriptionIfOtherErrors, "noSubscriptionIfOtherErrors", this);
            this.payloads.badSourceOther = _ClassStatement.forPayload(ObservableDelaySubscriptionOtherTest::badSourceOther, "badSourceOther", this);
            this.payloads.afterDelayNoInterrupt = _ClassStatement.forPayload(ObservableDelaySubscriptionOtherTest::afterDelayNoInterrupt, "afterDelayNoInterrupt", this);
        }
    }
}
