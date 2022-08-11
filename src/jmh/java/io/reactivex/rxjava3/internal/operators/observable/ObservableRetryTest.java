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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import org.mockito.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.subscriptions.BooleanSubscription;
import io.reactivex.rxjava3.observables.GroupedObservable;
import io.reactivex.rxjava3.observers.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableRetryTest extends RxJavaTest {

    @Test
    public void iterativeBackoff() {
        Observer<String> consumer = TestHelper.mockObserver();
        Observable<String> producer = Observable.unsafeCreate(new ObservableSource<String>() {

            private AtomicInteger count = new AtomicInteger(4);

            long last = System.currentTimeMillis();

            @Override
            public void subscribe(Observer<? super String> t1) {
                t1.onSubscribe(Disposable.empty());
                System.out.println(count.get() + " @ " + String.valueOf(last - System.currentTimeMillis()));
                last = System.currentTimeMillis();
                if (count.getAndDecrement() == 0) {
                    t1.onNext("hello");
                    t1.onComplete();
                } else {
                    t1.onError(new RuntimeException());
                }
            }
        });
        TestObserver<String> to = new TestObserver<>(consumer);
        producer.retryWhen(new Function<Observable<? extends Throwable>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<? extends Throwable> attempts) {
                // Worker w = Schedulers.computation().createWorker();
                return attempts.map(new Function<Throwable, Tuple>() {

                    @Override
                    public Tuple apply(Throwable n) {
                        return new Tuple(1L, n);
                    }
                }).scan(new BiFunction<Tuple, Tuple, Tuple>() {

                    @Override
                    public Tuple apply(Tuple t, Tuple n) {
                        return new Tuple(t.count + n.count, n.n);
                    }
                }).flatMap(new Function<Tuple, Observable<Long>>() {

                    @Override
                    public Observable<Long> apply(Tuple t) {
                        System.out.println("Retry # " + t.count);
                        return t.count > 20 ? Observable.<Long>error(t.n) : Observable.timer(t.count * 1L, TimeUnit.MILLISECONDS);
                    }
                }).cast(Object.class);
            }
        }).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertNoErrors();
        InOrder inOrder = inOrder(consumer);
        inOrder.verify(consumer, never()).onError(any(Throwable.class));
        inOrder.verify(consumer, times(1)).onNext("hello");
        inOrder.verify(consumer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    public static class Tuple {

        Long count;

        Throwable n;

        Tuple(Long c, Throwable n) {
            count = c;
            this.n = n;
        }
    }

    @Test
    public void retryIndefinitely() {
        Observer<String> observer = TestHelper.mockObserver();
        int numRetries = 20;
        Observable<String> origin = Observable.unsafeCreate(new FuncWithErrors(numRetries));
        origin.retry().subscribe(new TestObserver<>(observer));
        InOrder inOrder = inOrder(observer);
        // should show 3 attempts
        inOrder.verify(observer, times(numRetries + 1)).onNext("beginningEveryTime");
        // should have no errors
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        // should have a single success
        inOrder.verify(observer, times(1)).onNext("onSuccessOnly");
        // should have a single successful onComplete
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void schedulingNotificationHandler() {
        Observer<String> observer = TestHelper.mockObserver();
        int numRetries = 2;
        Observable<String> origin = Observable.unsafeCreate(new FuncWithErrors(numRetries));
        TestObserver<String> to = new TestObserver<>(observer);
        origin.retryWhen(new Function<Observable<? extends Throwable>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<? extends Throwable> t1) {
                return t1.observeOn(Schedulers.computation()).map(new Function<Throwable, Object>() {

                    @Override
                    public Object apply(Throwable t1) {
                        return 1;
                    }
                }).startWithItem(1);
            }
        }).doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) {
                e.printStackTrace();
            }
        }).subscribe(to);
        to.awaitDone(5, TimeUnit.SECONDS);
        InOrder inOrder = inOrder(observer);
        // should show 3 attempts
        inOrder.verify(observer, times(1 + numRetries)).onNext("beginningEveryTime");
        // should have no errors
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        // should have a single success
        inOrder.verify(observer, times(1)).onNext("onSuccessOnly");
        // should have a single successful onComplete
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void onNextFromNotificationHandler() {
        Observer<String> observer = TestHelper.mockObserver();
        int numRetries = 2;
        Observable<String> origin = Observable.unsafeCreate(new FuncWithErrors(numRetries));
        origin.retryWhen(new Function<Observable<? extends Throwable>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Observable<? extends Throwable> t1) {
                return t1.map(new Function<Throwable, Integer>() {

                    @Override
                    public Integer apply(Throwable t1) {
                        return 0;
                    }
                }).startWithItem(0).cast(Object.class);
            }
        }).subscribe(observer);
        InOrder inOrder = inOrder(observer);
        // should show 3 attempts
        inOrder.verify(observer, times(numRetries + 1)).onNext("beginningEveryTime");
        // should have no errors
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        // should have a single success
        inOrder.verify(observer, times(1)).onNext("onSuccessOnly");
        // should have a single successful onComplete
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void onCompletedFromNotificationHandler() {
        Observer<String> observer = TestHelper.mockObserver();
        Observable<String> origin = Observable.unsafeCreate(new FuncWithErrors(1));
        TestObserver<String> to = new TestObserver<>(observer);
        origin.retryWhen(new Function<Observable<? extends Throwable>, Observable<?>>() {

            @Override
            public Observable<?> apply(Observable<? extends Throwable> t1) {
                return Observable.empty();
            }
        }).subscribe(to);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onSubscribe((Disposable) notNull());
        inOrder.verify(observer, never()).onNext("beginningEveryTime");
        inOrder.verify(observer, never()).onNext("onSuccessOnly");
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verify(observer, never()).onError(any(Exception.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void onErrorFromNotificationHandler() {
        Observer<String> observer = TestHelper.mockObserver();
        Observable<String> origin = Observable.unsafeCreate(new FuncWithErrors(2));
        origin.retryWhen(new Function<Observable<? extends Throwable>, Observable<?>>() {

            @Override
            public Observable<?> apply(Observable<? extends Throwable> t1) {
                return Observable.error(new RuntimeException());
            }
        }).subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onSubscribe((Disposable) notNull());
        inOrder.verify(observer, never()).onNext("beginningEveryTime");
        inOrder.verify(observer, never()).onNext("onSuccessOnly");
        inOrder.verify(observer, never()).onComplete();
        inOrder.verify(observer, times(1)).onError(any(RuntimeException.class));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleSubscriptionOnFirst() throws Exception {
        final AtomicInteger inc = new AtomicInteger(0);
        ObservableSource<Integer> onSubscribe = new ObservableSource<Integer>() {

            @Override
            public void subscribe(Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                final int emit = inc.incrementAndGet();
                observer.onNext(emit);
                observer.onComplete();
            }
        };
        int first = Observable.unsafeCreate(onSubscribe).retryWhen(new Function<Observable<? extends Throwable>, Observable<?>>() {

            @Override
            public Observable<?> apply(Observable<? extends Throwable> attempt) {
                return attempt.zipWith(Observable.just(1), new BiFunction<Throwable, Integer, Void>() {

                    @Override
                    public Void apply(Throwable o, Integer integer) {
                        return null;
                    }
                });
            }
        }).blockingFirst();
        assertEquals("Observer did not receive the expected output", 1, first);
        assertEquals("Subscribe was not called once", 1, inc.get());
    }

    @Test
    public void originFails() {
        Observer<String> observer = TestHelper.mockObserver();
        Observable<String> origin = Observable.unsafeCreate(new FuncWithErrors(1));
        origin.subscribe(observer);
        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, times(1)).onNext("beginningEveryTime");
        inOrder.verify(observer, times(1)).onError(any(RuntimeException.class));
        inOrder.verify(observer, never()).onNext("onSuccessOnly");
        inOrder.verify(observer, never()).onComplete();
    }

    @Test
    public void retryFail() {
        int numRetries = 1;
        int numFailures = 2;
        Observer<String> observer = TestHelper.mockObserver();
        Observable<String> origin = Observable.unsafeCreate(new FuncWithErrors(numFailures));
        origin.retry(numRetries).subscribe(observer);
        InOrder inOrder = inOrder(observer);
        // should show 2 attempts (first time fail, second time (1st retry) fail)
        inOrder.verify(observer, times(1 + numRetries)).onNext("beginningEveryTime");
        // should only retry once, fail again and emit onError
        inOrder.verify(observer, times(1)).onError(any(RuntimeException.class));
        // no success
        inOrder.verify(observer, never()).onNext("onSuccessOnly");
        inOrder.verify(observer, never()).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void retrySuccess() {
        int numFailures = 1;
        Observer<String> observer = TestHelper.mockObserver();
        Observable<String> origin = Observable.unsafeCreate(new FuncWithErrors(numFailures));
        origin.retry(3).subscribe(observer);
        InOrder inOrder = inOrder(observer);
        // should show 3 attempts
        inOrder.verify(observer, times(1 + numFailures)).onNext("beginningEveryTime");
        // should have no errors
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        // should have a single success
        inOrder.verify(observer, times(1)).onNext("onSuccessOnly");
        // should have a single successful onComplete
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void infiniteRetry() {
        int numFailures = 20;
        Observer<String> observer = TestHelper.mockObserver();
        Observable<String> origin = Observable.unsafeCreate(new FuncWithErrors(numFailures));
        origin.retry().subscribe(observer);
        InOrder inOrder = inOrder(observer);
        // should show 3 attempts
        inOrder.verify(observer, times(1 + numFailures)).onNext("beginningEveryTime");
        // should have no errors
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        // should have a single success
        inOrder.verify(observer, times(1)).onNext("onSuccessOnly");
        // should have a single successful onComplete
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    /*
     * Checks in a simple and synchronous way that retry resubscribes
     * after error. This test fails against 0.16.1-0.17.4, hangs on 0.17.5 and
     * passes in 0.17.6 thanks to fix for issue #1027.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void retrySubscribesAgainAfterError() throws Throwable {
        // record emitted values with this action
        Consumer<Integer> record = mock(Consumer.class);
        InOrder inOrder = inOrder(record);
        // always throw an exception with this action
        Consumer<Integer> throwException = mock(Consumer.class);
        doThrow(new RuntimeException()).when(throwException).accept(Mockito.anyInt());
        // create a retrying Observable based on a PublishSubject
        PublishSubject<Integer> subject = PublishSubject.create();
        subject.doOnNext(record).doOnNext(throwException).retry().subscribe();
        inOrder.verifyNoMoreInteractions();
        subject.onNext(1);
        inOrder.verify(record).accept(1);
        subject.onNext(2);
        inOrder.verify(record).accept(2);
        subject.onNext(3);
        inOrder.verify(record).accept(3);
        inOrder.verifyNoMoreInteractions();
    }

    public static class FuncWithErrors implements ObservableSource<String> {

        private final int numFailures;

        private final AtomicInteger count = new AtomicInteger(0);

        FuncWithErrors(int count) {
            this.numFailures = count;
        }

        @Override
        public void subscribe(final Observer<? super String> o) {
            o.onSubscribe(Disposable.empty());
            o.onNext("beginningEveryTime");
            int i = count.getAndIncrement();
            if (i < numFailures) {
                o.onError(new RuntimeException("forced failure: " + (i + 1)));
            } else {
                o.onNext("onSuccessOnly");
                o.onComplete();
            }
        }
    }

    @Test
    public void unsubscribeFromRetry() {
        PublishSubject<Integer> subject = PublishSubject.create();
        final AtomicInteger count = new AtomicInteger(0);
        Disposable sub = subject.retry().subscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer n) {
                count.incrementAndGet();
            }
        });
        subject.onNext(1);
        sub.dispose();
        subject.onNext(2);
        assertEquals(1, count.get());
    }

    @Test
    public void retryAllowsSubscriptionAfterAllSubscriptionsUnsubscribed() throws InterruptedException {
        final AtomicInteger subsCount = new AtomicInteger(0);
        ObservableSource<String> onSubscribe = new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> observer) {
                subsCount.incrementAndGet();
                observer.onSubscribe(Disposable.fromRunnable(new Runnable() {

                    @Override
                    public void run() {
                        subsCount.decrementAndGet();
                    }
                }));
            }
        };
        Observable<String> stream = Observable.unsafeCreate(onSubscribe);
        Observable<String> streamWithRetry = stream.retry();
        Disposable sub = streamWithRetry.subscribe();
        assertEquals(1, subsCount.get());
        sub.dispose();
        assertEquals(0, subsCount.get());
        streamWithRetry.subscribe();
        assertEquals(1, subsCount.get());
    }

    @Test
    public void sourceObservableCallsUnsubscribe() throws InterruptedException {
        final AtomicInteger subsCount = new AtomicInteger(0);
        final TestObserver<String> to = new TestObserver<>();
        ObservableSource<String> onSubscribe = new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> observer) {
                BooleanSubscription bs = new BooleanSubscription();
                // if isUnsubscribed is true that means we have a bug such as
                // https://github.com/ReactiveX/RxJava/issues/1024
                if (!bs.isCancelled()) {
                    subsCount.incrementAndGet();
                    observer.onError(new RuntimeException("failed"));
                    // it unsubscribes the child directly
                    // this simulates various error/completion scenarios that could occur
                    // or just a source that proactively triggers cleanup
                    // FIXME can't unsubscribe child
                    // s.unsubscribe();
                    bs.cancel();
                } else {
                    observer.onError(new RuntimeException());
                }
            }
        };
        Observable.unsafeCreate(onSubscribe).retry(3).subscribe(to);
        // 1 + 3 retries
        assertEquals(4, subsCount.get());
    }

    @Test
    public void sourceObservableRetry1() throws InterruptedException {
        final AtomicInteger subsCount = new AtomicInteger(0);
        final TestObserver<String> to = new TestObserver<>();
        ObservableSource<String> onSubscribe = new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> observer) {
                observer.onSubscribe(Disposable.empty());
                subsCount.incrementAndGet();
                observer.onError(new RuntimeException("failed"));
            }
        };
        Observable.unsafeCreate(onSubscribe).retry(1).subscribe(to);
        assertEquals(2, subsCount.get());
    }

    @Test
    public void sourceObservableRetry0() throws InterruptedException {
        final AtomicInteger subsCount = new AtomicInteger(0);
        final TestObserver<String> to = new TestObserver<>();
        ObservableSource<String> onSubscribe = new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> observer) {
                observer.onSubscribe(Disposable.empty());
                subsCount.incrementAndGet();
                observer.onError(new RuntimeException("failed"));
            }
        };
        Observable.unsafeCreate(onSubscribe).retry(0).subscribe(to);
        assertEquals(1, subsCount.get());
    }

    static final class SlowObservable implements ObservableSource<Long> {

        final AtomicInteger efforts = new AtomicInteger(0);

        final AtomicInteger active = new AtomicInteger(0), maxActive = new AtomicInteger(0);

        final AtomicInteger nextBeforeFailure;

        final String context;

        private final int emitDelay;

        SlowObservable(int emitDelay, int countNext, String context) {
            this.emitDelay = emitDelay;
            this.nextBeforeFailure = new AtomicInteger(countNext);
            this.context = context;
        }

        @Override
        public void subscribe(final Observer<? super Long> observer) {
            final AtomicBoolean terminate = new AtomicBoolean(false);
            observer.onSubscribe(Disposable.fromRunnable(new Runnable() {

                @Override
                public void run() {
                    terminate.set(true);
                    active.decrementAndGet();
                }
            }));
            efforts.getAndIncrement();
            active.getAndIncrement();
            maxActive.set(Math.max(active.get(), maxActive.get()));
            final Thread thread = new Thread(context) {

                @Override
                public void run() {
                    long nr = 0;
                    try {
                        while (!terminate.get()) {
                            Thread.sleep(emitDelay);
                            if (nextBeforeFailure.getAndDecrement() > 0) {
                                observer.onNext(nr++);
                            } else {
                                active.decrementAndGet();
                                observer.onError(new RuntimeException("expected-failed"));
                                break;
                            }
                        }
                    } catch (InterruptedException t) {
                    }
                }
            };
            thread.start();
        }
    }

    /**
     * Observer for listener on separate thread.
     */
    static final class AsyncObserver<T> extends DefaultObserver<T> {

        protected CountDownLatch latch = new CountDownLatch(1);

        protected Observer<T> target;

        /**
         * Wrap existing Observer.
         * @param target the target nbp subscriber
         */
        AsyncObserver(Observer<T> target) {
            this.target = target;
        }

        /**
         * Wait.
         */
        public void await() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                fail("Test interrupted");
            }
        }

        // Observer implementation
        @Override
        public void onComplete() {
            target.onComplete();
            latch.countDown();
        }

        @Override
        public void onError(Throwable t) {
            target.onError(t);
            latch.countDown();
        }

        @Override
        public void onNext(T v) {
            target.onNext(v);
        }
    }

    @Test
    public void unsubscribeAfterError() {
        Observer<Long> observer = TestHelper.mockObserver();
        // Observable that always fails after 100ms
        SlowObservable so = new SlowObservable(100, 0, "testUnsubscribeAfterError");
        Observable<Long> o = Observable.unsafeCreate(so).retry(5);
        AsyncObserver<Long> async = new AsyncObserver<>(observer);
        o.subscribe(async);
        async.await();
        InOrder inOrder = inOrder(observer);
        // Should fail once
        inOrder.verify(observer, times(1)).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onComplete();
        assertEquals("Start 6 threads, retry 5 then fail on 6", 6, so.efforts.get());
        assertEquals("Only 1 active subscription", 1, so.maxActive.get());
    }

    @Test
    public void timeoutWithRetry() {
        Observer<Long> observer = TestHelper.mockObserver();
        // Observable that sends every 100ms (timeout fails instead)
        SlowObservable so = new SlowObservable(100, 10, "testTimeoutWithRetry");
        Observable<Long> o = Observable.unsafeCreate(so).timeout(80, TimeUnit.MILLISECONDS).retry(5);
        AsyncObserver<Long> async = new AsyncObserver<>(observer);
        o.subscribe(async);
        async.await();
        InOrder inOrder = inOrder(observer);
        // Should fail once
        inOrder.verify(observer, times(1)).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onComplete();
        assertEquals("Start 6 threads, retry 5 then fail on 6", 6, so.efforts.get());
    }

    @Test
    public void retryWithBackpressure() throws InterruptedException {
        final int NUM_LOOPS = 1;
        for (int j = 0; j < NUM_LOOPS; j++) {
            final int NUM_RETRIES = Flowable.bufferSize() * 2;
            for (int i = 0; i < 400; i++) {
                Observer<String> observer = TestHelper.mockObserver();
                Observable<String> origin = Observable.unsafeCreate(new FuncWithErrors(NUM_RETRIES));
                TestObserver<String> to = new TestObserver<>(observer);
                origin.retry().observeOn(Schedulers.computation()).subscribe(to);
                to.awaitDone(5, TimeUnit.SECONDS);
                InOrder inOrder = inOrder(observer);
                // should have no errors
                verify(observer, never()).onError(any(Throwable.class));
                // should show NUM_RETRIES attempts
                inOrder.verify(observer, times(NUM_RETRIES + 1)).onNext("beginningEveryTime");
                // should have a single success
                inOrder.verify(observer, times(1)).onNext("onSuccessOnly");
                // should have a single successful onComplete
                inOrder.verify(observer, times(1)).onComplete();
                inOrder.verifyNoMoreInteractions();
            }
        }
    }

    @Test
    public void retryWithBackpressureParallel() throws InterruptedException {
        final int NUM_LOOPS = 1;
        final int NUM_RETRIES = Flowable.bufferSize() * 2;
        int ncpu = Runtime.getRuntime().availableProcessors();
        ExecutorService exec = Executors.newFixedThreadPool(Math.max(ncpu / 2, 2));
        try {
            for (int r = 0; r < NUM_LOOPS; r++) {
                if (r % 10 == 0) {
                    System.out.println("testRetryWithBackpressureParallelLoop -> " + r);
                }
                final AtomicInteger timeouts = new AtomicInteger();
                final Map<Integer, List<String>> data = new ConcurrentHashMap<>();
                int m = 5000;
                final CountDownLatch cdl = new CountDownLatch(m);
                for (int i = 0; i < m; i++) {
                    final int j = i;
                    exec.execute(new Runnable() {

                        @Override
                        public void run() {
                            final AtomicInteger nexts = new AtomicInteger();
                            try {
                                Observable<String> origin = Observable.unsafeCreate(new FuncWithErrors(NUM_RETRIES));
                                TestObserverEx<String> to = new TestObserverEx<>();
                                origin.retry().observeOn(Schedulers.computation()).subscribe(to);
                                to.awaitDone(2500, TimeUnit.MILLISECONDS);
                                List<String> onNextEvents = new ArrayList<>(to.values());
                                if (onNextEvents.size() != NUM_RETRIES + 2) {
                                    for (Throwable t : to.errors()) {
                                        onNextEvents.add(t.toString());
                                    }
                                    for (long err = to.completions(); err != 0; err--) {
                                        onNextEvents.add("onComplete");
                                    }
                                    data.put(j, onNextEvents);
                                }
                            } catch (Throwable t) {
                                timeouts.incrementAndGet();
                                System.out.println(j + " | " + cdl.getCount() + " !!! " + nexts.get());
                            }
                            cdl.countDown();
                        }
                    });
                }
                cdl.await();
                assertEquals(0, timeouts.get());
                if (data.size() > 0) {
                    fail("Data content mismatch: " + allSequenceFrequency(data));
                }
            }
        } finally {
            exec.shutdown();
        }
    }

    static <T> StringBuilder allSequenceFrequency(Map<Integer, List<T>> its) {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<Integer, List<T>> e : its.entrySet()) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append(e.getKey()).append("={");
            b.append(sequenceFrequency(e.getValue()));
            b.append("}");
        }
        return b;
    }

    static <T> StringBuilder sequenceFrequency(Iterable<T> it) {
        StringBuilder sb = new StringBuilder();
        Object prev = null;
        int cnt = 0;
        for (Object curr : it) {
            if (sb.length() > 0) {
                if (!curr.equals(prev)) {
                    if (cnt > 1) {
                        sb.append(" x ").append(cnt);
                        cnt = 1;
                    }
                    sb.append(", ");
                    sb.append(curr);
                } else {
                    cnt++;
                }
            } else {
                sb.append(curr);
                cnt++;
            }
            prev = curr;
        }
        if (cnt > 1) {
            sb.append(" x ").append(cnt);
        }
        return sb;
    }

    @Test
    public void issue1900() throws InterruptedException {
        Observer<String> observer = TestHelper.mockObserver();
        final int NUM_MSG = 1034;
        final AtomicInteger count = new AtomicInteger();
        Observable<String> origin = Observable.range(0, NUM_MSG).map(new Function<Integer, String>() {

            @Override
            public String apply(Integer t1) {
                return "msg: " + count.incrementAndGet();
            }
        });
        origin.retry().groupBy(new Function<String, String>() {

            @Override
            public String apply(String t1) {
                return t1;
            }
        }).flatMap(new Function<GroupedObservable<String, String>, Observable<String>>() {

            @Override
            public Observable<String> apply(GroupedObservable<String, String> t1) {
                return t1.take(1);
            }
        }).subscribe(new TestObserver<>(observer));
        InOrder inOrder = inOrder(observer);
        // should show 3 attempts
        inOrder.verify(observer, times(NUM_MSG)).onNext(any(java.lang.String.class));
        // // should have no errors
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        // should have a single success
        // inOrder.verify(observer, times(1)).onNext("onSuccessOnly");
        // should have a single successful onComplete
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void issue1900SourceNotSupportingBackpressure() {
        Observer<String> observer = TestHelper.mockObserver();
        final int NUM_MSG = 1034;
        final AtomicInteger count = new AtomicInteger();
        Observable<String> origin = Observable.unsafeCreate(new ObservableSource<String>() {

            @Override
            public void subscribe(Observer<? super String> o) {
                o.onSubscribe(Disposable.empty());
                for (int i = 0; i < NUM_MSG; i++) {
                    o.onNext("msg:" + count.incrementAndGet());
                }
                o.onComplete();
            }
        });
        origin.retry().groupBy(new Function<String, String>() {

            @Override
            public String apply(String t1) {
                return t1;
            }
        }).flatMap(new Function<GroupedObservable<String, String>, Observable<String>>() {

            @Override
            public Observable<String> apply(GroupedObservable<String, String> t1) {
                return t1.take(1);
            }
        }).subscribe(new TestObserver<>(observer));
        InOrder inOrder = inOrder(observer);
        // should show 3 attempts
        inOrder.verify(observer, times(NUM_MSG)).onNext(any(java.lang.String.class));
        // // should have no errors
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        // should have a single success
        // inOrder.verify(observer, times(1)).onNext("onSuccessOnly");
        // should have a single successful onComplete
        inOrder.verify(observer, times(1)).onComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void retryPredicate() {
        Observable.just(1).concatWith(Observable.<Integer>error(new TestException())).retry(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable v) throws Exception {
                return true;
            }
        }).take(5).test().assertResult(1, 1, 1, 1, 1);
    }

    @Test
    public void retryUntil() {
        Observable.just(1).concatWith(Observable.<Integer>error(new TestException())).retryUntil(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return false;
            }
        }).take(5).test().assertResult(1, 1, 1, 1, 1);
    }

    @Test
    public void retryLongPredicateInvalid() {
        try {
            Observable.just(1).retry(-99, new Predicate<Throwable>() {

                @Override
                public boolean test(Throwable e) throws Exception {
                    return true;
                }
            });
            fail("Should have thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("times >= 0 required but it was -99", ex.getMessage());
        }
    }

    @Test
    public void shouldDisposeInnerObservable() {
        final PublishSubject<Object> subject = PublishSubject.create();
        final Disposable disposable = Observable.error(new RuntimeException("Leak")).retryWhen(new Function<Observable<Throwable>, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Observable<Throwable> errors) throws Exception {
                return errors.switchMap(new Function<Throwable, ObservableSource<Object>>() {

                    @Override
                    public ObservableSource<Object> apply(Throwable ignore) throws Exception {
                        return subject;
                    }
                });
            }
        }).subscribe();
        assertTrue(subject.hasObservers());
        disposable.dispose();
        assertFalse(subject.hasObservers());
    }

    @Test
    public void noCancelPreviousRetry() {
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger times = new AtomicInteger();
        Observable<Integer> source = Observable.defer(new Supplier<ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> get() throws Exception {
                if (times.getAndIncrement() < 4) {
                    return Observable.error(new TestException());
                }
                return Observable.just(1);
            }
        }).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        source.retry(5).test().assertResult(1);
        assertEquals(0, counter.get());
    }

    @Test
    public void noCancelPreviousRetryWhile() {
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger times = new AtomicInteger();
        Observable<Integer> source = Observable.defer(new Supplier<ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> get() throws Exception {
                if (times.getAndIncrement() < 4) {
                    return Observable.error(new TestException());
                }
                return Observable.just(1);
            }
        }).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        source.retry(5, Functions.alwaysTrue()).test().assertResult(1);
        assertEquals(0, counter.get());
    }

    @Test
    public void noCancelPreviousRetryWhile2() {
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger times = new AtomicInteger();
        Observable<Integer> source = Observable.defer(new Supplier<ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> get() throws Exception {
                if (times.getAndIncrement() < 4) {
                    return Observable.error(new TestException());
                }
                return Observable.just(1);
            }
        }).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        source.retry(new BiPredicate<Integer, Throwable>() {

            @Override
            public boolean test(Integer a, Throwable b) throws Exception {
                return a < 5;
            }
        }).test().assertResult(1);
        assertEquals(0, counter.get());
    }

    @Test
    public void noCancelPreviousRetryUntil() {
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger times = new AtomicInteger();
        Observable<Integer> source = Observable.defer(new Supplier<ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> get() throws Exception {
                if (times.getAndIncrement() < 4) {
                    return Observable.error(new TestException());
                }
                return Observable.just(1);
            }
        }).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        source.retryUntil(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return false;
            }
        }).test().assertResult(1);
        assertEquals(0, counter.get());
    }

    @Test
    public void noCancelPreviousRepeatWhen() {
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger times = new AtomicInteger();
        Observable<Integer> source = Observable.defer(new Supplier<ObservableSource<Integer>>() {

            @Override
            public ObservableSource<Integer> get() throws Exception {
                if (times.get() < 4) {
                    return Observable.error(new TestException());
                }
                return Observable.just(1);
            }
        }).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        source.retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {

            @Override
            public ObservableSource<?> apply(Observable<Throwable> e) throws Exception {
                return e.takeWhile(new Predicate<Object>() {

                    @Override
                    public boolean test(Object v) throws Exception {
                        return times.getAndIncrement() < 4;
                    }
                });
            }
        }).test().assertResult(1);
        assertEquals(0, counter.get());
    }

    @Test
    public void noCancelPreviousRepeatWhen2() {
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger times = new AtomicInteger();
        Observable<Integer> source = Observable.<Integer>error(new TestException()).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                counter.getAndIncrement();
            }
        });
        source.retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {

            @Override
            public ObservableSource<?> apply(Observable<Throwable> e) throws Exception {
                return e.takeWhile(new Predicate<Object>() {

                    @Override
                    public boolean test(Object v) throws Exception {
                        return times.getAndIncrement() < 4;
                    }
                });
            }
        }).test().assertResult();
        assertEquals(0, counter.get());
    }

    @Test
    public void repeatFloodNoSubscriptionError() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        final TestException error = new TestException();
        try {
            final PublishSubject<Integer> source = PublishSubject.create();
            final PublishSubject<Integer> signaller = PublishSubject.create();
            for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                TestObserver<Integer> to = source.take(1).map(new Function<Integer, Integer>() {

                    @Override
                    public Integer apply(Integer v) throws Exception {
                        throw error;
                    }
                }).retryWhen(new Function<Observable<Throwable>, ObservableSource<Integer>>() {

                    @Override
                    public ObservableSource<Integer> apply(Observable<Throwable> v) throws Exception {
                        return signaller;
                    }
                }).test();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                            source.onNext(1);
                        }
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
                            signaller.onNext(1);
                        }
                    }
                };
                TestHelper.race(r1, r2);
                to.dispose();
            }
            if (!errors.isEmpty()) {
                for (Throwable e : errors) {
                    e.printStackTrace();
                }
                fail(errors + "");
            }
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableRetryTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_iterativeBackoff() throws java.lang.Throwable {
            this.payloads.iterativeBackoff.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryIndefinitely() throws java.lang.Throwable {
            this.payloads.retryIndefinitely.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_schedulingNotificationHandler() throws java.lang.Throwable {
            this.payloads.schedulingNotificationHandler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextFromNotificationHandler() throws java.lang.Throwable {
            this.payloads.onNextFromNotificationHandler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompletedFromNotificationHandler() throws java.lang.Throwable {
            this.payloads.onCompletedFromNotificationHandler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorFromNotificationHandler() throws java.lang.Throwable {
            this.payloads.onErrorFromNotificationHandler.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleSubscriptionOnFirst() throws java.lang.Throwable {
            this.payloads.singleSubscriptionOnFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_originFails() throws java.lang.Throwable {
            this.payloads.originFails.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryFail() throws java.lang.Throwable {
            this.payloads.retryFail.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retrySuccess() throws java.lang.Throwable {
            this.payloads.retrySuccess.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_infiniteRetry() throws java.lang.Throwable {
            this.payloads.infiniteRetry.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retrySubscribesAgainAfterError() throws java.lang.Throwable {
            this.payloads.retrySubscribesAgainAfterError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeFromRetry() throws java.lang.Throwable {
            this.payloads.unsubscribeFromRetry.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryAllowsSubscriptionAfterAllSubscriptionsUnsubscribed() throws java.lang.Throwable {
            this.payloads.retryAllowsSubscriptionAfterAllSubscriptionsUnsubscribed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceObservableCallsUnsubscribe() throws java.lang.Throwable {
            this.payloads.sourceObservableCallsUnsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceObservableRetry1() throws java.lang.Throwable {
            this.payloads.sourceObservableRetry1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sourceObservableRetry0() throws java.lang.Throwable {
            this.payloads.sourceObservableRetry0.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_unsubscribeAfterError() throws java.lang.Throwable {
            this.payloads.unsubscribeAfterError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutWithRetry() throws java.lang.Throwable {
            this.payloads.timeoutWithRetry.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryWithBackpressure() throws java.lang.Throwable {
            this.payloads.retryWithBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryWithBackpressureParallel() throws java.lang.Throwable {
            this.payloads.retryWithBackpressureParallel.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1900() throws java.lang.Throwable {
            this.payloads.issue1900.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_issue1900SourceNotSupportingBackpressure() throws java.lang.Throwable {
            this.payloads.issue1900SourceNotSupportingBackpressure.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryPredicate() throws java.lang.Throwable {
            this.payloads.retryPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryUntil() throws java.lang.Throwable {
            this.payloads.retryUntil.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryLongPredicateInvalid() throws java.lang.Throwable {
            this.payloads.retryLongPredicateInvalid.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldDisposeInnerObservable() throws java.lang.Throwable {
            this.payloads.shouldDisposeInnerObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPreviousRetry() throws java.lang.Throwable {
            this.payloads.noCancelPreviousRetry.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPreviousRetryWhile() throws java.lang.Throwable {
            this.payloads.noCancelPreviousRetryWhile.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPreviousRetryWhile2() throws java.lang.Throwable {
            this.payloads.noCancelPreviousRetryWhile2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPreviousRetryUntil() throws java.lang.Throwable {
            this.payloads.noCancelPreviousRetryUntil.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPreviousRepeatWhen() throws java.lang.Throwable {
            this.payloads.noCancelPreviousRepeatWhen.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_noCancelPreviousRepeatWhen2() throws java.lang.Throwable {
            this.payloads.noCancelPreviousRepeatWhen2.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatFloodNoSubscriptionError() throws java.lang.Throwable {
            this.payloads.repeatFloodNoSubscriptionError.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRetryTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRetryTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRetryTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRetryTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableRetryTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableRetryTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableRetryTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableRetryTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement iterativeBackoff;

            public org.junit.runners.model.Statement retryIndefinitely;

            public org.junit.runners.model.Statement schedulingNotificationHandler;

            public org.junit.runners.model.Statement onNextFromNotificationHandler;

            public org.junit.runners.model.Statement onCompletedFromNotificationHandler;

            public org.junit.runners.model.Statement onErrorFromNotificationHandler;

            public org.junit.runners.model.Statement singleSubscriptionOnFirst;

            public org.junit.runners.model.Statement originFails;

            public org.junit.runners.model.Statement retryFail;

            public org.junit.runners.model.Statement retrySuccess;

            public org.junit.runners.model.Statement infiniteRetry;

            public org.junit.runners.model.Statement retrySubscribesAgainAfterError;

            public org.junit.runners.model.Statement unsubscribeFromRetry;

            public org.junit.runners.model.Statement retryAllowsSubscriptionAfterAllSubscriptionsUnsubscribed;

            public org.junit.runners.model.Statement sourceObservableCallsUnsubscribe;

            public org.junit.runners.model.Statement sourceObservableRetry1;

            public org.junit.runners.model.Statement sourceObservableRetry0;

            public org.junit.runners.model.Statement unsubscribeAfterError;

            public org.junit.runners.model.Statement timeoutWithRetry;

            public org.junit.runners.model.Statement retryWithBackpressure;

            public org.junit.runners.model.Statement retryWithBackpressureParallel;

            public org.junit.runners.model.Statement issue1900;

            public org.junit.runners.model.Statement issue1900SourceNotSupportingBackpressure;

            public org.junit.runners.model.Statement retryPredicate;

            public org.junit.runners.model.Statement retryUntil;

            public org.junit.runners.model.Statement retryLongPredicateInvalid;

            public org.junit.runners.model.Statement shouldDisposeInnerObservable;

            public org.junit.runners.model.Statement noCancelPreviousRetry;

            public org.junit.runners.model.Statement noCancelPreviousRetryWhile;

            public org.junit.runners.model.Statement noCancelPreviousRetryWhile2;

            public org.junit.runners.model.Statement noCancelPreviousRetryUntil;

            public org.junit.runners.model.Statement noCancelPreviousRepeatWhen;

            public org.junit.runners.model.Statement noCancelPreviousRepeatWhen2;

            public org.junit.runners.model.Statement repeatFloodNoSubscriptionError;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.iterativeBackoff = _ClassStatement.forPayload(ObservableRetryTest::iterativeBackoff, "iterativeBackoff", this);
            this.payloads.retryIndefinitely = _ClassStatement.forPayload(ObservableRetryTest::retryIndefinitely, "retryIndefinitely", this);
            this.payloads.schedulingNotificationHandler = _ClassStatement.forPayload(ObservableRetryTest::schedulingNotificationHandler, "schedulingNotificationHandler", this);
            this.payloads.onNextFromNotificationHandler = _ClassStatement.forPayload(ObservableRetryTest::onNextFromNotificationHandler, "onNextFromNotificationHandler", this);
            this.payloads.onCompletedFromNotificationHandler = _ClassStatement.forPayload(ObservableRetryTest::onCompletedFromNotificationHandler, "onCompletedFromNotificationHandler", this);
            this.payloads.onErrorFromNotificationHandler = _ClassStatement.forPayload(ObservableRetryTest::onErrorFromNotificationHandler, "onErrorFromNotificationHandler", this);
            this.payloads.singleSubscriptionOnFirst = _ClassStatement.forPayload(ObservableRetryTest::singleSubscriptionOnFirst, "singleSubscriptionOnFirst", this);
            this.payloads.originFails = _ClassStatement.forPayload(ObservableRetryTest::originFails, "originFails", this);
            this.payloads.retryFail = _ClassStatement.forPayload(ObservableRetryTest::retryFail, "retryFail", this);
            this.payloads.retrySuccess = _ClassStatement.forPayload(ObservableRetryTest::retrySuccess, "retrySuccess", this);
            this.payloads.infiniteRetry = _ClassStatement.forPayload(ObservableRetryTest::infiniteRetry, "infiniteRetry", this);
            this.payloads.retrySubscribesAgainAfterError = _ClassStatement.forPayload(ObservableRetryTest::retrySubscribesAgainAfterError, "retrySubscribesAgainAfterError", this);
            this.payloads.unsubscribeFromRetry = _ClassStatement.forPayload(ObservableRetryTest::unsubscribeFromRetry, "unsubscribeFromRetry", this);
            this.payloads.retryAllowsSubscriptionAfterAllSubscriptionsUnsubscribed = _ClassStatement.forPayload(ObservableRetryTest::retryAllowsSubscriptionAfterAllSubscriptionsUnsubscribed, "retryAllowsSubscriptionAfterAllSubscriptionsUnsubscribed", this);
            this.payloads.sourceObservableCallsUnsubscribe = _ClassStatement.forPayload(ObservableRetryTest::sourceObservableCallsUnsubscribe, "sourceObservableCallsUnsubscribe", this);
            this.payloads.sourceObservableRetry1 = _ClassStatement.forPayload(ObservableRetryTest::sourceObservableRetry1, "sourceObservableRetry1", this);
            this.payloads.sourceObservableRetry0 = _ClassStatement.forPayload(ObservableRetryTest::sourceObservableRetry0, "sourceObservableRetry0", this);
            this.payloads.unsubscribeAfterError = _ClassStatement.forPayload(ObservableRetryTest::unsubscribeAfterError, "unsubscribeAfterError", this);
            this.payloads.timeoutWithRetry = _ClassStatement.forPayload(ObservableRetryTest::timeoutWithRetry, "timeoutWithRetry", this);
            this.payloads.retryWithBackpressure = _ClassStatement.forPayload(ObservableRetryTest::retryWithBackpressure, "retryWithBackpressure", this);
            this.payloads.retryWithBackpressureParallel = _ClassStatement.forPayload(ObservableRetryTest::retryWithBackpressureParallel, "retryWithBackpressureParallel", this);
            this.payloads.issue1900 = _ClassStatement.forPayload(ObservableRetryTest::issue1900, "issue1900", this);
            this.payloads.issue1900SourceNotSupportingBackpressure = _ClassStatement.forPayload(ObservableRetryTest::issue1900SourceNotSupportingBackpressure, "issue1900SourceNotSupportingBackpressure", this);
            this.payloads.retryPredicate = _ClassStatement.forPayload(ObservableRetryTest::retryPredicate, "retryPredicate", this);
            this.payloads.retryUntil = _ClassStatement.forPayload(ObservableRetryTest::retryUntil, "retryUntil", this);
            this.payloads.retryLongPredicateInvalid = _ClassStatement.forPayload(ObservableRetryTest::retryLongPredicateInvalid, "retryLongPredicateInvalid", this);
            this.payloads.shouldDisposeInnerObservable = _ClassStatement.forPayload(ObservableRetryTest::shouldDisposeInnerObservable, "shouldDisposeInnerObservable", this);
            this.payloads.noCancelPreviousRetry = _ClassStatement.forPayload(ObservableRetryTest::noCancelPreviousRetry, "noCancelPreviousRetry", this);
            this.payloads.noCancelPreviousRetryWhile = _ClassStatement.forPayload(ObservableRetryTest::noCancelPreviousRetryWhile, "noCancelPreviousRetryWhile", this);
            this.payloads.noCancelPreviousRetryWhile2 = _ClassStatement.forPayload(ObservableRetryTest::noCancelPreviousRetryWhile2, "noCancelPreviousRetryWhile2", this);
            this.payloads.noCancelPreviousRetryUntil = _ClassStatement.forPayload(ObservableRetryTest::noCancelPreviousRetryUntil, "noCancelPreviousRetryUntil", this);
            this.payloads.noCancelPreviousRepeatWhen = _ClassStatement.forPayload(ObservableRetryTest::noCancelPreviousRepeatWhen, "noCancelPreviousRepeatWhen", this);
            this.payloads.noCancelPreviousRepeatWhen2 = _ClassStatement.forPayload(ObservableRetryTest::noCancelPreviousRepeatWhen2, "noCancelPreviousRepeatWhen2", this);
            this.payloads.repeatFloodNoSubscriptionError = _ClassStatement.forPayload(ObservableRetryTest::repeatFloodNoSubscriptionError, "repeatFloodNoSubscriptionError", this);
        }
    }
}
