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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableDistinctUntilChangedTest extends RxJavaTest {

    Subscriber<String> w;

    Subscriber<String> w2;

    // nulls lead to exceptions
    final Function<String, String> TO_UPPER_WITH_EXCEPTION = new Function<String, String>() {

        @Override
        public String apply(String s) {
            if (s.equals("x")) {
                return "xx";
            }
            return s.toUpperCase();
        }
    };

    @Before
    public void before() {
        w = TestHelper.mockSubscriber();
        w2 = TestHelper.mockSubscriber();
    }

    @Test
    public void distinctUntilChangedOfNone() {
        Flowable<String> src = Flowable.empty();
        src.distinctUntilChanged().subscribe(w);
        verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void distinctUntilChangedOfNoneWithKeySelector() {
        Flowable<String> src = Flowable.empty();
        src.distinctUntilChanged(TO_UPPER_WITH_EXCEPTION).subscribe(w);
        verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void distinctUntilChangedOfNormalSource() {
        Flowable<String> src = Flowable.just("a", "b", "c", "c", "c", "b", "b", "a", "e");
        src.distinctUntilChanged().subscribe(w);
        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("b");
        inOrder.verify(w, times(1)).onNext("c");
        inOrder.verify(w, times(1)).onNext("b");
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("e");
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void distinctUntilChangedOfNormalSourceWithKeySelector() {
        Flowable<String> src = Flowable.just("a", "b", "c", "C", "c", "B", "b", "a", "e");
        src.distinctUntilChanged(TO_UPPER_WITH_EXCEPTION).subscribe(w);
        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("b");
        inOrder.verify(w, times(1)).onNext("c");
        inOrder.verify(w, times(1)).onNext("B");
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("e");
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void directComparer() {
        Flowable.fromArray(1, 2, 2, 3, 2, 4, 1, 1, 2).distinctUntilChanged(new BiPredicate<Integer, Integer>() {

            @Override
            public boolean test(Integer a, Integer b) {
                return a.equals(b);
            }
        }).test().assertResult(1, 2, 3, 2, 4, 1, 2);
    }

    @Test
    public void directComparerConditional() {
        Flowable.fromArray(1, 2, 2, 3, 2, 4, 1, 1, 2).distinctUntilChanged(new BiPredicate<Integer, Integer>() {

            @Override
            public boolean test(Integer a, Integer b) {
                return a.equals(b);
            }
        }).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).test().assertResult(1, 2, 3, 2, 4, 1, 2);
    }

    @Test
    public void directComparerFused() {
        Flowable.fromArray(1, 2, 2, 3, 2, 4, 1, 1, 2).distinctUntilChanged(new BiPredicate<Integer, Integer>() {

            @Override
            public boolean test(Integer a, Integer b) {
                return a.equals(b);
            }
        }).to(TestHelper.<Integer>testSubscriber(Long.MAX_VALUE, QueueFuseable.ANY, false)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 2, 4, 1, 2);
    }

    @Test
    public void directComparerConditionalFused() {
        Flowable.fromArray(1, 2, 2, 3, 2, 4, 1, 1, 2).distinctUntilChanged(new BiPredicate<Integer, Integer>() {

            @Override
            public boolean test(Integer a, Integer b) {
                return a.equals(b);
            }
        }).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) {
                return true;
            }
        }).to(TestHelper.<Integer>testSubscriber(Long.MAX_VALUE, QueueFuseable.ANY, false)).assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 2, 4, 1, 2);
    }

    private static final Function<String, String> THROWS_NON_FATAL = new Function<String, String>() {

        @Override
        public String apply(String s) {
            throw new RuntimeException();
        }
    };

    @Test
    public void distinctUntilChangedWhenNonFatalExceptionThrownByKeySelectorIsNotReportedByUpstream() {
        Flowable<String> src = Flowable.just("a", "b", "null", "c");
        final AtomicBoolean errorOccurred = new AtomicBoolean(false);
        src.doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable t) {
                errorOccurred.set(true);
            }
        }).distinctUntilChanged(THROWS_NON_FATAL).subscribe(w);
        Assert.assertFalse(errorOccurred.get());
    }

    @Test
    public void customComparator() {
        Flowable<String> source = Flowable.just("a", "b", "B", "A", "a", "C");
        TestSubscriber<String> ts = TestSubscriber.create();
        source.distinctUntilChanged(new BiPredicate<String, String>() {

            @Override
            public boolean test(String a, String b) {
                return a.compareToIgnoreCase(b) == 0;
            }
        }).subscribe(ts);
        ts.assertValues("a", "b", "A", "C");
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void customComparatorThrows() {
        Flowable<String> source = Flowable.just("a", "b", "B", "A", "a", "C");
        TestSubscriber<String> ts = TestSubscriber.create();
        source.distinctUntilChanged(new BiPredicate<String, String>() {

            @Override
            public boolean test(String a, String b) {
                throw new TestException();
            }
        }).subscribe(ts);
        ts.assertValue("a");
        ts.assertNotComplete();
        ts.assertError(TestException.class);
    }

    @Test
    public void fused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.just(1, 2, 2, 3, 3, 4, 5).distinctUntilChanged(new BiPredicate<Integer, Integer>() {

            @Override
            public boolean test(Integer a, Integer b) throws Exception {
                return a.equals(b);
            }
        }).subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.SYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void fusedAsync() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        up.distinctUntilChanged(new BiPredicate<Integer, Integer>() {

            @Override
            public boolean test(Integer a, Integer b) throws Exception {
                return a.equals(b);
            }
        }).subscribe(ts);
        TestHelper.emit(up, 1, 2, 2, 3, 3, 4, 5);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void ignoreCancel() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                public void subscribeActual(Subscriber<? super Integer> s) {
                    s.onSubscribe(new BooleanSubscription());
                    s.onNext(1);
                    s.onNext(2);
                    s.onNext(3);
                    s.onError(new IOException());
                    s.onComplete();
                }
            }.distinctUntilChanged(new BiPredicate<Integer, Integer>() {

                @Override
                public boolean test(Integer a, Integer b) throws Exception {
                    throw new TestException();
                }
            }).test().assertFailure(TestException.class, 1);
            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    class Mutable {

        int value;
    }

    @Test
    public void mutableWithSelector() {
        Mutable m = new Mutable();
        PublishProcessor<Mutable> pp = PublishProcessor.create();
        TestSubscriber<Mutable> ts = pp.distinctUntilChanged(new Function<Mutable, Object>() {

            @Override
            public Object apply(Mutable m) throws Exception {
                return m.value;
            }
        }).test();
        pp.onNext(m);
        m.value = 1;
        pp.onNext(m);
        pp.onComplete();
        ts.assertResult(m, m);
    }

    @Test
    public void conditionalNormal() {
        Flowable.just(1, 2, 1, 3, 3, 4, 3, 5, 5).distinctUntilChanged().filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).test().assertResult(2, 4);
    }

    @Test
    public void conditionalNormal2() {
        Flowable.just(1, 2, 1, 3, 3, 4, 3, 5, 5).hide().distinctUntilChanged().filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).test().assertResult(2, 4);
    }

    @Test
    public void conditionalNormal3() {
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        TestSubscriber<Integer> ts = up.hide().distinctUntilChanged().filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).test();
        TestHelper.emit(up, 1, 2, 1, 3, 3, 4, 3, 5, 5);
        ts.assertResult(2, 4);
    }

    @Test
    public void conditionalSelectorCrash() {
        Flowable.just(1, 2, 1, 3, 3, 4, 3, 5, 5).distinctUntilChanged(new BiPredicate<Integer, Integer>() {

            @Override
            public boolean test(Integer a, Integer b) throws Exception {
                throw new TestException();
            }
        }).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void conditionalFused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.just(1, 2, 1, 3, 3, 4, 3, 5, 5).distinctUntilChanged().filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).subscribe(ts);
        ts.assertFusionMode(QueueFuseable.SYNC).assertResult(2, 4);
    }

    @Test
    public void conditionalAsyncFused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        UnicastProcessor<Integer> up = UnicastProcessor.create();
        up.distinctUntilChanged().filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v % 2 == 0;
            }
        }).subscribe(ts);
        TestHelper.emit(up, 1, 2, 1, 3, 3, 4, 3, 5, 5);
        ts.assertFusionMode(QueueFuseable.ASYNC).assertResult(2, 4);
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.distinctUntilChanged().filter(Functions.alwaysTrue());
            }
        }, false, 1, 1, 1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableDistinctUntilChangedTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctUntilChangedOfNone() throws java.lang.Throwable {
            this.payloads.distinctUntilChangedOfNone.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctUntilChangedOfNoneWithKeySelector() throws java.lang.Throwable {
            this.payloads.distinctUntilChangedOfNoneWithKeySelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctUntilChangedOfNormalSource() throws java.lang.Throwable {
            this.payloads.distinctUntilChangedOfNormalSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctUntilChangedOfNormalSourceWithKeySelector() throws java.lang.Throwable {
            this.payloads.distinctUntilChangedOfNormalSourceWithKeySelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_directComparer() throws java.lang.Throwable {
            this.payloads.directComparer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_directComparerConditional() throws java.lang.Throwable {
            this.payloads.directComparerConditional.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_directComparerFused() throws java.lang.Throwable {
            this.payloads.directComparerFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_directComparerConditionalFused() throws java.lang.Throwable {
            this.payloads.directComparerConditionalFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_distinctUntilChangedWhenNonFatalExceptionThrownByKeySelectorIsNotReportedByUpstream() throws java.lang.Throwable {
            this.payloads.distinctUntilChangedWhenNonFatalExceptionThrownByKeySelectorIsNotReportedByUpstream.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customComparator() throws java.lang.Throwable {
            this.payloads.customComparator.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_customComparatorThrows() throws java.lang.Throwable {
            this.payloads.customComparatorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.payloads.fused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedAsync() throws java.lang.Throwable {
            this.payloads.fusedAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreCancel() throws java.lang.Throwable {
            this.payloads.ignoreCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mutableWithSelector() throws java.lang.Throwable {
            this.payloads.mutableWithSelector.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalNormal() throws java.lang.Throwable {
            this.payloads.conditionalNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalNormal2() throws java.lang.Throwable {
            this.payloads.conditionalNormal2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalNormal3() throws java.lang.Throwable {
            this.payloads.conditionalNormal3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalSelectorCrash() throws java.lang.Throwable {
            this.payloads.conditionalSelectorCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalFused() throws java.lang.Throwable {
            this.payloads.conditionalFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_conditionalAsyncFused() throws java.lang.Throwable {
            this.payloads.conditionalAsyncFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDistinctUntilChangedTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDistinctUntilChangedTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDistinctUntilChangedTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDistinctUntilChangedTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableDistinctUntilChangedTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableDistinctUntilChangedTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableDistinctUntilChangedTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableDistinctUntilChangedTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement distinctUntilChangedOfNone;

            public org.junit.runners.model.Statement distinctUntilChangedOfNoneWithKeySelector;

            public org.junit.runners.model.Statement distinctUntilChangedOfNormalSource;

            public org.junit.runners.model.Statement distinctUntilChangedOfNormalSourceWithKeySelector;

            public org.junit.runners.model.Statement directComparer;

            public org.junit.runners.model.Statement directComparerConditional;

            public org.junit.runners.model.Statement directComparerFused;

            public org.junit.runners.model.Statement directComparerConditionalFused;

            public org.junit.runners.model.Statement distinctUntilChangedWhenNonFatalExceptionThrownByKeySelectorIsNotReportedByUpstream;

            public org.junit.runners.model.Statement customComparator;

            public org.junit.runners.model.Statement customComparatorThrows;

            public org.junit.runners.model.Statement fused;

            public org.junit.runners.model.Statement fusedAsync;

            public org.junit.runners.model.Statement ignoreCancel;

            public org.junit.runners.model.Statement mutableWithSelector;

            public org.junit.runners.model.Statement conditionalNormal;

            public org.junit.runners.model.Statement conditionalNormal2;

            public org.junit.runners.model.Statement conditionalNormal3;

            public org.junit.runners.model.Statement conditionalSelectorCrash;

            public org.junit.runners.model.Statement conditionalFused;

            public org.junit.runners.model.Statement conditionalAsyncFused;

            public org.junit.runners.model.Statement badSource;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.distinctUntilChangedOfNone = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::distinctUntilChangedOfNone, "distinctUntilChangedOfNone", this);
            this.payloads.distinctUntilChangedOfNoneWithKeySelector = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::distinctUntilChangedOfNoneWithKeySelector, "distinctUntilChangedOfNoneWithKeySelector", this);
            this.payloads.distinctUntilChangedOfNormalSource = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::distinctUntilChangedOfNormalSource, "distinctUntilChangedOfNormalSource", this);
            this.payloads.distinctUntilChangedOfNormalSourceWithKeySelector = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::distinctUntilChangedOfNormalSourceWithKeySelector, "distinctUntilChangedOfNormalSourceWithKeySelector", this);
            this.payloads.directComparer = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::directComparer, "directComparer", this);
            this.payloads.directComparerConditional = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::directComparerConditional, "directComparerConditional", this);
            this.payloads.directComparerFused = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::directComparerFused, "directComparerFused", this);
            this.payloads.directComparerConditionalFused = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::directComparerConditionalFused, "directComparerConditionalFused", this);
            this.payloads.distinctUntilChangedWhenNonFatalExceptionThrownByKeySelectorIsNotReportedByUpstream = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::distinctUntilChangedWhenNonFatalExceptionThrownByKeySelectorIsNotReportedByUpstream, "distinctUntilChangedWhenNonFatalExceptionThrownByKeySelectorIsNotReportedByUpstream", this);
            this.payloads.customComparator = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::customComparator, "customComparator", this);
            this.payloads.customComparatorThrows = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::customComparatorThrows, "customComparatorThrows", this);
            this.payloads.fused = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::fused, "fused", this);
            this.payloads.fusedAsync = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::fusedAsync, "fusedAsync", this);
            this.payloads.ignoreCancel = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::ignoreCancel, "ignoreCancel", this);
            this.payloads.mutableWithSelector = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::mutableWithSelector, "mutableWithSelector", this);
            this.payloads.conditionalNormal = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::conditionalNormal, "conditionalNormal", this);
            this.payloads.conditionalNormal2 = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::conditionalNormal2, "conditionalNormal2", this);
            this.payloads.conditionalNormal3 = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::conditionalNormal3, "conditionalNormal3", this);
            this.payloads.conditionalSelectorCrash = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::conditionalSelectorCrash, "conditionalSelectorCrash", this);
            this.payloads.conditionalFused = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::conditionalFused, "conditionalFused", this);
            this.payloads.conditionalAsyncFused = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::conditionalAsyncFused, "conditionalAsyncFused", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableDistinctUntilChangedTest::badSource, "badSource", this);
        }
    }
}
