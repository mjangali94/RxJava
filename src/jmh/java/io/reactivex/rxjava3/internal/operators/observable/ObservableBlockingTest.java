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
import org.junit.Test;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.RxJavaTest;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.observers.BlockingFirstObserver;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableBlockingTest extends RxJavaTest {

    @Test
    public void blockingFirst() {
        assertEquals(1, Observable.range(1, 10).subscribeOn(Schedulers.computation()).blockingFirst().intValue());
    }

    @Test
    public void blockingFirstDefault() {
        assertEquals(1, Observable.<Integer>empty().subscribeOn(Schedulers.computation()).blockingFirst(1).intValue());
    }

    @Test
    public void blockingSubscribeConsumer() {
        final List<Integer> list = new ArrayList<>();
        Observable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void blockingSubscribeConsumerConsumer() {
        final List<Object> list = new ArrayList<>();
        Observable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Consumer<Integer>() {

            @Override
            public void accept(Integer v) throws Exception {
                list.add(v);
            }
        }, Functions.emptyConsumer());
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void blockingSubscribeConsumerConsumerError() {
        final List<Object> list = new ArrayList<>();
        TestException ex = new TestException();
        Consumer<Object> cons = new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add(v);
            }
        };
        Observable.range(1, 5).concatWith(Observable.<Integer>error(ex)).subscribeOn(Schedulers.computation()).blockingSubscribe(cons, cons);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, ex), list);
    }

    @Test
    public void blockingSubscribeConsumerConsumerAction() {
        final List<Object> list = new ArrayList<>();
        Consumer<Object> cons = new Consumer<Object>() {

            @Override
            public void accept(Object v) throws Exception {
                list.add(v);
            }
        };
        Observable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(cons, cons, new Action() {

            @Override
            public void run() throws Exception {
                list.add(100);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 100), list);
    }

    @Test
    public void blockingSubscribeObserver() {
        final List<Object> list = new ArrayList<>();
        Observable.range(1, 5).subscribeOn(Schedulers.computation()).blockingSubscribe(new Observer<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object value) {
                list.add(value);
            }

            @Override
            public void onError(Throwable e) {
                list.add(e);
            }

            @Override
            public void onComplete() {
                list.add(100);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 100), list);
    }

    @Test
    public void blockingSubscribeObserverError() {
        final List<Object> list = new ArrayList<>();
        final TestException ex = new TestException();
        Observable.range(1, 5).concatWith(Observable.<Integer>error(ex)).subscribeOn(Schedulers.computation()).blockingSubscribe(new Observer<Object>() {

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object value) {
                list.add(value);
            }

            @Override
            public void onError(Throwable e) {
                list.add(e);
            }

            @Override
            public void onComplete() {
                list.add(100);
            }
        });
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, ex), list);
    }

    @Test(expected = TestException.class)
    public void blockingForEachThrows() {
        Observable.just(1).blockingForEach(new Consumer<Integer>() {

            @Override
            public void accept(Integer e) throws Exception {
                throw new TestException();
            }
        });
    }

    @Test(expected = NoSuchElementException.class)
    public void blockingFirstEmpty() {
        Observable.empty().blockingFirst();
    }

    @Test(expected = NoSuchElementException.class)
    public void blockingLastEmpty() {
        Observable.empty().blockingLast();
    }

    @Test
    public void blockingFirstNormal() {
        assertEquals(1, Observable.just(1, 2).blockingFirst(3).intValue());
    }

    @Test
    public void blockingLastNormal() {
        assertEquals(2, Observable.just(1, 2).blockingLast(3).intValue());
    }

    @Test(expected = NoSuchElementException.class)
    public void blockingSingleEmpty() {
        Observable.empty().blockingSingle();
    }

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(ObservableBlockingSubscribe.class);
    }

    @Test
    public void disposeUpFront() {
        TestObserver<Object> to = new TestObserver<>();
        to.dispose();
        Observable.just(1).blockingSubscribe(to);
        to.assertEmpty();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void delayed() throws Exception {
        final TestObserver<Object> to = new TestObserver<>();
        final Observer[] s = { null };
        Schedulers.single().scheduleDirect(new Runnable() {

            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                to.dispose();
                s[0].onNext(1);
            }
        }, 200, TimeUnit.MILLISECONDS);
        new Observable<Integer>() {

            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                observer.onSubscribe(Disposable.empty());
                s[0] = observer;
            }
        }.blockingSubscribe(to);
        while (!to.isDisposed()) {
            Thread.sleep(100);
        }
        to.assertEmpty();
    }

    @Test
    public void interrupt() {
        TestObserver<Object> to = new TestObserver<>();
        Thread.currentThread().interrupt();
        Observable.never().blockingSubscribe(to);
    }

    @Test
    public void onCompleteDelayed() {
        TestObserver<Object> to = new TestObserver<>();
        Observable.empty().delay(100, TimeUnit.MILLISECONDS).blockingSubscribe(to);
        to.assertResult();
    }

    @Test
    public void blockingCancelUpfront() {
        BlockingFirstObserver<Integer> o = new BlockingFirstObserver<>();
        assertFalse(o.isDisposed());
        o.dispose();
        assertTrue(o.isDisposed());
        Disposable d = Disposable.empty();
        o.onSubscribe(d);
        assertTrue(d.isDisposed());
        Thread.currentThread().interrupt();
        try {
            o.blockingGet();
            fail("Should have thrown");
        } catch (RuntimeException ex) {
            assertTrue(ex.toString(), ex.getCause() instanceof InterruptedException);
        }
        Thread.interrupted();
        o.onError(new TestException());
        try {
            o.blockingGet();
            fail("Should have thrown");
        } catch (TestException ex) {
        // expected
        }
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableBlockingTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirst() throws java.lang.Throwable {
            this.payloads.blockingFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirstDefault() throws java.lang.Throwable {
            this.payloads.blockingFirstDefault.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeConsumer() throws java.lang.Throwable {
            this.payloads.blockingSubscribeConsumer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeConsumerConsumer() throws java.lang.Throwable {
            this.payloads.blockingSubscribeConsumerConsumer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeConsumerConsumerError() throws java.lang.Throwable {
            this.payloads.blockingSubscribeConsumerConsumerError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeConsumerConsumerAction() throws java.lang.Throwable {
            this.payloads.blockingSubscribeConsumerConsumerAction.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeObserver() throws java.lang.Throwable {
            this.payloads.blockingSubscribeObserver.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSubscribeObserverError() throws java.lang.Throwable {
            this.payloads.blockingSubscribeObserverError.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingForEachThrows() throws java.lang.Throwable {
            this.payloads.blockingForEachThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirstEmpty() throws java.lang.Throwable {
            this.payloads.blockingFirstEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingLastEmpty() throws java.lang.Throwable {
            this.payloads.blockingLastEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingFirstNormal() throws java.lang.Throwable {
            this.payloads.blockingFirstNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingLastNormal() throws java.lang.Throwable {
            this.payloads.blockingLastNormal.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingSingleEmpty() throws java.lang.Throwable {
            this.payloads.blockingSingleEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_utilityClass() throws java.lang.Throwable {
            this.payloads.utilityClass.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeUpFront() throws java.lang.Throwable {
            this.payloads.disposeUpFront.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delayed() throws java.lang.Throwable {
            this.payloads.delayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_interrupt() throws java.lang.Throwable {
            this.payloads.interrupt.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onCompleteDelayed() throws java.lang.Throwable {
            this.payloads.onCompleteDelayed.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_blockingCancelUpfront() throws java.lang.Throwable {
            this.payloads.blockingCancelUpfront.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableBlockingTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableBlockingTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableBlockingTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableBlockingTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableBlockingTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableBlockingTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableBlockingTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableBlockingTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement blockingFirst;

            public org.junit.runners.model.Statement blockingFirstDefault;

            public org.junit.runners.model.Statement blockingSubscribeConsumer;

            public org.junit.runners.model.Statement blockingSubscribeConsumerConsumer;

            public org.junit.runners.model.Statement blockingSubscribeConsumerConsumerError;

            public org.junit.runners.model.Statement blockingSubscribeConsumerConsumerAction;

            public org.junit.runners.model.Statement blockingSubscribeObserver;

            public org.junit.runners.model.Statement blockingSubscribeObserverError;

            public org.junit.runners.model.Statement blockingForEachThrows;

            public org.junit.runners.model.Statement blockingFirstEmpty;

            public org.junit.runners.model.Statement blockingLastEmpty;

            public org.junit.runners.model.Statement blockingFirstNormal;

            public org.junit.runners.model.Statement blockingLastNormal;

            public org.junit.runners.model.Statement blockingSingleEmpty;

            public org.junit.runners.model.Statement utilityClass;

            public org.junit.runners.model.Statement disposeUpFront;

            public org.junit.runners.model.Statement delayed;

            public org.junit.runners.model.Statement interrupt;

            public org.junit.runners.model.Statement onCompleteDelayed;

            public org.junit.runners.model.Statement blockingCancelUpfront;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.blockingFirst = _ClassStatement.forPayload(ObservableBlockingTest::blockingFirst, "blockingFirst", this);
            this.payloads.blockingFirstDefault = _ClassStatement.forPayload(ObservableBlockingTest::blockingFirstDefault, "blockingFirstDefault", this);
            this.payloads.blockingSubscribeConsumer = _ClassStatement.forPayload(ObservableBlockingTest::blockingSubscribeConsumer, "blockingSubscribeConsumer", this);
            this.payloads.blockingSubscribeConsumerConsumer = _ClassStatement.forPayload(ObservableBlockingTest::blockingSubscribeConsumerConsumer, "blockingSubscribeConsumerConsumer", this);
            this.payloads.blockingSubscribeConsumerConsumerError = _ClassStatement.forPayload(ObservableBlockingTest::blockingSubscribeConsumerConsumerError, "blockingSubscribeConsumerConsumerError", this);
            this.payloads.blockingSubscribeConsumerConsumerAction = _ClassStatement.forPayload(ObservableBlockingTest::blockingSubscribeConsumerConsumerAction, "blockingSubscribeConsumerConsumerAction", this);
            this.payloads.blockingSubscribeObserver = _ClassStatement.forPayload(ObservableBlockingTest::blockingSubscribeObserver, "blockingSubscribeObserver", this);
            this.payloads.blockingSubscribeObserverError = _ClassStatement.forPayload(ObservableBlockingTest::blockingSubscribeObserverError, "blockingSubscribeObserverError", this);
            this.payloads.blockingForEachThrows = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableBlockingTest::blockingForEachThrows, io.reactivex.rxjava3.exceptions.TestException.class), "blockingForEachThrows", this);
            this.payloads.blockingFirstEmpty = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableBlockingTest::blockingFirstEmpty, java.util.NoSuchElementException.class), "blockingFirstEmpty", this);
            this.payloads.blockingLastEmpty = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableBlockingTest::blockingLastEmpty, java.util.NoSuchElementException.class), "blockingLastEmpty", this);
            this.payloads.blockingFirstNormal = _ClassStatement.forPayload(ObservableBlockingTest::blockingFirstNormal, "blockingFirstNormal", this);
            this.payloads.blockingLastNormal = _ClassStatement.forPayload(ObservableBlockingTest::blockingLastNormal, "blockingLastNormal", this);
            this.payloads.blockingSingleEmpty = _ClassStatement.forPayload(new se.chalmers.ju2jmh.api.ExceptionTest<>(ObservableBlockingTest::blockingSingleEmpty, java.util.NoSuchElementException.class), "blockingSingleEmpty", this);
            this.payloads.utilityClass = _ClassStatement.forPayload(ObservableBlockingTest::utilityClass, "utilityClass", this);
            this.payloads.disposeUpFront = _ClassStatement.forPayload(ObservableBlockingTest::disposeUpFront, "disposeUpFront", this);
            this.payloads.delayed = _ClassStatement.forPayload(ObservableBlockingTest::delayed, "delayed", this);
            this.payloads.interrupt = _ClassStatement.forPayload(ObservableBlockingTest::interrupt, "interrupt", this);
            this.payloads.onCompleteDelayed = _ClassStatement.forPayload(ObservableBlockingTest::onCompleteDelayed, "onCompleteDelayed", this);
            this.payloads.blockingCancelUpfront = _ClassStatement.forPayload(ObservableBlockingTest::blockingCancelUpfront, "blockingCancelUpfront", this);
        }
    }
}
