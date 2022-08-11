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
package io.reactivex.rxjava3.internal.operators.single;

import static org.junit.Assert.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.exceptions.TestException;
import io.reactivex.rxjava3.functions.*;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SingleMiscTest extends RxJavaTest {

    @Test
    public void never() {
        Single.never().test().assertNoValues().assertNoErrors().assertNotComplete();
    }

    @Test
    public void timer() throws Exception {
        Single.timer(100, TimeUnit.MILLISECONDS).test().awaitDone(5, TimeUnit.SECONDS).assertResult(0L);
    }

    @Test
    public void wrap() {
        assertSame(Single.never(), Single.wrap(Single.never()));
        Single.wrap(new SingleSource<Object>() {

            @Override
            public void subscribe(SingleObserver<? super Object> observer) {
                observer.onSubscribe(Disposable.empty());
                observer.onSuccess(1);
            }
        }).test().assertResult(1);
    }

    @Test
    public void cast() {
        Single<Number> source = Single.just(1d).cast(Number.class);
        source.test().assertResult((Number) 1d);
    }

    @Test
    public void contains() {
        Single.just(1).contains(1).test().assertResult(true);
        Single.just(2).contains(1).test().assertResult(false);
    }

    @Test
    public void compose() {
        Single.just(1).compose(new SingleTransformer<Integer, Object>() {

            @Override
            public SingleSource<Object> apply(Single<Integer> f) {
                return f.map(new Function<Integer, Object>() {

                    @Override
                    public Object apply(Integer v) throws Exception {
                        return v + 1;
                    }
                });
            }
        }).test().assertResult(2);
    }

    @Test
    public void hide() {
        assertNotSame(Single.never(), Single.never().hide());
    }

    @Test
    public void onErrorResumeWith() {
        Single.<Integer>error(new TestException()).onErrorResumeWith(Single.just(1)).test().assertResult(1);
    }

    @Test
    public void onErrorReturnValue() {
        Single.<Integer>error(new TestException()).onErrorReturnItem(1).test().assertResult(1);
    }

    @Test
    public void repeat() {
        Single.just(1).repeat().take(5).test().assertResult(1, 1, 1, 1, 1);
    }

    @Test
    public void repeatTimes() {
        Single.just(1).repeat(5).test().assertResult(1, 1, 1, 1, 1);
    }

    @Test
    public void repeatUntil() {
        final AtomicBoolean flag = new AtomicBoolean();
        Single.just(1).doOnSuccess(new Consumer<Integer>() {

            int c;

            @Override
            public void accept(Integer v) throws Exception {
                if (++c == 5) {
                    flag.set(true);
                }
            }
        }).repeatUntil(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() throws Exception {
                return flag.get();
            }
        }).test().assertResult(1, 1, 1, 1, 1);
    }

    @Test
    public void retry() {
        Single.fromCallable(new Callable<Object>() {

            int c;

            @Override
            public Object call() throws Exception {
                if (++c != 5) {
                    throw new TestException();
                }
                return 1;
            }
        }).retry().test().assertResult(1);
    }

    @Test
    public void retryBiPredicate() {
        Single.fromCallable(new Callable<Object>() {

            int c;

            @Override
            public Object call() throws Exception {
                if (++c != 5) {
                    throw new TestException();
                }
                return 1;
            }
        }).retry(new BiPredicate<Integer, Throwable>() {

            @Override
            public boolean test(Integer i, Throwable e) throws Exception {
                return true;
            }
        }).test().assertResult(1);
    }

    @Test
    public void retryTimes() {
        final AtomicInteger calls = new AtomicInteger();
        Single.fromCallable(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                if (calls.incrementAndGet() != 6) {
                    throw new TestException();
                }
                return 1;
            }
        }).retry(5).test().assertResult(1);
        assertEquals(6, calls.get());
    }

    @Test
    public void retryPredicate() {
        Single.fromCallable(new Callable<Object>() {

            int c;

            @Override
            public Object call() throws Exception {
                if (++c != 5) {
                    throw new TestException();
                }
                return 1;
            }
        }).retry(new Predicate<Throwable>() {

            @Override
            public boolean test(Throwable e) throws Exception {
                return true;
            }
        }).test().assertResult(1);
    }

    @Test
    public void timeout() throws Exception {
        Single.never().timeout(100, TimeUnit.MILLISECONDS, Schedulers.io()).test().awaitDone(5, TimeUnit.SECONDS).assertFailure(TimeoutException.class);
    }

    @Test
    public void timeoutOther() throws Exception {
        Single.never().timeout(100, TimeUnit.MILLISECONDS, Schedulers.io(), Single.just(1)).test().awaitDone(5, TimeUnit.SECONDS).assertResult(1);
    }

    @Test
    public void ignoreElement() {
        Single.just(1).ignoreElement().test().assertResult();
        Single.error(new TestException()).ignoreElement().test().assertFailure(TestException.class);
    }

    @Test
    public void toObservable() {
        Single.just(1).toObservable().test().assertResult(1);
        Single.error(new TestException()).toObservable().test().assertFailure(TestException.class);
    }

    @Test
    public void equals() {
        Single.sequenceEqual(Single.just(1), Single.just(1).hide()).test().assertResult(true);
        Single.sequenceEqual(Single.just(1), Single.just(2)).test().assertResult(false);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private SingleMiscTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_never() throws java.lang.Throwable {
            this.payloads.never.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timer() throws java.lang.Throwable {
            this.payloads.timer.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_wrap() throws java.lang.Throwable {
            this.payloads.wrap.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_cast() throws java.lang.Throwable {
            this.payloads.cast.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_contains() throws java.lang.Throwable {
            this.payloads.contains.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_compose() throws java.lang.Throwable {
            this.payloads.compose.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hide() throws java.lang.Throwable {
            this.payloads.hide.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorResumeWith() throws java.lang.Throwable {
            this.payloads.onErrorResumeWith.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_onErrorReturnValue() throws java.lang.Throwable {
            this.payloads.onErrorReturnValue.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeat() throws java.lang.Throwable {
            this.payloads.repeat.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatTimes() throws java.lang.Throwable {
            this.payloads.repeatTimes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_repeatUntil() throws java.lang.Throwable {
            this.payloads.repeatUntil.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retry() throws java.lang.Throwable {
            this.payloads.retry.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryBiPredicate() throws java.lang.Throwable {
            this.payloads.retryBiPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryTimes() throws java.lang.Throwable {
            this.payloads.retryTimes.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_retryPredicate() throws java.lang.Throwable {
            this.payloads.retryPredicate.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeout() throws java.lang.Throwable {
            this.payloads.timeout.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_timeoutOther() throws java.lang.Throwable {
            this.payloads.timeoutOther.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoreElement() throws java.lang.Throwable {
            this.payloads.ignoreElement.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toObservable() throws java.lang.Throwable {
            this.payloads.toObservable.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_equals() throws java.lang.Throwable {
            this.payloads.equals.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleMiscTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleMiscTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<SingleMiscTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleMiscTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new SingleMiscTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<SingleMiscTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(SingleMiscTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(SingleMiscTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement never;

            public org.junit.runners.model.Statement timer;

            public org.junit.runners.model.Statement wrap;

            public org.junit.runners.model.Statement cast;

            public org.junit.runners.model.Statement contains;

            public org.junit.runners.model.Statement compose;

            public org.junit.runners.model.Statement hide;

            public org.junit.runners.model.Statement onErrorResumeWith;

            public org.junit.runners.model.Statement onErrorReturnValue;

            public org.junit.runners.model.Statement repeat;

            public org.junit.runners.model.Statement repeatTimes;

            public org.junit.runners.model.Statement repeatUntil;

            public org.junit.runners.model.Statement retry;

            public org.junit.runners.model.Statement retryBiPredicate;

            public org.junit.runners.model.Statement retryTimes;

            public org.junit.runners.model.Statement retryPredicate;

            public org.junit.runners.model.Statement timeout;

            public org.junit.runners.model.Statement timeoutOther;

            public org.junit.runners.model.Statement ignoreElement;

            public org.junit.runners.model.Statement toObservable;

            public org.junit.runners.model.Statement equals;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.never = _ClassStatement.forPayload(SingleMiscTest::never, "never", this);
            this.payloads.timer = _ClassStatement.forPayload(SingleMiscTest::timer, "timer", this);
            this.payloads.wrap = _ClassStatement.forPayload(SingleMiscTest::wrap, "wrap", this);
            this.payloads.cast = _ClassStatement.forPayload(SingleMiscTest::cast, "cast", this);
            this.payloads.contains = _ClassStatement.forPayload(SingleMiscTest::contains, "contains", this);
            this.payloads.compose = _ClassStatement.forPayload(SingleMiscTest::compose, "compose", this);
            this.payloads.hide = _ClassStatement.forPayload(SingleMiscTest::hide, "hide", this);
            this.payloads.onErrorResumeWith = _ClassStatement.forPayload(SingleMiscTest::onErrorResumeWith, "onErrorResumeWith", this);
            this.payloads.onErrorReturnValue = _ClassStatement.forPayload(SingleMiscTest::onErrorReturnValue, "onErrorReturnValue", this);
            this.payloads.repeat = _ClassStatement.forPayload(SingleMiscTest::repeat, "repeat", this);
            this.payloads.repeatTimes = _ClassStatement.forPayload(SingleMiscTest::repeatTimes, "repeatTimes", this);
            this.payloads.repeatUntil = _ClassStatement.forPayload(SingleMiscTest::repeatUntil, "repeatUntil", this);
            this.payloads.retry = _ClassStatement.forPayload(SingleMiscTest::retry, "retry", this);
            this.payloads.retryBiPredicate = _ClassStatement.forPayload(SingleMiscTest::retryBiPredicate, "retryBiPredicate", this);
            this.payloads.retryTimes = _ClassStatement.forPayload(SingleMiscTest::retryTimes, "retryTimes", this);
            this.payloads.retryPredicate = _ClassStatement.forPayload(SingleMiscTest::retryPredicate, "retryPredicate", this);
            this.payloads.timeout = _ClassStatement.forPayload(SingleMiscTest::timeout, "timeout", this);
            this.payloads.timeoutOther = _ClassStatement.forPayload(SingleMiscTest::timeoutOther, "timeoutOther", this);
            this.payloads.ignoreElement = _ClassStatement.forPayload(SingleMiscTest::ignoreElement, "ignoreElement", this);
            this.payloads.toObservable = _ClassStatement.forPayload(SingleMiscTest::toObservable, "toObservable", this);
            this.payloads.equals = _ClassStatement.forPayload(SingleMiscTest::equals, "equals", this);
        }
    }
}
