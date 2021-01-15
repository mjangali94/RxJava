/**
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
package io.reactivex.rxjava3.subjects;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.testsupport.*;

public class SerializedSubjectTest extends RxJavaTest {

    @Test
    public void basic() {
        SerializedSubject<String> subject = new SerializedSubject<>(PublishSubject.<String>create());
        TestObserver<String> to = new TestObserver<>();
        subject.subscribe(to);
        subject.onNext("hello");
        subject.onComplete();
        to.awaitDone(5, TimeUnit.SECONDS);
        to.assertValue("hello");
    }

    @Test
    public void asyncSubjectValueRelay() {
        AsyncSubject<Integer> async = AsyncSubject.create();
        async.onNext(1);
        async.onComplete();
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertTrue(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
        assertEquals((Integer) 1, async.getValue());
        assertTrue(async.hasValue());
    }

    @Test
    public void asyncSubjectValueEmpty() {
        AsyncSubject<Integer> async = AsyncSubject.create();
        async.onComplete();
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertTrue(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
        assertNull(async.getValue());
        assertFalse(async.hasValue());
    }

    @Test
    public void asyncSubjectValueError() {
        AsyncSubject<Integer> async = AsyncSubject.create();
        TestException te = new TestException();
        async.onError(te);
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertFalse(serial.hasComplete());
        assertTrue(serial.hasThrowable());
        assertSame(te, serial.getThrowable());
        assertNull(async.getValue());
        assertFalse(async.hasValue());
    }

    @Test
    public void publishSubjectValueRelay() {
        PublishSubject<Integer> async = PublishSubject.create();
        async.onNext(1);
        async.onComplete();
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertTrue(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
    }

    @Test
    public void publishSubjectValueEmpty() {
        PublishSubject<Integer> async = PublishSubject.create();
        async.onComplete();
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertTrue(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
    }

    @Test
    public void publishSubjectValueError() {
        PublishSubject<Integer> async = PublishSubject.create();
        TestException te = new TestException();
        async.onError(te);
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertFalse(serial.hasComplete());
        assertTrue(serial.hasThrowable());
        assertSame(te, serial.getThrowable());
    }

    @Test
    public void behaviorSubjectValueRelay() {
        BehaviorSubject<Integer> async = BehaviorSubject.create();
        async.onNext(1);
        async.onComplete();
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertTrue(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
        assertNull(async.getValue());
        assertFalse(async.hasValue());
    }

    @Test
    public void behaviorSubjectValueRelayIncomplete() {
        BehaviorSubject<Integer> async = BehaviorSubject.create();
        async.onNext(1);
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertFalse(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
        assertEquals((Integer) 1, async.getValue());
        assertTrue(async.hasValue());
    }

    @Test
    public void behaviorSubjectIncompleteEmpty() {
        BehaviorSubject<Integer> async = BehaviorSubject.create();
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertFalse(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
        assertNull(async.getValue());
        assertFalse(async.hasValue());
    }

    @Test
    public void behaviorSubjectEmpty() {
        BehaviorSubject<Integer> async = BehaviorSubject.create();
        async.onComplete();
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertTrue(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
        assertNull(async.getValue());
        assertFalse(async.hasValue());
    }

    @Test
    public void behaviorSubjectError() {
        BehaviorSubject<Integer> async = BehaviorSubject.create();
        TestException te = new TestException();
        async.onError(te);
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertFalse(serial.hasComplete());
        assertTrue(serial.hasThrowable());
        assertSame(te, serial.getThrowable());
        assertNull(async.getValue());
        assertFalse(async.hasValue());
    }

    @Test
    public void replaySubjectValueRelay() {
        ReplaySubject<Integer> async = ReplaySubject.create();
        async.onNext(1);
        async.onComplete();
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertTrue(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
        assertEquals((Integer) 1, async.getValue());
        assertTrue(async.hasValue());
        assertArrayEquals(new Object[] { 1 }, async.getValues());
        assertArrayEquals(new Integer[] { 1 }, async.getValues(new Integer[0]));
        assertArrayEquals(new Integer[] { 1 }, async.getValues(new Integer[] { 0 }));
        assertArrayEquals(new Integer[] { 1, null }, async.getValues(new Integer[] { 0, 0 }));
    }

    @Test
    public void replaySubjectValueRelayIncomplete() {
        ReplaySubject<Integer> async = ReplaySubject.create();
        async.onNext(1);
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertFalse(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
        assertEquals((Integer) 1, async.getValue());
        assertTrue(async.hasValue());
        assertArrayEquals(new Object[] { 1 }, async.getValues());
        assertArrayEquals(new Integer[] { 1 }, async.getValues(new Integer[0]));
        assertArrayEquals(new Integer[] { 1 }, async.getValues(new Integer[] { 0 }));
        assertArrayEquals(new Integer[] { 1, null }, async.getValues(new Integer[] { 0, 0 }));
    }

    @Test
    public void replaySubjectValueRelayBounded() {
        ReplaySubject<Integer> async = ReplaySubject.createWithSize(1);
        async.onNext(0);
        async.onNext(1);
        async.onComplete();
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertTrue(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
        assertEquals((Integer) 1, async.getValue());
        assertTrue(async.hasValue());
        assertArrayEquals(new Object[] { 1 }, async.getValues());
        assertArrayEquals(new Integer[] { 1 }, async.getValues(new Integer[0]));
        assertArrayEquals(new Integer[] { 1 }, async.getValues(new Integer[] { 0 }));
        assertArrayEquals(new Integer[] { 1, null }, async.getValues(new Integer[] { 0, 0 }));
    }

    @Test
    public void replaySubjectValueRelayBoundedIncomplete() {
        ReplaySubject<Integer> async = ReplaySubject.createWithSize(1);
        async.onNext(0);
        async.onNext(1);
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertFalse(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
        assertEquals((Integer) 1, async.getValue());
        assertTrue(async.hasValue());
        assertArrayEquals(new Object[] { 1 }, async.getValues());
        assertArrayEquals(new Integer[] { 1 }, async.getValues(new Integer[0]));
        assertArrayEquals(new Integer[] { 1 }, async.getValues(new Integer[] { 0 }));
        assertArrayEquals(new Integer[] { 1, null }, async.getValues(new Integer[] { 0, 0 }));
    }

    @Test
    public void replaySubjectValueRelayBoundedEmptyIncomplete() {
        ReplaySubject<Integer> async = ReplaySubject.createWithSize(1);
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertFalse(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
        assertNull(async.getValue());
        assertFalse(async.hasValue());
        assertArrayEquals(new Object[] {}, async.getValues());
        assertArrayEquals(new Integer[] {}, async.getValues(new Integer[0]));
        assertArrayEquals(new Integer[] { null }, async.getValues(new Integer[] { 0 }));
        assertArrayEquals(new Integer[] { null, 0 }, async.getValues(new Integer[] { 0, 0 }));
    }

    @Test
    public void replaySubjectValueRelayEmptyIncomplete() {
        ReplaySubject<Integer> async = ReplaySubject.create();
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertFalse(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
        assertNull(async.getValue());
        assertFalse(async.hasValue());
        assertArrayEquals(new Object[] {}, async.getValues());
        assertArrayEquals(new Integer[] {}, async.getValues(new Integer[0]));
        assertArrayEquals(new Integer[] { null }, async.getValues(new Integer[] { 0 }));
        assertArrayEquals(new Integer[] { null, 0 }, async.getValues(new Integer[] { 0, 0 }));
    }

    @Test
    public void replaySubjectEmpty() {
        ReplaySubject<Integer> async = ReplaySubject.create();
        async.onComplete();
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertTrue(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
        assertNull(async.getValue());
        assertFalse(async.hasValue());
        assertArrayEquals(new Object[] {}, async.getValues());
        assertArrayEquals(new Integer[] {}, async.getValues(new Integer[0]));
        assertArrayEquals(new Integer[] { null }, async.getValues(new Integer[] { 0 }));
        assertArrayEquals(new Integer[] { null, 0 }, async.getValues(new Integer[] { 0, 0 }));
    }

    @Test
    public void replaySubjectError() {
        ReplaySubject<Integer> async = ReplaySubject.create();
        TestException te = new TestException();
        async.onError(te);
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertFalse(serial.hasComplete());
        assertTrue(serial.hasThrowable());
        assertSame(te, serial.getThrowable());
        assertNull(async.getValue());
        assertFalse(async.hasValue());
        assertArrayEquals(new Object[] {}, async.getValues());
        assertArrayEquals(new Integer[] {}, async.getValues(new Integer[0]));
        assertArrayEquals(new Integer[] { null }, async.getValues(new Integer[] { 0 }));
        assertArrayEquals(new Integer[] { null, 0 }, async.getValues(new Integer[] { 0, 0 }));
    }

    @Test
    public void replaySubjectBoundedEmpty() {
        ReplaySubject<Integer> async = ReplaySubject.createWithSize(1);
        async.onComplete();
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertTrue(serial.hasComplete());
        assertFalse(serial.hasThrowable());
        assertNull(serial.getThrowable());
        assertNull(async.getValue());
        assertFalse(async.hasValue());
        assertArrayEquals(new Object[] {}, async.getValues());
        assertArrayEquals(new Integer[] {}, async.getValues(new Integer[0]));
        assertArrayEquals(new Integer[] { null }, async.getValues(new Integer[] { 0 }));
        assertArrayEquals(new Integer[] { null, 0 }, async.getValues(new Integer[] { 0, 0 }));
    }

    @Test
    public void replaySubjectBoundedError() {
        ReplaySubject<Integer> async = ReplaySubject.createWithSize(1);
        TestException te = new TestException();
        async.onError(te);
        Subject<Integer> serial = async.toSerialized();
        assertFalse(serial.hasObservers());
        assertFalse(serial.hasComplete());
        assertTrue(serial.hasThrowable());
        assertSame(te, serial.getThrowable());
        assertNull(async.getValue());
        assertFalse(async.hasValue());
        assertArrayEquals(new Object[] {}, async.getValues());
        assertArrayEquals(new Integer[] {}, async.getValues(new Integer[0]));
        assertArrayEquals(new Integer[] { null }, async.getValues(new Integer[] { 0 }));
        assertArrayEquals(new Integer[] { null, 0 }, async.getValues(new Integer[] { 0, 0 }));
    }

    @Test
    public void dontWrapSerializedSubjectAgain() {
        PublishSubject<Object> s = PublishSubject.create();
        Subject<Object> s1 = s.toSerialized();
        Subject<Object> s2 = s1.toSerialized();
        assertSame(s1, s2);
    }

    @Test
    public void normal() {
        Subject<Integer> s = PublishSubject.<Integer>create().toSerialized();
        TestObserver<Integer> to = s.test();
        Observable.range(1, 10).subscribe(s);
        to.assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertFalse(s.hasObservers());
        s.onNext(11);
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            s.onError(new TestException());
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
        s.onComplete();
        Disposable bs = Disposable.empty();
        s.onSubscribe(bs);
        assertTrue(bs.isDisposed());
    }

    @Test
    public void onNextOnNextRace() {
        Set<Integer> expectedSet = new HashSet<>(Arrays.asList(1, 2));
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final Subject<Integer> s = PublishSubject.<Integer>create().toSerialized();
            TestObserverEx<Integer> to = s.to(TestHelper.<Integer>testConsumer());
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    s.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    s.onNext(2);
                }
            };
            TestHelper.race(r1, r2);
            to.assertSubscribed().assertNoErrors().assertNotComplete().assertValueCount(2);
            Set<Integer> actualSet = new HashSet<>(to.values());
            assertEquals("" + actualSet, expectedSet, actualSet);
        }
    }

    @Test
    public void onNextOnErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final Subject<Integer> s = PublishSubject.<Integer>create().toSerialized();
            TestObserver<Integer> to = s.test();
            final TestException ex = new TestException();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    s.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    s.onError(ex);
                }
            };
            TestHelper.race(r1, r2);
            to.assertError(ex).assertNotComplete();
            if (to.values().size() != 0) {
                to.assertValue(1);
            }
        }
    }

    @Test
    public void onNextOnCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final Subject<Integer> s = PublishSubject.<Integer>create().toSerialized();
            TestObserver<Integer> to = s.test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    s.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    s.onComplete();
                }
            };
            TestHelper.race(r1, r2);
            to.assertComplete().assertNoErrors();
            if (to.values().size() != 0) {
                to.assertValue(1);
            }
        }
    }

    @Test
    public void onNextOnSubscribeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final Subject<Integer> s = PublishSubject.<Integer>create().toSerialized();
            TestObserver<Integer> to = s.test();
            final Disposable bs = Disposable.empty();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    s.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    s.onSubscribe(bs);
                }
            };
            TestHelper.race(r1, r2);
            to.assertValue(1).assertNotComplete().assertNoErrors();
        }
    }

    @Test
    public void onCompleteOnSubscribeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final Subject<Integer> s = PublishSubject.<Integer>create().toSerialized();
            TestObserver<Integer> to = s.test();
            final Disposable bs = Disposable.empty();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    s.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    s.onSubscribe(bs);
                }
            };
            TestHelper.race(r1, r2);
            to.assertResult();
        }
    }

    @Test
    public void onCompleteOnCompleteRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final Subject<Integer> s = PublishSubject.<Integer>create().toSerialized();
            TestObserver<Integer> to = s.test();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    s.onComplete();
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    s.onComplete();
                }
            };
            TestHelper.race(r1, r2);
            to.assertResult();
        }
    }

    @Test
    public void onErrorOnErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final Subject<Integer> s = PublishSubject.<Integer>create().toSerialized();
            TestObserver<Integer> to = s.test();
            final TestException ex = new TestException();
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        s.onError(ex);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        s.onError(ex);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertFailure(TestException.class);
                TestHelper.assertUndeliverable(errors, 0, TestException.class);
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void onSubscribeOnSubscribeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final Subject<Integer> s = PublishSubject.<Integer>create().toSerialized();
            TestObserver<Integer> to = s.test();
            final Disposable bs1 = Disposable.empty();
            final Disposable bs2 = Disposable.empty();
            Runnable r1 = new Runnable() {

                @Override
                public void run() {
                    s.onSubscribe(bs1);
                }
            };
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    s.onSubscribe(bs2);
                }
            };
            TestHelper.race(r1, r2);
            to.assertEmpty();
        }
    }

    @Test
    public void onErrorQueued() {
        Subject<Integer> sp = PublishSubject.<Integer>create().toSerialized();
        TestObserver<Integer> to = new TestObserver<Integer>() {

            @Override
            public void onNext(@NonNull Integer t) {
                super.onNext(t);
                if (t == 1) {
                    sp.onNext(2);
                    sp.onNext(3);
                    sp.onSubscribe(Disposable.empty());
                    sp.onError(new TestException());
                }
            }
        };
        sp.subscribe(to);
        sp.onNext(1);
        // errors skip ahead
        to.assertFailure(TestException.class, 1);
    }

    @Test
    public void onCompleteQueued() {
        Subject<Integer> sp = PublishSubject.<Integer>create().toSerialized();
        TestObserver<Integer> to = new TestObserver<Integer>() {

            @Override
            public void onNext(@NonNull Integer t) {
                super.onNext(t);
                if (t == 1) {
                    sp.onNext(2);
                    sp.onNext(3);
                    sp.onComplete();
                }
            }
        };
        sp.subscribe(to);
        sp.onNext(1);
        to.assertResult(1, 2, 3);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class myBenchmark extends io.reactivex.rxjava3.core.RxJavaTest.myBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_basic() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::basic, this.description("basic"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncSubjectValueRelay() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncSubjectValueRelay, this.description("asyncSubjectValueRelay"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncSubjectValueEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncSubjectValueEmpty, this.description("asyncSubjectValueEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_asyncSubjectValueError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::asyncSubjectValueError, this.description("asyncSubjectValueError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishSubjectValueRelay() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::publishSubjectValueRelay, this.description("publishSubjectValueRelay"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishSubjectValueEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::publishSubjectValueEmpty, this.description("publishSubjectValueEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_publishSubjectValueError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::publishSubjectValueError, this.description("publishSubjectValueError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_behaviorSubjectValueRelay() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::behaviorSubjectValueRelay, this.description("behaviorSubjectValueRelay"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_behaviorSubjectValueRelayIncomplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::behaviorSubjectValueRelayIncomplete, this.description("behaviorSubjectValueRelayIncomplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_behaviorSubjectIncompleteEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::behaviorSubjectIncompleteEmpty, this.description("behaviorSubjectIncompleteEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_behaviorSubjectEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::behaviorSubjectEmpty, this.description("behaviorSubjectEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_behaviorSubjectError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::behaviorSubjectError, this.description("behaviorSubjectError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySubjectValueRelay() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaySubjectValueRelay, this.description("replaySubjectValueRelay"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySubjectValueRelayIncomplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaySubjectValueRelayIncomplete, this.description("replaySubjectValueRelayIncomplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySubjectValueRelayBounded() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaySubjectValueRelayBounded, this.description("replaySubjectValueRelayBounded"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySubjectValueRelayBoundedIncomplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaySubjectValueRelayBoundedIncomplete, this.description("replaySubjectValueRelayBoundedIncomplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySubjectValueRelayBoundedEmptyIncomplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaySubjectValueRelayBoundedEmptyIncomplete, this.description("replaySubjectValueRelayBoundedEmptyIncomplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySubjectValueRelayEmptyIncomplete() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaySubjectValueRelayEmptyIncomplete, this.description("replaySubjectValueRelayEmptyIncomplete"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySubjectEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaySubjectEmpty, this.description("replaySubjectEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySubjectError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaySubjectError, this.description("replaySubjectError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySubjectBoundedEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaySubjectBoundedEmpty, this.description("replaySubjectBoundedEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_replaySubjectBoundedError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::replaySubjectBoundedError, this.description("replaySubjectBoundedError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dontWrapSerializedSubjectAgain() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dontWrapSerializedSubjectAgain, this.description("dontWrapSerializedSubjectAgain"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::normal, this.description("normal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextOnNextRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextOnNextRace, this.description("onNextOnNextRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextOnErrorRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextOnErrorRace, this.description("onNextOnErrorRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextOnCompleteRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextOnCompleteRace, this.description("onNextOnCompleteRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onNextOnSubscribeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onNextOnSubscribeRace, this.description("onNextOnSubscribeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteOnSubscribeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteOnSubscribeRace, this.description("onCompleteOnSubscribeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteOnCompleteRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteOnCompleteRace, this.description("onCompleteOnCompleteRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorOnErrorRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorOnErrorRace, this.description("onErrorOnErrorRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeOnSubscribeRace() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSubscribeOnSubscribeRace, this.description("onSubscribeOnSubscribeRace"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorQueued() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorQueued, this.description("onErrorQueued"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteQueued() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onCompleteQueued, this.description("onCompleteQueued"));
        }

        private SerializedSubjectTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SerializedSubjectTest();
        }

        @java.lang.Override
        public SerializedSubjectTest implementation() {
            return this.implementation;
        }
    }
}
