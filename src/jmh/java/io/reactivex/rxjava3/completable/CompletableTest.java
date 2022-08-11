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
package io.reactivex.rxjava3.completable;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.*;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.disposables.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.*;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

/**
 * Test Completable methods and operators.
 */
public class CompletableTest extends RxJavaTest {

    /**
     * Iterable that returns an Iterator that throws in its hasNext method.
     */
    static final class IterableIteratorNextThrows implements Iterable<Completable> {

        @Override
        public Iterator<Completable> iterator() {
            return new Iterator<Completable>() {

                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public Completable next() {
                    throw new TestException();
                }

                @Override
                public void remove() {
                }
            };
        }
    }

    /**
     * Iterable that returns an Iterator that throws in its next method.
     */
    static final class IterableIteratorHasNextThrows implements Iterable<Completable> {

        @Override
        public Iterator<Completable> iterator() {
            return new Iterator<Completable>() {

                @Override
                public boolean hasNext() {
                    throw new TestException();
                }

                @Override
                public Completable next() {
                    return null;
                }

                @Override
                public void remove() {
                }
            };
        }
    }

    /**
     * A class containing a completable instance and counts the number of subscribers.
     */
    static final class NormalCompletable extends AtomicInteger {

        private static final long serialVersionUID = 7192337844700923752L;

        public final Completable completable = Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                getAndIncrement();
                EmptyDisposable.complete(observer);
            }
        });

        /**
         * Asserts the given number of subscriptions happened.
         * @param n the expected number of subscriptions
         */
        public void assertSubscriptions(int n) {
            Assert.assertEquals(n, get());
        }
    }

    /**
     * A class containing a completable instance that emits a TestException and counts
     * the number of subscribers.
     */
    static final class ErrorCompletable extends AtomicInteger {

        private static final long serialVersionUID = 7192337844700923752L;

        public final Completable completable = Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                getAndIncrement();
                EmptyDisposable.error(new TestException(), observer);
            }
        });

        /**
         * Asserts the given number of subscriptions happened.
         * @param n the expected number of subscriptions
         */
        public void assertSubscriptions(int n) {
            Assert.assertEquals(n, get());
        }
    }

    /**
     * A normal Completable object.
     */
    final NormalCompletable normal = new NormalCompletable();

    /**
     * An error Completable object.
     */
    final ErrorCompletable error = new ErrorCompletable();

    @Test
    public void complete() {
        Completable c = Completable.complete();
        c.blockingAwait();
    }

    @Test
    public void concatEmpty() {
        Completable c = Completable.concatArray();
        c.blockingAwait();
    }

    @Test
    public void concatSingleSource() {
        Completable c = Completable.concatArray(normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test(expected = TestException.class)
    public void concatSingleSourceThrows() {
        Completable c = Completable.concatArray(error.completable);
        c.blockingAwait();
    }

    @Test
    public void concatMultipleSources() {
        Completable c = Completable.concatArray(normal.completable, normal.completable, normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void concatMultipleOneThrows() {
        Completable c = Completable.concatArray(normal.completable, error.completable, normal.completable);
        c.blockingAwait();
    }

    @Test(expected = NullPointerException.class)
    public void concatMultipleOneIsNull() {
        Completable c = Completable.concatArray(normal.completable, null);
        c.blockingAwait();
    }

    @Test
    public void concatIterableEmpty() {
        Completable c = Completable.concat(Collections.<Completable>emptyList());
        c.blockingAwait();
    }

    @Test(expected = NullPointerException.class)
    public void concatIterableIteratorNull() {
        Completable c = Completable.concat(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                return null;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void concatIterableSingle() {
        Completable c = Completable.concat(Collections.singleton(normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test
    public void concatIterableMany() {
        Completable c = Completable.concat(Arrays.asList(normal.completable, normal.completable, normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void concatIterableOneThrows() {
        Completable c = Completable.concat(Collections.singleton(error.completable));
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void concatIterableManyOneThrows() {
        Completable c = Completable.concat(Arrays.asList(normal.completable, error.completable));
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void concatIterableIterableThrows() {
        Completable c = Completable.concat(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void concatIterableIteratorHasNextThrows() {
        Completable c = Completable.concat(new IterableIteratorHasNextThrows());
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void concatIterableIteratorNextThrows() {
        Completable c = Completable.concat(new IterableIteratorNextThrows());
        c.blockingAwait();
    }

    @Test
    public void concatObservableEmpty() {
        Completable c = Completable.concat(Flowable.<Completable>empty());
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void concatObservableError() {
        Completable c = Completable.concat(Flowable.<Completable>error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new TestException();
            }
        }));
        c.blockingAwait();
    }

    @Test
    public void concatObservableSingle() {
        Completable c = Completable.concat(Flowable.just(normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test(expected = TestException.class)
    public void concatObservableSingleThrows() {
        Completable c = Completable.concat(Flowable.just(error.completable));
        c.blockingAwait();
    }

    @Test
    public void concatObservableMany() {
        Completable c = Completable.concat(Flowable.just(normal.completable).repeat(3));
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void concatObservableManyOneThrows() {
        Completable c = Completable.concat(Flowable.just(normal.completable, error.completable));
        c.blockingAwait();
    }

    @Test
    public void concatObservablePrefetch() {
        final List<Long> requested = new ArrayList<>();
        Flowable<Completable> cs = Flowable.just(normal.completable).repeat(10).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long v) {
                requested.add(v);
            }
        });
        Completable c = Completable.concat(cs, 5);
        c.blockingAwait();
        Assert.assertEquals(Arrays.asList(5L, 4L, 4L), requested);
    }

    @Test(expected = NullPointerException.class)
    public void createOnSubscribeThrowsNPE() {
        Completable c = Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                throw new NullPointerException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void createOnSubscribeThrowsRuntimeException() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Completable c = Completable.unsafeCreate(new CompletableSource() {

                @Override
                public void subscribe(CompletableObserver observer) {
                    throw new TestException();
                }
            });
            c.blockingAwait();
            Assert.fail("Did not throw exception");
        } catch (NullPointerException ex) {
            if (!(ex.getCause() instanceof TestException)) {
                ex.printStackTrace();
                Assert.fail("Did not wrap the TestException but it returned: " + ex);
            }
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void defer() {
        Completable c = Completable.defer(new Supplier<Completable>() {

            @Override
            public Completable get() {
                return normal.completable;
            }
        });
        normal.assertSubscriptions(0);
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test(expected = NullPointerException.class)
    public void deferReturnsNull() {
        Completable c = Completable.defer(new Supplier<Completable>() {

            @Override
            public Completable get() {
                return null;
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void deferFunctionThrows() {
        Completable c = Completable.defer(new Supplier<Completable>() {

            @Override
            public Completable get() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void deferErrorSource() {
        Completable c = Completable.defer(new Supplier<Completable>() {

            @Override
            public Completable get() {
                return error.completable;
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void errorSupplierNormal() {
        Completable c = Completable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test(expected = NullPointerException.class)
    public void errorSupplierReturnsNull() {
        Completable c = Completable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return null;
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void errorSupplierThrows() {
        Completable c = Completable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void errorNormal() {
        Completable c = Completable.error(new TestException());
        c.blockingAwait();
    }

    @Test
    public void fromCallableNormal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return calls.getAndIncrement();
            }
        });
        c.blockingAwait();
        Assert.assertEquals(1, calls.get());
    }

    @Test(expected = TestException.class)
    public void fromCallableThrows() {
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void fromFlowableEmpty() {
        Completable c = Completable.fromPublisher(Flowable.empty());
        c.blockingAwait();
    }

    @Test
    public void fromFlowableSome() {
        for (int n = 1; n < 10000; n *= 10) {
            Completable c = Completable.fromPublisher(Flowable.range(1, n));
            c.blockingAwait();
        }
    }

    @Test(expected = TestException.class)
    public void fromFlowableError() {
        Completable c = Completable.fromPublisher(Flowable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new TestException();
            }
        }));
        c.blockingAwait();
    }

    @Test
    public void fromObservableEmpty() {
        Completable c = Completable.fromObservable(Observable.empty());
        c.blockingAwait();
    }

    @Test
    public void fromObservableSome() {
        for (int n = 1; n < 10000; n *= 10) {
            Completable c = Completable.fromObservable(Observable.range(1, n));
            c.blockingAwait();
        }
    }

    @Test(expected = TestException.class)
    public void fromObservableError() {
        Completable c = Completable.fromObservable(Observable.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new TestException();
            }
        }));
        c.blockingAwait();
    }

    @Test
    public void fromActionNormal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromAction(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        c.blockingAwait();
        Assert.assertEquals(1, calls.get());
    }

    @Test(expected = TestException.class)
    public void fromActionThrows() {
        Completable c = Completable.fromAction(new Action() {

            @Override
            public void run() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void fromSingleNormal() {
        Completable c = Completable.fromSingle(Single.just(1));
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void fromSingleThrows() {
        Completable c = Completable.fromSingle(Single.error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new TestException();
            }
        }));
        c.blockingAwait();
    }

    @Test
    public void mergeEmpty() {
        Completable c = Completable.mergeArray();
        c.blockingAwait();
    }

    @Test
    public void mergeSingleSource() {
        Completable c = Completable.mergeArray(normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test(expected = TestException.class)
    public void mergeSingleSourceThrows() {
        Completable c = Completable.mergeArray(error.completable);
        c.blockingAwait();
    }

    @Test
    public void mergeMultipleSources() {
        Completable c = Completable.mergeArray(normal.completable, normal.completable, normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void mergeMultipleOneThrows() {
        Completable c = Completable.mergeArray(normal.completable, error.completable, normal.completable);
        c.blockingAwait();
    }

    @Test(expected = NullPointerException.class)
    public void mergeMultipleOneIsNull() {
        Completable c = Completable.mergeArray(normal.completable, null);
        c.blockingAwait();
    }

    @Test
    public void mergeIterableEmpty() {
        Completable c = Completable.merge(Collections.<Completable>emptyList());
        c.blockingAwait();
    }

    @Test(expected = NullPointerException.class)
    public void mergeIterableIteratorNull() {
        Completable c = Completable.merge(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                return null;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void mergeIterableSingle() {
        Completable c = Completable.merge(Collections.singleton(normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test
    public void mergeIterableMany() {
        Completable c = Completable.merge(Arrays.asList(normal.completable, normal.completable, normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void mergeIterableOneThrows() {
        Completable c = Completable.merge(Collections.singleton(error.completable));
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeIterableManyOneThrows() {
        Completable c = Completable.merge(Arrays.asList(normal.completable, error.completable));
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeIterableIterableThrows() {
        Completable c = Completable.merge(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeIterableIteratorHasNextThrows() {
        Completable c = Completable.merge(new IterableIteratorHasNextThrows());
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeIterableIteratorNextThrows() {
        Completable c = Completable.merge(new IterableIteratorNextThrows());
        c.blockingAwait();
    }

    @Test
    public void mergeObservableEmpty() {
        Completable c = Completable.merge(Flowable.<Completable>empty());
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeObservableError() {
        Completable c = Completable.merge(Flowable.<Completable>error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new TestException();
            }
        }));
        c.blockingAwait();
    }

    @Test
    public void mergeObservableSingle() {
        Completable c = Completable.merge(Flowable.just(normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test(expected = TestException.class)
    public void mergeObservableSingleThrows() {
        Completable c = Completable.merge(Flowable.just(error.completable));
        c.blockingAwait();
    }

    @Test
    public void mergeObservableMany() {
        Completable c = Completable.merge(Flowable.just(normal.completable).repeat(3));
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void mergeObservableManyOneThrows() {
        Completable c = Completable.merge(Flowable.just(normal.completable, error.completable));
        c.blockingAwait();
    }

    @Test
    public void mergeObservableMaxConcurrent() {
        final List<Long> requested = new ArrayList<>();
        Flowable<Completable> cs = Flowable.just(normal.completable).repeat(10).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long v) {
                requested.add(v);
            }
        });
        Completable c = Completable.merge(cs, 5);
        c.blockingAwait();
        // FIXME this request pattern looks odd because all 10 completions trigger 1 requests
        Assert.assertEquals(Arrays.asList(5L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), requested);
    }

    @Test
    public void mergeDelayErrorEmpty() {
        Completable c = Completable.mergeArrayDelayError();
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorSingleSource() {
        Completable c = Completable.mergeArrayDelayError(normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorSingleSourceThrows() {
        Completable c = Completable.mergeArrayDelayError(error.completable);
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorMultipleSources() {
        Completable c = Completable.mergeArrayDelayError(normal.completable, normal.completable, normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test
    public void mergeDelayErrorMultipleOneThrows() {
        Completable c = Completable.mergeArrayDelayError(normal.completable, error.completable, normal.completable);
        try {
            c.blockingAwait();
        } catch (TestException ex) {
            normal.assertSubscriptions(2);
        }
    }

    @Test(expected = NullPointerException.class)
    public void mergeDelayErrorMultipleOneIsNull() {
        Completable c = Completable.mergeArrayDelayError(normal.completable, null);
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorIterableEmpty() {
        Completable c = Completable.mergeDelayError(Collections.<Completable>emptyList());
        c.blockingAwait();
    }

    @Test(expected = NullPointerException.class)
    public void mergeDelayErrorIterableIteratorNull() {
        Completable c = Completable.mergeDelayError(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                return null;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorIterableSingle() {
        Completable c = Completable.mergeDelayError(Collections.singleton(normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test
    public void mergeDelayErrorIterableMany() {
        Completable c = Completable.mergeDelayError(Arrays.asList(normal.completable, normal.completable, normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorIterableOneThrows() {
        Completable c = Completable.mergeDelayError(Collections.singleton(error.completable));
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorIterableManyOneThrows() {
        Completable c = Completable.mergeDelayError(Arrays.asList(normal.completable, error.completable, normal.completable));
        try {
            c.blockingAwait();
        } catch (TestException ex) {
            normal.assertSubscriptions(2);
        }
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorIterableIterableThrows() {
        Completable c = Completable.mergeDelayError(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorIterableIteratorHasNextThrows() {
        Completable c = Completable.mergeDelayError(new IterableIteratorHasNextThrows());
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorIterableIteratorNextThrows() {
        Completable c = Completable.mergeDelayError(new IterableIteratorNextThrows());
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorObservableEmpty() {
        Completable c = Completable.mergeDelayError(Flowable.<Completable>empty());
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorObservableError() {
        Completable c = Completable.mergeDelayError(Flowable.<Completable>error(new Supplier<Throwable>() {

            @Override
            public Throwable get() {
                return new TestException();
            }
        }));
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorObservableSingle() {
        Completable c = Completable.mergeDelayError(Flowable.just(normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorObservableSingleThrows() {
        Completable c = Completable.mergeDelayError(Flowable.just(error.completable));
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorObservableMany() {
        Completable c = Completable.mergeDelayError(Flowable.just(normal.completable).repeat(3));
        c.blockingAwait();
        normal.assertSubscriptions(3);
    }

    @Test(expected = TestException.class)
    public void mergeDelayErrorObservableManyOneThrows() {
        Completable c = Completable.mergeDelayError(Flowable.just(normal.completable, error.completable));
        c.blockingAwait();
    }

    @Test
    public void mergeDelayErrorObservableMaxConcurrent() {
        final List<Long> requested = new ArrayList<>();
        Flowable<Completable> cs = Flowable.just(normal.completable).repeat(10).doOnRequest(new LongConsumer() {

            @Override
            public void accept(long v) {
                requested.add(v);
            }
        });
        Completable c = Completable.mergeDelayError(cs, 5);
        c.blockingAwait();
        // FIXME this request pattern looks odd because all 10 completions trigger 1 requests
        Assert.assertEquals(Arrays.asList(5L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), requested);
    }

    @Test
    public void never() {
        final AtomicBoolean onSubscribeCalled = new AtomicBoolean();
        final AtomicInteger calls = new AtomicInteger();
        Completable.never().subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
                onSubscribeCalled.set(true);
            }

            @Override
            public void onError(Throwable e) {
                calls.getAndIncrement();
            }

            @Override
            public void onComplete() {
                calls.getAndIncrement();
            }
        });
        Assert.assertTrue("onSubscribe not called", onSubscribeCalled.get());
        Assert.assertEquals("There were calls to onXXX methods", 0, calls.get());
    }

    @Test
    public void timer() {
        Completable c = Completable.timer(500, TimeUnit.MILLISECONDS);
        c.blockingAwait();
    }

    @Test
    public void timerNewThread() {
        Completable c = Completable.timer(500, TimeUnit.MILLISECONDS, Schedulers.newThread());
        c.blockingAwait();
    }

    @Test
    public void timerTestScheduler() {
        TestScheduler scheduler = new TestScheduler();
        Completable c = Completable.timer(250, TimeUnit.MILLISECONDS, scheduler);
        final AtomicInteger calls = new AtomicInteger();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                calls.getAndIncrement();
            }

            @Override
            public void onError(Throwable e) {
                RxJavaPlugins.onError(e);
            }
        });
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, calls.get());
        scheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, calls.get());
    }

    @Test
    public void timerCancel() throws InterruptedException {
        Completable c = Completable.timer(250, TimeUnit.MILLISECONDS);
        final SequentialDisposable sd = new SequentialDisposable();
        final AtomicInteger calls = new AtomicInteger();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
                sd.replace(d);
            }

            @Override
            public void onError(Throwable e) {
                calls.getAndIncrement();
            }

            @Override
            public void onComplete() {
                calls.getAndIncrement();
            }
        });
        Thread.sleep(100);
        sd.dispose();
        Thread.sleep(200);
        Assert.assertEquals(0, calls.get());
    }

    @Test
    public void usingNormalEager() {
        final AtomicInteger dispose = new AtomicInteger();
        Completable c = Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Object, Completable>() {

            @Override
            public Completable apply(Object v) {
                return normal.completable;
            }
        }, new Consumer<Integer>() {

            @Override
            public void accept(Integer d) {
                dispose.set(d);
            }
        });
        final AtomicBoolean disposedFirst = new AtomicBoolean();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                error.lazySet(e);
            }

            @Override
            public void onComplete() {
                disposedFirst.set(dispose.get() != 0);
            }
        });
        Assert.assertEquals(1, dispose.get());
        Assert.assertTrue("Not disposed first", disposedFirst.get());
        Assert.assertNull(error.get());
    }

    @Test
    public void usingNormalLazy() {
        final AtomicInteger dispose = new AtomicInteger();
        Completable c = Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer v) {
                return normal.completable;
            }
        }, new Consumer<Integer>() {

            @Override
            public void accept(Integer d) {
                dispose.set(d);
            }
        }, false);
        final AtomicBoolean disposedFirst = new AtomicBoolean();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                error.lazySet(e);
            }

            @Override
            public void onComplete() {
                disposedFirst.set(dispose.get() != 0);
            }
        });
        Assert.assertEquals(1, dispose.get());
        Assert.assertFalse("Disposed first", disposedFirst.get());
        Assert.assertNull(error.get());
    }

    @Test
    public void usingErrorEager() {
        final AtomicInteger dispose = new AtomicInteger();
        Completable c = Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer v) {
                return error.completable;
            }
        }, new Consumer<Integer>() {

            @Override
            public void accept(Integer d) {
                dispose.set(d);
            }
        });
        final AtomicBoolean disposedFirst = new AtomicBoolean();
        final AtomicBoolean complete = new AtomicBoolean();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                disposedFirst.set(dispose.get() != 0);
            }

            @Override
            public void onComplete() {
                complete.set(true);
            }
        });
        Assert.assertEquals(1, dispose.get());
        Assert.assertTrue("Not disposed first", disposedFirst.get());
        Assert.assertFalse(complete.get());
    }

    @Test
    public void usingErrorLazy() {
        final AtomicInteger dispose = new AtomicInteger();
        Completable c = Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer v) {
                return error.completable;
            }
        }, new Consumer<Integer>() {

            @Override
            public void accept(Integer d) {
                dispose.set(d);
            }
        }, false);
        final AtomicBoolean disposedFirst = new AtomicBoolean();
        final AtomicBoolean complete = new AtomicBoolean();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                disposedFirst.set(dispose.get() != 0);
            }

            @Override
            public void onComplete() {
                complete.set(true);
            }
        });
        Assert.assertEquals(1, dispose.get());
        Assert.assertFalse("Disposed first", disposedFirst.get());
        Assert.assertFalse(complete.get());
    }

    @Test(expected = NullPointerException.class)
    public void usingMapperReturnsNull() {
        Completable c = Completable.using(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, new Function<Object, Completable>() {

            @Override
            public Completable apply(Object v) {
                return null;
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object v) {
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void usingResourceThrows() {
        Completable c = Completable.using(new Supplier<Object>() {

            @Override
            public Object get() {
                throw new TestException();
            }
        }, new Function<Object, Completable>() {

            @Override
            public Completable apply(Object v) {
                return normal.completable;
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object v) {
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void usingMapperThrows() {
        Completable c = Completable.using(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, new Function<Object, Completable>() {

            @Override
            public Completable apply(Object v) {
                throw new TestException();
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object v) {
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void usingDisposerThrows() {
        Completable c = Completable.using(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }, new Function<Object, Completable>() {

            @Override
            public Completable apply(Object v) {
                return normal.completable;
            }
        }, new Consumer<Object>() {

            @Override
            public void accept(Object v) {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void composeNormal() {
        Completable c = error.completable.compose(new CompletableTransformer() {

            @Override
            public Completable apply(Completable n) {
                return n.onErrorComplete();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void concatWithNormal() {
        Completable c = normal.completable.concatWith(normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(2);
    }

    @Test(expected = TestException.class)
    public void concatWithError() {
        Completable c = normal.completable.concatWith(error.completable);
        c.blockingAwait();
    }

    @Test
    public void delayNormal() throws InterruptedException {
        Completable c = normal.completable.delay(250, TimeUnit.MILLISECONDS);
        final AtomicBoolean done = new AtomicBoolean();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                error.set(e);
            }

            @Override
            public void onComplete() {
                done.set(true);
            }
        });
        Thread.sleep(100);
        Assert.assertFalse("Already done", done.get());
        int timeout = 10;
        while (timeout-- > 0 && !done.get()) {
            Thread.sleep(100);
        }
        Assert.assertTrue("Not done", done.get());
        Assert.assertNull(error.get());
    }

    @Test
    public void delayErrorImmediately() throws InterruptedException {
        final TestScheduler scheduler = new TestScheduler();
        final Completable c = error.completable.delay(250, TimeUnit.MILLISECONDS, scheduler);
        final AtomicBoolean done = new AtomicBoolean();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                error.set(e);
            }

            @Override
            public void onComplete() {
                done.set(true);
            }
        });
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        Assert.assertTrue(error.get().toString(), error.get() instanceof TestException);
        Assert.assertFalse("Already done", done.get());
        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        Assert.assertFalse("Already done", done.get());
    }

    @Test
    public void delayErrorToo() throws InterruptedException {
        Completable c = error.completable.delay(250, TimeUnit.MILLISECONDS, Schedulers.computation(), true);
        final AtomicBoolean done = new AtomicBoolean();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
                error.set(e);
            }

            @Override
            public void onComplete() {
                done.set(true);
            }
        });
        Thread.sleep(100);
        Assert.assertFalse("Already done", done.get());
        Assert.assertNull(error.get());
        Thread.sleep(200);
        Assert.assertFalse("Already done", done.get());
        Assert.assertTrue(error.get() instanceof TestException);
    }

    @Test
    public void doOnCompleteNormal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = normal.completable.doOnComplete(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        c.blockingAwait();
        Assert.assertEquals(1, calls.get());
    }

    @Test
    public void doOnCompleteError() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = error.completable.doOnComplete(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        try {
            c.blockingAwait();
            Assert.fail("Failed to throw TestException");
        } catch (TestException ex) {
        // expected
        }
        Assert.assertEquals(0, calls.get());
    }

    @Test(expected = TestException.class)
    public void doOnCompleteThrows() {
        Completable c = normal.completable.doOnComplete(new Action() {

            @Override
            public void run() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void doOnDisposeNormalDoesntCall() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = normal.completable.doOnDispose(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        c.blockingAwait();
        Assert.assertEquals(0, calls.get());
    }

    @Test
    public void doOnDisposeErrorDoesntCall() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = error.completable.doOnDispose(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        try {
            c.blockingAwait();
            Assert.fail("No exception thrown");
        } catch (TestException ex) {
        // expected
        }
        Assert.assertEquals(0, calls.get());
    }

    @Test
    public void doOnDisposeChildCancels() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = normal.completable.doOnDispose(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
                d.dispose();
            }

            @Override
            public void onError(Throwable e) {
            // ignored
            }

            @Override
            public void onComplete() {
            // ignored
            }
        });
        Assert.assertEquals(1, calls.get());
    }

    @Test
    public void doOnDisposeThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Completable c = normal.completable.doOnDispose(new Action() {

                @Override
                public void run() {
                    throw new TestException();
                }
            });
            c.subscribe(new CompletableObserver() {

                @Override
                public void onSubscribe(Disposable d) {
                    d.dispose();
                }

                @Override
                public void onError(Throwable e) {
                // ignored
                }

                @Override
                public void onComplete() {
                // ignored
                }
            });
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doOnErrorNoError() {
        final AtomicReference<Throwable> error = new AtomicReference<>();
        Completable c = normal.completable.doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
                error.set(e);
            }
        });
        c.blockingAwait();
        Assert.assertNull(error.get());
    }

    @Test
    public void doOnErrorHasError() {
        final AtomicReference<Throwable> err = new AtomicReference<>();
        Completable c = error.completable.doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
                err.set(e);
            }
        });
        try {
            c.blockingAwait();
            Assert.fail("Did not throw exception");
        } catch (Throwable e) {
        // expected
        }
        Assert.assertTrue(err.get() instanceof TestException);
    }

    @Test
    public void doOnErrorThrows() {
        Completable c = error.completable.doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
                throw new IllegalStateException();
            }
        });
        try {
            c.blockingAwait();
        } catch (CompositeException ex) {
            List<Throwable> a = ex.getExceptions();
            Assert.assertEquals(2, a.size());
            Assert.assertTrue(a.get(0) instanceof TestException);
            Assert.assertTrue(a.get(1) instanceof IllegalStateException);
        }
    }

    @Test
    public void doOnSubscribeNormal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = normal.completable.doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                calls.getAndIncrement();
            }
        });
        for (int i = 0; i < 10; i++) {
            c.blockingAwait();
        }
        Assert.assertEquals(10, calls.get());
    }

    @Test(expected = TestException.class)
    public void doOnSubscribeThrows() {
        Completable c = normal.completable.doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void doOnTerminateNormal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = normal.completable.doOnTerminate(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        c.blockingAwait();
        Assert.assertEquals(1, calls.get());
    }

    @Test
    public void doOnTerminateError() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = error.completable.doOnTerminate(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        try {
            c.blockingAwait();
            Assert.fail("Did dot throw exception");
        } catch (TestException ex) {
        // expected
        }
        Assert.assertEquals(1, calls.get());
    }

    @Test(expected = NullPointerException.class)
    public void liftReturnsNull() {
        Completable c = normal.completable.lift(new CompletableOperator() {

            @Override
            public CompletableObserver apply(CompletableObserver v) {
                return null;
            }
        });
        c.blockingAwait();
    }

    static final class CompletableOperatorSwap implements CompletableOperator {

        @Override
        public CompletableObserver apply(final CompletableObserver v) {
            return new CompletableObserver() {

                @Override
                public void onComplete() {
                    v.onError(new TestException());
                }

                @Override
                public void onError(Throwable e) {
                    v.onComplete();
                }

                @Override
                public void onSubscribe(Disposable d) {
                    v.onSubscribe(d);
                }
            };
        }
    }

    @Test(expected = TestException.class)
    public void liftOnCompleteError() {
        Completable c = normal.completable.lift(new CompletableOperatorSwap());
        c.blockingAwait();
    }

    @Test
    public void liftOnErrorComplete() {
        Completable c = error.completable.lift(new CompletableOperatorSwap());
        c.blockingAwait();
    }

    @Test
    public void mergeWithNormal() {
        Completable c = normal.completable.mergeWith(normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(2);
    }

    @Test
    public void observeOnNormal() throws InterruptedException {
        final AtomicReference<String> name = new AtomicReference<>();
        final AtomicReference<Throwable> err = new AtomicReference<>();
        final CountDownLatch cdl = new CountDownLatch(1);
        Completable c = normal.completable.observeOn(Schedulers.computation());
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                name.set(Thread.currentThread().getName());
                cdl.countDown();
            }

            @Override
            public void onError(Throwable e) {
                err.set(e);
                cdl.countDown();
            }
        });
        cdl.await();
        Assert.assertNull(err.get());
        Assert.assertTrue(name.get().startsWith("RxComputation"));
    }

    @Test
    public void observeOnError() throws InterruptedException {
        final AtomicReference<String> name = new AtomicReference<>();
        final AtomicReference<Throwable> err = new AtomicReference<>();
        final CountDownLatch cdl = new CountDownLatch(1);
        Completable c = error.completable.observeOn(Schedulers.computation());
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                name.set(Thread.currentThread().getName());
                cdl.countDown();
            }

            @Override
            public void onError(Throwable e) {
                name.set(Thread.currentThread().getName());
                err.set(e);
                cdl.countDown();
            }
        });
        cdl.await();
        Assert.assertTrue(err.get() instanceof TestException);
        Assert.assertTrue(name.get().startsWith("RxComputation"));
    }

    @Test
    public void onErrorComplete() {
        Completable c = error.completable.onErrorComplete();
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void onErrorCompleteFalse() {
        Completable c = error.completable.onErrorComplete(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable e) {
                return e instanceof IllegalStateException;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void onErrorResumeNextFunctionReturnsNull() {
        Completable c = error.completable.onErrorResumeNext(new Function<Throwable, Completable>() {

            @Override
            public Completable apply(Throwable e) {
                return null;
            }
        });
        try {
            c.blockingAwait();
            Assert.fail("Did not throw an exception");
        } catch (CompositeException ex) {
            List<Throwable> errors = ex.getExceptions();
            TestHelper.assertError(errors, 0, TestException.class);
            TestHelper.assertError(errors, 1, NullPointerException.class);
            assertEquals(2, errors.size());
        }
    }

    @Test
    public void onErrorResumeNextFunctionThrows() {
        Completable c = error.completable.onErrorResumeNext(new Function<Throwable, Completable>() {

            @Override
            public Completable apply(Throwable e) {
                throw new TestException();
            }
        });
        try {
            c.blockingAwait();
            Assert.fail("Did not throw an exception");
        } catch (CompositeException ex) {
            List<Throwable> a = ex.getExceptions();
            Assert.assertEquals(2, a.size());
            Assert.assertTrue(a.get(0) instanceof TestException);
            Assert.assertTrue(a.get(1) instanceof TestException);
        }
    }

    @Test
    public void onErrorResumeNextNormal() {
        Completable c = error.completable.onErrorResumeNext(new Function<Throwable, Completable>() {

            @Override
            public Completable apply(Throwable v) {
                return normal.completable;
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void onErrorResumeNextError() {
        Completable c = error.completable.onErrorResumeNext(new Function<Throwable, Completable>() {

            @Override
            public Completable apply(Throwable v) {
                return error.completable;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void repeatNormal() {
        final AtomicReference<Throwable> err = new AtomicReference<>();
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                calls.getAndIncrement();
                Thread.sleep(100);
                return null;
            }
        }).repeat();
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(final Disposable d) {
                Schedulers.single().scheduleDirect(new Runnable() {

                    @Override
                    public void run() {
                        d.dispose();
                    }
                }, 550, TimeUnit.MILLISECONDS);
            }

            @Override
            public void onError(Throwable e) {
                err.set(e);
            }

            @Override
            public void onComplete() {
            }
        });
        Assert.assertEquals(6, calls.get());
        Assert.assertNull(err.get());
    }

    @Test(expected = TestException.class)
    public void repeatError() {
        Completable c = error.completable.repeat();
        c.blockingAwait();
    }

    @Test
    public void repeat5Times() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                calls.getAndIncrement();
                return null;
            }
        }).repeat(5);
        c.blockingAwait();
        Assert.assertEquals(5, calls.get());
    }

    @Test
    public void repeat1Time() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                calls.getAndIncrement();
                return null;
            }
        }).repeat(1);
        c.blockingAwait();
        Assert.assertEquals(1, calls.get());
    }

    @Test
    public void repeat0Time() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                calls.getAndIncrement();
                return null;
            }
        }).repeat(0);
        c.blockingAwait();
        Assert.assertEquals(0, calls.get());
    }

    @Test
    public void repeatUntilNormal() {
        final AtomicInteger calls = new AtomicInteger();
        final AtomicInteger times = new AtomicInteger(5);
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                calls.getAndIncrement();
                return null;
            }
        }).repeatUntil(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() {
                return times.decrementAndGet() == 0;
            }
        });
        c.blockingAwait();
        Assert.assertEquals(5, calls.get());
    }

    @Test
    public void retryNormal() {
        Completable c = normal.completable.retry();
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test
    public void retry5Times() {
        final AtomicInteger calls = new AtomicInteger(5);
        Completable c = Completable.fromAction(new Action() {

            @Override
            public void run() {
                if (calls.decrementAndGet() != 0) {
                    throw new TestException();
                }
            }
        }).retry();
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void retryBiPredicate5Times() {
        Completable c = error.completable.retry(new BiPredicate<Integer, Throwable>() {

            @Override
            public boolean test(Integer n, Throwable e) {
                return n < 5;
            }
        });
        c.blockingAwait();
    }

    @Test(expected = TestException.class)
    public void retryTimes5Error() {
        Completable c = error.completable.retry(5);
        c.blockingAwait();
    }

    @Test
    public void retryTimes5Normal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromAction(new Action() {

            @Override
            public void run() {
                if (calls.incrementAndGet() != 6) {
                    throw new TestException();
                }
            }
        }).retry(5);
        c.blockingAwait();
        assertEquals(6, calls.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void retryNegativeTimes() {
        normal.completable.retry(-1);
    }

    @Test(expected = TestException.class)
    public void retryPredicateError() {
        Completable c = error.completable.retry(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable e) {
                return false;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void retryPredicate5Times() {
        final AtomicInteger calls = new AtomicInteger(5);
        Completable c = Completable.fromAction(new Action() {

            @Override
            public void run() {
                if (calls.decrementAndGet() != 0) {
                    throw new TestException();
                }
            }
        }).retry(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable e) {
                return true;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void retryWhen5Times() {
        final AtomicInteger calls = new AtomicInteger(5);
        Completable c = Completable.fromAction(new Action() {

            @Override
            public void run() {
                if (calls.decrementAndGet() != 0) {
                    throw new TestException();
                }
            }
        }).retryWhen(new Function<Flowable<? extends Throwable>, Publisher<Object>>() {

            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public Publisher<Object> apply(Flowable<? extends Throwable> f) {
                return (Publisher) f;
            }
        });
        c.blockingAwait();
    }

    @Test
    public void subscribe() throws InterruptedException {
        final AtomicBoolean complete = new AtomicBoolean();
        Completable c = normal.completable.delay(100, TimeUnit.MILLISECONDS).doOnComplete(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        });
        Disposable d = c.subscribe();
        assertFalse(d.isDisposed());
        Thread.sleep(150);
        Assert.assertTrue("Not completed", complete.get());
        assertTrue(d.isDisposed());
    }

    @Test
    public void subscribeDispose() throws InterruptedException {
        final AtomicBoolean complete = new AtomicBoolean();
        Completable c = normal.completable.delay(200, TimeUnit.MILLISECONDS).doOnComplete(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        });
        Disposable d = c.subscribe();
        Thread.sleep(100);
        d.dispose();
        Thread.sleep(150);
        Assert.assertFalse("Completed", complete.get());
    }

    @Test
    public void subscribeTwoCallbacksNormal() {
        final AtomicReference<Throwable> err = new AtomicReference<>();
        final AtomicBoolean complete = new AtomicBoolean();
        normal.completable.subscribe(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
                err.set(e);
            }
        });
        Assert.assertNull(err.get());
        Assert.assertTrue("Not completed", complete.get());
    }

    @Test
    public void subscribeTwoCallbacksError() {
        final AtomicReference<Throwable> err = new AtomicReference<>();
        final AtomicBoolean complete = new AtomicBoolean();
        error.completable.subscribe(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
                err.set(e);
            }
        });
        Assert.assertTrue(err.get() instanceof TestException);
        Assert.assertFalse("Not completed", complete.get());
    }

    @Test
    public void subscribeTwoCallbacksCompleteThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final AtomicReference<Throwable> err = new AtomicReference<>();
            normal.completable.subscribe(new Action() {

                @Override
                public void run() {
                    throw new TestException();
                }
            }, new Consumer<Throwable>() {

                @Override
                public void accept(Throwable e) {
                    err.set(e);
                }
            });
            Assert.assertNull(err.get());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void subscribeTwoCallbacksOnErrorThrows() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            error.completable.subscribe(new Action() {

                @Override
                public void run() {
                }
            }, new Consumer<Throwable>() {

                @Override
                public void accept(Throwable e) {
                    throw new TestException();
                }
            });
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void subscribeObserverNormal() {
        TestObserver<Object> to = new TestObserver<>();
        normal.completable.toObservable().subscribe(to);
        to.assertComplete();
        to.assertNoValues();
        to.assertNoErrors();
    }

    @Test
    public void subscribeObserverError() {
        TestObserver<Object> to = new TestObserver<>();
        error.completable.toObservable().subscribe(to);
        to.assertNotComplete();
        to.assertNoValues();
        to.assertError(TestException.class);
    }

    @Test
    public void subscribeActionNormal() {
        final AtomicBoolean run = new AtomicBoolean();
        normal.completable.subscribe(new Action() {

            @Override
            public void run() {
                run.set(true);
            }
        });
        Assert.assertTrue("Not completed", run.get());
    }

    @Test
    public void subscribeActionError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final AtomicBoolean run = new AtomicBoolean();
            error.completable.subscribe(new Action() {

                @Override
                public void run() {
                    run.set(true);
                }
            });
            Assert.assertFalse("Completed", run.get());
            TestHelper.assertError(errors, 0, OnErrorNotImplementedException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void subscribeSubscriberNormal() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        normal.completable.toFlowable().subscribe(ts);
        ts.assertComplete();
        ts.assertNoValues();
        ts.assertNoErrors();
    }

    @Test
    public void subscribeSubscriberError() {
        TestSubscriber<Object> ts = new TestSubscriber<>();
        error.completable.toFlowable().subscribe(ts);
        ts.assertNotComplete();
        ts.assertNoValues();
        ts.assertError(TestException.class);
    }

    @Test
    public void subscribeOnNormal() {
        final AtomicReference<String> name = new AtomicReference<>();
        Completable c = Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                name.set(Thread.currentThread().getName());
                EmptyDisposable.complete(observer);
            }
        }).subscribeOn(Schedulers.computation());
        c.blockingAwait();
        Assert.assertTrue(name.get().startsWith("RxComputation"));
    }

    @Test
    public void subscribeOnError() {
        final AtomicReference<String> name = new AtomicReference<>();
        Completable c = Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                name.set(Thread.currentThread().getName());
                EmptyDisposable.error(new TestException(), observer);
            }
        }).subscribeOn(Schedulers.computation());
        try {
            c.blockingAwait();
            Assert.fail("No exception thrown");
        } catch (TestException ex) {
        // expected
        }
        Assert.assertTrue(name.get().startsWith("RxComputation"));
    }

    @Test
    public void timeoutSwitchNormal() {
        Completable c = Completable.never().timeout(100, TimeUnit.MILLISECONDS, normal.completable);
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test
    public void timeoutTimerCancelled() throws InterruptedException {
        Completable c = Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                Thread.sleep(50);
                return null;
            }
        }).timeout(100, TimeUnit.MILLISECONDS, normal.completable);
        c.blockingAwait();
        Thread.sleep(100);
        normal.assertSubscriptions(0);
    }

    @Test
    public void toNormal() {
        normal.completable.to(new CompletableConverter<Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Completable c) {
                return c.toFlowable();
            }
        }).test().assertComplete().assertNoValues();
    }

    @Test
    public void asNormal() {
        normal.completable.to(new CompletableConverter<Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Completable c) {
                return c.toFlowable();
            }
        }).test().assertComplete().assertNoValues();
    }

    @Test
    public void as() {
        Completable.complete().to(new CompletableConverter<Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Completable v) {
                return v.toFlowable();
            }
        }).test().assertComplete();
    }

    @Test
    public void toFlowableNormal() {
        normal.completable.toFlowable().blockingForEach(Functions.emptyConsumer());
    }

    @Test(expected = TestException.class)
    public void toFlowableError() {
        error.completable.toFlowable().blockingForEach(Functions.emptyConsumer());
    }

    @Test
    public void toObservableNormal() {
        normal.completable.toObservable().blockingForEach(Functions.emptyConsumer());
    }

    @Test(expected = TestException.class)
    public void toObservableError() {
        error.completable.toObservable().blockingForEach(Functions.emptyConsumer());
    }

    @Test
    public void toSingleSupplierNormal() {
        Assert.assertEquals(1, normal.completable.toSingle(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }).blockingGet());
    }

    @Test(expected = TestException.class)
    public void toSingleSupplierError() {
        error.completable.toSingle(new Supplier<Object>() {

            @Override
            public Object get() {
                return 1;
            }
        }).blockingGet();
    }

    @Test(expected = NullPointerException.class)
    public void toSingleSupplierReturnsNull() {
        normal.completable.toSingle(new Supplier<Object>() {

            @Override
            public Object get() {
                return null;
            }
        }).blockingGet();
    }

    @Test(expected = TestException.class)
    public void toSingleSupplierThrows() {
        normal.completable.toSingle(new Supplier<Object>() {

            @Override
            public Object get() {
                throw new TestException();
            }
        }).blockingGet();
    }

    @Test(expected = TestException.class)
    public void toSingleDefaultError() {
        error.completable.toSingleDefault(1).blockingGet();
    }

    @Test
    public void toSingleDefaultNormal() {
        Assert.assertEquals((Integer) 1, normal.completable.toSingleDefault(1).blockingGet());
    }

    @Test
    public void unsubscribeOnNormal() throws InterruptedException {
        final AtomicReference<String> name = new AtomicReference<>();
        final CountDownLatch cdl = new CountDownLatch(1);
        normal.completable.delay(1, TimeUnit.SECONDS).doOnDispose(new Action() {

            @Override
            public void run() {
                name.set(Thread.currentThread().getName());
                cdl.countDown();
            }
        }).unsubscribeOn(Schedulers.computation()).subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(final Disposable d) {
                Schedulers.single().scheduleDirect(new Runnable() {

                    @Override
                    public void run() {
                        d.dispose();
                    }
                }, 100, TimeUnit.MILLISECONDS);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
        cdl.await();
        Assert.assertTrue(name.get().startsWith("RxComputation"));
    }

    @Test
    public void ambArrayEmpty() {
        Completable c = Completable.ambArray();
        c.blockingAwait();
    }

    @Test
    public void ambArraySingleNormal() {
        Completable c = Completable.ambArray(normal.completable);
        c.blockingAwait();
    }

    @Test
    public void ambArraySingleError() {
        Completable.ambArray(error.completable).test().assertError(TestException.class);
    }

    @Test
    public void ambArrayOneFires() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = Completable.ambArray(c1, c2);
        final AtomicBoolean complete = new AtomicBoolean();
        c.subscribe(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp1.onComplete();
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get());
    }

    @Test
    public void ambArrayOneFiresError() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = Completable.ambArray(c1, c2);
        final AtomicReference<Throwable> complete = new AtomicReference<>();
        c.subscribe(Functions.EMPTY_ACTION, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable v) {
                complete.set(v);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp1.onError(new TestException());
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get() instanceof TestException);
    }

    @Test
    public void ambArraySecondFires() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = Completable.ambArray(c1, c2);
        final AtomicBoolean complete = new AtomicBoolean();
        c.subscribe(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp2.onComplete();
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get());
    }

    @Test
    public void ambArraySecondFiresError() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = Completable.ambArray(c1, c2);
        final AtomicReference<Throwable> complete = new AtomicReference<>();
        c.subscribe(Functions.EMPTY_ACTION, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable v) {
                complete.set(v);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp2.onError(new TestException());
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get() instanceof TestException);
    }

    @Test
    public void ambMultipleOneIsNull() {
        Completable.ambArray(null, normal.completable).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambIterableEmpty() {
        Completable c = Completable.amb(Collections.<Completable>emptyList());
        c.blockingAwait();
    }

    @Test
    public void ambIterableIteratorNull() {
        Completable.amb(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                return null;
            }
        }).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambIterableWithNull() {
        Completable.amb(Arrays.asList(null, normal.completable)).test().assertError(NullPointerException.class);
    }

    @Test
    public void ambIterableSingle() {
        Completable c = Completable.amb(Collections.singleton(normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test
    public void ambIterableMany() {
        Completable c = Completable.amb(Arrays.asList(normal.completable, normal.completable, normal.completable));
        c.blockingAwait();
        normal.assertSubscriptions(1);
    }

    @Test
    public void ambIterableOneThrows() {
        Completable.amb(Collections.singleton(error.completable)).test().assertError(TestException.class);
    }

    @Test
    public void ambIterableManyOneThrows() {
        Completable.amb(Arrays.asList(error.completable, normal.completable)).test().assertError(TestException.class);
    }

    @Test
    public void ambIterableIterableThrows() {
        Completable.amb(new Iterable<Completable>() {

            @Override
            public Iterator<Completable> iterator() {
                throw new TestException();
            }
        }).test().assertError(TestException.class);
    }

    @Test
    public void ambIterableIteratorHasNextThrows() {
        Completable.amb(new IterableIteratorHasNextThrows()).test().assertError(TestException.class);
    }

    @Test
    public void ambIterableIteratorNextThrows() {
        Completable.amb(new IterableIteratorNextThrows()).test().assertError(TestException.class);
    }

    @Test
    public void ambWithArrayOneFires() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = c1.ambWith(c2);
        final AtomicBoolean complete = new AtomicBoolean();
        c.subscribe(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp1.onComplete();
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get());
    }

    @Test
    public void ambWithArrayOneFiresError() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = c1.ambWith(c2);
        final AtomicReference<Throwable> complete = new AtomicReference<>();
        c.subscribe(Functions.EMPTY_ACTION, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable v) {
                complete.set(v);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp1.onError(new TestException());
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get() instanceof TestException);
    }

    @Test
    public void ambWithArraySecondFires() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = c1.ambWith(c2);
        final AtomicBoolean complete = new AtomicBoolean();
        c.subscribe(new Action() {

            @Override
            public void run() {
                complete.set(true);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp2.onComplete();
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get());
    }

    @Test
    public void ambWithArraySecondFiresError() {
        PublishProcessor<Object> pp1 = PublishProcessor.create();
        PublishProcessor<Object> pp2 = PublishProcessor.create();
        Completable c1 = Completable.fromPublisher(pp1);
        Completable c2 = Completable.fromPublisher(pp2);
        Completable c = c1.ambWith(c2);
        final AtomicReference<Throwable> complete = new AtomicReference<>();
        c.subscribe(Functions.EMPTY_ACTION, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable v) {
                complete.set(v);
            }
        });
        Assert.assertTrue("First subject no subscribers", pp1.hasSubscribers());
        Assert.assertTrue("Second subject no subscribers", pp2.hasSubscribers());
        pp2.onError(new TestException());
        Assert.assertFalse("First subject has subscribers", pp1.hasSubscribers());
        Assert.assertFalse("Second subject has subscribers", pp2.hasSubscribers());
        Assert.assertTrue("Not completed", complete.get() instanceof TestException);
    }

    @Test
    public void startWithCompletableNormal() {
        final AtomicBoolean run = new AtomicBoolean();
        Completable c = normal.completable.startWith(Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                run.set(normal.get() == 0);
                return null;
            }
        }));
        c.blockingAwait();
        Assert.assertTrue("Did not start with other", run.get());
        normal.assertSubscriptions(1);
    }

    @Test
    public void startWithCompletableError() {
        Completable c = normal.completable.startWith(error.completable);
        try {
            c.blockingAwait();
            Assert.fail("Did not throw TestException");
        } catch (TestException ex) {
            normal.assertSubscriptions(0);
            error.assertSubscriptions(1);
        }
    }

    @Test
    public void startWithFlowableNormal() {
        final AtomicBoolean run = new AtomicBoolean();
        Flowable<Object> c = normal.completable.startWith(Flowable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                run.set(normal.get() == 0);
                return 1;
            }
        }));
        TestSubscriber<Object> ts = new TestSubscriber<>();
        c.subscribe(ts);
        Assert.assertTrue("Did not start with other", run.get());
        normal.assertSubscriptions(1);
        ts.assertValue(1);
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void startWithFlowableError() {
        Flowable<Object> c = normal.completable.startWith(Flowable.error(new TestException()));
        TestSubscriber<Object> ts = new TestSubscriber<>();
        c.subscribe(ts);
        normal.assertSubscriptions(0);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void startWithObservableNormal() {
        final AtomicBoolean run = new AtomicBoolean();
        Observable<Object> o = normal.completable.startWith(Observable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                run.set(normal.get() == 0);
                return 1;
            }
        }));
        TestObserver<Object> to = new TestObserver<>();
        o.subscribe(to);
        Assert.assertTrue("Did not start with other", run.get());
        normal.assertSubscriptions(1);
        to.assertValue(1);
        to.assertComplete();
        to.assertNoErrors();
    }

    @Test
    public void startWithObservableError() {
        Observable<Object> o = normal.completable.startWith(Observable.error(new TestException()));
        TestObserver<Object> to = new TestObserver<>();
        o.subscribe(to);
        normal.assertSubscriptions(0);
        to.assertNoValues();
        to.assertError(TestException.class);
        to.assertNotComplete();
    }

    @Test
    public void andThen() {
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        Completable.complete().andThen(Flowable.just("foo")).subscribe(ts);
        ts.request(1);
        ts.assertValue("foo");
        ts.assertComplete();
        ts.assertNoErrors();
        TestObserver<String> to = new TestObserver<>();
        Completable.complete().andThen(Observable.just("foo")).subscribe(to);
        to.assertValue("foo");
        to.assertComplete();
        to.assertNoErrors();
    }

    private static void expectUncaughtTestException(Action action) {
        Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();
        CapturingUncaughtExceptionHandler handler = new CapturingUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable error) throws Exception {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), error);
            }
        });
        try {
            action.run();
            assertEquals("Should have received exactly 1 exception", 1, handler.count);
            Throwable caught = handler.caught;
            while (caught != null) {
                if (caught instanceof TestException) {
                    break;
                }
                if (caught == caught.getCause()) {
                    break;
                }
                caught = caught.getCause();
            }
            assertTrue("A TestException should have been delivered to the handler", caught instanceof TestException);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(originalHandler);
            RxJavaPlugins.setErrorHandler(null);
        }
    }

    @Test
    public void subscribeOneActionThrowFromOnCompleted() {
        expectUncaughtTestException(new Action() {

            @Override
            public void run() {
                normal.completable.subscribe(new Action() {

                    @Override
                    public void run() {
                        throw new TestException();
                    }
                });
            }
        });
    }

    @Test
    public void subscribeTwoActionsThrowFromOnError() {
        expectUncaughtTestException(new Action() {

            @Override
            public void run() {
                error.completable.subscribe(new Action() {

                    @Override
                    public void run() {
                    }
                }, new Consumer<Throwable>() {

                    @Override
                    public void accept(Throwable throwable) {
                        throw new TestException();
                    }
                });
            }
        });
    }

    @Test
    public void propagateExceptionSubscribeOneAction() {
        expectUncaughtTestException(new Action() {

            @Override
            public void run() {
                error.completable.toSingleDefault(1).subscribe(new Consumer<Integer>() {

                    @Override
                    public void accept(Integer integer) {
                    }
                });
            }
        });
    }

    @Test
    public void usingFactoryReturnsNullAndDisposerThrows() {
        Consumer<Integer> onDispose = new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                throw new TestException();
            }
        };
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer t) {
                return null;
            }
        }, onDispose).<Integer>toFlowable().subscribe(ts);
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(CompositeException.class);
        CompositeException ex = (CompositeException) ts.errors().get(0);
        List<Throwable> listEx = ex.getExceptions();
        assertEquals(2, listEx.size());
        assertTrue(listEx.get(0).toString(), listEx.get(0) instanceof NullPointerException);
        assertTrue(listEx.get(1).toString(), listEx.get(1) instanceof TestException);
    }

    @Test
    public void subscribeReportsUnsubscribedOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            PublishSubject<String> stringSubject = PublishSubject.create();
            Completable completable = stringSubject.ignoreElements();
            Disposable completableSubscription = completable.subscribe();
            stringSubject.onError(new TestException());
            assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
            TestHelper.assertError(errors, 0, OnErrorNotImplementedException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void subscribeActionReportsUnsubscribed() {
        PublishSubject<String> stringSubject = PublishSubject.create();
        Completable completable = stringSubject.ignoreElements();
        Disposable completableSubscription = completable.subscribe(new Action() {

            @Override
            public void run() {
            }
        });
        stringSubject.onComplete();
        assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
    }

    @Test
    public void subscribeActionReportsUnsubscribedAfter() {
        PublishSubject<String> stringSubject = PublishSubject.create();
        Completable completable = stringSubject.ignoreElements();
        final AtomicReference<Disposable> disposableRef = new AtomicReference<>();
        Disposable completableSubscription = completable.subscribe(new Action() {

            @Override
            public void run() {
                if (disposableRef.get().isDisposed()) {
                    disposableRef.set(null);
                }
            }
        });
        disposableRef.set(completableSubscription);
        stringSubject.onComplete();
        assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
        assertNotNull("Unsubscribed before the call to onComplete", disposableRef.get());
    }

    @Test
    public void subscribeActionReportsUnsubscribedOnError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            PublishSubject<String> stringSubject = PublishSubject.create();
            Completable completable = stringSubject.ignoreElements();
            Disposable completableSubscription = completable.subscribe(new Action() {

                @Override
                public void run() {
                }
            });
            stringSubject.onError(new TestException());
            assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
            TestHelper.assertError(errors, 0, OnErrorNotImplementedException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void subscribeAction2ReportsUnsubscribed() {
        PublishSubject<String> stringSubject = PublishSubject.create();
        Completable completable = stringSubject.ignoreElements();
        Disposable completableSubscription = completable.subscribe(new Action() {

            @Override
            public void run() {
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable t) {
            }
        });
        stringSubject.onComplete();
        assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
    }

    @Test
    public void subscribeAction2ReportsUnsubscribedOnError() {
        PublishSubject<String> stringSubject = PublishSubject.create();
        Completable completable = stringSubject.ignoreElements();
        Disposable completableSubscription = completable.subscribe(new Action() {

            @Override
            public void run() {
            }
        }, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
            }
        });
        stringSubject.onError(new TestException());
        assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
    }

    @Test
    public void andThenSubscribeOn() {
        TestSubscriberEx<String> ts = new TestSubscriberEx<>(0);
        TestScheduler scheduler = new TestScheduler();
        Completable.complete().andThen(Flowable.just("foo").delay(1, TimeUnit.SECONDS, scheduler)).subscribe(ts);
        ts.request(1);
        ts.assertNoValues();
        ts.assertNotTerminated();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertValue("foo");
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void andThenSingleNever() {
        TestSubscriberEx<String> ts = new TestSubscriberEx<>(0);
        Completable.never().andThen(Single.just("foo")).toFlowable().subscribe(ts);
        ts.request(1);
        ts.assertNoValues();
        ts.assertNotTerminated();
    }

    @Test
    public void andThenSingleError() {
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        final AtomicBoolean hasRun = new AtomicBoolean(false);
        final Exception e = new Exception();
        Completable.error(e).andThen(new Single<String>() {

            @Override
            public void subscribeActual(SingleObserver<? super String> observer) {
                hasRun.set(true);
                observer.onSuccess("foo");
            }
        }).toFlowable().subscribe(ts);
        ts.assertNoValues();
        ts.assertError(e);
        Assert.assertFalse("Should not have subscribed to single when completable errors", hasRun.get());
    }

    @Test
    public void andThenSingleSubscribeOn() {
        TestSubscriberEx<String> ts = new TestSubscriberEx<>(0);
        TestScheduler scheduler = new TestScheduler();
        Completable.complete().andThen(Single.just("foo").delay(1, TimeUnit.SECONDS, scheduler)).toFlowable().subscribe(ts);
        ts.request(1);
        ts.assertNoValues();
        ts.assertNotTerminated();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        ts.assertValue("foo");
        ts.assertComplete();
        ts.assertNoErrors();
    }

    private Function<Completable, Completable> onCreate;

    private BiFunction<Completable, CompletableObserver, CompletableObserver> onStart;

    @Before
    public void setUp() throws Exception {
        onCreate = spy(new Function<Completable, Completable>() {

            @Override
            public Completable apply(Completable t) {
                return t;
            }
        });
        RxJavaPlugins.setOnCompletableAssembly(onCreate);
        onStart = spy(new BiFunction<Completable, CompletableObserver, CompletableObserver>() {

            @Override
            public CompletableObserver apply(Completable t1, CompletableObserver t2) {
                return t2;
            }
        });
        RxJavaPlugins.setOnCompletableSubscribe(onStart);
    }

    @After
    public void after() {
        RxJavaPlugins.reset();
    }

    @Test
    public void hookCreate() throws Throwable {
        CompletableSource subscriber = mock(CompletableSource.class);
        Completable create = Completable.unsafeCreate(subscriber);
        verify(onCreate, times(1)).apply(create);
    }

    @Test
    public void doOnCompletedNormal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = normal.completable.doOnComplete(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        c.blockingAwait();
        Assert.assertEquals(1, calls.get());
    }

    @Test
    public void doOnCompletedError() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = error.completable.doOnComplete(new Action() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        try {
            c.blockingAwait();
            Assert.fail("Failed to throw TestException");
        } catch (TestException ex) {
        // expected
        }
        Assert.assertEquals(0, calls.get());
    }

    @Test(expected = TestException.class)
    public void doOnCompletedThrows() {
        Completable c = normal.completable.doOnComplete(new Action() {

            @Override
            public void run() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void doAfterTerminateNormal() {
        final AtomicBoolean doneAfter = new AtomicBoolean();
        final AtomicBoolean complete = new AtomicBoolean();
        Completable c = normal.completable.doAfterTerminate(new Action() {

            @Override
            public void run() {
                doneAfter.set(complete.get());
            }
        });
        c.subscribe(new CompletableObserver() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                complete.set(true);
            }
        });
        c.blockingAwait();
        Assert.assertTrue("Not completed", complete.get());
        Assert.assertTrue("Closure called before onComplete", doneAfter.get());
    }

    @Test
    public void doAfterTerminateWithError() {
        final AtomicBoolean doneAfter = new AtomicBoolean();
        Completable c = error.completable.doAfterTerminate(new Action() {

            @Override
            public void run() {
                doneAfter.set(true);
            }
        });
        try {
            c.blockingAwait(5, TimeUnit.SECONDS);
            Assert.fail("Did not throw TestException");
        } catch (TestException ex) {
        // expected
        }
        Assert.assertTrue("Closure not called", doneAfter.get());
    }

    @Test
    public void subscribeEmptyOnError() {
        expectUncaughtTestException(new Action() {

            @Override
            public void run() {
                error.completable.subscribe();
            }
        });
    }

    @Test
    public void subscribeOneActionOnError() {
        expectUncaughtTestException(new Action() {

            @Override
            public void run() {
                error.completable.subscribe(new Action() {

                    @Override
                    public void run() {
                    }
                });
            }
        });
    }

    @Test
    public void propagateExceptionSubscribeEmpty() {
        expectUncaughtTestException(new Action() {

            @Override
            public void run() {
                error.completable.toSingleDefault(0).subscribe();
            }
        });
    }

    @Test
    public void andThenCompletableNormal() {
        final AtomicBoolean run = new AtomicBoolean();
        Completable c = normal.completable.andThen(Completable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                run.set(normal.get() == 0);
                return null;
            }
        }));
        c.blockingAwait();
        Assert.assertFalse("Start with other", run.get());
        normal.assertSubscriptions(1);
    }

    @Test
    public void andThenCompletableError() {
        Completable c = normal.completable.andThen(error.completable);
        try {
            c.blockingAwait();
            Assert.fail("Did not throw TestException");
        } catch (TestException ex) {
            normal.assertSubscriptions(1);
            error.assertSubscriptions(1);
        }
    }

    @Test
    public void andThenFlowableNormal() {
        final AtomicBoolean run = new AtomicBoolean();
        Flowable<Object> c = normal.completable.andThen(Flowable.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                run.set(normal.get() == 0);
                return 1;
            }
        }));
        TestSubscriber<Object> ts = new TestSubscriber<>();
        c.subscribe(ts);
        Assert.assertFalse("Start with other", run.get());
        normal.assertSubscriptions(1);
        ts.assertValue(1);
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void andThenFlowableError() {
        Flowable<Object> c = normal.completable.andThen(Flowable.error(new TestException()));
        TestSubscriber<Object> ts = new TestSubscriber<>();
        c.subscribe(ts);
        normal.assertSubscriptions(1);
        ts.assertNoValues();
        ts.assertError(TestException.class);
        ts.assertNotComplete();
    }

    @Test
    public void usingFactoryThrows() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> onDispose = mock(Consumer.class);
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer t) {
                throw new TestException();
            }
        }, onDispose).<Integer>toFlowable().subscribe(ts);
        verify(onDispose).accept(1);
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(TestException.class);
    }

    @Test
    public void usingFactoryAndDisposerThrow() {
        Consumer<Integer> onDispose = new Consumer<Integer>() {

            @Override
            public void accept(Integer t) {
                throw new TestException();
            }
        };
        TestSubscriberEx<Integer> ts = new TestSubscriberEx<>();
        Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer t) {
                throw new TestException();
            }
        }, onDispose).<Integer>toFlowable().subscribe(ts);
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(CompositeException.class);
        CompositeException ex = (CompositeException) ts.errors().get(0);
        List<Throwable> listEx = ex.getExceptions();
        assertEquals(2, listEx.size());
        assertTrue(listEx.get(0).toString(), listEx.get(0) instanceof TestException);
        assertTrue(listEx.get(1).toString(), listEx.get(1) instanceof TestException);
    }

    @Test
    public void usingFactoryReturnsNull() throws Throwable {
        @SuppressWarnings("unchecked")
        Consumer<Integer> onDispose = mock(Consumer.class);
        TestSubscriber<Integer> ts = TestSubscriber.create();
        Completable.using(new Supplier<Integer>() {

            @Override
            public Integer get() {
                return 1;
            }
        }, new Function<Integer, Completable>() {

            @Override
            public Completable apply(Integer t) {
                return null;
            }
        }, onDispose).<Integer>toFlowable().subscribe(ts);
        verify(onDispose).accept(1);
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(NullPointerException.class);
    }

    @Test
    public void subscribeReportsUnsubscribed() {
        PublishSubject<String> stringSubject = PublishSubject.create();
        Completable completable = stringSubject.ignoreElements();
        Disposable completableSubscription = completable.subscribe();
        stringSubject.onComplete();
        assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
    }

    @Test
    public void hookSubscribeStart() throws Throwable {
        TestSubscriber<String> ts = new TestSubscriber<>();
        Completable completable = Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver observer) {
                observer.onComplete();
            }
        });
        completable.<String>toFlowable().subscribe(ts);
        verify(onStart, times(1)).apply(eq(completable), any(CompletableObserver.class));
    }

    @Test
    public void onStartCalledSafe() {
        TestSubscriber<Object> ts = new TestSubscriber<Object>() {

            @Override
            public void onStart() {
                onNext(1);
            }
        };
        normal.completable.<Object>toFlowable().subscribe(ts);
        ts.assertValue(1);
        ts.assertNoErrors();
        ts.assertComplete();
    }

    @Test
    public void onErrorCompleteFunctionThrows() {
        TestSubscriberEx<String> ts = new TestSubscriberEx<>();
        error.completable.onErrorComplete(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable t) {
                throw new TestException("Forced inner failure");
            }
        }).<String>toFlowable().subscribe(ts);
        ts.assertNoValues();
        ts.assertNotComplete();
        ts.assertError(CompositeException.class);
        CompositeException composite = (CompositeException) ts.errors().get(0);
        List<Throwable> errors = composite.getExceptions();
        Assert.assertEquals(2, errors.size());
        Assert.assertTrue(errors.get(0).toString(), errors.get(0) instanceof TestException);
        Assert.assertNull(errors.get(0).toString(), errors.get(0).getMessage());
        Assert.assertTrue(errors.get(1).toString(), errors.get(1) instanceof TestException);
        Assert.assertEquals(errors.get(1).toString(), "Forced inner failure", errors.get(1).getMessage());
    }

    @Test
    public void subscribeAction2ReportsUnsubscribedAfter() {
        PublishSubject<String> stringSubject = PublishSubject.create();
        Completable completable = stringSubject.ignoreElements();
        final AtomicReference<Disposable> disposableRef = new AtomicReference<>();
        Disposable completableSubscription = completable.subscribe(new Action() {

            @Override
            public void run() {
                if (disposableRef.get().isDisposed()) {
                    disposableRef.set(null);
                }
            }
        }, Functions.emptyConsumer());
        disposableRef.set(completableSubscription);
        stringSubject.onComplete();
        assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
        assertNotNull("Unsubscribed before the call to onComplete", disposableRef.get());
    }

    @Test
    public void subscribeAction2ReportsUnsubscribedOnErrorAfter() {
        PublishSubject<String> stringSubject = PublishSubject.create();
        Completable completable = stringSubject.ignoreElements();
        final AtomicReference<Disposable> disposableRef = new AtomicReference<>();
        Disposable completableSubscription = completable.subscribe(Functions.EMPTY_ACTION, new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
                if (disposableRef.get().isDisposed()) {
                    disposableRef.set(null);
                }
            }
        });
        disposableRef.set(completableSubscription);
        stringSubject.onError(new TestException());
        assertTrue("Not unsubscribed?", completableSubscription.isDisposed());
        assertNotNull("Unsubscribed before the call to onError", disposableRef.get());
    }

    @Test
    public void propagateExceptionSubscribeOneActionThrowFromOnSuccess() {
        expectUncaughtTestException(new Action() {

            @Override
            public void run() {
                normal.completable.toSingleDefault(1).subscribe(new Consumer<Integer>() {

                    @Override
                    public void accept(Integer integer) {
                        throw new TestException();
                    }
                });
            }
        });
    }

    @Test
    public void andThenNever() {
        TestSubscriberEx<String> ts = new TestSubscriberEx<>(0);
        Completable.never().andThen(Flowable.just("foo")).subscribe(ts);
        ts.request(1);
        ts.assertNoValues();
        ts.assertNotTerminated();
    }

    @Test
    public void andThenError() {
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        final AtomicBoolean hasRun = new AtomicBoolean(false);
        final Exception e = new Exception();
        Completable.unsafeCreate(new CompletableSource() {

            @Override
            public void subscribe(CompletableObserver co) {
                co.onSubscribe(Disposable.empty());
                co.onError(e);
            }
        }).andThen(Flowable.<String>unsafeCreate(new Publisher<String>() {

            @Override
            public void subscribe(Subscriber<? super String> s) {
                hasRun.set(true);
                s.onSubscribe(new BooleanSubscription());
                s.onNext("foo");
                s.onComplete();
            }
        })).subscribe(ts);
        ts.assertNoValues();
        ts.assertError(e);
        Assert.assertFalse("Should not have subscribed to observable when completable errors", hasRun.get());
    }

    @Test
    public void andThenSingle() {
        TestSubscriber<String> ts = new TestSubscriber<>(0);
        Completable.complete().andThen(Single.just("foo")).toFlowable().subscribe(ts);
        ts.request(1);
        ts.assertValue("foo");
        ts.assertComplete();
        ts.assertNoErrors();
    }

    @Test
    public void fromFutureNormal() {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            Completable c = Completable.fromFuture(exec.submit(new Runnable() {

                @Override
                public void run() {
                // no action
                }
            }));
            c.blockingAwait();
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void fromFutureThrows() {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Completable c = Completable.fromFuture(exec.submit(new Runnable() {

            @Override
            public void run() {
                throw new TestException();
            }
        }));
        try {
            c.blockingAwait();
            Assert.fail("Failed to throw Exception");
        } catch (RuntimeException ex) {
            if (!((ex.getCause() instanceof ExecutionException) && (ex.getCause().getCause() instanceof TestException))) {
                ex.printStackTrace();
                Assert.fail("Wrong exception received");
            }
        } finally {
            exec.shutdown();
        }
    }

    @Test
    public void fromRunnableNormal() {
        final AtomicInteger calls = new AtomicInteger();
        Completable c = Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                calls.getAndIncrement();
            }
        });
        c.blockingAwait();
        Assert.assertEquals(1, calls.get());
    }

    @Test(expected = TestException.class)
    public void fromRunnableThrows() {
        Completable c = Completable.fromRunnable(new Runnable() {

            @Override
            public void run() {
                throw new TestException();
            }
        });
        c.blockingAwait();
    }

    @Test
    public void doOnEventComplete() {
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        Completable.complete().doOnEvent(new Consumer<Throwable>() {

            @Override
            public void accept(final Throwable throwable) throws Exception {
                if (throwable == null) {
                    atomicInteger.incrementAndGet();
                }
            }
        }).subscribe();
        assertEquals(1, atomicInteger.get());
    }

    @Test
    public void doOnEventError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final AtomicInteger atomicInteger = new AtomicInteger(0);
            Completable.error(new RuntimeException()).doOnEvent(new Consumer<Throwable>() {

                @Override
                public void accept(final Throwable throwable) throws Exception {
                    if (throwable != null) {
                        atomicInteger.incrementAndGet();
                    }
                }
            }).subscribe();
            assertEquals(1, atomicInteger.get());
            TestHelper.assertError(errors, 0, OnErrorNotImplementedException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void subscribeTwoCallbacksDispose() {
        PublishProcessor<Integer> pp = PublishProcessor.create();
        Disposable d = pp.ignoreElements().subscribe(Functions.EMPTY_ACTION, Functions.emptyConsumer());
        assertFalse(d.isDisposed());
        assertTrue(pp.hasSubscribers());
        d.dispose();
        assertTrue(d.isDisposed());
        assertFalse(pp.hasSubscribers());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private CompletableTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_complete() throws java.lang.Throwable {
            this.payloads.complete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatEmpty() throws java.lang.Throwable {
            this.payloads.concatEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatSingleSource() throws java.lang.Throwable {
            this.payloads.concatSingleSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatSingleSourceThrows() throws java.lang.Throwable {
            this.payloads.concatSingleSourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMultipleSources() throws java.lang.Throwable {
            this.payloads.concatMultipleSources.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMultipleOneThrows() throws java.lang.Throwable {
            this.payloads.concatMultipleOneThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatMultipleOneIsNull() throws java.lang.Throwable {
            this.payloads.concatMultipleOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableEmpty() throws java.lang.Throwable {
            this.payloads.concatIterableEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.concatIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableSingle() throws java.lang.Throwable {
            this.payloads.concatIterableSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableMany() throws java.lang.Throwable {
            this.payloads.concatIterableMany.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableOneThrows() throws java.lang.Throwable {
            this.payloads.concatIterableOneThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableManyOneThrows() throws java.lang.Throwable {
            this.payloads.concatIterableManyOneThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableIterableThrows() throws java.lang.Throwable {
            this.payloads.concatIterableIterableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableIteratorHasNextThrows() throws java.lang.Throwable {
            this.payloads.concatIterableIteratorHasNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatIterableIteratorNextThrows() throws java.lang.Throwable {
            this.payloads.concatIterableIteratorNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableEmpty() throws java.lang.Throwable {
            this.payloads.concatObservableEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableError() throws java.lang.Throwable {
            this.payloads.concatObservableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableSingle() throws java.lang.Throwable {
            this.payloads.concatObservableSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableSingleThrows() throws java.lang.Throwable {
            this.payloads.concatObservableSingleThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableMany() throws java.lang.Throwable {
            this.payloads.concatObservableMany.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservableManyOneThrows() throws java.lang.Throwable {
            this.payloads.concatObservableManyOneThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatObservablePrefetch() throws java.lang.Throwable {
            this.payloads.concatObservablePrefetch.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createOnSubscribeThrowsNPE() throws java.lang.Throwable {
            this.payloads.createOnSubscribeThrowsNPE.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_createOnSubscribeThrowsRuntimeException() throws java.lang.Throwable {
            this.payloads.createOnSubscribeThrowsRuntimeException.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_defer() throws java.lang.Throwable {
            this.payloads.defer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferReturnsNull() throws java.lang.Throwable {
            this.payloads.deferReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferFunctionThrows() throws java.lang.Throwable {
            this.payloads.deferFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_deferErrorSource() throws java.lang.Throwable {
            this.payloads.deferErrorSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSupplierNormal() throws java.lang.Throwable {
            this.payloads.errorSupplierNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.errorSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSupplierThrows() throws java.lang.Throwable {
            this.payloads.errorSupplierThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorNormal() throws java.lang.Throwable {
            this.payloads.errorNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallableNormal() throws java.lang.Throwable {
            this.payloads.fromCallableNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromCallableThrows() throws java.lang.Throwable {
            this.payloads.fromCallableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFlowableEmpty() throws java.lang.Throwable {
            this.payloads.fromFlowableEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFlowableSome() throws java.lang.Throwable {
            this.payloads.fromFlowableSome.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFlowableError() throws java.lang.Throwable {
            this.payloads.fromFlowableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableEmpty() throws java.lang.Throwable {
            this.payloads.fromObservableEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableSome() throws java.lang.Throwable {
            this.payloads.fromObservableSome.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromObservableError() throws java.lang.Throwable {
            this.payloads.fromObservableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionNormal() throws java.lang.Throwable {
            this.payloads.fromActionNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromActionThrows() throws java.lang.Throwable {
            this.payloads.fromActionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromSingleNormal() throws java.lang.Throwable {
            this.payloads.fromSingleNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromSingleThrows() throws java.lang.Throwable {
            this.payloads.fromSingleThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeEmpty() throws java.lang.Throwable {
            this.payloads.mergeEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeSingleSource() throws java.lang.Throwable {
            this.payloads.mergeSingleSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeSingleSourceThrows() throws java.lang.Throwable {
            this.payloads.mergeSingleSourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeMultipleSources() throws java.lang.Throwable {
            this.payloads.mergeMultipleSources.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeMultipleOneThrows() throws java.lang.Throwable {
            this.payloads.mergeMultipleOneThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeMultipleOneIsNull() throws java.lang.Throwable {
            this.payloads.mergeMultipleOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableEmpty() throws java.lang.Throwable {
            this.payloads.mergeIterableEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.mergeIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableSingle() throws java.lang.Throwable {
            this.payloads.mergeIterableSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableMany() throws java.lang.Throwable {
            this.payloads.mergeIterableMany.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableOneThrows() throws java.lang.Throwable {
            this.payloads.mergeIterableOneThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableManyOneThrows() throws java.lang.Throwable {
            this.payloads.mergeIterableManyOneThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableIterableThrows() throws java.lang.Throwable {
            this.payloads.mergeIterableIterableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableIteratorHasNextThrows() throws java.lang.Throwable {
            this.payloads.mergeIterableIteratorHasNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeIterableIteratorNextThrows() throws java.lang.Throwable {
            this.payloads.mergeIterableIteratorNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableEmpty() throws java.lang.Throwable {
            this.payloads.mergeObservableEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableError() throws java.lang.Throwable {
            this.payloads.mergeObservableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableSingle() throws java.lang.Throwable {
            this.payloads.mergeObservableSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableSingleThrows() throws java.lang.Throwable {
            this.payloads.mergeObservableSingleThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableMany() throws java.lang.Throwable {
            this.payloads.mergeObservableMany.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableManyOneThrows() throws java.lang.Throwable {
            this.payloads.mergeObservableManyOneThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeObservableMaxConcurrent() throws java.lang.Throwable {
            this.payloads.mergeObservableMaxConcurrent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorEmpty() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorSingleSource() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorSingleSource.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorSingleSourceThrows() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorSingleSourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorMultipleSources() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorMultipleSources.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorMultipleOneThrows() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorMultipleOneThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorMultipleOneIsNull() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorMultipleOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableEmpty() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorIterableEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableSingle() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorIterableSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableMany() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorIterableMany.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableOneThrows() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorIterableOneThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableManyOneThrows() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorIterableManyOneThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableIterableThrows() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorIterableIterableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableIteratorHasNextThrows() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorIterableIteratorHasNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorIterableIteratorNextThrows() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorIterableIteratorNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorObservableEmpty() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorObservableEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorObservableError() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorObservableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorObservableSingle() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorObservableSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorObservableSingleThrows() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorObservableSingleThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorObservableMany() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorObservableMany.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorObservableManyOneThrows() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorObservableManyOneThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeDelayErrorObservableMaxConcurrent() throws java.lang.Throwable {
            this.payloads.mergeDelayErrorObservableMaxConcurrent.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_never() throws java.lang.Throwable {
            this.payloads.never.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timer() throws java.lang.Throwable {
            this.payloads.timer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timerNewThread() throws java.lang.Throwable {
            this.payloads.timerNewThread.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timerTestScheduler() throws java.lang.Throwable {
            this.payloads.timerTestScheduler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timerCancel() throws java.lang.Throwable {
            this.payloads.timerCancel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingNormalEager() throws java.lang.Throwable {
            this.payloads.usingNormalEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingNormalLazy() throws java.lang.Throwable {
            this.payloads.usingNormalLazy.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingErrorEager() throws java.lang.Throwable {
            this.payloads.usingErrorEager.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingErrorLazy() throws java.lang.Throwable {
            this.payloads.usingErrorLazy.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingMapperReturnsNull() throws java.lang.Throwable {
            this.payloads.usingMapperReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingResourceThrows() throws java.lang.Throwable {
            this.payloads.usingResourceThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingMapperThrows() throws java.lang.Throwable {
            this.payloads.usingMapperThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingDisposerThrows() throws java.lang.Throwable {
            this.payloads.usingDisposerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_composeNormal() throws java.lang.Throwable {
            this.payloads.composeNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWithNormal() throws java.lang.Throwable {
            this.payloads.concatWithNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_concatWithError() throws java.lang.Throwable {
            this.payloads.concatWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayNormal() throws java.lang.Throwable {
            this.payloads.delayNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorImmediately() throws java.lang.Throwable {
            this.payloads.delayErrorImmediately.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayErrorToo() throws java.lang.Throwable {
            this.payloads.delayErrorToo.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompleteNormal() throws java.lang.Throwable {
            this.payloads.doOnCompleteNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompleteError() throws java.lang.Throwable {
            this.payloads.doOnCompleteError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompleteThrows() throws java.lang.Throwable {
            this.payloads.doOnCompleteThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeNormalDoesntCall() throws java.lang.Throwable {
            this.payloads.doOnDisposeNormalDoesntCall.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeErrorDoesntCall() throws java.lang.Throwable {
            this.payloads.doOnDisposeErrorDoesntCall.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeChildCancels() throws java.lang.Throwable {
            this.payloads.doOnDisposeChildCancels.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeThrows() throws java.lang.Throwable {
            this.payloads.doOnDisposeThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnErrorNoError() throws java.lang.Throwable {
            this.payloads.doOnErrorNoError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnErrorHasError() throws java.lang.Throwable {
            this.payloads.doOnErrorHasError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnErrorThrows() throws java.lang.Throwable {
            this.payloads.doOnErrorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribeNormal() throws java.lang.Throwable {
            this.payloads.doOnSubscribeNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribeThrows() throws java.lang.Throwable {
            this.payloads.doOnSubscribeThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateNormal() throws java.lang.Throwable {
            this.payloads.doOnTerminateNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnTerminateError() throws java.lang.Throwable {
            this.payloads.doOnTerminateError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_liftReturnsNull() throws java.lang.Throwable {
            this.payloads.liftReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_liftOnCompleteError() throws java.lang.Throwable {
            this.payloads.liftOnCompleteError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_liftOnErrorComplete() throws java.lang.Throwable {
            this.payloads.liftOnErrorComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_mergeWithNormal() throws java.lang.Throwable {
            this.payloads.mergeWithNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOnNormal() throws java.lang.Throwable {
            this.payloads.observeOnNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_observeOnError() throws java.lang.Throwable {
            this.payloads.observeOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorComplete() throws java.lang.Throwable {
            this.payloads.onErrorComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteFalse() throws java.lang.Throwable {
            this.payloads.onErrorCompleteFalse.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNextFunctionReturnsNull() throws java.lang.Throwable {
            this.payloads.onErrorResumeNextFunctionReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNextFunctionThrows() throws java.lang.Throwable {
            this.payloads.onErrorResumeNextFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNextNormal() throws java.lang.Throwable {
            this.payloads.onErrorResumeNextNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeNextError() throws java.lang.Throwable {
            this.payloads.onErrorResumeNextError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatNormal() throws java.lang.Throwable {
            this.payloads.repeatNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatError() throws java.lang.Throwable {
            this.payloads.repeatError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeat5Times() throws java.lang.Throwable {
            this.payloads.repeat5Times.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeat1Time() throws java.lang.Throwable {
            this.payloads.repeat1Time.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeat0Time() throws java.lang.Throwable {
            this.payloads.repeat0Time.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatUntilNormal() throws java.lang.Throwable {
            this.payloads.repeatUntilNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryNormal() throws java.lang.Throwable {
            this.payloads.retryNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retry5Times() throws java.lang.Throwable {
            this.payloads.retry5Times.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryBiPredicate5Times() throws java.lang.Throwable {
            this.payloads.retryBiPredicate5Times.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTimes5Error() throws java.lang.Throwable {
            this.payloads.retryTimes5Error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTimes5Normal() throws java.lang.Throwable {
            this.payloads.retryTimes5Normal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryNegativeTimes() throws java.lang.Throwable {
            this.payloads.retryNegativeTimes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryPredicateError() throws java.lang.Throwable {
            this.payloads.retryPredicateError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryPredicate5Times() throws java.lang.Throwable {
            this.payloads.retryPredicate5Times.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryWhen5Times() throws java.lang.Throwable {
            this.payloads.retryWhen5Times.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribe() throws java.lang.Throwable {
            this.payloads.subscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeDispose() throws java.lang.Throwable {
            this.payloads.subscribeDispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeTwoCallbacksNormal() throws java.lang.Throwable {
            this.payloads.subscribeTwoCallbacksNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeTwoCallbacksError() throws java.lang.Throwable {
            this.payloads.subscribeTwoCallbacksError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeTwoCallbacksCompleteThrows() throws java.lang.Throwable {
            this.payloads.subscribeTwoCallbacksCompleteThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeTwoCallbacksOnErrorThrows() throws java.lang.Throwable {
            this.payloads.subscribeTwoCallbacksOnErrorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeObserverNormal() throws java.lang.Throwable {
            this.payloads.subscribeObserverNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeObserverError() throws java.lang.Throwable {
            this.payloads.subscribeObserverError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeActionNormal() throws java.lang.Throwable {
            this.payloads.subscribeActionNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeActionError() throws java.lang.Throwable {
            this.payloads.subscribeActionError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeSubscriberNormal() throws java.lang.Throwable {
            this.payloads.subscribeSubscriberNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeSubscriberError() throws java.lang.Throwable {
            this.payloads.subscribeSubscriberError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOnNormal() throws java.lang.Throwable {
            this.payloads.subscribeOnNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOnError() throws java.lang.Throwable {
            this.payloads.subscribeOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutSwitchNormal() throws java.lang.Throwable {
            this.payloads.timeoutSwitchNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutTimerCancelled() throws java.lang.Throwable {
            this.payloads.timeoutTimerCancelled.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toNormal() throws java.lang.Throwable {
            this.payloads.toNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asNormal() throws java.lang.Throwable {
            this.payloads.asNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_as() throws java.lang.Throwable {
            this.payloads.as.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableNormal() throws java.lang.Throwable {
            this.payloads.toFlowableNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toFlowableError() throws java.lang.Throwable {
            this.payloads.toFlowableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservableNormal() throws java.lang.Throwable {
            this.payloads.toObservableNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservableError() throws java.lang.Throwable {
            this.payloads.toObservableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSingleSupplierNormal() throws java.lang.Throwable {
            this.payloads.toSingleSupplierNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSingleSupplierError() throws java.lang.Throwable {
            this.payloads.toSingleSupplierError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSingleSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.toSingleSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSingleSupplierThrows() throws java.lang.Throwable {
            this.payloads.toSingleSupplierThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSingleDefaultError() throws java.lang.Throwable {
            this.payloads.toSingleDefaultError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toSingleDefaultNormal() throws java.lang.Throwable {
            this.payloads.toSingleDefaultNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeOnNormal() throws java.lang.Throwable {
            this.payloads.unsubscribeOnNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArrayEmpty() throws java.lang.Throwable {
            this.payloads.ambArrayEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArraySingleNormal() throws java.lang.Throwable {
            this.payloads.ambArraySingleNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArraySingleError() throws java.lang.Throwable {
            this.payloads.ambArraySingleError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArrayOneFires() throws java.lang.Throwable {
            this.payloads.ambArrayOneFires.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArrayOneFiresError() throws java.lang.Throwable {
            this.payloads.ambArrayOneFiresError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArraySecondFires() throws java.lang.Throwable {
            this.payloads.ambArraySecondFires.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambArraySecondFiresError() throws java.lang.Throwable {
            this.payloads.ambArraySecondFiresError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambMultipleOneIsNull() throws java.lang.Throwable {
            this.payloads.ambMultipleOneIsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableEmpty() throws java.lang.Throwable {
            this.payloads.ambIterableEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableIteratorNull() throws java.lang.Throwable {
            this.payloads.ambIterableIteratorNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableWithNull() throws java.lang.Throwable {
            this.payloads.ambIterableWithNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableSingle() throws java.lang.Throwable {
            this.payloads.ambIterableSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableMany() throws java.lang.Throwable {
            this.payloads.ambIterableMany.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableOneThrows() throws java.lang.Throwable {
            this.payloads.ambIterableOneThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableManyOneThrows() throws java.lang.Throwable {
            this.payloads.ambIterableManyOneThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableIterableThrows() throws java.lang.Throwable {
            this.payloads.ambIterableIterableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableIteratorHasNextThrows() throws java.lang.Throwable {
            this.payloads.ambIterableIteratorHasNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambIterableIteratorNextThrows() throws java.lang.Throwable {
            this.payloads.ambIterableIteratorNextThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWithArrayOneFires() throws java.lang.Throwable {
            this.payloads.ambWithArrayOneFires.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWithArrayOneFiresError() throws java.lang.Throwable {
            this.payloads.ambWithArrayOneFiresError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWithArraySecondFires() throws java.lang.Throwable {
            this.payloads.ambWithArraySecondFires.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ambWithArraySecondFiresError() throws java.lang.Throwable {
            this.payloads.ambWithArraySecondFiresError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithCompletableNormal() throws java.lang.Throwable {
            this.payloads.startWithCompletableNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithCompletableError() throws java.lang.Throwable {
            this.payloads.startWithCompletableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithFlowableNormal() throws java.lang.Throwable {
            this.payloads.startWithFlowableNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithFlowableError() throws java.lang.Throwable {
            this.payloads.startWithFlowableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithObservableNormal() throws java.lang.Throwable {
            this.payloads.startWithObservableNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_startWithObservableError() throws java.lang.Throwable {
            this.payloads.startWithObservableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThen() throws java.lang.Throwable {
            this.payloads.andThen.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOneActionThrowFromOnCompleted() throws java.lang.Throwable {
            this.payloads.subscribeOneActionThrowFromOnCompleted.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeTwoActionsThrowFromOnError() throws java.lang.Throwable {
            this.payloads.subscribeTwoActionsThrowFromOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_propagateExceptionSubscribeOneAction() throws java.lang.Throwable {
            this.payloads.propagateExceptionSubscribeOneAction.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingFactoryReturnsNullAndDisposerThrows() throws java.lang.Throwable {
            this.payloads.usingFactoryReturnsNullAndDisposerThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeReportsUnsubscribedOnError() throws java.lang.Throwable {
            this.payloads.subscribeReportsUnsubscribedOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeActionReportsUnsubscribed() throws java.lang.Throwable {
            this.payloads.subscribeActionReportsUnsubscribed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeActionReportsUnsubscribedAfter() throws java.lang.Throwable {
            this.payloads.subscribeActionReportsUnsubscribedAfter.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeActionReportsUnsubscribedOnError() throws java.lang.Throwable {
            this.payloads.subscribeActionReportsUnsubscribedOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeAction2ReportsUnsubscribed() throws java.lang.Throwable {
            this.payloads.subscribeAction2ReportsUnsubscribed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeAction2ReportsUnsubscribedOnError() throws java.lang.Throwable {
            this.payloads.subscribeAction2ReportsUnsubscribedOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenSubscribeOn() throws java.lang.Throwable {
            this.payloads.andThenSubscribeOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenSingleNever() throws java.lang.Throwable {
            this.payloads.andThenSingleNever.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenSingleError() throws java.lang.Throwable {
            this.payloads.andThenSingleError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenSingleSubscribeOn() throws java.lang.Throwable {
            this.payloads.andThenSingleSubscribeOn.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hookCreate() throws java.lang.Throwable {
            this.payloads.hookCreate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompletedNormal() throws java.lang.Throwable {
            this.payloads.doOnCompletedNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompletedError() throws java.lang.Throwable {
            this.payloads.doOnCompletedError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnCompletedThrows() throws java.lang.Throwable {
            this.payloads.doOnCompletedThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doAfterTerminateNormal() throws java.lang.Throwable {
            this.payloads.doAfterTerminateNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doAfterTerminateWithError() throws java.lang.Throwable {
            this.payloads.doAfterTerminateWithError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeEmptyOnError() throws java.lang.Throwable {
            this.payloads.subscribeEmptyOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeOneActionOnError() throws java.lang.Throwable {
            this.payloads.subscribeOneActionOnError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_propagateExceptionSubscribeEmpty() throws java.lang.Throwable {
            this.payloads.propagateExceptionSubscribeEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenCompletableNormal() throws java.lang.Throwable {
            this.payloads.andThenCompletableNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenCompletableError() throws java.lang.Throwable {
            this.payloads.andThenCompletableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenFlowableNormal() throws java.lang.Throwable {
            this.payloads.andThenFlowableNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenFlowableError() throws java.lang.Throwable {
            this.payloads.andThenFlowableError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingFactoryThrows() throws java.lang.Throwable {
            this.payloads.usingFactoryThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingFactoryAndDisposerThrow() throws java.lang.Throwable {
            this.payloads.usingFactoryAndDisposerThrow.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_usingFactoryReturnsNull() throws java.lang.Throwable {
            this.payloads.usingFactoryReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeReportsUnsubscribed() throws java.lang.Throwable {
            this.payloads.subscribeReportsUnsubscribed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hookSubscribeStart() throws java.lang.Throwable {
            this.payloads.hookSubscribeStart.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onStartCalledSafe() throws java.lang.Throwable {
            this.payloads.onStartCalledSafe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCompleteFunctionThrows() throws java.lang.Throwable {
            this.payloads.onErrorCompleteFunctionThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeAction2ReportsUnsubscribedAfter() throws java.lang.Throwable {
            this.payloads.subscribeAction2ReportsUnsubscribedAfter.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeAction2ReportsUnsubscribedOnErrorAfter() throws java.lang.Throwable {
            this.payloads.subscribeAction2ReportsUnsubscribedOnErrorAfter.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_propagateExceptionSubscribeOneActionThrowFromOnSuccess() throws java.lang.Throwable {
            this.payloads.propagateExceptionSubscribeOneActionThrowFromOnSuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenNever() throws java.lang.Throwable {
            this.payloads.andThenNever.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenError() throws java.lang.Throwable {
            this.payloads.andThenError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_andThenSingle() throws java.lang.Throwable {
            this.payloads.andThenSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFutureNormal() throws java.lang.Throwable {
            this.payloads.fromFutureNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromFutureThrows() throws java.lang.Throwable {
            this.payloads.fromFutureThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableNormal() throws java.lang.Throwable {
            this.payloads.fromRunnableNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_fromRunnableThrows() throws java.lang.Throwable {
            this.payloads.fromRunnableThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEventComplete() throws java.lang.Throwable {
            this.payloads.doOnEventComplete.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEventError() throws java.lang.Throwable {
            this.payloads.doOnEventError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subscribeTwoCallbacksDispose() throws java.lang.Throwable {
            this.payloads.subscribeTwoCallbacksDispose.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.setUp();
                try {
                    this.payload.accept(this.benchmark.instance);
                } finally {
                    this.benchmark.instance.after();
                }
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new CompletableTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<CompletableTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(CompletableTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(CompletableTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement complete;

            public org.junit.runners.model.Statement concatEmpty;

            public org.junit.runners.model.Statement concatSingleSource;

            public org.junit.runners.model.Statement concatSingleSourceThrows;

            public org.junit.runners.model.Statement concatMultipleSources;

            public org.junit.runners.model.Statement concatMultipleOneThrows;

            public org.junit.runners.model.Statement concatMultipleOneIsNull;

            public org.junit.runners.model.Statement concatIterableEmpty;

            public org.junit.runners.model.Statement concatIterableIteratorNull;

            public org.junit.runners.model.Statement concatIterableSingle;

            public org.junit.runners.model.Statement concatIterableMany;

            public org.junit.runners.model.Statement concatIterableOneThrows;

            public org.junit.runners.model.Statement concatIterableManyOneThrows;

            public org.junit.runners.model.Statement concatIterableIterableThrows;

            public org.junit.runners.model.Statement concatIterableIteratorHasNextThrows;

            public org.junit.runners.model.Statement concatIterableIteratorNextThrows;

            public org.junit.runners.model.Statement concatObservableEmpty;

            public org.junit.runners.model.Statement concatObservableError;

            public org.junit.runners.model.Statement concatObservableSingle;

            public org.junit.runners.model.Statement concatObservableSingleThrows;

            public org.junit.runners.model.Statement concatObservableMany;

            public org.junit.runners.model.Statement concatObservableManyOneThrows;

            public org.junit.runners.model.Statement concatObservablePrefetch;

            public org.junit.runners.model.Statement createOnSubscribeThrowsNPE;

            public org.junit.runners.model.Statement createOnSubscribeThrowsRuntimeException;

            public org.junit.runners.model.Statement defer;

            public org.junit.runners.model.Statement deferReturnsNull;

            public org.junit.runners.model.Statement deferFunctionThrows;

            public org.junit.runners.model.Statement deferErrorSource;

            public org.junit.runners.model.Statement errorSupplierNormal;

            public org.junit.runners.model.Statement errorSupplierReturnsNull;

            public org.junit.runners.model.Statement errorSupplierThrows;

            public org.junit.runners.model.Statement errorNormal;

            public org.junit.runners.model.Statement fromCallableNormal;

            public org.junit.runners.model.Statement fromCallableThrows;

            public org.junit.runners.model.Statement fromFlowableEmpty;

            public org.junit.runners.model.Statement fromFlowableSome;

            public org.junit.runners.model.Statement fromFlowableError;

            public org.junit.runners.model.Statement fromObservableEmpty;

            public org.junit.runners.model.Statement fromObservableSome;

            public org.junit.runners.model.Statement fromObservableError;

            public org.junit.runners.model.Statement fromActionNormal;

            public org.junit.runners.model.Statement fromActionThrows;

            public org.junit.runners.model.Statement fromSingleNormal;

            public org.junit.runners.model.Statement fromSingleThrows;

            public org.junit.runners.model.Statement mergeEmpty;

            public org.junit.runners.model.Statement mergeSingleSource;

            public org.junit.runners.model.Statement mergeSingleSourceThrows;

            public org.junit.runners.model.Statement mergeMultipleSources;

            public org.junit.runners.model.Statement mergeMultipleOneThrows;

            public org.junit.runners.model.Statement mergeMultipleOneIsNull;

            public org.junit.runners.model.Statement mergeIterableEmpty;

            public org.junit.runners.model.Statement mergeIterableIteratorNull;

            public org.junit.runners.model.Statement mergeIterableSingle;

            public org.junit.runners.model.Statement mergeIterableMany;

            public org.junit.runners.model.Statement mergeIterableOneThrows;

            public org.junit.runners.model.Statement mergeIterableManyOneThrows;

            public org.junit.runners.model.Statement mergeIterableIterableThrows;

            public org.junit.runners.model.Statement mergeIterableIteratorHasNextThrows;

            public org.junit.runners.model.Statement mergeIterableIteratorNextThrows;

            public org.junit.runners.model.Statement mergeObservableEmpty;

            public org.junit.runners.model.Statement mergeObservableError;

            public org.junit.runners.model.Statement mergeObservableSingle;

            public org.junit.runners.model.Statement mergeObservableSingleThrows;

            public org.junit.runners.model.Statement mergeObservableMany;

            public org.junit.runners.model.Statement mergeObservableManyOneThrows;

            public org.junit.runners.model.Statement mergeObservableMaxConcurrent;

            public org.junit.runners.model.Statement mergeDelayErrorEmpty;

            public org.junit.runners.model.Statement mergeDelayErrorSingleSource;

            public org.junit.runners.model.Statement mergeDelayErrorSingleSourceThrows;

            public org.junit.runners.model.Statement mergeDelayErrorMultipleSources;

            public org.junit.runners.model.Statement mergeDelayErrorMultipleOneThrows;

            public org.junit.runners.model.Statement mergeDelayErrorMultipleOneIsNull;

            public org.junit.runners.model.Statement mergeDelayErrorIterableEmpty;

            public org.junit.runners.model.Statement mergeDelayErrorIterableIteratorNull;

            public org.junit.runners.model.Statement mergeDelayErrorIterableSingle;

            public org.junit.runners.model.Statement mergeDelayErrorIterableMany;

            public org.junit.runners.model.Statement mergeDelayErrorIterableOneThrows;

            public org.junit.runners.model.Statement mergeDelayErrorIterableManyOneThrows;

            public org.junit.runners.model.Statement mergeDelayErrorIterableIterableThrows;

            public org.junit.runners.model.Statement mergeDelayErrorIterableIteratorHasNextThrows;

            public org.junit.runners.model.Statement mergeDelayErrorIterableIteratorNextThrows;

            public org.junit.runners.model.Statement mergeDelayErrorObservableEmpty;

            public org.junit.runners.model.Statement mergeDelayErrorObservableError;

            public org.junit.runners.model.Statement mergeDelayErrorObservableSingle;

            public org.junit.runners.model.Statement mergeDelayErrorObservableSingleThrows;

            public org.junit.runners.model.Statement mergeDelayErrorObservableMany;

            public org.junit.runners.model.Statement mergeDelayErrorObservableManyOneThrows;

            public org.junit.runners.model.Statement mergeDelayErrorObservableMaxConcurrent;

            public org.junit.runners.model.Statement never;

            public org.junit.runners.model.Statement timer;

            public org.junit.runners.model.Statement timerNewThread;

            public org.junit.runners.model.Statement timerTestScheduler;

            public org.junit.runners.model.Statement timerCancel;

            public org.junit.runners.model.Statement usingNormalEager;

            public org.junit.runners.model.Statement usingNormalLazy;

            public org.junit.runners.model.Statement usingErrorEager;

            public org.junit.runners.model.Statement usingErrorLazy;

            public org.junit.runners.model.Statement usingMapperReturnsNull;

            public org.junit.runners.model.Statement usingResourceThrows;

            public org.junit.runners.model.Statement usingMapperThrows;

            public org.junit.runners.model.Statement usingDisposerThrows;

            public org.junit.runners.model.Statement composeNormal;

            public org.junit.runners.model.Statement concatWithNormal;

            public org.junit.runners.model.Statement concatWithError;

            public org.junit.runners.model.Statement delayNormal;

            public org.junit.runners.model.Statement delayErrorImmediately;

            public org.junit.runners.model.Statement delayErrorToo;

            public org.junit.runners.model.Statement doOnCompleteNormal;

            public org.junit.runners.model.Statement doOnCompleteError;

            public org.junit.runners.model.Statement doOnCompleteThrows;

            public org.junit.runners.model.Statement doOnDisposeNormalDoesntCall;

            public org.junit.runners.model.Statement doOnDisposeErrorDoesntCall;

            public org.junit.runners.model.Statement doOnDisposeChildCancels;

            public org.junit.runners.model.Statement doOnDisposeThrows;

            public org.junit.runners.model.Statement doOnErrorNoError;

            public org.junit.runners.model.Statement doOnErrorHasError;

            public org.junit.runners.model.Statement doOnErrorThrows;

            public org.junit.runners.model.Statement doOnSubscribeNormal;

            public org.junit.runners.model.Statement doOnSubscribeThrows;

            public org.junit.runners.model.Statement doOnTerminateNormal;

            public org.junit.runners.model.Statement doOnTerminateError;

            public org.junit.runners.model.Statement liftReturnsNull;

            public org.junit.runners.model.Statement liftOnCompleteError;

            public org.junit.runners.model.Statement liftOnErrorComplete;

            public org.junit.runners.model.Statement mergeWithNormal;

            public org.junit.runners.model.Statement observeOnNormal;

            public org.junit.runners.model.Statement observeOnError;

            public org.junit.runners.model.Statement onErrorComplete;

            public org.junit.runners.model.Statement onErrorCompleteFalse;

            public org.junit.runners.model.Statement onErrorResumeNextFunctionReturnsNull;

            public org.junit.runners.model.Statement onErrorResumeNextFunctionThrows;

            public org.junit.runners.model.Statement onErrorResumeNextNormal;

            public org.junit.runners.model.Statement onErrorResumeNextError;

            public org.junit.runners.model.Statement repeatNormal;

            public org.junit.runners.model.Statement repeatError;

            public org.junit.runners.model.Statement repeat5Times;

            public org.junit.runners.model.Statement repeat1Time;

            public org.junit.runners.model.Statement repeat0Time;

            public org.junit.runners.model.Statement repeatUntilNormal;

            public org.junit.runners.model.Statement retryNormal;

            public org.junit.runners.model.Statement retry5Times;

            public org.junit.runners.model.Statement retryBiPredicate5Times;

            public org.junit.runners.model.Statement retryTimes5Error;

            public org.junit.runners.model.Statement retryTimes5Normal;

            public org.junit.runners.model.Statement retryNegativeTimes;

            public org.junit.runners.model.Statement retryPredicateError;

            public org.junit.runners.model.Statement retryPredicate5Times;

            public org.junit.runners.model.Statement retryWhen5Times;

            public org.junit.runners.model.Statement subscribe;

            public org.junit.runners.model.Statement subscribeDispose;

            public org.junit.runners.model.Statement subscribeTwoCallbacksNormal;

            public org.junit.runners.model.Statement subscribeTwoCallbacksError;

            public org.junit.runners.model.Statement subscribeTwoCallbacksCompleteThrows;

            public org.junit.runners.model.Statement subscribeTwoCallbacksOnErrorThrows;

            public org.junit.runners.model.Statement subscribeObserverNormal;

            public org.junit.runners.model.Statement subscribeObserverError;

            public org.junit.runners.model.Statement subscribeActionNormal;

            public org.junit.runners.model.Statement subscribeActionError;

            public org.junit.runners.model.Statement subscribeSubscriberNormal;

            public org.junit.runners.model.Statement subscribeSubscriberError;

            public org.junit.runners.model.Statement subscribeOnNormal;

            public org.junit.runners.model.Statement subscribeOnError;

            public org.junit.runners.model.Statement timeoutSwitchNormal;

            public org.junit.runners.model.Statement timeoutTimerCancelled;

            public org.junit.runners.model.Statement toNormal;

            public org.junit.runners.model.Statement asNormal;

            public org.junit.runners.model.Statement as;

            public org.junit.runners.model.Statement toFlowableNormal;

            public org.junit.runners.model.Statement toFlowableError;

            public org.junit.runners.model.Statement toObservableNormal;

            public org.junit.runners.model.Statement toObservableError;

            public org.junit.runners.model.Statement toSingleSupplierNormal;

            public org.junit.runners.model.Statement toSingleSupplierError;

            public org.junit.runners.model.Statement toSingleSupplierReturnsNull;

            public org.junit.runners.model.Statement toSingleSupplierThrows;

            public org.junit.runners.model.Statement toSingleDefaultError;

            public org.junit.runners.model.Statement toSingleDefaultNormal;

            public org.junit.runners.model.Statement unsubscribeOnNormal;

            public org.junit.runners.model.Statement ambArrayEmpty;

            public org.junit.runners.model.Statement ambArraySingleNormal;

            public org.junit.runners.model.Statement ambArraySingleError;

            public org.junit.runners.model.Statement ambArrayOneFires;

            public org.junit.runners.model.Statement ambArrayOneFiresError;

            public org.junit.runners.model.Statement ambArraySecondFires;

            public org.junit.runners.model.Statement ambArraySecondFiresError;

            public org.junit.runners.model.Statement ambMultipleOneIsNull;

            public org.junit.runners.model.Statement ambIterableEmpty;

            public org.junit.runners.model.Statement ambIterableIteratorNull;

            public org.junit.runners.model.Statement ambIterableWithNull;

            public org.junit.runners.model.Statement ambIterableSingle;

            public org.junit.runners.model.Statement ambIterableMany;

            public org.junit.runners.model.Statement ambIterableOneThrows;

            public org.junit.runners.model.Statement ambIterableManyOneThrows;

            public org.junit.runners.model.Statement ambIterableIterableThrows;

            public org.junit.runners.model.Statement ambIterableIteratorHasNextThrows;

            public org.junit.runners.model.Statement ambIterableIteratorNextThrows;

            public org.junit.runners.model.Statement ambWithArrayOneFires;

            public org.junit.runners.model.Statement ambWithArrayOneFiresError;

            public org.junit.runners.model.Statement ambWithArraySecondFires;

            public org.junit.runners.model.Statement ambWithArraySecondFiresError;

            public org.junit.runners.model.Statement startWithCompletableNormal;

            public org.junit.runners.model.Statement startWithCompletableError;

            public org.junit.runners.model.Statement startWithFlowableNormal;

            public org.junit.runners.model.Statement startWithFlowableError;

            public org.junit.runners.model.Statement startWithObservableNormal;

            public org.junit.runners.model.Statement startWithObservableError;

            public org.junit.runners.model.Statement andThen;

            public org.junit.runners.model.Statement subscribeOneActionThrowFromOnCompleted;

            public org.junit.runners.model.Statement subscribeTwoActionsThrowFromOnError;

            public org.junit.runners.model.Statement propagateExceptionSubscribeOneAction;

            public org.junit.runners.model.Statement usingFactoryReturnsNullAndDisposerThrows;

            public org.junit.runners.model.Statement subscribeReportsUnsubscribedOnError;

            public org.junit.runners.model.Statement subscribeActionReportsUnsubscribed;

            public org.junit.runners.model.Statement subscribeActionReportsUnsubscribedAfter;

            public org.junit.runners.model.Statement subscribeActionReportsUnsubscribedOnError;

            public org.junit.runners.model.Statement subscribeAction2ReportsUnsubscribed;

            public org.junit.runners.model.Statement subscribeAction2ReportsUnsubscribedOnError;

            public org.junit.runners.model.Statement andThenSubscribeOn;

            public org.junit.runners.model.Statement andThenSingleNever;

            public org.junit.runners.model.Statement andThenSingleError;

            public org.junit.runners.model.Statement andThenSingleSubscribeOn;

            public org.junit.runners.model.Statement hookCreate;

            public org.junit.runners.model.Statement doOnCompletedNormal;

            public org.junit.runners.model.Statement doOnCompletedError;

            public org.junit.runners.model.Statement doOnCompletedThrows;

            public org.junit.runners.model.Statement doAfterTerminateNormal;

            public org.junit.runners.model.Statement doAfterTerminateWithError;

            public org.junit.runners.model.Statement subscribeEmptyOnError;

            public org.junit.runners.model.Statement subscribeOneActionOnError;

            public org.junit.runners.model.Statement propagateExceptionSubscribeEmpty;

            public org.junit.runners.model.Statement andThenCompletableNormal;

            public org.junit.runners.model.Statement andThenCompletableError;

            public org.junit.runners.model.Statement andThenFlowableNormal;

            public org.junit.runners.model.Statement andThenFlowableError;

            public org.junit.runners.model.Statement usingFactoryThrows;

            public org.junit.runners.model.Statement usingFactoryAndDisposerThrow;

            public org.junit.runners.model.Statement usingFactoryReturnsNull;

            public org.junit.runners.model.Statement subscribeReportsUnsubscribed;

            public org.junit.runners.model.Statement hookSubscribeStart;

            public org.junit.runners.model.Statement onStartCalledSafe;

            public org.junit.runners.model.Statement onErrorCompleteFunctionThrows;

            public org.junit.runners.model.Statement subscribeAction2ReportsUnsubscribedAfter;

            public org.junit.runners.model.Statement subscribeAction2ReportsUnsubscribedOnErrorAfter;

            public org.junit.runners.model.Statement propagateExceptionSubscribeOneActionThrowFromOnSuccess;

            public org.junit.runners.model.Statement andThenNever;

            public org.junit.runners.model.Statement andThenError;

            public org.junit.runners.model.Statement andThenSingle;

            public org.junit.runners.model.Statement fromFutureNormal;

            public org.junit.runners.model.Statement fromFutureThrows;

            public org.junit.runners.model.Statement fromRunnableNormal;

            public org.junit.runners.model.Statement fromRunnableThrows;

            public org.junit.runners.model.Statement doOnEventComplete;

            public org.junit.runners.model.Statement doOnEventError;

            public org.junit.runners.model.Statement subscribeTwoCallbacksDispose;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.complete = _ClassStatement.forPayload(CompletableTest::complete, "complete", this);
            this.payloads.concatEmpty = _ClassStatement.forPayload(CompletableTest::concatEmpty, "concatEmpty", this);
            this.payloads.concatSingleSource = _ClassStatement.forPayload(CompletableTest::concatSingleSource, "concatSingleSource", this);
            this.payloads.concatSingleSourceThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::concatSingleSourceThrows, io.reactivex.rxjava3.exceptions.TestException.class), "concatSingleSourceThrows", this);
            this.payloads.concatMultipleSources = _ClassStatement.forPayload(CompletableTest::concatMultipleSources, "concatMultipleSources", this);
            this.payloads.concatMultipleOneThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::concatMultipleOneThrows, io.reactivex.rxjava3.exceptions.TestException.class), "concatMultipleOneThrows", this);
            this.payloads.concatMultipleOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::concatMultipleOneIsNull, java.lang.NullPointerException.class), "concatMultipleOneIsNull", this);
            this.payloads.concatIterableEmpty = _ClassStatement.forPayload(CompletableTest::concatIterableEmpty, "concatIterableEmpty", this);
            this.payloads.concatIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::concatIterableIteratorNull, java.lang.NullPointerException.class), "concatIterableIteratorNull", this);
            this.payloads.concatIterableSingle = _ClassStatement.forPayload(CompletableTest::concatIterableSingle, "concatIterableSingle", this);
            this.payloads.concatIterableMany = _ClassStatement.forPayload(CompletableTest::concatIterableMany, "concatIterableMany", this);
            this.payloads.concatIterableOneThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::concatIterableOneThrows, io.reactivex.rxjava3.exceptions.TestException.class), "concatIterableOneThrows", this);
            this.payloads.concatIterableManyOneThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::concatIterableManyOneThrows, io.reactivex.rxjava3.exceptions.TestException.class), "concatIterableManyOneThrows", this);
            this.payloads.concatIterableIterableThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::concatIterableIterableThrows, io.reactivex.rxjava3.exceptions.TestException.class), "concatIterableIterableThrows", this);
            this.payloads.concatIterableIteratorHasNextThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::concatIterableIteratorHasNextThrows, io.reactivex.rxjava3.exceptions.TestException.class), "concatIterableIteratorHasNextThrows", this);
            this.payloads.concatIterableIteratorNextThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::concatIterableIteratorNextThrows, io.reactivex.rxjava3.exceptions.TestException.class), "concatIterableIteratorNextThrows", this);
            this.payloads.concatObservableEmpty = _ClassStatement.forPayload(CompletableTest::concatObservableEmpty, "concatObservableEmpty", this);
            this.payloads.concatObservableError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::concatObservableError, io.reactivex.rxjava3.exceptions.TestException.class), "concatObservableError", this);
            this.payloads.concatObservableSingle = _ClassStatement.forPayload(CompletableTest::concatObservableSingle, "concatObservableSingle", this);
            this.payloads.concatObservableSingleThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::concatObservableSingleThrows, io.reactivex.rxjava3.exceptions.TestException.class), "concatObservableSingleThrows", this);
            this.payloads.concatObservableMany = _ClassStatement.forPayload(CompletableTest::concatObservableMany, "concatObservableMany", this);
            this.payloads.concatObservableManyOneThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::concatObservableManyOneThrows, io.reactivex.rxjava3.exceptions.TestException.class), "concatObservableManyOneThrows", this);
            this.payloads.concatObservablePrefetch = _ClassStatement.forPayload(CompletableTest::concatObservablePrefetch, "concatObservablePrefetch", this);
            this.payloads.createOnSubscribeThrowsNPE = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::createOnSubscribeThrowsNPE, java.lang.NullPointerException.class), "createOnSubscribeThrowsNPE", this);
            this.payloads.createOnSubscribeThrowsRuntimeException = _ClassStatement.forPayload(CompletableTest::createOnSubscribeThrowsRuntimeException, "createOnSubscribeThrowsRuntimeException", this);
            this.payloads.defer = _ClassStatement.forPayload(CompletableTest::defer, "defer", this);
            this.payloads.deferReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::deferReturnsNull, java.lang.NullPointerException.class), "deferReturnsNull", this);
            this.payloads.deferFunctionThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::deferFunctionThrows, io.reactivex.rxjava3.exceptions.TestException.class), "deferFunctionThrows", this);
            this.payloads.deferErrorSource = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::deferErrorSource, io.reactivex.rxjava3.exceptions.TestException.class), "deferErrorSource", this);
            this.payloads.errorSupplierNormal = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::errorSupplierNormal, io.reactivex.rxjava3.exceptions.TestException.class), "errorSupplierNormal", this);
            this.payloads.errorSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::errorSupplierReturnsNull, java.lang.NullPointerException.class), "errorSupplierReturnsNull", this);
            this.payloads.errorSupplierThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::errorSupplierThrows, io.reactivex.rxjava3.exceptions.TestException.class), "errorSupplierThrows", this);
            this.payloads.errorNormal = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::errorNormal, io.reactivex.rxjava3.exceptions.TestException.class), "errorNormal", this);
            this.payloads.fromCallableNormal = _ClassStatement.forPayload(CompletableTest::fromCallableNormal, "fromCallableNormal", this);
            this.payloads.fromCallableThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::fromCallableThrows, io.reactivex.rxjava3.exceptions.TestException.class), "fromCallableThrows", this);
            this.payloads.fromFlowableEmpty = _ClassStatement.forPayload(CompletableTest::fromFlowableEmpty, "fromFlowableEmpty", this);
            this.payloads.fromFlowableSome = _ClassStatement.forPayload(CompletableTest::fromFlowableSome, "fromFlowableSome", this);
            this.payloads.fromFlowableError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::fromFlowableError, io.reactivex.rxjava3.exceptions.TestException.class), "fromFlowableError", this);
            this.payloads.fromObservableEmpty = _ClassStatement.forPayload(CompletableTest::fromObservableEmpty, "fromObservableEmpty", this);
            this.payloads.fromObservableSome = _ClassStatement.forPayload(CompletableTest::fromObservableSome, "fromObservableSome", this);
            this.payloads.fromObservableError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::fromObservableError, io.reactivex.rxjava3.exceptions.TestException.class), "fromObservableError", this);
            this.payloads.fromActionNormal = _ClassStatement.forPayload(CompletableTest::fromActionNormal, "fromActionNormal", this);
            this.payloads.fromActionThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::fromActionThrows, io.reactivex.rxjava3.exceptions.TestException.class), "fromActionThrows", this);
            this.payloads.fromSingleNormal = _ClassStatement.forPayload(CompletableTest::fromSingleNormal, "fromSingleNormal", this);
            this.payloads.fromSingleThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::fromSingleThrows, io.reactivex.rxjava3.exceptions.TestException.class), "fromSingleThrows", this);
            this.payloads.mergeEmpty = _ClassStatement.forPayload(CompletableTest::mergeEmpty, "mergeEmpty", this);
            this.payloads.mergeSingleSource = _ClassStatement.forPayload(CompletableTest::mergeSingleSource, "mergeSingleSource", this);
            this.payloads.mergeSingleSourceThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeSingleSourceThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeSingleSourceThrows", this);
            this.payloads.mergeMultipleSources = _ClassStatement.forPayload(CompletableTest::mergeMultipleSources, "mergeMultipleSources", this);
            this.payloads.mergeMultipleOneThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeMultipleOneThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeMultipleOneThrows", this);
            this.payloads.mergeMultipleOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeMultipleOneIsNull, java.lang.NullPointerException.class), "mergeMultipleOneIsNull", this);
            this.payloads.mergeIterableEmpty = _ClassStatement.forPayload(CompletableTest::mergeIterableEmpty, "mergeIterableEmpty", this);
            this.payloads.mergeIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeIterableIteratorNull, java.lang.NullPointerException.class), "mergeIterableIteratorNull", this);
            this.payloads.mergeIterableSingle = _ClassStatement.forPayload(CompletableTest::mergeIterableSingle, "mergeIterableSingle", this);
            this.payloads.mergeIterableMany = _ClassStatement.forPayload(CompletableTest::mergeIterableMany, "mergeIterableMany", this);
            this.payloads.mergeIterableOneThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeIterableOneThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeIterableOneThrows", this);
            this.payloads.mergeIterableManyOneThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeIterableManyOneThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeIterableManyOneThrows", this);
            this.payloads.mergeIterableIterableThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeIterableIterableThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeIterableIterableThrows", this);
            this.payloads.mergeIterableIteratorHasNextThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeIterableIteratorHasNextThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeIterableIteratorHasNextThrows", this);
            this.payloads.mergeIterableIteratorNextThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeIterableIteratorNextThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeIterableIteratorNextThrows", this);
            this.payloads.mergeObservableEmpty = _ClassStatement.forPayload(CompletableTest::mergeObservableEmpty, "mergeObservableEmpty", this);
            this.payloads.mergeObservableError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeObservableError, io.reactivex.rxjava3.exceptions.TestException.class), "mergeObservableError", this);
            this.payloads.mergeObservableSingle = _ClassStatement.forPayload(CompletableTest::mergeObservableSingle, "mergeObservableSingle", this);
            this.payloads.mergeObservableSingleThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeObservableSingleThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeObservableSingleThrows", this);
            this.payloads.mergeObservableMany = _ClassStatement.forPayload(CompletableTest::mergeObservableMany, "mergeObservableMany", this);
            this.payloads.mergeObservableManyOneThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeObservableManyOneThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeObservableManyOneThrows", this);
            this.payloads.mergeObservableMaxConcurrent = _ClassStatement.forPayload(CompletableTest::mergeObservableMaxConcurrent, "mergeObservableMaxConcurrent", this);
            this.payloads.mergeDelayErrorEmpty = _ClassStatement.forPayload(CompletableTest::mergeDelayErrorEmpty, "mergeDelayErrorEmpty", this);
            this.payloads.mergeDelayErrorSingleSource = _ClassStatement.forPayload(CompletableTest::mergeDelayErrorSingleSource, "mergeDelayErrorSingleSource", this);
            this.payloads.mergeDelayErrorSingleSourceThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeDelayErrorSingleSourceThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeDelayErrorSingleSourceThrows", this);
            this.payloads.mergeDelayErrorMultipleSources = _ClassStatement.forPayload(CompletableTest::mergeDelayErrorMultipleSources, "mergeDelayErrorMultipleSources", this);
            this.payloads.mergeDelayErrorMultipleOneThrows = _ClassStatement.forPayload(CompletableTest::mergeDelayErrorMultipleOneThrows, "mergeDelayErrorMultipleOneThrows", this);
            this.payloads.mergeDelayErrorMultipleOneIsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeDelayErrorMultipleOneIsNull, java.lang.NullPointerException.class), "mergeDelayErrorMultipleOneIsNull", this);
            this.payloads.mergeDelayErrorIterableEmpty = _ClassStatement.forPayload(CompletableTest::mergeDelayErrorIterableEmpty, "mergeDelayErrorIterableEmpty", this);
            this.payloads.mergeDelayErrorIterableIteratorNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeDelayErrorIterableIteratorNull, java.lang.NullPointerException.class), "mergeDelayErrorIterableIteratorNull", this);
            this.payloads.mergeDelayErrorIterableSingle = _ClassStatement.forPayload(CompletableTest::mergeDelayErrorIterableSingle, "mergeDelayErrorIterableSingle", this);
            this.payloads.mergeDelayErrorIterableMany = _ClassStatement.forPayload(CompletableTest::mergeDelayErrorIterableMany, "mergeDelayErrorIterableMany", this);
            this.payloads.mergeDelayErrorIterableOneThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeDelayErrorIterableOneThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeDelayErrorIterableOneThrows", this);
            this.payloads.mergeDelayErrorIterableManyOneThrows = _ClassStatement.forPayload(CompletableTest::mergeDelayErrorIterableManyOneThrows, "mergeDelayErrorIterableManyOneThrows", this);
            this.payloads.mergeDelayErrorIterableIterableThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeDelayErrorIterableIterableThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeDelayErrorIterableIterableThrows", this);
            this.payloads.mergeDelayErrorIterableIteratorHasNextThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeDelayErrorIterableIteratorHasNextThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeDelayErrorIterableIteratorHasNextThrows", this);
            this.payloads.mergeDelayErrorIterableIteratorNextThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeDelayErrorIterableIteratorNextThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeDelayErrorIterableIteratorNextThrows", this);
            this.payloads.mergeDelayErrorObservableEmpty = _ClassStatement.forPayload(CompletableTest::mergeDelayErrorObservableEmpty, "mergeDelayErrorObservableEmpty", this);
            this.payloads.mergeDelayErrorObservableError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeDelayErrorObservableError, io.reactivex.rxjava3.exceptions.TestException.class), "mergeDelayErrorObservableError", this);
            this.payloads.mergeDelayErrorObservableSingle = _ClassStatement.forPayload(CompletableTest::mergeDelayErrorObservableSingle, "mergeDelayErrorObservableSingle", this);
            this.payloads.mergeDelayErrorObservableSingleThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeDelayErrorObservableSingleThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeDelayErrorObservableSingleThrows", this);
            this.payloads.mergeDelayErrorObservableMany = _ClassStatement.forPayload(CompletableTest::mergeDelayErrorObservableMany, "mergeDelayErrorObservableMany", this);
            this.payloads.mergeDelayErrorObservableManyOneThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::mergeDelayErrorObservableManyOneThrows, io.reactivex.rxjava3.exceptions.TestException.class), "mergeDelayErrorObservableManyOneThrows", this);
            this.payloads.mergeDelayErrorObservableMaxConcurrent = _ClassStatement.forPayload(CompletableTest::mergeDelayErrorObservableMaxConcurrent, "mergeDelayErrorObservableMaxConcurrent", this);
            this.payloads.never = _ClassStatement.forPayload(CompletableTest::never, "never", this);
            this.payloads.timer = _ClassStatement.forPayload(CompletableTest::timer, "timer", this);
            this.payloads.timerNewThread = _ClassStatement.forPayload(CompletableTest::timerNewThread, "timerNewThread", this);
            this.payloads.timerTestScheduler = _ClassStatement.forPayload(CompletableTest::timerTestScheduler, "timerTestScheduler", this);
            this.payloads.timerCancel = _ClassStatement.forPayload(CompletableTest::timerCancel, "timerCancel", this);
            this.payloads.usingNormalEager = _ClassStatement.forPayload(CompletableTest::usingNormalEager, "usingNormalEager", this);
            this.payloads.usingNormalLazy = _ClassStatement.forPayload(CompletableTest::usingNormalLazy, "usingNormalLazy", this);
            this.payloads.usingErrorEager = _ClassStatement.forPayload(CompletableTest::usingErrorEager, "usingErrorEager", this);
            this.payloads.usingErrorLazy = _ClassStatement.forPayload(CompletableTest::usingErrorLazy, "usingErrorLazy", this);
            this.payloads.usingMapperReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::usingMapperReturnsNull, java.lang.NullPointerException.class), "usingMapperReturnsNull", this);
            this.payloads.usingResourceThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::usingResourceThrows, io.reactivex.rxjava3.exceptions.TestException.class), "usingResourceThrows", this);
            this.payloads.usingMapperThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::usingMapperThrows, io.reactivex.rxjava3.exceptions.TestException.class), "usingMapperThrows", this);
            this.payloads.usingDisposerThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::usingDisposerThrows, io.reactivex.rxjava3.exceptions.TestException.class), "usingDisposerThrows", this);
            this.payloads.composeNormal = _ClassStatement.forPayload(CompletableTest::composeNormal, "composeNormal", this);
            this.payloads.concatWithNormal = _ClassStatement.forPayload(CompletableTest::concatWithNormal, "concatWithNormal", this);
            this.payloads.concatWithError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::concatWithError, io.reactivex.rxjava3.exceptions.TestException.class), "concatWithError", this);
            this.payloads.delayNormal = _ClassStatement.forPayload(CompletableTest::delayNormal, "delayNormal", this);
            this.payloads.delayErrorImmediately = _ClassStatement.forPayload(CompletableTest::delayErrorImmediately, "delayErrorImmediately", this);
            this.payloads.delayErrorToo = _ClassStatement.forPayload(CompletableTest::delayErrorToo, "delayErrorToo", this);
            this.payloads.doOnCompleteNormal = _ClassStatement.forPayload(CompletableTest::doOnCompleteNormal, "doOnCompleteNormal", this);
            this.payloads.doOnCompleteError = _ClassStatement.forPayload(CompletableTest::doOnCompleteError, "doOnCompleteError", this);
            this.payloads.doOnCompleteThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::doOnCompleteThrows, io.reactivex.rxjava3.exceptions.TestException.class), "doOnCompleteThrows", this);
            this.payloads.doOnDisposeNormalDoesntCall = _ClassStatement.forPayload(CompletableTest::doOnDisposeNormalDoesntCall, "doOnDisposeNormalDoesntCall", this);
            this.payloads.doOnDisposeErrorDoesntCall = _ClassStatement.forPayload(CompletableTest::doOnDisposeErrorDoesntCall, "doOnDisposeErrorDoesntCall", this);
            this.payloads.doOnDisposeChildCancels = _ClassStatement.forPayload(CompletableTest::doOnDisposeChildCancels, "doOnDisposeChildCancels", this);
            this.payloads.doOnDisposeThrows = _ClassStatement.forPayload(CompletableTest::doOnDisposeThrows, "doOnDisposeThrows", this);
            this.payloads.doOnErrorNoError = _ClassStatement.forPayload(CompletableTest::doOnErrorNoError, "doOnErrorNoError", this);
            this.payloads.doOnErrorHasError = _ClassStatement.forPayload(CompletableTest::doOnErrorHasError, "doOnErrorHasError", this);
            this.payloads.doOnErrorThrows = _ClassStatement.forPayload(CompletableTest::doOnErrorThrows, "doOnErrorThrows", this);
            this.payloads.doOnSubscribeNormal = _ClassStatement.forPayload(CompletableTest::doOnSubscribeNormal, "doOnSubscribeNormal", this);
            this.payloads.doOnSubscribeThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::doOnSubscribeThrows, io.reactivex.rxjava3.exceptions.TestException.class), "doOnSubscribeThrows", this);
            this.payloads.doOnTerminateNormal = _ClassStatement.forPayload(CompletableTest::doOnTerminateNormal, "doOnTerminateNormal", this);
            this.payloads.doOnTerminateError = _ClassStatement.forPayload(CompletableTest::doOnTerminateError, "doOnTerminateError", this);
            this.payloads.liftReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::liftReturnsNull, java.lang.NullPointerException.class), "liftReturnsNull", this);
            this.payloads.liftOnCompleteError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::liftOnCompleteError, io.reactivex.rxjava3.exceptions.TestException.class), "liftOnCompleteError", this);
            this.payloads.liftOnErrorComplete = _ClassStatement.forPayload(CompletableTest::liftOnErrorComplete, "liftOnErrorComplete", this);
            this.payloads.mergeWithNormal = _ClassStatement.forPayload(CompletableTest::mergeWithNormal, "mergeWithNormal", this);
            this.payloads.observeOnNormal = _ClassStatement.forPayload(CompletableTest::observeOnNormal, "observeOnNormal", this);
            this.payloads.observeOnError = _ClassStatement.forPayload(CompletableTest::observeOnError, "observeOnError", this);
            this.payloads.onErrorComplete = _ClassStatement.forPayload(CompletableTest::onErrorComplete, "onErrorComplete", this);
            this.payloads.onErrorCompleteFalse = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::onErrorCompleteFalse, io.reactivex.rxjava3.exceptions.TestException.class), "onErrorCompleteFalse", this);
            this.payloads.onErrorResumeNextFunctionReturnsNull = _ClassStatement.forPayload(CompletableTest::onErrorResumeNextFunctionReturnsNull, "onErrorResumeNextFunctionReturnsNull", this);
            this.payloads.onErrorResumeNextFunctionThrows = _ClassStatement.forPayload(CompletableTest::onErrorResumeNextFunctionThrows, "onErrorResumeNextFunctionThrows", this);
            this.payloads.onErrorResumeNextNormal = _ClassStatement.forPayload(CompletableTest::onErrorResumeNextNormal, "onErrorResumeNextNormal", this);
            this.payloads.onErrorResumeNextError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::onErrorResumeNextError, io.reactivex.rxjava3.exceptions.TestException.class), "onErrorResumeNextError", this);
            this.payloads.repeatNormal = _ClassStatement.forPayload(CompletableTest::repeatNormal, "repeatNormal", this);
            this.payloads.repeatError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::repeatError, io.reactivex.rxjava3.exceptions.TestException.class), "repeatError", this);
            this.payloads.repeat5Times = _ClassStatement.forPayload(CompletableTest::repeat5Times, "repeat5Times", this);
            this.payloads.repeat1Time = _ClassStatement.forPayload(CompletableTest::repeat1Time, "repeat1Time", this);
            this.payloads.repeat0Time = _ClassStatement.forPayload(CompletableTest::repeat0Time, "repeat0Time", this);
            this.payloads.repeatUntilNormal = _ClassStatement.forPayload(CompletableTest::repeatUntilNormal, "repeatUntilNormal", this);
            this.payloads.retryNormal = _ClassStatement.forPayload(CompletableTest::retryNormal, "retryNormal", this);
            this.payloads.retry5Times = _ClassStatement.forPayload(CompletableTest::retry5Times, "retry5Times", this);
            this.payloads.retryBiPredicate5Times = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::retryBiPredicate5Times, io.reactivex.rxjava3.exceptions.TestException.class), "retryBiPredicate5Times", this);
            this.payloads.retryTimes5Error = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::retryTimes5Error, io.reactivex.rxjava3.exceptions.TestException.class), "retryTimes5Error", this);
            this.payloads.retryTimes5Normal = _ClassStatement.forPayload(CompletableTest::retryTimes5Normal, "retryTimes5Normal", this);
            this.payloads.retryNegativeTimes = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::retryNegativeTimes, java.lang.IllegalArgumentException.class), "retryNegativeTimes", this);
            this.payloads.retryPredicateError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::retryPredicateError, io.reactivex.rxjava3.exceptions.TestException.class), "retryPredicateError", this);
            this.payloads.retryPredicate5Times = _ClassStatement.forPayload(CompletableTest::retryPredicate5Times, "retryPredicate5Times", this);
            this.payloads.retryWhen5Times = _ClassStatement.forPayload(CompletableTest::retryWhen5Times, "retryWhen5Times", this);
            this.payloads.subscribe = _ClassStatement.forPayload(CompletableTest::subscribe, "subscribe", this);
            this.payloads.subscribeDispose = _ClassStatement.forPayload(CompletableTest::subscribeDispose, "subscribeDispose", this);
            this.payloads.subscribeTwoCallbacksNormal = _ClassStatement.forPayload(CompletableTest::subscribeTwoCallbacksNormal, "subscribeTwoCallbacksNormal", this);
            this.payloads.subscribeTwoCallbacksError = _ClassStatement.forPayload(CompletableTest::subscribeTwoCallbacksError, "subscribeTwoCallbacksError", this);
            this.payloads.subscribeTwoCallbacksCompleteThrows = _ClassStatement.forPayload(CompletableTest::subscribeTwoCallbacksCompleteThrows, "subscribeTwoCallbacksCompleteThrows", this);
            this.payloads.subscribeTwoCallbacksOnErrorThrows = _ClassStatement.forPayload(CompletableTest::subscribeTwoCallbacksOnErrorThrows, "subscribeTwoCallbacksOnErrorThrows", this);
            this.payloads.subscribeObserverNormal = _ClassStatement.forPayload(CompletableTest::subscribeObserverNormal, "subscribeObserverNormal", this);
            this.payloads.subscribeObserverError = _ClassStatement.forPayload(CompletableTest::subscribeObserverError, "subscribeObserverError", this);
            this.payloads.subscribeActionNormal = _ClassStatement.forPayload(CompletableTest::subscribeActionNormal, "subscribeActionNormal", this);
            this.payloads.subscribeActionError = _ClassStatement.forPayload(CompletableTest::subscribeActionError, "subscribeActionError", this);
            this.payloads.subscribeSubscriberNormal = _ClassStatement.forPayload(CompletableTest::subscribeSubscriberNormal, "subscribeSubscriberNormal", this);
            this.payloads.subscribeSubscriberError = _ClassStatement.forPayload(CompletableTest::subscribeSubscriberError, "subscribeSubscriberError", this);
            this.payloads.subscribeOnNormal = _ClassStatement.forPayload(CompletableTest::subscribeOnNormal, "subscribeOnNormal", this);
            this.payloads.subscribeOnError = _ClassStatement.forPayload(CompletableTest::subscribeOnError, "subscribeOnError", this);
            this.payloads.timeoutSwitchNormal = _ClassStatement.forPayload(CompletableTest::timeoutSwitchNormal, "timeoutSwitchNormal", this);
            this.payloads.timeoutTimerCancelled = _ClassStatement.forPayload(CompletableTest::timeoutTimerCancelled, "timeoutTimerCancelled", this);
            this.payloads.toNormal = _ClassStatement.forPayload(CompletableTest::toNormal, "toNormal", this);
            this.payloads.asNormal = _ClassStatement.forPayload(CompletableTest::asNormal, "asNormal", this);
            this.payloads.as = _ClassStatement.forPayload(CompletableTest::as, "as", this);
            this.payloads.toFlowableNormal = _ClassStatement.forPayload(CompletableTest::toFlowableNormal, "toFlowableNormal", this);
            this.payloads.toFlowableError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::toFlowableError, io.reactivex.rxjava3.exceptions.TestException.class), "toFlowableError", this);
            this.payloads.toObservableNormal = _ClassStatement.forPayload(CompletableTest::toObservableNormal, "toObservableNormal", this);
            this.payloads.toObservableError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::toObservableError, io.reactivex.rxjava3.exceptions.TestException.class), "toObservableError", this);
            this.payloads.toSingleSupplierNormal = _ClassStatement.forPayload(CompletableTest::toSingleSupplierNormal, "toSingleSupplierNormal", this);
            this.payloads.toSingleSupplierError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::toSingleSupplierError, io.reactivex.rxjava3.exceptions.TestException.class), "toSingleSupplierError", this);
            this.payloads.toSingleSupplierReturnsNull = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::toSingleSupplierReturnsNull, java.lang.NullPointerException.class), "toSingleSupplierReturnsNull", this);
            this.payloads.toSingleSupplierThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::toSingleSupplierThrows, io.reactivex.rxjava3.exceptions.TestException.class), "toSingleSupplierThrows", this);
            this.payloads.toSingleDefaultError = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::toSingleDefaultError, io.reactivex.rxjava3.exceptions.TestException.class), "toSingleDefaultError", this);
            this.payloads.toSingleDefaultNormal = _ClassStatement.forPayload(CompletableTest::toSingleDefaultNormal, "toSingleDefaultNormal", this);
            this.payloads.unsubscribeOnNormal = _ClassStatement.forPayload(CompletableTest::unsubscribeOnNormal, "unsubscribeOnNormal", this);
            this.payloads.ambArrayEmpty = _ClassStatement.forPayload(CompletableTest::ambArrayEmpty, "ambArrayEmpty", this);
            this.payloads.ambArraySingleNormal = _ClassStatement.forPayload(CompletableTest::ambArraySingleNormal, "ambArraySingleNormal", this);
            this.payloads.ambArraySingleError = _ClassStatement.forPayload(CompletableTest::ambArraySingleError, "ambArraySingleError", this);
            this.payloads.ambArrayOneFires = _ClassStatement.forPayload(CompletableTest::ambArrayOneFires, "ambArrayOneFires", this);
            this.payloads.ambArrayOneFiresError = _ClassStatement.forPayload(CompletableTest::ambArrayOneFiresError, "ambArrayOneFiresError", this);
            this.payloads.ambArraySecondFires = _ClassStatement.forPayload(CompletableTest::ambArraySecondFires, "ambArraySecondFires", this);
            this.payloads.ambArraySecondFiresError = _ClassStatement.forPayload(CompletableTest::ambArraySecondFiresError, "ambArraySecondFiresError", this);
            this.payloads.ambMultipleOneIsNull = _ClassStatement.forPayload(CompletableTest::ambMultipleOneIsNull, "ambMultipleOneIsNull", this);
            this.payloads.ambIterableEmpty = _ClassStatement.forPayload(CompletableTest::ambIterableEmpty, "ambIterableEmpty", this);
            this.payloads.ambIterableIteratorNull = _ClassStatement.forPayload(CompletableTest::ambIterableIteratorNull, "ambIterableIteratorNull", this);
            this.payloads.ambIterableWithNull = _ClassStatement.forPayload(CompletableTest::ambIterableWithNull, "ambIterableWithNull", this);
            this.payloads.ambIterableSingle = _ClassStatement.forPayload(CompletableTest::ambIterableSingle, "ambIterableSingle", this);
            this.payloads.ambIterableMany = _ClassStatement.forPayload(CompletableTest::ambIterableMany, "ambIterableMany", this);
            this.payloads.ambIterableOneThrows = _ClassStatement.forPayload(CompletableTest::ambIterableOneThrows, "ambIterableOneThrows", this);
            this.payloads.ambIterableManyOneThrows = _ClassStatement.forPayload(CompletableTest::ambIterableManyOneThrows, "ambIterableManyOneThrows", this);
            this.payloads.ambIterableIterableThrows = _ClassStatement.forPayload(CompletableTest::ambIterableIterableThrows, "ambIterableIterableThrows", this);
            this.payloads.ambIterableIteratorHasNextThrows = _ClassStatement.forPayload(CompletableTest::ambIterableIteratorHasNextThrows, "ambIterableIteratorHasNextThrows", this);
            this.payloads.ambIterableIteratorNextThrows = _ClassStatement.forPayload(CompletableTest::ambIterableIteratorNextThrows, "ambIterableIteratorNextThrows", this);
            this.payloads.ambWithArrayOneFires = _ClassStatement.forPayload(CompletableTest::ambWithArrayOneFires, "ambWithArrayOneFires", this);
            this.payloads.ambWithArrayOneFiresError = _ClassStatement.forPayload(CompletableTest::ambWithArrayOneFiresError, "ambWithArrayOneFiresError", this);
            this.payloads.ambWithArraySecondFires = _ClassStatement.forPayload(CompletableTest::ambWithArraySecondFires, "ambWithArraySecondFires", this);
            this.payloads.ambWithArraySecondFiresError = _ClassStatement.forPayload(CompletableTest::ambWithArraySecondFiresError, "ambWithArraySecondFiresError", this);
            this.payloads.startWithCompletableNormal = _ClassStatement.forPayload(CompletableTest::startWithCompletableNormal, "startWithCompletableNormal", this);
            this.payloads.startWithCompletableError = _ClassStatement.forPayload(CompletableTest::startWithCompletableError, "startWithCompletableError", this);
            this.payloads.startWithFlowableNormal = _ClassStatement.forPayload(CompletableTest::startWithFlowableNormal, "startWithFlowableNormal", this);
            this.payloads.startWithFlowableError = _ClassStatement.forPayload(CompletableTest::startWithFlowableError, "startWithFlowableError", this);
            this.payloads.startWithObservableNormal = _ClassStatement.forPayload(CompletableTest::startWithObservableNormal, "startWithObservableNormal", this);
            this.payloads.startWithObservableError = _ClassStatement.forPayload(CompletableTest::startWithObservableError, "startWithObservableError", this);
            this.payloads.andThen = _ClassStatement.forPayload(CompletableTest::andThen, "andThen", this);
            this.payloads.subscribeOneActionThrowFromOnCompleted = _ClassStatement.forPayload(CompletableTest::subscribeOneActionThrowFromOnCompleted, "subscribeOneActionThrowFromOnCompleted", this);
            this.payloads.subscribeTwoActionsThrowFromOnError = _ClassStatement.forPayload(CompletableTest::subscribeTwoActionsThrowFromOnError, "subscribeTwoActionsThrowFromOnError", this);
            this.payloads.propagateExceptionSubscribeOneAction = _ClassStatement.forPayload(CompletableTest::propagateExceptionSubscribeOneAction, "propagateExceptionSubscribeOneAction", this);
            this.payloads.usingFactoryReturnsNullAndDisposerThrows = _ClassStatement.forPayload(CompletableTest::usingFactoryReturnsNullAndDisposerThrows, "usingFactoryReturnsNullAndDisposerThrows", this);
            this.payloads.subscribeReportsUnsubscribedOnError = _ClassStatement.forPayload(CompletableTest::subscribeReportsUnsubscribedOnError, "subscribeReportsUnsubscribedOnError", this);
            this.payloads.subscribeActionReportsUnsubscribed = _ClassStatement.forPayload(CompletableTest::subscribeActionReportsUnsubscribed, "subscribeActionReportsUnsubscribed", this);
            this.payloads.subscribeActionReportsUnsubscribedAfter = _ClassStatement.forPayload(CompletableTest::subscribeActionReportsUnsubscribedAfter, "subscribeActionReportsUnsubscribedAfter", this);
            this.payloads.subscribeActionReportsUnsubscribedOnError = _ClassStatement.forPayload(CompletableTest::subscribeActionReportsUnsubscribedOnError, "subscribeActionReportsUnsubscribedOnError", this);
            this.payloads.subscribeAction2ReportsUnsubscribed = _ClassStatement.forPayload(CompletableTest::subscribeAction2ReportsUnsubscribed, "subscribeAction2ReportsUnsubscribed", this);
            this.payloads.subscribeAction2ReportsUnsubscribedOnError = _ClassStatement.forPayload(CompletableTest::subscribeAction2ReportsUnsubscribedOnError, "subscribeAction2ReportsUnsubscribedOnError", this);
            this.payloads.andThenSubscribeOn = _ClassStatement.forPayload(CompletableTest::andThenSubscribeOn, "andThenSubscribeOn", this);
            this.payloads.andThenSingleNever = _ClassStatement.forPayload(CompletableTest::andThenSingleNever, "andThenSingleNever", this);
            this.payloads.andThenSingleError = _ClassStatement.forPayload(CompletableTest::andThenSingleError, "andThenSingleError", this);
            this.payloads.andThenSingleSubscribeOn = _ClassStatement.forPayload(CompletableTest::andThenSingleSubscribeOn, "andThenSingleSubscribeOn", this);
            this.payloads.hookCreate = _ClassStatement.forPayload(CompletableTest::hookCreate, "hookCreate", this);
            this.payloads.doOnCompletedNormal = _ClassStatement.forPayload(CompletableTest::doOnCompletedNormal, "doOnCompletedNormal", this);
            this.payloads.doOnCompletedError = _ClassStatement.forPayload(CompletableTest::doOnCompletedError, "doOnCompletedError", this);
            this.payloads.doOnCompletedThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::doOnCompletedThrows, io.reactivex.rxjava3.exceptions.TestException.class), "doOnCompletedThrows", this);
            this.payloads.doAfterTerminateNormal = _ClassStatement.forPayload(CompletableTest::doAfterTerminateNormal, "doAfterTerminateNormal", this);
            this.payloads.doAfterTerminateWithError = _ClassStatement.forPayload(CompletableTest::doAfterTerminateWithError, "doAfterTerminateWithError", this);
            this.payloads.subscribeEmptyOnError = _ClassStatement.forPayload(CompletableTest::subscribeEmptyOnError, "subscribeEmptyOnError", this);
            this.payloads.subscribeOneActionOnError = _ClassStatement.forPayload(CompletableTest::subscribeOneActionOnError, "subscribeOneActionOnError", this);
            this.payloads.propagateExceptionSubscribeEmpty = _ClassStatement.forPayload(CompletableTest::propagateExceptionSubscribeEmpty, "propagateExceptionSubscribeEmpty", this);
            this.payloads.andThenCompletableNormal = _ClassStatement.forPayload(CompletableTest::andThenCompletableNormal, "andThenCompletableNormal", this);
            this.payloads.andThenCompletableError = _ClassStatement.forPayload(CompletableTest::andThenCompletableError, "andThenCompletableError", this);
            this.payloads.andThenFlowableNormal = _ClassStatement.forPayload(CompletableTest::andThenFlowableNormal, "andThenFlowableNormal", this);
            this.payloads.andThenFlowableError = _ClassStatement.forPayload(CompletableTest::andThenFlowableError, "andThenFlowableError", this);
            this.payloads.usingFactoryThrows = _ClassStatement.forPayload(CompletableTest::usingFactoryThrows, "usingFactoryThrows", this);
            this.payloads.usingFactoryAndDisposerThrow = _ClassStatement.forPayload(CompletableTest::usingFactoryAndDisposerThrow, "usingFactoryAndDisposerThrow", this);
            this.payloads.usingFactoryReturnsNull = _ClassStatement.forPayload(CompletableTest::usingFactoryReturnsNull, "usingFactoryReturnsNull", this);
            this.payloads.subscribeReportsUnsubscribed = _ClassStatement.forPayload(CompletableTest::subscribeReportsUnsubscribed, "subscribeReportsUnsubscribed", this);
            this.payloads.hookSubscribeStart = _ClassStatement.forPayload(CompletableTest::hookSubscribeStart, "hookSubscribeStart", this);
            this.payloads.onStartCalledSafe = _ClassStatement.forPayload(CompletableTest::onStartCalledSafe, "onStartCalledSafe", this);
            this.payloads.onErrorCompleteFunctionThrows = _ClassStatement.forPayload(CompletableTest::onErrorCompleteFunctionThrows, "onErrorCompleteFunctionThrows", this);
            this.payloads.subscribeAction2ReportsUnsubscribedAfter = _ClassStatement.forPayload(CompletableTest::subscribeAction2ReportsUnsubscribedAfter, "subscribeAction2ReportsUnsubscribedAfter", this);
            this.payloads.subscribeAction2ReportsUnsubscribedOnErrorAfter = _ClassStatement.forPayload(CompletableTest::subscribeAction2ReportsUnsubscribedOnErrorAfter, "subscribeAction2ReportsUnsubscribedOnErrorAfter", this);
            this.payloads.propagateExceptionSubscribeOneActionThrowFromOnSuccess = _ClassStatement.forPayload(CompletableTest::propagateExceptionSubscribeOneActionThrowFromOnSuccess, "propagateExceptionSubscribeOneActionThrowFromOnSuccess", this);
            this.payloads.andThenNever = _ClassStatement.forPayload(CompletableTest::andThenNever, "andThenNever", this);
            this.payloads.andThenError = _ClassStatement.forPayload(CompletableTest::andThenError, "andThenError", this);
            this.payloads.andThenSingle = _ClassStatement.forPayload(CompletableTest::andThenSingle, "andThenSingle", this);
            this.payloads.fromFutureNormal = _ClassStatement.forPayload(CompletableTest::fromFutureNormal, "fromFutureNormal", this);
            this.payloads.fromFutureThrows = _ClassStatement.forPayload(CompletableTest::fromFutureThrows, "fromFutureThrows", this);
            this.payloads.fromRunnableNormal = _ClassStatement.forPayload(CompletableTest::fromRunnableNormal, "fromRunnableNormal", this);
            this.payloads.fromRunnableThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(CompletableTest::fromRunnableThrows, io.reactivex.rxjava3.exceptions.TestException.class), "fromRunnableThrows", this);
            this.payloads.doOnEventComplete = _ClassStatement.forPayload(CompletableTest::doOnEventComplete, "doOnEventComplete", this);
            this.payloads.doOnEventError = _ClassStatement.forPayload(CompletableTest::doOnEventError, "doOnEventError", this);
            this.payloads.subscribeTwoCallbacksDispose = _ClassStatement.forPayload(CompletableTest::subscribeTwoCallbacksDispose, "subscribeTwoCallbacksDispose", this);
        }
    }
}
