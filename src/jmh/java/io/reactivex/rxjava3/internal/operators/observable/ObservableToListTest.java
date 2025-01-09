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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import org.mockito.Mockito;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.testsupport.TestHelper;

public class ObservableToListTest extends RxJavaTest {

    @Test
    public void listObservable() {
        Observable<String> w = Observable.fromIterable(Arrays.asList("one", "two", "three"));
        Observable<List<String>> observable = w.toList().toObservable();
        Observer<List<String>> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, times(1)).onNext(Arrays.asList("one", "two", "three"));
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void listViaObservableObservable() {
        Observable<String> w = Observable.fromIterable(Arrays.asList("one", "two", "three"));
        Observable<List<String>> observable = w.toList().toObservable();
        Observer<List<String>> observer = TestHelper.mockObserver();
        observable.subscribe(observer);
        verify(observer, times(1)).onNext(Arrays.asList("one", "two", "three"));
        verify(observer, Mockito.never()).onError(any(Throwable.class));
        verify(observer, times(1)).onComplete();
    }

    @Test
    public void listMultipleSubscribersObservable() {
        Observable<String> w = Observable.fromIterable(Arrays.asList("one", "two", "three"));
        Observable<List<String>> observable = w.toList().toObservable();
        Observer<List<String>> o1 = TestHelper.mockObserver();
        observable.subscribe(o1);
        Observer<List<String>> o2 = TestHelper.mockObserver();
        observable.subscribe(o2);
        List<String> expected = Arrays.asList("one", "two", "three");
        verify(o1, times(1)).onNext(expected);
        verify(o1, Mockito.never()).onError(any(Throwable.class));
        verify(o1, times(1)).onComplete();
        verify(o2, times(1)).onNext(expected);
        verify(o2, Mockito.never()).onError(any(Throwable.class));
        verify(o2, times(1)).onComplete();
    }

    @Test
    public void listWithBlockingFirstObservable() {
        Observable<String> o = Observable.fromIterable(Arrays.asList("one", "two", "three"));
        List<String> actual = o.toList().toObservable().blockingFirst();
        Assert.assertEquals(Arrays.asList("one", "two", "three"), actual);
    }

    @Test
    public void capacityHintObservable() {
        Observable.range(1, 10).toList(4).toObservable().test().assertResult(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void list() {
        Observable<String> w = Observable.fromIterable(Arrays.asList("one", "two", "three"));
        Single<List<String>> single = w.toList();
        SingleObserver<List<String>> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(Arrays.asList("one", "two", "three"));
        verify(observer, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void listViaObservable() {
        Observable<String> w = Observable.fromIterable(Arrays.asList("one", "two", "three"));
        Single<List<String>> single = w.toList();
        SingleObserver<List<String>> observer = TestHelper.mockSingleObserver();
        single.subscribe(observer);
        verify(observer, times(1)).onSuccess(Arrays.asList("one", "two", "three"));
        verify(observer, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void listMultipleSubscribers() {
        Observable<String> w = Observable.fromIterable(Arrays.asList("one", "two", "three"));
        Single<List<String>> single = w.toList();
        SingleObserver<List<String>> o1 = TestHelper.mockSingleObserver();
        single.subscribe(o1);
        SingleObserver<List<String>> o2 = TestHelper.mockSingleObserver();
        single.subscribe(o2);
        List<String> expected = Arrays.asList("one", "two", "three");
        verify(o1, times(1)).onSuccess(expected);
        verify(o1, Mockito.never()).onError(any(Throwable.class));
        verify(o2, times(1)).onSuccess(expected);
        verify(o2, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void listWithBlockingFirst() {
        Observable<String> o = Observable.fromIterable(Arrays.asList("one", "two", "three"));
        List<String> actual = o.toList().blockingGet();
        Assert.assertEquals(Arrays.asList("one", "two", "three"), actual);
    }

    static void await(CyclicBarrier cb) {
        try {
            cb.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (BrokenBarrierException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void capacityHint() {
        Observable.range(1, 10).toList(4).test().assertResult(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).toList().toObservable());
        TestHelper.checkDisposed(Observable.just(1).toList());
    }

    @Test
    public void error() {
        Observable.error(new TestException()).toList().toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void errorSingle() {
        Observable.error(new TestException()).toList().test().assertFailure(TestException.class);
    }

    @Test
    public void collectionSupplierThrows() {
        Observable.just(1).toList(new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() throws Exception {
                throw new TestException();
            }
        }).toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void collectionSupplierReturnsNull() {
        Observable.just(1).toList(new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() throws Exception {
                return null;
            }
        }).toObservable().to(TestHelper.<Collection<Integer>>testConsumer()).assertFailure(NullPointerException.class).assertErrorMessage(ExceptionHelper.nullWarning("The collectionSupplier returned a null Collection."));
    }

    @Test
    public void singleCollectionSupplierThrows() {
        Observable.just(1).toList(new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() throws Exception {
                throw new TestException();
            }
        }).test().assertFailure(TestException.class);
    }

    @Test
    public void singleCollectionSupplierReturnsNull() {
        Observable.just(1).toList(new Supplier<Collection<Integer>>() {

            @Override
            public Collection<Integer> get() throws Exception {
                return null;
            }
        }).to(TestHelper.<Collection<Integer>>testConsumer()).assertFailure(NullPointerException.class).assertErrorMessage(ExceptionHelper.nullWarning("The collectionSupplier returned a null Collection."));
    }

    @Test
    public void doubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservable(new Function<Observable<Object>, Observable<List<Object>>>() {

            @Override
            public Observable<List<Object>> apply(Observable<Object> f) throws Exception {
                return f.toList().toObservable();
            }
        });
        TestHelper.checkDoubleOnSubscribeObservableToSingle(new Function<Observable<Object>, Single<List<Object>>>() {

            @Override
            public Single<List<Object>> apply(Observable<Object> f) throws Exception {
                return f.toList();
            }
        });
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableToListTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listObservable() throws java.lang.Throwable {
            this.payloads.listObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listViaObservableObservable() throws java.lang.Throwable {
            this.payloads.listViaObservableObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listMultipleSubscribersObservable() throws java.lang.Throwable {
            this.payloads.listMultipleSubscribersObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listWithBlockingFirstObservable() throws java.lang.Throwable {
            this.payloads.listWithBlockingFirstObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_capacityHintObservable() throws java.lang.Throwable {
            this.payloads.capacityHintObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_list() throws java.lang.Throwable {
            this.payloads.list.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listViaObservable() throws java.lang.Throwable {
            this.payloads.listViaObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listMultipleSubscribers() throws java.lang.Throwable {
            this.payloads.listMultipleSubscribers.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_listWithBlockingFirst() throws java.lang.Throwable {
            this.payloads.listWithBlockingFirst.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_capacityHint() throws java.lang.Throwable {
            this.payloads.capacityHint.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_error() throws java.lang.Throwable {
            this.payloads.error.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSingle() throws java.lang.Throwable {
            this.payloads.errorSingle.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectionSupplierThrows() throws java.lang.Throwable {
            this.payloads.collectionSupplierThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_collectionSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.collectionSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleCollectionSupplierThrows() throws java.lang.Throwable {
            this.payloads.singleCollectionSupplierThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_singleCollectionSupplierReturnsNull() throws java.lang.Throwable {
            this.payloads.singleCollectionSupplierReturnsNull.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doubleOnSubscribe() throws java.lang.Throwable {
            this.payloads.doubleOnSubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToListTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToListTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToListTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToListTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableToListTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableToListTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableToListTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableToListTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement listObservable;

            public org.junit.runners.model.Statement listViaObservableObservable;

            public org.junit.runners.model.Statement listMultipleSubscribersObservable;

            public org.junit.runners.model.Statement listWithBlockingFirstObservable;

            public org.junit.runners.model.Statement capacityHintObservable;

            public org.junit.runners.model.Statement list;

            public org.junit.runners.model.Statement listViaObservable;

            public org.junit.runners.model.Statement listMultipleSubscribers;

            public org.junit.runners.model.Statement listWithBlockingFirst;

            public org.junit.runners.model.Statement capacityHint;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement error;

            public org.junit.runners.model.Statement errorSingle;

            public org.junit.runners.model.Statement collectionSupplierThrows;

            public org.junit.runners.model.Statement collectionSupplierReturnsNull;

            public org.junit.runners.model.Statement singleCollectionSupplierThrows;

            public org.junit.runners.model.Statement singleCollectionSupplierReturnsNull;

            public org.junit.runners.model.Statement doubleOnSubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.listObservable = _ClassStatement.forPayload(ObservableToListTest::listObservable, "listObservable", this);
            this.payloads.listViaObservableObservable = _ClassStatement.forPayload(ObservableToListTest::listViaObservableObservable, "listViaObservableObservable", this);
            this.payloads.listMultipleSubscribersObservable = _ClassStatement.forPayload(ObservableToListTest::listMultipleSubscribersObservable, "listMultipleSubscribersObservable", this);
            this.payloads.listWithBlockingFirstObservable = _ClassStatement.forPayload(ObservableToListTest::listWithBlockingFirstObservable, "listWithBlockingFirstObservable", this);
            this.payloads.capacityHintObservable = _ClassStatement.forPayload(ObservableToListTest::capacityHintObservable, "capacityHintObservable", this);
            this.payloads.list = _ClassStatement.forPayload(ObservableToListTest::list, "list", this);
            this.payloads.listViaObservable = _ClassStatement.forPayload(ObservableToListTest::listViaObservable, "listViaObservable", this);
            this.payloads.listMultipleSubscribers = _ClassStatement.forPayload(ObservableToListTest::listMultipleSubscribers, "listMultipleSubscribers", this);
            this.payloads.listWithBlockingFirst = _ClassStatement.forPayload(ObservableToListTest::listWithBlockingFirst, "listWithBlockingFirst", this);
            this.payloads.capacityHint = _ClassStatement.forPayload(ObservableToListTest::capacityHint, "capacityHint", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableToListTest::dispose, "dispose", this);
            this.payloads.error = _ClassStatement.forPayload(ObservableToListTest::error, "error", this);
            this.payloads.errorSingle = _ClassStatement.forPayload(ObservableToListTest::errorSingle, "errorSingle", this);
            this.payloads.collectionSupplierThrows = _ClassStatement.forPayload(ObservableToListTest::collectionSupplierThrows, "collectionSupplierThrows", this);
            this.payloads.collectionSupplierReturnsNull = _ClassStatement.forPayload(ObservableToListTest::collectionSupplierReturnsNull, "collectionSupplierReturnsNull", this);
            this.payloads.singleCollectionSupplierThrows = _ClassStatement.forPayload(ObservableToListTest::singleCollectionSupplierThrows, "singleCollectionSupplierThrows", this);
            this.payloads.singleCollectionSupplierReturnsNull = _ClassStatement.forPayload(ObservableToListTest::singleCollectionSupplierReturnsNull, "singleCollectionSupplierReturnsNull", this);
            this.payloads.doubleOnSubscribe = _ClassStatement.forPayload(ObservableToListTest::doubleOnSubscribe, "doubleOnSubscribe", this);
        }
    }
}
