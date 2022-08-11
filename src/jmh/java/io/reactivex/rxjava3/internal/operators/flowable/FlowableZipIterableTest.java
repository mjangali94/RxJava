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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.*;
import org.mockito.InOrder;
import org.reactivestreams.Subscriber;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.internal.util.CrashingIterable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class FlowableZipIterableTest extends RxJavaTest {

    BiFunction<String, String, String> concat2Strings;

    PublishProcessor<String> s1;

    PublishProcessor<String> s2;

    Flowable<String> zipped;

    Subscriber<String> subscriber;

    InOrder inOrder;

    @Before
    public void setUp() {
        concat2Strings = new BiFunction<String, String, String>() {

            @Override
            public String apply(String t1, String t2) {
                return t1 + "-" + t2;
            }
        };
        s1 = PublishProcessor.create();
        s2 = PublishProcessor.create();
        zipped = Flowable.zip(s1, s2, concat2Strings);
        subscriber = TestHelper.mockSubscriber();
        inOrder = inOrder(subscriber);
        zipped.subscribe(subscriber);
    }

    BiFunction<Object, Object, String> zipr2 = new BiFunction<Object, Object, String>() {

        @Override
        public String apply(Object t1, Object t2) {
            return "" + t1 + t2;
        }
    };

    Function3<Object, Object, Object, String> zipr3 = new Function3<Object, Object, Object, String>() {

        @Override
        public String apply(Object t1, Object t2, Object t3) {
            return "" + t1 + t2 + t3;
        }
    };

    @Test
    public void zipIterableSameSize() {
        PublishProcessor<String> r1 = PublishProcessor.create();
        /* define a Subscriber to receive aggregated events */
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        InOrder io = inOrder(subscriber);
        Iterable<String> r2 = Arrays.asList("1", "2", "3");
        r1.zipWith(r2, zipr2).subscribe(subscriber);
        r1.onNext("one-");
        r1.onNext("two-");
        r1.onNext("three-");
        r1.onComplete();
        io.verify(subscriber).onNext("one-1");
        io.verify(subscriber).onNext("two-2");
        io.verify(subscriber).onNext("three-3");
        io.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void zipIterableEmptyFirstSize() {
        PublishProcessor<String> r1 = PublishProcessor.create();
        /* define a Subscriber to receive aggregated events */
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        InOrder io = inOrder(subscriber);
        Iterable<String> r2 = Arrays.asList("1", "2", "3");
        r1.zipWith(r2, zipr2).subscribe(subscriber);
        r1.onComplete();
        io.verify(subscriber).onComplete();
        verify(subscriber, never()).onNext(any(String.class));
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void zipIterableEmptySecond() {
        PublishProcessor<String> r1 = PublishProcessor.create();
        /* define a Subscriber to receive aggregated events */
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        InOrder io = inOrder(subscriber);
        Iterable<String> r2 = Arrays.asList();
        r1.zipWith(r2, zipr2).subscribe(subscriber);
        r1.onNext("one-");
        r1.onNext("two-");
        r1.onNext("three-");
        r1.onComplete();
        io.verify(subscriber).onComplete();
        verify(subscriber, never()).onNext(any(String.class));
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void zipIterableFirstShorter() {
        PublishProcessor<String> r1 = PublishProcessor.create();
        /* define a Subscriber to receive aggregated events */
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        InOrder io = inOrder(subscriber);
        Iterable<String> r2 = Arrays.asList("1", "2", "3");
        r1.zipWith(r2, zipr2).subscribe(subscriber);
        r1.onNext("one-");
        r1.onNext("two-");
        r1.onComplete();
        io.verify(subscriber).onNext("one-1");
        io.verify(subscriber).onNext("two-2");
        io.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void zipIterableSecondShorter() {
        PublishProcessor<String> r1 = PublishProcessor.create();
        /* define a Subscriber to receive aggregated events */
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        InOrder io = inOrder(subscriber);
        Iterable<String> r2 = Arrays.asList("1", "2");
        r1.zipWith(r2, zipr2).subscribe(subscriber);
        r1.onNext("one-");
        r1.onNext("two-");
        r1.onNext("three-");
        r1.onComplete();
        io.verify(subscriber).onNext("one-1");
        io.verify(subscriber).onNext("two-2");
        io.verify(subscriber).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void zipIterableFirstThrows() {
        PublishProcessor<String> r1 = PublishProcessor.create();
        /* define a Subscriber to receive aggregated events */
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        InOrder io = inOrder(subscriber);
        Iterable<String> r2 = Arrays.asList("1", "2", "3");
        r1.zipWith(r2, zipr2).subscribe(subscriber);
        r1.onNext("one-");
        r1.onNext("two-");
        r1.onError(new TestException());
        io.verify(subscriber).onNext("one-1");
        io.verify(subscriber).onNext("two-2");
        io.verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void zipIterableIteratorThrows() {
        PublishProcessor<String> r1 = PublishProcessor.create();
        /* define a Subscriber to receive aggregated events */
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        InOrder io = inOrder(subscriber);
        Iterable<String> r2 = new Iterable<String>() {

            @Override
            public Iterator<String> iterator() {
                throw new TestException();
            }
        };
        r1.zipWith(r2, zipr2).subscribe(subscriber);
        r1.onNext("one-");
        r1.onNext("two-");
        r1.onError(new TestException());
        io.verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any(String.class));
    }

    @Test
    public void zipIterableHasNextThrows() {
        PublishProcessor<String> r1 = PublishProcessor.create();
        /* define a Subscriber to receive aggregated events */
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        InOrder io = inOrder(subscriber);
        Iterable<String> r2 = new Iterable<String>() {

            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {

                    int count;

                    @Override
                    public boolean hasNext() {
                        if (count == 0) {
                            return true;
                        }
                        throw new TestException();
                    }

                    @Override
                    public String next() {
                        count++;
                        return "1";
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }
        };
        r1.zipWith(r2, zipr2).subscribe(subscriber);
        r1.onNext("one-");
        r1.onError(new TestException());
        io.verify(subscriber).onNext("one-1");
        io.verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void zipIterableNextThrows() {
        PublishProcessor<String> r1 = PublishProcessor.create();
        /* define a Subscriber to receive aggregated events */
        Subscriber<String> subscriber = TestHelper.mockSubscriber();
        InOrder io = inOrder(subscriber);
        Iterable<String> r2 = new Iterable<String>() {

            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {

                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public String next() {
                        throw new TestException();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }
        };
        r1.zipWith(r2, zipr2).subscribe(subscriber);
        r1.onError(new TestException());
        io.verify(subscriber).onError(any(TestException.class));
        verify(subscriber, never()).onNext(any(String.class));
        verify(subscriber, never()).onComplete();
    }

    Consumer<String> printer = new Consumer<String>() {

        @Override
        public void accept(String pv) {
            System.out.println(pv);
        }
    };

    static final class SquareStr implements Function<Integer, String> {

        final AtomicInteger counter = new AtomicInteger();

        @Override
        public String apply(Integer t1) {
            counter.incrementAndGet();
            System.out.println("Omg I'm calculating so hard: " + t1 + "*" + t1 + "=" + (t1 * t1));
            return " " + (t1 * t1);
        }
    }

    @Test
    public void take2() {
        Flowable<Integer> f = Flowable.just(1, 2, 3, 4, 5);
        Iterable<String> it = Arrays.asList("a", "b", "c", "d", "e");
        SquareStr squareStr = new SquareStr();
        f.map(squareStr).zipWith(it, concat2Strings).take(2).subscribe(printer);
        assertEquals(2, squareStr.counter.get());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).zipWith(Arrays.asList(1), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(new Function<Flowable<Integer>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Flowable<Integer> f) throws Exception {
                return f.zipWith(Arrays.asList(1), new BiFunction<Integer, Integer, Object>() {

                    @Override
                    public Object apply(Integer a, Integer b) throws Exception {
                        return a + b;
                    }
                });
            }
        });
    }

    @Test
    public void iteratorThrows() {
        Flowable.just(1).zipWith(new CrashingIterable(100, 1, 100), new BiFunction<Integer, Integer, Object>() {

            @Override
            public Object apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        }).to(TestHelper.<Object>testConsumer()).assertFailureAndMessage(TestException.class, "hasNext()");
    }

    @Test
    public void badSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Flowable<Integer>() {

                @Override
                protected void subscribeActual(Subscriber<? super Integer> subscriber) {
                    subscriber.onSubscribe(new BooleanSubscription());
                    subscriber.onNext(1);
                    subscriber.onComplete();
                    subscriber.onNext(2);
                    subscriber.onError(new TestException());
                    subscriber.onComplete();
                }
            }.zipWith(Arrays.asList(1), new BiFunction<Integer, Integer, Object>() {

                @Override
                public Object apply(Integer a, Integer b) throws Exception {
                    return a + b;
                }
            }).test().assertResult(2);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableZipIterableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableSameSize() throws java.lang.Throwable {
            this.payloads.zipIterableSameSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableEmptyFirstSize() throws java.lang.Throwable {
            this.payloads.zipIterableEmptyFirstSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableEmptySecond() throws java.lang.Throwable {
            this.payloads.zipIterableEmptySecond.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableFirstShorter() throws java.lang.Throwable {
            this.payloads.zipIterableFirstShorter.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableSecondShorter() throws java.lang.Throwable {
            this.payloads.zipIterableSecondShorter.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableFirstThrows() throws java.lang.Throwable {
            this.payloads.zipIterableFirstThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableIteratorThrows() throws java.lang.Throwable {
            this.payloads.zipIterableIteratorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableHasNextThrows() throws java.lang.Throwable {
            this.payloads.zipIterableHasNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableNextThrows() throws java.lang.Throwable {
            this.payloads.zipIterableNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_take2() throws java.lang.Throwable {
            this.payloads.take2.evaluate();
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
        public void benchmark_iteratorThrows() throws java.lang.Throwable {
            this.payloads.iteratorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipIterableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipIterableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.setUp();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipIterableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipIterableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableZipIterableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableZipIterableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableZipIterableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableZipIterableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement zipIterableSameSize;

            public org.junit.runners.model.Statement zipIterableEmptyFirstSize;

            public org.junit.runners.model.Statement zipIterableEmptySecond;

            public org.junit.runners.model.Statement zipIterableFirstShorter;

            public org.junit.runners.model.Statement zipIterableSecondShorter;

            public org.junit.runners.model.Statement zipIterableFirstThrows;

            public org.junit.runners.model.Statement zipIterableIteratorThrows;

            public org.junit.runners.model.Statement zipIterableHasNextThrows;

            public org.junit.runners.model.Statement zipIterableNextThrows;

            public org.junit.runners.model.Statement take2;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement iteratorThrows;

            public org.junit.runners.model.Statement badSource;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.zipIterableSameSize = _ClassStatement.forPayload(FlowableZipIterableTest::zipIterableSameSize, "zipIterableSameSize", this);
            this.payloads.zipIterableEmptyFirstSize = _ClassStatement.forPayload(FlowableZipIterableTest::zipIterableEmptyFirstSize, "zipIterableEmptyFirstSize", this);
            this.payloads.zipIterableEmptySecond = _ClassStatement.forPayload(FlowableZipIterableTest::zipIterableEmptySecond, "zipIterableEmptySecond", this);
            this.payloads.zipIterableFirstShorter = _ClassStatement.forPayload(FlowableZipIterableTest::zipIterableFirstShorter, "zipIterableFirstShorter", this);
            this.payloads.zipIterableSecondShorter = _ClassStatement.forPayload(FlowableZipIterableTest::zipIterableSecondShorter, "zipIterableSecondShorter", this);
            this.payloads.zipIterableFirstThrows = _ClassStatement.forPayload(FlowableZipIterableTest::zipIterableFirstThrows, "zipIterableFirstThrows", this);
            this.payloads.zipIterableIteratorThrows = _ClassStatement.forPayload(FlowableZipIterableTest::zipIterableIteratorThrows, "zipIterableIteratorThrows", this);
            this.payloads.zipIterableHasNextThrows = _ClassStatement.forPayload(FlowableZipIterableTest::zipIterableHasNextThrows, "zipIterableHasNextThrows", this);
            this.payloads.zipIterableNextThrows = _ClassStatement.forPayload(FlowableZipIterableTest::zipIterableNextThrows, "zipIterableNextThrows", this);
            this.payloads.take2 = _ClassStatement.forPayload(FlowableZipIterableTest::take2, "take2", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableZipIterableTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableZipIterableTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.iteratorThrows = _ClassStatement.forPayload(FlowableZipIterableTest::iteratorThrows, "iteratorThrows", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableZipIterableTest::badSource, "badSource", this);
        }
    }
}
