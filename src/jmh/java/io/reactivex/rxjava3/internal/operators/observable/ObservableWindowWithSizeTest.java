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

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableWindowWithSizeTest extends RxJavaTest {

    private static <T> List<List<T>> toLists(Observable<Observable<T>> observables) {
        final List<List<T>> lists = new ArrayList<>();
        Observable.concatEager(observables.map(new Function<Observable<T>, Observable<List<T>>>() {

            @Override
            public Observable<List<T>> apply(Observable<T> xs) {
                return xs.toList().toObservable();
            }
        })).blockingForEach(new Consumer<List<T>>() {

            @Override
            public void accept(List<T> xs) {
                lists.add(xs);
            }
        });
        return lists;
    }

    @Test
    public void nonOverlappingWindows() {
        Observable<String> subject = Observable.just("one", "two", "three", "four", "five");
        Observable<Observable<String>> windowed = subject.window(3);
        List<List<String>> windows = toLists(windowed);
        assertEquals(2, windows.size());
        assertEquals(list("one", "two", "three"), windows.get(0));
        assertEquals(list("four", "five"), windows.get(1));
    }

    @Test
    public void skipAndCountGaplessWindows() {
        Observable<String> subject = Observable.just("one", "two", "three", "four", "five");
        Observable<Observable<String>> windowed = subject.window(3, 3);
        List<List<String>> windows = toLists(windowed);
        assertEquals(2, windows.size());
        assertEquals(list("one", "two", "three"), windows.get(0));
        assertEquals(list("four", "five"), windows.get(1));
    }

    @Test
    public void overlappingWindows() {
        Observable<String> subject = Observable.fromArray(new String[] { "zero", "one", "two", "three", "four", "five" });
        Observable<Observable<String>> windowed = subject.window(3, 1);
        List<List<String>> windows = toLists(windowed);
        assertEquals(6, windows.size());
        assertEquals(list("zero", "one", "two"), windows.get(0));
        assertEquals(list("one", "two", "three"), windows.get(1));
        assertEquals(list("two", "three", "four"), windows.get(2));
        assertEquals(list("three", "four", "five"), windows.get(3));
        assertEquals(list("four", "five"), windows.get(4));
        assertEquals(list("five"), windows.get(5));
    }

    @Test
    public void skipAndCountWindowsWithGaps() {
        Observable<String> subject = Observable.just("one", "two", "three", "four", "five");
        Observable<Observable<String>> windowed = subject.window(2, 3);
        List<List<String>> windows = toLists(windowed);
        assertEquals(2, windows.size());
        assertEquals(list("one", "two"), windows.get(0));
        assertEquals(list("four", "five"), windows.get(1));
    }

    @Test
    public void windowUnsubscribeNonOverlapping() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        final AtomicInteger count = new AtomicInteger();
        Observable.merge(Observable.range(1, 10000).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                count.incrementAndGet();
            }
        }).window(5).take(2)).subscribe(to);
        to.awaitDone(500, TimeUnit.MILLISECONDS);
        to.assertTerminated();
        to.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        // // System.out.println(ts.getOnNextEvents());
        assertEquals(10, count.get());
    }

    @Test
    public void windowUnsubscribeNonOverlappingAsyncSource() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        final AtomicInteger count = new AtomicInteger();
        Observable.merge(Observable.range(1, 100000).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                if (count.incrementAndGet() == 500000) {
                    // give it a small break halfway through
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                    // ignored
                    }
                }
            }
        }).observeOn(Schedulers.computation()).window(5).take(2)).subscribe(to);
        to.awaitDone(500, TimeUnit.MILLISECONDS);
        to.assertTerminated();
        to.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        // make sure we don't emit all values ... the unsubscribe should propagate
        assertTrue(count.get() < 100000);
    }

    @Test
    public void windowUnsubscribeOverlapping() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        final AtomicInteger count = new AtomicInteger();
        Observable.merge(Observable.range(1, 10000).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                count.incrementAndGet();
            }
        }).window(5, 4).take(2)).subscribe(to);
        to.awaitDone(500, TimeUnit.MILLISECONDS);
        to.assertTerminated();
        // // System.out.println(ts.getOnNextEvents());
        to.assertValues(1, 2, 3, 4, 5, 5, 6, 7, 8, 9);
        assertEquals(9, count.get());
    }

    @Test
    public void windowUnsubscribeOverlappingAsyncSource() {
        TestObserverEx<Integer> to = new TestObserverEx<>();
        final AtomicInteger count = new AtomicInteger();
        Observable.merge(Observable.range(1, 100000).doOnNext(new Consumer<Integer>() {

            @Override
            public void accept(Integer t1) {
                count.incrementAndGet();
            }
        }).observeOn(Schedulers.computation()).window(5, 4).take(2), 128).subscribe(to);
        to.awaitDone(500, TimeUnit.MILLISECONDS);
        to.assertTerminated();
        to.assertValues(1, 2, 3, 4, 5, 5, 6, 7, 8, 9);
    // make sure we don't emit all values ... the unsubscribe should propagate
    // assertTrue(count.get() < 100000); // disabled: a small hiccup in the consumption may allow the source to run to completion
    }

    private List<String> list(String... args) {
        List<String> list = new ArrayList<>();
        for (String arg : args) {
            list.add(arg);
        }
        return list;
    }

    public static Observable<Integer> hotStream() {
        return Observable.unsafeCreate(new ObservableSource<Integer>() {

            @Override
            public void subscribe(Observer<? super Integer> observer) {
                Disposable d = Disposable.empty();
                observer.onSubscribe(d);
                while (!d.isDisposed()) {
                    // burst some number of items
                    for (int i = 0; i < Math.random() * 20; i++) {
                        observer.onNext(i);
                    }
                    try {
                        // sleep for a random amount of time
                        // NOTE: Only using Thread.sleep here as an artificial demo.
                        Thread.sleep((long) (Math.random() * 200));
                    } catch (Exception e) {
                    // do nothing
                    }
                }
                // System.out.println("Hot done.");
            }
        }).subscribeOn(// use newThread since we are using sleep to block
        Schedulers.newThread());
    }

    @Test
    public void takeFlatMapCompletes() {
        TestObserver<Integer> to = new TestObserver<>();
        final int indicator = 999999999;
        hotStream().window(10).take(2).flatMap(new Function<Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> w) {
                return w.startWithItem(indicator);
            }
        }).subscribe(to);
        to.awaitDone(2, TimeUnit.SECONDS);
        to.assertComplete();
        to.assertValueCount(22);
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(PublishSubject.create().window(1));
        TestHelper.checkDisposed(PublishSubject.create().window(2, 1));
        TestHelper.checkDisposed(PublishSubject.create().window(1, 2));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Observable<Object>>>() {

            @Override
            public ObservableSource<Observable<Object>> apply(Observable<Object> o) throws Exception {
                return o.window(1);
            }
        });
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Observable<Object>>>() {

            @Override
            public ObservableSource<Observable<Object>> apply(Observable<Object> o) throws Exception {
                return o.window(2, 1);
            }
        });
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, ObservableSource<Observable<Object>>>() {

            @Override
            public ObservableSource<Observable<Object>> apply(Observable<Object> o) throws Exception {
                return o.window(1, 2);
            }
        });
    }

    @Test
    public void errorExact() {
        Observable.error(new TestException()).window(1).test().assertFailure(TestException.class);
    }

    @Test
    public void errorSkip() {
        Observable.error(new TestException()).window(1, 2).test().assertFailure(TestException.class);
    }

    @Test
    public void errorOverlap() {
        Observable.error(new TestException()).window(2, 1).test().assertFailure(TestException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void errorExactInner() {
        @SuppressWarnings("rawtypes")
        final TestObserver[] to = { null };
        Observable.just(1).concatWith(Observable.<Integer>error(new TestException())).window(2).doOnNext(new Consumer<Observable<Integer>>() {

            @Override
            public void accept(Observable<Integer> w) throws Exception {
                to[0] = w.test();
            }
        }).test().assertError(TestException.class);
        to[0].assertFailure(TestException.class, 1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void errorSkipInner() {
        @SuppressWarnings("rawtypes")
        final TestObserver[] to = { null };
        Observable.just(1).concatWith(Observable.<Integer>error(new TestException())).window(2, 3).doOnNext(new Consumer<Observable<Integer>>() {

            @Override
            public void accept(Observable<Integer> w) throws Exception {
                to[0] = w.test();
            }
        }).test().assertError(TestException.class);
        to[0].assertFailure(TestException.class, 1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void errorOverlapInner() {
        @SuppressWarnings("rawtypes")
        final TestObserver[] to = { null };
        Observable.just(1).concatWith(Observable.<Integer>error(new TestException())).window(3, 2).doOnNext(new Consumer<Observable<Integer>>() {

            @Override
            public void accept(Observable<Integer> w) throws Exception {
                to[0] = w.test();
            }
        }).test().assertError(TestException.class);
        to[0].assertFailure(TestException.class, 1);
    }

    @Test
    public void cancellingWindowCancelsUpstreamSize() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.window(10).take(1).flatMap(new Function<Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> w) throws Throwable {
                return w.take(1);
            }
        }).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertResult(1);
        assertFalse("Subject still has observers!", ps.hasObservers());
    }

    @Test
    public void windowAbandonmentCancelsUpstreamSize() {
        PublishSubject<Integer> ps = PublishSubject.create();
        final AtomicReference<Observable<Integer>> inner = new AtomicReference<>();
        TestObserver<Observable<Integer>> to = ps.window(10).take(1).doOnNext(new Consumer<Observable<Integer>>() {

            @Override
            public void accept(Observable<Integer> v) throws Throwable {
                inner.set(v);
            }
        }).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertValueCount(1).assertNoErrors().assertComplete();
        assertFalse("Subject still has observers!", ps.hasObservers());
        inner.get().test().assertResult(1);
    }

    @Test
    public void cancellingWindowCancelsUpstreamSkip() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.window(5, 10).take(1).flatMap(new Function<Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> w) throws Throwable {
                return w.take(1);
            }
        }).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertResult(1);
        assertFalse("Subject still has observers!", ps.hasObservers());
    }

    @Test
    public void windowAbandonmentCancelsUpstreamSkip() {
        PublishSubject<Integer> ps = PublishSubject.create();
        final AtomicReference<Observable<Integer>> inner = new AtomicReference<>();
        TestObserver<Observable<Integer>> to = ps.window(5, 10).take(1).doOnNext(new Consumer<Observable<Integer>>() {

            @Override
            public void accept(Observable<Integer> v) throws Throwable {
                inner.set(v);
            }
        }).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertValueCount(1).assertNoErrors().assertComplete();
        assertFalse("Subject still has observers!", ps.hasObservers());
        inner.get().test().assertResult(1);
    }

    @Test
    public void cancellingWindowCancelsUpstreamOverlap() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.window(5, 3).take(1).flatMap(new Function<Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Observable<Integer> w) throws Throwable {
                return w.take(1);
            }
        }).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertResult(1);
        assertFalse("Subject still has observers!", ps.hasObservers());
    }

    @Test
    public void windowAbandonmentCancelsUpstreamOverlap() {
        PublishSubject<Integer> ps = PublishSubject.create();
        final AtomicReference<Observable<Integer>> inner = new AtomicReference<>();
        TestObserver<Observable<Integer>> to = ps.window(5, 3).take(1).doOnNext(new Consumer<Observable<Integer>>() {

            @Override
            public void accept(Observable<Integer> v) throws Throwable {
                inner.set(v);
            }
        }).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.assertValueCount(1).assertNoErrors().assertComplete();
        assertFalse("Subject still has observers!", ps.hasObservers());
        inner.get().test().assertResult(1);
    }

    @Test
    public void cancelWithoutWindowSize() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Observable<Integer>> to = ps.window(10).test();
        assertTrue(ps.hasObservers());
        to.dispose();
        assertFalse("Subject still has observers!", ps.hasObservers());
    }

    @Test
    public void cancelAfterAbandonmentSize() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Observable<Integer>> to = ps.window(10).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.dispose();
        assertFalse("Subject still has observers!", ps.hasObservers());
    }

    @Test
    public void cancelWithoutWindowSkip() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Observable<Integer>> to = ps.window(10, 15).test();
        assertTrue(ps.hasObservers());
        to.dispose();
        assertFalse("Subject still has observers!", ps.hasObservers());
    }

    @Test
    public void cancelAfterAbandonmentSkip() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Observable<Integer>> to = ps.window(10, 15).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.dispose();
        assertFalse("Subject still has observers!", ps.hasObservers());
    }

    @Test
    public void cancelWithoutWindowOverlap() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Observable<Integer>> to = ps.window(10, 5).test();
        assertTrue(ps.hasObservers());
        to.dispose();
        assertFalse("Subject still has observers!", ps.hasObservers());
    }

    @Test
    public void cancelAfterAbandonmentOverlap() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Observable<Integer>> to = ps.window(10, 5).test();
        assertTrue(ps.hasObservers());
        ps.onNext(1);
        to.dispose();
        assertFalse("Subject still has observers!", ps.hasObservers());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableWindowWithSizeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nonOverlappingWindows() throws java.lang.Throwable {
            this.payloads.nonOverlappingWindows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipAndCountGaplessWindows() throws java.lang.Throwable {
            this.payloads.skipAndCountGaplessWindows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_overlappingWindows() throws java.lang.Throwable {
            this.payloads.overlappingWindows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipAndCountWindowsWithGaps() throws java.lang.Throwable {
            this.payloads.skipAndCountWindowsWithGaps.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowUnsubscribeNonOverlapping() throws java.lang.Throwable {
            this.payloads.windowUnsubscribeNonOverlapping.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowUnsubscribeNonOverlappingAsyncSource() throws java.lang.Throwable {
            this.payloads.windowUnsubscribeNonOverlappingAsyncSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowUnsubscribeOverlapping() throws java.lang.Throwable {
            this.payloads.windowUnsubscribeOverlapping.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowUnsubscribeOverlappingAsyncSource() throws java.lang.Throwable {
            this.payloads.windowUnsubscribeOverlappingAsyncSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_takeFlatMapCompletes() throws java.lang.Throwable {
            this.payloads.takeFlatMapCompletes.evaluate();
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
        public void benchmark_errorExact() throws java.lang.Throwable {
            this.payloads.errorExact.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSkip() throws java.lang.Throwable {
            this.payloads.errorSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorOverlap() throws java.lang.Throwable {
            this.payloads.errorOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorExactInner() throws java.lang.Throwable {
            this.payloads.errorExactInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSkipInner() throws java.lang.Throwable {
            this.payloads.errorSkipInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorOverlapInner() throws java.lang.Throwable {
            this.payloads.errorOverlapInner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellingWindowCancelsUpstreamSize() throws java.lang.Throwable {
            this.payloads.cancellingWindowCancelsUpstreamSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowAbandonmentCancelsUpstreamSize() throws java.lang.Throwable {
            this.payloads.windowAbandonmentCancelsUpstreamSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellingWindowCancelsUpstreamSkip() throws java.lang.Throwable {
            this.payloads.cancellingWindowCancelsUpstreamSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowAbandonmentCancelsUpstreamSkip() throws java.lang.Throwable {
            this.payloads.windowAbandonmentCancelsUpstreamSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancellingWindowCancelsUpstreamOverlap() throws java.lang.Throwable {
            this.payloads.cancellingWindowCancelsUpstreamOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_windowAbandonmentCancelsUpstreamOverlap() throws java.lang.Throwable {
            this.payloads.windowAbandonmentCancelsUpstreamOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWithoutWindowSize() throws java.lang.Throwable {
            this.payloads.cancelWithoutWindowSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterAbandonmentSize() throws java.lang.Throwable {
            this.payloads.cancelAfterAbandonmentSize.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWithoutWindowSkip() throws java.lang.Throwable {
            this.payloads.cancelWithoutWindowSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterAbandonmentSkip() throws java.lang.Throwable {
            this.payloads.cancelAfterAbandonmentSkip.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWithoutWindowOverlap() throws java.lang.Throwable {
            this.payloads.cancelWithoutWindowOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelAfterAbandonmentOverlap() throws java.lang.Throwable {
            this.payloads.cancelAfterAbandonmentOverlap.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithSizeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithSizeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithSizeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithSizeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableWindowWithSizeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableWindowWithSizeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableWindowWithSizeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableWindowWithSizeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement nonOverlappingWindows;

            public org.junit.runners.model.Statement skipAndCountGaplessWindows;

            public org.junit.runners.model.Statement overlappingWindows;

            public org.junit.runners.model.Statement skipAndCountWindowsWithGaps;

            public org.junit.runners.model.Statement windowUnsubscribeNonOverlapping;

            public org.junit.runners.model.Statement windowUnsubscribeNonOverlappingAsyncSource;

            public org.junit.runners.model.Statement windowUnsubscribeOverlapping;

            public org.junit.runners.model.Statement windowUnsubscribeOverlappingAsyncSource;

            public org.junit.runners.model.Statement takeFlatMapCompletes;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement errorExact;

            public org.junit.runners.model.Statement errorSkip;

            public org.junit.runners.model.Statement errorOverlap;

            public org.junit.runners.model.Statement errorExactInner;

            public org.junit.runners.model.Statement errorSkipInner;

            public org.junit.runners.model.Statement errorOverlapInner;

            public org.junit.runners.model.Statement cancellingWindowCancelsUpstreamSize;

            public org.junit.runners.model.Statement windowAbandonmentCancelsUpstreamSize;

            public org.junit.runners.model.Statement cancellingWindowCancelsUpstreamSkip;

            public org.junit.runners.model.Statement windowAbandonmentCancelsUpstreamSkip;

            public org.junit.runners.model.Statement cancellingWindowCancelsUpstreamOverlap;

            public org.junit.runners.model.Statement windowAbandonmentCancelsUpstreamOverlap;

            public org.junit.runners.model.Statement cancelWithoutWindowSize;

            public org.junit.runners.model.Statement cancelAfterAbandonmentSize;

            public org.junit.runners.model.Statement cancelWithoutWindowSkip;

            public org.junit.runners.model.Statement cancelAfterAbandonmentSkip;

            public org.junit.runners.model.Statement cancelWithoutWindowOverlap;

            public org.junit.runners.model.Statement cancelAfterAbandonmentOverlap;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.nonOverlappingWindows = _ClassStatement.forPayload(ObservableWindowWithSizeTest::nonOverlappingWindows, "nonOverlappingWindows", this);
            this.payloads.skipAndCountGaplessWindows = _ClassStatement.forPayload(ObservableWindowWithSizeTest::skipAndCountGaplessWindows, "skipAndCountGaplessWindows", this);
            this.payloads.overlappingWindows = _ClassStatement.forPayload(ObservableWindowWithSizeTest::overlappingWindows, "overlappingWindows", this);
            this.payloads.skipAndCountWindowsWithGaps = _ClassStatement.forPayload(ObservableWindowWithSizeTest::skipAndCountWindowsWithGaps, "skipAndCountWindowsWithGaps", this);
            this.payloads.windowUnsubscribeNonOverlapping = _ClassStatement.forPayload(ObservableWindowWithSizeTest::windowUnsubscribeNonOverlapping, "windowUnsubscribeNonOverlapping", this);
            this.payloads.windowUnsubscribeNonOverlappingAsyncSource = _ClassStatement.forPayload(ObservableWindowWithSizeTest::windowUnsubscribeNonOverlappingAsyncSource, "windowUnsubscribeNonOverlappingAsyncSource", this);
            this.payloads.windowUnsubscribeOverlapping = _ClassStatement.forPayload(ObservableWindowWithSizeTest::windowUnsubscribeOverlapping, "windowUnsubscribeOverlapping", this);
            this.payloads.windowUnsubscribeOverlappingAsyncSource = _ClassStatement.forPayload(ObservableWindowWithSizeTest::windowUnsubscribeOverlappingAsyncSource, "windowUnsubscribeOverlappingAsyncSource", this);
            this.payloads.takeFlatMapCompletes = _ClassStatement.forPayload(ObservableWindowWithSizeTest::takeFlatMapCompletes, "takeFlatMapCompletes", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableWindowWithSizeTest::dispose, "dispose", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableWindowWithSizeTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.errorExact = _ClassStatement.forPayload(ObservableWindowWithSizeTest::errorExact, "errorExact", this);
            this.payloads.errorSkip = _ClassStatement.forPayload(ObservableWindowWithSizeTest::errorSkip, "errorSkip", this);
            this.payloads.errorOverlap = _ClassStatement.forPayload(ObservableWindowWithSizeTest::errorOverlap, "errorOverlap", this);
            this.payloads.errorExactInner = _ClassStatement.forPayload(ObservableWindowWithSizeTest::errorExactInner, "errorExactInner", this);
            this.payloads.errorSkipInner = _ClassStatement.forPayload(ObservableWindowWithSizeTest::errorSkipInner, "errorSkipInner", this);
            this.payloads.errorOverlapInner = _ClassStatement.forPayload(ObservableWindowWithSizeTest::errorOverlapInner, "errorOverlapInner", this);
            this.payloads.cancellingWindowCancelsUpstreamSize = _ClassStatement.forPayload(ObservableWindowWithSizeTest::cancellingWindowCancelsUpstreamSize, "cancellingWindowCancelsUpstreamSize", this);
            this.payloads.windowAbandonmentCancelsUpstreamSize = _ClassStatement.forPayload(ObservableWindowWithSizeTest::windowAbandonmentCancelsUpstreamSize, "windowAbandonmentCancelsUpstreamSize", this);
            this.payloads.cancellingWindowCancelsUpstreamSkip = _ClassStatement.forPayload(ObservableWindowWithSizeTest::cancellingWindowCancelsUpstreamSkip, "cancellingWindowCancelsUpstreamSkip", this);
            this.payloads.windowAbandonmentCancelsUpstreamSkip = _ClassStatement.forPayload(ObservableWindowWithSizeTest::windowAbandonmentCancelsUpstreamSkip, "windowAbandonmentCancelsUpstreamSkip", this);
            this.payloads.cancellingWindowCancelsUpstreamOverlap = _ClassStatement.forPayload(ObservableWindowWithSizeTest::cancellingWindowCancelsUpstreamOverlap, "cancellingWindowCancelsUpstreamOverlap", this);
            this.payloads.windowAbandonmentCancelsUpstreamOverlap = _ClassStatement.forPayload(ObservableWindowWithSizeTest::windowAbandonmentCancelsUpstreamOverlap, "windowAbandonmentCancelsUpstreamOverlap", this);
            this.payloads.cancelWithoutWindowSize = _ClassStatement.forPayload(ObservableWindowWithSizeTest::cancelWithoutWindowSize, "cancelWithoutWindowSize", this);
            this.payloads.cancelAfterAbandonmentSize = _ClassStatement.forPayload(ObservableWindowWithSizeTest::cancelAfterAbandonmentSize, "cancelAfterAbandonmentSize", this);
            this.payloads.cancelWithoutWindowSkip = _ClassStatement.forPayload(ObservableWindowWithSizeTest::cancelWithoutWindowSkip, "cancelWithoutWindowSkip", this);
            this.payloads.cancelAfterAbandonmentSkip = _ClassStatement.forPayload(ObservableWindowWithSizeTest::cancelAfterAbandonmentSkip, "cancelAfterAbandonmentSkip", this);
            this.payloads.cancelWithoutWindowOverlap = _ClassStatement.forPayload(ObservableWindowWithSizeTest::cancelWithoutWindowOverlap, "cancelWithoutWindowOverlap", this);
            this.payloads.cancelAfterAbandonmentOverlap = _ClassStatement.forPayload(ObservableWindowWithSizeTest::cancelAfterAbandonmentOverlap, "cancelAfterAbandonmentOverlap", this);
        }
    }
}
