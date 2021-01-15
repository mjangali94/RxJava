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
package io.reactivex.rxjava3.internal.operators.single;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class SingleDoOnTest extends RxJavaTest {

    @Test
    public void doOnDispose() {
        final int[] count = { 0 };
        Single.never().doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                count[0]++;
            }
        }).test(true);
        assertEquals(1, count[0]);
    }

    @Test
    public void doOnError() {
        final Object[] event = { null };
        Single.error(new TestException()).doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable e) throws Exception {
                event[0] = e;
            }
        }).test();
        assertTrue(event[0].toString(), event[0] instanceof TestException);
    }

    @Test
    public void doOnSubscribe() {
        final int[] count = { 0 };
        Single.never().doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                count[0]++;
            }
        }).test();
        assertEquals(1, count[0]);
    }

    @Test
    public void doOnSuccess() {
        final Object[] event = { null };
        Single.just(1).doOnSuccess(new Consumer<Integer>() {

            @Override
            public void accept(Integer e) throws Exception {
                event[0] = e;
            }
        }).test();
        assertEquals(1, event[0]);
    }

    @Test
    public void doOnSubscribeNormal() {
        final int[] count = { 0 };
        Single.just(1).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                count[0]++;
            }
        }).test().assertResult(1);
        assertEquals(1, count[0]);
    }

    @Test
    public void doOnSubscribeError() {
        final int[] count = { 0 };
        Single.error(new TestException()).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                count[0]++;
            }
        }).test().assertFailure(TestException.class);
        assertEquals(1, count[0]);
    }

    @Test
    public void doOnSubscribeJustCrash() {
        Single.just(1).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void doOnSubscribeErrorCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Single.error(new TestException("Outer")).doOnSubscribe(new Consumer<Disposable>() {

                @Override
                public void accept(Disposable d) throws Exception {
                    throw new TestException("Inner");
                }
            }).to(TestHelper.testConsumer()).assertFailureAndMessage(TestException.class, "Inner");
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Outer");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void onErrorSuccess() {
        final int[] call = { 0 };
        Single.just(1).doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable v) throws Exception {
                call[0]++;
            }
        }).test().assertResult(1);
        assertEquals(0, call[0]);
    }

    @Test
    public void onErrorCrashes() {
        TestObserverEx<Object> to = Single.error(new TestException("Outer")).doOnError(new Consumer<Throwable>() {

            @Override
            public void accept(Throwable v) throws Exception {
                throw new TestException("Inner");
            }
        }).to(TestHelper.testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(errors, 0, TestException.class, "Outer");
        TestHelper.assertError(errors, 1, TestException.class, "Inner");
    }

    @Test
    public void doOnEventThrowsSuccess() {
        Single.just(1).doOnEvent(new BiConsumer<Integer, Throwable>() {

            @Override
            public void accept(Integer v, Throwable e) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void doOnEventThrowsError() {
        TestObserverEx<Integer> to = Single.<Integer>error(new TestException("Main")).doOnEvent(new BiConsumer<Integer, Throwable>() {

            @Override
            public void accept(Integer v, Throwable e) throws Exception {
                throw new TestException("Inner");
            }
        }).to(TestHelper.<Integer>testConsumer()).assertFailure(CompositeException.class);
        List<Throwable> errors = TestHelper.compositeList(to.errors().get(0));
        TestHelper.assertError(errors, 0, TestException.class, "Main");
        TestHelper.assertError(errors, 1, TestException.class, "Inner");
    }

    @Test
    public void doOnDisposeDispose() {
        final int[] calls = { 0 };
        TestHelper.checkDisposed(PublishSubject.create().singleOrError().doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                calls[0]++;
            }
        }));
        assertEquals(1, calls[0]);
    }

    @Test
    public void doOnDisposeSuccess() {
        final int[] calls = { 0 };
        Single.just(1).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                calls[0]++;
            }
        }).test().assertResult(1);
        assertEquals(0, calls[0]);
    }

    @Test
    public void doOnDisposeError() {
        final int[] calls = { 0 };
        Single.error(new TestException()).doOnDispose(new Action() {

            @Override
            public void run() throws Exception {
                calls[0]++;
            }
        }).test().assertFailure(TestException.class);
        assertEquals(0, calls[0]);
    }

    @Test
    public void doOnDisposeDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeSingle(new Function<Single<Object>, SingleSource<Object>>() {

            @Override
            public SingleSource<Object> apply(Single<Object> s) throws Exception {
                return s.doOnDispose(Functions.EMPTY_ACTION);
            }
        });
    }

    @Test
    public void doOnDisposeCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            PublishSubject<Integer> ps = PublishSubject.create();
            ps.singleOrError().doOnDispose(new Action() {

                @Override
                public void run() throws Exception {
                    throw new TestException();
                }
            }).test().dispose();
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void doOnSuccessErrors() {
        final int[] call = { 0 };
        Single.error(new TestException()).doOnSuccess(new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                call[0]++;
            }
        }).test().assertFailure(TestException.class);
        assertEquals(0, call[0]);
    }

    @Test
    public void doOnSuccessCrash() {
        Single.just(1).doOnSuccess(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void onSubscribeCrash() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            final Disposable bs = Disposable.empty();
            new Single<Integer>() {

                @Override
                protected void subscribeActual(SingleObserver<? super Integer> observer) {
                    observer.onSubscribe(bs);
                    observer.onError(new TestException("Second"));
                    observer.onSuccess(1);
                }
            }.doOnSubscribe(new Consumer<Disposable>() {

                @Override
                public void accept(Disposable d) throws Exception {
                    throw new TestException("First");
                }
            }).to(TestHelper.<Integer>testConsumer()).assertFailureAndMessage(TestException.class, "First");
            assertTrue(bs.isDisposed());
            TestHelper.assertUndeliverable(errors, 0, TestException.class, "Second");
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class myBenchmark extends io.reactivex.rxjava3.core.RxJavaTest.myBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnDispose, this.description("doOnDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnError, this.description("doOnError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnSubscribe, this.description("doOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnSuccess, this.description("doOnSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribeNormal() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnSubscribeNormal, this.description("doOnSubscribeNormal"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribeError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnSubscribeError, this.description("doOnSubscribeError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribeJustCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnSubscribeJustCrash, this.description("doOnSubscribeJustCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSubscribeErrorCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnSubscribeErrorCrash, this.description("doOnSubscribeErrorCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorSuccess, this.description("onErrorSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorCrashes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onErrorCrashes, this.description("onErrorCrashes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEventThrowsSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnEventThrowsSuccess, this.description("doOnEventThrowsSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnEventThrowsError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnEventThrowsError, this.description("doOnEventThrowsError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeDispose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnDisposeDispose, this.description("doOnDisposeDispose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeSuccess() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnDisposeSuccess, this.description("doOnDisposeSuccess"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeError() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnDisposeError, this.description("doOnDisposeError"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeDoubleOnSubscribe() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnDisposeDoubleOnSubscribe, this.description("doOnDisposeDoubleOnSubscribe"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnDisposeCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnDisposeCrash, this.description("doOnDisposeCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSuccessErrors() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnSuccessErrors, this.description("doOnSuccessErrors"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doOnSuccessCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doOnSuccessCrash, this.description("doOnSuccessCrash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onSubscribeCrash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::onSubscribeCrash, this.description("onSubscribeCrash"));
        }

        private SingleDoOnTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SingleDoOnTest();
        }

        @java.lang.Override
        public SingleDoOnTest implementation() {
            return this.implementation;
        }
    }
}
