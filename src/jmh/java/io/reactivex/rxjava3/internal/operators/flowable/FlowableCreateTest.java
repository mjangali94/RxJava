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
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.Cancellable;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableCreateTest extends RxJavaTest {

    @Test
    public void basic() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable d = Disposable.empty();
            Flowable.<Integer>create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    e.setDisposable(d);
                    e.onNext(1);
                    e.onNext(2);
                    e.onNext(3);
                    e.onComplete();
                    e.onError(new TestException("first"));
                    e.onNext(4);
                    e.onError(new TestException("second"));
                    e.onComplete();
                }
            }, BackpressureStrategy.BUFFER).test().assertResult(1, 2, 3);
            assertTrue(d.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "first");
            TestHelper.assertUndeliverable(errors, 1, TestException.class, "second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void basicWithCancellable() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable d1 = Disposable.empty();
            final Disposable d2 = Disposable.empty();
            Flowable.<Integer>create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    e.setDisposable(d1);
                    e.setCancellable(new Cancellable() {

                        @Override
                        public void cancel() throws Exception {
                            d2.dispose();
                        }
                    });
                    e.onNext(1);
                    e.onNext(2);
                    e.onNext(3);
                    e.onComplete();
                    e.onError(new TestException("first"));
                    e.onNext(4);
                    e.onError(new TestException("second"));
                    e.onComplete();
                }
            }, BackpressureStrategy.BUFFER).test().assertResult(1, 2, 3);
            assertTrue(d1.isDisposed());
            assertTrue(d2.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "first");
            TestHelper.assertUndeliverable(errors, 1, TestException.class, "second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void basicWithError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable d = Disposable.empty();
            Flowable.<Integer>create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    e.setDisposable(d);
                    e.onNext(1);
                    e.onNext(2);
                    e.onNext(3);
                    e.onError(new TestException());
                    e.onComplete();
                    e.onNext(4);
                    e.onError(new TestException("second"));
                }
            }, BackpressureStrategy.BUFFER).test().assertFailure(TestException.class, 1, 2, 3);
            assertTrue(d.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void basicSerialized() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable d = Disposable.empty();
            Flowable.<Integer>create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    e = e.serialize();
                    e.setDisposable(d);
                    e.onNext(1);
                    e.onNext(2);
                    e.onNext(3);
                    e.onComplete();
                    e.onError(new TestException("first"));
                    e.onNext(4);
                    e.onError(new TestException("second"));
                    e.onComplete();
                }
            }, BackpressureStrategy.BUFFER).test().assertResult(1, 2, 3);
            assertTrue(d.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "first");
            TestHelper.assertUndeliverable(errors, 1, TestException.class, "second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void basicWithErrorSerialized() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable d = Disposable.empty();
            Flowable.<Integer>create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    e = e.serialize();
                    e.setDisposable(d);
                    e.onNext(1);
                    e.onNext(2);
                    e.onNext(3);
                    e.onError(new TestException());
                    e.onComplete();
                    e.onNext(4);
                    e.onError(new TestException("second"));
                }
            }, BackpressureStrategy.BUFFER).test().assertFailure(TestException.class, 1, 2, 3);
            assertTrue(d.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void wrap() {
        Flowable.fromPublisher(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                subscriber.onNext(1);
                subscriber.onNext(2);
                subscriber.onNext(3);
                subscriber.onNext(4);
                subscriber.onNext(5);
                subscriber.onComplete();
            }
        }).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test
    public void unsafe() {
        Flowable.unsafeCreate(new Publisher<Integer>() {

            @Override
            public void subscribe(Subscriber<? super Integer> subscriber) {
                subscriber.onSubscribe(new BooleanSubscription());
                subscriber.onNext(1);
                subscriber.onNext(2);
                subscriber.onNext(3);
                subscriber.onNext(4);
                subscriber.onNext(5);
                subscriber.onComplete();
            }
        }).test().assertResult(1, 2, 3, 4, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unsafeWithFlowable() {
        Flowable.unsafeCreate(Flowable.just(1));
    }

    @Test
    public void createNullValueBuffer() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Throwable[] error = { null };
            Flowable.create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    try {
                        e.onNext(null);
                        e.onNext(1);
                        e.onError(new TestException());
                        e.onComplete();
                    } catch (Throwable ex) {
                        error[0] = ex;
                    }
                }
            }, BackpressureStrategy.BUFFER).test().assertFailure(NullPointerException.class);
            assertNull(error[0]);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void createNullValueLatest() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Throwable[] error = { null };
            Flowable.create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    try {
                        e.onNext(null);
                        e.onNext(1);
                        e.onError(new TestException());
                        e.onComplete();
                    } catch (Throwable ex) {
                        error[0] = ex;
                    }
                }
            }, BackpressureStrategy.LATEST).test().assertFailure(NullPointerException.class);
            assertNull(error[0]);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void createNullValueError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Throwable[] error = { null };
            Flowable.create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    try {
                        e.onNext(null);
                        e.onNext(1);
                        e.onError(new TestException());
                        e.onComplete();
                    } catch (Throwable ex) {
                        error[0] = ex;
                    }
                }
            }, BackpressureStrategy.ERROR).test().assertFailure(NullPointerException.class);
            assertNull(error[0]);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void createNullValueDrop() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Throwable[] error = { null };
            Flowable.create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    try {
                        e.onNext(null);
                        e.onNext(1);
                        e.onError(new TestException());
                        e.onComplete();
                    } catch (Throwable ex) {
                        error[0] = ex;
                    }
                }
            }, BackpressureStrategy.DROP).test().assertFailure(NullPointerException.class);
            assertNull(error[0]);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void createNullValueMissing() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Throwable[] error = { null };
            Flowable.create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    try {
                        e.onNext(null);
                        e.onNext(1);
                        e.onError(new TestException());
                        e.onComplete();
                    } catch (Throwable ex) {
                        error[0] = ex;
                    }
                }
            }, BackpressureStrategy.MISSING).test().assertFailure(NullPointerException.class);
            assertNull(error[0]);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void createNullValueBufferSerialized() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Throwable[] error = { null };
            Flowable.create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    e = e.serialize();
                    try {
                        e.onNext(null);
                        e.onNext(1);
                        e.onError(new TestException());
                        e.onComplete();
                    } catch (Throwable ex) {
                        error[0] = ex;
                    }
                }
            }, BackpressureStrategy.BUFFER).test().assertFailure(NullPointerException.class);
            assertNull(error[0]);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void createNullValueLatestSerialized() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Throwable[] error = { null };
            Flowable.create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    e = e.serialize();
                    try {
                        e.onNext(null);
                        e.onNext(1);
                        e.onError(new TestException());
                        e.onComplete();
                    } catch (Throwable ex) {
                        error[0] = ex;
                    }
                }
            }, BackpressureStrategy.LATEST).test().assertFailure(NullPointerException.class);
            assertNull(error[0]);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void createNullValueErrorSerialized() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Throwable[] error = { null };
            Flowable.create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    e = e.serialize();
                    try {
                        e.onNext(null);
                        e.onNext(1);
                        e.onError(new TestException());
                        e.onComplete();
                    } catch (Throwable ex) {
                        error[0] = ex;
                    }
                }
            }, BackpressureStrategy.ERROR).test().assertFailure(NullPointerException.class);
            assertNull(error[0]);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void createNullValueDropSerialized() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Throwable[] error = { null };
            Flowable.create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    e = e.serialize();
                    try {
                        e.onNext(null);
                        e.onNext(1);
                        e.onError(new TestException());
                        e.onComplete();
                    } catch (Throwable ex) {
                        error[0] = ex;
                    }
                }
            }, BackpressureStrategy.DROP).test().assertFailure(NullPointerException.class);
            assertNull(error[0]);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void createNullValueMissingSerialized() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Throwable[] error = { null };
            Flowable.create(new FlowableOnSubscribe<Integer>() {

                @Override
                public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                    e = e.serialize();
                    try {
                        e.onNext(null);
                        e.onNext(1);
                        e.onError(new TestException());
                        e.onComplete();
                    } catch (Throwable ex) {
                        error[0] = ex;
                    }
                }
            }, BackpressureStrategy.MISSING).test().assertFailure(NullPointerException.class);
            assertNull(error[0]);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onErrorRace() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            Flowable<Object> source = Flowable.create(new FlowableOnSubscribe<Object>() {

                @Override
                public void subscribe(FlowableEmitter<Object> e) throws Exception {
                    final FlowableEmitter<Object> f = e.serialize();
                    final TestException ex = new TestException();
                    Runnable r1 = new Runnable() {

                        @Override
                        public void run() {
                            f.onError(null);
                        }
                    };
                    Runnable r2 = new Runnable() {

                        @Override
                        public void run() {
                            f.onError(ex);
                        }
                    };
                    TestHelper.race(r1, r2);
                }
            }, m);
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                    source.test().assertFailure(Throwable.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
            assertFalse(errors.isEmpty());
        }
    }

    @Test
    public void onCompleteRace() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            Flowable<Object> source = Flowable.create(new FlowableOnSubscribe<Object>() {

                @Override
                public void subscribe(FlowableEmitter<Object> e) throws Exception {
                    final FlowableEmitter<Object> f = e.serialize();
                    Runnable r1 = new Runnable() {

                        @Override
                        public void run() {
                            f.onComplete();
                        }
                    };
                    Runnable r2 = new Runnable() {

                        @Override
                        public void run() {
                            f.onComplete();
                        }
                    };
                    TestHelper.race(r1, r2);
                }
            }, m);
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                source.test().assertResult();
            }
        }
    }

    @Test
    public void nullValue() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            Flowable.create(new FlowableOnSubscribe<Object>() {

                @Override
                public void subscribe(FlowableEmitter<Object> e) throws Exception {
                    e.onNext(null);
                }
            }, m).test().assertFailure(NullPointerException.class);
        }
    }

    @Test
    public void nullThrowable() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            // System.out.println(m);
            Flowable.create(new FlowableOnSubscribe<Object>() {

                @Override
                public void subscribe(FlowableEmitter<Object> e) throws Exception {
                    e.onError(null);
                }
            }, m).test().assertFailure(NullPointerException.class);
        }
    }

    @Test
    public void serializedConcurrentOnNextOnError() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            Flowable.create(new FlowableOnSubscribe<Object>() {

                @Override
                public void subscribe(FlowableEmitter<Object> e) throws Exception {
                    final FlowableEmitter<Object> f = e.serialize();
                    Runnable r1 = new Runnable() {

                        @Override
                        public void run() {
                            for (int i = 0; i < 1000; i++) {
                                f.onNext(1);
                            }
                        }
                    };
                    Runnable r2 = new Runnable() {

                        @Override
                        public void run() {
                            for (int i = 0; i < 100; i++) {
                                f.onNext(1);
                            }
                            f.onError(new TestException());
                        }
                    };
                    TestHelper.race(r1, r2);
                }
            }, m).to(TestHelper.<Object>testConsumer()).assertSubscribed().assertNotComplete().assertError(TestException.class);
        }
    }

    @Test
    public void callbackThrows() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            Flowable.create(new FlowableOnSubscribe<Object>() {

                @Override
                public void subscribe(FlowableEmitter<Object> e) throws Exception {
                    throw new TestException();
                }
            }, m).test().assertFailure(TestException.class);
        }
    }

    @Test
    public void nullValueSync() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            Flowable.create(new FlowableOnSubscribe<Object>() {

                @Override
                public void subscribe(FlowableEmitter<Object> e) throws Exception {
                    e.serialize().onNext(null);
                }
            }, m).test().assertFailure(NullPointerException.class);
        }
    }

    @Test
    public void createNullValue() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final Throwable[] error = { null };
                Flowable.create(new FlowableOnSubscribe<Integer>() {

                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        try {
                            e.onNext(null);
                            e.onNext(1);
                            e.onError(new TestException());
                            e.onComplete();
                        } catch (Throwable ex) {
                            error[0] = ex;
                        }
                    }
                }, m).test().assertFailure(NullPointerException.class);
                assertNull(error[0]);
                TestHelper.assertUndeliverable(errors, 0, TestException.class);
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void onErrorCrash() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            Flowable.create(new FlowableOnSubscribe<Object>() {

                @Override
                public void subscribe(FlowableEmitter<Object> e) throws Exception {
                    Disposable d = Disposable.empty();
                    e.setDisposable(d);
                    try {
                        e.onError(new IOException());
                        fail("Should have thrown");
                    } catch (TestException ex) {
                    // expected
                    }
                    assertTrue(d.isDisposed());
                }
            }, m).subscribe(new FlowableSubscriber<Object>() {

                @Override
                public void onSubscribe(Subscription s) {
                }

                @Override
                public void onNext(Object value) {
                }

                @Override
                public void onError(Throwable e) {
                    throw new TestException();
                }

                @Override
                public void onComplete() {
                }
            });
        }
    }

    @Test
    public void onCompleteCrash() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            Flowable.create(new FlowableOnSubscribe<Object>() {

                @Override
                public void subscribe(FlowableEmitter<Object> e) throws Exception {
                    Disposable d = Disposable.empty();
                    e.setDisposable(d);
                    try {
                        e.onComplete();
                        fail("Should have thrown");
                    } catch (TestException ex) {
                    // expected
                    }
                    assertTrue(d.isDisposed());
                }
            }, m).subscribe(new FlowableSubscriber<Object>() {

                @Override
                public void onSubscribe(Subscription s) {
                }

                @Override
                public void onNext(Object value) {
                }

                @Override
                public void onError(Throwable e) {
                }

                @Override
                public void onComplete() {
                    throw new TestException();
                }
            });
        }
    }

    @Test
    public void createNullValueSerialized() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final Throwable[] error = { null };
                Flowable.create(new FlowableOnSubscribe<Integer>() {

                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        e = e.serialize();
                        try {
                            e.onNext(null);
                            e.onNext(1);
                            e.onError(new TestException());
                            e.onComplete();
                        } catch (Throwable ex) {
                            error[0] = ex;
                        }
                    }
                }, m).test().assertFailure(NullPointerException.class);
                assertNull(error[0]);
                TestHelper.assertUndeliverable(errors, 0, TestException.class);
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void nullThrowableSync() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            Flowable.create(new FlowableOnSubscribe<Object>() {

                @Override
                public void subscribe(FlowableEmitter<Object> e) throws Exception {
                    e.serialize().onError(null);
                }
            }, m).test().assertFailure(NullPointerException.class);
        }
    }

    @Test
    public void serializedConcurrentOnNext() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            Flowable.create(new FlowableOnSubscribe<Object>() {

                @Override
                public void subscribe(FlowableEmitter<Object> e) throws Exception {
                    final FlowableEmitter<Object> f = e.serialize();
                    Runnable r1 = new Runnable() {

                        @Override
                        public void run() {
                            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                                f.onNext(1);
                            }
                        }
                    };
                    TestHelper.race(r1, r1);
                }
            }, m).take(TestHelper.RACE_DEFAULT_LOOPS).to(TestHelper.<Object>testConsumer()).assertSubscribed().assertValueCount(TestHelper.RACE_DEFAULT_LOOPS).assertComplete().assertNoErrors();
        }
    }

    @Test
    public void serializedConcurrentOnNextOnComplete() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            TestSubscriberEx<Object> ts = Flowable.create(new FlowableOnSubscribe<Object>() {

                @Override
                public void subscribe(FlowableEmitter<Object> e) throws Exception {
                    final FlowableEmitter<Object> f = e.serialize();
                    Runnable r1 = new Runnable() {

                        @Override
                        public void run() {
                            for (int i = 0; i < 1000; i++) {
                                f.onNext(1);
                            }
                        }
                    };
                    Runnable r2 = new Runnable() {

                        @Override
                        public void run() {
                            for (int i = 0; i < 100; i++) {
                                f.onNext(1);
                            }
                            f.onComplete();
                        }
                    };
                    TestHelper.race(r1, r2);
                }
            }, m).to(TestHelper.<Object>testConsumer()).assertSubscribed().assertComplete().assertNoErrors();
            int c = ts.values().size();
            assertTrue("" + c, c >= 100);
        }
    }

    @Test
    public void serialized() {
        for (BackpressureStrategy m : BackpressureStrategy.values()) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                Flowable.create(new FlowableOnSubscribe<Object>() {

                    @Override
                    public void subscribe(FlowableEmitter<Object> e) throws Exception {
                        FlowableEmitter<Object> f = e.serialize();
                        assertSame(f, f.serialize());
                        assertFalse(f.isCancelled());
                        final int[] calls = { 0 };
                        f.setCancellable(new Cancellable() {

                            @Override
                            public void cancel() throws Exception {
                                calls[0]++;
                            }
                        });
                        e.onComplete();
                        assertTrue(f.isCancelled());
                        assertEquals(1, calls[0]);
                    }
                }, m).test().assertResult();
                assertTrue(errors.toString(), errors.isEmpty());
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void tryOnError() {
        for (BackpressureStrategy strategy : BackpressureStrategy.values()) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final Boolean[] response = { null };
                Flowable.create(new FlowableOnSubscribe<Object>() {

                    @Override
                    public void subscribe(FlowableEmitter<Object> e) throws Exception {
                        e.onNext(1);
                        response[0] = e.tryOnError(new TestException());
                    }
                }, strategy).take(1).test().withTag(strategy.toString()).assertResult(1);
                assertFalse(response[0]);
                assertTrue(strategy + ": " + errors.toString(), errors.isEmpty());
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void tryOnErrorSerialized() {
        for (BackpressureStrategy strategy : BackpressureStrategy.values()) {
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                final Boolean[] response = { null };
                Flowable.create(new FlowableOnSubscribe<Object>() {

                    @Override
                    public void subscribe(FlowableEmitter<Object> e) throws Exception {
                        e = e.serialize();
                        e.onNext(1);
                        response[0] = e.tryOnError(new TestException());
                    }
                }, strategy).take(1).test().withTag(strategy.toString()).assertResult(1);
                assertFalse(response[0]);
                assertTrue(strategy + ": " + errors.toString(), errors.isEmpty());
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void emittersHasToString() {
        Map<BackpressureStrategy, Class<? extends FlowableEmitter>> emitterMap = new HashMap<>();
        emitterMap.put(BackpressureStrategy.MISSING, FlowableCreate.MissingEmitter.class);
        emitterMap.put(BackpressureStrategy.ERROR, FlowableCreate.ErrorAsyncEmitter.class);
        emitterMap.put(BackpressureStrategy.DROP, FlowableCreate.DropAsyncEmitter.class);
        emitterMap.put(BackpressureStrategy.LATEST, FlowableCreate.LatestAsyncEmitter.class);
        emitterMap.put(BackpressureStrategy.BUFFER, FlowableCreate.BufferAsyncEmitter.class);
        for (final Map.Entry<BackpressureStrategy, Class<? extends FlowableEmitter>> entry : emitterMap.entrySet()) {
            Flowable.create(new FlowableOnSubscribe<Object>() {

                @Override
                public void subscribe(FlowableEmitter<Object> emitter) throws Exception {
                    assertTrue(emitter.toString().contains(entry.getValue().getSimpleName()));
                    assertTrue(emitter.serialize().toString().contains(entry.getValue().getSimpleName()));
                }
            }, entry.getKey()).test().assertEmpty();
        }
    }

    @Test
    public void serializedMissingMoreWorkWithComplete() {
        AtomicReference<FlowableEmitter<Integer>> ref = new AtomicReference<>();
        Flowable.<Integer>create(emitter -> {
            emitter = emitter.serialize();
            ref.set(emitter);
            assertEquals(Long.MAX_VALUE, emitter.requested());
            emitter.onNext(1);
        }, BackpressureStrategy.MISSING).doOnNext(v -> {
            if (v == 1) {
                ref.get().onNext(2);
                ref.get().onComplete();
            }
        }).test().assertResult(1, 2);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.create(e -> {
        }, BackpressureStrategy.BUFFER));
    }

    @Test
    public void tryOnErrorNull() {
        Flowable.create(emitter -> emitter.tryOnError(null), BackpressureStrategy.MISSING).test().assertFailure(NullPointerException.class);
    }

    @Test
    public void serializedCompleteOnNext() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.<Integer>create(emitter -> {
            emitter = emitter.serialize();
            emitter.onComplete();
            emitter.onNext(1);
        }, BackpressureStrategy.MISSING).subscribe(ts);
        ts.assertResult();
    }

    @Test
    public void serializedCancelOnNext() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Flowable.<Integer>create(emitter -> {
            emitter = emitter.serialize();
            ts.cancel();
            emitter.onNext(1);
        }, BackpressureStrategy.MISSING).subscribe(ts);
        ts.assertEmpty();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableCreateTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basic() throws java.lang.Throwable {
            this.payloads.basic.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicWithCancellable() throws java.lang.Throwable {
            this.payloads.basicWithCancellable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicWithError() throws java.lang.Throwable {
            this.payloads.basicWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicSerialized() throws java.lang.Throwable {
            this.payloads.basicSerialized.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basicWithErrorSerialized() throws java.lang.Throwable {
            this.payloads.basicWithErrorSerialized.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_wrap() throws java.lang.Throwable {
            this.payloads.wrap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsafe() throws java.lang.Throwable {
            this.payloads.unsafe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsafeWithFlowable() throws java.lang.Throwable {
            this.payloads.unsafeWithFlowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullValueBuffer() throws java.lang.Throwable {
            this.payloads.createNullValueBuffer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullValueLatest() throws java.lang.Throwable {
            this.payloads.createNullValueLatest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullValueError() throws java.lang.Throwable {
            this.payloads.createNullValueError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullValueDrop() throws java.lang.Throwable {
            this.payloads.createNullValueDrop.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullValueMissing() throws java.lang.Throwable {
            this.payloads.createNullValueMissing.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullValueBufferSerialized() throws java.lang.Throwable {
            this.payloads.createNullValueBufferSerialized.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullValueLatestSerialized() throws java.lang.Throwable {
            this.payloads.createNullValueLatestSerialized.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullValueErrorSerialized() throws java.lang.Throwable {
            this.payloads.createNullValueErrorSerialized.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullValueDropSerialized() throws java.lang.Throwable {
            this.payloads.createNullValueDropSerialized.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullValueMissingSerialized() throws java.lang.Throwable {
            this.payloads.createNullValueMissingSerialized.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorRace() throws java.lang.Throwable {
            this.payloads.onErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteRace() throws java.lang.Throwable {
            this.payloads.onCompleteRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullValue() throws java.lang.Throwable {
            this.payloads.nullValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullThrowable() throws java.lang.Throwable {
            this.payloads.nullThrowable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedConcurrentOnNextOnError() throws java.lang.Throwable {
            this.payloads.serializedConcurrentOnNextOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_callbackThrows() throws java.lang.Throwable {
            this.payloads.callbackThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullValueSync() throws java.lang.Throwable {
            this.payloads.nullValueSync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullValue() throws java.lang.Throwable {
            this.payloads.createNullValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCrash() throws java.lang.Throwable {
            this.payloads.onErrorCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteCrash() throws java.lang.Throwable {
            this.payloads.onCompleteCrash.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createNullValueSerialized() throws java.lang.Throwable {
            this.payloads.createNullValueSerialized.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_nullThrowableSync() throws java.lang.Throwable {
            this.payloads.nullThrowableSync.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedConcurrentOnNext() throws java.lang.Throwable {
            this.payloads.serializedConcurrentOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedConcurrentOnNextOnComplete() throws java.lang.Throwable {
            this.payloads.serializedConcurrentOnNextOnComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serialized() throws java.lang.Throwable {
            this.payloads.serialized.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryOnError() throws java.lang.Throwable {
            this.payloads.tryOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryOnErrorSerialized() throws java.lang.Throwable {
            this.payloads.tryOnErrorSerialized.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emittersHasToString() throws java.lang.Throwable {
            this.payloads.emittersHasToString.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedMissingMoreWorkWithComplete() throws java.lang.Throwable {
            this.payloads.serializedMissingMoreWorkWithComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tryOnErrorNull() throws java.lang.Throwable {
            this.payloads.tryOnErrorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedCompleteOnNext() throws java.lang.Throwable {
            this.payloads.serializedCompleteOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serializedCancelOnNext() throws java.lang.Throwable {
            this.payloads.serializedCancelOnNext.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCreateTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCreateTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCreateTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCreateTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableCreateTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableCreateTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableCreateTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableCreateTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement basic;

            public org.junit.runners.model.Statement basicWithCancellable;

            public org.junit.runners.model.Statement basicWithError;

            public org.junit.runners.model.Statement basicSerialized;

            public org.junit.runners.model.Statement basicWithErrorSerialized;

            public org.junit.runners.model.Statement wrap;

            public org.junit.runners.model.Statement unsafe;

            public org.junit.runners.model.Statement unsafeWithFlowable;

            public org.junit.runners.model.Statement createNullValueBuffer;

            public org.junit.runners.model.Statement createNullValueLatest;

            public org.junit.runners.model.Statement createNullValueError;

            public org.junit.runners.model.Statement createNullValueDrop;

            public org.junit.runners.model.Statement createNullValueMissing;

            public org.junit.runners.model.Statement createNullValueBufferSerialized;

            public org.junit.runners.model.Statement createNullValueLatestSerialized;

            public org.junit.runners.model.Statement createNullValueErrorSerialized;

            public org.junit.runners.model.Statement createNullValueDropSerialized;

            public org.junit.runners.model.Statement createNullValueMissingSerialized;

            public org.junit.runners.model.Statement onErrorRace;

            public org.junit.runners.model.Statement onCompleteRace;

            public org.junit.runners.model.Statement nullValue;

            public org.junit.runners.model.Statement nullThrowable;

            public org.junit.runners.model.Statement serializedConcurrentOnNextOnError;

            public org.junit.runners.model.Statement callbackThrows;

            public org.junit.runners.model.Statement nullValueSync;

            public org.junit.runners.model.Statement createNullValue;

            public org.junit.runners.model.Statement onErrorCrash;

            public org.junit.runners.model.Statement onCompleteCrash;

            public org.junit.runners.model.Statement createNullValueSerialized;

            public org.junit.runners.model.Statement nullThrowableSync;

            public org.junit.runners.model.Statement serializedConcurrentOnNext;

            public org.junit.runners.model.Statement serializedConcurrentOnNextOnComplete;

            public org.junit.runners.model.Statement serialized;

            public org.junit.runners.model.Statement tryOnError;

            public org.junit.runners.model.Statement tryOnErrorSerialized;

            public org.junit.runners.model.Statement emittersHasToString;

            public org.junit.runners.model.Statement serializedMissingMoreWorkWithComplete;

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement tryOnErrorNull;

            public org.junit.runners.model.Statement serializedCompleteOnNext;

            public org.junit.runners.model.Statement serializedCancelOnNext;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.basic = _ClassStatement.forPayload(FlowableCreateTest::basic, "basic", this);
            this.payloads.basicWithCancellable = _ClassStatement.forPayload(FlowableCreateTest::basicWithCancellable, "basicWithCancellable", this);
            this.payloads.basicWithError = _ClassStatement.forPayload(FlowableCreateTest::basicWithError, "basicWithError", this);
            this.payloads.basicSerialized = _ClassStatement.forPayload(FlowableCreateTest::basicSerialized, "basicSerialized", this);
            this.payloads.basicWithErrorSerialized = _ClassStatement.forPayload(FlowableCreateTest::basicWithErrorSerialized, "basicWithErrorSerialized", this);
            this.payloads.wrap = _ClassStatement.forPayload(FlowableCreateTest::wrap, "wrap", this);
            this.payloads.unsafe = _ClassStatement.forPayload(FlowableCreateTest::unsafe, "unsafe", this);
            this.payloads.unsafeWithFlowable = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(FlowableCreateTest::unsafeWithFlowable, java.lang.IllegalArgumentException.class), "unsafeWithFlowable", this);
            this.payloads.createNullValueBuffer = _ClassStatement.forPayload(FlowableCreateTest::createNullValueBuffer, "createNullValueBuffer", this);
            this.payloads.createNullValueLatest = _ClassStatement.forPayload(FlowableCreateTest::createNullValueLatest, "createNullValueLatest", this);
            this.payloads.createNullValueError = _ClassStatement.forPayload(FlowableCreateTest::createNullValueError, "createNullValueError", this);
            this.payloads.createNullValueDrop = _ClassStatement.forPayload(FlowableCreateTest::createNullValueDrop, "createNullValueDrop", this);
            this.payloads.createNullValueMissing = _ClassStatement.forPayload(FlowableCreateTest::createNullValueMissing, "createNullValueMissing", this);
            this.payloads.createNullValueBufferSerialized = _ClassStatement.forPayload(FlowableCreateTest::createNullValueBufferSerialized, "createNullValueBufferSerialized", this);
            this.payloads.createNullValueLatestSerialized = _ClassStatement.forPayload(FlowableCreateTest::createNullValueLatestSerialized, "createNullValueLatestSerialized", this);
            this.payloads.createNullValueErrorSerialized = _ClassStatement.forPayload(FlowableCreateTest::createNullValueErrorSerialized, "createNullValueErrorSerialized", this);
            this.payloads.createNullValueDropSerialized = _ClassStatement.forPayload(FlowableCreateTest::createNullValueDropSerialized, "createNullValueDropSerialized", this);
            this.payloads.createNullValueMissingSerialized = _ClassStatement.forPayload(FlowableCreateTest::createNullValueMissingSerialized, "createNullValueMissingSerialized", this);
            this.payloads.onErrorRace = _ClassStatement.forPayload(FlowableCreateTest::onErrorRace, "onErrorRace", this);
            this.payloads.onCompleteRace = _ClassStatement.forPayload(FlowableCreateTest::onCompleteRace, "onCompleteRace", this);
            this.payloads.nullValue = _ClassStatement.forPayload(FlowableCreateTest::nullValue, "nullValue", this);
            this.payloads.nullThrowable = _ClassStatement.forPayload(FlowableCreateTest::nullThrowable, "nullThrowable", this);
            this.payloads.serializedConcurrentOnNextOnError = _ClassStatement.forPayload(FlowableCreateTest::serializedConcurrentOnNextOnError, "serializedConcurrentOnNextOnError", this);
            this.payloads.callbackThrows = _ClassStatement.forPayload(FlowableCreateTest::callbackThrows, "callbackThrows", this);
            this.payloads.nullValueSync = _ClassStatement.forPayload(FlowableCreateTest::nullValueSync, "nullValueSync", this);
            this.payloads.createNullValue = _ClassStatement.forPayload(FlowableCreateTest::createNullValue, "createNullValue", this);
            this.payloads.onErrorCrash = _ClassStatement.forPayload(FlowableCreateTest::onErrorCrash, "onErrorCrash", this);
            this.payloads.onCompleteCrash = _ClassStatement.forPayload(FlowableCreateTest::onCompleteCrash, "onCompleteCrash", this);
            this.payloads.createNullValueSerialized = _ClassStatement.forPayload(FlowableCreateTest::createNullValueSerialized, "createNullValueSerialized", this);
            this.payloads.nullThrowableSync = _ClassStatement.forPayload(FlowableCreateTest::nullThrowableSync, "nullThrowableSync", this);
            this.payloads.serializedConcurrentOnNext = _ClassStatement.forPayload(FlowableCreateTest::serializedConcurrentOnNext, "serializedConcurrentOnNext", this);
            this.payloads.serializedConcurrentOnNextOnComplete = _ClassStatement.forPayload(FlowableCreateTest::serializedConcurrentOnNextOnComplete, "serializedConcurrentOnNextOnComplete", this);
            this.payloads.serialized = _ClassStatement.forPayload(FlowableCreateTest::serialized, "serialized", this);
            this.payloads.tryOnError = _ClassStatement.forPayload(FlowableCreateTest::tryOnError, "tryOnError", this);
            this.payloads.tryOnErrorSerialized = _ClassStatement.forPayload(FlowableCreateTest::tryOnErrorSerialized, "tryOnErrorSerialized", this);
            this.payloads.emittersHasToString = _ClassStatement.forPayload(FlowableCreateTest::emittersHasToString, "emittersHasToString", this);
            this.payloads.serializedMissingMoreWorkWithComplete = _ClassStatement.forPayload(FlowableCreateTest::serializedMissingMoreWorkWithComplete, "serializedMissingMoreWorkWithComplete", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableCreateTest::badRequest, "badRequest", this);
            this.payloads.tryOnErrorNull = _ClassStatement.forPayload(FlowableCreateTest::tryOnErrorNull, "tryOnErrorNull", this);
            this.payloads.serializedCompleteOnNext = _ClassStatement.forPayload(FlowableCreateTest::serializedCompleteOnNext, "serializedCompleteOnNext", this);
            this.payloads.serializedCancelOnNext = _ClassStatement.forPayload(FlowableCreateTest::serializedCancelOnNext, "serializedCancelOnNext", this);
        }
    }
}
