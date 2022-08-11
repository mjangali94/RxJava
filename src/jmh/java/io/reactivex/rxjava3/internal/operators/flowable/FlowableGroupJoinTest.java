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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.*;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.*;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.*;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableGroupJoin.*;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.*;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import io.reactivex.rxjava3.testsupport.*;

public class FlowableGroupJoinTest extends RxJavaTest {

    Subscriber<Object> subscriber = TestHelper.mockSubscriber();

    BiFunction<Integer, Integer, Integer> add = new BiFunction<Integer, Integer, Integer>() {

        @Override
        public Integer apply(Integer t1, Integer t2) {
            return t1 + t2;
        }
    };

    <T> Function<Integer, Flowable<T>> just(final Flowable<T> flowable) {
        return new Function<Integer, Flowable<T>>() {

            @Override
            public Flowable<T> apply(Integer t1) {
                return flowable;
            }
        };
    }

    <T, R> Function<T, Flowable<R>> just2(final Flowable<R> flowable) {
        return new Function<T, Flowable<R>>() {

            @Override
            public Flowable<R> apply(T t1) {
                return flowable;
            }
        };
    }

    BiFunction<Integer, Flowable<Integer>, Flowable<Integer>> add2 = new BiFunction<Integer, Flowable<Integer>, Flowable<Integer>>() {

        @Override
        public Flowable<Integer> apply(final Integer leftValue, Flowable<Integer> rightValues) {
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
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Flowable<Integer> m = Flowable.merge(source1.groupJoin(source2, just(Flowable.never()), just(Flowable.never()), add2));
        m.subscribe(subscriber);
        source1.onNext(1);
        source1.onNext(2);
        source1.onNext(4);
        source2.onNext(16);
        source2.onNext(32);
        source2.onNext(64);
        source1.onComplete();
        source2.onComplete();
        verify(subscriber, times(1)).onNext(17);
        verify(subscriber, times(1)).onNext(18);
        verify(subscriber, times(1)).onNext(20);
        verify(subscriber, times(1)).onNext(33);
        verify(subscriber, times(1)).onNext(34);
        verify(subscriber, times(1)).onNext(36);
        verify(subscriber, times(1)).onNext(65);
        verify(subscriber, times(1)).onNext(66);
        verify(subscriber, times(1)).onNext(68);
        // Never emitted?
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
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

        final Flowable<PersonFruit> fruits;

        PPF(Person person, Flowable<PersonFruit> fruits) {
            this.person = person;
            this.fruits = fruits;
        }
    }

    @Test
    public void normal1() {
        Flowable<Person> source1 = Flowable.fromIterable(Arrays.asList(new Person(1, "Joe"), new Person(2, "Mike"), new Person(3, "Charlie")));
        Flowable<PersonFruit> source2 = Flowable.fromIterable(Arrays.asList(new PersonFruit(1, "Strawberry"), new PersonFruit(1, "Apple"), new PersonFruit(3, "Peach")));
        Flowable<PPF> q = source1.groupJoin(source2, just2(Flowable.<Object>never()), just2(Flowable.<Object>never()), new BiFunction<Person, Flowable<PersonFruit>, PPF>() {

            @Override
            public PPF apply(Person t1, Flowable<PersonFruit> t2) {
                return new PPF(t1, t2);
            }
        });
        q.subscribe(new FlowableSubscriber<PPF>() {

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
                        subscriber.onNext(Arrays.asList(ppf.person.name, t1.fruit));
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onComplete() {
                subscriber.onComplete();
            }

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }
        });
        verify(subscriber, times(1)).onNext(Arrays.asList("Joe", "Strawberry"));
        verify(subscriber, times(1)).onNext(Arrays.asList("Joe", "Apple"));
        verify(subscriber, times(1)).onNext(Arrays.asList("Charlie", "Peach"));
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, never()).onError(any(Throwable.class));
    }

    @Test
    public void leftThrows() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Flowable<Flowable<Integer>> m = source1.groupJoin(source2, just(Flowable.never()), just(Flowable.never()), add2);
        m.subscribe(subscriber);
        source2.onNext(1);
        source1.onError(new RuntimeException("Forced failure"));
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void rightThrows() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Flowable<Flowable<Integer>> m = source1.groupJoin(source2, just(Flowable.never()), just(Flowable.never()), add2);
        m.subscribe(subscriber);
        source1.onNext(1);
        source2.onError(new RuntimeException("Forced failure"));
        verify(subscriber, times(1)).onNext(any(Flowable.class));
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
    }

    @Test
    public void leftDurationThrows() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Flowable<Integer> duration1 = Flowable.<Integer>error(new RuntimeException("Forced failure"));
        Flowable<Flowable<Integer>> m = source1.groupJoin(source2, just(duration1), just(Flowable.never()), add2);
        m.subscribe(subscriber);
        source1.onNext(1);
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void rightDurationThrows() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Flowable<Integer> duration1 = Flowable.<Integer>error(new RuntimeException("Forced failure"));
        Flowable<Flowable<Integer>> m = source1.groupJoin(source2, just(Flowable.never()), just(duration1), add2);
        m.subscribe(subscriber);
        source2.onNext(1);
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void leftDurationSelectorThrows() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> fail = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                throw new RuntimeException("Forced failure");
            }
        };
        Flowable<Flowable<Integer>> m = source1.groupJoin(source2, fail, just(Flowable.never()), add2);
        m.subscribe(subscriber);
        source1.onNext(1);
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void rightDurationSelectorThrows() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        Function<Integer, Flowable<Integer>> fail = new Function<Integer, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer t1) {
                throw new RuntimeException("Forced failure");
            }
        };
        Flowable<Flowable<Integer>> m = source1.groupJoin(source2, just(Flowable.never()), fail, add2);
        m.subscribe(subscriber);
        source2.onNext(1);
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void resultSelectorThrows() {
        PublishProcessor<Integer> source1 = PublishProcessor.create();
        PublishProcessor<Integer> source2 = PublishProcessor.create();
        BiFunction<Integer, Flowable<Integer>, Integer> fail = new BiFunction<Integer, Flowable<Integer>, Integer>() {

            @Override
            public Integer apply(Integer t1, Flowable<Integer> t2) {
                throw new RuntimeException("Forced failure");
            }
        };
        Flowable<Integer> m = source1.groupJoin(source2, just(Flowable.never()), just(Flowable.never()), fail);
        m.subscribe(subscriber);
        source1.onNext(1);
        source2.onNext(2);
        verify(subscriber, times(1)).onError(any(Throwable.class));
        verify(subscriber, never()).onComplete();
        verify(subscriber, never()).onNext(any());
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(Flowable.just(1).groupJoin(Flowable.just(2), new Function<Integer, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Integer left) throws Exception {
                return Flowable.never();
            }
        }, new Function<Integer, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Integer right) throws Exception {
                return Flowable.never();
            }
        }, new BiFunction<Integer, Flowable<Integer>, Object>() {

            @Override
            public Object apply(Integer r, Flowable<Integer> l) throws Exception {
                return l;
            }
        }));
    }

    @Test
    public void innerCompleteLeft() {
        Flowable.just(1).groupJoin(Flowable.just(2), new Function<Integer, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Integer left) throws Exception {
                return Flowable.empty();
            }
        }, new Function<Integer, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Integer right) throws Exception {
                return Flowable.never();
            }
        }, new BiFunction<Integer, Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer r, Flowable<Integer> l) throws Exception {
                return l;
            }
        }).flatMap(Functions.<Flowable<Integer>>identity()).test().assertResult();
    }

    @Test
    public void innerErrorLeft() {
        Flowable.just(1).groupJoin(Flowable.just(2), new Function<Integer, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Integer left) throws Exception {
                return Flowable.error(new TestException());
            }
        }, new Function<Integer, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Integer right) throws Exception {
                return Flowable.never();
            }
        }, new BiFunction<Integer, Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer r, Flowable<Integer> l) throws Exception {
                return l;
            }
        }).flatMap(Functions.<Flowable<Integer>>identity()).test().assertFailure(TestException.class);
    }

    @Test
    public void innerCompleteRight() {
        Flowable.just(1).groupJoin(Flowable.just(2), new Function<Integer, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Integer left) throws Exception {
                return Flowable.never();
            }
        }, new Function<Integer, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Integer right) throws Exception {
                return Flowable.empty();
            }
        }, new BiFunction<Integer, Flowable<Integer>, Flowable<Integer>>() {

            @Override
            public Flowable<Integer> apply(Integer r, Flowable<Integer> l) throws Exception {
                return l;
            }
        }).flatMap(Functions.<Flowable<Integer>>identity()).test().assertResult(2);
    }

    @Test
    public void innerErrorRight() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            Flowable.just(1).groupJoin(Flowable.just(2), new Function<Integer, Flowable<Object>>() {

                @Override
                public Flowable<Object> apply(Integer left) throws Exception {
                    return Flowable.never();
                }
            }, new Function<Integer, Flowable<Object>>() {

                @Override
                public Flowable<Object> apply(Integer right) throws Exception {
                    return Flowable.error(new TestException());
                }
            }, new BiFunction<Integer, Flowable<Integer>, Flowable<Integer>>() {

                @Override
                public Flowable<Integer> apply(Integer r, Flowable<Integer> l) throws Exception {
                    return l;
                }
            }).flatMap(Functions.<Flowable<Integer>>identity()).test().assertFailure(TestException.class);
            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void innerErrorRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            final PublishProcessor<Object> pp1 = PublishProcessor.create();
            final PublishProcessor<Object> pp2 = PublishProcessor.create();
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                TestSubscriberEx<Flowable<Integer>> ts = Flowable.just(1).groupJoin(Flowable.just(2).concatWith(Flowable.<Integer>never()), new Function<Integer, Flowable<Object>>() {

                    @Override
                    public Flowable<Object> apply(Integer left) throws Exception {
                        return pp1;
                    }
                }, new Function<Integer, Flowable<Object>>() {

                    @Override
                    public Flowable<Object> apply(Integer right) throws Exception {
                        return pp2;
                    }
                }, new BiFunction<Integer, Flowable<Integer>, Flowable<Integer>>() {

                    @Override
                    public Flowable<Integer> apply(Integer r, Flowable<Integer> l) throws Exception {
                        return l;
                    }
                }).to(TestHelper.<Flowable<Integer>>testConsumer());
                final TestException ex1 = new TestException();
                final TestException ex2 = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp1.onError(ex1);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        pp2.onError(ex2);
                    }
                };
                TestHelper.race(r1, r2);
                ts.assertError(Throwable.class).assertSubscribed().assertNotComplete().assertValueCount(1);
                Throwable exc = ts.errors().get(0);
                if (exc instanceof CompositeException) {
                    List<Throwable> es = TestHelper.compositeList(exc);
                    TestHelper.assertError(es, 0, TestException.class);
                    TestHelper.assertError(es, 1, TestException.class);
                } else {
                    ts.assertError(TestException.class);
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
            final PublishProcessor<Object> pp1 = PublishProcessor.create();
            final PublishProcessor<Object> pp2 = PublishProcessor.create();
            List<Throwable> errors = TestHelper.trackPluginErrors();
            try {
                TestSubscriberEx<Object> ts = pp1.groupJoin(pp2, new Function<Object, Flowable<Object>>() {

                    @Override
                    public Flowable<Object> apply(Object left) throws Exception {
                        return Flowable.never();
                    }
                }, new Function<Object, Flowable<Object>>() {

                    @Override
                    public Flowable<Object> apply(Object right) throws Exception {
                        return Flowable.never();
                    }
                }, new BiFunction<Object, Flowable<Object>, Flowable<Object>>() {

                    @Override
                    public Flowable<Object> apply(Object r, Flowable<Object> l) throws Exception {
                        return l;
                    }
                }).flatMap(Functions.<Flowable<Object>>identity()).to(TestHelper.<Object>testConsumer());
                final TestException ex1 = new TestException();
                final TestException ex2 = new TestException();
                Runnable r1 = new Runnable() {

                    @Override
                    public void run() {
                        pp1.onError(ex1);
                    }
                };
                Runnable r2 = new Runnable() {

                    @Override
                    public void run() {
                        pp2.onError(ex2);
                    }
                };
                TestHelper.race(r1, r2);
                ts.assertError(Throwable.class).assertSubscribed().assertNotComplete().assertNoValues();
                Throwable exc = ts.errors().get(0);
                if (exc instanceof CompositeException) {
                    List<Throwable> es = TestHelper.compositeList(exc);
                    TestHelper.assertError(es, 0, TestException.class);
                    TestHelper.assertError(es, 1, TestException.class);
                } else {
                    ts.assertError(TestException.class);
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
        final PublishProcessor<Object> pp1 = PublishProcessor.create();
        final PublishProcessor<Object> pp2 = PublishProcessor.create();
        TestSubscriber<Object> ts = pp1.groupJoin(pp2, new Function<Object, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Object left) throws Exception {
                return Flowable.never();
            }
        }, new Function<Object, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Object right) throws Exception {
                return Flowable.never();
            }
        }, new BiFunction<Object, Flowable<Object>, Flowable<Object>>() {

            @Override
            public Flowable<Object> apply(Object r, Flowable<Object> l) throws Exception {
                return l;
            }
        }).flatMap(Functions.<Flowable<Object>>identity()).test();
        pp2.onNext(2);
        pp1.onNext(1);
        pp1.onComplete();
        pp2.onComplete();
        ts.assertResult(2);
    }

    @Test
    public void leftRightState() {
        JoinSupport js = mock(JoinSupport.class);
        LeftRightSubscriber o = new LeftRightSubscriber(js, false);
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
        LeftRightEndSubscriber o = new LeftRightEndSubscriber(js, false, 0);
        assertFalse(o.isDisposed());
        o.onNext(1);
        o.onNext(2);
        assertTrue(o.isDisposed());
        verify(js).innerClose(false, o);
    }

    @Test
    public void disposeAfterOnNext() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        pp1.groupJoin(pp2, v -> Flowable.never(), v -> Flowable.never(), (a, b) -> a).doOnNext(v -> {
            ts.cancel();
        }).subscribe(ts);
        pp2.onNext(1);
        pp1.onNext(1);
    }

    @Test
    public void completeWithMoreWork() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        TestSubscriber<Integer> ts = new TestSubscriber<>();
        pp1.groupJoin(pp2, v -> Flowable.never(), v -> Flowable.never(), (a, b) -> a).doOnNext(v -> {
            if (v == 1) {
                pp2.onNext(2);
                pp1.onComplete();
                pp2.onComplete();
            }
        }).subscribe(ts);
        pp2.onNext(1);
        pp1.onNext(1);
    }

    @Test
    public void badRequest() {
        TestHelper.assertBadRequestReported(Flowable.never().groupJoin(Flowable.never(), v -> Flowable.never(), v -> Flowable.never(), (a, b) -> a));
    }

    @Test
    public void missingBackpressure() {
        Flowable.just(1).groupJoin(Flowable.never(), v -> BehaviorProcessor.createDefault(1), v -> Flowable.never(), (a, b) -> a).test(0).assertFailure(MissingBackpressureException.class);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private FlowableGroupJoinTest instance;

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

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_badRequest() throws java.lang.Throwable {
            this.payloads.badRequest.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_missingBackpressure() throws java.lang.Throwable {
            this.payloads.missingBackpressure.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupJoinTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupJoinTest> payload, _Benchmark benchmark) {
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

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupJoinTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupJoinTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new FlowableGroupJoinTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<FlowableGroupJoinTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(FlowableGroupJoinTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(FlowableGroupJoinTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

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

            public org.junit.runners.model.Statement badRequest;

            public org.junit.runners.model.Statement missingBackpressure;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.behaveAsJoin = _ClassStatement.forPayload(FlowableGroupJoinTest::behaveAsJoin, "behaveAsJoin", this);
            this.payloads.normal1 = _ClassStatement.forPayload(FlowableGroupJoinTest::normal1, "normal1", this);
            this.payloads.leftThrows = _ClassStatement.forPayload(FlowableGroupJoinTest::leftThrows, "leftThrows", this);
            this.payloads.rightThrows = _ClassStatement.forPayload(FlowableGroupJoinTest::rightThrows, "rightThrows", this);
            this.payloads.leftDurationThrows = _ClassStatement.forPayload(FlowableGroupJoinTest::leftDurationThrows, "leftDurationThrows", this);
            this.payloads.rightDurationThrows = _ClassStatement.forPayload(FlowableGroupJoinTest::rightDurationThrows, "rightDurationThrows", this);
            this.payloads.leftDurationSelectorThrows = _ClassStatement.forPayload(FlowableGroupJoinTest::leftDurationSelectorThrows, "leftDurationSelectorThrows", this);
            this.payloads.rightDurationSelectorThrows = _ClassStatement.forPayload(FlowableGroupJoinTest::rightDurationSelectorThrows, "rightDurationSelectorThrows", this);
            this.payloads.resultSelectorThrows = _ClassStatement.forPayload(FlowableGroupJoinTest::resultSelectorThrows, "resultSelectorThrows", this);
            this.payloads.dispose = _ClassStatement.forPayload(FlowableGroupJoinTest::dispose, "dispose", this);
            this.payloads.innerCompleteLeft = _ClassStatement.forPayload(FlowableGroupJoinTest::innerCompleteLeft, "innerCompleteLeft", this);
            this.payloads.innerErrorLeft = _ClassStatement.forPayload(FlowableGroupJoinTest::innerErrorLeft, "innerErrorLeft", this);
            this.payloads.innerCompleteRight = _ClassStatement.forPayload(FlowableGroupJoinTest::innerCompleteRight, "innerCompleteRight", this);
            this.payloads.innerErrorRight = _ClassStatement.forPayload(FlowableGroupJoinTest::innerErrorRight, "innerErrorRight", this);
            this.payloads.innerErrorRace = _ClassStatement.forPayload(FlowableGroupJoinTest::innerErrorRace, "innerErrorRace", this);
            this.payloads.outerErrorRace = _ClassStatement.forPayload(FlowableGroupJoinTest::outerErrorRace, "outerErrorRace", this);
            this.payloads.rightEmission = _ClassStatement.forPayload(FlowableGroupJoinTest::rightEmission, "rightEmission", this);
            this.payloads.leftRightState = _ClassStatement.forPayload(FlowableGroupJoinTest::leftRightState, "leftRightState", this);
            this.payloads.leftRightEndState = _ClassStatement.forPayload(FlowableGroupJoinTest::leftRightEndState, "leftRightEndState", this);
            this.payloads.disposeAfterOnNext = _ClassStatement.forPayload(FlowableGroupJoinTest::disposeAfterOnNext, "disposeAfterOnNext", this);
            this.payloads.completeWithMoreWork = _ClassStatement.forPayload(FlowableGroupJoinTest::completeWithMoreWork, "completeWithMoreWork", this);
            this.payloads.badRequest = _ClassStatement.forPayload(FlowableGroupJoinTest::badRequest, "badRequest", this);
            this.payloads.missingBackpressure = _ClassStatement.forPayload(FlowableGroupJoinTest::missingBackpressure, "missingBackpressure", this);
        }
    }
}
