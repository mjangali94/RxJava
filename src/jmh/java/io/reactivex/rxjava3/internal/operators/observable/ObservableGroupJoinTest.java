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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.*;
import org.mockito.MockitoAnnotations;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.observable.ObservableGroupJoin.*;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.testsupport.*;

public class ObservableGroupJoinTest extends RxJavaTest {

    Observer<Object> observer = TestHelper.mockObserver();

    BiFunction<Integer, Integer, Integer> add = new BiFunction<Integer, Integer, Integer>() {

        @Override
        public Integer apply(Integer t1, Integer t2) {
            return t1 + t2;
        }
    };

    <T> Function<Integer, Observable<T>> just(final Observable<T> observable) {
        return new Function<Integer, Observable<T>>() {

            @Override
            public Observable<T> apply(Integer t1) {
                return observable;
            }
        };
    }

    <T, R> Function<T, Observable<R>> just2(final Observable<R> observable) {
        return new Function<T, Observable<R>>() {

            @Override
            public Observable<R> apply(T t1) {
                return observable;
            }
        };
    }

    BiFunction<Integer, Observable<Integer>, Observable<Integer>> add2 = new BiFunction<Integer, Observable<Integer>, Observable<Integer>>() {

        @Override
        public Observable<Integer> apply(final Integer leftValue, Observable<Integer> rightValues) {
            return rightValues.map(new Function<Integer, Integer>() {

                @Override
                public Integer apply(Integer rightValue) throws Throwable {
                    return add.apply(leftValue, rightValue);
                }
            });
        }
    };

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void behaveAsJoin() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Observable<Integer> m = Observable.merge(source1.groupJoin(source2, just(Observable.never()), just(Observable.never()), add2));
        m.subscribe(observer);
        source1.onNext(1);
        source1.onNext(2);
        source1.onNext(4);
        source2.onNext(16);
        source2.onNext(32);
        source2.onNext(64);
        source1.onComplete();
        source2.onComplete();
        verify(observer, times(1)).onNext(17);
        verify(observer, times(1)).onNext(18);
        verify(observer, times(1)).onNext(20);
        verify(observer, times(1)).onNext(33);
        verify(observer, times(1)).onNext(34);
        verify(observer, times(1)).onNext(36);
        verify(observer, times(1)).onNext(65);
        verify(observer, times(1)).onNext(66);
        verify(observer, times(1)).onNext(68);
        // Never emitted?
        verify(observer, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    class Person {

        final int id;

        final String name;

        Person(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    class PersonFruit {

        final int personId;

        final String fruit;

        PersonFruit(int personId, String fruit) {
            this.personId = personId;
            this.fruit = fruit;
        }
    }

    class PPF {

        final Person person;

        final Observable<PersonFruit> fruits;

        PPF(Person person, Observable<PersonFruit> fruits) {
            this.person = person;
            this.fruits = fruits;
        }
    }

    @Test
    public void normal1() {
        Observable<Person> source1 = Observable.fromIterable(Arrays.asList(new Person(1, "Joe"), new Person(2, "Mike"), new Person(3, "Charlie")));
        Observable<PersonFruit> source2 = Observable.fromIterable(Arrays.asList(new PersonFruit(1, "Strawberry"), new PersonFruit(1, "Apple"), new PersonFruit(3, "Peach")));
        Observable<PPF> q = source1.groupJoin(source2, just2(Observable.<Object>never()), just2(Observable.<Object>never()), new BiFunction<Person, Observable<PersonFruit>, PPF>() {

            @Override
            public PPF apply(Person t1, Observable<PersonFruit> t2) {
                return new PPF(t1, t2);
            }
        });
        q.subscribe(new Observer<PPF>() {

            @Override
            public void onNext(final PPF ppf) {
                ppf.fruits.filter(new Predicate<PersonFruit>() {

                    @Override
                    public boolean test(PersonFruit t1) {
                        return ppf.person.id == t1.personId;
                    }
                }).subscribe(new Consumer<PersonFruit>() {

                    @Override
                    public void accept(PersonFruit t1) {
                        observer.onNext(Arrays.asList(ppf.person.name, t1.fruit));
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                observer.onError(e);
            }

            @Override
            public void onComplete() {
                observer.onComplete();
            }

            @Override
            public void onSubscribe(Disposable d) {
            }
        });
        verify(observer, times(1)).onNext(Arrays.asList("Joe", "Strawberry"));
        verify(observer, times(1)).onNext(Arrays.asList("Joe", "Apple"));
        verify(observer, times(1)).onNext(Arrays.asList("Charlie", "Peach"));
        verify(observer, times(1)).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void leftThrows() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Observable<Observable<Integer>> m = source1.groupJoin(source2, just(Observable.never()), just(Observable.never()), add2);
        m.subscribe(observer);
        source2.onNext(1);
        source1.onError(new RuntimeException("Forced failure"));
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onNext(any());
    }

    @Test
    public void rightThrows() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Observable<Observable<Integer>> m = source1.groupJoin(source2, just(Observable.never()), just(Observable.never()), add2);
        m.subscribe(observer);
        source1.onNext(1);
        source2.onError(new RuntimeException("Forced failure"));
        verify(observer, times(1)).onNext(any(Observable.class));
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
    }

    @Test
    public void leftDurationThrows() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Observable<Integer> duration1 = Observable.<Integer>error(new RuntimeException("Forced failure"));
        Observable<Observable<Integer>> m = source1.groupJoin(source2, just(duration1), just(Observable.never()), add2);
        m.subscribe(observer);
        source1.onNext(1);
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onNext(any());
    }

    @Test
    public void rightDurationThrows() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Observable<Integer> duration1 = Observable.<Integer>error(new RuntimeException("Forced failure"));
        Observable<Observable<Integer>> m = source1.groupJoin(source2, just(Observable.never()), just(duration1), add2);
        m.subscribe(observer);
        source2.onNext(1);
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onNext(any());
    }

    @Test
    public void leftDurationSelectorThrows() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Function<Integer, Observable<Integer>> fail = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                throw new RuntimeException("Forced failure");
            }
        };
        Observable<Observable<Integer>> m = source1.groupJoin(source2, fail, just(Observable.never()), add2);
        m.subscribe(observer);
        source1.onNext(1);
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onNext(any());
    }

    @Test
    public void rightDurationSelectorThrows() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        Function<Integer, Observable<Integer>> fail = new Function<Integer, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer t1) {
                throw new RuntimeException("Forced failure");
            }
        };
        Observable<Observable<Integer>> m = source1.groupJoin(source2, just(Observable.never()), fail, add2);
        m.subscribe(observer);
        source2.onNext(1);
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onNext(any());
    }

    @Test
    public void resultSelectorThrows() {
        PublishSubject<Integer> source1 = PublishSubject.create();
        PublishSubject<Integer> source2 = PublishSubject.create();
        BiFunction<Integer, Observable<Integer>, Integer> fail = new BiFunction<Integer, Observable<Integer>, Integer>() {

            @Override
            public Integer apply(Integer t1, Observable<Integer> t2) {
                throw new RuntimeException("Forced failure");
            }
        };
        Observable<Integer> m = source1.groupJoin(source2, just(Observable.never()), just(Observable.never()), fail);
        m.subscribe(observer);
        source1.onNext(1);
        source2.onNext(2);
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onComplete();
        verify(observer, never()).onNext(any());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Observable.just(1).groupJoin(Observable.just(2), new Function<Integer, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Integer left) throws Exception {
                return Observable.never();
            }
        }, new Function<Integer, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Integer right) throws Exception {
                return Observable.never();
            }
        }, new BiFunction<Integer, Observable<Integer>, Object>() {

            @Override
            public Object apply(Integer r, Observable<Integer> l) throws Exception {
                return l;
            }
        }));
    }

    @Test
    public void innerCompleteLeft() {
        Observable.just(1).groupJoin(Observable.just(2), new Function<Integer, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Integer left) throws Exception {
                return Observable.empty();
            }
        }, new Function<Integer, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Integer right) throws Exception {
                return Observable.never();
            }
        }, new BiFunction<Integer, Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer r, Observable<Integer> l) throws Exception {
                return l;
            }
        }).flatMap(Functions.<Observable<Integer>>identity()).test().assertResult();
    }

    @Test
    public void innerErrorLeft() {
        Observable.just(1).groupJoin(Observable.just(2), new Function<Integer, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Integer left) throws Exception {
                return Observable.error(new TestException());
            }
        }, new Function<Integer, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Integer right) throws Exception {
                return Observable.never();
            }
        }, new BiFunction<Integer, Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer r, Observable<Integer> l) throws Exception {
                return l;
            }
        }).flatMap(Functions.<Observable<Integer>>identity()).test().assertFailure(TestException.class);
    }

    @Test
    public void innerCompleteRight() {
        Observable.just(1).groupJoin(Observable.just(2), new Function<Integer, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Integer left) throws Exception {
                return Observable.never();
            }
        }, new Function<Integer, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Integer right) throws Exception {
                return Observable.empty();
            }
        }, new BiFunction<Integer, Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer r, Observable<Integer> l) throws Exception {
                return l;
            }
        }).flatMap(Functions.<Observable<Integer>>identity()).test().assertResult(2);
    }

    @Test
    @SuppressUndeliverable
    public void innerErrorRight() {
        Observable.just(1).groupJoin(Observable.just(2), new Function<Integer, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Integer left) throws Exception {
                return Observable.never();
            }
        }, new Function<Integer, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Integer right) throws Exception {
                return Observable.error(new TestException());
            }
        }, new BiFunction<Integer, Observable<Integer>, Observable<Integer>>() {

            @Override
            public Observable<Integer> apply(Integer r, Observable<Integer> l) throws Exception {
                return l;
            }
        }).flatMap(Functions.<Observable<Integer>>identity()).test().assertFailure(TestException.class);
    }

    @Test
    public void innerErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Object> ps1 = PublishSubject.create();
            final PublishSubject<Object> ps2 = PublishSubject.create();
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                TestObserverEx<Observable<Integer>> to = Observable.just(1).groupJoin(Observable.just(2).concatWith(Observable.<Integer>never()), new Function<Integer, ObservableSource<Object>>() {

                    @Override
                    public ObservableSource<Object> apply(Integer left) throws Exception {
                        return ps1;
                    }
                }, new Function<Integer, ObservableSource<Object>>() {

                    @Override
                    public ObservableSource<Object> apply(Integer right) throws Exception {
                        return ps2;
                    }
                }, new BiFunction<Integer, Observable<Integer>, Observable<Integer>>() {

                    @Override
                    public Observable<Integer> apply(Integer r, Observable<Integer> l) throws Exception {
                        return l;
                    }
                }).to(TestHelper.<Observable<Integer>>testConsumer());
                final TestException ex1 = new TestException();
                final TestException ex2 = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps1.onError(ex1);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ps2.onError(ex2);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertError(Throwable.class).assertSubscribed().assertNotComplete().assertValueCount(1);
                Throwable exc = to.errors().get(0);
                if (exc instanceof CompositeException) {
                    List<Throwable> es = TestHelper.compositeList(exc);
                    TestHelper.assertError(es, 0, TestException.class);
                    TestHelper.assertError(es, 1, TestException.class);
                } else {
                    to.assertError(TestException.class);
                }
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void outerErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishSubject<Object> ps1 = PublishSubject.create();
            final PublishSubject<Object> ps2 = PublishSubject.create();
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                TestObserverEx<Object> to = ps1.groupJoin(ps2, new Function<Object, ObservableSource<Object>>() {

                    @Override
                    public ObservableSource<Object> apply(Object left) throws Exception {
                        return Observable.never();
                    }
                }, new Function<Object, ObservableSource<Object>>() {

                    @Override
                    public ObservableSource<Object> apply(Object right) throws Exception {
                        return Observable.never();
                    }
                }, new BiFunction<Object, Observable<Object>, Observable<Object>>() {

                    @Override
                    public Observable<Object> apply(Object r, Observable<Object> l) throws Exception {
                        return l;
                    }
                }).flatMap(Functions.<Observable<Object>>identity()).to(TestHelper.<Object>testConsumer());
                final TestException ex1 = new TestException();
                final TestException ex2 = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        ps1.onError(ex1);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        ps2.onError(ex2);
                    }
                };
                TestHelper.race(r1, r2);
                to.assertError(Throwable.class).assertSubscribed().assertNotComplete().assertNoValues();
                Throwable exc = to.errors().get(0);
                if (exc instanceof CompositeException) {
                    List<Throwable> es = TestHelper.compositeList(exc);
                    TestHelper.assertError(es, 0, TestException.class);
                    TestHelper.assertError(es, 1, TestException.class);
                } else {
                    to.assertError(TestException.class);
                }
                if (!errors.isEmpty()) {
                    TestHelper.assertUndeliverable(errors, 0, TestException.class);
                }
            } finally {
                RxJavaPlugins.reset();
            }
        }
    }

    @Test
    public void rightEmission() {
        final PublishSubject<Object> ps1 = PublishSubject.create();
        final PublishSubject<Object> ps2 = PublishSubject.create();
        TestObserver<Object> to = ps1.groupJoin(ps2, new Function<Object, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Object left) throws Exception {
                return Observable.never();
            }
        }, new Function<Object, ObservableSource<Object>>() {

            @Override
            public ObservableSource<Object> apply(Object right) throws Exception {
                return Observable.never();
            }
        }, new BiFunction<Object, Observable<Object>, Observable<Object>>() {

            @Override
            public Observable<Object> apply(Object r, Observable<Object> l) throws Exception {
                return l;
            }
        }).flatMap(Functions.<Observable<Object>>identity()).test();
        ps2.onNext(2);
        ps1.onNext(1);
        ps1.onComplete();
        ps2.onComplete();
        to.assertResult(2);
    }

    @Test
    public void leftRightState() {
        JoinSupport js = mock(JoinSupport.class);
        LeftRightObserver o = new LeftRightObserver(js, false);
        assertFalse(o.isDisposed());
        o.onNext(1);
        o.onNext(2);
        o.dispose();
        assertTrue(o.isDisposed());
        verify(js).innerValue(false, 1);
        verify(js).innerValue(false, 2);
    }

    @Test
    public void leftRightEndState() {
        JoinSupport js = mock(JoinSupport.class);
        LeftRightEndObserver o = new LeftRightEndObserver(js, false, 0);
        assertFalse(o.isDisposed());
        o.onNext(1);
        o.onNext(2);
        assertTrue(o.isDisposed());
        verify(js).innerClose(false, o);
    }

    @Test
    public void disposeAfterOnNext() {
        PublishSubject<Integer> ps1 = PublishSubject.create();
        PublishSubject<Integer> ps2 = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<>();
        ps1.groupJoin(ps2, v -> Observable.never(), v -> Observable.never(), (a, b) -> a).doOnNext(v -> {
            to.dispose();
        }).subscribe(to);
        ps2.onNext(1);
        ps1.onNext(1);
    }

    @Test
    public void completeWithMoreWork() {
        PublishSubject<Integer> ps1 = PublishSubject.create();
        PublishSubject<Integer> ps2 = PublishSubject.create();
        TestObserver<Integer> to = new TestObserver<>();
        ps1.groupJoin(ps2, v -> Observable.never(), v -> Observable.never(), (a, b) -> a).doOnNext(v -> {
            if (v == 1) {
                ps2.onNext(2);
                ps1.onComplete();
                ps2.onComplete();
            }
        }).subscribe(to);
        ps2.onNext(1);
        ps1.onNext(1);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

public _Payloads payloads;

        public ObservableGroupJoinTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_behaveAsJoin() throws java.lang.Throwable {
            this.payloads.behaveAsJoin.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_normal1() throws java.lang.Throwable {
            this.payloads.normal1.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_leftThrows() throws java.lang.Throwable {
            this.payloads.leftThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rightThrows() throws java.lang.Throwable {
            this.payloads.rightThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_leftDurationThrows() throws java.lang.Throwable {
            this.payloads.leftDurationThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rightDurationThrows() throws java.lang.Throwable {
            this.payloads.rightDurationThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_leftDurationSelectorThrows() throws java.lang.Throwable {
            this.payloads.leftDurationSelectorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rightDurationSelectorThrows() throws java.lang.Throwable {
            this.payloads.rightDurationSelectorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_resultSelectorThrows() throws java.lang.Throwable {
            this.payloads.resultSelectorThrows.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dispose() throws java.lang.Throwable {
            this.payloads.dispose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerCompleteLeft() throws java.lang.Throwable {
            this.payloads.innerCompleteLeft.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorLeft() throws java.lang.Throwable {
            this.payloads.innerErrorLeft.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerCompleteRight() throws java.lang.Throwable {
            this.payloads.innerCompleteRight.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorRight() throws java.lang.Throwable {
            this.payloads.innerErrorRight.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_innerErrorRace() throws java.lang.Throwable {
            this.payloads.innerErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_outerErrorRace() throws java.lang.Throwable {
            this.payloads.outerErrorRace.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_rightEmission() throws java.lang.Throwable {
            this.payloads.rightEmission.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_leftRightState() throws java.lang.Throwable {
            this.payloads.leftRightState.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_leftRightEndState() throws java.lang.Throwable {
            this.payloads.leftRightEndState.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_disposeAfterOnNext() throws java.lang.Throwable {
            this.payloads.disposeAfterOnNext.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_completeWithMoreWork() throws java.lang.Throwable {
            this.payloads.completeWithMoreWork.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableGroupJoinTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableGroupJoinTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance.before();
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableGroupJoinTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableGroupJoinTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableGroupJoinTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableGroupJoinTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableGroupJoinTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableGroupJoinTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        public static class _Payloads {

            public org.junit.runners.model.Statement behaveAsJoin;

            public org.junit.runners.model.Statement normal1;

            public org.junit.runners.model.Statement leftThrows;

            public org.junit.runners.model.Statement rightThrows;

            public org.junit.runners.model.Statement leftDurationThrows;

            public org.junit.runners.model.Statement rightDurationThrows;

            public org.junit.runners.model.Statement leftDurationSelectorThrows;

            public org.junit.runners.model.Statement rightDurationSelectorThrows;

            public org.junit.runners.model.Statement resultSelectorThrows;

            public org.junit.runners.model.Statement dispose;

            public org.junit.runners.model.Statement innerCompleteLeft;

            public org.junit.runners.model.Statement innerErrorLeft;

            public org.junit.runners.model.Statement innerCompleteRight;

            public org.junit.runners.model.Statement innerErrorRight;

            public org.junit.runners.model.Statement innerErrorRace;

            public org.junit.runners.model.Statement outerErrorRace;

            public org.junit.runners.model.Statement rightEmission;

            public org.junit.runners.model.Statement leftRightState;

            public org.junit.runners.model.Statement leftRightEndState;

            public org.junit.runners.model.Statement disposeAfterOnNext;

            public org.junit.runners.model.Statement completeWithMoreWork;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.behaveAsJoin = _ClassStatement.forPayload(ObservableGroupJoinTest::behaveAsJoin, "behaveAsJoin", this);
            this.payloads.normal1 = _ClassStatement.forPayload(ObservableGroupJoinTest::normal1, "normal1", this);
            this.payloads.leftThrows = _ClassStatement.forPayload(ObservableGroupJoinTest::leftThrows, "leftThrows", this);
            this.payloads.rightThrows = _ClassStatement.forPayload(ObservableGroupJoinTest::rightThrows, "rightThrows", this);
            this.payloads.leftDurationThrows = _ClassStatement.forPayload(ObservableGroupJoinTest::leftDurationThrows, "leftDurationThrows", this);
            this.payloads.rightDurationThrows = _ClassStatement.forPayload(ObservableGroupJoinTest::rightDurationThrows, "rightDurationThrows", this);
            this.payloads.leftDurationSelectorThrows = _ClassStatement.forPayload(ObservableGroupJoinTest::leftDurationSelectorThrows, "leftDurationSelectorThrows", this);
            this.payloads.rightDurationSelectorThrows = _ClassStatement.forPayload(ObservableGroupJoinTest::rightDurationSelectorThrows, "rightDurationSelectorThrows", this);
            this.payloads.resultSelectorThrows = _ClassStatement.forPayload(ObservableGroupJoinTest::resultSelectorThrows, "resultSelectorThrows", this);
            this.payloads.dispose = _ClassStatement.forPayload(ObservableGroupJoinTest::dispose, "dispose", this);
            this.payloads.innerCompleteLeft = _ClassStatement.forPayload(ObservableGroupJoinTest::innerCompleteLeft, "innerCompleteLeft", this);
            this.payloads.innerErrorLeft = _ClassStatement.forPayload(ObservableGroupJoinTest::innerErrorLeft, "innerErrorLeft", this);
            this.payloads.innerCompleteRight = _ClassStatement.forPayload(ObservableGroupJoinTest::innerCompleteRight, "innerCompleteRight", this);
            this.payloads.innerErrorRight = _ClassStatement.forPayload(ObservableGroupJoinTest::innerErrorRight, "innerErrorRight", this);
            this.payloads.innerErrorRace = _ClassStatement.forPayload(ObservableGroupJoinTest::innerErrorRace, "innerErrorRace", this);
            this.payloads.outerErrorRace = _ClassStatement.forPayload(ObservableGroupJoinTest::outerErrorRace, "outerErrorRace", this);
            this.payloads.rightEmission = _ClassStatement.forPayload(ObservableGroupJoinTest::rightEmission, "rightEmission", this);
            this.payloads.leftRightState = _ClassStatement.forPayload(ObservableGroupJoinTest::leftRightState, "leftRightState", this);
            this.payloads.leftRightEndState = _ClassStatement.forPayload(ObservableGroupJoinTest::leftRightEndState, "leftRightEndState", this);
            this.payloads.disposeAfterOnNext = _ClassStatement.forPayload(ObservableGroupJoinTest::disposeAfterOnNext, "disposeAfterOnNext", this);
            this.payloads.completeWithMoreWork = _ClassStatement.forPayload(ObservableGroupJoinTest::completeWithMoreWork, "completeWithMoreWork", this);
        }
    }
}
