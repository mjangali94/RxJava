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
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.*;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.DefaultObserver;

public class ObservableSwitchIfEmptyTest extends RxJavaTest {

    @Test
    public void switchWhenNotEmpty() throws Exception {
        final AtomicBoolean subscribed = new AtomicBoolean(false);
        final Observable<Integer> o = Observable.just(4).switchIfEmpty(Observable.just(2).doOnSubscribe(new Consumer<Disposable>() {

            @Override
            public void accept(Disposable d) {
                subscribed.set(true);
            }
        }));
        assertEquals(4, o.blockingSingle().intValue());
        assertFalse(subscribed.get());
    }

    @Test
    public void switchWhenEmpty() throws Exception {
        final Observable<Integer> o = Observable.<Integer>empty().switchIfEmpty(Observable.fromIterable(Arrays.asList(42)));
        assertEquals(42, o.blockingSingle().intValue());
    }

    @Test
    public void switchTriggerUnsubscribe() throws Exception {
        final Disposable d = Disposable.empty();
        Observable<Long> withProducer = Observable.unsafeCreate(new ObservableSource<Long>() {

            @Override
            public void subscribe(final Observer<? super Long> observer) {
                observer.onSubscribe(d);
                observer.onNext(42L);
            }
        });
        Observable.<Long>empty().switchIfEmpty(withProducer).lift(new ObservableOperator<Long, Long>() {

            @Override
            public Observer<? super Long> apply(final Observer<? super Long> child) {
                return new DefaultObserver<Long>() {

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Long aLong) {
                        cancel();
                    }
                };
            }
        }).subscribe();
        assertTrue(d.isDisposed());
    // FIXME no longer assertable
    // assertTrue(sub.isUnsubscribed());
    }

    @Test
    public void switchShouldTriggerUnsubscribe() {
        final Disposable d = Disposable.empty();
        Observable.unsafeCreate(new ObservableSource<Long>() {

            @Override
            public void subscribe(final Observer<? super Long> observer) {
                observer.onSubscribe(d);
                observer.onComplete();
            }
        }).switchIfEmpty(Observable.<Long>never()).subscribe();
        assertTrue(d.isDisposed());
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark {

        private _Payloads payloads;

        private ObservableSwitchIfEmptyTest instance;

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchWhenNotEmpty() throws java.lang.Throwable {
            this.payloads.switchWhenNotEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchWhenEmpty() throws java.lang.Throwable {
            this.payloads.switchWhenEmpty.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchTriggerUnsubscribe() throws java.lang.Throwable {
            this.payloads.switchTriggerUnsubscribe.evaluate();
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_switchShouldTriggerUnsubscribe() throws java.lang.Throwable {
            this.payloads.switchShouldTriggerUnsubscribe.evaluate();
        }

        private static class _InstanceStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSwitchIfEmptyTest> payload;

            private final _Benchmark benchmark;

            public _InstanceStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSwitchIfEmptyTest> payload, _Benchmark benchmark) {
                this.payload = payload;
                this.benchmark = benchmark;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.payload.accept(this.benchmark.instance);
            }
        }

        private static class _ClassStatement extends org.junit.runners.model.Statement {

            private final se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSwitchIfEmptyTest> payload;

            private final _Benchmark benchmark;

            private final org.junit.runner.Description description;

            private final org.junit.runners.model.FrameworkMethod frameworkMethod;

            private _ClassStatement(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSwitchIfEmptyTest> payload, _Benchmark benchmark, org.junit.runner.Description description, org.junit.runners.model.FrameworkMethod frameworkMethod) {
                this.payload = payload;
                this.benchmark = benchmark;
                this.description = description;
                this.frameworkMethod = frameworkMethod;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                this.benchmark.instance = new ObservableSwitchIfEmptyTest();
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

            public static org.junit.runners.model.Statement forPayload(se.chalmers.ju2jmh.api.ThrowingConsumer<ObservableSwitchIfEmptyTest> payload, String name, _Benchmark benchmark) {
                org.junit.runner.Description description = se.chalmers.ju2jmh.api.Rules.description(ObservableSwitchIfEmptyTest.class, name);
                org.junit.runners.model.FrameworkMethod frameworkMethod = se.chalmers.ju2jmh.api.Rules.frameworkMethod(ObservableSwitchIfEmptyTest.class, name);
                org.junit.runners.model.Statement statement = new _ClassStatement(payload, benchmark, description, frameworkMethod);
                return statement;
            }
        }

        private static class _Payloads {

            public org.junit.runners.model.Statement switchWhenNotEmpty;

            public org.junit.runners.model.Statement switchWhenEmpty;

            public org.junit.runners.model.Statement switchTriggerUnsubscribe;

            public org.junit.runners.model.Statement switchShouldTriggerUnsubscribe;
        }

        @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
        public void makePayloads() {
            this.payloads = new _Payloads();
            this.payloads.switchWhenNotEmpty = _ClassStatement.forPayload(ObservableSwitchIfEmptyTest::switchWhenNotEmpty, "switchWhenNotEmpty", this);
            this.payloads.switchWhenEmpty = _ClassStatement.forPayload(ObservableSwitchIfEmptyTest::switchWhenEmpty, "switchWhenEmpty", this);
            this.payloads.switchTriggerUnsubscribe = _ClassStatement.forPayload(ObservableSwitchIfEmptyTest::switchTriggerUnsubscribe, "switchTriggerUnsubscribe", this);
            this.payloads.switchShouldTriggerUnsubscribe = _ClassStatement.forPayload(ObservableSwitchIfEmptyTest::switchShouldTriggerUnsubscribe, "switchShouldTriggerUnsubscribe", this);
        }
    }
}
