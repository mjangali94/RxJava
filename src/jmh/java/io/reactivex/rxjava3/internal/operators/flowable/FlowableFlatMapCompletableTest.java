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
import java.util.List;
import java.util.concurrent.*;
import org.junit.Test;
import org.reactivestreams.Subscription;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.operators.QueueSubscription;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableFlatMapCompletableTest extends RxJavaTest {

    @Test
    public void normalFlowable() {
        Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).toFlowable().test().assertResult();
    }

    @Test
    public void mapperThrowsFlowable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).<Integer>toFlowable().test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertFailure(TestException.class);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void mapperReturnsNullFlowable() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestSubscriber<Integer> ts = pp.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return null;
            }
        }).<Integer>toFlowable().test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        ts.assertFailure(NullPointerException.class);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void normalDelayErrorFlowable() {
        Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, true, Integer.MAX_VALUE).toFlowable().test().assertResult();
    }

    @Test
    public void normalAsyncFlowable() {
        Flowable.range(1, 1000).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Flowable.range(1, 100).subscribeOn(Schedulers.computation()).ignoreElements();
            }
        }).toFlowable().test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void normalAsyncFlowableMaxConcurrency() {
        Flowable.range(1, 1000).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Flowable.range(1, 100).subscribeOn(Schedulers.computation()).ignoreElements();
            }
        }, false, 3).toFlowable().test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void normalDelayErrorAllFlowable() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).<Integer>toFlowable().to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(ts.errors().get(0));
        for (int i = 0; i < 11; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalDelayInnerErrorAllFlowable() {
        TestSubscriberEx<Integer> ts = Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).<Integer>toFlowable().to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(ts.errors().get(0));
        for (int i = 0; i < 10; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalNonDelayErrorOuterFlowable() {
        Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, false, Integer.MAX_VALUE).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void fusedFlowable() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).<Integer>toFlowable().subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void normal() {
        Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).test().assertResult();
    }

    @Test
    public void mapperThrows() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Void> to = pp.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                throw new TestException();
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        to.assertFailure(TestException.class);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void mapperReturnsNull() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Void> to = pp.flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return null;
            }
        }).test();
        assertTrue(pp.hasSubscribers());
        pp.onNext(1);
        to.assertFailure(NullPointerException.class);
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void normalDelayError() {
        Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, true, Integer.MAX_VALUE).test().assertResult();
    }

    @Test
    public void normalAsync() {
        Flowable.range(1, 1000).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Flowable.range(1, 100).subscribeOn(Schedulers.computation()).ignoreElements();
            }
        }).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void normalDelayErrorAll() {
        TestObserverEx<Void> to = Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        for (int i = 0; i < 11; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalDelayInnerErrorAll() {
        TestObserverEx<Void> to = Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }, true, Integer.MAX_VALUE).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        for (int i = 0; i < 10; i++) {
            TestHelper.assertError(errors, i, TestException.class);
        }
    }

    @Test
    public void normalNonDelayErrorOuter() {
        Flowable.range(1, 10).concatWith(Flowable.<Integer>error(new TestException())).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }, false, Integer.MAX_VALUE).test().assertFailure(TestException.class);
    }

    @Test
    public void fused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).<Integer>toFlowable().subscribe(ts);
        ts.assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult();
    }

    @Test
    public void disposed() {
        TestHelper.checkDisposed(Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }));
    }

    @Test
    public void normalAsyncMaxConcurrency() {
        Flowable.range(1, 1000).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Flowable.range(1, 100).subscribeOn(Schedulers.computation()).ignoreElements();
            }
        }, false, 3).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void disposedFlowable() {
        TestHelper.checkDisposed(Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).toFlowable());
    }

    @Test
    public void badSource() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.flatMapCompletable(new Function<Integer, CompletableSource>() {

                    @Override
                    public CompletableSource apply(Integer v) throws Exception {
                        return Completable.complete();
                    }
                });
            }
        }, false, 1, null);
    }

    @Test
    public void fusedInternalsFlowable() {
        Flowable.range(1, 10).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).toFlowable().subscribe(new FlowableSubscriber<Object>() {

            @Override
            public void onSubscribe(Subscription s) {
                QueueSubscription<?> qs = (QueueSubscription<?>) s;
                try {
                    assertNull(qs.poll());
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
                assertTrue(qs.isEmpty());
                qs.clear();
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
    public void innerObserverFlowable() {
        Flowable.range(1, 3).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return new Completable() {

                    @Override
                    protected void subscribeActual(CompletableObserver observer) {
                        observer.onSubscribe(Disposable.empty());
                        assertFalse(((Disposable) observer).isDisposed());
                        ((Disposable) observer).dispose();
                        assertTrue(((Disposable) observer).isDisposed());
                    }
                };
            }
        }).toFlowable().test();
    }

    @Test
    public void badSourceFlowable() {
        TestHelper.checkBadSourceFlowable(new Function<Flowable<Integer>, Object>() {

            @Override
            public Object apply(Flowable<Integer> f) throws Exception {
                return f.flatMapCompletable(new Function<Integer, CompletableSource>() {

                    @Override
                    public CompletableSource apply(Integer v) throws Exception {
                        return Completable.complete();
                    }
                }).toFlowable();
            }
        }, false, 1, null);
    }

    @Test
    public void innerObserver() {
        Flowable.range(1, 3).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                return new Completable() {

                    @Override
                    protected void subscribeActual(CompletableObserver observer) {
                        observer.onSubscribe(Disposable.empty());
                        assertFalse(((Disposable) observer).isDisposed());
                        ((Disposable) observer).dispose();
                        assertTrue(((Disposable) observer).isDisposed());
                    }
                };
            }
        }).test();
    }

    @Test
    public void delayErrorMaxConcurrency() {
        Flowable.range(1, 3).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                if (v == 2) {
                    return Completable.error(new TestException());
                }
                return Completable.complete();
            }
        }, true, 1).toFlowable().test().assertFailure(TestException.class);
    }

    @Test
    public void delayErrorMaxConcurrencyCompletable() {
        Flowable.range(1, 3).flatMapCompletable(new Function<Integer, CompletableSource>() {

            @Override
            public CompletableSource apply(Integer v) throws Exception {
                if (v == 2) {
                    return Completable.error(new TestException());
                }
                return Completable.complete();
            }
        }, true, 1).test().assertFailure(TestException.class);
    }

    @Test
    public void asyncMaxConcurrency() {
        for (int itemCount = 1; itemCount <= 100000; itemCount *= 10) {
            for (int concurrency = 1; concurrency <= 256; concurrency *= 2) {
                Flowable.range(1, itemCount).flatMapCompletable(Functions.justFunction(Completable.complete().subscribeOn(Schedulers.computation())), false, concurrency).test().withTag("itemCount=" + itemCount + ", concurrency=" + concurrency).awaitDone(5, TimeUnit.SECONDS).assertResult();
            }
        }
    }

    @Test
    public void undeliverableUponCancel() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Flowable<Integer> upstream) {
                return upstream.flatMapCompletable(new Function<Integer, Completable>() {

                    @Override
                    public Completable apply(Integer v) throws Throwable {
                        return Completable.complete().hide();
                    }
                });
            }
        });
    }

    @Test
    public void undeliverableUponCancelDelayError() {
        TestHelper.checkUndeliverableUponCancel(new FlowableConverter<Integer, Completable>() {

            @Override
            public Completable apply(Flowable<Integer> upstream) {
                return upstream.flatMapCompletable(new Function<Integer, Completable>() {

                    @Override
                    public Completable apply(Integer v) throws Throwable {
                        return Completable.complete().hide();
                    }
                }, true, 2);
            }
        });
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeFlowable(f -> f.flatMapCompletable(v -> Completable.never()).toFlowable());
    }

    @Test
    public void doubleOnSubscribeCompletable() {
        TestHelper.checkDoubleOnSubscribeFlowableToCompletable(f -> f.flatMapCompletable(v -> Completable.never()));
    }

    @Test
    public void cancelWhileMapping() throws Throwable {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishProcessor<Integer> pp1 = PublishProcessor.create();
            TestSubscriber<Object> ts = new TestSubscriber<>();
            CountDownLatch cdl = new CountDownLatch(1);
            pp1.flatMapCompletable(v -> {
                TestHelper.raceOther(() -> {
                    ts.cancel();
                }, cdl);
                return Completable.complete();
            }).toFlowable().subscribe(ts);
            pp1.onNext(1);
            cdl.await();
        }
    }

    @Test
    public void cancelWhileMappingCompletable() throws Throwable {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            PublishProcessor<Integer> pp1 = PublishProcessor.create();
            TestObserver<Void> to = new TestObserver<>();
            CountDownLatch cdl = new CountDownLatch(1);
            pp1.flatMapCompletable(v -> {
                TestHelper.raceOther(() -> {
                    to.dispose();
                }, cdl);
                return Completable.complete();
            }).subscribe(to);
            pp1.onNext(1);
            cdl.await();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public FlowableFlatMapCompletableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalFlowable() throws java.lang.Throwable {
            this.payloads.normalFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrowsFlowable() throws java.lang.Throwable {
            this.payloads.mapperThrowsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperReturnsNullFlowable() throws java.lang.Throwable {
            this.payloads.mapperReturnsNullFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayErrorFlowable() throws java.lang.Throwable {
            this.payloads.normalDelayErrorFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsyncFlowable() throws java.lang.Throwable {
            this.payloads.normalAsyncFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsyncFlowableMaxConcurrency() throws java.lang.Throwable {
            this.payloads.normalAsyncFlowableMaxConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayErrorAllFlowable() throws java.lang.Throwable {
            this.payloads.normalDelayErrorAllFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayInnerErrorAllFlowable() throws java.lang.Throwable {
            this.payloads.normalDelayInnerErrorAllFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalNonDelayErrorOuterFlowable() throws java.lang.Throwable {
            this.payloads.normalNonDelayErrorOuterFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedFlowable() throws java.lang.Throwable {
            this.payloads.fusedFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.payloads.normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperThrows() throws java.lang.Throwable {
            this.payloads.mapperThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapperReturnsNull() throws java.lang.Throwable {
            this.payloads.mapperReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayError() throws java.lang.Throwable {
            this.payloads.normalDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsync() throws java.lang.Throwable {
            this.payloads.normalAsync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayErrorAll() throws java.lang.Throwable {
            this.payloads.normalDelayErrorAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalDelayInnerErrorAll() throws java.lang.Throwable {
            this.payloads.normalDelayInnerErrorAll.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalNonDelayErrorOuter() throws java.lang.Throwable {
            this.payloads.normalNonDelayErrorOuter.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fused() throws java.lang.Throwable {
            this.payloads.fused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposed() throws java.lang.Throwable {
            this.payloads.disposed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normalAsyncMaxConcurrency() throws java.lang.Throwable {
            this.payloads.normalAsyncMaxConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposedFlowable() throws java.lang.Throwable {
            this.payloads.disposedFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSource() throws java.lang.Throwable {
            this.payloads.badSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fusedInternalsFlowable() throws java.lang.Throwable {
            this.payloads.fusedInternalsFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerObserverFlowable() throws java.lang.Throwable {
            this.payloads.innerObserverFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badSourceFlowable() throws java.lang.Throwable {
            this.payloads.badSourceFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerObserver() throws java.lang.Throwable {
            this.payloads.innerObserver.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorMaxConcurrency() throws java.lang.Throwable {
            this.payloads.delayErrorMaxConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorMaxConcurrencyCompletable() throws java.lang.Throwable {
            this.payloads.delayErrorMaxConcurrencyCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncMaxConcurrency() throws java.lang.Throwable {
            this.payloads.asyncMaxConcurrency.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancel() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_undeliverableUponCancelDelayError() throws java.lang.Throwable {
            this.payloads.undeliverableUponCancelDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribeCompletable() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribeCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWhileMapping() throws java.lang.Throwable {
            this.payloads.cancelWhileMapping.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cancelWhileMappingCompletable() throws java.lang.Throwable {
            this.payloads.cancelWhileMappingCompletable.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapCompletableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapCompletableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapCompletableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapCompletableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableFlatMapCompletableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableFlatMapCompletableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableFlatMapCompletableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableFlatMapCompletableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement normalFlowable;

            public org.junit.runners.model.Statement mapperThrowsFlowable;

            public org.junit.runners.model.Statement mapperReturnsNullFlowable;

            public org.junit.runners.model.Statement normalDelayErrorFlowable;

            public org.junit.runners.model.Statement normalAsyncFlowable;

            public org.junit.runners.model.Statement normalAsyncFlowableMaxConcurrency;

            public org.junit.runners.model.Statement normalDelayErrorAllFlowable;

            public org.junit.runners.model.Statement normalDelayInnerErrorAllFlowable;

            public org.junit.runners.model.Statement normalNonDelayErrorOuterFlowable;

            public org.junit.runners.model.Statement fusedFlowable;

            public org.junit.runners.model.Statement normal;

            public org.junit.runners.model.Statement mapperThrows;

            public org.junit.runners.model.Statement mapperReturnsNull;

            public org.junit.runners.model.Statement normalDelayError;

            public org.junit.runners.model.Statement normalAsync;

            public org.junit.runners.model.Statement normalDelayErrorAll;

            public org.junit.runners.model.Statement normalDelayInnerErrorAll;

            public org.junit.runners.model.Statement normalNonDelayErrorOuter;

            public org.junit.runners.model.Statement fused;

            public org.junit.runners.model.Statement disposed;

            public org.junit.runners.model.Statement normalAsyncMaxConcurrency;

            public org.junit.runners.model.Statement disposedFlowable;

            public org.junit.runners.model.Statement badSource;

            public org.junit.runners.model.Statement fusedInternalsFlowable;

            public org.junit.runners.model.Statement innerObserverFlowable;

            public org.junit.runners.model.Statement badSourceFlowable;

            public org.junit.runners.model.Statement innerObserver;

            public org.junit.runners.model.Statement delayErrorMaxConcurrency;

            public org.junit.runners.model.Statement delayErrorMaxConcurrencyCompletable;

            public org.junit.runners.model.Statement asyncMaxConcurrency;

            public org.junit.runners.model.Statement undeliverableUponCancel;

            public org.junit.runners.model.Statement undeliverableUponCancelDelayError;

            public org.junit.runners.model.Statement doubleOnSubscribe;

            public org.junit.runners.model.Statement doubleOnSubscribeCompletable;

            public org.junit.runners.model.Statement cancelWhileMapping;

            public org.junit.runners.model.Statement cancelWhileMappingCompletable;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.normalFlowable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::normalFlowable, "normalFlowable", this);
            this.payloads.mapperThrowsFlowable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::mapperThrowsFlowable, "mapperThrowsFlowable", this);
            this.payloads.mapperReturnsNullFlowable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::mapperReturnsNullFlowable, "mapperReturnsNullFlowable", this);
            this.payloads.normalDelayErrorFlowable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::normalDelayErrorFlowable, "normalDelayErrorFlowable", this);
            this.payloads.normalAsyncFlowable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::normalAsyncFlowable, "normalAsyncFlowable", this);
            this.payloads.normalAsyncFlowableMaxConcurrency = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::normalAsyncFlowableMaxConcurrency, "normalAsyncFlowableMaxConcurrency", this);
            this.payloads.normalDelayErrorAllFlowable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::normalDelayErrorAllFlowable, "normalDelayErrorAllFlowable", this);
            this.payloads.normalDelayInnerErrorAllFlowable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::normalDelayInnerErrorAllFlowable, "normalDelayInnerErrorAllFlowable", this);
            this.payloads.normalNonDelayErrorOuterFlowable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::normalNonDelayErrorOuterFlowable, "normalNonDelayErrorOuterFlowable", this);
            this.payloads.fusedFlowable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::fusedFlowable, "fusedFlowable", this);
            this.payloads.normal = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::normal, "normal", this);
            this.payloads.mapperThrows = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::mapperThrows, "mapperThrows", this);
            this.payloads.mapperReturnsNull = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::mapperReturnsNull, "mapperReturnsNull", this);
            this.payloads.normalDelayError = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::normalDelayError, "normalDelayError", this);
            this.payloads.normalAsync = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::normalAsync, "normalAsync", this);
            this.payloads.normalDelayErrorAll = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::normalDelayErrorAll, "normalDelayErrorAll", this);
            this.payloads.normalDelayInnerErrorAll = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::normalDelayInnerErrorAll, "normalDelayInnerErrorAll", this);
            this.payloads.normalNonDelayErrorOuter = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::normalNonDelayErrorOuter, "normalNonDelayErrorOuter", this);
            this.payloads.fused = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::fused, "fused", this);
            this.payloads.disposed = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::disposed, "disposed", this);
            this.payloads.normalAsyncMaxConcurrency = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::normalAsyncMaxConcurrency, "normalAsyncMaxConcurrency", this);
            this.payloads.disposedFlowable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::disposedFlowable, "disposedFlowable", this);
            this.payloads.badSource = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::badSource, "badSource", this);
            this.payloads.fusedInternalsFlowable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::fusedInternalsFlowable, "fusedInternalsFlowable", this);
            this.payloads.innerObserverFlowable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::innerObserverFlowable, "innerObserverFlowable", this);
            this.payloads.badSourceFlowable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::badSourceFlowable, "badSourceFlowable", this);
            this.payloads.innerObserver = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::innerObserver, "innerObserver", this);
            this.payloads.delayErrorMaxConcurrency = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::delayErrorMaxConcurrency, "delayErrorMaxConcurrency", this);
            this.payloads.delayErrorMaxConcurrencyCompletable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::delayErrorMaxConcurrencyCompletable, "delayErrorMaxConcurrencyCompletable", this);
            this.payloads.asyncMaxConcurrency = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::asyncMaxConcurrency, "asyncMaxConcurrency", this);
            this.payloads.undeliverableUponCancel = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::undeliverableUponCancel, "undeliverableUponCancel", this);
            this.payloads.undeliverableUponCancelDelayError = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::undeliverableUponCancelDelayError, "undeliverableUponCancelDelayError", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::doubleOnSubscribe, "doubleOnSubscribe", this);
            this.payloads.doubleOnSubscribeCompletable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::doubleOnSubscribeCompletable, "doubleOnSubscribeCompletable", this);
            this.payloads.cancelWhileMapping = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::cancelWhileMapping, "cancelWhileMapping", this);
            this.payloads.cancelWhileMappingCompletable = _ClassStatement.forPayload(FlowableFlatMapCompletableTest::cancelWhileMappingCompletable, "cancelWhileMappingCompletable", this);
        }
    }
}
