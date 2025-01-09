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
package io.reactivex.rxjava3.core;

import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import org.junit.*;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class XFlatMapTest extends RxJavaTest {

    @Rule
    public Retry retry = new Retry(5, 1000, true);

    static final int SLEEP_AFTER_CANCEL = 500;

    final CyclicBarrier cb = new CyclicBarrier(2);

    void sleep() throws Exception {
        cb.await();
        try {
            long before = System.currentTimeMillis();
            Thread.sleep(5000);
            throw new IllegalStateException("Was not interrupted in time?! " + (System.currentTimeMillis() - before));
        } catch (InterruptedException ex) {
        // ignored here
        }
    }

    void beforeCancelSleep(TestSubscriber<?> ts) throws Exception {
        long before = System.currentTimeMillis();
        Thread.sleep(50);
        if (System.currentTimeMillis() - before > 100) {
            ts.cancel();
            throw new IllegalStateException("Overslept?" + (System.currentTimeMillis() - before));
        }
    }

    void beforeCancelSleep(TestObserver<?> to) throws Exception {
        long before = System.currentTimeMillis();
        Thread.sleep(50);
        if (System.currentTimeMillis() - before > 100) {
            to.dispose();
            throw new IllegalStateException("Overslept?" + (System.currentTimeMillis() - before));
        }
    }

    @Test
    public void flowableFlowable() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestSubscriber<Integer> ts = Flowable.just(1).subscribeOn(Schedulers.io()).flatMap(new Function<Integer, Publisher<Integer>>() {

                @Override
                public Publisher<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Flowable.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(ts);
            ts.cancel();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            ts.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void flowableSingle() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestSubscriber<Integer> ts = Flowable.just(1).subscribeOn(Schedulers.io()).flatMapSingle(new Function<Integer, Single<Integer>>() {

                @Override
                public Single<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Single.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(ts);
            ts.cancel();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            ts.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void flowableMaybe() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestSubscriber<Integer> ts = Flowable.just(1).subscribeOn(Schedulers.io()).flatMapMaybe(new Function<Integer, Maybe<Integer>>() {

                @Override
                public Maybe<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Maybe.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(ts);
            ts.cancel();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            ts.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void flowableCompletable() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Void> to = Flowable.just(1).subscribeOn(Schedulers.io()).flatMapCompletable(new Function<Integer, Completable>() {

                @Override
                public Completable apply(Integer v) throws Exception {
                    sleep();
                    return Completable.error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void flowableCompletable2() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestSubscriber<Void> ts = Flowable.just(1).subscribeOn(Schedulers.io()).flatMapCompletable(new Function<Integer, Completable>() {

                @Override
                public Completable apply(Integer v) throws Exception {
                    sleep();
                    return Completable.error(new TestException());
                }
            }).<Void>toFlowable().test();
            cb.await();
            beforeCancelSleep(ts);
            ts.cancel();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            ts.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void observableObservable() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Observable.just(1).subscribeOn(Schedulers.io()).flatMap(new Function<Integer, Observable<Integer>>() {

                @Override
                public Observable<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Observable.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void observerSingle() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Observable.just(1).subscribeOn(Schedulers.io()).flatMapSingle(new Function<Integer, Single<Integer>>() {

                @Override
                public Single<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Single.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void observerMaybe() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Observable.just(1).subscribeOn(Schedulers.io()).flatMapMaybe(new Function<Integer, Maybe<Integer>>() {

                @Override
                public Maybe<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Maybe.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void observerCompletable() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Void> to = Observable.just(1).subscribeOn(Schedulers.io()).flatMapCompletable(new Function<Integer, Completable>() {

                @Override
                public Completable apply(Integer v) throws Exception {
                    sleep();
                    return Completable.error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void observerCompletable2() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Void> to = Observable.just(1).subscribeOn(Schedulers.io()).flatMapCompletable(new Function<Integer, Completable>() {

                @Override
                public Completable apply(Integer v) throws Exception {
                    sleep();
                    return Completable.error(new TestException());
                }
            }).<Void>toObservable().test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void singleSingle() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Single.just(1).subscribeOn(Schedulers.io()).flatMap(new Function<Integer, Single<Integer>>() {

                @Override
                public Single<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Single.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void singleMaybe() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Single.just(1).subscribeOn(Schedulers.io()).flatMapMaybe(new Function<Integer, Maybe<Integer>>() {

                @Override
                public Maybe<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Maybe.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void singleCompletable() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Void> to = Single.just(1).subscribeOn(Schedulers.io()).flatMapCompletable(new Function<Integer, Completable>() {

                @Override
                public Completable apply(Integer v) throws Exception {
                    sleep();
                    return Completable.error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void singleCompletable2() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Single.just(1).subscribeOn(Schedulers.io()).flatMapCompletable(new Function<Integer, Completable>() {

                @Override
                public Completable apply(Integer v) throws Exception {
                    sleep();
                    return Completable.error(new TestException());
                }
            }).toSingleDefault(0).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void singlePublisher() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestSubscriber<Integer> ts = Single.just(1).subscribeOn(Schedulers.io()).flatMapPublisher(new Function<Integer, Publisher<Integer>>() {

                @Override
                public Publisher<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Flowable.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(ts);
            ts.cancel();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            ts.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void singleCombiner() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Single.just(1).subscribeOn(Schedulers.io()).flatMap(new Function<Integer, Single<Integer>>() {

                @Override
                public Single<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Single.<Integer>error(new TestException());
                }
            }, (a, b) -> a + b).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void singleObservable() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Single.just(1).subscribeOn(Schedulers.io()).flatMapObservable(new Function<Integer, Observable<Integer>>() {

                @Override
                public Observable<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Observable.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void singleNotificationSuccess() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Single.just(1).subscribeOn(Schedulers.io()).flatMap(new Function<Integer, Single<Integer>>() {

                @Override
                public Single<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Single.<Integer>error(new TestException());
                }
            }, new Function<Throwable, Single<Integer>>() {

                @Override
                public Single<Integer> apply(Throwable v) throws Exception {
                    sleep();
                    return Single.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void singleNotificationError() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Single.<Integer>error(new TestException()).subscribeOn(Schedulers.io()).flatMap(new Function<Integer, Single<Integer>>() {

                @Override
                public Single<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Single.<Integer>error(new TestException());
                }
            }, new Function<Throwable, Single<Integer>>() {

                @Override
                public Single<Integer> apply(Throwable v) throws Exception {
                    sleep();
                    return Single.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void maybeSingle() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Maybe.just(1).subscribeOn(Schedulers.io()).flatMapSingle(new Function<Integer, Single<Integer>>() {

                @Override
                public Single<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Single.<Integer>error(new TestException());
                }
            }).toSingle().test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void maybeSingle2() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Maybe.just(1).subscribeOn(Schedulers.io()).flatMapSingle(new Function<Integer, Single<Integer>>() {

                @Override
                public Single<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Single.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void maybeMaybe() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Maybe.just(1).subscribeOn(Schedulers.io()).flatMap(new Function<Integer, Maybe<Integer>>() {

                @Override
                public Maybe<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Maybe.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void maybePublisher() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestSubscriber<Integer> ts = Maybe.just(1).subscribeOn(Schedulers.io()).flatMapPublisher(new Function<Integer, Publisher<Integer>>() {

                @Override
                public Publisher<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Flowable.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(ts);
            ts.cancel();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            ts.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void maybeObservable() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Maybe.just(1).subscribeOn(Schedulers.io()).flatMapObservable(new Function<Integer, Observable<Integer>>() {

                @Override
                public Observable<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Observable.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void maybeNotificationSuccess() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Maybe.just(1).subscribeOn(Schedulers.io()).flatMap(new Function<Integer, Maybe<Integer>>() {

                @Override
                public Maybe<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Maybe.<Integer>error(new TestException());
                }
            }, new Function<Throwable, Maybe<Integer>>() {

                @Override
                public Maybe<Integer> apply(Throwable v) throws Exception {
                    sleep();
                    return Maybe.<Integer>error(new TestException());
                }
            }, new Supplier<Maybe<Integer>>() {

                @Override
                public Maybe<Integer> get() throws Exception {
                    sleep();
                    return Maybe.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void maybeNotificationError() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Maybe.<Integer>error(new TestException()).subscribeOn(Schedulers.io()).flatMap(new Function<Integer, Maybe<Integer>>() {

                @Override
                public Maybe<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Maybe.<Integer>error(new TestException());
                }
            }, new Function<Throwable, Maybe<Integer>>() {

                @Override
                public Maybe<Integer> apply(Throwable v) throws Exception {
                    sleep();
                    return Maybe.<Integer>error(new TestException());
                }
            }, new Supplier<Maybe<Integer>>() {

                @Override
                public Maybe<Integer> get() throws Exception {
                    sleep();
                    return Maybe.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void maybeNotificationEmpty() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Maybe.<Integer>empty().subscribeOn(Schedulers.io()).flatMap(new Function<Integer, Maybe<Integer>>() {

                @Override
                public Maybe<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Maybe.<Integer>error(new TestException());
                }
            }, new Function<Throwable, Maybe<Integer>>() {

                @Override
                public Maybe<Integer> apply(Throwable v) throws Exception {
                    sleep();
                    return Maybe.<Integer>error(new TestException());
                }
            }, new Supplier<Maybe<Integer>>() {

                @Override
                public Maybe<Integer> get() throws Exception {
                    sleep();
                    return Maybe.<Integer>error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void maybeCombiner() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Integer> to = Maybe.just(1).subscribeOn(Schedulers.io()).flatMap(new Function<Integer, Maybe<Integer>>() {

                @Override
                public Maybe<Integer> apply(Integer v) throws Exception {
                    sleep();
                    return Maybe.<Integer>error(new TestException());
                }
            }, (a, b) -> a + b).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void maybeCompletable() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Void> to = Maybe.just(1).subscribeOn(Schedulers.io()).flatMapCompletable(new Function<Integer, Completable>() {

                @Override
                public Completable apply(Integer v) throws Exception {
                    sleep();
                    return Completable.error(new TestException());
                }
            }).test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void maybeCompletable2() throws Exception {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            TestObserver<Void> to = Maybe.just(1).subscribeOn(Schedulers.io()).flatMapCompletable(new Function<Integer, Completable>() {

                @Override
                public Completable apply(Integer v) throws Exception {
                    sleep();
                    return Completable.error(new TestException());
                }
            }).<Void>toMaybe().test();
            cb.await();
            beforeCancelSleep(to);
            to.dispose();
            Thread.sleep(SLEEP_AFTER_CANCEL);
            to.assertEmpty();
            assertTrue(errors.toString(), errors.isEmpty());
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public XFlatMapTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableFlowable() throws java.lang.Throwable {
            this.payloads.flowableFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableSingle() throws java.lang.Throwable {
            this.payloads.flowableSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableMaybe() throws java.lang.Throwable {
            this.payloads.flowableMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableCompletable() throws java.lang.Throwable {
            this.payloads.flowableCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableCompletable2() throws java.lang.Throwable {
            this.payloads.flowableCompletable2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observableObservable() throws java.lang.Throwable {
            this.payloads.observableObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerSingle() throws java.lang.Throwable {
            this.payloads.observerSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerMaybe() throws java.lang.Throwable {
            this.payloads.observerMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerCompletable() throws java.lang.Throwable {
            this.payloads.observerCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observerCompletable2() throws java.lang.Throwable {
            this.payloads.observerCompletable2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSingle() throws java.lang.Throwable {
            this.payloads.singleSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleMaybe() throws java.lang.Throwable {
            this.payloads.singleMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleCompletable() throws java.lang.Throwable {
            this.payloads.singleCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleCompletable2() throws java.lang.Throwable {
            this.payloads.singleCompletable2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singlePublisher() throws java.lang.Throwable {
            this.payloads.singlePublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleCombiner() throws java.lang.Throwable {
            this.payloads.singleCombiner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleObservable() throws java.lang.Throwable {
            this.payloads.singleObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleNotificationSuccess() throws java.lang.Throwable {
            this.payloads.singleNotificationSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleNotificationError() throws java.lang.Throwable {
            this.payloads.singleNotificationError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeSingle() throws java.lang.Throwable {
            this.payloads.maybeSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeSingle2() throws java.lang.Throwable {
            this.payloads.maybeSingle2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeMaybe() throws java.lang.Throwable {
            this.payloads.maybeMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybePublisher() throws java.lang.Throwable {
            this.payloads.maybePublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeObservable() throws java.lang.Throwable {
            this.payloads.maybeObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeNotificationSuccess() throws java.lang.Throwable {
            this.payloads.maybeNotificationSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeNotificationError() throws java.lang.Throwable {
            this.payloads.maybeNotificationError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeNotificationEmpty() throws java.lang.Throwable {
            this.payloads.maybeNotificationEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeCombiner() throws java.lang.Throwable {
            this.payloads.maybeCombiner.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeCompletable() throws java.lang.Throwable {
            this.payloads.maybeCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeCompletable2() throws java.lang.Throwable {
            this.payloads.maybeCompletable2.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<XFlatMapTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<XFlatMapTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<XFlatMapTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<XFlatMapTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new XFlatMapTest();
                org.junit.runners.model.Statement statement = new _InstanceStatement(this.payload, this.benchmark);
                statement = this.applyRule(this.benchmark.instance.retry, statement);
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<XFlatMapTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(XFlatMapTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(XFlatMapTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement flowableFlowable;

            public org.junit.runners.model.Statement flowableSingle;

            public org.junit.runners.model.Statement flowableMaybe;

            public org.junit.runners.model.Statement flowableCompletable;

            public org.junit.runners.model.Statement flowableCompletable2;

            public org.junit.runners.model.Statement observableObservable;

            public org.junit.runners.model.Statement observerSingle;

            public org.junit.runners.model.Statement observerMaybe;

            public org.junit.runners.model.Statement observerCompletable;

            public org.junit.runners.model.Statement observerCompletable2;

            public org.junit.runners.model.Statement singleSingle;

            public org.junit.runners.model.Statement singleMaybe;

            public org.junit.runners.model.Statement singleCompletable;

            public org.junit.runners.model.Statement singleCompletable2;

            public org.junit.runners.model.Statement singlePublisher;

            public org.junit.runners.model.Statement singleCombiner;

            public org.junit.runners.model.Statement singleObservable;

            public org.junit.runners.model.Statement singleNotificationSuccess;

            public org.junit.runners.model.Statement singleNotificationError;

            public org.junit.runners.model.Statement maybeSingle;

            public org.junit.runners.model.Statement maybeSingle2;

            public org.junit.runners.model.Statement maybeMaybe;

            public org.junit.runners.model.Statement maybePublisher;

            public org.junit.runners.model.Statement maybeObservable;

            public org.junit.runners.model.Statement maybeNotificationSuccess;

            public org.junit.runners.model.Statement maybeNotificationError;

            public org.junit.runners.model.Statement maybeNotificationEmpty;

            public org.junit.runners.model.Statement maybeCombiner;

            public org.junit.runners.model.Statement maybeCompletable;

            public org.junit.runners.model.Statement maybeCompletable2;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.flowableFlowable = _ClassStatement.forPayload(XFlatMapTest::flowableFlowable, "flowableFlowable", this);
            this.payloads.flowableSingle = _ClassStatement.forPayload(XFlatMapTest::flowableSingle, "flowableSingle", this);
            this.payloads.flowableMaybe = _ClassStatement.forPayload(XFlatMapTest::flowableMaybe, "flowableMaybe", this);
            this.payloads.flowableCompletable = _ClassStatement.forPayload(XFlatMapTest::flowableCompletable, "flowableCompletable", this);
            this.payloads.flowableCompletable2 = _ClassStatement.forPayload(XFlatMapTest::flowableCompletable2, "flowableCompletable2", this);
            this.payloads.observableObservable = _ClassStatement.forPayload(XFlatMapTest::observableObservable, "observableObservable", this);
            this.payloads.observerSingle = _ClassStatement.forPayload(XFlatMapTest::observerSingle, "observerSingle", this);
            this.payloads.observerMaybe = _ClassStatement.forPayload(XFlatMapTest::observerMaybe, "observerMaybe", this);
            this.payloads.observerCompletable = _ClassStatement.forPayload(XFlatMapTest::observerCompletable, "observerCompletable", this);
            this.payloads.observerCompletable2 = _ClassStatement.forPayload(XFlatMapTest::observerCompletable2, "observerCompletable2", this);
            this.payloads.singleSingle = _ClassStatement.forPayload(XFlatMapTest::singleSingle, "singleSingle", this);
            this.payloads.singleMaybe = _ClassStatement.forPayload(XFlatMapTest::singleMaybe, "singleMaybe", this);
            this.payloads.singleCompletable = _ClassStatement.forPayload(XFlatMapTest::singleCompletable, "singleCompletable", this);
            this.payloads.singleCompletable2 = _ClassStatement.forPayload(XFlatMapTest::singleCompletable2, "singleCompletable2", this);
            this.payloads.singlePublisher = _ClassStatement.forPayload(XFlatMapTest::singlePublisher, "singlePublisher", this);
            this.payloads.singleCombiner = _ClassStatement.forPayload(XFlatMapTest::singleCombiner, "singleCombiner", this);
            this.payloads.singleObservable = _ClassStatement.forPayload(XFlatMapTest::singleObservable, "singleObservable", this);
            this.payloads.singleNotificationSuccess = _ClassStatement.forPayload(XFlatMapTest::singleNotificationSuccess, "singleNotificationSuccess", this);
            this.payloads.singleNotificationError = _ClassStatement.forPayload(XFlatMapTest::singleNotificationError, "singleNotificationError", this);
            this.payloads.maybeSingle = _ClassStatement.forPayload(XFlatMapTest::maybeSingle, "maybeSingle", this);
            this.payloads.maybeSingle2 = _ClassStatement.forPayload(XFlatMapTest::maybeSingle2, "maybeSingle2", this);
            this.payloads.maybeMaybe = _ClassStatement.forPayload(XFlatMapTest::maybeMaybe, "maybeMaybe", this);
            this.payloads.maybePublisher = _ClassStatement.forPayload(XFlatMapTest::maybePublisher, "maybePublisher", this);
            this.payloads.maybeObservable = _ClassStatement.forPayload(XFlatMapTest::maybeObservable, "maybeObservable", this);
            this.payloads.maybeNotificationSuccess = _ClassStatement.forPayload(XFlatMapTest::maybeNotificationSuccess, "maybeNotificationSuccess", this);
            this.payloads.maybeNotificationError = _ClassStatement.forPayload(XFlatMapTest::maybeNotificationError, "maybeNotificationError", this);
            this.payloads.maybeNotificationEmpty = _ClassStatement.forPayload(XFlatMapTest::maybeNotificationEmpty, "maybeNotificationEmpty", this);
            this.payloads.maybeCombiner = _ClassStatement.forPayload(XFlatMapTest::maybeCombiner, "maybeCombiner", this);
            this.payloads.maybeCompletable = _ClassStatement.forPayload(XFlatMapTest::maybeCompletable, "maybeCompletable", this);
            this.payloads.maybeCompletable2 = _ClassStatement.forPayload(XFlatMapTest::maybeCompletable2, "maybeCompletable2", this);
        }
    }
}
