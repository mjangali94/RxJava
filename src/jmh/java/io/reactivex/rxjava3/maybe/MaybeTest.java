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
package io.reactivex.rxjava3.maybe;

import static org.junit.Assert.*;
import java.io.IOException;
import java.lang.management.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.reactivestreams.Publisher;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableZipTest.ArgsToString;
import io.reactivex.rxjava3.internal.operators.maybe.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.operators.QueueFuseable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class MaybeTest extends RxJavaTest {

    @Test
    public void fromFlowableEmpty() {
        Flowable.empty().singleElement().test().assertResult();
    }

    @Test
    public void fromFlowableJust() {
        Flowable.just(1).singleElement().test().assertResult(1);
    }

    @Test
    public void fromFlowableError() {
        Flowable.error(new TestException()).singleElement().test().assertFailure(TestException.class);
    }

    @Test
    public void fromFlowableValueAndError() {
        Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).singleElement().test().assertFailure(TestException.class);
    }

    @Test
    public void fromFlowableMany() {
        Flowable.range(1, 2).singleElement().test().assertFailure(IllegalArgumentException.class);
    }

    @Test
    public void fromFlowableDisposeComposesThrough() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        TestObserver<Integer> to = pp.singleElement().test();
        assertTrue(pp.hasSubscribers());
        to.dispose();
        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void fromObservableEmpty() {
        Observable.empty().singleElement().test().assertResult();
    }

    @Test
    public void fromObservableJust() {
        Observable.just(1).singleElement().test().assertResult(1);
    }

    @Test
    public void fromObservableError() {
        Observable.error(new TestException()).singleElement().test().assertFailure(TestException.class);
    }

    @Test
    public void fromObservableValueAndError() {
        Flowable.just(1).concatWith(Flowable.<Integer>error(new TestException())).singleElement().test().assertFailure(TestException.class);
    }

    @Test
    public void fromObservableMany() {
        Observable.range(1, 2).singleElement().test().assertFailure(IllegalArgumentException.class);
    }

    @Test
    public void fromObservableDisposeComposesThrough() {
        PublishSubject<Integer> ps = PublishSubject.create();
        TestObserver<Integer> to = ps.singleElement().test(false);
        assertTrue(ps.hasObservers());
        to.dispose();
        assertFalse(ps.hasObservers());
    }

    @Test
    public void fromObservableDisposeComposesThroughImmediatelyCancelled() {
        PublishSubject<Integer> ps = PublishSubject.create();
        ps.singleElement().test(true);
        assertFalse(ps.hasObservers());
    }

    @Test
    public void just() {
        Maybe.just(1).test().assertResult(1);
    }

    @Test
    public void empty() {
        Maybe.empty().test().assertResult();
    }

    @Test
    public void never() {
        Maybe.never().to(TestHelper.testConsumer()).assertSubscribed().assertNoValues().assertNoErrors().assertNotComplete();
    }

    @Test
    public void error() {
        Maybe.error(new TestException()).test().assertFailure(TestException.class);
    }

    @Test
    public void errorCallable() {
        Maybe.error(Functions.justSupplier(new TestException())).test().assertFailure(TestException.class);
    }

    @Test
    public void errorCallableReturnsNull() {
        Maybe.error(Functions.justSupplier((Throwable) null)).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void wrapCustom() {
        Maybe.wrap(new MaybeSource<Integer>() {

            @Override
            public void subscribe(MaybeObserver<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onSuccess(1);
            }
        }).test().assertResult(1);
    }

    @Test
    public void wrapMaybe() {
        assertSame(Maybe.empty(), Maybe.wrap(Maybe.empty()));
    }

    @Test
    public void emptySingleton() {
        assertSame(Maybe.empty(), Maybe.empty());
    }

    @Test
    public void neverSingleton() {
        assertSame(Maybe.never(), Maybe.never());
    }

    @Test
    public void liftJust() {
        Maybe.just(1).lift(new MaybeOperator<Integer, Integer>() {

            @Override
            public MaybeObserver<? super Integer> apply(MaybeObserver<? super Integer> t) throws Exception {
                return t;
            }
        }).test().assertResult(1);
    }

    @Test
    public void liftThrows() {
        Maybe.just(1).lift(new MaybeOperator<Integer, Integer>() {

            @Override
            public MaybeObserver<? super Integer> apply(MaybeObserver<? super Integer> t) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void deferThrows() {
        Maybe.defer(new Supplier<Maybe<Integer>>() {

            @Override
            public Maybe<Integer> get() throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void deferReturnsNull() {
        Maybe.defer(new Supplier<Maybe<Integer>>() {

            @Override
            public Maybe<Integer> get() throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void defer() {
        Maybe<Integer> source = Maybe.defer(new Supplier<Maybe<Integer>>() {

            int count;

            @Override
            public Maybe<Integer> get() throws Exception {
                return Maybe.just(count++);
            }
        });
        for (int i = 0; i < 128; i++) {
            source.test().assertResult(i);
        }
    }

    @Test
    public void flowableMaybeFlowable() {
        Flowable.just(1).singleElement().toFlowable().test().assertResult(1);
    }

    @Test
    public void obervableMaybeobervable() {
        Observable.just(1).singleElement().toObservable().test().assertResult(1);
    }

    @Test
    public void singleMaybeSingle() {
        Single.just(1).toMaybe().toSingle().test().assertResult(1);
    }

    @Test
    public void completableMaybeCompletable() {
        Completable.complete().toMaybe().ignoreElement().test().assertResult();
    }

    @Test
    public void unsafeCreate() {
        Maybe.unsafeCreate(new MaybeSource<Integer>() {

            @Override
            public void subscribe(MaybeObserver<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onSuccess(1);
            }
        }).test().assertResult(1);
    }

    @Test
    public void to() {
        Maybe.just(1).to(new MaybeConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Maybe<Integer> v) {
                return v.toFlowable();
            }
        }).test().assertResult(1);
    }

    @Test
    public void as() {
        Maybe.just(1).to(new MaybeConverter<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Maybe<Integer> v) {
                return v.toFlowable();
            }
        }).test().assertResult(1);
    }

    @Test
    public void compose() {
        Maybe.just(1).compose(new MaybeTransformer<Integer, Integer>() {

            @Override
            public MaybeSource<Integer> apply(Maybe<Integer> m) {
                return m.map(new Function<Integer, Integer>() {

                    @Override
                    public Integer apply(Integer w) throws Exception {
                        return w + 1;
                    }
                });
            }
        }).test().assertResult(2);
    }

    @Test
    public void mapReturnNull() {
        Maybe.just(1).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                return null;
            }
        }).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void mapThrows() {
        Maybe.just(1).map(new Function<Integer, Object>() {

            @Override
            public Object apply(Integer v) throws Exception {
                throw new IOException();
            }
        }).test().assertFailure(IOException.class);
    }

    @Test
    public void map() {
        Maybe.just(1).map(new Function<Integer, String>() {

            @Override
            public String apply(Integer v) throws Exception {
                return v.toString();
            }
        }).test().assertResult("1");
    }

    @Test
    public void filterThrows() {
        Maybe.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                throw new IOException();
            }
        }).test().assertFailure(IOException.class);
    }

    @Test
    public void filterTrue() {
        Maybe.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v == 1;
            }
        }).test().assertResult(1);
    }

    @Test
    public void filterFalse() {
        Maybe.just(2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v == 1;
            }
        }).test().assertResult();
    }

    @Test
    public void filterEmpty() {
        Maybe.<Integer>empty().filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v == 1;
            }
        }).test().assertResult();
    }

    @Test
    public void singleFilterThrows() {
        Single.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                throw new IOException();
            }
        }).test().assertFailure(IOException.class);
    }

    @Test
    public void singleFilterTrue() {
        Single.just(1).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v == 1;
            }
        }).test().assertResult(1);
    }

    @Test
    public void singleFilterFalse() {
        Single.just(2).filter(new Predicate<Integer>() {

            @Override
            public boolean test(Integer v) throws Exception {
                return v == 1;
            }
        }).test().assertResult();
    }

    @Test
    public void cast() {
        TestObserver<Number> to = Maybe.just(1).cast(Number.class).test();
        // don'n inline this due to the generic type
        to.assertResult((Number) 1);
    }

    @Test
    public void observeOnSuccess() {
        String main = Thread.currentThread().getName();
        TestObserver<String> to = Maybe.just(1).observeOn(Schedulers.single()).map(new Function<Integer, String>() {

            @Override
            public String apply(Integer v) throws Exception {
                return v + ": " + Thread.currentThread().getName();
            }
        }).test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(1);
        assertNotEquals("1: " + main, to.values().get(0));
    }

    @Test
    public void observeOnError() {
        Maybe.error(new TestException()).observeOn(Schedulers.single()).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void observeOnComplete() {
        Maybe.empty().observeOn(Schedulers.single()).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void observeOnDispose2() {
        TestHelper.checkDisposed(Maybe.empty().observeOn(Schedulers.single()));
    }

    @Test
    public void observeOnDoubleSubscribe() {
        TestHelper.checkDoubleOnSubscribeMaybe(new Function<Maybe<Object>, MaybeSource<Object>>() {

            @Override
            public MaybeSource<Object> apply(Maybe<Object> m) throws Exception {
                return m.observeOn(Schedulers.single());
            }
        });
    }

    @Test
    public void subscribeOnSuccess() {
        String main = Thread.currentThread().getName();
        TestObserver<String> to = Maybe.fromCallable(new Callable<String>() {

            @Override
            public String call() throws Exception {
                return Thread.currentThread().getName();
            }
        }).subscribeOn(Schedulers.single()).test().awaitDone(5, TimeUnit.SECONDS).assertValueCount(1);
        assertNotEquals(main, to.values().get(0));
    }

    @Test
    public void observeOnErrorThread() {
        String main = Thread.currentThread().getName();
        final String[] name = { null };
        Maybe.error(new TestException()).observeOn(Schedulers.single()).doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                name[0] = Thread.currentThread().getName();
            }
        }).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
        assertNotEquals(main, name[0]);
    }

    @Test
    public void observeOnCompleteThread() {
        String main = Thread.currentThread().getName();
        final String[] name = { null };
        Maybe.empty().observeOn(Schedulers.single()).doOnComplete(new Action() {

            @Override
            public void run() throws Exception {
                name[0] = Thread.currentThread().getName();
            }
        }).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
        assertNotEquals(main, name[0]);
    }

    @Test
    public void subscribeOnError() {
        Maybe.error(new TestException()).subscribeOn(Schedulers.single()).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
    }

    @Test
    public void subscribeOnComplete() {
        Maybe.empty().subscribeOn(Schedulers.single()).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
    }

    @Test
    public void fromAction() {
        final int[] call = { 0 };
        Maybe.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                call[0]++;
            }
        }).test().assertResult();
        assertEquals(1, call[0]);
    }

    @Test
    public void fromActionThrows() {
        Maybe.fromAction(new Action() {

            @Override
            public void run() throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void fromRunnable() {
        final int[] call = { 0 };
        Maybe.fromRunnable(new Runnable() {

            @Override
            public void run() {
                call[0]++;
            }
        }).test().assertResult();
        assertEquals(1, call[0]);
    }

    @Test
    public void fromRunnableThrows() {
        Maybe.fromRunnable(new Runnable() {

            @Override
            public void run() {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void fromCallableThrows() {
        Maybe.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void doOnSuccess() {
        final Integer[] value = { null };
        Maybe.just(1).doOnSuccess(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                value[0] = v;
            }
        }).test().assertResult(1);
        assertEquals(1, value[0].intValue());
    }

    @Test
    public void doOnSuccessEmpty() {
        final Integer[] value = { null };
        Maybe.<Integer>empty().doOnSuccess(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                value[0] = v;
            }
        }).test().assertResult();
        assertNull(value[0]);
    }

    @Test
    public void doOnSuccessThrows() {
        Maybe.just(1).doOnSuccess(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void doOnSubscribe() {
        final Disposable[] value = { null };
        Maybe.just(1).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable v) throws Exception {
                value[0] = v;
            }
        }).test().assertResult(1);
        assertNotNull(value[0]);
    }

    @Test
    public void doOnSubscribeThrows() {
        Maybe.just(1).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void doOnCompleteThrows() {
        Maybe.empty().doOnComplete(new Action() {

            @Override
            public void run() throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void doOnDispose() {
        final int[] call = { 0 };
        Maybe.just(1).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                call[0]++;
            }
        }).to(TestHelper.<Integer>testConsumer(true)).assertSubscribed().assertNoValues().assertNoErrors().assertNotComplete();
        assertEquals(1, call[0]);
    }

    @Test
    public void doOnDisposeThrows() {
        List<Throwable> list = TestHelper.trackPluginErrors();
        try {
            PublishProcessor<Integer> pp = PublishProcessor.create();
            TestObserverEx<Integer> to = pp.singleElement().doOnDispose(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).to(TestHelper.<Integer>testConsumer());
            assertTrue(pp.hasSubscribers());
            to.dispose();
            assertFalse(pp.hasSubscribers());
            to.assertSubscribed().assertNoValues().assertNoErrors().assertNotComplete();
            TestHelper.assertUndeliverable(list, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void observeOnDispose() throws Exception {
        final TestSubscriber<Integer> ts = new TestSubscriber<>();
        final CountDownLatch cdl = new CountDownLatch(1);
        Maybe.just(1).observeOn(Schedulers.single()).doOnSuccess(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                if (!cdl.await(5, TimeUnit.SECONDS)) {
                    throw new TimeoutException();
                }
            }
        }).toFlowable().subscribe(ts);
        Thread.sleep(250);
        ts.cancel();
        ts.awaitDone(5, TimeUnit.SECONDS).assertFailure(InterruptedException.class);
    }

    @Test
    public void doAfterTerminateSuccess() {
        final int[] call = { 0 };
        Maybe.just(1).doOnSuccess(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                call[0]++;
            }
        }).doAfterTerminate(new Action() {

            @Override
            public void run() throws Exception {
                if (call[0] == 1) {
                    call[0] = -1;
                }
            }
        }).test().assertResult(1);
        assertEquals(-1, call[0]);
    }

    @Test
    public void doAfterTerminateError() {
        final int[] call = { 0 };
        Maybe.error(new TestException()).doOnError(new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                call[0]++;
            }
        }).doAfterTerminate(new Action() {

            @Override
            public void run() throws Exception {
                if (call[0] == 1) {
                    call[0] = -1;
                }
            }
        }).test().assertFailure(TestException.class);
        assertEquals(-1, call[0]);
    }

    @Test
    public void doAfterTerminateComplete() {
        final int[] call = { 0 };
        Maybe.empty().doOnComplete(new Action() {

            @Override
            public void run() throws Exception {
                call[0]++;
            }
        }).doAfterTerminate(new Action() {

            @Override
            public void run() throws Exception {
                if (call[0] == 1) {
                    call[0] = -1;
                }
            }
        }).test().assertResult();
        assertEquals(-1, call[0]);
    }

    @Test
    public void sourceThrowsNPE() {
        try {
            Maybe.unsafeCreate(new MaybeSource<Object>() {

                @Override
                public void subscribe(MaybeObserver<? super Object> observer) {
                    throw new NullPointerException("Forced failure");
                }
            }).test();
            fail("Should have thrown!");
        } catch (NullPointerException ex) {
            assertEquals("Forced failure", ex.getMessage());
        }
    }

    @Test
    public void sourceThrowsIAE() {
        try {
            Maybe.unsafeCreate(new MaybeSource<Object>() {

                @Override
                public void subscribe(MaybeObserver<? super Object> observer) {
                    throw new IllegalArgumentException("Forced failure");
                }
            }).test();
            fail("Should have thrown!");
        } catch (NullPointerException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof IllegalArgumentException);
            assertEquals("Forced failure", ex.getCause().getMessage());
        }
    }

    @Test
    public void flatMap() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v * 10);
            }
        }).test().assertResult(10);
    }

    @Test
    public void concatMap() {
        Maybe.just(1).concatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v * 10);
            }
        }).test().assertResult(10);
    }

    @Test
    public void flatMapEmpty() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.empty();
            }
        }).test().assertResult();
    }

    @Test
    public void flatMapError() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void flatMapNotifySuccess() {
        Maybe.just(1).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v * 10);
            }
        }, new Function<Throwable, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Throwable v) throws Exception {
                return Maybe.just(100);
            }
        }, new Supplier<MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> get() throws Exception {
                return Maybe.just(200);
            }
        }).test().assertResult(10);
    }

    @Test
    public void flatMapNotifyError() {
        Maybe.<Integer>error(new TestException()).flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v * 10);
            }
        }, new Function<Throwable, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Throwable v) throws Exception {
                return Maybe.just(100);
            }
        }, new Supplier<MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> get() throws Exception {
                return Maybe.just(200);
            }
        }).test().assertResult(100);
    }

    @Test
    public void flatMapNotifyComplete() {
        Maybe.<Integer>empty().flatMap(new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v * 10);
            }
        }, new Function<Throwable, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Throwable v) throws Exception {
                return Maybe.just(100);
            }
        }, new Supplier<MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> get() throws Exception {
                return Maybe.just(200);
            }
        }).test().assertResult(200);
    }

    @Test
    public void ignoreElementSuccess() {
        Maybe.just(1).ignoreElement().test().assertResult();
    }

    @Test
    public void ignoreElementError() {
        Maybe.error(new TestException()).ignoreElement().test().assertFailure(TestException.class);
    }

    @Test
    public void ignoreElementComplete() {
        Maybe.empty().ignoreElement().test().assertResult();
    }

    @Test
    public void ignoreElementSuccessMaybe() {
        Maybe.just(1).ignoreElement().toMaybe().test().assertResult();
    }

    @Test
    public void ignoreElementErrorMaybe() {
        Maybe.error(new TestException()).ignoreElement().toMaybe().test().assertFailure(TestException.class);
    }

    @Test
    public void ignoreElementCompleteMaybe() {
        Maybe.empty().ignoreElement().toMaybe().test().assertResult();
    }

    @Test
    public void singleToMaybe() {
        Single.just(1).toMaybe().test().assertResult(1);
    }

    @Test
    public void singleToMaybeError() {
        Single.error(new TestException()).toMaybe().test().assertFailure(TestException.class);
    }

    @Test
    public void completableToMaybe() {
        Completable.complete().toMaybe().test().assertResult();
    }

    @Test
    public void completableToMaybeError() {
        Completable.error(new TestException()).toMaybe().test().assertFailure(TestException.class);
    }

    @Test
    public void emptyToSingle() {
        Maybe.empty().toSingle().test().assertFailure(NoSuchElementException.class);
    }

    @Test
    public void errorToSingle() {
        Maybe.error(new TestException()).toSingle().test().assertFailure(TestException.class);
    }

    @Test
    public void emptyToCompletable() {
        Maybe.empty().ignoreElement().test().assertResult();
    }

    @Test
    public void errorToCompletable() {
        Maybe.error(new TestException()).ignoreElement().test().assertFailure(TestException.class);
    }

    @Test
    public void concat2() {
        Maybe.concat(Maybe.just(1), Maybe.just(2)).test().assertResult(1, 2);
    }

    @Test
    public void concat2Empty() {
        Maybe.concat(Maybe.empty(), Maybe.empty()).test().assertResult();
    }

    @Test
    public void concat2Backpressured() {
        TestSubscriber<Integer> ts = Maybe.concat(Maybe.just(1), Maybe.just(2)).test(0L);
        ts.assertEmpty();
        ts.request(1);
        ts.assertValue(1);
        ts.request(1);
        ts.assertResult(1, 2);
    }

    @Test
    public void concat2BackpressuredNonEager() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Integer> ts = Maybe.concat(pp1.singleElement(), pp2.singleElement()).test(0L);
        assertTrue(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        ts.assertEmpty();
        ts.request(1);
        assertTrue(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        pp1.onNext(1);
        pp1.onComplete();
        ts.assertValue(1);
        assertFalse(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        ts.request(1);
        ts.assertValue(1);
        pp2.onNext(2);
        pp2.onComplete();
        ts.assertResult(1, 2);
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
    }

    @Test
    public void concat3() {
        Maybe.concat(Maybe.just(1), Maybe.just(2), Maybe.just(3)).test().assertResult(1, 2, 3);
    }

    @Test
    public void concat3Empty() {
        Maybe.concat(Maybe.empty(), Maybe.empty(), Maybe.empty()).test().assertResult();
    }

    @Test
    public void concat3Mixed1() {
        Maybe.concat(Maybe.just(1), Maybe.empty(), Maybe.just(3)).test().assertResult(1, 3);
    }

    @Test
    public void concat3Mixed2() {
        Maybe.concat(Maybe.just(1), Maybe.just(2), Maybe.empty()).test().assertResult(1, 2);
    }

    @Test
    public void concat3Backpressured() {
        TestSubscriber<Integer> ts = Maybe.concat(Maybe.just(1), Maybe.just(2), Maybe.just(3)).test(0L);
        ts.assertEmpty();
        ts.request(1);
        ts.assertValue(1);
        ts.request(2);
        ts.assertResult(1, 2, 3);
    }

    @Test
    public void concatArrayZero() {
        assertSame(Flowable.empty(), Maybe.concatArray());
    }

    @Test
    public void concatArrayOne() {
        Maybe.concatArray(Maybe.just(1)).test().assertResult(1);
    }

    @Test
    public void concat4() {
        Maybe.concat(Maybe.just(1), Maybe.just(2), Maybe.just(3), Maybe.just(4)).test().assertResult(1, 2, 3, 4);
    }

    @Test
    public void concatIterable() {
        Maybe.concat(Arrays.asList(Maybe.just(1), Maybe.just(2))).test().assertResult(1, 2);
    }

    @Test
    public void concatIterableEmpty() {
        Maybe.concat(Arrays.asList(Maybe.empty(), Maybe.empty())).test().assertResult();
    }

    @Test
    public void concatIterableBackpressured() {
        TestSubscriber<Integer> ts = Maybe.concat(Arrays.asList(Maybe.just(1), Maybe.just(2))).test(0L);
        ts.assertEmpty();
        ts.request(1);
        ts.assertValue(1);
        ts.request(1);
        ts.assertResult(1, 2);
    }

    @Test
    public void concatIterableBackpressuredNonEager() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Integer> ts = Maybe.concat(Arrays.asList(pp1.singleElement(), pp2.singleElement())).test(0L);
        assertTrue(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        ts.assertEmpty();
        ts.request(1);
        assertTrue(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        pp1.onNext(1);
        pp1.onComplete();
        ts.assertValue(1);
        assertFalse(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        ts.request(1);
        ts.assertValue(1);
        pp2.onNext(2);
        pp2.onComplete();
        ts.assertResult(1, 2);
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
    }

    @Test
    public void concatIterableZero() {
        Maybe.concat(Collections.<Maybe<Integer>>emptyList()).test().assertResult();
    }

    @Test
    public void concatIterableOne() {
        Maybe.concat(Collections.<Maybe<Integer>>singleton(Maybe.just(1))).test().assertResult(1);
    }

    @Test
    public void concatPublisher() {
        Maybe.concat(Flowable.just(Maybe.just(1), Maybe.just(2))).test().assertResult(1, 2);
    }

    @Test
    public void concatPublisherPrefetch() {
        Maybe.concat(Flowable.just(Maybe.just(1), Maybe.just(2)), 1).test().assertResult(1, 2);
    }

    @Test
    public void basic() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable d = Disposable.empty();
            Maybe.<Integer>create(new MaybeOnSubscribe<Integer>() {

                @Override
                public void subscribe(MaybeEmitter<Integer> e) throws Exception {
                    e.setDisposable(d);
                    e.onSuccess(1);
                    e.onError(new TestException());
                    e.onSuccess(2);
                    e.onError(new TestException());
                    e.onComplete();
                }
            }).test().assertResult(1);
            assertTrue(d.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            TestHelper.assertUndeliverable(errors, 1, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void basicWithError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable d = Disposable.empty();
            Maybe.<Integer>create(new MaybeOnSubscribe<Integer>() {

                @Override
                public void subscribe(MaybeEmitter<Integer> e) throws Exception {
                    e.setDisposable(d);
                    e.onError(new TestException());
                    e.onSuccess(2);
                    e.onError(new TestException());
                    e.onComplete();
                }
            }).test().assertFailure(TestException.class);
            assertTrue(d.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void basicWithComplete() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable d = Disposable.empty();
            Maybe.<Integer>create(new MaybeOnSubscribe<Integer>() {

                @Override
                public void subscribe(MaybeEmitter<Integer> e) throws Exception {
                    e.setDisposable(d);
                    e.onComplete();
                    e.onSuccess(1);
                    e.onError(new TestException());
                    e.onComplete();
                    e.onSuccess(2);
                    e.onError(new TestException());
                }
            }).test().assertResult();
            assertTrue(d.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
            TestHelper.assertUndeliverable(errors, 1, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void unsafeCreateWithMaybe() {
        Maybe.unsafeCreate(Maybe.just(1));
    }

    @Test
    public void maybeToPublisherEnum() {
        TestHelper.checkEnum(MaybeToPublisher.class);
    }

    @Test
    public void ambArrayOneIsNull() {
        Maybe.ambArray(null, Maybe.just(1)).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambArrayEmpty() {
        assertSame(Maybe.empty(), Maybe.ambArray());
    }

    @Test
    public void ambArrayOne() {
        assertSame(Maybe.never(), Maybe.ambArray(Maybe.never()));
    }

    @Test
    public void ambWithOrder() {
        Maybe<Integer> error = Maybe.error(new RuntimeException());
        Maybe.just(1).ambWith(error).test().assertValue(1);
    }

    @Test
    public void ambIterableOrder() {
        Maybe<Integer> error = Maybe.error(new RuntimeException());
        Maybe.amb(Arrays.asList(Maybe.just(1), error)).test().assertValue(1);
    }

    @Test
    public void ambArrayOrder() {
        Maybe<Integer> error = Maybe.error(new RuntimeException());
        Maybe.ambArray(Maybe.just(1), error).test().assertValue(1);
    }

    @Test
    public void ambArray1SignalsSuccess() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.ambArray(pp1.singleElement(), pp2.singleElement()).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onNext(1);
        pp1.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult(1);
    }

    @Test
    public void ambArray2SignalsSuccess() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.ambArray(pp1.singleElement(), pp2.singleElement()).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onNext(2);
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult(2);
    }

    @Test
    public void ambArray1SignalsError() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.ambArray(pp1.singleElement(), pp2.singleElement()).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onError(new TestException());
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void ambArray2SignalsError() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.ambArray(pp1.singleElement(), pp2.singleElement()).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onError(new TestException());
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void ambArray1SignalsComplete() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.ambArray(pp1.singleElement(), pp2.singleElement()).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void ambArray2SignalsComplete() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.ambArray(pp1.singleElement(), pp2.singleElement()).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void ambIterable1SignalsSuccess() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.amb(Arrays.asList(pp1.singleElement(), pp2.singleElement())).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onNext(1);
        pp1.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult(1);
    }

    @Test
    public void ambIterable2SignalsSuccess() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.amb(Arrays.asList(pp1.singleElement(), pp2.singleElement())).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onNext(2);
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult(2);
    }

    @Test
    public void ambIterable2SignalsSuccessWithOverlap() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.amb(Arrays.asList(pp1.singleElement(), pp2.singleElement())).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onNext(2);
        pp1.onNext(1);
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult(2);
    }

    @Test
    public void ambIterable1SignalsError() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.amb(Arrays.asList(pp1.singleElement(), pp2.singleElement())).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onError(new TestException());
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void ambIterable2SignalsError() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.amb(Arrays.asList(pp1.singleElement(), pp2.singleElement())).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onError(new TestException());
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailure(TestException.class);
    }

    @Test
    public void ambIterable2SignalsErrorWithOverlap() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserverEx<Integer> to = Maybe.amb(Arrays.asList(pp1.singleElement(), pp2.singleElement())).to(TestHelper.<Integer>testConsumer());
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onError(new TestException("2"));
        pp1.onError(new TestException("1"));
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertFailureAndMessage(TestException.class, "2");
    }

    @Test
    public void ambIterable1SignalsComplete() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.amb(Arrays.asList(pp1.singleElement(), pp2.singleElement())).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void ambIterable2SignalsComplete() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = Maybe.amb(Arrays.asList(pp1.singleElement(), pp2.singleElement())).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult();
    }

    @Test
    public void ambIterableIteratorNull() {
        Maybe.amb(new Iterable<Maybe<Object>>() {

            @Override
            public Iterator<Maybe<Object>> iterator() {
                return null;
            }
        }).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambIterableOneIsNull() {
        Maybe.amb(Arrays.asList(null, Maybe.just(1))).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambIterableEmpty() {
        Maybe.amb(Collections.<Maybe<Integer>>emptyList()).test().assertResult();
    }

    @Test
    public void ambIterableOne() {
        Maybe.amb(Collections.singleton(Maybe.just(1))).test().assertResult(1);
    }

    @Test
    public void mergeArray() {
        Maybe.mergeArray(Maybe.just(1), Maybe.just(2), Maybe.just(3)).test().assertResult(1, 2, 3);
    }

    @Test
    public void merge2() {
        Maybe.merge(Maybe.just(1), Maybe.just(2)).test().assertResult(1, 2);
    }

    @Test
    public void merge3() {
        Maybe.merge(Maybe.just(1), Maybe.just(2), Maybe.just(3)).test().assertResult(1, 2, 3);
    }

    @Test
    public void merge4() {
        Maybe.merge(Maybe.just(1), Maybe.just(2), Maybe.just(3), Maybe.just(4)).test().assertResult(1, 2, 3, 4);
    }

    @Test
    public void merge4Take2() {
        Maybe.merge(Maybe.just(1), Maybe.just(2), Maybe.just(3), Maybe.just(4)).take(2).test().assertResult(1, 2);
    }

    @Test
    public void mergeArrayBackpressured() {
        TestSubscriber<Integer> ts = Maybe.mergeArray(Maybe.just(1), Maybe.just(2), Maybe.just(3)).test(0L);
        ts.assertEmpty();
        ts.request(1);
        ts.assertValue(1);
        ts.request(1);
        ts.assertValues(1, 2);
        ts.request(1);
        ts.assertResult(1, 2, 3);
    }

    @Test
    public void mergeArrayBackpressuredMixed1() {
        TestSubscriber<Integer> ts = Maybe.mergeArray(Maybe.just(1), Maybe.<Integer>empty(), Maybe.just(3)).test(0L);
        ts.assertEmpty();
        ts.request(1);
        ts.assertValue(1);
        ts.request(1);
        ts.assertResult(1, 3);
    }

    @Test
    public void mergeArrayBackpressuredMixed2() {
        TestSubscriber<Integer> ts = Maybe.mergeArray(Maybe.just(1), Maybe.just(2), Maybe.<Integer>empty()).test(0L);
        ts.assertEmpty();
        ts.request(1);
        ts.assertValue(1);
        ts.request(1);
        ts.assertResult(1, 2);
    }

    @Test
    public void mergeArrayBackpressuredMixed3() {
        TestSubscriber<Integer> ts = Maybe.mergeArray(Maybe.<Integer>empty(), Maybe.just(2), Maybe.just(3)).test(0L);
        ts.assertEmpty();
        ts.request(1);
        ts.assertValue(2);
        ts.request(1);
        ts.assertResult(2, 3);
    }

    @Test
    public void mergeArrayFused() {
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Maybe.mergeArray(Maybe.just(1), Maybe.just(2), Maybe.just(3)).subscribe(ts);
        ts.assertSubscribed().assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertResult(1, 2, 3);
    }

    @Test
    public void mergeArrayFusedRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Integer> pp1 = PublishProcessor.create();
            final PublishProcessor<Integer> pp2 = PublishProcessor.create();
            TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
            Maybe.mergeArray(pp1.singleElement(), pp2.singleElement()).subscribe(ts);
            ts.assertSubscribed().assertFuseable().assertFusionMode(QueueFuseable.ASYNC);
            TestHelper.race(new Runnable() {

                @Override
                public void run() {
                    pp1.onNext(1);
                    pp1.onComplete();
                }
            }, new Runnable() {

                @Override
                public void run() {
                    pp2.onNext(1);
                    pp2.onComplete();
                }
            });
            ts.awaitDone(5, TimeUnit.SECONDS).assertResult(1, 1);
        }
    }

    @Test
    public void mergeArrayZero() {
        assertSame(Flowable.empty(), Maybe.mergeArray());
    }

    @Test
    public void mergeArrayOne() {
        Maybe.mergeArray(Maybe.just(1)).test().assertResult(1);
    }

    @Test
    public void mergePublisher() {
        Maybe.merge(Flowable.just(Maybe.just(1), Maybe.just(2), Maybe.just(3))).test().assertResult(1, 2, 3);
    }

    @Test
    public void mergePublisherMaxConcurrent() {
        final PublishProcessor<Integer> pp1 = PublishProcessor.create();
        final PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Integer> ts = Maybe.merge(Flowable.just(pp1.singleElement(), pp2.singleElement()), 1).test(0L);
        assertTrue(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        pp1.onNext(1);
        pp1.onComplete();
        ts.request(1);
        assertFalse(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
    }

    @Test
    public void mergeMaybe() {
        Maybe.merge(Maybe.just(Maybe.just(1))).test().assertResult(1);
    }

    @Test
    public void mergeIterable() {
        Maybe.merge(Arrays.asList(Maybe.just(1), Maybe.just(2), Maybe.just(3))).test().assertResult(1, 2, 3);
    }

    @Test
    public void mergeALot() {
        @SuppressWarnings("unchecked")
        Maybe<Integer>[] sources = new Maybe[Flowable.bufferSize() * 2];
        Arrays.fill(sources, Maybe.just(1));
        Maybe.mergeArray(sources).to(TestHelper.<Integer>testConsumer()).assertSubscribed().assertValueCount(sources.length).assertNoErrors().assertComplete();
    }

    @Test
    public void mergeALotLastEmpty() {
        @SuppressWarnings("unchecked")
        Maybe<Integer>[] sources = new Maybe[Flowable.bufferSize() * 2];
        Arrays.fill(sources, Maybe.just(1));
        sources[sources.length - 1] = Maybe.empty();
        Maybe.mergeArray(sources).to(TestHelper.<Integer>testConsumer()).assertSubscribed().assertValueCount(sources.length - 1).assertNoErrors().assertComplete();
    }

    @Test
    public void mergeALotFused() {
        @SuppressWarnings("unchecked")
        Maybe<Integer>[] sources = new Maybe[Flowable.bufferSize() * 2];
        Arrays.fill(sources, Maybe.just(1));
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<Integer>().setInitialFusionMode(QueueFuseable.ANY);
        Maybe.mergeArray(sources).subscribe(ts);
        ts.assertSubscribed().assertFuseable().assertFusionMode(QueueFuseable.ASYNC).assertValueCount(sources.length).assertNoErrors().assertComplete();
    }

    @Test
    public void mergeErrorSuccess() {
        Maybe.merge(Maybe.error(new TestException()), Maybe.just(1)).test().assertFailure(TestException.class);
    }

    @Test
    public void mergeSuccessError() {
        Maybe.merge(Maybe.just(1), Maybe.error(new TestException())).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void subscribeZero() {
        assertTrue(Maybe.just(1).subscribe().isDisposed());
    }

    @Test
    public void subscribeZeroError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            assertTrue(Maybe.error(new TestException()).subscribe().isDisposed());
            TestHelper.assertError(errors, 0, OnErrorNotImplementedException.class);
            Throwable c = errors.get(0).getCause();
            assertTrue("" + c, c instanceof TestException);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void subscribeToOnSuccess() {
        final List<Integer> values = new ArrayList<>();
        Consumer<Integer> onSuccess = new Consumer<Integer>() {

            @Override
            public void accept(Integer e) throws Exception {
                values.add(e);
            }
        };
        Maybe<Integer> source = Maybe.just(1);
        source.subscribe(onSuccess);
        source.subscribe(onSuccess, Functions.emptyConsumer());
        source.subscribe(onSuccess, Functions.emptyConsumer(), Functions.EMPTY_ACTION);
        assertEquals(Arrays.asList(1, 1, 1), values);
    }

    @Test
    public void subscribeToOnError() {
        final List<Throwable> values = new ArrayList<>();
        Consumer<Throwable> onError = new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                values.add(e);
            }
        };
        TestException ex = new TestException();
        Maybe<Integer> source = Maybe.error(ex);
        source.subscribe(Functions.emptyConsumer(), onError);
        source.subscribe(Functions.emptyConsumer(), onError, Functions.EMPTY_ACTION);
        assertEquals(Arrays.asList(ex, ex), values);
    }

    @Test
    public void subscribeToOnComplete() {
        final List<Integer> values = new ArrayList<>();
        Action onComplete = new Action() {

            @Override
            public void run() throws Exception {
                values.add(100);
            }
        };
        Maybe<Integer> source = Maybe.empty();
        source.subscribe(Functions.emptyConsumer(), Functions.emptyConsumer(), onComplete);
        assertEquals(Arrays.asList(100), values);
    }

    @Test
    public void subscribeWith() {
        MaybeObserver<Integer> mo = new MaybeObserver<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Integer value) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
        assertSame(mo, Maybe.just(1).subscribeWith(mo));
    }

    @Test
    public void doOnEventSuccess() {
        final List<Object> list = new ArrayList<>();
        assertTrue(Maybe.just(1).doOnEvent(new BiConsumer<Integer, Throwable>() {

            @Override
            public void accept(Integer v, Throwable e) throws Exception {
                list.add(v);
                list.add(e);
            }
        }).subscribe().isDisposed());
        assertEquals(Arrays.asList(1, null), list);
    }

    @Test
    public void doOnEventError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final List<Object> list = new ArrayList<>();
            TestException ex = new TestException();
            assertTrue(Maybe.<Integer>error(ex).doOnEvent(new BiConsumer<Integer, Throwable>() {

                @Override
                public void accept(Integer v, Throwable e) throws Exception {
                    list.add(v);
                    list.add(e);
                }
            }).subscribe().isDisposed());
            assertEquals(Arrays.asList(null, ex), list);
            TestHelper.assertError(errors, 0, OnErrorNotImplementedException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doOnEventComplete() {
        final List<Object> list = new ArrayList<>();
        assertTrue(Maybe.<Integer>empty().doOnEvent(new BiConsumer<Integer, Throwable>() {

            @Override
            public void accept(Integer v, Throwable e) throws Exception {
                list.add(v);
                list.add(e);
            }
        }).subscribe().isDisposed());
        assertEquals(Arrays.asList(null, null), list);
    }

    @Test
    public void doOnEventSuccessThrows() {
        Maybe.just(1).doOnEvent(new BiConsumer<Integer, Throwable>() {

            @Override
            public void accept(Integer v, Throwable e) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void doOnEventErrorThrows() {
        TestObserverEx<Integer> to = Maybe.<Integer>error(new TestException("Outer")).doOnEvent(new BiConsumer<Integer, Throwable>() {

            @Override
            public void accept(Integer v, Throwable e) throws Exception {
                throw new TestException("Inner");
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> list = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(list, 0, TestException.class, "Outer");
        TestHelper.assertError(list, 1, TestException.class, "Inner");
        assertEquals(2, list.size());
    }

    @Test
    public void doOnEventCompleteThrows() {
        Maybe.<Integer>empty().doOnEvent(new BiConsumer<Integer, Throwable>() {

            @Override
            public void accept(Integer v, Throwable e) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void concatArrayDelayError() {
        Maybe.concatArrayDelayError(Maybe.empty(), Maybe.just(1), Maybe.error(new TestException())).test().assertFailure(TestException.class, 1);
        Maybe.concatArrayDelayError(Maybe.error(new TestException()), Maybe.empty(), Maybe.just(1)).test().assertFailure(TestException.class, 1);
        assertSame(Flowable.empty(), Maybe.concatArrayDelayError());
        assertFalse(Maybe.concatArrayDelayError(Maybe.never()) instanceof MaybeConcatArrayDelayError);
    }

    @Test
    public void concatIterableDelayError() {
        Maybe.concatDelayError(Arrays.asList(Maybe.empty(), Maybe.just(1), Maybe.error(new TestException()))).test().assertFailure(TestException.class, 1);
        Maybe.concatDelayError(Arrays.asList(Maybe.error(new TestException()), Maybe.empty(), Maybe.just(1))).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void concatPublisherDelayError() {
        Maybe.concatDelayError(Flowable.just(Maybe.empty(), Maybe.just(1), Maybe.error(new TestException()))).test().assertFailure(TestException.class, 1);
        Maybe.concatDelayError(Flowable.just(Maybe.error(new TestException()), Maybe.empty(), Maybe.just(1))).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void concatPublisherDelayErrorPrefetch() {
        Maybe.concatDelayError(Flowable.just(Maybe.empty(), Maybe.just(1), Maybe.error(new TestException())), 1).test().assertFailure(TestException.class, 1);
        Maybe.concatDelayError(Flowable.just(Maybe.error(new TestException()), Maybe.empty(), Maybe.just(1)), 1).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void concatEagerArray() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Integer> ts = Maybe.concatArrayEager(pp1.singleElement(), pp2.singleElement()).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onNext(2);
        pp2.onComplete();
        ts.assertEmpty();
        pp1.onNext(1);
        pp1.onComplete();
        ts.assertResult(1, 2);
    }

    @Test
    public void concatEagerIterable() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Integer> ts = Maybe.concatEager(Arrays.asList(pp1.singleElement(), pp2.singleElement())).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onNext(2);
        pp2.onComplete();
        ts.assertEmpty();
        pp1.onNext(1);
        pp1.onComplete();
        ts.assertResult(1, 2);
    }

    @Test
    public void concatEagerPublisher() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Integer> ts = Maybe.concatEager(Flowable.just(pp1.singleElement(), pp2.singleElement())).test();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onNext(2);
        pp2.onComplete();
        ts.assertEmpty();
        pp1.onNext(1);
        pp1.onComplete();
        ts.assertResult(1, 2);
    }

    static Future<Integer> emptyFuture() {
        final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        return exec.schedule(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                exec.shutdown();
                return null;
            }
        }, 200, TimeUnit.MILLISECONDS);
    }

    @Test
    public void fromFuture() {
        Maybe.fromFuture(Flowable.just(1).delay(200, TimeUnit.MILLISECONDS).toFuture()).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
        Maybe.fromFuture(emptyFuture()).test().awaitDone(5, TimeUnit.SECONDS).assertResult();
        Maybe.fromFuture(Flowable.error(new TestException()).delay(200, TimeUnit.MILLISECONDS, true).toFuture()).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TestException.class);
        Maybe.fromFuture(Flowable.empty().delay(10, TimeUnit.SECONDS).toFuture(), 100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TimeoutException.class);
    }

    @Test
    public void mergeArrayDelayError() {
        Maybe.mergeArrayDelayError(Maybe.empty(), Maybe.just(1), Maybe.error(new TestException())).test().assertFailure(TestException.class, 1);
        Maybe.mergeArrayDelayError(Maybe.error(new TestException()), Maybe.empty(), Maybe.just(1)).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void mergeIterableDelayError() {
        Maybe.mergeDelayError(Arrays.asList(Maybe.empty(), Maybe.just(1), Maybe.error(new TestException()))).test().assertFailure(TestException.class, 1);
        Maybe.mergeDelayError(Arrays.asList(Maybe.error(new TestException()), Maybe.empty(), Maybe.just(1))).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void mergePublisherDelayError() {
        Maybe.mergeDelayError(Flowable.just(Maybe.empty(), Maybe.just(1), Maybe.error(new TestException()))).test().assertFailure(TestException.class, 1);
        Maybe.mergeDelayError(Flowable.just(Maybe.error(new TestException()), Maybe.empty(), Maybe.just(1))).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void mergeDelayError2() {
        Maybe.mergeDelayError(Maybe.just(1), Maybe.error(new TestException())).test().assertFailure(TestException.class, 1);
        Maybe.mergeDelayError(Maybe.error(new TestException()), Maybe.just(1)).test().assertFailure(TestException.class, 1);
    }

    @Test
    public void mergeDelayError3() {
        Maybe.mergeDelayError(Maybe.just(1), Maybe.error(new TestException()), Maybe.just(2)).test().assertFailure(TestException.class, 1, 2);
        Maybe.mergeDelayError(Maybe.error(new TestException()), Maybe.just(1), Maybe.just(2)).test().assertFailure(TestException.class, 1, 2);
        Maybe.mergeDelayError(Maybe.just(1), Maybe.just(2), Maybe.error(new TestException())).test().assertFailure(TestException.class, 1, 2);
    }

    @Test
    public void mergeDelayError4() {
        Maybe.mergeDelayError(Maybe.just(1), Maybe.error(new TestException()), Maybe.just(2), Maybe.just(3)).test().assertFailure(TestException.class, 1, 2, 3);
        Maybe.mergeDelayError(Maybe.error(new TestException()), Maybe.just(1), Maybe.just(2), Maybe.just(3)).test().assertFailure(TestException.class, 1, 2, 3);
        Maybe.mergeDelayError(Maybe.just(1), Maybe.just(2), Maybe.just(3), Maybe.error(new TestException())).test().assertFailure(TestException.class, 1, 2, 3);
    }

    @Test
    public void sequenceEqual() {
        Maybe.sequenceEqual(Maybe.just(1_000_000), Maybe.just(Integer.valueOf(1_000_000))).test().assertResult(true);
        Maybe.sequenceEqual(Maybe.just(1), Maybe.just(2)).test().assertResult(false);
        Maybe.sequenceEqual(Maybe.just(1), Maybe.empty()).test().assertResult(false);
        Maybe.sequenceEqual(Maybe.empty(), Maybe.just(2)).test().assertResult(false);
        Maybe.sequenceEqual(Maybe.empty(), Maybe.empty()).test().assertResult(true);
        Maybe.sequenceEqual(Maybe.just(1), Maybe.error(new TestException())).test().assertFailure(TestException.class);
        Maybe.sequenceEqual(Maybe.error(new TestException()), Maybe.just(1)).test().assertFailure(TestException.class);
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Maybe.sequenceEqual(Maybe.error(new TestException("One")), Maybe.error(new TestException("Two"))).to(TestHelper.<Boolean>testConsumer()).assertFailureAndMessage(TestException.class, "One");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Two");
        } finally {
            RxJavaPlugins.reset();
        }
        Maybe.sequenceEqual(Maybe.just(1), Maybe.error(new TestException()), new BiPredicate<Object, Object>() {

            @Override
            public boolean test(Object t1, Object t2) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void timer() {
        Maybe.timer(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(0L);
    }

    @Test
    public void blockingGet() {
        assertEquals(1, Maybe.just(1).blockingGet().intValue());
        assertEquals(100, Maybe.empty().blockingGet(100));
        try {
            Maybe.error(new TestException()).blockingGet();
            fail("Should have thrown!");
        } catch (TestException ex) {
        // expected
        }
        try {
            Maybe.error(new TestException()).blockingGet(100);
            fail("Should have thrown!");
        } catch (TestException ex) {
        // expected
        }
    }

    @Test
    public void flatMapContinuation() {
        Maybe.just(1).flatMapCompletable(new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer v) throws Exception {
                return Completable.complete();
            }
        }).test().assertResult();
        Maybe.just(1).flatMapCompletable(new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer v) throws Exception {
                return Completable.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
        Maybe.just(1).flatMapPublisher(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.range(1, 5);
            }
        }).test().assertResult(1, 2, 3, 4, 5);
        Maybe.just(1).flatMapPublisher(new Function<Integer, Publisher<Integer>>() {

            @Override
            public Publisher<Integer> apply(Integer v) throws Exception {
                return Flowable.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
        Maybe.just(1).flatMapObservable(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer v) throws Exception {
                return Observable.range(1, 5);
            }
        }).test().assertResult(1, 2, 3, 4, 5);
        Maybe.just(1).flatMapObservable(new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer v) throws Exception {
                return Observable.error(new TestException());
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void using() {
        final AtomicInteger disposeCount = new AtomicInteger();
        Maybe.using(Functions.justSupplier(1), new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }, new Consumer<Integer>() {

            @Override
            public void accept(Integer d) throws Exception {
                disposeCount.set(d);
            }
        }).map(new Function<Integer, Object>() {

            @Override
            public String apply(Integer v) throws Exception {
                return "" + disposeCount.get() + v * 10;
            }
        }).test().assertResult("110");
    }

    @Test
    public void usingNonEager() {
        final AtomicInteger disposeCount = new AtomicInteger();
        Maybe.using(Functions.justSupplier(1), new Function<Integer, MaybeSource<Integer>>() {

            @Override
            public MaybeSource<Integer> apply(Integer v) throws Exception {
                return Maybe.just(v);
            }
        }, new Consumer<Integer>() {

            @Override
            public void accept(Integer d) throws Exception {
                disposeCount.set(d);
            }
        }, false).map(new Function<Integer, Object>() {

            @Override
            public String apply(Integer v) throws Exception {
                return "" + disposeCount.get() + v * 10;
            }
        }).test().assertResult("010");
        assertEquals(1, disposeCount.get());
    }

    Function<Object[], String> arrayToString = new Function<Object[], String>() {

        @Override
        public String apply(Object[] a) throws Exception {
            return Arrays.toString(a);
        }
    };

    @SuppressWarnings("unchecked")
    @Test
    public void zipArray() {
        Maybe.zipArray(arrayToString, Maybe.just(1), Maybe.just(2)).test().assertResult("[1, 2]");
        Maybe.zipArray(arrayToString, Maybe.just(1), Maybe.empty()).test().assertResult();
        Maybe.zipArray(arrayToString, Maybe.just(1), Maybe.error(new TestException())).test().assertFailure(TestException.class);
        assertSame(Maybe.empty(), Maybe.zipArray(ArgsToString.INSTANCE));
        Maybe.zipArray(arrayToString, Maybe.just(1)).test().assertResult("[1]");
    }

    @Test
    public void zipIterable() {
        Maybe.zip(Arrays.asList(Maybe.just(1), Maybe.just(2)), arrayToString).test().assertResult("[1, 2]");
        Maybe.zip(Collections.<Maybe<Integer>>emptyList(), arrayToString).test().assertResult();
        Maybe.zip(Collections.singletonList(Maybe.just(1)), arrayToString).test().assertResult("[1]");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void zip2() {
        Maybe.zip(Maybe.just(1), Maybe.just(2), ArgsToString.INSTANCE).test().assertResult("12");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void zipWith() {
        Maybe.just(1).zipWith(Maybe.just(2), ArgsToString.INSTANCE).test().assertResult("12");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void zip3() {
        Maybe.zip(Maybe.just(1), Maybe.just(2), Maybe.just(3), ArgsToString.INSTANCE).test().assertResult("123");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void zip4() {
        Maybe.zip(Maybe.just(1), Maybe.just(2), Maybe.just(3), Maybe.just(4), ArgsToString.INSTANCE).test().assertResult("1234");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void zip5() {
        Maybe.zip(Maybe.just(1), Maybe.just(2), Maybe.just(3), Maybe.just(4), Maybe.just(5), ArgsToString.INSTANCE).test().assertResult("12345");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void zip6() {
        Maybe.zip(Maybe.just(1), Maybe.just(2), Maybe.just(3), Maybe.just(4), Maybe.just(5), Maybe.just(6), ArgsToString.INSTANCE).test().assertResult("123456");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void zip7() {
        Maybe.zip(Maybe.just(1), Maybe.just(2), Maybe.just(3), Maybe.just(4), Maybe.just(5), Maybe.just(6), Maybe.just(7), ArgsToString.INSTANCE).test().assertResult("1234567");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void zip8() {
        Maybe.zip(Maybe.just(1), Maybe.just(2), Maybe.just(3), Maybe.just(4), Maybe.just(5), Maybe.just(6), Maybe.just(7), Maybe.just(8), ArgsToString.INSTANCE).test().assertResult("12345678");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void zip9() {
        Maybe.zip(Maybe.just(1), Maybe.just(2), Maybe.just(3), Maybe.just(4), Maybe.just(5), Maybe.just(6), Maybe.just(7), Maybe.just(8), Maybe.just(9), ArgsToString.INSTANCE).test().assertResult("123456789");
    }

    @Test
    public void ambWith1SignalsSuccess() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().ambWith(pp2.singleElement()).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp1.onNext(1);
        pp1.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult(1);
    }

    @Test
    public void ambWith2SignalsSuccess() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestObserver<Integer> to = pp1.singleElement().ambWith(pp2.singleElement()).test();
        to.assertEmpty();
        assertTrue(pp1.hasSubscribers());
        assertTrue(pp2.hasSubscribers());
        pp2.onNext(2);
        pp2.onComplete();
        assertFalse(pp1.hasSubscribers());
        assertFalse(pp2.hasSubscribers());
        to.assertResult(2);
    }

    @Test
    public void zipIterableObject() {
        final List<Maybe<Integer>> maybes = Arrays.asList(Maybe.just(1), Maybe.just(4));
        Maybe.zip(maybes, new Function<Object[], Object>() {

            @Override
            public Object apply(final Object[] o) throws Exception {
                int sum = 0;
                for (Object i : o) {
                    sum += (Integer) i;
                }
                return sum;
            }
        }).test().assertResult(5);
    }

    static long usedMemoryNow() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        return heapMemoryUsage.getUsed();
    }

    @Test
    public void onTerminateDetach() throws Exception {
        System.gc();
        Thread.sleep(150);
        long before = usedMemoryNow();
        Maybe<Object> source = Flowable.just((Object) new Object[10000000]).singleElement();
        long middle = usedMemoryNow();
        MaybeObserver<Object> observer = new MaybeObserver<Object>() {

            @SuppressWarnings("unused")
            Disposable u;

            @Override
            public void onSubscribe(Disposable d) {
                this.u = d;
            }

            @Override
            public void onSuccess(Object value) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
        source.onTerminateDetach().subscribe(observer);
        source = null;
        System.gc();
        Thread.sleep(250);
        long after = usedMemoryNow();
        String log = String.format("%.2f MB -> %.2f MB -> %.2f MB%n", before / 1024.0 / 1024.0, middle / 1024.0 / 1024.0, after / 1024.0 / 1024.0);
        // System.out.printf(log);
        if (before * 1.3 < after) {
            fail("There seems to be a memory leak: " + log);
        }
        // hold onto the reference to prevent premature GC
        assertNotNull(observer);
    }

    @Test
    public void repeat() {
        Maybe.just(1).repeat().take(5).test().assertResult(1, 1, 1, 1, 1);
        Maybe.just(1).repeat(5).test().assertResult(1, 1, 1, 1, 1);
        Maybe.just(1).repeatUntil(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return false;
            }
        }).take(5).test().assertResult(1, 1, 1, 1, 1);
        Maybe.just(1).repeatWhen(new Function<Flowable<Object>, Publisher<Object>>() {

            @Override
            public Publisher<Object> apply(Flowable<Object> v) throws Exception {
                return v;
            }
        }).take(5).test().assertResult(1, 1, 1, 1, 1);
    }

    @Test
    public void retry() {
        Maybe.just(1).retry().test().assertResult(1);
        Maybe.just(1).retry(5).test().assertResult(1);
        Maybe.just(1).retry(Functions.alwaysTrue()).test().assertResult(1);
        Maybe.just(1).retry(5, Functions.alwaysTrue()).test().assertResult(1);
        Maybe.just(1).retry(new BiPredicate<Integer, Throwable>() {

            @Override
            public boolean test(Integer a, Throwable e) throws Exception {
                return true;
            }
        }).test().assertResult(1);
        Maybe.just(1).retryUntil(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return false;
            }
        }).test().assertResult(1);
        Maybe.just(1).retryWhen(new Function<Flowable<? extends Throwable>, Publisher<Object>>() {

            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public Publisher<Object> apply(Flowable<? extends Throwable> v) throws Exception {
                return (Publisher) v;
            }
        }).test().assertResult(1);
        final AtomicInteger calls = new AtomicInteger();
        try {
            Maybe.error(new Supplier<Throwable>() {

                @Override
                public Throwable get() {
                    calls.incrementAndGet();
                    return new TestException();
                }
            }).retry(5).test();
        } finally {
            assertEquals(6, calls.get());
        }
    }

    @Test
    public void onErrorResumeWithEmpty() {
        Maybe.empty().onErrorResumeWith(Maybe.just(1)).test().assertNoValues().assertNoErrors().assertComplete();
    }

    @Test
    public void onErrorResumeWithValue() {
        Maybe.just(1).onErrorResumeWith(Maybe.<Integer>empty()).test().assertNoErrors().assertValue(1);
    }

    @Test
    public void onErrorResumeWithError() {
        Maybe.error(new RuntimeException("some error")).onErrorResumeWith(Maybe.empty()).test().assertNoValues().assertNoErrors().assertComplete();
    }

    @Test
    public void valueConcatWithValue() {
        Maybe.just(1).concatWith(Maybe.just(2)).test().assertNoErrors().assertComplete().assertValues(1, 2);
    }

    @Test
    public void errorConcatWithValue() {
        Maybe.<Integer>error(new RuntimeException("error")).concatWith(Maybe.just(2)).to(TestHelper.<Integer>testConsumer()).assertError(RuntimeException.class).assertErrorMessage("error").assertNoValues();
    }

    @Test
    public void valueConcatWithError() {
        Maybe.just(1).concatWith(Maybe.<Integer>error(new RuntimeException("error"))).to(TestHelper.<Integer>testConsumer()).assertValue(1).assertError(RuntimeException.class).assertErrorMessage("error");
    }

    @Test
    public void emptyConcatWithValue() {
        Maybe.<Integer>empty().concatWith(Maybe.just(2)).test().assertNoErrors().assertComplete().assertValues(2);
    }

    @Test
    public void emptyConcatWithError() {
        Maybe.<Integer>empty().concatWith(Maybe.<Integer>error(new RuntimeException("error"))).to(TestHelper.<Integer>testConsumer()).assertNoValues().assertError(RuntimeException.class).assertErrorMessage("error");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private MaybeTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFlowableEmpty() throws java.lang.Throwable {
            this.payloads.fromFlowableEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFlowableJust() throws java.lang.Throwable {
            this.payloads.fromFlowableJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFlowableError() throws java.lang.Throwable {
            this.payloads.fromFlowableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFlowableValueAndError() throws java.lang.Throwable {
            this.payloads.fromFlowableValueAndError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFlowableMany() throws java.lang.Throwable {
            this.payloads.fromFlowableMany.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFlowableDisposeComposesThrough() throws java.lang.Throwable {
            this.payloads.fromFlowableDisposeComposesThrough.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableEmpty() throws java.lang.Throwable {
            this.payloads.fromObservableEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableJust() throws java.lang.Throwable {
            this.payloads.fromObservableJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableError() throws java.lang.Throwable {
            this.payloads.fromObservableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableValueAndError() throws java.lang.Throwable {
            this.payloads.fromObservableValueAndError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableMany() throws java.lang.Throwable {
            this.payloads.fromObservableMany.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableDisposeComposesThrough() throws java.lang.Throwable {
            this.payloads.fromObservableDisposeComposesThrough.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableDisposeComposesThroughImmediatelyCancelled() throws java.lang.Throwable {
            this.payloads.fromObservableDisposeComposesThroughImmediatelyCancelled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_just() throws java.lang.Throwable {
            this.payloads.just.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_empty() throws java.lang.Throwable {
            this.payloads.empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_never() throws java.lang.Throwable {
            this.payloads.never.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorCallable() throws java.lang.Throwable {
            this.payloads.errorCallable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorCallableReturnsNull() throws java.lang.Throwable {
            this.payloads.errorCallableReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_wrapCustom() throws java.lang.Throwable {
            this.payloads.wrapCustom.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_wrapMaybe() throws java.lang.Throwable {
            this.payloads.wrapMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptySingleton() throws java.lang.Throwable {
            this.payloads.emptySingleton.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_neverSingleton() throws java.lang.Throwable {
            this.payloads.neverSingleton.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_liftJust() throws java.lang.Throwable {
            this.payloads.liftJust.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_liftThrows() throws java.lang.Throwable {
            this.payloads.liftThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferThrows() throws java.lang.Throwable {
            this.payloads.deferThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferReturnsNull() throws java.lang.Throwable {
            this.payloads.deferReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_defer() throws java.lang.Throwable {
            this.payloads.defer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flowableMaybeFlowable() throws java.lang.Throwable {
            this.payloads.flowableMaybeFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_obervableMaybeobervable() throws java.lang.Throwable {
            this.payloads.obervableMaybeobervable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleMaybeSingle() throws java.lang.Throwable {
            this.payloads.singleMaybeSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableMaybeCompletable() throws java.lang.Throwable {
            this.payloads.completableMaybeCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsafeCreate() throws java.lang.Throwable {
            this.payloads.unsafeCreate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_to() throws java.lang.Throwable {
            this.payloads.to.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_as() throws java.lang.Throwable {
            this.payloads.as.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_compose() throws java.lang.Throwable {
            this.payloads.compose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapReturnNull() throws java.lang.Throwable {
            this.payloads.mapReturnNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mapThrows() throws java.lang.Throwable {
            this.payloads.mapThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_map() throws java.lang.Throwable {
            this.payloads.map.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterThrows() throws java.lang.Throwable {
            this.payloads.filterThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterTrue() throws java.lang.Throwable {
            this.payloads.filterTrue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterFalse() throws java.lang.Throwable {
            this.payloads.filterFalse.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterEmpty() throws java.lang.Throwable {
            this.payloads.filterEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleFilterThrows() throws java.lang.Throwable {
            this.payloads.singleFilterThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleFilterTrue() throws java.lang.Throwable {
            this.payloads.singleFilterTrue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleFilterFalse() throws java.lang.Throwable {
            this.payloads.singleFilterFalse.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cast() throws java.lang.Throwable {
            this.payloads.cast.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOnSuccess() throws java.lang.Throwable {
            this.payloads.observeOnSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOnError() throws java.lang.Throwable {
            this.payloads.observeOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOnComplete() throws java.lang.Throwable {
            this.payloads.observeOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOnDispose2() throws java.lang.Throwable {
            this.payloads.observeOnDispose2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOnDoubleSubscribe() throws java.lang.Throwable {
            this.payloads.observeOnDoubleSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOnSuccess() throws java.lang.Throwable {
            this.payloads.subscribeOnSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOnErrorThread() throws java.lang.Throwable {
            this.payloads.observeOnErrorThread.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOnCompleteThread() throws java.lang.Throwable {
            this.payloads.observeOnCompleteThread.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOnError() throws java.lang.Throwable {
            this.payloads.subscribeOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOnComplete() throws java.lang.Throwable {
            this.payloads.subscribeOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromAction() throws java.lang.Throwable {
            this.payloads.fromAction.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionThrows() throws java.lang.Throwable {
            this.payloads.fromActionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnable() throws java.lang.Throwable {
            this.payloads.fromRunnable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableThrows() throws java.lang.Throwable {
            this.payloads.fromRunnableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallableThrows() throws java.lang.Throwable {
            this.payloads.fromCallableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSuccess() throws java.lang.Throwable {
            this.payloads.doOnSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSuccessEmpty() throws java.lang.Throwable {
            this.payloads.doOnSuccessEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSuccessThrows() throws java.lang.Throwable {
            this.payloads.doOnSuccessThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribe() throws java.lang.Throwable {
            this.payloads.doOnSubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribeThrows() throws java.lang.Throwable {
            this.payloads.doOnSubscribeThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompleteThrows() throws java.lang.Throwable {
            this.payloads.doOnCompleteThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDispose() throws java.lang.Throwable {
            this.payloads.doOnDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeThrows() throws java.lang.Throwable {
            this.payloads.doOnDisposeThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOnDispose() throws java.lang.Throwable {
            this.payloads.observeOnDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doAfterTerminateSuccess() throws java.lang.Throwable {
            this.payloads.doAfterTerminateSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doAfterTerminateError() throws java.lang.Throwable {
            this.payloads.doAfterTerminateError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doAfterTerminateComplete() throws java.lang.Throwable {
            this.payloads.doAfterTerminateComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceThrowsNPE() throws java.lang.Throwable {
            this.payloads.sourceThrowsNPE.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceThrowsIAE() throws java.lang.Throwable {
            this.payloads.sourceThrowsIAE.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMap() throws java.lang.Throwable {
            this.payloads.flatMap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMap() throws java.lang.Throwable {
            this.payloads.concatMap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapEmpty() throws java.lang.Throwable {
            this.payloads.flatMapEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapError() throws java.lang.Throwable {
            this.payloads.flatMapError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapNotifySuccess() throws java.lang.Throwable {
            this.payloads.flatMapNotifySuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapNotifyError() throws java.lang.Throwable {
            this.payloads.flatMapNotifyError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapNotifyComplete() throws java.lang.Throwable {
            this.payloads.flatMapNotifyComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreElementSuccess() throws java.lang.Throwable {
            this.payloads.ignoreElementSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreElementError() throws java.lang.Throwable {
            this.payloads.ignoreElementError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreElementComplete() throws java.lang.Throwable {
            this.payloads.ignoreElementComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreElementSuccessMaybe() throws java.lang.Throwable {
            this.payloads.ignoreElementSuccessMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreElementErrorMaybe() throws java.lang.Throwable {
            this.payloads.ignoreElementErrorMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreElementCompleteMaybe() throws java.lang.Throwable {
            this.payloads.ignoreElementCompleteMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleToMaybe() throws java.lang.Throwable {
            this.payloads.singleToMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleToMaybeError() throws java.lang.Throwable {
            this.payloads.singleToMaybeError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableToMaybe() throws java.lang.Throwable {
            this.payloads.completableToMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completableToMaybeError() throws java.lang.Throwable {
            this.payloads.completableToMaybeError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyToSingle() throws java.lang.Throwable {
            this.payloads.emptyToSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorToSingle() throws java.lang.Throwable {
            this.payloads.errorToSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyToCompletable() throws java.lang.Throwable {
            this.payloads.emptyToCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorToCompletable() throws java.lang.Throwable {
            this.payloads.errorToCompletable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat2() throws java.lang.Throwable {
            this.payloads.concat2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat2Empty() throws java.lang.Throwable {
            this.payloads.concat2Empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat2Backpressured() throws java.lang.Throwable {
            this.payloads.concat2Backpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat2BackpressuredNonEager() throws java.lang.Throwable {
            this.payloads.concat2BackpressuredNonEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat3() throws java.lang.Throwable {
            this.payloads.concat3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat3Empty() throws java.lang.Throwable {
            this.payloads.concat3Empty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat3Mixed1() throws java.lang.Throwable {
            this.payloads.concat3Mixed1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat3Mixed2() throws java.lang.Throwable {
            this.payloads.concat3Mixed2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat3Backpressured() throws java.lang.Throwable {
            this.payloads.concat3Backpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArrayZero() throws java.lang.Throwable {
            this.payloads.concatArrayZero.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArrayOne() throws java.lang.Throwable {
            this.payloads.concatArrayOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concat4() throws java.lang.Throwable {
            this.payloads.concat4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterable() throws java.lang.Throwable {
            this.payloads.concatIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableEmpty() throws java.lang.Throwable {
            this.payloads.concatIterableEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableBackpressured() throws java.lang.Throwable {
            this.payloads.concatIterableBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableBackpressuredNonEager() throws java.lang.Throwable {
            this.payloads.concatIterableBackpressuredNonEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableZero() throws java.lang.Throwable {
            this.payloads.concatIterableZero.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableOne() throws java.lang.Throwable {
            this.payloads.concatIterableOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatPublisher() throws java.lang.Throwable {
            this.payloads.concatPublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatPublisherPrefetch() throws java.lang.Throwable {
            this.payloads.concatPublisherPrefetch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basic() throws java.lang.Throwable {
            this.payloads.basic.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicWithError() throws java.lang.Throwable {
            this.payloads.basicWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicWithComplete() throws java.lang.Throwable {
            this.payloads.basicWithComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsafeCreateWithMaybe() throws java.lang.Throwable {
            this.payloads.unsafeCreateWithMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maybeToPublisherEnum() throws java.lang.Throwable {
            this.payloads.maybeToPublisherEnum.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArrayOneIsNull() throws java.lang.Throwable {
            this.payloads.ambArrayOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArrayEmpty() throws java.lang.Throwable {
            this.payloads.ambArrayEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArrayOne() throws java.lang.Throwable {
            this.payloads.ambArrayOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWithOrder() throws java.lang.Throwable {
            this.payloads.ambWithOrder.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableOrder() throws java.lang.Throwable {
            this.payloads.ambIterableOrder.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArrayOrder() throws java.lang.Throwable {
            this.payloads.ambArrayOrder.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArray1SignalsSuccess() throws java.lang.Throwable {
            this.payloads.ambArray1SignalsSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArray2SignalsSuccess() throws java.lang.Throwable {
            this.payloads.ambArray2SignalsSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArray1SignalsError() throws java.lang.Throwable {
            this.payloads.ambArray1SignalsError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArray2SignalsError() throws java.lang.Throwable {
            this.payloads.ambArray2SignalsError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArray1SignalsComplete() throws java.lang.Throwable {
            this.payloads.ambArray1SignalsComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArray2SignalsComplete() throws java.lang.Throwable {
            this.payloads.ambArray2SignalsComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterable1SignalsSuccess() throws java.lang.Throwable {
            this.payloads.ambIterable1SignalsSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterable2SignalsSuccess() throws java.lang.Throwable {
            this.payloads.ambIterable2SignalsSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterable2SignalsSuccessWithOverlap() throws java.lang.Throwable {
            this.payloads.ambIterable2SignalsSuccessWithOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterable1SignalsError() throws java.lang.Throwable {
            this.payloads.ambIterable1SignalsError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterable2SignalsError() throws java.lang.Throwable {
            this.payloads.ambIterable2SignalsError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterable2SignalsErrorWithOverlap() throws java.lang.Throwable {
            this.payloads.ambIterable2SignalsErrorWithOverlap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterable1SignalsComplete() throws java.lang.Throwable {
            this.payloads.ambIterable1SignalsComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterable2SignalsComplete() throws java.lang.Throwable {
            this.payloads.ambIterable2SignalsComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.ambIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableOneIsNull() throws java.lang.Throwable {
            this.payloads.ambIterableOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableEmpty() throws java.lang.Throwable {
            this.payloads.ambIterableEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableOne() throws java.lang.Throwable {
            this.payloads.ambIterableOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArray() throws java.lang.Throwable {
            this.payloads.mergeArray.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_merge2() throws java.lang.Throwable {
            this.payloads.merge2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_merge3() throws java.lang.Throwable {
            this.payloads.merge3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_merge4() throws java.lang.Throwable {
            this.payloads.merge4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_merge4Take2() throws java.lang.Throwable {
            this.payloads.merge4Take2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArrayBackpressured() throws java.lang.Throwable {
            this.payloads.mergeArrayBackpressured.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArrayBackpressuredMixed1() throws java.lang.Throwable {
            this.payloads.mergeArrayBackpressuredMixed1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArrayBackpressuredMixed2() throws java.lang.Throwable {
            this.payloads.mergeArrayBackpressuredMixed2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArrayBackpressuredMixed3() throws java.lang.Throwable {
            this.payloads.mergeArrayBackpressuredMixed3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArrayFused() throws java.lang.Throwable {
            this.payloads.mergeArrayFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArrayFusedRace() throws java.lang.Throwable {
            this.payloads.mergeArrayFusedRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArrayZero() throws java.lang.Throwable {
            this.payloads.mergeArrayZero.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArrayOne() throws java.lang.Throwable {
            this.payloads.mergeArrayOne.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergePublisher() throws java.lang.Throwable {
            this.payloads.mergePublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergePublisherMaxConcurrent() throws java.lang.Throwable {
            this.payloads.mergePublisherMaxConcurrent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeMaybe() throws java.lang.Throwable {
            this.payloads.mergeMaybe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterable() throws java.lang.Throwable {
            this.payloads.mergeIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeALot() throws java.lang.Throwable {
            this.payloads.mergeALot.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeALotLastEmpty() throws java.lang.Throwable {
            this.payloads.mergeALotLastEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeALotFused() throws java.lang.Throwable {
            this.payloads.mergeALotFused.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeErrorSuccess() throws java.lang.Throwable {
            this.payloads.mergeErrorSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeSuccessError() throws java.lang.Throwable {
            this.payloads.mergeSuccessError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeZero() throws java.lang.Throwable {
            this.payloads.subscribeZero.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeZeroError() throws java.lang.Throwable {
            this.payloads.subscribeZeroError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeToOnSuccess() throws java.lang.Throwable {
            this.payloads.subscribeToOnSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeToOnError() throws java.lang.Throwable {
            this.payloads.subscribeToOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeToOnComplete() throws java.lang.Throwable {
            this.payloads.subscribeToOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeWith() throws java.lang.Throwable {
            this.payloads.subscribeWith.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEventSuccess() throws java.lang.Throwable {
            this.payloads.doOnEventSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEventError() throws java.lang.Throwable {
            this.payloads.doOnEventError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEventComplete() throws java.lang.Throwable {
            this.payloads.doOnEventComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEventSuccessThrows() throws java.lang.Throwable {
            this.payloads.doOnEventSuccessThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEventErrorThrows() throws java.lang.Throwable {
            this.payloads.doOnEventErrorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEventCompleteThrows() throws java.lang.Throwable {
            this.payloads.doOnEventCompleteThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatArrayDelayError() throws java.lang.Throwable {
            this.payloads.concatArrayDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableDelayError() throws java.lang.Throwable {
            this.payloads.concatIterableDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatPublisherDelayError() throws java.lang.Throwable {
            this.payloads.concatPublisherDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatPublisherDelayErrorPrefetch() throws java.lang.Throwable {
            this.payloads.concatPublisherDelayErrorPrefetch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatEagerArray() throws java.lang.Throwable {
            this.payloads.concatEagerArray.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatEagerIterable() throws java.lang.Throwable {
            this.payloads.concatEagerIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatEagerPublisher() throws java.lang.Throwable {
            this.payloads.concatEagerPublisher.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFuture() throws java.lang.Throwable {
            this.payloads.fromFuture.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeArrayDelayError() throws java.lang.Throwable {
            this.payloads.mergeArrayDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableDelayError() throws java.lang.Throwable {
            this.payloads.mergeIterableDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergePublisherDelayError() throws java.lang.Throwable {
            this.payloads.mergePublisherDelayError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayError2() throws java.lang.Throwable {
            this.payloads.mergeDelayError2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayError3() throws java.lang.Throwable {
            this.payloads.mergeDelayError3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayError4() throws java.lang.Throwable {
            this.payloads.mergeDelayError4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sequenceEqual() throws java.lang.Throwable {
            this.payloads.sequenceEqual.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timer() throws java.lang.Throwable {
            this.payloads.timer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingGet() throws java.lang.Throwable {
            this.payloads.blockingGet.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_flatMapContinuation() throws java.lang.Throwable {
            this.payloads.flatMapContinuation.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_using() throws java.lang.Throwable {
            this.payloads.using.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingNonEager() throws java.lang.Throwable {
            this.payloads.usingNonEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipArray() throws java.lang.Throwable {
            this.payloads.zipArray.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterable() throws java.lang.Throwable {
            this.payloads.zipIterable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip2() throws java.lang.Throwable {
            this.payloads.zip2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipWith() throws java.lang.Throwable {
            this.payloads.zipWith.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip3() throws java.lang.Throwable {
            this.payloads.zip3.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip4() throws java.lang.Throwable {
            this.payloads.zip4.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip5() throws java.lang.Throwable {
            this.payloads.zip5.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip6() throws java.lang.Throwable {
            this.payloads.zip6.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip7() throws java.lang.Throwable {
            this.payloads.zip7.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip8() throws java.lang.Throwable {
            this.payloads.zip8.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zip9() throws java.lang.Throwable {
            this.payloads.zip9.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWith1SignalsSuccess() throws java.lang.Throwable {
            this.payloads.ambWith1SignalsSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWith2SignalsSuccess() throws java.lang.Throwable {
            this.payloads.ambWith2SignalsSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_zipIterableObject() throws java.lang.Throwable {
            this.payloads.zipIterableObject.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onTerminateDetach() throws java.lang.Throwable {
            this.payloads.onTerminateDetach.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeat() throws java.lang.Throwable {
            this.payloads.repeat.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retry() throws java.lang.Throwable {
            this.payloads.retry.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeWithEmpty() throws java.lang.Throwable {
            this.payloads.onErrorResumeWithEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeWithValue() throws java.lang.Throwable {
            this.payloads.onErrorResumeWithValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeWithError() throws java.lang.Throwable {
            this.payloads.onErrorResumeWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_valueConcatWithValue() throws java.lang.Throwable {
            this.payloads.valueConcatWithValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorConcatWithValue() throws java.lang.Throwable {
            this.payloads.errorConcatWithValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_valueConcatWithError() throws java.lang.Throwable {
            this.payloads.valueConcatWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyConcatWithValue() throws java.lang.Throwable {
            this.payloads.emptyConcatWithValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyConcatWithError() throws java.lang.Throwable {
            this.payloads.emptyConcatWithError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new MaybeTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<MaybeTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(MaybeTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(MaybeTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement fromFlowableEmpty;

            public org.junit.runners.model.Statement fromFlowableJust;

            public org.junit.runners.model.Statement fromFlowableError;

            public org.junit.runners.model.Statement fromFlowableValueAndError;

            public org.junit.runners.model.Statement fromFlowableMany;

            public org.junit.runners.model.Statement fromFlowableDisposeComposesThrough;

            public org.junit.runners.model.Statement fromObservableEmpty;

            public org.junit.runners.model.Statement fromObservableJust;

            public org.junit.runners.model.Statement fromObservableError;

            public org.junit.runners.model.Statement fromObservableValueAndError;

            public org.junit.runners.model.Statement fromObservableMany;

            public org.junit.runners.model.Statement fromObservableDisposeComposesThrough;

            public org.junit.runners.model.Statement fromObservableDisposeComposesThroughImmediatelyCancelled;

            public org.junit.runners.model.Statement just;

            public org.junit.runners.model.Statement empty;

            public org.junit.runners.model.Statement never;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement errorCallable;

            public org.junit.runners.model.Statement errorCallableReturnsNull;

            public org.junit.runners.model.Statement wrapCustom;

            public org.junit.runners.model.Statement wrapMaybe;

            public org.junit.runners.model.Statement emptySingleton;

            public org.junit.runners.model.Statement neverSingleton;

            public org.junit.runners.model.Statement liftJust;

            public org.junit.runners.model.Statement liftThrows;

            public org.junit.runners.model.Statement deferThrows;

            public org.junit.runners.model.Statement deferReturnsNull;

            public org.junit.runners.model.Statement defer;

            public org.junit.runners.model.Statement flowableMaybeFlowable;

            public org.junit.runners.model.Statement obervableMaybeobervable;

            public org.junit.runners.model.Statement singleMaybeSingle;

            public org.junit.runners.model.Statement completableMaybeCompletable;

            public org.junit.runners.model.Statement unsafeCreate;

            public org.junit.runners.model.Statement to;

            public org.junit.runners.model.Statement as;

            public org.junit.runners.model.Statement compose;

            public org.junit.runners.model.Statement mapReturnNull;

            public org.junit.runners.model.Statement mapThrows;

            public org.junit.runners.model.Statement map;

            public org.junit.runners.model.Statement filterThrows;

            public org.junit.runners.model.Statement filterTrue;

            public org.junit.runners.model.Statement filterFalse;

            public org.junit.runners.model.Statement filterEmpty;

            public org.junit.runners.model.Statement singleFilterThrows;

            public org.junit.runners.model.Statement singleFilterTrue;

            public org.junit.runners.model.Statement singleFilterFalse;

            public org.junit.runners.model.Statement cast;

            public org.junit.runners.model.Statement observeOnSuccess;

            public org.junit.runners.model.Statement observeOnError;

            public org.junit.runners.model.Statement observeOnComplete;

            public org.junit.runners.model.Statement observeOnDispose2;

            public org.junit.runners.model.Statement observeOnDoubleSubscribe;

            public org.junit.runners.model.Statement subscribeOnSuccess;

            public org.junit.runners.model.Statement observeOnErrorThread;

            public org.junit.runners.model.Statement observeOnCompleteThread;

            public org.junit.runners.model.Statement subscribeOnError;

            public org.junit.runners.model.Statement subscribeOnComplete;

            public org.junit.runners.model.Statement fromAction;

            public org.junit.runners.model.Statement fromActionThrows;

            public org.junit.runners.model.Statement fromRunnable;

            public org.junit.runners.model.Statement fromRunnableThrows;

            public org.junit.runners.model.Statement fromCallableThrows;

            public org.junit.runners.model.Statement doOnSuccess;

            public org.junit.runners.model.Statement doOnSuccessEmpty;

            public org.junit.runners.model.Statement doOnSuccessThrows;

            public org.junit.runners.model.Statement doOnSubscribe;

            public org.junit.runners.model.Statement doOnSubscribeThrows;

            public org.junit.runners.model.Statement doOnCompleteThrows;

            public org.junit.runners.model.Statement doOnDispose;

            public org.junit.runners.model.Statement doOnDisposeThrows;

            public org.junit.runners.model.Statement observeOnDispose;

            public org.junit.runners.model.Statement doAfterTerminateSuccess;

            public org.junit.runners.model.Statement doAfterTerminateError;

            public org.junit.runners.model.Statement doAfterTerminateComplete;

            public org.junit.runners.model.Statement sourceThrowsNPE;

            public org.junit.runners.model.Statement sourceThrowsIAE;

            public org.junit.runners.model.Statement flatMap;

            public org.junit.runners.model.Statement concatMap;

            public org.junit.runners.model.Statement flatMapEmpty;

            public org.junit.runners.model.Statement flatMapError;

            public org.junit.runners.model.Statement flatMapNotifySuccess;

            public org.junit.runners.model.Statement flatMapNotifyError;

            public org.junit.runners.model.Statement flatMapNotifyComplete;

            public org.junit.runners.model.Statement ignoreElementSuccess;

            public org.junit.runners.model.Statement ignoreElementError;

            public org.junit.runners.model.Statement ignoreElementComplete;

            public org.junit.runners.model.Statement ignoreElementSuccessMaybe;

            public org.junit.runners.model.Statement ignoreElementErrorMaybe;

            public org.junit.runners.model.Statement ignoreElementCompleteMaybe;

            public org.junit.runners.model.Statement singleToMaybe;

            public org.junit.runners.model.Statement singleToMaybeError;

            public org.junit.runners.model.Statement completableToMaybe;

            public org.junit.runners.model.Statement completableToMaybeError;

            public org.junit.runners.model.Statement emptyToSingle;

            public org.junit.runners.model.Statement errorToSingle;

            public org.junit.runners.model.Statement emptyToCompletable;

            public org.junit.runners.model.Statement errorToCompletable;

            public org.junit.runners.model.Statement concat2;

            public org.junit.runners.model.Statement concat2Empty;

            public org.junit.runners.model.Statement concat2Backpressured;

            public org.junit.runners.model.Statement concat2BackpressuredNonEager;

            public org.junit.runners.model.Statement concat3;

            public org.junit.runners.model.Statement concat3Empty;

            public org.junit.runners.model.Statement concat3Mixed1;

            public org.junit.runners.model.Statement concat3Mixed2;

            public org.junit.runners.model.Statement concat3Backpressured;

            public org.junit.runners.model.Statement concatArrayZero;

            public org.junit.runners.model.Statement concatArrayOne;

            public org.junit.runners.model.Statement concat4;

            public org.junit.runners.model.Statement concatIterable;

            public org.junit.runners.model.Statement concatIterableEmpty;

            public org.junit.runners.model.Statement concatIterableBackpressured;

            public org.junit.runners.model.Statement concatIterableBackpressuredNonEager;

            public org.junit.runners.model.Statement concatIterableZero;

            public org.junit.runners.model.Statement concatIterableOne;

            public org.junit.runners.model.Statement concatPublisher;

            public org.junit.runners.model.Statement concatPublisherPrefetch;

            public org.junit.runners.model.Statement basic;

            public org.junit.runners.model.Statement basicWithError;

            public org.junit.runners.model.Statement basicWithComplete;

            public org.junit.runners.model.Statement unsafeCreateWithMaybe;

            public org.junit.runners.model.Statement maybeToPublisherEnum;

            public org.junit.runners.model.Statement ambArrayOneIsNull;

            public org.junit.runners.model.Statement ambArrayEmpty;

            public org.junit.runners.model.Statement ambArrayOne;

            public org.junit.runners.model.Statement ambWithOrder;

            public org.junit.runners.model.Statement ambIterableOrder;

            public org.junit.runners.model.Statement ambArrayOrder;

            public org.junit.runners.model.Statement ambArray1SignalsSuccess;

            public org.junit.runners.model.Statement ambArray2SignalsSuccess;

            public org.junit.runners.model.Statement ambArray1SignalsError;

            public org.junit.runners.model.Statement ambArray2SignalsError;

            public org.junit.runners.model.Statement ambArray1SignalsComplete;

            public org.junit.runners.model.Statement ambArray2SignalsComplete;

            public org.junit.runners.model.Statement ambIterable1SignalsSuccess;

            public org.junit.runners.model.Statement ambIterable2SignalsSuccess;

            public org.junit.runners.model.Statement ambIterable2SignalsSuccessWithOverlap;

            public org.junit.runners.model.Statement ambIterable1SignalsError;

            public org.junit.runners.model.Statement ambIterable2SignalsError;

            public org.junit.runners.model.Statement ambIterable2SignalsErrorWithOverlap;

            public org.junit.runners.model.Statement ambIterable1SignalsComplete;

            public org.junit.runners.model.Statement ambIterable2SignalsComplete;

            public org.junit.runners.model.Statement ambIterableIteratorNull;

            public org.junit.runners.model.Statement ambIterableOneIsNull;

            public org.junit.runners.model.Statement ambIterableEmpty;

            public org.junit.runners.model.Statement ambIterableOne;

            public org.junit.runners.model.Statement mergeArray;

            public org.junit.runners.model.Statement merge2;

            public org.junit.runners.model.Statement merge3;

            public org.junit.runners.model.Statement merge4;

            public org.junit.runners.model.Statement merge4Take2;

            public org.junit.runners.model.Statement mergeArrayBackpressured;

            public org.junit.runners.model.Statement mergeArrayBackpressuredMixed1;

            public org.junit.runners.model.Statement mergeArrayBackpressuredMixed2;

            public org.junit.runners.model.Statement mergeArrayBackpressuredMixed3;

            public org.junit.runners.model.Statement mergeArrayFused;

            public org.junit.runners.model.Statement mergeArrayFusedRace;

            public org.junit.runners.model.Statement mergeArrayZero;

            public org.junit.runners.model.Statement mergeArrayOne;

            public org.junit.runners.model.Statement mergePublisher;

            public org.junit.runners.model.Statement mergePublisherMaxConcurrent;

            public org.junit.runners.model.Statement mergeMaybe;

            public org.junit.runners.model.Statement mergeIterable;

            public org.junit.runners.model.Statement mergeALot;

            public org.junit.runners.model.Statement mergeALotLastEmpty;

            public org.junit.runners.model.Statement mergeALotFused;

            public org.junit.runners.model.Statement mergeErrorSuccess;

            public org.junit.runners.model.Statement mergeSuccessError;

            public org.junit.runners.model.Statement subscribeZero;

            public org.junit.runners.model.Statement subscribeZeroError;

            public org.junit.runners.model.Statement subscribeToOnSuccess;

            public org.junit.runners.model.Statement subscribeToOnError;

            public org.junit.runners.model.Statement subscribeToOnComplete;

            public org.junit.runners.model.Statement subscribeWith;

            public org.junit.runners.model.Statement doOnEventSuccess;

            public org.junit.runners.model.Statement doOnEventError;

            public org.junit.runners.model.Statement doOnEventComplete;

            public org.junit.runners.model.Statement doOnEventSuccessThrows;

            public org.junit.runners.model.Statement doOnEventErrorThrows;

            public org.junit.runners.model.Statement doOnEventCompleteThrows;

            public org.junit.runners.model.Statement concatArrayDelayError;

            public org.junit.runners.model.Statement concatIterableDelayError;

            public org.junit.runners.model.Statement concatPublisherDelayError;

            public org.junit.runners.model.Statement concatPublisherDelayErrorPrefetch;

            public org.junit.runners.model.Statement concatEagerArray;

            public org.junit.runners.model.Statement concatEagerIterable;

            public org.junit.runners.model.Statement concatEagerPublisher;

            public org.junit.runners.model.Statement fromFuture;

            public org.junit.runners.model.Statement mergeArrayDelayError;

            public org.junit.runners.model.Statement mergeIterableDelayError;

            public org.junit.runners.model.Statement mergePublisherDelayError;

            public org.junit.runners.model.Statement mergeDelayError2;

            public org.junit.runners.model.Statement mergeDelayError3;

            public org.junit.runners.model.Statement mergeDelayError4;

            public org.junit.runners.model.Statement sequenceEqual;

            public org.junit.runners.model.Statement timer;

            public org.junit.runners.model.Statement blockingGet;

            public org.junit.runners.model.Statement flatMapContinuation;

            public org.junit.runners.model.Statement using;

            public org.junit.runners.model.Statement usingNonEager;

            public org.junit.runners.model.Statement zipArray;

            public org.junit.runners.model.Statement zipIterable;

            public org.junit.runners.model.Statement zip2;

            public org.junit.runners.model.Statement zipWith;

            public org.junit.runners.model.Statement zip3;

            public org.junit.runners.model.Statement zip4;

            public org.junit.runners.model.Statement zip5;

            public org.junit.runners.model.Statement zip6;

            public org.junit.runners.model.Statement zip7;

            public org.junit.runners.model.Statement zip8;

            public org.junit.runners.model.Statement zip9;

            public org.junit.runners.model.Statement ambWith1SignalsSuccess;

            public org.junit.runners.model.Statement ambWith2SignalsSuccess;

            public org.junit.runners.model.Statement zipIterableObject;

            public org.junit.runners.model.Statement onTerminateDetach;

            public org.junit.runners.model.Statement repeat;

            public org.junit.runners.model.Statement retry;

            public org.junit.runners.model.Statement onErrorResumeWithEmpty;

            public org.junit.runners.model.Statement onErrorResumeWithValue;

            public org.junit.runners.model.Statement onErrorResumeWithError;

            public org.junit.runners.model.Statement valueConcatWithValue;

            public org.junit.runners.model.Statement errorConcatWithValue;

            public org.junit.runners.model.Statement valueConcatWithError;

            public org.junit.runners.model.Statement emptyConcatWithValue;

            public org.junit.runners.model.Statement emptyConcatWithError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.fromFlowableEmpty = _ClassStatement.forPayload(MaybeTest::fromFlowableEmpty, "fromFlowableEmpty", this);
            this.payloads.fromFlowableJust = _ClassStatement.forPayload(MaybeTest::fromFlowableJust, "fromFlowableJust", this);
            this.payloads.fromFlowableError = _ClassStatement.forPayload(MaybeTest::fromFlowableError, "fromFlowableError", this);
            this.payloads.fromFlowableValueAndError = _ClassStatement.forPayload(MaybeTest::fromFlowableValueAndError, "fromFlowableValueAndError", this);
            this.payloads.fromFlowableMany = _ClassStatement.forPayload(MaybeTest::fromFlowableMany, "fromFlowableMany", this);
            this.payloads.fromFlowableDisposeComposesThrough = _ClassStatement.forPayload(MaybeTest::fromFlowableDisposeComposesThrough, "fromFlowableDisposeComposesThrough", this);
            this.payloads.fromObservableEmpty = _ClassStatement.forPayload(MaybeTest::fromObservableEmpty, "fromObservableEmpty", this);
            this.payloads.fromObservableJust = _ClassStatement.forPayload(MaybeTest::fromObservableJust, "fromObservableJust", this);
            this.payloads.fromObservableError = _ClassStatement.forPayload(MaybeTest::fromObservableError, "fromObservableError", this);
            this.payloads.fromObservableValueAndError = _ClassStatement.forPayload(MaybeTest::fromObservableValueAndError, "fromObservableValueAndError", this);
            this.payloads.fromObservableMany = _ClassStatement.forPayload(MaybeTest::fromObservableMany, "fromObservableMany", this);
            this.payloads.fromObservableDisposeComposesThrough = _ClassStatement.forPayload(MaybeTest::fromObservableDisposeComposesThrough, "fromObservableDisposeComposesThrough", this);
            this.payloads.fromObservableDisposeComposesThroughImmediatelyCancelled = _ClassStatement.forPayload(MaybeTest::fromObservableDisposeComposesThroughImmediatelyCancelled, "fromObservableDisposeComposesThroughImmediatelyCancelled", this);
            this.payloads.just = _ClassStatement.forPayload(MaybeTest::just, "just", this);
            this.payloads.empty = _ClassStatement.forPayload(MaybeTest::empty, "empty", this);
            this.payloads.never = _ClassStatement.forPayload(MaybeTest::never, "never", this);
            this.payloads.error = _ClassStatement.forPayload(MaybeTest::error, "error", this);
            this.payloads.errorCallable = _ClassStatement.forPayload(MaybeTest::errorCallable, "errorCallable", this);
            this.payloads.errorCallableReturnsNull = _ClassStatement.forPayload(MaybeTest::errorCallableReturnsNull, "errorCallableReturnsNull", this);
            this.payloads.wrapCustom = _ClassStatement.forPayload(MaybeTest::wrapCustom, "wrapCustom", this);
            this.payloads.wrapMaybe = _ClassStatement.forPayload(MaybeTest::wrapMaybe, "wrapMaybe", this);
            this.payloads.emptySingleton = _ClassStatement.forPayload(MaybeTest::emptySingleton, "emptySingleton", this);
            this.payloads.neverSingleton = _ClassStatement.forPayload(MaybeTest::neverSingleton, "neverSingleton", this);
            this.payloads.liftJust = _ClassStatement.forPayload(MaybeTest::liftJust, "liftJust", this);
            this.payloads.liftThrows = _ClassStatement.forPayload(MaybeTest::liftThrows, "liftThrows", this);
            this.payloads.deferThrows = _ClassStatement.forPayload(MaybeTest::deferThrows, "deferThrows", this);
            this.payloads.deferReturnsNull = _ClassStatement.forPayload(MaybeTest::deferReturnsNull, "deferReturnsNull", this);
            this.payloads.defer = _ClassStatement.forPayload(MaybeTest::defer, "defer", this);
            this.payloads.flowableMaybeFlowable = _ClassStatement.forPayload(MaybeTest::flowableMaybeFlowable, "flowableMaybeFlowable", this);
            this.payloads.obervableMaybeobervable = _ClassStatement.forPayload(MaybeTest::obervableMaybeobervable, "obervableMaybeobervable", this);
            this.payloads.singleMaybeSingle = _ClassStatement.forPayload(MaybeTest::singleMaybeSingle, "singleMaybeSingle", this);
            this.payloads.completableMaybeCompletable = _ClassStatement.forPayload(MaybeTest::completableMaybeCompletable, "completableMaybeCompletable", this);
            this.payloads.unsafeCreate = _ClassStatement.forPayload(MaybeTest::unsafeCreate, "unsafeCreate", this);
            this.payloads.to = _ClassStatement.forPayload(MaybeTest::to, "to", this);
            this.payloads.as = _ClassStatement.forPayload(MaybeTest::as, "as", this);
            this.payloads.compose = _ClassStatement.forPayload(MaybeTest::compose, "compose", this);
            this.payloads.mapReturnNull = _ClassStatement.forPayload(MaybeTest::mapReturnNull, "mapReturnNull", this);
            this.payloads.mapThrows = _ClassStatement.forPayload(MaybeTest::mapThrows, "mapThrows", this);
            this.payloads.map = _ClassStatement.forPayload(MaybeTest::map, "map", this);
            this.payloads.filterThrows = _ClassStatement.forPayload(MaybeTest::filterThrows, "filterThrows", this);
            this.payloads.filterTrue = _ClassStatement.forPayload(MaybeTest::filterTrue, "filterTrue", this);
            this.payloads.filterFalse = _ClassStatement.forPayload(MaybeTest::filterFalse, "filterFalse", this);
            this.payloads.filterEmpty = _ClassStatement.forPayload(MaybeTest::filterEmpty, "filterEmpty", this);
            this.payloads.singleFilterThrows = _ClassStatement.forPayload(MaybeTest::singleFilterThrows, "singleFilterThrows", this);
            this.payloads.singleFilterTrue = _ClassStatement.forPayload(MaybeTest::singleFilterTrue, "singleFilterTrue", this);
            this.payloads.singleFilterFalse = _ClassStatement.forPayload(MaybeTest::singleFilterFalse, "singleFilterFalse", this);
            this.payloads.cast = _ClassStatement.forPayload(MaybeTest::cast, "cast", this);
            this.payloads.observeOnSuccess = _ClassStatement.forPayload(MaybeTest::observeOnSuccess, "observeOnSuccess", this);
            this.payloads.observeOnError = _ClassStatement.forPayload(MaybeTest::observeOnError, "observeOnError", this);
            this.payloads.observeOnComplete = _ClassStatement.forPayload(MaybeTest::observeOnComplete, "observeOnComplete", this);
            this.payloads.observeOnDispose2 = _ClassStatement.forPayload(MaybeTest::observeOnDispose2, "observeOnDispose2", this);
            this.payloads.observeOnDoubleSubscribe = _ClassStatement.forPayload(MaybeTest::observeOnDoubleSubscribe, "observeOnDoubleSubscribe", this);
            this.payloads.subscribeOnSuccess = _ClassStatement.forPayload(MaybeTest::subscribeOnSuccess, "subscribeOnSuccess", this);
            this.payloads.observeOnErrorThread = _ClassStatement.forPayload(MaybeTest::observeOnErrorThread, "observeOnErrorThread", this);
            this.payloads.observeOnCompleteThread = _ClassStatement.forPayload(MaybeTest::observeOnCompleteThread, "observeOnCompleteThread", this);
            this.payloads.subscribeOnError = _ClassStatement.forPayload(MaybeTest::subscribeOnError, "subscribeOnError", this);
            this.payloads.subscribeOnComplete = _ClassStatement.forPayload(MaybeTest::subscribeOnComplete, "subscribeOnComplete", this);
            this.payloads.fromAction = _ClassStatement.forPayload(MaybeTest::fromAction, "fromAction", this);
            this.payloads.fromActionThrows = _ClassStatement.forPayload(MaybeTest::fromActionThrows, "fromActionThrows", this);
            this.payloads.fromRunnable = _ClassStatement.forPayload(MaybeTest::fromRunnable, "fromRunnable", this);
            this.payloads.fromRunnableThrows = _ClassStatement.forPayload(MaybeTest::fromRunnableThrows, "fromRunnableThrows", this);
            this.payloads.fromCallableThrows = _ClassStatement.forPayload(MaybeTest::fromCallableThrows, "fromCallableThrows", this);
            this.payloads.doOnSuccess = _ClassStatement.forPayload(MaybeTest::doOnSuccess, "doOnSuccess", this);
            this.payloads.doOnSuccessEmpty = _ClassStatement.forPayload(MaybeTest::doOnSuccessEmpty, "doOnSuccessEmpty", this);
            this.payloads.doOnSuccessThrows = _ClassStatement.forPayload(MaybeTest::doOnSuccessThrows, "doOnSuccessThrows", this);
            this.payloads.doOnSubscribe = _ClassStatement.forPayload(MaybeTest::doOnSubscribe, "doOnSubscribe", this);
            this.payloads.doOnSubscribeThrows = _ClassStatement.forPayload(MaybeTest::doOnSubscribeThrows, "doOnSubscribeThrows", this);
            this.payloads.doOnCompleteThrows = _ClassStatement.forPayload(MaybeTest::doOnCompleteThrows, "doOnCompleteThrows", this);
            this.payloads.doOnDispose = _ClassStatement.forPayload(MaybeTest::doOnDispose, "doOnDispose", this);
            this.payloads.doOnDisposeThrows = _ClassStatement.forPayload(MaybeTest::doOnDisposeThrows, "doOnDisposeThrows", this);
            this.payloads.observeOnDispose = _ClassStatement.forPayload(MaybeTest::observeOnDispose, "observeOnDispose", this);
            this.payloads.doAfterTerminateSuccess = _ClassStatement.forPayload(MaybeTest::doAfterTerminateSuccess, "doAfterTerminateSuccess", this);
            this.payloads.doAfterTerminateError = _ClassStatement.forPayload(MaybeTest::doAfterTerminateError, "doAfterTerminateError", this);
            this.payloads.doAfterTerminateComplete = _ClassStatement.forPayload(MaybeTest::doAfterTerminateComplete, "doAfterTerminateComplete", this);
            this.payloads.sourceThrowsNPE = _ClassStatement.forPayload(MaybeTest::sourceThrowsNPE, "sourceThrowsNPE", this);
            this.payloads.sourceThrowsIAE = _ClassStatement.forPayload(MaybeTest::sourceThrowsIAE, "sourceThrowsIAE", this);
            this.payloads.flatMap = _ClassStatement.forPayload(MaybeTest::flatMap, "flatMap", this);
            this.payloads.concatMap = _ClassStatement.forPayload(MaybeTest::concatMap, "concatMap", this);
            this.payloads.flatMapEmpty = _ClassStatement.forPayload(MaybeTest::flatMapEmpty, "flatMapEmpty", this);
            this.payloads.flatMapError = _ClassStatement.forPayload(MaybeTest::flatMapError, "flatMapError", this);
            this.payloads.flatMapNotifySuccess = _ClassStatement.forPayload(MaybeTest::flatMapNotifySuccess, "flatMapNotifySuccess", this);
            this.payloads.flatMapNotifyError = _ClassStatement.forPayload(MaybeTest::flatMapNotifyError, "flatMapNotifyError", this);
            this.payloads.flatMapNotifyComplete = _ClassStatement.forPayload(MaybeTest::flatMapNotifyComplete, "flatMapNotifyComplete", this);
            this.payloads.ignoreElementSuccess = _ClassStatement.forPayload(MaybeTest::ignoreElementSuccess, "ignoreElementSuccess", this);
            this.payloads.ignoreElementError = _ClassStatement.forPayload(MaybeTest::ignoreElementError, "ignoreElementError", this);
            this.payloads.ignoreElementComplete = _ClassStatement.forPayload(MaybeTest::ignoreElementComplete, "ignoreElementComplete", this);
            this.payloads.ignoreElementSuccessMaybe = _ClassStatement.forPayload(MaybeTest::ignoreElementSuccessMaybe, "ignoreElementSuccessMaybe", this);
            this.payloads.ignoreElementErrorMaybe = _ClassStatement.forPayload(MaybeTest::ignoreElementErrorMaybe, "ignoreElementErrorMaybe", this);
            this.payloads.ignoreElementCompleteMaybe = _ClassStatement.forPayload(MaybeTest::ignoreElementCompleteMaybe, "ignoreElementCompleteMaybe", this);
            this.payloads.singleToMaybe = _ClassStatement.forPayload(MaybeTest::singleToMaybe, "singleToMaybe", this);
            this.payloads.singleToMaybeError = _ClassStatement.forPayload(MaybeTest::singleToMaybeError, "singleToMaybeError", this);
            this.payloads.completableToMaybe = _ClassStatement.forPayload(MaybeTest::completableToMaybe, "completableToMaybe", this);
            this.payloads.completableToMaybeError = _ClassStatement.forPayload(MaybeTest::completableToMaybeError, "completableToMaybeError", this);
            this.payloads.emptyToSingle = _ClassStatement.forPayload(MaybeTest::emptyToSingle, "emptyToSingle", this);
            this.payloads.errorToSingle = _ClassStatement.forPayload(MaybeTest::errorToSingle, "errorToSingle", this);
            this.payloads.emptyToCompletable = _ClassStatement.forPayload(MaybeTest::emptyToCompletable, "emptyToCompletable", this);
            this.payloads.errorToCompletable = _ClassStatement.forPayload(MaybeTest::errorToCompletable, "errorToCompletable", this);
            this.payloads.concat2 = _ClassStatement.forPayload(MaybeTest::concat2, "concat2", this);
            this.payloads.concat2Empty = _ClassStatement.forPayload(MaybeTest::concat2Empty, "concat2Empty", this);
            this.payloads.concat2Backpressured = _ClassStatement.forPayload(MaybeTest::concat2Backpressured, "concat2Backpressured", this);
            this.payloads.concat2BackpressuredNonEager = _ClassStatement.forPayload(MaybeTest::concat2BackpressuredNonEager, "concat2BackpressuredNonEager", this);
            this.payloads.concat3 = _ClassStatement.forPayload(MaybeTest::concat3, "concat3", this);
            this.payloads.concat3Empty = _ClassStatement.forPayload(MaybeTest::concat3Empty, "concat3Empty", this);
            this.payloads.concat3Mixed1 = _ClassStatement.forPayload(MaybeTest::concat3Mixed1, "concat3Mixed1", this);
            this.payloads.concat3Mixed2 = _ClassStatement.forPayload(MaybeTest::concat3Mixed2, "concat3Mixed2", this);
            this.payloads.concat3Backpressured = _ClassStatement.forPayload(MaybeTest::concat3Backpressured, "concat3Backpressured", this);
            this.payloads.concatArrayZero = _ClassStatement.forPayload(MaybeTest::concatArrayZero, "concatArrayZero", this);
            this.payloads.concatArrayOne = _ClassStatement.forPayload(MaybeTest::concatArrayOne, "concatArrayOne", this);
            this.payloads.concat4 = _ClassStatement.forPayload(MaybeTest::concat4, "concat4", this);
            this.payloads.concatIterable = _ClassStatement.forPayload(MaybeTest::concatIterable, "concatIterable", this);
            this.payloads.concatIterableEmpty = _ClassStatement.forPayload(MaybeTest::concatIterableEmpty, "concatIterableEmpty", this);
            this.payloads.concatIterableBackpressured = _ClassStatement.forPayload(MaybeTest::concatIterableBackpressured, "concatIterableBackpressured", this);
            this.payloads.concatIterableBackpressuredNonEager = _ClassStatement.forPayload(MaybeTest::concatIterableBackpressuredNonEager, "concatIterableBackpressuredNonEager", this);
            this.payloads.concatIterableZero = _ClassStatement.forPayload(MaybeTest::concatIterableZero, "concatIterableZero", this);
            this.payloads.concatIterableOne = _ClassStatement.forPayload(MaybeTest::concatIterableOne, "concatIterableOne", this);
            this.payloads.concatPublisher = _ClassStatement.forPayload(MaybeTest::concatPublisher, "concatPublisher", this);
            this.payloads.concatPublisherPrefetch = _ClassStatement.forPayload(MaybeTest::concatPublisherPrefetch, "concatPublisherPrefetch", this);
            this.payloads.basic = _ClassStatement.forPayload(MaybeTest::basic, "basic", this);
            this.payloads.basicWithError = _ClassStatement.forPayload(MaybeTest::basicWithError, "basicWithError", this);
            this.payloads.basicWithComplete = _ClassStatement.forPayload(MaybeTest::basicWithComplete, "basicWithComplete", this);
            this.payloads.unsafeCreateWithMaybe = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(MaybeTest::unsafeCreateWithMaybe, java.lang.IllegalArgumentException.class), "unsafeCreateWithMaybe", this);
            this.payloads.maybeToPublisherEnum = _ClassStatement.forPayload(MaybeTest::maybeToPublisherEnum, "maybeToPublisherEnum", this);
            this.payloads.ambArrayOneIsNull = _ClassStatement.forPayload(MaybeTest::ambArrayOneIsNull, "ambArrayOneIsNull", this);
            this.payloads.ambArrayEmpty = _ClassStatement.forPayload(MaybeTest::ambArrayEmpty, "ambArrayEmpty", this);
            this.payloads.ambArrayOne = _ClassStatement.forPayload(MaybeTest::ambArrayOne, "ambArrayOne", this);
            this.payloads.ambWithOrder = _ClassStatement.forPayload(MaybeTest::ambWithOrder, "ambWithOrder", this);
            this.payloads.ambIterableOrder = _ClassStatement.forPayload(MaybeTest::ambIterableOrder, "ambIterableOrder", this);
            this.payloads.ambArrayOrder = _ClassStatement.forPayload(MaybeTest::ambArrayOrder, "ambArrayOrder", this);
            this.payloads.ambArray1SignalsSuccess = _ClassStatement.forPayload(MaybeTest::ambArray1SignalsSuccess, "ambArray1SignalsSuccess", this);
            this.payloads.ambArray2SignalsSuccess = _ClassStatement.forPayload(MaybeTest::ambArray2SignalsSuccess, "ambArray2SignalsSuccess", this);
            this.payloads.ambArray1SignalsError = _ClassStatement.forPayload(MaybeTest::ambArray1SignalsError, "ambArray1SignalsError", this);
            this.payloads.ambArray2SignalsError = _ClassStatement.forPayload(MaybeTest::ambArray2SignalsError, "ambArray2SignalsError", this);
            this.payloads.ambArray1SignalsComplete = _ClassStatement.forPayload(MaybeTest::ambArray1SignalsComplete, "ambArray1SignalsComplete", this);
            this.payloads.ambArray2SignalsComplete = _ClassStatement.forPayload(MaybeTest::ambArray2SignalsComplete, "ambArray2SignalsComplete", this);
            this.payloads.ambIterable1SignalsSuccess = _ClassStatement.forPayload(MaybeTest::ambIterable1SignalsSuccess, "ambIterable1SignalsSuccess", this);
            this.payloads.ambIterable2SignalsSuccess = _ClassStatement.forPayload(MaybeTest::ambIterable2SignalsSuccess, "ambIterable2SignalsSuccess", this);
            this.payloads.ambIterable2SignalsSuccessWithOverlap = _ClassStatement.forPayload(MaybeTest::ambIterable2SignalsSuccessWithOverlap, "ambIterable2SignalsSuccessWithOverlap", this);
            this.payloads.ambIterable1SignalsError = _ClassStatement.forPayload(MaybeTest::ambIterable1SignalsError, "ambIterable1SignalsError", this);
            this.payloads.ambIterable2SignalsError = _ClassStatement.forPayload(MaybeTest::ambIterable2SignalsError, "ambIterable2SignalsError", this);
            this.payloads.ambIterable2SignalsErrorWithOverlap = _ClassStatement.forPayload(MaybeTest::ambIterable2SignalsErrorWithOverlap, "ambIterable2SignalsErrorWithOverlap", this);
            this.payloads.ambIterable1SignalsComplete = _ClassStatement.forPayload(MaybeTest::ambIterable1SignalsComplete, "ambIterable1SignalsComplete", this);
            this.payloads.ambIterable2SignalsComplete = _ClassStatement.forPayload(MaybeTest::ambIterable2SignalsComplete, "ambIterable2SignalsComplete", this);
            this.payloads.ambIterableIteratorNull = _ClassStatement.forPayload(MaybeTest::ambIterableIteratorNull, "ambIterableIteratorNull", this);
            this.payloads.ambIterableOneIsNull = _ClassStatement.forPayload(MaybeTest::ambIterableOneIsNull, "ambIterableOneIsNull", this);
            this.payloads.ambIterableEmpty = _ClassStatement.forPayload(MaybeTest::ambIterableEmpty, "ambIterableEmpty", this);
            this.payloads.ambIterableOne = _ClassStatement.forPayload(MaybeTest::ambIterableOne, "ambIterableOne", this);
            this.payloads.mergeArray = _ClassStatement.forPayload(MaybeTest::mergeArray, "mergeArray", this);
            this.payloads.merge2 = _ClassStatement.forPayload(MaybeTest::merge2, "merge2", this);
            this.payloads.merge3 = _ClassStatement.forPayload(MaybeTest::merge3, "merge3", this);
            this.payloads.merge4 = _ClassStatement.forPayload(MaybeTest::merge4, "merge4", this);
            this.payloads.merge4Take2 = _ClassStatement.forPayload(MaybeTest::merge4Take2, "merge4Take2", this);
            this.payloads.mergeArrayBackpressured = _ClassStatement.forPayload(MaybeTest::mergeArrayBackpressured, "mergeArrayBackpressured", this);
            this.payloads.mergeArrayBackpressuredMixed1 = _ClassStatement.forPayload(MaybeTest::mergeArrayBackpressuredMixed1, "mergeArrayBackpressuredMixed1", this);
            this.payloads.mergeArrayBackpressuredMixed2 = _ClassStatement.forPayload(MaybeTest::mergeArrayBackpressuredMixed2, "mergeArrayBackpressuredMixed2", this);
            this.payloads.mergeArrayBackpressuredMixed3 = _ClassStatement.forPayload(MaybeTest::mergeArrayBackpressuredMixed3, "mergeArrayBackpressuredMixed3", this);
            this.payloads.mergeArrayFused = _ClassStatement.forPayload(MaybeTest::mergeArrayFused, "mergeArrayFused", this);
            this.payloads.mergeArrayFusedRace = _ClassStatement.forPayload(MaybeTest::mergeArrayFusedRace, "mergeArrayFusedRace", this);
            this.payloads.mergeArrayZero = _ClassStatement.forPayload(MaybeTest::mergeArrayZero, "mergeArrayZero", this);
            this.payloads.mergeArrayOne = _ClassStatement.forPayload(MaybeTest::mergeArrayOne, "mergeArrayOne", this);
            this.payloads.mergePublisher = _ClassStatement.forPayload(MaybeTest::mergePublisher, "mergePublisher", this);
            this.payloads.mergePublisherMaxConcurrent = _ClassStatement.forPayload(MaybeTest::mergePublisherMaxConcurrent, "mergePublisherMaxConcurrent", this);
            this.payloads.mergeMaybe = _ClassStatement.forPayload(MaybeTest::mergeMaybe, "mergeMaybe", this);
            this.payloads.mergeIterable = _ClassStatement.forPayload(MaybeTest::mergeIterable, "mergeIterable", this);
            this.payloads.mergeALot = _ClassStatement.forPayload(MaybeTest::mergeALot, "mergeALot", this);
            this.payloads.mergeALotLastEmpty = _ClassStatement.forPayload(MaybeTest::mergeALotLastEmpty, "mergeALotLastEmpty", this);
            this.payloads.mergeALotFused = _ClassStatement.forPayload(MaybeTest::mergeALotFused, "mergeALotFused", this);
            this.payloads.mergeErrorSuccess = _ClassStatement.forPayload(MaybeTest::mergeErrorSuccess, "mergeErrorSuccess", this);
            this.payloads.mergeSuccessError = _ClassStatement.forPayload(MaybeTest::mergeSuccessError, "mergeSuccessError", this);
            this.payloads.subscribeZero = _ClassStatement.forPayload(MaybeTest::subscribeZero, "subscribeZero", this);
            this.payloads.subscribeZeroError = _ClassStatement.forPayload(MaybeTest::subscribeZeroError, "subscribeZeroError", this);
            this.payloads.subscribeToOnSuccess = _ClassStatement.forPayload(MaybeTest::subscribeToOnSuccess, "subscribeToOnSuccess", this);
            this.payloads.subscribeToOnError = _ClassStatement.forPayload(MaybeTest::subscribeToOnError, "subscribeToOnError", this);
            this.payloads.subscribeToOnComplete = _ClassStatement.forPayload(MaybeTest::subscribeToOnComplete, "subscribeToOnComplete", this);
            this.payloads.subscribeWith = _ClassStatement.forPayload(MaybeTest::subscribeWith, "subscribeWith", this);
            this.payloads.doOnEventSuccess = _ClassStatement.forPayload(MaybeTest::doOnEventSuccess, "doOnEventSuccess", this);
            this.payloads.doOnEventError = _ClassStatement.forPayload(MaybeTest::doOnEventError, "doOnEventError", this);
            this.payloads.doOnEventComplete = _ClassStatement.forPayload(MaybeTest::doOnEventComplete, "doOnEventComplete", this);
            this.payloads.doOnEventSuccessThrows = _ClassStatement.forPayload(MaybeTest::doOnEventSuccessThrows, "doOnEventSuccessThrows", this);
            this.payloads.doOnEventErrorThrows = _ClassStatement.forPayload(MaybeTest::doOnEventErrorThrows, "doOnEventErrorThrows", this);
            this.payloads.doOnEventCompleteThrows = _ClassStatement.forPayload(MaybeTest::doOnEventCompleteThrows, "doOnEventCompleteThrows", this);
            this.payloads.concatArrayDelayError = _ClassStatement.forPayload(MaybeTest::concatArrayDelayError, "concatArrayDelayError", this);
            this.payloads.concatIterableDelayError = _ClassStatement.forPayload(MaybeTest::concatIterableDelayError, "concatIterableDelayError", this);
            this.payloads.concatPublisherDelayError = _ClassStatement.forPayload(MaybeTest::concatPublisherDelayError, "concatPublisherDelayError", this);
            this.payloads.concatPublisherDelayErrorPrefetch = _ClassStatement.forPayload(MaybeTest::concatPublisherDelayErrorPrefetch, "concatPublisherDelayErrorPrefetch", this);
            this.payloads.concatEagerArray = _ClassStatement.forPayload(MaybeTest::concatEagerArray, "concatEagerArray", this);
            this.payloads.concatEagerIterable = _ClassStatement.forPayload(MaybeTest::concatEagerIterable, "concatEagerIterable", this);
            this.payloads.concatEagerPublisher = _ClassStatement.forPayload(MaybeTest::concatEagerPublisher, "concatEagerPublisher", this);
            this.payloads.fromFuture = _ClassStatement.forPayload(MaybeTest::fromFuture, "fromFuture", this);
            this.payloads.mergeArrayDelayError = _ClassStatement.forPayload(MaybeTest::mergeArrayDelayError, "mergeArrayDelayError", this);
            this.payloads.mergeIterableDelayError = _ClassStatement.forPayload(MaybeTest::mergeIterableDelayError, "mergeIterableDelayError", this);
            this.payloads.mergePublisherDelayError = _ClassStatement.forPayload(MaybeTest::mergePublisherDelayError, "mergePublisherDelayError", this);
            this.payloads.mergeDelayError2 = _ClassStatement.forPayload(MaybeTest::mergeDelayError2, "mergeDelayError2", this);
            this.payloads.mergeDelayError3 = _ClassStatement.forPayload(MaybeTest::mergeDelayError3, "mergeDelayError3", this);
            this.payloads.mergeDelayError4 = _ClassStatement.forPayload(MaybeTest::mergeDelayError4, "mergeDelayError4", this);
            this.payloads.sequenceEqual = _ClassStatement.forPayload(MaybeTest::sequenceEqual, "sequenceEqual", this);
            this.payloads.timer = _ClassStatement.forPayload(MaybeTest::timer, "timer", this);
            this.payloads.blockingGet = _ClassStatement.forPayload(MaybeTest::blockingGet, "blockingGet", this);
            this.payloads.flatMapContinuation = _ClassStatement.forPayload(MaybeTest::flatMapContinuation, "flatMapContinuation", this);
            this.payloads.using = _ClassStatement.forPayload(MaybeTest::using, "using", this);
            this.payloads.usingNonEager = _ClassStatement.forPayload(MaybeTest::usingNonEager, "usingNonEager", this);
            this.payloads.zipArray = _ClassStatement.forPayload(MaybeTest::zipArray, "zipArray", this);
            this.payloads.zipIterable = _ClassStatement.forPayload(MaybeTest::zipIterable, "zipIterable", this);
            this.payloads.zip2 = _ClassStatement.forPayload(MaybeTest::zip2, "zip2", this);
            this.payloads.zipWith = _ClassStatement.forPayload(MaybeTest::zipWith, "zipWith", this);
            this.payloads.zip3 = _ClassStatement.forPayload(MaybeTest::zip3, "zip3", this);
            this.payloads.zip4 = _ClassStatement.forPayload(MaybeTest::zip4, "zip4", this);
            this.payloads.zip5 = _ClassStatement.forPayload(MaybeTest::zip5, "zip5", this);
            this.payloads.zip6 = _ClassStatement.forPayload(MaybeTest::zip6, "zip6", this);
            this.payloads.zip7 = _ClassStatement.forPayload(MaybeTest::zip7, "zip7", this);
            this.payloads.zip8 = _ClassStatement.forPayload(MaybeTest::zip8, "zip8", this);
            this.payloads.zip9 = _ClassStatement.forPayload(MaybeTest::zip9, "zip9", this);
            this.payloads.ambWith1SignalsSuccess = _ClassStatement.forPayload(MaybeTest::ambWith1SignalsSuccess, "ambWith1SignalsSuccess", this);
            this.payloads.ambWith2SignalsSuccess = _ClassStatement.forPayload(MaybeTest::ambWith2SignalsSuccess, "ambWith2SignalsSuccess", this);
            this.payloads.zipIterableObject = _ClassStatement.forPayload(MaybeTest::zipIterableObject, "zipIterableObject", this);
            this.payloads.onTerminateDetach = _ClassStatement.forPayload(MaybeTest::onTerminateDetach, "onTerminateDetach", this);
            this.payloads.repeat = _ClassStatement.forPayload(MaybeTest::repeat, "repeat", this);
            this.payloads.retry = _ClassStatement.forPayload(MaybeTest::retry, "retry", this);
            this.payloads.onErrorResumeWithEmpty = _ClassStatement.forPayload(MaybeTest::onErrorResumeWithEmpty, "onErrorResumeWithEmpty", this);
            this.payloads.onErrorResumeWithValue = _ClassStatement.forPayload(MaybeTest::onErrorResumeWithValue, "onErrorResumeWithValue", this);
            this.payloads.onErrorResumeWithError = _ClassStatement.forPayload(MaybeTest::onErrorResumeWithError, "onErrorResumeWithError", this);
            this.payloads.valueConcatWithValue = _ClassStatement.forPayload(MaybeTest::valueConcatWithValue, "valueConcatWithValue", this);
            this.payloads.errorConcatWithValue = _ClassStatement.forPayload(MaybeTest::errorConcatWithValue, "errorConcatWithValue", this);
            this.payloads.valueConcatWithError = _ClassStatement.forPayload(MaybeTest::valueConcatWithError, "valueConcatWithError", this);
            this.payloads.emptyConcatWithValue = _ClassStatement.forPayload(MaybeTest::emptyConcatWithValue, "emptyConcatWithValue", this);
            this.payloads.emptyConcatWithError = _ClassStatement.forPayload(MaybeTest::emptyConcatWithError, "emptyConcatWithError", this);
        }
    }
}
