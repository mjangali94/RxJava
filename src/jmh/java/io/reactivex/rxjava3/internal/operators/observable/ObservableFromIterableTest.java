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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.mockito.Mockito;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.util.CrashingIterable;
import io.reactivex.rxjava3.observers.*;
import io.reactivex.rxjava3.operators.QueueDisposable;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableFromIterableTest extends RxJavaTest {

    @Test
    public void listIterable() {
        Observable<String> o = Observable.fromIterable(Arrays.<String>asList("one", "two", "three"));
        Observer<String> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, times(1)).onNext("three");
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    /**
     * This tests the path that can not optimize based on size so must use setProducer.
     */
    @Test
    public void rawIterable() {
        Iterable<String> it = new Iterable<String>() {

            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {

                    int i;

                    @Override
                    public boolean hasNext() {
                        return i < 3;
                    }

                    @Override
                    public String next() {
                        return String.valueOf(++i);
                    }

                    @Override
                    public void remove() {
                    }
                };
            }
        };
        Observable<String> o = Observable.fromIterable(it);
        Observer<String> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        verify(observer, times(1)).onNext("1");
        verify(observer, times(1)).onNext("2");
        verify(observer, times(1)).onNext("3");
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void observableFromIterable() {
        Observable<String> o = Observable.fromIterable(Arrays.<String>asList("one", "two", "three"));
        Observer<String> observer = TestHelper.mockObserver();
        o.subscribe(observer);
        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, times(1)).onNext("three");
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void noBackpressure() {
        Observable<Integer> o = Observable.fromIterable(Arrays.asList(1, 2, 3, 4, 5));
        TestObserverEx<Integer> to = new TestObserverEx<>();
        o.subscribe(to);
        to.assertValues(1, 2, 3, 4, 5);
        to.assertTerminated();
    }

    @Test
    public void subscribeMultipleTimes() {
        Observable<Integer> o = Observable.fromIterable(Arrays.asList(1, 2, 3));
        for (int i = 0; i < 10; i++) {
            TestObserver<Integer> to = new TestObserver<>();
            o.subscribe(to);
            to.assertValues(1, 2, 3);
            to.assertNoErrors();
            to.assertComplete();
        }
    }

    @Test
    public void doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure() {
        final AtomicBoolean called = new AtomicBoolean(false);
        Iterable<Integer> iterable = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    int count = 1;

                    @Override
                    public void remove() {
                    // ignore
                    }

                    @Override
                    public boolean hasNext() {
                        if (count > 1) {
                            called.set(true);
                            return false;
                        }
                        return true;
                    }

                    @Override
                    public Integer next() {
                        return count++;
                    }
                };
            }
        };
        Observable.fromIterable(iterable).take(1).subscribe();
        assertFalse(called.get());
    }

    @Test
    public void doesNotCallIteratorHasNextMoreThanRequiredFastPath() {
        final AtomicBoolean called = new AtomicBoolean(false);
        Iterable<Integer> iterable = new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    @Override
                    public void remove() {
                    // ignore
                    }

                    int count = 1;

                    @Override
                    public boolean hasNext() {
                        if (count > 1) {
                            called.set(true);
                            return false;
                        }
                        return true;
                    }

                    @Override
                    public Integer next() {
                        return count++;
                    }
                };
            }
        };
        Observable.fromIterable(iterable).subscribe(new DefaultObserver<Integer>() {

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer t) {
                // unsubscribe on first emission
                cancel();
            }
        });
        assertFalse(called.get());
    }

    @Test
    public void fusionWithConcatMap() {
        TestObserver<Integer> to = new TestObserver<>();
        Observable.fromIterable(Arrays.asList(1, 2, 3, 4)).concatMap(new Function<Integer, ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> apply(Integer v) {
                return Observable.range(v, 2);
            }
        }).subscribe(to);
        to.assertValues(1, 2, 2, 3, 3, 4, 4, 5);
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void iteratorThrows() {
        Observable.fromIterable(new CrashingIterable(1, 100, 100)).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "iterator()");
    }

    @Test
    public void hasNext2Throws() {
        Observable.fromIterable(new CrashingIterable(100, 2, 100)).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "hasNext()", 0);
    }

    @Test
    public void hasNextCancels() {
        final TestObserver<Integer> to = new TestObserver<>();
        Observable.fromIterable(new Iterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {

                    int count;

                    @Override
                    public boolean hasNext() {
                        if (++count == 2) {
                            to.dispose();
                        }
                        return true;
                    }

                    @Override
                    public Integer next() {
                        return 1;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        }).subscribe(to);
        to.assertValue(1).assertNoErrors().assertNotComplete();
    }

    @Test
    public void fusionRejected() {
        TestObserverEx<Integer> to = new TestObserverEx<>(QueueFuseable.ASYNC);
        Observable.fromIterable(Arrays.asList(1, 2, 3)).subscribe(to);
        to.assertFusionMode(QueueFuseable.NONE).assertResult(1, 2, 3);
    }

    @Test
    public void fusionClear() {
        Observable.fromIterable(Arrays.asList(1, 2, 3)).subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                @SuppressWarnings("unchecked")
                QueueDisposable<Integer> qd = (QueueDisposable<Integer>) d;
                qd.requestFusion(QueueFuseable.ANY);
                try {
                    assertEquals(1, qd.poll().intValue());
                } catch (Throwable ex) {
                    fail(ex.toString());
                }
                qd.clear();
                try {
                    assertNull(qd.poll());
                } catch (Throwable ex) {
                    fail(ex.toString());
                }
            }

            @Override
            public void onNext(Integer value) {
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
    public void disposeAfterHasNext() {
        TestObserver<Integer> to = new TestObserver<>();
        Observable.fromIterable(() -> new Iterator<Integer>() {

            int count;

            @Override
            public boolean hasNext() {
                if (count++ == 2) {
                    to.dispose();
                    return false;
                }
                return true;
            }

            @Override
            public Integer next() {
                return 1;
            }
        }).subscribeWith(to).assertValuesOnly(1, 1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableFromIterableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listIterable() throws java.lang.Throwable {
            this.payloads.listIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rawIterable() throws java.lang.Throwable {
            this.payloads.rawIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableFromIterable() throws java.lang.Throwable {
            this.payloads.observableFromIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noBackpressure() throws java.lang.Throwable {
            this.payloads.noBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeMultipleTimes() throws java.lang.Throwable {
            this.payloads.subscribeMultipleTimes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure() throws java.lang.Throwable {
            this.payloads.doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesNotCallIteratorHasNextMoreThanRequiredFastPath() throws java.lang.Throwable {
            this.payloads.doesNotCallIteratorHasNextMoreThanRequiredFastPath.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionWithConcatMap() throws java.lang.Throwable {
            this.payloads.fusionWithConcatMap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iteratorThrows() throws java.lang.Throwable {
            this.payloads.iteratorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNext2Throws() throws java.lang.Throwable {
            this.payloads.hasNext2Throws.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasNextCancels() throws java.lang.Throwable {
            this.payloads.hasNextCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionRejected() throws java.lang.Throwable {
            this.payloads.fusionRejected.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusionClear() throws java.lang.Throwable {
            this.payloads.fusionClear.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeAfterHasNext() throws java.lang.Throwable {
            this.payloads.disposeAfterHasNext.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromIterableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromIterableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromIterableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromIterableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableFromIterableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableFromIterableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableFromIterableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableFromIterableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement listIterable;

            public org.junit.runners.model.Statement rawIterable;

            public org.junit.runners.model.Statement observableFromIterable;

            public org.junit.runners.model.Statement noBackpressure;

            public org.junit.runners.model.Statement subscribeMultipleTimes;

            public org.junit.runners.model.Statement doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure;

            public org.junit.runners.model.Statement doesNotCallIteratorHasNextMoreThanRequiredFastPath;

            public org.junit.runners.model.Statement fusionWithConcatMap;

            public org.junit.runners.model.Statement iteratorThrows;

            public org.junit.runners.model.Statement hasNext2Throws;

            public org.junit.runners.model.Statement hasNextCancels;

            public org.junit.runners.model.Statement fusionRejected;

            public org.junit.runners.model.Statement fusionClear;

            public org.junit.runners.model.Statement disposeAfterHasNext;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.listIterable = _ClassStatement.forPayload(ObservableFromIterableTest::listIterable, "listIterable", this);
            this.payloads.rawIterable = _ClassStatement.forPayload(ObservableFromIterableTest::rawIterable, "rawIterable", this);
            this.payloads.observableFromIterable = _ClassStatement.forPayload(ObservableFromIterableTest::observableFromIterable, "observableFromIterable", this);
            this.payloads.noBackpressure = _ClassStatement.forPayload(ObservableFromIterableTest::noBackpressure, "noBackpressure", this);
            this.payloads.subscribeMultipleTimes = _ClassStatement.forPayload(ObservableFromIterableTest::subscribeMultipleTimes, "subscribeMultipleTimes", this);
            this.payloads.doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure = _ClassStatement.forPayload(ObservableFromIterableTest::doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure, "doesNotCallIteratorHasNextMoreThanRequiredWithBackpressure", this);
            this.payloads.doesNotCallIteratorHasNextMoreThanRequiredFastPath = _ClassStatement.forPayload(ObservableFromIterableTest::doesNotCallIteratorHasNextMoreThanRequiredFastPath, "doesNotCallIteratorHasNextMoreThanRequiredFastPath", this);
            this.payloads.fusionWithConcatMap = _ClassStatement.forPayload(ObservableFromIterableTest::fusionWithConcatMap, "fusionWithConcatMap", this);
            this.payloads.iteratorThrows = _ClassStatement.forPayload(ObservableFromIterableTest::iteratorThrows, "iteratorThrows", this);
            this.payloads.hasNext2Throws = _ClassStatement.forPayload(ObservableFromIterableTest::hasNext2Throws, "hasNext2Throws", this);
            this.payloads.hasNextCancels = _ClassStatement.forPayload(ObservableFromIterableTest::hasNextCancels, "hasNextCancels", this);
            this.payloads.fusionRejected = _ClassStatement.forPayload(ObservableFromIterableTest::fusionRejected, "fusionRejected", this);
            this.payloads.fusionClear = _ClassStatement.forPayload(ObservableFromIterableTest::fusionClear, "fusionClear", this);
            this.payloads.disposeAfterHasNext = _ClassStatement.forPayload(ObservableFromIterableTest::disposeAfterHasNext, "disposeAfterHasNext", this);
        }
    }
}
