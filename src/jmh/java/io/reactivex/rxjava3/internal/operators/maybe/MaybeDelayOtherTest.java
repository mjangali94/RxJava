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
import java.util.List;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.*;

public class MaybeDelayOtherTest extends RxJavaTest {

    @Test
    public void justWithOnNext() {
        PublishProcessor<Object> pp = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.just(1).delay(pp).test();
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        assertFalse(pp.hasSubscribers());
        to.assertResult(1);
    }

    @Test
    public void justWithOnComplete() {
        PublishProcessor<Object> pp = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.just(1).delay(pp).test();
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        pp.onComplete();
        assertFalse(pp.hasSubscribers());
        to.assertResult(1);
    }

    @Test
    public void justWithOnError() {
        PublishProcessor<Object> pp = PublishProcessor.create();
        TestObserverEx<Integer> to = Maybe.just(1).delay(pp).to(TestHelper.<Integer>testConsumer());
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        pp.onError(new TestException("Other"));
        assertFalse(pp.hasSubscribers());
        to.assertFailureAndMessage(TestException.class, "Other");
    }

    @Test
    public void emptyWithOnNext() {
        PublishProcessor<Object> pp = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.<Integer>empty().delay(pp).test();
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        assertFalse(pp.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void emptyWithOnComplete() {
        PublishProcessor<Object> pp = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.<Integer>empty().delay(pp).test();
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        pp.onComplete();
        assertFalse(pp.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void emptyWithOnError() {
        PublishProcessor<Object> pp = PublishProcessor.create();
        TestObserverEx<Integer> to = Maybe.<Integer>empty().delay(pp).to(TestHelper.<Integer>testConsumer());
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        pp.onError(new TestException("Other"));
        assertFalse(pp.hasSubscribers());
        to.assertFailureAndMessage(TestException.class, "Other");
    }

    @Test
    public void errorWithOnNext() {
        PublishProcessor<Object> pp = PublishProcessor.create();
        TestObserverEx<Integer> to = Maybe.<Integer>error(new TestException("Main")).delay(pp).to(TestHelper.<Integer>testConsumer());
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        assertFalse(pp.hasSubscribers());
        to.assertFailureAndMessage(TestException.class, "Main");
    }

    @Test
    public void errorWithOnComplete() {
        PublishProcessor<Object> pp = PublishProcessor.create();
        TestObserverEx<Integer> to = Maybe.<Integer>error(new TestException("Main")).delay(pp).to(TestHelper.<Integer>testConsumer());
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        pp.onComplete();
        assertFalse(pp.hasSubscribers());
        to.assertFailureAndMessage(TestException.class, "Main");
    }

    @Test
    public void errorWithOnError() {
        PublishProcessor<Object> pp = PublishProcessor.create();
        TestObserverEx<Integer> to = Maybe.<Integer>error(new TestException("Main")).delay(pp).to(TestHelper.<Integer>testConsumer());
        to.assertEmpty();
        assertTrue(pp.hasSubscribers());
        pp.onError(new TestException("Other"));
        assertFalse(pp.hasSubscribers());
        to.assertFailure(CompositeException.class);
        List<Throwable> list = TestHelper.compositeList(to.errors().get(0));
        assertEquals(2, list.size());
        TestHelper.assertError(list, 0, TestException.class, "Main");
        TestHelper.assertError(list, 1, TestException.class, "Other");
    }

    @Test
    public void withCompletableDispose() {
        TestHelper.checkDisposed(Completable.complete().andThen(Maybe.just(1)));
    }

    @Test
    public void withCompletableDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeCompletableToMaybe(new Function<Completable, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Completable c) throws Exception {
                return c.andThen(Maybe.just(1));
            }
        });
    }

    @Test
    public void withOtherPublisherDispose() {
        TestHelper.checkDisposed(Maybe.just(1).delay(Flowable.just(1)));
    }

    @Test
    public void withOtherPublisherDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Integer>, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Maybe<Integer> c) throws Exception {
                return c.delay(Flowable.never());
            }
        });
    }

    @Test
    public void otherPublisherNextSlipsThrough() {
        Maybe.just(1).delay(new Flowable<Integer>() {

            @Override
            protected void subscribeActual(Subscriber<? super Integer> s) {
                s.onSubscribe(new BooleanSubscription());
                s.onNext(1);
                s.onNext(2);
            }
        }).test().assertResult(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public MaybeDelayOtherTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justWithOnNext() throws java.lang.Throwable {
            this.payloads.justWithOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justWithOnComplete() throws java.lang.Throwable {
            this.payloads.justWithOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_justWithOnError() throws java.lang.Throwable {
            this.payloads.justWithOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyWithOnNext() throws java.lang.Throwable {
            this.payloads.emptyWithOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyWithOnComplete() throws java.lang.Throwable {
            this.payloads.emptyWithOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyWithOnError() throws java.lang.Throwable {
            this.payloads.emptyWithOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorWithOnNext() throws java.lang.Throwable {
            this.payloads.errorWithOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorWithOnComplete() throws java.lang.Throwable {
            this.payloads.errorWithOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorWithOnError() throws java.lang.Throwable {
            this.payloads.errorWithOnError.evaluate();
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
        public void benchmark_withOtherPublisherDispose() throws java.lang.Throwable {
            this.payloads.withOtherPublisherDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_withOtherPublisherDoubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.withOtherPublisherDoubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_otherPublisherNextSlipsThrough() throws java.lang.Throwable {
            this.payloads.otherPublisherNextSlipsThrough.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDelayOtherTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDelayOtherTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDelayOtherTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDelayOtherTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeDelayOtherTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeDelayOtherTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeDelayOtherTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeDelayOtherTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement justWithOnNext;

            public org.junit.runners.model.Statement justWithOnComplete;

            public org.junit.runners.model.Statement justWithOnError;

            public org.junit.runners.model.Statement emptyWithOnNext;

            public org.junit.runners.model.Statement emptyWithOnComplete;

            public org.junit.runners.model.Statement emptyWithOnError;

            public org.junit.runners.model.Statement errorWithOnNext;

            public org.junit.runners.model.Statement errorWithOnComplete;

            public org.junit.runners.model.Statement errorWithOnError;

            public org.junit.runners.model.Statement withCompletableDispose;

            public org.junit.runners.model.Statement withCompletableDoubleOnSubscribe;

            public org.junit.runners.model.Statement withOtherPublisherDispose;

            public org.junit.runners.model.Statement withOtherPublisherDoubleOnSubscribe;

            public org.junit.runners.model.Statement otherPublisherNextSlipsThrough;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.justWithOnNext = _ClassStatement.forPayload(MaybeDelayOtherTest::justWithOnNext, "justWithOnNext", this);
            this.payloads.justWithOnComplete = _ClassStatement.forPayload(MaybeDelayOtherTest::justWithOnComplete, "justWithOnComplete", this);
            this.payloads.justWithOnError = _ClassStatement.forPayload(MaybeDelayOtherTest::justWithOnError, "justWithOnError", this);
            this.payloads.emptyWithOnNext = _ClassStatement.forPayload(MaybeDelayOtherTest::emptyWithOnNext, "emptyWithOnNext", this);
            this.payloads.emptyWithOnComplete = _ClassStatement.forPayload(MaybeDelayOtherTest::emptyWithOnComplete, "emptyWithOnComplete", this);
            this.payloads.emptyWithOnError = _ClassStatement.forPayload(MaybeDelayOtherTest::emptyWithOnError, "emptyWithOnError", this);
            this.payloads.errorWithOnNext = _ClassStatement.forPayload(MaybeDelayOtherTest::errorWithOnNext, "errorWithOnNext", this);
            this.payloads.errorWithOnComplete = _ClassStatement.forPayload(MaybeDelayOtherTest::errorWithOnComplete, "errorWithOnComplete", this);
            this.payloads.errorWithOnError = _ClassStatement.forPayload(MaybeDelayOtherTest::errorWithOnError, "errorWithOnError", this);
            this.payloads.withCompletableDispose = _ClassStatement.forPayload(MaybeDelayOtherTest::withCompletableDispose, "withCompletableDispose", this);
            this.payloads.withCompletableDoubleOnSubscribe = _ClassStatement.forPayload(MaybeDelayOtherTest::withCompletableDoubleOnSubscribe, "withCompletableDoubleOnSubscribe", this);
            this.payloads.withOtherPublisherDispose = _ClassStatement.forPayload(MaybeDelayOtherTest::withOtherPublisherDispose, "withOtherPublisherDispose", this);
            this.payloads.withOtherPublisherDoubleOnSubscribe = _ClassStatement.forPayload(MaybeDelayOtherTest::withOtherPublisherDoubleOnSubscribe, "withOtherPublisherDoubleOnSubscribe", this);
            this.payloads.otherPublisherNextSlipsThrough = _ClassStatement.forPayload(MaybeDelayOtherTest::otherPublisherNextSlipsThrough, "otherPublisherNextSlipsThrough", this);
        }
    }
}
