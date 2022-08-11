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

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.internal.operators.maybe.MaybeMergeArray.MergeMaybeObserver;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.QueueSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class MaybeMergeArrayTest extends RxJavaTest {

    @Test
    public void normal() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.SYNC);
        Maybe.mergeArray(Maybe.just(1), Maybe.just(2)).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.NONE).assertResult(1, 2);
    }

    @Test
    public void fusedPollMixed() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Maybe.mergeArray(Maybe.just(1), Maybe.<Integer>empty(), Maybe.just(2)).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fusedEmptyCheck() {
        Maybe.mergeArray(Maybe.just(1), Maybe.<Integer>empty(), Maybe.just(2)).subscribe(new FlowableSubscriber<Integer>() {

            QueueSubscription<Integer> qs;

            @Override
            public void onSubscribe(Subscription s) {
                qs = (QueueSubscription<Integer>) s;
                assertEquals(QueueFuseable.ASYNC, qs.requestFusion(QueueFuseable.ANY));
            }

            @Override
            public void onNext(Integer value) {
                assertFalse(qs.isEmpty());
                qs.clear();
                assertTrue(qs.isEmpty());
                qs.cancel();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void cancel() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        Maybe.mergeArray(Maybe.just(1), Maybe.<Integer>empty(), Maybe.just(2)).subscribe(ts);
        ts.cancel();
        ts.request(10);
        ts.assertEmpty();
    }

    @Test
    public void firstErrors() {
        TestSubscriber<Integer> ts = new TestSubscriber<>(0L);
        Maybe.mergeArray(Maybe.<Integer>error(new TestException()), Maybe.<Integer>empty(), Maybe.just(2)).subscribe(ts);
        ts.assertFailure(TestException.class);
    }

    @Test
    public void errorFused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Maybe.mergeArray(Maybe.<Integer>error(new TestException()), Maybe.just(2)).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertFailure(TestException.class);
    }

    @Test
    public void errorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final PublishSubject<Integer> ps1 = PublishSubject.create();
                final PublishSubject<Integer> ps2 = PublishSubject.create();
                TestSubscriber<Integer> ts = Maybe.mergeArray(ps1.singleElement(), ps2.singleElement()).test();
                final TestException ex = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps1.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ps2.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                ts.assertFailure(Throwable.class);
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void mergeBadSource() {
        Maybe.mergeArray(new Maybe<Integer>() {

            @Override
            protected void subscribeActual(MaybeObserver<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onSuccess(1);
                observer.onSuccess(2);
                observer.onSuccess(3);
            }
        }, Maybe.never()).test().assertResult(1, 2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void smallOffer2Throws() {
        Maybe.mergeArray(Maybe.never(), Maybe.never()).subscribe(new FlowableSubscriber<Object>() {

            @SuppressWarnings("rawtypes")
            @Override
            public void onSubscribe(Subscription s) {
                MergeMaybeObserver o = (MergeMaybeObserver) s;
                try {
                    o.queue.offer(1, 2);
                    fail("Should have thrown");
                } catch (UnsupportedOperationException ex) {
                // expected
                }
            }

            @Override
            public void onNext(Object t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void largeOffer2Throws() {
        Maybe<Integer>[] a = new Maybe[1024];
        Arrays.fill(a, Maybe.never());
        Maybe.mergeArray(a).subscribe(new FlowableSubscriber<Object>() {

            @SuppressWarnings("rawtypes")
            @Override
            public void onSubscribe(Subscription s) {
                MergeMaybeObserver o = (MergeMaybeObserver) s;
                try {
                    o.queue.offer(1, 2);
                    fail("Should have thrown");
                } catch (UnsupportedOperationException ex) {
                // expected
                }
                o.queue.drop();
            }

            @Override
            public void onNext(Object t) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Maybe.mergeArray(MaybeSubject.create(), MaybeSubject.create()));
    }

    @Test
    public void cancel2() {
        TestHelper.checkDisposed(Maybe.mergeArray(MaybeSubject.create(), MaybeSubject.create()));
    }

    @Test
    public void take() {
        Maybe.mergeArray(Maybe.just(1), Maybe.empty(), Maybe.just(2)).doOnSubscribe(s -> s.request(Long.MAX_VALUE)).take(1).test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeMergeArrayTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedPollMixed() throws java.lang.Throwable {
            this.payloads.fusedPollMixed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedEmptyCheck() throws java.lang.Throwable {
            this.payloads.fusedEmptyCheck.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel() throws java.lang.Throwable {
            this.payloads.cancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_firstErrors() throws java.lang.Throwable {
            this.payloads.firstErrors.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorFused() throws java.lang.Throwable {
            this.payloads.errorFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorRace() throws java.lang.Throwable {
            this.payloads.errorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeBadSource() throws java.lang.Throwable {
            this.payloads.mergeBadSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_smallOffer2Throws() throws java.lang.Throwable {
            this.payloads.smallOffer2Throws.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_largeOffer2Throws() throws java.lang.Throwable {
            this.payloads.largeOffer2Throws.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancel2() throws java.lang.Throwable {
            this.payloads.cancel2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take() throws java.lang.Throwable {
            this.payloads.take.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeMergeArrayTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeMergeArrayTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeMergeArrayTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeMergeArrayTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeMergeArrayTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeMergeArrayTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeMergeArrayTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeMergeArrayTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement fusedPollMixed;

            public org.junit.runners.model.Statement fusedEmptyCheck;

            public org.junit.runners.model.Statement cancel;

            public org.junit.runners.model.Statement firstErrors;

            public org.junit.runners.model.Statement errorFused;

            public org.junit.runners.model.Statement errorRace;

            public org.junit.runners.model.Statement mergeBadSource;

            public org.junit.runners.model.Statement smallOffer2Throws;

            public org.junit.runners.model.Statement largeOffer2Throws;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement cancel2;

            public org.junit.runners.model.Statement take;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normal = _ClassStatement.forPayload(MaybeMergeArrayTest::normal, "normal", this);
            this.payloads.fusedPollMixed = _ClassStatement.forPayload(MaybeMergeArrayTest::fusedPollMixed, "fusedPollMixed", this);
            this.payloads.fusedEmptyCheck = _ClassStatement.forPayload(MaybeMergeArrayTest::fusedEmptyCheck, "fusedEmptyCheck", this);
            this.payloads.cancel = _ClassStatement.forPayload(MaybeMergeArrayTest::cancel, "cancel", this);
            this.payloads.firstErrors = _ClassStatement.forPayload(MaybeMergeArrayTest::firstErrors, "firstErrors", this);
            this.payloads.errorFused = _ClassStatement.forPayload(MaybeMergeArrayTest::errorFused, "errorFused", this);
            this.payloads.errorRace = _ClassStatement.forPayload(MaybeMergeArrayTest::errorRace, "errorRace", this);
            this.payloads.mergeBadSource = _ClassStatement.forPayload(MaybeMergeArrayTest::mergeBadSource, "mergeBadSource", this);
            this.payloads.smallOffer2Throws = _ClassStatement.forPayload(MaybeMergeArrayTest::smallOffer2Throws, "smallOffer2Throws", this);
            this.payloads.largeOffer2Throws = _ClassStatement.forPayload(MaybeMergeArrayTest::largeOffer2Throws, "largeOffer2Throws", this);
            this.payloads.badRequest = _ClassStatement.forPayload(MaybeMergeArrayTest::badRequest, "badRequest", this);
            this.payloads.cancel2 = _ClassStatement.forPayload(MaybeMergeArrayTest::cancel2, "cancel2", this);
            this.payloads.take = _ClassStatement.forPayload(MaybeMergeArrayTest::take, "take", this);
        }
    }
}
